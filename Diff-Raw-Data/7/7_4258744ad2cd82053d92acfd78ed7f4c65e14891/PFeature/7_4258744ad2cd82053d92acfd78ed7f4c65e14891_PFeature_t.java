 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * PFeature.java
  *
  * Created on 12. April 2005, 10:52
  */
 package de.cismet.cismap.commons.gui.piccolo;
 
 import com.vividsolutions.jts.geom.*;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 
 import edu.umd.cs.piccolo.PCamera;
 import edu.umd.cs.piccolo.PNode;
 import edu.umd.cs.piccolo.event.PInputEvent;
 import edu.umd.cs.piccolo.nodes.PImage;
 import edu.umd.cs.piccolo.nodes.PPath;
 import edu.umd.cs.piccolo.nodes.PText;
 import edu.umd.cs.piccolo.util.PBounds;
 import edu.umd.cs.piccolo.util.PDimension;
 
 import pswing.PSwing;
 
 import java.awt.*;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 
 import de.cismet.cismap.commons.Crs;
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.Refreshable;
 import de.cismet.cismap.commons.WorldToScreenTransform;
 import de.cismet.cismap.commons.features.*;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.DrawSelectionFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.tools.CurrentStackTrace;
 
 import de.cismet.tools.collections.MultiMap;
 
 /**
  * DOCUMENT ME!
  *
  * @author   hell
  * @version  $Revision$, $Date$
  */
 public class PFeature extends PPath implements Highlightable, Selectable, Refreshable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Color TRANSPARENT = new Color(255, 255, 255, 0);
     private static final Stroke FIXED_WIDTH_STROKE = new FixedWidthStroke();
 
     //~ Instance fields --------------------------------------------------------
 
     ArrayList splitHandlesBetween = new ArrayList();
     PHandle splitPolygonFromHandle = null;
     PHandle splitPolygonToHandle = null;
     PHandle ellipseHandle = null;
     PFeature selectedOriginal = null;
     PPath splitPolygonLine;
     List<Point2D> splitPoints = new ArrayList<Point2D>();
     Image origin = null;
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private Feature feature;
     private WorldToScreenTransform wtst;
     private double x_offset = 0.0d;
     private double y_offset = 0.0d;
     private PNode stickyChild = null;
     private PNode secondStickyChild = null;
     private PNode infoNode = null;
     private Point2D mid = null;
     private PHandle pivotHandle = null;
     private boolean selected = false;
     private Paint nonSelectedPaint = null;
     private boolean highlighted = false;
     private Paint nonHighlightingPaint = null;
     private Coordinate[][][] entityRingCoordArr = null;
     private float[][][] entityRingXArr = null;
     private float[][][] entityRingYArr = null;
     // private final ArrayList<CoordEntity> coordEntityList = new ArrayList<CoordEntity>();
     private MappingComponent viewer;
     private Stroke stroke = null;
     private Paint strokePaint = null;
 //    private ColorTintFilter tinter;
     private ImageIcon pushpinIco = new javax.swing.ImageIcon(getClass().getResource(
                 "/de/cismet/cismap/commons/gui/res/pushpin.png"));         // NOI18N
     private ImageIcon pushpinSelectedIco = new javax.swing.ImageIcon(getClass().getResource(
                 "/de/cismet/cismap/commons/gui/res/pushpinSelected.png")); // NOI18N
     private boolean ignoreStickyFeature = false;
     private InfoPanel infoPanel;
     private JComponent infoComponent;
     private PSwing pswingComp;
     private PText primaryAnnotation = null;
     private FeatureAnnotationSymbol pi = null;
     private FeatureAnnotationSymbol piSelected;
     private boolean snappable = true;
     private int selectedEntity = -1;
     private boolean selectedPiEdited = false;
     private boolean wasSelected = false;
     // r/w access only in synchronized(this) block
     private transient PImage rdfImage;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of PFeature.
      *
      * @param  feature  the underlying Feature
      * @param  viewer   MappingComponent
      */
     public PFeature(final Feature feature, final MappingComponent viewer) {
         this(feature, viewer.getWtst(), 0, 0, viewer);
     }
 
     /**
      * Creates a new PFeature object.
      *
      * @param  feature   DOCUMENT ME!
      * @param  wtst      DOCUMENT ME!
      * @param  x_offset  DOCUMENT ME!
      * @param  y_offset  DOCUMENT ME!
      * @param  viewer    DOCUMENT ME!
      */
     public PFeature(final Feature feature,
             final WorldToScreenTransform wtst,
             final double x_offset,
             final double y_offset,
             final MappingComponent viewer) {
         this(feature, wtst, x_offset, y_offset, viewer, false);
     }
 
     /**
      * Creates a new PFeature object.
      *
      * @param  canvasPoints  DOCUMENT ME!
      * @param  wtst          DOCUMENT ME!
      * @param  x_offset      DOCUMENT ME!
      * @param  y_offset      DOCUMENT ME!
      * @param  viewer        DOCUMENT ME!
      */
     @Deprecated
     public PFeature(final Point2D[] canvasPoints,
             final WorldToScreenTransform wtst,
             final double x_offset,
             final double y_offset,
             final MappingComponent viewer) {
         this(new PureNewFeature(canvasPoints, wtst), wtst, 0, 0, viewer);
     }
 
     /**
      * Creates a new PFeature object.
      *
      * @param  coordArr  DOCUMENT ME!
      * @param  wtst      DOCUMENT ME!
      * @param  x_offset  DOCUMENT ME!
      * @param  y_offset  DOCUMENT ME!
      * @param  viewer    DOCUMENT ME!
      */
     @Deprecated
     public PFeature(final Coordinate[] coordArr,
             final WorldToScreenTransform wtst,
             final double x_offset,
             final double y_offset,
             final MappingComponent viewer) {
         this(new PureNewFeature(coordArr, wtst), wtst, 0, 0, viewer);
     }
 
     /**
      * Creates a new PFeature object.
      *
      * @param  feature              DOCUMENT ME!
      * @param  wtst                 DOCUMENT ME!
      * @param  x_offset             DOCUMENT ME!
      * @param  y_offset             DOCUMENT ME!
      * @param  viewer               DOCUMENT ME!
      * @param  ignoreStickyfeature  DOCUMENT ME!
      */
     @Deprecated
     public PFeature(final Feature feature,
             final WorldToScreenTransform wtst,
             final double x_offset,
             final double y_offset,
             final MappingComponent viewer,
             final boolean ignoreStickyfeature) {
         try {
             setFeature(feature);
             this.ignoreStickyFeature = ignoreStickyfeature;
             this.wtst = wtst;
 //            this.x_offset=x_offset;
 //            this.y_offset=y_offset;
             this.x_offset = 0;
             this.y_offset = 0;
             this.viewer = viewer;
 
             visualize();
             addInfoNode();
             refreshDesign();
 
             stroke = getStroke();
             strokePaint = getStrokePaint();
 //            tinter = new ColorTintFilter(Color.BLUE, 0.5f);
         } catch (Throwable t) {
             log.error("Error in constructor of PFeature", t); // NOI18N
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public PNode getPrimaryAnnotationNode() {
         return primaryAnnotation;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   g  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     public PBounds boundsFromRectPolygonGeom(final Geometry g) {
         if (g instanceof Polygon) {
             final Polygon poly = (Polygon)g;
             if (poly.isRectangle()) {
                 final Coordinate[] coords = poly.getCoordinates();
                 final Coordinate first = coords[0];
                 final PBounds b = new PBounds();
                 // init
                 double x1 = first.x;
                 double x2 = first.x;
                 double y1 = first.y;
                 double y2 = first.y;
                 for (int i = 0; i < coords.length; ++i) {
                     final Coordinate c = coords[i];
                     if (c.x < x1) {
                         x1 = c.x;
                     }
                     if (c.x > x2) {
                         x2 = c.x;
                     }
                     if (c.y < y1) {
                         y1 = c.y;
                     }
                     if (c.y > y1) {
                         y2 = c.y;
                     }
                 }
                 return new PBounds(wtst.getScreenX(x1), wtst.getScreenY(y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
             }
         }
         throw new IllegalArgumentException("Geometry is not a rectangle polygon!"); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      */
     public void visualize() {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("visualize()", new CurrentStackTrace()); // NOI18N
             }
         }
 
         final Geometry geom = CrsTransformer.transformToGivenCrs(feature.getGeometry(), getViewerCrs().getCode());
         if (feature instanceof RasterDocumentFeature) {
             final RasterDocumentFeature rdf = (RasterDocumentFeature)feature;
             try {
                 final PBounds bounds = boundsFromRectPolygonGeom(geom);
                 final PImage pImage = new PImage(rdf.getRasterDocument());
 
                 synchronized (this) {
                     if (rdfImage != null) {
                         removeChild(rdfImage);
                     }
                     rdfImage = pImage;
                 }
 
                 // x,y,with,heigth
                 pImage.setBounds(bounds);
                 addChild(pImage);
             } catch (final IllegalArgumentException e) {
                 if (log.isInfoEnabled()) {
                     log.info("rasterdocumentfeature is no rectangle, we'll draw the geometry without raster image", e); // NOI18N
                 }
             }
             doGeometry(geom);
         } else {
             doGeometry(geom);
             if (feature instanceof StyledFeature) {
                 if ((pi == null)
                             || ((pi != null) && pi.equals(((StyledFeature)feature).getPointAnnotationSymbol()))) {
                     // log.debug("Sweetspot updated");
 // pi = new FeatureAnnotationSymbol(((StyledFeature) getFeature()).getPointAnnotationSymbol()
 // .getImage());
                     setFeatureAnnotationSymbols();
                 } else if ((pi != null) && (getFeature() != null) && (getFeature() instanceof StyledFeature)
                             && (((StyledFeature)getFeature()).getPointAnnotationSymbol() != null)) {
 //                        log.fatal("Sweetspot updated");                                                                  // NOI18N
                     if (log.isDebugEnabled()) {
                         log.debug("newSweetSpotx: "
                                     + ((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotX()); // NOI18N
                     }
                     pi.setSweetSpotX(((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotX());
                     pi.setSweetSpotY(((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotY());
                     piSelected.setSweetSpotX(((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotX());
                     piSelected.setSweetSpotY(((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotY());
                 }
             }
 
             if (geom instanceof Polygon) {
                 if (pi instanceof ShowAlsoOnPolygons) {
                     final Point p = ((Polygon)geom).getInteriorPoint();
                     addAnnotation(p.getX(), p.getY());
                 }
             } else if ((geom instanceof LineString) || (geom instanceof MultiLineString)) {
             } else if (geom instanceof MultiPolygon) {
 //                MultiPolygon mp = (MultiPolygon) geom;
             } else if ((geom instanceof Point) || (geom instanceof MultiPoint)) {
                 addAnnotation(entityRingCoordArr[0][0][0].x, entityRingCoordArr[0][0][0].y);
             }
             setSelected(isSelected());
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void setFeatureAnnotationSymbols() {
         final FeatureAnnotationSymbol piOrig = ((StyledFeature)getFeature()).getPointAnnotationSymbol();
         if ((piOrig == null) || (piOrig.getImage() == null)) {
             // no FeatureAnnotationSymbol is set, use PushPin Icons as  fallback case
             pi = new FeatureAnnotationSymbol(pushpinIco.getImage());
             piSelected = new FeatureAnnotationSymbol(pushpinSelectedIco.getImage());
             log.warn("No PointAnnotationSymbol found use PushPinIcons"); // NOI18N
             pi = new FeatureAnnotationSymbol(pushpinIco.getImage());
             pi.setSweetSpotX(0.46d);
             pi.setSweetSpotY(0.9d);
             piSelected.setSweetSpotX(0.46d);
             piSelected.setSweetSpotY(0.9d);
         } else {
             final double sweetSpotX = ((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotX();
             final double sweetSpotY = ((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotY();
             if ((piOrig != null) && (piOrig.getSelectedFeatureAnnotationSymbol() == null)) {
                 /*
                  * in this case we visualize the selection with a blue box around the icon of the
                  * FeatureAnnotationSymbol
                  */
                 if (!selectedPiEdited) {
                     selectedPiEdited = true;
                     final Image iconImage = piOrig.getImage();
                     final BufferedImage img = new BufferedImage(iconImage.getWidth(null),
                             iconImage.getHeight(null),
                             BufferedImage.TYPE_INT_ARGB);
                     final Graphics g = img.getGraphics();
                     g.drawImage(iconImage, 0, 0, null);
                     piSelected = new FeatureAnnotationSymbol(highlightImageAsSelected(
                                 img,
                                 new Color(0.3f, 0.3f, 1.0f, 0.4f),
                                 new Color(0.2f, 0.2f, 1.0f, 0.8f),
                                 10));
                     final double sweetX = ((10 + (piOrig.getImage().getWidth(null) * sweetSpotX))
                                     / piSelected.getImage().getWidth(null));
                     final double sweetY = ((10 + (piOrig.getImage().getHeight(null) * sweetSpotY))
                                     / piSelected.getImage().getHeight(null));
                     piSelected.setSweetSpotX(sweetX);
                     piSelected.setSweetSpotY(sweetY);
                 }
             } else {
                 piSelected = piOrig.getSelectedFeatureAnnotationSymbol();
             }
             if (pi == null) {
                 /*
                  * draw an invisble frame around the icon, this places the info node at the same position as for the
                  * selected FeatureAnnotationSymbol
                  */
                 pi = new FeatureAnnotationSymbol();
                 final Image iconImage = piOrig.getImage();
                 final BufferedImage img = new BufferedImage(iconImage.getWidth(null),
                         iconImage.getHeight(null),
                         BufferedImage.TYPE_INT_ARGB);
                 final Graphics g = img.getGraphics();
                 g.drawImage(iconImage, 0, 0, null);
                 pi.setImage(highlightImageAsSelected(img, TRANSPARENT, TRANSPARENT, 10));
                 final double sweetX = ((10 + (piOrig.getImage().getWidth(null) * sweetSpotX))
                                 / piSelected.getImage().getWidth(null));
                 final double sweetY = ((10 + (piOrig.getImage().getHeight(null) * sweetSpotY))
                                 / piSelected.getImage().getHeight(null));
                 pi.setSweetSpotX(sweetX);
                 pi.setSweetSpotY(sweetY);
             }
             pi.setSelectedFeatureAnnotationSymbol(piSelected);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  real_x  DOCUMENT ME!
      * @param  real_y  DOCUMENT ME!
      */
     private void addAnnotation(final double real_x, final double real_y) {
         if (!ignoreStickyFeature) {
             viewer.addStickyNode(pi);
             viewer.addStickyNode(piSelected);
         }
 
         // Hier soll getestet werden ob bei einem Punkt der pushpin schon hinzugef\u00FCgt wurde. Wegen
         // reconsider Feature
         if (stickyChild == null) {
             stickyChild = pi;
         } else {
             if (stickyChild instanceof StickyPText) {
                 secondStickyChild = pi;
             }
         }
         addChild(piSelected);
         piSelected.setOffset(wtst.getScreenX(real_x), wtst.getScreenY(real_y));
         addChild(pi);
         pi.setOffset(wtst.getScreenX(real_x), wtst.getScreenY(real_y));
     }
 
     /**
      * Dupliziert eine Koordinate.
      *
      * @param  entityPosition  DOCUMENT ME!
      * @param  ringPosition    DOCUMENT ME!
      * @param  coordPosition   Position der zu duplizierenden Koordinate
      */
     public void duplicateCoordinate(final int entityPosition, final int ringPosition, final int coordPosition) {
         final Coordinate[] origCoordArr = entityRingCoordArr[entityPosition][ringPosition];
         final float[] origXArr = entityRingXArr[entityPosition][ringPosition];
         final float[] origYArr = entityRingYArr[entityPosition][ringPosition];
         final Geometry geometry = getFeature().getGeometry();
 
         if (((geometry instanceof Polygon) && (origCoordArr != null)
                         && ((origCoordArr.length - 1) > coordPosition))
                     || ((geometry instanceof LineString) && (origCoordArr != null)
                         && (origCoordArr.length > coordPosition)
                         && (origCoordArr.length > 2))) {
             final Coordinate[] newCoordArr = new Coordinate[origCoordArr.length + 1];
             final float[] newXArr = new float[origXArr.length + 1];
             final float[] newYArr = new float[origYArr.length + 1];
 
             // vorher
             for (int i = 0; i <= coordPosition; ++i) {
                 newCoordArr[i] = origCoordArr[i];
                 newXArr[i] = origXArr[i];
                 newYArr[i] = origYArr[i];
             }
 
             // zu entferndes Element duplizieren, hier muss geklont werden
             newCoordArr[coordPosition + 1] = (Coordinate)(origCoordArr[coordPosition].clone());
             newXArr[coordPosition + 1] = origXArr[coordPosition];
             newYArr[coordPosition + 1] = origYArr[coordPosition];
 
             // nachher
             for (int i = coordPosition + 1; i < origCoordArr.length; ++i) {
                 newCoordArr[i + 1] = origCoordArr[i];
                 newXArr[i + 1] = origXArr[i];
                 newYArr[i + 1] = origYArr[i];
             }
 
             // Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist
             if ((coordPosition == 0) && (geometry instanceof Polygon)) {
                 newCoordArr[newCoordArr.length - 1] = newCoordArr[0];
                 newXArr[newXArr.length - 1] = newXArr[0];
                 newYArr[newXArr.length - 1] = newYArr[0];
             }
 
             setNewCoordinates(entityPosition, ringPosition, newXArr, newYArr, newCoordArr);
         }
     }
 
     /**
      * Liefert eine exakte Kopie dieses PFeatures. Es besitzt denselben Inhalt, jedoch einen anderen Hashwert als das
      * Original.
      *
      * @return  Kopie dieses PFeatures
      */
     @Override
     public Object clone() {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("clone()", new CurrentStackTrace()); // NOI18N
             }
         }
         final PFeature p = new PFeature(feature, wtst, this.x_offset, y_offset, viewer);
         p.splitPolygonFromHandle = splitPolygonFromHandle;
         p.splitPolygonToHandle = splitPolygonToHandle;
         return p;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   coord            coordEntity DOCUMENT ME!
      * @param   geometryFactory  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static Point createPoint(final Coordinate coord, final GeometryFactory geometryFactory) {
         return geometryFactory.createPoint(coord);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   coordArray       coordEntity DOCUMENT ME!
      * @param   geometryFactory  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static LineString createLineString(final Coordinate[] coordArray, final GeometryFactory geometryFactory) {
         return geometryFactory.createLineString(coordArray);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   ringCoordArray   coordEntity DOCUMENT ME!
      * @param   geometryFactory  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static Polygon createPolygon(final Coordinate[][] ringCoordArray, final GeometryFactory geometryFactory) {
         // ring des polygons erstellen
         final LinearRing shell = geometryFactory.createLinearRing(ringCoordArray[0]);
 
         // ringe der löscher erstellen
         final Collection<LinearRing> holes = new ArrayList<LinearRing>();
         for (int ringIndex = 1; ringIndex < ringCoordArray.length; ringIndex++) {
             final LinearRing holeShell = geometryFactory.createLinearRing(ringCoordArray[ringIndex]);
             holes.add(holeShell);
         }
 
         // polygon erstellen und hinzufügen
         final Polygon polygon = geometryFactory.createPolygon(shell, holes.toArray(new LinearRing[0]));
         return polygon;
     }
 
     /**
      * Gleicht die Geometrie an das PFeature an. Erstellt die jeweilige Geometrie (Punkt, Linie, Polygon) und f\u00FCgt
      * sie dem Feature hinzu.
      */
     public void syncGeometry() {
         try {
             if (getFeature().isEditable()) {
                 // geometryfactory erzeugen
                 final GeometryFactory geometryFactory = new GeometryFactory(
                         new PrecisionModel(PrecisionModel.FLOATING),
                         CrsTransformer.extractSridFromCrs(getViewerCrs().getCode()));
 
                 // sonderfall multipolygon TODO eigentlich garkein sonderfall, multipoint und multilinestring müssen
                 // langfristig genauso behandelt werden
                 if ((getFeature().getGeometry() instanceof MultiPolygon)
                             || ((getFeature().getGeometry() instanceof Polygon) && (entityRingCoordArr.length > 1))) {
                     final Collection<Polygon> polygons = new ArrayList<Polygon>(entityRingCoordArr.length);
                     for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
                         final Polygon polygon = createPolygon(entityRingCoordArr[entityIndex], geometryFactory);
                         polygons.add(polygon);
                     }
 
                     // multipolygon aus den polygonen erzeugen
                     final MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygons.toArray(
                                 new Polygon[0]));
 
                     assignSynchronizedGeometry(multiPolygon);
                 } else {
                     final boolean isSingle = entityRingCoordArr.length == 1;
                     if (isSingle) {
                         if (entityRingCoordArr[0][0] == null) {
                             log.warn("coordArr==null"); // NOI18N
                         } else {
                             final boolean isPoint = entityRingCoordArr[0][0].length == 1;
                             final boolean isPolygon = (entityRingCoordArr[0][0].length > 3)
                                         && entityRingCoordArr[0][0][0].equals(
                                             entityRingCoordArr[0][0][entityRingCoordArr[0][0].length - 1]);
                             final boolean isLineString = !isPoint && !isPolygon;
 
                             if (isPoint) {
                                 assignSynchronizedGeometry(createPoint(entityRingCoordArr[0][0][0], geometryFactory));
                             } else if (isLineString) {
                                 assignSynchronizedGeometry(createLineString(entityRingCoordArr[0][0], geometryFactory));
                             } else if (isPolygon) {
                                 assignSynchronizedGeometry(createPolygon(entityRingCoordArr[0], geometryFactory));
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             log.error("Error while synchronising PFeature with feature.", e);
         }
     }
 
     /**
      * Assigns the PFeature geometry to the feature if they differ. The feature will keep its crs.
      *
      * @param  newGeom  DOCUMENT ME!
      */
     private void assignSynchronizedGeometry(final Geometry newGeom) {
         final Geometry oldGeom = feature.getGeometry();
         final String oldCrs = CrsTransformer.createCrsFromSrid(oldGeom.getSRID());
         final String newCrs = CrsTransformer.createCrsFromSrid(newGeom.getSRID());
 
 //        if (!newGeom.isValid()) {
 //            doGeometry(oldGeom);
 //            return;
 //        }
 //
         if ((newGeom.getSRID() == oldGeom.getSRID())
                     || (CrsTransformer.isDefaultCrs(oldCrs) && CrsTransformer.isDefaultCrs(newCrs))) {
             if (log.isDebugEnabled()) {
                 log.debug("feature and pfeature geometry differ, but have the same crs and will be synchronized."); // NOI18N
             }
 
             if (CrsTransformer.isDefaultCrs(newCrs)) {
                 newGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
             }
             feature.setGeometry(newGeom);
         } else {
             try {
                 final CrsTransformer transformer = new CrsTransformer(newCrs);
                 final Geometry oldGeomWithNewSrid = transformer.transformGeometry(oldGeom, oldCrs);
 
                 if (!oldGeomWithNewSrid.equalsExact(newGeom)) {
                     final CrsTransformer reverseTransformer = new CrsTransformer(oldCrs);
                     final Geometry newGeomWithOldSrid = reverseTransformer.fastTransformGeometry(newGeom, newCrs);
 
                     if (log.isDebugEnabled()) {
                         log.debug("feature and pfeature geometry differ and will be synchronized."); // NOI18N
                     }
 
                     if (CrsTransformer.isDefaultCrs(oldCrs)) {
                         newGeomWithOldSrid.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                     }
                     feature.setGeometry(newGeomWithOldSrid);
                 } else {
                     if (log.isDebugEnabled()) {
                         log.debug("feature and pfeature geometry do not differ."); // NOI18N
                     }
                 }
             } catch (final Exception e) {
                 log.error("Cannot synchronize feature.", e);                       // NOI18N
             }
         }
     }
 
     /**
      * Erzeugt Koordinaten- und Punktarrays aus einem gegebenen Geometry-Objekt.
      *
      * @param  geom  vorhandenes Geometry-Objekt
      */
     private void doGeometry(final Geometry geom) {
         if (geom instanceof Point) {
             final Point point = (Point)geom;
             entityRingCoordArr = new Coordinate[][][] {
                     {
                         { point.getCoordinate() }
                     }
                 };
         } else if (geom instanceof LineString) {
             final LineString lineString = (LineString)geom;
             entityRingCoordArr = new Coordinate[][][] {
                     { lineString.getCoordinates() }
                 };
         } else if (geom instanceof Polygon) {
             final Polygon polygon = (Polygon)geom;
             final int numOfHoles = polygon.getNumInteriorRing();
             entityRingCoordArr = new Coordinate[1][1 + numOfHoles][];
             entityRingCoordArr[0][0] = polygon.getExteriorRing().getCoordinates();
             for (int ringIndex = 1; ringIndex < entityRingCoordArr[0].length; ++ringIndex) {
                 entityRingCoordArr[0][ringIndex] = polygon.getInteriorRingN(ringIndex - 1).getCoordinates();
             }
         } else if (geom instanceof LinearRing) {
             // doPolygon((Polygon)geom);
         } else if (geom instanceof MultiPoint) {
             entityRingCoordArr = new Coordinate[][][] {
                     { ((MultiPoint)geom).getCoordinates() }
                 };
         } else if (geom instanceof MultiLineString) {
             final MultiLineString multiLineString = (MultiLineString)geom;
             final int numOfGeoms = multiLineString.getNumGeometries();
             entityRingCoordArr = new Coordinate[numOfGeoms][][];
             for (int entityIndex = 0; entityIndex < numOfGeoms; ++entityIndex) {
                 final Coordinate[] coordSubArr = ((LineString)multiLineString.getGeometryN(entityIndex))
                             .getCoordinates();
                 entityRingCoordArr[entityIndex] = new Coordinate[][] { coordSubArr };
             }
         } else if (geom instanceof MultiPolygon) {
             final MultiPolygon multiPolygon = (MultiPolygon)geom;
             final int numOfEntities = multiPolygon.getNumGeometries();
             entityRingCoordArr = new Coordinate[numOfEntities][][];
             for (int entityIndex = 0; entityIndex < numOfEntities; ++entityIndex) {
                 final Polygon polygon = (Polygon)multiPolygon.getGeometryN(entityIndex);
                 final int numOfHoles = polygon.getNumInteriorRing();
                 entityRingCoordArr[entityIndex] = new Coordinate[1 + numOfHoles][];
                 entityRingCoordArr[entityIndex][0] = polygon.getExteriorRing().getCoordinates();
                 for (int ringIndex = 1; ringIndex < entityRingCoordArr[entityIndex].length; ++ringIndex) {
                     entityRingCoordArr[entityIndex][ringIndex] = polygon.getInteriorRingN(ringIndex - 1)
                                 .getCoordinates();
                 }
             }
         }
 
         if (geom != null) {
             updateXpAndYp();
             updatePath();
         }
 
         refreshDesign();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void updateXpAndYp() {
         entityRingXArr = new float[entityRingCoordArr.length][][];
         entityRingYArr = new float[entityRingCoordArr.length][][];
         for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
             entityRingXArr[entityIndex] = new float[entityRingCoordArr[entityIndex].length][];
             entityRingYArr[entityIndex] = new float[entityRingCoordArr[entityIndex].length][];
             for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                 final Coordinate[] transformedCoordArr = transformCoordinateArr(
                         entityRingCoordArr[entityIndex][ringIndex]);
                 final int length = transformedCoordArr.length;
                 entityRingXArr[entityIndex][ringIndex] = new float[length];
                 entityRingYArr[entityIndex][ringIndex] = new float[length];
                 for (int coordIndex = 0; coordIndex < length; coordIndex++) {
                     entityRingXArr[entityIndex][ringIndex][coordIndex] = (float)transformedCoordArr[coordIndex].x;
                     entityRingYArr[entityIndex][ringIndex][coordIndex] = (float)transformedCoordArr[coordIndex].y;
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   entityPosition  DOCUMENT ME!
      * @param   ringPosition    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Coordinate[] getCoordArr(final int entityPosition, final int ringPosition) {
         return entityRingCoordArr[entityPosition][ringPosition];
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   entityPosition  DOCUMENT ME!
      * @param   ringPosition    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public float[] getXp(final int entityPosition, final int ringPosition) {
         return entityRingXArr[entityPosition][ringPosition];
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   entityPosition  DOCUMENT ME!
      * @param   ringPosition    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public float[] getYp(final int entityPosition, final int ringPosition) {
         return entityRingYArr[entityPosition][ringPosition];
     }
 
     /**
      * F\u00FCgt dem PFeature ein weiteres Coordinate-Array hinzu. Dadurch entstehen Multipolygone und Polygone mit
      * L\u00F6chern, je nachdem, ob der neue LinearRing ausserhalb oder innerhalb des PFeatures liegt.
      *
      * @param  coordinateArr  die Koordinaten des hinzuzuf\u00FCgenden Rings als Coordinate-Array
      */
     private void addLinearRing(final Coordinate[] coordinateArr) {
         final Coordinate[] points = transformCoordinateArr(coordinateArr);
         final GeneralPath gp = new GeneralPath();
         gp.reset();
         if (points.length > 0) {
             gp.moveTo((float)points[0].x, (float)points[0].y);
             for (int i = 1; i < points.length; i++) {
                 gp.lineTo((float)points[i].x, (float)points[i].y);
             }
         }
         append(gp, false);
     }
 
     /**
      * Erzeugt PCanvas-Koordinaten-Punktarrays aus Realworldkoordinaten.
      *
      * @param   coordinateArr  Array mit Realworld-Koordinaten
      *
      * @return  DOCUMENT ME!
      */
     private Coordinate[] transformCoordinateArr(final Coordinate[] coordinateArr) {
         final Coordinate[] points = new Coordinate[coordinateArr.length];
         for (int i = 0; i < coordinateArr.length; ++i) {
             points[i] = new Coordinate();
             if (wtst == null) {
                 points[i].x = (float)(coordinateArr[i].x + x_offset);
                 points[i].y = (float)(coordinateArr[i].y + y_offset);
             } else {
                 points[i].x = (float)(wtst.getDestX(coordinateArr[i].x) + x_offset);
                 points[i].y = (float)(wtst.getDestY(coordinateArr[i].y) + y_offset);
             }
         }
 
         return points;
     }
 
     /**
      * Setzt die Zeichenobjekte des Features (z.B. unselektiert=rot) und st\u00F6\u00DFt ein Neuzeichnen an.
      */
     public void refreshDesign() {
         if (primaryAnnotation != null) {
             removeChild(primaryAnnotation);
             viewer.removeStickyNode(primaryAnnotation);
         }
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("refreshDesign()", new CurrentStackTrace()); // NOI18N
             }
         }
         if (getFeature().isHidden() && !getFeature().isEditable()) {
             setStroke(null);
             setPaint(null);
         } else {
             // hier muss die Anpassung bei den WFS Features hin.
             Stroke overridingstroke = null;
             if (getFeature() instanceof XStyledFeature) {
                 final XStyledFeature xsf = (XStyledFeature)getFeature();
                 overridingstroke = xsf.getLineStyle();
             }
 
             if (getFeature() instanceof RasterDocumentFeature) {
                 overridingstroke = FIXED_WIDTH_STROKE;
             }
 
             if ((getFeature() instanceof StyledFeature) && (overridingstroke == null)) {
                 final StyledFeature sf = (StyledFeature)getFeature();
                 if (sf.getLineWidth() <= 1) {
                     setStroke(FIXED_WIDTH_STROKE);
                 } else {
                     final CustomFixedWidthStroke old = new CustomFixedWidthStroke(sf.getLineWidth());
                     setStroke(old);
                 }
                 // Falls absichtlich keine Linie gesetzt worden ist (z.B. im StyleDialog)
                 if (sf.getLinePaint() == null) {
                     setStroke(null);
                 }
             }
 
             if (overridingstroke != null) {
                 setStroke(overridingstroke);
             }
             if ((getFeature().getGeometry() instanceof LineString)
                         || (getFeature().getGeometry() instanceof MultiLineString)) {
                 if ((feature instanceof StyledFeature)) {
                     final java.awt.Paint linePaint = ((StyledFeature)feature).getLinePaint();
                     if (linePaint != null) {
                         setStrokePaint(linePaint);
                     }
                 }
             } else {
                 if ((feature instanceof StyledFeature)) {
                     final java.awt.Paint paint = ((StyledFeature)feature).getFillingPaint();
                     final java.awt.Paint linePaint = ((StyledFeature)feature).getLinePaint();
                     if (paint != null) {
                         setPaint(paint);
                         nonHighlightingPaint = paint;
                     }
                     if (linePaint != null) {
                         setStrokePaint(linePaint);
                     }
                 }
             }
             stroke = getStroke();
             strokePaint = getStrokePaint();
             setSelected(this.isSelected());
 
             // TODO:Wenn feature=labeledFeature jetzt noch Anpassungen machen
             if (((feature instanceof AnnotatedFeature) && ((AnnotatedFeature)feature).isPrimaryAnnotationVisible()
                             && (((AnnotatedFeature)feature).getPrimaryAnnotation() != null))) {
                 final AnnotatedFeature af = (AnnotatedFeature)feature;
                 primaryAnnotation = new StickyPText(af.getPrimaryAnnotation());
                 primaryAnnotation.setJustification(af.getPrimaryAnnotationJustification());
                 if (af.isAutoscale()) {
                     stickyChild = primaryAnnotation;
                 }
                 viewer.getCamera()
                         .addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
 
                                 @Override
                                 public void propertyChange(final PropertyChangeEvent evt) {
                                     setVisibility(primaryAnnotation, af);
                                 }
                             });
                 // if (true || af.getMaxScaleDenominator() == null || af.getMinScaleDenominator() == null ||
                 // af.getMaxScaleDenominator() > denom && af.getMinScaleDenominator() < denom) {
 
                 if (af.getPrimaryAnnotationPaint() != null) {
                     primaryAnnotation.setTextPaint(af.getPrimaryAnnotationPaint());
                 } else {
                     primaryAnnotation.setTextPaint(Color.BLACK);
                 }
                 if (af.getPrimaryAnnotationScaling() > 0) {
                     primaryAnnotation.setScale(af.getPrimaryAnnotationScaling());
                 }
                 if (af.getPrimaryAnnotationFont() != null) {
                     primaryAnnotation.setFont(af.getPrimaryAnnotationFont());
                 }
                 final boolean vis = primaryAnnotation.getVisible();
 
                 final Point intPoint = CrsTransformer.transformToGivenCrs(feature.getGeometry(),
                             getViewerCrs().getCode())
                             .getInteriorPoint();
 
                 primaryAnnotation.setOffset(wtst.getScreenX(intPoint.getX()), wtst.getScreenY(intPoint.getY()));
 
                 addChild(primaryAnnotation);
 
                 if (!ignoreStickyFeature && af.isAutoscale()) {
                     viewer.addStickyNode(primaryAnnotation);
                     viewer.rescaleStickyNode(primaryAnnotation);
                 }
                 setVisibility(primaryAnnotation, af);
                 // }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  ptext  DOCUMENT ME!
      * @param  af     DOCUMENT ME!
      */
     private void setVisibility(final PText ptext, final AnnotatedFeature af) {
         final double denom = viewer.getScaleDenominator();
         if ((af.getMaxScaleDenominator() == null) || (af.getMinScaleDenominator() == null)
                     || ((af.getMaxScaleDenominator() > denom) && (af.getMinScaleDenominator() < denom))) {
             ptext.setVisible(true);
         } else {
             ptext.setVisible(false);
         }
     }
 
     /**
      * F\u00FCgt eine neue \u00FCbergebene Koordinate in das Koordinatenarray ein, statt nur einen Punkt zu duplizieren.
      *
      * @param  entityPosition  DOCUMENT ME!
      * @param  ringPosition    DOCUMENT ME!
      * @param  coordPosition   die Position des neuen Punktes im Array
      * @param  newValueX       original das Original-Array
      * @param  newValueY       der einzuf\u00FCgende Wert
      */
     public void insertCoordinate(final int entityPosition,
             final int ringPosition,
             final int coordPosition,
             final float newValueX,
             final float newValueY) {
         final Coordinate[] originalCoordArr = entityRingCoordArr[entityPosition][ringPosition];
         final float[] originalXArr = entityRingXArr[entityPosition][ringPosition];
         final float[] originalYArr = entityRingYArr[entityPosition][ringPosition];
 
         if ((((getFeature().getGeometry() instanceof Polygon) || (getFeature().getGeometry() instanceof MultiPolygon))
                         && (originalXArr != null)
                         && ((originalXArr.length - 1) >= coordPosition))
                     || ((getFeature().getGeometry() instanceof LineString) && (originalXArr != null)
                         && (originalXArr.length > coordPosition)
                         && (originalXArr.length > 2))) {
             final Coordinate[] newCoordArr = new Coordinate[originalCoordArr.length + 1];
             final float[] newXArr = new float[originalXArr.length + 1];
             final float[] newYArr = new float[originalYArr.length + 1];
 
             // vorher
             for (int i = 0; i < coordPosition; ++i) {
                 newCoordArr[i] = originalCoordArr[i];
                 newXArr[i] = originalXArr[i];
                 newYArr[i] = originalYArr[i];
             }
 
             newCoordArr[coordPosition] = new Coordinate(viewer.getWtst().getSourceX(newValueX),
                     viewer.getWtst().getSourceY(newValueY));
             newXArr[coordPosition] = newValueX;
             newYArr[coordPosition] = newValueY;
 
             // nachher
             for (int i = coordPosition; i < originalCoordArr.length; ++i) {
                 newCoordArr[i + 1] = originalCoordArr[i];
                 newXArr[i + 1] = originalXArr[i];
                 newYArr[i + 1] = originalYArr[i];
             }
 
             if ((getFeature().getGeometry() instanceof Polygon)
                         || (getFeature().getGeometry() instanceof MultiPolygon)) {
                 // Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist
                 if ((coordPosition == 0) || (coordPosition == (originalCoordArr.length - 1))) {
                     newCoordArr[newCoordArr.length - 1] = newCoordArr[0];
                     newXArr[newXArr.length - 1] = newXArr[0];
                     newYArr[newYArr.length - 1] = newYArr[0];
                 }
             }
 
             setNewCoordinates(entityPosition, ringPosition, newXArr, newYArr, newCoordArr);
         }
     }
 
     /**
      * Entfernt eine Koordinate aus der Geometrie, z.B. beim L\u00F6schen eines Handles.
      *
      * @param  entityPosition  DOCUMENT ME!
      * @param  ringPosition    DOCUMENT ME!
      * @param  coordPosition   Position des zu l\u00F6schenden Punkes im Koordinatenarray
      */
     public void removeCoordinate(final int entityPosition, final int ringPosition, final int coordPosition) {
         final Coordinate[] originalCoordArr = entityRingCoordArr[entityPosition][ringPosition];
         final float[] originalXArr = entityRingXArr[entityPosition][ringPosition];
         final float[] originalYArr = entityRingYArr[entityPosition][ringPosition];
 
         if ((((getFeature().getGeometry() instanceof Polygon) || (getFeature().getGeometry() instanceof MultiPolygon))
                         && (originalCoordArr != null)
                         && ((originalCoordArr.length - 1) > coordPosition))
                     || ((getFeature().getGeometry() instanceof LineString) && (originalCoordArr != null)
                         && (originalCoordArr.length > coordPosition)
                         && (originalCoordArr.length > 2))) {
             final Coordinate[] newCoordArr = new Coordinate[originalCoordArr.length - 1];
             final float[] newXArr = new float[originalXArr.length - 1];
             final float[] newYArr = new float[originalYArr.length - 1];
 
             // vorher
             for (int i = 0; i < coordPosition; ++i) {
                 newCoordArr[i] = originalCoordArr[i];
                 newXArr[i] = originalXArr[i];
                 newYArr[i] = originalYArr[i];
             }
             // zu entferndes Element \u00FCberspringen
 
             // nachher
             for (int i = coordPosition; i < newCoordArr.length; ++i) {
                 newCoordArr[i] = originalCoordArr[i + 1];
                 newXArr[i] = originalXArr[i + 1];
                 newYArr[i] = originalYArr[i + 1];
             }
 
             // Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist (nur beim Polygon)
             if (((coordPosition == 0) && (getFeature().getGeometry() instanceof Polygon))
                         || (getFeature().getGeometry() instanceof MultiPolygon)) {
                 newCoordArr[newCoordArr.length - 1] = newCoordArr[0];
                 newXArr[newXArr.length - 1] = newXArr[0];
                 newXArr[newYArr.length - 1] = newYArr[0];
             }
 
             setNewCoordinates(entityPosition, ringPosition, newXArr, newYArr, newCoordArr);
 
             // Jetzt sind allerdings alle Locator noch falsch und das handle existiert noch
             // handleLayer.removeChild(this);
             // Das w\u00E4re zwar optimal (Performance) korrigiert allerdings nicht die falschen
             // Locator
         }
     }
 
     /**
      * Erzeugt alle Handles f\u00FCr dieses PFeature auf dem \u00FCbergebenen HandleLayer.
      *
      * @param  handleLayer  PLayer der die Handles aufnimmt
      */
     public void addHandles(final PNode handleLayer) {
         if (getFeature() instanceof LinearReferencedPointFeature) {
             addLinearReferencedPointPHandle(handleLayer);
         } else if ((getFeature() instanceof PureNewFeature)
                     && (((PureNewFeature)getFeature()).getGeometryType() == PureNewFeature.geomTypes.ELLIPSE)) {
             addEllipseHandle(handleLayer);
         } else {
             for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
                 for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                     final Coordinate[] coordArr = entityRingCoordArr[entityIndex][ringIndex];
                     int length = coordArr.length;
 
                     final Geometry geometry = getFeature().getGeometry();
                     if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
                         length--; // xp.length-1 weil der erste und letzte Punkt identisch sind
                     }
                     for (int coordIndex = 0; coordIndex < length; ++coordIndex) {
                         addHandle(handleLayer, entityIndex, ringIndex, coordIndex);
                     }
                 }
             }
         }
     }
 
     /**
      * Erzeugt ein PHandle an den Koordinaten eines bestimmten Punktes des Koordinatenarrays und f\u00FCgt es dem
      * HandleLayer hinzu.
      *
      * @param  handleLayer     PLayer dem das Handle als Kind hinzugef\u00FCgt wird
      * @param  entityPosition  DOCUMENT ME!
      * @param  ringPosition    DOCUMENT ME!
      * @param  coordPosition   Position des Punktes im Koordinatenarray
      */
     public void addHandle(final PNode handleLayer,
             final int entityPosition,
             final int ringPosition,
             final int coordPosition) {
         final int positionInArray = coordPosition;
 
         final PHandle h = new TransformationPHandle(this, entityPosition, ringPosition, positionInArray);
 
 //        EventQueue.invokeLater(new Runnable() {
 //
 //            public void run() {
         handleLayer.addChild(h);
         h.addClientProperty("coordinate", entityRingCoordArr[entityPosition][ringPosition][coordPosition]); // NOI18N
         h.addClientProperty("coordinate_position_entity", new Integer(entityPosition));                     // NOI18N
         h.addClientProperty("coordinate_position_ring", new Integer(ringPosition));                         // NOI18N
         h.addClientProperty("coordinate_position_coord", new Integer(coordPosition));                       // NOI18N
 //            }
 //        });
     }
 
     /**
      * Pr\u00FCft alle Features, ob sie zu das gegebene PFeature \u00FCberschneiden und ein Handle besitzen das weniger
      * als 1cm vom angeklickten Handle entfernt ist. Falls beides zutrifft, wird eine MultiMap mit diesen Features
      * gef\u00FCllt und zur\u00FCckgegeben.
      *
      * @param   entityPosition  DOCUMENT ME!
      * @param   ringPosition    DOCUMENT ME!
      * @param   coordPosition   Postion des geklickten Handles im Koordinatenarray um Koordinaten herauszufinden
      *
      * @return  MultiMap mit Features, die die Bedingungen erf\u00FCllen
      */
     public de.cismet.tools.collections.MultiMap checkforGlueCoords(
             final int entityPosition,
             final int ringPosition,
             final int coordPosition) {
         final GeometryFactory gf = new GeometryFactory();
         final MultiMap glueCoords = new MultiMap();
 
         // Alle vorhandenen Features holen und pr\u00FCfen
         final List<Feature> allFeatures = getViewer().getFeatureCollection().getAllFeatures();
         for (final Feature f : allFeatures) {
             // \u00DCberschneiden sich die Features? if (!f.equals(PFeature.this.getFeature()) &&
             // f.getGeometry().intersects(PFeature.this.getFeature().getGeometry()) ){
             if (!f.equals(PFeature.this.getFeature())) {
                 final Geometry fgeo = CrsTransformer.transformToGivenCrs(f.getGeometry(), getViewerCrs().getCode());
                 final Geometry thisGeo = CrsTransformer.transformToGivenCrs(PFeature.this.getFeature().getGeometry(),
                         getViewerCrs().getCode());
                 if (fgeo.buffer(0.01).intersects(thisGeo.buffer(0.01))) {
                     final Coordinate coord = entityRingCoordArr[entityPosition][ringPosition][coordPosition];
                     final Point p = gf.createPoint(coord);
                     // Erzeuge Array mit allen Eckpunkten
                     final Coordinate[] ca = fgeo.getCoordinates();
 
                     // Prüfe für alle Punkte ob der Abstand < 1cm ist
                     for (int i = 0; i < ca.length; ++i) {
                         final Point p2 = gf.createPoint(ca[i]);
                         final double abstand = p.distance(p2);
                         if (abstand < 0.01) {
                             glueCoords.put(getViewer().getPFeatureHM().get(f), i);
                             if (viewer.isFeatureDebugging()) {
                                 if (log.isDebugEnabled()) {
                                     log.debug("checkforGlueCoords() Abstand kleiner als 1cm: " + abstand + " :: " + f); // NOI18N
                                 }
                             }
                         } else {
                             if (viewer.isFeatureDebugging()) {
                                 if (log.isDebugEnabled()) {
                                     log.debug("checkforGlueCoords() Abstand: " + abstand);                              // NOI18N
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return glueCoords;
     }
 
     /**
      * Erzeugt alle RotaionHandles f\u00FCr dieses PFeature auf dem \u00FCbergebenen HandleLayer.
      *
      * @param  handleLayer  PLayer der die RotationHandles aufnimmt
      */
     public void addRotationHandles(final PNode handleLayer) {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("addRotationHandles(): PFeature:" + this); // NOI18N
             }
         }
 
         // SchwerpunktHandle erzeugen
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("PivotHandle==" + pivotHandle); // NOI18N
             }
         }
         if (pivotHandle == null) {
             addPivotHandle(handleLayer);
         } else {
             boolean contains = false;
             for (final Object o : handleLayer.getChildrenReference()) {
                 if (o == pivotHandle) {
                     contains = true;
                     break;
                 }
             }
             if (!contains) {
                 handleLayer.addChild(pivotHandle);
             }
         }
 
         // Handles einfügen
         for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
             for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                 final Coordinate[] coordArr = entityRingCoordArr[entityIndex][ringIndex];
                 int length = coordArr.length;
 
                 final Geometry geometry = getFeature().getGeometry();
                 if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
                     length--; // xp.length-1 weil der erste und letzte Punkt identisch sind
                 }
                 for (int coordIndex = 0; coordIndex < length; ++coordIndex) {
                     addRotationHandle(handleLayer, entityIndex, ringIndex, coordIndex);
                 }
             }
         }
     }
 
     /**
      * F\u00FCgt dem PFeature spezielle Handles zum Rotieren des PFeatures an den Eckpunkten hinzu. Zus\u00E4tzlich ein
      * Handle am Rotationsmittelpunkt, um diesen manuell \u00E4nder nzu k\u00F6nnen.
      *
      * @param  handleLayer     HandleLayer der MappingComponent
      * @param  entityPosition  DOCUMENT ME!
      * @param  ringPosition    DOCUMENT ME!
      * @param  coordPosition   DOCUMENT ME!
      */
     private void addRotationHandle(final PNode handleLayer,
             final int entityPosition,
             final int ringPosition,
             final int coordPosition) {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("addRotationHandles():add from " + coordPosition + ". RotationHandle"); // NOI18N
             }
         }
 
         final PHandle rotHandle = new RotationPHandle(
                 this,
                 entityPosition,
                 ringPosition,
                 coordPosition,
                 mid,
                 pivotHandle);
 
         rotHandle.setPaint(new Color(1f, 1f, 0f, 0.7f));
 //        EventQueue.invokeLater(new Runnable() {
 //
 //            @Override
 //            public void run() {
         handleLayer.addChild(rotHandle);
         rotHandle.addClientProperty("coordinate", entityRingCoordArr[entityPosition][ringPosition][coordPosition]); // NOI18N
         rotHandle.addClientProperty("coordinate_position_entity", new Integer(entityPosition));                     // NOI18N
         rotHandle.addClientProperty("coordinate_position_ring", new Integer(ringPosition));                         // NOI18N
         rotHandle.addClientProperty("coordinate_position_coord", new Integer(coordPosition));                       // NOI18N
 //            }
 //        });
     }
 
     /**
      * Erzeugt den Rotations-Angelpunkt. Der Benutzer kann den Punkt verschieben, um die Drehung um einen anderen Punkt
      * als den Mittel-/Schwerpunkt auszuf\u00FChren.
      *
      * @param  handleLayer  PLayer der das PivotHandle aufnimmt
      */
     public void addPivotHandle(final PNode handleLayer) {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("addPivotHandle()"); // NOI18N
             }
         }
         PBounds allBounds = null;
         if (getViewer().getFeatureCollection() instanceof DefaultFeatureCollection) {
             final Collection selectedFeatures = getViewer().getFeatureCollection().getSelectedFeatures();
             Rectangle2D tmpBounds = getBounds().getBounds2D();
             for (final Object o : selectedFeatures) {
                 final PFeature pf = (PFeature)getViewer().getPFeatureHM().get(o);
                 if (!(selectedFeatures.contains(pf))) {
                     tmpBounds = pf.getBounds().getBounds2D().createUnion(tmpBounds);
                 }
             }
             allBounds = new PBounds(tmpBounds);
         }
         final Collection selArr = getViewer().getFeatureCollection().getSelectedFeatures();
         for (final Object o : selArr) {
             final PFeature pf = (PFeature)(getViewer().getPFeatureHM().get(o));
             pf.setPivotPoint(allBounds.getCenter2D());
             mid = allBounds.getCenter2D();
         }
 
         pivotHandle = new PivotPHandle(this, mid);
         pivotHandle.setPaint(new Color(0f, 0f, 0f, 0.6f));
 //        EventQueue.invokeLater(new Runnable() {
 //
 //            @Override
 //            public void run() {
         handleLayer.addChild(pivotHandle);
 //            }
 //        });
         for (final Object o : selArr) {
             final PFeature pf = (PFeature)(getViewer().getPFeatureHM().get(o));
             pf.pivotHandle = this.pivotHandle;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  handleLayer  DOCUMENT ME!
      */
     public void addLinearReferencedPointPHandle(final PNode handleLayer) {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("addLinearReferencingHandle()"); // NOI18N
             }
         }
 
         final PHandle h = new LinearReferencedPointPHandle(this);
 
 //        EventQueue.invokeLater(new Runnable() {
         //
 //            public void run() {
         handleLayer.addChild(h);
 //            }
 //        });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  handleLayer  DOCUMENT ME!
      */
     public void addEllipseHandle(final PNode handleLayer) {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("addEllipseHandle()"); // NOI18N
             }
         }
 
         ellipseHandle = new EllipsePHandle(this);
 
         ellipseHandle.setPaint(new Color(0f, 0f, 0f, 0.6f));
         handleLayer.addChild(ellipseHandle);
     }
 
     /**
      * Sets a new pivotpoint for the roation.
      *
      * @param  newPivot  new Point2D
      */
     public void setPivotPoint(final Point2D newPivot) {
         this.mid = newPivot;
     }
 
     /**
      * Berechnet anhand einer Rotationsmatrix die neuen Punkte des Features, diese werden dann mittels
      * moveCoordinateToNewPiccoloPosition() auch auf die zugeh\u00F6rige Geometrie \u00FCbertragen.
      *
      * @param  rad      Winkel der Rotation im Bogenma\u00DF
      * @param  tempMid  Mittelpunkt der Rotation
      */
     public void rotateAllPoints(double rad, Point2D tempMid) {
         final double[][] matrix = new double[2][2];
         double cos;
         double sin;
         if (rad > 0.0d) { // Clockwise
             cos = Math.cos(rad);
             sin = Math.sin(rad);
             matrix[0][0] = cos;
             matrix[0][1] = sin * (-1);
             matrix[1][0] = sin;
             matrix[1][1] = cos;
         } else {          // Counterclockwise
             rad *= -1;
             cos = Math.cos(rad);
             sin = Math.sin(rad);
             matrix[0][0] = cos;
             matrix[0][1] = sin;
             matrix[1][0] = sin * (-1);
             matrix[1][1] = cos;
         }
         if (tempMid == null) {
             tempMid = mid;
         }
         for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
             for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                 for (int coordIndex = entityRingCoordArr[entityIndex][ringIndex].length - 1; coordIndex >= 0;
                             coordIndex--) {
                     final double dx = entityRingXArr[entityIndex][ringIndex][coordIndex] - tempMid.getX();
                     final double dy = entityRingYArr[entityIndex][ringIndex][coordIndex] - tempMid.getY();
 
                     // Clockwise
                     final float resultX = new Double(tempMid.getX() + ((dx * matrix[0][0]) + (dy * matrix[0][1])))
                                 .floatValue();
                     final float resultY = new Double(tempMid.getY() + ((dx * matrix[1][0]) + (dy * matrix[1][1])))
                                 .floatValue();
 
                     moveCoordinateToNewPiccoloPosition(entityIndex, ringIndex, coordIndex, resultX, resultY);
                 }
             }
         }
     }
 
     /**
      * Bildet aus Mausposition, Mittelpunkt und Handleposition ein Dreieck und berechnet daraus, den bei der Bewegung
      * zur\u00FCckgelegten Winkel und dessen Richtung.
      *
      * @param   event  PInputEvent der Mausbewegung
      * @param   x      X-Koordinate des Handles
      * @param   y      Y-Koordinate des Handles
      *
      * @return  \u00FCberstrichener Winkel der Bewegung im Bogenma\u00DF
      */
     public double calculateDrag(final PInputEvent event, final float x, final float y) {
         final Point2D mousePos = event.getPosition();
 
         // create vectors
         final double[] mv = { (mousePos.getX() - mid.getX()), (mousePos.getY() - mid.getY()) };
         final double[] hv = { (x - mid.getX()), (y - mid.getY()) };
 
         final double cosm = ((mv[0]) / Math.hypot(mv[0], mv[1]));
         final double cosh = ((hv[0]) / Math.hypot(hv[0], hv[1]));
         final double resH = Math.acos(cosh);
         final double resM = Math.acos(cosm);
         double res = 0;
 
         if (((mousePos.getY() - mid.getY()) > 0) && ((y - mid.getY()) > 0)) {
             res = resM - resH;
         } else if (((mousePos.getY() - mid.getY()) > 0) && ((y - mid.getY()) < 0)) {
             res = resM - (resH * -1);
         } else if ((y - mid.getY()) < 0) {
             res = resH - resM;
         } else if (((mousePos.getY() - mid.getY()) < 0) && ((y - mid.getY()) > 0)) {
             res = (resH * -1) - resM;
         }
         return res;
     }
 
     /**
      * Ver\u00E4ndert die PCanvas-Koordinaten eines Punkts des PFeatures.
      *
      * @param  entityPosition  DOCUMENT ME!
      * @param  ringPosition    DOCUMENT ME!
      * @param  coordPosition   Position des Punkts im Koordinatenarray
      * @param  newX            neue X-Koordinate
      * @param  newY            neue Y-Koordinate
      */
     public void moveCoordinateToNewPiccoloPosition(final int entityPosition,
             final int ringPosition,
             final int coordPosition,
             final float newX,
             final float newY) {
         final Coordinate[] origCoordArr = entityRingCoordArr[entityPosition][ringPosition];
         final float[] origXArr = entityRingXArr[entityPosition][ringPosition];
         final float[] origYArr = entityRingYArr[entityPosition][ringPosition];
 
         final Coordinate[] newCoordArr = new Coordinate[origCoordArr.length];
         System.arraycopy(origCoordArr, 0, newCoordArr, 0, newCoordArr.length);
         newCoordArr[coordPosition] = new Coordinate(wtst.getSourceX(newX - x_offset),
                 wtst.getSourceY(newY - y_offset));
 
         final Geometry geometry = getFeature().getGeometry();
         if ((coordPosition == 0) && ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon))) {
             newCoordArr[origXArr.length - 1] = newCoordArr[0];
         }
 
         origXArr[coordPosition] = newX;
         origYArr[coordPosition] = newY;
         origCoordArr[coordPosition] = newCoordArr[coordPosition];
 
         if ((coordPosition == 0) && ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon))) {
             origXArr[origXArr.length - 1] = origXArr[0];
             origYArr[origYArr.length - 1] = origYArr[0];
             origCoordArr[origXArr.length - 1] = origCoordArr[0];
         }
 
         updatePath();
     }
 
     /**
      * Removes the current splitline and creates a new one from the startingpoint.
      */
     private void resetSplitLine() {
         removeAllChildren();
         splitPolygonLine = new PPath();
         splitPoints = new ArrayList<Point2D>();
         splitPoints.add(getFirstSplitHandle());
         splitPolygonLine.setStroke(FIXED_WIDTH_STROKE);
         // splitPolygonLine.setPaint(new Color(1f,0f,0f,0.5f));
         addChild(splitPolygonLine);
     }
 
     /**
      * Fügt dem PFeature ein Handle hinzu mit dem man das PFeature in zwei zerlegen kann.
      *
      * @param  p  das SplitHandle
      */
     public void addSplitHandle(final PHandle p) {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("addSplitHandle()");                                                           // NOI18N
             }
         }
         if (splitPolygonFromHandle == p) {
             splitPolygonFromHandle = null;
             p.setSelected(false);
         } else if (splitPolygonToHandle == p) {
             splitPolygonToHandle = null;
             p.setSelected(false);
         } else if (splitPolygonFromHandle == null) {
             splitPolygonFromHandle = p;
             p.setSelected(true);
             resetSplitLine();
             if (viewer.isFeatureDebugging()) {
                 if (log.isDebugEnabled()) {
                     log.debug("after addSplitHandle: splitPolygonFromHandle=" + splitPolygonFromHandle); // NOI18N
                 }
             }
             if (viewer.isFeatureDebugging()) {
                 if (log.isDebugEnabled()) {
                     log.debug("in addSplitHandle this=" + this);                                         // NOI18N
                 }
             }
         } else if (splitPolygonToHandle == null) {
             splitPolygonToHandle = p;
             p.setSelected(true);
             splitPoints.add(new Point2D.Double(
                     splitPolygonToHandle.getLocator().locateX(),
                     splitPolygonToHandle.getLocator().locateY()));
         } else {
             p.setSelected(false);
         }
 //LineString()
         if ((splitPolygonFromHandle != null) && (splitPolygonToHandle != null)) {
             final Coordinate[] ca = new Coordinate[splitPoints.size() + 2];
 //            ca[0]=(Coordinate)splitPolygonFromHandle.getClientProperty("coordinate");
 //            ca[1]=(Coordinate)splitPolygonToHandle.getClientProperty("coordinate");
 //            GeometryFactory gf=new GeometryFactory();
 //            LineString ls=gf.createLineString(ca);
             // Geometry geom=feature.getGeometry();
 //            if ((geom.overlaps(ls))) {
 //                splitPolygonLine=PPath.createLine((float)splitPolygonFromHandle.getLocator().locateX(),(float)splitPolygonFromHandle.getLocator().locateY(),
 //                        (float)splitPolygonToHandle.getLocator().locateX(),(float)splitPolygonToHandle.getLocator().locateY());
 //                splitPolygonLine.setStroke(new FixedWidthStroke());
 //                this.addChild(splitPolygonLine);
 //            }
         }
     }
 
     /**
      * Returns the point of the handle from which the split starts.
      *
      * @return  Point2D
      */
     public Point2D getFirstSplitHandle() {
         if ((splitPolygonFromHandle != null)
                     && (splitPolygonFromHandle.getClientProperty("coordinate") instanceof Coordinate)) { // NOI18N
             final Coordinate c = ((Coordinate)splitPolygonFromHandle.getClientProperty("coordinate"));   // NOI18N
             final Point2D ret = new Point2D.Double((double)splitPolygonFromHandle.getLocator().locateX(),
                     (double)splitPolygonFromHandle.getLocator().locateY());
             return ret;
         } else {
             return null;
         }
     }
 
     /**
      * Returns if the PFeature in currently in a splitmode.
      *
      * @return  true, if splitmode is active, else false
      */
     public boolean inSplitProgress() {
         final CurrentStackTrace cst = new CurrentStackTrace();
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("splitPolygonFromHandle:" + splitPolygonFromHandle, cst);                                   // NOI18N
             }
         }
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("splitPolygonToHandle:" + splitPolygonToHandle, cst);                                       // NOI18N
             }
         }
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("inSplitProgress=" + ((splitPolygonFromHandle != null) && (splitPolygonToHandle == null))); // NOI18N
             }
         }
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("in inSplitProgress this=" + this);                                                         // NOI18N
             }
         }
         return ((splitPolygonFromHandle != null) && (splitPolygonToHandle == null));
     }
 
     /**
      * Zerlegt das Feature dieses PFeatures in zwei Features an Hand einer vom Benutzer gezogenen Linie zwischen 2
      * Handles.
      *
      * @return  Feature-Array mit den Teilfeatures
      */
     public Feature[] split() {
         if (isSplittable()) {
             final PureNewFeature[] ret = new PureNewFeature[2];
             int from = ((Integer)(splitPolygonFromHandle.getClientProperty("coordinate_position_coord"))).intValue(); // NOI18N
             int to = ((Integer)(splitPolygonToHandle.getClientProperty("coordinate_position_coord"))).intValue();     // NOI18N
 
             splitPolygonToHandle = null;
             splitPolygonFromHandle = null;
 
             // In splitPoints.get(0) steht immer from
             // In splitPoint.get(size-1) steht immer to
             // Werden die beiden vertauscht, so muss dies sp\u00E4ter bei der Reihenfolge ber\u00FCcksichtigt werden.
             boolean wasSwapped = false;
 
             if (from > to) {
                 final int swap = from;
                 from = to;
                 to = swap;
                 wasSwapped = true;
             }
             // Erstes Polygon
             if (viewer.isFeatureDebugging()) {
                 if (log.isDebugEnabled()) {
                     log.debug("ErstesPolygon" + (to - from + splitPoints.size())); // NOI18N
                 }
             }
             final Coordinate[] c1 = new Coordinate[to - from + splitPoints.size()];
             int counter = 0;
 
             // TODO multipolygon / multilinestring
             final Coordinate[] coordArr = entityRingCoordArr[0][0];
             for (int i = from; i <= to; ++i) {
                 c1[counter] = (Coordinate)coordArr[i].clone();
                 counter++;
             }
             if (wasSwapped) {
                 if (viewer.isFeatureDebugging()) {
                     if (log.isDebugEnabled()) {
                         log.debug("SWAPPED"); // NOI18N
                     }
                 }
                 for (int i = 1; i < (splitPoints.size() - 1); ++i) {
                     final Point2D splitPoint = (Point2D)splitPoints.get(i);
                     final Coordinate splitCoord = new Coordinate(wtst.getSourceX(splitPoint.getX()),
                             wtst.getSourceY(splitPoint.getY()));
                     c1[counter] = splitCoord;
                     counter++;
                 }
             } else {
                 if (viewer.isFeatureDebugging()) {
                     if (log.isDebugEnabled()) {
                         log.debug("NOT_SWAPPED"); // NOI18N
                     }
                 }
                 for (int i = splitPoints.size() - 2; i > 0; --i) {
                     final Point2D splitPoint = (Point2D)splitPoints.get(i);
                     final Coordinate splitCoord = new Coordinate(wtst.getSourceX(splitPoint.getX()),
                             wtst.getSourceY(splitPoint.getY()));
                     c1[counter] = splitCoord;
                     counter++;
                 }
             }
             c1[counter] = (Coordinate)coordArr[from].clone();
             ret[0] = new PureNewFeature(c1, wtst);
             ret[0].setEditable(true);
 
             // Zweites Polygon
             // Größe Array= (Anzahl vorh. Coords) - (anzahl vorh. Handles des ersten Polygons) + (SplitLinie )
             final Coordinate[] c2 = new Coordinate[(coordArr.length) - (to - from + 1) + splitPoints.size()];
             counter = 0;
             for (int i = 0; i <= from; ++i) {
                 c2[counter] = (Coordinate)coordArr[i].clone();
                 counter++;
             }
             if (wasSwapped) {
                 if (viewer.isFeatureDebugging()) {
                     if (log.isDebugEnabled()) {
                         log.debug("SWAPPED"); // NOI18N
                     }
                 }
                 for (int i = splitPoints.size() - 2; i > 0; --i) {
                     final Point2D splitPoint = (Point2D)splitPoints.get(i);
                     final Coordinate splitCoord = new Coordinate(wtst.getSourceX(splitPoint.getX()),
                             wtst.getSourceY(splitPoint.getY()));
                     c2[counter] = splitCoord;
                     counter++;
                 }
             } else {
                 if (viewer.isFeatureDebugging()) {
                     if (log.isDebugEnabled()) {
                         log.debug("NOT_SWAPPED"); // NOI18N
                     }
                 }
                 for (int i = 1; i < (splitPoints.size() - 1); ++i) {
                     final Point2D splitPoint = (Point2D)splitPoints.get(i);
                     final Coordinate splitCoord = new Coordinate(wtst.getSourceX(splitPoint.getX()),
                             wtst.getSourceY(splitPoint.getY()));
                     c2[counter] = splitCoord;
                     counter++;
                 }
             }
 
             for (int i = to; i < coordArr.length; ++i) {
                 c2[counter] = (Coordinate)coordArr[i].clone();
                 counter++;
             }
 //            c1[counter]=(Coordinate)coordArr[0].clone();
             for (int i = 0; i < c2.length; ++i) {
                 if (viewer.isFeatureDebugging()) {
                     if (log.isDebugEnabled()) {
                         log.debug("c2[" + i + "]=" + c2[i]); // NOI18N
                     }
                 }
             }
 //            ret[1]=new PFeature(c2,wtst,x_offset,y_offset,viewer);
             ret[1] = new PureNewFeature(c2, wtst);
             ret[1].setEditable(true);
 //            ret[0].setViewer(viewer);
 //            ret[1].setViewer(viewer);
             return ret;
 //            ret[1]=new PFeature(c1,wtst,x_offset,y_offset);
 //            ret[0].setViewer(viewer);
 //            ret[1].setViewer(viewer);
 //            return ret;
         } else {
             return null;
         }
     }
 
     /**
      * Moves the PFeature for a certain dimension.
      *
      * @param  dim  PDimension to move
      */
     public void moveFeature(final PDimension dim) {
         try {
             final double scale = viewer.getCamera().getViewScale();
             if (viewer.isFeatureDebugging()) {
                 if (log.isDebugEnabled()) {
                     log.debug("Scale=" + scale); // NOI18N
                 }
             }
             for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
                 for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                     for (int coordIndex = 0; coordIndex < entityRingCoordArr[entityIndex][ringIndex].length;
                                 ++coordIndex) {
                         final Coordinate[] coordArr = entityRingCoordArr[entityIndex][ringIndex];
                         final float[] xArr = entityRingXArr[entityIndex][ringIndex];
                         final float[] yArr = entityRingYArr[entityIndex][ringIndex];
 
                         xArr[coordIndex] = xArr[coordIndex] + (float)(dim.getWidth() / (float)scale);
                         yArr[coordIndex] = yArr[coordIndex] + (float)(dim.getHeight() / (float)scale);
                         coordArr[coordIndex].x = wtst.getSourceX(xArr[coordIndex]); // -x_offset);
                         coordArr[coordIndex].y = wtst.getSourceY(yArr[coordIndex]); // -y_offset);
                     }
                 }
             }
             updatePath();
             syncGeometry();
             resetInfoNodePosition();
         } catch (NullPointerException npe) {
             log.warn("error at moveFeature:", npe);                                 // NOI18N
         }
     }
 
     /**
      * Sets the offset of the stickychild to the interiorpoint of this PFeature.
      */
     public void resetInfoNodePosition() {
         if (stickyChild != null) {
             final Geometry geom = CrsTransformer.transformToGivenCrs(getFeature().getGeometry(),
                     getViewerCrs().getCode());
             if (viewer.isFeatureDebugging()) {
                 if (log.isDebugEnabled()) {
                     log.debug("getFeature().getGeometry():" + geom);                  // NOI18N
                 }
             }
             if (viewer.isFeatureDebugging()) {
                 if (log.isDebugEnabled()) {
                     log.debug("getFeature().getGeometry().getInteriorPoint().getY():" // NOI18N
                                 + geom.getInteriorPoint().getY());
                 }
             }
             stickyChild.setOffset(wtst.getScreenX(geom.getInteriorPoint().getX()),
                 wtst.getScreenY(geom.getInteriorPoint().getY()));
         }
     }
 
     /**
      * Renews the InfoNode by deleting the old and creating a new one.
      */
     public void refreshInfoNode() {
         if ((stickyChild == infoNode) && (infoNode != null)) {
             stickyChild = null;
             removeChild(infoNode);
         } else if ((stickyChild != null) && (infoNode != null)) {
             stickyChild.removeChild(infoNode);
         }
         addInfoNode();
     }
 
     /**
      * Calls refreshInfoNode() in the EDT.
      */
     @Override
     public void refresh() {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     if (log.isDebugEnabled()) {
                         log.debug("refreshInfoNode"); // NOI18N
                     }
                     PFeature.this.refreshInfoNode();
                 }
             });
     }
 
     /**
      * Creates an InfoPanel which is located in a PSwingComponent. This component will be added as child of this
      * PFeature. The InfoPanel contains the featuretype as icon and the name of the PFeature.
      */
     public void addInfoNode() {
         try {
             if (getFeature() instanceof XStyledFeature) {
                 final XStyledFeature xsf = (XStyledFeature)getFeature();
 
                 if (infoComponent == null) {
                     infoComponent = xsf.getInfoComponent(this);
                 }
 
                 if (viewer.isFeatureDebugging()) {
                     if (log.isDebugEnabled()) {
                         log.debug("ADD INFONODE3"); // NOI18N
                     }
                 }
                 if (infoPanel != null) {
                     viewer.getSwingWrapper().remove(infoPanel);
                 }
 
                 infoPanel = new InfoPanel(infoComponent);
                 infoPanel.setPfeature(this);
                 infoPanel.setTitleText(xsf.getName());
                 infoPanel.setTitleIcon(xsf.getIconImage());
 
                 pswingComp = new PSwing(viewer, infoPanel);
                 pswingComp.resetBounds();
                 pswingComp.setOffset(0, 0);
 
 //            PText pt=new PText(xsf.getName());
 //            if (getFeature().isEditable()) {
 //                pt.setTextPaint(new Color(255,0,0));
 //            } else {
 //                pt.setTextPaint(new Color(0,0,0));
 //            }
 //            int width=(int)(pt.getWidth()+pi.getWidth());
 //            int height=(int)(pi.getHeight());
 
                 // Dieser node wird gebraucht damit die Mouseover sachen funktionieren. Geht nicht mit einem PSwing.
                 // Auch nicht wenn das PSwing Element ParentNodeIsAPFeature & PSticky implementieren
                 final StickyPPath p = new StickyPPath(new Rectangle(0, 0, 1, 1));
                 p.setStroke(null);
                 p.setPaint(new Color(250, 0, 0, 0)); // letzer Wert Wert Alpha: Wenn 0 dann unsichtbar
                 p.setStrokePaint(null);
                 infoPanel.setPNodeParent(p);
                 infoPanel.setPSwing(pswingComp);
 
                 p.addChild(pswingComp);
                 pswingComp.setOffset(0, 0);
 
                 if (stickyChild != null) {
                     stickyChild.addChild(p);
                     p.setOffset(stickyChild.getWidth(), 0);
                 } else if (getFeature().getGeometry() != null) {
                     syncGeometry();
                     final Geometry geom = CrsTransformer.transformToGivenCrs(getFeature().getGeometry(),
                             getViewerCrs().getCode());
                     Point interiorPoint = null;
                     try {
                         interiorPoint = geom.getInteriorPoint();
                     } catch (TopologyException e) {
                         log.warn("Interior point of geometry couldn't be calculated. Try to use buffering.");
                         // see http://www.vividsolutions.com/JTS/bin/JTS%20Developer%20Guide.pdf, p. 11/12
                     }
                     if (interiorPoint == null) {
                         final GeometryFactory factory = new GeometryFactory();
                         final GeometryCollection collection = factory.createGeometryCollection(new Geometry[] { geom });
                         final Geometry union = collection.buffer(0);
                         interiorPoint = union.getInteriorPoint();
                     }
                     p.setOffset(wtst.getScreenX(interiorPoint.getX()),
                         wtst.getScreenY(interiorPoint.getY()));
                     addChild(p);
                     p.setWidth(pswingComp.getWidth());
                     p.setHeight(pswingComp.getHeight());
                     stickyChild = p;
                     if (!ignoreStickyFeature) {
                         viewer.addStickyNode(p);
                         viewer.rescaleStickyNodes();
                     }
                     if (viewer.isFeatureDebugging()) {
                         if (log.isDebugEnabled()) {
                             log.debug("addInfoNode()"); // NOI18N
                         }
                     }
                 }
                 infoNode = p;
                 if ((viewer != null) && (infoNode != null)) {
                     infoNode.setVisible(viewer.isInfoNodesVisible());
                     if (viewer.isFeatureDebugging()) {
                         if (log.isDebugEnabled()) {
                             log.debug("addInfoNode()"); // NOI18N
                         }
                     }
                     viewer.rescaleStickyNodes();
                     p.setWidth(pswingComp.getWidth());
                     p.setHeight(pswingComp.getHeight());
                 } else {
                     if (infoNode != null) {
                         infoNode.setVisible(false);
                     }
                 }
                 pswingComp.addPropertyChangeListener("fullBounds", new PropertyChangeListener() { // NOI18N
                         @Override
                         public void propertyChange(final PropertyChangeEvent evt) {
                             p.setWidth(pswingComp.getWidth());
                             p.setHeight(pswingComp.getHeight());
                         }
                     });
             }
         } catch (Throwable t) {
             log.error("Error in AddInfoNode", t);       // NOI18N
         }
     }
 
     /**
      * Deletes the InfoPanel and hides the PFeature.
      */
     public void cleanup() {
         if (infoPanel != null) {
             infoPanel.setVisible(false);
             viewer.getSwingWrapper().remove(infoPanel);
         }
         this.setVisible(false);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void ensureFullVisibility() {
         final PBounds all = viewer.getCamera().getViewBounds();
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("getViewBounds()" + all);             // NOI18N
             }
         }
         final PBounds newBounds = new PBounds();
         newBounds.setRect(this.getFullBounds().createUnion(all.getBounds2D()));
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("getFullBounds()" + getFullBounds()); // NOI18N
             }
         }
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("newBounds" + newBounds);             // NOI18N
             }
         }
         viewer.getCamera().animateViewToCenterBounds(newBounds.getBounds2D(), true, viewer.getAnimationDuration());
         viewer.refresh();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isInfoNodeExpanded() {
         return (infoPanel != null) && infoPanel.isExpanded();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  expanded  DOCUMENT ME!
      */
     public void setInfoNodeExpanded(final boolean expanded) {
         if (infoPanel != null) {
             infoPanel.setExpanded(expanded, false);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  selectedOriginal  DOCUMENT ME!
      */
     public void setSelectedOriginal(final PFeature selectedOriginal) {
         this.selectedOriginal = selectedOriginal;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public PFeature getSelectedOriginal() {
         return selectedOriginal;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Feature getFeature() {
         return feature;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  feature  DOCUMENT ME!
      */
     public void setFeature(final Feature feature) {
         this.feature = feature;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public PPath getSplitLine() {
         return splitPolygonLine;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public List<Point2D> getSplitPoints() {
         return splitPoints;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isSplittable() {
         if ((splitPolygonFromHandle != null) && (splitPolygonToHandle != null)) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Zeichnet das PFeature bei einem RollOver um 40% heller.
      *
      * @param  highlighting  true, wenn das PFeature hervorgehoben werden soll
      */
     @Override
     public void setHighlighting(final boolean highlighting) {
         final boolean highlightingEnabledIfStyledFeature = ((getFeature() != null)
                         && !(getFeature() instanceof StyledFeature))
                     || ((getFeature() != null) && ((StyledFeature)getFeature()).isHighlightingEnabled());
         if (!isSelected() && (getPaint() != null) && highlightingEnabledIfStyledFeature) {
             highlighted = highlighting;
             if (highlighted) {
                 nonHighlightingPaint = getPaint();
                 if (nonHighlightingPaint instanceof Color) {
                     final Color c = (Color)nonHighlightingPaint;
                     int red = (int)(c.getRed() + 70);
                     int green = (int)(c.getGreen() + 70);
                     int blue = (int)(c.getBlue() + 70);
                     if (red > 255) {
                         red = 255;
                     }
                     if (green > 255) {
                         green = 255;
                     }
                     if (blue > 255) {
                         blue = 255;
                     }
                     setPaint(new Color(red, green, blue, c.getAlpha()));
                 } else {
                     setPaint(new Color(1f, 1f, 1f, 0.6f));
                 }
             } else {
                 setPaint(nonHighlightingPaint);
             }
             repaint();
         }
     }
 
     /**
      * Liefert ein boolean, ob das Pfeature gerade hervorgehoben wird.
      *
      * @return  true, falls hervorgehoben
      */
     @Override
     public boolean getHighlighting() {
         return highlighted;
     }
 
     /**
      * Selektiert das PFeature je nach \u00FCbergebenem boolean-Wert.
      *
      * @param  selected  true, markiert. false, nicht markiert
      */
     @Override
     public void setSelected(final boolean selected) {
         if (viewer.isFeatureDebugging()) {
             if (log.isDebugEnabled()) {
                 log.debug("setSelected(" + selected + ")"); // NOI18N
             }
         }
 
         this.selected = selected;
         this.selectedEntity = -1;
 
         boolean showSelected = true;
         if (getFeature() instanceof DrawSelectionFeature) {
             showSelected = (((DrawSelectionFeature)getFeature()).isDrawingSelection());
         }
         if (showSelected) {
             showSelected(selected);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  selected  DOCUMENT ME!
      */
     private void showSelected(final boolean selected) {
         splitPolygonFromHandle = null;
         splitPolygonToHandle = null;
         if (this.selected && !selected) {
             pivotHandle = null;
         }
         this.selected = selected;
 
         // PUNKT
         if (getFeature().getGeometry() instanceof Point) {
             if ((pi != null) && (piSelected != null)) {
                 piSelected.setVisible(selected);
                 pi.setVisible(!selected);
                 /*
                  * since we have two different FeatureAnnotationSymbols for selection and normal we have to switch the
                  * infoNode to them depending on selection state
                  */
                 if (selected) {
                     wasSelected = true;
 //                    addInfoNode();
                     if (infoNode != null) {
                        try {
                            pi.removeChild(infoNode);
                        } catch (NullPointerException e) {
                            // This happens when you move a LinearReferencedLineFeature on the map
                            log.debug("removeChild throws a NullPointerException", e);
                        }
                         piSelected.addChild(infoNode);
                     }
                 } else if (wasSelected) {
                     wasSelected = false;
                     if (infoNode != null) {
                         piSelected.removeChild(infoNode);
                         pi.addChild(infoNode);
                     }
                 }
             }
             viewer.rescaleStickyNodes();
         }                                                                                                  // LINESTRING
         else if ((feature.getGeometry() instanceof LineString) || (feature.getGeometry() instanceof MultiLineString)) {
             if (selected) {
                 final CustomFixedWidthStroke fws = new CustomFixedWidthStroke(5f);
                 setStroke(fws);
                 setStrokePaint(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground")); // NOI18N
                 setPaint(null);
             } else {
                 // setStroke(new FixedWidthStroke());
                 if (stroke != null) {
                     setStroke(stroke);
                 } else {
                     setStroke(FIXED_WIDTH_STROKE);
                 }
                 if (strokePaint != null) {
                     setStrokePaint(strokePaint);
                 } else {
                     setStrokePaint(Color.black);
                 }
             }
         } // POLYGON
         else {
             if (stroke != null) {
                 setStroke(stroke);
             } else {
                 setStroke(FIXED_WIDTH_STROKE);
             }
 
             if (selected) {
                 nonSelectedPaint = getPaint();
                 if (nonSelectedPaint instanceof Color) {
                     final Color c = (Color)nonHighlightingPaint;
                     if (c != null) {
                         final int red = (int)(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground")
                                         .getRed());                           // NOI18N
                         final int green = (int)(javax.swing.UIManager.getDefaults().getColor(
                                     "Table.selectionBackground").getGreen()); // NOI18N
                         final int blue = (int)(javax.swing.UIManager.getDefaults().getColor(
                                     "Table.selectionBackground").getBlue());  // NOI18N
                         setPaint(new Color(red, green, blue, c.getAlpha() / 2));
                     }
                 } else {
                     setPaint(new Color(172, 210, 248, 178));
                 }
             } else {
                 setPaint(nonHighlightingPaint);
             }
         }
         repaint();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  s  DOCUMENT ME!
      */
     @Override
     public void setStroke(final Stroke s) {
         // log.debug("setStroke: " + s, new CurrentStackTrace());
         super.setStroke(s);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public boolean isSelected() {
         return selected;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  polygon  DOCUMENT ME!
      */
     public void addEntity(final Polygon polygon) {
         if (getFeature().isEditable()) {
             final int numOfHoles = polygon.getNumInteriorRing();
             final Coordinate[][][] origEntityCoordArr = entityRingCoordArr;
 
             // neues entityRingCoordArr mit entity-länge + 1, und alte daten daten darin kopieren
             final Coordinate[][][] newEntityCoordArr = new Coordinate[origEntityCoordArr.length + 1][][];
             System.arraycopy(origEntityCoordArr, 0, newEntityCoordArr, 0, origEntityCoordArr.length);
 
             // neues ringCoordArr für neues entity erzeugen, und Hülle + Löcher darin speicherm
             final Coordinate[][] newRingCoordArr = new Coordinate[1 + numOfHoles][];
             newRingCoordArr[0] = polygon.getExteriorRing().getCoordinates();
             for (int ringIndex = 1; ringIndex < newRingCoordArr.length; ++ringIndex) {
                 newRingCoordArr[ringIndex] = polygon.getInteriorRingN(ringIndex - 1).getCoordinates();
             }
 
             // neues entity an letzte stelle speichern, und als neues entityRingCoordArr übernehmen
             newEntityCoordArr[origEntityCoordArr.length] = newRingCoordArr;
             entityRingCoordArr = newEntityCoordArr;
 
             // refresh
             syncGeometry();
             updateXpAndYp();
             updatePath();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  entityPosition  DOCUMENT ME!
      */
     public void removeEntity(final int entityPosition) {
         if (getFeature().isEditable()) {
             final Coordinate[][][] origEntityCoordArr = entityRingCoordArr;
 
             final boolean isInBounds = (entityPosition >= 0) && (entityPosition < origEntityCoordArr.length);
 
             if (isInBounds) {
                 if (origEntityCoordArr.length == 1) {           // wenn nur ein entity drin
                     entityRingCoordArr = new Coordinate[0][][]; // dann nur durch leeres ersetzen
                 } else {                                        // wenn mehr als ein entity drin
 
                     // neues entityRingCoordArr mit entity-länge - 1, und originaldaten daten darin kopieren außer
                     // entityPosition
                     final Coordinate[][][] newEntityCoordArr = new Coordinate[origEntityCoordArr.length - 1][][];
                     // alles vor entityPosition
                     System.arraycopy(origEntityCoordArr, 0, newEntityCoordArr, 0, entityPosition);
                     // alles nach entityPosition
                     System.arraycopy(
                         origEntityCoordArr,
                         entityPosition
                                 + 1,
                         newEntityCoordArr,
                         entityPosition,
                         newEntityCoordArr.length
                                 - entityPosition);
                     // original durch neues ersetzen
                     entityRingCoordArr = newEntityCoordArr;
                 }
 
                 // refresh
                 syncGeometry();
                 updateXpAndYp();
                 updatePath();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  entityPosition  DOCUMENT ME!
      * @param  lineString      DOCUMENT ME!
      */
     public void addHoleToEntity(final int entityPosition, final LineString lineString) {
         if (getFeature().isEditable()) {
             final boolean isInBounds = (entityPosition >= 0) && (entityPosition < entityRingCoordArr.length);
             if (isInBounds) {
                 final Coordinate[][] origRingCoordArr = entityRingCoordArr[entityPosition];
                 final int origLength = origRingCoordArr.length;
 
                 final Coordinate[][] newRingCoordArr = new Coordinate[origLength + 1][];
                 System.arraycopy(origRingCoordArr, 0, newRingCoordArr, 0, origLength);
                 newRingCoordArr[origLength] = lineString.getCoordinates();
 
                 entityRingCoordArr[entityPosition] = newRingCoordArr;
             }
 
             syncGeometry();
             updateXpAndYp();
             updatePath();
         }
     }
 
     /**
      * alle entities die diesen punkt beinhalten (löscher werden ignoriert, da sonst nur eine entity existieren kann).
      *
      * @param   point  coordinate DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private List<Integer> getEntitiesPositionsUnderPoint(final Point point) {
         final List<Integer> positions = new ArrayList<Integer>();
         final Geometry geometry = getFeature().getGeometry();
         for (int entityIndex = 0; entityIndex < geometry.getNumGeometries(); entityIndex++) {
             final Geometry envelope = geometry.getEnvelope(); // ohne löscher
             if (envelope.contains(point)) {
                 positions.add(entityIndex);
             }
         }
         return positions;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  point  coordinate DOCUMENT ME!
      */
     public void removeHoleUnderPoint(final Point point) {
         final int entityPosition = getMostInnerEntityUnderPoint(point);
 
         final boolean isEntityInBounds = (entityPosition >= 0) && (entityPosition < entityRingCoordArr.length);
         if (isEntityInBounds) {
             final Coordinate[][] origRingCoordArr = entityRingCoordArr[entityPosition];
             final int holePosition = getHolePositionUnderPoint(point, entityPosition);
             final boolean isRingInBounds = (holePosition >= 0) && (holePosition < origRingCoordArr.length);
 
             if (isRingInBounds) {
                 final Polygon entityPolygon = ((Polygon)getFeature().getGeometry().getGeometryN(entityPosition));
                 final Geometry holeGeometry = entityPolygon.getInteriorRingN(holePosition - 1).getEnvelope(); // zu entfernende
                 // Geometrie, ohne
                 // Löcher
                 if (!hasEntitiesInGeometry(holeGeometry)) {
                     final Coordinate[][] newRingCoordArr = new Coordinate[origRingCoordArr.length - 1][];
                     System.arraycopy(origRingCoordArr, 0, newRingCoordArr, 0, holePosition);
                     System.arraycopy(
                         origRingCoordArr,
                         holePosition
                                 + 1,
                         newRingCoordArr,
                         holePosition,
                         newRingCoordArr.length
                                 - holePosition);
 
                     // original durch neues ersetzen
                     entityRingCoordArr[entityPosition] = newRingCoordArr;
 
                     // refresh
                     syncGeometry();
                     updateXpAndYp();
                     updatePath();
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   point           coordinate DOCUMENT ME!
      * @param   entityPosition  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getHolePositionUnderPoint(final Point point, final int entityPosition) {
         final Geometry geometry = getFeature().getGeometry();
         final boolean isInBounds = (entityPosition >= 0) && (entityPosition < geometry.getNumGeometries());
         if (isInBounds) {
             final Polygon polygon = (Polygon)geometry.getGeometryN(entityPosition);
 
             if (polygon.getNumInteriorRing() > 0) { // hat überhaupt löscher ?
                 for (int ringIndex = 0; ringIndex < polygon.getNumInteriorRing(); ringIndex++) {
                     final Geometry envelope = polygon.getInteriorRingN(ringIndex).getEnvelope();
                     if (envelope.contains(point)) {
                         return ringIndex + 1;       // +1 weil ring 0 der äußere ring ist
                     }
                 }
             }
         }
         return -1;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   point  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private int getMostInnerEntityUnderPoint(final Point point) {
         // alle außenringe (löscher werden zunächst ignoriert) holen die grundsätzlich unter der koordinate liegen
         final List<Integer> entityPositions = getEntitiesPositionsUnderPoint(point);
 
         // interessant sind nur entities die Löscher haben
         final List<Integer> entityPositionsWithHoles = new ArrayList<Integer>();
         for (final int position : entityPositions) {
             if (entityRingCoordArr[position].length > 1) {
                 entityPositionsWithHoles.add(position);
             }
         }
 
         final Geometry geometry = getFeature().getGeometry();
 
         if (entityPositionsWithHoles.size() == 1) {
             return entityPositionsWithHoles.get(0); // nur eine entity mit loch, also muss sie das sein
         } else {
             // mehrere entities, es wird geprüft welche entity welche andere beinhaltet
             for (int indexA = 0; indexA < entityPositionsWithHoles.size(); indexA++) {
                 final int entityPositionA = entityPositionsWithHoles.get(indexA);
                 final Geometry envelopeA = geometry.getGeometryN(entityPositionA).getEnvelope();
 
                 boolean containsAnyOtherRing = false;
                 for (int indexB = 0; indexB < entityPositionsWithHoles.size(); indexB++) {
                     if (indexA != indexB) {
                         final int entityPositionB = entityPositionsWithHoles.get(indexB);
                         final Geometry envelopeB = geometry.getGeometryN(entityPositionB).getEnvelope();
                         if (envelopeA.contains(envelopeB)) {
                             containsAnyOtherRing = true;
                         }
                     }
                 }
                 if (!containsAnyOtherRing) {
                     return entityPositionA;
                 }
             }
 
             return -1;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   point  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getEntityPositionUnderPoint(final Point point) {
         for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
             final Geometry geometry = getFeature().getGeometry().getGeometryN(entityIndex);
 
             if (geometry.contains(point)) {
                 return entityIndex;
             }
         }
         return -1;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   geometry  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean hasEntitiesInGeometry(final Geometry geometry) {
         for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
             final Geometry entityGeometry = getFeature().getGeometry().getGeometryN(entityIndex);
 
             if (geometry.contains(entityGeometry)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  entityPosition  DOCUMENT ME!
      */
     public void setSelectedEntity(final int entityPosition) {
         final boolean isInBounds = (entityPosition >= 0) && (entityPosition < entityRingCoordArr.length);
         if (isInBounds) {
             selectedEntity = entityPosition;
         } else {
             selectedEntity = -1;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   toSelect   DOCUMENT ME!
      * @param   colFill    DOCUMENT ME!
      * @param   colEdge    DOCUMENT ME!
      * @param   insetSize  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Image highlightImageAsSelected(final Image toSelect, Color colFill, Color colEdge, final int insetSize) {
         if (colFill == null) {
             colFill = TRANSPARENT;
         }
         if (colEdge == null) {
             colEdge = TRANSPARENT;
         }
         if (toSelect != null) {
             final int doubleInset = 2 * insetSize;
             final BufferedImage tint = new BufferedImage(toSelect.getWidth(null) + doubleInset,
                     toSelect.getHeight(null)
                             + doubleInset,
                     BufferedImage.TYPE_INT_ARGB);
             final Graphics2D g2d = (Graphics2D)tint.getGraphics();
             g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
             g2d.setPaint(colFill);
             g2d.fillRoundRect(
                 0,
                 0,
                 toSelect.getWidth(null)
                         - 1
                         + doubleInset,
                 toSelect.getHeight(null)
                         - 1
                         + doubleInset,
                 10,
                 10);
             g2d.setPaint(colEdge);
             g2d.drawRoundRect(
                 0,
                 0,
                 toSelect.getWidth(null)
                         - 1
                         + doubleInset,
                 toSelect.getHeight(null)
                         - 1
                         + doubleInset,
                 10,
                 10);
             g2d.drawImage(toSelect, insetSize, insetSize, null);
             return tint;
         } else {
             return toSelect;
         }
     }
 
     /**
      * Ver\u00E4ndert die Sichtbarkeit der InfoNode.
      *
      * @param  visible  true, wenn die InfoNode sichtbar sein soll
      */
     public void setInfoNodeVisible(final boolean visible) {
         if (infoNode != null) {
             infoNode.setVisible(visible);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public MappingComponent getViewer() {
         return viewer;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  viewer  DOCUMENT ME!
      */
     public void setViewer(final MappingComponent viewer) {
         this.viewer = viewer;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Crs getViewerCrs() {
         return viewer.getMappingModel().getSrs();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public MappingComponent getMappingComponent() {
         return viewer;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Paint getNonSelectedPaint() {
         return nonSelectedPaint;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  nonSelectedPaint  DOCUMENT ME!
      */
     public void setNonSelectedPaint(final Paint nonSelectedPaint) {
         this.nonSelectedPaint = nonSelectedPaint;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Paint getNonHighlightingPaint() {
         return nonHighlightingPaint;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  nonHighlightingPaint  DOCUMENT ME!
      */
     public void setNonHighlightingPaint(final Paint nonHighlightingPaint) {
         this.nonHighlightingPaint = nonHighlightingPaint;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  entityPosition  coordEntity DOCUMENT ME!
      * @param  ringPosition    DOCUMENT ME!
      * @param  xp              DOCUMENT ME!
      * @param  yp              DOCUMENT ME!
      * @param  coordArr        DOCUMENT ME!
      */
     private void setNewCoordinates(final int entityPosition,
             final int ringPosition,
             final float[] xp,
             final float[] yp,
             final Coordinate[] coordArr) {
         if (isValidWithThisCoordinates(entityPosition, ringPosition, coordArr)) {
             entityRingCoordArr[entityPosition][ringPosition] = coordArr;
             entityRingXArr[entityPosition][ringPosition] = xp;
             entityRingYArr[entityPosition][ringPosition] = yp;
 
             syncGeometry();
 
             updatePath();
             getViewer().showHandles(false);
             final Collection<Feature> features = new ArrayList<Feature>();
             features.add(getFeature());
             ((DefaultFeatureCollection)getViewer().getFeatureCollection()).fireFeaturesChanged(features);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   entityPosition  DOCUMENT ME!
      * @param   ringPosition    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isValid(final int entityPosition, final int ringPosition) {
         return isValidWithThisCoordinates(
                 entityPosition,
                 ringPosition,
                 getCoordArr(entityPosition, ringPosition));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   entityPosition  DOCUMENT ME!
      * @param   ringCoordArr    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isValidWithThisEntity(final int entityPosition, final Coordinate[][] ringCoordArr) {
         // polygon für die prüfung erzeugen
         final Geometry newGeometry;
         try {
             final GeometryFactory geometryFactory = new GeometryFactory(
                     new PrecisionModel(PrecisionModel.FLOATING),
                     CrsTransformer.extractSridFromCrs(getViewerCrs().getCode()));
             if ((getFeature().getGeometry() instanceof Polygon)
                         || (getFeature().getGeometry() instanceof MultiPolygon)) {
                 newGeometry = createPolygon(ringCoordArr, geometryFactory);
             } else if ((getFeature().getGeometry() instanceof LineString)
                         || (getFeature().getGeometry() instanceof MultiLineString)) {
                 newGeometry = createLineString(ringCoordArr[0], geometryFactory);
             } else if ((getFeature().getGeometry() instanceof Point)
                         || (getFeature().getGeometry() instanceof MultiPoint)) {
                 newGeometry = createPoint(ringCoordArr[0][0], geometryFactory);
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("unknown geometry type");
                 }
                 return false;
             }
 
             if (!newGeometry.isValid()) {
                 return false;
             }
 
             final Geometry geometry = getFeature().getGeometry();
             for (int entityIndex = 0; entityIndex < geometry.getNumGeometries(); entityIndex++) {
                 if ((entityPosition < 0) || (entityIndex != entityPosition)) { // nicht mit sich (bzw seinem alten
                     // selbst) selbst vergleichen
                     final Geometry otherGeometry = geometry.getGeometryN(entityIndex);
                     if (newGeometry.intersects(otherGeometry)) {
                         // polygon schneidet ein anderes teil-polygon
                         return false;
                     }
                 }
             }
             // alles ok
             return true;
         } catch (final Exception ex) {
             if (log.isDebugEnabled()) {
                 // verändertes teil-polygon ist selbst schon nicht gültig;
                 log.debug("invalid geometry", ex);
             }
             return false;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   coordArr  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isValidWithThisNewEntityCoordinates(final Coordinate[] coordArr) {
         final Coordinate[][] tempRingCoordArr = new Coordinate[1][];
         tempRingCoordArr[0] = coordArr;
         return isValidWithThisEntity(-1, tempRingCoordArr);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   entityPosition  DOCUMENT ME!
      * @param   coordArr        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isValidWithThisNewHoleCoordinates(final int entityPosition, final Coordinate[] coordArr) {
         final Coordinate[][] tempRingCoordArr = new Coordinate[entityRingCoordArr[entityPosition].length + 1][];
         System.arraycopy(
             entityRingCoordArr[entityPosition],
             0,
             tempRingCoordArr,
             0,
             entityRingCoordArr[entityPosition].length);
         tempRingCoordArr[entityRingCoordArr[entityPosition].length] = coordArr;
         return isValidWithThisEntity(entityPosition, tempRingCoordArr);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   entityPosition  DOCUMENT ME!
      * @param   ringPosition    DOCUMENT ME!
      * @param   coordArr        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isValidWithThisCoordinates(final int entityPosition,
             final int ringPosition,
             final Coordinate[] coordArr) {
         // copy von original teil-polygon machen
         final Coordinate[][] tempRingCoordArr = new Coordinate[entityRingCoordArr[entityPosition].length][];
         System.arraycopy(
             entityRingCoordArr[entityPosition],
             0,
             tempRingCoordArr,
             0,
             entityRingCoordArr[entityPosition].length);
         // ring in der kopie austauschen
         tempRingCoordArr[ringPosition] = coordArr;
 
         return isValidWithThisEntity(entityPosition, tempRingCoordArr);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void updatePath() {
         getPathReference().reset();
         final Geometry geom = feature.getGeometry();
         if (geom instanceof Point) {
             setPathToPolyline(
                 new float[] { entityRingXArr[0][0][0], entityRingXArr[0][0][0] },
                 new float[] { entityRingYArr[0][0][0], entityRingYArr[0][0][0] });
         } else if ((geom instanceof LineString) || (geom instanceof MultiPoint)) {
             setPathToPolyline(entityRingXArr[0][0], entityRingYArr[0][0]);
         } else if ((geom instanceof Polygon) || (geom instanceof MultiPolygon)) {
             getPathReference().setWindingRule(GeneralPath.WIND_EVEN_ODD);
             for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
                 for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                     final Coordinate[] coordArr = entityRingCoordArr[entityIndex][ringIndex];
                     addLinearRing(coordArr);
                 }
             }
         } else if (geom instanceof MultiLineString) {
             for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
                 for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                     final Coordinate[] coordArr = entityRingCoordArr[entityIndex][ringIndex];
                     addLinearRing(coordArr);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public PNode getInfoNode() {
         return infoNode;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public PNode getStickyChild() {
         return stickyChild;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean hasSecondStickyChild() {
         return (secondStickyChild != null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public PNode getSecondStickyChild() {
         return secondStickyChild;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isSnappable() {
         return snappable;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  snappable  DOCUMENT ME!
      */
     public void setSnappable(final boolean snappable) {
         this.snappable = snappable;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param       coordArr  DOCUMENT ME!
      *
      * @deprecated  DOCUMENT ME!
      */
     public void setCoordArr(final Coordinate[] coordArr) {
         entityRingCoordArr = new Coordinate[][][] {
                 { coordArr }
             };
         updateXpAndYp();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  entityPosition  DOCUMENT ME!
      * @param  ringPosition    DOCUMENT ME!
      * @param  coordArr        DOCUMENT ME!
      */
     public void setCoordArr(final int entityPosition, final int ringPosition, final Coordinate[] coordArr) {
         entityRingCoordArr[entityPosition][ringPosition] = coordArr;
         updateXpAndYp();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getNumOfEntities() {
         return entityRingCoordArr.length;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   entityIndex  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getNumOfRings(final int entityIndex) {
         return entityRingCoordArr[entityIndex].length;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class StickyPPath extends PPath implements ParentNodeIsAPFeature, PSticky {
 
         //~ Instance fields ----------------------------------------------------
 
         int transparency = 0;
         Color c = null;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new StickyPPath object.
          *
          * @param  s  DOCUMENT ME!
          */
         public StickyPPath(final Shape s) {
             super(s);
         }
     }
 
     /**
      * StickyPText represents the annotation of a PFeature.
      *
      * @version  $Revision$, $Date$
      */
     class StickyPText extends PText implements ParentNodeIsAPFeature, PSticky {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new StickyPText object.
          */
         public StickyPText() {
             super();
         }
 
         /**
          * Creates a new StickyPText object.
          *
          * @param  text  DOCUMENT ME!
          */
         public StickyPText(final String text) {
             super(text);
         }
     }
 }
