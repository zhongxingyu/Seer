 package com.restaurant.collection;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.restaurant.adapter.RestaurantGridViewAdapter;
 import com.restaurant.collection.api.RestaurantAPI;
 import com.restaurant.collection.db.SQLiteRestaurant;
 import com.restaurant.collection.entity.Area;
 import com.restaurant.collection.entity.Restaurant;
 import com.restaurant.collection.entity.Note;
 import com.restaurant.fragment.RestaurantNotesFragment;
 import com.restaurant.gps.util.GPSTracker;
 import com.viewpagerindicator.CirclePageIndicator;
 
 public class RestaurantIntroActivity extends SherlockFragmentActivity {
 	private static final int ID_IMPEACH = 0;
 	private ViewPager pager;
 	private ImageButton share_btn;
 	private ImageButton direction_button;
 	private ImageButton place_button;
 	private ImageButton favorite_button;
 	private TextView address_text;
 	private TextView opentime_text;
 	private TextView price_text;
 	private TextView restaurant_intro_text;
 	private Button official_btn;
 	
 	private LinearLayout                      progressLayout;
     private LinearLayout                      layoutReload;
     private Button                            buttonReload;
 	private Restaurant restaurant;
 	private ArrayList<Note> notes;
 	private double latitude;
 	private double longitude;
 	private TextView official_link_none_text;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_restaurant_intro);
         
         Bundle mBundle = this.getIntent().getExtras();
         int restaurantId = mBundle.getInt("ResturantId");
         String restaurantName = mBundle.getString("ResturantName");
         restaurant = new Restaurant(restaurantId, restaurantName,"","","","","", "", "", "", "", 0, "", "", "", latitude,longitude,"");
         
         final ActionBar ab = getSupportActionBar();
         ab.setTitle(restaurant.getName());
         ab.setDisplayHomeAsUpEnabled(true);
         
         findViews();
         getCurrentLocation();
         
         new DownloadRestaurantTask().execute();
         
     }
     
     private void getCurrentLocation() {
     	GPSTracker mGPS = new GPSTracker(this);
 
     	if(mGPS.canGetLocation() ){
 
     		latitude =mGPS.getLatitude();
     		longitude=mGPS.getLongitude();
 
     	}else{
     	// can't get the location
     	}
 		
 	}
 
 	private class DownloadRestaurantTask extends AsyncTask {
 
 
 		@Override
         protected void onPreExecute() {
             super.onPreExecute();
 
         }
 
         @Override
         protected Object doInBackground(Object... params) {
         	restaurant = RestaurantAPI.getRestaurant(restaurant.getId());
         	notes = RestaurantAPI.getRestaurantNotes(restaurant.getId(), 1);
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
         	FragmentPagerAdapter adapter = new NotePagerAdapter(getSupportFragmentManager());
             pager = (ViewPager) findViewById(R.id.pager);
             pager.setAdapter(adapter);
 
             CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
             indicator.setViewPager(pager);
       	    progressLayout.setVisibility(View.GONE);
     		setViews();
             super.onPostExecute(result);
 
         }
     }
 	
 
     private void setViews() {
     	
     	final ActionBar ab = getSupportActionBar();
         ab.setTitle(restaurant.getName());
     	address_text.setText(restaurant.getAddress());
 		opentime_text.setText(restaurant.getOpenTime());
 		price_text.setText(restaurant.getPrice());
 		restaurant_intro_text.setText(restaurant.getIntroduction());
 		
 		if(restaurant.getOfficialLink().equals("null")){
 			official_btn.setVisibility(View.GONE);
 			
 		}else{
 			official_link_none_text.setVisibility(View.GONE);
 			official_btn.setOnClickListener(new OnClickListener() {
 	            @Override
 	            public void onClick(View v) {
 	            	Uri uri = Uri.parse(restaurant.getOfficialLink());
 	            	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 	            	startActivity(intent);
 	            }
 	        });	
 		}
 
     	share_btn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
             	Intent intent = new Intent(Intent.ACTION_SEND);
             	intent.setType("text/plain");
             	intent.putExtra(android.content.Intent.EXTRA_TEXT, "推薦餐廳 :" + restaurant.getName()+"\n");
             	startActivity(intent); 
             }
         });
     	
     	place_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
             	Uri uri = Uri.parse("geo:0,0?q="+restaurant.getAddress() );
 				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 				startActivity(intent);
             }
         });
     	direction_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
             	Intent intent = new Intent(Intent.ACTION_VIEW,
             			Uri.parse("http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+restaurant.getAddress()+"&hl=tw"));
             			startActivity(intent);
             }
         });
     	favorite_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
             	SQLiteRestaurant db = new SQLiteRestaurant(RestaurantIntroActivity.this);
             	if (db.isRestaurantCollected(restaurant.getId())){
             		favorite_button.setImageResource( R.drawable.icon_heart_grey );
             		db.deleteRestaurant(restaurant);
             	}else{
             		favorite_button.setImageResource( R.drawable.icon_heart );
             		db.insertRestaurant(restaurant);
             	}
             
             }
         });
     	
     	SQLiteRestaurant db = new SQLiteRestaurant(RestaurantIntroActivity.this);
     	if (db.isRestaurantCollected(restaurant.getId())){
     		favorite_button.setImageResource( R.drawable.icon_heart );
     	}else{
     		favorite_button.setImageResource( R.drawable.icon_heart_grey );
     	}
 	}
 
 	private void findViews() {
 		share_btn = (ImageButton)findViewById(R.id.share_button);
 		direction_button = (ImageButton)findViewById(R.id.direction_button);
 		place_button = (ImageButton)findViewById(R.id.place_button);
 		favorite_button = (ImageButton)findViewById(R.id.favorite_button);
 		address_text = (TextView)findViewById(R.id.address_text);
 		opentime_text = (TextView)findViewById(R.id.opentime_text);
 		price_text = (TextView)findViewById(R.id.price_text);
 		official_link_none_text = (TextView)findViewById(R.id.official_link_none);
 		restaurant_intro_text = (TextView)findViewById(R.id.restaurant_intro_text);
 		official_btn = (Button)findViewById(R.id.official_btn);
 		progressLayout = (LinearLayout) findViewById(R.id.layout_progress);
         layoutReload = (LinearLayout) findViewById(R.id.layout_reload);
         buttonReload = (Button) findViewById(R.id.button_reload);
 	}
 
 	class NotePagerAdapter extends FragmentPagerAdapter {
 
         public NotePagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             Fragment kk = new Fragment();
         	
             kk = RestaurantNotesFragment.newInstance(notes.get(position).getId(),notes.get(position).getRestaurantId(),notes.get(position).getTitle(),notes.get(position).getLink(), notes.get(position).getX(), notes.get(position).getY(),notes.get(position).getPicUrl());
             return kk;
         }
 
         @Override
         public int getCount() {
             return notes.size();
         }
     }
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(0, ID_IMPEACH, 5, "檢舉餐廳").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         return true;
     }
     
 	@Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         int itemId = item.getItemId();
         switch (itemId) {
         case android.R.id.home:
             finish();
             break;
         case ID_IMPEACH:
         	showImpeachDialog();
         	break;
             
         }
         return true;
     }
 
 	private void showImpeachDialog() {
 		LayoutInflater inflater = this.getLayoutInflater();
     	LinearLayout recomendLayout = (LinearLayout) inflater.inflate(R.layout.dialog_impeach,null);
     	final Spinner areaSpinner = (Spinner) recomendLayout.findViewById(R.id.spinnner);
     	final EditText impeach_reason = (EditText)recomendLayout.findViewById(R.id.impeach_reason);
     	
     	
     	final String[] items = {"餐廳已歇業","菜色不佳", "服務態度不好"};
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
         areaSpinner.setAdapter(adapter);
         
     	Builder a = new AlertDialog.Builder(this).setTitle("檢舉餐廳").setIcon(R.drawable.icon)
         .setPositiveButton("確定", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
             	Intent email = new Intent(Intent.ACTION_SEND);
             	email.putExtra(Intent.EXTRA_EMAIL, new String[]{"brotherkos@gmail.com"});		  
            	email.putExtra(Intent.EXTRA_SUBJECT, "嚴選餐廳問題回報:檢舉餐廳" );
            	email.putExtra(Intent.EXTRA_TEXT, 
            			"檢舉原因:" + items[areaSpinner.getSelectedItemPosition()]+"\n"+
            			"補充說明:" + impeach_reason.getText().toString());
             	email.setType("message/rfc822");
             	startActivity(Intent.createChooser(email, "Choose an Email client :"));
             }
         }).setNegativeButton("取消", null);
     	a.setView(recomendLayout);
     	a.show();
 		
 	}
 
 }
