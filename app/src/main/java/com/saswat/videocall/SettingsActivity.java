package com.saswat.videocall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    private Button savebtn;
    private EditText username , status;
    private ImageView profile;
    private static int picture = 1;
    private Uri image_uri;
    private StorageReference storageReference;
    private String downloadUrl;
    private DatabaseReference userRef;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        savebtn = (Button)findViewById(R.id.settings_save);
        username = (EditText)findViewById(R.id.settings_username);
        status = (EditText)findViewById(R.id.bio_staus);
        profile = (ImageView)findViewById(R.id.setting_profile);
        progressDialog = new ProgressDialog(this);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picture_select = new Intent();
                picture_select.setAction(Intent.ACTION_GET_CONTENT);
                picture_select.setType("image/*");
                startActivityForResult(picture_select,picture);
            }
        });

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserData();
            }
        });

        retriveUserinfo();
    }

    private void saveUserData() {
        final String p_username = username.getText().toString();
        final String p_status = status.getText().toString();

        if(image_uri==null){
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){
                       OnlysavInfowithouProfileImage();
                    }else {
                        Toast.makeText(SettingsActivity.this, "Please sent Image First", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else if(username.equals("")){
            Toast.makeText(this, "Please Write Your Name...", Toast.LENGTH_SHORT).show();
        }else if(status.equals("")){
            Toast.makeText(this, "Please Write Your Status....", Toast.LENGTH_SHORT).show();
        }else{

            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please Wait While we are Updating your Profile...");
            progressDialog.show();


            final StorageReference filepath = storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final UploadTask uploadTask = filepath.putFile(image_uri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    downloadUrl = filepath.getDownloadUrl().toString();
                    return filepath.getDownloadUrl();
                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if(task.isSuccessful()){
                        downloadUrl = task.getResult().toString();
                        HashMap<String,Object> profilrmap = new HashMap<>();
                        profilrmap.put("uid" , FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profilrmap.put("name" ,p_username);
                        profilrmap.put("status" ,p_status);
                        profilrmap.put("image" ,downloadUrl);

                       userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profilrmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                                   startActivity(intent);
                                   finish();
                                   progressDialog.dismiss();

                                   Toast.makeText(SettingsActivity.this, "Profile Setting is Updated...", Toast.LENGTH_SHORT).show();
                               }else{
                                   String e = task.getException().toString();
                                   Toast.makeText(SettingsActivity.this, "Error"+e, Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                    }
                }
            });
        }
    }

    private void OnlysavInfowithouProfileImage() {
        final String p_username = username.getText().toString();
        final String p_status = status.getText().toString();


        if(username.equals("")){
            Toast.makeText(this, "Please Write Your Name...", Toast.LENGTH_SHORT).show();
        }else if(status.equals("")){
            Toast.makeText(this, "Please Write Your Status....", Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please Wait While we are Updating your Profile...");
            progressDialog.show();

            HashMap<String,Object> profilrmap = new HashMap<>();
            profilrmap.put("uid" , FirebaseAuth.getInstance().getCurrentUser().getUid());
            profilrmap.put("name" ,p_username);
            profilrmap.put("status" ,p_status);

            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profilrmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();

                        Toast.makeText(SettingsActivity.this, "Profile Setting is Updated...", Toast.LENGTH_SHORT).show();
                    }else{
                        String e = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error"+e, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==picture && resultCode==RESULT_OK && data!=null){
            image_uri = data.getData();
            profile.setImageURI(image_uri);
        }
    }

    private void retriveUserinfo(){
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String imagedb = dataSnapshot.child("image").getValue().toString();
                    String namedb = dataSnapshot.child("name").getValue().toString();
                    String statusdb = dataSnapshot.child("status").getValue().toString();

                    username.setText(namedb);
                    status.setText(statusdb);
                    Picasso.get().load(imagedb).placeholder(R.drawable.profile_image).into(profile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
