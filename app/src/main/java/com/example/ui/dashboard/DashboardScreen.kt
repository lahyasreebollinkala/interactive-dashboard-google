package com.example.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EducationMetric
import com.example.ui.viewmodel.DashboardViewModel
import java.util.Locale
import kotlin.math.roundToInt

// Soft and modern Material 3 Dashboard colors
private val TrendBlue = Color(0xFF0284C7)
private val ChartIndigo = Color(0xFF6366F1)
private val EmeraldGreen = Color(0xFF10B981)
private val WarmAmber = Color(0xFFF59E0B)
private val DarkSlate = Color(0xFF0F172A)
private val LightCardBg = Color(0xFFF8FAFC)
private val SoftBorder = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val allMetrics by viewModel.allMetrics.collectAsStateWithLifecycle()
    val filteredMetrics by viewModel.filteredMetrics.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subcategories by viewModel.subcategories.collectAsStateWithLifecycle()
    val years by viewModel.years.collectAsStateWithLifecycle()
    
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()

    val bookmarks by viewModel.bookmarkedMetrics.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Floating Interactive Focus Point for Chart Detail
    var hoveredPointInfo by remember { mutableStateOf<String?>(null) }

    // Dynamic Calculations for Dashboard KPI's Based on current list
    val avgLiteracy = remember(filteredMetrics) {
        val literacyItems = filteredMetrics.filter { it.subcategory.contains("Literacy", ignoreCase = true) }
        if (literacyItems.isNotEmpty()) literacyItems.map { it.value }.average() else null
    }

    val avgPupilTeacher = remember(filteredMetrics) {
        val ptItems = filteredMetrics.filter { it.subcategory.contains("Pupil", ignoreCase = true) }
        if (ptItems.isNotEmpty()) ptItems.map { it.value }.average() else null
    }

    val avgSpendingGdp = remember(filteredMetrics) {
        val spendItems = filteredMetrics.filter { it.subcategory.contains("Spending", ignoreCase = true) }
        if (spendItems.isNotEmpty()) spendItems.map { it.value }.average() else null
    }

    val studentFunding = remember(filteredMetrics) {
        val fundItems = filteredMetrics.filter { it.subcategory.contains("Funding", ignoreCase = true) }
        if (fundItems.isNotEmpty()) fundItems.map { it.value }.average() else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Custom SVG-like graduation accent icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(TrendBlue, ChartIndigo))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "EDUCATE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Interactive Metrics Analyzer",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.restoreSampleData() },
                        modifier = Modifier.testTag("reset_dataset")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restore Default Sample Data",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (activeTab == 1) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("add_custom_record_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Metrics Entry")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // KPI dynamic panel bar
            EducationKpiMetricsPanel(
                avgLiteracy = avgLiteracy,
                avgPtRatio = avgPupilTeacher,
                avgSpending = avgSpendingGdp ?: studentFunding
            )

            // Dynamic Tab Indicators
            PrimaryTabRow(
                selectedTabIndex = activeTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Visual Analytics", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_analytics")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Data Explorer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_explorer")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("AI Strategic Advisor", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_ai_advisor")
                )
            }

            // Central Animated Swapper Content
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> AnalyticsTabContent(
                        metrics = filteredMetrics,
                        allMetrics = allMetrics,
                        hoveredPointInfo = hoveredPointInfo,
                        onHoverPointChange = { hoveredPointInfo = it }
                    )
                    1 -> ExplorerTabContent(
                        metrics = filteredMetrics,
                        searchQuery = searchQuery,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        subcategories = subcategories,
                        selectedSubcategory = selectedSubcategory,
                        years = years,
                        selectedYear = selectedYear,
                        bookmarks = bookmarks,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategorySelect = { viewModel.setSelectedCategory(it) },
                        onSubcatSelect = { viewModel.setSelectedSubcategory(it) },
                        onYearSelect = { viewModel.setSelectedYear(it) },
                        onBookmarkToggle = { viewModel.toggleBookmark(it) },
                        onDeleteMetric = { viewModel.deleteMetric(it) }
                    )
                    2 -> AiAdvisorTabContent(
                        aiResult = aiResult,
                        aiLoading = aiLoading,
                        onSubmitQuestion = { viewModel.askGeminiForInsights(it) },
                        onClearResult = { viewModel.clearAiResult() }
                    )
                }
            }
        }
    }

    // Modal dialog for entering customized records
    if (showAddDialog) {
        AddMetricRecordDialog(
            categories = categories.filter { it != "All" },
            onDismiss = { showAddDialog = false },
            onSave = { category, subcat, region, year, value, unit, notes ->
                viewModel.addMetric(category, subcat, region, year, value, unit, notes)
                showAddDialog = false
            }
        )
    }
}

