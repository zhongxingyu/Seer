 package com.tj.qotd;
 
import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * Single entry point to get some quotes
  *
  * We don't extend ContentProvider because we don't need to provide quotes to other apps
  */
 public class QuoteProvider {
 
     private Context mContext;
     private QuoteDatabase mQuoteDatabase;
 
     private static final int COLUMN_QUOTE_ID_INDEX = 0;
     private static final int COLUMN_QUOTE_INDEX = 1;
 
     public QuoteProvider(Context context) {
         mContext = context;
         mQuoteDatabase = new QuoteDatabase(mContext);
     }
 
     /**
      * Fetch a previously generated quote
      */
     public String getCurrentQuote() {
        SharedPreferences sp = mContext.getSharedPreferences("qotd", Activity.MODE_PRIVATE);
         int currentQuoteId = sp.getInt("qotd.current_quote_id", -1);
 
         if (currentQuoteId < 0) {
             return resetQuote();
         }
         else
         {
             Cursor c = mQuoteDatabase.getQuote(currentQuoteId);
             if (c != null) {
               return getQuoteFromCursor(c);
             }
             else {
               return getQuoteNotFound();
             }
         }
     }
 
     /**
      * Fetch a randow quote, and save its id for later access
      */
     public String resetQuote() {
 
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
         String lang = sp.getString("quote_lang", "en");
 
         Cursor c = mQuoteDatabase.getRandomQuote(lang);
 
         if (c != null && c.moveToFirst()) {
             int currentQuoteId = c.getInt(COLUMN_QUOTE_ID_INDEX);
             Log.e("QOTD", "reset id : " + currentQuoteId);
             Editor editor = sp.edit();
             editor.putInt("qotd.current_quote_id", currentQuoteId);
             editor.commit();
             return getQuoteFromCursor(c);
         }
         else {
             return getQuoteNotFound();
         }
     }
 
     /**
      * Get a quote not found error message
      */
     private String getQuoteNotFound() {
         return mContext.getString(R.string.no_quote_found);
     }
 
 
     /**
      * Take a cursor, and return a formatted quote
      */
     private String getQuoteFromCursor(Cursor c) {
         String res = "";
         if (c.moveToFirst()) {
             res = c.getString(COLUMN_QUOTE_INDEX);
         }
         c.close();
         return res;
     }
 }
