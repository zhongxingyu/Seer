 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.helper;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import se.toxbee.sleepfighter.model.Alarm;
 import se.toxbee.sleepfighter.model.AlarmList;
 import se.toxbee.sleepfighter.utils.message.Message;
 import se.toxbee.sleepfighter.utils.message.MessageBus;
 
 /**
  * AlarmTimeRefresher has the responsibility of asynchronously refreshing all alarms.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Nov 17, 2013
  */
 public class AlarmTimeRefresher {
 	private static long REFRESH_INTERVAL = 1000;
 
 	private Timer timer;
 	private final AlarmList list;
 
 	public class RefreshedEvent implements Message {
 		public AlarmList getList() {
 			return list;
 		}
 
 		public AlarmTimeRefresher getRefresher() {
 			return AlarmTimeRefresher.this;
 		}
 	}
 
 	/**
 	 * Constructs the refresher given the list of alarms.
 	 *
 	 * @param list the list.
 	 */
 	public AlarmTimeRefresher( AlarmList list ) {
 		this.list = list;
 	}
 
 	/**
 	 * Starts the refresher.
 	 */
 	public void start() {
		if ( this.timer != null ) {
 			synchronized ( this.timer ) {
 				TimerTask task = new TimerTask() {
 					@Override
 					public void run() {
 						refresh();
 					}
 				};
 
 				// Refresh every second.
				this.timer = new Timer();
 				this.timer.scheduleAtFixedRate( task, 0, REFRESH_INTERVAL );
 			}
 		}
 	}
 
 	/**
 	 * Stops the refresher.
 	 */
 	public void stop() {
 		if ( this.timer != null ) {
 			synchronized ( this.timer ) {
 				this.timer.cancel();
 			}
 
 			this.timer = null;
 		}
 	}
 
 	private void refresh() {
 		List<Alarm> l = Collections.synchronizedList( this.list );
 		synchronized( l ) {
 			if ( this.list.isEmpty() ) {
 				return;
 			}
 
 			// Do refreshing.
 			for ( Alarm a : l ) {
 				synchronized( a ) {
 					if ( this.timer == null ) {
 						return;
 					}
 
 					a.getTime().refresh();
 				}
 			}
 
 			if ( this.timer == null ) {
 				return;
 			}
 
 			// Notify bus of refresh.
 			MessageBus<Message> bus = this.list.getMessageBus();
 			if ( bus != null ) {
 				bus.publish( new RefreshedEvent() );
 			}
 		}
 	}
 }
