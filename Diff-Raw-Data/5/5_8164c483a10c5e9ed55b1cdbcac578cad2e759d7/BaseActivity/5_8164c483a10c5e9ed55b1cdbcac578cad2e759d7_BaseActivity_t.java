 package com.app.getconnected.activities;
 
 import com.app.getconnected.R;
 import com.app.getconnected.animations.CollapseAnimation;
 import com.app.getconnected.animations.ExpandAnimation;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.DisplayMetrics;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 abstract class BaseActivity extends Activity {
 
 	protected TextView txtHeading;
 	protected Button buttonBack;
 	private Button buttonMenu;
 	protected Button buttonOk;
 	protected Button buttonHome;
 		
 	private LinearLayout MenuList;
 	private int screenHeight;
 	private boolean isExpanded = false;
 
 	protected static final String activityPackage = "com.app.getconnected.activities";
 	protected static Boolean loggedIn=false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.footer);
 		
 	}
 
 	
 	protected void initLayout(int resId, boolean homeButton,
 			boolean backButton, boolean menuButton, boolean okButton) {
 
 		if (txtHeading == null)
 			txtHeading = (TextView) findViewById(R.id.header_text);
 		if (txtHeading != null)
 			txtHeading.setText(resId);
 
 		buttonHome = (Button) findViewById(R.id.header_button_home);
 		buttonBack = (Button) findViewById(R.id.footer_button_back);
 		buttonMenu = (Button) findViewById(R.id.footer_button_menu);
 		buttonOk = (Button) findViewById(R.id.footer_button_ok);
 		MenuList = (LinearLayout) findViewById(R.id.linearLayout3);
 		
 		MenuList.bringToFront();
         
         
 		buttonBack.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goBack();
 			}
 		});
 		
 		buttonMenu.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				handleMenu();
 			}
 		});
 
 
 		buttonHome.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(BaseActivity.this,
 						MainActivity.class);
 				startActivityForResult(intent, 1);
 				finish();
 			}
 		});
 
 		this.buttonHome.setVisibility(homeButton ? View.VISIBLE
 				: View.INVISIBLE);
 		this.buttonBack.setVisibility(backButton ? View.VISIBLE
 				: View.INVISIBLE);
 		this.buttonMenu.setVisibility(menuButton ? View.VISIBLE
 				: View.INVISIBLE);
 		this.buttonOk.setVisibility(okButton ? View.VISIBLE : View.INVISIBLE);
 	}
 
 	protected void goBack() {
 		super.onBackPressed();
 	}
 	
 	private void handleMenu() {
 		DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         screenHeight = metrics.heightPixels;
         
       
         			
         			if (isExpanded) {
             			isExpanded = false;
            			MenuList.startAnimation(new CollapseAnimation(MenuList, 0,(int)(screenHeight*0.5), 20));
             		}else {
                 		isExpanded = true;
                		MenuList.startAnimation(new ExpandAnimation(MenuList, 0,(int)(screenHeight*0.5), 20));
             		}
         		
 	}
 
 
 	protected void disableBackButton() {
 
 	}
 
 	/**
 	 *
 	 * @param view
 	 */
 	public void startIntentByButton(View view) {
 		Button button = (Button) view;
 		if(!button.getTag().equals("")) {
 			try {
 				Intent intent = new Intent(getApplicationContext(), Class.forName(BaseActivity.activityPackage + "." + button.getTag().toString()));
 				startActivityForResult(intent, 1);
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
