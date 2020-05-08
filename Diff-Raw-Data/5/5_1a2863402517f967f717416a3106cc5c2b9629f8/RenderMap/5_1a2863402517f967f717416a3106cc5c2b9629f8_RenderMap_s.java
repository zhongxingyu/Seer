 /*
     Open Aviation Map
     Copyright (C) 2012-2013 Ákos Maróy
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as
     published by the Free Software Foundation, either version 3 of the
     License, or (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package hu.tyrell.openaviationmap.rendering;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 import hu.tyrell.openaviationmap.rendering.grid.Lines;
 import hu.tyrell.openaviationmap.rendering.grid.ortholine.LineOrientation;
 import hu.tyrell.openaviationmap.rendering.grid.ortholine.OrthoLineDef;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.SampleModel;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 
 import javax.media.jai.JAI;
 import javax.media.jai.PlanarImage;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.Query;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.geometry.DirectPosition2D;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.jdbc.JDBCDataStore;
 import org.geotools.map.FeatureLayer;
 import org.geotools.map.MapContent;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.GeodeticCalculator;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.renderer.GTRenderer;
 import org.geotools.renderer.lite.RendererUtilities;
 import org.geotools.renderer.lite.StreamingRenderer;
 import org.geotools.styling.DefaultResourceLocator;
 import org.geotools.styling.SLDParser;
 import org.geotools.styling.Style;
 import org.jaitools.tiledimage.DiskMemImage;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.cs.AxisDirection;
 import org.opengis.referencing.operation.CoordinateOperation;
 import org.opengis.referencing.operation.CoordinateOperationFactory;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.OperationNotFoundException;
 import org.opengis.referencing.operation.TransformException;
 import org.xml.sax.SAXException;
 
 import com.vividsolutions.jts.geom.Coordinate;
 
 /**
  * Command line utility to render a map into a bitmap, possibly for printing.
  */
 public final class RenderMap {
 
     /** The default DPI value. */
     public static final double DEFAULT_DPI = 300;
 
     /** The default low zoom level for tilesets. */
     public static final int DEFAULT_LOW_LEVEL = 0;
 
     /** The default high zoom level for tilesets. */
     public static final int DEFAULT_HIGH_LEVEL = 11;
 
     /** The tile size, in pixels. */
     public static final int TILE_SIZE = 256;
 
     /** The metatile size, in tiles. */
     public static final int METATILE_SIZE = 32;
 
     /** Float formatter. */
     private static final DecimalFormat FLOAT_FORMAT =
                                                 new DecimalFormat("###.##");
 
     /** Constants marking alignment. */
     private enum Alignment {
         /** Constant marking center alignment. */
         CENTER,
 
         /** Constant marking a left alignment. */
         LEFT,
 
         /** Constant marking a right alignment. */
         RIGHT
     }
 
     /** Constants marking a rotation. */
     private enum Rotation {
         /** Constant marking no rotation. */
         NONE,
 
         /** Constant marking a 90 degree clockwise rotation. */
         CW,
 
         /** Constant marking a 90 degree counter-clockwise rotation. */
         CCW
     }
 
     /**
      * The grid definition.
      *
      * major lines at 30' degree spacing are indicated by level = 2
      * minor lines at 10' degree spacing are indicated by level = 1
      * (level values are arbitrary; only rank order matters)
      */
     private static final List<OrthoLineDef> GRID_DEF = Arrays.asList(
             // vertical (longitude) lines
             new OrthoLineDef(LineOrientation.VERTICAL, 2, 1.0 / 2.0,
                                                        1 / 60.0, 1 / 60.0),
             new OrthoLineDef(LineOrientation.VERTICAL, 1, 1.0 / 6.0),
 
             // horizontal (latitude) lines
             new OrthoLineDef(LineOrientation.HORIZONTAL, 2, 1.0 / 2.0,
                                                        1 / 60.0, 1 / 120.0),
             new OrthoLineDef(LineOrientation.HORIZONTAL, 1, 1.0 / 6.0));
 
 
     /**
      * Private constructor.
      */
     private RenderMap() {
     }
 
     /**
      * Print a help message.
      */
     private static void printHelpMessage() {
         System.out.println(
         "Open Aviation Map SLD scaling utility");
         System.out.println();
         System.out.println(
         "usage:");
         System.out.println();
         System.out.println(
         "  -a | --oam host,db,user,pw   the Open Aviation Map PostGIS");
         System.out.println(
         "                               database connection parameters in a");
         System.out.println(
         "                               comma-separated list");
         System.out.println(
         "  -c | --coverage A,B,C,D      map coverage in degrees, with A,B");
         System.out.println(
         "                               the lower-left, C,D the upper-right");
         System.out.println(
         "                               corner. defaults to the whole map.");
         System.out.println(
         "                               special value of 'Hungary' accepted");
         System.out.println(
         "  -d | --dpi <value>           the target device dpi");
         System.out.println(
         "                               optional, defaults to "
                                       + DEFAULT_DPI);
         System.out.println(
         "  -l | --levels <value>        the zoom levels used for tileset");
         System.out.println(
         "                               creation, e.g 1,8 - defaults to "
                                + DEFAULT_LOW_LEVEL + "," + DEFAULT_HIGH_LEVEL);
         System.out.println(
         "  -m | --osm host,db,user,pw   the Open Street Map PostGIS database");
         System.out.println(
         "                               connection parameters in a");
         System.out.println(
         "                               comma-separated list");
         System.out.println(
         "  -o | --output <output.file>  the output, a file name for single");
         System.out.println(
         "                               files, a directory for tilesets");
         System.out.println(
         "  -r | --crs <value>           the CRS id to use for projection");
         System.out.println(
         "                               defaults to the CRS of the OAM data");
         System.out.println(
         "                               source. a special value of");
         System.out.println(
         "                               'Hungary:Lambert' is also accepted");
         System.out.println(
         "  -s | --scale <value>         the scale to generate the map in");
         System.out.println(
         "                               e.g. 1:<value>");
         System.out.println(
         "  -t | --type <value>          type of rendering, one of TIFF or");
         System.out.println(
         "                               tileset");
         System.out.println(
         "  -u | --sldurl <value>        the base URL where the SLD files are");
         System.out.println(
         "                               located");
         System.out.println(
         "  -h | --help                  show this usage page");
         System.out.println();
     }
 
     /**
      * Program entry point.
      *
      * @param args command line parameters
      * @throws IOException on I/O errors
      * @throws ParserConfigurationException on XML parser errors
      * @throws SAXException on XML parser errors
      * @throws FactoryException on CRS transformation errors
      * @throws TransformException on CRS transformation errors
      */
     public static void main(String[] args)
                                   throws IOException,
                                          SAXException,
                                          ParserConfigurationException,
                                          TransformException,
                                          FactoryException {
 
         LongOpt[] longopts = new LongOpt[11];
 
         longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
         longopts[1] = new LongOpt("oam", LongOpt.REQUIRED_ARGUMENT,
                 null, 'a');
         longopts[2] = new LongOpt("coverage", LongOpt.REQUIRED_ARGUMENT,
                 null, 'c');
         longopts[3] = new LongOpt("dpi", LongOpt.REQUIRED_ARGUMENT,
                 null, 'd');
         longopts[4] = new LongOpt("levels", LongOpt.REQUIRED_ARGUMENT,
                 null, 'l');
         longopts[5] = new LongOpt("osm", LongOpt.REQUIRED_ARGUMENT,
                 null, 'm');
         longopts[6] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
                 null, 'o');
         longopts[7] = new LongOpt("crs", LongOpt.REQUIRED_ARGUMENT,
                 null, 'r');
         longopts[8] = new LongOpt("scale", LongOpt.REQUIRED_ARGUMENT,
                 null, 's');
         longopts[9] = new LongOpt("type", LongOpt.REQUIRED_ARGUMENT,
                 null, 't');
         longopts[10] = new LongOpt("sldurl", LongOpt.REQUIRED_ARGUMENT,
                 null, 'u');
 
         Getopt g = new Getopt("RenderMap", args, "a:c:d:hl:m:o:r:s:t:u:",
                               longopts);
 
         int c;
 
         String      oamStr      = null;
         String      osmStr      = null;
         String      outputPath  = null;
         String      strScale    = null;
         String      strDpi      = null;
         String      sldUrlStr   = null;
         String      coverageStr = null;
         String      crsStr      = null;
         String      typeStr     = "TIFF";
         String      levelsStr   = null;
 
         while ((c = g.getopt()) != -1) {
             switch (c) {
             case 'a':
                 oamStr = g.getOptarg();
                 break;
 
             case 'c':
                 coverageStr = g.getOptarg();
                 break;
 
             case 'd':
                 strDpi = g.getOptarg();
                 break;
 
             case 'l':
                 levelsStr = g.getOptarg();
                 break;
 
             case 'm':
                 osmStr = g.getOptarg();
                 break;
 
             case 'o':
                 outputPath = g.getOptarg();
                 break;
 
             case 'r':
                 crsStr = g.getOptarg();
                 break;
 
             case 's':
                 strScale = g.getOptarg();
                 break;
 
             case 't':
                 typeStr = g.getOptarg();
                 break;
 
             case 'u':
                 sldUrlStr = g.getOptarg();
                 break;
 
             default:
             case 'h':
                 printHelpMessage();
                 return;
 
             case '?':
                 System.out.println("Invalid option '" + g.getOptopt()
                                    + "' specified");
                 return;
             }
         }
 
         if (oamStr == null) {
             System.out.println("Required option oam not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (osmStr == null) {
             System.out.println("Required option osm not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (outputPath == null) {
             System.out.println("Required option output not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if ("TIFF".equals(typeStr.toUpperCase()) && strScale == null) {
             System.out.println("Required option scale not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (sldUrlStr == null) {
             System.out.println("Required option sldurl not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
 
         // normalize the base URL
         if (!sldUrlStr.endsWith("/")) {
             sldUrlStr = sldUrlStr + "/";
         }
 
         double scale = Double.NaN;
         if (strScale != null) {
             try {
                 scale = Double.parseDouble(strScale);
             } catch (Exception e) {
                 System.out.println("Error parsing scale value.");
                 System.out.println();
                 e.printStackTrace(System.out);
                 return;
             }
         }
 
         // parse the DPI value, if supplied
         double dpi = DEFAULT_DPI;
         if (strDpi != null) {
             try {
                 dpi = Double.parseDouble(strDpi);
             } catch (Exception e) {
                 System.out.println("Error parsing dpi value.");
                 System.out.println();
                 e.printStackTrace(System.out);
                 return;
             }
         }
 
         // parse the levels value
         int lowLevel  = DEFAULT_LOW_LEVEL;
         int highLevel = DEFAULT_HIGH_LEVEL;
         if (levelsStr != null) {
             StringTokenizer tok = new StringTokenizer(levelsStr, ",");
             if (tok.countTokens() != 2) {
                 System.out.println("invalid levels option specified: "
                                  + levelsStr);
                 return;
             }
 
             try {
                 lowLevel = Integer.parseInt(tok.nextToken());
                 highLevel = Integer.parseInt(tok.nextToken());
             } catch (Exception e) {
                 System.out.println("Error parsing level value.");
                 System.out.println();
                 e.printStackTrace(System.out);
                 return;
             }
         }
 
         CoordinateReferenceSystem crs = null;
         if (crsStr != null) {
             if ("hungary:lambert".equals(crsStr.toLowerCase())) {
                 crs = getHungarianLambertProjection();
             } else {
                 crs = CRS.decode(crsStr);
             }
         }
 
         ReferencedEnvelope coverage = null;
         if (coverageStr != null) {
             if ("hungary".equals(coverageStr.toLowerCase())) {
                 coverageStr = "16,45.5,23,48.75";
             }
             coverage = parseCoverage(coverageStr);
         }
 
         Map<String, Object> osmParams = parseDbParams(osmStr);
         Map<String, Object> oamParams = parseDbParams(oamStr);
 
         if ("TIFF".equals(typeStr.toUpperCase())) {
 
             System.out.println("Rendering map at scale 1:" + ((int) scale)
                              + " at " + ((int) dpi) + " dpi to " + outputPath);
 
             renderMapToFile(osmParams, oamParams, coverage, crs, sldUrlStr,
                             scale, dpi, true, true, outputPath);
 
         } else if ("tileset".equals(typeStr.toLowerCase())) {
 
             System.out.println("Rendering map into tileset levels "
                     + lowLevel + "..." + highLevel + " to " + outputPath);
 
             renderMapTileset(osmParams, oamParams, coverage, crs, sldUrlStr,
                              dpi, lowLevel, highLevel, outputPath);
         }
     }
 
     /**
      * Render a map into a file.
      *
      * @param osmParams the parameters to create the Open Street Map DataStore
      * @param oamParams the parameters to create the Open Aviaton Map DataStore
      * @param coverage the coverage of the rendered map, if null, the whole
      *        map area covered by the datastores is used
      * @param crs the CRS to use for projection. if null, the CRS of the OAM
      *        data source is used
      * @param sldUrlStr the base URL for SLD files &amp; related resources.
      * @param scale the scale, that is, 1:scale will be used
      * @param dpi the number of dots per inch on the target image
      * @param drawLattice specify true if a lattice is to be drawn onto the map
      * @param drawLegend specify true of labels should be drawn around the map
      * @param outputFile the name of the output TIFF file to create
      * @throws IOException on I/O errors
      * @throws FactoryException on CRS transformation errors
      * @throws TransformException on CRS transformation errors
      */
     private static void
     renderMapToFile(Map<String, Object>           osmParams,
                     Map<String, Object>           oamParams,
                     ReferencedEnvelope            coverage,
                     CoordinateReferenceSystem     crs,
                     String                        sldUrlStr,
                     double                        scale,
                     double                        dpi,
                     boolean                       drawLattice,
                     boolean                       drawLegend,
                     String                        outputFile)
                                                    throws IOException,
                                                           TransformException,
                                                           FactoryException {
 
         // create the data stores
         DataStore osmDataStore = DataStoreFinder.getDataStore(osmParams);
         if (osmDataStore == null) {
             System.out.println(
                     "can't connect to the Open Street Map database");
             return;
         }
 
         DataStore oamDataStore = DataStoreFinder.getDataStore(oamParams);
         if (oamDataStore == null) {
             System.out.println(
                     "can't connect the to Open Aviation Map database");
             return;
         }
 
         // set logging levels for the geotools APIs
         ((JDBCDataStore) oamDataStore).getLogger().setLevel(Level.SEVERE);
         ((JDBCDataStore) osmDataStore).getLogger().setLevel(Level.SEVERE);
         org.geotools.util.logging.Logging.
           getLogger("org.geotools.referencing.factory").setLevel(Level.SEVERE);
 
 
         // create the parser with the sld configuration
         final URL sldUrl = new URL(sldUrlStr);
         SLDParser sldParser = new SLDParser(
                                     CommonFactoryFinder.getStyleFactory(null));
         DefaultResourceLocator rl = new DefaultResourceLocator();
         rl.setSourceUrl(sldUrl);
         sldParser.setOnLineResourceLocator(rl);
 
         MapContent osmMap = openOSM(crs, scale, dpi, osmDataStore, sldUrl,
                 sldParser);
 
         MapContent oamMap = openOAM(crs, scale, dpi, oamDataStore, sldUrl,
                 sldParser);
 
         CoordinateReferenceSystem refCrs = crs == null
                                     ? oamMap.getCoordinateReferenceSystem()
                                     : crs;
 
         // calculate map coverage and image size
         ReferencedEnvelope mapBounds = coverage == null
                                      ? calcCoverage(osmMap, oamMap, refCrs)
                                      : transformCoverage(coverage, refCrs);
 
         if (drawLattice) {
             addLatticeLayer(mapBounds, sldParser, sldUrl, scale, dpi, refCrs,
                             oamMap);
         }
 
         ReferencedEnvelope mapBoundsWgs84 =
                 mapBounds.transform(DefaultGeographicCRS.WGS84, false);
         normalizeWgs84Bounds(mapBoundsWgs84);
 
         System.out.println("Map coverage: "
                + FLOAT_FORMAT.format(mapBoundsWgs84.getMinX())
                    + "\u00b0," + FLOAT_FORMAT.format(mapBoundsWgs84.getMinY())
                + "\u00b0 x "
                + FLOAT_FORMAT.format(mapBoundsWgs84.getMaxX())
                    + "\u00b0," + FLOAT_FORMAT.format(mapBoundsWgs84.getMaxY())
                    + "\u00b0");
 
         Rectangle imageBounds = calcImageBounds(scale, dpi, mapBoundsWgs84);
 
 
         System.out.println("Image size: " + ((int) imageBounds.getWidth())
                 + "x" + ((int) imageBounds.getHeight()) + " pixels");
 
         renderMap(osmMap, oamMap, scale, dpi,
                   drawLegend, mapBounds, imageBounds, "TIFF", outputFile);
 
         oamMap.dispose();
         osmMap.dispose();
         oamDataStore.dispose();
         osmDataStore.dispose();
     }
 
     /**
      * Calculate a coverage area based on two maps. The result is an area
      * that include the two maps total area.
      *
      * @param osmMap one of the maps to calculate the coverage for
      * @param oamMap the other map to calculate the coverage for
      * @param refCrs the CRS to provide the coverage in
      * @return a coverage that include the area of both supplied maps
      * @throws TransformException on CRS transformation issues
      * @throws FactoryException on CRS factory issues
      */
     private static ReferencedEnvelope
     calcCoverage(MapContent                 osmMap,
                  MapContent                 oamMap,
                  CoordinateReferenceSystem  refCrs)
                                                  throws TransformException,
                                                         FactoryException {
         ReferencedEnvelope mapBounds;
 
         mapBounds = transformCoverage(
                         new ReferencedEnvelope(osmMap.getMaxBounds()), refCrs);
         mapBounds.include(transformCoverage(oamMap.getMaxBounds(), refCrs));
 
         return mapBounds;
     }
 
     /**
      * Transform a coverage into the provided CRS. Only performs a
      * transformation if necessary
      *
      * @param coverage the coverage to transform
      * @param refCrs the CRS to transform into
      * @return the coverage in refCrs
      * @throws TransformException on CRS transformation issues
      * @throws FactoryException on CRS factory issues
      */
     private static ReferencedEnvelope
     transformCoverage(ReferencedEnvelope        coverage,
                       CoordinateReferenceSystem refCrs)
                                                   throws TransformException,
                                                          FactoryException {
         ReferencedEnvelope mapBounds;
 
         if (CRS.equalsIgnoreMetadata(refCrs,
                                     coverage.getCoordinateReferenceSystem())) {
             mapBounds = new ReferencedEnvelope(coverage);
         } else {
             mapBounds = coverage.transform(refCrs, false);
         }
 
         return mapBounds;
     }
 
     /**
      * Render a map.
      *
      * @param osmMap the Open Street Map to render
      * @param oamMap the Open Aviation Map to render
      * @param scale the scale of rendering
      * @param dpi the desired dpi value
      * @param drawLegend specify true if the legend is to be drawn
      * @param mapBounds the bounds of the map to render
      * @param imageBounds the image bounds
      * @param fileType the type of file to generate, e.g. TIFF or PNG, etc.
      * @param outputFile the name of the output file
      * @throws TransformException on CRS transformation errors
      * @throws FactoryException on transformation factory errors
      * @throws IOException on I/O errors
      */
     private static void
     renderMap(MapContent            osmMap,
               MapContent            oamMap,
               double                scale,
               double                dpi,
               boolean               drawLegend,
               ReferencedEnvelope    mapBounds,
               Rectangle             imageBounds,
               String                fileType,
               String                outputFile)
                                           throws TransformException,
                                                  FactoryException,
                                                  IOException {
         // first, generate the ground map
         System.out.println("Rendering ground map...");
         PlanarImage osmImage =
                           renderMap(osmMap, mapBounds, imageBounds, scale, dpi);
 
 
         // second, generate the aviation map
         System.out.println("Rendering aviation map...");
         PlanarImage oamImage =
                           renderMap(oamMap, mapBounds, imageBounds, scale, dpi);
 
 
         // third, combine these together and into outputFile
         System.out.println("Combining ground & aviation map...");
         DiskMemImage image = combineImages(imageBounds, osmImage, oamImage);
         oamImage.dispose();
         osmImage.dispose();
 
         if (drawLegend) {
             // draw the labels around the map
             System.out.println("Drawing legend...");
             image = drawLabels(mapBounds, image, scale, dpi);
         }
 
         // save the map
         JAI.create("filestore", image, outputFile, fileType, null);
         System.out.println("Map saved to " + outputFile);
         image.dispose();
     }
 
     /**
      * Make sure no value of a WGS84 bounding box is outside of valid values.
      *
      * @param mapBoundsWgs84 the box to normalize
      */
     private static void
     normalizeWgs84Bounds(ReferencedEnvelope mapBoundsWgs84) {
         double west = Math.max(mapBoundsWgs84.getMinX(), -180d);
         double east = Math.min(mapBoundsWgs84.getMaxX(), 180d);
         double south = Math.max(mapBoundsWgs84.getMinY(), -90);
         double north = Math.min(mapBoundsWgs84.getMaxY(), 90);
 
         mapBoundsWgs84.setBounds(new ReferencedEnvelope(west, east,
                                 south, north,
                                 mapBoundsWgs84.getCoordinateReferenceSystem()));
     }
 
     /**
      * Open the Open Avation Map.
      *
      * @param crs the CRS to use for projection. if null, the CRS of the OAM
      *        data source is used
      * @param scale the scale, that is, 1:scale will be used
      * @param sldUrl the base URL for SLD files &amp; related resources.
      * @param dpi the number of dots per inch on the target image
      * @param oamDataStore the Open Aviation Map datastore to use
      * @param sldParser the SLD parser to use to process SLD files
      * @return an Open Aviation Map
      * @throws IOException on I/O errors
      * @throws FactoryException on CRS transformation errors
      */
     private static MapContent
     openOAM(CoordinateReferenceSystem   crs,
             double                      scale,
             double                      dpi,
             DataStore                   oamDataStore,
             final URL                   sldUrl,
             SLDParser                   sldParser)
                                                     throws IOException,
                                                            FactoryException {
         System.out.println("Opening Open Aviation Map database...");
 
         MapContent oamMap = new MapContent();
 
         // add the aviation layers
         addLayer(oamDataStore, sldParser, sldUrl, crs,
                 "planet_osm_polygon", "oam_airspaces.sldt", scale, dpi, oamMap);
         addLayer(oamDataStore, sldParser, sldUrl, crs,
                 "planet_osm_point", "oam_navaids.sldt", scale, dpi, oamMap);
         addLayer(oamDataStore, sldParser, sldUrl, crs,
                 "planet_osm_line", "oam_runways.sld", scale, dpi, oamMap);
 
         return oamMap;
     }
 
     /**
      * Open the Open Street Map.
      *
      * @param crs the CRS to use for projection. if null, the CRS of the OAM
      *        data source is used
      * @param scale the scale, that is, 1:scale will be used
      * @param sldUrl the base URL for SLD files &amp; related resources.
      * @param dpi the number of dots per inch on the target image
      * @param osmDataStore the Open Street Map datastore to use
      * @param sldParser the SLD parser to use to process SLD files
      * @return an Open Aviation Map
      * @throws IOException on I/O errors
      * @throws FactoryException on CRS transformation errors
      */
     private static MapContent
     openOSM(CoordinateReferenceSystem   crs,
             double                      scale,
             double                      dpi,
             DataStore                   osmDataStore,
             final URL                   sldUrl,
             SLDParser                   sldParser) throws IOException,
                                                           FactoryException {
         MapContent osmMap = new MapContent();
 
         System.out.println("Opening Open Street Map database...");
 
         // add the ground layers
         addLayer(osmDataStore, sldParser, sldUrl, crs,
                 "planet_osm_polygon", "oam_waters.sldt", scale, dpi, osmMap);
        addLayer(osmDataStore, sldParser, sldUrl, crs,
                  "planet_osm_polygon", "oam_forests.sld", scale, dpi, osmMap);
         addLayer(osmDataStore, sldParser, sldUrl, crs,
                  "planet_osm_point", "oam_city_markers.sldt", scale, dpi,
                  osmMap);
         addLayer(osmDataStore, sldParser, sldUrl, crs,
                  "planet_osm_polygon", "oam_cities.sldt", scale, dpi, osmMap);
         addLayer(osmDataStore, sldParser, sldUrl, crs,
                  "planet_osm_point", "oam_peaks.sldt", scale, dpi, osmMap);
         addLayer(osmDataStore, sldParser, sldUrl, crs,
                  "planet_osm_line", "oam_roads.sldt", scale, dpi, osmMap);
         addLayer(osmDataStore, sldParser, sldUrl, crs,
                  "planet_osm_point", "oam_labels.sldt", scale, dpi, osmMap);
 
         return osmMap;
     }
 
     /**
      * Render a map into a tileset. Assuming 256x256 pixel tiles and the
      * standard EPSG:900913 tileset zoom levels
      *
      * @param osmParams the parameters to create the Open Street Map DataStore
      * @param oamParams the parameters to create the Open Aviaton Map DataStore
      * @param coverage the area to generate tiles for. if null, the coverage
      *        area of the Open Aviation Map is used
      * @param crs the CRS to use for projection. if null, the CRS of the OAM
      *        data source is used
      * @param sldUrlStr the base URL for SLD files &amp; related resources.
      * @param dpi the number of dots per inch on the target image
      * @param lowLevel the lowest level to render
      * @param highLevel the highest level to render
      * @param outputPath the directory path where to generate the tile set
      * @throws IOException on I/O issues
      * @throws FactoryException in CRS factory issues
      * @throws TransformException on coordinate transformation issues
      */
     private static void
     renderMapTileset(Map<String, Object>        osmParams,
                      Map<String, Object>        oamParams,
                      ReferencedEnvelope         coverage,
                      CoordinateReferenceSystem  crs,
                      String                     sldUrlStr,
                      double                     dpi,
                      int                        lowLevel,
                      int                        highLevel,
                      String                     outputPath)
                                                      throws IOException,
                                                             TransformException,
                                                             FactoryException {
 
         // make sure we have an output directory
         File dir = new File(outputPath);
         if (dir.exists() && !dir.isDirectory()) {
             throw new IllegalArgumentException("Specified path " + outputPath
                                         + " exists, but is not a directory");
         } else if (!dir.exists() && !dir.mkdirs()) {
             throw new IllegalArgumentException("Could not create output dir "
                                         + outputPath);
         }
 
         // create the data stores
         DataStore osmDataStore = DataStoreFinder.getDataStore(osmParams);
         if (osmDataStore == null) {
             System.out.println(
                     "can't connect to the Open Street Map database");
             return;
         }
 
         DataStore oamDataStore = DataStoreFinder.getDataStore(oamParams);
         if (oamDataStore == null) {
             System.out.println(
                     "can't connect the to Open Aviation Map database");
             return;
         }
 
         // set logging levels for the geotools APIs
         ((JDBCDataStore) oamDataStore).getLogger().setLevel(Level.SEVERE);
         ((JDBCDataStore) osmDataStore).getLogger().setLevel(Level.SEVERE);
         org.geotools.util.logging.Logging.
           getLogger("org.geotools.referencing.factory").setLevel(Level.SEVERE);
 
 
         // create the parser with the sld configuration
         final URL sldUrl = new URL(sldUrlStr);
         SLDParser sldParser = new SLDParser(
                                     CommonFactoryFinder.getStyleFactory(null));
         DefaultResourceLocator rl = new DefaultResourceLocator();
         rl.setSourceUrl(sldUrl);
         sldParser.setOnLineResourceLocator(rl);
 
 
         List<Double> scales = KnownScaleList.epsg900913ScaleList(dpi,
                                                                  highLevel + 1);
 
         // coverage is the whole Earth
 
         ReferencedEnvelope mapBounds;
         if (coverage == null) {
             MapContent osmMap = openOSM(crs, scales.get(highLevel), dpi,
                                         osmDataStore, sldUrl, sldParser);
             MapContent oamMap = openOAM(crs, scales.get(highLevel), dpi,
                                         oamDataStore, sldUrl, sldParser);
 
             mapBounds = calcCoverage(osmMap, oamMap,
                                      DefaultGeographicCRS.WGS84);
 
             osmMap.dispose();
             oamMap.dispose();
         } else {
             mapBounds = transformCoverage(coverage, DefaultGeographicCRS.WGS84);
         }
 
         for (int level = lowLevel; level <= highLevel; ++level) {
             System.out.println("Rendering tiles for level " + level
                          + ", at dpi " + dpi + ", scale: " + scales.get(level));
 
             MapContent osmMap = openOSM(crs, scales.get(level), dpi,
                                         osmDataStore, sldUrl, sldParser);
 
             renderMapTileset(osmMap, crs, dpi, level, scales.get(level),
                          mapBounds,
                          outputPath + File.separator + "osm" + File.separator);
 
             osmMap.dispose();
 
             MapContent oamMap = openOAM(crs, scales.get(level), dpi,
                                         oamDataStore, sldUrl, sldParser);
 
             renderMapTileset(oamMap, crs, dpi, level, scales.get(level),
                         mapBounds,
                         outputPath + File.separator + "oam" + File.separator);
 
             oamMap.dispose();
 
             System.out.println("Level " + level + " done.");
         }
 
         osmDataStore.dispose();
         oamDataStore.dispose();
     }
 
     /**
      * Render a tiles for a particular map.
      *
      * @param map the map to render
      * @param crs the CRS to use for rendering
      * @param dpi the resolution to use for rendering
      * @param level the zoom level to render at
      * @param scale the scale to render at
      * @param mapBounds the map bounds to render
      * @param outputPath the base output path to render tiles into
      * @throws IOException on I/O errors
      * @throws FactoryException on CRS factory errors
      * @throws TransformException the CRS transformation errors
      */
     private static void
     renderMapTileset(MapContent                 map,
                      CoordinateReferenceSystem  crs,
                      double                     dpi,
                      int                        level,
                      double                     scale,
                      ReferencedEnvelope         mapBounds,
                      String                     outputPath)
                                                      throws IOException,
                                                             FactoryException,
                                                             TransformException {
 
         CoordinateReferenceSystem refCrs = crs == null
                                         ? map.getCoordinateReferenceSystem()
                                         : crs;
 
         Rectangle tileBounds = getTileBounds(mapBounds, level);
 
         int x = (int) tileBounds.getMinX();
         while (x <= (int) tileBounds.getMaxX()) {
 
             int metatileWidth = x + METATILE_SIZE <= (int) tileBounds.getMaxX()
                               ? METATILE_SIZE
                               : ((int) tileBounds.getMaxX()) - x + 1;
 
             int y = (int) tileBounds.getMinY();
             while (y <= (int) tileBounds.getMaxY()) {
 
                 int metatileHeight =
                                 y + METATILE_SIZE <= (int) tileBounds.getMaxY()
                                 ? METATILE_SIZE
                                 : ((int) tileBounds.getMaxY()) - y + 1;
 
                 System.out.println("Rendering tiles " + level + File.separator
                         + x + ".." + (x + metatileWidth - 1) + File.separator
                         + y + ".." + (y + metatileHeight - 1));
 
                 Rectangle imageBounds = new Rectangle(TILE_SIZE * metatileWidth,
                                                     TILE_SIZE * metatileHeight);
 
                 ReferencedEnvelope c = tile2BoundingBox(x, y, level);
                 ReferencedEnvelope d = tile2BoundingBox(x + metatileWidth - 1,
                                                         y + metatileHeight - 1,
                                                         level);
                 c.expandToInclude(d);
                 ReferencedEnvelope mb = c.transform(refCrs, false);
 
                 // first, generate the ground map
                 PlanarImage image = renderMap(map, mb, imageBounds,
                                               scale, dpi);
 
                 // now we have the metatile, cut it up and save it as tiles
                 for (int i = x; i < x + metatileWidth; ++i) {
                     File tileDir = new File(outputPath + File.separator + level
                                                        + File.separator + i);
 
                     if (!tileDir.exists() && !tileDir.mkdirs()) {
                         throw new IllegalArgumentException(
                                              "Could not create tile dir "
                                               + tileDir.getAbsolutePath());
                     }
 
                     for (int j = y; j < y + metatileHeight; ++j) {
 
                         String fileName = tileDir.getAbsolutePath()
                                                 + File.separator + j + ".png";
 
                         Rectangle r = new Rectangle((i - x) * TILE_SIZE,
                                                     (j - y) * TILE_SIZE,
                                                     TILE_SIZE, TILE_SIZE);
                         BufferedImage tile = image.getAsBufferedImage(r, null);
 
                         // save the tile
                         JAI.create("filestore", tile, fileName, "PNG", null);
                     }
                 }
 
                 image.dispose();
 
                 y += metatileHeight;
             }
 
             x += metatileWidth;
         }
     }
 
     /**
      * Return the tile index boundaries for a specific real-world boundary
      * at a specified zoom level.
      *
      * @param bounds the real-world bounds
      * @param zoom the zoom level
      * @return the tile index rectangle, representing the specified real-world
      *         region
      */
     public static Rectangle
     getTileBounds(ReferencedEnvelope bounds, final int zoom) {
         int west = (int) Math.floor(
                                 (bounds.getMinX() + 180) / 360 * (1 << zoom));
         int east = (int) Math.floor(
                                 (bounds.getMaxX() + 180) / 360 * (1 << zoom));
         int south = (int) Math.floor(
                 (1 - Math.log(Math.tan(Math.toRadians(bounds.getMinY())) + 1
                         / Math.cos(Math.toRadians(bounds.getMinY()))) / Math.PI)
                         / 2 * (1 << zoom));
         int north = (int) Math.floor(
                 (1 - Math.log(Math.tan(Math.toRadians(bounds.getMaxY())) + 1
                         / Math.cos(Math.toRadians(bounds.getMaxY()))) / Math.PI)
                         / 2 * (1 << zoom));
 
         return new Rectangle(west, north, east - west, south - north);
     }
 
     /**
      * Return a tile path for a particular position and zoom level.
      *
      * @param lat the latitude of the position
      * @param lon the longitude of the position
      * @param zoom the zoom level
      * @return a relative path to this particular tile, without extension
      */
     public static String
     getTilePath(final double lat, final double lon, final int zoom) {
         int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
         int ytile = (int) Math.floor(
                 (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1
               / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
 
         return "" + zoom + File.separator + xtile + File.separator + ytile;
     }
 
     /**
      * Return a tile directory path for a particular position and zoom level.
      *
      * @param lat the latitude of the position
      * @param lon the longitude of the position
      * @param zoom the zoom level
      * @return a relative directory path to this particular tile, without the
      *         filename
      */
     public static String
     getTileDir(final double lat, final double lon, final int zoom) {
         int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
 
         return "" + zoom + File.separator + xtile;
     }
 
     /**
      * Generate a geographical bounding box for a particular tile at a
      * particular zoom level.
      *
      * @param x the x tile coordinate
      * @param y the y tile coordinate
      * @param zoom the zoom level
      * @return a bounding box in WGS84 covering the specified tile
      */
     private static ReferencedEnvelope
     tile2BoundingBox(final int x, final int y, final int zoom) {
         return new ReferencedEnvelope(tile2lon(x, zoom),
                                       tile2lon(x + 1, zoom),
                                       tile2lat(y + 1, zoom),
                                       tile2lat(y, zoom),
                                       DefaultGeographicCRS.WGS84);
     }
 
     /**
      * Convert a tile x coordinate into a longitude value in degrees, at
      * a particular zoom level.
      *
      * @param x the x coordinate
      * @param z the zoom level
      * @return the longitude in degrees
      */
     private static double tile2lon(int x, int z) {
         double d = x / Math.pow(2.0, z) * 360.0 - 180;
 
         return d;
     }
 
     /**
      * Convert a tile y coordinate into a latitude value in degrees, at
      * a particular zoom level.
      *
      * @param y the y coordinate
      * @param z the zoom level
      * @return the latitude in degrees
      */
     private static double tile2lat(int y, int z) {
         double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
         double d = Math.toDegrees(Math.atan(Math.sinh(n)));
 
         return d;
     }
 
     /**
      * Add a lattice (grid) layer to a map.
      *
      * @param bounds the bounds of the lattice layer
      * @param sldParser the SLD parser to use for parsing SLDs
      * @param urlBase the base URL for the SLD & related resources
      * @param scale the scale of the rendering, used to re-scale SLD templates
      * @param dpi the target DPI, used to re-scale SLD templates
      * @param crs the CRS to use
      * @param map the map to add the lattice layer to
      * @throws FactoryException if some factories are not found
      * @throws TransformException on CRS transformation issues
      * @throws IOException on I/O errors
      */
     private static void
     addLatticeLayer(ReferencedEnvelope          bounds,
                     SLDParser                   sldParser,
                     final URL                   urlBase,
                     double                      scale,
                     double                      dpi,
                     CoordinateReferenceSystem   crs,
                     MapContent                  map)
                                                     throws TransformException,
                                                            FactoryException,
                                                            IOException {
 
         ReferencedEnvelope b = bounds.transform(DefaultGeographicCRS.WGS84,
                                                 false);
 
         b = noramlizeEnvelope(b);
 
         // Specify vertex spacing to get "densified" polygons
         double vertexSpacing = 0.1;
         SimpleFeatureSource grid = Lines.createOrthoLines(b,
                                                           GRID_DEF,
                                                           vertexSpacing);
 
         // get the style by parsing & scaling an SLD
         Style style;
 
         try {
             style = scaleSld(sldParser,
                              urlBase,
                              "oam_grid.sldt",
                              bounds.transform(crs, false),
                              scale,
                              dpi);
         } catch (Exception e) {
             System.out.println("error scaling SLD template oam_grid.sldt");
             System.out.println(e.getMessage());
             return;
         }
 
         // add the layer to the map
         Query query = new Query(Query.ALL);
         query.setCoordinateSystemReproject(crs);
 
         FeatureLayer layer = new FeatureLayer(grid.getFeatures(query), style);
 
         map.addLayer(layer);
     }
 
     /**
      * Return the Hungarian Lambert Conformal Projection as a CRS, which has the
      * standard parallels at 46 and 48 degrees.
      *
      * @return a Lambert projection suitable for Hungary
      * @throws FactoryException on CRS creation errors
      */
     public static CoordinateReferenceSystem
     getHungarianLambertProjection() throws FactoryException {
         // the projection covers the following area: 16,45 23,49
         // thus the central meridian is 19.5
 
         final String wkt = "PROJCS[\"WGS 84 - "
                 + "Hungary Lambert Conformal Conic Projection\","
 
                 + "GEOGCS[\"WGS 84\","
                 + "DATUM[\"World Geodetic System 1984\","
                 + "SPHEROID[\"WGS 84\", 6378137.0, 298.257223563,"
                         + "AUTHORITY[\"EPSG\",\"7030\"]],"
                     + "AUTHORITY[\"EPSG\",\"6326\"]],"
                 + "PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],"
                 + "UNIT[\"degree\", 0.017453292519943295],"
                 + "AXIS[\"Geodetic latitude\", NORTH],"
                 + "AXIS[\"Geodetic longitude\", EAST],"
                 + "AUTHORITY[\"EPSG\",\"4326\"]],"
 
                 + "PROJECTION[\"Lambert_Conformal_Conic_2SP\","
                     + "AUTHORITY[\"EPSG\",\"9802\"]],"
                 + "PARAMETER[\"central_meridian\", 19.5],"
                 + "PARAMETER[\"latitude_of_origin\", 47.0],"
                 + "PARAMETER[\"standard_parallel_1\", 48.0],"
                 + "PARAMETER[\"false_easting\", 400000.0],"
                 + "PARAMETER[\"false_northing\", 400000.0],"
                 + "PARAMETER[\"scale_factor\", 1.0],"
                 + "PARAMETER[\"standard_parallel_2\", 46.0],"
                 + "UNIT[\"m\", 1.0],"
                 + "AXIS[\"Northing\", NORTH],"
                 + "AXIS[\"Easting\", EAST]]";
 
 
         return CRS.parseWKT(wkt);
     }
 
     /**
      * Draw the static labels on the rendered map, like scale, title, etc.
      *
      * @param mapBounds the map to draw the labels for. used to calculate grid line
      *        end positions, to draw grid line degrees
      * @param mapImage the map image to draw around
      * @param scale the scale of the map
      * @param dpi the DPI of rendering
      * @return an extended image, with the labels around the original map image
      * @throws FactoryException on CRS conversion errors
      * @throws TransformException on CRS conversion errors
      */
     private static DiskMemImage
     drawLabels(ReferencedEnvelope   mapBounds,
                DiskMemImage         mapImage,
                double               scale,
                double               dpi) throws TransformException,
                                                  FactoryException {
 
         // expand our original image at 3.5% on top & bottom, at 1.5% on the sides
         Rectangle bounds = mapImage.getBounds();
         int edgeHeight = (int) (bounds.height * .035);
         int edgeWidth  = (int) (bounds.height * .015);
 
         DiskMemImage image = new DiskMemImage(
                                         0, 0,
                                         mapImage.getWidth() + 2 * edgeWidth,
                                         mapImage.getHeight() + 2 * edgeHeight,
                                         0, 0,
                                         mapImage.getSampleModel(),
                                         mapImage.getColorModel());
 
         // get a graphics object & create a while fill
         Graphics2D gr = image.createGraphics();
         bounds = image.getBounds();
         gr.setColor(Color.WHITE);
         gr.fill(bounds);
 
         // draw the original map on our image
         gr.drawRenderedImage(mapImage, new AffineTransform(1.0, 0, 0, 1.0,
                                                         edgeWidth, edgeHeight));
 
         // draw a frame around the map
         gr.setColor(Color.BLACK);
         gr.setStroke(new BasicStroke((int) (bounds.height * .002)));
         gr.draw(new Rectangle(edgeWidth, edgeHeight,
                               bounds.width - 2 * edgeWidth,
                               bounds.height - 2 * edgeHeight));
 
         // set a large bold font
         gr.setFont(getFont("Arial", Font.BOLD, (int) (edgeHeight * .50),
                            Rotation.NONE, gr));
         gr.setColor(Color.BLACK);
         FontMetrics fm = gr.getFontMetrics();
 
         // draw a label of 2% of the whole height, in the center on the top
         String str = "Open Aviation Map - Hungary VFR Chart";
         Rectangle2D strR = fm.getStringBounds(str, gr);
         gr.drawString(str, (int) ((bounds.width / 2.0) - strR.getWidth() / 2.0),
                            (int) ((edgeHeight / 2.0)
                                 + (strR.getHeight() - fm.getDescent()) / 2.0));
 
         // draw the scale information on the top-right
         final DecimalFormat df = new DecimalFormat("#,###");
         gr.setFont(getFont("Arial", Font.BOLD, (int) (edgeHeight * .40),
                            Rotation.NONE, gr));
         fm = gr.getFontMetrics();
         str = "Scale 1:" + df.format(scale);
         strR = fm.getStringBounds(str, gr);
         gr.drawString(str, (int) ((bounds.width - edgeWidth) - strR.getWidth()),
                            (int) ((edgeHeight / 2.0)
                                 + (strR.getHeight() - fm.getDescent()) / 2.0));
 
         // draw projection information on the top right
         CoordinateReferenceSystem crs =
                                     mapBounds.getCoordinateReferenceSystem();
         String projStr = crs.getName().getCode();
         String projStr2 = "";
 
         if (CRS.equalsIgnoreMetadata(crs, getHungarianLambertProjection())) {
             projStr = "Lambert Conformal Conic Projection, Datum: WGS84";
             projStr2 = "Standard Parallels at 46\u00b0 and 48\u00b0";
         } else if (CRS.equalsIgnoreMetadata(crs, DefaultGeographicCRS.WGS84)) {
             projStr = "Mercator Projection, Datum: WGS84";
         } else if (CRS.equalsIgnoreMetadata(crs, CRS.decode("EPSG:900913"))) {
             projStr = "EPSG:900913 Google Mercator Projection, Datum: WGS84";
         }
         gr.setFont(getFont("Arial", Font.BOLD, (int) (edgeHeight * .20),
                            Rotation.NONE, gr));
         fm = gr.getFontMetrics();
         strR = fm.getStringBounds(projStr, gr);
         gr.drawString(projStr, edgeWidth, (int) ((edgeHeight * .20)
                                 + (strR.getHeight() - fm.getDescent()) / 2.0));
         if (!projStr2.isEmpty()) {
             strR = fm.getStringBounds(projStr2, gr);
             gr.drawString(projStr2, edgeWidth, (int) ((edgeHeight * .55)
                                  + (strR.getHeight() - fm.getDescent()) / 2.0));
         }
 
         // draw a reference to the project in the lower right
         gr.setFont(getFont("Arial", Font.BOLD, (int) (edgeHeight * .40),
                            Rotation.NONE, gr));
         fm = gr.getFontMetrics();
         str = "Open Aviation Map - http://openaviationmap.tyrell.hu/";
         strR = fm.getStringBounds(str, gr);
         gr.drawString(str, (int) ((bounds.width - edgeWidth) - strR.getWidth()),
                            (int) (bounds.height - (edgeHeight / 2.0)
                                 + (strR.getHeight() - fm.getDescent()) / 2.0));
 
         // draw scales on the lower left
         drawScaleBarMetric(edgeWidth,
                            (int) (bounds.height * .982),
                            (int) (bounds.width / 4d),
                            (int) (edgeHeight / 8.0d),
                            scale,
                            dpi,
                            gr,
                            bounds,
                            edgeHeight);
 
         drawScaleBarNautical(edgeWidth,
                              (int) (bounds.height * .982 + edgeHeight / 9.0d),
                              (int) (bounds.width / 4d),
                              (int) (edgeHeight / 9.0d),
                              scale,
                              dpi,
                              gr,
                              bounds,
                              edgeHeight);
 
         // draw grid line values
         drawGridLabels(mapBounds, mapImage, bounds, edgeHeight, edgeWidth, gr);
 
         // clean up
         gr.dispose();
 
         return image;
     }
 
     /**
      * Draw labels for the grid lines outside the map.
      *
      * @param mapBounds the bounds of the original map, includes proper
      *        projection info
      * @param mapImage the image of the original map
      * @param bounds the bounds of the image to draw on
      * @param edgeHeight the height of the edge area to draw, this extends
      *        beyond the map image
      * @param edgeWidth the width of the edge area to draw, this extends
      *        beyond the map image
      * @param gr the graphics object to draw into
      * @throws OperationNotFoundException on coordinate conversion operations
      *         not found
      * @throws FactoryException on CRS transformation errors
      * @throws TransformException on CRS transformation errors
      */
     private static void
     drawGridLabels(ReferencedEnvelope   mapBounds,
                    DiskMemImage         mapImage,
                    Rectangle            bounds,
                    int                  edgeHeight,
                    int                  edgeWidth,
                    Graphics2D           gr) throws FactoryException,
                                                    TransformException {
 
         AffineTransform ws = RendererUtilities.worldToScreenTransform(
                                               mapBounds, mapImage.getBounds());
 
         OrthoLineDef hDef = null;
         OrthoLineDef vDef = null;
 
         for (OrthoLineDef ld : GRID_DEF) {
             if (hDef != null && vDef != null) {
                 break;
             }
 
             if (ld.getLevel() == 2) {
                 if (ld.getOrientation() == LineOrientation.HORIZONTAL) {
                     hDef = ld;
                 }
                 if (ld.getOrientation() == LineOrientation.VERTICAL) {
                     vDef = ld;
                 }
             }
         }
 
         CoordinateOperationFactory coordinateOperationFactory =
                                       CRS.getCoordinateOperationFactory(false);
 
         CoordinateOperation wgs84ToMap =
                     coordinateOperationFactory.createOperation(
                                     DefaultGeographicCRS.WGS84,
                                     mapBounds.getCoordinateReferenceSystem());
 
         MathTransform wgs84ToMapT = wgs84ToMap.getMathTransform();
 
         CoordinateOperation mapToWgs84 =
                     coordinateOperationFactory.createOperation(
                                     mapBounds.getCoordinateReferenceSystem(),
                                     DefaultGeographicCRS.WGS84);
 
         ReferencedEnvelope wgs84Bounds = new ReferencedEnvelope(
                         CRS.transform(mapToWgs84, mapBounds).toRectangle2D(),
                         DefaultGeographicCRS.WGS84);
 
         wgs84Bounds = noramlizeEnvelope(wgs84Bounds);
 
 
         int fontSize = (int) (edgeWidth * .3);
 
         if (hDef != null && vDef != null) {
             // draw grid line values for the vertical axis
             double maxVertical = wgs84Bounds.getMaxY();
 
             for (double v = wgs84Bounds.getMinY(); v < maxVertical;
                  v += vDef.getSpacing()) {
 
                 // draw on the east edge
                 double[] p1 = new double[] {wgs84Bounds.getMaxX(), v};
                 double[] p2 = new double[2];
                 double[] p3 = new double[2];
 
                 // transform from WGS84 to map coordinates
                 wgs84ToMapT.transform(p1, 0, p2, 0, 1);
                 // transform from map coordinates to screen coordinates
                 ws.transform(p2,  0, p3, 0, 1);
                 drawGridLabel(bounds.width - edgeWidth,
                               (int) (edgeHeight + p3[1]),
                               fontSize,
                               Alignment.RIGHT, Rotation.CW,
                               p1[1], gr);
 
                 // draw on the west edge
                 p1 = new double[] {wgs84Bounds.getMinX(), v};
 
                 // transform from WGS84 to map coordinates
                 wgs84ToMapT.transform(p1, 0, p2, 0, 1);
                 // transform from map coordinates to screen coordinates
                 ws.transform(p2,  0, p3, 0, 1);
                 drawGridLabel(edgeWidth,
                               (int) (edgeHeight + p3[1]),
                               fontSize,
                               Alignment.LEFT, Rotation.CCW,
                               p1[1], gr);
             }
 
             double maxHorizontal = wgs84Bounds.getMaxX();
 
             for (double v = wgs84Bounds.getMinX(); v < maxHorizontal;
                  v += hDef.getSpacing()) {
 
                 // draw on the north edge
                 double[] p1 = new double[] {v, wgs84Bounds.getMaxY()};
                 double[] p2 = new double[2];
                 double[] p3 = new double[2];
 
                 // transform from WGS84 to map coordinates
                 wgs84ToMapT.transform(p1, 0, p2, 0, 1);
                 // transform from map coordinates to screen coordinates
                 ws.transform(p2,  0, p3, 0, 1);
                 drawGridLabel((int) (edgeWidth + p3[0]),
                               edgeHeight,
                               fontSize,
                               Alignment.CENTER, Rotation.NONE,
                               p1[0], gr);
 
                 // draw on the south edge
                 p1 = new double[] {v, wgs84Bounds.getMinY()};
 
                 // transform from WGS84 to map coordinates
                 wgs84ToMapT.transform(p1, 0, p2, 0, 1);
                 // transform from map coordinates to screen coordinates
                 ws.transform(p2,  0, p3, 0, 1);
                 drawGridLabel((int) (edgeWidth + p3[0]),
                               bounds.height - edgeHeight + fontSize * 2,
                               fontSize,
                               Alignment.CENTER, Rotation.NONE,
                               p1[0], gr);
             }
         }
     }
 
     /**
      * Normalize an envelope so that the edges align with 0.5 degrees.
      *
      * @param e the envelope to normalize
      * @return a normalized envelope
      */
     private static ReferencedEnvelope
     noramlizeEnvelope(ReferencedEnvelope e) {
         return new ReferencedEnvelope(Math.floor(e.getMinX() * 2.0) / 2.0,
                                       Math.ceil(e.getMaxX() * 2.0) / 2.0,
                                       Math.floor(e.getMinY() * 2.0) / 2.0,
                                       Math.ceil(e.getMaxY() * 2.0) / 2.0,
                                       e.getCoordinateReferenceSystem());
     }
 
     /**
      * Draw a grid label at a specified point.
      *
      * @param x the x coordinate to draw at
      * @param y the y coordinate to draw at
      * @param size the size of the font to use
      * @param align the alignment, see constants in SwingConstants, one of
      *        LEFT or RIGHT
      * @param rotation specify a rotation if needed
      * @param degree the degree to draw
      * @param gr the graphics object used for drawing
      */
     private static void
     drawGridLabel(int x, int y, int size, Alignment align, Rotation rotation,
                   double degree, Graphics2D gr) {
         final MessageFormat mf = new MessageFormat(
                                         "{0,number,#}\u00b0{1,number,00}''");
 
         gr.setFont(getFont("Arial", Font.BOLD, size, rotation, gr));
         FontMetrics fm = gr.getFontMetrics();
 
         double d = Math.round(degree * 100.0) / 100.0;
         double minutes = Math.floor((d - Math.floor(d)) * 60d);
         Object[] args = {Math.floor(d), minutes};
 
         String str = mf.format(args);
 
         Rectangle2D strR = fm.getStringBounds(str, gr);
         double height = strR.getHeight() - fm.getDescent();
 
         double xOff;
         double yOff;
 
         switch (rotation) {
         case CW:
             xOff = height / 2.0;
             yOff = (strR.getWidth() / 2.0);
             break;
 
         case CCW:
             xOff = height;
             yOff = (strR.getWidth() / 2.0);
             break;
 
         case NONE:
         default:
             xOff = strR.getWidth();
             yOff = height / 2.0;
         }
 
         // note: all vertical alignments are centered
         switch (align) {
         case CENTER:
             gr.drawString(str, (int) (x - xOff / 2.0), (int) (y - yOff));
             break;
 
         case RIGHT:
             gr.drawString(str, (int) (x + xOff), (int) (y - yOff));
             break;
 
         case LEFT:
         default:
             gr.drawString(str, (int) (x - xOff), (int) (y + yOff));
         }
     }
 
     /**
      * Generate a font that has a height of certain pixels.
      *
      * @param name the name of the font
      * @param style the style of the font
      * @param size the size of the font, in pixels, which is the height in
      *        pixels
      * @param rotation specify a rotation if needed.
      * @param gr the Graphics object to generate the font for
      * @return a font of the specified size
      */
     private static Font getFont(String      name,
                                 int         style,
                                 int         size,
                                 Rotation    rotation,
                                 Graphics2D  gr) {
 
         Font font = new Font(name, style, 10);
         FontMetrics fm = gr.getFontMetrics(font);
         double pointPerPixel = (fm.getMaxAscent() + fm.getMaxDescent()) / 10.0d;
 
         font = new Font(name, style, (int) (size * pointPerPixel));
 
         AffineTransform at = new AffineTransform();
 
         switch (rotation) {
         case CW:
             at.rotate(Math.PI / 2.0d);
             font = font.deriveFont(at);
             break;
 
         case CCW:
             at.rotate(-1.0d * Math.PI / 2.0d);
             font = font.deriveFont(at);
             break;
 
         case NONE:
         default:
         }
 
         return font;
     }
 
     /**
      * Draw a metric scale bar on the bottom-left.
      *
      * @param x the x coordinate of the left hand side of the scale bar
      * @param y the y coordinate of the opper side of the scale bar
      *        (note: additional decoration will be drawn above this)
      * @param width the maximum width of the scale bar
      * @param scaleHeight the height of the scale bar itself (not the whole
      *        thing though)
      * @param scale the scale of the map
      * @param dpi the DPI value
      * @param gr the graphics object to use
      * @param bounds the bounds of the entire map
      * @param edgeHeight the height of the edge (drawing area)
      */
     private static void
     drawScaleBarMetric(int          x,
                        int          y,
                        int          width,
                        int          scaleHeight,
                        double       scale,
                        double       dpi,
                        Graphics2D   gr,
                        Rectangle    bounds,
                        int          edgeHeight) {
 
         String str;
         Rectangle2D strR;
 
         // draw a scale bar on the bottom left, on the 1/3rd of the map
         double dotInMeters = 0.0254d / dpi;
         double meterPerPixel = 1 / dotInMeters;
         double scaledMeterPerPixel = meterPerPixel / scale;
         double scaleLength = bounds.width / 4.0d;
         double scaleLengthInMeters = scaleLength / scaledMeterPerPixel;
         if (scaleLengthInMeters >= 100000) {
             scaleLengthInMeters -= scaleLengthInMeters % 100000;
         } else {
             scaleLengthInMeters -= scaleLengthInMeters % 10000;
         }
         gr.setColor(Color.BLACK);
         gr.setStroke(new BasicStroke((int) (bounds.height * .0001)));
 
 
         // draw a 100km long scale line
         gr.draw(new Rectangle(x, y,
                               (int) (scaleLengthInMeters * scaledMeterPerPixel),
                               scaleHeight));
 
         // draw a checkered scale at 10km each
         boolean fill = true;
         gr.setFont(getFont("Arial", Font.PLAIN, scaleHeight,
                            Rotation.NONE, gr));
         FontMetrics fm = gr.getFontMetrics();
 
         // draw the first line that extends from the rectangle
         gr.draw(new Line2D.Double(x, y + scaleHeight, x, y - scaleHeight));
         str = "0km";
         strR = fm.getStringBounds(str, gr);
         gr.drawString(str,
                       (int) (x - (strR.getWidth() / 2.0)),
                       y - scaleHeight);
 
         // draw a line each 10km
         for (double i = 10000; i < scaleLengthInMeters; i += 10000) {
             int xx = (int) (x + i * scaledMeterPerPixel);
 
             // fill in if needed
             if (fill) {
                 gr.fill(new Rectangle(xx, y,
                                       (int) (10000 * scaledMeterPerPixel),
                                       scaleHeight));
                 fill = false;
             } else {
                 fill = true;
             }
 
             // draw a line that extends from the rectangle
             gr.draw(new Line2D.Double(xx, y + scaleHeight,
                                       xx, y - scaleHeight));
 
             // draw a length value
             str = "" + ((int) (i / 1000)) + "km";
             strR = fm.getStringBounds(str, gr);
             gr.drawString(str,
                           (int) (xx - (strR.getWidth() / 2.0)),
                           y - scaleHeight);
         }
 
         // draw the last line that extends from the rectangle
         gr.draw(new Line2D.Double(x + scaleLengthInMeters * scaledMeterPerPixel,
                                   y + scaleHeight,
                                   x + scaleLengthInMeters * scaledMeterPerPixel,
                                   y - scaleHeight));
         str = "" + ((int) scaleLengthInMeters / 1000) + "km";
         strR = fm.getStringBounds(str, gr);
         gr.drawString(str,
                       (int) (x + scaleLengthInMeters * scaledMeterPerPixel
                            - (strR.getWidth() / 2.0)),
                       y - scaleHeight);
 
     }
 
     /**
      * Draw a nautical scale bar on the bottom-left.
      *
      * @param x the x coordinate of the left hand side of the scale bar
      * @param y the y coordinate of the opper side of the scale bar
      *        (note: additional decoration will be drawn above this)
      * @param width the maximum width of the scale bar
      * @param scaleHeight the height of the scale bar itself (not the whole
      *        thing though)
      * @param scale the scale of the map
      * @param dpi the DPI value
      * @param gr the graphics object to use
      * @param bounds the bounds of the entire map
      * @param edgeHeight the height of the edge (drawing area)
      */
     private static void
     drawScaleBarNautical(int          x,
                          int          y,
                          int          width,
                          int          scaleHeight,
                          double       scale,
                          double       dpi,
                          Graphics2D   gr,
                          Rectangle    bounds,
                          int          edgeHeight) {
 
         String str;
         Rectangle2D strR;
 
         // draw a scale bar on the bottom left, on the 1/3rd of the map
         double dotInNm = 0.0000137149d / dpi;
         double nmPerPixel = 1 / dotInNm;
         double scaledNmPerPixel = nmPerPixel / scale;
         double scaleLength = bounds.width / 4.0d;
         double scaleLengthInNm = scaleLength / scaledNmPerPixel;
         if (scaleLengthInNm >= 100) {
             scaleLengthInNm -= scaleLengthInNm % 100;
         } else {
             scaleLengthInNm -= scaleLengthInNm % 10;
         }
         gr.setColor(Color.BLACK);
 
 
         // draw a 100nm long scale line
         gr.draw(new Rectangle(x, y,
                               (int) (scaleLengthInNm * scaledNmPerPixel),
                               scaleHeight));
 
         // draw a checkered scale at 10km each
         boolean fill = true;
         gr.setFont(getFont("Arial", Font.PLAIN, scaleHeight, Rotation.NONE, gr));
         FontMetrics fm = gr.getFontMetrics();
 
         // draw the first line that extends from the rectangle
         gr.draw(new Line2D.Double(x, y + scaleHeight, x, y + 2 * scaleHeight));
         str = "0nm";
         strR = fm.getStringBounds(str, gr);
         gr.drawString(str,
                       (int) (x - (strR.getWidth() / 2.0)),
                       (int) (y + 2 * scaleHeight + strR.getHeight()));
 
         // draw a line each 10nm
         for (double i = 10; i < scaleLengthInNm; i += 10) {
             int xx = (int) (x + i * scaledNmPerPixel);
 
             // fill in if needed
             if (fill) {
                 gr.fill(new Rectangle(xx, y,
                                       (int) (10 * scaledNmPerPixel),
                                       scaleHeight));
                 fill = false;
             } else {
                 fill = true;
             }
 
             // draw a line that extends from the rectangle
             gr.draw(new Line2D.Double(xx, y + scaleHeight,
                                       xx, y + 2 * scaleHeight));
 
             // draw a length value
             str = "" + ((int) i) + "nm";
             strR = fm.getStringBounds(str, gr);
             gr.drawString(str,
                           (int) (xx - (strR.getWidth() / 2.0)),
                           (int) (y + 2 * scaleHeight + strR.getHeight()));
         }
 
         // draw the last line that extends from the rectangle
         gr.draw(new Line2D.Double(x + scaleLengthInNm * scaledNmPerPixel,
                                   y + scaleHeight,
                                   x + scaleLengthInNm  * scaledNmPerPixel,
                                   y + 2 * scaleHeight));
         str = "" + ((int) scaleLengthInNm) + "nm";
         strR = fm.getStringBounds(str, gr);
         gr.drawString(str,
                       (int) (x + scaleLengthInNm * scaledNmPerPixel
                            - (strR.getWidth() / 2.0)),
                       (int) (y + 2 * scaleHeight + strR.getHeight()));
     }
 
     /**
      * Combine two image files into a third one, but superimposing the two
      * images with each other.
      *
      * @param imageBounds the bounds of the image to combine into
      * @param osmImage the lower layer to combine
      * @param oamImage the upper layer to combine
      * @return the combined image
      * @throws IOException on I/O errors
      */
     private static DiskMemImage
     combineImages(Rectangle     imageBounds,
                   PlanarImage   osmImage,
                   PlanarImage   oamImage)
                                                         throws IOException {
 
         ColorModel cm = ColorModel.getRGBdefault();
         SampleModel sm = cm.createCompatibleSampleModel(1024, 1024);
 
         DiskMemImage image = new DiskMemImage(0, 0,
                                         imageBounds.width, imageBounds.height,
                                         0, 0, sm, cm);
 
         Graphics2D gr = image.createGraphics();
         gr.setPaint(Color.WHITE);
         gr.fill(imageBounds);
 
         gr.drawRenderedImage(osmImage, new AffineTransform());
         gr.drawRenderedImage(oamImage, new AffineTransform());
 
         gr.dispose();
 
         return image;
     }
 
     /**
      * Parse a db parameter string, and create a parameter object that
      * is acceptable by the GeoTools DataStoreFinder.
      *
      * @param paramStr the parameter string
      * @return a map that can be used by DataStoreFinder.getDataStore()
      *         to find a data store
      */
     private static Map<String, Object>
     parseDbParams(String paramStr) {
         Map<String, Object> params = new HashMap<String, Object>();
 
         StringTokenizer tok = new StringTokenizer(paramStr, ",");
         if (tok.countTokens() != 4) {
             throw new IllegalArgumentException(
                     "wrong number of commas in DB parameter string");
         }
 
         params.put("dbtype", "postgis");
         params.put("host", tok.nextToken());
         params.put("port", 5432);
         params.put("schema", "public");
         params.put("database", tok.nextToken());
         params.put("user", tok.nextToken());
         params.put("passwd", tok.nextToken());
 
         return params;
     }
 
     /**
      * Add a layer to a map.
      *
      * @param dataStore the data store to add the layer from
      * @param sldParser the SLD parser to use for parsing SLDs
      * @param urlBase the base URL for the SLD & related resources
      * @param crs the CRS to use. if null, the CRS of the data store is used
      * @param featureName the name of the feature from the data store
      * @param styleName the name of the SLD file to use
      * @param scale the scale of the rendering, used to re-scale SLD templates
      * @param dpi the target DPI, used to re-scale SLD templates
      * @param map the map to add the layer to
      * @throws IOException on I/O errors
      * @throws FactoryException on CRS factory errors
      */
     private static void
     addLayer(DataStore                  dataStore,
              SLDParser                  sldParser,
              final URL                  urlBase,
              CoordinateReferenceSystem  crs,
              String                     featureName,
              String                     styleName,
              double                     scale,
              double                     dpi,
              MapContent                 map) throws IOException,
                                                     FactoryException {
 
         if (styleName.endsWith(".sldt")) {
 
             SimpleFeatureSource fs = dataStore.getFeatureSource(featureName);
             ReferencedEnvelope bounds = fs.getBounds();
 
             try {
                 Style style = scaleSld(sldParser,
                                        urlBase,
                                        styleName,
                                        bounds,
                                        scale,
                                        dpi);
                 FeatureLayer layer;
                 if (crs != null) {
                     Query query = new Query(Query.ALL);
                     query.setCoordinateSystemReproject(crs);
                     layer = new FeatureLayer(fs.getFeatures(query), style);
                 } else {
                     layer = new FeatureLayer(fs, style);
                 }
                 map.addLayer(layer);
             } catch (RuntimeException e) {
                 throw e;
             } catch (IOException e) {
                 throw e;
             } catch (Exception e) {
                 System.out.println("error scaling SLD template " + styleName);
                 System.out.println(e.getMessage());
             }
         } else {
             sldParser.setInput(new URL(urlBase + styleName));
             Style[] styles = sldParser.readXML();
             FeatureLayer layer;
             if (crs != null) {
                 Query query = new Query(Query.ALL);
                 query.setCoordinateSystemReproject(crs);
                 layer = new FeatureLayer(
                     dataStore.getFeatureSource(featureName).getFeatures(query),
                     styles[0]);
             } else {
                 layer = new FeatureLayer(
                         dataStore.getFeatureSource(featureName), styles[0]);
             }
             map.addLayer(layer);
         }
     }
 
     /**
      * Scale an SLD and produce a style object from it.
      *
      * @param sldParser the SLD parser to use for parsing SLDs
      * @param urlBase the base URL for the SLD & related resources
      * @param styleName the name of the SLD file to use
      * @param bounds the bounds of the area to scale the SLD for
      * @param scale the scale of the rendering, used to re-scale SLD templates
      * @param dpi the target DPI, used to re-scale SLD templates
      * @return the scale SLD as a style object
      * @throws Exception on errors
      */
     private static Style
     scaleSld(SLDParser          sldParser,
              URL                urlBase,
              String             styleName,
              ReferencedEnvelope bounds,
              double             scale,
              double             dpi) throws Exception {
         CoordinateReferenceSystem crs =
                 bounds.getCoordinateReferenceSystem();
 
         Coordinate centerPoint = bounds.centre();
 
         Reader scaledSld = scaleSld(urlBase,
                                     styleName,
                                     crs,
                                     scale,
                                     dpi,
                                     centerPoint);
 
         sldParser.setInput(scaledSld);
         Style[] styles = sldParser.readXML();
 
         return styles[0];
     }
 
     /**
      * Scale an SLD template into an SLD, tailor made for the specified
      * scale and DPI value.
      *
      * @param urlBase the base URL where to find the SLD template
      * @param styleName the name of the SLD template, relative to urlBase
      * @param crs the CRS to use for scaling
      * @param scale the target scale, which is 1:scale
      * @param dpi the target DPI
      * @param centerPoint a reference point to calculate real-world scale,
      *        in CRS notation
      * @return the transformed SLD
      * @throws Exception on scaling errors
      */
     private static Reader
     scaleSld(URL                        urlBase,
             String                      styleName,
             CoordinateReferenceSystem   crs,
             double                      scale,
             double                      dpi,
             Coordinate                  centerPoint) throws Exception {
 
         URL    url    = new URL(urlBase + styleName);
         Reader reader = new InputStreamReader(url.openStream());
 
         List<Double> scales = new ArrayList<Double>(1);
         scales.add(scale);
 
         double[] refXY = {centerPoint.x, centerPoint.y};
 
         StringWriter output = new StringWriter();
 
         ScaleSLD.scaleSld(reader, scales, dpi, crs, refXY, output);
 
         StringReader result = new StringReader(output.toString());
 
         return result;
     }
 
     /**
      * Render a map into an image.
      *
      * @param map the map to save
      * @param mapBounds the part of the map to render
      * @param imageBounds the size of the image to render
      * @param scale the scale of the map
      * @param dpi the DPI of rendering
      * @return the image containing the rendered map
      * @throws FactoryException on CRS transformation errors
      * @throws TransformException on CRS transformation errors
      */
     public static PlanarImage
     renderMap(final MapContent          map,
               final ReferencedEnvelope  mapBounds,
               final Rectangle           imageBounds,
               final double              scale,
               final double              dpi)
                                                   throws TransformException,
                                                          FactoryException {
 
         // set up the renderer
         GTRenderer renderer = new StreamingRenderer();
         renderer.setMapContent(map);
 
         Map<Object, Object> rendererParams = new HashMap<Object, Object>();
         // don't add a DPI_KEY hint, as the renderer will try to re-scale
         // from your DPI to it's 'ideal' DPI of 90
         rendererParams.put(StreamingRenderer.SCALE_COMPUTATION_METHOD_KEY,
                            StreamingRenderer.SCALE_ACCURATE);
         rendererParams.put(StreamingRenderer.ADVANCED_PROJECTION_HANDLING_KEY,
                            new Boolean(true));
         rendererParams.put(StreamingRenderer.VECTOR_RENDERING_KEY,
                             new Boolean(true));
         rendererParams.put(StreamingRenderer.DECLARED_SCALE_DENOM_KEY, scale);
         rendererParams.put("renderingBuffer", 100);
         renderer.setRendererHints(rendererParams);
 
         RenderingHints hints2D =
                 new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
         hints2D.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         renderer.setJava2DHints(hints2D);
 
         // render the map
         ColorModel cm = ColorModel.getRGBdefault();
         SampleModel sm = cm.createCompatibleSampleModel(1024, 1024);
 
         DiskMemImage image = new DiskMemImage(0, 0,
                                          imageBounds.width, imageBounds.height,
                                          0, 0, sm, cm);
 
         Graphics2D gr = image.createGraphics();
 
         renderer.paint(gr, imageBounds, mapBounds);
 
         gr.dispose();
 
         return image;
     }
 
     /**
      * Calculate the size of the generated image.
      *
      * @param scale the scale to use for generating the image
      * @param dpi the dots per inch in the image
      * @param mapBounds the bounds of the map used for generating the image
      * @return the rectangle depicting the size of the generated image
      * @throws TransformException on coordinate transformation errors
      */
     private static Rectangle
     calcImageBounds(final double                scale,
                     final double                dpi,
                     ReferencedEnvelope          mapBounds)
                                                 throws TransformException {
         Rectangle imageBounds;
 
         double widthInMeters;
         double heightInMeters;
 
         CoordinateReferenceSystem crs =
                                     mapBounds.getCoordinateReferenceSystem();
         GeodeticCalculator gc = new GeodeticCalculator(crs);
 
         double mean0 = (mapBounds.getMinimum(0) + mapBounds.getMaximum(0))
                      / 2.0d;
         double mean1 = (mapBounds.getMinimum(1) + mapBounds.getMaximum(1))
                      / 2.0d;
 
         if (AxisDirection.NORTH.equals(
                     crs.getCoordinateSystem().getAxis(0).getDirection())) {
 
             gc.setStartingPosition(new DirectPosition2D(crs,
                                     mean0, mapBounds.getMinimum(1)));
             gc.setDestinationPosition(new DirectPosition2D(crs,
                     mapBounds.getMinimum(1) + mapBounds.getWidth() / 3.0d,
                     mean0));
 
             widthInMeters = gc.getOrthodromicDistance() * 3.0d;
 
             gc.setStartingPosition(new DirectPosition2D(crs,
                                    mapBounds.getMinimum(0), mean1));
             gc.setDestinationPosition(new DirectPosition2D(crs, mean1,
                     mapBounds.getMinimum(0) + mapBounds.getHeight() / 3.0d));
 
             heightInMeters = gc.getOrthodromicDistance() * 3.0d;
 
         } else if (AxisDirection.EAST.equals(
                         crs.getCoordinateSystem().getAxis(0).getDirection())) {
 
             gc.setStartingPosition(new DirectPosition2D(crs,
                                    mapBounds.getMinimum(0), mean1));
             gc.setDestinationPosition(new DirectPosition2D(crs,
                         mapBounds.getMinimum(0) + mapBounds.getWidth() / 3.0d,
                         mean1));
 
             widthInMeters = gc.getOrthodromicDistance() * 3.0d;
 
             gc.setStartingPosition(new DirectPosition2D(crs,
                                    mean0, mapBounds.getMinimum(1)));
             gc.setDestinationPosition(new DirectPosition2D(crs, mean0,
                     mapBounds.getMinimum(1) + mapBounds.getHeight() / 3.0d));
 
             heightInMeters = gc.getOrthodromicDistance() * 3.0d;
 
         } else {
             throw new IllegalArgumentException("unsupported CRS axis setup");
         }
 
         double dotInMeters = 0.0254d / dpi;
         double imageWidth = widthInMeters / (scale * dotInMeters);
         double heightToWidth = heightInMeters / widthInMeters;
         imageBounds = new Rectangle(
                             0, 0, (int) imageWidth,
                             (int) Math.round(imageWidth * heightToWidth));
 
         return imageBounds;
     }
 
     /**
      * Parse the map coverage string parameter and create a WGS84 referenced
      * envelope out of it.
      *
      * @param coverageStr the coverage string to parse, in A,B,C,D format,
      *        which is coordinates in degrees
      * @return the corresponding referenced envelope
      */
     private static ReferencedEnvelope
     parseCoverage(String coverageStr) {
 
         StringTokenizer tok = new StringTokenizer(coverageStr, ",");
         if (tok.countTokens() != 4) {
             throw new IllegalArgumentException(
                     "incorrect coverage string: " + coverageStr);
         }
 
         double x1 = Double.parseDouble(tok.nextToken());
         double y1 = Double.parseDouble(tok.nextToken());
         double x2 = Double.parseDouble(tok.nextToken());
         double y2 = Double.parseDouble(tok.nextToken());
 
         ReferencedEnvelope coverage = new ReferencedEnvelope(
                                            x1, x2, y1, y2,
                                            DefaultGeographicCRS.WGS84);
 
         return coverage;
     }
 
 }
