package com.example.demo.entity

import com.example.demo.enum.BoardGameCategory
import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "game")
class GameEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        val name: String = "",
        val minPlayers: Int = 0,
        val maxPlayers:Int = 0,
        val difficulty: Double = 0.0,
        val playTime: Int = 0,

        @ElementCollection(fetch = FetchType.EAGER)
        @Enumerated(EnumType.STRING)
        @CollectionTable(name = "game_category")
        val categories: List<BoardGameCategory> = mutableListOf()
    )
