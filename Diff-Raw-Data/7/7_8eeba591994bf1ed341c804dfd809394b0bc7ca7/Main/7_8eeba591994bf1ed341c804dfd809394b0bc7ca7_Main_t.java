 package com.todoroo.relax;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.todoroo.andlib.service.ContextManager;
 import com.todoroo.andlib.utility.Preferences;
 
 public class Main extends Activity {
     private int current, total;
     ImageSource imageSource;
     String[] results;
 
     private ImageButton previous, next;
     private ImageView image;
     private TextView text;
     private TextView loading;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         final Window win = getWindow();
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         ContextManager.setContext(this);
         setContentView(R.layout.main);
 
         imageSource = new ImageSource();
 
         current = Preferences.getInt("current", 0);
         total= Preferences.getInt("total", 0);
 
         previous = (ImageButton) findViewById(R.id.prev);
         next = (ImageButton) findViewById(R.id.next);
         image = (ImageView) findViewById(R.id.image);
         text = (TextView) findViewById(R.id.text);
         loading = (TextView) findViewById(R.id.loading);
 
         previous.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 current--;
                 if(current < 0) {
                     loadMoreImages(total - 10);
                     if(results.length > 0)
                         current = results.length - 1;
                 }
                 loadResult();
             }
         });
 
         next.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 current++;
                 if(current >= results.length)
                     loadMoreImages(total + 10);
                 loadResult();
                 precacheImages();
             }
         });
 
         int oldCurrent = current;
         loadMoreImages(total);
         current = oldCurrent;
         loadResult();
     }
 
     private void loadMoreImages(int newTotal) {
         int oldTotal = total;
         try {
             total = newTotal;
             results = imageSource.search("kitten", total);
             precacheImages();
         } catch (Exception e) {
             Log.e("error", "loading images", e);
             loading.setText(e.toString());
             results = new String[0];
             total = oldTotal;
         }
         current = 0;
     }
 
     // --- precaching
 
    private static final int MAX_ENTRIES = 5;
 
     private Map<String, Bitmap> mCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(
             MAX_ENTRIES, .75F, true) {
         private static final long serialVersionUID = 1L;
 
         @Override
         protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
             return size() > MAX_ENTRIES;
         }
     });
 
     private void precacheImages() {
         new Thread(new Runnable() {
             @Override
             public void run() {
                 for(int i = current + 1; i < current + 1 + MAX_ENTRIES && i < results.length ; i++) {
                     try {
                         fetch(results[i]);
                     } catch (Throwable e) {
                         Log.e("error", "error",  e);
                     }
                 }
             }
         }).start();
     }
 
     // --- results loading
 
     private Thread loadingThread = null;
 
     private void loadResult() {
         Preferences.setInt("total", total);
         Preferences.setInt("current", current);
 
         previous.setEnabled(total + current > 0);
         next.setEnabled(imageSource.hasMoreResults(total + current));
         text.setText(String.format("%d", total + current + 1));
 
         loading.setVisibility(View.VISIBLE);
 
         if(loadingThread != null)
             loadingThread.interrupt();
         loadingThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     final Bitmap bitmap = fetch(results[current]);
 
                     if(Thread.interrupted())
                         return;
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             image.setImageBitmap(bitmap);
                             loading.setVisibility(View.GONE);
                         }
                     });
 
                 } catch (Throwable e) {
                     Log.e("error", "error",  e);
                 }
             }
         });
         loadingThread.start();
     }
 
     /**
      * Retrieve icon that corresponds with this coach. Has two layers of caching,
      * session-level caching and in-database coach caching.
      *
      * @category tested
      * @return
      */
     public Bitmap fetch(String urlString) throws IOException {
         if(mCache.containsKey(urlString))
             return mCache.get(urlString);
 
         URL url = new URL(urlString);
         System.err.println("fetching " + urlString);
 
         InputStream is = null;
         Bitmap bitmap = null;
         try {
             URLConnection conn = url.openConnection();
             conn.connect();
             is = conn.getInputStream();
             int contentLength = conn.getContentLength();
             if(contentLength < 400000) {
                 // write bytes to output stream
                 byte[] bytes = new byte[contentLength];
                 int offset = 0;
                 while(true) {
                     int byteCount = is.read(bytes, offset, contentLength - offset);
                     if(byteCount == -1)
                         break;
                     else
                         offset += byteCount;
                 }
 
                 bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } else {
                bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.icon)).getBitmap();
             }
         } finally {
             if(is != null)
                 is.close();
         }
         mCache.put(urlString, bitmap);
         return bitmap;
     }
 }
