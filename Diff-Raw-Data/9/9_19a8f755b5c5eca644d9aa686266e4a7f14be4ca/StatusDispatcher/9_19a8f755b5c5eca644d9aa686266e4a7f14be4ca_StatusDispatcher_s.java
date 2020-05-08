 /*Copyright [2010-2011] [David Van de Ven]
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
  */
 
 package org.wahtod.wififixer.utility;
 
 import org.wahtod.wififixer.prefs.PrefUtil;
 import org.wahtod.wififixer.prefs.PrefConstants.Pref;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 
 public class StatusDispatcher {
     private StatusMessage m;
     private static final int MESSAGE_DELAY = 10000;
     private static final int MESSAGE = 42;
     private Context c;
     private PrefUtil prefs;
 
     public StatusDispatcher(final Context context, PrefUtil p) {
	c = context;
 	prefs = p;
     }
 
     /*
      * Essentially, a Leaky Bucket Widget messages throttled to once every 10
      * seconds
      */
     private Handler messagehandler = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    if (prefs.getFlag(Pref.HASWIDGET_KEY))
 		NotifUtil.broadcastStatNotif(c, m);
 	}
 
     };
 
     public void clearQueue() {
 	messagehandler.removeMessages(MESSAGE);
     }
 
     public void sendMessage(final Context context, final StatusMessage message) {
 	if (!message.show) {
 	    /*
 	     * Handle notification cancel case
 	     */
	    NotifUtil.addStatNotif(c, message);
 	} else {
 	    /*
 	     * Only if not a cancel (i.e. show = false) do we want to display on
 	     * widget
 	     */
 	    m = message;
 	    /*
 	     * Dispatch Status Notification update
 	     */
 	    if (prefs.getFlag(Pref.STATENOT_KEY))
		NotifUtil.addStatNotif(c, m);
 	    /*
 	     * queue update for widget
 	     */
 
 	    if (!messagehandler.hasMessages(MESSAGE)) {
 		messagehandler.sendEmptyMessage(MESSAGE);
 		messagehandler.sendEmptyMessageDelayed(MESSAGE, MESSAGE_DELAY);
 	    }
 	}
     }
 }
