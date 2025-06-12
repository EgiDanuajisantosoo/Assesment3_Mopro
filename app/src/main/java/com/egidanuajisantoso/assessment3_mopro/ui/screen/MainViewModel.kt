package com.egidanuajisantoso.assessment3_mopro.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egidanuajisantoso.assessment3_mopro.model.Gallery
import com.egidanuajisantoso.assessment3_mopro.network.GalleryApi
import com.egidanuajisantoso.assessment3_mopro.network.GalleryApi.ApiStatus
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
        Log.d("MainViewModel", "Refreshing all gallery data. Triggered by UserID: '$userId'")
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                val response = GalleryApi.service.getAllGallery()

                if (response.status == "success") {
                    data.value = response.data ?: emptyList()
                    status.value = ApiStatus.SUCCESS

                } else {
                    throw Exception(response.message ?: "Pesan error tidak tersedia dari API")
                }
            } catch (e: Exception) {
                val errorMsg = "Gagal memuat data: ${e.message}"
                Log.e("MainViewModel", errorMsg, e)
                errorMessage.value = errorMsg
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun retrieveDataUser(userId: String) {
        Log.d("MainViewModel", "Refreshing all gallery data. Triggered by UserID: '$userId'")
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                val response = GalleryApi.service.getGallery(userId)

                if (response.status == "success") {
                    data.value = response.data ?: emptyList()
                    status.value = ApiStatus.SUCCESS

                } else {
                    throw Exception(response.message ?: "Pesan error tidak tersedia dari API")
                }
            } catch (e: Exception) {
                val errorMsg = "Gagal memuat data: ${e.message}"
                Log.e("MainViewModel", errorMsg, e)
                errorMessage.value = errorMsg
                status.value = ApiStatus.FAILED
            }
        }
    }


    fun saveData(userId: String, lokasi: String, deskripsi: String, tanggal: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Panggilan API kini menggunakan MessageResponse dan tidak akan crash
                val result = GalleryApi.service.postGallery(
                    userId,
                    lokasi.toRequestBody("text/plain".toMediaTypeOrNull()),
                    deskripsi.toRequestBody("text/plain".toMediaTypeOrNull()),
                    tanggal.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )

                if (result.status == "success") {
                    Log.d("MainViewModel", "Sukses menyimpan: ${result.data}")
                    retrieveData(userId) // Muat ulang semua data
                } else {
                    // Jika status dari server 'error', lempar pesan errornya
                    throw Exception(result.message ?: result.data)
                }
            } catch (e: Exception) {
                val errorMsg = "Gagal menyimpan data: ${e.message}"
                Log.e("MainViewModel", errorMsg, e)
                errorMessage.value = errorMsg
            }
        }
    }

    fun updateData(id: String, userId: String, lokasi: String, deskripsi: String, tanggal: String, bitmap: Bitmap) {
        Log.d("MainViewModel", "Updating data with ID: '$id' and UserID: '$userId' and Bitmap: '$bitmap' and Lokasi: '$lokasi' and Deskripsi: '$deskripsi' and Tanggal: '$tanggal'")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = GalleryApi.service.updateGallery(
                    id,
                    lokasi.toRequestBody("text/plain".toMediaTypeOrNull()),
                    deskripsi.toRequestBody("text/plain".toMediaTypeOrNull()),
                    tanggal.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )

                if (result.status == "success") {
                    retrieveData(userId)
                } else {
                    val errorMsg = "Gagal memperbarui data: ${result.message}"
                    Log.e("MainViewModel", errorMsg)
                    errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error saat memperbarui: ${e.message}"
                Log.e("MainViewModel", errorMsg, e)
                errorMessage.value = errorMsg
            }
        }
    }

    fun deleteData(id: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = GalleryApi.service.deleteGallery(id)

                if (result.status == "success") {
                    retrieveData(userId)
                } else {
                    val errorMsg = "Gagal menghapus data: ${result.message}"
                    Log.e("MainViewModel", errorMsg)
                    errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error saat menghapus: ${e.message}"
                Log.e("MainViewModel", errorMsg, e)
                errorMessage.value = errorMsg
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpeg".toMediaTypeOrNull(),
            0,
            byteArray.size
        )
        return MultipartBody.Part.createFormData("gambar", "image.jpg", requestBody)
    }

    fun clearMessage() {
        errorMessage.value = null
    }
}