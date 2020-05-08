 /**
  * GDMS-Topology  is a library dedicated to graph analysis. It's based on the JGraphT available at
  * http://www.jgrapht.org/. It ables to compute and process large graphs using spatial
  * and alphanumeric indexes.
  * This version is developed at French IRSTV institut as part of the
  * EvalPDU project, funded by the French Agence Nationale de la Recherche
  * (ANR) under contract ANR-08-VILL-0005-01 and GEBD project
  * funded by the French Ministery of Ecology and Sustainable Development .
  *
  * GDMS-Topology  is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
  *
  * GDMS-Topology is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult: <http://trac.orbisgis.org/>
  * or contact directly:
  * info_at_ orbisgis.org
  */
 package org.gdms.gdmstopology.process;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import org.gdms.data.NoSuchTableException;
 import org.gdms.data.SQLDataSourceFactory;
 import org.gdms.data.indexes.DefaultAlphaQuery;
 import org.gdms.data.indexes.IndexException;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DiskBufferDriver;
 import org.gdms.driver.DriverException;
 import org.gdms.gdmstopology.model.DWMultigraphDataSource;
 import org.gdms.gdmstopology.model.EdgeReversedGraphDataSource;
 import org.gdms.gdmstopology.model.GDMSValueGraph;
 import org.gdms.gdmstopology.model.GraphEdge;
 import org.gdms.gdmstopology.model.GraphException;
 import org.gdms.gdmstopology.model.GraphMetadataFactory;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.gdms.gdmstopology.model.WMultigraphDataSource;
 import org.jgrapht.Graphs;
 import org.jgrapht.alg.DijkstraShortestPath;
 import org.jgrapht.traverse.ClosestFirstIterator;
 import org.orbisgis.progress.NullProgressMonitor;
 import org.orbisgis.progress.ProgressMonitor;
 
 /**
  *
  * @author ebocher
  */
 public class GraphAnalysis {
 
         //Constants used in the functions
         public static int DIRECT = 1;
         public static int DIRECT_REVERSED = 2;
         public static int UNDIRECT = 3;
         private static int ID_FIELD_INDEX = -1;
         private static int SOURCE_FIELD_INDEX = -1;
         private static int TARGET_FIELD_INDEX = -1;
 
         /**
          * Some usefull methods to analyse a graph network.
          */
         private GraphAnalysis() {
         }
 
         /**
          * Find the shortest path between two nodes using
          * the Dijkstra algorithm.
          * It returns a list of @GraphEdge
          * @param dsf
          * @param graph
          * @param startNode
          * @param endNode
          * @return 
          */
         public static List<GraphEdge> findPathBetween(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 Integer startNode, Integer endNode) {
                 return DijkstraShortestPath.findPathBetween(graph, startNode, endNode);
         }
 
         /**
          * Find the shortest path between two nodes using
          * the Dijkstra algorithm.
          * The list of @GraphEdge is stored in a datasource.
          * @param dsf
          * @param graph
          * @param startNode
          * @param endNode
          * @return 
          */
         public static DiskBufferDriver findPathBetween2Nodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 Integer sourceVertex, Integer targetVertex, ProgressMonitor pm) throws GraphException, DriverException {
                 return findPathBetween2Nodes(dsf, graph, sourceVertex, targetVertex, Double.POSITIVE_INFINITY, pm);
         }
 
         /**
          * Find the shortest path between two nodes using
          * the Dijkstra algorithm.
          * A radius can be used to constraint the analysis of the graph.
          * The list of @GraphEdge is stored in a datasource.
          * For each path the method returns all geometries of the input graph datasource.
          * @param dsf
          * @param graph
          * @param startNode
          * @param endNode
          * @param radius
          * @return 
          */
         public static DiskBufferDriver findPathBetween2Nodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 Integer sourceVertex, Integer targetVertex, double radius, ProgressMonitor pm) throws GraphException, DriverException {
 
                 if (!graph.containsVertex(targetVertex)) {
                         throw new GraphException(
                                 "The graph must contain the target vertex");
                 }
                 ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(graph, sourceVertex, radius);
                 DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, GraphMetadataFactory.createEdgeMetadataGraph());
                 int count = 0;
                 pm.startTask("Find shortest path", 100);
                 while (cl.hasNext()) {
                         if (count >= 100 && count % 100 == 0) {
                                 if (pm.isCancelled()) {
                                         break;
                                 }
                         }
                         count++;
                         int vertex = cl.next();
                         if (vertex == targetVertex) {
                                 int v = targetVertex;
                                 int k = 0;
                                 while (true) {
                                         GraphEdge edge = cl.getSpanningTreeEdge(v);
                                         if (edge == null) {
                                                 break;
                                         }
                                         diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(graph.getGeometry(edge)),
                                                         ValueFactory.createValue(edge.getRowId()),
                                                         ValueFactory.createValue(k),
                                                         ValueFactory.createValue(edge.getSource()),
                                                         ValueFactory.createValue(edge.getTarget()),
                                                         ValueFactory.createValue(edge.getWeight())});
                                         k++;
                                         v = Graphs.getOppositeVertex(graph, edge, v);
                                 }
                                 break;
                         }
                 }
                 diskBufferDriver.writingFinished();
                 pm.endTask();
                 diskBufferDriver.stop();
 
                 return diskBufferDriver;
 
         }
 
         /**
          * Compute all shortest distancs between one node.
          * The distances are stored in a datasource.
          * @param dsf
          * @param graph
          * @param startNode
          * @param endNode
          * @return 
          */
         public static DiskBufferDriver computeDistancesBetweenOneNode(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 Integer sourceVertex, ProgressMonitor pm) throws GraphException, DriverException {
                 return computeDistancesBetweenOneNode(dsf, graph, sourceVertex, Double.POSITIVE_INFINITY, pm);
         }
 
         /**
          * Compute all shortest distancs between one node.
          * A radius can be used to constraint the analysis of the graph.
          * The distances are stored in a datasource.
          * @param dsf
          * @param graph
          * @param startNode
          * @param endNode
          * @param radius
          * @return 
          */
         public static DiskBufferDriver computeDistancesBetweenOneNode(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 Integer sourceVertex, double radius, ProgressMonitor pm) throws GraphException, DriverException {
 
                 if (!graph.containsVertex(sourceVertex)) {
                         throw new GraphException(
                                 "The graph must contain the target vertex");
                 }
 
                 DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, GraphMetadataFactory.createDistancesMetadataGraph());
 
                 ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(graph, sourceVertex, radius);
 
                 int count = 0;
                 pm.startTask("Calculate distances path", 100);
                 int k = 0;
                 while (cl.hasNext()) {
                         if (count >= 100 && count % 100 == 0) {
                                 if (pm.isCancelled()) {
                                         break;
                                 }
                         }
                         count++;
                         Integer node = cl.next();
                         if (node != sourceVertex) {
                                 double length = cl.getShortestPathLength(node);
                                 diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(k),
                                                 ValueFactory.createValue(sourceVertex), ValueFactory.createValue(node),
                                                 ValueFactory.createValue(length)});
                                 k++;
                         }
                 }
                 diskBufferDriver.writingFinished();
                 pm.endTask();
                 diskBufferDriver.stop();
 
                 return diskBufferDriver;
 
         }
 
         /**
          * Find the shortest path between sereval nodes using
          * the Dijkstra algorithm.
          * A radius can be used to constraint the analysis of the graph.
          * A datasource that contains all destinations must be used following the schema :
          * id :: int, source::int, target::int.
          * The algorithm iters all paths (source, target) and returns the results in a datasource.
          * Duplicate paths (ie rows with the same source and target values) are computed once again.
          * @param dsf
          * @param graph
          * @param nodes
          * @param radius
          * @return
          * @throws GraphException
          * @throws DriverException 
          */
         public static DiskBufferDriver findPathBetweenSeveralNodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 DataSet nodes, ProgressMonitor pm) throws GraphException, DriverException {
                 return findPathBetweenSeveralNodes(dsf, graph, nodes, Double.POSITIVE_INFINITY, pm);
         }
 
         /**
          * Find the shortest path between sereval nodes using
          * the Dijkstra algorithm.
          * A datasource that contains all destinations must be used following the schema :
          * id :: int, source::int, target::int.
          * The algorithm iters all paths (source, target) and returns the results in a datasource.
          * Duplicate paths (ie rows with the same source and target values) are computed once again.
          * @param dsf
          * @param graph
          * @param nodes
          * @return
          * @throws GraphException
          * @throws DriverException 
          */
         public static DiskBufferDriver findPathBetweenSeveralNodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 DataSet nodes, double radius, ProgressMonitor pm) throws GraphException, DriverException {
                 initIndex(dsf, nodes, pm);
                 DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, GraphMetadataFactory.createEdgeMetadataGraph());
 
                 Iterator<Value[]> it = nodes.iterator();
                 ClosestFirstIterator<Integer, GraphEdge> cl = null;
                 HashSet<Integer> visitedSources = new HashSet<Integer>();
                 while (it.hasNext()) {
                         Value[] values = it.next();
                         int source = values[SOURCE_FIELD_INDEX].getAsInt();
                         HashMap<Integer, Integer> targets = null;
                         if (!visitedSources.contains(source)) {
                                 cl = new ClosestFirstIterator<Integer, GraphEdge>(graph, source);
                                 targets = getTargets(dsf, nodes, source);
                                 int targetsNumber = targets.size();
                                 int targetVisisted = 0;
                                 boolean isAllTargetsDone = false;
                                 while (cl.hasNext() && (!isAllTargetsDone)) {
                                         int vertex = cl.next();
                                         if (targets.containsKey(vertex)) {
                                                 targetVisisted++;
                                                 isAllTargetsDone = targetsNumber - targetVisisted == 0;
                                                 int v = vertex;
                                                 int idNodes = targets.get(vertex);
                                                 int k = 0;
                                                 while (true) {
                                                         GraphEdge edge = cl.getSpanningTreeEdge(v);
                                                         if (edge == null) {
                                                                 break;
                                                         }
                                                         diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(graph.getGeometry(edge)),
                                                                         ValueFactory.createValue(idNodes),
                                                                         ValueFactory.createValue(k),
                                                                         ValueFactory.createValue(edge.getSource()),
                                                                         ValueFactory.createValue(edge.getTarget()),
                                                                         ValueFactory.createValue(edge.getWeight())});
                                                         k++;
                                                         v = Graphs.getOppositeVertex(graph, edge, v);
                                                 }
                                         }
                                 }
                                 visitedSources.add(source);
                         }
 
                 }
                 diskBufferDriver.writingFinished();
                 diskBufferDriver.stop();
                 return diskBufferDriver;
         }
 
         /**
          * 
          * @param dsf
          * @param graph
          * @param nodes
          * @param pm
          * @return
          * @throws GraphException
          * @throws DriverException 
          */
         public static DiskBufferDriver computeDistanceBetweenSeveralNodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 DataSet nodes, ProgressMonitor pm) throws GraphException, DriverException {
                 return computeDistanceBetweenSeveralNodes(dsf, graph, nodes, Double.POSITIVE_INFINITY, pm);
 
         }
 
         /**
          * 
          * @param dsf
          * @param graph
          * @param nodes
          * @param radius
          * @param pm
          * @return
          * @throws GraphException
          * @throws DriverException 
          */
         public static DiskBufferDriver computeDistanceBetweenSeveralNodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                 DataSet nodes, double radius, ProgressMonitor pm) throws GraphException, DriverException {
                 initIndex(dsf, nodes, new NullProgressMonitor());
                 DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, GraphMetadataFactory.createDistancesMetadataGraph());
 
                 Iterator<Value[]> it = nodes.iterator();
                 ClosestFirstIterator<Integer, GraphEdge> cl = null;
                 HashSet<Integer> visitedSources = new HashSet<Integer>();
                 while (it.hasNext()) {
                         Value[] values = it.next();
                         int source = values[SOURCE_FIELD_INDEX].getAsInt();
                         HashMap<Integer, Integer> targets = null;
                         if (!visitedSources.contains(source)) {
                                 cl = new ClosestFirstIterator<Integer, GraphEdge>(graph, source);
                                 targets = getTargets(dsf, nodes, source);
                                 int targetsNumber = targets.size();
                                 int targetVisisted = 0;
                                 boolean isAllTargetsDone = false;
                                 while (cl.hasNext() && (!isAllTargetsDone)) {
                                         int vertex = cl.next();
                                         if (targets.containsKey(vertex)) {
                                                 targetVisisted++;
                                                 isAllTargetsDone = targetsNumber - targetVisisted == 0;
                                                 int v = vertex;
                                                 int idNodes = targets.get(vertex);
                                                 int k = 0;
                                                 double sum = 0;
                                                 while (true) {
                                                         GraphEdge edge = cl.getSpanningTreeEdge(v);
                                                         if (edge == null) {
                                                                 break;
                                                         }
                                                         sum += edge.getWeight();
                                                         k++;
                                                         v = Graphs.getOppositeVertex(graph, edge, v);
                                                 }
                                                 diskBufferDriver.addValues(new Value[]{
                                                                 ValueFactory.createValue(idNodes),
                                                                 ValueFactory.createValue(source),
                                                                 ValueFactory.createValue(vertex),
                                                                 ValueFactory.createValue(sum)});
 
                                         }
 
                                 }
                                 visitedSources.add(source);
                         }
 
                 }
                 diskBufferDriver.writingFinished();
                 diskBufferDriver.stop();
                 return diskBufferDriver;
         }
 
         /**
          * Return as set of geometries that represent the shortest path between two nodes. 
          * @param dsf
          * @param dataSet
          * @param source
          * @param target
          * @param costField
          * @param graphType
          * @param pm
          * @return
          * @throws GraphException
          * @throws DriverException 
          */
         public static DiskBufferDriver getShortestPath(SQLDataSourceFactory dsf, DataSet dataSet, int source, int target, String costField, int graphType, ProgressMonitor pm) throws GraphException, DriverException {
                 if (graphType == DIRECT) {
                         DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                         dwMultigraphDataSource.setWeigthFieldIndex(costField);
                         return findPathBetween2Nodes(dsf, dwMultigraphDataSource, source, target, pm);
                 } else if (graphType == DIRECT_REVERSED) {
                         DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                         dwMultigraphDataSource.setWeigthFieldIndex(costField);
                         EdgeReversedGraphDataSource edgeReversedGraph = new EdgeReversedGraphDataSource(dwMultigraphDataSource);
                         return findPathBetween2Nodes(dsf, edgeReversedGraph, source, target, pm);
                 } else if (graphType == UNDIRECT) {
                         WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
                         wMultigraphDataSource.setWeigthFieldIndex(costField);
                         return findPathBetween2Nodes(dsf, wMultigraphDataSource, source, target, pm);
                 } else {
                         throw new GraphException("Only 3 type of graphs are allowed."
                                 + "1 if the path is computing using a directed graph.\n"
                                 + "2 if the path is computing using a directed graph and edges are reversed\n"
                                 + "3 if the path is computing using a undirected.");
                 }
         }
 
         /**
          * Return all shortest paths from a set of start and target nodes.
          * The dataset that contains all nodes must following the schema : 
          * id (int or long), source (int or long) ,target(int or long)
          * @param dsf
          * @param dataSet
          * @param nodes
          * @param costField
          * @param graphType
          * @param pm
          * @return
          * @throws GraphException
          * @throws DriverException 
          */
         public static DiskBufferDriver getMShortestPath(SQLDataSourceFactory dsf, DataSet dataSet, DataSet nodes, String costField, int graphType, ProgressMonitor pm) throws GraphException, DriverException {
                 if (checkMetadata(nodes)) {
                         if (graphType == DIRECT) {
                                 DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                                 dwMultigraphDataSource.setWeigthFieldIndex(costField);
                                 return findPathBetweenSeveralNodes(dsf, dwMultigraphDataSource, nodes, pm);
                         } else if (graphType == DIRECT_REVERSED) {
                                 DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                                 dwMultigraphDataSource.setWeigthFieldIndex(costField);
                                 EdgeReversedGraphDataSource edgeReversedGraph = new EdgeReversedGraphDataSource(dwMultigraphDataSource);
                                 return findPathBetweenSeveralNodes(dsf, edgeReversedGraph, nodes, pm);
                         } else if (graphType == UNDIRECT) {
                                 WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
                                 wMultigraphDataSource.setWeigthFieldIndex(costField);
                                 return findPathBetweenSeveralNodes(dsf, wMultigraphDataSource, nodes, pm);
                         } else {
                                 throw new GraphException("Only 3 type of graphs are allowed."
                                         + "1 if the path is computing using a directed graph.\n"
                                         + "2 if the path is computing using a directed graph and edges are reversed\n"
                                         + "3 if the path is computing using a undirected.");
                         }
                 } else {
                         throw new GraphException("The table nodes must contains the field id, source and target");
                 }
         }
 
         /**
          * Compute the shortest path distance between two nodes.
          * @param dsf
          * @param dataSet
          * @param source
          * @param target
          * @param costField
          * @param graphType
          * @param pm
          * @return
          * @throws GraphException
          * @throws DriverException 
          */
         public static DiskBufferDriver getShortestPathLength(SQLDataSourceFactory dsf, DataSet dataSet, int source, String costField, int graphType, ProgressMonitor pm) throws GraphException, DriverException {
                 if (graphType == DIRECT) {
                         DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                         dwMultigraphDataSource.setWeigthFieldIndex(costField);
                         return computeDistancesBetweenOneNode(dsf, dwMultigraphDataSource, source, pm);
                 } else if (graphType == DIRECT_REVERSED) {
                         DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                         dwMultigraphDataSource.setWeigthFieldIndex(costField);
                         EdgeReversedGraphDataSource edgeReversedGraph = new EdgeReversedGraphDataSource(dwMultigraphDataSource);
                         return computeDistancesBetweenOneNode(dsf, edgeReversedGraph, source, pm);
                 } else if (graphType == UNDIRECT) {
                         WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
                         wMultigraphDataSource.setWeigthFieldIndex(costField);
                         return computeDistancesBetweenOneNode(dsf, wMultigraphDataSource, source, pm);
                 } else {
                         throw new GraphException("Only 3 type of graphs are allowed."
                                 + "1 if the path is computing using a directed graph.\n"
                                 + "2 if the path is computing using a directed graph and edges are reversed\n"
                                 + "3 if the path is computing using a undirected.");
                 }
         }
 
         /**
          * Compute the shortest path distance between several nodes.
          * @param dsf
          * @param dataSet
          * @param source
          * @param target
          * @param costField
          * @param graphType
          * @param pm
          * @return
          * @throws GraphException
          * @throws DriverException 
          */
         public static DiskBufferDriver getMShortestPathLength(SQLDataSourceFactory dsf, DataSet dataSet, DataSet nodes, String costField, int graphType, ProgressMonitor pm) throws GraphException, DriverException {
                 if (checkMetadata(nodes)) {
                         if (graphType == DIRECT) {
                                 DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                                 dwMultigraphDataSource.setWeigthFieldIndex(costField);
                                 return computeDistanceBetweenSeveralNodes(dsf, dwMultigraphDataSource, nodes, pm);
                         } else if (graphType == DIRECT_REVERSED) {
                                 DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                                 dwMultigraphDataSource.setWeigthFieldIndex(costField);
                                 EdgeReversedGraphDataSource edgeReversedGraph = new EdgeReversedGraphDataSource(dwMultigraphDataSource);
                                 return computeDistanceBetweenSeveralNodes(dsf, edgeReversedGraph, nodes, pm);
                         } else if (graphType == UNDIRECT) {
                                 WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
                                 wMultigraphDataSource.setWeigthFieldIndex(costField);
                                 return computeDistanceBetweenSeveralNodes(dsf, wMultigraphDataSource, nodes, pm);
                         } else {
                                 throw new GraphException("Only 3 type of graphs are allowed."
                                         + "1 if the path is computing using a directed graph.\n"
                                         + "2 if the path is computing using a directed graph and edges are reversed\n"
                                         + "3 if the path is computing using a undirected.");
                         }
                 } else {
                         throw new GraphException("The table nodes must contains the field id, source and target");
                 }
         }
 
         /**
          * A method to check if the schemas are well populated to use graph analysis
          * tools.
          *
          * @throws DriverException
          */
         private static boolean checkMetadata(DataSet nodes) throws DriverException {
                 Metadata nodesMD = nodes.getMetadata();
                 ID_FIELD_INDEX = nodesMD.getFieldIndex(GraphSchema.ID);
                 SOURCE_FIELD_INDEX = nodesMD.getFieldIndex(GraphSchema.SOURCE_NODE);
                 TARGET_FIELD_INDEX = nodesMD.getFieldIndex(GraphSchema.TARGET_NODE);
                 if (ID_FIELD_INDEX == -1) {
                         throw new IllegalArgumentException("The table must contains a field named " + GraphSchema.ID);
                 }
 
                 if (SOURCE_FIELD_INDEX == -1) {
                         throw new IllegalArgumentException("The table must contains a field named " + GraphSchema.SOURCE_NODE);
                 }
                 if (TARGET_FIELD_INDEX == -1) {
                         throw new IllegalArgumentException("The table must contains a field named " + GraphSchema.TARGET_NODE);
                 }
                 return true;
         }
 
         /**
          * A method to create an alphanumeric index on the field source
          * stored in the datasource nodes.
          * @param dsf
          * @param nodes
          * @param pm
          * @throws GraphException 
          */
         private static void initIndex(SQLDataSourceFactory dsf, DataSet nodes, ProgressMonitor pm) throws GraphException {
                 try {
                         if (!dsf.getIndexManager().isIndexed(nodes, GraphSchema.SOURCE_NODE)) {
                                 dsf.getIndexManager().buildIndex(nodes, GraphSchema.SOURCE_NODE, pm);
                         }
                 } catch (IndexException ex) {
                         throw new GraphException("Unable to create index.", ex);
                 } catch (NoSuchTableException ex) {
                         throw new GraphException("Unable to find the table.", ex);
                 }
         }
 
         /**
          * Query the dataset using an alphanumeric index
          * @param fieldToQuery
          * @param valueToQuery
          * @return a map where key = target value and value = id value
          * @throws DriverException 
          */
         public static HashMap<Integer, Integer> getTargets(SQLDataSourceFactory dsf, DataSet nodes, Integer valueToQuery) throws DriverException {
                 DefaultAlphaQuery defaultAlphaQuery = new DefaultAlphaQuery(
                         GraphSchema.SOURCE_NODE, ValueFactory.createValue(valueToQuery));
                 Iterator<Integer> iterator = nodes.queryIndex(dsf, defaultAlphaQuery);
                 HashMap<Integer, Integer> targets = new HashMap<Integer, Integer>();
                 while (iterator.hasNext()) {
                         Integer integer = iterator.next();
                         targets.put(nodes.getInt(integer, TARGET_FIELD_INDEX), nodes.getInt(integer, ID_FIELD_INDEX));
                 }
                 return targets;
         }
 }
