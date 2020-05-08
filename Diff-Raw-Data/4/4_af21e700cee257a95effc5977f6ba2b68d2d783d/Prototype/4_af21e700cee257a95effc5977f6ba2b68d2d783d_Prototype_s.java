 package com.lisasoft.face;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JToolBar;
 
 import org.geotools.coverage.GridSampleDimension;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
 import org.geotools.coverage.grid.io.AbstractGridFormat;
 import org.geotools.coverage.grid.io.GridFormatFinder;
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultRepository;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.geotools.map.FeatureLayer;
 import org.geotools.map.GridReaderLayer;
 import org.geotools.map.MapContent;
 import org.geotools.map.MapContext;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.renderer.lite.StreamingRenderer;
 import org.geotools.styling.ChannelSelection;
 import org.geotools.styling.ContrastEnhancement;
 import org.geotools.styling.RasterSymbolizer;
 import org.geotools.styling.SLD;
 import org.geotools.styling.SelectedChannelType;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleFactory;
 import org.geotools.swing.JMapPane;
 import org.geotools.swing.action.ZoomInAction;
 import org.geotools.swing.action.ZoomOutAction;
 import org.geotools.swing.table.FeatureCollectionTableModel;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.FilterFactory2;
 import org.opengis.style.ContrastMethod;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 
 /**
  * This is a prototype application *just* showing how to integrate a MapComponent with an existing
  * application.
  * <p>
  * As such the details of this application are not all that interesting; they do serve to illustrate
  * how to:
  * <ol>
  * <li>set up a MapContent (this is used as the background for the MapComponent)</li>
  * <li>set up a MapComponent (this is actually a simple JPanel consisting of a JMapPane</li>
  * <li>set up a toolbar using some of the actions available for controlling MapComponent</li>
  * </ul> * In all cases this is straight forward application of the components provided by GeoTools.
  * <p>
  * Here is the interesting bit:
  * <ul>
  * <li>set up a MapComponentTable (actually a small JPanel consisting of a JTable working against
  * the MapComponent table model)</li>
  * <li>Custom table model; just backed by the Faces provided to MapComponentTable</li>
  * <li>Custom tool to "select" Faces in the MapComponent; this will update both update an internal
  * *filter* used to display selected faces; and update a list selection model published for use with
  * MapComponentTable.</li>
  * <li>Custom DataStore used to present the Faces provided to MapComponent to the GeoTools rendering
  * system. This is used by *two* layers. One layer to display the sites; and a second to display the
  * selection (These two lays are added to the MapContent).</li>
  * </ul>>
  * 
  * Implementation Notes:
  * <ul>
  * <li>SH: is creating the layout of this form using Eclipse 3.7 window builder tools (this is not
  * really important or interesting; just FYI)</li>
  * </ul>
  * 
  * @author Scott Henderson (LISASoft)
  * @author Jody Garnett (LISASoft)
  */
 public class Prototype extends JFrame {
     /** serialVersionUID */
     private static final long serialVersionUID = -1415741029620524123L;
 
     /**
      * Used to create GeoTools styles; based on OGC Style Layer Descriptor specification.
      */
     private StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
 
     /**
      * Used to create GeoTools filters; to query data.
      */
     private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
 
     /**
      * Table used to list Face content.
      */
     private JTable table;
 
     /**
      * Small toolbar configured with both JMapPane tools (for panning and zooming) and custom tools
      * to select Face content.
      */
     private JToolBar toolBar;
 
     /**
      * Repository used to hold on to DataStores.
      */
     private DefaultRepository repo;
 
     /** Used to hold on to rasters */
     private Map<String, AbstractGridCoverage2DReader> raster;
 
     /**
      * This is the mapcontent used as a backdrop for mapComponent
      */
     private MapContent map;
 
     private SimpleFeatureCollection faces;
 
     /**
      * Create a Prototype Frame; please call init() to configure.
      * <p>
      * How to use:
      * 
      * <pre>
      * Prototype prototype = new Prototype();
      * // any configuration here
      * init();
      * show();
      * </pre>
      * 
      * Subclasses can override init() or set key methods inorder to control or experiment with how
      * this class functions. These methods are set up to show how to perform common tasks.
      */
     private Prototype() {
         super("AGP Prototype");
         repo = new DefaultRepository();
 
         raster = new LinkedHashMap<String, AbstractGridCoverage2DReader>();
     }
 
     /**
      * Prompts the user for a GeoTIFF file and a Shapefile and passes them to the displayLayers
      * method Usual protected init method called from the constructor(); subclasses can override key
      * methods in order to takepart in configuration.
      * <ul>
      * <li>loadData() - load data into a repository
      * <li>createMap() - create a MapContent
      * <li>loadSites() - load site information
      * <li>initUserInterface() - layout user interface components; this will create the MapComponent and
      * connect it to the required data model etc...
      * </ul>
      */
     protected void init() throws Exception {
         loadData();
         loadSites();
         map = createMap(repo, raster);
         initUserInterface();
     }
 
     /**
      * Used to laod data; any DataStore's laoded should be registered in a repository (so they can
      * be cleaned up).
      */
     private void loadData() {
         File directory = new File(".");
         if (directory.exists() && directory.isDirectory()) {
             // check for shapefiles
             //
             File[] shapefiles = directory.listFiles(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.toUpperCase().endsWith(".SHP");
                 }
             });
             for (File shp : shapefiles) {
                 try {
                     FileDataStore dataStore = FileDataStoreFinder.getDataStore(shp);
                     if (dataStore != null) {
                         for (String typeName : dataStore.getTypeNames()) {
                             repo.register(typeName, dataStore);
                         }
                     }
                 } catch (IOException eek) {
                     System.err.println("Unable to load shapefile " + shp + ":" + eek);
                     eek.printStackTrace(System.err);
                 }
             }
             // check for geotiff files
             File[] tiffFiles = directory.listFiles(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.toUpperCase().endsWith(".TIF")
                             || name.toUpperCase().endsWith(".TIFF");
                 }
             });
             for (File tif : tiffFiles) {
                 try {
                     AbstractGridFormat format = GridFormatFinder.findFormat(tif);
                     AbstractGridCoverage2DReader reader = format.getReader(tif);
                     if (reader == null) {
                         System.err.println("Unable to load " + tif);
                         continue;
                     }
                     String fileName = tif.getName();
                     String name = fileName.substring(0, fileName.lastIndexOf(".") - 1);
                     raster.put(name, reader);
                 } catch (Throwable eek) {
                     System.err.println("Unable to load " + tif + ":" + eek);
                     eek.printStackTrace(System.err);
                 }
 
             }
         }
     }
 
     /**
      * Displays a GeoTIFF file overlaid with a Shapefile
      * 
      * @param rasterFile the GeoTIFF file
      * @param shpFile the Shapefile
      */
     private MapContent createMap(DefaultRepository repo2,
             Map<String, AbstractGridCoverage2DReader> raster2) {
 
         // Set up a MapContext with the two layers
         final MapContent map = new MapContent();
 
         // use rasters as "basemap"
         for (Entry<String, AbstractGridCoverage2DReader> entry : raster.entrySet()) {
             // Initially display the raster in greyscale using the
             // data from the first image band
             String name = entry.getKey();
             AbstractGridCoverage2DReader reader = entry.getValue();
 
             Style style = createStyle(reader);
             GridReaderLayer layer = new GridReaderLayer(reader, style);
             if (reader.getInfo() != null && reader.getInfo().getTitle() != null) {
                 layer.setTitle(reader.getInfo().getTitle());
             }
             map.addLayer(layer);
         }
         // add shapefiles on top
         for (DataStore dataStore : repo.getDataStores()) {
             
             try {
                 for( String typeName : dataStore.getTypeNames() ){
                     SimpleFeatureSource featureSource = dataStore.getFeatureSource( typeName );
 
                     // Create a basic style with yellow lines and no fill
                     Style style = SLD.createPolygonStyle(Color.RED, null, 0.0f);
 
                     FeatureLayer layer = new FeatureLayer( featureSource, style );
                     
                     if (featureSource.getInfo() != null && featureSource.getInfo().getTitle() != null) {
                         layer.setTitle(featureSource.getInfo().getTitle());
                     }
 
                     map.addLayer( layer );
                 }
             } catch (IOException e) {
                 System.err.print("Could not load "+dataStore );
             }
         }
 
         // configure map
         map.setTitle("Prototype");
         
         return map;
 
     }
 
     /*
      * We create a FeatureCollection into which we will put each Feature created from a record in
      * the input csv data file
      */
     protected void loadSites() {
         File csvFile = new File("data/locations.csv");
 
         if (csvFile.exists()) {
             try {
                 faces = getFeaturesFromFile(csvFile);
             } catch (Throwable eek) {
                 System.out.println("Could not load faces:" + eek);
             }
         }
     }
 
     @SuppressWarnings("deprecation")
     private void initUserInterface() {
         getContentPane().setLayout(new BorderLayout());
 
         JScrollPane scrollPane = new JScrollPane(table);
         getContentPane().add(scrollPane, BorderLayout.SOUTH);
 
         // scott this is the collection from csv, read this display table
         if (faces != null) {
             FeatureCollectionTableModel model = new FeatureCollectionTableModel(faces);
             table = new JTable();
             table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
             table.setPreferredScrollableViewportSize(new Dimension(800, 200));
             table.setModel(model);
         }
         /*
          * mapFrame.setSize(800, 600); mapFrame.enableStatusBar(true);
          * //frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
          * mapFrame.enableToolBar(true);
          * 
          * JMenuBar menuBar = new JMenuBar(); mapFrame.setJMenuBar(menuBar); JMenu menu = new
          * JMenu("Raster"); menuBar.add(menu);
          * 
          * menu.add( new SafeAction("Grayscale display") { public void action(ActionEvent e) throws
          * Throwable { Style style = createGreyscaleStyle(); if (style != null) {
          * map.getLayer(0).setStyle(style); mapFrame.repaint(); } } });
          * 
          * menu.add( new SafeAction("RGB display") { public void action(ActionEvent e) throws
          * Throwable { Style style = createRGBStyle(); if (style != null) {
          * map.getLayer(0).setStyle(style); mapFrame.repaint(); } } });
          */
 
         JMapPane mapPane = new JMapPane();
 
         // set a renderer to use with the map pane
         mapPane.setRenderer(new StreamingRenderer());
 
         // set the map context that contains the layers to be displayed
         mapPane.setMapContext( new MapContext( map ));
         mapPane.setSize(800, 500);
 
         toolBar = new JToolBar();
         toolBar.setOrientation(JToolBar.HORIZONTAL);
         toolBar.setFloatable(false);
 
         ButtonGroup cursorToolGrp = new ButtonGroup();
 
         JButton zoomInBtn = new JButton(new ZoomInAction(mapPane));
         toolBar.add(zoomInBtn);
         // cursorToolGrp.add(zoomInBtn);
 
         JButton zoomOutBtn = new JButton(new ZoomOutAction(mapPane));
         toolBar.add(zoomOutBtn);
         toolBar.setSize(800, 100);
         // cursorToolGrp.add(zoomOutBtn);
 
        getContentPane().add(toolBar, BorderLayout.CENTER);
        getContentPane().add(mapPane, BorderLayout.SOUTH);
         // mapFrame.setVisible(true);
     }
 
     /**
      * Create a Style to display the specified band of the GeoTIFF image as a greyscale layer.
      * <p>
      * This method is a helper for createGreyScale() and is also called directly by the
      * displayLayers() method when the application first starts.
      * 
      * @param band the image band to use for the greyscale display
      * 
      * @return a new Style instance to render the image in greyscale
      */
     private Style createGreyscaleStyle(int band) {
         ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
         SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(band), ce);
 
         RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
         ChannelSelection sel = sf.channelSelection(sct);
         sym.setChannelSelection(sel);
 
         return SLD.wrapSymbolizers(sym);
     }
 
     /**
      * This method examines the names of the sample dimensions in the provided coverage looking for
      * "red...", "green..." and "blue..." (case insensitive match). If these names are not found it
      * uses bands 1, 2, and 3 for the red, green and blue channels. It then sets up a raster
      * symbolizer and returns this wrapped in a Style.
      * 
      * @param reader
      * 
      * @return a new Style object containing a raster symbolizer set up for RGB image
      */
     private Style createStyle(AbstractGridCoverage2DReader reader) {
         GridCoverage2D cov = null;
         try {
             cov = reader.read(null);
         } catch (IOException giveUp) {
             throw new RuntimeException(giveUp);
         }
         // We need at least three bands to create an RGB style
         int numBands = cov.getNumSampleDimensions();
         if (numBands < 3) {
             // assume the first brand
             return createGreyscaleStyle(1);
         }
         // Get the names of the bands
         String[] sampleDimensionNames = new String[numBands];
         for (int i = 0; i < numBands; i++) {
             GridSampleDimension dim = cov.getSampleDimension(i);
             sampleDimensionNames[i] = dim.getDescription().toString();
         }
         final int RED = 0, GREEN = 1, BLUE = 2;
         int[] channelNum = { -1, -1, -1 };
         // We examine the band names looking for "red...", "green...", "blue...".
         // Note that the channel numbers we record are indexed from 1, not 0.
         for (int i = 0; i < numBands; i++) {
             String name = sampleDimensionNames[i].toLowerCase();
             if (name != null) {
                 if (name.matches("red.*")) {
                     channelNum[RED] = i + 1;
                 } else if (name.matches("green.*")) {
                     channelNum[GREEN] = i + 1;
                 } else if (name.matches("blue.*")) {
                     channelNum[BLUE] = i + 1;
                 }
             }
         }
         // If we didn't find named bands "red...", "green...", "blue..."
         // we fall back to using the first three bands in order
         if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
             channelNum[RED] = 1;
             channelNum[GREEN] = 2;
             channelNum[BLUE] = 3;
         }
         // Now we create a RasterSymbolizer using the selected channels
         SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
         ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
         for (int i = 0; i < 3; i++) {
             sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
         }
         RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
         ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
         sym.setChannelSelection(sel);
 
         return SLD.wrapSymbolizers(sym);
     }
 
     /**
      * Here is how you can use a SimpleFeatureType builder to create the schema for your shapefile
      * dynamically.
      * <p>
      * This method is an improvement on the code used in the main method above (where we used
      * DataUtilities.createFeatureType) because we can set a Coordinate Reference System for the
      * FeatureType and a a maximum field length for the 'name' field dddd
      */
     private static SimpleFeatureType createFeatureType() {
 
         SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
         builder.setName("Location");
         builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
 
         // add attributes in order
         builder.add("Location", Point.class);
         builder.length(15).add("Name", String.class); // <- 15 chars width for name field
         builder.length(15).add("Number", Integer.class); // <- 15 chars width for name field
 
         // build the type
         final SimpleFeatureType LOCATION = builder.buildFeatureType();
 
         return LOCATION;
     }
 
     /**
      * Here is how you can use a SimpleFeatureType builder to create the schema for your shapefile
      * dynamically.
      * <p>
      * This method is an improvement on the code used in the main method above (where we used
      * DataUtilities.createFeatureType) because we can set a Coordinate Reference System for the
      * FeatureType and a a maximum field length for the 'name' field dddd
      */
     private SimpleFeatureCollection getFeaturesFromFile(File csvFile) throws Exception {
 
         final SimpleFeatureType TYPE = createFeatureType();
 
         /*
          * We create a FeatureCollection into which we will put each Feature created from a record
          * in the input csv data file
          */
         SimpleFeatureCollection collection = FeatureCollections.newCollection();
         /*
          * GeometryFactory will be used to create the geometry attribute of each feature (a Point
          * object for the location)
          */
         GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
 
         SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
 
         BufferedReader csvReader = new BufferedReader(new FileReader(csvFile));
         try {
             /* First line of the data file is the header */
             String line = csvReader.readLine();
             System.out.println("Header: " + line);
 
             for (line = csvReader.readLine(); line != null; line = csvReader.readLine()) {
                 if (line.trim().length() > 0) { // skip blank lines
                     String tokens[] = line.split("\\,");
 
                     double latitude = Double.parseDouble(tokens[0]);
                     double longitude = Double.parseDouble(tokens[1]);
                     String name = tokens[2].trim();
                     int number = Integer.parseInt(tokens[3].trim());
 
                     /* Longitude (= x coord) first ! */
                     Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
 
                     featureBuilder.add(point);
                     featureBuilder.add(name);
                     featureBuilder.add(number);
                     SimpleFeature feature = featureBuilder.buildFeature(null);
                     collection.add(feature);
                 }
             }
         } finally {
             csvReader.close();
         }
 
         return collection;
     }
 
     /**
      * Opens the prototype user interface.
      * <p>
      * Please note any shapefiles or raster files in the current directory will be used as a
      * background.
      * 
      * @param args
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
         Prototype app = new Prototype();
 
         // configuration
         app.init();
 
         // display
         app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         app.setSize(900, 900);
         app.setVisible(true);
     }
 }
