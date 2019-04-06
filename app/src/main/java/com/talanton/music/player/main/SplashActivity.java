package com.talanton.music.player.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.talanton.music.player.R;

import com.talanton.music.player.interwork.ServerInterworking;
import com.talanton.music.player.interwork.ServerInterworking.GetServiceParameterInfo;
import com.talanton.music.player.interwork.ServerInterworking.DownloadFileContent;
import com.talanton.music.player.utils.Constants;
import com.talanton.music.player.utils.MusicUtils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity implements GetServiceParameterInfo, DownloadFileContent {
    protected static final long SPLASH_TIMER = 1000L;
    private Handler mHandler;
    private ServerInterworking mSi;
    private static final int SHOW_PROGRESS_BAR = 101;
    private static final int SHOW_COMMUNICATION_ERROR = 102;
    private static final int NEED_PERMISSION_ACKNOWLEDGE = 103;
    private ProgressDialog mDialog;

    private int db_version = 0;
    private String url;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mHandler = new Handler();
        mSi = ServerInterworking.getInstance(getBaseContext());

        getServiceParameterInformation();
    }

    private void getServiceParameterInformation() {
        mSi.registerCallback(this);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showDialog(SHOW_PROGRESS_BAR);
            }
        });
        mSi.initGettingServiceParameterInfo(Constants.RETRIEVE_PARAMETER_URL);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case SHOW_PROGRESS_BAR: {
                mDialog = new ProgressDialog(this);
                mDialog.setTitle(R.string.title_db_upgrade_progress);
                mDialog.setMessage(getString(R.string.msg_db_upgrade_progress));
                mDialog.setIndeterminate(true);
                return mDialog;
            }
            case SHOW_COMMUNICATION_ERROR: {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setTitle(R.string.title_communication_error);
                builder.setMessage(R.string.msg_communication_error);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setNeutralButton(R.string.confirm_msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return dialog;
            }
            case NEED_PERMISSION_ACKNOWLEDGE: {
                Toast.makeText(this, "외부저장장치의 접근이 거부됨.", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setTitle(R.string.title_permission_acknowledge);
                builder.setMessage(R.string.msg_permission_acknowledge);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setNeutralButton(R.string.confirm_msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return dialog;
            }
            default:
                return null;
        }
    }

    /*
        "classic_db" : "version_code=1,url=5034",key=yyyy
     */
    @Override
    public void callbackReturnServiceParameter(String result) {
        Log.i("debug", "result : " + result);
        if(result.isEmpty()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(), R.string.msg_communication_error, Toast.LENGTH_LONG).show();
                    if(mDialog != null)
                        mDialog.dismiss();
                }
            });
            showDialog(SHOW_COMMUNICATION_ERROR);
            return;
        }
        String[] data = result.split(",");      // data[0]: version_code=1, data[1]: url=xxxx(5034), data[2]: key=yyyyy
        String[] version = data[0].split("=");  // version[0]: version_code, version[1]: 1
        String[] urls = data[1].split("=");     // urls[0]: url, urls[1]: xxxx
        String[] keys = data[2].split("=");     // keys[0]: key, keys[1]: yyyyy
        url = urls[1];
        key = keys[1];
        db_version = Integer.valueOf(version[1]);
        int prefValue = MusicUtils.getConfigInteger(getApplicationContext(), Constants.KEY_DB_VERSION, 0);
//		Log.i(TAG, "db_version = " + db_version + ", prefValue = " + prefValue);
        if(db_version > prefValue) {	// 최근 것
            // 위험 권한 부여 요청
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SD 카드에 접근할 권한 없음.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                mSi.registerCallbackForDownloadFile(this);
                mSi.initDownloadFileContent(Constants.GET_DATABASE_URL, url, key, Constants.CLASSIC_DB_NAME);
            }
        }
        else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.complete_data_download, Toast.LENGTH_SHORT).show();
                    if(mDialog != null)
                        mDialog.dismiss();
                }
            });
            callGotoMainByDelay(SPLASH_TIMER);
        }
    }

    @Override
    public void callbackReturnDownloadFinish() {
        StringBuilder sb = new StringBuilder(MusicUtils.getDownloadExternalStorageDirectoy());
        sb.append("/").append(Constants.CLASSIC_DB_NAME);
        moveDBFromSDCardToDataDirectory(sb.toString());
        MusicUtils.saveConfig(getApplicationContext(), Constants.KEY_DB_VERSION, db_version);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), R.string.complete_data_download, Toast.LENGTH_SHORT).show();
                if(mDialog != null)
                    mDialog.dismiss();
            }
        });
        callGotoMainByDelay(SPLASH_TIMER);
    }

    public void moveDBFromSDCardToDataDirectory(String localPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(localPath);
            out = new FileOutputStream(makeDatabaseStoragePath());
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(in != null)
                    in.close();
                if(out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Delete temporary file
        File dbFile = new File(localPath);
        if(dbFile.exists())
            dbFile.delete();
    }

    private String makeDatabaseStoragePath() {
        StringBuilder sb = new StringBuilder(Environment.getDataDirectory().getAbsolutePath());
        sb.append("/data/").append(getPackageName()).append("/databases/");
        File localDBDir = new File(sb.toString());
        if(!localDBDir.exists())    localDBDir.mkdir();
        sb.append(Constants.CLASSIC_DB_NAME);
        return sb.toString();
    }

    private void callGotoMainByDelay(long delay) {
        // Timer activation for ProgrssBar update
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, delay);	// 3000(3초) time interval
    }

    protected void TimerMethod() {
        Intent intent = new Intent(SplashActivity.this, com.talanton.music.player.main.MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "외부저장장치의 접근이 허용함.", Toast.LENGTH_LONG).show();
                    mSi.registerCallbackForDownloadFile(this);
                    mSi.initDownloadFileContent(Constants.GET_DATABASE_URL, url, key, Constants.CLASSIC_DB_NAME);
                } else {
                    showDialog(NEED_PERMISSION_ACKNOWLEDGE);
                }
                return;
            }
        }
    }
}
