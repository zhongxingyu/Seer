 package com.prototypical.darksoulscalculator;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentSender.SendIntentException;
 import android.content.ServiceConnection;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.DisplayMetrics;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.android.vending.billing.IInAppBillingService;
 
 public class MainActivity extends Activity {
 
 	static int yourLevel = 0, canBeSummonedLow = 0,
 			   canBeSummonedHigh = 0, canSummonLow = 0,
 			   canSummonHigh = 0;
 	
 	EditText yourLevelETX;
 	TextView canSummonNum;
 	TextView canBeSummonedNum;
 	
 	TextView canSummonTXT;
 	TextView canBeSummonedTXT;
 	
 	TextView yourLevelTXT;
 	TextView titleTXT;
 	
 	Button calculateBTN;
 		
 	static int color = Color.BLACK;
 	static int tColor = Color.WHITE;
 	static int radius = 5;
 	static int dx = 4;
 	static int dy = 3;
 	int bigFont = 30;
 	int normFont = 25;
 	int smallFont = 22;
 	
 	Button donateBTN;
 	
 	IInAppBillingService mService;
 
 	ServiceConnection mServiceConn = new ServiceConnection() {
 	   @Override
 	   public void onServiceDisconnected(ComponentName name) {
 	       mService = null;
 	   }
 
 	   @Override
 	   public void onServiceConnected(ComponentName name, 
 	      IBinder service) {
 	       mService = IInAppBillingService.Stub.asInterface(service);
 	   }
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		setContentView(R.layout.activity_main);
 		Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/OptimusPrinceps.ttf");
 		calculateBTN = (Button) findViewById(R.id.calculateButton);
 		yourLevelETX = (EditText) findViewById(R.id.yourLevelNum);
 		canSummonNum = (TextView) findViewById(R.id.canSummonNum);
 		canBeSummonedNum = (TextView) findViewById(R.id.canBeSummonedByNum);
 		canSummonTXT = (TextView) findViewById(R.id.canSummonText);
 		canBeSummonedTXT = (TextView) findViewById(R.id.canBeSummonedBy);
 		yourLevelTXT = (TextView) findViewById(R.id.yourLevelText);
 		titleTXT = (TextView) findViewById(R.id.titleText);
 		
 		bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"),
 		                mServiceConn, Context.BIND_AUTO_CREATE);
 		
		donateBTN = (Button) findViewById(R.id.donateButton);
 		
 		donateBTN.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				String sku = "donation";
 				try {
 					Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
 							   sku, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
 					PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
 					startIntentSenderForResult(pendingIntent.getIntentSender(),
 							   1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
 							   Integer.valueOf(0));
 				} catch (RemoteException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (SendIntentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
		});
 		
 		yourLevelETX.setTextColor(tColor);
 		yourLevelETX.setTypeface(font);
 		yourLevelETX.setTextSize(smallFont);
 		
 		calculateBTN.setTextColor(tColor);
 		calculateBTN.setTypeface(font);
 		calculateBTN.setTextSize(smallFont);
 		
 		canSummonNum.setShadowLayer(radius, dx, dy, color);
 		canSummonNum.setTypeface(font);
 		canSummonNum.setTextSize(smallFont);
 		canSummonNum.setTextColor(tColor);
 		
 		canBeSummonedNum.setShadowLayer(radius, dx, dy, color);
 		canBeSummonedNum.setTypeface(font);
 		canBeSummonedNum.setTextSize(smallFont);
 		canBeSummonedNum.setTextColor(tColor);
 		
 		canSummonTXT.setShadowLayer(radius, dx, dy, color);
 		canSummonTXT.setTypeface(font);
 		canSummonTXT.setTextSize(normFont);
 		canSummonTXT.setTextColor(tColor);
 		
 		canBeSummonedTXT.setShadowLayer(radius, dx, dy, color);
 		canBeSummonedTXT.setTypeface(font);
 		canBeSummonedTXT.setTextSize(normFont);
 		canBeSummonedTXT.setTextColor(tColor);
 		
 		yourLevelTXT.setShadowLayer(radius, dx, dy, color);
 		yourLevelTXT.setTypeface(font);
 		yourLevelTXT.setTextSize(bigFont);
 		yourLevelTXT.setTextColor(tColor);
 		
 		
 		titleTXT.setShadowLayer(radius, dx, dy, color);
 		titleTXT.setTypeface(font);
 		titleTXT.setTextSize(bigFont);
 		titleTXT.setGravity(Gravity.CENTER_HORIZONTAL);
 		titleTXT.setTextColor(tColor);
 		
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
 		int height = metrics.heightPixels;
 		int width = metrics.widthPixels;
 		
 		/*TableLayout viewTable = (TableLayout) findViewById(R.id.table);
 		Bitmap donateBG = BitmapFactory.decodeResource(getResources(), R.drawable.calculate_button);
 		donateBG = Bitmap.createScaledBitmap(donateBG, 500, 70, false);*/
 		
 		findViewById(R.id.table).setBackgroundResource(R.drawable.bg);
 		
 		calculateBTN.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				if(yourLevelETX.getText().toString().length() > 0){
 					CalculateValues();
 					SetValues();
 				} else {
 					Toast.makeText(getApplicationContext(), "Please enter your level first.", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
 	   if (requestCode == 1001) {           
 	      int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
 	      String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
 	      String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
 	        
 	      if (resultCode == RESULT_OK) {
 	         try {
 	            JSONObject jo = new JSONObject(purchaseData);
 	            String sku = jo.getString("productId");
 	            Toast.makeText(getApplicationContext(), "Thank you for the donation!", Toast.LENGTH_LONG).show();
 	          }
 	          catch (JSONException e) {
 	        	 Toast.makeText(getApplicationContext(), "Donation could not be processed. Sorry.", Toast.LENGTH_LONG).show();
 	             e.printStackTrace();
 	          }
 	      }
 	   }
 	}
 
 	@Override
 	public void onResume(){
 		super.onResume();
 	}
 	
 	public void CalculateValues(){
 		yourLevel = Integer.parseInt(yourLevelETX.getText().toString());
 		canBeSummonedLow = (int) Math.floor( yourLevel - (0.10 * yourLevel) - 10);
 		canBeSummonedHigh = (int) Math.floor(yourLevel + (0.10 * yourLevel) + 10);
 		canSummonLow = canBeSummonedLow;
 		canSummonHigh = canBeSummonedHigh + 1;
 	}
 	
 	public void SetValues(){
 		if(canBeSummonedLow < 1) canBeSummonedLow = 1;
 		if(canSummonLow < 1) canSummonLow = 1;
 		if(canBeSummonedHigh > 713) canBeSummonedHigh = 713;
 		if(canSummonHigh > 713) canSummonHigh = 713;
 		canBeSummonedNum.setText("LVL: " + canBeSummonedLow + " - " + canBeSummonedHigh);
 		canSummonNum.setText("LVL: " + canSummonLow + " - " + canSummonHigh);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onDestroy() {
 	    super.onDestroy();
 	    if (mServiceConn != null) {
 	        unbindService(mServiceConn);
 	    }   
 	}
 
 }
