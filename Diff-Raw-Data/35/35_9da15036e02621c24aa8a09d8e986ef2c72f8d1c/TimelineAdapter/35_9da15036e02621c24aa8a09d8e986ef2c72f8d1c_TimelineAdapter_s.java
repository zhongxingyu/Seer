 package com.company.tweeter;
 
 import java.util.Hashtable;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class TimelineAdapter extends SimpleCursorAdapter {
 
 	private Activity activity;
 	private Cursor data;
 	
 	private CacheManager cacheManager;
 	
 	private Hashtable<String, String> imageUrlHastable;
 	
 	public TimelineAdapter(Activity activity, int layout, Cursor c,
 			String[] from, int[] to) {
 		super(activity, layout, c, from, to);
 		// TODO Auto-generated constructor stub
 		this.activity = activity;
 		this.data = c;
 		
 		cacheManager = CacheManager.getInstance();
 		imageUrlHastable = new Hashtable<String, String>();
 		
 		Log.d(Constants.TAG, "Inside TimelineAdapter Constructor");
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		View v = convertView;
 		if(v == null) {
 			LayoutInflater inflater = activity.getLayoutInflater();
 			v = inflater.inflate(R.layout.tweet_row, null);
 		}
 		
 		TextView username = (TextView) v.findViewById(R.id.username);
 		TextView time = (TextView) v.findViewById(R.id.time);
 		TextView tweetMessage = (TextView) v.findViewById(R.id.tweetMessage);
 		TextView retweetedBy = (TextView) v.findViewById(R.id.retweetedBy);
 		
 		ImageView userProfileImageView = (ImageView) v.findViewById(R.id.userImageView);
 		
 		if(data.moveToPosition(position)) {
 			String usernameString = data.getString(data.getColumnIndex(Constants.USERNAME));
 			String imageUrl = data.getString(data.getColumnIndex(Constants.PROFILE_IMAGE));
 			
 			if(!imageUrlHastable.contains(usernameString)) {
 				imageUrlHastable.put(usernameString, imageUrl);
 			}
 			
 			username.setText(usernameString);
 			time.setText(data.getString(data.getColumnIndex(Constants.CREATED_TIME)));
 			tweetMessage.setText(data.getString(data.getColumnIndex(Constants.TWEET)));
 			retweetedBy.setText(data.getString(data.getColumnIndex(Constants.RETWEETED_BY)));
 			
 			String imagePath = cacheManager.getImageForKey(usernameString);
 			
 			if(imagePath == null) {
 				userProfileImageView.setImageResource(R.drawable.ic_launcher);
 //				Log.d(Constants.TAG, "Now downloading image for..." + usernameString);
 //				Log.d(Constants.TAG, "###########");
 				ImageDownloader downloader = new ImageDownloader();
 				downloader.execute(imageUrlHastable);
 			}
 			else {
 				Bitmap bm = getImageBitmapFromPath(imagePath);
 				userProfileImageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bm));
 //				Log.d(Constants.TAG, "Image found at..." + imagePath);
 //				Log.d(Constants.TAG, "###########");
 			}
 			
 		}
 		
 		return v;
 	}
 	
 	private Bitmap getImageBitmapFromPath(String imagePath) {
 		return BitmapFactory.decodeFile(imagePath);
 	}
 	
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return super.getCount();
 	}
 
 }
