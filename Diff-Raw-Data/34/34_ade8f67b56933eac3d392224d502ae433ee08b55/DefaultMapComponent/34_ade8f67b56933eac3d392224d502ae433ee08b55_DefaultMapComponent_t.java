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
 package org.deegree.igeo.views.swing.map;
 
 import static java.awt.Color.WHITE;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JComponent;
 import javax.swing.OverlayLayout;
 import javax.swing.Timer;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.framework.utils.MapTools;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.ChangeListener;
 import org.deegree.igeo.ValueChangedEvent;
 import org.deegree.igeo.commands.digitize.DeleteFeatureCommand;
 import org.deegree.igeo.commands.digitize.MoveFeatureCommand;
 import org.deegree.igeo.commands.digitize.UpdateFeatureCommand;
 import org.deegree.igeo.commands.model.ZoomCommand;
 import org.deegree.igeo.config.ViewFormType;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.mapmodel.Layer;
 import org.deegree.igeo.mapmodel.LayerChangedEvent;
 import org.deegree.igeo.mapmodel.LayerGroup;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.mapmodel.MapModelChangedEvent;
 import org.deegree.igeo.mapmodel.MapModelVisitor;
 import org.deegree.igeo.modules.IModule;
 import org.deegree.igeo.settings.Settings;
 import org.deegree.igeo.views.DialogFactory;
 import org.deegree.igeo.views.IView;
 import org.deegree.igeo.views.LayerPane;
 import org.deegree.igeo.views.ViewException;
 import org.deegree.igeo.views.swing.ControlElement;
 import org.deegree.igeo.views.swing.Footer;
 import org.deegree.igeo.views.swing.MapMouseCoordsFooterEntry;
 import org.deegree.igeo.views.swing.PopUpMenu;
 import org.deegree.igeo.views.swing.util.PopUpRegister;
 import org.deegree.kernel.CommandProcessedEvent;
 import org.deegree.kernel.CommandProcessedListener;
 import org.deegree.model.Identifier;
 import org.deegree.model.feature.FeatureCollection;
 import org.deegree.model.spatialschema.Envelope;
 import org.deegree.model.spatialschema.EnvelopeImpl;
 
 /**
  * 
  * <code>DefaultMapComponent</code>
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class DefaultMapComponent extends JComponent implements IView<Container>, ChangeListener,
                                                    CommandProcessedListener, MapModelVisitor {
 
     private static final long serialVersionUID = 417784357523678071L;
 
     private static final ILogger LOG = LoggerFactory.getLogger( DefaultMapComponent.class );
 
     private String mmcFooterentryName = "MapMouseCoordinates";
 
     private IModule<Container> owner;
 
     MapModel mapModel;
 
     private ApplicationContainer<Container> appContainer;
 
     private PopUpMenu popup;
 
     private ControlElement popUpController;
 
     private Footer privateFooter;
 
     private static MapMouseCoordsLabel mapCoordsLabel;
 
     private Map<String, LayerPane> panes = new HashMap<String, LayerPane>();
 
     transient private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
 
     private HighlightRenderer highlightRenderer;
 
     /**
      * 
      * 
      */
     public DefaultMapComponent() {
         setLayout( new OverlayLayout( this ) );
         setDoubleBuffered( true );
         setVisible( true );
         addComponentListener( new DMCComponentListener() );
     }
 
     /**
      * @return popup controller
      */
     public ControlElement getPopUpController() {
         return this.popUpController;
     }
 
     /**
      * 
      * @param footer
      */
     public void setPrivateFooter( Footer footer ) {
         this.privateFooter = footer;
     }
 
     /**
      * @return the privateFooter
      */
     public Footer getPrivateFooter() {
         return privateFooter;
     }
 
     /**
      * @return the name of the footer entry, showing th map mouse coordinates of this map component
      */
     public String getMmcFooterentryName() {
         return mmcFooterentryName;
     }
 
     @Override
     public void paint( Graphics g ) {
         // clear the background
         // seems to be crazy - but works...
         ( (Graphics2D) g ).setBackground( WHITE );
         g.clearRect( 0, 0, this.mapModel.getTargetDevice().getPixelWidth(),
                      this.mapModel.getTargetDevice().getPixelHeight() );
         super.paint( g );
         highlightRenderer.highlightFeatures( g );
 
     }
 
     /**
      * Handles changes of order of the layer components of the map (layer inserted, removed or layer order changes).
      * 
      * @throws ViewException
      * 
      */
     private void reorderLayers()
                             throws ViewException {
         panes.clear();
         images.clear();
 
         Component[] comps = getComponents();
         for ( int i = 0; i < comps.length; i++ ) {
             if ( comps[i] instanceof LayerComponent ) {
                 LayerPane lp = ( (LayerComponent) comps[i] ).getLayerPane();
                if ( mapModel.getLayerByIdentifier( lp.getLayer().getIdentifier() ) == null ) {
                    // this is necessary to ensure proper gc!
                    lp.getLayer().destroy();
                    mapModel.removeChangeListener( lp );
                    continue;
                }
                 panes.put( lp.getLayer().getIdentifier().getAsQualifiedString(), lp );
                 images.put( lp.getLayer().getIdentifier().getAsQualifiedString(),
                             ( (LayerComponent) comps[i] ).getImage() );
             }
         }
         // remove all layer components
         Component[] components = getComponents();
         List<Component> tmp = new ArrayList<Component>();
         // first collect components to remove ...
         for ( int i = 0; i < components.length; i++ ) {
             if ( components[i] instanceof LayerComponent ) {
                 tmp.add( getComponent( i ) );
             }
         }
         // ... than remove them
         for ( Component component : tmp ) {
             remove( component );
         }
 
         try {
             mapModel.walkLayerTree( this );
         } catch ( Exception e ) {
             throw new ViewException( e.getMessage(), e );
         }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.mapmodel.MapModelVisitor#visit(org.deegree.igeo.mapmodel.Layer)
      */
     public void visit( Layer layer )
                             throws Exception {
         LayerComponent layerComponent = null;
         if ( panes.get( layer.getIdentifier().getAsQualifiedString() ) != null ) {
             layerComponent = new LayerComponent( panes.get( layer.getIdentifier().getAsQualifiedString() ) );
             layerComponent.setForceDeepRepaint( false );
             layerComponent.setImage( images.get( layer.getIdentifier().getAsQualifiedString() ) );
         } else {
             layerComponent = createNewLayerComponent( layer );
         }
         layerComponent.setBounds( 0, 0, this.mapModel.getTargetDevice().getPixelWidth(),
                                   this.mapModel.getTargetDevice().getPixelHeight() );
         add( layerComponent );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.mapmodel.MapModelVisitor#visit(org.deegree.igeo.mapmodel.LayerGroup)
      */
     public void visit( LayerGroup layerGroup )
                             throws Exception {
         // do nothing
     }
 
     /**
      * Create a new JLayerComponent out of the layerId of the layer adapter and add the component to the map component.
      * This map component class will be registered as change listener to be informed about changes of the map model.
      * 
      * @param layer
      *            layer
      * @throws ViewException
      */
     private LayerComponent createNewLayerComponent( Layer layer )
                             throws ViewException {
 
         // create a JLayerPanel out of the layer adapter
         LayerPane lp = new LayerPane();
         lp.setModel( this.mapModel, layer );
         lp.setParentModule( this.owner );
         // assign LayerPane as listener to its layer to be informed about changes that
         // requires a repainting
         layer.addChangeListener( lp );
         LayerComponent layerComponent = new LayerComponent( lp );
         layerComponent.setBounds( 0, 0, this.mapModel.getTargetDevice().getPixelWidth(),
                                   this.mapModel.getTargetDevice().getPixelHeight() );
 
         // add current layer component to the container
         return layerComponent;
     }
 
     // /////////////////////////////////////////////////////////////////////////////////
     // IView
     // /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * @param viewForm
      * @throws Exception
      */
     public void init( ViewFormType viewForm )
                             throws Exception {
 
         appContainer = this.owner.getApplicationContainer();
         appContainer.getCommandProcessor().addCommandProcessedListener( this );
 
         String mmId = owner.getInitParameter( "assignedMapModel" );
         this.mapModel = appContainer.getMapModel( new Identifier( mmId ) );
 
         // register as change listener to be informed when assigned map model changes
         this.mapModel.addChangeListener( this );
 
         // adjust size and extent of the map model before painting the map
         MapTools.adjustMapModelExtent( this.mapModel.getTargetDevice().getPixelWidth(),
                                        this.mapModel.getTargetDevice().getPixelHeight(), this.mapModel );
 
         // add JLayerPanels
         mapModel.walkLayerTree( this );
 
         // write the coordinates of the mouse in the footer
         // mapCoordsLabel must be created if it's null or if wether the global footer
         // not the private/local footer of a map window contains a map coordinate label
         if ( mapCoordsLabel == null ) {
             mapCoordsLabel = new MapMouseCoordsLabel( appContainer );
             if ( privateFooter != null && !privateFooter.hasEntry( getMmcFooterentryName() ) ) {
                 privateFooter.addEntry( new MapMouseCoordsFooterEntry( getMmcFooterentryName(), mapCoordsLabel ) );
             } else if ( appContainer.getFooter() != null
                         && !appContainer.getFooter().hasEntry( getMmcFooterentryName() ) ) {
                 appContainer.getFooter().addEntry( new MapMouseCoordsFooterEntry( getMmcFooterentryName(),
                                                                                   mapCoordsLabel ) );
             }
         } else {
             if ( privateFooter != null && !privateFooter.hasEntry( getMmcFooterentryName() ) ) {
                 privateFooter.addEntry( new MapMouseCoordsFooterEntry( getMmcFooterentryName(), mapCoordsLabel ) );
             } else if ( appContainer.getFooter() != null
                         && !appContainer.getFooter().hasEntry( getMmcFooterentryName() ) ) {
                 appContainer.getFooter().addEntry( new MapMouseCoordsFooterEntry( getMmcFooterentryName(),
                                                                                   mapCoordsLabel ) );
             }
         }
         mapCoordsLabel.updateCRSList();
 
         highlightRenderer = new HighlightRenderer( mapModel, this );
 
         // register as mouseMotionListener to update the mouse coordinates when mouse moves
         addMouseMotionListener( new DMCMouseMotionListener() );
         // register keylister to zoom and pan map via keyboard
         addKeyListener( new DMCKeyListener() );
         // register mouse listener to request window focus
         addMouseListener( new DMCMouseListener() );
         // register mouse wheel listener to perform zooming by mouse wheel
         addMouseWheelListener( new DMCMouseWheelListener() );
 
         setPreferredSize( new Dimension( this.mapModel.getTargetDevice().getPixelWidth(),
                                          this.mapModel.getTargetDevice().getPixelHeight() ) );
 
     }
 
     @Override
     public boolean isFocusable() {
         return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.views.IView#registerModule(org.deegree.client.application.modules.IModule)
      */
     public void registerModule( IModule<Container> module ) {
         this.owner = module;
         popUpController = PopUpRegister.registerPopups( module.getApplicationContainer(), this, owner, popUpController,
                                                         new DMCPopupListener() );
         popup = (PopUpMenu) popUpController.getView();
     }
 
     /**
      * performs an update by invoking {@link #repaint()}
      */
     public void update() {
         repaint();
     }
 
     // /////////////////////////////////////////////////////////////////////////////////
     // ComandProcessedListener
     // /////////////////////////////////////////////////////////////////////////////////
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.deegree.client.presenter.connector.CommandProcessedListener#commandProcessed(org.deegree.client.presenter
      * .connector.CommandProcessedEvent)
      */
     public void commandProcessed( CommandProcessedEvent event ) {
 
         if ( event.getSource() instanceof MoveFeatureCommand || event.getSource() instanceof UpdateFeatureCommand
              || event.getSource() instanceof DeleteFeatureCommand ) {
             for ( int i = 0; i < getComponentCount(); i++ ) {
                 if ( getComponent( i ) instanceof LayerComponent ) {
                     // forces the repaint of a layer
                     ( (LayerComponent) getComponent( i ) ).setForceDeepRepaint( true );
                 }
             }
         }
         repaint();
     }
 
     // /////////////////////////////////////////////////////////////////////////////////
     // ChangeListener
     // /////////////////////////////////////////////////////////////////////////////////
 
     public void valueChanged( ValueChangedEvent event ) {
 
         if ( event instanceof MapModelChangedEvent ) {
             MapModelChangedEvent mapModelEvent = (MapModelChangedEvent) event;
             switch ( mapModelEvent.getChangeType() ) {
             case layerInserted:
             case layerOrderChanged:
             case layerRemoved:
             case layerGroupRemoved:
             case layerGroupInserted:
                 try {
                     reorderLayers();
                 } catch ( ViewException e ) {
                     throw new RuntimeException( e.getMessage(), e );
                 }
                 repaint();
                 break;
             case extentChanged:
             case crsChanged:
             case targetDeviceChanged:
                 for ( int i = 0; i < getComponentCount(); i++ ) {
                     if ( getComponent( i ) instanceof LayerComponent ) {
                         ( (LayerComponent) getComponent( i ) ).setForceDeepRepaint( true );
                     }
                 }
                 repaint();
                 break;
             case layerStateChanged:
                 event = (LayerChangedEvent) mapModelEvent.getEmbeddedEvent();
                 break;
             }
         }
         if ( event instanceof LayerChangedEvent ) {
             LayerChangedEvent lce = (LayerChangedEvent) event;
             switch ( lce.getChangeType() ) {
             case dataChanged:
             case datasourceAdded:
             case datasourceRemoved:
             case scaleRangeChanged:
             case stylesSet:
                 Layer layer = lce.getSource();
                 for ( int i = 0; i < getComponentCount(); i++ ) {
                     if ( getComponent( i ) instanceof LayerComponent ) {
                         Layer tmp = ( (LayerComponent) getComponent( i ) ).getLayerPane().getLayer();
                         if ( tmp.getIdentifier().equals( layer.getIdentifier() ) ) {
                             ( (LayerComponent) getComponent( i ) ).setForceDeepRepaint( true );
                             break;
                         }
                     }
                 }
                 repaint();
             case visibilityChanged:
                 repaint();
                 break;
             case featureSelected:
                 FeatureCollection fc = ( (Layer) lce.getSource() ).getSelectedFeatures();
                 List<FeatureCollection> selectedFeatures = new ArrayList<FeatureCollection>();
                 selectedFeatures.add( fc );
                 highlightRenderer.setSelectedFeatures( selectedFeatures );
                 repaint();
                 break;
             case featureUnselected:
                 highlightRenderer.setSelectedFeatures( null );
                 repaint();
                 break;
             }
 
         }
 
     }
 
     // /////////////////////////////////////////////////////////////////////////////////
     // inner class //
     // /////////////////////////////////////////////////////////////////////////////////
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     private class DMCPopupListener extends MouseAdapter {
 
         @Override
         public void mousePressed( MouseEvent e ) {
             maybeShowPopup( e );
         }
 
         @Override
         public void mouseReleased( MouseEvent e ) {
             maybeShowPopup( e );
         }
 
         private void maybeShowPopup( MouseEvent e ) {
 
             if ( e.isPopupTrigger() ) {
                 popup.show( e.getComponent(), e.getX(), e.getY() );
             }
         }
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     public class DMCMouseMotionListener implements MouseMotionListener {
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
          */
         public void mouseDragged( MouseEvent event ) {
             mapCoordsLabel.setMouseCoords( event.getX(), event.getY(), getWidth(), getHeight() );
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
          */
         public void mouseMoved( MouseEvent event ) {
             mapCoordsLabel.setMouseCoords( event.getX(), event.getY(), getWidth(), getHeight() );
         }
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     private class DMCMouseWheelListener implements MouseWheelListener {
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
          */
         public void mouseWheelMoved( MouseWheelEvent e ) {
             EnvelopeImpl tmp = (EnvelopeImpl) mapModel.getEnvelope();
             Envelope env = (Envelope) tmp.clone();
             float zl = appContainer.getSettings().getZoomLevel() / 100f;
             ZoomCommand zc = new ZoomCommand( mapModel );
             if ( e.getWheelRotation() > 0 ) {
                 float sZL = appContainer.getSettings().getZoomLevel();
                 if ( sZL > 95 ) {
                     sZL = 95;
                 }
                 zl = -100f / ( ( 100f - sZL ) );
             } else {
                 if ( zl > 0.45 ) {
                     // correct zoom level because a buffer can not be >= 50% of a box size
                     zl = -0.45f;
                 } else {
                     zl *= -1f;
                 }
             }
             int xx = mapModel.getTargetDevice().getPixelWidth() / 2;
             int yy = mapModel.getTargetDevice().getPixelHeight() / 2;
             zc.setZoom( xx, yy, xx, yy, zl, mapModel.getTargetDevice().getPixelWidth(),
                         mapModel.getTargetDevice().getPixelHeight() );
             try {
                 appContainer.getCommandProcessor().executeSychronously( zc, true );
             } catch ( Exception ex ) {
                 LOG.logError( ex.getMessage(), ex );
                 DialogFactory.openErrorDialog( appContainer.getViewPlatform(), DefaultMapComponent.this,
                                                Messages.getMessage( getLocale(), "$MD11253" ),
                                                Messages.getMessage( getLocale(), "$MD11252", env ), ex );
             }
 
         }
 
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     private class DMCKeyListener extends KeyAdapter {
 
         @Override
         public void keyPressed( KeyEvent e ) {
 
             EnvelopeImpl tmp = (EnvelopeImpl) mapModel.getEnvelope();
             Envelope env = (Envelope) tmp.clone();
             Settings settings = appContainer.getSettings();
             ZoomCommand zc = null;
             if ( e.getKeyCode() == KeyEvent.VK_DOWN ) {
                 zc = new ZoomCommand( mapModel );
                 float pan = settings.getPanLevel() / 100f;
                 env = env.translate( 0, env.getHeight() * -1 * pan );
                 zc.setZoomBox( env, mapModel.getTargetDevice().getPixelWidth(),
                                mapModel.getTargetDevice().getPixelHeight() );
             } else if ( e.getKeyCode() == KeyEvent.VK_UP ) {
                 zc = new ZoomCommand( mapModel );
                 float pan = settings.getPanLevel() / 100f;
                 env = env.translate( 0, env.getHeight() * pan );
                 zc.setZoomBox( env, mapModel.getTargetDevice().getPixelWidth(),
                                mapModel.getTargetDevice().getPixelHeight() );
             } else if ( e.getKeyCode() == KeyEvent.VK_LEFT ) {
                 zc = new ZoomCommand( mapModel );
                 float pan = settings.getPanLevel() / 100f;
                 env = env.translate( env.getWidth() * -1 * pan, 0 );
                 zc.setZoomBox( env, mapModel.getTargetDevice().getPixelWidth(),
                                mapModel.getTargetDevice().getPixelHeight() );
             } else if ( e.getKeyCode() == KeyEvent.VK_RIGHT ) {
                 zc = new ZoomCommand( mapModel );
                 float pan = settings.getPanLevel() / 100f;
                 env = env.translate( env.getWidth() * pan, 0 );
                 zc.setZoomBox( env, mapModel.getTargetDevice().getPixelWidth(),
                                mapModel.getTargetDevice().getPixelHeight() );
             } else if ( e.getKeyCode() == KeyEvent.VK_PLUS ) {
                 zc = new ZoomCommand( mapModel );
                 float zl = appContainer.getSettings().getZoomLevel() / 100f;
                 if ( zl > 0.45 ) {
                     // correct zoom level because a buffer can not be >= 50% of a box size
                     zl = -0.45f;
                 } else {
                     zl *= -1f;
                 }
                 int xx = mapModel.getTargetDevice().getPixelWidth() / 2;
                 int yy = mapModel.getTargetDevice().getPixelHeight() / 2;
                 zc.setZoom( xx, yy, xx, yy, zl, mapModel.getTargetDevice().getPixelWidth(),
                             mapModel.getTargetDevice().getPixelHeight() );
             } else if ( e.getKeyCode() == KeyEvent.VK_MINUS ) {
                 zc = new ZoomCommand( mapModel );
                 float sZL = appContainer.getSettings().getZoomLevel();
                 if ( sZL > 95 ) {
                     sZL = 95;
                 }
                 float zl = -100f / ( ( 100f - sZL ) );
                 int xx = mapModel.getTargetDevice().getPixelWidth() / 2;
                 int yy = mapModel.getTargetDevice().getPixelHeight() / 2;
                 zc.setZoom( xx, yy, xx, yy, zl, mapModel.getTargetDevice().getPixelWidth(),
                             mapModel.getTargetDevice().getPixelHeight() );
             }
 
             if ( zc != null ) {
                 try {
                     appContainer.getCommandProcessor().executeSychronously( zc, true );
                 } catch ( Exception ex ) {
                     LOG.logError( ex.getMessage(), ex );
                     DialogFactory.openErrorDialog( appContainer.getViewPlatform(), DefaultMapComponent.this,
                                                    Messages.getMessage( getLocale(), "$MD11253" ),
                                                    Messages.getMessage( getLocale(), "$MD11252", env ), ex );
                 }
             }
 
         }
 
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     public class DMCMouseListener extends MouseAdapter {
 
         @Override
         public void mouseEntered( MouseEvent e ) {
             requestFocusInWindow( true );
         }
 
         @Override
         public void mouseExited( MouseEvent e ) {
             requestFocusInWindow( false );
         }
 
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     class DMCComponentListener extends ComponentAdapter implements ActionListener {
 
         private Timer timer;
 
         private ComponentEvent event;
 
         @Override
         public synchronized void componentResized( ComponentEvent event ) {
             if ( timer != null ) {
                 timer.stop();
             }
 
             timer = new Timer( 100, this );
             this.event = event;
             timer.start();
         }
 
         public void actionPerformed( ActionEvent e ) {
             MapTools.adjustMapModelExtent( event.getComponent().getWidth(), event.getComponent().getHeight(), mapModel );
             timer.stop();
             timer = null;
         }
 
     }
 
 }
