 package org.life.sl.graphs;
 
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
 
 
 /*
  * The JTS Topology Suite is a collection of Java classes that
  * implement the fundamental operations required to validate a given
  * geo-spatial data set to a known topological specification.
  *
  * Copyright (C) 2001 Vivid Solutions
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * For more information, contact:
  *
  *     Vivid Solutions
  *     Suite #1A
  *     2328 Government Street
  *     Victoria BC  V8T 5G5
  *     Canada
  *
  *     (250)385-6040
  *     www.vividsolutions.com
  */
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.Transaction;
 import org.geotools.data.shapefile.ShapefileDataStore;
 import org.geotools.data.shapefile.ShapefileDataStoreFactory;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.data.simple.SimpleFeatureStore;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.SchemaException;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernatespatial.criterion.SpatialRestrictions;
 import org.life.sl.orm.HibernateUtil;
 import org.life.sl.orm.OSMEdge;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.Point;
 
 import com.vividsolutions.jts.planargraph.DirectedEdge;
 import com.vividsolutions.jts.planargraph.Edge;
 import com.vividsolutions.jts.planargraph.Node;
 
 /**
  * A planar graph of edges that is analyzed to sew the edges together. The 
  * <code>marked</code> flag on @{link com.vividsolutions.planargraph.Edge}s 
  * and @{link com.vividsolutions.planargraph.Node}s indicates whether they have been
  * logically deleted from the graph.
  *
  * @version 1.7
  */
 public class PathSegmentGraph {
 
 	private double xMin,xMax,yMin,yMax;
 
 	// algorithms
 	private AllPairsShortestPath allPairsShortestPath;
 	private boolean distancesCalculated;
 	private LineMergeGraphH4cked lineMergeGraphH4cked;
 	
 	private com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
 
 	private Logger logger = Logger.getRootLogger();
 	
 	public HashMap<Node, HashMap<Node, Float>> getAPSDistances() {
 		return allPairsShortestPath.getDistances();
 	}
 	public float[][] getAPSDistancesArr() {
 		return allPairsShortestPath.getDistancesArr();
 	}
 	
 	/**
 	 * default constructor: sets up an empty graph
 	 */
 	public PathSegmentGraph() {
 		super();
 		distancesCalculated = false;
 		setLineMergeGraphH4cked(new LineMergeGraphH4cked());
 	}
 	
 	/**
 	 * initialize the graph using data read from a shapefile
 	 * @param shapeFile the shapefile containing the network
 	 * @throws IOException
 	 */
 	public PathSegmentGraph(String shapeFile) throws IOException {
 		this();
 		addLineStringsFromShape(shapeFile);
 	}
 	
 	/**
 	 * initialize the graph from the database (using the complete network)
 	 * @param i
 	 */
 	public PathSegmentGraph(int i) {
 		this();
 		addLineStringsFromDatabase();
 	}
 	
 	/**
 	 * initialize the graph from a section of the (database-stored) network enveloping the GPS track 
 	 * @param track array of points on the track under examination
 	 */
 	public PathSegmentGraph(ArrayList<Point> track, float bufferSize, String dumpFile) {
 		this();
 		addLineStringsFromDatabase(track, bufferSize, dumpFile);
 	}
 	
 	/**
 	 * Create a new graph, with linestrings read from a shapefile 
 	 * @param shapeFile name of the shapefile containing the network data
 	 * @throws IOException
 	 */
 	public void addLineStringsFromShape(String shapeFile) throws IOException {
 		setLineMergeGraphH4cked(new LineMergeGraphH4cked());
 		distancesCalculated = false;
 		LineStringReader reader = new LineStringReader(shapeFile);
 		
 		reader.read();
 		boolean first = true;
 		int id = 0;
 		for (LineString ls : reader.getLineStrings()) {
 			if (first == true) {
 				xMax = xMin = ls.getCoordinate().x;
 				yMax = yMin = ls.getCoordinate().y;
 				first = false;
 			}
 	
 			addLineString(ls, ++id, (short)0, (short)0, 0);
 		}
 	}
 
 	/**
 	 * Create a new graph, with linestrings read from the database (using the complete table OSMEdge!) 
 	 */
 	public void addLineStringsFromDatabase() {
 		addLineStringsFromDatabase(null, 0, "");
 	}
 
 	/**
 	 * Create a new graph, with linestrings read from the database (optionally using only a buffer around a given track for the network)
 	 * @param track GPS track consisting of a list of data points
 	 * @param bufferSize size of the buffer to select around the track
 	 * @param dumpFile if not empty, the path of a shapefile to dump the network into.
 	 */
 	public void addLineStringsFromDatabase(ArrayList<Point> track, float bufferSize, String dumpFile) {
 		setLineMergeGraphH4cked(new LineMergeGraphH4cked());
 		distancesCalculated = false;
 
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		
 		//Query gpsResults = session.createQuery("from" );
 		
 		if (track == null) {
 			Query result = session.createQuery("from OSMEdge");
 			
 			@SuppressWarnings("unchecked")
 			Iterator<OSMEdge> iter = result.iterate();
 			while (iter.hasNext() ) {
 				OSMEdge  o = iter.next();
 				LineString g = o.getGeometry();
 				addLineString(g, o.getId(), o.getEnvtype(), o.getCyktype(), o.getGroenm());
 			}
 		} else {
 			// let us join the nodes of the track to a linestring ....
 			
 			Iterator<Point> trackIter = track.iterator();
 			
 //			ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
 //			while (trackIter.hasNext()) {
 //				Point p = trackIter.next();	
 //				coords.add(p.getCoordinate());
 //			}
 			
 			Coordinate[] coords = new Coordinate[track.size()];
 			int i =0;
 			while (trackIter.hasNext()) {
 				Point p = trackIter.next();	
 				coords[i] = p.getCoordinate();
 				i++;
 			}
 			
 			LineString l = fact.createLineString(coords);
 			
 			// ... build a buffer ...
 			
 			Geometry buffer = l.buffer(bufferSize);
 	        
 	        Criteria testCriteria = session.createCriteria(OSMEdge.class);
 			testCriteria.add(SpatialRestrictions.within("geometry", buffer));
 			@SuppressWarnings("unchecked")
 			List<OSMEdge> result = testCriteria.list();
 			
 			logger.info("Spatial query selected " + result.size() + " edges");
 			
 			Iterator<OSMEdge> iter = result.iterator();
 			i = 0;
 			while (iter.hasNext() ) {
 				i++;
 				OSMEdge  o = iter.next();
 				LineString g = o.getGeometry();
 				addLineString(g, o.getId(), o.getEnvtype(), o.getCyktype(), o.getGroenm());
 			}
 			
 			// if required, dump the graph to a shapefile:
 			if (dumpFile != "") {
 				try {
 					this.dumpBuffer(result, dumpFile);
 					logger.info("buffer dumped");
 				} catch (Exception e) {
 					logger.error("error dumping buffer: " + e);
 				}
 			}
 		}
 		
 		session.disconnect();
 	}
 	
 	public void dumpBuffer(List<OSMEdge> edges, String filename) throws SchemaException, IOException {
 		
 		final SimpleFeatureType TYPE = DataUtilities.createType("route",
 						"location:LineString:srid=4326," + // <- the geometry attribute: Polyline type
 						"name:String," + // <- a String attribute
 						"number:Integer" // a number attribute
 				);
 
 		// 1. build a feature
 		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
 		SimpleFeatureCollection collection = FeatureCollections.newCollection();
 		
 		Iterator<OSMEdge> iter = edges.iterator();
 		while (iter.hasNext()) {
 			OSMEdge o = iter.next();
 			SimpleFeature feature = featureBuilder.buildFeature(null);	
 			feature.setDefaultGeometry(o.getGeometry());
 		}
 		
 		logger.info("Writing to shapefile " + filename);
 		File newFile = new File(filename);
 		// File newFile = getNewShapeFile(file);
 
         ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
 
         Map<String, Serializable> params = new HashMap<String, Serializable>();
         params.put("url", newFile.toURI().toURL());
         params.put("create spatial index", Boolean.TRUE);
 
         ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
         newDataStore.createSchema(TYPE);
 
         // You can comment out this line if you are using the createFeatureType method (at end of
         // class file) rather than DataUtilities.createType
         newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
 		
         Transaction transaction = new DefaultTransaction("create");
 
         String typeName = newDataStore.getTypeNames()[0];
         SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
         
         if (featureSource instanceof SimpleFeatureStore) {
             SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
 
             featureStore.setTransaction(transaction);
             try {
                 featureStore.addFeatures(collection);
                 transaction.commit();
             } catch (Exception problem) {
                 problem.printStackTrace();
                 transaction.rollback();
             } finally {
                 transaction.close();
             }
             //System.exit(0); // success!
         } else {
         	logger.error(typeName + " does not support read/write access");
             //System.exit(1);	// exit program with status 1 (error)
         }
 	}
 
 	public void addLineString(LineString lineString, int id) {
 		addLineString(lineString, id, (short)0, (short)0, 0);
 	}
 
 	/**
 	 * Adds an Edge, DirectedEdges, and Nodes for the given LineString representation
 	 * of an edge. Snaps all vertices according to GraphParams.GLOBAL_SNAP_DIST
 	 * @param lineString
 	 * @param id : the id coming out of OSM
 	 */
 	public void addLineString(LineString lineString, int id, short envType, short cykType, double groenM) {
 
 		distancesCalculated = false;
 
 		if (lineString.isEmpty()) { return; }
 		if (lineString.getCoordinates().length < 2) { System.exit(1); }
 
 		Coordinate[] coordinates = lineString.getCoordinates();
 		modifyEnvelope(coordinates);
 
 		if (GraphParams.getInstance().getSnap()) {
 			double sd = GraphParams.getInstance().getSnapDistance();
 			for(Coordinate c : coordinates) {
 				c.x = c.x - (c.x % sd);
 				c.y = c.y - (c.y % sd);
 			}
 		}
 
 		Edge edge = getLineMergeGraphH4cked().addEdge(lineString);
 		if (edge != null) {	// edge might not have been added because of coinciding coordinates
 			if (lineString.getUserData() == null) lineString.setUserData(new HashMap<String, Object>(3));
 			@SuppressWarnings("unchecked")
 			HashMap<String, Object> userdata = (HashMap<String, Object>) lineString.getUserData();
 			// HashMap<String, Object> hm = new HashMap<String, Object>();
 			
 			userdata.put("id", id);
 			userdata.put("et", envType);
 			userdata.put("ct", cykType);
 			userdata.put("gm", groenM);	// groenM
 	// 		userdata.put("geom", lineString);
 			edge.setData(userdata);
 		}
 	}
 
 	private void modifyEnvelope(Coordinate[] coordinates) {
 		for(Coordinate c : coordinates) {
 			if(c.x < xMin) xMin = c.x;
 			if(c.x > xMax) xMax = c.x;
 			if(c.y < yMin) yMin = c.y;
 			if(c.y > yMax) yMax = c.y;
 		}
 	}
 
 	/**
 	 * Get the distance from one Node to another Node in the graph.
 	 * @param from The Node to get the distance from.
 	 * @param to The Node to get the distance to.
 	 * @return The distance between two Nodes
 	 */
 	public double getDistance(Node from, Node to) {
 		if(!distancesCalculated) {
 			allPairsShortestPath = new AllPairsShortestPath(this);
 			distancesCalculated = true;
 		}
 		return allPairsShortestPath.getDistance(from, to);
 	}
 
 	public void calculateDistances() {
 		if(!distancesCalculated) {
 			allPairsShortestPath = new AllPairsShortestPath(this);
 			distancesCalculated = true;
 		}
 	}
 
 	/**
 	 * Find the node in the graph that is nearest the query coordinate. Implemented as linear search, can be vastly improved using e.g. a kd-tree
 	 * @param query The Coordinate used for the query.
 	 * @return The Node in the graph that is nearest the query Coordinate.
 	 */
 	public Node findClosestNode(Coordinate query) {
 
 		double bestDistance = Double.MAX_VALUE;
 		Node closestNode = null;
 		for(Node n : getNodes()) {
 			double currentDistance = n.getCoordinate().distance(query);
 			if(currentDistance < bestDistance) {
 				closestNode = n;
 				bestDistance = currentDistance;
 			}
 		}
 		return closestNode;
 	}
 
 	public ArrayList<Node> getNodes() {
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		for(Object obj : getLineMergeGraphH4cked().getNodes()) {
 			nodes.add((Node)obj);
 		}
 		return nodes;
 	}
 
 
 	public Edge getEdgeByNodes(Node n1, Node n2) {
 		@SuppressWarnings("unchecked")
 		Iterator<Edge> ei = Node.getEdgesBetween(n1, n2).iterator();
 		return (ei.hasNext() ? ei.next() : null);
 	}
 	
 	public Edge getSingleEdgeAtNode(Node n1) {
 		try {
 			Object obj = n1.getOutEdges().getEdges().get(0);
 			return ((DirectedEdge)obj).getEdge();
 		} catch(Exception e) {
 			System.out.println(e);
 			return null;
 		}
 	}
 	
 	public Envelope getEnvelope() {
 		Envelope env = new Envelope(xMin, xMax, yMin, yMax);
 		return env;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Collection<Edge> getEdges() {
 		return (Collection<Edge>) getLineMergeGraphH4cked().getEdges();
 	}
 	
 	public int getSize_Edges() {
 		return getLineMergeGraphH4cked().getEdges().size();
 	}
 
 	public LineMergeGraphH4cked getLineMergeGraphH4cked() {
 		return lineMergeGraphH4cked;
 	}
 
 	public void setLineMergeGraphH4cked(LineMergeGraphH4cked lineMergeGraphH4cked) {
 		this.lineMergeGraphH4cked = lineMergeGraphH4cked;
 	}
 
 	public static void main(String[] args) {
 		PathSegmentGraph psg = new PathSegmentGraph();
 	}
 
 }
