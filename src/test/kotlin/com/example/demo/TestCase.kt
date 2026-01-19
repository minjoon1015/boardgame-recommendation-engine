package com.example.demo

import com.example.demo.dto.BoardGameDto
import com.example.demo.entity.GameEntity
import com.example.demo.enum.GameOptions
import com.example.demo.repository.BoardGameRepository
import com.example.demo.service.BoardGameService
import org.junit.jupiter.api.Test
import org.springframework.aot.hint.TypeReference.listOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import kotlin.test.BeforeTest

@SpringBootTest
class TestCase(
    @Autowired val boardGameService: BoardGameService,
    @Autowired val boardGameRepository: BoardGameRepository,
) {

    @BeforeTest
    fun setUp() {
        boardGameService.refreshCache()
        println("셋업 성공")
    }

    @Test
    @Rollback(false)
    fun saveBoardGame() {
        val dummyGames = listOf(
            BoardGameDto(name = "카르카손", category = "가족, 전략", minPlayers = 2, maxPlayers = 5, difficulty = 2.0, playTime = 35),
            BoardGameDto(name = "루미큐브", category = "가족", minPlayers = 2, maxPlayers = 4, difficulty = 1.5, playTime = 20),
            BoardGameDto(name = "아카디아 퀘스트", category = "전략, 추리", minPlayers = 2, maxPlayers = 4, difficulty = 2.5, playTime = 60),
            BoardGameDto(name = "다빈치 코드", category = "추리, 파티", minPlayers = 2, maxPlayers = 4, difficulty = 1.0, playTime = 15),
            BoardGameDto(name = "세븐 원더스 듀얼", category = "전략", minPlayers = 2, maxPlayers = 2, difficulty = 2.5, playTime = 30),
            BoardGameDto(name = "티켓 투 라이드", category = "가족, 전략", minPlayers = 2, maxPlayers = 5, difficulty = 2.0, playTime = 45),
            BoardGameDto(name = "코드네임", category = "파티, 추리", minPlayers = 2, maxPlayers = 8, difficulty = 1.5, playTime = 15),
            BoardGameDto(name = "아그리콜라", category = "전략", minPlayers = 1, maxPlayers = 4, difficulty = 3.5, playTime = 90),
            BoardGameDto(name = "윙스팬", category = "전략, 가족", minPlayers = 1, maxPlayers = 5, difficulty = 2.5, playTime = 50),
            BoardGameDto(name = "레지스탕스 아발론", category = "파티, 추리", minPlayers = 5, maxPlayers = 10, difficulty = 2.0, playTime = 30),
            BoardGameDto(name = "패치워크", category = "가족, 전략", minPlayers = 2, maxPlayers = 2, difficulty = 1.5, playTime = 20),
            BoardGameDto(name = "스컬", category = "파티", minPlayers = 3, maxPlayers = 6, difficulty = 1.0, playTime = 15),
            BoardGameDto(name = "푸드 체인 거물", category = "전략", minPlayers = 2, maxPlayers = 5, difficulty = 4.5, playTime = 150),
            BoardGameDto(name = "언락!", category = "추리, 가족", minPlayers = 1, maxPlayers = 6, difficulty = 2.0, playTime = 60),
            BoardGameDto(name = "자이푸르", category = "전략, 가족", minPlayers = 2, maxPlayers = 2, difficulty = 1.5, playTime = 30),
            BoardGameDto(name = "다크 소울 보드게임", category = "전략, 추리", minPlayers = 1, maxPlayers = 4, difficulty = 3.0, playTime = 100),
            BoardGameDto(name = "딕싯", category = "파티, 가족", minPlayers = 3, maxPlayers = 6, difficulty = 1.0, playTime = 30),
            BoardGameDto(name = "정령섬", category = "전략", minPlayers = 1, maxPlayers = 4, difficulty = 4.0, playTime = 120),
            BoardGameDto(name = "화이트홀 미스터리", category = "추리, 전략", minPlayers = 2, maxPlayers = 4, difficulty = 2.5, playTime = 45),
            BoardGameDto(name = "라스베가스", category = "파티, 가족", minPlayers = 2, maxPlayers = 5, difficulty = 1.0, playTime = 30)
        )

        boardGameService.saveGame(dummyGames)
        println("저장 완료!")
    }

    @Test
    fun createVectorTest() {
        val gameEntity: GameEntity = boardGameRepository.findById(2).orElse(null)
        val a: List<Double> =boardGameService.createGameVector(gameEntity)
        println(a)
    }

    @Test
    fun testSearchGame() {
        val games: List<String> = boardGameService.searchBoardGame("스컬")
        for (game in games) {
            println(game)
        }
    }

    @Test
    fun weightSearchGame() {
        val options = mutableListOf<GameOptions>()
        options.add(GameOptions.DIFFICULTY)
        options.add(GameOptions.CATEGORY)
        val games: List<String> = boardGameService.weightSearchBoardGame("뱅!", options)
        for (game in games) {
            println(game)
        }
    }

}