 package com.benbentaxi.passenger.taxirequest.confirm;
 
 import com.benbentaxi.passenger.R;
 import com.benbentaxi.passenger.location.DemoApplication;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 
 public class ConfirmPopupWindow extends PopupWindow{
 	
	private final static String TAG = ConfirmPopupWindow.class.getName();
 	private final static String BTN_POS_TEXT= "确认";
 	private final static String BTN_NEG_TEXT= "重新打车";
 
 	private View mView;
 	private TextView mTitle;
 	private TextView mContent;
 	private Button mBtnPos, mBtnNeg;
	private DemoApplication mApp;
 	private Activity mActivity;
 	
 	private View.OnClickListener mPosfunc = null, mNegfunc = null;
 
 	
 	public ConfirmPopupWindow(Context c)
 	{
 		super(c);
 	}
 	public ConfirmPopupWindow(Activity activity,int width,int height)
 	{
 		super(activity.getLayoutInflater().inflate(R.layout.confirm_dialog, null),width,height);
 		mActivity = activity;
 	}
 	public ConfirmPopupWindow(Activity activity)
 	{
 		this(activity,600,400);
 		mActivity 			 = activity;
 		DemoApplication mApp = (DemoApplication)activity.getApplicationContext();
 		
 		mView = this.getContentView();
 //		Display display = activity.getWindowManager().getDefaultDisplay();
 //		Log.e(TAG,"----------------------");
 //		Log.e(TAG,""+display.getHeight());
 //		Log.e(TAG,""+display.getWidth());
 		mTitle = (TextView)mView.findViewById(R.id.tvConfirmTitle);
     	mContent = (TextView)mView.findViewById(R.id.tvConfirmContent);
     	mBtnPos = (Button)mView.findViewById(R.id.btnConfirmOk);
     	mBtnNeg = (Button)mView.findViewById(R.id.btnConfirmCancel);
     	mTitle.setText("有司机响应，距离您约");
 		String d =(mApp.getCurrentTaxiRequest() != null) ? mApp.getCurrentTaxiRequest().getDistance().toString() : "0";
     	mContent.setText(d+"公里");
     	mBtnPos.setText(BTN_POS_TEXT);
     	mBtnNeg.setText(BTN_NEG_TEXT);
     	mPosfunc = mNegfunc = new View.OnClickListener() {		
 			@Override
 			public void onClick(View v) {
 				if ( ConfirmPopupWindow.this.isShowing() ) {
 					ConfirmPopupWindow.this.dismiss();
 				}
 			}
 		};
 	}
 	
 	public void show()
 	{
 		mBtnPos.setOnClickListener(mPosfunc);
 		mBtnNeg.setOnClickListener(mNegfunc);
 		mBtnPos.setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						ConfirmTask confirmRequest = new ConfirmTask(ConfirmPopupWindow.this.mActivity,true);
 						confirmRequest.go();
 						ConfirmPopupWindow.this.dismiss();
 
 					}
 	        	}
 		);
 		mBtnNeg.setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						ConfirmTask confirmRequest = new ConfirmTask(ConfirmPopupWindow.this.mActivity,false);
 						confirmRequest.go();
 						ConfirmPopupWindow.this.dismiss();
 
 					}
 	        	}
 		);
 		showAtLocation(mView, Gravity.CENTER, 0, 0);
 		
 	}
 
 	
 	
 	
 }
