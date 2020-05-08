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
  
 package org.continuent.appia.protocols.group.events;
 
 import java.net.InetSocketAddress;
 
 import org.continuent.appia.core.*;
 import org.continuent.appia.protocols.common.InetWithPort;
 import org.continuent.appia.protocols.group.AppiaGroupException;
 import org.continuent.appia.protocols.group.Endpt;
 import org.continuent.appia.protocols.group.ViewState;
 
 
 
 /**
  * {@link org.continuent.appia.core.Event Event} that initializes the
  * <i>Group Communication protocols</i>.
  * <br>
  * In reality the several <i>Group Communication protocols</i> will only start
  * operating upon receiving the first
  * {@link org.continuent.appia.protocols.group.intra.View View event}, but the
  * <i>GroupInit</i> is required to create that first <i>view</i>.
  *
  * @author Alexandre Pinto
  * @version 0.1
  * @see org.continuent.appia.protocols.group.intra.View
  */
 public class GroupInit extends Event {
 
   /**
    * The initial <i>view</i>
    */
   public ViewState vs;
   /**
    * The initial <i>rank</i> of the member
    */
   public int rank;
   /**
    * The <i>IP multicast</i> address of the group.
    * <br>
    * If <i>IP multicast</i> is not supported then it is <b>null</b>.
    */
   public Object ip_multicast;
   /**
    * The IP addresses of a set of <i>Gossip Servers</i>.
    * <br>
    * If there isn't a <i>Gossip Server</i> it is <b>null</b>.
    */
   public Object[] ip_gossip;
   
   private ViewState baseVS;
 
     /**
    * Creates an initialized <i>GroupInit</i>.
    *
    * @param vs the initial <i>view</i>
    * @param endpt the {@link org.continuent.appia.protocols.group.Endpt Endpt} of the member
    * @param ip_multicast the <i>IP multicast</i> address, or <b>null</b>
    * @param channel the {@link org.continuent.appia.core.Channel Channel} of the Event
    * @param dir the {@link org.continuent.appia.core.Direction Direction} of the Event
    * @param source the {@link org.continuent.appia.core.Session Session} that is generating the Event
    * @throws AppiaEventException as the result of calling
    * {@link org.continuent.appia.core.Event#Event(Channel,int,Session)
    * Event(Channel,Direction,Session)}
    * @deprecated
    */
   public GroupInit(
           ViewState vs,
           Endpt endpt,
           InetWithPort ip_multicast,
           InetWithPort[] ip_gossip,
           Channel channel, int dir, Session source)
     throws AppiaEventException,NullPointerException,AppiaGroupException {
 
     super(channel,dir,source);
 
     if ((vs == null) || (endpt == null))
        throw new NullPointerException("appia:group:GroupInit: view state or endpoint not given");
 
     this.vs=vs;
     if ((rank=vs.getRank(endpt)) < 0)
        throw new AppiaGroupException("GroupInit: endpoint given doesn't belong to view");
     this.ip_multicast=new InetSocketAddress(ip_multicast.host, ip_multicast.port);
     this.ip_gossip = new InetSocketAddress[ip_gossip.length];
     for(int i=0; i<ip_gossip.length; i++)
         this.ip_gossip[i] = new InetSocketAddress(ip_gossip[i].host, ip_gossip[i].port);
   }
   
   /**
    * Creates an initialized <i>GroupInit</i>.
    *
    * @param vs the initial <i>view</i>
    * @param endpt the {@link org.continuent.appia.protocols.group.Endpt Endpt} of the member
    * @param ipMulticast the <i>IP multicast</i> address, or <b>null</b>
    * @param ipGossip the IP of the gossip service
    * @param channel the {@link org.continuent.appia.core.Channel Channel} of the Event
    * @param dir the {@link org.continuent.appia.core.Direction Direction} of the Event
    * @param source the {@link org.continuent.appia.core.Session Session} that is generating the Event
    * @throws AppiaEventException as the result of calling
    * {@link org.continuent.appia.core.Event#Event(Channel,int,Session)
    * Event(Channel,Direction,Session)}
    */
   public GroupInit(
           ViewState vs,
           Endpt endpt,
          InetSocketAddress ipMulticast,
          InetSocketAddress[] ipGossip,
           Channel channel, int dir, Session source)
     throws AppiaEventException,NullPointerException,AppiaGroupException {
 
     super(channel,dir,source);
 
     if ((vs == null) || (endpt == null))
        throw new NullPointerException("appia:group:GroupInit: view state or endpoint not given");
 
     this.vs=vs;
     if ((rank=vs.getRank(endpt)) < 0)
        throw new AppiaGroupException("GroupInit: endpoint given doesn't belong to view");
     this.ip_multicast=ipMulticast;
     this.ip_gossip=ipGossip;
   }
 
   public ViewState getBaseVS() {
       return baseVS;
   }
 
   public void setBaseVS(ViewState baseVS) {
       this.baseVS = baseVS;
   }
 
 }
