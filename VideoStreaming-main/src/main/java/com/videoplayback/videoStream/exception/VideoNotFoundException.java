package com.videoplayback.videoStream.exception;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(String videoId) {
        super(String.format("Video with ID %s not found", videoId));
    }
}


