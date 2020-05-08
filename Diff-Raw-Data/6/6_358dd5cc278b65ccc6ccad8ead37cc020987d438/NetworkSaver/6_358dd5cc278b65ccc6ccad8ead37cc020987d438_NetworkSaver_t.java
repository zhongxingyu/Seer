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
 
 package org.amanzi.neo.loader.core.saver.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.amanzi.neo.loader.core.parser.BaseTransferData;
 import org.amanzi.neo.loader.core.preferences.DataLoadPreferences;
 import org.amanzi.neo.loader.core.preferences.PreferenceStore;
 import org.amanzi.neo.loader.core.saver.AbstractHeaderSaver;
 import org.amanzi.neo.loader.core.saver.MetaData;
 import org.amanzi.neo.services.GisProperties;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.utils.Utils;
 import org.geotools.referencing.CRS;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 /**
  * <p>
  * Network saver
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class NetworkSaver extends AbstractHeaderSaver<BaseTransferData> {
 
     private boolean headerNotHandled;
     private String siteName = null;
     private String bscName = null;
     private String cityName = null;
     private Node site = null;
     private Node bsc = null;
     private Node city = null;
     private final HashMap<String, Node> bsc_s = new HashMap<String, Node>();
     private final HashMap<String, Node> city_s = new HashMap<String, Node>();
     private final MetaData metadata=new MetaData("network", MetaData.SUB_TYPE,"radio"); 
     
     private enum NetworkLevels {
         NETWORK, CITY, BSC, SITE, SECTOR;
     }
 
     private Set<NetworkLevels> levels = EnumSet.of(NetworkLevels.NETWORK);
     private boolean trimSectorName;
     private CoordinateReferenceSystem crs;
 
     @Override
     public void init(BaseTransferData element) {
         super.init(element);
         propertyMap.clear();
         headerNotHandled = true;
         trimSectorName = PreferenceStore.getPreferenceStore().getValue(DataLoadPreferences.REMOVE_SITE_NAME);
         addNetworkIndexes();
         String crsWkt =element.get("CRS");
         if (crsWkt!=null){
             try {
                 crs=CRS.parseWKT(crsWkt);
             } catch (FactoryException e) {
                 exception(e);
                 crs=null;
             }
         }
         
     }
     private void addNetworkIndexes() {
         try {
             addIndex(NodeTypes.SITE.getId(),service.getLocationIndexProperty(rootname));
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
 
     }
 
 
     @Override
     public void save(BaseTransferData element) {
         if (headerNotHandled) {
 //            Iterator<Entry<String, String>> iterator = element.entrySet().iterator();
 //            while (iterator.hasNext()) {
 //                String name = iterator.next().getKey();
 //                String[] headers = getPossibleHeaders(name);
 //                propertyMap.put(name, headers[0]);
 //            }
             initializeKnownHeaders();
             ArrayList<String> headers = new ArrayList<String>();
             Set<Entry<String, String>> set = element.entrySet();
             Iterator<Entry<String, String>> iterator = set.iterator();
             while (iterator.hasNext()) {
                 headers.add(iterator.next().getKey());
             }
             definePropertyMap(element);
             startMainTx(1000);
             initializeIndexes();
             headerNotHandled = false;
             // Kasnitskij_V:
             rootNode.setProperty(INeoConstants.PROPERTY_STRUCTURE_NAME, getLevelsFound());
             parseHeader(headers);
             saveFileStructure();
         }
         
         saveRow(element);
     }
     
     /**
      * Kasnitskij_V:
      * Gets all found network levels.
      *
      * @return levels found as an array of Strings
      */
     private String[] getLevelsFound() {
         levels.iterator().next();
         String[] levelsFound = new String[NetworkLevels.values().length];
         Iterator<NetworkLevels> iterator = levels.iterator();
         int i = 0;
         iterator.next();
         for (NetworkLevels networkLevel : NetworkLevels.values()) {
             String name = networkLevel.name().toLowerCase();
             levelsFound[i++] = name;
         }
         return levelsFound;
     }
     
     /**
      * Kasnitskij_V:
      * Save file structure.
      */
     private void saveFileStructure() {
         Map<String,String> localHeaders = new HashMap<String, String>();
         for(Entry<String, String> prop : headers.entrySet()){
 
             String prefix = INeoConstants.SECTOR_PROPERTY_NAME_PREFIX;
             String propertyKey = prop.getKey();
             String propOriginalName = prop.getValue();
 
             if (propertyKey.endsWith(INeoConstants.PROPERTY_LAT_NAME) || 
                 propertyKey.endsWith(INeoConstants.PROPERTY_LON_NAME)) {
                 prefix = INeoConstants.SITE_PROPERTY_NAME_PREFIX;
             } else if (propertyKey.endsWith("sector")) {
                 propertyKey = INeoConstants.PROPERTY_NAME_NAME;
             } else if (propertyKey.endsWith(NodeTypes.BSC.getId())) {
                 propertyKey = INeoConstants.PROPERTY_NAME_NAME;
                 prefix = INeoConstants.BSC_PROPERTY_NAME_PREFIX;
             } else if (propertyKey.endsWith("site")) {
                 propertyKey = INeoConstants.PROPERTY_NAME_NAME;
                 prefix = INeoConstants.SITE_PROPERTY_NAME_PREFIX;
             }
             else if (propertyKey.endsWith("city")) {
                 propertyKey = INeoConstants.PROPERTY_NAME_NAME;
                 prefix = INeoConstants.CITY_PROPERTY_NAME_PREFIX;
             }
 
             localHeaders.put(prefix + propertyKey, propOriginalName);
         }
         
         service.addOriginalHeaders(rootNode, localHeaders);
     }
     
     /**
      * Build a map of internal header names to format specific names for types that need to be known
      * in the algorithms later.
      */
     private void initializeKnownHeaders() {
         addMainHeader("city", getPossibleHeaders(DataLoadPreferences.NH_CITY));
         addMainHeader("msc", getPossibleHeaders(DataLoadPreferences.NH_MSC));
         addMainHeader("bsc", getPossibleHeaders(DataLoadPreferences.NH_BSC));
         addMainIdentityHeader("site", getPossibleHeaders(DataLoadPreferences.NH_SITE));
         addMainIdentityHeader("sector", getPossibleHeaders(DataLoadPreferences.NH_SECTOR));
         addMainHeader(INeoConstants.PROPERTY_SECTOR_CI, getPossibleHeaders(DataLoadPreferences.NH_SECTOR_CI));
         addMainHeader(INeoConstants.PROPERTY_SECTOR_LAC, getPossibleHeaders(DataLoadPreferences.NH_SECTOR_LAC));
         addMainHeader(INeoConstants.PROPERTY_LAT_NAME, getPossibleHeaders(DataLoadPreferences.NH_LATITUDE));
         addMainHeader(INeoConstants.PROPERTY_LON_NAME, getPossibleHeaders(DataLoadPreferences.NH_LONGITUDE));
 
         // Known headers that are sector data properties
         addKnownHeader("beamwidth", getPossibleHeaders(DataLoadPreferences.NH_BEAMWIDTH), false);
         addKnownHeader("azimuth", getPossibleHeaders(DataLoadPreferences.NH_AZIMUTH), false);
         addKnownHeader("band", new String[] {"Ant_Freq_Band"}, false);
     }
     
     /**
      * Save row.
      *
      * @param element the element
      */
     protected void saveRow(BaseTransferData element) {
         try {
             String bscField = getStringValue("bsc", element);
             String cityField = getStringValue("city", element);
             String siteField = getStringValue("site", element);
             String sectorField = getStringValue("sector", element);
             if (sectorField == null) {
                 error("Missing sector name on line " + element.getLine());
                 return;
             }
 
             if (siteField == null) {
                 siteField = sectorField.substring(0, sectorField.length() - 1);
             }
 
             // Lagutko, 24.02.2010, sector name can be repeatable (for example 'sector1') so we need
             // additional variable for Lucene Index
             String sectorIndexName = sectorField;
             if (trimSectorName) {
                 sectorField = sectorField.replaceAll(siteField + "[\\:\\-]?", "");
             }
             if (cityField != null && !cityField.equals(cityName)) {
                 cityName = cityField;
                 city = city_s.get(cityField);
                 if (city == null) {
                     city = getIndexService().getSingleNode(Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.CITY), cityName);
                     if (city == null) {
                         city = addSimpleChild(rootNode, NodeTypes.CITY, cityName);
                     }
                     city_s.put(cityField, city);
                 }
             }
             if (bscField != null && !bscField.equals(bscName)) {
                 if (!levels.contains(NetworkLevels.BSC)) {
                     levels.add(NetworkLevels.BSC);
                 }
                 bscName = bscField;
                 bsc = bsc_s.get(bscField);
                 if (bsc == null) {
                     bsc = getIndexService().getSingleNode(Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.BSC), bscName);
                     if (bsc == null) {
                         bsc = addSimpleChild(city == null ? rootNode : city, NodeTypes.BSC, bscName);
                     }
                     bsc_s.put(bscField, bsc);
                 }
             }
             Double lat = getNumberValue(Double.class, INeoConstants.PROPERTY_LAT_NAME, element);
             Double lon = getNumberValue(Double.class, INeoConstants.PROPERTY_LON_NAME, element);
             if (lat == null) {
                 lat = 0d;
             }
             if (lon == null) {
                 lon = 0d;
             }
             if (!siteField.equals(siteName)) {
                 siteName = siteField;
                 Node siteRoot = bsc == null ? (city == null ? rootNode : city) : bsc;
                 Node newSite = getIndexService().getSingleNode(Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SITE), siteName);
                 if (newSite != null) {
                     Relationship relation = newSite.getSingleRelationship(GeoNeoRelationshipTypes.CHILD, Direction.INCOMING);
                     Node oldRoot = relation.getOtherNode(newSite);
                     if (!oldRoot.equals(siteRoot)) {
                         // TODO check work in batchinserter
                         getService().delete(relation);
                         siteRoot.createRelationshipTo(newSite, GeoNeoRelationshipTypes.CHILD);
                     }
                 } else {
 
                     if (lat == 0 && lon == 0) {
                         // not stored site!
                         return;
                     }
                     newSite = addSimpleChild(siteRoot, NodeTypes.SITE, siteName);
                     (site == null ? rootNode : site).createRelationshipTo(newSite, GeoNeoRelationshipTypes.NEXT);
                 }
                 site = newSite;
 
                 GisProperties gisProperties = getGisProperties(rootNode);
                 gisProperties.updateBBox(lat, lon);
                 if (gisProperties.getCrs() == null) {
                     if (crs==null){
                     gisProperties.checkCRS(lat, lon, null);
                     }else{
                       gisProperties.setCrs(crs);
                       gisProperties.saveCRS();                        
                     }
 //                    if (gisProperties.getCrs() != null) {
 //                        CoordinateReferenceSystem crs = AbstractLoader.askCRSChoise(gisProperties);
 //                        if (crs != null) {
 //                            gisProperties.setCrs(crs);
 //                            gisProperties.saveCRS();
 //                        }
 //                    }
                 }
               
                updateProperty(rootname,NodeTypes.SITE.getId(),site,INeoConstants.PROPERTY_LAT_NAME, lat);
                updateProperty(rootname,NodeTypes.SITE.getId(),site,INeoConstants.PROPERTY_LON_NAME, lon);
                 index(site);
             }
             Integer ci = getNumberValue(Integer.class, INeoConstants.PROPERTY_SECTOR_CI, element);
             Integer lac = getNumberValue(Integer.class, INeoConstants.PROPERTY_SECTOR_LAC, element);
             Node sector = service.findSector(rootNode, ci, lac, sectorIndexName, true);
             if (sector != null) {
                 // TODO check
                 String nodeName = service.getNodeName(sector);
                 if (!sectorField.equals(nodeName)){
                     error(String.format("File: %s, line %s sector %s have same CI(=%s) and LAC(=%s) like sector %s. Sector storing without storing of CI and LAC", element.getFileName(),element.getLine(),sectorField,ci,lac,nodeName));
                     sector = addSimpleChild(site, NodeTypes.SECTOR, sectorField);
                     sector.setProperty("sector_id", sectorIndexName);
                     service.indexByProperty(rootNode.getId(), sector, "sector_id");                  
                 }
             } else {
                 sector = addSimpleChild(site, NodeTypes.SECTOR, sectorField);
                 sector.setProperty("sector_id", sectorIndexName);
                 service.indexByProperty(rootNode.getId(), sector, "sector_id");
                 if (ci != null) {
                     sector.setProperty(INeoConstants.PROPERTY_SECTOR_CI, ci);
                     getIndexService().index(sector, Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_SECTOR_CI, NodeTypes.SECTOR), ci);
                 }
                 if (lac != null) {
                     sector.setProperty(INeoConstants.PROPERTY_SECTOR_LAC, lac);
                     getIndexService().index(sector, Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_SECTOR_LAC, NodeTypes.SECTOR), lac);
                 }
             }
             Integer beamwith = getNumberValue(Integer.class, "beamwidth", element);
             if (beamwith!=null){
                 updateProperty(rootname,NodeTypes.SECTOR.getId(),sector,"beamwidth",beamwith);
             }
             Integer azimuth = getNumberValue(Integer.class, "azimuth", element);
             if (azimuth!=null){
                 updateProperty(rootname,NodeTypes.SECTOR.getId(),sector,"azimuth",azimuth);
             }
             // header.parseLine(sector, fields);
             Map<String, Object> sectorData = getNotHandledData(element,rootname,NodeTypes.SECTOR.getId());
             
             String band = getStringValue("band", element);
             
             processCarriers(sector, band, sectorData);
 
             for (Map.Entry<String, Object> entry : sectorData.entrySet()) {
                 String key = entry.getKey();
                 updateProperty(rootname,NodeTypes.SECTOR.getId(),sector,key,entry.getValue());
             }
             index(sector);
             getGisProperties(rootNode).updateBBox(lat, lon);
 //            updateTx(0,0);
             // return true;
         } catch (Exception e) {
             exception("Error parsing line " + element.getLine() + ": ", e);
         }
     }
 
     private void processCarriers(Node sector, String band, Map<String, Object> propertyMap) {
         if (band==null){
             return;
         }
         //try to get a Band
         int spaceIndex = band.indexOf(" ");
         if (spaceIndex > 0) {
             band = band.substring(spaceIndex).trim();
         }
         
         //try to get BCCH frequency
         Object frequency = propertyMap.get("bcch");
         if (frequency != null) {
             Integer iFrequency = (Integer)frequency;
             
             Utils.createBCCHCarrier(sector, band, new int[] {iFrequency}, getService());
         }
         
         //try to get other frequencies
         ArrayList<String> propertiesToRemove = new ArrayList<String>();
         for (String key : propertyMap.keySet()) {
             if (key.startsWith("trx")) {
                 String trxIndex = key.substring(key.indexOf("trx") + 3);
                 Integer trxId = Integer.parseInt(trxIndex);
                 
                 Integer arfcn = (Integer)propertyMap.get(key);
                 propertiesToRemove.add(key);
                 Utils.createCarrier(sector, trxId, band, new int[] {arfcn}, getService());
             }
         }
         
         //clean up sector data from trxs
         for (String trxKey : propertiesToRemove) {
             propertyMap.remove(trxKey);
         }
         
     }
 
 
 
 
 
 
     /**
      * @param element
      */
     protected void definePropertyMap(BaseTransferData element) {
         Set<String> headers = element.keySet();
         defineHeader(headers, "city", getPossibleHeaders(DataLoadPreferences.NH_CITY));
         defineHeader(headers, "msc", getPossibleHeaders(DataLoadPreferences.NH_MSC));
         defineHeader(headers, "bsc", getPossibleHeaders(DataLoadPreferences.NH_BSC));
         defineHeader(headers, "site", getPossibleHeaders(DataLoadPreferences.NH_SITE));
         defineHeader(headers, "sector", getPossibleHeaders(DataLoadPreferences.NH_SECTOR));
         defineHeader(headers, INeoConstants.PROPERTY_SECTOR_CI, getPossibleHeaders(DataLoadPreferences.NH_SECTOR_CI));
         defineHeader(headers, INeoConstants.PROPERTY_SECTOR_LAC, getPossibleHeaders(DataLoadPreferences.NH_SECTOR_LAC));
         defineHeader(headers, INeoConstants.PROPERTY_LAT_NAME, getPossibleHeaders(DataLoadPreferences.NH_LATITUDE));
         defineHeader(headers, INeoConstants.PROPERTY_LON_NAME, getPossibleHeaders(DataLoadPreferences.NH_LONGITUDE));
         defineHeader(headers, "beamwidth", getPossibleHeaders(DataLoadPreferences.NH_BEAMWIDTH));
         defineHeader(headers, "azimuth", getPossibleHeaders(DataLoadPreferences.NH_AZIMUTH));
         
         defineHeader(headers, "band",  new String[] {"Ant_Freq_Band"});
 //        is3G = element.keySet().contains("gsm_ne");
     }
 
 
 
 
     @Override
     protected void fillRootNode(Node rootNode, BaseTransferData element) {
 
     }
 
 
 
     @Override
     protected String getRootNodeType() {
         return NodeTypes.NETWORK.getId();
     }
     @Override
     protected String getTypeIdForGisCount(GisProperties gis) {
         return NodeTypes.SECTOR.getId();
     }
     @Override
     public Iterable<MetaData> getMetaData() {
         return Arrays.asList(new MetaData[]{metadata});
     }
 
 }
