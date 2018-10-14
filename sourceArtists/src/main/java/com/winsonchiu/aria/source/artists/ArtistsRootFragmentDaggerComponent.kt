package com.winsonchiu.aria.source.artists

import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.source.artists.artist.ArtistFragmentDaggerComponent
import com.winsonchiu.aria.source.artists.artists.ArtistsFragmentDaggerComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
annotation class ArtistsRootFragmentScreenScope

@ArtistsRootFragmentScreenScope
@Subcomponent(
        modules = [
            ArtistsRootFragmentModule::class
        ]
)
interface ArtistsRootFragmentDaggerComponent {

    fun artistsFragmentComponent() : ArtistsFragmentDaggerComponent

    fun artistFragmentComponent() : ArtistFragmentDaggerComponent

    fun inject(fragment: ArtistsRootFragment)

    interface ComponentProvider {
        fun artistsRootFragmentComponent(): ArtistsRootFragmentDaggerComponent
    }
}

@Module
class ArtistsRootFragmentModule {

    @Provides
    @IntoSet
    @ArtistsRootFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = FragmentDefaultBoundModule.DefaultBoundComponent
}