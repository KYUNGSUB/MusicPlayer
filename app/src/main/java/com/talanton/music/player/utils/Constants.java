package com.talanton.music.player.utils;

public class Constants {
	public static final String SP_NAME = "pref";
	public static final String TAG = "Classic";
	public static final String SERVICE_SERVER_URL = "http://talanton.kr";
//	public static final String SERVICE_SERVER_URL = "http://192.168.0.26:8080";
//	public static final String SERVICE_SERVER_URL = "http://192.168.0.34:8080";
	public static final String SIGNUP_URL = "/member/joinMember";
    public static final String DUPLICATE_CHECK_ID_URL = "/member/idCheck";
	public static final String SIGNIN_URL = "/member/loginPro";
	public static final String SIGNOUT_URL = "/member/logout";
	public static final String RETRIEVE_PARAMETER_URL = "/parameter/get?parameterName=classic_db";
	public static final int SIGNIN_INTENT_ID = 1000;
	public static final String KEY_DB_VERSION = "db_version";
	public static final String DOWNLOAD_FILE_URL = "/pds/download";
	public static final String GET_DATABASE_URL = "/pds/getDB";
	public static final String MUSIC_DIRECTORY = "MusicPlayer";
	public static final String CLASSIC_DB_NAME = "MusicListDB.db";
	public static final String SESSION_CHECK_URL = "/member/sessionCheck";

	public static final String BROADCAST_ACTION = "com.talanton.music.player.BroadcastEvent";
	public static final String SERVICE_COMMAND = "service_command";

	public static final int START_OF_MUSIC = 1;
	public static final int COMPLETION_OF_MUSIC = 3;
	public static final int PLAY_MUSIC_FAULT = 4;
	public static final int UPDATED_DOWNLOAD_INFO = 7;
	public static final int UPDATED_FAVORITELIST_INFO = 8;
	public static final int REMOVED_FAVORITELIST_INFO = 9;

	public static final int MAXIMUM_BACKGROUND_IMAGE = 40;
	public static final String LOGIN_SESSION_ID = "login_session_id";

	public static final int SIGNIN_REQUEST = 1;

	public static String makeURL(String detail) {
        return SERVICE_SERVER_URL + detail;
    }
}