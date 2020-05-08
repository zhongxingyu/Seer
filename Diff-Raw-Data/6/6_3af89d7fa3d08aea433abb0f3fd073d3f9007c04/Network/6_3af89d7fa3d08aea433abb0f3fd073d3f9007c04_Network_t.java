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
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import de.tuilmenau.ics.fog.EventHandler;
 import de.tuilmenau.ics.fog.facade.Host;
 import de.tuilmenau.ics.fog.routing.simulated.RemoteRoutingService;
import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import de.tuilmenau.ics.middleware.JiniHelper;
 
 
 /**
  * Container for collecting "physical" nodes and links somehow belonging together.
  * In addition this container provides a graph representation for drawing the GUI. 
  */
 public class Network
 {
 	public Network(String pName, Logger pLogger, EventHandler pTimeBase)
 	{
 		mName = pName;
 		mLogger = pLogger;
 		mTimeBase = pTimeBase;
 	}
 	
 	public synchronized boolean addNode(Node newNode)
 	{
 		String name = newNode.getName();
 		boolean tOk = false;
 		
 		if(!containsNode(name) && (name != null)) {
 			nodelist.put(name, newNode);
 		
 			mScenario.add(newNode);
 			tOk = true;
		}else{
			Logging.err(this, "A node with name " + name + " already exists");
		}
 		
 		return tOk;
 	}
 	
 	public boolean containsNode(String name)
 	{
 		return nodelist.containsKey(name);
 	}
 	
 	public boolean removeNode(String name)
 	{
 		return removeNode(name, true);
 	}
 
 	private synchronized boolean removeNode(String name, boolean informElement)
 	{
 		boolean tOk = false;
 		
 		Node tNode = nodelist.remove(name);
 		
 		if(tNode != null) {
 			mScenario.remove(tNode);
 			
 			if(informElement) tNode.deleted();
 			tOk = true;
 		} else {
 			mLogger.log(this, "Can not remove node. " +name +" not known.");
 		}
 		
 		return tOk;
 	}
 
 	public synchronized boolean setNodeBroken(String pNode, boolean pBroken, boolean pErrorTypeVisible)
 	{
 		boolean tOk = false;
 		Node tNode = getNodeByName(pNode);
 		
 		if (tNode != null) {
 			tNode.setBroken(pBroken, pErrorTypeVisible);
 			tOk = true;
 		} else {
 			mLogger.log(this, "Can not " + (pBroken ? "break" : "repair") +
 				" node. " + pNode +" not known.");
 		}
 		return tOk;
 	}
 	
 	public synchronized boolean setBusBroken(String pBus, boolean pBroken, boolean pErrorTypeVisible)
 	{
 		boolean tOk = false;
 		ILowerLayer tBus = getBusByName(pBus);
 		
 		if (tBus != null) {
 			try {
 				tBus.setBroken(pBroken, pErrorTypeVisible);
 				tOk = true;
 			}
 			catch(RemoteException tExc) {
 				mLogger.err(this, "Can not " + (pBroken ? "break" : "repair") +
 						" bus. " + pBus +" due to exception " +tExc);
 			}
 		} else {
 			mLogger.err(this, "Can not " + (pBroken ? "break" : "repair") +
 				" bus. " + pBus +" not known.");
 		}
 		
 		return tOk;
 	}
 	
 	public String getName()
 	{
 		return mName;
 	}
 	
 	public synchronized boolean addBus(ILowerLayer newBus)
 	{
 		boolean tOk = false;
 		
 		try {
 			String name = newBus.getName();
 			if(!containsBus(name)) {
 				if(name != null) {
 					buslist.put(name, newBus);
 				} else {
 					buslist.put(newBus.getName(), newBus);
 				}
 				
 				mScenario.add(newBus);
 				
 				if(name != null) {
 					RemoteMedium proxy = newBus.getProxy();
 					
 					// is it the original object?
 					if(proxy != null) {
 						JiniHelper.registerService(RemoteMedium.class, proxy, name);
 						mLogger.debug(this, "Registered bus with " + JiniHelper.getService(RemoteMedium.class, name));
 					}
 				}
 				
 				tOk = true;
 			}
 		}
 		catch(RemoteException exc) {
 			mLogger.err(this, "Can not add bus because it is not accessible.", exc);
 		}
 		
 		return tOk;
 	}
 
 	public boolean containsBus(String name)
 	{
 		return buslist.containsKey(name);
 	}
 	
 	public boolean removeBus(String pName)
 	{
 		return removeBus(pName, true);
 	}
 	
 	private synchronized boolean removeBus(String pName, boolean pInformElement)
 	{
 		boolean tRes = false;
 		
 		ILowerLayer tBus = buslist.remove(pName);
 		
 		if(tBus != null) {
 			tRes = true;
 			
 			try {
 				RemoteMedium proxy = tBus.getProxy();
 				if(proxy != null) {
 					JiniHelper.unregisterService(RemoteMedium.class, proxy);
 				}
 			}
 			catch(RemoteException tExc) {
 				mLogger.err(this, "Can not remove bus " +pName +" from JINI registry.", tExc);
 			}
 			
 			mScenario.remove(tBus);
 			
 			if(pInformElement) {
 				try {
 					tBus.close();
 				} catch (RemoteException tExc) {
 					mLogger.err(this, "Can not close bus " + tBus +" due to exception " +tExc);
 				}
 			}
 		} else {
 			mLogger.log(this, "Can not remove bus. " +pName +" not known or not local.");
 		}
 		
 		return tRes;
 	}
 
 	/**
 	 * @param name Name of requested node
 	 * @return Reference to node with name or null, if no such node exists
 	 */
 	public Node getNodeByName(String name)
 	{
 		return nodelist.get(name);
 	}
 	
 	/**
 	 * @return a random node from the network
 	 */
 	public Node getRandomNode()
 	{
 		Collection<Node> nodes = nodelist.values();
 		Node node = null;
 		
 		// get random entry index starting from 1...size
 		// details: random [0...1) * size => 0...(size-1)
 		int randomEntry = 1+ (int) (Math.random()*nodes.size());
 		
 		// go through list until random position was reached 
 		Iterator<Node> it = nodes.iterator();
 		while(it.hasNext()) {
 			randomEntry--;
 			node = it.next();
 			
 			if(randomEntry <= 0) {
 				break;
 			}
 		}
 		
 		return node;
 	}
 	
 	public Host getHostByName(String name)
 	{
 		Node node = nodelist.get(name);
 		
 		if(node != null) return node.getHost();
 		else return null;
 	}
 	
 	public synchronized ILowerLayer getBusByName(String name)
 	{
 		ILowerLayer tRes = buslist.get(name);
 		
 		// locally not available? => try it via RMI
 		if(tRes == null) {
 			RemoteMedium tProxy = (RemoteMedium) JiniHelper.getService(RemoteMedium.class, name);
 
 			if(tProxy != null) {
 				tRes = tProxy.activate(mTimeBase, mLogger);
 	
 				if(tRes != null) {
 					buslist.put(name, tRes);
 				}
 			}
 			// else: nothing available via Jini => bus not known
 		}
 		
 		return tRes;
 	}
 	
 	public HashMap<String, Node> getNodelist()
 	{
 		return nodelist;
 	}
 	
 	public boolean attach(Node node, ILowerLayer lowerLayer)
 	{
 		NetworkInterface interf = node.attach(lowerLayer);
 		
 		if(interf != null) {
 			mScenario.storeLink(node, lowerLayer, interf);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/*
 	 * @param rsA partial RoutingService
 	 * @param rsB global/upper RoutingService
 	 */
 	public boolean attach(RemoteRoutingService rsA, RemoteRoutingService rsB)
 	{
 		mScenario.storeLink(rsA, rsB, "routing for " + rsA);
 		return true;
 	}
 	
 	public boolean detach(Node node, ILowerLayer lowerLayer)
 	{
 		NetworkInterface interf = node.detach(lowerLayer);
 		
 		if(interf != null) {
 			mScenario.unlink(interf);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public int numberOfNodes()
 	{
 		return nodelist.size();
 	}
 	
 	public int numberOfBuses()
 	{
 		return buslist.size();
 	}
 	
 	/**
 	 * Removes all elements of the network as if they had
 	 * been deleted one by one.
 	 */
 	public void removeAll()
 	{
 		removeAll(true);
 	}
 	
 	/**
 	 * Empties network without informing the elements about that.
 	 * It is used to terminate a simulation without needing the
 	 * simulated elements to shutdown properly.
 	 */
 	public void cleanup()
 	{
 		removeAll(false);
 	}
 
 	private synchronized void removeAll(boolean informElements)
 	{
 		mLogger.trace(this, "Removing whole network (inform elements = " +informElements +")");
 		
 		// 1. inform nodes that they should shut down
 		if(informElements) {
 			for(Node node : nodelist.values()) {
 				node.shutdown(true);
 			}
 		}
 
 		// 2. remove them from network
 		while(!nodelist.isEmpty()) {
 			String tNodeName = nodelist.keySet().iterator().next();
 			
 			removeNode(tNodeName, informElements);
 		}
 		
 		// 3. delete buses
 		while(!buslist.isEmpty()) {
 			String tBusName = buslist.keySet().iterator().next();
 			
 			removeBus(tBusName, informElements);
 		}
 	}
 	
 	public Logger getLogger()
 	{
 		return mLogger;
 	}
 	
 	public RoutableGraph<Object, Object> getGraph()
 	{
 		return mScenario;
 	}
 	
 	protected Logger mLogger;
 	
 	private String mName = null;
 	private RoutableGraph<Object, Object> mScenario = new RoutableGraph<Object, Object>();
 	private EventHandler mTimeBase;
 	
 	private HashMap<String, Node> nodelist = new HashMap<String, Node>();
 	private HashMap<String, ILowerLayer>  buslist  = new HashMap<String, ILowerLayer>();
 
 }
