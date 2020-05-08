 package com.chaseit.activities;
 
 import java.io.ByteArrayOutputStream;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.chaseit.ParseHelper;
 import com.chaseit.R;
 import com.chaseit.fragments.AddPictureFragment;
 import com.chaseit.fragments.AddPictureFragment.AddPictureListener;
 import com.chaseit.models.Hunt;
 import com.chaseit.models.Location;
 import com.chaseit.util.Constants;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseFile;
 import com.parse.ParseGeoPoint;
 import com.parse.SaveCallback;
 
 public class AddChaseLocation extends FragmentActivity implements
 AddPictureListener {
 	public static final int ADD_CHASE_LOCATION_ACTIVITY_CODE = 100;
 	private EditText etChaseHint;
 	private EditText etPlace;
 	private TextView tvLatValue;
 	private TextView tvLongValue;
 	private Button btnCancel;
 	private Button btnAdd;
 
 	private String friendlyName;
 	private ParseFile photoHint;
 
 	private int locationIndex;
 	private Hunt chase;
 	private ParseGeoPoint point;
 
 	//private ProgressDialog pDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_add_chase_location);
 		etChaseHint = (EditText) findViewById(R.id.etChaseHint);
 		etPlace = (EditText) findViewById(R.id.etPlace);
 		tvLatValue = (TextView) findViewById(R.id.tvLatValue);
 		tvLongValue = (TextView) findViewById(R.id.tvLongValue);
 		btnCancel = (Button) findViewById(R.id.btnCancel);
 		btnAdd = (Button) findViewById(R.id.btnAdd);
 		Intent i = getIntent();
 		String chaseId = i.getStringExtra("chaseId");
 		locationIndex = i.getIntExtra("locationIndex", 0);
 		friendlyName = i.getStringExtra("friendlyName");
 		point = new ParseGeoPoint(i.getDoubleExtra("latitude",
 				Constants.latUnionSquare), i.getDoubleExtra("longitude",
 						Constants.lngUnionSquare));
 
 		tvLatValue.setText(String.valueOf(point.getLatitude()));
 		tvLongValue.setText(String.valueOf(point.getLongitude()));
 		etPlace.setText(friendlyName);
 
 		ParseHelper.getHuntByObjectId(chaseId, new GetCallback<Hunt>() {
 			@Override
 			public void done(Hunt object, ParseException e) {
 				if (e == null) {
 					chase = object;
 				} else {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		btnCancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				setResult(RESULT_CANCELED);
 				finish();
 			}
 		});
 
 		btnAdd.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 //				pDialog = new ProgressDialog(getBaseContext());
 //				pDialog.setMessage("Adding Location to Your Chase...");
 //				pDialog.setCancelable(false);
 //				pDialog.show();
 				
 				//dont proceed if no hint
 				if(photoHint == null && StringUtils.isBlank(etChaseHint.getText().toString())){
 					showError();
 					return;
 				}
 				
 				String newFriendlyName = etPlace.getText().toString();
 				friendlyName = StringUtils.isNotBlank(newFriendlyName) ? newFriendlyName : friendlyName;
 				Location location = new Location();
 				location.setHint(etChaseHint.getText().toString());
 				location.setImage(photoHint);
 				location.setIndexInHunt(locationIndex);
 				location.setLocation(point);
 				location.setLocationName(friendlyName);
 				location.setParentHunt(chase);
 				if (photoHint != null) {
 					photoHint.saveInBackground();
 				}
 				location.saveInBackground(new SaveCallback() {
 					@Override
 					public void done(ParseException e) {
 //						pDialog.dismiss();
 						if (e == null) {
 							Intent resultIntent = new Intent();
 							resultIntent.putExtra("friendlyName", friendlyName);
 							setResult(RESULT_OK, resultIntent);
 							finish();
 						} else {
 							e.printStackTrace();
 						}
 					}
 				});
 			}
 		});
 	}
 
 	private void showError(){
 //		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
 //		findViewById(android.R.id.content).getRootView().startAnimation(shake);
 		Toast.makeText(getBaseContext(), "Please add a picture or message for hint", Toast.LENGTH_SHORT).show();
 	}	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.add_chase_location, menu);
 		return true;
 	}
 
 	@Override
 	public void onPictureAdded(Fragment fragment) {
 		Bitmap bmp = ((AddPictureFragment) fragment).getPhotoBitmap();
 		if (bmp != null) {
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 			byte[] scaledData = bos.toByteArray();
 			photoHint = new ParseFile(
 					((AddPictureFragment) fragment).getPhotoName(), scaledData);
 		}
 	}
 
 }
