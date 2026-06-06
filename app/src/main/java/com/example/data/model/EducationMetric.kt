package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "education_metrics")
data class EducationMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,       // e.g. "Global Indicators", "Institutional Performance", "Financial Allocation"
    val subcategory: String,    // e.g. "Literacy Rate", "Pupil-Teacher Ratio", "Graduation Rate", "Education Spending (% GDP)"
    val region: String,         // e.g. "North America", "Sub-Saharan Africa", "High School Alpha", "PISA OECD"
    val year: Int,              // e.g. 2020, 2021, 2022, 2023, 2024, 2025
    val value: Double,          // e.g. 94.5, 12.8, 88.0, 5200.00
    val unit: String,           // e.g. "%", "students/teacher", "$ per student", "PISA score"
    val notes: String = "",     // Optional custom policy context or explanation
    val isBookmarked: Boolean = false,
    val isCustom: Boolean = false // Set to true if added by the user
)
