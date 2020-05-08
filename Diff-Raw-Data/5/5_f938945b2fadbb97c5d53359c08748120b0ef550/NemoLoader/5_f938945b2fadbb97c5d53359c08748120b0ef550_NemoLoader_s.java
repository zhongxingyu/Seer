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
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.database.services.events.UpdateViewEventType;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.MeasurementRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.DriveEvents;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.apache.log4j.Logger;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 
 /**
  * <p>
  * Nemo loader (new version nemo files)
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class NemoLoader extends DriveLoader {
     private List<Map<String, Object>> subNodes = null;
     private static final Logger LOGGER = Logger.getLogger(NemoLoader.class);
     protected static final SimpleDateFormat EVENT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
 
     /** String EVENT_ID field */
     protected static final String EVENT_ID = INeoConstants.EVENT_ID;
     /** String TIME_FORMAT field */
     protected static final String TIME_FORMAT = "HH:mm:ss.S";
     private static final String MM_KEY = "mm";
     protected char fieldSepRegex;
     protected Node pointNode;
     protected Pair<Node, Node> pairNode;
     protected Node parentMnode;
     protected SimpleDateFormat timeFormat;
     protected LinkedHashMap<String, Header> headers;
     private final LinkedHashMap<String, Header> headersVirt;
     private Node virtualMnode = null;
     protected Float curLat;
     protected Float curLon;
 
     // protected Node msNode;
 
     /**
      * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
      * 
      * @param filename of file to load
      * @param display for opening message dialogs
      * @param dataset to add data to
      */
     public NemoLoader(Calendar time, String filename, Display display, String dataset) {
         _workDate = time;
         driveType = DriveTypes.NEMO2;
         initialize("Nemo", null, filename, display, dataset);
         headers = getHeaderMap(1).headers;
         headersVirt = getHeaderMap(2).headers;
         timeFormat = new SimpleDateFormat(TIME_FORMAT);
         pointNode = null;
         initializeKnownHeaders();
         addDriveIndexes();
         initializeLuceneIndex();
         possibleFieldSepRegexes = new String[] {"\t", ",", ";"};
 
     }
     
     /**
     * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
     * @param time date to which time data will be appended
     * @param filename of file to load
     * @param dataset to add data to
     */
     public NemoLoader(final Calendar time,final String filename,final String dataset,final GraphDatabaseService neo)
     {
         _workDate = time;
         driveType = DriveTypes.NEMO2;
         initialize("Nemo", neo , filename, null, dataset);
         headers = getHeaderMap(1).headers;
         headersVirt = getHeaderMap(2).headers;
         timeFormat = new SimpleDateFormat(TIME_FORMAT);
         pointNode = null;
         initializeKnownHeaders();
         addDriveIndexes();
         initializeLuceneIndex();
         possibleFieldSepRegexes = new String[] {"\t", ",", ";"};
     }
     @Override
     protected void initializeLuceneIndex() {
         index = NeoServiceProvider.getProvider().getIndexService();
     } 
     /**
      * initialize headers
      */
     protected void initializeKnownHeaders() {
         headers.put(INeoConstants.PROPERTY_TYPE_EVENT, new StringHeader(new Header(INeoConstants.PROPERTY_TYPE_EVENT, INeoConstants.PROPERTY_TYPE_EVENT, 0)));
         // headers.put(TIME, new Header(TIME, TIME, 1));
         // MappedHeaderRule mapRule = new MappedHeaderRule("timestamp", TIME, new
         // DateTimeMapper(TIME_FORMAT));
         // headers.put(mapRule.key, new MappedHeader(headers.get(TIME), mapRule));
 
     }
 
     @Override
     protected void parseLine(String line) {
         if (parser == null) {
             determineFieldSepRegex(line);
         }
 
         List<String> parsedLine = splitLine(line);
         if (parsedLine.size() < 1) {
             return;
         }
         Event event = new Event(parsedLine);
         try {
             event.analyseKnownParameters(headers);
         } catch (Exception e) {
             e.printStackTrace();
             NeoLoaderPlugin.error(e.getLocalizedMessage());
             return;
         }
 
         String eventId = event.eventId;
         createMNode(event);
         if ("GPS".equalsIgnoreCase(eventId)) {
             createPointNode(event);
             return;
         }
     }
 
     /**
      * create mp node
      * 
      * @param event - event
      */
     protected void createPointNode(Event event) {
         Transaction transaction = neo.beginTx();
         try {
             Float lon = (Float)event.parsedParameters.get("lon");
             Float lat = (Float)event.parsedParameters.get("lat");
             String time = event.time;
             if ((lon == null || lat == null) ||
                 (lon == 0) && (lat == 0)) {
                 return;
             }
             Node mp = neo.createNode();
             mp.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.MP.getId());
             mp.setProperty(INeoConstants.PROPERTY_TIME_NAME, time);
 
             mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, lat.doubleValue());
             mp.setProperty(INeoConstants.PROPERTY_LON_NAME, lon.doubleValue());
             GisProperties gisProperties = getGisProperties(dataset);
             gisProperties.updateBBox(lat, lon);
             gisProperties.checkCRS(lat, lon, null);
             index(mp);
             transaction.success();
             pointNode = mp;
             curLat = lat;
             curLon = lon;
         } catch (Exception e) {
             NeoLoaderPlugin.error(e.getLocalizedMessage());
             return;
         } finally {
             transaction.finish();
         }
     }
 
     /**
      * create ms node
      * 
      * @param event - event
      */
     protected void createMNode(Event event) {
         Transaction transaction = neo.beginTx();
         long timestamp;
         try {
             String id = event.eventId;// getEventId(event);
             String time = event.time;// getEventTime(event);
 
             try {
                 timestamp = getTimeStamp(1, timeFormat.parse(time));
             } catch (ParseException e) {
                 // some parameters do not have time
                 // NeoLoaderPlugin.error(e.getLocalizedMessage());
                 timestamp = 0;
             }
             Node ms = neo.createNode();
 
             findOrCreateFileNode(ms);
             event.store(ms, headers);
             ms.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.M.getId());
             if (timestamp != 0) {
                 ms.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
             }
             if (parentMnode != null) {
                 parentMnode.createRelationshipTo(ms, MeasurementRelationshipTypes.NEXT);
             }
             if (pointNode != null) {
                 ms.createRelationshipTo(pointNode, GeoNeoRelationshipTypes.LOCATION);
                 if (timestamp != 0) {
                     pointNode.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
                 }
                 if (event.driveEvents!=null){
                     index.index(pointNode, INeoConstants.EVENTS_LUCENE_INDEX_NAME, dataset);
                 }
             }
             ms.setProperty(INeoConstants.PROPERTY_NAME_NAME, id);
             index(ms);
             parentMnode = ms;
             transaction.success();
         } catch (Exception e) {
             e.printStackTrace();
             NeoLoaderPlugin.error(e.getLocalizedMessage());
             return;
         } finally {
             transaction.finish();
         }
         createSubNodes(event, timestamp);
     }
 
     protected void createSubNodes(Event event, long timestamp) {
         if (subNodes == null) {
             return;
         }
         Transaction tx = neo.beginTx();
         try {
             for (Map<String, Object> propertyMap : subNodes) {
                 Iterator<Entry<String, Object>> iter = propertyMap.entrySet().iterator();
                 while (iter.hasNext()) {
                     Entry<String, Object> entry = iter.next();
                     if (entry.getValue() == null) {
                         iter.remove();
                     }
                 }
                 if (propertyMap.isEmpty()) {
                     continue;
                 }
                 try {
                     Node mm = neo.createNode();
                     if (timestamp != 0) {
                         mm.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
                         updateTimestampMinMax(2, timestamp);
                     }
                     findOrCreateVirtualFileNode(mm);
                    NodeTypes.MM.setNodeType(mm, neo);
                     mm.setProperty(INeoConstants.PROPERTY_NAME_NAME, event.eventId);
                     findOrCreateVirtualFileNode(mm);
                     if (virtualMnode != null) {
                         virtualMnode.createRelationshipTo(mm, GeoNeoRelationshipTypes.NEXT);
                     }
                     virtualMnode = mm;
                     for (String key : propertyMap.keySet()) {
                         
                         Object parsedValue = propertyMap.get(key);
                         if (parsedValue!=null&&parsedValue.getClass().isArray()){
                             setProperty(mm, key, parsedValue);
                         }else{
                             setIndexProperty(headersVirt, mm, key, parsedValue);
                         }
                     }
 
                     index(mm);
                     if (pointNode != null) {
                         mm.createRelationshipTo(pointNode, GeoNeoRelationshipTypes.LOCATION);
                     }
                 } catch (Exception e) {
                     NeoLoaderPlugin.exception(e);
                     e.printStackTrace();
                 }
             }
 
         } finally {
             tx.finish();
             subNodes = null;
         }
     }
 
     /**
      * Get event time
      * 
      * @param parsedLine - list of fields
      * @return String
      */
     protected String getEventTime(List<String> parsedLine) {
         return parsedLine.get(1);
     }
 
     /**
      * Get event id
      * 
      * @param parsedLine - list of fields
      * @return String
      */
     protected String getEventId(List<String> parsedLine) {
         return parsedLine.get(0);
     }
 
     /**
      * add index
      */
     private void addDriveIndexes() {
         try {
             addIndex(NodeTypes.M.getId(), NeoUtils.getTimeIndexProperty(dataset));
             addIndex(NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(dataset));
             String virtualDatasetName = DriveTypes.MS.getFullDatasetName(dataset);
             
             addIndex(NodeTypes.MM.getId(), NeoUtils.getTimeIndexProperty(virtualDatasetName));
             addMappedIndex(MM_KEY, NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(virtualDatasetName));
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * <p>
      * Event - provide information about command (1 row from log file)
      * </p>
      * 
      * @author cinkel_a
      * @since 1.0.0
      */
     public class Event {
         /** SimpleDateFormat EVENT_DATE_FORMAT field */
         protected String eventId;
         protected String time;
         protected List<Integer> contextId = new ArrayList<Integer>();
         protected List<String> parameters;
         protected Map<String, Object> parsedParameters;
         protected NemoEvents event;
         DriveEvents driveEvents=null;
 
         /**
          * constructor
          */
         public Event(List<String> parcedLine) {
             parsedParameters = new LinkedHashMap<String, Object>();
             parse(parcedLine);
         }
 
         /**
          *parse line
          * 
          * @param parcedLine - list of string tag
          */
         protected void parse(List<String> parcedLine) {
             eventId = parcedLine.get(0);
             event = NemoEvents.getEventById(eventId);
             time = parcedLine.get(1);
             String numberContextId = parcedLine.get(2);
             contextId.clear();
             Integer firstParamsId = 3;
             if (!numberContextId.isEmpty()) {
                 int numContext = Integer.parseInt(numberContextId);
                 for (int i = 1; i <= numContext; i++) {
                     int value = 0;
                     String field = parcedLine.get(firstParamsId++);
                     if (!field.isEmpty()) {
                         try {
                             value = Integer.parseInt(field);
                         } catch (NumberFormatException e) {
                             // TODO Handle NumberFormatException
                             NeoLoaderPlugin.error("Wrong context id:" + field);
                             value = 0;
                         }
                     }
                     contextId.add(value);
                 }
             }
             parameters = new ArrayList<String>();
             for (int i = firstParamsId; i < parcedLine.size(); i++) {
                 parameters.add(parcedLine.get(i));
             }
         }
 
         /**
          *create parsedParameters - list of parsed parameters of event.
          */
         @SuppressWarnings("unchecked")
         protected void analyseKnownParameters(Map<String, Header> statisticHeaders) {
             if (parameters.isEmpty()) {
                 return;
             }
 
             if (event == null) {
                 return;
             }
             Map<String, Object> parParam;
             try {
                 parParam = event.fill(getVersion(), parameters);
             } catch (Exception e1) {
                 LOGGER.debug(eventId);
                 LOGGER.debug(parameters.toString());
                 // TODO Handle Exception
                 throw (RuntimeException) new RuntimeException( ).initCause( e1 );
             }
             if (parParam.isEmpty()) {
                 return;
             }
             driveEvents=(DriveEvents)parParam.remove(NemoEvents.DRIVE_EVENTS);
             subNodes = (List<Map<String, Object>>)parParam.remove(NemoEvents.SUB_NODES);
             // add context field
             if (parParam.containsKey(NemoEvents.FIRST_CONTEXT_NAME)) {
                 List<String> contextName = (List<String>)parParam.get(NemoEvents.FIRST_CONTEXT_NAME);
                 parParam.remove(NemoEvents.FIRST_CONTEXT_NAME);
                 for (int i = 0; i < contextId.size() && i < contextName.size(); i++) {
                     if (contextId.get(i) != 0) {
                         parParam.put(contextName.get(i), contextId.get(i));
                     }
                 }
             }
             if (_workDate == null && event == NemoEvents.START) {
                 _workDate = new GregorianCalendar();
                 Date date;
                 try {
                     date = EVENT_DATE_FORMAT.parse((String)parParam.get("Date"));
 
                 } catch (Exception e) {
                     NeoLoaderPlugin.error("Wrong time format" + e.getLocalizedMessage());
                     date = new Date(new File(filename).lastModified());
                 }
                 _workDate.setTime(date);
             }
             //Pechko_E make property names Ruby-compatible
            Set<Entry<String, Object>> entrySet = parParam.entrySet();
            //TODO Check may be a new map is unnecessary and we can use parsedParameters
            Map<String, Object> parParamCleaned=new HashMap<String, Object>(parParam.size());
            for (Entry<String, Object> entry:entrySet){
                parParamCleaned.put(AbstractLoader.cleanHeader(entry.getKey()), entry.getValue());
            }
            parsedParameters.putAll(parParamCleaned);
             if (statisticHeaders == null) {
                 return;
             }
             for (String key : parParamCleaned.keySet()) {
                 if (!statisticHeaders.containsKey(key)) {
                     Object value = parParamCleaned.get(key);
                     if (value == null) {
                         continue;
                     }
                     Header header = new Header(key, key, 0);
                     if (value instanceof Float) {
                         header = new FloatHeader(header);
                     } else if (value instanceof Integer) {
                         header = new IntegerHeader(header);
                     } else if (value instanceof String) {
                         header = new StringHeader(header);
                     } else {
                         continue;
                     }
                     statisticHeaders.put(key, header);
                 }
             }
         }
 
         /**
          * get version of file format
          * 
          * @return ff version
          */
         protected String getVersion() {
             return "2.01";
         }
 
         /**
          * store all properties in ms node
          * 
          * @param msNode - ms node
          * @param statisticHeaders - statistic headers
          */
         public void store(Node msNode, Map<String, Header> statisticHeaders) {
             storeProperties(msNode, INeoConstants.PROPERTY_TYPE_EVENT, eventId, statisticHeaders);
             if (driveEvents != null) {
                 driveEvents.setEventType(msNode, neo);
             }
             storeProperties(msNode, INeoConstants.PROPERTY_TIME_NAME, time, statisticHeaders);
             storeProperties(msNode, INeoConstants.EVENT_CONTEXT_ID, contextId.toArray(new Integer[0]), null);
             for (String key : parsedParameters.keySet()) {
                 storeProperties(msNode, key, parsedParameters.get(key), statisticHeaders);
             }
         }
 
         /**
          * Store property in node
          * 
          * @param msNode - ms node
          * @param key - key
          * @param value - value
          * @param statisticHeaders - statistic headers
          */
         protected void storeProperties(Node msNode, String key, Object value, Map<String, Header> statisticHeaders) {
             if (value == null) {
                 return;
             }
             Header header = statisticHeaders == null ? null : statisticHeaders.get(key);
             if (header == null) {
                 msNode.setProperty(key, value);
             } else {
                 // TODO remove double parsing
                 Object valueToSave = header.parse(value.toString());
                 if (valueToSave == null) {
                     // NeoLoaderPlugin.info("Not saved key=" + key + "\t value=" + value);
                     return;
                 }
                 msNode.setProperty(key, valueToSave);
             }
             msNode.setProperty(key, value);
         }
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         if (key == 1) {
             return datasetNode;
         } else {
             final String name = DriveTypes.MS.getFullDatasetName(dataset);
             return virtualDatasets.get(name);
         }
     }
     @Override
     protected String getPrymaryType(Integer key) {
         if (key == 1) {
             return NodeTypes.M.getId();
         } else {
            return NodeTypes.MM.getId();
         }
         
     }
     @Override
     protected boolean needParceHeaders() {
         return false;
     }
     
     @Override
     protected void finishUp() {
         super.finishUp();
         if (!isTest()) {
             sendUpdateEvent(UpdateViewEventType.GIS);
         }
     }
 }
