 package fr.spirotron.spirotest;
 
 import android.app.Activity;
import android.content.Context;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity implements OnTouchListener {
 	private SoundPool soundPool;
 	private int soundId;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
 		soundId = soundPool.load(getApplicationContext(), R.raw.click, 1);
 		
 		ImageView view = (ImageView) findViewById(R.id.imageView1);
 		view.setOnTouchListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	public void playSound(View view) {
	    AudioManager mgr = (AudioManager)view.getContext().getSystemService(Context.AUDIO_SERVICE);
	    float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
	    float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
	    float volume = streamVolumeCurrent / streamVolumeMax;
		soundPool.play(soundId, volume, volume, 1, 0, 1f);
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		Log.d("TEST", "motionevent: "+event.getAction());
 		
 		switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				playSound(v);
 				return true;
 		}
 		
 		return false;
 	}
 }
