package com.talanton.music.player.sub;

public class BookmarkInfo {
	public static final int START = 1;
	public static final int COMPLETE = 2;
	public static final int ERROR = 3;
	public static final int DELETED = 4;

	private int index;
	private String uuid;
	private String filename;
	private int state;		// 다운로드 상태 (1:Start, 2: Complete, 3: Deleted)
	
	public BookmarkInfo(int index, String uuid, String filename) {
		this.index = index;
		this.uuid = uuid;
		this.filename = filename;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}