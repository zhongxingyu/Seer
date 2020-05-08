 package com.androidproductions.ics.sms.transactions;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.NotificationCompat.BigTextStyle;
 import android.support.v4.app.NotificationCompat.Builder;
 import android.support.v4.app.NotificationCompat.InboxStyle;
 import com.androidproductions.ics.sms.Constants;
 import com.androidproductions.ics.sms.R;
 import com.androidproductions.ics.sms.SmsDialog;
 import com.androidproductions.ics.sms.SmsNotify;
 import com.androidproductions.ics.sms.messaging.IMessage;
 import com.androidproductions.ics.sms.messaging.MessageUtilities;
 import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
 import com.androidproductions.ics.sms.ICSSMSActivity_;
 import com.androidproductions.ics.sms.SmsViewer_;
 import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;
 import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.IMessageView;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class NotificationHelper {
 
     private final Context mContext;
     private final NotificationManager mNotificationManager;
     private List<IMessageView> messages;
     private int messageSize;
     private final ConfigurationHelper configurationHelper;
 
     private static NotificationHelper mInstance;
     private boolean alertOnce;
 
     public static NotificationHelper getInstance(final Context context)
     {
         if (mInstance != null) return mInstance;
         mInstance = new NotificationHelper(context);
         return mInstance;
     }
 
     private NotificationHelper(final Context context)
     {
         mContext = context;
         mNotificationManager = (NotificationManager) mContext.getSystemService(
                 Context.NOTIFICATION_SERVICE);
         messages = new ArrayList<IMessageView>();
         messageSize = 0;
         configurationHelper = ConfigurationHelper.getInstance();
     }
 
     public void updateUnreadSms() {
         notifyUnreadMessages(MessageUtilities.GetUnreadMessages(mContext));
     }
 
     private void notifyUnreadMessages(final List<IMessageView> smss) {
         alertOnce = shouldAlertOnce(smss);
         messages = smss;
         messageSize = messages.size();
         if (configurationHelper.getBooleanValue(ConfigurationHelper.NOTIFICATIONS_ENABLED))
         {
             if (messageSize > 0)
             {
                 mNotificationManager.notify(Constants.NOTIFICATION_ID, buildNotification());
                 configurationHelper.setBooleanValue(ConfigurationHelper.NOTIFICATION_SHOWING, true);
             }
             else
             {
                 mNotificationManager.cancel(Constants.NOTIFICATION_ID);
                 configurationHelper.setBooleanValue(ConfigurationHelper.NOTIFICATION_SHOWING, false);
             }
         }
     }
 
     private boolean shouldAlertOnce(final List<IMessageView> smss) {
         if (messages.size() < smss.size())
             return false;
         for(final IMessageView mess : smss)
         {
             boolean found = false;
             for(final IMessageView mess2 : messages)
             {
                 if (mess2.getAddress().equals(mess.getAddress()) && mess.getBody().equals(mess2.getBody()))
                     found = true;
             }
             if (!found)
                 return false;
         }
         return true;
     }
 
     private Notification buildNotification()
     {
         final Builder builder = buildBaseNotification();
         final Resources res = mContext.getResources();
         builder.addAction(
                 R.drawable.ic_go,
                 res.getString(messageSize == 1 ? R.string.openConvo : R.string.showMore),
                 getIntent());
         return buildBigNotification(builder);
     }
 
     Notification buildBigNotification(final Builder builder) {
         if (messageSize == 1)
         {
             final BigTextStyle big = new NotificationCompat.BigTextStyle(builder);
             big.bigText(getContent());
             return big.build();
         }
 
         final List<String> numbers = new ArrayList<String>();
         final HashMap<String, ArrayList<IMessageView>> groupedMessages = new HashMap<String,ArrayList<IMessageView>>();
         for (final IMessageView s : messages)
         {
             if (!numbers.contains(s.getAddress()))
             {
                 numbers.add(s.getAddress());
                 groupedMessages.put(s.getAddress(), new ArrayList<IMessageView>());
             }
             groupedMessages.get(s.getAddress()).add(s);
         }
         final InboxStyle big = new InboxStyle(builder);
         int messageCount = 0;
         int extraCount= 0;
         if (numbers.size() == 1)
         {
             for(final IMessageView sms : groupedMessages.get(numbers.get(0)))
             {
                 if (messageCount < Constants.MAX_INBOX_DISPLAY)
                 {
                     big.addLine(sms.getBody());
                     messageCount++;
                 }
                 else
                     extraCount++;
             }
             if (extraCount > 0)
                 big.setSummaryText("+ " + extraCount + " more");
         }
         else
         {
             // TODO: What to display here?
             for(final String item : groupedMessages.keySet())
             {
                 if (messageCount < Constants.MAX_INBOX_DISPLAY)
                 {
                     final IMessageView sms = groupedMessages.get(item).get(0);
                     final String name = sms.getContactName();
                     big.addLine(name + ": " + sms.getBody());
                     messageCount++;
                 }
                 else
                     extraCount++;
             }
             if (extraCount > 0)
                 big.setSummaryText("+ " + extraCount + " other"
                         + (extraCount > 2 ? "s" : "" ));
         }
         return big.build();
     }
 
     public void notifySendFailed() {
         final Cursor c = mContext.getContentResolver().query(SmsUri.FAILED_URI, null, null, null, null);
         int smsCount = 1;
         if (c != null)
         {
             smsCount = c.getCount();
             c.close();
         }
         if (smsCount > 0)
         {
             final Builder builder = new Builder(mContext);
             final Intent multiIntent = new Intent(mContext, ICSSMSActivity_.class);
             multiIntent.putExtra(Constants.NOTIFICATION_STATE_UPDATE, true);
            multiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
             final PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, multiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
             final String notAllSent = mContext.getResources().getString(R.string.notAllSent);
             final String sendingFailed = mContext.getResources().getString(R.string.sendingFailed);
             @SuppressWarnings("deprecation") final
             Notification notify = builder.setAutoCancel(true)
                     .setContentText(notAllSent)
                     .setContentTitle(sendingFailed)
                     .setDefaults(Notification.DEFAULT_ALL)
                     .setSmallIcon(R.drawable.ic_launcher_sms)
                     .setTicker(notAllSent)
                     .setWhen(System.currentTimeMillis())
                     .setContentIntent(contentIntent)
                     .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),android.R.drawable.ic_dialog_alert))
                     .setNumber(smsCount)
                     .getNotification();
             mNotificationManager.notify(Constants.NOTIFICATION_SENDFAILED_ID, notify);
         }
         else
         {
             mNotificationManager.cancel(Constants.NOTIFICATION_SENDFAILED_ID);
         }
     }
 
     public void cancelSendFailed() {
         mNotificationManager.cancel(Constants.NOTIFICATION_SENDFAILED_ID);
     }
 
     private Builder buildBaseNotification() {
         final Builder builder = new Builder(mContext);
         final IMessageView first = messages.get(0);
         final IMessageView last = messages.get(messageSize-1);
         builder.setAutoCancel(true)
                 .setContentText(getContent())
                 .setContentTitle(first.getContactName())
                 .setSmallIcon(getIcon())
                 .setTicker(getTickerTitle())
                 .setWhen(System.currentTimeMillis())
                 .setContentIntent(getIntent())
                 .setLargeIcon(first.getContactPhoto())
                 .setNumber(messageSize)
                 .setOnlyAlertOnce(alertOnce);
 
         int defaults = 0;
         if (setVibrate(builder))
             defaults |= Notification.DEFAULT_VIBRATE;
         if (setSound(builder))
             defaults |= Notification.DEFAULT_SOUND;
         if (setLights(builder))
             defaults |= Notification.DEFAULT_LIGHTS;
         if (defaults > 0)
         {
             if (defaults == 7)
                 builder.setDefaults(Notification.DEFAULT_ALL);
             else
                 builder.setDefaults(defaults);
         }
 
         final Intent dialogIntent;
         if (configurationHelper.getStringValue(ConfigurationHelper.DIALOG_TYPE).equals("2"))
             dialogIntent = new Intent(mContext, SmsNotify.class);
         else
             dialogIntent = new Intent(mContext, SmsDialog.class);
         dialogIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, last.getAddress());
         dialogIntent.putExtra(Constants.SMS_MESSAGE, last.getBody());
         dialogIntent.putExtra(Constants.SMS_TIME, last.getDate());
         dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         final PendingIntent dialogpending = PendingIntent.getActivity(mContext, 0, dialogIntent, PendingIntent.FLAG_CANCEL_CURRENT);
         final Resources res = mContext.getResources();
         builder.addAction(R.drawable.ic_go, res.getString(R.string.quickReply), dialogpending);
 
         return builder;
     }
 
 
     int getIcon()
     {
         return configurationHelper.getBooleanValue(ConfigurationHelper.ALTERNATIVE_ICON) ?
                 R.drawable.stat_notify_sms : R.drawable.ic_launcher_sms;
     }
 
     String getTickerTitle()
     {
         if (configurationHelper.getBooleanValue(ConfigurationHelper.PRIVATE_NOTIFICATIONS)) {
             return messageSize + " new message" + (messageSize == 1 ? "" : "s");
         }
         else
         {
             final IMessageView mess;
             if (messageSize == 1)
                 mess = messages.get(0);
             else
                 mess = messages.get(messageSize-1);
             return mess.getContactName() + ": " + mess.getBody();
 
         }
     }
 
     String getContent()
     {
         if (messageSize == 1)
         {
             if (configurationHelper.getBooleanValue(ConfigurationHelper.PRIVATE_NOTIFICATIONS)) {
                 return "You have 1 new message";
             }
             return messages.get(0).getBody();
         }
         else
         {
             // Find all contacts
             final List<String> numbers = new ArrayList<String>();
             for (final IMessageView s : messages)
             {
                 if (!numbers.contains(s.getAddress()))
                 {
                     numbers.add(s.getAddress());
                 }
             }
             final int contactCount = numbers.size();
             if (contactCount == 1)
                 return "You have "+messageSize+" new messages";
             else
                 return "+ " + (contactCount - 1) + " other"
                         + (contactCount > 2 ? "s" : "" );
         }
     }
 
     PendingIntent getIntent()
     {
         final Intent contentIntent;
         if (messageSize == 1)
         {
             contentIntent = new Intent(mContext, SmsViewer_.class);
             contentIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, messages.get(0).getAddress());
         }
         else
         {
             contentIntent = new Intent(mContext, ICSSMSActivity_.class);
             contentIntent.putExtra(Constants.NOTIFICATION_STATE_UPDATE, true);
         }
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mContext, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
     }
 
     Boolean setVibrate(final Builder builder)
     {
         if (configurationHelper.getBooleanValue(ConfigurationHelper.VIBRATION))
             return true;
         builder.setVibrate(new long[] {0L});
         return false;
     }
 
     Boolean setSound(final Builder builder)
     {
         if (!configurationHelper.getBooleanValue(ConfigurationHelper.CUSTOM_SOUND))
             return true;
         final String sound = configurationHelper.getStringValue(ConfigurationHelper.NOTIFICATION_SOUND);
         if (sound.equals(""))
             return true;
         builder.setSound(Uri.parse(sound));
         return false;
     }
 
     Boolean setLights(final Builder builder)
     {
         return true;
     }
 }
