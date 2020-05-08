 package com.ecn.urbapp.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.ecn.urbapp.R;
 import com.ecn.urbapp.db.GpsGeom;
 import com.ecn.urbapp.db.LocalDataSource;
 import com.ecn.urbapp.db.Photo;
 import com.ecn.urbapp.utils.ConvertGeom;
 import com.ecn.urbapp.utils.CustomListViewAdapter;
 import com.ecn.urbapp.utils.MathOperation;
 import com.ecn.urbapp.utils.RowItem;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 
 public class LoadLocalPhotosActivity extends Activity{
 
 	/**
 	 * creating datasource
 	 */
 	private LocalDataSource datasource;
 	/**
 	 * Contains all the projects attributes
 	 */
 	private List<Photo> refreshedValues;
 
 	/**
 	 * Hashmap between unique id of markers and the relative project_id
 	 */
 	private HashMap<String, Integer> photosMarkers = new HashMap<String, Integer>();
 	/**
 	 * displayable photos list
 	 */
 	private ListView listePhotos = null;	
 	/**
 	 * The google map object
 	 */
 	private GoogleMap map = null;
 	/**
 	 * The instance of GeoActivity for map activity
 	 */
 	GeoActivity displayedMap;
 	/**
 	 * The button for switching to satellite view
 	 */
 	private Button satellite = null;
 
 	/**
 	 * The button for switching to plan view
 	 */
 	private Button plan = null;
 
 	/**
 	 * The button for switching to hybrid view
 	 */
 	private Button hybrid = null;
 
 	//TODO add description for javadoc
 	private ArrayList<RowItem> rowItems;
 
 
 	//TODO add description for javadoc
 	long project_id;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.layout_loadlocalphotos);
 		datasource=MainActivity.datasource;
 		datasource.open();
 
 		/**
 		 * extras that countains the project_id
 		 */
 
 		project_id = getIntent().getExtras().getLong("SELECTED_PROJECT_ID");
 
 		map = ((MapFragment) getFragmentManager()
 				.findFragmentById(R.id.map)).getMap();
 
 		displayedMap = new GeoActivity(true, GeoActivity.defaultPos, map);
 
 		/**
 		 * Define the listeners for switch satellite/plan/hybrid
 		 */
 		satellite = (Button)findViewById(R.id.satellite);
 		plan = (Button)findViewById(R.id.plan);
 		hybrid = (Button)findViewById(R.id.hybrid);
 
 		satellite.setOnClickListener(displayedMap.toSatellite);
 		plan.setOnClickListener(displayedMap.toPlan);
 		hybrid.setOnClickListener(displayedMap.toHybrid);
 
 		listePhotos = (ListView) findViewById(R.id.listViewPhotos);
 		refreshListPhoto();
 
 
 		listePhotos.setOnItemClickListener(selectedPhoto);
 		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
 			@Override
 			public void onInfoWindowClick(Marker marker) {
 				Toast.makeText(MainActivity.baseContext, refreshedValues.get(photosMarkers.get(marker.getId())).toString(), Toast.LENGTH_LONG).show();
 				
 				//TODO Sebastien has to make it more readable
 				MainActivity.datasource.instanciatePhoto(refreshedValues.get(photosMarkers.get(marker.getId())).getPhoto_id());
 				
 				//TODO do a better way to have the path !
 				MainActivity.pathImage=Environment.getExternalStorageDirectory()+"/featureapp/"+refreshedValues.get(photosMarkers.get(marker.getId())).getPhoto_url();
 				
 				finish();
 
 			}
 		});
 	}
 
 	protected void onClose() {      
 		datasource.close();
 	}
 
 	//TODO add description for javadoc
 	/**
 	 * loading the different projects of the local db
 	 * @return
 	 */
 	public List<com.ecn.urbapp.db.Photo> recupPhoto() {
 
 		//List<com.ecn.urbapp.db.Photo> values = this.datasource.getAllPhotos();
 
 		//TODO CATCH EXCEPTION
 
 		List<com.ecn.urbapp.db.Photo> values = this.datasource.getAllPhotolinkedtoProject(project_id);
 		return values;
 
 	}
 	
 	/**
 	 * loading the different projects of the local db
 	 * @return
 	 */
 	public List<com.ecn.urbapp.db.GpsGeom> recupGpsGeom() {
 
 		//List<com.ecn.urbapp.db.Photo> values = this.datasource.getAllPhotos();
 
 		//TODO CATCH EXCEPTION
 
 		List<com.ecn.urbapp.db.GpsGeom> values = this.datasource.getAllGpsGeom();
 		return values;
 
 	}
 
 	/**
 	 * creating a list of project and loads in the view
 	 */
 	public void refreshListPhoto(){      
 
 		refreshedValues = recupPhoto();
 		List<com.ecn.urbapp.db.GpsGeom> allGpsGeom = recupGpsGeom();
 
 		rowItems = new ArrayList<RowItem>();
 		for (Photo image:refreshedValues) {
 			RowItem item = new RowItem(Environment.getExternalStorageDirectory()+"/featureapp/"+image.getPhoto_url(), image.getPhoto_url(), image.getPhoto_description());
 			rowItems.add(item);
 		}
 
 		CustomListViewAdapter adapter = new CustomListViewAdapter(this,
 				R.layout.layout_photolistview, rowItems);
 		listePhotos.setAdapter(adapter);
 
 		/**
 		 * Put markers on the map
 		 */
 		Integer i = Integer.valueOf(0);
 		for (Photo enCours:refreshedValues){
 			
			//TODO Get the real GPSGeom from Photo table in local Database !!!
			//Fake one ! for testing purpose
			/*String[] coord = enCours.getExt_GpsGeomCoord().split("//");
			LatLng coordPhoto = new LatLng(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
			LatLng coordPhoto1 = new LatLng(Double.parseDouble(coord[0])+0.2, Double.parseDouble(coord[1])+0.2);

			ArrayList<LatLng> photoGPS = new ArrayList<LatLng>();
			photoGPS.add(coordPhoto);
			photoGPS.add(coordPhoto1);*/
 			//TODO request for GPSGeom
 			ArrayList<LatLng> photoGPS = null;
 			for(GpsGeom gg : allGpsGeom){
 				if(gg.getGpsGeomsId()==enCours.getGpsGeom_id()){
 					photoGPS = ConvertGeom.gpsGeomToLatLng(gg);
 				}
 			}
 			//end of fake photoGPS values
 			
 			LatLng GPSCentered = MathOperation.barycenter(photoGPS);
 					
 			Marker marker = displayedMap.addMarkersColored(i, "Cliquez ici pour valider cette photo", GPSCentered);
 			
 			/**
 			 * Adding the line in the map
 			 */
 			
 			displayedMap.drawPolygon(photoGPS, false);
 
 			photosMarkers.put(marker.getId(), i);
 			i++;
 		}
 	}    
 
 	/**
 	 *  get the project selected in listview and show its position on the map 
 	 */
 	public OnItemClickListener selectedPhoto = new OnItemClickListener()
 	{
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View v, int position,
 				long id) {
 
 			//TODO Get the real GPSGeom from Photo table in local Database !!!
 			//Fake one ! for testing purpose
 			/*String[] coord = refreshedValues.get(position).getExt_GpsGeomCoord().split("//");
 			LatLng coordPhoto = new LatLng(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
 			LatLng coordPhoto1 = new LatLng(Double.parseDouble(coord[0])+0.2, Double.parseDouble(coord[1])+0.2);
 
 			ArrayList<LatLng> photoGPS = new ArrayList<LatLng>();
 			photoGPS.add(coordPhoto);
 			photoGPS.add(coordPhoto1);*/
 
 			List<com.ecn.urbapp.db.GpsGeom> allGpsGeom = recupGpsGeom();
 			ArrayList<LatLng> photoGPS = null;
 			for(GpsGeom gg : allGpsGeom){
 				if(gg.getGpsGeomsId()==refreshedValues.get(position).getGpsGeom_id()){
 					photoGPS = ConvertGeom.gpsGeomToLatLng(gg);
 				}
 			}
 			//end of fake photoGPS values
 			
 			LatLng GPSCentered = MathOperation.barycenter(photoGPS);
 			displayedMap = new GeoActivity(false, GPSCentered, map);
 			Toast.makeText(getApplicationContext(), refreshedValues.get(position).getPhoto_url(), Toast.LENGTH_LONG).show();                  
 		}
 	};
 }
