 /**
  *
  * Copyright (c) 2012, PetalsLink
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
 package controllers;
 
 import java.util.List;
 
 import models.EventCloud;
 
import org.ow2.play.governance.api.EventCloudManagementService;
 import org.ow2.play.governance.api.GovernanceExeption;
import org.ow2.play.governance.api.bean.Topic;
 
 import utils.Locator;
 
 /**
  * @author chamerling
  * 
  */
 public class EventCloudController extends PlayController {
 
 	public static void list() {
		EventCloudManagementService client = null;
 		try {
 			client = Locator.getEventCloudManagementService(getNode());
 		} catch (Exception e) {
 			handleException(null, e);
 		}
 
 		List<String> list = null;
 		try {
 			list = client.getEventCloudIds();
 		} catch (Exception e) {
 			handleException("Unable to get EC IDs", e);
 		}
 		render(list);
 	}
 	
 	public static void ec(String id) {
		EventCloudManagementService client = null;
 		try {
 			client = Locator.getEventCloudManagementService(getNode());
 		} catch (Exception e) {
 			handleException(null, e);
 		}
 		
 		EventCloud ec = new EventCloud();
 		try {
			ec.publish = client.getPublishProxyEndpointUrls(id);
			ec.putget = client.getPutgetProxyEndpointUrls(id);
			ec.subscribe = client.getSubscribeProxyEndpointUrls(id);
 		} catch (GovernanceExeption e) {
 			flash.error("Error while getting information for %s : %s", id, e.getMessage());
 			list();
 		}
 		ec.stream = id;
 		render(ec);
 	}
 }
