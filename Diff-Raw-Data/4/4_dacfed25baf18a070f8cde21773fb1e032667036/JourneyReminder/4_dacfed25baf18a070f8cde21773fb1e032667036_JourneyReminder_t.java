 /*******************************************************************************
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @contributor(s): Freerider Team 2 (Group 3, IT2901 Spring 2013, NTNU)
  * @version: 2.0
  * 
  * Copyright 2013 Freerider Team 2
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
  ******************************************************************************/
 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.socialhitchhiking.service;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.socialhitchhiking.SocialHitchhikingApplication;
 import no.ntnu.idi.socialhitchhiking.utility.SendNotification;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 public class JourneyReminder extends BroadcastReceiver{
 	SocialHitchhikingApplication app;
 
 	@Override
 	public void onReceive(Context con, Intent intent) {
 
 		app = (SocialHitchhikingApplication) con.getApplicationContext();
 		List<Journey> jour;
 		if(app.getSettings().isPullNotifications()){
 			try {
 				jour = app.sendJourneysRequest();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				jour = app.getJourneys();
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				jour = app.getJourneys();
 			}
 		}
 		else jour = app.getJourneys();
 		Calendar nextHour = Calendar.getInstance();
 		nextHour.add(Calendar.HOUR_OF_DAY, 1);
		Calendar now = Calendar.getInstance();
 		if(jour == null || jour.size() == 0) return;
 		int count = 0;
 
 		for (Journey j : jour) {
			if(j.getStart().before(nextHour) && j.getStart().after(now)){
 				if(!app.isKey("journeyreminder"+j.getSerial())){
 					app.setKeyState("journeyreminder"+j.getSerial(), true);
 					count++;
 				}
 
 			}
 		}
 
 		if(count > 1)SendNotification.create(app, SendNotification.LIST_JOURNEY, "Reminder, Upcoming trips", "You have "+count+" scheduled trips in less than one hour from now", "Scheduled Journey");
 		else if(count > 0)SendNotification.create(app, SendNotification.LIST_JOURNEY, "Reminder, Upcoming trip", "You have a scheduled trip in less than one hour from now", "Scheduled Journey");
 
 
 	}
 
 }
