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
 
 import java.awt.Color;
 import java.math.BigInteger;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMRoutingService;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 public class L2Address extends HRMName
 {
 	private static final long serialVersionUID = 4484202410314555829L;
 
 	/**
 	 * This is the L2Address counter, which allows for globally (related to a physical simulation machine) unique clusL2Addresses.
 	 * We start with "1" because the address "0" is reserved.
 	 */
 	private static long sNextFreeAddress = 1;
 
 	/**
 	 * Stores the physical simulation machine specific multiplier, which is used to create unique L2Addresses, even if multiple physical simulation machines are connected by FoGSiEm instances
 	 * The value "-1" is important for initialization!
 	 */
 	private static long sIDMachineMultiplier = -1;
 
 	/**
 	 * Is this an address for a server?
 	 * This function is not part of the concept. It is only useful for debugging purposes.
 	 */
 	private boolean mIsServer = false;
 
 	/**
 	 * Is this an address for a client?
 	 * This function is not part of the concept. It is only useful for debugging purposes.
 	 */
 	private boolean mIsClient = false;
 	
 	/**
 	 * Is this an address for a central node?
 	 * This function is not part of the concept. It is only useful for debugging purposes.
 	 */
 	private boolean mIsCentralNode = false;
 
 	/**
 	 * Is this an address for the central node of the host?
 	 * This function is not part of the concept. It is only useful for debugging purposes.
 	 */
 	private boolean mIsThisHostCentralNode = false;
 
 	/**
 	 * The description about the creator
 	 * This function is not part of the concept. It is only useful for debugging purposes.
 	 */
 	private String mCreatorDescription = "";
 	
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
 	 * Determines the physical simulation machine specific L2Address multiplier
 	 * 
 	 * @return the generated multiplier
 	 */
 	private static long idMachineMultiplier()
 	{
 		if (sIDMachineMultiplier < 0){
 			String tHostName = HRMController.getHostName();
 			if (tHostName != null){
 				sIDMachineMultiplier = Math.abs((tHostName.hashCode() % 10000) * 10000);
 			}else{
 				Logging.err(null, "Unable to determine the machine-specific L2Address multiplier because host name couldn't be indentified");
 			}
 		}
 
 		return sIDMachineMultiplier;
 	}
 
 	/**
 	 * Generates a new unique L2Address
 	 * 
 	 * @return the new L2Address
 	 */
 	public static L2Address createL2Address(HRMRoutingService pHRS)
 	{
 		// get the current address counter
 		long tAddr = sNextFreeAddress * idMachineMultiplier();
 
 		// make sure the next address isn't equal
 		sNextFreeAddress++;
 		
 		// generate new object with the correct number
 		L2Address tResult = new L2Address(tAddr);
 
 		tResult.setCreatorDescription(pHRS.toString());
 		
 		return tResult;
 	}
 
 	/**
 	 * Sets a new description about the creator of this address
 	 * 
 	 * @param pDescription the description 
 	 */
 	private void setCreatorDescription(String pDescription)
 	{
 		mCreatorDescription = pDescription;
 	}
 
 	/**
 	 * Returns an object clone
 	 * 
 	 * @return the object clone
 	 */
 	public L2Address clone()
 	{
 		L2Address tResult = new L2Address(mAddress.longValue());
 		
 		tResult.mOptionalDescr = mOptionalDescr;
 		
 		tResult.mIsCentralNode = (mIsCentralNode | mIsThisHostCentralNode);
 		
 		tResult.mIsClient = mIsClient;
 		
 		tResult.mIsServer = mIsServer;
 		
 		tResult.mIsThisHostCentralNode = false;
 		
 		return tResult;
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
			L2Address tOtherL2Addr = (L2Address)pObj;
			
			return (tOtherL2Addr.mAddress.longValue() == mAddress.longValue());
 		} 
 		
 		return false;
 	}
 	
 	/**
 	 * Create a descriptive string about this object
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + (mAddress.longValue() / idMachineMultiplier()) + "(\"" + mOptionalDescr + "\")";
 	}
 
 	/**
 	 * Returns true or false, depending on the comparison
 	 * 
 	 * @param pOtherL2Address the other L2 address
 	 * @return true or false
 	 */
 	public boolean isHigher(L2Address pOtherL2Address)
 	{
 		boolean tResult = false;
 
 		//Logging.log(this, mAddress.longValue() + " >>?>> " + pOtherL2Address.mAddress.longValue());
 		if(mAddress.longValue() > pOtherL2Address.mAddress.longValue()) {
 			tResult = true;
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns true or false, depending on the comparison
 	 * 
 	 * @param pOtherL2Address the other L2 address
 	 * @return true or false
 	 */
 	public boolean isLower(L2Address pOtherL2Address)
 	{
 		boolean tResult = false;
 
 		if(mAddress.longValue() < pOtherL2Address.mAddress.longValue()) {
 			tResult = true;
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Defines the decoration color for the ARG viewer
 	 * 
 	 * @return color for the HRMID
 	 */
 	@Override
 	public Color getColor()
 	{
 		if(mIsClient){
 			return Color.YELLOW;			
 		}
 		if(mIsServer){
 			return Color.RED;			
 		}
 		if(mIsCentralNode){
 			return Color.CYAN;			
 		}
 		if(mIsThisHostCentralNode){
 			return Color.GREEN;
 		}
 		return Color.ORANGE;
 	}
 
 	/**
 	 * Returns the description about the creator
 	 * 
 	 * @return the description
 	 */
 	public String getCreatorDescription()
 	{
 		return mCreatorDescription;
 	}
 	
 	/**
 	 * Mark as address for a server 
 	 */
 	public void setServer()
 	{
 		mIsServer = true;
 	}
 
 	/**
 	 * Mark as address for a client 
 	 */
 	public void setClient()
 	{
 		mIsClient = true;
 	}
 
 	/**
 	 * Mark as address for a central node 
 	 */
 	public void setCentralNode()
 	{
 		mIsCentralNode = true;
 	}
 
 	/**
 	 * Mark as address for the central node for the host 
 	 */
 	public void setHostCentralNode()
 	{
 		mIsThisHostCentralNode = true;
 	}
 }
