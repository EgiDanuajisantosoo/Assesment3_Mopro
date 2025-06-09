package com.egidanuajisantoso.assessment3_mopro.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egidanuajisantoso.assessment3_mopro.model.Gallery
import com.egidanuajisantoso.assessment3_mopro.network.GalleryApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {
    var data = mutableStateOf(emptyList<Gallery>())
    private set

    var status = MutableStateFlow(GalleryApi.ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set


    fun retrieveData(userId: String) {
        Log.d("MainViewModel", "Retrieving data for user: $userId")
        viewModelScope.launch(Dispatchers.IO) {
            status.value = GalleryApi.ApiStatus.LOADING
            try {
                data.value = GalleryApi.service.getGallery(userId)
                status.value = GalleryApi.ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = GalleryApi.ApiStatus.FAILED
            }
        }
    }

    fun saveData(userId: String, lokasi: String, deskripsi: String,tanggal: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = GalleryApi.service.postGallery(
                    userId,
                    lokasi.toRequestBody("text/plain".toMediaTypeOrNull()),
                    deskripsi.toRequestBody("text/plain".toMediaTypeOrNull()),
                    tanggal.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )

                if (result.status == "success")
                    retrieveData(userId)
                else throw
                Exception(result.message)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part{
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(),0, byteArray.size)
        return MultipartBody.Part.createFormData("gambar", "image.jpg", requestBody)
    }

    fun clearMessage(){ errorMessage.value = null}
}