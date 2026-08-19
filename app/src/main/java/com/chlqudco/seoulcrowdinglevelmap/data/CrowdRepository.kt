package com.chlqudco.seoulcrowdinglevelmap.data

import android.content.Context
import com.chlqudco.seoulcrowdinglevelmap.BuildConfig
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdSnapshot
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlin.math.max

data class RefreshResult(
    val refreshed: Int,
    val failed: Int,
    val skipped: Boolean,
    val message: String
)

class CrowdRepository(context: Context) {
    private val preferences = CrowdPreferences(context.applicationContext)
    private val remote = SeoulCityRemoteDataSource(BuildConfig.SEOUL_API_KEY)
    private val refreshMutex = Mutex()
    private val requestSemaphore = Semaphore(2)
    private var lastManualRefreshAt = 0L

    val snapshots: Flow<List<CrowdSnapshot>> = preferences.snapshots
    val favorites: Flow<Set<String>> = preferences.favorites
    val autoRefresh: Flow<Boolean> = preferences.autoRefresh
    val refreshInterval: Flow<Int> = preferences.refreshInterval
    val isDemoMode: Boolean = remote.isDemoMode

    suspend fun initialize() {
        val seed = PlaceCatalog.seedSnapshots(System.currentTimeMillis())
        preferences.seedIfEmpty(seed)
        val currentCodes = preferences.currentSnapshots().mapTo(mutableSetOf()) { it.areaCode }
        for (snapshot in seed.filterNot { it.areaCode in currentCodes }) {
            preferences.updateSnapshot(snapshot)
        }
    }

    suspend fun refreshAll(ttlMinutes: Int, force: Boolean): RefreshResult {
        val now = System.currentTimeMillis()
        if (force && now - lastManualRefreshAt < MANUAL_COOLDOWN) {
            val seconds = ((MANUAL_COOLDOWN - (now - lastManualRefreshAt)) / 1_000L + 1L).coerceAtLeast(1L)
            return RefreshResult(0, 0, true, "${seconds}초 뒤에 다시 새로고침할 수 있어요.")
        }
        if (force) lastManualRefreshAt = now
        return refreshMutex.withLock {
            val snapshots = preferences.currentSnapshots()
            val byCode = snapshots.associateBy { it.areaCode }
            val targets = PlaceCatalog.places.filter { config ->
                val snapshot = byCode[config.areaCode]
                force || snapshot == null || now - snapshot.fetchedAt > ttlMinutes * 60_000L
            }
            if (targets.isEmpty()) {
                return@withLock RefreshResult(0, 0, true, "모든 장소가 최신 상태예요.")
            }
            val results = coroutineScope {
                targets.map { config ->
                    async {
                        requestSemaphore.withPermit {
                            refresh(config, byCode[config.areaCode], now)
                        }
                    }
                }.awaitAll()
            }
            val refreshed = results.count { it }
            val failed = results.size - refreshed
            val message = when {
                failed == 0 && isDemoMode -> "데모 장소 ${refreshed}곳을 최신 상태로 바꿨어요."
                failed == 0 -> "장소 ${refreshed}곳을 새로고침했어요."
                refreshed == 0 -> "실시간 갱신에 실패해 저장된 데이터를 표시해요."
                else -> "${refreshed}곳 갱신 완료 · ${failed}곳은 저장된 데이터를 표시해요."
            }
            RefreshResult(refreshed, failed, false, message)
        }
    }

    suspend fun refreshPlace(areaCode: String): RefreshResult {
        val config = PlaceCatalog.places.firstOrNull { it.areaCode == areaCode }
            ?: return RefreshResult(0, 1, true, "장소 정보를 찾지 못했어요.")
        return refreshMutex.withLock {
            val previous = preferences.currentSnapshots().firstOrNull { it.areaCode == areaCode }
            val success = requestSemaphore.withPermit {
                refresh(config, previous, System.currentTimeMillis())
            }
            if (success) {
                RefreshResult(1, 0, false, "${config.areaName} 정보를 새로고침했어요.")
            } else {
                RefreshResult(0, 1, false, "갱신하지 못해 저장된 정보를 표시해요.")
            }
        }
    }

    suspend fun toggleFavorite(areaCode: String) = preferences.toggleFavorite(areaCode)

    suspend fun setAutoRefresh(enabled: Boolean) = preferences.setAutoRefresh(enabled)

    suspend fun setRefreshInterval(minutes: Int) = preferences.setRefreshInterval(minutes)

    private suspend fun refresh(
        config: PlaceConfig,
        previous: CrowdSnapshot?,
        now: Long
    ): Boolean {
        return runCatching {
            val snapshot = if (isDemoMode && config.areaCode != SAMPLE_AREA_CODE) {
                refreshDemo(config, previous, now)
            } else {
                remote.fetch(config, previous)
            }
            preferences.updateSnapshot(snapshot)
        }.fold(
            onSuccess = { true },
            onFailure = {
                if (previous != null) preferences.updateSnapshot(previous.copy(fetchError = true))
                false
            }
        )
    }

    private fun refreshDemo(
        config: PlaceConfig,
        previous: CrowdSnapshot?,
        now: Long
    ): CrowdSnapshot {
        val base = previous ?: PlaceCatalog.seedSnapshots(now).first { it.areaCode == config.areaCode }
        val movement = ((((now / 60_000L) + config.displayOrder) % 5L) - 2L).toInt() * 400
        val newMin = max(500, base.minPopulation + movement)
        val width = max(1_000, base.maxPopulation - base.minPopulation)
        return base.copy(
            minPopulation = newMin,
            maxPopulation = newMin + width,
            previousMidPopulation = base.midPopulation,
            sourceUpdatedAt = now - 5 * 60_000L,
            fetchedAt = now,
            fetchError = false,
            isDemo = true
        )
    }

    private companion object {
        const val SAMPLE_AREA_CODE = "POI009"
        const val MANUAL_COOLDOWN = 5_000L
    }
}
