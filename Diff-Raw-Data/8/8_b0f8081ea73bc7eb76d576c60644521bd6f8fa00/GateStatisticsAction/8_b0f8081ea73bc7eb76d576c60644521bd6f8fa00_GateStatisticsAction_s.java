 /*
  *  GeoBatch - Open Source geospatial batch processing system
  *  http://geobatch.geo-solutions.it/
  *  Copyright (C) 2013 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.geobatch.destination.action.gatestatistics;
 
 import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
 import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
 import it.geosolutions.geobatch.actions.ds2ds.Ds2dsConfiguration;
 import it.geosolutions.geobatch.actions.ds2ds.util.FeatureConfigurationUtil;
 import it.geosolutions.geobatch.annotations.Action;
 import it.geosolutions.geobatch.catalog.impl.configuration.TimeFormatConfiguration;
 import it.geosolutions.geobatch.destination.ingestion.MetadataIngestionHandler;
 import it.geosolutions.geobatch.destination.ingestion.gate.statistics.GateStatisticsProcess;
 import it.geosolutions.geobatch.flow.event.action.ActionException;
 import it.geosolutions.geobatch.flow.event.action.BaseAction;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.EventObject;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.geotools.data.DataStore;
 import org.geotools.jdbc.JDBCDataStore;
 
 /**
  * GeoBatch gate statistics action
  * 
  * @author adiaz
  */
 @Action(configurationClass = GateStatisticConfiguration.class)
 public class GateStatisticsAction extends BaseAction<EventObject> {
 
 private GateStatisticConfiguration configuration;
 
 /**
  * Time format configuration for the ingestion
  */
 private TimeFormatConfiguration timeFormatConfiguration;
 
 public GateStatisticsAction(final GateStatisticConfiguration configuration)
         throws IOException {
     super(configuration);
     this.configuration = configuration;
 }
 
 /**
  * Check if the configuration it's correctly. Just obtain the data source
  */
 public boolean checkConfiguration() {
     DataStore ds = null;
     try {
         // Don't read configuration for the file, just
         // this.outputfeature configuration
         ds = FeatureConfigurationUtil.createDataStore(configuration
                 .getOutputFeature());
         if (!(ds instanceof JDBCDataStore)) {
             LOGGER.error("Incorrect datasource for this action");
             return false;
         } else {
             return true;
         }
     } catch (Exception e) {
         LOGGER.error("Incorrect datasource for this action");
         return false;
     } finally {
         ds.dispose();
     }
 }
 
 /**
  * Execute process
  */
 public Queue<EventObject> execute(Queue<EventObject> events)
         throws ActionException {
 
     // return object
     final Queue<EventObject> ret = new LinkedList<EventObject>();
 
     while (events.size() > 0) {
         final EventObject ev;
         try {
             if ((ev = events.remove()) != null) {
                 if (LOGGER.isTraceEnabled()) {
                     LOGGER.trace("Working on incoming event: " + ev.getSource());
                 }
                 if (ev instanceof FileSystemEvent) {
                     FileSystemEvent fileEvent = (FileSystemEvent) ev;
                     File file = fileEvent.getSource();
                     // The source dataStore it's used to read statistics
                     DataStore sourceDs = FeatureConfigurationUtil
                             .createDataStore(configuration.getSourceFeature());
                     // The output dataStore it's used to write statistics/log
                     DataStore ds = FeatureConfigurationUtil
                             .createDataStore(configuration.getOutputFeature());
                     if (ds == null) {
                         throw new ActionException(this, "Can't find datastore ");
                     }
                     try {
                         if (!(ds instanceof JDBCDataStore)) {
                             throw new ActionException(this,
                                     "Bad Datastore type "
                                             + ds.getClass().getName());
                         }
                         if (!(sourceDs instanceof JDBCDataStore)) {
                             throw new ActionException(this,
                                     "Bad Datastore type "
                                             + sourceDs.getClass().getName());
                         }
                         JDBCDataStore dataStore = (JDBCDataStore) ds;
                         JDBCDataStore inputDataStore = (JDBCDataStore) sourceDs;
                         dataStore.setExposePrimaryKeyColumns(true);
                         MetadataIngestionHandler metadataHandler = new MetadataIngestionHandler(
                                 dataStore);
                         doProcess(configuration, dataStore, metadataHandler,
                                 file, inputDataStore);
 
                         // pass the feature config to the next action
                         ret.add(new FileSystemEvent(((FileSystemEvent) ev)
                                 .getSource(), FileSystemEventType.FILE_ADDED));
                     } finally {
                         ds.dispose();
                     }
                 }
 
                 // add the event to the return
                 ret.add(ev);
 
             } else {
                 if (LOGGER.isErrorEnabled()) {
                     LOGGER.error("Encountered a NULL event: SKIPPING...");
                 }
                 continue;
             }
         } catch (Exception ioe) {
             final String message = "Unable to produce the output: "
                     + ioe.getLocalizedMessage();
             if (LOGGER.isErrorEnabled())
                 LOGGER.error(message, ioe);
 
             throw new ActionException(this, message);
         }
     }
     return ret;
 }
 
 /**
  * Call to Gate statistics process
  * 
  * @param cfg
  * @param dataStore
  * @param metadataHandler
  * @param file
  * @param inputDataStore
  * @throws ActionException
  */
 protected void doProcess(Ds2dsConfiguration cfg, JDBCDataStore dataStore,
         MetadataIngestionHandler metadataHandler, File file,
         JDBCDataStore inputDataStore) throws ActionException {
 
     try {
         GateStatisticsProcess computation = new GateStatisticsProcess(
                 // type name read on file name
                 getInputTypeName(file), listenerForwarder, metadataHandler,
                 dataStore, file, inputDataStore,
                 configuration.getTimeFormatConfiguration());
 
         computation.generateStatistics(configuration.isPurgeData());
 
     } catch (Exception ex) {
         // TODO: what shall we do here??
         // log and rethrow for the moment, but a rollback should be
         // implementened somewhere
         LOGGER.error("Error generating gate statistics", ex);
         throw new ActionException(this, "Error generating gate statistics", ex);
     }
 
 }
 
 /**
  * @param file
  * @return
  */
 private String getInputTypeName(File file) {
     String fileName = file != null ? file.getName() : null;
     if (fileName != null && fileName.contains(".")) {
         fileName = fileName.substring(0, fileName.lastIndexOf("."));
     }
     return fileName;
 }
 
 /**
  * @return the timeFormatConfiguration
  */
 public TimeFormatConfiguration getTimeFormatConfiguration() {
     return timeFormatConfiguration;
 }
 
 /**
  * @param timeFormatConfiguration the timeFormatConfiguration to set
  */
 public void setTimeFormatConfiguration(
         TimeFormatConfiguration timeFormatConfiguration) {
     this.timeFormatConfiguration = timeFormatConfiguration;
 }
 
 }
