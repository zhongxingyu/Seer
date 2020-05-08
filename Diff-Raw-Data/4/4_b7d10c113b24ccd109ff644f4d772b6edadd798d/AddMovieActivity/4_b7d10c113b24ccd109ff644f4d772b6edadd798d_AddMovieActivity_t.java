 package se.chalmers.watchme.activity;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import se.chalmers.watchme.database.DatabaseAdapter;
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.model.Movie.PosterSize;
 import se.chalmers.watchme.R;
 import android.net.Uri;
 import se.chalmers.watchme.model.Tag;
 import se.chalmers.watchme.ui.DatePickerFragment;
 import se.chalmers.watchme.ui.DatePickerFragment.DatePickerListener;
 import se.chalmers.watchme.utils.DateTimeUtils;
 import se.chalmers.watchme.utils.MovieHelper;
 import se.chalmers.watchme.net.IMDBHandler;
 import se.chalmers.watchme.notifications.NotificationClient;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class AddMovieActivity extends FragmentActivity implements DatePickerListener {
 	
 	private TextView dateField;
 	private TextView tagField;
 	private TextView noteField;
 	private AutoCompleteTextView titleField;
 	private ProgressBar progressSpinner;
 	private Button addButton;
 	
 	// The handler to interface with the notification system and scheduler
 	private NotificationClient notifications = new NotificationClient(this);
 	
 	// The list adapter for the auto complete box
 	private ArrayAdapter<JSONObject> autoCompleteAdapter;
 	
 	private Calendar releaseDate;
 	
 	private DatabaseAdapter db;
 	
     @SuppressLint("NewApi")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_movie);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         this.releaseDate = Calendar.getInstance();
         this.autoCompleteAdapter = new AutoCompleteAdapter(this, R.layout.auto_complete_item, new IMDBHandler());
         
         initUIControls();
         
         this.notifications.connectToService();
     }
     
     /**
      * Create references to UI elements in the XML
      */
     private void initUIControls() {
     	 //TODO Use the XML-value although it is overwritten here?
         this.dateField = (TextView) findViewById(R.id.release_date_label);
         this.dateField.setText(DateTimeUtils.toSimpleDate(this.releaseDate));
         
         this.titleField = (AutoCompleteTextView) findViewById(R.id.title_field);
         this.noteField = (TextView) findViewById(R.id.note_field_addmovie);
         this.tagField = (TextView) findViewById(R.id.tag_field_addmovie);
         
         // The progress bar when fetching IMDb movies
         this.progressSpinner = (ProgressBar) findViewById(R.id.title_progress);
         this.progressSpinner.setVisibility(View.INVISIBLE);
         
         // Add listeners to the title field
         this.titleField.addTextChangedListener(new AddButtonToggler());
         this.titleField.setOnItemClickListener(new AutoCompleteClickListener());
         // Set a minimum of 3 characters in order to kick-off auto complete
         this.titleField.setThreshold(3);
         
         this.titleField.setAdapter(this.autoCompleteAdapter);
         
         // Disable add movie button on init
         this.addButton = (Button) findViewById(R.id.add_movie_button);
         this.addButton.setEnabled(false);
     }
     
     /**
      * Click callback. Create a new Movie object and set it on
      * the Intent, and then finish this Activity.
      */
     public void onAddButtonClick(View view) {
     	
     	addMovie();
 		finish();
     }
     
     private void addMovie() {
     	db = new DatabaseAdapter(this.getContentResolver());
     	
     	// Get the movie from the auto-complete field 
     	Movie movie = (Movie) this.titleField.getTag();
     	String title = this.titleField.getText().toString();
     	
     	/*
     	 * If the auto-complete failed, or if the user choses
     	 * a movie from the dropdown and later erases the title
     	 * text to something custom, *without* choosing a new 
     	 * suggestion from the list.
     	 */
     	if(movie == null || !movie.getTitle().equals(title)) {
     		movie = new Movie(title);
     	}
     	
     	movie.setNote(this.noteField.getText().toString());
     	movie.setDate(releaseDate);
     	
 		/*
 		 * Extract the rating from the ratingBar and convert it to
 		 * an integer
 		 */
 		RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_bar); 
 		int rating = (int) ratingBar.getRating();
 		movie.setRating(rating);
 		
 		Log.i("Custom", movie.getPosterURLs().toString());
 		
 		// Insert into database
 		db.addMovie(movie);
 		
 		/* 
 		 * Split the text input into separate strings input at
 		 * commas (",") from tag-field
 		 */
 		String [] tagStrings = tagField.getText().toString().split(",");
 		
 		// TODO Shouldn't the tags be added to database in the addMovie-method?
 		db.attachTags(movie, MovieHelper.stringArrayToTagList(tagStrings));
 		
 		// Set a notification for the date picked
     	this.setNotification(movie);
     }
     
     /**
      * Queue a notification for the added movie
      * 
      * @param movie The movie
      */
     private void setNotification(Movie movie) {
     	this.notifications.setMovieNotification(movie);
    	
     	Toast.makeText(this, 
    			getString(R.string.notification_prefix_text) + 
     			DateTimeUtils.toSimpleDate(movie.getDate()), 
     			Toast.LENGTH_LONG)
     		.show();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_add_movie, menu);
         return true;
     }
     
     @Override
     protected void onStop() {
     	// Disconnect the service (if started) when this activity is stopped.
     	
     	if(this.notifications != null) {
     		this.notifications.disconnectService();
     	}
     	
     	super.onStop();
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     // @Override is not allowed in Java 1.5 for inherited interface methods
     public void setDate(Calendar pickedDate) {
 		this.releaseDate = pickedDate;
 
 		dateField.setText(DateTimeUtils.toSimpleDate(this.releaseDate));
 		
 	}
     
     /**
      * Click callback. Shows the date picker for a movies release date
      */
     public void onDatePickerButtonClick(View v) {
 		DialogFragment datePickerFragment = new DatePickerFragment();
         datePickerFragment.show(getSupportFragmentManager(),
         		"datePicker");
 	}
     
     private class AddButtonToggler implements TextWatcher {
         	
         public void onTextChanged(CharSequence s, int start, int before, int count) {
            	if(s.toString().isEmpty()) {
            		addButton.setEnabled(false);
            	} else {
            		addButton.setEnabled(true);
            	}
         }
 
 		public void afterTextChanged(Editable arg0) {
 			// Empty. Needs to be here
 		}
 
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 			// Empty. Needs to be here
 		}
 
     }
     
     /**
      * Class responsible for listening to click events in the auto complete
      * dropdown box. 
      * 
      * <p>Creates a new Movie from the JSON object chosen from the list and 
      * tags it to the title field.</p>
      * 
      * @author Johan
      */
     private class AutoCompleteClickListener implements OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			// Get the actual JSON object
 			JSONObject json = autoCompleteAdapter.getItem(position);
 			
 			// Create a new movie from the object
 			Movie movie = new Movie(json.optString(Movie.JSON_KEY_NAME));
 			
 			// Parse out the poster URLs
 			
 			String largeImage = MovieHelper.getPosterFromCollection(
 						json.optJSONArray("posters"), 
 						Movie.PosterSize.MID);
 			
 			String smallImage = MovieHelper.getPosterFromCollection(
 					json.optJSONArray("posters"), 
 					Movie.PosterSize.THUMB);
 			
 			Map<Movie.PosterSize, String> posters = new HashMap<Movie.PosterSize, String>();
 			posters.put(Movie.PosterSize.MID, largeImage);
 			posters.put(Movie.PosterSize.THUMB, smallImage);
 			
 			movie.setPosters(posters);
 			movie.setApiID(json.optInt(Movie.JSON_KEY_ID, Movie.NO_API_ID));
 			
 			// Tag the field with this movie object
 			titleField.setTag(movie);
 		}
     	
     }
     
 }
