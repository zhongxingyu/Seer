 package sk.ksp.riso.imgView;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.content.ComponentName;
 import android.util.Log;
 import android.webkit.WebView;
 import android.os.ParcelFileDescriptor;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 public class imgView extends Activity
 {
     static final String TAG = "imgView";
     public WebView wv;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         wv = new WebView(this);
         setContentView(wv);
 
         wv.getSettings().setBuiltInZoomControls(true);
 
         /*
         Intent in = new Intent(getIntent());
         in.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
         in.setAction(android.content.Intent.ACTION_VIEW);
         Log.v(TAG, "Starting browser");
         startActivity(in);
         */
 
         try {
           ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(getIntent().getData(), "r");
           long len = pfd.getStatSize();
           FileInputStream f = new FileInputStream(pfd.getFileDescriptor());
           byte[] buf = new byte[(int)len];
           len = f.read(buf);
           f.close();
           String img = "<html><head></head><body><img src=\"data:image/gif;base64," +
             android.util.Base64.encodeToString(buf, android.util.Base64.DEFAULT) +
             "\"></body></html>";
           wv.loadData(img, "text/html", "utf8");
         } catch (IOException e) {
           wv.loadData("Some problem", "text/html", "utf8");
         }
 
     }
 }
