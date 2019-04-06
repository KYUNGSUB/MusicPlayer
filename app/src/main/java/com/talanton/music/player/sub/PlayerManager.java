package com.talanton.music.player.sub;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class PlayerManager {
	public static final int STOP_STATE = 0;
	public static final int RUNNING_STATE = 1;
	public static final int PAUSE_STATE = 2;
	
	public static final int REPEAT_OFF = 0;
	public static final int REPEAT_ALL = 1;
	public static final int REPEAT_ONCE = 2;
	public static final int SINGLE_MUSIC = 4;

	public static final int PLAY_START = 1;		// 음악 감상 시작
	public static final int PLAY_COMPLETE = 2;		// 음악 감상 완료
	public static final int PLAY_ERROR = 3;		// MediaPlayer Error
	public static final int PLAY_NEXT = 4;			// Next Button press
	public static final int PLAY_PREVIOUS = 5;		// Previous button press
	public static final int PLAY_STOP = 6;			// stop button press

	public static final int START_OF_MUSIC = 1;
	public static final int ONE_SECOND_TIMEOUT = 2;
	public static final int COMPLETION_OF_MUSIC = 3;
	public static final int PLAY_MUSIC_FAULT = 4;
	public static final int FINISH_MUSIC_PLAY = 5;
	public static final int COMPLETION_OF_ONE_MINUTE = 6;

	private static PlayerManager sInstance;
	private ArrayList<MusicSong> musicInfos;
	private int playState;
	private String language;
	private boolean shuffle_flag;	// off(false), on(true)
	private int repeat_state;
	
	private int apiVersion;

	private String pOid;		// Play 상태 저장을 위한 Object ID

	public String getpOid() {
		return pOid;
	}

	public void setpOid(String pOid) {
		this.pOid = pOid;
	}

	public static PlayerManager getInstance() {
		if(sInstance == null) {
			sInstance = new PlayerManager();
		}
		return sInstance;
	}
	
	/**
	 * init() : 1. initialize music player operation state
	 *          2. store favorite music list into local list fields from DB
	 *  */
	public void init() {
		musicInfos = new ArrayList<MusicSong>();
		setPlayState(STOP_STATE);
		Locale lo = Locale.getDefault();
		language = lo.getLanguage();
		shuffle_flag = false;
		repeat_state = 0;
		apiVersion = android.os.Build.VERSION.SDK_INT;
	}
	
	public int getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(int apiVersion) {
		this.apiVersion = apiVersion;
	}

	public int getRepeat_state() {
		return repeat_state;
	}

	public void setRepeat_state(int repeat_state) {
		this.repeat_state = repeat_state;
	}

	public boolean isShuffle_flag() {
		return shuffle_flag;
	}

	public void setShuffle_flag(boolean shuffle_flag) {
		this.shuffle_flag = shuffle_flag;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public ArrayList<MusicSong> getMusicInfos() {
		return musicInfos;
	}

	public void setMusicInfos(ArrayList<MusicSong> msList) {
		this.musicInfos = msList;
	}

	public int getPlayState() {
		return playState;
	}

	public void setPlayState(int playState) {
		this.playState = playState;
	}
	
	public void makeShuffleIndex() {
		int nofMusic = musicInfos.size();
//		Log.i(TAG, "makeShuffleIndex(shuffle mode = " + MainService.mShuffleMode + ", nofMusic = " + nofMusic + ")");
		if(isShuffle_flag() == false) {
			for(int i = 0;i < nofMusic;i++) {
				musicInfos.get(i).setPlayOrder(i);
			}
		}
		else {
			Random rnd = new Random();
			for(int i = 0;i < nofMusic;i++) {
				boolean exit = false;
				int possible;
				do {
					possible = rnd.nextInt(nofMusic);
					int j;
					for(j = 0;j < i;j++) {
						if(possible == musicInfos.get(j).getPlayOrder()) {
							break;
						}
					}
					if(j == i) {
						exit = true;
					}
				}
				while (exit == false);
				musicInfos.get(i).setPlayOrder(possible);
			}
		}
	}
}