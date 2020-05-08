 package com.strollimo.android;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.google.zxing.config.ZXingLibConfig;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import com.strollimo.android.dialog.TreasureFoundDialog;
 import com.strollimo.android.dialog.TreasureNotFoundDialog;
 
 public class DetailsActivity extends Activity {
     public static final String PLACE_ID_EXTRA = "place_id";
     private ZXingLibConfig zxingLibConfig;
     private PlacesService mPlacesService;
     private UserService mUserService;
     private TextView mStatusTextView;
     private TextView mTitleTextView;
     private Place mCurrentPlace;
     private Button mCaptureButton;
     private ImageView mStatusImageView;
     private View.OnClickListener onCaptureButtonClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             IntentIntegrator integrator = new IntentIntegrator(DetailsActivity.this);
             integrator.initiateScan();
         }
     };
     private ImageView mDetailsPhoto;
 
     public static Intent createDetailsIntent(Context context, int placeId) {
         Intent intent = new Intent(context, DetailsActivity.class);
         intent.putExtra(PLACE_ID_EXTRA, placeId);
         return intent;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         mPlacesService = ((StrollimoApplication) getApplication()).getService(PlacesService.class);
         mUserService = ((StrollimoApplication) getApplication()).getService(UserService.class);
         zxingLibConfig = new ZXingLibConfig();
         zxingLibConfig.useFrontLight = true;
 
         setContentView(R.layout.details_screen);
         mStatusTextView = (TextView) findViewById(R.id.status);
         mStatusImageView = (ImageView) findViewById(R.id.status_icon);
         mTitleTextView = (TextView) findViewById(R.id.title);
         mCaptureButton = (Button) findViewById(R.id.capture_button);
         mCaptureButton.setOnClickListener(onCaptureButtonClickListener);
 
         mCurrentPlace = mPlacesService.getPlaceById(getIntent().getIntExtra(PLACE_ID_EXTRA, 0));
         mTitleTextView.setText(mCurrentPlace == null ? "Error" : mCurrentPlace.mTitle.toUpperCase());
         mDetailsPhoto = (ImageView)findViewById(R.id.detailed_photo);
         mDetailsPhoto.setImageBitmap(mCurrentPlace.getBitmap());
         mDetailsPhoto.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View view, MotionEvent motionEvent) {
                 if (motionEvent.getPointerCount() >= 3) {
                     mUserService.reset();
                 }
                Intent intent = new Intent(DetailsActivity.this, StrollMapActivity.class);
                startActivity(intent);
                return true;
             }
         });
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (mUserService.isPlaceCaptured(mCurrentPlace.mId)) {
             mStatusTextView.setText("Opened");
             mStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.open_padlock));
             mCaptureButton.setVisibility(View.GONE);
 
         } else {
             mStatusTextView.setText("Closed");
             mStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_padlock));
             mCaptureButton.setVisibility(View.VISIBLE);
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
             case IntentIntegrator.REQUEST_CODE:
                 IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
                         resultCode, data);
                 if (scanResult == null) {
                     return;
                 }
                 final String result = scanResult.getContents();
                 Log.i("BB", "" + result);
                 if (mCurrentPlace.isScannedCodeValid(result)) {
                     boolean levelUp = mUserService.capturePlace(mCurrentPlace);
                     int placesFound = mUserService.getFoundPlacesNum();
                     int placesCount = mPlacesService.getPlacesCount();
                     int coinValue = mCurrentPlace.mCoinValue;
                     String levelText = levelUp ? mUserService.getCurrentLevel() : mUserService.getNextLevel();
                     TreasureFoundDialog dialog = new TreasureFoundDialog(placesFound, placesCount, coinValue, levelUp, levelText);
                     dialog.show(getFragmentManager(), "dialog");
                 } else {
                     new TreasureNotFoundDialog().show(getFragmentManager(), "dialog");
                 }
 
                 break;
             default:
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 onBackPressed();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
