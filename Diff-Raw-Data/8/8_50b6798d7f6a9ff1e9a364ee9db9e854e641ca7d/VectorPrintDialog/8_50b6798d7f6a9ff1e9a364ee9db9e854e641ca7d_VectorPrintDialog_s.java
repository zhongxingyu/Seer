 //$HeadURL$
 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2007 by:
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
 package org.deegree.igeo.views.swing.print;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.prefs.Preferences;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.xml.transform.OutputKeys;
 
 import org.deegree.datatypes.QualifiedName;
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.framework.util.MapUtils;
 import org.deegree.framework.util.StringTools;
 import org.deegree.framework.xml.NamespaceContext;
 import org.deegree.framework.xml.XMLFragment;
 import org.deegree.framework.xml.XMLTools;
 import org.deegree.graphics.transformation.GeoTransform;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.commands.VectorPrintCommand;
 import org.deegree.igeo.commands.VectorPrintCommand.PrintDescriptionBean;
 import org.deegree.igeo.commands.model.AddMemoryLayerCommand;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.io.FileSystemAccess;
 import org.deegree.igeo.io.FileSystemAccessFactory;
 import org.deegree.igeo.mapmodel.Layer;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.modules.DefaultMapModule;
 import org.deegree.igeo.modules.IModule;
 import org.deegree.igeo.state.mapstate.MapTool;
 import org.deegree.igeo.views.DialogFactory;
 import org.deegree.igeo.views.HelpManager;
 import org.deegree.igeo.views.swing.ButtonGroup;
 import org.deegree.igeo.views.swing.HelpFrame;
 import org.deegree.igeo.views.swing.util.GenericFileChooser;
 import org.deegree.igeo.views.swing.util.GenericFileChooser.FILECHOOSERTYPE;
 import org.deegree.igeo.views.swing.util.IGeoFileFilter;
 import org.deegree.igeo.views.swing.util.IconRegistry;
 import org.deegree.model.spatialschema.Envelope;
 import org.deegree.model.spatialschema.Geometry;
 import org.deegree.model.spatialschema.GeometryFactory;
 import org.deegree.model.spatialschema.Point;
 import org.deegree.ogcbase.CommonNamespaces;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.lowagie.text.PageSize;
 import com.lowagie.text.Rectangle;
 
 /**
  * 
  * Dialog for printing a map as vector pdf document via iText
  * 
  * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  */
 public class VectorPrintDialog extends javax.swing.JDialog {
 
     private static final long serialVersionUID = -3920635540202430586L;
 
     private static final ILogger LOG = LoggerFactory.getLogger( VectorPrintDialog.class );
 
     private JButton btPrint;
 
     private JTextField tfOutputFile;
 
     private JButton btOutputFile;
 
     private JPanel pnOutput;
 
     private JComboBox cbDPI;
 
     private JPanel pnDPI;
 
     private JComboBox cbScale;
 
     private JRadioButton rbVariable;
 
     private JRadioButton rbConst;
 
     private JLabel lbPageSize;
 
     private JComboBox cbPageFormat;
 
     private JLabel lbPageWidth;
 
     private JSpinner tfPageWidth;
 
     private JLabel lbPageUnitW;
 
     private JLabel lbPageHeight;
 
     private JSpinner tfPageHeight;
 
     private JLabel lbPageUnitH;
 
     private JPanel pnFormat;
 
     private JSpinner spMapBottom;
 
     private JSpinner spMapLeft;
 
     private JLabel lbMapBottom;
 
     private JLabel lbMapLeft;
 
     private JLabel lbHeight;
 
     private JLabel lbWidth;
 
     private JLabel lbTop;
 
     private JLabel lbLeft;
 
     private JSpinner spHeight;
 
     private JSpinner spWidth;
 
     private JSpinner spTop;
 
     private JSpinner spLeft;
 
     private JLabel lb4;
 
     private JLabel lb3;
 
     private JLabel lb2;
 
     private JLabel lb1;
 
     private JPanel pnScale;
 
     private JPanel pnMapCoord;
 
     private JPanel pnLayoutPosition;
 
     private JButton btSave;
 
     private JButton btLoad;
 
     private JPanel pnFile;
 
     private PreviewPanel pnPreview;
 
     private JPanel pnPrint;
 
     private JButton btHelp;
 
     private JPanel pnHelp;
 
     private JButton btCancel;
 
     private JPanel pnButtons;
 
     private ButtonGroup bg = new ButtonGroup();
 
     private ApplicationContainer<?> appContainer;
 
     private MapModel mapModel;
 
     private Layer previewLayer;
 
     private PrintSizeListener printSizeListener = new PrintSizeListener();
 
     private MouseListener ml = new PrintMouseListener();
 
     private MouseMotionListener mml = new PrintMouseMotionListener();
 
     private Point pressPoint;
 
     /**
      * 
      * @param frame
      */
     public VectorPrintDialog( JFrame frame, ApplicationContainer<?> appContainer ) {
         super( frame );
         this.appContainer = appContainer;
         this.mapModel = appContainer.getMapModel( null );
         MapTool<Container> mt = getAssignedMapModule().getMapTool();
         mt.resetState();
         addWindowListener( new WindowAdapter() {
 
             @Override
             public void windowClosed( WindowEvent e ) {
                 removePreviewLayer();
                 DefaultMapModule<Container> mapModule = getAssignedMapModule();
                 Container jco = mapModule.getMapContainer();
                 jco.removeMouseListener( ml );
                 jco.removeMouseMotionListener( mml );
             }
 
             @Override
             public void windowClosing( WindowEvent e ) {
                 removePreviewLayer();
                 DefaultMapModule<Container> mapModule = getAssignedMapModule();
                 Container jco = mapModule.getMapContainer();
                 jco.removeMouseListener( ml );
                 jco.removeMouseMotionListener( mml );
             }
         } );
         initGUI();
         addPreviewLayer();
         toFront();
         setAlwaysOnTop( true );
     }
 
     private void initGUI() {
         try {
             {
                 GridBagLayout thisLayout = new GridBagLayout();
                 thisLayout.rowWeights = new double[] { 0.0, 0.0 };
                 thisLayout.rowHeights = new int[] { 531, 16 };
                 thisLayout.columnWeights = new double[] { 0.1, 0.1 };
                 thisLayout.columnWidths = new int[] { 7, 20 };
                 getContentPane().setLayout( thisLayout );
                 {
                     pnButtons = new JPanel();
                     FlowLayout pnButtonsLayout = new FlowLayout();
                     pnButtonsLayout.setAlignment( FlowLayout.LEFT );
                     pnButtons.setLayout( pnButtonsLayout );
                     getContentPane().add( pnButtons,
                                           new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     {
                         btPrint = new JButton( Messages.getMessage( getLocale(), "$MD11785" ),
                                                IconRegistry.getIcon( "accept.png" ) );
                         pnButtons.add( btPrint );
                         btPrint.addActionListener( new ActionListener() {
 
                             public void actionPerformed( ActionEvent event ) {
                                 if ( tfOutputFile.getText().trim().length() < 3 ) {
                                     DialogFactory.openWarningDialog( appContainer.getViewPlatform(),
                                                                      VectorPrintDialog.this,
                                                                      Messages.get( "$MD11817" ),
                                                                      Messages.get( "$MD11818" ) );
                                 } else {
                                     doPrint();
                                 }
                             }
                         } );
                     }
                     {
                         btCancel = new JButton( Messages.getMessage( getLocale(), "$MD11786" ),
                                                 IconRegistry.getIcon( "cancel.png" ) );
                         btCancel.addActionListener( new ActionListener() {
 
                             public void actionPerformed( ActionEvent e ) {
                                 VectorPrintDialog.this.dispose();
                             }
                         } );
                         pnButtons.add( btCancel );
 
                     }
                 }
                 {
                     pnHelp = new JPanel();
                     FlowLayout pnHelpLayout = new FlowLayout();
                     pnHelpLayout.setAlignment( FlowLayout.RIGHT );
                     pnHelp.setLayout( pnHelpLayout );
                     getContentPane().add( pnHelp,
                                           new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     {
                         btHelp = new JButton( Messages.getMessage( getLocale(), "$MD11787" ),
                                               IconRegistry.getIcon( "help.png" ) );
                         pnHelp.add( btHelp );
                         btHelp.addActionListener( new ActionListener() {
                             public void actionPerformed( ActionEvent e ) {
                                 HelpFrame hf = HelpFrame.getInstance( new HelpManager( appContainer ) );
                                 hf.setVisible( true );
                                 hf.gotoModule( "Print" );
                             }
                         } );
                     }
                 }
                 {
                     pnPrint = new JPanel();
                     GridBagLayout pnPrintLayout = new GridBagLayout();
                     getContentPane().add( pnPrint,
                                           new GridBagConstraints( 0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     pnPrint.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(), "$MD11788" ) ) );
                     pnPrintLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.1 };
                     pnPrintLayout.rowHeights = new int[] { 78, 111, 126, 65, 60, 7 };
                     pnPrintLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.1 };
                     pnPrintLayout.columnWidths = new int[] { 117, 158, 151, 7 };
                     pnPrint.setLayout( pnPrintLayout );
                     {
                         pnPreview = new PreviewPanel();
                         pnPreview.setLayout( new BorderLayout() );
                         pnPrint.add( pnPreview, new GridBagConstraints( 0, 0, 2, 3, 0.0, 0.0,
                                                                         GridBagConstraints.CENTER,
                                                                         GridBagConstraints.BOTH,
                                                                         new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                         pnPreview.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(),
                                                                                                     "$MD11789" ) ) );
                         {
                             lbPageSize = new JLabel();
                             pnPreview.add( lbPageSize, BorderLayout.SOUTH );
                             lbPageSize.setText( Messages.getMessage( getLocale(), "$MD11790" ) );
                         }
                     }
                     {
                         pnFile = new JPanel();
                         FlowLayout pnFileLayout = new FlowLayout();
                         pnFileLayout.setAlignment( FlowLayout.LEFT );
                         pnFile.setLayout( pnFileLayout );
                         pnPrint.add( pnFile, new GridBagConstraints( 0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                      GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ),
                                                                      0, 0 ) );
                         pnFile.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(),
                                                                                                  "$MD11791" ) ) );
                         {
                             btLoad = new JButton( Messages.getMessage( getLocale(), "$MD11792" ),
                                                   IconRegistry.getIcon( "open.gif" ) );
                             pnFile.add( btLoad );
                             btLoad.addActionListener( new ActionListener() {
 
                                 public void actionPerformed( ActionEvent event ) {
                                     doLoadSettings();
                                 }
                             } );
                         }
                         {
                             btSave = new JButton( Messages.getMessage( getLocale(), "$MD11793" ),
                                                   IconRegistry.getIcon( "save.gif" ) );
                             pnFile.add( btSave );
                             btSave.addActionListener( new ActionListener() {
 
                                 public void actionPerformed( ActionEvent event ) {
                                     doSaveSettings();
                                 }
                             } );
                         }
                     }
                     {
                         pnLayoutPosition = new JPanel();
                         GridBagLayout pnLayoutPositionLayout = new GridBagLayout();
                         pnLayoutPositionLayout.rowWeights = new double[] { 0.0, 0.1, 0.1, 0.1 };
                         pnLayoutPositionLayout.rowHeights = new int[] { 39, 7, 7, 7 };
                         pnLayoutPositionLayout.columnWeights = new double[] { 0.0, 0.0, 0.1 };
                         pnLayoutPositionLayout.columnWidths = new int[] { 69, 95, 7 };
                         pnLayoutPosition.setLayout( pnLayoutPositionLayout );
                         pnPrint.add( pnLayoutPosition, new GridBagConstraints( 2, 0, 2, 2, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.BOTH,
                                                                                new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                         pnLayoutPosition.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(),"$MD11848" ) ) );
                         {
                             lb1 = new JLabel( Messages.getMessage( getLocale(), "$MD11794" ) );
                             pnLayoutPosition.add( lb1, new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             lb2 = new JLabel( Messages.getMessage( getLocale(), "$MD11794" ) );
                             pnLayoutPosition.add( lb2, new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             lb3 = new JLabel( Messages.getMessage( getLocale(), "$MD11794" ) );
                             pnLayoutPosition.add( lb3, new GridBagConstraints( 2, 2, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             lb4 = new JLabel( Messages.getMessage( getLocale(), "$MD11794" ) );
                             pnLayoutPosition.add( lb4, new GridBagConstraints( 2, 3, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             spLeft = new JSpinner( new SpinnerNumberModel( 20, 0, 100000, 1 ) );
                             pnLayoutPosition.add( spLeft, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0,
                                                                                   GridBagConstraints.CENTER,
                                                                                   GridBagConstraints.HORIZONTAL,
                                                                                   new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                             spLeft.addChangeListener( printSizeListener );
                         }
                         {
                             spTop = new JSpinner( new SpinnerNumberModel( 20, 0, 100000, 1 ) );
                             pnLayoutPosition.add( spTop, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0,
                                                                                  GridBagConstraints.CENTER,
                                                                                  GridBagConstraints.HORIZONTAL,
                                                                                  new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                             spTop.addChangeListener( printSizeListener );
                         }
                         {
                             spWidth = new JSpinner( new SpinnerNumberModel( 150, 10, 100000, 1 ) );
                             pnLayoutPosition.add( spWidth, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0,
                                                                                    GridBagConstraints.CENTER,
                                                                                    GridBagConstraints.HORIZONTAL,
                                                                                    new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                             spWidth.addChangeListener( printSizeListener );
                         }
                         {
                             spHeight = new JSpinner( new SpinnerNumberModel( 200, 10, 100000, 1 ) );
                             pnLayoutPosition.add( spHeight, new GridBagConstraints( 1, 3, 1, 1, 0.0, 0.0,
                                                                                     GridBagConstraints.CENTER,
                                                                                     GridBagConstraints.HORIZONTAL,
                                                                                     new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                             spHeight.addChangeListener( printSizeListener );
                         }
                         {
                             lbLeft = new JLabel( Messages.getMessage( getLocale(), "$MD11795" ) );
                             pnLayoutPosition.add( lbLeft, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                                                                                   GridBagConstraints.CENTER,
                                                                                   GridBagConstraints.HORIZONTAL,
                                                                                   new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             lbTop = new JLabel( Messages.getMessage( getLocale(), "$MD11796" ) );
                             pnLayoutPosition.add( lbTop, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0,
                                                                                  GridBagConstraints.CENTER,
                                                                                  GridBagConstraints.HORIZONTAL,
                                                                                  new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             lbWidth = new JLabel( Messages.getMessage( getLocale(), "$MD11797" ) );
                             pnLayoutPosition.add( lbWidth, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0,
                                                                                    GridBagConstraints.CENTER,
                                                                                    GridBagConstraints.HORIZONTAL,
                                                                                    new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             lbHeight = new JLabel( Messages.getMessage( getLocale(), "$MD11798" ) );
                             pnLayoutPosition.add( lbHeight, new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0,
                                                                                     GridBagConstraints.CENTER,
                                                                                     GridBagConstraints.HORIZONTAL,
                                                                                     new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                     }
                     {
                         pnMapCoord = new JPanel();
                         GridBagLayout pnMapCoordLayout = new GridBagLayout();
                         pnPrint.add( pnMapCoord,
                                      new GridBagConstraints( 2, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                              GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                         pnMapCoord.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(),
                                                                                                      "$MD11799" ) ) );
                         pnMapCoordLayout.rowWeights = new double[] { 0.1, 0.1 };
                         pnMapCoordLayout.rowHeights = new int[] { 7, 7 };
                         pnMapCoordLayout.columnWeights = new double[] { 0.0, 0.1, 0.1 };
                         pnMapCoordLayout.columnWidths = new int[] { 68, 7, 7 };
                         pnMapCoord.setLayout( pnMapCoordLayout );
                         {
                             lbMapLeft = new JLabel( Messages.getMessage( getLocale(), "$MD11800" ) );
                             pnMapCoord.add( lbMapLeft, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             lbMapBottom = new JLabel( Messages.getMessage( getLocale(), "$MD11801" ) );
                             pnMapCoord.add( lbMapBottom, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0,
                                                                                  GridBagConstraints.CENTER,
                                                                                  GridBagConstraints.HORIZONTAL,
                                                                                  new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                         {
                             spMapLeft = new JSpinner( new SpinnerNumberModel( 0, -9E9, 9E9, 0.5 ) );
                             spMapLeft.setValue( mapModel.getEnvelope().getMin().getX() );
                             pnMapCoord.add( spMapLeft, new GridBagConstraints( 1, 0, 2, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                             spMapLeft.addChangeListener( printSizeListener );
                         }
                         {
                             spMapBottom = new JSpinner( new SpinnerNumberModel( 0, -9E9, 9E9, 0.5 ) );
                             spMapBottom.setValue( mapModel.getEnvelope().getMin().getY() );
                             pnMapCoord.add( spMapBottom, new GridBagConstraints( 1, 1, 2, 1, 0.0, 0.0,
                                                                                  GridBagConstraints.CENTER,
                                                                                  GridBagConstraints.HORIZONTAL,
                                                                                  new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                             spMapBottom.addChangeListener( printSizeListener );
                         }
                     }
                     {
                         pnScale = new JPanel();
                         GridBagLayout pnScaleLayout = new GridBagLayout();
                         pnPrint.add( pnScale, new GridBagConstraints( 2, 3, 2, 2, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                       GridBagConstraints.BOTH,
                                                                       new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                         pnScale.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(),
                                                                                                   "$MD11802" ) ) );
                         pnScaleLayout.rowWeights = new double[] { 0.1, 0.1 };
                         pnScaleLayout.rowHeights = new int[] { 7, 7 };
                         pnScaleLayout.columnWeights = new double[] { 0.1, 0.1 };
                         pnScaleLayout.columnWidths = new int[] { 7, 7 };
                         pnScale.setLayout( pnScaleLayout );
                         {
                             rbConst = new JRadioButton( Messages.getMessage( getLocale(), "$MD11803" ) );
                             pnScale.add( rbConst, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                                                                           GridBagConstraints.CENTER,
                                                                           GridBagConstraints.HORIZONTAL,
                                                                           new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                             rbConst.setSelected( true );
                             bg.add( rbConst );
                         }
                         {
                             rbVariable = new JRadioButton( Messages.getMessage( getLocale(), "$MD11804" ) );
                             pnScale.add( rbVariable, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0,
                                                                              GridBagConstraints.CENTER,
                                                                              GridBagConstraints.HORIZONTAL,
                                                                              new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                             bg.add( rbVariable );
                         }
                         {
                             int[] sc = StringTools.toArrayInt( Messages.getMessage( getLocale(), "$MD11805" ), ",; " );
                             ListEntry[] le = new ListEntry[sc.length];
                             for ( int i = 0; i < sc.length; i++ ) {
                                 le[i] = new ListEntry( "1:" + sc[i], sc[i] );
                             }
                             cbScale = new JComboBox( new DefaultComboBoxModel( le ) );
                             cbScale.setSelectedIndex( 6 );
                             pnScale.add( cbScale, new GridBagConstraints( 0, 1, 2, 1, 0.0, 0.0,
                                                                           GridBagConstraints.CENTER,
                                                                           GridBagConstraints.HORIZONTAL,
                                                                           new Insets( 0, 9, 0, 9 ), 0, 0 ) );
                             cbScale.setEditable( true );
                             cbScale.addActionListener( new ActionListener() {
 
                                 public void actionPerformed( ActionEvent e ) {
                                     Object o = cbScale.getSelectedItem();
                                     if ( o instanceof String ) {
                                         try {
                                             // handle use defined scales
                                             ListEntry le = null;
                                             if ( ( (String) o ).indexOf( ":" ) > 0 ) {
                                                 String[] t = StringTools.toArray( (String) o, ":", false );
                                                 le = new ListEntry( (String) o, Integer.parseInt( t[1].trim() ) );
                                             } else {
                                                 le = new ListEntry( "1:" + o, Integer.parseInt( (String) o ) );
                                             }
                                             ( (DefaultComboBoxModel) cbScale.getModel() ).addElement( le );
                                             cbScale.setSelectedItem( le );
                                         } catch ( Exception ex ) {
                                             DialogFactory.openWarningDialog( appContainer.getViewPlatform(),
                                                                              VectorPrintDialog.this,
                                                                              Messages.get( "$MD11821" ),
                                                                              Messages.get( "$MD11822" ) );
                                             return;
                                         }
                                     }
                                     updatePreview();
                                 }
                             } );
                         }
                     }
                     {
                         pnFormat = new JPanel();
                         GridBagLayout pnFormatLayout = new GridBagLayout();
                         pnPrint.add( pnFormat, new GridBagConstraints( 0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                        GridBagConstraints.BOTH,
                                                                        new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                         pnFormat.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(),
                                                                                                    "$MD11806" ) ) );
                         pnFormatLayout.rowWeights = new double[] { 0.1 };
                         pnFormatLayout.rowHeights = new int[] { 7 };
                         pnFormatLayout.columnWeights = new double[] { 0.1 };
                         pnFormatLayout.columnWidths = new int[] { 7 };
                         pnFormat.setLayout( pnFormatLayout );
                         {
                             String[] tmp = StringTools.toArray( Messages.getMessage( getLocale(), "$MD11807" ), ",;",
                                                                 true );
                             ListEntry[] le = new ListEntry[tmp.length / 2 + 1];
                             le[0] = new ListEntry( Messages.getMessage( getLocale(), "$MD11829" ), null );
                             for ( int i = 0; i < tmp.length; i += 2 ) {
                                 le[i / 2 + 1] = new ListEntry( tmp[i], tmp[i + 1] );
                             }
                             cbPageFormat = new JComboBox( new DefaultComboBoxModel( le ) );
                             cbPageFormat.setSelectedIndex( 2 );
 
                             pnFormat.add( cbPageFormat, new GridBagConstraints( 0, 0, 3, 1, 0.0, 0.0,
                                                                                 GridBagConstraints.CENTER,
                                                                                 GridBagConstraints.HORIZONTAL,
                                                                                 new Insets( 0, 9, 0, 9 ), 0, 0 ) );
 
                             // width
                             lbPageWidth = new JLabel( Messages.getMessage( getLocale(), "$MD11831" ) );
                             pnFormat.add( lbPageWidth, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 15, 0, 0 ), 0, 0 ) );
                             tfPageWidth = new JSpinner( new SpinnerNumberModel( inMM( PageSize.A4.getWidth() ), 0,
                                                                                 6080, 1 ) );
                             pnFormat.add( tfPageWidth, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 5, 0, 0, 0 ), 0, 0 ) );
 
                             tfPageWidth.setEnabled( false );
                             lbPageUnitW = new JLabel( Messages.getMessage( getLocale(), "$MD11832" ) );
                             pnFormat.add( lbPageUnitW, new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.WEST,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 9, 0, 15 ), 0, 0 ) );
                             
                             // height
                             lbPageHeight = new JLabel( Messages.getMessage( getLocale(), "$MD11830" ) );
                             pnFormat.add( lbPageHeight, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0,
                                                                                 GridBagConstraints.CENTER,
                                                                                 GridBagConstraints.HORIZONTAL,
                                                                                 new Insets( 0, 15, 0, 0 ), 0, 0 ) );
                             tfPageHeight = new JSpinner( new SpinnerNumberModel( inMM( PageSize.A4.getHeight() ), 0,
                                                                                  6080, 1 ) );
                             pnFormat.add( tfPageHeight, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0,
                                                                                 GridBagConstraints.CENTER,
                                                                                 GridBagConstraints.HORIZONTAL,
                                                                                 new Insets( 5, 0, 0, 0 ), 0, 0 ) );
                             tfPageHeight.setEnabled( false );
                             lbPageUnitH = new JLabel( Messages.getMessage( getLocale(), "$MD11832" ) );
                             pnFormat.add( lbPageUnitH, new GridBagConstraints( 2, 2, 1, 1, 0.0, 0.0,
                                                                                GridBagConstraints.CENTER,
                                                                                GridBagConstraints.HORIZONTAL,
                                                                                new Insets( 0, 9, 0, 15 ), 0, 0 ) );
 
                             cbPageFormat.addActionListener( new ActionListener() {
 
                                 public void actionPerformed( ActionEvent e ) {
 
                                     int width;
                                     int height;
                                     ListEntry le = (ListEntry) ( (JComboBox) e.getSource() ).getSelectedItem();
                                     if ( le.value != null ) {
                                         String value = (String) le.value;
                                         Rectangle rect = PageSize.getRectangle( value );
                                         width = (int) Math.round( rect.getWidth() / 72 * 25.4 );
                                         height = (int) Math.round( rect.getHeight() / 72 * 25.4 );
                                         tfPageWidth.setEnabled( false );
                                         tfPageHeight.setEnabled( false );
                                     } else {
                                         tfPageWidth.setEnabled( true );
                                         tfPageHeight.setEnabled( true );
                                         width = ( (Number) tfPageWidth.getValue() ).intValue();
                                         height = ( (Number) tfPageHeight.getValue() ).intValue();
                                     }
                                     // if page format has been changed max size of printed map must be changed
                                     // for new map size (millimeter) left and top border must be considered to
                                     // ensure that printed map does not overlap paper at the right and at the bottom
 
                                     width -= ( ( (Number) spLeft.getValue() ).intValue() * 2 );
                                     height -= ( ( (Number) spTop.getValue() ).intValue() * 2 );
                                     spWidth.setValue( width );
                                     spHeight.setValue( height );
                                     // preview of printed area must be updated
                                     updatePreview();
                                 }
                             } );
                         }
                     }
                     {
                         pnDPI = new JPanel();
                         GridBagLayout pnDPILayout = new GridBagLayout();
                         pnPrint.add( pnDPI, new GridBagConstraints( 0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                     GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ),
                                                                     0, 0 ) );
                         pnDPI.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(), "$MD11808" ) ) );
                         pnDPILayout.rowWeights = new double[] { 0.1 };
                         pnDPILayout.rowHeights = new int[] { 7 };
                         pnDPILayout.columnWeights = new double[] { 0.1 };
                         pnDPILayout.columnWidths = new int[] { 7 };
                         pnDPI.setLayout( pnDPILayout );
                         {
                             final DefaultComboBoxModel cbDPIModel = new DefaultComboBoxModel( new Integer[] { 72, 96,
                                                                                                              150, 300,
                                                                                                              600, 1200,
                                                                                                              2400 } );
                             cbDPI = new JComboBox();
                             pnDPI.add( cbDPI, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                       GridBagConstraints.HORIZONTAL,
                                                                       new Insets( 0, 9, 0, 9 ), 0, 0 ) );
                             cbDPI.setModel( cbDPIModel );
                             cbDPI.setEditable( true );
                             cbDPI.addActionListener( new ActionListener() {
 
                                 public void actionPerformed( ActionEvent e ) {
                                     Object o = cbDPI.getSelectedItem();
                                     if ( o instanceof String ) {
                                         try {
                                             Integer i = Integer.parseInt( o.toString() );
                                             cbDPIModel.addElement( i );
                                             cbDPI.setSelectedItem( i );
                                         } catch ( Exception ex ) {
                                             DialogFactory.openWarningDialog( appContainer.getViewPlatform(),
                                                                              VectorPrintDialog.this,
                                                                              Messages.get( "$MD11819" ),
                                                                              Messages.get( "$MD11820" ) );
                                         }
                                     }
                                 }
                             } );
                         }
                     }
                     {
                         pnOutput = new JPanel();
                         GridBagLayout pnOutputLayout = new GridBagLayout();
                         pnOutputLayout.rowWeights = new double[] { 0.1 };
                         pnOutputLayout.rowHeights = new int[] { 7 };
                         pnOutputLayout.columnWeights = new double[] { 0.1, 0.0, 0.1 };
                         pnOutputLayout.columnWidths = new int[] { 7, 94, 7 };
                         pnOutput.setLayout( pnOutputLayout );
                         pnPrint.add( pnOutput, new GridBagConstraints( 2, 5, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                        GridBagConstraints.BOTH,
                                                                        new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                         pnOutput.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(),
                                                                                                    "$MD11809" ) ) );
                         {
                             btOutputFile = new JButton( Messages.get( "$MD11823" ) );
                             btOutputFile.addActionListener( new ActionListener() {
 
                                 public void actionPerformed( ActionEvent e ) {
                                     Preferences prefs = Preferences.userNodeForPackage( VectorPrintDialog.class );
                                     File file = GenericFileChooser.showSaveDialog( FILECHOOSERTYPE.externalResource,
                                                                                    appContainer,
                                                                                    VectorPrintDialog.this, prefs,
                                                                                    "print definition",
                                                                                    IGeoFileFilter.PDF );
                                     tfOutputFile.setText( file.getAbsolutePath() );
                                 }
                             } );
                             pnOutput.add( btOutputFile, new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0,
                                                                                 GridBagConstraints.CENTER,
                                                                                 GridBagConstraints.HORIZONTAL,
                                                                                 new Insets( 0, 9, 0, 9 ), 0, 0 ) );
                         }
                         {
                             tfOutputFile = new JTextField();
                             pnOutput.add( tfOutputFile, new GridBagConstraints( 0, 0, 2, 1, 0.0, 0.0,
                                                                                 GridBagConstraints.CENTER,
                                                                                 GridBagConstraints.HORIZONTAL,
                                                                                 new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         }
                     }
                 }
             }
             pnPreview.setAreaLeft( ( (Number) spLeft.getValue() ).intValue() );
             pnPreview.setAreaTop( ( (Number) spTop.getValue() ).intValue() );
             pnPreview.setAreaWidth( ( (Number) spWidth.getValue() ).intValue() );
             pnPreview.setAreaHeight( ( (Number) spHeight.getValue() ).intValue() );
             Rectangle rect;
             ListEntry le = (ListEntry) cbPageFormat.getSelectedItem();
             if ( le.value != null ) {
                 rect = PageSize.getRectangle( (String) le.value );
             } else {
                 rect = new Rectangle( inPt( ( (Number) tfPageWidth.getValue() ).intValue() ),
                                       inPt( ( (Number) tfPageHeight.getValue() ).intValue() ) );
             }
             pnPreview.setPageSize( rect );
             this.setBounds( 300, 200, 530, 609 );
         } catch ( Exception e ) {
             e.printStackTrace();
         }
 
         DefaultMapModule<Container> mapModule = getAssignedMapModule();
         Container jco = mapModule.getMapContainer();
         jco.addMouseListener( ml );
         jco.addMouseMotionListener( mml );
     }
 
     private int inPt( int inMM ) {
         return (int) Math.round( ( (double) inMM / 25.4 * 72 ) );
     }
 
     private int inMM( float inPt ) {
         return (int) Math.round( inPt / 72 * 25.4 );
     }
 
     private void doPrint() {
         PrintDescriptionBean pdb = new PrintDescriptionBean();
         pdb.setAreaHeight( ( (Number) spHeight.getValue() ).intValue() );
         pdb.setAreaWidth( ( (Number) spWidth.getValue() ).intValue() );
         pdb.setAreaLeft( ( (Number) spLeft.getValue() ).intValue() );
         pdb.setAreaTop( ( (Number) spTop.getValue() ).intValue() );
         pdb.setDpi( (Integer) cbDPI.getSelectedItem() );
 
         pdb.setMapBottom( ( (Number) spMapBottom.getValue() ).doubleValue() );
         pdb.setMapLeft( ( (Number) spMapLeft.getValue() ).doubleValue() );
 
         ListEntry le = (ListEntry) cbPageFormat.getSelectedItem();
         if ( le.value != null ) {
             pdb.setPageFormat( (String) le.value );
         } else {
             pdb.setPageWidth( inPt( ( (Number) tfPageWidth.getValue() ).intValue() ) );
             pdb.setPageHeight( inPt( ( (Number) tfPageHeight.getValue() ).intValue() ) );
         }
 
         if ( rbConst.isSelected() ) {
             pdb.setScale( (Integer) ( (ListEntry) cbScale.getSelectedItem() ).value );
         } else {
             pdb.setScale( -1 );
         }
         pdb.setTargetFile( tfOutputFile.getText() );
 
         VectorPrintCommand cmd = new VectorPrintCommand();
         cmd.setApplicationContainer( appContainer );
         cmd.setPrintDefinition( pdb );
         try {
             appContainer.getCommandProcessor().executeSychronously( cmd, false );
         } catch ( Exception e ) {
             DialogFactory.openErrorDialog( appContainer.getViewPlatform(), this,
                                            Messages.getMessage( getLocale(), "$MD11810" ),
                                            Messages.getMessage( getLocale(), "$MD11811" ), e );
         }
         DialogFactory.openInformationDialog( appContainer.getViewPlatform(), this,
                                              Messages.getMessage( getLocale(), "$MD11824" ), "INFORMATION" );
     }
 
     /**
      * <pre>
      * <PrintDefinition xmlns="http://www.deegree.org/print">
      *    <AreaHeight>180</AreaHeight>
      *    <AreaWidth>270</AreaWidth>
      *    <AreaLeft>10</AreaLeft>
      *    <AreaTop>10</AreaTop>
      *    <DPI>300</DPI>
      *    <MapLeft>2597678</MapLeft>
      *    <MapBottom>5697438</MapBottom>
      *    <PageFormat>
      *       <Named label="Din A4">A4</Named>
      *       <!-- or -->
      *       <Extent>
      *          <width unit="pt">300.0</width>
      *          <height unit="pt">500.0</height>
      *       <Extent>
      *    </PageFormat>
      *    <Scale>25000</Scale>
      *    <TargetFile>e:/temp/test.pdf</TargetFile>
      * </PrintDefinition>
      * </pre>
      */
     private void doSaveSettings() {
         Preferences prefs = Preferences.userNodeForPackage( VectorPrintDialog.class );
         File file = GenericFileChooser.showSaveDialog( FILECHOOSERTYPE.externalResource, appContainer, this, prefs,
                                                        "print definition", IGeoFileFilter.XML );
         if ( file == null ) {
             // cancel has been pressed
             return;
         }
 
         XMLFragment xml = new XMLFragment( new QualifiedName( "PrintDefinition",
                                                               URI.create( "http://www.deegree.org/print" ) ) );
         Document doc = xml.getRootElement().getOwnerDocument();
         Element root = xml.getRootElement();
         try {
             // fill XML document
             Element el = doc.createElementNS( "http://www.deegree.org/print", "AreaHeight" );
             XMLTools.setNodeValue( el, spHeight.getValue().toString() );
             root.appendChild( el );
             el = doc.createElementNS( "http://www.deegree.org/print", "AreaWidth" );
             XMLTools.setNodeValue( el, spWidth.getValue().toString() );
             root.appendChild( el );
             el = doc.createElementNS( "http://www.deegree.org/print", "AreaLeft" );
             XMLTools.setNodeValue( el, spLeft.getValue().toString() );
             root.appendChild( el );
             el = doc.createElementNS( "http://www.deegree.org/print", "AreaTop" );
             XMLTools.setNodeValue( el, spTop.getValue().toString() );
             root.appendChild( el );
             el = doc.createElementNS( "http://www.deegree.org/print", "DPI" );
             XMLTools.setNodeValue( el, cbDPI.getSelectedItem().toString() );
             root.appendChild( el );
             el = doc.createElementNS( "http://www.deegree.org/print", "MapLeft" );
             XMLTools.setNodeValue( el, spMapLeft.getValue().toString() );
             root.appendChild( el );
             el = doc.createElementNS( "http://www.deegree.org/print", "MapBottom" );
             XMLTools.setNodeValue( el, spMapBottom.getValue().toString() );
             root.appendChild( el );
             el = doc.createElementNS( "http://www.deegree.org/print", "PageFormat" );
             Element subEl;
             ListEntry le = (ListEntry) cbPageFormat.getSelectedItem();
             if ( le.value != null ) {
                 subEl = doc.createElementNS( "http://www.deegree.org/print", "Named" );
                 XMLTools.setNodeValue( subEl, le.value.toString() );
                 subEl.setAttribute( "label", le.title );
             } else {
                 subEl = doc.createElementNS( "http://www.deegree.org/print", "Extent" );
                 Element w = doc.createElementNS( "http://www.deegree.org/print", "width" );
                 XMLTools.setNodeValue( w, Integer.toString( ( (Number) tfPageWidth.getValue() ).intValue() ) );
                 w.setAttribute( "unit", "mm" );
                 subEl.appendChild( w );
                 Element h = doc.createElementNS( "http://www.deegree.org/print", "height" );
                 XMLTools.setNodeValue( h, Integer.toString( ( (Number) tfPageHeight.getValue() ).intValue() ) );
                 h.setAttribute( "unit", "mm" );
                 subEl.appendChild( h );
                 el.appendChild( subEl );
             }
             root.appendChild( el );
             el = doc.createElementNS( "http://www.deegree.org/print", "Scale" );
             if ( rbConst.isSelected() ) {
                 XMLTools.setNodeValue( el, ( (ListEntry) cbScale.getSelectedItem() ).value.toString() );
             } else {
                 XMLTools.setNodeValue( el, "-1" );
             }
             el.setAttribute( "label", ( (ListEntry) cbScale.getSelectedItem() ).title );
             root.appendChild( el );
 
             el = doc.createElementNS( "http://www.deegree.org/print", "TargetFile" );
             XMLTools.setNodeValue( el, tfOutputFile.getText() );
             root.appendChild( el );
 
             // write to file
             FileOutputStream fos = new FileOutputStream( file );
             Properties props = new Properties();
             props.put( OutputKeys.ENCODING, "UTF-8" );
             xml.write( fos, props );
             fos.close();
         } catch ( Exception e ) {
             LOG.logError( e );
             DialogFactory.openErrorDialog( appContainer.getViewPlatform(), this,
                                            Messages.getMessage( getLocale(), "$MD11812" ),
                                            Messages.getMessage( getLocale(), "$MD11813" ), e );
         }
 
     }
 
     /**
      * <pre>
      * <PrintDefinition xmlns="http://www.deegree.org/print">
      *    <AreaHeight>180</AreaHeight>
      *    <AreaWidth>270</AreaWidth>
      *    <AreaLeft>10</AreaLeft>
      *    <AreaTop>10</AreaTop>
      *    <DPI>300</DPI>
      *    <MapLeft>2597678</MapLeft>
      *    <MapBottom>5697438</MapBottom>
      *    <PageFormat>
      *       <Named label="Din A4">A4</Named>
      *       <!-- or -->
      *       <Extent>
      *          <width unit="pt">300</width>
      *          <height unit="pt">500</height>
      *       <Extent>
      *    </PageFormat>
      *    <Scale label="1:25000">25000</Scale>
      *    <TargetFile>e:/temp/test.pdf</TargetFile>
      * </PrintDefinition>
      * </pre>
      */
     private void doLoadSettings() {
         Preferences prefs = Preferences.userNodeForPackage( VectorPrintDialog.class );
         File file = GenericFileChooser.showOpenDialog( FILECHOOSERTYPE.externalResource, appContainer, this, prefs,
                                                        "print definition", IGeoFileFilter.XML );
         if ( file == null ) {
             // cancel has been pressed
             return;
         }
         FileSystemAccessFactory fsaf = FileSystemAccessFactory.getInstance( appContainer );
         try {
             FileSystemAccess fsa = fsaf.getFileSystemAccess( FILECHOOSERTYPE.externalResource );
             URL url = fsa.getFileURL( file.getAbsolutePath() );
 
             XMLFragment xml = new XMLFragment( url );
             NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
             nsc.addNamespace( "prnt", URI.create( "http://www.deegree.org/print" ) );
             int ah = XMLTools.getRequiredNodeAsInt( xml.getRootElement(), "prnt:AreaHeight", nsc );
             int aw = XMLTools.getRequiredNodeAsInt( xml.getRootElement(), "prnt:AreaWidth", nsc );
             int al = XMLTools.getRequiredNodeAsInt( xml.getRootElement(), "prnt:AreaLeft", nsc );
             int at = XMLTools.getRequiredNodeAsInt( xml.getRootElement(), "prnt:AreaTop", nsc );
             int dpi = XMLTools.getRequiredNodeAsInt( xml.getRootElement(), "prnt:DPI", nsc );
             double ml = XMLTools.getRequiredNodeAsDouble( xml.getRootElement(), "prnt:MapLeft", nsc );
             double mb = XMLTools.getRequiredNodeAsDouble( xml.getRootElement(), "prnt:MapBottom", nsc );
             String pf = XMLTools.getNodeAsString( xml.getRootElement(), "prnt:PageFormat/prnt:Named", nsc, null );
             String pfl = XMLTools.getNodeAsString( xml.getRootElement(), "prnt:PageFormat/prnt:Named/@label", nsc, null );
             int pfw = XMLTools.getNodeAsInt( xml.getRootElement(), "prnt:PageFormat/prnt:Extent/prnt:width", nsc,
                                              inMM( PageSize.A4.getWidth() ) );
             int pfh = XMLTools.getNodeAsInt( xml.getRootElement(), "prnt:PageFormat/prnt:Extent/prnt:height", nsc,
                                              inMM( PageSize.A4.getHeight() ) );
             int sc = XMLTools.getNodeAsInt( xml.getRootElement(), "prnt:Scale", nsc, -1 );
             String scl = XMLTools.getNodeAsString( xml.getRootElement(), "prnt:Scale/@label", nsc, null );
             String tf = XMLTools.getNodeAsString( xml.getRootElement(), "prnt:TargetFilet", nsc, "" );
 
            spHeight.setValue( new Integer( ah ) );
            spWidth.setValue( new Integer( aw ) );
            spLeft.setValue( new Integer( al ) );
            spTop.setValue( new Integer( at ) );
             cbDPI.setSelectedItem( new Integer( dpi ) );
             spMapLeft.setValue( new Double( ml ) );
             spMapBottom.setValue( new Double( mb ) );
             if ( pf != null ) {
                 cbPageFormat.setSelectedItem( new ListEntry( pfl, pf ) );
             } else {
                 cbPageFormat.setSelectedIndex( 0 );
                 tfPageHeight.setValue( pfh );
                 tfPageHeight.setEnabled( true );
                 tfPageWidth.setValue( pfw );
                 tfPageWidth.setEnabled( true );
             }
             if ( sc > -1 ) {
                 rbConst.setSelected( true );
                 cbScale.setSelectedItem( new ListEntry( scl, sc ) );
             } else {
                 rbVariable.setSelected( true );
             }
             tfOutputFile.setText( tf );
         } catch ( Exception e ) {
             LOG.logError( e );
             DialogFactory.openErrorDialog( appContainer.getViewPlatform(), this,
                                            Messages.getMessage( getLocale(), "$MD11814" ),
                                            Messages.getMessage( getLocale(), "$MD11815" ), e );
         }
     }
 
     private void updatePreview() {
         removePreviewLayer();
         addPreviewLayer();
 
         Rectangle r;
         int pw;
         int ph;
         ListEntry le = (ListEntry) cbPageFormat.getSelectedItem();
         if ( le.value != null ) {
             tfPageWidth.setEnabled( false );
             tfPageHeight.setEnabled( false );
             r = PageSize.getRectangle( (String) le.value );
             pw = (int) Math.round( r.getWidth() / 72 * 25.4 );
             ph = (int) Math.round( r.getHeight() / 72 * 25.4 );
         } else {
             tfPageWidth.setEnabled( true );
             tfPageHeight.setEnabled( true );
             pw = ( (Number) tfPageWidth.getValue() ).intValue();
             ph = ( (Number) tfPageHeight.getValue() ).intValue();
             r = new Rectangle( inPt( pw ), inPt( ph ) );
         }
 
         lbPageSize.setText( Messages.getMessage( getLocale(), "$MD11816", pw, ph ) );
         pnPreview.setAreaLeft( ( (Number) spLeft.getValue() ).intValue() );
         pnPreview.setAreaTop( ( (Number) spTop.getValue() ).intValue() );
         pnPreview.setAreaWidth( ( (Number) spWidth.getValue() ).intValue() );
         pnPreview.setAreaHeight( ( (Number) spHeight.getValue() ).intValue() );
         pnPreview.setPageSize( r );
         pnPreview.repaint();
     }
 
     private void addPreviewLayer() {
         java.awt.Rectangle rect = getCanvasSize();
         Envelope extent = mapModel.getEnvelope();
         extent = MapUtils.ensureAspectRatio( extent, (Integer) spWidth.getValue(), (Integer) spHeight.getValue() );
         if ( rbConst.isSelected() ) {
             // use scale selected by used
             double currentScale = MapUtils.calcScale( rect.width, rect.height, extent, extent.getCoordinateSystem(),
                                                       0.0254 / (Integer) cbDPI.getSelectedItem() );
             extent = MapUtils.scaleEnvelope( extent, currentScale,
                                              (Integer) ( (ListEntry) cbScale.getSelectedItem() ).value );
         }
         // move rectangle to defined lower left coordinates
         double dx = ( (Number) spMapLeft.getValue() ).doubleValue() - extent.getMin().getX();
         double dy = ( (Number) spMapBottom.getValue() ).doubleValue() - extent.getMin().getY();
         extent.translate( dx, dy );
 
         try {
             Geometry geom = GeometryFactory.createSurface( extent, mapModel.getCoordinateSystem() );
             List<Geometry> list = new ArrayList<Geometry>();
             list.add( geom );
             AddMemoryLayerCommand cmd = new AddMemoryLayerCommand();
             cmd.setApplicationContainer( appContainer );
             cmd.setTitle( "deegree:PrintBorder" );
             cmd.setGeometries( list );
             appContainer.getCommandProcessor().executeSychronously( cmd, true );
             previewLayer = (Layer) cmd.getResult();
         } catch ( Exception e ) {
             LOG.logError( e );
         }
     }
 
     /**
      * Fetches and returns the assigned map module
      * 
      * @return the assigned map module
      */
     @SuppressWarnings("unchecked")
     DefaultMapModule<Container> getAssignedMapModule() {
 
         List<?> list = this.appContainer.findModuleByName( "MapModule" );
         if ( list.size() == 0 ) {
             return (DefaultMapModule<Container>) list.get( 0 );
         } else {
             for ( Object iModule : list ) {
                 String t = ( (IModule) iModule ).getInitParameter( "assignedMapModel" );
                 if ( t != null && t.equals( appContainer.getMapModel( null ).getIdentifier().getValue() ) ) {
                     return (DefaultMapModule<Container>) iModule;
                 }
             }
             return (DefaultMapModule<Container>) list.get( 0 );
         }
     }
 
     private int convert( double millimeter ) {
         return (int) Math.round( millimeter * (Integer) cbDPI.getSelectedItem() / 25.4 );
     }
 
     private java.awt.Rectangle getCanvasSize() {
         int w = convert( (Integer) spWidth.getValue() );
         int h = convert( (Integer) spHeight.getValue() );
         int x = convert( (Integer) spLeft.getValue() );
         int y = convert( (Integer) spTop.getValue() );
         return new java.awt.Rectangle( x, y, w, h );
     }
 
     private void removePreviewLayer() {
         if ( mapModel.exists( previewLayer.getIdentifier() ) ) {
             mapModel.remove( previewLayer );
         }
     }
 
     // /////////////////////////////////////////////////////////////////////////////////////////////////////
     // inner classes
     // /////////////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * 
      * TODO add class documentation here
      * 
      * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version $Revision$, $Date$
      */
     private class PrintSizeListener implements ChangeListener {
 
         /*
          * (non-Javadoc)
          * 
          * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
          */
         public void stateChanged( ChangeEvent arg0 ) {
             updatePreview();
         }
 
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
     private static class ListEntry {
         String title;
 
         Object value;
 
         /**
          * @param title
          * @param value
          */
         public ListEntry( String title, Object value ) {
             this.title = title;
             this.value = value;
         }
 
         @Override
         public String toString() {
             return title;
         }
 
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
     private class PrintMouseListener extends MouseAdapter {
 
         @Override
         public void mousePressed( MouseEvent e ) {
             GeoTransform gt = mapModel.getToTargetDeviceTransformation();
             double x = gt.getSourceX( e.getX() );
             double y = gt.getSourceY( e.getY() );
             pressPoint = GeometryFactory.createPoint( x, y, mapModel.getCoordinateSystem() );
         }
 
         @Override
         public void mouseReleased( MouseEvent e ) {
             GeoTransform gt = mapModel.getToTargetDeviceTransformation();
             double x = gt.getSourceX( e.getX() );
             double y = gt.getSourceY( e.getY() );
             Point releasePoint = GeometryFactory.createPoint( x, y, mapModel.getCoordinateSystem() );
             double dx = releasePoint.getX() - pressPoint.getX();
             double dy = releasePoint.getY() - pressPoint.getY();
             spMapLeft.setValue( ( (Number) spMapLeft.getValue() ).doubleValue() + dx );
             spMapBottom.setValue( ( (Number) spMapBottom.getValue() ).doubleValue() + dy );
             updatePreview();
         }
     }
 
     private class PrintMouseMotionListener extends MouseMotionAdapter {
         @Override
         public void mouseDragged( MouseEvent e ) {
             GeoTransform gt = mapModel.getToTargetDeviceTransformation();
             double x = gt.getSourceX( e.getX() );
             double y = gt.getSourceY( e.getY() );
             Point dragPoint = GeometryFactory.createPoint( x, y, mapModel.getCoordinateSystem() );
             double dx = dragPoint.getX() - pressPoint.getX();
             double dy = dragPoint.getY() - pressPoint.getY();
             spMapLeft.setValue( ( (Number) spMapLeft.getValue() ).doubleValue() + dx );
             spMapBottom.setValue( ( (Number) spMapBottom.getValue() ).doubleValue() + dy );
             pressPoint = dragPoint;
         }
     }
 
 }
