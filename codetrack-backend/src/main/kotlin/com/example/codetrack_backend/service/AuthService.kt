package com.example.codetrack_backend.service

import com.example.codetrack_backend.dto.*
import com.example.codetrack_backend.entity.PasswordResetToken
import com.example.codetrack_backend.entity.User
import com.example.codetrack_backend.repository.PasswordResetTokenRepository
import com.example.codetrack_backend.repository.UserRepository

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val resetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtEncoder: JwtEncoder,
    private val emailService: EmailService
) {

    // Safely encode passwords and guarantee a non-null String.
    private fun encodePassword(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
            ?: throw IllegalStateException("Password encoding failed")
    }

    // REGISTER
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {

        val email = request.email.trim().lowercase()

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "An account with this email already exists"
            )
        }

        val user = User(
            name = request.name.trim(),
            email = email,
            password = encodePassword(request.password)
        )

        val savedUser = userRepository.save(user)

        return createAuthResponse(savedUser)
    }

    // LOGIN
    fun login(request: LoginRequest): AuthResponse {

        val user = userRepository.findByEmailIgnoreCase(
            request.email.trim()
        ) ?: throw ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password"
        )

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
            )
        }

        return createAuthResponse(user)
    }

    // GENERATE JWT TOKEN
    private fun createAuthResponse(user: User): AuthResponse {

        val userId = user.id
            ?: throw IllegalStateException("User ID is missing")

        val now = Instant.now()
        val expiresAt = now.plus(24, ChronoUnit.HOURS)

        val claims = JwtClaimsSet.builder()
            .issuer("codetrack")
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(userId.toString())
            .claim("email", user.email)
            .claim("name", user.name)
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()

        val token = jwtEncoder.encode(
            JwtEncoderParameters.from(header, claims)
        ).tokenValue

        return AuthResponse(
            message = "Authentication successful",
            token = token,
            tokenType = "Bearer",
            userId = userId,
            name = user.name,
            email = user.email
        )
    }

    // FORGOT PASSWORD
    @Transactional
    fun forgotPassword(
        request: ForgotPasswordRequest
    ): MessageResponse {

        val genericMessage =
            "If an account exists with this email, a reset token has been sent."

        val user = userRepository.findByEmailIgnoreCase(
            request.email.trim()
        ) ?: return MessageResponse(genericMessage)

        val userId = user.id
            ?: throw IllegalStateException("User ID is missing")

        // Delete previously generated reset tokens.
        resetTokenRepository.deleteAllByUserId(userId)

        // Generate a secure random token.
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)

        val rawToken = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes)

        // Store only the SHA-256 hash of the token.
        val tokenHash = sha256(rawToken)

        val resetToken = PasswordResetToken(
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )

        resetTokenRepository.save(resetToken)

        // Send the original token through email.
        emailService.sendPasswordResetToken(
            user.email,
            rawToken
        )

        return MessageResponse(genericMessage)
    }

    // RESET PASSWORD
    @Transactional
    fun resetPassword(
        request: ResetPasswordRequest
    ): MessageResponse {

        val user = userRepository.findByEmailIgnoreCase(
            request.email.trim()
        ) ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid or expired reset token"
        )

        val userId = user.id
            ?: throw IllegalStateException("User ID is missing")

        // Hash the received reset token.
        val tokenHash = sha256(request.token)

        val resetToken = resetTokenRepository.findByTokenHash(tokenHash)
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid or expired reset token"
            )

        // Validate token ownership and expiration.
        if (
            resetToken.userId != userId ||
            resetToken.usedAt != null ||
            !resetToken.expiresAt.isAfter(LocalDateTime.now())
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid or expired reset token"
            )
        }

        // Update the user's password.
        user.password = encodePassword(request.newPassword)

        userRepository.save(user)

        // Invalidate all reset tokens for this user.
        resetTokenRepository.deleteAllByUserId(userId)

        return MessageResponse(
            "Password reset successfully"
        )
    }

    // SHA-256 TOKEN HASHING
    private fun sha256(value: String): String {

        val digest = MessageDigest.getInstance("SHA-256")

        val hash = digest.digest(
            value.toByteArray(Charsets.UTF_8)
        )

        return hash.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }
}