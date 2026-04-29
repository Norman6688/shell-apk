package com.shellterminal.di

import com.shellterminal.data.repository.HostRepositoryImpl
import com.shellterminal.domain.repository.HostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHostRepository(impl: HostRepositoryImpl): HostRepository
}