 /**
  *
  * @author David Findley (ThinksInBits)
  * 
  * The source for this application may be found in its entirety at 
  * https://github.com/ThinksInBits/OU-Mobile-App
  * 
  * This application is published on the Google Play Store under
  * the title: OU Mobile Alpha:
  * https://play.google.com/store/apps/details?id=com.geared.ou
  * 
  * If you want to follow the official development of this application
  * then check out my Trello board for the project at:
  * https://trello.com/board/ou-app/4f1f697a28390abb75008a97
  * 
  * Please email me at: thefindley@gmail.com with questions.
  * 
  */
 
 package com.geared.ou;
 
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.TextView;

 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
 import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FeedFetcher;
 import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FetcherException;
 import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
 import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
 
 /**
  *
  * This is a top level activity that displays News that is read from oudaily.com
  * to the user. Currently this is just a framework, and none of the real
  * functionality has been implemented.
  * 
  */
 public class NewsActivity extends Activity implements View.OnClickListener {
     private LinearLayout buttonClasses;
     private LinearLayout buttonEmail;
     private LinearLayout layoutContent;
     private OUApplication app;
    List<?> items;
     SyndFeed feed;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         
         setContentView(R.layout.news);
 
         // Get Action Buttons
         buttonClasses = (LinearLayout) findViewById(R.id.classesbutton);
         buttonEmail = (LinearLayout) findViewById(R.id.emailbutton);
         buttonClasses.setOnClickListener(this);
         buttonEmail.setOnClickListener(this);
         layoutContent = (LinearLayout) findViewById(R.id.content);
         
         app = (OUApplication) this.getApplication();
         feed = app.getFeed();
         if (feed == null) {
             setStatusTextViewToUpdating();
             new Load().execute();
         }
         else {
             updateDisplay();
         }
     }
     
     private class Load extends AsyncTask<Integer, Integer, Boolean> {
         @Override
         protected Boolean doInBackground(Integer... s) {
             feed = getMostRecentNews("http://www.oudaily.com/rss/headlines/front/");
             app.setFeed(feed);
             if (feed == null)
                 return false;
             return true;
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             super.onProgressUpdate(values);
             // Update percentage
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             super.onPostExecute(result);
             updateDisplay();
         }
     }
     
     protected void updateDisplay() {
         layoutContent.removeViews(1, layoutContent.getChildCount()-1);
         LayoutParams lparam = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         items = feed.getEntries();
        Iterator<?> i = items.iterator();
         addSpacer(layoutContent, Color.BLACK, 1, -1, View.VISIBLE);
         int counter = 0;
         while (i.hasNext()) {
             SyndEntryImpl entry = (SyndEntryImpl) i.next();
             TextView tv = new TextView(this);
             tv.setLayoutParams(lparam);
             tv.setGravity(Gravity.CENTER_VERTICAL);
             tv.setTextColor(Color.argb(255, 75, 25, 25));
             tv.setTextSize(13);
             tv.setId(counter);
             tv.setTypeface(null, Typeface.BOLD);
             tv.setPadding(10, 12, 10, 12);
             tv.setHorizontalFadingEdgeEnabled(true);
             tv.setFadingEdgeLength(35);
             tv.setSingleLine(true);
             tv.setText(entry.getTitle());
             tv.setOnClickListener(this);
             tv.setClickable(true);
             tv.setBackgroundResource(R.drawable.content_list_button_selector);
             layoutContent.addView(tv);
             addSpacer(layoutContent, Color.BLACK, 1, -1, View.VISIBLE);
             TextView tvc = new TextView(this);
             tvc.setLayoutParams(lparam);
             tvc.setGravity(Gravity.CENTER_VERTICAL);
             tvc.setTextColor(Color.argb(255, 75, 25, 25));
             tvc.setTextSize(13);
             tvc.setId(counter+100);
             tvc.setPadding(10, 5, 10, 5);
             tvc.setVisibility(View.GONE);
             tvc.setText(entry.getDescription().getValue());
             layoutContent.addView(tvc);
             addSpacer(layoutContent, Color.BLACK, 1, counter+200, View.GONE);
             counter++;
         }
     }
     
     public void addSpacer(LinearLayout l, int color, int height, int id, int vis) {
         TextView t = new TextView(this);
         t.setWidth(l.getWidth());
         t.setHeight(height);
         t.setId(id);
         t.setVisibility(vis);
         t.setBackgroundColor(color);
         l.addView(t);
     }
     
     protected SyndFeed getMostRecentNews( final String feedUrl )
     {
         try
         {
             return retrieveFeed( feedUrl );
         }
         catch ( Exception e )
         {
             throw new RuntimeException( e );
         }
     }
     
     private SyndFeed retrieveFeed( final String feedUrl ) throws IOException, FeedException, FetcherException
     {
         FeedFetcher feedFetcher = new HttpURLFeedFetcher();
         return feedFetcher.retrieveFeed( new URL( feedUrl ) );
     }
     
     public void onClick(View v) {
         Log.d("OU", "Something was clicked");
         if (v.getId() == R.id.classesbutton)
         {
             Log.d("OU", "Classes button pressed.");
             Intent myIntent = new Intent(this, ClassesActivity.class);
             myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(myIntent);
         }
         else if (v.getId() == R.id.emailbutton)
         {
             Log.d("OU", "Email button pressed.");
             Intent myIntent = new Intent(this, CampusMapActivity.class);
             myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(myIntent);
         }
         else {
             SyndEntryImpl i = (SyndEntryImpl)items.get(v.getId());
             if (i == null) {
                 Log.d("OU", "Could not get the clicked view.");
                 return;
             }
             TextView tv = (TextView)findViewById(v.getId()+100);
             TextView sp = (TextView)findViewById(v.getId()+200);
             if (tv.getVisibility() == View.GONE) {
                 tv.setVisibility(View.VISIBLE);
                 sp.setVisibility(View.VISIBLE);
             }
             else {
                 tv.setVisibility(View.GONE);
                 sp.setVisibility(View.GONE);
             }
         }
     }
     
     protected void setStatusTextViewToUpdating() {
         final AnimationDrawable img = new AnimationDrawable();
         img.addFrame(getResources().getDrawable(R.drawable.loading1), 150);
         img.addFrame(getResources().getDrawable(R.drawable.loading2), 150);
         img.addFrame(getResources().getDrawable(R.drawable.loading3), 150);
         img.addFrame(getResources().getDrawable(R.drawable.loading4), 150);
         img.setBounds(0, 0, 30, 30);
         img.setOneShot(false);
         TextView updateTV = (TextView)findViewById(R.id.updateTextView);
         if (updateTV == null) {
             updateTV = new TextView(this);
             updateTV.setText(" Updating...");
             updateTV.setCompoundDrawables(img, null, null, null);
             updateTV.setGravity(Gravity.TOP);
             updateTV.setWidth(layoutContent.getWidth());
             updateTV.setPadding(15, 3, 3, 3);
             updateTV.setTextColor(Color.BLACK);
             updateTV.setTextSize(13);
             updateTV.setId(R.id.updateTextView);
             layoutContent.addView(updateTV);
         } else {
             updateTV.setCompoundDrawables(img, null, null, null);
             updateTV.setText(" Updating...");
             updateTV.setTextColor(Color.BLACK);
         }
         updateTV.post(new Runnable() {
             public void run() {
                 img.start();
             }
         });
     }
 }
