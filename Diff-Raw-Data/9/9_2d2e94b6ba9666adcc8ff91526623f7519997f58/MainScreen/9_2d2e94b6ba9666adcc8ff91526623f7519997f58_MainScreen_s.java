 package com.group15.djhero;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class MainScreen extends Activity implements OnItemClickListener {
 
 	private ListView m_listview;
 	MyApplication myApp;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main_scrren);
 		m_listview = (ListView) findViewById(R.id.main_list_view);
 		m_listview.setOnItemClickListener(this);
 		
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main_screen, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch(item.getItemId()){
 		
 		case R.id.action_update:
 			try {
 				onRefreshClick();
 			} catch (InterruptedException e) {}
 			return true;
 		
 		case R.id.action_sort:
 			ArrayList<String> arrayList = new ArrayList<String>(myApp.mainSongList.Songs.size());
 			
 			for(int i = 0; i<myApp.mainSongList.Songs.size(); i++){
 				arrayList.add(myApp.mainSongList.Songs.get(i).Title);
 			}
 			Collections.sort(arrayList); 
 			songList tempSongList = new songList();
 			for(int i = 0; i<myApp.mainSongList.Songs.size(); i++){
				
				if(arrayList.get(i).equals(myApp.mainSongList.Songs.get(i).Title))
				{
 					tempSongList.addSong((myApp.mainSongList.Songs.get(i)));
 				}
 			}
 			myApp.mainSongList = tempSongList;
 			onResume();
 		
 		case R.id.action_settings:
 			Intent intent = new Intent(this, AutoDetect.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			this.startActivity(intent);
 			return true;
 		
 		case R.id.action_dj:
 			Intent djIntent = new Intent(this, DJInterface.class);
 			djIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			this.startActivity(djIntent);
 			return true;
 			
 		case R.id.action_music:	
 			return true;
 	
 		case R.id.action_playlist:
 			Intent newPLaylist = new Intent(this, PlayLists.class);
 			newPLaylist.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			this.startActivity(newPLaylist);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void onResume(){
 		super.onResume();
 		myApp = (MyApplication) MainScreen.this.getApplication();
 		LazyAdapter adapter = new LazyAdapter(MainScreen.this,
 				myApp.mainSongList);
 		m_listview.setAdapter(adapter);
 	}
 	@Override
 	public void onItemClick(AdapterView<?> adapter, View arg1, int position,
 			long arg3) {
 
 		if (myApp.sock != null) {
 				Song thisSong = myApp.mainSongList.Songs.get(position);
 				myApp.songlist = myApp.mainSongList;
 				Intent intent = new Intent(this, PlaySongPage.class);
 				intent.putExtra("songName", thisSong.Title);
 				intent.putExtra("position", position);
 				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 				startActivity(intent);
 			}
 		 else {
 			// Take the user to the settings view if the socket is not open
 			Intent intent = new Intent(this, AutoDetect.class);
 			startActivity(intent);
 
 		}
 	}
 
 	public void onRefreshClick() throws InterruptedException {
 		if (myApp.sock == null) {
 			// Take the user to the settings view if the socket is not open
 			Intent intent = new Intent(this, AutoDetect.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			startActivity(intent);
 		} else {
 			// Clear the songlist and prepare to get the updated song list
 			myApp.mainSongList.clearList();
 
 			// Send a request for the song list string
 			//SendMessage.sendMessage("l", myApp.sock); UNCOMMENT LATER
 			SendMessage.sendMessage("l ", myApp.sock);
 
 			// Display a progress dialog while we get the list from the DE2
 			new RefreshProgressDialog().execute(); 
 		}
 	}
 
 	public void ipSettings(View view) {
 
 		Intent intent = new Intent(this, AutoDetect.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 		startActivity(intent);
 
 	}
 
 	class RefreshProgressDialog extends AsyncTask<Void, Void, Integer> {
 
 		ProgressDialog progress;
 		
 		@Override
 		protected void onPreExecute() {
 			progress = new ProgressDialog(MainScreen.this);
 			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			progress.setMessage("Updating song list");
 			progress.setMax(100);
 			progress.setProgressNumberFormat(null);
 			progress.show();
 		}
 
 		@Override
 		protected Integer doInBackground(Void... arg0) {
 
 			while (!myApp.listComplete);
 			return 0;
 
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			progress.dismiss();
 			myApp.listComplete = false;
 			myApp.images = new Bitmap[myApp.mainSongList.Songs.size()];
 
 			LazyAdapter adapter = new LazyAdapter(MainScreen.this,
 					myApp.mainSongList);
 			m_listview.setAdapter(adapter);
 			
 			for (int i = 0; i < myApp.mainSongList.Songs.size(); i++) {
 				myApp.images[i] = null;
 				new DownloadImages().execute(
 						"http://server.gursimran.net/test2.php?track="
 								+ myApp.mainSongList.Songs.get(i).Title.replace(" ", "+") + "&artist=" + myApp.mainSongList.Songs.get(i).artist.replace(" ", "+"),
 						String.valueOf(i));
 			}
 		}
 	}
 
 	// This is the asynchronous task. It is extended from AsyncTask
 	class DownloadImages extends
 			com.group15.djhero.AsyncTask<String, Void, Integer> {
 		// This is the "guts" of the asynchronus task. The code
 		// in doInBackground will be executed in a separate thread
 		@Override
 		protected Integer doInBackground(String... url_array) {
 			URL url;
 			Log.i("MainActivity", "Inside the asynchronous task");
 			try {
 				url = new URL(url_array[0]);
 				HttpURLConnection connection = (HttpURLConnection) url
 						.openConnection();
 				connection.setDoInput(true);
 				connection.connect();
 				Log.i("MainActivity", "Successfully opened the web page");
 				InputStream input = connection.getInputStream();
 				Bitmap bitmap = BitmapFactory.decodeStream(input);
 				input.close();
 				myApp.images[Integer.parseInt(url_array[1])] = bitmap;
 				return Integer.parseInt(url_array[1]);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 	}
 
 }
