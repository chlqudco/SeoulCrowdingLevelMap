package com.chlqudco.seoulcrowdinglevelmap.model

enum class CrowdLevel(
    val label: String,
    val rank: Int,
    val guidance: String
) {
    RELAXED("여유", 0, "지금 방문하기 편안해요"),
    NORMAL("보통", 1, "평범한 수준이에요"),
    BUSY("약간 붐빔", 2, "사람이 제법 있어요"),
    CROWDED("붐빔", 3, "혼잡을 피하려면 다른 장소를 고려하세요"),
    UNKNOWN("업데이트 필요", 4, "최신 혼잡도를 확인하고 있어요");

    companion object {
        fun fromApi(value: String?): CrowdLevel = when (value?.trim()) {
            "여유" -> RELAXED
            "보통" -> NORMAL
            "약간 붐빔" -> BUSY
            "붐빔" -> CROWDED
            else -> UNKNOWN
        }
    }
}

enum class PlaceCategory(val label: String) {
    ALL("전체"),
    HOT_PLACE("핫플"),
    PARK("공원·산책"),
    TOURISM("관광"),
    SHOPPING("쇼핑·상권")
}

enum class Trend {
    UP,
    DOWN,
    STABLE,
    UNKNOWN
}

data class PlaceConfig(
    val areaCode: String,
    val areaName: String,
    val category: PlaceCategory,
    val district: String,
    val tagline: String,
    val latitude: Double,
    val longitude: Double,
    val displayOrder: Int
)

data class CrowdSnapshot(
    val areaCode: String,
    val areaName: String,
    val level: CrowdLevel,
    val minPopulation: Int,
    val maxPopulation: Int,
    val previousMidPopulation: Int?,
    val crowdMessage: String,
    val sourceUpdatedAt: Long,
    val fetchedAt: Long,
    val temperature: Double?,
    val skyStatus: String?,
    val pm25: Int?,
    val pm25Level: String?,
    val trafficIndex: String?,
    val trafficSpeed: Double?,
    val roadMessage: String?,
    val fetchError: Boolean,
    val isDemo: Boolean
) {
    val midPopulation: Int
        get() = (minPopulation + maxPopulation) / 2

    val trend: Trend
        get() {
            val previous = previousMidPopulation ?: return Trend.UNKNOWN
            val threshold = maxOf(1_000, (previous * 0.05).toInt())
            val difference = midPopulation - previous
            return when {
                difference > threshold -> Trend.UP
                difference < -threshold -> Trend.DOWN
                else -> Trend.STABLE
            }
        }
}

data class Place(
    val config: PlaceConfig,
    val snapshot: CrowdSnapshot,
    val isFavorite: Boolean
)

fun Place.isStale(now: Long, ttlMinutes: Int): Boolean {
    return now - snapshot.fetchedAt > ttlMinutes * 60_000L
}

fun rankPlaces(
    places: List<Place>,
    now: Long,
    ttlMinutes: Int
): List<Place> {
    return places.sortedWith(
        compareBy<Place> {
            val stalePenalty = if (it.isStale(now, ttlMinutes)) 5_000_000L else 0L
            it.snapshot.level.rank * 10_000_000L + stalePenalty + it.snapshot.midPopulation
        }.thenBy { it.config.displayOrder }
    )
}
