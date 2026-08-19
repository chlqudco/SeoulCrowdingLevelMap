package com.chlqudco.seoulcrowdinglevelmap.data

import com.chlqudco.seoulcrowdinglevelmap.model.CrowdLevel
import com.chlqudco.seoulcrowdinglevelmap.model.CrowdSnapshot
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceCategory
import com.chlqudco.seoulcrowdinglevelmap.model.PlaceConfig

object PlaceCatalog {
    private data class OfficialPlace(
        val areaCode: String,
        val areaName: String,
        val englishName: String,
        val category: PlaceCategory
    )

    private data class PlaceDetail(
        val district: String,
        val tagline: String,
        val latitude: Double,
        val longitude: Double
    )

    private val officialPlaces = listOf(
        OfficialPlace("POI001", "강남 MICE 관광특구", "Gangnam MICE Special Tourist Zone", PlaceCategory.TOURIST_ZONE),
        OfficialPlace("POI002", "동대문 관광특구", "Dongdaemun Fashion Town Special Tourist Zone", PlaceCategory.TOURIST_ZONE),
        OfficialPlace("POI003", "명동 관광특구", "Myeong-dong Namdaemun Bukchang-dong Da-dong Mugyo-dong Special Tourist Zone", PlaceCategory.TOURIST_ZONE),
        OfficialPlace("POI004", "이태원 관광특구", "Itaewon Special Tourist Zone", PlaceCategory.TOURIST_ZONE),
        OfficialPlace("POI005", "잠실 관광특구", "Jamsil Special Tourist Zone", PlaceCategory.TOURIST_ZONE),
        OfficialPlace("POI006", "종로·청계 관광특구", "Jongno Cheonggye Special Tourist Zone", PlaceCategory.TOURIST_ZONE),
        OfficialPlace("POI007", "홍대 관광특구", "Hongdae Culture and Arts Special Tourist Zone", PlaceCategory.TOURIST_ZONE),
        OfficialPlace("POI008", "경복궁", "Gyeongbokgung Palace", PlaceCategory.HERITAGE),
        OfficialPlace("POI009", "광화문·덕수궁", "Gwanghwamun and Deoksugung Palace", PlaceCategory.HERITAGE),
        OfficialPlace("POI010", "보신각", "Bosingak", PlaceCategory.HERITAGE),
        OfficialPlace("POI011", "서울 암사동 유적", "Amsa Prehistoric Settlement Site", PlaceCategory.HERITAGE),
        OfficialPlace("POI012", "창덕궁·종묘", "Changdeokgung Palace and Jongmyo Shrine", PlaceCategory.HERITAGE),
        OfficialPlace("POI013", "가산디지털단지역", "Gasan Digital Complex Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI014", "강남역", "Gangnam Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI015", "건대입구역", "Konkuk University Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI016", "고덕역", "Godeok Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI017", "고속터미널역", "Express Bus Terminal Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI018", "교대역", "Seoul National University of Education Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI019", "구로디지털단지역", "Guro Digital Complex Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI020", "구로역", "Guro Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI021", "군자역", "Gunja Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI023", "대림역", "Daerim Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI024", "동대문역", "Dongdaemun Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI025", "뚝섬역", "Ttukseom Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI026", "미아사거리역", "Miasageori Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI027", "발산역", "Balsan Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI029", "사당역", "Sadang Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI030", "삼각지역", "Samgakji Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI031", "서울대입구역", "Seoul National University Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI032", "서울식물원·마곡나루역", "Seoul Botanic Park and Magongnaru Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI033", "서울역", "Seoul Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI034", "선릉역", "Seolleung Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI035", "성신여대입구역", "Sungshin Women's University Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI036", "수유역", "Suyu Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI037", "신논현역·논현역", "Sinnonhyeon and Nonhyeon Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI038", "신도림역", "Sindorim Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI039", "신림역", "Sillim Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI040", "신촌·이대역", "Sinchon and Ewha Womans University Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI041", "양재역", "Yangjae Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI042", "역삼역", "Yeoksam Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI043", "연신내역", "Yeonsinnae Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI044", "오목교역·목동운동장", "Omokgyo Station and Mok-dong Stadium", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI045", "왕십리역", "Wangsimni Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI046", "용산역", "Yongsan Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI047", "이태원역", "Itaewon Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI048", "장지역", "Jangji Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI049", "장한평역", "Janghanpyeong Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI050", "천호역", "Cheonho Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI051", "총신대입구(이수)역", "Chongshin University Isu Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI052", "충정로역", "Chungjeongno Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI053", "합정역", "Hapjeong Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI054", "혜화역", "Hyehwa Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI055", "홍대입구역(2호선)", "Hongik University Station Line 2", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI056", "회기역", "Hoegi Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI058", "가락시장", "Garak Market", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI059", "가로수길", "Garosu-gil", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI060", "광장(전통)시장", "Gwangjang Traditional Market", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI061", "김포공항", "Gimpo Airport", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI063", "노량진", "Noryangjin", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI064", "덕수궁길·정동길", "Deoksugung-gil and Jeongdong-gil", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI066", "북촌한옥마을", "Bukchon Hanok Village", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI067", "서촌", "Seochon", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI068", "성수카페거리", "Seongsu Cafe Street", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI070", "쌍문역", "Ssangmun Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI071", "압구정로데오거리", "Apgujeong Rodeo Street", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI072", "여의도", "Yeouido", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI073", "연남동", "Yeonnam-dong", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI074", "영등포 타임스퀘어", "Yeongdeungpo Times Square", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI076", "용리단길", "Yongnidan-gil", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI077", "이태원 앤틱가구거리", "Itaewon Antique Furniture Street", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI078", "인사동", "Insa-dong", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI079", "창동 신경제 중심지", "Changdong New Economic Center", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI080", "청담동 명품거리", "Cheongdam-dong Luxury Fashion Street", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI081", "청량리 제기동 일대 전통시장", "Traditional Market in Cheongnyangni Jegi-dong", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI082", "해방촌·경리단길", "Haebangchon and Gyeongnidan-gil", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI083", "DDP(동대문디자인플라자)", "DDP Dongdaemun Design Plaza", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI084", "DMC(디지털미디어시티)", "DMC Digital Media City", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI085", "강서한강공원", "Gangseo Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI086", "고척돔", "Gocheok Dome", PlaceCategory.PARK),
        OfficialPlace("POI087", "광나루한강공원", "Gwangnaru Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI088", "광화문광장", "Gwanghwamun Square", PlaceCategory.PARK),
        OfficialPlace("POI089", "국립중앙박물관·용산가족공원", "The National Museum of Korea and Yongsan Family Park", PlaceCategory.PARK),
        OfficialPlace("POI090", "난지한강공원", "Nanji Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI091", "남산공원", "Namsan Park", PlaceCategory.PARK),
        OfficialPlace("POI092", "노들섬", "Nodeul Island", PlaceCategory.PARK),
        OfficialPlace("POI093", "뚝섬한강공원", "Ttukseom Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI094", "망원한강공원", "Mangwon Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI095", "반포한강공원", "Banpo Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI096", "북서울꿈의숲", "Dream Forest", PlaceCategory.PARK),
        OfficialPlace("POI098", "서리풀공원·몽마르뜨공원", "Seoripul Park and Montmartre Park", PlaceCategory.PARK),
        OfficialPlace("POI100", "서울대공원", "Seoul Grand Park", PlaceCategory.PARK),
        OfficialPlace("POI101", "서울숲공원", "Seoul Forest", PlaceCategory.PARK),
        OfficialPlace("POI102", "아차산", "Achasan", PlaceCategory.PARK),
        OfficialPlace("POI103", "양화한강공원", "Yanghwa Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI104", "어린이대공원", "Children's Grand Park", PlaceCategory.PARK),
        OfficialPlace("POI105", "여의도한강공원", "Yeouido Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI106", "월드컵공원", "World Cup Park", PlaceCategory.PARK),
        OfficialPlace("POI107", "응봉산", "Eungbongsan", PlaceCategory.PARK),
        OfficialPlace("POI108", "이촌한강공원", "Ichon Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI109", "잠실종합운동장", "Jamsil Sports Complex", PlaceCategory.PARK),
        OfficialPlace("POI110", "잠실한강공원", "Jamsil Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI111", "잠원한강공원", "Jamwon Hangang Park", PlaceCategory.PARK),
        OfficialPlace("POI112", "청계산", "Cheonggyesan", PlaceCategory.PARK),
        OfficialPlace("POI114", "북창동 먹자골목", "Bukchang-dong Food Alley", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI115", "남대문시장", "Namdaemun Market", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI116", "익선동", "Ikseon-dong", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI117", "신정네거리역", "Sinjeongnegeori Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI118", "잠실새내역", "Jamsil Saenae Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI119", "잠실역", "Jamsil Station", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI120", "잠실롯데타워·석촌호수", "Jamsil Lotte Tower and Seokchon Lake", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI121", "송리단길·호수단길", "Songridan-gil and Hosudan-gil", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI122", "신촌 스타광장", "Sinchon Star Plaza", PlaceCategory.COMMERCIAL),
        OfficialPlace("POI123", "보라매공원", "Boramae Park", PlaceCategory.PARK),
        OfficialPlace("POI124", "서대문독립공원", "Seodaemun Independence Park", PlaceCategory.PARK),
        OfficialPlace("POI125", "안양천", "Anyangcheon River", PlaceCategory.PARK),
        OfficialPlace("POI126", "여의서로", "Yeouiseoro", PlaceCategory.PARK),
        OfficialPlace("POI127", "올림픽공원", "Olympic Park", PlaceCategory.PARK),
        OfficialPlace("POI128", "홍제폭포", "Hongje Waterfall", PlaceCategory.PARK),
        OfficialPlace("POI129", "송현녹지광장", "Songhyeon Green Plaza", PlaceCategory.PARK),
        OfficialPlace("POI130", "시의회 앞", "City Council", PlaceCategory.CROWDED_AREA),
        OfficialPlace("POI131", "숭례문", "Sungnyemun Gate", PlaceCategory.CROWDED_AREA)
    )

