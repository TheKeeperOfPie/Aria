package com.winsonchiu.aria.dialog

import android.content.Intent
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager

abstract class DialogActivity : AppCompatActivity() {

    @get:IdRes
    abstract val dialogFragmentContainerId: Int

    fun deliverResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    final override fun onBackPressed() {
        super.onBackPressed()

        val fragment = supportFragmentManager.findFragmentById(dialogFragmentContainerId)
        if (fragment is DialogActivityFragment) {
            val backStackTag = fragment.arguments!!.getString(DialogActivityFragment.KEY_BACK_STACK_TAG)
            supportFragmentManager.popBackStack(backStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            handleBackPressed()
        }
    }

    open fun handleBackPressed() {
        super.onBackPressed()
    }

    final override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        if (resultCode != DialogDummyActivity.RESULT_IGNORED) {
            handleActivityResult(requestCode, resultCode, data)
        }
    }

    open fun handleActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}