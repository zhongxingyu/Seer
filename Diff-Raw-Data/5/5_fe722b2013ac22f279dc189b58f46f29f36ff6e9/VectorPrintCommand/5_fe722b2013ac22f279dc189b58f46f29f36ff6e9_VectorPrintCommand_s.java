 //$HeadURL$
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2009 by:
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
 package org.deegree.igeo.commands;
 
 import static org.deegree.framework.util.MapUtils.DEFAULT_PIXEL_SIZE;
 import static org.deegree.graphics.MapFactory.createMapView;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.deegree.datatypes.QualifiedName;
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.framework.util.MapUtils;
 import org.deegree.graphics.MapView;
 import org.deegree.graphics.Theme;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.mapmodel.Layer;
 import org.deegree.igeo.mapmodel.LayerGroup;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.mapmodel.MapModelVisitor;
 import org.deegree.igeo.views.LayerPane;
 import org.deegree.kernel.AbstractCommand;
 import org.deegree.kernel.Command;
 import org.deegree.model.spatialschema.Envelope;
 
 import com.lowagie.text.Document;
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.PageSize;
 import com.lowagie.text.Rectangle;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfWriter;
 
 /**
  * {@link Command} implementation for printing a map as a vector PDF document
  * 
  * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  */
 public class VectorPrintCommand extends AbstractCommand {
 
     public static final QualifiedName commandName = new QualifiedName( "Print Map as Vector PDF" );
 
     private static final ILogger LOG = LoggerFactory.getLogger( UnselectFeaturesCommand.class );
 
     private PrintDescriptionBean printDefinition;
 
     private ApplicationContainer<?> appContainer;
 
     private Document document;
 
     /**
      * 
      * @param appContainer
      */
     public void setApplicationContainer( ApplicationContainer<?> appContainer ) {
         this.appContainer = appContainer;
     }
 
     /**
      * 
      * @param printDefinition
      */
     public void setPrintDefinition( PrintDescriptionBean printDefinition ) {
         this.printDefinition = printDefinition;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.kernel.Command#execute()
      */
     public void execute()
                             throws Exception {
 
         Graphics2D g = initDocument();
 
         java.awt.Rectangle rect = getCanvasSize();
         g.setClip( rect.x, rect.y, rect.width, rect.height );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            // show canvas as yellow rectangle
            g.setColor( Color.yellow );
            g.fillRect( rect.x, rect.y, rect.width, rect.height );
        }
 
         MapModel mm = appContainer.getMapModel( null );
         Envelope originalExtent = mm.getEnvelope();
         Envelope extent = originalExtent;
         extent = MapUtils.ensureAspectRatio( extent, rect.width, rect.height );
         if ( printDefinition.getScale() > 0 ) {
             // use scale selected by used
             double currentScale = MapUtils.calcScale( rect.width, rect.height, extent, extent.getCoordinateSystem(),
                                                       0.0254 / printDefinition.getDpi() );
             extent = MapUtils.scaleEnvelope( extent, currentScale, printDefinition.getScale() );
         }
         double dx = printDefinition.getMapLeft() - extent.getMin().getX();
         double dy = printDefinition.getMapBottom() - extent.getMin().getY();
         extent.translate( dx, dy );
 
         // change map extent for printing
         mm.setEnvelope( extent );
         int w = mm.getTargetDevice().getPixelWidth();
         int h = mm.getTargetDevice().getPixelHeight();
         mm.getTargetDevice().setPixelWidth( rect.width );
         mm.getTargetDevice().setPixelHeight( rect.height );
         Theme[] themes = getThemes( extent );
         MapView mv = createMapView( "iGeoDesktop", extent, mm.getCoordinateSystem(), themes, DEFAULT_PIXEL_SIZE );
         mv.paint( g );
         kill( g );
 
         // reset map extent
         mm.getTargetDevice().setPixelWidth( w );
         mm.getTargetDevice().setPixelHeight( h );
         mm.setEnvelope( originalExtent );
     }
 
     /**
      * 
      * @param extent
      * @return a {@link Theme} for each layers visible in map to be printed
      * @throws Exception
      */
     private Theme[] getThemes( final Envelope extent )
                             throws Exception {
         final List<Theme> themes = new ArrayList<Theme>();
         final MapModel mm = appContainer.getMapModel( null );
         mm.walkLayerTree( new MapModelVisitor() {
             public void visit( Layer layer )
                                     throws Exception {
                 double mis = layer.getMinScaleDenominator();
                 double mxs = layer.getMaxScaleDenominator();
                 if ( layer.isVisible() && mis <= mm.getScaleDenominator() && mxs >= mm.getScaleDenominator()
                      && !layer.getTitle().equals( "deegree:PrintBorder" ) ) {
                     List<Theme> layerThemes = LayerPane.createThemes( layer.getCurrentStyle(), layer.getDataAccess(),
                                                                       extent.getCoordinateSystem() );
                     themes.add( 0, layerThemes.get( 0 ) );
                 }
             }
 
             public void visit( LayerGroup layerGroup )
                                     throws Exception {
                 // not using grouping nodes
             }
         } );
         return themes.toArray( new Theme[themes.size()] );
     }
 
     private void kill( Graphics2D g ) {
         g.dispose();
         document.close();
     }
 
     /**
      * initializes the {@link Document} required for printing using iText
      * 
      * @return graphic context ({@link Graphics2D}) of the initialized document
      * @throws FileNotFoundException
      * @throws DocumentException
      */
     private Graphics2D initDocument()
                             throws FileNotFoundException, DocumentException {
         String pageFormat = printDefinition.getPageFormat();
         Rectangle pageSize;
         if ( pageFormat != null )
             pageSize = PageSize.getRectangle( pageFormat );
         else
             pageSize = new Rectangle( printDefinition.getPageWidth(), printDefinition.getPageHeight() );
         LOG.logDebug( "page size", pageSize );
         // create (pdf) document with selected pages size; set margin and PDF-version
         document = new Document( pageSize );
         document.setMargins( printDefinition.getAreaLeft(),
                              printDefinition.getAreaLeft() + printDefinition.getAreaWidth(),
                              printDefinition.getAreaTop(),
                              printDefinition.getAreaTop() + printDefinition.getAreaHeight() );
 
         PdfWriter writer = PdfWriter.getInstance( document, new FileOutputStream( printDefinition.getTargetFile() ) );
         writer.setPdfVersion( printDefinition.getPdfVersion() );
         document.open();
         addMetaData( document );
         PdfContentByte cb = writer.getDirectContent();
 
         // create canvas
         Graphics2D g = cb.createGraphics( convert( pageSize.getWidth() / 72 * 25.4 ),
                                           convert( pageSize.getHeight() / 72 * 25.4 ) );
         LOG.logDebug( "canvas size", convert( pageSize.getWidth() / 72 * 25.4 ) + " "
                                      + convert( pageSize.getHeight() / 72 * 25.4 ) );
 
         // required for correct scaling of raster symbols
         int i1 = convert( pageSize.getHeight() / 72 * 25.4 );
         int i2 = convert_( pageSize.getHeight() / 72 * 25.4 );
         g.translate( 0, i1 - i2 );
         g.scale( 72d / printDefinition.getDpi(), 72d / printDefinition.getDpi() );
         return g;
     }
 
     private void addMetaData( Document document ) {
         // TODO
         // read metadata from configuration or input dialog
         // document.addTitle( "My first PDF" );
         // document.addSubject( "Using iText" );
         // document.addKeywords( "Java, PDF, iText" );
         // document.addAuthor( "Lars Vogel" );
         // document.addCreator( "Lars Vogel" );
     }
 
     private int convert( double millimeter ) {
         return (int) Math.round( millimeter * printDefinition.getDpi() / 25.4 );
     }
 
     private int convert_( double millimeter ) {
         return (int) Math.round( millimeter * 72 / 25.4 );
     }
 
     private java.awt.Rectangle getCanvasSize() {
         int w = convert( printDefinition.getAreaWidth() );
         int h = convert( printDefinition.getAreaHeight() );
         int x = convert( printDefinition.getAreaLeft() );
         int y = convert( printDefinition.getAreaTop() );
         return new java.awt.Rectangle( x, y, w, h );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.kernel.Command#getName()
      */
     public QualifiedName getName() {
         return commandName;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.kernel.Command#getResult()
      */
     public Object getResult() {
         // TODO Auto-generated method stub
         return null;
     }
 
     /**
      * 
      * TODO add class documentation here
      * 
      * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version $Revision$, $Date$
      */
     public static class PrintDescriptionBean {
 
         private int areaLeft;
 
         private int areaTop;
 
         private int areaWidth;
 
         private int areaHeight;
 
         private double mapLeft;
 
         private double mapBottom;
 
         private double scale;
 
         private String pageFormat;
 
         private float pageWidth = PageSize.A4.getWidth();
 
         private float pageHeight = PageSize.A4.getHeight();
 
         private char pdfVersion = PdfWriter.VERSION_1_4;
 
         private String targetFile;
 
         private int dpi;
 
         /**
          * @return the pdfVersion
          */
         public char getPdfVersion() {
             return pdfVersion;
         }
 
         /**
          * @param pdfVersion
          *            the pdfVersion to set
          */
         public void setPdfVersion( char pdfVersion ) {
             this.pdfVersion = pdfVersion;
         }
 
         /**
          * @return the targetFile
          */
         public String getTargetFile() {
             return targetFile;
         }
 
         /**
          * @param targetFile
          *            the targetFile to set
          */
         public void setTargetFile( String targetFile ) {
             this.targetFile = targetFile;
         }
 
         /**
          * @return the areaLeft
          */
         public int getAreaLeft() {
             return areaLeft;
         }
 
         /**
          * @param areaLeft
          *            the areaLeft to set
          */
         public void setAreaLeft( int areaLeft ) {
             this.areaLeft = areaLeft;
         }
 
         /**
          * @return the areaTop
          */
         public int getAreaTop() {
             return areaTop;
         }
 
         /**
          * @param areaTop
          *            the areaTop to set
          */
         public void setAreaTop( int areaTop ) {
             this.areaTop = areaTop;
         }
 
         /**
          * @return the areaWidth
          */
         public int getAreaWidth() {
             return areaWidth;
         }
 
         /**
          * @param areaWidth
          *            the areaWidth to set
          */
         public void setAreaWidth( int areaWidth ) {
             this.areaWidth = areaWidth;
         }
 
         /**
          * @return the areaHeight
          */
         public int getAreaHeight() {
             return areaHeight;
         }
 
         /**
          * @param areaHeight
          *            the areaHeight to set
          */
         public void setAreaHeight( int areaHeight ) {
             this.areaHeight = areaHeight;
         }
 
         /**
          * @return the mapLeft
          */
         public double getMapLeft() {
             return mapLeft;
         }
 
         /**
          * @param mapLeft
          *            the mapLeft to set
          */
         public void setMapLeft( double mapLeft ) {
             this.mapLeft = mapLeft;
         }
 
         /**
          * @return the mapBottom
          */
         public double getMapBottom() {
             return mapBottom;
         }
 
         /**
          * @param mapBottom
          *            the mapBottom to set
          */
         public void setMapBottom( double mapBottom ) {
             this.mapBottom = mapBottom;
         }
 
         /**
          * @return the scale
          */
         public double getScale() {
             return scale;
         }
 
         /**
          * @param scale
          *            the scale to set
          */
         public void setScale( double scale ) {
             this.scale = scale;
         }
 
         /**
          * @return the pageFormat
          */
         public String getPageFormat() {
             return pageFormat;
         }
 
         /**
          * @param pageFormat
          *            the pageFormat to set
          */
         public void setPageFormat( String pageFormat ) {
             this.pageFormat = pageFormat;
         }
 
         /**
          * @return the dpi
          */
         public int getDpi() {
             return dpi;
         }
 
         /**
          * @param dpi
          *            the dpi to set
          */
         public void setDpi( int dpi ) {
             this.dpi = dpi;
         }
 
         /**
          * @param pageWidth
          *            the individuell width of the page to print (use this and height instad of pageFormat)
          */
         public void setPageWidth( float pageWidth ) {
             this.pageWidth = pageWidth;
         }
 
         /**
          * 
          * @return
          */
         public float getPageWidth() {
             return pageWidth;
         }
 
         /**
          * 
          * @param pageHeight
          *            the individuell height of the page to print (use this and height instad of pageFormat)
          */
         public void setPageHeight( float pageHeight ) {
             this.pageHeight = pageHeight;
         }
 
         /**
          * 
          * @return
          */
         public float getPageHeight() {
             return pageHeight;
         }
 
     }
 
 }
