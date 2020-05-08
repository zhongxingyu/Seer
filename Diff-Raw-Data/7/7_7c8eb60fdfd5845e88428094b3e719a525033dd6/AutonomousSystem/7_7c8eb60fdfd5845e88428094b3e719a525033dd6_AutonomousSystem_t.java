 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
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
 package de.tuilmenau.ics.fog.topology;
 
 import java.rmi.RemoteException;
 
 import de.tuilmenau.ics.fog.EventHandler;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.application.Application;
 import de.tuilmenau.ics.fog.application.ReroutingExecutor;
 import de.tuilmenau.ics.fog.application.ReroutingExecutor.ReroutingSession;
 import de.tuilmenau.ics.fog.commands.CommandParsing;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.routing.RoutingServiceInstanceRegister;
 import de.tuilmenau.ics.fog.routing.simulated.RemoteRoutingService;
 import de.tuilmenau.ics.fog.routing.simulated.RoutingServiceSimulated;
 import de.tuilmenau.ics.fog.scenario.NodeConfiguratorContainer;
import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.SimpleName;
 import de.tuilmenau.ics.fog.util.ParameterMap;
 import de.tuilmenau.ics.middleware.JiniHelper;
 
 
 /**
  * Extends network container by a routing service and administrative domain.
  */
 public class AutonomousSystem extends Network implements IAutonomousSystem
 {
 	/**
 	 * Required to stop execution time of event handler during the execution of an command.
 	 */
 	private final static boolean ENABLE_SYNCHRONIZED_COMMAND_EXECUTION = true;
 	
 	/**
 	 * This is the AS, which allows for globally (related to a physical simulation machine) unique AS IDs.
 	 */
 	public static long sNextFreeAsID = 1;
 	
 	public AutonomousSystem(String pName, Simulation pSimulation, boolean pPartialRouting, String pPartialRoutingServiceName)
 	{	
 		super(pName, new Logger(pSimulation.getLogger()), pSimulation.getTimeBase());
 		
 		mName = pName;
 		mSim = pSimulation;
 		mAsID = createAsID();
		Logging.warn(this, "Created AS" + getGUIAsID() + ": " + mName);
 		RoutingServiceInstanceRegister register = RoutingServiceInstanceRegister.getInstance(pSimulation);
 		RemoteRoutingService tGrs = register.getGlobalRoutingService(mSim);
 		
 		if(pPartialRouting) {
 			if(pPartialRoutingServiceName != null) {
 				mRoutingService = register.get(pPartialRoutingServiceName);
 				
 				if(mRoutingService == null) {
 					mLogger.log(this, "Can not get routing service with name '" +pPartialRoutingServiceName +"'. Creating new one with this name.");
 				}
 			} else {
 				pPartialRoutingServiceName = mName;
 			}
 			
 			if(mRoutingService == null) {
 				mRoutingService = register.create(mSim, mSim.getTimeBase(), mLogger, pPartialRoutingServiceName, tGrs);
 			}
 		} else {
 			mRoutingService = tGrs;
 		}
 		
 		JiniHelper.registerService(IAutonomousSystem.class, this, mName);
 		mLogger.debug(this, "Registered Autonomous System with " + JiniHelper.getService(IAutonomousSystem.class, mName) );
 	}
 	
 	/**
 	 * Generates a new AsID
 	 * 
 	 * @return the AsID
 	 */
	static private synchronized long createAsID()
 	{
 		// get the current unique ID counter
 		long tResult = sNextFreeAsID * Simulation.uniqueIDsSimulationMachineMultiplier();
 
 		// make sure the next ID isn't equal
 		sNextFreeAsID++;
 	
 		return tResult;
 	}
 
 	/**
 	 * Returns the full AsID (including the machine specific multiplier)
 	 * 
 	 *  @return the full AsID
 	 */
 	public Long getAsID()
 	{
 		return mAsID;
 	}
 
 	/**
 	 * Returns the machine-local AsID (excluding the machine specific multiplier)
 	 * 
 	 * @return the machine-local AsID
 	 */
 	public Long getGUIAsID()
 	{
 		//TODO: if JINI is used, the function uniqueIDsSimulationMachineMultiplier() could return the wrong value here
 		if (getAsID() != null)
 			return getAsID() / Simulation.uniqueIDsSimulationMachineMultiplier();
 		else
 			return new Long(-1);
 	}
 
 	public RemoteRoutingService getRoutingService()
 	{
 		return mRoutingService;
 	}
 
 	public boolean createNode(String pName, ParameterMap pParameter)
 	{
 		Node newNode = new Node(pName, this, pParameter);
 		
 		String tRoutingConfigurator = mSim.getConfig().Scenario.ROUTING_CONFIGURATOR;
 		NodeConfiguratorContainer.getRouting().configure(tRoutingConfigurator, pName, this, newNode);
 		
 		FoGEntity layer = (FoGEntity) newNode.getLayer(FoGEntity.class);
 		if(layer != null) {
 			if(!layer.hasRoutingService()) {
 				// no routing service at all? -> create default routing service
 				layer.registerRoutingService(new RoutingServiceSimulated(getRoutingService(), pName, newNode));
 			}
 		}
 		
 		String tAppConfigurator = mSim.getConfig().Scenario.APPLICATION_CONFIGURATOR;
 		NodeConfiguratorContainer.getApplication().configure(tAppConfigurator, pName, this, newNode);
 		
 		return addNode(newNode);
 	}
 	
 	public boolean attach(String pNode, String pBus)
 	{
 		Node tNode = getNodeByName(pNode);
 		ILowerLayer tBus = getBusByName(pBus);
 		
 		if((tNode != null) && (tBus != null)) {
 			return attach(tNode, tBus);
 		} else {
 			mLogger.log(this, "Can not connect " +tNode +" to bus " +tBus);
 			return false;
 		}
 	}
 	
 	public boolean detach(String pNode, String pBus)
 	{
 		Node tNode = getNodeByName(pNode);
 		ILowerLayer tBus = getBusByName(pBus);
 		
 		if((tNode != null) && (tBus != null)) {
 			return detach(tNode, tBus);
 		} else {
 			mLogger.log(this, "Can not disconnect " +tNode +" from bus " +tBus);
 			return false;
 		}
 	}
 	
 	@Override
 	public synchronized boolean executeCommand(String pCmd)
 	{
 		// check if we have to pause the simulation in
 		// order to stop the simulation time during the
 		// execution of an command
 		boolean inEventThread = true;
 		if(ENABLE_SYNCHRONIZED_COMMAND_EXECUTION) {
 			inEventThread = mSim.getTimeBase().inEventThread();
 		}
 		
 		boolean result = false;
 		boolean paused = true;
 		
 		// even if we are not in the event thread,
 		// it might be acceptable is the simulation
 		// is running in real time
 		if(!inEventThread) {
 			inEventThread = !mSim.getTimeBase().isInFastMode();
 		}
 		
 		if(!inEventThread) {
 			paused = mSim.getTimeBase().isPaused();
 			
 			mSim.getTimeBase().pause(true);
 		}
 		
 		try {
 			result = CommandParsing.executeCommand(mSim, this, pCmd);
 		}
 		finally {
 			if(!inEventThread) {
 				// restore old state
 				if(!paused) {
 					mSim.getTimeBase().pause(false);
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public void cleanup()
 	{
 		super.cleanup();
 		
 		JiniHelper.unregisterService(IAutonomousSystem.class, this);
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "AS" + Long.toString(getGUIAsID()) + ":" + mName;
 	}
 	
 	public EventHandler getTimeBase()
 	{
 		return mSim.getTimeBase();
 	}
 	
 	public Simulation getSimulation()
 	{
 		return mSim;
 	}
 	
 	@Override
 	public String getRandomNodeString() throws RemoteException
 	{
 		return this.getRandomNode().toString();
 	}
 	
 	// TODO turn this into something generic for the experiment infrastructure (example RandomConnectScript)
 	public ReroutingSession establishConnection(String pSendingNode, String pTargetNode)
 	{
 		Node tNode = getNodeByName(pSendingNode);
 
 		for(Application tApplication : tNode.getApps()) {
 			if(tApplication instanceof ReroutingExecutor) {
 				ReroutingExecutor tExecutor = ((ReroutingExecutor)tApplication);
 				ReroutingSession tSession = tExecutor.new ReroutingSession(true);
 
 				Connection tConnection = null;
 				tConnection = tNode.getLayer(null).connect(new SimpleName(new Namespace("rerouting"), pTargetNode), tExecutor.getDescription(), null);
 
 				tSession.start(tConnection);
 				if(!JiniHelper.isEnabled()) {
 					return tSession;
 				} else {
 					/*
 					 * register remote interface but do not make ReroutingSession accessible
 					 */
 					JiniHelper.registerService(ReroutingSession.class, tSession, "Rerouting:" + pSendingNode + "->" + pTargetNode);
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 	
 	private RemoteRoutingService mRoutingService;
 	private String mName;
 	private Simulation mSim;
 	private long mAsID = 0;
 }
