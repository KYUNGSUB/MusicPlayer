package com.talanton.music.player.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

import com.talanton.music.player.utils.Constants;

public class MusicUtils {
	public static void saveConfig(Context context , String name , Object value){
		
		if (value == null) {
			return;
		}
		
		SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		
		if ( value instanceof String) {
			editor.putString(name, (String)value);
		} else if ( value instanceof Integer) {
			editor.putInt(name, (Integer)value);
		} else if ( value instanceof Boolean) {
			editor.putBoolean(name, (Boolean)value);
		} else if ( value instanceof Long) {
			editor.putLong(name, (Long)value);
		}
		
		editor.commit();
	}

	public static boolean getConfigBoolean(Context context , String name, boolean defaultVal ){
		SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
		return sp.getBoolean(name, defaultVal);
	}

	public static float getConfigFloat(Context context, String name, float defaultVal) {
		SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
		return sp.getFloat(name, defaultVal);
	}
	
	public static int getConfigInteger(Context context , String name, int defaultVal ){
		SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
		return sp.getInt(name, defaultVal);
	}

	public static String getConfigString(Context context , String name, String defaultVal ){
		SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
		return sp.getString(name, defaultVal);
	}

	public static String getMusicExternalStorageDirectoy() {
		StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append("/").append(Constants.MUSIC_DIRECTORY);
		return sb.toString();
	}

	public static String getDownloadExternalStorageDirectoy() {
		StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append("/").append(Constants.MUSIC_DIRECTORY).append("/download");
		return sb.toString();
	}
}