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
  /**
  * Title:        Appia<p>
  * Description:  Protocol development and composition framework<p>
  * Copyright:    Copyright (c) Nuno Carvalho and Luis Rodrigues<p>
  * Company:      F.C.U.L.<p>
  * @author Nuno Carvalho and Luis Rodrigues
  * @version 1.0
  */
 package org.continuent.appia.protocols.group.remote;
 
 import java.io.PrintStream;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import org.apache.log4j.Logger;
 import org.continuent.appia.core.AppiaCursorException;
 import org.continuent.appia.core.AppiaDuplicatedSessionsException;
 import org.continuent.appia.core.AppiaEventException;
 import org.continuent.appia.core.AppiaInvalidQoSException;
 import org.continuent.appia.core.Channel;
 import org.continuent.appia.core.ChannelCursor;
 import org.continuent.appia.core.Direction;
 import org.continuent.appia.core.Event;
 import org.continuent.appia.core.EventQualifier;
 import org.continuent.appia.core.Layer;
 import org.continuent.appia.core.QoS;
 import org.continuent.appia.core.Session;
 import org.continuent.appia.core.events.channel.ChannelInit;
 import org.continuent.appia.core.events.channel.Debug;
 import org.continuent.appia.core.message.Message;
 import org.continuent.appia.protocols.common.RegisterSocketEvent;
 import org.continuent.appia.protocols.fifo.FifoLayer;
 import org.continuent.appia.protocols.fifo.FifoSession;
 import org.continuent.appia.protocols.group.Endpt;
 import org.continuent.appia.protocols.group.Group;
 import org.continuent.appia.protocols.group.ViewID;
 import org.continuent.appia.protocols.group.ViewState;
 import org.continuent.appia.protocols.group.heal.GossipOutEvent;
 import org.continuent.appia.protocols.udpsimple.UdpSimpleLayer;
 import org.continuent.appia.protocols.udpsimple.UdpSimpleSession;
 import org.continuent.appia.protocols.utils.ParseUtils;
 import org.continuent.appia.xml.interfaces.InitializableSession;
 import org.continuent.appia.xml.utils.SessionProperties;
 
 
 /**
  * RemoteViewSession is the session that implements the RemoteView request 
  * functionality.<p> 
  *
  * It registers itself in the specified gossip server.
  * The requests are made by sending a {@link RemoteViewEvent} to this session
  * with the required group id, witch are replied by an ascending 
  * RemoteViewEvent containing the group id and an array of the group member's
  * addresses.
  */
 public class RemoteViewSession extends Session implements InitializableSession {
 	
     private static Logger log = Logger.getLogger(RemoteViewSession.class);
     private static final boolean FULL_DEBUG = false;
     private PrintStream debug = null;
 
 	public static final int DEFAULT_GOSSIP_PORT = 10000;
 	private static final int NUM_LAYERS_GOSSIP_CHANNEL = 3;
     
 	private InetSocketAddress myAddress=null;
 	private InetSocketAddress gossipAddress;
 	private Hashtable addrTable = new Hashtable();
 	private Channel myChannel = null;
 	private Channel initChannel;
 	private boolean needsRse = true;
 	private RemoteViewEvent rve=null;
 	
 	/**
 	 * Session standard constructor.
 	 */
 	public RemoteViewSession(Layer l) {
 		super(l);
 	}
 	
 	
 	/**
 	 * Initializes this session. Must be called before the channel is started.
 	 *
 	 * @param gossipAddress the address of the gossip server to contact
 	 */
 	public void init(InetSocketAddress gossipAddress) {
 		this.gossipAddress = gossipAddress;
 	}
 	
 	public void init(SessionProperties params) {
 	    if (params.containsKey("gossip")) {
 	        try {
 	            gossipAddress = ParseUtils.parseSocketAddress(params.getString("gossip"),null,DEFAULT_GOSSIP_PORT);
 	        } catch (UnknownHostException e) {
 	            log.warn("XML initialization failed due to the following exception: "+e);
 	        } catch (ParseException e) {
 	            log.warn("XML initialization failed due to the following exception: "+e);
 	        }
 	    }
 	}
 	
 	/**
 	 * The event handler method.
 	 *
 	 * @param event the event
 	 */
 	public void handle(Event event) {
 		
         if(FULL_DEBUG)
             debug("received event on handle: " + event);
 		
 		if (event instanceof ChannelInit) {
 			handleChannelInit((ChannelInit) event);
 			return;
 		}
 		
 		if (event instanceof RemoteViewEvent) {
 			handleRemoteView((RemoteViewEvent) event);
 			return;
 		}
 		
 		if (event instanceof GossipOutEvent) {
 			handleGossipOut((GossipOutEvent) event);
 			return;
 		}
 		
 		if(event instanceof RegisterSocketEvent){
 			handleRegisterSocketEvent((RegisterSocketEvent) event);
 			return;
 		}
 		
 		if (event instanceof Debug) {
 			final Debug ev = (Debug) event;
 			
 			if (ev.getQualifierMode() == EventQualifier.ON) {
 				if (ev.getOutput() instanceof PrintStream)
 					debug = (PrintStream)ev.getOutput();
 				else
 					debug = new PrintStream(ev.getOutput());
 				log.debug("Full debugging started.");
 			} else {
 				if (ev.getQualifierMode() == EventQualifier.OFF)
 					debug = null;
 				else if (ev.getQualifierMode() == EventQualifier.NOTIFY) {
 					if (ev.getOutput() instanceof PrintStream)
 						debug = (PrintStream)ev.getOutput();
 					else
 						debug = new PrintStream(ev.getOutput());
 					printAddrTable();
 					debug = null;
 				}
 			}
 			
 			try { 
 			    ev.go(); 
 			} catch (AppiaEventException ex) {
                 log.debug("error forwarding event of type "+ev.getClass().getName()+" : "+ex);
 			}
 			return;
 		} // end of debug event handling
 		
 		log.warn("Received unwanted event (" + event.getClass().getName()+"). Forwarding it.");
 		try {
 		    event.go(); 
 		} catch (AppiaEventException ex) {
             log.debug("error forwarding event of type "+event.getClass().getName()+" : "+ex);
 		}
 	}
 	
 	
 	private void handleChannelInit(ChannelInit event) {
 		
 	    try { 
 	        event.go(); 
 	    } catch (AppiaEventException ex) {
 	        log.debug("error forwarding event of type "+event.getClass().getName()+" : "+ex);
 	    }
 		
 		if (myChannel == null) {
 			initChannel = event.getChannel();
 			makeOutChannel(initChannel);
 		} else {
 			if (needsRse) {
 				try {
 					final RegisterSocketEvent rse =
 						new RegisterSocketEvent(myChannel, Direction.DOWN, this, myAddress.getPort());
 					rse.go();
 				} catch (AppiaEventException ex) {
                     log.debug("error forwarding event of type "+RegisterSocketEvent.class.getClass().getName()+" : "+ex);
 				}
 			}	    
 			
 			// send a GossipOutEvent to set things in motion immediatly
 			try {
 				
 				final GossipOutEvent goe =
 					new GossipOutEvent(myChannel, Direction.DOWN, this);
 				final Message msg = (Message) goe.getMessage();
 				msg.pushObject(new ViewID(0, new Endpt()));
 				msg.pushObject(new Group());
 				msg.pushObject(myAddress);
 				goe.source = myAddress;
 				goe.dest = gossipAddress;
 				goe.init();
 				goe.go();
 			} catch (AppiaEventException ex) {
                 log.debug("error forwarding event of type "+GossipOutEvent.class.getName()+" : "+ex);
 			}
 		}
 	}
 	
 	
 	private void makeOutChannel(Channel t) {
 		
 		final ChannelCursor cursor = new ChannelCursor(t);
 		
 		final Layer[] layers = new Layer[NUM_LAYERS_GOSSIP_CHANNEL];
 		
 		try {
 			cursor.bottom();
 			if (cursor.getLayer() instanceof UdpSimpleLayer) {
 				layers[0] = cursor.getLayer();
 				needsRse = false;
 			} else {
 				layers[0] = new UdpSimpleLayer();
 				needsRse = true;
 			}
 			
 			while(cursor.isPositioned() && !(cursor.getLayer() instanceof FifoLayer))
 				cursor.up();
 			if (cursor.isPositioned())
 				layers[1] = cursor.getLayer();
 			else
 				layers[1] = new FifoLayer();
 			
 			layers[2] = this.getLayer();
 
 			final QoS qos = new QoS("Gossip Out QoS", layers);
 			myChannel = qos.createUnboundChannel("Gossip Channel",t.getEventScheduler());
 			
 			final ChannelCursor mycc = myChannel.getCursor();
 			mycc.bottom();
 			cursor.bottom();
 			
 			if (cursor.getSession() instanceof UdpSimpleSession)
 				mycc.setSession(cursor.getSession());
 			
 			mycc.up();
 			
 			while (cursor.isPositioned() && 
 					!(cursor.getSession() instanceof FifoSession))
 				cursor.up();
 			if (cursor.isPositioned())
 				mycc.setSession(cursor.getSession());
 			
 			mycc.up();
 			mycc.setSession(this);
 			
 			myChannel.start();
 		} catch (AppiaCursorException ex) {
 			log.debug("Error: unable to create GossipOut channel: "+ex);
 		} catch (AppiaInvalidQoSException ex) {
 			log.debug("Error: unable to create GossipOut channel: "+ex);
 		} catch (AppiaDuplicatedSessionsException ex) {
 			log.debug("Error: unable to create GossipOut channel: "+ex);
 		}
 	}
 		
 	private void handleGossipOut(GossipOutEvent event) {
 		
 		try{
 			event.go();
 		}
 		catch(AppiaEventException e){
             log.debug("error forwarding event of type "+event.getClass().getName()+" : "+e);
 		}
 	}
 	
 	
 	private void handleRemoteView(RemoteViewEvent event) {
 		if (event.getDir() == Direction.DOWN) {
 			// we got a request			
 			if (myAddress==null) {
 				rve=event;
 				return;
 			}
 			
 			try {
 				if(log.isDebugEnabled())
 					log.debug("sending request from " + myAddress + 
 						" to " + gossipAddress + " in channel " + myChannel.getChannelID() +
 						" for group " + event.getGroup());
 
 				event.dest = gossipAddress;
 				event.source = myAddress;
 				
				final Message msg = new Message();
 				Group.push(event.getGroup(),msg);
 				msg.pushObject(myAddress);
 				
				event.setMessage(msg);
 				event.setChannel(myChannel);
 				event.setSource(this);
 				
 				event.init();
 				event.go();
 				
 				event = null;
 			} catch (AppiaEventException ex) {
 				log.debug("error sending down RemoteViewEvent: "+ex);
 			}
 		} else {
 			boolean appearsViewState = true;
 			final Message msg = (Message) event.getMessage();
 			try{
 				ViewState.peek(msg);
 			}catch(Exception special){
 				appearsViewState = false;
 			}
 			//debug("Received remote view event!!!!!("+ViewState.peek(om)+")");
 			if (appearsViewState && ViewState.peek(msg) instanceof ViewState) {
 				final ViewState receivedVs = ViewState.pop(msg);
 				event.setAddresses(receivedVs.addresses);	    
 				event.setGroup(receivedVs.group);
 				event.setSource(this);
 				event.setChannel(initChannel);
 				
 				try {
 					event.init();
 					event.go();
 				} catch(AppiaEventException ex){
                     log.debug("error forwarding event of type "+event.getClass().getName()+" : "+ex);
 				}
 			}
 		}
 	}
 	
 	
 	private void handleRegisterSocketEvent(RegisterSocketEvent e){
 		if(e.getDir()==Direction.UP){
 			myAddress = new InetSocketAddress(e.localHost, e.port);
 			
 			if(rve!=null){
 				handle(rve);
 				rve=null; 
 			}
 		}
 		
 		try{
 			e.go();
 		}
 		catch(AppiaEventException ex){
             log.debug("error forwarding event of type "+e.getClass().getName()+" : "+e);
 		}
 	}
 	
 	/**
 	 * Sends a Debug event through the Gossip Out channel with the specified
 	 * EventQualifier
 	 *
 	 * @see Debug
 	 * @see EventQualifier
 	 */
 	public void doDebug(int eq) { 
 		try {
 			final java.io.OutputStream debugOut = System.out;
 			
 			final Debug e = new Debug(debugOut);
 			e.setChannel(myChannel);
 			e.setDir(Direction.DOWN);
 			e.setSource(this);
 			e.setQualifierMode(eq);
 			e.init();
 			e.go();
 		} catch (AppiaEventException ex) {
 			ex.printStackTrace();
 			System.err.println("Exception when sending debug event");
 		}
 	}
 		
 	private void debug(String s){
 		if(debug != null)
 			debug.println(this.getClass().getName()+"[FULL DEBUG] "+s);
 	}
 	
 	private void printAddrTable() {
 	    if(FULL_DEBUG){
 	        debug("address Table:");
 	        final Enumeration e = addrTable.keys();
             String g = null;
             InetSocketAddress ad = null;
             while(e.hasMoreElements()) {
 	            g = (String) e.nextElement();
 	            ad = (InetSocketAddress) addrTable.get(g);
 	            debug("{" + g + "=" + ad + "}");
 	        }
 	    }
 	}
 }
