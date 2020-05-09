package com.saswat.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    String reciever_user_id="" , reciever_user_image="" , reciever_user_name="";
    private ImageView user_image_picture;
    private Button add_friend , cancel_friend ;
    private TextView user_profile_name;
    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState = "new";
    private DatabaseReference friendRequestRef , contactRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        reciever_user_id = getIntent().getExtras().get("Vister_user_profile").toString();
        reciever_user_image = getIntent().getExtras().get("Profile_image").toString();
        reciever_user_name = getIntent().getExtras().get("Profile_name").toString();

        user_image_picture = (ImageView)findViewById(R.id.user_image_picture);
        add_friend = (Button)findViewById(R.id.add_friend );
        cancel_friend = (Button)findViewById(R.id.cancel_friend);
        user_profile_name = (TextView)findViewById(R.id.user_profile_name);

        Picasso.get().load(reciever_user_image).into(user_image_picture);
        user_profile_name.setText(reciever_user_name);

        manageClickEvents();
    }

    private void manageClickEvents() {

        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(reciever_user_id)){
                    String requestType = dataSnapshot.child(reciever_user_id).child("request_type")
                            .getValue().toString();
                    if(requestType.equals("sent")){
                        currentState = "request_sent";
                        add_friend.setText("Cancel Friend Request");
                        add_friend.setBackgroundColor(getResources().getColor(R.color.btn_color_red));
                    }else if(requestType.equals("received")){
                        currentState = "request_received";
                        add_friend.setText("Accept Friend Request");
                        cancel_friend.setVisibility(View.VISIBLE);
                        cancel_friend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelFriendRequest();
                            }
                        });
                    }else{
                        contactRef.child(senderUserId).addListenerForSingleValueEvent
                                (new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(dataSnapshot.hasChild(reciever_user_id)){
                                    currentState="friends";
                                    add_friend.setText("Delete Contact");
                                }else {
                                    currentState = "new";
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(senderUserId.equals(reciever_user_id)){
            add_friend.setVisibility(View.GONE);
        }if(!senderUserId.equals(reciever_user_id)){
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(currentState.equals("new")){
                        sendFriendRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelFriendRequest();
                    }
                    if(currentState.equals("request_received")){
                        acceptFriendRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelFriendRequest();
                    }
                }
            });
        }

    }

    private void acceptFriendRequest() {
        contactRef.child(senderUserId).child(reciever_user_id).child("Contact")
                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactRef.child(reciever_user_id).child(senderUserId).child("Contact")
                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                friendRequestRef.child(senderUserId).child(reciever_user_id).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    friendRequestRef.child(reciever_user_id).child(senderUserId)
                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                currentState = "friend";
                                                                add_friend.setText("Delete Contact..");
                                                                add_friend.setBackgroundColor(getResources().getColor(R.color.btn_color_red));
                                                                cancel_friend.setVisibility(View.GONE);

                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(reciever_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(reciever_user_id).child(senderUserId)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        currentState = "new";
                                        add_friend.setText("Add Friend");
                                        add_friend.setBackgroundColor(getResources().getColor(R.color.btn_color_green));
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(senderUserId).child(reciever_user_id).child("request_type").setValue("sent")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendRequestRef.child(reciever_user_id).child(senderUserId).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        currentState = "request_sent";
                                        add_friend.setText("Cancel Friend Request");
                                        add_friend.setBackgroundColor(getResources().getColor(R.color.btn_color_red));
                                        Toast.makeText(ProfileActivity.this, "Friend Request sent...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }
}
