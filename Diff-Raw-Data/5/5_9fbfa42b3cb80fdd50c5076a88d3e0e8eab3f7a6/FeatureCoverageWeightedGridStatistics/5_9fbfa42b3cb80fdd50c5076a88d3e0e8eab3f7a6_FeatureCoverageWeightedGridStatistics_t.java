 package gov.usgs.gdp.analysis.grid;
 
 import gov.usgs.gdp.analysis.statistics.WeightedStatistics1D;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Formatter;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.SchemaException;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.operation.TransformException;
 
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDataset;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.ft.FeatureDataset;
 import ucar.nc2.ft.FeatureDatasetFactoryManager;
 import ucar.unidata.geoloc.LatLonRect;
 
 import com.google.common.base.Preconditions;
 import gov.usgs.gdp.analysis.GeoToolsNetCDFUtility;
 import gov.usgs.gdp.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
 import java.io.OutputStreamWriter;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.dataset.CoordinateAxis;
 import ucar.nc2.dataset.CoordinateAxis1DTime;
 import ucar.nc2.iosp.geotiff.GeoTiffIOServiceProvider;
 
 public class FeatureCoverageWeightedGridStatistics {
 
     public static void generate(
             FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
             String attributeName,
             GridDataset gridDataset,
             String variableName,
             Range timeRange,
             List<Statistic> statisticList,
             BufferedWriter writer,
             boolean groupByStatistic,
             String delimiter)
             throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
     {
 
         GridDatatype gdt = gridDataset.findGridDatatype(variableName);
         Preconditions.checkNotNull(gdt, "Variable named %s not found in gridDataset", variableName);
 
         GridType gt = GridType.findGridType(gdt.getCoordinateSystem());
         if( !(gt == GridType.YX || gt == GridType.TYX) ) {
             throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
         }
 
         LatLonRect llr = GeoToolsNetCDFUtility.getLatLonRectFromEnvelope(
                 featureCollection.getBounds(),
                 DefaultGeographicCRS.WGS84);
 
         try {
             Range[] ranges = getRangesFromLatLonRect(llr, gdt.getCoordinateSystem());
             gdt = gdt.makeSubset(null, null, timeRange, null, ranges[1], ranges[0]);
         } catch (InvalidRangeException ex) {
             System.out.println(gdt.getCoordinateSystem().getLatLonBoundingBox());
             System.out.println(llr);
             Logger.getLogger(FeatureCoverageWeightedGridStatistics.class.getName()).log(Level.SEVERE, null, ex);
             throw ex;  // rethrow requested by IS
         }
 
         GridCoordSystem gcs = gdt.getCoordinateSystem();
 
         Map<Object, GridCellCoverage> attributeCoverageMap =
                 GridCellCoverageFactory.generateByFeatureAttribute(
                     featureCollection,
                     attributeName,
                     gdt.getCoordinateSystem());
 
         String variableUnits = gdt.getVariable().getUnitsString();
 
         List<Object> attributeList = Collections.unmodifiableList(new ArrayList<Object>(attributeCoverageMap.keySet()));
 
         FeatureCoverageWeightedGridStatisticsWriter writerX =
                 new FeatureCoverageWeightedGridStatisticsWriter(
                     attributeList,
                     variableName,
                     variableUnits,
                     statisticList,
                     groupByStatistic,
                     delimiter,
                     writer);
 
         WeightedGridStatisticsVisitor v = null;
         switch (gt) {
             case YX:
                 v = new WeightedGridStatisticsVisitor_YX(attributeCoverageMap, writerX);
             break;
             case TYX:
                 v = new WeightedGridStatisticsVisitor_TYX(attributeCoverageMap, writerX, gcs.getTimeAxis1D(), timeRange);
             break;
             default:
                 throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
         }
 
         GridCellTraverser gct = new GridCellTraverser(gdt);
 
         gct.traverse(v);
     }
 
 
     // Handle bugs in NetCDF 4.1 for X and Y CoordinateAxis2D (c2d) with
     // shapefile bound (LatLonRect) that doesn't interect any grid center
     // (aka midpoint) *and* issue calculating edges for c2d axes with  < 3
     // grid cells in any dimension.
    public static Range[] getRangesFromLatLonRect(LatLonRect llr, GridCoordSystem gcs)
             throws InvalidRangeException
     {
 
         int[] lowerCornerIndices = new int[2];
         int[] upperCornerIndices = new int[2];
 
         gcs.findXYindexFromLatLon(llr.getLatMin(), llr.getLonMin(), lowerCornerIndices);
         gcs.findXYindexFromLatLon(llr.getLatMax(), llr.getLonMax(), upperCornerIndices);
 
         if (lowerCornerIndices[0] < 0 || lowerCornerIndices[1] < 0 ||
             upperCornerIndices[0] < 0 || upperCornerIndices[1] < 0) {
             throw new InvalidRangeException();
         }
 
         int lowerX;
         int upperX;
         int lowerY;
         int upperY;
         if(lowerCornerIndices[0] < upperCornerIndices[0]) {
             lowerX = lowerCornerIndices[0];
             upperX = upperCornerIndices[0];
         } else {
             upperX = lowerCornerIndices[0];
             lowerX = upperCornerIndices[0];
         }
         if(lowerCornerIndices[1] < upperCornerIndices[1]) {
             lowerY = lowerCornerIndices[1];
             upperY = upperCornerIndices[1];
         } else {
             upperY = lowerCornerIndices[1];
             lowerY = upperCornerIndices[1];
         }
 
         // Buffer X dimension to width of 3, otherwise grid cell width calc fails.
         // NOTE: NetCDF ranges are upper edge inclusive
         int deltaX = upperX - lowerX;
         if (deltaX < 2) {
             CoordinateAxis xAxis = gcs.getXHorizAxis();
             int maxX = xAxis.getShape(xAxis.getRank() - 1) - 1; // inclusive
             if (maxX < 2) {
                 throw new InvalidRangeException("Source grid too small");
             }
             if (deltaX == 0) {
                 if (lowerX > 0 && upperX < maxX) {
                     --lowerX;
                     ++upperX;
                 } else {
                     // we're on an border cell
                     if (lowerX == 0) {
                         upperX = 2;
                     } else {
                         lowerX = maxX - 2;
                     }
                 }
             } else if (deltaX == 1) {
                 if (lowerX == 0) {
                     ++upperX;
                 } else {
                     --lowerX;
                 }
             }
         }
 
         // Buffer Y dimension to width of 3, otherwise grid cell width calc fails.
         // NOTE: NetCDF ranges are upper edge inclusive
         int deltaY = upperY - lowerY;
        if (deltaY < 2) {
             CoordinateAxis yAxis = gcs.getYHorizAxis();
             int maxY = yAxis.getShape(0)  - 1 ; // inclusive
             if (maxY < 2) {
                 throw new InvalidRangeException("Source grid too small");
             }
             if (deltaY == 0) {
                 if (lowerY > 0 && upperY < maxY) {
                     --lowerY;
                     ++upperY;
                 } else {
                     // we're on an border cell
                     if (lowerY == 0) {
                         upperY = 2;
                     } else {
                         lowerY = maxY - 2;
                     }
                 }
             } else if (deltaY == 1) {
                 if (lowerY == 0) {
                     ++upperY;
                 } else {
                     --lowerY;
                 }
             }
         }
 
         return new Range[] {
             new Range(lowerX, upperX),
             new Range(lowerY, upperY),
         };
     }
 
     protected static abstract class WeightedGridStatisticsVisitor extends FeatureCoverageGridCellVisitor {
 
         protected Map<Object, WeightedStatistics1D> perAttributeStatistics;
         protected WeightedStatistics1D allAttributeStatistics;
 
         public WeightedGridStatisticsVisitor(Map<Object, GridCellCoverage> attributeCoverageMap) {
             super(attributeCoverageMap);
         }
 
         protected Map<Object, WeightedStatistics1D> createPerAttributeStatisticsMap() {
             Map map = new LinkedHashMap<Object, WeightedStatistics1D>();
             for (Object attributeValue : attributeCoverageMap.keySet()) {
                 map.put(attributeValue, new WeightedStatistics1D());
             }
             return map;
         }
 
         @Override
         public void yxStart() {
             perAttributeStatistics = createPerAttributeStatisticsMap();
             allAttributeStatistics = new WeightedStatistics1D();
         }
 
         @Override
         public void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute) {
             perAttributeStatistics.get(attribute).accumulate(value, coverage);
         }
 
         @Override
         public void processAllAttributeGridCellCoverage(double value, double coverage) {
             allAttributeStatistics.accumulate(value, coverage);
         }
     }
 
     protected static class WeightedGridStatisticsVisitor_YX extends WeightedGridStatisticsVisitor {
 
         FeatureCoverageWeightedGridStatisticsWriter writer;
 
         WeightedGridStatisticsVisitor_YX(
                 Map<Object, GridCellCoverage> attributeCoverageMap,
                 FeatureCoverageWeightedGridStatisticsWriter writer) {
             super(attributeCoverageMap);
             this.writer = writer;
         }
 
         @Override
         public void traverseStart() {
             try {
                 writer.writerHeader(null);
             } catch (IOException ex) {
                 // TODO
             }
         }
 
         @Override
         public void traverseEnd() {
             try {
                 writer.writeRow(
                         null,
                         perAttributeStatistics.values(),
                         allAttributeStatistics);
             } catch (IOException ex) {
                 // TODO
             }
         }
 
     }
 
     protected static class WeightedGridStatisticsVisitor_TYX extends WeightedGridStatisticsVisitor {
 
         protected Map<Object, WeightedStatistics1D> allTimestepPerAttributeStatistics;
         protected WeightedStatistics1D allTimestepAllAttributeStatistics;
 
         protected FeatureCoverageWeightedGridStatisticsWriter writer;
         
         protected CoordinateAxis1DTime tAxis;
         protected int tIndexOffset;
 
         public WeightedGridStatisticsVisitor_TYX(
                 Map<Object, GridCellCoverage> attributeCoverageMap,
                 FeatureCoverageWeightedGridStatisticsWriter writer,
                 CoordinateAxis1DTime tAxis,
                 Range tRange) {
             super(attributeCoverageMap);
             this.writer = writer;
             this.tAxis = tAxis;
             this.tIndexOffset = tRange.first();
         }
 
         @Override
         public void traverseStart() {
             try {
                 writer.writerHeader(FeatureCoverageWeightedGridStatisticsWriter.TIMESTEPS_LABEL);
             } catch (IOException ex) {
                 // TODO
             }
 
             allTimestepPerAttributeStatistics =
                     createPerAttributeStatisticsMap();
             allTimestepAllAttributeStatistics = new WeightedStatistics1D();
         }
 
         @Override
         public void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute) {
             super.processPerAttributeGridCellCoverage(value, coverage, attribute);
             allTimestepPerAttributeStatistics.get(attribute).accumulate(value, coverage);
         }
 
         @Override
         public void processAllAttributeGridCellCoverage(double value, double coverage) {
             super.processAllAttributeGridCellCoverage(value, coverage);
             allTimestepAllAttributeStatistics.accumulate(value, coverage);
         }
 
         @Override
         public void tEnd(int tIndex) {
             try {
                 writer.writeRow(
                         tAxis.getTimeDate(tIndexOffset + tIndex).toGMTString(),
                         perAttributeStatistics.values(),
                         allAttributeStatistics);
             } catch (IOException e) {
                 // TODO
             }
         }
 
         @Override
         public void traverseEnd() {
             try {
                 writer.writeRow(
                         FeatureCoverageWeightedGridStatisticsWriter.ALL_TIMESTEPS_LABEL,
                         allTimestepPerAttributeStatistics.values(),
                         allTimestepAllAttributeStatistics);
             } catch (IOException ex) {
                 // TODO
             }
         }
     }
 
     // SIMPLE inline testing only, need unit tests...
     public static void main(String[] args) throws Exception {
 
         NetcdfFile.registerIOProvider(GeoTiffIOServiceProvider.class);
         
         String ncLocation =
 //                "http://runoff.cr.usgs.gov:8086/thredds/dodsC/hydro/national/2.5arcmin";
 //                "http://internal.cida.usgs.gov/thredds/dodsC/misc/us_gfdl.A1.monthly.Tavg.1960-2099.nc";
 //                "http://localhost:18080/thredds/dodsC/ncml/gridded_obs.daily.Wind.ncml";
 //                "/Users/tkunicki/Downloads/thredds-data/CONUS_2001-2010.ncml";
 //                "/Users/tkunicki/Downloads/thredds-data/gridded_obs.daily.Wind.ncml";
 //                "dods://igsarm-cida-javadev1.er.usgs.gov:8081/thredds/dodsC/qpe/ncrfc.ncml";
 //                "dods://internal.cida.usgs.gov/thredds/dodsC/qpe/GRID.0530/200006_ncrfc_240ss.grd";
 //                "dods://michigan.glin.net:8080/thredds/dodsC/glos/all/GLCFS/Forecast/m201010900.out1.nc";
                 "dods://michigan.glin.net:8080/thredds/dodsC/glos/glcfs/michigan/ncas_his2d";
 //                "/Users/tkunicki/x.tiff";
 
         String sfLocation =
 //                "/Users/tkunicki/Projects/GDP/GDP/src/main/resources/Sample_Files/Shapefiles/serap_hru_239.shp";
 //                "/Users/tkunicki/Downloads/lkm_hru/lkm_hru.shp";
 //                "/Users/tkunicki/Downloads/HUC12LM/lake_mich_12_alb_NAD83.shp";
 //                "/Users/tkunicki/Downloads/dbshapefiles/lk_mich.shp";
                 "/Users/tkunicki/Downloads/lk_mich/test.shp";
 
         String attributeName =
 //                "GRID_CODE";
 //                "GRIDCODE";
 //                "OBJECTID";
                 "Id";
 
         String variableName =
 //                "Wind";
 //                "P06M_NONE";
                 "eta";
 //                "depth";
 //                "I0B0";
 
         Range timeRange =
                 new Range(30, 39);
 //                null;
 
         FeatureDataset dataset = null;
         FileDataStore dataStore = null;
         long start = System.currentTimeMillis();
         try {
             dataset = FeatureDatasetFactoryManager.open(null, ncLocation, null, new Formatter());
             dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
             FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
             FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();
 
             // example csv dump...
             BufferedWriter writer = null;
             try {
                 writer = new BufferedWriter(new OutputStreamWriter(System.out));
 
             FeatureCoverageWeightedGridStatistics.generate(
                     featureCollection,
                     attributeName,
                     (GridDataset)dataset,
                     variableName,
                     timeRange,
                     Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[] {
                             FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
                             FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum, }),
                     writer,
                     false,
                     ","
                     );
 
             } catch (IOException ex) {
                 Logger.getLogger(FeatureCoverageWeightedGridStatistics.class.getName()).log(Level.SEVERE, null, ex);
             } finally {
                 if (writer != null) {
                     try {
                         writer.close();
                     } catch (IOException ex) {
                         Logger.getLogger(FeatureCoverageWeightedGridStatistics.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
 
         } catch (Exception ex) {
             Logger.getLogger(FeatureCoverageWeightedGridStatistics.class.getName()).log(Level.SEVERE, null, ex);
         }finally {
             if (dataset != null) {
                 try {
                     dataset.close();
                 } catch (IOException ex) {
                     Logger.getLogger(FeatureCoverageWeightedGridStatistics.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             if (dataStore != null) {
                 dataStore.dispose();
             }
         }
         System.out.println("Completed in " + (System.currentTimeMillis() - start) + " ms.");
     }
 }
