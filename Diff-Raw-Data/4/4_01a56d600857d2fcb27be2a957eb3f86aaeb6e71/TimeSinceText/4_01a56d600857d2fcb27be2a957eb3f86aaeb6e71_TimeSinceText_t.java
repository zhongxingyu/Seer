 package uk.co.thomasc.wordmaster.view;
 
 import android.content.Context;
 import android.util.AttributeSet;
 
 import uk.co.thomasc.wordmaster.util.TimeUtil;
 
 public class TimeSinceText extends RussoText {
 	
 	private long timestamp = TimeUtil.now();
 	private boolean running = true;
 	private TimerThread thread;
 
 	public TimeSinceText(Context context) {
 		super(context);
 		init();
 	}
 	
 	public TimeSinceText(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	public TimeSinceText(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	private void init() {
 		thread = new TimerThread();
 	}
 	
 	@Override
 	protected void onDetachedFromWindow() {
 		running = false;
 	}
 	
 	@Override
 	protected void onAttachedToWindow() {
 		running = true;
 		thread.start();
 	}
 	
 	private class TimerThread extends Thread {
 		
 		@Override
 		public void run() {
 			while (running) {
 				post(new Runnable() {
 					@Override
 					public void run() {
 						setText(TimeUtil.timeSince(timestamp));
 					}
 				});
 				try {
 					Thread.sleep(TimeUtil.sleepTime(timestamp));
 				} catch (InterruptedException e) {
					// Ignore this exception, it's used to refresh the text
 				}
 			}
 		}
 		
 	}
 
 	public void setTimestamp(long lastUpdateTimestamp) {
 		this.timestamp = lastUpdateTimestamp;
		thread.interrupt();
 	}
 	
 }
