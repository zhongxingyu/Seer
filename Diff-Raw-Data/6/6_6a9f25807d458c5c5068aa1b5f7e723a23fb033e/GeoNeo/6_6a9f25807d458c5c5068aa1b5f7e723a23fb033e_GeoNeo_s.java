 package org.amanzi.awe.catalog.neo;
 
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.Traverser;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.vividsolutions.jts.geom.Coordinate;
 
 /**
  * This class is a utility class for reading GIS information from a specific
  * source within a Neo4J database. The GIS information is assumed to start with
  * a node known as the GIS node. This node contains the CRS information and a relationship
  * to the first of a graph of nodes representing GIS data. Each of these is expected to
  * have properties x/y or lat/long containing the location.
  * @author craig
  */
 public class GeoNeo {
     private Node gisNode;   // the root of some specific GIS information in the Neo4j database
     private CoordinateReferenceSystem crs;
     private ReferencedEnvelope bounds;
     private String name;
     private org.neo4j.api.core.NeoService neo;
     private GisTypes types;
     private String propertyName;
     // private Integer propertyAdjacency;
     private Double minPropertyValue;
     private Double maxPropertyValue;
     private Set<Node> selectedNodes = new HashSet<Node>();
     private Double propertyValueMin;
     private Double propertyValueMax;
     private String distrName;
     private String selectName;
     private long count = 0;
 
     /**
      * A class representing a located Node in the database. By convention all GeoNodes
      * are expected to contain properties for "type" and "name". In addition they should contain
      * a location or set of coordinates in one of the following formats:
      * <ul>
      * <li>"coords" => double[] of coordinates</li>
      * <li>"x" & "y" => two coords of type double</li>
      * <li>"lat" & "long" => two coords of type double</li>
      * <li>"lat" & "lon" => two coords of type double</li>
      * <li>"geom" => Geometry object in WKT or WKB format (currently unsupported)</li>
      * </ul>
      * 
      * @author craig
      * @since 1.0.0
      */
     public static class GeoNode {
         private double[] coords;
         private Node node;
         private Coordinate coordinate;
         private GeoNode(Node node){
             this.coords = getCoords(node);
             this.node = node;
         }
 
         public Coordinate getCoordinate() {
             if(coordinate==null) coordinate = new Coordinate(coords[0],coords[1]);
             return coordinate;
         }
         public double[] getCoords(){
             return coords;
         }
         public Node getNode(){
             return node;
         }
         public String getType(){
             return node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).toString();
         }
         public String getName(){
             return node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
         }
         public String toString(){
             return getName();
         }
         private static double[] getCoords(Node next) {
             if(next.hasProperty(INeoConstants.PROPERTY_COORDS_NAME)){
                 return (double[])next.getProperty(INeoConstants.PROPERTY_COORDS_NAME);
             }
             if(next.hasProperty(INeoConstants.PROPERTY_X_NAME) && next.hasProperty(INeoConstants.PROPERTY_Y_NAME)){
                 return new double[]{(Float)next.getProperty(INeoConstants.PROPERTY_X_NAME),(Float)next.getProperty(INeoConstants.PROPERTY_Y_NAME)};
             }
             if(next.hasProperty(INeoConstants.PROPERTY_LAT_NAME)){
                 if(next.hasProperty(INeoConstants.PROPERTY_LON_NAME)){
                    return new double[]{(Float)next.getProperty(INeoConstants.PROPERTY_LON_NAME),(Float)next.getProperty(INeoConstants.PROPERTY_LAT_NAME)};
                 }
             }
             return null;
         }
     }
 
     /**
      * Create a GeoNeo reader for loading GIS data from the specified GIS root node.
      * 
      * @param gisNode
      */
     public GeoNeo(org.neo4j.api.core.NeoService neo, Node gisNode){
         this.neo = neo;
         this.gisNode = gisNode;
         this.name = this.gisNode.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
         this.types = GisTypes.findGisTypeByHeader(this.gisNode.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME).toString());
         this.count = (Long)this.gisNode.getProperty("count", 0L);
     }
 
     /**
      * get child (depth=1) by necessary type
      * 
      * @param type type name
      * @return set of child's
      */
     public Set<Node> getChildByType(String type) {
         HashSet<Node> result = new HashSet<Node>();
         Iterable<Relationship> relations = gisNode.getRelationships(Direction.OUTGOING);
         for (Relationship relationship : relations) {
             Node node = relationship.getEndNode();
             if (type.equals(node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, ""))) {
                 result.add(node);
             }
         }
         return result;
     }
 
     /**
      * Find the Coordinate Reference System in the GIS node, or default
      * to WGS84 if none found.
      * @return CoordinateReferenceSystem
      */
     public CoordinateReferenceSystem getCRS(){
         return getCRS(DefaultGeographicCRS.WGS84);
     }
 
     /**
      * Find the Coordinate Reference System in the GIS node, or default
      * to the specified default if no CRS is found.
      * @return CoordinateReferenceSystem
      */
     public CoordinateReferenceSystem getCRS(CoordinateReferenceSystem defaultCRS){
         if(crs==null){
             crs = defaultCRS; // default if crs cannot be found below
             try{
                 if(gisNode.hasProperty(INeoConstants.PROPERTY_CRS_NAME)){
                     // The simple approach is to name the CRS, eg. EPSG:4326 (GeoNeo spec prefers a new naming standard, but I'm not sure geotools knows it)
                     crs = CRS.decode(gisNode.getProperty(INeoConstants.PROPERTY_CRS_NAME).toString());
                 }else if(gisNode.hasProperty(INeoConstants.PROPERTY_CRS_HREF_NAME)){
                     // TODO: This type is specified in GeoNeo spec, but what the HREF means is not, so we assume it is a live URL that will feed a CRS specification directly
                     // TODO: Lagutko: gisNode.hasProperty() has 'crs_href' as parameter, but gisNode.getProperty() has only 'href'. What is right?
                     URL crsURL = new URL(gisNode.getProperty(INeoConstants.PROPERTY_CRS_HREF_NAME).toString());
                     crs = CRS.decode(crsURL.getContent().toString());
                 }
             }catch(Exception crs_e){
                 System.err.println("Failed to interpret CRS: "+crs_e.getMessage());
                 crs_e.printStackTrace(System.err);
             }
         }
         return crs;
     }
 
     public Traverser makeGeoNeoTraverser(){
         return gisNode.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE,
                 GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
     }
 
     /**
      * Find the bounding box for the data set as a ReferenceEnvelope. It uses the getCRS method to
      * find the reference system then looks for explicit "bbox" elements, and finally, if no bbox
      * was found, scans all feature geometries for coordinates and builds the bounds on those. The
      * result is cached for future calls.
      * 
      * @return ReferencedEnvelope for bounding box
      */
     public ReferencedEnvelope getBounds(){
         if(bounds==null){
             // Create Null envelope
             this.bounds = new ReferencedEnvelope(getCRS());
             // First try to find the BBOX definition in the gisNode directly
             try{
                 if(gisNode.hasProperty(INeoConstants.PROPERTY_BBOX_NAME)){
                     double[] bbox = (double[])gisNode.getProperty(INeoConstants.PROPERTY_BBOX_NAME);
                     this.bounds = new ReferencedEnvelope(bbox[0],bbox[1],bbox[2],bbox[3],crs);
                 }else{
                     System.err.println("No BBox defined in the GeoNeo object");
                 }
             }catch(Exception bbox_e){
                 System.err.println("Failed to interpret BBOX: "+bbox_e.getMessage());
                 bbox_e.printStackTrace(System.err);
             }
             // Secondly, if bounds is still empty, try find all feature geometries and calculate bounds
             try{
                 if(this.bounds.isNull()){
                     // Try to create envelope from any data referenced by the gisNode
                     for(GeoNode node:getGeoNodes()){
                         //TODO: support high dimensions
                         this.bounds.expandToInclude(node.getCoords()[0], node.getCoords()[1]);
                     }
                 }
             }catch(Exception bbox_e){
                 System.err.println("Failed to interpret BBOX: "+bbox_e.getMessage());
                 bbox_e.printStackTrace(System.err);
             }
         }
         return bounds;
     }
 
     /**
      * Return the name of the dataset as specified in the Neo, or default
      * to the URL.getFile().
      * @return dataset name
      */
     public String getName(){
         return name;
     }
 
     /**
      * Return a descriptive string of this dataset. This is based on
      * the name, crs and bounding box.
      * @return descriptive string
      */
     public String toString(){
         return "Neo["+getName()+"]: CRS:"+getCRS()+" Bounds:"+getBounds();
     }
 
     private class GeoIterator implements Iterator<GeoNode>{
         private Iterator<Node> iterator;
         private GeoNode next;
         private Transaction transaction;
         private GeoIterator(Node gisNode){
             this.iterator = makeGeoNeoTraverser().iterator();
             this.transaction = neo.beginTx();
         }
         public boolean hasNext() {
             while(next==null){
                 if(!iterator.hasNext()) break;
                 next = new GeoNode(iterator.next());
                 if(next.getCoords()==null) next = null;
             }
             if(next==null){
                 transaction.success();
                 transaction.finish();
             }
             return next!=null;
         }
 
         public GeoNode next() {
             GeoNode toReturn = next;
             next = null;
             return toReturn;
         }
         public void remove() {
             throw new RuntimeException("Unimplemented");
         }
     }
 
     public Iterable<GeoNode> getGeoNodes() {
         return new Iterable<GeoNode>(){
             public Iterator<GeoNode> iterator() {
                 return new GeoIterator(gisNode);
             }};
     }
 
     /**
      * @return Returns the gis type of node.
      */
     public GisTypes getGisType() {
         return types;
     }
 
     /**
      * Sets or remove property for dawning
      * 
      * @param aggrNode aggregation chart node
      * @param propertyNode property node
      * @param adjacency - adjacency
      * @param maxSelNode
      * @param minSelNode
      */
     public void setPropertyToRefresh(Node aggrNode, Node propertyNode, Node minSelNode, Node maxSelNode) {
         if (aggrNode != null && propertyNode != null) {
             distrName = (String)aggrNode.getProperty(INeoConstants.PROPERTY_DISTRIBUTE_NAME);
             selectName = (String)aggrNode.getProperty(INeoConstants.PROPERTY_SELECT_NAME, null);
             propertyName = (String)aggrNode.getProperty(INeoConstants.PROPERTY_NAME_NAME);
             propertyValueMin = (Double)propertyNode.getProperty(INeoConstants.PROPERTY_NAME_MIN_VALUE);
             propertyValueMax = (Double)propertyNode.getProperty(INeoConstants.PROPERTY_NAME_MAX_VALUE);
             minPropertyValue = (Double)minSelNode.getProperty(INeoConstants.PROPERTY_NAME_MIN_VALUE);
             maxPropertyValue = (Double)maxSelNode.getProperty(INeoConstants.PROPERTY_NAME_MAX_VALUE);
             // propertyAdjacency = adjacency;
         } else {
             propertyName = null;
             minPropertyValue = null;
             maxPropertyValue = null;
             // propertyAdjacency = 0;
         }
     }
 
     /**
      * @return Returns the minPropertyValue.
      */
     public Double getMinPropertyValue() {
         return minPropertyValue;
     }
 
     /**
      * @return Returns the maxPropertyValue.
      */
     public Double getMaxPropertyValue() {
         return maxPropertyValue;
     }
 
     /**
      * @return Returns the gisNode.
      */
     public Node getMainGisNode() {
         return gisNode;
     }
 
     /**
      * @return Returns the propertyName.
      */
     public String getPropertyName() {
         return propertyName;
     }
 
     /**
      * @return Returns the propertyValueMin.
      */
     public Double getPropertyValueMin() {
         return propertyValueMin;
     }
 
     /**
      * @return Returns the propertyValueMax.
      */
     public Double getPropertyValueMax() {
         return propertyValueMax;
     }
 
     /**
      * @param object
      */
     public void setSelectedNodes(Set<Node> selectedNodes) {
         if (selectedNodes == null) {
             this.selectedNodes = new HashSet<Node>();
         } else {
             this.selectedNodes = selectedNodes;
         }
     }
 
     /**
      * @param node
      */
     public void addNodeToSelect(Node node) {
         selectedNodes.add(node);
     }
 
     /**
      * @return Returns the selectedNodes.
      */
     public Set<Node> getSelectedNodes() {
         return selectedNodes;
     }
 
     /**
      * @return Returns the distrName.
      */
     public String getDistrName() {
         return distrName;
     }
 
     /**
      * @return Returns the selectName.
      */
     public String getSelectName() {
         return selectName;
     }
 
     public long getCount() {
         return this.count;
     }
 
 }
