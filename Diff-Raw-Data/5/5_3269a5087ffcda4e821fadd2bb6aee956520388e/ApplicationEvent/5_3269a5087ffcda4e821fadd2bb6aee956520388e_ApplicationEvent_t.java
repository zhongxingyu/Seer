 /**
  *
  * Copyright (c) 2013, Linagora
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package utils;
 
 import com.google.common.eventbus.EventBus;
import controllers.actors.WebSocket;
 import models.BaseEvent;
 import models.Message;
 import play.Logger;
 
 /**
  * Manage application level events.
  *
  * @author Christophe Hamerling - chamerling@linagora.com
  */
 public class ApplicationEvent {
 
     private static EventBus bus = new EventBus();
 
     /**
      * Push a live event. Skip its registration by handlers.
      *
      * @param pattern
      * @param args
      */
     public static void live(String pattern, Object... args) {
         Message message = new Message();
         message.content = String.format(pattern, args);
         message.title = "New event";
         System.out.println("TODO!!!");
        WebSocket.message(message);
     }
 
     public static void post(String type, String pattern, Object... args) {
         bus.post(BaseEvent.event(type, pattern, args));
     }
 
     public static void info(String pattern, Object... args) {
         bus.post(BaseEvent.event("info", pattern, args));
     }
 
     public static void warning(String pattern, Object... args) {
         bus.post(BaseEvent.event("warning", pattern, args));
     }
 
     public static void register(Object... listeners) {
         if (listeners == null) {
             return;
         }
 
         for(Object listener : listeners) {
             Logger.info("Registering listener " + listener.getClass().getCanonicalName());
             bus.register(listener);
         }
     }
 }
