 package no.hials.muldvarp.utility;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import java.io.File;
 import java.io.InputStreamReader;
 import no.hials.muldvarp.R;
 
 /**
  *
  * @author mikael
  */
 public class DownloadTask extends AsyncTask<String, Void, Void> {
     Intent intent;
     Context ctx;
 
     public DownloadTask(Context ctx, Intent intent) {
         this.intent = intent;
         this.ctx = ctx;
     }
 
     @Override
     protected Void doInBackground(String... params) {
         File f = new File(ctx.getCacheDir(), params[1]);
         if (System.currentTimeMillis() - f.lastModified() > ctx.getResources().getInteger(R.integer.cacheTime)) {  // funka ditta skikkelig?
             try {
                 DownloadUtilities.cacheThis(new InputStreamReader(DownloadUtilities.getJSONData(params[0])), f);
             } catch (Exception ex) {
                 Log.e("DownloadTask", "Failed to cache " + f.getName(), ex);
             }
         }
         return null;
     }
 
     @Override
     protected void onPostExecute(Void retVal) {
         LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
     }
 
 }
