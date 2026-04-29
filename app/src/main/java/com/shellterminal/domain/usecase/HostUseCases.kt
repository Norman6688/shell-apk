package com.shellterminal.domain.usecase

import com.shellterminal.domain.model.SSHHost
import com.shellterminal.domain.repository.HostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllHostsUseCase @Inject constructor(
    private val repository: HostRepository
) {
    operator fun invoke(): Flow<List<SSHHost>> = repository.getAllHosts()
}

class GetHostUseCase @Inject constructor(
    private val repository: HostRepository
) {
    suspend operator fun invoke(id: String): SSHHost? = repository.getHost(id)
}

class SaveHostUseCase @Inject constructor(
    private val repository: HostRepository
) {
    suspend operator fun invoke(host: SSHHost) = repository.saveHost(host)
}

class DeleteHostUseCase @Inject constructor(
    private val repository: HostRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteHost(id)
}