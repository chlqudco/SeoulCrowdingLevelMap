package com.chlqudco.seoulcrowdinglevelmap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chlqudco.seoulcrowdinglevelmap.data.CrowdRepository
import com.chlqudco.seoulcrowdinglevelmap.model.Place
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceCategory
import com.chlqudco.seoulcrowdinglevelmap.model.rankPlaces
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
    FAVORITES("즐겨찾기"),
    SETTINGS("설정")
}

data class RefreshUiState(
    val isRefreshing: Boolean = false,
    val message: String? = null
)

data class UserPreferences(
    val favorites: Set<String> = emptySet(),
    val autoRefresh: Boolean = true,
    val refreshInterval: Int = 10
)

data class ScreenControls(
    val selectedTab: MainTab = MainTab.HOME,
    val selectedCategory: PlaceCategory = PlaceCategory.ALL,
    val selectedAreaCode: String? = null,
    val refresh: RefreshUiState = RefreshUiState(),
    val now: Long = System.currentTimeMillis()
)

data class CrowdUiState(
    val allPlaces: List<Place> = emptyList(),
    val visiblePlaces: List<Place> = emptyList(),
    val topPlaces: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val selectedTab: MainTab = MainTab.HOME,
    val selectedCategory: PlaceCategory = PlaceCategory.ALL,
    val autoRefresh: Boolean = true,
    val refreshInterval: Int = 10,
    val isRefreshing: Boolean = false,
    val message: String? = null,
    val isDemoMode: Boolean = true,
    val now: Long = System.currentTimeMillis()
) {
    val lastRefreshAt: Long?
        get() = allPlaces.maxOfOrNull { it.snapshot.fetchedAt }
}

class CrowdViewModel(private val repository: CrowdRepository) : ViewModel() {
    private val selectedTab = MutableStateFlow(MainTab.HOME)
    private val selectedCategory = MutableStateFlow(PlaceCategory.ALL)
    private val selectedAreaCode = MutableStateFlow<String?>(null)
    private val refreshState = MutableStateFlow(RefreshUiState())
    private val clock = MutableStateFlow(System.currentTimeMillis())

    private val preferences = combine(
        repository.favorites,
        repository.autoRefresh,
        repository.refreshInterval
    ) { favorites, autoRefresh, refreshInterval ->
        UserPreferences(favorites, autoRefresh, refreshInterval)
    }

    private val controls = combine(
        selectedTab,
        selectedCategory,
        selectedAreaCode,
        refreshState,
        clock
    ) { tab, category, areaCode, refresh, now ->
        ScreenControls(tab, category, areaCode, refresh, now)
    }

    val uiState = combine(repository.snapshots, preferences, controls) { snapshots, preferences, controls ->
        val snapshotsByCode = snapshots.associateBy { it.areaCode }
        val allPlaces = com.chlqudco.seoulcrowdinglevelmap.data.PlaceCatalog.places.mapNotNull { config ->
            snapshotsByCode[config.areaCode]?.let { snapshot ->
                Place(config, snapshot, config.areaCode in preferences.favorites)
            }
        }
        val ranked = rankPlaces(allPlaces, controls.now, preferences.refreshInterval)
        val categoryPlaces = ranked.filter {
            controls.selectedCategory == PlaceCategory.ALL || it.config.category == controls.selectedCategory
        }
        val visible = if (controls.selectedTab == MainTab.FAVORITES) {
            ranked.filter { it.isFavorite }
        } else {
            categoryPlaces
        }
        CrowdUiState(
            allPlaces = ranked,
            visiblePlaces = visible,
            topPlaces = categoryPlaces.take(5),
            selectedPlace = ranked.firstOrNull { it.config.areaCode == controls.selectedAreaCode },
            selectedTab = controls.selectedTab,
            selectedCategory = controls.selectedCategory,
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
    }

    fun selectCategory(category: PlaceCategory) {
        selectedCategory.value = category
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

    fun refreshAll() {
        refresh(force = true, showProgress = true)
    }

    fun refreshPlace(areaCode: String) {
        if (refreshState.value.isRefreshing) return
        viewModelScope.launch {
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

    private fun refresh(force: Boolean, showProgress: Boolean) {
        if (refreshState.value.isRefreshing) return
        viewModelScope.launch {
            if (showProgress) refreshState.value = RefreshUiState(isRefreshing = true)
            val interval = repository.refreshInterval.first()
            val result = repository.refreshAll(interval, force)
            if (showProgress || result.failed > 0) showResult(result.message)
        }
    }

    private suspend fun showResult(message: String) {
        refreshState.value = RefreshUiState(message = message)
        delay(3_500L)
        if (refreshState.value.message == message) refreshState.value = RefreshUiState()
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
