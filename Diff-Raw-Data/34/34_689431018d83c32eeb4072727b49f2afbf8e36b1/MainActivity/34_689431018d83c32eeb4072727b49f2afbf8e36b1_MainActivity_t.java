 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Day;
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 import com.github.groupENIGMA.journalEgocentrique.model.Photo;
 
 public class MainActivity extends Activity {
 
     public final static String EXTRA_WRITE_NOTE_NoteId = "NoteId";
     public final static String EXTRA_WRITE_NOTE_DayId = "EntryId";
     public final static String EXTRA_PHOTO_ACTIVITY_DayId = "DayId";
     public final static String EXTRA_SETTINGS_TextSize = "text_size";
     public final static String EXTRA_SETTINGS_TextFont = "text_font";
 
     private final static String PREF_SELECTED_ENTRY = "selectedEntry_id";
 
     private DB dataBase;
     private DaysArrayAdapter daysListArrayAdapter;
     private Day selectedDay = null;
     private SharedPreferences sharedPreferences;
     private int textSize = R.array.textSize;
     private Typeface textFont;
     private String font;
     private int timeout;
 
     // Views for the Detail Section of the UI
     ListView entryListView;
     ImageView dailyPhotoHeader;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
         dataBase = new DB(getApplicationContext());
 
         // Open the shared preferences file
         sharedPreferences = getSharedPreferences(
                 AppConstants.SHARED_PREFERENCES_FILENAME,
                 MODE_PRIVATE
         );
         
         // Open database connection
         dataBase.open();
         // Display the list of days with an Entry
         displayMasterLayout();
         // Prepare the Detail Layout
         prepareDetailLayout();
 
         // Display the last viewed Day (if any) and the text size and font
         SharedPreferences pref = getPreferences(MODE_PRIVATE);
         Intent settings = getIntent();
        	textSize = settings.getIntExtra(EXTRA_SETTINGS_TextSize, -1);
        	if(textSize == -1)
        		textSize = pref.getInt(EXTRA_SETTINGS_TextSize, 12);
        	font = settings.getStringExtra(EXTRA_SETTINGS_TextFont);
        	if(font != null)
        		textFont = Typeface.create(settings.getStringExtra(EXTRA_SETTINGS_TextFont), Typeface.ITALIC);
        	else
        		textFont = Typeface.create(pref.getString(EXTRA_SETTINGS_TextFont, null), Typeface.ITALIC);
        	timeout = settings.getIntExtra(AppConstants.PREFERENCES_KEY_ENTRY_TIMEOUT, AppConstants.DEFAULT_NOTE_TIMEOUT);
        	Log.d("timeout", timeout+"");
         // Save the timeout extra
        	SharedPreferences.Editor edit = sharedPreferences.edit();
         edit.putInt(AppConstants.PREFERENCES_KEY_ENTRY_TIMEOUT, timeout);
         edit.commit();
         long id = pref.getLong(PREF_SELECTED_ENTRY, -1L);
         if(id != -1) {
             selectedDay = dataBase.getDay(id);
             displayDetailLayout();
         }
         else {
             selectedDay = null;
         }
 
         // If the Entry for today already exists disable the AddEntry button
         if (dataBase.existsDay()) {
             Button addEntry = (Button)findViewById(R.id.ListDaysAddEntryButton);
             addEntry.setEnabled(false);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // Database connection must be reopened if the app was previously
         // "paused" with onPause()
         if (!dataBase.isOpen()) {
             dataBase.open();
         }
     }
 
     /**
      * Display the "Master" section of the UI (the list of Days)
      */
     private void displayMasterLayout() {
         // Get the ListView that display the days
         ListView daysListView = (ListView)findViewById(R.id.daysList);
 
         // Get the list of available days from the database
         List<Calendar> daysList = dataBase.getDatesList();
 
         // Create and set the custom ArrayAdapter DaysArrayAdapter
         daysListArrayAdapter = new DaysArrayAdapter(
                 this, R.layout.row, daysList
         );
         daysListView.setAdapter(daysListArrayAdapter);
 
         // Set the listener
         OnItemClickListener clickListener = new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapter, View view,
                                     int position, long id) {
                 selectedDay = dataBase.getDay(
                         (Calendar) adapter.getItemAtPosition(position)
                 );
                 // Display the Detail of the selected day
                 displayDetailLayout();
             }
         };
         daysListView.setOnItemClickListener(clickListener);
     }
 
     /**
      * Prepare the "Detail" section of the UI (Daily photo + Entry)
      *
      * This is in a separate method from displayDetailLayout because
      * addHeaderView can be called only once per ListView
      */
     private void prepareDetailLayout() {
         // Get the ListView
         entryListView = (ListView) findViewById(R.id.EntryList);
         // Get the View with the daily Photo
         LayoutInflater inflater = LayoutInflater.from(this);
         dailyPhotoHeader = (ImageView) inflater
                 .inflate(R.layout.main_detail_photo_header, null, false);
         // Add the header with the DailyPhoto to the detailView
         entryListView.addHeaderView(dailyPhotoHeader);
     }
 
     /**
      * Display the "Detail" section of the UI (Daily photo + Entry)
      */
     private void displayDetailLayout() {
         // The Detail section will be displayed only when there's a Day selected
         if (selectedDay != null) {
             // Get the list of Entry for the selectedDay
             List<Entry> entries = selectedDay.getEntries();
 
             // Prepare the custom ArrayAdapter
             EntryAdapter entryAdapter = new EntryAdapter(
                     this, R.layout.main_row_entry, entries, textSize, textFont
             );
 
             // If available, display the Photo in the header
             Photo dailyPhoto = selectedDay.getPhoto();
             if (dailyPhoto != null) {
                 String photoPath = dailyPhoto.getPathThumb();
                 dailyPhotoHeader.setImageURI(Uri.parse(photoPath));
             }
             // Display the default image
             else {
                 dailyPhotoHeader.setImageResource(R.drawable.ic_launcher);
             }
 
             // If the selected Day can be updated add the listener that starts
             // the PhotoActivity (to take a new Photo)
             if (selectedDay.canBeUpdated()) {
                 dailyPhotoHeader.setOnTouchListener(new OnTouchListener()
                 {
                     @Override
                     public boolean onTouch(View v, MotionEvent event)
                     {
                         // Start the PhotoActivity
                         Intent intent = new Intent(
                                 getApplicationContext(),
                                 PhotoActivity.class
                         );
                         intent.putExtra(
                                 EXTRA_PHOTO_ACTIVITY_DayId,
                                 selectedDay.getId()
                         );
                         startActivity(intent);
                         return false;
                     }
                 });
             }
             // The Photo can't be updated
             else {
                 dailyPhotoHeader.setOnTouchListener(new OnTouchListener(){
                 	
     				public boolean onTouch(View v, MotionEvent event) {
     					Toast.makeText(getApplicationContext(),
     							"The photo can't be updated", Toast.LENGTH_SHORT)
     							.show();
 					return true;
 				}
 			});
             }
 
             // Set the custom ArrayAdapter to the detailView
             entryListView.setAdapter(entryAdapter);
 
             // Add the onLongClickListener that activates the WriteNote activity
             // that can be used to update the Entry text
             OnItemClickListener clickListener = new OnItemClickListener() {
 
                 @Override
                 public void onItemClick(AdapterView<?> adapter, View view,
                                                int position, long id) {
                     // Enable the onClickListener only if the Entry can be
                     // updated.
                     final Entry selectedEntry = (Entry) adapter
                             .getItemAtPosition(position);
                     Log.d("entry", selectedEntry.getNote());
                     if (selectedEntry.canBeUpdated(sharedPreferences)) {
                         AlertDialog.Builder build = new AlertDialog.Builder(
                                 MainActivity.this
                         );
                         build.setMessage("Select the action");
                         build.setNegativeButton("Delete note", new DialogInterface.OnClickListener(){
 
                             @Override
                             public void onClick(DialogInterface dialog, int id) {
                                 dataBase.deleteEntry(selectedEntry);
                                 Toast toast = Toast.makeText(
                                         getApplicationContext(),
                                         "Deleted",
                                         Toast.LENGTH_LONG);
                                 toast.show();
                                 Intent intent = new Intent(
                                         getApplicationContext(),
                                         MainActivity.class
                                 );
                                 startActivity(intent);
                             }
                         });
 
                         build.setPositiveButton("Update note", new DialogInterface.OnClickListener(){
 
                             @Override
                             public void onClick(DialogInterface dialog, int id){
                                 Intent intent = new Intent(
                                         getApplicationContext(),
                                         WriteNote.class
                                 );
                                 intent.putExtra(
                                         EXTRA_WRITE_NOTE_NoteId,
                                         selectedEntry.getId()
                                 );
                                 startActivity(intent);
                             }
                         });
                         AlertDialog alert = build.create();
                         alert.show();
                     }
                     // The Entry can't be updated
                     else {
                         Toast.makeText(getApplicationContext(), "The entry can't be updated", Toast.LENGTH_LONG).show();
                     }
                 }
             };
             entryListView.setOnItemClickListener(clickListener);
         }
     }
 
     @Override
     protected void onPause(){
         super.onPause();
         // Get the preference file
         SharedPreferences pref = getPreferences(MODE_PRIVATE);
         SharedPreferences.Editor edit = pref.edit();
         // Save selected Day (if any)
         if (selectedDay == null) {
             edit.putLong(PREF_SELECTED_ENTRY, -1L);
         }
         else {
             edit.putLong(PREF_SELECTED_ENTRY, selectedDay.getId());
         }
         // Save the text size
         edit.putInt(EXTRA_SETTINGS_TextSize, textSize);
         // Save the text font
         edit.putString(EXTRA_SETTINGS_TextFont, font);
         edit.commit();
         // Close database connection
         dataBase.close();
     }
 
     /**
      * Adds an Entry for today to the database and to the displayed list.
      * Used by ListDaysAddEntryButton in main.xml
      *
      * @param view as required by android:onClick xml attribute. Not used.
      */
     public void addTodayEntry(View view) {
         // New Entry in the database
         selectedDay = dataBase.createDay();
         // Entry to the beginning of the displayed list
         daysListArrayAdapter.insert(selectedDay.getDate(), 0);
         // Disable the ListDaysAddEntryButton
         Button addEntry = (Button)findViewById(R.id.ListDaysAddEntryButton);
         addEntry.setEnabled(false);
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.option, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
             case R.id.newNote:
                 Intent intent = new Intent(
                         getApplicationContext(), WriteNote.class
                 );
                 intent.putExtra(EXTRA_WRITE_NOTE_NoteId, -1L);
                 intent.putExtra(EXTRA_WRITE_NOTE_DayId, selectedDay.getId());
                 startActivity(intent);
                 return true;
 	        case R.id.settings:
 	        	Intent settings = new Intent(getApplicationContext(), Settings.class);
 	        	startActivity(settings);
 	        	return true;
 	        case R.id.deleteEntry:
 	        	if(selectedDay != null){
                    AlertDialog.Builder build = new AlertDialog.Builder(
                            MainActivity.this
                    );
                    build.setMessage("Are you sure?");
                    build.setNegativeButton("Delete Day", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
        	        		dataBase.deleteDay(selectedDay);
        	        		selectedDay = null;
                            Toast toast = Toast.makeText(
                                    getApplicationContext(),
                                    "Deleted",
                                    Toast.LENGTH_LONG);
                            toast.show();
                            Intent intent = new Intent(
                                    getApplicationContext(),
                                    MainActivity.class
                            );
                            startActivity(intent);
                        }
                    });

                    build.setPositiveButton("Don't delete", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int id){
                        	// Nothing
                        }
                    });
                    AlertDialog alert = build.create();
                    alert.show();;
 	        		return true;
 	        	}
 	        case R.id.gallery:
 	        	Intent gallery = new Intent(getApplicationContext(), GalleryActivity.class);
 	        	startActivity(gallery);
 	        	return true;
 	        case R.id.share:
 	        	Intent share = new Intent(getApplicationContext(), ShareActivity.class);
 	        	share.putExtra("EntryId", selectedDay.getId());
 	        	startActivity(share);
                 return true;
 	    }
 		return false;
 	}
 }
