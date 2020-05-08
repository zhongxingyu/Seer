 /**
  * GDMS-Topology is a library dedicated to graph analysis. It is based on the
  * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
  * and processing large graphs using spatial and alphanumeric indexes.
  *
  * This version is developed at French IRSTV institut as part of the EvalPDU
  * project, funded by the French Agence Nationale de la Recherche (ANR) under
  * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministery
  * of Ecology and Sustainable Development.
  *
  * GDMS-Topology is distributed under GPL 3 license. It is produced by the
  * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
  * 2488.
  *
  * Copyright (C) 2009-2012 IRSTV (FR CNRS 2488)
  *
  * GDMS-Topology is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
  * directly: info_at_ orbisgis.org
  */
 package org.gdms.gdmstopology.process;
 
 import com.graphhopper.routing.DijkstraBidirectionRef;
 import com.graphhopper.routing.Path;
 import com.graphhopper.routing.util.CarFlagEncoder;
 import com.graphhopper.storage.Graph;
 import com.graphhopper.util.EdgeIterator;
 import com.graphhopper.util.GHUtility;
 import gnu.trove.iterator.TIntIterator;
 import gnu.trove.list.TIntList;
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.indexes.IndexException;
 import org.gdms.data.schema.DefaultMetadata;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DiskBufferDriver;
 import org.gdms.driver.DriverException;
 import org.gdms.gdmstopology.graphcreator.UnweightedGraphCreator;
 import org.gdms.gdmstopology.graphcreator.WeightedGraphCreator;
 import org.gdms.gdmstopology.model.GraphException;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.orbisgis.progress.ProgressMonitor;
 
 /**
  * Helper class to calculate a shortest path between two vertices of a
  * GraphHopper graph constructed from a {@link DataSet}.
  *
  * @author Adam Gouge
  */
 public class GraphPathCalculator {
 
     /**
      * Calculates a shortest path between two vertices of a weighted GraphHopper
      * graph constructed from the given {@link DataSet}.
      *
      * @param dsf              The {@link DataSourceFactory} used to parse the
      *                         data set.
      * @param dataSet          The input table.
      * @param source           The source vertex.
      * @param target           The target vertex.
      * @param graphType        The graph orientation.
      * @param weightColumnName The weight column name.
      * @param pm               Progress monitor.
      *
      * @return A {@link DiskBufferDriver} representing the edges in the shortest
      *         path.
      *
      * @throws DriverException
      * @throws GraphException
      */
     public static DiskBufferDriver calculateShortestPath(
             DataSourceFactory dsf,
             DataSet dataSet,
             int source,
             int target,
             int graphType,
             String weightColumnName,
             ProgressMonitor pm) throws DriverException,
             GraphException,
             IndexException {
 
         // Prepare the graph.
         Graph graph = new WeightedGraphCreator(
                 dataSet, graphType, weightColumnName).prepareGraph();
         System.out.println("Created weighted graph.");
 
         // Calculate the path,
         Path path = getPath(graph, source, target);
 
         // Return the result.
         return getShortestPathDriver(dsf, graph, path, pm);
     }
 
     /**
      * Calculates a shortest path between two vertices of a GraphHopper graph
      * constructed from the given {@link DataSet}, where the weight of each edge
      * is considered to be 1.
      *
      * @param dsf              The {@link DataSourceFactory} used to parse the
      *                         data set.
      * @param dataSet          The input table.
      * @param source           The source vertex.
      * @param target           The target vertex.
      * @param graphType        The graph orientation.
      * @param weightColumnName The weight column name.
      * @param pm               Progress monitor.
      *
      * @return A {@link DiskBufferDriver} representing the edges in the shortest
      *         path.
      *
      * @throws DriverException
      * @throws GraphException
      */
     public static DiskBufferDriver calculateShortestPathAllWeightsOne(
             DataSourceFactory dsf,
             DataSet dataSet,
             int source,
             int target,
             int graphType,
             ProgressMonitor pm) throws DriverException,
             GraphException,
             IndexException {
 
         // Prepare the graph.
         Graph graph = new UnweightedGraphCreator(dataSet, graphType)
                 .prepareGraph();
         System.out.println("Created graph with all weights one.");
 
         // Calculate the path,
         Path path = getPath(graph, source, target);
 
         // Return the result.
         return getShortestPathDriver(dsf, graph, path, pm);
     }
 
     /**
      * Returns a shortest path from the given source vertex to the given target
      * vertex.
      *
      * <p> <i>Note</i>: This uses a bidirectional Dijkstra search to calculate
      * the path.
      *
      * @param graph  The graph.
      * @param source The source vertex.
      * @param target The target vertex.
      *
      * @return A shortest path.
      */
     private static Path getPath(Graph graph, int source, int target) {
         DijkstraBidirectionRef algorithm = 
                 new DijkstraBidirectionRef(graph, new CarFlagEncoder());
         long start = System.currentTimeMillis();
         Path path = algorithm.calcPath(source, target);
         long stop = System.currentTimeMillis();
         System.out.println("Calculated path in " + (stop - start) + " ms.");
 //        System.out.println(path.toString());
         System.out.println(path.toDetailsString());
         return path;
     }
 
     /**
      * Returns the given shortest path as a {@link DiskBufferDriver}.
      *
      * @param dsf   The {@link DataSourceFactory} used to parse the data set.
      * @param graph The graph.
      * @param path  The shortest path.
      * @param pm    Progress monitor.
      *
      * @return A {@link DiskBufferDriver} representation of the edges in the
      *         shortest path.
      *
      * @throws DriverException
      */
     private static DiskBufferDriver getShortestPathDriver(
             DataSourceFactory dsf,
             Graph graph,
             Path path,
             ProgressMonitor pm) throws DriverException {
 
         // Create the metadata for the new table that will hold the 
         // shortest path.
         Metadata pathMetadata = createShortestPathMetadata();
 
         // Create a DiskBufferDriver to store the centrality indices.
         DiskBufferDriver shortestPathDriver =
                 new DiskBufferDriver(
                 dsf,
                 pathMetadata);
 
         // Store the path in the DiskBufferDriver.
         storePath(path, graph, shortestPathDriver);
 
         // Clean up.
         cleanUp(shortestPathDriver, pm);
 
         // Print the path.
         printDiskBufferDriver(shortestPathDriver);
 
         // Return the (closed) DiskBufferDriver.
         return shortestPathDriver;
     }
 
     /**
      * Creates the metadata model used to store the shortest path in a
      * {@link DiskBufferDriver}.
      *
      * @return The metadata model used to store the shortest path in a
      *         {@link DiskBufferDriver}.
      */
    public static Metadata createShortestPathMetadata() {
         Metadata md = new DefaultMetadata(
                 new Type[]{
                     //                    TypeFactory.createType(
                     //                    Type.GEOMETRY,
                     //                    new Constraint[]{
                     //                        new GeometryDimensionConstraint(
                     //                        GeometryDimensionConstraint.DIMENSION_CURVE)
                     //                    }),
                     //                    TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.DOUBLE)
                 },
                 new String[]{
                     //                    "the_geom",
                     //                    GraphSchema.ID,
                     GraphSchema.PATH_ID,
                     GraphSchema.START_NODE,
                     GraphSchema.END_NODE,
                     GraphSchema.WEIGHT
                 });
         return md;
     }
 
     /**
      * Stores the given path in a {@link DiskBufferDriver}.
      *
      * @param graph  The graph.
      * @param path   The shortest path.
      * @param driver The {@link DiskBufferDriver} in which to store the given
      *               shortest path.
      *
      * @throws DriverException
      */
     private static void storePath(
             Path path,
             Graph graph,
             DiskBufferDriver driver) throws DriverException {
 //                // Recover the edge Metadata.
 //                // TODO: Add a check to make sure the metadata was loaded correctly.
 //                Metadata edgeMetadata = dataSet.getMetadata();
 //                // Get the index of the geometry
 //                int geometryIndex = dataSet.getSpatialFieldIndex();
 //                int edgeID = edgeMetadata.getFieldIndex(GraphSchema.ID);
 //        //        int weight = edgeMetadata.getFieldIndex(GraphSchema.WEIGHT);
 
 //                int current;
 //                try {
 //                    current = path.node(0);
 //                } catch (ArrayIndexOutOfBoundsException e) {
 //                    System.out.println("There is no node(0)!");
 //                    current = 0;
 //                }
 
         long start = System.currentTimeMillis();
 
         TIntList nodeList = path.calcNodes();
         TIntIterator nodeListIt = nodeList.iterator();
         if (!nodeListIt.hasNext()) {
             System.out.println("The path is empty!!");
         }
         int current = nodeListIt.next();
         int next;
         int pathEdgeID = 1;
         while (nodeListIt.hasNext()) {
             next = nodeListIt.next();
             // Get the smallest edge distance.
             double edgeLength =
                     getSmallestEdgeDistance(graph, current, next);
             // Store this path edge.
             System.out.print("Adding " + current + " -> " + next
                     + ", weight: " + edgeLength + " ... ");
             driver.addValues(
                     new Value[]{
                         // the_geom
                         //                        ValueFactory.createNullValue(),
                         //                        // The row ID of the edge in the data source.
                         //                        ValueFactory.createValue(currentEdge.getRowId()),
                         // An id for this edge in the shortest path.
                         ValueFactory.createValue(pathEdgeID),
                         // Start node
                         ValueFactory.createValue(current),
                         // End node
                         ValueFactory.createValue(next),
                         // Weight
                         ValueFactory.createValue(edgeLength)
                     });
             System.out.println("DONE!");
             current = next;
             pathEdgeID++;
         }
 
         long stop = System.currentTimeMillis();
         System.out.println("Stored path in " + (stop - start) + " ms.");
 
     }
 
     /**
      * Returns the smallest edge distance from the given source vertex to the
      * given target vertex.
      *
      * @param graph  The graph.
      * @param source The source vertex.
      * @param target The target vertex.
      *
      * @return The smallest edge distance from the given source vertex to the
      *         given target vertex.
      */
     private static double getSmallestEdgeDistance(
             Graph graph,
             int source,
             int target) {
         // Obtain the smallest weight for this edge.
         EdgeIterator outgoingEdges = GHUtility.getCarOutgoing(graph, source);
         double smallestDistance = Double.MAX_VALUE;
         while (outgoingEdges.next()) {
             if (outgoingEdges.node() == target) {
                 if (outgoingEdges.distance() < smallestDistance) {
                     smallestDistance = outgoingEdges.distance();
                 }
             }
         }
         return smallestDistance;
     }
 
     /**
      * Frees resources.
      *
      * @param shortestPathDriver The {@link DiskBufferDriver} in which the
      *                           shortest path has been stored.
      * @param pm                 Progress monitor.
      *
      * @throws DriverException
      */
     private static void cleanUp(
             DiskBufferDriver shortestPathDriver,
             ProgressMonitor pm) throws DriverException {
         // We are done writing.
         shortestPathDriver.writingFinished();
         // The task is done.
         pm.endTask();
         // So close the DiskBufferDriver.
         shortestPathDriver.close();
     }
 
     /**
      * Print out the contents of the given DiskBufferDriver.
      *
      * @param driver The given DiskBufferDriver.
      *
      * @throws DriverException
      */
     private static void printDiskBufferDriver(DiskBufferDriver driver)
             throws DriverException {
         long start = System.currentTimeMillis();
         if (driver.isOpen()) {
             System.out.println("Driver is open.");
         } else {
             System.out.println("Driver is closed.");
             System.out.print("Opening the DiskBufferDriver ...");
             driver.open();
             System.out.println("DiskBufferDriver opened.");
         }
         final long rowCount = driver.getRowCount();
         System.out.println("Printing " + rowCount + " rows.");
         for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
             Value[] row = driver.getRow(rowIndex);
             for (int i = 0; i < row.length; i++) {
                 String value = row[i].toString();
                 System.out.print(value + "; ");
             }
             System.out.println();
         }
         long stop = System.currentTimeMillis();
         System.out.println("Finished printing in " + (stop - start) + " ms.");
         driver.close();
     }
 }
