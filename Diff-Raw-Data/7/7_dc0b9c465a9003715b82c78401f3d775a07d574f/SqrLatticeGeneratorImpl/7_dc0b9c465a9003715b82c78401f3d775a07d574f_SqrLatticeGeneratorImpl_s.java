 package org.eclipse.stem.definitions.lattice.impl;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.stem.core.STEMURI;
 import org.eclipse.stem.core.Utility;
 import org.eclipse.stem.core.common.DublinCore;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.GraphFactory;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.impl.EdgeImpl;
 import org.eclipse.stem.core.graph.impl.NodeImpl;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.adapters.spatial.geo.InlineLatLongDataProvider;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong.SegmentBuilder;
 import org.eclipse.stem.definitions.labels.AreaLabel;
 import org.eclipse.stem.definitions.labels.LabelsFactory;
 import org.eclipse.stem.definitions.labels.PopulationLabel;
 import org.eclipse.stem.definitions.labels.RelativePhysicalRelationshipLabel;
 import org.eclipse.stem.definitions.labels.TransportMode;
 import org.eclipse.stem.definitions.labels.impl.CommonBorderRelationshipLabelImpl;
 import org.eclipse.stem.definitions.labels.impl.TransportRelationshipLabelImpl;
 import org.eclipse.stem.definitions.lattice.GraphLatticeGeneratorInterface;
 import org.eclipse.stem.definitions.nodes.NodesFactory;
 import org.eclipse.stem.definitions.nodes.Region;
 
 
 
 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 
 public class SqrLatticeGeneratorImpl extends GraphLatticeGenerator {
 	
 	private static final String URI_SQR_PREFIX = URI_PREFIX+"SQR_";
 	
 	/** 
 	 * must specify the lattice type
 	 */
 	public static final String LATTICE_TYPE=SQR_LATTICE_TYPE;
 	
 	/**
 	 * The number of kilometers (or arb unit) of common border that two nodes share.
 	 * Always 1 for a Square lattice
 	 */
 	public static final int COMMON_BORDER_LENGTH = 1;
 	
 	/**
 	 * Square areas = 1.0
 	 */
 	public static final double SQUARE_AREA = 1.0;
 	
 	/**
 	 * 
 	 */
 	public static final String DEFAULT_POPULATION_NAME="human";
 	
 	/**
 	 * 
 	 */
 	public static String POPULATION_NAME=DEFAULT_POPULATION_NAME;
 	
 	/**
 	 * 
 	 */
 	public static final double DEFAULT_POPULATION_COUNT = 1.0;
 	
 	/**
 	 * return a square lattice of specified size as a graph
 	 */
 	public Graph getGraph(int xSize, int ySize, boolean addNearestNeighbors, boolean addNextNearestNeighbors, boolean periodicBoundaries) {
 		final Graph graph = GraphFactory.eINSTANCE.createGraph();
 		final DublinCore dc = graph.getDublinCore();
 		dc.populate();
 		dc.setTitle(LATTICE_TYPE);
 		dc.setSource(this.getClass().getSimpleName());
 		Calendar c = Calendar.getInstance();
         SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
         String valid = formatter.format(c.getTime());
        	dc.setValid(valid);
 						
 			final Node nodeHolder[][] = new Node[xSize][ySize];
 
 			// Create the nodes and put them into the graph
 			for (int x = 0; x < xSize; x++) {
 				for (int y = 0; y < ySize; y++) {
 					final Region regionNode = createRegionNode(URI_SQR_PREFIX, x, y, graph);
 					
 					// on a square lattice all nodes have area=1.0
 					final AreaLabel areaLabel = LabelsFactory.eINSTANCE.createAreaLabel();
 					areaLabel.getCurrentAreaValue().setArea(SQUARE_AREA);
 					regionNode.getLabels().add(areaLabel);
 					
 					// add a default population
 // SED commented out					final PopulationLabel pop = createPopulationLabel(POPULATION_NAME, DEFAULT_POPULATION_COUNT, regionNode );
 					
 					nodeHolder[x][y] = regionNode;
 					graph.putNode(regionNode);
 				} // for each y
 			} // for each x
 
 			// If we are including Nearest Neighbors (NN)
 			if(addNearestNeighbors) {
 				//Add the edges
 				for (int x = 0; x < xSize; x++) {
 					for (int y = 0; y < ySize; y++) {
 						if((y-1)>=0) {
 							createEdge(graph,nodeHolder[x][y - 1],nodeHolder[x][y], COMMON_BORDER_LENGTH);
 						} else {
 							if(periodicBoundaries) {
 								createEdge(graph,nodeHolder[x][ySize - 1],nodeHolder[x][y], COMMON_BORDER_LENGTH);
 							}
 						}
 						
 						if((x-1)>=0) {
 							createEdge(graph,nodeHolder[x - 1][y],nodeHolder[x][y], COMMON_BORDER_LENGTH);
 						}else {
 							if(periodicBoundaries) {
 								createEdge(graph,nodeHolder[xSize - 1][y],nodeHolder[x][y], COMMON_BORDER_LENGTH);
 							}
 						}
 						
 					} // for each y
 				} // for each x
 			}// NN edges
 
 			// If we are including NEXT Nearest Neighbors (NNN)
 			if(addNextNearestNeighbors) {
 				for (int x = 0; x < xSize; x++) {
 					for (int y = 0; y < ySize; y++) {
 						// uppper right?
 						if( ((x+1)<xSize) && ((y+1)<ySize) ) {
 							createEdge(graph,nodeHolder[x][y],nodeHolder[x+1][y+1], COMMON_BORDER_LENGTH);
 						} else {
 								if(periodicBoundaries) {
 									int x2 = x+1;
 									int y2 = y+1;
 									if(x2>=xSize) x2=0;
 									if(y2>=ySize) y2=0;
 									createEdge(graph,nodeHolder[x][y],nodeHolder[x2][y2], COMMON_BORDER_LENGTH);
 								}
 						}// upper right
 							
 						// lower right
 						if( ((x+1)<xSize) && ((y-1)>=0) ) {
 							createEdge(graph,nodeHolder[x][y],nodeHolder[x+1][y-1], COMMON_BORDER_LENGTH);
 						} else {
 							if(periodicBoundaries) {
 								int x2 = x+1;
 								int y2 = y-1;
 								if(x2>=xSize) x2=0;
 								if(y2 <= -1) y2=ySize-1;
 								createEdge(graph,nodeHolder[x][y],nodeHolder[x2][y2], COMMON_BORDER_LENGTH);
 							}
 						}
 						
 					} // for each y
 				} // for each x
 				
 			}// NNN edges
 
 			// Add the spatial polygons to the spatial attribute of each node's
 			// dublin core instance and for each edge. The value for the nodes will
 			// be a rectangular area expressed as a set of lat/long points that
 			// layout a polygon. The position of the polygon will at position that
 			// matches the position (x, y) of the node in the lattice.
 			addSpatialSpecifications(nodeHolder, xSize, ySize);
 
 			assert graph.sane();
 
 			return graph;
 		}
 		
 	
 	
 	
 	
 	
 	
 		
 
 	/**
 	 * Add the spatial specification to the spatial attribute of each node's
 	 * dublin core instance. The value will be a retangular area expressed as a
 	 * set of lat/long points that layout a polygon. The position of the polygon
 	 * will at position that matches the position (x, y) of the node in
 	 * the lattice. Node at position [0][0] will be in the upper left while the
 	 * node at positon [numxs][numys] will be in the lower right of the
 	 * rectangular area.
 	 * 
 	 * @param nodeHolder
 	 *            the array that holds the nodes of the lattic
 	 */
 	private static void addSpatialSpecifications(final Node[][] nodeHolder, int xSize, int ySize) {
 
 		// First run through all of the nodes creating a polygon for their
 		// border and adding the data as an inline value in the spatial
 		// attribute of the node's dublin core.
 		for (int x = 0; x < xSize; x++) {
 			for (int y = 0; y < ySize; y++) {
 				final Node node = nodeHolder[x][y];
 				final LatLong nodeSegments = createNodePolygon(x,y);
 				final String spatialURIString = InlineLatLongDataProvider
 						.createSpatialInlineURIString(nodeSegments);
 				node.getDublinCore().setSpatial(spatialURIString);
 			} // for each y
 		} // for each x
 	} // addSpatialSpecifications
 	
 	
 	
 
 
 	
 	/**
 	 * 
 	 * @param x0
 	 * @param y0
 	 * @param x1
 	 * @param y1
 	 * @return
 	 */
 	private static LatLong createNodePolygon(final double x, double y) {
 		final LatLong retValue = new LatLong();
 		
 		final SegmentBuilder sb = new SegmentBuilder();
 		// We just make a square...
 		sb.add(x-0.5, y-0.5);
 		sb.add(x-0.5, y+0.5);
 		sb.add(x+0.5, y+0.5);
 		sb.add(x+0.5, y-0.5);
 		sb.add(x-0.5, y-0.5);
 		retValue.add(sb.toSegment());
 
 		return retValue;
 	} // createNodePolygon
 	
 	
 	
 	
 	/**
 	 * For testing
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SqrLatticeGeneratorImpl slgi = new SqrLatticeGeneratorImpl();
 		Graph g = slgi.getGraph(10, 10, true, false, true);
		System.out.println("graph built ..now save it");
 		
 		String graphUriString = "platform:/resource/play/graphs/sqrLatticeGraph.graph";
 		g.setURI(URI.createURI(graphUriString));
 		URI outputURI = URI.createFileURI("/Users/jhkauf/Documents/runtime-stemMacOS.product/play/graphs/sqrLatticeGraph.graph");
 		
 		try {
 			Utility.serializeIdentifiable(g, outputURI);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		
		System.out.println("done");
 
 	}
 
 
 	
 }// SqrLatticeGeneratorImpl
