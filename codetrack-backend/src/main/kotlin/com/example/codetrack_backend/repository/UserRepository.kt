package com.example.codetrack_backend.repository

import com.example.codetrack_backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByEmailIgnoreCase(email: String): User?

    fun existsByEmailIgnoreCase(email: String): Boolean
}