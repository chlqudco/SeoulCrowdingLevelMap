package com.chlqudco.seoulcrowdinglevelmap.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.Place
import com.chlqudco.seoulcrowdinglevelmap.model.Trend
import com.chlqudco.seoulcrowdinglevelmap.model.isStale
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.BusyOrange
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.CrowdedRed
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.NormalYellow
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.RadarGreen
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.RadarMint
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.RelaxedGreen

fun CrowdLevel.statusColor(): Color = when (this) {
    CrowdLevel.RELAXED -> RelaxedGreen
    CrowdLevel.NORMAL -> NormalYellow
    CrowdLevel.BUSY -> BusyOrange
    CrowdLevel.CROWDED -> CrowdedRed
    CrowdLevel.UNKNOWN -> Color(0xFF7A8581)
}

fun CrowdLevel.softColor(): Color = when (this) {
    CrowdLevel.RELAXED -> Color(0xFFE2F6EC)
    CrowdLevel.NORMAL -> Color(0xFFFFF4CF)
    CrowdLevel.BUSY -> Color(0xFFFFEBDD)
    CrowdLevel.CROWDED -> Color(0xFFFFE4E4)
    CrowdLevel.UNKNOWN -> Color(0xFFECEFED)
}

@Composable
fun RadarMark(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = RadarMint
    Canvas(modifier = modifier.size(40.dp)) {
        val center = this.center
        drawCircle(primary.copy(alpha = 0.09f), radius = size.minDimension / 2)
        drawCircle(primary.copy(alpha = 0.24f), radius = size.minDimension * 0.33f, style = Stroke(1.5.dp.toPx()))
        drawCircle(primary.copy(alpha = 0.38f), radius = size.minDimension * 0.19f, style = Stroke(1.5.dp.toPx()))
        drawLine(
            color = primary,
            start = center,
            end = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.27f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(secondary, radius = 3.5.dp.toPx(), center = center)
    }
}

@Composable
fun CrowdBadge(level: CrowdLevel, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = level.softColor(),
        contentColor = level.statusColor(),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(level.statusColor(), CircleShape)
            )
            Text(level.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FreshnessBadge(place: Place, now: Long, ttlMinutes: Int) {
    val isStale = place.isStale(now, ttlMinutes)
    if (!isStale && !place.snapshot.fetchError) return
    val label = if (place.snapshot.fetchError) "갱신 실패" else "업데이트 필요"
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, modifier = Modifier.size(13.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun TrendText(place: Place, modifier: Modifier = Modifier) {
    val color = when (place.snapshot.trend) {
        Trend.UP -> CrowdedRed
        Trend.DOWN -> RelaxedGreen
        Trend.STABLE, Trend.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = "${place.snapshot.trendText()} ${place.snapshot.trendSymbol()}",
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun TopPlaceCard(
    place: Place,
    rank: Int,
    now: Long,
    ttlMinutes: Int,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Card(
        onClick = onClick,
        modifier = modifier.width(252.dp),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Text(
                        text = rank.toString(),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.weight(1f))
                FreshnessBadge(place, now, ttlMinutes)
                IconButton(onClick = onFavorite, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = if (place.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (place.isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                        tint = if (place.isFavorite) CrowdedRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = place.config.areaName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${place.config.district} · ${place.config.category.label}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(14.dp))
            CrowdBadge(place.snapshot.level)
            Spacer(Modifier.height(14.dp))
            Text("예상 인구", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(place.snapshot.populationRange(), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(5.dp))
            TrendText(place)
        }
    }
}

@Composable
fun PlaceListCard(
    place: Place,
    now: Long,
    ttlMinutes: Int,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .size(width = 5.dp, height = 45.dp)
                        .clip(CircleShape)
                        .background(place.snapshot.level.statusColor())
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.config.areaName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Place,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "${place.config.district} · ${place.config.category.label}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                IconButton(onClick = onFavorite, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (place.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (place.isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                        tint = if (place.isFavorite) CrowdedRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CrowdBadge(place.snapshot.level)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = place.snapshot.populationRange(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrendText(place, Modifier.weight(1f))
                FreshnessBadge(place, now, ttlMinutes)
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Rounded.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    relativeTime(place.snapshot.sourceUpdatedAt, now),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun InformationBanner(
    text: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    val background = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val foreground = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isWarning) Icons.Rounded.ErrorOutline else Icons.Rounded.Info,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(19.dp)
        )
        Text(text, color = foreground, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(7.dp))
        Text(
            description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
