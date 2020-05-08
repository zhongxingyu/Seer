 package mai.icyrain;
 
 // import static com.mapquest.android.maps.MapActivity.TAG;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.location.LocationManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.mapquest.android.maps.GeoPoint;
 import com.mapquest.android.maps.MapView;
 import com.mapquest.android.maps.MyLocationOverlay;
 import com.mapquest.android.maps.RouteManager;
 import com.mapquest.android.maps.RouteResponse;
 import com.mapquest.android.maps.ServiceResponse.Info;
 
 public class RouteItineraryDemo extends SimpleMap {
 
   protected MapView mapView;
 
   protected MyLocationOverlay myLocationOverlay;
   protected RouteLocationListener locationListener;
 
   protected EditText start;
 
   @Override
   protected int getLayoutId() {
     return R.layout.custom_route_itinerary_demo;
   }
 
   @Override
   protected void init() {
     super.init();
 
     // find the objects we need to interact with
     mapView = (MapView) findViewById(R.id.map);
     myLocationOverlay = new MyLocationOverlay(this, mapView);
     final RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
     final RelativeLayout itineraryLayout = (RelativeLayout) findViewById(R.id.itineraryLayout);
     final Button createRouteButton = (Button) findViewById(R.id.createRouteButton);
     final Button showItineraryButton = (Button) findViewById(R.id.showItineraryButton);
     final Button showMapButton = (Button) findViewById(R.id.showMapButton);
     final Button clearButton = (Button) findViewById(R.id.clearButton);
 
     final EditText end = (EditText) findViewById(R.id.endTextView);
     final RouteItineraryView itinerary = (RouteItineraryView) findViewById(R.id.itinerary);
 
     final RouteManager routeManager = new RouteManager(this);
     routeManager.setMapView(this.mapView);
     routeManager.setBestFitRoute(true);
 
     // we want walking directions
     JSONObject options = new JSONObject();
     try {
       options.put("routeType", "pedestrian");
       // pass in options
       routeManager.setOptions(options.toString());
     } catch (JSONException e) {
      Log.d("pullLocation", "Issue with JSON options.");
       e.printStackTrace();
     }
 
     routeManager.setDebug(true);
 
     // callbacks for the routemanager
     routeManager.setRouteCallback(new RouteManager.RouteCallback() {
       @Override
       public void onError(RouteResponse routeResponse) {
         final Info info = routeResponse.info;
         final int statusCode = info.statusCode;
 
         final StringBuilder message = new StringBuilder();
         message.append("Unable to create route.\n").append("Error: ").append(statusCode)
           .append("\n").append("Message: ").append(info.messages);
         Toast.makeText(getApplicationContext(), message.toString(), Toast.LENGTH_LONG).show();
         createRouteButton.setEnabled(true);
       }
 
       @Override
       public void onSuccess(RouteResponse routeResponse) {
         clearButton.setVisibility(View.VISIBLE);
         if (showItineraryButton.getVisibility() == View.GONE
           && showMapButton.getVisibility() == View.GONE) {
           showItineraryButton.setVisibility(View.VISIBLE);
         }
 
         // listen for location changes
         locationListener = new RouteLocationListener(routeResponse);
         LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
         lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, locationListener);
 
         itinerary.setRouteResponse(routeResponse);
         createRouteButton.setEnabled(true);
       }
 
     });
 
     // attach the show itinerary listener
     showItineraryButton.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
         mapLayout.setVisibility(View.GONE);
         itineraryLayout.setVisibility(View.VISIBLE);
         showItineraryButton.setVisibility(View.GONE);
         showMapButton.setVisibility(View.VISIBLE);
       }
     });
 
     // attach the show map listener
     showMapButton.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
         mapLayout.setVisibility(View.VISIBLE);
         itineraryLayout.setVisibility(View.GONE);
         showMapButton.setVisibility(View.GONE);
         showItineraryButton.setVisibility(View.VISIBLE);
       }
     });
 
     // attach the clear route listener
     clearButton.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
         routeManager.clearRoute();
         clearButton.setVisibility(View.GONE);
         showItineraryButton.setVisibility(View.GONE);
         showMapButton.setVisibility(View.GONE);
         mapLayout.setVisibility(View.VISIBLE);
         itineraryLayout.setVisibility(View.GONE);
       }
     });
 
     // create an onclick listener for the instructional text
     createRouteButton.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
         hideSoftKeyboard(view);
         createRouteButton.setEnabled(false);
 
         myLocationOverlay.enableMyLocation();
 
         myLocationOverlay.runOnFirstFix(new Runnable() {
 
           @Override
           public void run() {
             final GeoPoint currentLocation = myLocationOverlay.getMyLocation();
 
             mapView.getController().animateTo(currentLocation);
             mapView.getController().setZoom(14);
             mapView.getOverlays().add(myLocationOverlay);
             myLocationOverlay.setFollowing(true);
 
             final double latitude = currentLocation.getLatitude();
             final double longitude = currentLocation.getLongitude();
 
             final String startAt = "{latLng:{lat:" + latitude + ",lng:" + longitude + "}}";
             final String endAt = getText(end);
             Log.d("pullLocation", "startAt is " + startAt);
             Log.d("pullLocation", "endAt is " + endAt);
 
             runOnUiThread(new Runnable() {
               @Override
               public void run() {
                 routeManager.createRoute(startAt, endAt);
               }
             });
           }
         });
       }
     });
 
   }
 
 }
