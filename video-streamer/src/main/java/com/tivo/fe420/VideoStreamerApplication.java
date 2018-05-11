package com.tivo.fe420;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.tivo.fe420"})
public class VideoStreamerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoStreamerApplication.class, args);
	}

}
