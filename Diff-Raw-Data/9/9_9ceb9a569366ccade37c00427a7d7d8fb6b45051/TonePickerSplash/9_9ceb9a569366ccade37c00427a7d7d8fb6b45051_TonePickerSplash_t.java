 package com.hlidskialf.android.tonepicker;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.os.Build;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.content.Intent;
 import android.content.Context;
 import android.view.View;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import android.provider.Settings;
 
 public class TonePickerSplash extends Activity
                   implements Button.OnClickListener
 {
   public static final int REQUEST_RINGTONE = 42;
   public static final int REQUEST_NOTIFICATION = 43;
   public static final int REQUEST_ALARM = 44;
 
   private AudioManager mAudioManager;
   private MenuItem mAboutItem;
 
   private class ViewHolder
   {
     String existing_uri;
     int request_code;
     public ViewHolder(String existing_uri, int request_code) { 
       this.existing_uri = existing_uri;
       this.request_code = request_code;
     }
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.splash);
 
     mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 
     initVolumeSlider(R.id.volume_ringer, R.id.volume_text_ringer, AudioManager.STREAM_RING);
     initVolumeSlider(R.id.volume_music, R.id.volume_text_music, AudioManager.STREAM_MUSIC);
     initVolumeSlider(R.id.volume_call, R.id.volume_text_call, AudioManager.STREAM_VOICE_CALL);
     initVolumeSlider(R.id.volume_system, R.id.volume_text_system, AudioManager.STREAM_SYSTEM);
     initVolumeSlider(R.id.volume_alarm, R.id.volume_text_alarm, AudioManager.STREAM_ALARM);
     if (Integer.valueOf(Build.VERSION.SDK) >= 3) {
      View v = findViewById(R.id.row_notify);
      v.setVisibility(View.VISIBLE); 
       initVolumeSlider(R.id.volume_notify, R.id.volume_text_notify, AudioManager.STREAM_NOTIFICATION);
     }
     if (Integer.valueOf(Build.VERSION.SDK) >= 5) {
      View v = findViewById(R.id.row_dtmf);
      v.setVisibility(View.VISIBLE); 
       initVolumeSlider(R.id.volume_dtmf, R.id.volume_text_dtmf, AudioManager.STREAM_DTMF);
     }
 
 
     Button button;
     String existing;
 
     existing = Settings.System.getString(getContentResolver(), Settings.System.RINGTONE);
     button = (Button) findViewById(R.id.splash_button_ringtone);
     button.setTag(new ViewHolder(existing, REQUEST_RINGTONE));
     button.setOnClickListener(this);
 
     existing = Settings.System.getString(getContentResolver(), Settings.System.NOTIFICATION_SOUND);
     button = (Button) findViewById(R.id.splash_button_notification);
     button.setTag(new ViewHolder(existing, REQUEST_NOTIFICATION));
     button.setOnClickListener(this);
 
     if (Integer.valueOf(Build.VERSION.SDK) >= 5) {
       existing = Settings.System.getString(getContentResolver(), Settings.System.ALARM_ALERT);
       button = (Button) findViewById(R.id.splash_button_alarm);
       button.setVisibility(View.VISIBLE);
       button.setTag(new ViewHolder(existing, REQUEST_ALARM));
       button.setOnClickListener(this);
     }
 
   }
 
   /*Button.OnClickListener*/
   public void onClick(View v) {
     ViewHolder vh = (ViewHolder)v.getTag();
 
     Intent i = new Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER);
     if (vh.existing_uri != null) 
       i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(vh.existing_uri));
     i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
     i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
     startActivityForResult(i, vh.request_code);
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) 
   {
     if (resultCode != RESULT_OK || data == null) return;
 
     Uri u = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
 
     if (u == null) return;
 
     if (requestCode == REQUEST_RINGTONE) {
       Settings.System.putString(getContentResolver(), Settings.System.RINGTONE, u.toString());
     }
     else
     if (requestCode == REQUEST_NOTIFICATION) {
       Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_SOUND, u.toString());
     }
     else
     if (requestCode == REQUEST_ALARM) {
       Settings.System.putString(getContentResolver(), Settings.System.ALARM_ALERT, u.toString());
     }
   }
 
   private void initVolumeSlider(int seekbar_id, int label_id, int stream_id)
   {
     final int stream = stream_id;
     TextView tv = (TextView)findViewById(label_id);
     SeekBar sb = (SeekBar)findViewById(seekbar_id);
     sb.setMax( mAudioManager.getStreamMaxVolume(stream_id) );
     sb.setProgress( mAudioManager.getStreamVolume(stream_id) );
     sb.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
       public void onProgressChanged(SeekBar sb, int progress, boolean touch) {
         if (progress != mAudioManager.getStreamVolume(stream) ) {
           mAudioManager.setStreamVolume(stream, progress, AudioManager.FLAG_SHOW_UI);
         }
       }
       public void onStartTrackingTouch(SeekBar sb) {}
       public void onStopTrackingTouch(SeekBar sb) {}
     });
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
     mAboutItem = menu.add(0, 0, 0, R.string.about);
     mAboutItem.setIcon(android.R.drawable.ic_menu_info_details);
 
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if (item.equals(mAboutItem)) {
       View v = getLayoutInflater().inflate(R.layout.about_dialog,null);
       AlertDialog dia = new AlertDialog.Builder(this)
           .setTitle(R.string.about_title)
           .setView(v)
           .setPositiveButton(android.R.string.ok,null)
           .show();
     }
 
     return super.onOptionsItemSelected(item);
   }
  
 
 }
