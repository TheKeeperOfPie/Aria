package com.winsonchiu.aria.dialog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DialogDummyActivity : AppCompatActivity() {

    companion object {
        const val RESULT_IGNORED = 1121
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_IGNORED)
        finish()
    }
}