    private val details = mapOf(
        "POI068" to PlaceDetail("성동구", "카페와 팝업이 모인 서울의 대표 핫플", 37.5446, 127.0559),
        "POI007" to PlaceDetail("마포구", "공연과 문화가 밤까지 이어지는 거리", 37.5563, 126.9237),
        "POI014" to PlaceDetail("강남구", "약속과 퇴근 인파가 교차하는 중심지", 37.4979, 127.0276),
        "POI001" to PlaceDetail("강남구", "코엑스와 봉은사를 함께 즐기는 도심 코스", 37.5117, 127.0592),
        "POI054" to PlaceDetail("종로구", "연극과 골목 산책을 즐기는 대학로", 37.5821, 127.0019),
        "POI101" to PlaceDetail("성동구", "도심 속에서 느긋하게 걷기 좋은 숲", 37.5444, 127.0374),
        "POI105" to PlaceDetail("영등포구", "강변 피크닉과 야경을 즐기는 공원", 37.5284, 126.9345),
        "POI094" to PlaceDetail("마포구", "시장 나들이와 함께 걷기 좋은 강변", 37.5524, 126.8998),
        "POI090" to PlaceDetail("마포구", "넓은 잔디와 캠핑장이 있는 한강 공원", 37.5689, 126.8763),
        "POI091" to PlaceDetail("중구", "서울 도심을 내려다보는 산책 코스", 37.5512, 126.9882),
        "POI009" to PlaceDetail("종로·중구", "궁궐과 광장을 잇는 서울 역사 산책", 37.5700, 126.9769),
        "POI008" to PlaceDetail("종로구", "조선의 중심을 만나는 대표 궁궐", 37.5796, 126.9770),
        "POI003" to PlaceDetail("중구", "쇼핑과 길거리 음식이 가득한 관광지", 37.5636, 126.9869),
        "POI066" to PlaceDetail("종로구", "한옥 골목 사이로 걷는 조용한 여행", 37.5826, 126.9830),
        "POI078" to PlaceDetail("종로구", "전통 공예와 찻집을 만나는 문화 거리", 37.5740, 126.9856),
        "POI116" to PlaceDetail("종로구", "한옥 사이 개성 있는 가게가 모인 골목", 37.5743, 126.9895),
        "POI002" to PlaceDetail("중구", "패션과 야시장을 밤까지 즐기는 곳", 37.5676, 127.0090),
        "POI060" to PlaceDetail("종로구", "서울의 맛과 활기를 만나는 전통시장", 37.5700, 126.9996),
        "POI115" to PlaceDetail("중구", "골목마다 다양한 물건과 먹거리가 가득한 시장", 37.5592, 126.9777)
    )

