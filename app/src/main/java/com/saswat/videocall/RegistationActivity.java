package com.saswat.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistationActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phonetext;
    private EditText codetext;
    private Button nextbtn;
    private String checker = "",phoneNumber = "";
    private RelativeLayout relativeLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registation);

        mAuth = FirebaseAuth.getInstance();
        progressbar = new ProgressDialog(this);


        phonetext = findViewById(R.id.phoneText);
        codetext = findViewById(R.id.codeText);
        nextbtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phonetext);


        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(nextbtn.getText().equals("Submit") || checker.equals("Code Sent")){
                    String verificationCode = codetext.getText().toString();
                    if (verificationCode.equals("")){
                        Toast.makeText(RegistationActivity.this, "Please write the Verfiv=cation code", Toast.LENGTH_SHORT).show();
                    }else{
                        progressbar.setTitle("Code Verification");
                        progressbar.setMessage("Please wait, while we are verifying your Code");
                        progressbar.setCanceledOnTouchOutside(false);
                        progressbar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
               }else{
                   phoneNumber = ccp.getFullNumberWithPlus();
                   if(!phoneNumber.equals("")){
                        progressbar.setTitle("Phone Number Verification");
                        progressbar.setMessage("Please wait, while we are verifying your Phone Number");
                        progressbar.setCanceledOnTouchOutside(false);
                        progressbar.show();
                       PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60 , TimeUnit.SECONDS ,RegistationActivity.this ,mCallbacks);
                   }else{
                       Toast.makeText(RegistationActivity.this, "Please Enter a valid Number", Toast.LENGTH_SHORT).show();
                   }
               }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(RegistationActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
                progressbar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);
                nextbtn.setText("Continue");
                codetext.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                
                mVerificationId = s;
                mResendToken = forceResendingToken;
                        
                        
                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                nextbtn.setText("Submit");
                codetext.setVisibility(View.VISIBLE);

                Toast.makeText(RegistationActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser !=null){
            Intent homeintent = new Intent(RegistationActivity.this,MainActivity.class);
            startActivity(homeintent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressbar.dismiss();
                            Toast.makeText(RegistationActivity.this, "Conguration you have Login", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            progressbar.dismiss();
                            String e = task.getException().toString();
                            Toast.makeText(RegistationActivity.this, "Error" + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendUserToMainActivity(){
        Intent intent = new Intent(RegistationActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
