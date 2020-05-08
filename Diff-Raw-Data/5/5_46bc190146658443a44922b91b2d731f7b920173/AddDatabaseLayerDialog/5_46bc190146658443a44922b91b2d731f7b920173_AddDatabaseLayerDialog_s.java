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
 package org.deegree.igeo.views.swing.addlayer;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.prefs.Preferences;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.TitledBorder;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.framework.util.StringTools;
 import org.deegree.framework.utils.CRSUtils;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.commands.model.AddDatabaseLayerCommand;
 import org.deegree.igeo.config.DatabaseDriverUtils;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.jdbc.DatabaseConnectionManager;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.views.DialogFactory;
 import org.deegree.igeo.views.HelpManager;
 import org.deegree.igeo.views.swing.AutoCompleteComboBox;
 import org.deegree.igeo.views.swing.CursorRegistry;
 import org.deegree.igeo.views.swing.HelpFrame;
 import org.deegree.igeo.views.swing.util.IconRegistry;
 import org.deegree.io.DBConnectionPool;
 import org.deegree.io.DBPoolException;
 import org.deegree.kernel.Command;
 
 /**
  * 
  * TODO add class documentation here
  * 
  * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  */
 public class AddDatabaseLayerDialog extends JDialog {
 
     private static final ILogger LOG = LoggerFactory.getLogger( AddDatabaseLayerDialog.class );
 
     private static final long serialVersionUID = -9109608043033778181L;
 
     private JPanel pnDescription;
 
     private JComboBox cbPKColumn;
 
     private JLabel lbPKColumn;
 
     private JButton btConnect;
 
     private JTextField tfLayername;
 
     private JLabel lbLayername;
 
     private JPanel pnLayer;
 
     private JTextField tfDatabase;
 
     private JLabel lbDatabase;
 
     private JCheckBox cbLazyLoading;
 
     private JCheckBox cbAllowTransactions;
 
     private JPanel pnSpace;
 
     private JSpinner spMaxScale;
 
     private JLabel lbMaxScale;
 
     private JSpinner spMinScale;
 
     private JLabel lbMinScale;
 
     private JPanel pnScale;
 
     private JButton btExpert;
 
     private JComboBox cbNativeCRS;
 
     private JLabel lbCRS;
 
     private JComboBox cbGeom;
 
     private JLabel lbGeom;
 
     private JPanel pnHelp;
 
     private JButton btHelp;
 
     private JPanel pnContent;
 
     private JComboBox cbTable;
 
     private JLabel lbTable;
 
     private JButton btTest;
 
     private JPanel pnDatabaseButtons;
 
     private JCheckBox cbSavePassword;
 
     private JPasswordField pwPassword;
 
     private JTextField tfUser;
 
     private JSpinner spPort;
 
     private JTextField tfURL;
 
     private JComboBox cbDriver;
 
     private JLabel lbPassword;
 
     private JLabel lbUser;
 
     private JLabel lbPort;
 
     private JLabel lbURL;
 
     private JLabel lbDriver;
 
     private JPanel pnDatabase;
 
     private JButton btCancel;
 
     private JButton btOK;
 
     private JPanel pnButtons;
 
     private JEditorPane epDescription;
 
     private ApplicationContainer<Container> appCont;
 
     private String geomField;
 
     private String sql;
 
     private String pkColumn;
 
     private static String[] crsList;
 
     static {
         if ( crsList == null ) {
             crsList = CRSUtils.getAvailableEPSGCodesAsArray();
         }
     }
 
     /**
      * 
      * @param appCont
      */
     public AddDatabaseLayerDialog( ApplicationContainer<Container> appCont ) {
         this.appCont = appCont;
         initGUI();
         setLocationRelativeTo( null );
         setVisible( true );
         setModal( true );
     }
 
     private void initGUI() {
         try {
             {
                 GridBagLayout thisLayout = new GridBagLayout();
                 thisLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.1 };
                 thisLayout.rowHeights = new int[] { 243, 292, 49, 7 };
                 thisLayout.columnWeights = new double[] { 0.0, 0.0, 0.1 };
                 thisLayout.columnWidths = new int[] { 220, 287, 7 };
                 getContentPane().setLayout( thisLayout );
                 {
                     pnDescription = new JPanel();
                     BorderLayout pnDescriptionLayout = new BorderLayout();
                     pnDescription.setLayout( pnDescriptionLayout );
                     getContentPane().add( pnDescription,
                                           new GridBagConstraints( 0, 0, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     pnDescription.setBorder( BorderFactory.createTitledBorder( null, Messages.getMessage( getLocale(),
                                                                                                           "$MD11422" ),
                                                                                TitledBorder.LEADING,
                                                                                TitledBorder.DEFAULT_POSITION ) );
                     {
                         epDescription = new JEditorPane();
                         epDescription.setContentType( "text/html" );
                         epDescription.setText( Messages.getMessage( getLocale(), "$MD11448" ) );
                         pnDescription.add( epDescription, BorderLayout.CENTER );
                         epDescription.setEditable( false );
                         epDescription.setBackground( pnDescription.getBackground() );
                     }
                 }
                 {
                     pnButtons = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
                     getContentPane().add( pnButtons,
                                           new GridBagConstraints( 0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     {
                         btOK = new JButton( Messages.getMessage( getLocale(), "$MD11423" ),
                                             IconRegistry.getIcon( "accept.png" ) );
                         pnButtons.add( btOK );
                         btOK.addActionListener( new ActionListener() {
 
                             public void actionPerformed( ActionEvent e ) {
                                 createLayer();
                             }
                         } );
                     }
                     {
                         btCancel = new JButton( Messages.getMessage( getLocale(), "$MD11424" ),
                                                 IconRegistry.getIcon( "cancel.png" ) );
                         pnButtons.add( btCancel );
                         btCancel.addActionListener( new ActionListener() {
 
                             public void actionPerformed( ActionEvent arg0 ) {
                                 dispose();
                             }
                         } );
                     }
                 }
                 {
                     pnHelp = new JPanel();
                     FlowLayout pnHelpLayout = new FlowLayout();
                     pnHelpLayout.setAlignment( FlowLayout.RIGHT );
                     getContentPane().add( pnHelp,
                                           new GridBagConstraints( 2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     pnHelp.setLayout( pnHelpLayout );
                     {
                         btHelp = new JButton( Messages.getMessage( getLocale(), "$MD11425" ),
                                               IconRegistry.getIcon( "help.png" ) );
                         pnHelp.add( btHelp );
                         btHelp.addActionListener( new ActionListener() {
                             public void actionPerformed( ActionEvent e ) {
                                 HelpFrame hf = HelpFrame.getInstance( new HelpManager( appCont ) );
                                 hf.setVisible( true );
                                 hf.gotoModule( "AddLayer" );
                             }
                         } );
                     }
                 }
                 {
                     pnDatabase = new JPanel();
                     GridBagLayout pnDatabaseLayout = new GridBagLayout();
                     getContentPane().add( pnDatabase,
                                           new GridBagConstraints( 1, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     pnDatabase.setBorder( BorderFactory.createTitledBorder( null, Messages.getMessage( getLocale(),
                                                                                                        "$MD11426" ),
                                                                             TitledBorder.LEADING,
                                                                             TitledBorder.DEFAULT_POSITION ) );
                     pnDatabaseLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1 };
                     pnDatabaseLayout.rowHeights = new int[] { 32, 32, 32, 32, 32, 32, 20 };
                     pnDatabaseLayout.columnWeights = new double[] { 0.0, 0.0, 0.1 };
                     pnDatabaseLayout.columnWidths = new int[] { 211, 153, 7 };
                     pnDatabase.setLayout( pnDatabaseLayout );
                     {
                         lbDriver = new JLabel( Messages.getMessage( getLocale(), "$MD11427" ) );
                         pnDatabase.add( lbDriver, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                                                                           GridBagConstraints.CENTER,
                                                                           GridBagConstraints.HORIZONTAL,
                                                                           new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         lbURL = new JLabel( Messages.getMessage( getLocale(), "$MD11428" ) );
                         pnDatabase.add( lbURL, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                        GridBagConstraints.HORIZONTAL,
                                                                        new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         lbPort = new JLabel( Messages.getMessage( getLocale(), "$MD11429" ) );
                         pnDatabase.add( lbPort, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0,
                                                                         GridBagConstraints.CENTER,
                                                                         GridBagConstraints.HORIZONTAL,
                                                                         new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         lbUser = new JLabel( Messages.getMessage( getLocale(), "$MD11430" ) );
                         pnDatabase.add( lbUser, new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0,
                                                                         GridBagConstraints.CENTER,
                                                                         GridBagConstraints.HORIZONTAL,
                                                                         new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         lbPassword = new JLabel( Messages.getMessage( getLocale(), "$MD11431" ) );
                         pnDatabase.add( lbPassword, new GridBagConstraints( 0, 5, 1, 1, 0.0, 0.0,
                                                                             GridBagConstraints.CENTER,
                                                                             GridBagConstraints.HORIZONTAL,
                                                                             new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         cbDriver = new JComboBox( new DefaultComboBoxModel( DatabaseDriverUtils.getDriverLabels() ) );
                         pnDatabase.add( cbDriver, new GridBagConstraints( 1, 0, 2, 1, 0.0, 0.0,
                                                                           GridBagConstraints.CENTER,
                                                                           GridBagConstraints.HORIZONTAL,
                                                                           new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                         cbDriver.addActionListener( new ActionListener() {
 
                             public void actionPerformed( ActionEvent e ) {
                                 changeDatabaseVendor();
                             }
                         } );
                     }
                     {
                         tfURL = new JTextField();
                         pnDatabase.add( tfURL, new GridBagConstraints( 1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                        GridBagConstraints.HORIZONTAL,
                                                                        new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                         // tfURL.setText( "hurricane" );
                     }
                     {
                         spPort = new JSpinner( new SpinnerNumberModel( 5432, 0, 60000, 1 ) );
                         spPort.setEditor( new JSpinner.NumberEditor( spPort, "####" ) );
                         pnDatabase.add( spPort, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0,
                                                                         GridBagConstraints.CENTER,
                                                                         GridBagConstraints.HORIZONTAL,
                                                                         new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                     }
                     {
                         tfUser = new JTextField();
                         pnDatabase.add( tfUser, new GridBagConstraints( 1, 4, 2, 1, 0.0, 0.0,
                                                                         GridBagConstraints.CENTER,
                                                                         GridBagConstraints.HORIZONTAL,
                                                                         new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                     }
                     {
                         pwPassword = new JPasswordField();
                         pnDatabase.add( pwPassword, new GridBagConstraints( 1, 5, 1, 1, 0.0, 0.0,
                                                                             GridBagConstraints.CENTER,
                                                                             GridBagConstraints.HORIZONTAL,
                                                                             new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                     }
                     {
                         cbSavePassword = new JCheckBox( Messages.getMessage( getLocale(), "$MD11433" ) );
                         pnDatabase.add( cbSavePassword, new GridBagConstraints( 1, 6, 2, 1, 0.0, 0.0,
                                                                                 GridBagConstraints.CENTER,
                                                                                 GridBagConstraints.HORIZONTAL,
                                                                                 new Insets( 0, 9, 0, 9 ), 0, 0 ) );
                     }
                     {
                         pnDatabaseButtons = new JPanel();
                         FlowLayout pnDatabaseButtonsLayout = new FlowLayout();
                         pnDatabaseButtonsLayout.setAlignment( FlowLayout.LEFT );
                         pnDatabaseButtons.setLayout( pnDatabaseButtonsLayout );
                         pnDatabase.add( pnDatabaseButtons, new GridBagConstraints( 0, 7, 3, 1, 0.0, 0.0,
                                                                                    GridBagConstraints.CENTER,
                                                                                    GridBagConstraints.BOTH,
                                                                                    new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                         {
                             btTest = new JButton( Messages.getMessage( getLocale(), "$MD11434" ) );
                             pnDatabaseButtons.add( btTest );
                             pnDatabaseButtons.add( getBtConnect() );
                             btTest.addActionListener( new ActionListener() {
                                 public void actionPerformed( ActionEvent e ) {
                                     testConnection();
                                 }
                             } );
                         }
                     }
                     {
                         lbDatabase = new JLabel( Messages.getMessage( getLocale(), "$MD11435" ) );
                         pnDatabase.add( lbDatabase, new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0,
                                                                             GridBagConstraints.CENTER,
                                                                             GridBagConstraints.HORIZONTAL,
                                                                             new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         tfDatabase = new JTextField();
                         pnDatabase.add( tfDatabase, new GridBagConstraints( 1, 3, 2, 1, 0.0, 0.0,
                                                                             GridBagConstraints.CENTER,
                                                                             GridBagConstraints.HORIZONTAL,
                                                                             new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                     }
                 }
                 {
                     pnContent = new JPanel();
                     pnContent.setVisible( false );
                     GridBagLayout pnContentLayout = new GridBagLayout();
                     getContentPane().add( pnContent,
                                           new GridBagConstraints( 1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     getContentPane().add( getPnLayer(),
                                           new GridBagConstraints( 1, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     pnContent.setBorder( BorderFactory.createTitledBorder( Messages.getMessage( getLocale(), "$MD11436" ) ) );
                     pnContentLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
                     pnContentLayout.rowHeights = new int[] { 31, 32, 34, 32, 32, 32, 32, 32 };
                     pnContentLayout.columnWeights = new double[] { 0.0, 0.1, 0.1 };
                     pnContentLayout.columnWidths = new int[] { 208, 7, 7 };
                     pnContent.setLayout( pnContentLayout );
                     {
                         lbTable = new JLabel( Messages.getMessage( getLocale(), "$MD11437" ) );
                         pnContent.add( lbTable, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                                                                         GridBagConstraints.CENTER,
                                                                         GridBagConstraints.HORIZONTAL,
                                                                         new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         cbTable = new JComboBox();
                         pnContent.add( cbTable, new GridBagConstraints( 1, 0, 2, 1, 0.0, 0.0,
                                                                         GridBagConstraints.CENTER,
                                                                         GridBagConstraints.HORIZONTAL,
                                                                         new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                         cbTable.addActionListener( new ActionListener() {
 
                             public void actionPerformed( ActionEvent e ) {
                                 readGeometryColumns();
                                 readAvailableColumns();
                                 tfLayername.setText( cbTable.getSelectedItem().toString() );
                             }
                         } );
                     }
                     {
                         lbGeom = new JLabel( Messages.getMessage( getLocale(), "$MD11438" ) );
                         pnContent.add( lbGeom, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                        GridBagConstraints.HORIZONTAL,
                                                                        new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         cbGeom = new JComboBox();
                         pnContent.add( cbGeom, new GridBagConstraints( 1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                        GridBagConstraints.HORIZONTAL,
                                                                        new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                     }
                     {
                         lbCRS = new JLabel( Messages.getMessage( getLocale(), "$MD11439" ) );
                         pnContent.add( lbCRS, new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                       GridBagConstraints.HORIZONTAL,
                                                                       new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                     }
                     {
                         cbNativeCRS = new AutoCompleteComboBox( crsList );
                         cbNativeCRS.setEditable( true );
                         cbNativeCRS.setSelectedItem( appCont.getMapModel( null ).getCoordinateSystem().getIdentifier() );
                         pnContent.add( cbNativeCRS, new GridBagConstraints( 1, 4, 2, 1, 0.0, 0.0,
                                                                             GridBagConstraints.CENTER,
                                                                             GridBagConstraints.HORIZONTAL,
                                                                             new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                     }
                     {
                         btExpert = new JButton( Messages.getMessage( getLocale(), "$MD11440" ) );
                         pnContent.add( btExpert,
                                        new GridBagConstraints( 2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                                                GridBagConstraints.NONE, new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                         btExpert.addActionListener( new ActionListener() {
 
                             public void actionPerformed( ActionEvent e ) {
                                 DatabaseExpertDialog ded = null;
                                 ded = new DatabaseExpertDialog( appCont, getDriver(), getConnectionString(),
                                                                 tfUser.getText(),
                                                                 new String( pwPassword.getPassword() ), sql );
                                 sql = ded.getSQL();
                                 geomField = ded.getGeometryColumn();
                                 pkColumn = ded.getPKColumn();
                                 if ( sql != null && geomField != null ) {
                                     cbTable.setEnabled( false );
                                     cbGeom.setEnabled( false );
                                     cbAllowTransactions.setSelected( false );
                                     cbAllowTransactions.setEnabled( false );
                                     cbPKColumn.setEnabled( false );
                                 } else {
                                     cbTable.setEnabled( true );
                                     cbGeom.setEnabled( true );
                                     cbAllowTransactions.setEnabled( true );
                                     cbPKColumn.setEnabled( true );
                                 }
                             }
                         } );
                     }
                     {
                         pnScale = new JPanel();
                         FlowLayout pnScaleLayout = new FlowLayout();
                         pnScaleLayout.setAlignment( FlowLayout.LEFT );
                         pnContent.add( pnScale, new GridBagConstraints( 0, 5, 3, 1, 0.0, 0.0,
                                                                         GridBagConstraints.CENTER,
                                                                         GridBagConstraints.BOTH,
                                                                         new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                         pnScale.setLayout( pnScaleLayout );
                         {
                             lbMinScale = new JLabel( Messages.getMessage( getLocale(), "$MD11441" ) );
                             pnScale.add( lbMinScale );
                         }
                         {
                             spMinScale = new JSpinner( new SpinnerNumberModel( 0, 0, 999999999d, 10 ) );
                             pnScale.add( spMinScale );
                             spMinScale.setPreferredSize( new Dimension( 125, 22 ) );
                         }
                         {
                             pnSpace = new JPanel();
                             pnScale.add( pnSpace );
                             pnSpace.setPreferredSize( new Dimension( 36, 21 ) );
                         }
                         {
                             lbMaxScale = new JLabel( Messages.getMessage( getLocale(), "$MD11442" ) );
                             pnScale.add( lbMaxScale );
                         }
                         {
                             spMaxScale = new JSpinner( new SpinnerNumberModel( 999999999d, 0, 999999999d, 10 ) );
                             pnScale.add( spMaxScale );
                             spMaxScale.setPreferredSize( new Dimension( 125, 22 ) );
                         }
                     }
                     {
                         cbAllowTransactions = new JCheckBox();
                         pnContent.add( cbAllowTransactions, new GridBagConstraints( 0, 7, 1, 1, 0.0, 0.0,
                                                                                     GridBagConstraints.WEST,
                                                                                     GridBagConstraints.HORIZONTAL,
                                                                                     new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         cbAllowTransactions.setText( Messages.getMessage( getLocale(), "$MD11443" ) );
                     }
                     {
                         cbLazyLoading = new JCheckBox();
                         pnContent.add( cbLazyLoading, new GridBagConstraints( 0, 6, 1, 1, 0.0, 0.0,
                                                                               GridBagConstraints.CENTER,
                                                                               GridBagConstraints.HORIZONTAL,
                                                                               new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         lbPKColumn = new JLabel( Messages.getMessage( getLocale(), "$MD11461" ) );
                         pnContent.add( lbPKColumn, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0,
                                                                            GridBagConstraints.CENTER,
                                                                            GridBagConstraints.HORIZONTAL,
                                                                            new Insets( 0, 9, 0, 0 ), 0, 0 ) );
                         cbPKColumn = new JComboBox();
                         pnContent.add( cbPKColumn, new GridBagConstraints( 1, 2, 2, 1, 0.0, 0.0,
                                                                            GridBagConstraints.CENTER,
                                                                            GridBagConstraints.HORIZONTAL,
                                                                            new Insets( 0, 0, 0, 9 ), 0, 0 ) );
                         cbLazyLoading.setText( Messages.getMessage( getLocale(), "$MD11444" ) );
                         cbLazyLoading.setEnabled( true );
                     }
                 }
             }
             readConnectionInfoFromCache();
             this.setSize( 735, 680 );
         } catch ( Exception e ) {
             e.printStackTrace();
         }
     }
 
     private void readAvailableColumns() {
         DBConnectionPool pool = DBConnectionPool.getInstance();
         String driver = getDriver();
         String database = getConnectionString();
         Connection conn = null;
         Statement stmt = null;
         ResultSet rs = null;
         try {
             conn = pool.acquireConnection( driver, database, tfUser.getText(), new String( pwPassword.getPassword() ) );
             stmt = conn.createStatement();
             rs = stmt.executeQuery( "select * from " + cbTable.getSelectedItem().toString() + " WHERE 1 = 2" );
             List<String> list = new ArrayList<String>();
             ResultSetMetaData rsmd = rs.getMetaData();
             for ( int i = 0; i < rsmd.getColumnCount(); i++ ) {
                 list.add( rsmd.getColumnName( i + 1 ) );
             }
             cbPKColumn.setModel( new DefaultComboBoxModel( list.toArray() ) );
         } catch ( Exception e ) {
             DialogFactory.openErrorDialog( appCont.getViewPlatform(), this, Messages.getMessage( getLocale(),
                                                                                                  "$MD11462" ),
                                            Messages.getMessage( getLocale(), "$MD11463",
                                                                 cbTable.getSelectedItem().toString() ), e );
         } finally {
             try {
                 if ( rs != null ) {
                     rs.close();
                 }
                 if ( stmt != null ) {
                     stmt.close();
                 }
             } catch ( Exception e ) {
                 LOG.logWarning( "can not close Statement/ResultSet" );
             }
             try {
                 pool.releaseConnection( conn, driver, database, tfUser.getText(), new String( pwPassword.getPassword() ) );
             } catch ( DBPoolException e ) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * 
      */
     private void readGeometryColumns() {
         DBConnectionPool pool = DBConnectionPool.getInstance();
         String driver = getDriver();
         String s = cbDriver.getSelectedItem().toString().toLowerCase();
         String database = getConnectionString();
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             List<String> list = new ArrayList<String>();
             conn = pool.acquireConnection( driver, database, tfUser.getText(), new String( pwPassword.getPassword() ) );
             if ( s.indexOf( "postgis" ) > -1 ) {
                 String[] tmp = StringTools.toArray( cbTable.getSelectedItem().toString(), ".", false );
                 // get names geometry columns for current schema.table
                 String sql = "SELECT pg_attribute.attname  FROM pg_attribute "
                              + "JOIN pg_class ON pg_class.oid = pg_attribute.attrelid "
                              + "JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace "
                              + "JOIN pg_type ON pg_attribute.atttypid = pg_type.oid "
                              + "WHERE pg_attribute.attstattarget <> 0 and pg_type.typname ='geometry' and "
                              + "lower(pg_namespace.nspname) = lower(?) and lower(pg_class.relname) = lower(?)";
                 stmt = conn.prepareStatement( sql );
                 stmt.setString( 1, tmp[0] );
                 stmt.setString( 2, tmp[1] );
                 rs = stmt.executeQuery();
                 List<String> list_ = new ArrayList<String>();
                 while ( rs.next() ) {
                     list_.add( rs.getString( 1 ) );
                 }
                 rs.close();
                 stmt.close();
                 // get SRID for each geometry column
                 for ( String col : list_ ) {
                     sql = "select distinct ST_SRID(" + col + ") from " + cbTable.getSelectedItem();
                     stmt = conn.prepareStatement( sql );
                     rs = stmt.executeQuery();
                     rs.next();
                     list.add( col + " (" + rs.getObject( 1 ) + ')' );
                     rs.close();
                     stmt.close();
                 }
             } else if ( s.indexOf( "oracle" ) > -1 ) {
                 stmt = conn.prepareStatement( "select column_name, srid from USER_SDO_GEOM_METADATA" );
                 rs = stmt.executeQuery();
                 while ( rs.next() ) {
                     String geomCol = rs.getString( 1 );
                     String srid = rs.getObject( 2 ).toString();
                     list.add( geomCol + " (" + srid + ')' );
                 }
             } else if ( s.indexOf( "mysql" ) > -1 ) {
                 // TODO
                 LOG.logWarning( "MY SQL is not Supported yet" );
             } else if ( s.indexOf( "sqlserver" ) > -1 ) {
                 stmt = conn.prepareStatement( "select column_name from information_schema.columns where TABLE_NAME = ? and DATA_TYPE = 'geometry'" );
                 stmt.setString( 1, cbTable.getSelectedItem().toString() );
                 rs = stmt.executeQuery();
                 while ( rs.next() ) {
                     String geomCol = rs.getString( 1 );
                    list.add( geomCol + " (0)");
                 }
             }
             cbGeom.setModel( new DefaultComboBoxModel( list.toArray() ) );
         } catch ( Exception e ) {
             DialogFactory.openErrorDialog( appCont.getViewPlatform(), this, Messages.getMessage( getLocale(),
                                                                                                  "$MD11449" ),
                                            Messages.getMessage( getLocale(), "$MD11450",
                                                                 cbTable.getSelectedItem().toString() ), e );
         } finally {
             try {
                 if ( rs != null ) {
                     rs.close();
                 }
                 if ( stmt != null ) {
                     stmt.close();
                 }
             } catch ( Exception e ) {
                 LOG.logWarning( "can not close Statement/ResultSet" );
             }
             try {
                 pool.releaseConnection( conn, driver, database, tfUser.getText(), new String( pwPassword.getPassword() ) );
             } catch ( DBPoolException e ) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * 
      */
     private void changeDatabaseVendor() {
         String s = cbDriver.getSelectedItem().toString().toLowerCase();
         if ( s.indexOf( "postgis" ) > -1 ) {
             lbDatabase.setText( Messages.getMessage( getLocale(), "$MD11435" ) );
             spPort.setValue( 5432 );
         } else if ( s.indexOf( "oracle" ) > -1 ) {
             lbDatabase.setText( "SID" );
             spPort.setValue( 1521 );
         } else if ( s.indexOf( "mysql" ) > -1 ) {
             lbDatabase.setText( Messages.getMessage( getLocale(), "$MD11435" ) );
             spPort.setValue( 3306 );
         } else if ( s.indexOf( "sqlserver" ) > -1 ) {
             lbDatabase.setText( Messages.getMessage( getLocale(), "$MD11435" ) );
             spPort.setValue( 1433 );
         }
         SwingUtilities.updateComponentTreeUI( lbDatabase );
         readConnectionInfoFromCache();
     }
 
     /**
      * 
      */
     private void createLayer() {
         try {
             String driver = getDriver();
             String database = getConnectionString();
             String user = tfUser.getText();
             String password = new String( pwPassword.getPassword() );
             if ( cbTable.isEnabled() && cbTable.getSelectedIndex() == 0 ) {
                 DialogFactory.openWarningDialog( appCont.getViewPlatform(), this,
                                                  Messages.getMessage( getLocale(), "$MD11451" ),
                                                  Messages.getMessage( getLocale(), "$MD11452" ) );
                 return;
             }
             String table = cbTable.getSelectedItem().toString();
             if ( cbGeom.isEnabled() && cbGeom.getSelectedItem() == null ) {
                 DialogFactory.openWarningDialog( appCont.getViewPlatform(), this,
                                                  Messages.getMessage( getLocale(), "$MD11453" ),
                                                  Messages.getMessage( getLocale(), "$MD11452" ) );
                 return;
             }
             if ( cbGeom.isEnabled() ) {
                 // cbGeom is not enable if expert mode has been used
                 geomField = cbGeom.getSelectedItem().toString();
             }
             String[] tmp = StringTools.toArray( geomField, " ", false );
             geomField = tmp[0];
             String srid = tmp[1].trim().substring( 1, tmp[1].trim().length() - 1 );
             if ( cbNativeCRS.getSelectedIndex() == 0 ) {
                 DialogFactory.openWarningDialog( appCont.getViewPlatform(), this,
                                                  Messages.getMessage( getLocale(), "$MD11454" ),
                                                  Messages.getMessage( getLocale(), "$MD11452" ) );
                 return;
             }
             String nativeCRS = cbNativeCRS.getSelectedItem().toString();
            if ( sql == null ) {
                 // sql is not null if expert mode has been used
                 sql = "select * from " + table;
             }
             double minScale = ( (Number) spMinScale.getValue() ).doubleValue();
             double maxScale = ( (Number) spMaxScale.getValue() ).doubleValue();
             boolean supportTransactions = cbAllowTransactions.isSelected();
             boolean lazyLoading = cbLazyLoading.isSelected();
             String layerName = tfLayername.getText();
             MapModel mapModel = appCont.getMapModel( null );
             if ( pkColumn == null ) {
                 pkColumn = cbPKColumn.getSelectedItem().toString();
             }
             writeConnectionInfoToCache();
             Command command = new AddDatabaseLayerCommand( mapModel, driver, database, user, password, geomField,
                                                            pkColumn, minScale, maxScale, supportTransactions,
                                                            lazyLoading, nativeCRS, sql, srid, layerName,
                                                            cbSavePassword.isSelected() );
             getOwner().setCursor( CursorRegistry.WAIT_CURSOR );
             setCursor( CursorRegistry.WAIT_CURSOR );
             // final ProcessMonitor pm = ProcessMonitorFactory.createDialogProcessMonitor(
             // appCont.getViewPlatform(),
             // Messages.getMessage(
             // getLocale(),
             // "$MD11459" ),
             // Messages.getMessage(
             // getLocale(),
             // "$MD11459",
             // sql ), 0,
             // -1, command );
             // command.setProcessMonitor( pm );
             // command.addListener( new CommandProcessedListener() {
 
             // public void commandProcessed( CommandProcessedEvent event ) {
             // try {
             // pm.cancel();
             // } catch ( Exception e ) {
             // e.printStackTrace();
             // }
             // }
 
             // } );
             // appCont.getCommandProcessor().executeASychronously( command );
             appCont.getCommandProcessor().executeSychronously( command, true );
             setCursor( CursorRegistry.DEFAULT_CURSOR );
             getOwner().setCursor( CursorRegistry.DEFAULT_CURSOR );
             dispose();
         } catch ( Exception e ) {
             DialogFactory.openErrorDialog( appCont.getViewPlatform(), this,
                                            Messages.getMessage( getLocale(), "$MD11452" ),
                                            Messages.getMessage( getLocale(), "$MD11455" ), e );
         }
 
     }
 
     private void writeConnectionInfoToCache() {
         if ( cbSavePassword.isSelected() ) {
             Preferences prefs = Preferences.userNodeForPackage( AddDatabaseLayerDialog.class );
             String s = cbDriver.getSelectedItem().toString().toLowerCase();
             prefs.put( s + "URL", tfURL.getText() );
             prefs.put( s + "USER", tfUser.getText() );
             prefs.put( s + "DATABASE", tfDatabase.getText() );
             prefs.put( s + "PASSWORD", new String( pwPassword.getPassword() ) );
             prefs.putInt( s + "PORT", ( (Number) spPort.getValue() ).intValue() );
         }
     }
 
     private void readConnectionInfoFromCache() {
         Preferences prefs = Preferences.userNodeForPackage( AddDatabaseLayerDialog.class );
         String s = cbDriver.getSelectedItem().toString().toLowerCase();
         tfURL.setText( prefs.get( s + "URL", "localhost" ) );
         tfUser.setText( prefs.get( s + "USER", "" ) );
         tfDatabase.setText( prefs.get( s + "DATABASE", "" ) );
         pwPassword.setText( prefs.get( s + "PASSWORD", "" ) );
 
         if ( s.indexOf( "postgis" ) > -1 ) {
             spPort.setValue( prefs.getInt( s + "PORT", 5432 ) );
         } else if ( s.indexOf( "oracle" ) > -1 ) {
             spPort.setValue( prefs.getInt( s + "PORT", 1521 ) );
         } else if ( s.indexOf( "mysql" ) > -1 ) {
             spPort.setValue( prefs.getInt( s + "PORT", 3306 ) );
         } else if ( s.indexOf( "sqlserver" ) > -1 ) {
             spPort.setValue( prefs.getInt( s + "PORT", 1433 ) );
         }
     }
 
     private String getConnectionString() {
         return DatabaseConnectionManager.getConnectionUrl( cbDriver.getSelectedItem().toString(), tfURL.getText(),
                                                            ( (Number) spPort.getValue() ).intValue(),
                                                            tfDatabase.getText() );
     }
 
     private String getDriver() {
         return DatabaseConnectionManager.getDriver( cbDriver.getSelectedItem().toString() );
     }
 
     private String getUser() {
         return tfUser.getText();
     }
 
     private String getPassword() {
         return new String( pwPassword.getPassword() );
     }
 
     private void testConnection() {
         try {
             DatabaseConnectionManager.testConnection( getDriver(), getConnectionString(), getUser(), getPassword() );
             DialogFactory.openInformationDialog( appCont.getViewPlatform(), this,
                                                  Messages.getMessage( getLocale(), "$MD11456" ),
                                                  Messages.getMessage( getLocale(), "$MD11452" ) );
         } catch ( Exception e ) {
             DialogFactory.openErrorDialog( appCont.getViewPlatform(), this,
                                            Messages.getMessage( getLocale(), "$MD11452" ),
                                            Messages.getMessage( getLocale(), "$MD11457", getConnectionString() ), e );
         }
     }
 
     private JPanel getPnLayer() {
         if ( pnLayer == null ) {
             pnLayer = new JPanel();
             GridBagLayout pnLayerLayout = new GridBagLayout();
             pnLayerLayout.rowWeights = new double[] { 0.1 };
             pnLayerLayout.rowHeights = new int[] { 7 };
             pnLayerLayout.columnWeights = new double[] { 0.0, 0.1 };
             pnLayerLayout.columnWidths = new int[] { 204, 7 };
             pnLayer.setLayout( pnLayerLayout );
             pnLayer.setBorder( BorderFactory.createTitledBorder( null, Messages.getMessage( getLocale(), "$MD11447" ),
                                                                  TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION ) );
             pnLayer.add( getLbLayername(),
                          new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                  GridBagConstraints.HORIZONTAL, new Insets( 0, 9, 0, 0 ), 0, 0 ) );
             pnLayer.add( getTfLayername(),
                          new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                  GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 9 ), 0, 0 ) );
         }
         return pnLayer;
     }
 
     private JLabel getLbLayername() {
         if ( lbLayername == null ) {
             lbLayername = new JLabel( Messages.getMessage( getLocale(), "$MD11446" ) );
         }
         return lbLayername;
     }
 
     private JTextField getTfLayername() {
         if ( tfLayername == null ) {
             tfLayername = new JTextField();
         }
         return tfLayername;
     }
 
     private JButton getBtConnect() {
         if ( btConnect == null ) {
             btConnect = new JButton( Messages.getMessage( getLocale(), "$MD11445" ) );
             btConnect.addActionListener( new ActionListener() {
 
                 public void actionPerformed( ActionEvent e ) {
                     connectToDatabase();
                 }
             } );
         }
         return btConnect;
     }
 
     /**
      * 
      */
     private void connectToDatabase() {
         Connection conn = null;
         try {
             conn = DatabaseConnectionManager.aquireConnection( getDriver(), getConnectionString(), getUser(),
                                                                getPassword() );
             readAvailableTables( conn );
             pnContent.setVisible( true );
         } catch ( Exception e ) {
             DialogFactory.openErrorDialog( appCont.getViewPlatform(), this,
                                            Messages.getMessage( getLocale(), "$MD11452" ),
                                            Messages.getMessage( getLocale(), "$MD11458", e.getMessage() ), e );
         } finally {
             if ( conn != null ) {
                 try {
                     DatabaseConnectionManager.releaseConnection( conn, getDriver(), getConnectionString(), getUser(),
                                                                  getPassword() );
                 } catch ( DBPoolException e ) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     /**
      * @param conn
      */
     private void readAvailableTables( Connection conn )
                             throws Exception {
         String s = cbDriver.getSelectedItem().toString().toLowerCase();
         String sql = null;
         List<String> tables = new ArrayList<String>( 500 );
         tables.add( "--- select a table ---" );
         ResultSet rs = null;
         Statement stmt = conn.createStatement();
         try {
             if ( s.indexOf( "postgis" ) > -1 ) {
                 sql = "SELECT pg_class.relname AS relname, pg_namespace.nspname AS nspname FROM pg_attribute"
                       + "   JOIN pg_class ON pg_class.oid = pg_attribute.attrelid"
                       + "   JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace"
                       + "   JOIN pg_type ON pg_attribute.atttypid = pg_type.oid"
                       + "  WHERE pg_attribute.attstattarget <> 0 and pg_type.typname ='geometry' order by 1,2";
                 System.out.println( "..." + sql );
                 rs = stmt.executeQuery( sql );
                 while ( rs.next() ) {
                     String table = rs.getString( 1 );
                     String schema = rs.getString( 2 );
                     tables.add( schema + '.' + table );
                 }
             } else if ( s.indexOf( "oracle" ) > -1 ) {
                 sql = "select table_name from USER_SDO_GEOM_METADATA";
                 rs = stmt.executeQuery( sql );
                 while ( rs.next() ) {
                     tables.add( rs.getString( 1 ) );
                 }
             } else if ( s.indexOf( "mysql" ) > -1 ) {
                 // TODO
                 LOG.logWarning( "MY SQL is not Supported yet" );
             } else if ( s.indexOf( "sqlserver" ) > -1 ) {
                 sql = "select TABLE_NAME from information_schema.tables where TABLE_TYPE = 'BASE TABLE'";
                 rs = stmt.executeQuery( sql );
                 while ( rs.next() ) {
                     tables.add( rs.getString( 1 ) );
                 }
             }
         } catch ( Exception e ) {
             throw e;
         } finally {
             rs.close();
             stmt.close();
         }
         Collections.sort( tables );
         cbTable.setModel( new DefaultComboBoxModel( tables.toArray() ) );
     }
 
 }
