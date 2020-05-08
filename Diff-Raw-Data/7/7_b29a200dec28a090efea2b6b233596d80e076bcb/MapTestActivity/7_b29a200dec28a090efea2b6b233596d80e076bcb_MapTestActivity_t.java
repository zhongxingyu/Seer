 package waleed.home.android;
 
 import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
 
 import android.os.Bundle;
 
 public class MapTestActivity extends MapActivity {
    private MapView mymap ;
    private static final boolean DISPLAYZOOMONMAP = true ; 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        mymap = (MapView)findViewById(R.id.mapView);
        mymap.setBuiltInZoomControls(DISPLAYZOOMONMAP);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         // TODO Auto-generated method stub
         return false;
     }
 }
