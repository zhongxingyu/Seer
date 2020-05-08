 package com.envsocial.android.features.order;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.envsocial.android.R;
 import com.envsocial.android.api.ActionHandler;
 import com.envsocial.android.api.Annotation;
 import com.envsocial.android.api.Location;
 import com.envsocial.android.features.Feature;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class OrderFragment extends SherlockFragment implements OnClickListener, ISendOrder {
 	private static final String TAG = "OrderFragment";
 	
 	public static final int DIALOG_REQUEST = 0;
 	
 	private Location mLocation;
 	private OrderFeature mOrderFeature;
 	
 	private Button mBtnOrder;
 	private Button mBtnTab;
 	
 	private ViewPager mCatalogPager;
 	private OrderCatalogPagerAdapter mCatalogPagerAdapter;
 	
 	// mapping of selections by the item ID contained within them
 	private Map<Integer, Map<String, Object>> mOrderTab;
 	private List<Map<String, Object>> mCurrentOrderSelections;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 		
 		mOrderTab = new HashMap<Integer, Map<String,Object>>();
 	}
 	
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 							Bundle savedInstanceState) {
 		Log.i(TAG, "[INFO] onCreateView called.");
 		
 		// Inflate layout for this fragment.
 		View v = inflater.inflate(R.layout.catalog, container, false);
 		
 		mLocation = (Location) getArguments().get(ActionHandler.CHECKIN);
 		mOrderFeature = (OrderFeature)mLocation.getFeature(Feature.ORDER);
 		
 		mCatalogPager = (ViewPager) v.findViewById(R.id.catalog_pager);
 		mCatalogPagerAdapter = new OrderCatalogPagerAdapter(this);
 		mCatalogPager.setAdapter(mCatalogPagerAdapter);
 		
 		//Bind the title indicator to the adapter
 		TitlePageIndicator titleIndicator = (TitlePageIndicator) v.findViewById(R.id.catalog_page_titles);
 		titleIndicator.setViewPager(mCatalogPager);
 		mCatalogPagerAdapter.setTitlePageIndicator(titleIndicator);
 		
 		mBtnOrder = (Button) v.findViewById(R.id.btn_order);
 		mBtnOrder.setOnClickListener(this);
 	    
 		mBtnTab = (Button) v.findViewById(R.id.btn_tab);
 		mBtnTab.setOnClickListener(this);
 	    
 		
 	    return v;
 	}
 	
 	
 	public void onClick(View v) {
 		
 		if (v == mBtnOrder) {
 			List<Map<String, Object>> orderSelections = mCatalogPagerAdapter.getOrderSelections();
 			
 			if (!orderSelections.isEmpty()) {
 				OrderDialogFragment summaryDialog = OrderDialogFragment.newInstance(orderSelections);
 				summaryDialog.setTargetFragment(this, DIALOG_REQUEST);
 				summaryDialog.show(getFragmentManager(), "dialog");
 			}
 		}
 		else if (v == mBtnTab) {
 			List<Map<String, Object>> orderTabSelections = new ArrayList<Map<String,Object>>(mOrderTab.values());
 			OrderTabDialogFragment orderTabDialog = OrderTabDialogFragment.newInstance(orderTabSelections);
 			orderTabDialog.setTargetFragment(this, DIALOG_REQUEST);
 			orderTabDialog.show(getFragmentManager(), "dialog");
 		}
 		
 	}
 	
 	
 	public void sendOrder(OrderDialogFragment dialog) {
 		String orderJSON = dialog.getOrderJSONString();
 		
 		// hold on to current order selections
 		mCurrentOrderSelections = dialog.getOrderSelections();
 		dialog.dismiss();
 		
 		Annotation order = new Annotation(mLocation, 
 				Feature.ORDER, Calendar.getInstance(), orderJSON);
 		new SendOrderTask(getActivity(), this, order).execute();
 	}
 	
 	
 	@Override
 	public void postSendOrder(boolean success) {
 		if (success) {
 			// add current selections to tab then clear them
 			for (Map<String, Object> itemData : mCurrentOrderSelections) {
 				int itemId = (Integer) itemData.get(OrderFeature.ITEM_ID);
 				
 				Map<String, Object> itemTab = mOrderTab.get(itemId);
 				if (itemTab == null) {
 					itemTab = new HashMap<String, Object>();
 					itemTab.putAll(itemData);
 					
 					mOrderTab.put(itemId, itemTab);
 				}
 				else {
 					Integer tabQuantity = (Integer)itemTab.get("quantity");
 					tabQuantity += (Integer)itemData.get("quantity");
 					itemTab.put("quantity", tabQuantity);
 					
 					//Log.d(TAG, "new tab quantity: " + itemId + " >> " + mOrderTab);
 				}
 			}
 		}
 		
 		mCurrentOrderSelections = null;
 	}
 	
 	
 	Location getCurrentLocation() {
 		return mLocation;
 	}
 	
 	OrderFeature getOrderFeature() {
 		return mOrderFeature;
 	}
 }
