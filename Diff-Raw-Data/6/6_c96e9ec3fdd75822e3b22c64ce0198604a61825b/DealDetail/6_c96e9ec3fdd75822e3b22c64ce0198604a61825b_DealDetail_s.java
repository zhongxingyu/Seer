 package com.citydeals.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.citydeals.Deal;
 import com.citydeals.R;
 import com.citydeals.service.DealService;
 import com.citydeals.util.StringUtil;
 import com.citydeals.util.TimeService;
 import com.facebook.FacebookRequestError;
 import com.facebook.HttpMethod;
 import com.facebook.LoggingBehavior;
 import com.facebook.Request;
 import com.facebook.RequestAsyncTask;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.Settings;
 import com.facebook.widget.LoginButton;
 
 public class DealDetail extends Activity {
 	
 	private Session.StatusCallback statusCallback = new SessionStatusCallback();
     private Button share;
     private LoginButton loginFacebookButton;
     private String headline;
     private String subheadline;
    List<String> PERMISSIONS = new ArrayList<String>();
     Boolean pendingPublishReauthorization;
 
 	protected ProgressBar progressBar;
 	protected Deal d;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Remove title bar
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.detail_page);
 		// get views
 		TextView textViewHeadline = (TextView) findViewById(R.id.textViewHeadline);
 		TextView textViewSubHeadline = (TextView) findViewById(R.id.textViewSubHeadline);
 		TextView textViewDesc = (TextView) findViewById(R.id.textViewDescription);
 		TextView textViewValid = (TextView) findViewById(R.id.textView2);
 
 		// get variable from previous activity
 		Bundle extras = getIntent().getExtras();
 		d = (Deal) extras.getSerializable("deal");
 		String[] splitteHeadline = StringUtil.splitLine(d.getHeadline());
 		headline = splitteHeadline[0];
 		subheadline = splitteHeadline[1];
 		String category = d.getCategory().getCategory();
 
 		if (d.getAvailability() == 0) {
 			showAvailability();
 		} else {
 			showAvailability(d.getClaimeddeals(), d.getAvailability());
 		}
 		
 		textViewDesc.setText(d.getDescription());
 		textViewHeadline.setText(headline);
 		textViewSubHeadline.setText(subheadline);
 
 		String[] str = TimeService.getRemainingTime(d.getValidUntil());
 		textViewValid.setText(str[0] + " T : " + str[1] + " H : " + str[2]
 				+ " M ");
 
 		// get the image of the deal
 		String imgBase64 = DealService.getImageByDeal(d.getId());
 		if (imgBase64 != null) {
 			Bitmap img = d.base64ToBitmap(imgBase64);
 			d.setImage(img);
 		}
 		setImageByCategory(category, d);
 		
 // <--------------------FB related Code ---------------------------------------------->
 		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
         
         Session session = Session.getActiveSession();
         if (session == null) {
             if (savedInstanceState != null) {
                 session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
             }
             if (session == null) {
                 session = new Session(this);
             }
             Session.setActiveSession(session);
             if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                 session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
             }
         }
         
        
         loginFacebookButton = (LoginButton) findViewById(R.id.loginFacebookButton);
         
         share = (Button) findViewById(R.id.button1);
         share.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				publishStory();
 			}
 		});
 
         updateView();
 	}
 
 	private void setImageByCategory(String category, Deal d) {
 
 		ImageView i = (ImageView) findViewById(R.id.imageViewBigPicture);
 
 		if (d.getImage() != null) {
 			i.setImageBitmap(d.getImage());
 		} else if (category.equals("FoodDrink")) {
 			i.setImageResource(R.drawable.food);
 		} else if (category.equals("Shopping")) {
 
 			i.setImageResource(R.drawable.shopping);
 		} else if (category.equals("CarTransport")) {
 
 			i.setImageResource(R.drawable.transport);
 		} else if (category.equals("Service")) {
 
 			i.setImageResource(R.drawable.service);
 		} else if (category.equals("Recreation")) {
 
 			i.setImageResource(R.drawable.recreation);
 		}
 	}
 
 	private void showAvailability(int X, int N) {
 
 		TextView textViewRemainingDeals = (TextView) findViewById(R.id.textViewRemainingDeals);
 		textViewRemainingDeals.setText(String.valueOf(X));
 
 		TextView textViewNumberOfDeals = (TextView) findViewById(R.id.textViewNumberOfDeals);
 		textViewNumberOfDeals.setText(String.valueOf(N));
 
 		progressBar = (ProgressBar) findViewById(R.id.progressBar);
 		double d = (100 / (double) N) * (double) X;
 		int i = (int) d;
 		progressBar.setProgress(i);
 
 	}
 
 	private void showAvailability() {
 
 		TextView textViewAvailability = (TextView) findViewById(R.id.textViewAvailability);
 		// textViewAvailability.setText("Verfgbarkeit: unbegrenzt");
 		textViewAvailability.setVisibility(View.GONE);
 		TextView textViewRemainingDeals = (TextView) findViewById(R.id.textViewRemainingDeals);
 		textViewRemainingDeals.setVisibility(View.GONE);
 		TextView textViewAvailabilityOf = (TextView) findViewById(R.id.textViewAvailabilityOf);
 		textViewAvailabilityOf.setVisibility(View.GONE);
 		TextView textViewNumberOfDeals = (TextView) findViewById(R.id.textViewNumberOfDeals);
 		textViewNumberOfDeals.setVisibility(View.GONE);
 
 		progressBar = (ProgressBar) findViewById(R.id.progressBar);
 		progressBar.setVisibility(View.GONE);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.deal_detail, menu);
 		return true;
 	}
 	
 	// <--------------------FB related Code ---------------------------------------------->
 	
     @Override
     public void onStart() {
         super.onStart();
         Session.getActiveSession().addCallback(statusCallback);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         Session.getActiveSession().removeCallback(statusCallback);
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         Session session = Session.getActiveSession();
         Session.saveSession(session, outState);
     }
 
     private void updateView() {
         Session session = Session.getActiveSession();
         if (session.isOpened()) {
         	share.setVisibility(View.VISIBLE);
         	loginFacebookButton.setVisibility(View.INVISIBLE);
         } else {
         	share.setVisibility(View.INVISIBLE);
         	loginFacebookButton.setVisibility(View.VISIBLE);
         }
     }
 
     private void publishStory() {
         Session session = Session.getActiveSession();
 
         if (session != null){
 
             // Check for publish permissions    
             List<String> permissions = session.getPermissions();
             if (!isSubsetOf(PERMISSIONS, permissions)) {
                 pendingPublishReauthorization = true;
                 Session.NewPermissionsRequest newPermissionsRequest = new Session
                         .NewPermissionsRequest(this, PERMISSIONS);
             session.requestNewPublishPermissions(newPermissionsRequest);
                 return;
             }
 
             Bundle postParams = new Bundle();
             postParams.putString("name", headline);
             postParams.putString("caption", subheadline);
             postParams.putString("description", d.getDescription());
             postParams.putString("link", "https://developers.facebook.com/android");
             postParams.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
 
 
             Request.Callback callback= new Request.Callback() {
                 public void onCompleted(Response response) {
                     JSONObject graphResponse = response
                                                .getGraphObject()
                                                .getInnerJSONObject();
                     String postId = null;
                     try {
                         postId = graphResponse.getString("id");
                     } catch (JSONException e) {
                         Log.i("Error",
                             "JSON error "+ e.getMessage());
                     }
                     FacebookRequestError error = response.getError();
                     if (error != null) {
                         Toast.makeText(getApplicationContext(),
                              error.getErrorMessage(),
                              Toast.LENGTH_SHORT).show();
                         } else {
                             Toast.makeText(getApplicationContext(), 
                                  "Der Deal wurde erfolgreich auf Facebook geteilt.",
                                  Toast.LENGTH_LONG).show();
                     }
                 }
             };
 
             Request request = new Request(session, "me/feed", postParams, 
                                   HttpMethod.POST, callback);
 
             RequestAsyncTask task = new RequestAsyncTask(request);
             task.execute();
         }
 
     }
     
     private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
         for (String string : subset) {
             if (!superset.contains(string)) {
                 return false;
             }
         }
         return true;
     }
 	
 
     private class SessionStatusCallback implements Session.StatusCallback {
         @Override
         public void call(Session session, SessionState state, Exception exception) {
             updateView();
         }
     }
 
 }
