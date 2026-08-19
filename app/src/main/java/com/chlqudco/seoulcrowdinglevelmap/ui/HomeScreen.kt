package com.chlqudco.seoulcrowdinglevelmap.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceCategory
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.RadarGreenDark
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.RadarMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: CrowdUiState,
    onCategorySelected: (PlaceCategory) -> Unit,
    onPlaceClick: (String) -> Unit,
    onFavorite: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        if (state.allPlaces.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@PullToRefreshBox
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                HomeTopBar(
                    isRefreshing = state.isRefreshing,
                    lastRefreshAt = state.lastRefreshAt,
                    now = state.now,
                    onRefresh = onRefresh
                )
            }
            if (state.isDemoMode) {
                item {
                    InformationBanner(
                        text = "API 샘플 모드예요. 광화문·덕수궁 외 장소는 체험용 데이터로 표시됩니다.",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }
            item {
                SeoulHero(
                    relaxedCount = state.allPlaces.count { it.snapshot.level == CrowdLevel.RELAXED },
                    totalCount = state.allPlaces.size,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
            item {
                SectionTitle(
                    title = "지금 한산한 곳 TOP 5",
                    subtitle = "덜 붐비는 순서로 골랐어요",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.topPlaces, key = { _, place -> place.config.areaCode }) { index, place ->
                        TopPlaceCard(
                            place = place,
                            rank = index + 1,
                            now = state.now,
                            ttlMinutes = state.refreshInterval,
                            onClick = { onPlaceClick(place.config.areaCode) },
                            onFavorite = { onFavorite(place.config.areaCode) }
                        )
                    }
                }
            }
            item {
                Text(
                    text = "TOP 5는 현재 수집된 장소를 비교한 결과이며 안전을 보장하는 지표가 아니에요.",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            item { Spacer(Modifier.height(28.dp)) }
            item {
                SectionTitle(
                    title = "장소 둘러보기",
                    subtitle = "${state.visiblePlaces.size}곳",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PlaceCategory.entries, key = { it.name }) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = state.selectedCategory == category,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
            items(state.visiblePlaces, key = { it.config.areaCode }) { place ->
                PlaceListCard(
                    place = place,
                    now = state.now,
                    ttlMinutes = state.refreshInterval,
                    onClick = { onPlaceClick(place.config.areaCode) },
                    onFavorite = { onFavorite(place.config.areaCode) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    isRefreshing: Boolean,
    lastRefreshAt: Long?,
    now: Long,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadarMark()
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text("서울 혼잡도 레이더", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(RadarMint, CircleShape))
                Text(
                    text = if (lastRefreshAt == null) " 데이터 준비 중" else " ${relativeTime(lastRefreshAt, now)} 갱신",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        IconButton(onClick = onRefresh, enabled = !isRefreshing) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Refresh, contentDescription = "전체 새로고침")
            }
        }
    }
}

@Composable
private fun SeoulHero(
    relaxedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(196.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(RadarGreenDark, Color(0xFF1B765A)),
                    start = Offset.Zero,
                    end = Offset.Infinite
                ),
                shape
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.86f, size.height * 0.2f)
            listOf(46f, 82f, 120f).forEach { radius ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = radius.dp.toPx(),
                    center = center,
                    style = Stroke(1.2.dp.toPx())
                )
            }
            drawLine(
                color = RadarMint.copy(alpha = 0.55f),
                start = center,
                end = Offset(size.width * 0.98f, size.height * 0.05f),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(RadarMint, 5.dp.toPx(), center)
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.13f),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    "LIVE · 서울 ${totalCount}곳",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(15.dp))
            Text("지금, 덜 붐비는\n서울을 찾아보세요", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (relaxedCount > 0) "현재 여유로운 장소가 ${relaxedCount}곳 있어요" else "장소별 혼잡도를 비교해 보세요",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}
