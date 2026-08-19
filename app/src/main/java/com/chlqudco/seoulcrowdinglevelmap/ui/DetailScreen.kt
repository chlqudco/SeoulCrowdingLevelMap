package com.chlqudco.seoulcrowdinglevelmap.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.Place
import com.chlqudco.seoulcrowdinglevelmap.model.Trend
import com.chlqudco.seoulcrowdinglevelmap.model.isStale
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.CrowdedRed
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.RadarGreenDark
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.RelaxedGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: Place,
    now: Long,
    ttlMinutes: Int,
    isRefreshing: Boolean,
    isDemoMode: Boolean,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        place.config.areaName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.Refresh, contentDescription = "이 장소 새로고침")
                        }
                    }
                    IconButton(onClick = onFavorite) {
                        Icon(
                            if (place.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (place.isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                            tint = if (place.isFavorite) CrowdedRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (place.isStale(now, ttlMinutes) || place.snapshot.fetchError) {
                item {
                    InformationBanner(
                        text = if (place.snapshot.fetchError) {
                            "실시간 갱신에 실패해 마지막 저장 데이터를 보여드려요."
                        } else {
                            "데이터가 오래됐어요. 새로고침 후 방문을 결정해 주세요."
                        },
                        isWarning = true
                    )
                }
            }
            if (isDemoMode && place.snapshot.isDemo) {
                item { InformationBanner("이 장소는 API 키 연결 전 체험용 데이터예요.") }
            }
            item { CrowdOverviewCard(place) }
            item { PopulationCard(place) }
            item {
                Text("현장 정보", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 6.dp))
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        icon = Icons.Rounded.Cloud,
                        label = "날씨",
                        value = listOfNotNull(
                            place.snapshot.temperature?.let { "${it.toInt()}°" },
                            place.snapshot.skyStatus
                        ).joinToString(" · ").ifBlank { "정보 없음" },
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Rounded.WaterDrop,
                        label = "초미세먼지",
                        value = place.snapshot.pm25Level
                            ?: place.snapshot.pm25?.let { "${it}㎍/㎥" }
                            ?: "정보 없음",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                MetricCard(
                    icon = Icons.Rounded.DirectionsCar,
                    label = "주변 도로",
                    value = listOfNotNull(
                        place.snapshot.trafficIndex,
                        place.snapshot.trafficSpeed?.let { "평균 ${it.toInt()}km/h" }
                    ).joinToString(" · ").ifBlank { "교통 정보 없음" },
                    description = place.snapshot.roadMessage,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { GuidanceCard(place) }
            item {
                DataTimeCard(place, now)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val name = Uri.encode(place.config.areaName)
                            val uri = "geo:${place.config.latitude},${place.config.longitude}?q=${place.config.latitude},${place.config.longitude}($name)".toUri()
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Rounded.Map, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("지도에서 보기")
                    }
                    FilledTonalButton(
                        onClick = onFavorite,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            if (place.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (place.isFavorite) "저장됨" else "저장하기")
                    }
                }
            }
        }
    }
}

@Composable
private fun CrowdOverviewCard(place: Place) {
    val baseColor = when (place.snapshot.level) {
        CrowdLevel.RELAXED -> RadarGreenDark
        CrowdLevel.NORMAL -> Color(0xFF8A6700)
        CrowdLevel.BUSY -> Color(0xFFB85016)
        CrowdLevel.CROWDED -> Color(0xFFA63737)
        CrowdLevel.UNKNOWN -> Color(0xFF56625E)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(baseColor, baseColor.copy(alpha = 0.78f))),
                RoundedCornerShape(28.dp)
            )
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color.White.copy(alpha = 0.15f), contentColor = Color.White, shape = RoundedCornerShape(50)) {
                Text(
                    "${place.config.category.label} · ${place.config.district}",
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(11.dp).background(Color.White, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text("실시간", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(23.dp))
        Text("현재 혼잡도", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
        Text(place.snapshot.level.label, color = Color.White, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(10.dp))
        Text(place.snapshot.level.guidance, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
        Spacer(Modifier.height(16.dp))
        Text(place.config.tagline, color = Color.White.copy(alpha = 0.76f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PopulationCard(place: Place) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(11.dp))
                Column {
                    Text("예상 실시간 인구", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(place.snapshot.populationRange(), style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(Modifier.height(18.dp))
            val previous = place.snapshot.previousMidPopulation
            if (previous != null) {
                val current = place.snapshot.midPopulation
                val maxValue = maxOf(previous, current).coerceAtLeast(1)
                PopulationBar("이전", previous.toFloat() / maxValue, MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(9.dp))
                PopulationBar("현재", current.toFloat() / maxValue, place.snapshot.level.statusColor())
                Spacer(Modifier.height(12.dp))
            }
            val trendColor = when (place.snapshot.trend) {
                Trend.UP -> CrowdedRed
                Trend.DOWN -> RelaxedGreen
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                "${place.snapshot.trendText()} ${place.snapshot.trendSymbol()}",
                color = trendColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun PopulationBar(label: String, ratio: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(38.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0.08f, 1f))
                    .height(8.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(13.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun GuidanceCard(place: Place) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(place.snapshot.level.softColor(), RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Rounded.TipsAndUpdates,
            contentDescription = null,
            tint = place.snapshot.level.statusColor(),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text("방문 팁", style = MaterialTheme.typography.titleMedium, color = place.snapshot.level.statusColor())
            Spacer(Modifier.height(5.dp))
            Text(place.snapshot.crowdMessage, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DataTimeCard(place: Place, now: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.AccessTime,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("서울시 데이터 ${formatClock(place.snapshot.sourceUpdatedAt)} 기준", style = MaterialTheme.typography.bodyMedium)
            Text(
                "앱 수신 ${relativeTime(place.snapshot.fetchedAt, now)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
