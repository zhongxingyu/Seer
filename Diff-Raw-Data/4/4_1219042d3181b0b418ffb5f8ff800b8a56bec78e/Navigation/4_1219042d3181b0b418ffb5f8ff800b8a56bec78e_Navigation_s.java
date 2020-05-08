 /**
  * 
  */
 package eu.fbk.dycapo.activities;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import eu.fbk.dycapo.exceptions.DycapoException;
 import eu.fbk.dycapo.factories.bundles.ParticipationBundle;
 import eu.fbk.dycapo.maputils.DycapoItemizedOverlay;
 import eu.fbk.dycapo.maputils.DycapoOverlay;
 import eu.fbk.dycapo.maputils.LocationService;
 import eu.fbk.dycapo.models.Location;
 import eu.fbk.dycapo.models.Participation;
 import eu.fbk.dycapo.models.Person;
 import eu.fbk.dycapo.persistency.ActiveTrip;
 import eu.fbk.dycapo.persistency.DBParticipation;
 import eu.fbk.dycapo.persistency.DBPerson;
 import eu.fbk.dycapo.persistency.DBTrip;
 import eu.fbk.dycapo.services.broker.Broker;
 import eu.fbk.dycapo.util.Environment;
 import eu.fbk.dycapo.util.GeoGuard;
 import eu.fbk.dycapo.util.ParticipationUtils;
 
 /**
  * 
  * @author riccardo
  * 
  */
 public class Navigation extends MapActivity {
 	private static final String TAG = "Navigation";
 
 	private static ProgressDialog myProgressDialog;
 	private LocationService dls = null;
 	private int navRole;
 	private DycapoItemizedOverlay items = null;
 	private DycapoItemizedOverlay me = null;
 	private Drawable driverMarker = null;
 	private Drawable riderMarker = null;
 
 	public Broker br = null;
 	private MapView mapView;
 
 	private Button button1;
 	private Button button2;
 	private Button button3;
 	private Thread updateStatus = new Thread() {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			int what = 0;
 			Participation tmp = DBParticipation.getParticipations().get(0);
 			if (riderOnBoard == false) {
 				tmp.setStatus(Participation.STARTED);
 				Navigation.this.button2.setText("Finish Participation");
 			} else {
 				tmp.setStatus(Participation.FINISHED);
 				what = 1;
 			}
 			ParticipationUtils.updateDycapoParticipation(tmp);
 			DBParticipation.updateParticipation(tmp);
 			Navigation.this.handleCommonSuccess.sendEmptyMessage(what);
 		}
 
 	};
 
 	private Thread pathDrawer = new Thread() {
 
 		/*
 		 * (non-Javadoc) null
 		 * 
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			DrawPath();
 			((MapView) Navigation.this.findViewById(R.id.myMapView1))
 					.postInvalidate();
 			Navigation.this.handleCommonSuccess.sendEmptyMessage(1);
 
 		}
 	};
 
 	private OnClickListener startTrip = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			myProgressDialog = ProgressDialog.show(Navigation.this,
 					"Please wait...", "Updating On the Server", true, true);
 			Navigation.this.updateStatus.start();
 
 		}
 
 	};
 
 	private static boolean riderOnBoard = false;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.navigation);
 
 		Log.d(TAG, this.getIntent().getExtras().keySet().toString());
 		String tmp = this.getIntent().getExtras().getString("role");
 		if (tmp.equals("driver"))
 			this.navRole = Environment.DRIVER;
 		else
 			this.navRole = Environment.RIDER;
 
 		this.driverMarker = this.getResources().getDrawable(R.drawable.dyca03);
 		this.riderMarker = this.getResources().getDrawable(R.drawable.dyca04);
 		this.dls = new LocationService(this);
 		this.br = Broker.BrokerFactory.getBroker(navRole, this);
 
 		this.dls.startLocationService();
 		this.br.startBroker();
 
 		this.mapView = (MapView) findViewById(R.id.myMapView1);
 		this.mapView.setBuiltInZoomControls(true);
 		this.mapView.setSatellite(false);
 		this.mapView.setStreetView(true);
 		this.mapView.setClickable(true);
 
 		this.button1 = (Button) this.findViewById(R.id.navButton1);
 		this.button2 = (Button) this.findViewById(R.id.navButton2);
 		this.button3 = (Button) this.findViewById(R.id.navButton3);
 
 		switch (this.navRole) {
 		case Environment.DRIVER:
 			this.items = new DycapoItemizedOverlay(this.riderMarker, this);
 			this.me = new DycapoItemizedOverlay(this.driverMarker, this);
 			this.button1.setText("Participants");
 			this.button2.setText("Finish Trip");
 			try {
 
 				this.mapView.getController().setZoom(15);
 
 				// start thread
 				myProgressDialog = ProgressDialog.show(Navigation.this,
 						"Please wait...", "Drawing Directions", true, true);
 
 				this.pathDrawer.start();
 			} catch (Exception e) {
 				new AlertDialog.Builder(this)
 						.setMessage(e.getMessage())
 						.setPositiveButton("OK",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										finish();
 									}
 								}).show();
 			}
 
 			break;
 		case Environment.RIDER:
 			this.items = new DycapoItemizedOverlay(this.driverMarker, this);
 			this.me = new DycapoItemizedOverlay(this.riderMarker, this);
 			this.button1.setText("Show Driver");
 			this.button2.setText("Start Participation");
 
 			this.button2.setOnClickListener(this.startTrip);
 			break;
 		}
 		this.button3.setText("Cancel");
 	}
 
 	public Handler handleCommonSuccess = new Handler() {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.os.Handler#handleMessage(android.os.Message)
 		 */
 		@Override
 		public void handleMessage(Message msg) {
 			myProgressDialog.dismiss();
 			switch (msg.what) {
 			case 0:
 				break;
 			case 1:
 				break;
 			}
 
 		}
 
 	};
 
 	public Handler handleMapUpdate = new Handler() {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.os.Handler#handleMessage(android.os.Message)
 		 */
 		@Override
 		public void handleMessage(Message msg) {
 			List<Participation> list = ParticipationBundle
 					.unboxParticipations(msg.getData());
 			MapView map = (MapView) Navigation.this
 					.findViewById(R.id.myMapView1);
 			GeoPoint point;
 			OverlayItem overlayitem;
 			List<Overlay> mapoverlays = Navigation.this.mapView.getOverlays();
 			if (mapoverlays.contains(Navigation.this.items))
 				mapoverlays.remove(Navigation.this.items);
 			if (mapoverlays.contains(Navigation.this.me))
 				mapoverlays.remove(Navigation.this.me);
 
 			Navigation.this.me.clearOverlays();
 			Navigation.this.items.clearOverlays();
 
 			Log.d(TAG, String.valueOf(list.size()));
 			for (int i = 0; i < list.size(); i++) {
 				Person tmp = list.get(i).getAuthor();
 				Log.d(TAG, tmp.getUsername());
 				if (LocationService.getPosition(tmp) instanceof Location) {
 					tmp.setPosition(LocationService.getPosition(tmp));
 
 					String geo_point = tmp.getPosition().getGeorss_point();
 
 					point = GeoGuard.parseGeoRSSPoint(geo_point);
 					Log.d(TAG,
 							"point Latitude : "
 									+ String.valueOf(((double) point
 											.getLatitudeE6()) / 1E6));
 					Log.d(TAG,
 							"point Longitude : "
 									+ String.valueOf(((double) point
 											.getLongitudeE6()) / 1E6));
 
 					Log.d(TAG, list.get(i).getAuthor().getUsername());
 					overlayitem = new OverlayItem(point, list.get(i)
 							.getAuthor().getUsername(), list.get(i).getAuthor()
 							.getHref());
 
 					if (tmp.getUsername().equals(
 							DBPerson.getUser().getUsername()))
 						me.addOverlay(overlayitem);
 					else
 						items.addOverlay(overlayitem);
 				}
 			}
 			if (items.size() != 0)
 				map.getOverlays().add(items);
			map.getOverlays().add(me);
 			Log.d(TAG, "sending PostInvalidate");
 			map.postInvalidate();
 		}
 
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
 	 */
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	/**
 	 * Method draws the direction provided by google on the map
 	 * 
 	 * @param src
 	 *            is the Source Location
 	 * @param dest
 	 *            is the destination Location
 	 * @param color
 	 *            is the Color of the overlay
 	 * @param mMapView01
 	 */
 	private void DrawPath() {
 		try {
 			ActiveTrip aTrip = DBTrip.getActiveTrip();
 			if (aTrip.getRoute().getmDecodedPolyline() != null)
 				this.mapView.getOverlays().add(
 						new DycapoOverlay(aTrip.getRoute()
 								.getmDecodedPolyline()));
 			this.mapView.getController().animateTo(
 					aTrip.getRoute().getmDecodedPolyline().get(0));
 			myProgressDialog.dismiss();
 		} catch (DycapoException e) {
 			e.alertUser(Navigation.this);
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		this.mapView.postInvalidate();
 	}
 
 }
