 package com.parent.management.monitor;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.os.Handler;
 import android.provider.Browser;
 import android.util.Log;
 
 import com.parent.management.ManagementApplication;
 import com.parent.management.db.ManagementProvider;
 
 public class BrowserHistoryMonitor extends Monitor {
     private static final String TAG = ManagementApplication.getApplicationTag() + "." +
             BrowserHistoryMonitor.class.getSimpleName();
 	
 	private BrowserHistoryObserver contentObserver = null;
 
 	public BrowserHistoryMonitor(Context context) {
 		super(context);
 	    this.contentUri = Browser.BOOKMARKS_URI;
 	    this.contentObserver = new BrowserHistoryObserver(new Handler());
 	}
 
 	@Override
 	public void startMonitoring() {
 		this.contentResolver.registerContentObserver(this.contentUri, true, this.contentObserver);
 	    this.monitorStatus = true;
 	    Log.d(TAG, "----> startMonitoring");
 	}
 
 	@Override
 	public void stopMonitoring() {
         this.contentResolver.unregisterContentObserver(this.contentObserver);
         this.monitorStatus = false;
         Log.d(TAG, "----> stopMonitoring");
 	}
 	
 	private class BrowserHistoryObserver extends ContentObserver {
 
 		public BrowserHistoryObserver(Handler handler) {
 			super(handler);
 		}
 		
 		@Override
         public void onChange(boolean selfChange) {
 		    String[] browserProj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL };
 	        String browserSel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
 	        Cursor browserCur = ManagementApplication.getContext().getContentResolver().query(
 	                Browser.BOOKMARKS_URI, null, browserSel, null, null);
 	        
 	        String title = "";
 	        String url = "";
 	        String id = "";
 	        String count = "";
 	        String last_visit = "";
 
            if (browserCur == null) {
                Log.v(TAG, "open browserHistory native failed");
                return;
            }
 	        if (browserCur.moveToFirst() && browserCur.getCount() > 0) {
 	            while (browserCur.isAfterLast() == false) {
 
 	                title = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
 	                url = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns.URL));
 	                id = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns._ID));
 	                count = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns.VISITS));
 	                last_visit = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns.DATE));
 	                // Do something with title and url
 	                Log.v(TAG, "id=" + id + ";title=" + title + ";url=" + url + ";count=" + count);
 
 	                String[] browserHistoryProj = new String[] { ManagementProvider.BrowserHistory.ID,
 	                        ManagementProvider.BrowserHistory.VISIT_COUNT};
 	                String browserHistorySel = ManagementProvider.BrowserHistory.ID + " = " + id;
 	                Cursor browserHistoryCur = ManagementApplication.getContext().getContentResolver().query(
 	                        ManagementProvider.BrowserHistory.CONTENT_URI,
 	                        browserHistoryProj, browserHistorySel, null, null);
 	                
 	                if (browserHistoryCur == null) {
                         Log.v(TAG, "open browserHistory failed");
                         browserCur.close();
 	                    return;
 	                }
 	                if (browserHistoryCur.moveToFirst() && browserHistoryCur.getCount() > 0) {
 	                    String logged_visit_count = browserHistoryCur.getString(
 	                            browserHistoryCur.getColumnIndex(
 	                                    ManagementProvider.BrowserHistory.VISIT_COUNT));
 	                    if (!logged_visit_count.equals(count)) {
 	                        final ContentValues values = new ContentValues();
 	                        values.put(ManagementProvider.BrowserHistory.VISIT_COUNT, count);
                             values.put(ManagementProvider.BrowserHistory.LAST_VISIT, last_visit);
                             values.put(ManagementProvider.BrowserHistory.IS_SENT,
                                     ManagementProvider.IS_SENT_NO);
 	                        
 	                        ManagementApplication.getContext().getContentResolver().update(
 	                                ManagementProvider.BrowserHistory.CONTENT_URI,
 	                                values,
 	                                ManagementProvider.BrowserHistory.ID + "=\"" + id +"\"",
 	                                null);
 	                        Log.v(TAG, "update one");
 	                    }
 	                } else {
 	                    final ContentValues values = new ContentValues();
 	                    values.put(ManagementProvider.BrowserHistory.ID, id);
                         values.put(ManagementProvider.BrowserHistory.URL, url);
                         values.put(ManagementProvider.BrowserHistory.TITLE, title);
                         values.put(ManagementProvider.BrowserHistory.VISIT_COUNT, count);
                         values.put(ManagementProvider.BrowserHistory.LAST_VISIT, last_visit);
 	                    
 	                    ManagementApplication.getContext().getContentResolver().insert(
 	                            ManagementProvider.BrowserHistory.CONTENT_URI, values);
                         Log.v(TAG, "insert one");
 	                }
 	                
 	                browserHistoryCur.close();
 	                browserCur.moveToNext();
 	            }
 	        }
 	        browserCur.close();
 		}
 		
 	}
 
     @Override
     public Cursor extraData() {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
