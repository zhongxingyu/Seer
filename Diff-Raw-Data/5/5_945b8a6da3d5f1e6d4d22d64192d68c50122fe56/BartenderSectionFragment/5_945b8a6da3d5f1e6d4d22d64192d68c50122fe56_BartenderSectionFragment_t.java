 /**
  * 
  */
 package com.vendsy.bartsy.venue.view;
 
 
 import java.util.ArrayList;
 
 import com.vendsy.bartsy.venue.R;
 import com.vendsy.bartsy.venue.BartsyApplication;
 import com.vendsy.bartsy.venue.MainActivity;
 import com.vendsy.bartsy.venue.dialog.CodeDialogFragment;
 import com.vendsy.bartsy.venue.model.Order;
 import com.vendsy.bartsy.venue.model.Profile;
 import com.vendsy.bartsy.venue.utils.Constants;
 import com.vendsy.bartsy.venue.utils.Utilities;
 import com.vendsy.bartsy.venue.utils.WebServices;
 
 import android.support.v4.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * @author peterkellis
  * 
  */
 public class BartenderSectionFragment extends Fragment implements OnClickListener {
 
 	static final String TAG = "BartenderSectionFragment";
 	
 	private View mRootView = null;
 	LinearLayout mNewOrdersView = null;
 	LinearLayout mAcceptedOrdersView = null;
 	LinearLayout mCompletedOrdersView = null;
 	LayoutInflater mInflater = null;
 	ViewGroup mContainer = null;
 	public BartsyApplication mApp = null;
 	
 
     // Viewing modes - these control additional features of the view such as customer-centric view
     private int mViewMode = VIEW_MODE_ALL;
     private String mViewModeOptions = null;
     public static final int VIEW_MODE_ALL		= 0; // View all orders in the layout
     public static final int VIEW_MODE_CUSTOMER	= 1; // View only orders specified in the viewModeOptions field
     
 
 	/*
 	 * Creates a map view, which is for now a mock image. Listen for clicks on the image
 	 * and toggle the bar details image
 	 */ 
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 		Log.v("Bartsy", "OrdersSectionFragment.onCreateView()");
 
 		mInflater = inflater;
 		mContainer = container;
 		mRootView = mInflater.inflate(R.layout.bartender_main, mContainer, false);
 		mNewOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_new_order_list);
 		mAcceptedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_accepted_order_list);
 		mCompletedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_completed_order_list);
 		
 		// Make sure the fragment pointed to by the activity is accurate
 		mApp = (BartsyApplication) getActivity().getApplication();
 		((MainActivity) getActivity()).mBartenderFragment = this;	
 		
 		// Update the view
 		updateOrdersView();
 		
 		// Check and set development environment display
 		if (WebServices.DOMAIN_NAME.equalsIgnoreCase("http://54.235.76.180:8080/") && WebServices.SENDER_ID.equalsIgnoreCase("605229245886")) 
 			((TextView) mRootView.findViewById(R.id.view_main_deployment_environment)).setText("Server: DEV");
 		else if (WebServices.DOMAIN_NAME.equalsIgnoreCase("http://app.bartsy.vendsy.com/") && WebServices.SENDER_ID.equalsIgnoreCase("560663323691")) 
 			((TextView) mRootView.findViewById(R.id.view_main_deployment_environment)).setText("Server: PROD");
 		else 
 			((TextView) mRootView.findViewById(R.id.view_main_deployment_environment)).setText("** INCONSISTENT DEPLOYMENT **");
 		
 		// Set up button listeners
 		mRootView.findViewById(R.id.view_order_new_button).setOnClickListener(this);
 		mRootView.findViewById(R.id.view_order_in_progress_button).setOnClickListener(this);
 		mRootView.findViewById(R.id.view_order_ready_button).setOnClickListener(this);
 		mRootView.findViewById(R.id.view_customer_mode_button).setOnClickListener(this);
 		
 		return mRootView;
 	}
 	
 
 	/**
 	 * Sets the view modes of this view
 	 * @param mode
 	 * @param options
 	 */
 	public void setViewMode(int mode, String options) {
 		switch (mode) {
 		case VIEW_MODE_ALL:
 			mViewMode = mode;
 			mViewModeOptions = options;
 			break;
 		case VIEW_MODE_CUSTOMER:
 			mViewMode = mode;
 			mViewModeOptions = options;
 			break;
 		}
 		
 		// Notify of the change in view
 		mApp.notifyObservers(BartsyApplication.ORDERS_UPDATED);
 	}
 	
 	/***
 	 * Updates the orders view
 	 */
 	
 	synchronized public void updateOrdersView() {
 		
 		Log.v(TAG, "updateOrdersView()");
 		
 		// Defensive programming - this should not really happen
 		if (mRootView == null) return;
 		if (mNewOrdersView == null || mAcceptedOrdersView == null || mCompletedOrdersView == null)
 			return;
 		
 //		mNewOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_new_order_list);
 //		mAcceptedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_accepted_order_list);
 //		mCompletedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_completed_order_list);
 		
 
 		
 		// Setup view options
 		switch (mViewMode) {
 		case VIEW_MODE_ALL:
 			mRootView.findViewById(R.id.view_customer_mode).setVisibility(View.GONE);
 			break;
 		case VIEW_MODE_CUSTOMER:
 			((TextView) mRootView.findViewById(R.id.view_customer_pickup_code)).setText("Customer code: " + mViewModeOptions);
 			mRootView.findViewById(R.id.view_customer_mode).setVisibility(View.VISIBLE);
 			
 			
 			break;
 		}
 		
 		// Make sure the list views are all empty
 		
 		mNewOrdersView.removeAllViews();
 		mAcceptedOrdersView.removeAllViews();
 		mCompletedOrdersView.removeAllViews();
 		
 		// Add any existing orders in the layout, one by one
 		
 		Log.v(TAG, "mApp.mOrders list size = " + mApp.getOrderCount());
 		
 		ArrayList<Order> ordersClone = mApp.cloneOrders();
 
 		// Counters for inserted orders in the different layouts
 		int newOrdersCount = 0;
 		int acceptedOrdersCount = 0;
 		int completedOrdersCount = 0;
 		
 		Profile customer = null;
 		
 		for (Order order : ordersClone) {
 			
 			// If we're in customer mode check to see if we can exit that mode 
 			if (mViewMode == VIEW_MODE_CUSTOMER && !mViewModeOptions.equals(order.userSessionCode)) {
 
 				Log.v(TAG, "Customer mode - Skipping order " + order.orderId + " with status " + order.status + " and last status " + order.last_status + " to the layout");
 			
 			} else {
 				Log.v(TAG, "Adding order " + order.orderId + " with status " + order.status + " and last status " + order.last_status + " to the layout");
 				
 				if (mViewMode== VIEW_MODE_CUSTOMER)
 					customer = order.orderRecipient;
 					
 				// Update the view's main layout 
 				order.updateView(mInflater, mContainer, mViewMode);
 				
 				switch (order.status) {
 				case Order.ORDER_STATUS_NEW:
 					// add order to the top of the accepted orders list view
 					newOrdersCount += insertOrderInLayout(order,mNewOrdersView);
 					break;
 				case Order.ORDER_STATUS_IN_PROGRESS:
 					// add order to the top of the accepted orders list view
 					acceptedOrdersCount += insertOrderInLayout(order, mAcceptedOrdersView);
 					break;
 				case Order.ORDER_STATUS_READY:
 					// add order to the bottom of the completed orders list view 
 					completedOrdersCount += insertOrderInLayout(order, mCompletedOrdersView);
 					break;
 				case Order.ORDER_STATUS_CANCELLED:
 				case Order.ORDER_STATUS_TIMEOUT:
 					// add cancelled order in the right layout based on its last state
 					switch (order.last_status) {
 					case Order.ORDER_STATUS_NEW:
 						newOrdersCount += insertOrderInLayout(order, mNewOrdersView);
 						break;
 					case Order.ORDER_STATUS_IN_PROGRESS:
 						acceptedOrdersCount += insertOrderInLayout(order, mAcceptedOrdersView);
 						break;
 					case Order.ORDER_STATUS_READY:
 						completedOrdersCount += insertOrderInLayout(order, mCompletedOrdersView);
 						break;
 					default:
 						// We should not have gotten there. Show the order regardless but warn the user...
 						order.errorReason = "This order is cancelled, but in the wrong state. Please let the Bartsy team know.";
 						completedOrdersCount += insertOrderInLayout(order, mCompletedOrdersView);
 					}
 					break;
 
 				}
 			}
 		}
 		
 		// Update customer view if we're in that mode
 		if (customer != null)			
 			customer.updateView(mRootView);
 		
 		// Get order timeouts
 		int minTimeout = 0;
 		int maxTimeout = 0;
 		for (Order order : ordersClone) {
 			minTimeout = Math.min(order.timeOut, minTimeout);
 			maxTimeout = Math.max(order.timeOut, maxTimeout);
 		}
 		String timeoutString;
 		if (minTimeout == 0 && maxTimeout == 0)
 			timeoutString = "";
 		else if (minTimeout == 0)
 			timeoutString = "(" + maxTimeout + " min timeout)";
 		else if (maxTimeout == 0)
 			timeoutString = "(" + minTimeout + " min timeout)";
 		else
 			timeoutString = "(" + minTimeout + "-" + maxTimeout + " min timeout)";
 		
 		// Set title for new orders layout
 		String title = "";
 		switch (newOrdersCount) {
 		case 0:
 			title = "No new orders";
 			break;
 		case 1:
 			title = "1 new order " + timeoutString;
 			break;
 		default:
 			title = newOrdersCount	+ " new orders " + timeoutString;
 		}
 		((Button) mRootView.findViewById(R.id.view_order_new_button)).setText(title);
 		
 		// Set title for accepted orders layout
 		switch (acceptedOrdersCount) {
 		case 0:
 			title = "No orders in progress";
 			break;
 		case 1:
 			title = "1 order in progress " + timeoutString;
 			break;
 		default:
 			title = newOrdersCount	+ " orders in progress " + timeoutString;
 		}
 		((Button) mRootView.findViewById(R.id.view_order_in_progress_button)).setText(title);
 		
 		// Set title for completed orders layout
 		switch (completedOrdersCount) {
 		case 0:
 			title = "No completed orders";
 			break;
 		default:
 			title = "Click to enter pickup code" ;
 			break;
 		}
 		((Button) mRootView.findViewById(R.id.view_order_ready_button)).setText(title);
 	}
 
 	
 	/**
 	 * Optionally bundles orders of a user together using the same order number for convenience
 	 */
 	
 	int insertOrderInLayout(Order order, LinearLayout layout) {
 		
 		// How many orders we're inserted in the layout
 		int count = 0; 
 		
 		// Never bundle expired or cancelled orders 
 		if (Constants.bundle && order.status != Order.ORDER_STATUS_CANCELLED && order.status != Order.ORDER_STATUS_TIMEOUT) {
 		
 			// Try to insert the order in a previous order from the same user
 
 			for (int i=0 ; i < layout.getChildCount() ; i++) {
 				
 				View view = layout.getChildAt(i);
 				Order layoutOrder = (Order) view.getTag();
 				
 				if (layoutOrder.status != Order.ORDER_STATUS_CANCELLED && // Don't insert in expired orders
 						layoutOrder.status != Order.ORDER_STATUS_TIMEOUT &&
 						layoutOrder.orderRecipient.userID.equalsIgnoreCase(order.orderRecipient.userID)) {
 					
 					// Found an existing order from the same user. Insert a mini-view of the order
 					order.addItemsView((LinearLayout) view.findViewById(R.id.view_order_mini), mInflater, mContainer);
 					
 					// Update the view (not the order itself) of the master order total values to include the order just added
 					Float tipAmount = (Float) view.findViewById(R.id.view_order_tip_amount).getTag();
 					Float taxAmount = (Float) view.findViewById(R.id.view_order_tax_amount).getTag();
 					Float totalAmount = (Float) view.findViewById(R.id.view_order_total_amount).getTag();
 					layoutOrder.updateTipTaxTotalView(tipAmount + order.tipAmount, taxAmount + order.taxAmount, totalAmount + order.totalAmount);
 					
 					return count;
 				}
 			}
 		}
 		
 		// No previous order was found, insert the order at the top level
 		layout.addView(order.view);
 		count++;
 		
 		// Update order view buttons
 		order.view.findViewById(R.id.view_order_button_positive).setOnClickListener(this);
 		order.view.findViewById(R.id.view_order_button_positive).setTag(order);
 		
 		order.view.findViewById(R.id.view_order_button_negative).setOnClickListener(this);
 		order.view.findViewById(R.id.view_order_button_negative).setTag(order);
 		
 		order.view.findViewById(R.id.view_order_button_expired).setOnClickListener(this);
 		order.view.findViewById(R.id.view_order_button_expired).setTag(order);
 		
 		order.view.findViewById(R.id.view_order_button_customer_details).setOnClickListener(this);
 		order.view.findViewById(R.id.view_order_button_customer_details).setTag(order);
 		
 		return count;
 	}
 	
 
 	/**
 	 * 
 	 * Handle clicks coming from an item in the order list. These change the state of the orders and notify the
 	 * other sides. We bundle orders together by sender and pressing a positive or negative button processes all
 	 * the orders in the bundle. Individual items in an order can also be rejected by pressing the button on the 
 	 * left of the time.
 	 * 
 	 */
 	
 	@Override
 	public synchronized void onClick(View v) {
 
 		Log.v(TAG, "onClick()");
 
 		Order order = (Order) v.getTag();
 		ArrayList<Order> orders = null;
 		int status = 0;
 		String userID = null;
 		if (order != null) {
 			orders = mApp.cloneOrders();
 			status = order.status;
 			userID = order.orderRecipient.userID;
 			Log.v(TAG, "---- Master order: " + order.orderId + " from " + userID + " with status " + status);
 		}
 		
 		// Update the order status locally 
 		
 
 		switch (v.getId()) {
 		
 		case R.id.view_order_button_positive:
 			
 			// Process all orders for that user that are currently in this state
 			Log.v(TAG, "Clicked on order positive button");
 			order.nextPositiveState();	
 			Log.v(TAG, "Child matches parent - update status to " + order.status);
 			
 //			mApp.update(); //- this will get called automatically in the next cycle, don't call it now to make UI more snappy
 			break;
 			
 		case R.id.view_order_button_negative:
 			
 			// Process all orders for that user that are currently in this state
 			Log.v(TAG, "Clicked on order negative button");
 			order.nextNegativeState("Order rejected by the bartender");	
 //			mApp.update();
 			break;
 			
 		case R.id.view_order_button_expired:
 			Log.v(TAG, "Clicked on order expired button");
 			order.view = null;
 			mApp.removeOrder(order);
 			break;
 
 		case R.id.view_order_button_customer_details:
 			Log.v(TAG, "Clicked on the customers details button - toggle customer details view");
 			order.showCustomerDetails = !order.showCustomerDetails;
 			break;
 			
 		case R.id.view_order_ready_button:
 			Log.v(TAG, "Clicked on the pickup button");
 			
 			// Create an instance of the dialog fragment and show it
 			CodeDialogFragment dialog = new CodeDialogFragment();
 			dialog.show(getActivity().getSupportFragmentManager(),"Enter customer code");
 			break;
 			
 		case R.id.view_customer_mode_button:
 			Log.v(TAG, "Clicked on the close customer details button");
 			
 			setViewMode(VIEW_MODE_ALL, null);
 			break;
 			
 		default:
 			break;
 		}
 		
 		
 		// If we're in customer mode check to see if we can exit that mode 
 		boolean customerOrderFound = false;
 		if (order!= null && mViewMode == VIEW_MODE_CUSTOMER) {
 			for (Order customerOrder : mApp.cloneOrders()) {
 				if(customerOrder.status == Order.ORDER_STATUS_READY && mViewModeOptions.equals(customerOrder.userSessionCode)) {
 					customerOrderFound = true;
 					break;
 				}
 			}
 			
 			// If no remaining orders found for that user return to all orders viewing mode
 			if (!customerOrderFound) {
 				setViewMode(VIEW_MODE_ALL, null);
 			}
 		}
 		
 		
 		// Update the orders view
 		updateOrdersView();
 	}
 	
 	
 	@Override 
 	public void onDestroyView() {
 		super.onDestroyView();
 
 		Log.v(TAG, "onDestroyView()");
		
		// Make sure the list views are all empty
		mNewOrdersView.removeAllViews();
		mAcceptedOrdersView.removeAllViews();
		mCompletedOrdersView.removeAllViews();
 	}
 	
 	
 	@Override 
 	public void onDestroy() {
 		super.onDestroy();
 
 		Log.v(TAG, "onDestroy()");
 		
 		mRootView = null;
 		mNewOrdersView = null;
 		mInflater = null;
 		mContainer = null;
 
 		// Because the fragment may be destroyed while the activity persists, remove pointer from activity
 		((MainActivity) getActivity()).mBartenderFragment = null;
 	}
 }
