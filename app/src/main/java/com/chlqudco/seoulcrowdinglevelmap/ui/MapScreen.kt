package com.chlqudco.seoulcrowdinglevelmap.ui

import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.chlqudco.seoulcrowdinglevelmap.BuildConfig
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.Place
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceCategory
import com.chlqudco.seoulcrowdinglevelmap.model.isStale
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import kotlin.math.cos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrowdMapScreen(
    state: CrowdUiState,
    onCategorySelected: (PlaceCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPageSelected: (Int) -> Unit,
    onPlaceClick: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAreaCode by rememberSaveable { mutableStateOf<String?>(null) }
    var mapAuthErrorCode by rememberSaveable { mutableStateOf<String?>(null) }
    var authListenerReady by remember { mutableStateOf(false) }
    val selectedPlace = state.mapPlaces.firstOrNull { it.config.areaCode == selectedAreaCode }

    BackHandler(enabled = selectedAreaCode != null && state.selectedPlace == null) {
        selectedAreaCode = null
    }

    LaunchedEffect(state.mapPlaces.map { it.config.areaCode }) {
        if (selectedAreaCode != null && selectedPlace == null) selectedAreaCode = null
    }

    DisposableEffect(context) {
        val sdk = NaverMapSdk.getInstance(context)
        val previousListener = sdk.onAuthFailedListener
        sdk.onAuthFailedListener = NaverMapSdk.OnAuthFailedListener { exception ->
            mapAuthErrorCode = exception.errorCode
        }
        authListenerReady = true
        onDispose {
            sdk.onAuthFailedListener = previousListener
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (BuildConfig.NAVER_MAP_KEY_ID.isBlank()) {
            MissingMapKeyState(Modifier.fillMaxSize())
        } else if (authListenerReady) {
            NaverCrowdMap(
                places = state.mapPlaces,
                selectedAreaCode = selectedAreaCode,
                now = state.now,
                ttlMinutes = state.refreshInterval,
                onMarkerSelected = { selectedAreaCode = it },
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp)
        ) {
            MapControlPanel(
                state = state,
                onCategorySelected = onCategorySelected,
                onSearchQueryChanged = onSearchQueryChanged,
                onPageSelected = onPageSelected,
                onRefresh = onRefresh
            )
            if (mapAuthErrorCode != null) {
                Spacer(Modifier.height(8.dp))
                MapAuthErrorBanner(
                    errorCode = mapAuthErrorCode.orEmpty(),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        MapLegend(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

        if (selectedPlace != null) {
            SelectedMapPlaceCard(
                place = selectedPlace,
                now = state.now,
                onClose = { selectedAreaCode = null },
                onDetail = { onPlaceClick(selectedPlace.config.areaCode) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 70.dp)
            )
        }
    }
}

@Composable
private fun MapAuthErrorBanner(errorCode: String, modifier: Modifier = Modifier) {
    val message = when (errorCode) {
        "401" -> "네이버 클라우드 Maps의 Android 앱 패키지 등록값을 ${BuildConfig.APPLICATION_ID}로 설정해 주세요."
        "429" -> "네이버 클라우드 Maps에서 Dynamic Map 선택과 사용량을 확인해 주세요."
        "800" -> "NAVER_MAP_KEY_ID가 지도 SDK에 전달되지 않았어요."
        else -> "네이버 지도 인증에 실패했어요. 네이버 클라우드 설정을 확인해 주세요."
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Text(
            "$message · 오류 $errorCode",
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapControlPanel(
    state: CrowdUiState,
    onCategorySelected: (PlaceCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPageSelected: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(9.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("혼잡도 지도", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "조건에 맞는 장소 ${state.mapPlaces.size}곳 · 지도에 전체 표시",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                IconButton(onClick = onRefresh, enabled = !state.isRefreshing) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(Modifier.size(21.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = "지도 데이터 새로고침")
                    }
                }
            }
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                placeholder = { Text("장소명, 영문명, 장소 코드 검색") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = if (state.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "검색어 지우기")
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items(PlaceCategory.entries, key = { it.name }) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            if (state.totalPages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { onPageSelected(state.currentPage - 1) },
                        enabled = state.currentPage > 1 && !state.isRefreshing
                    ) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "이전 갱신 범위")
                    }
                    Text(
                        "API 갱신 ${state.pageStart}-${state.pageEnd} / ${state.filteredCount} · ${state.currentPage}/${state.totalPages}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = { onPageSelected(state.currentPage + 1) },
                        enabled = state.currentPage < state.totalPages && !state.isRefreshing
                    ) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "다음 갱신 범위")
                    }
                }
            }
        }
    }
}

@Composable
private fun NaverCrowdMap(
    places: List<Place>,
    selectedAreaCode: String?,
    now: Long,
    ttlMinutes: Int,
    onMarkerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapView = rememberNaverMapView()
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    val markers = remember { mutableMapOf<String, Marker>() }
    val density = LocalDensity.current
    val markerWidth = with(density) { 34.dp.roundToPx() }
    val markerHeight = with(density) { 43.dp.roundToPx() }
    val selectedMarkerWidth = with(density) { 43.dp.roundToPx() }
    val selectedMarkerHeight = with(density) { 54.dp.roundToPx() }
    val placeCodes = places.map { it.config.areaCode }

    DisposableEffect(mapView) {
        var active = true
        mapView.getMapAsync { map ->
            if (active) naverMap = map
        }
        onDispose {
            active = false
            naverMap = null
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)

    LaunchedEffect(naverMap) {
        naverMap?.let { map ->
            map.uiSettings.isZoomControlEnabled = false
            map.uiSettings.isLocationButtonEnabled = false
            map.minZoom = 8.0
            map.maxZoom = 18.0
            map.moveCamera(CameraUpdate.scrollAndZoomTo(SEOUL_CENTER, 10.2))
        }
    }

    LaunchedEffect(naverMap, placeCodes) {
        val map = naverMap ?: return@LaunchedEffect
        if (places.isEmpty()) return@LaunchedEffect
        val coordinates = places.mapNotNull { place ->
            val latitude = place.config.latitude
            val longitude = place.config.longitude
            if (latitude == null || longitude == null) null else LatLng(latitude, longitude)
        }
        if (coordinates.isNotEmpty()) {
            val center = LatLng(
                coordinates.map { it.latitude }.average(),
                coordinates.map { it.longitude }.average()
            )
            val latitudeSpan = coordinates.maxOf { it.latitude } - coordinates.minOf { it.latitude }
            val longitudeSpan = (coordinates.maxOf { it.longitude } - coordinates.minOf { it.longitude }) *
                cos(Math.toRadians(center.latitude))
            val zoom = mapZoom(maxOf(latitudeSpan, longitudeSpan), coordinates.size)
            map.moveCamera(
                CameraUpdate.scrollAndZoomTo(center, zoom)
                    .animate(CameraAnimation.Easing, 500)
            )
        }
    }

    LaunchedEffect(naverMap, places, selectedAreaCode, now, ttlMinutes) {
        val map = naverMap ?: return@LaunchedEffect
        val activeCodes = places.mapTo(mutableSetOf()) { it.config.areaCode }
        markers.keys.filterNot { it in activeCodes }.forEach { code ->
            markers.remove(code)?.map = null
        }
        places.forEach { place ->
            val latitude = place.config.latitude ?: return@forEach
            val longitude = place.config.longitude ?: return@forEach
            val selected = place.config.areaCode == selectedAreaCode
            val marker = markers.getOrPut(place.config.areaCode) { Marker() }
            marker.position = LatLng(latitude, longitude)
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = place.snapshot.level.statusColor().toArgb()
            marker.width = if (selected) selectedMarkerWidth else markerWidth
            marker.height = if (selected) selectedMarkerHeight else markerHeight
            marker.alpha = if (place.isStale(now, ttlMinutes)) 0.68f else 1f
            marker.captionText = if (selected || places.size <= 25) place.config.areaName else ""
            marker.subCaptionText = if (selected) place.snapshot.level.label else ""
            marker.captionTextSize = 12f
            marker.subCaptionTextSize = 10f
            marker.captionColor = AndroidColor.rgb(25, 32, 30)
            marker.captionHaloColor = AndroidColor.WHITE
            marker.subCaptionColor = place.snapshot.level.statusColor().toArgb()
            marker.subCaptionHaloColor = AndroidColor.WHITE
            marker.zIndex = if (selected) 1_000 else place.snapshot.level.rank
            marker.isHideCollidedMarkers = !selected
            marker.isHideCollidedCaptions = !selected
            marker.isForceShowIcon = selected
            marker.setOnClickListener {
                onMarkerSelected(place.config.areaCode)
                map.moveCamera(
                    CameraUpdate.scrollAndZoomTo(marker.position, maxOf(map.cameraPosition.zoom, 12.0))
                        .animate(CameraAnimation.Easing, 350)
                )
                true
            }
            marker.map = map
        }
    }

    DisposableEffect(naverMap) {
        onDispose {
            markers.values.forEach { it.map = null }
            markers.clear()
        }
    }
}

@Composable
private fun SelectedMapPlaceCard(
    place: Place,
    now: Long,
    onClose: () -> Unit,
    onDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(11.dp)
                        .background(place.snapshot.level.statusColor(), CircleShape)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    place.snapshot.level.label,
                    color = place.snapshot.level.statusColor(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    relativeTime(place.snapshot.sourceUpdatedAt, now),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "선택 해제", modifier = Modifier.size(19.dp))
                }
            }
            Text(place.config.areaName, style = MaterialTheme.typography.titleLarge)
            Text(
                "${place.config.category.label} · ${place.snapshot.populationRange()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onDetail, modifier = Modifier.fillMaxWidth()) {
                Text("상세 정보 보기")
            }
        }
    }
}

