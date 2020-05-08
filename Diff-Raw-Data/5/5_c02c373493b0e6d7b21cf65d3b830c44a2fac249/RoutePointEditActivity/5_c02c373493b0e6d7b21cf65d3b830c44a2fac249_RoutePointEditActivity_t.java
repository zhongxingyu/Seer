 package com.mss.application;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.mss.domain.models.Customer;
 import com.mss.domain.models.Route;
 import com.mss.domain.models.RoutePoint;
 import com.mss.domain.models.ShippingAddress;
 import com.mss.domain.services.CustomerService;
 import com.mss.domain.services.RoutePointService;
 import com.mss.domain.services.RouteService;
 import com.mss.domain.services.ShippingAddressService;
 import com.mss.infrastructure.ormlite.DatabaseHelper;
 import com.mss.utils.IterableHelpers;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.app.NavUtils;
 import android.support.v4.app.TaskStackBuilder;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class RoutePointEditActivity extends SherlockFragmentActivity implements LoaderCallbacks<RoutePoint> {
 
 	private static final String TAG = RoutePointEditActivity.class.getSimpleName();
 
 	public static final int REQUEST_EDIT_ROUTE_POINT = 5;
 	public static final String KEY_ROUTE_POINT_ID = "id";
 	public static final String KEY_ROUTE_DATE = "route_date";
 	public static final int LOADER_ID_ROUTE_POINT = 0;
 	
 	static final int PICK_CUSTOMER_REQUEST = 1;
 	static final int PICK_SHIPPING_ADDRESS_REQUEST = 2;
 
 	private Date mRouteDate;
 	private long mRoutePointId;
 	private RoutePoint mRoutePoint;
 	private Customer mCustomer;
 	private ShippingAddress mShippingAddress;
 	private EditText mCustomerEditText;
 	private EditText mShippinAddressEditText;
 
 	private DatabaseHelper mHelper;
 	private RouteService mRouteService;
 	private RoutePointService mRoutePointService;
 	private CustomerService mCustomerService;
 	private ShippingAddressService mShippingAddressService;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_route_point_edit);
 
 		mRoutePointId = getIntent().getLongExtra(KEY_ROUTE_POINT_ID, RoutePointActivity.ROUTE_POINT_ID_NEW);
 
 		mHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
 		try {
 			mRouteService = new RouteService(mHelper);
 			mRoutePointService = new RoutePointService(mHelper);
 			mCustomerService = new CustomerService(mHelper);
 			mShippingAddressService = new ShippingAddressService(mHelper);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 		}
 		
 		String routeDateStr = getIntent().getStringExtra(KEY_ROUTE_DATE);		
 		if (routeDateStr != null && routeDateStr != "") {
 			try {
 				DateFormat format = SimpleDateFormat.getDateInstance();
 				mRouteDate = format.parse(getIntent().getStringExtra(KEY_ROUTE_DATE));
 			} catch (ParseException e) {
 				Log.e(TAG, e.getMessage());
 			}
 		}		
 		
 		if (mRoutePointId != RoutePointActivity.ROUTE_POINT_ID_NEW) {
 			mRoutePoint = mRoutePointService.getById(mRoutePointId);
 			Route route = mRouteService.getById(mRoutePoint.getRouteId());
 			mRouteDate = route.getDate();
 			
 			mShippingAddress = mShippingAddressService.getById(mRoutePoint.getShippingAddressId());
 			if (mShippingAddress != null)
 				mCustomer = mCustomerService.getByShippingAddress(mShippingAddress);
 			
 			getSupportLoaderManager().initLoader(LOADER_ID_ROUTE_POINT, null, this);
 		}
 		
 		mCustomerEditText = (EditText) findViewById(R.id.customer_edit_text);
 		mShippinAddressEditText = (EditText) findViewById(R.id.shipping_address_edit_text);
 		mCustomerEditText.setOnClickListener(new TextView.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent customersActivity = new Intent(getApplicationContext(), CustomersActivity.class);
 		    	startActivityForResult(customersActivity, PICK_CUSTOMER_REQUEST);
 			}
         });
 		
 		mCustomerEditText.setKeyListener(null);
 		
 		mShippinAddressEditText.setOnClickListener(new TextView.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent shippingAddressesActivity = new Intent(getApplicationContext(), ShippingAddressesActivity.class);
 				if (mCustomer != null) {
 					shippingAddressesActivity.putExtra("customer_id", (Long)mCustomer.getId());
 				}
 				startActivityForResult(shippingAddressesActivity, PICK_SHIPPING_ADDRESS_REQUEST);
 			}
         });
 		
 		mShippinAddressEditText.setKeyListener(null);
 
 		// Let's show the application icon as the Up button
 		if (getSupportActionBar() != null)
 			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.menu_route_point_edit, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    // Check which request we're responding to
 	    if (requestCode == PICK_CUSTOMER_REQUEST) {
 	        // Make sure the request was successful
 	        if (resultCode == RESULT_OK) {
 	        	long customerId = data.getLongExtra("customer_id", 0l);
 	        	
 	        	try {
 	        		mCustomer = mCustomerService.getById(customerId);
 	        		Iterable<ShippingAddress> shippingAddresses = mShippingAddressService.findByCustomer(mCustomer);
 					if (IterableHelpers.size(ShippingAddress.class, shippingAddresses) == 1) {
 						mShippingAddress = shippingAddresses.iterator().next();
 					} else {	        		
 						mShippingAddress = null;
 					}
 				} catch (Throwable e) {
 					Log.e(TAG, e.getMessage());
 				}	        
 	        	
 	        	getSupportLoaderManager().restartLoader(LOADER_ID_ROUTE_POINT, null, this);
 	        }
 	    } else if (requestCode == PICK_SHIPPING_ADDRESS_REQUEST) {
 	        // Make sure the request was successful
 	        if (resultCode == RESULT_OK) {
 	        	long shippingAddressId = data.getLongExtra("shipping_address_id", 0l);	        		        	
 	        	try {
 	        		mShippingAddress = mShippingAddressService.getById(shippingAddressId);
 					mCustomer = mCustomerService.getById(mShippingAddress.getCustomerId());
 				} catch (Throwable e) {
 					Log.e(TAG, e.getMessage());
 				}
 	        	
 	        	getSupportLoaderManager().restartLoader(LOADER_ID_ROUTE_POINT, null, this);
 	        }
 	    }
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent upIntent = new Intent(this, RouteActivity.class);
 			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
 				TaskStackBuilder.create(this).addNextIntent(upIntent).startActivities();
 				finish();
 			} else {
 				NavUtils.navigateUpTo(this, upIntent);
 			}
 			return true;
 		case R.id.menu_item_save:
 			try {
 				mCustomerEditText.setError(null);
 				mShippinAddressEditText.setError(null);
 					
 				boolean cancel = false;
 			    View focusView = null;
 			        
 			    if (mCustomer == null) {
 			       	mCustomerEditText.setError(getString(R.string.error_field_required));
 			        focusView = mCustomerEditText;
 			        cancel = true;
 			    }
 			        
 			    if (mShippingAddress == null) {
 			     	mShippinAddressEditText.setError(getString(R.string.error_field_required));
 			        focusView = mShippinAddressEditText;
 			        cancel = true;
 			    }
 			        
			    if (!cancel) {	
			    	RoutePoint routePoint = mRoutePointService.getPointByDateAndAddress(mRouteDate, mShippingAddress); 
			      	if (routePoint != null && routePoint.getId() != mRoutePointId) {
 			       		mShippinAddressEditText.setError(getString(R.string.error_same_point_already_exist));
 			       		focusView = mShippinAddressEditText;
 			           	cancel = true;
 			       	}
 			    }
 			        
 			    if (cancel) {
 			        focusView.requestFocus();
 			    } else {
 			    	if (mRoutePoint == null) {
 			    		mRoutePoint = mRoutePointService.cratePoint(mRouteDate, mShippingAddress);
 			    	} else {
 			    		mRoutePoint.setShippingAddress(mShippingAddress);
 			    	}
 			       	mRoutePointService.savePoint(mRoutePoint);
 			       	
 			       	Intent intent=new Intent();
 					setResult(RESULT_OK, intent);
 			       	finish();
 			    }					
 					
 			} catch (Throwable e) {
 				Log.e(TAG, e.getMessage());
 			}		
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public Loader<RoutePoint> onCreateLoader(int id, Bundle args) {
 		switch (id) {
 		case LOADER_ID_ROUTE_POINT:
 			try {
 				return new RoutePointLoader(this, mRoutePointId);
 			} catch (Throwable e) {
 				Log.e(TAG, e.getMessage());
 			}
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public void onLoadFinished(Loader<RoutePoint> loader, RoutePoint data) {
 		mRoutePoint = data;
 		
 		if (mCustomer != null)
 			mCustomerEditText.setText(mCustomer.getName());
 		else 
 			mCustomerEditText.setText("");
 		
 		if (mShippingAddress != null)
 			mShippinAddressEditText.setText(mShippingAddress.getAddress());
 		else
 			mShippinAddressEditText.setText("");
 	}
 
 	@Override
 	public void onLoaderReset(Loader<RoutePoint> loader) {
 		mRoutePoint = null;
 	}
 }
