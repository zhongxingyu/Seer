 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Bus
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.bus;
 
 import java.awt.Color;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Random;
 
 import de.tuilmenau.ics.CommonSim.datastream.DatastreamManager;
 import de.tuilmenau.ics.CommonSim.datastream.StreamTime;
 import de.tuilmenau.ics.CommonSim.datastream.annotations.AutoWire;
 import de.tuilmenau.ics.CommonSim.datastream.numeric.DoubleNode;
 import de.tuilmenau.ics.CommonSim.datastream.numeric.IDoubleWriter;
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.EventHandler;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.Config.Simulator.SimulatorMode;
 import de.tuilmenau.ics.fog.application.util.LayerObserverCallback;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.properties.DatarateProperty;
 import de.tuilmenau.ics.fog.facade.properties.DelayProperty;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.facade.properties.LossRateProperty;
 import de.tuilmenau.ics.fog.facade.properties.MinMaxProperty.Limit;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.ILowerLayer;
 import de.tuilmenau.ics.fog.topology.ILowerLayerReceive;
 import de.tuilmenau.ics.fog.topology.NeighborInformation;
 import de.tuilmenau.ics.fog.topology.NeighborList;
 import de.tuilmenau.ics.fog.topology.RemoteMedium;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.gates.headers.NumberingHeader;
 import de.tuilmenau.ics.fog.ui.Decorator;
 import de.tuilmenau.ics.fog.ui.IPacketObserver;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.PacketLogger;
 import de.tuilmenau.ics.fog.ui.Viewable;
 import de.tuilmenau.ics.fog.ui.PacketQueue.PacketQueueEntry;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.RateMeasurement;
 
 
 /**
  * Extends ForwardingElement just because of RoutingService and GUI reasons. Only ForwardingElements
  * can be stored in the routing service and only them can be drawn in the GUI.
  */
 public class Bus extends Observable implements ILowerLayer, ForwardingElement, IPacketObserver, Decorator
 {
 	/**
 	 * It is static in order to enforce global unique bus IDs.
 	 * In reality it is not needed, but it makes debugging
 	 * in the simulation much easier.
 	 */
 	private static int lastUsedID = 0;
 	
 	/**
 	 * Dis-/Enables statistic information output. Just done in GUI mode,
 	 * since such detailed informations are not needed in large batch mode simulations. 
 	 */
 	public static final boolean OUTPUT_STATISTICS_VIA_DATASTREAM = (Config.Simulator.MODE != SimulatorMode.FAST_SIM);
 	
 
 	public Bus(AutonomousSystem pAS, String pName, Description pDescr)
 	{
 		setNewBusNumber();
 		mAS = pAS;
 		mLogger = new Logger(pAS.getLogger());
 		mName = pName;
 		mConfig = pAS.getSimulation().getConfig();
 		
 		packetLog = PacketLogger.createLogger(getTimeBase(), this, null);
 		packetLog.addObserver(this);
 
 		mDescription = new Description();
 		int tNewBandwidth = Config.getConfig().Scenario.DEFAULT_DATA_RATE_KBIT;
 		double tVariance = Config.getConfig().Scenario.DEFAULT_DATA_RATE_VARIANCE;
 		setPhysicalDataRate(tNewBandwidth, tVariance);
 		setDelayMSec(mConfig.Scenario.DEFAULT_DELAY_MSEC);
 		setPacketLossProbability(mConfig.Scenario.DEFAULT_PACKET_LOSS_PROP);
 		setBitErrorProbability(mConfig.Scenario.DEFAULT_BIT_ERROR_PROP);
 		mDelayConstant = mConfig.Scenario.DEFAULT_DELAY_CONSTANT;
 		
 		// if a description is given, override the default values
 		if(pDescr != null) {
 			Property prop = pDescr.get(DatarateProperty.class);
 			if(prop != null) {
 				int tDataRate = ((DatarateProperty) prop).getMax();
 				setPhysicalDataRate(tDataRate, ((DatarateProperty) prop).getVariance());
 			}
 			
 			prop = pDescr.get(DelayProperty.class);
 			if(prop != null) {
 				setDelayMSec(((DelayProperty) prop).getMin());
 			}
 			
 			prop = pDescr.get(LossRateProperty.class);
 			if(prop != null) {
 				setPacketLossProbability(((LossRateProperty) prop).getLossRate());
 			}
 		}
 		
 		if(OUTPUT_STATISTICS_VIA_DATASTREAM) {
 			DatastreamManager.autowire(this);
 			
 			mDatarateMeasurement = new RateMeasurement(getTimeBase(), this +".rate");
 		}
 	}
 	
 	private void setNewBusNumber()
 	{
 		mAS = null;
 		init();
 	}
 
 	public AutonomousSystem getAS()
 	{
 		return mAS;
 	}
 	
 	public String getASName()
 	{
 		return mAS.getName();
 	}
 	
 	public void init()
 	{
 		synchronized (busNumber) {
 			busID = busNumber;
 			busNumber++;
 		}
 	}
 	
 	public void setBroken(boolean pBroken, boolean pErrorTypeVisible)
 	{
 		boolean stateChange = broken != pBroken;
 		
 		// is it a repair operation?
 		boolean repaired = broken && !pBroken; 
 		
 		broken = pBroken;
 		if(broken) {
 			mErrorTypeVisible = pErrorTypeVisible;
 		} else {
 			// reset it do default
 			mErrorTypeVisible = Config.Routing.ERROR_TYPE_VISIBLE;
 		}
 		
 		if(stateChange) notifyObservers(pBroken);
 		
 		// initiate the repair operation
 		if(repaired) {
 			getTimeBase().scheduleIn(0, new IEvent() {
 				@Override
 				public void fire()
 				{
 					checkAfterRepair();
 				}
 			});
 		}
 	}
 	
 	/**
 	 * Method should be called after a repair operation in order
 	 * to inform attached nodes about the repaired Bus.
 	 */
 	private void checkAfterRepair()
 	{
 		for(LayerObserverCallback obs : observerList) {
 			try {
 				obs.neighborCheck();
 			}
 			catch(Exception tExc) {
 				// ignore exceptions; just report them
 				mLogger.err(this, "Ignoring exception from observer " +obs +" while check.", tExc);
 			}
 		}
 	}
 	
 	public Status isBroken()
 	{
 		if(broken) {
 			if(mErrorTypeVisible) {
 				return Status.BROKEN;
 			} else {
 				return Status.UNKNOWN_ERROR;
 			}
 		} else {
 			return Status.OK;
 		}
 	}
 	
 	public String getName()
 	{
 		return mName;
 	}
 	
 	public EventHandler getTimeBase()
 	{
 		return mAS.getTimeBase();
 	}
 	
 	public Logger getLogger()
 	{
 		return mLogger;
 	}
 	
 	public Description getDescription()
 	{
 		return mDescription;
 	}
 	
 	public int getBitErrorProbability()
 	{
 		return (int) (mBitErrorRate *100.0f);
 	}
 	
 	public void setBitErrorProbability(int newError)
 	{
 		if(newError < 0) newError = 0;
 		if(newError > 100) newError = 100;
 		
 		mBitErrorRate = (float) newError / 100.0f;
 	}
 
 	public int getPacketLossProbability()
 	{
 		return (int) (mPacketLossRate *100.0f);
 	}
 	
 	public void setPacketLossProbability(int newLoss)
 	{
 		if(newLoss < 0) newLoss = 0;
 		if(newLoss > 100) newLoss = 100;
 		
 		mPacketLossRate = (float) newLoss / 100.0f;
 		
 		// update description
 		mDescription.set(new LossRateProperty(getPacketLossProbability(), Limit.MIN));
 	}
 	
 	private void setPhysicalDataRate(int newBandwidth, double newBandwidthVariance)
 	{
 		mAvailableDataRate = newBandwidth;
 		mPhysMaxDataRate = newBandwidth;
 		
 		if(mPhysMaxDataRate.intValue() > 0) {
 			// update description
 			mDescription.set(new DatarateProperty(mPhysMaxDataRate.intValue(), newBandwidthVariance, Limit.MAX));
 		} else {
 			// Infinite data rate:
 			// remove previous limits from list
 			mDescription.remove(mDescription.get(DatarateProperty.class));
 		}
 	}
 
 	@Override
 	public void modifyAvailableDataRate(int pDataRateOffset)
 	{
 		DatarateProperty tPropDataRate = (DatarateProperty) mDescription.get(DatarateProperty.class);
 		
 		if(tPropDataRate != null) {
 			int tCurDataRate = tPropDataRate.getMax();
 			
 			mAvailableDataRate = tCurDataRate + pDataRateOffset;
 
 			mLogger.log(this, "Modifying available data rate by " + pDataRateOffset + " from " + tCurDataRate + " to " + mAvailableDataRate);
 			
 			if(tCurDataRate >= 0) {
 				tPropDataRate = new DatarateProperty(tCurDataRate + pDataRateOffset, Limit.MAX);
 				
 				//mLogger.log(this, "  ..old description: " + mDescription);	
 				mDescription.set(tPropDataRate);
 				//mLogger.log(this, "  ..new description: " + mDescription);	
 			}
 		}else{
 			mLogger.err(this, "Haven't found the data rate property");
 		}
 	}
 	
 	/**
 	 * @return
 	 */
 	public double getUtilization() 
 	{
 		double tResult = 0;
 
 		if(mAvailableDataRate.doubleValue() >= 0){
 			tResult = 100 - ((double)100 * mAvailableDataRate.doubleValue() / mPhysMaxDataRate.doubleValue());
 		}
 		
 		//mLogger.log(this, "Utilization: " + tResult);
 		
 		return tResult;
 	}
 
 	public long getAvailableDataRate()
 	{
 		return mAvailableDataRate.longValue();
 	}
 	
 	public long getDelayMSec()
 	{
 		return Math.round(mDelaySec * 1000.0d);
 	}
 	
 	public void setDelayMSec(long newDelayMSec)
 	{
 		mDelaySec = Math.max(0, (double) newDelayMSec / 1000.0d);
 		
 		// update description
 		mDescription.set(new DelayProperty((int)getDelayMSec(), Limit.MIN));
 	}
 	
 	/**
 	 * Registers a new receiving gate at the bus.
 	 * 
 	 * @param newnode Gate, which should be added.
 	 */
 	@Override
 	public NeighborInformation attach(String name, ILowerLayerReceive receivingNode)
 	{
 		if(!broken) {
 			HigherLayerRegistration higherLayer = new HigherLayerRegistration(getTimeBase(), mDatarateMeasurement, getLogger(), name, getNewID(), receivingNode);
 			
 			synchronized (nodelist) {
 				nodelist.add(higherLayer);
 			}
 			
 			for(LayerObserverCallback obs : observerList) {
 				try {
 					obs.neighborDiscovered(higherLayer.getNeighbor());
 				}
 				catch(Exception tExc) {
 					// ignore exceptions; just report them
 					mLogger.err(this, "Ignoring exception from observer " +obs +" while attach.", tExc);
 				}
 			}
 			
 			// create a new object because HigherLayerRegistration can not be serialised
 			return higherLayer.getNeighbor();
 		} else {
 			return null;
 		}
 	}
 	
 	private synchronized int getNewID()
 	{
 		lastUsedID++;
 		return lastUsedID;
 	}
 	
 	@Override
 	public SendResult sendPacketTo(NeighborInformation destination, Packet packet, NeighborInformation from)
 	{
 		if(Config.Connection.LOG_PACKET_STATIONS){
 			Logging.log(this, "Sending: " + packet + ", source: " + from + ", destination: " + destination);
 		}
 
 		if(destination != null) {
 			if(!broken) {
 				if(!isPacketLost(packet)) {
 					generateByteErrors(packet);
 					
 					//
 					// Calculate timing issues
 					//
 					double tNow = getTimeBase().now();
 					StreamTime tNowStream = null;
 					if(OUTPUT_STATISTICS_VIA_DATASTREAM) {
 						tNowStream = new StreamTime(tNow);
 					}
 					
 					double tAheadOfTime = Math.max(0, mNextFreeTimeSlot -tNow);
 					if(OUTPUT_STATISTICS_VIA_DATASTREAM) {
 						mAheadOfTime.write(tAheadOfTime, tNowStream);
 					}
 					
 					// check if bus is overloaded and if
 					// packet is dropped
 					if(Config.Transfer.PACKET_LOSS_AT_LINK_OVERLOAD) {
 						if(tAheadOfTime >= Config.Transfer.MAX_AHEAD_OF_TIME_SEC) {
 							if(OUTPUT_STATISTICS_VIA_DATASTREAM) {
 								mDroppedPackets.write(1.0d, tNowStream);
 							}
 
 							return SendResult.OK;
 						}
 					}
 					if(OUTPUT_STATISTICS_VIA_DATASTREAM) {
 						mDroppedPackets.write(0.0d, tNowStream);
 					}
 					
 					double tDelayForPacket = 0;
 					if(mDelayConstant) {
 						tDelayForPacket += mDelaySec;
 					} else {
 						if(mAvailableDataRate.floatValue() >= 0) {
 							// 1000 * kbit/s = bit/s
 							// bit/s / 8 = byte/s
 							double tBytesPerSecond = 1000 * mAvailableDataRate.floatValue() / 8;
 							
 							tDelayForPacket += (double)packet.getSerialisedSize() / tBytesPerSecond;
 						}
 						// else: data rate is infinity (no delay)
 					}
 					
 					double tPacketDeliverTime = Math.max(tNow, mNextFreeTimeSlot);
 					tPacketDeliverTime += tDelayForPacket;
 					mNextFreeTimeSlot = tPacketDeliverTime;
 					
 					if(Config.Transfer.DEBUG_PACKETS) {
 						if(mDelayConstant) {
 							mLogger.debug(this, "Bus delay is " + mDelaySec + "s (still blocked for " +tAheadOfTime +"s)");
 						} else {
 							mLogger.debug(this, "Bus data rate is " + mAvailableDataRate + "kbit/s and packet takes " +tDelayForPacket +"s delay (still blocked for " +tAheadOfTime +"s)");
 						}
 					}
 
 					//
 					// Store packet
 					//
 					int numberOfMatchingNeighbors = 0;
 					Status storeRes = Status.OK;
 					Envelope delivery = new Envelope(packet, from, tPacketDeliverTime, tPacketDeliverTime -tNow);
 					
 					packet.addBus(mName);
 					
 					// log packet for statistic
 					packetLog.add(packet);
 		
 					synchronized (nodelist) {
 						for(HigherLayerRegistration hl : nodelist) {
 							if(hl.getNeighbor().equals(destination) || (destination.equals(BROADCAST))) {
 								if(Config.Connection.LOG_PACKET_STATIONS){
 									Logging.log(this, "Storing: " + packet + ", in higher layer: " + hl);
 								}
 								numberOfMatchingNeighbors++;
 								// Inform queue for higher layer about packet.
 								// Packet will be cloned later, during the delivery process. 
 								storeRes = hl.storePacket(delivery);
 							}
 						}
 					}
 					
 					// inform others (esp. GUI) about state change
 					notifyObservers(packet);
 					
 					if((numberOfMatchingNeighbors == 0) && (destination != BROADCAST)) {
 						mLogger.err(this, "Neighbor '" +destination +"' not known.");
 						return SendResult.NEIGHBOR_NOT_KNOWN;
 					}
 					else if((numberOfMatchingNeighbors == 1) && (destination != BROADCAST)) {
 						switch(storeRes) {
 							case OK: return SendResult.OK;
 							case BROKEN: return SendResult.NEIGHBOR_NOT_REACHABLE;
 							default:
 								return SendResult.UNKNOWN_ERROR;
 						}
 					}
 					else {
 						return SendResult.OK;
 					}
 				} else {
 					//
 					// Packet gets lost
 					//
 					// log packet for statistic
 					if (Config.Transfer.DEBUG_PACKETS)
 						mLogger.log(this, "Lost packet " +packet);
 					packetLog.add(packet);
 					
 					// log end result of packet
 					if(OUTPUT_STATISTICS_VIA_DATASTREAM) {
 						mDroppedPackets.write(1.0d, new StreamTime(getTimeBase().now()));
 					}
 					packet.logStats(mAS.getSimulation(), this);
 					
 					return SendResult.OK;
 				}
 			}
 			else { // mBus.isBroken
 				mLogger.log(this, "Cannot send packet " + packet + " through broken bus");
 				if(mErrorTypeVisible) {
 					return SendResult.LOWER_LAYER_BROKEN;
 				} else {
 					return SendResult.UNKNOWN_ERROR;
 				}
 			}
 		} else {
 			// invalid destination reference
 			return SendResult.NEIGHBOR_NOT_KNOWN;
 		}
 	}
 
 	@Override
 	public synchronized void detach(ILowerLayerReceive receivingNode)
 	{
 		boolean found;
 		
 		// do loop for deleting *all* entries with the reference
 		// to the receiving node
 		do {
 			found = false;
 			
 			synchronized (nodelist) {
 				for(HigherLayerRegistration hl : nodelist) {
 					if(hl.is(receivingNode)) {
 						if(!broken) {
 							//
 							// inform observer about removed neighbor
 							//
 							for(LayerObserverCallback obs : observerList) {
 								try {
 									obs.neighborDisappeared(hl.getNeighbor());
 								}
 								catch(Exception tExc) {
 									// ignore exceptions; just report them
 									mLogger.err(this, "Ignoring exception from observer " +obs +" while detach.", tExc);
 								}
 							}
 						}
 						
 						found = true;
 						nodelist.remove(hl);
 						// iterator is invalid after removing -> leave for loop
 						break;
 					}
 				}
 			}
 		}
 		while(found);
 	}
 	
 
 	/**
 	 * Method for GUI in order to show more information about the connected
 	 * nodes at a bus.
 	 * 
 	 * @param forNeighbor Identification of neighbor, for which more information are requested
 	 * @return
 	 */
 	public synchronized ILowerLayerReceive getNeighborDetails(NeighborInformation forNeighbor)
 	{
 		if(forNeighbor != null) {
 			// search in list for this neighbor
 			synchronized (nodelist) {
 				for(HigherLayerRegistration hl : nodelist) {
 					NeighborInformation neighbor = hl.getNeighbor();
 					
 					if(forNeighbor.equals(neighbor)) {
 						return hl.getLowerLayerReceive();
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public synchronized NeighborList getNeighbors(NeighborInformation forMe)
 	{
 		NeighborList neighborlist = null;
 		
 		if(broken) {
 			neighborlist = new NeighborList(this);
 			
 			synchronized (nodelist) {
 				// copy elements from entity list
 				for(HigherLayerRegistration hl : nodelist) {
 					NeighborInformation neighbor = hl.getNeighbor();
 					
 					if(neighbor != null) {
 						// filter an entry from list?
 						if(forMe != null) {
 							if(!hl.getNeighbor().equals(forMe)) {
 								neighborlist.add(hl.getNeighbor());
 							}
 						} else {
 							neighborlist.add(hl.getNeighbor());
 						}
 					}
 				}
 			}
 		}
 		
 		return neighborlist;
 	}
 
 	@Override
 	public void registerObserverNeighborList(LayerObserverCallback observer)
 	{
 		if(!observerList.contains(observer))
 			observerList.add(observer);
 		
 	}
 
 	@Override
 	public boolean unregisterObserverNeighborList(LayerObserverCallback observer)
 	{
 		return observerList.remove(observer);	
 	}
 	
 	/**
 	 * Removes bus from the simulation.
 	 * 
 	 * TODO the nodes must be informed about the deletion of the bus!
 	 */
 	@Override
 	public synchronized void close()
 	{
 		synchronized (nodelist) {
 			nodelist.clear();
 		}
 		observerList.clear();
 		
 		setBroken(true, Config.Routing.ERROR_TYPE_VISIBLE);
 		
 		packetLog.deleteObserver(this);
 		packetLog.close();
 		packetLog = null;
 	}
 	
 	@Override
 	public RemoteMedium getProxy()
 	{
 		if(proxyForRemote == null) proxyForRemote = new RemoteMediumBus(this);
 		
 		return proxyForRemote;
 	}
 	
 	/**
 	 * @return Number of all packets processed by the bus.
 	 */
 	public int getNumberPackets()
 	{
 		if(packetLog != null) {
 			return packetLog.getPacketCounter();
 		} else {
 			return 0;
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		String tBandwith = "";
 		if ((mAvailableDataRate.doubleValue() >= 0) && (mPhysMaxDataRate.intValue() != -1)){
 			tBandwith = " BW=";
 			if(!mAvailableDataRate.equals(mPhysMaxDataRate)){
 				if(mPhysMaxDataRate.intValue() >= 1000 * 1000)
 					tBandwith += (mAvailableDataRate.intValue() / 1000000) + "/" + (mPhysMaxDataRate.intValue() / 1000000) + " Gbit/s";
 				else if(mPhysMaxDataRate.intValue() >= 1000)
 					tBandwith += (mAvailableDataRate.intValue() / 1000) + "/" + (mPhysMaxDataRate.intValue() / 1000) + " Mbit/s";
 				else
 					tBandwith += mAvailableDataRate.intValue() + "/" + mPhysMaxDataRate.intValue() + " kbit/s";
 			}else{
 				if(mPhysMaxDataRate.intValue() >= 1000 * 1000)
 					tBandwith += (mPhysMaxDataRate.intValue() / 1000000) + " Gbit/s";
 				else if(mPhysMaxDataRate.intValue() >= 1000)
 					tBandwith += (mPhysMaxDataRate.intValue() / 1000) + " Mbit/s";
 				else
 					tBandwith += mPhysMaxDataRate.intValue() + " kbit/s";
 			}
 		}
 		String tDelay = (mDelaySec != 0 ? " Del=" + mDelaySec * 1000 + "ms" : "");
 		
 		if (mName != null) {
 			return mName + "(" + busID + ")" + tBandwith + tDelay; 
 		} else {
 			return "bus(" + busID + ")" + tBandwith + tDelay;
 		}
 	}
 
 	@Override
 	public boolean equals(Object pOther)
 	{
 		if (pOther instanceof Bus){
 			Bus pOtherBus = (Bus)pOther;
 			
 			return (busID == pOtherBus.busID); 
 		}else{
 			return super.equals(pOther);
 		}
 	}
 	
 	@Override
 	public void notify(PacketLogger logger, EventType event, PacketQueueEntry packet)
 	{
 		notifyObservers(logger);
 	}
 
 	@Override
 	public synchronized void notifyObservers(Object pEvent)
 	{
 		setChanged();
 		super.notifyObservers(pEvent);
 	}
 	
 	/**
 	 * Just implemented for RoutingService and GUI reasons.
 	 * Method MUST NOT be used at all.
 	 */
 	@Override
 	public void handlePacket(Packet pPacket, ForwardingElement pLastHop)
 	{
 		throw new RuntimeException("Method Bus.handlePacket MUST NOT be used.");
 	}
 	
 	/**
 	 * @return Random decision if a packet gets lost in the lower layer. 
 	 */
 	private boolean isPacketLost(Packet packet)
 	{
 		if((mPacketLossRate > 0) && !packet.isInvisible()) {
 			return randomGenerator.nextFloat() <= mPacketLossRate;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * @return Randomize bytes in the payload (only type byte[]!) of a packet. 
 	 * The loss rate defines the amount of packets, related to 100%, which should have errors. 
 	 * The amount of errors per packet also depends on the defined error rate.
 	 * If packet loss is zero no bits will be modified!
 	 */
 	private boolean generateByteErrors(Packet packet)
 	{
 		if((mPacketLossRate > 0) && (mBitErrorRate > 0) && !packet.isSignalling() && !packet.isInvisible() && (randomGenerator.nextFloat() <= mBitErrorRate))
 		{
 			float tAdaptedLoss = mBitErrorRate * 10;
 			Object tPacketData = packet.getData();
 			Object tUserData = null;
 			
 			if(tPacketData instanceof NumberingHeader) {
 				NumberingHeader tNumberHeader = (NumberingHeader)packet.getData();
 				packet.setData(tNumberHeader.clone());
 				tUserData = tNumberHeader.getData();
 			} else {
 				tUserData = packet.getData();
 			}
 			
 			if(tUserData instanceof byte[])	{
 				byte[] tPacketPayloadArray = (byte[])tUserData;
 				byte[] tPacketPayloadArrayCopy = Arrays.copyOf(tPacketPayloadArray, tPacketPayloadArray.length);
 				
 				for(int i = 64 /* protect headers */; i < tPacketPayloadArrayCopy.length; i++)	{
 					if (randomGenerator.nextFloat() <= tAdaptedLoss)
 						tPacketPayloadArrayCopy[i] = (byte) (127 * (-randomGenerator.nextFloat())); 
 				}
 				if(tPacketData instanceof NumberingHeader) {
 					NumberingHeader tNumberHeader = (NumberingHeader)packet.getData();
 					tNumberHeader.setData(tPacketPayloadArrayCopy);
 					tNumberHeader.setIsCorrupted();
 				} else {
 					packet.setData(tPacketPayloadArrayCopy);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.ui.Decorator#getText()
 	 */
 	@Override
 	public String getText() 
 	{
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.ui.Decorator#getColor()
 	 */
 	@Override
 	public Color getColor() 
 	{
 		mLogger.log(this, "################### BLA BLA BLA");
 		
 		if(!mAvailableDataRate.equals(mPhysMaxDataRate))
 			return Color.GREEN;
 		else
 			return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.ui.Decorator#getImageName()
 	 */
 	@Override
 	public String getImageName() 
 	{
 		return null;
 	} 
 
 	private Logger mLogger;
 	private double mNextFreeTimeSlot = 0;
 	private Config mConfig;
 	
 	private static Integer busNumber = 0;
 	private PacketLogger packetLog;
 	private LinkedList<HigherLayerRegistration> nodelist = new LinkedList<HigherLayerRegistration>();
 	private LinkedList<LayerObserverCallback> observerList = new LinkedList<LayerObserverCallback>();
 	private static Random randomGenerator = new Random();
 	
 	@Viewable("ID")
 	private int busID;
 	
 	@Viewable("Broken")
 	private Boolean broken = false;
 	private boolean mErrorTypeVisible = Config.Routing.ERROR_TYPE_VISIBLE;
 	
 	@Viewable("Autonomous system")
 	private AutonomousSystem mAS = null;
 	@Viewable("Name")
 	private String mName = null;
 
 	@AutoWire(name="DeliveredPackets", type=DoubleNode.class, unique=true, prefix=true)
 	private IDoubleWriter mDeliveredPackets;
 	
 	@AutoWire(name="AheadOfTime", type=DoubleNode.class, unique=true, prefix=true)
 	private IDoubleWriter mAheadOfTime;
 	
 	@AutoWire(name="DroppedPackets", type=DoubleNode.class, unique=true, prefix=true)
 	private IDoubleWriter mDroppedPackets;
 	
 	private RateMeasurement mDatarateMeasurement = null;
 	
 	private RemoteMedium proxyForRemote = null;
 	
 	//
 	// QoS parameters for lower layer
 	//
 	@Viewable("Description")
 	private Description mDescription;
 	/**
 	 * Currently available data rate in [kbit/s] (1000 bit/s)
 	 */
 	@Viewable("AvailDataRate")
 	private Number mAvailableDataRate = 0; 
 	/**
 	 * Physical max. data rate in [kbit/s] (1000 bit/s)
 	 */
 	@Viewable("PhysMaxDataRate")
 	private Number mPhysMaxDataRate = 0;
 	@Viewable("Delay (sec)")
 	private double mDelaySec = 0;
 	@Viewable("Delay constant")
 	private boolean mDelayConstant = false;
 	@Viewable("Loss probability")
 	private float mPacketLossRate = 0;
 	@Viewable("Bit error probability")
 	private float mBitErrorRate = 0;
 }
 
