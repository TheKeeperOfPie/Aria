package com.winsonchiu.aria.artwork

import com.winsonchiu.aria.framework.util.FileFilters
import java.io.File
import java.io.IOException
import java.util.Stack

/**
 * Copied [FileTreeWalk] that iterates audio files, image files, and then directions within a depth level
 */
class ArtworkFileTreeWalk private constructor(
        private val start: File,
        private val onEnter: ((File) -> Boolean)?,
        private val onLeave: ((File) -> Unit)?,
        private val onFail: ((f: File, e: IOException) -> Unit)?,
        private val maxDepth: Int = Int.MAX_VALUE
) : Sequence<File> {

    internal constructor(start: File) : this(start, null, null, null)

    /** Returns an iterator walking through files. */
    override fun iterator(): Iterator<File> = FileTreeWalkIterator()

    /** Abstract class that encapsulates file visiting in some order, beginning from a given [root] */
    private abstract class WalkState(val root: File) {
        /** Call of this function proceeds to a next file for visiting and returns it */
        abstract fun step(): File?
    }

    /** Abstract class that encapsulates directory visiting in some order, beginning from a given [rootDir] */
    private abstract class DirectoryState(rootDir: File) : WalkState(rootDir)

    private inner class FileTreeWalkIterator : AbstractIterator<File>() {

        // Stack of directory states, beginning from the start directory
        private val state = Stack<WalkState>()

        init {
            when {
                start.isDirectory -> state.push(TopDownDirectoryState(start))
                start.isFile -> state.push(SingleFileState(start))
                else -> done()
            }
        }

        override fun computeNext() {
            val nextFile = gotoNext()
            if (nextFile != null)
                setNext(nextFile)
            else
                done()
        }

        private tailrec fun gotoNext(): File? {

            if (state.empty()) {
                // There is nothing in the state
                return null
            }
            // Take next file from the top of the stack
            val topState = state.peek()!!
            val file = topState.step()
            if (file == null) {
                // There is nothing more on the top of the stack, go back
                state.pop()
                return gotoNext()
            } else {
                // Check that file/directory matches the filter
                if (file == topState.root || !file.isDirectory || state.size >= maxDepth) {
                    // Proceed to a root directory or a simple file
                    return file
                } else {
                    // Proceed to a sub-directory
                    state.push(TopDownDirectoryState(file))
                    return gotoNext()
                }
            }
        }

        /** Visiting in top-down order */
        private inner class TopDownDirectoryState(rootDir: File) : DirectoryState(rootDir) {

            private var rootVisited = false

            private var fileList: List<File>? = null

            private var fileIndex = 0

            /** First root directory, then all children */
            override fun step(): File? {
                if (!rootVisited) {
                    // First visit root
                    if (onEnter?.invoke(root) == false) {
                        return null
                    }

                    rootVisited = true
                    return root
                } else if (fileList == null || fileIndex < fileList!!.size) {
                    if (fileList == null) {
                        // Then read an array of files, if any

                        val audioFiles = root.listFiles(FileFilters.AUDIO)
                        val imageFiles = root.listFiles(FileFilters.IMAGES)
                        val allFiles = root.listFiles()

                        fileList = (audioFiles + imageFiles + allFiles).distinctBy { it.absolutePath }

                        if (fileList == null) {
                            onFail
                                    ?.invoke(
                                            root,
                                            AccessDeniedException(
                                                    file = root,
                                                    reason = "Cannot list files in a directory"
                                            )
                                    )
                        }
                        if (fileList == null || fileList!!.isEmpty()) {
                            onLeave?.invoke(root)
                            return null
                        }
                    }
                    // Then visit all files
                    return fileList!![fileIndex++]
                } else {
                    // That's all
                    onLeave?.invoke(root)
                    return null
                }
            }
        }

        private inner class SingleFileState(rootFile: File) : WalkState(rootFile) {
            private var visited: Boolean = false

            override fun step(): File? {
                if (visited) return null
                visited = true
                return root
            }
        }

    }

    /**
     * Sets a predicate [function], that is called on any entered directory before its files are visited
     * and before it is visited itself.
     *
     * If the [function] returns `false` the directory is not entered and neither it nor its files are visited.
     */
    fun onEnter(function: (File) -> Boolean): ArtworkFileTreeWalk {
        return ArtworkFileTreeWalk(
                start,
                onEnter = function,
                onLeave = onLeave,
                onFail = onFail,
                maxDepth = maxDepth
        )
    }

    /**
     * Sets a callback [function], that is called on any left directory after its files are visited and after it is visited itself.
     */
    fun onLeave(function: (File) -> Unit): ArtworkFileTreeWalk {
        return ArtworkFileTreeWalk(
                start,
                onEnter = onEnter,
                onLeave = function,
                onFail = onFail,
                maxDepth = maxDepth
        )
    }

    /**
     * Set a callback [function], that is called on a directory when it's impossible to get its file list.
     *
     * [onEnter] and [onLeave] callback functions are called even in this case.
     */
    fun onFail(function: (File, IOException) -> Unit): ArtworkFileTreeWalk {
        return ArtworkFileTreeWalk(
                start,
                onEnter = onEnter,
                onLeave = onLeave,
                onFail = function,
                maxDepth = maxDepth
        )
    }

    /**
     * Sets the maximum [depth] of a directory tree to traverse. By default there is no limit.
     *
     * The value must be positive and [Int.MAX_VALUE] is used to specify an unlimited depth.
     *
     * With a value of 1, walker visits only the origin directory and all its immediate children,
     * with a value of 2 also grandchildren, etc.
     */
    fun maxDepth(depth: Int): ArtworkFileTreeWalk {
        if (depth <= 0)
            throw IllegalArgumentException("depth must be positive, but was $depth.")
        return ArtworkFileTreeWalk(start, onEnter, onLeave, onFail, depth)
    }
}