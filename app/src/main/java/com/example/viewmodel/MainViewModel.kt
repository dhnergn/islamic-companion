package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.ArticleCategory
import com.example.data.ArticleRepository
import com.example.data.DailyQuranVerse
import com.example.data.IslamicArticle
import com.example.data.PrayerTrack
import com.example.data.QuranRepository
import com.example.data.QuranRepositoryImpl
import com.example.data.SavedVerse
import com.example.data.TasbihItem
import com.example.network.GeminiRequest
import com.example.network.GenerationConfig
import com.example.network.QuranVerseResponse
import com.example.network.HadithResponse
import com.example.network.RetrofitClient
import com.example.notification.NotificationHelper
import com.example.util.PrayerTimesCalculator
import com.example.util.QiblaCalculator
import com.example.util.QuranFallbacks
import com.example.util.HadithRepository
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getDatabase(application)
    private val savedVerseDao = db.savedVerseDao()
    private val prayerTrackDao = db.prayerTrackDao()
    private val tasbihDao = db.tasbihDao()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Preset Cities
    data class CityPreset(val name: String, val latitude: Double, val longitude: Double)
    val cityPresets = listOf(
        CityPreset("Makkah", 21.4225, 39.8262),
        CityPreset("London", 51.5074, -0.1278),
        CityPreset("Cairo", 30.0444, 31.2357),
        CityPreset("New York", 40.7128, -74.0060),
        CityPreset("Jakarta", -6.2088, 106.8456),
        CityPreset("Kuala Lumpur", 3.1390, 101.6869),
        CityPreset("Sydney", -33.8688, 151.2093)
    )

    // UI States
    private val _selectedCity = MutableStateFlow(cityPresets[0])
    val selectedCity: StateFlow<CityPreset> = _selectedCity.asStateFlow()

    private val _customLatitude = MutableStateFlow("21.4225")
    val customLatitude: StateFlow<String> = _customLatitude.asStateFlow()

    private val _customLongitude = MutableStateFlow("39.8262")
    val customLongitude: StateFlow<String> = _customLongitude.asStateFlow()

    private val _isCustomCoordinates = MutableStateFlow(false)
    val isCustomCoordinates: StateFlow<Boolean> = _isCustomCoordinates.asStateFlow()

    // Calculated Prayer Times State
    private val _prayerTimes = MutableStateFlow<PrayerTimesCalculator.PrayerTimes?>(null)
    val prayerTimes: StateFlow<PrayerTimesCalculator.PrayerTimes?> = _prayerTimes.asStateFlow()

    // API Prayer Times State
    private val _isUsingApiPrayerTimes = MutableStateFlow(false)
    val isUsingApiPrayerTimes: StateFlow<Boolean> = _isUsingApiPrayerTimes.asStateFlow()

    private val _isApiLoading = MutableStateFlow(false)
    val isApiLoading: StateFlow<Boolean> = _isApiLoading.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    private val _apiPrayerTimes = MutableStateFlow<PrayerTimesCalculator.PrayerTimes?>(null)
    val apiPrayerTimes: StateFlow<PrayerTimesCalculator.PrayerTimes?> = _apiPrayerTimes.asStateFlow()

    private val _apiHijriDate = MutableStateFlow<String?>(null)
    val apiHijriDate: StateFlow<String?> = _apiHijriDate.asStateFlow()

    // Qibla direction details
    private val _qiblaBearing = MutableStateFlow(0.0)
    val qiblaBearing: StateFlow<Double> = _qiblaBearing.asStateFlow()

    private val _distanceToMecca = MutableStateFlow(0.0)
    val distanceToMecca: StateFlow<Double> = _distanceToMecca.asStateFlow()

    // Real-time azimuth from system compass sensor
    private val _deviceAzimuth = MutableStateFlow(0.0f)
    val deviceAzimuth: StateFlow<Float> = _deviceAzimuth.asStateFlow()

    // Daily Verse State
    private val quranRepository: QuranRepository = QuranRepositoryImpl()

    private val _currentDailyVerse = MutableStateFlow<DailyQuranVerse>(quranRepository.getFallbackVerse("Faith & Patience"))
    val currentDailyVerse: StateFlow<DailyQuranVerse> = _currentDailyVerse.asStateFlow()

    private val _deepVerseExplanation = MutableStateFlow<String?>(null)
    val deepVerseExplanation: StateFlow<String?> = _deepVerseExplanation.asStateFlow()

    private val _isGeneratingExplanation = MutableStateFlow(false)
    val isGeneratingExplanation: StateFlow<Boolean> = _isGeneratingExplanation.asStateFlow()

    private val _currentVerse = MutableStateFlow<QuranVerseResponse>(QuranFallbacks.getRandomFallback())
    val currentVerse: StateFlow<QuranVerseResponse> = _currentVerse.asStateFlow()

    private val _isVerseLoading = MutableStateFlow(false)
    val isVerseLoading: StateFlow<Boolean> = _isVerseLoading.asStateFlow()

    private val _verseError = MutableStateFlow<String?>(null)
    val verseError: StateFlow<String?> = _verseError.asStateFlow()

    private val _verseTopic = MutableStateFlow("Patience & Faith")
    val verseTopic: StateFlow<String> = _verseTopic.asStateFlow()

    private val _isCurrentVerseSaved = MutableStateFlow(false)
    val isCurrentVerseSaved: StateFlow<Boolean> = _isCurrentVerseSaved.asStateFlow()

    // Islamic Articles State (Fiqh, Aqeedah, Seerah)
    private val _selectedArticleCategory = MutableStateFlow(ArticleCategory.ALL)
    val selectedArticleCategory: StateFlow<ArticleCategory> = _selectedArticleCategory.asStateFlow()

    val articles: StateFlow<List<IslamicArticle>> = _selectedArticleCategory
        .map { ArticleRepository.getArticlesByCategory(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticleRepository.articles)

    fun selectArticleCategory(category: ArticleCategory) {
        _selectedArticleCategory.value = category
    }

    // Saved Verses (Room Bookmarks)
    val savedVerses: StateFlow<List<SavedVerse>> = savedVerseDao.getAllSavedVerses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prayer Tracker State for today (Room Track)
    private val todayString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayPrayerTrack: StateFlow<PrayerTrack> = prayerTrackDao.getTrackForDate(todayString)
        .map { it ?: PrayerTrack(date = todayString) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerTrack(date = todayString))

    // Notification toggles
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // Ramadan Calendar State
    private val _ramadanDays = MutableStateFlow<List<RamadanDay>>(emptyList())
    val ramadanDays: StateFlow<List<RamadanDay>> = _ramadanDays.asStateFlow()

    // Islamic Events Calendar State
    private val _islamicEvents = MutableStateFlow<List<IslamicEvent>>(emptyList())
    val islamicEvents: StateFlow<List<IslamicEvent>> = _islamicEvents.asStateFlow()

    // Hadith of the Day State
    private val _currentHadith = MutableStateFlow<HadithResponse>(HadithRepository.getRandomHadith())
    val currentHadith: StateFlow<HadithResponse> = _currentHadith.asStateFlow()

    private val _isHadithLoading = MutableStateFlow(false)
    val isHadithLoading: StateFlow<Boolean> = _isHadithLoading.asStateFlow()

    private val _hadithError = MutableStateFlow<String?>(null)
    val hadithError: StateFlow<String?> = _hadithError.asStateFlow()

    // Tasbih State Flow & Database Synchronization
    val tasbihList: StateFlow<List<TasbihItem>> = tasbihDao.getAllTasbihs()
        .onEach { list ->
            if (list.isEmpty()) {
                withContext(Dispatchers.IO) {
                    val defaults = listOf(
                        TasbihItem("subhanallah", "SubhanAllah", 0, 33, true),
                        TasbihItem("alhamdulillah", "Alhamdulillah", 0, 33, true),
                        TasbihItem("allahu_akbar", "Allahu Akbar", 0, 34, true),
                        TasbihItem("astaghfirullah", "Astaghfirullah", 0, 100, true),
                        TasbihItem("la_ilaha_illallah", "La ilaha illallah", 0, 100, true)
                    )
                    defaults.forEach { tasbihDao.insertOrUpdateTasbih(it) }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun incrementTasbih(item: TasbihItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(count = item.count + 1, timestamp = System.currentTimeMillis())
            tasbihDao.insertOrUpdateTasbih(updated)
        }
    }

    fun decrementTasbih(item: TasbihItem) {
        if (item.count > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                val updated = item.copy(count = item.count - 1, timestamp = System.currentTimeMillis())
                tasbihDao.insertOrUpdateTasbih(updated)
            }
        }
    }

    fun resetTasbih(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tasbihDao.resetTasbih(id)
        }
    }

    fun addCustomTasbih(name: String, target: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis()
            val newItem = TasbihItem(
                id = id,
                name = name,
                count = 0,
                target = target,
                isDefault = false,
                timestamp = System.currentTimeMillis()
            )
            tasbihDao.insertOrUpdateTasbih(newItem)
        }
    }

    fun setTasbihCount(item: TasbihItem, count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(count = count.coerceAtLeast(0), timestamp = System.currentTimeMillis())
            tasbihDao.insertOrUpdateTasbih(updated)
        }
    }

    fun setTasbihTarget(item: TasbihItem, target: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(target = target.coerceAtLeast(1), timestamp = System.currentTimeMillis())
            tasbihDao.insertOrUpdateTasbih(updated)
        }
    }

    // Customizable Dhikr Reminders
    private val _dhikrReminderEnabled = MutableStateFlow(false)
    val dhikrReminderEnabled: StateFlow<Boolean> = _dhikrReminderEnabled.asStateFlow()

    private val _dhikrReminderPhrase = MutableStateFlow("SubhanAllah wa bihamdihi")
    val dhikrReminderPhrase: StateFlow<String> = _dhikrReminderPhrase.asStateFlow()

    private val _dhikrReminderIntervalHours = MutableStateFlow(3)
    val dhikrReminderIntervalHours: StateFlow<Int> = _dhikrReminderIntervalHours.asStateFlow()

    fun configureDhikrReminder(
        enabled: Boolean,
        phrase: String,
        intervalHours: Int,
        specificHour: Int = -1,
        specificMinute: Int = 0
    ) {
        _dhikrReminderEnabled.value = enabled
        _dhikrReminderPhrase.value = phrase
        _dhikrReminderIntervalHours.value = intervalHours

        if (enabled) {
            NotificationHelper.scheduleDhikrAlarm(
                context = getApplication(),
                dhikrPhrase = phrase,
                intervalHours = intervalHours,
                specificHour = specificHour,
                specificMinute = specificMinute
            )
        } else {
            NotificationHelper.cancelDhikrAlarm(getApplication())
        }
    }

    fun deleteCustomTasbih(item: TasbihItem) {
        viewModelScope.launch(Dispatchers.IO) {
            tasbihDao.deleteTasbih(item)
        }
    }

    init {
        // Build initial prayer times & Qibla
        recalculatePrayerTimesAndQibla()
        checkIfCurrentVerseSaved()
        fetchDailyHadith()
        
        // Start sensor listening
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    fun selectPresetCity(city: CityPreset) {
        _selectedCity.value = city
        _isCustomCoordinates.value = false
        _customLatitude.value = city.latitude.toString()
        _customLongitude.value = city.longitude.toString()
        recalculatePrayerTimesAndQibla()
    }

    fun enableCustomCoordinates(lat: String, lon: String) {
        _isCustomCoordinates.value = true
        _customLatitude.value = lat
        _customLongitude.value = lon
        recalculatePrayerTimesAndQibla()
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        if (enabled) {
            triggerNotificationScheduling()
            scheduleAllEventReminders()
        } else {
            // Notifications are toggled off in settings
        }
    }

    fun changeVerseTopic(topic: String) {
        _verseTopic.value = topic
        fetchDailyVerse(topic)
    }

    fun recalculatePrayerTimesAndQibla() {
        viewModelScope.launch {
            val lat = _customLatitude.value.toDoubleOrNull() ?: 21.4225
            val lon = _customLongitude.value.toDoubleOrNull() ?: 39.8262

            val calendar = Calendar.getInstance()
            val tzOffset = TimeZone.getDefault().rawOffset / (1000.0 * 60.0 * 60.0)

            val calculated = withContext(Dispatchers.Default) {
                PrayerTimesCalculator.calculateTimes(
                    calendar = calendar,
                    latitude = lat,
                    longitude = lon,
                    timezoneOffset = tzOffset
                )
            }

            if (!_isUsingApiPrayerTimes.value) {
                _prayerTimes.value = calculated
            }

            // Calculate Qibla Bearing and Distance
            val bearing = QiblaCalculator.calculateQiblaDirection(lat, lon)
            val distance = QiblaCalculator.calculateDistanceToMecca(lat, lon)

            _qiblaBearing.value = bearing
            _distanceToMecca.value = distance

            // Calculate Ramadan Calendar dynamically for these coordinates
            val ramadanCalList = calculateRamadanCalendar(lat, lon, tzOffset)
            _ramadanDays.value = ramadanCalList

            // Calculate Islamic Events dynamically
            val eventsList = calculateIslamicEvents()
            _islamicEvents.value = eventsList

            // Fetch public API times in the background
            fetchApiPrayerTimes(lat, lon)

            if (_notificationsEnabled.value) {
                triggerNotificationScheduling()
                scheduleAllEventReminders()
            }
        }
    }

    fun togglePrayerTimeSource(useApi: Boolean) {
        _isUsingApiPrayerTimes.value = useApi
        val currentApiTimes = _apiPrayerTimes.value
        if (useApi && currentApiTimes != null) {
            _prayerTimes.value = currentApiTimes
            if (_notificationsEnabled.value) {
                triggerNotificationScheduling()
            }
        } else {
            // Recalculate will apply the local calculated times immediately if offline
            recalculatePrayerTimesAndQibla()
        }
    }

    private fun formatApiTime(timeStr: String): String {
        return timeStr.substringBefore(" ").trim()
    }

    fun fetchApiPrayerTimes(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isApiLoading.value = true
            _apiError.value = null
            try {
                val response = withContext(Dispatchers.IO) {
                    com.example.network.AladhanClient.service.getTimings(lat, lon)
                }
                if (response.code == 200) {
                    val timings = response.data.timings
                    val apiTimes = PrayerTimesCalculator.PrayerTimes(
                        fajr = formatApiTime(timings.fajr),
                        sunrise = formatApiTime(timings.sunrise),
                        dhuhr = formatApiTime(timings.dhuhr),
                        asr = formatApiTime(timings.asr),
                        maghrib = formatApiTime(timings.maghrib),
                        isha = formatApiTime(timings.isha)
                    )
                    _apiPrayerTimes.value = apiTimes
                    
                    val hijri = response.data.date.hijri
                    if (hijri != null) {
                        _apiHijriDate.value = "${hijri.day} ${hijri.month?.en} ${hijri.year} AH"
                    }

                    if (_isUsingApiPrayerTimes.value) {
                        _prayerTimes.value = apiTimes
                        if (_notificationsEnabled.value) {
                            triggerNotificationScheduling()
                        }
                    }
                } else {
                    _apiError.value = "API responded with code: ${response.code}"
                }
            } catch (e: Exception) {
                _apiError.value = "Failed to load online times: ${e.localizedMessage ?: "Network error"}"
            } finally {
                _isApiLoading.value = false
            }
        }
    }

    private fun triggerNotificationScheduling() {
        val times = _prayerTimes.value ?: return
        val lat = _customLatitude.value.toDoubleOrNull() ?: 21.4225
        val lon = _customLongitude.value.toDoubleOrNull() ?: 39.8262
        NotificationHelper.schedulePrayerAlarms(getApplication(), times, lat, lon)
    }

    fun scheduleAllEventReminders() {
        val events = _islamicEvents.value
        events.forEach { event ->
            if (event.daysRemaining > 0) {
                NotificationHelper.scheduleEventAlarm(
                    context = getApplication(),
                    eventName = event.name,
                    eventTimeMs = event.calendarDate.timeInMillis,
                    daysInAdvance = 3
                )
                NotificationHelper.scheduleEventAlarm(
                    context = getApplication(),
                    eventName = event.name,
                    eventTimeMs = event.calendarDate.timeInMillis,
                    daysInAdvance = 1
                )
            }
        }
    }

    // Toggle complete a prayer log
    fun togglePrayerTrack(prayerName: String) {
        viewModelScope.launch {
            val currentTrack = todayPrayerTrack.value
            val updated = when (prayerName.lowercase()) {
                "fajr" -> currentTrack.copy(fajrCompleted = !currentTrack.fajrCompleted)
                "dhuhr" -> currentTrack.copy(dhuhrCompleted = !currentTrack.dhuhrCompleted)
                "asr" -> currentTrack.copy(asrCompleted = !currentTrack.asrCompleted)
                "maghrib" -> currentTrack.copy(maghribCompleted = !currentTrack.maghribCompleted)
                "isha" -> currentTrack.copy(ishaCompleted = !currentTrack.ishaCompleted)
                else -> currentTrack
            }
            prayerTrackDao.insertOrUpdateTrack(updated)
        }
    }

    // Ramadan Notifications
    private val _ramadanNotificationsEnabled = MutableStateFlow(false)
    val ramadanNotificationsEnabled: StateFlow<Boolean> = _ramadanNotificationsEnabled.asStateFlow()

    fun toggleRamadanNotifications(enabled: Boolean) {
        _ramadanNotificationsEnabled.value = enabled
        if (enabled) {
            scheduleRamadanNotifications()
        } else {
            NotificationHelper.cancelRamadanAlarms(getApplication())
        }
    }

    fun scheduleRamadanNotifications() {
        val days = _ramadanDays.value
        val targetDay = days.find { it.isToday } ?: days.firstOrNull()
        if (targetDay != null) {
            val lat = _customLatitude.value.toDoubleOrNull() ?: _selectedCity.value.latitude
            val lon = _customLongitude.value.toDoubleOrNull() ?: _selectedCity.value.longitude
            NotificationHelper.scheduleRamadanAlarms(
                context = getApplication(),
                imsakTime = targetDay.imsak,
                iftarTime = targetDay.iftar,
                latitude = lat,
                longitude = lon
            )
        }
    }

    // Fetch daily verse using QuranRepository and Gemini REST API
    fun fetchDailyVerse(topic: String = _verseTopic.value) {
        _verseTopic.value = topic
        viewModelScope.launch {
            _isVerseLoading.value = true
            _verseError.value = null
            _deepVerseExplanation.value = null

            try {
                val verse = quranRepository.getDailyVerse(topic)
                _currentDailyVerse.value = verse
                _currentVerse.value = QuranVerseResponse(
                    surahName = verse.surahName,
                    surahNumber = verse.surahNumber,
                    verseNumber = verse.verseNumber,
                    arabic = verse.arabicText,
                    translation = verse.translation,
                    reflection = verse.explanation
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error in fetchDailyVerse", e)
                _verseError.value = "Failed to load verse. Showing fallback."
                val fallback = quranRepository.getFallbackVerse(topic)
                _currentDailyVerse.value = fallback
                _currentVerse.value = QuranVerseResponse(
                    surahName = fallback.surahName,
                    surahNumber = fallback.surahNumber,
                    verseNumber = fallback.verseNumber,
                    arabic = fallback.arabicText,
                    translation = fallback.translation,
                    reflection = fallback.explanation
                )
            } finally {
                _isVerseLoading.value = false
                checkIfCurrentVerseSaved()
            }
        }
    }

    fun generateDeepExplanation() {
        val verse = _currentDailyVerse.value
        viewModelScope.launch {
            _isGeneratingExplanation.value = true
            try {
                val explanation = quranRepository.generateDeepExplanation(
                    surahName = verse.surahName,
                    verseNumber = verse.verseNumber,
                    arabicText = verse.arabicText,
                    translation = verse.translation
                )
                _deepVerseExplanation.value = explanation
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to generate deep explanation", e)
                _deepVerseExplanation.value = "Contemplate the profound peace and guidance embedded within this sacred verse."
            } finally {
                _isGeneratingExplanation.value = false
            }
        }
    }

    // Bookmarking / Saving Verses
    fun toggleBookmarkCurrentVerse() {
        viewModelScope.launch {
            val verse = _currentVerse.value
            val isSaved = _isCurrentVerseSaved.value
            if (isSaved) {
                // Delete
                val allSaved = savedVerseDao.getAllSavedVerses().first()
                val target = allSaved.find { it.surahNumber == verse.surahNumber && it.verseNumber == verse.verseNumber }
                if (target != null) {
                    savedVerseDao.deleteSavedVerse(target)
                }
            } else {
                // Insert
                savedVerseDao.insertSavedVerse(
                    SavedVerse(
                        surahNumber = verse.surahNumber,
                        verseNumber = verse.verseNumber,
                        surahName = verse.surahName,
                        arabicText = verse.arabic,
                        translation = verse.translation,
                        reflection = verse.reflection
                    )
                )
            }
            checkIfCurrentVerseSaved()
        }
    }

    private fun checkIfCurrentVerseSaved() {
        viewModelScope.launch {
            val verse = _currentVerse.value
            _isCurrentVerseSaved.value = savedVerseDao.isVerseSaved(verse.surahNumber, verse.verseNumber)
        }
    }

    fun deleteSavedVerse(saved: SavedVerse) {
        viewModelScope.launch {
            savedVerseDao.deleteSavedVerse(saved)
            checkIfCurrentVerseSaved()
        }
    }

    // Compass Sensor Callback
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            
            // Azimuth is orientation[0], in radians. Convert to degrees.
            var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            azimuth = (azimuth + 360f) % 360f
            _deviceAzimuth.value = azimuth
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

    // Fetch daily Hadith using Gemini REST API or Fallback
    fun fetchDailyHadith(topic: String = "Sincerity & Wisdom") {
        viewModelScope.launch {
            _isHadithLoading.value = true
            _hadithError.value = null

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                withContext(Dispatchers.IO) {
                    val fallback = HadithRepository.getRandomHadith()
                    _currentHadith.value = fallback
                }
                _isHadithLoading.value = false
                return@launch
            }

            try {
                val prompt = """
                    Please provide an authentic Hadith (saying of the Prophet Muhammad ﷺ) that aligns with the theme: "$topic".
                    Return a JSON object exactly matching this schema:
                    {
                      "text": "The exact Arabic text of the Hadith",
                      "translation": "The English translation of the Hadith",
                      "source": "The exact authentic source of the Hadith (e.g. Sahih al-Bukhari 1, Sahih Muslim 1907)",
                      "reflection": "A beautiful, modern spiritual reflection (2-3 sentences) linking this Hadith to daily life, personal growth, and compassion."
                    }
                    Return ONLY raw JSON, with no markdown formatting.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(com.example.network.Content(parts = listOf(com.example.network.Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.7
                    ),
                    systemInstruction = com.example.network.Content(parts = listOf(com.example.network.Part(
                        text = "You are a highly respectful, knowledgeable Islamic scholar who excels at presenting authentic Hadiths with correct sources to modern audiences in an inspiring, peaceful, and beautifully reflective light."
                    )))
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }

                val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (rawJson != null) {
                    val adapter: JsonAdapter<HadithResponse> = RetrofitClient.moshiParser.adapter(HadithResponse::class.java)
                    val parsed = withContext(Dispatchers.Default) {
                        adapter.fromJson(rawJson)
                    }
                    if (parsed != null) {
                        _currentHadith.value = parsed
                    } else {
                        _hadithError.value = "Failed to parse Hadith schema."
                        _currentHadith.value = HadithRepository.getRandomHadith()
                    }
                } else {
                    _hadithError.value = "No response content received."
                    _currentHadith.value = HadithRepository.getRandomHadith()
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching Hadith from Gemini API", e)
                _hadithError.value = "API connection offline. Showing fallback Hadith."
                _currentHadith.value = HadithRepository.getRandomHadith()
            } finally {
                _isHadithLoading.value = false
            }
        }
    }

    // Helper to calculate Ramadan Calendar days dynamically
    private fun calculateRamadanCalendar(latitude: Double, longitude: Double, timezoneOffset: Double): List<RamadanDay> {
        val list = mutableListOf<RamadanDay>()
        
        val today = Calendar.getInstance()
        // Determine whether to use Ramadan 2026 or Ramadan 2027
        val ramadanYear = if (today.get(Calendar.YEAR) < 2026 || (today.get(Calendar.YEAR) == 2026 && today.get(Calendar.MONTH) <= Calendar.MARCH)) {
            2026
        } else {
            2027
        }
        
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, ramadanYear)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DAY_OF_MONTH, if (ramadanYear == 2026) 18 else 7)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
        
        val isTodaySameYearMonthDay = { cal: Calendar ->
            today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
            today.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
            today.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)
        }

        val dateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        for (day in 1..30) {
            val currentDayCal = startCal.clone() as Calendar
            currentDayCal.add(Calendar.DAY_OF_MONTH, day - 1)
            
            val times = PrayerTimesCalculator.calculateTimes(
                calendar = currentDayCal,
                latitude = latitude,
                longitude = longitude,
                timezoneOffset = timezoneOffset
            )
            
            val imsakTime = subtractTenMinutes(times.fajr)
            
            list.add(
                RamadanDay(
                    dayOfRamadan = day,
                    dateString = dateFormatter.format(currentDayCal.time),
                    imsak = imsakTime,
                    iftar = times.maghrib,
                    isToday = isTodaySameYearMonthDay(currentDayCal)
                )
            )
        }
        return list
    }

    private fun subtractTenMinutes(fajrTime: String): String {
        return try {
            val parts = fajrTime.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            var totalMins = hours * 60 + minutes - 10
            if (totalMins < 0) totalMins += 24 * 60
            val finalHours = (totalMins / 60) % 24
            val finalMins = totalMins % 60
            String.format(Locale.getDefault(), "%02d:%02d", finalHours, finalMins)
        } catch (e: Exception) {
            fajrTime
        }
    }

    // Helper to calculate Islamic events with countdowns dynamically
    private fun calculateIslamicEvents(): List<IslamicEvent> {
        val list = mutableListOf<IslamicEvent>()
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        
        val createEventCalendar = { y: Int, m: Int, d: Int ->
            Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        val eventDefs = if (year <= 2026) {
            listOf(
                Triple("Laylat al-Qadr", createEventCalendar(2026, Calendar.MARCH, 15), "The Night of Power, better than a thousand months. Focus on deep prayer, Quran recitation, and seeking forgiveness."),
                Triple("Eid al-Fitr", createEventCalendar(2026, Calendar.MARCH, 19), "Celebration of breaking the fast, marking the end of the holy month of Ramadan with prayers, charity, and feasting."),
                Triple("Eid al-Adha", createEventCalendar(2026, Calendar.MAY, 26), "Festival of Sacrifice, commemorating Ibrahim's devotion and the completion of the Hajj pilgrimage."),
                Triple("Islamic New Year", createEventCalendar(2026, Calendar.JULY, 16), "Hijri New Year, marking the migration (Hijrah) of the Prophet Muhammad from Makkah to Madinah."),
                Triple("Day of Ashura", createEventCalendar(2026, Calendar.JULY, 25), "Highly significant day of fasting, commemorating the saving of Prophet Musa (Moses) and his followers.")
            )
        } else {
            listOf(
                Triple("Laylat al-Qadr", createEventCalendar(2027, Calendar.MARCH, 4), "The Night of Power, better than a thousand months. Focus on deep prayer, Quran recitation, and seeking forgiveness."),
                Triple("Eid al-Fitr", createEventCalendar(2027, Calendar.MARCH, 8), "Celebration of breaking the fast, marking the end of the holy month of Ramadan with prayers, charity, and feasting."),
                Triple("Eid al-Adha", createEventCalendar(2027, Calendar.MAY, 16), "Festival of Sacrifice, commemorating Ibrahim's devotion and the completion of the Hajj pilgrimage."),
                Triple("Islamic New Year", createEventCalendar(2027, Calendar.JULY, 5), "Hijri New Year, marking the migration (Hijrah) of the Prophet Muhammad from Makkah to Madinah."),
                Triple("Day of Ashura", createEventCalendar(2027, Calendar.JULY, 14), "Highly significant day of fasting, commemorating the saving of Prophet Musa (Moses) and his followers.")
            )
        }

        val todayZero = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        eventDefs.forEach { (name, cal, desc) ->
            val diffMs = cal.timeInMillis - todayZero.timeInMillis
            val days = diffMs / (1000 * 60 * 60 * 24)
            list.add(
                IslamicEvent(
                    name = name,
                    dateString = dateFormatter.format(cal.time),
                    daysRemaining = days,
                    description = desc,
                    calendarDate = cal
                )
            )
        }
        return list
    }
}

data class RamadanDay(
    val dayOfRamadan: Int,
    val dateString: String,
    val imsak: String,
    val iftar: String,
    val isToday: Boolean
)

data class IslamicEvent(
    val name: String,
    val dateString: String,
    val daysRemaining: Long,
    val description: String,
    val calendarDate: Calendar
)
