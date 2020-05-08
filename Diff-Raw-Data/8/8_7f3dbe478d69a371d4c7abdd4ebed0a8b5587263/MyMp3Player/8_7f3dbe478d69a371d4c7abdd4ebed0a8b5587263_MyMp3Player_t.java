 package com.lnu;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.SimpleAdapter;
 
 public class MyMp3Player extends ListActivity implements OnClickListener,
 		OnItemClickListener, SeekBar.OnSeekBarChangeListener,
 		OnCompletionListener {
 
 	private SeekBar songProgressBar;
 	private Button prevButton;
 	private Button playButton;
 	private Button stopButton;
 	private Button nextButton;
 	private static int lastSelectedIndex = -1;
 	private final List<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
 	private final SongsManager songsManager = new SongsManager();
 	private MediaPlayer mp = new MediaPlayer();
 	private final Handler mHandler = new Handler();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_mp3player);
 		try {
 			loadSongs();
 			String[] from = { "name" };
 			int[] to = { android.R.id.text1 };
 			ListAdapter adapter = new SimpleAdapter(this, songsList,
 					android.R.layout.simple_list_item_1, from, to);
 			setListAdapter(adapter);
 
 			ListView lv = getListView();
 			lv.setOnItemClickListener(this);
 			playButton = (Button) findViewById(R.id.playButton);
 			playButton.setOnClickListener(this);
 			nextButton = (Button) findViewById(R.id.nextButton);
 			nextButton.setOnClickListener(this);
 			prevButton = (Button) findViewById(R.id.prevButton);
 			prevButton.setOnClickListener(this);
 			stopButton = (Button) findViewById(R.id.stopButton);
 			stopButton.setOnClickListener(this);
 			songProgressBar = (SeekBar) findViewById(R.id.seekBar);
 			songProgressBar.setOnSeekBarChangeListener(this);
 			mp.setOnCompletionListener(this);
 
 		} catch (Exception e) {
 			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 			alertDialog.setMessage(e.getMessage());
 			alertDialog.show();
 		}
 
 	}
 
 	private void loadSongs() {
 		for (File song : songsManager.getSongsList()) {
 			HashMap<String, String> item = new HashMap<String, String>();
 			item.put("name", song.getName());
 			item.put("songPath", song.getPath());
 			songsList.add(item);
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_my_mp3player, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		playSong(position);
 	}
 
 	private void playSong(int position) {
 		try {
 			if (songsList.isEmpty()) {
 				throw new RuntimeException("No Mp3 Songs found.");
 			}
 			if (position < 0) {
 				position = 0;
 			} else if (position >= songsList.size()) {
 				position = songsList.size() - 1;
 			}
 			lastSelectedIndex = position;
 			if (mp == null) {
 				mp = new MediaPlayer();
 				mp.setOnCompletionListener(this);
 			}
 			mp.reset();
 			mp.setDataSource(songsList.get(position).get("songPath"));
 			mp.prepare();
 			mp.start();
 			playButton.setText("Pause");
 			songProgressBar.setProgress(0);
 			songProgressBar.setMax(100);
 			updateProgressBar();
 		} catch (Exception e) {
 			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 			alertDialog.setMessage(e.getMessage());
 			alertDialog.show();
 		}
 	}
 
 	private void updateProgressBar() {
 		mHandler.postDelayed(mUpdateTimeTask, 100);
 	}
 
 	private Runnable mUpdateTimeTask = new Runnable() {
 		public void run() {
 			long totalDuration = mp.getDuration();
 			long currentDuration = mp.getCurrentPosition();
 			long currentSeconds = (int) (currentDuration / 1000);
 			long totalSeconds = (int) (totalDuration / 1000);
 			int progress = Double.valueOf(
 					(((double) currentSeconds) / totalSeconds) * 100)
 					.intValue();
 			songProgressBar.setProgress(progress);
 			mHandler.postDelayed(this, 100);
 		}
 	};
 
 	@Override
 	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar arg0) {
 		mHandler.removeCallbacks(mUpdateTimeTask);
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		mHandler.removeCallbacks(mUpdateTimeTask);
 		int totalDuration = mp.getDuration();
 		int currentDuration = 0;
 		totalDuration = (int) (totalDuration / 1000);
 		currentDuration = (int) ((((double) seekBar.getProgress()) / 100) * totalDuration) * 1000;
 		int currentPosition = currentDuration;
 		mp.seekTo(currentPosition);
 		updateProgressBar();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.playButton: {
 			if (lastSelectedIndex == -1) {
 				playSong(0);
 			} else if (mp == null) {
 				playSong(lastSelectedIndex);
 			} else if (mp.isPlaying()) {
 				mp.pause();
 				playButton.setText("Play");
 			} else {
 				mp.start();
 			}
 			break;
 		}
 		case R.id.nextButton: {
 			playSong(lastSelectedIndex + 1);
 			break;
 		}
 		case R.id.prevButton: {
 			playSong(lastSelectedIndex - 1);
 			break;
 		}
 		case R.id.stopButton: {
 			mp.stop();
 			mp = null;
 			mHandler.removeCallbacks(mUpdateTimeTask);
 			songProgressBar.setProgress(0);
 			playButton.setText("Play");
 			break;
 		}
 
 		}
 	}
 
	
 	@Override
 	public void onCompletion(MediaPlayer mp) {
		lastSelectedIndex +=1; 
 		if (lastSelectedIndex >= songsList.size()) {
 			lastSelectedIndex = 0;
 		}
		playSong(lastSelectedIndex);
 	}
 }
