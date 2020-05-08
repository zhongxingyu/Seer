 package com.restaurant.collection;
 
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 
 public class GcmBroadcastReceiver extends BroadcastReceiver{
 	
 	static final String TAG = "GCMDemo";
     public static final int NOTIFICATION_ID = 1;
     private NotificationManager mNotificationManager;
     NotificationCompat.Builder builder;
     Context ctx;
     
     @Override
     public void onReceive(Context context, Intent intent) {
         GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
         ctx = context;
         String messageType = gcm.getMessageType(intent);
         if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
 //            sendNotification("Send error: " + intent.getExtras().toString());
         } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
 //            sendNotification("Deleted messages on server: " +
 //                    intent.getExtras().toString());
         } else {
         	try{
         		sendNotification(intent);
         	}catch(Exception e){
         		
         	}
         }
         setResultCode(Activity.RESULT_OK);
     }
     
     int openActivity = 0; // 0: MainActivity, 1: MyNovelActivity, 2: BookmarkActivity , 3: CategoryActivity, 4: NovelIntroduceActivity
     PendingIntent contentIntent;
 
     // Put the GCM message into a notification and post it.
     private void sendNotification(Intent intent) {
         mNotificationManager = (NotificationManager)
                 ctx.getSystemService(Context.NOTIFICATION_SERVICE);
         
         openActivity = Integer.parseInt(intent.getStringExtra("activity"));
         
         switch(openActivity){
         	case 0:
         		Intent activity_intent = new Intent(ctx, RestaurantIntroActivity.class);
         		activity_intent.putExtra("ResturantName",intent.getStringExtra("resturant_name"));
         		activity_intent.putExtra("ResturantId",Integer.parseInt(intent.getStringExtra("resturant_id")));
        		contentIntent = PendingIntent.getActivity(ctx, 0, activity_intent, 0);
         		break;
         	case 1:
         		Intent activity_intent2 = new Intent(ctx, RestaurantNoteActivity.class);
         		activity_intent2.putExtra("NoteTitle",intent.getStringExtra("note_title"));
         		activity_intent2.putExtra("NoteLink",intent.getStringExtra("note_link"));
         		activity_intent2.putExtra("NotePic",intent.getStringExtra("note_pic"));
         		activity_intent2.putExtra("NoteId",Integer.parseInt(intent.getStringExtra("note_id")));
         		activity_intent2.putExtra("RestaurantId",Integer.parseInt(intent.getStringExtra("restaurant_id")));
         		activity_intent2.putExtra("NoteX",Double.parseDouble(intent.getStringExtra("note_x")));
         		activity_intent2.putExtra("NoteY",Double.parseDouble(intent.getStringExtra("note_y")));
         		
        		contentIntent = PendingIntent.getActivity(ctx, 0, activity_intent2, 0);
         		break;
         	
         }
         
 
         NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
         .setSmallIcon(R.drawable.icon72)
         .setContentTitle(intent.getStringExtra("title"))
         .setStyle(new NotificationCompat.BigTextStyle()
         .bigText(intent.getStringExtra("big_text")))
         .setContentText(intent.getStringExtra("content"))
         .setAutoCancel(true);
 
         mBuilder.setContentIntent(contentIntent);
         mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
     }
 }
