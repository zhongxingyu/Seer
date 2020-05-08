 package com.android.Oasis.diary;
 
 import java.io.FileNotFoundException;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.android.Oasis.LoginButton;
 import com.android.Oasis.MySQLite;
 import com.android.Oasis.R;
 import com.android.Oasis.SessionEvents;
 import com.android.Oasis.SessionEvents.AuthListener;
 import com.android.Oasis.SessionEvents.LogoutListener;
 import com.android.Oasis.SessionStore;
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.Facebook;
 
 public class BrowseDiary extends Activity {
 	
 	public static final String APP_ID = "285141848231182";
 	private Facebook mFacebook;
 	private AsyncFacebookRunner mAsyncRunner;
 	private LoginButton mLoginButton;
 	
 	Bitmap img = null;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.browsediary);
 		
 		mLoginButton = (LoginButton) findViewById(R.id.login);
 		mLoginButton.setImageResource(R.drawable.diary_btn_share);
 		
 		mFacebook = new Facebook(APP_ID);
 	   	mAsyncRunner = new AsyncFacebookRunner(mFacebook);
 	   	
 	   	SessionStore.restore(mFacebook, this);
 		SessionEvents.addAuthListener(new AuthListener(){
 
 			@Override
 			public void onAuthSucceed() {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onAuthFail(String error) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		SessionEvents.addLogoutListener(new LogoutListener(){
 
 			@Override
 			public void onLogoutBegin() {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onLogoutFinish() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		mLoginButton.init(this, mFacebook, 2);
 		
 		Bundle bundle;
 		bundle = this.getIntent().getExtras();
 		boolean isMine = bundle.getBoolean("ismine");
 		Uri path = Uri.parse(bundle.getString("path"));
 		final int id = bundle.getInt("db_id");
 		ImageView myImageView = (ImageView)findViewById(R.id.browsediary_img);
 		LinearLayout ll = (LinearLayout)findViewById(R.id.browsediary_ll);
 		
 		ll.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				BrowseDiary.this.finish();
 				System.gc();
 			}
 			
 		});
 		
 		ContentResolver vContentResolver = getContentResolver();
 		try {
 			img = BitmapFactory.decodeStream(vContentResolver
 					.openInputStream(path));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		myImageView.setAdjustViewBounds(true);
 		myImageView.setImageBitmap(img);
 		//img.recycle();
 		
 		LinearLayout ll_mine = (LinearLayout)findViewById(R.id.browsediary_ll_mine);
 		LinearLayout ll_others = (LinearLayout)findViewById(R.id.browsediary_ll_others);
 		
 		ImageButton btn_delete = (ImageButton)findViewById(R.id.diary_btn_delete);
 		btn_delete.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				MySQLite db = new MySQLite(BrowseDiary.this);
 				db.delete(id);
 				db.close();
 				BrowseDiary.this.finish();
 			}
 		});
 		
 		if(isMine==true){
 			ll_mine.setVisibility(View.VISIBLE);
 			ll_others.setVisibility(View.GONE);
 		}
 		else{
 			ll_mine.setVisibility(View.GONE);
 			ll_others.setVisibility(View.VISIBLE);
 		}
 
 	}
 	
 	public void sendPost()
 	{
 		Log.e("BrowseDiary:sendPost", "Begin");
 		postToWall();
 		System.gc();
 		Log.e("BrowseDiary:sendPost", "Before finish.");
 		BrowseDiary.this.finish();
 	}
 	
 	
 	private void postToWall() {
 
 		final Handler handler = new Handler() {
 			public void handleMessage(Message what) {
 				Log.e("BrowseDiary:postToWall", "Before finish.");
 				finish();
 			}
 		};
 		Thread thread = new Thread() {
 			public void run() {
 				(new DiaryPoster("BrowseDiary", mFacebook)).publishToWall(img, "");
 				handler.sendEmptyMessage(0);
 			}
 		};
 		thread.start();
 
 	}
 	
 }
