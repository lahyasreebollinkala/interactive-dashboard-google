package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EducationDatabase
import com.example.data.model.EducationMetric
import com.example.data.repository.EducationRepository
import com.example.data.network.GeminiNetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EducationRepository

    // Search and filter parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow("All")
    val selectedSubcategory: StateFlow<String> = _selectedSubcategory.asStateFlow()

    private val _selectedYear = MutableStateFlow("All") // "All" or specific year e.g., "2024"
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    // AI Analytical States
    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    init {
        val database = EducationDatabase.getDatabase(application)
        repository = EducationRepository(database.educationDao())
        
        // Seed some data so dashboard starts live immediately
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedSampleDataIfEmpty()
        }
    }

    // Computed flows that react to database changes and filter selections
    val allMetrics: StateFlow<List<EducationMetric>> = repository.allMetrics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMetrics: StateFlow<List<EducationMetric>> = combine(
        allMetrics,
        _searchQuery,
        _selectedCategory,
        _selectedSubcategory,
        _selectedYear
    ) { metrics, search, category, subcat, year ->
        metrics.filter { metric ->
            val matchesSearch = search.isEmpty() ||
                    metric.region.contains(search, ignoreCase = true) ||
                    metric.category.contains(search, ignoreCase = true) ||
                    metric.subcategory.contains(search, ignoreCase = true) ||
                    metric.notes.contains(search, ignoreCase = true)

            val matchesCategory = category == "All" || metric.category == category
            val matchesSubcat = subcat == "All" || metric.subcategory == subcat
            val matchesYear = year == "All" || metric.year.toString() == year

            matchesSearch && matchesCategory && matchesSubcat && matchesYear
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique Categories available
    val categories: StateFlow<List<String>> = allMetrics.map { list ->
        listOf("All") + list.map { it.category }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Unique Subcategories based on selected category
    val subcategories: StateFlow<List<String>> = combine(allMetrics, _selectedCategory) { list, category ->
        val filtered = if (category == "All") list else list.filter { it.category == category }
        listOf("All") + filtered.map { it.subcategory }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Unique Years available
    val years: StateFlow<List<String>> = allMetrics.map { list ->
        listOf("All") + list.map { it.year.toString() }.distinct().sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Bookmarked metrics only
    val bookmarkedMetrics: StateFlow<List<EducationMetric>> = allMetrics.map { list ->
        list.filter { it.isBookmarked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Methods to change filter parameters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
        // Reset subcategory if category changes to keep filters valid
        _selectedSubcategory.value = "All"
    }

    fun setSelectedSubcategory(subcategory: String) {
        _selectedSubcategory.value = subcategory
    }

    fun setSelectedYear(year: String) {
        _selectedYear.value = year
    }

    // Add, Toggle, Delete operations
    fun addMetric(
        category: String,
        subcategory: String,
        region: String,
        year: Int,
        value: Double,
        unit: String,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val metric = EducationMetric(
                category = category,
                subcategory = subcategory,
                region = region,
                year = year,
                value = value,
                unit = unit,
                notes = notes,
                isCustom = true
            )
            repository.insertMetric(metric)
        }
    }

    fun toggleBookmark(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(id)
        }
    }

    fun deleteMetric(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMetricById(id)
        }
    }

    fun clearDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
            repository.seedSampleDataIfEmpty()
        }
    }

    // Call Gemini API automatically with the selected metrics and user prompt
    fun askGeminiForInsights(educationQuestion: String) {
        _aiLoading.value = true
        _aiResult.value = null
        viewModelScope.launch(Dispatchers.IO) {
            // Pick a snapshot of filtered metrics to build context
            val metricsSnapshot = filteredMetrics.value.take(25)
            val metricsSummary = metricsSnapshot.joinToString("\n") {
                "- [${it.category}] ${it.subcategory} in ${it.region} (${it.year}): ${it.value} ${it.unit}"
            }

            val systemPrompt = """
                You are a highly analytical World Education Policy Advisor.
                You are looking at a subset of educational statistics provided by the user.
                Provide structured, professional, evidence-based guidelines and analysis to improve national and regional education outcomes.
                Keep your insights concise, engaging, and directly applicable.
                Strictly do not use any markdown formatting except bullet points, bold headers, and line breaks for clean viewing.
            """.trimIndent()

            val fullPrompt = """
                Selected Educational Data Snapshot:
                $metricsSummary
                
                Question / Focus:
                $educationQuestion
                
                Please analyze the data and explain policy guidelines, structural trends, and step-by-step solutions to address the user's issue.
            """.trimIndent()

            val result = GeminiNetworkClient.generateEducationalInsight(fullPrompt, systemPrompt)
            _aiResult.value = result
            _aiLoading.value = false
        }
    }

    // Reset AI insights
    fun clearAiResult() {
        _aiResult.value = null
    }
}
