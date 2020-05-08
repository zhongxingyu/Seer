 package com.kg6.schlafdoedel.custom;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 import com.kg6.schlafdoedel.event.Event;
 import com.kg6.schlafdoedel.event.EventScheduler;
import com.kg6.schlafdoedelmaster.R;
 
 public class DigitalClock extends View {
 	private final int TIMER_SLEEPTIME = 1000;
 	
 	private final int MINIMUM_WIDTH = 440;
 	private final int MINIMUM_HEIGHT = 120;
 	
 	private final int VIEW_TOP_MARGIN = 20;
 	private final int VIEW_LEFT_MARGIN = 20;
 	private final int VIEW_DIGITS_WIDTH = 50;
 	private final int VIEW_DIGITS_HEIGHT = 110;
 	private final int VIEW_DIGITS_SPACE = 5;
 	
 	private final Paint DIGIT_PAINT;
 	
 	private Timer timer;
 	private Bitmap[] digits;
 	private Bitmap spacer;
 	
 	private EventScheduler eventScheduler;
 	
 	public DigitalClock(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		DIGIT_PAINT = new Paint();
 		DIGIT_PAINT.setColor(Color.WHITE);
 		DIGIT_PAINT.setFlags(Paint.ANTI_ALIAS_FLAG);
 		DIGIT_PAINT.setTextSize(25);
 		
 		this.eventScheduler = null;
 		
 		setMinimumWidth(MINIMUM_WIDTH);
 		setMinimumHeight(MINIMUM_HEIGHT);
 		
 		loadDigits();
 		
 		startTimer();
 	}
 	
 	public void setEventScheduler(EventScheduler eventScheduler) {
 		this.eventScheduler = eventScheduler;
 	}
 
 	private void loadDigits() {
 		this.digits = new Bitmap[] {
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_0),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_1),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_2),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_3),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_4),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_5),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_6),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_7),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_8),
 			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_9),
 		};
 		
 		this.spacer = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.alarmclock_spacer);
 	}
 
 	private void startTimer() {
 		TimerTask task = new TimerTask() {
 			
 			@Override
 			public void run() {
 				postInvalidate();
 			}
 		};
 		
 		this.timer = new Timer();
 		this.timer.scheduleAtFixedRate(task, 0, TIMER_SLEEPTIME);
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		try {
 			Calendar calendar = Calendar.getInstance();
 			final int hour = calendar.get(Calendar.HOUR_OF_DAY);
 			final int minute = calendar.get(Calendar.MINUTE);
 			final int second = calendar.get(Calendar.SECOND);
 			
 			//hour
 			int xPos = VIEW_LEFT_MARGIN;
 			canvas.drawBitmap(this.digits[hour / 10], xPos, VIEW_TOP_MARGIN, DIGIT_PAINT);
 			
 			xPos += VIEW_DIGITS_WIDTH + VIEW_DIGITS_SPACE;
 			canvas.drawBitmap(this.digits[hour % 10], xPos, VIEW_TOP_MARGIN, DIGIT_PAINT);
 			
 			//spacer
 			xPos += VIEW_DIGITS_WIDTH + VIEW_DIGITS_SPACE;
 			canvas.drawBitmap(this.spacer, xPos, VIEW_TOP_MARGIN, DIGIT_PAINT);
 			
 			//minute
 			xPos += VIEW_DIGITS_WIDTH + VIEW_DIGITS_SPACE;
 			canvas.drawBitmap(this.digits[minute / 10], xPos, VIEW_TOP_MARGIN, DIGIT_PAINT);
 			
 			xPos += VIEW_DIGITS_WIDTH + VIEW_DIGITS_SPACE;
 			canvas.drawBitmap(this.digits[minute % 10], xPos, VIEW_TOP_MARGIN, DIGIT_PAINT);
 			
 			//spacer
 			xPos += VIEW_DIGITS_WIDTH + VIEW_DIGITS_SPACE;
 			canvas.drawBitmap(this.spacer, xPos, VIEW_TOP_MARGIN, DIGIT_PAINT);
 			
 			//second
 			xPos += VIEW_DIGITS_WIDTH + VIEW_DIGITS_SPACE;
 			canvas.drawBitmap(this.digits[second / 10], xPos, VIEW_TOP_MARGIN, DIGIT_PAINT);
 			
 			xPos += VIEW_DIGITS_WIDTH + VIEW_DIGITS_SPACE;
 			canvas.drawBitmap(this.digits[second % 10], xPos, VIEW_TOP_MARGIN, DIGIT_PAINT);
 			
 			//upcoming event
 			if(this.eventScheduler != null) {
 				Event event = this.eventScheduler.getNextUpcomingEvent();
 				
 				if(event != null) {
 					String date = Util.GetPrintableTimeOfDay(event.getStart());
 					String text = String.format("Next event: %s", date);
 					final float textWidth = DIGIT_PAINT.measureText(text);
 					
 					canvas.drawText(text, MINIMUM_WIDTH / 2 - textWidth / 2, VIEW_DIGITS_HEIGHT + VIEW_DIGITS_SPACE, DIGIT_PAINT);
 				}
 			}
 		} catch (Exception e) {
 			Log.e("DigitalClock.java", "Unable to draw digital clock", e);
 		}
 	}
 }
