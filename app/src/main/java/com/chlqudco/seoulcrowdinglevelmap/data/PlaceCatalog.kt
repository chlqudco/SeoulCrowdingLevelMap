package com.chlqudco.seoulcrowdinglevelmap.data

import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdSnapshot
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceCategory
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceConfig

object PlaceCatalog {
    val places = listOf(
        PlaceConfig("POI068", "성수카페거리", PlaceCategory.HOT_PLACE, "성동구", "카페와 팝업이 모인 서울의 대표 핫플", 37.5446, 127.0559, 0),
        PlaceConfig("POI007", "홍대 관광특구", PlaceCategory.HOT_PLACE, "마포구", "공연과 문화가 밤까지 이어지는 거리", 37.5563, 126.9237, 1),
        PlaceConfig("POI014", "강남역", PlaceCategory.HOT_PLACE, "강남구", "약속과 퇴근 인파가 교차하는 중심지", 37.4979, 127.0276, 2),
        PlaceConfig("POI001", "강남 MICE 관광특구", PlaceCategory.HOT_PLACE, "강남구", "코엑스와 봉은사를 함께 즐기는 도심 코스", 37.5117, 127.0592, 3),
        PlaceConfig("POI054", "혜화역", PlaceCategory.HOT_PLACE, "종로구", "연극과 골목 산책을 즐기는 대학로", 37.5821, 127.0019, 4),
        PlaceConfig("POI101", "서울숲공원", PlaceCategory.PARK, "성동구", "도심 속에서 느긋하게 걷기 좋은 숲", 37.5444, 127.0374, 5),
        PlaceConfig("POI105", "여의도한강공원", PlaceCategory.PARK, "영등포구", "강변 피크닉과 야경을 즐기는 공원", 37.5284, 126.9345, 6),
        PlaceConfig("POI094", "망원한강공원", PlaceCategory.PARK, "마포구", "시장 나들이와 함께 걷기 좋은 강변", 37.5524, 126.8998, 7),
        PlaceConfig("POI090", "난지한강공원", PlaceCategory.PARK, "마포구", "넓은 잔디와 캠핑장이 있는 한강 공원", 37.5689, 126.8763, 8),
        PlaceConfig("POI091", "남산공원", PlaceCategory.PARK, "중구", "서울 도심을 내려다보는 산책 코스", 37.5512, 126.9882, 9),
        PlaceConfig("POI009", "광화문·덕수궁", PlaceCategory.TOURISM, "종로·중구", "궁궐과 광장을 잇는 서울 역사 산책", 37.5700, 126.9769, 10),
        PlaceConfig("POI008", "경복궁", PlaceCategory.TOURISM, "종로구", "조선의 중심을 만나는 대표 궁궐", 37.5796, 126.9770, 11),
        PlaceConfig("POI003", "명동 관광특구", PlaceCategory.TOURISM, "중구", "쇼핑과 길거리 음식이 가득한 관광지", 37.5636, 126.9869, 12),
        PlaceConfig("POI066", "북촌한옥마을", PlaceCategory.TOURISM, "종로구", "한옥 골목 사이로 걷는 조용한 여행", 37.5826, 126.9830, 13),
        PlaceConfig("POI078", "인사동", PlaceCategory.TOURISM, "종로구", "전통 공예와 찻집을 만나는 문화 거리", 37.5740, 126.9856, 14),
        PlaceConfig("POI116", "익선동", PlaceCategory.SHOPPING, "종로구", "한옥 사이 개성 있는 가게가 모인 골목", 37.5743, 126.9895, 15),
        PlaceConfig("POI002", "동대문 관광특구", PlaceCategory.SHOPPING, "중구", "패션과 야시장을 밤까지 즐기는 곳", 37.5676, 127.0090, 16),
        PlaceConfig("POI060", "광장(전통)시장", PlaceCategory.SHOPPING, "종로구", "서울의 맛과 활기를 만나는 전통시장", 37.5700, 126.9996, 17),
        PlaceConfig("POI115", "남대문시장", PlaceCategory.SHOPPING, "중구", "골목마다 다양한 물건과 먹거리가 가득한 시장", 37.5592, 126.9777, 18)
    )

