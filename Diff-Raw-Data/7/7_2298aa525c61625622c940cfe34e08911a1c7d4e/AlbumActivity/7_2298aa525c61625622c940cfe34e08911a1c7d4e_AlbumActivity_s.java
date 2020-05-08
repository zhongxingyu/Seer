 package net.sourcewalker.smugview.gui;
 
 import net.sourcewalker.smugview.R;
 import net.sourcewalker.smugview.data.SmugView;
 import net.sourcewalker.smugview.parcel.AlbumInfo;
 import net.sourcewalker.smugview.parcel.Extras;
 import android.app.ListActivity;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Window;
 import android.widget.ListAdapter;
 import android.widget.SimpleCursorAdapter;
 
 public class AlbumActivity extends ListActivity {
 
     protected static final String TAG = "AlbumActivity";
 
     private ListAdapter listAdapter;
     private long albumId;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.album);
 
         albumId = (Long) getIntent().getExtras().get(Extras.EXTRA_ALBUM);
 
        Cursor cursor = getContentResolver()
                .query(ContentUris.withAppendedId(SmugView.Album.CONTENT_URI,
                        albumId), SmugView.Album.DEFAULT_PROJECTION, null,
                        null, null);
         cursor.moveToFirst();
         AlbumInfo album = new AlbumInfo(cursor);
 
         setTitle(album.getTitle());
 
         startGetImages();
     }
 
     private void startGetImages() {
         new GetImagesTask().execute(albumId);
     }
 
     private class GetImagesTask extends AsyncTask<Long, Void, Cursor> {
 
         @Override
         protected void onPreExecute() {
             setProgressBarIndeterminateVisibility(true);
         }
 
         @Override
         protected Cursor doInBackground(Long... params) {
             return managedQuery(SmugView.Image.CONTENT_URI,
                     SmugView.Image.DEFAULT_PROJECTION, SmugView.Image.ALBUM_ID
                             + " = ?", new String[] { params[0].toString() },
                     null);
         }
 
         @Override
         protected void onPostExecute(Cursor result) {
             setProgressBarIndeterminateVisibility(false);
 
             listAdapter = new SimpleCursorAdapter(AlbumActivity.this,
                     R.layout.album_row, result,
                     new String[] { SmugView.Image.DESCRIPTION,
                             SmugView.Image.FILENAME }, new int[] {
                             R.id.image_desc, R.id.image_filename });
             setListAdapter(listAdapter);
         }
 
     }
 
     // private class GetThumbnailTask extends AsyncTask<Object, Void, Object[]>
     // {
     //
     // @Override
     // protected Object[] doInBackground(Object... params) {
     // AlbumAdapter adapter = (AlbumAdapter) params[0];
     // ImageInfo image = (ImageInfo) params[1];
     // String thumbUrl = image.getThumbUrl();
     // Drawable result = null;
     // if (thumbUrl != null) {
     // HttpGet get = new HttpGet(thumbUrl);
     // HttpClient client = new DefaultHttpClient();
     // try {
     // HttpResponse response = client.execute(get);
     // result = new BitmapDrawable(response.getEntity()
     // .getContent());
     // } catch (IOException e) {
     // Log.e("GetThumbnailTask",
     // "Error getting thumbnail: " + e.getMessage());
     // }
     // }
     // return new Object[] { adapter, image, result };
     // }
     //
     // @Override
     // protected void onPostExecute(Object[] result) {
     // AlbumAdapter adapter = (AlbumAdapter) result[0];
     // ImageInfo image = (ImageInfo) result[1];
     // Drawable thumbnail = (Drawable) result[2];
     // if (thumbnail != null) {
     // image.setThumbnail(thumbnail);
     // adapter.update(image);
     // }
     // }
     // }
 
 }
