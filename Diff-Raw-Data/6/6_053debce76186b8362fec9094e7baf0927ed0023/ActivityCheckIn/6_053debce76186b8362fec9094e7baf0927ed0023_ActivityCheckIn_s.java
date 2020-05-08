 package com.coffeeandpower.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.Constants;
 import com.coffeeandpower.app.R;
 import com.coffeeandpower.RootActivity;
 import com.coffeeandpower.cache.CacheMgrService;
 import com.coffeeandpower.cache.CachedDataContainer;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.cont.UserShort;
 import com.coffeeandpower.cont.UserSmart;
 import com.coffeeandpower.cont.VenueSmart;
 import com.coffeeandpower.cont.VenueSmart.CheckinData;
 import com.coffeeandpower.imageutil.ImageLoader;
 import com.coffeeandpower.location.LocationDetectionStateMachine;
 import com.coffeeandpower.maps.MyItemizedOverlay2;
 import com.coffeeandpower.utils.Executor;
 import com.coffeeandpower.utils.UserAndTabMenu;
 import com.coffeeandpower.utils.Executor.ExecutorInterface;
 import com.coffeeandpower.utils.Utils;
 import com.coffeeandpower.views.CustomDialog;
 import com.coffeeandpower.views.CustomFontView;
 import com.coffeeandpower.views.CustomSeek;
 import com.coffeeandpower.views.CustomSeek.HoursChangeListener;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class ActivityCheckIn extends RootActivity implements Observer {
 
 	// Map items
 	private MapView mapView;
 	private MapController mapController;
 	private MyItemizedOverlay2 itemizedoverlay;
 
     private ProgressDialog progress;
 
 	private VenueSmart venue;
 
 	// Views
 	private CustomFontView textHours;
 	private CustomFontView textTitle;
 	private CustomFontView textName;
 	private CustomFontView textStreet;
 	private CustomSeek hoursSeek;
 
 	private RelativeLayout layoutCheckedInUsers;
 	private LinearLayout layoutForInflate;
 	private LinearLayout layoutPopUp;
 
 	private EditText statusEditText;
 	
 	private Context context;
 
     private int checkInDuration = 1; // default 1 hour checkin duration, slider
                                      // sets other values
 
 	private DataHolder result;
 
     // TODO
     // Eliminate these and make them local variables
 	ArrayList<UserSmart> usersArray;
     // This needs to be a member variable due to click callback
 	ArrayList<UserShort> checkedInUsers;
 
 	private Executor exe;
 
     // Scheduler - create a custom message handler for use in passing userdata
     // data from background API call to main thread
 	protected Handler taskHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			usersArray = msg.getData().getParcelableArrayList("users");
             // FIXME
             // For now we are going to convert from UserSmart to UserShort
             // Eventually UserShort should just be eliminated
 			checkedInUsers = convertUserSmart2UserShort(usersArray);
 			populateUsersIfExist();
 			
 			// Deregister since we only want to get a single data update in the checkin view
 			// multiple data updates will result in the checked in users getting replicated
 			CacheMgrService.stopObservingAPICall("venuesWithCheckins",ActivityCheckIn.this);
 			
 			super.handleMessage(msg);
 		}
 	};
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setContentView(R.layout.activity_check_in);
 
 		usersArray = new ArrayList<UserSmart>();
 		checkedInUsers = new ArrayList<UserShort>();
 
 		context = this;
         this.progress = new ProgressDialog(context);
 
 		// Executor
 		
 		exe = new Executor(ActivityCheckIn.this);
 		exe.setExecutorListener(new ExecutorInterface() {
 			@Override
 			public void onErrorReceived() {
 				errorReceived();
 			}
 
 			@Override
 			public void onActionFinished(int action) {
                 actionFinished(action);
 			}
 		});
 
 		// Get Data from Intent
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			venue = (VenueSmart) extras.getParcelable("venue");
 		} else {
 			venue = new VenueSmart();
 		}
 
 		// Views
 		textTitle = (CustomFontView) findViewById(R.id.text_title);
 		textHours = (CustomFontView) findViewById(R.id.textview_hours);
 		textName = (CustomFontView) findViewById(R.id.textview_name);
 		textStreet = (CustomFontView) findViewById(R.id.textview_street);
 		hoursSeek = (CustomSeek) findViewById(R.id.seekbar_hours);
 		statusEditText = (EditText) findViewById(R.id.edittext_optional);
 		layoutCheckedInUsers = (RelativeLayout) findViewById(R.id.layout_name);
 		layoutForInflate = (LinearLayout) findViewById(R.id.inflate_users);
 		layoutPopUp = (LinearLayout) findViewById(R.id.layout_popup_info);
 		mapView = (MapView) findViewById(R.id.imageview_mapview);
         Drawable drawable = this.getResources().getDrawable(
                 R.drawable.map_marker_iphone);
 		itemizedoverlay = new MyItemizedOverlay2(drawable);
 
 		// Views states
 		textTitle.setText(venue.getName());
 		textStreet.setText(venue.getAddress());
 		textName.setText(venue.getName());
 		layoutCheckedInUsers.setVisibility(View.GONE);
 		layoutPopUp.setVisibility(View.GONE);
 
 		// Set others
 		mapView.setClickable(false);
 		mapView.setEnabled(false);
 		mapController = mapView.getController();
 		mapController.setZoom(18);
 
 		// Navigate map to location from intent data
         GeoPoint point = new GeoPoint((int) (venue.getLat() * 1E6),
                 (int) (venue.getLng() * 1E6));
         GeoPoint pointForCenter = new GeoPoint(
                 point.getLatitudeE6()
                         + Utils.getScreenDependentItemSize(Utils.MAP_VER_OFFSET_FROM_CENTER),
                 point.getLongitudeE6()
                         - Utils.getScreenDependentItemSize(Utils.MAP_HOR_OFFSET_FROM_CENTER));
 		mapController.animateTo(pointForCenter);
 		createMarker(point);
 
 		// Listener for Hours change on SeekBar
 		hoursSeek.setOnHoursChangeListener(new HoursChangeListener() {
 			@Override
 			public void onHoursChange(int hours) {
 				switch (hours) {
 				case 1:
 					textHours.setText(hours + " hour");
 					checkInDuration = 1;
 					break;
 
 				default:
 					textHours.setText(hours + " hours");
 					checkInDuration = hours;
 					break;
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onStart() {
 		if (Constants.debugLog)
             Log.d("CheckIn", "ActivityCheckIn.onStart()");
 		super.onStart();
 
 		//UAirship.shared().getAnalytics().activityStarted(this);
 		CacheMgrService.startObservingAPICall("venuesWithCheckins",this);
 	}
 
 	@Override
 	public void onStop() {
 		if (Constants.debugLog)
             Log.d("CheckIn", "ActivityCheckIn.onStop()");
 		super.onStop();
 
 		//UAirship.shared().getAnalytics().activityStopped(this);
 		CacheMgrService.stopObservingAPICall("venuesWithCheckins",this);
 	}
 
 	private void createMarker(GeoPoint point) {
 		OverlayItem overlayitem = new OverlayItem(point, "", "");
 		itemizedoverlay.addOverlay(overlayitem);
 		if (itemizedoverlay.size() > 0) {
 			mapView.getOverlays().add(itemizedoverlay);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 
     private Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
 
             progress.dismiss();
 
             switch (msg.what) {
             case AppCAP.HTTP_ERROR:
                 new CustomDialog(context, "Error", result.getResponseMessage())
                         .show();
                 break;
 
             case UserAndTabMenu.HANDLE_CHECK_OUT:
                 CacheMgrService.checkOutTrigger();
                 DoCheckIn();
 
                 break;
 
 
             }
         }
 
     };
 	/**
 	 * Checkin me in venue
 	 * 
 	 * @param v
 	 */
 	public void onClickCheckIn(View v) {
 
         // FIXME
         // The Venue and VenueSmart classes still need to be unified, this is
         // designed for Venue,
         // but is being passed in as a VenueSmart
         // ((TextView)
         // findViewById(R.id.textview_check_in)).setText("Check Out");
         // ((ImageView)
         // findViewById(R.id.imageview_check_in_clock_hand)).setAnimation(AnimationUtils.loadAnimation(ActivityCheckIn.this,
 		//		R.anim.rotate_indefinitely));
         if (AppCAP.isUserCheckedIn()) {
             progress.setMessage(getResources().getString(R.string.message_checking_out));
             progress.show();
             AppCAP.setUserCheckedIn(false);
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                             result = AppCAP.getConnection()
                                     .checkOut();
                             handler.sendEmptyMessage(result
                                     .getHandlerCode());
                 }
             },"ActivityCheckIn.onClickCheckIn").start();
         } else {
             DoCheckIn();
         }
     }
 
     public void DoCheckIn() {
         final int checkInTime = (int) (System.currentTimeMillis() / 1000);
         final int checkOutTime = checkInDuration;
 		// If user has not already selected this venue for auto checkin, 
 		// Create Dialog to ask whether user wants to check in automatically at this venue
 		int[] venueList = AppCAP.getVenuesWithAutoCheckins();
 		boolean venueMatched = false;
 		for (int venueId:venueList) {
 			if (venue.getVenueId() == venueId) {
 				venueMatched = true;
 				break;
 			}
 		}
 		
 		if (!venueMatched) {
                 		
         		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
         		myAlertDialog.setTitle("Automatic Checkin");
         		myAlertDialog.setMessage("Do you want to check in to " + venue.getName() + " automatically?");
         		myAlertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
         			
         			 public void onClick(DialogInterface arg0, int arg1) {
         				 Log.d("CheckIn","User clicked YES");
         				 exe.checkIn(venue, checkInTime, checkOutTime, statusEditText.getText().toString(),true,false,context);
         				 LocationDetectionStateMachine.manualCheckin(getApplicationContext(),new Handler(), venue);
         		 	 }
         		});
         		myAlertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
         		       
                 		  public void onClick(DialogInterface arg0, int arg1) {
                 			  Log.d("CheckIn","User clicked NO");
                 			  exe.checkIn(venue, checkInTime, checkOutTime, statusEditText.getText().toString(),false,false,context);
                 		  }
         		});
         		myAlertDialog.show();
 		} else {
 			// If user already selected autocheckin, just check them in silently
 			exe.checkIn(venue, checkInTime, checkOutTime, statusEditText.getText().toString(),false,false,context);
 		}
 		
 		
 		
 	}
 	
 	private ArrayList<UserShort> convertUserSmart2UserShort(ArrayList<UserSmart> userList) {
 		ArrayList<UserShort> shortUsers = new ArrayList<UserShort>();
         for (CheckinData currCheckedIn : venue.getArrayCheckins()) {
 			for (UserSmart currSmartUser : userList) {
                 if (currCheckedIn.getUserId() == currSmartUser.getUserId()) {
                     if (currCheckedIn.getCheckedIn() == 1) {
                         // (int id, String nickName, String statusText, String
                         // about, String joinDate, String imageURL, String
                         // hourlyBilingRate)
 
                         shortUsers
                                 .add(new UserShort(currSmartUser.getUserId(),
                                         currSmartUser.getNickName(),
                                         currSmartUser.getStatusText(),
                                         "About Me", "Join Date", currSmartUser
                                                 .getFileName(), "NA"));
 					}
 					break;
 				}
 			}
 
 		}
 
         return shortUsers;
 	}
 
 	private void populateUsersIfExist() {
 		if (checkedInUsers != null) {
 			if (checkedInUsers.size() > 0) {
 
 				layoutCheckedInUsers.setVisibility(View.VISIBLE);
 				layoutPopUp.setVisibility(View.VISIBLE);
 				ImageLoader imageLoader = new ImageLoader(this);
 
 				// Set text on first view
                 ((TextView) layoutPopUp.getChildAt(0)).setText(checkedInUsers
                         .get(0).getNickName());
 				String status = checkedInUsers.get(0).getStatusText();
 				status = status.length() < 1 ? "No status set..." : status;
                 ((TextView) layoutPopUp.getChildAt(1)).setText(AppCAP
                         .cleanResponseString(status));
 
 				for (int i = 0; i < checkedInUsers.size(); i++) {
 
 					ImageView image = new ImageView(this);
 					image.setPadding(10, 10, 10, 10);
 					image.setTag(i);
 					image.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
                             ((TextView) layoutPopUp.getChildAt(0))
                                     .setText(checkedInUsers.get(
                                             (Integer) v.getTag()).getNickName());
 
                             String status = checkedInUsers.get(
                                     (Integer) v.getTag()).getStatusText();
                             status = status.length() < 1 ? "No status set..."
                                     : status;
                             ((TextView) layoutPopUp.getChildAt(1))
                                     .setText(AppCAP.cleanResponseString(status));
 						}
 					});
 
                     imageLoader.DisplayImage(checkedInUsers.get(i)
                             .getImageURL(), image, R.drawable.default_avatar50,
                             70);
 					layoutForInflate.addView(image);
 
 				}
 			}
 		}
 	}
 
 	private void errorReceived() {
 
 	}
 
 	private void actionFinished(int action) {
 		result = exe.getResult();
 
 		switch (action) {
 
 		case Executor.HANDLE_CHECK_IN:
 			CacheMgrService.checkInTrigger(venue);
 			setResult(AppCAP.ACT_QUIT);
 			AppCAP.setUserCheckedIn(true);
 			ActivityCheckIn.this.finish();
 			break;
         case AppCAP.ERROR_SUCCEEDED_SHOW_MESS:
             if (result != null) {
                 new CustomDialog(ActivityCheckIn.this,
                         "Error Checkin",
                         result.getResponseMessage()).show();
             }
             break;
 
 		}
 	}
 
 	public void onClickBack(View v) {
 		onBackPressed();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		/*
 		 * verify that the data is really of type CounterData, and log the
 		 * details
 		 */
 		if (data instanceof CachedDataContainer) {
 			CachedDataContainer counterdata = (CachedDataContainer) data;
 			DataHolder result = counterdata.getData();
 
 			Object[] obj = (Object[]) result.getObject();
 			@SuppressWarnings("unchecked")
 			List<UserSmart> arrayUsers = (List<UserSmart>) obj[1];
 
 			Message message = new Message();
 			Bundle bundle = new Bundle();
 			bundle.putCharSequence("type", counterdata.type);
 			bundle.putParcelableArrayList("users", new ArrayList<UserSmart>(arrayUsers));
 
 			message.setData(bundle);
 
 			if (Constants.debugLog)
                 Log.d("CheckIn",
                         "ActivityCheckIn.update: Sending handler message...");
 			taskHandler.sendMessage(message);
         } else if (Constants.debugLog)
             Log.d("CheckIn", "Error: Received unexpected data type: "
                     + data.getClass().toString());
 		}
 }
