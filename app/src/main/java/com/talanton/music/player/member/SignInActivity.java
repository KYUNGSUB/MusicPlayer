package com.talanton.music.player.member;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.talanton.music.player.R;
import com.talanton.music.player.interwork.ServerInterworking;
import com.talanton.music.player.utils.Constants;
import com.talanton.music.player.utils.MusicUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity implements ServerInterworking.ISignInResult {
    EditText idEt;
    EditText pwEt;

    String id;
    String pw;

    ServerInterworking mSi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSi = ServerInterworking.getInstance(this);
        idEt = (EditText)findViewById(R.id.textId);
        idEt.setHintTextColor(Color.BLACK);
        pwEt = (EditText)findViewById(R.id.textPassword);
        pwEt.setHintTextColor(Color.BLACK);

        ImageButton signup = (ImageButton)findViewById(R.id.signup_goto_btn);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        ImageButton signin = (ImageButton)findViewById(R.id.login_btn);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signinProcession();
            }
        });
    }

    private void signinProcession() {
        id = idEt.getText().toString();
        if(id.isEmpty()) {
            showToast(R.string.help_id_input);
            return;
        }
        pw = pwEt.getText().toString();
        if(pw.isEmpty()) {
            showToast(R.string.help_password_input);
            return;
        }
        mSi.registerISignInResult(this);
        mSi.initSignIn(Constants.SIGNIN_URL, id, pw);
    }

    private void showToast(int msgId) {
        Toast.makeText(this, msgId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void getSignInResult(String response) {
        try {
            JSONObject responseJSON = new JSONObject(response);
            int result = (int)responseJSON.get("result");
            if(result == 1) {   // 로그인 성공
                String sessionId = (String)responseJSON.get("id");
                MusicUtils.saveConfig(this, Constants.LOGIN_SESSION_ID, sessionId);
                showToast(R.string.help_login_success);
                setResult(RESULT_OK);
                finish();
            } else {    // 로그인 실패
                showToast(R.string.help_check_input_data);
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG, "Error result data");
        }
    }
}
