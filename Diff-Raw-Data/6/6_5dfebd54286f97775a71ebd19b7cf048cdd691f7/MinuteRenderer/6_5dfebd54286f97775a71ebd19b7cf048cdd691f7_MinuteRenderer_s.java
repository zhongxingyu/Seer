  package com.sobinary.work;
 
 import java.util.Calendar;
 
 
 import com.sobinary.app.SmlInfoProvider;
 import com.sobinary.app.BigInfoProvider;
 import com.sobinary.app.BigClockProvider;
 import com.sobinary.base.Core;
 import com.sobinary.clockplus.R;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.PathEffect;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.graphics.Bitmap.Config;
 import android.graphics.Paint.Align;
 import android.graphics.PorterDuff.Mode;
 import android.os.BatteryManager;
 import android.preference.PreferenceManager;
 
 public class MinuteRenderer 
 {
 	/**********PSEUDO CONSTANT STYLE-PARAMS*************/
 	private float BIG_CIRC_STROKE_WIDTH = 6.0f;
 	private float SML_CIRC_STROKE_WIDTH = 4.0f;
 	private float GAUGE_TEXT_SIZE = 38f;
 	private float GAUGE_TEXT_STROKE_WIDTH = 1f;
 	private float HOUR_DOT_RAD = 10.0f;
 	private float MINUTE_DOT_RAD = 7.0f;
 	
 	private int minute, hour;
 	private float minuteRdn, hourRdn;
 	private float smallRad, smallRadFull, bigRad, bigRadFull, gaugeRad;
 	private boolean isVisibleBat, isVisibleWeath;
 	 
 	private Bitmap bmp;
 	private Canvas can;
 	private final Paint paint;
 	
 	private final Context cont;
 	private final SharedPreferences prefs;
 	private final Calendar now;
 	
 
 	
 	
 	
 	
 	
 	
 	
 	
 	/*******************************INITIALIZATION**************************************/
 	
 	
 	
 
 	
 	
 	
 	public MinuteRenderer(Context cont)
 	{
 		this.cont = cont;
 		this.prefs = PreferenceManager.getDefaultSharedPreferences(cont);
 		this.now = Calendar.getInstance();
 		
 		this.paint = new Paint();
 		reloadState();
 		initParams();
 	}  
 
 	public void initParams()
 	{
         final float MINUTES_PER_DAY = 60f * 12f;
 		this.minute = now.get(Calendar.MINUTE);
 		this.hour = now.get(Calendar.HOUR);
 		this.minuteRdn = (float)Math.toRadians((((float)minute / 60f) * 360f) - 90);
 		this.hourRdn =  (float)Math.toRadians((((float)((hour * 60f) + minute) / MINUTES_PER_DAY) * 360) - 90);
 	}
   
 	public void reloadState()
 	{
 		final float density = prefs.getFloat("sdens", 2.0f);
 		BIG_CIRC_STROKE_WIDTH *= density;
 		SML_CIRC_STROKE_WIDTH *= density;
 		GAUGE_TEXT_SIZE *= density;
 		GAUGE_TEXT_STROKE_WIDTH *= density;
 		HOUR_DOT_RAD *= density;
 		MINUTE_DOT_RAD *= density;
 		
 		smallRadFull = Float.parseFloat( Core.bleach(prefs.getString("ssize", "40")) ) * density;
 		smallRad = smallRadFull - SML_CIRC_STROKE_WIDTH/2;
 		
 		bigRadFull = Float.parseFloat( Core.bleach(prefs.getString("lsize", "240")) ) * density;
 		bigRad = bigRadFull - BIG_CIRC_STROKE_WIDTH/2;
 		
 		isVisibleBat =  prefs.getBoolean("showbat", true);
 		isVisibleWeath =  prefs.getBoolean("showweather", false);
 		
 		gaugeRad = (Float.parseFloat(prefs.getString("smallperc", "50f")) / 100f) * (bigRad / 2);
 	}
 
 	public Calendar getCalendar()
 	{
 		return this.now;
 	}
 
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/***************************************DRAWING*****************************************/
 	
 
 	
 	  
 	
 	
 	
 	
 	
 	
 	
 	
 	public void renderInfoWidget()
 	{
 		int sideLength = (int)(smallRadFull * 2);
 		this.bmp = Bitmap.createBitmap(sideLength, sideLength, Config.ARGB_8888);
 		this.can = new Canvas(bmp);
 		this.can.drawColor(0, Mode.CLEAR);
 		
 		paint.setAntiAlias(true);
   		paint.setColor(Color.parseColor( prefs.getString("clcol", "#ffffffff")));
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeWidth(SML_CIRC_STROKE_WIDTH);
 
 		float houArmLen = (smallRad - SML_CIRC_STROKE_WIDTH/2) * 0.68f;
 		float minArmLen = (smallRad - SML_CIRC_STROKE_WIDTH/2) * 0.90f;
 
 		float xHourArmTip = smallRadFull + houArmLen * (float)Math.cos(hourRdn);
 		float yHourArmTip = smallRadFull + houArmLen * (float)Math.sin(hourRdn);
 
 		float xMinArmTip = smallRadFull + minArmLen * (float)Math.cos(minuteRdn);
 		float yMinArmTip = smallRadFull + minArmLen * (float)Math.sin(minuteRdn);
 
 		can.drawCircle(smallRadFull, smallRadFull, smallRad, paint);
 		
 		can.drawLine(smallRadFull, smallRadFull, xMinArmTip, yMinArmTip, paint);
 		can.drawLine(smallRadFull, smallRadFull, xHourArmTip, yHourArmTip, paint);
 		
 		paint.setStyle(Paint.Style.FILL);
 		can.drawCircle(smallRadFull, smallRadFull, MINUTE_DOT_RAD, paint);
 		
 		BigInfoProvider.setImage(cont, bmp, R.id.clockimg);
 		SmlInfoProvider.setImage(cont, bmp, R.id.clockimg);
 	}
 	
 	public void renderClockWidget()
 	{
 		int sideLength = (int)(bigRadFull * 2);
 		this.bmp = Bitmap.createBitmap(sideLength, sideLength, Config.ARGB_8888);
 		this.can = new Canvas(bmp);
 		this.can.drawColor(0, Mode.CLEAR);
 
 		paint.setAntiAlias(true);
 		paint.setColor(Color.parseColor( prefs.getString("clcol", "#ffffffff")));
 		paint.setStyle(Paint.Style.FILL);
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeWidth(BIG_CIRC_STROKE_WIDTH);
 		
 		drawTime();
 		drawBattery();
 		drawWeather();
 		BigClockProvider.setImage(cont, bmp);
 	}
 
 	private void drawTime()
 	{
 		float minuteCenterDist = bigRad - BIG_CIRC_STROKE_WIDTH - gaugeRad;
 		float xGauge = bigRadFull + minuteCenterDist * (float)Math.cos(hourRdn);
 		float yGauge = bigRadFull + minuteCenterDist * (float)Math.sin(hourRdn);
 		
 		float hourArmLen = bigRad - BIG_CIRC_STROKE_WIDTH - 2 * gaugeRad;
 		float xHourArmTip = bigRadFull + hourArmLen * (float)Math.cos(hourRdn);
 		float yHourArmTip = bigRadFull + hourArmLen * (float)Math.sin(hourRdn);
 				
 		float minuteArmLen = gaugeRad - BIG_CIRC_STROKE_WIDTH/2 - MINUTE_DOT_RAD;
 		float xMinuteArmTip = xGauge + minuteArmLen * (float)Math.cos(minuteRdn);
 		float yMinuteArmTip = yGauge + minuteArmLen * (float)Math.sin(minuteRdn);
 
 		can.drawCircle(bigRadFull, bigRadFull, bigRad, paint);
 		can.drawCircle(xGauge, yGauge, gaugeRad, paint);
 		can.drawLine(bigRadFull, bigRadFull, xHourArmTip, yHourArmTip, paint);
 		can.drawLine(xGauge, yGauge, xMinuteArmTip, yMinuteArmTip, paint);
 		
 		paint.setStyle(Paint.Style.FILL_AND_STROKE);
 		paint.setStrokeWidth(0);
 		can.drawCircle(xMinuteArmTip, yMinuteArmTip, MINUTE_DOT_RAD, paint);
 		can.drawCircle(xGauge, yGauge, MINUTE_DOT_RAD, paint);
 		can.drawCircle(bigRadFull, bigRadFull, HOUR_DOT_RAD, paint);
 	}
 	
 	private void drawWeather()
 	{
 		if(!isVisibleWeath) return;
 
 		String txt; float endAngle;
 		
 		long prev = prefs.getLong("lastweather", 0l);
 		long now = System.currentTimeMillis();
 		long interval = Long.parseLong(Core.bleach(prefs.getString("weatherinterval", "60l"))) * 60 * 1000;
 		
 		if(now - prev > interval)
 		{
 			float[]data = WeatherService.getQuickLook(cont);
 			
 			if(data != null)
 			{
 				txt = (int)data[0] + "°";
 				endAngle = data[1] * 360;
 				prefs.edit().putString("lastweathval", txt).commit();
 				prefs.edit().putFloat("lastweathrat", endAngle).commit();
 				prefs.edit().putLong("lastweather", now).commit();
 			}
 			else 
 			{
 				txt = "?°";
 				endAngle = 180f;
 			}
 		}
 		else
 		{
 			txt = prefs.getString("lastweathval", "?°");
 			endAngle = prefs.getFloat("lastweathrat", 180f);
 		}
 		drawGauge(endAngle, false, txt);
 	}
 	
 	private void drawBattery()
 	{
 		if(!isVisibleBat) return;
 		
 		String txt; float endAngle;
 		
 		try
 		{
 			Intent intent = cont.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 			float nom = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
 			float denom = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
 			endAngle = (nom/denom) * 360;
 			int pretty = (int)((nom/(denom/100f)));
 			txt = pretty + "%";
 		}
 		catch(Exception e)
 		{
 			Core.print("Battery fail");
 			endAngle = 180; 
 			txt = "...";
 		}
 		drawGauge(endAngle, true, txt);
 	}
 	
 	private void drawGauge(float dashAng, boolean hor, String text)
 	{
		float hourRdnTwo = hor ? (float)Math.toRadians( (hour >= 6) ? 0 : 180 ) :
			(float)Math.toRadians( (hour >= 3 && hour <= 9) ? 270 : 90 );
	  
 		float minuteCenterDist = bigRad - BIG_CIRC_STROKE_WIDTH - gaugeRad;
 		float xGauge = bigRadFull + minuteCenterDist * (float)Math.cos(hourRdnTwo);
 		float yGauge = bigRadFull + minuteCenterDist * (float)Math.sin(hourRdnTwo);
 		 
 		RectF frame = new RectF();
 		frame.bottom = yGauge + gaugeRad;
 		frame.top = yGauge - gaugeRad;
 		frame.left = xGauge - gaugeRad;
 		frame.right = xGauge + gaugeRad;
 		
 		paint.setStrokeWidth(BIG_CIRC_STROKE_WIDTH);
 		PathEffect old = paint.getPathEffect();
 		paint.setStyle(Paint.Style.STROKE);
 		can.drawArc(frame, 270, dashAng, false, paint);
 		paint.setPathEffect(new DashPathEffect(new float[] {4,4}, 0));
 		can.drawArc(frame, 270+dashAng, 360-dashAng, false, paint);
 
 		paint.setTextSize(GAUGE_TEXT_SIZE);
 		paint.setStrokeWidth(GAUGE_TEXT_STROKE_WIDTH);
 		paint.setTextAlign(Align.CENTER);
 		paint.setPathEffect(old);
 		paint.setStyle(Paint.Style.FILL);
 		paint.setTypeface(Typeface.DEFAULT_BOLD);
 
 		Rect txtRect = new Rect();
 		paint.getTextBounds("29%", 0, 3, txtRect);
 		can.drawText(text, xGauge, yGauge - (txtRect.top*0.46f), paint);
 	}
 	
 }
