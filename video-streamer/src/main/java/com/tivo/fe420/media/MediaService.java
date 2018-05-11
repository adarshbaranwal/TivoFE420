package com.tivo.fe420.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class MediaService {
	private static final Logger logger = LoggerFactory.getLogger(MediaService.class);
	private static final String mediaFolder = "VideoLibrary";

	private static Gson gson = null;

	public MediaList getContent(String path) throws FileNotFoundException {
		if (gson == null) {
			gson = new Gson();
		}

		File folder = new File(mediaFolder + path);
		if(!folder.exists()) {
			throw new FileNotFoundException();
		}
		logger.info("Getting the content list on folder path " + mediaFolder + path);

		String[] listFiles = folder.list();
		List<Media> media = new ArrayList<Media>();

		for (String file : listFiles) {

			if ((new File(mediaFolder + path + "/" + file)).isDirectory()) {
				logger.debug(mediaFolder + path + "/" + file + " is a directory");
				media.add(new Media(file, "dir", null));
			} else if (file.toLowerCase().matches("^.*\\.mp4$")) {
				String name = file.substring(0, file.lastIndexOf('.')) + ".json";
				MetaData metaData = null;
				File jsonFile = new File(mediaFolder + path + "/" + name);
				if (jsonFile.exists()) {
					System.out.println("jsonFile.exists " + name);
					try {
						JsonReader jsonReader = new JsonReader(new FileReader(jsonFile));
						metaData = gson.fromJson(jsonReader, MetaData.class);
					} catch (FileNotFoundException e) {
						logger.debug(e.getMessage());
					}
				}
				if (metaData == null) {
					metaData = new MetaData(new SkipIntro(10, 40));
				}
				logger.debug("file: "+ file + " MetaData: {}", metaData);
				media.add(new Media(file, "file", metaData));
			}
		}
		MediaList mediaList = new MediaList(media, path);
		logger.info("returning mediaList {}", mediaList);
		return mediaList;
	}

	public ModelAndView streamVideo(String path) {
		String filePath = mediaFolder + path;
		logger.info("Stream request received for file: ", path);
		ModelAndView modelAndView = new ModelAndView("streamView", "movieName", filePath);
		return modelAndView;
	}
	
	public ResponseEntity<InputStreamResource> sendDataToClient(String path) throws FileNotFoundException {
		File mediaFile = new File(mediaFolder + path);
		logger.info("Request received for downloading file: " + path);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("Content-disposition", "attachment; filename=" + mediaFile.getName());
		return ResponseEntity.ok().headers(headers).contentLength(mediaFile.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(new InputStreamResource(new FileInputStream(mediaFile)));
	}
}
