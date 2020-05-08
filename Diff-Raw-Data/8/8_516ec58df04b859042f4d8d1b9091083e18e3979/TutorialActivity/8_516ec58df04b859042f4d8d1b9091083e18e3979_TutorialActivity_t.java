 package com.example.gamecontrollerdatatransfer;
 
import android.R.anim;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class TutorialActivity extends Activity {
 
 	AnimationDrawable animation = new AnimationDrawable();
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tutorial);	
 		
 		startTutorialAnimation();
 	}
 
 	@SuppressWarnings("deprecation")
 	private void startTutorialAnimation() {
 	    animation.addFrame(getResources().getDrawable(R.drawable.position), 5000);
 	    animation.addFrame(getResources().getDrawable(R.drawable.controls), 4000);
 	    animation.addFrame(getResources().getDrawable(R.drawable.tiltdown), 3000);
 	    animation.addFrame(getResources().getDrawable(R.drawable.tiltup), 2000);
 	    animation.addFrame(getResources().getDrawable(R.drawable.tiltleft), 2000);
 	    animation.addFrame(getResources().getDrawable(R.drawable.tiltright), 2000);
 	    animation.addFrame(getResources().getDrawable(R.drawable.simulatebutton), 3000);
 	    animation.addFrame(getResources().getDrawable(R.drawable.servermap), 5000);
 	    animation.addFrame(getResources().getDrawable(R.drawable.buttons), 5000);
 	    animation.setOneShot(true);
 
 	    ImageView imageAnim =  (ImageView) findViewById(R.id.image);
 	    imageAnim.setBackgroundDrawable(animation);
 
 	    // start the animation!
 	    animation.start();
 	    checkIfAnimationDone(animation);
 	    Button skipTut = (Button) findViewById(R.id.skiptut);
 		skipTut.setOnClickListener(skipTutorial);
 	}
 	
 	Button.OnClickListener skipTutorial = new Button.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			animation.stop();
 		}
 	};
 	
 	 private void checkIfAnimationDone(AnimationDrawable anim){
 	        final AnimationDrawable a = anim;
 	        int timeBetweenChecks = 3000;
 	        Handler h = new Handler();
 	        h.postDelayed(new Runnable(){
 	            public void run(){
 	                if (a.getCurrent() != a.getFrame(a.getNumberOfFrames() - 1)){
 	                    checkIfAnimationDone(a);
 	                } else{
 	                	animation.stop();
 	                	for (int i = 0; i < animation.getNumberOfFrames(); ++i){
 	                	    Drawable frame = animation.getFrame(i);
 	                	    if (frame instanceof BitmapDrawable) {
 	                	        ((BitmapDrawable)frame).getBitmap().recycle();
 	                	    }
 	                	    frame.setCallback(null);
 	                	}
 	                    Intent k = new Intent(TutorialActivity.this, TransitionActivity.class);
 	                    startActivity(k);
 	                }
 	            }
 	        }, timeBetweenChecks);
 	    };
 }
