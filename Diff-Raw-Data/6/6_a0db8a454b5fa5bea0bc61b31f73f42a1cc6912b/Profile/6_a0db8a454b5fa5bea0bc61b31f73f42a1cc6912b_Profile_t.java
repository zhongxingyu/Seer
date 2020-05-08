 package com.tuit.ar.activities;
 
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.tuit.ar.R;
 import com.tuit.ar.api.Avatar;
 import com.tuit.ar.api.AvatarObserver;
 import com.tuit.ar.api.Twitter;
 import com.tuit.ar.api.TwitterAccount;
 import com.tuit.ar.api.TwitterAccountRequestsObserver;
 import com.tuit.ar.api.TwitterRequest;
 import com.tuit.ar.api.request.Options;
 import com.tuit.ar.models.User;
 
 public class Profile extends Activity implements AvatarObserver, TwitterAccountRequestsObserver {
 	protected static final int MENU_VIEW_TIMELINE = 0;
 
 	private ImageView avatar;
 	private TextView nickname;
 	private TextView fullname;
 	private TextView description;
 	private Button following;
 	private TextView followingNumber;
 	private TextView followerNumber;
 	private TextView tweetsNumber;
 	private TextView location;
 	private TextView locationLabel;
 	private Button seeInMap;
 	private Button sendDm;
 	private Button url;
 
 	private User user = null;
 
 	private ProgressDialog loading = null;
 	static private User userToDisplay = null;
 
 	static public void setUserToDisplay(User user) {
 		userToDisplay = user;
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.profile);
 		Twitter.getInstance().getDefaultAccount().addRequestObserver(this);
 		user = userToDisplay;
 		
 		userToDisplay = null;
 
 		avatar = (ImageView)findViewById(R.id.avatar);
 		avatar.setVisibility(View.INVISIBLE);
 		nickname = (TextView)findViewById(R.id.nickname);
 		fullname = (TextView)findViewById(R.id.fullname);
 		following = (Button)findViewById(R.id.follow);
 		following.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				following();
 			}
 		});
 		description = (TextView)findViewById(R.id.description);
 		followingNumber = (TextView)findViewById(R.id.following_number);
 		followerNumber = (TextView)findViewById(R.id.follower_number);
 		tweetsNumber = (TextView)findViewById(R.id.tweets_number);
 		locationLabel = (TextView)findViewById(R.id.location_label);
 		location = (TextView)findViewById(R.id.location);
 
 		url = (Button)findViewById(R.id.url);
 		url.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(Intent.ACTION_VIEW , Uri.parse(user.getUrl())));
 			}
 		});
 
 		sendDm = (Button)findViewById(R.id.send_dm);
 		sendDm.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				Intent intent = new Intent(getApplicationContext(), NewDirectMessage.class);
 				intent.putExtra("to_user", user.getScreenName());
 				startActivity(intent);
 			}
 		});
 		seeInMap = (Button)findViewById(R.id.see_in_map);
 		seeInMap.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse("geo:" + user.getLocation()));
 				try {
 					startActivity(intent);
 				} catch (Exception e) {
 					Toast.makeText(Profile.this, getString(R.string.unableToShowMap), Toast.LENGTH_SHORT).show();
 					e.printStackTrace();
 				}
 			}
 		});
 
 		if (user == null) {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("screen_name", getIntent().getStringExtra("screen_name")));
 			try {
 				loading  = new ProgressDialog(this);
 				loading.setTitle(R.string.loading);
 				loading.show();
				Twitter.getInstance().getDefaultAccount().requestUrl(Options.USER_PROFILE, params, TwitterRequest.METHOD_GET);
 			} catch (Exception e) {
 				fetchUserFailed();
 			}
 		} else {
 			setUser();
 		}
 	}
 
 	private void fetchUserFailed() {
 		Toast.makeText(this, getString(R.string.unableToShowUser), Toast.LENGTH_SHORT).show();
 		finish();
 	}
 
 	private void setUser() {
 		if (user != null) {
 			if (loading != null) {
 				loading.hide();
 				loading = null;
 			}
 			showFollowing();
 			Avatar avatar = Avatar.get(user.getProfileImageUrl());
 			avatar.addRequestObserver(this);
 			avatar.download();
 			
 			nickname.setText(user.getScreenName());
 			fullname.setText(user.getName());
 			description.setText(user.getDescription());
 			followingNumber.setText(String.valueOf(user.getFriendsCount()));
 			followerNumber.setText(String.valueOf(user.getFollowersCount()));
 			tweetsNumber.setText(String.valueOf(user.getStatusesCount()));
 			String _location = user.getLocation();
 			if (_location == null || _location.length() == 0) {
 				// FIXME: it's ugly and makes the layout not-reausable... View.GONE breaks everything
 				// View.INVISIBLE lefts the space empty
 				location.setHeight(0);
 				locationLabel.setHeight(0);
 			} else {
 				location.setText(_location);
 				location.setVisibility(View.VISIBLE);
 				locationLabel.setVisibility(View.VISIBLE);
 			}
 			seeInMap.setVisibility(user.getLocation() == null ? View.INVISIBLE : View.VISIBLE);
 			String _url = user.getUrl();
 			boolean hasUrl = _url != null && _url.length() > 0;
 			url.setVisibility(hasUrl ? View.VISIBLE : View.INVISIBLE);
 			if (hasUrl) url.setText(_url);
 		}
 	}
 
 	public void avatarHasFailed(Avatar avatar) {
 		this.avatar.setVisibility(View.INVISIBLE);
 		avatar.removeRequestObserver(this);
 	}
 
 	public void avatarHasFinished(Avatar avatar) {
 		this.avatar.setImageBitmap(avatar.getResponse());
 		this.avatar.setVisibility(View.VISIBLE);
 		avatar.removeRequestObserver(this);
 	}
 
 	protected void following() {
         new AlertDialog.Builder(this)
         .setMessage(user.isFollowing() ? R.string.confirmUnfollow : R.string.confirmFollow)
         .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
             	try {
             		if (user.isFollowing()) user.stopFollowing();
             		else user.follow();
             	} catch (Exception e) {
             		Toast.makeText(Profile.this, getString(R.string.unableToFollow), Toast.LENGTH_SHORT).show();
             	}
             }
         })
         .setNegativeButton(R.string.no, null)
         .show();
 	}
 
 	public void requestHasFinished(TwitterRequest request) {
 		if ((request.getUrl().equals(Options.FOLLOW)) || (request.getUrl().equals(Options.UNFOLLOW))) {
 			user.requestHasFinished(request);
 			showFollowing();
 		} else if (request.getUrl().equals(Options.USER_PROFILE)) {
 			try {
 				user = new User(new JSONObject(new JSONTokener(request.getResponse())));
 				setUser();
 			} catch (JSONException e) {
 				fetchUserFailed();
 			}
 		}
 	}
 
 	private void showFollowing() {
 		if (user == null) {
 			following.setVisibility(View.GONE);
 		} else {
 			following.setVisibility(Twitter.getInstance().getDefaultAccount().getUser().getId() == user.getId() ? View.GONE : View.VISIBLE);
 			following.setText(getString(user.isFollowing() ? R.string.following : R.string.notFollowing));
 		}
 	}
 
 	public void requestHasStarted(TwitterRequest request) {
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_VIEW_TIMELINE, 0, R.string.viewTimeline);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {  
 	    switch (item.getItemId()) {  
 	    case MENU_VIEW_TIMELINE:
 	    {
 	    	com.tuit.ar.activities.timeline.User.setUser(user);
 	        Intent intent = new Intent(this.getApplicationContext(), com.tuit.ar.activities.timeline.User.class);
 	        this.startActivity(intent);
 	        return true;
 	    }
 	    }
 	    return false;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
		Twitter.getInstance().getDefaultAccount().removeRequestObserver(this);
 		following.setOnClickListener(null);
 		url.setOnClickListener(null);
 		sendDm.setOnClickListener(null);
 		seeInMap.setOnClickListener(null);
 	}
 }
