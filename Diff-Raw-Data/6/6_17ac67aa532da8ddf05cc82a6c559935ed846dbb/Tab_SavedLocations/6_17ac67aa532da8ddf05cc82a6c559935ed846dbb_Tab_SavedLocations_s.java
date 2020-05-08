 package tb14.walkbasehackathon;
 
 import java.util.Date;
 import java.util.List;
 
 import tb14.walkbasehackathon.Adapter.LocationAdapter;
 import tb14.walkbasehackathon.DAOs.LocationDAO;
 import tb14.walkbasehackathon.DTO.Location;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Tab_SavedLocations extends Fragment {
 	private LocationDAO locationDAO;
 	private ListView list;
 	private final String TAG = "Saved locations";
 	private SharedPreferences prefs;
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		final View view = inflater.inflate(R.layout.tab_savedlocations, container, false);
 		
 		prefs = (SharedPreferences) view.getContext().getSharedPreferences("WBHPrefs", Context.MODE_PRIVATE);
 
 		locationDAO = new LocationDAO(view.getContext());
 		locationDAO.open();
 		final List<Location> values = locationDAO.getAllLocations();
 
 		list = (ListView) view.findViewById(R.id.list);
 		list.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// When clicked, show a toast with the TextView text
 				Log.v(TAG, "item "+position+" clicked");
 			}
 		});
 		
 		ArrayAdapter<Location> adapter = new LocationAdapter(view.getContext(),
 				R.layout.locationrow, values);
 		list.setAdapter(adapter);
 		
 		Button addLocationButton = (Button)view.findViewById(R.id.addLocationButton);
 		addLocationButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ArrayAdapter<Location> adapter = new LocationAdapter(view.getContext(),
 						R.layout.locationrow, values);
 				TextView locationName = (TextView)view.findViewById(R.id.location_name);
 				if (!locationName.getText().toString().equals("")) {
 					Location tmplocation = new Location();
 					tmplocation.setName( locationName.getText().toString());
 					tmplocation.setTimestamp(new Date(prefs.getLong("timestamp", 0)));
 					tmplocation.setLatitude( (double) prefs.getLong("latitude", 0));
 					tmplocation.setLongitude( (double) prefs.getLong("longitude", 0));
 					tmplocation.setAccuracy( (double) prefs.getLong("accuracy", 0));
 					Location location = locationDAO.createLocation(tmplocation);
 					adapter.add(location);
 				} else {
 					Toast.makeText(v.getContext(), "Specify a name for the location", 2000).show();
 					
 				}
 			}
 		});
 		
 
 
 		// Inflate the layout for this fragment
 		return view;
 	}
 }
