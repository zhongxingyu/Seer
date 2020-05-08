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
 package org.gdms.gdmstopology.centrality;
 
 import com.graphhopper.sna.progress.DefaultProgressMonitor;
 import java.lang.reflect.InvocationTargetException;
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.indexes.IndexException;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DriverException;
 import static org.gdms.gdmstopology.centrality.GraphAnalyzer.ANALYZER_PREP_ERROR;
 import static org.gdms.gdmstopology.centrality.GraphAnalyzer.LOGGER;
 import org.gdms.gdmstopology.graphcreator.WeightedGraphCreator;
 import org.gdms.gdmstopology.model.GraphException;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.orbisgis.progress.ProgressMonitor;
 
 /**
  * A {@link GraphAnalyzer} for weighted graphs.
  *
  * @author Adam Gouge
  */
public class WeightedGraphAnalyzer extends GraphAnalyzer {
 
     /**
      * The name of the weight column.
      */
     private final String weightColumnName;
 
     /**
      * Constructs a new {@link WeightedGraphAnalyzer}.
      *
      * @param dsf         The {@link DataSourceFactory} used to parse the data
      *                    set.
      * @param dataSet     The data set.
      * @param pm          The progress monitor used to track the progress of the
      *                    calculation.
      * @param orientation The orientation.
      *
      * @throws DriverException
      * @throws GraphException
      */
     public WeightedGraphAnalyzer(
             DataSourceFactory dsf,
             DataSet dataSet,
             ProgressMonitor pm,
             int orientation,
             String weightColumnName)
             throws DriverException,
             GraphException {
         super(dsf, dataSet, pm, orientation);
         this.weightColumnName = weightColumnName;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getOutputTableSuffix() {
         return GraphSchema.WEIGHTED + "." + super.getOutputTableSuffix();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected com.graphhopper.sna.centrality.WeightedGraphAnalyzer prepareAnalyzer() {
         try {
             return new com.graphhopper.sna.centrality.WeightedGraphAnalyzer(
                     new WeightedGraphCreator(dataSet, orientation,
                                              weightColumnName)
                     .prepareGraph(),
                     new DefaultProgressMonitor());
         } catch (IndexException ex) {
             LOGGER.trace(ANALYZER_PREP_ERROR + " " + INDICES_ERROR, ex);
         } catch (NoSuchMethodException ex) {
             LOGGER.trace(ANALYZER_PREP_ERROR, ex);
         } catch (InstantiationException ex) {
             LOGGER.trace(ANALYZER_PREP_ERROR, ex);
         } catch (IllegalAccessException ex) {
             LOGGER.trace(ANALYZER_PREP_ERROR, ex);
         } catch (IllegalArgumentException ex) {
             LOGGER.trace(ANALYZER_PREP_ERROR, ex);
         } catch (InvocationTargetException ex) {
             LOGGER.trace(ANALYZER_PREP_ERROR, ex);
         }
         return null;
     }
 }
