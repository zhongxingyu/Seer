 /* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.geoserver.ows.util.RequestUtils;
 import org.geotools.feature.type.DateUtil;
 import org.geotools.map.MapLayer;
 import org.geotools.renderer.style.LineStyle2D;
 import org.geotools.renderer.style.MarkStyle2D;
 import org.geotools.renderer.style.PolygonStyle2D;
 import org.geotools.renderer.style.SLDStyleFactory;
 import org.geotools.renderer.style.Style2D;
 import org.geotools.renderer.style.TextStyle2D;
 import org.geotools.styling.ExternalGraphic;
 import org.geotools.styling.FeatureTypeStyle;
 import org.geotools.styling.LineSymbolizer;
 import org.geotools.styling.Mark;
 import org.geotools.styling.PointSymbolizer;
 import org.geotools.styling.PolygonSymbolizer;
 import org.geotools.styling.Rule;
 import org.geotools.styling.SLD;
 import org.geotools.styling.Symbolizer;
 import org.geotools.styling.TextSymbolizer;
 import org.geotools.util.NumberRange;
 import org.geotools.xml.transform.Translator;
 import org.geotools.xs.bindings.XSDateTimeBinding;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.filter.expression.Expression;
 import org.vfny.geoserver.global.NameSpaceInfo;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.responses.featureInfo.FeatureTemplate;
 import org.vfny.geoserver.wms.responses.featureInfo.FeatureTimeTemplate;
 import org.vfny.geoserver.wms.responses.featureInfo.FeatureHeightTemplate;
 import org.xml.sax.ContentHandler;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.CoordinateFilter;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.LineSegment;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.MultiPoint;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 
 /**
  * Common utility adapter for kml raster/vector transformers.
  * 
  * @author Wayne Fang, Refractions Research, wfang@refractions.net
  * @author Arne Kepp - OpenGeo
  * @author Justin Deoliveira - OpenGeo
  * 
  * @version $Id$
  */
 public abstract class KMLMapTransformer extends KMLTransformerBase {
     /**
      * logger
      */
     static Logger LOGGER = org.geotools.util.logging.Logging
             .getLogger("org.geoserver.kml");
 
     /**
      * The scale denominator.
      * 
      * TODO: calcuate a real value based on image size to bbox ratio, as image
      * size has no meanining for KML yet this is a fudge.
      */
     protected double scaleDenominator = 1;
 
     NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
 
     /**
      * used to create 2d style objects for features
      */
     SLDStyleFactory styleFactory = new SLDStyleFactory();
 
     /**
      * Feature template, cached for performance reasons
      */
     FeatureTemplate template = new FeatureTemplate();
 
     /**
      * The map context
      */
     protected WMSMapContext mapContext;
 
     /**
      * The map layer being transformed
      */
     protected final MapLayer mapLayer;
 
     /**
      * list of formats which correspond to the default formats in which
      * freemarker outputs dates when a user calls the
      * ?datetime(),?date(),?time() fuctions.
      */
     static List/* <SimpleDateFormat> */dtformats = new ArrayList();
 
     static List/* <SimpleDateFormat> */dformats = new ArrayList();
 
     static List/* <SimpleDateFormat> */tformats = new ArrayList();
     static {
 
         // add default freemarker ones first since they are likely to be used
         // first, the order of this list matters.
 
         dtformats.add(DateFormat.getDateTimeInstance());
         dtformats.add(FeatureTemplate.DATETIME_FORMAT);
         addFormats(dtformats, "dd%MM%yy hh:mm:ss");
         addFormats(dtformats, "MM%dd%yy hh:mm:ss");
         // addFormats(formats,"yy%MM%dd hh:mm:ss" );
         addFormats(dtformats, "dd%MMM%yy hh:mm:ss");
         addFormats(dtformats, "MMM%dd%yy hh:mm:ss");
         // addFormats(formats,"yy%MMM%dd hh:mm:ss" );
 
         addFormats(dtformats, "dd%MM%yy hh:mm");
         addFormats(dtformats, "MM%dd%yy hh:mm");
         // addFormats(formats,"yy%MM%dd hh:mm" );
         addFormats(dtformats, "dd%MMM%yy hh:mm");
         addFormats(dtformats, "MMM%dd%yy hh:mm");
         // addFormats(formats,"yy%MMM%dd hh:mm" );
 
         dformats.add(DateFormat.getDateInstance());
         dformats.add(FeatureTemplate.DATE_FORMAT);
         addFormats(dformats, "dd%MM%yy");
         addFormats(dformats, "MM%dd%yy");
         // addFormats(formats,"yy%MM%dd" );
         addFormats(dformats, "dd%MMM%yy");
         addFormats(dformats, "MMM%dd%yy");
         // addFormats(formats,"yy%MMM%dd" );
 
         tformats.add(DateFormat.getTimeInstance());
         tformats.add(FeatureTemplate.TIME_FORMAT);
     }
 
     static void addFormats(List formats, String pattern) {
 
         formats.add(new SimpleDateFormat(pattern.replaceAll("%", "-")));
         formats.add(new SimpleDateFormat(pattern.replaceAll("%", "/")));
         formats.add(new SimpleDateFormat(pattern.replaceAll("%", ".")));
         formats.add(new SimpleDateFormat(pattern.replaceAll("%", " ")));
         formats.add(new SimpleDateFormat(pattern.replaceAll("%", ",")));
 
     }
 
     public KMLMapTransformer(WMSMapContext mapContext, MapLayer mapLayer) {
         this.mapContext = mapContext;
         this.mapLayer = mapLayer;
     }
 
     public abstract class KMLMapTranslatorSupport extends KMLTranslatorSupport {
         /**
          * Geometry transformer
          */
         KMLGeometryTransformer.KMLGeometryTranslator geometryTranslator;
 
         public KMLMapTranslatorSupport(ContentHandler contentHandler) {
             super(contentHandler);
         }
 
         /**
          * Encodes a KML Placemark name from a feature by processing a template.
          */
         protected void encodePlacemarkName(SimpleFeature feature,
                 FeatureTypeStyle[] styles) throws IOException {
 
             // Algorithm for finding name / label of a placemark
             // 1. The title template for feature
             // 2. If the title is the same as the fid
             //    - try getting something better from the SLD 
             // 3. Add <name> with whatever we've got, fid is worst case
 
             String title = template.title(feature);
             boolean trySLD = false;
             
             if (title == null || "".equals(title)) {
                 title = feature.getID();
                 
             }
             
             if(title.equals(feature.getID())) {
                 trySLD = true;
             }
             
             if(trySLD) {
                 StringBuffer label = new StringBuffer();
                 for (int i = 0; i < styles.length; i++) {
                     Rule[] rules = KMLUtils.filterRules(styles[i], feature, scaleDenominator);
                     for (int j = 0; j < rules.length; j++) {
                         
                         Symbolizer[] syms = rules[j].getSymbolizers();
                         for (int k = 0; k < syms.length; k++) {
                             if (syms[k] instanceof TextSymbolizer) {
                                 Expression e = SLD.textLabel((TextSymbolizer) syms[k]);
                                 Object object = null;
                                 if (e != null)
                                     object = e.evaluate(feature);
                                 String value = null;
 
                                 if (object instanceof String) {
                                     value = (String) object;
                                 } else {
                                     if (object != null) {
                                         value = object.toString();
                                     }
                                 }
 
                                 if ((value != null) && !"".equals(value.trim())) {
                                     label.append(value);
                                 }
                             }
                         }
                     }
                 }
             
 
                 if (label.length() > 0) {
                     title = label.toString();
                 }
             }
             
             start("name");
             cdata(title);
             end("name");
         }
 
         /**
          * Encodes the Snipped element
          * 
          * @param feature
          * @param styles
          */
         protected void encodePlacemarkSnippet(SimpleFeature feature,
                 FeatureTypeStyle[] styles) {
             // does nothing at the moment
         }
 
         /**
          * Encodes a KML Placemark description from a feature
          */
         protected void encodePlacemarkDescription(SimpleFeature feature,
                 FeatureTypeStyle[] styles) throws IOException {
 
             String description = template.description(feature);
 
             if (description != null) {
                 start("description");
                 cdata(description);
                 end("description");
             }
         }
 
         /**
          * Encods a KML Placemark LookAt from a geometry + centroid.
          */
         protected void encodePlacemarkLookAt(Coordinate centroid) {
             start("LookAt");
 
             element("longitude", Double.toString(centroid.x));
             element("latitude", Double.toString(centroid.y));
             element("heading", "10.0");
             element("tilt", "10.0");
             element("range", "700");
 
             end("LookAt");
         }
 
         /**
          * Encodes the provided set of rules as KML styles.
          */
         protected boolean encodeStyle(SimpleFeature feature,
                 FeatureTypeStyle[] styles) {
             return encodeStyle(feature, styles, false);
         }
         
         protected boolean encodeStyle(SimpleFeature feature,
                 FeatureTypeStyle[] styles, boolean isVector) {
 
             /**
              * KML requires sorting, should we do it here ? 
              */
             // encode the Line/Poly styles
             List symbolizerList = new ArrayList();
             for (int j = 0; j < styles.length; j++) {
                 Rule[] rules;
                     rules = KMLUtils.filterRules(styles[j], feature,
                             scaleDenominator);
                 for (int i = 0; i < rules.length; i++) {
                     symbolizerList.addAll(Arrays.asList(rules[i]
                             .getSymbolizers()));
                 }
             }
 
             if (!symbolizerList.isEmpty()) {
                 // start the style
                 start("Style", KMLUtils.attributes(new String[] { "id",
                         "GeoServerStyle" + feature.getID() }));
 
                 Symbolizer[] symbolizers = (Symbolizer[]) symbolizerList
                         .toArray(new Symbolizer[symbolizerList.size()]);
                 encodeStyle(feature, symbolizers);
 
                 // end the style
                 end("Style");
 
                 // return true to specify that the feature has a style
                 return true;
             } else {
                 // dont encode
                 return false;
             }
 
         }
 
         /**
          * Encodes an IconStyle for a feature.
          */
         protected void encodeDefaultIconStyle(SimpleFeature feature) {
             // encode the style for the icon
 
             // figure out if line or polygon
             boolean line = feature.getDefaultGeometry() != null
                     && (feature.getDefaultGeometry() instanceof LineString || feature
                             .getDefaultGeometry() instanceof MultiLineString);
             boolean poly = feature.getDefaultGeometry() != null
                     && (feature.getDefaultGeometry() instanceof Polygon || feature
                             .getDefaultGeometry() instanceof MultiPolygon);
 
             // Final pre-flight check
             if (!line && !poly) {
                 LOGGER.log(
                         Level.FINER,
                         "Unexpectedly entered encodeDefaultIconStyle() "
                         + "with something that does not have a multipoint geometry.");
                 return;
             }
 
             // start IconStyle
             start("IconStyle");
 
             // make transparent if they didn't ask for attributes
             if (!mapContext.getRequest().getKMattr()) {
                 encodeColor("00ffffff");
             }
 
             // if line or polygon scale the label
             if (line || poly) {
                 element("scale", "0.4");
             }
 
             // start Icon
             start("Icon");
 
             // Note the version number in case we want to replace the icon
             String imageURL = "http://icons.opengeo.org/markers/icon-"
                     + (poly ? "poly.1" : "line.1") + ".png";
             element("href", imageURL);
 
             end("Icon");
 
             // end IconStyle
             end("IconStyle");
 
         }
         
         
         /**
          * Encodes a transparent KML LabelStyle
          */
         protected void encodeDefaultTextStyle() {
             start("LabelStyle");
             encodeColor("00ffffff");
             end("LabelStyle");
         }
 
         /**
          * Encodes the provided set of symbolizers as KML styles.
          */
         protected void encodeStyle(SimpleFeature feature,
                 Symbolizer[] symbolizers) {
             try {
                 /**
                  * This causes some performance overhead, but we should separate
                  * out repeated styles anyway...
                  * 
                  * In order of appearance, according to KML specs
                  */
                 LinkedList<PointSymbolizer> iconStyles = new LinkedList<PointSymbolizer>();
                 LinkedList<TextSymbolizer> labelStyles = new LinkedList<TextSymbolizer>();
                 LinkedList<LineSymbolizer> lineStyles = new LinkedList<LineSymbolizer>();
                 LinkedList<PolygonSymbolizer> polyStyles = new LinkedList<PolygonSymbolizer>();
                 // * Not used: <kml:BalloonStyle>
                 // * Not used: <kml:ListStyle>
 
                 for (int i = 0; i < symbolizers.length; i++) {
                     Symbolizer sym = symbolizers[i];
                     if (sym instanceof PointSymbolizer) {
                         iconStyles.add((PointSymbolizer) sym);
                     } else if (sym instanceof TextSymbolizer) {
                         labelStyles.add((TextSymbolizer) sym);
                     } else if (sym instanceof LineSymbolizer) {
                         lineStyles.add((LineSymbolizer) sym);
                     } else if (sym instanceof PolygonSymbolizer) {
                         polyStyles.add((PolygonSymbolizer) sym);
                     }
                     LOGGER.finer(new StringBuffer("Adding symbolizer ").append(
                             sym).toString());
                 }
 
                 // Points / Icons
                 if (iconStyles.isEmpty()) {
                     // Add a default point symbolizer, so people have something
                     // to click on
                     encodeDefaultIconStyle(feature);
                 } else {
                     Iterator<PointSymbolizer> iter = iconStyles.iterator();
                     while (iter.hasNext()) {
                         PointSymbolizer sym = (PointSymbolizer) iter.next();
                         try {
                             Style2D style = styleFactory.createStyle(feature,
                                     sym, scaleRange);
                             encodePointStyle(feature, style, sym);
                         } catch (IllegalArgumentException iae) {
                             LOGGER.fine(iae.getMessage() + " for "
                                     + sym.toString());
                         }
                     }
                 }
 
                 // Labels / Text
                 if (labelStyles.isEmpty()) {
                     encodeDefaultTextStyle();
                 } else {
                     Iterator<TextSymbolizer> iter = labelStyles.iterator();
                     while (iter.hasNext()) {
                         TextSymbolizer sym = (TextSymbolizer) iter.next();
                         try {
                             TextStyle2D style = (TextStyle2D) styleFactory
                                 .createStyle(feature, sym, scaleRange);
                             encodeTextStyle(feature, style, sym);
                         } catch (IllegalArgumentException iae) {
                             LOGGER.fine(iae.getMessage() + " for "
                                     + sym.toString());
                         }
                     }
                 }
 
                 // Lines
                 if (!lineStyles.isEmpty()) {
                     Iterator<LineSymbolizer> iter = lineStyles.iterator();
                     while (iter.hasNext()) {
                         LineSymbolizer sym = (LineSymbolizer) iter.next();
                         try {
                             LineStyle2D style = (LineStyle2D) styleFactory
                                     .createStyle(feature, sym, scaleRange);
                             encodeLineStyle(feature, style, sym);
                         } catch (IllegalArgumentException iae) {
                             LOGGER.fine(iae.getMessage() + " for "
                                     + sym.toString());
                         }
                     }
                 }
 
                 // Polygons
                 if (!polyStyles.isEmpty()) {
                     Iterator<PolygonSymbolizer> iter = polyStyles.iterator();
                     while (iter.hasNext()) {
                         PolygonSymbolizer sym = (PolygonSymbolizer) iter.next();
                         try {
                             PolygonStyle2D style = (PolygonStyle2D) styleFactory
                                     .createStyle(feature, sym, scaleRange);
                             // The last argument is forced outline
                             encodePolygonStyle(feature, style, sym, !lineStyles
                                     .isEmpty());
                         } catch (IllegalArgumentException iae) {
                             LOGGER.fine(iae.getMessage() + " for "
                                     + sym.toString());
                         }
                     }
                 }
 
             } catch (Exception e) {
                 LOGGER.log(Level.WARNING,
                         "Error occurred during style encoding", e);
             }
         }
 
         /**
          * Encodes a KML IconStyle + PolyStyle from a polygon style and
          * symbolizer.
          */
         protected void encodePolygonStyle(SimpleFeature feature, PolygonStyle2D style,
                 PolygonSymbolizer symbolizer, boolean forceOutline) {
             // star the polygon style
             start("PolyStyle");
 
             // fill
             if (symbolizer.getFill() != null) {
                 // get opacity
                 double opacity = ((Number)symbolizer.getFill().getOpacity().evaluate(feature)).doubleValue();
 
                 if (Double.isNaN(opacity)) {
                     // none specified, default to full opacity
                     opacity = 1.0;
                 }
 
                encodeColor(
                        (Color)symbolizer.getFill().getColor().evaluate(feature, Color.class),
                        opacity
                        );
             } else {
                 // make it transparent
                 encodeColor("00aaaaaa");
             }
 
             // outline
             if (symbolizer.getStroke() != null || forceOutline) {
                 element("outline", "1");
             } else {
                 element("outline", "0");
             }
 
             end("PolyStyle");
 
             // if stroke specified add line style as well
             if (symbolizer.getStroke() != null) {
                 start("LineStyle");
 
                 // opacity
                 double opacity = 
                     ((Number)symbolizer.getStroke().getOpacity().evaluate(feature)).doubleValue();
 
                 if (Double.isNaN(opacity)) {
                     // none specified, default to full opacity
                     opacity = 1.0;
                 }
 
                 if (style != null) {
                     encodeColor(
                             KMLUtils.colorToHex(
                                 (Color)
                                 symbolizer.getStroke().getColor().evaluate(feature, Color.class),
                                 opacity
                                 )
                             );
                 }
 
                 // width
                 int width = (Integer)symbolizer.getStroke().getWidth().evaluate(feature);
 
                 if (width != SLD.NOTFOUND) {
                     element("width", Integer.toString(width));
                 }
 
                 end("LineStyle");
             }
         }
 
         /**
          * Encodes a KML IconStyle + LineStyle from a polygon style and
          * symbolizer.
          */
         protected void encodeLineStyle(SimpleFeature feature, LineStyle2D style,
                 LineSymbolizer symbolizer) {
             start("LineStyle");
 
             // stroke
             if (symbolizer.getStroke() != null) {
                 // opacity
                 double opacity = ((Number)symbolizer.getStroke().getOpacity().evaluate(feature)).doubleValue();
 
                 if (Double.isNaN(opacity)) {
                     // default to full opacity
                     opacity = 1.0;
                 }
 
                 if (symbolizer.getStroke().getColor() != null) {
                     encodeColor((Color)symbolizer.getStroke().getColor().evaluate(feature, Color.class), opacity);
                 } else if (style != null) {
                     encodeColor((Color) style.getContour(), opacity);
                 }
 
                 // width
                 int width = SLD.width(symbolizer.getStroke());
 
                 if (width != SLD.NOTFOUND) {
                     element("width", Integer.toString(width));
                 }
             } else {
                 // default
                 encodeColor("ffaaaaaa");
                 element("width", "1");
             }
 
             end("LineStyle");
         }
 
         /**
          * Encodes a KML IconStyle from a point style and symbolizer.
          */
         protected void encodePointStyle(SimpleFeature feature, Style2D style,
                 PointSymbolizer symbolizer) {
             start("IconStyle");
 
             if (style instanceof MarkStyle2D) {
                 Mark mark = SLD.mark(symbolizer);
 
                 if (mark != null) {
                     double opacity = ((Number)mark.getFill().getOpacity().evaluate(feature)).doubleValue();
 
                     if (Double.isNaN(opacity)) {
                         // default to full opacity
                         opacity = 1.0;
                     }
 
                     if (mark.getFill() != null) {
                         final Color color = (Color)mark.getFill().getColor().evaluate(feature, Color.class);
                         encodeColor(color, opacity);
                     }
                 }
             }
 
             element("colorMode", "normal");
 
             // placemark icon
             String iconHref = null;
 
             // if the point symbolizer uses an external graphic use it
             if ((symbolizer.getGraphic() != null)
                     && (symbolizer.getGraphic().getExternalGraphics() != null)
                     && (symbolizer.getGraphic().getExternalGraphics().length > 0)) {
                 ExternalGraphic graphic = symbolizer.getGraphic()
                         .getExternalGraphics()[0];
 
                 try {
                     if ("file".equals(graphic.getLocation().getProtocol())) {
                         // it is a local file, reference locally from "styles"
                         // directory
                         File file = new File(graphic.getLocation().getFile());
                         iconHref = RequestUtils.baseURL(mapContext.getRequest()
                                 .getHttpServletRequest())
                                 + "styles/" + file.getName();
                     } else if ("http".equals(graphic.getLocation()
                             .getProtocol())) {
                         iconHref = graphic.getLocation().toString();
                     } else {
                         // TODO: should we check for http:// and use it
                         // directly?
                         // other protocols?
                     }
 
                 } catch (Exception e) {
                     LOGGER.log(Level.WARNING,
                             "Error processing external graphic:" + graphic, e);
                 }
             }
 
             if (iconHref == null) {
                 iconHref = "http://maps.google.com/mapfiles/kml/pal4/icon25.png";
             }
 
             start("Icon");
 
             element("href", iconHref);
             end("Icon");
 
             end("IconStyle");
         }
 
         /**
          * Encodes a KML LabelStyle from a text style and symbolizer.
          */
         protected void encodeTextStyle(SimpleFeature feature, TextStyle2D style,
                 TextSymbolizer symbolizer) {
             start("LabelStyle");
 
             if (symbolizer.getFill() != null) {
                 double opacity = ((Number)symbolizer.getFill().getOpacity().evaluate(feature)).doubleValue();
 
                 if (Double.isNaN(opacity)) {
                     // default to full opacity
                     opacity = 1.0;
                 }
 
                encodeColor((Color)symbolizer.getFill().getColor().evaluate(feature, Color.class), opacity);
             } else {
                 // default
                 encodeColor("ffffffff");
             }
 
             end("LabelStyle");
         }
 
         /**
          * Encodes a color element from its color + opacity representation.
          * 
          * @param color
          *            The color to encode.
          * @param opacity
          *            The opacity ( alpha ) of the color.
          */
         void encodeColor(Color color, double opacity) {
             encodeColor(KMLUtils.colorToHex(color, opacity));
         }
 
         /**
          * Encodes a color element from its hex representation.
          * 
          * @param hex
          *            The hex value ( with alpha ) of the color.
          * 
          */
         void encodeColor(String hex) {
             element("color", hex);
         }
 
         /**
          * Returns the centroid of the geometry, handling a geometry collection.
          * <p>
          * In the case of a collection a multi point containing the centroid of
          * each geometry in the collection is calculated. The first point in the
          * multi point is returned as the cetnroid.
          * </p>
          */
         Coordinate geometryCentroid(Geometry g) {
             // TODO: should the collection case return the centroid of the
             // multi point?
             if (g instanceof GeometryCollection) {
                 GeometryCollection gc = (GeometryCollection) g;
 
                 // check for case of single geometry
                 if (gc.getNumGeometries() == 1) {
                     g = gc.getGeometryN(0);
                 } else {
                     double maxAreaSoFar = gc.getGeometryN(0).getArea();
                     Coordinate centroidToReturn = gc.getGeometryN(0).getCentroid().getCoordinate();
 
                     for (int t = 0; t < gc.getNumGeometries(); t++) {
                         double area = gc.getGeometryN(t).getArea();
                         if (area > maxAreaSoFar) {
                             maxAreaSoFar = area;
                             centroidToReturn = gc.getGeometryN(t).getCentroid().getCoordinate();
                         }
                     }
 
                     return centroidToReturn;
                 }
             }
 
             if (g instanceof Point) {
                 // thats easy
                 return g.getCoordinate();
             } else if (g instanceof LineString) {
                 // make sure the point we return is actually on the line
                 double tol = 1E-6;
                 double mid = g.getLength() / 2d;
 
                 Coordinate[] coords = g.getCoordinates();
 
                 // walk along the linestring until we get to a point where we
                 // have two coordinates that straddle the midpoint
                 double len = 0d;
                 for (int i = 1; i < coords.length; i++) {
                     LineSegment line = new LineSegment(coords[i - 1], coords[i]);
                     len += line.getLength();
 
                     if (Math.abs(len - mid) < tol) {
                         // close enough
                         return line.getCoordinate(1);
                     }
 
                     if (len > mid) {
                         // we have gone past midpoint
                         return line.pointAlong(1 - ((len - mid) / line
                                 .getLength()));
                     }
                 }
 
                 // should never get there
                 return g.getCentroid().getCoordinate();
             } else {
                 // return the actual centroid
                 return g.getCentroid().getCoordinate();
             }
         }
 
         protected void encodePlacemark(SimpleFeature feature,
                 FeatureTypeStyle[] styles) {
             encodePlacemark(feature, styles, null);
         }
 
         /**
          * Encodes a KML Placemark from a feature and optional name.
          */
         protected void encodePlacemark(SimpleFeature feature,
                 FeatureTypeStyle[] styles, Geometry markGeometry) {
             Geometry geometry = featureGeometry(feature);
             Coordinate centroid = geometryCentroid(geometry);
 
             start("Placemark", KMLUtils.attributes(new String[] { "id",
                     feature.getID() }));
 
             // encode name + description only if kmattr was specified
             if (mapContext.getRequest().getKMattr()) {
                 // name
                 try {
                     encodePlacemarkName(feature, styles);
                 } catch (Exception e) {
                     String msg = "Error occured processing 'title' template.";
                     LOGGER.log(Level.WARNING, msg, e);
                 }
 
                 // snippet (only used by OWS5 prototype at the moment)
                 try {
                     encodePlacemarkSnippet(feature, styles);
                 } catch (Exception e) {
                     String msg = "Error occured processing 'description' template.";
                     LOGGER.log(Level.WARNING, msg, e);
                 }
 
                 // description
                 try {
                     encodePlacemarkDescription(feature, styles);
                 } catch (Exception e) {
                     String msg = "Error occured processing 'description' template.";
                     LOGGER.log(Level.WARNING, msg, e);
                 }
             }
 
             String selfLinks = (String) mapContext.getRequest()
                     .getFormatOptions().get("selfLinks");
             if (selfLinks != null && selfLinks.equalsIgnoreCase("true")) {
                 GetMapRequest request = mapContext.getRequest();
                 String link = "";
 
                 try {
                     link = getFeatureTypeURL();
                 } catch (IOException ioe) {
                     /* what could *possibly* go wrong? */
                     throw new RuntimeException(ioe);
                 }
                 String[] id = feature.getID().split("\\.");
 
                 link = link + "/" + id[1] + ".kml";
 
                 element("atom:link", null, KMLUtils.attributes(new String[] {
                         "rel", "self", "href", link }));
             }
 
             // look at
             encodePlacemarkLookAt(centroid);
 
             // time
             try {
                 encodePlacemarkTime(feature, styles);
             } catch (Exception e) {
                 String msg = "Error occured processing 'time' template: "
                         + e.getMessage();
                 LOGGER.log(Level.WARNING, msg);
                 LOGGER.log(Level.FINE, "", e);
             }
 
             // style reference
             element("styleUrl", "#GeoServerStyle" + feature.getID());
 
             // encode extended data (kml 2.2)
             encodeExtendedData(feature);
 
             // geometry
             if (markGeometry == null) {
                 encodePlacemarkGeometry(geometry, centroid, styles);
             } else {
                 // if given a specific placemark geometry, encode a point
                 // at the geometry coordinates
                 Coordinate markCentroid = markGeometry.getCoordinate();
                 start("Point");
                 if (!Double.isNaN(markCentroid.z)) {
                     element("coordinates", markCentroid.x + ","
                             + markCentroid.y + "," + markCentroid.z);
                 } else {
                     element("coordinates", markCentroid.x + ","
                             + markCentroid.y);
                 }
                 end("Point");
             }
 
             end("Placemark");
         }
 
         /**
          * Encodes kml 2.2 extended data section
          * 
          * @param feature
          */
         protected void encodeExtendedData(SimpleFeature feature) {
             // code at the moment is in KML3VectorTransfomer
         }
 
         protected String getFeatureTypeURL() throws IOException {
             String nsUri = mapLayer.getFeatureSource().getSchema().getName()
                     .getNamespaceURI();
             NameSpaceInfo ns = mapContext.getRequest().getWMS().getData()
                     .getNameSpaceFromURI(nsUri);
             String featureTypeName = mapLayer.getFeatureSource().getSchema()
                     .getName().getLocalPart();
             GetMapRequest request = mapContext.getRequest();
 
             // TODO The old code, commented out below, results in
             // link =
             // http://localhost:8080/geoserver/rest/geosearch/topp/states.kml?st
             // due to getBaseUrl()
             //
             // String link = RequestUtils.proxifiedBaseURL(request.getBaseUrl(),
             // request.getGeoServer().getProxyBaseUrl());
             //
 
             // If you prefer pretty code, this is a good point to close your
             // eyes:
             String baseUrl = request.getHttpServletRequest().getRequestURL()
                     .toString();
             int searchIdx = baseUrl.indexOf("rest/geosearch");
             if (searchIdx < 0) {
                 // LOGGER.log(Level.WARNING, "Unable to find rest/geosearch in
                 // URL " + baseUrl);
             } else {
                 baseUrl = baseUrl.substring(0, searchIdx);
             }
             baseUrl = RequestUtils.proxifiedBaseURL(baseUrl, request
                     .getGeoServer().getProxyBaseUrl());
 
             return baseUrl + "rest/geosearch/" + ns.getPrefix() + "/"
                     + featureTypeName;
         }
 
         /**
          * Encodes a KML TimePrimitive geometry from a feature.
          */
         protected void encodePlacemarkTime(SimpleFeature feature,
                 FeatureTypeStyle[] styles) throws IOException {
             try {
                 String[] time = new FeatureTimeTemplate(template)
                         .execute(feature);
                 if (time.length == 0) {
                     return;
                 }
 
                 if (time.length == 1) {
                     encodeKmlTimeStamp(parseDateTime(time[0]));
                 } else {
                     encodeKmlTimeSpan(parseDateTime(time[0]),
                             parseDateTime(time[1]));
                 }
             } catch (Exception e) {
                 throw (IOException) new IOException().initCause(e);
             }
         }
 
         /**
          * Encodes the time pairs into a kml TimeSpan (from and to will be
          * parsed into the official kml date/time representation)
          * 
          * @param from
          * @param to
          * @throws Exception
          */
         protected void encodeKmlTimeSpan(Date from, Date to) throws Exception {
             // timespan case
             String begin = encodeDateTime(from);
             String end = encodeDateTime(to);
 
             if (!(begin == null && end == null)) {
                 start("TimeSpan");
                 if (begin != null) {
                     element("begin", begin);
                 }
                 if (end != null) {
                     element("end", end);
                 }
                 end("TimeSpan");
             }
         }
 
         /**
          * Encodes a kml Timestamp element with provided time (which will be
          * parsed into the standard kml representation)
          * 
          * @param time
          * @throws Exception
          */
         protected void encodeKmlTimeStamp(Date time) throws Exception {
             String datetime = encodeDateTime(time);
             if (datetime != null) {
                 // timestamp case
                 start("TimeStamp");
                 element("when", datetime);
                 end("TimeStamp");
             }
         }
 
         protected String encodeDateTime(Date date) {
             if (date != null) {
                 Calendar c = Calendar.getInstance();
                 c.setTime(date);
                 return new XSDateTimeBinding().encode(c, null);
             } else {
                 return null;
             }
         }
 
         /**
          * Encodes a date as an xs:dateTime.
          */
         protected Date parseDateTime(String date) throws Exception {
 
             // first try as date time
             Date d = parseDate(dtformats, date);
             if (d == null) {
                 // then try as date
                 d = parseDate(dformats, date);
             }
             if (d == null) {
                 // try as time
                 d = parseDate(tformats, date);
             }
 
             if (d == null) {
                 // last ditch effort, try to parse as xml dates
                 try {
                     // try as xml date time
                     d = DateUtil.deserializeDateTime(date);
                 } catch (Exception e1) {
                     try {
                         // try as xml date
                         d = DateUtil.deserializeDate(date);
                     } catch (Exception e2) {
                     }
                 }
             }
 
             if (d != null) {
                 return d;
             }
 
             LOGGER.warning("Could not parse date: " + date);
             return null;
         }
 
         /**
          * Parses a date as a string into a well-known format.
          */
         protected Date parseDate(List formats, String date) {
             for (Iterator f = formats.iterator(); f.hasNext();) {
                 SimpleDateFormat format = (SimpleDateFormat) f.next();
                 Date d = null;
                 try {
                     d = format.parse(date);
                 } catch (ParseException e) {
                 }
 
                 if (d != null) {
                     return d;
                 }
             }
 
             return null;
         }
 
         /**
          * Encodes a KML Placemark geometry from a geometry + centroid.
          * 
          * @param styles
          */
         protected void encodePlacemarkGeometry(Geometry geometry,
                 Coordinate centroid, FeatureTypeStyle[] styles) {
             // if point, just encode a single point, otherwise encode the
             // geometry + centroid
             if (geometry instanceof Point || (geometry instanceof MultiPoint)
                     && ((MultiPoint) geometry).getNumPoints() == 1) {
                 
                 // This adds attributes that violate the KML specs
                 // encodeGeometry( geometry, styles );
 
                 // Cut and paste from below
                 start("Point");
                 if (!Double.isNaN(centroid.z)) {
                     geometryTranslator.insertExtrudeTags(geometry);
                     element("coordinates", centroid.x + "," + centroid.y + ","
                             + centroid.z);
                 } else {
                     element("coordinates", centroid.x + "," + centroid.y);
                 }
                 end("Point");
                 // End cut and paste
             } else {
                 start("MultiGeometry");
 
                 if (!Double.isNaN(geometry.getCoordinate().z)) {
                     centroid.z = geometry.getCoordinate().z;
                 }
 
                 // the centroid
                 start("Point");
 
                 if (!Double.isNaN(centroid.z)) {
                     geometryTranslator.insertExtrudeTags(geometry);
                     element("coordinates", centroid.x + "," + centroid.y + ","
                             + centroid.z);
                 } else {
                     element("coordinates", centroid.x + "," + centroid.y);
                 }
 
                 end("Point");
 
                 // the actual geometry
                 encodeGeometry(geometry, styles);
 
                 end("MultiGeometry");
             }
 
         }
 
         /**
          * Encodes a KML geometry.
          * 
          * @param styles
          */
         protected void encodeGeometry(Geometry geometry,
                 FeatureTypeStyle[] styles) {
             if (geometry instanceof GeometryCollection) {
                 // unwrap the collection
                 GeometryCollection collection = (GeometryCollection) geometry;
 
                 for (int i = 0; i < collection.getNumGeometries(); i++) {
                     encodeGeometry(collection.getGeometryN(i), styles);
                 }
             } else {
                 geometryTranslator.encode(geometry);
             }
         }
 
         /**
          * Returns the id of the feature removing special characters like
          * '&','>','<','%'.
          */
         String featureId(SimpleFeature feature) {
             String id = feature.getID();
             id = id.replaceAll("&", "");
             id = id.replaceAll(">", "");
             id = id.replaceAll("<", "");
             id = id.replaceAll("%", "");
 
             return id;
         }
 
         /**
          * Returns the geometry for the feature reprojecting if necessary.
          */
         Geometry featureGeometry(SimpleFeature f) {
             // get the geometry
             Geometry geom = (Geometry) f.getDefaultGeometry();
             try{
                 final double height = new FeatureHeightTemplate(template).execute(f);
 
                 if (!Double.isNaN(height) && height != 0){
                     geom.apply(
                         new CoordinateFilter(){
                             public void filter(Coordinate c){
                                 c.setCoordinate(new Coordinate(c.x, c.y, height));
                             }
                         }
                     );
                     geom.geometryChanged();
                 }
             } catch (IOException ioe){
                 LOGGER.log(Level.WARNING, "Couldn't render height template for " + f.getID(), ioe);
             }
 
             // rprojection done in KMLTransformer
 //          if (!CRS.equalsIgnoreMetadata(sourceCrs, mapContext.getCoordinateReferenceSystem())) {
 //              try {
 //                  MathTransform transform = CRS.findMathTransform(sourceCrs,
 //                          mapContext.getCoordinateReferenceSystem(), true);
 //                  geom = JTS.transform(geom, transform);
 //              } catch (MismatchedDimensionException e) {
 //                  LOGGER.severe(e.getLocalizedMessage());
 //              } catch (TransformException e) {
 //                  LOGGER.severe(e.getLocalizedMessage());
 //              } catch (FactoryException e) {
 //                  LOGGER.severe(e.getLocalizedMessage());
 //              }
 //          }
 
             return geom;
         }
     }
 }
