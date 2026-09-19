package com.example.codetrack.data.model

data class DailyProgress(
    val dsaCompleted: Int = 0,
    val dsaTarget: Int = 5,
    val aptitudeCompleted: Int = 0,
    val aptitudeTarget: Int = 10,
    val interviewCompleted: Int = 0,
    val interviewTarget: Int = 3
)
