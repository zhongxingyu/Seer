 package edu.berkeley.cs160.billiterate;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.berkeley.cs160.billiterate.R.drawable;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.text.SpannableString;
 import android.text.style.UnderlineSpan;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.TabListener {
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * current tab position.
 	 */
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 	private Fragment agendaFragment;
 	private Fragment trendingFragment;
 	private Fragment representativeFragment;
 
 	SharedPreferences mPrefs;
 	final String welcomeScreenShownPref = "welcomeScreenShown";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Set up welcome screen
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		// second argument is the default to use if the preference can't be
 		// found
 		Boolean welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref,
 				false);
 
 		if (!welcomeScreenShown) {
 			// here you can launch another activity if you like
 			// the code below will display a popup
 			Intent intent = new Intent(this, TabActivity.class);
 			startActivity(intent);
 
 			/*
 			 * String welcomeTitle =
 			 * getResources().getString(R.string.welcomeTitle); String
 			 * welcomeText = getResources().getString(R.string.welcomeText); new
 			 * AlertDialog
 			 * .Builder(this).setIcon(android.R.drawable.ic_dialog_alert
 			 * ).setTitle
 			 * (welcomeTitle).setMessage(welcomeText).setPositiveButton(
 			 * R.string.ok, new DialogInterface.OnClickListener() { public void
 			 * onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
 			 * }).show();
 			 */
 			SharedPreferences.Editor editor = mPrefs.edit();
 			editor.putBoolean(welcomeScreenShownPref, true);
 			editor.commit(); // Very important to save the preference
 		}
 
 		this.agendaFragment = new AgendaFragment();
 		this.trendingFragment = new TrendingFragment();
 		this.representativeFragment = new RepresentativeActivity.AsFragment();
 
 		// Set up the action bar to show tabs.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// For each of the sections in the app, add a tab to the action bar.
 		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
 				.setTabListener(this));
 		actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
 				.setTabListener(this));
 		actionBar.addTab(actionBar.newTab().setText(R.string.title_section3)
 				.setTabListener(this));
 
 		// TEMPORARY for testing
 		/*
 		 * Intent i = new Intent(this, RepresentativeActivity.class);
 		 * i.putExtra("representative", "Linda Maio"); startActivity(i);
 		 */
 
 		/*
 		 * Intent i = new Intent(this, BillInfoActivity.class);
 		 * i.putExtra("title",
 		 * "Zoning Amendments to Allow Later Hours of Operation on Telegraph Avenue; BMC Section 23E.56.060"
 		 * ); startActivity(i);
 		 */
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current tab position.
 		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current tab position.
 		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
 				.getSelectedNavigationIndex());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.menu_help:
 	            Intent intent = new Intent(this, TabActivity.class);
 	            startActivity(intent);
 	            return true;
 	        case R.id.menu_search:
 	        	Intent search = new Intent(this, SearchActivity.class);
 	        	startActivity(search);
 	        	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, show the tab contents in the
 		// container view.
 		if (tab.getPosition() == 0) {
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.container, agendaFragment).commit();
 			return;
 		} else if (tab.getPosition() == 1) {
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.container, trendingFragment).commit();
 			return;
 		} else if (tab.getPosition() == 2) {
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.container, representativeFragment).commit();
 			return;
 		}
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	/*
 	 * public void onBillClick(View view) { Intent intent = new Intent(this,
 	 * BillInfoActivity.class); intent.putExtra("title", ((TextView)
 	 * view).getText()); startActivity(intent); }
 	 */
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public DummySectionFragment() {
 		}
 	}
 
 	public static class AgendaFragment extends Fragment {
 
 		ArrayList<Meeting> meetingsList = new ArrayList<Meeting>();
 		LinearLayout ll;
 		View agenda2;
 
 		public AgendaFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View agenda = inflater.inflate(R.layout.agenda_layout, container,
 					false);
 			agenda2 = agenda;
 			ll = (LinearLayout) agenda.findViewById(R.id.agenda_layout);
 			LoadAgendaTask load_agenda = new LoadAgendaTask();
 			load_agenda.execute();
 			System.out.println("executed loading agenda");
 			return agenda;
 		}
 
 		public void setAgendaView(LinearLayout ll) {
 			ll.removeAllViews();
 
 			String date = "";
 			System.out
 					.println("There should be two meetings listed, and there is actually: "
 							+ meetingsList.size());
 			for (final Meeting mt : meetingsList) {
 				if (!(mt.date.equals(date))) {
 					System.out.println("setting agenda there is a new date!");
 					TextView dateHeading = new TextView(getActivity());
 					dateHeading.setLayoutParams(new LayoutParams(
 							LayoutParams.MATCH_PARENT,
 							LayoutParams.WRAP_CONTENT));
 					dateHeading.setPadding(10, 10, 10, 10);
 					dateHeading.setGravity(Gravity.CENTER);
 					dateHeading.setBackgroundResource(R.drawable.border);
 					dateHeading.setBackgroundColor(Color.BLACK);
 					dateHeading.setTextColor(Color.CYAN);
 					dateHeading.setText(mt.date);
 					dateHeading.setTextSize(25);
 					date = mt.date;
 					ll.addView(dateHeading);
 				}
 
 				View agendaItem = LayoutInflater.from(getActivity()).inflate(
 						R.layout.agenda_item, ll, false);
 				final LinearLayout billsList = (LinearLayout) agendaItem
 						.findViewById(R.id.expandable);
 
 				TextView heading = (TextView) agendaItem
 						.findViewById(R.id.header);
 				final ImageView arrow = (ImageView) agendaItem
 						.findViewById(R.id.accordion_arrow);
 				arrow.setId(1);
 
 				heading.setText(mt.type + " " + mt.time);
 				heading.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						arrow.setImageResource(billsList.getVisibility() == View.GONE ? R.drawable.arrowhead_down
 								: R.drawable.arrowhead_right);
 						billsList
 								.setVisibility(billsList.getVisibility() == View.GONE ? View.VISIBLE
 										: View.GONE);
 						arrow.invalidate();
 					}
 				});
 
 				TextView loc = (TextView) agendaItem
 						.findViewById(R.id.location);
 				loc.setText(mt.location);
 				loc.setTextSize(20);
 
 				Button findLocationButton = (Button) agendaItem
 						.findViewById(R.id.findLocation);
 				findLocationButton.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						String uri = "geo:0,0?q=" + mt.location
 								+ ", Berkeley, CA";
 						uri = uri.replace(" ", "+");
 						Intent intent = new Intent(
 								android.content.Intent.ACTION_VIEW, Uri
 										.parse(uri));
 						startActivity(intent);
 					}
 				});
 
 				ll.addView(agendaItem);
 
 				LoadBillsTask load_bills = new LoadBillsTask(this.getActivity()
 						.getApplicationContext(), billsList);
 				load_bills.execute(mt.pk);
 			}
 		}
 
 		/**
 		 * Takes a string of format year-month-day and returns string of format
 		 * month day, year
 		 * 
 		 * @param date
 		 * @return
 		 */
 		private String convertDate(String date) {
 			SimpleDateFormat inFormat = new SimpleDateFormat(
 					"yyyy-MM-dd h:mm:ss", Locale.US);
 			Calendar calendar = new GregorianCalendar();
 			try {
 				calendar.setTime(inFormat.parse(date));
 			} catch (ParseException e) {
 				throw new RuntimeException(e);
 			}
 
 			SimpleDateFormat outFormat = new SimpleDateFormat("MMMMM d, yyyy",
 					Locale.US);
 			return outFormat.format(calendar.getTime());
 		}
 
 		private String convertTime(String date) {
 			SimpleDateFormat inFormat = new SimpleDateFormat(
 					"yyyy-MM-dd h:mm:ss", Locale.US);
 			Calendar calendar = new GregorianCalendar();
 			try {
 				calendar.setTime(inFormat.parse(date));
 			} catch (ParseException e) {
 				throw new RuntimeException(e);
 			}
 
 			SimpleDateFormat outFormat = new SimpleDateFormat("h:mm a",
 					Locale.US);
 			return outFormat.format(calendar.getTime());
 		}
 
 		private class LoadAgendaTask extends AsyncTask<Void, Void, JSONArray> {
 
 			@Override
 			protected JSONArray doInBackground(Void... arg0) {
 				if (meetingsList.size() > 0)
 					return null;
 
 				String url = "http://billiterate.pythonanywhere.com/billapp/agendas";
 				System.err.println("URL = " + url);
 				HttpResponse response;
 				HttpClient client = new DefaultHttpClient();
 				String responseString = "";
 
 				try {
 					response = client.execute(new HttpGet(url));
 					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 						ByteArrayOutputStream out = new ByteArrayOutputStream();
 						response.getEntity().writeTo(out);
 						out.close();
 						responseString = out.toString();
 					} else {
 						response.getEntity().getContent().close();
 					}
 				} catch (ClientProtocolException cpe) {
 					cpe.printStackTrace();
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				}
 				try {
 					JSONArray messages = new JSONArray(responseString);
 					return messages;
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			protected void onPostExecute(JSONArray messageList) {
 				if (messageList == null && meetingsList.size() == 0) {
 					System.err
 							.println("Something went wrong with loading agenda");
 					return;
 				}
 
 				if (messageList == null) {
 					setAgendaView(ll);
 					return;
 				}
 
 				for (int i = 0; i < messageList.length(); i++) {
 					int pk = 0;
 					String date = "";
 					String time = "";
 					String type = "";
 					String location = "";
 					try {
 						JSONObject current = messageList.getJSONObject(i);
 						pk = current.getInt("pk");
 						JSONObject fields = current.getJSONObject("fields");
 						String date_time = fields.getString("date_time");
 						date = convertDate(date_time);
 						time = convertTime(date_time);
 						type = fields.getString("type");
 						location = fields.getString("location");
 					} catch (JSONException e) {
 						System.err.println(messageList.toString());
 						e.printStackTrace();
 					}
 					Meeting mt = new Meeting(pk, date, time, type, location);
 					System.err.println("Adding new meeting to list");
 					meetingsList.add(mt);
 					System.err
 							.println("After adding meeting to list, there are "
 									+ meetingsList.size() + " meetings!");
 				}
 				if (ll == null) {
 					ll = (LinearLayout) agenda2
 							.findViewById(R.id.agenda_layout);
 				}
 				setAgendaView(ll);
 
 			}
 
 		}
 
 		private class BillClickListener implements OnClickListener {
 
 			Context context;
 			String title;
 			String summary;
 			String rep;
 			int id;
 
 			public BillClickListener(Context context, String title,
 					String summary, String rep, int id) {
 				this.context = context;
 				this.title = title;
 				this.summary = summary;
 				this.rep = rep;
 				this.id = id;
 			}
 
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(this.context, BillInfoActivity.class);
 				i.putExtra("title", this.title);
 				i.putExtra("summary", this.summary);
 				i.putExtra("representative", this.rep);
 				i.putExtra("id", this.id);
 				startActivity(i);
 			}
 
 		}
 
 		private class LoadBillsTask extends AsyncTask<Integer, Void, JSONArray> {
 
 			Context context;
 			LinearLayout bills_layout;
 
 			public LoadBillsTask(Context context, LinearLayout ll) {
 				this.context = context;
 				this.bills_layout = ll;
 			}
 
 			@Override
 			protected JSONArray doInBackground(Integer... params) {
 				String url = "http://billiterate.pythonanywhere.com/billapp/agenda_bills?id="
 						+ params[0];
 				System.err.println("URL = " + url);
 				HttpResponse response;
 				HttpClient client = new DefaultHttpClient();
 				String responseString = "";
 
 				try {
 					response = client.execute(new HttpGet(url));
 					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 						ByteArrayOutputStream out = new ByteArrayOutputStream();
 						response.getEntity().writeTo(out);
 						out.close();
 						responseString = out.toString();
 					} else {
 						response.getEntity().getContent().close();
 					}
 				} catch (ClientProtocolException cpe) {
 					cpe.printStackTrace();
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				}
 				try {
 					JSONArray messages = new JSONArray(responseString);
 					return messages;
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			protected void onPostExecute(JSONArray messageList) {
 				if (messageList == null) {
 					System.err
 							.println("something went wrong with getting the bills");
 					return;
 				}
 
 				for (int i = 0; i < messageList.length(); i++) {
 					String title = "";
 					String summary = "";
 					String representative = "";
 					int id = 0;
 					try {
 						JSONObject current = messageList.getJSONObject(i);
 						id = current.getInt("pk");
 						JSONObject fields = current.getJSONObject("fields");
 						title = fields.getString("title");
 						summary = fields.getString("summary");
 						representative = fields.getString("representative");
 					} catch (JSONException e) {
 						System.err.print(messageList.toString());
 						e.printStackTrace();
 					}
 					TextView bill = new TextView(this.context);
 					bill.setLayoutParams(new LayoutParams(
 							LayoutParams.MATCH_PARENT,
 							LayoutParams.WRAP_CONTENT));
 					bill.setPadding(20, 10, 20, 10);
 					bill.setBackgroundResource(R.drawable.border);
 					bill.setGravity(Gravity.CENTER_VERTICAL);
 					SpannableString u_title = new SpannableString(title);
 					u_title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
 					bill.setText(u_title);
 					bill.setTextColor(Color.BLACK);
 					bill.setTextSize(18);
 					bill.setClickable(true);
 					bill.setOnClickListener(new BillClickListener(this.context,
 							title, summary, representative, id));
 					bills_layout.addView(bill);
 					View bar = new View(this.context);
 					bar.setLayoutParams(new LayoutParams(
 							LayoutParams.MATCH_PARENT, 1));
 					bar.setBackgroundColor(Color.DKGRAY);
 					bills_layout.addView(bar);
 				}
 			}
 
 		}
 	}
 
 	public static class TrendingFragment extends Fragment {
 		public TrendingFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View ret = inflater.inflate(R.layout.trending_layout, container,
 					false);
 			LoadTrendingTask task = new LoadTrendingTask(this.getActivity()
 					.getApplicationContext());
 			task.execute();
 			return ret;
 		}
 
 		@Override
 		public void onActivityCreated(Bundle savedInstanceState) {
 			super.onActivityCreated(savedInstanceState);
 		}
 
 		@Override
 		public void onSaveInstanceState(Bundle outState) {
 			super.onSaveInstanceState(outState);
 		}
 
 		private class BillClickListener implements OnClickListener {
 
 			Context context;
 			String title;
 			String summary;
 			String rep;
 			int id;
 
 			public BillClickListener(Context context, String title,
 					String summary, String rep, int id) {
 				this.context = context;
 				this.title = title;
 				this.summary = summary;
 				this.rep = rep;
 				this.id = id;
 			}
 
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(this.context, BillInfoActivity.class);
 				i.putExtra("title", this.title);
 				i.putExtra("summary", this.summary);
 				i.putExtra("representative", this.rep);
 				i.putExtra("id", this.id);
 				startActivity(i);
 			}
 
 		}
 
 		private class LoadTrendingTask extends AsyncTask<Void, Void, JSONArray> {
 			/*
 			 * protected void onPreExecute() {
 			 * getView().findViewById(R.id.trending_progress
 			 * ).setVisibility(View.VISIBLE); }
 			 */
 			Context context;
 
 			public LoadTrendingTask(Context context) {
 				this.context = context;
 			}
 
 			protected JSONArray doInBackground(Void... arg0) {
 				String url = "http://billiterate.pythonanywhere.com/billapp/trending";
 				System.err.println("URL = " + url);
 				HttpResponse response;
 				HttpClient client = new DefaultHttpClient();
 				String responseString = "";
 
 				try {
 					response = client.execute(new HttpGet(url));
 					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 						ByteArrayOutputStream out = new ByteArrayOutputStream();
 						response.getEntity().writeTo(out);
 						out.close();
 						responseString = out.toString();
 					} else {
 						response.getEntity().getContent().close();
 					}
 				} catch (ClientProtocolException cpe) {
 					cpe.printStackTrace();
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				}
 				try {
 					JSONArray messages = new JSONArray(responseString);
 					return messages;
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			protected void onPostExecute(JSONArray messageList) {
 				getView().findViewById(R.id.trending_progress).setVisibility(
 						View.GONE);
				LinearLayout trendingItems = (LinearLayout) getView()
						.findViewById(R.id.trendingItems);
 				trendingItems.removeAllViews();
 				LayoutInflater inflater = LayoutInflater.from(getActivity());
 				if (messageList == null) {
 					View newItem = inflater.inflate(R.layout.trending_item,
 							trendingItems, false);
 					((TextView) newItem.findViewById(R.id.name))
 							.setText("There was an error");
 					trendingItems.addView(newItem);
 					return;
 				}
 
				for (int i = 0; i < messageList.length() && i < 5; i++) {
 					String title = "";
 					String summary = "";
 					String rep = "";
 					int numLikes = 0;
 					int numDislikes = 0;
 					int id = 0;
 					try {
 						JSONObject current = messageList.getJSONObject(i);
 						id = current.getInt("pk");
 						JSONObject fields = current.getJSONObject("fields");
 						title = fields.getString("title");
 						summary = fields.getString("summary");
 						rep = fields.getString("representative");
 						numLikes = fields.getInt("num_likes");
 						numDislikes = fields.getInt("num_dislikes");
 					} catch (JSONException e) {
 						System.err.print(messageList.toString());
 						e.printStackTrace();
 					}
 					View newItem = inflater.inflate(R.layout.trending_item,
 							trendingItems, false);
 					SpannableString ssTitle = new SpannableString(title);
 					ssTitle.setSpan(new UnderlineSpan(), 0, ssTitle.length(), 0);
 					((TextView) newItem.findViewById(R.id.name))
 							.setText(ssTitle);
 					((TextView) newItem.findViewById(R.id.likes))
 							.setText("Likes: " + numLikes);
 					((TextView) newItem.findViewById(R.id.dislikes))
 							.setText("Dislikes: " + numDislikes);
 					newItem.setOnClickListener(new BillClickListener(
 							this.context, title, summary, rep, id));
 					trendingItems.addView(newItem);
 				}
 			}
 		}
 	}
 
 }