// Subcomponent: Live calculated KPI panels using clean typography rules
@Composable
fun EducationKpiMetricsPanel(
    avgLiteracy: Double?,
    avgPtRatio: Double?,
    avgSpending: Double?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiWidget(
            title = "Avg Literacy Rate",
            value = if (avgLiteracy != null) String.format("%.1f", avgLiteracy) + "%" else "N/A",
            indicatorColor = EmeraldGreen,
            description = "Selected region reading benchmark",
            icon = Icons.Default.Check,
            modifier = Modifier.width(160.dp)
        )
        KpiWidget(
            title = "Avg Pupil-Teacher",
            value = if (avgPtRatio != null) String.format("%.1f", avgPtRatio) else "N/A",
            indicatorColor = TrendBlue,
            description = "Instruction density ratio",
            icon = Icons.Default.Info,
            modifier = Modifier.width(160.dp)
        )
        KpiWidget(
            title = "Avg Allocation Impact",
            value = if (avgSpending != null) {
                if (avgSpending > 100) "${String.format("%,.0f", avgSpending)}" else "${String.format("%.2f", avgSpending)}% GDP"
            } else "N/A",
            indicatorColor = WarmAmber,
            description = "Direct financial support metric",
            icon = Icons.Default.Star,
            modifier = Modifier.width(180.dp)
        )
    }
}

