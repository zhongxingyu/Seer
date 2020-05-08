 /*
  * Copyright 2010 Andrew De Quincey -  adq@lidskialf.net
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BusTimesActivity extends ListActivity implements BusDataResponseListener {
 
 	private long stopCode = -1;
 	private String stopName = "";	
 	private String sorting = "";
 
 	private ProgressDialog busyDialog = null;
 	private int expectedRequestId = -1;
 	
 	public static final int ALERT_NOTIFICATION_ID = 1;
 
 	private static final SimpleDateFormat titleDateFormat = new SimpleDateFormat("EEE dd MMM HH:mm");
 	private static final SimpleDateFormat advanceDateFormat = new SimpleDateFormat("EEE dd MMM yyyy");
 
 	private static final String[] temporalAlarmStrings = new String[] { "Due", "5 mins away", "10 mins away", "20 mins away", "30 mins away" };
 	private static final int[] temporalAlarmTimeouts = new int[] { 0, 5 * 60, 10 *  60, 20 * 60, 30 * 60 };
 
 	private static final String[] proximityAlarmStrings = new String[] { "50  metres", "100 metres", "250 metres", "500 metres" };
 	private static final int[] proximityAlarmDistances= new int[] { 50, 100, 200, 500};
 
 	private static final String[] hourStrings = new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };
 	private static final String[] minStrings = new String[] { "00", "15", "30", "45" };
 	
 	public static void showActivity(Context context, long stopCode) {
 		Intent i = new Intent(context, BusTimesActivity.class);
 		i.putExtra("StopCode", stopCode);
 		context.startActivity(i);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.bustimes);
 		registerForContextMenu(getListView());
 		
 		stopCode = getIntent().getLongExtra("StopCode", -1);
 		if (stopCode != -1)
 			findViewById(android.R.id.empty).setVisibility(View.GONE);
 		
 		LocalDBHelper db = new LocalDBHelper(this);
 		try {
 			sorting = db.getGlobalSetting("bustimesort", "arrival");
 			stopName = db.getBookmarkName(stopCode);
 		} finally {
 			db.close();
 		}
 		
 		PointTree pt = PointTree.getPointTree(this);
 		int stopNodeIdx = pt.lookupStopNodeIdxByStopCode((int) stopCode);
 		if (stopNodeIdx != -1) {
 			stopName = pt.lookupStopNameByStopNodeIdx(stopNodeIdx);
 		} else {
 			stopName = "";
 		}
 		
 		update();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		busyDialog = null;
 		super.onDestroy();		
 	}
 
 	private void update() {
 		update(0, null);
 	}
 
 	private void update(int daysInAdvance, Date timeInAdvance) {
 		if (stopCode != -1) {
 			Date displayDate = timeInAdvance;
 			if (displayDate == null)
 				displayDate = new Date();
 	
 			setTitle(stopName + " (" + titleDateFormat.format(displayDate) + ")");
 			displayBusy("Retrieving bus times");
 
 			expectedRequestId = BusDataHelper.getBusTimesAsync(stopCode, daysInAdvance, timeInAdvance, this);
 		} else {
 			setTitle("Unknown bus stop");
 			findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void displayBusy(String reason) {
 		dismissBusy();
 
 		busyDialog = ProgressDialog.show(this, "", reason, true, true, new OnCancelListener() {
 			public void onCancel(DialogInterface dialog) {
 				BusTimesActivity.this.expectedRequestId = -1;
 			}
 		});
 	}
 
 	private void dismissBusy() {
 		if (busyDialog != null) {
 			try {
 				busyDialog.dismiss();
 			} catch (Throwable t) {
 			}
 			busyDialog = null;
 		}
 	}
 
 	private void hideStatusBoxes() {
 		findViewById(R.id.bustimes_nodepartures).setVisibility(View.GONE);
 		findViewById(R.id.bustimes_error).setVisibility(View.GONE);
 		findViewById(android.R.id.empty).setVisibility(View.GONE);
 	}
 
 	public void getBusTimesError(int requestId, int code, String message) {
 		if (requestId != expectedRequestId)
 			return;
 		
 		dismissBusy();
 		hideStatusBoxes();
 
 		setListAdapter(new BusTimesAdapter(this, R.layout.bustimes_item, new ArrayList<BusTime>()));
 		findViewById(R.id.bustimes_error).setVisibility(View.VISIBLE);
 
 		new AlertDialog.Builder(this).setTitle("Error").
 			setMessage("Unable to download bus times: " + message).
 			setPositiveButton(android.R.string.ok, null).
 			show();
 	}
 
 	public void getBusTimesSuccess(int requestId, List<BusTime> busTimes) {
 		if (requestId != expectedRequestId)
 			return;
 		
 		dismissBusy();
 		hideStatusBoxes();
 		
 		if (sorting.equalsIgnoreCase("service")) {
 			Collections.sort(busTimes, new Comparator<BusTime>() {
 				public int compare(BusTime arg0, BusTime arg1) {
 					if (arg0.baseService != arg1.baseService)
 						return arg0.baseService - arg1.baseService;
 					return arg0.service.compareTo(arg1.service);
 				}
 			});
 		} else if (sorting.equalsIgnoreCase("arrival")) {
 			Collections.sort(busTimes, new Comparator<BusTime>() {
 				public int compare(BusTime arg0, BusTime arg1) {
 					if ((arg0.arrivalAbsoluteTime != null) && (arg1.arrivalAbsoluteTime != null)) {
 						// bus data never seems to span to the next day, so this string comparison should always work
 						return arg0.arrivalAbsoluteTime.compareTo(arg1.arrivalAbsoluteTime);
 					}
 					return arg0.arrivalSortingIndex - arg1.arrivalSortingIndex;
 				}
 			});
 		}
 
 		setListAdapter(new BusTimesAdapter(this, R.layout.bustimes_item, busTimes));
 		if (busTimes.isEmpty())
 			findViewById(R.id.bustimes_nodepartures).setVisibility(View.VISIBLE);
 	}
 	
 	private void UpdateServicesList(Button b, String[] services, boolean[] selectedServices)
 	{
 		StringBuffer result = new StringBuffer();
 		for(int i=0; i< services.length; i++) {
 			if (selectedServices[i]) {
 				if (result.length() > 0)
 					result.append(", ");
 				result.append(services[i]);
 			}
 		}
 		b.setText(result);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		TextView clickedService = (TextView) v.findViewById(R.id.bustimes_service);
 		addTemporalAlert(clickedService.getText().toString());
 	}
 
 	private void addOngoingNotification(Context context)
 	{
 		Intent i = new Intent(this, AlertNotificationPressedReceiver.class);		
 		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT); 
 
 		Notification notification = new Notification(R.drawable.tracker_24x24_masked, "Bus alarm active", System.currentTimeMillis());
 		notification.defaults = 0;
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 		notification.setLatestEventInfo(context, "Bus alarm active", "Press to cancel", pi);
 
 		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.notify(ALERT_NOTIFICATION_ID, notification);
 	}
 
 	public static void cancelAlerts(Context ctx) {
 		// cancel any temporal alert
 		Intent i = new Intent(ctx, TemporalAlarmReceiver.class);
 		PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_NO_CREATE);
 		if (pi != null) {
 			AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
 			am.cancel(pi);
 			pi.cancel();
 		}
 
 		// cancel any proximity alert
 		i = new Intent(ctx, ProximityAlarmReceiver.class);
 		pi = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_NO_CREATE);
 		if (pi != null) {
 			LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
 			lm.removeUpdates(pi);
 			lm.removeProximityAlert(pi);
 			pi.cancel();
 		}
 		
 		// cancel any ongoing alerts
 		NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.cancel(ALERT_NOTIFICATION_ID);
 	}
 	
 	private void addTemporalAlert(String selectedService) {
 		// get the list of services for this stop
 		PointTree pt = PointTree.getPointTree(this);
 		int stopNodeIdx = pt.lookupStopNodeIdxByStopCode((int) stopCode);
 		if (stopNodeIdx == -1)
 			return;
 		BusServiceMap serviceMap = pt.lookupServiceMapByStopNodeIdx(stopNodeIdx);
 		
 		ArrayList<String> servicesList = pt.getServiceNames(serviceMap);
 		final String[] services = servicesList.toArray(new String[servicesList.size()]);
 		final boolean[] selectedServices = new boolean[services.length];
 
 		// preselect the clicked-on service
 		if (selectedService != null) {
 			for(int i=0; i< services.length; i++) {
 				if (selectedService.equalsIgnoreCase(services[i])) {
 					selectedServices[i] = true;
 					break;
 				}
 			}
 		} else {
 			if (selectedServices.length > 0)
 				selectedServices[0] = true;
 		}
 
 		// load the view
 		View dialogView = getLayoutInflater().inflate(R.layout.addtemporalalert, null);		
 
 		// setup services selector
 		final Button servicesButton = (Button) dialogView.findViewById(R.id.addtemporalalert_services);
 		BusTimesActivity.this.UpdateServicesList(servicesButton, services, selectedServices);
 		servicesButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				new AlertDialog.Builder( BusTimesActivity.this )
 	     	       .setMultiChoiceItems( services, selectedServices, new DialogInterface.OnMultiChoiceClickListener() {
 						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 							selectedServices[which] = isChecked;							
 						}
 	     	       })
 	     	       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							BusTimesActivity.this.UpdateServicesList(servicesButton, services, selectedServices);
 						}
 	     	       })
 	     	       .show();
 			}
 		});
 
 		// setup time selector
 		final Spinner timeSpinner = (Spinner) dialogView.findViewById(R.id.addtemporalalert_time);
 		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, temporalAlarmStrings);
 		timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		timeSpinner.setAdapter(timeAdapter);
 
 		// show the dialog!
 		new AlertDialog.Builder(this)
 			.setView(dialogView)
 			.setTitle("Set alarm")
 			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					// cancel any current alerts
 					cancelAlerts(BusTimesActivity.this);
 					
 					// figure out list of services
 					ArrayList<String> selectedServicesList = new ArrayList<String>();
 					for(int i=0; i< services.length; i++) {
 						if (selectedServices[i]) {
 							selectedServicesList.add(services[i]);
 						}
 					}
 					if (selectedServicesList.size() == 0)
 						return;
 
 					// create an intent
 					Intent i = new Intent(BusTimesActivity.this, TemporalAlarmReceiver.class);
 					i.putExtra("StopCode", stopCode);
 					i.putExtra("StopName", stopName);
 					i.putExtra("Services", selectedServicesList.toArray(new String[selectedServicesList.size()]));
 					i.putExtra("StartTime", System.currentTimeMillis());
 					i.putExtra("TimeoutSecs", temporalAlarmTimeouts[timeSpinner.getSelectedItemPosition()]);
 					PendingIntent pi = PendingIntent.getBroadcast(BusTimesActivity.this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 
 					// schedule it in 10 seconds
 					AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 					am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pi);
 	
 					addOngoingNotification(BusTimesActivity.this);
 				
 					Toast.makeText(BusTimesActivity.this, "Alarm added!", Toast.LENGTH_SHORT).show();
 				}
 			})
 			.setNegativeButton(android.R.string.cancel, null)
 			.show();
 	}
 
 	private void addProximityAlert() {
 		// load the view
 		View dialogView = getLayoutInflater().inflate(R.layout.addproximityalert, null);		
 
 		// setup distance selector
 		final Spinner distanceSpinner = (Spinner) dialogView.findViewById(R.id.addproximityalert_distance);
 		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, proximityAlarmStrings);
 		timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		distanceSpinner.setAdapter(timeAdapter);
 
 		// show the dialog!
 		new AlertDialog.Builder(this)
 			.setView(dialogView)
 			.setTitle("Set alarm")
 			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					PointTree pt = PointTree.getPointTree(BusTimesActivity.this);
 					int stopNodeIdx = pt.lookupStopNodeIdxByStopCode((int) stopCode);
 					if (stopNodeIdx == -1)
 						return;
 
 					// cancel any current alerts
 					cancelAlerts(BusTimesActivity.this);
 
 					// stop location
 					Location location = new Location("");
					location.setLatitude(pt.lat[stopNodeIdx] / 1E6);
					location.setLongitude(pt.lon[stopNodeIdx] / 1E6);
 
 					// create an intent
 					Intent i = new Intent(BusTimesActivity.this, ProximityAlarmReceiver.class);
 					i.putExtra("StopCode", stopCode);
 					i.putExtra("StopName", stopName);
 					i.putExtra("Location", location);
 					i.putExtra("Distance", proximityAlarmDistances[distanceSpinner.getSelectedItemPosition()]);
 					i.putExtra("StartTime", System.currentTimeMillis());
 					PendingIntent pi = PendingIntent.getBroadcast(BusTimesActivity.this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 
 					// weird! Found I needed to add a proximity alert *first* otherwise the GPS on my phone doesn't get a lock with just the requestlocationupdates!?!
 					LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 					lm.addProximityAlert(pt.lat[stopNodeIdx], pt.lon[stopNodeIdx], 1, 0, pi);
 					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 1000, 25, pi);
 
 					addOngoingNotification(BusTimesActivity.this);
 
 					Toast.makeText(BusTimesActivity.this, "Alarm added!", Toast.LENGTH_SHORT).show();
 				}
 			})
 			.setNegativeButton(android.R.string.cancel, null)
 			.show();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.bustimes_menu, menu);
 		
         LocalDBHelper db = new LocalDBHelper(this);
         try {
         	if (db.isBookmark(stopCode)) {
         		menu.findItem(R.id.bustimes_menu_addbookmark).setEnabled(false);
         	} else {
         		menu.findItem(R.id.bustimes_menu_renamebookmark).setEnabled(false);
         		menu.findItem(R.id.bustimes_menu_deletebookmark).setEnabled(false);
         	}
         } finally {
         	db.close();
         }
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.bustimes_menu_refresh:
 			update();
 			return true;
 
 		case R.id.bustimes_menu_addbookmark:
 			if (stopCode != -1) {
 				LocalDBHelper db = new LocalDBHelper(this);
 				try {
 					db.addBookmark(stopCode, stopName);
 				} finally {
 					db.close();
 				}
 				Toast.makeText(this, "Added bookmark", Toast.LENGTH_SHORT).show();
 			}
 			return true;
 			
 		case R.id.bustimes_menu_renamebookmark: {
 			 final EditText input = new EditText(this);
 				input.setText(stopName);
 
 				new AlertDialog.Builder(this)
 						.setTitle("Rename bookmark")
 						.setView(input)
 						.setPositiveButton(android.R.string.ok,
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int whichButton) {
 				                        LocalDBHelper db = new LocalDBHelper(BusTimesActivity.this);
 				                        try {
 				                        	db.renameBookmark(BusTimesActivity.this.stopCode, input.getText().toString());
 				                        } finally {
 				                        	db.close();
 				                        }
 				                        stopName = input.getText().toString();
 				                        update();
 									}
 								})
 						.setNegativeButton(android.R.string.cancel, null)
 						.show();
 			return true;
 		}
 
 		case R.id.bustimes_menu_deletebookmark: {
 			new AlertDialog.Builder(this)
 				.setTitle("Delete bookmark")
 				.setMessage("Are you sure you want to delete this bookmark?")
 				.setPositiveButton(android.R.string.ok, 
 						new DialogInterface.OnClickListener() {
 			                public void onClick(DialogInterface dialog, int whichButton) {
 			                    LocalDBHelper db = new LocalDBHelper(BusTimesActivity.this);
 			                    try {
 			                    	db.deleteBookmark(BusTimesActivity.this.stopCode);
 			                    } finally {
 			                    	db.close();
 			                    }
 			                    BusTimesActivity.this.update();
 			                }
 						})
 				.setNegativeButton(android.R.string.cancel, null)
 	            .show();
 			return true;
 		}
 
 
 		case R.id.bustimes_menu_viewonmap:
 			PointTree pt = PointTree.getPointTree(BusTimesActivity.this);
 			int stopNodeIdx = pt.lookupStopNodeIdxByStopCode((int) stopCode);
 			if (stopNodeIdx != -1)
 				StopMapActivity.showActivity(this, pt.lat[stopNodeIdx], pt.lon[stopNodeIdx]);
 			return true;
 
 
 		case R.id.bustimes_menu_futuredepartures:
 			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View v = vi.inflate(R.layout.futuredepartures, null);
 
 			final GregorianCalendar calendar = new GregorianCalendar();
 			final Spinner datePicker = (Spinner) v.findViewById(R.id.futuredepartures_date);
 			String[] dates = new String[4];
 			for(int i=0; i < 4; i++) {
 				dates[i] = advanceDateFormat.format(calendar.getTime());
 				calendar.add(Calendar.DAY_OF_MONTH, 1);
 			}
 			ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dates);
 			dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			datePicker.setAdapter(dateAdapter);
 			calendar.add(Calendar.DAY_OF_MONTH, -4);
 			
 			final Spinner hourPicker = (Spinner) v.findViewById(R.id.futuredepartures_time_hour);
 			ArrayAdapter<String> hourAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hourStrings);
 			hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			hourPicker.setAdapter(hourAdapter);
 			hourPicker.setSelection(12);
 			
 			final Spinner minPicker = (Spinner) v.findViewById(R.id.futuredepartures_time_min);
 			ArrayAdapter<String> minAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, minStrings);
 			minAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			minPicker.setAdapter(minAdapter);
 
 			new AlertDialog.Builder(this)
 				.setTitle("Choose the desired date/time")
 				.setView(v)
 				.setPositiveButton(android.R.string.ok,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int whichButton) {
 								calendar.add(Calendar.DAY_OF_MONTH, datePicker.getSelectedItemPosition());
 								calendar.set(Calendar.HOUR_OF_DAY, hourPicker.getSelectedItemPosition());
 								calendar.set(Calendar.MINUTE, minPicker.getSelectedItemPosition() * 15);
 								update(datePicker.getSelectedItemPosition(), calendar.getTime());
 							}
 						})
 				.setNegativeButton(android.R.string.cancel, null)
 				.show();
 			return true;
 
 		case R.id.bustimes_menu_sorting_arrival: {
 			sorting = "arrival";
 			
 			LocalDBHelper db = new LocalDBHelper(this);
 			try {
 				db.setGlobalSetting("bustimesort",  sorting);
 			} finally {
 				db.close();
 			}
 			update();
 			return true;
 		}
 
 		case R.id.bustimes_menu_sorting_service: {
 			sorting = "service";
 			
 			LocalDBHelper db = new LocalDBHelper(this);
 			try {
 				db.setGlobalSetting("bustimesort",  sorting);
 			} finally {
 				db.close();
 			}
 			update();
 			return true;
 		}
 		
 		case R.id.bustimes_menu_proximityalert: {
 			addProximityAlert();
 			return true;
 		}
 		
 		case R.id.bustimes_menu_temporalalert:
 			addTemporalAlert(null);
 			return true;			
 		}
 
 		return false;
 	}
 
 	private class BusTimesAdapter extends ArrayAdapter<BusTime> {
 		private List<BusTime> items;
 		private int textViewResourceId;
 
 		public BusTimesAdapter(Context context, int textViewResourceId, List<BusTime> items) {
 			super(context, textViewResourceId, items);
 
 			this.textViewResourceId = textViewResourceId;
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(textViewResourceId, null);
 			}
 
 			BusTime busTime = items.get(position);
 			if (busTime != null) {
 				TextView serviceView = (TextView) v.findViewById(R.id.bustimes_service);
 				TextView destinationView = (TextView) v.findViewById(R.id.bustimes_destination);
 				TextView timeView = (TextView) v.findViewById(R.id.bustimes_time);
 
 				serviceView.setText(busTime.service);
 				
 				if (busTime.isDiverted) {
 					destinationView.setText("DIVERTED");
 					timeView.setText("");
 				} else {
 					destinationView.setText(busTime.destination);
 	
 					if (busTime.arrivalIsDue)
 						timeView.setText("Due");
 					else if (busTime.arrivalAbsoluteTime != null)
 						timeView.setText(busTime.arrivalAbsoluteTime);
 					else
 						timeView.setText(Integer.toString(busTime.arrivalMinutesLeft));
 					
 					if (busTime.arrivalEstimated)
 						timeView.setText("~" + timeView.getText());
 					}
 			}
 
 			return v;
 		}
 	}
 }
