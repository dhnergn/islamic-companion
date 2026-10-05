package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AladhanResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: AladhanData
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    @Json(name = "timings") val timings: AladhanTimings,
    @Json(name = "date") val date: AladhanDate
)

@JsonClass(generateAdapter = true)
data class AladhanTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Sunset") val sunset: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String,
    @Json(name = "Midnight") val midnight: String
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    @Json(name = "readable") val readable: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "hijri") val hijri: HijriDate? = null
)

@JsonClass(generateAdapter = true)
data class HijriDate(
    @Json(name = "date") val date: String,
    @Json(name = "format") val format: String,
    @Json(name = "day") val day: String,
    @Json(name = "weekday") val weekday: HijriWeekday? = null,
    @Json(name = "month") val month: HijriMonth? = null,
    @Json(name = "year") val year: String
)

@JsonClass(generateAdapter = true)
data class HijriWeekday(
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class HijriMonth(
    @Json(name = "number") val number: Int,
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String? = null
)

interface AladhanApiService {
    @GET("v1/timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int? = null
    ): AladhanResponse
}

object AladhanClient {
    private const val BASE_URL = "https://api.aladhan.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val service: AladhanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AladhanApiService::class.java)
    }
}
