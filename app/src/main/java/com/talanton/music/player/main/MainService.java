package com.talanton.music.player.main;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.talanton.music.player.R;
import com.talanton.music.player.db.MusicListDBHelper;
import com.talanton.music.player.interwork.ServerInterworking;
import com.talanton.music.player.sub.MusicSong;
import com.talanton.music.player.sub.PlayerManager;
import com.talanton.music.player.utils.Constants;
import com.talanton.music.player.utils.MusicUtils;


import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service implements MediaPlayer.OnPreparedListener,
													MediaPlayer.OnErrorListener {
	private static final String TAG = "Classic";
	public static final String BROADCAST_ACTION="com.talanton.music.prenatal.TestEvent";
	protected static final String POSITION_OF_MUSICLIST = "position_of_playlist";
	protected static final String SERVICE_COMMAND = "service_command";
	static final int SINGLE_MUSIC = 4;
	Handler mHandler = new Handler();
	public static int mMusicIndex;
	
	private static int mBackgroundIndex;
	private final IBinder mBinder = new LocalBinder();
	
	MediaPlayer mPlayer;
//	private Equalizer mEqualizer;
	int defaultVal[] = { 300, 0, 0, 0, 300 };
//	private static boolean mShuffleMode;		// Shuffle mode : off(false), on(true) 
//	private static int mRepeatMode;		// Repeat mode : 0(repeat off), 1(repeat all), 2(repeat this)
	private boolean onlyOne;
	
	static final String COMMAND_CODE = "service_command";
//	private static final StFring TIMEOUT_TYPE = "timeout_type";
	protected static final String NOTIFICATION_TIME = "notification_time";
	
	static final int COMMAND_PLAY_FIRST_MUSIC = 140;
	static final int COMMAND_CONTINUOUS_MUSIC_PLAY = 141;
	static final int COMMAND_PAUSE_MUSIC_PLAY = 142;
	static final int COMMAND_PREVIOUS_MUSIC_PLAY = 143;
	static final int COMMAND_NEXT_MUSIC_PLAY = 144;
	static final int COMMAND_STOP_MUSIC_PLAY = 145;
	static final int COMMAND_REPLAY_MUSIC = 146;
	static final int COMMAND_PLAY_SINGLE_MUSIC = 147;
	static final int COMMAND_MUSIC_FILE_AUDIT = 148;
	private static final long MUSIC_DISPLAY_INTERVAL = 1000L;
	private static final long DISPLAY_DURATION_OFFSET = 0L;
	public static final String MUSIC_INDEX = "music_index";
	private static final int ID_BACKGROUND_SERVICE_RUNNING = 200;
//	private static final String PREFERENCE_FILE_NAME = "musicplayer.txt";
	protected static final String KEY_SHUFFLE_MODE = "shuffle_mode";
	protected static final String KEY_REPEAT_MODE = "repeat_mode";
	protected static final boolean ONLY_ONE = true;
	
	private boolean amFlag = false;
	protected MusicListDBHelper musicDB = null;
	FileInputStream fis = null;
	
	private ConnectivityManager mCM;
	private NetworkInfo activeNetwork;
	private WifiLock mWifiLock;
	private TelephonyManager telephonyManager;
	private Timer myTimer;
	
	private PlayerManager pm;
	private ArrayList<MusicSong> msList;

	private ServerInterworking mSi;
    private String mOid;

	@Override
	public void onCreate() {
		super.onCreate();
//		Log.i(TAG, "MainService:onCreate()");
		
		pm = PlayerManager.getInstance();
		msList = pm.getMusicInfos();

		mBackgroundIndex = 0;
		MusicUtils.saveConfig(getApplicationContext(), getString(R.string.key_background_display_mode), true);
		
		mCM = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiLock = ((WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
		telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		activateServiceRunNotificationTimer();
	}

    public class LocalBinder extends Binder {
        public MainService getService() {
//        	Log.i(TAG, "MainService:getService()");
            return MainService.this;
        }
    }
	
	private boolean isConnected() {
		activeNetwork = mCM.getActiveNetworkInfo();
		if(activeNetwork == null)
			return false;
		return activeNetwork.isConnectedOrConnecting();
	}
	
	private boolean isWiFi() {
		activeNetwork = mCM.getActiveNetworkInfo();
		return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
	}
	
	private void activateServiceRunNotificationTimer() {
//		Log.i(TAG, "MainService:activateServiceRunNotificationTimer");
		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		Intent intent = new Intent(MainService.this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(MainService.this, 0, intent, 0);
		Notification noti;
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		StringBuilder sb = new StringBuilder(getString(R.string.app_name)).append(" ").append(getString(R.string.label_is_running));
		noti = builder.setContentIntent(pi)
				.setSmallIcon(R.drawable.stat_notify_alarm).setTicker(getString(R.string.app_name) + " " +
						getString(R.string.run_service_notification)).setWhen(System.currentTimeMillis())
				.setAutoCancel(true).setContentTitle(getString(R.string.run_service_notification))
				.setContentText(sb.toString()).build();
		nm.notify(ID_BACKGROUND_SERVICE_RUNNING, noti);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(TAG, "MainService:onStart(startId = " + startId + ")");
		
		if(intent == null) {	// MainService Restart
			return;
		}
		
		int mCommand = intent.getIntExtra(COMMAND_CODE, 0);
//		Log.i(TAG, "Command = " + mCommand);
		
		switch(mCommand) {
		case COMMAND_PLAY_FIRST_MUSIC: {
			playFirstMusic();
			break;
		}
		case COMMAND_NEXT_MUSIC_PLAY: {
			nextMusicPlay();
			break;
		}
		case COMMAND_PREVIOUS_MUSIC_PLAY: {
			previousMusicPlay();
			break;
		}
		case COMMAND_CONTINUOUS_MUSIC_PLAY: {
			continueMusicPlay();
			break;
		}
		case COMMAND_PAUSE_MUSIC_PLAY: {
			pauseMusicPlay();
			break;
		}
		case COMMAND_STOP_MUSIC_PLAY: {
			stop();
			break;
		}
		case COMMAND_REPLAY_MUSIC: {
			playMusicByIndex(false);
			break;
		}
		case COMMAND_PLAY_SINGLE_MUSIC: {
			int songId = intent.getIntExtra(MUSIC_INDEX, 0);
			playSingleMusic(songId);
			break;
		}
		default: {
//			Log.i(DTAG, "MainService:onStart:default()");
			break;
		}
		}
	}
    
	public void playSingleMusic(int songId) {
//    	Log.i(TAG, "MainService:playSingleMusic()");
    	mMusicIndex = songId;
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    	Thread thread = new Thread(null, doPlaySingleMusicBP, "SingleMusic");
		thread.start();
	}

	private Runnable doPlaySingleMusicBP = new Runnable() {
		public void run() {
			mPlayer = new MediaPlayer();
			playMusicByIndex(ONLY_ONE);		// only one music play
		}
    };

	PhoneStateListener callStateListener = new PhoneStateListener() {
    	private int phoneStateWas = -1;		// initial
    	public void onCallStateChanged(int state, String incomingNumber) {
//    		Log.d(DTAG, "phone state = " + state);
    		
    		if(state == TelephonyManager.CALL_STATE_IDLE) {
    			if(phoneStateWas == TelephonyManager.CALL_STATE_OFFHOOK
    					|| phoneStateWas == TelephonyManager.CALL_STATE_RINGING) {
    				if(mPlayer != null && mPlayer.isPlaying() == false) {
    					activateTimer(MUSIC_DISPLAY_INTERVAL);
    					mPlayer.start();
						pm.setPlayState(PlayerManager.RUNNING_STATE);
					}
    			}
    			phoneStateWas = TelephonyManager.CALL_STATE_IDLE;
    		}
    		else if(state == TelephonyManager.CALL_STATE_RINGING) {
    			if(mPlayer != null && mPlayer.isPlaying() == true) {
//    				Log.i(DTAG, "cancel Timer");
    				myTimer.cancel();
    				mPlayer.pause();
    				pm.setPlayState(PlayerManager.PAUSE_STATE);
    			}
    			phoneStateWas = TelephonyManager.CALL_STATE_RINGING;
    		}
    		else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
    			if(mPlayer != null && mPlayer.isPlaying() == true) {
//    				Log.i(DTAG, "cancel Timer");
    				myTimer.cancel();
    				mPlayer.pause();
    				pm.setPlayState(PlayerManager.PAUSE_STATE);
    			}
   				phoneStateWas = TelephonyManager.CALL_STATE_OFFHOOK;
    		}
    	}
    };

	@Override
	public IBinder onBind(Intent intent) {
//		Log.i(TAG, "MainService:onBind()");

    	return mBinder;
	}

	public int getSongId() throws RemoteException {
		//Log.i(DTAG, "MainService:ServiceStub:getSongId()");
		if(pm.isShuffle_flag() == false)
			return MainService.mMusicIndex;
		else
			return pm.getMusicInfos().get(MainService.mMusicIndex).getPlayOrder();
	}
	
	public String getSongName() throws RemoteException {
		int songId = getSongId();
		return pm.getMusicInfos().get(songId).getTitle();
	}

	public void nextMusicPlay() {
//		Log.i(TAG, "MainService:nextPlayMusic()");
//		String uOid = ((BaasApplication)getApplication()).getMy().getObjectId();
//		MusicSong currentMusic = pm.getMusicInfos().get(phyIndex);
//		reportPlayStopToServer(uOid, currentMusic.getPid(), PlayerManager.PLAY_NEXT, mPlayer.getCurrentPosition() / 1000);
		myTimer.cancel();
		mPlayer.stop();
		pm.setPlayState(PlayerManager.STOP_STATE);
		mPlayer.reset();
		mMusicIndex++;
		nextMusicByIndex();
	}

	public void incrementBackgroundIndex() {
		mBackgroundIndex++;
		if(mBackgroundIndex >= Constants.MAXIMUM_BACKGROUND_IMAGE) {
			mBackgroundIndex = 0;
		}
	}
	
	public void decrementBackgroundIndex() {
		mBackgroundIndex--;
		if(mBackgroundIndex < 0) {
			mBackgroundIndex = Constants.MAXIMUM_BACKGROUND_IMAGE - 1;
		}
	}

	public int position() {
//		Log.i(TAG, "MainService:position()");
		if(mPlayer != null) {
			return mPlayer.getCurrentPosition();
		}
		return 0;
	}

	public int duration() {
//		Log.i(TAG, "MainService:duration(mp = " + mp + ")");
		if(mPlayer != null) {
			return mPlayer.getDuration();
		}
		return 0;
	}

	public boolean isPlaying() {
		if(mPlayer != null) {
			return mPlayer.isPlaying();
		}
		return false;
	}

	public void previousMusicPlay() {
//		Log.i(TAG, "MainService:previousPlayMusic()");
//		String uOid = ((BaasApplication)getApplication()).getMy().getObjectId();
//		MusicSong currentMusic = pm.getMusicInfos().get(phyIndex);
//		reportPlayStopToServer(uOid, currentMusic.getPid(), PlayerManager.PLAY_PREVIOUS, mPlayer.getCurrentPosition() / 1000);
		myTimer.cancel();
		mPlayer.stop();
		pm.setPlayState(PlayerManager.STOP_STATE);
		mPlayer.reset();
		mMusicIndex--;
		previousMusicByIndex();
	}
	
	public void continueMusicPlay() {
//		Log.i(TAG, "MainService:continuePlayMusic()");
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		activateTimer(MUSIC_DISPLAY_INTERVAL);
		mPlayer.start();
		pm.setPlayState(PlayerManager.RUNNING_STATE);
	}
	
	public void pauseMusicPlay() {
//		Log.i(TAG, "MainService:pauseMusic()");
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
		myTimer.cancel();
		mPlayer.pause();
		pm.setPlayState(PlayerManager.PAUSE_STATE);
	}
	
	public void stop() {
//		Log.i(TAG, "MainService:stopMusic()");

		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
		if(mWifiLock != null && mWifiLock.isHeld())
			mWifiLock.release();
//		String uOid = ((BaasApplication)getApplication()).getMy().getObjectId();
//		MusicSong currentMusic = pm.getMusicInfos().get(phyIndex);
//		reportPlayStopToServer(uOid, currentMusic.getPid(), PlayerManager.PLAY_STOP, mPlayer.getCurrentPosition() / 1000);
//		((BaasApplication)getApplication()).sendFirebaseEvent("Music", currentMusic.getTitle(),  "End");
		myTimer.cancel();
		mPlayer.stop();
		pm.setPlayState(PlayerManager.STOP_STATE);
		mPlayer.release();
	}
	
	public void playFirstMusic() {
//		Log.i(TAG, "MainService:playFirstMusic()");
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		mMusicIndex = 0;
		Thread thread = new Thread(null, doBackgroundProcessing, "Background");
		thread.start();
	}
	
	private Runnable doBackgroundProcessing = new Runnable() {
		public void run() {
			mPlayer = new MediaPlayer();
			mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
			nextMusicByIndex();
		}
	    };
    
    private int phyIndex;
    
	private void playMusicByIndex(boolean onlyOneFlag) {
//		Log.i(TAG, "playMusicByIndex(Index = " + mMusicIndex + ")");

		onlyOne = onlyOneFlag;
		if(pm.isShuffle_flag() == false) {	// �Ѱ� ���
			phyIndex = mMusicIndex;
		}
		else {
			phyIndex = msList.get(mMusicIndex).getPlayOrder();
		}
		
		MusicSong currentMusic = msList.get(phyIndex);
//		Log.i(TAG, "physical index of music = " + phyIndex);
		
//		Log.i(TAG, "filename = " + currentMusic.getFilename());
		if(mPlayer != null) {
			// Timer activation for ProgrssBar update
			getBackgroundScreenIndex();

			try {
				if(isConnected()) {	// ����� ������ ���
					if(isWiFi()) {	// WiFi�� ������ �� ���
						mWifiLock.acquire();
					}
				}
				else {				// ����� �Ұ����� ���
					reportMediaPlayerState(PlayerManager.PLAY_MUSIC_FAULT, mMusicIndex);
					return;
				}
				reportMediaPlayerState(Constants.START_OF_MUSIC, phyIndex);

//				StringBuilder sb = new StringBuilder(MusicUtils.getDownloadExternalStorageDirectoy());
//				sb.append("/").append(currentMusic.getPid());
//				fis = new FileInputStream(sb.toString());
//				mPlayer.setDataSource(fis.getFD());

				String musicIndex = currentMusic.getUrlinfo();
				StringBuilder sb = new StringBuilder(Constants.makeURL(Constants.DOWNLOAD_FILE_URL));
				sb.append("?id=").append(musicIndex);
				Uri uri = Uri.parse(sb.toString());
				Map<String, String> headers = new HashMap<String, String>();
				String session_id = MusicUtils.getConfigString(MainService.this, Constants.LOGIN_SESSION_ID, "");
				headers.put("Cookie", "loginCookie=" + session_id);

				// Use java reflection call the hide API:
				Method method = mPlayer.getClass().getMethod("setDataSource", new Class[] { Context.class, Uri.class, Map.class });
				method.invoke(mPlayer, new Object[] {this, uri, headers});

//				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//				mPlayer.setDataSource(sb.toString());

//				setupEqualizer();
				mPlayer.setOnErrorListener(this);
				mPlayer.setOnPreparedListener(this);
				mPlayer.prepareAsync();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.e(TAG, "IOexception error happen in mediaplayer create ");
			// send broadcast to activity to report position of playing music
			reportMediaPlayerState(PlayerManager.PLAY_MUSIC_FAULT, mMusicIndex);
		}
	}

	private String getServerUrlInfo(String urlinfo) {
//		return "http://kyungsub.ddns.net:8080/classic/pds/download.jsp?id=" + urlinfo;
		return Constants.DOWNLOAD_FILE_URL + urlinfo;
	}

	private int getBackgroundScreenIndex() {
//		Log.i(DTAG, "number of background files" + background_files.length);
		boolean backgroundShuffle = MusicUtils.getConfigBoolean(getApplicationContext(), getString(R.string.key_background_display_mode), false);
		if(backgroundShuffle) {
			int index = randomNumberGeneration(Constants.MAXIMUM_BACKGROUND_IMAGE); // -1 : default background
//			Log.i(DTAG, "Random (index = " + index + ")");
			return index;
		}
		else {
			mBackgroundIndex++;
//			Log.i(DTAG, "Sequential (index = " + mBackgroundIndex + ")");
			if(mBackgroundIndex >= Constants.MAXIMUM_BACKGROUND_IMAGE) {
				mBackgroundIndex = 0;
			}
			return mBackgroundIndex;
		}
	}
	
	private Random mRandom = new Random(System.currentTimeMillis());
	private int randomNumberGeneration(int numberOfTotalBackground) {
		return mRandom.nextInt(numberOfTotalBackground);
	}

	private void activateTimer(long duration) {
//		Log.i(DTAG, "activateTimer");
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}
		}, duration - DISPLAY_DURATION_OFFSET, duration);	// 2000(2��) time interval
	}

	protected void TimerMethod() {
		reportMediaPlayerState(PlayerManager.ONE_SECOND_TIMEOUT, mMusicIndex);
//		mCurrentPT += TIMER_TICK_INCREMENT;	// �� ����
//		if(mCurrentPT == 60) {
//			if(isItExpired()) {
//				myTimer.cancel();
//				mp.stop();
//				playerState = PlayerFragment.STOP_STATE;
//				reportMediaPlayerState(PlayerFragment.COMPLETION_OF_ONE_MINUTE, mMusicIndex);
//				mp.reset();
//			}
//		}
	}
//	
//	private boolean isItExpired() {
//		boolean doesSubscribe = MusicUtils.getConfigBoolean(getApplicationContext(), Constant.KEY_DOES_SUBSCRIBE, false);
//		Log.d(TAG, "doesSubscribe = " + doesSubscribe);
//		if(doesSubscribe)
//			return false;
//		long remain_date = MusicUtils.getConfigLong(MainService.this, Constant.KEY_EXPIRE_DATE, 0L);
//		Calendar today = Calendar.getInstance();
//		long today_date = today.getTimeInMillis();
//
//		if(remain_date < today_date) {
//			return true;
//		}
//		return false;
//	}
//	
	private void reportMediaPlayerState(int commandCode, int musicIndex) {
//		Log.i(TAG, "MainService:reportMediaPlayerState()");
		
		// send broadcast to activity to report position of playing music
		Intent broadcast = new Intent(Constants.BROADCAST_ACTION);
		broadcast.putExtra(SERVICE_COMMAND, commandCode);
		broadcast.putExtra(POSITION_OF_MUSICLIST, musicIndex);
		sendBroadcast(broadcast);
	}

	private void nextMusicByIndex() {
//		Log.i(TAG, "MainService:nextMusicByIndex(mMusicIndex = " + mMusicIndex + ")");
		
		if(mMusicIndex >= 0 && mMusicIndex < msList.size()) {
			playMusicByIndex(false);
		}
		else {
			if(pm.getRepeat_state() == PlayerManager.REPEAT_ALL) {
				mMusicIndex = 0;
				nextMusicByIndex();
			}
			else {					// only 1 turn
				// send broadcast to activity to report completion of playing music
				pm.setPlayState(PlayerManager.STOP_STATE);
//				reportMediaPlayerState(MainActivity.COMPLETION_OF_MUSIC, mMusicIndex);
				reportMediaPlayerState(PlayerManager.COMPLETION_OF_MUSIC, msList.size() - 1);
				mPlayer.release();
			}
		}
    }
	
	private void previousMusicByIndex() {
//		Log.i(TAG, "previousMusicByIndex(mMusicIndex = " + mMusicIndex + ")");
		
		if(mMusicIndex >= 0 && mMusicIndex < msList.size()) {
			playMusicByIndex(false);
		}
		else {
			if(pm.getRepeat_state() == PlayerManager.REPEAT_ALL) {	// Repeat Mode
				mMusicIndex = msList.size() - 1;
				previousMusicByIndex();
			}
			else {					// only 1 turn
				// send broadcast to activity to report completion of playing music
				pm.setPlayState(PlayerManager.STOP_STATE);
//				reportMediaPlayerState(MainActivity.COMPLETION_OF_MUSIC, mMusicIndex);
				reportMediaPlayerState(PlayerManager.COMPLETION_OF_MUSIC, 0);
				mPlayer.release();
			}
		}
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
//		Log.i(TAG, "MainService:onDestroy(myTimer = " + myTimer + ", mp = " + mp + ")");
		if(myTimer != null) {
			myTimer.cancel();
		}
		if(mPlayer != null) {
//			mEqualizer.release();
			mPlayer.release();
			mPlayer = null;
		}
		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(ID_BACKGROUND_SERVICE_RUNNING);
		if(amFlag == true)
			deactivateAlarmManager();
		if(mWifiLock != null && mWifiLock.isHeld())
			mWifiLock.release();
	}
	
	private void deactivateAlarmManager() {
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(this, MainService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
		am.cancel(pi);
	}

	public void onPrepared(MediaPlayer mp) {
		reportMediaPlayerState(PlayerManager.START_OF_MUSIC, phyIndex);
		activateTimer(MUSIC_DISPLAY_INTERVAL);
//		final String uOid = ((BaasApplication)getApplication()).getMy().getObjectId();
//		MusicSong currentMusic = pm.getMusicInfos().get(phyIndex);
//		reportPlayStartToServer(uOid, currentMusic.getPid(), PlayerManager.PLAY_START);
//		pm.reportPlayStartToServer(uOid, currentMusic.getPid(), PlayerManager.PLAY_START);
//
//		sendFirebaseEvent("Music", currentMusic.getTitle(),  "Start");
		mp.start();
//		mCurrentPT = 0;
		pm.setPlayState(PlayerManager.RUNNING_STATE);
		
		mp.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
//				Log.i(TAG, "complete play a song : " + mMusicIndex);
//				MusicSong currentMusic = pm.getMusicInfos().get(phyIndex);
//				reportPlayCompleteToServer(uOid, currentMusic.getPid(), PlayerManager.PLAY_COMPLETE, mp.getDuration() / 1000);
//				((BaasApplication)getApplication()).sendFirebaseEvent("Music", currentMusic.getTitle(),  "End");
				myTimer.cancel();
				if(onlyOne != true) {
					mp.reset();
					if(pm.getRepeat_state() != PlayerManager.REPEAT_ONCE) {
						mMusicIndex++;
					}
					nextMusicByIndex();
				}
				else {
					pm.setPlayState(PlayerManager.STOP_STATE);
					reportMediaPlayerState(PlayerManager.COMPLETION_OF_MUSIC, phyIndex);
					mp.release();
					mPlayer = null;
				}
			}
		});
	}

