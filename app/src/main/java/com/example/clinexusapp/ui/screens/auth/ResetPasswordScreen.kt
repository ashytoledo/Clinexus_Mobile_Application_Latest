package com.example.clinexusapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clinexusapp.ui.components.ElegantTextField
import com.example.clinexusapp.ui.components.ElegantButton
import com.example.clinexusapp.ui.components.ElegantCard
import com.example.clinexusapp.util.Resource
import com.example.clinexusapp.viewmodel.OTPViewModel

@Composable
fun ResetPasswordScreen(
    resetToken: String,
    viewModel: OTPViewModel,
    onResetSuccess: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val otpState by viewModel.otpState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(otpState) {
        if (otpState is Resource.Success) {
            onResetSuccess()
            viewModel.resetState()
        } else if (otpState is Resource.Error) {
            snackbarHostState.showSnackbar(otpState?.message ?: "Reset failed")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "New Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set a strong password to secure your account.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            ElegantCard {
                ElegantTextField(value = newPassword, onValueChange = { newPassword = it }, label = "New Password", icon = Icons.Default.Lock, isPassword = true)
                Spacer(modifier = Modifier.height(16.dp))
                ElegantTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password", icon = Icons.Default.LockReset, isPassword = true)
            }

            Spacer(modifier = Modifier.height(32.dp))

            ElegantButton(
                text = if (otpState is Resource.Loading) "Resetting..." else "Reset Password",
                onClick = {
                    if (newPassword == confirmPassword) {
                        viewModel.resetPassword(resetToken, newPassword)
                    } else {
                        // In a real app, use snackbar
                    }
                },
                enabled = newPassword.isNotEmpty() && newPassword == confirmPassword && otpState !is Resource.Loading
            )
        }
    }
}
