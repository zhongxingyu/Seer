 package com.lopefied.pepemon;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.lopefied.pepemon.adapter.AlbumListAdapter;
 import com.lopefied.pepemon.adapter.AlbumListAdapter.IAlbumListAdapter;
 import com.lopefied.pepemon.model.Album;
 import com.lopefied.pepemon.task.GetAlbumsFQLTask;
 import com.lopefied.pepemon.task.GetAlbumsTask.IAlbumDownloader;
 
 /**
  * 
  * @author Lope Chupijay Emano
  * 
  */
 public class MainActivity extends Activity {
     Facebook facebook = new Facebook("195085947295131");
     AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
     private static final String FACEBOOK_ID = "pepemon3";
 
     private SharedPreferences mPrefs;
     private ListView listView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         /*
          * Get existing access_token if any
          */
         mPrefs = this.getSharedPreferences("com.lopefied.pepemon", MODE_WORLD_READABLE);
         String accessToken = mPrefs.getString("access_token", null);
         System.out.println("Access token : " + accessToken);
         long expires = mPrefs.getLong("access_expires", 0);
         if (accessToken != null) {
             facebook.setAccessToken(accessToken);
         }
         if (expires != 0) {
             facebook.setAccessExpires(expires);
         }
 
         /*
          * Only call authorize if the access_token has expired.
          */
         if (!facebook.isSessionValid()) {
 
             facebook.authorize(this, new String[] {}, new DialogListener() {
                 @Override
                 public void onComplete(Bundle values) {
                     System.out.println("Return access token : "
                             + facebook.getAccessToken());
                     SharedPreferences.Editor editor = mPrefs.edit();
                     editor.putString("access_token", facebook.getAccessToken());
                     editor.putLong("access_expires",
                             facebook.getAccessExpires());
                     editor.commit();
                    downloadAlbum(facebook.getAccessToken());
                 }
 
                 @Override
                 public void onFacebookError(FacebookError error) {
                     System.out.println("onFBError");
                 }
 
                 @Override
                 public void onError(DialogError e) {
                     e.printStackTrace();
                 }
 
                 @Override
                 public void onCancel() {
                     System.out.println("onCancel");
                 }
             });
         }
         if (accessToken != null)
             downloadAlbum(accessToken);
     }
 
     private void downloadAlbum(final String accessToken) {
         final ProgressDialog progressDialog = new ProgressDialog(this);
         IAlbumDownloader albumDownloaderListener = new IAlbumDownloader() {
 
             @Override
             public void noMoreAlbums() {
                 progressDialog.dismiss();
             }
 
             @Override
             public String getFBAccessToken() {
                 return accessToken;
             }
 
             @Override
             public void foundAlbums(List<Album> albumList) {
                 downloadAndDisplayPictures(albumList);
             }
         };
         progressDialog.setCancelable(false);
         progressDialog.setMessage("Downloading albums..");
         progressDialog
                 .setProgressStyle(ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
         progressDialog.setProgress(0);
         progressDialog.setMax(100);
         GetAlbumsFQLTask task = new GetAlbumsFQLTask(albumDownloaderListener,
                 progressDialog, accessToken);
         task.execute(FACEBOOK_ID);
     }
 
     private void downloadAndDisplayPictures(List<Album> albumList) {
         final String accessToken = mPrefs.getString("access_token", null);
         listView = (ListView) findViewById(R.id.listView);
         IAlbumListAdapter albumListListener = new IAlbumListAdapter() {
             @Override
             public String getFBToken() {
                 return accessToken;
             }
 
             @Override
             public void selected(Album album) {
                 launchAlbumPhotoList(album.getAlbumID());
             }
         };
         final AlbumListAdapter adapter = new AlbumListAdapter(this,
                 R.layout.item_album, albumList, albumListListener);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> arg0, View arg1,
                     int position, long arg3) {
                 launchAlbumPhotoList(adapter.getItem(position).getAlbumID());
             }
         });
     }
 
     public void launchAlbumPhotoList(String albumID) {
         Intent intent = new Intent();
         intent.setClass(getApplicationContext(), AlbumPhotosActivity.class);
         intent.putExtra(AlbumPhotosActivity.ALBUM_ID, albumID);
         startActivity(intent);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         facebook.authorizeCallback(requestCode, resultCode, data);
     }
 
 }
