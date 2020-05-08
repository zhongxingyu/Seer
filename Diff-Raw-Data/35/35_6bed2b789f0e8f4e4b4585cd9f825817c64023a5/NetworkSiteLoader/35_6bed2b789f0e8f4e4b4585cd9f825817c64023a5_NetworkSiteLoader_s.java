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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * <p>
  * Network Site loader
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class NetworkSiteLoader extends AbstractLoader {
     /** String SITE_ID_KEY field */
     private static final String SITE_ID_KEY = "site_id";
     private ArrayList<String> mainHeaders = new ArrayList<String>();
     private NetworkHeader networkHeader = null;
     public ArrayList<String> lineErrors = new ArrayList<String>();
     private final Node networkNode;
     private boolean needParceHeader;
     /**
      * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
      * 
      * @param filename of file to load
      * @param display for opening message dialogs
      * @param dataset to add data to
      */
     public NetworkSiteLoader(Node networkNode, String filename, Display display) {
         this.networkNode = networkNode;
         initialize("Network", null, filename, display);
         initializeKnownHeaders();
     }
     @Override
     protected void parseLine(String line) {
         List<String> fields = splitLine(line);
         if (fields.size() < 3)
             return;
         if (this.isOverLimit())
             return;        
         Transaction transaction = neo.beginTx();
         try {
             if (networkHeader == null) {
                 networkHeader = new NetworkHeader(fields);
             } else {
                 networkHeader.setData(fields);
             }
             String siteField = networkHeader.getString(SITE_ID_KEY);
 
             if (siteField == null) {
                 lineErrors.add("Missing sector name on line " + lineNumber);
                 return;
             }
 
             Node siteNode = getSiteNode(siteField);
             if (siteNode == null) {
                 lineErrors.add("Missing sector name on line " + lineNumber);
                 return;
             }
             // header.parseLine(sector, fields);
             Map<String, Object> sectorData = networkHeader.getSiteData();
             for (Map.Entry<String, Object> entry : sectorData.entrySet()) {
                 String key = entry.getKey();
                 if ("type".equals(key)) {
                     key = "site_type";
                 }
                 siteNode.setProperty(key, entry.getValue());
             }
 
             gisNodes.values().iterator().next().incSaved();
             transaction.success();
             // return true;
         } catch (Exception e) {
             lineErrors.add("Error parsing line " + lineNumber + ": " + e);
             error(lineErrors.get(lineErrors.size() - 1));
             if (lineErrors.size() == 1) {
                 e.printStackTrace(System.err);
             } else if (lineErrors.size() > 10) {
                 e.printStackTrace(System.err);
                 // return false;
             }
         } finally {
             transaction.finish();
         }
 
     }
 
     /**
      * @param siteField
      * @return
      */
     private Node getSiteNode(final String siteField) {
         Iterator<Node> iterator = networkNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node curNode = currentPos.currentNode();
                 return NeoUtils.getNodeType(curNode, "").equals("site")
                         && NeoUtils.getSimpleNodeName(curNode, "").equals(siteField);
             }
         }, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator();
         return iterator.hasNext() ? iterator.next() : null;
     }
 
     /**
      * Add a known header entry as well as mark it as a main header. All other fields will be
      * assumed to be sector properties.
      * 
      * @param key
      * @param regexes
      */
     private void addMainHeader(String key, String[] regexes) {
         addKnownHeader(1, key, regexes);
         mainHeaders.add(key);
     }
 
     /**
      * Build a map of internal header names to format specific names for types that need to be known
      * in the algorithms later.
      */
     private void initializeKnownHeaders() {
         needParceHeader = true;
         // Known headers that are not sector data properties
        addMainHeader(SITE_ID_KEY, new String[] {"SITE_ID"});
         // addMainHeader("type", new String[] {"TYPE"});
         // Stop statistics collection for properties we will not save to the sector
         addNonDataHeaders(1, mainHeaders);
 
         // force String types on some risky headers (sometimes these look like integers)
         useMapper(1, SITE_ID_KEY, new StringMapper());
 
         // Known headers that are sector data properties
        addKnownHeader(1, "beamwidth", new String[] {".*beamwidth.*", "beam", "hbw"});
        addKnownHeader(1, "azimuth", new String[] {".*azimuth.*"});
     }
 
     private class NetworkHeader {
         private Map<String, String> mainKeys = new HashMap<String, String>();
         private ArrayList<String> siteData = new ArrayList<String>();
         Map<String, Object> lineData = null;
 
         private NetworkHeader(List<String> fields) {
             lineData = makeDataMap(fields);
             HeaderMaps headerMap = getHeaderMap(1);
             for (String header : lineData.keySet()) {
                 String name = headerMap.headerName(header);
                 if (mainHeaders.contains(header)) {
                     mainKeys.put(header, name);
                 } else {
                     siteData.add(header);
                 }
             }
         }
 
         private void setData(List<String> fields) {
             lineData = makeDataMap(fields);
         }
 
         private Map<String, Object> getSiteData() {
             Map<String, Object> data = new LinkedHashMap<String, Object>();
             for (String key : siteData) {
                 if (lineData.containsKey(key)) {
                     data.put(key, lineData.get(key));
                 }
             }
             return data;
         }
 
         private String getString(String key) {
             Object value = lineData.get(key);
             if (value == null || value instanceof String) {
                 return (String)value;
             } else {
                 return value.toString();
             }
         }
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         //TODO: Lagutko: need to be refactored
         return gisNodes.values().iterator().next().getGis();
     }
 
     @Override
     protected boolean needParceHeaders() {
         if (needParceHeader) {
             needParceHeader = false;
             return true;
         }
         return false;
     }
 
 }
