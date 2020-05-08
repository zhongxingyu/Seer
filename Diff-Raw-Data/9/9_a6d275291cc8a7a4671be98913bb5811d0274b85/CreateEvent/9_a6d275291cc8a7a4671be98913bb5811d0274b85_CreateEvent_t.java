 package com.appchallenge.android;
 
 import android.accounts.AccountManager;
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import java.text.DateFormat;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.appchallenge.android.Event.Type;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 /**
  * Wizard activity for creating new events.
  */
 public class CreateEvent extends SherlockFragmentActivity implements CreateEventInterface {
     /**
      * A pager widget that controls animating between fragments.
      */
     private ViewPager mPager;
 
     /**
      * The pager adapter, which provides the pages to the view pager widget.
      */
     private PagerAdapter mPagerAdapter;
 
     /**
      * Provides access to our local sqlite database.
      */
     private LocalDatabase localDB;
 
     /**
      * The local event we are creating.
      */
     private LocalEvent newEvent;
 
     // Method implementations for CreateEventInterface.
 	public void setTitle(String title) {
 		newEvent.setTitle(title);
 	}
 	public String getEventTitle() {
 		return newEvent.getTitle();
 	}
 
 	public void setType(Type type) {
 		newEvent.setType(type);
 	}
 	public Type getType() {
 		return newEvent.getType();
 	}
 
 	public void setDescription(String description) {
 		newEvent.setDescription(description);
 	}
 	public String getDescription() {
 		return newEvent.getDescription();
 	}
 
 	public void setStartDate(Date date) {
 		newEvent.setStartDate(date);
 	}
 	public Date getStartDate() {
 		return newEvent.getStartDate();
 	}
 
 	public void setEndDate(Date date) {
 		newEvent.setEndDate(date);
 	}
 	public Date getEndDate() {
 		return newEvent.getEndDate();
 	}
 
 	public void setLocation(LatLng location) {
 		newEvent.setLocation(location);
 	}
 	public LatLng getLocation() {
 		return newEvent.getLocation();
 	}
 
 	public MarkerOptions getMarker() {
 		return newEvent.toMarker(true);
 	}
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_create_event);
         
         // The home button takes the user back to the map display.
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         
         // Keeps keyboard from popping up unless invoked
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
         Intent receivedIntent = getIntent();
 
         // Set the initial date values for the event.
         if (savedInstanceState != null)
         	this.newEvent = savedInstanceState.getParcelable("newEvent");
         else {
         	// See if an event was passed in.
         	if (receivedIntent.hasExtra("event")) {
         		this.newEvent = new LocalEvent((Event)receivedIntent.getParcelableExtra("event"));
 
         		// Establish proper relative time intervals.
         		Calendar today = Calendar.getInstance();
         		Calendar c = Calendar.getInstance();
         		c.setTime(this.newEvent.getStartDate());
         		c.set(Calendar.DAY_OF_YEAR, today.get(Calendar.DAY_OF_YEAR));
         		this.newEvent.setStartDate(c.getTime());
         		c.setTime(this.newEvent.getEndDate());
         		c.set(Calendar.DAY_OF_YEAR, today.get(Calendar.DAY_OF_YEAR));
         		this.newEvent.setEndDate(c.getTime());
         		
         	}
         	else
         	    this.newEvent = new LocalEvent();
         }
 
         if (this.getStartDate() == null && this.getEndDate() == null) {
         	this.setStartDate(new Date());
         	Calendar c = Calendar.getInstance();
            	c.add(Calendar.HOUR_OF_DAY, 3);
             this.setEndDate(c.getTime());
         }
 
         // Set the map location to use the location passed in.
         if (this.getLocation() == null)
             this.setLocation((LatLng)receivedIntent.getParcelableExtra("location"));
 
         // Instantiate a ViewPager and a PagerAdapter.
         mPager = (ViewPager)findViewById(R.id.pager);
         mPagerAdapter = new CreateEventPagerAdapter(getSupportFragmentManager());
         mPager.setAdapter(mPagerAdapter);
         mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When changing pages, reset the action bar actions since they are dependent
                 // on which page is currently active. An alternative approach is to have each
                 // fragment expose actions itself (rather than the activity exposing actions),
                 // but for simplicity, the activity provides the actions.
                 invalidateOptionsMenu();
             }
         });
     }
 
     public void onSaveInstanceState(Bundle savedInstanceState) {
     	savedInstanceState.putParcelable("newEvent", this.newEvent);
     	super.onSaveInstanceState(savedInstanceState);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getSupportMenuInflater().inflate(R.menu.activity_create_event, menu);
 
         menu.findItem(R.id.action_back).setEnabled(mPager.getCurrentItem() > 0);
 
         // Add either a "next" or "submit" button to the action bar.
         int action = R.string.action_next;
         int id = R.id.action_next;
         if (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1) {
         	action = R.string.action_finish;
         	id = R.id.action_submit;
         }
 
         MenuItem item = menu.add(Menu.NONE, id, Menu.NONE, action);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {    
         int duration = Toast.LENGTH_SHORT;
 
         // Close the keyboard after each page change.
         try {
         	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
         } catch (Exception e) {
         	Log.w("CreateEvent.onOptionsItemSelected", "hideSoftInputFromWindow threw an error.");
         }
 
         if (item.getItemId() == android.R.id.home) {
 			// Handle navigating back to the EventViewer on Home press.
         	Intent viewerIntent = new Intent(this, EventViewer.class);
             if (NavUtils.shouldUpRecreateTask(this, viewerIntent)) {
                 NavUtils.navigateUpTo(this, viewerIntent);
                 finish();
             } else
                 finish();
             return true;
 		} else if (item.getItemId() == R.id.action_back) {
 			// Go to the previous step in the wizard. If there is no previous step,
 			// setCurrentItem will do nothing.
 			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
 			return true;
 		} else if (item.getItemId() == R.id.action_next) {
 			// Advance to the next step in the wizard after performing error checking.
 			if (mPager.getCurrentItem() == 0 && this.getEventTitle().trim() == "") {
 				Toast.makeText(this, "Please enter a title for the event!", duration).show();
 				return true;
 			}
 
 			if (mPager.getCurrentItem() == 1) {
 				if (getStartDate().after(getEndDate()) || getEndDate().before(new Date())) {
 					Toast.makeText(this, "Please enter a valid timespan!", duration).show();
 					return true;
 				}
 			}
 
 			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
 			return true;
 		} else if (item.getItemId() == R.id.action_submit) {
 			// Prevent being offline from losing entered data.
 			if (!isOnline()) {
 				Toast.makeText(this, "Please connect to a network and try again.", duration).show();
 				return true;
 			}
 
 			// Perform an asynchronous API call to create the new event.
 			createEventAPICaller apiCall = new createEventAPICaller();
 			apiCall.execute(this.newEvent);
 			return true;
 		}
 
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onBackPressed() {
         // Have the back button go back one page instead of exit.
         if (mPager.getCurrentItem() != 0) {
             mPager.setCurrentItem(mPager.getCurrentItem() - 1);
             return;
         }
         super.onBackPressed(); 
     }
 
     protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == GoogleAuth.REQUEST_CODE_GOOGLE_PLUS_ACCOUNTNAME && resultCode == RESULT_OK) {
             String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
             Log.d("CreateEvent.onActivityResult", "Got account name: " + accountName);
         }
     }
 
     /**
 	 * Determines if the device has internet connectivity.
 	 * @return Whether a data connection is available.
 	 */
 	public boolean isOnline() {
         ConnectivityManager connectivityManager =
           (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 
         return (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable());
     }
 
     /**
      * Shows the time picker to allow changing the Event time.
      * @param v
      */
     public void showEventTimeDialog(View v) {
     	DialogFragment timePicker;
     	switch (v.getId()) {
 	    	case R.id.event_start_button:
 	            timePicker = new StartTimePicker();
 	            timePicker.show(getSupportFragmentManager(), "startTimePicker");
 	            break;
 	    	case R.id.event_end_button:
 	    		timePicker = new EndTimePicker();
 	    		timePicker.show(getSupportFragmentManager(), "endTimePicker");
 	            break;
     	}
     }
 
 	@SuppressLint("ValidFragment")
 	public class StartTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the current time as the default values for the picker
 			Date startTime = ((CreateEventInterface)getActivity()).getStartDate();
 			Calendar c = Calendar.getInstance();
 			c.setTime(startTime);
 			
 			// Create a new instance of TimePickerDialog and return it
 			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			// Create a new Date object with the updated time.
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(((CreateEventInterface)getActivity()).getStartDate());
 			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			cal.set(Calendar.MINUTE, minute);
 
 			// Send this updated date back to the wizard activity.
 			Date newDate = cal.getTime();
 			((CreateEventInterface)getActivity()).setStartDate(newDate);
 
 			// Update the display text.
 			String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate);
 		    ((Button) findViewById(R.id.event_start_button)).setText(timeString);
 		}
     }
 
 	@SuppressLint("ValidFragment")
 	public class EndTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			Date endTime = ((CreateEventInterface) getActivity()).getEndDate();
 			Calendar c = Calendar.getInstance();
 			c.setTime(endTime);
 
 			// Create a new instance of TimePickerDialog and return it
 			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			// Create a new Date object with the updated time.
 			Calendar c = Calendar.getInstance();
 			c.setTime(((CreateEventInterface)getActivity()).getEndDate());
 			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			c.set(Calendar.MINUTE, minute);
 
 			// Send this updated date back to the wizard activity.
 			Date newDate = c.getTime();
 			((CreateEventInterface)getActivity()).setEndDate(newDate);
 
 			// Update the display text.
 			String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate);
 		    ((Button) findViewById(R.id.event_end_button)).setText(timeString);
 		}
     }
     
 	/**
      * A pager adapter that represents the wizard pages sequentially.
      */
     public class CreateEventPagerAdapter extends FragmentStatePagerAdapter {
     	// Instances of each wizard page, in order.
     	public Fragment[] wizardPages = new Fragment[] {
             new CreateEventPage1EventName(),
             new CreateEventPage2EventTime(),
             new CreateEventPage3EventLocation()
     	};
     	
         public CreateEventPagerAdapter(FragmentManager fragmentManager) {
             super(fragmentManager);
         }
 
         @Override
         public Fragment getItem(int position) {
             return wizardPages[position];
         }
 
         @Override
         public int getCount() {
             return wizardPages.length;
         }
     }
 
     /**
 	 * Performs an asynchronous API call create a new event.
 	 */
 	private class createEventAPICaller extends AsyncTask<Event, Void, Event> {
 		/**
 	     * Informs the user that the event is being created.
 	     */
 	    ProgressDialog dialog;
 
 		@Override
 		protected void onPreExecute() {
 			// Set up progress indication.
 			dialog = ProgressDialog.show(CreateEvent.this, "Creating...", "");
 		}
 
 		@Override
 		protected Event doInBackground(Event... event) {
 			return APICalls.createEvent(event[0], Identity.getUserId(getApplicationContext()));
 		}
 
 		@Override
 		protected void onPostExecute(Event result) {
 			// Close the wizard and any progress indication.
 			dialog.dismiss();
 			dialog = null;
 			if (result == null) {
 				(Toast.makeText(getApplicationContext(), "Could not create event!", Toast.LENGTH_LONG)).show();
 				return;
 			}
 
 			// Save this event's secret_id to our local storage.
 	    	if (localDB == null)
 	    		localDB = new LocalDatabase(CreateEvent.this);
 	    	localDB.takeOwnership(result);
 			
 			// Pass the new event to the event viewer.
 			Intent intent = new Intent(CreateEvent.this, EventViewer.class);
 			intent.putExtra("event", result);
 			CreateEvent.this.setResult(RESULT_OK, intent);
 			CreateEvent.this.finish();
 		}
 	}
 }
