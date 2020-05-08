 package com.hifiremote.jp1;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.StringTokenizer;
 
 import javax.swing.AbstractAction;
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkListener;
 
 import com.hifiremote.LibraryLoader;
 import com.hifiremote.jp1.io.IO;
 import com.hifiremote.jp1.io.JP12Serial;
 import com.hifiremote.jp1.io.JP1Parallel;
 import com.hifiremote.jp1.io.JP1USB;
 
 /**
  * Description of the Class.
  * 
  * @author Greg
  * @created November 30, 2006
  */
 
 public class RemoteMaster extends JP1Frame implements ActionListener, PropertyChangeListener, HyperlinkListener,
     ChangeListener
 {
   public static final int MAX_RDF_SYNC = 4;
   public static final int MIN_RDF_SYNC = 3;
 
   /** The frame. */
   private static JP1Frame frame = null;
 
   /** Description of the Field. */
   public final static String version = "v2.00-preview5";
 
   /** The dir. */
   private File dir = null;
 
   /** Description of the Field. */
   public File file = null;
 
   /** The remote config. */
   private RemoteConfiguration remoteConfig = null;
 
   private RMAction newAction = null;
   private RMAction newUpgradeAction = null;
 
   private RMAction codesAction = null;
 
   /** The open item. */
   private RMAction openAction = null;
 
   /** The save item. */
   private RMAction saveAction = null;
 
   /** The save as item. */
   private RMAction saveAsAction = null;
 
   private RMAction openRdfAction = null;
 
   /** The export ir item. */
   private JMenuItem exportIRItem = null;
 
   /** The recent files. */
   private JMenu recentFiles = null;
 
   /** The exit item. */
   private JMenuItem exitItem = null;
 
   // Remote menu items
   /** The interfaces. */
   private ArrayList< IO > interfaces = new ArrayList< IO >();
 
   /** The download action. */
   private RMAction downloadAction = null;
 
   /** The upload action. */
   private RMAction uploadAction = null;
 
   /** The raw download item */
   private JMenuItem downloadRawItem = null;
 
   /** The verify upload item */
   private JCheckBoxMenuItem verifyUploadItem = null;
 
   /** The upload wav item. */
   private JMenuItem uploadWavItem = null;
 
   /** The look and feel items. */
   private JRadioButtonMenuItem[] lookAndFeelItems = null;
 
   // Help menu items
   private JMenuItem readmeItem = null;
 
   private JMenuItem tutorialItem = null;
 
   private JMenuItem homePageItem = null;
 
   private JMenuItem learnedSignalItem = null;
 
   private JMenuItem wikiItem = null;
 
   private JMenuItem forumItem = null;
 
   /** The about item. */
   private JMenuItem aboutItem = null;
 
   /** The tabbed pane. */
   private JTabbedPane tabbedPane = null;
 
   private RMPanel currentPanel = null;
 
   /** The general panel. */
   private GeneralPanel generalPanel = null;
 
   /** The key move panel. */
   private KeyMovePanel keyMovePanel = null;
 
   /** The macro panel. */
   private MacroPanel macroPanel = null;
 
   /** The special function panel. */
   private SpecialFunctionPanel specialFunctionPanel = null;
 
   private TimedMacroPanel timedMacroPanel = null;
 
   private FavScanPanel favScanPanel = null;
 
   /** The device panel. */
   private DeviceUpgradePanel devicePanel = null;
 
   /** The protocol panel. */
   private ProtocolUpgradePanel protocolPanel = null;
 
   /** The learned panel. */
   private LearnedSignalPanel learnedPanel = null;
 
   /** The raw data panel. */
   private RawDataPanel rawDataPanel = null;
 
   /** The adv progress bar. */
   private JProgressBar advProgressBar = null;
 
   /** The upgrade progress bar. */
   private JProgressBar upgradeProgressBar = null;
   private JProgressBar devUpgradeProgressBar = null;
 
   private JPanel upgradeProgressPanel = null;
 
   /** The learned progress bar. */
   private JProgressBar learnedProgressBar = null;
 
   private boolean hasInvalidCodes = false;
 
   private CodeSelectorDialog codeSelectorDialog = null;
 
   private TextFileViewer rdfViewer = null;
 
   private class RMAction extends AbstractAction
   {
     public RMAction( String text, String action, ImageIcon icon, String description, Integer mnemonic )
     {
       super( text, icon );
       putValue( ACTION_COMMAND_KEY, action );
       putValue( SHORT_DESCRIPTION, description );
       putValue( MNEMONIC_KEY, mnemonic );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     @Override
     public void actionPerformed( ActionEvent event )
     {
       try
       {
         String command = event.getActionCommand();
         if ( command.equals( "NEW" ) )
         {
           Remote remote = RMNewDialog.showDialog( RemoteMaster.this );
           remote.load();
           remoteConfig = new RemoteConfiguration( remote );
           remoteConfig.initializeSetup();
           remoteConfig.updateImage();
           remoteConfig.setDateIndicator();
           remoteConfig.setSavedData();
           update();
           saveAction.setEnabled( false );
           saveAsAction.setEnabled( true );
           openRdfAction.setEnabled( true );
           uploadAction.setEnabled( !interfaces.isEmpty() );
         }
         else if ( command.equals( "NEWDEVICE" ) )
         {
           new KeyMapMaster( properties );
         }
         else if ( command.equals( "OPEN" ) )
         {
           openFile();
         }
         else if ( command.equals( "SAVE" ) )
         {
           if ( !allowSave( Remote.SetupValidation.WARN ) )
           {
             return;
           }
           remoteConfig.save( file );
         }
         else if ( command.equals( "SAVEAS" ) )
         {
           if ( !allowSave( Remote.SetupValidation.WARN ) )
           {
             return;
           }
           saveAs();
         }
         else if ( command.equals( "DOWNLOAD" ) )
         {
           IO io = getOpenInterface();
           if ( io == null )
           {
             JOptionPane.showMessageDialog( RemoteMaster.this, "No remotes found!" );
             return;
           }
           String sig = io.getRemoteSignature();
           Remote currentRemote = null;
           Remote remote = null;
           if ( remoteConfig != null )
           {
             currentRemote = remoteConfig.getRemote();
           }
           if ( currentRemote == null || !currentRemote.getSignature().equals( sig ) )
           {
             List< Remote > remotes = RemoteManager.getRemoteManager().findRemoteBySignature( sig );
             if ( remotes.isEmpty() )
             {
               JOptionPane.showMessageDialog( RemoteMaster.this, "No RDF matches signature " + sig );
               io.closeRemote();
               return;
             }
             else if ( remotes.size() == 1 )
             {
               remote = remotes.get( 0 );
             }
             else
             {// ( remotes.length > 1 )
               int maxEepromSize = 0;
               for ( Remote r : remotes )
               {
                 r.load();
                 for ( FixedData fixedData : r.getFixedData() )
                 {
                   maxEepromSize = Math.max( maxEepromSize, fixedData.getAddress() + fixedData.getData().length - 1 );
                 }
               }
 
               short[] buffer = new short[ maxEepromSize ];
               io.readRemote( remotes.get( 0 ).getBaseAddress(), buffer );
 
               Remote[] choices = new Remote[ remotes.size() ];
               choices = remotes.toArray( choices );
               String message = "Please pick the best match to your remote from the following list:";
               Object rc = JOptionPane.showInputDialog( null, message, "Ambiguous Remote", JOptionPane.ERROR_MESSAGE,
                   null, choices, choices[ 0 ] );
               if ( rc == null )
               {
                 io.closeRemote();
                 return;
               }
               else
               {
                 remote = ( Remote )rc;
               }
             }
           }
           else
           {
             remote = currentRemote;
           }
           remote.load();
           remoteConfig = new RemoteConfiguration( remote );
           io.readRemote( remote.getBaseAddress(), remoteConfig.getData() );
           io.closeRemote();
           remoteConfig.parseData();
           saveAction.setEnabled( false );
           saveAsAction.setEnabled( true );
           uploadAction.setEnabled( true );
           update();
         }
         else if ( command.equals( "UPLOAD" ) )
         {
           Remote remote = remoteConfig.getRemote();
           if ( !allowSave( remote.getSetupValidation() ) )
           {
             return;
           }
           IO io = getOpenInterface();
           if ( io == null )
           {
             JOptionPane.showMessageDialog( RemoteMaster.this, "No remotes found!" );
             return;
           }
           String sig = io.getRemoteSignature();
           if ( !sig.equals( remote.getSignature() ) )
           {
             Object[] options =
             {
                 "Upload to the remote", "Cancel the upload"
             };
             int rc = JOptionPane
                 .showOptionDialog(
                     RemoteMaster.this,
                     "The signature of the attached remote does not match the signature you are trying to upload.  The image\n"
                         + "you are trying to upload may not be compatible with attached remote, and uploading it may damage the\n"
                         + "remote.  Copying the contents of one remote to another is only safe when the remotes are identical.\n\n"
                         + "This message will be displayed when installing an extender in your remote, which is the only time it is\n"
                         + "safe to upload to a remote when the signatures do not match.\n\n"
                         + "How would you like to proceed?", "Upload Signature Mismatch", JOptionPane.DEFAULT_OPTION,
                     JOptionPane.WARNING_MESSAGE, null, options, options[ 1 ] );
             if ( rc == 1 || rc == JOptionPane.CLOSED_OPTION )
             {
               io.closeRemote();
               return;
             }
           }
 
           AutoClockSet autoClockSet = remote.getAutoClockSet();
           short[] data = remoteConfig.getData();
           if ( autoClockSet != null )
           {
             autoClockSet.saveTimeBytes( data );
             autoClockSet.setTimeBytes( data );
             remoteConfig.updateCheckSums();
           }
 
           int rc = io.writeRemote( remote.getBaseAddress(), data );
 
           if ( rc != data.length )
           {
             io.closeRemote();
             JOptionPane.showMessageDialog( RemoteMaster.this, "writeRemote returned " + rc );
             return;
           }
           if ( verifyUploadItem.isSelected() )
           {
             short[] readBack = new short[ data.length ];
             rc = io.readRemote( remote.getBaseAddress(), readBack );
             io.closeRemote();
             if ( rc != data.length )
             {
               JOptionPane.showMessageDialog( RemoteMaster.this, "Upload verify failed: read back " + rc
                   + " byte, but expected " + data.length );
 
             }
             else if ( !Hex.equals( data, readBack ) )
             {
               JOptionPane.showMessageDialog( RemoteMaster.this,
                   "Upload verify failed: data read back doesn't match data written." );
             }
           }
           else
           {
             io.closeRemote();
             JOptionPane.showMessageDialog( RemoteMaster.this, "Upload complete!" );
           }
           if ( autoClockSet != null )
           {
             autoClockSet.restoreTimeBytes( data );
             remoteConfig.updateCheckSums();
           }
         }
         else if ( command == "OPENRDF" )
         {
           String title = "View/Edit RDF";
           rdfViewer = TextFileViewer.showFile( RemoteMaster.this, remoteConfig.getRemote().getFile(), title, false );
         }
         else if ( command == "OPENCODES" )
         {
           codeSelectorDialog = CodeSelectorDialog.showDialog( RemoteMaster.this );
           codeSelectorDialog.enableAssign( currentPanel == generalPanel );
         }
       }
       catch ( Exception ex )
       {
         ex.printStackTrace( System.err );
       }
     }
   }
 
   /**
    * Constructor for the RemoteMaster object.
    * 
    * @param workDir
    *          the work dir
    * @param prefs
    *          the prefs
    * @throws Exception
    *           the exception
    * @exception Exception
    *              Description of the Exception
    */
   public RemoteMaster( File workDir, PropertyFile prefs ) throws Exception
   {
     super( "RM IR", prefs );
     dir = properties.getFileProperty( "IRPath", workDir );
 
     JToolBar toolBar = new JToolBar();
     toolBar.setFloatable( false );
 
     createMenus( toolBar );
 
     setDefaultCloseOperation( DISPOSE_ON_CLOSE );
     setDefaultLookAndFeelDecorated( true );
 
     addWindowListener( new WindowAdapter()
     {
       @Override
       public void windowClosing( WindowEvent event )
       {
         try
         {
           for ( int i = 0; i < recentFiles.getItemCount(); ++i )
           {
             JMenuItem item = recentFiles.getItem( i );
             properties.setProperty( "RecentIRs." + i, item.getActionCommand() );
           }
           int state = getExtendedState();
           if ( state != Frame.NORMAL )
           {
             setExtendedState( Frame.NORMAL );
           }
           Rectangle bounds = getBounds();
           properties
               .setProperty( "RMBounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );
 
           properties.save();
 
           if ( generalPanel.getDeviceUpgradeEditor() != null )
           {
             generalPanel.getDeviceUpgradeEditor().dispose();
           }
           if ( devicePanel.getDeviceUpgradeEditor() != null )
           {
             devicePanel.getDeviceUpgradeEditor().dispose();
           }
 
         }
         catch ( Exception exc )
         {
           exc.printStackTrace( System.err );
         }
       }
     } );
 
     Container mainPanel = getContentPane();
 
     mainPanel.add( toolBar, BorderLayout.PAGE_START );
 
     // Set color for text on Progress Bars
     UIManager.put( "ProgressBar.selectionBackground", new javax.swing.plaf.ColorUIResource( Color.BLUE ) );
     UIManager.put( "ProgressBar.selectionForeground", new javax.swing.plaf.ColorUIResource( Color.BLUE ) );
 
     tabbedPane = new JTabbedPane();
     mainPanel.add( tabbedPane, BorderLayout.CENTER );
 
     generalPanel = new GeneralPanel();
     tabbedPane.addTab( "General", generalPanel );
     generalPanel.addPropertyChangeListener( this );
 
     keyMovePanel = new KeyMovePanel();
     tabbedPane.addTab( "Key Moves", keyMovePanel );
     keyMovePanel.addPropertyChangeListener( this );
 
     macroPanel = new MacroPanel();
     tabbedPane.addTab( "Macros", macroPanel );
     macroPanel.addPropertyChangeListener( this );
 
     specialFunctionPanel = new SpecialFunctionPanel();
     tabbedPane.add( "Special Functions", specialFunctionPanel );
     specialFunctionPanel.addPropertyChangeListener( this );
 
     timedMacroPanel = new TimedMacroPanel();
     tabbedPane.add( "Timed Macros", timedMacroPanel );
     timedMacroPanel.addPropertyChangeListener( this );
 
     favScanPanel = new FavScanPanel();
     tabbedPane.addTab( "Fav/Scan", favScanPanel );
     favScanPanel.addPropertyChangeListener( this );
 
     devicePanel = new DeviceUpgradePanel();
     tabbedPane.addTab( "Devices", devicePanel );
     devicePanel.addPropertyChangeListener( this );
 
     protocolPanel = new ProtocolUpgradePanel();
     tabbedPane.addTab( "Protocols", protocolPanel );
     protocolPanel.addPropertyChangeListener( this );
 
     try
     {
       LearnedSignal.getDecodeIR();
       learnedPanel = new LearnedSignalPanel();
       tabbedPane.addTab( "Learned Signals", learnedPanel );
       learnedPanel.addPropertyChangeListener( this );
     }
     catch ( NoClassDefFoundError ncdfe )
     {
       System.err.println( "DecodeIR class not found!" );
     }
     catch ( NoSuchMethodError nsme )
     {
       System.err.println( "DecodeIR class is wrong version!" );
     }
     catch ( UnsatisfiedLinkError ule )
     {
       System.err.println( "DecodeIR JNI interface not found!" );
     }
 
     rawDataPanel = new RawDataPanel();
     tabbedPane.addTab( "Raw Data", rawDataPanel );
     rawDataPanel.addPropertyChangeListener( this );
 
     tabbedPane.addChangeListener( this );
 
     JPanel statusBar = new JPanel();
 
     mainPanel.add( statusBar, BorderLayout.SOUTH );
 
     statusBar.add( new JLabel( "Move/Macro:" ) );
 
     advProgressBar = new JProgressBar();
     advProgressBar.setStringPainted( true );
     advProgressBar.setString( "N/A" );
     statusBar.add( advProgressBar );
 
     statusBar.add( Box.createHorizontalStrut( 5 ) );
     JSeparator sep = new JSeparator( SwingConstants.VERTICAL );
     Dimension d = sep.getPreferredSize();
     d.height = advProgressBar.getPreferredSize().height;
     sep.setPreferredSize( d );
     statusBar.add( sep );
 
     statusBar.add( new JLabel( "Upgrade:" ) );
 
     upgradeProgressBar = new JProgressBar();
     upgradeProgressBar.setStringPainted( true );
     upgradeProgressBar.setString( "N/A" );
 
     upgradeProgressPanel = new JPanel();
     upgradeProgressPanel.setPreferredSize( advProgressBar.getPreferredSize() );
     upgradeProgressPanel.setLayout( new BorderLayout() );
 
     devUpgradeProgressBar = new JProgressBar();
     devUpgradeProgressBar.setStringPainted( true );
     devUpgradeProgressBar.setString( "N/A" );
     devUpgradeProgressBar.setVisible( false );
 
     upgradeProgressPanel.add( upgradeProgressBar, BorderLayout.NORTH );
     upgradeProgressPanel.add( devUpgradeProgressBar, BorderLayout.SOUTH );
 
     statusBar.add( upgradeProgressPanel );
 
     statusBar.add( Box.createHorizontalStrut( 5 ) );
     sep = new JSeparator( SwingConstants.VERTICAL );
     sep.setPreferredSize( d );
     statusBar.add( sep );
 
     statusBar.add( new JLabel( "Learned:" ) );
 
     learnedProgressBar = new JProgressBar();
     learnedProgressBar.setStringPainted( true );
     learnedProgressBar.setString( "N/A" );
     statusBar.add( learnedProgressBar );
 
     String temp = properties.getProperty( "RMBounds" );
     if ( temp != null )
     {
       Rectangle bounds = new Rectangle();
       StringTokenizer st = new StringTokenizer( temp, "," );
       bounds.x = Integer.parseInt( st.nextToken() );
       bounds.y = Integer.parseInt( st.nextToken() );
       bounds.width = Integer.parseInt( st.nextToken() );
       bounds.height = Integer.parseInt( st.nextToken() );
       setBounds( bounds );
     }
     else
     {
       pack();
     }
     currentPanel = generalPanel;
     setVisible( true );
   }
 
   /**
    * Gets the frame attribute of the RemoteMaster class.
    * 
    * @return The frame value
    */
   public static JP1Frame getFrame()
   {
     return frame;
   }
 
   public static ImageIcon createIcon( String imageName )
   {
     String imgLocation = "toolbarButtonGraphics/general/" + imageName + ".gif";
     URLClassLoader sysloader = ( URLClassLoader )ClassLoader.getSystemClassLoader();
 
     java.net.URL imageURL = sysloader.getResource( imgLocation );
 
     if ( imageURL == null )
     {
       System.err.println( "Resource not found: " + imgLocation );
       return null;
     }
     else
     {
       return new ImageIcon( imageURL );
     }
   }
 
   /**
    * Description of the Method.
    */
   private void createMenus( JToolBar toolBar )
   {
     JMenuBar menuBar = new JMenuBar();
     setJMenuBar( menuBar );
 
     JMenu menu = new JMenu( "File" );
     menu.setMnemonic( KeyEvent.VK_F );
     menuBar.add( menu );
 
     JMenu newMenu = new JMenu( "New" );
     newMenu.setMnemonic( KeyEvent.VK_N );
     menu.add( newMenu );
 
     newAction = new RMAction( "Remote Image...", "NEW", createIcon( "RMNew24" ), "Create new file", KeyEvent.VK_R );
     newMenu.add( newAction ).setIcon( null );
     toolBar.add( newAction );
 
     newUpgradeAction = new RMAction( "Device Upgrade", "NEWDEVICE", null, "Create new Device Upgrade", KeyEvent.VK_D );
     newMenu.add( newUpgradeAction );
 
     openAction = new RMAction( "Open...", "OPEN", createIcon( "RMOpen24" ), "Open a file", KeyEvent.VK_O );
     menu.add( openAction ).setIcon( null );
     toolBar.add( openAction );
 
     saveAction = new RMAction( "Save", "SAVE", createIcon( "Save24" ), "Save to file", KeyEvent.VK_S );
     saveAction.setEnabled( false );
     menu.add( saveAction ).setIcon( null );
     toolBar.add( saveAction );
 
     saveAsAction = new RMAction( "Save as...", "SAVEAS", createIcon( "SaveAs24" ), "Save to a different file",
         KeyEvent.VK_A );
     saveAsAction.setEnabled( false );
     JMenuItem menuItem = menu.add( saveAsAction );
     menuItem.setDisplayedMnemonicIndex( 5 );
     menuItem.setIcon( null );
     toolBar.add( saveAsAction );
 
     // revertItem = new JMenuItem( "Revert to saved" );
     // revertItem.setMnemonic( KeyEvent.VK_R );
     // revertItem.addActionListener( this );
     // menu.add( revertItem );
 
     menu.addSeparator();
     toolBar.addSeparator();
 
     // exportIRItem = new JMenuItem( "Export as IR...", KeyEvent.VK_I );
     // exportIRItem.setEnabled( false );
     // exportIRItem.addActionListener( this );
     // menu.add( exportIRItem );
     // menu.addSeparator();
 
     recentFiles = new JMenu( "Recent" );
     menu.add( recentFiles );
     recentFiles.setEnabled( false );
     for ( int i = 0; i < 10; i++ )
     {
       String propName = "RecentIRs." + i;
       String temp = properties.getProperty( propName );
       if ( temp == null )
       {
         break;
       }
       properties.remove( propName );
       File f = new File( temp );
       if ( f.canRead() )
       {
         JMenuItem item = new JMenuItem( temp );
         item.setActionCommand( temp );
         item.addActionListener( this );
         recentFiles.add( item );
       }
     }
     if ( recentFiles.getItemCount() > 0 )
     {
       recentFiles.setEnabled( true );
     }
     menu.addSeparator();
 
     exitItem = new JMenuItem( "Exit", KeyEvent.VK_X );
     exitItem.addActionListener( this );
     menu.add( exitItem );
 
     menu = new JMenu( "Remote" );
     menu.setMnemonic( KeyEvent.VK_R );
     menuBar.add( menu );
 
     File userDir = new File( System.getProperty( "user.dir" ) );
     try
     {
       interfaces.add( new JP1Parallel( userDir ) );
     }
     catch ( LinkageError le )
     {
       System.err.println( "Unable to create JP1Parallel object: " + le.getMessage() );
     }
 
     try
     {
       interfaces.add( new JP12Serial( userDir ) );
     }
     catch ( LinkageError le )
     {
       System.err.println( "Unable to create JP12Serial object: " + le.getMessage() );
     }
 
     try
     {
       interfaces.add( new JP1USB( userDir ) );
     }
     catch ( LinkageError le )
     {
       System.err.println( "Unable to create JP1USB object: " + le.getMessage() );
     }
 
     ActionListener interfaceListener = new ActionListener()
     {
       public void actionPerformed( ActionEvent event )
       {
         String command = event.getActionCommand();
         if ( command.equals( "autodetect" ) )
         {
           properties.remove( "Interface" );
           properties.remove( "Port" );
           return;
         }
 
         for ( IO io : interfaces )
         {
           if ( io.getInterfaceName().equals( command ) )
           {
             String defaultPort = null;
             if ( command.equals( properties.getProperty( "Interface" ) ) )
             {
               defaultPort = properties.getProperty( "Port" );
             }
 
             String[] availablePorts = io.getPortNames();
 
             PortDialog d = new PortDialog( RemoteMaster.this, availablePorts, defaultPort );
             d.setVisible( true );
             if ( d.getUserAction() == JOptionPane.OK_OPTION )
             {
               String port = d.getPort();
               properties.setProperty( "Interface", io.getInterfaceName() );
               if ( port.equals( PortDialog.AUTODETECT ) )
               {
                 properties.remove( "Port" );
               }
               else
               {
                 properties.setProperty( "Port", port );
               }
             }
 
             break;
           }
         }
       }
     };
 
     if ( !interfaces.isEmpty() )
     {
       JMenu subMenu = new JMenu( "Interface" );
       menu.add( subMenu );
       subMenu.setMnemonic( KeyEvent.VK_I );
       ButtonGroup group = new ButtonGroup();
       String preferredInterface = properties.getProperty( "Interface" );
       JRadioButtonMenuItem item = new JRadioButtonMenuItem( "Auto-detect" );
       item.setActionCommand( "autodetect" );
       item.setSelected( preferredInterface == null );
       subMenu.add( item );
       group.add( item );
       item.setMnemonic( KeyEvent.VK_A );
       item.addActionListener( interfaceListener );
 
       ListIterator< IO > it = interfaces.listIterator();
       while ( it.hasNext() )
       {
         IO io = it.next();
         try
         {
           String ioName = io.getInterfaceName();
           item = new JRadioButtonMenuItem( ioName + "..." );
           item.setActionCommand( ioName );
           item.setSelected( ioName.equals( preferredInterface ) );
           subMenu.add( item );
           group.add( item );
           item.addActionListener( interfaceListener );
         }
         catch ( UnsatisfiedLinkError ule )
         {
           it.remove();
           String className = io.getClass().getName();
           int dot = className.lastIndexOf( '.' );
           if ( dot != -1 )
           {
             className = className.substring( dot + 1 );
           }
           JOptionPane.showMessageDialog( this, "An incompatible version of the " + className
               + " driver was detected.  You will not be able to download or upload using that driver.",
               "Incompatible Driver", JOptionPane.ERROR_MESSAGE );
           ule.printStackTrace( System.err );
         }
       }
     }
 
     downloadAction = new RMAction( "Download from Remote", "DOWNLOAD", createIcon( "Import24" ),
         "Download from the attached remote", KeyEvent.VK_D );
     downloadAction.setEnabled( !interfaces.isEmpty() );
     menu.add( downloadAction ).setIcon( null );
     toolBar.add( downloadAction );
 
     uploadAction = new RMAction( "Upload to Remote", "UPLOAD", createIcon( "Export24" ),
         "Upload to the attached remote", KeyEvent.VK_U );
     uploadAction.setEnabled( false );
     menu.add( uploadAction ).setIcon( null );
     toolBar.add( uploadAction );
 
     toolBar.addSeparator();
     openRdfAction = new RMAction( "Open RDF...", "OPENRDF", createIcon( "RMOpenRDF24" ), "Open RDF to view or edit",
         null );
     openRdfAction.setEnabled( false );
     toolBar.add( openRdfAction );
 
     codesAction = new RMAction( "Code Selector...", "OPENCODES", createIcon( "RMCodes24" ), "Open Code Selector", null );
     codesAction.setEnabled( false );
     toolBar.add( codesAction );
 
     uploadWavItem = new JMenuItem( "Create WAV", KeyEvent.VK_W );
     uploadWavItem.setEnabled( false );
     uploadWavItem.addActionListener( this );
     menu.add( uploadWavItem );
 
     menu.addSeparator();
     downloadRawItem = new JMenuItem( "Raw download", KeyEvent.VK_R );
     downloadRawItem.setEnabled( true );
     downloadRawItem.addActionListener( this );
     menu.add( downloadRawItem );
 
     menu.addSeparator();
     verifyUploadItem = new JCheckBoxMenuItem( "Verify after upload" );
     verifyUploadItem.setMnemonic( KeyEvent.VK_V );
     verifyUploadItem.setSelected( Boolean.parseBoolean( properties.getProperty( "verifyUpload", "true" ) ) );
     verifyUploadItem.addActionListener( this );
     menu.add( verifyUploadItem );
 
     menu = new JMenu( "Options" );
     menu.setMnemonic( KeyEvent.VK_O );
     menuBar.add( menu );
 
     JMenu subMenu = new JMenu( "Look and Feel" );
     subMenu.setMnemonic( KeyEvent.VK_L );
     menu.add( subMenu );
 
     ActionListener al = new ActionListener()
     {
       public void actionPerformed( ActionEvent e )
       {
         try
         {
           JRadioButtonMenuItem item = ( JRadioButtonMenuItem )e.getSource();
           String lf = item.getActionCommand();
           UIManager.setLookAndFeel( lf );
           SwingUtilities.updateComponentTreeUI( RemoteMaster.this );
           RemoteMaster.this.pack();
           properties.setProperty( "LookAndFeel", lf );
         }
         catch ( Exception x )
         {
           x.printStackTrace( System.err );
         }
       }
     };
 
     ButtonGroup group = new ButtonGroup();
     String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
     UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
     lookAndFeelItems = new JRadioButtonMenuItem[ info.length ];
     for ( int i = 0; i < info.length; i++ )
     {
       JRadioButtonMenuItem item = new JRadioButtonMenuItem( info[ i ].getName() );
       lookAndFeelItems[ i ] = item;
       item.setMnemonic( item.getText().charAt( 0 ) );
       item.setActionCommand( info[ i ].getClassName() );
       group.add( item );
       subMenu.add( item );
       if ( item.getActionCommand().equals( lookAndFeel ) )
       {
         item.setSelected( true );
       }
       item.addActionListener( al );
     }
 
     menu = new JMenu( "Help" );
     menu.setMnemonic( KeyEvent.VK_H );
     menuBar.add( menu );
 
     if ( desktop != null )
     {
       readmeItem = new JMenuItem( "Readme", KeyEvent.VK_R );
       readmeItem.addActionListener( this );
       menu.add( readmeItem );
 
       tutorialItem = new JMenuItem( "Tutorial", KeyEvent.VK_T );
       tutorialItem.addActionListener( this );
       menu.add( tutorialItem );
 
       learnedSignalItem = new JMenuItem( "Interpreting Decoded IR Signals", KeyEvent.VK_I );
       learnedSignalItem.addActionListener( this );
       menu.add( learnedSignalItem );
 
       menu.addSeparator();
 
       homePageItem = new JMenuItem( "Home Page", KeyEvent.VK_H );
       homePageItem.addActionListener( this );
       menu.add( homePageItem );
 
       wikiItem = new JMenuItem( "Wiki", KeyEvent.VK_W );
       wikiItem.addActionListener( this );
       menu.add( wikiItem );
 
       forumItem = new JMenuItem( "Forums", KeyEvent.VK_F );
       forumItem.addActionListener( this );
       menu.add( forumItem );
 
       menu.addSeparator();
     }
 
     aboutItem = new JMenuItem( "About...", KeyEvent.VK_A );
     aboutItem.addActionListener( this );
     menu.add( aboutItem );
   }
 
   /**
    * Gets the fileChooser attribute of the RemoteMaster object.
    * 
    * @return The fileChooser value
    */
   public RMFileChooser getFileChooser()
   {
     RMFileChooser chooser = new RMFileChooser( dir );
     EndingFileFilter irFilter = new EndingFileFilter( "All supported files", allEndings );
     chooser.addChoosableFileFilter( irFilter );
     chooser.addChoosableFileFilter( new EndingFileFilter( "RM IR files (*.rmir)", rmirEndings ) );
     chooser.addChoosableFileFilter( new EndingFileFilter( "IR files (*.ir)", irEndings ) );
     chooser.addChoosableFileFilter( new EndingFileFilter( "RM Device Upgrades (*.rmdu)", rmduEndings ) );
     chooser.addChoosableFileFilter( new EndingFileFilter( "KM Device Upgrades (*.txt)", txtEndings ) );
     chooser.addChoosableFileFilter( new EndingFileFilter( "Sling Learned Signals (*.xml)", slingEndings ) );
     chooser.setFileFilter( irFilter );
 
     return chooser;
   }
 
   /**
    * Description of the Method.
    * 
    * @return Description of the Return Value
    * @throws Exception
    *           the exception
    * @exception Exception
    *              Description of the Exception
    */
   public File openFile() throws Exception
   {
     return openFile( null );
   }
 
   /**
    * Description of the Method.
    * 
    * @param file
    *          the file
    * @return Description of the Return Value
    * @throws Exception
    *           the exception
    * @exception Exception
    *              Description of the Exception
    */
   public File openFile( File file ) throws Exception
   {
     while ( file == null )
     {
       RMFileChooser chooser = getFileChooser();
       int returnVal = chooser.showOpenDialog( this );
       if ( returnVal == RMFileChooser.APPROVE_OPTION )
       {
         file = chooser.getSelectedFile();
 
         if ( !file.exists() )
         {
           JOptionPane.showMessageDialog( this, file.getName() + " doesn't exist.", "File doesn't exist.",
               JOptionPane.ERROR_MESSAGE );
         }
         else if ( file.isDirectory() )
         {
           JOptionPane.showMessageDialog( this, file.getName() + " is a directory.", "File doesn't exist.",
               JOptionPane.ERROR_MESSAGE );
         }
       }
       else
       {
         return null;
       }
     }
 
     System.err.println( "Opening " + file.getCanonicalPath() + ", last modified "
         + DateFormat.getInstance().format( new Date( file.lastModified() ) ) );
 
     String ext = file.getName().toLowerCase();
     int dot = ext.lastIndexOf( '.' );
     ext = ext.substring( dot );
 
     dir = file.getParentFile();
     properties.setProperty( "IRPath", dir );
 
     if ( ext.equals( ".rmdu" ) || ext.equals( ".rmir" ) )
     {
       updateRecentFiles( file );
 
     }
 
     if ( ext.equals( ".rmdu" ) || ext.equals( ".txt" ) )
     {
       KeyMapMaster km = new KeyMapMaster( properties );
       km.loadUpgrade( file );
       return null;
     }
 
     if ( ext.equals( ".xml" ) )
     {
       List< Remote > remotes = RemoteManager.getRemoteManager().findRemoteBySignature( "XMLLEARN" );
       if ( remotes.isEmpty() )
       {
         JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
             "The RDF for XML Slingbox Learns was not found.  Please place it in the RDF folder and try again.",
             "Missing RDF File", JOptionPane.ERROR_MESSAGE );
         return null;
       }
       Remote remote = remotes.get( 0 );
       remote.load();
       remoteConfig = new RemoteConfiguration( remote );
       remoteConfig.initializeSetup();
       remoteConfig.updateImage();
       remoteConfig.setDateIndicator();
       remoteConfig.setSavedData();
       SlingLearnParser.parse( file, remoteConfig );
       remoteConfig.updateImage();
       update();
       saveAction.setEnabled( false );
       saveAsAction.setEnabled( true );
       uploadAction.setEnabled( !interfaces.isEmpty() );
       openRdfAction.setEnabled( true );
       return null;
     }
 
     if ( ext.equals( ".rmir" ) )
     {
       saveAction.setEnabled( true );
       saveAsAction.setEnabled( true );
       openRdfAction.setEnabled( true );
     }
     else
     // ext.equals( ".ir" )
     {
       saveAction.setEnabled( false );
       saveAsAction.setEnabled( true );
       openRdfAction.setEnabled( true );
     }
     // exportIRItem.setEnabled( true );
     uploadAction.setEnabled( !interfaces.isEmpty() );
     remoteConfig = new RemoteConfiguration( file );
     update();
     setTitleFile( file );
     this.file = file;
     return file;
   }
 
   /**
    * Description of the Method.
    * 
    * @param file
    *          the file
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    * @exception IOException
    *              Description of the Exception
    */
   private void updateRecentFiles( File file ) throws IOException
   {
     JMenuItem item = null;
     String path = file.getCanonicalPath();
     for ( int i = 0; i < recentFiles.getItemCount(); ++i )
     {
       File temp = new File( recentFiles.getItem( i ).getText() );
 
       if ( temp.getCanonicalPath().equals( path ) )
       {
         item = recentFiles.getItem( i );
         recentFiles.remove( i );
         break;
       }
     }
     if ( item == null )
     {
       item = new JMenuItem( path );
       item.setActionCommand( path );
       item.addActionListener( this );
     }
     recentFiles.insert( item, 0 );
     while ( recentFiles.getItemCount() > 10 )
     {
       recentFiles.remove( 10 );
     }
     recentFiles.setEnabled( true );
     dir = file.getParentFile();
     properties.setProperty( "IRPath", dir );
   }
 
   /**
    * Description of the Method.
    * 
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    * @exception IOException
    *              Description of the Exception
    */
   public void saveAs() throws IOException
   {
     RMFileChooser chooser = getFileChooser();
     if ( file != null )
     {
       String name = file.getName().toLowerCase();
       if ( name.endsWith( ".ir" ) || name.endsWith( ".txt" ) )
       {
         int dot = name.lastIndexOf( '.' );
         name = name.substring( 0, dot ) + ".rmir";
         file = new File( name );
       }
       chooser.setSelectedFile( file );
     }
     int returnVal = chooser.showSaveDialog( this );
     if ( returnVal == RMFileChooser.APPROVE_OPTION )
     {
       String name = chooser.getSelectedFile().getAbsolutePath();
       if ( !name.toLowerCase().endsWith( ".rmir" ) )
       {
         name = name + ".rmir";
       }
       File newFile = new File( name );
       int rc = JOptionPane.YES_OPTION;
       if ( newFile.exists() )
       {
         rc = JOptionPane.showConfirmDialog( this, newFile.getName() + " already exists.  Do you want to replace it?",
             "Replace existing file?", JOptionPane.YES_NO_OPTION );
       }
 
       if ( rc != JOptionPane.YES_OPTION )
       {
         return;
       }
 
       dir = newFile.getParentFile();
       properties.setProperty( "IRPath", dir );
 
       file = newFile;
       remoteConfig.save( file );
       setTitleFile( file );
       updateRecentFiles( file );
       saveAction.setEnabled( true );
       // exportIRItem.setEnabled( true );
       uploadAction.setEnabled( !interfaces.isEmpty() );
     }
   }
 
   /**
    * Description of the Method.
    * 
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    * @exception IOException
    *              Description of the Exception
    */
   public void exportAsIR() throws IOException
   {
     RMFileChooser chooser = getFileChooser();
     String name = file.getName().toLowerCase();
     if ( !name.endsWith( ".ir" ) )
     {
       int dot = name.lastIndexOf( '.' );
       name = name.substring( 0, dot ) + ".IR";
       file = new File( name );
     }
     chooser.setSelectedFile( file );
     int returnVal = chooser.showSaveDialog( this );
     if ( returnVal == RMFileChooser.APPROVE_OPTION )
     {
       name = chooser.getSelectedFile().getAbsolutePath();
       if ( !name.toLowerCase().endsWith( ".ir" ) )
       {
         name = name + ".IR";
       }
       File newFile = new File( name );
       int rc = JOptionPane.YES_OPTION;
       if ( newFile.exists() )
       {
         rc = JOptionPane.showConfirmDialog( this, newFile.getName() + " already exists.  Do you want to replace it?",
             "Replace existing file?", JOptionPane.YES_NO_OPTION );
       }
 
       if ( rc != JOptionPane.YES_OPTION )
       {
         return;
       }
 
       file = newFile;
       remoteConfig.exportIR( file );
     }
   }
 
   /**
    * Sets the titleFile attribute of the RemoteMaster object.
    * 
    * @param file
    *          the file
    */
   private void setTitleFile( File file )
   {
     if ( file == null )
     {
       setTitle( "RM IR" );
     }
     else
     {
       setTitle( "RM IR: " + file.getName() + " - " + remoteConfig.getRemote().getName() );
     }
   }
 
   /**
    * Gets the open interface.
    * 
    * @return the open interface
    */
   public IO getOpenInterface()
   {
     String interfaceName = properties.getProperty( "Interface" );
     String portName = properties.getProperty( "Port" );
     if ( interfaceName != null )
     {
       for ( IO temp : interfaces )
       {
         if ( temp.getInterfaceName().equals( interfaceName ) )
         {
           if ( temp.openRemote( portName ) != null )
           {
             return temp;
           }
         }
       }
     }
     else
     {
       for ( IO temp : interfaces )
       {
         portName = temp.openRemote();
         if ( portName != null )
         {
           return temp;
         }
       }
     }
     return null;
   }
 
   /**
    * Description of the Method.
    * 
    * @param e
    *          the e
    */
   public void actionPerformed( ActionEvent e )
   {
     try
     {
       Object source = e.getSource();
       if ( source == exportIRItem )
       {
         exportAsIR();
       }
       else if ( source == exitItem )
       {
         dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
       }
       else if ( source == downloadRawItem )
       {
         RawDataDialog dlg = new RawDataDialog( this );
         dlg.setVisible( true );
       }
       else if ( source == verifyUploadItem )
       {
         properties.setProperty( "verifyUpload", Boolean.toString( verifyUploadItem.isSelected() ) );
       }
       else if ( source == aboutItem )
       {
         StringBuilder sb = new StringBuilder( 1000 );
         sb.append( "<html><b>RemoteMaster " );
         sb.append( version );
         sb.append( "</b>" );
         sb.append( "<p>Written primarily by <i>Greg Bush</i> (now accepting donations at " );
         sb
             .append( "<a href=\"http://sourceforge.net/donate/index.php?user_id=735638\">http://sourceforge.net/donate/index.php?user_id=735638</a>)</p>" );
         sb.append( "<p>Other contributors include:<blockquote>" );
         sb
             .append( "Graham&nbsp;Dixon, John&nbsp;S&nbsp;Fine, Nils&nbsp;Ekberg, Jon&nbsp;Armstrong, Robert&nbsp;Crowe, " );
         sb.append( "Mark&nbsp;Pauker, Mark&nbsp;Pierson, Mike&nbsp;England</blockquote></p>" );
 
         sb.append( "<p>RDFs loaded from <b>" );
         sb.append( properties.getProperty( "RDFPath" ) );
         sb.append( "</b></p>" );
 
         sb.append( "</p><p>Images and Maps loaded from <b>" );
         sb.append( properties.getProperty( "ImagePath" ) );
         sb.append( "</b></p>" );
         try
         {
           String v = LearnedSignal.getDecodeIR().getVersion();
           sb.append( "<p>DecodeIR version " );
           sb.append( v );
           sb.append( "</p>" );
         }
         catch ( LinkageError le )
         {
           sb.append( "<p><b>DecodeIR is not available!</b></p>" );
         }
 
         if ( !interfaces.isEmpty() )
         {
           sb.append( "<p>Interfaces:<ul>" );
           for ( IO io : interfaces )
           {
             sb.append( "<li>" );
             sb.append( io.getInterfaceName() );
             sb.append( " version " );
             sb.append( io.getInterfaceVersion() );
             sb.append( "</li>" );
           }
           sb.append( "</ul></p>" );
         }
 
         String[] propertyNames =
         {
             "java.version", "java.vendor", "os.name", "os.arch"
         };
 
         sb.append( "<p>System Properties:<ul>" );
         for ( String name : propertyNames )
         {
           sb.append( "<li>" );
           sb.append( name );
           sb.append( " = \"" );
           sb.append( System.getProperty( name ) );
           sb.append( "\"</li>" );
         }
         sb.append( "</ul>" );
 
         sb.append( "<p>Libraries loaded from " );
         sb.append( LibraryLoader.getLibraryFolder() );
         sb.append( "</p></html>" );
 
         JEditorPane pane = new JEditorPane( "text/html", sb.toString() );
         pane.addHyperlinkListener( this );
         pane.setEditable( false );
         pane.setBackground( getContentPane().getBackground() );
         new TextPopupMenu( pane );
         JScrollPane scroll = new JScrollPane( pane );
         Dimension d = pane.getPreferredSize();
         d.height = d.height * 5 / 4;
         d.width = d.width * 2 / 3;
         scroll.setPreferredSize( d );
 
         JOptionPane.showMessageDialog( this, scroll, "About Java IR", JOptionPane.INFORMATION_MESSAGE, null );
       }
       else if ( source == readmeItem )
       {
         File readme = new File( "Readme.html" );
         desktop.browse( readme.toURI() );
       }
       else if ( source == tutorialItem )
       {
         File file = new File( "tutorial/tutorial.html" );
         desktop.browse( file.toURI() );
       }
       else if ( source == learnedSignalItem )
       {
         File file = new File( "DecodeIR.html" );
         desktop.browse( file.toURI() );
       }
       else if ( source == homePageItem )
       {
         URL url = new URL( "http://controlremote.sourceforge.net/" );
         desktop.browse( url.toURI() );
       }
       else if ( source == wikiItem )
       {
         URL url = new URL( "https://sourceforge.net/apps/mediawiki/controlremote/index.php?title=Main_Page" );
         desktop.browse( url.toURI() );
       }
       else if ( source == forumItem )
       {
         URL url = new URL( "http://www.hifi-remote.com/forums/" );
         desktop.browse( url.toURI() );
       }
       else
       {
         JMenuItem item = ( JMenuItem )source;
         File file = new File( item.getActionCommand() );
         recentFiles.remove( item );
         if ( file.canRead() )
         {
           openFile( file );
         }
       }
     }
     catch ( Exception ex )
     {
       ex.printStackTrace( System.err );
     }
   }
 
   /**
    * Description of the Method.
    */
   public void update()
   {
     if ( remoteConfig != null )
     {
       setTitle( "RM IR - " + remoteConfig.getRemote().getName() );
     }
     else
     {
       setTitle( "RM IR" );
     }
 
     Remote remote = remoteConfig.getRemote();
 
     generalPanel.set( remoteConfig );
     keyMovePanel.set( remoteConfig );
     macroPanel.set( remoteConfig );
 
     int index = getTabIndex( specialFunctionPanel );
     if ( remote.getSpecialProtocols().isEmpty() )
     {
       if ( index >= 0 )
       {
         tabbedPane.remove( index );
       }
     }
     else if ( index < 0 )
     {
       tabbedPane.insertTab( "Special Functions", null, specialFunctionPanel, null, getTabIndex( macroPanel ) + 1 );
     }
 
     index = getTabIndex( timedMacroPanel );
     if ( remote.hasTimedMacroSupport() )
     {
       if ( index < 0 )
       {
         index = getTabIndex( specialFunctionPanel );
         if ( index < 0 )
         {
           index = getTabIndex( macroPanel );
         }
         tabbedPane.insertTab( "Timed Macros", null, timedMacroPanel, null, index + 1 );
       }
     }
     else if ( index > 0 )
     {
       tabbedPane.remove( index );
     }
 
     index = getTabIndex( favScanPanel );
     if ( remote.hasFavKey() )
     {
       if ( index < 0 )
       {
         tabbedPane.insertTab( "Fav/Scan", null, favScanPanel, null, getTabIndex( devicePanel ) );
       }
     }
     else if ( index >= 0 )
     {
       tabbedPane.remove( index );
     }
 
     specialFunctionPanel.set( remoteConfig );
     timedMacroPanel.set( remoteConfig );
     favScanPanel.set( remoteConfig );
 
     devicePanel.set( remoteConfig );
     protocolPanel.set( remoteConfig );
 
     if ( learnedPanel != null )
     {
       learnedPanel.set( remoteConfig );
     }
 
     codesAction.setEnabled( remote.getSetupCodes().size() > 0 );
     if ( codeSelectorDialog != null )
     {
       if ( codeSelectorDialog.isDisplayable() )
       {
         codeSelectorDialog.dispose();
       }
       codeSelectorDialog = null;
     }
 
     if ( rdfViewer != null )
     {
       if ( rdfViewer.isDisplayable() )
       {
         rdfViewer.dispose();
       }
       rdfViewer = null;
     }
 
     updateUsage();
 
     rawDataPanel.set( remoteConfig );
   }
 
   private boolean updateUsage( JProgressBar bar, AddressRange range, int used )
   {
     if ( range != null )
     {
       int available = range.getSize();
       bar.setMinimum( 0 );
       bar.setMaximum( available );
       bar.setValue( used );
       bar.setString( Integer.toString( available - used ) + " free" );
 
       return available >= used;
     }
     else
     {
       bar.setMinimum( 0 );
       bar.setMaximum( 0 );
       bar.setValue( 0 );
       bar.setString( "N/A" );
 
       return true;
     }
   }
 
   private void updateUsage()
   {
     Remote remote = remoteConfig.getRemote();
     Dimension d = advProgressBar.getPreferredSize();
     Font font = advProgressBar.getFont();
     if ( remote.getDeviceUpgradeAddress() == null )
     {
       upgradeProgressBar.setVisible( false );
       upgradeProgressBar.setPreferredSize( d );
       upgradeProgressBar.setFont( font );
       upgradeProgressBar.setVisible( true );
       devUpgradeProgressBar.setVisible( false );
     }
     else
     {
       d.height /= 2;
       Font font2 = font.deriveFont( ( float )font.getSize() * 0.75f );
       upgradeProgressBar.setVisible( false );
       upgradeProgressBar.setPreferredSize( d );
       upgradeProgressBar.setFont( font2 );
       upgradeProgressBar.setVisible( true );
       devUpgradeProgressBar.setVisible( false );
       devUpgradeProgressBar.setPreferredSize( d );
       devUpgradeProgressBar.setFont( font2 );
       devUpgradeProgressBar.setVisible( true );
     }
 
     if ( !updateUsage( advProgressBar, remote.getAdvancedCodeAddress(), remoteConfig.getAdvancedCodeBytesNeeded() ) )
     {
       JOptionPane
           .showMessageDialog(
               this,
               "The defined advanced codes (keymoves, macros, special functions etc.) use more space than is available.  Please remove some.",
               "Available Space Exceeded", JOptionPane.ERROR_MESSAGE );
     }
     if ( !updateUsage( timedMacroPanel.timedMacroProgressBar, remote.getTimedMacroAddress(), remoteConfig
         .getTimedMacroBytesNeeded() ) )
     {
       JOptionPane.showMessageDialog( this,
           "The defined timed macros use more space than is available.  Please remove some.",
           "Available Space Exceeded", JOptionPane.ERROR_MESSAGE );
     }
     if ( !updateUsage( upgradeProgressBar, remote.getUpgradeAddress(), remoteConfig.getUpgradeCodeBytesNeeded() ) )
     {
       JOptionPane.showMessageDialog( this,
           "The defined device upgrades use more space than is available. Please remove some.",
           "Available Space Exceeded", JOptionPane.ERROR_MESSAGE );
     }
     if ( !updateUsage( devUpgradeProgressBar, remote.getDeviceUpgradeAddress(), remoteConfig
         .getDevUpgradeCodeBytesNeeded() ) )
     {
       JOptionPane.showMessageDialog( this,
           "The defined button-dependent device upgrades use more space than is available. Please remove some.",
           "Available Space Exceeded", JOptionPane.ERROR_MESSAGE );
     }
     if ( !updateUsage( learnedProgressBar, remote.getLearnedAddress(), remoteConfig.getLearnedSignalBytesNeeded() ) )
     {
       JOptionPane.showMessageDialog( this,
           "The defined learned signals use more space than is available.  Please remove some.",
           "Available Space Exceeded", JOptionPane.ERROR_MESSAGE );
     }
   }
 
   /**
    * Description of the Method.
    * 
    * @param event
    *          the event
    */
   public void propertyChange( PropertyChangeEvent event )
   {
     // No need to check unassigned upgrades as keymoves are saved in .rmir file
     // remoteConfig.checkUnassignedUpgrades();
    remoteConfig.updateImage();
    updateUsage();
    hasInvalidCodes = generalPanel.setWarning();
     if ( currentPanel == keyMovePanel )
     {
       ( ( KeyMoveTableModel )keyMovePanel.getModel() ).resetKeyMoves();
     }
   }
 
   private boolean allowSave( Remote.SetupValidation setupValidation )
   {
     if ( !hasInvalidCodes )
     {
       return true;
     }
     String title = "Setup codes";
     if ( setupValidation == Remote.SetupValidation.WARN )
     {
       String message = "The current setup contains invalid device codes.\n" + "Are you sure you wish to continue?";
       return JOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION;
     }
     else if ( setupValidation == Remote.SetupValidation.ENFORCE )
     {
       String message = "The current setup contains invalid device codes\n"
           + "which would cause this remote to malfunction.\n" + "Please correct these codes and try again.";
       JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
     }
     return false;
   }
 
   /**
    * Description of the Method.
    * 
    * @param args
    *          the args
    */
   private static void createAndShowGUI( ArrayList< String > args )
   {
     try
     {
       File workDir = new File( System.getProperty( "user.dir" ) );
       File propertiesFile = null;
       File fileToOpen = null;
       boolean launchRM = true;
       for ( int i = 0; i < args.size(); ++i )
       {
         String parm = args.get( i );
         if ( parm.equalsIgnoreCase( "-ir" ) )
         {
           launchRM = true;
         }
         else if ( parm.equalsIgnoreCase( "-rm" ) )
         {
           launchRM = false;
         }
         else if ( "-home".startsWith( parm ) )
         {
           String dirName = args.get( ++i );
           System.err.println( parm + " applies to \"" + dirName + '"' );
           workDir = new File( dirName );
           System.setProperty( "user.dir", workDir.getCanonicalPath() );
         }
         else if ( "-properties".startsWith( parm ) )
         {
           String fileName = args.get( ++i );
           System.err.println( "Properties file name is \"" + fileName + '"' );
           propertiesFile = new File( fileName );
         }
         else
         {
           fileToOpen = new File( parm );
         }
       }
 
       try
       {
         System.setErr( new PrintStream( new FileOutputStream( new File( workDir, "rmaster.err" ) ) ) );
       }
       catch ( Exception e )
       {
         e.printStackTrace( System.err );
       }
 
       System.err.println( "RemoteMaster " + RemoteMaster.version );
       String[] propertyNames =
       {
           "java.version", "java.vendor", "os.name", "os.arch"
       };
 
       System.err.println( "System Properties:" );
       for ( String name : propertyNames )
       {
         System.err.println( "   " + name + " = " + System.getProperty( name ) );
       }
 
       ClassPathAdder.addFile( workDir );
 
       FilenameFilter filter = new FilenameFilter()
       {
         public boolean accept( File dir, String name )
         {
           String temp = name.toLowerCase();
           return temp.endsWith( ".jar" ) && !temp.endsWith( "remotemaster.jar" ) && !temp.endsWith( "setup.jar" );
         }
       };
 
       File[] jarFiles = workDir.listFiles( filter );
       ClassPathAdder.addFiles( jarFiles );
 
       if ( propertiesFile == null )
       {
         File dir = workDir;
         propertiesFile = new File( dir, "RemoteMaster.properties" );
         if ( !propertiesFile.exists() )
         {
           if ( System.getProperty( "os.name" ).startsWith( "Windows" )
               && Float.parseFloat( System.getProperty( "os.version" ) ) >= 6.0f )
           {
             String baseFolderName = System.getenv( "APPDATA" );
             if ( baseFolderName == null || "".equals( baseFolderName ) )
             {
               baseFolderName = System.getProperty( "user.home" );
             }
 
             dir = new File( baseFolderName, "RemoteMaster" );
             if ( !dir.exists() )
             {
               dir.mkdirs();
             }
           }
 
           propertiesFile = new File( dir, "RemoteMaster.properties" );
         }
       }
       PropertyFile properties = new PropertyFile( propertiesFile );
 
       String lookAndFeel = properties.getProperty( "LookAndFeel", UIManager.getSystemLookAndFeelClassName() );
       try
       {
         UIManager.setLookAndFeel( lookAndFeel );
       }
       catch ( UnsupportedLookAndFeelException ulafe )
       {
         ulafe.printStackTrace( System.err );
       }
 
       RemoteManager.getRemoteManager().loadRemotes( properties );
 
       ProtocolManager.getProtocolManager().load( new File( workDir, "protocols.ini" ) );
 
       DigitMaps.load( new File( workDir, "digitmaps.bin" ) );
 
       if ( launchRM )
       {
         RemoteMaster rm = new RemoteMaster( workDir, properties );
         if ( fileToOpen != null )
         {
           rm.openFile( fileToOpen );
         }
         frame = rm;
       }
       else
       {
         KeyMapMaster km = new KeyMapMaster( properties );
         km.loadUpgrade( fileToOpen );
         frame = km;
       }
     }
     catch ( Exception e )
     {
       System.err.println( "Caught exception in RemoteMaster.main()!" );
       e.printStackTrace( System.err );
       System.err.flush();
       System.exit( 0 );
     }
     System.err.flush();
   }
 
   /**
    * The main program for the RemoteMaster class.
    * 
    * @param args
    *          the args
    */
   public static void main( String[] args )
   {
     JDialog.setDefaultLookAndFeelDecorated( true );
     JFrame.setDefaultLookAndFeelDecorated( true );
     Toolkit.getDefaultToolkit().setDynamicLayout( true );
 
     for ( String arg : args )
     {
       if ( "-version".startsWith( arg ) )
       {
         System.out.println( version );
         return;
       }
       else
       {
         parms.add( arg );
       }
     }
     javax.swing.SwingUtilities.invokeLater( new Runnable()
     {
       public void run()
       {
         createAndShowGUI( parms );
       }
     } );
   }
 
   /** The parms. */
   private static ArrayList< String > parms = new ArrayList< String >();
 
   /** The Constant rmirEndings. */
   private final static String[] rmirEndings =
   {
     ".rmir"
   };
 
   /** The Constant rmduEndings. */
   private final static String[] rmduEndings =
   {
     ".rmdu"
   };
 
   /** The Constant irEndings. */
   private final static String[] irEndings =
   {
     ".ir"
   };
 
   /** The Constant txtEndings. */
   private final static String[] txtEndings =
   {
     ".txt"
   };
 
   private final static String[] slingEndings =
   {
     ".xml"
   };
 
   private final static String[] allEndings =
   {
       ".rmir", ".ir", ".rmdu", ".txt", ".xml"
   };
 
   @Override
   public void stateChanged( ChangeEvent event )
   {
     RMPanel newPanel = ( RMPanel )tabbedPane.getSelectedComponent();
     if ( newPanel != currentPanel )
     {
       newPanel.set( remoteConfig );
       currentPanel = newPanel;
     }
     if ( codeSelectorDialog != null )
     {
       codeSelectorDialog.enableAssign( currentPanel == generalPanel );
     }
   }
 
   private int getTabIndex( Component c )
   {
     for ( int i = 0; i < tabbedPane.getTabCount(); i++ )
     {
       if ( tabbedPane.getComponentAt( i ).equals( c ) )
       {
         return i;
       }
     }
     return -1;
   }
 
   public RemoteConfiguration getRemoteConfiguration()
   {
     return remoteConfig;
   }
 
   public GeneralPanel getGeneralPanel()
   {
     return generalPanel;
   }
 
   public DeviceUpgradePanel getDeviceUpgradePanel()
   {
     return devicePanel;
   }
 
 }