@Composable
fun KpiWidget(
    title: String,
    value: String,
    indicatorColor: Color,
    description: String,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBg),
        border = BorderStroke(1.dp, SoftBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(indicatorColor, CircleShape)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlate,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 9.sp,
                color = Color.Gray,
                lineHeight = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------- VISUAL ANALYTICS TAB CONTENT ----------------------
@Composable
fun AnalyticsTabContent(
    metrics: List<EducationMetric>,
    allMetrics: List<EducationMetric>,
    hoveredPointInfo: String?,
    onHoverPointChange: (String?) -> Unit
) {
    // Determine unique subcategories from the current database to plot
    val availableSubcats = remember(allMetrics) {
        allMetrics.map { it.subcategory }.distinct().sorted()
    }

    if (availableSubcats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.LightGray
                )
                Text("No data points are loaded. Click the restore database refresh button above.", textAlign = TextAlign.Center, color = Color.Gray)
            }
        }
        return
    }

    var chartSubcategory by remember { mutableStateOf(availableSubcats.firstOrNull() ?: "") }
    // Auto sync selection when subcategory catalog changes
    LaunchedEffect(availableSubcats) {
        if (chartSubcategory.isEmpty() && availableSubcats.isNotEmpty()) {
            chartSubcategory = availableSubcats.first()
        }
    }

    // Filter points matching the charting subcategory
    val rawChartRecords = remember(allMetrics, chartSubcategory) {
        allMetrics.filter { it.subcategory == chartSubcategory }
    }

    // Extract unique regions and years for the visual trend plotting
    val regions = remember(rawChartRecords) {
        rawChartRecords.map { it.region }.distinct().sorted()
    }

    val years = remember(rawChartRecords) {
        rawChartRecords.map { it.year }.distinct().sorted()
    }

    var selectedComparisonYear by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(years) {
        if (years.isNotEmpty() && (selectedComparisonYear == null || selectedComparisonYear !in years)) {
            selectedComparisonYear = years.last()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "TREND CORRELATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Select metric subcategory below to map historical changes:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Horizontal scroll category badges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableSubcats) { subcat ->
                val isSelected = chartSubcategory == subcat
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        chartSubcategory = subcat
                        onHoverPointChange(null) // Reset highlight
                    },
                    label = { Text(subcat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightCardBg),
            border = BorderStroke(1.dp, SoftBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time-Series Dynamics (${years.firstOrNull() ?: 2020} - ${years.lastOrNull() ?: 2024})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                    
                    // Simple interactive metric unit details
                    val samplePoint = rawChartRecords.firstOrNull()
                    if (samplePoint != null) {
                        Text(
                            text = "Unit: ${samplePoint.unit}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (rawChartRecords.isEmpty() || years.size < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Not enough historical metrics to generate an axis trend. Please add custom yearly observations in Explorer.", textAlign = TextAlign.Center, color = Color.Gray)
                    }
                } else {
                    // Draw custom interactive compose line chart
                    InteractiveLineChart(
                        records = rawChartRecords,
                        regions = regions,
                        years = years,
                        onHoverPoint = onHoverPointChange
                    )
                }

                if (hoveredPointInfo != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = hoveredPointInfo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            IconButton(
                                onClick = { onHoverPointChange(null) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear analysis detail highlight",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Comparative Bar Chart Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CROSS-REGIONAL COMPARISON",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Side-by-side performance comparison:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Dropdown selector matching years
            if (years.isNotEmpty()) {
                var expandedYearDropdown by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expandedYearDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Year: ${selectedComparisonYear ?: ""}", fontSize = 11.sp)
                    }
                    DropdownMenu(
                        expanded = expandedYearDropdown,
                        onDismissRequest = { expandedYearDropdown = false }
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = {
                                    selectedComparisonYear = y
                                    expandedYearDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Get subset matching the selected subcategory and selected comparison year
        val comparisonRecords = remember(rawChartRecords, selectedComparisonYear) {
            rawChartRecords.filter { it.year == selectedComparisonYear }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightCardBg),
            border = BorderStroke(1.dp, SoftBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                if (comparisonRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No items populated for year ${selectedComparisonYear ?: "N/A"}.", color = Color.Gray)
                    }
                } else {
                    ComparativeBarGraph(records = comparisonRecords)
                }
            }
        }
    }
}

@Composable
fun InteractiveLineChart(
    records: List<EducationMetric>,
    regions: List<String>,
    years: List<Int>,
    onHoverPoint: (String?) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    // Assign consistent color markers to each region
    val colorPalette = remember(regions) {
        listOf(TrendBlue, ChartIndigo, EmeraldGreen, WarmAmber, Color(0xFFEC4899), Color(0xFF8B5CF6))
    }

    val regionColorMapping = remember(regions, colorPalette) {
        regions.mapIndexed { index, region ->
            region to colorPalette[index % colorPalette.size]
        }.toMap()
    }

    // Determine numerical value extrema for canvas mapping boundingbox
    val minVal = 0.0
    val maxValRaw = remember(records) { records.map { it.value }.maxOrNull() ?: 100.0 }
    val maxVal = maxValRaw * 1.15 // 15% head cushion to avoid graph clipping

    Column {
        // Legend Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            regions.forEach { region ->
                val col = regionColorMapping[region] ?: TrendBlue
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(col, CircleShape)
                    )
                    Text(text = region, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Graphics Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.White)
                // Add click detection mock values for simplicity
                .pointerInput(records, regions, years) {
                    detectTapGestures { offset ->
                        val w = this@pointerInput.size.width.toFloat()
                        val h = this@pointerInput.size.height.toFloat()
                        val leftPadding = 80f
                        val bottomPadding = 60f
                        val rightPadding = 30f
                        val topPadding = 20f

                        val chartWidth = w - leftPadding - rightPadding
                        val chartHeight = h - topPadding - bottomPadding

                        if (years.size >= 2) {
                            val minYear = years.first()
                            val maxYear = years.last()
                            val yearRange = (maxYear - minYear).toFloat()

                            // Iterate point coordinate math to identify the nearest tapped node
                            var closestRecord: EducationMetric? = null
                            var minDistance = 150f // Maximum safe threshold in pixels

                            records.forEach { item ->
                                val regionColor = regionColorMapping[item.region] ?: TrendBlue
                                val x = leftPadding + ((item.year - minYear) / yearRange) * chartWidth
                                val y = topPadding + (chartHeight - ((item.value / maxVal) * chartHeight).toFloat())

                                val distance = Math.hypot((offset.x - x).toDouble(), (offset.y - y).toDouble()).toFloat()
                                if (distance < minDistance) {
                                    minDistance = distance
                                    closestRecord = item
                                }
                            }

                            if (closestRecord != null) {
                                val item = closestRecord!!
                                onHoverPoint("${item.region} (${item.year}): ${String.format("%.2f", item.value)} ${item.unit} — Context: ${item.notes.ifEmpty { "Historical policy observation." }}")
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val leftPadding = 80f
            val bottomPadding = 60f
            val rightPadding = 30f
            val topPadding = 20f

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            // 1. Draw horizontal metric reference grids
            val stepsGrid = 4
            for (i in 0..stepsGrid) {
                val gridVal = (maxVal / stepsGrid) * i
                val y = topPadding + (chartHeight - (i * chartHeight / stepsGrid))
                
                // Grid guidance line
                drawLine(
                    color = SoftBorder.copy(alpha = 0.6f),
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1f
                )

                // Metric scale label string
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.1f", gridVal),
                    10f,
                    y + 8f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                    }
                )
            }

            if (years.size < 2) return@Canvas
            val minYear = years.first()
            val maxYear = years.last()
            val yearRange = (maxYear - minYear).toFloat()

            // 2. Plot X-Axis labels
            years.forEachIndexed { idx, yr ->
                val x = leftPadding + (idx * chartWidth / (years.size - 1))
                drawContext.canvas.nativeCanvas.drawText(
                    yr.toString(),
                    x - 20f,
                    height - 15f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 24f
                        isFakeBoldText = true
                    }
                )
                // Small tic marks
                drawLine(
                    color = Color.LightGray,
                    start = Offset(x, height - bottomPadding),
                    end = Offset(x, height - bottomPadding + 8f),
                    strokeWidth = 2f
                )
            }

            // 3. Draw trend lines for regions
            regions.forEach { reg ->
                val regionColor = regionColorMapping[reg] ?: TrendBlue
                val regRecords = records.filter { it.region == reg }.sortedBy { it.year }
                if (regRecords.isNotEmpty()) {
                    val path = Path()
                    regRecords.forEachIndexed { idx, item ->
                        val x = leftPadding + ((item.year - minYear) / yearRange) * chartWidth
                        val y = topPadding + (chartHeight - ((item.value / maxVal) * chartHeight).toFloat())

                        if (idx == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Stroke region curve
                    drawPath(
                        path = path,
                        color = regionColor,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // Draw metric point bubble anchors
                    regRecords.forEach { item ->
                        val x = leftPadding + ((item.year - minYear) / yearRange) * chartWidth
                        val y = topPadding + (chartHeight - ((item.value / maxVal) * chartHeight).toFloat())

                        // Dot fill
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = Offset(x, y)
                        )
                        // Dot accent ring outline
                        drawCircle(
                            color = regionColor,
                            radius = 8f,
                            center = Offset(x, y),
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
        }
        Text(
            text = "💡 Tap on any point coordinates to inspect specific trend values and policy notes.",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun ComparativeBarGraph(
    records: List<EducationMetric>
) {
    if (records.isEmpty()) return

    val maxVal = remember(records) { records.map { it.value }.maxOrNull() ?: 100.0 } * 1.15
    val regionsCount = records.size

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.White)
        ) {
            val width = size.width
            val height = size.height
            val leftPadding = 80f
            val bottomPadding = 40f
            val rightPadding = 20f
            val topPadding = 15f

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            // Grid markers
            val gridLinesCount = 3
            for (i in 0..gridLinesCount) {
                val gridVal = (maxVal / gridLinesCount) * i
                val y = topPadding + (chartHeight - (i * chartHeight / gridLinesCount))
                drawLine(
                    color = SoftBorder.copy(alpha = 0.5f),
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1f
                )
                // Label values
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.1f", gridVal),
                    10f,
                    y + 8f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 22f
                    }
                )
            }

            // Draw rounded vertical columns
            val spacingPercentage = 0.35f
            val availableGroupWidth = chartWidth / regionsCount
            val barWidth = availableGroupWidth * (1f - spacingPercentage)

            records.forEachIndexed { idx, item ->
                val xLeft = leftPadding + (idx * availableGroupWidth) + (availableGroupWidth * spacingPercentage / 2)
                val barHeightVal = ((item.value / maxVal) * chartHeight).toFloat()
                val yTop = topPadding + (chartHeight - barHeightVal)

                // Alternate bar colors dynamically
                val fillBrush = Brush.linearGradient(
                    colors = when (idx % 3) {
                        0 -> listOf(TrendBlue, ChartIndigo)
                        1 -> listOf(EmeraldGreen, Color(0xFF34D399))
                        else -> listOf(WarmAmber, Color(0xFFFBBF24))
                    }
                )

                // Fill column
                drawRoundRect(
                    brush = fillBrush,
                    topLeft = Offset(xLeft, yTop),
                    size = Size(barWidth, barHeightVal),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Plot metrics numbers directly on top of bar column boundaries
                val textSpec = String.format("%.1f", item.value)
                drawContext.canvas.nativeCanvas.drawText(
                    textSpec,
                    xLeft + (barWidth / 2) - (textSpec.length * 6),
                    if (yTop - 10f < 15f) 20f else yTop - 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 22f
                        isFakeBoldText = true
                    }
                )

                // Render abbreviation region descriptors inside columns or under axis
                val abbreviation = if (item.region.length > 10) item.region.take(8) + ".." else item.region
                drawContext.canvas.nativeCanvas.drawText(
                    abbreviation,
                    xLeft + (barWidth / 2) - (abbreviation.length * 5),
                    height - 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 20f
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}


// ---------------------- DATA EXPLORER TAB CONTENT ----------------------
@Composable
fun ExplorerTabContent(
    metrics: List<EducationMetric>,
    searchQuery: String,
    categories: List<String>,
    selectedCategory: String,
    subcategories: List<String>,
    selectedSubcategory: String,
    years: List<String>,
    selectedYear: String,
    bookmarks: List<EducationMetric>,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onSubcatSelect: (String) -> Unit,
    onYearSelect: (String) -> Unit,
    onBookmarkToggle: (Int) -> Unit,
    onDeleteMetric: (Int) -> Unit
) {
    var filterExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Filter header bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search school, region, topic...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = SoftBorder
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("explorer_search_bar")
            )

            // Filter button trigger
            Button(
                onClick = { filterExpanded = !filterExpanded },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (filterExpanded) MaterialTheme.colorScheme.primaryContainer else LightCardBg,
                    contentColor = if (filterExpanded) MaterialTheme.colorScheme.onPrimaryContainer else DarkSlate
                ),
                border = BorderStroke(1.dp, SoftBorder),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("explorer_filter_toggle")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Toggle filtering option panels", modifier = Modifier.size(16.dp))
                    Text("Filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Expanded Filtering Panels
        AnimatedVisibility(
            visible = filterExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightCardBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SoftBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categorized Segments", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        TextButton(
                            onClick = {
                                onCategorySelect("All")
                                onSubcatSelect("All")
                                onYearSelect("All")
                            }
                        ) {
                            Text("Clear Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 1. Category selector
                    Text("Global Domain Limit:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    FilterBadgeSelector(
                        items = categories,
                        selectedItem = selectedCategory,
                        onSelect = onCategorySelect
                    )

                    // 2. Subcategory selector
                    Text("Detail Key Metrics:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    FilterBadgeSelector(
                        items = subcategories,
                        selectedItem = selectedSubcategory,
                        onSelect = onSubcatSelect
                    )

                    // 3. Year selector
                    Text("Reporting Year:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    FilterBadgeSelector(
                        items = years,
                        selectedItem = selectedYear,
                        onSelect = onYearSelect
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid counter details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "METRIC DATA RECORDS (${metrics.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            if (bookmarks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(WarmAmber.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = WarmAmber)
                        Text("${bookmarks.size} Bookmarked Comparison Set", fontSize = 10.sp, color = DarkSlate, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // List Grid Layout
        if (metrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Text("No records match current query filtering parameters.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(metrics, key = { it.id }) { item ->
                    MetricCardItem(
                        item = item,
                        onBookmarkToggle = { onBookmarkToggle(item.id) },
                        onDelete = { onDeleteMetric(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterBadgeSelector(
    items: List<String>,
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            val isSelected = item == selectedItem
            Surface(
                onClick = { onSelect(item) },
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else SoftBorder),
                modifier = Modifier.testTag("filter_chip_${item.lowercase().replace(" ", "_").replace("(", "").replace(")", "").replace("%", "")}")
            ) {
                Text(
                    text = item,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else DarkSlate,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun MetricCardItem(
    item: EducationMetric,
    onBookmarkToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedNotes by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBg),
        border = BorderStroke(1.dp, SoftBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("metric_record_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon indicators
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (item.category) {
                                "Global Indicators" -> TrendBlue.copy(alpha = 0.15f)
                                "Institutional Performance" -> ChartIndigo.copy(alpha = 0.15f)
                                else -> EmeraldGreen.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.category) {
                            "Global Indicators" -> Icons.Default.Home
                            "Institutional Performance" -> Icons.Default.Star
                            else -> Icons.Default.Settings
                        },
                        contentDescription = null,
                        tint = when (item.category) {
                            "Global Indicators" -> TrendBlue
                            "Institutional Performance" -> ChartIndigo
                            else -> EmeraldGreen
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Metadata Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.region,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.subcategory,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(modifier = Modifier.size(3.dp).background(Color.Gray, CircleShape))
                        Text(
                            text = "Yr ${item.year}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Numeric metrics result
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = String.format("%.1f", item.value),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                    Text(
                        text = item.unit,
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Star bookmark toggle button
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.testTag("bookmark_toggle_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isBookmarked) Icons.Default.Star else Icons.Default.Star,
                        contentDescription = "Toggle analytic bookmark comparison",
                        tint = if (item.isBookmarked) WarmAmber else Color.LightGray.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Custom deletion options
                if (item.isCustom) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_record_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove custom record row bounds",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expandable notes section drawer
            if (item.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { expandedNotes = !expandedNotes }
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if (expandedNotes) "Hide policy context" else "View policy context...",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expandedNotes) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                AnimatedVisibility(visible = expandedNotes) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, SoftBorder, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = item.notes,
                            fontSize = 10.sp,
                            color = DarkSlate,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}


// ---------------------- AI ADVISOR TAB CONTENT ----------------------
@Composable
fun AiAdvisorTabContent(
    aiResult: String?,
    aiLoading: Boolean,
    onSubmitQuestion: (String) -> Unit,
    onClearResult: () -> Unit
) {
    var questionInput by remember { mutableStateOf("") }

    val suggestedQuestions = listOf(
        "Analyze general subcategory trends in low spending regions.",
        "Provide direct guidelines on lowering Pupil-Teacher ratio.",
        "What are strategic policy steps for Metropolis high school?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // AI Header card banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = "AI Educational Strategic Lab",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Synthesizer backed by Gemini 3.5 Flash",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Request macro-policy action guidelines, institutional strategic advice, or correlations directly derived from your active statistics database filter boundaries.",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CHOOSE SYSTEM SUGGESTIONS:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Micro recommendation chips scrolling row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            suggestedQuestions.forEach { prompt ->
                Surface(
                    onClick = {
                        questionInput = prompt
                        onSubmitQuestion(prompt)
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = LightCardBg,
                    border = BorderStroke(1.dp, SoftBorder),
                    modifier = Modifier.testTag("ai_suggestion_chip_${prompt.take(15).lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = prompt,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom search query field box
        OutlinedTextField(
            value = questionInput,
            onValueChange = { questionInput = it },
            placeholder = { Text("Ask. e.g. How does funding impact high school grad rates?", fontSize = 13.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = SoftBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .testTag("ai_question_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (questionInput.isNotBlank()) {
                    onSubmitQuestion(questionInput)
                }
            },
            enabled = !aiLoading && questionInput.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("ai_submit_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (aiLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Text(if (aiLoading) "Synthesizing Core Data..." else "Synthesize Active Metrics", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Response details
        AnimatedVisibility(
            visible = aiLoading || aiResult != null,
            enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightCardBg),
                border = BorderStroke(1.dp, SoftBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
                    .testTag("ai_result_container")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Core Analytics Guidelines Output",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (aiResult != null) {
                            IconButton(
                                onClick = {
                                    onClearResult()
                                    questionInput = ""
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Divider(color = SoftBorder, modifier = Modifier.padding(vertical = 10.dp))

                    if (aiLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text("AI is extracting statistics from your custom metrics snapshots...", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else if (aiResult != null) {
                        Text(
                            text = aiResult!!,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = DarkSlate,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚠ Pre-evaluated statistics simulation for educational diagnostic research purposes.",
                            fontSize = 9.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


// ---------------------- RECORD ENTRY DIALOG PANEL ----------------------
@Composable
fun AddMetricRecordDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (category: String, subcat: String, region: String, year: Int, value: Double, unit: String, notes: String) -> Unit
) {
    var categoryInput by remember { mutableStateOf(categories.firstOrNull() ?: "Global Indicators") }
    var subcatInput by remember { mutableStateOf("") }
    var regionInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("2025") }
    var valueInput by remember { mutableStateOf("") }
    var unitInput by remember { mutableStateOf("%") }
    var notesInput by remember { mutableStateOf("") }

    var errorPrompt by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SoftBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .testTag("add_record_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add Custom Observational Metric",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate
                )
                Divider(color = SoftBorder)

                // Domain Selection
                Text("Domain Category", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = categoryInput == cat,
                            onClick = { categoryInput = cat },
                            label = { Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Subcategory
                OutlinedTextField(
                    value = subcatInput,
                    onValueChange = { subcatInput = it },
                    label = { Text("Subcategory key. (e.g. Literacy %)", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subcategory")
                )

                // Region / Institutional High School ID
                OutlinedTextField(
                    value = regionInput,
                    onValueChange = { regionInput = it },
                    label = { Text("Target School District or Region (e.g. Area High)", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_region")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Reporting Year
                    OutlinedTextField(
                        value = yearInput,
                        onValueChange = { yearInput = it },
                        label = { Text("Year (e.g. 2025)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_year")
                    )

                    // Numeric outcome value
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("Value (e.g. 94.5)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_value")
                    )
                }

                // Unit selection (e.g. %, score, USD)
                OutlinedTextField(
                    value = unitInput,
                    onValueChange = { unitInput = it },
                    label = { Text("Measurement Unit (e.g. %, ratios, score, USD)", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_unit")
                )

                // Optional policy notations
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Policy contextual notations (Optional)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("input_notes")
                )

                if (errorPrompt != null) {
                    Text(
                        text = errorPrompt!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            val doubleVal = valueInput.toDoubleOrNull()
                            val intYear = yearInput.toIntOrNull()

                            if (subcatInput.isBlank() || regionInput.isBlank()) {
                                errorPrompt = "Please fill in all descriptive text fields correctly."
                            } else if (intYear == null || intYear < 1900 || intYear > 2100) {
                                errorPrompt = "Please set a valid year scope between 1900-2100."
                            } else if (doubleVal == null) {
                                errorPrompt = "Please fill in a valid numeric outcome data point."
                            } else {
                                onSave(categoryInput, subcatInput.trim(), regionInput.trim(), intYear, doubleVal, unitInput.trim(), notesInput.trim())
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_custom_record_button")
                    ) {
                        Text("Save Record", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
