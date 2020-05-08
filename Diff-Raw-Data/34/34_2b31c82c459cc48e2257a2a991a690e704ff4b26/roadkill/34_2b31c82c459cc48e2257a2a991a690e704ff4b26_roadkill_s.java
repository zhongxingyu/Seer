 package edu.ucdavis.cros.roadkill;
 
 /* TODO: Add license information, properly cite the Android dev code under Apache 2.0 license
 *
 *Code inspiration
 *http://developer.android.com/resources/tutorials/views/hello-timepicker.html
 *http://developer.android.com/resources/tutorials/views/hello-datepicker.html
 *
 */
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.ImageButton;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 
 public class roadkill extends Activity {
 	//Debug log tag id
 	private static final String TAG = "RoadKill";
 	//Declare Variables
 	public ImageButton photoButton;
 	public static String _path;
 //	private String strLatLong;
 //	private Date d;
 //	private SimpleDateFormat dateParser;
 //	private SimpleDateFormat dateConverter;
 	private Button locationButton;
 	private Button dateButton;
 	private Button timeButton;
 	private AutoCompleteTextView Species;
 	private Button saveButton;
 	public static final int PHOTO_BMP = 1;
 	//Declare Database
 	private dbAdapter myDbHelper;
 	//private DataBaseHelper myDbHelper;
 	private int mYear;
     private int mMonth;
     private int mDay;
 	static final int DATE_DIALOG_ID = 1;
 	private int mHour;
     private int mMinute;
     static final int TIME_DIALOG_ID = 2;
     private String lat = "38.5";
     private String lon = "-121.5";
     static final int LOCATION_DIALOG_ID = 3;
     private StringBuffer timestamp;
     LocationManager lm = null;
     LocationListener ll = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.alt);
 
         myDbHelper = new dbAdapter(this);
         myDbHelper.open();
         
         //DataBaseHelper myDbHelper = new DataBaseHelper();
         //myDbHelper = new DataBaseHelper(this);
  
         //try { myDbHelper.createDataBase();
         //} catch (IOException ioe) {
  		//throw new Error("Unable to create database"); 
         //}
 
         
         this.photoButton = (ImageButton)findViewById(R.id.photoButton);
         this.locationButton = (Button)findViewById(R.id.locationButton);
         this.dateButton = (Button)findViewById(R.id.dateButton);
         this.timeButton = (Button)findViewById(R.id.timeButton);
         splist();
         timestamp = new StringBuffer();
         
         //Turn on GPS at application start/resume for better/faster fix
         lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
         	Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         	startActivity(gpsIntent);
         }
         
         this.saveButton = (Button)findViewById(R.id.saveButton);
         
         
         //Set up listeners for each button
         this.photoButton.setOnClickListener(new OnClickListener(){
 //        	@Override
         	public void onClick(View v) {
         		//Call the code to take the picture
         		Log.i(TAG, "photoButton.onClick()" );
         		
         		// set path for photo to be saved in db
         		_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "CROSPic.jpg";
         		
         		//Start TakePhoto Activity
         		Intent photoIntent = new Intent(roadkill.this,TakePhoto.class);
         		startActivityForResult(photoIntent,PHOTO_BMP);
         		
         	}
         });
     	this.locationButton.setOnClickListener(new OnClickListener(){
 //        	@Override
         	public void onClick(View v) {
         		Log.i(TAG, "locationButton.onClick()" );
         		//open a selection window, 
         		//ask the user if they want the GPS fix
         		//or adjust manually on a map
         		showDialog(LOCATION_DIALOG_ID);
         	}
     	});
         this.dateButton.setOnClickListener(new OnClickListener(){
 //        	@Override
         	public void onClick(View v) {
         		//open a popup with a date/time selector widget
         		Log.i(TAG, "dateButton.onClick()" );
         		showDialog(DATE_DIALOG_ID);
         	}
     	});
         //Setup the current date/time for the dialog
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
         //Enable the next line only if you want set the data immediately
         //updateDisplay();
         this.timeButton.setOnClickListener(new OnClickListener(){
 //        	@Override
         	public void onClick(View v) {
         		//open a popup with a date/time selector widget
         		Log.i(TAG, "dateButton.onClick()" );
         		showDialog(TIME_DIALOG_ID);
         	}
     	});
         // get the current time
         mHour = c.get(Calendar.HOUR_OF_DAY);
         mMinute = c.get(Calendar.MINUTE);
 
         
         this.Species.setOnClickListener(new OnClickListener(){
 //        	@Override
         	public void onClick(View v) {
         		Log.i(TAG, "Species.onClick()" );
         		//clear the Species label once the start typing
         		//auto match based on the pre-seeded data
         		//if (Species.getText() == "Species"){
         		//		Species.setText("");
         		
         	}
     	});
     
         this.saveButton.setOnClickListener(new OnClickListener(){
 //        	@Override
         	public void onClick(View v) {
         		//save data to record, if existing record update information
         		Log.i(TAG, "saveButton.onClick()" );
         		Species.performValidation();
         		if (timestamp.length() < 1) {
 	        		timestamp.append(dateButton.getText());
 	        		timestamp.append("T");
 	        		timestamp.append(timeButton.getText());
         		}
         		String photopath = new String("");
         		//TODO: get real photo path, lat/lon from GPS, implement saving rating
         		myDbHelper.save(Species.getText().toString(), lat, lon, timestamp.toString(), photopath);
         		Log.i(TAG,"Saved Record");
         	}
     	});
     
     }
     
     private void splist() {
     	//Set the species list from a database query
     	final ArrayList<String> spplist = myDbHelper.spplist();
 //        startManagingCursor(sppCursor);
 //        String[] from = new String[]{"common"};
 //        int[] to = new int[]{R.id.speciesTextView};
 //        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_dropdown_item_1line, sppCursor, from, to);
 //        SpCurAdapter adapter = new SpCurAdapter(this,sppCursor);
 //        int desiredColumn = 1;
 //        adapter.setCursorToStringConverter(null);
 //        adapter.setStringConversionColumn(desiredColumn);
 
     	//TODO: fix, Hack to convert arraylist to stringarray?
     	final String[] mString = (String []) spplist.toArray(new String [spplist.size()]);
     	//Method uses a straight string array
 //    	ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,android.R.layout.simple_dropdown_item_1line, dbAdapter.animalList);
     	ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,android.R.layout.simple_dropdown_item_1line, mString);
     	//adapter.setStringConversionColumn(1);
         //adapter.convertToString(sppCursor);
         this.Species = (AutoCompleteTextView) findViewById(R.id.speciesTextView);
         this.Species.setAdapter(adapter);
     }
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	//Triggered from the device menu button
     	//Allows user to reach other views and configure the application
     	// TODO : implement opening views based on button picked, with onClick property in menu.xml
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.view_menu, menu);
         return true;
     }   
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
     	//handles the pop up menu
     	switch (item.getItemId()) {
         case R.id.op_settings:
             Log.i(TAG,"Settings clicked");
             return true;
         case R.id.op_upload:
             Log.i(TAG,"Upload clicked");
             return true;
         case R.id.op_map:
             Log.i(TAG,"Map clicked");
             //Intent datamap = new Intent(roadkill.this,Maps.class);
             //TODO: replace Intent with Map Activity that shows the data
 //            StringBuffer loc = new StringBuffer();
 //            loc.append("geo:");
 //            loc.append(lat);
 //            loc.append(",");
 //            loc.append(lon);
 //            loc.append("?z=10");
             Intent locateIntent = new Intent(roadkill.this,DataMap.class);
         	locateIntent.putExtra(DataMap.EXTRA_LATITUDE, lat);
         	locateIntent.putExtra(DataMap.EXTRA_LONGITUDE, lon);
         	locateIntent.putExtra(DataMap.EXTRA_NAME, Species.getText().toString());
         	startActivity(locateIntent);
             //Intent datamap = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(loc.toString()));
             //startActivity(datamap);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
 
     }
     
     @Override
     protected Dialog onCreateDialog(int id) {
             switch (id) {
 
             case DATE_DIALOG_ID:
                     return new DatePickerDialog(this,
                             mDateSetListener,
                             mYear, mMonth, mDay);
             
             case TIME_DIALOG_ID:
 		        return new TimePickerDialog(this,
 		                mTimeSetListener, mHour, mMinute, false);
 		    
             case LOCATION_DIALOG_ID: {
             	final CharSequence[] items = {"From Photo Data", "From GPS", "From Map"};
             	AlertDialog.Builder builder = new AlertDialog.Builder(this);
             	builder.setTitle("Get Location:");
             	builder.setItems(items, new DialogInterface.OnClickListener() {
             	    public void onClick(DialogInterface dialog, int item) {
             	        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
             	        Log.i(TAG,items[item].toString());
             	        switch (item) {
             	        case 0:
             	        	//From Photo
             	        	lat = "0";
             	        	lon = "0";
             	        	LocationSet();
             	        	break;
             	        case 1:
             	        	//From GPS
             	        	Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER); //
                     		lat = String.format("%f",location.getLatitude());
                     		lon = String.format("%f", location.getLongitude());
                     		LocationSet();
                     		break;
             	        case 2:
             	        	//FROM Map
             	        	lat = "38.6";
             	            lon = "-121.1";
             	        	//Intent locateIntent = new Intent(roadkill.this,DataMap.class);
             	        	//startActivityForResult(locateIntent,PHOTO_BMP);
             	        	LocationSet();
             	        	break;
             	        }
             	    }
             	});
             	AlertDialog gpsalert = builder.create();
             	return gpsalert;
             }
             }
 
             return null;
     }
     private void LocationSet(){
     	StringBuffer loc = new StringBuffer();
         loc.append(lat);
         loc.append(",");
         loc.append(lon);
     	locationButton.setText(loc.toString());
     }
     protected void onPrepareDialog(int id, Dialog dialog) {
             switch (id) {
 
             case DATE_DIALOG_ID:
                     ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                     break;
             }
     }    
     
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
 
             public void onDateSet(DatePicker view, int year, int monthOfYear,
                             int dayOfMonth) {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     DateTime.DupdateDisplay(mYear,mMonth,mDay,dateButton);
             }
     };
     // the callback received when the user "sets" the time in the dialog
     private TimePickerDialog.OnTimeSetListener mTimeSetListener =
         new TimePickerDialog.OnTimeSetListener() {
    		@Override
             public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                 mHour = hourOfDay;
                 mMinute = minute;
                 DateTime.TupdateDisplay(mHour,mMinute,timeButton);
             }
 
 			
     };
     
 //    private GPSDialog extends AlertDialog{
 //    TODO: Implement AlertDialog as it's own function/class, work ok as is for now.	
 //    };
         
     //TODO: Handle Pause, Stop, Resume etc - don't forget to close the database and turn on/off the GPS
         
 
 	   @Override
 	   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 //		   super.onActivityResult(requestCode, resultCode, extras);
 		   //return the photo to the button as a thumbnail
 
 		   if (requestCode == PHOTO_BMP) {
 			   if (resultCode == RESULT_OK) {
 				   
 				   // Downsample the image size (inSampleSize > 1) for viewing in App
 				   BitmapFactory.Options options = new BitmapFactory.Options();
 				   //TODO: return image that fits the button
 				   options.inSampleSize = 16;
 				   //options.outWidth = 100;
 				   // Put bitmap image onto photoButton
 				   Bitmap bitmap = BitmapFactory.decodeFile( _path, options );
 				   photoButton.setImageBitmap(bitmap);
 				   
 				   //return the exif data in the photo for date, time and location
 				   if (TakePhoto.strLatC != null) {
 					   String strLatLong = new String (TakePhoto.strLatC + "," + TakePhoto.strLongC);
 					   locationButton.setText(strLatLong);
 				   };
 			   
 				   if (TakePhoto.strDateTime != null) {
					   SimpleDateFormat dateParser = new SimpleDateFormat("yyy:MM:dd HH:mm:ss");
					   SimpleDateFormat dateConverter = new SimpleDateFormat ("yyy-MM-dd'T'HH:mm:ss");
					   Date d = null;
							try {
								d = dateParser.parse(TakePhoto.strDateTime);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					   timestamp.append(dateConverter.format(d));
 				   }
 			   }			   
 		   }
 
 	   }
 	   
 };
