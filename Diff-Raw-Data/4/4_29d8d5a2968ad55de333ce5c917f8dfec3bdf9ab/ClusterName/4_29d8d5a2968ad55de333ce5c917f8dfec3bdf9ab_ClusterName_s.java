 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
 import java.io.Serializable;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 
 public class ClusterName implements Serializable, ICluster, AbstractRoutingGraphNode
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3027076881853652810L;
 	private int mToken;
 	private Long mClusterID;
 	private HierarchyLevel mHierarchyLevel = null;
 	
 	/**
 	 * 
 	 * @param pToken token of the cluster
 	 * @param pClusterID ID of the cluster
 	 * @param pLevel level of the cluster
 	 */
 	public ClusterName(int pToken, Long pClusterID, HierarchyLevel pLevel)
 	{
 		mClusterID = pClusterID;
 		mToken = pToken;
 		mHierarchyLevel = pLevel;
 	}
 	
 	@Override
 	public String toString()
 	{
		return "\"(ID=" + mClusterID + ", Tok=" + mToken + (mHierarchyLevel != null ? ", HierLvl.=" + mHierarchyLevel.getValue() : "") + ")\""; 
 	}
 
 	@Override
 	public void setPriority(BullyPriority pPriority) {
 		
 	}
 
 	@Override
 	public Long getClusterID() {
 		return mClusterID;
 	}
 
 	@Override
 	public HierarchyLevel getHierarchyLevel() {
 		return mHierarchyLevel;
 	}
 
 	@Override
 	public Name getCoordinatorName() {
 		return null;
 	}
 
 	@Override
 	public BullyPriority getPriority() {
 		return null;
 	}
 
 	@Override
 	public String getClusterDescription() {
 		return null;
 	}
 
 	@Override
 	public void setCoordinatorName(Name pCoordName) {
 		
 	}
 
 	@Override
 	public void setToken(int pToken) {
 		
 	}
 
 	@Override
 	public int getToken() {
 		return mToken;
 	}
 
 	@Override
 	public BullyPriority getHighestPriority()
 	{
 		return null;
 	}
 
 	@Override
 	public void setHighestPriority(BullyPriority pHighestPriority) {
 		
 	}
 
 	@Override
 	public boolean equals(Object pObj)
 	{
 		boolean tResult = false;
 		
 		if(pObj instanceof ICluster) {
 			ICluster tCluster = (ICluster) pObj;
 			if(tCluster.getClusterID().equals(getClusterID()) && tCluster.getToken() == getToken() && tCluster.getHierarchyLevel() == getHierarchyLevel()) {
 				tResult = true;
 			} 
 		}
 		
 		return tResult;
 	}
 
 	@Override
 	public ComChannelMuxer getMultiplexer()
 	{
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.facade.Name#getNamespace()
 	 */
 	@Override
 	public Namespace getNamespace()
 	{
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.facade.Name#getSerialisedSize()
 	 */
 	@Override
 	public int getSerialisedSize()
 	{
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.routing.hierarchical.management.AbstractRoutingGraphNode#getHRMID()
 	 */
 	@Override
 	public HRMID getHRMID()
 	{
 		return null;
 	}
 }
