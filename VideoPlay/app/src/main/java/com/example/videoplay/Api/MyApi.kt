package com.example.videoplay.Api

import com.example.videoplay.data.VideoData
import com.example.videoplay.utils.AppConstants
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface MyAPI {
    @Multipart
    @POST("api/v1/videos/add")
    fun uploadVideo(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody
    ): Call<UploadResponse>
//    @GET("api/v1/stream/range/{videoId}")
//    suspend fun streamVideo(@Path("videoId") videoId: String): Response<ResponseBody>

    @GET("api/v1/stream/range/{videoId}")
    suspend fun streamVideo(
        @Path("videoId") videoId: String,
        @Header("Range") range: String
    ): Response<ResponseBody>

    @GET("/api/v1/videos")
    suspend fun getVideos(
    ): List<VideoData>


    companion object {
        operator fun invoke(): MyAPI {
            // Setup logging interceptor
            //helps to send large files
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS // or NONE
            }
            // Create OkHttpClient with logging
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Connection timeout
                .writeTimeout(360, TimeUnit.SECONDS)    // Writing timeout (upload)
                .readTimeout(120, TimeUnit.SECONDS)     // Reading timeout (server response)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(AppConstants.BASE_URL)
                .client(okHttpClient) // Use OkHttpClient with logging
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyAPI::class.java)
        }
    }
}
