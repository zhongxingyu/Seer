 package org.life.sl.mapmatching;
 
 /*
 JMapMatcher
 
 Copyright (c) 2011 Bernhard Barkow, Hans Skov-Petersen, Bernhard Snizek and Contributors
 
 mail: bikeability@life.ku.dk
 web: http://www.bikeability.dk
 
 This program is free software; you can redistribute it and/or modify it under 
 the terms of the GNU General Public License as published by the Free Software 
 Foundation; either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT 
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with 
 this program; if not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.geotools.feature.SchemaException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 //import org.geotools.util.logging.Logging;
 import org.life.sl.graphs.GraphParams;
 import org.life.sl.graphs.PathSegmentGraph;
 import org.life.sl.orm.HibernateUtil;
 import org.life.sl.orm.OSMNode;
 import org.life.sl.orm.Respondent;
 import org.life.sl.orm.ResultMetaData;
 import org.life.sl.orm.ResultRoute;
 import org.life.sl.orm.ResultNodeChoice;
 import org.life.sl.orm.SourceRoute;
 import org.life.sl.readers.shapefile.PointFileReader;
 import org.life.sl.routefinder.RFParams;
 import org.life.sl.routefinder.Label;
 import org.life.sl.routefinder.RouteFinder;
 import org.life.sl.utils.BestNAverageStat;
 import org.life.sl.utils.MathUtil;
 import org.life.sl.utils.Timer;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.Point;
 //import com.vividsolutions.jts.planargraph.Edge;
 import com.vividsolutions.jts.planargraph.DirectedEdge;
 import com.vividsolutions.jts.planargraph.Node;
 
 // JVM argument for max. heap size (required for large networks): -Xmx2048m
 
 /**
  * Main controller for the map matching algorithm
  * @author Bernhard Snizek
  * @author Bernhard Barkow
  */
 public class JMapMatcher {
 	public enum gpsLoader {
 		SHAPEFILE,
 		PGSQLDATABASE,
 		BULK_PGSQLDATABASE
 	}
 
 	private static String kCfgFileName = "JMM.cfg";
 	private static String kOutputDir = "results/";
 	public final double kCoordEps = 1.e-6;		///< tolerance for coordinate comparison (if (x1-x2 < kCoordEps) then x1==x2)
 
 	// input data:
 	private static gpsLoader kGPSLoader    = gpsLoader.BULK_PGSQLDATABASE;
 	private static gpsLoader kGraphLoader  = gpsLoader.PGSQLDATABASE;
 	private static boolean kUseReducedNetwork = true;	///< true: restrict network to an area enveloping the track
 	
 	private static String kGraphDataFileName = "testdata/SparseNetwork.shp";
 	private static String kGPSPointFileName = "testdata/GPS_Points.shp";
 	
 	private static int kGPSTrackID = 12158;		///< database ID of GPS track to match	
 
 	private PathSegmentGraph graph = null;	///< data basis (graph)
 	private GPSTrack gpsPoints;		///< the path to match (GPS points)
 	private RFParams rfParams = null;		///< parameters for the route finding algorithm
 	private static JMMConfig cfg = new JMMConfig(kCfgFileName);	///< parameters for the MapMatching controller
 	
 	private int sourcerouteID = 0;
 	
 	private com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
 	private static Logger logger = Logger.getLogger("JMapMatcher");
 
 	/**
 	 * Default constructor: initialization with an empty graph (graph must then be be created later on)
 	 */
 	public JMapMatcher() {
 		this.graph = null;
 	}
 
 	/**
 	 * Initialization with an existing graph (right now, we only store the PathSegmentGraph locally)
 	 * @param g the PathSegmentGraph containing the path (if ==null, it can be created later on)
 	 */
 	public JMapMatcher(PathSegmentGraph g) {
 		this.graph = g;
 	}
 	
 	/**
 	 * Loads GPS data (points) from a file
 	 * @param fileName shapefile containing the GPS data points 
 	 * @throws IOException
 	 */
 	public void loadGPSPoints(String fileName) throws IOException {
 		File pointFile = new File(fileName);	// load data
 		PointFileReader pfr = new PointFileReader(pointFile);	// initialize data from file
 		gpsPoints = new GPSTrack(pfr.getPoints());	// the collection of GPS data points
 	}
 
 	/**
 	 * Deletes the currently loaded graph in order to free memory and start matching a new track (by setting it to null)
 	 */
 	public void clearGraph() {
 		this.graph = null;
 	}
 
 	/**
 	 * Loads a PathSegmentGraph containing the network to use (track + buffer)
 	 * @param track the GPS data
 	 * @return
 	 */
 	private PathSegmentGraph loadGraphFromDB(ArrayList<Point> track) {
 		String dumpFile = "";
		if (cfg.bDumpNetwork) dumpFile = String.format("%s/%05d%s", cfg.sDumpNetworkDir, sourcerouteID, ".shp");	// path for network buffer dump
 		return new PathSegmentGraph(track, (float)rfParams.getDouble(RFParams.Type.NetworkBufferSize), dumpFile);
 	}
 	
 	/**
 	 * Loads GPS data from a file, then invokes the actual matching
 	 * @throws IOException
 	 */
 	public void match(String fileName) throws IOException {
 		loadGPSPoints(fileName);
 		match();
 	}	
 
 	/**
 	 * Starts the map matching using GPS data from the database
 	 * @param sourceroute_id database ID of the sourcepoints (GPS route)
 	 */
 	public void match(int sourceroute_id)  {
 		System.out.println("Matching sourcerout (ID=" + sourceroute_id + ")");
 		gpsPoints = new GPSTrack(sourceroute_id);
 		if (gpsPoints.size() > 0) {	// check if the track contains points
 			sourcerouteID = sourceroute_id;				// store in class variable, for later use
 			match();								// start the matching process with the loaded track
 		} else {
 			System.err.println("GPS track " + sourceroute_id + " contains no points!");
 		}
 	}
 
 	/**
 	 * Controls the map matching algorithm (assumes GPS data and graph data have been loaded before);
 	 * if no graph has been loaded yet, a new graph enveloping the GPS track is read from the database
 	 * @throws IOException
 	 */
 	public void match() {
 		Timer timer = new Timer();
 		timer.init();	// initialize timer
 		
 		RFParams rfParams = initConstraints();
 		boolean repeat = false;
 		do {	// loop: will be repeated if network buffer is resized
 			if (graph == null || repeat) {	// create a new graph enveloping the GPS track
 				graph = loadGraphFromDB(gpsPoints);
 			}
 			Node fromNode = graph.findClosestNode(gpsPoints.getCoordinate(0));	// first node (Origin)
 			Node toNode   = graph.findClosestNode(gpsPoints.getCoordinate(-1));	// last node in GPS route (Destination) 
 			// log coordinates:
 			logger.info("Origin:      " + fromNode.getCoordinate());
 			logger.info("Destination: " + toNode.getCoordinate());
 			double t_0 = timer.getRunTime(true, "++ graph loaded");
 
 			RouteFinder rf = new RouteFinder(graph, rfParams);	// perform the actual route finding procedure on the PathSegmentGraph
 			rf.calculateNearest();	// calculate nearest edges to all data points (needed for edges statistics)
 			// Prepare the evaluation (assigning score to labels):
 			EdgeStatistics eStat = new EdgeStatistics(rf, gpsPoints);
 			double t_2 = timer.getRunTime(true);
 	
 			ArrayList<Label> labels = rf.findRoutes(fromNode, toNode, gpsPoints.getTrackLength());	///< list containing all routes that were found (still unsorted)
 	
 			double t_1 = timer.getRunTime(true, "++ Routefinding finished");
 			double t_3 = 0;
 			
 			repeat = false;
 			if (!labels.isEmpty()) {
 				// loop over all result routes, store them together with their score: 
 				t_2 += timer.getRunTime(true, "++ Edge statistics created");
 				Collections.sort(labels, Collections.reverseOrder());	// sort labels (result routes) by their score in reverse order, so that the best (highest score) comes first
 		
 				int nOK = saveData(labels, fromNode, toNode, eStat);
 				
 				t_3 = timer.getRunTime(true, "++ " + nOK + " routes stored");
 				logger.info("++ load graph: " + t_0 + "s");
 				logger.info("++ findRoutes: " + t_1 + "s");
 				logger.info("++ saveRoutes: " + t_3 + "s");
 			} else {	// no labels found
 				logger.warn("No labels found");
 				double bsf = rfParams.getDouble(RFParams.Type.NoLabelsResizeNetwork);
 				if (bsf > 1.) {	// try to resize the network
 					double bs = rfParams.getDouble(RFParams.Type.NetworkBufferSize) * bsf;
 					rfParams.setDouble(RFParams.Type.NetworkBufferSize, bs);
 					if (bs <= rfParams.getDouble(RFParams.Type.NetworkBufferSizeMax)) {
 						repeat = true;
 						logger.info("Network buffer resized to " + bs + " - repeating task");
 					} else {	// network size limit reached
 						logger.warn("Network buffer size limit reached!");
 					}
 					// if repeat==true, the matching will now be repeated with a bigger network buffer
 				}
 			}
 			logger.info("++ Total time: " + (t_0 + t_1 + t_2 + t_3) + "s");
 		} while (repeat);
 	}
 	
 	/**
 	 * Saves the results of the map matching, either to shapefiles (deprecated) or to a database (via Hibernate) 
 	 * @param labels list of result routes (all labels)
 	 * @param fromNode start node of the routes (origin)
 	 * @param toNode end node of the routes (destination)
 	 * @param eStat statistics object, required for writing track metadata
 	 * @return the number of results that have been saved 
 	 */
 	private int saveData(ArrayList<Label> labels, Node fromNode, Node toNode, EdgeStatistics eStat) {
 		String outFileName = "";
 		int respondentID = 0;
 		
 		if (cfg.bWriteToDatabase) {
 			// clear existing results from relevant database tables:
 			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 			try {
 				session.beginTransaction();
 				session.createQuery("delete from ResultMetaData where sourcerouteid=" + sourcerouteID).executeUpdate();
 				session.createQuery("delete from ResultRoute where sourcerouteid=" + sourcerouteID).executeUpdate();
 				session.createQuery("delete from ResultNodeChoice where sourcerouteid=" + sourcerouteID).executeUpdate();
 				session.getTransaction().commit();
 			} catch (Exception e) {
 				logger.error("Error while deleting from database tables: maybe the tables have not been created yet? " + e.toString());
 			}
 		
 			Respondent resp = Respondent.getForSourceRouteID(sourcerouteID);
 			respondentID = resp.getId();
 		}	
 
 		// write data for matched routes (1 record per matched route):
 		int nNonChoice = 0;
 		int nOK = 0;
 		boolean first = true;
 		int nLabels = labels.size();
 		int nRoutes = Math.min(cfg.nRoutesToWrite, nLabels);
 		ArrayList<Integer> selRoutes = new ArrayList<Integer>(nRoutes); 
 		int j;
 		for (int i = 0; i < nRoutes; i++) {
 			if (i < cfg.iWriteNBest || i >= nRoutes - cfg.iWriteNWorst || nRoutes == nLabels) {	// write those without randomization
 				j = i;
 			} else {	// select random route
 				do {
 					j = (int)(Math.random() * nLabels + .5);
 				} while (selRoutes.contains(j));
 			}
 			Label curLabel = labels.get(j);
 			
 			if (cfg.bWriteToDatabase) {
 				if (writeLabelToDatabase(curLabel, first, respondentID, fromNode, toNode)) {
 					nOK++;
 				} else {
 					logger.error("ERROR storing route!!");
 				}
 			}
 			
 			if (cfg.bWriteToShapefiles) {
 				try {
 					if (first) {	// the first route is the "choice" (best score) ...
 						first = false;
 						outFileName = kOutputDir + "Best.shp";
 					} else {	// ... the other routes are "non-choices"+
 						outFileName = String.format("%s%03d%s", kOutputDir + "NonChoice", nNonChoice, ".shp");
 						nNonChoice++;
 					}
 					curLabel.dumpToShapeFile(outFileName);	// write result route to file
 				} catch (SchemaException e1) {
 					System.err.println("Error writing file " + outFileName + " (SchemaException)");
 				} catch (IOException e2) {
 					System.err.println("Error writing file " + outFileName + " (IOException)");
 				}
 			}
 			
 			first = false;	// remaining routes are non-choices
 		}
 
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		// write metadata (1 record per matched GPS track):
 		ResultMetaData metaData = new ResultMetaData(sourcerouteID, respondentID);
 
 		metaData.setnAlternatives(labels.size());
 		metaData.setMaxDistanceFactor((float)rfParams.getDouble(RFParams.Type.DistanceFactor));
 		metaData.setAvgDistPt((float)gpsPoints.getAvgDist());
 		metaData.setMaxDistPt((float)gpsPoints.getMaxDist());
 		metaData.setMinDistPt((float)gpsPoints.getMinDist());
 		metaData.setnPoints(gpsPoints.size());
 		metaData.setTrackLength((float)gpsPoints.getTrackLength());
 		metaData.setDistPEavg((float)eStat.getDistPEAvg());
 		metaData.setDistPEavg05((float)eStat.getDistPE05());
 		metaData.setDistPEavg50((float)eStat.getDistPE50());
 		metaData.setDistPEavg95((float)eStat.getDistPE95());
 		// add scores for the best routes:
 		int[] nBest = { 1, 5, 10, 25, 50, 100 };
 		BestNAverageStat matchScoreAvgs = new BestNAverageStat(nBest);
 		BestNAverageStat matchLengthAvgs = new BestNAverageStat(nBest);
 		BestNAverageStat noMatchEdgeAvgs = new BestNAverageStat(nBest);
 		for (int i=0; i < 100; i++) {
 			//matchScoreAvgs.add(labels.get(i).getScore());
 			ResultRoute route = new ResultRoute(sourcerouteID, respondentID, i==0, labels.get(i), gpsPoints);
 			matchScoreAvgs.add(route.getMatchScore());
 			matchLengthAvgs.add(route.getMatchLengthR());
 			noMatchEdgeAvgs.add((double)(route.getnEdgesWOPts()));
 		}
 		metaData.setScoreAvgs(matchScoreAvgs.getAverages());
 		metaData.setMatchLengthAvgs(matchLengthAvgs.getAverages());
 		metaData.setNoMatchEdgeAvgs(noMatchEdgeAvgs.getAverages());
 		
 		
 		session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.save(metaData);
 		session.getTransaction().commit();
 
 		return nOK;
 	}
 	
 	/**
 	 * Writes the data of one label to the database (route data, node choice data)
 	 * @param label
 	 * @param isChoice true if the label is the selected one
 	 * @param respondentID ID of the respondent
 	 * @param fromNode origin O
 	 * @param toNode destination D
 	 * @return true if no error occurred during writing the data
 	 */
 	private boolean writeLabelToDatabase(Label label, boolean isChoice, int respondentID, Node fromNode, Node toNode) {
 		boolean ok = false;
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		
 		// set up entry for route table:
 		ResultRoute route = new ResultRoute(sourcerouteID, respondentID, isChoice, label, gpsPoints);
 		// set remaining route parameters:
 		// get node list to create the lineString representing the route:
 		Coordinate[] coordinates = label.getCoordinates();
 		ok = (coordinates.length > 0);	// true if there were any points in the route
 		if (ok) try {
 			LineString lineString = fact.createLineString(coordinates);
 			route.setGeometry(lineString);
 			
 			session.save(route);
 
 			// create output for the choice experiment:
 			if (cfg.bWriteChoices && isChoice) {	// (this is required only for the chosen route)
 				ResultNodeChoice choice = new ResultNodeChoice(route.getId(), sourcerouteID, respondentID);
 				
 				double dist = 0.;
 				int i = 0;	// counter
 				float[] edgeLengths = route.getEdgeLengths();
 				DirectedEdge lastEdge = null;
 				List<DirectedEdge> edges = label.getRouteAsEdges();
 				for (DirectedEdge e : edges) {		// for each node along the route:
 					@SuppressWarnings("unchecked")
 					HashMap<String, Object> ed = (HashMap<String, Object>) e.getEdge().getData();
 					Integer edgeID = (Integer)ed.get("id");
 					
 					Node node = e.getFromNode();	// node at beginning of edge
 					Coordinate c_n = node.getCoordinate();
 
 					// get node ID from database:
 					int nodeID = 0;
 					String s = " from OSMEdge where id=" + edgeID;
 					//s = "from OSMNode where id in ( (select fromnode"+s+"), (select tonode"+s+") )";	// this sometimes yields only 1 record instead of 2!?!
 					s = "from OSMNode where (id = (select fromnode"+s+") or id = (select tonode"+s+"))";
 					Query nodeRes = session.createQuery(s);
 					// match coordinates:
 					@SuppressWarnings("unchecked")
 					Iterator<OSMNode> it = nodeRes.iterate();
 					while (it.hasNext()) {
 						OSMNode on = it.next();
 						Coordinate onc = on.getGeometry().getCoordinate();
 						if (Math.abs(c_n.x - onc.x) < kCoordEps && Math.abs(c_n.x - onc.x) < kCoordEps) {
 							nodeID = on.getId();
 							break;
 						}								
 					}	// now, nodeID is either 0 or the database ID of the corresponding node
 					choice.setNodeID(nodeID);
 					choice.setI(i);
 					choice.setDist((float)(dist / label.getLength()));	// distance along the route as fraction of the whole route
 					
 					// angle from node to destination:
 					Coordinate c_d = toNode.getCoordinate();
 					double angle_direct = Math.atan2(c_d.y - c_n.y, c_d.x - c_n.x);
 
 					@SuppressWarnings("unchecked")
 					List<DirectedEdge> outEdges = node.getOutEdges().getEdges();
 					for (DirectedEdge oe : outEdges) {
 						if (oe != lastEdge) {	// don't use the backEdge!	// if (lastEdge != null && 
 							@SuppressWarnings("unchecked")
 							HashMap<String, Object> ed2 = (HashMap<String, Object>) oe.getEdge().getData();
 							choice.setSelected(oe == e);
 							choice.setEdgeID((Integer)ed2.get("id"));
 							choice.setEnvType((Short)ed2.get("et"));
 							choice.setCykType((Short)ed2.get("ct"));
 							choice.setAngleToDest((float)(MathUtil.mapAngle_degrees(Math.toDegrees(oe.getAngle() - angle_direct))));	// store value in degrees
 							choice.setAngle(lastEdge != null ? (float)MathUtil.mapAngle_degrees(Math.toDegrees(oe.getAngle() - lastEdge.getAngle())) : 0);	// angle between edges at node
 	
 							session.save(choice.clone());	// save 1 choice/nonchoice for each outEdge!
 						}
 					}
 					dist += edgeLengths[i++];
 					lastEdge = e;	// save for next comparison
 				}
 				
 			}
 			
 			session.getTransaction().commit();
 		} catch (Exception e) {
 			ok = false;
 			System.out.println(e);
 		}
 
 		return ok;
 	}
 
 	/**
 	 * Setup of default configuration parameters for the routefinding algorithm
 	 * @return
 	 */
 	private RFParams initConstraints() {
 		// initialize constraint fields:
 		rfParams = new RFParams();
 
 		// initialize with hardwired default values:
 		rfParams.setInt(RFParams.Type.MaximumNumberOfRoutes, 2000);	///< maximum number of routes to find (or 0 for infinite)
 		rfParams.setInt(RFParams.Type.BridgeOverlap, 1);
 		rfParams.setInt(RFParams.Type.EdgeOverlap, 1);				///< how often each edge may be used
 		//rfParams.setInt(RFParams.Type.ArticulationPointOverlap, 2);
 		rfParams.setInt(RFParams.Type.NodeOverlap, 1);				///< how often each single node may be crossed
 		rfParams.setDouble(RFParams.Type.DistanceFactor, 1.1);		///< how much the route may deviate from the shortest possible
 		rfParams.setDouble(RFParams.Type.MinimumLength, 0.0);		///< minimum route length
 		rfParams.setDouble(RFParams.Type.MaximumLength, 1.e20);		///< maximum route length (quasi no limit here)
 		rfParams.setDouble(RFParams.Type.NetworkBufferSize, 100.);	///< buffer size in meters (!)
 		rfParams.setInt(RFParams.Type.RejectedLabelsLimit, 0);		///< limit for rejected labels (if no routes are found)
 		
 		// read config file, eventually overwriting existing values:
 		int r = rfParams.readFromFile(kCfgFileName);
 		if (r < 0) logger.warn("Config file " + kCfgFileName + " not found!");
 		else logger.info("Read " + r + " values from config file " + kCfgFileName);
 		
 		return rfParams;
 	}
 
 	/**
 	 * main method: loads the data and invokes the matching algorithm
 	 * @param args command line arguments: 1: route ID; 2: route ID (end of range)
 	 * @throws IOException 
 	 */
 	public static void main(String... args) throws IOException {
 		//Logger logger = Logger.getRootLogger();
 		//BasicConfigurator.configure();	// set up logging
 		logger.setLevel(cfg.logLevel);
 		Logger.getRootLogger().setLevel(cfg.logLevel);
 		//logger.setLevel(Level.INFO);
 		GraphParams.getInstance().setSnap(cfg.graphSnapDistance > 0.);
 		GraphParams.getInstance().setSnapDistance(cfg.graphSnapDistance);
 		
 		PathSegmentGraph g = null;
 		// Let us load the graph ...
 		if (kGraphLoader == gpsLoader.PGSQLDATABASE) {
 			if (!kUseReducedNetwork) g = new PathSegmentGraph(1);	//... or delay the loading if kUseReducedNetwork==true
 		} else if (kGraphLoader == gpsLoader.SHAPEFILE) {
 			g = new PathSegmentGraph(kGraphDataFileName);	// get network from a file
 		}
 		// ... and invoke the matching algorithm:
 		if (kGPSLoader == gpsLoader.PGSQLDATABASE) {
 			new JMapMatcher(g).match(kGPSTrackID);			// get track from database; NOTE: g may be null here
 		} 
 		
 		if (kGPSLoader == gpsLoader.SHAPEFILE) {
 			new JMapMatcher(g).match(kGPSPointFileName);	// get track data from a file
 		}
 		
 		// ... a sweet bulk loader
 		if (kGPSLoader == gpsLoader.BULK_PGSQLDATABASE) {
 			// 1. get a list over sourceroute ids
 			org.hibernate.classic.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 			session.beginTransaction();
 			
 			Query result;
 			String query = "from SourceRoute";
 			if (args.length == 0 && cfg.sourcerouteID >= 0) query += " WHERE id="+cfg.sourcerouteID;
 			if (args.length == 1) query += " WHERE id="+args[0];
 			if (args.length == 2) query += " WHERE id>="+args[0]+" AND id<="+args[1];
 			result = session.createQuery(query);
 			@SuppressWarnings("unchecked")
 			Iterator<SourceRoute> iterator = result.iterate();
 			logger.info(result.list().size() + " tracks to be matched");
 			ArrayList<Integer> sRoutes= new ArrayList<Integer>();
 			while (iterator.hasNext()) {
 				SourceRoute sR = iterator.next();
 				sRoutes.add(sR.getId());
 				//
 			}
 			JMapMatcher jmm = new JMapMatcher(null);
 			for (int i=0; i<sRoutes.size(); i++) {
 				logger.info("--- Matching track " + sRoutes.get(i) + "...");
 				jmm.clearGraph();	// clear the graph, so that a new one enveloping the current track is loaded
 				jmm.match(sRoutes.get(i));
 				logger.info("--- Track " + sRoutes.get(i) + " matched.");
 			}
 			
 		}
 	}
 }
