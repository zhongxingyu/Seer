 package jp.mixi.sample.drawable;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.graphics.drawable.ClipDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.ScaleDrawable;
 import android.graphics.drawable.TransitionDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 
 public class MainActivity extends Activity {
 	private static final int TRANSITION_DURATION = 5000;
 	private static final long TIMER_TASK_PERIOD = 1000L;
 	private static final int TIMER_TASK_DELAY = 0;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         final Handler handler = new Handler();
         final Timer timer = new Timer();
 
         // 5bƂɃxύX^XN
        final Drawable levelListDrawable = findViewById(R.id.LevelListButton).getBackground();
         timer.scheduleAtFixedRate(
         		new TimerTask() {
 					@Override
 					public void run() {
 						final int level = levelListDrawable.getLevel();
 						handler.post(new Runnable() {
 							@Override
 							public void run() {
 								if (level == 0) {
 									levelListDrawable.setLevel(1);
 								} else {
 									levelListDrawable.setLevel(0);
 								}
 							}
 						});
 					}
 				},
 				TIMER_TASK_DELAY, TIMER_TASK_PERIOD);
 
         // NXtF[hJn
         final TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.TransitionView).getBackground();
         transition.startTransition(TRANSITION_DURATION);
 
         // 5bƂɃxύXANbv͈̔͂ω^XN
         final ClipDrawable clipDrawable = (ClipDrawable) findViewById(R.id.ClipView).getBackground();
         timer.scheduleAtFixedRate(
       		new TimerTask() {
 					@Override
 					public void run() {
 						handler.post(new Runnable() {
 							@Override
 							public void run() {
 								clipDrawable.setLevel(clipDrawable.getLevel() + 1000);
 							}
 						});
 					}
 				},
 				TIMER_TASK_DELAY, TIMER_TASK_PERIOD);
 
         // XP[̃xύX
         ScaleDrawable scale = (ScaleDrawable) findViewById(R.id.ScaleView).getBackground();
         scale.setLevel(1);
 	}
 }
