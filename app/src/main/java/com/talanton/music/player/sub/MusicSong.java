package com.talanton.music.player.sub;

public class MusicSong {
	private String pid;				// product ID
	private String title;
	private String author;
	private String filename;
	private int filesize;
	private String timeinfo;
	private String urlinfo;
	private int download;
	private int bookmark;
	private float averageRate;
	private int myRate;
	private int rateCount;
	private int rateSum;
	private int postCount;
	private int playOrder;
	private int id;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getPid() {
		return pid;
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}

	public int getFilesize() {
		return filesize;
	}
	
	public void setFilesize(int filesize) {
		this.filesize = filesize;
	}
	
	public String getTimeinfo() {
		return timeinfo;
	}
	
	public void setTimeinfo(String timeinfo) {
		this.timeinfo = timeinfo;
	}
	
	public int getPlayOrder() {
		return playOrder;
	}
	
	public void setPlayOrder(int playOrder) {
		this.playOrder = playOrder;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getUrlinfo() {
		return urlinfo;
	}
	
	public void setUrlinfo(String urlinfo) {
		this.urlinfo = urlinfo;
	}
	
	public int getDownload() {
		return download;
	}
	
	public void setDownload(int download) {
		this.download = download;
	}
	
	public int getBookmark() {
		return bookmark;
	}
	
	public void setBookmark(int bookmark) {
		this.bookmark = bookmark;
	}

	public float getAverageRate() {
		return averageRate;
	}

	public void setAverageRate(float rate) {
		this.averageRate = rate;
	}

	public int getRateCount() {
		return rateCount;
	}

	public void setRateCount(int rateCount) {
		this.rateCount = rateCount;
	}

	public int getPostCount() {
		return postCount;
	}

	public void setPostCount(int postCount) {
		this.postCount = postCount;
	}

	public void setPid(int pid) {
		this.pid = String.valueOf(pid);
	}

	public int getRateSum() {
		return rateSum;
	}

	public void setRateSum(int rateSum) {
		this.rateSum = rateSum;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getFilename() {
		return filename;
	}

	public int getMyRate() {
		return myRate;
	}

	public void setMyRate(int myRate) {
		this.myRate = myRate;
	}
}