 package uk.co.mentalspace.android.bustimes.displays.metawatch;
 
 import java.util.List;
 
 import uk.co.mentalspace.android.bustimes.BusTime;
 import uk.co.mentalspace.android.bustimes.BusTimeRefreshService;
 import uk.co.mentalspace.android.bustimes.Location;
 import uk.co.mentalspace.android.bustimes.LocationManager;
 import uk.co.mentalspace.android.bustimes.Renderer;
 import uk.co.mentalspace.android.bustimes.WakefulIntentService;
 import uk.co.mentalspace.android.bustimes.utils.LocationTracker;
 import android.content.Intent;
 import android.util.Log;
 
 public class MetaWatchService extends WakefulIntentService {
 	private static final String LOGNAME = "MetaWatchService";
 	
 	private static final int BUTTON_NEXT_LOCATION = 5;
 	
 	private static Location loc = null;
 	private LocationTracker posTracker = null;
 	
 	public MetaWatchService() {
 		super("MetaWatchService");
 	}
 	
 	protected LocationTracker getPosTracker() {
 		if (null == posTracker) {
 			posTracker = new LocationTracker(getApplicationContext());
 		}
 		return posTracker;
 	}
 	
 	public void processIntent(Intent intent) {
 		if (null == posTracker) {
 			Log.d(LOGNAME, "Location Tracker not yet initialised - initialising...");
 			posTracker = new LocationTracker(getApplicationContext());
 		}
 		
 		try {
 			Log.d(LOGNAME, "Meta Watch Service handling intent");
 			final String action = intent.getAction();
 			if (MetaWatchReceiver.MW_ACTIVATED.equals(action)) {
 				Log.d(LOGNAME, "MetaWatch app activated, handing over to Coordinator");
 				MetaWatchDisplay mwd = new MetaWatchDisplay(getApplicationContext());
 				if (null == loc) {
 					LocationTracker pt = getPosTracker();
 					int lat = (int)(pt.getLatitude()*10000);
 					int lon = (int)(pt.getLongitude()*10000);
 					loc = LocationManager.getNearestSelectedLocation(getApplicationContext(), lat, lon);
 				}
 
 				getBusTimes(mwd, loc); 
 			}
 			else if (MetaWatchReceiver.MW_DEACTIVATED.equals(action)) {
 				Log.d(LOGNAME, "MetaWatch app deactivated, displaying blank screen and terminating");
 				MetaWatchDisplay mwd = new MetaWatchDisplay(getApplicationContext());
 				mwd.displayMessage(null, "", MetaWatchDisplay.MESSAGE_NORMAL);
 				//GPS will auto-disconnect when this function terminates
 			}
 			else if (MetaWatchReceiver.MW_BUTTON.equals(action)) {
 				int btnId = intent.getIntExtra("button", -1);
 				Log.d(LOGNAME, "Metawatch Button ["+btnId+"] pressed");
 				if (BUTTON_NEXT_LOCATION == btnId) {
 					MetaWatchDisplay mwd = new MetaWatchDisplay(getApplicationContext());
 					Log.d(LOGNAME, "Getting next location. Current: "+loc.getLocationName());
 					
 					loc = LocationManager.getNextLocation(getApplicationContext(), loc);
 					Log.d(LOGNAME, "Next Location: "+loc.getLocationName());
 					getBusTimes(mwd, loc);
 				} else {
 					Log.d(LOGNAME, "Wrong button. "+BUTTON_NEXT_LOCATION+" != "+btnId);
 				}
 			}
 			else if (BusTimeRefreshService.ACTION_LATEST_BUS_TIMES.equals(action)) {
 				if (null == loc) {
 					Log.w(LOGNAME, "Received updated bus times, but no location selected.  Ignoring");
 				} else {
 					long locId = intent.getLongExtra(BusTimeRefreshService.EXTRA_LOCATION_ID, -1);
 					String srcId = intent.getStringExtra(BusTimeRefreshService.EXTRA_SOURCE_ID);
 					
 					if (loc.getId() != locId || !loc.getSourceId().equals(srcId)) {
 						Log.w(LOGNAME, "Received updated bus times, but for a different location than selected.  Ignoring.");
 					} else {
 						@SuppressWarnings("unchecked")
 						List<BusTime> busTimes = (List<BusTime>)intent.getSerializableExtra(BusTimeRefreshService.EXTRA_BUS_TIMES);
 						
 						int busTimesSize = (null == busTimes) ? -1 : busTimes.size();
 						Log.d(LOGNAME, "Received ["+busTimesSize+"] bus times");
 
 						MetaWatchDisplay mwd = new MetaWatchDisplay(getApplicationContext());
 						mwd.displayBusTimes(loc, busTimes);
 					}
 				}
 			}
 			else {
 				Log.d(LOGNAME, "Unrecognised intent action: "+action);
 			}
		} catch (Exception e) {
			Log.e(LOGNAME, "Unknown exception: ", e);
 		}
		terminate();
 	}
 	
 	public void getBusTimes(Renderer display, Location loc) {
 		Intent service = new Intent(this, BusTimeRefreshService.class);
 		service.setAction(BusTimeRefreshService.ACTION_REFRESH_BUS_TIMES);
 		service.putExtra(BusTimeRefreshService.EXTRA_LOCATION_ID, loc.getId());
 		service.putExtra(BusTimeRefreshService.EXTRA_SOURCE_ID, loc.getSourceId());
 		this.startService(service);
 
 		Log.d(LOGNAME, "Initiating request of bus times for location: "+loc);
 		display.displayMessage(loc, "fetching bus times...", Renderer.MESSAGE_NORMAL);
 	}
 
 	public void terminate() {
 		if (null != posTracker) {
 			posTracker.stopTrackingLocation();
 		}
 		posTracker = null;
 	}
 
 	
 }
