 /**
  * GDMS-Topology is a library dedicated to graph analysis. It is based on the
  * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
  * and processing large graphs using spatial and alphanumeric indexes.
  *
  * This version is developed at French IRSTV Institute as part of the EvalPDU
  * project, funded by the French Agence Nationale de la Recherche (ANR) under
  * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
  * of Ecology and Sustainable Development.
  *
  * GDMS-Topology is distributed under GPL 3 license. It is produced by the
  * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
  * 2488.
  *
  * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
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
  * directly: info_at_orbisgis.org
  */
 package org.gdms.gdmstopology.function;
 
 import com.vividsolutions.jts.io.ParseException;
 import java.util.HashMap;
 import java.util.Map;
 import org.gdms.data.DataSource;
 import org.gdms.data.DataSourceCreationException;
 import org.gdms.data.NoSuchTableException;
 import org.gdms.data.schema.DefaultMetadata;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DriverException;
 import org.gdms.driver.memory.MemoryDataSetDriver;
 import org.gdms.gdmstopology.TopologySetupTest;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.gdms.sql.function.FunctionException;
 import org.javanetworkanalyzer.data.VBetw;
 import org.javanetworkanalyzer.model.Edge;
 import org.javanetworkanalyzer.model.KeyedGraph;
 import org.javanetworkanalyzer.model.UndirectedG;
 import org.junit.Test;
 import org.orbisgis.progress.NullProgressMonitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import static org.junit.Assert.*;
 
 /**
  * Tests {@link ST_ShortestPathLength} on directed weighted graphs.
  *
  * @author Adam Gouge
  */
 public class ST_ShortestPathLengthTest extends TopologySetupTest {
 
     private static final Logger LOGGER =
             LoggerFactory.getLogger(ST_ShortestPathLengthTest.class);
     private static final int[] EDGE_ORIENTATIONS =
             new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
     private static final double[] EDGE_WEIGHTS =
            new double[]{10.0, 1.0, 2.0, 2.0, 2.0, 4.0, 6.0, 5.0, 7.0};
     private static final double TOLERANCE = 0.0;
     private static final int numberOfNodes = 5;
 
     @Test
     public void testEdgeOrientationParameter() throws Exception {
 
         DataSet newEdges =
                 introduceOrientations(introduceWeights(prepareEdges(),
                                                        EDGE_WEIGHTS),
                                       EDGE_ORIENTATIONS);
 
         DataSet[] tables = new DataSet[]{newEdges};
 
         Map<Integer, Map<Integer, Double>> expected = expectedDirectedDistances();
 
 
         DataSet result = new ST_ShortestPathLength().evaluate(
                 dsf,
                 tables,
                 new Value[]{ValueFactory.createValue(2),
                             ValueFactory.createValue(5),
                             ValueFactory.createValue("reversed - "
                                                      + "edge_orientation"),
                             ValueFactory.createValue(GraphSchema.WEIGHT)},
                 new NullProgressMonitor());
 
         print(result);
 
     }
 
     @Test
    public void sourceTargetWeights() throws Exception {
 
         DataSet newEdges = introduceWeights(prepareEdges(), EDGE_WEIGHTS);
 
         DataSet[] tables = new DataSet[]{newEdges};
 
         Map<Integer, Map<Integer, Double>> expected = expectedDirectedDistances();
 
         for (int i = 1; i < numberOfNodes + 1; i++) {
             for (int j = 1; j < numberOfNodes + 1; j++) {
                 DataSet result = new ST_ShortestPathLength().evaluate(
                         dsf,
                         tables,
                         new Value[]{ValueFactory.createValue(i),
                                     ValueFactory.createValue(j),
                                     ValueFactory.createValue(GraphSchema.WEIGHT)},
                         new NullProgressMonitor());
                 // Check result.
 
                 assertTrue(result.getRowCount() == 1);
 
                 Metadata md = result.getMetadata();
                 int sourceIndex = md.getFieldIndex(ST_ShortestPathLength.SOURCE);
                 int destinationIndex = md.getFieldIndex(
                         ST_ShortestPathLength.DESTINATION);
                 int distanceIndex = md.getFieldIndex(
                         ST_ShortestPathLength.DISTANCE);
 
                 Value[] row = result.getRow(0);
                 int source = row[sourceIndex].getAsInt();
                 int destination = row[destinationIndex].getAsInt();
                 double distance = row[distanceIndex].getAsInt();
 
                 assertEquals(expected.get(source).get(destination),
                              distance,
                              TOLERANCE);
 
                 print(result);
             }
         }
     }
 
     @Test
    public void sourceWeights() throws Exception {
 
         DataSet newEdges = introduceWeights(prepareEdges(), EDGE_WEIGHTS);
 
         DataSet[] tables = new DataSet[]{newEdges};
 
         Map<Integer, Map<Integer, Double>> expected = expectedDirectedDistances();
 
         for (int i = 1; i < numberOfNodes + 1; i++) {
             DataSet result = new ST_ShortestPathLength().evaluate(
                     dsf,
                     tables,
                     new Value[]{ValueFactory.createValue(i),
                                 ValueFactory.createValue(GraphSchema.WEIGHT)},
                     new NullProgressMonitor());
             // Check result.
             print(result);
 
             Metadata md = result.getMetadata();
             int sourceIndex = md.getFieldIndex(ST_ShortestPathLength.SOURCE);
             int destinationIndex = md.getFieldIndex(
                     ST_ShortestPathLength.DESTINATION);
             int distanceIndex = md.getFieldIndex(ST_ShortestPathLength.DISTANCE);
             for (int j = 0; j < numberOfNodes; j++) {
                 Value[] row = result.getRow(j);
                 int source = row[sourceIndex].getAsInt();
                 int destination = row[destinationIndex].getAsInt();
                 double distance = row[distanceIndex].getAsInt();
                 assertEquals(expected.get(source).get(destination),
                              distance,
                              TOLERANCE);
             }
         }
     }
 
     @Test
    public void sourceTargetTableWeights() throws Exception {
 
         DataSet newEdges = introduceWeights(prepareEdges(), EDGE_WEIGHTS);
 
         MemoryDataSetDriver sourceDestTable = new MemoryDataSetDriver(
                 new String[]{ST_ShortestPathLength.SOURCE, ST_ShortestPathLength.DESTINATION},
                 new Type[]{TypeFactory.createType(Type.INT),
                            TypeFactory.createType(Type.INT)});
         // Add all possible combinations.
         for (int i = 1; i < numberOfNodes + 1; i++) {
             for (int j = 1; j < numberOfNodes + 1; j++) {
                 sourceDestTable.addValues(new Value[]{
                     ValueFactory.createValue(i),
                     ValueFactory.createValue(j)});
             }
         }
 
         DataSet[] tables = new DataSet[]{newEdges, sourceDestTable};
 
         DataSet result = new ST_ShortestPathLength().evaluate(
                 dsf,
                 tables,
                 new Value[]{ValueFactory.createValue(GraphSchema.WEIGHT)},
                 new NullProgressMonitor());
 
         print(result);
 
         Map<Integer, Map<Integer, Double>> expected = expectedDirectedDistances();
 
         Metadata md = result.getMetadata();
         int sourceIndex = md.getFieldIndex(ST_ShortestPathLength.SOURCE);
         int destinationIndex = md.getFieldIndex(
                 ST_ShortestPathLength.DESTINATION);
         int distanceIndex = md.getFieldIndex(ST_ShortestPathLength.DISTANCE);
 
         for (int i = 1; i < numberOfNodes + 1; i++) {
             for (int j = 0; j < numberOfNodes; j++) {
                 Value[] row = result.getRow(j);
                 int source = row[sourceIndex].getAsInt();
                 int destination = row[destinationIndex].getAsInt();
                 double distance = row[distanceIndex].getAsInt();
                 assertEquals(expected.get(source).get(destination),
                              distance,
                              TOLERANCE);
             }
         }
     }
 
     private DataSet prepareEdges() throws FunctionException, DriverException,
             DataSourceCreationException, NoSuchTableException, ParseException {
         MemoryDataSetDriver data = initializeDriver();
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(1 2, 2 3)")),
             ValueFactory.createValue(1)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(2 3, 4 3)")),
             ValueFactory.createValue(2)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(2 3, 2 1)")),
             ValueFactory.createValue(3)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(2 1, 4 1)")),
             ValueFactory.createValue(4)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(2 1, 2 3)")),
             ValueFactory.createValue(5)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(4 3, 4 1)")),
             ValueFactory.createValue(6)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(4 1, 4 3)")),
             ValueFactory.createValue(7)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(1 2, 2 1)")),
             ValueFactory.createValue(8)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(4 1, 1 2)")),
             ValueFactory.createValue(9)});
 
         DataSet[] tables = new DataSet[]{data};
 
         LOGGER.debug("\tDATA");
         print(data);
 
         // Evaluate ST_Graph.
         new ST_Graph().evaluate(dsf,
                                 tables,
                                 // Tolerance, orient by elevation, output
                                 new Value[]{ValueFactory.createValue(0),
                                             ValueFactory.createValue(false),
                                             ValueFactory.createValue("output")},
                                 new NullProgressMonitor());
 
         // Check the nodes table.
         DataSource nodes = dsf.getDataSource("output.nodes");
         nodes.open();
         LOGGER.debug("\tNODES");
         print(nodes);
         nodes.close();
 
         // Check the edges table.
         DataSource edges = dsf.getDataSource("output.edges");
         edges.open();
         LOGGER.debug("\tEDGES");
         print(edges);
 
         return edges;
     }
 
     /**
      * Sets up a driver with geometry and gid columns ready to receive input
      * data.
      *
      * @return A newly created driver
      */
     private MemoryDataSetDriver initializeDriver() {
         final MemoryDataSetDriver data =
                 new MemoryDataSetDriver(
                 new String[]{"the_geom", "gid"},
                 new Type[]{TypeFactory.createType(Type.GEOMETRY),
                            TypeFactory.createType(Type.INT)});
         return data;
     }
 
     /**
      * Prints the given table for debugging.
      *
      * @param table The table
      *
      * @throws DriverException
      */
     private void print(DataSet table) throws DriverException {
         String metadata = "";
         for (String name : table.getMetadata().getFieldNames()) {
             metadata += name + "\t";
         }
        LOGGER.debug(metadata);
         for (int i = 0; i < table.getRowCount(); i++) {
             String row = "";
             for (Value v : table.getRow(i)) {
                 row += v + "\t";
             }
            LOGGER.debug(row);
         }
     }
 
     /**
      * Prints all edges of the graph.
      *
      * @param graph The graph.
      */
     private void print(KeyedGraph<? extends VBetw, Edge> graph) {
         LOGGER.debug("\tGRAPH");
         String leftArrow;
         if (graph instanceof UndirectedG) {
             leftArrow = "<";
         } else {
             leftArrow = "";
         }
         for (Edge edge : graph.edgeSet()) {
             LOGGER.debug("{} {}--> {} ({})",
                          graph.getEdgeSource(edge).getID(),
                          leftArrow,
                          graph.getEdgeTarget(edge).getID(),
                          graph.getEdgeWeight(edge));
         }
     }
 
     private DataSet introduceOrientations(DataSet edges,
                                           int[] edgeOrientations)
             throws DriverException {
         DefaultMetadata newMetadata = new DefaultMetadata(edges.getMetadata());
         newMetadata.addField(GraphSchema.EDGE_ORIENTATION,
                              TypeFactory.createType(Type.INT));
         MemoryDataSetDriver newEdges =
                 new MemoryDataSetDriver(newMetadata);
         for (int i = 0; i < edges.getRowCount(); i++) {
             Value[] oldRow = edges.getRow(i);
             final Value[] newRow = new Value[newMetadata.getFieldCount()];
             System.arraycopy(oldRow, 0, newRow, 0,
                              newMetadata.getFieldCount() - 1);
             newRow[newMetadata.getFieldCount() - 1] =
                     ValueFactory.createValue(edgeOrientations[i]);
             newEdges.addValues(newRow);
         }
         LOGGER.debug("\tORIENTED EDGES");
         print(newEdges);
         return newEdges;
     }
 
     private DataSet introduceWeights(DataSet edges,
                                      double[] edgeWeights)
             throws DriverException {
         DefaultMetadata newMetadata = new DefaultMetadata(edges.getMetadata());
         newMetadata.addField(GraphSchema.WEIGHT,
                              TypeFactory.createType(Type.DOUBLE));
         MemoryDataSetDriver newEdges =
                 new MemoryDataSetDriver(newMetadata);
         for (int i = 0; i < edges.getRowCount(); i++) {
             Value[] oldRow = edges.getRow(i);
             final Value[] newRow = new Value[newMetadata.getFieldCount()];
             System.arraycopy(oldRow, 0, newRow, 0,
                              newMetadata.getFieldCount() - 1);
             newRow[newMetadata.getFieldCount() - 1] =
                     ValueFactory.createValue(edgeWeights[i]);
             newEdges.addValues(newRow);
         }
         LOGGER.debug("\tWEIGHTED EDGES");
         print(newEdges);
         return newEdges;
     }
 
     private Map<Integer, Map<Integer, Double>> expectedDirectedDistances() {
         Map<Integer, Map<Integer, Double>> distances =
                 new HashMap<Integer, Map<Integer, Double>>();
 
         Map<Integer, Double> dFromOne = new HashMap<Integer, Double>();
         dFromOne.put(1, 0.0);
        dFromOne.put(2, 7.0);
        dFromOne.put(3, 8.0);
         dFromOne.put(4, 5.0);
         dFromOne.put(5, 7.0);
 
         Map<Integer, Double> dFromTwo = new HashMap<Integer, Double>();
         dFromTwo.put(1, 11.0);
         dFromTwo.put(2, 0.0);
         dFromTwo.put(3, 1.0);
         dFromTwo.put(4, 2.0);
         dFromTwo.put(5, 4.0);
 
         Map<Integer, Double> dFromThree = new HashMap<Integer, Double>();
         dFromThree.put(1, 11.0);
        dFromThree.put(2, 18.0);
         dFromThree.put(3, 0.0);
         dFromThree.put(4, 16.0);
         dFromThree.put(5, 4.0);
 
         Map<Integer, Double> dFromFour = new HashMap<Integer, Double>();
         dFromFour.put(1, 9.0);
        dFromFour.put(2, 2.0);
        dFromFour.put(3, 3.0);
         dFromFour.put(4, 0.0);
         dFromFour.put(5, 2.0);
 
         Map<Integer, Double> dFromFive = new HashMap<Integer, Double>();
         dFromFive.put(1, 7.0);
        dFromFive.put(2, 14.0);
         dFromFive.put(3, 6.0);
         dFromFive.put(4, 12.0);
         dFromFive.put(5, 0.0);
 
         distances.put(1, dFromOne);
         distances.put(2, dFromTwo);
         distances.put(3, dFromThree);
         distances.put(4, dFromFour);
         distances.put(5, dFromFive);
 
         return distances;
     }
 }
