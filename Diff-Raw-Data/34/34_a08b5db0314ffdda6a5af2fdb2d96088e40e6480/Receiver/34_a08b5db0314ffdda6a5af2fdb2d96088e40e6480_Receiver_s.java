 /***
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may
  * not use this file except in compliance with the License. You may obtain
  * a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package de.mangelow.slideitloud;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class Receiver extends BroadcastReceiver {
 
 	private final String TAG = "SIL";
	private final boolean D = true;
 
 	private Helper mHelper = new Helper();
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if(D)Log.d(TAG, "onReceive()");
 
 		if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
 			if(D)Log.d(TAG, "BOOT_COMPLETED");
 
 			final boolean enabled = mHelper.loadBooleanPref(context, "enabled", mHelper.ENABLED);
 			if(enabled) {
 				Log.i(TAG, "Enable Service");
 
 				Intent serviceIntent = new Intent(context,Service.class);
 				context.startService(serviceIntent);
 			}
 		}
 	}
 }
