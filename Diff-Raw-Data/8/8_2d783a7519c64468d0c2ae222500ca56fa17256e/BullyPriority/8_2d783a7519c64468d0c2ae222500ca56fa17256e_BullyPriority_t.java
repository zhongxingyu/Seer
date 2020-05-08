 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.clustering;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used for a Cluster in order to encapsulate the Bully priority computation and all needed adaption in case of topology changes.
  */
 public class BullyPriority
 {
 	/**
 	 * The value defines the prefix for the node specific configuration parameters for Bully algorithm.
 	 */
 	private String NODE_PARAMETER_PREFIX = "BULLY_PRIORITY_LEVEL_";
 	
 	/**
 	 * This value is used when the connectivity changes.
 	 */
 	private int OFFSET_FOR_CONNECTIVITY = 100;
 	
 	/**
 	 * Constructor
 	 * Initializes the Bully priority for the Cluster depending on the node configuration and the hierarchy level.
 	 * 
 	 * @param pNode the cluster to which this Bully priority belongs to.
 	 */
 	public BullyPriority(Node pNode, int pHierarchyLevel)
 	{
 		if (pNode == null) {
 			Logging.log(this,  "Invalid reference to the physical node found");
 			return;
 		}
 		mPriority = (float) pNode.getParameter().get(NODE_PARAMETER_PREFIX + pHierarchyLevel, HRMConfig.Election.DEFAULT_BULLY_PRIORITY);
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pCluster the cluster to which this Bully priority belongs to.
 	 * @param pPriority the defined Bully priority
 	 */
 	public BullyPriority(float pPriority)
 	{
 		mPriority = pPriority;
 	}
 
 	/**
 	 * Returns the Bully priority value.
 	 * 
 	 * @return Bully priority
 	 */
 	public float getPriority()
 	{
 		return mPriority;
 	}
 	
 	/**
	 * The function is called if a neighbor cluster/node is found. It increase the Bully value by a fixed value. 
 	 */
 	public void increaseConnectivity()
 	{
 		mPriority += OFFSET_FOR_CONNECTIVITY;
 	}
 	
 	private float mPriority = HRMConfig.Election.DEFAULT_BULLY_PRIORITY;
 
 }
