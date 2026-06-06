package com.example.data.repository

import com.example.data.local.EducationDao
import com.example.data.model.EducationMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class EducationRepository(private val educationDao: EducationDao) {

    val allMetrics: Flow<List<EducationMetric>> = educationDao.getAllMetrics()

    fun getMetricsByCategory(category: String): Flow<List<EducationMetric>> {
        return educationDao.getMetricsByCategory(category)
    }

    suspend fun insertMetric(metric: EducationMetric): Long {
        return educationDao.insertMetric(metric)
    }

    suspend fun updateMetric(metric: EducationMetric) {
        educationDao.updateMetric(metric)
    }

    suspend fun toggleBookmark(id: Int) {
        val metric = educationDao.getMetricById(id)
        if (metric != null) {
            educationDao.updateMetric(metric.copy(isBookmarked = !metric.isBookmarked))
        }
    }

    suspend fun deleteMetricById(id: Int) {
        educationDao.deleteMetricById(id)
    }

    suspend fun clearAll() {
        educationDao.clearAllMetrics()
    }

    suspend fun seedSampleDataIfEmpty() {
        val count = educationDao.getMetricsCount()
        if (count == 0) {
            val list = mutableListOf<EducationMetric>()

            // Category: Global Indicators
            // Subcategory: Literacy Rate (%)
            val literacyData = listOf(
                TestData("North America", 2020..2024, listOf(99.0, 99.1, 99.1, 99.2, 99.3)),
                TestData("East Asia & Pacific", 2020..2024, listOf(95.8, 96.0, 96.2, 96.5, 96.9)),
                TestData("Sub-Saharan Africa", 2020..2024, listOf(64.5, 65.2, 65.9, 66.4, 67.2)),
                TestData("Latin America", 2020..2024, listOf(92.4, 92.8, 93.1, 93.5, 93.9)),
                TestData("South Asia", 2020..2024, listOf(71.2, 72.1, 73.0, 74.2, 75.3))
            )
            for (td in literacyData) {
                td.years.forEachIndexed { index, year ->
                    list.add(
                        EducationMetric(
                            category = "Global Indicators",
                            subcategory = "Literacy Rate (%)",
                            region = td.region,
                            year = year,
                            value = td.values[index],
                            unit = "%",
                            notes = "Percent of population aged 15 and above who can read and write with understanding."
                        )
                    )
                }
            }

            // Subcategory: Pupil-Teacher Ratio (Primary)
            val pupilTeacherData = listOf(
                TestData("North America", 2020..2024, listOf(14.2, 14.1, 13.9, 13.8, 13.5)),
                TestData("East Asia & Pacific", 2020..2024, listOf(17.5, 17.2, 16.9, 16.6, 16.2)),
                TestData("Sub-Saharan Africa", 2020..2024, listOf(38.6, 38.0, 37.5, 36.8, 36.1)),
                TestData("Latin America", 2020..2024, listOf(21.0, 20.6, 20.1, 19.7, 19.3)),
                TestData("South Asia", 2020..2024, listOf(29.8, 29.1, 28.5, 27.9, 27.2))
            )
            for (td in pupilTeacherData) {
                td.years.forEachIndexed { index, year ->
                    list.add(
                        EducationMetric(
                            category = "Global Indicators",
                            subcategory = "Pupil-Teacher Ratio",
                            region = td.region,
                            year = year,
                            value = td.values[index],
                            unit = "students/teacher",
                            notes = "Average number of pupils per teacher in primary schools. Lower denotes higher individualized attention."
                        )
                    )
                }
            }

            // Subcategory: Education Spending (% of GDP)
            val spendingData = listOf(
                TestData("North America", 2020..2024, listOf(5.2, 5.4, 5.1, 5.3, 5.6)),
                TestData("East Asia & Pacific", 2020..2024, listOf(4.1, 4.2, 4.3, 4.4, 4.5)),
                TestData("Sub-Saharan Africa", 2020..2024, listOf(3.4, 3.6, 3.5, 3.7, 3.9)),
                TestData("Latin America", 2020..2024, listOf(4.6, 4.7, 4.5, 4.6, 4.9)),
                TestData("OECD Average", 2020..2024, listOf(4.9, 5.0, 4.8, 4.9, 5.1))
            )
            for (td in spendingData) {
                td.years.forEachIndexed { index, year ->
                    list.add(
                        EducationMetric(
                            category = "Global Indicators",
                            subcategory = "Spending (% GDP)",
                            region = td.region,
                            year = year,
                            value = td.values[index],
                            unit = "% GDP",
                            notes = "Total public expenditure on education (current and capital) expressed as a percentage of GDP."
                        )
                    )
                }
            }

            // Category: Institutional Performance
            // Subcategory: Graduation Rate (%)
            val gradData = listOf(
                TestData("Metropolis High School", 2020..2024, listOf(86.5, 87.2, 88.0, 89.4, 91.5)),
                TestData("Westside Academy", 2020..2024, listOf(92.0, 92.5, 93.4, 94.1, 95.3)),
                TestData("Oakridge Technical Institute", 2020..2024, listOf(78.4, 80.1, 81.5, 83.0, 85.2)),
                TestData("Crestview Charter School", 2020..2024, listOf(88.1, 88.9, 89.5, 90.2, 91.0))
            )
            for (td in gradData) {
                td.years.forEachIndexed { index, year ->
                    list.add(
                        EducationMetric(
                            category = "Institutional Performance",
                            subcategory = "Graduation Rate",
                            region = td.region,
                            year = year,
                            value = td.values[index],
                            unit = "%",
                            notes = "Percentage of students completing secondary education within theoretical timeframe."
                        )
                    )
                }
            }

            // Subcategory: PISA Math Score
            val mathData = listOf(
                TestData("Metropolis High School", 2020..2024, listOf(492.0, 498.0, 502.0, 509.0, 516.0)),
                TestData("Westside Academy", 2020..2024, listOf(512.0, 518.0, 524.0, 528.0, 534.0)),
                TestData("Oakridge Technical Institute", 2020..2024, listOf(461.0, 467.0, 473.0, 479.0, 486.0)),
                TestData("PISA OECD Norm", 2020..2024, listOf(480.0, 481.0, 482.0, 483.0, 485.0))
            )
            for (td in mathData) {
                td.years.forEachIndexed { index, year ->
                    list.add(
                        EducationMetric(
                            category = "Institutional Performance",
                            subcategory = "PISA Score (Math)",
                            region = td.region,
                            year = year,
                            value = td.values[index],
                            unit = "score",
                            notes = "Standardized PISA score assessing mathematical competence, reasoning, and real-world application."
                        )
                    )
                }
            }

            // Category: Vocational & STEM Specializations
            // Subcategory: STEM Enrollment Rate (%)
            val stemData = listOf(
                TestData("Metropolis High School", 2020..2024, listOf(28.5, 29.8, 31.7, 33.5, 35.8)),
                TestData("Westside Academy", 2020..2024, listOf(35.2, 36.9, 38.5, 39.8, 41.5)),
                TestData("Oakridge Technical Institute", 2020..2024, listOf(45.0, 47.5, 51.0, 54.2, 58.6)),
                TestData("Crestview Charter School", 2020..2024, listOf(22.1, 23.5, 24.8, 26.0, 27.5))
            )
            for (td in stemData) {
                td.years.forEachIndexed { index, year ->
                    list.add(
                        EducationMetric(
                            category = "Vocational & STEM",
                            subcategory = "STEM Enrollment",
                            region = td.region,
                            year = year,
                            value = td.values[index],
                            unit = "%",
                            notes = "Proportion of enrolled student population majoring or specializing in Science, Technology, Engineering, and Math."
                        )
                    )
                }
            }

            // Subcategory: Funding Per Student ($)
            val fundingData = listOf(
                TestData("Metropolis High School", 2020..2024, listOf(8200.0, 8500.0, 8900.0, 9300.0, 9700.0)),
                TestData("Westside Academy", 2020..2024, listOf(11200.0, 11500.0, 11800.0, 12200.0, 12600.0)),
                TestData("Oakridge Technical Institute", 2020..2024, listOf(9800.0, 10200.0, 10700.0, 11200.0, 11900.0)),
                TestData("Crestview Charter School", 2020..2024, listOf(7400.0, 7700.0, 8100.0, 8400.0, 8800.0))
            )
            for (td in fundingData) {
                td.years.forEachIndexed { index, year ->
                    list.add(
                        EducationMetric(
                            category = "Vocational & STEM",
                            subcategory = "Funding Per Scholar",
                            region = td.region,
                            year = year,
                            value = td.values[index],
                            unit = "USD",
                            notes = "Annual operational and academic expenditure allocated directly per enrolled student."
                        )
                    )
                }
            }

            educationDao.insertMetrics(list)
        }
    }

    private data class TestData(
        val region: String,
        val years: IntRange,
        val values: List<Double>
    )
}
