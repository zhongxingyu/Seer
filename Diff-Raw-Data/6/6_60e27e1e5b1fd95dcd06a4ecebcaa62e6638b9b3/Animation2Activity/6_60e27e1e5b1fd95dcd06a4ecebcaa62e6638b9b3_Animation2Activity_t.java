 package com.example.animation2;
 
 import android.app.Activity;
 import android.graphics.drawable.AnimationDrawable;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.MarginLayoutParams;
 //import android.widget.FrameLayout.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import android.os.Bundle;
 
 public class Animation2Activity extends Activity {
 
 	// constants..
 	private static final String TAG =Animation2Activity.class.getSimpleName();
 	private static final boolean IS_DEBUG = true;
 
 	// these are used in the different methods..
 	ImageView animImageStickman;
 	AnimationDrawable animDrawableStickman;	
 
 	// these are used to work out direction..
 	float xStartPos, xEndPos, yStartPos, yEndPos;
 	boolean isUpward = false, isDownward = false,
 			isLeft = false, isRight = false;
 
 	// this is the lister that will be stuck onto animImageStickman..
 	OnClickListener OnClickStickman;
 
 	//useful logging method..
 	private void logDebug(String log_message) {
 		if (IS_DEBUG) { Log.d(TAG, log_message); } 
 	}
 
 	//Called when the activity is first created. 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		try {						
 			super.onCreate(savedInstanceState);	
 			setContentView(R.layout.main);
 
 			final TextView reportView = (TextView) findViewById(R.id.report_view);
 			// make an image view for r.id.stickman..			
 			animImageStickman = (ImageView) 
 					findViewById(R.id.stickman);
 
 			//animDrawableStickman = (AnimationDrawable) animImageStickman.getDrawable();
 
 			// we run the animation (using post)..
 			runAnimationViaPost();			
 
 			// we set a listener onto this image and override the onclick behaviour..
 			animImageStickman.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {	
 					Log.w(TAG,"SHORT CLICK! isUpward="+isUpward+", isDownward="+isDownward);
 					//animDrawableStickman = (AnimationDrawable) animImageStickman.getDrawable();
 					runAnimationViaPost();		
 				}
 			}
 
 					);
 
 
 			animImageStickman.setOnTouchListener(new OnTouchListener()
 			{
 				private float xDiff, yDiff;
 				@Override
 				public boolean onTouch(View v, MotionEvent event)
 				{
 					boolean myReturn = false;
 					//Log.d(TAG,"START "+ "action id="+event.getAction() +"x="+event.getX()+", y="+event.getY());
 					switch (event.getAction())
 					{ 
 					// this is called when the screen is touched
 					case MotionEvent.ACTION_DOWN:		    	  
 						xStartPos = event.getX();
 						yStartPos = event.getY();
 						Log.d(TAG,"START ("+xStartPos+", "+yStartPos+")");
 						myReturn = false;
 						break;
 
 						// this is called when the touch ends
 					case MotionEvent.ACTION_UP:
 						xEndPos = event.getX();
 						yEndPos = event.getY();
 						Log.d(TAG,"ACTION UP END ("+xEndPos+", "+yEndPos+")");
 						Log.d(TAG,"diff in y = "+(yEndPos - yStartPos) );
 
 						xDiff = Math.abs(xEndPos - xStartPos);
 						yDiff = Math.abs(yEndPos - yStartPos);
 						if (yDiff > xDiff) {
							
							//we turn off the x flags as y won!
							isRight = false; isLeft = false;
 							if (yStartPos > yEndPos) {
 								isDownward = false; isUpward = true;
 							}
 							else if (yStartPos < yEndPos) {
 								isDownward = true; isUpward = false;
 							}	
 						}
 						else  {
							
							isDownward = false; isUpward = false;
 							if (xStartPos > xEndPos) {
 								isRight= false; isLeft = true;
 							}
 							else if (xStartPos < xEndPos) {
 								isRight = true; isLeft = false;
 							}	
 						}
 					
 					myReturn = true;
 
 					runAnimationViaPost();
 					break;
 
 				default:		
 					myReturn = false;
 					//Log.d(TAG,"other = "+event.getAction());
 				}
 				//Log.d(TAG,"END "+ "action id="+event.getAction() +"x="+event.getX()+", y="+event.getY());
 				return myReturn;
 			}
 		});
 
 
 
 	}catch(Throwable tr) { Log.e(TAG, "Error in onCreate! ",tr);}
 }
 ////////////////////////////////////////////
 
 ////////////////////////////////////////////
 
 // this method causes the animation to run..
 private void runAnimationViaPost() {
 	boolean postResult = false;
 
 	// we chose which animation to use..
 	if ( isUpward ){animImageStickman.setImageResource(R.anim.stickman_pullup);}
 	else if (isDownward){animImageStickman.setImageResource(R.anim.stickman_pulldown);}
 	else if (isLeft){animImageStickman.setImageResource(R.anim.stickman_left);}
 	else if (isRight){animImageStickman.setImageResource(R.anim.stickman_right);}
 	else {animImageStickman.setImageResource(R.anim.stickman_wave);}
 
 	animDrawableStickman = (AnimationDrawable) animImageStickman.getDrawable();
 
 	postResult = animImageStickman.post(
 			new Runnable() {
 				public void run() { 
 					try{
 						logDebug("starting animation!");
 						animImageStickman.setImageDrawable(animDrawableStickman);
 						//animImageStickman.setFillAfter(true);
 						animDrawableStickman.start();
 						// this is the reset bit..
 						animDrawableStickman.setVisible(true, true);
 
 					}catch(Throwable tr) { Log.e(TAG, "Error in runAnimationViaPost! ",tr);}
 				}
 			}
 			);		
 }
 ////////////////////////////////////////////
 
 
 }
 
 
 
 
 
