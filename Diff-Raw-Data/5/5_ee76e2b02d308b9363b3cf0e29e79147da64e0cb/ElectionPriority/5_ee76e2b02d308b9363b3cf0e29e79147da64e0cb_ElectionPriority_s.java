 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.election;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.Localization;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ControlEntity;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used for a cluster in order to encapsulate the Election priority computation and all needed adaption in case of topology changes.
  * 
  * In general, a node has a Election priority for each hierarchy level. In the beginning, each of those values is 1. However, the administrator of a local
  * broadcast domain is allowed to use a different value than 1 as initialization value. The highest allowed initialization value is 99.
  * 
  * The Election priority is increased:
  *      + by 100 for each detected physical neighbor node
  *      + by 100*1000 for each known (remote) base hierarchy level coordinator, multiplied by the hop distance
  * 
  * 
  * HINT: We avoid dependencies from L1+ structures and neighborhood relations because this might cause oscillation effects.
  * 
  */
 public class ElectionPriority
 {
 	/**
 	 * Allow for a better debugging based on messages each time such an object is created.
 	 */
 	private static boolean DEBUG_CREATION = false;
 	
 	/**
 	 * This value represents an undefined priority.
 	 */
 	private static long UNDEFINED_PRIORITY = -1;
 	
 	/**
 	 * This value represents the connectivity of a node. ==> instantiate coordinators at link crossings
 	 */
 	public static int OFFSET_FOR_CONNECTIVITY = 100;
 	
 	/**
 	 * This value represents the closeness to level 0 coordinators. It is used when a remote coordinator announcement is received ==> instantiate coordinators far away from one-way network parts, instantiate them close to the network core
 	 */
	public static long OFFSET_FOR_KNOWN_BASE_REMOTE_L0_COORDINATOR = 10000;
 
 	/**
 	 * This value represents the closeness to level 1+ coordinators. It is used when a remote coordinator announcement is received ==> instantiate coordinators far away from one-way network parts, instantiate them close to the network core
 	 */
	public static long OFFSET_FOR_KNOWN_BASE_REMOTE_L1p_COORDINATOR = 10;
 	
 	/**
 	 * Stores the priority value
 	 */
 	private long mPriority = HRMConfig.Election.DEFAULT_PRIORITY;
 
 	/**
 	 * Defines the size of this object if it is serialized
 	 * 
 	 * measured in [bytes]
 	 */
 	private final static int SERIALIZED_SIZE = 4; // 4 bytes for the actual priority value  
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pPriority the new priority value
 	 */
 	private ElectionPriority(long pPriority)
 	{
 		mPriority = pPriority;
 	}
 
 	/**
 	 * Factory function: initializes the Election priority for a cluster depending on the node configuration and the hierarchy level.
 	 * 
 	 * @param pCluster the cluster to which this Election priority belongs to.
 	 */
 	public static ElectionPriority createForControlEntity(HRMController pHRMController, ControlEntity pControlEntity)
 	{
 		if (pHRMController == null) {
 			Logging.log(pControlEntity, "Cannot create Election priority, invalid reference to HRMController found");
 			return null;
 		}
 
 		ElectionPriority tResult = new ElectionPriority(pHRMController.getNodePriority(pControlEntity.getHierarchyLevel()));
 
 		if (DEBUG_CREATION){
 			Logging.log(pControlEntity, "Created Election priority object (initial priority is " + tResult.getValue() + ")");
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Factory function: initializes the Election priority with "undefined"
 	 */
 	public static ElectionPriority create(Object pParent)
 	{
 		if(pParent != null){
 			if ((pParent instanceof Integer) || (pParent instanceof Long)){
 				Logging.warn(pParent, "This object is an Integer/Long class, this often means a wrong call to ElectionPriority::create()");
 			}
 		}
 
 		ElectionPriority tResult = new ElectionPriority(UNDEFINED_PRIORITY);
 
 		/**
 		 * HINT: Be aware of recursion here. The Election priority is very often used inside toString(). For example, this would lead 
 		 * to recursive calls caused by getElectionPriority in the Cluster/Coordinator class. 
 		 */
 		
 		if (DEBUG_CREATION){
 			Logging.log(pParent, "Created ElectionPriority object (undefined priority) for class \"" + (pParent != null ? pParent.getClass().getSimpleName() : "null") + "\"");
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Factory function: initializes the Election priority with "undefined"
 	 */
 	public static ElectionPriority create(Object pParent, long pPredefinedPriority)
 	{
 		if ((pParent instanceof Integer) || (pParent instanceof Long)){
 			Logging.warn(pParent, "This object is an Integer/Long class, this often means a wrong call to ElectionPriority::create()");
 		}
 
 		ElectionPriority tResult = new ElectionPriority(pPredefinedPriority);
 
 		//Logging.log(pParent, "Created ElectionPriority object (predefined priority " + pPredefinedPriority + ")");
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the Election priority value.
 	 * 
 	 * @return Election priority
 	 */
 	public long getValue()
 	{
 		return mPriority;
 	}
 
 	/**
 	 * Check if the priority is still undefined.
 	 * 
 	 * @return true if this Election priority is undefined.
 	 */
 	public boolean isUndefined()
 	{
 		return (mPriority == UNDEFINED_PRIORITY);
 	}
 	
 	/**
 	 * Clones this object.
 	 * 
 	 *  @return the object clone
 	 */
 	public ElectionPriority clone()
 	{
 		ElectionPriority tClone = new ElectionPriority(mPriority);
 		
 		return tClone;
 	}
 
 	/**
 	 * Check if the Election priority is higher than the other given one.
 	 * 
 	 * @param pCheckLocation a reference to the origin object from where the call comes from
 	 * @param pOtherPriority the other given priority
 	 * @return return "true" if the Election priority is higher than the other one, otherwise return "false"
 	 */
 	public boolean isHigher(Object pCheckLocation, ElectionPriority pOtherPriority)
 	{
 		String pLocationDescription = pCheckLocation.getClass().getSimpleName();
 		if (pCheckLocation instanceof Localization){
 			Localization tHRMEntity = (Localization)pCheckLocation;
 			pLocationDescription = tHRMEntity.toLocation();
 		}
 
 		if (pOtherPriority == null){
 			Logging.log(pLocationDescription + ": COMPARING BULLY priority " + mPriority + " with NULL POINTER, returning always \"true\" for isHigher()");
 			return true;
 		}
 		
 		//Logging.log(pLocationDescription + ": COMPARING BULLY priority " + mPriority + " with alternative " + pOtherPriority.getValue());
 		
 		// if the priority values are equal, we return "true"
 		if (mPriority > pOtherPriority.getValue()){
 			return true;
 		}
 		
 		// otherwise always "false"
 		return false;
 	}
 
 	/**
 	 * Compares two priority objects
 	 * 
 	 * @return true if both represent the same priority value
 	 */
 	@Override
 	public boolean equals(Object pObj)
 	{
 		// do we have another Election priority object?
 		if(pObj instanceof ElectionPriority) {
 			
 			// cast to ElectionPriority object
 			ElectionPriority tOtherPrio = (ElectionPriority) pObj;
 			
 			// if the priority values are equal, we return "true"
 			if (tOtherPrio.getValue() == mPriority){
 				return true;
 			}
 		}
 		
 		// otherwise always "false"
 		return false;
 	}
 
 	/**
 	 * Returns the size of a serialized representation.
 	 * 
 	 * @return the size of a serialized representation
 	 */
 	public int getSerialisedSize()
 	{
 		return SERIALIZED_SIZE;
 	}
 
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 *  @return the descriptive string
 	 */
 	@Override
 	public String toString()
 	{
 		return "ElectionPriority(Prio=" + Long.toString(getValue()) + ")";
 	}
 }
