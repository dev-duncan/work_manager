package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R
import timber.log.Timber


class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val appContext = applicationContext

        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        makeStatusNotification("Blurring Image", appContext)

        sleep()

        try {

            if (TextUtils.isEmpty(resourceUri)){
                Timber.e("invalid Nput Uri")
                throw IllegalArgumentException("Invalid Input Uri")
            }

            val resolver = appContext.contentResolver

            val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
            )

            val output = blurBitmap(picture, appContext)

            val outPutUri =  writeBitmapToFile(appContext, picture)

            makeStatusNotification("Output is $outPutUri", appContext)

            val outPutData = workDataOf(KEY_IMAGE_URI to outPutUri.toString())
            Result.success(outPutData)

            return Result.success()

        }catch (throwable:Throwable){
            Timber.e(throwable, "Error applying blur")

        return Result.failure()}

    }
}