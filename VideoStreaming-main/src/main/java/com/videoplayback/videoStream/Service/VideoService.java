package com.videoplayback.videoStream.Service;

import com.videoplayback.videoStream.Entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

public interface VideoService {
    Video save(Video video, MultipartFile file) throws IOException;


    // get video by id
    Video get(String videoId);


    // get video by title

    Video getByTitle(String title);

    List<Video> getAll();

    //video processing
    Future<String> processVideo(String videoId);
    boolean isProcessingComplete(String videoId);

}
