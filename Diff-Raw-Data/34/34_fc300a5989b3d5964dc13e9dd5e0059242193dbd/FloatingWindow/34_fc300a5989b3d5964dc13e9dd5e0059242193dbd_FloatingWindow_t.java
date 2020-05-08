 package com.myhome.widgets;
 
 import com.example.myhome.R;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.PixelFormat;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.WindowManager.LayoutParams;
 import android.widget.TextView;
 
 public class FloatingWindow {
 	
 	private WindowManager windowManager;
 	private WindowManager.LayoutParams params;
 	private View windowView;
 	private LayoutInflater layoutInflater;
 	
 	public FloatingWindow(Context context){
 		windowManager=(WindowManager) context.getSystemService(context.WINDOW_SERVICE);
 		layoutInflater=(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
 		params=new LayoutParams();
 	}
 	
 	public void setDefaultParams(){
 		
 		
 		//set floating window to be global "TYPE_PHONE"
 		params.type=LayoutParams.TYPE_PHONE;
 		//background transparent
 		params.format=PixelFormat.TRANSLUCENT;
 		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 		params.gravity=Gravity.LEFT | Gravity.TOP;
 		params.x=0;
 		params.y=0;
 		params.width=WindowManager.LayoutParams.WRAP_CONTENT;
 		params.height=WindowManager.LayoutParams.WRAP_CONTENT;
 
 	}
 	
 	public void setView(int resource){
 		windowView=layoutInflater.inflate(resource, null);
 		TextView tv=(TextView)windowView.findViewById(R.id.textView1);
 		tv.setTextColor(Color.BLACK);
 		
 		//set floating window movable
 		windowView.setOnTouchListener(new View.OnTouchListener() {
 			

			float startx=0;
			float starty=0;
 			
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub

				float x=0,y=0;
 				
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					startx=event.getX();
 					starty=event.getY();
					//updatePosition(x-startx, y-starty);
 					break;
 				case MotionEvent.ACTION_MOVE:
					x=event.getX();
					y=event.getY();
					updatePosition(params.x+x-startx, params.y+y-starty);
					//Log.v("myhome", "action_move: "+x+","+y);
					//Log.v("myhome", "action_move: "+startx+","+starty);
					//Log.v("myhome", "action_move: "+(x-startx)+","+(y-starty));
 
 					break;
 				case MotionEvent.ACTION_UP:
 					
 					break;
 		
 				default:
 					break;
 				}
 				
 				return false;
 			}
 		});
 	}
 	
 	public void updatePosition(float x,float y){
 		params.x=(int)x;
 		params.y=(int)y;
 		windowManager.updateViewLayout(windowView, params);
 	}
 	
 	public void display(){
 
 		windowManager.addView(windowView, params);
 	}
 	
 	public void hide(){
 		windowManager.removeView(windowView);
 	}
 
 }
