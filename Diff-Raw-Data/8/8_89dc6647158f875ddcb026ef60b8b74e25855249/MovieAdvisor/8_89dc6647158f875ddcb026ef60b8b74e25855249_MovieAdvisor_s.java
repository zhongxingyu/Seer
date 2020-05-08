 package com.movieadvisor;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.Toast;
 
 public class MovieAdvisor extends Activity {
 
 	private Gallery gallery;
 	private ArrayList<Bitmap> imagesBoxOffice;
 	private GalleryAdapter boxOfficeAdapter;
 	private ArrayList<String> posters = new ArrayList<String>();
 	private ArrayList<String> ids = new ArrayList<String>();
 	ArrayList<Pair<Integer, String>> top3;
 	private Resources res;
 	private boolean loading = true;
 	public String boxoffice_limit = "20";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.movieadvisor);
 		imagesBoxOffice = new ArrayList<Bitmap>();
 		res=this.getResources();
 		imagesBoxOffice.add(BitmapFactory.decodeResource(res, R.drawable.loading));
 		boxOfficeAdapter = new GalleryAdapter(this, imagesBoxOffice);
 		gallery = (Gallery) findViewById(R.id.gallery1);
 		gallery.setAdapter(boxOfficeAdapter);
 		gallery.setPadding(0, 50, 0, 0);
 		gallery.setOnItemClickListener(new OnItemClickListener() {
 			@SuppressWarnings("rawtypes")
 			public void onItemClick(AdapterView parent, View v, int position, long id) {
 				Intent intent = new Intent(v.getContext(), MovieDetails.class);
 				Bundle b = new Bundle();
				b.putString("movieId", ids.get(position));
				intent.putExtras(b);
				startActivity(intent);
 			}
 		});
 		
 		if(!checkNetwork())
 			new RetrieveBoxOffice().execute();
 		
 		
 		
 		final Button button = (Button) findViewById(R.id.button1);
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (!checkNetwork()) {
 					Intent intent = new Intent(v.getContext(),
 							MovieSearchResults.class);
 					Bundle b = new Bundle();
 					EditText movieSearch = (EditText) findViewById(R.id.movieSearch);
 					String movieName = movieSearch.getText().toString();
 					b.putString("movieName", movieName);
 					intent.putExtras(b);
 					startActivityForResult(intent, 0);
 				}
 			}
 		});
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.configmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.about:
 			String ms1 = "This application was developed by some really cool guys at Faculdade de Engenharia da Universidade do Porto! ";
 			String ms2 = "If you like it, feel free to have a chat with us at movieadvisor@gmail.com!";
 			new AlertDialog.Builder(this)
 					.setTitle("About")
 					.setMessage(ms1 + "\n" + ms2)
 					.setPositiveButton("Close",
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 								}
 						}
 								).show();
 		break;	
 		case R.id.config:
 			Intent intent = new Intent(MovieAdvisor.this,
 					PrefsActivity.class);
 			this.startActivity(intent);
 			break;
 		}
 		
 		return true;
 
 	}
 
 	private boolean checkNetwork() {
 		if (!isNetworkAvailable()) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Please turn on an internet connection!")
 					.setCancelable(false)
 					.setPositiveButton("Settings",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									startActivityForResult(
 											new Intent(
 													android.provider.Settings.ACTION_WIRELESS_SETTINGS),
 											0);
 									// MovieAdvisor.this.finish();
 								}
 							})
 					.setNegativeButton("Exit",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									MovieAdvisor.this.finish();
 
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 			return true;
 
 		}
 		return false;
 	}
 
 	private boolean isNetworkAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    super.onActivityResult(requestCode, resultCode, data);
 	    if(resultCode == RESULT_FIRST_USER && requestCode == 0) {
 	    	Toast.makeText(this, "Your search returned no results!", Toast.LENGTH_LONG).show();
 	    }     
 	}
 	
 	class RetrieveBoxOffice extends AsyncTask<String, Integer, String> {
 
 		@Override
 		protected String doInBackground(String... arg0) {
 			String uri = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/box_office.json?limit="+boxoffice_limit+"&country=PT&apikey="+Utility.apiKey;
 			
 			URL url = null;
 			try {
 				url = new URL(uri);				
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			String line = null;
 			JSONObject jo = null;
 			JSONArray jsa = null;
 			try {
 				line = Utility.getJSONLine(url);
 				jo = new JSONObject(line);
 				jsa = jo.getJSONArray("movies");
 				for (int i = 0; i < jsa.length(); i++) {
 					JSONObject movie;
 					movie = jsa.getJSONObject(i);
 					JSONObject postersLink = movie.getJSONObject("posters");
 					ids.add(movie.getString("id"));
 					posters.add(postersLink.getString("profile"));
 				}
 			}
 			catch(Exception e){
 				Log.e("Error downloading box office", e.getMessage());
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(String result) { 
 			new RetrieveImages().execute();
 		}
 		
 		
 	}
 	
 	class RetrieveImages extends AsyncTask<String, Integer, String> {
 
 		@Override
 		protected String doInBackground(String... arg0) {
 			
 			
 			try{
 					imagesBoxOffice.add(loadImageFromNetwork(posters.get(0)));
 			}
 			catch(Exception e){
 				Log.e("Error downloading box office", e.getMessage());
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			if(loading){
 				imagesBoxOffice.remove(0);
 				loading = false;
 				if(!isNetworkAvailable())
 					imagesBoxOffice.add(BitmapFactory.decodeResource(res, R.drawable.network));
 			}
 			if(posters.size()>0) posters.remove(0);
 			boxOfficeAdapter.notifyDataSetChanged();
 			if(posters.size()>0){
 				new RetrieveImages().execute();
 			}
 		}
 		
 		public Bitmap loadImageFromNetwork(String url) throws MalformedURLException, IOException {
 			HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
 			conn.connect();
 			return BitmapFactory.decodeStream(new FlushedInputStream(conn.getInputStream()));
 			
 		}
 		
 	}
 	
 }
