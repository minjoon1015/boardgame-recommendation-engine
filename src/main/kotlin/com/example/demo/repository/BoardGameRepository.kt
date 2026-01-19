package com.example.demo.repository

import com.example.demo.entity.GameEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BoardGameRepository : JpaRepository<GameEntity, Int?> {
    @Query("select * from game where name = :name", nativeQuery = true)
    fun findByName(@Param("name") name: String): GameEntity
    fun findById(id: Long): Optional<GameEntity?>?
}