 package com.papagiannis.tuberun;
 
 import android.app.Activity;
 import android.os.Handler;
 import android.view.LayoutInflater;
import android.view.MotionEvent;
 import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 public class SlidingBehaviour {
 	
 	private Activity activity;
 	private int mainLayout;
 	
 	public SlidingBehaviour(Activity activity,int mainLayout) {
 		this.activity=activity;
 		this.mainLayout=mainLayout;
 	}
 	
 	protected MyHorizontalScrollView scrollView;
     protected MainMenu menu;
     protected View app;
     protected Button showMenuButton1;
     protected Button showMenuButton2;
     protected boolean menuOut = false;
     protected Handler handler = new Handler();
     protected int btnWidth;
 	
     public void setupHSVWithLayout() {
 		LayoutInflater inflater = LayoutInflater.from(activity);
         scrollView = (MyHorizontalScrollView) inflater.inflate(R.layout.horz_scroll_with_list_menu, null);
 
         menu = new MainMenu(activity);
         app = inflater.inflate(mainLayout, null);
         ViewGroup tabBar = (ViewGroup) app.findViewById(R.id.main_layout);
 
         showMenuButton1 = (Button) tabBar.findViewById(R.id.logo_button);
         showMenuButton2 = (Button) tabBar.findViewById(R.id.back_button);
         showMenuButton1.setOnClickListener(new ClickListenerForMenu(scrollView, menu));
         showMenuButton2.setOnClickListener(new ClickListenerForMenu(scrollView, menu));
         menu.setMenuButton(showMenuButton1);
 
         final View[] children = new View[] { menu, app };
 
         // Scroll to app (view[1]) when layout finished.
         int scrollToViewIdx = 1;
         scrollView.initViews(children, scrollToViewIdx, new SizeCallbackForMenu(showMenuButton1));
         
         activity.setContentView(scrollView);
 	}
     
 }
