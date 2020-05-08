 package com.thoughtworks.mingle.murmurs;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
import android.util.Log;
 import android.widget.ImageView;
 
 import com.dephillipsdesign.io.IOUtils;
 import com.thoughtworks.mingle.murmurs.IconCache.IconViewDownloader;
 
 public class AuthorIconView extends ImageView {
 
   public AuthorIconView(Context context) {
     super(context);
   }
 
   public AuthorIconView(Context context, AttributeSet attributes) {
     this(context, attributes, 0);
   }
 
   public AuthorIconView(Context context, AttributeSet attributes, int style) {
     super(context, attributes, style);
   }
 
   public void setImageURI(final Uri uri) {
    Log.d(AuthorIconView.class.getName(), "Setting uri: " + uri);
     IconViewDownloader iconDownloader = new IconViewDownloader(new Handler() {
       public void handleMessage(Message msg) {
        Log.d(AuthorIconView.class.getName(), "Setting image bitmap from: " + IconCache.getCachedIconPath(uri));
         String localImagePath = IconCache.getCachedIconPath(uri);
         InputStream inputStream = IOUtils.openInputStream(localImagePath);
         ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();
         IOUtils.copyStream(inputStream, tempOutputStream);
         byte[] imageData = tempOutputStream.toByteArray();
        Log.d(AuthorIconView.class.getName(), "Image file " + localImagePath + " size: " + imageData.length + " bytes");

        Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(imageData));
         setImageBitmap(bitmap);
       }
     });
     iconDownloader.execute(uri.toString());
   }
 }
