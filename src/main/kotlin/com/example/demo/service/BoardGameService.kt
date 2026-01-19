package com.example.demo.service

import com.example.demo.dto.BoardGameDto
import com.example.demo.entity.GameEntity
import com.example.demo.enum.BoardGameCategory
import com.example.demo.enum.GameOptions
import com.example.demo.enum.GameOptions.*
import com.example.demo.repository.BoardGameRepository
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class BoardGameService(private val boardGameRepository: BoardGameRepository) {
    private val vectorCache = ConcurrentHashMap<Long, List<Double>>()
    private val weightVectorCache = ConcurrentHashMap<Long, List<Double>>()

    fun refreshCache() {
        val games: List<GameEntity> = boardGameRepository.findAll()
        for (game in games) {
            val vector: List<Double> = createGameVector(game)
            vectorCache.put(game.id!!, vector)
        }
    }

    fun weightRefreshCache(options: List<GameOptions>) {
        val games: List<GameEntity> = boardGameRepository.findAll()
        for (game in games) {
            val vector: List<Double> = createGameWeightVector(game, options)
            weightVectorCache.put(game.id!!, vector)
        }
    }

    fun saveGame(boardGameDtos: List<BoardGameDto>) {
        for (boardGameDto: BoardGameDto in boardGameDtos) {
            val categories: List<BoardGameCategory> = mappingCategories(boardGameDto.category)
            boardGameRepository.save(GameEntity(
                name = boardGameDto.name,
                minPlayers = boardGameDto.minPlayers,
                maxPlayers = boardGameDto.maxPlayers,
                difficulty = boardGameDto.difficulty,
                playTime = boardGameDto.playTime,
                categories = categories
            ))
        }
    }

    fun mappingCategories(category:String) : List<BoardGameCategory> {
        return category.split(",").map { it.trim() }.mapNotNull { c -> BoardGameCategory.find(c) }
    }

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

    fun searchBoardGame(name : String) : List<String> {
        val targetGame = boardGameRepository.findByName(name)
        val v1 = createGameVector(targetGame)
        val list = vectorCache.map { it.key to calculateCosineSimilarity(v1, it.value) }.filter { targetGame.id != it.first }.sortedByDescending { it.second }.take(3)
        val result = mutableListOf<String>()
        for (l in list) {
            val game: GameEntity = boardGameRepository.findById(l.first)?.orElse(null) ?: continue
            result.add(game.name)
        }
        return result
    }

    fun weightSearchBoardGame(name:String, options: List<GameOptions>) : List<String> {
        weightRefreshCache(options)
        val targetGame = boardGameRepository.findByName(name)
        val v1 = createGameWeightVector(targetGame, options)
        val list = weightVectorCache.map { it.key to calculateCosineSimilarity(v1, it.value) }.filter { targetGame.id != it.first }.sortedByDescending { it.second }.take(3)
        val result = mutableListOf<String>()
        for (l in list) {
            val game: GameEntity = boardGameRepository.findById(l.first)?.orElse(null) ?: continue
            result.add(game.name)
        }
        return result
    }
}