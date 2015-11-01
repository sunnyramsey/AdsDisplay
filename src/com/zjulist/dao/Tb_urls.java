package com.zjulist.dao;

public class Tb_urls {
	private long id;
	private String url;
	public Tb_urls(String url)
	{
		this.url = url;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

}
