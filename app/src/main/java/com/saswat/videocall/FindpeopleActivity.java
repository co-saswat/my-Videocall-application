package com.saswat.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindpeopleActivity extends AppCompatActivity {
    private RecyclerView frecyclerView;
    private EditText friendlist;
    private String chars="";
    private DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpeople);

        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        frecyclerView = (RecyclerView)findViewById(R.id.find_people_contact_list);
        friendlist = (EditText)findViewById(R.id.findpeople_search_btn);
        frecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        friendlist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(friendlist.getText().toString().equals("")){
                    Toast.makeText(FindpeopleActivity.this, "Please write a name...", Toast.LENGTH_SHORT).show();
                }else{
                    chars = charSequence.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    protected void onStart() {

        super.onStart();


        FirebaseRecyclerOptions<contacts> options = null;

        if(chars.equals("")){
            options = new  FirebaseRecyclerOptions.Builder<contacts>().setQuery(userRef,contacts.class).build();
        }else {
            options = new  FirebaseRecyclerOptions.Builder<contacts>().setQuery(userRef.orderByChild("name").startAt(chars).endAt(chars + "\uf8ff"),contacts.class).build();
        }

        FirebaseRecyclerAdapter<contacts,FindFriendViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int i, @NonNull final contacts model) {
                holder.user_name_request_video_call.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.present_user_profile);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String vister_user_name = getRef(i).getKey();
                        Intent find_intent = new Intent(FindpeopleActivity.this,ProfileActivity.class);
                        find_intent.putExtra("Vister_user_profile",vister_user_name);
                        find_intent.putExtra("Profile_image",model.getImage());
                        find_intent.putExtra("Profile_name",model.getName());
                        startActivity(find_intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                FindFriendViewHolder friendViewHolder = new FindFriendViewHolder(view);
                return friendViewHolder;
            }
        };
        frecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{
        TextView user_name_request_video_call;
        Button video_call ;
        ImageView present_user_profile;
        RelativeLayout card_relativelayout_contacts;
        public FindFriendViewHolder(@NonNull View viewholder) {
            super(viewholder);

            user_name_request_video_call = (TextView)viewholder.findViewById(R.id.video_call_user_name);
            video_call = (Button)viewholder.findViewById(R.id.btn_video_call);
            present_user_profile = (ImageView)viewholder.findViewById(R.id.video_call_friends_list_notitfy);
            card_relativelayout_contacts = (RelativeLayout)viewholder.findViewById(R.id.card_view_relativelayout1);

        }
    }

}
