 package net.djmacgyver.bgt.activity;
 
 import net.djmacgyver.bgt.R;
 import net.djmacgyver.bgt.gps.GPSTrackingService;
 import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
 import net.djmacgyver.bgt.keepalive.KeepAliveThread;
 import net.djmacgyver.bgt.map.MapHandler;
 import net.djmacgyver.bgt.map.RouteOverlay;
 import net.djmacgyver.bgt.session.Session;
 import net.djmacgyver.bgt.socket.HttpSocketConnection;
 import net.djmacgyver.bgt.socket.HttpSocketListener;
 import net.djmacgyver.bgt.socket.SocketService;
 import net.djmacgyver.bgt.user.User;
 
 import org.json.JSONObject;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.TextView;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 public class Map extends MapActivity implements KeepAliveTarget {
 	private MyLocationOverlay myLoc;
 	private KeepAliveThread refresher;
 	private RouteOverlay route;
 	private GPSTrackingService service;
 	private boolean bound = false;
 	private MapView map;
 	private MapHandler handler = new MapHandler(this);
 	private Handler stateHandler = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 			int state = (Integer) msg.obj;
 			if (state == HttpSocketConnection.STATE_CONNECTED) {
 				removeDialog(DIALOG_CONNECTING);
 			} else {
 				showDialog(DIALOG_CONNECTING);
 				handler.getUserOverlay().reset();
 				getLengthTextView().setText("n/a");
 				getSpeedTextView().setText("n/a");
 				getCycleTimeTextView().setText("n/a");
 			}
 		}
 	};
 	private HttpSocketListener listener = new HttpSocketListener() {
 		@Override
 		public void receiveUpdate(JSONObject data) {
 			Message msg = new Message();
 			msg.obj = data;
 			handler.sendMessage(msg);
 		}
 
 		@Override
 		public void receiveCommand(String command, JSONObject data) {
 		}
 
 		@Override
 		public void receiveStateChange(int newState) {
 			Message msg = new Message();
 			msg.obj = newState;
 			stateHandler.sendMessage(msg);
 		}
 	};
 	
 	public static final int DIALOG_CONNECTING = 1;
 	
 	// GPSListener Service connection
     private ServiceConnection conn = new ServiceConnection() {
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			service = null;
 			if (!hasLocationOverlay()) return;
 	    	getMap().getOverlays().remove(getLocationOverlay());
 	    	getLocationOverlay().disableMyLocation();
 	    	setLocationOverlay(null);
 		}
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder binder) {
 			service = ((GPSTrackingService.LocalBinder) binder).getService();
 			if (!service.isEnabled()) return;
 			setLocationOverlay(new MyLocationOverlay(getApplicationContext(), getMap()));
 			getLocationOverlay().enableMyLocation();
 	    	getMap().getOverlays().add(getLocationOverlay());
 		}
 	};
 	
 	private HttpSocketConnection socket;
 	private SocketService sockService;
 	
 	// SocketService connection
 	private ServiceConnection sconn = new ServiceConnection() {
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 		}
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder s) {
 			sockService = ((SocketService.LocalBinder) s).getService();
 			socket = sockService.getSharedConnection(Map.this);
 			onConnect();
 		}
 	};
 	
 	public MapView getMap() {
 		if (map == null) {
 			map = (MapView) findViewById(R.id.mapview);
 		}
 		return map;
 	}
 	
 	public RouteOverlay getRoute() {
 		if (route == null) {
 			route = new RouteOverlay(getMap());
 		}
 		return route;
 	}
 	
 	private MyLocationOverlay getLocationOverlay() {
 		return myLoc;
 	}
 	
 	private void setLocationOverlay(MyLocationOverlay myLoc) {
 		this.myLoc = myLoc;
 	}
 	
 	private boolean hasLocationOverlay() {
 		return myLoc != null;
 	}
 	
 	public TextView getLengthTextView() {
 		return (TextView) findViewById(R.id.bladeNightLength);
 	}
 	
 	public TextView getSpeedTextView() {
 		return (TextView) findViewById(R.id.bladeNightSpeed);
 	}
 	
 	public TextView getCycleTimeTextView() {
 		return (TextView) findViewById(R.id.bladeNightCycleTime);
 	}
 	
 	public TextView getTitleTextView() {
 		return (TextView) findViewById(R.id.title);
 	}
 	
 	private KeepAliveThread getRefresher()
 	{
 		if (refresher == null) {
 			refresher = new KeepAliveThread(this, 5);
 		}
 		return refresher;
 	}
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         setContentView(R.layout.map);
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
         TextView t = (TextView) findViewById(R.id.title);
         t.setText(R.string.map_name);
     	
     	getMap().setBuiltInZoomControls(true);
     	
     	getMap().getOverlays().add(getRoute());
 
         bindService(new Intent(this, GPSTrackingService.class), conn, Context.BIND_AUTO_CREATE);
         bound = true;
     }
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (bound) {
 			unbindService(conn);
 			bound = false;
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return true;
 	}
 	
 	@Override
 	protected boolean isLocationDisplayed() {
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		showDialog(DIALOG_CONNECTING);
         bindService(new Intent(this, SocketService.class), sconn, Context.BIND_AUTO_CREATE);
     	getRefresher().start();
     	if (service != null && hasLocationOverlay() && service.isEnabled()) {
     		getLocationOverlay().enableMyLocation();
     	}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (hasLocationOverlay()) getLocationOverlay().disableMyLocation();
 		getRefresher().terminate();
 		this.refresher = null;
 		
 		if (socket != null) {
			socket.unSubscribeUpdates(new String[]{"movements", "map", "stats", "quit"});
 			socket.removeListener(listener);
 			sockService.removeStake(this);
 			unbindService(sconn);
 		}
 	}
 
 	@Override
 	public void keepAlive(KeepAliveThread source) {
 		if (source != getRefresher()) return;
 		runOnUiThread(new Runnable() {
 			public void run() {
 				getMap().invalidate();
 			}
 		});
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 			case DIALOG_CONNECTING:
 				Dialog d = new ProgressDialog(this);
 				d.setCancelable(false);
 				d.setTitle(R.string.connect_progress);
 				return d;
 		}
 		return super.onCreateDialog(id);
 	}
 	
 	public void onConnect() {
 		socket.addListener(listener);
		socket.addListener(handler);
		socket.subscribeUpdates(new String[]{"movements", "map", "stats", "quit"});
 		if (socket.getState() == HttpSocketConnection.STATE_CONNECTED) removeDialog(Map.DIALOG_CONNECTING);
 	}
 	
 	public void onDisconnect() {
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		User u = Session.getUser();
 		if (u == null || !u.isAdmin()) return false;
 		getMenuInflater().inflate(R.menu.map, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.mapselection:
 				Intent i = new Intent(getApplicationContext(), MapSelection.class);
 				startActivity(i);
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void onBeforeConnect() {
 		if (!this.isFinishing()) showDialog(Map.DIALOG_CONNECTING);
 	}
 }
