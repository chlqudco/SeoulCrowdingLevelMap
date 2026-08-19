# 서울 혼잡도 레이더

서울 주요 장소 19곳의 혼잡도를 비교하고 지금 덜 붐비는 장소를 찾는 Android 앱입니다.

## 주요 기능

- 혼잡도와 추정 인구 기반 TOP 5 및 전체 장소 정렬
- 핫플, 공원·산책, 관광, 쇼핑·상권 카테고리 필터
- 장소별 인구 변화, 날씨, 미세먼지, 교통 상세 정보
- DataStore 기반 TTL 캐시, 즐겨찾기, 자동 갱신 설정
- 일부 API 실패 시 마지막 캐시 유지 및 오래된 데이터 표시
- 지도 앱 연결

## API 키

`local.properties`에 서울 열린데이터광장 인증키를 추가하면 19개 장소를 실시간으로 조회합니다.

```properties
SEOUL_API_KEY=발급받은_인증키
```

키가 없으면 `sample` 키로 광화문·덕수궁을 조회하고 나머지 장소는 체험 데이터로 동작합니다.

## 실행

```powershell
.\gradlew.bat :app:assembleDebug
```

디버그 APK는 `app/build/outputs/apk/debug/app-debug.apk`에 생성됩니다.
