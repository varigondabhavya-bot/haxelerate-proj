package com.example.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.models.AlertEntity
import com.example.data.models.BrandEntity
import com.example.data.models.FirebaseUserEntity
import com.example.data.models.InvestigatorNoteEntity
import com.example.data.models.ThreatIncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandShieldDao {
    // Firebase Users & Auth Profiles
    @Query("SELECT * FROM firebase_users ORDER BY lastLoginAt DESC")
    fun getAllFirebaseUsers(): Flow<List<FirebaseUserEntity>>

    @Query("SELECT * FROM firebase_users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getFirebaseUserByEmail(email: String): FirebaseUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFirebaseUser(user: FirebaseUserEntity)

    // Brands
    @Query("SELECT * FROM brands ORDER BY createdAt ASC")
    fun getAllBrands(): Flow<List<BrandEntity>>

    @Query("SELECT * FROM brands WHERE brandId = :brandId LIMIT 1")
    suspend fun getBrandById(brandId: String): BrandEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: BrandEntity)

    @Query("DELETE FROM brands WHERE brandId = :brandId")
    suspend fun deleteBrandById(brandId: String)

    @Query("DELETE FROM threats WHERE brandId = :brandId")
    suspend fun deleteThreatsByBrandId(brandId: String)

    @Query("SELECT COUNT(*) FROM brands")
    suspend fun getBrandCount(): Int

    // Threats
    @Query("SELECT * FROM threats ORDER BY detectedAt DESC")
    fun getAllThreats(): Flow<List<ThreatIncidentEntity>>

    @Query("SELECT * FROM threats WHERE threatId = :threatId LIMIT 1")
    fun observeThreatById(threatId: String): Flow<ThreatIncidentEntity?>

    @Query("SELECT * FROM threats WHERE threatId = :threatId LIMIT 1")
    suspend fun getThreatById(threatId: String): ThreatIncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreat(threat: ThreatIncidentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreats(threats: List<ThreatIncidentEntity>)

    @Update
    suspend fun updateThreat(threat: ThreatIncidentEntity)

    @Query("UPDATE threats SET status = :newStatus WHERE threatId = :threatId")
    suspend fun updateThreatStatus(threatId: String, newStatus: String)

    @Query("UPDATE threats SET assignedTo = :analystName, status = :newStatus WHERE threatId = :threatId")
    suspend fun assignThreatAnalyst(threatId: String, analystName: String, newStatus: String)

    @Query("UPDATE threats SET aiExplanation = :explanation, aiChecklistCsv = :checklistCsv WHERE threatId = :threatId")
    suspend fun updateThreatAiAnalysis(threatId: String, explanation: String, checklistCsv: String)

    // Investigator Notes
    @Query("SELECT * FROM investigator_notes WHERE threatId = :threatId ORDER BY timestamp DESC")
    fun getNotesForThreat(threatId: String): Flow<List<InvestigatorNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: InvestigatorNoteEntity)

    // Alerts
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("UPDATE alerts SET isRead = 1 WHERE alertId = :alertId")
    suspend fun markAlertRead(alertId: Int)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAlertsRead()
}

@Database(
    entities = [
        BrandEntity::class,
        ThreatIncidentEntity::class,
        InvestigatorNoteEntity::class,
        AlertEntity::class,
        FirebaseUserEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BrandShieldDatabase : RoomDatabase() {
    abstract fun dao(): BrandShieldDao

    companion object {
        @Volatile
        private var INSTANCE: BrandShieldDatabase? = null

        fun getDatabase(context: Context): BrandShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrandShieldDatabase::class.java,
                    "brandshield_soc.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
