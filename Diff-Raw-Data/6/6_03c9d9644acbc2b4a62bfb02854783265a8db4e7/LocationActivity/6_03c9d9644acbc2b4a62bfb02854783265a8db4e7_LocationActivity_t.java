 package hr.android.petkovic.igoout.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import hr.android.petkovic.igoout.Constants;
 import hr.android.petkovic.igoout.R;
 import hr.android.petkovic.igoout.adapter.EventListAdapter;
 import hr.android.petkovic.igoout.api.EventsListener;
 import hr.android.petkovic.igoout.api.RestApiClient;
 import hr.android.petkovic.igoout.model.Event;
 import hr.android.petkovic.igoout.model.Location;
 import hr.android.petkovic.igoout.model.MockData;
 import hr.android.petkovic.igoout.utils.BitmapLruCache;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.android.volley.RequestQueue;
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.NetworkImageView;
 import com.android.volley.toolbox.Volley;
 
 public class LocationActivity extends AbstractFragmentActivity implements OnClickListener, OnItemClickListener {
 
 	private ListView eventList;
 	private EventListAdapter adapter;
 	private View header;
 	private View cont;
 	private View detailsCont;
 	private NetworkImageView locationImage;
 	private NetworkImageView locationMap;
 	private TextView name;
 	private TextView type;
 	private TextView address;
 	private TextView website;
 	private TextView workingHours;
 	private TextView summary;
 	private TextView phone;
 	private Location location;
 
 	private RequestQueue mRequestQueue;
 	private ImageLoader imageLoader;
 	private ArrayList<Event> events;
 	private EventsListener eventsListener;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) { //liniju po liniju objasniti :)
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.location);
 
 		mRequestQueue = Volley.newRequestQueue(this);
 		imageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(BitmapLruCache.getDefaultLruCacheSize()));
 
 		header = getLayoutInflater().inflate(R.layout.location_header, null, false);
 		eventList = (ListView) findViewById(R.id.location_event_list);
 		eventList.addHeaderView(header);
 
 		cont = findViewById(R.id.location_header_cont);
 		detailsCont = findViewById(R.id.location_header_details_cont);
 		locationImage = (NetworkImageView) findViewById(R.id.location_img);
 		locationMap = (NetworkImageView) findViewById(R.id.location_map);
 		name = (TextView) findViewById(R.id.location_name);
 		type = (TextView) findViewById(R.id.location_type);
 
 		address = (TextView) findViewById(R.id.location_address);
 		website = (TextView) findViewById(R.id.location_website);
 		workingHours = (TextView) findViewById(R.id.location_working_hours);
 		summary = (TextView) findViewById(R.id.location_summary);
 		phone = (TextView) findViewById(R.id.location_phone);
 
 		location = (Location) getIntent().getSerializableExtra(Constants.LOCATION);
 
 		locationImage.setDefaultImageResId(R.drawable.ic_launcher);
 		locationImage.setImageUrl(location.getPictureUrl(), imageLoader);
 		locationMap.setDefaultImageResId(R.drawable.ic_launcher);
 		locationMap.setImageUrl(getString(R.string.google_static_map_api, location.getLat(), location.getLng()), imageLoader);
 		name.setText(location.getName());
 		type.setText(location.getType());
 		address.setText(location.getAddress());
 		website.setText(location.getWebsite());
 		workingHours.setText(location.getWorkingHours());
 		phone.setText(location.getPhoneNumber());
 		summary.setText(location.getSummary());
 
 		detailsCont.setVisibility(View.GONE);
 
 		// events = MockData.getEvents();
 
 		cont.setOnClickListener(this);
 		locationMap.setOnClickListener(this);
 		eventList.setOnItemClickListener(this);
 
 		ActionBar ab = getSupportActionBar();
 		if (ab != null) {
 			ab.setTitle(location.getName());
 			ab.setDisplayHomeAsUpEnabled(true);
 		}
 		eventsListener = new EventsListener() {
 
 			@Override
 			public void onEventsReady(ArrayList<Event> events) {
 				hideDialog();
 				LocationActivity.this.events = events;
 				if (events == null) {
 					Toast.makeText(LocationActivity.this, "Unable to get events..", Toast.LENGTH_SHORT).show();
 				} else {
 					populateEvents();
 				}
 			}
 		};
 		getData();
 		
 	}
 
 	private void getData() {
 
 		showDialog();
 		RestApiClient.get().getEvents(location.getId(), eventsListener);
 	}
 
 	private void populateEvents() {
 		adapter = new EventListAdapter(this, events);
 		eventList.setAdapter(adapter);
 	}
 
 	@Override
	protected void onResume() { //�to se doga�a kod resume?
 		super.onResume();
 	
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		eventsListener = null;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.location_menu, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.location_share) {
 			Intent intent = new Intent(Intent.ACTION_SEND);
 			intent.putExtra(Intent.EXTRA_SUBJECT, location.getName());
 			intent.putExtra(Intent.EXTRA_TEXT, location.getWebsite());
 			intent.setType("text/plain");
 			startActivity(Intent.createChooser(intent, getString(R.string.share)));
 			return true;
 		} else if (item.getItemId() == android.R.id.home) {
 			finish();
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		if (position == 0){
			return;
		}
 		Intent i = new Intent(this, EventActivity.class);
 		i.putExtra(Constants.EVENT, events.get(position - 1));
 		i.putExtra(Constants.LOCATION, location);
 		startActivity(i);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == cont) {
 			detailsCont.setVisibility(View.VISIBLE);
 		} else if (v == locationMap) {
 			startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_map_external, location.getLat(),
 					location.getLng(), location.getLat(), location.getLng()))));
 		}
 	}
 
 }
