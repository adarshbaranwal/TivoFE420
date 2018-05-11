package com.tivo.fe420.media;

import java.util.List;

public class MediaList {

	List <Media> media;
	String path;
	
	public MediaList(List <Media> media, String path) {
		this.media = media;
		this.path = path;
	}

	public List<Media> getMedia() {
		return media;
	}

	public void setMedia(List<Media> media) {
		this.media = media;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


}
