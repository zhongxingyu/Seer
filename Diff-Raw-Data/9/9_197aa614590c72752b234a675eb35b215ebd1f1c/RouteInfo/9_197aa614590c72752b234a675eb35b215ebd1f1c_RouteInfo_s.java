 package com.dapontes.trackmyroute.activities;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.AbstractMap;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.dapontes.trackmyroute.Position;
 import com.dapontes.trackmyroute.R;
 import com.dapontes.trackmyroute.Route;
 import com.dapontes.trackmyroute.adapters.PositionsAdapter;
 import com.dapontes.trackmyroute.loaders.PositionsLoader;
 import com.dapontes.trackmyroute.support.Logger;
 import com.dapontes.trackmyroute.support.Utilities;
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 public class RouteInfo extends FragmentActivity implements LoaderManager.LoaderCallbacks<List<Position>>
 {
     private static final String TAG = "RouteInfo";
     
     // activity elements
     private TextView routeDate;    
     private TextView totalDistance;
     private TextView totalTime;
     private GoogleMap map;    
     
     private Route selectedRoute = null;
     private PositionsAdapter adapter;
     
     private LatLngBounds routeBounds = null;
     private List<Marker> markers = new ArrayList<Marker>();
     
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         BugSenseHandler.initAndStartSession(this, "06c7982d");
         setContentView(R.layout.activity_route_info);               
 
         try 
         {
             MapsInitializer.initialize(getApplicationContext());
         } 
         catch (GooglePlayServicesNotAvailableException impossible) 
         {
             Log.i(TAG, "Cannot initialize map");
             return;
         }
         
         // initialize activity elements
         routeDate = (TextView)findViewById(R.id.routeDate);        
         totalDistance = (TextView)findViewById(R.id.totalDistance);
         totalTime = (TextView)findViewById(R.id.totalTime);                
         map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.routeMap)).getMap();
         
         map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.97, 23.74), 10));
         
         adapter = new PositionsAdapter(this);
         
         
         // get the route info from the intent extras
         Intent intent = getIntent();        
         selectedRoute = intent.getExtras().getParcelable("selectedRoute"); 
         
         // fill the controls
         routeDate.setText(
                 new SimpleDateFormat("dd/MM/yyyy", new Locale("el")).format(selectedRoute.startDate) +
                 " - " +                
                 new SimpleDateFormat("HH:mm:ss", new Locale("el")).format(selectedRoute.startDate));
         
         totalTime.setText(Utilities.VerbalizeTimeSpan(selectedRoute.startDate, selectedRoute.endDate, this.getResources(), false));
         totalDistance.setText(Integer.toString(selectedRoute.totalMeters) + " meters");
 
         // start a loader to get all the positions of the selectedRoute
         Bundle bundle = new Bundle();        
         bundle.putLong("currentRouteId", selectedRoute.id);
         getSupportLoaderManager().initLoader(0, bundle, this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_route_info, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch(item.getItemId())
         {
             case R.id.menu_center_route:
                 map.moveCamera(CameraUpdateFactory.newLatLngBounds(routeBounds, 20));
                 return true;
         }       
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public Loader<List<Position>> onCreateLoader(int id, Bundle args)
     {
         return new PositionsLoader(this, selectedRoute.id);
     }
     
     @Override
     public void onLoaderReset(Loader<List<Position>> loader)
     {
         adapter.setData(null);
     }
 
     @Override
     public void onLoadFinished(Loader<List<Position>> loader, List<Position> data)
     {
         adapter.setData(data);
         
         PolylineOptions polylineOptions = new PolylineOptions();
         polylineOptions.width(5);
         polylineOptions.color(Color.BLUE);
         
         // create a bounds object that will contain all the route points
         // so as to be able to zoom to the whole route
         LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();  
         
         // set a variable to hold the total distance between the first and last point 
         // calculated by adding the distances between all sequential points
         float distance = 0;
         
         LatLng previous = null;
         Iterator<Position> iterator = data.iterator();        
         while(iterator.hasNext())
         {
             Position position = iterator.next();
             LatLng current = new LatLng(position.latitude, position.longitude);
             
             polylineOptions.add(current);            
             boundsBuilder.include(current);
             
             if (previous != null)
             {
                 Location previousLocation = Utilities.locationFromLatLng(previous);
                 Location currentLocation = Utilities.locationFromLatLng(current);                                
                 float distanceToPrevious = previousLocation.distanceTo(currentLocation);
                 
                 // check if thousand has changed
                 if (Math.floor(distance / 1000) * 1000 != Math.floor((distance + distanceToPrevious) / 1000) * 1000)
                 {
                     addKmMarker(previousLocation, currentLocation, distance, distanceToPrevious, position.trackTime);                    
                 }
                 
                 distance += distanceToPrevious;
                 previous = current;
             }
             else
             {
                 previous = current;                                             
             }
         }
         
         
         // add a marker for the first and last point of the route
         Position first = data.get(0), 
                  last = data.get(data.size() - 1);
         
         LatLng firstPoint = new LatLng(first.latitude, first.longitude),
                lastPoint = new LatLng(last.latitude, last.longitude);
         
         map.addMarker(new MarkerOptions().position(firstPoint).title("Route Start").snippet(Utilities.TimeToText(first.trackTime)));
         map.addMarker(new MarkerOptions().position(lastPoint).title("Route End").snippet(Utilities.TimeToText(last.trackTime)));
         
         //totalDistance.setText(Float.toString((float)Math.round(distance * 100) / 100) + " meters");
         
         // draw the route line on the map
         map.addPolyline(polylineOptions);
                 
         // move the map to the route bounds
         routeBounds = boundsBuilder.build();
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(routeBounds, 20));
     }
     
     /**
      * Adds a marker for the completed km of the route 
      */
     @SuppressWarnings("unchecked")
     private void addKmMarker(Location previousLocation, Location currentLocation, float distanceUpToPrevious, float distance, Date previousLocationDate)
     {        
         int nextThousandMeters = (int)Math.floor((distance + distanceUpToPrevious) / 1000) * 1000;
         float percentage = (nextThousandMeters - distanceUpToPrevious) / distance;
         
         // find the kilometer location where the marker should be placed
         double  kmLng = 0, 
                 kmLat = 0,        
                 x1 = previousLocation.getLongitude(), 
                 x2 = currentLocation.getLongitude(), 
                 y1 = previousLocation.getLatitude(), 
                 y2 = currentLocation.getLatitude();
                        
         if (x1 > x2)
         {
             kmLng = x1 - percentage * (x1 - x2);
         }
         else
         {
             kmLng = x1 + percentage * (x2 - x1);
         }
         
         if (y1 > y2)
         {
             kmLat = y1 - percentage * (y1 - y2);
         }
         else
         {
             kmLat = y1 + percentage * (y2 - y1);
         }
         
         LatLng markerLocation = new LatLng(kmLat, kmLng);
         String markerSnippet = Utilities.TimeToText(previousLocationDate);
         String markerTitle = "Km " + Integer.toString(nextThousandMeters / 1000);        
         Marker marker = map.addMarker(new MarkerOptions().position(markerLocation).title(markerTitle).snippet(markerSnippet));
         markers.add(marker);
         
         // reverse geocode markers location in AsyncTask
         new GeocodeKilometerMarkers(RouteInfo.this).execute(new AbstractMap.SimpleEntry<String, LatLng>(marker.getId(), markerLocation));
     }
     
     
     private class GeocodeKilometerMarkers extends AsyncTask<AbstractMap.SimpleEntry<String, LatLng>, AbstractMap.SimpleEntry<String, String>, Void>
     {
         private Context context;
         
         public GeocodeKilometerMarkers(Context context)
         {
             super();
             this.context = context;
         }
         
         @SuppressWarnings("unchecked")
         @Override
         protected Void doInBackground(AbstractMap.SimpleEntry<String, LatLng>... values)
         {
             LatLng latLng = values[0].getValue();
             Geocoder geocoder = new Geocoder(context, Locale.getDefault());            
             List<Address> addresses = null;
                         
             try
             {
                 addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
             }
             catch(IOException e)
             {
                 Logger.write("Unable to reverse geocode [" + Double.toString(latLng.latitude) + " " + Double.toString(latLng.longitude) + "]", "RouteInfo-GeocodeKilometerMarkers", context);
                 addresses = null;
             }
             
             if (addresses != null && addresses.size() > 0)
             {
                 SimpleEntry<String, String> markerAddress = new SimpleEntry<String, String>(values[0].getKey(), addresses.get(0).getAddressLine(0));
                 publishProgress(markerAddress);                    
             }            
 
             return null;
         }
         
         @Override
         protected void onProgressUpdate(SimpleEntry<String, String>... value)
         {
             for (int i=0; i<markers.size(); i++)
             {
                 if (markers.get(i).getId().equals(value[0].getKey()))
                 {
                     markers.get(i).setSnippet(markers.get(i).getSnippet() + " " + value[0].getValue());
                     break;
                 }
             }                
         }
     }
 }
