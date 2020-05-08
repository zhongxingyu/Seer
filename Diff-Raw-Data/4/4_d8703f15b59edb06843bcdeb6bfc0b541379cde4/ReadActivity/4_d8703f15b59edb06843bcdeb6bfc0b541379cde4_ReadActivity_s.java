 /**
  * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details at http://www.gnu.org/copyleft/gpl.html
  */
 package com.daily;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.webkit.WebSettings.TextSize;
 import android.widget.Toast;
 
 import com.daily.resource.DailyReadingFetcher;
 import com.daily.resource.ReadingHtmlFormatter;
 import com.daily.resource.SPDKFetcher;
 import com.daily.resource.db.FeedDB;
 import com.daily.resource.rss.Article;
 import com.daily.resource.rss.Direction;
 import com.daily.settings.Preferences;
 import com.daily.settings.Preferences.ArchiveSize;
 import com.daily.utils.DateUtils;
 import com.daily.view.ActivitySwipeDetector;
 import com.daily.view.ReadingWebView;
 import com.daily.view.SwipeDetectorHandler;
 
 /**
  * The Class DailyReadingActivity. Used for display texts (main app activity).
  */
 public class ReadActivity extends AbstractReadingActivity implements SwipeDetectorHandler {
     
     /** The reading db. Used as a cache and datasource */
     FeedDB readingDB;
     
     /** The current displayed. */
     Article currentArticle;
     
     /** The rss fetcher for spkd.cz. No other sources supported yet. */
     private SPDKFetcher rssFetcher; 
     
     private SharedPreferences prefs;
     
     private ReadingWebView readingArea;
     
     private static final int NOT_FOUND_EXIT_TRY_AGAIN_DIALOG = 1;
     
     private static final int SHOW_PREFERENCES = 1;
     private static final int SHOW_ARTICLES_LIST = 2;
     
     private static final String LAST_DISPLAYED_ARTICLE = "LAST_DISPLAYED_ARTICLE";
 
     /** {@inheritDoc} */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.read);
         
         init();
         showReading();
     }
     
     private void init() {
         prefs = PreferenceManager.getDefaultSharedPreferences(this);
         this.readingArea = (ReadingWebView)findViewById(R.id.readingArea);
         
         ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this, this.readingArea);
         this.readingArea.setOnTouchListener(activitySwipeDetector);        
        
         readingArea.setBackgroundColor(0);
         readingArea.setBackgroundResource(R.drawable.paper_texture);
         String selectedFont = this.prefs.getString(Preferences.PREF_FONT_SIZE, "NORMAL");
         readingArea.getSettings().setTextSize(TextSize.valueOf(selectedFont));
         
         String archiveSize = this.prefs.getString(Preferences.PREF_ARCHIVE_SIZE, "MONTH");
         this.readingDB = new FeedDB(this, Preferences.ArchiveSize.fromString(archiveSize));
         ((GlobalState)getApplication()).setFeedDb(this.readingDB);
         this.rssFetcher = new SPDKFetcher();
         
         setCurrentFeed(this.readingDB.getTodayFeed());
     }
     
     /**
      * Show reading.
      */
     private void showReading() {
         if (getCurrentFeed() == null) { // No data in db
             if (!isOnline()) {
                 showDialog(NOT_FOUND_EXIT_TRY_AGAIN_DIALOG);
             } else {
                 if (this.prefs.getBoolean(Preferences.PREF_AUTO_UPDATE, true)) {
                     refreshFeeds();
                 } else {
                     showText();
                 }
             }
             
         } else {
             showText();
         }
     }
     
     /**
      * Refresh feeds.
      */
     private void refreshFeeds() {
         Article todaysFeed = this.readingDB.getTodayFeed();
         
         if (todaysFeed != null) {
             Toast.makeText(this, getString(R.string.no_need_to_refresh), 3).show();
         } else {
             new LoadFeedsTask().execute(this.rssFetcher); // We are online, can load data in background
         }
     }
     
     /**
      * Background task used for loading feeds to DB.
      */
     private class LoadFeedsTask extends AsyncTask<DailyReadingFetcher, Integer, Long> {
         
         /** The p dialog. */
         ProgressDialog pDialog;
         
         /** {@inheritDoc} */
         protected void onPreExecute() {
             pDialog = ProgressDialog.show(ReadActivity.this, getString(R.string.please_wait), getString(R.string.receiving_data), true);
             pDialog.show();
         }
         
         /** {@inheritDoc} */
         protected void onPostExecute(Long result) {
             pDialog.dismiss();
             showText();
         }
         
         /** {@inheritDoc} */
         @Override
         protected Long doInBackground(DailyReadingFetcher... fetcher) {
             List<Article> feeds = fetcher[0].fetch();
             int insertedRecords = readingDB.insertArticles(feeds);
             setCurrentFeed(readingDB.getTodayFeed());
             Log.i("DB", "Successfully stored " + insertedRecords + " articles.");
             return 0L;
         }
     }
     
     /**
      * Show text in the reading area.
      * 
      * @param feeds the feed(s) to display
      */
     private void showText() {
         if (getCurrentFeed() == null) {
             Toast.makeText(this, getString(R.string.could_not_load_trying_previous), 3).show();
             List<Article> allFeeds = this.readingDB.getFeeds();
             if (allFeeds != null && allFeeds.size() > 0) {
                 //dismissDialog(NOT_FOUND_EXIT_TRY_AGAIN_DIALOG);
                 Article current = allFeeds.get(0);
                 setCurrentFeed(current);
                 displayWord(current);
             } else {
                 showDialog(NOT_FOUND_EXIT_TRY_AGAIN_DIALOG);
             }
             
         } else {
             displayWord(getCurrentFeed());
         }
     }
     
     
     /**
      * Display word.
      * 
      * @param article the article
      */
     private void displayWord(Article article) {
         if (article == null) {
             return; // Possibly display some default text
         }
         readingArea.loadDataWithBaseURL("", ReadingHtmlFormatter.format(article), "text/html", "UTF-8", null);
         this.readingDB.savePreference(LAST_DISPLAYED_ARTICLE, article.getId());
     }
     
     
     /**
      * Gets the todays feed.
      * 
      * @return the todays feed
      */
     private Article getCurrentFeed() {
         return this.currentArticle;
     }
     
     /**
      * Sets the current feed.
      * 
      * @param currentArticle the new current feed
      */
     public void setCurrentFeed(Article currentArticle) {
         this.currentArticle = currentArticle;
     }
     
     
     /** {@inheritDoc} */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.reading_menu, menu);
         
         return true;
     }
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         if (getCurrentFeed() == null || DateUtils.isToday(getCurrentFeed().getDate())) {
             menu.findItem(R.id.menu_show_today).setEnabled(false);
         } else {
             menu.findItem(R.id.menu_show_today).setEnabled(true);
         }
         return super.onPrepareOptionsMenu(menu);
         
     }
     
     /** {@inheritDoc} */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         
         switch (item.getItemId()) {
             case (R.id.menu_preferences): {
                 Intent i = new Intent(this, Preferences.class);
                 startActivityForResult(i, SHOW_PREFERENCES);
                 return true;
             }
             case (R.id.menu_refresh): {
                 refreshFeeds();
                 return true;
             }
             case (R.id.menu_exit): {
                 moveTaskToBack(true);
                 return true;
             }
             case (R.id.menu_archive): {
                 Intent i = new Intent(this, ArticlesListActivity.class);
                 startActivityForResult(i, SHOW_ARTICLES_LIST);
                 return true;
             }
             case (R.id.menu_show_today): {
                 Article selected = this.readingDB.getTodayFeed();
                 setCurrentFeed(selected);
                 showText();
                 return true;
             }
 //            case (R.id.menu_previous): {
 //                movePage(Direction.LEFT);
 //                return true;
 //            }
 //            case (R.id.menu_next): {
 //                movePage(Direction.RIGHT);
 //                return true;
 //            }
         }
         return false;
     }
     
     /** {@inheritDoc} */
     @Override
     public Dialog onCreateDialog(int id) {
         switch (id) {
             case (NOT_FOUND_EXIT_TRY_AGAIN_DIALOG):
                 return new AlertDialog.Builder(this).setTitle(R.string.could_not_load_data_from_internet).setMessage(R.string.try_again_or_exit)
                         .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                             
                             public void onClick(DialogInterface dialog, int which) {
                                 refreshFeeds();
                             }
                         }).setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                             
                             public void onClick(DialogInterface dialog, int which) {
                                 moveTaskToBack(true);
                             }
                         }).create();
         }
         return null;
     }
     
     /** {@inheritDoc} */
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         
         if (requestCode == SHOW_PREFERENCES) {
             if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                 updateFromPreferences();
             }
         }
         
         if (requestCode == SHOW_ARTICLES_LIST) {
             if (resultCode == ArticlesListActivity.SUCCESS_RETURN_CODE) {
                 String selectedArticleId = data.getExtras().getString(ArticlesListActivity.SELECTED_ARTICLE_ID);
                 Article selectedArticle = this.readingDB.getFeed(selectedArticleId);
                 setCurrentFeed(selectedArticle);
                 showText();
             }
         }
     }
     
     /**
      * Update from preferences.
      */
     private void updateFromPreferences() {
         String selectedFont = this.prefs.getString(Preferences.PREF_FONT_SIZE, "NORMAL");
         readingArea.getSettings().setTextSize(TextSize.valueOf(selectedFont));
         
         String archiveSize = this.prefs.getString(Preferences.PREF_ARCHIVE_SIZE, "MONTH");
         this.readingDB.deleteOldRecords(ArchiveSize.fromString(archiveSize));
     }
 
     /**
      * Moves page in specified direction (either left or right) if possible. If not displays info message.
      * 
      * @param dir the instance of {@link Direction}
      */
     public void movePage(Direction dir) {
         int hours = (dir == Direction.LEFT)?-24:24;
         Article selected = this.readingDB.getFeed(DateUtils.addToDate(getCurrentFeed().getDate(), Calendar.HOUR, hours));
         if (selected != null) {
             setCurrentFeed(selected);
             showText();
             Toast.makeText(this, getString(R.string.word_from_day) + " " + selected.getFormattedDate(new SimpleDateFormat("d.MM.yyyy")), 1).show();
         } else {
             Toast.makeText(this, getString(R.string.could_not_load_next_data), 3).show();
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void onRightToLeftSwipe(MotionEvent event) {
         movePage(Direction.LEFT);
         this.readingArea.onTouchEvent(event);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onLeftToRightSwipe(MotionEvent event) {
         movePage(Direction.RIGHT);
         this.readingArea.onTouchEvent(event);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onTopToBottomSwipe(MotionEvent event) {
         this.readingArea.onTouchEvent(event);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onBottomToTopSwipe(MotionEvent event) {
         this.readingArea.onTouchEvent(event);
     }
     
     
 }
