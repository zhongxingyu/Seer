 /*******************************************************************************
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @contributor(s): Freerider Team 2 (Group 3, IT2901 Spring 2013, NTNU)
  * @version: 2.0
  * 
  * Copyright 2013 Freerider Team 2
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.socialhitchhiking.facebook;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.UserRequest;
 import no.ntnu.idi.freerider.protocol.UserResponse;
 import no.ntnu.idi.socialhitchhiking.Main;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.utility.SocialHitchhikingActivity;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.webkit.CookieSyncManager;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.facebook.android.Util;
 
 /**
  * 
  * Class used to connect and log on to Facebook.
  * @extends SocialHitchhikingActivity
  */
 public abstract class FBConnectionActivity extends SocialHitchhikingActivity{
 	public static final String TAG = "FACEBOOK";
 	private Facebook mFacebook;
 	private User user;	
 	private boolean newUserBoolean = false;
 	private static final int RELOGIN = 592824052;
 	public static final String APP_ID = "321654017885450";
 	private AsyncFacebookRunner mAsyncRunner;
 	private static final String[] PERMS = new String[] { "read_stream" , "publish_stream"};
 	private SharedPreferences sharedPrefs;
 	private Context mContext;
 	protected Runnable fbc;
 	private Main main;
 	protected ArrayList<User> friends = new ArrayList<User>();
 	protected TextView username,txtfriend;
 	protected ProgressBar pbLogin;
 	protected ListView listView;
 	private int failCounter;
 
 	/**
 	 * 
 	 * Handler to handle messages sent by independent threads called by
 	 * runOnUIThread() in a Listener class
 	 */
 	private Handler handler = new Handler(){
 		public void handleMessage(Message msg) {
 			if(msg.obj instanceof User){
 				final Message temp = msg;
 				Thread t = new Thread() {
 					public void run() {
 						initiateUser(temp);
 					}
 				};
 				t.start();
 				try {
 					t.join();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					Log.e("Interrupted","Exc");
 				}
 				
 			}
 			else if(msg.what == failCounter){
 				main.createCantConnectDialog("Can't connect to Facebook!\nAre you sure you're connected?", "Retry");
 			}
 			else if(msg.what == RELOGIN){
 				getApp().fireAccesTokenChanged();
 			}
 		}
 	};
 
 	/**
 	 * Initiates the Facebook connection
 	 */
 	public void setConnection(Main m) {
 		main = m;
 		getApp().setMain(m);
 		mContext = getApp();
 		friends = new ArrayList<User>();
 		mFacebook = new Facebook(APP_ID);
 		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
 		failCounter = 0;
 	}
 
 	/**
 	 * Log out of Facebook
 	 */
 	public void logOut(Main m){
		setConnection(m);
 		mAsyncRunner.logout(mContext, new LogoutRequestListener());
 	}
 
 	private void retry(){
 		if(failCounter < 15){
 			failCounter++;
 			getID();
 		}
 		else{
 			failCounter = 0;
 			handler.sendEmptyMessage(failCounter);
 		}
 	}
 	/**
 	 * Checks if the current Facebook session is valid, and tries to relogin if it's not.
 	 * 
 	 * 
 	 * @param progbar - A ProgressBar to indicate if your still trying to login to Facebook
 	 * @param m - Main, a pointer to the Main activity which started the application
 	 */
 	public void getID() {
 		try{
 			if (isSession()) {
 				mAsyncRunner.request("me", new IDRequestListener());
 			} else {
 				// not logged in, so relogin
 				mFacebook.authorize(this, PERMS, new NewLoginDialogListener());
 			}
 		}
 		catch(NullPointerException e){
 			mFacebook.authorize(this, PERMS, new LoginDialogListener());
 		}
 	}
 		
 	public boolean getIdBoolean(){
 		try{
 			if (isSession()) {
 				mAsyncRunner.request("me", new IDRequestListener());
 			} else {
 				// not logged in, so relogin
 				mFacebook.authorize(this, PERMS, new NewLoginDialogListener());
 				return true;
 			}
 		}
 		catch(NullPointerException e){
 			mFacebook.authorize(this, PERMS, new NewLoginDialogListener());
 		}
 		return false;
 
 
 	}
 	public void getAccess() {
 		mFacebook.authorize(this, PERMS, new ReloginDialogListener());
 	}
 	public User getUser(){
 		return user;
 	}
 
 
 	/**
 	 * Initializes the Facebook user.
 	 * 
 	 * @param msg - A Message handled by the handler, consists of a {@link User}
 	 */
 	private void initiateUser(Message msg) {
 		user = (User)msg.obj;
 		user.setPicture(getPictureByteArray(user.getID()));
 		getApp().setUser(user);
 		//CookieSyncManager syncManager = 
 		CookieSyncManager.createInstance(this);
         //syncManager.sync();
 		main.onResult();
 	}
 	private void deleteSession(){
 		sharedPrefs.edit().remove("access_token").commit();
 		sharedPrefs.edit().remove("access_expires").commit();
 		try{
 			android.webkit.CookieManager.getInstance().removeSessionCookie();
 		}catch(Exception e){
 			
 		}
 		mFacebook.setAccessToken(null);
 		mFacebook.setAccessExpires(-1);
 		/*Intent intent = new Intent(mContext, no.ntnu.idi.socialhitchhiking.Main.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		mContext.startActivity(intent);*/
 		mFacebook.authorize(this, PERMS, Facebook.FORCE_DIALOG_AUTH, new NewLoginDialogListener());
 	}
 	protected void resetSession(){
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				mFacebook.authorize(FBConnectionActivity.this, PERMS, new ResetConnectionListener());
 			}
 		});
 	}
 	protected boolean isSession() {
 
 		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 		String access_token = sharedPrefs.getString("access_token", null);
 		Long expires = sharedPrefs.getLong("access_expires", -1);
 		System.out.println("Sesjonsvariabel hentes!");
 		if (access_token != null && expires != -1) {
 			System.out.println("Sesjonsvariabel opprettes!");
 			mFacebook.setAccessToken(access_token);
 			mFacebook.setAccessExpires(expires);
 		}
 		System.out.println("Sesjonsvariabel valid: " + mFacebook.isSessionValid());
 		return mFacebook.isSessionValid();
 
 	}
 	/**
 	 * Listener for when a Logout request is finished
 	 *
 	 */
 	private class LogoutRequestListener implements RequestListener {
 		@Override
 		public void onComplete(String response, final Object state) {
 			/*
 			 * callback should be run in the original thread, not the background
 			 * thread
 			 */
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					if(mFacebook != null){
 						if (isSession()){
 							deleteSession();
 						}
 						else{
 							mFacebook.authorize(FBConnectionActivity.this, PERMS, Facebook.FORCE_DIALOG_AUTH, new NewLoginDialogListener());
 						}
 					}
 				}
 			});
 		}
 		@Override
 		public void onIOException(IOException e, Object state) {
 		}
 		@Override
 		public void onFileNotFoundException(FileNotFoundException e,Object state) {
 		}
 		@Override
 		public void onMalformedURLException(MalformedURLException e,Object state) {
 		}
 		@Override
 		public void onFacebookError(FacebookError e, Object state) {
 		}
 	}
 
 	/**
 	 * Listener for when a Connection must be reset
 	 */
 	private class ResetConnectionListener implements DialogListener {
 		public void onComplete(Bundle values) {
 			String token = mFacebook.getAccessToken();
 			long token_expires = mFacebook.getAccessExpires();
 			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 			sharedPrefs.edit().putLong("access_expires", token_expires).commit();
 			sharedPrefs.edit().putString("access_token", token).commit();
 		}
 		public void onFacebookError(FacebookError e) {
 		}
 		public void onError(DialogError e) {
 		}
 		public void onCancel() {
 		}
 	}//end of private class ResetConnectionListener
 
 
 	/**
 	 * Listener for when a Login request is finished
 	 */
 	private class LoginDialogListener implements DialogListener {
 
 
 		public void onComplete(Bundle values) {
 			System.out.println("LoginDialog starter");
 			String token = mFacebook.getAccessToken();
 
 			long token_expires = mFacebook.getAccessExpires();
 
 			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 
 			sharedPrefs.edit().putLong("access_expires", token_expires).commit();
 
 			sharedPrefs.edit().putString("access_token", token).commit();
 
 			mAsyncRunner.request("me", (RequestListener) new IDRequestListener());
 
 		}
 		public void onFacebookError(FacebookError e) {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 		public void onError(DialogError e) {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 		public void onCancel() {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 	}//end of private class LoginDialogListener
 	/**
 	 * Listener for when a Login request is finished
 	 *
 	 */
 	private class NewLoginDialogListener implements DialogListener {
 
 
 		public void onComplete(Bundle values) {
 			System.out.println("NewLoginDIalog starter");
 			String token = mFacebook.getAccessToken();
 			long token_expires = mFacebook.getAccessExpires();
 			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 			sharedPrefs.edit().putLong("access_expires", token_expires).commit();
 			sharedPrefs.edit().putString("access_token", token).commit();
 			mAsyncRunner.request("me", (RequestListener) new IDRequestListener());
 		}
 		public void onFacebookError(FacebookError e) {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 		public void onError(DialogError e) {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 		public void onCancel() {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 	}//end of private class LoginDialogListener
 	/**
 	 * Listener for when a Login request is finished
 	 *
 	 */
 	private class ReloginDialogListener implements DialogListener {
 
 
 		public void onComplete(Bundle values) {
 			String token = mFacebook.getAccessToken();
 			long token_expires = mFacebook.getAccessExpires();
 			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 			sharedPrefs.edit().putLong("access_expires", token_expires).commit();
 			sharedPrefs.edit().putString("access_token", token).commit();
 			Message msg = new Message();
 			msg.what = RELOGIN;
 			msg.obj = token;
 			handler.sendMessage(msg);
 		}
 		public void onFacebookError(FacebookError e) {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 		public void onError(DialogError e) {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 		public void onCancel() {
 			main.createLoginFailedDialog(false,"Login failed","Retry");
 		}
 	}//end of private class LoginDialogListener
 
 	/**
 	 * Listener for when an ID request is finished
 	 *
 	 */
 	private class IDRequestListener implements RequestListener {
 
 		public void onComplete(final String response, Object state) {
 			try {
 				JSONObject json = Util.parseJson(response);
 				final String id = json.getString("id");
 				final String firstName = json.getString("first_name");
 				final String surName = json.getString("last_name");
 				final String gender = json.getString("gender");
 
 				FBConnectionActivity.this.runOnUiThread(fbc = new Runnable() {
 					public void run() {
 						User login = new User(firstName,id);
 						login.setSurname(surName);
 						// Setting gender
 						if(gender.equals("male")){
 							login.setGender("m");
 						}
 						else if(gender.equals("female")){
 							login.setGender("f");
 						}
 						//username.setText("Welcome: " + name+"\n ID: "+id);
 						Message msg = new Message();
 						
 						if(newUser(login.getID())){
 							System.out.println("Brukeren er NY!");
 							//new UserRequest(RequestType.CREATE_USER, login);
 							newUserBoolean = true;
 							Log.e("I connection", Boolean.toString(newUserBoolean));
 						}
 						msg.obj = login;
 						handler.sendMessage(msg);
 					}
 
 				});
 			} catch (JSONException e) {
 				retry();
 			} catch (FacebookError e) {
 				retry();
 			}
 		}
 		
 		public void onIOException(IOException e, Object state) {
 			retry();
 		}
 		@Override
 		public void onFileNotFoundException(FileNotFoundException e,Object state) {
 			retry();
 		}
 		@Override
 		public void onMalformedURLException(MalformedURLException e,Object state) {
 			retry();
 		}
 		@Override
 		public void onFacebookError(FacebookError e, Object state) {
 			retry();
 		}
 
 	}//end of private class IDRequestListener
 	
 	/**
 	 * Static method that retrieves a users Facebook profile picture.
 	 * 
 	 * @param id - String, containing a Facebook users id.
 	 * @return {@link Bitmap} of the users profile picture.
 	 */
 	public static Bitmap getPicture(String id){
 		URL img_value = null;
 		Bitmap mIcon1 = null;
 		try {
 			img_value = new URL("http://graph.facebook.com/"+id+"/picture?type=normal");
 			mIcon1 = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return mIcon1;
 	}
 
 	/**
 	 * TODO: BUG; Near bmp.compress()
 	 * @param id
 	 * @return
 	 */
 	public static byte[] getPictureByteArray(String id){
 		Bitmap bmp = getPicture(id);
 
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		try {
 			bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
 		} catch (Exception e) {
 			return new byte[0];
 		}
 		byte[] byteArray = stream.toByteArray();
 
 		return byteArray;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		mFacebook.authorizeCallback(requestCode, resultCode, data);
 	}
 	
 	/*
 	 * Method for checking if user has previously logged in
 	 */
 	private boolean newUser(String id){
 		User user = new User("Dummy",id); //"Dummy" and 0.0 are dummy vars. getApp() etc sends the current user's carid
 		Request req = new UserRequest(RequestType.GET_USER, user);
 		UserResponse res = null;
 		try {
 			res = (UserResponse) RequestTask.sendRequest(req,getApp());
 			System.out.println("Error melding: " + res.getErrorMessage());
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Excecution feil: " + e.getMessage());
 			e.printStackTrace();
 		}
 		if(res==null){
 			return true;
 		}else
 			return false;
 	}
 	
 	public boolean checkNewUser(){
 		return newUserBoolean;
 	}
 }
