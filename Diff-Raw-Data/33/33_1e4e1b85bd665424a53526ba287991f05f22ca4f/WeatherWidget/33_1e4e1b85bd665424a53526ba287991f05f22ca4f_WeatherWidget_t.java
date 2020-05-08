 package com.jsr.controller;
 
import java.util.List;

import org.sdu.dao.TaskDao;
 import org.sdu.gis.R;
import org.sdu.pojo.Task;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.text.format.DateFormat;
 import android.text.format.Time;
 import android.widget.RemoteViews;
 
 import com.jsr.model.DataForecastService;
 import com.jsr.model.DataTimeService;
 import com.jsr.model.DataWidget;
 import com.jsr.model.LoadDataNet;
 import com.jsr.model.WeatherUtil;
 
 public class WeatherWidget extends AppWidgetProvider { 
     private static Time TIME_WIDGET=new Time();
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		// TODO Auto-generated method stub
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 		DataForecastService.addWidgetIDs(appWidgetIds);
 		context.startService(new Intent(context,DataForecastService.class));
 		context.startService(new Intent(context,DataTimeService.class));
 	}
 	public static RemoteViews updataViews(Context context,Uri uri){
 		System.out.println("WeatherWidget RemoteViews is running");
 		RemoteViews views=new RemoteViews(context.getPackageName(), R.layout.weatherwidget);
 		DataWidget dataWidget=LoadDataNet.getDataWidget(context, uri, true);
 		if(dataWidget!=null){
 			System.out.println("update View from dataWidget");
 			views.setImageViewResource(R.id.forecastImage, WeatherUtil.getForecastImage(dataWidget.getIcon(), WeatherUtil.isDaytime()));
 			views.setTextViewText(R.id.cityText, dataWidget.getCity());
 			views.setTextViewText(R.id.conditionText, dataWidget.getCondition());
 			views.setTextViewText(R.id.tempCText, dataWidget.getTempc().toString()+"");
 					
 			views.setTextViewText(R.id.hightText, "H:"+ dataWidget.getDetails().get(0).getHight().toString()+"");
 				
 			views.setTextViewText(R.id.lowText, "L:"+ dataWidget.getDetails().get(0).getLow().toString()+"");
 					
 		}
 
 		 // Connect click intent to launch details dialog
         Intent detailIntent = new Intent(context, DetailForecastActivity.class);
         detailIntent.setData(uri);
         PendingIntent pending = PendingIntent.getActivity(context, 0, detailIntent, 0);
         views.setOnClickPendingIntent(R.id.forecastImage, pending);
         
         
         Intent detailIntent1 = new Intent(context, DetailForecastActivity.class);
         detailIntent1.setData(uri);
         PendingIntent pending1 = PendingIntent.getActivity(context, 0, detailIntent, 0);
         views.setOnClickPendingIntent(R.id.cityText, pending1);
         
         Intent detailIntent2 = new Intent(context, DetailForecastActivity.class);
         detailIntent2.setData(uri);
         PendingIntent pending2 = PendingIntent.getActivity(context, 0, detailIntent, 0);
         views.setOnClickPendingIntent(R.id.conditionText, pending2);
         
         Intent detailIntent3 = new Intent(context, DetailForecastActivity.class);
         detailIntent3.setData(uri);
         PendingIntent pending3 = PendingIntent.getActivity(context, 0, detailIntent, 0);
         views.setOnClickPendingIntent(R.id.dateText, pending3);
         
         Intent detailIntent4 = new Intent(context, DetailForecastActivity.class);
         detailIntent4.setData(uri);
         PendingIntent pending4 = PendingIntent.getActivity(context, 0, detailIntent, 0);
         views.setOnClickPendingIntent(R.id.dayText, pending4);
         		
         Intent intentminute01 = new Intent();     
         intentminute01.setClassName("com.android.alarmclock", "com.android.alarmclock.AlarmClock");
 		PendingIntent pendingIntent01 = PendingIntent.getActivity(context, 0,
 				intentminute01, 0);
 		views.setOnClickPendingIntent(R.id.minute01Image, pendingIntent01);
 		
 		Intent intentminute02 = new Intent();
 		intentminute02.setClassName("com.android.alarmclock", "com.android.alarmclock.AlarmClock");
 		PendingIntent pendingIntent02 = PendingIntent.getActivity(context, 0,
 				intentminute02, 0);
 		views.setOnClickPendingIntent(R.id.minute02Image, pendingIntent02);
 		
 		Intent intenthour01 = new Intent();     
 		intenthour01.setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
 		PendingIntent pendingIntent03 = PendingIntent.getActivity(context, 0,
 				intenthour01, 0);
 		views.setOnClickPendingIntent(R.id.hour01Image, pendingIntent03);
 		
 		Intent intenthour02 = new Intent();
 		intenthour02.setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
 		PendingIntent pendingIntent04 = PendingIntent.getActivity(context, 0,
 				intenthour02, 0);
 		views.setOnClickPendingIntent(R.id.hour02Image, pendingIntent04);
 		
 		Intent intenlow = new Intent();
 		intenlow.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
 		PendingIntent pendingIntent05 = PendingIntent.getActivity(context, 0,
 				intenlow, 0);
 		views.setOnClickPendingIntent(R.id.lowText, pendingIntent05);
 		
 		Intent intenhight = new Intent();
 		intenhight.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
 		PendingIntent pendingIntent06 = PendingIntent.getActivity(context, 0,
 				intenhight, 0);
 		views.setOnClickPendingIntent(R.id.hightText, pendingIntent06);
 		
 		Intent intennow = new Intent();
 		intennow.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
 		PendingIntent pendingIntent07 = PendingIntent.getActivity(context, 0,
 				intennow, 0);
 		views.setOnClickPendingIntent(R.id.tempCText, pendingIntent07);
 		
 		return views;
 	}
 	static int id=1;
 	public static RemoteViews updateTime(Context context) {
 		// TODO Auto-generated method stub
 		RemoteViews views = new RemoteViews(context.getPackageName(),
 				R.layout.weatherwidget);
 		
 		TIME_WIDGET.setToNow();
 		int hour01 = TIME_WIDGET.hour / 10;
 		int hour02 = TIME_WIDGET.hour % 10;
 		int minute01 = TIME_WIDGET.minute / 10;
 		int minute02 = TIME_WIDGET.minute % 10;
 		int dayOfWeek = TIME_WIDGET.weekDay;
 		CharSequence dt = DateFormat.format(context.getString(R.string.dateFormat), TIME_WIDGET.toMillis(false));
 		
 		views.setTextViewText(R.id.dateText, dt);		
 		views.setTextViewText(R.id.dayText, WeatherUtil.getDayofWeek(context, dayOfWeek));
 		views.setImageViewResource(R.id.hour01Image, WeatherUtil.getImageNumber(hour01));
 		views.setImageViewResource(R.id.hour02Image, WeatherUtil.getImageNumber(hour02));
 		views.setImageViewResource(R.id.minute01Image, WeatherUtil.getImageNumber(minute01));
 		views.setImageViewResource(R.id.minute02Image, WeatherUtil.getImageNumber(minute02));
		TaskDao tdao=new TaskDao(context);
		List<Task> lst=tdao.find();
		if(id % 2==0){
 		    RemoteViews subViews = new RemoteViews(context.getPackageName(),R.layout.tv2);
 		    views.removeAllViews(R.id.message);
 		    views.addView(R.id.message, subViews);
		    views.setTextViewText(R.id.data, ""+lst.size());
		    id ++;
 		}else{
 		    RemoteViews subViews = new RemoteViews(context.getPackageName(),R.layout.tv1);
 		    views.removeAllViews(R.id.message);
 		    views.addView(R.id.message, subViews); 
		    views.setTextViewText(R.id.data, ""+lst.size());
		    id ++;
 		}
 		return views;
 	}
 
 }
