**Video Streaming Application**
This repository contains the backend and Frontend implementation of a Video Streaming Application. The backend is built using Spring Boot and frontend uses Kotlin and xml  it includes features like video upload, thumbnail generation, and HLS video streaming.
In frontend, the list of videos that are available on the server is shown to the users on their screens.
The application is containerized using Docker, with FFmpeg integrated for video processing tasks such as thumbnail generation and HLS segmentation.

**Features**
- Upload videos with metadata (title, description).
- Generate video thumbnails dynamically using FFmpeg.
- Process videos into HLS format for adaptive streaming.
- Retrieve video information and stream videos using REST APIs.
- Containerized with Docker for cross-platform compatibility.
- Store video details using Retrofit and Gson for JSON serialization/deserialization.
- Real-time video upload progress indication.
**Technologies Used**
->Backend
- Spring Boot: Handles video upload API and database operations.
- MySQL: Stores metadata related to uploaded videos.
- FFmpeg: Processes video files for streaming (optional).
->Android Frontend
- Kotlin: Primary language for app development.
- Retrofit: For making HTTP requests to the backend API.
- Gson: For JSON parsing and serialization.
- View Binding: Simplifies view interaction in the Activity.
- Video Upload
- URL: POST /api/v1/videos/add
- Description: Upload a video with metadata.
- Body:
json
{
  "file": "<Multipart File>",
  "title": "Sample Title",
  "description": "Sample Description"
}
- Response:
json
{
  "success": true,
  "message": "Video uploaded successfully"
}
- Get All Videos
- URL: GET /api/v1/videos
- Description: Retrieve all videos with metadata.
- Stream Video
- URL: GET /api/v1/stream/{videoId}
- Description: Stream a video by its ID.
- Stream Video in Bytes
- URL: GET /api/v1/stream/range/{videoId}
- Description: Stream video in chunks using byte ranges.
- Get Video Thumbnail
- URL: GET /api/v1/thumbnails/{filename}
- Description: Retrieve a video thumbnail by its filename.

-Docker Hub Image
Pull the Docker image for this backend from Docker Hub:

bash
- docker pull madhusudan785/videostreamingap-backend
Get All Videos
<img width="899" alt="Screenshot 2025-01-02 203700" src="https://github.com/user-attachments/assets/7503c6f1-e8e3-499c-9654-5f00dea1fc68" />
<img width="876" alt="Screenshot 2025-01-02 203427" src="https://github.com/user-attachments/assets/5c720343-c16b-4fcf-baa8-be1251215c50" />



