 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical;
 
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 
 /**
  * HRM Routing: The class describes a routing entry consisting of
  * 			1.) Destination: either a node or an aggregated address (a cluster)
  * 			2.) Next hop: either a node or an aggregated address (a cluster)
  * 			3.) Hop count: the hop costs this route causes
  * 			4.) Utilization: the route usage, e.g., 0.59 for 59%
  * 			5.) Min. delay [ms]: the additional delay, which is caused minimal from this route
  * 			6.) Max. data rate [Kb/s]: the data rate, which is possible via this route under optimal circumstances
  */
 public class RoutingEntry implements RouteSegment
 {
	private static final long serialVersionUID = 1328799038900154655L;

 	/**
 	 * Defines a constant value for "no hop costs".
 	 */
 	public static final int NO_HOP_COSTS = 0;
 
 	/**
 	 * Defines a constant value for "no utilization".
 	 */
 	public static final float NO_UTILIZATION = -1;
 
 	/**
 	 * Defines a constant value for "no delay".
 	 */
 	public static final long NO_DELAY = -1;
 
 	/**
 	 * Defines a constant value for "infinite data rate".
 	 */
 	public static final long INFINITE_DATARATE = -1;
 
 	
 	/**
 	 * Stores the destination of this route entry.
 	 */
 	private HRMID mDestination = null;
 	
 	/**
 	 * Stores the source of this route entry.
 	 */
 	private HRMID mSource = null;
 
 	/**
 	 * Stores the next hop of this route entry.
 	 */
 	private HRMID mNextHop = null;
 	
 	/**
 	 * Stores the hop costs the described route causes.
 	 */
 	private int mHopCount = NO_HOP_COSTS;
 	
 	/**
 	 * Stores the utilization of the described route.
 	 */
 	private float mUtilization = NO_UTILIZATION;
 	
 	/**
 	 * Stores the minimum additional delay[ms] the described route causes.
 	 */
 	private long mMinDelay = NO_DELAY;
 	
 	/**
 	 * Stores the maximum data rate[Kb/s] the described route might provide.
 	 */
 	private long mMaxDataRate = INFINITE_DATARATE;
 
 	/**
 	 * Stores if the route describes a local loop.
 	 * (for GUI only)
 	 */
 	private boolean mLocalLoop = false; 
 	
 	/**
 	 * Stores if the route describes a link to a neighbor node.
 	 * (for GUI only)
 	 */
 	private boolean mRouteToDirectNeighbor = false;
 
 	/**
 	 * Stores the L2 address of the next hop if known
 	 */
 	private L2Address mNextHopL2Address = null;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pSource the source of this route
 	 * @param pDestination the destination of this route
 	 * @param pNextHop the next hop for this route
 	 * @param pHopCount the hop costs
 	 * @param pUtilization the utilization of the described route
 	 * @param pMinDelay the minimum additional delay the described route causes
 	 * @param pMaxDataRate the maximum data rate the described route might provide
 	 */
 	private RoutingEntry(HRMID pSource, HRMID pDestination, HRMID pNextHop, int pHopCount, float pUtilization, long pMinDelay, long pMaxDataRate)
 	{
 		mDestination = pDestination;
 		mSource = pSource;
 		mNextHop = pNextHop;
 		mHopCount = pHopCount;
 		mUtilization = pUtilization;
 		mMinDelay = pMinDelay;
 		mMaxDataRate = pMaxDataRate;
 		mLocalLoop = false;
 		mRouteToDirectNeighbor = false;
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pDestination the destination of this route
 	 * @param pNextHop the next hop for this route
 	 * @param pHopCount the hop costs
 	 * @param pUtilization the utilization of the described route
 	 * @param pMinDelay the minimum additional delay the described route causes
 	 * @param pMaxDataRate the maximum data rate the described route might provide
 	 */
 	private RoutingEntry(HRMID pDestination, HRMID pNextHop, int pHopCount, float pUtilization, long pMinDelay, long pMaxDataRate)
 	{
 		this(null, pDestination, pNextHop, pHopCount, pUtilization, pMinDelay, pMaxDataRate); 
 	}
 	
 	/**
 	 * Factory function: creates a routing loop, which is used for routing traffic on the local host.
 	 * 
 	 * @param pLoopAddress the address which defines the destination and next hop of this route
 	 */
 	public static RoutingEntry createLocalhostEntry(HRMID pLoopAddress)
 	{
 		// create instance
 		RoutingEntry tEntry = new RoutingEntry(null, pLoopAddress, pLoopAddress, NO_HOP_COSTS, NO_UTILIZATION, NO_DELAY, INFINITE_DATARATE);
 
 		// mark as local loop
 		tEntry.mLocalLoop = true;
 
 		// set the source of this route
 		tEntry.mSource = pLoopAddress;
 		
 		// return with the entry
 		return tEntry;
 	}
 
 	/**
 	 * Factory function: creates a general route
 	 * 
 	 * @param pSource the source of the route
 	 * @param pDestination the direct neighbor
 	 * @param pNextHop the next hop for the route
 	 * @param pHopCount the hop costs
 	 * @param pUtilization the utilization of the described route
 	 * @param pMinDelay the minimum additional delay the described route causes
 	 * @param pMaxDataRate the maximum data rate the described route might provide
 	 */
 	public static RoutingEntry create(HRMID pSource, HRMID pDestination, HRMID pNextHop, int pHopCount, float pUtilization, long pMinDelay, long pMaxDataRate)
 	{
 		// create instance
 		RoutingEntry tEntry = new RoutingEntry(pSource, pDestination, pNextHop, pHopCount, pUtilization, pMinDelay, pMaxDataRate);
 		
 		// return with the entry
 		return tEntry;
 	}
 
 	/**
 	 * Factory function: creates a route to a direct neighbor.
 	 * 
 	 * @param pSource the source of the route
 	 * @param pDestination the direct neighbor
 	 * @param pNextHop the next hop for the route
 	 * @param pUtilization the utilization of the described route
 	 * @param pMinDelay the minimum additional delay the described route causes
 	 * @param pMaxDataRate the maximum data rate the described route might provide
 	 */
 	public static RoutingEntry createRouteToDirectNeighbor(HRMID pSource, HRMID pDestination, HRMID pNextHop, float pUtilization, long pMinDelay, long pMaxDataRate)
 	{
 		// create instance
 		RoutingEntry tEntry = create(pSource, pDestination, pNextHop, HRMConfig.Routing.HOP_COSTS_TO_A_DIRECT_NEIGHBOR, pUtilization, pMinDelay, pMaxDataRate);
 		
 		// mark as local loop
 		tEntry.mRouteToDirectNeighbor = true;
 		
 		// return with the entry
 		return tEntry;
 	}
 
 	/**
 	 * Factory function: creates a route to a direct neighbor.
 	 * 
 	 * @param pSource the source of the route
 	 * @param pDirectNeighbor the direct neighbor
 	 * @param pUtilization the utilization of the described route
 	 * @param pMinDelay the minimum additional delay the described route causes
 	 * @param pMaxDataRate the maximum data rate the described route might provide
 	 */
 	public static RoutingEntry createRouteToDirectNeighbor(HRMID pSource, HRMID pDirectNeighbor, float pUtilization, long pMinDelay, long pMaxDataRate)
 	{
 		// return with the entry
 		return createRouteToDirectNeighbor(pSource, pDirectNeighbor, pDirectNeighbor, pUtilization, pMinDelay, pMaxDataRate);
 	}
 
 	/**
 	 * Factory function: creates a route to a direct neighbor.
 	 * 
 	 * @param pDirectNeighbor the destination of the direct neighbor
 	 * @param pUtilization the utilization of the described route
 	 * @param pMinDelay the minimum additional delay the described route causes
 	 * @param pMaxDataRate the maximum data rate the described route might provide
 	 */
 	public static RoutingEntry createRouteToDirectNeighbor(HRMID pDirectNeighbor, float pUtilization, long pMinDelay, long pMaxDataRate)
 	{
 		// return with the entry
 		return createRouteToDirectNeighbor(null, pDirectNeighbor, pUtilization, pMinDelay, pMaxDataRate);
 	}
 
 	/**
 	 * Defines the L2 address of the next hop
 	 * 
 	 * @param pDestL2Address the L2 address of the next hop
 	 */
 	public void setNextHopL2Address(L2Address pNextHopL2Address)
 	{
 		mNextHopL2Address = pNextHopL2Address;	
 	}
 	
 	/**
 	 * Returns the L2 address of the next hop of this route (if known)
 	 * 
 	 * @return the L2 address of the next hop, returns null if none is known
 	 */
 	public L2Address getNextHopL2Address()
 	{
 		return mNextHopL2Address;
 	}
 	
 	/**
 	 * Returns the source of the route
 	 * 
 	 * @return the source
 	 */
 	public HRMID getSource()
 	{
 		return mSource;
 	}
 
 	/**
 	 * Returns the destination of the route
 	 * 
 	 * @return the destination
 	 */
 	public HRMID getDest()
 	{
 		return mDestination;
 	}
 	
 	/**
 	 * Returns the next hop of this route
 	 * 
 	 * @return the next hop
 	 */
 	public HRMID getNextHop()
 	{
 		return mNextHop;
 	}
 	
 	/**
 	 * Returns the hop costs of this route
 	 * 
 	 * @return the hop costs
 	 */
 	public int getHopCount()
 	{
 		return mHopCount;
 	}
 	
 	/**
 	 * Returns the utilization of this route
 	 *  
 	 * @return the utilization
 	 */
 	public float getUtilization()
 	{
 		return mUtilization;
 	}
 	
 	/**
 	 * Returns the minimum additional delay this route causes.
 	 * 
 	 * @return the minimum additional delay
 	 */
 	public long getMinDelay()
 	{
 		return mMinDelay;
 	}
 	
 	/**
 	 * Returns the maximum data rate this route might provide.
 	 * 
 	 * @return the maximum data rate
 	 */
 	public long getMaxDataRate()
 	{
 		return mMaxDataRate;
 	}
 	
 	/**
 	 * Determines if the route describes a local loop.
 	 * (for GUI only)
 	 * 
 	 * @return true if the route is a local loop, otherwise false
 	 */
 	public boolean isLocalLoop()
 	{
 		return mLocalLoop;
 	}
 	
 	/**
 	 * Determines if the route ends at a direct neighbor.
 	 * (for GUI only)
 	 * 
 	 * @return true if the route is such a route, otherwise false
 	 */
 	public boolean isRouteToDirectNeighbor()
 	{
 		return mRouteToDirectNeighbor;
 	}
 
 	/**
 	 * Creates an identical duplicate
 	 * 
 	 * @return the identical duplicate
 	 */
 	public RoutingEntry clone()
 	{
 		// create object copy
 		RoutingEntry tResult = new RoutingEntry(mSource, mDestination, mNextHop, mHopCount, mUtilization, mMinDelay, mMaxDataRate);
 		
 		// update the flag "route to direct neighbor"
 		tResult.mRouteToDirectNeighbor = mRouteToDirectNeighbor;
 		
 		// update flag "route to local host"
 		tResult.mLocalLoop = mLocalLoop;
 		
 		// update next hop L2 address
 		tResult.mNextHopL2Address = mNextHopL2Address;
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns if both objects address the same cluster/coordinator
 	 * 
 	 * @return true or false
 	 */
 	@Override
 	public boolean equals(Object pObj)
 	{
 		if(pObj instanceof RoutingEntry){
 			RoutingEntry tOther = (RoutingEntry)pObj;
 			
 			if((getDest() != null) && (getDest().equals(tOther.getDest())) &&
 			   (getNextHop() != null) && (getNextHop().equals(tOther.getNextHop()))){
 				return true;
 			}
 		}
 		
 		return false;
 	}	
 
 	/**
 	 * Returns the size of a serialized representation
 	 * 
 	 * @return the size
 	 */
 	@Override
 	public int getSerialisedSize()
 	{
 		// TODO: implement me
 		return 0;
 	}
 
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
		return getClass().getSimpleName() + "(" + (getSource() != null ? "Source=" + getSource() + ", " : "") + "Dest.=" + getDest() + ", Next=" + getNextHop() + (getNextHopL2Address() != null ? ", NextL2=" + getNextHopL2Address() : "") + ", Hops=" + (getHopCount() > 0 ? getHopCount() : "none") + ", Util=" + (getUtilization() > 0 ? getUtilization() : "none") + ", MinDel=" + (getMinDelay() > 0 ? getMinDelay() : "none") + ", MaxDR=" + (getMaxDataRate() > 0 ? getMaxDataRate() : "inf.") + ")"; 
 	}
 }
