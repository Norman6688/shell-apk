package com.shellterminal.domain.model

enum class AuthType {
    PASSWORD,
    PRIVATE_KEY
}

data class SSHHost(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType = AuthType.PASSWORD,
    val password: String? = null,
    val privateKey: String? = null,
    val passphrase: String? = null
)