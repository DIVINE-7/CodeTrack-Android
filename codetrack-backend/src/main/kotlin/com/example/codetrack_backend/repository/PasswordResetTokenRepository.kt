package com.example.codetrack_backend.repository

import com.example.codetrack_backend.entity.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PasswordResetTokenRepository :
    JpaRepository<PasswordResetToken, Long> {

    fun findByTokenHash(tokenHash: String): PasswordResetToken?

    fun deleteAllByUserId(userId: Long)
}