@Composable
private fun MapLegend(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(50),
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CrowdLevel.entries.filter { it != CrowdLevel.UNKNOWN }.forEach { level ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(level.statusColor(), CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text(level.label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun MissingMapKeyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.Map,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(13.dp))
            Text("네이버 지도 키가 필요해요", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(7.dp))
            Text(
                "local.properties에 NAVER_MAP_KEY_ID를 입력한 뒤 다시 빌드해 주세요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun rememberNaverMapView(): MapView {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context).apply { onCreate(null) } }

    DisposableEffect(mapView, lifecycle) {
        var started = false
        var resumed = false
        var destroyed = false

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
            started = true
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
            resumed = true
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> if (!started) {
                    mapView.onStart()
                    started = true
                }
                Lifecycle.Event.ON_RESUME -> if (!resumed) {
                    mapView.onResume()
                    resumed = true
                }
                Lifecycle.Event.ON_PAUSE -> if (resumed) {
                    mapView.onPause()
                    resumed = false
                }
                Lifecycle.Event.ON_STOP -> if (started) {
                    mapView.onStop()
                    started = false
                }
                Lifecycle.Event.ON_DESTROY -> if (!destroyed) {
                    mapView.onDestroy()
                    destroyed = true
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (resumed) mapView.onPause()
            if (started) mapView.onStop()
            if (!destroyed) mapView.onDestroy()
        }
    }
    return mapView
}

private fun mapZoom(span: Double, itemCount: Int): Double {
    if (itemCount == 1) return 14.5
    return when {
        span < 0.015 -> 14.0
        span < 0.035 -> 13.0
        span < 0.075 -> 12.0
        span < 0.15 -> 11.0
        span < 0.28 -> 10.2
        else -> 9.6
    }
}

private val SEOUL_CENTER = LatLng(37.5665, 126.9780)
