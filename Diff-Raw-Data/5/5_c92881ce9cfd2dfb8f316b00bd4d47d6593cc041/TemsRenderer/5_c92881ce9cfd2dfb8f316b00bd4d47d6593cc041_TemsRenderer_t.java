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
 package org.amanzi.awe.render.drive;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.awt.image.ImageObserver;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IStyleBlackboard;
 import net.refractions.udig.project.internal.render.Renderer;
 import net.refractions.udig.project.internal.render.impl.RendererImpl;
 import net.refractions.udig.project.render.RenderException;
 import net.refractions.udig.ui.PlatformGIS;
 
 import org.amanzi.awe.catalog.neo.GeoConstant;
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.catalog.neo.GeoNeo.GeoNode;
 import org.amanzi.awe.neostyle.NeoStyle;
 import org.amanzi.awe.neostyle.NeoStyleContent;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.DriveEvents;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.index.MultiPropertyIndex;
 import org.amanzi.neo.index.PropertyIndex.NeoIndexRelationshipTypes;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.geotools.brewer.color.BrewerPalette;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.util.index.LuceneIndexService;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * <p>
  * Renderer for GeoNeo with GisTypes==GisTypes.Tems
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class TemsRenderer extends RendererImpl implements Renderer {
     private MathTransform transform_d2w;
     private MathTransform transform_w2d;
     private AffineTransform base_transform = null; // save original graphics transform for repeated
     // re-use
     private Color drawColor = Color.BLACK;
     private Color fillColor = new Color(200, 128, 255, (int)(0.6 * 255.0));
     private Color labelColor = Color.DARK_GRAY;
     private Node aggNode;
     private String mpName;
     private String msName;
     private boolean normalSiteName;
     private boolean notMsLabel;
     private int eventIconSize;
     private int eventIconOffset;
     private boolean scaleIcons = false;
     private int eventIconBaseSize = 12;
     private int eventIconMaxSize = 32;
     private static final int[] eventIconSizes = new int[] {6, 8, 12, 16, 32, 48, 64};
     private static final Color COLOR_HIGHLIGHTED = Color.CYAN;;
     private static final Color COLOR_HIGHLIGHTED_SELECTED = Color.RED;
     private static final Color FADE_LINE = new Color(127, 127, 127, 127);
 
     private LuceneIndexService index;
     private boolean notMpLabel;
 
     private static int getIconSize(int size) {
         int lower = eventIconSizes[0];
         for (int s : eventIconSizes) {
             if (size > s) {
                 lower = s;
             }
         }
         return lower;
     }
 
     private static int calcIconSize(int min, int max, int minT, int maxT, double count) {
         int iconSize = min;
         try {
             double ratio = (maxT - count) / (maxT - minT);
             iconSize = min + (int)(ratio * (max - min));
         } catch (Exception e) {
             System.out.println("Error calculating icons sizes: " + e);
         }
         return getIconSize(iconSize);
     }
 
     public TemsRenderer() {
         index = NeoServiceProvider.getProvider().getIndexService();
     }
 
     @Override
     public void render(Graphics2D destination, IProgressMonitor monitor) throws RenderException {
         ILayer layer = getContext().getLayer();
         // Are there any resources in the layer that respond to the GeoNeo class (should be the case
         // if we found a Neo4J database with GeoNeo data)
         IGeoResource resource = layer.findGeoResource(GeoNeo.class);
         if (resource != null) {
             renderGeoNeo(destination, resource, monitor);
         }
     }
 
     @Override
     public void render(IProgressMonitor monitor) throws RenderException {
         Graphics2D g = getContext().getImage().createGraphics();
         render(g, monitor);
     }
 
     private Pair<MathTransform, MathTransform> setCrsTransforms(CoordinateReferenceSystem dataCrs) throws FactoryException {
         boolean lenient = true; // needs to be lenient to work on uDIG 1.1 (otherwise we get error:
         // bursa wolf parameters required
         CoordinateReferenceSystem worldCrs = context.getCRS();
         Pair<MathTransform, MathTransform> oldTransform = new Pair<MathTransform, MathTransform>(transform_d2w, transform_w2d);
         this.transform_d2w = CRS.findMathTransform(dataCrs, worldCrs, lenient);
         this.transform_w2d = CRS.findMathTransform(worldCrs, dataCrs, lenient);
         return oldTransform;
     }
 
     private void setCrsTransforms(Pair<MathTransform, MathTransform> oldTransform) throws FactoryException {
         this.transform_d2w = oldTransform.left();
         this.transform_w2d = oldTransform.right();
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
      * This method is called to render data from the Neo4j 'GeoNeo' Geo-Resource.
      */
     private void renderGeoNeo(Graphics2D g, IGeoResource neoGeoResource, IProgressMonitor monitor) throws RenderException {
         if (monitor == null)
             monitor = new NullProgressMonitor();
         monitor.beginTask("render drive test data", IProgressMonitor.UNKNOWN);
         GeoNeo geoNeo = null;
         // enable/disable rendering of rectangles for the spatial index
         boolean enableIndexRendering = false;
 
         // Setup default drawing parameters and thresholds (to be modified by style if found)
         int maxSitesLabel = 30;
         int maxSitesFull = 100;
         int maxSitesLite = 1000;
         // int maxSymbolSize = 40;
         int alpha = (int)(0.6 * 255.0);
         int drawSize = 3;
         Font font = g.getFont();
         int fontSize = font.getSize();
         IStyleBlackboard style = getContext().getLayer().getStyleBlackboard();
         NeoStyle neostyle = (NeoStyle)style.get(NeoStyleContent.ID);
         mpName = NeoStyleContent.DEF_MAIN_PROPERTY;
         msName = NeoStyleContent.DEF_SECONDARY_PROPERTY;
         eventIconSize = eventIconBaseSize;
         if (neostyle != null) {
             fillColor = neostyle.getFill();
             drawColor = neostyle.getLine();
             alpha = 255 - (int)((double)neostyle.getSymbolTransparency() / 100.0 * 255.0);
             try {
                 fillColor = neostyle.getFill();
                 drawColor = neostyle.getLine();
                 labelColor = neostyle.getLabel();
                 alpha = 255 - (int)((double)neostyle.getSymbolTransparency() / 100.0 * 255.0);
                 drawSize = 3;
                 maxSitesLabel = neostyle.getLabeling() / 4;
                 maxSitesFull = neostyle.getSmallSymb();
                 maxSitesLite = neostyle.getSmallestSymb() * 10;
                 fontSize = neostyle.getFontSize();
                 mpName = neostyle.getMainProperty();
                 msName = neostyle.getSecondaryProperty();
                 scaleIcons = !neostyle.isFixSymbolSize();
                 eventIconOffset = neostyle.getIconOffset();
                 eventIconBaseSize = getIconSize(neostyle.getSymbolSize());
                 eventIconMaxSize = getIconSize(neostyle.getMaximumSymbolSize());
                 eventIconSize = eventIconBaseSize;
                 if (neostyle.getSymbolSize() < eventIconSizes[0]) {
                     eventIconSize = 0;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 // we can get here if an old style exists, and we have added new fields
             }
         }
         normalSiteName = NeoStyleContent.DEF_MAIN_PROPERTY.equals(mpName);
         notMpLabel = !normalSiteName && NeoStyleContent.DEF_SECONDARY_PROPERTY.equals(mpName);
         notMsLabel = NeoStyleContent.DEF_SECONDARY_PROPERTY.equals(msName);
         g.setFont(font.deriveFont((float)fontSize));
 
         int drawWidth = 1 + 2 * drawSize;
         NeoService neo = NeoServiceProvider.getProvider().getService();
         Transaction tx = neo.beginTx();
         try {
             monitor.subTask("connecting");
             geoNeo = neoGeoResource.resolve(GeoNeo.class, new SubProgressMonitor(monitor, 10));
             String gisName = NeoUtils.getSimpleNodeName(geoNeo.getMainGisNode(), "");
             Iterable<Relationship> relations = geoNeo.getMainGisNode().getRelationships(
                     NetworkRelationshipTypes.LINKED_NETWORK_DRIVE, Direction.INCOMING);
             ArrayList<IGeoResource> networkGeoNeo = new ArrayList<IGeoResource>();
             for (Relationship relationship : relations) {
                 IGeoResource network = getNetwork(relationship.getOtherNode(geoNeo.getMainGisNode()));
                 if (network != null) {
                     networkGeoNeo.add(network);
                 }
             }
             // String selectedProp = geoNeo.getPropertyName();
             aggNode = geoNeo.getAggrNode();
             Map<String, Object> selectionMap = getSelectionMap(geoNeo);
             Long crossHairId1 = null;
             Long crossHairId2 = null;
             List<String> eventList = new ArrayList<String>();
             String selected_events = null;
             if (selectionMap != null) {
                 crossHairId1 = (Long)selectionMap.get(GeoConstant.Drive.SELECT_PROPERTY1);
                 crossHairId2 = (Long)selectionMap.get(GeoConstant.Drive.SELECT_PROPERTY2);
                 selected_events = (String)selectionMap.get(GeoConstant.SELECTED_EVENT);
                 eventList = (List<String>)selectionMap.get(GeoConstant.EVENT_LIST);
                 if (eventList == null) {
                     eventList = new ArrayList<String>();
                 }
             }
             // Integer propertyAdjacency = geoNeo.getPropertyAdjacency();
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
             } else if (data_bounds != null && data_bounds.getHeight() > 0 && data_bounds.getWidth() > 0) {
                 double dataScaled = (bounds_transformed.getHeight() * bounds_transformed.getWidth())
                         / (data_bounds.getHeight() * data_bounds.getWidth());
                 double countScaled = dataScaled * geoNeo.getCount();
                 drawLabels = countScaled < maxSitesLabel;
                 drawFull = countScaled < maxSitesFull;
                 if (scaleIcons && eventIconSize > 0) {
                     if (countScaled < maxSitesFull) {
                         eventIconSize = calcIconSize(eventIconBaseSize, eventIconMaxSize, maxSitesLabel, maxSitesFull, countScaled);
                     } else if (countScaled < maxSitesLite) {
                         eventIconSize = calcIconSize(eventIconSizes[0], eventIconBaseSize, maxSitesFull, maxSitesLite, countScaled);
                     } else {
                         eventIconSize = 0;
                     }
                     if (eventIconSize > eventIconMaxSize)
                         eventIconSize = eventIconMaxSize;
                     // eventIconSize = countScaled * 32 <= maxSitesFull ? 32 :countScaled * 16 <=
                     // maxSitesFull ? 16 : countScaled * 4 <= maxSitesFull ? 12
                     // : countScaled * 2 <= maxSitesFull ? 8 : 6;
                 }
                 drawLite = countScaled < maxSitesLite;
             }
             int trans = alpha;
             if (haveSelectedNodes()) {
                 trans = 25;
             }
             // draw event icon flag
             boolean drawEvents = /* true || */drawFull;
 
             fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), trans);
             g.setColor(drawColor);
             int count = 0;
             monitor.subTask("drawing");
             // single object for re-use in transform below (minimize object creation)
             Coordinate world_location = new Coordinate();
             java.awt.Point prev_p = null;
             java.awt.Point prev_l_p = null;
             java.awt.Point cached_l_p = null;
             GeoNode cached_node = null; // for label positioning
             long startTime = System.currentTimeMillis();
 
             // First we find all selected points to draw with a highlight behind the main points
             ArrayList<Node> selectedPoints = new ArrayList<Node>();
             final Set<Node> selectedNodes = new HashSet<Node>(geoNeo.getSelectedNodes());
 
             // TODO refactor selection point (for example: in draws mp node add method
             // isSelected(node))
             Long beginTime = null;
             Long endTime = null;
             BrewerPalette palette = null;
             if (selectionMap != null) {
                 String paletteName = (String)selectionMap.get(GeoConstant.Drive.SELECT_PALETTE);
                 try {
                     palette = PlatformGIS.getColorBrewer().getPalette(paletteName);
                 } catch (Exception e) {
                     palette = null;
                 }
                 beginTime = (Long)selectionMap.get(GeoConstant.Drive.BEGIN_TIME);
                 endTime = (Long)selectionMap.get(GeoConstant.Drive.END_TIME);
                 if (beginTime != null && endTime != null && beginTime <= endTime) {
                     MultiPropertyIndex<Long> timestampIndex = NeoUtils.getTimeIndexProperty(geoNeo.getName());
                     timestampIndex.initialize(NeoServiceProvider.getProvider().getService(), null);
                     for (Node node : timestampIndex.searchTraverser(new Long[] {beginTime}, new Long[] {endTime})) {
                        if (!node.hasRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING)) {
                             continue;
                         }
                        Node mpNode = node.getSingleRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING).getOtherNode(
                                 node);
                         selectedNodes.add(mpNode);
                     }
                 }
             }
             boolean needDrawLines = !networkGeoNeo.isEmpty() & beginTime != null && endTime != null && beginTime <= endTime;
             boolean haveSelectedEvents = needDrawLines && palette != null && selected_events != null;
             boolean allEvents = haveSelectedEvents && selected_events.equals(GeoConstant.ALL_EVENTS);
             Color eventColor = null;
             if (haveSelectedEvents && !allEvents) {
                 int i = eventList.indexOf(selected_events);
                 if (i < 0) {
                     i = 0;
                 }
                 Color[] colors = palette.getColors(palette.getMaxColors());
                 int index = i % colors.length;
                 eventColor = colors[index];
             }
             // TODO is it really necessary draw selection before drawing all mp node instead drawing
             // in one traverse?
             for (Node node : selectedNodes) {
                 if (NeoUtils.isFileNode(node)) {
                     // Select all 'mp' nodes in that file
                     for (Node rnode : node.traverse(Traverser.Order.BREADTH_FIRST, new StopEvaluator() {
                         @Override
                         public boolean isStopNode(TraversalPosition currentPos) {
                             return !currentPos.isStartNode() && !NeoUtils.isDriveMNode(currentPos.currentNode());
                         }
                     }, new ReturnableEvaluator() {
 
                         @Override
                         public boolean isReturnableNode(TraversalPosition currentPos) {
                             return NeoUtils.isDrivePointNode(currentPos.currentNode());
                         }
                     }, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING, GeoNeoRelationshipTypes.CHILD, Direction.INCOMING)) {
                         selectedPoints.add(rnode);
                     }
                 } else {
                     // Traverse backwards on CHILD relations to closest 'mp' Point
                     for (@SuppressWarnings("unused")
                     Node rnode : node.traverse(Traverser.Order.DEPTH_FIRST, new StopEvaluator() {
                         @Override
                         public boolean isStopNode(TraversalPosition currentPos) {
                             return NeoUtils.isDrivePointNode(currentPos.currentNode());
                         }
                     }, new ReturnableEvaluator() {
 
                         @Override
                         public boolean isReturnableNode(TraversalPosition currentPos) {
                             return NeoUtils.isDrivePointNode(currentPos.currentNode());
                         }
                     }, GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING)) {
                         selectedPoints.add(rnode);
                         break;
                     }
                 }
             }
             // Now draw the selected points highlights
             for (Node rnode : selectedPoints) {
                 GeoNode node = new GeoNode(rnode);
                 Coordinate location = node.getCoordinate();
                 if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                     continue; // Don't draw points outside viewport
                 }
                 try {
                     JTS.transform(location, world_location, transform_d2w);
                 } catch (Exception e) {
                     continue;
                 }
                 java.awt.Point p = getContext().worldToPixel(world_location);
                 if (prev_p != null && prev_p.x == p.x && prev_p.y == p.y) {
                     prev_p = p;
                     continue;
                 } else {
                     prev_p = p;
                 }
                 renderSelectedPoint(g, p, drawSize, drawFull, drawLite);
             }
             Node indexNode = null;
             HashMap<String, Integer> colorErrors = new HashMap<String, Integer>();
             prev_p = null;// else we do not show selected node
             // Now draw the actual points
             for (GeoNode node : geoNeo.getGeoNodes(bounds_transformed)) {
                 if (enableIndexRendering && indexNode == null) {
                     indexNode = getIndexNode(node);
                 }
                 Coordinate location = node.getCoordinate();
 
                 if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                     continue; // Don't draw points outside viewport
                 }
                 try {
                     JTS.transform(location, world_location, transform_d2w);
                 } catch (Exception e) {
                     // JTS.transform(location, world_location, transform_w2d.inverse());
                 }
 
                 java.awt.Point p = getContext().worldToPixel(world_location);
                 if (prev_p != null && prev_p.x == p.x && prev_p.y == p.y) {
                     prev_p = p;
                     continue;
                 } else {
                     prev_p = p;
                 }
 
                 Color nodeColor = fillColor;
                 try {
                     nodeColor = getNodeColor(node.getNode(), fillColor);
                     // nodeColor = getColorOfMpNode(select, node.getNode(), fillColor,
                     // selectedProp, redMinValue, redMaxValue,
                     // lesMinValue, moreMaxValue);
                 } catch (RuntimeException e) {
                     String errName = e.toString();
                     if (colorErrors.containsKey(errName)) {
                         colorErrors.put(errName, colorErrors.get(errName) + 1);
                     } else {
                         colorErrors.put(errName, 1);
                     }
                 }
                 Color borderColor = g.getColor();
                 if (selectedNodes.size() > 0) {
                     if (selectedNodes.contains(node.getNode())) {
                         borderColor = COLOR_HIGHLIGHTED;
                     }
                 }
                 long id = node.getNode().getId();
                 if ((crossHairId1 != null && id == crossHairId1) || (crossHairId2 != null && crossHairId2 == id)) {
                     borderColor = COLOR_HIGHLIGHTED_SELECTED;
                 }
 
                 renderPoint(g, p, borderColor, nodeColor, drawSize, drawWidth, drawFull, drawLite);
                 if (drawLabels) {
                     double theta = 0.0;
                     double dx = 0.0;
                     double dy = 0.0;
                     if (prev_l_p == null) {
                         prev_l_p = p;
                         cached_l_p = p; // so we can draw first point using second point settings
                         cached_node = node;
                     } else {
                         try {
                             dx = p.x - prev_l_p.x;
                             dy = p.y - prev_l_p.y;
                             if (Math.abs(dx) < Math.abs(dy) / 2) {
                                 // drive goes north-south
                                 theta = 0;
                             } else if (Math.abs(dy) < Math.abs(dx) / 2) {
                                 // drive goes east-west
                                 theta = Math.PI / 2;
                             } else if (dx * dy < 0) {
                                 // drive has negative slope
                                 theta = -Math.PI / 4;
                             } else {
                                 theta = Math.PI / 4;
                             }
                         } catch (Exception e) {
                         }
                     }
                     if (Math.abs(dx) > 20 || Math.abs(dy) > 20) {
                         // if (drawLabels) {
                         renderLabel(g, count, node, p, theta);
                         // }
                         // if (drawEvents) {
                         // renderEvents(g, node, p, theta);
                         // }
                         if (cached_node != null) {
                             // if (drawLabels) {
                             renderLabel(g, 0, cached_node, cached_l_p, theta);
                             // }
                             // if (drawEvents) {
                             // renderEvents(g, cached_node, cached_l_p, theta);
                             // }
                             cached_node = null;
                             cached_l_p = null;
                         }
                         prev_l_p = p;
                     }
                 }
                 if (base_transform != null) {
                     // recover the normal transform
                     g.setTransform(base_transform);
                     g.setColor(drawColor);
                     // base_transform = null;
                 }
                 monitor.worked(1);
                 count++;
                 if (monitor.isCanceled())
                     break;
                 // TODO refactor
                 final Node mpNode = node.getNode();
                 if (needDrawLines) {
                     Long time = NeoUtils.getNodeTime(mpNode);
                     // if (true) {
                     if (time != null && time >= beginTime && time <= endTime) {
                         Color lineColor;
                         if (haveSelectedEvents) {
                             Set<String> events = NeoUtils.getEventsList(mpNode, null);
                             if (!events.isEmpty() && (allEvents || events.contains(selected_events))) {
                                 if (allEvents) {
                                     int i = eventList.indexOf(events.iterator().next());
                                     if (i < 0) {
                                         i = 0;
                                     }
                                     Color[] colors = palette.getColors(palette.getMaxColors());
                                     int index = i % colors.length;
                                     eventColor = colors[index];
                                 }
                                 lineColor = eventColor;
                             } else {
                                 lineColor = FADE_LINE;
                             }
                         } else {
                             lineColor = FADE_LINE;
                         }
                         Relationship relation = mpNode.getSingleRelationship(NetworkRelationshipTypes.DRIVE, Direction.INCOMING);
                         if (relation != null) {
                             Node sectorDrive = relation.getOtherNode(mpNode);
 
                             for (Relationship relationSector : sectorDrive.getRelationships(NetworkRelationshipTypes.SECTOR,
                                     Direction.OUTGOING)) {
                                 Node sector = null;
                                 Object networkGisName = relationSector.getProperty(INeoConstants.NETWORK_GIS_NAME);
                                 IGeoResource networkGisNode = null;
                                 for (IGeoResource networkResource : networkGeoNeo) {
                                     GeoNeo networkGis = networkResource.resolve(GeoNeo.class, null);
                                     if (networkGisName.equals(NeoUtils.getSimpleNodeName(networkGis.getMainGisNode(), ""))) {
                                         sector = relationSector.getOtherNode(sectorDrive);
                                         networkGisNode = networkResource;
                                         break;
                                     }
                                 }
                                 if (sector != null) {
                                     Pair<MathTransform, MathTransform> driveTransform = setCrsTransforms(networkGisNode.getInfo(
                                             monitor).getCRS());// TODO
                                     Node site = sector.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING)
                                             .getOtherNode(sector);
                                     GeoNode siteGn = new GeoNode(site);
                                     location = siteGn.getCoordinate();
                                     try {
                                         JTS.transform(location, world_location, transform_d2w);
                                     } catch (Exception e) {
                                         // JTS.transform(location, world_location,
                                         // transform_w2d.inverse());
                                     }
                                     java.awt.Point pSite = getContext().worldToPixel(world_location);
                                     pSite = getSectorCenter(g, sector, pSite);
 
                                     Color oldColor = g.getColor();
                                     g.setColor(lineColor);
                                     g.drawLine(p.x, p.y, pSite.x, pSite.y);
                                     g.setColor(oldColor);
                                     // restore old transform;
                                     setCrsTransforms(driveTransform);
                                 }
                             }
                         }
                     }
                 }
             }
             if (cached_node != null && drawLabels) {
                 renderLabel(g, 0, cached_node, cached_l_p, 0);
             }
             prev_p = null;
             prev_l_p = null;
             cached_node = null;
             if (eventIconSize > 0) {
                 for (Node node1 : index.getNodes(INeoConstants.EVENTS_LUCENE_INDEX_NAME, gisName)) {
                     if (monitor.isCanceled())
                         break;
                     GeoNode node = new GeoNode(node1);
                     Coordinate location = node.getCoordinate();
 
                     if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                         continue; // Don't draw points outside viewport
                     }
                     try {
                         JTS.transform(location, world_location, transform_d2w);
                     } catch (Exception e) {
                         // JTS.transform(location, world_location, transform_w2d.inverse());
                     }
 
                     java.awt.Point p = getContext().worldToPixel(world_location);
                     if (prev_p != null && prev_p.x == p.x && prev_p.y == p.y) {
                         prev_p = p;
                         continue;
                     } else {
                         prev_p = p;
                     }
                     double theta = 0.0;
                     double dx = 0.0;
                     double dy = 0.0;
                     if (prev_l_p == null) {
                         prev_l_p = p;
                         cached_l_p = p; // so we can draw first point using second point settings
                         cached_node = node;
                     } else {
                         try {
                             dx = p.x - prev_l_p.x;
                             dy = p.y - prev_l_p.y;
                             if (Math.abs(dx) < Math.abs(dy) / 2) {
                                 // drive goes north-south
                                 theta = 0;
                             } else if (Math.abs(dy) < Math.abs(dx) / 2) {
                                 // drive goes east-west
                                 theta = Math.PI / 2;
                             } else if (dx * dy < 0) {
                                 // drive has negative slope
                                 theta = -Math.PI / 4;
                             } else {
                                 theta = Math.PI / 4;
                             }
                         } catch (Exception e) {
                         }
                     }
                     if (Math.abs(dx) > 20 || Math.abs(dy) > 20) {
                         renderEvents(g, node, p, theta);
                         if (cached_node != null) {
                             renderEvents(g, cached_node, cached_l_p, theta);
                             cached_node = null;
                             cached_l_p = null;
                         }
                         prev_l_p = p;
                     }
 
                 }
                 if (cached_node != null) {
                     renderEvents(g, cached_node, cached_l_p, 0);
                 }
             }
             for (String errName : colorErrors.keySet()) {
                 int errCount = colorErrors.get(errName);
                 System.err.println("Error determining color of " + errCount + " nodes: " + errName);
             }
             if (indexNode != null) {
                 renderIndex(g, bounds_transformed, indexNode);
             }
             System.out.println("Drive renderer took " + ((System.currentTimeMillis() - startTime) / 1000.0) + "s to draw " + count
                     + " points");
             tx.success();
         } catch (TransformException e) {
             throw new RenderException(e);
         } catch (FactoryException e) {
             throw new RenderException(e);
         } catch (IOException e) {
             throw new RenderException(e); // rethrow any exceptions encountered
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             tx.finish();
             // if (geoNeo != null)
             // geoNeo.close();
             monitor.done();
 
         }
     }
 
     /**
      * Gets center of sector
      * 
      * @param sector sector mode
      * @param pSite site coordinate
      * @return sector coordinate
      */
     private Point getSectorCenter(Graphics2D g, Node sector, Point pSite) {
         // double beamwidth = ((Number)sector.getProperty("beamwidth", 360.0)).doubleValue();
         double azimuth = ((Number)sector.getProperty("azimuth", Double.NaN)).doubleValue();
         if (azimuth == Double.NaN) {
             return pSite;
         }
         double angdeg = -90 + azimuth;
         AffineTransform transform2 = new AffineTransform(g.getTransform());
         transform2.translate(pSite.x, pSite.y);
         transform2.rotate(Math.toRadians(angdeg), 0, 0);
         double xLoc = 10;
         double yLoc = 0;
         transform2.concatenate(g.getTransform());
         int x = (int)(transform2.getScaleX() * xLoc + transform2.getShearX() * yLoc + transform2.getTranslateX());
         int y = (int)(transform2.getShearY() * xLoc + transform2.getScaleY() * yLoc + transform2.getTranslateY());
         return new Point(x, y);
     }
 
     /**
      * @param otherNode
      * @return
      */
     private IGeoResource getNetwork(Node networkNode) {
         try {
             List<ILayer> layers = getContext().getMap().getMapLayers();
             for (ILayer iLayer : layers) {
                 if (iLayer.getGeoResource().canResolve(GeoNeo.class)) {
                     GeoNeo resource;
                     resource = iLayer.getGeoResource().resolve(GeoNeo.class, null);
 
                     if (resource.getMainGisNode().equals(networkNode)) {
                         return iLayer.getGeoResource();
                     }
                 }
             }
             return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     private Node getIndexNode(GeoNode node) {
         try {
             System.out.println("Searching for index nodes on node: " + node.getName());
             Node endNode = node.getNode();
             System.out.println("Searching for index nodes on node: id:" + endNode.getId() + ", name:"
                     + endNode.getProperty("name", null) + ", type:" + endNode.getProperty("type", null) + ", index:"
                     + endNode.getProperty("index", null) + ", level:" + endNode.getProperty("level", null) + ", max:"
                     + endNode.getProperty("max", null) + ", min:" + endNode.getProperty("min", null));
             for (Relationship relationship : node.getNode().getRelationships(NeoIndexRelationshipTypes.IND_CHILD, Direction.INCOMING)) {
                 endNode = relationship.getStartNode();
                 System.out.println("Trying possible index node: id:" + endNode.getId() + ", name:"
                         + endNode.getProperty("name", null) + ", type:" + endNode.getProperty("type", null) + ", index:"
                         + endNode.getProperty("index", null) + ", level:" + endNode.getProperty("level", null) + ", max:"
                         + endNode.getProperty("max", null) + ", min:" + endNode.getProperty("min", null));
                 int[] index = (int[])endNode.getProperty("index", new int[0]);
                 if (index.length == 2) {
                     return endNode;
                 }
             }
         } catch (Exception e) {
             System.err.println("Failed to find index node: " + e);
             // e.printStackTrace(System.err);
         }
         return null;
     }
 
     private void renderIndex(Graphics2D g, Envelope bounds_transformed, Node indexNode) {
         Coordinate world_location = new Coordinate();
         try {
             INDEX_LOOP: for (Node index : indexNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH,
                     ReturnableEvaluator.ALL_BUT_START_NODE, NeoIndexRelationshipTypes.IND_CHILD, Direction.BOTH)) {
                 int[] ind = (int[])index.getProperty("index", new int[0]);
                 if (ind.length == 2) {
                     double[] max = (double[])index.getProperty("max", new double[0]);
                     double[] min = (double[])index.getProperty("min", new double[0]);
                     int level = (Integer)index.getProperty("level", 0);
                     if (max.length == 2 && min.length == 2) {
                         drawColor = new Color(0.5f, 0.5f, 0.5f, 1.0f - Math.max(0.1f, 0.8f * (5.0f - level) / 5.0f));
                         g.setColor(drawColor);
                         Coordinate[] c = new Coordinate[2];
                         java.awt.Point[] p = new java.awt.Point[2];
                         c[0] = new Coordinate(min[1], max[0]);
                         c[1] = new Coordinate(max[1], min[0]);
                         for (int i = 0; i < 2; i++) {
                             if (bounds_transformed != null && !bounds_transformed.contains(c[i])) {
                                 continue INDEX_LOOP;
                             }
                             try {
                                 JTS.transform(c[i], world_location, transform_d2w);
                             } catch (Exception e) {
                                 // JTS.transform(location, world_location, transform_w2d.inverse());
                             }
 
                             p[i] = getContext().worldToPixel(world_location);
                         }
                         if (p[1].x > p[0].x && p[1].y > p[0].y) {
                             g.drawRect(p[0].x, p[0].y, p[1].x - p[0].x, p[1].y - p[0].y);
                             g.drawString("" + ind[0] + ":" + ind[1] + "[" + level + "]", p[0].x, p[0].y);
                         } else {
                             System.err.println("Invalid index bbox: " + p[0] + ":" + p[1]);
                             g.drawRect(Math.min(p[0].x, p[1].x), Math.min(p[0].y, p[1].y), Math.abs(p[1].x - p[0].x), Math
                                     .abs(p[1].y - p[0].y));
                         }
                     }
                 }
             }
         } catch (Exception e) {
             System.err.println("Failed to draw index: " + e);
             e.printStackTrace(System.err);
         }
     }
 
     @SuppressWarnings("unchecked")
     private Map<String, Object> getSelectionMap(GeoNeo geoNeo) {
         Map<String, Object> selectionMap = (Map<String, Object>)geoNeo.getProperties(GeoNeo.DRIVE_INQUIRER);
         return selectionMap;
     }
 
     /**
      * @return true if drive have selected node
      */
     private boolean haveSelectedNodes() {
         return aggNode != null;
     }
 
     private void renderLabel(Graphics2D g, int count, GeoNode node, java.awt.Point p, double theta) {
         if (base_transform == null)
             base_transform = g.getTransform();
         g.setTransform(base_transform);
         g.translate(p.x, p.y);
         g.rotate(-theta);
         g.setColor(labelColor);
         // g.drawString(""+Integer.toString(count)+": "+node.toString(), 10, 5);
         g.drawString(getPointLabel(node), 10, 5);
     }
 
     /**
      *Gets label of mp node
      * 
      * @param node GeoNode
      * @return String
      */
     private String getPointLabel(GeoNode node) {
         StringBuilder pointName = new StringBuilder(normalSiteName ? node.toString() : notMpLabel ? "" : node.getNode()
                 .getProperty(mpName, node.toString()).toString());
         if (!notMsLabel) {
             String msNames = NeoUtils.getMsNames(node.getNode(), msName);
             if (!msNames.isEmpty()) {
                 pointName.append(", ").append(msNames);
             }
         }
         return notMpLabel && pointName.length() > 1 ? pointName.substring(2) : pointName.toString();
     }
 
     /**
      * gets sector color
      * 
      * @param child - sector node
      * @param defColor - default value
      * @return color
      */
     private Color getNodeColor(Node node, Color defColor) {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             if (aggNode == null) {
                 return defColor;
             }
             Node chartNode = NeoUtils.getChartNode(node, aggNode);
             if (chartNode == null) {
                 return defColor;
             }
             return new Color((Integer)chartNode.getProperty(INeoConstants.AGGREGATION_COLOR, defColor.getRGB()));
         } finally {
             tx.finish();
         }
     }
 
     /**
      * This one is very simple, just draw a rectangle at the point location.
      * 
      * @param g
      * @param p
      * @param node
      */
     private void renderPoint(Graphics2D g, java.awt.Point p, Color borderColor, Color fillColor, int drawSize, int drawWidth,
             boolean drawFull, boolean drawLite) {
         Color oldColor = g.getColor();
         if (drawFull) {
             g.setColor(fillColor);
             g.fillRect(p.x - drawSize, p.y - drawSize, drawWidth, drawWidth);
             g.setColor(borderColor);
             g.drawRect(p.x - drawSize, p.y - drawSize, drawWidth, drawWidth);
 
         } else if (drawLite) {
             g.setColor(fillColor);
             g.fillOval(p.x - drawSize, p.y - drawSize, drawWidth, drawWidth);
         } else {
             g.setColor(fillColor);
             g.fillOval(p.x - 2, p.y - 2, 5, 5);
         }
         g.setColor(oldColor);
     }
 
     /**
      * This one is very simple, just draw a rectangle at the point location.
      * 
      * @param g
      * @param p
      * @param node
      */
     private void renderEvents(Graphics2D g, GeoNode node, java.awt.Point p, double theta) {
         // null - use current transaction
         DriveEvents event = DriveEvents.getWorstEvent(node.getNode(), null);
         if (event == null || eventIconSize < eventIconSizes[0]) {
             return;
         }
         Image eventImage = event.getEventIcon().getImage(eventIconSize);
         if (eventImage != null) {
             if (base_transform == null)
                 base_transform = g.getTransform();
             g.setTransform(base_transform);
             g.translate(p.x, p.y);
             if (eventIconOffset > 0) {
                 g.rotate(-theta);
             }
 
             ImageObserver imOb = null;
             final int width = eventImage.getWidth(imOb);
             final int height = eventImage.getHeight(imOb);
             g.drawImage(eventImage, -eventIconOffset - width / 2, -height / 2, width, height, imOb);
             return;
         }
     }
 
     /**
      * This one is very simple, just draw a rectangle at the point location.
      * 
      * @param g
      * @param p
      */
     private void renderSelectedPoint(Graphics2D g, java.awt.Point p, int drawSize, boolean drawFull, boolean drawLite) {
         Color oldColor = g.getColor();
         if (drawFull) {
             renderSelectionGlow(g, p, drawSize * 3);
         } else if (drawLite) {
             renderSelectionGlow(g, p, drawSize * 2);
         } else {
             renderSelectionGlow(g, p, drawSize);
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
         drawSize *= 3;
         Color highColor = new Color(COLOR_HIGHLIGHTED.getRed(), COLOR_HIGHLIGHTED.getGreen(), COLOR_HIGHLIGHTED.getBlue(), 8);
         g.setColor(highColor);
         for (; drawSize > 2; drawSize *= 0.8) {
             g.fillOval(p.x - drawSize, p.y - drawSize, 2 * drawSize, 2 * drawSize);
         }
     }
 
 }
