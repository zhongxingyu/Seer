 /*
 
  *    http://geotools.org
  *
  *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package it.geosolutions.geobatch.destination.ingestion;
 
 import it.geosolutions.geobatch.catalog.impl.TimeFormat;
 import it.geosolutions.geobatch.catalog.impl.configuration.TimeFormatConfiguration;
 import it.geosolutions.geobatch.destination.common.InputObject;
 import it.geosolutions.geobatch.destination.ingestion.gate.model.ExportData;
 import it.geosolutions.geobatch.destination.ingestion.gate.model.Transit;
 import it.geosolutions.geobatch.destination.ingestion.gate.model.Transits;
 import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXB;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.Transaction;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.data.simple.SimpleFeatureStore;
 import org.geotools.factory.Hints;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Handle the gate ingestion processes. This processor reads a known format
  * exported data file for transit elements and insert in 'siig_gate_t_dato'
  * table using JDBCDataSource. <br />
  * <br />
  * This is an example of the xml file: <br/>
  * <code>
  * &lt;ExportData xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;
  * <br/>    &lt;Transits&gt;
  * <br/>    &lt;Transit&gt;
  * <br/>      &lt;IdGate&gt;2004&lt;/IdGate&gt;
  * <br/>      &lt;IdTransito&gt;81831&lt;/IdTransito&gt;
  * <br/>      &lt;DataRilevamento&gt;2013-10-23T10:34:00Z&lt;/DataRilevamento&gt;
  * <br/>      &lt;Corsia&gt;0&lt;/Corsia&gt;
  * <br/>      &lt;Direzione&gt;0&lt;/Direzione &gt;
  * <br/>      &lt;KemlerCode /&gt;
  * <br/>      &lt;OnuCode /&gt;
  * <br/>    &lt;/Transit&gt;
  * <br/>    &lt;Transit&gt;
  * <br/>      &lt;IdGate&gt;2004&lt;/IdGate&gt;
  * <br/>      &lt;IdTransito&gt;81832&lt;/IdTransito&gt;
  * <br/>      &lt;DataRilevamento&gt;2013-10-23T10:34:00Z&lt;/DataRilevamento&gt;
  * <br/>      &lt;Corsia&gt;0&lt;/ Corsia &gt;
  * <br/>      &lt;Direzione&gt;1&lt;/Direzione&gt;
  * <br/>      &lt;KemlerCode /&gt;
  * <br/>      &lt;OnuCode /&gt;
  * <br/>    &lt;/Transit&gt;
  * <br/>  &lt;/Transits&gt;
  * <br/>&lt;/ExportData&gt;
  * </code>
  * 
  * @author adiaz
  */
 public class GateIngestionProcess extends InputObject {
 
 private final static Logger LOGGER = LoggerFactory
         .getLogger(GateIngestionProcess.class);
 
 /**
  * No target specified
  */
 private static final Integer DEFAULT_GATE_TYPE = NO_TARGET;
 
 /**
  * Pattern: [Server]_[date]_[time].xml
  */
 private static Pattern typeNameParts = Pattern
         .compile("^([0-9]{2})[_-]([0-9]{8})[_-]([0-9]{6})$");
 
 /**
  * Partner always 1
  */
 private static int PARTNER = 1;
 
 /**
  * Always 'A'
  */
 private static String PARTNER_CODE = "A";
 
 /**
  * Flag to indicate that
  */
 private Boolean ignorePks;
 
 /**
  * File with the data to be inserted
  */
 private File file = null;
 
 /**
  * Date in the stored in the file name
  */
 private String date;
 
 private String outputType= "siig_gate_t_dato";
 
 /**
  * Key to save ids stored of ingested gates
  */
 public static String IDS = "IDS";
 
 /**
  * Key to save error count
  */
 public static String ERROR_COUNT = "ERROR_COUNT";
 
 /**
  * Key to save processed count
  */
 public static String PROCESSED_COUNT = "PROCESSED_COUNT";
 
 /**
  * Key to save total count
  */
 public static String TOTAL_COUNT = "TOTAL_COUNT";
 
 /**
  * Time format component
  */
 private TimeFormat timeFormat;
 
 /**
  * Parametrized constructor
  * 
  * @param typeName
  * @param listenerForwarder
  * @param metadataHandler
  * @param dataStore
  * @param file
  */
 public GateIngestionProcess(String typeName,
         ProgressListenerForwarder listenerForwarder,
         MetadataIngestionHandler metadataHandler, DataStore dataStore, File file, TimeFormatConfiguration timeFormatConfiguration) {
 
     super(typeName, listenerForwarder, metadataHandler, dataStore);
 
     // init from file to be inserted
     this.setFile(file);
     
     // create time format
     this.timeFormat = new TimeFormat(null, null, null, timeFormatConfiguration);
 }
 
 /**
  * Parametrized constructor
  * 
  * @param typeName
  * @param listenerForwarder
  * @param metadataHandler
  * @param dataStore
  * @param file
  * @param ignorePks
  */
 public GateIngestionProcess(String typeName,
         ProgressListenerForwarder listenerForwarder,
         MetadataIngestionHandler metadataHandler, DataStore dataStore,
         File file, Boolean ignorePks, TimeFormatConfiguration timeFormatConfiguration) {
     this(typeName, listenerForwarder, metadataHandler, dataStore, file, timeFormatConfiguration);
     this.ignorePks = ignorePks;
 }
 
 /**
  * Read data from the file name
  */
 protected boolean parseTypeName(String inputTypeName) {
     Matcher m = typeNameParts.matcher(inputTypeName);
 
     if (m.matches()) {
         // file date identifier
        date = m.group(2);
         this.inputTypeName = inputTypeName;
 
         return true;
     }
     return false;
 }
 
 /**
  * Imports the gate data from the exported file to database.
  * 
  * @param ignorePks Flag to indicates that should ignore pks in the xml file and
  *        create a new one with a sequence manager
  * @throws IOException
  * 
  * @return list of ids inserted on database
  */
 @SuppressWarnings("unchecked")
 public List<Long> importGates(boolean ignorePks) throws IOException {
     return (List<Long>) doProcess(ignorePks).get(IDS);
 }
 
 /**
  * Imports the gate data from the exported file to database.
  * 
  * @param ignorePks Flag to indicates that should ignore pks in the xml file and
  *        create a new one with a sequence manager
  * @throws IOException
  * 
  * @return resume of the operation in a map
  */
 public Map<String, Object> doProcess(boolean ignorePks) throws IOException {
     
     Map<String, Object> result = new HashMap<String, Object>();
     List<Long> ids = new ArrayList<Long>();
     reset();
     this.ignorePks = ignorePks;
     if (isValid()) {
 
         int process = -1;
         int trace = -1;
         int errors = 0;
         int total = 0;
         int processed = 0;
         float percent = 0;
 
         try {
 
             process = createProcess();
 
             // write log for the imported file
             trace = logFile(process, DEFAULT_GATE_TYPE, PARTNER, PARTNER_CODE,
                     date, false);
 
             // Read xml file
             ExportData exportData = null;
             try {
                 exportData = JAXB.unmarshal(file, ExportData.class);
             } catch (Exception e) {
                 String msg = "Unknown file format for gate ingestion";
                 updateProgress(90, msg);
                 metadataHandler.logError(trace, errors, msg, getError(e), 0);
                 throw new IOException(msg);
             }
 
             if (exportData != null && exportData.getTransits().size() == 1) {
                 Transits transits = exportData.getTransits().get(0);
                 total = transits.getTransit().size();
                 float ftot = new Float(total);
 
                 // Insert one by one
                 for (Transit transit : transits.getTransit()) {
                     Long id = null;
                     try {
 
                         // Update status
                         inputCount++;
                         float fproc = new Float(++processed);
                         String msg = "Importing data in transit table: "
                                 + (processed) + "/" + total;
                         percent = (fproc++ / ftot);
                         updateProgress(percent * 100, msg);
                         if (LOGGER.isInfoEnabled()) {
                             LOGGER.info(msg);
                         }
 
                         // insert
                         id = createTransit(transit);
 
                         // add to result
                         if (id != null) {
                             // Trace insert
                             if (LOGGER.isTraceEnabled()) {
                                 LOGGER.trace("Correctly insert id " + id);
                             }
                             ids.add(id);
                         } else {
                             LOGGER.error("Error on gate ingestion for element "
                                     + inputCount);
                         }
 
                     } catch (Exception e) {
                         errors++;
                         if (id != null) {
                             metadataHandler.logError(trace, errors,
                                     "Error on gate ingestion", getError(e),
                                     id.intValue());
                             String msg = "Error on gate ingestion for id "
                                     + id.intValue();
                             updateProgress(percent * 100, msg);
                             LOGGER.error(msg);
                         } else {
                             metadataHandler.logError(trace, errors,
                                     "Error importing element " + inputCount,
                                     getError(e), 0);
                             String msg = "Error on gate ingestion for element "
                                     + inputCount;
                             updateProgress(percent * 100, msg);
                             LOGGER.error(msg);
                         }
                     }
                 }
             } else {
                 LOGGER.error("Incorrect format for ingestion");
             }
 
             // all complete
             importFinished(total, errors, "Data imported in transit table");
             metadataHandler.updateLogFile(trace, total, errors, true);
 
         } catch (IOException e) {
             errors++;
             metadataHandler.logError(trace, errors, "Error importing data",
                     getError(e), 0);
 
             // close current process phase
             process = closeProcess(process);
 
             throw e;
         } finally {
             if (process != -1) {
                 // close current process phase
                 metadataHandler.closeProcessPhase(process, "A");
             }
 
         }
         
         // save counts
         result.put(ERROR_COUNT, errors);
         result.put(PROCESSED_COUNT, processed);
         result.put(TOTAL_COUNT, total);
 
         // close current process phase
         process = closeProcess(process);
     }
 
     // save ids
     result.put(IDS, ids);
     
     return result;
 }
 
 private void updateProgress(float progress, String msg) {
     listenerForwarder.setProgress(progress);
     listenerForwarder.setTask(msg);
     listenerForwarder.progressing();
 }
 
 private int closeProcess(int process) throws IOException {
     if (process != -1) {
         // close current process phaseransit.getIdTransito().toString()
         metadataHandler.closeProcessPhase(process, "A");
         process = -1;
     }
     return process;
 }
 
 /**
  * Create a transit in the data store
  * 
  * @param transit
  * @return
  * @throws Exception
  */
 public Long createTransit(Transit transit) throws Exception {
 
     // prepare data
     Timestamp timestamp = timeFormat.getTimeStamp(transit.getDataRilevamento());
     Date date = timeFormat.getDate(transit.getDataRilevamento());
     Calendar cal = timeFormat.getCalendar(date);
     
     // null value should throw an exception
     String arriveDate = timestamp != null ? timestamp + "" : null;
     Long idLong = null;
 
     int idTematico;
     if (ignorePks) {
         idTematico = nextId();
     } else {
         idTematico = transit.getIdTransito().intValue();
     }
     ;
     idLong = new Long(idTematico);
 
     // Create feature
     SimpleFeatureSource featureSource = dataStore.getFeatureSource(outputType);
     Transaction transaction = new DefaultTransaction();
 
     SimpleFeatureType featureType = featureSource.getSchema();
     SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
 
     // values
     for (AttributeDescriptor attDesc : featureType.getAttributeDescriptors()) {
         // known columns
         String name = attDesc.getName().getLocalPart();
         if (name.equals("id_dato")) {
             featureBuilder.add(idLong.toString());
         } else if (name.equals("fk_gate")) {
             featureBuilder.add(transit.getIdGate().toString());
         } else if (name.equals("data_rilevamento")) {
             featureBuilder.add(arriveDate);
         } else if (name.equals("ora_fuso_orario")){
             featureBuilder.add(timeFormat.getHour(cal));
         }else if (name.equals("minuto_fuso_orario")) {
             featureBuilder.add(timeFormat.getMinutes(cal));
         } else if (name.equals("data_ricezione")) {
             featureBuilder.add(timeFormat.getNowTimestamp());
         } else if (name.equals("flg_corsia")) {
             featureBuilder.add(transit.getCorsia().toString());
         } else if (name.equals("flg_direzione")) {
             featureBuilder.add(transit.getDirezione());
         } else if (name.equals("codice_kemler")) {
             featureBuilder.add(transit.getKemlerCode());
         } else if (name.equals("codice_onu")) {
             featureBuilder.add(transit.getOnuCode());
         }else{
             // unknown column, try to insert as null.
             featureBuilder.add(null);
         }
     }
 
     SimpleFeature feature = featureBuilder.buildFeature(idLong.toString());
     feature.getUserData().put(Hints.USE_PROVIDED_FID, true);
 
     SimpleFeatureStore featureStore = null;
     if (featureSource instanceof SimpleFeatureStore) {
         featureStore = (SimpleFeatureStore) featureSource;
 
     } else if (featureSource.getDataStore() instanceof SimpleFeatureSource){
         featureStore = (SimpleFeatureStore) featureSource.getDataStore(); 
     }else{
         String msg = "'" + featureSource.getClass().getSimpleName() + "' is an unknown format for the data source. Couldn't save feature";
         LOGGER.error(msg);
         throw new IOException(msg);
     }
     
     if(featureStore != null){
 
         featureStore.setTransaction(transaction);
         try {
             featureStore.addFeatures(DataUtilities.collection(feature));
             transaction.commit();
 
         } catch (Exception problem) {
             transaction.rollback();
             throw new IOException(problem);
         } finally {
             transaction.close();
         }
     }
 
     return idLong;
 }
 
 /**
  * Check if file and datastore is not null
  */
 protected boolean isValid() throws IOException {
     return file != null && dataStore != null;
 }
 
 /**
  * @param file the file to set
  */
 public void setFile(File file) {
     // init from file to be inserted
     this.file = file;
     
     //parseTypeName(fileName);
 }
 
 }
