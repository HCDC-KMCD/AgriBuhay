package com.AgriBuhayProj.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.AgriBuhayProj.app.R;
import com.AgriBuhayProj.app.ReusableCode.ReusableCodeForAll;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ChefVerifyPhone extends AppCompatActivity {

    String verificationId;
    FirebaseAuth FAuth;
    Button verify;
    Button Resend;
    TextView txt;
    EditText entercode;
    String phonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_verify_phone);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Verify");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChefVerifyPhone.this, ChooseOne.class));
            }
        });

        phonenumber = getIntent().getStringExtra("phonenumber").trim();

        sendverificationcode(phonenumber);
        entercode = (EditText) findViewById(R.id.phoneno);
        txt = (TextView) findViewById(R.id.text);
        Resend = (Button) findViewById(R.id.Resendotp);
        FAuth = FirebaseAuth.getInstance();
        verify = (Button) findViewById(R.id.Verify);
        Resend.setVisibility(View.INVISIBLE);
        txt.setVisibility(View.INVISIBLE);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = entercode.getText().toString().trim();
                Resend.setVisibility(View.INVISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    if (code.isEmpty() && code.length() < 6) {
                        entercode.setError("Enter code");
                        entercode.requestFocus();
                        return;
                    }
                }
                verifyCode(code);
            }
        });

        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txt.setVisibility(View.VISIBLE);
                txt.setText("Resend Code within " + millisUntilFinished / 1000 + " Seconds");
            }

            @Override
            public void onFinish() {
                Resend.setVisibility(View.VISIBLE);
                txt.setVisibility(View.INVISIBLE);

            }
        }.start();

        Resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Resend.setVisibility(View.INVISIBLE);
                Resendotp(phonenumber);

                new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        txt.setVisibility(View.VISIBLE);
                        txt.setText("Resend Code within " + millisUntilFinished / 1000 + " Seconds");
                    }

                    @Override
                    public void onFinish() {
                        Resend.setVisibility(View.VISIBLE);
                        txt.setVisibility(View.INVISIBLE);

                    }
                }.start();

            }
        });

    }

    private void Resendotp(String phonenumber) {

        sendverificationcode(phonenumber);
    }


    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        linkCredential(credential);
    }

    private void linkCredential(PhoneAuthCredential credential) {

        FAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(ChefVerifyPhone.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            
                            Intent intent = new Intent(ChefVerifyPhone.this,MainMenu.class);
                            startActivity(intent);
                            finish();


                        } else {
                            ReusableCodeForAll.ShowAlert(ChefVerifyPhone.this,"Error",task.getException().getMessage());
                        }
                    }
                });
    }

    //Error here
    private void sendverificationcode(String number) {

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FAuth).setPhoneNumber(phonenumber).setTimeout(60L,TimeUnit.SECONDS).setActivity(this).setCallbacks(mCallBack).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {


            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                entercode.setText(code);
                verifyCode(code);

            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            Toast.makeText(ChefVerifyPhone.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };
}

