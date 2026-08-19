package com.chlqudco.seoulcrowdinglevelmap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chlqudco.seoulcrowdinglevelmap.data.CrowdRepository
import com.chlqudco.seoulcrowdinglevelmap.data.PlaceCatalog
import com.chlqudco.seoulcrowdinglevelmap.model.Place
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceCategory
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceConfig
import com.chlqudco.seoulcrowdinglevelmap.model.pageCount
import com.chlqudco.seoulcrowdinglevelmap.model.pageSlice
import com.chlqudco.seoulcrowdinglevelmap.model.rankPlaces
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MainTab(val label: String) {
    HOME("홈"),
    MAP("지도"),
    FAVORITES("즐겨찾기"),
    SETTINGS("설정")
}

data class RefreshUiState(
    val isRefreshing: Boolean = false,
    val message: String? = null
)

private data class UserPreferences(
    val favorites: Set<String> = emptySet(),
    val autoRefresh: Boolean = true,
    val refreshInterval: Int = 10
)

private data class BrowseState(
    val selectedCategory: PlaceCategory = PlaceCategory.ALL,
    val searchQuery: String = "",
    val currentPage: Int = 1
)

private data class ScreenControls(
    val selectedTab: MainTab = MainTab.HOME,
    val browse: BrowseState = BrowseState(),
    val selectedAreaCode: String? = null,
    val refresh: RefreshUiState = RefreshUiState(),
    val now: Long = System.currentTimeMillis()
)

data class CrowdUiState(
    val allPlaces: List<Place> = emptyList(),
    val visiblePlaces: List<Place> = emptyList(),
    val mapPlaces: List<Place> = emptyList(),
    val topPlaces: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val selectedTab: MainTab = MainTab.HOME,
    val selectedCategory: PlaceCategory = PlaceCategory.ALL,
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val totalPages: Int = 0,
    val filteredCount: Int = 0,
    val pageStart: Int = 0,
    val pageEnd: Int = 0,
    val autoRefresh: Boolean = true,
    val refreshInterval: Int = 10,
    val isRefreshing: Boolean = false,
    val message: String? = null,
    val isDemoMode: Boolean = true,
    val now: Long = System.currentTimeMillis()
) {
    val lastRefreshAt: Long?
        get() = visiblePlaces.maxOfOrNull { it.snapshot.fetchedAt }
            ?: allPlaces.maxOfOrNull { it.snapshot.fetchedAt }
}

class CrowdViewModel(private val repository: CrowdRepository) : ViewModel() {
    private val selectedTab = MutableStateFlow(MainTab.HOME)
    private val browseState = MutableStateFlow(BrowseState())
    private val selectedAreaCode = MutableStateFlow<String?>(null)
    private val refreshState = MutableStateFlow(RefreshUiState())
    private val clock = MutableStateFlow(System.currentTimeMillis())
    private var refreshJob: Job? = null
    private var searchRefreshJob: Job? = null

    private val preferences = combine(
        repository.favorites,
        repository.autoRefresh,
        repository.refreshInterval
    ) { favorites, autoRefresh, refreshInterval ->
        UserPreferences(favorites, autoRefresh, refreshInterval)
    }

    private val controls = combine(
        selectedTab,
        browseState,
        selectedAreaCode,
        refreshState,
        clock
    ) { tab, browse, areaCode, refresh, now ->
        ScreenControls(tab, browse, areaCode, refresh, now)
    }

