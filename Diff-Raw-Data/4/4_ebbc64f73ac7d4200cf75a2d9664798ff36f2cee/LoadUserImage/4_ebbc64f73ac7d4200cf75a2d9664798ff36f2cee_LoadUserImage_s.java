 package com.sniper.utility;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.widget.ImageView;
 
 import com.parse.ParseUser;
 import com.sniper.R;
 
 public class LoadUserImage {
 	private static HashMap<String, UserImage> map = new HashMap<String, UserImage>();
 	public class UserImage{
 		public Bitmap image;
 		private Date date;
 		public UserImage(Bitmap image){
 			this(image, new Date());
 		}
 		public UserImage(Bitmap image, Date date){
 			this.date = date;
 			this.image = image;
 		}
 		public boolean ShouldUpdate(){
 			Date now = new Date();
 			final Calendar c = Calendar.getInstance();
 			c.add(c.MINUTE, -5);
 			now.setTime(c.getTimeInMillis());
 			
 			return now.after(date);
 		}
 	}
 	public static void UpdateImage(ParseUser user, Bitmap image){
 		 LoadUserImage.map.put(user.getObjectId(), 
 				 new LoadUserImage().new UserImage(image));         		
 	}
 	
 	public static void GetImage(ParseUser user, Activity activity){
 		UserImage image = map.get(user.getObjectId());
 		if(image != null && !image.ShouldUpdate()){
 			activity.runOnUiThread(new LoadUserImage().new UpdateImage(image.image, activity));
 			return;
 		}
 		
 		URL url = null;
 		try {
 			url = new URL("https://s3.amazonaws.com/sniper_profilepictures/" 
 					+ user.getObjectId());
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}		
 		GetImageRequest request = new GetImageRequest(activity, user);
 		request.execute(url);
 	}
 	
 	class UpdateImage implements Runnable{
 		public Bitmap bitmap;
 		public Activity activity;
 		public UpdateImage(Bitmap bitmap, Activity activity){
 			this.bitmap = bitmap;
 			this.activity = activity;
 		}
 		public void run(){
 			ImageView output = (ImageView) activity.findViewById(R.id.user_image);
 			output.setImageBitmap(bitmap);
 		}	
 	}
 	public static class GetImageRequest extends AsyncTask<URL, Void, String>
 	{
 		/** progress dialog to show user that the backup is processing. */
 	    private ProgressDialog dialog;
 	    /** application context. */
 	    private Activity activity;
 	    /** application context. */
         private Context context;
 	    
         private String userId;
         
 		public GetImageRequest(Activity activity, ParseUser user){
 			this.activity = activity;
 			context = activity;
 			dialog = new ProgressDialog(context);
 			userId = user.getObjectId();
 		}
 		
 		 protected void onPreExecute() {
 	            this.dialog.setMessage("Loading...");
 	            this.dialog.show();
 	        }
 		 
 		 protected void onPostExecute(String result)
 			{
 				super.onPostExecute(result);
 				if (dialog.isShowing()) 
 	                dialog.dismiss();
 			}
 		protected String doInBackground(URL... params)
 		{
 			URL url = params[0];
 			
 			try {
 				Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
 				LoadUserImage.map.put(userId, 
 						 new LoadUserImage().new UserImage(image)); 				
 				activity.runOnUiThread(new LoadUserImage().new UpdateImage(image, activity));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 	        return url.toString();
 		}
 		
 	}
 }
