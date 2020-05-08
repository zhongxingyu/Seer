 package com.jgaunt.Soundbox;
 
 import com.dropbox.client2.android.AndroidAuthSession;
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.DropboxAPI.Entry;
 import com.dropbox.client2.exception.DropboxException;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 // TODO: implement some status reporting/progress
 public class SoundBoxAsyncFetcher extends AsyncTask<String, Void, String[]> {
     /** Define an inner interface for handling completion */
     public static interface CompletionHandler { void onTaskComplete(String [] result); }
 
     // track state so we know when to notify the handler
     private enum State { EXECUTING, FINISHED, NOTIFIED; }
     private State state = State.EXECUTING;
 
     // member var for hanging on to the AuthSession
     private DropboxAPI<AndroidAuthSession> mDBApi;
 
     // Notify this object when the task is complete
     private CompletionHandler mHandler = null;
 
     // Hang on to our result in case the Handler detaches for a bit
     private String [] result = null;
 
     /* ********************************************************************* *
      *                          AsyncTask calls                              *
      * ********************************************************************* */
     @Override
     protected String [] doInBackground(String... params) {
         // FUTURE: handle more than one Fetch param
         return getDropboxListing(params[0]);
     }
 
     @Override
     protected void onPostExecute(String [] aEntryArray) {
         result = aEntryArray;
         state = State.FINISHED;
         notifyHandler();
     }
 
     /** Setter for the CompletionHandler */
     public void registerCompletionHandler (SoundBoxAsyncFetcher.CompletionHandler aCompletionHandler) {
         // Don't check for null so it can be cleared
         mHandler = aCompletionHandler;
         notifyHandler();
     }
 
     // TODO make this a service that can do the dropbox connection itself
     public void setDropboxAPI( DropboxAPI<AndroidAuthSession> aDBApi ) {
         mDBApi = aDBApi;
         notifyHandler();
     }
 
     // Do this separately so we can call it from multiple places
     private void notifyHandler() {
         if (null == mHandler) {
             return;
         }
         switch (state) {
             case EXECUTING:
                 break;
             case FINISHED:
                 state = State.NOTIFIED;
                 mHandler.onTaskComplete(result);
                 break;
             case NOTIFIED:
                 throw new IllegalStateException("Attempt to register after notification");
         }
     }
 
 
     /* ********************************************************************* *
      *                          Dropbox API calls                            *
      * ********************************************************************* */
 
     private String[] getDropboxListing(String path) {
         // TODO: handle the case where path points to a file, not a folder
         if (mDBApi == null) {
             return new String[0];
         }
         Entry folderListing = null;
         try {
             folderListing = mDBApi.metadata(path, 0, null, true, null);
         } catch (DropboxException e) {
             // When there is a 404, we land here. 
             Log.e("DbExampleLog", "Something went wrong while getting metadata." + e);
             // TODO: consider just throwing the exception, so the app can
             //       retry if it wants
             return new String[0];
         }
 
         // Check to see if we got cancelled while doing the API call
         if (isCancelled()) {
             return new String[0];
         }
 
         // convert the List<entry> to a String[] containing just the leaf names
         // TODO: change this to iteration and skip the array, go straight to names
         //       or build other objects to hand back.
         Entry [] folderEntries = new Entry [folderListing.contents.size()];
         folderEntries = (Entry [])folderListing.contents.toArray(folderEntries);
 
         // we're then going to pull the paths out of the Entry objects
         String [] folders = new String [folderEntries.length];
         for ( int i = 0; i < folderEntries.length ; i++ ) {
             // TODO: make the special characters a preference, user-settable
             String leafPattern = "([\\w&@$%!~?.\\-\\{\\}]+)$";
 
             // match only word and a few special characters running to the end-of-line
             Pattern pattern = Pattern.compile(leafPattern);
             Matcher matcher = pattern.matcher(folderEntries[i].path);
             while (matcher.find()) {
                 // because it matches the EOL there *should* be a 1-to-1 mapping
                 folders[i] = matcher.group();
             }
         }
         return folders;
     }
 }
