package com.saswat.videocall;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navView;
    RecyclerView recyclerView;
    ImageView imageView;
    private DatabaseReference contactRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String user_name="" , profile_img="";
    private String call_by="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        recyclerView = (RecyclerView)findViewById(R.id.main_contacts_list);
        imageView = (ImageView)findViewById(R.id.find_people_btn);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent find_people = new Intent(MainActivity.this,FindpeopleActivity.class);
                startActivity(find_people);
            }
        });

    }



    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.navigation_home :
                    Intent mainIntent = new Intent(MainActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    break;
                case R.id.navigation_settings :
                    Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case R.id.navigation_notifications :
                    Intent notificationIntent = new Intent(MainActivity.this,NotificationActivity.class);
                    startActivity(notificationIntent);
                    break;
                case R.id.navigation_logout :
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent = new Intent(MainActivity.this,RegistationActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;



            }
            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        checkUserIsFree();

        validatedUser();

        FirebaseRecyclerOptions<contacts> options
                = new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(contactRef.child(currentUserId) , contacts.class).build();

        FirebaseRecyclerAdapter<contacts,ContactsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull contacts contacts) {
                final String listUserId = getRef(i).getKey();
                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            user_name = dataSnapshot.child("name").getValue().toString();
                            profile_img = dataSnapshot.child("image").getValue().toString();

                            holder.videocall_user_name.setText(user_name);
                            Picasso.get().load(profile_img).into(holder.contact_user_profile);

                        }

                        holder.calling_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent callingIntent = new Intent(MainActivity.this , CallingActivity.class);
                                callingIntent.putExtra("visit_user_id" , listUserId);
                                startActivity(callingIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                ContactsViewHolder ViewHolder = new ContactsViewHolder(view);
                return ViewHolder;
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        TextView videocall_user_name;
        Button calling_btn;
        ImageView contact_user_profile;
        public ContactsViewHolder(@NonNull View viewholder) {
            super(viewholder);

            videocall_user_name = (TextView)viewholder.findViewById(R.id.video_call_user_name);
            calling_btn = (Button)viewholder.findViewById(R.id.btn_video_call);
            contact_user_profile = (ImageView)viewholder.findViewById(R.id.video_call_friends_list_notitfy);

        }
    }

    private void validatedUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("User").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent settingIntent = new Intent(MainActivity.this , SettingsActivity.class);
                startActivity(settingIntent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkUserIsFree() {

        userRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("ringing")){
                    call_by = dataSnapshot.child("ringing").getValue().toString();
                    Intent callingIntent = new Intent(MainActivity.this , CallingActivity.class);
                    callingIntent.putExtra("visit_user_id" , call_by);
                    startActivity(callingIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
