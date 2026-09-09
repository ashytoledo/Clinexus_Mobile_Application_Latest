package com.example.clinexusapp.ui.screens.appointments

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.clinexusapp.model.*
import com.example.clinexusapp.ui.components.*
import com.example.clinexusapp.ui.theme.*
import com.example.clinexusapp.util.NotificationHelper
import com.example.clinexusapp.util.Resource
import com.example.clinexusapp.viewmodel.BookingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SelectionCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scale by animateFloatAsState(if (isSelected) 0.98f else 1f, label = "scale")
    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isSelected) DeepTeal.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.1f),
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isSelected) ActionButtonGradient else Brush.linearGradient(listOf(White, White)),
                )
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            content()
        }
    }
}

@Composable
fun BookingSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = DeepTeal, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Black,
            fontSize = 19.sp,
            color = RoyalNavy,
            letterSpacing = (-0.5).sp,
        )
    }
}

@Composable
fun BookingDateSelector(
    selected: String,
    enabledDays: List<String>? = null,
    onSelect: (String) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    val dates = remember(enabledDays, locale) {
        val calendar = Calendar.getInstance()
        val sdfDisplay = SimpleDateFormat("EEE dd", locale)
        val sdfValue = SimpleDateFormat("yyyy-MM-dd", locale)
        val sdfDayName = SimpleDateFormat("EEEE", Locale.US)
        List(14) {
            val date = calendar.time
            val display = sdfDisplay.format(date).uppercase()
            val value = sdfValue.format(date)
            val dayName = sdfDayName.format(date)
            val isEnabled = (enabledDays == null) || enabledDays.any { it.equals(dayName, ignoreCase = true) }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            DateOption(display, value, isEnabled)
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        items(dates) { option ->
            val isSelected = selected == option.value
            Surface(
                onClick = { if (option.isEnabled) onSelect(option.value) },
                modifier = Modifier
                    .width(96.dp)
                    .height(110.dp)
                    .shadow(
                        elevation = if (isSelected) 12.dp else 2.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = if (isSelected) {
                            if (option.isEnabled) DeepTeal.copy(alpha = 0.4f) else DarkRed.copy(alpha = 0.4f)
                        } else Color.Black.copy(alpha = 0.05f),
                    ),
                shape = RoundedCornerShape(22.dp),
                color = when {
                    isSelected && option.isEnabled -> VibrantTeal
                    isSelected && !option.isEnabled -> DarkRed
                    else -> White
                },
                border = if (!option.isEnabled && !isSelected) androidx.compose.foundation.BorderStroke(1.dp, DarkRed.copy(alpha = 0.3f)) else null
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = option.display.split(" ").first(),
                        color = when {
                            isSelected -> White.copy(alpha = 0.8f)
                            option.isEnabled -> SlateGray
                            else -> DarkRed.copy(alpha = 0.6f)
                        },
                        fontSize = 14.sp,
                        fontWeight = if (option.isEnabled) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.display.split(" ").last(),
                        color = when {
                            isSelected -> White
                            option.isEnabled -> RoyalNavy
                            else -> DarkRed
                        },
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

data class DateOption(val display: String, val value: String, val isEnabled: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentBookingScreen(
    onBack: () -> Unit,
    onBookSuccess: () -> Unit,
    viewModel: BookingViewModel = viewModel(),
    doctorName: String = "Dr. Olivia Bennett"
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    var selectedSlot by remember { mutableStateOf<AvailableSlotDTO?>(null) }
    var isBookingConfirmed by remember { mutableStateOf(value = false) }

    val dentistsState by viewModel.dentistsState.collectAsState()
    val servicesState by viewModel.servicesState.collectAsState()
    val selectedDentist by viewModel.selectedDentist.collectAsState()
    val selectedServices by viewModel.selectedServices.collectAsState()
    val availableTimeslots by viewModel.availableTimeslots.collectAsState()
    val bookingState by viewModel.bookingState.collectAsState()
    val dentistScheduleState by viewModel.dentistSchedule.collectAsState()

    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is Resource.Success -> {
                NotificationHelper.showBookingNotification(context, selectedDentist?.dentistName ?: doctorName)
                isBookingConfirmed = true
                viewModel.resetState()
            }
            is Resource.Error -> {
                android.widget.Toast.makeText(context, state.message ?: "Booking failed", android.widget.Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            ElegantTopAppBar(
                title = "Schedule Visit",
                onBack = if (isBookingConfirmed) null else onBack
            )
        },
        containerColor = SoftMist
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Dentist Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BookingSectionHeader("Select Dentist", Icons.Default.Person)
                    when (val dState = dentistsState) {
                        is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VibrantTeal)
                        }
                        is Resource.Error -> Text(dState.message ?: "Error", color = Color.Red, modifier = Modifier.padding(horizontal = 24.dp))
                        is Resource.Success -> {
                            val dentists = dState.data
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                dentists.chunked(2).forEach { rowDentists ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        rowDentists.forEach { dentist ->
                                            DentistSelectionItem(
                                                dentist = dentist,
                                                isSelected = selectedDentist?.dentistId == dentist.dentistId,
                                                onClick = {
                                                    viewModel.selectDentist(dentist)
                                                    selectedSlot = null
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowDentists.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                // Services Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BookingSectionHeader("Select Service", Icons.Default.MedicalServices)
                    when (val sState = servicesState) {
                        is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VibrantTeal)
                        }
                        is Resource.Error -> Text(sState.message ?: "Error", color = Color.Red, modifier = Modifier.padding(horizontal = 24.dp))
                        is Resource.Success -> {
                            val services = sState.data
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                services.chunked(2).forEach { rowServices ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        rowServices.forEach { service ->
                                            BookingServiceItem(
                                                service = service,
                                                isSelected = selectedServices.any { it.serviceId == service.serviceId },
                                                onClick = {
                                                    viewModel.toggleService(service)
                                                    selectedSlot = null
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowServices.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                // Date Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BookingSectionHeader("Select Date", Icons.Default.Event)
                    val workingDays = when (val scheduleState = dentistScheduleState) {
                        is Resource.Success -> scheduleState.data.workingDays
                        else -> null
                    }
                    BookingDateSelector(
                        selected = selectedDate,
                        enabledDays = workingDays
                    ) {
                        viewModel.checkAndFetchTimeslots(it)
                        selectedSlot = null
                    }
                }

                // Time Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BookingSectionHeader("Preferred Time", Icons.Default.AccessTime)
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        val configuration = LocalConfiguration.current
                        val locale = configuration.locales[0]
                        val workingDays = when (val scheduleState = dentistScheduleState) {
                            is Resource.Success -> scheduleState.data.workingDays
                            else -> null
                        }
                        val isNonWorkingDay = (workingDays != null) && selectedDate.isNotEmpty() && run {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", locale)
                            val dayFormat = SimpleDateFormat("EEEE", Locale.US)
                            try {
                                val date = sdf.parse(selectedDate)
                                val dayName = dayFormat.format(date!!)
                                workingDays.none { it.equals(dayName, ignoreCase = true) }
                            } catch (_: Exception) {
                                false
                            }
                        }

                        if (isNonWorkingDay) {
                            Text(
                                text = "Dr. ${selectedDentist?.dentistName ?: doctorName} has no availability for the selected day.",
                                color = DarkRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            when (val aState = availableTimeslots) {
                                is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = VibrantTeal)
                                }
                                is Resource.Error -> Text(aState.message ?: "Select dentist first", color = SlateGray, modifier = Modifier.padding(16.dp))
                                is Resource.Success -> {
                                    val slots = aState.data
                                    if (slots.isEmpty() && selectedDate.isNotEmpty()) {
                                        Text(
                                            text = "Dr. ${selectedDentist?.dentistName ?: doctorName} has no availability for the selected day.",
                                            color = DarkRed,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    } else if (slots.isNotEmpty()) {
                                        BookingTimeGrid(
                                            slots = slots,
                                            selectedSlot = selectedSlot
                                        ) {
                                            selectedSlot = it
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(140.dp))
            }

            // Bottom Continue Button
            AnimatedVisibility(
                visible = (selectedSlot != null) && selectedServices.isNotEmpty() && (selectedDentist != null) && (!isBookingConfirmed),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        VibrantButton(
                            text = if (bookingState is Resource.Loading) "Processing..." else "Authorize Booking",
                            onClick = {
                                viewModel.createAppointment(selectedDate, selectedSlot!!)
                            },
                            enabled = bookingState !is Resource.Loading
                        )
                    }
                }
            }

            if (isBookingConfirmed) {
                BookingSuccessOverlay(
                    doctorName = selectedDentist?.dentistName ?: doctorName,
                    date = selectedDate,
                    time = selectedSlot?.label ?: "",
                    onDone = onBookSuccess
                )
            }
        }
    }
}

@Composable
fun DentistSelectionItem(dentist: DentistDTO, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SelectionCard(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier.height(105.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) White.copy(alpha = 0.2f) else SoftMist),
                contentAlignment = Alignment.Center
            ) {
                if (dentist.profileImage.isNullOrBlank()) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = if (isSelected) White else DeepTeal,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    AsyncImage(
                        model = dentist.profileImage,
                        contentDescription = dentist.dentistName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = dentist.dentistName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isSelected) White else RoyalNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Availability:",
                        fontSize = 12.sp,
                        color = if (isSelected) White.copy(alpha = 0.8f) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = formatAvailability(dentist.daysOfWeek),
                    fontSize = 12.sp,
                    color = if (isSelected) White.copy(alpha = 0.8f) else RoyalNavy,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
        }
    }
}

private fun formatAvailability(days: String?): String {
    if (days.isNullOrBlank()) return "Not specified"

    val abbreviations = listOf("Monday" to "Mon", "Tuesday" to "Tue", "Wednesday" to "Wed", "Thursday" to "Thur", "Friday" to "Fri", "Saturday" to "Sat", "Sunday" to "Sun")
    val selected = days.split(",")
        .map { it.trim() }
        .mapNotNull { day -> abbreviations.indexOfFirst { it.first.equals(day, ignoreCase = true) }.takeIf { it >= 0 } }
        .distinct()
        .sorted()

    if (selected.isEmpty()) return "Not specified"

    val parts = mutableListOf<String>()
    var index = 0
    while (index < selected.size) {
        var end = index
        while (end + 1 < selected.size && selected[end + 1] == selected[end] + 1) end++
        parts += if (end - index >= 2) {
            "${abbreviations[selected[index]].second}-${abbreviations[selected[end]].second}"
        } else {
            (index..end).joinToString(", ") { abbreviations[selected[it]].second }
        }
        index = end + 1
    }
    return parts.joinToString(", ")
}

@Composable
fun BookingServiceItem(service: BookableServiceDTO, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SelectionCard(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier.height(92.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = service.serviceName ?: "Unknown Service",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isSelected) White else RoyalNavy,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = service.price?.let { "PHP ${String.format(Locale.US, "%,.2f", it)}" } ?: "Price unavailable",
                    fontSize = 12.sp,
                    color = if (isSelected) White.copy(alpha = 0.85f) else DeepTeal,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BookingTimeGrid(slots: List<AvailableSlotDTO>, selectedSlot: AvailableSlotDTO?, onSlotSelected: (AvailableSlotDTO) -> Unit) {
    val rows = slots.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowSlots ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowSlots.forEach { slot ->
                    Box(modifier = Modifier.weight(1f)) {
                        val isSelected = selectedSlot?.startTime == slot.startTime
                        Surface(
                            onClick = { onSlotSelected(slot) },
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = if (isSelected) 6.dp else 1.dp,
                            color = if (isSelected) VibrantTeal else White
                        ) {
                            Box(modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = slot.label ?: "Unavailable",
                                    color = if (isSelected) White else RoyalNavy,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                repeat(3 - rowSlots.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun BookingSuccessOverlay(doctorName: String, date: String, time: String, onDone: () -> Unit) {
    val primaryColor = DeepTeal
    val onPrimaryColor = White
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val currentOnDone by rememberUpdatedState(onDone)
    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        launch {
            alpha.animateTo(1f, tween(800))
        }
        delay(3000.milliseconds)
        currentOnDone()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = primaryColor.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .scale(scale.value)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(onPrimaryColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = onPrimaryColor, modifier = Modifier.size(70.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Session Authorized", color = onPrimaryColor, fontWeight = FontWeight.Black, fontSize = 28.sp)
            Text("REDIRECTING TO HOME...", color = onPrimaryColor.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(48.dp))
            NeumorphicCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("APPOINTMENT PASS", color = primaryColor, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(doctorName, color = RoyalNavy, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(date, color = RoyalNavy.copy(alpha = 0.6f), fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(time, color = primaryColor, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("REF ID: #CX-${UUID.randomUUID().toString().take(6).uppercase()}", color = RoyalNavy.copy(alpha = 0.3f), fontSize = 10.sp)
                }
            }
        }
    }
}