    val places: List<PlaceConfig> = officialPlaces.mapIndexed { index, place ->
        val detail = details[place.areaCode]
        PlaceConfig(
            areaCode = place.areaCode,
            areaName = place.areaName,
            englishName = place.englishName,
            category = place.category,
            displayOrder = index,
            district = detail?.district ?: "서울",
            tagline = detail?.tagline ?: "${place.areaName}의 실시간 혼잡도를 확인해 보세요",
            latitude = detail?.latitude,
            longitude = detail?.longitude
        )
    }

    fun seedSnapshots(now: Long): List<CrowdSnapshot> {
        return places.mapIndexed { index, place -> seedSnapshot(place, index, now) }
    }

    private fun seedSnapshot(place: PlaceConfig, index: Int, now: Long): CrowdSnapshot {
        val level = when ((index * 7 + 3) % 10) {
            0, 1 -> CrowdLevel.RELAXED
            in 2..5 -> CrowdLevel.NORMAL
            in 6..8 -> CrowdLevel.BUSY
            else -> CrowdLevel.CROWDED
        }
        val minPopulation = 2_000 + (((index * 7_919) % 42_000) / 1_000) * 1_000
        val maxPopulation = minPopulation + 2_000 + index % 3 * 1_000
        val previousPopulation = ((minPopulation + maxPopulation) / 2 + (index % 3 - 1) * 2_000)
            .coerceAtLeast(1_000)
        val trafficIndex = when (index % 3) {
            0 -> "원활"
            1 -> "서행"
            else -> "정체"
        }
        return CrowdSnapshot(
            areaCode = place.areaCode,
            areaName = place.areaName,
            level = level,
            minPopulation = minPopulation,
            maxPopulation = maxPopulation,
            previousMidPopulation = previousPopulation,
            crowdMessage = level.guidance,
            sourceUpdatedAt = now - (16L + index % 8) * 60_000L,
            fetchedAt = now - (12L + index % 10) * 60_000L,
            temperature = 25.0 + index % 6 * 0.7,
            skyStatus = if (index % 4 == 0) "구름 조금" else "맑음",
            pm25 = 14 + index % 12,
            pm25Level = if (index % 5 == 0) "보통" else "좋음",
            trafficIndex = trafficIndex,
            trafficSpeed = when (trafficIndex) {
                "원활" -> 27.0
                "서행" -> 18.0
                else -> 11.0
            },
            roadMessage = when (trafficIndex) {
                "원활" -> "주변 도로 흐름이 원활해요."
                "서행" -> "주변 도로에서 서행 구간이 있어요."
                else -> "진입에 시간이 더 걸릴 수 있어요."
            },
            fetchError = false,
            isDemo = true
        )
    }
}
