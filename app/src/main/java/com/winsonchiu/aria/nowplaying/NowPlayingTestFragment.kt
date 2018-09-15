package com.winsonchiu.aria.nowplaying

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.winsonchiu.aria.R
import kotlinx.android.synthetic.main.now_playing_test_fragment.*

class NowPlayingTestFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.now_playing_test_fragment, container, false)

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        textSongDescription.text = "Brave Shine - Aimer"

        seekBarAnimation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
            ) {
                textSongDescription.progress = progress / 1000f
                Log.d("NowPlayingTestFragment", "onProgressChanged called with textSongDescription.progress = ${textSongDescription.progress}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                  // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }
}