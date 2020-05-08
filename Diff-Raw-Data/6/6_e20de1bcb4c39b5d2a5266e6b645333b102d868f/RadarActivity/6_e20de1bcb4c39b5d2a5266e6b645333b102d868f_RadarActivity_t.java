 package com.tabbie.android.radar;
 
 import java.net.URL;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.tabbie.android.radar.http.ServerGetRequest;
 import com.tabbie.android.radar.http.ServerPostRequest;
 import com.tabbie.android.radar.http.ServerResponse;
 
 public class RadarActivity extends ServerThreadActivity implements
     OnTabChangeListener, EventListAdapter.EventClickListener,
     EventListAdapter.EventLocationClickListener {
   
   // Intent constants
   private static final String[] FOUNDERS_EMAIL = {"founders@tonight-life.com"};
   private static final String APP_FEEDBACK_SUBJECT = "TonightLife Application Feedback";
 
   // Often-used views
   private TabHost tabHost;
   private ListView currentListView;
   private ListView featuredListView;
   private ListView allListView;
   private ListView radarListView;
   private TextView myNameView;
 
   // Internal state for views
   private String token;
   private int currentViewPosition = 0;
   private boolean tabbieVirgin = true; // SharedPref variable to determine if
                                        // the tutorial should run
   private boolean forceFeatureTab = false; // Used to make sure the user can't
                                            // escape the tutorial
 
   // Controllers
   private RadarCommonController commonController;
   private UnicornSlayerController tutorialController;
 
   // FB junk
   private Facebook facebook = new Facebook("217386331697217");
   private SharedPreferences preferences;
   
   @Override
   public void onCreate(final Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
     Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 
     currentListView = featuredListView = (ListView) findViewById(R.id.featured_event_list);
     allListView = (ListView) findViewById(R.id.all_event_list);
     radarListView = (ListView) findViewById(R.id.radar_list);
     myNameView = (TextView) findViewById(R.id.user_name);
 
     // Spinning loading dialog
 
     ((ImageView) findViewById(R.id.loading_spin)).startAnimation(AnimationUtils
         .loadAnimation(this, R.anim.rotate));
 
     preferences = getPreferences(MODE_PRIVATE);
     // Facebook Access Token
     String accessToken = preferences.getString("access_token", null);
     long expires = preferences.getLong("access_expires", 0);
     // Check and see if the facebook access is still valid
     if (null != accessToken) {
       facebook.setAccessToken(accessToken);
     }
     if (0 != expires) {
       facebook.setAccessExpires(expires);
     }
 
     if (!facebook.isSessionValid()) {
       facebook.authorize(this, new String[] { "email" }, new DialogListener() {
         public void onComplete(Bundle values) {
           SharedPreferences.Editor editor = preferences.edit();
           editor.putString("access_token", facebook.getAccessToken());
           editor.putLong("access_expires", facebook.getAccessExpires());
           editor.commit();
           sendServerRequest(new ServerGetRequest(
               "https://graph.facebook.com/me/?access_token="
                   + facebook.getAccessToken(), MessageType.FACEBOOK_LOGIN));
         }
 
         public void onFacebookError(FacebookError error) {}
         public void onError(DialogError e) {}
         public void onCancel() {}
       });
     } else {
       // Already have fb session
       sendServerRequest(new ServerGetRequest(
           "https://graph.facebook.com/me/?access_token="
               + facebook.getAccessToken(), MessageType.FACEBOOK_LOGIN));
     }
 
     commonController = new RadarCommonController();
     tutorialController = new UnicornSlayerController(new AlertDialog.Builder(this));
 
     // Set up the Tab Host
     tabHost = (FlingableTabHost) findViewById(android.R.id.tabhost);
     tabHost.setup();
     tabHost.setOnTabChangedListener(this);
 
     featuredListView.setAdapter(new EventListAdapter(this, commonController.featuredList));
     allListView.setAdapter(new EventListAdapter(this, commonController.eventsList));
     radarListView.setAdapter(new EventListAdapter(this, commonController.radarList));
 
     findViewById(R.id.map_button).setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
         if (!forceFeatureTab) {
           Intent intent = new Intent(RadarActivity.this, RadarMapActivity.class);
           intent.putExtra("controller", commonController);
           intent.putExtra("token", token);
           startActivity(intent);
         } else {
           Toast.makeText(RadarActivity.this,
               "Please select an event to continue the tutorial",
               Toast.LENGTH_SHORT).show();
         }
       }
     });
 
     setupTab(featuredListView, getString(R.string.tab_top10));
     setupTab(allListView, getString(R.string.tab_all_events));
     setupTab(radarListView, getString(R.string.tab_short_list));
 
     tabHost.setCurrentTab(0);
 
     featuredListView.setFastScrollEnabled(true);
     allListView.setFastScrollEnabled(true);
     radarListView.setFastScrollEnabled(true);
   }
 
   public void onTabChanged(String tabName) {
     if (!tabbieVirgin) {
       findViewById(R.id.radar_list_empty_text).setVisibility(View.GONE);
       
 
       final View v;
       
       if(0 == commonController.eventsList.size()) {
     	v = findViewById(R.id.radar_list_empty_text);
       	v.setVisibility(View.VISIBLE);
       } else if (tabName.equals(getString(R.string.tab_all_events))) {
         v = findViewById(R.id.all_event_list);
 
         currentListView = allListView;
 
       } else if (tabName.equals(getString(R.string.tab_top10))) {
         v = findViewById(R.id.featured_event_list);
         currentListView = featuredListView;
 
       } else if (tabName.equals(getString(R.string.tab_short_list))) {
         if (0 == commonController.radarList.size()) {
           v = findViewById(R.id.radar_list_empty_text);
           v.setVisibility(View.VISIBLE);
         } else {
           v = findViewById(R.id.radar_list);
           commonController.order();
           ((EventListAdapter) radarListView.getAdapter())
               .notifyDataSetChanged();
         }
         currentListView = radarListView;
       } else
         throw new RuntimeException();
 
       PlayAnim(v, getBaseContext(), android.R.anim.fade_in, 100);
     } else if (forceFeatureTab) {
       tabHost.setCurrentTab(0); // TODO This probably shouldn't be hardcoded
       Toast.makeText(this, "Please select an event to continue the tutorial",
           Toast.LENGTH_SHORT).show();
     }
   }
 
   public Animation PlayAnim(View v, Context con, int animationId,
       int StartOffset) {
     if (null != v) {
       Animation animation = AnimationUtils.loadAnimation(con, animationId);
       animation.setStartOffset(StartOffset);
       v.startAnimation(animation);
 
       return animation;
     }
     return null;
   }
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    Log.v("Request Code", "This: " + requestCode);
     super.onActivityResult(requestCode, resultCode, data);
     switch (requestCode) {
     case RadarCommonController.RETRIEVE_INSTANCE:
       final Bundle controller = data.getExtras();
       commonController = controller.getParcelable("controller");
 
       // New adapters are necessary (for now) for the lists
       // to update if the user clicked anything in Details
       
       featuredListView.setAdapter(new EventListAdapter(this, commonController.featuredList));
 
       allListView.setAdapter(new EventListAdapter(this, commonController.eventsList));
 
       radarListView.setAdapter(new EventListAdapter(this, commonController.radarList));
 
       ((EventListAdapter) featuredListView.getAdapter()).notifyDataSetChanged();
       ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
       ((EventListAdapter) radarListView.getAdapter()).notifyDataSetChanged();
 
       currentListView.setSelection(currentViewPosition);
       if (forceFeatureTab) {
         forceFeatureTab = false;
       }
       break;
     case RadarCommonController.FIRE_EVENT:
     	final Event e = commonController.getEvent(data.getExtras().getString("event"));
         Intent intent = new Intent(RadarActivity.this,
                 EventDetailsActivity.class);
             intent.putExtra("eventId", e.id);
             intent.putExtra("controller", commonController);
             intent.putExtra("image",
             		((EventListAdapter) allListView.getAdapter()).
             		imageLoader.getBitmap(e.image.toString())); // TODO Make this code suck less
             intent.putExtra("token", token);
         startActivity(intent);
         break;
     default:
       facebook.authorizeCallback(requestCode, resultCode, data);
       break;
     }
   }
 
   @Override
   public void onResume() {
     super.onResume();
     facebook.extendAccessTokenIfNeeded(this, null);
   }
 
   private void setupTab(final View view, final String tag) {
     View tabview = createTabView(tabHost.getContext(), tag);
 
     TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview)
         .setContent(new TabHost.TabContentFactory() {
           public View createTabContent(String tag) {
             return view;
           }
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
   protected synchronized boolean handleServerResponse(ServerResponse resp) {
     if (MessageType.FACEBOOK_LOGIN == resp.responseTo) { // Deal with facebook login JSON
       JSONObject json = resp.parseJsonContent();
       if (json == null || !json.has("id")) {
         return false;
       }
       final String facebookName;
       try {
         facebookName = json.getString("first_name") + " "
             + json.getString("last_name").substring(0, 1) + ".";
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
           ServerPostRequest req = new ServerPostRequest(
               ServerThread.TABBIE_SERVER + "/mobile/auth.json",
               MessageType.TABBIE_LOGIN);
           req.params.put("fb_token", facebook.getAccessToken());
 
           sendServerRequest(req);
         }
       });
     } else if (MessageType.TABBIE_LOGIN == resp.responseTo) { // Deal with Tabbie Login JSON
       JSONObject json = resp.parseJsonContent();
       if (null == json || !json.has("token")) {
         return false;
       }
       try {
         token = json.getString("token");
         this.runOnUiThread(new Runnable() {
           public void run() {
             ServerGetRequest req = new ServerGetRequest(
                 ServerThread.TABBIE_SERVER + "/mobile/all.json?auth_token="
                     + token, MessageType.LOAD_EVENTS);
             sendServerRequest(req);
           }
         });
       } catch (JSONException e) {
         e.printStackTrace();
         return false;
       }
     } else if (MessageType.LOAD_EVENTS == resp.responseTo) { // Deal with loading event information from Tabbie
       JSONArray list = resp.parseJsonArray();
       
 
       Set<String> serverRadarIds = new LinkedHashSet<String>();
       try {
         JSONObject radarObj = list.getJSONObject(list.length() - 1);
         JSONArray tmpRadarList = radarObj.getJSONArray("radar");
         for (int i = 0; i < tmpRadarList.length(); ++i) {
           serverRadarIds.add(tmpRadarList.getString(i));
           Log.d("Here is id", tmpRadarList.getString(i));
         }
       } catch (JSONException e1) {
         e1.printStackTrace();
       }
 
       commonController.clear();
       for (int i = 0; i < list.length() - 1; ++i) {
         try {
           JSONObject obj = list.getJSONObject(i);
           String radarCountStr = obj.getString("user_count");
           int radarCount = 0;
 
           if (null != radarCountStr && 0 != radarCountStr.compareTo("null"))
             radarCount = Integer.parseInt(radarCountStr);
 
           final Event e = new Event(  obj.getString("id"),
                                       obj.getString("name"),
                                       obj.getString("description"),
                                       obj.getString("location"),
                                       obj.getString("street_address"),
                                       new URL("http://tonight-life.com" + obj.getString("image_url")),
                                       obj.getDouble("latitude"),
                                       obj.getDouble("longitude"),
                                       radarCount,
                                       obj.getBoolean("featured"),
                                       obj.getString("start_time"),
                                       serverRadarIds.contains(obj.getString("id")));
 
           commonController.addEvent(e);
 
         } catch (JSONException e) {
           Toast.makeText(this, "Fatal Error: Failed to Parse JSON",
               Toast.LENGTH_SHORT).show();
           e.printStackTrace();
           return false;
         } catch (final Exception e) {
           Log.e("RadarActivity",
               "Fatal Error: Non JSON-Exception during event creation");
           throw new RuntimeException();
         }
       }
       Log.d("RadarActivity", "Loading Benchmark 3, all events instantiated");
       commonController.order();
       this.runOnUiThread(new Runnable() {
         public void run() {
           ((EventListAdapter) featuredListView.getAdapter())
               .notifyDataSetChanged();
           ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
           ((EventListAdapter) radarListView.getAdapter())
               .notifyDataSetChanged();
 
           findViewById(R.id.loading_screen).setVisibility(View.GONE);
           findViewById(R.id.loading_screen_image).setVisibility(View.GONE);
           findViewById(R.id.loading_spin).setVisibility(View.GONE);
           findViewById(R.id.tonightlife_layout).setVisibility(View.VISIBLE);
          
          if(0 == commonController.eventsList.size() || 0 == commonController.featuredList.size())
          	findViewById(R.id.radar_list_empty_text).setVisibility(View.VISIBLE);
 
           tabbieVirgin = getPreferences(MODE_PRIVATE).getBoolean("virgin", true);
 
           if (tabbieVirgin) {
             tutorialController.showTabsTutorial(new UnicornSlayerController.TabsCallback() {
               @Override
               public void openRadarTab() {
                 tabHost.setCurrentTab(2);
               }
 
               @Override
               public void openFeaturedTab() {
                 tabHost.setCurrentTab(0);
                 forceFeatureTab = true;
               }
 
               @Override
               public void openEventsTab() {
                 tabHost.setCurrentTab(1);
               }
             }, preferences.edit());
           }
         }
       });
     }
     return false;
   }
 
   @Override
   public boolean onCreateOptionsMenu(final Menu menu) {
     final MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.main_menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(final MenuItem item) {
     // Handle item selection
 
     // TODO This doesn't work right now, don't know why
     switch (item.getItemId()) {
     case R.id.refresh_me:
       this.runOnUiThread(new Runnable() {
         public void run() {
           ServerGetRequest req = new ServerGetRequest(
               ServerThread.TABBIE_SERVER + "/mobile/all.json?auth_token="
                   + token, MessageType.LOAD_EVENTS);
           sendServerRequest(req);
         }
       });
 
       return true;
     case R.id.report_me:
     	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
     	emailIntent.setType("plain/text");
     	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, FOUNDERS_EMAIL);
     	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, APP_FEEDBACK_SUBJECT);
     	startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
     	return true;
     default:
       return super.onOptionsItemSelected(item);
     }
   }
 
   @Override
   public void onEventClicked(final Event e, final int position, final Bitmap image) {
 	Log.d("RadarActivity", "Callback to onEventClicked");
     if (null != e) {
         currentViewPosition = position;
         ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
             .vibrate(30);
 
         new AsyncTask<Void, Void, Intent>() {
 
           private ProgressDialog dialog;
 
           @Override
           protected void onPreExecute() {
             dialog = ProgressDialog.show(RadarActivity.this, "",
                 "Loading, please wait...");
             super.onPreExecute();
           }
 
           @Override
           protected Intent doInBackground(Void... params) {
             Intent intent = new Intent(RadarActivity.this,
                 EventDetailsActivity.class);
             intent.putExtra("eventId", e.id);
             intent.putExtra("controller", commonController);
             intent.putExtra("image", image);
             intent.putExtra("token", token);
             if (tabbieVirgin) {
               intent.putExtra("virgin", true); // Make sure this
                                                // activity knows it's in
                                                // tutorial mode
               tabbieVirgin = false; // User is no longer a prepubescent
                                     // pussy
               getPreferences(MODE_PRIVATE).edit()
                   .putBoolean("virgin", false).commit();
             }
             return intent;
           }
 
           @Override
           protected void onPostExecute(Intent result) {
             startActivityForResult(result,
                 RadarCommonController.RETRIEVE_INSTANCE);
             dialog.dismiss();
 
           };
         }.execute();
       }
 	}
 
 	@Override
 	public void onEventLocationClicked(final Event e) {
 	    Intent intent = new Intent(RadarActivity.this,
 	            RadarMapActivity.class);
 	        intent.putExtra("controller", commonController);
 	        intent.putExtra("event", e);
 	        intent.putExtra("token", token);
 	        startActivityForResult(intent, RadarCommonController.FIRE_EVENT);
 	}
 }
