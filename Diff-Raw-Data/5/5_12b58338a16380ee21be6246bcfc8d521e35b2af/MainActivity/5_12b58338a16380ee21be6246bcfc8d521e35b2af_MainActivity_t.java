 /*
  * Copyright (c) 2013 Matt Jibson <matt.jibson@gmail.com>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package com.goread.reader;
 
 import android.accounts.AccountManager;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.android.volley.Request;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.BasicNetwork;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.android.volley.toolbox.NoCache;
 import com.android.volley.toolbox.StringRequest;
 import com.google.android.gms.auth.GoogleAuthException;
 import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
 import com.google.android.gms.common.AccountPicker;
 import com.jakewharton.disklrucache.DiskLruCache;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class MainActivity extends ListActivity {
 
     static final String TAG = "goread";
     static final int PICK_ACCOUNT_REQUEST = 1;
     static final String APP_ENGINE_SCOPE = "ah";
     static final String GOREAD_DOMAIN = "www.goread.io";
     static final String GOREAD_URL = "http://" + GOREAD_DOMAIN;
     static final String P_ACCOUNT = "ACCOUNT_NAME";
 
     private FeedAdapter aa;
     private Intent i;
     private JSONArray oa;
     private JSONObject to = null;
     private int pos = -1;
     private SharedPreferences p;
 
     static public JSONObject lj = null;
     static public JSONObject stories = null;
     static public HashMap<String, JSONObject> feeds;
     static public DiskLruCache storyCache = null;
     static public RequestQueue rq = null;
     private static boolean loginDone = false;
     private File feedCache = null;
 
     static public UnreadCounts unread = null;
 
     public class UnreadCounts {
         public int All = 0;
         public HashMap<String, Integer> Folders = new HashMap<String, Integer>();
         public HashMap<String, Integer> Feeds = new HashMap<String, Integer>();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         try {
             Log.e(TAG, "onCreate");
             setContentView(R.layout.activity_main);
             p = getPreferences(MODE_PRIVATE);
             aa = new FeedAdapter(this, R.layout.item_row);
             setListAdapter(aa);
             if (feedCache == null) {
                 feedCache = new File(getFilesDir(), "feedCache");
             }
             if (lj == null) {
                 try {
                     BufferedReader br = new BufferedReader(new FileReader(feedCache));
                     try {
                         StringBuilder sb = new StringBuilder();
                         String line = br.readLine();
 
                         while (line != null) {
                             sb.append(line);
                             sb.append('\n');
                             line = br.readLine();
                         }
                         String s = sb.toString();
                         updateFeedProperties(new JSONObject(s));
                         displayFeeds();
                         Log.e(TAG, "read from feed cache");
                     } finally {
                         br.close();
                     }
                 } catch (Exception e) {
                     Log.e(TAG, "br", e);
                 }
             } else {
                 displayFeeds();
             }
             if (rq == null) {
                 rq = new RequestQueue(new NoCache(), new BasicNetwork(new OkHttpStack()));
                 rq.start();
             }
             if (storyCache == null) {
                 File f = getFilesDir();
                 f = new File(f, "storyCache");
                 storyCache = DiskLruCache.open(f, 1, 1, (1 << 20) * 5);
             }
             start();
         } catch (Exception e) {
             Log.e(TAG, "oc", e);
         }
     }
 
     protected void start() {
         if (!loginDone) {
             if (p.contains(P_ACCOUNT)) {
                 getAuthCookie();
             } else {
                 pickAccount();
             }
         } else if (lj == null) {
             fetchListFeeds();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         try {
             // Handle item selection
             switch (item.getItemId()) {
                 case R.id.action_logout:
                     logout();
                     return true;
                 case R.id.action_refresh:
                     refresh();
                     return true;
             }
         } catch (Exception e) {
             Log.e(TAG, "oois", e);
         }
         return super.onOptionsItemSelected(item);
     }
 
     protected void refresh() throws IOException, GoogleAuthException {
         // todo: make sure only one of this runs at once
         start();
     }
 
     protected void logout() {
         SharedPreferences.Editor e = p.edit();
         e.remove(P_ACCOUNT);
         e.commit();
         pickAccount();
     }
 
     protected void pickAccount() {
         Log.e(TAG, "pickAccount");
         Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
         startActivityForResult(intent, PICK_ACCOUNT_REQUEST);
     }
 
     protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
         try {
             if (requestCode == PICK_ACCOUNT_REQUEST) {
                 if (resultCode == RESULT_OK) {
                     String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                     SharedPreferences.Editor e = p.edit();
                     e.putString(P_ACCOUNT, accountName);
                     e.commit();
                     getAuthCookie();
                 } else {
                     Log.e(TAG, String.format("%d, %d, %s", requestCode, resultCode, data));
                     Log.e(TAG, "pick not ok, try again");
                     pickAccount();
                 }
             } else {
                 Log.e(TAG, String.format("activity result: %d, %d, %s", requestCode, resultCode, data));
             }
         } catch (Exception e) {
             Log.e(TAG, "oar", e);
         }
     }
 
     protected void getAuthCookie() {
         Log.e(TAG, "getAuthCookie");
         final Context c = this;
         AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... voids) {
                 try {
                     String accountName = p.getString(P_ACCOUNT, "");
                     String authToken = GoogleAuthUtil.getToken(c, accountName, APP_ENGINE_SCOPE);
                     return authToken;
                } catch (UserRecoverableAuthException e) {
                    Intent intent = e.getIntent();
                    startActivityForResult(intent, PICK_ACCOUNT_REQUEST);
                 } catch (Exception e) {
                     Log.e(TAG, "gac", e);
                 }
                 return null;
             }
 
             @Override
             protected void onPostExecute(String authToken) {
                 if (authToken == null) {
                     Toast toast = Toast.makeText(c, "Error: could not authorize account", Toast.LENGTH_LONG);
                     toast.show();
                     pickAccount();
                     return;
                 }
                 try {
                     URL url = new URL(GOREAD_URL + "/_ah/login" + "?continue=" + URLEncoder.encode(GOREAD_URL, "UTF-8") + "&auth=" + URLEncoder.encode(authToken, "UTF-8"));
                     rq.add(new StringRequest(Request.Method.GET, url.toString(), new Response.Listener<String>() {
                         @Override
                         public void onResponse(String s) {
                             Log.e(TAG, "resp");
                             loginDone = true;
                             fetchListFeeds();
                         }
                     }, new Response.ErrorListener() {
                         @Override
                         public void onErrorResponse(VolleyError volleyError) {
                             Log.e(TAG, volleyError.toString());
                             // todo: something here
                         }
                     }
                     ));
                 } catch (Exception e) {
                     Toast toast = Toast.makeText(c, "Error: could not log in", Toast.LENGTH_LONG);
                     toast.show();
                     pickAccount();
                     Log.e(TAG, "gac ope", e);
                 }
             }
         };
         task.execute();
     }
 
     protected void addFeed(JSONObject o) {
         try {
             feeds.put(o.getString("XmlUrl"), o);
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 
     protected void fetchListFeeds() {
         Log.e(TAG, "fetchListFeeds");
         rq.add(new JsonObjectRequest(Request.Method.GET, GOREAD_URL + "/user/list-feeds", null, new Response.Listener<JSONObject>() {
             @Override
             public void onResponse(JSONObject jsonObject) {
                 try {
                     lj = jsonObject;
                     FileWriter fw = new FileWriter(feedCache);
                     fw.write(jsonObject.toString());
                     fw.close();
                     Log.e(TAG, "write feed cache");
 
                     updateFeedProperties(lj);
                     downloadStories();
                     displayFeeds();
 
                 } catch (Exception e) {
                     Log.e(TAG, "flf", e);
                 }
             }
         }, null));
     }
 
     public static String hashStory(JSONObject j) throws JSONException {
         return hashStory(j.getString("Feed"), j.getString("Story"));
     }
 
     public static String hashStory(String feed, String story) {
         MessageDigest cript = null;
         try {
             cript = MessageDigest.getInstance("SHA-1");
             cript.reset();
             cript.update(feed.getBytes("utf8"));
             cript.update("|".getBytes());
             cript.update(story.getBytes());
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         String sha = new BigInteger(1, cript.digest()).toString(16);
         return sha;
     }
 
     protected void downloadStories() {
         Log.e(TAG, "downloadStories");
         try {
             final JSONArray ja = new JSONArray();
             Iterator<String> keys = stories.keys();
             while (keys.hasNext()) {
                 String key = keys.next();
                 JSONArray sos = stories.getJSONArray(key);
                 for (int i = 0; i < sos.length(); i++) {
                     JSONObject so = sos.getJSONObject(i);
                     JSONObject jo = new JSONObject()
                             .put("Feed", key)
                             .put("Story", so.getString("Id"));
                     String hash = hashStory(jo);
                     if (storyCache.get(hash) == null) {
                         ja.put(jo);
                     }
                 }
             }
 
             rq.add(new com.goread.reader.JsonArrayRequest(Request.Method.POST, GOREAD_URL + "/user/get-contents", ja, new Response.Listener<JSONArray>() {
                 @Override
                 public void onResponse(JSONArray jsonArray) {
                     cacheStories(ja, jsonArray);
                 }
             }, null));
         } catch (Exception e) {
             Log.e(TAG, "ds", e);
         }
     }
 
     protected void cacheStories(JSONArray ids, JSONArray contents) {
         for (int i = 0; i < ids.length(); i++) {
             try {
                 JSONObject is = ids.getJSONObject(i);
                 String content = contents.getString(i);
                 String key = hashStory(is);
                 DiskLruCache.Editor edit = storyCache.edit(key);
                 edit.set(0, content);
                 edit.commit();
             } catch (JSONException e) {
                 Log.e(TAG, "cachestories json", e);
             } catch (IOException e) {
                 Log.e(TAG, "cachestories io", e);
             }
         }
         try {
             storyCache.flush();
         } catch (IOException e) {
             Log.e(TAG, "cache flush", e);
         }
     }
 
     protected void updateFeedProperties(JSONObject o) {
         try {
             lj = o;
             stories = lj.getJSONObject("Stories");
             unread = new UnreadCounts();
             JSONArray opml = lj.getJSONArray("Opml");
             updateFeedProperties(null, opml);
         } catch (JSONException e) {
             Log.e(TAG, "ufp", e);
         }
     }
 
     protected void updateFeedProperties(String folder, JSONArray opml) {
         try {
             for (int i = 0; i < opml.length(); i++) {
                 JSONObject outline = opml.getJSONObject(i);
                 if (outline.has("Outline")) {
                     updateFeedProperties(outline.getString("Title"), outline.getJSONArray("Outline"));
                 } else {
                     String f = outline.getString("XmlUrl");
                     if (!stories.has(f)) {
                         continue;
                     }
                     JSONArray us = stories.getJSONArray(f);
                     Integer c = us.length();
                     if (c == 0) {
                         continue;
                     }
                     unread.All += c;
                     if (!unread.Feeds.containsKey(f)) {
                         unread.Feeds.put(f, 0);
                     }
                     unread.Feeds.put(f, unread.Feeds.get(f) + c);
                     if (folder != null) {
                         if (!unread.Folders.containsKey(folder)) {
                             unread.Folders.put(folder, 0);
                         }
                         unread.Folders.put(folder, unread.Folders.get(folder) + c);
                     }
                 }
             }
         } catch (JSONException e) {
             Log.e(TAG, "ufp2", e);
         }
     }
 
     protected void displayFeeds() {
         Log.e(TAG, "displayFeeds");
         try {
             i = getIntent();
             aa.clear();
 
             if (i.hasExtra(K_OUTLINE)) {
                 pos = i.getIntExtra(K_OUTLINE, -1);
                 try {
                     JSONArray ta = lj.getJSONArray("Opml");
                     to = ta.getJSONObject(pos);
                     String t = to.getString("Title");
                     setTitle(t);
                     if (unread.Folders.containsKey(t)) {
                         Integer c = unread.Folders.get(t);
                         t = String.format("%s (%d)", t, c);
                     }
                     addItem(t, ICON_FOLDER);
                     oa = to.getJSONArray("Outline");
                     parseJSON();
                 } catch (JSONException e) {
                     Log.e(TAG, "pos", e);
                 }
             } else {
                 String t = "all items";
                 if (unread.All > 0) {
                     t = String.format("%s (%d)", t, unread.All);
                 }
                 addItem(t, ICON_FOLDER);
                 feeds = new HashMap<String, JSONObject>();
                 oa = lj.getJSONArray("Opml");
                 for (int i = 0; i < oa.length(); i++) {
                     JSONObject o = null;
                     o = oa.getJSONObject(i);
                     if (o.has("Outline")) {
                         JSONArray outa = o.getJSONArray("Outline");
                         for (int j = 0; j < outa.length(); j++) {
                             addFeed(outa.getJSONObject(j));
                         }
                     } else {
                         addFeed(o);
                     }
                 }
                 parseJSON();
             }
         } catch (JSONException e) {
             Log.e(TAG, "display feeds json", e);
         }
     }
 
     public static final String ICON_FOLDER = "__folder__";
 
     protected void addItem(String i, String icon) {
         aa.add(new Outline(i, icon));
     }
 
     protected void parseJSON() {
         try {
             for (int i = 0; i < oa.length(); i++) {
                 JSONObject o = oa.getJSONObject(i);
                 String t = o.getString("Title");
                 String icon = ICON_FOLDER;
                 if (o.has("Outline") && unread.Folders.containsKey(t)) {
                     Integer c = unread.Folders.get(t);
                     t = String.format("%s (%d)", t, c);
                 } else if (o.has("XmlUrl")) {
                     String u = o.getString("XmlUrl");
                     icon = getIcon(u);
                     if (unread.Feeds.containsKey(u)) {
                         Integer c = unread.Feeds.get(u);
                         t = String.format("%s (%d)", t, c);
                     }
                 }
                 addItem(t, icon);
             }
 
         } catch (JSONException e) {
             Log.e(TAG, "parse json", e);
         }
     }
 
     public static final String K_OUTLINE = "OUTLINE";
     public static final String K_FOLDER = "FOLDER";
     public static final String K_FEED = "FEED";
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         try {
             if (position == 0) {
                 Intent i = new Intent(this, StoryListActivity.class);
                 i.putExtra(K_FOLDER, pos);
                 startActivity(i);
             } else {
                 JSONObject o = oa.getJSONObject(position - 1);
                 if (o.has("Outline")) {
                     Intent i = new Intent(this, MainActivity.class);
                     i.putExtra(K_OUTLINE, position - 1);
                     startActivity(i);
                 } else {
                     Intent i = new Intent(this, StoryListActivity.class);
                     i.putExtra(K_FEED, o.getString("XmlUrl"));
                     startActivity(i);
                 }
             }
         } catch (JSONException e) {
             Log.e(TAG, "list item click", e);
         }
     }
 
     public static String getIcon(String f) {
         final String suffix = "=s16";
         try {
             JSONObject i = lj.getJSONObject("Icons");
             if (i.has(f)) {
                 String u = i.getString(f);
                 if (u.endsWith(suffix)) {
                     u = u.substring(0, u.length() - suffix.length());
                 }
                 return u;
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return null;
     }
 }
