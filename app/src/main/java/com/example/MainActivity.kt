package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ArticleCategory
import com.example.data.DailyQuranVerse
import com.example.data.IslamicArticle
import com.example.data.SavedVerse
import com.example.network.QuranVerseResponse
import com.example.notification.NotificationHelper
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.RamadanDay
import com.example.viewmodel.IslamicEvent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.cos

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Prayer alarms notifications enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission denied. Alarms won't show visual alerts.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(this)

        // Request runtime notification permission on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme {
                IslamicCompanionApp()
            }
        }
    }
}

@Composable
fun IslamicCompanionApp() {
    val viewModel: MainViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Prayers") },
                    label = { Text("Prayers") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicGreenDark,
                        selectedTextColor = IslamicGreenDark,
                        indicatorColor = WarmGoldAccent,
                        unselectedIconColor = IslamicGreenLight,
                        unselectedTextColor = IslamicGreenLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Qibla") },
                    label = { Text("Qibla") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicGreenDark,
                        selectedTextColor = IslamicGreenDark,
                        indicatorColor = WarmGoldAccent,
                        unselectedIconColor = IslamicGreenLight,
                        unselectedTextColor = IslamicGreenLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Wisdom") },
                    label = { Text("Wisdom") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicGreenDark,
                        selectedTextColor = IslamicGreenDark,
                        indicatorColor = WarmGoldAccent,
                        unselectedIconColor = IslamicGreenLight,
                        unselectedTextColor = IslamicGreenLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Ramadan") },
                    label = { Text("Ramadan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicGreenDark,
                        selectedTextColor = IslamicGreenDark,
                        indicatorColor = WarmGoldAccent,
                        unselectedIconColor = IslamicGreenLight,
                        unselectedTextColor = IslamicGreenLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicGreenDark,
                        selectedTextColor = IslamicGreenDark,
                        indicatorColor = WarmGoldAccent,
                        unselectedIconColor = IslamicGreenLight,
                        unselectedTextColor = IslamicGreenLight
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> PrayersScreen(viewModel)
                1 -> QiblaScreen(viewModel)
                2 -> WisdomScreen(viewModel)
                3 -> RamadanScreen(viewModel)
                4 -> BookmarksSettingsScreen(viewModel)
            }
        }
    }
}

@Composable
fun PrayersScreen(viewModel: MainViewModel) {
    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
    val todayTrack by viewModel.todayPrayerTrack.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val isCustom by viewModel.isCustomCoordinates.collectAsStateWithLifecycle()

    val isUsingApi by viewModel.isUsingApiPrayerTimes.collectAsStateWithLifecycle()
    val isApiLoading by viewModel.isApiLoading.collectAsStateWithLifecycle()
    val apiError by viewModel.apiError.collectAsStateWithLifecycle()
    val apiHijriDate by viewModel.apiHijriDate.collectAsStateWithLifecycle()

    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var showPresetMenu by remember { mutableStateOf(false) }
    var showCoordDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Header Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_islamic_banner),
                contentDescription = "Mosque Header Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    )
            )
            // Title text overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Islamic Companion",
                    color = IslamicGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Info & Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Location",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location",
                            tint = IslamicGreenMedium,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCustom) "Custom Coordinates" else selectedCity.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isCustom) {
                        Text(
                            text = "Lat: ${viewModel.customLatitude.value} | Lon: ${viewModel.customLongitude.value}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { showPresetMenu = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = IslamicGold.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.LocationCity, contentDescription = "Cities preset", tint = IslamicGreenLight)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showCoordDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = IslamicGreenLight.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.AddLocation, contentDescription = "Custom coordinate", tint = IslamicGold)
                    }
                }
            }
        }

        // Cities Preset Dropdown Menu Dialog
        if (showPresetMenu) {
            AlertDialog(
                onDismissRequest = { showPresetMenu = false },
                title = { Text("Select Preset City", fontFamily = FontFamily.Serif, color = IslamicGreenMedium) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        viewModel.cityPresets.forEach { city ->
                            TextButton(
                                onClick = {
                                    viewModel.selectPresetCity(city)
                                    showPresetMenu = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(city.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                    Text("Lat: ${city.latitude}", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPresetMenu = false }) {
                        Text("Cancel", color = IslamicGreenMedium)
                    }
                }
            )
        }

        // Custom Coordinates Input Dialog
        if (showCoordDialog) {
            AlertDialog(
                onDismissRequest = { showCoordDialog = false },
                title = { Text("Enter Custom Coordinates", fontFamily = FontFamily.Serif, color = IslamicGreenMedium) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = latInput,
                            onValueChange = { latInput = it },
                            label = { Text("Latitude (e.g., 21.4225)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = lonInput,
                            onValueChange = { lonInput = it },
                            label = { Text("Longitude (e.g., 39.8262)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (latInput.toDoubleOrNull() != null && lonInput.toDoubleOrNull() != null) {
                                viewModel.enableCustomCoordinates(latInput, lonInput)
                                showCoordDialog = false
                            } else {
                                // Invalid input
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenMedium)
                    ) {
                        Text("Apply", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCoordDialog = false }) {
                        Text("Cancel", color = IslamicGreenMedium)
                    }
                }
            )
        }

        // Prayer Source Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, IslamicGold.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Prayer Times Source",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenDark
                        )
                        Text(
                            text = if (isUsingApi) "Online Aladhan Public API" else "Offline Mathematical Calculator",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    
                    Switch(
                        checked = isUsingApi,
                        onCheckedChange = { viewModel.togglePrayerTimeSource(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IslamicGreenMedium,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceLight
                        ),
                        modifier = Modifier.testTag("api_toggle")
                    )
                }

                if (isUsingApi) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isApiLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = IslamicGreenMedium,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fetching times from api.aladhan.com...",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    } else if (apiError != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = apiError ?: "Network error",
                                fontSize = 12.sp,
                                color = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { 
                                    val lat = viewModel.customLatitude.value.toDoubleOrNull() ?: 21.4225
                                    val lon = viewModel.customLongitude.value.toDoubleOrNull() ?: 39.8262
                                    viewModel.fetchApiPrayerTimes(lat, lon) 
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = IslamicGreenMedium)
                            ) {
                                Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Connected",
                                tint = IslamicGreenMedium,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Times synced successfully",
                                fontSize = 12.sp,
                                color = IslamicGreenMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (apiHijriDate != null) {
                                Text(
                                    text = apiHijriDate ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGold,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prayer Times List Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Prayer Times",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Tracker",
                fontSize = 12.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Render each prayer time
        if (prayerTimes != null) {
            val t = prayerTimes!!
            val prayersList = listOf(
                PrayerRowData("Fajr", t.fajr, todayTrack.fajrCompleted, Icons.Outlined.WbTwilight),
                PrayerRowData("Sunrise", t.sunrise, false, Icons.Outlined.LightMode, isSunrise = true),
                PrayerRowData("Dhuhr", t.dhuhr, todayTrack.dhuhrCompleted, Icons.Outlined.WbSunny),
                PrayerRowData("Asr", t.asr, todayTrack.asrCompleted, Icons.Outlined.FilterHdr),
                PrayerRowData("Maghrib", t.maghrib, todayTrack.maghribCompleted, Icons.Outlined.WbCloudy),
                PrayerRowData("Isha", t.isha, todayTrack.ishaCompleted, Icons.Outlined.NightsStay)
            )

            // Determine current/next upcoming prayer to highlight it
            val nextPrayerIndex = getNextPrayerIndex(prayersList)

            prayersList.forEachIndexed { index, prayer ->
                val isCurrent = index == nextPrayerIndex
                PrayerItemRow(
                    prayer = prayer,
                    isCurrent = isCurrent,
                    onCheckedChange = {
                        if (!prayer.isSunrise) {
                            viewModel.togglePrayerTrack(prayer.name)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IslamicGreenMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

data class PrayerRowData(
    val name: String,
    val time: String,
    val completed: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isSunrise: Boolean = false
)

@Composable
fun PrayerItemRow(
    prayer: PrayerRowData,
    isCurrent: Boolean,
    onCheckedChange: () -> Unit
) {
    val borderColor = IslamicGold
    val backgroundBrush = if (isCurrent) {
        Brush.horizontalGradient(listOf(IslamicGreenMedium, IslamicGreenMedium))
    } else {
        Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }
    val contentColor = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(brush = backgroundBrush)
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = prayer.icon,
                    contentDescription = prayer.name,
                    tint = if (isCurrent) WarmGoldAccent else IslamicGreenLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = prayer.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    if (isCurrent) {
                        Text(
                            text = "Active Prayer",
                            fontSize = 11.sp,
                            color = WarmGoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prayer.time,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) Color.White else IslamicGreenMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (!prayer.isSunrise) {
                    Checkbox(
                        checked = prayer.completed,
                        onCheckedChange = { onCheckedChange() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (isCurrent) WarmGoldAccent else IslamicGreenMedium,
                            uncheckedColor = if (isCurrent) Color.White.copy(alpha = 0.6f) else TextMuted,
                            checkmarkColor = if (isCurrent) IslamicGreenMedium else Color.White
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(24.dp)) // Equal spacing placeholder
                }
            }
        }
    }
}

private fun getNextPrayerIndex(prayers: List<PrayerRowData>): Int {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val nowStr = sdf.format(Date())
    val nowVal = timeStringToMinutes(nowStr)

    var highlightedIndex = -1
    for (i in prayers.indices) {
        val prayerTimeVal = timeStringToMinutes(prayers[i].time)
        if (nowVal <= prayerTimeVal) {
            highlightedIndex = i
            break
        }
    }
    // If all times passed today, Fajr of tomorrow is upcoming
    return if (highlightedIndex == -1) 0 else highlightedIndex
}

private fun timeStringToMinutes(timeStr: String): Int {
    return try {
        val parts = timeStr.split(":")
        parts[0].toInt() * 60 + parts[1].toInt()
    } catch (e: Exception) {
        0
    }
}

@Composable
fun QiblaScreen(viewModel: MainViewModel) {
    val qiblaBearing by viewModel.qiblaBearing.collectAsStateWithLifecycle()
    val distanceToMecca by viewModel.distanceToMecca.collectAsStateWithLifecycle()
    val deviceAzimuth by viewModel.deviceAzimuth.collectAsStateWithLifecycle()

    // Calculate dynamic needle orientation pointing directly towards Mecca:
    // needle rotation = qiblaBearing - deviceAzimuth
    val needleRotation = (qiblaBearing - deviceAzimuth + 360) % 360

    // Check alignment within +/- 4 degrees
    val isAligned = abs(qiblaBearing - deviceAzimuth) < 4.0 || abs(qiblaBearing - deviceAzimuth) > 356.0

    val alignedAnimGlow = rememberInfiniteTransition(label = "alignedGlow")
    val glowScale by alignedAnimGlow.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Qibla Direction",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = IslamicGreenMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Keep your phone horizontal to calibrate correctly",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Aligned/Compass Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isAligned) IslamicGreenMedium else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isAligned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Aligned",
                        tint = IslamicGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ALIGNED WITH MAKKAH",
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Point your phone directly towards the Kaaba",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Not Aligned",
                        tint = IslamicGreenLight,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ROTATE TO ALIGN",
                        color = IslamicGreenMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Slowly turn your phone until the needle matches the alignment arc",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Beautiful Interactive Compass Canvas
        Box(
            modifier = Modifier
                .size(260.dp)
                .drawBehind {
                    if (isAligned) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(IslamicGold.copy(alpha = 0.4f), Color.Transparent),
                                center = center,
                                radius = size.minDimension / 2 * glowScale
                            ),
                            radius = size.minDimension / 2 * glowScale
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Draw compass body
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 12.dp.toPx()

                // Outer decorative gold ring
                drawCircle(
                    color = IslamicGold,
                    radius = radius + 6.dp.toPx(),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Outer compass ring
                drawCircle(
                    color = IslamicGreenMedium,
                    radius = radius,
                    style = Stroke(width = 6.dp.toPx())
                )

                // Grid markings every 30 degrees (rotating with compass)
                rotate(-deviceAzimuth, pivot = centerOffset) {
                    for (angle in 0 until 360 step 30) {
                        val angleRad = Math.toRadians(angle.toDouble())
                        val startPoint = Offset(
                            (centerOffset.x + (radius - 12.dp.toPx()) * sin(angleRad)).toFloat(),
                            (centerOffset.y - (radius - 12.dp.toPx()) * cos(angleRad)).toFloat()
                        )
                        val endPoint = Offset(
                            (centerOffset.x + radius * sin(angleRad)).toFloat(),
                            (centerOffset.y - radius * cos(angleRad)).toFloat()
                        )
                        drawLine(
                            color = IslamicGold.copy(alpha = 0.7f),
                            start = startPoint,
                            end = endPoint,
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Cardinal Directions text indicators
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#133F2B")
                        textSize = 14.sp.toPx()
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    drawContext.canvas.nativeCanvas.drawText("N", centerOffset.x, centerOffset.y - radius + 22.dp.toPx(), textPaint)
                    drawContext.canvas.nativeCanvas.drawText("S", centerOffset.x, centerOffset.y + radius - 10.dp.toPx(), textPaint)
                    drawContext.canvas.nativeCanvas.drawText("E", centerOffset.x + radius - 16.dp.toPx(), centerOffset.y + 5.dp.toPx(), textPaint)
                    drawContext.canvas.nativeCanvas.drawText("W", centerOffset.x - radius + 16.dp.toPx(), centerOffset.y + 5.dp.toPx(), textPaint)
                }

                // Draw central Kaaba icon representation
                drawCircle(
                    color = IslamicGreenDark,
                    radius = 20.dp.toPx()
                )
                drawCircle(
                    color = IslamicGold,
                    radius = 20.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Central rotating needle (pointing to Mecca)
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Qibla Pointer",
                tint = if (isAligned) IslamicGold else IslamicGreenLight,
                modifier = Modifier
                    .size(80.dp)
                    .rotate(needleRotation.toFloat())
            )

            // Miniature Kaaba symbol in center
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black)
                    .border(2.dp, IslamicGold, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Gold decorative ribbon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(IslamicGold)
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Spherical trigonometry data card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Mecca Direction", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f° N", qiblaBearing),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenMedium
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Current Heading", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f° Azimuth", deviceAzimuth),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = IslamicGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Distance to Holy Kaaba", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f km", distanceToMecca),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenMedium
                    )
                }
            }
        }
    }
}

@Composable
fun VerseScreen(viewModel: MainViewModel) {
    val dailyVerse by viewModel.currentDailyVerse.collectAsStateWithLifecycle()
    val isLoading by viewModel.isVerseLoading.collectAsStateWithLifecycle()
    val savedVerses by viewModel.savedVerses.collectAsStateWithLifecycle()
    val isSaved by viewModel.isCurrentVerseSaved.collectAsStateWithLifecycle()
    val selectedTopic by viewModel.verseTopic.collectAsStateWithLifecycle()
    val apiError by viewModel.verseError.collectAsStateWithLifecycle()
    val deepExplanation by viewModel.deepVerseExplanation.collectAsStateWithLifecycle()
    val isGeneratingExp by viewModel.isGeneratingExplanation.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val isApiKeyEmpty = BuildConfig.GEMINI_API_KEY.isEmpty() || BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY"

    val topics = listOf(
        "Patience & Faith",
        "Hope & Relief",
        "Gratitude & Joy",
        "Wisdom & Peace",
        "Mercy & Forgiveness"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Daily Quranic Wisdom",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = IslamicGreenMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Daily Quran verses accompanied by Gemini AI explanations and reflections",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Topic selector horizontal chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topics.forEach { topic ->
                val isSelected = topic == selectedTopic
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.changeVerseTopic(topic) },
                    label = { Text(topic) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGreenMedium,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // API Status Warning Banner
        if (isApiKeyEmpty) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGold.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, IslamicGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = IslamicGreenDark, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pro-tip: Add your GEMINI_API_KEY in the Secrets panel in AI Studio to unlock unlimited custom AI-generated verses and live Tafsir explanations!",
                        fontSize = 11.sp,
                        color = IslamicGreenDark,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (apiError != null && !isApiKeyEmpty) {
            Text(
                text = apiError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // Verse Rendering Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = IslamicGreenMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Gemini is reflecting on verses...", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header with Surah name and Revelation badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${dailyVerse.surahName} (${dailyVerse.surahNumber}:${dailyVerse.verseNumber})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenMedium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IslamicGoldLight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = dailyVerse.revelationType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IslamicGreenDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Theme: ${dailyVerse.theme}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Quran Verse", "${dailyVerse.arabicText}\n\n\"${dailyVerse.translation}\"\n(${dailyVerse.surahName} ${dailyVerse.surahNumber}:${dailyVerse.verseNumber})")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied verse to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = IslamicGreenLight)
                            }

                            IconButton(
                                onClick = {
                                    viewModel.toggleBookmarkCurrentVerse()
                                    val msg = if (isSaved) "Bookmark removed" else "Verse bookmarked!"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.Bookmark,
                                    contentDescription = "Bookmark",
                                    tint = if (isSaved) IslamicGreenMedium else IslamicGreenLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Arabic Script (large styled text)
                    Text(
                        text = dailyVerse.arabicText,
                        fontSize = 24.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Medium,
                        color = IslamicGreenMedium,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 38.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // English Translation
                    Text(
                        text = "Translation",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${dailyVerse.translation}\"",
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Gemini AI Explanation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IslamicGreenDark.copy(alpha = 0.05f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Explanation",
                                    tint = IslamicGreenMedium,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Gemini AI Explanation & Reflection",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreenDark
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dailyVerse.explanation,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Optional Deep Tafsir section
                    if (deepExplanation != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(IslamicGoldLight.copy(alpha = 0.6f))
                                .border(1.dp, IslamicGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "Deep Tafsir",
                                        tint = IslamicGreenDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "In-Depth Gemini Spiritual Tafsir",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IslamicGreenDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = deepExplanation!!,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // In-depth Tafsir button
        OutlinedButton(
            onClick = { viewModel.generateDeepExplanation() },
            enabled = !isGeneratingExp,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicGreenDark),
            border = BorderStroke(1.dp, IslamicGreenMedium),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isGeneratingExp) {
                CircularProgressIndicator(color = IslamicGreenMedium, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gemini is analyzing Tafsir...", fontSize = 13.sp)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IslamicGreenMedium, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate In-Depth Gemini Tafsir", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action generate button
        Button(
            onClick = { viewModel.fetchDailyVerse() },
            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenMedium),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Request Another Verse", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ArticlesSubScreen(viewModel: MainViewModel) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedArticleCategory.collectAsStateWithLifecycle()
    
    var activeArticleForDialog by remember { mutableStateOf<IslamicArticle?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
    ) {
        // Category filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ArticleCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectArticleCategory(category) },
                    label = { Text(category.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGreenMedium,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Articles List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(articles) { article ->
                val categoryColor = when (article.category) {
                    ArticleCategory.FIQH -> IslamicGreenDark
                    ArticleCategory.AQEEDAH -> IslamicGold
                    ArticleCategory.SEERAH -> IslamicGreenMedium
                    else -> IslamicGreenDark
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeArticleForDialog = article },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(categoryColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = article.category.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Read time",
                                    tint = TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = article.readTime,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = article.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenDark,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = article.summary,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = article.reference,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = TextMuted,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Read Article →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Article Reader Dialog
    if (activeArticleForDialog != null) {
        val article = activeArticleForDialog!!
        AlertDialog(
            onDismissRequest = { activeArticleForDialog = null },
            confirmButton = {
                TextButton(onClick = { activeArticleForDialog = null }) {
                    Text("Close", color = IslamicGreenMedium, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Column {
                    Text(
                        text = article.category.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenDark,
                        lineHeight = 22.sp
                    )
                }
            },
            text = {
                val dialogScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(dialogScrollState)
                ) {
                    Text(
                        text = article.content,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key Takeaway Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarmGoldAccent.copy(alpha = 0.7f))
                            .border(1.dp, IslamicGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = "Takeaway", tint = IslamicGreenDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Key Practical Takeaway",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreenDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = article.keyTakeaway,
                                fontSize = 12.5.sp,
                                color = IslamicGreenDark,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Source: ${article.reference}",
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = TextMuted
                    )
                }
            }
        )
    }
}

@Composable
fun BookmarksSettingsScreen(viewModel: MainViewModel) {
    val savedVerses by viewModel.savedVerses.collectAsStateWithLifecycle()
    val notifsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    var showClearConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Preferences & Saved",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = IslamicGreenMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Settings Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Settings",
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenMedium,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Adhan Push Notifications", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Enable local push alerts for all 5 daily prayers", fontSize = 12.sp, color = TextMuted)
                        }
                        Switch(
                            checked = notifsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IslamicGreenMedium,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = IslamicGold.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Saved Bookmarks section
        item {
            Text(
                text = "My Bookmarked Verses (${savedVerses.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (savedVerses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextMuted)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No saved bookmarks yet.", fontWeight = FontWeight.Medium, color = TextMuted)
                            Text("Click the bookmark icon on any verse to save it here.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        } else {
            items(savedVerses) { saved ->
                SavedVerseItem(saved = saved, onDelete = { viewModel.deleteSavedVerse(saved) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SavedVerseItem(saved: SavedVerse, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${saved.surahName} (${saved.surahNumber}:${saved.verseNumber})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = saved.arabicText,
                fontSize = 18.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                color = IslamicGreenMedium,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${saved.translation}\"",
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(IslamicGold.copy(alpha = 0.08f))
                    .padding(8.dp)
            ) {
                Text(
                    text = saved.reflection,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun WisdomScreen(viewModel: MainViewModel) {
    var subTab by remember { mutableStateOf(0) } // 0 = Quran, 1 = Hadith, 2 = Tasbih
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Wisdom & Tasbih",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = IslamicGreenDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        
        // Custom segmented tab control using modern, cozy pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(IslamicGoldLight.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == 0) IslamicGreenMedium else Color.Transparent)
                    .clickable { subTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Quran",
                    color = if (subTab == 0) Color.White else IslamicGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == 1) IslamicGreenMedium else Color.Transparent)
                    .clickable { subTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hadith",
                    color = if (subTab == 1) Color.White else IslamicGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == 2) IslamicGreenMedium else Color.Transparent)
                    .clickable { subTab = 2 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Articles",
                    color = if (subTab == 2) Color.White else IslamicGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == 3) IslamicGreenMedium else Color.Transparent)
                    .clickable { subTab = 3 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tasbih",
                    color = if (subTab == 3) Color.White else IslamicGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (subTab) {
            0 -> VerseScreen(viewModel)
            1 -> HadithSubScreen(viewModel)
            2 -> ArticlesSubScreen(viewModel)
            3 -> TasbihSubScreen(viewModel)
        }
    }
}

@Composable
fun HadithSubScreen(viewModel: MainViewModel) {
    val currentHadith by viewModel.currentHadith.collectAsStateWithLifecycle()
    val isLoading by viewModel.isHadithLoading.collectAsStateWithLifecycle()
    val apiError by viewModel.hadithError.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val isApiKeyEmpty = BuildConfig.GEMINI_API_KEY.isEmpty() || BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY"

    val topics = listOf(
        "Sincerity & Intent",
        "Charity & Kindness",
        "Knowledge & Wisdom",
        "Patience & Fasting",
        "Good Character"
    )
    
    var selectedHadithTopic by remember { mutableStateOf("Sincerity & Intent") }
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Topic Selector horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topics.forEach { topic ->
                val isSelected = topic == selectedHadithTopic
                FilterChip(
                    selected = isSelected,
                    onClick = { 
                        selectedHadithTopic = topic
                        viewModel.fetchDailyHadith(topic) 
                    },
                    label = { Text(topic) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGreenMedium,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isApiKeyEmpty) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGold.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, IslamicGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = IslamicGreenDark, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pro-tip: Add your GEMINI_API_KEY in the Secrets panel in AI Studio to unlock authentic Hadiths on custom topics generated live!",
                        fontSize = 11.sp,
                        color = IslamicGreenDark,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (apiError != null && !isApiKeyEmpty) {
            Text(
                text = apiError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // Hadith Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = IslamicGreenMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Gemini is reflecting on Hadith...", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hadith of the Day",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenMedium
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Hadith",
                            tint = IslamicGreenLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Arabic Text
                    Text(
                        text = currentHadith.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = IslamicGreenMedium,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Translation
                    Text(
                        text = "Translation",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentHadith.translation,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Source
                    Text(
                        text = "Source",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentHadith.source,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = IslamicGreenDark,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Reflection
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Reflection",
                            tint = IslamicGreenMedium,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Spiritual Guidance",
                            fontSize = 12.sp,
                            color = IslamicGreenMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentHadith.reflection,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RamadanScreen(viewModel: MainViewModel) {
    val ramadanDays by viewModel.ramadanDays.collectAsStateWithLifecycle()
    val islamicEvents by viewModel.islamicEvents.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    
    var subTab by remember { mutableStateOf(0) } // 0 = Calendar, 1 = Significant Events
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ramadan & Events",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = IslamicGreenDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        
        // Custom segmented tab control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(IslamicGoldLight.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == 0) IslamicGreenMedium else Color.Transparent)
                    .clickable { subTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ramadan Calendar",
                    color = if (subTab == 0) Color.White else IslamicGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == 1) IslamicGreenMedium else Color.Transparent)
                    .clickable { subTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Islamic Events",
                    color = if (subTab == 1) Color.White else IslamicGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (subTab == 0) {
            RamadanCalendarSubScreen(viewModel, ramadanDays, selectedCity.name)
        } else {
            IslamicEventsSubScreen(islamicEvents)
        }
    }
}

@Composable
fun RamadanCalendarSubScreen(viewModel: MainViewModel, ramadanDays: List<RamadanDay>, cityName: String) {
    val context = LocalContext.current
    val ramadanNotifsEnabled by viewModel.ramadanNotificationsEnabled.collectAsStateWithLifecycle()
    val today = Calendar.getInstance()
    val todayYear = today.get(Calendar.YEAR)
    
    val targetYear = if (todayYear < 2026 || (todayYear == 2026 && today.get(Calendar.MONTH) <= Calendar.MARCH)) 2026 else 2027
    val startCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, targetYear)
        set(Calendar.MONTH, Calendar.FEBRUARY)
        set(Calendar.DAY_OF_MONTH, if (targetYear == 2026) 18 else 7)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val todayZero = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffMs = startCal.timeInMillis - todayZero.timeInMillis
    val daysRemaining = diffMs / (1000 * 60 * 60 * 24)

    val currentActiveDay = ramadanDays.find { it.isToday }

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner Area
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            border = BorderStroke(1.dp, IslamicGold)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentActiveDay != null) {
                    Text(
                        text = "Ramadan Mubarak!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Today is Day ${currentActiveDay.dayOfRamadan} of Ramadan ($cityName)",
                        fontSize = 14.sp,
                        color = TextLightPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Imsak (Suhoor stop)", fontSize = 11.sp, color = TextMuted)
                            Text(currentActiveDay.imsak, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IslamicGreenMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Iftar (Maghrib)", fontSize = 11.sp, color = TextMuted)
                            Text(currentActiveDay.iftar, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IslamicGreenMedium)
                        }
                    }
                } else {
                    Text(
                        text = if (daysRemaining > 0) "Upcoming Ramadan $targetYear" else "Ramadan Calendar $targetYear",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (daysRemaining > 0) {
                            "Begins in $daysRemaining days (estimated Feb ${if (targetYear == 2026) "18" else "7"}, $targetYear)"
                        } else {
                            "Fast times calculated dynamically for $cityName"
                        },
                        fontSize = 14.sp,
                        color = TextLightPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Push Notifications for Imsak and Iftar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            colors = CardDefaults.cardColors(containerColor = WarmGoldAccent.copy(alpha = 0.55f)),
            border = BorderStroke(1.dp, IslamicGold.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Ramadan Push Alerts",
                            tint = IslamicGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Imsak & Iftar Push Alerts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = IslamicGreenDark
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Receive notifications at Imsak (10m before Fajr) and Iftar (at Maghrib) for $cityName.",
                        fontSize = 11.5.sp,
                        color = TextLightPrimary,
                        lineHeight = 16.sp
                    )
                }

                Switch(
                    checked = ramadanNotifsEnabled,
                    onCheckedChange = { isChecked ->
                        viewModel.toggleRamadanNotifications(isChecked)
                        val msg = if (isChecked) "Imsak and Iftar push alerts scheduled!" else "Ramadan notifications disabled"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IslamicGreenMedium,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = IslamicGold.copy(alpha = 0.4f)
                    )
                )
            }
        }

        // Calendar List Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IslamicGreenLight.copy(alpha = 0.15f))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Day", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IslamicGreenDark)
            Text("Date", modifier = Modifier.weight(2.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IslamicGreenDark)
            Text("Imsak", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IslamicGreenDark, textAlign = TextAlign.Center)
            Text("Iftar", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IslamicGreenDark, textAlign = TextAlign.Center)
        }

        // Calendar Lazy List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(ramadanDays) { rDay ->
                val bg = if (rDay.isToday) WarmGoldAccent else if (rDay.dayOfRamadan % 2 == 0) SurfaceLight else Color.Transparent
                val border = if (rDay.isToday) BorderStroke(1.5.dp, IslamicGreenMedium) else null
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    border = border
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Day ${rDay.dayOfRamadan}",
                            modifier = Modifier.weight(1.5f),
                            fontWeight = if (rDay.isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (rDay.isToday) IslamicGreenDark else TextLightPrimary
                        )
                        Text(
                            text = rDay.dateString,
                            modifier = Modifier.weight(2.5f),
                            fontSize = 13.sp,
                            color = TextLightPrimary
                        )
                        Text(
                            text = rDay.imsak,
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = IslamicGreenDark
                        )
                        Text(
                            text = rDay.iftar,
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = IslamicGreenMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicEventsSubScreen(islamicEvents: List<IslamicEvent>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGreenMedium.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, IslamicGreenMedium.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alerts",
                        tint = IslamicGreenMedium,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Automatic Reminders are enabled! You will receive push notifications 3 days and 1 day in advance of Eid al-Fitr, Eid al-Adha, and Laylat al-Qadr.",
                        fontSize = 11.5.sp,
                        color = IslamicGreenDark,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        items(islamicEvents) { event ->
            val days = event.daysRemaining
            val isUpcoming = days >= 0
            
            val (badgeText, badgeBg, badgeTextClr) = when {
                days == 0L -> Triple("Today", IslamicGreenMedium, Color.White)
                days == 1L -> Triple("Tomorrow", IslamicGold, IslamicGreenDark)
                days in 2L..7L -> Triple("In $days days", WarmGoldAccent, IslamicGreenDark)
                days > 7L -> Triple("In $days days", SurfaceLight, TextLightPrimary)
                else -> Triple("Passed", Color.LightGray.copy(alpha = 0.3f), TextMuted)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, if (days == 0L || days == 1L) IslamicGreenMedium else Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenDark
                        )
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextClr
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.dateString,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IslamicGreenMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        fontSize = 13.sp,
                        color = TextLightPrimary,
                        lineHeight = 18.sp
                    )

                    if (isUpcoming && (event.name == "Eid al-Fitr" || event.name == "Eid al-Adha" || event.name == "Laylat al-Qadr")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder",
                                tint = IslamicGreenLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Alarms scheduled for 3d and 1d in advance",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TasbihSubScreen(viewModel: MainViewModel) {
    val tasbihList by viewModel.tasbihList.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.dhikrReminderEnabled.collectAsStateWithLifecycle()
    val reminderPhrase by viewModel.dhikrReminderPhrase.collectAsStateWithLifecycle()
    val reminderInterval by viewModel.dhikrReminderIntervalHours.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSetCountDialog by remember { mutableStateOf(false) }

    // Reminder customization local states
    var selectedIntervalHours by remember(reminderInterval) { mutableStateOf(reminderInterval) }
    var selectedPhrase by remember(reminderPhrase) { mutableStateOf(reminderPhrase) }

    val activeItem = tasbihList.find { it.id == selectedId } ?: tasbihList.firstOrNull()

    // Keep track of last selected ID if it becomes null
    LaunchedEffect(tasbihList) {
        if (selectedId == null && tasbihList.isNotEmpty()) {
            selectedId = tasbihList.first().id
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal list of Dhikrs header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Recitation:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreenDark,
                modifier = Modifier.padding(end = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.textButtonColors(contentColor = IslamicGreenMedium)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Custom", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tasbihList.forEach { item ->
                val isSelected = item.id == activeItem?.id
                val progress = if (item.target > 0) (item.count.toFloat() / item.target).coerceIn(0f, 1f) else 0f
                
                Card(
                    modifier = Modifier
                        .width(135.dp)
                        .clickable { selectedId = item.id },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) WarmGoldAccent else SurfaceLight
                    ),
                    border = BorderStroke(
                        1.dp, 
                        if (isSelected) IslamicGreenMedium else Color.Transparent
                    )
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreenDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!item.isDefault) {
                                    IconButton(
                                        onClick = { 
                                            viewModel.deleteCustomTasbih(item)
                                            if (selectedId == item.id) {
                                                selectedId = null
                                            }
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${item.count}/${item.target}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = IslamicGreenMedium,
                                trackColor = IslamicGold.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeItem != null) {
            val progress = (activeItem.count.toFloat() / activeItem.target).coerceIn(0f, 1f)
            val isTargetReached = activeItem.count >= activeItem.target

            // Main Tasbih Tap Circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTargetReached) {
                            IslamicGoldLight.copy(alpha = 0.8f)
                        } else {
                            SurfaceLight
                        }
                    )
                    .border(
                        BorderStroke(1.dp, IslamicGold.copy(alpha = 0.5f)),
                        CircleShape
                    )
                    .clickable {
                        val newCount = activeItem.count + 1
                        viewModel.incrementTasbih(activeItem)
                        
                        if (newCount == activeItem.target) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(context, "Mubarak! Reached target of ${activeItem.target} for ${activeItem.name}!", Toast.LENGTH_SHORT).show()
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Circular Progress Ring around the tap area
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(232.dp),
                    color = IslamicGreenMedium,
                    trackColor = Color.Transparent,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )

                // Inner content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = activeItem.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenDark,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${activeItem.count}",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenMedium,
                        fontFamily = FontFamily.Serif
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Target: ${activeItem.target}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    if (isTargetReached) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Completed",
                                tint = IslamicGreenMedium,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Goal Reached!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenMedium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap circle to count",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement Button (-1)
                FilledTonalButton(
                    onClick = {
                        if (activeItem.count > 0) {
                            viewModel.decrementTasbih(activeItem)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    enabled = activeItem.count > 0,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SurfaceLight,
                        contentColor = IslamicGreenMedium
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Subtract",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("-1", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Set Count & Target Button
                OutlinedButton(
                    onClick = { showSetCountDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = IslamicGreenDark
                    ),
                    border = BorderStroke(1.dp, IslamicGreenMedium),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Set Count",
                        modifier = Modifier.size(16.dp),
                        tint = IslamicGreenMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set Count", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Reset Button
                Button(
                    onClick = {
                        viewModel.resetTasbih(activeItem.id)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "${activeItem.name} count reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IslamicGreenMedium,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Customizable Dhikr Reminders Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                border = BorderStroke(1.dp, IslamicGreenLight.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Dhikr Reminders",
                                tint = IslamicGreenMedium,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Customizable Dhikr Reminders",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = IslamicGreenDark
                                )
                                Text(
                                    text = "Timed notifications to keep your tongue moist with Dhikr",
                                    fontSize = 10.5.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.configureDhikrReminder(
                                    enabled = enabled,
                                    phrase = selectedPhrase,
                                    intervalHours = selectedIntervalHours
                                )
                                val msg = if (enabled) "Dhikr reminder scheduled!" else "Dhikr reminder cancelled"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IslamicGreenMedium
                            )
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Dhikr Phrase Selection
                        Text(
                            text = "Dhikr Supplication:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = IslamicGreenDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val standardPhrases = listOf(
                            "SubhanAllah",
                            "Alhamdulillah",
                            "Allahu Akbar",
                            "Astaghfirullah",
                            "Salawat on Prophet ﷺ"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            standardPhrases.forEach { phrase ->
                                val isSelected = selectedPhrase == phrase
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPhrase = phrase },
                                    label = { Text(phrase, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IslamicGreenMedium,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reminder Interval / Time Selection
                        Text(
                            text = "Reminder Interval / Frequency:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = IslamicGreenDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val intervals = listOf(
                            1 to "Every 1 Hour",
                            2 to "Every 2 Hours",
                            3 to "Every 3 Hours",
                            4 to "Every 4 Hours"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            intervals.forEach { (hours, label) ->
                                val isSelected = selectedIntervalHours == hours
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedIntervalHours = hours },
                                    label = { Text(label, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IslamicGold,
                                        selectedLabelColor = IslamicGreenDark
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.configureDhikrReminder(
                                    enabled = true,
                                    phrase = selectedPhrase,
                                    intervalHours = selectedIntervalHours
                                )
                                Toast.makeText(
                                    context,
                                    "Saved! Will remind: \"$selectedPhrase\" every $selectedIntervalHours hr(s)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenMedium),
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Update Dhikr Schedule", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle Guidance/Reflection Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, IslamicGold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Sunnah",
                        tint = IslamicGreenMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Reciting Dhikr keeps the heart at peace, brings barakah (blessing), and earns rewards in the Sunnah. Tap the big circle to track your progress.",
                        fontSize = 11.5.sp,
                        color = TextLightPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Set Count & Target Dialog
        if (showSetCountDialog && activeItem != null) {
            var countInput by remember { mutableStateOf(activeItem.count.toString()) }
            var targetInput by remember { mutableStateOf(activeItem.target.toString()) }
            var errorMsg by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showSetCountDialog = false },
                title = {
                    Text(
                        text = "Set Count for ${activeItem.name}",
                        color = IslamicGreenDark,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Manually update your current recitation count or set a custom goal target.",
                            fontSize = 12.sp,
                            color = TextLightPrimary
                        )

                        OutlinedTextField(
                            value = countInput,
                            onValueChange = {
                                countInput = it
                                errorMsg = null
                            },
                            label = { Text("Current Count") },
                            placeholder = { Text("0") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IslamicGreenMedium,
                                focusedLabelColor = IslamicGreenMedium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = targetInput,
                            onValueChange = {
                                targetInput = it
                                errorMsg = null
                            },
                            label = { Text("Goal Target") },
                            placeholder = { Text("33") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IslamicGreenMedium,
                                focusedLabelColor = IslamicGreenMedium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMsg != null) {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedCount = countInput.trim().toIntOrNull()
                            val parsedTarget = targetInput.trim().toIntOrNull()

                            if (parsedCount == null || parsedCount < 0) {
                                errorMsg = "Please enter a valid count (0 or greater)."
                            } else if (parsedTarget == null || parsedTarget <= 0) {
                                errorMsg = "Please enter a valid target greater than 0."
                            } else {
                                viewModel.setTasbihCount(activeItem, parsedCount)
                                viewModel.setTasbihTarget(activeItem, parsedTarget)
                                showSetCountDialog = false
                                Toast.makeText(context, "${activeItem.name} count updated to $parsedCount!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IslamicGreenMedium,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Count")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSetCountDialog = false }) {
                        Text("Cancel", color = IslamicGreenLight)
                    }
                }
            )
        }

        // Custom Dhikr Creator Dialog
        if (showAddDialog) {
            var nameInput by remember { mutableStateOf("") }
            var targetInput by remember { mutableStateOf("33") }
            var errorMsg by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = "Create Custom Dhikr",
                        color = IslamicGreenDark,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Add your own custom recitation, supplication, or prayer log and track it securely.",
                            fontSize = 12.sp,
                            color = TextLightPrimary
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { 
                                nameInput = it
                                errorMsg = null 
                            },
                            label = { Text("Dhikr Supplication Name") },
                            placeholder = { Text("e.g. Astaghfirullah al-Azim") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IslamicGreenMedium,
                                focusedLabelColor = IslamicGreenMedium
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = targetInput,
                            onValueChange = { 
                                targetInput = it
                                errorMsg = null
                            },
                            label = { Text("Daily Recitation Target") },
                            placeholder = { Text("e.g. 100") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IslamicGreenMedium,
                                focusedLabelColor = IslamicGreenMedium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMsg != null) {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cleanName = nameInput.trim()
                            val cleanTarget = targetInput.trim().toIntOrNull()
                            
                            if (cleanName.isEmpty()) {
                                errorMsg = "Please enter a supplication name."
                            } else if (cleanTarget == null || cleanTarget <= 0) {
                                errorMsg = "Please enter a valid target count greater than 0."
                            } else {
                                viewModel.addCustomTasbih(cleanName, cleanTarget)
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IslamicGreenMedium,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showAddDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = IslamicGreenLight)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
