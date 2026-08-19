package com.chlqudco.seoulcrowdinglevelmap.ui

import com.chlqudco.seoulcrowdinglevelmap.model.CrowdSnapshot
import com.chlqudco.seoulcrowdinglevelmap.model.Trend
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val seoulZone = ZoneId.of("Asia/Seoul")

fun formatPopulation(value: Int): String = numberFormat.format(value)

fun CrowdSnapshot.populationRange(): String {
    return "${formatPopulation(minPopulation)}~${formatPopulation(maxPopulation)}명"
}

fun relativeTime(timestamp: Long, now: Long): String {
    val minutes = ((now - timestamp).coerceAtLeast(0L) / 60_000L).toInt()
    return when {
        minutes < 1 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        minutes < 24 * 60 -> "${minutes / 60}시간 전"
        else -> "${minutes / (24 * 60)}일 전"
    }
}

fun formatClock(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp).atZone(seoulZone).format(timeFormat)
}

fun CrowdSnapshot.trendText(): String = when (trend) {
    Trend.UP -> "이전보다 증가"
    Trend.DOWN -> "이전보다 감소"
    Trend.STABLE -> "비슷한 수준"
    Trend.UNKNOWN -> "변화 확인 중"
}

fun CrowdSnapshot.trendSymbol(): String = when (trend) {
    Trend.UP -> "↑"
    Trend.DOWN -> "↓"
    Trend.STABLE -> "→"
    Trend.UNKNOWN -> "·"
}
