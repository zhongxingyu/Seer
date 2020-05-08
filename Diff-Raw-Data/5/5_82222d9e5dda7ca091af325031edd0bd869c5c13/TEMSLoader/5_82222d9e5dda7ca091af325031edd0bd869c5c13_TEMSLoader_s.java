 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.loader;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 public class TEMSLoader extends DriveLoader {
     private static final String TIMESTAMP_DATE_FORMAT = "HH:mm:ss.S";
     private static final String MS_KEY = "ms";
     // private Node point = null;
     private int first_line = 0;
     private int last_line = 0;
     private String previous_ms = null;
     private String previous_time = null;
     private int previous_pn_code = -1;
     private Float currentLatitude = null;
     private Float currentLongitude = null;
     private String time = null;
     private long timestamp = 0L;
     private HashMap<String, float[]> signals = new HashMap<String, float[]>();
     private String event;
     private ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
     private Node mNode;
     private Node virtualMnode;
     private String virtualDatasetName;
 
     /**
      * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
      * 
      * @param filename of file to load
      * @param display for opening message dialogs
      * @param dataset to add data to
      */
     public TEMSLoader(Calendar workDate, String filename, Display display, String dataset) {
         _workDate = workDate;
         driveType = DriveTypes.TEMS;
         mNode = null;
         virtualMnode = null;
         initialize("TEMS", null, filename, display, dataset);
         initializeLuceneIndex();
         initializeKnownHeaders();
         addDriveIndexes();
     }
 
     /**
      * Constructor for loading data in test mode, with no display and NeoService passed
      * 
      * @param neo database to load data into
      * @param filename of file to load
      * @param display
      */
     public TEMSLoader(GraphDatabaseService neo, String filename) {
         driveType = DriveTypes.TEMS;
         virtualMnode = null;
         mNode = null;
         _workDate = new GregorianCalendar();
         _workDate.setTimeInMillis(new File(filename).lastModified());
         initialize("TEMS", neo, filename, null, null);
         initializeLuceneIndex();
         initializeKnownHeaders();
         addDriveIndexes();
     }
     
     /**
      * Constructor for loading data in test mode, with no display and NeoService passed
      * 
      * @param neo database to load data into
      * @param filename of file to load
      * @param display
      */
     public TEMSLoader(GraphDatabaseService neo, String filename, String datasetName, LuceneIndexService anIndex) {
         driveType = DriveTypes.TEMS;
         virtualMnode = null;
         mNode = null;
         _workDate = new GregorianCalendar();
         _workDate.setTimeInMillis(new File(filename).lastModified());
         initialize("TEMS", neo, filename, null, datasetName);
         if (anIndex==null) {
 			initializeLuceneIndex();
 		}
         else{
         	index = anIndex;
         }
 		initializeKnownHeaders();
         addDriveIndexes();
     }
 
     /**
      * Build a map of internal header names to format specific names for types that need to be known
      * in the algorithms later.
      */
     private void initializeKnownHeaders() {
         addKnownHeader(1, "latitude", ".*latitude");
         addKnownHeader(1, "longitude", ".*longitude");
         addKnownHeader(1, "ms","MS");
         addMappedHeader(1, "ms","MS", "ms", new StringMapper());
         addMappedHeader(1, "message_type","Message Type", "message_type", new StringMapper());
         addMappedHeader(1, "event", "Event Type", "event_type", new PropertyMapper() {
 
             @Override
             public Object mapValue(String originalValue) {
                 return originalValue.replaceAll("HO Command.*", "HO Command");
             }
         });
 
         final SimpleDateFormat df = new SimpleDateFormat(TIMESTAMP_DATE_FORMAT);
         addMappedHeader(1, "time", "Timestamp", "timestamp", new PropertyMapper() {
 
             @Override
             public Object mapValue(String time) {
                 Date datetime;
                 try {
                     datetime = df.parse(time);
                 } catch (ParseException e) {
                     error(e.getLocalizedMessage());
                     return 0L;
                 }
                 return datetime;
             }
         });
         PropertyMapper intMapper = new PropertyMapper() {
             
             @Override
             public Object mapValue(String value) {
                 try {
                     return Integer.parseInt(value);
                 } catch (NumberFormatException e) {
                     error(e.getLocalizedMessage());
                     return 0L;
                 }
             }
         };
         addMappedHeader(1, "all_rxlev_full","All-RxLev Full", "all_rxlev_full", intMapper);
         addMappedHeader(1, "all_rxlev_sub","All-RxLev Sub", "all_rxlev_sub", intMapper);
         addMappedHeader(1, "all_rxqual_full","All-RxQual Full", "all_rxqual_full", intMapper);
         addMappedHeader(1, "all_rxqual_sub","All-RxQual Sub", "all_rxqual_sub", intMapper);
         addMappedHeader(1, "all_sqi","All-SQI", "all_sqi", intMapper);
         PropertyMapper floatMapper = new PropertyMapper() {
             
             @Override
             public Object mapValue(String value) {
                 try {
                     return Float.parseFloat(value);
                 } catch (NumberFormatException e) {
                     error(e.getLocalizedMessage());
                     return 0L;
                 }
             }
         };
         addMappedHeader(1, "all_sqi_mos","All-SQI MOS", "all_sqi_mos", floatMapper);
     }
 
     private void addDriveIndexes() {
         try {
             String virtualDatasetName = DriveTypes.MS.getFullDatasetName(dataset);
             
             addIndex(NodeTypes.M.getId(), NeoUtils.getTimeIndexProperty(dataset));
             addIndex(INeoConstants.HEADER_MS, NeoUtils.getTimeIndexProperty(virtualDatasetName));
             addIndex(NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(dataset));
             addMappedIndex(MS_KEY, NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(virtualDatasetName));
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * After all lines have been parsed, this method is called. In this loader we save remaining
      * cached data, and call the super method to finalize saving of data to gis node and the
      * properties map.
      */
     protected void finishUp() {
         saveData();
         super.finishUp();
     }
 
     protected void parseLine(String line) {
 
         // debug(line);
         List<String> fields = splitLine(line);
         if (fields.size() < 2)
             return;
         if (this.isOverLimit())
             return;
         Map<String, Object> lineData = makeDataMap(fields);
         // debug(line);
 
         this.time = lineData.get("time").toString();
         this.timestamp = getTimeStamp(1, (Date)lineData.get("timestamp"));
         String ms = (String)lineData.get("ms");
         event = (String)lineData.get("event"); // currently only getting this as a change
 
         // marker
         String message_type = (String)lineData.get("message_type"); // need this to filter for only      
         
         this.incValidMessage();
         
         Float latitude = (Float)lineData.get("latitude");
         Float longitude = (Float)lineData.get("longitude");
         if (time == null || latitude == null || longitude == null) {
             return;
         }
         if ((latitude != null)
                 && (longitude != null)
                 && (((currentLatitude == null) && (currentLongitude == null)) || ((Math.abs(currentLatitude - latitude) > 10E-10) || (Math
                         .abs(currentLongitude - longitude) > 10E-10)))) {
             currentLatitude = latitude;
             currentLongitude = longitude;
             saveData(); // persist the current data to database
         }
         if (!lineData.isEmpty()) {
             data.add(lineData);
         }
         this.incValidLocation();
 
         if (!"EV-DO Pilot Sets Ver2".equals(message_type))
             return;
         int channel = 0;
         int pn_code = 0;
         int ec_io = 0;
         int measurement_count = 0;
         try {
             channel = (Integer)(lineData.get("all_active_set_channel_1"));
             pn_code = (Integer)(lineData.get("all_active_set_pn_1"));
             ec_io = (Integer)(lineData.get("all_active_set_ec_io_1"));
             measurement_count = (Integer)(lineData.get("all_pilot_set_count"));
         } catch (Exception e) {
             error("Failed to parse a field on line " + lineNumber + ": " + e.getMessage());
             return;
         }
         if (measurement_count > 12) {
             error("Measurement count " + measurement_count + " > 12");
             measurement_count = 12;
         }
         boolean changed = false;
         if (!ms.equals(this.previous_ms)) {
             changed = true;
             this.previous_ms = ms;
         }
         if (!this.time.equals(this.previous_time)) {
             changed = true;
             this.previous_time = this.time;
         }
         if (pn_code != this.previous_pn_code) {
             if (this.previous_pn_code >= 0) {
                 error("SERVER CHANGED");
             }
             changed = true;
             this.previous_pn_code = pn_code;
         }
         if (measurement_count > 0 && (changed || (event != null && event.length() > 0))) {
             if (this.isOverLimit())
                 return;
             if (first_line == 0)
                 first_line = lineNumber;
             last_line = lineNumber;
             this.incValidChanged();
             debug(time + ": server channel[" + channel + "] pn[" + pn_code + "] Ec/Io[" + ec_io + "]\t" + event + "\t"
                     + this.currentLatitude + "\t" + this.currentLongitude);
             for (int i = 1; i <= measurement_count; i++) {
                 // Delete invalid data, as you can have empty ec_io
                 // zero ec_io is correct, but empty ec_io is not
                 try {
                     ec_io = (Integer)(lineData.get("all_pilot_set_ec_io_" + i));
                     channel = (Integer)(lineData.get("all_pilot_set_channel_" + i));
                     pn_code = (Integer)(lineData.get("all_pilot_set_pn_" + i));
                     debug("\tchannel[" + channel + "] pn[" + pn_code + "] Ec/Io[" + ec_io + "]");
                     addStats(pn_code, ec_io);
                     String chan_code = "" + channel + "\t" + pn_code;
                     if (!signals.containsKey(chan_code))
                         signals.put(chan_code, new float[2]);
                     signals.get(chan_code)[0] += LoaderUtils.dbm2mw(ec_io);
                     signals.get(chan_code)[1] += 1;
                 } catch (Exception e) {
                     error("Error parsing column " + i + " for EC/IO, Channel or PN: " + e.getMessage());
                 }
             }
         }
     }
 
     /**
      * This method is called to dump the current cache of signals as one located point linked to a
      * number of signal strength measurements.
      */
     private void saveData() {
         if (signals.size() > 0 || !data.isEmpty()) {
             Transaction transaction = neo.beginTx();
             try {
 
                 if (!data.isEmpty()) {
                     Node mp = neo.createNode();
                     if (timestamp != 0) {
                         mp.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, this.timestamp);
                         updateTimestampMinMax(1, timestamp);
                     }
                     mp.setProperty(INeoConstants.PROPERTY_FIRST_LINE_NAME, first_line);
                     mp.setProperty(INeoConstants.PROPERTY_LAST_LINE_NAME, last_line);
                     mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, currentLatitude.doubleValue());
                     mp.setProperty(INeoConstants.PROPERTY_LON_NAME, currentLongitude.doubleValue());
                     mp.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.MP.getId());
                     index(mp);
                     boolean haveEvents = false;
                     for (Map<String, Object> dataLine : data) {
                         Node m = neo.createNode();
                         findOrCreateFileNode(m);
                         m.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.M.getId());
                         for (Map.Entry<String, Object> entry : dataLine.entrySet()) {
                             if (entry.getKey().equals(INeoConstants.SECTOR_ID_PROPERTIES)) {
                                 mp.setProperty(INeoConstants.SECTOR_ID_PROPERTIES, entry.getValue());
                                 // ms.setProperty(INeoConstants.SECTOR_ID_PROPERTIES,
                                 // entry.getValue());
                             } else if ("timestamp".equals(entry.getKey())) {
                                 long timeStamp = getTimeStamp(1, ((Date)entry.getValue()));
                                 if (timeStamp != 0) {
                                     m.setProperty(entry.getKey(), timeStamp);
                                     mp.setProperty(entry.getKey(), timeStamp);
                                 }
                             } else {
                                 m.setProperty(entry.getKey(), entry.getValue());
                                 haveEvents = haveEvents || INeoConstants.PROPERTY_TYPE_EVENT.equals(entry.getKey());
                             }
                         }
                         // debug("\tAdded measurement: " + propertiesString(ms));
                         m.createRelationshipTo(mp, GeoNeoRelationshipTypes.LOCATION);
                         if (mNode != null) {
                             mNode.createRelationshipTo(m, GeoNeoRelationshipTypes.NEXT);
                         }
                         m.setProperty(INeoConstants.PROPERTY_NAME_NAME, getMNodeName(dataLine));
                         mNode = m;
                         index(m);
                     }
                     if (haveEvents) {
                         index.index(mp, INeoConstants.EVENTS_LUCENE_INDEX_NAME, dataset);
                     }
                     GisProperties gisProperties = getGisProperties(dataset);
                     gisProperties.updateBBox(currentLatitude, currentLongitude);
                     gisProperties.checkCRS(currentLatitude, currentLongitude, null);
                     gisProperties.incSaved();
                 }
                 if (!signals.isEmpty()) {
                     Node mp = neo.createNode();
                     if (timestamp != 0) {
                         mp.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, this.timestamp);
                         updateTimestampMinMax(1, timestamp);
                     }
                     mp.setProperty(INeoConstants.PROPERTY_FIRST_LINE_NAME, first_line);
                     mp.setProperty(INeoConstants.PROPERTY_LAST_LINE_NAME, last_line);
                     mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, currentLatitude.doubleValue());
                     mp.setProperty(INeoConstants.PROPERTY_LON_NAME, currentLongitude.doubleValue());
                     mp.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.MP.getId());
                     index(MS_KEY, mp);
 
                     LinkedHashMap<String, Header> statisticHeader = getHeaderMap(2).headers;
                     if (statisticHeader.isEmpty()) {
                         Header header = new Header(INeoConstants.PRPOPERTY_CHANNEL_NAME, INeoConstants.PRPOPERTY_CHANNEL_NAME, 0);
                         header = new IntegerHeader(header);
                         statisticHeader.put(INeoConstants.PRPOPERTY_CHANNEL_NAME, header);
                         header = new Header(INeoConstants.PROPERTY_CODE_NAME, INeoConstants.PROPERTY_CODE_NAME, 0);
                         header = new IntegerHeader(header);
                         statisticHeader.put(INeoConstants.PROPERTY_CODE_NAME, header);
                         header = new Header(INeoConstants.PROPERTY_DBM_NAME, INeoConstants.PROPERTY_DBM_NAME, 0);
                         header = new FloatHeader(header);
                         statisticHeader.put(INeoConstants.PROPERTY_DBM_NAME, header);
                         header = new Header(INeoConstants.PROPERTY_MW_NAME, INeoConstants.PROPERTY_MW_NAME, 0);
                         header = new FloatHeader(header);
                         statisticHeader.put(INeoConstants.PROPERTY_MW_NAME, header);
                     }
                     TreeMap<Float, String> sorted_signals = new TreeMap<Float, String>();
                     for (String chanCode : signals.keySet()) {
                         float[] signal = signals.get(chanCode);
                         sorted_signals.put(signal[1] / signal[0], chanCode);
                     }
                     for (Map.Entry<Float, String> entry : sorted_signals.entrySet()) {
                         String chanCode = entry.getValue();
                         float[] signal = signals.get(chanCode);
                         double mw = signal[0] / signal[1];
                         Node ms = neo.createNode();
                         String[] cc = chanCode.split("\\t");
 
                         ms.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.HEADER_MS);
                         Header header = statisticHeader.get(INeoConstants.PRPOPERTY_CHANNEL_NAME);
                         Object valueToSave = header.parse(cc[0]);
                         ms.setProperty(INeoConstants.PRPOPERTY_CHANNEL_NAME, valueToSave);
                         header = statisticHeader.get(INeoConstants.PROPERTY_CODE_NAME);
                         valueToSave = header.parse(cc[1]);
                         ms.setProperty(INeoConstants.PROPERTY_CODE_NAME, valueToSave);
                         ms.setProperty(INeoConstants.PROPERTY_NAME_NAME, cc[1]);
                         float dbm = LoaderUtils.mw2dbm(mw);
                         header = statisticHeader.get(INeoConstants.PROPERTY_DBM_NAME);
                         valueToSave = header.parse(String.valueOf(dbm));
                         ms.setProperty(INeoConstants.PROPERTY_DBM_NAME, valueToSave);
 
                         header = statisticHeader.get(INeoConstants.PROPERTY_MW_NAME);
                         valueToSave = header.parse(String.valueOf(mw));
                         ms.setProperty(INeoConstants.PROPERTY_MW_NAME, mw);
                         debug("\tAdded measurement: " + propertiesString(ms));
                         if (timestamp != 0) {
                             ms.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, this.timestamp);
                             updateTimestampMinMax(2, timestamp);
                         }
                         index(ms);
                         findOrCreateVirtualFileNode(ms);
                         GisProperties gisProperties = getGisProperties(getVirtualDatasetName());
                         gisProperties.incSaved();
                         if (virtualMnode != null) {
                             virtualMnode.createRelationshipTo(ms, GeoNeoRelationshipTypes.NEXT);
                         }
 
                         virtualMnode = ms;
                         ms.createRelationshipTo(mp, GeoNeoRelationshipTypes.LOCATION);
                     }
                     GisProperties gisProperties = getGisProperties(getVirtualDatasetName());
                     gisProperties.updateBBox(currentLatitude, currentLongitude);
                     gisProperties.checkCRS(currentLatitude, currentLongitude, null);
                 }
 
                 transaction.success();
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 transaction.finish();
             }
         }
         signals.clear();
         data.clear();
         first_line = 0;
         last_line = 0;
     }
 
     /**
      *get name of virtual dataset
      * 
      * @return
      */
     private String getVirtualDatasetName() {
         if (virtualDatasetName == null) {
             virtualDatasetName = DriveTypes.MS.getFullDatasetName(dataset);
         }
         return virtualDatasetName;
     }
 
     /**
      * get name of m node
      * 
      * @param dataLine - node data
      * @return node name
      */
     private Object getMNodeName(Map<String, Object> dataLine) {
        return "m";
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         if (args.length < 1)
             args = new String[] {"amanzi/test.FMT", "amanzi/0904_90.FMT", "amanzi/0905_22.FMT", "amanzi/0908_44.FMT"};
         EmbeddedGraphDatabase neo = new EmbeddedGraphDatabase("../../testing/neo");
         try {
             for (String filename : args) {
                 TEMSLoader driveLoader = new TEMSLoader(neo, filename);
                 driveLoader.setLimit(100);
                 driveLoader.run(null);
                 driveLoader.printStats(true); // stats for this load
             }
             printTimesStats(); // stats for all loads
         } catch (IOException e) {
             System.err.println("Error loading TEMS data: " + e);
             e.printStackTrace(System.err);
         } finally {
             neo.shutdown();
         }
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         String datasetName = null;
         if (key == 1) {
             datasetName = dataset;
         }
         else {
             datasetName = getVirtualDatasetName();
         }
         
         GisProperties gisProperties = gisNodes.get(datasetName);
 		return gisProperties==null?null:gisProperties.getGis();
     }
     
     @Override
     protected ArrayList<Node> getGisNodes() {
         ArrayList<Node> result = new ArrayList<Node>();
         
         Transaction transaction = neo.beginTx();
         try {
             Iterator<Node> gisNodes = datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
             
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return NeoUtils.isGisNode(currentPos.currentNode());
                 }   
             }, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING,
                GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).iterator();
         
             while (gisNodes.hasNext()) {
                 result.add(gisNodes.next());            
             }
         }
         catch (Exception e) {
             NeoCorePlugin.error(null, e);
             transaction.failure();
         }
         finally {
             transaction.finish();
         }
         
         return result;
     }
 }
