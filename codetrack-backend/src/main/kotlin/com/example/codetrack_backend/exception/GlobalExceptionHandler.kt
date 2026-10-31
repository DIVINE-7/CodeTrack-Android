
package com.example.codetrack_backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    // Handle missing resources
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        exception: ResourceNotFoundException
    ): ResponseEntity<Map<String, Any>> {

        val response = mapOf(
            "timestamp" to LocalDateTime.now(),
            "status" to 404,
            "error" to "Not Found",
            "message" to (exception.message ?: "Resource not found")
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    // Handle invalid request data
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        exception: MethodArgumentNotValidException
    ): ResponseEntity<Map<String, Any>> {

        val errors = exception.bindingResult.fieldErrors.map {
            mapOf(
                "field" to it.field,
                "message" to (it.defaultMessage ?: "Invalid value")
            )
        }

        val response = mapOf(
            "timestamp" to LocalDateTime.now(),
            "status" to 400,
            "error" to "Bad Request",
            "messages" to errors
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
}
