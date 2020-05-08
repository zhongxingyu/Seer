 package com.brent.nprdroid;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class NPRDroidActivity extends ListActivity {
 	private String sdPath; 
 	private List<String> songs = new ArrayList<String>();
 	private MediaPlayer mp = new MediaPlayer();
 	private int currentPosition = 0;
 	private String showChoice;
 	private RadioGroup radioGroupShows;
 	private String TAG = "NPRDroidActivity";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		radioGroupShows = (RadioGroup) findViewById(R.id.radioGroupShows);
 		updateSongList();
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
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		currentPosition = position;
		playSong(sdPath + songs.get(position));
 	}
 
 	private void playSong(String songPath) {
 		Log.i(TAG, songPath);
 		try {
 			mp.reset();
 			mp.setDataSource(songPath);
 			mp.prepare();
 			mp.start();
 			// Setup listener so next song starts automatically
 			mp.setOnCompletionListener(new OnCompletionListener() {
 				public void onCompletion(MediaPlayer arg0) {
 					nextSong();
 				}
 			});
 		} catch (IOException e) {
 			Log.v(getString(R.string.app_name), e.getMessage());
 		}
 	}
 
 	private void nextSong() {
 		if (++currentPosition >= songs.size()) {
 			// Last song, just reset currentPosition
 			currentPosition = 0;
 		} else {
 			// Play next song
 			playSong(sdPath + songs.get(currentPosition));
 		}
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
 					//					LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
 					//				    TextView fileText = new TextView(NPRDroidActivity.this);
 					//				    fileText.setText(index + ": " + (new Date(audioFile.lastModified())).toString());
 					//				    fileText.setId(index);
 					//				    fileText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 					//				    ((LinearLayout) layout).addView(fileText);
 					++index;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 		}
 	}
 
 	public void readWebpage(View view) {
 		int selectedId = radioGroupShows.getCheckedRadioButtonId();
 		RadioButton selectedButton = (RadioButton) findViewById(selectedId);
 		if (selectedButton.getText().equals("Morning Edition")) showChoice = "me";
 		else showChoice = "atc";
 		Log.i("NPR", "button text: " + selectedButton.getText());
 		Calendar calNow = Calendar.getInstance();
 		int year = calNow.get(Calendar.YEAR);
 		int intMonth = calNow.get(Calendar.MONTH) + 1;
 		String month = intMonth > 9 ? "" + intMonth : "0" + intMonth;
 		int intDay = calNow.get(Calendar.DATE);
 		String day = intDay > 9 ? "" + intDay : "0" + intDay;
 		String prepend;
 		String URL[] = new String[30];
 		for (int i = 0; i < 30; ++i) {
 			if (i < 9) 
 				prepend = "_0";
 			else
 				prepend = "_";
 			URL[i] = "http://pd.npr.org/anon.npr-mp3/npr/" + showChoice + "/" + year + "/" + month + "/" + year + month + day + "_" + showChoice + prepend + (i + 1) + ".mp3";
 			Log.i("NPR", URL[i]);
 		}
 		DownloadWebPageTask task = new DownloadWebPageTask();
 		task.execute(URL);
 
 	}
 }
