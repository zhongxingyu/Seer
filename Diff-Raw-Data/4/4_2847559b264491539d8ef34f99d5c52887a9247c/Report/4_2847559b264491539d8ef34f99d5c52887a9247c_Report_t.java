 package ru.inventos.yum;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class Report extends Activity implements OrderStatusReceiver {
 	private Cart mCart;
 	private NetStorage netStorage;
 	private TextView mTitle;
 	private TextView mText1;
 	private TextView mText2;
 	private TextView mText3;
 	private ImageButton mButton;
 	private Resources mResources;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_report);
 		mCart = new Cart();
 		netStorage = new NetStorage(this);
 		findViews();
 		mResources = this.getResources();
 		setWait();		
 		CartItem[] items = mCart.getArray();
 		mCart.clear();
 		netStorage.makeOrder(this, items);		
 	}
 	
 	private void findViews() {
 		mTitle = (TextView) findViewById(R.id.report_title);
 		mText1 =  (TextView) findViewById(R.id.report_text1);
 		mText2 =  (TextView) findViewById(R.id.report_text2);
 		mText3 =  (TextView) findViewById(R.id.report_text3);
 		mButton = (ImageButton) findViewById(R.id.report_btn);		
 	}
 	
 	private void setWait() {		
 		mTitle.setText(mResources.getString(R.string.report_title_wait));
 		mText1.setText(mResources.getString(R.string.report_text1_wait));
 		mText2.setVisibility(TextView.INVISIBLE);
 		mText3.setVisibility(TextView.INVISIBLE);
 		mButton.setVisibility(ImageButton.INVISIBLE);		
 	}
 	
 	private void setError() {		
 		mTitle.setText(mResources.getString(R.string.report_title_err));
 		mText1.setText(mResources.getString(R.string.report_text1_err));
 		mText2.setVisibility(TextView.GONE);
 		mText3.setVisibility(TextView.VISIBLE);
 		mText3.setText(mResources.getString(R.string.report_text3_err));
 		mButton.setVisibility(ImageButton.VISIBLE);		
 	}
 	
 	private void setOk() {		
 		mTitle.setText(mResources.getString(R.string.report_title_normal));
 		mText1.setText(mResources.getString(R.string.report_text1_normal));
 		mText2.setVisibility(TextView.VISIBLE);
 		mText2.setText(mResources.getString(R.string.report_text2_normal));
 		mText3.setVisibility(TextView.VISIBLE);
 		mText3.setText(mResources.getString(R.string.report_text3_normal));
 		mButton.setVisibility(ImageButton.VISIBLE);		
 	}
 	
 	
	public void onBtnClick(View v) {		
 		finish();
 	}
 	
 	@Override
 	public void receiveStatus(boolean status) {
 		if (status) {
 			setOk();
 		}
 		else {
 			setError();
 		}
 	}
 }
