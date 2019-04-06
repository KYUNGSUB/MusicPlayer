package com.talanton.music.player.member;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.talanton.music.player.R;
import com.talanton.music.player.interwork.ServerInterworking;
import com.talanton.music.player.utils.Constants;
import com.talanton.music.player.widget.TitleBitmapButton;

public class SignUpActivity extends AppCompatActivity implements ServerInterworking.ISignUpResult, ServerInterworking.IDuplicateCheckID {
    ServerInterworking mSi;
    ImageButton signupExecution;
    ImageButton viaFacebook;
    ImageButton viaKakao;
    ImageButton returnSignin;
    TitleBitmapButton duplicateCheck;

    String idtxt;
    String usernametxt;
    String passwordtxt;
    String password2txt;
    String mobiletxt;

    EditText id;
    EditText password;
    EditText password2;
    EditText username;
    EditText mobile;

    boolean duplicateCheckPass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mSi = ServerInterworking.getInstance(this);
        id = (EditText)findViewById(R.id.textId);
        id.setHintTextColor(Color.BLACK);
        password = (EditText)findViewById(R.id.textPassword);
        password.setHintTextColor(Color.BLACK);
        password2 = (EditText)findViewById(R.id.textPassword2);
        password2.setHintTextColor(Color.BLACK);
        username = (EditText)findViewById(R.id.textName);
        username.setHintTextColor(Color.BLACK);
        mobile = (EditText)findViewById(R.id.textMobile);
        mobile.setHintTextColor(Color.BLACK);

        signupExecution = (ImageButton) findViewById(R.id.btnSignupExecution);
        viaFacebook = (ImageButton) findViewById(R.id.btnViaFacebook);
        viaKakao = (ImageButton) findViewById(R.id.btnViaKakao);
        returnSignin = (ImageButton) findViewById(R.id.btnReturnSignIn);
        duplicateCheck = (TitleBitmapButton)findViewById(R.id.duplicationCheck);

        duplicateCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duplicateIdCheck();
            }
        });

        signupExecution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupProcessing();
            }
        });
        returnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void duplicateIdCheck() {
        idtxt = id.getText().toString();
        if(idtxt.isEmpty()) {
            showToast(getString(R.string.help_id_input));
            return;
        }

        mSi.registerIDuplicateCheckIDResult(SignUpActivity.this);
        mSi.initDuplicateCheckID(Constants.DUPLICATE_CHECK_ID_URL, idtxt);
    }

    private void signupProcessing() {
        idtxt = id.getText().toString();
        if(idtxt.isEmpty()) {
            showToast(getString(R.string.help_id_input));
            return;
        }
        if(duplicateCheckPass == false) {
            showToast(getString(R.string.help_use_duplicate_check));
            return;
        }
        passwordtxt = password.getText().toString();
        if(passwordtxt.isEmpty()) {
            showToast(getString(R.string.help_password_input));
            return;
        }
        password2txt = password2.getText().toString();
        if(password2txt.isEmpty()) {
            showToast(getString(R.string.help_password2_input));
            return;
        }
        if(!passwordtxt.equals(password2txt)) {
            showToast(getString(R.string.help_password_not_equal));
            return;
        }
        usernametxt = username.getText().toString();
        if(usernametxt.isEmpty()) {
            showToast(getString(R.string.help_username_input));
            return;
        }
        mobiletxt = mobile.getText().toString();
        if(mobiletxt.isEmpty()) {
            showToast(getString(R.string.help_mobile_input));
            return;
        }

        mSi.registerISignUpResult(SignUpActivity.this);
        mSi.initSignUp(Constants.SIGNUP_URL, idtxt, passwordtxt, usernametxt, mobiletxt);
    }

    private void showToast(String msg) {
        Toast.makeText(SignUpActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void getSignUpResult(String result) {
        if(result.equals("ok")) {   // 회원가입 성공
            showToast(getString(R.string.help_success_signup));
            finish();
        } else {    // 회원가입 실패
            showToast(getString(R.string.help_failure_signup));
        }
    }

    @Override
    public void getDuplicateCheckIDResult(String result) {
        if(result.equals("true")) { // 아이디 중복되지 않음
            showToast(getString(R.string.help_non_duplicated_id));
            duplicateCheckPass = true;
        } else {
            showToast(getString(R.string.help_is_duplicated_id));
            duplicateCheckPass = false;
        }
    }
}
