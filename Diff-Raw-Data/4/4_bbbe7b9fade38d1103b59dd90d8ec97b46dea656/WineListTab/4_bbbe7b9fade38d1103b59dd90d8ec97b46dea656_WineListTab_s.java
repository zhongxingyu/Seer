 package watsons.wine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.impl.client.TunnelRefusedException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import watsons.wine.notification.NotificationMainActivity;
 import android.R.string;
 import android.app.Activity;
 import android.app.ActivityGroup;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ExpandableListView.OnGroupClickListener;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SimpleAdapter;
 import android.widget.SimpleExpandableListAdapter;
 import android.widget.TextView;
 
 public class WineListTab extends Activity {
 	// url to make request
     private static String url = "http://watsonwine.bull-b.com/CodeIgniter_2.1.3/index.php/api/list_countries";
     
     // JSON Node names
     private static final String TAG_COUNTRIES = "countries";
     private static final String TAG_ID = "id";
     private static final String TAG_NAME = "name";
     private static final String TAG_PROVINCE = "province";
     private static final String TAG_PRODUCT_COUNT = "product_count";
 
  
     // contacts JSONArray
     JSONArray contries = null;
     JSONArray provinces = null;
     JSONArray provinces_children = null;
     private SimpleExpandableListAdapter mAdapter;
     Context mContext = WineListTab.this;   
     EditText et;
     ImageView iv;
     RelativeLayout rl;
 
     List<Integer> emptyList = new ArrayList<Integer>();
     // Hashmap for ListView
  	List<Map<String, String>> countryList = new ArrayList<Map<String, String>>();
  	List<List<Map<String, String>>> provinceList = new ArrayList<List<Map<String, String>>>();	
 
 	private JSONObject json;
 	private SharedPreferences sharedPreferences;
     
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.winelist_tab);
 		/* Wine List Tab Content */
 		// Search Bar Stuff
 		et = (EditText) findViewById(R.id.search_text);
 		et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 		    @Override
 		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 		        	InputMethodManager imm = 
 		                    (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
 		                 imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
 		            performSearch();
 		            return true;
 		        }
 		        return false;
 		    }
 		});
 		ImageButton ib = (ImageButton) findViewById(R.id.search_btn);
 		ib.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				performSearch();
 			}
 		});
 		
 		// Refresh Button
 		rl = (RelativeLayout) findViewById(R.id.refresh_img);
 		iv = (ImageView) findViewById(R.id.refresh_btn);
 		iv.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				performRefresh();
 			}
 			
 		});
 		
 		
 		new JsonTask().execute(url);
 
 		// Define a new Adapter
 		// First parameter - Context
 		// Second parameter - Layout for the row
 		// Third parameter - ID of the TextView to which the data is written
 		// Forth - the Array of data
 		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 		//  R.layout.list_wine_item, R.id.list_wineitem_text, contryArr);
 
 		 mAdapter = new SimpleExpandableListAdapter(
 	                this,
 	                countryList,
 	                R.layout.list_wine_item,
 	                new String[] {TAG_NAME},
 	                new int[] { R.id.list_wineitem_text },
 	                provinceList,
 	                R.layout.list_wine_item_child,
 	                new String[] {TAG_NAME},
 	                new int[] {	R.id.list_wineitem_text_child }
 	                )
 		 {
 			@Override
 			public View getGroupView (int groupPosition, 
 						boolean isExpanded, 
 						View convertView, 
 						ViewGroup parent) 
 			{
 					//final View v = super.getGroupView( groupPosition, isExpanded, convertView, parent);
 					//convertView = newGroupView(isExpanded, parent);
 					LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 					View view = li.inflate(R.layout.list_wine_item, null);
 					ImageView inidicatorImage = (ImageView) view.findViewById(R.id.explist_indicator);
 					
 					if ( getChildrenCount( groupPosition ) == 0 ) 
 					{
 						emptyList.add(groupPosition);
 						inidicatorImage .setVisibility( View.INVISIBLE	 );
 					}
 					else
 					{	
 						inidicatorImage.setImageResource(isExpanded?R.drawable.arrow_down:R.drawable.arrow_right);
 						inidicatorImage .setVisibility( View.VISIBLE );
 					}
 					TextView tv = (TextView) view.findViewById(R.id.list_wineitem_text);
 		            tv.setText(countryList.get(groupPosition).get(TAG_NAME));
 					return view;
 			}
 			
 			public View getChildView(int groupPosition, int childPosition, 
 				    boolean isLastChild, View convertView, ViewGroup parent) 
 			{
 				LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				View view = li.inflate(R.layout.list_wine_item_child, null);
 				TextView tv = (TextView) view.findViewById(R.id.list_wineitem_text_child);
 	            tv.setText(provinceList.get(groupPosition).get(childPosition).get(TAG_NAME));
 			    return view;
 			}
 			 
 		 };
 
 		// Assign adapter to ListView
 		ExpandableListView listView = (ExpandableListView) findViewById(R.id.list_wine);
 		listView.setAdapter(mAdapter);
 		listView.setDividerHeight(0);
 		listView.setOnChildClickListener(new OnChildClickListener() 
 		{
 	        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
 	                int childPosition, long id) 
 	        {
 	        	Bundle bundle = new Bundle();
 	        	bundle.putBoolean("country", false);
 	    		bundle.putBoolean("search", false);
 	        	bundle.putString("id", provinceList.get(groupPosition).get(childPosition).get(TAG_ID));
 	        	bundle.putString("name", provinceList.get(groupPosition).get(childPosition).get(TAG_NAME));
 	        	bundle.putString("country_name", countryList.get(groupPosition).get(TAG_NAME));
 	        	Constants.SHOW_DETAILS = true;
 	        	Intent intent = new Intent(getParent(), WineListProduct.class);
 	        	TabGroupBase parentActivity = (TabGroupBase)getParent();
 	        	intent.putExtras(bundle);
 	        	parentActivity.startChildActivity("WineProductActivity", intent);
 	        	return true;
 	        }
 		});
 		
 		listView.setOnGroupClickListener(new OnGroupClickListener()
 		{
 			public boolean onGroupClick(ExpandableListView parent, View v,
 					int groupPosition, long id) 
 			{
 				if ( emptyList.contains(groupPosition) ) 
 				{
 					Bundle bundle = new Bundle();
 		        	bundle.putBoolean("country", true);
 		    		bundle.putBoolean("search", false);
 		        	bundle.putString("id", countryList.get(groupPosition).get(TAG_ID));
 		        	bundle.putString("name", countryList.get(groupPosition).get(TAG_NAME));
 		        	bundle.putString("country_name", countryList.get(groupPosition).get(TAG_NAME));
 		        	Constants.SHOW_DETAILS = true;
 		        	Intent intent = new Intent(getParent(), WineListProduct.class);
 		        	TabGroupBase parentActivity = (TabGroupBase)getParent();
 		        	intent.putExtras(bundle);
 		        	parentActivity.startChildActivity("WineProductActivity", intent);
 				}
 				return false;
 			}
 			
 			
 		});
 		
 		ImageButton home_button = (ImageButton)findViewById(R.id.cellar_home_button);
         home_button.setOnClickListener(new OnClickListener(){
 
  			@Override
  			public void onClick(View v) {
  				
  				finish();
  				
  			}
      	   
         });
         
         ImageButton mail_button = (ImageButton)findViewById(R.id.cellar_mail_button);
         mail_button.setOnClickListener(new OnClickListener(){
 
  			@Override
  			public void onClick(View v) {
  				
  				Intent intent = new Intent(getParent(), NotificationMainActivity.class);
 				TabGroupBase parentActivity = (TabGroupBase)getParent();
 	        	parentActivity.startChildActivity("MyCellarsMainCallByMail", intent);
  				
  			}
      	   
         });
 	}
 	
 	protected void performRefresh()
 	{
 		rl.setVisibility(View.VISIBLE);
 		iv.setVisibility(View.INVISIBLE);
 		//SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.remove("json_wine");
 		editor.commit();
 		Handler handler = new Handler(); 
 	    handler.postDelayed(new Runnable() { 
 	         public void run() { 
 	        	Intent intent = new Intent(getParent(), WineListTab.class);
 	     	    TabGroupBase parentActivity = (TabGroupBase)getParent();
 	     		parentActivity.startChildActivityNotAddId("WineListTab", intent);
 	         } 
 	    }, 500); 
 	}
 
 	/*@Override
 	protected void onRestart() {
 	    super.onRestart();
 	    
 	
 	}*/
 
 	protected void performSearch() {
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     	imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 		
 		String search_str = et.getText().toString();
 		if (search_str.equals(""))
 			return;
 		Bundle bundle = new Bundle();
 		bundle.putBoolean("search", true);
     	bundle.putString("search_str", search_str);
     	Constants.SHOW_DETAILS = true;
     	Intent intent = new Intent(getParent(), WineListProduct.class);
     	TabGroupBase parentActivity = (TabGroupBase)getParent();
     	intent.putExtras(bundle);
     	parentActivity.startChildActivity("WineProductActivity", intent);
 	}
 
 
 	public void replaceContentView(String id, Intent newIntent) {
 	    View view = ((ActivityGroup) ((Activity) mContext).getParent())
 	            .getLocalActivityManager()
 	            .startActivity(id,
 	                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
 	            .getDecorView();
 	    ((Activity) mContext).setContentView(view);
 	}
 	
 	private class JsonTask extends AsyncTask<String, Void, String> {
 			
 			ProgressDialog pdia;
 			Boolean quitTask;
 			Boolean skipBGTask;
 			String resultJsonStr;
 			
 			@Override
 			protected void onPreExecute() {
 				super.onPreExecute();
 				pdia = new ProgressDialog(getParent());
 	            pdia.setMessage("Loading...");
 	            pdia.setCancelable(false);
 	            pdia.show();   
 	            quitTask = false;
 	            skipBGTask = false;
 	            
 	            //by stark  
 	            sharedPreferences = getPreferences(MODE_PRIVATE);
 	            resultJsonStr = sharedPreferences.getString("json_wine", null);
 				if(resultJsonStr != null)
 				{
 					skipBGTask = true;
 				}
 			}
 	
 			@Override
 			protected void onPostExecute(String result) {
 				super.onPostExecute(result);
 				pdia.dismiss();
 				
 				if (quitTask) {
 					
 					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 							WineListTab.this.getParent());
 					alertDialogBuilder.setTitle("Warnings!");
 					alertDialogBuilder
 							.setMessage("Cannot connect. Please check your network and try again later.")
 							.setCancelable(true)
 							.setPositiveButton("OK",new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,int id) {
 									dialog.cancel();
 								}
 							});
 					AlertDialog alertDialog = alertDialogBuilder.create();
 					alertDialog.show();
 					
 					return;
 				}
 				else {
 					
 					try {
 						json = new JSONObject(resultJsonStr);
 					} catch (JSONException e1) {
 						e1.printStackTrace();
 					}
 					
 					try {
 			            // Getting Array of Contacts
 			        	contries = json.getJSONArray(TAG_COUNTRIES);
 
 			            // looping through All Contacts
 			            for(int i = 0; i < contries.length(); i++){
 			                JSONObject c = contries.getJSONObject(i);
 			 
 			                // Storing each json item in variable
 			                String id = c.getString(TAG_ID);
 			                String name = c.getString(TAG_NAME);
 			                //String province = c.getString(TAG_PROVINCE);
 			                String product_count = c.getString(TAG_PRODUCT_COUNT);
 			                
 			                // Provinces is again a JSON Object
 			                try
 			                {
 			                	provinces = c.getJSONArray(TAG_PROVINCE);
 			                }
 			                catch (Exception e) {
 			                    Log.e("Json Error", "province converting result " + e.toString());       
 			                }
 
 			                List<Map<String, String>> children = new ArrayList<Map<String, String>>();
 			                for(int j = 0; j < provinces.length(); j++){
 			                	JSONObject p = provinces.getJSONObject(j);
 				                String province_id = p.getString(TAG_ID);
 				                String province_name = p.getString(TAG_NAME);
 				                String province_product_count = p.getString(TAG_PRODUCT_COUNT);
 				                
 				                // creating new HashMap
 				                HashMap<String, String> p_map = new HashMap<String, String>();
 				                p_map.put(TAG_ID, province_id);
 				                p_map.put(TAG_NAME, province_name);
 				                p_map.put(TAG_PRODUCT_COUNT, province_product_count);
 				                children.add(p_map);
 			                }
 			                provinceList.add(children);
 			                
 			                // creating new HashMap
 			                HashMap<String, String> map = new HashMap<String, String>();
 			                // adding each child node to HashMap key => value
 			                map.put(TAG_ID, id);
 			                map.put(TAG_NAME, name);
 			                //map.put(TAG_PROVINCE, province);
 			                map.put(TAG_PRODUCT_COUNT, product_count);
 			 
 			                // adding HashList to ArrayList
 			                countryList.add(map);
 			            }
 			        } 
 			        catch (JSONException e) {
 			            e.printStackTrace();
 			        }
 					
 					mAdapter.notifyDataSetChanged();
 				}				
 			}
 
 			@Override
 			protected String doInBackground(String... arg0) {
 				
 				if (skipBGTask) {
 					return null;
 				}
 				
 				JSONParser jParser = new JSONParser();
 				json = jParser.getJSONFromUrl(url);
 				if(json == null)
 				{
 					quitTask = true;
 					return null;
 				}
 				resultJsonStr = json.toString();
 				return null;
 				
 				
 				/*sharedPreferences = getPreferences(MODE_PRIVATE);
 				String strJson = sharedPreferences.getString("json_wine", null);
 				
 				if(strJson != null)
 				{
 					try {
 						json = new JSONObject(strJson);
 					} catch (JSONException e1) {
 						e1.printStackTrace();
 					}
 				}
 				else
 				{
 			        // Creating JSON Parser instance
 			        JSONParser jParser = new JSONParser(); 
 			        // getting JSON string from URL
 			        
 			        json = jParser.getJSONFromUrl(url);
 			        
 			        if(json == null)
 			        {
 			        	quitTask = true;
 			        	return null;
 			        }
 						        
 				    SharedPreferences.Editor editor = sharedPreferences.edit();
 				    editor.putString("json_wine", json.toString());
 				    editor.commit();
 				}
 		                
 		        try {
 		            // Getting Array of Contacts
 		        	contries = json.getJSONArray(TAG_COUNTRIES);
 
 		            // looping through All Contacts
 		            for(int i = 0; i < contries.length(); i++){
 		                JSONObject c = contries.getJSONObject(i);
 		 
 		                // Storing each json item in variable
 		                String id = c.getString(TAG_ID);
 		                String name = c.getString(TAG_NAME);
 		                //String province = c.getString(TAG_PROVINCE);
 		                String product_count = c.getString(TAG_PRODUCT_COUNT);
 		                
 		                // Provinces is again a JSON Object
 		                try
 		                {
 		                	provinces = c.getJSONArray(TAG_PROVINCE);
 		                }
 		                catch (Exception e) {
 		                    Log.e("Json Error", "province converting result " + e.toString());       
 		                }
 
 		                List<Map<String, String>> children = new ArrayList<Map<String, String>>();
 		                for(int j = 0; j < provinces.length(); j++){
 		                	JSONObject p = provinces.getJSONObject(j);
 			                String province_id = p.getString(TAG_ID);
 			                String province_name = p.getString(TAG_NAME);
 			                String province_product_count = p.getString(TAG_PRODUCT_COUNT);
 			                
 			                // creating new HashMap
 			                HashMap<String, String> p_map = new HashMap<String, String>();
 			                p_map.put(TAG_ID, province_id);
 			                p_map.put(TAG_NAME, province_name);
 			                p_map.put(TAG_PRODUCT_COUNT, province_product_count);
 			                children.add(p_map);
 		                }
 		                provinceList.add(children);
 		                
 		                // creating new HashMap
 		                HashMap<String, String> map = new HashMap<String, String>();
 		                // adding each child node to HashMap key => value
 		                map.put(TAG_ID, id);
 		                map.put(TAG_NAME, name);
 		                //map.put(TAG_PROVINCE, province);
 		                map.put(TAG_PRODUCT_COUNT, product_count);
 		 
 		                // adding HashList to ArrayList
 		                countryList.add(map);
 		            }
 		        } 
 		        catch (JSONException e) {
 		            e.printStackTrace();
 		        }*/
 				// TODO Auto-generated method stub
 				//return null;
 			}
 	}
 }
