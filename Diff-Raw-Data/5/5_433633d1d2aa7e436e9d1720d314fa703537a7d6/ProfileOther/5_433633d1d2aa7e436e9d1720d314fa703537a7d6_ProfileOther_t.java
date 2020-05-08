 package com.example.ucrinstagram;
 
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.example.ucrinstagram.Models.Photo;
 import com.example.ucrinstagram.Models.User;
import com.example.ucrinstagram.Models.UserProfile;
 
 public class ProfileOther extends Activity implements OnClickListener {
 
 	ArrayList<String> image_links = new ArrayList<String>();
 	ImageView[] image;
 	User user1;
 	String username;
 	InputStream is;
 	ArrayList<String> image_links2 = new ArrayList<String>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile_other);
 
 		User user2 = new User(getIntent().getExtras().getString("username"));
 		username = user2.username;
 
 		loadInfo();
 		loadPics();
 
 		user1 = new User(username);
 
 		WebAPI api = new WebAPI();
 		Photo profilePic = api.getPhoto(user1.getProfile().profile_photo);
 
 		// Photo profilePic = user1.getProfile().getProfilePhoto();
 		// Loader image - will be shown before loading image
 		int loader = R.drawable.loader;
 		// Imageview to show
 		ImageView image = (ImageView) findViewById(R.id.image);
 		// Image url
 		String image_url = profilePic.path + "/" + profilePic.filename;
 		System.out.println(image_url);
 		// String image_url = "http://api.androidhive.info/images/sample.jpg";
 		// ImageLoader class instance
 		ImageLoader imgLoader = new ImageLoader(getApplicationContext());
 		imgLoader.DisplayImage(image_url, loader, image);
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		loadInfo();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_profile_other, menu);
 		return true;
 	}
 
 	public void loadInfo() {
 		// Using the data from the sharedPreference and updating it to the
 		// server
 		user1 = new User(getIntent().getExtras().getString("username"));
 
 		TextView usernametv = (TextView) findViewById(R.id.username);
 		TextView nicknametv = (TextView) findViewById(R.id.nickname);
 		TextView gendertv = (TextView) findViewById(R.id.gender);
 		TextView biotv = (TextView) findViewById(R.id.aboutme);
 		TextView followerstv = (TextView) findViewById(R.id.followers);
 		TextView followingtv = (TextView) findViewById(R.id.following);
 		TextView datetv = (TextView) findViewById(R.id.profile_creation);
 		TextView phototv = (TextView) findViewById(R.id.photos);
 		TextView bdaytv = (TextView) findViewById(R.id.bday);
 
 		String un = user1.username;
 
		UserProfile user1profile = user1.getProfile();
 		String nickname = user1profile.nickname;
 		String gender = user1profile.gender;
 		String bio = user1profile.bio;
 
 		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");
 		Date birthdate = user1profile.birthday;
 		String bday = "Birthday: " + dateFormatter.format(birthdate);
 
 		Date created = user1profile.getCreatedAt();
 		String prof_created = "Profile Created On:\n"
 				+ dateFormatter.format(created);
 
 		// following
 		User[] following = user1.getFriends();
 		int fCount1 = following.length;
 		String followingCount = fCount1 + " Following";
 
 		// followers
 		User[] follower = user1.getFriendedBy();
 		int fCount2 = follower.length;
 		String followerCount = fCount2 + " Followers";
 
 		Photo[] userphotos = user1.getPhotos();
 		int pCount = userphotos.length;
 		String photoCount = pCount + " Photos";
 
 		usernametv.setText(un);
 		nicknametv.setText(nickname);
 		gendertv.setText(gender);
 		biotv.setText(bio);
 		followingtv.setText(followingCount);
 		followerstv.setText(followerCount);
 		datetv.setText(prof_created);
 		phototv.setText(photoCount);
 		bdaytv.setText(bday);
 	}
 
 	public void loadPics() {
 		User user1 = new User(username);
 		Photo[] userphotos = user1.getPhotos();
 
 		image = new ImageView[userphotos.length];
 		for (int i = userphotos.length - 1; i >= 0; i--) {
 			image[i] = new ImageView(this);
 			image[i].setImageResource(R.drawable.ic_launcher);
 			image[i].setAdjustViewBounds(true);
 			image[i].setOnClickListener(this);
 			LayoutParams lp = new LayoutParams(400, 400);
 			image[i].setLayoutParams(lp);
 			LinearLayout linlay = (LinearLayout) findViewById(R.id.linearPictures);
 			linlay.addView(image[i]);
 
 			// System.out.println(userphotos[i].path + '/' +
 			// userphotos[i].filename);
 			new DownloadImageTask(image[i]).execute(userphotos[i].path + '/'
 					+ userphotos[i].filename);
 		}
 	}
 
 	public void onClick(View view) {
 		User user1 = new User(username);
 		Photo[] userphotos = user1.getPhotos();
 
 		Intent intent = new Intent(this, SinglePicture.class);
 
 		for (int i = userphotos.length - 1; i >= 0; i--) {
 			if (view == image[i]) {
 				String link1 = userphotos[i].path + '/'
 						+ userphotos[i].filename;
 				String cap = userphotos[i].caption;
 				int photoid = userphotos[i].getId();
 				intent.putExtra("link", link1);
 				intent.putExtra("caption", cap);
 				intent.putExtra("photoid", photoid);
 				startActivity(intent);
 			}
 		}
 	}
 
 	public void home(View view) {
 		Intent intent = new Intent(this, HomeScreen.class);
 		startActivity(intent);
 	}
 
 	public void explore(View view) {
 		Intent intent = new Intent(this, Explore.class);
 		startActivity(intent);
 	}
 
 	public void camera(View view) {
 		Intent intent = new Intent(this, Camera.class);
 		startActivity(intent);
 	}
 
 	public void updates(View view) {
 		Intent intent = new Intent(this, Updates.class);
 		startActivity(intent);
 	}
 
 	public void profile(View view) {
 		Intent intent = new Intent(this, Profile.class);
 		startActivity(intent);
 	}
 
 	public void settings(View view) {
 		Intent intent = new Intent(this, PrefsActivity.class);
 		startActivity(intent);
 	}
 
 	public void followers(View view) {
 		Intent intent = new Intent(this, Followers.class);
 		intent.putExtra("username",username);
 		startActivity(intent);
 	}
 
 	public void following(View view) {
 		Intent intent = new Intent(this, Following.class);
 		intent.putExtra("username",username);
 		startActivity(intent);
 	}
 
 	public void follow(View view) {
 		User currentUser = new User(Login.username);
 		User chosenUser = new User(username);
 		currentUser.addFriend(chosenUser);
 
 		finish();
 		startActivity(getIntent());
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
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inSampleSize = 5;
 				InputStream in = new java.net.URL(urldisplay).openStream();
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
