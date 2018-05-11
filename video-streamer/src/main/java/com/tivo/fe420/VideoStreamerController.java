package com.tivo.fe420;

import java.io.FileNotFoundException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tivo.fe420.media.MediaList;
import com.tivo.fe420.media.MediaService;

@RestController
public class VideoStreamerController {
	Logger logger = LoggerFactory.getLogger(VideoStreamerController.class);
	private MediaService mediaService;
	
	@PostConstruct
	public void init() {
		mediaService = new MediaService();
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = { "application/json" })
	public MediaList getList(@RequestParam(name = "path", required = false, defaultValue = "/") String path) throws FileNotFoundException {
		return mediaService.getContent(path);
	}
	
	@RequestMapping(value = "/getOffline", method = RequestMethod.GET, produces = { "application/json" })
	public ResponseEntity<InputStreamResource>  getMediaOffline(@RequestParam(name = "path", required = true) String path) throws FileNotFoundException {
		return mediaService.sendDataToClient(path);
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public ModelAndView streamMedia(@RequestParam(name = "path", required = true) String path) {
		return mediaService.streamVideo(path);
	}
}
