 package com.xdev.obliquity;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Vibrator;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.fedorvlasov.lazylist.ImageLoader;
 import com.google.android.apps.analytics.easytracking.EasyTracker;
 import com.google.android.apps.analytics.easytracking.TrackedActivity;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
 import com.xdev.obliquity.JsonResponse.Events;
 
 public class ActivityEvent extends TrackedActivity {
 
 	private final String TAG = Config.TAG_EVENT;
 	private final boolean DEBUG = Config.DEBUG_EVENT;
 	
 	Context mContext;
 	
 	Handler mHandler; // Feed Handler
 	Obliquity appState; // Application Subclass
 	DownloadHandler dlHandler; // Reference to Download Manager
 	Vibrator mVibrator;
 	Util mUtil;
 	EventListAdapter adapter; // Adapter
 	
 	PullToRefreshExpandableListView list;
 	ExpandableListView mActualList;
 	
 	boolean listInitialized = false;
 	boolean refreshing = false;
 	
 	// LIFECYCLE FUNCTIONS
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.ac_event);
     
         appState = (Obliquity)getApplication();
         mUtil = appState.getUtil();
         mContext = this;
         mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         
         // Handles Messages from the Obliquity class (Application class)
         mHandler = new Handler() {
         	@Override
 			public void handleMessage(Message message) {
         		Log.i(TAG, "Message Recieved what : "+ message.what);
         		switch(message.what) {
         			case 1:
         				downloadSuccessful();
         				break;
         			case 0:
         				downloadFailed();
         				break;
         			case 2: // isOnline returns false
         			case 99: // Connected yet server not responding
         				noInternetAccess(message.what);
         				break;
         			case 11: 
         				cacheSuccessful();
         				break;
         			// Cache Failed would not propogate, Handled by Obliquity class
         		}	
         	}
         };
         
         if(DEBUG) Log.d(TAG, "Event Subscribing");
         
 		// Second boolean, ImmediateAction
 		dlHandler = appState.getDownloadHandler(mHandler, mUtil.getBoolean(Config.PREF_C2DM_STATUS, true), true); // Subscribe to the Download Manager
 		
 	}
 	
 	@Override
 	public void onResume() { // TODO, immideateAction POSSIBLE LOOPHOLE
 		super.onResume();
 		
 		if(!mUtil.isOnline())
 			makeToast("No internet access. These may not be upto date.", Toast.LENGTH_LONG);
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		Log.d(TAG, "Event unsubscribing");
 		appState.unsubscribe();
 	}
 	
 	// END LIFECYCLE FUNCTIONS
 	
 	// Handles refresh calls
 	public void refresh() {
 		mVibrator.vibrate(Config.REFRESH_VIBRATE_DURATION);
 		
 		// InternetAccess will be handled by application & DataHandler. Handled Here to avoid wasteful function calls
 		if(mUtil.isOnline()) {
 			refreshData();
 		} else {
 			list.onRefreshComplete();
 			noInternetAccess(2); // TODO : What is this about :s
 		}
 	}
 	
 	
 	// Called by Obliquity(APP) on failed download
 	private void downloadFailed() {
 		makeToast("We encountered an error while trying to refresh.", Toast.LENGTH_SHORT);
 		
 		refreshing = false;
 		if(list != null)
 			list.onRefreshComplete();
 		
 		if(askLoadCache())
 			dlHandler.loadCache();
 		else
 			displayEmpty();
 	}
 	
 	// Called by Obliquity(APP) on successful download
 	private void downloadSuccessful() {
 		setAdapter(dlHandler.getEvents());
 	}
 	
 	// Called by Obliquity on successful cache load
 	private void cacheSuccessful() {
 		setAdapter(dlHandler.getCachedEvents());
 		
 	}
 	
 	// Asks User weather to load data from Cache
 	private boolean askLoadCache() {
 		return true; // TODO ask user weather to load cache ? or load anyway
 	}
 	
 	// No Cache or Downloaded data to display. modify UI and alert user
 	public void displayEmpty() {
 		makeToast("Please connect to the internet and hit refresh!", Toast.LENGTH_LONG);
 	}
 	
 	// No Internet Access. Load Cache if possible. Else call displayEmpty()
 	public void noInternetAccess(int what) {
 		if(DEBUG) Log.e(TAG, "No Internet Access, What : " + what);
 		
 		switch(what) {
 			case 2:
 				makeToast("Cannot refresh without an active internet connection.", Toast.LENGTH_SHORT);
 				break;
 			case 99:
 				makeToast("Sorry, We are facing some issues right now. Please try again later. ", Toast.LENGTH_LONG);
 		}
 		
 		refreshing = false;
 		if(list != null)
 			list.onRefreshComplete();
 	}
 	
 	private void makeToast(String text, int duration) {
 		Toast.makeText(mContext, text, duration).show();
 	}
 	
 	private void refreshData() {
 		dlHandler.refresh();
 	}
 	
 	
 	// -----------------------------------------------------------------------
 	// Initialize Adapter
 	private void setAdapter(List<Events> values) {
 		if(DEBUG) Log.i(TAG, "Setting up Adapter with count : " + values.size());
 		
 		if(listInitialized) { // Returning from refresh()
 			adapter.changeDataSet(values);
 			if(refreshing) {
 				makeToast("These events are minty fresh now!", Toast.LENGTH_SHORT);
 				Date d = new Date();
 				CharSequence s  = DateFormat.format("hh:mm a", d.getTime());
 				list.onRefreshComplete();
 				list.setLastUpdatedLabel("Last Updated : " + s);
 				refreshing = false;
 			}
 		} else {
 			adapter = new EventListAdapter(mContext, values, appState.getServerQueue());
 			list = (PullToRefreshExpandableListView)findViewById(R.id.expandableList);
 			mActualList = (ExpandableListView) list.getRefreshableView();
 			
 			list.setOnRefreshListener(new OnRefreshListener(){
 				@Override
 				public void onRefresh() {
 					refreshing = true;
 					refresh();
 					
 				}
 			});
 			
 			mActualList.setAdapter(adapter);
 			adapter.init();
 			listInitialized = true;
 		}
 	}
 	
 	// ADAPTER
 	private class EventListAdapter extends BaseExpandableListAdapter implements OnClickListener{
     	private final String TAG = "--Event--Adapter";
     	private final String thumbURL = "http://obliquityindia.netau.net/AndroidCP/img/events/thumbs/";
     	private final String imageURL = "http://obliquityindia.netau.net/AndroidCP/img/events/";
     	
     	private Context mContext;
     	private ServerQueue mSQueue;
     	private EasyTracker tracker;
     	private ImageLoader imgLoader;
     	private List<Events> values;
     	private List<String> rsvpHistory;
     	
     	private class pViewHolder {
         	public TextView title;
         	public TextView date;
         	public ImageView thumb;
         	public ImageView rsvpIcon;
         }
         
         private class cViewHolder {
         	public TextView description;
         	public Button rsvp;
         	public ImageView eventImage;
         }
     	
     	// Constructor
     	public EventListAdapter(Context context, List<Events> values, ServerQueue mSQueue) {
     		Log.d(TAG, "Adapter Constructor");
     		this.mContext = context;
     		this.values = values;
     		this.mSQueue = mSQueue;
     		
     		// ImageLoader(context, image_loading_stub_R_Id, REQUIRED_SIZE)
     		imgLoader = new ImageLoader(context, 0);
     		tracker = EasyTracker.getTracker();
     		
     		setRSVPHistory();
     	}
     	
     	// Called when Adapter is bound to listview
     	public void init() {
     		mActualList.setOnItemLongClickListener(new OnItemLongClickListener() {
 
     			@Override
     			public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
     				tracker.trackEvent("Events", "LongClick", "Event ShareWith", 1);
     				
     				int groupPosition = ExpandableListView.getPackedPositionGroup(id);
     				Events event = values.get(groupPosition);
     				
     				StringBuilder sb = new StringBuilder();
     				sb.append("Obliquity's Event on (");
     				sb.append(event.date);
     				sb.append(")\n");
     				sb.append("Event Title : ");
     				sb.append(event.title);
     				sb.append("\n");
     				sb.append("Event Description : ");
     				sb.append(event.description);
     				sb.append("\n");
     		          
     				Intent intent = new Intent(Intent.ACTION_SEND);
     			    intent.setType("text/plain");
     			    intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
     			    startActivity(Intent.createChooser(intent, "Share with"));
     				
     				return true;
     			}
     		});
     	}
     	
     	// Gets RSVP History
     	public void setRSVPHistory() {
     		String q = mUtil.getString(Config.PREF_RSVP_HISTORY, "");
         	if(DEBUG) Log.d(TAG, "RSVPHistory : " + q);
         	
         	String[] h = q.split(";");
         	
         	rsvpHistory = new ArrayList<String>();
         	
         	for(int i = 0; i < h.length; i++) { rsvpHistory.add(h[i]); }
         	
         	if(DEBUG) Log.d(TAG, "Queue array : " + Arrays.toString(h));
     	}
     	
     	// Swaps the dataset
     	public void changeDataSet(List<Events> values) {
     		this.values = values;
     		this.notifyDataSetChanged();
     	}
     	
     	// Parent List
     	int mEventId;
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 			Log.i(TAG, "GroupPosition called : " + groupPosition);
 			View rowView = convertView;
 			mEventId = values.get(groupPosition).eventId;
     		pViewHolder holder;
     		
     		if (rowView == null) {
     			LayoutInflater inflater = (LayoutInflater) mContext
     					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     			rowView = inflater.inflate(R.layout.event_row, parent, false);
     			
     			holder = new pViewHolder();
     			holder.title = (TextView) rowView.findViewById(R.id.eventTitle);
     			holder.date = (TextView) rowView.findViewById(R.id.eventDate);
     			holder.thumb = (ImageView) rowView.findViewById(R.id.thumb);
     			holder.rsvpIcon = (ImageView) rowView.findViewById(R.id.rsvpIcon);
     			rowView.setTag(holder);
     		} else {
     			holder = (pViewHolder)rowView.getTag();
     		}
     		
     		holder.title.setText(values.get(groupPosition).title);
     		holder.date.setText(values.get(groupPosition).date);
     		
     		// RSVP'd Icon
     		if(contains(mEventId)) {
     			holder.rsvpIcon.setVisibility(View.VISIBLE);
     		} else {
     			holder.rsvpIcon.setVisibility(View.GONE);
     		}
     		
     		if(values.get(groupPosition).hasImage == 1) {
     			if(DEBUG) Log.d(TAG, "HasImage for : " + groupPosition);
    			//DisplayImage(URL, ImageView, RequiredSize)
    			imgLoader.DisplayImage(thumbURL + values.get(groupPosition).eventId + ".jpg", holder.thumb, 100);
     		} else {
     			// TODO : If noticed a lag, try with setting a tag with a boolean check to avoid reassigning everytime. 
     			holder.thumb.setImageResource(R.drawable.nothumb);
     		}
     		
     		return rowView;
 		}
 		
 		// Child Row
     	View childView;
 		cViewHolder holder;
 		int event;
 		@Override
 		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 			childView = convertView;
     		event = values.get(groupPosition).eventId;
     		
     		if (childView == null) {
     			LayoutInflater inflater = (LayoutInflater) mContext
     					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     			childView = inflater.inflate(R.layout.event_child, parent, false);
     			
     			holder = new cViewHolder();
     			holder.description = (TextView) childView.findViewById(R.id.eventDescription);
     			holder.rsvp = (Button) childView.findViewById(R.id.btnRSVP);
     			holder.eventImage = (ImageView)childView.findViewById(R.id.eventImage);
     			holder.eventImage.setOnClickListener(mListener);
     			holder.rsvp.setOnClickListener(this);
     			childView.setTag(holder);
     		} else {
     			holder = (cViewHolder)childView.getTag();
     		}
     	
     		holder.description.setText(values.get(groupPosition).description);
 			holder.rsvp.setTag(R.string.key_eventid, event);
 			
     		if(contains(event)) {
     			holder.rsvp.setText("Cancel");
     			holder.rsvp.setTag(R.string.key_rsvp, true);
     		} else {
     			holder.rsvp.setText("Attending");
     			holder.rsvp.setTag(R.string.key_rsvp, false);
     		}
     		
     		if(values.get(groupPosition).hasImage == 1) {
    			imgLoader.DisplayImage(imageURL + event + ".jpg", holder.eventImage, 100);
     		} else {
     			holder.eventImage.setImageResource(R.drawable.nothumb); // TODO : Set it correctly
     		}
     		
     		holder.eventImage.setTag(groupPosition);
     		
     		return childView;
 		}
 		
 		// Checks if number is in RSVP list
 		public boolean contains(int event) {
 			if(rsvpHistory.contains(Integer.toString(event))) {
 				Log.i(TAG, "rsvpHistory true event : " + event);
 				return true;
 			} else {
 				Log.i(TAG, "rsvpHistory false event : " + event);
  				return false;
 			}
 		}
 		
 		// OnClickListener for the image
 		OnClickListener mListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String URL = values.get((Integer) v.getTag()).largerImageURL;
 				Intent mIntent = new Intent(mContext, ActivityImageViewer.class);
 				mIntent.putExtra("URL", URL);
 				startActivity(mIntent);
 			}
 		};
 		
     	// OnClick RSVP
     	@Override
     	public void onClick(final View v) {
     		
     		if(!mUtil.isOnline()) { // TODO : Maybe cache isOnline result to avoid extra cycles
     			Toast.makeText(mContext, "Cannot connect to the server", Toast.LENGTH_SHORT).show();
     			return;
     		}
     		
     		int event = (Integer) v.getTag(R.string.key_eventid);
     		boolean rsvp = (Boolean) v.getTag(R.string.key_rsvp);
     		
     		if(DEBUG) Log.d(TAG, "RSVP Button rsvp : " + rsvp + "| event : " + event);
     		
     		if(rsvp) {
     			
     			dialogConfirmRemoveRsvp(v);
     			
     		} else {
     			
 	    		if(!mUtil.getBoolean(Config.PREF_USER_DETAILS, false))  
 	    			dialogRedirectToPreference(v); // If user details are incomplete
 	    		else
 	    			dialogConfirmRsvp(v); // Shows a confirmation dialog. which further propagates on button_positive
 	    		
     		}
     	}
     	
     	// Asks for rsvp confirmation
     	public void dialogConfirmRsvp(final View v) {
     		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			    @Override
 			    public void onClick(DialogInterface dialog, int which) {
 			        switch (which){
 			        case DialogInterface.BUTTON_POSITIVE:
 			            dialogRsvpFriends(v);
 			            break;
 
 			        case DialogInterface.BUTTON_NEGATIVE:
 			            return;
 			        }
 			    }
 			};
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 			builder.setMessage("Are you attending this event?").setPositiveButton("Let's Party!", dialogClickListener)
 			    .setNegativeButton("No", dialogClickListener).show();
     	}
     	
 		// Check if UserDetails is completed. If not asks for completion and redirects to preferences
     	public void dialogRedirectToPreference(final View v) {
     		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			    @Override
 			    public void onClick(DialogInterface dialog, int which) {
 			        switch (which){
 			        case DialogInterface.BUTTON_POSITIVE:
 			            startActivity(new Intent(mContext, Preferences.class));
 			            break;
 
 			        case DialogInterface.BUTTON_NEGATIVE:
 			            return;
 			        }
 			    }
 			};
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 			builder.setMessage("You have to tell us who you are before You can RSVP. Setup your details now?").setPositiveButton("Yes", dialogClickListener)
 			    .setNegativeButton("No", dialogClickListener).show();
     	}
     	
 		// Ask if RSVP for friends. if Yes displays another dialog. If no calls RSVP function
     	public void dialogRsvpFriends(final View v) {
     		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			    @Override
 			    public void onClick(DialogInterface dialog, int which) {
 			        switch (which){
 			        case DialogInterface.BUTTON_POSITIVE:
 			            rsvpFriends(v);
 			            break;
 
 			        case DialogInterface.BUTTON_NEGATIVE:
 			        	initiateRSVP(v, "");
 			        }
 			    }
 			};
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 			builder.setMessage("RSVP for your friends too?").setPositiveButton("Yes!", dialogClickListener)
 			    .setNegativeButton("No", dialogClickListener).show();
     	}
     	
     	// Asks user to input the names of friends to RSVP
     	public void rsvpFriends(final View v) {
     		// Ask if RSVP for friends. if Yes displays another dialog. If no calls RSVP function
     		
     		final EditText eText = new EditText(mContext);
     		
     		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			    @Override
 			    public void onClick(DialogInterface dialog, int which) {
 			        switch (which){
 			        case DialogInterface.BUTTON_POSITIVE:
 			        	tracker.trackEvent("Events", "RSVP Friends", "RSVP'd For Friends", 1);
 			            initiateRSVP(v, eText.getText().toString());
 			            break;
 
 			        case DialogInterface.BUTTON_NEGATIVE:
 			        	return; // RSVP process cancelled // TODO maybe ask abuot rsvp friends again ? 
 			        }
 			    }
 			}; 
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 			builder.setMessage("Enter the names of your friends seperated by a ',' ").setPositiveButton("I'm done!", dialogClickListener)
 			    .setNegativeButton("Cancel", dialogClickListener).setView(eText).show();
     	}
     	
     	// Asks if the user wants to remove the RSVP (Confirmation dialog)
     	public void dialogConfirmRemoveRsvp(final View v) {
     		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			    @Override
 			    public void onClick(DialogInterface dialog, int which) {
 			        switch (which){
 			        case DialogInterface.BUTTON_POSITIVE:
 			        	removeRSVP(v);
 			            break;
 
 			        case DialogInterface.BUTTON_NEGATIVE:
 			        	return; // RSVP process cancelled
 			        }
 			    }
 			}; 
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 			builder.setMessage("Are you sure you'r not coming?").setPositiveButton("Yes :/", dialogClickListener)
 			    .setNegativeButton("Cancel", dialogClickListener).show();
     	}
     	
     	// Initiates RSVP on thread. disables button. adds eventId to rsvpHistory
     	public void initiateRSVP(View v, String comments) {
     		Button btn = (Button)v;
     		btn.setText("Cancel");
     		
     		int event = (Integer) v.getTag(R.string.key_eventid);
     		
     		rsvpHistory.add(Integer.toString(event)); // Add to the local Copy
     		mSQueue.addRSVPHistory(event); // Add to the preference copy
     		
     		mSQueue.runOnThread(ServerQueue.RSVP, event, null, comments);
     		this.notifyDataSetChanged(); // Otherwise RSVP error occurs. as tag set only in getView
     	}
 		
     	// Removes RSVP on thread. enables button. removes eventId from rsvpHistory
     	public void removeRSVP(View v) {
     		
     		tracker.trackEvent("Events", "RSVP", "Remove'd RSVP", 1);
     		
     		Button btn = (Button)v;
     		btn.setText("Attending");
     		
     		int event = (Integer) v.getTag(R.string.key_eventid);
     		
     		rsvpHistory.remove(Integer.toString(event)); // Removes from local copy
     		mSQueue.removeRSVPHistory(event); // Removes from preference copy
     	
     		mSQueue.runOnThread(ServerQueue.CANCEL_RSVP, event, null, null);
     		this.notifyDataSetChanged();
     	}
     	
     	// -------------------------------------------------------------------------------------------------------------------
 		public Object getChild(int groupPosition, int childPosition) { return values.get(groupPosition).description; }
 		
 		public long getChildId(int groupPosition, int childPosition) { return childPosition; }
 
 		public int getChildrenCount(int groupPosition) { return 1; }
 
 		public Object getGroup(int groupPosition) { return values.get(groupPosition); }
 
 		public int getGroupCount() { return values.size(); }
 
 		public long getGroupId(int groupPosition) { return groupPosition; }
 
 		public boolean hasStableIds() { return true; }
 		
 		public boolean areAllItemsEnabled() { return true; }
 
 		public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }
 
     }
 }
