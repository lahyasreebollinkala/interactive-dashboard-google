package com.example.data.local

import androidx.room.*
import com.example.data.model.EducationMetric
import kotlinx.coroutines.flow.Flow

@Dao
interface EducationDao {
    @Query("SELECT * FROM education_metrics ORDER BY category ASC, subcategory ASC, region ASC, year DESC")
    fun getAllMetrics(): Flow<List<EducationMetric>>

    @Query("SELECT * FROM education_metrics WHERE id = :id")
    suspend fun getMetricById(id: Int): EducationMetric?

    @Query("SELECT * FROM education_metrics WHERE category = :category ORDER BY year DESC")
    fun getMetricsByCategory(category: String): Flow<List<EducationMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: EducationMetric): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetrics(metrics: List<EducationMetric>)

    @Update
    suspend fun updateMetric(metric: EducationMetric)

    @Delete
    suspend fun deleteMetric(metric: EducationMetric)

    @Query("DELETE FROM education_metrics WHERE id = :id")
    suspend fun deleteMetricById(id: Int)

    @Query("DELETE FROM education_metrics")
    suspend fun clearAllMetrics()

    @Query("SELECT COUNT(*) FROM education_metrics")
    suspend fun getMetricsCount(): Int
}
