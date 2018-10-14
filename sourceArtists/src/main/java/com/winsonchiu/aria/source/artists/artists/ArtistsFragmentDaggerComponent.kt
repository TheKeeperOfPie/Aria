package com.winsonchiu.aria.source.artists.artists

import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
annotation class ArtistsFragmentScreenScope

@ArtistsFragmentScreenScope
@Subcomponent(
        modules = [
            ArtistsFragmentModule::class
        ]
)
interface ArtistsFragmentDaggerComponent {

    fun inject(fragment: ArtistsFragment)
}

@Module
class ArtistsFragmentModule {

    @Provides
    @IntoSet
    @ArtistsFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = FragmentDefaultBoundModule.DefaultBoundComponent

    @Provides
    @IntoSet
    @ArtistsFragmentScreenScope
    fun bindArtistsController(artistsController: ArtistsController): FragmentLifecycleBoundComponent = artistsController
}