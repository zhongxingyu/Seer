 /*
  * Copyright (C) 2013-2014 Scott Warner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.tortel.deploytrack.provider;
 
 import java.util.List;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 
 import com.tortel.deploytrack.Log;
 import com.tortel.deploytrack.Prefs;
 import com.tortel.deploytrack.R;
 import com.tortel.deploytrack.data.*;
 
 import android.annotation.TargetApi;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.RectF;
 import android.graphics.Region;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.RemoteViews;
 
 public class WidgetProvider extends AppWidgetProvider {
     private static final String UPDATE_INTENT = "com.tortel.deploytrack.WIDGET_UPDATE";
     
     private static final int DEFAULT_SIZE = 200;
     private static final float PADDING = 0.5f;
     private static final int THICKNESS = 75;
     
     @Override
     public void onReceive(Context context, Intent intent) {
         super.onReceive(context, intent);
         Log.v("Update intent received");
         AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
         onUpdate(context, widgetManager, new int[0]);
     }
 
     @TargetApi(19)
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager,
             int[] appWidgetIds){
         List<WidgetInfo> infoList = DatabaseManager.getInstance(context).getAllWidgetInfo();
     	
         for(WidgetInfo info : infoList){
         	int widgetId = info.getId();
 
             Log.d("Updating widget "+widgetId);
             
             RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                     R.layout.widget_layout);
 
             Log.d("Widget "+info.getId()+" with deployment "+info.getDeployment().getId());
 
             //Draw everything
             remoteViews = updateWidgetView(context, remoteViews, info);
 
             
             //Update it
             try{
             	appWidgetManager.updateAppWidget(widgetId, remoteViews);
             } catch(Exception e){
             	/*
             	 * Catching all exceptions, because I suspect that if a widget has been deleted,
             	 * yet not removed from the database, it will still try to update it and probably cause
             	 * some sort of exception. So Ill just go ahead and keep the app from crashing.
             	 */
             	Log.e("Uhoh!",e);
             }
         }
         
         //Schedule an update at midnight
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         DateTime now = new DateTime();
         DateMidnight tomorrow = new DateMidnight(now.plusDays(1));
         
         PendingIntent pending = PendingIntent.getBroadcast(context, 0, 
                 new Intent(UPDATE_INTENT), PendingIntent.FLAG_UPDATE_CURRENT);
         
         //Adding 100msec to make sure its triggered after midnight
         Log.d("Scheduling update for "+tomorrow.getMillis() + 100);
         
         if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
             alarmManager.setExact(AlarmManager.RTC, tomorrow.getMillis() + 100, pending);
         } else {
             alarmManager.set(AlarmManager.RTC, tomorrow.getMillis() + 100, pending);
         }
     }
     
     @Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         //Remove them from the database
         for(int id: appWidgetIds){
             DatabaseManager.getInstance(context).deleteWidgetInfo(id);
         }
         
         super.onDeleted(context, appWidgetIds);
     }
     
     /**
      * Sets up and fills the RemoteViews with the data provided in the WidgetInfo class
      * @param context
      * @param remoteViews
      * @param info
      */
     public static RemoteViews updateWidgetView(Context context, RemoteViews remoteViews, WidgetInfo info){
         Deployment deployment = info.getDeployment();
         Resources resources = context.getResources();
         Prefs.load(context);
         
         // Set the text
         remoteViews.setTextViewText(R.id.widget_percent, deployment.getPercentage()+"%");
         remoteViews.setTextViewText(R.id.widget_name, deployment.getName());
         remoteViews.setTextViewText(
                 R.id.widget_info,
                 resources.getQuantityString(R.plurals.days_remaining,
                         deployment.getRemaining(), deployment.getRemaining()));
         remoteViews.setImageViewBitmap(R.id.widget_pie, getChartBitmap(deployment, DEFAULT_SIZE));
 
         // Apply hide preferences
         if(Prefs.hideDate()){
             remoteViews.setViewVisibility(R.id.widget_info, View.GONE);
        } else {
            remoteViews.setViewVisibility(R.id.widget_info, View.VISIBLE);
         }
         
         if(Prefs.hidePercent()){
             remoteViews.setViewVisibility(R.id.widget_percent, View.GONE);
        } else {
            remoteViews.setViewVisibility(R.id.widget_percent, View.VISIBLE);
         }
         
         // Register an onClickListener
         Intent intent = new Intent(context, WidgetProvider.class);
 
         intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
         int array[] = new int[1];
         array[0] = info.getId();
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, array);
 
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
         remoteViews
                 .setOnClickPendingIntent(R.id.widget_pie, pendingIntent);
         
         return remoteViews;
     }
     
     public static Bitmap getChartBitmap(Deployment deployment, int size){
     	//Set up the pie chart image
         Bitmap.Config conf = Bitmap.Config.ARGB_8888;
         Bitmap bmp = Bitmap.createBitmap(size,size,conf);
         Canvas canvas = new Canvas(bmp);
     	
     	canvas.drawColor(Color.TRANSPARENT);
 		Paint paint = new Paint();
 		paint.setAntiAlias(true);
 		Path p = new Path();
 		
 		RectF rect = new RectF();
 		Region region = new Region();
 		
 		//Reset variables
 	    float currentAngle = 270;
 	    float currentSweep = 0;
 	    float totalLength = deployment.getCompleted() + deployment.getRemaining();
 		
 		float midX = size / 2f;
 		float midY = size / 2f;
 		float radius;
 		if (midX < midY){
 			radius = midX;
 		} else {
 			radius = midY;
 		}
 		radius -= PADDING;
 		float innerRadius = radius - THICKNESS;
 		
 		// Draw completed
 		if(deployment.getCompleted() > 0){
 			p.reset();
 			paint.setColor(deployment.getCompletedColor());
 			currentSweep = (deployment.getCompleted() / totalLength)*(360);
 			rect.set(midX-radius, midY-radius, midX+radius, midY+radius);
 			p.arcTo(rect, currentAngle+PADDING, currentSweep - PADDING);
 			
 			rect.set(midX-innerRadius, midY-innerRadius, midX+innerRadius, midY+innerRadius);
 			p.arcTo(rect, (currentAngle+PADDING) + (currentSweep - PADDING), -(currentSweep-PADDING));
 			p.close();
 			
 			region.set((int)(midX-radius), (int)(midY-radius), (int)(midX+radius), (int)(midY+radius));
 			canvas.drawPath(p, paint);
 			
 			currentAngle = currentAngle+currentSweep;
 		}
 		
 		// Draw remaining
 		if(deployment.getCompleted() > 0){
 			p.reset();
 			paint.setColor(deployment.getRemainingColor());
 			currentSweep = (deployment.getRemaining() / totalLength)*(360);
 			rect.set(midX-radius, midY-radius, midX+radius, midY+radius);
 			p.arcTo(rect, currentAngle+PADDING, currentSweep - PADDING);
 			
 			rect.set(midX-innerRadius, midY-innerRadius, midX+innerRadius, midY+innerRadius);
 			p.arcTo(rect, (currentAngle+PADDING) + (currentSweep - PADDING), -(currentSweep-PADDING));
 			p.close();
 			
 			region.set((int)(midX-radius), (int)(midY-radius), (int)(midX+radius), (int)(midY+radius));
 			canvas.drawPath(p, paint);
 			
 			currentAngle = currentAngle+currentSweep;
 		}
 
 		return bmp;
     }
 
     
     @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     public void onAppWidgetOptionsChanged(Context context,
             AppWidgetManager appWidgetManager, int appWidgetId,
             Bundle newOptions) {
         // TODO Auto-generated method stub
         super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                 newOptions);
     }
 }
