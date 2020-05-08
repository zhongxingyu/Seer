 package com.example.starter;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View.OnTouchListener;
 
 import com.example.controller.Controller;
 import com.example.controller.TouchController;
 import com.example.model.Model;
 import com.example.model.ModelImplementation;
 import com.example.view.CanvasView;
 
 public class SurfaceViewActivity extends Activity implements OnTouchListener{
     private Model model;
     private CanvasView view;
     private Controller controller;
     private static Context context;
    
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         context = getApplicationContext();
         instantiateClasses();
         setUpListeners();
         setContentView(view);	   
     }
 
 
 	private void instantiateClasses() {
 		controller = new TouchController();
 		model = new ModelImplementation();
 		view = new CanvasView();
 	}
 	
 	private void setUpListeners() {
 		model.addObserver(view);
 		controller.addObserver(model);
 	}
 		
 	public static Context getAppContext() {
 		return context;
 	}	
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
		controller.gridGotTouched((int)event.getX(), (int)event.getY() - 40);
		return true;
 	}
 	
 
 	@Override
 	public boolean onTouch(android.view.View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
