 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
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
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  package org.continuent.appia.protocols.group.leave;
 
 import org.continuent.appia.core.AppiaEventException;
 import org.continuent.appia.core.Channel;
 import org.continuent.appia.core.Session;
 import org.continuent.appia.protocols.group.Group;
 import org.continuent.appia.protocols.group.ViewID;
 import org.continuent.appia.protocols.group.events.GroupSendableEvent;
 
 
 /**
  * Event to request that the member leaves the group.
  * <br>
  * When a member wishes to leave it should send a LeaveEvent downwards.
  * See {@link org.continuent.appia.protocols.group.leave.LeaveLayer LeaveLayer} for more
  * details.
  * <br>
  * If the group is {@link org.continuent.appia.protocols.group.sync.BlockOk blocked} there
 * isn't any guarantee that a leave wil succeed.
  *
  * @author Alexandre Pinto
  * @version 0.1
  * @see org.continuent.appia.protocols.group.leave.LeaveLayer
  */
 public class LeaveEvent extends GroupSendableEvent {
 
   /**
    * Constructs an uninitialized <i>LeaveEvent</i>.
    */
   public LeaveEvent() {}
 
   /**
    * Constructs an initialized <i>LeaveEvent</i>.
    *
    * @param channel the {@link org.continuent.appia.core.Channel Channel} of the Event
    * @param dir the {@link org.continuent.appia.core.Direction Direction} of the Event
    * @param source the {@link org.continuent.appia.core.Session Session} that is generating the Event
    * @param group the {@link org.continuent.appia.protocols.group.Group Group} of the Event
    * @param view_id the {@link org.continuent.appia.protocols.group.ViewID ViewID} of the Event
    * @throws AppiaEventException as the result of calling
    * {@link org.continuent.appia.core.events.SendableEvent#SendableEvent(Channel,int,Session)
    * SendableEvent(Channel,Direction,Session)}
    */
   public LeaveEvent(Channel channel, int dir, Session source, Group group, ViewID view_id) throws AppiaEventException {
     super(channel,dir,source,group,view_id);
   }
 }
