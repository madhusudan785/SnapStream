package com.videoplayback.videoStream.response;

import com.videoplayback.videoStream.Entity.Video;
import com.videoplayback.videoStream.Entity.VideoStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoResponse {
    private String videoId;
    private String title;
    private String description;
    private String thumbnail;
    private VideoStatus status;

    public static VideoResponse fromEntity(Video video) {
        return VideoResponse.builder()
                .videoId(video.getVideoId())
                .title(video.getTitle())
                .description(video.getDescription())
                .thumbnail(video.getThumbnail())
                .status(video.getStatus())
                .build();
    }
}

