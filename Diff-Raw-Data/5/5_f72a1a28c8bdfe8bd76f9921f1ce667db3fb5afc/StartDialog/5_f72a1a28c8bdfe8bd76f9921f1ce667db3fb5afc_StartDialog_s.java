 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.gui.startdialog;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 
 import es.eucm.eadventure.common.auxiliar.ReleaseFolders;
 import es.eucm.eadventure.common.data.adventure.DescriptorData;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.common.loader.Loader;
 import es.eucm.eadventure.common.loader.incidences.Incidence;
 import es.eucm.eadventure.editor.auxiliar.filefilters.EADAndFolderFileFilter;
 import es.eucm.eadventure.editor.auxiliar.filefilters.JARFileFilter;
 import es.eucm.eadventure.editor.auxiliar.filefilters.ZIPFileFilter;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.config.ConfigData;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 
 public class StartDialog extends JFileChooser {
 
     /**
      * 
      */
     private static final long serialVersionUID = -544319646379939561L;
 
     private int option;
     
     private JDialog dialog;
 
     public static final int NO_CUSTOM_OPTION = -2;
 
     public static final int NEW_FILE_OPTION = 4;
 
     public static final int OPEN_FILE_OPTION = JFileChooser.APPROVE_OPTION;
 
     public static final int RECENT_FILE_OPTION = 5;
     
     public static final int APROVE_SELECTION = 6;
 
     public static final int NEW_TAB = 0;
 
     public static final int OPEN_TAB = 1;
 
     private File recentFile;
 
     private int fileType;
 
     private JList list;
 
     private JEditorPane helpText;
 
     private JTable todayTable;
 
     private JTable yesterdayTable;
 
     private JTable beforeTable;
 
     private DescriptorDataPanel descriptorDataPanel;
 
     private JButton buttonCancel;
 
     private JButton buttonNew;
 
     private JButton openRecentsButton;
 
     private JTabbedPane tab;
 
     /**
      * @return the fileType
      */
     public int getFileType( ) {
 
         return fileType;
     }
 
     /**
      * @param fileType
      *            the fileType to set
      */
     public void setFileType( int fileType ) {
 
         this.fileType = fileType;
     }
 
     public StartDialog( int tab ) {
 
         this( );
         this.tab.setSelectedIndex( tab );
     }
 
     public StartDialog( ) {
 
         super( );
         this.setFileFilter( new ZIPFileFilter() );
         this.setFileFilter( new JARFileFilter() );
         this.setFileFilter( new EADAndFolderFileFilter( this ) );
         this.setCurrentDirectory( ReleaseFolders.projectsFolder( ) );
         this.setMultiSelectionEnabled( false );
         this.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
 
         
         option = NO_CUSTOM_OPTION;
         recentFile = null;
         fileType = -2;
         //Load the logo
         Icon logo = new ImageIcon( "img/logo-editor.png" );
         JLabel label = new JLabel( logo );
 
         JPanel centralPanel = new JPanel( );
         centralPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.weighty = 0;
         c.weightx = 0;
         c.fill = GridBagConstraints.BOTH;
         centralPanel.add( label, c );
         tab = new JTabbedPane( );
 
         tab.insertTab( TC.get( "GeneralText.New" ), null, createNewFilePanel( ), "", 0 );
         tab.insertTab( TC.get( "GeneralText.Open" ), null, createOpenFilePanel( ), "", 1 );
         tab.insertTab( TC.get( "StartDialog.Recent" ), null, createRecentFilesPanel( ), "", 2 );
         tab.addChangeListener( new ChangeListener( ) {
 
             public void stateChanged( ChangeEvent e ) {
 
                 if( ( (JTabbedPane) e.getSource( ) ).getSelectedIndex( ) == 1 ) {
                     option = NO_CUSTOM_OPTION;
                 }
 
             }
 
         } );
         //tab.setMaximumSize( new Dimension( 500, 200 ) );
         setLayout( new BorderLayout( ) );
         c.gridy = 1;
         c.weighty = 1;
         c.weightx = 1;
         centralPanel.add( tab, c );
         add( centralPanel );
         setSize( new Dimension( 690, 600 ) );
         setMinimumSize( new Dimension( 690, 600 ) );
         setPreferredSize( new Dimension( 690, 600 ) );
 
        
 
         
         
        //addTrayIcon();
         
         
     }
 
       /*  private void addTrayIcon(){
             if (SystemTray.isSupported()) {
                 PopupMenu menu = new PopupMenu();
                 MenuItem quitItem = new MenuItem("Quit");
                 quitItem.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         System.exit(0);
                     }});
                 menu.add(quitItem);
                 Image icon = new ImageIcon( "img/Icono-Editor-32x32.png" ).getImage( );
                 TrayIcon trayIcon = new TrayIcon(icon, TC.get( "StartDialog.Title" ),menu );
                 try {
                      SystemTray tray = SystemTray.getSystemTray( );
                     tray.add( trayIcon );
                 } catch (AWTException e) {
                     // it isn't possible to add a system tray
                 }
 
                
         }
         }*/
         
         
     private JPanel createNewFilePanel( ) {
 
         // Create the container
         JPanel panelNew = new JPanel( );
         panelNew.setLayout( new GridBagLayout( ) );
         GridBagConstraints c1 = new GridBagConstraints( );
 
         // Create the help Panel, which will show help text to guide the user
         JScrollPane helpPanel = new JScrollPane( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
         helpText = new JEditorPane( );
         //add(new JScrollPane(helpText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
         updateHelpText( );
         helpText.setBorder( BorderFactory.createEtchedBorder( ) );
         helpPanel.setViewportView( helpText );
         helpPanel.setBackground( this.getBackground( ) );
         helpPanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "GeneralText.Information" ) ) );
         helpPanel.setMaximumSize( new Dimension( 320, 200 ) );
         helpPanel.setMinimumSize( new Dimension( 320, 200 ) );
 
         // Create the selection list with the different types of <e-adventure> files a user can edit
         JScrollPane documentTypes = new JScrollPane( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
         // Icon new adventure - MODE player visible (Monkey Island games)
         JPanel newAdventure1 = new JPanel( );
         newAdventure1.setLayout( new BorderLayout( ) );
         newAdventure1.setPreferredSize( new Dimension( 67, 67 ) );
         newAdventure1.add( new JLabel( new ImageIcon( "img/newAdventureNormalMode65.png" ) ), BorderLayout.CENTER );
 
         // Icon new adventure - MODE player transparent (Myst games)
         JPanel newAdventure2 = new JPanel( );
         newAdventure2.setLayout( new BorderLayout( ) );
         newAdventure2.setPreferredSize( new Dimension( 67, 67 ) );
         newAdventure2.add( new JLabel( new ImageIcon( "img/newAdventureTransparentMode65.png" ) ), BorderLayout.CENTER );
 
         // Icon assessment file
         /*JPanel newAssessment = new JPanel( );
         newAssessment.setLayout( new BorderLayout( ) );
         newAssessment.setPreferredSize( new Dimension( 67, 67 ) );
         newAssessment.add( new JLabel( new ImageIcon( "img/newAssessment65.png" ) ), BorderLayout.CENTER );
         */
         // Icon adaptation file
         /*JPanel newAdaptation = new JPanel( );
         newAdaptation.setLayout( new BorderLayout( ) );
         newAdaptation.setPreferredSize( new Dimension( 67, 67 ) );
         newAdaptation.add( new JLabel( new ImageIcon( "img/newAdaptation65.png" ) ), BorderLayout.CENTER );
          */
 
         //The list
         list = new JList( );
         list.setVisibleRowCount( -20 );
         list.setBorder( BorderFactory.createEtchedBorder( ) );
         //list.setListData( new JPanel[] { newAdventure1, newAdventure2, newAssessment, newAdaptation } );
         list.setListData( new JPanel[] { newAdventure1, newAdventure2 } );
         list.setSelectedIndex( 0 );
         list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
 
         list.addListSelectionListener( new ListSelectionListener( ) {
 
             public void valueChanged( ListSelectionEvent e ) {
 
                 if( list.getSelectedIndex( ) == 0 ) {
                     fileType = Controller.FILE_ADVENTURE_3RDPERSON_PLAYER;
                     buttonNew.setEnabled( true );
                 }
                 else if( list.getSelectedIndex( ) == 1 ) {
                     fileType = Controller.FILE_ADVENTURE_1STPERSON_PLAYER;
                     buttonNew.setEnabled( true );
                 }
                 else if( list.getSelectedIndex( ) == 2 ) {
                     fileType = Controller.FILE_ASSESSMENT;
                     buttonNew.setEnabled( false );
                 }
                 else if( list.getSelectedIndex( ) == 3 ) {
                     fileType = Controller.FILE_ADAPTATION;
                     buttonNew.setEnabled( false );
                 }
 
                 updateHelpText( );
             }
 
         } );
         list.setCellRenderer( new CellRenderer( ) );
         list.setMinimumSize( new Dimension( 150, 200 ) );
         list.setPreferredSize( new Dimension( 150, 200 ) );
         documentTypes.setViewportView( list );
         TitledBorder border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "StartDialog.DocumentTypes" ) );
         border.setTitlePosition( TitledBorder.CENTER );
         // documentTypes.setBorder( border );
         documentTypes.setBackground( list.getBackground( ) );
         documentTypes.setBorder( border );
         documentTypes.setBackground( this.getBackground( ) );
         documentTypes.setMaximumSize( new Dimension( 150, 200 ) );
         documentTypes.setMinimumSize( new Dimension( 150, 200 ) );
         list.setBorder( BorderFactory.createEtchedBorder( ) );
         list.setLayoutOrientation( JList.HORIZONTAL_WRAP );
 
         //The panel with the buttons
         buttonNew = new JButton( TC.get( "StartDialog.CreateNew" ) );
         //buttonNew.setPreferredSize( new Dimension( 80, 30 ) );
         buttonNew.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 option = NEW_FILE_OPTION;
                 if( fileType == -2 ) {
                     fileType = Controller.FILE_ADVENTURE_3RDPERSON_PLAYER;
                 }
                 approveSelection( );
             }
 
         } );
         buttonCancel = new JButton( TC.get( "GeneralText.Cancel" ) );
         //buttonCancel.setPreferredSize( new Dimension( 80, 30 ) );
         buttonCancel.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 option = NO_CUSTOM_OPTION;
                 cancelSelection( );
             }
 
         } );
         JPanel southPanel = new JPanel( );
         southPanel.setLayout( new BoxLayout( southPanel, BoxLayout.LINE_AXIS ) );
         southPanel.add( buttonNew );
         southPanel.add( Box.createHorizontalStrut( 2 ) );
         southPanel.add( buttonCancel );
 
         // Finally, add all the elements
         c1.gridwidth = 1;
         c1.weightx = 0.3;
         c1.weighty = 1;
         c1.fill = GridBagConstraints.BOTH;
         panelNew.add( documentTypes, c1 );
 
         c1.gridx = 1;
         c1.weightx = 0.7;
         c1.weighty = 0;
         c1.fill = GridBagConstraints.BOTH;
         panelNew.add( helpPanel, c1 );
 
         c1.gridy = 1;
         c1.gridwidth = 1;
         c1.fill = GridBagConstraints.NONE;
         c1.anchor = GridBagConstraints.CENTER;
         c1.gridx = 1;
         panelNew.add( southPanel, c1 );
         return panelNew;
     }
 
     private JPanel createOpenFilePanel( ) {
 
         JPanel panelOpen = new JPanel( );
 
         // Transfer the elements in the JFileChooser to the open file panel 
 
         LayoutManager layout = getLayout( );
         if( layout instanceof BorderLayout ) {
             panelOpen.setLayout( new BorderLayout( ) );
             BorderLayout currentLayout = (BorderLayout) getLayout( );
 
             for( Component comp : getComponents( ) ) {
                 panelOpen.add( comp, currentLayout.getConstraints( comp ) );
             }
         }
         else if( layout instanceof BoxLayout ) {
             BoxLayout currentLayout = (BoxLayout) getLayout( );
             panelOpen.setLayout( new BoxLayout( panelOpen, currentLayout.getAxis( ) ) );
 
             for( Component comp : getComponents( ) ) {
                 panelOpen.add( comp );
             }
         }
 
         return panelOpen;
     }
 
     private JPanel createRecentFilesPanel( ) {
 
         //----------------- CREATE ALL THE TABLES --------------------------//
         todayTable = new JTable( );
         yesterdayTable = new JTable( );
         beforeTable = new JTable( );
 
         //----------------- TODAY OPENED ---------------------------------//
         JScrollPane todayPanel = new JScrollPane( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
         TitledBorder todayBorder = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "StartDialog.Recent.OpenedToday" ) );
         todayBorder.setTitleJustification( TitledBorder.CENTER );
 
         JTable[] todayOtherTables = new JTable[] { yesterdayTable, beforeTable };
         RecentFilesTableModel todayModel = new RecentFilesTableModel( 1, 0, todayTable, todayOtherTables );
         todayTable.setModel( todayModel );
         todayTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         todayTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 32 );
         todayTable.getColumnModel( ).getColumn( 0 ).setMaxWidth( 32 );
         todayTable.getColumnModel( ).getColumn( 0 ).setMinWidth( 32 );
         todayTable.getColumnModel( ).getColumn( 0 ).setCellRenderer( new TableRenderer( ) );
         todayTable.setRowHeight( 32 );
         todayTable.addMouseListener( todayModel );
         todayPanel.setViewportView( todayTable );
         todayPanel.setBorder( todayBorder );
 
         //----------------- YESTERDAY OPENED ---------------------------------//
         JScrollPane yesterdayPanel = new JScrollPane( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
         TitledBorder yesterdayBorder = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "StartDialog.Recent.OpenedYesterday" ) );
         yesterdayBorder.setTitleJustification( TitledBorder.CENTER );
         JTable[] yesterdayOtherTables = new JTable[] { todayTable, beforeTable };
         RecentFilesTableModel yesterdayModel = new RecentFilesTableModel( 2, 1, yesterdayTable, yesterdayOtherTables );
         yesterdayTable.setModel( yesterdayModel );
         yesterdayTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 32 );
         yesterdayTable.getColumnModel( ).getColumn( 0 ).setMaxWidth( 32 );
         yesterdayTable.getColumnModel( ).getColumn( 0 ).setMinWidth( 32 );
         yesterdayTable.setRowHeight( 32 );
         yesterdayTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         yesterdayTable.addMouseListener( yesterdayModel );
         yesterdayTable.getColumnModel( ).getColumn( 0 ).setCellRenderer( new TableRenderer( ) );
         yesterdayPanel.setViewportView( yesterdayTable );
         yesterdayPanel.setBorder( yesterdayBorder );
 
         //----------------- OLDER FILES OPENED ---------------------------------//
         JScrollPane beforePanel = new JScrollPane( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
         TitledBorder beforeBorder = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "StartDialog.Recent.OpenedOlder" ) );
         beforeBorder.setTitleJustification( TitledBorder.CENTER );
         beforePanel.setBorder( beforeBorder );
         JTable[] beforeOtherTables = new JTable[] { yesterdayTable, todayTable };
         RecentFilesTableModel beforeModel = new RecentFilesTableModel( 2, beforeTable, beforeOtherTables );
         beforeTable.setModel( beforeModel );
         beforeTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         beforeTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 32 );
         beforeTable.getColumnModel( ).getColumn( 0 ).setMaxWidth( 32 );
         beforeTable.getColumnModel( ).getColumn( 0 ).setMinWidth( 32 );
         beforeTable.getColumnModel( ).getColumn( 0 ).setCellRenderer( new TableRenderer( ) );
         beforeTable.setRowHeight( 32 );
         beforeTable.addMouseListener( beforeModel );
         beforePanel.setViewportView( beforeTable );
 
         //-------------------------------- BUTTONS PANEL------------------------//
         JPanel buttonsPanel = new JPanel( );
         buttonsPanel.setLayout( new BoxLayout( buttonsPanel, BoxLayout.LINE_AXIS ) );
         openRecentsButton = new JButton( TC.get( "GeneralText.Open" ) );
         openRecentsButton.setEnabled( false );
         openRecentsButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 option = StartDialog.RECENT_FILE_OPTION;
                 approveSelection( );
             }
 
         } );
         JButton cancelButton = new JButton( TC.get( "GeneralText.Cancel" ) );
         cancelButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 option = StartDialog.NO_CUSTOM_OPTION;
                 cancelSelection( );
             }
 
         } );
         buttonsPanel.add( openRecentsButton );
         buttonsPanel.add( Box.createHorizontalStrut( 1 ) );
         buttonsPanel.add( cancelButton );
         //-----------------MAIN PANEL-----------------------------------------//
 
         JPanel recentFiles = new JPanel( );
         recentFiles.setLayout( new BoxLayout( recentFiles, BoxLayout.PAGE_AXIS ) );
         todayPanel.setPreferredSize( new Dimension( 100, 100 ) );
         yesterdayPanel.setPreferredSize( new Dimension( 100, 100 ) );
         beforePanel.setPreferredSize( new Dimension( 100, 100 ) );
         if( todayTable.getRowCount( ) == 0 ) {
             todayPanel.removeAll( );
             todayPanel.setPreferredSize( new Dimension( 100, 1 ) );
             //todayPanel.setViewportView( new JLabel("No recent files") );
         }
         recentFiles.add( todayPanel );
         if( yesterdayTable.getRowCount( ) == 0 ) {
             yesterdayPanel.removeAll( );
             yesterdayPanel.setPreferredSize( new Dimension( 100, 1 ) );
         }
         recentFiles.add( yesterdayPanel );
         if( beforeTable.getRowCount( ) == 0 ) {
             beforePanel.removeAll( );
             beforePanel.setPreferredSize( new Dimension( 100, 1 ) );
         }
         recentFiles.add( beforePanel );
         recentFiles.add( buttonsPanel );
 
         JPanel mainPanel = new JPanel( );
         mainPanel.setLayout( new BoxLayout( mainPanel, BoxLayout.LINE_AXIS ) );
         recentFiles.setMinimumSize( new Dimension( 400, 300 ) );
         recentFiles.setPreferredSize( new Dimension( 300, 300 ) );
         mainPanel.add( recentFiles );
         //AdventureDataDialog adDialog = new AdventureDataDialog(false,"Título", "Decription description dddddddddddddd", true);
         //Container infoPanel =createDescriptorPanel();//adDialog.getContentPane( );
         descriptorDataPanel = new DescriptorDataPanel( null, null );
         descriptorDataPanel.setMinimumSize( new Dimension( 200, 300 ) );
         descriptorDataPanel.setPreferredSize( new Dimension( 200, 300 ) );
 
         mainPanel.add( descriptorDataPanel );
         return mainPanel;
     }
 
     private void updateHelpText( ) {
 
         String helpPath = null;
         if( fileType == Controller.FILE_ADVENTURE_1STPERSON_PLAYER ) {
             //helpText.setText( TextConstants.getText( "StartDialog.NewAdventure-TransparentMode.Description" ) );
             helpPath = "startDialog/FirstPerson.html";
         }
         else if( fileType == Controller.FILE_ADVENTURE_3RDPERSON_PLAYER ) {
             //helpText.setText( TextConstants.getText( "StartDialog.NewAdventure-VisibleMode.Description" ) );
             helpPath = "startDialog/ThirdPerson.html";
         }
         else if( fileType == Controller.FILE_ASSESSMENT ) {
             //helpText.setText( TextConstants.getText( "StartDialog.NewAssessmentFile.Description" ) );
             helpPath = "";
         }
         else {
             //helpText.setText( TextConstants.getText( "StartDialog.HelpMessage" ) );
             helpPath = "startDialog/Description.html";
         }
 
         String folder = "help/" + Controller.getInstance( ).getLanguage( ) + "/";
         // if there isn't file, load the default file
         File fichero = new File(folder);
         if (!fichero.exists( ))
             folder = "help/" + Controller.getInstance( ).getDefaultLanguage( ) + "/";
         File file = new File( folder + helpPath );
         if( file.exists( ) ) {
             try {
                 helpText.setPage( file.toURI( ).toURL( ) );
                 helpText.setEditable( false );
 
             }
             catch( MalformedURLException e1 ) {
                 writeFileNotFound( folder + helpPath );
             }
             catch( IOException e1 ) {
                 writeFileNotFound( folder + helpPath );
             }
         }
         else {
             writeFileNotFound( folder + helpPath );
         }
 
         helpText.updateUI( );
 
     }
 
     private void writeFileNotFound( String path ) {
 
         helpText.add( new JLabel( TC.get( "HelpDialog.FileNotFound" ) + " " + path ) );
     }
 
     private class RecentFilesTableModel extends AbstractTableModel implements MouseListener {
 
         /**
          * Required
          */
         private static final long serialVersionUID = -4878543735587447453L;
 
         private String[][] info;
 
         private JTable table;
 
         private JTable[] otherTables;
 
         public RecentFilesTableModel( int l, int r, JTable table, JTable[] otherTables ) {
 
             info = ConfigData.getRecentFilesInfo( l, r );
             this.table = table;
             this.otherTables = otherTables;
         }
 
         public RecentFilesTableModel( int r, JTable table, JTable[] otherTables ) {
 
             info = ConfigData.getRecentFilesInfo( r );
             this.table = table;
             this.otherTables = otherTables;
         }
 
         public int getColumnCount( ) {
 
             return 2;
         }
 
         public int getRowCount( ) {
 
             return info.length;
         }
 
         @Override
         public String getColumnName( int columnIndex ) {
 
             return TC.get( "StartDialog.Recent.ColumnHeader" + columnIndex );
         }
 
         public Object getValueAt( int rowIndex, int columnIndex ) {
 
             int index = info.length - rowIndex - 1;
             if( columnIndex == 1 ) {
                 String data = info[index][0];
                 int i = data.lastIndexOf( "\\" );
                 if( i > 0 && i < data.length( ) )
                     return data.substring( i + 1, data.length( ) );
                 else {
                     i = data.lastIndexOf( "/" );
                     if( i > 0 && i < data.length( ) ) {
                         return data.substring( i + 1, data.length( ) );
                     }
                     else
                         return "";
                 }
 
             }
             else if( columnIndex == 0 ) {
                 return info[index][0];
 
             }
             else {
                 return "";
             }
         }
 
         public void mouseClicked( MouseEvent e ) {
 
             if( e.getClickCount( ) == 2 ) {
                 option = StartDialog.RECENT_FILE_OPTION;
                 approveSelection( );
             }
         }
 
         public void mouseEntered( MouseEvent e ) {
 
         }
 
         public void mouseExited( MouseEvent e ) {
 
         }
 
         public void mousePressed( MouseEvent e ) {
 
             int selectedRow = info.length - table.getSelectedRow( ) - 1;
             if( selectedRow >= 0 ) {
                 openRecentsButton.setEnabled( true );
                 recentFile = new File( info[selectedRow][0] );
                 DescriptorData summary = null;
                 List<Incidence> incidences = new ArrayList<Incidence>();
                 try {
                     summary = Loader.loadDescriptorData( AssetsController.getInputStreamCreator( recentFile.getAbsolutePath( ) ), incidences );
                 }
                 catch( Exception ex ) {
                     summary = null;
                 }
                 if (incidences.size( )>0){
                     summary=null;
                 }
                 
                 if (summary!=null){
                     descriptorDataPanel.update( summary, recentFile.getAbsolutePath( ) );
                 } else {
                     descriptorDataPanel.error(recentFile.getAbsolutePath( ));
                     openRecentsButton.setEnabled( false );
                 }
                 
                 for( JTable otherTable : otherTables ) {
                     otherTable.clearSelection( );
                 }
             }
             else
                 openRecentsButton.setEnabled( false );
         }
 
         public void mouseReleased( MouseEvent e ) {
 
         }
 
     }
 
     private class TableRenderer implements TableCellRenderer {
 
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
 
             if( column == 0 ) {
                 JPanel panelIcon = new JPanel( );
                 panelIcon.setLayout( new BorderLayout( ) );
                 DescriptorData d = null;
                 List<Incidence> incidences = new ArrayList<Incidence>();
                 try {
                     String path = (String) value;
                     d = Loader.loadDescriptorData( AssetsController.getInputStreamCreator( path ) , incidences);
                 } catch( Exception ex ) {
                     d=null;
                 }
                 if (incidences.size( )>1){
                     d=null;
                 }
                 
                 if( d!=null && d.getPlayerMode( ) == DescriptorData.MODE_PLAYER_1STPERSON ) {
                     return new JLabel( new ImageIcon( "img/TransparentAdventure32.png" ) );
                 }
                 else if( d!=null && d.getPlayerMode( ) == DescriptorData.MODE_PLAYER_3RDPERSON ) {
                     return new JLabel( new ImageIcon( "img/NormalAdventure32.png" ) );
                 }
                 else {
                     return new JLabel( new ImageIcon( "img/icons/deleteNode.png" ) );
                 }
             }
             return null;
         }
 
     }
 
     private class CellRenderer implements ListCellRenderer {
 
         public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
 
             JPanel docType = (JPanel) value;
 
             if( !isSelected || docType.getComponentCount( ) == 0 ) {
 
                 docType.setBackground( list.getBackground( ) );
                 docType.setForeground( list.getForeground( ) );
             }
             else {
                 docType.setBackground( list.getSelectionBackground( ) );
                 docType.setForeground( list.getSelectionForeground( ) );
             }
 
             return docType;
 
         }
 
     }
 
     @Override
     public int showDialog( Component parent, String approveButtonText ) {
 
         int value = super.showDialog( parent, approveButtonText );
 
         if( option == NO_CUSTOM_OPTION ) {
             return value;
         }
         else
             return option;
     }
 
     @Override
     protected JDialog createDialog( Component parent ) throws HeadlessException {
 
         dialog = super.createDialog( parent );
         dialog.setModalityType(  Dialog.ModalityType.TOOLKIT_MODAL );
         List<Image> icons = new ArrayList<Image>( );
 
         icons.add( AssetsController.getImage( "img/Icono-Editor-16x16.png" ) );
         icons.add( AssetsController.getImage( "img/Icono-Editor-32x32.png" ) );
         icons.add( AssetsController.getImage( "img/Icono-Editor-64x64.png" ) );
         icons.add( AssetsController.getImage( "img/Icono-Editor-128x128.png" ) );
         dialog.setIconImages( icons );
 
         
         dialog.setTitle( TC.get( "StartDialog.Title" ) );
 
         return dialog;
     }
 
     /**
      * @return the recentFile
      */
     public File getRecentFile( ) {
 
         return recentFile;
     }
 
     /**
      * @param recentFile
      *            the recentFile to set
      */
     public void setRecentFile( File recentFile ) {
 
         this.recentFile = recentFile;
     }
 
     
     public boolean isOpenOption(){
         return tab.getSelectedIndex( )==1;
     }
     
     public int getOption( ) {
     
         return option;
     }
 
 }
