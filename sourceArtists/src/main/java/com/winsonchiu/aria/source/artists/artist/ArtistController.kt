package com.winsonchiu.aria.source.artists.artist

import android.app.Application
import androidx.fragment.app.Fragment
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import javax.inject.Inject

@ArtistFragmentScreenScope
class ArtistController @Inject constructor(
        private val application: Application
) : FragmentLifecycleBoundComponent() {

    private val artistId by arg(ArtistFragment.Args.artistId)

    override fun onFirstInitialize(fragment: Fragment) {
        super.onFirstInitialize(fragment)
    }
}