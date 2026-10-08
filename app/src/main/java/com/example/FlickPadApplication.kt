package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FlickPadApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { ProfileRepository(database.profileDao()) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.ensureDefaultProfilesExist()
        }
    }
}
