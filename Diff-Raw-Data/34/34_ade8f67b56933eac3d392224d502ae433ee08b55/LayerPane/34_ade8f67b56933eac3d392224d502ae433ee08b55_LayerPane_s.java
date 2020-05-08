 //$HeadURL$
 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2008 by:
  Department of Geography, University of Bonn
  http://www.giub.uni-bonn.de/deegree/
  lat/lon GmbH
  http://www.lat-lon.de
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  Contact:
 
  Andreas Poth
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
  E-Mail: poth@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 package org.deegree.igeo.views;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.framework.util.MapUtils;
 import org.deegree.graphics.Layer;
 import org.deegree.graphics.MapFactory;
 import org.deegree.graphics.MapView;
 import org.deegree.graphics.Theme;
 import org.deegree.graphics.sld.UserStyle;
 import org.deegree.igeo.ChangeListener;
 import org.deegree.igeo.ValueChangedEvent;
 import org.deegree.igeo.dataadapter.DataAccessAdapter;
 import org.deegree.igeo.dataadapter.FeatureAdapter;
 import org.deegree.igeo.dataadapter.GridCoverageAdapter;
 import org.deegree.igeo.mapmodel.LayerChangedEvent;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.mapmodel.MapModelChangedEvent;
 import org.deegree.igeo.mapmodel.NamedStyle;
 import org.deegree.igeo.mapmodel.LayerChangedEvent.LAYER_CHANGE_TYPE;
 import org.deegree.igeo.mapmodel.MapModelChangedEvent.CHANGE_TYPE;
 import org.deegree.igeo.modules.FeatureLayer;
 import org.deegree.igeo.modules.IModule;
 import org.deegree.igeo.modules.LazyFeatureLayer;
 import org.deegree.igeo.modules.LazyRasterLayer;
 import org.deegree.model.Identifier;
 import org.deegree.model.crs.CoordinateSystem;
 import org.deegree.model.spatialschema.Envelope;
 
 /**
  * 
  * The <code>LayerPanel</code> represents a layer of a map as JComponent. One layerPanel is equivalent to the graphical
  * representation of one layer defined in the project configurationfile.
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class LayerPane implements ChangeListener {
 
     private static final long serialVersionUID = 2004725249423908329L;
 
     private static final ILogger LOG = LoggerFactory.getLogger( LayerPane.class );
 
     private MapModel mapModel;
 
     private org.deegree.igeo.mapmodel.Layer layer;
 
     private MapView mapView;
 
     private IModule<?> parentModule;
 
     private boolean valid;
 
     /**
      * 
      * @param identifier
      */
     public LayerPane() {
         valid = false;
     }
 
     /**
      * 
      * @param g
      */
     public void paint( Graphics g ) {
         if ( g != null && layer.isVisible() ) {
             mapView.setBoundingBox( this.mapModel.getEnvelope() );
             try {
                 if ( !valid ) {
                     createMapView();
                 }
                 mapView.paint( g );
             } catch ( Throwable e ) {
                 DialogFactory.openErrorDialog( "application", (Component) parentModule.getViewForm(), e.getMessage(),
                                                "rendering error", e );
                 LOG.logError( e.getMessage(), e );
             }
         }
     }
 
     /**
      * 
      * 
      * @param style
      * @param dataAccessAdapter
      * @param crs
      * @return list of themes out of the style and several dataAccessAdapters of a layer
      * @throws Exception
      */
     public static List<Theme> createThemes( NamedStyle style, List<DataAccessAdapter> dataAccessAdapter,
                                             CoordinateSystem crs )
                             throws Exception {
         UserStyle[] userStyle = new UserStyle[1];
         if ( style != null && style.getStyle() instanceof UserStyle ) {
             userStyle[0] = (UserStyle) style.getStyle();
         }
 
         List<Theme> themes = new ArrayList<Theme>();
 
         for ( DataAccessAdapter dataAccess : dataAccessAdapter ) {
             Layer deegreeLayer = null;
             Identifier layerId = dataAccess.getLayer().getIdentifier();
 
             if ( dataAccess instanceof GridCoverageAdapter ) {
                 GridCoverageAdapter gridCoverageAdapter = (GridCoverageAdapter) dataAccess;
                 deegreeLayer = new LazyRasterLayer( layerId.getAsQualifiedString(), crs, gridCoverageAdapter );
                 themes.add( MapFactory.createTheme( layerId.getAsQualifiedString(), deegreeLayer, userStyle ) );
             } else if ( dataAccess instanceof FeatureAdapter ) {
                 FeatureAdapter featureAdapter = (FeatureAdapter) dataAccess;
                 if ( featureAdapter.getDatasource().isLazyLoading() ) {
                     deegreeLayer = new LazyFeatureLayer( featureAdapter, crs );
                 } else {
                     deegreeLayer = new FeatureLayer( featureAdapter, crs );
                 }
                 themes.add( MapFactory.createTheme( layerId.getAsQualifiedString(), deegreeLayer, userStyle ) );
             }
 
         }
         return themes;
     }
 
     private void createMapView()
                             throws ViewException {
         try {
             Envelope extent = this.mapModel.getEnvelope();
             List<Theme> layerThemes = createThemes( this.layer.getCurrentStyle(), this.layer.getDataAccess(),
                                                     extent.getCoordinateSystem() );
             mapView = MapFactory.createMapView( this.layer.getTitle(), extent, extent.getCoordinateSystem(),
                                                 layerThemes.toArray( new Theme[layerThemes.size()] ),
                                                 MapUtils.DEFAULT_PIXEL_SIZE );
         } catch ( Exception e ) {
             LOG.logError( e.getMessage(), e );
             throw new ViewException( e.getMessage(), e );
         }
         valid = true;
     }
 
     /**
      * @return the visibility of this layer panel
      */
     public boolean isVisible() {
         return this.layer.isVisible();
     }
 
     /**
      * 
      * @param mapModel
      * @param layer
      * @throws ViewException
      */
     public void setModel( MapModel mapModel, org.deegree.igeo.mapmodel.Layer layer )
                             throws ViewException {
         this.layer = layer;
         this.layer.removeChangeListener( this );
         this.layer.addChangeListener( this );
         this.mapModel = mapModel;
         this.mapModel.removeChangeListener( this );
         this.mapModel.addChangeListener( this );
         createMapView();
     }
 
     /**
      * 
      * @param parentModule
      */
     public void setParentModule( IModule<?> parentModule ) {
         this.parentModule = parentModule;
     }
 
     /**
      * 
      * @return wrapped layer
      */
     public org.deegree.igeo.mapmodel.Layer getLayer() {
         return layer;
     }
 
     /**
      * 
      * @return underlying map model (enables access to all layers and their data)
      */
     public MapModel getMapModel() {
         return mapModel;
     }
 
     /**
      * 
      * @return module a LayerPane is owned by
      */
     public IModule<?> getParentModule() {
         return parentModule;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.presenter.ChangeListener#valueChanged(org.deegree.igeo.presenter.ValueChangedEvent)
      */
     public void valueChanged( ValueChangedEvent event ) {
 
         if ( event instanceof LayerChangedEvent ) {
             LAYER_CHANGE_TYPE ct = ( (LayerChangedEvent) event ).getChangeType();
             switch ( ct ) {
             case dataChanged:
             case datasourceChanged:
             case datasourceAdded:
             case datasourceRemoved:
             case scaleRangeChanged:
             case stylesSet:
             case visibilityChanged: {
                 valid = false;
             }
             }
 
         } else if ( event instanceof MapModelChangedEvent ) {
            CHANGE_TYPE ct = ( (MapModelChangedEvent) event ).getChangeType();
            switch ( ct ) {
            case crsChanged:
            case targetDeviceChanged: {
                valid = false;
            }
            }
         }
 
     }
 
     /**
      * @return the valid
      */
     public boolean isValid() {
         return valid;
     }
 
 }
