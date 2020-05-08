 /*
    Copyright 2012 Mikhail Chabanov
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package mc.lib.network;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 
 import mc.lib.interfaces.OnCompleteListener;
 import mc.lib.interfaces.OnProgressListener;
 import mc.lib.log.Log;
 import mc.lib.stream.StreamHelper;
 import android.os.AsyncTask;
 
 public class AsyncFileDownloader extends AsyncTask<Void, Integer, Boolean>
 {
     private static final String LOGTAG = AsyncFileDownloader.class.getSimpleName();
     private static final int READING_TIMEOUT = 5000;
 
     private static int id = 1;
     private static Map<Integer, AsyncFileDownloader> map = new HashMap<Integer, AsyncFileDownloader>();
 
     public static synchronized int downloadFile(String url, String outputFilePath, OnCompleteListener<File> clistener, OnProgressListener plistener)
     {
         try
         {
             return downloadFile(new URL(url), new File(outputFilePath), clistener, plistener);
         }
         catch(MalformedURLException e)
         {
             Log.e(LOGTAG, "Wrong url", e);
         }
         return -1;
     }
 
     public static synchronized int downloadFile(String url, File output, OnCompleteListener<File> clistener, OnProgressListener plistener)
     {
         try
         {
             return downloadFile(new URL(url), output, clistener, plistener);
         }
         catch(MalformedURLException e)
         {
             Log.e(LOGTAG, "Wrong url", e);
         }
         return -1;
     }
 
     public static synchronized int downloadFile(URL url, String outputFilePath, OnCompleteListener<File> clistener, OnProgressListener plistener)
     {
         return downloadFile(url, new File(outputFilePath), clistener, plistener);
     }
 
     public static synchronized int downloadFile(URL url, File output, OnCompleteListener<File> clistener, OnProgressListener plistener)
     {
         if(url != null && output != null)
         {
             AsyncFileDownloader afd = new AsyncFileDownloader(url, output, clistener, plistener);
             id++;
             map.put(id, afd);
             afd.execute((Void[])null);
             return id;
         }
         else
         {
             Log.e(LOGTAG, "URL or Output file is NULL");
         }
         return -1;
     }
 
     public static synchronized void cancelDownload(int id, boolean callOnCompleteListener)
     {
         AsyncFileDownloader afd = map.get(id);
         if(afd == null)
         {
             Log.w(LOGTAG, "Cannot find download task with id" + id);
             return;
         }
         afd.cancel = true;
         afd.callOnCompleteListener = callOnCompleteListener;
     }
 
     private URL url;
     private File file;
     private OnCompleteListener<File> clistener;
     private OnProgressListener plistener;
     private boolean callOnCompleteListener;
     private boolean cancel;
 
     private AsyncFileDownloader(URL url, File file, OnCompleteListener<File> clistener, OnProgressListener plistener)
     {
         super();
         this.url = url;
         this.file = file;
         this.clistener = clistener;
         this.plistener = plistener;
         this.callOnCompleteListener = true;
     }
 
     @Override
     protected Boolean doInBackground(Void... v)
     {
         InputStream is = null;
         OutputStream os = null;
         try
         {
             Log.i(LOGTAG, "Srart downloading url(" + url + ") to file(" + file + ")");
             URLConnection c = url.openConnection();
             int readingTimeout = c.getReadTimeout();
             if(readingTimeout < READING_TIMEOUT)
             {
                 readingTimeout = READING_TIMEOUT;
                 c.setReadTimeout(readingTimeout);
             }
             c.connect();
             is = c.getInputStream();
 
             int fileSize = c.getContentLength();
             if(fileSize > 0)
             {
                 Log.i(LOGTAG, "Size of file to download:" + fileSize);
             }
 
             int readLast = 0, readTotal = 0;
             os = new FileOutputStream(file);
             byte[] buff = new byte[StreamHelper.BUFFER_SIZE];
             readLast = is.read(buff);
             while(readLast > 0)
             {
                 if(cancel)
                 {
                     break;
                 }
 
                 os.write(buff, 0, readLast);
                 readTotal += readLast;
                 publishProgress(readTotal, fileSize);
                 readLast = is.read(buff);
             }
            publishProgress(Integer.MAX_VALUE, Integer.MAX_VALUE);
         }
         catch(IOException e)
         {
             Log.e(LOGTAG, "Exception while reading stream", e);
             file = null;
         }
         catch(Exception e)
         {
             Log.e(LOGTAG, "Cannot open stream", e);
             file = null;
         }
         finally
         {
             StreamHelper.close(os);
             StreamHelper.close(is);
         }
 
         if(cancel)
         {
             Log.i(LOGTAG, "Download task canceled. Id:" + id);
             if(file != null)
             {
                 file.delete();
                 file = null;
             }
             return false;
         }
 
         return true;
     }
 
     @Override
     protected void onProgressUpdate(Integer... p)
     {
         if(plistener != null && !cancel)
         {
             plistener.notifyProgress(p[0], p[1]);
         }
     }
 
     @Override
     protected void onPostExecute(Boolean result)
     {
         if(result)
         {
             Log.i(LOGTAG, "Download complete sucessfully");
         }
         if(clistener != null && callOnCompleteListener)
         {
             clistener.complete(file);
         }
     }
 }
