/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.app.Application
import android.graphics.BlurMaskFilter
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker


class BlurViewModel(application: Application) : AndroidViewModel(application) {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null
    private val workManger = WorkManager.getInstance(application)
    internal val outputWorkInfos: LiveData<List<WorkInfo>>
    init {
        outputWorkInfos = workManger.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    internal fun applyBlur(blurLevel: Int) {

        // Add WorkRequest to Cleanup temporary images
        val cleanRequest = OneTimeWorkRequest
                .from(CleanupWorker::class.java)

        var continuation = workManger
                .beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,ExistingWorkPolicy.REPLACE,cleanRequest)

        // Add WorkRequests to blur the image the number of times requested
        for (i in 0 until blurLevel){
            val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
                    if (i == 0){
                        blurRequest.setInputData(createInputDataForUri())

                    }
            continuation = continuation.then(blurRequest  .build())

        }

        // Add WorkRequest to save the image to the filesystem
        val saveRequest = OneTimeWorkRequest.Builder(SaveImageToFileWorker::class.java)
                .addTag(TAG_OUTPUT)
                .build()

       continuation = continuation.then(saveRequest)

        continuation.enqueue()


    }

    private fun createInputDataForUri():Data{
        val builder = Data.Builder()
        imageUri?.let{
            builder.putString(KEY_IMAGE_URI, imageUri.toString())

            }
        return builder.build()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * Setters
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }
}