//    private void reportPlayCompleteToServer(String uOid, final String pid, int playComplete, int duration) {
//        mSi = new ServerInterworking(this);
//        mSi.registerCallbackPlay(new ServerInterworking.ReportPlayStatus() {
//            @Override
//            public void callbackReturnReportPlayStatus(String result) {
//                try {
//                    Log.i(Constants.TAG, "pid=" + pid + ", result = " + result);
//                    JSONObject obj = new JSONObject(result);
////                    Log.i(Constant.TAG, "callbackReturnReportPlayStatus(" + obj.get("result") + ")");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//        String parameter = "id="+mOid+"&state="+playComplete+"&duration="+duration;
////        mSi.reportPlayStateToServer(Constant.makeTalantonURL("192.168.0.2", Constant.REPORT_PLAY_STATUS2), parameter);
//        mSi.reportPlayStateToServer(Constant.makeTalantonURL(((BaasApplication)getApplication()).getServer_ip(), Constant.REPORT_PLAY_STATUS2), parameter);
//    }

//    private void reportPlayStartToServer(final String uOid, final String pid, final int playStart) {
//		mSi = new ServerInterworking(this);
//		mSi.registerCallbackPlay(new ServerInterworking.ReportPlayStatus() {
//            @Override
//            public void callbackReturnReportPlayStatus(String result) {
//                try {
//                    Log.i(Constant.TAG, "pid=" + pid + ", result = " + result);
//                    JSONObject obj = new JSONObject(result);
//                    if(obj.get("result").equals("success")) {
//                        if(obj.get("cmd").equals("add")) {
//                            mOid = (String)obj.get("id");
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        String parameter = "uid="+uOid+"&pid="+pid+"&state="+playStart;
////        mSi.reportPlayStateToServer(Constant.makeTalantonURL("192.168.0.2", Constant.REPORT_PLAY_STATUS1), parameter);
//        mSi.reportPlayStateToServer(Constant.makeTalantonURL(((BaasApplication)getApplication()).getServer_ip(), Constant.REPORT_PLAY_STATUS1), parameter);
//	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
//		final String uOid = ((BaasApplication)getApplication()).getMy().getObjectId();
//		MusicSong currentMusic = pm.getMusicInfos().get(phyIndex);
//        reportPlayStopToServer(uOid, currentMusic.getPid(), PlayerManager.PLAY_ERROR, 0);
		if(myTimer != null) {
			myTimer.cancel();
		}
		pm.setPlayState(PlayerManager.STOP_STATE);
		reportMediaPlayerState(PlayerManager.COMPLETION_OF_MUSIC, phyIndex);
		mp.release();
		mPlayer = null;
		return false;
	}

//    private void reportPlayStopToServer(String uOid, final String pid, int playError, int duration) {
//        mSi = new ServerInterworking(this);
//        mSi.registerCallbackPlay(new ServerInterworking.ReportPlayStatus() {
//            @Override
//            public void callbackReturnReportPlayStatus(String result) {
//                try {
//                    Log.i(Constant.TAG, "pid=" + pid + ", result = " + result);
//                    JSONObject obj = new JSONObject(result);
//                    Log.i(Constant.TAG, "callbackReturnReportPlayStatus(" + obj.get("result") + ")");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//        String parameter = "id="+mOid+"&state="+playError+"&duration="+duration;
////        mSi.reportPlayStateToServer(Constant.makeTalantonURL("192.168.0.2", Constant.REPORT_PLAY_STATUS2), parameter);
//        mSi.reportPlayStateToServer(Constant.makeTalantonURL(((BaasApplication)getApplication()).getServer_ip(), Constant.REPORT_PLAY_STATUS2), parameter);
//    }

    public int getBackgroundIndex() {
//		Log.i(DTAG, "MainService:ServiceStub:getBackgroundIndex()");
		return MainService.mBackgroundIndex;
	}
}