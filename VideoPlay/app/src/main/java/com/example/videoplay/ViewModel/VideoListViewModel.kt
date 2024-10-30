package com.example.videoplay.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoplay.data.VideoData
import com.example.videoplay.repo.VideoListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoListViewModel : ViewModel() {
    private val videoRepository = VideoListRepository()

    private val _videos = MutableLiveData<List<VideoData>>()
    val videos: LiveData<List<VideoData>> get() = _videos

    init {
        fetchAllVideos()
    }

    fun fetchAllVideos() {
        viewModelScope.launch {
            try {
                val videoList = withContext(Dispatchers.IO) {
                    videoRepository.getAllVideos()
                }
                _videos.value = videoList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}