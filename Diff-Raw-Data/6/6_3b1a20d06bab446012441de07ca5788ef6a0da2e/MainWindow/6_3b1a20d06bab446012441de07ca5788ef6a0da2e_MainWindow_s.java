 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *          research group.
  *   
  *    Copyright 2005-2012 <e-UCM> research group.
  *  
  *     <e-UCM> is a research group of the Department of Software Engineering
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
  * This file is part of <e-Adventure>, version 1.4.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       <e-Adventure> is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      <e-Adventure> is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import javax.swing.filechooser.FileFilter;
 
 import es.eucm.eadventure.common.auxiliar.ReleaseFolders;
 import es.eucm.eadventure.common.auxiliar.ReportDialog;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.auxiliar.filefilters.FolderFileFilter;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.config.ConfigData;
 import es.eucm.eadventure.editor.gui.editdialogs.GenericFileChooserDialog;
 import es.eucm.eadventure.editor.gui.editdialogs.GenericOptionPaneDialog;
 import es.eucm.eadventure.editor.gui.structurepanel.StructureControl;
 import es.eucm.eadventure.editor.gui.structurepanel.StructurePanel;
 
 /**
  * This class represents the main frame of the application. It has all the
  * elements of the view part of the application, as well as the responsible
  * functions for showing and requesting data.
  */
 public class MainWindow extends JFrame {
 
     /**
      * Required.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Instance of the controller.
      */
     private Controller controller;
 
     /**
      * Chapters menu (to reload the chapters when needed).
      */
     private JMenu chaptersMenu;
 
     private JMenuItem itPlayerMode;
 
     private StructurePanel structurePanel;
     
 
     /**
      * Stack of windows opened.
      */
     private Stack<Window> windowsStack;
 
     private JMenuItem undo;
 
     private JMenuItem redo;
     
     private  JMenu fileMenu;
     
     private JCheckBoxMenuItem itAutoBackup;
 
     private JMenu configurationMenu;
 
     private JMenu runMenu;
     
     
     private JCheckBoxMenuItem paintGridMenuItem;
     private JCheckBoxMenuItem paintHotSpotsMenuItem;
     private JCheckBoxMenuItem paintBoundingAreasMenuItem;
 
     /**
      * Constructor. Creates the general layout.
      */
     public MainWindow( ) {
 
         // Store the controller
         controller = Controller.getInstance( );
 
         // Set the look and feel
         
         if (System.getProperty( "os.name" ).toLowerCase( ).contains( "win" ) || 
                 System.getProperty( "os.name" ).toLowerCase( ).contains( "mac" )){
            
             try {
                 //UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName( ) );
                 UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName( ) );
             }
             catch( Exception e ) {
                 //ErrorReportDialog.GenerateErrorReport(e, true, "UNKNOWERROR");
             }
         }
 
         // Create the list of icons of the window
         List<Image> icons = new ArrayList<Image>( );
 
         /*icons.add( AssetsController.getImage("img/Icono-Editor-16x16.png") );
         icons.add( AssetsController.getImage("img/Icono-Editor-32x32.png") );
         icons.add( AssetsController.getImage("img/Icono-Editor-64x64.png") );
         icons.add( AssetsController.getImage("img/Icono-Editor-128x128.png") );*/
         icons.add( new ImageIcon( "img/Icono-Editor-16x16.png" ).getImage( ) );
         icons.add( new ImageIcon( "img/Icono-Editor-32x32.png" ).getImage( ) );
         icons.add( new ImageIcon( "img/Icono-Editor-64x64.png" ).getImage( ) );
         icons.add( new ImageIcon( "img/Icono-Editor-128x128.png" ).getImage( ) );
         this.setIconImages( icons );
 
         // First of all, create the bar
         setJMenuBar( createMenuBar( ) );
 
         // Create the two panels
         JPanel editorContainer = new JPanel( ) {
             @Override
             public Component add(Component c) {
                 this.removeAll( );
                 return super.add(c);
             }
         };
         editorContainer.setMinimumSize( new Dimension( 400, 0 ) );
         editorContainer.setLayout( new BorderLayout( ) );
 
         structurePanel = new StructurePanel( editorContainer );
         structurePanel.recreateElements( );
         structurePanel.setMinimumSize( new Dimension( 210, 0 ) );
         structurePanel.setMaximumSize( new Dimension( 210, Integer.MAX_VALUE ) );
         structurePanel.setPreferredSize( new Dimension( 210, 0 ) );
         StructureControl.getInstance( ).setStructurePanel( structurePanel );
 
         JPanel structureToolsPanel = new JPanel( );
         structureToolsPanel.setLayout( new BorderLayout( ) );
         structureToolsPanel.setMinimumSize( new Dimension( 210, 0 ) );
         structureToolsPanel.setMaximumSize( new Dimension( 210, Integer.MAX_VALUE ) );
 
         structureToolsPanel.add( structurePanel, BorderLayout.CENTER );
 
         //		treeToolsPanel.add(treePanel, BorderLayout.CENTER);
         //JPanel treeToolsPanel = new JPanel();
         //treeToolsPanel.setLayout(new BorderLayout());
         //treeToolsPanel.add(structurePanel, BorderLayout.CENTER);
 
         JPanel toolsPanel = createToolsPanel( );
         structureToolsPanel.add( toolsPanel, BorderLayout.NORTH );
 
         // Create the split panel
         JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, structureToolsPanel, editorContainer );
         splitPane.setBorder( null );
 
         // Add the panels to the frame
         setLayout( new BorderLayout( ) );
         add( splitPane, BorderLayout.CENTER );
 
         // Set the "on close" operation and the closing listener
         setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
         addWindowListener( new WindowAdapter( ) {
 
             @Override
             public void windowClosing( WindowEvent arg0 ) {
 
                 controller.exit( );
             }
         } );
 
         // Create the windows stack
         windowsStack = new Stack<Window>( );
 
         // Set size and position
         setMinimumSize( new Dimension( 640, 400 ) );
         Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
         int width = (int) Math.min( 960, screenSize.getWidth( ) );
         int height = (int) Math.min( 720, screenSize.getHeight( ) );
         setSize( width, height );
         setLocation( Math.max( ( screenSize.width - width ) / 2, 0 ), Math.max(( screenSize.height - height ) / 2, 0) );
 
         //		setExtendedState(JFrame.MAXIMIZED_BOTH);
 
         this.setModalExclusionType( Dialog.ModalExclusionType.APPLICATION_EXCLUDE );
         // Set title and properties
         updateTitle( );
     }
 
     private JPanel createToolsPanel( ) {
 
         JPanel temp = new JPanel( );
 
         JButton undoButton = createToolButton( "img/icons/undo.png", TC.get( "Tools.Undo" ) );
         JButton redoButton = createToolButton( "img/icons/redo.png", TC.get( "Tools.Redo" ) );
         JButton backButton = createToolButton( "img/icons/moveNodeLeft.png", TC.get( "Tools.Back" ) );
         JButton forwardButton = createToolButton( "img/icons/moveNodeRight.png", TC.get( "Tools.Forward" ) );
         JButton findButton = createToolButton( "img/icons/find.png", TC.get( "Tools.Find" ) );
 
         undoButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.undoTool( );
             }
         } );
         redoButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.redoTool( );
             }
         } );
         findButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.search( );
             }
         } );
         backButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 StructureControl.getInstance( ).goBack( );
             }
         } );
         forwardButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 StructureControl.getInstance( ).goForward( );
             }
         } );
 
         temp.setLayout( new FlowLayout( FlowLayout.CENTER ) );
 
         temp.add( undoButton );
         temp.add( redoButton );
         JSeparator separator = new JSeparator( SwingConstants.VERTICAL );
         separator.setPreferredSize( new Dimension( 2, 24 ) );
         temp.add( separator );
         temp.add( findButton );
         separator = new JSeparator( SwingConstants.VERTICAL );
         separator.setPreferredSize( new Dimension( 2, 24 ) );
         temp.add( separator );
         temp.add( backButton );
         temp.add( forwardButton );
 
         temp.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "Tools.Title" ) ) );
 
         return temp;
     }
 
     private JButton createToolButton( String icon, String toolTipText ) {
 
         JButton tempButton = new JButton( new ImageIcon( icon ) );
         tempButton.setPreferredSize( new Dimension( 24, 22 ) );
         tempButton.setFocusPainted( false );
         tempButton.setContentAreaFilled( false );
         tempButton.setRolloverEnabled( true );
         tempButton.setToolTipText( toolTipText );
         return tempButton;
     }
 
     private JMenuBar createMenuBar( ) {
 
         return createMenuBarAdventureMode( );
     }
 
     private JMenuBar createMenuBarAdventureMode( ) {
 
         JMenuBar windowMenu = new JMenuBar( );
         //windowMenu.setLayout( new BoxLayout(windowMenu, BoxLayout.LINE_AXIS));
         windowMenu.setLayout( new FlowLayout( FlowLayout.LEFT ) );
 
         // Create the menus
         fileMenu = new JMenu( TC.get( "MenuFile.Title" ) );
         fileMenu.setMnemonic( KeyEvent.VK_F );
         windowMenu.add( fileMenu );
         JMenu editMenu = new JMenu( TC.get( "MenuEdit.Title" ) );
         windowMenu.add( editMenu );
         JMenu adventureMenu = new JMenu( TC.get( "MenuAdventure.Title" ) );
         adventureMenu.setEnabled( Controller.getInstance( ).isFolderLoaded( ) );
         adventureMenu.setMnemonic( KeyEvent.VK_A );
         windowMenu.add( adventureMenu );
         chaptersMenu = new JMenu( TC.get( "MenuChapters.Title" ) );
         chaptersMenu.setEnabled( Controller.getInstance( ).isFolderLoaded( ) );
         chaptersMenu.setMnemonic( KeyEvent.VK_H );
         windowMenu.add( chaptersMenu );
         runMenu = new JMenu( TC.get( "MenuRun.Title" ) );
         runMenu.setEnabled( Controller.getInstance( ).isFolderLoaded( ) );
         windowMenu.add( runMenu );
         configurationMenu = new JMenu( TC.get( "MenuConfiguration.Title" ) );
         configurationMenu.setMnemonic( KeyEvent.VK_T );
         windowMenu.add( configurationMenu );
         JMenu about = new JMenu( TC.get( "Menu.About" ) );
         JMenuItem aboutEadventure = new JMenuItem( TC.get( "Menu.AboutEAD" ) );
         about.add( aboutEadventure );
         aboutEadventure.setArmed( false );
         aboutEadventure.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 controller.showAboutDialog( );
             }
         } );
         JMenuItem sendComments = new JMenuItem( TC.get( "Menu.SendComments" ) );
         about.add( sendComments );
         sendComments.setArmed( false );
         sendComments.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 ReportDialog.GenerateCommentsReport( );
             }
         } );
         windowMenu.add( about );
 
         JMenuItem itFileNew = new JMenuItem( TC.get( "MenuFile.New" ) );
         itFileNew.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 Runnable r = new Runnable() {
                     public void run() {
                         controller.newFile( );
                 }};
                 
                 Thread t = new Thread(r);
                 t.setDaemon(false);
                 t.start();
                 
             }
         } );
         fileMenu.add( itFileNew );
 
         JMenuItem itFileLoad = new JMenuItem( TC.get( "MenuFile.Load" ) );
         itFileLoad.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 
                 Runnable r = new Runnable() {
                     public void run() {
                         controller.loadFile( );
                 }};
                 
                 Thread t = new Thread(r);
                 t.setDaemon(false);
                 t.start();
                 
                 
             }
         } );
 
         itFileLoad.setAccelerator( KeyStroke.getKeyStroke( 'L', InputEvent.CTRL_MASK ) );
         fileMenu.add( itFileLoad );
         fileMenu.addSeparator( );
         JMenuItem itFileSave = new JMenuItem( TC.get( "MenuFile.Save" ) );
         itFileSave.setEnabled( controller.isFolderLoaded( ) );
         itFileSave.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                       //controller.saveFile( controller.isTempFile( ) );
                         controller.saveFile( false );
             
                 
             }
         } );
         itFileSave.setAccelerator( KeyStroke.getKeyStroke( 'S', InputEvent.CTRL_MASK ) );
         fileMenu.add( itFileSave );
         JMenuItem itFileSaveAs = new JMenuItem( TC.get( "MenuFile.SaveAs" ) );
         itFileSaveAs.setEnabled( controller.isFolderLoaded( ) );
         itFileSaveAs.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.saveFile( true );
             }
         } );
         fileMenu.add( itFileSaveAs );
         fileMenu.addSeparator( );
 
         // create Lom Properties menu (IEEE LOM, IMS LOM, LOM-ES)
         JMenu itLomProperties = new JMenu( TC.get( "MenuFile.AllLOMProperties" ) );
 
         JMenuItem itLOMProp = new JMenuItem( TC.get( "MenuFile.LOMProperties" ) );
         itLOMProp.setEnabled( controller.isFolderLoaded( ) );
         itLOMProp.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.showLOMDataDialog( );
             }
         } );
         itLomProperties.add( itLOMProp );
         //itLomProperties.addSeparator( );
 
         JMenuItem itLOMSCORMProp = new JMenuItem( TC.get( "MenuFile.LOMSCORMProperties" ) );
         itLOMSCORMProp.setEnabled( controller.isFolderLoaded( ) );
         itLOMSCORMProp.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.showLOMSCORMDataDialog( );
             }
         } );
         itLomProperties.add( itLOMSCORMProp );
         //itLomProperties.addSeparator( );
 
         JMenuItem itLOMESProp = new JMenuItem( TC.get( "MenuFile.LOMESProperties" ) );
         //This option will be enabled only for Spanish
         itLOMESProp.setEnabled( controller.isFolderLoaded( )&&controller.getLanguage( ).toLowerCase( ).contains( "es" ) );
         itLOMESProp.setToolTipText( controller.getLanguage( ).toLowerCase( ).contains( "es" )?"":TC.get( "LOMES.Disabled" ) );
         itLOMESProp.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.showLOMESDataDialog( );
             }
         } );
         itLomProperties.add( itLOMESProp );
 
         fileMenu.add( itLomProperties );
         fileMenu.addSeparator( );
 
         JMenuItem importChapter = new JMenuItem( TC.get( "MenuFile.ImportChapter" ) );
         importChapter.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.importChapter( );
             }
         } );
 
         JMenu itExport = new JMenu( TC.get( "MenuFile.Export" ) );
         itExport.setEnabled( controller.isFolderLoaded( ) );
         JMenuItem itExportGame = new JMenuItem( TC.get( "MenuFile.ExportGame" ) );
         itExportGame.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.exportGame( );
             }
         } );
 
         JMenuItem itExportStandalone = new JMenuItem( TC.get( "MenuFile.ExportStandalone" ) );
         itExportStandalone.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.exportStandaloneGame( );
             }
         } );
 
         JMenuItem itExportLOM = new JMenuItem( TC.get( "MenuFile.ExportLOM" ) );
         itExportLOM.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.exportToLOM( );
             }
         } );
 
         itExport.add( itExportGame );
         itExport.add( itExportStandalone );
         itExport.add( itExportLOM );
         fileMenu.add( itExport );
         fileMenu.addSeparator( );
 
         JMenuItem itFileExit = new JMenuItem( TC.get( "MenuFile.Exit" ) );
         itFileExit.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.exit( );
             }
         } );
         fileMenu.add( itFileExit );
 
         undo = new JMenuItem( TC.get( "MenuEdit.Undo" ) );
         undo.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.undoTool( );
             }
         } );
         undo.setAccelerator( KeyStroke.getKeyStroke( 'Z', InputEvent.CTRL_MASK ) );
 
         redo = new JMenuItem( TC.get( "MenuEdit.Redo" ) );
         redo.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.redoTool( );
             }
         } );
         redo.setAccelerator( KeyStroke.getKeyStroke( 'Y', InputEvent.CTRL_MASK ) );
 
         JMenuItem search = new JMenuItem( TC.get( "Search.DialogTitle" ) );
         search.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.search( );
             }
         } );
 
         editMenu.add( undo );
         editMenu.add( redo );
         editMenu.add( search );
 
         // Create the "Adventure" elements
         JMenuItem itCheckConsistency = new JMenuItem( TC.get( "MenuAdventure.CheckConsistency" ) );
         itCheckConsistency.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.checkAdventureConsistency( );
             }
         } );
         adventureMenu.add( itCheckConsistency );
         adventureMenu.addSeparator( );
         JMenuItem itAdventureData = new JMenuItem( TC.get( "MenuAdventure.AdventureData" ) );
         itAdventureData.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.showAdventureDataDialog( );
             }
         } );
         adventureMenu.add( itAdventureData );
 
         JMenu visualization = new JMenu( TC.get( "MenuAdventure.Visualization" ) );
         //Now the traditional hud can be used
         /*JMenuItem itGUIStyles = new JMenuItem( TC.get( "MenuAdventure.GUIStyles" ) );
         itGUIStyles.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.showGUIStylesDialog( );
             }
         } );
         visualization.add( itGUIStyles );*/
         JMenuItem itCustomizeGUI = new JMenuItem( TC.get( "MenuAdventure.CustomizeGUI" ) );
         itCustomizeGUI.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.showCustomizeGUIDialog( );
             }
         } );
         visualization.add( itCustomizeGUI );
         JMenuItem itGraphicConfig = new JMenuItem( TC.get( "MenuAdventure.GraphicConfig" ) );
         itGraphicConfig.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.showGraphicConfigDialog( );
             }
         } );
         visualization.add( itGraphicConfig );
         adventureMenu.add( visualization );
         if( controller.isFolderLoaded( ) ) {
             if( controller.isPlayTransparent( ) ) {
                 itPlayerMode = new JMenuItem( TC.get( "MenuAdventure.ChangeToModePlayerVisible" ) );
                 itPlayerMode.setToolTipText( TC.get( "MenuAdventure.ModePlayerVisible" ) );
             }
             else {
                 itPlayerMode = new JMenuItem( TC.get( "MenuAdventure.ChangeToModePlayerTransparent" ) );
                 itPlayerMode.setToolTipText( TC.get( "MenuAdventure.ModePlayerTransparent" ) );
             }
         }
         else {
             itPlayerMode = new JMenuItem( TC.get( "MenuAdventure.ChangeToModePlayerVisible" ) );
             itPlayerMode.setEnabled( false );
         }
         itPlayerMode.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.swapPlayerMode( true );
                 reloadData( );
             }
 
         } );
         adventureMenu.add( itPlayerMode );
         adventureMenu.addSeparator( );
         /*JMenuItem itAssessmentFiles = new JMenuItem( TextConstants.getText( "MenuAdventure.AssessmentFiles" ) );
         itAssessmentFiles.addActionListener( new ActionListener( ) {
         	public void actionPerformed( ActionEvent e ) {
         		controller.showAssessmentFilesDialog( );
         	}
         } );
         adventureMenu.add( itAssessmentFiles );
         JMenuItem itAdaptationFiles = new JMenuItem( TextConstants.getText( "MenuAdventure.AdaptationFiles" ) );
         itAdaptationFiles.addActionListener( new ActionListener( ) {
         	public void actionPerformed( ActionEvent e ) {
         		controller.showAdaptationFilesDialog( );
         	}
         } );
         adventureMenu.add( itAdaptationFiles );*/
         //adventureMenu.addSeparator( );
         JMenuItem itDeleteUnusedAssets = new JMenuItem( TC.get( "MenuAdventure.DeleteUnusedAssets" ) );
         itDeleteUnusedAssets.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.deleteUnsuedAssets( );
             }
         } );
         adventureMenu.add( itDeleteUnusedAssets );
 
         // BE CAREFULL!! if you add/remove some items in the chapter menu, change the index at updateChapterMenu()
         // (there are a commentary there explaining how-to)
         
         // Create the "Chapter" elements
         JMenuItem itAddChapter = new JMenuItem( TC.get( "MenuChapters.AddChapter" ) );
         itAddChapter.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.addChapter( );
             }
         } );
         chaptersMenu.add( itAddChapter );
         JMenuItem itDeleteChapter = new JMenuItem( TC.get( "MenuChapters.DeleteChapter" ) );
         itDeleteChapter.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.deleteChapter( );
             }
         } );
         chaptersMenu.add( itDeleteChapter );
         
         // import chapter option
         JMenuItem itImportChapter = new JMenuItem( TC.get( "MenuChapters.ImportChapter" ));
         itImportChapter.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.importChapter( );
             }
         } );
         chaptersMenu.add( itImportChapter );
         
         chaptersMenu.addSeparator( );
         
         JMenuItem itMoveChapterUp = new JMenuItem( TC.get( "MenuChapters.MoveChapterUp" ) );
         itMoveChapterUp.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.moveChapterUp( );
             }
         } );
         chaptersMenu.add( itMoveChapterUp );
         JMenuItem itMoveChapterDown = new JMenuItem( TC.get( "MenuChapters.MoveChapterDown" ) );
         itMoveChapterDown.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.moveChapterDown( );
             }
         } );
         chaptersMenu.add( itMoveChapterDown );
         chaptersMenu.addSeparator( );
         JMenuItem itEditFlags = new JMenuItem( TC.get( "MenuChapters.Flags" ) );
         itEditFlags.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.showEditFlagDialog( );
             }
         } );
         itEditFlags.setAccelerator( KeyStroke.getKeyStroke( 'F', InputEvent.CTRL_MASK ) );
         chaptersMenu.add( itEditFlags );
         chaptersMenu.addSeparator( );
         updateChapterMenu( );
 
         // Create "run" elements
         JMenuItem normalRun = new JMenuItem( TC.get( "MenuRun.Normal" ) );
         normalRun.setAccelerator( KeyStroke.getKeyStroke( 'R', InputEvent.CTRL_MASK ) );
         normalRun.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.run( );
             }
 
         } );
         runMenu.add( normalRun );
 
         JMenuItem debugRun = new JMenuItem( TC.get( "MenuRun.Debug" ) );
         debugRun.setAccelerator( KeyStroke.getKeyStroke( 'D', InputEvent.CTRL_MASK ) );
         debugRun.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.debugRun( );
             }
 
         } );
         runMenu.add( debugRun );
 
         itAutoBackup = new JCheckBoxMenuItem( TC.get( "MenuConfiguration.AutoBackup" ), controller.getAutoSaveEnabled( ) );
         itAutoBackup.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 controller.setAutoSaveEnabled( ( (JCheckBoxMenuItem) e.getSource( ) ).isSelected( ) );
             }
         } );
         //		configurationMenu.add( itAutoBackup );
         
         JMenu languageMenu = new JMenu( TC.get( "MenuConfiguration.Language" ) );
         for (final String language : ReleaseFolders.getLanguages("editor")) {
             JCheckBoxMenuItem itEnglish = new JCheckBoxMenuItem( ReleaseFolders.getLanguageName( language ), controller.getLanguage( ).equals( language ) );
             itEnglish.addActionListener( new ActionListener( ) {
                 public void actionPerformed( ActionEvent e ) {
                     controller.setLanguage( language );
                 }
             } );
             languageMenu.add( itEnglish );
         }
         configurationMenu.add( languageMenu );
         
         JMenu debugOptionsMenu = new JMenu( TC.get( "MenuConfiguration.DebugOptions" ) );
         paintGridMenuItem = new JCheckBoxMenuItem( TC.get( "MenuConfiguration.DebugOptions.PaintGrid" ), ConfigData.getDebugOptions( ).isPaintGrid( ) );
         paintHotSpotsMenuItem = new JCheckBoxMenuItem( TC.get( "MenuConfiguration.DebugOptions.PaintHotSpots" ), ConfigData.getDebugOptions( ).isPaintHotSpots( ) );
         paintBoundingAreasMenuItem = new JCheckBoxMenuItem( TC.get( "MenuConfiguration.DebugOptions.PaintBoundingAreas" ), ConfigData.getDebugOptions( ).isPaintBoundingAreas( ) );
         paintGridMenuItem.addActionListener( new ActionListener(){
 
             public void actionPerformed( ActionEvent e ) {
                 ConfigData.getDebugOptions( ).setPaintGrid( paintGridMenuItem.isSelected( ) );
             }
             
         });
         debugOptionsMenu.add( paintGridMenuItem );
         
         paintHotSpotsMenuItem.addActionListener( new ActionListener(){
 
             public void actionPerformed( ActionEvent e ) {
                 ConfigData.getDebugOptions( ).setPaintHotSpots( paintHotSpotsMenuItem.isSelected( ) );
             }
             
         });
         debugOptionsMenu.add( paintHotSpotsMenuItem );
         
         paintBoundingAreasMenuItem.addActionListener( new ActionListener(){
 
             public void actionPerformed( ActionEvent e ) {
                 ConfigData.getDebugOptions( ).setPaintBoundingAreas( paintBoundingAreasMenuItem.isSelected( ) );
             }
             
         });
         debugOptionsMenu.add( paintBoundingAreasMenuItem );
         
         configurationMenu.add( debugOptionsMenu );
 
 
         return windowMenu;
     }
 
     public void setNormalRunAvailable( boolean available ) {
 
         this.runMenu.setEnabled( available );
         this.fileMenu.setEnabled( available );
         this.chaptersMenu.setEnabled( available );
         this.itPlayerMode.setEnabled( available );
         this.configurationMenu.setEnabled( available );
        
        
     }
 
     /**
      * This method reloads the data of the window. It picks the new tree, along
      * with the file name.
      */
     public void reloadData( ) {
 
         structurePanel.recreateElements( );
 
         updateChapterMenu( );
 
         //Update the change player mode item menu
         if( controller.isPlayTransparent( ) ) {
             itPlayerMode.setText( TC.get( "MenuAdventure.ChangeToModePlayerVisible" ) );
             itPlayerMode.setToolTipText( TC.get( "MenuAdventure.ModePlayerVisible" ) );
         }
         else {
             itPlayerMode.setText( TC.get( "MenuAdventure.ChangeToModePlayerTransparent" ) );
             itPlayerMode.setToolTipText( TC.get( "MenuAdventure.ModePlayerTransparent" ) );
         }
         itAutoBackup.setSelected( controller.getAutoSaveEnabled( ) );
 
         StructureControl.getInstance( ).changeDataControl( controller.getSelectedChapterDataControl( ) );
         StructureControl.getInstance( ).visitDataControl( controller.getSelectedChapterDataControl( ) );
         
         //controller.getSelectedChapterDataControl( ).get
         
         // Update the menu bar
         this.setJMenuBar( createMenuBar( ) );
         this.getJMenuBar( ).updateUI( );
 
         // Update the title
         updateTitle( );
 
         this.repaint( );
     }
 
     /**
      * Updates the title of the window.
      */
     public void updateTitle( ) {
 
         String modified = controller.isDataModified( ) ? " *" : "";
         //if( controller.isTempFile( ) ) {
         //	setTitle( TextConstants.getText( "MainWindow.Title.NewFile" ) + modified );
         //} else {
 
         setTitle( TC.get( "MainWindow.Title", controller.getFileName( ) ) + modified );
         //		}
     }
 
     /**
      * Updates the chapter menu, deleting all the items and adding new ones.
      */
     public void updateChapterMenu( ) {
 
         // First, delete all chapter elements (there are eight elements above the chapters in the menu)
         
             
         int chapterIndex = 0;
         while( chaptersMenu.getItemCount( ) > 8 )
             chaptersMenu.remove( 8 );
         //countChapter++;
         if( Controller.getInstance( ).isFolderLoaded( ) ) {
             // Then, add the new chapters to the menu
            chapterIndex = 0;
             ButtonGroup chapterButtonGroup = new ButtonGroup( );
             for( String chapterTitle : controller.getChapterTitles( ) ) {
                 // Create the button, add the action listener and set an accelerator for the first nine chapters
                 JRadioButtonMenuItem itChapter = new JRadioButtonMenuItem( ( chapterIndex + 1 ) + ": " + chapterTitle, controller.getSelectedChapter( ) == chapterIndex );
                 itChapter.setEnabled( true );
 
                 itChapter.addActionListener( new ChapterMenuItemListener( chapterIndex ) );
                 if( chapterIndex < 9 )
                     itChapter.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_1 + chapterIndex++, InputEvent.CTRL_MASK ) );
 
                 // Add to the chapter menu and the chapter button group
                 chapterButtonGroup.add( itChapter );
                 chaptersMenu.add( itChapter );
             }
         }
         if( chapterIndex < 2)
         {
             // Change those index if some elements in the chapter menu are added/removed
             chaptersMenu.getItem( 4 ).setEnabled( false );   
             chaptersMenu.getItem( 5 ).setEnabled( false );   
         }
     
     }
 
     public void updateStructure( ) {
 
         structurePanel.update( );
     }
 
     public void updatePanel( ) {
 
         for( Window window : windowsStack ) {
             if( window instanceof Updateable ) {
                 ( (Updateable) window ).updateFields( );
             }
         }
         structurePanel.updateElementPanel( );
     }
 
     public void reloadPanel( ) {
 
         for( Window window : windowsStack ) {
             if( window instanceof Updateable ) {
                 ( (Updateable) window ).updateFields( );
             }
         }
         structurePanel.reloadElementPanel( );
     }
 
     /**
      * Returns the last window opened by the application.
      * 
      * @return Last window opened
      */
     public Window peekWindow( ) {
 
         Window window;
 
         // If the stack is empty, take the main window
         if( windowsStack.empty( ) )
             window = this;
         // If not, take a peek at the last window pushed
         else
             window = windowsStack.peek( );
 
         return window;
     }
 
     /**
      * Pushes a new window in the windows stack.
      * 
      * @param window
      *            Window to push
      */
     public void pushWindow( Window window ) {
 
         windowsStack.push( window );
     }
 
     /**
      * Pops the last window pushed into the stack.
      */
     public void popWindow( ) {
 
         if (!windowsStack.isEmpty( ))
             windowsStack.pop( );
         structurePanel.updateElementPanel( );
     }
 
     /**
      * Shows a load dialog to select a single existing file.
      * 
      * @param path
      *            Base path for the dialog
      * @param filter
      *            File filter for the dialog
      * @return Full path of the selected file, null if no file was selected
      */
     public String showSingleSelectionLoadDialog( String path, FileFilter filter ) {
 
         String fileFullPath = null;
 
         // If no path gas given, set the path of the application
         if( path == null )
             path = ".";
 
         // Create the file chooser dialog
         //JFileChooser fileDialog = new JFileChooser( path );
         GenericFileChooserDialog fileDialog = new GenericFileChooserDialog( path );
         fileDialog.setAcceptAllFileFilterUsed( false );
         fileDialog.setFileFilter( filter );
 
         // If a file has really been choosen and opened, load it
         if( fileDialog.showOpenDialog( peekWindow( ) ) == JFileChooser.APPROVE_OPTION ) {
             fileFullPath = fileDialog.getSelectedFile( ).getAbsolutePath( );
         }
 
         return fileFullPath;
     }
 
     /**
      * Shows a load dialog to select multiple existing files.
      * 
      * @param path
      *            Base path for the dialog
      * @param filter
      *            File filter for the dialog
      * @return Full path of the selected files, null if no files were selected
      */
     public String[] showMultipleSelectionLoadDialog( String path, FileFilter filter ) {
 
         String[] filesFullPath = null;
 
         // If no path gas given, set the path of the application
         if( path == null )
             path = ".";
 
         // Create the file chooser dialog
         //JFileChooser fileDialog = new JFileChooser( path );
         GenericFileChooserDialog fileDialog = new GenericFileChooserDialog( path );
         fileDialog.setAcceptAllFileFilterUsed( false );
         fileDialog.setMultiSelectionEnabled( true );
         fileDialog.setFileFilter( filter );
 
         // If a file has really been choosen and opened, load it
         if( fileDialog.showOpenDialog( peekWindow( ) ) == JFileChooser.APPROVE_OPTION ) {
             // Take the selected files and initialize the array
             File[] selectedFiles = fileDialog.getSelectedFiles( );
             filesFullPath = new String[ selectedFiles.length ];
 
             // Copy the data
             for( int i = 0; i < selectedFiles.length; i++ )
                 filesFullPath[i] = selectedFiles[i].getAbsolutePath( );
         }
 
         return filesFullPath;
     }
 
     /**
      * Shows a save dialog to select a file.
      * 
      * @param path
      *            Base path for the dialog
      * @param filter
      *            File filter for the dialog
      * @return Full path of the selected file, null if no file was selected
      */
     public String showSaveDialog( String path, FileFilter filter ) {
 
         String fileFullPath = null;
 
         // Create the file chooser dialog
         //JFileChooser fileDialog = new JFileChooser( path );
         GenericFileChooserDialog fileDialog = new GenericFileChooserDialog( path );
         if( filter instanceof FolderFileFilter ) {
             ( (FolderFileFilter) filter ).setFileChooser( fileDialog );
         }
         fileDialog.setFileFilter( filter );
 
         // If a file has really been choosen and opened, load it
         if( fileDialog.showSaveDialog( peekWindow( ) ) == JFileChooser.APPROVE_OPTION ) {
             fileFullPath = fileDialog.getSelectedFile( ).getAbsolutePath( );
         }
 
         return fileFullPath;
     }
 
     /**
      * Shows a dialog with the given set of options.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @param options
      *            Array of strings containing the options of the dialog
      * @return The index of the option selected, JOptionPane.CLOSED_OPTION if
      *         the dialog was closed.
      */
     public int showOptionDialog( String title, String message, String[] options ) {
 
         // Create the panel
         JPanel messagePanel = new JPanel( );
         messagePanel.setLayout( new BorderLayout( ) );
 
         // Create the text pane
         JTextPane messageTextPane = new JTextPane( );
         messageTextPane.setPreferredSize( new Dimension( 220, 80 ) );
         messageTextPane.setEditable( false );
         messageTextPane.setBackground( messagePanel.getBackground( ) );
         messageTextPane.setText( message );
         messagePanel.add( messageTextPane, BorderLayout.CENTER );
 
         // Show the dialog
         //return JOptionPane.showOptionDialog( peekWindow( ), messagePanel, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null );
         //return (Integer) new GenericOptionPaneDialog( peekWindow( ), title, messagePanel, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, options ).getIntegerOption( );
   
         return (Integer)GenericOptionPaneDialog.showOptionDialog(  peekWindow( ), title, message, JOptionPane.PLAIN_MESSAGE, options );
     
     }
 
     /**
      * Shows a dialog with the options "Yes", "No" and "Cancel", with the given
      * title and text.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @return Value for the option selected. It can be JOptionPane.YES_OPTION,
      *         JOptionPane.NO_OPTION or JOptionPane.CANCEL_OPTION
      */
     public int showConfirmDialog( String title, String message ) {
 
         //return JOptionPane.showConfirmDialog( peekWindow( ), message, title, JOptionPane.YES_NO_CANCEL_OPTION );
         return (Integer) GenericOptionPaneDialog.showConfirmDialog( peekWindow( ), title, message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION );
     }
 
     /**
      * Shows a dialog with the options "Yes" and "No", with the given title and
      * text.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @return True if the "Yes" button was pressed, false otherwise
      */
     public boolean showStrictConfirmDialog( String title, String message ) {
 
         //return JOptionPane.showConfirmDialog( peekWindow( ), message, title, JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION;
         return (Integer) GenericOptionPaneDialog.showConfirmDialog( peekWindow( ), title, message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION;
     }
 
     /**
      * Shows an input dialog with the given title, message and default value.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @param defaultValue
      *            Default value of the dialog
      * @return String typed in the dialog, null if the cancel button was pressed
      */
     public String showInputDialog( String title, String message, String defaultValue ) {
 
         //return (String) JOptionPane.showInputDialog( peekWindow( ), message, title, JOptionPane.PLAIN_MESSAGE, null, null, defaultValue );
         return (String) GenericOptionPaneDialog.showInputDialog( peekWindow( ), title, message, JOptionPane.PLAIN_MESSAGE, null, defaultValue );
     }
     
     public String showInputDialog( String title, String message ) {
 
         //return (String) JOptionPane.showInputDialog( peekWindow( ), message, title, JOptionPane.PLAIN_MESSAGE, null, null, defaultValue );
         return (String) GenericOptionPaneDialog.showInputDialog( peekWindow( ), title, message, JOptionPane.PLAIN_MESSAGE, null, null );
     }
 
     /**
      * Shows an input dialog with the given title, message and set of possible
      * values.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @param selectionValues
      *            Possible selection values of the dialog
      * @return Option selected in the dialog, null if the cancel button was
      *         pressed
      */
     public String showInputDialog( String title, String message, Object[] selectionValues ) {
 
         //return (String) JOptionPane.showInputDialog( peekWindow( ), message, title, JOptionPane.PLAIN_MESSAGE, null, selectionValues, null );
         return (String) GenericOptionPaneDialog.showInputDialog( peekWindow( ), title, message, JOptionPane.PLAIN_MESSAGE, selectionValues, null );
     }
 
     /**
      * Shows an information dialog with the given title and message.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      */
     public void showInformationDialog( String title, String message ) {
 
         //JOptionPane.showMessageDialog( peekWindow( ), message, title, JOptionPane.INFORMATION_MESSAGE );
         GenericOptionPaneDialog.showMessageDialog( peekWindow( ), title, message, JOptionPane.INFORMATION_MESSAGE );
     }
 
     /**
      * Shows a warning dialog with the given title and message.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      */
     public void showWarningDialog( String title, String message ) {
 
         //JOptionPane.showMessageDialog( peekWindow( ), message, title, JOptionPane.WARNING_MESSAGE );
         GenericOptionPaneDialog.showMessageDialog( peekWindow( ), title, message, JOptionPane.WARNING_MESSAGE );
     }
 
     /**
      * Shows an error dialog with the given title and message.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      */
     public void showErrorDialog( String title, String message ) {
 
         //JOptionPane.showMessageDialog( peekWindow( ), message, title, JOptionPane.ERROR_MESSAGE );
         GenericOptionPaneDialog.showMessageDialog( peekWindow( ), title, message, JOptionPane.ERROR_MESSAGE );
     }
 
     /**
      * Listener for the chapter elements in the "Chapter" menu. Used to switch
      * between chapters when the user desires so.
      */
     private class ChapterMenuItemListener implements ActionListener {
 
         /**
          * Index of the chapter that holds this listener.
          */
         private int chapterIndex;
 
         /**
          * Constructor.
          * 
          * @param chapterIndex
          *            Chapter index of the button holding this listener
          */
         public ChapterMenuItemListener( int chapterIndex ) {
 
             this.chapterIndex = chapterIndex;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed( ActionEvent e ) {
 
             controller.setSelectedChapter( chapterIndex );
         }
     }
 }
