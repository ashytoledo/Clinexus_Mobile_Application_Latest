package com.example.clinexusapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clinexusapp.ui.components.ElegantButton
import com.example.clinexusapp.util.Resource
import com.example.clinexusapp.viewmodel.OTPViewModel

@Composable
fun OTPVerificationScreen(
    email: String,
    viewModel: OTPViewModel,
    isPasswordReset: Boolean = false,
    onVerifySuccess: (String?) -> Unit
) {
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    
    val otpState by viewModel.otpState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(otpState) {
        if (otpState is Resource.Success) {
            onVerifySuccess(otpState?.data?.resetToken)
            viewModel.resetState()
        } else if (otpState is Resource.Error) {
            snackbarHostState.showSnackbar(otpState?.message ?: "Verification failed")
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
                text = if (isPasswordReset) "OTP Verification" else "Verify Email",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the 6-digit code sent to\n$email",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                otpValues.forEachIndexed { index, value ->
                    OtpBox(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otpValues[index] = newValue
                                if (newValue.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }
                        },
                        onBackspace = {
                            if (value.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequesters[index])
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            ElegantButton(
                text = if (otpState is Resource.Loading) "Verifying..." else "Verify",
                onClick = { 
                    val fullOtp = otpValues.joinToString("")
                    viewModel.verifyOtp(email, fullOtp, isPasswordReset) 
                },
                enabled = otpValues.all { it.isNotEmpty() } && otpState !is Resource.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Code not received? ", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                TextButton(onClick = { viewModel.resendOtp(email) }) {
                    Text(
                        text = "Resend", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OtpBox(
    value: String,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .aspectRatio(1f)
            .onKeyEvent {
                if (it.key == Key.Backspace && it.type == KeyEventType.KeyDown) {
                    onBackspace()
                }
                false
            },
        textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}
