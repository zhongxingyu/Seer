 package com.movieadvisor;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.database.DataHelper;
 
 public class MovieDetails extends Activity {
 
 	private ImageView thumb;
 	private Movie m;
 	TextView movie;
 	private String movieId;
 	private String movieLink;
 	private ProgressDialog dialog;
 	private DataHelper dh;
 	int nstars = 5;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.moviedetails);
 		dh = new DataHelper(this);
 		movie = (TextView) findViewById(R.id.movieName);
 
 		Bundle b = this.getIntent().getExtras();
 		movieLink = b.getString("movieLink");
 		movieId = b.getString("movieId");
 		if (movieId == null)
 			movieId = "-1";
 		dialog = ProgressDialog.show(this, "", "Loading...", true);
 		dialog.setCancelable(true);
 		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
 			   public void onCancel(DialogInterface dialog) {
 			    finish();
 			   }
 			  });
 
 		new DownloadMovieDetails().execute(movieLink);
 
 	}
 
 	private class DownloadMovieDetails extends AsyncTask<String, Void, Movie> {
 		@Override
 		protected Movie doInBackground(String... arg0) {
 			GetMovieDetails movieDetails = new GetMovieDetails(arg0[0],
 					Integer.parseInt(movieId));
 			try {
 				return movieDetails.call();
 			} catch (Exception e) {
 				return null;
 			}
 		}
 
 		protected void onPostExecute(Movie result) {
 			if(result == null)
 			{
 				Toast.makeText(MovieDetails.this, "Details unavaliable", Toast.LENGTH_SHORT).show();
 				MovieDetails.this.finish();
 				return;
 			}
 			m = result;
 			movieId = String.valueOf(m.idRT);
 			movie.setText(m.name);
 			
 			thumb = (ImageView) findViewById(R.id.movieThumb);
 			thumb.setEnabled(false);
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(getBaseContext());
 			
 
 			Boolean moviePoster = prefs.getBoolean("showposter", false);
 
 			if (!moviePoster)
 				new DownloadImageTask().execute(m.imageUrl);
 
 			RatingBar rb = (RatingBar) findViewById(R.id.rating);
 			rb.setEnabled(false);
 			rb.setNumStars(nstars);
 			rb.setStepSize((float) 0.2);
 
 			rb.setRating((float) (nstars * m.rating) / 100);
 
 			ListView genresList = (ListView) findViewById(R.id.genres);
 			genresList.setDividerHeight(2);
 			genresList.setEnabled(false);
 			genresList.setAdapter(new PersonView(m.genres));
 			Utility.setListViewHeightBasedOnChildren(genresList, m.genres);
 
 			ListView actorsList = (ListView) findViewById(R.id.actors);
 			actorsList.setDividerHeight(2);
 			actorsList.setEnabled(false);
 			actorsList.setAdapter(new PersonView(m.actors));
 			Utility.setListViewHeightBasedOnChildren(actorsList, m.actors);
 
 			ListView directorsList = (ListView) findViewById(R.id.directors);
 			directorsList.setDividerHeight(2);
 			directorsList.setEnabled(false);
 			directorsList.setAdapter(new PersonView(m.directors));
 			Utility.setListViewHeightBasedOnChildren(directorsList, m.directors);
 			TextView synopsis = (TextView) findViewById(R.id.synopsis);
 
 			if (!m.synopsis.equals("")) {
 				synopsis.setGravity(Gravity.FILL_HORIZONTAL
 						| Gravity.FILL_VERTICAL);
 				synopsis.setText(m.synopsis);
 			} else {
 				synopsis.setGravity(Gravity.CENTER_HORIZONTAL
 						| Gravity.CENTER_VERTICAL);
 				synopsis.setText("Not available");
 			}
 
 			TextView pontuation = (TextView) findViewById(R.id.pontuation);
 			pontuation.setText((int) m.rating + "/100");
 			pontuation.setGravity(Gravity.CENTER_VERTICAL
 					| Gravity.CENTER_HORIZONTAL);
 
 			TextView movieYear = (TextView) findViewById(R.id.movieYear);
 			movieYear.setText("" + m.year);
 			
 			TextView TrailerLink = (TextView) findViewById(R.id.TrailerLink);
 			if (m.getTrailerLink() != null && !m.getTrailerLink().equals("null")) 	
 				TrailerLink.setText(m.getTrailerLink());
 			else
 				TrailerLink.setText("Not avaliable...");
 				
 			dialog.dismiss();
 		}
 
 	}
 
 	public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 		protected Bitmap doInBackground(String... urls) {
 			try {
 				return loadImageFromNetwork(urls[0]);
 			} catch (IOException e) {
 				return null;
 			} catch (OutOfMemoryError e) {
 				try {
 					return loadImageFromNetwork("http://images.rottentomatoescdn.com/images/redesign/poster_default.gif");
 				} catch (Exception e2) {
 					return null;
 				}
 			} catch (Exception e) {
 				try {
 					return loadImageFromNetwork("http://images.rottentomatoescdn.com/images/redesign/poster_default.gif");
 				} catch (Exception e2) {
 					return null;
 				}
 			}
 		}
 
 		protected void onPostExecute(Bitmap result) {
 			if (result != null)
 				thumb.setImageBitmap(result);
 		}
 	}
 
 	private class GetSimilar
 			extends
 			AsyncTask<String, Void, Pair<ArrayList<Integer>, ArrayList<String>>> {
 		protected Pair<ArrayList<Integer>, ArrayList<String>> doInBackground(
 				String... id) {
 			try {
 				return Utility.getSimilar(Integer.parseInt(id[0]));
 			} catch (Exception e) {
 				return null;
 			}
 			
 		}
 
 		protected void onPostExecute(
 				Pair<ArrayList<Integer>, ArrayList<String>> result) {
 			if (result != null) {
 				Intent intent = new Intent(MovieDetails.this, DisplayList.class);
 				dialog.dismiss();
 				Bundle b = new Bundle();
 				b.putIntegerArrayList("ids", result.getFirst());
 				b.putStringArrayList("names", result.getSecond());
 				intent.putExtras(b);
 				startActivity(intent);
 			}
 			else {
 				dialog.dismiss();
 				Toast.makeText(MovieDetails.this, "A problem has occurred, please try again!", Toast.LENGTH_SHORT).show();
 			}
 		}
 		
 	}
 
 	private Bitmap loadImageFromNetwork(String url)
 			throws MalformedURLException, IOException {
 		HttpURLConnection conn = (HttpURLConnection) (new URL(url))
 				.openConnection();
 		conn.connect();
 		return BitmapFactory.decodeStream(new FlushedInputStream(conn
 				.getInputStream()));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(MovieDetails.this);
 		/*String account = prefs.getString("accountchooser", null);
 		if (account == null) {
 			Utility.checkAccount(MovieDetails.this);
 			return true;
 		}*/
 		
 		Boolean sharePopUp = prefs.getBoolean("sharepopup", false);
 
 		switch (item.getItemId()) {
 		case R.id.suggest:
 			Intent i = new Intent(Intent.ACTION_SEND);
 			i.setType("text/plain");
 			i.putExtra(Intent.EXTRA_SUBJECT, "MovieAdvisor Suggestion");
 			i.putExtra(Intent.EXTRA_TEXT,
 					"Hi! I really liked this movie and you should see it!\n"
 							+ m.name + "(" + m.year + ")" + "\nRating: "
 							+ m.rating + "\n\n" + m.imdbLink
 							+ "\n\nSuggested by Movie Advisor.");
 			try {
 				startActivity(Intent.createChooser(i, "Sending..."));
 			} catch (android.content.ActivityNotFoundException ex) {
 				Toast.makeText(MovieDetails.this,
 						"There are no email clients installed.",
 						Toast.LENGTH_SHORT).show();
 			}
 
 			break;
 
 		case R.id.like:
 			share("I just liked: ",false);
 			break;
 		case R.id.want:
 			if(!sharePopUp)
 				share("I can't wait to see: ",true);
 			if (!addToDatabase(0))
 				Toast.makeText(MovieDetails.this, "This movie is already on that list!", Toast.LENGTH_SHORT).show();
 			else
 				Toast.makeText(MovieDetails.this, "Movie added with success!", Toast.LENGTH_SHORT).show();
 			break;
 		case R.id.seen:
 			if(!sharePopUp)
 				share("I have just seen: ",true);
 			if (!addToDatabase(1))
 				Toast.makeText(MovieDetails.this, "This movie is already on that list!", Toast.LENGTH_SHORT).show();
 			else
 				Toast.makeText(MovieDetails.this, "Movie added with success!", Toast.LENGTH_SHORT).show();
 			break;
 		case R.id.similar:
 			new GetSimilar().execute(m.idRT + "");
 			break;
 		}
 		return true;
 	}
 
 	public boolean addToDatabase(int idList) {
 		String movieLinkdb = "http://api.rottentomatoes.com/api/public/v1.0/movies/"+movieId+".json";
 		try {
 			switch (idList) {
 			case 0:
 				dh.insert("table_tosee", movieId, m.name, movieLinkdb);
 				if(Utility.lists.size()>0)
 					Utility.lists.get(0).refreshMyList("refresh");
 				if(Utility.lists.size()>1)
 					Utility.lists.get(1).refreshMyList("refresh");
 				break;
 			case 1:
 				HashMap<String,ArrayList<String>> info = dh.selectAll("table_tosee");
 				if(info.containsKey(movieId)){
 					dh.deleteID("table_tosee", movieId);
 				}
 				dh.insert("table_seen", movieId, m.name, movieLinkdb);
 				if(Utility.lists.size()>0)
 					Utility.lists.get(0).refreshMyList("refresh");
 				if(Utility.lists.size()>1)
 					Utility.lists.get(1).refreshMyList("refresh");
 				break;
 			default:
 				break;
 			}
 
 		} catch (Exception e) {
 			Log.e("ERROR Inserting on db", e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	public void share(final String message, boolean flag) {
 		final CharSequence[] itemsTrue = { "Facebook", "Twitter",
 				"No" };
 		final CharSequence[] itemsFalse = { "Facebook", "Twitter" };
 		final CharSequence[] items;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Share?");
 		if(flag)
 			items = itemsTrue;
 		else
 			items = itemsFalse;
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int item) {
 				switch (item) {
 
 				case 0: {
 					Intent intent;
 					Bundle b;
 					String directors;
 					intent = new Intent(MovieDetails.this,
 							FacebookActivity.class);
 					b = new Bundle();
 					b.putString("message", message);
 					b.putString("name", m.name);
 					b.putString("link", m.getTrailerLink());
 
 					directors = "Directed by: ";
 					for (String a : m.directors)
 						directors = directors.concat(a) + ", ";
 					if (!directors.equals("Directed by:")) {
 						directors = directors.substring(0,
 								directors.length() - 2);
 						b.putString("caption", directors);
 					} else
 						b.putString("caption", "" + m.year);
 
 					b.putString("imageLink", m.imageUrl);
 
 					if (!m.synopsis.equals(""))
 						b.putString("description", m.synopsis);
 					else {
 						String actors = "Presenting: ";
 						for (String a : m.actors)
 							actors = actors.concat(a) + ", ";
 						if (!actors.equals("Presenting: ")) {
 							actors = actors.substring(0, actors.length() - 2);
 							b.putString("description", actors);
 						}
 					}
 
 					intent.putExtras(b);
 					startActivity(intent);
 					break;
 				}
 
 				case 1: {
 					Bundle b = new Bundle();
 					b.putString("text", message + " " + m.name);
 					Intent intent = new Intent(MovieDetails.this,
 							TestPost.class);
 					intent.putExtras(b);
 					startActivity(intent);
 					break;
 				}
 
 				default:
 					break;
 				}
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	@Override
 	protected void onDestroy() {
 	    super.onDestroy();
 	    if (dh != null) {
 	        dh.close();
 	    }
 	}
 
 }
