 package com.bluebarracudas.model;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /** A factory to provide unique instances of TStops and TTrips */
 public class TFactory {
 
 	/** Whether or not the TStations are initialized */
 	private static boolean bStationsInitialized = false;
 	/** Whether or not the TStops are initialized */
 	private static boolean bStopsInitialized = false;
 	
 	/** A map from stop IDs to the corresponding TStop */
 	private static volatile Map<Integer, TStop> g_pStopMap;
 	/** A map from station IDs to the corresponding TStation */
 	private static volatile Map<Integer, TStation> g_pStationMap;
 	/** A map from trip IDs to the corresponding TTrip */
 	private static volatile Map<String, TTrip> g_pTripMap;
 
 	/** Private default constructor */
 	private TFactory() { }
 
 	/** Helper function for creating TStops */
 	private static void addStop( TStationData pStationData, TStopData ... pStopData ) {
 		for( TStopData pData : pStopData ) {
 			// Create a new stop object and put it in our map
 			TStop pStop = new TStop(pStationData, pData);
 			g_pStopMap.put( pData.getID(), pStop );
 
 			// Add the stop to the given station
 			getStation( pStationData.getID() ).addStop(pStop);
 		}
 	}
 
 	/** Helper function for creating TStations */
 	private static void addStation(TStationData pData, Point2D pPosition ) {
 		g_pStationMap.put( pData.getID(), new TStation(pData, pPosition) );
 	}
 
 	/** Helper function for connecting TStops */
 	private static void connectStops( TStopData ... pStopData ) {
 		for (int iData = 0; iData < pStopData.length; iData++ ) {
 			TStop pCurrent = getStop(pStopData[iData].getID());
 
 			if(iData > 0)
 				pCurrent.addPrevStop(getStop(pStopData[iData - 1].getID()));
 
 			if(iData != pStopData.length - 1)
 				pCurrent.addNextStop(getStop(pStopData[iData + 1].getID()));
 		}
 	}
 
 	/**
 	 * Set up our factory
 	 *
 	 * @author Tom Fiset
 	 * Revised by: Chris Welden, Liz Brown
 	 */
 	private static void initialize() {
 		// Set up our maps
 		g_pStopMap    = new HashMap<Integer, TStop>();
 		g_pStationMap = new HashMap<Integer, TStation>();
 		g_pTripMap    = new HashMap<String, TTrip>();
 
 		if (!bStationsInitialized)
 			initStations();
 		
 		if (!bStopsInitialized)
 			initStops();
 
 		connectAllStops();
 	}
 
 	/** Set up our stations */
 	private static void initStations() {
 		// Red Line
 		addStation( TStationData.ALEWIFE,       new Point2D.Float(207, 139) );
 		addStation( TStationData.DAVIS,         new Point2D.Float(242, 139) );
 		addStation( TStationData.PORTER,        new Point2D.Float(290, 147) );
 		addStation( TStationData.HARVARD,       new Point2D.Float(317, 174) );
 		addStation( TStationData.CENTRAL,       new Point2D.Float(341, 198) );
 		addStation( TStationData.KENDALL,       new Point2D.Float(370, 227) );
 		addStation( TStationData.CHARLES,       new Point2D.Float(396, 254) );
 		addStation( TStationData.PARK,          new Point2D.Float(423, 281) );
 		addStation( TStationData.DTX,           new Point2D.Float(449, 307) );
 		addStation( TStationData.SOUTH,         new Point2D.Float(491, 349) );
 		addStation( TStationData.BROADWAY,      new Point2D.Float(495, 387) );
 		addStation( TStationData.ANDREW,        new Point2D.Float(495, 423) );
 		addStation( TStationData.JFK,           new Point2D.Float(495, 459) );
 		addStation( TStationData.SAVINHILL,     new Point2D.Float(460, 501) );
 		addStation( TStationData.FIELDS_CORNER, new Point2D.Float(451, 527) );
 		addStation( TStationData.SHAWMUT,       new Point2D.Float(451, 559) );
 		addStation( TStationData.ASHMONT,       new Point2D.Float(451, 588) );
 		addStation( TStationData.NORTH_QUINCY,  new Point2D.Float(557, 562) );
 		addStation( TStationData.WOLLASTON,     new Point2D.Float(585, 590) );
 		addStation( TStationData.QUINCY_CENTER, new Point2D.Float(612, 617) );
 		addStation( TStationData.QUINCY_ADAMS,  new Point2D.Float(640, 645) );
 		addStation( TStationData.BRAINTREE,     new Point2D.Float(649, 706) );
 
 		// Orange Line
 		addStation( TStationData.OAK_GROVE,     new Point2D.Float(451, 43) );
 		addStation( TStationData.MALDEN,        new Point2D.Float(451, 69) );
 		addStation( TStationData.WELLINGTON,    new Point2D.Float(451, 94) );
 		addStation( TStationData.SULLIVAN,      new Point2D.Float(451, 119) );
 		addStation( TStationData.COMM_COLLEGE,  new Point2D.Float(451, 145) );
 		addStation( TStationData.NORTH,         new Point2D.Float(461, 200) );
 		addStation( TStationData.HAYMARKET,     new Point2D.Float(478, 229) );
 		addStation( TStationData.STATE,         new Point2D.Float(478, 257) );
 		addStation( TStationData.CHINATOWN,     new Point2D.Float(417, 339) );
 		addStation( TStationData.TUFTS,         new Point2D.Float(400, 357) );
 		addStation( TStationData.BACK_BAY,      new Point2D.Float(381, 376) );
 		addStation( TStationData.MASS_AVE,      new Point2D.Float(362, 395) );
 		addStation( TStationData.RUGGLES,       new Point2D.Float(342, 415) );
 		addStation( TStationData.ROXBURY,       new Point2D.Float(312, 445) );
 		addStation( TStationData.JACKSON,       new Point2D.Float(296, 462) );
 		addStation( TStationData.STONY_BROOK,   new Point2D.Float(282, 476) );
 		addStation( TStationData.GREEN_ST,      new Point2D.Float(268, 490) );
 		addStation( TStationData.FOREST_HILLS,  new Point2D.Float(251, 507) );
 
 		// Blue Line
 		addStation( TStationData.WONDERLAND,    new Point2D.Double(695, 62) );
 		addStation( TStationData.REVERE,        new Point2D.Double(671, 86) );
 		addStation( TStationData.BEACHMONT,     new Point2D.Double(651, 106) );
 		addStation( TStationData.SUFFOLK,       new Point2D.Double(630, 127) );
 		addStation( TStationData.ORIENT_HGHTS,  new Point2D.Double(610, 147) );
 		addStation( TStationData.WOOD_ISLAND,   new Point2D.Double(590, 168) );
 		addStation( TStationData.AIRPORT,       new Point2D.Double(570, 188) );
 		addStation( TStationData.MAVERICK,      new Point2D.Double(546, 212) );
 		addStation( TStationData.AQUARIUM,      new Point2D.Double(505, 253) );
 		addStation( TStationData.GVT_CENTER,    new Point2D.Double(457, 247) );
 		addStation( TStationData.BOWDOIN,       new Point2D.Double(436, 226) );
 	
 		// Set our initialization flag for TStations
 		bStationsInitialized = true;
 	}
 
 	/** Set up our stops */
 	private static void initStops() {
 		// Red Line
 		addStop( TStationData.ALEWIFE,       TStopData.ALEWIFE_S,       TStopData.ALEWIFE_N );
 		addStop( TStationData.DAVIS,         TStopData.DAVIS_S,         TStopData.DAVIS_N );
 		addStop( TStationData.PORTER,        TStopData.PORTER_S,        TStopData.PORTER_N );
 		addStop( TStationData.HARVARD,       TStopData.HARVARD_S,       TStopData.HARVARD_N );
 		addStop( TStationData.CENTRAL,       TStopData.CENTRAL_S,       TStopData.CENTRAL_N );
 		addStop( TStationData.KENDALL,       TStopData.KENDALL_S,       TStopData.KENDALL_N );
 		addStop( TStationData.CHARLES,       TStopData.CHARLES_S,       TStopData.CHARLES_N );
 		addStop( TStationData.PARK,          TStopData.PARK_S,          TStopData.PARK_N );
 		addStop( TStationData.DTX,           TStopData.DTX_S_R,         TStopData.DTX_N_R );
 		addStop( TStationData.SOUTH,         TStopData.SOUTH_S,         TStopData.SOUTH_N );
 		addStop( TStationData.BROADWAY,      TStopData.BROADWAY_S,      TStopData.BROADWAY_N );
 		addStop( TStationData.ANDREW,        TStopData.ANDREW_S,        TStopData.ANDREW_N );
 		addStop( TStationData.JFK,           TStopData.JFK_S_A,         TStopData.JFK_N_A,
 		                                     TStopData.JFK_S_B,         TStopData.JFK_N_B );
 		addStop( TStationData.SAVINHILL,     TStopData.SAVINHILL_S,     TStopData.SAVINHILL_N );
 		addStop( TStationData.FIELDS_CORNER, TStopData.FIELDS_CORNER_S, TStopData.FIELDS_CORNER_N );
 		addStop( TStationData.SHAWMUT,       TStopData.SHAWMUT_S,       TStopData.SHAWMUT_N );
 		addStop( TStationData.ASHMONT,       TStopData.ASHMONT_S,       TStopData.ASHMONT_N );
 		addStop( TStationData.NORTH_QUINCY,  TStopData.NORTH_QUINCY_S,  TStopData.NORTH_QUINCY_N );
 		addStop( TStationData.WOLLASTON,     TStopData.WOLLASTON_S,     TStopData.WOLLASTON_N );
 		addStop( TStationData.QUINCY_CENTER, TStopData.QUINCY_CENTER_S, TStopData.QUINCY_CENTER_N );
 		addStop( TStationData.QUINCY_ADAMS,  TStopData.QUINCY_ADAMS_S,  TStopData.QUINCY_ADAMS_N );
 		addStop( TStationData.BRAINTREE,     TStopData.BRAINTREE_S,     TStopData.BRAINTREE_N );
 
 		// Orange Line
 		addStop( TStationData.OAK_GROVE,     TStopData.OAK_GROVE_S,     TStopData.OAK_GROVE_N );
 		addStop( TStationData.MALDEN,        TStopData.MALDEN_S,        TStopData.MALDEN_N );
 		addStop( TStationData.WELLINGTON,    TStopData.WELLINGTON_S,    TStopData.WELLINGTON_N );
 		addStop( TStationData.SULLIVAN,      TStopData.SULLIVAN_S,      TStopData.SULLIVAN_N );
 		addStop( TStationData.COMM_COLLEGE,  TStopData.COMM_COLLEGE_S,  TStopData.COMM_COLLEGE_N );
 		addStop( TStationData.NORTH,         TStopData.NORTH_S,         TStopData.NORTH_N );
 		addStop( TStationData.HAYMARKET,     TStopData.HAYMARKET_S,     TStopData.HAYMARKET_N );
 		addStop( TStationData.STATE,         TStopData.STATE_S_O,       TStopData.STATE_N_O );
 		addStop( TStationData.DTX,           TStopData.DTX_S_O,         TStopData.DTX_N_O );
 		addStop( TStationData.CHINATOWN,     TStopData.CHINATOWN_S,     TStopData.CHINATOWN_N );
 		addStop( TStationData.TUFTS,         TStopData.TUFTS_S,         TStopData.TUFTS_N );
 		addStop( TStationData.BACK_BAY,      TStopData.BACK_BAY_S,      TStopData.BACK_BAY_N );
 		addStop( TStationData.MASS_AVE,      TStopData.MASS_AVE_S,      TStopData.MASS_AVE_N );
 		addStop( TStationData.RUGGLES,       TStopData.RUGGLES_S,       TStopData.RUGGLES_N );
 		addStop( TStationData.ROXBURY,       TStopData.ROXBURY_S,       TStopData.ROXBURY_N );
 		addStop( TStationData.JACKSON,       TStopData.JACKSON_S,       TStopData.JACKSON_N );
 		addStop( TStationData.STONY_BROOK,   TStopData.STONY_BROOK_S,   TStopData.STONY_BROOK_N );
 		addStop( TStationData.GREEN_ST,      TStopData.GREEN_ST_S,      TStopData.GREEN_ST_N );
 		addStop( TStationData.FOREST_HILLS,  TStopData.FOREST_HILLS_S,  TStopData.FOREST_HILLS_N );
 
 		// Blue Line
 		addStop( TStationData.WONDERLAND,    TStopData.WONDERLAND_S,    TStopData.WONDERLAND_N );
 		addStop( TStationData.REVERE,        TStopData.REVERE_S,        TStopData.REVERE_N );
 		addStop( TStationData.BEACHMONT,     TStopData.BEACHMONT_S,     TStopData.BEACHMONT_N );
 		addStop( TStationData.SUFFOLK,       TStopData.SUFFOLK_S,       TStopData.SUFFOLK_N );
 		addStop( TStationData.ORIENT_HGHTS,  TStopData.ORIENT_HGHTS_S,  TStopData.ORIENT_HGHTS_N );
 		addStop( TStationData.WOOD_ISLAND,   TStopData.WOOD_ISLAND_S,   TStopData.WOOD_ISLAND_N );
 		addStop( TStationData.AIRPORT,       TStopData.AIRPORT_S,       TStopData.AIRPORT_N );
 		addStop( TStationData.MAVERICK,      TStopData.MAVERICK_S,      TStopData.MAVERICK_N );
 		addStop( TStationData.AQUARIUM,      TStopData.AQUARIUM_S,      TStopData.AQUARIUM_N );
 		addStop( TStationData.STATE,         TStopData.STATE_S_B,       TStopData.STATE_N_B );
 		addStop( TStationData.GVT_CENTER,    TStopData.GVT_CENTER_S,    TStopData.GVT_CENTER_N );
 		addStop( TStationData.BOWDOIN,       TStopData.BOWDOIN_S,       TStopData.BOWDOIN_N );
 	
 		// Set our initialization flag for TStops
 		bStopsInitialized = true;
 	}
 
 	private static void connectAllStops() {
 		// Red Line - South Bound
 		connectStops( TStopData.ALEWIFE_S,       TStopData.DAVIS_S,        TStopData.PORTER_S,         TStopData.HARVARD_S,
 		              TStopData.CENTRAL_S,       TStopData.KENDALL_S,      TStopData.CHARLES_S,        TStopData.PARK_S,
 		              TStopData.DTX_S_R,         TStopData.SOUTH_S,        TStopData.BROADWAY_S,       TStopData.ANDREW_S,
 		              TStopData.JFK_S_A,         TStopData.SAVINHILL_S,    TStopData.FIELDS_CORNER_S,  TStopData.SHAWMUT_S,
 		              TStopData.ASHMONT_S );
 		connectStops( TStopData.ANDREW_S,        TStopData.JFK_S_B,        TStopData.NORTH_QUINCY_S,   TStopData.WOLLASTON_S,
 		              TStopData.QUINCY_CENTER_S, TStopData.QUINCY_ADAMS_S, TStopData.BRAINTREE_S );
 
 		// Red Line - North Bound
 		connectStops( TStopData.ASHMONT_N,       TStopData.SHAWMUT_N,      TStopData.FIELDS_CORNER_N,  TStopData.SAVINHILL_N,
 		              TStopData.JFK_N_A,         TStopData.ANDREW_N,       TStopData.BROADWAY_N,       TStopData.SOUTH_N,
 		              TStopData.DTX_N_R,         TStopData.PARK_N,         TStopData.CHARLES_N,        TStopData.KENDALL_N,
 		              TStopData.CENTRAL_N,       TStopData.HARVARD_N,      TStopData.PORTER_N,         TStopData.DAVIS_N,
 		              TStopData.ALEWIFE_N );
 		connectStops( TStopData.BRAINTREE_N,     TStopData.QUINCY_ADAMS_N, TStopData.QUINCY_CENTER_N,  TStopData.WOLLASTON_N,
 		              TStopData.NORTH_QUINCY_N,  TStopData.JFK_N_B,        TStopData.ANDREW_N );
 
 		// Orange Line - South Bound
 		connectStops( TStopData.OAK_GROVE_S,     TStopData.MALDEN_S,       TStopData.WELLINGTON_S,     TStopData.SULLIVAN_S,
 		              TStopData.COMM_COLLEGE_S,  TStopData.NORTH_S,        TStopData.HAYMARKET_S,      TStopData.STATE_S_O,
 		              TStopData.DTX_S_O,         TStopData.CHINATOWN_S,    TStopData.TUFTS_S,          TStopData.BACK_BAY_S,
 		              TStopData.MASS_AVE_S,      TStopData.RUGGLES_S,      TStopData.ROXBURY_S,        TStopData.JACKSON_S,
 		              TStopData.STONY_BROOK_S,   TStopData.GREEN_ST_S,     TStopData.FOREST_HILLS_S );
 		// Orange Line - North Bound
		connectStops( TStopData.FOREST_HILLS_N,  TStopData.GREEN_ST_N,     TStopData.STONY_BROOK_N,    TStopData.JACKSON_N,
 		              TStopData.ROXBURY_N,       TStopData.RUGGLES_N,      TStopData.MASS_AVE_N,       TStopData.BACK_BAY_N,
 		              TStopData.TUFTS_N,         TStopData.CHINATOWN_N,    TStopData.DTX_N_O,          TStopData.STATE_N_O,
 		              TStopData.HAYMARKET_N,     TStopData.NORTH_N,        TStopData.COMM_COLLEGE_N,   TStopData.SULLIVAN_N,
 		              TStopData.WELLINGTON_N,    TStopData.MALDEN_N,       TStopData.OAK_GROVE_N );
 
 		// Blue Line - South Bound
 		connectStops( TStopData.WONDERLAND_S,    TStopData.REVERE_S,       TStopData.BEACHMONT_S,      TStopData.SUFFOLK_S,
 		              TStopData.ORIENT_HGHTS_S,  TStopData.WOOD_ISLAND_S,  TStopData.AIRPORT_S,        TStopData.MAVERICK_S,
 		              TStopData.AQUARIUM_S,      TStopData.STATE_S_B,      TStopData.GVT_CENTER_S,     TStopData.BOWDOIN_S );
 		// Blue line - North Bound
		connectStops( TStopData.BOWDOIN_N,       TStopData.GVT_CENTER_N,   TStopData.STATE_N_B,        TStopData.AQUARIUM_N,
 		              TStopData.MAVERICK_N,      TStopData.AIRPORT_N,      TStopData.WOOD_ISLAND_N,    TStopData.ORIENT_HGHTS_N,
 		              TStopData.SUFFOLK_N,       TStopData.BEACHMONT_N,    TStopData.REVERE_N,         TStopData.WONDERLAND_N );
 	}
 
 	/**
 	 * Get the appropriate TStation
 	 *
 	 * @param nID The ID of the desired TStation
 	 */
 	public static TStation getStation(int nID) {
 		// If the factory hasn't been initialized yet, do so
 		if (!bStationsInitialized) initialize();
 
 		// Otherwise look up the station in the map
 		return g_pStationMap.get(nID);
 	}
 
 	/** Get a collection of all TStations */
 	public static Collection<TStation> getAllStations() {
 		// If the factory hasn't been initialized yet, do so
 		if (!bStationsInitialized) initialize();
 
 		// Otherwise return the map of stops
 		return g_pStationMap.values();
 	}
 
 	/**
 	 * Get the appropriate TStop
 	 *
 	 * @param nID The ID of the desired TStop
 	 */
 	public static synchronized TStop getStop(int nID) {
 		// If the factory hasn't been initialized yet, do so
 		if (!bStopsInitialized) initialize();
 
 		// Otherwise look up the stop in the map
 		return g_pStopMap.get(nID);
 	}
 	
 	/**
 	 * Get all of the TStops
 	 */
 	public static synchronized Collection<TStop> getAllStops() {
 		// If the factory hasn't been initialized yet, do so
 		if (!bStopsInitialized) initialize();
 		
 		// Otherwise return the map of stops
 		return g_pStopMap.values();
 	}
 	
 	/**
 	 * Get the appropriate TTrip, create a new one if it doesn't exist.
 	 *
 	 * @author Tom Fiset
 	 * Revised by: Liz Brown
 	 *
 	 * @param nID The ID of the desired TTrip
 	 */
 	public static synchronized TTrip getTrip(String sID) {
 		// If the factory hasn't been initialized yet, do so
 		if (!bStationsInitialized || !bStopsInitialized) initialize();
 
 		// Otherwise look up the trip in the map
 		TTrip result= g_pTripMap.get(sID);
 
 		// If the entry doesn't exist, create a new one
 		if (result == null)
 			result= new TTrip( sID );
 			g_pTripMap.put(sID, result);
 
 		return result;
 	}
 	
 	/**
 	 * Get all of the existing TTrips
 	 *
 	 * @author Liz Brown
 	 */
 	public static synchronized Collection<TTrip> getAllTrips() {
 		// If the factory hasn't been initialized yet, do so
 		if (!bStationsInitialized || !bStopsInitialized) initialize();
 		
 		// Otherwise return the map of trips
 		return g_pTripMap.values();
 	}
 
 	/**
 	 * Find the shortest path between two TStops
 	 */
 	public static TPath findShortestPath(int nStationIDA, int nStationIDB) {
 		return findShortestPath(getStation(nStationIDA), getStation(nStationIDB));
 	}
 
 	/**
 	 * Find the shortest path between two TStops
 	 */
 	public static TPath findShortestPath(TStation pStationA, TStation pStationB) {
 
 		// A Node to use in our BFS
 		class Node {
 			private TStop m_pStop;
 			private boolean m_bVisited;
 			private int m_nDistance;
 
 			public Node(TStop pStop) { m_pStop = pStop; m_bVisited = false; }
 
 			public boolean isVisited() { return m_bVisited; }
 			public void setVisited()   { m_bVisited = true; }
 			
 			public int getDistance()   { return m_nDistance; }
 			public void setDistance(int nDist)   { m_nDistance = nDist; }
 
 			public TStop getStop() { return m_pStop; }
 		}
 		
 		List<Node> pUnvisitedNodes = new ArrayList<Node>();
 		
 		
 		// loop:
 		//   if the current node is the destination node, stop
 		//   get all neighbors of current node
 		//   if a neighbor is unvisited, update it's distance
 		//   mark current node as visited
 		//   
 		//   set the unvisited node with the shortest distance as the next current node
 		
 		//TStop currentStop
 		return null;
 	}
 
 }
