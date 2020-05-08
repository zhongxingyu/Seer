 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.naming.hierarchical;
 
 import java.math.BigInteger;
 import java.util.Random;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 
 public class L2Address extends HRMName
 {
 	private static final long serialVersionUID = 4484202410314555829L;
 
 	/**
	 * Stores the random number generator instance. One instance per physical simulation machine is used because a singleton is needed. 
	 * Otherwise, parallel number generators might be initialized with the same seed and generate address duplicates. This simplifies 
	 * the L2address generation and maintains unique addressing scheme. For real use, a UUID has to be used as L2address which promises 
	 * a low risk of collisions.
 	 */
 	private static Random mRandomGenerator = null;
 
 	/**
 	 * Create an address that is used to identify a node at the MAC layer.
 	 * 
 	 * @param pAddress This can be a simple long value.
 	 */
 	private L2Address(long pAddress)
 	{
 		super(BigInteger.valueOf(pAddress));
 	}
 
 	/**
 	 * Create a new L2 address based on a random number from the random number generator  
 	 * 
 	 * @return the new L2 address
 	 */
 	public static L2Address create()
 	{
 		long tNumber = getRandomGenerator().nextLong();
 		
 		// generate new object with the correct number
 		L2Address tResult = new L2Address(tNumber);
 		
 		return tResult;
 	}
 
 	/**
 	 * Clones the object but uses the same internal address value
 	 * 
 	 * @return the object clone
 	 */
 	public L2Address clone()
 	{
 		return new L2Address(mAddress.longValue());
 	}
 	
 	/**
 	 * Returns a reference to the singleton with the random number generator
 	 * 
 	 * @return the random number generator
 	 */
 	private static Random getRandomGenerator()
 	{
 		// create singleton for random number generator
 		if (mRandomGenerator == null)
 			mRandomGenerator = new Random(System.currentTimeMillis());
 		
 		return mRandomGenerator;
 	}
 	
 	public boolean equals(Object pObj)
 	{
 		if(pObj == null){
 			return false;
 		}
 		
 		if(pObj == this){
 			return true;
 		}
 		
 		/**
 		 * L2Address
 		 */
 		if(pObj instanceof L2Address) {
 			return (((L2Address) pObj).mAddress.equals(mAddress));
 		} 
 		
 		/**
 		 * RoutingServiceLinkVector
 		 */
 		if (pObj instanceof RoutingServiceLinkVector) {
 			return ( ((RoutingServiceLinkVector)pObj).getSource() != null && ((RoutingServiceLinkVector)pObj).getSource().equals(this)) || (((RoutingServiceLinkVector)pObj).getDestination() != null && ((RoutingServiceLinkVector)pObj).getDestination().equals(this) ) ;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Create a descriptive string about this object
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + "(name=\"" + mOptionalDescr + "\", addr.=" + mAddress + ")";
 	}
 }
