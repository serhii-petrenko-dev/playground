package io.xps.playground.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.xps.playground.data.ContentRepoImpl
import io.xps.playground.data.DataStoreManagerImpl
import io.xps.playground.domain.ContentRepo
import io.xps.playground.domain.DataStoreManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideContentRepo(dataStore: DataStoreManager): ContentRepo = ContentRepoImpl(dataStore)

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext appContext: Context): DataStoreManager {
        return DataStoreManagerImpl(appContext)
    }

}