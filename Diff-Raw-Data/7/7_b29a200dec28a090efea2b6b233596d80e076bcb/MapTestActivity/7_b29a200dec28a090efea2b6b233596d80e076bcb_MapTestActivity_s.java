 package waleed.home.android;
 
 import com.google.android.maps.MapActivity;
 
 import android.os.Bundle;
 
 public class MapTestActivity extends MapActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         // TODO Auto-generated method stub
         return false;
     }
 }
