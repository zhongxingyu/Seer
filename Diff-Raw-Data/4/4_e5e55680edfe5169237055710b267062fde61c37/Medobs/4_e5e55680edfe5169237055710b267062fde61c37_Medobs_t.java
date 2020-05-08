 package sk.gista.medobs;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import sk.gista.medobs.view.CalendarListener;
 import sk.gista.medobs.view.CalendarView;
 import sk.gista.medobs.view.ReservationAdapter;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Medobs extends Activity implements CalendarListener {
 
 	public static String VERSION;
 	
 	private static final String SERVER_URL_SETTING = "server_url";
 	private static final String USERNAME_SETTING = "username";
 	private static final String PASSWORD_SETTING = "password";
 	private static final String PLACE_SETTING = "place";
 	
 	private static final int PLACES_DIALOG = 0;
 	private static final int CALENDAR_DIALOG = 1;
 	private static final int ABOUT_DIALOG = 2;
 
 	private static final Object NO_PARAM = null;
 	
 	private SharedPreferences prefferences;
 	private Client client;
 	private Calendar calendar;
 	private List<Place> places;
 	private Place currentPlace;
 	private List<Integer> activeDays; //in current month
 	
 	private ListView reservationsView;
 	private TextView selectedDateView;
 	private ImageButton showCalendarButton;
 	private ImageButton prevDayButton;
 	private ImageButton nextDayButton;
 	private ProgressBar progressBar;
 	private View datePickerView;
 	
 	private CalendarView calendarView;
 	private ProgressBar calendarProgressBar;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		try {
 			VERSION = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionName;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		prefferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		reservationsView = (ListView) findViewById(R.id.reservations_list);
 		showCalendarButton = (ImageButton) findViewById(R.id.show_calendar);
 		selectedDateView = (TextView) findViewById(R.id.selected_date_text);
 		prevDayButton = (ImageButton) findViewById(R.id.prev_day);
 		nextDayButton = (ImageButton) findViewById(R.id.next_day);
 		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
 		datePickerView = findViewById(R.id.date_picker);
 		
 		calendar = Calendar.getInstance();
 		
 		prevDayButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				int step = -1;
 				if (activeDays != null) {
 					int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
 					for (int i : activeDays) {
 						if (i >= currentDay) {
 							break;
 						}
 						step = i-currentDay;
 					}
 					if (!activeDays.contains(currentDay+step)) {
 						// move to last day of previous month
 						calendar.add(Calendar.DAY_OF_MONTH, -currentDay);
 						activeDays = null;
 						new FetchDaysTask().execute(calendar);
 						step = 0;
 					}
 				}
 				calendar.add(Calendar.DAY_OF_MONTH, step);
 				fetchReservations();
 			}
 		});
 
 		nextDayButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				int step = 1;
 				if (activeDays != null) {
 					int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
 					for (int i : activeDays) {
 						if (i > currentDay) {
 							step = i-currentDay;
 							break;
 						}
 					}
 					if (!activeDays.contains(currentDay+step)) {
 						// set to first day of next month
 						calendar.add(Calendar.MONTH, 1);
 						calendar.set(Calendar.DAY_OF_MONTH, 1);
 						activeDays = null;
 						new FetchDaysTask().execute(calendar);
 						step = 0;
 					}
 				}
 				calendar.add(Calendar.DAY_OF_MONTH, step);
 				fetchReservations();
 			}
 		});
 
 		datePickerView.setOnClickListener(new View.OnClickListener() {
 			private Runnable postAction = new Runnable() {
 				
 				@Override
 				public void run() {
 					showCalendar();
 				}
 			};
 			@Override
 			public void onClick(View v) {
 				activeDays = null;
 				new FetchDaysTask(postAction).execute(calendar);
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (client == null) {
 			String url = prefferences.getString(SERVER_URL_SETTING, "");
 			if (url.length() > 7) {
 				client = new Client(url);
 				new LoginTask().execute(NO_PARAM);
 			} else {
 				showMessage(R.string.msg_server_url_not_configured);
 				return;
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 			if (client == null) {
 				showMessage(R.string.msg_server_url_not_configured);
 			} else {
 				if (client.isLoggedIn()) {
 					fetchReservations();
 					new FetchDaysTask().execute(calendar);
 				} else {
 					new LoginTask().execute(NO_PARAM);
 				}
 			}
 			return true;
 		case R.id.menu_select_place:
 			showDialog(PLACES_DIALOG);
 			return true;
 		case R.id.menu_settings:
 			saveState();
 			places = null;
 			activeDays = null;
			if (client != null) {
				client.cancelCurrentRequest();
			}
 			client = null;
 			reservationsView.setAdapter(null);
 			startActivity(new Intent(this, Settings.class));
 			return true;
 		case R.id.menu_about:
 			showDialog(ABOUT_DIALOG);
 			return true;
 		}
 		return false;
 	}
 
 	private void saveState() {
 		if (currentPlace != null) {
 			Editor editor = prefferences.edit();
 			editor.putInt(PLACE_SETTING, currentPlace.getId());
 			editor.commit();
 		}
 	}
 
 	private void fetchReservations() {
 		//if (currentPlace != null && client != null && !client.isExecuting()) {
 		if (currentPlace != null && client != null) {
 			new FetchReservationsTask().execute(NO_PARAM);
 		}
 	}
 
 	@Override
 	public void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		if (client != null) {
 			if (client.isExecuting()) {
 				client.cancelCurrentRequest();
 			}
 			client.logout();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (client != null) {
 			client.logout();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		saveState();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		switch(id) {
 		case PLACES_DIALOG:
 			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (which < places.size()) {
 						setCurrentPlace(places.get(which));
 						fetchReservations();
 					}
 				}
 			};
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(R.string.label_select_place);
 			builder.setItems(new String[0], listener);
 			dialog = builder.create();
 			break;
 		case CALENDAR_DIALOG:
 			dialog = new Dialog(this);
 			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 			View view = getLayoutInflater().inflate(R.layout.calendar, null);
 			calendarView = (CalendarView) view.findViewById(R.id.calendar);
 			calendarProgressBar = (ProgressBar) view.findViewById(R.id.calendar_progress_bar);
 			// override default button actions, so we can move on prev/next month
 			// after getting enabled/disabled days
 			calendarView.setOnPrevMonthListener(new View.OnClickListener() {
 				private Calendar cal = Calendar.getInstance();
 				private Runnable postAction = new Runnable() {
 					@Override
 					public void run() {
 						calendarProgressBar.setVisibility(View.INVISIBLE);
 						calendarView.setPrevViewItem();
 					}
 				};
 				@Override
 				public void onClick(View v) {
 					cal.setTimeInMillis(calendarView.getCurrentMonth().getTimeInMillis());
 					cal.add(Calendar.MONTH, -1);
 					activeDays = null;
 					calendarProgressBar.setVisibility(View.VISIBLE);
 					new FetchDaysTask(postAction).execute(cal);
 				}
 			});
 			calendarView.setOnNextMonthListener(new View.OnClickListener() {
 				private Calendar cal = Calendar.getInstance();
 				private Runnable postAction = new Runnable() {
 					@Override
 					public void run() {
 						calendarProgressBar.setVisibility(View.INVISIBLE);
 						calendarView.setNextViewItem();
 					}
 				};
 				@Override
 				public void onClick(View v) {
 					cal.setTimeInMillis(calendarView.getCurrentMonth().getTimeInMillis());
 					cal.add(Calendar.MONTH, 1);
 					activeDays = null;
 					calendarProgressBar.setVisibility(View.VISIBLE);
 					new FetchDaysTask(postAction).execute(cal);
 				}
 			});
 			if (activeDays != null) {
 				calendarView.setEnabledDays(activeDays);
 			}
 			dialog.setContentView(view);
 			calendarView.setCalendarListener(this);
 			dialog.setOnDismissListener(new Dialog.OnDismissListener() {
 				
 				@Override
 				public void onDismiss(DialogInterface dialog) {
 					activeDays = null;
 					fetchReservations();
 					new FetchDaysTask().execute(calendar);
 				}
 			});
 			break;
 		case ABOUT_DIALOG:
 			dialog = new AboutDialog(this);
 			dialog.setTitle(R.string.label_about);
 			break;
 		}
 		return dialog;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		super.onPrepareDialog(id, dialog);
 		switch (id) {
 		case PLACES_DIALOG: 
 			AlertDialog placesDialog = (AlertDialog) dialog;
 			String[] items = {};
 			int selectedItem = -1;
 			if (places != null) {
 				items = new String[places.size()];
 				for (int i = 0; i < places.size(); i++) {
 					items[i] = places.get(i).getName();
 					if (currentPlace != null && currentPlace.getId() == places.get(i).getId()) {
 						selectedItem = i;
 					}
 				}
 			}
 			ListView list = placesDialog.getListView();
 			list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			
 			placesDialog.getListView().setAdapter(new ArrayAdapter<String>(this, R.layout.simple_list_item_single_choice, items));
 			if (selectedItem != -1) {
 				list.setItemChecked(selectedItem, true);
 			}
 			break;
 		case CALENDAR_DIALOG:
 			calendarView.setSelectedDate(calendar);
 			break;
 		}
 	}
 
 	private void showCalendar() {
 		if (activeDays == null) {
 			new FetchDaysTask(new Runnable() {
 				
 				@Override
 				public void run() {
 					showDialog(CALENDAR_DIALOG);
 				}
 			}).execute(calendar);
 		} else {
 			showDialog(CALENDAR_DIALOG);
 		}
 	}
 	private void setCurrentPlace(Place place) {
 		currentPlace = place;
 		if (currentPlace != null) {
 			setTitle(String.format("%s - %s (%s)", getString(R.string.app_name), currentPlace.getName(), currentPlace.getStreet()));
 		} else {
 			setTitle(getString(R.string.app_name));
 		}
 	}
 
 	private void showMessage(int resid) {
 		Toast.makeText(this, resid, Toast.LENGTH_SHORT).show();
 	}
 
 	public static String readInputStream(InputStream is) throws IOException {
 		String line;
 		StringBuilder content = new StringBuilder();
 		BufferedReader input = new BufferedReader(new InputStreamReader(is), 1024);
 		while ((line = input.readLine()) != null) {
 			content.append(line);
 		}
 		input.close();
 		return content.toString();
 	}
 
 	private class FetchPlacesTask extends AsyncTask<Object, Integer, String> {
 
 		@Override
 		protected void onPreExecute() {
 			progressBar.setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		protected String doInBackground(Object... params) {
 			String content = null;
 			if (client != null) {
 				HttpResponse resp = null;
 				try {
 					resp = client.httpGet("/places/");
 					if (resp.getStatusLine().getStatusCode() < 400) {
 						content = readInputStream(resp.getEntity().getContent());
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					client.closeResponse(resp);
 				}
 			}
 			return content;
 		}
 
 		@Override
 		protected void onPostExecute(String content) {
 			if (content != null) {
 				int lastPlaceId = prefferences.getInt(PLACE_SETTING, 0);
 				places = new ArrayList<Place>();
 				try {
 					JSONArray jsonPlaces = new JSONArray(content);
 					for (int i = 0; i < jsonPlaces.length(); i++) {
 						JSONObject jsonPlace = jsonPlaces.getJSONObject(i);
 						int id = jsonPlace.getInt("id");
 						String name = jsonPlace.getString("name");
 						String street = jsonPlace.getString("street");
 						String city = jsonPlace.getString("city");
 						Place place = new Place(id, name, street, city);
 						places.add(place);
 						if (id == lastPlaceId) {
 							setCurrentPlace(place);
 						}
 					}
 				}  catch (JSONException e) {
 					e.printStackTrace();
 					showMessage(R.string.msg_bad_response_error);
 				}
 				if (currentPlace == null && places.size() > 0) {
 					setCurrentPlace(places.get(0));
 				}
 				//fetchReservations();
 				//new FetchDaysTask().execute(calendar);
 				showCalendar();
 			} else {
 				showMessage(R.string.msg_http_error);
 			}
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 	}
 
 	private static SimpleDateFormat labelDateFormat = new SimpleDateFormat("d MMMM yyyy");
 	private static SimpleDateFormat requestDateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	
 	private class FetchReservationsTask extends AsyncTask<Object, Integer, String> {
 
 		@Override
 		protected void onPreExecute() {
 			selectedDateView.setText(labelDateFormat.format(calendar.getTime()));
 			progressBar.setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		protected String doInBackground(Object ... params) {
 			String content = null;
 			if (client != null && currentPlace != null) {
 				String date = requestDateFormat.format(calendar.getTime());
 				HttpResponse resp = null;
 				try {
 					resp = client.httpGet("/reservations/"+date+"/"+currentPlace.getId()+"/");
 					if (resp.getStatusLine().getStatusCode() < 400) {
 						content = readInputStream(resp.getEntity().getContent());
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					client.closeResponse(resp);
 				}
 			}
 			return content;
 		}
 
 		@Override
 		protected void onPostExecute(String content) {
 			progressBar.setVisibility(View.INVISIBLE);
 			List<Reservation> reservations = new ArrayList<Reservation>();
 			if (content != null) {
 				try {
 					JSONArray array = new JSONArray(content);
 					for (int i = 0; i < array.length(); i++) {
 						JSONObject reservation = array.getJSONObject(i);
 						int status = reservation.getInt("status");
 						String time = reservation.getString("time");
 						String patient = reservation.getString("patient");
 						String patientPhone = reservation.getString("phone_number");
 						String patientEmail = reservation.getString("email");
 						reservations.add(new Reservation(time, Reservation.Status.valueOf(status), patient, patientPhone, patientEmail));
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 					showMessage(R.string.msg_bad_response_error);
 				}
 			} else {
 				showMessage(R.string.msg_http_error);
 			}
 			reservationsView.setAdapter(new ReservationAdapter(Medobs.this, reservations));
 		}
 	}
 
 	private class FetchDaysTask extends AsyncTask<Calendar, Integer, String> {
 
 		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM");
 		private Runnable postAction;
 
 		public FetchDaysTask() {}
 		
 		public FetchDaysTask(Runnable postAction) {
 			this.postAction = postAction;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			progressBar.setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		protected String doInBackground(Calendar... params) {
 			String content = null;
 			if (client != null && currentPlace != null) {
 				HttpResponse resp = null;
 				String date = dateFormat.format(params[0].getTime());
 				try {
 					resp = client.httpGet("/days_status/"+date+"/"+currentPlace.getId()+"/");
 					if (resp != null && resp.getStatusLine().getStatusCode() < 400) {
 						content = readInputStream(resp.getEntity().getContent());
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					client.closeResponse(resp);
 				}
 			}
 			return content;
 		}
 
 		@Override
 		protected void onPostExecute(String content) {
 			progressBar.setVisibility(View.INVISIBLE);
 			if (content != null) {
 				try {
 					JSONObject data = new JSONObject(content);
 					Iterator<String> it = data.keys();
 					List<Integer> enabledDays = new ArrayList<Integer>();
 					while (it.hasNext()) {
 						String date = it.next();
 						boolean enabled = data.getBoolean(date);
 						if (enabled) {
 							enabledDays.add(Integer.parseInt(date.substring(date.lastIndexOf('-')+1)));
 						}
 					}
 					Collections.sort(enabledDays);
 					activeDays = enabledDays;
 					if (calendarView != null) {
 						calendarView.setEnabledDays(activeDays);
 					}
 					if (postAction != null) {
 						postAction.run();
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 					showMessage(R.string.msg_bad_response_error);
 				}
 			} else {
 				showMessage(R.string.msg_http_error);
 			}
 		}
 	}
 
 	class LoginTask extends AsyncTask<Object, Integer, Boolean> {
 		private String username;
 		private String password;
 
 		@Override
 		protected void onPreExecute() {
 			progressBar.setVisibility(View.VISIBLE);
 			username = prefferences.getString(USERNAME_SETTING, "");
 			password = prefferences.getString(PASSWORD_SETTING, "");
 		}
 
 		@Override
 		protected Boolean doInBackground(Object... params) {
 			return client.login(username, password);
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			progressBar.setVisibility(View.INVISIBLE);
 			if (result) {
 				new FetchPlacesTask().execute(NO_PARAM);
 			} else {
 				showMessage(R.string.msg_login_failed);
 			}
 		}
 	}
 
 	@Override
 	public void onMonthChanged(CalendarView calendarView, Calendar value) {
 		activeDays = null;
 		new FetchDaysTask().execute(value);
 	}
 
 	@Override
 	public void onDateSelected(CalendarView calendarView) {
 		dismissDialog(CALENDAR_DIALOG);
 		calendar = calendarView.getSelectedValue();
 		activeDays = null;
 		fetchReservations();
 		new FetchDaysTask().execute(calendar);
 	}
 }
