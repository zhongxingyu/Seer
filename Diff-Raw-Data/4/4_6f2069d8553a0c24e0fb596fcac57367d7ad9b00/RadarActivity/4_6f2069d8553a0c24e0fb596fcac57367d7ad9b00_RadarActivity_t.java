 package com.tabbie.android.radar;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 
 public class RadarActivity extends ServerThreadActivity implements OnTabChangeListener {
 
   private static final String LIST_FEATURED_TAG = "Featured";
   private static final String EVENT_TAB_TAG = "Events";
   private static final String RADAR_TAB_TAG = "Radar";
 
   private TabHost tabHost;
   private ListView featuredListView;
   private ListView allListView;
   private ListView radarListView;
   private RadarCommonController commonController;
   private TextView myNameView;
   
   private String token;
   
   // FB junk
   private Facebook facebook = new Facebook("217386331697217");
   private SharedPreferences preferences;
   
   private class EventListAdapter extends ArrayAdapter<Event> {
 
     public EventListAdapter(Context context, int resource,
         int textViewResourceId, List<Event> events) {
       super(context, resource, textViewResourceId, events);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
       if (null == convertView) {
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = inflater.inflate(R.layout.event_list_element, null);
       }
       final Event e = getItem(position);
       TextView title = (TextView) convertView.findViewById(R.id.event_text);
       title.setText(e.name);
       
       ((TextView) convertView.findViewById(R.id.event_list_time)).setText(e.time);
       ((TextView) convertView.findViewById(R.id.event_location)).setText(e.venueName);
       final TextView upVotes = ((TextView) convertView.findViewById(R.id.upvotes));
       upVotes.setText(Integer.toString(e.radarCount));
       
       ImageView img = (ImageView) convertView.findViewById(R.id.event_image);
       try {
         img.setImageDrawable(Drawable.createFromStream(e.image.openStream(),
             "src"));
       } catch (IOException exception) {
         exception.printStackTrace();
       }
       
       final ImageView radarButton = (ImageView) convertView.findViewById(R.id.add_to_radar_image);
       
      radarButton.setSelected(e.isOnRadar());
      
       convertView.findViewById(R.id.list_list_element_layout).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
 	        if (null != e) {
 	          Intent intent = new Intent(RadarActivity.this, EventDetailsActivity.class);
 	          intent.putExtra("event", e);
 	          startActivity(intent);
 	        }
         }
       });
       
      
       convertView.findViewById(R.id.add_to_radar_image).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
           if (e.isOnRadar() && commonController.removeFromRadar(e)) {
             radarButton.setSelected(false);
           } else if (!e.isOnRadar() && commonController.addToRadar(e)){
             radarButton.setSelected(true);
           }
           upVotes.setText(Integer.toString(e.radarCount));
           if (tabHost.getCurrentTab() != 2) {
             ((EventListAdapter) radarListView.getAdapter()).notifyDataSetChanged();
           }
           if (tabHost.getCurrentTab() != 0) {
             ((EventListAdapter) featuredListView.getAdapter()).notifyDataSetChanged();
           }
           if (tabHost.getCurrentTab() != 1) {
             ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
           }
         }
       });
       
       convertView.findViewById(R.id.location_image).setOnClickListener(new OnClickListener() {        
         public void onClick(View v) {
           Intent intent = new Intent(RadarActivity.this, RadarMapActivity.class);
           intent.putExtra("controller", commonController);
           intent.putExtra("event", e);
           startActivity(intent);
         }
       });
       return convertView;
     }
 
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
     
     featuredListView = (ListView) findViewById(R.id.featured_event_list);
     allListView = (ListView) findViewById(R.id.all_event_list);
     radarListView = (ListView) findViewById(R.id.radar_list);
     myNameView = (TextView) findViewById(R.id.user_name);
 
     preferences = getPreferences(MODE_PRIVATE);
     String accessToken = preferences.getString("access_token", null);
     long expires = preferences.getLong("access_expires", 0);
     if (accessToken != null) {
         facebook.setAccessToken(accessToken);
     }
     if (expires != 0) {
         facebook.setAccessExpires(expires);
     }
     
     if(!facebook.isSessionValid()) {
       facebook.authorize(this, new String[] { "email" }, new DialogListener() {
         public void onComplete(Bundle values) {
           sendServerRequest(new ServerGetRequest( "https://graph.facebook.com/me/?access_token=" + facebook.getAccessToken(),
                                                   MessageType.FACEBOOK_LOGIN));
         }
     
         public void onFacebookError(FacebookError error) {}
     
         public void onError(DialogError e) {}
     
         public void onCancel() {}
       });
     }
     
     commonController = new RadarCommonController();
 
     tabHost = (TabHost) findViewById(android.R.id.tabhost);
     tabHost.setup();
     tabHost.setOnTabChangedListener(this);
     tabHost.getTabWidget().setDividerDrawable(R.drawable.divider_vertical_dark);
 
     Filter<Event> featuredOnly = new Filter<Event>() {
       @Override
       public boolean apply(Event o) {
         return o.featured;
       }
     };
 
     featuredListView.setAdapter(new EventListAdapter(this,
         R.id.featured_event_list, R.layout.event_list_element, commonController.featuredList));
     allListView.setAdapter(new EventListAdapter(this, R.id.all_event_list,
         R.layout.event_list_element, commonController.eventsList));
     radarListView.setAdapter(new EventListAdapter(this, R.id.radar_list,
         R.layout.event_list_element, commonController.radar));
     
     findViewById(R.id.map_button).setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
         Intent intent = new Intent(RadarActivity.this, RadarMapActivity.class);
         intent.putExtra("controller", commonController);
         startActivity(intent);
       }
     });
     
     setupTab(featuredListView, LIST_FEATURED_TAG);
     setupTab(allListView, EVENT_TAB_TAG);
     setupTab(radarListView, RADAR_TAB_TAG);
 
     // IMPERATIVE to keep this hack here, otherwise app fubars
     tabHost.setCurrentTab(2);
     tabHost.setCurrentTab(1);
     tabHost.setCurrentTab(0);
   }
 
   public void onTabChanged(String tabName) {
     if (tabName.equals(EVENT_TAB_TAG)) {
 
     } else if (tabName.equals(LIST_FEATURED_TAG)) {
 
     } else if (tabName.equals(RADAR_TAB_TAG)) {
 
     }
   }
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     facebook.authorizeCallback(requestCode, resultCode, data);
   }
   
   @Override
   public void onResume() {    
     super.onResume();
     facebook.extendAccessTokenIfNeeded(this, null);
   }
   
   private void setupTab(final View view, final String tag) {
     View tabview = createTabView(tabHost.getContext(), tag);
 
     TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(new TabHost.TabContentFactory() {
       public View createTabContent(String tag) {return view;}
     });
     tabHost.addTab(setContent);
 
   }
 
   private static View createTabView(final Context context, final String text) {
     View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
     TextView tv = (TextView) view.findViewById(R.id.tabsText);
     tv.setText(text);
     return view;
   }
 
   @SuppressLint({ "ParserError", "ParserError" })
 @Override
   protected boolean handleServerResponse(ServerResponse resp) {
     if (MessageType.FACEBOOK_LOGIN == resp.responseTo) {
       JSONObject json = resp.parseJsonContent();
       if (json == null || !json.has("id")) {
         return false;
       }
       final Long facebookId;
       final String facebookName;
       try {
         facebookId = json.getLong("id");
         facebookName = json.getString("first_name") + " " + json.getString("last_name").substring(0, 1) + ".";
       } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return false;
       }
       // Now that we have our Facebook user info, we can send this to Tabbie to
       // get our Tabbie id
       this.runOnUiThread(new Runnable() {
         public void run() {
           myNameView.setText(facebookName);
           ServerPostRequest req = new ServerPostRequest(  ServerThread.TABBIE_SERVER + "/mobile/auth.json",
                                                           MessageType.TABBIE_LOGIN);
           req.params.put("fb_token", facebook.getAccessToken());
           
           sendServerRequest(req);
         }
       });
     } else if (MessageType.TABBIE_LOGIN == resp.responseTo) {
       JSONObject json = resp.parseJsonContent();
       if (null == json || !json.has("token")) {
         return false;
       }
       try {
         token = json.getString("token");
         this.runOnUiThread(new Runnable() {
           public void run() {
             ServerGetRequest req = new ServerGetRequest(  ServerThread.TABBIE_SERVER + "/mobile/events.json?auth_token=" + token,
                                                           MessageType.LOAD_EVENTS);
             sendServerRequest(req);
           }
         });
       } catch (JSONException e) {
         e.printStackTrace();
         return false;
       }
     } else if (MessageType.LOAD_EVENTS == resp.responseTo) {
       JSONArray list = resp.parseJsonArray();
       if (null == list) {
         Log.d("Hi", "In here2");
         return false;
       }
       Log.d("Hi", "In here");
       for (int i = 0; i < list.length(); ++i) {
         try {
           JSONObject obj = list.getJSONObject(i);
           Log.d("Hi", "::" + i + " " + obj.getBoolean("featured"));
           String radarCountStr = obj.getString("user_count");
           int radarCount = 0;
           if (null != radarCountStr && 0 != radarCountStr.compareTo("null")) {
             radarCount = Integer.parseInt(radarCountStr);
           }
           String time = obj.getString("start_time");
           Date d = parseRFC3339Date(time);
           String dd = (d.getHours() > 12 ? d.getHours() - 12 : d.getHours()) + "";
           if (d.getMinutes() > 0) {
             dd += ":" + d.getMinutes();
           }
           dd += "pm";
           String title = obj.getString("name");
           if (title.length() > 31) {
             title = title.substring(0, 31) + "...";
           }
           Event e = new Event(  obj.getString("id"),
                                 title,
                                 obj.getString("description"),
                                 obj.getString("street_address"),
                                 new URL("http://tabb.ie" + obj.getString("image_url")),
                                 obj.getDouble("latitude"),
                                 obj.getDouble("longitude"),
                                 radarCount,
                                 obj.getBoolean("featured"),
                                 dd
                                 );
           commonController.addEvent(e);
         } catch (JSONException e) {
           e.printStackTrace();
           return false;
         } catch (IndexOutOfBoundsException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         } catch (ParseException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         } catch (MalformedURLException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       }
       commonController.order();
       this.runOnUiThread(new Runnable() {
         public void run() {
           ((EventListAdapter) featuredListView.getAdapter()).notifyDataSetChanged();
           ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
           ((EventListAdapter) radarListView.getAdapter()).notifyDataSetChanged();
         }
       });
     }
     return false;
   }
   
   public static java.util.Date parseRFC3339Date(String datestring) throws java.text.ParseException, IndexOutOfBoundsException{
     Date d = new Date();
 
         //if there is no time zone, we don't need to do any special parsing.
     if(datestring.endsWith("Z")){
       try{
         SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");//spec for RFC3339          
         d = s.parse(datestring);      
       }
       catch(java.text.ParseException pe){//try again with optional decimals
         SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");//spec for RFC3339 (with fractional seconds)
         s.setLenient(true);
         d = s.parse(datestring);      
       }
       return d;
     }
 
          //step one, split off the timezone. 
     String firstpart = datestring.substring(0,datestring.lastIndexOf('-'));
     String secondpart = datestring.substring(datestring.lastIndexOf('-'));
     
           //step two, remove the colon from the timezone offset
     secondpart = secondpart.substring(0,secondpart.indexOf(':')) + secondpart.substring(secondpart.indexOf(':')+1);
     datestring  = firstpart + secondpart;
     SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");//spec for RFC3339    
     try{
       d = s.parse(datestring);      
     }
     catch(java.text.ParseException pe){//try again with optional decimals
       s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");//spec for RFC3339 (with fractional seconds)
       s.setLenient(true);
       d = s.parse(datestring);      
     }
     return d;
   }
 }
