package com.chlqudco.seoulcrowdinglevelmap

import com.chlqudco.seoulcrowdinglevelmap.data.PlaceCatalog
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdSnapshot
import com.chlqudco.seoulcrowdinglevelmap.model.Place
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceCategory
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceConfig
import com.chlqudco.seoulcrowdinglevelmap.model.Trend
import com.chlqudco.seoulcrowdinglevelmap.model.rankPlaces
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrowdRankingTest {
    @Test
    fun apiLevelsAreMappedInRankingOrder() {
        assertEquals(CrowdLevel.RELAXED, CrowdLevel.fromApi("여유"))
        assertEquals(CrowdLevel.NORMAL, CrowdLevel.fromApi("보통"))
        assertEquals(CrowdLevel.BUSY, CrowdLevel.fromApi("약간 붐빔"))
        assertEquals(CrowdLevel.CROWDED, CrowdLevel.fromApi("붐빔"))
        assertEquals(CrowdLevel.UNKNOWN, CrowdLevel.fromApi(null))
    }

    @Test
    fun placesAreRankedByLevelThenPopulation() {
        val now = 1_000_000L
        val places = listOf(
            place("C", CrowdLevel.NORMAL, 1_000, 2_000, now),
            place("B", CrowdLevel.RELAXED, 4_000, 6_000, now),
            place("A", CrowdLevel.RELAXED, 1_000, 3_000, now)
        )
        assertEquals(listOf("A", "B", "C"), rankPlaces(places, now, 10).map { it.config.areaCode })
    }

    @Test
    fun trendUsesMeaningfulPopulationDifference() {
        val increasing = snapshot("A", CrowdLevel.NORMAL, 12_000, 14_000, 10_000, 1L)
        val stable = snapshot("B", CrowdLevel.NORMAL, 9_500, 10_500, 10_000, 1L)
        val decreasing = snapshot("C", CrowdLevel.NORMAL, 6_000, 8_000, 10_000, 1L)
        assertEquals(Trend.UP, increasing.trend)
        assertEquals(Trend.STABLE, stable.trend)
        assertEquals(Trend.DOWN, decreasing.trend)
    }

    @Test
    fun catalogContainsUniqueMvpPlacesAndSeedData() {
        val now = System.currentTimeMillis()
        assertTrue(PlaceCatalog.places.size in 12..20)
        assertEquals(PlaceCatalog.places.size, PlaceCatalog.places.map { it.areaCode }.toSet().size)
        assertEquals(PlaceCatalog.places.map { it.areaCode }, PlaceCatalog.seedSnapshots(now).map { it.areaCode })
    }

    private fun place(
        code: String,
        level: CrowdLevel,
        min: Int,
        max: Int,
        fetchedAt: Long
    ): Place {
        val config = PlaceConfig(code, code, PlaceCategory.HOT_PLACE, "서울", "", 0.0, 0.0, code.first().code)
        return Place(config, snapshot(code, level, min, max, null, fetchedAt), false)
    }

    private fun snapshot(
        code: String,
        level: CrowdLevel,
        min: Int,
        max: Int,
        previous: Int?,
        fetchedAt: Long
    ): CrowdSnapshot {
        return CrowdSnapshot(
            areaCode = code,
            areaName = code,
            level = level,
            minPopulation = min,
            maxPopulation = max,
            previousMidPopulation = previous,
            crowdMessage = level.guidance,
            sourceUpdatedAt = fetchedAt,
            fetchedAt = fetchedAt,
            temperature = null,
            skyStatus = null,
            pm25 = null,
            pm25Level = null,
            trafficIndex = null,
            trafficSpeed = null,
            roadMessage = null,
            fetchError = false,
            isDemo = true
        )
    }
}
