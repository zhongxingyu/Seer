 package de.htwg.seapal.aview.gui.activity;
 
 import java.util.List;
 import java.util.UUID;
 
import roboguice.activity.RoboActivity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.google.inject.Inject;
 
 import de.htwg.seapal.R;
 import de.htwg.seapal.aview.gui.adapter.TripListAdapter;
 import de.htwg.seapal.controller.impl.TripController;
 import de.htwg.seapal.utils.observer.Event;
 import de.htwg.seapal.utils.observer.IObserver;
 
public class TripListActivity extends RoboActivity implements IObserver {
 
 	@Inject
 	private TripController controller;
 	private List<UUID> tripList;
 	private UUID boat;
	private ViewGroup mainView ;
 	private View header;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.triplist);
 		controller.addObserver(this);
 
 		Bundle extras = getIntent().getExtras();
 		boat = UUID.fromString(extras.getString("boat"));
 
 		tripList = controller.getTrips(boat);
 		addListView();
		
 	}
	
	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		mainView.removeView(header);
 		addListView();
 	}
 
 	@Override
 	public void update(Event event) {
 		// TODO Auto-generated method stub
 
 	}
	
 	private void addListView() {
 		ListView listview = (ListView) findViewById(R.id.tripsListView);
 		TripListAdapter adapter = new TripListAdapter(this,
 				R.layout.triplistadapter, tripList, controller);
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		header = inflater.inflate(R.layout.tripslistheader, null);
 		mainView = (ViewGroup) findViewById(R.id.tripList);
 		mainView.addView(header, 0);
 
 		listview.setAdapter(adapter);
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				// start TripActivity
 			}
 
 		});
 	}
 
 }
