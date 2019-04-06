package com.talanton.music.player.main;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.talanton.music.player.R;
import com.talanton.music.player.db.MusicListDB.MusicServerInfo;
import com.talanton.music.player.db.MusicListDB.MusicClientInfo;
import com.talanton.music.player.db.MusicListDBHelper;
import com.talanton.music.player.interwork.ServerInterworking;
import com.talanton.music.player.member.SignInActivity;
import com.talanton.music.player.sub.MusicSong;
import com.talanton.music.player.sub.PlayerManager;
import com.talanton.music.player.utils.Constants;
import com.talanton.music.player.utils.MusicUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, ServerInterworking.ISignOutResult {
    private PlayerManager pm;
    protected MusicListDBHelper musicDB = null;
    private String language;
    private TelephonyManager telephonyManager;
    private int phoneState = -1;		// initial
    PhoneStateListener callStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
//    		Log.d(TAG, "MusicPE: phone state = " + state);

            phoneState = state;
        }
    };

    boolean mTouched = false;
    private LinearLayout imageLayout;
    private LinearLayout mainLayout;
    private ImageView imageView;
    private int backgroundResId;
    private ListView music_list;
    private TextView tvHelp;
    private SeekBar mSeekVolume;
    private TextView progTime_tv;
    private TextView totalTime_tv;
    private ImageButton playBtn;
    private ProgressBar mProgress;

    private static final int DISPLAY_PLAY_VIEW = 1;
    private static final int DISPLAY_IMAGE_VIEW = 2;

    private static final int CONFIRM_FINISH_DIALOG = 100;
    private static final int SHOW_PROGRESS_BAR = 102;
    private static final int RX_PUSH_MESSAGE = 200;

    private static final long ACTIVE_DURATION = 5000;		// 5 sec
    private static final float MAXIMUM_RATING_VALUE = 5.0f;
    private Timer myTimer;
    private Handler mHandler = new Handler();
    private Cursor mCursor;
    private MyMusicListAdapter adapter;
    private ArrayList<MusicSong> msList;
    private float mRatingValue = 0.0f;
    private int background_index = 0;
    public MainService sService = null;
    boolean mBound = false;
    private ProgressDialog mDialog;
    AtomicInteger msgId = new AtomicInteger();

   private ServerInterworking mSi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSi = ServerInterworking.getInstance(this);
        pm = PlayerManager.getInstance();
        pm.init();
        sendCommandToService();

        musicDB = new MusicListDBHelper(this);
        Locale lo = Locale.getDefault();
        language = lo.getLanguage();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addMenuItemInNavMenuDrawer(true);

        initActivity();

        Intent rxIntent = getIntent();
//		Log.d(Constant.TAG, "rx intent : " + rxIntent);
        if(rxIntent != null && rxIntent.getExtras() != null) {
            Log.d(Constants.TAG, "rx intent : " + rxIntent.getExtras().toString());
            Bundle data = rxIntent.getExtras();
//            showDialog(RX_PUSH_MESSAGE);
        }

        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void initActivity() {
        View view = findViewById(R.id.mainLayout);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//				Log.i(TAG, "event : " + event.getAction());
                if(pm.getPlayState() == PlayerManager.RUNNING_STATE && mTouched != true && event.getAction() == MotionEvent.ACTION_DOWN) {
                    mTouched = true;
                    viewControl(DISPLAY_PLAY_VIEW);
                    callGoToInvible(ACTIVE_DURATION);
                }
                return false;
            }
        });
        imageLayout = (LinearLayout)findViewById(R.id.image_layout);
        mainLayout = (LinearLayout)findViewById(R.id.player_layout);
        imageView = (ImageView)findViewById(R.id.background_image_id);
        backgroundResId = R.drawable.main_background01;
        music_list = (ListView)findViewById(R.id.music_list);
        tvHelp = (TextView)findViewById(R.id.help_description_id);
        music_list.setEmptyView(tvHelp);
        progTime_tv = (TextView)findViewById(R.id.prog_time);
        totalTime_tv = (TextView)findViewById(R.id.total_time);
        playBtn = (ImageButton)findViewById(R.id.music_play_button);
        playBtn.setOnClickListener(mOnClickListener);
        ImageButton imsiBtn = (ImageButton)findViewById(R.id.previous_btn_id);
        imsiBtn.setOnClickListener(mOnClickListener);
        imsiBtn = (ImageButton)findViewById(R.id.stop_btn_id);
        imsiBtn.setOnClickListener(mOnClickListener);
        imsiBtn = (ImageButton)findViewById(R.id.next_btn_id);
        imsiBtn.setOnClickListener(mOnClickListener);
        mProgress = (ProgressBar)findViewById(R.id.progress);
        mSeekVolume = (SeekBar)findViewById(R.id.volume);
