 /*
  * Copyright (C) 2013 UniCoPA
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package unicopa.copa.base.com.request;
 
 import java.util.List;
 import java.util.Map;
 import unicopa.copa.base.UserRole;
 
 /**
  * 
  * @author Felix Wiemuth
  */
 public class GetMyEventsResponse extends AbstractResponse {
     private Map<UserRole, List<Integer>> eventIDs;
 
     public GetMyEventsResponse(Map<UserRole, List<Integer>> eventIDs) {
 	this.eventIDs = eventIDs;
     }
 
     /**
      * Get a list of event IDs where the user is RIGHTHOLDER.
      * 
      * @return
      */
     public List<Integer> getRightholderEvents() {
 	return eventIDs.get(UserRole.RIGHTHOLDER);
     }
 
     /**
      * Get a list of event IDs where the user is DEPUTY.
      * 
      * @return
      */
     public List<Integer> getDeputyEvents() {
	return eventIDs.get(UserRole.RIGHTHOLDER);
     }
 
     /**
      * Get a list of event IDs where the user is OWNER.
      * 
      * @return
      */
     public List<Integer> getOwnerEvents() {
	return eventIDs.get(UserRole.RIGHTHOLDER);
     }
 }
