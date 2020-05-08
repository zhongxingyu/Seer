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
 
 package com.goread.goreader;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v7.widget.ShareActionProvider;
 import android.text.Html;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import com.squareup.picasso.Picasso;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.util.Date;
 
 public class StoryActivity extends ActionBarActivity {
     private String mFeedTitle = "";
     private String mStoryTitle = "";
     private String mStoryLink = "";
     private ShareActionProvider mShareActionProvider;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Needs to be called before setting the content view
         setContentView(R.layout.activity_story);
         WebView wv = (WebView) findViewById(R.id.webview);
         Intent i = getIntent();
         try {
             JSONObject story = new JSONObject(i.getStringExtra("story"));
             JSONObject feed = GoRead.get().feeds.get(story.getString("feed"));
             mStoryLink = story.getString("Link");
             mStoryTitle = story.getString("Title");
             mFeedTitle = feed.getString("Title");
 
             setTitle(mFeedTitle);
             StringBuilder sb = new StringBuilder();
             sb.append("<html><head><style>");
             InputStream is = getResources().openRawResource(R.raw.bootstrap);
             BufferedReader r = new BufferedReader(new InputStreamReader(is));
             String l;
             while ((l = r.readLine()) != null) {
                 sb.append(l);
             }
             sb.append("body { margin: 5px; font-size: 18px; }");
             sb.append("h3 { margin: 0; font-size: 22px; }");
             sb.append("hr { margin-top: 5px; margin-bottom: 5px; }");
             sb.append("</style></head><body>");
            sb.append(String.format("<h3><a href=\"%s\">%s</a></h3>", mStoryLink,
                     Html.escapeHtml(mStoryTitle)));
             sb.append("<hr>");
            sb.append(String.format("<p><a href=\"%s\">%s</a>", story.getString("feed"),
                     mFeedTitle));
             try {
                 Date d = new Date(Long.parseLong(story.getString("Date")) * 1000);
                 DateFormat df = android.text.format.DateFormat.getDateFormat(this);
                 DateFormat tf = android.text.format.DateFormat.getTimeFormat(this);
                sb.append(String.format(" on %s %s", df.format(d), tf.format(d)));
             } catch (Exception e) {
             }
             sb.append("</p><div>");
             sb.append(i.getStringExtra("contents"));
             sb.append("</div></body></html>");
             wv.loadDataWithBaseURL(null, sb.toString(), null, "UTF-8", null);
 
             final Context c = this;
             AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
                 @Override
                 protected Void doInBackground(String... params) {
                     try {
                         String iconURL = GoRead.getIcon(params[0]);
                         if (iconURL != null) {
                             Bitmap bi = Picasso.with(c).load(iconURL).resize(128, 128).get();
                             BitmapDrawable bd = new BitmapDrawable(getResources(), bi);
                             getActionBar().setIcon(bd);
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     return null;
                 }
             };
             task.execute(story.getString("feed"));
         } catch (JSONException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.story, menu);
         // Get the menu item.
         MenuItem shareItem = menu.findItem(R.id.action_share_story);
         // Get the provider and hold onto it to set/change the share intent.
         mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
         setShare();
         return super.onCreateOptionsMenu(menu);
     }
 
     /**
      * Set the share action
      *
      * @return The sharing intent.
      */
     private void setShare() {
         Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
         shareIntent.setType("text/plain");
         shareIntent.putExtra(Intent.EXTRA_SUBJECT, mStoryTitle);
         shareIntent.putExtra(Intent.EXTRA_TEXT, mStoryLink);
         mShareActionProvider.setShareIntent(shareIntent);
     }
 
 }
