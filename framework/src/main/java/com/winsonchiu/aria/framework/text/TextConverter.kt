package com.winsonchiu.aria.framework.text

import androidx.annotation.WorkerThread
import com.atilika.kuromoji.ipadic.Token
import com.atilika.kuromoji.ipadic.Tokenizer
import com.moji4j.MojiConverter

private interface Fragment {
    val charSequence: CharSequence
}

private inline class JapaneseFragment(override val charSequence: CharSequence) : Fragment
private inline class DefaultFragment(override val charSequence: CharSequence) : Fragment

object TextConverter {

    private val tokenizer = Tokenizer.Builder().build()

    private val converter = MojiConverter()

    @WorkerThread
    fun translate(
            text: String?,
            titleCase: Boolean = true
    ): String? {
        if (text?.hasJapanese() != true) {
            return text
        }

        val fragments = text.splitToFragments()
        val result = fragments.joinToString(separator = "") {
            if (it !is JapaneseFragment) {
                return@joinToString it.charSequence
            }

            val string = it.charSequence.toString()
            val tokens = tokenizer.tokenize(string)
            tokens.joinTokens {
                val reading = it.reading
                val surface = it.surface

                val textToConvert = when {
                    !it.isKnown || reading == "*" -> surface
                    else -> reading
                }

                val romaji = converter.convertKanaToRomaji(textToConvert)
                when {
                    titleCase -> romaji.titleCase()
                    else -> romaji
                }
            }
        }

        return if (fragments.getOrNull(0) is JapaneseFragment) {
            result.capitalize()
        } else {
            result
        }
    }

    private fun Iterable<Token>.joinTokens(
            transform: ((Token) -> CharSequence)
    ): String {
        val builder = StringBuilder()
        var previous: CharSequence? = null

        for ((index, element) in this.withIndex()) {
            val result = transform(element)

            if (index + 1 > 1 && result.isNotEmpty() && (previous?.first()?.isWhitespace() != true)) {
                if (result.length > 1
                        || result.firstOrNull()?.isUpperCase() == true
                        || result.firstOrNull()?.isLowerCase() == true) {
                    builder.append(" ")
                }
            }

            if (result.isNotEmpty()) {
                builder.append(result)
                previous = result
            }
        }
        return builder.toString()
    }

    private fun CharSequence.hasJapanese() = any { it.isJapanese() }

    private fun Char.isJapanese(): Boolean {
        return Character.UnicodeBlock.of(this) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || Character.UnicodeBlock.of(this) == Character.UnicodeBlock.HIRAGANA
                || Character.UnicodeBlock.of(this) == Character.UnicodeBlock.KATAKANA
                || Character.UnicodeBlock.of(this) == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS
                || Character.UnicodeBlock.of(this) == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || Character.UnicodeBlock.of(this) == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || Character.UnicodeBlock.of(this) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
    }

    private fun CharSequence.splitToFragments(): List<Fragment> {
        if (length == 0) {
            return emptyList()
        }

        val fragments = mutableListOf<Fragment>()

        var currentIndex = 0

        var wasJapanese = get(0).isJapanese()

        fun addFragment(fragment: CharSequence) {
            fragments += if (wasJapanese) {
                val lastFragment = fragments.lastOrNull()
                val shouldPrefixWhitespace = lastFragment !is JapaneseFragment
                        && lastFragment != null
                        && !lastFragment.charSequence.last().isWhitespace()

                JapaneseFragment("${if (shouldPrefixWhitespace) " " else ""}$fragment")
            } else {
                DefaultFragment(fragment)
            }
        }

        for ((index, char) in this.withIndex()) {
            if (char.isWhitespace()) {
                continue
            }

            if (char.isJapanese() != wasJapanese) {
                val fragment = subSequence(currentIndex, index)
                val fragmentTrimmed = if (wasJapanese) fragment.trimEnd() else fragment

                val difference = fragment.length - fragmentTrimmed.length

                addFragment(fragmentTrimmed)

                currentIndex = index - difference
                wasJapanese = !wasJapanese
            }
        }

        val fragment = subSequence(currentIndex, length)

        if (!fragment.isEmpty()) {
            addFragment(fragment)
        }

        return fragments
    }

    private fun CharSequence.titleCase(): String {
        if (isBlank()) {
            return toString()
        }

        val prefix = takeWhile { it.isWhitespace() }
        val suffix = takeWhile { it.isWhitespace() }

        val result = splitToSequence(' ')
                .map {
                    when (it) {
                        "ba",
                        "da",
                        "de",
                        "e",
                        "ga",
                        "ka",
                        "kai",
                        "kara",
                        "kke",
                        "me",
                        "mo",
                        "na",
                        "naa",
                        "ne",
                        "ni",
                        "no",
                        "o",
                        "ra",
                        "sa",
                        "saa",
                        "shi",
                        "te",
                        "to",
                        "tte",
                        "wa",
                        "wo",
                        "ya",
                        "yo",
                        "ze",
                        "zo",
                        "zu" -> it
                        else -> it.capitalize()
                    }
                }
                .joinToString(separator = " ")

        return "$prefix$result$suffix"
    }
}