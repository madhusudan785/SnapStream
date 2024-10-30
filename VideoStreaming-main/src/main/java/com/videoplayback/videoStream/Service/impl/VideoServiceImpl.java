package com.videoplayback.videoStream.Service.impl;

import com.videoplayback.videoStream.Entity.Video;
import com.videoplayback.videoStream.Entity.VideoStatus;
import com.videoplayback.videoStream.Reposetory.VideoRepo;
import com.videoplayback.videoStream.Service.VideoService;
import com.videoplayback.videoStream.exception.VideoNotFoundException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoRepo videoRepo;
    private final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

    public VideoServiceImpl(VideoRepo videoRepo) {
        this.videoRepo = videoRepo;
    }

    @Value("${files.video}")
    String DIR;

    @Value("${files.video.hsl}")
    String HSL_DIR;

    @Value("${files.thumbnail}")
    String THUMBNAIL_DIR;

    @PostConstruct
    public void init() {
        createDirectories(DIR);
        createDirectories(HSL_DIR);
        createDirectories(THUMBNAIL_DIR);
    }

    private void createDirectories(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Directory created: " + dirPath);
            } else {
                System.err.println("Failed to create directory: " + dirPath);
            }
        }
    }

    private String generateThumbnail(String videoPath, String videoId) throws IOException, InterruptedException {
        String thumbnailFileName = videoId + "_thumb.jpg";
        Path thumbnailPath = Paths.get(THUMBNAIL_DIR, thumbnailFileName);

        // FFmpeg command to extract thumbnail at 1 second mark
        String ffmpegCmd = String.format(
                "ffmpeg -i \"%s\" -ss 00:00:01.000 -vframes 1 -f image2 \"%s\"",
                videoPath,
                thumbnailPath.toString()
        );

        ProcessBuilder processBuilder;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
        } else {
            processBuilder = new ProcessBuilder("sh", "-c", ffmpegCmd);
        }

        processBuilder.inheritIO();
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Thumbnail generation failed");
        }

        return thumbnailFileName;
    }

    @Override
    public Video save(Video video, MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            String cleanFileName = StringUtils.cleanPath(fileName);
            String cleanFolder = StringUtils.cleanPath(DIR);
            Path videoPath = Paths.get(cleanFolder, cleanFileName);
            Files.copy(inputStream, videoPath, StandardCopyOption.REPLACE_EXISTING);

            // Generate thumbnail from video
            String thumbnailFileName = generateThumbnail(videoPath.toString(), video.getVideoId());

            // Set video details
            video.setContentType(contentType);
            video.setFilePath(videoPath.toString());
            video.setThumbnail(thumbnailFileName); // Store just the filename

            // Save video entity
            videoRepo.save(video);

            // Start video processing asynchronously
            processVideo(video.getVideoId());

            return video;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to save video and generate thumbnail", e);
        }
    }

    @Override
    public Video get(String videoId) {
        logger.debug("Fetching video with ID: {}", videoId);

        return videoRepo.findById(videoId)
                .orElseThrow(() -> {
                    logger.error("Video not found with ID: {}", videoId);
                    return new VideoNotFoundException(videoId);
                });

    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return videoRepo.findAll();
    }

    @Override
    @Async
    public Future<String> processVideo(String videoId) {
        Video video = this.get(videoId);
        String filePath = video.getFilePath();
        Path path = Paths.get(filePath);

        try {
            Path outputPath = Paths.get(HSL_DIR, videoId);
            Files.createDirectories(outputPath);

            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\" \"%s/master.m3u8\"",
                    path.toString(), outputPath.toString(), outputPath.toString()
            );

            ProcessBuilder processBuilder;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            } else {
                processBuilder = new ProcessBuilder("sh", "-c", ffmpegCmd);
            }
            processBuilder.inheritIO();
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                video.setStatus(VideoStatus.FAILED);
                videoRepo.save(video);
                throw new RuntimeException("Video processing failed!!");
            }

            video.setStatus(VideoStatus.COMPLETED);
            videoRepo.save(video);

            return new AsyncResult<>(videoId);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            video.setStatus(VideoStatus.FAILED);
            videoRepo.save(video);
            throw new RuntimeException("Failed to start video processing", e);
        }
    }

    @Override
    public boolean isProcessingComplete(String videoId) {
        Video video = videoRepo.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
        return video.getStatus() == VideoStatus.COMPLETED || video.getStatus() == VideoStatus.FAILED;
    }
}