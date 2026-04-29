package com.shellterminal.data.repository

import com.shellterminal.data.local.SecureStorage
import com.shellterminal.domain.model.SSHHost
import com.shellterminal.domain.repository.HostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostRepositoryImpl @Inject constructor(
    private val secureStorage: SecureStorage
) : HostRepository {

    override fun getAllHosts(): Flow<List<SSHHost>> = secureStorage.getAllHosts()

    override suspend fun getHost(id: String): SSHHost? = secureStorage.getHost(id)

    override suspend fun saveHost(host: SSHHost) = secureStorage.saveHost(host)

    override suspend fun deleteHost(id: String) = secureStorage.deleteHost(id)
}