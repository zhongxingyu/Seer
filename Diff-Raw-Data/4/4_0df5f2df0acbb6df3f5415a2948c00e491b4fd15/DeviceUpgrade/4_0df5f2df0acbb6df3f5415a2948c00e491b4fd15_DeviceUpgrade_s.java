 package com.hifiremote.jp1;
 
 import java.util.*;
 import java.io.*;
 import java.text.*;
 import javax.swing.*;
 import java.awt.*;
 
 public class DeviceUpgrade
 {
   public DeviceUpgrade()
   {
     devTypeAliasName = deviceTypeAliasNames[ 0 ];
     initFunctions();
   }
 
   public void reset()
   {
     description = null;
     setupCode = 0;
 
     // remove all currently assigned functions
     remote.clearButtonAssignments();
 
     Remote[] remotes = RemoteManager.getRemoteManager().getRemotes();
     if ( remote == null )
       remote = remotes[ 0 ];
     devTypeAliasName = deviceTypeAliasNames[ 0 ];
 
 
     ProtocolManager pm = ProtocolManager.getProtocolManager();
     Vector names = pm.getNames();
     Protocol tentative = null;
     for ( Enumeration e = names.elements(); e.hasMoreElements(); )
     {
       String protocolName = ( String )e.nextElement();
       Protocol p = pm.findProtocolForRemote( remote, protocolName );
       if ( p != null )
       {
         protocol = p;
         break;
       }
     }
 
     DeviceParameter[] devParms = protocol.getDeviceParameters();
     for ( int i = 0; i < devParms.length; i++ )
       devParms[ i ].setValue( null );
 
     setProtocol( protocol );
 
     notes = null;
     file = null;
 
     functions.clear();
     initFunctions();
 
     extFunctions.clear();
   }
 
   private void initFunctions()
   {
     String[] names = KeyMapMaster.getCustomNames();
     if ( names == null )
       names = defaultFunctionNames;
     for ( int i = 0; i < names.length; i++ )
       functions.add( new Function( names[ i ]));
   }
 
   public void setDescription( String text )
   {
     description = text;
   }
 
   public String getDescription()
   {
     return description;
   }
 
   public void setSetupCode( int setupCode )
   {
     this.setupCode = setupCode;
   }
 
   public int getSetupCode()
   {
     return setupCode;
   }
 
   public void setRemote( Remote newRemote )
   {
     Protocol p = protocol;
     ProtocolManager pm = ProtocolManager.getProtocolManager();
     Vector protocols = pm.getProtocolsForRemote( newRemote, false );
     if ( !protocols.contains( p ))
     {
       System.err.println( "DeviceUpgrade.setRemote(), protocol " + p.getDiagnosticName() +
                           " is not built into remote " + newRemote.getName());
       Protocol newp = pm.findProtocolForRemote( newRemote, p.getName() );
 
       if ( newp != null )
       {
         System.err.println( "protocol " + newp.getDiagnosticName() + " will be used." );
         if ( newp != p )
         {
           System.err.println( "\tChecking for matching dev. parms" );
           DeviceParameter[] parms = p.getDeviceParameters();
           DeviceParameter[] parms2 = newp.getDeviceParameters();
 
           int[] map = new int[ parms.length ];
           boolean parmsMatch = true;
           for ( int i = 0; i < parms.length; i++ )
           {
             String name = parms[ i ].getName();
             System.err.print( "\tchecking " + name );
             boolean nameMatch = false;
             for ( int j = 0; j < parms2.length; j++ )
             {
               if ( name.equals( parms2[ j ].getName()))
               {
                 map[ i ] = j;
                 nameMatch = true;
                 System.err.print( " has a match!" );
                 break;
               }
             }
             if ( !nameMatch )
             {
               Object v = parms[ i ].getValue();
               Object d = parms[ i ].getDefaultValue();
               if ( d != null )
                d = (( DefaultValue )d ).value();
               System.err.print( " no match!" );
 
               if (( v == null ) ||
                   ( v.equals( d )))
               {
                 nameMatch = true;
                 map[ i ] = -1;
                 System.err.print( " But there's no value anyway!  " );
               }
             }
             System.err.println();
             parmsMatch = nameMatch;
             if ( !parmsMatch )
               break;
           }
           if ( parmsMatch )
           {
             // copy parameters from p to p2!
             System.err.println( "\tCopying dev. parms" );
             for ( int i = 0; i < map.length; i++ )
             {
               if ( map[ i ] == -1 )
                 continue;
               System.err.println( "\tfrom index " + i + " to index " + map[ i ]);
               parms2[ map[ i ]].setValue( parms[ i ].getValue());
             }
             System.err.println();
             System.err.println( "Setting new protocol" );
             p.convertFunctions( functions, newp );
             protocol = newp;
           }
         }
       }
       else
         JOptionPane.showMessageDialog( KeyMapMaster.getKeyMapMaster(),
                                        "The selected protocol " + p.getDiagnosticName() +
                                        "\nis not compatible with the selected remote.\n" +
                                        "This upgrade will NOT function correctly.\n" +
                                        "Please choose a different protocol.",
                                        "Error", JOptionPane.ERROR_MESSAGE );
 
     }
     if (( remote != null ) && ( remote != newRemote ))
     {
       Button[] buttons = remote.getUpgradeButtons();
       Button[] newButtons = newRemote.getUpgradeButtons();
       Vector unassigned = new Vector();
       for ( int i = 0; i < buttons.length; i++ )
       {
         Button b = buttons[ i ];
         Function f = b.getFunction();
         Function sf = b.getShiftedFunction();
         Function xf = b.getXShiftedFunction();
         if (( f != null ) || ( sf != null ) || ( xf != null ))
         {
           if ( f != null )
             b.setFunction( null );
           if ( sf != null )
             b.setShiftedFunction( null );
           if ( xf != null )
             b.setXShiftedFunction( null );
 
           Button newB = newRemote.findByStandardName( b );
           if ( newB != null )
           {
             if ( f != null )
               newB.setFunction( f );
             if ( sf != null )
               newB.setShiftedFunction( sf );
             if ( xf != null )
               newB.setXShiftedFunction( xf );
           }
           else // keep track of lost assignments
           {
             Vector temp = null;
             if ( f != null )
             {
               temp = new Vector();
               temp.add( f.getName());
               temp.add( b.getName());
               unassigned.add( temp );
             }
             if ( sf != null )
             {
               temp = new Vector();
               temp.add( sf.getName());
               temp.add( b.getShiftedName());
               unassigned.add( temp );
             }
             if ( xf != null )
             {
               temp = new Vector();
               temp.add( xf.getName());
               temp.add( b.getXShiftedName());
               unassigned.add( temp );
             }
           }
         }
       }
       if ( !unassigned.isEmpty())
       {
         String message = "Some of the functions defined in the device upgrade " +
                          "were assigned to buttons that do not match buttons on the newly selected remote. " +
                          "The functions and the corresponding button names from the original remote are listed below." +
                          "\n\nUse the Button or Layout panel to assign those functions properly.";
 
         JFrame frame = new JFrame( "Lost Function Assignments" );
         Container container = frame.getContentPane();
 
         JTextArea text = new JTextArea( message );
         text.setEditable( false );
         text.setLineWrap( true );
         text.setWrapStyleWord( true );
         text.setBackground( container.getBackground() );
         text.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
         container.add( text, BorderLayout.NORTH );
         Vector titles = new Vector();
         titles.add( "Function name" );
         titles.add( "Button name" );
         JTable table = new JTable( unassigned, titles );
         Dimension d = table.getPreferredScrollableViewportSize();
         d.height = d.height / 4;
         table.setPreferredScrollableViewportSize( d );
 
         container.add( new JScrollPane( table ), BorderLayout.CENTER );
         frame.pack();
         frame.setLocationRelativeTo( KeyMapMaster.getKeyMapMaster());
         frame.show();
       }
     }
     remote = newRemote;
   }
 
   public Remote getRemote()
   {
     return remote;
   }
 
   public void setDeviceTypeAliasName( String name )
   {
     if ( name != null )
     {
       if ( remote.getDeviceTypeByAliasName( name ) != null )
       {
         devTypeAliasName = name;
         return;
       }
       System.err.println( "Unable to find device type with alias name " + name );
     }
     devTypeAliasName = deviceTypeAliasNames[ 0 ];
   }
 
   public String getDeviceTypeAliasName()
   {
     return devTypeAliasName;
   }
 
   public DeviceType getDeviceType()
   {
     return remote.getDeviceTypeByAliasName( devTypeAliasName );
   }
 
   public void setProtocol( Protocol protocol )
   {
     this.protocol = protocol;
     parmValues = protocol.getDeviceParmValues();
   }
 
   public Protocol getProtocol()
   {
     return protocol;
   }
 
   public void setNotes( String notes )
   {
     this.notes = notes;
   }
 
   public String getNotes()
   {
     return notes;
   }
 
   public Vector getFunctions()
   {
     return functions;
   }
 
   public Function getFunction( String name )
   {
     Function rc = getFunction( name, functions );
     if ( rc == null )
       rc =  getFunction( name, extFunctions );
     return rc;
   }
 
   public Function getFunction( String name, Vector funcs )
   {
     Function rc = null;
     for ( Enumeration e = funcs.elements(); e.hasMoreElements(); )
     {
       Function func = ( Function )e.nextElement();
       String funcName = func.getName();
       if (( funcName != null ) && funcName.equalsIgnoreCase( name ))
       {
         rc = func;
         break;
       }
     }
     return rc;
   }
 
   public Vector getExternalFunctions()
   {
     return extFunctions;
   }
 
   public Vector getKeyMoves()
   {
     return keymoves;
   }
 
   public void setFile( File file )
   {
     this.file = file;
   }
 
   public File getFile(){ return file; }
 
   private int findDigitMapIndex()
   {
     Button[] buttons = remote.getUpgradeButtons();
     int[] digitMaps = remote.getDigitMaps();
     if (( digitMaps != null ) && ( protocol.getDefaultCmd().length() == 1 ))
     {
       for ( int i = 0; i < digitMaps.length; i++ )
       {
         int mapNum = digitMaps[ i ];
         if ( mapNum == 999 )
           continue;
         int[] codes = DigitMaps.data[ mapNum ];
         int rc = -1;
         for ( int j = 0; ; j++ )
         {
           Function f = buttons[ j ].getFunction();
           if (( f != null ) && !f.isExternal())
           {
             if (( f.getHex().getData()[ 0 ] & 0xFF ) == codes[ j ])
               rc = i + 1;
             else
               break;
           }
           if ( j == 9 )
           {
             return rc;
           }
         }
       }
     }
     return -1;
   }
 
   public String getUpgradeText( boolean includeNotes )
   {
     StringBuffer buff = new StringBuffer( 400 );
     buff.append( "Upgrade code 0 = " );
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     int[] id = protocol.getID( remote ).getData();
     int temp = devType.getNumber() * 0x1000 +
                ( id[ 0 ] & 1 ) * 0x0800 +
                setupCode - remote.getDeviceCodeOffset();
 
     int[] deviceCode = new int[2];
     deviceCode[ 0 ] = (temp >> 8 );
     deviceCode[ 1 ] = temp;
 
     buff.append( Hex.toString( deviceCode ));
     buff.append( " (" );
     buff.append( devTypeAliasName );
     buff.append( '/' );
     DecimalFormat df = new DecimalFormat( "0000" );
     buff.append( df.format( setupCode ));
     buff.append( ")" );
     if ( includeNotes )
     {
       String descr = "";
       if ( description != null )
         descr = description.trim();
       if ( descr.length() != 0 )
       {
         buff.append( ' ' );
         buff.append( descr );
       }
       buff.append( " (RM " );
       buff.append( KeyMapMaster.version );
       buff.append( ')' );
     }
     buff.append( "\n " );
     buff.append( Hex.asString( id[ 1 ]));
 
     int digitMapIndex = -1;
 
     if ( !remote.getOmitDigitMapByte())
     {
       buff.append( ' ' );
       digitMapIndex = findDigitMapIndex();
       if ( digitMapIndex == -1 )
         buff.append( "00" );
       else
       {
         buff.append( Hex.asString( digitMapIndex ));
       }
     }
 
     ButtonMap map = devType.getButtonMap();
     if ( map != null )
     {
       buff.append( ' ' );
       buff.append( Hex.toString( map.toBitMap( digitMapIndex != -1 )));
     }
 
     buff.append( ' ' );
     buff.append( protocol.getFixedData( parmValues ).toString());
 
     if ( map != null )
     {
       int[] data = map.toCommandList( digitMapIndex != -1 );
       if (( data != null ) && ( data.length != 0 ))
       {
         buff.append( "\n " );
         buff.append( Hex.toString( data, 16 ));
       }
     }
 
     Button[] buttons = remote.getUpgradeButtons();
     boolean hasKeyMoves = false;
     int startingButton = 0;
     int i;
     for ( i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       Function f = b.getFunction();
       Function sf = b.getShiftedFunction();
       if ( b.getShiftedButton() != null )
         sf = null;
       Function xf = b.getXShiftedFunction();
       if ( b.getXShiftedButton() != null )
         xf = null;
       if ((( f != null ) && (( map == null ) || !map.isPresent( b ) || f.isExternal())) ||
           (( sf != null ) && ( sf.getHex() != null )) || (( xf != null) && ( xf.getHex() != null )))
       {
         hasKeyMoves = true;
         break;
       }
     }
     if ( hasKeyMoves )
     {
       deviceCode[ 0 ] = ( deviceCode[ 0 ] & 0xF7 );
       buff.append( "\nKeyMoves" );
       boolean first = true;
       for ( ; i < buttons.length; i++ )
       {
         Button button = buttons[ i ];
 
         Function f = button.getFunction();
         first = appendKeyMove( buff, button.getKeyMove( f, 0, deviceCode, devType, remote ),
                                f, includeNotes, first );
         f = button.getShiftedFunction();
         if ( button.getShiftedButton() != null )
           f = null;
         first = appendKeyMove( buff, button.getKeyMove( f, remote.getShiftMask(), deviceCode, devType, remote ),
                                f, includeNotes, first );
         f = button.getXShiftedFunction();
         if ( button.getXShiftedButton() != null )
           f = null;
         first = appendKeyMove( buff, button.getKeyMove( f, remote.getXShiftMask(), deviceCode, devType, remote ),
                                f, includeNotes, first );
       }
     }
 
     buff.append( "\nEnd" );
 
     return buff.toString();
   }
 
   private boolean appendKeyMove( StringBuffer buff, int[] keyMove,Function f, boolean includeNotes, boolean first )
   {
     if (( keyMove == null ) || ( keyMove.length == 0 ))
       return first;
 
     if ( includeNotes && !first )
       buff.append( '\u00a6' );
     buff.append( "\n " );
 
     buff.append( Hex.toString( keyMove ));
 
     if ( includeNotes )
     {
       buff.append( '\u00ab' );
       buff.append( f.getName());
       String notes = f.getNotes();
       if (( notes != null ) && ( notes.length() != 0 ))
       {
         buff.append( ": " );
         buff.append( notes );
       }
       buff.append( '\u00bb' );
     }
     return false;
   }
   public void store()
     throws IOException
   {
     store( file );
   }
 
   public static String valueArrayToString( Value[] parms )
   {
     StringBuffer buff = new StringBuffer( 200 );
     for ( int i = 0; i < parms.length; i++ )
     {
       if ( i > 0 )
         buff.append( ' ' );
       buff.append( parms[ i ].getUserValue());
     }
     return buff.toString();
   }
 
   public static Value[] stringToValueArray( String str )
   {
     StringTokenizer st = new StringTokenizer( str );
     Value[] parms = new Value[ st.countTokens()];
     for ( int i = 0; i < parms.length; i++ )
     {
       String token = st.nextToken();
       Object val = null;
       if ( !token.equals( "null" ))
       {
         if ( token.equals( "true" ))
           val = new Integer( 1 );
         else if ( token.equals( "false" ))
           val = new Integer( 0 );
         else
           val = new Integer( token );
       }
       parms[ i ] = new Value( val, null );
     }
     return parms;
   }
 
   public void store( File file )
     throws IOException
   {
     this.file = file;
     PropertyWriter out =
       new PropertyWriter( new PrintWriter( new FileWriter( file )));
 
     if ( description != null )
       out.print( "Description", description );
     out.print( "Remote.name", remote.getName());
     out.print( "Remote.signature", remote.getSignature());
     out.print( "DeviceType", devTypeAliasName );
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     out.print( "DeviceIndex", Integer.toHexString( devType.getNumber()));
     out.print( "SetupCode", Integer.toString( setupCode ));
 //    protocol.setDeviceParms( parmValues );
     protocol.store( out, parmValues );
     if ( notes != null )
       out.print( "Notes", notes );
     int i = 0;
     for ( Enumeration e = functions.elements(); e.hasMoreElements(); i++ )
     {
       Function func = ( Function )e.nextElement();
       func.store( out, "Function." + i );
     }
     i = 0;
     for ( Enumeration e = extFunctions.elements(); e.hasMoreElements(); i++ )
     {
       ExternalFunction func = ( ExternalFunction )e.nextElement();
       func.store( out, "ExtFunction." + i );
     }
     Button[] buttons = remote.getUpgradeButtons();
     String regex = "\\|";
     String replace = "\\\\u007c";
     for ( i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       Function f = b.getFunction();
 
       String fstr;
       if ( f == null )
         fstr = "null";
       else
         fstr = f.getName().replaceAll( regex, replace );
 
       Function sf = b.getShiftedFunction();
       String sstr;
       if ( sf == null )
         sstr = "null";
       else
         sstr = sf.getName().replaceAll( regex, replace );
 
       Function xf = b.getXShiftedFunction();
       String xstr;
       if ( xf == null )
         xstr = null;
       else
         xstr = xf.getName().replaceAll( regex, replace );
       if (( f != null ) || ( sf != null ) || ( xf != null ))
       {
         out.print( "Button." + Integer.toHexString( b.getKeyCode()),
                            fstr + '|' + sstr + '|' + xstr );
       }
 
     }
     out.flush();
     out.close();
   }
 
   public void load( File file )
     throws Exception
   {
     load( file, true );
   }
 
   public void load( File file, boolean loadButtons )
     throws Exception
   {
     if ( file.getName().toLowerCase().endsWith( ".rmdu" ))
       this.file = file;
     BufferedReader reader = new BufferedReader( new FileReader( file ));
     load( reader, loadButtons );
   }
 
   public void load( BufferedReader reader )
     throws Exception
   {
     load( reader, true );
   }
 
   public void load( BufferedReader reader, boolean loadButtons )
     throws Exception
   {
     reader.mark( 160 );
     String line = reader.readLine();
     reader.reset();
     if ( line.startsWith( "Name:" ))
     {
       importUpgrade( reader, loadButtons );
       return;
     }
 
     Properties props = new Properties();
     Property property = new Property();
     PropertyReader pr = new PropertyReader( reader );
     while (( property = pr.nextProperty( property )) != null )
     {
       props.put( property.name, property.value );
     }
     reader.close();
 
     String str = props.getProperty( "Description" );
     if ( str != null )
       description = str;
     str = props.getProperty( "Remote.name" );
     if ( str == null )
     {
       JOptionPane.showMessageDialog( KeyMapMaster.getKeyMapMaster(),
                                      "The upgrade you are trying to import is not valid!  It does not contain a value for Remote.name",
                                      "Import Failure", JOptionPane.ERROR_MESSAGE );
       return;
     }
     String sig = props.getProperty( "Remote.signature" );
     remote = RemoteManager.getRemoteManager().findRemoteByName( str );
     remote.load();
     int index = -1;
     str = props.getProperty( "DeviceIndex" );
     if ( str != null )
       index = Integer.parseInt( str, 16 );
     setDeviceTypeAliasName( props.getProperty( "DeviceType" ) );
     setupCode = Integer.parseInt( props.getProperty( "SetupCode" ));
 
     Hex pid = new Hex( props.getProperty( "Protocol", "0200" ));
     String name = props.getProperty( "Protocol.name", "" );
     String variantName = props.getProperty( "Protocol.variantName", "" );
 
     ProtocolManager pm = ProtocolManager.getProtocolManager();
     if ( name.equals( "Manual Settings" ) ||
          name.equals( "Manual" ) ||
          name.equals( "PID " + pid.toString()))
     {
       protocol = new ManualProtocol( pid, props );
       pm.add( protocol );
     }
     else
     {
       protocol = pm.findNearestProtocol( name, pid, variantName );
 
       if ( protocol == null )
       {
         JOptionPane.showMessageDialog( KeyMapMaster.getKeyMapMaster(),
                                        "No protocol found with name=\"" + name +
                                        "\", ID=" + pid.toString() +
                                        ", and variantName=\"" + variantName + "\"",
                                        "File Load Error", JOptionPane.ERROR_MESSAGE );
         return;
       }
     }
 
     str = props.getProperty( "ProtocolParms" );
     System.err.println( "ProtocolParms='" + str + "'" );
     if (( str != null ) && ( str.length() != 0 ))
     {
       protocol.setDeviceParms( stringToValueArray( str ));
       parmValues = protocol.getDeviceParmValues();
     }
 
     protocol.setProperties( props );
 
     notes = props.getProperty( "Notes" );
 
     functions.clear();
     int i = 0;
     while ( true )
     {
       Function f = new Function();
       f.load( props, "Function." + i );
       if ( f.isEmpty())
       {
         break;
       }
       functions.add( f );
       i++;
     }
 
     extFunctions.clear();
     i = 0;
     while ( true )
     {
       ExternalFunction f = new ExternalFunction();
       f.load( props, "ExtFunction." + i, remote );
       if ( f.isEmpty())
       {
         break;
       }
       extFunctions.add( f );
       i++;
     }
 
     if ( loadButtons )
     {
       Button[] buttons = remote.getUpgradeButtons();
       String regex = "\\\\u007c";
       String replace = "|";
       for ( i = 0; i < buttons.length; i++ )
       {
         Button b = buttons[ i ];
         str = props.getProperty( "Button." + Integer.toHexString( b.getKeyCode()));
         if ( str == null )
         {
           continue;
         }
         StringTokenizer st = new StringTokenizer( str, "|" );
         str = st.nextToken();
         Function func = null;
         if ( !str.equals( "null" ))
         {
           func = getFunction( str.replaceAll( regex, replace ));
           b.setFunction( func );
         }
         str = st.nextToken();
         if ( !str.equals( "null" ))
         {
           func = getFunction( str.replaceAll( regex, replace ));
           b.setShiftedFunction( func );
         }
         if ( st.hasMoreTokens())
         {
           str = st.nextToken();
           if ( !str.equals( "null" ))
           {
             func = getFunction( str.replaceAll( regex, replace ));
             b.setXShiftedFunction( func );
           }
         }
       }
     }
   }
 
   private String getNextField( StringTokenizer st, String delim )
   {
     String rc = null;
     if ( st.hasMoreTokens())
     {
       rc = st.nextToken();
       if ( rc.equals( delim ))
         rc = null;
       else
       {
         if ( rc.startsWith( "\"" ))
         {
           if ( rc.endsWith( "\"" ))
           {
             rc = rc.substring( 1, rc.length() - 1 ).replaceAll( "\"\"", "\"" );
           }
           else
           {
             StringBuffer buff = new StringBuffer( 200 );
             buff.append( rc.substring( 1 ));
             while ( true )
             {
               String token = st.nextToken(); // skip delim
               buff.append( delim );
               token = st.nextToken();
               if ( token.endsWith( "\"" ))
               {
                 buff.append( token.substring( 0, token.length() - 1 ));
                 break;
               }
               else
                 buff.append( token );
             }
             rc = buff.toString().replaceAll( "\"\"", "\"" );
           }
         }
         if ( st.hasMoreTokens())
           st.nextToken(); // skip delim
       }
     }
     return rc;
   }
 
   public void importUpgrade( BufferedReader in )
     throws Exception
   {
     importUpgrade( in, true );
   }
 
 
   public void importUpgrade( BufferedReader in, boolean loadButtons )
     throws Exception
   {
     String line = in.readLine(); // line 1
     String token = line.substring( 0, 5 );
     if ( !token.equals( "Name:" ))
     {
       JOptionPane.showMessageDialog( KeyMapMaster.getKeyMapMaster(),
                                      "The upgrade you are trying to import is not valid!",
                                      "Import Failure", JOptionPane.ERROR_MESSAGE );
       return;
     }
     String delim = line.substring( 5, 6 );
     StringTokenizer st = new StringTokenizer( line, delim, true );
     getNextField( st, delim );
     description = getNextField( st, delim );
 
     String protocolLine = in.readLine(); // line 3
     String manualLine = in.readLine(); // line 4
 
     line = in.readLine(); // line 5
     st = new StringTokenizer( line, delim );
     st.nextToken();
     token = st.nextToken();
     setupCode = Integer.parseInt( token );
     token = st.nextToken();
     String str = token.substring( 5 );
 
     remote = RemoteManager.getRemoteManager().findRemoteByName( str );
     Hex pid = null;
     while ( true )
     {
       line = in.readLine();
       if (( line != null ) && ( line.length() > 0 ) && ( line.charAt( 0 ) == '\"' ))
         line = line.substring( 1 );
       int equals = line.indexOf( '=' );
       if (( equals != -1 ) && line.substring( 0, equals ).toLowerCase().startsWith( "upgrade code " ))
       {
         int[] id = new int[ 2 ];
         int temp = Integer.parseInt( line.substring( equals + 2, equals + 4 ), 16 );
         if (( temp & 8 ) != 0 )
           id[ 0 ] = 1;
 
         line = in.readLine();
         temp = Integer.parseInt( line.substring( 0, 2 ), 16 );
         id[ 1 ] = temp;
         pid = new Hex( id );
         break;
       }
     }
 
     remote.load();
     token = st.nextToken();
     str = token.substring( 5 );
 
     if ( remote.getDeviceTypeByAliasName( str ) == null )
     {
       String rc = null;
       String msg = "Remote \"" + remote.getName() + "\" does not support the device type " +
       str + ".  Please select one of the supported device types below to use instead.\n";
       while ( rc == null )
       {
         rc = ( String )JOptionPane.showInputDialog( KeyMapMaster.getKeyMapMaster(),
                                                     msg,
                                                     "Unsupported Device Type",
                                                     JOptionPane.ERROR_MESSAGE,
                                                     null,
                                                     remote.getDeviceTypeAliasNames(),
                                                     null );
       }
       str = rc;
     }
     setDeviceTypeAliasName( str );
 
     String buttonStyle = st.nextToken();
     st = new StringTokenizer( protocolLine, delim, true );
     getNextField( st, delim ); // skip header
     String protocolName = getNextField( st, delim );  // protocol name
 
     ProtocolManager protocolManager = ProtocolManager.getProtocolManager();
     if ( protocolName.equals( "Manual Settings" ))
     {
       System.err.println( "protocolName=" + protocolName );
       System.err.println( "manualLine=" + manualLine );
       StringTokenizer manual = new StringTokenizer( manualLine, delim, true );
       System.err.println( "skipping " + getNextField( manual, delim )); // skip header
       String pidStr = getNextField( manual, delim );
       System.err.println( "pid=" + pidStr );
       if ( pidStr != null )
       {
         int space = pidStr.indexOf( ' ' );
         if ( space != -1 )
         {
           pid = new Hex( pidStr );
         }
         else
         {
           int pidInt = Integer.parseInt( pidStr, 16 );
           int[] data = new int[ 2 ];
           data[ 0 ] = ( pidInt & 0xFF00 ) >> 8;
           data[ 1 ] = pidInt & 0xFF;
           pid = new Hex( data );
         }
       }
       int byte2 = Integer.parseInt( getNextField( manual, delim ).substring( 0, 1 ));
       System.err.println( "byte2=" +  byte2 );
       String signalStyle = getNextField( manual, delim );
       System.err.println( "SignalStyle=" + signalStyle );
       String bitsStr = getNextField( manual, delim );
       int devBits = Integer.parseInt( bitsStr.substring( 0, 1 ), 16);
       int cmdBits = Integer.parseInt( bitsStr.substring( 1 ), 16 );
       System.err.println( "devBits=" + devBits + " and cmdBits=" + cmdBits );
 
       Vector values = new Vector();
 
       str = getNextField( st, delim ); // Device 1
       if ( str != null )
         values.add( new Integer( str ));
 
       str = getNextField( st, delim ); // Device 2
       if ( str != null )
         values.add( new Integer( str ));
 
       str = getNextField( st, delim ); // Device 3
       if ( str != null )
         values.add( new Integer( str ));
 
       str = getNextField( st, delim ); // Raw Fixed Data
       if ( str == null )
         str = "";
       int[] rawHex = Hex.parseHex( str );
 
       protocol = new ManualProtocol( protocolName, pid, byte2, signalStyle, devBits, values, rawHex, cmdBits );
       protocolName = protocol.getName();
       setParmValues( protocol.getDeviceParmValues());
       protocolManager.add( protocol );
     }
     else
     {
 //    protocol = protocolManager.findProtocolForRemote( remote, protocolName );
       Protocol p = protocolManager.findNearestProtocol( protocolName, pid, null );
 
       if ( p == null )
       {
         p = protocolManager.findProtocolByOldName( remote, protocolName, pid );
 
         if ( p == null )
         {
           JOptionPane.showMessageDialog( KeyMapMaster.getKeyMapMaster(),
                                          "No protocol found with name=\"" + protocolName +
                                          "\" for remote \"" + remote.getName() + "\".",
                                          "Import Failure", JOptionPane.ERROR_MESSAGE );
           reset();
           return;
         }
       }
       protocol = p;
 
       Value[] importParms = new Value[ 4 ];
       for ( int i = 0; i < importParms.length; i++ )
       {
         token = getNextField( st, delim );
         Object val = null;
         if ( token == null )
           val = null;
         else
         {
           if ( token.equals( "true" ))
             val = new Integer( 1 );
           else if ( token.equals( "false" ))
             val = new Integer( 0 );
           else
             val = token;
 //            val = new Integer( token );
         }
         importParms[ i ] = new Value( val );
       }
       protocol.importDeviceParms( importParms );
       parmValues = protocol.getDeviceParmValues();
     }
 
     // compute cmdIndex
     boolean useOBC = false;
     boolean useEFC = false;
     if ( buttonStyle.equals( "OBC" ))
       useOBC = true;
     else if ( buttonStyle.equals( "EFC" ))
       useEFC = true;
 
     int obcIndex = -1;
     CmdParameter[] cmdParms = protocol.getCommandParameters();
     for ( obcIndex = 0; obcIndex < cmdParms.length; obcIndex++ )
     {
       if ( cmdParms[ obcIndex ].getName().equals( "OBC" ))
         break;
     }
 
     String match1 = "fFunctions" + delim;
     String match2 = "Functions" + delim;
     if ( useOBC )
     {
       match1 = match1 + "fOBC" + delim;
       match2 = match2 + "OBC" + delim;
     }
     else
     {
       match1 = match1 + "fEFC" + delim;
       match2 = match2 + "EFC" + delim;
     }
 
     while ( true )
     {
       line = in.readLine();
       if ( line.startsWith( match1 ) || line.startsWith( match2 ))
         break;
     }
 
     functions.clear();
 
     DeviceCombiner combiner = null;
     if ( protocol.getClass() == DeviceCombiner.class )
       combiner = ( DeviceCombiner )protocol;
     Vector unassigned = new Vector();
     Vector usedFunctions = new Vector();
     for ( int i = 0; i < 128; i++ )
     {
       line = in.readLine();
       st = new StringTokenizer( line, delim, true );
       token = getNextField( st, delim ); // get the name (field 1)
       if (( token != null ) && ( token.length() == 5 ) &&
           token.startsWith( "num " ) && Character.isDigit( token.charAt( 4 )))
         token = token.substring( 4 );
 
       System.err.println( "Looking for function " + token );
       Function f = getFunction( token, usedFunctions );
       if ( f == null )
       {
         System.err.println( "Had to create a new one!" );
         if (( token != null ) && ( token.charAt( 0 ) == '=' ) && ( token.indexOf( '/' ) != -1 ))
           f = new ExternalFunction();
         else
           f = new Function();
         f.setName( token );
       }
       else
         System.err.println( "Found it!" );
 
       token = getNextField( st, delim );  // get the function code (field 2)
       if ( token != null )
       {
         Hex hex = null;
         if ( f.isExternal())
         {
           ExternalFunction ef = ( ExternalFunction )f;
           String name = ef.getName();
           int slash = name.indexOf( '/' );
           String devName = name.substring( 1, slash );
           ef.setDeviceTypeAliasName( devName );
           int space = name.indexOf( ' ', slash + 1 );
           String codeString = null;
           if ( space == -1 )
             codeString = name.substring( slash + 1 );
           else
             codeString = name.substring( slash + 1, space );
           ef.setSetupCode( Integer.parseInt( codeString ));
          if ( token.startsWith( "h") || token.endsWith( "h") || (token.indexOf( ' ' ) != -1 ))
           {
             hex = new Hex( token );
             ef.setType( ExternalFunction.HexType );
           }
           else
           {
             hex = new Hex( 1 );
             protocol.efc2hex( new EFC( token ), hex, 0 );
             ef.setType( ExternalFunction.EFCType );
           }
         }
         else
         {
           hex = protocol.getDefaultCmd();
           protocol.importCommand( hex, token, useOBC, obcIndex, useEFC );
           
           token = getNextField( st, delim ); // get byte2 (field 3)
           if ( token != null )
             protocol.importCommandParms( hex, token );
         }
         f.setHex( hex );
       }
       else
       {
         token = getNextField( st, delim ); // skip field 3
       }
       String actualName = getNextField( st, delim ); // get assigned button name (field 4)
       System.err.println( "actualName='" + actualName + "'" );
 
       if (( actualName != null ) && actualName.length() == 0 )
         actualName = null;
       String buttonName = null;
       if ( actualName != null )
       {
         if ( i < genericButtonNames.length )
           buttonName = genericButtonNames[ i ];
         else
         {
           System.err.println( "No generic name available!" );
           Button b = remote.getButton( actualName );
           if ( b == null )
             b = remote.getButton( actualName.replace( ' ', '_' ));
           if ( b != null )
             buttonName = b.getStandardName();
         }
       }
 
       Button b = null;
       if ( buttonName != null )
       {
         System.err.println( "Searching for button w/ name " + buttonName );
         b = remote.findByStandardName( new Button( buttonName, null, ( byte )0, remote ));
         System.err.println( "Found button " + b );
       }
       else
         System.err.println( "No buttonName for actualName=" + actualName + " and i=" + i );
 
       token = getNextField( st, delim );  // get normal function (field 5)
       if (( buttonName != null ) && ( token != null ) &&
            Character.isDigit( token.charAt( 0 )) &&
            Character.isDigit( token.charAt( 1 )) &&
            ( token.charAt( 2 ) == ' ' ) &&
            ( token.charAt( 3 ) == '-' ) &&
            ( token.charAt( 4 ) == ' ' ))
       {
         String name = token.substring( 5 );
         if (( name.length() == 5 ) && name.startsWith( "num " ) &&
               Character.isDigit( name.charAt( 4 )))
           name = name.substring( 4 );
 
         Function func = null;
         if (( f.getName() != null ) && f.getName().equalsIgnoreCase( name ))
           func = f;
         else
         {
           func = getFunction( name, functions );
           if ( func == null )
             func = getFunction( name, usedFunctions );
         }
         if ( func == null )
         {
           System.err.println( "Creating new function " + name );
           if (( name.charAt( 0 ) == '=' ) && ( name.indexOf( '/' ) != -1 ))
             func = new ExternalFunction();
           else
             func = new Function();
           func.setName( name );
           if ( b != null )
             usedFunctions.add( func );
         }
         else
           System.err.println( "Found function " + name );
 
         if ( b == null )
         {
           Vector temp = new Vector( 2 );
           temp.add( name );
           temp.add( actualName );
           unassigned.add( temp );
           System.err.println( "Couldn't find button " + buttonName + " to assign function " + name );
         }
         else if ( loadButtons )
         {
           System.err.println( "Setting function " + name + " on button " + b );
           b.setFunction( func );
         }
       }
 
       token = getNextField( st, delim );  // get notes (field 6)
       if ( token != null )
         f.setNotes( token );
 
       if ( !f.isEmpty())
       {
         if ( f.isExternal())
           extFunctions.add( f );
         else
           functions.add( f );
       }
 
       String pidStr = getNextField( st, delim ); // field 7
       String fixedDataStr = getNextField( st, delim ); // field 8
 
       if (( combiner != null ) && ( pidStr != null ) && // ( fixedDataStr != null ) &&
           !pidStr.equals( "Protocol ID" )) // && !fixedDataStr.equals( "Fixed Data" )
       {
         Hex fixedData = new Hex();
         if ( fixedDataStr != null )
           fixedData = new Hex( fixedDataStr );
 
         Hex newPid = new Hex( pidStr );
         Vector protocols = protocolManager.findByPID( newPid );
         boolean foundMatch = false;
         for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
         {
           Protocol p = ( Protocol )e.nextElement();
           if ( !remote.supportsVariant( newPid, p.getVariantName()))
             continue;
           CombinerDevice dev = new CombinerDevice( p, fixedData );
           Hex calculatedFixedData = dev.getFixedData();
           if ( !calculatedFixedData.equals( fixedData ))
             continue;
           combiner.add( dev );
           foundMatch = true;
           break;
         }
 
         if ( !foundMatch )
         {
           ManualProtocol p = new ManualProtocol( newPid, new Properties());
           p.setRawHex( fixedData );
           combiner.add( new CombinerDevice( p, null, null ));
         }
       }
 
       // skip to field 13
       for ( int j = 8; j < 13; j++ )
         token = getNextField( st, delim );
 
       if ( token != null )
       {
         String name = token.substring( 5 );
         if (( name.length() == 5 ) && name.startsWith( "num " ) &&
               Character.isDigit( token.charAt( 4 )))
           name = name.substring( 4 );
         Function func = getFunction( name, functions );
         if ( func == null )
           func = getFunction( name, extFunctions );
         if ( func == null )
         {
           if (( name.charAt( 0 ) == '=' ) && ( name.indexOf( '/' ) != -1 ))
             func = new ExternalFunction();
           else
             func = new Function();
           func.setName( name );
           usedFunctions.add( func );
         }
         if ( b == null )
         {
           Vector temp = new Vector( 2 );
           temp.add( name );
           temp.add( "shift-" + buttonName );
           unassigned.add( temp );
         }
         else if ( loadButtons )
           b.setShiftedFunction( func );
       }
     }
 
     while (( line = in.readLine()) != null )
     {
       st = new StringTokenizer( line, delim );
       token = getNextField( st, delim );
       if ( token != null )
       {
         if ( token.equals( "Line Notes:" ) || token.equals( "Notes:" ))
         {
           StringBuffer buff = new StringBuffer();
           boolean first = true;
           String tempDelim = null;
           while (( line = in.readLine()) != null )
           {
             if ( line.charAt( 0 ) == '"' )
               tempDelim = "\"";
             else
               tempDelim = delim;
             st = new StringTokenizer( line, tempDelim );
             if ( st.hasMoreTokens())
             {
               token = st.nextToken();
               if ( token.startsWith( "EOF Marker" ))
                 break;
               if ( first )
                 first = false;
               else
                 buff.append( "\n" );
               buff.append( token.trim());
             }
             else
               buff.append( "\n" );
           }
           notes = buff.toString().trim();
           if ( protocol.getClass() == ManualProtocol.class )
             protocol.importUpgradeCode( notes );
         }
       }
     }
     if ( !unassigned.isEmpty())
     {
       System.err.println( "Removing undefined functions from usedFunctions" );
       for( ListIterator i = unassigned.listIterator(); i.hasNext(); )
       {
         Vector temp = ( Vector )i.next();
         String funcName = ( String )temp.elementAt( 0 );
         System.err.print( "Checking '" + funcName + "'" );
         Function f = getFunction( funcName, usedFunctions );
         if (( f == null ) || ( f.getHex() == null ) || ( f.getHex().length() == 0 ))
         {
           System.err.println( "Removing function " + f + ", which has name '" + funcName + "'" );
           i.remove();
         }
       }
     }
     if ( !unassigned.isEmpty())
     {
       String message = "Some of the functions defined in the imported device upgrade " +
                        "were assigned to buttons that could not be matched by name. " +
                        "The functions and the corresponding button names are listed below." +
                        "\n\nPlease post this information in the \"JP1 - Software\" section of the " +
                        "JP1 Forums at www.hifi-remote.com" +
                        "\n\nUse the Button or Layout panel to assign those functions properly.";
 
       JFrame frame = new JFrame( "Import Failure" );
       Container container = frame.getContentPane();
 
       JTextArea text = new JTextArea( message );
       text.setEditable( false );
       text.setLineWrap( true );
       text.setWrapStyleWord( true );
       text.setBackground( container.getBackground() );
       text.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
       container.add( text, BorderLayout.NORTH );
       Vector titles = new Vector();
       titles.add( "Function name" );
       titles.add( "Button name" );
       JTable table = new JTable( unassigned, titles );
       Dimension d = table.getPreferredScrollableViewportSize();
       d.height = d.height / 4;
       table.setPreferredScrollableViewportSize( d );
 
       container.add( new JScrollPane( table ), BorderLayout.CENTER );
       frame.pack();
       frame.setLocationRelativeTo( KeyMapMaster.getKeyMapMaster());
       frame.show();
     }
     Button[] buttons = remote.getUpgradeButtons();
     System.err.println( "Removing assigned functions with no hex!" );
     for ( int i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       Function f = b.getFunction();
       if (( f != null ) && ( f.getHex() == null ))
         b.setFunction( null );
       f = b.getShiftedFunction();
       if (( f != null ) && ( f.getHex() == null ))
         b.setShiftedFunction( null );
       f = b.getXShiftedFunction();
       if (( f != null ) && ( f.getHex() == null ))
         b.setXShiftedFunction( null );
     }
     System.err.println( "Done!" );
   }
 
   public Value[] getParmValues()
   {
     return parmValues;
   }
 
   public void setParmValues( Value[] parmValues )
   {
     this.parmValues = parmValues;
   }
 
   public static final String[] getDeviceTypeAliasNames()
   {
     return deviceTypeAliasNames;
   }
 
   public void autoAssignFunctions()
   {
     autoAssignFunctions( functions );
     autoAssignFunctions( extFunctions );
   }
 
   private void autoAssignFunctions( Vector funcs )
   {
     Button[] buttons = remote.getUpgradeButtons();
     for ( Enumeration e = funcs.elements(); e.hasMoreElements(); )
     {
       Function func = ( Function )e.nextElement();
       if ( func.getHex() != null )
       {
         for ( int i = 0; i < buttons.length; i++ )
         {
           Button b = buttons[ i ];
           if ( b.getFunction() == null )
           {
             if ( b.getName().equalsIgnoreCase( func.getName()) ||
                  b.getStandardName().equalsIgnoreCase( func.getName()))
             {
               b.setFunction( func );
               break;
             }
           }
         }
       }
     }
   }
 
   private String description = null;
   private int setupCode = 0;
   private Remote remote = null;
   private String devTypeAliasName = null;
   private Protocol protocol = null;
   private Value[] parmValues = new Value[ 0 ];
   private String notes = null;
   private Vector functions = new Vector();
   private Vector extFunctions = new Vector();
   private Vector keymoves = new Vector();
   private File file = null;
 
   private static final String[] deviceTypeAliasNames =
   {
     "Cable", "TV", "VCR", "CD", "Tuner", "DVD", "SAT", "Tape", "Laserdisc",
     "DAT", "Home Auto", "Misc Audio", "Phono", "Video Acc", "Amp", "PVR", "OEM Mode"
   };
 
   private static final String[] defaultFunctionNames =
   {
     "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
     "vol up", "vol down", "mute",
     "channel up", "channel down",
     "power", "enter", "tv/vcr",
     "last (prev ch)", "menu", "program guide", "up arrow", "down arrow",
     "left arrow", "right arrow", "select", "sleep", "pip on/off", "display",
     "pip swap", "pip move", "play", "pause", "rewind", "fast fwd", "stop",
     "record", "exit", "surround", "input toggle", "+100", "fav/scan",
     "device button", "next track", "prev track", "shift-left", "shift-right",
     "pip freeze", "slow", "eject", "slow+", "slow-", "X2", "center", "rear"
   };
 
   private final static String[] genericButtonNames =
   {
     "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
     "vol up", "vol down", "mute",
     "channel up", "channel down",
     "power", "enter", "tv/vcr", "prev ch", "menu", "guide",
     "up arrow", "down arrow", "left arrow", "right arrow", "select",
     "sleep", "pip on/off", "display", "pip swap", "pip move",
     "play", "pause", "rewind", "fast fwd", "stop", "record",
     "exit", "surround", "input", "+100", "fav/scan",
     "device button", "next track", "prev track", "shift-left", "shift-right",
     "pip freeze", "slow", "eject", "slow+", "slow-", "x2", "center", "rear",
     "phantom1", "phantom2", "phantom3", "phantom4", "phantom5", "phantom6",
     "phantom7", "phantom8", "phantom9", "phantom10",
     "setup", "light", "theater",
     "macro1", "macro2", "macro3", "macro4",
     "learn1", "learn2", "learn3", "learn4" // ,
 //    "button85", "button86", "button87", "button88", "button89", "button90",
 //    "button91", "button92", "button93", "button94", "button95", "button96",
 //    "button97", "button98", "button99", "button100", "button101", "button102",
 //    "button103", "button104", "button105", "button106", "button107", "button108",
 //    "button109", "button110", "button112", "button113", "button114", "button115",
 //    "button116", "button117", "button118", "button119", "button120", "button121",
 //    "button122", "button123", "button124", "button125", "button126", "button127",
 //    "button128", "button129", "button130", "button131", "button132", "button133",
 //    "button134", "button135", "button136"
   };
 }
