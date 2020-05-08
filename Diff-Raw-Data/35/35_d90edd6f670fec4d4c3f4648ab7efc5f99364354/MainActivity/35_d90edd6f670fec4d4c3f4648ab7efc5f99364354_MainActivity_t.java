 package uk.ac.dur.duchess;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import uk.ac.dur.duchess.data.CalendarFunctions;
 import uk.ac.dur.duchess.data.NetworkFunctions;
 import uk.ac.dur.duchess.data.SessionFunctions;
 import uk.ac.dur.duchess.data.UserFunctions;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.ListView;
 
 public class MainActivity extends ListActivity
 {
 
 	private ProgressDialog progressDialog;
 	private boolean featureMode = true;
 	private Button alphabeticalSortButton;
 	private Button chronologicalSortButton;
 	private Button featuredFilterButton;
 	private Button categoryGridButton;
 	private Button loginButton;
 	private Button settingsButton;
 
 	private User currentUser;
 	private Activity activity;
 	
 	private ImageView adImageContainer;
 	private ListView listView;
 	private Event currentAd;
 	
 	private ArrayList<Event> eventList;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		currentUser = SessionFunctions.getCurrentUser(this);
 		activity = this;
 		listView = getListView();
 
 		alphabeticalSortButton = (Button) findViewById(R.id.alphabeticalSortButton);
 		chronologicalSortButton = (Button) findViewById(R.id.chronologicalSortButton);
 		featuredFilterButton = (Button) findViewById(R.id.featureFilterButton);
 		categoryGridButton = (Button) findViewById(R.id.categoryGridButton);
 		loginButton = (Button) findViewById(R.id.loginButton);
 		settingsButton = (Button) findViewById(R.id.settingsButton);
 		
 		adImageContainer = (ImageView) findViewById(R.id.adImageContainer);
 
 		if (currentUser != null)
 		{
 			setTitle("Duchess - Hello, " + currentUser.getForename() + " "
 					+ currentUser.getSurname());
 		}
 		else
 		{
 			setTitle("Duchess - Guest");
 			settingsButton.setVisibility(View.GONE);
 		}
 
 		try
 		{
 
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			final XMLReader reader = parser.getXMLReader();
 
 			final URL url = new URL("http://www.dur.ac.uk/cs.seg01/duchess/api/v1/events.php");
 
 			eventList = new ArrayList<Event>();
 
 			EventXMLParser myXMLHandler = new EventXMLParser(eventList);
 
 			reader.setContentHandler(myXMLHandler);
 
 			final EventListAdapter adapter = new EventListAdapter(this,
 					R.layout.custom_event_list_row, eventList);
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
 					
 					for (Event e: eventList)
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
 							adImageContainer.setMinimumHeight((int) (width/3.0));
 							adImageContainer.setImageBitmap(NetworkFunctions.downloadImage(e.getAdImageURL()));
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
 							Log.d("BEFORE FILTER", ""+eventList.size());
 							UserFunctions.filterByPreferences(currentUser, eventList);
 							Log.d("AFTER FILTER", ""+eventList.size());
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
 			progressDialog = ProgressDialog.show(MainActivity.this, "Please wait...",
 					"Downloading Events ...", true);
 
 			alphabeticalSortButton.setOnClickListener(new View.OnClickListener()
 			{
 
 				@Override
 				public void onClick(View v)
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
 			});
 
 			chronologicalSortButton.setOnClickListener(new View.OnClickListener()
 			{
 
 				@Override
 				public void onClick(View v)
 				{
 					Collections.sort(eventList, new Comparator<Event>()
 					{
 
 						@Override
 						public int compare(Event e1, Event e2)
 						{
 							String sDateStr = e1.getStartDate();
 							String tDateStr = e2.getStartDate();
 
 							return CalendarFunctions.compareDates(sDateStr, sDateStr);
 						}
 
 					});
 					adapter.notifyDataSetChanged();
 				}
 			});
 
 			final Activity listActivity = this;
 
 			featuredFilterButton.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					if (featureMode)
 					{
 						ArrayList<Event> featuredEvents = new ArrayList<Event>();
 						for (Event event : eventList)
 						{
 							if (event.isFeatured()) featuredEvents.add(event);
 						}
 						setListAdapter(new EventListAdapter(listActivity,
 								R.layout.custom_event_list_row, featuredEvents));
 						featureMode = false;
 					}
 					else
 					{
 						setListAdapter(adapter);
 						featureMode = true;
 					}
 				}
 			});
 
 			categoryGridButton.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					Intent i = new Intent(v.getContext(), CategoryGridActivity.class);
 					startActivity(i);
 				}
 			});
 
 			loginButton.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					if (currentUser != null)
 					{
 						SessionFunctions.endUserSession(activity);
 						currentUser = null;
 						Intent i = new Intent(v.getContext(), MainActivity.class);
 						startActivity(i);
 					}
 					else
 					{
 						Intent i = new Intent(v.getContext(), LoginActivity.class);
 						startActivity(i);
 					}
 				}
 			});
 
 			settingsButton.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					Intent i = new Intent(v.getContext(), PreferenceSettingsActivity.class);
 					startActivity(i);
 				}
 			});
 			
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
 
 }
