 package watsons.wine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import watsons.wine.Constants;
 
 import android.app.Activity;
 import android.app.ActivityGroup;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.SimpleExpandableListAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ExpandableListView.OnGroupClickListener;
 
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
     private ExpandableListAdapter mAdapter;
     Context mContext = WineListTab.this;    
 
     List<Integer> emptyList = new ArrayList<Integer>();
     
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.winelist_tab);
 		/* Wine List Tab Content */
 		
 		// Hashmap for ListView
 		final List<Map<String, String>> countryList = new ArrayList<Map<String, String>>();
 		final List<List<Map<String, String>>> provinceList = new ArrayList<List<Map<String, String>>>();
 		
  
         // Creating JSON Parser instance
         JSONParser jParser = new JSONParser();
  
         // getting JSON string from URL
         JSONObject json = jParser.getJSONFromUrl(url);
                 
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
         } catch (JSONException e) {
             e.printStackTrace();
         }
         
         /*
         List<String> contryArr = new ArrayList<String>();
         List<String> provinceArr = new ArrayList<String>();
         for (int i = 0 ; i<contryList.size(); i++)
         {
         	contryArr.add(contryList.get(i).get(TAG_NAME));
         	
         }
         for (int i = 0 ; i<provinceList.size() ; i++)
         {
         	//provinceArr.add(provinceList.get(i).get(TAG_NAME));
         }*/
 
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
 		//int width = getWindowManager().getDefaultDisplay().getWidth();
 		//listView.setIndicatorBounds(width-GetDipsFromPixel(16), width-GetDipsFromPixel(5));
 		listView.setAdapter(mAdapter);
 		listView.setDividerHeight(2);
 		listView.setOnChildClickListener(new OnChildClickListener() 
 		{
 	        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
 	                int childPosition, long id) 
 	        {
 	        	Bundle bundle = new Bundle();
 	        	bundle.putBoolean("country", false);
 	        	bundle.putString("id", provinceList.get(groupPosition).get(childPosition).get(TAG_ID));
 	        	bundle.putString("name", provinceList.get(groupPosition).get(childPosition).get(TAG_NAME));
 	        	Intent intent = new Intent(WineListTab.this, WineListProduct.class);
 	        	intent.putExtras(bundle);
	        	startActivityForResult(intent, 0);
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
 		        	bundle.putString("id", countryList.get(groupPosition).get(TAG_ID));
 		        	bundle.putString("name", countryList.get(groupPosition).get(TAG_NAME));
 		        	Intent intent = new Intent(WineListTab.this, WineListProduct.class);
 		        	intent.putExtras(bundle);
 		        	//startActivityForResult(intent, 0);
 		        	Constants.SHOW_DETAILS = true;
 		        	replaceContentView("activity3", intent);
 				}
 				return false;
 			}
 			
 			
 		});
 		
 	}
 
 
 	public void replaceContentView(String id, Intent newIntent) {
 	    View view = ((ActivityGroup) ((Activity) mContext).getParent())
 	            .getLocalActivityManager()
 	            .startActivity(id,
 	                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
 	            .getDecorView();
 	    ((Activity) mContext).setContentView(view);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		super.onBackPressed();
 		if (Constants.SHOW_DETAILS) {
 		    Log.e("back", "pressed accepted");
 		    Constants.LIST_ACTIVITY = 0;
 		    Constants.SHOW_DETAILS = false;
 		    Intent intent = new Intent(WineListTab.this, WineListTab.class);
 		    replaceContentView("activity1", intent);
 		    //TabBarExample ParentActivity;
 		    //ParentActivity = (TabBarExample) this.getParent();
 		    //ParentActivity.getTabHost().setCurrentTab(0);
 	  	}
 		else
 		{
 			finish();
 		}
 		return;
 	}
 	
 	
 	
 	//Convert pixel to dip 
 	public int GetDipsFromPixel(float pixels)
 	{
 	        // Get the screen's density scale
 	        final float scale = getResources().getDisplayMetrics().density;
 	        // Convert the dps to pixels, based on density scale
 	        return (int) (pixels * scale + 0.5f);
 	} 
 }
