package com.example.clinexusapp.ui.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clinexusapp.ui.components.*
import com.example.clinexusapp.ui.theme.*
import com.example.clinexusapp.ui.navigation.Screen
import com.example.clinexusapp.viewmodel.HistoryViewModel
import com.example.clinexusapp.util.Resource

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentHistoryScreen(
    onBack: () -> Unit,
    onNavigateToBooking: () -> Unit,
    viewModel: HistoryViewModel
) {
    val historyState by viewModel.historyState.collectAsState()
    var selectedAppointment by remember { mutableStateOf<HistoryAppointment?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }
    
    var cancelReason by remember { mutableStateOf("") }
    var rescheduleNote by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1. Initial Choice Dialog
    if (showActionDialog && selectedAppointment != null) {
        val appt = selectedAppointment!!
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Manage Visit", fontWeight = FontWeight.Black, color = RoyalNavy) },
            text = { Text("What would you like to do with your appointment with ${appt.title}?") },
            confirmButton = {
                Button(
                    onClick = { 
                        showActionDialog = false
                        showRescheduleDialog = true 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                ) {
                    Text("RESCHEDULE")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showActionDialog = false
                    showCancelDialog = true
                }) {
                    Text("CANCEL VISIT", color = DarkRed)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // 2. Cancellation Reason Dialog
    if (showCancelDialog && selectedAppointment != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Appointment", fontWeight = FontWeight.Bold, color = DarkRed) },
            text = {
                Column {
                    Text("Please tell us why you are cancelling:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        placeholder = { Text("Enter reason...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cancelReason.isNotBlank()) {
                            viewModel.cancelAppointment(selectedAppointment!!.id, cancelReason)
                            showCancelDialog = false
                            selectedAppointment = null
                            cancelReason = ""
                            scope.launch { snackbarHostState.showSnackbar("Cancellation Request Sent") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRed),
                    enabled = cancelReason.isNotBlank()
                ) {
                    Text("CONFIRM CANCEL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("BACK") }
            }
        )
    }

    // 3. Reschedule Note Dialog
    if (showRescheduleDialog && selectedAppointment != null) {
        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            title = { Text("Request Reschedule", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    Text("When would you like to move this appointment? (e.g. Next Monday morning)")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rescheduleNote,
                        onValueChange = { rescheduleNote = it },
                        placeholder = { Text("Enter your preference...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rescheduleNote.isNotBlank()) {
                            // Note: We use placeholders for date/time as it's a "request" for now
                            viewModel.rescheduleAppointment(selectedAppointment!!.id, "2026-01-01", "00:00", "00:00", rescheduleNote)
                            showRescheduleDialog = false
                            selectedAppointment = null
                            rescheduleNote = ""
                            scope.launch { snackbarHostState.showSnackbar("Reschedule Request Sent") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                    enabled = rescheduleNote.isNotBlank()
                ) {
                    Text("SEND REQUEST")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDialog = false }) { Text("BACK") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ElegantTopAppBar(
                title = "My Appointments",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = historyState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message ?: "An error occurred", color = Color.Red)
                    }
                }
                is Resource.Success -> {
                    val appointmentsList = state.data ?: emptyList()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
                    ) {
                        item {
                            Text(
                                text = "Scheduled Visits",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        items(appointmentsList) { appointment ->
                            val isPending = appointment.appointmentStatus.contains("Pending", ignoreCase = true)
                            NeumorphicCard(modifier = Modifier.fillMaxWidth().clickable {
                                selectedAppointment = HistoryAppointment(
                                    appointment.appointmentId,
                                    appointment.doctor,
                                    "${com.example.clinexusapp.util.DateUtils.formatDisplayDate(appointment.appointmentDate)} • ${com.example.clinexusapp.util.DateUtils.formatDisplayTime(appointment.startTime)}",
                                    Icons.Default.MedicalServices,
                                    VibrantTeal
                                )
                                showActionDialog = true
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(VibrantTeal.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MedicalServices, null, tint = VibrantTeal, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appointment.doctor, 
                                            fontWeight = FontWeight.Bold, 
                                            color = MaterialTheme.colorScheme.onSurface, 
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "${com.example.clinexusapp.util.DateUtils.formatDisplayDate(appointment.appointmentDate)} • ${com.example.clinexusapp.util.DateUtils.formatDisplayTime(appointment.startTime)}", 
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), 
                                            fontSize = 13.sp
                                        )
                                        if (isPending) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text(
                                                    text = appointment.appointmentStatus.uppercase(), 
                                                    fontSize = 9.sp, 
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                                        null, 
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Need a New Appointment?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        item {
                            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    VibrantButton(
                                        text = "Book Appointment",
                                        onClick = onNavigateToBooking
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class HistoryAppointment(val id: Int, val title: String, val date: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)
