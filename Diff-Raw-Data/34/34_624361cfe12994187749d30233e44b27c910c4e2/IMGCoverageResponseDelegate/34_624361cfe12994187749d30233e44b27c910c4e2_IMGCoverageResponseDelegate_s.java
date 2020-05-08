 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wcs.responses.coverage;
 
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.gce.image.WorldImageWriter;
 import org.geotools.image.ImageWorker;
 import org.opengis.coverage.grid.Format;
 import org.opengis.coverage.grid.GridCoverageWriter;
 import org.opengis.parameter.GeneralParameterValue;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.wcs.responses.CoverageResponseDelegate;
 import java.awt.image.ComponentColorModel;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Iterator;
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.MemoryCacheImageOutputStream;
 import javax.media.jai.PlanarImage;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public class IMGCoverageResponseDelegate implements CoverageResponseDelegate {
     /**
      *
      * @uml.property name="sourceCoverage"
      * @uml.associationEnd multiplicity="(0 1)"
      */
     private GridCoverage2D sourceCoverage;
     private String outputFormat;
 
     public IMGCoverageResponseDelegate() {
     }
 
     public boolean canProduce(String outputFormat) {
         if (outputFormat.equalsIgnoreCase("bmp") || outputFormat.equalsIgnoreCase("gif")
                 || outputFormat.equalsIgnoreCase("tiff") || outputFormat.equalsIgnoreCase("png")
                 || outputFormat.equalsIgnoreCase("jpeg") || outputFormat.equalsIgnoreCase("tif")) {
             return true;
         }
 
         return false;
     }
 
     public void prepare(String outputFormat, GridCoverage2D coverage)
         throws IOException {
         this.outputFormat = outputFormat;
         this.sourceCoverage = coverage;
     }
 
     public String getContentType(GeoServer gs) {
         return new StringBuffer("image/").append(outputFormat.toLowerCase()).toString();
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
         return (outputFormat.equalsIgnoreCase("tiff") || outputFormat.equalsIgnoreCase("tif"))
         ? new StringBuffer("attachment;filename=").append(this.sourceCoverage.getName()).append(".")
                                                   .append(outputFormat).toString() : null;
     }
 
     public void encode(OutputStream output) throws ServiceException, IOException {
         if (sourceCoverage == null) {
             throw new IllegalStateException(new StringBuffer(
                     "It seems prepare() has not been called").append(" or has not succeed")
                                                                                                       .toString());
         }
 
         final GridCoverageWriter writer = new WorldImageWriter(output);
 
         // writing parameters for Image
         final Format writerParams = writer.getFormat();
         writerParams.getWriteParameters().parameter("Format")
                     .setValue(this.outputFormat.toLowerCase());
 
         // writing
         writer.write(sourceCoverage,
             (GeneralParameterValue[]) writerParams.getWriteParameters().values()
                                                   .toArray(new GeneralParameterValue[1]));
 
         // freeing everything
         writer.dispose();
        this.sourceCoverage.dispose();
         this.sourceCoverage = null;
     }
 }
