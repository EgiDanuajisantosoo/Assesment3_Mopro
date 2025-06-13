package com.egidanuajisantoso.assessment3_mopro.network

import com.egidanuajisantoso.assessment3_mopro.model.MessageResponse
import com.egidanuajisantoso.assessment3_mopro.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

private const val BASE_URL = "https://ac96-36-69-194-228.ngrok-free.app/api/"


private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface GalleryApiService {
    @GET("items")
    suspend fun getGallery(
        @Header("Authorization") userId: String
    ): OpStatus

    @GET("items/all")
    suspend fun getAllGallery(): OpStatus

    @Multipart
    @POST("items")
    suspend fun postGallery(
        @Header("Authorization") userId: String,
        @Part("lokasi") lokasi: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("tanggal") tanggal: RequestBody,
        @Part gambar: MultipartBody.Part
    ): MessageResponse

    @Multipart
    @POST("items/{id}")
    suspend fun updateGallery(
        @Path("id") id: String,
        @Part("lokasi") lokasi: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("tanggal") tanggal: RequestBody,
        @Part gambar: MultipartBody.Part
    ): OpStatus


    @DELETE("items/{id}")
    suspend fun deleteGallery(
        @Path("id") id: String
    ): OpStatus
}

object GalleryApi{
    val service: GalleryApiService by lazy {
        retrofit.create(GalleryApiService::class.java)
    }

    enum class ApiStatus { LOADING, SUCCESS, FAILED }

    fun getGalleryUrl(gambar: String): String {
        return "${BASE_URL.replace("/api/", "/")}storage/$gambar"
    }
}