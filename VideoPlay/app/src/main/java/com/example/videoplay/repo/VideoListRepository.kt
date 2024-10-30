package com.example.videoplay.repo

import com.example.videoplay.Api.MyAPI
import com.example.videoplay.data.VideoData
import com.example.videoplay.utils.AppConstants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VideoListRepository {
    private val api: MyAPI by lazy {
        Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyAPI::class.java)
    }

    suspend fun getAllVideos(): List<VideoData> {
        return api.getVideos()
    }

}