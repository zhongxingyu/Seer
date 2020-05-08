 package edu.mit.mobile.android.net;
 /*
  * Copyright (C) 2012-2013  MIT Mobile Experience Lab
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation version 2
  * of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.apache.http.HttpStatus;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.support.v4.content.AsyncTaskLoader;
 import android.util.Log;
 import edu.mit.mobile.android.flipr.BuildConfig;
 import edu.mit.mobile.android.flipr.R;
 import edu.mit.mobile.android.locast.Constants;
 import edu.mit.mobile.android.locast.net.NetworkProtocolException;
 import edu.mit.mobile.android.utils.StreamUtils;
 
 public class DownloadLoader extends AsyncTaskLoader<Uri> {
 
     public static final String TAG = DownloadLoader.class.getSimpleName();
 
     /**
      * Videos smaller than this will be presumed corrupted.
      */
     private static final long MINIMUM_REASONABLE_VIDEO_SIZE = 1024; // bytes
 
     Exception mException;
     private final String mUrl;
     private final File outdir;
 
     private final ConnectivityManager mCm;
 
     public DownloadLoader(Context context, String url, File destinationDir) {
         super(context);
         mUrl = url;
         outdir = destinationDir;
         mCm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 
     }
 
     @Override
     protected void onStartLoading() {
         super.onStartLoading();
 
         forceLoad();
 
     }
 
     public Exception getException() {
         return mException;
     }
 
     @Override
     public Uri loadInBackground() {
         boolean alreadyHasDownload = false;
 
         if (!outdir.exists() && !outdir.mkdirs()) {
             mException = new IOException("could not mkdirs: " + outdir.getAbsolutePath());
             return null;
         }
         final String fname = mUrl.substring(mUrl.lastIndexOf('/'));
         final File outfile = new File(outdir, fname);
 
         alreadyHasDownload = outfile.exists() && outfile.length() > MINIMUM_REASONABLE_VIDEO_SIZE;
 
         final long lastModified = outfile.exists() ? outfile.lastModified() : 0;
 
         try {
             final NetworkInfo netInfo = mCm.getActiveNetworkInfo();
             if (netInfo == null || !netInfo.isConnected()) {
                 // no connection, but there's already a file. Hopefully it works!
                 if (alreadyHasDownload) {
                     return Uri.fromFile(outfile);
                 } else {
                     mException = new IOException(getContext().getString(
                             R.string.err_no_data_connection));
                     return null;
                 }
             }
 
             HttpURLConnection hc = (HttpURLConnection) new URL(mUrl).openConnection();
 
             hc.setInstanceFollowRedirects(true);
 
             if (lastModified != 0) {
                 hc.setIfModifiedSince(lastModified);
             }
 
             int resp = hc.getResponseCode();
 
             if (resp >= 300 && resp < 400) {
 
                final String redirectUrl = hc.getURL().toExternalForm();

                 if (BuildConfig.DEBUG) {
                     Log.d(TAG, "following redirect to " + redirectUrl);
                 }
                 hc = (HttpURLConnection) new URL(redirectUrl).openConnection();
 
                 if (lastModified != 0) {
                     hc.setIfModifiedSince(lastModified);
                 }
                 resp = hc.getResponseCode();
             }
 
             if (resp != HttpStatus.SC_OK && resp != HttpStatus.SC_NOT_MODIFIED) {
                 Log.e(TAG, "got a non-200 response from server");
                 mException = new NetworkProtocolException("Received " + resp
                         + " response from server");
                 return null;
             }
             if (resp == HttpStatus.SC_NOT_MODIFIED) {
                 if (Constants.DEBUG) {
                     Log.d(TAG, "got NOT MODIFIED");
                 }
                 // verify the integrity of the file
                 if (alreadyHasDownload) {
                     if (Constants.DEBUG) {
                         Log.d(TAG, fname + " has not been modified since it was downloaded last");
                     }
                     return Uri.fromFile(outfile);
                 } else {
                     // re-request without the if-modified header. This shouldn't happen.
                     hc = (HttpURLConnection) new URL(mUrl).openConnection();
                     final int responseCode = hc.getResponseCode();
                     if (responseCode != HttpStatus.SC_OK) {
                         Log.e(TAG, "got a non-200 response from server");
                         mException = new NetworkProtocolException("Received " + responseCode
                                 + " response from server");
                         return null;
                     }
                 }
             }
 
             final int contentLength = hc.getContentLength();
 
             if (contentLength == 0) {
                 Log.e(TAG, "got an empty response from server");
                 mException = new IOException("Received an empty response from server.");
                 return null;
 
             } else if (contentLength < MINIMUM_REASONABLE_VIDEO_SIZE) { // this is probably not a
                                                                          // video
                 Log.e(TAG,
                         "got a very small response from server of length " + hc.getContentLength());
                 mException = new IOException("Received an unusually-small response from server.");
                 return null;
             }
             boolean downloadSucceeded = false;
             try {
 
                 final BufferedInputStream bis = new BufferedInputStream(hc.getInputStream());
                 final FileOutputStream fos = new FileOutputStream(outfile);
                 if (Constants.DEBUG) {
                     Log.d(TAG, "downloading...");
                 }
                 StreamUtils.inputStreamToOutputStream(bis, fos);
 
                 fos.close();
 
                 // store the server's last modified date in the filesystem
                 outfile.setLastModified(hc.getLastModified());
 
                 downloadSucceeded = true;
                 if (Constants.DEBUG) {
                     Log.d(TAG, "done! Saved to " + outfile);
                 }
                 return Uri.fromFile(outfile);
 
             } finally {
                 hc.disconnect();
                 // cleanup if this is the first attempt to download
                 if (!alreadyHasDownload && !downloadSucceeded) {
                     outfile.delete();
                 }
             }
         } catch (final IOException e) {
             Log.e(TAG, "error downloading file", e);
             mException = e;
             return null;
         }
     }
 }
