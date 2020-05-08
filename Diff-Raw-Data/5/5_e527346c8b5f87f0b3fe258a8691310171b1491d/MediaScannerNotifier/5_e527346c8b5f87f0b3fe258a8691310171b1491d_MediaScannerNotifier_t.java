 package jp.yagni.media;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.media.MediaScannerConnection;
 import android.media.MediaScannerConnection.MediaScannerConnectionClient;
 import android.net.Uri;
 import android.provider.MediaStore;
 import android.util.Log;
 
 import java.io.File;
 
 
 /**
  * This class is client for notify to MediaContentProvider.
  * 
  * @author YUKI Kaoru
  */
 public class MediaScannerNotifier implements MediaScannerConnectionClient
 {
    private final String TAG = getClass().getSimpleName();
     
     private Context mContext;
     private MediaScannerConnection mConnection;
     private File mFile;
 
    public MediaScannerNotifier(Context context)
     {
         mContext = context;
         mConnection = new MediaScannerConnection(context, this);
     }
 
     @Override
     public void onMediaScannerConnected()
     {
         Log.d(TAG, "Started scan of " + mFile.getAbsolutePath());
         mConnection.scanFile(mFile.getAbsolutePath(), null);
     }
 
     @Override
     public void onScanCompleted(String path, Uri uri)
     {
         Log.d(TAG, "Completed scan of " + path);
         mConnection.disconnect();
     }
     
     public void scan()
     {
         mConnection.connect();
     }
     
     public void scan(File file)
     {
         mFile = file;
         scan();
     }
     
     public void scan(Uri uri)
     {
         String [] columns = {MediaStore.Images.Media.DATA};
         Cursor c = mContext.getContentResolver().query(uri, columns, null, null, null);
         try {
             if (c.moveToFirst()) {
                 mFile = new File(c.getString(0));
             }
         } finally {
             if (c != null) {
                 c.close();
             }
         }
         scan();
     }
 }
