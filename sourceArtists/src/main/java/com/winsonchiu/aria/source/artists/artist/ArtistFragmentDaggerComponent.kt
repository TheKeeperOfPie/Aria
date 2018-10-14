package com.winsonchiu.aria.source.artists.artist

import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
annotation class ArtistFragmentScreenScope

@ArtistFragmentScreenScope
@Subcomponent(
        modules = [
            ArtistFragmentModule::class
        ]
)
interface ArtistFragmentDaggerComponent {

    fun inject(fragment: ArtistFragment)
}

@Module
class ArtistFragmentModule {

    @Provides
    @IntoSet
    @ArtistFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = FragmentDefaultBoundModule.DefaultBoundComponent

    @Provides
    @IntoSet
    @ArtistFragmentScreenScope
    fun bindArtistController(artistController: ArtistController): FragmentLifecycleBoundComponent = artistController
}