 package com.example.ucrinstagram;
 
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.ucrinstagram.Models.Photo;
 
 public class SinglePicture extends Activity {
 	String username = Login.username.toLowerCase().replaceAll("\\s", "");
 	InputStream is;
 	String link;
 	String caption;
	String gps;
 	int photoId;
 	String[] tokens;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_single_picture);
 
 		link = getIntent().getExtras().getString("link");
 		caption = getIntent().getExtras().getString("caption");
 		photoId = getIntent().getExtras().getInt("photoid");
		gps 	= getIntent().getExtras().getString("gps");
 
 		String prefix = "https://s3.amazonaws.com/";
 		String noPrefixStr = link.substring(link.indexOf(prefix)
 				+ prefix.length());
 		tokens = noPrefixStr.split("/");
 
 		TextView textView = (TextView) findViewById(R.id.textView1);
 		textView.setText(tokens[1]);
 
 		TextView textView2 = (TextView) findViewById(R.id.textView2);
 		textView2.setText(caption);
 
		//WebAPI api = new WebAPI();
		TextView textView3 = (TextView) findViewById(R.id.textView3);
		textView3.setText(gps);
		//System.out.println(api.getPhoto(photoId).caption);
 		new DownloadImageTask((ImageView) findViewById(R.id.imageView1))
 				.execute(link);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_single_picture, menu);
 		return true;
 	}
 
 	public void delete(View view) {
 		System.out.println(username);
 		System.out.println(tokens[1]);
 		if (username.equals(tokens[1])) {
 			WebAPI api = new WebAPI();
 			Photo tempPhoto = api.getPhoto(photoId);
 			tempPhoto.deletePhoto();
 			Toast.makeText(this.getApplicationContext(), "Deleted",
 					Toast.LENGTH_LONG).show();
 		} else {
 			Toast.makeText(this.getApplicationContext(),
 					"You don't own this picture", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 		ImageView bmImage;
 
 		public DownloadImageTask(ImageView bmImage) {
 			this.bmImage = bmImage;
 		}
 
 		protected Bitmap doInBackground(String... urls) {
 			String urldisplay = urls[0];
 			Bitmap mIcon11 = null;
 			try {
 				InputStream in = new java.net.URL(urldisplay).openStream();
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inSampleSize = 2;
 				mIcon11 = BitmapFactory.decodeStream(in, null, options);
 			} catch (Exception e) {
 				Log.e("Error", e.getMessage());
 				e.printStackTrace();
 			}
 			return mIcon11;
 		}
 
 		protected void onPostExecute(Bitmap result) {
 			bmImage.setImageBitmap(result);
 		}
 	}
 
 }
