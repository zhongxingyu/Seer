 package watsons.wine;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class LocationList extends Activity {
 	
     Context mContext = LocationList.this;
     ArrayList<String> idList;
     ArrayList<String> nameList;
     ArrayList<String> nameListHK = new ArrayList<String>();
     ArrayList<String> nameListKL = new ArrayList<String>();
     ArrayList<String> nameListNT = new ArrayList<String>();
     ArrayList<String> nameListOthers = new ArrayList<String>();
     ArrayList<String> districtList;
     List<Map<String, String>> idNameMap = new ArrayList<Map<String, String>>();
     static HashMap<String, String> hm_id_name = new HashMap<String, String>();
     static HashMap<String, String> hm_name_district = new HashMap<String, String>();
     static Boolean hashed = false;
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/* Location List Content */
 		setContentView(R.layout.location_list);
 		
 		Bundle bundle = this.getIntent().getExtras();
 		idList = bundle.getStringArrayList("idList"); 
 	    nameList = bundle.getStringArrayList("nameList"); 
 	    districtList = bundle.getStringArrayList("districtList");
 	    if (hashed == false)
 	    {
 		    for (int i=0; i<idList.size();i++)
 		    {
 		    	hm_id_name.put(nameList.get(i),idList.get(i));
 		    	hm_name_district.put(nameList.get(i), districtList.get(i));
 		    }
 		    hashed = true;
 	    }
 	    
 	    for (int i=0;i<idList.size();i++)
 	    {
 	    	if (hm_name_district.get(nameList.get(i)).equals(getString(R.string.hk)))
 	    	{
 	    		nameListHK.add(nameList.get(i));
 	    	}
 	    	else if (hm_name_district.get(nameList.get(i)).equals(getString(R.string.kl)))
 	    	{
 	    		nameListKL.add(nameList.get(i));
 	    	}
 	    	else if (hm_name_district.get(nameList.get(i)).equals(getString(R.string.nt)))
 	    	{
 	    		nameListNT.add(nameList.get(i));
 	    	}
 	    	else
 	    	{
 	    		nameListOthers.add(nameList.get(i));
 	    	}
 	    }
 	    Collections.sort(nameListHK);
 	    Collections.sort(nameListKL);
 	    Collections.sort(nameListNT);
 	    Collections.sort(nameListOthers);
 	    
 		ListView lv_hk = (ListView) findViewById(R.id.list_location_hk);
 		ListView lv_kl = (ListView) findViewById(R.id.list_location_kl);
 		ListView lv_nt = (ListView) findViewById(R.id.list_location_nt);
 		ListView lv_others = (ListView) findViewById(R.id.list_location_others);
         
         ArrayAdapter<String> adapter_hk = new ArrayAdapter<String>(this,
                 R.layout.list_location_item, nameListHK);
         ArrayAdapter<String> adapter_kl = new ArrayAdapter<String>(this,
         		R.layout.list_location_item, nameListKL);
         ArrayAdapter<String> adapter_nt = new ArrayAdapter<String>(this,
         		R.layout.list_location_item, nameListNT);
         ArrayAdapter<String> adapter_others = new ArrayAdapter<String>(this,
         		R.layout.list_location_item, nameListOthers);
        
         lv_hk.setAdapter(adapter_hk);
         lv_hk.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position,
                     long id) {
             	Bundle bundle = new Bundle();
             	int id2 = Integer.parseInt(hm_id_name.get(nameListHK.get(position)));
 		    	bundle.putInt("id", id2);
 		    	Intent intent = new Intent(getParent(), LocationWebView.class);
 	        	TabGroupBase parentActivity = (TabGroupBase)getParent();
 	        	intent.putExtras(bundle);
 	        	parentActivity.startChildActivity("LocationWebView", intent);
             }
         });
         
         lv_kl.setAdapter(adapter_kl);
         lv_kl.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position,
                     long id) {
             	Bundle bundle = new Bundle();
             	int id2 = Integer.parseInt(hm_id_name.get(nameListKL.get(position)));
 		    	bundle.putInt("id", id2);
 		    	Intent intent = new Intent(getParent(), LocationWebView.class);
 	        	TabGroupBase parentActivity = (TabGroupBase)getParent();
 	        	intent.putExtras(bundle);
 	        	parentActivity.startChildActivity("LocationWebView", intent);
             }
         });
         lv_nt.setAdapter(adapter_nt);
         lv_nt.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position,
                     long id) {
             	Bundle bundle = new Bundle();
             	int id2 = Integer.parseInt(hm_id_name.get(nameListNT.get(position)));
 		    	bundle.putInt("id", id2);
 		    	Intent intent = new Intent(getParent(), LocationWebView.class);
 	        	TabGroupBase parentActivity = (TabGroupBase)getParent();
 	        	intent.putExtras(bundle);
 	        	parentActivity.startChildActivity("LocationWebView", intent);
             }
         });
         lv_others.setAdapter(adapter_others);
         lv_others.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position,
                     long id) {
             	Bundle bundle = new Bundle();
             	int id2 = Integer.parseInt(hm_id_name.get(nameListOthers.get(position)));
 		    	bundle.putInt("id", id2);
 		    	Intent intent = new Intent(getParent(), LocationWebView.class);
 	        	TabGroupBase parentActivity = (TabGroupBase)getParent();
 	        	intent.putExtras(bundle);
 	        	parentActivity.startChildActivity("LocationWebView", intent);
             }
         });
 	}
 }
