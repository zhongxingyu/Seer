 package com.livejournal.karino2.multigallery;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.MediaStore;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 
 import com.livejournal.karino2.whiteboardcast.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class MultiGalleryActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_multi_gallery);
 
         startAlbumSetLoad();
 
     }
 
     private void startAlbumSetLoad() {
         GridView grid = getGridView();
         grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 MediaHolder holder = (MediaHolder)view.getTag();
                 if(holder != null && holder.getItem() != null) {
                     setAlbum((AlbumItem)holder.getItem());
                 }
             }
         });
 
         getExecutor().submit(new AlbumSetLoadTask());
     }
 
     private GridView getGridView() {
         return (GridView)findViewById(R.id.grid);
     }
 
     boolean isAlbum = false;
 
     private void setAlbum(AlbumItem album) {
         AlbumLoader loader = new AlbumLoader(getContentResolver(), album);
         AlbumSlidingWindow slidingWindow = new AlbumSlidingWindow(loader);
         AlbumAdapter adapter = new AlbumAdapter(slidingWindow);
         discardAllPendingRequest();
         getGridView().setAdapter(adapter);
         isAlbum = true;
     }
 
     int getThumbnailSize() {
         return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, THUMBNAIL_SIZE, getResources().getDisplayMetrics());
     }
 
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.multi_gallery, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         int id = item.getItemId();
         if (id == R.id.action_settings) {
             return true;
         }
         if(id == android.R.id.home && isAlbum) {
             finishAlbumAndStartAlbumSet();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void finishAlbumAndStartAlbumSet() {
         discardAllPendingRequest();
         isAlbum = false;
         startAlbumSetLoad();
     }
 
 
     @Override
     public void onBackPressed() {
         if(isAlbum) {
             finishAlbumAndStartAlbumSet();
             return;
         }
         super.onBackPressed();
     }
 
     class AlbumItem implements MediaItem {
         int id;
         String name;
         String path;
         AlbumItem(int id, String name, String path) {
             this.id = id; this.name = name; this.path = path;
         }
         public String getPath() {
             return path;
         }
 
         public int getId() {
             return id;
         }
     }
 
     static final String TOP_PATH = "/local/image";
 
     class AlbumSetLoadTask implements Runnable{
 
         ContentResolver resolver;
         AlbumSetLoadTask() {
             this.resolver = getContentResolver();
         }
 
         @Override
         public void run() {
             ArrayList<MediaItem> albums = new ArrayList<MediaItem>();
 
             Cursor cursor = getContentResolver().query(
                     MediaStore.Files.getContentUri("external"), new String[]{
                     MediaStore.Images.ImageColumns.BUCKET_ID,
                     MediaStore.Files.FileColumns.MEDIA_TYPE,
                     MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                     MediaStore.Images.Media.DATA},
                     "1) GROUP BY 1,(2", null, "MAX(datetaken) DESC");
             try {
                 while (cursor.moveToNext()) {
                     if(((1 << MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) & (1 << cursor.getInt(1))) != 0){
                         albums.add(new AlbumItem(cursor.getInt(0), cursor.getString(2), cursor.getString(3)));
                     }
                 }
 
                 // TODO: reorder here.
                 notifyAlbumsComing(albums);
             }finally {
                 cursor.close();
             }
 
         }
     }
 
 
     Handler handler = new Handler();
 
     private void notifyAlbumsComing(final ArrayList<MediaItem> albums) {
         handler.post(new Runnable() {
             @Override
             public void run() {
                 GridView grid = getGridView();
                 AlbumSetAdapter adapter = new AlbumSetAdapter(albums);
                 grid.setAdapter(adapter);
             }
         });
 
     }
 
     ExecutorService executor;
     ExecutorService getExecutor() {
         if(executor == null) {
            executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()-1));
         }
         return executor;
 
     }
 
     class CacheEngine {
         ArrayList<String> keys = new ArrayList<String>();
         HashMap<String, Bitmap> entries = new HashMap<String, Bitmap>();
         final int CASH_SIZE = 100;
         Bitmap lookup(MediaItem item) {
             if(entries.containsKey(item.getPath()))
                 return entries.get(item.getPath());
             return null;
         }
         void put(MediaItem item, Bitmap thumbnail) {
             String path = item.getPath();
             if(keys.contains(path)) {
                 keys.remove(path);
                 keys.add(path);
                 return;
             }
 
             keys.add(path);
             entries.put(path, thumbnail);
 
             if(keys.size() > CASH_SIZE) {
                 String first = keys.get(0);
                 entries.remove(first);
                 keys.remove(0);
             }
         }
     }
 
     static final int THUMBNAIL_SIZE = 254; //  200;
 
     CacheEngine cache = new CacheEngine();
 
 
     static Bitmap loadingImage;
     public static Bitmap getLoadingBitmap(int thumbnailSize) {
         if(loadingImage == null) {
             loadingImage = Bitmap.createBitmap(thumbnailSize, thumbnailSize, Bitmap.Config.ARGB_8888);
             loadingImage.eraseColor(Color.CYAN); // for debug.
         }
         return loadingImage;
     }
 
     Set<MediaLoadRequest> pendingRequest = new HashSet<MediaLoadRequest>();
     void addToPendingSet(MediaLoadRequest newReq) {
         pendingRequest.add(newReq);
     }
 
     void removePendingSet(MediaLoadRequest req) {
         pendingRequest.remove(req);
     }
 
     void discardAllPendingRequest() {
         for(MediaLoadRequest req : pendingRequest) {
             req.discard();
         }
         pendingRequest.clear();
     }
 
     class MediaHolder implements MediaLoadRequest.MediaLoadListener{
         ImageView target;
         MediaItem item;
         Bitmap thumbnail;
         MediaLoadRequest request;
         public MediaHolder(MediaItem item, ImageView iv) {
             this.item = item;
             target = iv;
         }
 
         public MediaItem getItem() { return item; }
 
         public void recycle(MediaItem item) {
             if(request != null) {
                 request.discard();
                 removePendingSet(request);
                 request = null;
             }
             this.item = item;
         }
 
 
         public void beginLoad() {
             Bitmap thumbnail = cache.lookup(item);
             if(thumbnail != null) {
                 onThumbnailComing(thumbnail);
                 return;
             }
             target.setImageBitmap(getLoadingBitmap(getThumbnailSize()));
             request = new MediaLoadRequest(getItem(), this, getThumbnailSize());
             addToPendingSet(request);
             getExecutor().submit(request);
         }
 
 
         public void onThumbnailComing(Bitmap thumbnail) {
             cache.put(item, thumbnail);
             removePendingSet(request);
             request = null;
             this.thumbnail = thumbnail;
             handler.post(new Runnable() {
                 @Override
                 public void run() {
                     target.setImageBitmap(MediaHolder.this.thumbnail);
                 }
             });
         }
     }
 
     public class AlbumSetAdapter extends BaseAdapter implements ListAdapter {
         ArrayList<MediaItem> albums;
         public AlbumSetAdapter(ArrayList<MediaItem> albums) {
             this.albums = albums;
         }
 
         @Override
         public int getCount() {
             return albums.size();
         }
 
         @Override
         public Object getItem(int i) {
             return albums.get(i);
         }
 
         @Override
         public long getItemId(int i) {
             return (long)i;
         }
 
         @Override
         public View getView(int i, View view, ViewGroup viewGroup) {
             ImageView iv;
             MediaItem item = (MediaItem)getItem(i);
             if(view == null) {
                 iv = new ImageView(MultiGalleryActivity.this);
                 MediaHolder holder = new MediaHolder(item, iv);
                 iv.setTag(holder);
                 holder.beginLoad();
             } else {
                 MediaHolder holder = (MediaHolder)view.getTag();
                 holder.recycle(item);
                 holder.beginLoad();
                 iv = (ImageView)view;
             }
             return iv;
         }
     }
 
     public class AlbumAdapter extends BaseAdapter implements ListAdapter {
         AlbumSlidingWindow slidingWindow;
         public AlbumAdapter(AlbumSlidingWindow albumWindow) {
             this.slidingWindow = albumWindow;
         }
 
         @Override
         public int getCount() {
             return slidingWindow.size();
         }
 
         @Override
         public Object getItem(int i) {
             return slidingWindow.requestSlot(i);
         }
 
         @Override
         public long getItemId(int i) {
             return (long)i;
         }
 
         // TODO: almost the same as AlbumSetAdapter.
         @Override
         public View getView(int i, View view, ViewGroup viewGroup) {
             ImageView iv;
             MediaItem item = (MediaItem)getItem(i);
             if(view == null) {
                 iv = new ImageView(MultiGalleryActivity.this);
                 MediaHolder holder = new MediaHolder(item, iv);
                 iv.setTag(holder);
                 holder.beginLoad();
             } else {
                 MediaHolder holder = (MediaHolder)view.getTag();
                 // might need to recycle for SlidingWindow, but not yet.
                 holder.recycle(item);
                 holder.beginLoad();
                 iv = (ImageView)view;
             }
             return iv;
         }
     }
 
 }
