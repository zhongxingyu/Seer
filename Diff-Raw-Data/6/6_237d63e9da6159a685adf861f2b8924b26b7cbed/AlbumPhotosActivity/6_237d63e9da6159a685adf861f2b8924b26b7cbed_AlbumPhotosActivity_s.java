 package com.allplayers.android;
 
 import com.allplayers.rest.RestApiV1;
 import com.allplayers.objects.AlbumData;
 import com.allplayers.objects.PhotoData;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ListView;
 import android.widget.AdapterView;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class AlbumPhotosActivity extends Activity {
     private ArrayList<PhotoData> photoList;
     private ArrayAdapter blankAdapter = null;
     private PhotoAdapter photoAdapter;
     private int currentPage;
     private int currentAmountShown;
     private GridView grid;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.albumdisplay);
         AlbumData album = (new Router(this)).getIntentAlbum();
         photoList = new ArrayList<PhotoData>();
         grid = (GridView) findViewById(R.id.gridview);
         grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 if (photoList.get(position) != null) {
                     // Display the group page for the selected group
                     Intent intent = (new Router(AlbumPhotosActivity.this)).getPhotoPagerActivityIntent(photoList.get(position));
                     startActivity(intent);
                 }
             }
         });
         new GetAlbumPhotosByAlbumIdTask().execute(album);
     }
 
     public void setAdapter() {
         if (photoList.isEmpty()) {
             String[] values = new String[] {"no photos to display"};
 
             blankAdapter = new ArrayAdapter<String>(AlbumPhotosActivity.this,
                                                     android.R.layout.simple_list_item_1, values);
             grid.setAdapter(blankAdapter);
         } else {
             //Create a customized ArrayAdapter
             photoAdapter = new PhotoAdapter(getApplicationContext());
             grid.setAdapter(photoAdapter);
         }
     }
 
     public void updateAlbumPhotos(String jsonResult) {
         PhotosMap photos = new PhotosMap(jsonResult);
         photoList.addAll(photos.getPhotoData());
         setAdapter();
        photoAdapter.addAll(photoList);
        currentAmountShown += photoList.size();
         /*for (int i = currentAmountShown; i < photoList.size(); i++, currentAmountShown++) {
             photoAdapter.add(photoList.get(i));
         }*/
     }
 
     /*
      * Gets the photos from an album specified by its album ID using a rest call.
      */
     public class GetAlbumPhotosByAlbumIdTask extends AsyncTask<AlbumData, Void, String> {
 
         protected String doInBackground(AlbumData... album) {
             return RestApiV1.getAlbumPhotosByAlbumId(album[0].getUUID(), 0, 0);
         }
 
         protected void onPostExecute(String jsonResult) {
             updateAlbumPhotos(jsonResult);
         }
     }
 }
