 package com.centric.Recruit_Android;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.centric.recruit_models.Resource;
 import com.google.gson.reflect.TypeToken;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.loopj.android.http.PersistentCookieStore;
 import com.loopj.android.http.RequestParams;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import com.google.gson.Gson;
 
 import com.centric.Recruit_Android.CentricBdClient;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mike Brooks
  * Date: 7/24/13
  * Time: 1:25 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SearchableActivity extends ListActivity {
     public static final String SESSION_TOKEN = "TOKEN_KEY";
     private String ResourceJSON = null;
     private List<Resource> ResourceList = null;
     private static final int VIEW_RESOURCE = 1;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.searchresults);
 
         if (savedInstanceState != null) {
             ResourceJSON = savedInstanceState.getString("resources");
             ResourceList = parseToResourceList(ResourceJSON);
 
             ListAdapter adapter = new ListAdapter(getApplicationContext(), R.layout.resourcelistrow, ResourceList);
             setListAdapter(adapter);
 
         }
         else {
             // Get the intent, verify the action and get the query
             Intent intent = getIntent();
             if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                 Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
 
                 String query = intent.getStringExtra(SearchManager.QUERY) ;
                 String token = appData.getString(SESSION_TOKEN);
 
                 doSearch(query, token);
             }
         }
 
         ListView view = getListView();
 
         view.setOnItemClickListener(ResourcesOnItemClickListener);
     }
 
     private AdapterView.OnItemClickListener ResourcesOnItemClickListener = new AdapterView.OnItemClickListener() {
         public void onItemClick(AdapterView parent, View v, int position, long id) {
             Intent intent = new Intent(getBaseContext(), ViewResource.class);
             Resource item = ResourceList.get((int)id);
             intent.putExtra(getString(R.string.viewResource), item);
             startActivityForResult(intent, VIEW_RESOURCE);
         }
     };
 
     @Override
     protected void onSaveInstanceState(Bundle bundle) {
         bundle.putString("resources", ResourceJSON);
         super.onSaveInstanceState(bundle);
     }
 
     public static List<Resource> parseToResourceList(String json) {
         Gson gson = new Gson();
         return gson.fromJson(json, new TypeToken<List<Resource>>(){}.getType());
     }
 
 
     public void doSearch(String query, String sessionToken) {
         Resources res = getResources();
        String searchUrl = res.getString(R.string.search_url) + query + ".json";
        CentricBdClient.setApiToken(getApplicationContext(), sessionToken);
         CentricBdClient.get(searchUrl, null, new JsonHttpResponseHandler() {
             @Override
             public void onSuccess(JSONArray resourcesArray) {
                 System.out.println(resourcesArray.toString());
                 // Convert from JSONArray to Resources
                 if (resourcesArray != null && resourcesArray.length() > 0) {
                     Gson gson = new Gson();
                     ResourceJSON = resourcesArray.toString();
                     ResourceList = parseToResourceList(ResourceJSON);
 
                     ListAdapter adapter = new ListAdapter(getApplicationContext(), R.layout.resourcelistrow, ResourceList);
                     setListAdapter(adapter);
                 }
             }
 
             @Override
             public void onFailure(Throwable throwable, JSONArray jsonArray) {
                 super.onFailure(throwable, jsonArray);
                 System.out.println(throwable.toString());
 
             }
         });
     }
 
     public class ListAdapter extends ArrayAdapter<Resource> {
 
         public ListAdapter(Context context, int textViewResourceId) {
             super(context, textViewResourceId);
             // TODO Auto-generated constructor stub
         }
 
         private List<Resource> items;
 
         public ListAdapter(Context context, int resource, List<Resource> items) {
 
             super(context, resource, items);
 
             this.items = items;
 
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
 
             View v = convertView;
 
             if (v == null) {
 
                 LayoutInflater vi;
                 vi = LayoutInflater.from(getContext());
                 v = vi.inflate(R.layout.resourcelistrow, null);
 
             }
 
             Resource p = items.get(position);
 
             if (p != null) {
 
                 TextView tt = (TextView) v.findViewById(R.id.name);
 
                 if (tt != null) {
                     tt.setText(p.getFullName());
                 }
             }
 
             return v;
         }
     }
 }
