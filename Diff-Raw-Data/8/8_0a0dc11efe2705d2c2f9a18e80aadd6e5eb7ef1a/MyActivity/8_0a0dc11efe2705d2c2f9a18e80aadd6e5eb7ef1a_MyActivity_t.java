 package com.zabozhanov.chilly;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.*;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.zabozhanov.chilly.chilly_player.ChillyDelegate;
 import com.zabozhanov.chilly.chilly_player.ChillyService;
 
 public class MyActivity extends Activity implements View.OnClickListener, ChillyDelegate {
 
     private ImageButton _playButton;
     private ImageButton _pauseButton;
 
     private TextView _statusView;
 
     private Boolean isPlaying = true;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         //Remove title bar
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 
         setContentView(R.layout.main);
         _playButton = (ImageButton) findViewById(R.id.imgPlay);
         _playButton.setOnClickListener(this);
 
         _pauseButton = (ImageButton) findViewById(R.id.imgPause);
         _pauseButton.setOnClickListener(this);
 
         _statusView = (TextView) findViewById(R.id.lblStatus);
 
         startService(new Intent(this, ChillyService.class));
         doBindService();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add("Exit");
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         if (item.getTitle() == "Exit") {
             doUnbindService();
             stopService(new Intent(this, ChillyService.class));
             finish();
             System.exit(0);
         }
         return super.onMenuItemSelected(featureId, item);
     }
 
     @Override
     public void onClick(View view) {
 
         switch (view.getId()) {
             case R.id.imgPause:
             {
                 _pauseButton.setVisibility(View.GONE);
                 _playButton.setVisibility(View.VISIBLE);
                 break;
             }
             case R.id.imgPlay:
             {
                 _playButton.setVisibility(View.GONE);
                 _pauseButton.setVisibility(View.VISIBLE);
                 break;
             }
         }
 
         isPlaying = !isPlaying;
 
         if (null != _BoundService) {
             _BoundService.playPause();
         }
     }
 
     private void setStatus(String status) {
         _statusView.setText( status);
     }
 
     @Override
     public void playing() {
         //setStatus("");
         _pauseButton.setEnabled(true);
         _playButton.setVisibility(View.GONE);
         _pauseButton.setVisibility(View.VISIBLE);
     }
 
     @Override
     public void preparing() {
         setStatus("загрузка...");
         _pauseButton.setEnabled(false);
     }
 
     @Override
     public void paused() {
         //setStatus("");
         _pauseButton.setVisibility(View.GONE);
         _playButton.setVisibility(View.VISIBLE);
     }
 
     @Override
     public void setCurrentTrack(String track) {
         if (track != null) {
             _statusView.setText(track);
         } else {
             _statusView.setText("Null track");
         }
     }
 
     private  ChillyService _BoundService = null;
     private Boolean _IsBound = false;
 
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             _BoundService = ((ChillyService.ChillyBinder)service).getService();
             //Toast.makeText(getApplicationContext(), "Chilly connected", Toast.LENGTH_SHORT).show();
             _BoundService.initPlayback(MyActivity.this);
             _BoundService.setDelegate(MyActivity.this);
         }
 
         public void onServiceDisconnected(ComponentName className) {
             _BoundService = null;
         }
     };
 
     void doBindService() {
         bindService(new Intent(MyActivity.this,
                 ChillyService.class), mConnection, Context.BIND_AUTO_CREATE);
         _IsBound = true;
     }
 
     void doUnbindService() {
         if (_IsBound) {
             unbindService(mConnection);
             _IsBound = false;
         }
     }
 
     @Override
     protected void onDestroy() {
         doUnbindService();
         super.onDestroy();
     }
 
 }
