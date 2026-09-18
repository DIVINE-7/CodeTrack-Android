package com.example.codetrack

import android.app.Application
import com.example.codetrack.data.local.AppDatabase
import com.example.codetrack.data.repository.CodeTrackRepository

class CodeTrackApplication : Application() {

    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository by lazy {
        CodeTrackRepository(
            database.problemDao(),
            database.revisionDao()
        )
    }
}