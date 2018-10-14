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
import com.winsonchiu.aria.source.artists.ArtistsToArtistTransition
import com.winsonchiu.aria.source.artists.R
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
            startPostponedEnterTransition()
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

        postponeEnterTransition(150)

        val artistId = Args.artistId.retrieve(arguments)
        imageArtist.transitionName = ArtistsToArtistTransition.image(artistId)
        Picasso.get().load(imageUri).into(imageArtist, imageCallback)
    }
}