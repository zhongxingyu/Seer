 package com.bspif.app.mobilemechanic;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 
 import net.robotmedia.billing.BillingController;
 import net.robotmedia.billing.BillingController.IConfiguration;
 import net.robotmedia.billing.BillingRequest.ResponseCode;
 import net.robotmedia.billing.helper.AbstractBillingObserver;
 import net.robotmedia.billing.model.Transaction.PurchaseState;
 
 import com.bspif.app.mobilemechanic.AppData.CategoryData;
 import com.bspif.app.mobilemechanic.AppData.LessonData;
 import com.google.ads.AdView;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LessonListActivity extends Activity implements OnItemClickListener, IConfiguration {
 
 	private static final String TAG = "SubCat";
 	private int catID = -1;
 	private AppData.CategoryData catData = null;
 	private ViewGroup contentView = null;
 	private ListView listView = null;
 	private AbstractBillingObserver mBillingObserver;
 	private LessonListItemAdapter mLessonListAdapter = null;
 	private boolean mRequestingBilling = false;
 	
 	private class LessonListItemAdapter extends BaseAdapter {
 		private LayoutInflater inflater = null;
 		private View[] items = null;
 
 		public LessonListItemAdapter(Context context) {
 			super();
 			inflater = LayoutInflater.from(context);
 			items = new View[catData.lessons.length];
 			for (int i = 0; i < catData.lessons.length; i++) {
 				LessonData lessonData = catData.getLesson(i);
 				View item = inflater.inflate(R.layout.art_list_item, null);
 				TextView titleView = (TextView) item.findViewById(R.id.title);
 				ImageView iconView = (ImageView) item.findViewById(R.id.icon);
 				titleView.setText(lessonData.title);
 				try {
 					String iconFileName = lessonData.icon;
 					if (null == iconFileName) {
 						iconFileName = catData.icon;
 					}
 					Bitmap iconBmp = Util.getBitmapFromAsset(context, iconFileName);
 					iconView.setImageBitmap(iconBmp);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				if (!lessonData.canAccess()) {
 					titleView.setTextColor(Color.RED);
 				}
 				items[i] = item;
 			}
 		}
 		
 		public int getCount() {
 			return catData.getTitles().length;
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			return items[position];
 		}
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Intent intent = this.getIntent();
 		catID = intent.getExtras().getInt("catID");
 		catData = AppData.getCategory(catID);
 		this.setTitle(catData.title);
 
 		//ad view
 		AdView adView = Global.instance.getAdView();
 		
 		// list view
 		//String[] titles = catData.getTitles();
 		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.art_list_item, titles);
 		mLessonListAdapter = new LessonListItemAdapter(this);
 		listView = new ListView(this);
 		listView.setAdapter(mLessonListAdapter);
 		listView.setOnItemClickListener(this);
 		RelativeLayout.LayoutParams lvParam = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		lvParam.addRule(RelativeLayout.ABOVE, adView.getId());
 		listView.setLayoutParams(lvParam);
 		
 		// add views
 		RelativeLayout layout = new RelativeLayout(this);
 		layout.addView(listView);
 		//layout.addView(adView);
 		contentView = layout;
 		this.setContentView(contentView);
 		
 		initBilling();
 	}
 
 	@Override
 	protected void onDestroy() {
 		uninitBilling();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onResume() {
 		if (!AppData.isPurchased) {
 			AdView adView = Global.instance.getAdView();
 			contentView.addView(adView);
 		}
 		super.onResume();
 	}
 	
     @Override
 	protected void onPause() {
     	AdView adView = Global.instance.getAdView();
     	contentView.removeView(adView);
 		super.onPause();
 	}
 	
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {  
         super.onConfigurationChanged(newConfig);  
         if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {  
                 // land do nothing is ok  
         } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {  
                 // port do nothing is ok  
         }  
 	}
 
 	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
 		Log.d(TAG, String.format("on subcat item clicked %d", index));
 		AppData.LessonData lessonData = catData.getLesson(index); 
 		if (null == lessonData) {
 			return;	// DO NOTHING
 		}
 		
 		if (!lessonData.canAccess()) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Lesson locked");
 			builder.setMessage("Buy full version?");
 			builder.setPositiveButton("Yes", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					mRequestingBilling = true;
 					BillingController.BillingStatus status = BillingController.checkBillingSupported(LessonListActivity.this);	// purchase
 					if (status == BillingController.BillingStatus.UNSUPPORTED) {
 						showNotSupportedBilling();
 					}
 				}
 			});
 			builder.setNegativeButton("Later", null);
 			builder.show();
 			return;
 		}
 		
 		if (null != lessonData.link) {
 			
 			int cID = AppData.findCategoryByTitle(lessonData.link.category);
 			if (cID < 0) {
 				// ERROR
 				return;
 			}
 			CategoryData catData = AppData.getCategory(cID);
 			int lsID = catData.findLessonByTitle(lessonData.link.lesson);
 			if (lsID < 0) {
 				// ERROR
 				return;
 			}
 			int p = lessonData.link.page;
 			
 			Intent intent = new Intent(this, LessonActivity.class);
 			intent.putExtra("catID", cID);
 			intent.putExtra("lessonID", lsID);
 			intent.putExtra("page", p);
 			this.startActivity(intent);
 			return;
 		}
 		
 		Intent intent = new Intent(this, LessonActivity.class);
 		intent.putExtra("catID", catID);
 		intent.putExtra("lessonID", index);
 		this.startActivity(intent);
 	}
 	
 	protected void updateView() {
 		finish();
 		startActivity(getIntent());
 	}
 	
 	protected void onPurchased() {
 		AppData.setPurchased(true, this);
 		updateView();
 	}
 	
 	protected void onRefund() {
 		try {
 			AppData.put(AppData.JSON_DATA_PURCHASE_STATE_KEY, null);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
		AppData.saveData();
 		AppData.isPurchased = false;
 		updateView();
 	}
 
 	@Override
 	public byte[] getObfuscationSalt() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getPublicKey() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	protected void showNotSupportedBilling() {
 		Toast.makeText(LessonListActivity.this, R.string.inapp_not_supported, Toast.LENGTH_LONG).show();
 	}
 	
 	protected void initBilling() {
 		mBillingObserver = new AbstractBillingObserver(this) {
 
 			public void onBillingChecked(boolean supported) {
 				Log.i(TAG, "onBillingChecked %s", supported);
 				if (!mRequestingBilling) {
 					return;
 				}
 				if (!supported) {
 					showNotSupportedBilling();
 					mRequestingBilling = false;
 					return;
 				} else {
 					// request purchase
 					String itemId = LessonListActivity.this.getResources().getString(R.string.inapp_item_unlock);
 					BillingController.requestPurchase(LessonListActivity.this, itemId);
 				}
 			}
 			
 			public void onSubscriptionChecked(boolean supported) {
 				Log.i(TAG, "onSubscriptionChecked %s", supported);
 				// DO NOTHING
 			}
 
 			public void onPurchaseStateChanged(String itemId, PurchaseState state) {
 				Log.i(TAG, "onPurchaseStateChanged %s, %d", itemId, state.ordinal());
 				String unlockId = LessonListActivity.this.getResources().getString(R.string.inapp_item_unlock);
 				if (!itemId.equals(unlockId)) {
 					return;
 				}
 				if (state == PurchaseState.PURCHASED) {
 					onPurchased();
 				} else if (state == PurchaseState.CANCELLED) {
 					// DO NOTHING
 				} else if (state == PurchaseState.REFUNDED) {
 					onRefund();
 				} else if (state == PurchaseState.EXPIRED) {
 					// NERVER HAPPEN
 				} else {
 					// DO NOTHING
 				}
 				//mRequestingBilling = false;
 			}
 
 			public void onRequestPurchaseResponse(String itemId, ResponseCode response) {
 				Log.i(TAG, "onRequestPurchaseResponse %s, %d", itemId, response.ordinal());
 				mRequestingBilling = false;
 				String unlockId = LessonListActivity.this.getResources().getString(R.string.inapp_item_unlock);
 				if (!itemId.equals(unlockId)) {
 					return;
 				}
 				if (response == ResponseCode.RESULT_OK) {
 					onPurchased();
 					return;
 				}
 				if (response == ResponseCode.RESULT_USER_CANCELED) {
 					// DO NOTHING
 					return;
 				}
 				if (response == ResponseCode.RESULT_BILLING_UNAVAILABLE) {
 					Toast.makeText(LessonListActivity.this, "ERROR: Billing unavailable", Toast.LENGTH_LONG).show();
 				}
 				if (response == ResponseCode.RESULT_ITEM_UNAVAILABLE) {
 					Toast.makeText(LessonListActivity.this, "ERROR: Item unavailable", Toast.LENGTH_LONG).show();
 				}
 				if (response == ResponseCode.RESULT_SERVICE_UNAVAILABLE) {
 					Toast.makeText(LessonListActivity.this, "ERROR: Sever unavailable", Toast.LENGTH_LONG).show();
 				}
 				if (response == ResponseCode.RESULT_ERROR || response == ResponseCode.RESULT_DEVELOPER_ERROR) {
 					Toast.makeText(LessonListActivity.this, "ERROR: Purchase falied", Toast.LENGTH_LONG).show();
 				}
 			}
 		};
 		BillingController.registerObserver(mBillingObserver);
 		BillingController.setConfiguration(this); // This activity will provide
 		// the public key and salt
 		//BillingController.checkBillingSupported(this);
 		if (!mBillingObserver.isTransactionsRestored()) {	// TODO
 			BillingController.restoreTransactions(this);
 		}
 	}
 	
 	protected void uninitBilling() {
 		BillingController.unregisterObserver(mBillingObserver);
 		BillingController.setConfiguration(null);
 	}
 	
 }