    private data class Seed(
        val level: CrowdLevel,
        val min: Int,
        val max: Int,
        val previous: Int,
        val temperature: Double,
        val sky: String,
        val pm25: Int,
        val pm25Level: String,
        val traffic: String,
        val speed: Double
    )

    fun seedSnapshots(now: Long): List<CrowdSnapshot> {
        val seeds = listOf(
            Seed(CrowdLevel.BUSY, 24_000, 26_000, 22_000, 29.1, "맑음", 17, "좋음", "서행", 18.0),
            Seed(CrowdLevel.CROWDED, 48_000, 52_000, 45_000, 28.7, "구름 조금", 19, "좋음", "정체", 11.0),
            Seed(CrowdLevel.BUSY, 36_000, 38_000, 40_000, 29.4, "맑음", 21, "보통", "서행", 16.0),
            Seed(CrowdLevel.NORMAL, 18_000, 20_000, 18_500, 29.3, "맑음", 20, "보통", "원활", 25.0),
            Seed(CrowdLevel.NORMAL, 12_000, 14_000, 15_000, 28.5, "구름 조금", 18, "좋음", "서행", 18.0),
            Seed(CrowdLevel.RELAXED, 6_000, 8_000, 9_000, 28.4, "맑음", 16, "좋음", "원활", 27.0),
            Seed(CrowdLevel.BUSY, 28_000, 30_000, 25_000, 29.0, "맑음", 18, "좋음", "서행", 17.0),
            Seed(CrowdLevel.NORMAL, 10_000, 12_000, 13_000, 28.3, "구름 조금", 17, "좋음", "원활", 24.0),
            Seed(CrowdLevel.RELAXED, 4_000, 6_000, 7_000, 28.1, "구름 조금", 15, "좋음", "원활", 26.0),
            Seed(CrowdLevel.NORMAL, 9_000, 11_000, 8_000, 27.8, "맑음", 14, "좋음", "서행", 19.0),
            Seed(CrowdLevel.NORMAL, 30_000, 32_000, 29_000, 29.2, "맑음", 20, "보통", "서행", 15.0),
            Seed(CrowdLevel.BUSY, 32_000, 34_000, 30_000, 29.0, "맑음", 18, "좋음", "서행", 14.0),
            Seed(CrowdLevel.CROWDED, 44_000, 48_000, 42_000, 28.8, "구름 조금", 22, "보통", "정체", 10.0),
            Seed(CrowdLevel.RELAXED, 5_000, 7_000, 8_000, 28.2, "맑음", 16, "좋음", "원활", 23.0),
            Seed(CrowdLevel.NORMAL, 14_000, 16_000, 13_000, 28.9, "맑음", 19, "좋음", "서행", 17.0),
            Seed(CrowdLevel.BUSY, 20_000, 22_000, 18_000, 28.8, "구름 조금", 20, "보통", "서행", 15.0),
            Seed(CrowdLevel.NORMAL, 16_000, 18_000, 19_000, 29.1, "맑음", 21, "보통", "서행", 16.0),
            Seed(CrowdLevel.BUSY, 26_000, 28_000, 23_000, 29.0, "맑음", 19, "좋음", "정체", 12.0),
            Seed(CrowdLevel.NORMAL, 18_000, 20_000, 21_000, 28.9, "구름 조금", 20, "보통", "서행", 15.0)
        )
        return places.zip(seeds).mapIndexed { index, (place, seed) ->
            CrowdSnapshot(
                areaCode = place.areaCode,
                areaName = place.areaName,
                level = seed.level,
                minPopulation = seed.min,
                maxPopulation = seed.max,
                previousMidPopulation = seed.previous,
                crowdMessage = seed.level.guidance,
                sourceUpdatedAt = now - (4L + index % 4) * 60_000L,
                fetchedAt = now - (2L + index % 7) * 60_000L,
                temperature = seed.temperature,
                skyStatus = seed.sky,
                pm25 = seed.pm25,
                pm25Level = seed.pm25Level,
                trafficIndex = seed.traffic,
                trafficSpeed = seed.speed,
                roadMessage = when (seed.traffic) {
                    "원활" -> "주변 도로 흐름이 원활해요."
                    "서행" -> "주변 도로에서 서행 구간이 있어요."
                    else -> "진입에 시간이 더 걸릴 수 있어요."
                },
                fetchError = false,
                isDemo = true
            )
        }
    }
}
