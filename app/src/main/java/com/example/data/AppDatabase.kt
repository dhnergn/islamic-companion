package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_verses")
data class SavedVerse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int,
    val verseNumber: Int,
    val surahName: String,
    val arabicText: String,
    val translation: String,
    val reflection: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_track")
data class PrayerTrack(
    @PrimaryKey val date: String, // format: YYYY-MM-DD
    val fajrCompleted: Boolean = false,
    val dhuhrCompleted: Boolean = false,
    val asrCompleted: Boolean = false,
    val maghribCompleted: Boolean = false,
    val ishaCompleted: Boolean = false
)

@Entity(tableName = "tasbih_counters")
data class TasbihItem(
    @PrimaryKey val id: String, // e.g., name-based ID or random UUID
    val name: String,
    val count: Int = 0,
    val target: Int = 33,
    val isDefault: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SavedVerseDao {
    @Query("SELECT * FROM saved_verses ORDER BY timestamp DESC")
    fun getAllSavedVerses(): Flow<List<SavedVerse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedVerse(verse: SavedVerse)

    @Delete
    suspend fun deleteSavedVerse(verse: SavedVerse)

    @Query("SELECT EXISTS(SELECT * FROM saved_verses WHERE surahNumber = :surah AND verseNumber = :verse)")
    suspend fun isVerseSaved(surah: Int, verse: Int): Boolean
}

@Dao
interface PrayerTrackDao {
    @Query("SELECT * FROM prayer_track WHERE date = :date")
    fun getTrackForDate(date: String): Flow<PrayerTrack?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTrack(track: PrayerTrack)
}

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_counters ORDER BY isDefault DESC, timestamp ASC")
    fun getAllTasbihs(): Flow<List<TasbihItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTasbih(tasbih: TasbihItem)

    @Delete
    suspend fun deleteTasbih(tasbih: TasbihItem)

    @Query("UPDATE tasbih_counters SET count = 0 WHERE id = :id")
    suspend fun resetTasbih(id: String)
}

@Database(entities = [SavedVerse::class, PrayerTrack::class, TasbihItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedVerseDao(): SavedVerseDao
    abstract fun prayerTrackDao(): PrayerTrackDao
    abstract fun tasbihDao(): TasbihDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "islamic_companion_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
