package com.winsonchiu.aria.dialog

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicInteger

open class DialogActivityFragment : Fragment() {

    companion object {

        val KEY_REQUEST_CODE = "${DialogActivityFragment::class.java.canonicalName}.requestCode"

        val KEY_REQUEST_INDEX = "${DialogActivityFragment::class.java.canonicalName}.requestIndex"

        val KEY_BACK_STACK_TAG = "${DialogActivityFragment::class.java.canonicalName}.backStackTag"

        private val atomicCounter = AtomicInteger()

        fun nextBackStackTag() = "$KEY_BACK_STACK_TAG${atomicCounter.incrementAndGet()}"

        private var requestIndexField: Field? = null

        private val FragmentActivity.currentRequestIndex: Int
            get() {
                if (requestIndexField == null) {
                    requestIndexField = FragmentActivity::class.java.getDeclaredField("mNextCandidateRequestIndex")
                            .apply {
                                isAccessible = true
                            }
                }

                return requestIndexField!!.getInt(this)
            }
    }

    fun show(
            fragment: Fragment,
            requestCode: Int
    ) {
        val backStackTag = nextBackStackTag()

        fragment.activity?.let {
            it.supportFragmentManager.beginTransaction()
                    .add((it as DialogActivity).dialogFragmentContainerId, this, null)
                    .addToBackStack(backStackTag)
                    .commit()

            val intent = Intent(fragment.context, DialogDummyActivity::class.java)
            fragment.startActivityForResult(intent, requestCode)

            arguments!!.apply {
                putInt(KEY_REQUEST_INDEX, it.currentRequestIndex)
                putInt(KEY_REQUEST_CODE, requestCode)
                putString(KEY_BACK_STACK_TAG, backStackTag)
            }
        }
    }

    protected fun dismiss() {
        activity?.supportFragmentManager
                ?.popBackStack(arguments!!.getString(KEY_BACK_STACK_TAG), FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    protected fun deliverResultAndDismiss(data: Intent) {
        (activity as? DialogActivity)?.let {
            val requestCode = arguments!!.getInt(KEY_REQUEST_CODE)
            val requestIndex = arguments!!.getInt(KEY_REQUEST_INDEX)

            val wrappedIndex = if (requestIndex == 0) {
                0xffff - 1
            } else {
                requestIndex
            }

            val shiftedCode = ((wrappedIndex) shl 16) + (requestCode and 0xffff)
            it.deliverResult(shiftedCode, Activity.RESULT_OK, data)
            dismiss()
        }
    }
}