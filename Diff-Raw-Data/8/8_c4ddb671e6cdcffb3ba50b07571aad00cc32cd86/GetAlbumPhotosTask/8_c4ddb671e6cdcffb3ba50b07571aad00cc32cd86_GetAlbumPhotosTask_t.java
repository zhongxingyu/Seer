 package com.lopefied.pepemon.task;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.facebook.android.Util;
 import com.lopefied.pepemon.model.Photo;
 import com.lopefied.pepemon.util.FBParseUtils;
 
 /**
  * 
  * @author Lope Chupijay Emano
  * 
  */
 public class GetAlbumPhotosTask extends AsyncTask<String, Void, List<Photo>> {
     public static final String TAG = GetAlbumPhotosTask.class.getSimpleName();
     public static final Integer PAGE_COUNT = 8;
     private ProgressDialog progressDialog;
     private IAlbumPhotosDownloader albumPhotosDownloader;
     private String accessToken;
     private Integer page;
 
     public GetAlbumPhotosTask(IAlbumPhotosDownloader albumPhotosDownloader,
             ProgressDialog progressDialog, String accessToken, Integer page) {
         this.albumPhotosDownloader = albumPhotosDownloader;
         this.progressDialog = progressDialog;
         this.accessToken = accessToken;
         this.page = page;
     }
 
     @Override
     protected void onPreExecute() {
         // SHOW THE PROGRESS BAR (SPINNER) WHILE LOADING ALBUMS
         progressDialog.show();
     }
 
     private String createLimit() {
         return page + "," + PAGE_COUNT;
     }
 
     @Override
     protected List<Photo> doInBackground(String... albumIDs) {
         List<Photo> albumPhotoList = new ArrayList<Photo>();
 
         // SET THE INITIAL URL TO GET THE FIRST LOT OF ALBUMS
         String albumID = albumIDs[0];
         try {
             String queryAlbumPhotos = "";
 
             String query = "SELECT pid,src_big,images FROM photo WHERE aid=\""
                    + albumID + "\" LIMIT "
                     + createLimit();
             Log.i(TAG, "Query dump : " + query);
             Bundle b = new Bundle();
             b.putString("access_token", accessToken);
             b.putString("q", query);
 
             try {
                 queryAlbumPhotos = Util.openUrl(
                         "https://graph.facebook.com/fql", "GET", b);
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             JSONObject JOTemp = new JSONObject(queryAlbumPhotos);
 
             JSONArray JAAlbumPhotos = JOTemp.getJSONArray("data");
 
             if (JAAlbumPhotos.length() == 0) {
                 albumPhotosDownloader.noMoreAlbumPhotos();
             } else {
                 // PAGING JSONOBJECT
                 Photo photo;
                 for (int i = 0; i < JAAlbumPhotos.length(); i++) {
                     JSONObject JOPhoto = JAAlbumPhotos.getJSONObject(i);
                     photo = new Photo();
                     // GET THE ALBUM ID
                     if (JOPhoto.has("pid")) {
                         photo.setPhotoID(JOPhoto.getString("pid"));
                     } else {
                         photo.setPhotoID(null);
                     }
                     String returnImageURL = FBParseUtils
                             .extractURLFromImageObject(JOPhoto);
                     photo.setPhotoURL(returnImageURL);
                     albumPhotoList.add(photo);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return albumPhotoList;
     }
 
     @Override
     protected void onPostExecute(List<Photo> albumPhotoList) {
         // HIDE THE PROGRESS BAR (SPINNER) AFTER LOADING ALBUMS
         progressDialog.hide();
         albumPhotosDownloader.foundAlbumPhotos(albumPhotoList);
         albumPhotosDownloader.noMoreAlbumPhotos();
     }
 
     public interface IAlbumPhotosDownloader {
         public String getFBAccessToken();
 
         public void noMoreAlbumPhotos();
 
         public void foundAlbumPhotos(List<Photo> photoList);
     }
 
 }
