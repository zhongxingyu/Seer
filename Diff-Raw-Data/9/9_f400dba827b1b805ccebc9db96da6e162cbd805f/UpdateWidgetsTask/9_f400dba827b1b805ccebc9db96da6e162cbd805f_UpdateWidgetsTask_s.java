 /*
  *
  *  * Copyright (c) 2012 Aleksey Zhidkov. All Rights Reserved.
  *  
  */
 
 package ru.jdev.qd.tasks;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.View;
 import android.widget.RemoteViews;
 import ru.jdev.qd.QdWidgetProvider;
 import ru.jdev.qd.R;
 import ru.jdev.qd.services.TapListenerService;
 import ru.jdev.qd.Utils;
 import ru.jdev.qd.model.ContactInfo;
 import ru.jdev.qd.model.Page;
 import ru.jdev.qd.model.Pager;
 
 import java.io.InputStream;
 
 /**
  * User: jdev
  * Date: 28.01.12
  */
 public class UpdateWidgetsTask implements Runnable {
 
     private static final String TAG = "QD.UW";
 
     private static final int[][] mostUsedLabelIds = {
             {R.id.mu_1_label, R.id.mu_2_label, R.id.mu_3_label, R.id.mu_4_label},
             {R.id.mu_1_photo, R.id.mu_2_photo, R.id.mu_3_photo, R.id.mu_4_photo}};
 
     private static final int[][] lastCalledLabelIds = {
             {R.id.lc_1_label, R.id.lc_2_label, R.id.lc_3_label, R.id.lc_4_label},
             {R.id.lc_1_photo, R.id.lc_2_photo, R.id.lc_3_photo, R.id.lc_4_photo}};
 
     private final Context context;
     private final Pager pager;
 
     private int[] appWidgetIds;
 
     public UpdateWidgetsTask(Context context, Pager pager, int[] appWidgetIds) {
         this.context = context;
         this.pager = pager;
         this.appWidgetIds = appWidgetIds;
     }
 
     @Override
     public void run() {
         final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         if (appWidgetIds == null) {
             appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QdWidgetProvider.class));
         }
         for (int appWidgetId : appWidgetIds) {
             updateWidget(appWidgetManager, appWidgetId, pager);
         }
     }
 
     private void updateWidget(AppWidgetManager appWidgetManager, int appWidgetId, Pager pager) {
         Log.v(TAG, "update widget: " + appWidgetId);
         RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);
         views.setViewVisibility(R.id.loading_layout, View.GONE);
         views.setViewVisibility(R.id.darkener, View.GONE);
         views.setViewVisibility(R.id.data_layout, View.VISIBLE);
 
         final int currentPage = context.getSharedPreferences(QdWidgetProvider.PREFS_FILE_NAME, Context.MODE_PRIVATE).getInt(Utils.getWidgetPageProperty(appWidgetId), 0);
         Log.v(TAG, "Current page: " + String.valueOf(currentPage));
         final Page page = pager.getPage(currentPage);
         Log.v(TAG, "Page: " + page);
         setRow(views, page.lastCalled, lastCalledLabelIds, appWidgetId, 0);
         setRow(views, page.mostUsed, mostUsedLabelIds, appWidgetId, lastCalledLabelIds.length);
 
         Log.v(TAG, "Pending intents setted");
 
         appWidgetManager.updateAppWidget(appWidgetId, views);
     }
 
     private void setRow(RemoteViews views, ContactInfo[] contactInfos, int[][] labels, int appWidgetId, int idx) {
         for (int i = 0; i < contactInfos.length; i++) {
             final ContactInfo contactInfo = contactInfos[i];
             if (contactInfo != null) {
                 Log.v(TAG, "Fill contact: " + contactInfo.getName() + " : " + contactInfo.getLastDialedPhone() + " : " + contactInfo.getLookupId() + " : " +
                         contactInfo.getUsage() + " : " + contactInfo.personUri);
                 views.setTextViewText(labels[0][i], contactInfo.name);
 
                 addIntent(views, labels[0][i], labels[0][i], idx++, appWidgetId, contactInfo.getLastDialedPhone());
                 addIntent(views, labels[1][i], labels[0][i], idx++, appWidgetId, contactInfo.getLastDialedPhone());
 
                 if (contactInfo.personUri != null) {
                     Log.v(TAG, "personUri: " +contactInfo.personUri);
                     final InputStream input = ContactsContract.Contacts
                             .openContactPhotoInputStream(context.getContentResolver(),
                                     contactInfo.personUri);
 
                     if (input != null) {
                         Bitmap bitmap = BitmapFactory.decodeStream(input);
                         Log.v(TAG, "Bitmap object: " + bitmap);
                        Log.v(TAG, "Bitmap size: " + bitmap.getByteCount());
                         views.setImageViewBitmap(labels[1][i], bitmap);
                     }
                 } else {
                     views.setImageViewResource(labels[1][i], R.drawable.ic_contact_picture);
                 }
             } else {
                 views.setTextViewText(labels[0][i], "No Data");
                 views.setImageViewResource(labels[1][i], R.drawable.ic_contact_picture);
             }
         }
     }
 
     private void addIntent(RemoteViews views, int viewId, int labelId, int idx, int appWidgetId, String phoneToCall) {
         final Intent callIntent = new Intent(context, TapListenerService.class);
         callIntent.putExtra("phoneToCall", phoneToCall);
         callIntent.putExtra("labelId", labelId);
         callIntent.putExtra(TapListenerService.EXTRA_APP_WIDGET_ID, appWidgetId);
         callIntent.setData(Uri.parse("qd://" + viewId));
         final PendingIntent pi = PendingIntent.getService(context, idx, callIntent, PendingIntent.FLAG_CANCEL_CURRENT);
         views.setOnClickPendingIntent(viewId, pi);
     }
 
 }
