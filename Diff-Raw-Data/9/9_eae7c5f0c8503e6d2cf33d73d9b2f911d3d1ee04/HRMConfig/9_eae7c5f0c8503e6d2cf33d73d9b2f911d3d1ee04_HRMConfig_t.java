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
 
 public class HRMConfig
 {
 	public class Hierarchy
 	{
 		/**
 		 * amount of hierarchical levels in the simulation
 		 */
 		public static final int HEIGHT = 3; // TV
 
 		/**
 		 * this limits the maximum amount of nodes inside one cluster and defined the space which is used for selecting a hierarchy level
 		 */
 		public static final int USED_BITS_PER_LEVEL = 8; //TV
 
 		/**
		 * specifies whether the hierarchy is created automatically,
		 * otherwise it is done step by step
 		 */
 		public static final boolean BUILD_AUTOMATICALLY = false; //TV
 		
 		/**
 		 * Identifies the base level of the hierarchy.
 		 */
 		public static final int BASE_LEVEL = 0;		
 	}
 	
 	/**
 	 * Configuration parameters for the routing process and routing service
 	 */
 	public class Routing
 	{
 
 		/**
 		 * Maximum radius that is allowed during expansion phase 
 		 */
 		public static final int EXPANSION_RADIUS = 4; //TV
 		
 		
 		/**
 		 * Disables/enables the region limitation feature which can be used to implement an obstructive/restrictive routing based on the HRM infrastructure.
 		 */
 		public static final boolean ENABLE_REGION_LIMITATION = false; //TV
 	}
 
 	public class Debugging
 	{
 		/**
 		 * (De-)activates the usage of HRMIDs when using toString() for clusters.
 		 */
 		public static final boolean PRINT_HRMIDS_AS_CLUSTER_IDS = false; //TV
 	}
 	
 	/**
 	 * Configuration for the election process
 	 */
 	public class Election //TV
 	{
 		/**
 		 * Default priority for election process. This value is used when no value is explicitly defined for a node.
 		 */
 		public static final long DEFAULT_BULLY_PRIORITY = 1; //TV
 	}
 }
