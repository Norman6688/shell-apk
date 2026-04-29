package com.shellterminal.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shellterminal.domain.model.SSHHost
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "ssh_hosts_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()
    private val _hostsFlow = MutableStateFlow<List<SSHHost>>(emptyList())

    init {
        _hostsFlow.value = loadHosts()
    }

    fun getAllHosts(): Flow<List<SSHHost>> = _hostsFlow.asStateFlow()

    fun getHost(id: String): SSHHost? {
        return _hostsFlow.value.find { it.id == id }
    }

    fun saveHost(host: SSHHost) {
        val currentHosts = _hostsFlow.value.toMutableList()
        val existingIndex = currentHosts.indexOfFirst { it.id == host.id }

        if (existingIndex >= 0) {
            currentHosts[existingIndex] = host
        } else {
            currentHosts.add(host)
        }

        saveHosts(currentHosts)
        _hostsFlow.value = currentHosts
    }

    fun deleteHost(id: String) {
        val currentHosts = _hostsFlow.value.toMutableList()
        currentHosts.removeAll { it.id == id }
        saveHosts(currentHosts)
        _hostsFlow.value = currentHosts
    }

    private fun loadHosts(): List<SSHHost> {
        val json = sharedPreferences.getString(KEY_HOSTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SSHHost>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHosts(hosts: List<SSHHost>) {
        val json = gson.toJson(hosts)
        sharedPreferences.edit().putString(KEY_HOSTS, json).apply()
    }

    companion object {
        private const val KEY_HOSTS = "ssh_hosts"
    }
}