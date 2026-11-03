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

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private var _instance: CodeTrackApplication? = null

        var instance: CodeTrackApplication
            get() = _instance ?: throw IllegalStateException("CodeTrackApplication is not initialized")
            set(value) {
                _instance = value
            }

        val repository: CodeTrackRepository
            get() = instance.repository
    }
}
