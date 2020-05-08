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
 
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.simulated.RoutingServiceAddress;
 
 /**
  * This is the base class for the addresses that are used within the HRM system.
  * It is inherited by L2Address and HRMID objects.
  */
 public class HRMName implements Name
 {
 	protected BigInteger mAddress;
 	
 	/**
 	 * Optional parameter just for making a human readable name.
 	 * This is mainly used for the GUI.
 	 */
 	protected String mOptionalDescr;
 	
	/**
	 * This description includes the requirements given by the FoG system. 
	 */
	private Description mDescription;
	
 	private static final long serialVersionUID = 6612145890128148511L;
 	private static final Namespace NAMESPACE_HRM = new Namespace("HRM");
 	
 	/**
 	 * 
 	 * @param pAddress Provide the address 
 	 */
 	public HRMName(BigInteger pAddress)
 	{
 		mAddress = pAddress;
 	}
 	
 	/**
 	 * 
 	 * @return The address will be returned by this method.
 	 */
 	public BigInteger getComplexAddress()
 	{
 		return mAddress;
 	}
 	
 	@Override
 	public Namespace getNamespace()
 	{
 		return NAMESPACE_HRM;
 	}
 
 	@Override
 	public int getSerialisedSize()
 	{
 		return mAddress.bitLength();
 	}
 	
 	/**
 	 * Set a description of the HRMName via this method.
 	 * 
 	 * @param pDescr
 	 */
 	public void setDescr(String pDescr)
 	{
 		mOptionalDescr = pDescr;
 	}
 	
 	/**
 	 * 
 	 * @return The object used for description is returned.
 	 */
 	public String getDescr()
 	{
 		return mOptionalDescr;
 	}
 	
 	public boolean equals(Object pObj)
 	{
 		if(pObj == null){
 			return false;
 		}
 		
 		if(pObj == this){
 			return true;
 		}
 		
 		if(pObj instanceof RoutingServiceAddress) {
 			return ((RoutingServiceAddress) pObj).getAddress() == mAddress.longValue();
 		} else if(pObj instanceof HRMName) {
 			return (((HRMName) pObj).mAddress.equals(mAddress));
 		} if (pObj instanceof RoutingServiceLinkVector) {
 			return ( ((RoutingServiceLinkVector)pObj).getSource() != null && ((RoutingServiceLinkVector)pObj).getSource().equals(this)) || (((RoutingServiceLinkVector)pObj).getDestination() != null && ((RoutingServiceLinkVector)pObj).getDestination().equals(this) ) ;
 		}
 		
 		return false;
 	}
 }
