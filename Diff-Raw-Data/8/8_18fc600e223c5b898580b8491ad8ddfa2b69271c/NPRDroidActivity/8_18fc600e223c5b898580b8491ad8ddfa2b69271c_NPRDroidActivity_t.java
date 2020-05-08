 package com.brent.nprdroid;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.brent.nprdroid.MusicService.LocalBinder;
 
 import android.app.ListActivity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class NPRDroidActivity extends ListActivity implements OnClickListener, OnSeekBarChangeListener {
 	private String sdPath; 
 	private List<String> songs = new ArrayList<String>();
 	private String TAG = "NPRDroidActivity";
 	private ImageButton rewindButton, playButton, pauseButton, nextButton;
 	private MusicService musicService;
 	boolean mBound = false;
 	private int mInterval = 1000; // 1 second updates
 	private Handler mHandler;
 	private SeekBar seekBar;
 	SharedPreferences pref;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		updateSongList();
 		rewindButton = (ImageButton) findViewById(R.id.rewindButton);
 		playButton = (ImageButton) findViewById(R.id.playButton);
 		pauseButton = (ImageButton) findViewById(R.id.pauseButton);
 		nextButton = (ImageButton) findViewById(R.id.nextButton);
 		rewindButton.setOnClickListener(this);
 		playButton.setOnClickListener(this);
 		pauseButton.setOnClickListener(this);
 		nextButton.setOnClickListener(this);
 		seekBar = (SeekBar) findViewById(R.id.seekBar);
 		mHandler = new Handler();
 		pref = getSharedPreferences("NPRDownloadPreferences", Context.MODE_PRIVATE);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// Bind to MusicService
 		Intent intent = new Intent(this, MusicService.class);
 		intent.putExtra("messenger", new Messenger(handler));
 		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);		
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		startRepeatingTask();
 		// Highlight currently playing or paused item in list 
 		ListView listView = getListView();
 		if (musicService != null) {
 			int listPosition = musicService.mRetriever.listPosition;
 			View current = listView.getChildAt(listPosition - 1); 		
 			if (current != null) {
 				((TextView) current).setTextColor(Color.YELLOW);
 			}
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		stopRepeatingTask();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		// Unbind from the service
 		if (mBound) {
 			unbindService(mConnection);
 			mBound = false;
 		}
 	}
 
 	Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Log.i(TAG, "position from service: " + msg.arg1);
 			SharedPreferences.Editor editor = pref.edit();			
 			ListView listView = getListView();
			int newPosition = msg.arg1 - 1;
			View vCurrent = listView.getChildAt(newPosition);
 			((TextView) vCurrent).setTextColor(Color.YELLOW);
 			View vPrevious = listView.getChildAt(pref.getInt("listPosition", 0));	//change previously played item back to default color 
 			((TextView) vPrevious).setTextColor(Color.LTGRAY);
			editor.putInt("listPosition", newPosition);	//save this position so its list item can be changed later
 			editor.commit();
 		}
 	};
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	private ServiceConnection mConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName className,	IBinder service) {
 			// We've bound to MusicService, cast the IBinder and get MusicService instance
 			Log.i(TAG, "onServiceConnected");
 			LocalBinder binder = (LocalBinder) service;
 			musicService = binder.getService();
 			mBound = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 	};
 
 	Runnable mStatusChecker = new Runnable() {
 		@Override 
 		public void run() {
 			TextView textRemaining = (TextView) findViewById(R.id.textRemaining);
 			TextView textDuration = (TextView) findViewById(R.id.textDuration);
 			mHandler.postDelayed(mStatusChecker, mInterval);
 			if (musicService != null) {
 				int playerPosition = musicService.getPlayerPosition();
 				int duration = musicService.getDuration();
 				seekBar.setMax(duration);
 				seekBar.setProgress(playerPosition);
 				int remaining = duration - playerPosition;				
 				textRemaining.setText(timeString(remaining));
 				textDuration.setText(timeString(duration));
 			}
 			Log.i(TAG, "musicService is NULL!");
 		}
 	};
 
 	void startRepeatingTask() {
 		mStatusChecker.run(); 
 	}
 
 	void stopRepeatingTask() {
 		mHandler.removeCallbacks(mStatusChecker);
 	}
 	
 	private String timeString(int totalSeconds) {
 		int minutes = (totalSeconds)/60;
 		int intSeconds = (totalSeconds)%60;
 		String seconds = intSeconds > 9 ? "" + intSeconds : "0" + intSeconds;
 		return (minutes + ":" + seconds);
 	}
 
 	private void updateSongList() {
 		sdPath = getExternalFilesDir(null).getAbsolutePath() + "/";
 		Log.i(TAG , sdPath);
 		File sdPathFile = new File(sdPath);
 		File[] files = sdPathFile.listFiles();
 		if (files.length > 0) {
 			for (File file : files) {
 				songs.add(file.getName());
 			}
 			ArrayAdapter<String> songList = new ArrayAdapter<String>(this, R.layout.song_item, songs);
 			setListAdapter(songList);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == playButton) {
 			startService(new Intent(MusicService.ACTION_PLAY));			
 			playButton.setImageResource(R.drawable.play_button_pressed);   
 			pauseButton.setImageResource(R.drawable.pause_button_normal);
 		}
 		else if (v == pauseButton) {
 			startService(new Intent(MusicService.ACTION_PAUSE));
 			pauseButton.setImageResource(R.drawable.pause_button_pressed);
 			playButton.setImageResource(0);
 			playButton.setImageResource(R.drawable.play_button_normal);
 		}
 		else if (v == nextButton) {
 			startService(new Intent(MusicService.ACTION_SKIP));
 			nextButton.setImageResource(R.drawable.next_button_pressed);
 			nextButton.setImageResource(0); //restores button to "normal", where R.drawable.next_button_normal produces weird effects
 			playButton.setImageResource(R.drawable.play_button_pressed);
 			pauseButton.setImageResource(R.drawable.pause_button_normal);
 		}	
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Log.i(TAG, "onListItemClick");
 		Intent intent = new Intent(MusicService.ACTION_URL);
 		intent.putExtra("fileName", songs.get(position));
 		intent.putExtra("listPosition", position + 1);
 		playButton.setImageResource(R.drawable.play_button_pressed);
 		pauseButton.setImageResource(R.drawable.pause_button_normal);
 		startService((intent));
 	}
 
 	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
 		@Override
 		protected String doInBackground(String... urls) {
 			int index = 1;
 			for (String url : urls) {
 				DefaultHttpClient client = new DefaultHttpClient();
 				HttpGet httpGet = new HttpGet(url);
 				final String urlCopy = url;
 				runOnUiThread(new Runnable() {
 					public void run() {
 						TextView urlText = (TextView) findViewById(R.id.urlText);
 						urlText.setMovementMethod(new ScrollingMovementMethod());
 						urlText.setText(urlCopy);
 					}
 				});						
 				try {
 					HttpResponse execute = client.execute(httpGet);
 					InputStream content = execute.getEntity().getContent();
 					byte[] buffer = new byte[1024];
 					int length;
 					File audioFile = new File(getExternalFilesDir(null), index + ".mp3");
 					FileOutputStream out = new FileOutputStream(audioFile);
 					while ((length = content.read(buffer)) > 0) {
 						out.write(buffer, 0, length);
 					}
 					++index;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			updateSongList();
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 		}
 	}
 
 	public void readWebpage(View view) {	
 		String showChoice;
 		if (view.getId() == R.id.me) showChoice = "me";
 		else showChoice = "atc";
 		Log.i(TAG, "button text: " + ((Button)view).getText());
 		Calendar calNow = Calendar.getInstance();
 		int year = calNow.get(Calendar.YEAR);
 		int intMonth = calNow.get(Calendar.MONTH) + 1;
 		String month = intMonth > 9 ? "" + intMonth : "0" + intMonth;
 		int intDay = calNow.get(Calendar.DATE);
 		String day = intDay > 9 ? "" + intDay : "0" + intDay;
 		String prepend;
 		String URL[] = new String[25];
 		for (int i = 0; i < 25; ++i) {
 			if (i < 9) 
 				prepend = "_0";
 			else
 				prepend = "_";
 			URL[i] = "http://pd.npr.org/anon.npr-mp3/npr/" + showChoice + "/" + year + "/" + month + "/" + year + month + day + "_" + showChoice + prepend + (i + 1) + ".mp3";
 			Log.i("NPR", URL[i]);
 		}
 		(new DownloadWebPageTask()).execute(URL);
 
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
