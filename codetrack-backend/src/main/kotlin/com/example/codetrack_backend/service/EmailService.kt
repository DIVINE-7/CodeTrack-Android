package com.example.codetrack_backend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,

    @Value("\${spring.mail.username}")
    private val senderEmail: String
) {

    fun sendPasswordResetToken(email: String, token: String) {

        val message = SimpleMailMessage()

        message.from = senderEmail
        message.setTo(email)
        message.subject = "CodeTrack Password Reset"

        message.text = """
            Hello,

            We received a request to reset your CodeTrack password.

            Your password reset token is:

            $token

            This token expires in 15 minutes.

            If you did not request this, you can ignore this email.

            CodeTrack Team
        """.trimIndent()

        mailSender.send(message)
    }
}