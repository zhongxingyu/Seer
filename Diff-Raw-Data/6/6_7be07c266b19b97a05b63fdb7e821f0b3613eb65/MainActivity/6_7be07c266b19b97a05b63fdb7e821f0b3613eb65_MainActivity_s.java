 package com.hack.regionunlocked;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	GameStatus scanStatus;
 
 	@Override
 	public void onBackPressed() {
 		finish();
 	}
 
 	protected void setScanStatus(GameStatus s) {
 		startActivity(new Intent(getApplicationContext(), ResultsActivity.class));
 		/*setContentView(R.layout.results);
 		scanStatus = s;
 		TextView textView2 = (TextView) findViewById(R.id.textView2);
 		textView2.setText(s.getSupportAsText());*/
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
		//setting the image size for any screen.
		DisplayMetrics screenDimensions = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(screenDimensions);
		int height = screenDimensions.heightPixels;
		
		
 		setContentView(R.layout.activity_main);
 		ImageButton scanButton = (ImageButton) findViewById(R.id.scanButton);
 		scanButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent clickIntent = new Intent(getApplicationContext(),
 						ScanBarcodeActivity.class);
 				startActivityForResult(clickIntent, 1);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (requestCode == 1) {
 
 			if (resultCode == RESULT_OK) {
 				String result = data.getStringExtra("barcode");
 				setScanStatus(new GameStatus(result));
 			}
 			if (resultCode == RESULT_CANCELED) {
 				// Do nothing! Wait for the user to initiate another go.
 			}
 		}
 	}
 }
