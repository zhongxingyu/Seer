 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.media.jai.Interpolation;
 
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.DefaultProcessor;
 import org.geotools.coverage.processing.operation.Interpolate;
 import org.geotools.coverage.processing.operation.Resample;
 import org.geotools.coverage.processing.operation.SelectSampleDimension;
 import org.geotools.factory.Hints;
 import org.opengis.coverage.Coverage;
 import org.opengis.coverage.grid.GridCoverage;
 import org.opengis.parameter.ParameterValueGroup;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.wcs.WcsException;
 
 /**
  * 
  * @author Simone Giannecchini, GeoSolutions
  * @author Alessio Fabiani, GeoSolutions
  * 
  */
 public class WCSUtils {
 
     private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(WCSUtils.class);
     
     public final static Hints LENIENT_HINT = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
 
     private final static SelectSampleDimension bandSelectFactory = new SelectSampleDimension();
 
     private final static Interpolate interpolateFactory = new Interpolate();
 
     private final static Resample resampleFactory = new Resample();
 
     private final static ParameterValueGroup bandSelectParams;
 
     private final static ParameterValueGroup interpolateParams;
 
     private final static ParameterValueGroup resampleParams;
 
     private final static Hints hints = new Hints();
 
     static {
         hints.add(LENIENT_HINT);
         // ///////////////////////////////////////////////////////////////////
         //
         // Static Processors
         //
         // ///////////////////////////////////////////////////////////////////
        final DefaultProcessor processor = new DefaultProcessor(LENIENT_HINT);
         bandSelectParams = processor.getOperation("SelectSampleDimension").getParameters();
         interpolateParams = processor.getOperation("Interpolate").getParameters();
         resampleParams = processor.getOperation("Resample").getParameters();        
     }
 
     /**
      * <strong>Reprojecting</strong><br>
      * The new grid geometry can have a different coordinate reference system than the underlying
      * grid geometry. For example, a grid coverage can be reprojected from a geodetic coordinate
      * reference system to Universal Transverse Mercator CRS.
      * 
      * 
      * @param coverage
      *            GridCoverage2D
      * @param sourceCRS
      *            CoordinateReferenceSystem
      * @param targetCRS
      *            CoordinateReferenceSystem
      * @return GridCoverage2D
      * @throws WcsException
      */
     public static GridCoverage2D resample(
     		final GridCoverage2D coverage,
             final CoordinateReferenceSystem sourceCRS, 
             final CoordinateReferenceSystem targetCRS,
             final GridGeometry2D gridGeometry,
             final Interpolation interpolation) throws WcsException {
 
 
         final ParameterValueGroup param = (ParameterValueGroup) resampleParams.clone();
         param.parameter("Source").setValue(coverage);
         param.parameter("CoordinateReferenceSystem").setValue(targetCRS);
         param.parameter("GridGeometry").setValue(gridGeometry);
         param.parameter("InterpolationType").setValue(interpolation);
 
         return (GridCoverage2D) resampleFactory.doOperation(param, hints);
 
     }
 
     /**
      * <strong>Interpolating</strong><br>
      * Specifies the interpolation type to be used to interpolate values for points which fall
      * between grid cells. The default value is nearest neighbor. The new interpolation type
      * operates on all sample dimensions. Possible values for type are: {@code "NearestNeighbor"},
      * {@code "Bilinear"} and {@code "Bicubic"} (the {@code "Optimal"} interpolation type is
      * currently not supported).
      * 
      * @param coverage
      *            GridCoverage2D
      * @param interpolation
      *            Interpolation
      * @return GridCoverage2D
      * @throws WcsException
      */
     public static GridCoverage2D interpolate(
     		final GridCoverage2D coverage,
             final Interpolation interpolation) throws WcsException {
         // ///////////////////////////////////////////////////////////////////
         //
         // INTERPOLATE
         //
         //
         // ///////////////////////////////////////////////////////////////////
         if (interpolation != null) {
             /* Operations.DEFAULT.interpolate(coverage, interpolation) */
             final ParameterValueGroup param = (ParameterValueGroup) interpolateParams.clone();
             param.parameter("Source").setValue(coverage);
             param.parameter("Type").setValue(interpolation);
 
             return (GridCoverage2D) interpolateFactory.doOperation(param, hints);
         }
 
         return coverage;
     }
 
     /**
      * <strong>Band Selecting</strong><br>
      * Chooses <var>N</var> {@linkplain org.geotools.coverage.GridSampleDimension sample dimensions}
      * from a grid coverage and copies their sample data to the destination grid coverage in the
      * order specified. The {@code "SampleDimensions"} parameter specifies the source
      * {@link org.geotools.coverage.GridSampleDimension} indices, and its size ({@code
      * SampleDimensions.length}) determines the number of sample dimensions of the destination grid
      * coverage. The destination coverage may have any number of sample dimensions, and a particular
      * sample dimension of the source coverage may be repeated in the destination coverage by
      * specifying it multiple times in the {@code "SampleDimensions"} parameter.
      * 
      * @param params
      *            Set
      * @param coverage
      *            GridCoverage
      * @return Coverage
      * @throws WcsException
      */
     public static Coverage bandSelect(final Map params, final GridCoverage coverage)
             throws WcsException {
         // ///////////////////////////////////////////////////////////////////
         //
         // BAND SELECT
         //
         //
         // ///////////////////////////////////////////////////////////////////
         final int numDimensions = coverage.getNumSampleDimensions();
         final Map dims = new HashMap();
         final ArrayList selectedBands = new ArrayList();
 
         for (int d = 0; d < numDimensions; d++) {
             dims.put("band" + (d + 1), new Integer(d));
         }
 
         if ((params != null) && !params.isEmpty()) {
             for (Iterator p = params.keySet().iterator(); p.hasNext();) {
                 final String param = (String) p.next();
 
                 if (param.equalsIgnoreCase("BAND")) {
                     try {
                         final String values = (String) params.get(param);
 
                         if (values.indexOf("/") > 0) {
                             final String[] minMaxRes = values.split("/");
                             final int min = (int) Math.round(Double.parseDouble(minMaxRes[0]));
                             final int max = (int) Math.round(Double.parseDouble(minMaxRes[1]));
                             final double res = ((minMaxRes.length > 2) ? Double.parseDouble(minMaxRes[2]) : 0.0);
 
                             for (int v = min; v <= max; v++) {
                                 final String key = param.toLowerCase() + v;
 
                                 if (dims.containsKey(key)) {
                                     selectedBands.add(dims.get(key));
                                 }
                             }
                         } else {
                             final String[] bands = values.split(",");
 
                             for (int v = 0; v < bands.length; v++) {
                                 final String key = param.toLowerCase() + bands[v];
 
                                 if (dims.containsKey(key)) {
                                     selectedBands.add(dims.get(key));
                                 }
                             }
 
                             if (selectedBands.size() == 0) {
                                 throw new Exception("WRONG PARAM VALUES.");
                             }
                         }
                     } catch (Exception e) {
                         throw new WcsException("Band parameters incorrectly specified: "
                                 + e.getLocalizedMessage());
                     }
                 }
             }
         }
 
         final int length = selectedBands.size();
         final int[] bands = new int[length];
 
         for (int b = 0; b < length; b++) {
             bands[b] = ((Integer) selectedBands.get(b)).intValue();
         }
 
         return bandSelect(coverage, bands);
     }
 
     public static Coverage bandSelect(final GridCoverage coverage, final int[] bands) {
         Coverage bandSelectedCoverage;
 
         if ((bands != null) && (bands.length > 0)) {
             /* Operations.DEFAULT.selectSampleDimension(coverage, bands) */
             final ParameterValueGroup param = (ParameterValueGroup) bandSelectParams.clone();
             param.parameter("Source").setValue(coverage);
             param.parameter("SampleDimensions").setValue(bands);
             // param.parameter("VisibleSampleDimension").setValue(bands);
             bandSelectedCoverage = bandSelectFactory.doOperation(param, hints);
         } else {
             bandSelectedCoverage = coverage;
         }
 
         return bandSelectedCoverage;
     }
 
 	public static final String ELEVATION = "ELEVATION";
 }
