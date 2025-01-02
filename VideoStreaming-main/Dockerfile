FROM openjdk:21-slim

# Install FFmpeg and required dependencies
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY target/videoStream-0.0.1-SNAPSHOT.jar /app/app.jar

# Create directories for video storage
RUN mkdir -p /app/videos /app/hls /app/thumbnails

# Expose the port your application listens on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar","app.jar"]
