package com.example.demo.enum

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