package com.shellterminal.domain.usecase

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.shellterminal.domain.model.AuthType
import com.shellterminal.domain.model.SSHHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

@Singleton
class SSHConnectionManager @Inject constructor() {
    private var session: Session? = null
    private var channel: com.jcraft.jsch.Channel? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _output = MutableStateFlow("")
    val output: StateFlow<String> = _output

    suspend fun connect(host: SSHHost): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.Connecting
            disconnect()

            val jsch = JSch()

            if (host.authType == AuthType.PRIVATE_KEY && host.privateKey != null) {
                if (host.passphrase.isNullOrEmpty()) {
                    jsch.addIdentity("key", host.privateKey.toByteArray(), null, null)
                } else {
                    jsch.addIdentity("key", host.privateKey.toByteArray(), null, host.passphrase.toByteArray())
                }
            }

            session = jsch.getSession(host.username, host.host, host.port)

            if (host.authType == AuthType.PASSWORD && host.password != null) {
                session?.setPassword(host.password)
            }

            session?.setConfig("StrictHostKeyChecking", "no")
            session?.setConfig("PreferredAuthentications", when (host.authType) {
                AuthType.PASSWORD -> "password"
                AuthType.PRIVATE_KEY -> "publickey"
            })

            session?.connect(30000)
            channel = session?.openChannel("shell")

            val inputStream = PipedInputStream()
            val outputStream = PipedOutputStream(inputStream)

            channel?.inputStream = inputStream
            channel?.outputStream = outputStream

            // Set up PTY
            val term = System.getenv("TERM") ?: "xterm-256color"
            val termsize = Pair(80, 24)
            (channel as? com.jcraft.jsch.ChannelShell)?.setPtySize(termsize.first, termsize.second, 640, 480)

            channel?.connect()

            _connectionState.value = ConnectionState.Connected

            // Start reading output in background
            launchOutputReader(outputStream)

            Result.success(Unit)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "连接失败")
            Result.failure(e)
        }
    }

    private fun launchOutputReader(outputStream: PipedOutputStream) {
        // Output will be read from the channel's input stream
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            channel?.outputStream?.write("$command\n".toByteArray())
            channel?.outputStream?.flush()
            Result.success("")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendInput(data: String) = withContext(Dispatchers.IO) {
        try {
            channel?.outputStream?.write(data.toByteArray())
            channel?.outputStream?.flush()
        } catch (e: Exception) {
            // Handle write error
        }
    }

    fun disconnect() {
        try {
            channel?.disconnect()
            session?.disconnect()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        channel = null
        session = null
        _connectionState.value = ConnectionState.Disconnected
    }

    fun isConnected(): Boolean = session?.isConnected == true && channel?.isConnected == true
}