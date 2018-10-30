package com.winsonchiu.aria.source.artists.artist

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.framework.fragment.FragmentInitializer
import com.winsonchiu.aria.framework.fragment.arg
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.source.artists.ArtistId
import com.winsonchiu.aria.source.artists.ArtistsRootFragmentDaggerComponent
import com.winsonchiu.aria.source.artists.R
import com.winsonchiu.aria.source.artists.transition.ArtistsToArtistTransition
import kotlinx.android.synthetic.main.artist_fragment.*

class ArtistFragment : BaseFragment<ArtistsRootFragmentDaggerComponent, ArtistFragmentDaggerComponent>() {

    override val layoutId = R.layout.artist_fragment

    override fun makeComponent(parentComponent: ArtistsRootFragmentDaggerComponent) = parentComponent.artistFragmentComponent()

    override fun injectSelf(component: ArtistFragmentDaggerComponent) = component.inject(this)

    object Args : Builder()
    open class Builder : FragmentInitializer<ArtistFragment>({ ArtistFragment() }) {
        val artistId = string<ArtistId, ArtistId>({ it.value }, "artistId") {
            ArtistId(it)
        }
        val imageUri = parcelable<Uri?>("imageUri")
    }

    private val artistId by arg(Args.artistId)
    private val imageUri by arg(Args.imageUri)

    private val imageCallback = object : Callback {
        override fun onSuccess() {
            view?.postOnAnimation {
                startPostponedEnterTransition()
            }
        }

        override fun onError(e: Exception?) {
            startPostponedEnterTransition()
        }
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition(300)

        val artistId = Args.artistId.retrieve(arguments)
        layoutAppBar.transitionName = ArtistsToArtistTransition.header(artistId)
        imageArtistLayout.clipToOutline = true
        Picasso.get()
                .load(imageUri)
                .resize(resources.displayMetrics.widthPixels, 0)
                .onlyScaleDown()
                .into(imageArtist, imageCallback)
    }
}