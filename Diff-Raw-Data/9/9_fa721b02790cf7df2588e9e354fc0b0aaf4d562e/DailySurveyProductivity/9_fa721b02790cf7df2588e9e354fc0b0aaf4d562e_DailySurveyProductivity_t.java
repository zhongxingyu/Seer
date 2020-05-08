 package com.example.drinkingapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class DailySurveyProductivity extends Activity implements OnClickListener,OnCheckedChangeListener{
 
 	Button finish;
 	SeekBar productivityQualityBar,academicsQualityBar,stressBar;
 	TextView test1,test2,test3;
 	String seekbarResult1="0",seekbarResult2="0",seekbarResult3="0";
 	Intent goToAssessment;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);// full screen
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.dailysurveyproductivity);
 
 		finish=(Button)findViewById(R.id.bDSProductivityFinish);
 		test1=(TextView)findViewById(R.id.tvDSProductivityTest1);
 		test2=(TextView)findViewById(R.id.tvDSProductivityTest2);
 		test3=(TextView)findViewById(R.id.tvDSProductivityTest3);
 		productivityQualityBar=(SeekBar)findViewById(R.id.sbDSProductivity1);
 		academicsQualityBar=(SeekBar)findViewById(R.id.sbDSProductivity2);
 		stressBar=(SeekBar)findViewById(R.id.sbDSProductivity3);
 		
 
 		finish.setOnClickListener(this);
 		productivityQualityBar.setProgress(0); //this is line 19
 		productivityQualityBar.setMax(100);
 		academicsQualityBar.setProgress(0); //this is line 19
 		academicsQualityBar.setMax(100);
 		stressBar.setProgress(0); //this is line 19
 		stressBar.setMax(100);
 		
 		initializeSeekBar();
 	}
 
 	private void initializeSeekBar() {
 		// TODO Auto-generated method stub
 		productivityQualityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// TODO Auto-generated method stub
 				test1.setText(String.valueOf(progress));
 				seekbarResult1=String.valueOf(progress);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		academicsQualityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// TODO Auto-generated method stub
 				test2.setText(String.valueOf(progress));
 				seekbarResult2=String.valueOf(progress);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		stressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// TODO Auto-generated method stub
 				test3.setText(String.valueOf(progress));
 				seekbarResult3=String.valueOf(progress);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch(arg0.getId()){
 		/*
 		case R.id.bDS2Record:
 			break;
 			
 			*/
		case R.id.bDSProductivityFinish:
			finish();
			break;	
 		}
 		
 	}
 	//not used in this survey, but kept in case it's needed in the future
 	@Override
 	public void onCheckedChanged(RadioGroup group, int checkedId) {
 		// TODO Auto-generated method stub
 		switch(group.getId()){
 		}
 	}
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		finish();
 	}
 
 }
