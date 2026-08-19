package com.chlqudco.seoulcrowdinglevelmap.data

import android.util.Xml
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdSnapshot
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface SeoulCityService {
    @GET("{key}/xml/citydata/1/5/{areaCode}")
    suspend fun getCityData(
        @Path("key") key: String,
        @Path("areaCode") areaCode: String
    ): ResponseBody
}

class SeoulCityRemoteDataSource(apiKey: String) {
    private val key = apiKey.ifBlank { "sample" }
    val isDemoMode: Boolean = key == "sample"

    private val service = Retrofit.Builder()
        .baseUrl("http://openapi.seoul.go.kr:8088/")
        .build()
        .create(SeoulCityService::class.java)

    suspend fun fetch(
        config: PlaceConfig,
        previous: CrowdSnapshot?
    ): CrowdSnapshot = withContext(Dispatchers.IO) {
        val response = service.getCityData(key, config.areaCode).string()
        SeoulCityXmlParser.parse(response, config, previous, System.currentTimeMillis())
    }
}

object SeoulCityXmlParser {
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val fields = setOf(
        "RESULT.CODE",
        "RESULT.MESSAGE",
        "AREA_NM",
        "AREA_CD",
        "AREA_CONGEST_LVL",
        "AREA_CONGEST_MSG",
        "AREA_PPLTN_MIN",
        "AREA_PPLTN_MAX",
        "PPLTN_TIME",
        "ROAD_TRAFFIC_IDX",
        "ROAD_TRAFFIC_SPD",
        "ROAD_MSG",
        "TEMP",
        "SKY_STTS",
        "PM25_INDEX",
        "PM25"
    )

    fun parse(
        xml: String,
        config: PlaceConfig,
        previous: CrowdSnapshot?,
        fetchedAt: Long
    ): CrowdSnapshot {
        val values = linkedMapOf<String, String>()
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(xml.reader())
        }
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name in fields && parser.name !in values) {
                values[parser.name] = parser.nextText().trim()
            }
            event = parser.next()
        }
        val resultCode = values["RESULT.CODE"]
        if (resultCode != "INFO-000") {
            throw SeoulApiException(values["RESULT.MESSAGE"].orEmpty().ifBlank { "서울시 데이터를 불러오지 못했습니다." })
        }
        val minPopulation = values["AREA_PPLTN_MIN"]?.toIntOrNull()
            ?: throw SeoulApiException("실시간 인구 최솟값이 없습니다.")
        val maxPopulation = values["AREA_PPLTN_MAX"]?.toIntOrNull()
            ?: throw SeoulApiException("실시간 인구 최댓값이 없습니다.")
        return CrowdSnapshot(
            areaCode = values["AREA_CD"].orEmpty().ifBlank { config.areaCode },
            areaName = values["AREA_NM"].orEmpty().ifBlank { config.areaName },
            level = CrowdLevel.fromApi(values["AREA_CONGEST_LVL"]),
            minPopulation = minPopulation,
            maxPopulation = maxPopulation,
            previousMidPopulation = previous?.midPopulation,
            crowdMessage = values["AREA_CONGEST_MSG"].orEmpty().ifBlank {
                CrowdLevel.fromApi(values["AREA_CONGEST_LVL"]).guidance
            },
            sourceUpdatedAt = parseTime(values["PPLTN_TIME"]) ?: fetchedAt,
            fetchedAt = fetchedAt,
            temperature = values["TEMP"]?.toDoubleOrNull(),
            skyStatus = values["SKY_STTS"]?.ifBlank { null },
            pm25 = values["PM25"]?.toDoubleOrNull()?.toInt(),
            pm25Level = values["PM25_INDEX"]?.ifBlank { null },
            trafficIndex = values["ROAD_TRAFFIC_IDX"]?.ifBlank { null },
            trafficSpeed = values["ROAD_TRAFFIC_SPD"]?.toDoubleOrNull(),
            roadMessage = values["ROAD_MSG"]?.ifBlank { null },
            fetchError = false,
            isDemo = false
        )
    }

    private fun parseTime(value: String?): Long? {
        return runCatching {
            LocalDateTime.parse(value, timeFormatter)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }
}

class SeoulApiException(message: String) : Exception(message)
