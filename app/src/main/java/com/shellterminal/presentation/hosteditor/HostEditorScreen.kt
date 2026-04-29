package com.shellterminal.presentation.hosteditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shellterminal.domain.model.AuthType

@Composable
fun HostEditorScreen(
    hostId: String? = null,
    initialHost: com.shellterminal.domain.model.SSHHost? = null,
    onSave: (com.shellterminal.domain.model.SSHHost) -> Unit,
    onCancel: () -> Unit,
    viewModel: HostEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialHost) {
        viewModel.loadHost(initialHost)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (hostId.isNullOrEmpty()) "添加主机" else "编辑主机") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(HostEditorEvent.NameChanged(it)) },
                label = { Text("主机名称 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.host,
                onValueChange = { viewModel.onEvent(HostEditorEvent.HostChanged(it)) },
                label = { Text("主机地址 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("如 192.168.1.100 或 example.com") }
            )

            OutlinedTextField(
                value = state.port,
                onValueChange = { viewModel.onEvent(HostEditorEvent.PortChanged(it)) },
                label = { Text("端口") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = { viewModel.onEvent(HostEditorEvent.UsernameChanged(it)) },
                label = { Text("用户名 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("认证方式", style = MaterialTheme.typography.titleMedium)

            Column(Modifier.selectableGroup()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.authType == AuthType.PASSWORD,
                            onClick = { viewModel.onEvent(HostEditorEvent.AuthTypeChanged(AuthType.PASSWORD)) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.authType == AuthType.PASSWORD,
                        onClick = null
                    )
                    Text("密码认证", modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.authType == AuthType.PRIVATE_KEY,
                            onClick = { viewModel.onEvent(HostEditorEvent.AuthTypeChanged(AuthType.PRIVATE_KEY)) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.authType == AuthType.PRIVATE_KEY,
                        onClick = null
                    )
                    Text("密钥认证", modifier = Modifier.padding(start = 8.dp))
                }
            }

            when (state.authType) {
                AuthType.PASSWORD -> {
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(HostEditorEvent.PasswordChanged(it)) },
                        label = { Text("密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                AuthType.PRIVATE_KEY -> {
                    OutlinedTextField(
                        value = state.privateKey,
                        onValueChange = { viewModel.onEvent(HostEditorEvent.PrivateKeyChanged(it)) },
                        label = { Text("私钥内容") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 10
                    )

                    OutlinedTextField(
                        value = state.passphrase,
                        onValueChange = { viewModel.onEvent(HostEditorEvent.PassphraseChanged(it)) },
                        label = { Text("密钥口令（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            }

            state.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        viewModel.getHost()?.let { onSave(it) }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }
            }
        }
    }
}