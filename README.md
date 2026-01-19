**목표**  
: 실제 운영 가능한 보드게임 추천 엔진 MVP 구현

**주제**  
: 특정 보드게임 1개를 입력하면 그 게임과 가장 비슷한 TOP3를 반환하는 추천 엔진 구현

**요구사항**  

| 필드 | 설명 |
|------|------|
| id | 게임 ID |
| name | 게임 이름 |
| minPlayers | 최소 인원 |
| maxPlayers | 최대 인원 |
| difficulty | 난이도 (1.0 ~ 5.0) |
| playTime | 플레이 시간 (분 단위) |
| category | 게임 카테고리 (문자열) |

* category는 문자열이므로 숫자를 벡터로 변환하는 방식이 필요

**핵심 알고리즘 구현**  
- 조건 1. Cosine Similarity (코사인 유사도) 사용
- 조건 2. 사용자 선호 반영 추천 확장

---

## 핵심 로직 설계

### 1. 카테고리 enum 선언
- find 함수를 통해 한글과 내부 상수 간의 매핑 처리
```kotlin
enum class BoardGameCategory(val displayName: String) {
    STRATEGY("전략"),
    PARTY("파티"),
    FAMILY("가족"),
    MYSTERY("추리"),
    COOPERATIVE("협력"),
    DECK_BUILDING("덱빌딩"),
    ABSTRACT("추상전략"),
    WARGAME("워게임");

    companion object {
        fun find(name:String) = entries.find { it.displayName == name }
    }
}
```

### 2. 보드게임 데이터 벡터화 로직
- 카테고리의 경우 [전략, 가족, 추리] -> [1, 0, 0, 1] 와 같은 형태로 변환
- 필드 값 모든 수치를 0~1 범위로 변환하여 데이터 정규화 후 일차원 배열 생성

```kotlin
fun createGameVector(game: GameEntity) : List<Double> {
    val allCategory = BoardGameCategory.entries
    val categoryValue = allCategory.map {
        category -> if (game.categories.contains(category)) 1.0 else 0.0
    }

    val nomNumber = listOf(
        game.difficulty / 5.0,
        game.minPlayers / 1.0,
        game.maxPlayers / 10.0,
        game.playTime / 180.0
    )
    return categoryValue + nomNumber
}
```

### 3. 전역 변수 선언
- spring의 Bean의 경우 싱글톤 객체 -> 캐싱을 위해 전역 변수 선언
- HashMap의 경우 멀티 스레드가 전부 다 접근이 가능하므로 동시성 문제 발생
- ConcurrentHashMap 사용하여 해결
```kotlin
private val vectorCache = ConcurrentHashMap<Long, List<Double>>()
// HashMap -> 같은 칸 공유
// ConcurrentHashMap -> 데이터 저장 공간을 여러 구역으로 나눔, 버킷 > 스레드 수
```

### 4. 전체 데이터 벡터화 캐싱


```kotlin
fun refreshCache() {
    val games: List<GameEntity> = boardGameRepository.findAll()
    for (game in games) {
        val vector: List<Double> = createGameVector(game)
        vectorCache.put(game.id!!, vector)
    }
}
```

### 5. 코사인 유사도 계산 로직
- 분자 = v1, v2의 동일한 index 곱의 합
- 분모(루트)n = vn의 모든 인수 제곱의 합
- 분자 / (분모1 * 분모2)

```kotlin
fun calculateCosineSimilarity(v1: List<Double>, v2: List<Double>) : Double {
    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0

    for (i in v1.indices) {
        dotProduct += v1.get(i) * v2.get(i)
        normA += v1.get(i) * v1.get(i)
        normB += v2.get(i) * v2.get(i)
    }

    if (normA == 0.0 || normB == 0.0) return 0.0
    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB))
}
```

### 6. 가중치 세팅
- options: List<GameOptions> 옵션 리스트를 받아와서 해당 리스트에 해당하는 값들에 대해서 가중치 부여

```kotlin
fun createGameWeightVector(game: GameEntity, options: List<GameOptions>) : List<Double> {
    val allCategory = BoardGameCategory.entries

    var categoryWeight = 1.0
    var difficultyWeight = 1.0
    var minPlayersWeight = 1.0
    var maxPlayersWeight = 1.0
    var playTimeWeight = 1.0

    for (option in options) {
        when (option) {
            MINPLAYER -> minPlayersWeight = 3.0
            MAXPLAYER -> maxPlayersWeight = 3.0
            DIFFICULTY -> difficultyWeight = 3.0
            PLAYTIME -> playTimeWeight = 3.0
            CATEGORY -> categoryWeight = 3.0
        }
    }

    val categoryValue = allCategory.map {
        category -> if (game.categories.contains(category)) 1.0 * categoryWeight else 0.0
    }

    val nomNumber = listOf(
        game.difficulty / 5.0 * difficultyWeight,
        game.minPlayers / 1.0 * minPlayersWeight,
        game.maxPlayers / 10.0 * maxPlayersWeight,
        game.playTime / 180.0 * playTimeWeight
    )
    return categoryValue + nomNumber
}
```
