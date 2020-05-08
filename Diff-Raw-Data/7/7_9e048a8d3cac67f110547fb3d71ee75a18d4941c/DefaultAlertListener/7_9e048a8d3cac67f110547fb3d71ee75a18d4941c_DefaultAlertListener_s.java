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
 package monitoring;
 
import controllers.AlertWebSocket;
 import controllers.actors.WebSocket;
 import models.Alert;
 import play.Logger;
 import utils.ApplicationEvent;
 
 /**
  * Local alert receiver. Used to centralize all the stuff to do when an alert is received.
  *
  * @author Christophe Hamerling - chamerling@linagora.com
  */
 public class DefaultAlertListener implements AlertListener {
 
     @Override
     public void handle(Alert alert) {
         Logger.debug("Handle alert");
         if (alert == null) {
             return;
         }
 
         try {
             alert.save();
         } catch (Exception e) {
         }
         ApplicationEvent.info("New alert has been received : %s", alert.message);
         WebSocket.alert(alert);
     }
 }
