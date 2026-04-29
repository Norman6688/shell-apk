package com.shellterminal.domain.repository

import com.shellterminal.domain.model.SSHHost
import kotlinx.coroutines.flow.Flow

interface HostRepository {
    fun getAllHosts(): Flow<List<SSHHost>>
    suspend fun getHost(id: String): SSHHost?
    suspend fun saveHost(host: SSHHost)
    suspend fun deleteHost(id: String)
}