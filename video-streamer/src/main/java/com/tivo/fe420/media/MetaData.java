package com.tivo.fe420.media;

public class MetaData {
	private SkipIntro skipIntro;

	public SkipIntro getSkipIntro() {
		return skipIntro;
	}

	public void setSkipIntro(SkipIntro skipIntro) {
		this.skipIntro = skipIntro;
	}

	public MetaData(SkipIntro skipIntro) {
		super();
		this.skipIntro = skipIntro;
	}

}
