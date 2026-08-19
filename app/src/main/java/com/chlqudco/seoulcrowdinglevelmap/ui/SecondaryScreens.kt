package com.chlqudco.seoulcrowdinglevelmap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel

@Composable
fun FavoritesScreen(
    state: CrowdUiState,
    onPlaceClick: (String) -> Unit,
    onFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                title = "즐겨찾기",
                description = "자주 확인하는 장소를 한곳에서 비교해 보세요"
            )
        }
        if (state.visiblePlaces.isEmpty()) {
            item {
                EmptyState(
                    title = "저장한 장소가 없어요",
                    description = "장소 카드의 하트를 눌러\n나만의 혼잡도 목록을 만들어 보세요"
                )
            }
        } else {
            item {
                InformationBanner("즐겨찾기 ${state.visiblePlaces.size}곳을 덜 붐비는 순으로 정렬했어요.")
            }
            items(state.visiblePlaces, key = { it.config.areaCode }) { place ->
                PlaceListCard(
                    place = place,
                    now = state.now,
                    ttlMinutes = state.refreshInterval,
                    onClick = { onPlaceClick(place.config.areaCode) },
                    onFavorite = { onFavorite(place.config.areaCode) }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    state: CrowdUiState,
    onAutoRefreshChanged: (Boolean) -> Unit,
    onRefreshIntervalChanged: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                title = "설정",
                description = "데이터 갱신 방식과 혼잡도 기준을 확인하세요"
            )
        }
        item {
            SettingsCard {
                SettingRow(
                    icon = Icons.Rounded.Autorenew,
                    title = "자동 갱신",
                    description = "앱을 사용하는 동안 오래된 데이터만 갱신해요",
                    trailing = {
                        Switch(checked = state.autoRefresh, onCheckedChange = onAutoRefreshChanged)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingIcon(Icons.Rounded.Schedule)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("캐시 유효 시간", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "이 시간이 지나면 업데이트가 필요하다고 판단해요",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15).forEach { minutes ->
                        FilterChip(
                            selected = state.refreshInterval == minutes,
                            onClick = { onRefreshIntervalChanged(minutes) },
                            label = { Text("${minutes}분") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingRow(
                    icon = Icons.Rounded.CloudSync,
                    title = "서울시 API 연결",
                    description = if (state.isDemoMode) {
                        "샘플 키 사용 중 · 광화문 외 장소는 체험 데이터"
                    } else {
                        "인증키 연결됨 · 19개 장소 실시간 조회"
                    },
                    trailing = {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (state.isDemoMode) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                if (state.isDemoMode) "DEMO" else "LIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
                Spacer(Modifier.height(17.dp))
                Button(
                    onClick = onRefresh,
                    enabled = !state.isRefreshing,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 13.dp)
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("지금 전체 새로고침")
                }
            }
        }
        item {
            Text("혼잡도 기준", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
        }
        item {
            SettingsCard {
                CrowdLevel.entries.filter { it != CrowdLevel.UNKNOWN }.forEachIndexed { index, level ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(level.statusColor(), CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(level.label, style = MaterialTheme.typography.titleMedium)
                            Text(level.guidance, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (index < 3) HorizontalDivider(modifier = Modifier.padding(vertical = 13.dp), color = MaterialTheme.colorScheme.outline)
                }
            }
        }
        item {
            SettingsCard {
                SettingRow(
                    icon = Icons.Rounded.DataObject,
                    title = "데이터 안내",
                    description = "서울시 실시간 도시데이터를 장소별로 조회합니다. 통신사 추정 인구이므로 실제 현장과 차이가 있을 수 있어요."
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "데이터 출처 · 서울 열린데이터광장",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun ScreenHeader(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(5.dp))
        Text(
            description,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    description: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SettingIcon(icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        }
    }
}

@Composable
private fun SettingIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
    }
}
