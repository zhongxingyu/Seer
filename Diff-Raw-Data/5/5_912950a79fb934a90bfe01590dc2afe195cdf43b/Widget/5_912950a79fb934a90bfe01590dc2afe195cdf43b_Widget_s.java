 package by.fksis.schedule.widget;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.SystemClock;
 import android.widget.RemoteViews;
 import by.fksis.schedule.L;
 import by.fksis.schedule.Preferences;
 import by.fksis.schedule.R;
 import by.fksis.schedule.Util;
 import by.fksis.schedule.app.MainActivity;
 import by.fksis.schedule.dal.ScheduleClass;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 public class Widget extends AppWidgetProvider {
     public static int WIDGET_ID = 1;
     private static DateFormat sdf_all = new SimpleDateFormat("yyyy-MM-dd hh:mm");
     private static DateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
 
     @Override
     public void onEnabled(Context context) {
         Intent updaterIntent = new Intent();
         updaterIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
         updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{WIDGET_ID});
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, updaterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 1000, pendingIntent);
         super.onEnabled(context);
     }
 
     @Override
     public void onDeleted(Context context, int[] ids) {
         for (int id : ids) {
             Intent intent = new Intent();
             intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
             intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{id});
             PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
             AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
             alarmManager.cancel(pendingIntent);
         }
         super.onDeleted(context, ids);
     }
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         Intent appLaunch = new Intent(context, MainActivity.class);
         appLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         PendingIntent appLaunchPending = PendingIntent.getActivity(context, 0, appLaunch, 0);
 
         Calendar time = Calendar.getInstance();
 
         ComponentName thisWidget = new ComponentName(context, Widget.class);
         int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
         int weekNumber = Util.getScheduleWeek(time.getTime());
         int dayOfWeek = Util.getDayOfWeekIndex(time);
 
         for (int widgetId : allWidgetIds) {
             RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);
             remoteViews.setOnClickPendingIntent(R.id.widget, appLaunchPending);
             remoteViews.setTextViewText(R.id.widget_current, context.getString(R.string.no_classes));
             remoteViews.setTextViewText(R.id.widget_next, context.getString(R.string.no_classes));
 
             time.setTimeInMillis(System.currentTimeMillis());
             time.set(Calendar.HOUR, 0);
             time.set(Calendar.MINUTE, 00);
             time.set(Calendar.AM_PM, Calendar.AM);
             time.add(Calendar.DATE, 1);
             try {
                 List<ScheduleClass> classes = ScheduleClass.get(ScheduleClass.class)
                         .filter("weeks%", "%" + weekNumber + "%")
                         .filter("day", dayOfWeek)
                         .filter("studentGroup", new Preferences(context).getGroup())
                         .filter("subgroups%", "%" + new Preferences(context).getSubgroupString() + "%")
                         .list();
 
                 if ((classes != null) && (classes.size() > 0)) {
                     boolean found = false;
                     for (Iterator<ScheduleClass> i = classes.iterator(); i.hasNext() && !found; ) {
                         ScheduleClass l = i.next();
                         Date dateStart = sdf_all.parse(sdf_date.format(Calendar.getInstance().getTime()) + " " + context.getResources().getStringArray(R.array.timeSlotStart)[l.timeSlot]);
                         Date dateEnd = sdf_all.parse(sdf_date.format(Calendar.getInstance().getTime()) + " " + context.getResources().getStringArray(R.array.timeSlotEnd)[l.timeSlot]);
                         if ((dateStart.getTime() <= Calendar.getInstance().getTime().getTime())
                                 && (dateEnd.getTime() >= Calendar.getInstance().getTime().getTime())) {
                             remoteViews.setTextViewText(R.id.widget_current, l.name + " " + ((l.room != null) ? l.room : ""));
                             time.setTime(dateEnd);
                             if (i.hasNext()) {
                                 ScheduleClass l_next = i.next();
                                remoteViews.setTextViewText(R.id.widget_next, l_next.name + l_next.room);
                             } else {
                                 remoteViews.setTextViewText(R.id.widget_next, context.getString(R.string.no_classes));
                             }
                             found = true;
                         }
                     }
 
                     if (!found) {
                         found = false;
                         for (Iterator<ScheduleClass> i = classes.iterator(); i.hasNext() && !found; ) {
                             ScheduleClass l = i.next();
                             Date dateStart = sdf_all.parse(sdf_date.format(Calendar.getInstance().getTime()) + " " + context.getResources().getStringArray(R.array.timeSlotStart)[l.timeSlot]);
                             Date dateEnd = sdf_all.parse(sdf_date.format(Calendar.getInstance().getTime()) + " " + context.getResources().getStringArray(R.array.timeSlotEnd)[l.timeSlot]);
                             if (dateStart.getTime() > Calendar.getInstance().getTime().getTime()) {
                                 found = true;
                                 remoteViews.setTextViewText(R.id.widget_current, context.getString(R.string.no_classes));
                                remoteViews.setTextViewText(R.id.widget_next, l.name + " " + ((l.room != null) ? l.room : ""));
                                 time.setTime(dateStart);
                             }
                         }
                     }
                 }
             } catch (Exception e) {
                 L.d(e.getMessage());
             } finally {
                 appWidgetManager.updateAppWidget(widgetId, remoteViews);
             }
         }
 
         for (int id : appWidgetIds) {
             Intent intent = new Intent();
             intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
             intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{id});
             PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
             AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
             alarmManager.cancel(pendingIntent);
             alarmManager.set(AlarmManager.RTC, time.getTimeInMillis(), pendingIntent);
         }
 
         super.onUpdate(context, appWidgetManager, appWidgetIds);
     }
 }
