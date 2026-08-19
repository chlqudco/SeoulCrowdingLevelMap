# 서울 혼잡도 레이더

서울시 실시간 도시데이터 공식 장소 121곳의 혼잡도를 비교하고 지금 덜 붐비는 장소를 찾는 Android 앱입니다.

## 주요 기능

- 혼잡도와 추정 인구 기반 TOP 5
- 장소명·영문명·장소 코드 검색
- 관광특구, 고궁·문화유산, 인구밀집지역, 발달상권, 공원 분류 필터
- 20개씩 총 7페이지 탐색 및 현재 페이지 단위 갱신
- 네이버 지도 기반 121개 장소 혼잡도 색상 마커, 검색·분류 필터, 20곳 단위 API 갱신 범위 선택
- 장소별 인구 변화, 날씨, 미세먼지, 교통 상세 정보
- DataStore 기반 TTL 캐시, 즐겨찾기, 자동 갱신 설정
- 일부 API 실패 시 마지막 캐시 유지 및 오래된 데이터 표시
- 지도 앱 연결

## API 키

`local.properties`에 서울 열린데이터광장 인증키를 추가하면 공식 121개 장소를 실시간으로 조회합니다. 한 번에 과도한 요청이 발생하지 않도록 현재 페이지의 최대 20개 장소만 갱신합니다.

```properties
SEOUL_API_KEY=발급받은_인증키
NAVER_MAP_KEY_ID=네이버_지도_키_ID
```

키가 없으면 `sample` 키로 광화문·덕수궁을 조회하고 나머지 120개 장소는 체험 데이터로 동작합니다.

네이버 클라우드 플랫폼 Maps 애플리케이션에서는 Dynamic Map을 선택하고 Android 앱 패키지에 `com.chlqudco.seoulcrowdinglevelmap`을 등록해야 합니다.

## 실행

```powershell
.\gradlew.bat :app:assembleDebug
```

디버그 APK는 `app/build/outputs/apk/debug/app-debug.apk`에 생성됩니다.
