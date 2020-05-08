 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wcs.responses.coverage;
 
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.gce.gtopo30.GTopo30Writer;
 import org.opengis.coverage.grid.GridCoverageWriter;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.wcs.responses.CoverageResponseDelegate;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.zip.ZipOutputStream;
 
 
 /**
  * @author simone giannecchini
  * @author alesio fabiani
  *
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 public class GTopo30CoverageResponseDelegate implements CoverageResponseDelegate {
     /**
      * the grid coverage to be used in this repsonse
      *
      * @uml.property name="sourceCoverage"
      * @uml.associationEnd multiplicity="(0 1)"
      */
     private GridCoverage2D sourceCoverage;
 
     /*
      * (non-Javadoc)
      *
      * @see org.vfny.geoserver.wcs.responses.CoverageResponseDelegate#canProduce(java.lang.String)
      */
     public boolean canProduce(String outputFormat) {
         if (outputFormat.compareToIgnoreCase("GTopo30") == 0) {
             return true;
         }
 
         return false;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.vfny.geoserver.wcs.responses.CoverageResponseDelegate#prepare(java.lang.String,
      *      org.geotools.coverage.grid.GridCoverage2D)
      */
     public void prepare(String outputFormat, GridCoverage2D coverage)
         throws IOException {
         this.sourceCoverage = coverage;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.vfny.geoserver.wcs.responses.CoverageResponseDelegate#getContentType(org.vfny.geoserver.global.GeoServer)
      */
     public String getContentType(GeoServer gs) {
         return "application/x-zip";
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.vfny.geoserver.wcs.responses.CoverageResponseDelegate#getContentEncoding()
      */
     public String getContentEncoding() {
         // return "zip";
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentDisposition() {
         return new StringBuffer("attachment;filename=").append(this.sourceCoverage.getName())
                                                        .append(".zip").toString();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.vfny.geoserver.wcs.responses.CoverageResponseDelegate#encode(java.io.OutputStream)
      */
     public void encode(OutputStream output) throws ServiceException, IOException {
         // creating a zip outputstream
         final ZipOutputStream outZ = new ZipOutputStream(output);
         output = outZ;
 
         // creating a writer
         final GridCoverageWriter writer = new GTopo30Writer(outZ);
 
         // writing
         if (writer != null) {
             writer.write(sourceCoverage, null);
         } else {
             throw new ServiceException("Could not create a writer for the format Gtopo30!");
         }
 
         // freeing everything
         writer.dispose();
        this.sourceCoverage.dispose();
         this.sourceCoverage = null;
     }
 }
