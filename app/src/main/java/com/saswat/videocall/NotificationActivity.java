package com.saswat.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView notify_user_list;
    private DatabaseReference friendRequestRef , contactRef , userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        notify_user_list = (RecyclerView)findViewById(R.id.notify_list);
        notify_user_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new  FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(friendRequestRef.child(currentUserId),contacts.class).build();

        FirebaseRecyclerAdapter<contacts,NotificationViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<contacts, NotificationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationViewHolder holder, int i, @NonNull contacts contacts) {
                holder.accept.setVisibility(View.VISIBLE);
                holder.reject.setVisibility(View.VISIBLE);

                final String listUserId = getRef(i).getKey();
                DatabaseReference request_TypeRef = getRef(i).child("request_type").getRef();
                request_TypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();
                            if(type.equals("received")){
                                holder.card_relativelayout.setVisibility(View.VISIBLE);
                                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){
                                            final String imgStr = dataSnapshot.child("image")
                                                    .getValue().toString();

                                            Picasso.get().load(imgStr).into(holder.request_user_profile);

                                        }
                                            final String nameStr = dataSnapshot.child("name")
                                                    .getValue().toString();
                                            holder.user_name_request.setText(nameStr);
                                            holder.accept.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    contactRef.child(currentUserId).child(listUserId).child("Contact")
                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                contactRef.child(listUserId).child(currentUserId).child("Contact")
                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            friendRequestRef.child(currentUserId).child(listUserId).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                friendRequestRef.child(listUserId).child(currentUserId)
                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()){
                                                                                                            Toast.makeText(NotificationActivity.this, "Contact Saved!!!", Toast.LENGTH_SHORT).show();

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
                                            });
                                            holder.reject.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    friendRequestRef.child(currentUserId).child(listUserId).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        friendRequestRef.child(currentUserId).child(listUserId)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(NotificationActivity.this, "Friend Request Cancelled!!!", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                }
                                            });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                holder.card_relativelayout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends,parent,false);
                NotificationViewHolder ViewHolder = new NotificationViewHolder(view);
                return ViewHolder;
            }
        };
        notify_user_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder{
        TextView user_name_request;
        Button accept , reject;
        ImageView request_user_profile;
        RelativeLayout card_relativelayout;
        public NotificationViewHolder(@NonNull View viewholder) {
            super(viewholder);

            user_name_request = (TextView)viewholder.findViewById(R.id.notify_user_name);
            accept = (Button)viewholder.findViewById(R.id.btn_notify_friend_list_accept);
            reject = (Button)viewholder.findViewById(R.id.btn_notify_friend_list_reject);
            request_user_profile = (ImageView)viewholder.findViewById(R.id.friends_list_notitfy);
            card_relativelayout = (RelativeLayout)viewholder.findViewById(R.id.card_view_relativelayout);

        }
    }

 /*   private void acceptFriendRequest() {

    }

    private void cancelFriendRequest() {

    }*/
}