//		Log.i(TAG, "seekbar = " + mSeekVolume);
        mSeekVolume.setProgress(getVolumeAndConversionToSeekBar());
        mSeekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int mProgValue;
            public void onStopTrackingTouch(SeekBar seekBar) {
//				Log.i(DTAG, "MainActivity:onStopTrackingTouch(mProgValue = " + mProgValue + ")");
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 15 * mProgValue / 100, 0);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
//				Log.i(DTAG, "MainActivity:onStartTrackingTouch()");
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//				Log.i(DTAG, "MainActivity:onProgressChanged(progress = " + progress + ", fromUser = " + fromUser + ")");
                mProgValue = progress;
            }
        });

        if(MusicUtils.getConfigBoolean(this, "playlist_changed", false)) {
            MusicUtils.saveConfig(this, "playlist_changed", false);
        }

        makePlayMusicList();
        displayMusicList();
    }

    private void makePlayMusicList() {
//		Log.i(TAG, "PlayerFragment:makePlayMusicList()");
        SQLiteDatabase dbHandler = musicDB.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MusicServerInfo.TABLE_NAME + "," + MusicClientInfo.TABLE_NAME);
        StringBuilder sb = new StringBuilder();
        sb.append(MusicServerInfo.TABLE_NAME).append(".").append(MusicServerInfo.PRODUCT_ID)
                .append(" = ").append(MusicClientInfo.TABLE_NAME).append(".").append(MusicClientInfo.PRODUCT_ID);
        queryBuilder.appendWhere(sb.toString());
        String columnsToReturn [] = {
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.TITLE_KO,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.AUTHOR_KO,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.TITLE_EN,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.AUTHOR_EN,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.PLAY_TIME,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.FILE_INFO,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.UUID_INFO,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.BOOKMARK,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.PRODUCT_ID,
                MusicClientInfo.TABLE_NAME + "." + MusicClientInfo.CLIENT_ID
        };
        String sortOrder = MusicClientInfo.TABLE_NAME + "." + MusicClientInfo.CLIENT_ID;
        mCursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, sortOrder);
