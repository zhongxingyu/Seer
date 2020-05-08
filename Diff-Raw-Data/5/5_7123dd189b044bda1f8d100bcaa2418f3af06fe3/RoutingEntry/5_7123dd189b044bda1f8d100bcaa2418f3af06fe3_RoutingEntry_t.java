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
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.AbstractRoutingGraph;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.AbstractRoutingGraphLink;
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
 	public static final float NO_UTILIZATION = 0;
 
 	/**
 	 * Defines a constant value for "no delay".
 	 */
 	public static final long NO_DELAY = 0;
 
 	/**
 	 * Defines a constant value for "infinite data rate".
 	 */
 	public static final long INFINITE_DATARATE = Long.MAX_VALUE;
 
 	
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
 	 * Stores the utilization[%] of the described route.
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
 	 * Stores the cause for this entry.
 	 * This variable is not part of the concept. It is only for GUI/debugging use.
 	 */
 	private LinkedList<String> mCause = new LinkedList<String>();
 	
 	/**
 	 * Stores if this entry belongs to an HRG instance.
 	 * This variable is not part of the concept. It is only used to simplify the implementation. Otherwise, the same class has to be duplicated with very minor differences in order to be used within a HRG
 	 */
 	private boolean mBelongstoHRG = false;
 	
 	/**
 	 * Stores the timeout value of this route entry
 	 */
 	private double mTimeout = 0;
 	
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
 	 * @param pCause the cause for this routing table entry
 	 */
 	@SuppressWarnings("unchecked")
 	private RoutingEntry(HRMID pSource, HRMID pDestination, HRMID pNextHop, int pHopCount, float pUtilization, long pMinDelay, long pMaxDataRate, LinkedList<String> pCause)
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
 		if((pCause != null) && (!pCause.isEmpty())){
 			mCause = (LinkedList<String>) pCause.clone();
 		}
 	}
 	
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
 	 * @param pCause the cause for this routing table entry
 	 */
 	private RoutingEntry(HRMID pSource, HRMID pDestination, HRMID pNextHop, int pHopCount, float pUtilization, long pMinDelay, long pMaxDataRate, String pCause)
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
 		if((pCause != null) && (!pCause.isEmpty())){
 			mCause.add(pCause);
 		}
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
 	 * @param pCause the cause for this routing table entry
 	 */
 	private RoutingEntry(HRMID pDestination, HRMID pNextHop, int pHopCount, float pUtilization, long pMinDelay, long pMaxDataRate, String pCause)
 	{
 		this(null, pDestination, pNextHop, pHopCount, pUtilization, pMinDelay, pMaxDataRate, pCause); 
 	}
 	
 	/**
 	 * Factory function: creates a routing loop, which is used for routing traffic on the local host.
 	 * 
 	 * @param pLoopAddress the address which defines the destination and next hop of this route
 	 * @param pCause the cause for this routing table entry
 	 */
 	public static RoutingEntry createLocalhostEntry(HRMID pLoopAddress, String pCause)
 	{
 		// create instance
 		RoutingEntry tEntry = new RoutingEntry(null, pLoopAddress, pLoopAddress, NO_HOP_COSTS, NO_UTILIZATION, NO_DELAY, INFINITE_DATARATE, pCause);
 
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
 	 * @param pCause the cause for this routing table entry
 	 */
 	public static RoutingEntry create(HRMID pSource, HRMID pDestination, HRMID pNextHop, int pHopCount, float pUtilization, long pMinDelay, long pMaxDataRate, LinkedList<String> pCause)
 	{
 		// create instance
 		RoutingEntry tEntry = new RoutingEntry(pSource, pDestination, pNextHop, pHopCount, pUtilization, pMinDelay, pMaxDataRate, pCause);
 		
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
 	 * @param pCause the cause for this routing table entry
 	 */
 	public static RoutingEntry create(HRMID pSource, HRMID pDestination, HRMID pNextHop, int pHopCount, float pUtilization, long pMinDelay, long pMaxDataRate, String pCause)
 	{
 		// create instance
 		RoutingEntry tEntry = new RoutingEntry(pSource, pDestination, pNextHop, pHopCount, pUtilization, pMinDelay, pMaxDataRate, pCause);
 		
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
 	 * @param pCause the cause for this routing table entry
 	 */
 	public static RoutingEntry createRouteToDirectNeighbor(HRMID pSource, HRMID pDestination, HRMID pNextHop, float pUtilization, long pMinDelay, long pMaxDataRate, String pCause)
 	{
 		// create instance
 		RoutingEntry tEntry = create(pSource, pDestination, pNextHop, HRMConfig.Routing.HOP_COSTS_TO_A_DIRECT_NEIGHBOR, pUtilization, pMinDelay, pMaxDataRate, pCause);
 		
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
 	 * @param pCause the cause for this routing table entry
 	 */
 	public static RoutingEntry createRouteToDirectNeighbor(HRMID pSource, HRMID pDirectNeighbor, float pUtilization, long pMinDelay, long pMaxDataRate, String pCause)
 	{
 		// return with the entry
 		return createRouteToDirectNeighbor(pSource, pDirectNeighbor, pDirectNeighbor, pUtilization, pMinDelay, pMaxDataRate, pCause);
 	}
 
 	/**
 	 * Factory function: creates a route to a direct neighbor.
 	 * 
 	 * @param pDirectNeighbor the destination of the direct neighbor
 	 * @param pUtilization the utilization of the described route
 	 * @param pMinDelay the minimum additional delay the described route causes
 	 * @param pMaxDataRate the maximum data rate the described route might provide
 	 * @param pCause the cause for this routing table entry
 	 */
 	public static RoutingEntry createRouteToDirectNeighbor(HRMID pDirectNeighbor, float pUtilization, long pMinDelay, long pMaxDataRate, String pCause)
 	{
 		// return with the entry
 		return createRouteToDirectNeighbor(null, pDirectNeighbor, pUtilization, pMinDelay, pMaxDataRate, pCause);
 	}
 
 	/**
 	 * Assigns the entry to an HRG instance
 	 */
 	public void assignToHRG(AbstractRoutingGraph<HRMID, AbstractRoutingGraphLink> pMHierarchicalRoutingGraph)
 	{
 		mBelongstoHRG = true;
 	}
 	
 	/**
 	 * Extends the cause string
 	 * 
 	 * @param pCause the additional cause string
 	 */
 	public void extendCause(String pCause)
 	{
 		mCause.add(pCause);
 	}
 	
 	/**
 	 * Returns the cause for this entry
 	 * 
 	 * @return the cause
 	 */
 	public LinkedList<String> getCause()
 	{
 		return mCause;
 	}
 	
 	/**
 	 * Sets a new timeout for this route entry
 	 * 
 	 * @param pTimeout the new timeout
 	 */
 	public void setTimeout(double pTimeout)
 	{
 		mTimeout = pTimeout;
 	}
 	
 	/**
 	 * Returns the timeout for this route entry
 	 * 
 	 * @return the new timeout
 	 */
 	public double getTimeout()
 	{
 		return mTimeout;
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
 	 * Sets a new destination
 	 * 
 	 * @param pDestination the new destination
 	 */
 	public void setDest(HRMID pDestination)
 	{
 		mDestination = pDestination;
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
 	 * Sets a new next hop
 	 * 
 	 * @param pNextHop the new next hop
 	 */
 	public void setNextHop(HRMID pNextHop)
 	{
 		mNextHop = pNextHop;
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
 
 	public void chain(RoutingEntry pOtherEntry)
 	{
 		// HOP COUNT -> add both
 		mHopCount += pOtherEntry.mHopCount;
 		
 		// MIN DELAY -> add both
 		mMinDelay += pOtherEntry.mMinDelay;
 		
 		// MAX DATA RATE -> find the minimum
 		mMaxDataRate = (mMaxDataRate < pOtherEntry.mMaxDataRate ? mMaxDataRate : pOtherEntry.mMaxDataRate);
 		
 		// UTILIZATION -> find the maximum
 		mUtilization = (mUtilization > pOtherEntry.mUtilization ? mUtilization : pOtherEntry.mUtilization);
 
 		// how should we combine both route entries?
 		if(mSource.equals(pOtherEntry.mNextHop)){
 			/**
 			 * we have "other ==> this"
 			 */			
 			mSource = pOtherEntry.mSource;
 			mNextHop = pOtherEntry.mNextHop;
 			mNextHopL2Address = pOtherEntry.mNextHopL2Address;
 			
 		}else if(mNextHop.equals(pOtherEntry.mSource)){
 			/**
 			 * we have "this == other"
 			 */ 
 			mDestination = pOtherEntry.mDestination;
 		}
 		
 		// deactivate loopback flag
 		mLocalLoop = false;
 		
 		// deactivate neighbor flag
 		mRouteToDirectNeighbor = false;
 	}
 	
 	/**
 	 * Creates an identical duplicate
 	 * 
 	 * @return the identical duplicate
 	 */
 	public RoutingEntry clone()
 	{
 		// create object copy
 		RoutingEntry tResult = new RoutingEntry(mSource, mDestination, mNextHop, mHopCount, mUtilization, mMinDelay, mMaxDataRate, mCause);
 		
 		// update the flag "route to direct neighbor"
 		tResult.mRouteToDirectNeighbor = mRouteToDirectNeighbor;
 		
 		// update flag "route to local host"
 		tResult.mLocalLoop = mLocalLoop;
 		
 		// update next hop L2 address
 		tResult.mNextHopL2Address = mNextHopL2Address;
 		
 		// update timeout
 		tResult.mTimeout = mTimeout;
 		
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
 		//Logging.trace(this, "Comparing routing entry with: " + pObj);
 		if(pObj instanceof RoutingEntry){
 			RoutingEntry tOther = (RoutingEntry)pObj;
 			
 			if(((getDest() == null) || (tOther.getDest() == null) || (getDest().equals(tOther.getDest()))) &&
 			   ((getNextHop() == null) || (tOther.getNextHop() == null) || (getNextHop().equals(tOther.getNextHop()))) &&
 			   ((getSource() == null) || (tOther.getSource() == null) || (getSource().equals(tOther.getSource()))) //&&
 			   /*((getNextHopL2Address() == null) || (tOther.getNextHopL2Address() == null) || (getNextHopL2Address().equals(tOther.getNextHopL2Address())))*/){
 				//Logging.trace(this, "  ..true");
 				return true;
 			}
 		}
 		
 		//Logging.trace(this, "  ..false");
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
 		if(!mBelongstoHRG){
			return "(" + (getSource() != null ? "Source=" + getSource() + ", " : "") + "Dest.=" + getDest() + ", Next=" + getNextHop() + (getNextHopL2Address() != null ? ", NextL2=" + getNextHopL2Address() : "") + ", Hops=" + (getHopCount() > 0 ? getHopCount() : "none") + (HRMConfig.QoS.REPORT_QOS_ATTRIBUTES_AUTOMATICALLY ? ", Util=" + (getUtilization() > 0 ? getUtilization() : "none") + ", MinDel=" + (getMinDelay() > 0 ? getMinDelay() : "none") + ", MaxDR=" + (getMaxDataRate() != INFINITE_DATARATE ? getMaxDataRate() : "inf.") : "") + ")";
 		}else{
			return getSource() + " <==> " + getNextHop() + ", Dest.=" + getDest() + (mTimeout > 0 ? ", TO: " + mTimeout : "") + (getNextHopL2Address() != null ? ", NextL2=" + getNextHopL2Address() : "") + ", Hops=" + (getHopCount() > 0 ? getHopCount() : "none") + (HRMConfig.QoS.REPORT_QOS_ATTRIBUTES_AUTOMATICALLY ? ", Util=" + (getUtilization() > 0 ? getUtilization() : "none") + ", MinDel=" + (getMinDelay() > 0 ? getMinDelay() : "none") + ", MaxDR=" + (getMaxDataRate() != INFINITE_DATARATE ? getMaxDataRate() : "inf.") : "");
 		}
 	}
 }
