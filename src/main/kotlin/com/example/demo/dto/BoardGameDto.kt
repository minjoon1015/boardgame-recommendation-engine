package com.example.demo.dto

class BoardGameDto(
    val name:String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val difficulty: Double,
    val playTime: Int,
    val category: String
)