//		Log.i(TAG, "PlayerFragment : count of cursor = " + mCursor.getCount());
//		mActivity.startManagingCursor(mCursor);
        if(mCursor.getCount() == 0) {		// Don't have Music into Bookmark
            Toast.makeText(this, getString(R.string.help_add_bookmark), Toast.LENGTH_LONG).show();
        }

        adapter = new MyMusicListAdapter(this, mCursor);
        music_list.setAdapter(adapter);
    }

    private int getVolumeAndConversionToSeekBar() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        return (currentVolume * 100 / maxVolume);
    }

    private void callGoToInvible(long activeDuration) {
//		Log.i(DTAG, "MainActivity:callGoToInvible()");
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, activeDuration);	// 3000(3��) time interval
    }

    protected void TimerMethod() {
//		Log.i(DTAG, "MainActivity:TimerMethod()");
        mTouched = false;
        mHandler.post(new Runnable() {
            public void run() {		// foreground job���� ����
                assignBackgroundScreenResourceFile(background_index);
                viewControl(DISPLAY_IMAGE_VIEW);
            }
        });
    }

    private void assignBackgroundScreenResourceFile(int index) {
        imageView.setBackgroundResource(backgroundResId + index);
    }

    private void viewControl(int whichView) {
//		Log.i(DTAG, "MainActivity:viewControl(visible = " + visible + ")");
        if(whichView == DISPLAY_PLAY_VIEW) {
            mainLayout.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.GONE);
        }
        else {
            mainLayout.setVisibility(View.GONE);
            imageLayout.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(phoneState != TelephonyManager.CALL_STATE_IDLE) {
                Toast.makeText(getApplicationContext(), getString(R.string.guide_on_busy), Toast.LENGTH_LONG).show();
                return;
            }

            switch(v.getId()) {
                case R.id.previous_btn_id:
                    try {
                        previousMusicClick(v);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.music_play_button:
                    try {
                        playMusicClick(v);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.stop_btn_id:
                    try {
                        stopMusicClick(v);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.next_btn_id:
                    try {
                        nextMusicClick(v);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    public void nextMusicClick(View v) throws RemoteException {
//		Log.d(TAG, "MainActivity:nextMusicClick()");

        int repeat_state = pm.getRepeat_state();
        if(repeat_state == PlayerManager.REPEAT_ALL || repeat_state == PlayerManager.REPEAT_OFF) {
            if(pm.getPlayState() == PlayerManager.RUNNING_STATE) {
//				sendCommandToService(MainService.COMMAND_NEXT_MUSIC_PLAY, 0);
                sService.nextMusicPlay();
            }
        }
    }

    public void stopMusicClick(View v) throws RemoteException {
//		Log.d(TAG, "MainActivity:stopMusicClick()");

        if(pm.getPlayState() != PlayerManager.STOP_STATE) {
            changeMusicStateToStop();

//			sendCommandToService(MainService.COMMAND_STOP_MUSIC_PLAY, 0);
            sService.stop();
//			Intent intent = new Intent(getBaseContext(), MainService.class);
//			stopService(intent);
            displayMusicList();
        }
    }

//    public void playMusicClick(View v) {
////		Log.d(TAG, "MainActivity:playMusicClick (playState = " + pm.getPlayState() + ")");
//
//        String session_id = MusicUtils.getConfigString(this, Constants.LOGIN_SESSION_ID, "");
//        if(session_id.equals("")) {
//            showMessage(R.string.we_need_login);
//            return;
//        } else {
//            mSi.registerSessionCheckCallback(this);
//            mSi.initSessionCheck(Constants.SESSION_CHECK_URL, session_id, "1");
//        }
//    }

    private void showMessage(int msg_id) {
        Toast.makeText(this, msg_id, Toast.LENGTH_LONG).show();
    }

    public void playMusicClick(View v) throws RemoteException {
        if(MusicUtils.getConfigString(this, Constants.LOGIN_SESSION_ID, "").equals("")) {
            showMessage(R.string.we_need_login);
        }
        else {
            if(pm.getPlayState() == PlayerManager.STOP_STATE) {	// Initial state or stop
                if(areThereValidMusic() == true) {
//				Log.i(TAG, "MainActivity:playMusicClick()");
                    pm.makeShuffleIndex();
                    sService.playFirstMusic();		// binding占쏙옙 占실깍옙 占쏙옙占싱띰옙 占쏙옙占쏙옙占싹몌옙 占싫듸옙
                    displayMusicSongInfoByaSong();
                    pm.setPlayState(PlayerManager.RUNNING_STATE);
                    mTouched = false;
                    playBtn.setImageResource(android.R.drawable.ic_media_pause);
                }
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.guide_no_selected_song), Toast.LENGTH_LONG).show();
                }
            }
            else if(pm.getPlayState() == PlayerManager.RUNNING_STATE) {	// Pause
//			sendCommandToService(MainService.COMMAND_PAUSE_MUSIC_PLAY, 0);
                sService.pauseMusicPlay();
                pm.setPlayState(PlayerManager.PAUSE_STATE);
                if(myTimer != null) {		// at pause state, background로 가지 않도록 수정
                    myTimer.cancel();
                }
                playBtn.setImageResource(android.R.drawable.ic_media_play);
            }
            else {		// Continue Playing (占싹쏙옙 占쏙옙占쏙옙占쏙옙 占쏙옙占승울옙占쏙옙  占쏙옙占�占쏙옙綬�占쏙옙占쏙옙占쏙옙 占쏙옙占�
//			sendCommandToService(MainService.COMMAND_CONTINUOUS_MUSIC_PLAY, 0);
                sService.continueMusicPlay();
                pm.setPlayState(PlayerManager.RUNNING_STATE);
                callGoToInvible(ACTIVE_DURATION);	// Continue 상태일 때, 다시 background 동작 활성화
                playBtn.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }

//    @Override
//    public void callbackLoginSessionCheck(String result) {
//        if(result.equals("1")) {    // play music clicked
//            try {
//                playMusicClickProcessing();
//            } catch (RemoteException e) {
//                Log.e(Constants.TAG, "Play Music error happened");
//            }
//        } else {
//            showMessage(R.string.we_need_login);
//        }
//    }

    public void previousMusicClick(View v) throws RemoteException {
//		Log.d(TAG, "MainActivity:previousMusicClick()");

        int repeat_state = pm.getRepeat_state();
        if(repeat_state == PlayerManager.REPEAT_ALL || repeat_state == PlayerManager.REPEAT_OFF) {
            if(pm.getPlayState() == PlayerManager.RUNNING_STATE) {
//				sendCommandToService(MainService.COMMAND_PREVIOUS_MUSIC_PLAY, 0);
                sService.previousMusicPlay();
            }
        }
    }

    private void displayMusicList() {
//		Log.i(TAG, "MainActivity:displayMusicList(playState = " + playState + ")");
        if(pm.getPlayState() == PlayerManager.STOP_STATE) {
            displayInitialListInformation();
        }
        else if(pm.getPlayState() == PlayerManager.PAUSE_STATE){
            displayPlayingSongInformation();
        }
        else {
            if(pm.getPlayState() == PlayerManager.STOP_STATE) {
                changeMusicStateToStop();
                Toast.makeText(getApplicationContext(), getString(R.string.finish_music_play), Toast.LENGTH_LONG).show();
//				stopMusicService();
                displayInitialListInformation();
            }
            else {		// on Running
                displayPlayingSongInformation();
            }
        }
    }

    private void displayPlayingSongInformation() {
        int index = MainService.mMusicIndex;
//		Log.i(TAG, "index = " + index);

        try {
            displayCurrentMusicSongInformation(index);
            progressBarExpression(index);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void progressBarExpression(int index) throws RemoteException {
        MusicSong ms = msList.get(index);
        int currentPosition = sService.position() / 1000;
        String[] playTime = ms.getTimeinfo().split(":");
        int duration = Integer.valueOf(playTime[0]) * 60 + Integer.valueOf(playTime[1]);
        progTime_tv.setText(String.format("%02d:%02d", currentPosition / 60, currentPosition % 60));
        mProgress.setProgress(currentPosition * 100 / duration);
    }

    private void displayCurrentMusicSongInformation(int index) throws RemoteException {
        MusicSong ms = msList.get(index);
        totalTime_tv.setText(ms.getTimeinfo());
        int currentPosition = sService.position() / 1000;
        progTime_tv.setText(String.format("%02d:%02d", currentPosition / 60, currentPosition % 60));
    }

    private void displayInitialListInformation() {
        totalTime_tv.setText("00:00");
        progTime_tv.setText("00:00");
        mProgress.setProgress(0);
        viewControl(DISPLAY_PLAY_VIEW);
        if(myTimer != null) {
            myTimer.cancel();
        }
    }

    private void changeMusicStateToStop() {
//		if(pm.getPlayState() == PlayerManager.RUNNING_STATE) {	// back play button icon
        playBtn.setImageResource(android.R.drawable.ic_media_play);
//		}

        pm.setPlayState(PlayerManager.STOP_STATE);
    }

    private void displayMusicSongInfoByaSong() {
//		Log.i(DTAG, "MainActivity:displayMusicSongInfoByaSong");
        viewControl(DISPLAY_IMAGE_VIEW);
    }

    private boolean areThereValidMusic() {
        if(msList.size() > 0)
            return true;
        else
            return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(pm.getPlayState() != PlayerManager.STOP_STATE) {
            Toast.makeText(this, R.string.guide_on_playing, Toast.LENGTH_SHORT).show();
            return true;
        }

        if(item.getItemId() == R.id.menu_delall) {
//			Log.i(TAG, "menu del all");
            updateBookmarkInformationAll();
        }
        else if(item.getItemId() == R.id.menu_shuffle_off) {
//			Log.i(TAG, "menu shuffle off");
            pm.setShuffle_flag(false);
            pm.makeShuffleIndex();
        }
        else if(item.getItemId() == R.id.menu_shuffle_on) {
//			Log.i(TAG, "menu shuffle on");
            pm.setShuffle_flag(true);
            pm.makeShuffleIndex();
        }
        else if(item.getItemId() == R.id.menu_repeat_all) {
//			Log.i(TAG, "menu repeat all");
            pm.setRepeat_state(PlayerManager.REPEAT_ALL);
        }
        else if(item.getItemId() == R.id.menu_repeat_off) {
//			Log.i(TAG, "menu repeat off");
            pm.setRepeat_state(PlayerManager.REPEAT_OFF);
        }
        else {
//			Log.i(TAG, "menu repeat once");
            pm.setRepeat_state(PlayerManager.REPEAT_ONCE);
        }
        return true;
    }

    private void updateBookmarkInformationAll() {
        if(mCursor == null || mCursor.getCount() == 0)
            return;

        SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
        for(int i = mCursor.getCount() - 1;i >= 0;i--) {
//			Log.i(TAG, "index = " + i);
            mCursor.moveToPosition(i);
            String productID = mCursor.getString(mCursor.getColumnIndex(MusicServerInfo.PRODUCT_ID));
            String filename = mCursor.getString(mCursor.getColumnIndex(MusicServerInfo.FILE_INFO));
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

            queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
            String w = MusicServerInfo.PRODUCT_ID + "='" + productID + "'";
            queryBuilder.appendWhere(w);
            Cursor cursor = queryBuilder.query(dbHandler, null,null,null,null,null, null);
            if(cursor.getCount() > 0) {
                // Update bookmark (DL_COUNT)
                ContentValues musicRowValues = new ContentValues();
                musicRowValues.put(MusicServerInfo.BOOKMARK, 0);
                dbHandler.update(MusicServerInfo.TABLE_NAME, musicRowValues, w, null);

                queryBuilder = new SQLiteQueryBuilder();

                queryBuilder.setTables(MusicClientInfo.TABLE_NAME);
                w = MusicClientInfo.PRODUCT_ID + "='" + productID + "'";
                queryBuilder.appendWhere(w);
                //占쏙옙占�占쏙옙占쏙옙占쏙옙占쏙옙 占쏙옙占쏙옙占쏙옙 占시뤄옙占쏙옙 占싱몌옙占쏙옙(2占쏙옙 占싱삼옙 占쏙옙占싱븝옙占�占쏙옙占쏙옙占�풀占쏙옙占쏙옙占쏙옙 占쌍억옙占�占쏙옙)
                String columnsToReturn [] = {
                        MusicClientInfo.PRODUCT_ID
                };

                //占쏙옙占쏙옙占�占쏙옙占쏙옙占쏙옙 占쌔댐옙 占쏙옙占�占쏙옙占쏙옙占쏙옙 占쏙옙占쏙옙 占승댐옙
                cursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, null);
//				Log.i(TAG, "count = " + cursor.getCount());

                if(cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    dbHandler.delete(MusicClientInfo.TABLE_NAME, w, null);
                }
                cursor.close();
                msList.remove(i);
                adapter.notifyDataSetChanged();
            }
        }
//		Log.d(TAG, "mCusor.requery(6)");
        mCursor.requery();
        Toast.makeText(this, R.string.delete_bookmark_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_ACTION);
        registerReceiver(mReceiver, filter);

        if(MusicUtils.getConfigBoolean(this, "playlist_changed", false)) {
            makePlayMusicList();
            MusicUtils.saveConfig(this, "playlist_changed", false);
        }
    }

    @Override
    public void getSignOutResult(String result) {
        showMessage(R.string.help_logout_success);
        addMenuItemInNavMenuDrawer(true);
    }

    static class FavorListItemContainer {
        TextView mMusicName;
        TextView mAuthorName;
        TextView mTime;
    }

    // Adapter implementation
    public class MyMusicListAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public MyMusicListAdapter(Context context, Cursor cursor) {
// 			Log.i(TAG, "MyMusicListAdapter()");
            mContext = context;
            mInflater = LayoutInflater.from(mContext);

            // 커占쏙옙占쏙옙 占쌓븝옙占쏙옙抉占�占싼댐옙.
            cursor.moveToFirst();
            msList = pm.getMusicInfos();
            msList.clear();

            if(cursor.getCount() > 0) {
                do {
                    MusicSong tmpSong = new MusicSong();
                    if(!language.equals("ko")) {
                        tmpSong.setTitle(cursor.getString(cursor.getColumnIndex(MusicServerInfo.TITLE_EN)));
                        tmpSong.setAuthor(cursor.getString(cursor.getColumnIndex(MusicServerInfo.AUTHOR_EN)));
                    }
                    else {
                        tmpSong.setTitle(cursor.getString(cursor.getColumnIndex(MusicServerInfo.TITLE_KO)));
                        tmpSong.setAuthor(cursor.getString(cursor.getColumnIndex(MusicServerInfo.AUTHOR_KO)));
                    }

                    tmpSong.setTimeinfo(cursor.getString(cursor.getColumnIndex(MusicServerInfo.PLAY_TIME)));
                    tmpSong.setFilename(cursor.getString(cursor.getColumnIndex(MusicServerInfo.FILE_INFO)));
                    tmpSong.setUrlinfo(cursor.getString(cursor.getColumnIndex(MusicServerInfo.UUID_INFO)));
// 					tmpSong.setMyRate(cursor.getInt(cursor.getColumnIndex(MusicServerInfo.MY_RATE)));
                    tmpSong.setPid(cursor.getString(cursor.getColumnIndex(MusicServerInfo.PRODUCT_ID)));
// 					Log.d(TAG, "pid = " + tmpSong.getPid());
                    msList.add(tmpSong);
                }
                while (cursor.moveToNext());
            }
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
// 			Log.i(TAG, "getView start(position = " + position + ", convertView = " + convertView + ")");
            final FavorListItemContainer musicListItem;

            if(convertView == null) {
                convertView = (LinearLayout)mInflater.inflate(R.layout.listview_player, null);
                musicListItem = new FavorListItemContainer();

                musicListItem.mMusicName = (TextView)convertView.findViewById(R.id.list_music_title);
                musicListItem.mAuthorName = (TextView)convertView.findViewById(R.id.list_composer_name);
                musicListItem.mTime = (TextView)convertView.findViewById(R.id.list_music_download);

                convertView.setTag(musicListItem);
            }
            else {
                musicListItem = (FavorListItemContainer) convertView.getTag();
            }

            final MusicSong data = msList.get(position);
            musicListItem.mMusicName.setText(data.getTitle());
            musicListItem.mAuthorName.setText(data.getAuthor());
            musicListItem.mTime.setText(data.getTimeinfo());

            return convertView;
        }


        private String expressRatingbarValue() {
            StringBuilder sb = new StringBuilder();
            sb.append(mRatingValue).append("/").append(MAXIMUM_RATING_VALUE);
            return sb.toString();
        }

        public int getCount() {
            return msList.size();
        }

        public Object getItem(int position) {
            return msList.get(position);
        }

        public long getItemId(int position) {
            return msList.get(position).getId();
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Constants.BROADCAST_ACTION)) {
                int mCommand = intent.getIntExtra(Constants.SERVICE_COMMAND, 1);

                if(mCommand == Constants.START_OF_MUSIC) {				// 1 start of music
                    try {
                        changeBackground();
                        setTitle(sService.getSongName());
                        displayCurrentMusicSongInformation(sService.getSongId());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                else if(mCommand == PlayerManager.ONE_SECOND_TIMEOUT) {		// 2 second elapsed
                    if(pm.getPlayState() == PlayerManager.STOP_STATE) {
                        return;
                    }
                    try {
                        progressBarExpression(sService.getSongId());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                else if(mCommand == Constants.COMPLETION_OF_MUSIC) {		// 3 completion of music
                    setTitle(getString(R.string.app_name));
                    changeMusicStateToStop();
                    Toast.makeText(getApplicationContext(), getString(R.string.finish_music_play), Toast.LENGTH_LONG).show();
                    displayMusicList();
                }
                else if(mCommand == PlayerManager.COMPLETION_OF_ONE_MINUTE) {		// 3 completion of music
                    changeMusicStateToStop();
                    Toast.makeText(getApplicationContext(), getString(R.string.finish_pre_listen), Toast.LENGTH_LONG).show();
                    displayMusicList();
                }
                else if(mCommand == Constants.PLAY_MUSIC_FAULT) {		// MusicPlayer creation error
                    setTitle(getString(R.string.app_name));
                    changeMusicStateToStop();
                    Toast.makeText(getApplicationContext(), getString(R.string.guide_stop_listen_music), Toast.LENGTH_LONG).show();
                    stopMusicService();
                    finish();
                }
//                else if(mCommand == Constants.UPDATED_FAVORITELIST_INFO) {
//                    makePlayMusicList();
//                }
            }
        }
    };

    private void changeBackground() {
        background_index = sService.getBackgroundIndex();
        assignBackgroundScreenResourceFile(background_index);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.i(TAG, "KeyCode = " + keyCode);
        if(keyCode == KeyEvent.KEYCODE_BACK){
            showDialog(CONFIRM_FINISH_DIALOG);
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case CONFIRM_FINISH_DIALOG: {
                return new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.alert_dialog_icon)
                        .setTitle(R.string.title_finish_music_confirm)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                stopMusicService();
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .create();
            }
            case SHOW_PROGRESS_BAR: {
                mDialog = new ProgressDialog(this);
                mDialog.setTitle(R.string.title_db_upgrade_progress);
                mDialog.setMessage(getString(R.string.msg_db_upgrade_progress));
                mDialog.setIndeterminate(true);
//			mDialog.setCancelable(true);
                return mDialog;
            }
            default: {
                return new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.alert_dialog_icon)
                        .setTitle(R.string.title_push_message_rx)
                        .setMessage("Need Modify")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .create();
            }
        }
    }

    public void stopMusicService() {
        if(mBound) {
            if(pm.getPlayState() != PlayerManager.STOP_STATE && sService.isPlaying())
                sService.stop();
            unbindService(osc);
        }
    }

    private void sendCommandToService() {
        Intent intent = new Intent(this, MainService.class);
        bindService(intent, osc, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection osc = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
//        	Log.i(TAG, "MianActivity:onServiceConnected()");
            MainService.LocalBinder binder = (MainService.LocalBinder) obj;
            sService = binder.getService();
            mBound = true;
//            Log.i(TAG, "MainActivity:onServiceConnected(service = " + sService + ", mFragment = " + mPlayerFragment + ")");
        }

        public void onServiceDisconnected(ComponentName classname) {
//        	Log.i(TAG, "MainActivity:onServiceDisconnected()");
            mBound = false;
        }
    };

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favorite) {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivityForResult(intent, Constants.SIGNIN_INTENT_ID);
        } else if (id == R.id.nav_logout) {
            mHandler.postDelayed(new Runnable() {   // 지연 후 실행
                @Override
                public void run() {
                    mSi.registerISignOutResult(MainActivity.this);
                    mSi.initSignOut(Constants.SIGNOUT_URL);
                }
            }, 10);
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.SIGNIN_INTENT_ID && resultCode == RESULT_OK) { // 로그인 성공
            showMessage(R.string.help_login_success);
            addMenuItemInNavMenuDrawer(false);   // show logout menu
        }
    }

    private void addMenuItemInNavMenuDrawer(boolean flag) {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();
        MenuItem commonMenu = menu.findItem(R.id.common_menu);
        Menu subMenu = commonMenu.getSubMenu();

        MenuItem login = subMenu.findItem(R.id.nav_login);
        MenuItem logout = subMenu.findItem(R.id.nav_logout);

        if(flag == true) {  // display Login sub menu
            login.setVisible(true);
            logout.setVisible(false);
        } else {            // display logout sub menu
            login.setVisible(false);
            logout.setVisible(true);
        }

        navView.invalidate();
    }
}
