
package com.example.codetrack_backend.dto

import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class RevisionRequest(

    @field:Positive(message = "Revision number must be greater than zero")
    val revisionNumber: Int = 1,

    val revisionDate: LocalDate,

    val completed: Boolean? = false,
    val notes: String? = null
)
