 package com.arcao.sayitlouder;
 
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 
import com.arcao.sayitloud.R;
 import com.arcao.sayitlouder.shake.ShakeDetector;
 import com.arcao.sayitlouder.shake.ShakeListener;
 import com.arcao.sayitlouder.view.MessageView;
 
 public class DisplayActivity extends Activity implements ShakeListener, Runnable {
 	public static final String INTENT_EXTRA_MESSAGE = "MESSAGE";
 	
 	public static int HIGHLIGHT_ITERATION_COUNT = 5;
 	public static int HIGHLIGHT_PAUSE_MS = 200;
 
 	private ShakeDetector detector;
 	private int highlightIteration = 0;
 	private ScheduledThreadPoolExecutor executor;
 	private MessageView view;
 	private ScheduledFuture<?> future;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.display);
 		LinearLayout v = (LinearLayout) findViewById(R.id.displayHolder);
 		view = new MessageView(this, getIntent().getStringExtra(INTENT_EXTRA_MESSAGE));
 		v.addView(view);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		detector = new ShakeDetector(this);
 		detector.addListener(this);
 		detector.start();
 		
 		executor = new ScheduledThreadPoolExecutor(1);
 	}
 
 	@Override
 	protected void onPause() {
 		view.shutdown();
 		detector.stop();
 		detector.removeListener(this);
 		super.onPause();
 	}
 
 	@Override
 	public void onShake() {
 		future = executor.scheduleWithFixedDelay(this, 0, HIGHLIGHT_ITERATION_COUNT, TimeUnit.MILLISECONDS);
 	}
 	
 	@Override
 	public void run() {
 		if (highlightIteration >= HIGHLIGHT_ITERATION_COUNT) {
 			highlightIteration = 0;
 			future.cancel(true);
 			return;
 		}
 			
 		view.highlight();
 		
 		highlightIteration++;
 	}
 }
