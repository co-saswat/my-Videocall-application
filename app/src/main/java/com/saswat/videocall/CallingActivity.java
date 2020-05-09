package com.saswat.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {
    private TextView calling_user_name;
    private ImageView calling_user_profile;
    private ImageView cancelcall_img_btn , makecall_img_btn;
    private String receiverUserId="" , receiverUserName="" , receiverUserImg="";
    private String senderUserId="" , senderUserName="" , senderUserImg="" , checker="";
    private DatabaseReference userRef;
    private String callingID="" , ringingID="";
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        mediaPlayer = MediaPlayer.create(this,R.raw.ringing);

        calling_user_name = (TextView)findViewById(R.id.caller_username);
        calling_user_profile = (ImageView)findViewById(R.id.user_profile_calling);
        cancelcall_img_btn = (ImageView)findViewById(R.id.cancel_a_call);
        makecall_img_btn = (ImageView)findViewById(R.id.make_a_call);

        cancelcall_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                checker="clicked";
                cancelCallingUser();
            }
        });

        makecall_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                final HashMap<String,Object> calling_pickup_map = new HashMap<>();
                calling_pickup_map.put("picked","picked");
                userRef.child(senderUserId).child("Ringing").updateChildren(calling_pickup_map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Intent intent = new Intent(CallingActivity.this,VideoChartActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });

        getAndSetUserProfileInfo();
    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(receiverUserId).exists()){
                    receiverUserImg = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName = dataSnapshot.child(receiverUserId).child("name").getValue().toString();

                    calling_user_name.setText(receiverUserName);
                    Picasso.get().load(receiverUserImg).placeholder(R.drawable.profile_image).into(calling_user_profile);
                }if(dataSnapshot.child(senderUserId).exists()){
                    senderUserImg = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserName = dataSnapshot.child(senderUserId).child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();
        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing")){

                        final HashMap<String , Object> callingInfo = new HashMap<>();
                        callingInfo.put("calling",receiverUserId);

                        userRef.child(senderUserId)
                                .child("Calling")
                                .updateChildren(callingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    final HashMap<String , Object> ringingInfo = new HashMap<>();
                                    ringingInfo.put("ringing",senderUserId);

                                    userRef.child(receiverUserId).child("Ringing").updateChildren(ringingInfo);
                                }
                            }
                        });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling")){
                    makecall_img_btn.setVisibility(View.VISIBLE);

                }
                if(dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")){
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this,VideoChartActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void cancelCallingUser() {
        //From Sender Side...
        userRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("calling")){
                    callingID = dataSnapshot.child("calling").getValue().toString();
                    userRef.child(callingID).child("Ringing").removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        userRef.child(callingID).child("Calling")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Intent call_intent = new Intent(CallingActivity.this,RegistationActivity.class);
                                                startActivity(call_intent);
                                                finish();
                                            }
                                        });
                                    }
                                }
                            });

                }else {
                    startActivity(new Intent(CallingActivity.this,RegistationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //From Receiver Side
        userRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("ringing")){
                    ringingID = dataSnapshot.child("ringing").getValue().toString();
                    userRef.child(ringingID).child("Calling").removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        userRef.child(callingID).child("Ringing")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Intent call_intent = new Intent(CallingActivity.this,RegistationActivity.class);
                                                        startActivity(call_intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });

                }else {
                    startActivity(new Intent(CallingActivity.this,RegistationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
