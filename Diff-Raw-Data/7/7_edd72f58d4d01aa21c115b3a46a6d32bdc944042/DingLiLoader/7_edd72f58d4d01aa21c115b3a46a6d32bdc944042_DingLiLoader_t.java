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
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
import org.amanzi.neo.loader.AbstractLoader.PropertyMapper;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.services.GisProperties;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.DriveTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 /**
  * <p>
  * Loader for DingLi log files
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class DingLiLoader extends DriveLoader {
 
     /** The property header map. */
     private Map<String, List<String>> propertyHeaderMap = new HashMap<String, List<String>>();
     private HashSet<String> ignoreHeaders = new HashSet<String>();
     private boolean ignoreHex = true;
     private Node lastMpNode = null;
     private Node mNode;
     private LinkedHashMap<String, Header> headers;
 
     /**
      * Instantiates a new ding li loader. This method is the normal one to be called from within AWE.
      * 
      * @param filename the filename
      * @param display the display
      * @param dataset the dataset
      */
     public DingLiLoader(String filename, Display display, String dataset) {
         this(null,filename,display,dataset);
     }
 
     /**
      * Instantiates a new ding li loader for testing purposes, with no display, but a pre-initialized database service.
      * 
      * @param neo the GraphDatabaseService to save the data to
      * @param filename the filename
      * @param dataset the dataset
      */
     public DingLiLoader(GraphDatabaseService neo, String filename, String dataset) {
         this(neo,filename,null,dataset);
     }
 
     /**
      * Instantiates a new ding li loader.
      * 
      * @param filename the filename
      * @param display the display
      * @param dataset the dataset
      */
     private DingLiLoader(GraphDatabaseService neo, String filename, Display display, String dataset) {
         driveType = DriveTypes.DING_LI;
         initialize("DingLi", neo, filename, display, dataset);
         initializeLuceneIndex();
         addDriveIndexes();
         headers = getHeaderMap(1).headers;
        addKnownHeader(1, "event_type", "EventInfo",false);
         possibleFieldSepRegexes = new String[] {"\t"};
         _workDate = Calendar.getInstance();
         // rounded to begin of day
         _workDate.set(Calendar.MILLISECOND, 0);
         _workDate.set(Calendar.SECOND, 0);
         _workDate.set(Calendar.MINUTE, 0);
         _workDate.set(Calendar.HOUR_OF_DAY, 0);
     }
 
     /**
      * Need parce headers.
      * 
      * @return true, if successful
      */
     @Override
     protected boolean needParceHeaders() {
         return false;
     }
 
     /**
      * Adds the drive indexes.
      */
     private void addDriveIndexes() {
         try {
             addIndex(NodeTypes.M.getId(), NeoUtils.getTimeIndexProperty(dataset));
             addIndex(NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(dataset));
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * Parses the line.
      * 
      * @param line the line
      */
     @Override
     protected int parseLine(String line) {
         try {
             if (parser == null) {
                 determineFieldSepRegex(line);
             }
 
             List<String> parsedLine = splitLine(line);
             if (parsedLine.size() < 1) {
                 return 0;
             }
             String elem = parsedLine.get(0);
             if ("FileInfo".equals(elem)) {
                 return 0;
             }
             if ("RegReport".equals(elem)) {
                 updatePropertyHeaders(parsedLine);
                 return 0;
             }
             return storeEvent(parsedLine);
         } catch (Exception e) {
             NeoLoaderPlugin.exception(e);
             return 0;
         }
     }
 
     /**
      * Store event.
      * 
      * @param parsedLine the parsed line
      */
     private int storeEvent(List<String> parsedLine) {
         Iterator<String> iterator = parsedLine.iterator();
         String name = iterator.next();
         if ("DAY".equals(name)) {
             updateTimeByDay(iterator);
             return 0;
         } else if ("HOUR".equals(name)) {
             updateTimeByHour(iterator);
             return 0;
         } else if ("MIN".equals(name)) {
             updateTimeByMIN(iterator);
             return 0;
         } else if ("SEC".equals(name)) {
             updateTimeBySec(iterator);
             return 0;
         } else if ("GPS".equals(name)) {
             createMpNode(iterator);
             incValidLocation();
             return 1;
         }
         List<String> propertyList = propertyHeaderMap.get(name);
         if (propertyList==null){
             NeoLoaderPlugin.error(String.format("Not found header for event %s",name));
             return 0;
         }
         if(ignoreHeaders.contains(name)){
            return 0;
         }
         Node m = neo.createNode();
         findOrCreateFileNode(m);
         m.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.M.getId());
         m.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
         for (String property : propertyList) {
             String value=getStringValue(iterator);
             if (value==null){
                 break;
             }
             setIndexPropertyNotParcedValue(headers, m, property, value);
         }
         if (lastMpNode != null) {
             m.createRelationshipTo(lastMpNode, GeoNeoRelationshipTypes.LOCATION);
         }
         if (mNode != null) {
             mNode.createRelationshipTo(m, GeoNeoRelationshipTypes.NEXT);
         }
         mNode = m;
         m.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, _workDate.getTimeInMillis());
         updateTimestampMinMax(1, _workDate.getTimeInMillis());
         index(m);
         this.incValidMessage();
         return 1;
     }
 
     /**
      * @param iterator
      */
     private void createMpNode(Iterator<String> iterator) {
         lastMpNode=neo.createNode();
         Double lon = getDoubleValue(iterator);
         lastMpNode.setProperty(INeoConstants.PROPERTY_LON_NAME, lon);
         Double lat = getDoubleValue(iterator);
         lastMpNode.setProperty(INeoConstants.PROPERTY_LAT_NAME, lat);
         //TODO check real types of GPS values
         lastMpNode.setProperty("altitude", getDoubleValue(iterator));
         lastMpNode.setProperty("speed", getDoubleValue(iterator));
         NodeTypes.MP.setNodeType(lastMpNode, neo);
         index(lastMpNode);
         GisProperties gisProperties = getGisProperties(dataset);
         gisProperties.updateBBox(lat, lon);
         gisProperties.checkCRS(lat, lon, null);
         gisProperties.incSaved();
     }
 
     /**
      * Update time by sec.
      * 
      * @param iterator the iterator
      */
     private void updateTimeBySec(Iterator<String> iterator) {
         _workDate.set(Calendar.SECOND, getIntegerValue(iterator));
         _workDate.set(Calendar.MILLISECOND, getIntegerValue(iterator));
     }
 
     /**
      * Update time by min.
      * 
      * @param iterator the iterator
      */
     private void updateTimeByMIN(Iterator<String> iterator) {
         _workDate.set(Calendar.MINUTE, getIntegerValue(iterator));
     }
 
     /**
      * Update time by hour.
      * 
      * @param iterator the iterator
      */
     private void updateTimeByHour(Iterator<String> iterator) {
         _workDate.set(Calendar.HOUR_OF_DAY, getIntegerValue(iterator));
     }
 
     /**
      * Update time by day.
      * 
      * @param iterator the iterator
      */
     private void updateTimeByDay(Iterator<String> iterator) {
         Integer year = getIntegerValue(iterator);
         Integer month = getIntegerValue(iterator);
         Integer day = getIntegerValue(iterator);
         _workDate.set(Calendar.YEAR, year);
         _workDate.set(Calendar.MONTH, month-1);//Pechko_E: month is from 1 to 12 in Dingli DT files
         _workDate.set(Calendar.DAY_OF_MONTH, day);
         System.out.println("day: "+day+"\t"+_workDate.getTimeInMillis());
 
     }
 
     /**
      * Update property headers.
      * 
      * @param parsedLine the parsed line
      */
     private void updatePropertyHeaders(List<String> parsedLine) {
         Iterator<String> iterator = parsedLine.iterator();
         iterator.next();
         String event = iterator.next();
         List<String> properties = new ArrayList<String>();
         while (iterator.hasNext()) {
             String field = iterator.next();
             if(ignoreHex && field.contains("HexContent")) {
                 ignoreHeaders.add(event);
             }
            if (field.equalsIgnoreCase("EventInfo")){
                field="event_id";
            }
             properties.add(field);
         }
         propertyHeaderMap.put(event, properties);
     }
 
     /**
      * Gets the prymary type.
      * 
      * @param key the key
      * @return the prymary type
      */
     @Override
     protected String getPrymaryType(Integer key) {
         return NodeTypes.M.getId();
     }
 
     /**
      * Gets the storing node.
      * 
      * @param key the key
      * @return the storing node
      */
     @Override
     protected Node getStoringNode(Integer key) {
         if (datasetNode == null) {
             Transaction tx = neo.beginTx();
             try {
                 datasetNode = findOrCreateDatasetNode(neo.getReferenceNode(), dataset);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return datasetNode;
     }
 
     /**
      * Gets the integer value.
      * 
      * @param parameters the parameters
      * @return the integer value
      */
     protected static Double getDoubleValue(Iterator<String> parameters) {
         
         if (parameters == null || !parameters.hasNext()) {
             return null;
         }
         String value = parameters.next();
         if (value.isEmpty()) {
             return null;
         }
             return Double.parseDouble(value);
     }
     /**
      * Gets the integer value.
      * 
      * @param parameters the parameters
      * @return the integer value
      */
     protected static Integer getIntegerValue(Iterator<String> parameters) {
 
         if (parameters == null || !parameters.hasNext()) {
             return null;
         }
         String value = parameters.next();
         if (value.isEmpty()) {
             return null;
         }
 
             return Integer.parseInt(value);
     }
 
     /**
      * Gets the string value.
      * 
      * @param parameters the parameters
      * @return the string value
      */
     protected static String getStringValue(Iterator<String> parameters) {
         if (parameters == null || !parameters.hasNext()) {
             return null;
         }
         return parameters.next();
     }
 
     /**
      * Gets the float value.
      * 
      * @param parameters the parameters
      * @return the float value
      */
     protected static Float getFloatValue(Iterator<String> parameters) {
         if (parameters == null || !parameters.hasNext()) {
             return null;
         }
         String value = parameters.next();
         if (value.isEmpty()) {
             return null;
         }
 
         return Float.parseFloat(value);
     }
 
     public static class TestScenario {
         int stringBlockSize = 120;
         String[] files;
         boolean ignoreHex;
 
         public TestScenario(int sbs, String[] files, boolean ignoreHex) {
             this.stringBlockSize = (sbs / 2) * 2; // ensure multiple of two
             this.files = files;
             this.ignoreHex = ignoreHex;
         }
 
         public void run() {
             String dbPath = "../../testing/dingli/neo_" + (ignoreHex ? "X" : "H") + "_" + stringBlockSize;
             String dataset = "DingLi Dataset";
             Map<String, String> config = new HashMap<String, String>();
             config.put("string_block_size", String.valueOf(stringBlockSize));
             EmbeddedGraphDatabase neo = new EmbeddedGraphDatabase(dbPath, config);
             try {
                 for (String filename : files) {
                     DingLiLoader driveLoader = new DingLiLoader(neo, filename, dataset);
                     //driveLoader.setLimit(100);
                     driveLoader.ignoreHex = ignoreHex;
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
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         if (args.length < 1) {
             File dir = new File(System.getenv("HOME") + "/AWE/Data/Dingli/Canton");
             args = dir.list(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.endsWith(".log");
                 }
             });
             for (int i = 0; i < args.length; i++) {
                 args[i] = (new File(dir, args[i])).getPath();
             }
         }
         ArrayList<TestScenario> tests = new ArrayList<TestScenario>();
         for (int sbs = 240; sbs > 10; sbs /= 2) {
             for (boolean ignoreHex : new boolean[] {true, false}) {
                 tests.add(new TestScenario(sbs, args, ignoreHex));
             }
         }
         for (TestScenario test : tests) {
             test.run();
         }
     }
 
 
 }
