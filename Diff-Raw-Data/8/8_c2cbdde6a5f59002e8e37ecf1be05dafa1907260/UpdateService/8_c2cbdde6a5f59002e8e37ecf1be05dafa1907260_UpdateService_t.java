 package net.blogjava.aquarium.digitalclock;
 
 import java.util.Date;
 import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
 
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Paint.Align;
 import android.graphics.PorterDuff.Mode;
 import android.os.IBinder;
 import android.text.format.DateFormat;
 import android.text.format.Time;
 import android.widget.RemoteViews;
 import android.widget.ScrollView;
 
 public class UpdateService extends Service {
 	
 	private static Canvas sCanvas;
 	
 	private static boolean sScreenOn;
 	
 	private static Typeface sTypeface;
 	
 	private static Bitmap sSurface;
 
 	
 	private BroadcastReceiver mIntentReceiver;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
 		intentFilter.addAction(Intent.ACTION_TIME_TICK);
 		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
 		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
 		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
 		
 		mIntentReceiver = new BroadcastReceiver() {
 			
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()))
 					sScreenOn = false;
 				else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()))
 					sScreenOn = true;
 				
 				if (sScreenOn) 
 					updateAppWidget(context);
 			}
 		};
 		
 		registerReceiver(mIntentReceiver, intentFilter);
 		
 		
 	}
 	
 	
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		
 		updateAppWidget(this);
 	}
 	
 	
 	public static void updateAppWidget(Context context) {
 		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.digital_clock);
 		
 		views.setImageViewBitmap(R.id.date_time, createDateTimeBitmap(context));
 		
 		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 		
 		appWidgetManager.updateAppWidget(new ComponentName(context, DigitalClockAppWidgetProvider.class), views);
 	}
 	
 	
 	private static Bitmap createDateTimeBitmap(Context context) {
 		Resources resources = context.getResources();
 		
 		final float timeFontSize = resources.getDimension(R.dimen.time_font_size);
 		final float timePosY = resources.getDimension(R.dimen.time_pos_y);
 		
 		final float dateFontSize = resources.getDimension(R.dimen.date_font_size);
 		final float datePosY = resources.getDimension(R.dimen.date_pos_y);
 		
 		final float ampmCanvasPosY = resources.getDimension(R.dimen.ampm_canvas_pos_y);
 		final float ampmCanvasPosX = resources.getDimension(R.dimen.ampm_canvas_pos_x);
 		
 		final float ampmFontSize = resources.getDimension(R.dimen.ampm_font_size);
 		
 		final String am = "AM";
 		final String pm = "PM";	
 			
 		final int canvasWidth = (int)resources.getDimension(R.dimen.canvas_width);
 		final int canvasHeight = (int)resources.getDimension(R.dimen.canvas_height);
 		
 		
		if (sSurface == null) {
 			Config config = Config.ARGB_4444;
 			
 			sSurface = Bitmap.createBitmap(canvasWidth, canvasHeight, config);
 			
 			sCanvas = new Canvas(sSurface);
 			
 			Paint paint = new Paint();
 			paint.setAntiAlias(true);
 			
 			paint.setTextSize(dateFontSize);
 			
			paint.setARGB(0xff, 0xff, 0xff, 0xff);
 			
 			paint.setColor(0xff000000);
 			
 			paint.setTextAlign(Align.CENTER);
 			
 			paint.setShadowLayer(1.0f, 0.0f, 0.0f, 0x100);
 			
 			
 			Paint blackPaint = new Paint();
 			
 			blackPaint.setARGB(0xff, 0x80, 0x80, 0x80);
 			
 			Date date = new Date();
 			
 			java.text.DateFormat format = DateFormat.getDateFormat(context);
 			
 			String sDate = format.format(date);
 			
 			Bitmap maskDateImage = createMaskImage(context, R.drawable.digitalclock_mask_date_icn);
 			
 			Paint dstPaint = getMaskPaint();
 			
 			float centerWidth = (float) canvasWidth / 2;
 			
 			int dateWidth = (int) paint.measureText(sDate);
 			
 			Rect dateRect = new Rect((int) (centerWidth - dateWidth / 2), (int) timePosY, (int) (centerWidth + dateWidth / 2), (int)datePosY);
 			
 //			Rect timeRect = 
 			
 			paint.setTypeface(Typeface.SANS_SERIF);
 			
 			sCanvas.drawText(sDate, centerWidth - dateWidth / 2, datePosY, paint);
 			
 			if (maskDateImage != null) {
 //				sCanvas.drawBitmap(bitmap, src, dst, dstPaint);
 				
 				
 			}
 				
		}
 		
 		return sSurface;
 	}
 	
 	private static Bitmap createMaskImage(Context context, int maskImageResourceId) {
 		Options opts = new Options();
 		opts.inPreferredConfig = Config.ALPHA_8;
 		Bitmap maskImage = BitmapFactory.decodeResource(context.getResources(), maskImageResourceId, opts);
 		return maskImage;
 	}
 	
 	private static Paint getMaskPaint() {
 		Paint dstOutPaint = new Paint();
 		
 		dstOutPaint.setAntiAlias(true);
 		
 		dstOutPaint.setDither(true);
 		dstOutPaint.setFilterBitmap(true);
 		
 		dstOutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
 		
 		return dstOutPaint;
 	}
 }
