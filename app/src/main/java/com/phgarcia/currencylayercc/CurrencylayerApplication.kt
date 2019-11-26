package com.phgarcia.currencylayercc

import android.app.Application
import android.util.Log
import androidx.work.*
import com.phgarcia.currencylayercc.work.RefreshDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CurrencylayerApplication : Application() {

    private val logTag: String = CurrencylayerApplication::class.java.simpleName

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                RefreshDataWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build()
            )
            Log.i(logTag, "Scheduled data update Work request")
        }
    }

}