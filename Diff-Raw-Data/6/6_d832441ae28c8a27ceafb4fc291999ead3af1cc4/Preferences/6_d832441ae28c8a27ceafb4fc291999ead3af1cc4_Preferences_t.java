 package com.hifiremote.jp1;
 
 import java.util.*;
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class Preferences
 {
   public Preferences( File home, File file )
   {
     this.home = home;
     this.file = file;
   }
 
   public void load( JMenu recentFileMenu )
     throws Exception
   {
     Properties props = new Properties();
 
     if ( file.canRead())
     {
       FileInputStream in = new FileInputStream( file );
       props.load( in );
       in.close();
     }
 
     String temp = props.getProperty( "RDFPath" );
     if ( temp != null )
       rdfPath = new File( temp );
     else
       rdfPath = new File( home, "rdf" );
     while ( !rdfPath.exists() && !rdfPath.isDirectory())
       rdfPath = rdfPath.getParentFile();
 
     temp = props.getProperty( "UpgradePath" );
     if ( temp == null )
       temp = props.getProperty( "KMPath" );
     if ( temp != null )
       upgradePath = new File( temp );
     else
       upgradePath = new File( home, upgradeDirectory );
    while (( upgradePath != null ) && !upgradePath.exists() && !upgradePath.isDirectory())
       upgradePath = upgradePath.getParentFile();
 
    if (  upgradePath == null )
      upgradePath = new File( home, upgradeDirectory );

     String defaultLookAndFeel = UIManager.getSystemLookAndFeelClassName();
     temp = props.getProperty( "LookAndFeel", defaultLookAndFeel );
     try
     {
       UIManager.setLookAndFeel( temp );
       SwingUtilities.updateComponentTreeUI( KeyMapMaster.getKeyMapMaster());
       for ( int i = 0; i < lookAndFeelItems.length; i ++ )
       {
         if ( lookAndFeelItems[ i ].getActionCommand().equals( temp ))
         {
           lookAndFeelItems[ i ].setSelected( true );
           break;
         }
       }
     }
     catch ( Exception e )
     {
       System.err.println( "Exception thrown when setting look and feel to " + temp );
     }
 
     lastRemoteName = props.getProperty( "Remote.name" );
     lastRemoteSignature = props.getProperty( "Remote.signature" );
 
     temp = props.getProperty( "PromptToSave", promptStrings[ 0 ] );
     for ( int i = 0; i < promptStrings.length; i++ )
       if ( promptStrings[ i ].equals( temp ))
         promptFlag = i;
     if ( promptFlag > promptStrings.length )
       promptFlag = 0;
 
     promptButtons[ promptFlag ].setSelected( true );
 
     for ( int i = 0; true; i++ )
     {
       temp = props.getProperty( "PreferredRemotes." + i );
       if ( temp == null )
         break;
       System.err.println( "Preferred remote name " + temp );
       preferredRemoteNames.add( temp );
     }
 
     temp = props.getProperty( "ShowRemotes", "All" );
     if ( temp.equals( "All" ))
       useAllRemotes.setSelected( true );
     else
       usePreferredRemotes.setSelected( true );
 
     if ( preferredRemoteNames.size() == 0 )
     {
       useAllRemotes.setSelected( true );
       usePreferredRemotes.setEnabled( false );
     }
     for ( int i = 0; i < 10; i++ )
     {
       temp = props.getProperty( "RecentFiles." + i );
       if ( temp == null )
         break;
       File f = new File( temp );
       if ( f.canRead() )
         recentFileMenu.add( new FileAction( f ));
     }
     if ( recentFileMenu.getItemCount() > 0 )
       recentFileMenu.setEnabled( true );
 
     temp = props.getProperty( "Bounds" );
     if ( temp != null )
     {
       bounds = new Rectangle();
       StringTokenizer st = new StringTokenizer( temp, "," );
       bounds.x = Integer.parseInt( st.nextToken());
       bounds.y = Integer.parseInt( st.nextToken());
       bounds.width = Integer.parseInt( st.nextToken());
       bounds.height = Integer.parseInt( st.nextToken());
     }
 
     temp = props.getProperty( "CustomNames" );
     if ( temp != null )
     {
       StringTokenizer st = new StringTokenizer( temp, "|" );
       int count = st.countTokens();
       customNames = new String[ count ];
       for ( int i = 0; i < count; i++ )
         customNames[ i ] = st.nextToken();
     }
 
     temp = props.getProperty( "UseCustomNames" );
     useCustomNames.setSelected( temp != null );
   }
 
   public void createMenuItems( JMenu menu )
   {
     JMenu submenu = new JMenu( "Look and Feel" );
     submenu.setMnemonic( KeyEvent.VK_L );
     menu.add( submenu );
 
     ButtonGroup group = new ButtonGroup();
     UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
 
     ActionListener al = new ActionListener()
     {
       public void actionPerformed( ActionEvent e )
       {
         try
         {
           UIManager.setLookAndFeel((( JRadioButtonMenuItem )e.getSource()).getActionCommand());
           SwingUtilities.updateComponentTreeUI( KeyMapMaster.getKeyMapMaster());
         }
         catch ( Exception x )
         {}
       }
     };
 
     lookAndFeelItems = new JRadioButtonMenuItem[ info.length ];
     for ( int i = 0; i < info.length; i++ )
     {
       JRadioButtonMenuItem item = new JRadioButtonMenuItem( info[ i ].getName());
       lookAndFeelItems[ i ] = item;
       item.setMnemonic( item.getText().charAt( 0 ));
       item.setActionCommand( info[ i ].getClassName());
       group.add( item );
       submenu.add( item );
       item.addActionListener( al );
     }
 
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
             break;
           }
       }
     };
 
     promptButtons = new JRadioButtonMenuItem[ promptStrings.length ];
     for ( int i = 0; i < promptStrings.length; i++ )
     {
       JRadioButtonMenuItem item = new JRadioButtonMenuItem( promptStrings[ i ] );
       item.setMnemonic( promptMnemonics[ i ]);
       promptButtons[ i ] = item;
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
         if (( source == useAllRemotes ) || ( source == usePreferredRemotes ))
           KeyMapMaster.getKeyMapMaster().setRemotes();
         else
           editPreferredRemotes();
       }
     };
     useAllRemotes.setSelected( true );
     useAllRemotes.addActionListener( al );
     group.add( useAllRemotes );
     submenu.add( useAllRemotes );
 
     usePreferredRemotes = new JRadioButtonMenuItem( "Preferred" );
     usePreferredRemotes.setMnemonic( KeyEvent.VK_P );
     usePreferredRemotes.addActionListener( al );
     group.add( usePreferredRemotes );
     submenu.add( usePreferredRemotes );
 
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
     useCustomNames.addActionListener( al );
     group.add( useCustomNames );
     submenu.add( useCustomNames );
 
     submenu.addSeparator();
     item = new JMenuItem( "Edit custom names..." );
     item.setMnemonic( KeyEvent.VK_E );
     item.addActionListener( al );
     submenu.add( item );
 
   }
 
   public boolean getUsePreferredRemotes()
   {
     return usePreferredRemotes.isSelected();
   }
 
   public Remote[] getPreferredRemotes()
     throws Exception
   {
     if ( preferredRemotes.length == 0 )
     {
       RemoteManager rm = RemoteManager.getRemoteManager();
       Vector work = new Vector();
       for ( Enumeration e = preferredRemoteNames.elements(); e.hasMoreElements(); )
       {
         String name = ( String )e.nextElement();
         Remote r = rm.findRemoteByName( name );
         if ( r != null )
           work.add( r );
       }
       preferredRemotes = ( Remote[] )work.toArray( preferredRemotes );
       preferredRemoteNames.removeAllElements();
       preferredRemoteNames = null;
     }
 
     return preferredRemotes;
   }
 
   public void save( JMenu recentFileMenu )
     throws Exception
   {
     Properties props = new Properties();
     props.setProperty( "RDFPath", rdfPath.getAbsolutePath());
     props.setProperty( "UpgradePath", upgradePath.getAbsolutePath());
     props.setProperty( "LookAndFeel", UIManager.getLookAndFeel().getClass().getName());
     Remote remote = KeyMapMaster.getRemote();
     props.setProperty( "Remote.name", remote.getName());
     props.setProperty( "Remote.signature", remote.getSignature());
     props.setProperty( "PromptToSave", promptStrings[ promptFlag ]);
     if ( useAllRemotes.isSelected())
       props.setProperty( "ShowRemotes", "All" );
     else
       props.setProperty( "ShowRemotes", "Preferred" );
 
     for ( int i = 0; i < recentFileMenu.getItemCount(); i++ )
     {
       JMenuItem item = recentFileMenu.getItem( i );
       FileAction action = ( FileAction )item.getAction();
       File f = action.getFile();
       props.setProperty( "RecentFiles." + i, f.getAbsolutePath());
     }
 
     for ( int i = 0; i < preferredRemotes.length; i++ )
     {
       Remote r = preferredRemotes[ i ];
       props.setProperty( "PreferredRemotes." + i, r.getName());
     }
 
     KeyMapMaster km = KeyMapMaster.getKeyMapMaster();
     int state = km.getExtendedState();
     if ( state != Frame.NORMAL )
       km.setExtendedState( Frame.NORMAL );
     Rectangle bounds = km.getBounds();
     props.setProperty( "Bounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );
 
     if ( useCustomNames.isSelected())
       props.setProperty( "UseCustomNames", "yes" );
 
     if ( customNames != null )
     {
       StringBuffer value = new StringBuffer();
       for ( int i = 0; i < customNames.length; i++ )
       {
         if ( i != 0 )
           value.append( '|' );
         value.append( customNames[ i ]);
       }
       props.setProperty( "CustomNames", value.toString() );
     }
 
     FileOutputStream out = new FileOutputStream( file );
     props.store( out, null );
     out.flush();
     out.close();
   }
 
   public File getRDFPath()
   {
     return rdfPath;
   }
 
   public void setRDFPath( File path )
   {
     rdfPath = path;
   }
 
   public File getUpgradePath()
   {
     return upgradePath;
   }
 
   public void setUpgradePath( File path )
   {
     upgradePath = path;
   }
 
   public Rectangle getBounds()
   {
     return bounds;
   }
 
   public String getLastRemoteName()
   {
     return lastRemoteName;
   }
 
   public int getPromptFlag()
   {
     return promptFlag;
   }
 
   private void editPreferredRemotes()
   {
     KeyMapMaster km = KeyMapMaster.getKeyMapMaster();
     PreferredRemoteDialog d = new PreferredRemoteDialog( km, preferredRemotes );
     d.show();
     if ( d.getUserAction() == JOptionPane.OK_OPTION )
     {
       preferredRemotes = d.getPreferredRemotes();
       if ( preferredRemotes.length == 0 )
       {
         usePreferredRemotes.setEnabled( false );
         if  ( !useAllRemotes.isSelected())
         {
           useAllRemotes.setSelected( true );
 
           km.setRemoteList( RemoteManager.getRemoteManager().getRemotes());
         }
       }
       else
         usePreferredRemotes.setEnabled( true );
 
       if ( usePreferredRemotes.isSelected())
         km.setRemoteList( preferredRemotes );
     }
   }
 
   public String[] getCustomNames()
   {
     if ( useCustomNames.isSelected())
       return customNames;
     else
       return null;
   }
 
 
   private void editCustomNames()
   {
     CustomNameDialog d = new CustomNameDialog( KeyMapMaster.getKeyMapMaster(), customNames );
     d.show();
     if ( d.getUserAction() == JOptionPane.OK_OPTION )
     {
       customNames = d.getCustomNames();
       if (( customNames == null ) || customNames.length == 0 )
       {
         useCustomNames.setEnabled( false );
         useDefaultNames.setSelected( true );
       }
       else
         useCustomNames.setEnabled( true );
     }
   }
 
   private File home;
   private File file;
   private File rdfPath;
   private File upgradePath = null;
   private JRadioButtonMenuItem[] lookAndFeelItems = null;
   private JRadioButtonMenuItem[] promptButtons = null;
   private String lastRemoteName = null;
   private String lastRemoteSignature = null;
   private Vector preferredRemoteNames = new Vector( 0 );
   private static String[] customNames = null;
 
   private final static String upgradeDirectory = "Upgrades";
   private final static String[] promptStrings = { "Always", "On Exit", "Never" };
   private final static int[] promptMnemonics = { KeyEvent.VK_A, KeyEvent.VK_X, KeyEvent.VK_N };
   public final static int PROMPT_NEVER = 2;
   public final static int PROMPT_ALWAYS = 0;
   private int promptFlag = 0;
   private JMenuItem useAllRemotes = null;
   private JMenuItem usePreferredRemotes = null;
   private JMenuItem useDefaultNames = null;
   private JMenuItem useCustomNames = null;
   private Rectangle bounds = null;
   private Remote[] preferredRemotes = new Remote[ 0 ];
 }
