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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.services.GisProperties;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.DriveTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.Node;
 
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
     private Node lastMpNode = null;
     private Node mNode;
     private LinkedHashMap<String, Header> headers;
 
     /**
      * Instantiates a new ding li loader.
      * 
      * @param filename the filename
      * @param display the display
      * @param dataset the dataset
      */
     public DingLiLoader(String filename, Display display, String dataset) {
         driveType = DriveTypes.TEMS;
         initialize("DingLi", null, filename, display, dataset);
         initializeLuceneIndex();
         addDriveIndexes();
         headers = getHeaderMap(1).headers;
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
     protected void parseLine(String line) {
         try {
             if (parser == null) {
                 determineFieldSepRegex(line);
             }
 
             List<String> parsedLine = splitLine(line);
             if (parsedLine.size() < 1) {
                 return;
             }
             String elem = parsedLine.get(0);
             if ("FileInfo".equals(elem)) {
                 return;
             }
             if ("RegReport".equals(elem)) {
                 updatePropertyHeaders(parsedLine);
                 return;
             }
             storeEvent(parsedLine);
         } catch (Exception e) {
             NeoLoaderPlugin.exception(e);
         }
     }
 
     /**
      * Store event.
      * 
      * @param parsedLine the parsed line
      */
     private void storeEvent(List<String> parsedLine) {
         Iterator<String> iterator = parsedLine.iterator();
         String name = iterator.next();
         if ("DAY".equals(name)) {
             updateTimeByDay(iterator);
             return;
         } else if ("HOUR".equals(name)) {
             updateTimeByHour(iterator);
             return;
         } else if ("MIN".equals(name)) {
             updateTimeByMIN(iterator);
             return;
         } else if ("SEC".equals(name)) {
             updateTimeBySec(iterator);
             return;
         } else if ("GPS".equals(name)) {
             createMpNode(iterator);
             return;
         }
         List<String> propertyList = propertyHeaderMap.get(name);
         if (propertyList==null){
             NeoLoaderPlugin.error(String.format("Not found header for event %s",name));
             return;
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
         if (lastMpNode!=null){
         m.createRelationshipTo(lastMpNode, GeoNeoRelationshipTypes.LOCATION);
         }
         if (mNode != null) {
             mNode.createRelationshipTo(m, GeoNeoRelationshipTypes.NEXT);
         }
         mNode = m;
         m.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, _workDate.getTimeInMillis());
         index(m);
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
         _workDate.set(Calendar.MONTH, month);
         _workDate.set(Calendar.DAY_OF_MONTH, day);
 
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
             properties.add(iterator.next());
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
 }
