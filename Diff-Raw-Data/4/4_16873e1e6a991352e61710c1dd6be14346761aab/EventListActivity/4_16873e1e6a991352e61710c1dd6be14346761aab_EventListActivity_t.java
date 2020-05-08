 package uk.ac.dur.duchess.activity;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import uk.ac.dur.duchess.EventListAdapter;
 import uk.ac.dur.duchess.R;
 import uk.ac.dur.duchess.data.CalendarFunctions;
 import uk.ac.dur.duchess.data.NetworkFunctions;
 import uk.ac.dur.duchess.data.SessionFunctions;
 import uk.ac.dur.duchess.data.UserFunctions;
 import uk.ac.dur.duchess.entity.Event;
 import uk.ac.dur.duchess.entity.EventXMLParser;
 import uk.ac.dur.duchess.entity.User;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.DatePicker;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.ListView;
 
 public class EventListActivity extends ListActivity
 {
 	private ProgressDialog progressDialog;
 	private boolean featureMode = true;
 
 	private User currentUser;
 	private Activity activity;
 
 	private ImageView adImageContainer;
 	private ListView listView;
 	private Event currentAd;
 
 	private ArrayList<Event> eventList;
 	private String categoryFilter;
 	private EventListAdapter adapter;
 
 	private static final int REQUEST_DATEFRAME = 1;
 	private static final int REQUEST_CALENDAR = 2;
 	private static final int DATE_DIALOG_ID = 1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		currentUser = SessionFunctions.getCurrentUser(this);
 		activity = this;
 		listView = getListView();
 
 		Bundle extras = getIntent().getExtras();
 
 		if (extras != null) categoryFilter = extras.getString("category_filter");
 
 		adImageContainer = (ImageView) findViewById(R.id.adImageContainer);
 
 		if (currentUser != null)
 		{
 			setTitle("Duchess - Hello, " + currentUser.getForename() + " "
 					+ currentUser.getSurname());
 		}
 		else
 		{
 			setTitle("Duchess - Guest");
 		}
 
 		try
 		{
 
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			final XMLReader reader = parser.getXMLReader();
 
 			final URL url;
 			if (categoryFilter == null)
 			{
 				url = new URL("http://www.dur.ac.uk/cs.seg01/duchess/api/v1/events.php");
 			}
 			else
 			{
 				url = new URL("http://www.dur.ac.uk/cs.seg01/duchess/api/v1/events.php?category="
 						+ categoryFilter);
 			}
 
 			eventList = new ArrayList<Event>();
 
 			EventXMLParser myXMLHandler = new EventXMLParser(eventList);
 
 			reader.setContentHandler(myXMLHandler);
 
 			adapter = new EventListAdapter(this, R.layout.custom_event_list_row, eventList);
 			setListAdapter(adapter);
 
 			getListView().setOnItemClickListener(new OnItemClickListener()
 			{
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 				{
 
 					Intent i = new Intent(view.getContext(), EventDetailsTabRootActivity.class);
 					Event e = (Event) getListAdapter().getItem(position);
 					i.putExtra("event_id", e.getEventID());
 					i.putExtra("event_name", e.getName());
 					i.putExtra("event_start_date", e.getStartDate());
 					i.putExtra("event_end_date", e.getEndDate());
 					i.putExtra("event_description", e.getDescriptionHeader());
 					i.putExtra("event_contact_telephone_number", e.getContactTelephoneNumber());
 					i.putExtra("event_contact_email_address", e.getContactEmailAddress());
 					i.putExtra("event_web_address", e.getWebAddress());
 					i.putExtra("event_address1", e.getAddress1());
 					i.putExtra("event_address2", e.getAddress2());
 					i.putExtra("event_city", e.getCity());
 					i.putExtra("event_postcode", e.getPostcode());
 					i.putExtra("event_latitude", e.getLatitude());
 					i.putExtra("event_longitude", e.getLongitude());
 					i.putExtra("image_url", e.getImageURL());
 					startActivity(i);
 				}
 			});
 
 			final Runnable callbackFunction = new Runnable()
 			{
 
 				@Override
 				public void run()
 				{
 					progressDialog.dismiss();
 					adapter.notifyDataSetChanged();
 
 					for (Event e : eventList)
 					{
 						if (e.isFeatured() && e.getAdImageURL() != null)
 						{
 							currentAd = e;
 							Log.d("Download AD", e.getAdImageURL());
 							adImageContainer.setAdjustViewBounds(true);
 							adImageContainer.setScaleType(ScaleType.CENTER_CROP);
 							DisplayMetrics displaymetrics = new DisplayMetrics();
 							getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 							int height = displaymetrics.heightPixels;
 							int width = displaymetrics.widthPixels;
 							adImageContainer.setMinimumWidth(width);
 							adImageContainer.setMinimumHeight((int) (width / 3.0));
 							adImageContainer.setImageBitmap(NetworkFunctions.downloadImage(e
 									.getAdImageURL()));
 							adImageContainer.invalidate();
 							break;
 						}
 					}
 				}
 			};
 
 			Runnable parseData = new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					try
 					{
 						InputStream is = url.openStream();
 						InputSource source = new InputSource(is);
 						source.setEncoding("UTF-8");
 						reader.parse(source);
 						if (currentUser != null)
 						{
 							Log.d("BEFORE FILTER", "" + eventList.size());
 							UserFunctions.filterByPreferences(currentUser, eventList);
 							Log.d("AFTER FILTER", "" + eventList.size());
 						}
 						runOnUiThread(callbackFunction);
 					}
 					catch (Exception ex)
 					{
 						ex.printStackTrace();
 					}
 				}
 			};
 
 			Thread thread = new Thread(null, parseData, "SAXParser");
 			thread.start();
 			progressDialog = ProgressDialog.show(EventListActivity.this, "Please wait...",
 					"Downloading Events ...", true);
 
 			adImageContainer.setClickable(true);
 			adImageContainer.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					Intent i = new Intent(v.getContext(), EventDetailsTabRootActivity.class);
 					Event e = currentAd;
 					i.putExtra("event_id", e.getEventID());
 					i.putExtra("event_name", e.getName());
 					i.putExtra("event_start_date", e.getStartDate());
 					i.putExtra("event_end_date", e.getEndDate());
 					i.putExtra("event_description", e.getDescriptionHeader());
 					i.putExtra("event_contact_telephone_number", e.getContactTelephoneNumber());
 					i.putExtra("event_contact_email_address", e.getContactEmailAddress());
 					i.putExtra("event_web_address", e.getWebAddress());
 					i.putExtra("event_address1", e.getAddress1());
 					i.putExtra("event_address2", e.getAddress2());
 					i.putExtra("event_city", e.getCity());
 					i.putExtra("event_postcode", e.getPostcode());
 					i.putExtra("event_latitude", e.getLatitude());
 					i.putExtra("event_longitude", e.getLongitude());
 					i.putExtra("image_url", e.getImageURL());
 					startActivity(i);
 				}
 			});
 
 		}
 		catch (Exception e)
 		{
 			// TODO handle error
 			e.printStackTrace();
 		}
 
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.event_list_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 		case R.id.submenuEventListFeatured:
 			filterEventByFeatured();
 			return true;
 		case R.id.submenuEventListByDate:
 		{
 			showDialog(DATE_DIALOG_ID);
 			return true;
 		}
 		case R.id.submenuEventListByDateRange:
 		{
 			Intent i = new Intent(this, DateFrameActivity.class);
 			startActivityForResult(i, REQUEST_DATEFRAME);
 			return true;
 		}
 		case R.id.submenuEventListByCalendar:
 		{
 			Intent i = new Intent(this, CalendarActivity.class);
 			startActivityForResult(i, REQUEST_CALENDAR);
 			return true;
 		}
 		case R.id.submenuEventListAZ:
 			sortEventsAlphabetically();
 			return true;
 		case R.id.submenuEventListChronological:
 			sortEventsChronologically();
 			return true;
 		case R.id.menuCategoryBrowse:
 			Intent i = new Intent(this, CategoryGridActivity.class);
 			startActivity(i);
 			return true;
 		default:
 			return true;
 		}
 	}
 
 	private void filterEventByFeatured()
 	{
 		if (featureMode)
 		{
 			ArrayList<Event> featuredEvents = new ArrayList<Event>();
 			for (Event event : eventList)
 			{
 				if (event.isFeatured()) featuredEvents.add(event);
 			}
 			setListAdapter(new EventListAdapter(this, R.layout.custom_event_list_row,
 					featuredEvents));
 			featureMode = false;
 		}
 		else
 		{
 			setListAdapter(adapter);
 			featureMode = true;
 		}
 	}
 
 	private void filterEventByDateRange(String fromDate, String toDate)
 	{
 		ArrayList<Event> inRangeEvents = new ArrayList<Event>();
 
 		for (Event event : eventList)
 		{
 			if (CalendarFunctions.inRange(event.getStartDate(), event.getEndDate(), fromDate,
 					toDate)) inRangeEvents.add(event);
 		}
 
 		setListAdapter(new EventListAdapter(this, R.layout.custom_event_list_row,
 				inRangeEvents));
 	}
 
 	private void sortEventsAlphabetically()
 	{
 		Collections.sort(eventList, new Comparator<Event>()
 		{
 
 			@Override
 			public int compare(Event obj1, Event obj2)
 			{
 				return obj1.getName().compareTo(obj2.getName());
 			}
 		});
 		adapter.notifyDataSetChanged();
 	}
 
 	private void sortEventsChronologically()
 	{
 		Collections.sort(eventList, new Comparator<Event>()
 		{
 			@Override
 			public int compare(Event e1, Event e2)
 			{
 				String sDateStr = e1.getStartDate();
 				String tDateStr = e2.getStartDate();
 
 				return CalendarFunctions.compareDates(sDateStr, tDateStr);
 			}
 
 		});
 		adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int responseCode, Intent data)
 	{
 		switch (requestCode)
 		{
 		case REQUEST_DATEFRAME:
 		{
 			if (responseCode == RESULT_OK)
 			{
 				String fromDate = data.getStringExtra("from_date");
 				String toDate = data.getStringExtra("to_date");
 
 				filterEventByDateRange(fromDate, toDate);
 			}
 			break;
 		}
 		case REQUEST_CALENDAR:
 		{
 			if(responseCode == RESULT_OK)
 			{
 				String dates = data.getStringExtra("dates");
 				Log.d("CALENDAR", dates);
 				
 				String[] dateArray = dates.split(", ");
 				
 				ArrayList<Event> inRangeEvents = new ArrayList<Event>();
 				
 				for (Event event : eventList)
 				{
 					for(String date : dateArray)
 					{
 						String[] values = date.split("-");
 						String toDate = values[0] + "-" + values[1] + "-" + (Integer.parseInt(values[2]) + 1);
 						
 						if(CalendarFunctions.inRange(event.getStartDate(), event.getEndDate(), date, toDate))
 						{
 							inRangeEvents.add(event);
 							break;
 						}
 					}
 				}
 				
 				setListAdapter(new EventListAdapter(this, R.layout.custom_event_list_row,
 						inRangeEvents));
 			}
 			break;
 		}
 
 		default:
 			break;
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		switch (id)
 		{
 			case DATE_DIALOG_ID:
 			{
 				Calendar c = Calendar.getInstance();
 				int year = c.get(Calendar.YEAR);
 				int month = c.get(Calendar.MONTH);
 				int day = c.get(Calendar.DATE);
 	
 				return new DatePickerDialog(this, dateSetListener, year, month, day);
 			}
 		}
 		return null;
 	}
 
 	private OnDateSetListener dateSetListener = new OnDateSetListener()
 	{
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
 		{
			String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
			String nextDay = year + "-" + (monthOfYear + 1) + "-" + (dayOfMonth + 1);
 
 			filterEventByDateRange(date, nextDay);
 		}
 	};
 }
