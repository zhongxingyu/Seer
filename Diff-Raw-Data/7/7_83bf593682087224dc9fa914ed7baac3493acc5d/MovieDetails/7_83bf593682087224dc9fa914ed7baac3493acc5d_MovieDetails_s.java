 package com.app.parsjson.activity;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
import android.app.SearchManager;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 import com.app.parsjson.Downloader;
 import com.app.parsjson.activity.GetActivity;
 import com.app.parsjson.MovieInfo;
 import com.example.parsjson.R;
 
 public class MovieDetails extends Activity {
 	private long id;
 	private String query;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mov_det);
 		Intent intent = getIntent();
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			intent.setClass(MovieDetails.this, GetActivity.class);
 			startActivity(intent);
 		} else {
 			id = intent.getLongExtra(GetActivity.M_ID, 0);
 			query = "http://private-8a74b-themoviedb.apiary.io/3/movie/" + id
 					+ "?api_key=9abbb583ac624dedefae66bfb579e008";
 			setTitle(intent.getStringExtra(GetActivity.NAME));
 			((TextView) findViewById(R.id.movieName)).setText(intent
 					.getStringExtra(GetActivity.NAME));
 			((TextView) findViewById(R.id.popularityVal)).setText(intent
 					.getStringExtra(GetActivity.POPULARITY));
 			GetMovie movieLoader = new GetMovie();
 			movieLoader.execute();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.movie_details, menu);
 		return true;
 	}
 
 	private class GetMovie extends AsyncTask<Void, Void, MovieInfo> {
 
 		public final static String MESSAGE = "ID";
 
 		@Override
 		protected MovieInfo doInBackground(Void... arg0) {
 
 			try {
 				Downloader download = new Downloader();
 				JSONObject filmJSON = download.GetJson(query);
 
 				MovieInfo movie = new MovieInfo();
 				movie.setRating((float) filmJSON.getDouble("vote_average") / 2);
 				movie.setDate(filmJSON.getString("release_date"));
 				movie.setRuntime(Integer.parseInt(filmJSON.getString("runtime")));
 				movie.setOverview(filmJSON.getString("overview"));
 				String url = "https://d3gtl9l2a4fn1j.cloudfront.net/t/p/w185"
 						+ filmJSON.getString("poster_path");
 				movie.setBmp(download.getImage(url, id, getCacheDir()));
 				return movie;
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			return null;
 
 		}
 
 		protected void onPostExecute(MovieInfo results) {
 			((TextView) findViewById(R.id.releaseVal)).setText(results
 					.getDate());
 			((TextView) findViewById(R.id.runtimeVal)).setText(String
 					.valueOf(results.getRuntime()) + " min");
 			((TextView) findViewById(R.id.overview)).setText(results
 					.getOverview());
 			((RatingBar) findViewById(R.id.movieRate)).setRating(results
 					.getRating());
 			((ImageView) findViewById(R.id.Poster)).setImageBitmap(results
 					.getBmp());
 		}
 	}
 
 }
