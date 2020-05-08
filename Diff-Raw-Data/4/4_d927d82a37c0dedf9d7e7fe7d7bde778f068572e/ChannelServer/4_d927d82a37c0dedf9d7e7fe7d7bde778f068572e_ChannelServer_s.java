 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.factory.corba.util;
 
 import gda.configuration.properties.LocalProperties;
 import gda.factory.corba.StructuredEvent;
 import gda.factory.corba.StructuredEventHelper;
 import gda.util.ObjectServer;
 import gda.util.logging.LogbackUtils;
 import gda.util.logging.LoggingUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.jacorb.events.EventChannelImpl;
 import org.omg.CORBA.Any;
 import org.omg.CORBA.ORB;
 import org.omg.CORBA.ORBPackage.InvalidName;
 import org.omg.CosNaming.NamingContextExt;
 import org.omg.CosNaming.NamingContextExtHelper;
 import org.omg.CosNaming.NamingContextPackage.NotFound;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 /**
  * This drives the event channel object.
  */
 public class ChannelServer {
 	private static final Logger logger = LoggerFactory.getLogger(ChannelServer.class);
 
 	/**
 	 * Main entry point for running the channel server as a standalone application.
 	 * 
 	 * @param args
 	 *            command-line arguments
 	 */
 	public static void main(String[] args) {
 		LoggingUtils.setLogDirectory();
 		LogbackUtils.configureLoggingForServerProcess();
 
 		String eventChannelName = null;
 		String property;
 		Properties props = System.getProperties();
 
 		props.put("org.omg.CORBA.ORBClass", LocalProperties.get("gda.ORBClass", "org.jacorb.orb.ORB"));
 		props.put("org.omg.CORBA.ORBSingletonClass", LocalProperties.get("gda.ORBSingletonClass", "org.jacorb.orb.ORBSingleton"));
 
 		if (args.length > 0)
 			eventChannelName = args[0];
 		else if ((property = NameFilter.getEventChannelName()) != null)
 			eventChannelName = property;
		else
 			logger.warn("NameFilter.getEventChannelName() should never null!");
 			eventChannelName = "local.eventChannel";
 
 		ORB orb = ORB.init(args, props);
 		ChannelServer channelServer = new ChannelServer(eventChannelName, orb);
 		channelServer.createAndBindEventChannel();
 		Runtime.getRuntime().addShutdownHook(new UnbindEventServerShutdownHook(channelServer));
 		orb.run();
 	}
 
 	private String eventChannelName;
 
 	private ORB orb;
 
 	/**
 	 * Creates a new unconfigured channel server.
 	 */
 	public ChannelServer() {
 		// do nothing
 	}
 
 	/**
 	 * Creates a new channel server with the specified name.
 	 * 
 	 * @param eventChannelName
 	 *            the event channel name
 	 * @param orb
 	 *            an ORB
 	 */
 	public ChannelServer(String eventChannelName, ORB orb) {
 		this.eventChannelName = eventChannelName;
 		this.orb = orb;
 	}
 
 	/**
 	 * Sets the event channel name for this channel server.
 	 * 
 	 * @param eventChannelName
 	 *            the event channel name
 	 */
 	public void setEventChannelName(String eventChannelName) {
 		this.eventChannelName = eventChannelName;
 	}
 
 	/**
 	 * Sets the ORB that the channel server should use.
 	 * 
 	 * @param orb
 	 *            an ORB
 	 */
 	public void setOrb(ORB orb) {
 		this.orb = orb;
 	}
 
 	/**
 	 * Creates the event channel and binds it in the naming service.
 	 */
 	public void createAndBindEventChannel() {
 		logger.info("Creating event channel with name " + StringUtils.quote(eventChannelName));
 
 		try {
 			org.omg.PortableServer.POA poa = org.omg.PortableServer.POAHelper.narrow(orb
 					.resolve_initial_references("RootPOA"));
 
 			NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
 
 			GdaEventChannelImpl channel = new GdaEventChannelImpl(orb, poa);
 
 			org.omg.CORBA.Object o = poa.servant_to_reference(channel);
 			nc.rebind(nc.to_name(eventChannelName), o);
 
 			logger.info("Event channel initialisation complete");
 			String fileDir = LocalProperties.get(ObjectServer.INITIALISATIONCOMPLETEFOLDER);
 			if (fileDir == null) {
 				fileDir = System.getenv("TEMP");
 			}
 			if (fileDir == null)
 				fileDir = "/tmp";
 			
 			// Create 'initialisation complete' folder if it doesn't exist
 			final File fileDirAsFile = new File(fileDir);
 			if (!fileDirAsFile.exists()) {
 				if (fileDirAsFile.mkdirs()) {
 					logger.info("Created " + fileDirAsFile);
 				} else {
 					logger.error(fileDirAsFile + " didn't exist and it could not be created");
 				}
 			}
 			
 			File oos = new File(fileDir + File.separator + "event_server_startup");
 			try {
 				oos.createNewFile();
 				oos.setLastModified(System.currentTimeMillis());
 			} catch (IOException e) {
 				logger.info("startup file already exist");
 			}
 			oos = null;
 		} catch (org.omg.CORBA.TRANSIENT ct) {
 			logger.error("NameServer not started: " + ct.getMessage());
 		} catch (Exception e) {
 			logger.error("Couldn't create event channel", e);
 		}
 	}
 
 	/**
 	 * Unbinds the event channel from the naming service.
 	 */
 	public void unbind() {
 		NamingContextExt nc;
 		try {
 			nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
 		} catch (InvalidName e) {
 			throw new RuntimeException("Couldn't unbind event channel: couldn't get name service", e);
 		}
 
 		try {
 			nc.unbind(nc.to_name(eventChannelName));
 		} catch (NotFound e) {
 			// ignore - wasn't bound in the first place
 		} catch (Exception e) {
 			throw new RuntimeException("Couldn't unbind event channel", e);
 		}
 	}
 
 }
 
 class GdaEventChannelImpl extends EventChannelImpl {
 	private static final Logger logger = LoggerFactory.getLogger(GdaEventChannelImpl.class);
 
 	GdaEventChannelImpl(org.omg.CORBA.ORB orb, org.omg.PortableServer.POA poa) {
 		super(orb, poa);
 		int threadPoolSize;
 		if ((threadPoolSize = LocalProperties.getAsInt("gda.factory.corba.util.MyEventChannelImpl.threadPoolSize", 0)) > 0) {
 			newFixedThreadPool = Executors.newFixedThreadPool(threadPoolSize);
 		}
 	}
 
 	protected void push_eventNow(TimedAny timedAny) {
 		try {
 			super.push_event(timedAny.event);
 			if (logger.isDebugEnabled()) {
 				long timeAfterDispatch = System.currentTimeMillis();
 				long timeToDispatch = timeAfterDispatch - timedAny.timeReceivedMs;
 				if (timeToDispatch > 500) {
 					if (timedAny.isStructuredEvent()) {
 						StructuredEvent event = StructuredEventHelper.extract(timedAny.event);
 						logger.debug(String.format("Event took %dms to push_event (source=%s, type=%s)", timeToDispatch,
 								event.eventHeader.eventName, event.eventHeader.typeName));
 					} else {
 						logger.debug(String.format("Event took %dms to push_event %s", timeToDispatch, timedAny.toString()));
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			String s = "any = null.";
 			try {
 				s = timedAny.event.type().id();
 			} catch (Exception ex) {
 				// do nothing
 			}
 			logger.error(String.format("Could not push event (type: %s)", s), e);
 		}
 	}
 
 	private int eventsPushedWithinTime = 0;
 	ExecutorService newFixedThreadPool;
 
 	@Override
 	protected void push_event(Any any) {
 
 		final TimedAny timedAny = new TimedAny(any, logger.isDebugEnabled() ? System.currentTimeMillis() : 0);
 
 		if (newFixedThreadPool == null) {
 			push_eventNow(timedAny);
 		} else {
 			newFixedThreadPool.submit(new Callable<Void>() {
 
 				@Override
 				public Void call() throws Exception {
 					if (logger.isDebugEnabled()) {
 						long timeOfDispatch = System.currentTimeMillis();
 						long timeBeforeDispatching = timeOfDispatch - timedAny.timeReceivedMs;
 						if (timeBeforeDispatching > 100) {
 							logger.debug(String.format(
 									"Event took %dms until push_event %s %d events pushed since last warning",
 									timeBeforeDispatching, timedAny.toString(), eventsPushedWithinTime));
 							eventsPushedWithinTime = 0;
 						} else {
 							eventsPushedWithinTime++;
 						}
 
 						timedAny.timeReceivedMs = timeOfDispatch;
 					}
 					push_eventNow(timedAny);
 					return null;
 				}
 			});
 		}
 	}
 
 }
 
 class UnbindEventServerShutdownHook extends Thread {
 
 	private ChannelServer channelServer;
 
 	public UnbindEventServerShutdownHook(ChannelServer channelServer) {
 		this.channelServer = channelServer;
 	}
 
 	@Override
 	public void run() {
 		channelServer.unbind();
 	}
 
 }
