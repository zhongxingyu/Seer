 package com.example.moodstocks;
 
 import tw.edu.ntust.dt.herbal2.DataHelper;
 import tw.edu.ntust.dt.herbal2.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.moodstocks.android.MoodstocksError;
 import com.moodstocks.android.Result;
 import com.moodstocks.android.ScannerSession;
 
 public class ScanActivity extends Activity implements ScannerSession.Listener {
 
 	private int ScanOptions = Result.Type.IMAGE | Result.Type.EAN8
 			| Result.Type.EAN13 | Result.Type.QRCODE | Result.Type.DATAMATRIX;
 
 	private ScannerSession session;
 	private TextView resultTextView;
 
 	private ImageButton closeButton;
 	private ImageView discoverGoal;
 
 	private Intent nextActivity;
 	private Intent nextNextActivity;
 
 	private int discoverGoalResId;
 	private String discoverGoalString;
 	private String discoevrGoalChineseString;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_scan);
 
 		// get the camera preview surface & result text view
 		SurfaceView preview = (SurfaceView) findViewById(R.id.preview);
 
 		// Create a scanner session
 		try {
 			session = new ScannerSession(this, this, preview);
 		} catch (MoodstocksError e) {
 			e.log();
 		}
 
 		// set session options
 		session.setOptions(ScanOptions);
 
 		resultTextView = (TextView) findViewById(R.id.scan_result);
 		resultTextView.setText("Scan result: N/A");
 
 		discoverGoal = (ImageView) findViewById(R.id.discover_goal);
 		discoverGoalResId = getSharedPreferences("herbal", Context.MODE_PRIVATE)
 				.getInt("discoverHerbalId", -1);
 		discoverGoalString = DataHelper.herbalDiscoverResIdToName
 				.get(discoverGoalResId);
 		discoevrGoalChineseString = DataHelper.herbalDiscoverResIdToChineseName
 				.get(discoverGoalResId);
 
 		if (discoverGoalResId != -1) {
 			discoverGoal.setImageResource(discoverGoalResId);
 		}
 
 		// nextActivity = getIntent().getParcelableExtra("next_activity");
 		// nextNextActivity =
 		// getIntent().getParcelableExtra("next_next_activity");
 
 		nextActivity = new Intent();
 		nextActivity.setClassName(this,
 				"tw.edu.ntust.dt.herbal2.activity.UploadActivity");
 
 		nextNextActivity = new Intent();
 		nextNextActivity.setClassName(this,
 				"tw.edu.ntust.dt.herbal2.activity.UploadActivity");
 
 		closeButton = (ImageButton) findViewById(R.id.close_button);
 		closeButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (nextActivity == null)
 					return;
				session.close();
 				startActivity(nextActivity);
 			}
 		});
 		closeButton.setOnLongClickListener(new OnLongClickListener() {
 
 			@Override
 			public boolean onLongClick(View v) {
 				if (nextActivity == null)
 					return false;
				session.close();
 				startActivity(nextNextActivity);
 				return false;
 			}
 		});
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// start the scanner session
 		session.resume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		// pause the scanner session
 		session.pause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		// close the scanner session
 		session.close();
 	}
 
 	@Override
 	public void onApiSearchStart() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onApiSearchComplete(Result result) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onApiSearchFailed(MoodstocksError e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		session.close();
 		super.onBackPressed();
 	}
 	
 	@Override
 	public void onScanComplete(Result result) {
 		if (result != null) {
 
 			session.pause();
 			resultTextView.setText(String.format("Scan result: %s",
 					result.getValue()));
 
 			Toast.makeText(this,
 					String.format("Scan result: %s", result.getValue()),
 					Toast.LENGTH_SHORT).show();
 
 			if (discoverGoalString.equals(result.getValue()) == false) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				String name = DataHelper.herbalDiscoverResIdToChineseName
 						.get(result.getValue());
 				builder.setMessage("找錯囉這是" + name).setPositiveButton("重新尋找",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								session.resume();
 							}
 						});
				builder.setCancelable(false);
 				builder.create().show();
 			} else {
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage("找到囉").setPositiveButton("好",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								session.close();
 								startActivity(nextActivity);
 								finish();
 							}
 						});
				builder.setCancelable(false);
 				builder.create().show();
 			}
 		}
 	}
 
 	@Override
 	public void onScanFailed(MoodstocksError error) {
 		resultTextView.setText(String.format("Scan failed: %d",
 				error.getErrorCode()));
 	}
 }
