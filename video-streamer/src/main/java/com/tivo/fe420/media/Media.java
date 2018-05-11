package com.tivo.fe420.media;

public class Media {
	private String name;
	private String type;
	private MetaData metaData;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public MetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	public Media(String name, String type, MetaData metaData) {
		super();
		this.name = name;
		this.type = type;
		this.metaData = metaData;
	}

}
