package com.phgarcia.currencylayercc.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phgarcia.currencylayercc.database.room.CurrencylayerDatabase
import com.phgarcia.currencylayercc.repository.CurrencylayerRepository
import retrofit2.HttpException

class RefreshDataWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val logTag: String = RefreshDataWorker::class.java.simpleName

    companion object {
        const val WORK_NAME = "com.phgarcia.currencylayercc.work.RefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        val database = CurrencylayerDatabase.getInstance(applicationContext)
        val repository = CurrencylayerRepository(database)

        try {
            repository.refreshCurrencies()
            repository.refreshExchangeRates()
            Log.i(logTag, "Work request for data sync is run")
        } catch (e: HttpException) {
            return Result.retry()
        }

        return Result.success()
    }

}