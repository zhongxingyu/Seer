 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.google.android.apps.chrometophone;
 
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 
 import com.google.android.c2dm.C2DMBaseReceiver;
 
 public class C2DMReceiver extends C2DMBaseReceiver {
     public C2DMReceiver() {
         super(DeviceRegistrar.SENDER_ID);
     }
 
     @Override
     public void onRegistrered(Context context, String registration) {
         DeviceRegistrar.registerWithServer(context, registration);
     }
 
     @Override
     public void onUnregistered(Context context) {
         SharedPreferences prefs = Prefs.get(context);
         String deviceRegistrationID = prefs.getString("deviceRegistrationID", null);
         DeviceRegistrar.unregisterWithServer(context, deviceRegistrationID);
     }
 
     @Override
     public void onError(Context context, String errorId) {
         context.sendBroadcast(new Intent("com.google.ctp.UPDATE_UI"));
     }
 
     @Override
     public void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String url = (String) extras.get("url");
            String title = (String) extras.get("title");
            String sel = (String) extras.get("sel");
            String debug = (String) extras.get("debug");
 
            if (debug != null) {
                // server-controlled debug - the server wants to know
                // we received the message, and when. This is not user-controllable,
                // we don't want extra traffic on the server or phone. Server may
                // turn this on for a small percentage of requests or for users
                // who report issues.
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(DeviceRegistrar.BASE_URL + "/debug?id="
                        + extras.get("collapse_key"));
                // No auth - the purpose is only to generate a log/confirm delivery
                // (to avoid overhead of getting the token)
                try {
                    client.execute(get);
                } catch (ClientProtocolException e) {
                    // ignore
                } catch (IOException e) {
                    // ignore
                }
            }
 
            if (title != null && url != null && url.startsWith("http")) {
                SharedPreferences settings = Prefs.get(context);
                Intent launchIntent = getLaunchIntent(context, url, title, sel);
 
                if (settings.getBoolean("launchBrowserOrMaps", true) && launchIntent != null) {
                    playNotificationSound(context);
                    context.startActivity(launchIntent);
                } else {
                    if (sel != null && sel.length() > 0) {  // have selection
                        generateNotification(context, sel,
                                context.getString(R.string.copied_desktop_clipboard), launchIntent);
                    } else {
                        generateNotification(context, url, title, launchIntent);
                    }
                }
            }
        }
    }
 
     private Intent getLaunchIntent(Context context, String url, String title, String sel) {
         Intent intent = null;
         String number = parseTelephoneNumber(sel);
         if (number != null) {
             intent = new Intent("android.intent.action.DIAL",
                     Uri.parse("tel:" + number));
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             ClipboardManager cm =
                 (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
             cm.setText(number);
         } else if (sel != null && sel.length() > 0) {
             // No intent for selection - just copy to clipboard
             ClipboardManager cm =
                 (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
             cm.setText(sel);
         } else {
             final String GMM_PACKAGE_NAME = "com.google.android.apps.maps";
             final String GMM_CLASS_NAME = "com.google.android.maps.MapsActivity";
 
             intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             if (isMapsURL(url)) {
                 intent.setClassName(GMM_PACKAGE_NAME, GMM_CLASS_NAME);
             }
 
             // Fall back if we can't resolve intent (i.e. app missing)
             PackageManager pm = context.getPackageManager();
             if (null == intent.resolveActivity(pm)) {
                 intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             }
         }
         return intent;
     }
 
    private void generateNotification(Context context, String msg, String title, Intent intent) {
        int icon = R.drawable.status_icon;
        long when = System.currentTimeMillis();
 
        Notification notification = new Notification(icon, title, when);
        notification.setLatestEventInfo(context, title, msg,
                PendingIntent.getActivity(context, 0, intent, 0));
        notification.defaults = Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
 
        SharedPreferences settings = Prefs.get(context);
        int notificatonID = settings.getInt("notificationID", 0); // allow multiple notifications
 
        NotificationManager nm =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificatonID, notification);
 
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("notificationID", ++notificatonID % 32);
        editor.commit();
    }
 
    private void playNotificationSound(Context context) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (uri != null) {
            Ringtone rt = RingtoneManager.getRingtone(context, uri);
            if (rt != null) rt.play();
        }
    }
 
    private String parseTelephoneNumber(String sel) {
       // Hack: Remove trailing left-to-right mark (Google Maps adds this)
       if (sel.codePointAt(sel.length() - 1) == 8206) {
           sel = sel.substring(0, sel.length() - 1);
       }

        String number = null;
        if (sel != null && sel.matches("([Tt]el[:]?)?\\s?[+]?(\\(?[0-9|\\s|\\-|\\.]\\)?)+")) {
            String elements[] = sel.split("([Tt]el[:]?)");
            number = elements.length > 1 ? elements[1] : elements[0];
            number = number.replace(" ", "");
 
            // Remove option (0) in international numbers, e.g. +44 (0)20 ...
            if (number.matches("\\+[0-9]{2,3}\\(0\\).*")) {
                int openBracket = number.indexOf('(');
                int closeBracket = number.indexOf(')');
                number = number.substring(0, openBracket) +
                        number.substring(closeBracket + 1);
            }
        }
 
        return number;
    }
 
    private boolean isMapsURL(String url) {
        return url.matches("http://maps\\.google\\.[a-z]{2,3}(\\.[a-z]{2})?[/?].*") ||
                url.matches("http://www\\.google\\.[a-z]{2,3}(\\.[a-z]{2})?/maps.*");
    }
 }
