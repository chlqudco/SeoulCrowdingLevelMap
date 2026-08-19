package com.chlqudco.seoulcrowdinglevelmap.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CrowdRadarApp(viewModel: CrowdViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { snackbarHostState.showSnackbar(it) }
    }
    BackHandler(enabled = state.selectedPlace != null) {
        viewModel.closeDetail()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val selectedPlace = state.selectedPlace
        if (selectedPlace != null) {
            PlaceDetailScreen(
                place = selectedPlace,
                now = state.now,
                ttlMinutes = state.refreshInterval,
                isRefreshing = state.isRefreshing,
                isDemoMode = state.isDemoMode,
                onBack = viewModel::closeDetail,
                onFavorite = { viewModel.toggleFavorite(selectedPlace.config.areaCode) },
                onRefresh = { viewModel.refreshPlace(selectedPlace.config.areaCode) }
            )
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                        MainTab.entries.forEach { tab ->
                            val icon = when (tab) {
                                MainTab.HOME -> Icons.Rounded.Home
                                MainTab.FAVORITES -> Icons.Rounded.Favorite
                                MainTab.SETTINGS -> Icons.Rounded.Settings
                            }
                            NavigationBarItem(
                                selected = state.selectedTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { Icon(icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when (state.selectedTab) {
                    MainTab.HOME -> HomeScreen(
                        state = state,
                        onCategorySelected = viewModel::selectCategory,
                        onSearchQueryChanged = viewModel::updateSearchQuery,
                        onPageSelected = viewModel::selectPage,
                        onPlaceClick = viewModel::openDetail,
                        onFavorite = viewModel::toggleFavorite,
                        onRefresh = viewModel::refreshCurrentPage,
                        modifier = Modifier.padding(innerPadding)
                    )
                    MainTab.FAVORITES -> FavoritesScreen(
                        state = state,
                        onPlaceClick = viewModel::openDetail,
                        onFavorite = viewModel::toggleFavorite,
                        modifier = Modifier.padding(innerPadding)
                    )
                    MainTab.SETTINGS -> SettingsScreen(
                        state = state,
                        onAutoRefreshChanged = viewModel::setAutoRefresh,
                        onRefreshIntervalChanged = viewModel::setRefreshInterval,
                        onRefresh = viewModel::refreshCurrentPage,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = if (selectedPlace == null) 92.dp else 16.dp)
        )
    }
}
