package com.example.videoplay.Api

data class UploadResponse(
    val error: Boolean,
    val message: String,
    val video: String,
)