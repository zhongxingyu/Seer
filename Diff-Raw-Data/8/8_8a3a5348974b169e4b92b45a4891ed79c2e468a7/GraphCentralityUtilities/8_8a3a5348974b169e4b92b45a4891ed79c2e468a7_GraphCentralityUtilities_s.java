 /**
  * GDMS-Topology is a library dedicated to graph analysis. It is based on the
  * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
  * and processing large graphs using spatial and alphanumeric indexes.
  *
  * This version is developed at French IRSTV institute as part of the EvalPDU
  * project, funded by the French Agence Nationale de la Recherche (ANR) under
  * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
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
 
 import com.graphhopper.storage.Graph;
 import com.graphhopper.storage.GraphStorage;
 import com.graphhopper.storage.RAMDirectory;
import com.graphhoppersna.centrality.UnweightedGraphAnalyzer;
 import gnu.trove.iterator.TIntDoubleIterator;
 import gnu.trove.map.hash.TIntDoubleHashMap;
 import java.util.Iterator;
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.schema.DefaultMetadata;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DiskBufferDriver;
 import org.gdms.driver.DriverException;
 import org.gdms.gdmstopology.model.GraphException;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.orbisgis.progress.ProgressMonitor;
 
 /**
  * A collection of utilities to calculate various centrality indices on the
  * nodes of a given graph.
  *
  * @author Adam Gouge
  */
 public class GraphCentralityUtilities extends GraphAnalysis {
 
     /**
      * Registers the closeness centrality indices of the nodes of the given
      * graph with all weights equal to 1.
      *
      * @param dsf       The {@link DataSourceFactory} used to parse the data
      *                  set.
      * @param dataSet   The graph data set.
      * @param graphType An integer specifying the type of graph: 1 if directed,
      *                  2 if directed and we wish to reverse the direction of the
      *                  edges, 3 if undirected.
      * @param pm        Used to track the progress of the calculation.
      *
      * @throws GraphException
      * @throws DriverException
      */
     public static void registerClosenessCentralityIndicesAllWeightsOne(
             DataSourceFactory dsf,
             DataSet dataSet,
             String outputTablePrefix,
             int graphType,
             ProgressMonitor pm)
             throws GraphException, DriverException {
 
         // Create the graph from the data set.
         GraphStorage graph = GraphLoaderUtilities.
                 loadGraphFromDataSetAllWeightsOne(
                 dataSet,
                 graphType);
 
         // Calculate the closeness centrality.
         DiskBufferDriver closenessCentralityDriver =
                 calculateClosenessCentralityIndicesAllWeightsOne(dsf, graph, pm);
 
         // Write the results to a new table.
         writeToTable(dsf, closenessCentralityDriver, outputTablePrefix);
     }
 //    /**
 //     * Registers the closeness centrality indices of the nodes of the given
 //     * graph.
 //     *
 //     * @param dsf              The {@link DataSourceFactory} used to parse the
 //     *                         data set.
 //     * @param dataSet          The graph data set.
 //     * @param weightColumnName The string specifying the name of the weight
 //     *                         column giving the weight of each edge.
 //     * @param graphType        An integer specifying the type of graph: 1 if
 //     *                         directed, 2 if directed and we wish to reverse the
 //     *                         direction of the edges, 3 if undirected.
 //     * @param pm               Used to track the progress of the calculation.
 //     *
 //     * @throws GraphException
 //     * @throws DriverException
 //     */
 //    public static void registerClosenessCentralityIndices(
 //            DataSourceFactory dsf,
 //            DataSet dataSet,
 //            String weightColumnName,
 //            String outputTablePrefix,
 //            int graphType,
 //            ProgressMonitor pm)
 //            throws GraphException, DriverException {
 //
 //        // Create the graph from the data set.
 //        Graph graph = GraphLoaderUtilities.
 //                loadGraphFromDataSet(
 //                dataSet,
 //                weightColumnName,
 //                graphType);
 //
 //        // Calculate the closeness centrality.
 //        DiskBufferDriver closenessCentralityDriver =
 //                calculateClosenessCentralityIndices(dsf, graph, pm);
 //
 //        // Write the results to a new table.
 //        writeToTable(dsf, closenessCentralityDriver, outputTablePrefix);
 //    }
 
     /**
      * Calculates the closeness centrality indices of all nodes of the given
      * graph.
      *
      * @param dsf   The {@link DataSourceFactory} used to parse the data set.
      * @param graph The graph on which we will calculate the closeness
      *              centrality indices.
      * @param pm    Used to track the progress of the calculation.
      *
      * @return A new {@link DiskBufferDriver} containing a list of all nodes and
      *         their closeness centrality indices.
      *
      * @throws GraphException
      * @throws DriverException
      */
     public static DiskBufferDriver calculateClosenessCentralityIndicesAllWeightsOne(
             DataSourceFactory dsf,
             Graph graph,
             ProgressMonitor pm) throws DriverException {
 
         // CALCULATION
 
         // Initialize an unweighted graph analyzer.
         // TODO: Adapt this to weighted graphs later.
         UnweightedGraphAnalyzer analyzer = new UnweightedGraphAnalyzer(graph);
 
         // Calculate the closeness centrality.
        // TODO: Would a list be a more efficient data structure?
        TIntDoubleHashMap closenessCentrality = analyzer.
                computeClosenessAllWeightsOne();
 
         // TRANSFER THE RESULTS FROM GRAPHHOPPER TO A DISKBUFFERDRIVER
 
         // Create the metadata for the new table that will hold the 
         // closeness centrality indices.
         Metadata closenessMetadata = new DefaultMetadata(
                 new Type[]{
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.DOUBLE)},
                 new String[]{
                     GraphSchema.ID,
                     GraphSchema.CLOSENESS_CENTRALITY});
 
         // Create a DiskBufferDriver to store the centrality indices.
         DiskBufferDriver closenessBufferDriver = new DiskBufferDriver(dsf,
                 closenessMetadata);
 
         // Get an iterator on the closeness centrality hash map.
         TIntDoubleIterator centralityIt = closenessCentrality.iterator();
 
         // Record the centrality indices in the DiskBufferDriver.
         // TODO: Are the results sorted?
         while (centralityIt.hasNext()) {
             centralityIt.advance();
             closenessBufferDriver.addValues(
                     new Value[]{
                         // Node ID
                         ValueFactory.createValue(centralityIt.key()),
                         // Centrality index
                         ValueFactory.createValue(centralityIt.value())
                     });
         }
 
         // CLEAN UP
 
         // We are done writing.
         closenessBufferDriver.writingFinished();
         // The task is done.
         pm.endTask();
         // So close the DiskBufferDriver.
         closenessBufferDriver.close();
         // And return it.
         return closenessBufferDriver;
     }
 
     /**
      * Writes the results of the closeness centrality calculation to a new table
      * in OrbisGIS.
      *
      * @param dsf                       The {@link DataSourceFactory} used to
      *                                  parse the data set.
      * @param closenessCentralityDriver
      * @param outputTablePrefix
      *
      * @throws DriverException
      */
     // TODO: Also write the geometries.
     private static void writeToTable(
             DataSourceFactory dsf,
             DiskBufferDriver closenessCentralityDriver,
             String outputTablePrefix) throws DriverException {
         closenessCentralityDriver.open(); // TODO: Necessary?
         // Register the result in a new output table.
         String outputTableName = dsf.getSourceManager().
                 getUniqueName(
                 outputTablePrefix
                 + "." + GraphSchema.CLOSENESS_CENTRALITY);
         dsf.getSourceManager().register(
                 outputTableName,
                 closenessCentralityDriver.getFile());
     }
 }
