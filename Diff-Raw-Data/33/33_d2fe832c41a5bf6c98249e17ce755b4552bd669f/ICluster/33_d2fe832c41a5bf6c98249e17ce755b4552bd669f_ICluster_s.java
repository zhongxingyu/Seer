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
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 
 public interface ICluster extends Serializable
 {
 	/**
 	 * 
 	 * @return The priority of the node that is associated to this cluster is return here
 	 */
 	public BullyPriority getPriority();
 
 	/**
 	 * 
 	 * @return The ID of the cluster is returned here.
 	 */
 	public Long getClusterID();
 	
 	/**
 	 * 
 	 * @return The hierarchical level the cluster acts on is returned here.
 	 */
 	public HierarchyLevel getHierarchyLevel();
 	
 	/**
 	 * 
 	 * @return The name of the coordinator that is responsible for this cluster is return here.
 	 */
	public Name getCoordinatorHostName();
 	
 	/**
 	 * 
 	 * @param pCoordinatorHostName This is the name of the coordinator that is responsible for this cluster
 	 */
 	public void setCoordinatorHostName(Name pCoordinatorHostName);
 	
 	/**
 	 * 
 	 * @return The token that is used for additional identification of the cluster is provided here.
 	 */
 	public int getCoordinatorID();
 	
 	/**
 	 * 
 	 * @param pObj Provide the object for comparison
 	 * @return Return true in case the objects are equal to each other.
 	 */
 	public boolean equals(Object pObj);
 }
