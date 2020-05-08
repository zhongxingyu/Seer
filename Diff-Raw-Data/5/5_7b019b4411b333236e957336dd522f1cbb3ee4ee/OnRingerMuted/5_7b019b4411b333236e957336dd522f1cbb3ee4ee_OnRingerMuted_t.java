 /**
  * Copyright (C) 2009 Jesse Wilson
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
 
 package com.publicobject.shush;
 
 import android.app.ActivityManager;
 import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import static android.media.AudioManager.EXTRA_RINGER_MODE;
 import static android.media.AudioManager.RINGER_MODE_SILENT;
 import static android.media.AudioManager.RINGER_MODE_VIBRATE;
 
 /**
 * Upon ringer mode changing (a broadcast intent), show the ringer muted dialog
 * (an activity intent).
  */
 public class OnRingerMuted extends BroadcastReceiver {
 
     private static final String[] CONFLICTING_APPS = {
         // "bt.android.elixir", // widget; always in the foreground
         "com.idelata.MuteButtonFree",
         "com.littlephoto",
         "com.motorola.Camera",
         "com.noimjosh.profile",
         "com.urbandroid.sleep",
         "vStudio.Android.Camera360",
        "vStudio.Android.GPhotoPaid",
     };
 
     public void onReceive(Context context, Intent intent) {
         int newRingerMode = intent.getIntExtra(EXTRA_RINGER_MODE, -1);
         if (newRingerMode == RINGER_MODE_SILENT || newRingerMode == RINGER_MODE_VIBRATE) {
             if (isMutedByApp(context)) {
                 return;
             }
             context.startActivity(RingerMutedDialog.getIntent(context));
         } else {
             RingerMutedNotification.dismiss(context);
             TurnRingerOn.cancelScheduled(context);
         }
     }
 
     /**
      * Returns true if the current ringer mute intent was likely caused by
      * another app and not a user action. Some camera apps secretly mute the
      * ringer to silence the shutter. Other apps manage the ringer on their own
      * and don't benefit from Shush interference.
      */
     private boolean isMutedByApp(Context context) {
         ActivityManager activityManager
                 = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
         for (ActivityManager.RunningAppProcessInfo p : activityManager.getRunningAppProcesses()) {
             if (p.importance != IMPORTANCE_FOREGROUND) {
                 continue;
             }
             String processName = p.processName;
             for (String app : CONFLICTING_APPS) {
                 if (processName.equals(app)) {
                     return true;
                 }
             }
         }
         return false;
     }
 }
