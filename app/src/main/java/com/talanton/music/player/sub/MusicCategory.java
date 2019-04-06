package com.talanton.music.player.sub;

public class MusicCategory {
	private int code;
	private String kname;
	private String ename;
	private int count;
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getKname() {
		return kname;
	}
	public void setKname(String name) {
		this.kname = name;
	}
	public String getEname() {
		return ename;
	}
	public void setEname(String name) {
		this.ename = name;
	}
}