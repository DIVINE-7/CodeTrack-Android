package com.example.codetrack.data.model

data class WeeklyStatistics(
    val dsaCompleted: Int,
    val dsaTarget: Int,
    val aptitudeCompleted: Int,
    val aptitudeTarget: Int,
    val interviewCompleted: Int,
    val interviewTarget: Int,
    val weeklyActivity: List<Int> // Monday to Sunday problem counts
) {
    val totalCompleted: Int = dsaCompleted + aptitudeCompleted + interviewCompleted
    val totalTarget: Int = dsaTarget + aptitudeTarget + interviewTarget
    
    val activeDays: Int = weeklyActivity.count { it > 0 }

    val completionPercentage: Float = if (totalTarget > 0) {
        totalCompleted.toFloat() / totalTarget
    } else {
        0f
    }
    
    val dsaProgress: Float = if (dsaTarget > 0) dsaCompleted.toFloat() / dsaTarget else 0f
    val aptitudeProgress: Float = if (aptitudeTarget > 0) aptitudeCompleted.toFloat() / aptitudeTarget else 0f
    val interviewProgress: Float = if (interviewTarget > 0) interviewCompleted.toFloat() / interviewTarget else 0f
}
