 /**
  * Copyright 2013 Hideyuki SHIMOOKA <shimooka@doyouphp.jp>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jp.doyouphp.android.temperaturelayer.receiver;
 
 import jp.doyouphp.android.temperaturelayer.TemperatureLayerActivity;
 import jp.doyouphp.android.temperaturelayer.config.TemperatureLayerConfig;
 import jp.doyouphp.android.temperaturelayer.service.TemperatureLayerService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 /**
  * BroadcastReceiver class for starting on boot
  *
  * This receiver will receive Intent.ACTION_BOOT_COMPLETED or
  * Intent.ACTION_PACKAGE_REPLACED and start TemperatureLayer service if current
  * configuration is on.
  */
 public class TemperatureLayerReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
         Log.v(TemperatureLayerActivity.TAG, "intent : " + intent.getAction());
         Log.v(TemperatureLayerActivity.TAG,
                 "data string : " + intent.getDataString());
 
         TemperatureLayerConfig config = new TemperatureLayerConfig(context);
         if (!config.isStartOnBoot()) {
             Log.v(TemperatureLayerActivity.TAG, "start on boot is false");
             return;
         }
         Log.v(TemperatureLayerActivity.TAG, "intent : " + intent.getAction());
         Log.v(TemperatureLayerActivity.TAG,
                 "data string : " + intent.getDataString());
         if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                 || (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) && intent
                         .getDataString().equals(
                                 "package:jp.doyouphp.android.temperaturelayer"))) {
             Log.v(TemperatureLayerActivity.TAG, "send intent");
            context.stopService(new Intent(context,
                    TemperatureLayerService.class));
             context.startService(new Intent(context,
                     TemperatureLayerService.class));
         }
     }
 }
