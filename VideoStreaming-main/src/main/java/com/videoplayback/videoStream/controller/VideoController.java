package com.videoplayback.videoStream.controller;

import com.videoplayback.videoStream.Entity.Video;
import com.videoplayback.videoStream.Payload.CustomMessage;
import com.videoplayback.videoStream.Service.VideoService;
import com.videoplayback.videoStream.exception.ResourceNotFoundException;
import com.videoplayback.videoStream.exception.VideoException;
import com.videoplayback.videoStream.response.VideoResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Slf4j
public class VideoController {
    private static final String CONTENT_TYPE_HLS = "application/vnd.apple.mpegurl";
    private static final String CONTENT_TYPE_MP2T = "video/mp2t";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";



    @Autowired
    private VideoService videoService;
    private final Logger logger = LoggerFactory.getLogger(VideoController.class);
    @Value("${files.video}")
    private String DIR;
    @Value("${files.video.hsl}")
    private String HSL_DIR;
    @Value("${files.thumbnail}")
    String THUMBNAIL_DIR;


    @PostMapping("/videos/add")
    public ResponseEntity<Object> createVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description) {
        try {
            log.info("Creating new video with title: {}", title);

            Video video = Video.builder()
                    .videoId(UUID.randomUUID().toString())
                    .title(title)
                    .description(description)
                    .build();

            Video savedVideo = videoService.save(video, file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(VideoResponse.fromEntity(savedVideo));

        } catch (Exception e) {
            log.error("Error creating video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomMessage(false, "Failed to create video: " + e.getMessage()));
        }
    }
    @GetMapping("/videos")
    public ResponseEntity<List<VideoResponse>> getAllVideos() {
        logger.debug("Fetching all videos");
        return ResponseEntity.ok(
                videoService.getAll().stream()
                        .map(VideoResponse::fromEntity)
                        .toList()
        );
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String videoId) {
        Video video = videoService.get(videoId);
        String contentType=video.getContentType();
        String filePath = video.getFilePath();
        if(contentType ==null){
            contentType="application/octet-stream";
        }
        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    //Send Videos In Required bytes


    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(@PathVariable String videoId,
                                                     @RequestHeader(value = "Range", required = false) String range) {


        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());

        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType() != null ? video.getContentType() : "application/octet-stream";
        long fileLength = path.toFile().length();
        System.out.println("Range Header: " + range+"-"+fileLength);

        if (range == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        long rangeStart;
        long rangeEnd;

        try {
            String[] ranges = range.replace("bytes=", "").split("-");
            rangeStart = Long.parseLong(ranges[0]);
            rangeEnd = ranges.length > 1 ? Long.parseLong(ranges[1]) : rangeStart + AppConstants.CHUNK_SIZE - 1;
            if (rangeEnd >= fileLength) {
                rangeEnd = fileLength - 1;
            }

            System.out.println("Range Start: " + rangeStart);
            System.out.println("Range End: " + rangeEnd);

            InputStream inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            byte[] data = new byte[(int) (rangeEnd - rangeStart + 1)];
            int bytesRead = inputStream.read(data);
            inputStream.close();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(bytesRead);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/stream/{videoId}/master.m3u8")
    public ResponseEntity<Resource> getHLSMasterPlaylist(@PathVariable String videoId) {
        Path playlistPath = Paths.get(HSL_DIR, videoId, "master.m3u8");
        return serveHLSResource(playlistPath, CONTENT_TYPE_HLS);
    }
    private ResponseEntity<Resource> serveHLSResource(Path path, String contentType) {
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Resource not found: " + path);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(new FileSystemResource(path));
    }
    @GetMapping("/stream/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> getSegmentFile(@PathVariable String videoId
    ,@PathVariable String segment){
        Path path = Paths.get(HSL_DIR,videoId,segment+".ts");
        if (!Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE,"video/mp2t")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(resource);

    }

    @GetMapping("/{videoId}")
    public ResponseEntity<Video> getVideo(@PathVariable String videoId) {
        logger.debug("Received request for video ID: {}", videoId);
        Video video = videoService.get(videoId);
        return ResponseEntity.ok(video);
    }
    @GetMapping("/thumbnails/{filename}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable String filename) {
        try {
            Path thumbnailPath = Paths.get(THUMBNAIL_DIR).resolve(filename);
            Resource resource = new UrlResource(thumbnailPath.toUri());

            if (!resource.exists()) {
                throw new ResourceNotFoundException("Thumbnail not found: " + filename);
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error fetching thumbnail {}: {}", filename, e.getMessage(), e);
            throw new VideoException("Error fetching thumbnail", e);
        }
    }


}
