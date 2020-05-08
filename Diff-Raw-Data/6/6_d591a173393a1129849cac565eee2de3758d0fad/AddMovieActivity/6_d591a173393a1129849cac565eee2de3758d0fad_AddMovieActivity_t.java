 package se.chalmers.watchme.activity;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import se.chalmers.watchme.R;
 import se.chalmers.watchme.database.DatabaseHandler;
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.model.Tag;
 import se.chalmers.watchme.ui.DatePickerFragment;
 import se.chalmers.watchme.ui.DatePickerFragment.DatePickerListener;
 import se.chalmers.watchme.utils.DateConverter;
 import se.chalmers.watchme.net.IMDBHandler;
 import se.chalmers.watchme.notifications.NotificationClient;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.Intent;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.LinearLayout;
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
 	private TextView titleField;
 	private Button addButton;
 	
 	// The handler to interface with the notification system and scheduler
 	private NotificationClient notifications = new NotificationClient(this);
 	
 	// The database handler
 	private DatabaseHandler db = new DatabaseHandler(this);
 	
 	// The IMDB API handler
 	private IMDBHandler imdb = new IMDBHandler();
 	
 	// The async IMDb search task
 	private IMDBSearchTask asyncTask;
 	
 	// The list adapter for the auto complete box
 	private ArrayAdapter<String> autoCompleteAdapter;
 	
 	private Calendar releaseDate;
 
     @SuppressLint("NewApi")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_movie);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         this.releaseDate = Calendar.getInstance();
         
         initUIControls();
         
         this.asyncTask = new IMDBSearchTask();
         this.autoCompleteAdapter = new ArrayAdapter<String>(this, R.layout.list_item);
         
         this.notifications.connectToService();
         
         // Disable add movie button on init
         this.addButton = (Button) findViewById(R.id.add_movie_button);
         this.addButton.setEnabled(false);
         
         ((AutoCompleteTextView) this.titleField).setAdapter(this.autoCompleteAdapter);
     }
     
     /**
      * Create references to UI elements in the XML
      */
     private void initUIControls() {
     	 //TODO Use the XML-value although it is overwritten here?
         this.dateField = (TextView) findViewById(R.id.release_date_label);
         this.dateField.setText(DateConverter.toSimpleDate(this.releaseDate));
         
         this.titleField = (TextView) findViewById(R.id.title_field);
         this.noteField = (TextView) findViewById(R.id.note_field);
        this.tagField = (TextView) findViewById(R.id.tag_field);
         
         // Add listeners to the title field
         this.titleField.addTextChangedListener(new AddButtonToggler());
         this.titleField.addTextChangedListener(new AutoCompleteWatcher());
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
     	String movieTitle = this.titleField.getText().toString();
     	String movieNote = this.noteField.getText().toString();
     	
     	// TODO Better suited list for tags?
 		List<Tag> newTags = new ArrayList<Tag>();
 		
 		/* 
 		 * Split the text input into separate strings input at
 		 * commas (",") from tag-field
 		 */
 		String [] tagStrings = tagField.getText().toString().split(",");
 		
 		for(String tagString : tagStrings) {
 			
 			/* Remove whitespaces from the beginning and end of each
 			 * string to allow for multi-word tags.
 			 */
 			newTags.add(new Tag(tagString.trim()));
 		}
 		
 		/*
 		 * Extract the rating from the ratingBar and convert it to
 		 * an integer
 		 */
 		RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_bar); 
 		int rating = (int) ratingBar.getRating();
 		
 		Movie movie = new Movie(movieTitle, releaseDate, rating, movieNote);
 		
 		db.addMovie(movie);
 		
 		Intent home = new Intent(this, MainActivity.class);
 		setResult(RESULT_OK, home);
 		home.putExtra("movie", movie);
 		
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
     	Toast.makeText(this, "Notification set for " + DateConverter.toSimpleDate(movie.getDate()), Toast.LENGTH_LONG).show();
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
 
 		dateField.setText(DateConverter.toSimpleDate(this.releaseDate));
 		
 	}
     
     /**
      * Click callback. Shows the date picker for a movies release date
      */
     public void onDatePickerButtonClick(View v) {
 		DialogFragment datePickerFragment = new DatePickerFragment();
         datePickerFragment.show(getSupportFragmentManager(),
         		"datePicker");
 	}
 
     /**
      * Class responsible for running an asynchronous task fetching
      * IMDb search results.  
      * 
      * @author Johan
      */
     private class IMDBSearchTask extends AsyncTask<String, Void, JSONArray> {
 
     	/**
     	 * Run a background task searching for movies with a title
     	 */
 		@Override
 		protected JSONArray doInBackground(String... params) {
 			return imdb.searchForMovieTitle(params[0]);
 		}
 		
 		@Override
 		protected void onPostExecute(final JSONArray results) {
 			if(results != null) {
 				// Re-initialize the adapter for the auto complete box
 				autoCompleteAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list_item);
 				((AutoCompleteTextView) titleField).setAdapter(autoCompleteAdapter);
 
 				// Parse the JSON objects and add to adapter
 				for(int i = 0; i < results.length(); i++) {
 					JSONObject o = results.optJSONObject(i);
 					
 					autoCompleteAdapter.add(o.optString("original_name"));
 					autoCompleteAdapter.notifyDataSetChanged();
 				}
 			}
 		}
     	
     }
     
     private class AddButtonToggler implements TextWatcher {
         	
         public void onTextChanged(CharSequence s, int start, int before, int count) {
            	if(s.toString().equals("")) {
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
      * Class responsible for running the IMDb search task
      * when the user types in the title field. 
      * 
      * @author Johan
      */
     private class AutoCompleteWatcher implements TextWatcher {
 
 		public void afterTextChanged(Editable arg0) {
 		}
 
 		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
 				int arg3) {
 		}
 
 		public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
 			
 			if(this.shouldAutoComplete(s.toString())) {
 				// Cancel any running tasks and execute a new one
 				asyncTask.cancel(true);
 				asyncTask = new IMDBSearchTask();
 				asyncTask.execute(s.toString());
 			}
 		}
 		
 		/**
 		 * Decides whether to run a new search task or not.
 		 * 
 		 * @param s The input query
 		 * @return True if auto complete should fire, otherwise false
 		 */
 		private boolean shouldAutoComplete(String s) {
 			return 	s.length() > 3 && 
 					asyncTask.getStatus() != AsyncTask.Status.RUNNING;
 		}
     	
     }
 }
