 package com.gotosauna.activity.photo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.gotosauna.R;
 import com.gotosauna.util.ImageDownloader;
 import com.gotosauna.util.JSONDownloader;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class SaunaPhotosActivity extends Activity {
 	private static final String URL_KEY="url";	
 	ImageDownloader downloader;
 	
     @Override    
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.sauna_photos_gallery);
         
   		Bundle extras = getIntent().getExtras(); 
   		final String url = extras.getString(URL_KEY);
   		        
         this.downloader = new ImageDownloader();
         
   		runOnUiThread(new Runnable() {
  		     public void run() {
  		    	new DownloadWebpageText().execute(url);
  		    }
  		}); 
     }	
     
     private class DownloadWebpageText extends AsyncTask<String, Integer, JSONObject> {
     	Dialog progress;
     	
         @Override
         protected void onPreExecute() {
         	progress = ProgressDialog.show(SaunaPhotosActivity.this, getResources().getString(R.string.loading_data), getResources().getString(R.string.please_wait));             
             super.onPreExecute();
         }
         
     	@Override
 		protected JSONObject doInBackground(String... urls) {
             String url = urls[0];
 			try {
                  return (JSONObject) JSONDownloader.downloadUrl(url, false);
             } catch (IOException e) {
                 return null;
             }		
 		}
        
 		@Override
 		 protected void onProgressUpdate(Integer... progress) {
 			 super.onProgressUpdate(progress);			 
 		 }
 	     
 		@Override
 		protected void onPostExecute(JSONObject result) {	
 			fillListView(result);			
 			progress.dismiss();
 		}		
 		
 	    private void fillListView(JSONObject result){
 			try {										
                 JSONArray array = result.getJSONArray("sauna_photos");		
                 
                final ArrayList<String> urls = new ArrayList<String>();                               
                for (int i = 0; i < array.length(); i++) {
                     JSONObject jo = (JSONObject) array.get(i);
                     urls.add(jo.getString("photo_url"));
                 }
                            
                 if (urls.size() > 0) {              
                     Gallery gallery = (Gallery) findViewById(R.id.gallery);                    
                     ImageAdapter ia = new ImageAdapter(SaunaPhotosActivity.this, urls, downloader);
                     gallery.setAdapter(ia);        
                     gallery.setOnItemClickListener(new OnItemClickListener() 
                     {
                         public void onItemClick(AdapterView parent, View v, int position, long id) 
                         {   
                         	ImageView imageView = (ImageView) findViewById(R.id.preview_image);                        	
                         	File f = new File(imageView.getContext().getCacheDir(), String.valueOf(urls.get(position).hashCode()));              	            
                             imageView.setImageBitmap(downloader.imageCache.get(f.getPath()));                                   	    	 	
                         }
                     });  
                 } else {                   
                     TextView header = (TextView) findViewById(R.id.gallery_header);
                 	header.setText(getResources().getString(R.string.no_data));
                 }
 			} catch (JSONException e) {				
 			}   	  			
 	    }
     }     
 }
