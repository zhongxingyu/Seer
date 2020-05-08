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
 package org.gdms.gdmstopology.function;
 
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.indexes.IndexException;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.values.Value;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DiskBufferDriver;
 import org.gdms.driver.DriverException;
 import org.gdms.gdmstopology.graphcreator.GraphCreator;
 import org.gdms.gdmstopology.model.GraphException;
 import org.gdms.gdmstopology.model.GraphMetadataFactory;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.gdms.gdmstopology.parse.GraphFunctionParser;
 import org.gdms.gdmstopology.process.GraphPathCalculator;
 import org.gdms.sql.function.FunctionException;
 import org.gdms.sql.function.FunctionSignature;
 import org.gdms.sql.function.ScalarArgument;
 import org.gdms.sql.function.table.AbstractTableFunction;
 import org.gdms.sql.function.table.TableArgument;
 import org.gdms.sql.function.table.TableDefinition;
 import org.gdms.sql.function.table.TableFunctionSignature;
 import org.orbisgis.progress.ProgressMonitor;
 
 /**
  * Calculates the shortest path between two vertices of a graph using Dijkstra's
  * algorithm.
  *
  * <p> Example usage: <center> {@code SELECT * from
  * ST_ShortestPath(
  * input_table,
  * source_vertex,
  * target_vertex,
  * 'weights_column'[,orientation]);} </center> or: <center> {@code SELECT * from
  * ST_ShortestPath(
  * input_table,
  * source_vertex,
  * target_vertex,
  * 1[,orientation]);} </center>
  *
  * <p> Required parameters: <ul> <li> {@code input_table} - the input table.
  * Specifically, this is the {@code output_table_prefix.edges} table produced by
  * {@link ST_Graph}, except that an additional column specifying the weight of
  * each edge must be added (this is the 'weights_column'). <li>
  * {@code source_vertex} - an integer specifying the source vertex. <li>
  * {@code target_vertex} - an integer specifying the target vertex. <li>
  * {@code 'weights_column'} - a string specifying the name of the column of the
  * input table that gives the weight of each edge. If the graph is to be
  * considered unweighted, then enter 1.</ul>
  *
  * <p> Optional parameter: <ul> <li> {@code orientation} - an integer specifying
  * the orientation of the graph: <ul> <li> 1 if the graph is directed, <li> 2 if
  * it is directed and we wish to reverse the orientation of the edges, <li> 3 if
  * the graph is undirected. </ul> If no orientation is specified, we assume the
  * graph is directed. </ul>
  *
  * <p> October 15, 2012: Documentation added by Adam Gouge.
  *
  * @author Erwan Bocher
  * @author Adam Gouge
  */
 public class ST_ShortestPath extends AbstractTableFunction {
 
     /**
      * The name of this function.
      */
     private static final String NAME = "ST_ShortestPath";
     /**
      * The number of required arguments.
      */
     private static final int NUMBER_OF_REQUIRED_ARGUMENTS = 3;
     /**
      * The SQL order of this function.
      */
     private static final String SQL_ORDER =
             "SELECT * from  ST_ShortestPath("
             + "input_table, "
             + "source_vertex, "
             + "target_vertex, "
             + "'weights_column'"
             + "[,orientation]);";
     /**
      * Short description of this function.
      */
     private static final String SHORT_DESCRIPTION =
             "Calculates the shortest path between two vertices of a graph "
             + "using Dijkstra's algorithm. ";
     /**
      * Long description of this function.
      */
     private static final String LONG_DESCRIPTION =
             "<p> "
             + "Required parameters: "
             + "<ul> "
             + "<li> "
             + "<code>input_table</code> "
             + "- the "
             + "<code>output_table_prefix.edges</code> "
             + "table produced by the "
             + "<code>ST_Graph</code> function, except that an extra column "
             + "must be added to specify the weight of each edge ("
             + "<code>'weights_column'</code>"
             + "). "
             + "<li> "
             + "<code>source_vertex</code> "
             + "- specified by an integer. "
             + "<li> "
             + "<code>target_vertex</code> "
             + "- specified by an integer. "
             + "<li> "
             + "<code>'weights_column'</code> "
             + "- a string specifying the name of the column of the input "
             + "table that gives the weight of each edge. If the graph "
             + "is to be considered unweighted, then enter 1."
             + "</ul> " // end required parameters.
             + "<p> "
             + "Optional parameter: "
             + "<code>orientation</code> "
             + "- an integer specifying the orientation of the graph: "
             + "<ul> "
             + "<li> 1 if the graph is directed, "
             + "<li> 2 if it is directed and we wish to reverse the "
             + "orientation of the edges, "
             + "<li> 3 if the graph is undirected. "
             + "</ul> " // end orientation list
             + "If no orientation is specified, we assume the graph is"
             + "directed. "; // end optional parameters. 
     // REQUIRED ARGUMENTS
     /**
      * The source vertex.
      */
     private int source;
     /**
      * The target vertex.
      */
     private int target;
     /**
      * Specifies the weight column (or 1 in the case of an unweighted graph).
      */
     private String weightsColumn;
     // OPTIONAL ARGUMENTS
     /**
      * Specifies the orientation of the graph.
      */
     private int orientation = -1;
     /**
      * Description of this function.
      */
     private static final String DESCRIPTION =
             SHORT_DESCRIPTION + LONG_DESCRIPTION;
     /**
      * An error message to be displayed when {@link #evaluate(
      * org.gdms.data.DataSourceFactory,
      * org.gdms.driver.DataSet[],
      * org.gdms.data.values.Value[],
      * org.orbisgis.progress.ProgressMonitor)
      * fails.
      */
     private static final String EVALUATE_ERROR =
             "Cannot compute the shortest path";
 
     /**
      * Evaluates the function to calculate the shortest path using Dijkstra's
      * algorithm.
      *
      * @param dsf    The {@link DataSourceFactory} used to parse the data set.
      * @param tables The input table. (This {@link DataSet} array will contain
      *               only one element since there is only one input table.)
      * @param values Array containing the optional arguments.
      * @param pm     The progress monitor used to track the progress of the
      *               shortest path calculation.
      *
      * @return The {@link DataSet} containing the shortest path.
      *
      * @throws FunctionException
      */
     @Override
     public DataSet evaluate(
             DataSourceFactory dsf,
             DataSet[] tables,
             Value[] values,
             ProgressMonitor pm)
             throws FunctionException {
         try {
             // Set the source vertex.
             source = GraphFunctionParser.parseSource(values[0]);
             // Set the target vertex.
             target = GraphFunctionParser.parseTarget(values[1]);
             // Set the weights column.
             weightsColumn = GraphFunctionParser.parseWeight(values[2]);
 
             parseOptionalArguments(values);
 
             return calculateShortestPathAccordingToUserInput(
                     dsf,
                     tables[0],
                     pm);
         } catch (Exception ex) {
             throw new FunctionException(EVALUATE_ERROR, ex);
         }
     }
 
     /**
      * Calculates the shortest path according to the SQL arguments provided by
      * the user.
      *
      * @param dsf     The {@link DataSourceFactory} used to parse the data set.
      * @param dataSet The input table.
      * @param pm      The progress monitor used to track the progress of the
      *                calculation.
      *
      * @return The shortest path.
      *
      * @throws GraphException
      * @throws DriverException
      */
     private DiskBufferDriver calculateShortestPathAccordingToUserInput(
             DataSourceFactory dsf,
             DataSet dataSet,
             ProgressMonitor pm) throws GraphException,
             DriverException,
             IndexException {
 
         DiskBufferDriver diskBufferDriver;
         // UNWEIGHTED GRAPHS
         if (weightsColumn == null) {
             diskBufferDriver =
                     GraphPathCalculator.calculateShortestPathAllWeightsOne(
                     dsf,
                     dataSet,
                     source,
                     target,
                     orientation,
                     pm);
         } else { // WEIGHTED GRAPHS
             diskBufferDriver =
                     GraphPathCalculator.calculateShortestPath(
                     dsf,
                     dataSet,
                     source,
                     target,
                     orientation,
                     weightsColumn,
                     pm);
         }
         diskBufferDriver.open();
         return diskBufferDriver;
     }
 
     /**
      * Parse the optional function argument at the given index.
      *
      * <p> <i>Note</i>: For this function, there is only one optional argument,
      * the orientation.
      *
      * @param values Array containing the other arguments.
      * @param index  The index.
      *
      * @throws FunctionException
      */
     private void parseOptionalArgument(Value[] values, int index) throws
             FunctionException {
         orientation = GraphFunctionParser.parseOrientation(values[index]);
     }
 
     /**
      * Parse the optional function arguments.
      *
      * <p> <i>Note</i>: If the orientation is not specified, we consider the
      * graph to be directed.
      *
      * @param values Array containing the other arguments.
      *
      * @throws FunctionException
      */
     private void parseOptionalArguments(Value[] values) throws FunctionException,
             GraphException {
         // Set the default orientation to be directed.
         orientation = GraphSchema.DIRECT;
         // If the orientation is specified, then recover it.
         int index = NUMBER_OF_REQUIRED_ARGUMENTS;
         while (values.length > index) {
             parseOptionalArgument(values, index++);
         }
         System.out.print("Set the orientation to be ");
         if (orientation == GraphSchema.DIRECT) {
             System.out.println("directed.");
         } else if (orientation == GraphSchema.DIRECT_REVERSED) {
             System.out.println("reversed.");
         } else if (orientation == GraphSchema.UNDIRECT) {
             System.out.println("undirected.");
         } else {
             throw new GraphException(GraphCreator.GRAPH_TYPE_ERROR);
         }
     }
 
     /**
      * Returns the name of this function. This name will be used in SQL
      * statements.
      *
      * @return The name of this function.
      */
     @Override
     public String getName() {
         return NAME;
     }
 
     /**
      * Returns an example query using this function.
      *
      * @return An example query using this function.
      */
     @Override
     public String getSqlOrder() {
         return SQL_ORDER;
     }
 
     /**
      * Returns a description of this function.
      *
      * @return A description of this function.
      */
     @Override
     public String getDescription() {
         return DESCRIPTION;
     }
 
     /**
      * Returns the {@link Metadata} of the result of this function without
      * executing the query.
      *
      * @param tables {@link Metadata} objects of the input tables.
      *
      * @return The {@link Metadata} of the result.
      *
      * @throws DriverException
      */
     // TODO: The input 'Metadata[] tables' is never used!
     @Override
     public Metadata getMetadata(Metadata[] tables) throws DriverException {
        return GraphMetadataFactory.createEdgeMetadataShortestPath();
     }
 
     /**
      * Returns an array of all possible signatures of this function. Multiple
      * signatures arise from some arguments being optional.
      *
      * @return An array of all possible signatures of this function.
      */
     @Override
     public FunctionSignature[] getFunctionSignatures() {
         return new FunctionSignature[]{
                     // ALL WEIGHTS ONE
                     // First possible signature: (TABLE input_table, INT source, INT target, INT 1).
                     new TableFunctionSignature(
                     TableDefinition.ANY, // was TableDefinition.GEOMETRY before,
                     // but we are not necessarily going to return the geometries directly.
                     new TableArgument(TableDefinition.GEOMETRY),
                     ScalarArgument.INT,
                     ScalarArgument.INT,
                     ScalarArgument.INT),
                     // Second possible signature: (TABLE input_table, INT source, INT target, INT 1, INT orientation).
                     new TableFunctionSignature(
                     TableDefinition.ANY,
                     new TableArgument(TableDefinition.GEOMETRY),
                     ScalarArgument.INT,
                     ScalarArgument.INT,
                     ScalarArgument.INT,
                     ScalarArgument.INT),
                     // WEIGHTED
                     // First possible signature: (TABLE input_table, INT source, INT target, STRING 'weights_column').
                     new TableFunctionSignature(
                     TableDefinition.ANY,
                     new TableArgument(TableDefinition.GEOMETRY),
                     ScalarArgument.INT,
                     ScalarArgument.INT,
                     ScalarArgument.STRING),
                     // Second possible signature: (TABLE input_table, INT source, INT target, STRING 'weights_column', INT orientation).
                     new TableFunctionSignature(
                     TableDefinition.ANY,
                     new TableArgument(TableDefinition.GEOMETRY),
                     ScalarArgument.INT,
                     ScalarArgument.INT,
                     ScalarArgument.STRING,
                     ScalarArgument.INT)
                 };
     }
 }
