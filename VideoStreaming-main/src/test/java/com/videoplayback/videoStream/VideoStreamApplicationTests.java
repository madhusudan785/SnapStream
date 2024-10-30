package com.videoplayback.videoStream;

import com.videoplayback.videoStream.Entity.Video;
import com.videoplayback.videoStream.Service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VideoStreamApplicationTests {
	@Autowired
	VideoService videoService;

	@Test
	void contextLoads() {
		videoService.processVideo("891162b9-1fac-429a-bfdf-5695ee393bde");
	}

}
