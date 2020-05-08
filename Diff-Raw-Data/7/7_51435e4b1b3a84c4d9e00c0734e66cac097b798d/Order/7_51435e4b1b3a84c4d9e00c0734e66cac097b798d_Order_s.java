 package com.vendsy.bartsy.venue.model;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.vendsy.bartsy.venue.R;
 import com.vendsy.bartsy.venue.utils.Utilities;
 import com.vendsy.bartsy.venue.view.BartenderSectionFragment;
 
 public class Order  {
 
 	static final String TAG = "Order";
 	
 	// Each order has an ID that is unique within a session number
 	public String orderId; 
 	
 	// List of items in this order
 	public ArrayList<Item> items = new ArrayList<Item>();
 	
 	// The total price is in the local denomination and is the sum of price * quantity, fee and tax
 	public int quantity = 1;
 	public int image_resource;
 	
 	// Fees: total = base + tax + fee + tip
 	public Float baseAmount;
 	public Float feeAmount;
 	public Float taxAmount;
 	public Float tipAmount;
 	public Float totalAmount;
 	
 	public String updatedDate;
 	public String createdDate;
 	
 	public String profileId;
 	
 	// Each order contains the sender and the recipient (another single in the bar or a friend to pick the order up)
 	public Profile orderSender;
 	public Profile orderRecipient;
 	public String senderId;
 	public String recipientId;
 	public String userSessionCode;
 	
 	// The view displaying this order or null. The view is the display of the order in a list. 
 	// The list could be either on the client or the server and it looks different in both cases
 	// but the code manages the differences. 
 	public View view = null;
 	
 	// The states are implemented in a status variable and each state transition has an associated time
 	public int status;	
 	public int last_status;	// the previous status of this order (needed for timeouts in particular)
 	public int timeOut;			// time in minutes this order has before it times out (from the last updated state)
 
 	// Order states
 	// (received) -> NEW -> (accepted) -> IN_PROGRESS -> (completed) -> READY   -> (picked_up) -> COMPLETE    -> (timed out, error, etc) -> CANCELLED
 	//                      (rejected) -> REJECTED       (failed)    -> FAILED     (forgotten) -> INCOMPLETE  
 	
 	public static final int ORDER_STATUS_NEW			= 0;
     public static final int ORDER_STATUS_REJECTED    	= 1;
     public static final int ORDER_STATUS_IN_PROGRESS  	= 2;
     public static final int ORDER_STATUS_READY 			= 3;
 	public static final int ORDER_STATUS_FAILED	 		= 4;
 	public static final int ORDER_STATUS_COMPLETE	 	= 5;
 	public static final int ORDER_STATUS_INCOMPLETE	 	= 6;
 	public static final int ORDER_STATUS_CANCELLED	 	= 7;
 
 	// These remote statuses are not to be shown locally. Orders of this status should be ignored.
 	public static final int ORDER_STATUS_OFFER_REJECTED = 8;
 	public static final int ORDER_STATUS_OFFERED 		= 9;
 	public static final int ORDER_STATUS_REMOVED		= 10;
 	
 	// Local order states
 	public static final int ORDER_STATUS_TIMEOUT 		= 11;
 	
 	// Total order status count
 	public static final int ORDER_STATUS_COUNT 			= 12;
 	
 	public String errorReason = ""; // used to send an error reason for negative order states
     public Date[] state_transitions = new Date[ORDER_STATUS_COUNT];
 
     DecimalFormat df = new DecimalFormat();
     public boolean showCustomerDetails = false;
     
     
 	/**
 	 * TODO - Constructors
 	 */
     
 	public Order() {
 		df.setMaximumFractionDigits(2);
 		df.setMinimumFractionDigits(2);
 	}
 
 	
 	public Order(JSONObject json) {
 		
 		try {
 			
 			orderId = json.getString("orderId");
 
 			// Parse old format item
 			if (json.has("title")  || json.has("description") || json.has("itemId")) {
 				Item item = new Item();
 				if (json.has("itemName"))
 					item.setTitle(json.getString("itemName"));
 				if (json.has("description"))
 					item.setDescription(json.getString("description"));
 				if (json.has("basePrice"))
 					item.setPrice(json.getString("basePrice"));
 				if (json.has("itemId"))
 					item.setItemId(json.getString("itemId"));
 				items.add(item);
 			} 
 	
 			// Parse new format item list
 			if (json.has("itemsList")) {
 				JSONArray itemsJSON = json.getJSONArray("itemsList");
 				
 				for (int i=0 ; i < itemsJSON.length() ; i++) {
 					items.add(new Item(itemsJSON.getJSONObject(i)));
 				}
 			}
 			
 			baseAmount = Float.valueOf(json.getString("basePrice"));
 			tipAmount = Float.valueOf(json.getString("tipPercentage"));
 			totalAmount = Float.valueOf(json.getString("totalPrice"));
 			taxAmount = totalAmount - tipAmount - baseAmount;
 			
 			if (json.has("bartsyId"))
 				profileId = json.getString("bartsyId");
 			
 			if (json.has("orderTimeout"))
 				timeOut = json.getInt("orderTimeout");
 
 			// Setup the order status if it exists or set it to NEW_ORDER if it doesn't
 			if (json.has("orderStatus"))
 				status = Integer.parseInt(json.getString("orderStatus"));
 			else 
 				status = ORDER_STATUS_NEW;
 			
 			// Used only by the getPastOrders syscall
 			if (json.has("dateCreated")) 
 				createdDate = json.getString("dateCreated");
 				
 			// Setup created date (time the order was placed)
 			if (json.has("orderTime")) {
 				// User server provided creation date in the following format: 27 Jun 2013 12:03:04 GMT
 				createdDate = json.getString("orderTime");
 				state_transitions[ORDER_STATUS_NEW] = parseWeirdDate(createdDate);
 			} else {
 				// If no created date use current date for the creation state
 				state_transitions[ORDER_STATUS_NEW] = new Date();
 			}
 			
 			// Setup sender and recipient
 			if (json.has("senderBartsyId"))
 				senderId = json.getString("senderBartsyId");
 			if (json.has("recipientBartsyId"))
 				recipientId = json.getString("recipientBartsyId");
 
 			// Setup last updated date (time the order was updated last)  *** MAKE SURE to have updated status before getting here ***
 			if (json.has("updateTime")) {
 				// User server provided creation date in the following format: 27 Jun 2013 12:03:04 GMT
 				updatedDate = json.getString("updateTime");
 				state_transitions[status] = parseWeirdDate(json.getString("updateTime"));
 			} else {
 				// If no created date use current date for the update time
 				state_transitions[status] = new Date();
 			}
 			
 			// For now, since we are not receiving the last status time, set it to the update time to avoid expiring the order locally before the server had a chance. 
 			// The bartender may not be warned with enough notice and the order may expire prematurely
 			if (status == ORDER_STATUS_READY) {
 				// HACK for now - need to add value is syscalls
 				state_transitions[ORDER_STATUS_IN_PROGRESS] = state_transitions[status];
 			}
 			
 			// Set up last status based on current status
 			switch (status) {
 			case ORDER_STATUS_NEW:
 				last_status = status;
 				break;
 			case ORDER_STATUS_IN_PROGRESS:
 				last_status = ORDER_STATUS_READY;
 				break;
 			case ORDER_STATUS_READY:
 				last_status = ORDER_STATUS_IN_PROGRESS;
 				break;
 			}
 			
 			if (json.has("lastState"))
 				last_status = Integer.parseInt(json.getString("lastState"));
 			
 			// Set up user session code
 			if (json.has("userSessionCode")) 
 				userSessionCode = json.getString("userSessionCode");
 			
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		// Set our format 
 		df.setMaximumFractionDigits(2);
 		df.setMinimumFractionDigits(2);
 		
 	}
 	
 	/**
 	 * TODO - Utilities
 	 */
 	
 	@Override
 	public String toString() {
 		
 		JSONObject orderData = new JSONObject();
 		
 		try {
 			if (items.size() > 1) {
 				JSONArray jsonItems = new JSONArray();
 				for (Item item : items) {
 					JSONObject jsonItem = new JSONObject();
 					jsonItem.put("itemId", item.getItemId());
 					jsonItem.put("itemName", item.getTitle());
 					jsonItem.put("description", item.getDescription());
 					jsonItem.put("basePrice", item.getPrice());
 					jsonItems.put(jsonItem);
 				}
 				orderData.put("itemsList", jsonItems);
 			} else if (items.size() == 1) {
 				orderData.put("itemId", items.get(0).getItemId());
 				orderData.put("itemName", items.get(0).getTitle());
 				orderData.put("description", items.get(0).getDescription());
 			}
 
 			orderData.put("basePrice", String.valueOf(baseAmount));
 			orderData.put("tipPercentage", String.valueOf(tipAmount));
 			orderData.put("totalPrice", String.valueOf(totalAmount));
 			orderData.put("orderStatus", ORDER_STATUS_NEW);
 			
 			orderData.put("status", status);
 			orderData.put("orderTimeout", timeOut);
 			orderData.put("serverId", orderId);
 			
 			orderData.put("dateCreated", this.createdDate);
 			orderData.put("dateUpdated", this.updatedDate);
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return orderData.toString();
 	}
 
 	Date parseWeirdDate(String date) {
 		Date d = Utilities.getLocalDateFromGTMString(createdDate, "dd MMM yyyy HH:mm:ss 'GMT'");
 		if (d == null)
 			d = Utilities.getLocalDateFromGTMString(createdDate, "d MMM yyyy HH:mm:ss 'GMT'");
 		return d;
 	}
 	
 	
 	public String getRecipientName(ArrayList<Profile> people) {
 
 		if (orderRecipient != null) {
 			return orderRecipient.getName();
 		}
 		
 		if (recipientId == null)
 			return "<unkown>";
 					
 		for (Profile p : people) {
 			if (p.userID.equals(recipientId)) {
 				// User found
 				orderRecipient = p;
 				return p.getName();
 			}
 		}
 		return "<unkown>";
 	}
 	
 	public JSONObject statusChangedJSON(){
 		final JSONObject orderData = new JSONObject();
 		try {
 			orderData.put("orderId", orderId);
 			orderData.put("orderStatus", status);
 			orderData.put("orderRejectionReason", errorReason);
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return orderData;
 	}
 
 	
 	
 	/**
 	 * TODO - State
 	 */
 	
 	public void nextPositiveState() {
 		
 		Log.v(TAG, "setNextPositiveState()");
 
 		switch (status) {
 		case ORDER_STATUS_NEW:
 			last_status = status;
 			status = ORDER_STATUS_IN_PROGRESS;
 			break;
 		case ORDER_STATUS_IN_PROGRESS:
 			last_status = status;
 			status = ORDER_STATUS_READY;
 			break;
 		case ORDER_STATUS_READY:
 			last_status = status;
 			status = ORDER_STATUS_COMPLETE;
 			break;
 		default:
 			Log.v(TAG, "Order " + orderId + " status not changed to next positive because the status was " + status);
 			return;
 		}
 		
 		// Mark the time of the state transition in the timetable
 		state_transitions[status] = new Date();
 		
 		Log.v(TAG, "Order " + this.orderId + " moved from state" + last_status + " to state " + status);
 	}
 
 	public void setCancelledState(String cancelReason) {
 		
 		Log.v(TAG, "setCancelledState()");
 		
 		// Don't change orders that have already this status because their last_status would get lost
 		if (status == ORDER_STATUS_CANCELLED || status == ORDER_STATUS_TIMEOUT) {
 			Log.v(TAG, "Order " + this.orderId + " was already cancelled or timed out (last status: " + last_status + ")");
 			return;
 		}
 		
 		last_status = status;
 		status = ORDER_STATUS_CANCELLED;
 		state_transitions[status] = new Date();
 		errorReason = cancelReason;
 
 		Log.v(TAG, "Order " + this.orderId + " moved from status" + last_status + " to cancelled status " + status + " with reason " + cancelReason);
 	}
 
 	public void setTimeoutState() {
 		
 		Log.v(TAG, "setTimeoutState()");
 
 		if (status == ORDER_STATUS_NEW || status == ORDER_STATUS_READY || status == ORDER_STATUS_IN_PROGRESS) {
 			// Change status of orders 
 			last_status = status;
 			status = ORDER_STATUS_TIMEOUT;
 			state_transitions[status] = new Date();
 			errorReason = "Server unreachable. Check your internet connection and notify Bartsy customer support.";
 
 			Log.v(TAG, "Order " + this.orderId + " moved from state " + last_status + " to timeout state " + status);
 		} else {
 			Log.v(TAG, "Order " + this.orderId + "with last status " + last_status + " not changed to timeout status because the status was " + status + " with reason " + errorReason);
 			return;
 		}
 		
 	}
 	
 	public void nextNegativeState(String errorReason) {
 		
 		Log.v(TAG, "setNegativeState()");
 
 		int oldStatus = status;
 		
 		switch (status) {
 		case ORDER_STATUS_NEW:
 			last_status = status;
 			status = ORDER_STATUS_REJECTED;
 			break;
 		case ORDER_STATUS_IN_PROGRESS:
 			last_status = status;
 			status = ORDER_STATUS_FAILED;
 			break;
 		case ORDER_STATUS_READY:
 			last_status = status;
 			status = ORDER_STATUS_INCOMPLETE;
 			break;
 		default:
 			Log.v(TAG, "Order " + orderId + " status not changed to negative with reason " + errorReason + " because the status was " + status);
 			return;
 		}
 		
 		// Log the state change and update the order with an error reason
 		Log.v(TAG, "Order " + orderId + " changed status from " + oldStatus + " to " + status + " for reason: "  + errorReason);
 		this.errorReason = errorReason;
 		
 		// Mark the time of the state transition in the timetable
 		state_transitions[status] = new Date();
 	}
 	 
 	
 	/**
 	 * TODO - Views
 	 */
 	
 	/**
 	 * Updates the order view. Notice the view holds a pointer to the object being displayed through the "tag" field
 	 */
 	public void updateView (LayoutInflater inflater, ViewGroup container, int options) {
 
 		Log.v(TAG, "updateView()");
 		Log.v(TAG, "Order sender   :" + orderSender);
 		Log.v(TAG, "Order receiver :" + orderRecipient);
 		
 		view = (View) inflater.inflate(R.layout.bartender_order, container, false);
 		
 		if (view == null) return;
 
 		// Set main order parameters
 		((TextView) view.findViewById(R.id.view_order_number)).setText(orderId);
 		
 		// Add the order list
 		addItemsView((LinearLayout) view.findViewById(R.id.view_order_mini), inflater, container);
 
 		// Set base price
 		((TextView) view.findViewById(R.id.view_order_mini_base_amount)).setText(df.format(baseAmount));
 		
 		// Set the totals (we'll update again if we have more mini orders...)
 		updateTipTaxTotalView(tipAmount, taxAmount, totalAmount);
 		
 		if (orderRecipient != null) {
 
 			// Update customer visible name
 			((TextView) view.findViewById(R.id.view_order_profile_name)).setText(orderRecipient.getName());
 
 			// Update sender profile section if the details view is showing
 			if (showCustomerDetails ) {
 				ImageView profileImageView = ((ImageView)view.findViewById(R.id.view_order_profile_picture));
 				
 				// Set the saved image to the imageView
 				profileImageView.setImageBitmap(orderRecipient.image);
 			}			
 		}
 
 		// Update buttons 
 		switch (status) {
 		case ORDER_STATUS_NEW:
 			((Button) view.findViewById(R.id.view_order_button_positive)).setText("ACCEPT");
 			((Button) view.findViewById(R.id.view_order_button_negative)).setText("REJECT");
 			break;
 		case ORDER_STATUS_IN_PROGRESS:
 			((Button) view.findViewById(R.id.view_order_button_positive)).setText("COMPLETED");
 			((Button) view.findViewById(R.id.view_order_button_negative)).setText("FAILED");
 			break;
 		case ORDER_STATUS_READY:
 			((Button) view.findViewById(R.id.view_order_button_positive)).setText("PICKED UP");
 			((Button) view.findViewById(R.id.view_order_button_negative)).setText("NO SHOW");
 
 			if (options == BartenderSectionFragment.VIEW_MODE_ALL)
 				view.findViewById(R.id.view_order_actions).setVisibility(View.GONE);
 			break;
 		}
 
 		// Show/hide customer details
 		if (showCustomerDetails) {
 			view.findViewById(R.id.view_order_customer_details).setVisibility(View.VISIBLE);
 			((ImageButton) view.findViewById(R.id.view_order_button_customer_details)).setImageResource(R.drawable.arrowexpanded);
 		} else {
 			view.findViewById(R.id.view_order_customer_details).setVisibility(View.GONE);
 			((ImageButton) view.findViewById(R.id.view_order_button_customer_details)).setImageResource(R.drawable.arrowexpand);
 		}
 		
 		// Compute timers. Placed shows the time since the order was placed. Expires shows the time left in the current state until timeout.
 		double current_ms	= System.currentTimeMillis() ;
 		double timeout_ms	= timeOut * 60000;
 		double elapsed_ms	= current_ms - state_transitions[ORDER_STATUS_NEW].getTime();
 		double left_ms     	= timeout_ms - (current_ms - state_transitions[status].getTime());
 		double elapsed_min 	= elapsed_ms / (double) 60000;
 		double left_min    	= Math.ceil(left_ms / (double) 60000);
 		
 		// Set the background color of the order depending on how much time has elapsed as a percent of the timeout green->orange->red
 		if (left_ms <= timeout_ms / 3.0)
 			view.findViewById(R.id.view_order_background).setBackgroundResource(android.R.color.holo_red_light);
 		else if (left_ms <=  timeout_ms * 2.0 / 3.0)
 			view.findViewById(R.id.view_order_background).setBackgroundResource(android.R.color.holo_orange_light);
 		else
 			view.findViewById(R.id.view_order_background).setBackgroundResource(android.R.color.holo_green_light);
 
 		// Update timer since the order was placed
 		((TextView) view.findViewById(R.id.view_order_timer)).setText(String.valueOf((int)elapsed_min)+" min");
 		
 		// Handle timeout views
 		if (status == ORDER_STATUS_CANCELLED || status == ORDER_STATUS_TIMEOUT) {
 			// Change the order display to hide the normal action buttons and to show the timeout acknowledgment
 			view.findViewById(R.id.view_order_actions).setVisibility(View.GONE);
 			view.findViewById(R.id.view_order_expired).setVisibility(View.VISIBLE);
 			view.findViewById(R.id.view_order_background).setBackgroundResource(android.R.color.holo_red_light);
 			
 			// Set text based on reason
 			((TextView) view.findViewById(R.id.view_order_state_description)).setText(errorReason);
 
 			// Update timeout counter to always be expired even if there is some left (due to clock inconsistencies between local and server)
 			((TextView) view.findViewById(R.id.view_order_timeout)).setText("Expired");
 			
 			// Set tag to self for clicks
 			view.findViewById(R.id.view_order_button_expired).setTag(this);
 
 		} else {
 
 			// Update timeout counter		
 			if (left_min > 0) 
 				((TextView) view.findViewById(R.id.view_order_timeout)).setText("Expires in < " + String.valueOf((int)left_min)+" min");
 			else
 				((TextView) view.findViewById(R.id.view_order_timeout)).setText("About to expire");
 		}
 		
 		// Set a pointer to the object being displayed 
 		view.setTag(this);
 	}
 	
 	/**
 	 * Update the view of the order only with the given tip, tax and total amounts 
 	 */
 	public void updateTipTaxTotalView(float tipAmount,float taxAmount,float totalAmount){
 		if(view!=null){
 			((TextView) view.findViewById(R.id.view_order_tip_amount)).setText(df.format(tipAmount));
 			((TextView) view.findViewById(R.id.view_order_tax_amount)).setText(df.format(taxAmount));
 			((TextView) view.findViewById(R.id.view_order_total_amount)).setText(df.format(totalAmount));
 
 			// Set the tags of these views with the actual values (used for computing totals later)
 			((TextView) view.findViewById(R.id.view_order_tip_amount)).setTag(tipAmount);
 			((TextView) view.findViewById(R.id.view_order_tax_amount)).setTag(taxAmount);
 			((TextView) view.findViewById(R.id.view_order_total_amount)).setTag(totalAmount);		
 		}
 	}
 	
 	public View addItemsView(LinearLayout itemsView, LayoutInflater inflater, ViewGroup container ) {
 		
 		for (Item item : items) {
 			LinearLayout view = (LinearLayout) inflater.inflate(R.layout.bartender_order_mini, container, false);
 			((TextView) view.findViewById(R.id.view_order_mini_base_amount)).setText(df.format(Float.parseFloat(item.getPrice())));
 			((TextView) view.findViewById(R.id.view_order_title)).setText(item.getTitle());
			if (item.getDescription() != null || item.getDescription().equalsIgnoreCase(""))
 				((TextView) view.findViewById(R.id.view_order_description)).setVisibility(View.GONE);
 			else
 				((TextView) view.findViewById(R.id.view_order_description)).setText(item.getDescription());
 			
 			itemsView.addView(view);
 		}
 
 
 		return view;
 	}
 
 }
