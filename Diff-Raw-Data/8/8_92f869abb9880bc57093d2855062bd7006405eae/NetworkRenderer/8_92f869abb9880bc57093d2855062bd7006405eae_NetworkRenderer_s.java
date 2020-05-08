 package org.amanzi.awe.render.network;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.core.Pair;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IStyleBlackboard;
 import net.refractions.udig.project.internal.render.impl.RendererImpl;
 import net.refractions.udig.project.render.RenderException;
 
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.catalog.neo.GeoNeo.GeoNode;
 import org.amanzi.awe.neostyle.NeoStyle;
 import org.amanzi.awe.neostyle.NeoStyleContent;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.PropertyHeader;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.preferences.DataLoadPreferences;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 
 public class NetworkRenderer extends RendererImpl {
     public static final String BLACKBOARD_NODE_LIST = "org.amanzi.awe.tool.star.StarTool.nodes";
     public static final String BLACKBOARD_START_ANALYSER = "org.amanzi.awe.tool.star.StarTool.analyser";
     private static final Color COLOR_SELECTED = Color.RED;
     private static final Color COLOR_LESS = Color.BLUE;
     private static final Color COLOR_MORE = Color.GREEN;
     private static final Color COLOR_SITE_SELECTED = Color.CYAN;
     private static final Color COLOR_SECTOR_SELECTED = Color.CYAN;
     private static final Color COLOR_SECTOR_STAR = Color.RED;
     private AffineTransform base_transform = null;  // save original graphics transform for repeated re-use
     private Color drawColor = Color.DARK_GRAY;
     private Color siteColor = new Color(128, 128, 128,(int)(0.6*255.0));
     private Color fillColor = new Color(255, 255, 128,(int)(0.6*255.0));
     private MathTransform transform_d2w;
     private MathTransform transform_w2d;
 	private Color labelColor;
     private boolean isAggregatedProperties;
     private String[] aggregationList;
     private void setCrsTransforms(CoordinateReferenceSystem dataCrs) throws FactoryException{
         boolean lenient = true; // needs to be lenient to work on uDIG 1.1 (otherwise we get error: bursa wolf parameters required
         CoordinateReferenceSystem worldCrs = context.getCRS();
         this.transform_d2w = CRS.findMathTransform(dataCrs, worldCrs, lenient);
         this.transform_w2d = CRS.findMathTransform(worldCrs, dataCrs, lenient); // could use transform_d2w.inverse() also
     }
 
     private Envelope getTransformedBounds() throws TransformException {
         ReferencedEnvelope bounds = getRenderBounds();
         if (bounds == null) {
             bounds = this.context.getViewportModel().getBounds();
         }
         Envelope bounds_transformed = null;
         if (bounds != null && transform_w2d != null) {
             bounds_transformed = JTS.transform(bounds, transform_w2d);
         }
         return bounds_transformed;
     }
 
     /**
      * This method is called to render what it can. It is passed a graphics context
      * with which it can draw. The class already contains a reference to a RenderContext
      * from which it can obtain the layer and the GeoResource to render.
      * @see net.refractions.udig.project.internal.render.impl.RendererImpl#render(java.awt.Graphics2D, org.eclipse.core.runtime.IProgressMonitor)
      */
     @Override
     public void render( Graphics2D g, IProgressMonitor monitor ) throws RenderException {
         ILayer layer = getContext().getLayer();
         // Are there any resources in the layer that respond to the GeoNeo class (should be the case if we found a Neo4J database with GeoNeo data)
         IGeoResource resource = layer.findGeoResource(GeoNeo.class);
         if(resource != null){
             renderGeoNeo(g,resource,monitor);
         }
     }
 
     /**
      * This method is called to render data from the Neo4j 'GeoNeo' Geo-Resource.
      */
     private void renderGeoNeo( Graphics2D g, IGeoResource neoGeoResource, IProgressMonitor monitor ) throws RenderException {
         if (monitor == null)
             monitor = new NullProgressMonitor();
 
         monitor.beginTask("render network sites and sectors: "+neoGeoResource.getIdentifier(), IProgressMonitor.UNKNOWN);    // TODO: Get size from info
 
         GeoNeo geoNeo = null;
 
         // Setup default drawing parameters and thresholds (to be modified by style if found)
         int drawSize=15;
         int alpha = (int)(0.6*255.0);
         int maxSitesLabel = 30;
         int maxSitesFull = 100;
         int maxSitesLite = 1000;
         int maxSymbolSize = 40;
         Font font = g.getFont();
         int fontSize = font.getSize();
         boolean scaleSectors = true;
 
         IStyleBlackboard style = getContext().getLayer().getStyleBlackboard();
         NeoStyle neostyle = (NeoStyle)style.get(NeoStyleContent.ID );     
         if (neostyle!=null){
             try {
                 siteColor = neostyle.getSiteFill();
                 fillColor=neostyle.getFill();
                 drawColor=neostyle.getLine();
                 labelColor=neostyle.getLabel();
                 drawSize = neostyle.getSymbolSize();
                 alpha = 255 - (int)((double)neostyle.getSectorTransparency() / 100.0 * 255.0);
                 maxSitesLabel = neostyle.getLabeling();
                 maxSitesFull = neostyle.getSmallSymb();
                 maxSitesLite = neostyle.getSmallestSymb();
                 scaleSectors = !neostyle.isFixSymbolSize();
                 maxSymbolSize = neostyle.getMaximumSymbolSize();
                 fontSize = neostyle.getFontSize();
             } catch (Exception e) {
                 //TODO: we can get here if an old style exists, and we have added new fields
             }
         }
         g.setFont(font.deriveFont((float)fontSize));
         siteColor = new Color(siteColor.getRed(), siteColor.getGreen(), siteColor.getBlue(), alpha);
         fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);
         Map<Node, java.awt.Point> nodesMap = new HashMap<Node, java.awt.Point>();
         try {
             monitor.subTask("connecting");
             geoNeo = neoGeoResource.resolve(GeoNeo.class, new SubProgressMonitor(monitor, 10));
             PropertyHeader handler =  PropertyHeader.getNetworkVault(geoNeo.getMainGisNode());
             System.out.println("NetworkRenderer resolved geoNeo '"+geoNeo.getName()+"' from resource: "+neoGeoResource.getIdentifier());
             String selectedProp = geoNeo.getPropertyName();
             Double redMinValue = geoNeo.getPropertyValueMin();
             Double redMaxValue = geoNeo.getPropertyValueMax();
             Double lesMinValue = geoNeo.getMinPropertyValue();
             Double moreMaxValue = geoNeo.getMaxPropertyValue();
             Select select = Select.findSelectByValue(geoNeo.getSelectName());
             //IBlackboard blackboard = getContext().getMap().getBlackboard();
             String starProperty = getSelectProperty(geoNeo);
             Pair<Point,Long> starPoint = (Pair<Point,Long>)getContext().getLayer().getBlackboard().get(BLACKBOARD_START_ANALYSER);
             Node starNode = null;
             if(starPoint != null) {
                 System.out.println("Have star selection: "+starPoint);
             }
 
             isAggregatedProperties = selectedProp != null && INeoConstants.PROPERTY_ALL_CHANNELS_NAME.equals(selectedProp);
             aggregationList = geoNeo.getAggregatedProperties();
             setCrsTransforms(neoGeoResource.getInfo(null).getCRS());
             Envelope bounds_transformed = getTransformedBounds();
             Envelope data_bounds = geoNeo.getBounds();
             boolean drawFull = true;
             boolean drawLite = true;
             boolean drawLabels = true;
             if (bounds_transformed == null) {
                 drawFull = false;
                 drawLite = false;
                 drawLabels = false;
             }else if (data_bounds != null && data_bounds.getHeight()>0 && data_bounds.getWidth()>0) {
                 double dataScaled = (bounds_transformed.getHeight() * bounds_transformed.getWidth())
                         / (data_bounds.getHeight() * data_bounds.getWidth());
                 long count = geoNeo.getCount();
                 if (NeoLoaderPlugin.getDefault().getPreferenceStore().getBoolean(DataLoadPreferences.NETWORK_COMBINED_CALCULATION)) {
                     count = getAverageCount(monitor);
                 }
 
                 double countScaled = dataScaled * count;
                 drawLabels = countScaled < maxSitesLabel;
                 drawFull = countScaled < maxSitesFull;
                 drawLite = countScaled < maxSitesLite;
                 if (drawFull && scaleSectors) {
                     drawSize *= Math.sqrt(maxSitesFull) / (3 * Math.sqrt(countScaled));
                     drawSize = Math.min(drawSize, maxSymbolSize);
                 }
                 // expand the boundary to include sites just out of view (so partial sectors can be see)
                 bounds_transformed.expandBy(0.75 * (bounds_transformed.getHeight() + bounds_transformed.getWidth()));
             }
 
             g.setColor(drawColor);
             int count = 0;
             monitor.subTask("drawing");
             Coordinate world_location = new Coordinate(); // single object for re-use in transform below (minimize object creation)
             long startTime = System.currentTimeMillis();
             for(GeoNode node:geoNeo.getGeoNodes(bounds_transformed)) {
                 Coordinate location = node.getCoordinate();
 
                 if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                     continue; // Don't draw points outside viewport
                 }
                 try {
                     JTS.transform(location, world_location, transform_d2w);
                 } catch (Exception e) {
                     //JTS.transform(location, world_location, transform_w2d.inverse());
                 }
 
                 java.awt.Point p = getContext().worldToPixel(world_location);
                 Color borderColor = g.getColor();
                 boolean selected = false;
                 if (geoNeo.getSelectedNodes().contains(node.getNode())) {
                     borderColor = COLOR_SITE_SELECTED;
                     selected = true;
                 } else {
                     for (Node rnode:node.getNode().traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.CHILD, Direction.BOTH)){
                         if (geoNeo.getSelectedNodes().contains(rnode)) {
                             selected = true;
                             break;
                         }
                     }
                     if(!selected) {
                         DELTA_LOOP: for (Node rnode:node.getNode().traverse(Traverser.Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.MISSING, Direction.INCOMING, NetworkRelationshipTypes.DIFFERENT, Direction.INCOMING)){
                             if (geoNeo.getSelectedNodes().contains(rnode)) {
                                 selected = true;
                                 break;
                             } else {
                                 for (Node xnode:rnode.traverse(Traverser.Order.BREADTH_FIRST, new StopEvaluator(){
 
                                     @Override
                                     public boolean isStopNode(TraversalPosition currentPos) {
                                         return "delta_report".equals(currentPos.currentNode().getProperty("type",""));
                                     }}, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.CHILD, Direction.INCOMING)){
                                     if (geoNeo.getSelectedNodes().contains(xnode)) {
                                         selected = true;
                                         break DELTA_LOOP;
                                     }
                                 }
                             }
                         }
                     }
                 }
                 renderSite(g, p, borderColor, siteColor, drawSize, drawFull, drawLite, selected);
                 if (drawFull) {
                     int countOmnis = 0;
                     double[] label_position_angles = new double[] {0, 90};
                     try {
                         int s = 0;
                         for (Relationship relationship : node.getNode().getRelationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                             // for(Relationship
                             // relationship:node.getNode().getRelationships(NetworkLoader.NetworkRelationshipTypes.CHILD,
                             // Direction.OUTGOING)){
                             Node child = relationship.getEndNode();
                             if (child.hasProperty("type") && child.getProperty("type").toString().equals("sector")) {
                                 // double azimuth = Double.NaN;
                                 double beamwidth = handler.getBeamwidth(child, 360.0);
                                 Color colorToFill = getSectorColor(select, child, fillColor, selectedProp, redMinValue,
                                         redMaxValue, lesMinValue, moreMaxValue);
 
                                 // for (String key : child.getPropertyKeys()) {
                                 // if (key.toLowerCase().contains("azimuth")) {
                                 // Object value = child.getProperty(key);
                                 // if (value instanceof Integer) {
                                 // azimuth = (Integer)value;
                                 // } else {
                                 // try {
                                 // azimuth = Integer.parseInt(value.toString());
                                 // } catch (Exception e) {
                                 // }
                                 // }
                                 // }
                                 // }
                                 Double azimuth = handler.getAzimuth(child);
                                 if (azimuth == null) {
                                     azimuth = Double.NaN;
                                     continue;
                                 }
                                 borderColor = drawColor;
                                 if (starPoint != null && starPoint.right().equals(child.getId())) {
                                     borderColor = COLOR_SECTOR_STAR;
                                     starNode = child;
                                 } else
                                 if (geoNeo.getSelectedNodes().contains(child)) {
                                     borderColor = COLOR_SECTOR_SELECTED;
                                 }
                                 // put sector information in to blackboard
 
                                 Point centerPoint = renderSector(g, p, azimuth, beamwidth, colorToFill, borderColor, drawSize);
                                 nodesMap.put(child, centerPoint);
                                 if (s < label_position_angles.length) {
                                     label_position_angles[s] = azimuth;
                                 }
                                 // g.setColor(drawColor);
                                 // g.rotate(-Math.toRadians(beamwidth/2));
                                 // g.drawString(sector.getString("name"),drawSize,0);
                                 if(beamwidth==360) countOmnis++;
                                 s++;
                             }
                         }
                     } finally {
                         if (base_transform != null) {
                             // recover the normal transform
                             g.setTransform(base_transform);
                             g.setColor(drawColor);
                         }
                     }
                     if (countOmnis>1) {
                         System.err.println("Site "+node+" had "+countOmnis+" omni antennas");
                     }
                     if (drawLabels) {
                         double label_position_angle = Math.toRadians(-90 + (label_position_angles[0] + label_position_angles[1]) / 2.0);
                         int label_x = 5 + (int)(10 * Math.cos(label_position_angle));
                         int label_y = (int)(10 * Math.sin(label_position_angle));
                         g.setColor(labelColor);
                         g.drawString(node.toString(), p.x + label_x, p.y + label_y);
                     }
                     if (base_transform != null) {
                         g.setTransform(base_transform);
                     }
                 }
                 monitor.worked(1);
                 count++;
                 if (monitor.isCanceled())
                     break;
             }
             if (starNode != null && starProperty != null) {
                 drawAnalyser(g, starNode, starPoint.left(), starProperty, nodesMap);
             }
             System.out.println("Network renderer took " + ((System.currentTimeMillis() - startTime) / 1000.0) + "s to draw " + count + " sites from "+neoGeoResource.getIdentifier());
         } catch (TransformException e) {
             throw new RenderException(e);
         } catch (FactoryException e) {
             throw new RenderException(e);
         } catch (IOException e) {
             throw new RenderException(e); // rethrow any exceptions encountered
         } finally {
             if (neoGeoResource != null) {
                 HashMap<Long,Point> idMap = new HashMap<Long,Point>();
                 for(Node node:nodesMap.keySet()){
                     idMap.put(node.getId(), nodesMap.get(node));
                 }
                 getContext().getMap().getBlackboard().put(BLACKBOARD_NODE_LIST, idMap);
             }
             // if (geoNeo != null)
             // geoNeo.close();
             monitor.done();
         }
     }
 
     /**
      * gets average count of geoNeo.getCount() from all resources in map
      * 
      * @return average count
      */
     private Long getAverageCount(IProgressMonitor monitor) {
         long result = 0;
         long count = 0;
         try {
             for (ILayer layer : getContext().getMap().getMapLayers()) {
                 if (layer.getGeoResource().canResolve(GeoNeo.class)) {
                     GeoNeo resource = layer.getGeoResource().resolve(GeoNeo.class, monitor);
                    result += resource.getCount();
                    count++;
                 }
             }
         } catch (IOException e) {
             // TODO Handle IOException
             NeoCorePlugin.error(e.getLocalizedMessage(), e);
             return null;
         }
         return count == 0 ? null : result / count;
     }
 
     /**
      * @param child
      * @return
      */
     private Color getSectorColor(Select select, Node node, Color defColor, String selectedProp, Double redMinValue,
             Double redMaxValue, Double lesMinValue, Double moreMaxValue) {
 
         Color colorToFill = defColor;
         if (selectedProp == null) {
             return colorToFill;
         }
         Double valueD = getNodeValue(node, selectedProp, select, lesMinValue, moreMaxValue);
         if (valueD == null) {
             return colorToFill;
         }
         double value = valueD.doubleValue();
         if (value < redMaxValue || value == redMinValue) {
             if (value >= redMinValue) {
                 colorToFill = COLOR_SELECTED;
             } else if (value >= lesMinValue) {
                 colorToFill = COLOR_LESS;
             }
         } else if (value < moreMaxValue) {
             colorToFill = COLOR_MORE;
         }
         return colorToFill;
     }
 
     /**
      * @param node
      * @param propertyName
      * @param select
      * @param minValue
      * @param range
      * @return
      */
     private Double getNodeValue(Node node, String propertyName, Select select, double minValue, double maxValue) {
 
         if (isAggregatedProperties) {
             Double min = null;
             Double max = null;
             int count = 0;
             double sum = (double)0;
             for (String singleProperties : aggregationList) {
                 if (node.hasProperty(singleProperties)) {
                     double doubleValue = ((Number)node.getProperty(singleProperties)).doubleValue();
                     if (select == Select.FIRST) {
                         return doubleValue;
                     } else if (select == Select.EXISTS) {
                         if (doubleValue == minValue || (doubleValue >= minValue && doubleValue < maxValue)) {
                             return doubleValue;
                         }
                     }
                     min = min == null ? doubleValue : Math.min(doubleValue, min);
                     max = max == null ? doubleValue : Math.max(doubleValue, max);
                     sum += doubleValue;
                     count++;
                 }
             }
             switch (select) {
             case AVERAGE:
                 return count == 0 ? null : sum / (double)count;
             case MAX:
                 return max;
             case MIN:
                 return min;
             }
             return null;
         }
         return node.hasProperty(propertyName) ? ((Number)node.getProperty(propertyName)).doubleValue() : null;
     }
 
     /**
      * Render the sector symbols based on the point and azimuth. We simply save the graphics
      * transform, then modify the graphics through the appropriate transformations (origin to site,
      * and rotations for drawing the lines and arcs).
      * 
      * @param g
      * @param p
      * @param azimuth
      */
     private java.awt.Point renderSector(Graphics2D g, java.awt.Point p, double azimuth, double beamwidth, Color fillColor,
             Color borderColor, int drawSize) {
         Color oldColor = g.getColor();
         java.awt.Point result = null;
         if(base_transform==null) base_transform = g.getTransform();
         if(beamwidth<10) beamwidth = 10;
         g.setTransform(base_transform);
         g.translate(p.x, p.y);
         if (beamwidth >= 360.0) {
             g.setColor(fillColor);
             g.fillOval(-drawSize, -drawSize, 2 * drawSize, 2 * drawSize);
             g.setColor(borderColor);
             g.drawOval(-drawSize, -drawSize, 2 * drawSize, 2 * drawSize);
             result = p;
 
         } else {
             double angdeg = -90 + azimuth - beamwidth / 2.0;
             g.rotate(Math.toRadians(angdeg));
             g.setColor(fillColor);
             g.fillArc(-drawSize, -drawSize, 2 * drawSize, 2 * drawSize, 0, -(int)beamwidth);
             // TODO correct gets point
 
             g.setColor(borderColor);
             g.drawArc(-drawSize, -drawSize, 2 * drawSize, 2 * drawSize, 0, -(int)beamwidth);
             g.drawLine(0, 0, drawSize, 0);
             g.rotate(Math.toRadians(beamwidth / 2));
             double xLoc = drawSize / 2;
             double yLoc = 0;
             AffineTransform transform = g.getTransform();
             int x = (int)(transform.getScaleX() * xLoc + transform.getShearX() * yLoc + transform.getTranslateX());
             int y = (int)(transform.getShearY() * xLoc + transform.getScaleY() * yLoc + transform.getTranslateY());
             g.rotate(Math.toRadians(beamwidth / 2));
             g.drawLine(0, 0, drawSize, 0);
             result = new java.awt.Point(x, y);
             g.setColor(oldColor);
         }
         return result;
     }
 
     /**
      * This one is very simple, just draw a circle at the site location.
      * 
      * @param g
      * @param p
      * @param borderColor
      * @param drawSize 
      */
     private void renderSite(Graphics2D g, java.awt.Point p, Color borderColor, Color fillColor, int drawSize, boolean drawFull, boolean drawLite, boolean selected) {
         Color oldColor = g.getColor();
         if (drawFull) {
             if(selected) renderSelectionGlow(g, p, drawSize * 4);
             drawSize /= 4;
             if (drawSize < 2) drawSize = 2;
             g.setColor(fillColor);
             g.fillOval(p.x - drawSize, p.y - drawSize, 2 * drawSize, 2 * drawSize);
             g.setColor(borderColor);
             g.drawOval(p.x - drawSize, p.y - drawSize, 2 * drawSize, 2 * drawSize);
         } else if (drawLite) {
             if(selected) renderSelectionGlow(g, p, 20);
             g.setColor(borderColor);
             g.drawOval(p.x - 5, p.y - 5, 10, 10);
         } else {
             if(selected) renderSelectionGlow(g, p, 20);
             g.setColor(borderColor);
             g.drawRect(p.x - 1, p.y - 1, 3, 3);
         }
         g.setColor(oldColor);
     }
 
     /**
      * This method draws a fading glow around a point for selected site/sectors
      *
      * @param g
      * @param p
      * @param drawSize
      */
     private void renderSelectionGlow(Graphics2D g, java.awt.Point p, int drawSize) {
         Color highColor = new Color(COLOR_SITE_SELECTED.getRed(), COLOR_SITE_SELECTED.getGreen(), COLOR_SITE_SELECTED.getBlue(), 8);
         g.setColor(highColor);
         for(;drawSize > 2; drawSize *= 0.8) {
             g.fillOval(p.x - drawSize, p.y - drawSize, 2 * drawSize, 2 * drawSize);
         }
     }
 
     /**
      * perform and draw star analyser
      * 
      * @param context context
      */
     private void drawAnalyser(Graphics2D g, Node mainNode, Point starPoint, String property, Map<Node, Point> nodesMap) {
         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             Point point = nodesMap.get(mainNode);
             if (point != null) {
                 starPoint = point;
             }
             drawMainNode(g, mainNode, starPoint);
             Object propertyValue = mainNode.getProperty(property,null);
             if(propertyValue!=null) {
                 for (Node node : nodesMap.keySet()) {
                     if (node.hasProperty(property) && propertyValue.equals(node.getProperty(property))) {
                         Point nodePoint = nodesMap.get(node);
                         g.setColor(getLineColor(mainNode, node));
                         g.drawLine(starPoint.x, starPoint.y, nodePoint.x, nodePoint.y);
                     }
                 }
             }
 
         } finally {
             tx.finish();
         }
 
     }
 
     /**
      * get Line color
      * 
      * @param mainNiode main node
      * @param node node
      * @return Line color
      */
     private Color getLineColor(Node mainNiode, Node node) {
         return Color.BLACK;
     }
 
     /**
      * get property to analyze
      * @param geoNeo 
      * 
      * @return property name
      */
     private String getSelectProperty(GeoNeo geoNeo) {
         String result = null;
         Transaction transaction = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             result = geoNeo.getProperty(INeoConstants.PROPERTY_SELECTED_AGGREGATION, "").toString();
             transaction.success();
         } finally {
             transaction.finish();
         }
         return result == null || result.isEmpty() ? null : result;
     }
 
     /**
      * Draw selection of main node
      * 
      * @param context context
      * @param mainNiode main node
      */
     private void drawMainNode(Graphics2D g, Node mainNode, Point point) {
         g.setColor(Color.RED);
         g.fillOval(point.x - 4, point.y - 4, 10, 10);
     }
 
     @Override
     public void render( IProgressMonitor monitor ) throws RenderException {
         Graphics2D g = getContext().getImage().createGraphics();
         render(g, monitor);
     }
 
     /**
      * <p>
      * TODO union with org.amanzi.awe.views.reuse.Select now simple copy enum from
      * org.amanzi.awe.views.reuse.Select
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.1.0
      */
     private enum Select {
         MAX("max"), MIN("min"), AVERAGE("average"), EXISTS("exists"), FIRST("first");
         private final String value;
 
         /**
          * Constructor
          * 
          * @param value - string value
          */
         private Select(String value) {
             this.value = value;
         }
 
         public static Select findSelectByValue(String value) {
             if (value == null) {
                 return null;
             }
             for (Select selection : Select.values()) {
                 if (selection.value.equals(value)) {
                     return selection;
                 }
             }
             return null;
         }
 
         public static String[] getEnumAsStringArray() {
             Select[] enums = Select.values();
             String[] result = new String[enums.length];
             for (int i = 0; i < enums.length; i++) {
                 result[i] = enums[i].value;
             }
             return result;
         }
 
         @Override
         public String toString() {
             return value;
         }
     }
 
 }
