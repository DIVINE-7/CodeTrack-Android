
package com.example.codetrack_backend.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "revisions")
class Revision(

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:Column(name = "revision_number", nullable = false)
    var revisionNumber: Int = 1,

    @field:Column(name = "revision_date", nullable = false)
    var revisionDate: LocalDate = LocalDate.now(),

    @field:Column(name = "completed")
    var completed: Boolean? = false,

    @field:Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @field:Column(name = "notes", columnDefinition = "text")
    var notes: String? = null,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(
        name = "problem_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_revision_problem")
    )
    var problem: Problem? = null
)
