 package com.hifiremote.jp1;
 
 import java.awt.Color;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class OutputPanel.
  */
 public class OutputPanel extends KMPanel implements ActionListener
 {
 
   /**
    * Instantiates a new output panel.
    * 
    * @param deviceUpgrade
    *          the device upgrade
    */
   public OutputPanel( DeviceUpgrade deviceUpgrade )
   {
     super( "Output", deviceUpgrade );
     BoxLayout bl = new BoxLayout( this, BoxLayout.Y_AXIS );
     setLayout( bl );
     setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
 
     clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 
     Box box = Box.createHorizontalBox();
     box.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ) );
     add( box );
 
     deviceLabel = new JLabel( "Device Upgrade Code" );
     deviceLabel.setAlignmentY( 1f );
     box.add( deviceLabel );
 
     box.add( Box.createHorizontalGlue() );
 
     ImageIcon copyIcon = RemoteMaster.createIcon( "Copy24" );
 
     copyDeviceUpgrade = new JButton( copyIcon );
     copyDeviceUpgrade.setToolTipText( "Copy to clipboard" );
     copyDeviceUpgrade.setAlignmentY( 1f );
     copyDeviceUpgrade.addActionListener( this );
     box.add( copyDeviceUpgrade );
 
     upgradeText = new JTextArea( 10, 40 );
     upgradeText.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
     upgradeText.setEditable( false );
     upgradeText.setDragEnabled( true );
     upgradeText.setBackground( deviceLabel.getBackground() );
     JScrollPane scroll = new JScrollPane( upgradeText );
     add( scroll );
 
     popup = new JPopupMenu();
     copyItem = new JMenuItem( "Copy" );
     copyItem.addActionListener( this );
     popup.add( copyItem );
 
     MouseAdapter mh = new MouseAdapter()
     {
       @Override
       public void mousePressed( MouseEvent e )
       {
         showPopup( e );
       }
 
       @Override
       public void mouseReleased( MouseEvent e )
       {
         showPopup( e );
       }
 
       private void showPopup( MouseEvent e )
       {
         if ( e.isPopupTrigger() )
         {
           popover = ( JTextArea )e.getSource();
           popup.show( popover, e.getX(), e.getY() );
         }
       }
     };
     upgradeText.addMouseListener( mh );
 
     add( Box.createVerticalStrut( 20 ) );
 
     box = Box.createHorizontalBox();
     add( box );
     box.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ) );
 
     protocolLabel = new JLabel( "Upgrade Protocol Code" );
     protocolLabel.setAlignmentY( 1f );
     box.add( protocolLabel );
     box.add( Box.createHorizontalGlue() );
 
     copyProtocolUpgrade = new JButton( copyIcon );
     copyProtocolUpgrade.setToolTipText( "Copy to clipboard" );
     copyProtocolUpgrade.setAlignmentY( 1f );
     copyProtocolUpgrade.addActionListener( this );
     box.add( copyProtocolUpgrade );
 
     protocolText = new JTextArea( 10, 40 );
     protocolText.setEditable( false );
     protocolText.setDragEnabled( true );
     protocolText.addMouseListener( mh );
     protocolText.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
     protocolText.setBackground( protocolLabel.getBackground() );
 
     scroll = new JScrollPane( protocolText );
     add( scroll );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.KMPanel#update()
    */
   @Override
   public void update()
   {
     Remote r = deviceUpgrade.getRemote();
 
     int keyMoveBytes = 0;
     List< KeyMove > keyMoves = deviceUpgrade.getKeyMoves();
     for ( KeyMove keyMove : keyMoves )
     {
       keyMoveBytes += keyMove.getSize( deviceUpgrade.getRemote() );
     }
     String keyMoveInfo = "";
     if ( keyMoves.size() > 0 )
     {
       keyMoveInfo = String.format( " + %1$d bytes from %2$d keymoves", keyMoveBytes, keyMoves.size() );
     }
     deviceLabel.setText( String.format( "Device Upgrade Code (%1$d bytes%2$s)", deviceUpgrade.getUpgradeHex().length(),
         keyMoveInfo ) );
 
     upgradeText.setText( deviceUpgrade.getUpgradeText() );
     Protocol p = deviceUpgrade.getProtocol();
     Hex code = deviceUpgrade.getCode();
     Hex altCode = null;
     if ( deviceUpgrade.getRemoteConfig() != null && 
         deviceUpgrade.getProtocol().getCustomCode( deviceUpgrade.getRemote().getProcessor() ) == null )
     {
       ProtocolUpgrade pu = p.getCustomUpgrade( deviceUpgrade.getRemoteConfig(), true );
       if ( pu != null && p.matched() )
       {
         altCode = pu.getCode();
         deviceUpgrade.translateCode( altCode );
         code = altCode;
       }
     }
 
     if ( deviceUpgrade.needsProtocolCode() || altCode != null )
     {
      if ( code != null )
       {
         protocolLabel.setForeground( Color.black );
         protocolLabel.setText( String.format( "Upgrade Protocol Code *** REQUIRED *** (%1$d bytes)", code.length() ) );
         protocolText.setForeground( Color.black );
       }
       else
       {
         protocolLabel.setForeground( Color.red );
         protocolLabel.setText( "Upgrade Protocol Code REQUIRED BUT NOT AVAILABLE" );
         protocolText.setForeground( protocolText.getBackground() );
       }
     }
     else
     {
       protocolLabel.setForeground( Color.red );
       protocolLabel.setText( "Upgrade Protocol Code NOT REQUIRED" );
       protocolText.setForeground( protocolText.getBackground() );
     }
     if ( code == null )
     {
       protocolText.setText( "" );
     }
     else
     {
       Processor processor = r.getProcessor();
       StringBuilder buff = new StringBuilder( 300 );
       buff.append( "Upgrade protocol 0 = " );
       buff.append( p.getID( r ).toString() );
       buff.append( " (" );
       buff.append( processor.getFullName() );
       buff.append( ")" );
       buff.append( ' ' );
       buff.append( p.getName() );
       String variantName = p.getVariantName();
       Hex customCode = p.getCustomCode( processor );
       if ( !variantName.equals( "" ) || customCode != null || altCode != null )
       {
         buff.append( ':' );
         if ( !variantName.equals( "" ) )
         {
           buff.append( variantName );
           if ( customCode != null || altCode != null )
           {
             buff.append( "-Custom" );
           }
         }
         else
         {
           buff.append( "Custom" );
         }
       }
       buff.append( " (RM " );
       buff.append( RemoteMaster.version );
       buff.append( ')' );
 
       try
       {
         BufferedReader rdr = new BufferedReader( new StringReader( code.toString( 16 ) ) );
         String line = null;
         while ( ( line = rdr.readLine() ) != null )
         {
           buff.append( "\n " );
           buff.append( line );
         }
       }
       catch ( IOException ioe )
       {
         ioe.printStackTrace( System.err );
       }
       buff.append( "\nEnd" );
       protocolText.setText( buff.toString() );
     }
     deviceUpgrade.checkSize();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed( ActionEvent e )
   {
     JTextArea area = null;
     Object source = e.getSource();
     if ( source == includeNotes )
     {
       update();
       return;
     }
     if ( source == copyDeviceUpgrade )
     {
       area = upgradeText;
     }
     else if ( source == copyProtocolUpgrade )
     {
       area = protocolText;
     }
     else
     {
       // assume copyItem
       area = popover;
     }
 
     String text = area.getText();
     StringSelection data = new StringSelection( text );
     clipboard.setContents( data, data );
   }
 
   /** the device label */
   private JLabel deviceLabel = null;
 
   /** The protocol label. */
   private JLabel protocolLabel = null;
 
   /** The upgrade text. */
   private JTextArea upgradeText = null;
 
   /** The protocol text. */
   private JTextArea protocolText = null;
 
   /** The popover. */
   private JTextArea popover = null;
 
   /** The popup. */
   private JPopupMenu popup = null;
 
   /** The copy item. */
   private JMenuItem copyItem = null;
 
   /** The copy device upgrade. */
   private JButton copyDeviceUpgrade = null;
 
   /** The copy protocol upgrade. */
   private JButton copyProtocolUpgrade = null;
 
   /** The clipboard. */
   private Clipboard clipboard = null;
 
   /** The include notes. */
   private JCheckBox includeNotes = null;
 }
