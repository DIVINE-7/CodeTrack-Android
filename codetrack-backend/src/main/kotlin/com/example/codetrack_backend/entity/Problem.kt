
package com.example.codetrack_backend.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "problems")
class Problem(

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:Column(name = "title", nullable = false, length = 255)
    var title: String = "",

    @field:Column(name = "platform", length = 100)
    var platform: String? = null,

    @field:Column(name = "topic", length = 100)
    var topic: String? = null,

    @field:Column(name = "difficulty", length = 50)
    var difficulty: String? = null,

    @field:Column(name = "problem_url", length = 500)
    var problemUrl: String? = null,

    @field:Column(name = "solved")
    var solved: Boolean? = false,

    @field:Column(name = "solved_date")
    var solvedDate: LocalDate? = null,

    @field:Column(name = "notes", columnDefinition = "text")
    var notes: String? = null,

    @field:Column(
        name = "created_at",
        insertable = false,
        updatable = false
    )
    var createdAt: LocalDateTime? = null,

    @field:OneToMany(
        mappedBy = "problem",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var revisions: MutableList<Revision> = mutableListOf()
)
