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
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.database.services.events.UpdateViewEventType;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.SectorIdentificationType;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 public class RomesLoader extends DriveLoader {
     private Node mNode = null;
     private int first_line = 0;
     private int last_line = 0;
     private Float currentLatitude = null;
     private Float currentLongitude = null;
     private String time = null;
     // private long timestamp = 0L;
     private final ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
     private String luceneIndexName;
     /**
      * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
      * 
      * @param filename of file to load
      * @param display for opening message dialogs
      * @param dataset to add data to
      */
     public RomesLoader(Calendar workTime, String filename, Display display, String dataset) {
         _workDate = workTime;
         driveType = DriveTypes.ROMES;
         initialize("Romes", null, filename, display, dataset);
         initData();
         initializeLuceneIndex();
         initializeKnownHeaders();
         addDriveIndexes();
     }
 
     /**
      *initialize start date
      */
     private void initData() {
         if (_workDate != null) {
             return;
         }
         Pattern p = Pattern.compile(".*_(\\d{6})_.*");
         Matcher m = p.matcher(filename);
         Date date;
         if (m.matches()) {
             String dateText = m.group(1);
             try {
                 date = (new SimpleDateFormat("yyMMdd")).parse(dateText);
             } catch (ParseException e) {
                 NeoLoaderPlugin.error("Wrong filename format: " + filename);
                 // TODO ask user?
                 date = new Date(new File(filename).lastModified());
             }
         } else {
             NeoLoaderPlugin.error("Wrong filename format: " + filename);
             // TODO ask user?
             date = new Date(new File(filename).lastModified());
         }
         _workDate = new GregorianCalendar();
         _workDate.setTime(date);
     }
 
     /**
      * Constructor for loading data in test mode, with no display and NeoService passed
      * 
      * @param neo database to load data into
      * @param filename of file to load
      * @param display
      */
     public RomesLoader(GraphDatabaseService neo, String filename, String datasetName, LuceneIndexService anIndex) {
         initialize("Romes", neo, filename, null, datasetName);
         driveType = DriveTypes.ROMES;
         initData();
         if (anIndex == null) {
             initializeLuceneIndex();
         } else {
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
         addKnownHeader(1, "time", "time.*", false);
         addKnownHeader(1, "latitude", ".*latitude.*", false);
         addKnownHeader(1, "longitude", ".*longitude.*", false);
         addMappedHeader(1, "events", "Event Type", "event_type", new PropertyMapper() {
 
             @Override
             public Object mapValue(String originalValue) {
                 return originalValue.replaceAll("HO Command.*", "HO Command");
             }
         });
         addMappedHeader(1, "time", "Timestamp", "timestamp", new DateMapper("HH:mm:ss"));
         addKnownHeader(1, INeoConstants.SECTOR_ID_PROPERTIES, ".*Server.*Report.*CI.*", true);
         addNonDataHeaders(1, Arrays.asList(new String[] {"timestamp"}));
         dropHeaderStats(1, new String[] {"time", "timestamp", "latitude", "longitude"/*, INeoConstants.SECTOR_ID_PROPERTIES*/});
         addIdentityHeaders(1, Arrays.asList(new String[]{INeoConstants.SECTOR_ID_PROPERTIES}));
     }
 
     private void addDriveIndexes() {
         try {
             addIndex(NodeTypes.M.getId(), NeoUtils.getTimeIndexProperty(dataset));
             addIndex(NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(dataset));
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     @Override
     protected void parseLine(String line) {
         try {
             // debug(line);
             List<String> fields = splitLine(line);
             if (fields.size() < 2)
                 return;
             if (this.isOverLimit())
                 return;
             this.incValidMessage(); // we have not filtered the message out on non-accepted content
             this.incValidChanged(); // we have not filtered the message out on lack of data change
             if (first_line == 0)
                 first_line = lineNumber;
             last_line = lineNumber;
             Map<String, Object> lineData = makeDataMap(fields);
             this.time = (String)lineData.get("time");
             // Date nodeDate = (Date)lineData.get("timestamp");
             // this.timestamp = getTimeStamp(nodeDate);
             Float latitude = (Float)lineData.get("latitude");
             Float longitude = (Float)lineData.get("longitude");
             if (time == null || latitude == null || longitude == null) {
                 return;
             }
             if ((latitude != null)
                     && (longitude != null)
                     && (((currentLatitude == null) && (currentLongitude == null)) || ((Math.abs(currentLatitude - latitude) > 10E-10) || (Math.abs(currentLongitude
                             - longitude) > 10E-10)))) {
                 currentLatitude = latitude;
                 currentLongitude = longitude;
                 saveData(); // persist the current data to database
             }
             this.incValidLocation(); // we have not filtered the message out on lack of location
             if (lineData.size() > 0) {
                 data.add(lineData);
             }
         } catch (Exception e) {
             // TODO: handle exception
             e.printStackTrace();
         }
     }
 
     /**
      * After all lines have been parsed, this method is called. In this loader we save remaining
      * cached data, and call the super method to finalize saving of data to gis node and the
      * properties map.
      */
     @Override
     protected void finishUp() {
        getStoringNode(1).setProperty(INeoConstants.SECTOR_ID_TYPE, SectorIdentificationType.CI.toString());
         saveData();
         super.finishUp();
         if (!isTest()) {
             sendUpdateEvent(UpdateViewEventType.GIS);
         }
     }
 
     /**
      * This method is called to dump the current cache of signals as one located point linked to a
      * number of signal strength measurements.
      */
     private void saveData() {
         if (data.size() > 0) {
             Transaction transaction = neo.beginTx();
             try {
                 Node mp = neo.createNode();
                 mp.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.MP.getId());
                 mp.setProperty(INeoConstants.PROPERTY_TIME_NAME, this.time);
                 mp.setProperty(INeoConstants.PROPERTY_FIRST_LINE_NAME, first_line);
                 mp.setProperty(INeoConstants.PROPERTY_LAST_LINE_NAME, last_line);
                 mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, currentLatitude.doubleValue());
                 mp.setProperty(INeoConstants.PROPERTY_LON_NAME, currentLongitude.doubleValue());
                 index(mp);
 
                 // debug("Added measurement point: " + propertiesString(mp));
 
                 boolean haveEvents = false;
                 for (Map<String, Object> dataLine : data) {
                     Node m = neo.createNode();
                     findOrCreateFileNode(m);
                     m.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.M.getId());
                     for (Map.Entry<String, Object> entry : dataLine.entrySet()) {
                         if (entry.getKey().equals(INeoConstants.SECTOR_ID_PROPERTIES)) {
                             m.setProperty(INeoConstants.SECTOR_ID_PROPERTIES, entry.getValue());
                             // Pechko_E: index sector_id property for correlation
                             index.index(m, getSectorIDIndexName(), entry.getValue());
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
                     m.createRelationshipTo(mp, GeoNeoRelationshipTypes.LOCATION);
                     if (mNode != null) {
                         mNode.createRelationshipTo(m, GeoNeoRelationshipTypes.NEXT);
                     }
                     m.setProperty(INeoConstants.PROPERTY_NAME_NAME, getMNodeName(dataLine));
                     mNode = m;
                     index(m);
                     storingProperties.values().iterator().next().incSaved();
                 }
                 if (haveEvents) {
                     index.index(mp, INeoConstants.EVENTS_LUCENE_INDEX_NAME, dataset);
                 }
                 GisProperties gisProperties = getGisProperties(dataset);
                 gisProperties.updateBBox(currentLatitude, currentLongitude);
                 gisProperties.checkCRS(currentLatitude, currentLongitude, null);
                 gisProperties.incSaved();
                 
                 transaction.success();
             } finally {
                 transaction.finish();
             }
         }
         data.clear();
     }
 
     private String getSectorIDIndexName() {
         if (luceneIndexName!=null){
             return luceneIndexName;
         }
         return luceneIndexName = NeoUtils.getLuceneIndexKeyByProperty(datasetNode, INeoConstants.SECTOR_ID_PROPERTIES, NodeTypes.M);
     }
 
     /**
      * get name of m node
      * 
      * @param dataLine - node data
      * @return node name
      */
     private String getMNodeName(Map<String, Object> dataLine) {
         // TODO check name of node
         Object timeNode = dataLine.get("time");
         return timeNode == null ? "ms node" : timeNode.toString();
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         NeoLoaderPlugin.debug = false;
         if (args.length < 1)
             args = new String[] {"amanzi/G_YA004_090723_W09_Test.ASC"};
         EmbeddedGraphDatabase neo = new EmbeddedGraphDatabase("../../testing/neo");
         try {
             for (String filename : args) {
                 RomesLoader driveLoader = new RomesLoader(neo, filename, null, null);
                 driveLoader.setLimit(5000);
                 driveLoader.run(null);
                 driveLoader.printStats(true); // stats for this load
             }
             printTimesStats(); // stats for all loads
         } catch (IOException e) {
             System.err.println("Error loading Romes data: " + e);
             e.printStackTrace(System.err);
         } finally {
             neo.shutdown();
         }
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
 		return datasetNode;
     }
 
     @Override
     protected String getPrymaryType(Integer key) {
         return NodeTypes.M.getId();
     }
 }
