 package com.hifiremote.jp1;
 
 import info.clearthought.layout.TableLayout;
 import java.awt.*;
 import java.awt.datatransfer.*;
 import java.awt.event.*;
import java.beans.*;
 import java.io.*;
 import java.text.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.filechooser.*;
 import javax.swing.plaf.FontUIResource;
 
 public class KeyMapMaster
  extends JFrame
 implements ActionListener, PropertyChangeListener
 {
   private static KeyMapMaster me = null;
   private Preferences preferences = null;
 
   private DeviceEditorPanel editorPanel = null;
   private JMenuItem newItem = null;
   private JMenuItem openItem = null;
   private JMenuItem saveItem = null;
   private JMenuItem saveAsItem = null;
 //  private JMenuItem importItem = null;
   private JMenuItem importFromClipboardItem = null;
   private JMenu recentFileMenu = null;
   private JMenuItem exitItem = null;
   private JMenuItem manualItem = null;
   private JMenuItem editorItem = null;
   private JMenuItem rawItem = null;
   private JMenuItem binaryItem = null;
   private JMenuItem writeBinaryItem = null;
   private JMenuItem updateItem = null;
   private JMenuItem aboutItem = null;
   private JPanel actionPanel = null;
   private JButton okButton = null;
   private JButton cancelButton = null;
   private JLabel messageLabel = null;
   private Remote[] remotes = new Remote[ 0 ];
   private ProtocolManager protocolManager = ProtocolManager.getProtocolManager();
   private DeviceUpgrade deviceUpgrade = null;
   private static File homeDirectory = null;
   private static String upgradeExtension = ".rmdu";
   public final static int ACTION_EXIT = 1;
   public final static int ACTION_LOAD = 2;
 
   public KeyMapMaster( PropertyFile prefs )
   {
     super( "RemoteMaster" );
     me = this;
 
     setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
 
     preferences = new Preferences( prefs );
     homeDirectory = prefs.getFile().getParentFile();
 
     addWindowListener( new WindowAdapter()
     {
       public void windowClosing( WindowEvent event )
       {
         try
         {
           if ( !promptToSaveUpgrade( ACTION_EXIT ))
             return;
           savePreferences();
           setVisible( false );
           dispose();
         }
         catch ( Exception e )
         {
           System.err.println( "KeyMapMaster.windowClosing() caught an exception!" );
           e.printStackTrace( System.out );
         }
 //        setVisible( false );
       }
     });
 
     createMenus();
 
     preferences.load( recentFileMenu, this );
 
     deviceUpgrade = new DeviceUpgrade();
     Remote r = null;
 
     String name = preferences.getLastRemoteName();
     RemoteManager rm = RemoteManager.getRemoteManager();
     if ( name != null )
       r = rm.findRemoteByName( name );
     if ( r == null )
       r = getRemotes()[ 0 ];
     Protocol protocol = protocolManager.getProtocolsForRemote( r ).get( 0 );
     deviceUpgrade.setProtocol( protocol );
     deviceUpgrade.setRemote( r );
 
     editorPanel = new DeviceEditorPanel( deviceUpgrade, getRemotes());
     add( editorPanel, BorderLayout.CENTER );
    editorPanel.addPropertyChangeListener( this, "remote" );
     messageLabel = new JLabel( " " );
     messageLabel.setForeground( Color.RED );
     add( messageLabel, BorderLayout.SOUTH );
 
     fontSizeAdjustment = preferences.getFontSizeAdjustment();
       adjustFontSize( fontSizeAdjustment );
 
     pack();
     Rectangle bounds = preferences.getBounds();
     if ( bounds != null )
       setBounds( bounds );
     setVisible( true );
   }
 
 //  public static KeyMapMaster getKeyMapMaster(){ return me;}
 
   private void createMenus()
   {
     JMenuBar menuBar = new JMenuBar();
     setJMenuBar( menuBar );
     JMenu menu = new JMenu( "File" );
     menu.setMnemonic( KeyEvent.VK_F );
     menuBar.add( menu );
     newItem = new JMenuItem( "New" );
     newItem.setMnemonic( KeyEvent.VK_N );
     newItem.addActionListener( this );
     menu.add( newItem );
     openItem = new JMenuItem( "Open..." );
     openItem.setMnemonic( KeyEvent.VK_O );
     openItem.addActionListener( this );
     menu.add( openItem );
     saveItem = new JMenuItem( "Save" );
     saveItem.setMnemonic( KeyEvent.VK_S );
     saveItem.setEnabled( false );
     saveItem.addActionListener( this );
     menu.add( saveItem );
     saveAsItem = new JMenuItem( "Save as..." );
     saveAsItem.setMnemonic( KeyEvent.VK_A );
     saveAsItem.setDisplayedMnemonicIndex( 5 );
     saveAsItem.addActionListener( this );
     menu.add( saveAsItem );
 
     menu.addSeparator();
 
 //    importItem = new JMenuItem( "Import KM file..." );
 //    importItem.setMnemonic( KeyEvent.VK_K );
 //    importItem.addActionListener( this );
 //    menu.add( importItem );
 //
     importFromClipboardItem = new JMenuItem( "Import from Clipboard" );
     importFromClipboardItem.setMnemonic( KeyEvent.VK_C );
     importFromClipboardItem.addActionListener( this );
     menu.add( importFromClipboardItem );
 
     menu.addSeparator();
 
     recentFileMenu = new JMenu( "Recent" );
     recentFileMenu.setMnemonic( KeyEvent.VK_R );
     recentFileMenu.setEnabled( false );
     menu.add( recentFileMenu );
     menu.addSeparator();
 
     exitItem = new JMenuItem( "Exit" );
     exitItem.setMnemonic( KeyEvent.VK_X );
     exitItem.addActionListener( this );
     menu.add( exitItem );
 
     menu = new JMenu( "Options" );
     menu.setMnemonic( KeyEvent.VK_O );
     menuBar.add( menu );
 
     JMenu submenu = new JMenu( "Look and Feel" );
     submenu.setMnemonic( KeyEvent.VK_L );
     menu.add( submenu );
 
     ActionListener al = new ActionListener()
     {
       public void actionPerformed( ActionEvent e )
       {
         try
         {
           JRadioButtonMenuItem item = ( JRadioButtonMenuItem )e.getSource();
           String lf = item.getActionCommand();
           UIManager.setLookAndFeel( lf );
           preferences.setLookAndFeel( lf );
           SwingUtilities.updateComponentTreeUI( me );
           preferences.setLookAndFeel( lf );
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
       JRadioButtonMenuItem item = new JRadioButtonMenuItem( info[ i ].getName());
       lookAndFeelItems[ i ] = item;
       item.setMnemonic( item.getText().charAt( 0 ));
       item.setActionCommand( info[ i ].getClassName());
       group.add( item );
       submenu.add( item );
       if ( item.getActionCommand().equals( lookAndFeel ))
         item.setSelected( true );
       item.addActionListener( al );
     }
 
     submenu = new JMenu( "Font size" );
     submenu.setMnemonic( KeyEvent.VK_F );
     menu.add( submenu );
 
     al = new ActionListener()
     {
       public void actionPerformed( ActionEvent e )
       {
         JMenuItem button = ( JMenuItem )e.getSource();
         float adjustment = Float.parseFloat( button.getActionCommand());
         adjustFontSize( adjustment );
         fontSizeAdjustment += adjustment;
       }
     };
 
     JMenuItem menuItem = new JMenuItem( "Increase" );
     menuItem.setMnemonic( KeyEvent.VK_I );
     submenu.add( menuItem );
     menuItem.addActionListener( al );
     menuItem.setActionCommand( "1f" );
 
     menuItem = new JMenuItem( "Decrease" );
     menuItem.setMnemonic( KeyEvent.VK_I );
     submenu.add( menuItem );
     menuItem.addActionListener( al );
     menuItem.setActionCommand( "-1f" );
 
     group = new ButtonGroup();
     submenu = new JMenu( "Prompt to Save" );
     submenu.setMnemonic( KeyEvent.VK_P );
     menu.add( submenu );
 
     al = new ActionListener()
     {
       public void actionPerformed( ActionEvent e )
       {
         Object source = e.getSource();
         for ( int i = 0; i < promptButtons.length; i++ )
           if ( promptButtons[ i ] == source )
           {
             promptFlag = i;
             preferences.setPromptToSave( promptStrings[ i ]);
             break;
           }
       }
     };
 
     String promptText = preferences.getPromptToSave();
     promptButtons = new JRadioButtonMenuItem[ promptStrings.length ];
     for ( int i = 0; i < promptStrings.length; i++ )
     {
       JRadioButtonMenuItem item = new JRadioButtonMenuItem( promptStrings[ i ] );
       item.setMnemonic( promptMnemonics[ i ]);
       promptButtons[ i ] = item;
       if ( promptStrings[ i ].equals( promptText ))
       {
         item.setSelected( true );
         promptFlag = i;
       }
       item.addActionListener( al );
       group.add( item );
       submenu.add( item );
     }
 
     submenu = new JMenu( "Remotes" );
     submenu.setMnemonic( KeyEvent.VK_R );
     menu.add( submenu );
 
     group = new ButtonGroup();
     useAllRemotes = new JRadioButtonMenuItem( "All" );
     useAllRemotes.setMnemonic( KeyEvent.VK_A );
     al = new ActionListener()
     {
       public void actionPerformed( ActionEvent e )
       {
         Object source = e.getSource();
         if ( source == useAllRemotes )
         {
           editorPanel.setRemotes( RemoteManager.getRemoteManager().getRemotes());
           preferences.setShowRemotes( "All" );
         }
         else if ( source == usePreferredRemotes )
         {
           editorPanel.setRemotes( preferences.getPreferredRemotes());
           preferences.setShowRemotes( "Preferred" );
         }
         else
           editPreferredRemotes();
       }
     };
     useAllRemotes.setSelected( true );
     group.add( useAllRemotes );
     submenu.add( useAllRemotes );
 
     usePreferredRemotes = new JRadioButtonMenuItem( "Preferred" );
     usePreferredRemotes.setMnemonic( KeyEvent.VK_P );
     group.add( usePreferredRemotes );
     submenu.add( usePreferredRemotes );
 
     String temp = preferences.getShowRemotes();
     if ( temp.equals( "All" ))
       useAllRemotes.setSelected( true );
     else
       usePreferredRemotes.setSelected( true );
 
     if ( preferences.getPreferredRemotes().length == 0 )
     {
       useAllRemotes.setSelected( true );
       usePreferredRemotes.setEnabled( false );
     }
     useAllRemotes.addActionListener( al );
     usePreferredRemotes.addActionListener( al );
 
     submenu.addSeparator();
     JMenuItem item = new JMenuItem( "Edit preferred..." );
     item.setMnemonic( KeyEvent.VK_E );
     item.addActionListener( al );
     submenu.add( item );
 
     submenu = new JMenu( "Function names" );
     submenu.setMnemonic( KeyEvent.VK_F );
     menu.add( submenu );
 
     group = new ButtonGroup();
     useDefaultNames = new JRadioButtonMenuItem( "Default" );
     useDefaultNames.setMnemonic( KeyEvent.VK_D );
     al = new ActionListener()
     {
       public void actionPerformed( ActionEvent e )
       {
         Object source = e.getSource();
         if (( source != useDefaultNames ) && ( source != useCustomNames ))
           editCustomNames();
       }
     };
     useDefaultNames.setSelected( true );
     useDefaultNames.addActionListener( al );
     group.add( useDefaultNames );
     submenu.add( useDefaultNames );
 
     useCustomNames = new JRadioButtonMenuItem( "Custom" );
     useCustomNames.setMnemonic( KeyEvent.VK_C );
     useCustomNames.setSelected( preferences.getUseCustomNames());
 
     useCustomNames.addActionListener( al );
     group.add( useCustomNames );
     submenu.add( useCustomNames );
 
     submenu.addSeparator();
     item = new JMenuItem( "Edit custom names..." );
     item.setMnemonic( KeyEvent.VK_E );
     item.addActionListener( al );
     submenu.add( item );
 
     menu = new JMenu( "Advanced" );
     menu.setMnemonic( KeyEvent.VK_A );
     menuBar.add( menu );
 
     manualItem = new JMenuItem( "Manual Settings..." );
     manualItem.setMnemonic( KeyEvent.VK_M );
     manualItem.addActionListener( this );
     menu.add( manualItem );
 
     editorItem = new JMenuItem( "Protocol Editor..." );
     editorItem.setMnemonic( KeyEvent.VK_P );
     editorItem.addActionListener( this );
     menu.add( editorItem );
 
     rawItem = new JMenuItem( "Import Raw Upgrade..." );
     rawItem.setMnemonic( KeyEvent.VK_R );
     rawItem.addActionListener( this );
     menu.add( rawItem );
 
     binaryItem = new JMenuItem( "Import Binary Upgrade..." );
     binaryItem.setMnemonic( KeyEvent.VK_B );
     binaryItem.addActionListener( this );
     menu.add( binaryItem );
 
     writeBinaryItem = new JMenuItem( "Export Binary Upgrade..." );
     writeBinaryItem.setEnabled( false );
     writeBinaryItem.setMnemonic( KeyEvent.VK_X );
     writeBinaryItem.addActionListener( this );
     menu.add( writeBinaryItem );
 
     menu = new JMenu( "Help" );
     menu.setMnemonic( KeyEvent.VK_H );
     menuBar.add( menu );
 
     updateItem = new JMenuItem( "Check for updates", KeyEvent.VK_C );
     updateItem.addActionListener( this );
     menu.add( updateItem );
 
     aboutItem = new JMenuItem( "About..." );
     aboutItem.setMnemonic( KeyEvent.VK_A );
     aboutItem.addActionListener( this );
     menu.add( aboutItem );
   }
 
   private void savePreferences()
     throws Exception
   {
     int state = getExtendedState();
     if ( state != Frame.NORMAL )
       setExtendedState( Frame.NORMAL );
     preferences.setBounds( getBounds());
     preferences.save( recentFileMenu );
   }
 
   private void editManualSettings()
   {
     ManualSettingsDialog d =
       new ManualSettingsDialog( this, protocolManager.getManualProtocol());
     d.setVisible( true );
   }
 
   public void showMessage( String message )
   {
     messageLabel.setText( message );
     Toolkit.getDefaultToolkit().beep();
   }
 
   public static void showMessage( String message, Component c )
   {
     KeyMapMaster km = ( KeyMapMaster )SwingUtilities.getAncestorOfClass( KeyMapMaster.class, c );
     if ( km != null )
     {
       km.showMessage( message );
       return;
     }
 
     JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
     if ( frame != null )
     {
       frame.showMessage( message );
       return;
     }
     JOptionPane.showMessageDialog( c, message );
   }
 
   public void clearMessage()
   {
     messageLabel.setText( " " );
   }
 
   public static void clearMessage( Component c )
   {
     KeyMapMaster km = ( KeyMapMaster )SwingUtilities.getAncestorOfClass( KeyMapMaster.class, c );
     if ( km != null )
     {
       km.clearMessage();
       return;
     }
 
     JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
     if ( frame != null )
     {
       frame.clearMessage();
       return;
     }
   }
 
   // ActionListener Methods
   public void actionPerformed( ActionEvent e )
   {
     try
     {
       Object source = e.getSource();
 
       if ( source == newItem )
       {
         if ( !promptToSaveUpgrade( ACTION_LOAD ))
           return;
         Protocol oldProtocol = deviceUpgrade.getProtocol();
         deviceUpgrade.reset();
       }
       else if ( source == saveItem )
       {
         deviceUpgrade.store();
       }
       else if ( source == saveAsItem )
       {
         saveAs();
       }
       else if ( source == openItem )
       {
         if ( !promptToSaveUpgrade( ACTION_LOAD ))
           return;
         File file = getUpgradeFile( preferences.getUpgradePath());
         loadUpgrade( file );
       }
       else if ( source == importFromClipboardItem )
       {
         if ( !promptToSaveUpgrade( ACTION_LOAD ))
           return;
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         Transferable clipData = clipboard.getContents( clipboard );
         if ( clipData != null )
         {
           try
           {
             if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
             {
               String s =
                 ( String )( clipData.getTransferData( DataFlavor.stringFlavor ));
               BufferedReader in = new BufferedReader( new StringReader( s ));
               loadUpgrade( in );
             }
           }
           catch (Exception ex)
           {
             ex.printStackTrace( System.err );
           }
         }
       }
       else if ( source == exitItem )
       {
         dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ));
       }
       else if ( source == manualItem )
       {
         editManualSettings();
       }
       else if ( source == editorItem )
       {
         ProtocolEditor d = new ProtocolEditor( this );
         d.setVisible( true );
       }
       else if ( source == rawItem )
       {
         ImportRawUpgradeDialog d = new ImportRawUpgradeDialog( this, deviceUpgrade );
         d.setVisible( true );
       }
       else if ( source == binaryItem )
       {
         File file = null;
         RMFileChooser chooser = new RMFileChooser( preferences.getBinaryUpgradePath());
         try
         {
           chooser.setAcceptAllFileFilterUsed( false );
         }
         catch ( Exception ex )
         {
           ex.printStackTrace( System.err );
         }
         chooser.setFileFilter( new EndingFileFilter( "Binary upgrade files", binaryEndings ));
         int returnVal = chooser.showOpenDialog( this );
         if ( returnVal == RMFileChooser.APPROVE_OPTION )
         {
           file = chooser.getSelectedFile();
 
           int rc = JOptionPane.YES_OPTION;
           if ( !file.exists())
           {
             JOptionPane.showMessageDialog( this,
                                            file.getName() + " doesn't exist.",
                                            "File doesn't exist.",
                                            JOptionPane.ERROR_MESSAGE );
           }
           else if ( file.isDirectory())
           {
             JOptionPane.showMessageDialog( this,
                                            file.getName() + " is a directory.",
                                            "File doesn't exist.",
                                            JOptionPane.ERROR_MESSAGE );
           }
           else
           {
             preferences.setBinaryUpgradePath( file.getParentFile());
             BinaryUpgradeReader reader = new BinaryUpgradeReader( file );
             Remote r = reader.getRemote();
             DeviceType devType = r.getDeviceTypeByIndex( reader.getDeviceIndex());
             String aliasName = null;
             String[] aliasNames = r.getDeviceTypeAliasNames();
             boolean nameMatch = false;
             for ( int i = 0; i < aliasNames.length && !nameMatch ; i++ )
             {
               String tryit = aliasNames[ i ];
               if ( devType == r.getDeviceTypeByAliasName( tryit ))
               {
                 nameMatch = devType.getName().equalsIgnoreCase( tryit );
                 if (( aliasName == null ) || nameMatch )
                   aliasName = tryit;
               }
             }
             deviceUpgrade.importRawUpgrade( reader.getCode(),
                                             r,
                                             aliasName,
                                             reader.getPid(),
                                             reader.getProtocolCode());
             deviceUpgrade.setSetupCode( reader.getSetupCode());
           }
         }
       }
       else if ( source == writeBinaryItem )
       {
         File path = BinaryUpgradeWriter.write( deviceUpgrade, preferences.getBinaryUpgradePath());
         if ( path != null )
           preferences.setBinaryUpgradePath( path );
       }
       else if ( source == updateItem )
       {
         java.net.URL url = new java.net.URL( "http://controlremote.sourceforge.net/version.dat" );
         BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream()));
         String latestVersion = in.readLine();
         in.close();
         String text = null;
         if ( RemoteMaster.version.equals( latestVersion ))
           text = "You are using the latest version (" + RemoteMaster.version + ") of RemoteMaster.";
         else
           text = "<html>Version " + latestVersion + " of RemoteMaster is available, but you are still using version " + RemoteMaster.version +
                  "<p>The new version is available for download from<br><a href=\"http://prdownloads.sourceforge.net/controlremote/RemoteMaster." + latestVersion + ".zip?download\">" +
                  "http://prdownloads.sourceforge.net/controlremote/RemoteMaster." + latestVersion + ".zip?download</a></html>";
 
         JEditorPane pane = new JEditorPane( "text/html", text );
         pane.setEditable( false );
         pane.setBackground( getContentPane().getBackground());
         new TextPopupMenu( pane );
         JOptionPane.showMessageDialog( this, pane, "RemoteMaster Version Check", JOptionPane.INFORMATION_MESSAGE );
       }
       else if ( source == aboutItem )
       {
         String text = "<html><b>RemoteMaster Device Upgrade Editor, " + RemoteMaster.version + "</b>" +
                       "<p>Get the latest version at <a href=\"http://controlremote.sourceforge.net\">http://controlremote.sourceforge.net</a></p>" +
                       "<p>Java version " + System.getProperty( "java.version" ) + " from " + System.getProperty( "java.vendor" ) + "</p>" +
                       "<p>RDFs loaded from <b>" + preferences.getRDFPath() + "</b></p>" +
                       "<p>Written primarily by <i>Greg Bush</i>, and now accepting donations " +
                       "at <a href=\"http://sourceforge.net/donate/index.php?user_id=735638\">http://sourceforge.net/donate/index.php?user_id=735638</a></p>" +
                       "<p>Other contributors include:<blockquote>" + "John&nbsp;S&nbsp;Fine, Nils&nbsp;Ekberg, Jon&nbsp;Armstrong, Robert&nbsp;Crowe, " +
                       "Mark&nbsp;Pauker, Mark&nbsp;Pierson, Mike&nbsp;England</blockquote></html>";
 
         JEditorPane pane = new JEditorPane( "text/html", text );
         pane.setEditable( false );
         pane.setBackground( getContentPane().getBackground());
         new TextPopupMenu( pane );
         JScrollPane scroll = new JScrollPane( pane );
         Dimension d = pane.getPreferredSize();
         d.height = ( d.height * 5 ) / 4;
         d.width = ( d.width * 2 ) / 3;
         scroll.setPreferredSize( d );
 
         JOptionPane.showMessageDialog( this, scroll, "About RemoteMaster", JOptionPane.INFORMATION_MESSAGE );
       }
       else if ( source == okButton )
       {
         setVisible( false );
       }
       else if ( source == cancelButton )
       {
         deviceUpgrade = null;
         setVisible( false );
       }
       else // must be a recent file
       {
         JMenuItem item = ( JMenuItem )source;
         File f = new File( item.getText());
         loadUpgrade( f );
       }
       refresh();
     }
     catch ( Exception ex )
     {
       ex.printStackTrace( System.err );
     }
   } // actionPerformed
 
   public static File promptForUpgradeFile( File path )
   {
     return me.getUpgradeFile( path );
   }
 
   public File getUpgradeFile( File path )
   {
     if ( path == null )
       path = preferences.getUpgradePath();
 
     File file = null;
     RMFileChooser chooser = new RMFileChooser( path );
     try
     {
       chooser.setAcceptAllFileFilterUsed( false );
     }
     catch ( Exception e )
     {
       e.printStackTrace( System.err );
     }
     chooser.setFileFilter( new EndingFileFilter( "All device upgrade files", anyEndings ));
     chooser.addChoosableFileFilter( new EndingFileFilter( "KeyMapMaster device upgrade files", kmEndings ));
     chooser.addChoosableFileFilter( new EndingFileFilter( "RemoteMaster device upgrade files", rmEndings ));
 
     int returnVal = chooser.showOpenDialog( this );
     if ( returnVal == RMFileChooser.APPROVE_OPTION )
     {
       file = chooser.getSelectedFile();
 
       int rc = JOptionPane.YES_OPTION;
       if ( !file.exists())
       {
         JOptionPane.showMessageDialog( this,
                                        file.getName() + " doesn't exist.",
                                        "File doesn't exist.",
                                        JOptionPane.ERROR_MESSAGE );
       }
       else if ( file.isDirectory())
       {
         JOptionPane.showMessageDialog( this,
                                        file.getName() + " is a directory.",
                                        "File doesn't exist.",
                                        JOptionPane.ERROR_MESSAGE );
       }
       else
       {
         String str = file.getName().toLowerCase();
         preferences.setUpgradePath( file.getParentFile());
       }
     }
     return file;
   }
 
   public void saveAs()
     throws IOException
   {
     RMFileChooser chooser = new RMFileChooser( preferences.getUpgradePath());
     chooser.setFileFilter( new EndingFileFilter( "RemoteMaster device upgrade files (*.rmdu)", rmEndings ));
     File f = deviceUpgrade.getFile();
     if ( f == null )
     {
       String fname = deviceUpgrade.getDescription() + upgradeExtension;
       fname = fname.replace( '/', '-' );
       f = new File( preferences.getUpgradePath(), fname );
     }
     chooser.setSelectedFile( f );
     int returnVal = chooser.showSaveDialog( this );
     if ( returnVal == RMFileChooser.APPROVE_OPTION )
     {
       String name = chooser.getSelectedFile().getAbsolutePath();
       if ( !name.toLowerCase().endsWith( upgradeExtension ))
         name = name + upgradeExtension;
       File file = new File( name );
       int rc = JOptionPane.YES_OPTION;
       if ( file.exists())
       {
         rc = JOptionPane.showConfirmDialog( this,
                                             file.getName() + " already exists.  Do you want to replace it?",
                                             "Replace existing file?",
                                             JOptionPane.YES_NO_OPTION );
       }
       if ( rc == JOptionPane.YES_OPTION )
       {
         deviceUpgrade.store( file );
         updateRecentFiles( file );
         saveItem.setEnabled( true );
         setTitle( file.getCanonicalPath() + " - RemoteMaster" );
       }
     }
   }
 
   public boolean promptToSaveUpgrade( int action )
     throws IOException
   {
     String promptFlag = preferences.getPromptToSave();
     if ( promptFlag.equals( promptStrings[ PROMPT_NEVER ]))
       return true;
     else if ( !promptFlag.equals( promptStrings[ PROMPT_ALWAYS ]))
     {
       if ( action != ACTION_EXIT )
         return true;
     }
 
     int rc = JOptionPane.showConfirmDialog( this,
 //                                            "All changes made to the current upgrade will be lost if you proceed.\n\n" +
                                             "Do you want to save the current upgrade before proceeding?",
                                             "Save upgrade?",
                                             JOptionPane.YES_NO_CANCEL_OPTION );
     if (( rc == JOptionPane.CANCEL_OPTION ) || ( rc == JOptionPane.CLOSED_OPTION ))
       return false;
     if ( rc == JOptionPane.NO_OPTION )
       return true;
 
     if ( deviceUpgrade.getFile() != null )
       deviceUpgrade.store();
     else
       saveAs();
     return true;
   }
 
   public void loadUpgrade( File file )
     throws Exception
   {
     if (( file == null ) || !file.exists())
       return;
 
     System.err.println( "Opening " + file.getCanonicalPath() + ", last modified " +
                         DateFormat.getInstance().format( new Date( file.lastModified())));
 
     deviceUpgrade.reset();
     deviceUpgrade.load( file );
 
     boolean isRMDU = file.getName().toLowerCase().endsWith( ".rmdu" );
 
     if ( isRMDU )
       updateRecentFiles( file );
     preferences.setUpgradePath( file.getParentFile());
     refresh();
   }
 
   private void updateRecentFiles( File file )
     throws IOException
   {
     boolean isRMDU = file.getName().toLowerCase().endsWith( ".rmdu" );
 
     if ( isRMDU )
     {
       int i = recentFileMenu.getItemCount() - 1;
       while ( i >= 0 )
       {
         JMenuItem item = recentFileMenu.getItem( i );
         File f = new File( item.getText());
         if ( f.getCanonicalPath().equals( file.getCanonicalPath()))
           recentFileMenu.remove( i );
         --i;
       }
       i = recentFileMenu.getItemCount();
       while ( i > 9 )
         recentFileMenu.remove( --i );
 
       JMenuItem item = new JMenuItem( file.getAbsolutePath());
       item.addActionListener( this );
       recentFileMenu.add( item, 0 );
 
       recentFileMenu.setEnabled( true );
     }
   }
 
   public void loadUpgrade( BufferedReader reader )
     throws Exception
   {
     deviceUpgrade.reset();
     deviceUpgrade.load( reader );
     refresh();
   }
 
   private void refresh()
   {
     String title = "RemoteMaster";
     File file = deviceUpgrade.getFile();
     if ( file != null )
       title = file.getAbsolutePath() + " - RemoteMaster";
 
     saveItem.setEnabled( file != null );
     writeBinaryItem.setEnabled( deviceUpgrade.getRemote().getSupportsBinaryUpgrades());
 
     validateUpgrade();
     editorPanel.refresh();
   }
 
   public void importUpgrade( BufferedReader in )
     throws Exception
   {
     deviceUpgrade.reset();
     deviceUpgrade.importUpgrade( in );
     refresh();
   }
 
   public static File getHomeDirectory()
   {
     if ( homeDirectory == null )
     {
       String temp = System.getProperty( "user.dir" );
       if ( temp != null )
         homeDirectory = new File( temp );
     }
     return homeDirectory;
   }
 
   public void validateUpgrade()
   {
     Remote r = deviceUpgrade.getRemote();
     Protocol p = deviceUpgrade.getProtocol();
     java.util.List< Protocol > protocols = protocolManager.getProtocolsForRemote( r );
     if ( !protocols.contains( p ) && !p.hasCode( r ))
     {
       System.err.println( "KeyMapMaster.validateUpgrade(), protocol " + p.getDiagnosticName() +
                           "is not compatible with remote " + r.getName());
 
       // Find a matching protocol for this remote
       Protocol match = null;
       String name = p.getName();
       for ( Protocol p2 : protocols )
       {
         if ( p2.getName().equals( name ))
         {
           match = p2;
           System.err.println( "\tFound one with the same name: " + p2.getDiagnosticName());
           break;
         }
       }
       if ( match != null )
         deviceUpgrade.setProtocol( match );
       else
         JOptionPane.showMessageDialog( this,
                                        "The selected protocol " + p.getDiagnosticName() +
                                        "\nis not compatible with the selected remote.\n" +
                                        "This upgrade will NOT function correctly.\n" +
                                        "Please choose a different protocol.",
                                        "Error", JOptionPane.ERROR_MESSAGE );
     }
   }
 
   public static Remote getRemote()
   {
     return me.deviceUpgrade.getRemote();
   }
 
   public Remote[] getRemotes()
   {
     if ( preferences.getShowRemotes().equals( "Preferred" ))
       return preferences.getPreferredRemotes();
     else
       return RemoteManager.getRemoteManager().getRemotes();
   }
 
   public Preferences getPreferences()
   {
     return preferences;
   }
 
   public DeviceUpgrade getDeviceUpgrade()
   {
     return deviceUpgrade;
   }
 
   private void editPreferredRemotes()
   {
     PreferredRemoteDialog d = new PreferredRemoteDialog( this, preferences.getPreferredRemotes());
     d.setVisible( true );
     if ( d.getUserAction() == JOptionPane.OK_OPTION )
     {
       Remote[] preferredRemotes = d.getPreferredRemotes();
       preferences.setPreferredRemotes( preferredRemotes );
       if ( preferredRemotes.length == 0 )
       {
         usePreferredRemotes.setEnabled( false );
         if  ( !useAllRemotes.isSelected())
         {
           useAllRemotes.setSelected( true );
 
           editorPanel.setRemotes( RemoteManager.getRemoteManager().getRemotes());
         }
       }
       else
         usePreferredRemotes.setEnabled( true );
 
       if ( usePreferredRemotes.isSelected())
         editorPanel.setRemotes( preferredRemotes );
     }
   }
 
   public String[] getCustomNames()
   {
     if ( useCustomNames.isSelected())
       return preferences.getCustomNames();
     else
       return null;
   }
 
   private void editCustomNames()
   {
     CustomNameDialog d = new CustomNameDialog( this, preferences.getCustomNames());
     d.setVisible( true );
     if ( d.getUserAction() == JOptionPane.OK_OPTION )
     {
       String[] customNames = d.getCustomNames();
       if (( customNames == null ) || customNames.length == 0 )
       {
         useCustomNames.setEnabled( false );
         useDefaultNames.setSelected( true );
       }
       else
       {
         useCustomNames.setEnabled( true );
         preferences.setCustomNames( customNames );
       }
     }
   }
 
   private void adjustFontSize( float adjustment )
   {
     if ( adjustment == 0.0f )
       return;
     UIDefaults defaults = UIManager.getDefaults(); // Build of Map of attributes for each component
     for( Enumeration en = defaults.keys(); en.hasMoreElements(); )
     {
       Object o = en.nextElement();
       if ( o.getClass() != String.class )
         continue;
       String key = ( String )o;
       if ( key.endsWith(".font") && !key.startsWith("class") && !key.startsWith("javax"))
       {
         FontUIResource font = ( FontUIResource )UIManager.get( key );
         FontUIResource newFont = new FontUIResource( font.deriveFont( font.getSize2D() + adjustment ));
         if ( key.indexOf( "Table") != -1 )
         {
           System.err.println( "key=" + key );
           System.err.println( "got font " + font );
           System.err.println( "set font " + newFont );
         }
         UIManager.put( key, newFont );
       }
     }
     SwingUtilities.updateComponentTreeUI( this );
     pack();
   }
  
  public void propertyChange( PropertyChangeEvent evt )
  {
    refresh();
  }
 
   private JRadioButtonMenuItem[] lookAndFeelItems = null;
   private JRadioButtonMenuItem[] promptButtons = null;
   private JMenuItem useAllRemotes = null;
   private JMenuItem usePreferredRemotes = null;
   private JMenuItem useDefaultNames = null;
   private JMenuItem useCustomNames = null;
   private Float fontSizeAdjustment = 0.0f;
   private int promptFlag = 0;
 
   private final static String[] promptStrings = { "Always", "On Exit", "Never" };
   private final static int[] promptMnemonics = { KeyEvent.VK_A, KeyEvent.VK_X, KeyEvent.VK_N };
   public final static int PROMPT_NEVER = 2;
   public final static int PROMPT_ALWAYS = 0;
   private final static String[] anyEndings = { ".txt", ".km", upgradeExtension };
   private final static String[] kmEndings = { ".txt" };
   private final static String[] rmEndings = { ".km", upgradeExtension };
   private final static String[] binaryEndings = { ".bin", "_obj" };
 }
 
