 package com.dev.streetfood;
 
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 
import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 public class ShopDetailView extends Activity {
 
 	private static final String TAG = "ShowDetails";
 	String sql;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_shop_detail_view);
 		// to hide the action bar
 				try
 				{
 				ActionBar actionBar = getActionBar();
 				actionBar.hide();
 				}
 				catch (Exception ex)
 				{
 				  Log.e(TAG,"Device Do Not Support Action Bar"+ex.toString());
 				  
 				}
 		
 		RadioGroup radioGroupActivitySelector = (RadioGroup) findViewById(R.id.radio_group_activity_selector);
 		radioGroupActivitySelector.setOnCheckedChangeListener(new OnCheckedChangeListener() 
 	    {
 	        public void onCheckedChanged(RadioGroup group, int checkedId) {
 	            // checkedId is the RadioButton selected
 		switch (checkedId) {
 		  case R.id.radioList :
 			  Log.i(TAG,"RadioList Activity Selected");
 			  goToListView();
 		                   	              break;
 		  case R.id.radioMap :
 			  Log.i(TAG,"RadioMap Activity Selected");
 			  goToMapView();
 		                   	              break;                 	              
 		 
 		 }
 	        }
 	    });
 		
 		Bundle b = getIntent().getExtras();
 		String shopName=(String) b.get("itemName");
 		
 		
 		TextView txtShopName = (TextView) findViewById(R.id.txtShopName);	
 		txtShopName.setText(shopName);
 		
 		TextView txtShopInfo = (TextView) findViewById(R.id.txtShopInfo);	
 		//TextView txtRating = (TextView) findViewById(R.id.txtRating);	
 		TextView txtAddress = (TextView) findViewById(R.id.txtAddress);	
 			
 		// get relevant info from db
 		sql="select ifnull(info,\'Not Available\')  from streetShopInfo where shopName=\""+shopName+"\"";
 		StreetFoodDataBaseAdapter mDBAdapter= new StreetFoodDataBaseAdapter(this);
 		mDBAdapter.createDatabase();       
 		mDBAdapter.open();
 		String info=mDBAdapter.getSingleStringVal(sql);
 		txtShopInfo.setText(info);
 		Log.i(TAG,info);
 		
 		/*sql="select ifnull(ratings,0)  from streetShopInfo where shopName=\""+shopName+"\"";
 		int ratings=mDBAdapter.getSingleIntVal(sql);
 		String ratingsinstring=String.valueOf(ratings);
 		txtRating.setText(ratingsinstring);
 		Log.i(TAG,"Got Rating");*/
 	
 		sql="select ifnull(address,\'Not Available\')  from streetShopInfo where shopName=\""+shopName+"\"";
 		String address=mDBAdapter.getSingleStringVal(sql);
 		txtAddress.setText("Address: \n"+address);
 		Log.i(TAG,"Got Address:"+address);
 		
 		mDBAdapter.close();
 		
 	}
 	
 	public void goToListView()
 	{
 		 Intent intent = new Intent(getApplicationContext(),ShopListView.class);
 		 //start the DisplayActivity
          startActivity(intent);
 	}
 	
 	public void goToMapView()
 	{
 		 Intent intent = new Intent(getApplicationContext(),ShopMapView.class); // change it to Map Activity
 		 //start the DisplayActivity
          startActivity(intent);
 	}
 	
 	
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_shop_detail_view, menu);
 		return true;
 	}
	
	
	@Override
	protected void onStart() {
	    super.onStart();
	  //default show Popular Shops
	    
	    RadioButton ListRadioButton=(RadioButton) findViewById(R.id.radioList);
  		ListRadioButton.setChecked(false);
  		 RadioButton MapRadioButton=(RadioButton) findViewById(R.id.radioMap);
  		MapRadioButton.setChecked(false);
  		
  		
	}
 
 }
