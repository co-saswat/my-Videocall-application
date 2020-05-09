package com.saswat.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChartActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {
    private static String API_Key = "46703882";
    private static String SESSION_ID = "2_MX40NjcwMzg4Mn5-MTU4ODA1MjU4MDM5N35ITjA1MkdTcEtqcWNVakZNRzEwY2dYUWZ-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjcwMzg4MiZzaWc9MDBmNjQ2ZTMzNDU0OGE5ZWJkNTY0MDQ1NTJhNjlkNzVhZWMzMWVlODpzZXNzaW9uX2lkPTJfTVg0ME5qY3dNemc0TW41LU1UVTRPREExTWpVNE1ETTVOMzVJVGpBMU1rZFRjRXRxY1dOVmFrWk5SekV3WTJkWVVXWi1mZyZjcmVhdGVfdGltZT0xNTg4MDUyNzM1Jm5vbmNlPTAuNjI3NjU5MzMzMzk2Njg3OCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTkwNjQ0NzMzJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoChartActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERMISSIONS = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;


    private ImageView close_video_chat_call_btn;
    private DatabaseReference userRef;
    private String userId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chart);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        close_video_chat_call_btn =(ImageView)findViewById(R.id.end_call_btn);

        close_video_chat_call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(userId).hasChild("Ringing")){
                            userRef.child(userId).child("Ringing").removeValue();
                            if(mPublisher !=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber !=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChartActivity.this,RegistationActivity.class));
                            finish();
                        }
                        if(dataSnapshot.child(userId).hasChild("Calling")){
                            userRef.child(userId).child("Calling").removeValue();
                            if(mPublisher !=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber !=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChartActivity.this,RegistationActivity.class));
                            finish();
                        }
                        else{
                            startActivity(new Intent(VideoChartActivity.this,RegistationActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
       RequestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChartActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERMISSIONS)
    private void RequestPermissions(){
        String[] perms = {Manifest.permission.INTERNET , Manifest.permission.CAMERA , Manifest.permission.RECORD_AUDIO};

        if(EasyPermissions.hasPermissions(this , perms)){
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            //1.Initialize and Connect to the Sessions
            mSession = new Session.Builder(this , API_Key , SESSION_ID).build();
            mSession.setSessionListener(VideoChartActivity.this);
            mSession.connect(TOKEN);
        }
        else{
            EasyPermissions.requestPermissions(this,"Hey please allow the permissions...",RC_VIDEO_APP_PERMISSIONS,perms);

        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //2.Publishing a stream to the Session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChartActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView)mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");

    }

    //3.Subscribing to the Stream
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");

        if(mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");
        if(mSubscriber !=null){
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Stream Face some Error");

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
