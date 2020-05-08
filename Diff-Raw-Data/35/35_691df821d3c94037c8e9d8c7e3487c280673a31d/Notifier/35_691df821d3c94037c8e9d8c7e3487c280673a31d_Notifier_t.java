 package edu.usf.eng.pie.avatars4change.notifier;
 
 import edu.usf.eng.pie.avatars4change.R;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 
 public class Notifier {
     private static int FM_NOTIFICATION_ID = 17141;	//this is just some random number i picked...
     // post a notification to the user with given message
     	// Add app running notification  
     public static void addNotification(Context context,String message) {
     	NotificationCompat.Builder builder =  
                 new NotificationCompat.Builder(context)  
                 .setSmallIcon(R.drawable.thumb)
                 .setContentTitle("AvatarWallpaper Error!")  
                 .setContentText(message);  
     	//set the intent opened when notification is clicked
         Intent notificationIntent = new Intent(context, edu.usf.eng.pie.avatars4change.notifier.Notifier.class);  //TODO 2nd class here should be changed to a message display activity
         notificationIntent.putExtra("message", message);
         PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,   
                 PendingIntent.FLAG_UPDATE_CURRENT);  
         builder.setContentIntent(contentIntent);  
 
         // Add as notification  
         NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  
         manager.notify(FM_NOTIFICATION_ID, builder.build());  
     }  
 
     // Remove notification  
     public static void removeNotification(Context context) {  
         NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  
         manager.cancel(FM_NOTIFICATION_ID);  
     }  
 }