    val uiState = combine(repository.snapshots, preferences, controls) { snapshots, preferences, controls ->
        val snapshotsByCode = snapshots.associateBy { it.areaCode }
        val catalogPlaces = PlaceCatalog.places.mapNotNull { config ->
            snapshotsByCode[config.areaCode]?.let { snapshot ->
                Place(config, snapshot, config.areaCode in preferences.favorites)
            }
        }
        val ranked = rankPlaces(catalogPlaces, controls.now, preferences.refreshInterval)
        val filtered = catalogPlaces.filter { it.config.matches(controls.browse) }
        val totalPages = pageCount(filtered.size, PAGE_SIZE)
        val currentPage = if (totalPages == 0) 1 else controls.browse.currentPage.coerceIn(1, totalPages)
        val currentPagePlaces = pageSlice(filtered, currentPage, PAGE_SIZE)
        val rankedFiltered = rankPlaces(filtered, controls.now, preferences.refreshInterval)
        val liveRanked = rankedFiltered.filterNot { it.snapshot.isDemo }
        val topPlaces = if (!repository.isDemoMode && liveRanked.size >= TOP_PLACE_COUNT) {
            liveRanked.take(TOP_PLACE_COUNT)
        } else {
            rankedFiltered.take(TOP_PLACE_COUNT)
        }
        val visible = when (controls.selectedTab) {
            MainTab.FAVORITES -> ranked.filter { it.isFavorite }
            else -> currentPagePlaces
        }
        val pageStart = if (filtered.isEmpty()) 0 else (currentPage - 1) * PAGE_SIZE + 1
        val pageEnd = minOf(currentPage * PAGE_SIZE, filtered.size)
        CrowdUiState(
            allPlaces = catalogPlaces,
            visiblePlaces = visible,
            mapPlaces = filtered,
            topPlaces = topPlaces,
            selectedPlace = catalogPlaces.firstOrNull { it.config.areaCode == controls.selectedAreaCode },
            selectedTab = controls.selectedTab,
            selectedCategory = controls.browse.selectedCategory,
            searchQuery = controls.browse.searchQuery,
            currentPage = currentPage,
            totalPages = totalPages,
            filteredCount = filtered.size,
            pageStart = pageStart,
            pageEnd = pageEnd,
            autoRefresh = preferences.autoRefresh,
            refreshInterval = preferences.refreshInterval,
            isRefreshing = controls.refresh.isRefreshing,
            message = controls.refresh.message,
            isDemoMode = repository.isDemoMode,
            now = controls.now
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CrowdUiState(isDemoMode = repository.isDemoMode)
    )

    init {
        viewModelScope.launch {
            repository.initialize()
            if (repository.autoRefresh.first()) {
                refresh(force = false, showProgress = false)
            }
        }
        viewModelScope.launch {
            while (isActive) {
                delay(60_000L)
                clock.value = System.currentTimeMillis()
            }
        }
        viewModelScope.launch {
            combine(repository.autoRefresh, repository.refreshInterval) { enabled, minutes -> enabled to minutes }
                .collectLatest { (enabled, minutes) ->
                    while (enabled && isActive) {
                        delay(minutes * 60_000L)
                        refresh(force = false, showProgress = false)
                    }
                }
        }
    }

    fun selectTab(tab: MainTab) {
        selectedTab.value = tab
        selectedAreaCode.value = null
        if (tab == MainTab.MAP) refresh(force = false, showProgress = false, areaCodes = mapAreaCodes())
    }

    fun selectCategory(category: PlaceCategory) {
        searchRefreshJob?.cancel()
        browseState.value = browseState.value.copy(selectedCategory = category, currentPage = 1)
        refresh(force = false, showProgress = false)
    }

    fun updateSearchQuery(query: String) {
        browseState.value = browseState.value.copy(searchQuery = query, currentPage = 1)
        searchRefreshJob?.cancel()
        searchRefreshJob = viewModelScope.launch {
            delay(SEARCH_REFRESH_DELAY)
            refresh(force = false, showProgress = false)
        }
    }

    fun selectPage(page: Int) {
        if (page == browseState.value.currentPage || page < 1) return
        searchRefreshJob?.cancel()
        browseState.value = browseState.value.copy(currentPage = page)
        refresh(force = false, showProgress = false)
    }

    fun openDetail(areaCode: String) {
        selectedAreaCode.value = areaCode
    }

    fun closeDetail() {
        selectedAreaCode.value = null
    }

    fun toggleFavorite(areaCode: String) {
        viewModelScope.launch { repository.toggleFavorite(areaCode) }
    }

    fun refreshCurrentPage() {
        refresh(force = true, showProgress = true)
    }

    fun refreshMapPlaces() {
        refresh(force = true, showProgress = true, areaCodes = mapAreaCodes())
    }

    fun refreshPlace(areaCode: String) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            refreshState.value = RefreshUiState(isRefreshing = true)
            val result = repository.refreshPlace(areaCode)
            showResult(result.message)
        }
    }

    fun setAutoRefresh(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoRefresh(enabled) }
    }

    fun setRefreshInterval(minutes: Int) {
        viewModelScope.launch { repository.setRefreshInterval(minutes) }
    }

    private fun refresh(
        force: Boolean,
        showProgress: Boolean,
        areaCodes: List<String> = currentPageAreaCodes()
    ) {
        if (areaCodes.isEmpty()) return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            if (showProgress) refreshState.value = RefreshUiState(isRefreshing = true)
            val interval = repository.refreshInterval.first()
            val result = repository.refreshPlaces(areaCodes, interval, force)
            if (showProgress || result.failed > 0) showResult(result.message)
        }
    }

    private fun currentPageAreaCodes(): List<String> {
        val browse = browseState.value
        val filtered = PlaceCatalog.places.filter { it.matches(browse) }
        return pageSlice(filtered, browse.currentPage, PAGE_SIZE).map { it.areaCode }
    }

    private fun mapAreaCodes(): List<String> {
        val browse = browseState.value
        val filtered = PlaceCatalog.places.filter { it.matches(browse) }
        return pageSlice(filtered, browse.currentPage, PAGE_SIZE).map { it.areaCode }
    }

    private fun PlaceConfig.matches(browse: BrowseState): Boolean {
        if (browse.selectedCategory != PlaceCategory.ALL && category != browse.selectedCategory) return false
        val query = browse.searchQuery.trim()
        return query.isBlank() ||
            areaName.contains(query, ignoreCase = true) ||
            englishName.contains(query, ignoreCase = true) ||
            areaCode.contains(query, ignoreCase = true) ||
            category.label.contains(query, ignoreCase = true)
    }

    private suspend fun showResult(message: String) {
        refreshState.value = RefreshUiState(message = message)
        delay(3_500L)
        if (refreshState.value.message == message) refreshState.value = RefreshUiState()
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val TOP_PLACE_COUNT = 5
        const val SEARCH_REFRESH_DELAY = 600L
    }
}

class CrowdViewModelFactory(
    private val repository: CrowdRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CrowdViewModel(repository) as T
    }
}
