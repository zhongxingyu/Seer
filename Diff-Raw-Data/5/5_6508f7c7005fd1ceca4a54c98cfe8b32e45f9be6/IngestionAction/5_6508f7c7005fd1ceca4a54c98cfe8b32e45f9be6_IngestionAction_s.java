 /*
  *  GeoBatch - Open Source geospatial batch processing system
  *  https://github.com/nfms4redd/nfms-geobatch
  *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.geobatch.unredd.script.ingestion;
 
 //import it.geosolutions.geobatch.unredd.script.util.rasterize.Rasterize;
 import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
 import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
 import it.geosolutions.geobatch.annotations.Action;
 import it.geosolutions.geobatch.flow.event.action.ActionException;
 import it.geosolutions.geobatch.flow.event.action.BaseAction;
 import it.geosolutions.geobatch.unredd.script.exception.FlowException;
 import it.geosolutions.geobatch.unredd.script.exception.PostGisException;
 import it.geosolutions.geobatch.unredd.script.model.Request;
 import it.geosolutions.geobatch.unredd.script.util.FlowUtil;
 import it.geosolutions.geobatch.unredd.script.util.GeoStoreUtil;
 import it.geosolutions.geobatch.unredd.script.util.GeoTiff;
 import it.geosolutions.geobatch.unredd.script.util.Mosaic;
 import it.geosolutions.geobatch.unredd.script.util.PostGISUtils;
 import it.geosolutions.geobatch.unredd.script.util.RequestJDOMReader;
 import it.geosolutions.geobatch.unredd.script.util.rasterize.GDALRasterize;
 import it.geosolutions.geostore.core.model.Resource;
 import it.geosolutions.unredd.geostore.model.UNREDDFormat;
 import it.geosolutions.unredd.geostore.model.UNREDDLayer;
 import it.geosolutions.unredd.geostore.model.UNREDDLayer.Attributes;
 import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
 import it.geosolutions.unredd.geostore.utils.NameUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.apache.commons.compress.archivers.ArchiveInputStream;
 import org.apache.commons.compress.archivers.ArchiveStreamFactory;
 import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.geotools.gce.geotiff.GeoTiffReader;
 import org.opengis.coverage.grid.GridEnvelope;
 import org.opengis.geometry.Envelope;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * This single Action contains the complete Ingestion flow.
  *
  * @author Luca Paolino - luca.paolino@geo-solutions.it
  */
 @Action(configurationClass=IngestionConfiguration.class)
 public class IngestionAction extends BaseAction<FileSystemEvent> {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(IngestionAction.class);
 
     private final IngestionConfiguration cfg;
 
     private static final String INFO_XML = "info.xml";
     
     private static final String DEFAULT_MOSAIC_STYLE = "raster";
     
     private static final String DATA_DIR_NAME="data";
 
     public IngestionAction(IngestionConfiguration configuration)
             throws ActionException {
         super(configuration);
 
           this.cfg = configuration;
     }
 
     /**
      * Main loop on input files.
      * Single file processing is called on execute(File inputZipFile)
      */
     public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
             throws ActionException {
 
         final Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();
         LOGGER.warn("Ingestion flow running");
 
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Performing basic checks");
         }
         // control if directories and PostGISUtils exist
         basicChecks();
 
 
         while (!events.isEmpty()) {
             final FileSystemEvent ev = events.remove();
 
             try {
                 if (ev != null) {
                     if (LOGGER.isTraceEnabled()) {
                         LOGGER.trace("Processing incoming event: " + ev.getSource());
                     }
 
                     File inputZipFile = ev.getSource(); // this is the input zip file
                     File out = execute(inputZipFile);
                     ret.add(new FileSystemEvent(out, FileSystemEventType.FILE_ADDED));
 
                 } else {
                     LOGGER.error("NULL event: skipping...");
                     continue;
                 }
 
             } catch (ActionException ex) { // ActionEx have already been processed
                 LOGGER.error(ex.getMessage(), ex);
                 throw ex;
 
             } catch (Exception ex) {
                 final String message = "GeostoreAction.execute(): Unable to produce the output: "
                         + ex.getLocalizedMessage();
                 LOGGER.error(message, ex);
                 throw new ActionException(this, message);
             }
         }
 
         return ret;
     }
 
     /**
      * Performs some basic checks on configuration values.
      *
      * @throws ActionException
      */
     public void basicChecks() throws ActionException {
 
         if ( cfg.getOriginalDataTargetDir() == null || ! cfg.getOriginalDataTargetDir().canWrite() || ! cfg.getOriginalDataTargetDir().isDirectory()){
             LOGGER.warn("OriginalDataTargetDir is not setted or has been wrong specified or GeoBatch doesn't have write permissions");
         }
 
         if(cfg.getRetilerConfiguration() == null){
             throw new ActionException(this, "RetilerConfiguration not set");
         }
 
         if(cfg.getGeoStoreConfig() == null){
             throw new ActionException(this, "GeoStoreConfiguration not set");
         }
 
         if(cfg.getPostGisConfig() == null){
             throw new ActionException(this, "PostGisConfiguration not set");
         }
 
         if(cfg.getGeoServerConfig() == null){
             LOGGER.warn("GeoServer config is null. GeoServer data will not be refreshed");
         }
     }
 
 
 
     protected File execute(File inputZipFile) throws ActionException, IOException {
 
         this.listenerForwarder.started();
 
 
         /******************
          * Extract information from the zip file
          *
          ****************/
         this.listenerForwarder.progressing(5, "Unzipping input file ");
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Extracting files from " + inputZipFile);
         }
 
         File unzipDir = new File(getTempDir(), "unzip");
         unzipDir.mkdir();
         unzipInputFile(inputZipFile, unzipDir); // throws ActionException
 
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Extraction successfully completed");
         }
         this.listenerForwarder.progressing(10, "File unzipped");
 
         return executeUnzipped(unzipDir);
     }
 
     protected File executeUnzipped(File unzipDir) throws ActionException, IOException {
 
 
         /*************
          *
          * read the content of the XML file
          *
          ***********/
         this.listenerForwarder.progressing(10, "Parsing " + INFO_XML);
 
         File infoXmlFile = new File(unzipDir, INFO_XML);
 
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Reading XML parameters from " + infoXmlFile);
         }
         Request request = null;
         try {
             request = RequestJDOMReader.parseFile(infoXmlFile);
         } catch (Exception e) {
             throw new ActionException(this, "Error reading info.xml file, Are you sure to have built the input zip pkg in the right way? Note that all the content must be placed in the zip root folder, no any other subfolder are allowed..." , e);
         }
 
         if(request.getFormat() == null) {
             throw new ActionException(this, "the format cannot be null.");
         }
 
         final String layername = request.getLayername();
         if (layername==null)
             throw new ActionException(this, "the layername cannot be null.");
 
         final String year = request.getYear();
         if (year==null)
             throw new ActionException(this, "the year cannot be null.");
 
         if( ! year.matches("\\d{4}")) {
             throw new ActionException(this, "Bad format for year parameter ("+year+")");
         }
 
         final String month = request.getMonth();
 
         if(month!= null && ! month.matches("\\d\\d?") )
             throw new ActionException(this, "Bad format for month parameter ("+month+")");
         
         final String day = request.getDay();
 
         if(month!= null && ! month.matches("\\d\\d?") )
             throw new ActionException(this, "Bad format for month parameter ("+day+")");
 
         final String srcFilename = request.buildFileName();
 
         // build the name of the snapshot
         final String layerUpdateName = NameUtils.buildLayerUpdateName(layername, year, month, day);
 
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Info: layername:" + layername + " year:"+year + " month:"+month + " day:"+day);
         }
         this.listenerForwarder.progressing(12, "Info from xml file: layername:" + layername + " year:"+year + " month:"+month + " day:"+day);
 
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("XML parameter settings : [layer name = " + layername + "], [year = " + year + "], [month = " + month + "], [day = " + day + "], [ file name = " + srcFilename + "]");
             LOGGER.debug("XML parameter settings : [layer update name = " + layerUpdateName + "]");
         }
 
         File unzippedDataDir = new File(unzipDir, DATA_DIR_NAME);
         File dataFile = new File(unzippedDataDir, srcFilename);
 
         if( ! dataFile.isFile()) {
             throw new ActionException(this, "Could not read main data file " + dataFile);
         }
 
         /*****************/
         GeoStoreUtil geostore = new GeoStoreUtil(cfg.getGeoStoreConfig(), this.getTempDir());
 
         /******************
          *  Load Layer data
          ******************/
 
         this.listenerForwarder.progressing(15, "Searching layer in GeoStore");
 
         final Resource layerRes;
         try {
             layerRes = geostore.searchLayer(layername);
         } catch(Exception e) {
             throw new ActionException(this, "Error loading Layer "+layername, e);
         }
 
         if(layerRes == null)
             throw new ActionException(this, "Layer not found: "+layername);
 
         UNREDDLayer layer = new UNREDDLayer(layerRes);
 
         LOGGER.info("Layer resource found ");
 
         if( ! layer.getAttribute(Attributes.LAYERTYPE).equalsIgnoreCase(request.getFormat().getName()))
             throw new ActionException(this, "Bad Layer format "
                     + "(declared:"+ request.getFormat().getName()
                     + ", expected:"+layer.getAttribute(Attributes.LAYERTYPE) );
 
         // this attribute is read for moving the raster file to the destination directory, not for rasterization
         String mosaicDirPath = layer.getAttribute(UNREDDLayer.Attributes.MOSAICPATH);
         if( mosaicDirPath == null) {
            throw new ActionException(this, "Null mosaic directory for layer:"+layername);
         }
 
         File mosaicDir = new File(mosaicDirPath);
         if( ! mosaicDir.isDirectory() && ! mosaicDir.isAbsolute()) {
            throw new ActionException(this, "Bad mosaic directory for layer:"+layername+": " + mosaicDir);
         }
 
         // ******************
         // Check for LayerUpdate
         // ******************
         this.listenerForwarder.progressing(20, "Check for existing LayerUpdate in GeoStore");
 
         Resource existingLayerUpdate = null;
         try {
             existingLayerUpdate = geostore.searchLayerUpdate(layername, year, month, day);
         } catch (Exception e) {
             LOGGER.debug("Parameter : [layerSnapshot=" + layerUpdateName + "]");
             throw new ActionException(this, "Error searching for a LayerUpdate (layer:"+layername+" year:"+year+ " month:"+month+")", e);
         }
 
         if (existingLayerUpdate != null) {
             throw new ActionException(this, "LayerUpdate already exists (layer:"+layername+" year:"+year+ " month:"+month+")");
         }
 
         /********************************
          *
          * Image processing
          *
          *******************************/
         final File rasterFile;
         if (request.getFormat() == UNREDDFormat.VECTOR ) {
             rasterFile = processVector(dataFile, layername, year, month, day, layer, mosaicDir);
 
         } else {
             rasterFile = processRaster(dataFile, layer, mosaicDir, layername);
 
         }
 
         // *** Image processing has finished
 
         // ********************
         // Create LayerUpdate
         // ********************
 
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Adding LayerUpdate into GeoStore");
         }
         this.listenerForwarder.progressing(70, "Adding LayerUpdate into GeoStore");
 
         try {
             geostore.insertLayerUpdate(layername, year, month, day);
         } catch (Exception e) {
             LOGGER.debug("Parameter : [layername=" + layername + ", year=" + year + ", month=" + month + "]");
             throw new ActionException(this, "Error while inserting a LayerUpdate", e);
         }
 
         // ********************
         // Run stats
         // ********************
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Starting statistic processing");
         }
         this.listenerForwarder.progressing(80, "Starting statistic processing");
 
         FlowUtil flowUtil= new FlowUtil(getTempDir(), getConfigDir());
         try {
             flowUtil.runStatsAndScripts(layername, year, month, day, rasterFile, geostore);
         } catch (FlowException e) {
             throw new ActionException(this, e.getMessage(), e);
         }
 
         /*************************
          * Copy orig data
          *************************/
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Moving original data");
         }
         this.listenerForwarder.progressing(90, "Moving original data");
 
         LOGGER.warn("*** TODO: move original files"); // TODO
 
 //        File srcDir = new File(unzipPath, ORIG_DIR);
 //        if (!srcDir.exists()) {
 //            LOGGER.warn("Original data not found"); // no problem in this case
 //        } else {
 //            File destDirRelative = new File(cfg.repositoryDir, destRelativePath);
 //            File destDirComplete = new File(destDirRelative, layerUpdateName);
 //            LOGGER.info("Moving "+srcDir.getCanonicalPath()+" to "+destDirComplete.getAbsolutePath());
 //            FileUtils.copyDirectoryToDirectory(srcDir, destDirComplete);
 //        }
 
         // finish action
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Ingestion action succesfully completed");
         }
         this.listenerForwarder.completed();
         this.listenerForwarder.progressing(100, "Action successfully completed");
 
         //*******************************************************************
 //        postgisUtils.getPgDatastore().dispose(); // shouldnt it be run in a finally{} block?
 
         // add the event to the return queue
         return rasterFile;
     }
 
     /**
      * Process raster data.
      * <OL>
      * <LI>check extent</LI>
      * <LI>check pixel size</LI>
      * <LI>retile</LI>
      * <LI>create overviews</LI>
      * <LI>call ImageMosaic action</LI>
      *  <OL>
      *      <LI>copy raster into mosaic dir</LI>
      *      <LI>add granule in tile db</LI>
      *      <LI>refresh geoserver cache</LI>
      * </OL>
      * </OL>
      *
      * @param dataFile input raster
      * @param layer
      * @param mosaicDir
      * @param layername
      * @return
      * @throws ActionException
      * @throws NumberFormatException
      */
     protected File processRaster(File dataFile, UNREDDLayer layer, File mosaicDir, final String layername) throws ActionException, NumberFormatException {
         this.listenerForwarder.progressing(25, "Checking raster extents");
         checkRasterSize(dataFile, layer);
 
         File rasterFile;
         
         try {
             LOGGER.info("Starting retiling for " + dataFile);
             this.listenerForwarder.progressing(30, "Starting retiling");
 
             rasterFile = GeoTiff.retile(cfg.getRetilerConfiguration(), dataFile, getTempDir()); // retile replaces the original input file
 
             LOGGER.info("Retiling completed into " + rasterFile);
 
         } catch (Exception e) {
             throw new ActionException(this, "Error while retiling " + dataFile, e);
         }
         // === embedOverviews
         LOGGER.info("Starting overviews");
         this.listenerForwarder.progressing(40, "Starting overviews");
         try {
             rasterFile = GeoTiff.embedOverviews(cfg.getOverviewsEmbedderConfiguration(), rasterFile, getTempDir());
         } catch (Exception e) {
             throw new ActionException(this, "Error creating overviews on " + rasterFile, e);
         }
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Geoserver mosaic update started "+cfg);
         }
         this.listenerForwarder.progressing(60, "Adding new time coordinate in GeoServer");
         // Publish data on GeoServer
         // mosaic action will
         //  - copy the raster into the mosaic dir (with current filename)
         //  - add the granule in the tile db
         try {
             
             String style = layer.getAttribute(UNREDDLayer.Attributes.LAYERSTYLE);
             if(style==null || style.isEmpty()){
                 style = DEFAULT_MOSAIC_STYLE;
             }
             StringBuilder msg = new StringBuilder();
             msg.append("Publishing the Mosaic Granule with Style -> ");
             msg.append(style);
             LOGGER.info(msg.toString());
             
             double [] bbox = new double[4];
             bbox[0] = Double.valueOf(layer.getAttribute(Attributes.RASTERX0));
             bbox[1] = Double.valueOf(layer.getAttribute(Attributes.RASTERY0));
             bbox[2] = Double.valueOf(layer.getAttribute(Attributes.RASTERX1));
             bbox[3] = Double.valueOf(layer.getAttribute(Attributes.RASTERY1));
             
             Mosaic mosaic = new Mosaic(cfg.getGeoServerConfig(), mosaicDir, getTempDir(), getConfigDir());
             mosaic.add(cfg.getGeoServerConfig().getWorkspace(), layername, rasterFile, "EPSG:4326", bbox, style, cfg.getDatastorePath());
         
         } catch (Exception e) {
             this.listenerForwarder.progressing(60, "Error in ImageMosaic: " + e.getMessage());
             LOGGER.error("Error in ImageMosaic: " + e.getMessage(), e);
             throw new ActionException(this, "Error updating mosaic " + rasterFile.getName(), e);
         }
         
         File expectedMosaicTile = new File(mosaicDir, dataFile.getName());
         if(expectedMosaicTile.exists()) {
             if(LOGGER.isInfoEnabled()) {
                 LOGGER.info("Mosaic granule is in the mosaic dir: " + expectedMosaicTile);
             }
         } else {
             LOGGER.error("Mosaic granule not found: " + expectedMosaicTile);
         }
 
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Geoserver mosaic update completed");
         }
         return rasterFile;
     }
 
     /**
      * Process vector data.
      * <OL>
      * <LI>check attributes matching</LI>
      * <LI>copy shp into pg</LI>
      * <LI>rasterize</LI>
      * <LI>create overviews</LI>
      * <LI>copy raster into mosaic dir</LI>
      * </OL>
      *
      * @param dataFile the shapefile to be processes
      * @param layername the bare  name of the layer
      * @param year the 4-digits year
      * @param month the month, may be null
      * @param layer UNREDDLayer info, contains data about rasterization
      * @param mosaicDir destination directory
      *
      * @return the raster File, ready to be used for computing stats
      *
      * @throws ActionException
      * @throws IOException
      */
     protected File processVector(File dataFile, final String layername, final String year, final String month, final String day,  UNREDDLayer layer, File mosaicDir) throws ActionException, IOException {
         LOGGER.info("Starting PostGIS ingestion for " + dataFile);
         this.listenerForwarder.progressing(25, "Starting PostGIS ingestion");
         try {
             int cp = PostGISUtils.shapeToPostGis(dataFile, cfg.getPostGisConfig(), layername, year, month, day);
             this.listenerForwarder.progressing(29, "Copied "+cp+ " features");
         } catch (PostGisException e) {
             LOGGER.error("Error ingesting shapefile: " + e.getMessage());
             throw new ActionException(this, "Error ingesting shapefile: " + e.getMessage(), e);
         }
 
         File rasterFile;
         LOGGER.info("Starting rasterization");
         this.listenerForwarder.progressing(30, "Starting rasterization");
         try {
             GDALRasterize rasterize = new GDALRasterize( cfg.getRasterizeConfig(), cfg.getConfigDir(), getTempDir());
             rasterFile = rasterize.run(layer, new UNREDDLayerUpdate(layername, year, month, day), dataFile);
         } catch (Exception e) {
             throw new ActionException(this, "Error while rasterizing "+dataFile+": " + e.getMessage(), e);
         }
 
         LOGGER.info("Starting overviews");
         this.listenerForwarder.progressing(40, "Starting overviews");
         try {
             rasterFile = GeoTiff.embedOverviews(cfg.getOverviewsEmbedderConfiguration(), rasterFile, getTempDir());
         } catch (Exception e) {
             throw new ActionException(this, "Error creating overviews on " + rasterFile, e);
         }
 
         //=== Move final raster file into its mosaic dir
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Moving raster file into mosaic dir");
         }
         this.listenerForwarder.progressing(50, "Copying raster into mosaic dir");
         String finalRasterName = NameUtils.buildTifFileName(layername, year, month, day);
         File destFile = new File(mosaicDir, finalRasterName);
         if(destFile.exists())
             throw new ActionException(this, "Destination file in mosaic dir already exists " + destFile);
         FileUtils.copyFile(rasterFile, destFile);
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Raster file moved into mosaic dir: " + destFile);
         }
         return rasterFile;
     }
 
     /**
      * @param inputZipFile
      * @param unzipDir
      * @throws ActionException
      */
     public void unzipInputFile(File inputZipFile, File unzipDir) throws ActionException {
         LOGGER.debug("Unzipping " + inputZipFile + " into " + unzipDir);
         ArchiveInputStream in = null;
         try {
             final InputStream is = new FileInputStream(inputZipFile);
             in = new ArchiveStreamFactory().createArchiveInputStream("zip", is);
             ZipArchiveEntry entry;
             while( (entry = (ZipArchiveEntry) in.getNextEntry()) != null) {
                 File currentFile = new File(unzipDir, entry.getName());
                 if(entry.isDirectory()) {
                     LOGGER.info("Unzipping dir  " + entry);
                     FileUtils.forceMkdir(currentFile);
                 } else {
                     LOGGER.info("Unzipping file " + entry);
                     //create parent dir if needed
                     File parent = currentFile.getParentFile();
                     if( ! parent.exists()) {
                         if(LOGGER.isInfoEnabled())
                             LOGGER.info("Forcing creation of parent dir  " + parent);
                         FileUtils.forceMkdir(parent);
                     }
 
                     OutputStream out = new FileOutputStream(currentFile);
                     IOUtils.copy(in, out);
                     out.flush();
                     IOUtils.closeQuietly(out);
                 }
             }
 
             LOGGER.info("Zip extracted in " + unzipDir);
 
         } catch (Exception e) {
             throw new ActionException(this, "Error extracting from " + inputZipFile, e);
         } finally {
             IOUtils.closeQuietly(in);
         }
     }
 
 //    /**
 //     * Ingesto
 //     * @param layer
 //     * @param layerUpdate
 //     * @param shapeFile
 //     * @return
 //     * @throws ActionException
 //     */
 //    protected File processVector(UNREDDLayer layer, UNREDDLayerUpdate layerUpdate, File shapeFile) throws ActionException {
 //
 //        try {
 //            PostGISUtils.shapeToPostGis(shapeFile, cfg.getPostGisConfig(),
 //                    layerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.LAYER),
 //                    layerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.YEAR),
 //                    layerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.MONTH));
 //        } catch (PostGisException e) {
 //            LOGGER.error("Error ingesting shapefile: " + e.getMessage());
 //            throw new ActionException(this, "Error ingesting shapefile: " + e.getMessage(), e);
 //        }
 //
 //
 //        File rasterFile = null;
 //        try {
 //            GDALRasterize rasterize = new GDALRasterize( cfg.getRasterizeConfig(), cfg.getConfigDir(), getTempDir());
 //            rasterFile = rasterize.run(layer, layerUpdate, shapeFile);
 //        } catch (Exception e) {
 //            throw new ActionException(this, "Error while rasterizing "+shapeFile+": " + e.getMessage(), e);
 //        }
 //        // at this point the raster is also tiled,  we do must skip the tiling action and go to the embedOverviews
 //
 //        return rasterFile;
 //    }
 
     /**
      * Check that raster size and extent are the expected ones.
      *
      * @param dataFile The input data field to be checked
      * @param layer the geostore resource containing the expected values
      *
      * @throws ActionException
      * @throws NumberFormatException
      */
     protected void checkRasterSize(File dataFile, UNREDDLayer layer) throws ActionException, NumberFormatException {
         //= check that raster size is the expected one
         GeoTiffReader reader;
         try {
             reader = new GeoTiffReader(dataFile);
         } catch (Exception e) {
             throw new ActionException(this, "Error reading tiff file " + dataFile, e);
         }
 
         GridEnvelope ge = reader.getOriginalGridRange();
 //            GridCoverage2D gc2d = reader.read(null);
 //            GridGeometry2D gg2d = gc2d.getGridGeometry();
 //            GridEnvelope  ge = gg2d.getGridRange();
         try {
             int expectedW = Float.valueOf(layer.getAttribute(Attributes.RASTERPIXELWIDTH)).intValue();
             int expectedH = Float.valueOf(layer.getAttribute(Attributes.RASTERPIXELHEIGHT)).intValue();
             int foundW = ge.getSpan(0);
             int foundH = ge.getSpan(1);
             if ( expectedW != foundW || expectedH != foundH ) {
                 throw new ActionException(this, "Bad raster size " + foundW + "x" + foundH + ", expected " + expectedW + "x" + expectedH);
             }
 
             //= check that extent is the expected one
 //            checkCoord(layer, Attributes.RASTERX0)
             double expectedX0 = Double.valueOf(layer.getAttribute(Attributes.RASTERX0));
             double expectedX1 = Double.valueOf(layer.getAttribute(Attributes.RASTERX1));
             double expectedY0 = Double.valueOf(layer.getAttribute(Attributes.RASTERY0));
             double expectedY1 = Double.valueOf(layer.getAttribute(Attributes.RASTERY1));
             Envelope env = reader.getOriginalEnvelope();
 //            Envelope env = gc2d.getEnvelope();
             double foundX0 = env.getMinimum(0);
             double foundX1 = env.getMaximum(0);
             double foundY0 = env.getMinimum(1);
             double foundY1 = env.getMaximum(1);
             if ( ! nearEnough(expectedX0, foundX0) || !nearEnough(expectedX1, foundX1) ||
                     ! nearEnough(expectedY0, foundY0) || ! nearEnough(expectedY1, foundY1) ) {
                 throw new ActionException(this, "Bad raster extent X[" + foundX0 + "," + foundX1 + "] Y[" + foundY0 + "," + foundY1 + "]"
                         + " expected X[" + expectedX0 + "," + expectedX1 + "] Y[" + expectedY0 + "," + expectedY1 + "]");
             }
         } catch (ActionException e) {
             throw e;
         } catch (Exception e) {
             throw new ActionException(this,"Error while checking raster dimensions", e);
         }
     }
 
     private final static double EXTENT_TRESHOLD = 0.000001;
 
     protected static boolean nearEnough(double d1, double d2) {
         double delta = Math.abs(d1 - d2 );
         if(delta == 0)
             return true;
         if( delta < EXTENT_TRESHOLD && LOGGER.isInfoEnabled()) {
             LOGGER.info("Delta not zero:" + d1 + "," + d2);
             return true;
         }
         return false;
     }
 
     @Override
     public boolean checkConfiguration() {
         return true;
     }
 }
