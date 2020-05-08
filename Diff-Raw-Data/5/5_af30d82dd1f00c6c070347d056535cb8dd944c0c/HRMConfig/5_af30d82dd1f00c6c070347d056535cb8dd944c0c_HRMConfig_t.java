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
 	public static class DebugOutput
 	{
 		/**
 		 * Limits the size of the packet storage in a comm. channel
 		 */
 		public static final int COM_CHANNELS_MAX_PACKET_STORAGE_SIZE = 64;
 
 		/** 
 		 * Show debug outputs about observable/observer construct, which is used to inform GUIs about HRMController internal changes.
 		 */
 		public static final boolean GUI_SHOW_NOTIFICATIONS = false; 
 
 		/** 
 		 * Show debug outputs about HRMID updates for nodes.
 		 */
 		public static final boolean GUI_HRMID_UPDATES = false;
 
 		/**
 		 * Show relative addresses in the GUI? (e.g., "0.0.1")
 		 */
 		public static final boolean GUI_SHOW_RELATIVE_ADDRESSES = false;
 
 		/**
 		 * Show cluster addresses in the GUI? (e.g., "3.7.0")
 		 */
 		public static final boolean GUI_SHOW_CLUSTER_ADDRESSES = true;
 
 		/**
 		 * Show debug outputs about the timing of report/share phases of each existing coordinator.
 		 */
 		public static final boolean GUI_SHOW_TIMING_ROUTE_DISTRIBUTION = false;
 
 		/**
 		 * Shows debug outputs for each received packet of a session.
 		 */
 		public static final boolean SHOW_RECEIVED_SESSION_PACKETS = false;
 
 		/**
 		 * Shows debug outputs for each sent packet of a session.
 		 */
 		public static final boolean SHOW_SENT_SESSION_PACKETS = false;
 
 		/**
 		 * Shows debug outputs for each received packet of a channel.
 		 */
 		public static final boolean SHOW_RECEIVED_CHANNEL_PACKETS = false;
 
 		/**
 		 * Shows debug outputs for each sent packet of a channel.
 		 */
 		public static final boolean SHOW_SENT_CHANNEL_PACKETS = false;
 
 		/**
 		 * Shows debug outputs for each clustering process.
 		 */
 		public static final boolean SHOW_CLUSTERING_STEPS = false;
 
 		/**
 		 * Shows debug outputs for the report phase
 		 */
 		public static final boolean SHOW_REPORT_PHASE = false;
 
 		/**
 		 * Shows debug outputs for the report phase of comm. channels
 		 */
 		public static final boolean SHOW_REPORT_PHASE_COM_CHANNELS = false;
 		
 		/**
 		 * Shows debug outputs for the share phase
 		 */
 		public static final boolean SHOW_SHARE_PHASE = false;
 
 		/**
 		 * Shows debug outputs for the share phase about redundant route entries
 		 */
 		public static final boolean SHOW_SHARE_PHASE_REDUNDANT_ROUTES = false;
 		
 		/**
 		 * Shows debug outputs about the routing process 
 		 */
 		public static final boolean GUI_SHOW_ROUTING = false;
 
 		/**
 		 * Show debug outputs about node/link detection
 		 */
 		public static final boolean GUI_SHOW_TOPOLOGY_DETECTION = false;
 
 		/**
 		 * Shows debug outputs about multiplex packets 
 		 */
 		public static final boolean GUI_SHOW_MULTIPLEX_PACKETS = false;
 
 		/**
 		 * Shows general debug outputs about signaling messages
 		 */
 		public static final boolean GUI_SHOW_SIGNALING = false;
 
 		/**
 		 * Shows detailed debug outputs about Election related signaling messages
 		 */
 		public static final boolean GUI_SHOW_SIGNALING_ELECTIONS = false;
 		
 		/**
 		 * Shows detailed debug outputs about distributed Election related signaling messages
 		 */
 		public static final boolean GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS = false;
 
 		/**
 		 * Shows detailed debug outputs about HRMViewer steps
 		 */
 		public static final boolean GUI_SHOW_VIEWER_STEPS = false;
 
 		/**
 		 * Shows coordinator as cluster members in the ARG viewer
 		 * HINT: clusters HAVE TO BE STORED in the ARG, otherwise routing isn't possible
 		 */
 		public static final boolean GUI_SHOW_COORDINATOR_CLUSTER_MEMBERS_IN_ARG = false;
 
 		/**
 		 * Defines the minimum time period between two updates of the node specific HRM viewer.
 		 * IMPORTANT: The value shouldn't be too low. Otherwise, the GUI updates might slow down the FoGSiEm environment.
 		 */
		public static final double GUI_HRM_VIEWERS_UPDATE_INTERVAL = 1.0;
 
 		/**
 		 * Defines if the hierarchy creation should start once the entire simulation was created. 
 		 */
 		public static final boolean BLOCK_HIERARCHY_UNTIL_END_OF_SIMULATION_CREATION = false;
 
 		/**
 		 * Defines if all HRM entities should be linked to a central node in the ARG
 		 */
 		public static final boolean SHOW_ALL_OBJECT_REFS_TO_CENTRAL_NODE_IN_ARG = false;
 
 		/**
 		 * Defines if data about "AnnounceCoordinator" packets should be shown
 		 */
 		public static final boolean SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS = false;
 
 		/**
 		 * Defines if the route of "AnnounceCoordinator" packets should be shown
 		 */
 		public static final boolean SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS_ROUTE = false;
 		
 		/**
 		 * Defines if the route of "InvalidCoordinator" packets should be shown
 		 */
 		public static final boolean SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS = false;
 		
 		/**
 		 * Defines if signaling messages for address distribution are shown
 		 */
 		public static final boolean SHOW_DEBUG_ADDRESS_DISTRIBUTION = false;
 
 		/**
 		 * Defines if the address aggregation is shown in the debug output
 		 */
 		public static final boolean GUI_SHOW_ADDRESS_AGGREGATION = false;
 
 		/**
 		 * Defines if HRG updates should be shown
 		 */
 		public static final boolean GUI_SHOW_HRG_UPDATES = false;
 
 		/**
 		 * Show debug outputs about HRG node/link detection
 		 */
 		public static final boolean GUI_SHOW_HRG_DETECTION = false;
 
 		/**
 		 * Defines the minimum time period between two updates of the node specific HRG viewer.
 		 * IMPORTANT: The value shouldn't be too low. Otherwise, the GUI updates might slow down the FoGSiEm environment.
 		 */
		public static final double GUI_HRG_VIEWERS_UPDATE_INTERVAL = 5.0;
 
 		/**
 		 * Defines if very verbose debugging should be active.
 		 * WARNING: This consumes more and more memory.
 		 */
 		public static final boolean MEMORY_CONSUMING_OPERATIONS = false;
 	}
 
 	public static class Measurement
 	{
 		/**
 		 * Defines if the AnnounceCoordinator packets should be automatically deactivated if the last packet with impact on the hierarchy data is too far in the past.
 		 * IMPORTANT: This function is not part of the concept. It is only useful for debugging purposes and measurement speedups.
 		 * 			  The value influences only the speed of the FoGSiEm environment.
 		 */
 		public static final boolean AUTO_DEACTIVATE_ANNOUNCE_COORDINATOR_PACKETS = true;
 
 		/**
 		 * Defines if the address distribution should be automatically started right after AnnounceCoordinator packets were deactivated.
 		 * This works only if "Addressing.ASSIGN_AUTOMATICALLY" is set to false.
 		 * IMPORTANT: This function is not part of the concept. It is only useful for debugging purposes and measurement speedups.
 		 * 			  The value influences only the speed of the FoGSiEm environment.
 		 */
 		public static final boolean AUTO_DEACTIVATE_ANNOUNCE_COORDINATOR_PACKETS_AUTO_START_ADDRESS_DISTRIBUTION = true;
 		
 		/**
 		 * Defines if the report/share phase should be automatically started right after AnnounceCoordinator packets were deactivated and addresses are distributed.
 		 * This works only if "Routing.REPORT_TOPOLOGY_AUTOMATICALLY" is set to false.
 		 * IMPORTANT: This function is not part of the concept. It is only useful for debugging purposes and measurement speedups.
 		 * 			  The value influences only the speed of the FoGSiEm environment.
 		 */
 		public static final boolean AUTO_DEACTIVATE_ANNOUNCE_COORDINATOR_PACKETS_AUTO_START_ADDRESS_DISTRIBUTION_AUTO_START_REPORTING_SHARING = true;
 		
 		/**
 		 * Defines if additional validation checks should be applied.
 		 */
 		public static final boolean VALIDATE_RESULTS = true;
 		
 		/**
 		 * Defines if additionally verbose validation checks should be applied.
 		 */
 		public static final boolean VALIDATE_RESULTS_EXTENSIVE = false;
 	}
 	
 	public static class Addressing
 	{
 		/**
 		 * Specifies whether the address are assigned automatically,
 		 * otherwise it has to be triggered step by step via the GUI.
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean ASSIGN_AUTOMATICALLY = false;
 		
 		/**
 		 * Defines the address which is used for cluster broadcasts
 		 */
 		public static final long BROADCAST_ADDRESS = 0;
 
 		/**
 		 * Defines if relative addresses should be distributed if a coordinators doesn't have superior coordinators
 		 */
 		public static final boolean DISTRIBUTE_RELATIVE_ADDRESSES = false;
 
 		/**
 		 * Defines if already assigned addresses should be reused during address distribution
 		 */
 		public static final boolean REUSE_ADDRESSES = true;
 	}
 	
 	public static class Hierarchy
 	{
 		/**
 		 * Defines if coordinators should announce their existences among cluster members/neighbors
 		 * IMPORTANT: If this is disabled, the hierarchy creation won't be correct.
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean COORDINATOR_ANNOUNCEMENTS = true;
 
 		/**
 		 * Defines the base time period for CoordinatorAnnounce distributions. Depending on this value, the following announce periods will be calculated as follows:
 		 * 		announce 				time period
 		 * 			1						x * 1
 		 * 			2						x * 2
 		 *          :						  :	
 		 * 			t						x * t
 		 * 	  	(t + 1)						x * f 
 		 * 	  	(t + 2)						x * f 
 		 *  		:						  :
 		 *  
 		 *  x = COORDINATOR_ANNOUNCEMENTS_INTERVAL
 		 *  t = COORDINATOR_ANNOUNCEMENTS_INITIAL_THRESHOLD
 		 *  f = COORDINATOR_ANNOUNCEMENTS_INTERVAL_LONG_TERM_FACTOR
 		 *  
 		 */
 		public static final double COORDINATOR_ANNOUNCEMENTS_INTERVAL = 2.0;
 		
 		/**
 		 * Defines how many initial announces should be sent.
 		 */
 		public static final long COORDINATOR_ANNOUNCEMENTS_INITIAL_THRESHOLD = 10;
 
 		/**
 		 * Defines the factor for long-term announces.
 		 */
 		public static final double COORDINATOR_ANNOUNCEMENTS_INTERVAL_LONG_TERM_FACTOR = 1;
 
 		/**
 		 * Defines the default timeout for a coordinator proxy. If the proxy doesn't get any refresh in the defined time period, the proxy gets deleted.
 		 */
 		public static final double COORDINATOR_TIMEOUT = COORDINATOR_ANNOUNCEMENTS_INTERVAL * 2; 
 
 		/**
 		 * Defines if coordinators should periodically announce their existences among cluster members/neighbors
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean PERIODIC_COORDINATOR_ANNOUNCEMENTS = true;
 
 		/**
 		 * This defines the amount of hierarchical levels in the simulation.
 		 * A maximum value of 5 is allowed.
 		 */
 		public static final int HEIGHT = 3;
 
 		/**
 		 * this limits the maximum amount of nodes inside one cluster and defined the space which is used for selecting a hierarchy level
 		 */
 		public static final int BITS_PER_HIERARCHY_LEVEL = 8;
 
 		/**
 		 * Maximum radius that is allowed during cluster expansion phase.
 		 * HINT: As a result of a value of (n), the distance between two coordinators on a hierarchy level will be less than (n + 1) hops.  
 		 */
 		public static final int EXPANSION_RADIUS = 1;
 
 		/**
 		 * The same like START_AUTOMATICALLY but restricted to base hierarchy level
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean START_AUTOMATICALLY_BASE_LEVEL = true; 
 
 		/**
 		 * This specifies whether the hierarchy build process is continued automatically.
 		 * Otherwise, it is done step by step by the help of GUI and user inputs.
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean CONTINUE_AUTOMATICALLY = true;
 		
 		/**
 		 * This specifies whether the hierarchy build process should stop at a defined hierarchy level or not.
 		 * A value of "HEIGHT" deactivates the limitation.
 		 */
 		public static final int CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT = 99;
 
 		/**
 		 * Defines if connection should remain open or be automatically closed if the last inferior comm. channel was closed
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean AUTO_CLEANUP_FOR_CONNECTIONS = false; //TODO: fuehrt zu race conditions wenn letzter Kanal geht und neuer sofort wieder hinzu kommt
 
 		/**
 		 * Defines if elections at higher hierarchy levels should be based on a separate hierarchy priority per node.
 		 * This values is computed based on the received L0 coordinator announcements. It expresses the L0 clustering
 		 * neighborhood. The more neighbor L0 regions exist within the given max. radius (EXPANSION_RADIUS), the higher
 		 * is this value.
 		 */
 		public static final boolean USE_SEPARATE_HIERARCHY_NODE_PRIORITY = true;
 
 		/**
 		 * Defines if a separate priority per hierarchy level should be used
 		 */
 		public static final boolean USE_SEPARATE_HIERARCHY_NODE_PRIORITY_PER_LEVEL = true;
 
 		/**
 		 * Defines the limit for the hierarchy height
 		 */
 		public static final int HEIGHT_LIMIT = 99;
 
 		/**
 		 * Defines the maximum for the hop count to a remote coordinator (distance between a local CoordinatorProxy and a distant Coordinator instance) 
 		 */
 		public static final int MAX_HOPS_TO_A_REMOTE_COORDINATOR = 256;
 
 		/**
 		 * Defines the timeout for a connect() try.
 		 */
 		public static final double CONNECT_TIMEOUT = 3.0;
 	}
 	
 	/**
 	 * Configuration parameters for the routing process and routing service
 	 */
 	public static class Routing
 	{
 		/**
 		 * Defines if an HRM entity should report its topology knowledge to the superior entity.
 		 * IMPORTANT: If this is disabled, the hierarchy won't learn any aggregated network topology.
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean REPORT_TOPOLOGY_AUTOMATICALLY = false;
 
 		/**
 		 * Defines if an HRM entity should share its routing knowledge to all inferior entities.
 		 * IMPORTANT: If this is disabled, the hierarchy won't learn any aggregated routes to foreign destinations.
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean SHARE_ROUTES_AUTOMATICALLY = true;
 
 		/**
 		 * Should the packets of the "share phase" be send periodically?
 		 * If a distributed simulation (span a network over multiple physical nodes) is used, this value has to be set to "true". 
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean PERIODIC_SHARE_PHASES = true;
 
 		/**
 		 * Should each HRS instance try to avoid duplicates in its internal routing tables?
 		 * In this case, also updates of routing table entries are made if the new route has better QoS values than the old one.
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean AVOID_DUPLICATES_IN_ROUTING_TABLES = true;
 
 		/**
 		 * Defines the hop costs for a route to a direct neighbor. 
 		 */
 		public static final int HOP_COSTS_TO_A_DIRECT_NEIGHBOR = 1;
 
 		/**
 		 * Defines the time between two triggers for the HRMController/node specific "report/share phase"
 		 * The higher in the hierarchy a coordinator is, the higher is the multiplier for this value.
 		 */
 		public static final double REPORT_SHARE_PHASE_TIME_BASE = 2.0; // in seconds
 		
 		/**
 		 * Define if the HRM based route should be recorded in a ProbeRoutingProperty if the connection  request uses this property.
 		 */		
 		public static final boolean RECORD_ROUTE_FOR_PROBES = true;
 
 		/**
 		 * Defines the default timeout for a route. If the route doesn't get any refresh in the defined time period, the route gets deleted.
 		 */
 		public static final double ROUTE_TIMEOUT = REPORT_SHARE_PHASE_TIME_BASE * 2;
 
 		/**
 		 * Defines if multiple paths for the same destination should be distributed
 		 * This value should be set to "true". Otherwise, only the best-effort (hop costs!) route will be shared top-to-down.
 		 */
 		public static final boolean MULTIPATH_ROUTING = true;
 
 		public enum REPORT_SHARE_TIMINGS {CONSTANT, LINEAR, EXPONENTIAL}; 
 		public static REPORT_SHARE_TIMINGS REPORT_SHARE_PHASE_TIMING_SCHEME = REPORT_SHARE_TIMINGS.CONSTANT;
 	}
 
 	public static class QoS
 	{
 		/**
 		 * Defines if a reported HRM route should include QoS attributes 
 		 */
 		public static final boolean REPORT_QOS_ATTRIBUTES_AUTOMATICALLY = true;		
 
 		/**
 		 * Defines the max. allowed delay
 		 */
 		public static final long MAX_DELAY = Long.MAX_VALUE;
 		
 		/**
 		 * Defines if the reservation of data rate (bandwidth) is supported
 		 */
 		public static final boolean QOS_RESERVATIONS = true;
 	}
 	
 	/**
 	 * Configuration for the election process
 	 */
 	public static class Election //TV
 	{
 		/**
 		 * Defines if link states should be used.
 		 * This is used for distributed election processes.
 		 */
 		public static final boolean USE_LINK_STATES = true;
 
 		/**
 		 * Default priority for election process. This value is used when no value is explicitly defined for a node.
 		 */
 		public static final long DEFAULT_PRIORITY = 0;
 		
 		/**
 		 * (De-)activate sending of "alive" messages in order to detect dead cluster members.
 		 * IMPORTANT: Deactivating this function is only useful for debugging purposes.
 		 */
 		public static final boolean SEND_ALIVES = true;
 	}
 }
