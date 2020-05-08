 //$HeadURL$
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2011 by:
  - Department of Geography, University of Bonn -
  and
  - lat/lon GmbH -
 
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  Contact information:
 
  lat/lon GmbH
  Aennchenstr. 19, 53177 Bonn
  Germany
  http://lat-lon.de/
 
  Department of Geography, University of Bonn
  Prof. Dr. Klaus Greve
  Postfach 1147, 53001 Bonn
  Germany
  http://www.geographie.uni-bonn.de/deegree/
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 package org.deegree.securityproxy.responsefilter.wcs;
 
 import static org.geotools.geometry.jts.JTS.transform;
 import static org.geotools.referencing.CRS.decode;
 import static org.geotools.referencing.CRS.findMathTransform;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.deegree.securityproxy.responsefilter.logging.ResponseClippingReport;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.processing.operation.Crop;
 import org.geotools.gce.geotiff.GeoTiffReader;
 import org.geotools.gce.geotiff.GeoTiffWriter;
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.opengis.parameter.ParameterValueGroup;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 /**
  * Concrete implementation to clip geotiffs.
  * 
  * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="mailto:goltz@lat-lon.de">Dirk Stenger</a>
  * @author last edited by: $Author: lyn $
  * 
  * @version $Revision: $, $Date: $
  */
 public class GeotiffClipper implements ImageClipper {
 
     private static Logger LOG = Logger.getLogger( GeotiffClipper.class );
 
     private static final String VISIBLE_AREA_CRS = "EPSG:4326";
 
     @Override
     public ResponseClippingReport calculateClippedImage( InputStream imageToClip, Geometry visibleArea,
                                                          OutputStream destination )
                             throws IllegalArgumentException {
         checkRequiredParameters( imageToClip, visibleArea, destination );
 
         // / TODO: check if exception was thrown!
         try {
             File imageToClipAsFile = writeToTempFile( imageToClip );
             GeoTiffReader reader = new GeoTiffReader( imageToClipAsFile );
 
             Geometry visibleAreaInImageCrs = transformVisibleAreaToImageCrs( visibleArea, reader );
 
             GeoTiffWriter writer = new GeoTiffWriter( destination );
             GridCoverage2D geotiffToWrite = (GridCoverage2D) reader.read( null );
 
             Geometry imageEnvelope = convertImageEnvelopeToGeometry( reader );
             if ( isClippingRequired( imageEnvelope, visibleAreaInImageCrs ) ) {
                 GridCoverage2D croppedCoverageToWrite = executeCropping( geotiffToWrite, visibleAreaInImageCrs );
                 writer.write( croppedCoverageToWrite, null );
                 Geometry visibleAreaAfterClipping = calculateGeometryVisibleAfterClipping( reader,
                                                                                            visibleAreaInImageCrs );
                 return new ResponseClippingReport( visibleAreaAfterClipping, true );
             } else {
                 writer.write( geotiffToWrite, null );
                 return new ResponseClippingReport( imageEnvelope, false );
             }
         } catch ( Exception e ) {
             LOG.error( "An error occured during clipping the image!", e );
             return new ResponseClippingReport( e.getMessage() );
         }
     }
 
     /**
      * Checks if clipping is required for the passed reader
      * 
      * @param reader
      *            never <code>null</code>
      * @param clippingGeometry
      *            in image crs, never <code>null</code>
      * @return <code>true</code> if the image boundary is not completely inside the clippingGeometry, <code>false</code>
      *         otherwise
      */
     boolean isClippingRequired( GeoTiffReader reader, Geometry clippingGeometry ) {
         Geometry imageGeometry = convertImageEnvelopeToGeometry( reader );
         return isClippingRequired( imageGeometry, clippingGeometry );
     }
 
     /**
      * Calculates the geometry visible after clipping
      * 
      * @param reader
      *            never <code>null</code>
      * @param visibleArea
      *            in image crs, never <code>null</code>
      * @return the geometry visible after clipping
      */
     Geometry calculateGeometryVisibleAfterClipping( GeoTiffReader reader, Geometry visibleArea ) {
         Geometry imageGeometry = convertImageEnvelopeToGeometry( reader );
         if ( visibleArea.contains( imageGeometry ) ) {
             return imageGeometry;
         } else if ( visibleArea.intersects( imageGeometry ) ) {
             return visibleArea.intersection( imageGeometry );
         }
         return new GeometryFactory().toGeometry( new Envelope() );
     }
 
     // TODO: dirty hack! coverage should be read from input stream directly
     private File writeToTempFile( InputStream coverageToClip )
                             throws IOException, FileNotFoundException {
         File tempFile = File.createTempFile( "imageToClip", ".tif" );
         FileOutputStream output = new FileOutputStream( tempFile );
         IOUtils.copy( coverageToClip, output );
         output.close();
         return tempFile;
     }
 
     private void checkRequiredParameters( InputStream imageToClip, Geometry visibleArea, OutputStream toWriteImage )
                             throws IllegalArgumentException {
         if ( imageToClip == null )
             throw new IllegalArgumentException( "Image to clip must not be null!" );
         if ( visibleArea == null )
             throw new IllegalArgumentException( "Wcs request must not be null!" );
         if ( toWriteImage == null )
             throw new IllegalArgumentException( "Output stream to write image to must not be null!" );
     }
 
     private GridCoverage2D executeCropping( GridCoverage2D coverageToWrite, Geometry transformedVisibleArea ) {
         Crop crop = new Crop();
         ParameterValueGroup cropParameters = crop.getParameters();
         cropParameters.parameter( "Source" ).setValue( coverageToWrite );
         cropParameters.parameter( "ROI" ).setValue( transformedVisibleArea );
         GridCoverage2D croppedCoverageToWrite = (GridCoverage2D) crop.doOperation( cropParameters, null );
         return croppedCoverageToWrite;
     }
 
     private Geometry transformVisibleAreaToImageCrs( Geometry visibleArea, GeoTiffReader reader )
                             throws NoSuchAuthorityCodeException, FactoryException, TransformException {
         CoordinateReferenceSystem visibleAreaCRS = decode( VISIBLE_AREA_CRS );
         CoordinateReferenceSystem imageCRS = reader.getCrs();
 
         MathTransform transformVisibleAreaToImageCrs = findMathTransform( visibleAreaCRS, imageCRS );
         return transform( visibleArea, transformVisibleAreaToImageCrs );
     }
 
     private Geometry convertImageEnvelopeToGeometry( GeoTiffReader reader ) {
         GeometryFactory geometryFactory = new GeometryFactory();
         GeneralEnvelope imageEnvelope = reader.getOriginalEnvelope();
         ReferencedEnvelope envelope = new ReferencedEnvelope( imageEnvelope );
         return geometryFactory.toGeometry( envelope );
     }
 
     private boolean isClippingRequired( Geometry imageGeometry, Geometry clippingGeometry ) {
        return !clippingGeometry.contains( imageGeometry );
     }
 
 }
