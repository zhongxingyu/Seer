 package carnero.netmap.activity;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.telephony.CellInfo;
 import android.telephony.CellLocation;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.telephony.cdma.CdmaCellLocation;
 import android.telephony.gsm.GsmCellLocation;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import carnero.netmap.App;
 import carnero.netmap.R;
 import carnero.netmap.common.Constants;
 import carnero.netmap.common.Geo;
 import carnero.netmap.common.LocationUtil;
 import carnero.netmap.common.SimpleGeoReceiver;
 import carnero.netmap.fragment.NetMapFragment;
 import carnero.netmap.listener.OnLocationObtainedListener;
 import carnero.netmap.model.Bts;
 import carnero.netmap.model.BtsCache;
 import carnero.netmap.model.SectorCache;
 import com.google.android.gms.maps.model.LatLng;
 
 import java.util.List;
 
 public class MainActivity extends Activity implements SimpleGeoReceiver {
 
 	private Geo mGeo;
 	private TelephonyManager mTelephony;
 	private WifiManager mWiFi;
 	private LatLng mLastLocation;
 	private String[] mNetworkTypes;
 	private TextView mOperatorView;
 	private TextView mLocationView;
 	private TextView mNetworkView;
 	final private StatusListener mListener = new StatusListener();
 	final private LocationListener mLocationListener = new LocationListener();
 
 	@Override
 	public void onCreate(Bundle state) {
 		super.onCreate(state);
 
 		mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 
 		setContentView(R.layout.activity_main);
 
 		mOperatorView = (TextView) findViewById(R.id.network_operator);
 		mLocationView = (TextView) findViewById(R.id.network_location);
 		mNetworkView = (TextView) findViewById(R.id.network_type);
 
 		if (state == null) {
 			getFragmentManager()
 					.beginTransaction()
 					.replace(R.id.fragment_container, new NetMapFragment())
 					.commit();
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		mNetworkTypes = getResources().getStringArray(R.array.network_types);
 
 		mTelephony.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_DATA_ACTIVITY);
 
 		mGeo = App.getGeolocation();
 		mGeo.addReceiver(this);
 	}
 
 	@Override
 	public void onPause() {
 		mGeo.removeReceiver(this);
 
 		mTelephony.listen(mListener, PhoneStateListener.LISTEN_NONE);
 
 		super.onPause();
 	}
 
 	public void onLocationChanged(Location location) {
 		mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
 
 		getNetworkInfo();
 	}
 
 	public void getNetworkInfo() {
 		getNetworkInfo(null);
 	}
 
 	public void getNetworkInfo(CellLocation cell) {
 		if (mWiFi == null) {
 			mWiFi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		}
 
 		// final long time = System.currentTimeMillis();
 		// final int wifi = mWiFi.getWifiState();
 		final String operator = mTelephony.getNetworkOperator();
 		final String opName = mTelephony.getNetworkOperatorName();
 		// final boolean roaming = mTelephony.isNetworkRoaming();
 		// final int data = mTelephony.getDataState();
 		final int type = mTelephony.getNetworkType();
 
 		if (cell == null) {
 			cell = mTelephony.getCellLocation();
 		}
 
 		if (mLastLocation != null) {
 			final LatLng position = new LatLng(mLastLocation.latitude, mLastLocation.longitude);
 
 			SectorCache.update(LocationUtil.getSectorXY(position), type);
 		}
 
 		mOperatorView.setText(opName);
 		mNetworkView.setText(mNetworkTypes[type]);
 		if (cell instanceof GsmCellLocation) {
 			final GsmCellLocation gsmCell = (GsmCellLocation) cell;
 			final Bts bts = BtsCache.update(operator, gsmCell.getLac(), gsmCell.getCid(), type);
 
 			if (bts == null) {
 				mLocationView.setText(null);
 				mLocationView.setOnClickListener(null);
 			} else {
 				StringBuilder sb = new StringBuilder();
 				sb.append(Long.toString(bts.lac));
 				sb.append(":");
 				sb.append(Long.toHexString(bts.cid).toUpperCase());
 
 				mLocationView.setText(sb.toString());
 				mLocationView.setOnClickListener(new BtsClickListener(gsmCell.getCid()));
 
 				bts.getLocation(mLocationListener);
 			}
 		} else if (cell instanceof CdmaCellLocation) {
 			Log.w(Constants.TAG, "CDMA location not implemented");
 		}
 	}
 
 	// classes
 
 	public class StatusListener extends PhoneStateListener {
 
 		@Override
 		public void onCellLocationChanged(CellLocation cell) {
 			getNetworkInfo(cell);
 		}
 
 		@Override
 		public void onDataActivity(int direction) {
 			getNetworkInfo();
 		}
 
 		@Override
 		public void onCellInfoChanged(List<CellInfo> info) {
 			getNetworkInfo();
 		}
 	}
 
 	private class LocationListener implements OnLocationObtainedListener {
 
 		public void onLocationObtained(Bts bts) {
			if (bts.locationNew == null) {
 				return;
 			}
 
 			BtsCache.update(bts);
 		}
 	}
 
 	public class BtsClickListener implements View.OnClickListener {
 
 		private int mCid;
 
 		public BtsClickListener(int cid) {
 			mCid = cid;
 		}
 
 		@Override
 		public void onClick(View v) {
 			if (mCid <= 0) {
 				return;
 			}
 
 			String url = Constants.URL_BASE_GSMWEB + Integer.toHexString(mCid).toUpperCase();
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse(url));
 
 			startActivity(intent);
 		}
 	}
 }
