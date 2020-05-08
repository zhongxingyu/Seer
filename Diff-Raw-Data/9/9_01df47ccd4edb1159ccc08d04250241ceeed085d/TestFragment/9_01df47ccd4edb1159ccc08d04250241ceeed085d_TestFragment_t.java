 package com.github.remomueller.tasktracker.android;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.widget.Toast;
 
 // From libs directory
 import org.apache.commons.io.IOUtils;
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 
 import com.github.remomueller.tasktracker.android.util.Base64;
 import com.github.remomueller.tasktracker.android.util.UserFunctionsGSON;
 
 public class TestFragment extends SherlockFragment {
     private static final String TAG = "TaskTrackerAndroid";
 
     private int position = -1;
     ListView list;
 
     public final static String STICKY_POSITION = "com.github.remomueller.tasktracker.android.stickies.STICKY_POSITION";
     public final static String STICKY_ID = "com.github.remomueller.tasktracker.android.stickies.STICKY_ID";
     public final static String STICKY_DESCRIPTION = "com.github.remomueller.tasktracker.android.stickies.STICKY_DESCRIPTION";
     public final static String STICKY_GROUP_DESCRIPTION = "com.github.remomueller.tasktracker.android.stickies.STICKY_GROUP_DESCRIPTION";
     public final static String STICKY_DUE_DATE = "com.github.remomueller.tasktracker.android.stickies.STICKY_DUE_DATE";
     public final static String STICKY_COMPLETED = "com.github.remomueller.tasktracker.android.stickies.STICKY_COMPLETED";
 
     public final static String TAG_ID = "com.github.remomueller.tasktracker.android.stickies.TAG_ID";
     public final static String TAG_NAME = "com.github.remomueller.tasktracker.android.stickies.TAG_NAME";
     public final static String TAG_COLOR = "com.github.remomueller.tasktracker.android.stickies.TAG_COLOR";
 
     UserFunctionsGSON userFunctionsGSON;
 
     public ArrayList<Sticky> stickies = new ArrayList<Sticky>();
     StickyAdapter stickyAdapter;
 
     public static TestFragment newInstance(int location) {
         TestFragment fragment = new TestFragment();
         fragment.position = location;
         return fragment;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 
         userFunctionsGSON = new UserFunctionsGSON();
         new DownloadJSONSTickies().execute(Integer.toString(position));
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.stickies_index, container, false);
         list=(ListView)view.findViewById(R.id.stickies_list);

        stickyAdapter = new StickyAdapter(getActivity(), stickies);
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         list.setAdapter(stickyAdapter);
     }
 
 
     private class DownloadJSONSTickies extends AsyncTask<String, Void, String> {
         @Override
         protected String doInBackground(String... tab_page_nums) {
             try {
                 return getStickiesJSON(Integer.parseInt(tab_page_nums[0]));
             } catch (IOException e) {
                 return "Unable to Connect: Make sure you have an active network connection.";
             }
         }
         // onPostExecute displays the results of the AsyncTask.
         @Override
         protected void onPostExecute(String json) {
             String result = "";
 
             Gson gson = new Gson();
 
             Sticky[] stickies_array;
 
             try {
                 stickies_array = gson.fromJson(json, Sticky[].class);
                 if(stickies_array == null){
                     stickies_array = new Sticky[0];
                 }
                 result = stickies_array.length + " Stick" + (stickies_array.length == 1 ? "y" : "ies");
             } catch (JsonParseException e) {
                 stickies_array = new Sticky[0];
                 result = "Login Failed: Please make sure your email and password are correct.";
             }
 
             for(int i = 0; i < stickies_array.length; i++){
                 stickies.add(stickies_array[i]);
             }
 
            if(stickyAdapter != null){
                stickyAdapter.notifyDataSetChanged();
            }
        }
     }
 
     private String getStickiesJSON(int location) throws IOException {
       InputStream is = null;
       int len = 1000;
 
       try {
         Date date = new Date();
         SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
         String s = formatter.format(date);
         String due_date_end_date = s;
 
         String email = userFunctionsGSON.getEmail(getActivity().getApplicationContext());
         String password = userFunctionsGSON.getPassword(getActivity().getApplicationContext());
         String site_url = userFunctionsGSON.getSiteURL(getActivity().getApplicationContext());
 
         String params = "";
 
         Log.d(TAG, "Location '" + location + "'");
 
         if(location == 0) { // Recently Completed
             Log.d(TAG, "Recently Completed");
             params = "status[]=completed&owner_id=me&order=stickies.due_date+DESC&due_date_end_date="+due_date_end_date;
         }else if(location == 2) { // Upcoming
             Log.d(TAG, "Upcoming");
             params = "status[]=planned&owner_id=me&order=stickies.due_date+ASC&due_date_start_date="+due_date_end_date;
         }else{ // Past Due
             Log.d(TAG, "Past Due");
             params = "status[]=planned&owner_id=me&order=stickies.due_date+DESC&due_date_end_date="+due_date_end_date;
         }
 
         URL url = new URL(site_url + "/stickies.json?" + params);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setReadTimeout(10000 /* milliseconds */);
         conn.setConnectTimeout(15000 /* milliseconds */);
         conn.setRequestMethod("GET"); /* Can be POST */
         conn.setDoInput(true);
         conn.setRequestProperty("Accept-Charset", "UTF-8");
         conn.setRequestProperty("Content-Type", "application/json");
         conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");
 
 
         String decoded = email+":"+password;
         String encoded = Base64.encodeBytes( decoded.getBytes() );
 
         conn.setRequestProperty("Authorization", "Basic "+encoded);
 
         // Starts the query
         conn.connect();
 
         int response = conn.getResponseCode();
         // Log.d(TAG, "The response is: " + response);
         is = conn.getInputStream();
 
         // Convert the InputStream into a string
         String contentAsString = "";
         if(is != null){
            contentAsString = readIt(is, len);
         }
 
         if(contentAsString == null){
             contentAsString = "";
         }
 
         return contentAsString;
 
       } finally {
         if (is != null) {
             is.close();
         }
       }
     }
 
     // Reads an InputStream and converts it to a String.
     public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
         String encoding = "UTF-8";
         StringWriter writer = new StringWriter();
         IOUtils.copy(stream, writer, encoding);
         return new String(writer.toString());
     }
 
 }
