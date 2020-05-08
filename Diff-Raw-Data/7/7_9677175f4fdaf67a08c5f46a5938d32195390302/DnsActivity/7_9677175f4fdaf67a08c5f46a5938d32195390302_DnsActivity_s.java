 package com.studentersamfundet.app.ui;
 
 import java.util.Calendar;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.studentersamfundet.app.R;
 
 public class DnsActivity extends BaseDnsActivity {
 	String[] openingHoursHouse = {
 		"13.00 - 01.00",
 		"13.00 - 01.00",
 		"      - 01.30",
 		"      - 03.00",
 		"      - 03.00",
 		"15.00 - 03.00",
 		"      - 19.00"};
 	
 	String[] openingHoursBC = {
		"Stengt",
 		"19.00 - 00.00",
 		"19.00 - 00.00",
 		"19.00 - 00.00",
 		"19.00 - 03.00",
 		"19.00 - 03.00",
		"20.00 - 03.00"};
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		/* Set opening hours: */
 		TextView tvNeuf = (TextView)findViewById(R.id.main_menu_hours_neuf);
 		TextView tvBC = (TextView)findViewById(R.id.main_menu_hours_bc);
 		int day = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7;
 		
 		tvNeuf.setText(openingHoursHouse[day]);
 		tvBC.setText(openingHoursBC[day]);
 		
 		setupFocus();
 	}
 
 	public void setupFocus() {
 		final View[] buttons = {
 			findViewById(R.id.main_menu_program),
			findViewById(R.id.main_menu_hours),
 			findViewById(R.id.main_menu_tickets),
 			findViewById(R.id.main_menu_join),
 		};
 		
 		for (int i = 0; i < buttons.length; i++) {
 			final int index = i;
 			
 			buttons[index].setOnTouchListener(new OnTouchListener() {
 				boolean pointerMoved = false;
 				
 				public boolean onTouch(View v, MotionEvent event) {
 					if (event.getAction() == MotionEvent.ACTION_DOWN) {
 						for (int j = 0; j < buttons.length; j++) {
 							if (index != j) {								
 								Animation animation = new AlphaAnimation(1.0f, 0.6f);
 								animation.setDuration(100);
 								animation.setFillAfter(true);
 
 								buttons[j].startAnimation(animation);
 								
 							}
 						}
 						
 						this.pointerMoved = false;
 						return true;
 					} 
 					
 					if (event.getAction() == MotionEvent.ACTION_UP) {
 						/* Define the animation that makes elements opaque: */
 						Animation animation = new AlphaAnimation(0.6f, 1.0f);
 						animation.setDuration(50);
 						animation.setFillAfter(true);
 						
 						if (!this.pointerMoved) {					
 							v.performClick();
 						}
 						
 						for (int j = 0; j < buttons.length; j++) {
 							buttons[j].startAnimation(animation);
 						}
 						
 						return true;
 					}
 					
 					if (event.getAction() == MotionEvent.ACTION_MOVE) {
 						this.pointerMoved = true;
 					}
 					
 					return false;
 				}
 			});
 		}
 	}
 	
 	public void programButton(View v) {
 		Intent intent = new Intent();
 
 		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
 				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		intent.setClass(this, ProgramListActivity.class);
 
 		startActivity(intent);
 	}
 
 	public void ticketButton(View v) {
 		Intent intent = new Intent();
 
 		intent.setAction(Intent.ACTION_VIEW);
 		intent.setData(Uri.parse("http://m.billettservice.no/ticket/search.do?articles=tmno&query=studentersamfundet&submit=S%C3%B8k"));
 
 		startActivity(intent);
 	}
 
 	public void newsButton(View v) {
 		Toast.makeText(this, "Not implemented (yet!)", Toast.LENGTH_SHORT).show();
 	}
 
 	public void joinButton(View v) {
 		Intent intent = new Intent();
 		
 		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		intent.setClass(this, JoinUsActivity.class);
 		
 		startActivity(intent);
 
 	}
 }
