 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wcs.responses.coverage;
 
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.grid.io.AbstractGridFormat;
 import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
 import org.geotools.gce.geotiff.GeoTiffFormat;
 import org.geotools.gce.geotiff.GeoTiffWriteParams;
 import org.opengis.coverage.grid.GridCoverageWriter;
 import org.opengis.parameter.GeneralParameterValue;
 import org.opengis.parameter.ParameterValueGroup;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.wcs.responses.CoverageResponseDelegate;
 import java.io.IOException;
 import java.io.OutputStream;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public class GeoTIFFCoverageResponseDelegate implements CoverageResponseDelegate {
     /**
      *
      * @uml.property name="sourceCoverage"
      * @uml.associationEnd multiplicity="(0 1)"
      */
     private GridCoverage2D sourceCoverage;
 
     public GeoTIFFCoverageResponseDelegate() {
     }
 
     public boolean canProduce(String outputFormat) {
         if (outputFormat.equalsIgnoreCase("geotiff")) {
             return true;
         }
 
         return false;
     }
 
     public void prepare(String outputFormat, GridCoverage2D coverage)
         throws IOException {
         this.sourceCoverage = coverage;
     }
 
     public String getContentType(GeoServer gs) {
         return "image/tiff";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentEncoding() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentDisposition() {
         return "attachment;filename=" + this.sourceCoverage.getName() + ".tiff";
     }
 
     public void encode(OutputStream output) throws ServiceException, IOException {
         if (sourceCoverage == null) {
             throw new IllegalStateException("It seems prepare() has not been called"
                 + " or has not succeed");
         }
 
         final GeoTiffFormat format = new GeoTiffFormat();
         final GeoTiffWriteParams wp = new GeoTiffWriteParams();
         wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
         wp.setCompressionType("LZW");
         wp.setCompressionQuality(0.75F);
         wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
         wp.setTiling(256, 256);
 
         final ParameterValueGroup writerParams = format.getWriteParameters();
         writerParams.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())
                     .setValue(wp);
 
         GridCoverageWriter writer = format.getWriter(output);
         writer.write(sourceCoverage,
             (GeneralParameterValue[]) writerParams.values().toArray(new GeneralParameterValue[1]));
 
         writer.dispose();
 
        this.sourceCoverage.dispose(false);
         this.sourceCoverage = null;
     }
 }
