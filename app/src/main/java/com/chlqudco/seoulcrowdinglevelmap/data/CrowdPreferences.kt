package com.chlqudco.seoulcrowdinglevelmap.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.crowdDataStore by preferencesDataStore(name = "crowd_radar")

class CrowdPreferences(private val context: Context) {
    private object Keys {
        val snapshots = stringPreferencesKey("snapshots")
        val favorites = stringSetPreferencesKey("favorites")
        val autoRefresh = booleanPreferencesKey("auto_refresh")
        val refreshInterval = intPreferencesKey("refresh_interval")
    }

    private val data: Flow<Preferences> = context.crowdDataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val snapshots: Flow<List<CrowdSnapshot>> = data.map { preferences ->
        decodeSnapshots(preferences[Keys.snapshots].orEmpty())
    }

    val favorites: Flow<Set<String>> = data.map { preferences ->
        preferences[Keys.favorites].orEmpty()
    }

    val autoRefresh: Flow<Boolean> = data.map { preferences ->
        preferences[Keys.autoRefresh] ?: true
    }

    val refreshInterval: Flow<Int> = data.map { preferences ->
        preferences[Keys.refreshInterval] ?: 10
    }

    suspend fun seedIfEmpty(seed: List<CrowdSnapshot>) {
        context.crowdDataStore.edit { preferences ->
            if (decodeSnapshots(preferences[Keys.snapshots].orEmpty()).isEmpty()) {
                preferences[Keys.snapshots] = encodeSnapshots(seed)
            }
        }
    }

    suspend fun currentSnapshots(): List<CrowdSnapshot> = snapshots.first()

    suspend fun updateSnapshot(snapshot: CrowdSnapshot) {
        context.crowdDataStore.edit { preferences ->
            val current = decodeSnapshots(preferences[Keys.snapshots].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.areaCode == snapshot.areaCode }
            if (index >= 0) current[index] = snapshot else current += snapshot
            preferences[Keys.snapshots] = encodeSnapshots(current)
        }
    }

    suspend fun toggleFavorite(areaCode: String) {
        context.crowdDataStore.edit { preferences ->
            val current = preferences[Keys.favorites].orEmpty().toMutableSet()
            if (!current.add(areaCode)) current.remove(areaCode)
            preferences[Keys.favorites] = current
        }
    }

    suspend fun setAutoRefresh(enabled: Boolean) {
        context.crowdDataStore.edit { it[Keys.autoRefresh] = enabled }
    }

    suspend fun setRefreshInterval(minutes: Int) {
        context.crowdDataStore.edit { it[Keys.refreshInterval] = minutes.coerceIn(5, 30) }
    }

    private fun encodeSnapshots(snapshots: List<CrowdSnapshot>): String {
        val array = JSONArray()
        snapshots.forEach { snapshot ->
            array.put(JSONObject().apply {
                put("areaCode", snapshot.areaCode)
                put("areaName", snapshot.areaName)
                put("level", snapshot.level.name)
                put("minPopulation", snapshot.minPopulation)
                put("maxPopulation", snapshot.maxPopulation)
                put("previousMidPopulation", snapshot.previousMidPopulation ?: JSONObject.NULL)
                put("crowdMessage", snapshot.crowdMessage)
                put("sourceUpdatedAt", snapshot.sourceUpdatedAt)
                put("fetchedAt", snapshot.fetchedAt)
                put("temperature", snapshot.temperature ?: JSONObject.NULL)
                put("skyStatus", snapshot.skyStatus ?: JSONObject.NULL)
                put("pm25", snapshot.pm25 ?: JSONObject.NULL)
                put("pm25Level", snapshot.pm25Level ?: JSONObject.NULL)
                put("trafficIndex", snapshot.trafficIndex ?: JSONObject.NULL)
                put("trafficSpeed", snapshot.trafficSpeed ?: JSONObject.NULL)
                put("roadMessage", snapshot.roadMessage ?: JSONObject.NULL)
                put("fetchError", snapshot.fetchError)
                put("isDemo", snapshot.isDemo)
            })
        }
        return array.toString()
    }

    private fun decodeSnapshots(value: String): List<CrowdSnapshot> {
        if (value.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(value)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        CrowdSnapshot(
                            areaCode = item.getString("areaCode"),
                            areaName = item.getString("areaName"),
                            level = runCatching { CrowdLevel.valueOf(item.getString("level")) }.getOrDefault(CrowdLevel.UNKNOWN),
                            minPopulation = item.optInt("minPopulation"),
                            maxPopulation = item.optInt("maxPopulation"),
                            previousMidPopulation = item.optionalInt("previousMidPopulation"),
                            crowdMessage = item.optString("crowdMessage"),
                            sourceUpdatedAt = item.optLong("sourceUpdatedAt"),
                            fetchedAt = item.optLong("fetchedAt"),
                            temperature = item.optionalDouble("temperature"),
                            skyStatus = item.optionalString("skyStatus"),
                            pm25 = item.optionalInt("pm25"),
                            pm25Level = item.optionalString("pm25Level"),
                            trafficIndex = item.optionalString("trafficIndex"),
                            trafficSpeed = item.optionalDouble("trafficSpeed"),
                            roadMessage = item.optionalString("roadMessage"),
                            fetchError = item.optBoolean("fetchError"),
                            isDemo = item.optBoolean("isDemo", true)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun JSONObject.optionalString(key: String): String? {
        return if (isNull(key)) null else optString(key).ifBlank { null }
    }

    private fun JSONObject.optionalInt(key: String): Int? {
        return if (isNull(key)) null else optInt(key)
    }

    private fun JSONObject.optionalDouble(key: String): Double? {
        return if (isNull(key)) null else optDouble(key)
    }
}
