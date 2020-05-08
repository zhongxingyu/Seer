 package jp.knct.di.c6t.ui.exploration;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.model.Exploration;
 import jp.knct.di.c6t.model.Quest;
 import jp.knct.di.c6t.util.ActivityUtil;
 import jp.knct.di.c6t.util.MapUtil;
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 
 public class ExplorationMainActivity extends Activity
 		implements ConnectionCallbacks,
 		OnConnectionFailedListener,
 		LocationListener {
 	private Exploration mExploration;
 	private LocationClient mLocationClient;
 	private GoogleMap mMap;
 	private int mCurrentQuestNumber = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_exploration_main);
 
 		mExploration = getIntent().getParcelableExtra(IntentData.EXTRA_KEY_EXPLORATION);
 
 		setUpMap();
 
 		mLocationClient = new LocationClient(this, this, this);
 		mLocationClient.connect();
 	}
 
 	private void setUpMap() {
 		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.exploration_main_map))
 				.getMap();
 		mMap.setMyLocationEnabled(true);
 
 		CameraPosition position = getIntent().getParcelableExtra(IntentData.EXTRA_KEY_CAMERA_POSITION);
 		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == QuestExecutionActivity.REQUEST_CODE_EXECUTION &&
 				resultCode == RESULT_OK) {
 			mCurrentQuestNumber++;
			mLocationClient.connect();
 			// TODO: update quest image and something
 		}
 
 		if (mCurrentQuestNumber > 4) {
 			finishExploration();
 		}
 	}
 
 	private void finishExploration() {
 		Toast.makeText(this, "T", Toast.LENGTH_SHORT).show();
 	}
 
 	private void setLocationHintsText(float distance, float bearing) {
 		new ActivityUtil(this)
 				.setText(R.id.exploration_main_distance, distance + "m")
 				.setText(R.id.exploration_main_bearing, bearing + "x");
 	}
 
 	@Override
 	public void onConnected(Bundle bundle) {
 		mLocationClient.requestLocationUpdates(LocationRequest.create()
 				.setFastestInterval(5000)
 				.setInterval(5000)
 				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY), this);
 	}
 
 	@Override
 	public void onDisconnected() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult result) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		Quest currentQuest = mExploration.getRoute().getQuests().get(mCurrentQuestNumber);
 		float[] distanceAndBearing = MapUtil.calculateDistanceAndBearingToQuestPoint(location, currentQuest);
 		float distance = distanceAndBearing[0];
 		float bearing = distanceAndBearing[1];
 
 		setLocationHintsText(distance, bearing);
 
		if (distance < 500000000) {
			mLocationClient.disconnect();
 			Toast.makeText(this, "NGXgsʂɈڍs܂", Toast.LENGTH_SHORT).show();
 			Intent intent = new Intent(this, QuestExecutionActivity.class)
 					.putExtra(IntentData.EXTRA_KEY_QUEST, currentQuest)
 					.putExtra(IntentData.EXTRA_KEY_QUEST_NUMBER, mCurrentQuestNumber);
 			startActivityForResult(intent, QuestExecutionActivity.REQUEST_CODE_EXECUTION);
 			return;
 		}
 	}
 }
