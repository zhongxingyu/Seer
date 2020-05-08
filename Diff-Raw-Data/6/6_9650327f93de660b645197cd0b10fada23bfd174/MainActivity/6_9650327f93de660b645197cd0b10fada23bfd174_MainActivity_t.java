 package com.alvarosantisteban.berlincurator;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.alvarosantisteban.berlincurator.IHeartBerlinHtmlParser.Entry;
 
 /**
  * Creates the main screen, from where the user can access the Settings, load the events of the current day or 
  * go to Calendar to select another day
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  *
  */
 public class MainActivity extends Activity {
 
 	public static final int MAX_NUMBER_OF_WEBSITES = 5;
 	private static final String DEBUG_TAG = "HttpExample";
 	public static final String EXTRA_HTML = "com.alvarosantisteban.berlincurator.html";
 	//public static List<List<Event>> events = (ArrayList)new ArrayList <ArrayList<Event>>();
 	public static Map<String, List<Event>> events = (Map<String, List<Event>>)(Map<String,?>) new HashMap <String, ArrayList<Event>>();
 	
 	// Settings
 	private static final int RESULT_SETTINGS = 1;
 	SharedPreferences sharedPref;
 	Context context;
 	public static String[] webs = {"I Heart Berlin", "Berlin Art Parasites", "Metal Concerts", "White Trashs concerts", "Koepis activities"};
 	
 	/**
 	 * The set of urls from where the html will be downloaded
 	 */
    	String[] stringUrls = {"http://www.iheartberlin.de/events/",
 				   			"http://www.berlin-artparasites.com/recommended",
 				   			"http://berlinmetal.lima-city.de/index.php/index.php?id=start",
 				   			"http://www.whitetrashfastfood.com/events/",
 				   			"http://www.koepi137.net/eventskonzerte.php",};
    	
    	/**
    	 * The set of htmls from the corresponding {@link stringUrls}
    	 */
    	String[] htmls = new String[MAX_NUMBER_OF_WEBSITES];
 
 	ProgressBar loadProgressBar;
 	RelativeLayout mainLayout;
     ImageButton loadButton;
     TextView calendarText;
     TextView settingsText;
     ImageView loadEventsImage;
     
     private int progressBarStatus = 0;
 
 	List<Entry> entries;
 	
 	/**
 	 * Loads the elements from the resources
 	 */
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		System.out.println("--------------- BEGIN ------------");
 		context = this;
 		
 		mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
 		loadButton = (ImageButton) findViewById(R.id.loadButton);
 		loadProgressBar = (ProgressBar)findViewById(R.id.progressLoadHtml);
 		calendarText = (TextView)findViewById(R.id.textCalendar);
 		loadEventsImage = (ImageView)findViewById(R.id.loadData);
 		
 		// Get the default shared preferences
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		// This is suppose to be used to clear the data from shared preferences
 		/*
 		Editor editor = sharedPref.edit();
 		editor.clear();
 		editor.commit();
 		 */
 		
 		// Check which sites are meant to be shown
		Set<String> set = sharedPref.getStringSet("multilist", new HashSet<String>(Arrays.asList(webs)));
 		webs = set.toArray(new String[0]);
 
 		/*
 		 * To check that they are there*/
 		System.out.println();
 		for(int i=0;i<webs.length;i++){
 			System.out.print(webs[i] + " / ");
 		}
 		System.out.println();
 		
 		
 		loadEventsImage.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Toast.makeText(getBaseContext(), "HOLA CARACOLA.", Toast.LENGTH_SHORT).show();
 			}
 			
 		});
 		
 		
 		/*
 		calendarText.setOnClickListener(new OnClickListener() {
 			
 			// Goes to the Calendar activity
 			public void onClick(View v) {
 				Intent intent = new Intent(context, CalendarActivity.class);
 				startActivity(intent);
 			}
 		});
 		*/
 		
 		loadButton.setOnClickListener(new OnClickListener() {
 			
 			/**
 			 * Downloads the html from the websites and goes to the DataActivity
 			 */
 			public void onClick(View v) {
 				
 				// prepare for a progress bar dialog
 				loadProgressBar.setProgress(0);
 				loadProgressBar.setMax(MAX_NUMBER_OF_WEBSITES);				
 				
 				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 			    // Check if is possible to establish a connection
 			    if (networkInfo != null && networkInfo.isConnected()) {
 					DownloadWebpageTask download = new DownloadWebpageTask();
 					// Execute the asyncronous task of downloading the websites
 					// TODO Try to make it fail by giving ONE false url
 					download.execute(stringUrls);
 			    } else {
 			    	// Inform the user that there is no network connection available
 			    	Toast.makeText(getBaseContext(), "No network connection available.", Toast.LENGTH_LONG).show();
 			        System.out.println("No network connection available.");
 			    }
 			}
 		});	
 	}
 	
 	/**
 	 * Inflates the menu from the XML
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.main, menu);
 		getMenuInflater().inflate(R.menu.preferences, menu);
 		return true;
 	}
 	
 	/**
 	 * Checks which item from the menu has been clicked
 	 */
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
  
         // Goes to the settings activity
         case R.id.menu_settings:
             Intent i = new Intent(this, SettingsActivity.class);
             startActivityForResult(i, RESULT_SETTINGS);
             break;
  
         }
  
         return true;
     }
     
     /** 
      * Uses AsyncTask to create a task away from the main UI thread. 
      * This task takes the creates several HttpUrlConnection to download the html from different websites. 
      * Afterwards, the several lists of Events are created and the execution goes to the Date Activity.
     */
 	private class DownloadWebpageTask extends AsyncTask<String, Integer, Map<String,List<Event>>> {
 		
 		/**
 		 * Makes the progressBar visible
 		 */
 		protected void onPreExecute(){
 	    	System.out.println("onPreExecute");
 	    	loadProgressBar.setVisibility(View.VISIBLE);
 		}
 		
 		/**
 		 * Downloads the htmls and creates the lists of Events. 
 		 * Updates the status of the progressBar.
 		 * 
 		 */
 		protected Map<String,List<Event>> doInBackground(String... urls) { 	
 			// Remove all the entries from the map
 			events.clear();
 			// TODO Instead of clearing all the events, maintain the ones that did not change
 			// Load the events from the selected websites
 			for (int i=0; i<webs.length; i++){
 				List<Event> event = null;
 				if (webs[i].equals("I Heart Berlin")){
 					System.out.println("Ihearberlin dentro");
 					event = EventLoaderFactory.newIHeartBerlinEventLoader().load();
 				}else if(webs[i].equals("Berlin Art Parasites")){
 					event = EventLoaderFactory.newArtParasitesEventLoader().load();
 				}else if(webs[i].equals("Metal Concerts")){
 					event = EventLoaderFactory.newMetalConcertsEventLoader().load();
 				}else if(webs[i].equals("White Trashs concerts")){
 					event = EventLoaderFactory.newWhiteTrashEventLoader().load();
 				}else if(webs[i].equals("Koepis activities")){
 					System.out.println("koepi dentro");
 					event = EventLoaderFactory.newKoepiEventLoader().load();
 				}else{
 					return null;
 				}
 				events.put(webs[i], event);					
 			}
 			return events;
 		} 
        
 		/**
 		 * Sets the progress of the progressBar.
 		 */
 		protected void onProgressUpdate(Integer... progress) {
     		System.out.println("Estoy en onProgressUpdate:"+progress[0].intValue());
           	loadProgressBar.setProgress(progress[0].intValue());
 		}
 		
 		/**
 		* Goes to the Date Activity and hides the progressBar.
         */
 		protected void onPostExecute(Map<String, List<Event>> result) {
 			System.out.println("onPostExecute de la primera tarea.");
 			loadProgressBar.setVisibility(View.GONE);
 			// Go to the Date Activity
 			Intent intent = new Intent(context, DateActivity.class);
 			//intent.putParcelableArrayListExtra(EXTRA_HTML, result);
 			startActivity(intent);
 			
 		}
 	}
 	/* Check which sites are meant to be shown
 	Map<String,?> all = sharedPref.getAll();
 	System.out.println("value:"+ all.get("multilist"));
 	for (Map.Entry<String, ?> entry : all.entrySet()){
 	    System.out.println(entry.getKey() + "/" + entry.getValue());
 	}
 	*/
 }
