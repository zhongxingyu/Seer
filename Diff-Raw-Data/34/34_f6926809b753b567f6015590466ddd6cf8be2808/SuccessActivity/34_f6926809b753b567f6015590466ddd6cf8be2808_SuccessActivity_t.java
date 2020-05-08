 package uq.deco7381.runspyrun.Activity;
 
import uq.deco7381.runspyrun.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class SuccessActivity extends Activity {
 
 	static final LatLng NKUT = new LatLng(23.979548, 120.696745);
     private GoogleMap map;
     
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_success);
 		
 		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
         Marker nkut = map.addMarker(new MarkerOptions().position(NKUT).title("�n�}��ޤj��").snippet("�Ʀ�ͬ��зN�t"));
 		// Move the camera instantly to NKUT with a zoom of 16.
         map.moveCamera(CameraUpdateFactory.newLatLngZoom(NKUT, 16));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.success, menu);
 		return true;
 	}
 
 }
