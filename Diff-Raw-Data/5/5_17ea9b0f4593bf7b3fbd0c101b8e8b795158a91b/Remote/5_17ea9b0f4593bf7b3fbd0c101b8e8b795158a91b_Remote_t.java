 package com.hifiremote.jp1;
 
 import java.awt.*;
 import java.awt.geom.*;
 import java.lang.reflect.*;
 import java.util.*;
 import java.io.*;
 import javax.swing.*;
 
 public class Remote
   implements Comparable
 {
   public Remote( Remote aRemote, int index )
   {
     this.file = aRemote.file;
     this.signature = aRemote.signature;
     supportsBinaryUpgrades = aRemote.supportsBinaryUpgrades;
     this.names = aRemote.names;
     nameIndex = index;
   }
 
   public Remote( File rdf )
   {
     file = rdf;
     String rdfName = rdf.getName();
     StringTokenizer st = new StringTokenizer( rdfName );
     signature = st.nextToken(); // upto the 1st space
     supportsBinaryUpgrades = signature.startsWith( "BIN" );
     int openParen = rdfName.indexOf( '(' );
     int closeParen = rdfName.lastIndexOf( ')' );
     String name = rdfName.substring( openParen + 1, closeParen );
     st = new StringTokenizer( name, " -", true );
     String prefix = "";
     String postfix = "";
     String[] middles = null;
     boolean foundUnderscore = false;
     while ( st.hasMoreTokens())
     {
       String token = st.nextToken();
       if (( token.length() > 3 ) && ( token.indexOf( '_' ) != -1 ))
       {
         foundUnderscore = true;
         StringTokenizer st2 = new StringTokenizer( token, "_" );
         middles = new String[ st2.countTokens() ];
         for ( int i = 0; i < middles.length; i++ )
           middles[ i ] = st2.nextToken();
       }
       else
       {
         token = token.replace( '_', '/' );
         if ( foundUnderscore )
           postfix = postfix + token;
         else
           prefix = prefix + token;
       }
     }
     if ( middles == null )
     {
       names[ 0 ] = prefix;
     }
     else
     {
       names = new String[ middles.length ];
       for ( int i = 0; i < middles.length; i++ )
       {
          if ( middles[ i ].length() < middles[ 0 ].length() )
            names[ i ] = middles[ i ] + postfix;
          else
            names[ i ] = prefix + middles[ i ] + postfix;
       }
     }
   }
 
   public File getFile(){ return file; }
 
   public void load()
 //    throws Exception
   {
     try
     {
       if ( loaded )
         return;
       loaded = true;
       RDFReader rdr = new RDFReader( file );
       String line = rdr.readLine();
       while ( line != null )
       {
         if ( line.length() == 0 )
         {
           line = rdr.readLine();
         }
         else if ( line.charAt( 0 ) == '[' )
         {
           StringTokenizer st = new StringTokenizer( line, "[]" );
           line = st.nextToken();
 
           if ( line.equals( "General" ))
             line = parseGeneralSection( rdr );
           else if ( line.equals( "SpecialProtocols" ))
             line = parseSpecialProtocols( rdr );
           else if ( line.equals( "Checksums" ))
             line = parseCheckSums( rdr );
           else if ( line.equals( "Settings" ))
             line = parseSettings( rdr );
           else if ( line.equals( "FixedData" ))
             line = parseFixedData( rdr );
           else if ( line.equals( "DeviceButtons" ))
             line = parseDeviceButtons( rdr );
           else if ( line.equals( "DigitMaps" ))
             line = parseDigitMaps( rdr );
           else if ( line.equals( "DeviceTypes" ))
             line = parseDeviceTypes( rdr );
           else if ( line.equals( "DeviceAbbreviations" ))
             line = parseDeviceAbbreviations( rdr );
           else if ( line.equals( "DeviceTypeAliases" ))
             line = parseDeviceTypeAliases( rdr );
           else if ( line.equals( "DeviceTypeImageMaps" ))
             line = parseDeviceTypeImageMaps( rdr );
           else if ( line.equals( "Buttons" ))
             line = parseButtons( rdr );
           else if ( line.equals( "MultiMacros" ))
             line = parseMultiMacros( rdr );
           else if ( line.equals( "ButtonMaps" ))
             line = parseButtonMaps( rdr );
           else if ( line.equals( "Protocols" ))
             line = parseProtocols( rdr );
           else
             line = rdr.readLine();
         }
         else
           line = rdr.readLine();
       }
       rdr.close();
 
       if ( buttonMaps.length == 0 )
       {
         System.err.println( "ERROR: " + file.getName() + " does not specify any ButtonMaps!" );
         buttonMaps = new ButtonMap[ 1 ];
         buttonMaps[ 0 ] = new ButtonMap( 0, new short[ 0 ][ 0 ]);
       }
       for ( int i = 0; i < buttonMaps.length; i++ )
         buttonMaps[ i ].setButtons( this );
 
       for ( Enumeration< DeviceType> e = deviceTypes.elements(); e.hasMoreElements(); )
       {
         DeviceType type = e.nextElement();
         int map = type.getMap();
         if ( map == -1 )
           System.err.println( "ERROR:" + file.getName() + ": DeviceType " + type.getName() + " doesn't have a map." );
         if ( map >= buttonMaps.length )
         {
           System.err.println( "ERROR:" + file.getName() + ": DeviceType " + type.getName() + " uses an undefined map index." );
           map = buttonMaps.length - 1;
         }
         if (( map != -1 ) && ( buttonMaps.length > 0 ))
           type.setButtonMap( buttonMaps[ map ] );
       }
 
       if ( deviceTypeAliasNames == null )
       {
         java.util.List<String> v = new ArrayList<String>();
         DeviceType vcrType = null;
         boolean hasPVRalias = false;
         for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
         {
           DeviceType type = ( DeviceType )e.nextElement();
 
           String typeName = type.getName();
           if ( typeName.startsWith( "VCR" ))
             vcrType = type;
           if ( typeName.equals( "PVR" ))
             hasPVRalias = true;
           deviceTypeAliases.put( typeName, type );
           v.add( typeName );
         }
         if ( !hasPVRalias )
         {
           v.add( "PVR" );
           deviceTypeAliases.put( "PVR", vcrType );
         }
         deviceTypeAliasNames = new String[ 0 ];
         deviceTypeAliasNames = ( String[] )v.toArray( deviceTypeAliasNames );
         Arrays.sort( deviceTypeAliasNames );
       }
 
       // find the longest button map
       ButtonMap longestMap = null;
       for ( Enumeration< DeviceType> e = deviceTypes.elements(); e.hasMoreElements(); )
       {
         DeviceType type = e.nextElement();
         ButtonMap thisMap = type.getButtonMap();
         if (( longestMap == null ) || ( longestMap.size() < thisMap.size() ))
           longestMap = thisMap;
       }
 
       // Now figure out which buttons are bindable
       java.util.List< Button > bindableButtons = new ArrayList< Button >();
 
       // first copy the bindable buttons from the longest map
       int index = 0;
       while ( index < longestMap.size())
       {
         Button b = longestMap.get( index++ );
         if ( b.allowsKeyMove() || b.allowsShiftedKeyMove() ||
              b.allowsXShiftedKeyMove())
           bindableButtons.add( b );
       }
 
       // now copy the rest of the bindable buttons, skipping those already added
       for ( Button b : buttons )
       {
         if (( b.allowsKeyMove() ||
               b.allowsShiftedKeyMove() ||
               b.allowsXShiftedKeyMove())
             && !bindableButtons.contains( b ))
           bindableButtons.add( b );
       }
       upgradeButtons = ( Button[] )bindableButtons.toArray( upgradeButtons );
 
       if (( imageMaps.length > 0 ) && ( imageMaps[ mapIndex ] != null ))
         imageMaps[ mapIndex ].parse( this );
 
       for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
       {
         DeviceType type = ( DeviceType )e.nextElement();
         ImageMap[][] maps = type.getImageMaps();
         if ( maps.length > 0 )
         {
           ImageMap[] a = maps[ mapIndex ];
           for ( int i = 0; i < a.length; ++i )
             a[ i ].parse( this );
         }
       }
 
       setPhantomShapes();
 
       loaded = true;
     }
     catch ( Exception e )
     {
       StringWriter sw = new StringWriter();
       PrintWriter pw = new PrintWriter( sw );
       e.printStackTrace( pw );
       pw.flush();
       pw.close();
       JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
                                      sw.toString(), "Remote Load Error",
                                      JOptionPane.ERROR_MESSAGE );
       System.err.println( sw.toString());
     }
   }
 
   private void setPhantomShapes()
   {
     double radius = 8;
     double gap = 6;
 
     double diameter = 2 * radius;
     double x = gap;
     java.util.List< ImageMap > maps = new ArrayList< ImageMap >();
     if ( imageMaps.length > 0 )
       maps.add( imageMaps[ mapIndex ]);
     for ( Enumeration< DeviceType> e = deviceTypes.elements(); e.hasMoreElements(); )
     {
       DeviceType type = e.nextElement();
       if ( type.getImageMaps().length == 0 )
         continue;
       ImageMap[] devMaps = type.getImageMaps()[ mapIndex ];
       for ( int i = 0; i < devMaps.length; ++i )
         maps.add( devMaps[ i ]);
     }
 
     for ( ImageMap map : maps )
     {
       int h = map.getImage().getIconHeight();
       int w = map.getImage().getIconWidth();
       if ( h > height )
         height = h;
       if ( w > width )
         width = w;
     }
     double y = height + gap;
 
     for ( int i = 0; i < upgradeButtons.length; i++ )
     {
       Button b = upgradeButtons[ i ];
       if ( !b.getHasShape() && !b.getIsShifted() && !b.getIsXShifted())
       {
         if (( x + diameter + gap ) > width )
         {
           x = gap;
           y += ( gap + diameter );
         }
         Shape shape = new Ellipse2D.Double( x, y, diameter, diameter );
         x += ( diameter + gap );
         ButtonShape buttonShape = new ButtonShape( shape, b );
         phantomShapes.add( buttonShape );
         b.setHasShape( true );
       }
     }
     height = ( int )( y + gap + diameter );
     for ( ImageMap map : maps )
     {
       map.getShapes().addAll( phantomShapes );
     }
   }
 
   public String toString(){ return names[ nameIndex ]; }
   public String getSignature(){ return signature; }
   public String getName(){ return names[ nameIndex ]; }
   public int getNameCount(){ return names.length; }
   public int getBaseAddress(){ return baseAddress; }
   public int getEepromSize(){ return eepromSize; }
   public int getDeviceCodeOffset(){ return deviceCodeOffset; }
   public DeviceType[] getDeviceTypes()
   {
     DeviceType[] types = new DeviceType[ deviceTypes.size() ];
     for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
     {
       DeviceType type = ( DeviceType )e.nextElement();
       types[ type.getNumber() ] = type;
     }
     return types;
   }
 
   public DeviceType getDeviceType( String typeName )
   {
     DeviceType devType = deviceTypes.get( typeName );
     return devType;
   }
 
   public DeviceType getDeviceTypeByAliasName( String aliasName )
   {
     DeviceType type = ( DeviceType )deviceTypeAliases.get( aliasName );
     if ( type != null )
       return type;
     return getDeviceType( aliasName );
   }
 
   public DeviceType getDeviceTypeByIndex( int index )
   {
     for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
     {
       DeviceType type = ( DeviceType )e.nextElement();
       if ( type.getNumber() == index )
         return type;
     }
     return null;
   }
 
   public String getDeviceTypeAlias( DeviceType type )
   {
     String tentative = null;
     for ( String alias : deviceTypeAliasNames )
     {
       if ( getDeviceTypeByAliasName( alias ) != type )
         continue;
       String typeName = type.getName();
       if ( typeName.equals( alias ))
         return alias;
       if (( typeName.contains( alias ) || alias.contains( typeName )) && ( tentative == null ))
         tentative = alias;
     }
     if ( tentative != null )
       return tentative;
     for ( String alias : deviceTypeAliasNames )
       if ( getDeviceTypeByAliasName( alias ) == type )
       {
         tentative = alias;
         break;
       }
     return tentative;
   }
 
   public DeviceButton[] getDeviceButtons()
   {
     load();
     return deviceButtons;
   }
 
   public java.util.List< Button > getButtons()
   {
     load();
     return buttons;
   }
 
   public Button[] getUpgradeButtons()
   {
     load();
     return upgradeButtons;
   }
 
   public java.util.List< ButtonShape > getPhantomShapes()
   {
     load();
     return phantomShapes;
   }
 
   public Processor getProcessor()
   {
     load();
     return processor;
   }
 
   // public String getProcessorVersion()
   // {
     // return processorVersion;
   // }
 
   public int getRAMAddress()
   {
     load();
     return RAMAddress;
   }
 
   public short[] getDigitMaps()
   {
     load();
     return digitMaps;
   }
 
   public boolean getOmitDigitMapByte()
   {
     load();
     return omitDigitMapByte;
   }
 
   public ImageMap[] getImageMaps( DeviceType type )
   {
     load();
     ImageMap[][] maps = type.getImageMaps();
     if (( maps != null ) && ( maps.length != 0 ))
       return maps[ mapIndex ];
     else
     {
       ImageMap[] rc = new ImageMap[ 1 ];
       rc[ 0 ] = imageMaps[ mapIndex ];
       return rc;
     }
   }
 
   public int getAdvCodeFormat()
   {
     load();
     return advCodeFormat;
   }
 
   public int getAdvCodeBindFormat()
   {
     load();
     return advCodeBindFormat;
   }
 
   public int getEFCDigits()
   {
     load();
     return efcDigits;
   }
 
   private boolean parseFlag( StringTokenizer st )
   {
     String flag = st.nextToken( " =\t" );
     if ( flag.equalsIgnoreCase( "Y" ) ||
          flag.equalsIgnoreCase( "Yes" ) ||
          flag.equalsIgnoreCase( "T" ) ||
          flag.equalsIgnoreCase( "True" ) ||
          flag.equalsIgnoreCase( "1" ))
     {
       return true;
     }
     return false;
   }
 
   private String parseGeneralSection( RDFReader rdr )
     throws Exception
   {
     String processorName = null;
     String processorVersion = null;
     String line = null;
     while ( true )
     {
       line = rdr.readLine();
 
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, "=" );
 
       String parm = st.nextToken();
       if ( parm.equals( "Name" ))
         ;
       else if ( parm.equals( "BaseAddr" ))
         baseAddress = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "EepromSize" ))
         eepromSize = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "DevCodeOffset" ))
         deviceCodeOffset = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "FavKey" ))
       {
         int keyCode = rdr.parseNumber( st.nextToken( "=, \t" ));
         int deviceButtonAddress = rdr.parseNumber( st.nextToken());
         int maxEntries = rdr.parseNumber( st.nextToken());
         int entrySize = rdr.parseNumber( st.nextToken());
         boolean segregated = false;
         if ( st.hasMoreTokens())
            segregated = rdr.parseNumber( st.nextToken()) != 0;
         favKey = new FavKey( keyCode, deviceButtonAddress, maxEntries, entrySize, segregated );
       }
       else if ( parm.equals( "OEMDevice" ))
       {
         int deviceNumber = rdr.parseNumber( st.nextToken(",="));
         int deviceAddress = rdr.parseNumber( st.nextToken());
         oemDevice = new OEMDevice( deviceNumber, deviceAddress );
       }
       else if ( parm.equals( "OEMControl" ))
         oemControl = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "UpgradeBug" ))
         upgradeBug = parseFlag( st );
       else if ( parm.equals( "AdvCodeAddr" ))
       {
         int start = rdr.parseNumber( st.nextToken( ".=" ));
         int end = rdr.parseNumber( st.nextToken());
         advancedCodeAddress = new AddressRange( start, end );
       }
       else if ( parm.equals( "MacroSupport" ))
         macroSupport = ( rdr.parseNumber( st.nextToken()) != 0 );
       else if ( parm.equals( "UpgradeAddr" ))
       {
         int start = rdr.parseNumber( st.nextToken(".="));
         int end = rdr.parseNumber( st.nextToken());
         upgradeAddress = new AddressRange( start, end );
       }
       else if ( parm.equals( "DevUpgradeAddr" ))
       {
         int start = rdr.parseNumber( st.nextToken( ".=" ));
         int end = rdr.parseNumber( st.nextToken());
         deviceUpgradeAddress = new AddressRange( start, end );
       }
       else if ( parm.equals( "TimedMacroAddr" ))
       {
         int start = rdr.parseNumber( st.nextToken( ".=" ));
         int end = rdr.parseNumber( st.nextToken());
         timedMacroAddress = new AddressRange( start, end );
       }
       else if ( parm.equals( "TimedMacroWarning" ))
         timedMacroWarning = parseFlag( st );
       else if ( parm.equals( "LearnedAddr" ))
       {
         int start = rdr.parseNumber( st.nextToken( ".=" ));
         int end = rdr.parseNumber( st.nextToken());
         learnedAddress = new AddressRange( start, end );
       }
       else if ( parm.equals( "Processor" ))
       {
         processorName = st.nextToken();
         if ( processorName.equals( "6805" ) && ( processorVersion == null ))
           processorVersion = "C9";
       }
       else if ( parm.equals( "ProcessorVersion" ))
         processorVersion = st.nextToken();
       else if ( parm.equals( "RAMAddr" ))
         RAMAddress = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "TimeAddr" ))
         timeAddress = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "RDFSync" ))
         RDFSync = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "PunchThruBase" ))
         punchThruBase = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "ScanBase" ))
         scanBase = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "SleepStatusBit" ))
       {
         int addr = rdr.parseNumber( st.nextToken( ".=" ));
         int bit = rdr.parseNumber( st.nextToken());
         int onVal = 1;
         if ( st.hasMoreTokens())
           onVal = rdr.parseNumber( st.nextToken());
         sleepStatusBit = new StatusBit( addr, bit, onVal );
       }
       else if ( parm.equals( "VPTStatusBit" ))
       {
         int addr = rdr.parseNumber( st.nextToken( ".=" ));
         int bit = rdr.parseNumber( st.nextToken());
         int onVal = 1;
         if ( st.hasMoreTokens())
           onVal = rdr.parseNumber( st.nextToken());
         vptStatusBit = new StatusBit( addr, bit, onVal );
       }
       else if ( parm.equals( "OmitDigitMapByte" ))
         omitDigitMapByte = parseFlag( st );
       else if ( parm.equals( "ImageMap" ))
       {
         PropertyFile properties = JP1Frame.getProperties();
         File imageDir = properties.getFileProperty( "ImagePath" );
         if ( imageDir == null )
           imageDir = new File( properties.getFile().getParentFile(), "Images" );
 
         if ( !imageDir.exists())
         {
           JOptionPane.showMessageDialog( null, "Images folder not found!",
                                          "Error", JOptionPane.ERROR_MESSAGE );
           RMFileChooser chooser = new RMFileChooser( imageDir.getParentFile());
           chooser.setFileSelectionMode( RMFileChooser.DIRECTORIES_ONLY );
           chooser.setDialogTitle( "Choose the directory containing the remote images and maps" );
           if ( chooser.showOpenDialog( null ) != RMFileChooser.APPROVE_OPTION )
             System.exit( -1 );
 
           imageDir = chooser.getSelectedFile();
           properties.setProperty( "ImagePath", imageDir );
         }
 
         String mapList = st.nextToken();
         StringTokenizer mapTokenizer = new StringTokenizer( mapList, "," );
         int mapCount = mapTokenizer.countTokens();
         imageMaps = new ImageMap[ mapCount ];
         for ( int m = 0; m < mapCount; ++m )
           imageMaps[ m ] = new ImageMap( new File( imageDir, mapTokenizer.nextToken()));
 
         if ( nameIndex >= mapCount )
           mapIndex = mapCount - 1;
         else
           mapIndex = nameIndex;
       }
       else if ( parm.equals( "DefaultRestrictions" ))
         defaultRestrictions = parseRestrictions( st.nextToken());
       else if ( parm.equals( "Shift" ))
       {
         shiftMask = rdr.parseNumber( st.nextToken( "=," ));
         if ( st.hasMoreTokens())
           shiftLabel = st.nextToken().trim();
       }
       else if ( parm.equals( "XShift" ))
       {
         xShiftEnabled = true;
         xShiftMask = rdr.parseNumber( st.nextToken( "=," ));
         if ( st.hasMoreTokens())
           xShiftLabel = st.nextToken().trim();
       }
       else if ( parm.equals( "AdvCodeFormat" ))
       {
         String value = st.nextToken();
         if ( value.equals( "HEX" ))
           advCodeFormat = HEX_FORMAT;
         else if ( value.equals( "EFC" ))
           advCodeFormat = EFC_FORMAT;
       }
       else if ( parm.equals( "AdvCodeBindFormat" ))
       {
         String value = st.nextToken();
         if ( value.equals( "NORMAL" ))
           advCodeBindFormat = NORMAL;
         else if ( value.equals( "LONG" ))
           advCodeBindFormat = LONG;
       }
       else if ( parm.equals( "EFCDigits" ))
       {
         String value = st.nextToken();
         efcDigits = rdr.parseNumber( value );
       }
       else if ( parm.equals( "DevComb" ))
       {
         devCombAddress = new int[ 7 ];
         String combParms = st.nextToken();
         StringTokenizer st2 = new StringTokenizer( combParms, ",", true );
         for ( int i = 0; i < 7; i++ )
         {
           if ( st2.hasMoreTokens())
           {
             String tok = st2.nextToken();
             if ( tok.equals( "," ))
               devCombAddress[ i ] = -1;
             else
             {
               devCombAddress[ i ] = rdr.parseNumber( tok );
               if ( st2.hasMoreTokens())
                 st2.nextToken(); // skip delimeter
             }
           }
           else
             devCombAddress[ i ] = -1;
         }
       }
       else if ( parm.equals( "ProtocolVectorOffset" ))
         protocolVectorOffset = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "ProtocolDataOffset" ))
         protocolDataOffset = rdr.parseNumber( st.nextToken());
       else if ( parm.equals( "EncDec" ))
       {
         String className = st.nextToken("=()");
         String textParm = null;
         if ( st.hasMoreTokens())
           textParm = st.nextToken();
         try
         {
           if ( className.indexOf( '.' ) == -1 )
             className = "com.hifiremote.jp1." + className;
 
           Class cl = Class.forName( className );
           Class[] parmClasses = { String.class };
           Constructor ct = cl.getConstructor( parmClasses );
           Object[] ctParms = { textParm };
           encdec = ( EncrypterDecrypter )ct.newInstance( ctParms );
         }
         catch ( Exception e )
         {
           System.err.println( "Error creating an instance of " + className );
           e.printStackTrace( System.err );
         }
       }
       else if ( parm.equals( "MaxUpgradeLength" ))
         maxUpgradeLength = new Integer( rdr.parseNumber( st.nextToken()));
       else if ( parm.equals( "MaxProtocolLength" ))
         maxProtocolLength = new Integer( rdr.parseNumber( st.nextToken()));
       else if ( parm.equals( "MaxCombinedUpgradeLength" ))
         maxCombinedUpgradeLength = new Integer( rdr.parseNumber( st.nextToken()));
       else if ( parm.equals( "SectionTerminator" ))
         sectionTerminator = ( short )rdr.parseNumber( st.nextToken());
       else if ( parm.equalsIgnoreCase( "2BytePid" ))
         twoBytePID = parseFlag( st );
       else if ( parm.equalsIgnoreCase( "LearnedDevBtnSwapped" ))
         learnedDevBtnSwapped = parseFlag( st );
     }
     processor = ProcessorManager.getProcessor( processorName, processorVersion );
     return line;
   }
 
   public int[] getDevCombAddresses()
   {
     load();
     return devCombAddress;
   }
 
   private int parseRestrictions( String str )
   {
     int rc = 0;
     if ( restrictionTable == null )
     {
       restrictionTable = new Hashtable< String, Integer >( 46 );
       restrictionTable.put( "MoveBind", new Integer( Button.MOVE_BIND ));
       restrictionTable.put( "ShiftMoveBind", new Integer( Button.SHIFT_MOVE_BIND ));
       restrictionTable.put( "XShiftMoveBind", new Integer( Button.XSHIFT_MOVE_BIND ));
       restrictionTable.put( "AllMoveBind", new Integer( Button.ALL_MOVE_BIND ));
       restrictionTable.put( "MacroBind", new Integer( Button.MACRO_BIND ));
       restrictionTable.put( "ShiftMacroBind", new Integer( Button.SHIFT_MACRO_BIND ));
       restrictionTable.put( "XShiftMacroBind", new Integer( Button.XSHIFT_MACRO_BIND ));
       restrictionTable.put( "AllMacroBind", new Integer( Button.ALL_MACRO_BIND ));
       restrictionTable.put( "LearnBind", new Integer( Button.LEARN_BIND ));
       restrictionTable.put( "ShiftLearnBind", new Integer( Button.SHIFT_LEARN_BIND ));
       restrictionTable.put( "XShiftLearnBind", new Integer( Button.XSHIFT_LEARN_BIND ));
       restrictionTable.put( "AllLearnBind", new Integer( Button.ALL_LEARN_BIND ));
       restrictionTable.put( "MacroData", new Integer( Button.MACRO_DATA ));
       restrictionTable.put( "ShiftMacroData", new Integer( Button.SHIFT_MACRO_DATA ));
       restrictionTable.put( "XShiftMacroData", new Integer( Button.XSHIFT_MACRO_DATA ));
       restrictionTable.put( "AllMacroData", new Integer( Button.ALL_MACRO_DATA ));
       restrictionTable.put( "TMacroData", new Integer( Button.TMACRO_DATA ));
       restrictionTable.put( "ShiftTMacroData", new Integer( Button.SHIFT_TMACRO_DATA ));
       restrictionTable.put( "XShiftMacroData", new Integer( Button.XSHIFT_TMACRO_DATA ));
       restrictionTable.put( "AllTMacroData", new Integer( Button.ALL_TMACRO_DATA ));
       restrictionTable.put( "FavData", new Integer( Button.FAV_DATA ));
       restrictionTable.put( "ShiftFavData", new Integer( Button.SHIFT_FAV_DATA ));
       restrictionTable.put( "XShiftFavData", new Integer( Button.XSHIFT_FAV_DATA ));
       restrictionTable.put( "AllFavData", new Integer( Button.ALL_FAV_DATA ));
       restrictionTable.put( "Bind", new Integer( Button.BIND ));
       restrictionTable.put( "ShiftBind", new Integer( Button.SHIFT_BIND ));
       restrictionTable.put( "XShiftBind", new Integer( Button.XSHIFT_BIND ));
       restrictionTable.put( "Data", new Integer( Button.DATA ));
       restrictionTable.put( "ShiftData", new Integer( Button.SHIFT_DATA ));
       restrictionTable.put( "XShiftData", new Integer( Button.XSHIFT_DATA ));
       restrictionTable.put( "AllBind", new Integer( Button.ALL_BIND ));
       restrictionTable.put( "AllData", new Integer( Button.ALL_DATA ));
       restrictionTable.put( "Shift", new Integer( Button.SHIFT ));
       restrictionTable.put( "XShift", new Integer( Button.XSHIFT ));
       restrictionTable.put( "All", new Integer( Button.ALL ));
     }
     StringTokenizer st = new StringTokenizer( str, "+-", true );
     boolean isAdd = true;
     while ( st.hasMoreTokens())
     {
       String token = st.nextToken();
       if ( token.equals( "+" ))
         isAdd = true;
       else if ( token.equals( "-" ))
         isAdd = false;
       else
       {
         Integer value = restrictionTable.get( token );
         if ( value == null )
           continue;
         if ( isAdd )
           rc |= value.intValue();
         else
           rc &= ~value.intValue();
       }
     }
     return rc;
   }
   private String parseSpecialProtocols( RDFReader rdr )
     throws Exception
   {
     java.util.List< CheckSum > work = new ArrayList< CheckSum >();
     String line;
     while ( true )
     {
       line = rdr.readLine();
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, "=" );
       String name = st.nextToken();
       Hex pid = new Hex( st.nextToken());
       specialProtocols.add( SpecialProtocol.create( name, pid ));
     }
     checkSums = ( CheckSum[] )work.toArray( checkSums );
     return line;
   }
 
   private String parseCheckSums( RDFReader rdr )
     throws Exception
   {
     java.util.List< CheckSum > work = new ArrayList< CheckSum >();
     String line;
     while ( true )
     {
       line = rdr.readLine();
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       char ch = line.charAt( 0 );
 
       line = line.substring( 1 );
       StringTokenizer st = new StringTokenizer( line, ":." );
       int addr = rdr.parseNumber( st.nextToken());
       AddressRange range = new AddressRange( rdr.parseNumber( st.nextToken()),
                                              rdr.parseNumber( st.nextToken()));
       CheckSum sum = null;
       if ( ch == '+' )
         sum = new AddCheckSum( addr, range );
       else
         sum = new XorCheckSum( addr, range );
       work.add( sum );
     }
     checkSums = ( CheckSum[] )work.toArray( checkSums );
     return line;
   }
 
   public CheckSum[] getCheckSums(){ return checkSums; }
 
   private String parseSettings( RDFReader rdr )
     throws Exception
   {
     String line;
     java.util.List< Setting > work = new ArrayList< Setting >();
     while ( true )
     {
       line = rdr.readLine();
 
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, "=" );
       String title = st.nextToken();
 
       int byteAddress = rdr.parseNumber( st.nextToken( ".= \t" ));
       int bitNumber = rdr.parseNumber( st.nextToken());
       int numberOfBits = rdr.parseNumber( st.nextToken());
       int initialValue = rdr.parseNumber( st.nextToken());
       boolean inverted = ( rdr.parseNumber( st.nextToken()) != 0 );
 
       java.util.List< String> options = null;
       String sectionName = null;
 
       if ( st.hasMoreTokens())
       {
         String token = st.nextToken( ",;)" ).trim();
         if ( token.charAt( 0 ) == '(' )
         {
           options = new ArrayList< String >();
           options.add( token.substring( 1 ));
           while ( st.hasMoreTokens())
             options.add( st.nextToken());
         }
         else
           sectionName = token.trim();
       }
       String[] optionsList = null;
       if ( options != null )
         optionsList = options.toArray( new String[ 0 ]);
       work.add( new Setting( title, byteAddress, bitNumber,
                              numberOfBits, initialValue, inverted,
                              optionsList, sectionName ));
     }
     settings = ( Setting[] )work.toArray( settings );
     return line;
   }
 
   public Setting[] getSettings(){ return settings; }
 
   public Object[] getSection( String name )
   {
     if ( name.equals( "DeviceButtons" ))
       return getDeviceButtons();
     else if ( name.equals( "DeviceTypes" ))
       return getDeviceTypes();
 
     return null;
   }
 
   private String parseFixedData( RDFReader rdr )
     throws Exception
   {
     java.util.List< FixedData > work = new ArrayList< FixedData >();
     java.util.List< Byte > temp = new ArrayList< Byte >();
     String line;
     int address = -1;
     int value = -1;
 
     while ( true )
     {
       line = rdr.readLine();
 
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st =  new StringTokenizer( line, ",; \t" );
       String token = st.nextToken();
       while( true )
       {
         if ( token.charAt( 0 ) == '=' ) // the last token was an address
         {
           token = token.substring( 1 );
           if ( address != -1 )                       // we've seen some bytes
           {
             byte[] b = new byte[ temp.size()];
             int i = 0;
             for ( Byte val : temp )
             {
               b[ i++ ] = val.byteValue();
             }
             work.add( new FixedData( address, b ));
             temp.clear();
           }
           address = value;
           value = -1;
           if ( token.length() != 0 )
             continue;
         }
         else
         {
           int equal = token.indexOf( '=' );
           String saved = token;
           if ( equal != -1 )
           {
             token = token.substring( 0, equal );
           }
           if ( value != -1 )
           {
             temp.add( new Byte(( byte )value ));
           }
           value = rdr.parseNumber( token );
           if ( equal != -1 )
           {
             token = saved.substring( equal );
             continue;
           }
         }
         if ( !st.hasMoreTokens() )
           break;
         token = st.nextToken();
       }
     }
     temp.add( new Byte(( byte )value ));
     byte[] b = new byte[ temp.size()];
     int j = 0;
     for ( Byte by : temp )
     {
       b[ j ] = by.byteValue();
     }
     work.add( new FixedData( address, b ));
     fixedData = work.toArray( fixedData );
     return line;
   }
 
   private String parseDeviceButtons( RDFReader rdr )
     throws Exception
   {
     java.util.List< DeviceButton > work = new ArrayList< DeviceButton >();
     String line;
     while ( true )
     {
       line = rdr.readLine();
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line );
       String name = st.nextToken( "= \t" );
 
       int hiAddr = rdr.parseNumber( st.nextToken( ",= \t" ));
       int lowAddr = rdr.parseNumber( st.nextToken());
       int typeAddr = 0;
       if ( st.hasMoreTokens())
         typeAddr = rdr.parseNumber( st.nextToken());
       work.add( new DeviceButton( name, hiAddr, lowAddr, typeAddr ));
     }
     deviceButtons = work.toArray( deviceButtons );
     return line;
   }
 
   private String parseDeviceAbbreviations( RDFReader rdr )
     throws Exception
   {
     String line;
     while ( true )
     {
       line = rdr.readLine();
       if ( line == null )
         break;
       if (( line.length() == 0 ) || ( line.charAt( 0 ) == '[' ))
           break;
       StringTokenizer st = new StringTokenizer( line, "," );
       while ( st.hasMoreTokens())
       {
         String token = st.nextToken().trim();
         int equal = token.indexOf( '=' );
         if ( equal == -1 )
           continue;
 
         String devName = token.substring( 0, equal );
         String abbreviation = token.substring( equal + 1 );
         DeviceType devType = getDeviceType( devName );
         if ( devType != null )
           devType.setAbbreviation( abbreviation );
       }
     }
     return line;
   }
 
   private String parseDigitMaps( RDFReader rdr )
     throws Exception
   {
     java.util.List< Integer > work = new ArrayList< Integer >();
     String line;
     while ( true )
     {
       line = rdr.readLine();
 
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, ",; \t" );
       while ( st.hasMoreTokens())
       {
         work.add( new Integer( rdr.parseNumber( st.nextToken())));
       }
     }
 
     digitMaps = new short[ work.size()];
     int i = 0;
     for ( Integer v : work )
     {
       digitMaps[ i++ ] = v.shortValue();
     }
     return line;
   }
 
   private String parseDeviceTypes( RDFReader rdr )
     throws Exception
   {
     String line;
     int type = 0;
     while ( true )
     {
       line = rdr.readLine();
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, "=, \t" );
       String name = st.nextToken();
       int map = 0;
       if ( st.hasMoreTokens())
       {
         map = rdr.parseNumber( st.nextToken());
         if ( st.hasMoreTokens())
           type = rdr.parseNumber( st.nextToken());
       }
       deviceTypes.put( name, new DeviceType( name, map, type ));
       type += 0x0101;
     }
     return line;
   }
 
   private String parseDeviceTypeAliases( RDFReader rdr )
     throws Exception
   {
     String line;
     java.util.List< String > v = new ArrayList< String >();
     DeviceType vcrType = null;
     boolean hasPVRalias = false;
     while ( true )
     {
       line = rdr.readLine();
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, "= \t" );
       String typeName = st.nextToken();
       DeviceType type = getDeviceType( typeName );
       st.nextToken( "=" );
       String rest = st.nextToken().trim();
       st = new StringTokenizer( rest, "," );
       while ( st.hasMoreTokens())
       {
         String aliasName = st.nextToken().trim();
         if ( aliasName.equals( "VCR" ))
           vcrType = type;
         if ( aliasName.equals( "PVR" ))
           hasPVRalias = true;
         deviceTypeAliases.put( aliasName, type );
         v.add( aliasName );
       }
     }
     if ( !hasPVRalias && ( vcrType != null ))
     {
       v.add( "PVR" );
       deviceTypeAliases.put( "PVR", vcrType );
     }
     deviceTypeAliasNames = new String[ 0 ];
     deviceTypeAliasNames = v.toArray( deviceTypeAliasNames );
     Arrays.sort( deviceTypeAliasNames );
     return line;
   }
 
   public String[] getDeviceTypeAliasNames()
   {
     load();
     return deviceTypeAliasNames;
   }
 
   private String parseDeviceTypeImageMaps( RDFReader rdr )
     throws Exception
   {
     String line;
     DeviceType type = null;
     java.util.List< java.util.List< ImageMap >> outer = new ArrayList< java.util.List< ImageMap >>();
     java.util.List< ImageMap > inner = null;
     boolean nested = false;
    PropertyFile properties = JP1Frame.getProperties();
    File imageDir = properties.getFileProperty( "ImagePath" );
    if ( imageDir == null )
      imageDir = new File( properties.getFile().getParentFile(), "Images" );    
 
     while ( true )
     {
       line = rdr.readLine();
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, "=, \t" );
       String typeName = st.nextToken();
       type = getDeviceType( typeName );
 
       while ( st.hasMoreTokens())
       {
         String token = st.nextToken();
         if ( token.charAt( 0 ) == '(' ) // it's a list
         {
           nested = true;
           token = token.substring( 1 );
           inner = new ArrayList< ImageMap >();
           outer.add( inner );
         }
 
         if ( !nested )
         {
           inner = new ArrayList< ImageMap >();
           outer.add( inner );
         }
 
         int closeParen = token.indexOf( ')' );
         if ( closeParen != -1 )
         {
           nested = false;
           token = token.substring( 0, closeParen );
         }
 
         inner.add( new ImageMap( new File( imageDir, token )));
       }
       ImageMap[][] outerb = new ImageMap[ outer.size()][];
       int o = 0;
       for ( java.util.List< ImageMap > maps : outer )
       {
         ImageMap[] innerb = new ImageMap[ maps.size()];
         outerb[ o++ ] = innerb;
         int i = 0;
         for ( ImageMap map : maps )
         {
           innerb[ i++ ] = map;
         }
         maps.clear();
       }
       outer.clear();
       type.setImageMaps( outerb );
     }
     return line;
   }
 
   private String parseButtons( RDFReader rdr )
     throws Exception
   {
     String line;
     short keycode = 1;
     int restrictions = defaultRestrictions;
     while ( true )
     {
       line = rdr.readLine();
       if ( line == null )
         break;
       if (( line.length() == 0 ) || ( line.charAt( 0 ) == '[' ))
           break;
       StringTokenizer st = new StringTokenizer( line, "," );
       while ( st.hasMoreTokens())
       {
         String token = st.nextToken().trim();
         int equal = token.indexOf( '=' );
         if ( equal != -1 )
         {
           String keycodeStr = token.substring( equal + 1 );
           token = token.substring( 0, equal );
           int pos = keycodeStr.indexOf( ':' );
           if ( pos != -1 )
           {
             String restrictStr = keycodeStr.substring( pos + 1 );
             restrictions = parseRestrictions( restrictStr );
             keycodeStr = keycodeStr.substring( 0, pos );
           }
           else
             restrictions = defaultRestrictions;
           keycode = ( short )rdr.parseNumber( keycodeStr );
         }
 
         int colon = token.indexOf( ':' );
         String name = token;
         if ( colon != -1 )
         {
           name = token.substring( colon + 1 );
           char ch = name.charAt( 0 );
           if (( ch == '\'' ) || ch == '"' )
           {
             int end = name.lastIndexOf( ch );
             name = name.substring( 1, end );
           }
           token = token.substring( 0, colon );
           ch = token.charAt( 0 );
           if (( ch == '\'' ) || ch == '"' )
           {
             int end = token.lastIndexOf( ch );
             token = token.substring( 1, end );
           }
         }
         Button b = new Button( token, name, keycode, this );
         b.setRestrictions( restrictions );
         keycode++;
         addButton( b );
       }
     }
 
     return line;
   }
 
   public Button getButton( int keyCode )
   {
     load();
     return ( Button )buttonsByKeyCode.get( new Integer( keyCode ));
   }
 
   public String getButtonName( int keyCode )
   {
     Button b = getButton( keyCode );
 
     if ( b == null )
     {
       int mask = keyCode & 0xC0;
       int baseCode = keyCode & 0x3F;
       if ( baseCode != 0 )
       {
         b = getButton( baseCode );
         if (( baseCode | shiftMask ) == keyCode )
           return b.getShiftedName();
         if ( xShiftEnabled && (( baseCode | xShiftMask ) == keyCode ))
           return b.getXShiftedName();
       }
       baseCode = keyCode & ~ shiftMask;
       b = getButton( baseCode );
       if ( b != null )
         return b.getShiftedName();
       baseCode = keyCode & ~ xShiftMask;
       b = getButton( baseCode );
       if ( b != null )
         return b.getXShiftedName();
     }
 
     return b.getName();
   }
 
   public Button getButton( String name )
   {
     load();
     return ( Button )buttonsByName.get( name.toLowerCase());
   }
 
   public void addButton( Button b )
   {
     int keycode = b.getKeyCode();
     int unshiftedCode = keycode & 0x3f;
     if ( b.getIsShifted())
     {
       Button c = getButton( unshiftedCode );
       if ( c != null )
       {
         c.setShiftedButton( b );
         b.setBaseButton( c );
         if ( b.getName() == null )
         {
           String name = shiftLabel + '-' + c.getName();
           b.setName( name );
           b.setStandardName( name );
         }
       }
     }
     else if ( b.getIsXShifted())
     {
       Button c = getButton( unshiftedCode );
       if ( c != null )
       {
         c.setXShiftedButton( b );
         b.setBaseButton( c );
         if ( b.getName() == null )
         {
           String name = xShiftLabel + '-' + c.getName();
           b.setName( name );
           b.setStandardName( name );
         }
       }
     }
     else
     {
       // Look for a shifted button for which this is the base.
       int shiftedCode = keycode + shiftMask;
       Button c = getButton( shiftedCode );
       if ( c != null )
       {
         c.setBaseButton( b );
         b.setShiftedButton( c );
       }
       if ( xShiftEnabled )
       {
         // Look for an xshifted button for which this is the base.
         shiftedCode = keycode + xShiftMask;
         c = getButton( shiftedCode );
         if ( c != null )
         {
           c.setBaseButton( b );
           b.setXShiftedButton( c );
         }
       }
     }
     if ( b.getName() == null )
     {
       String name = "unknown" + Integer.toHexString( keycode );
       b.setName( name );
       b.setStandardName( name );
     }
     buttons.add( b );
     buttonsByName.put( b.getName().toLowerCase(), b );
     buttonsByStandardName.put( b.getStandardName().toLowerCase(), b );
     buttonsByKeyCode.put( new Integer( keycode ), b );
   }
 
   private String parseMultiMacros( RDFReader rdr )
     throws Exception
   {
     String line;
     while ( true )
     {
       line = rdr.readLine();
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
 
       StringTokenizer st = new StringTokenizer( line, "=" );
       String name = st.nextToken();
 
       // Find the matching button
       Button button = ( Button )buttonsByName.get( name );
       if ( button != null )
         button.setMultiMacroAddress( rdr.parseNumber( st.nextToken()));
     }
     return line;
   }
 
   public Button findByStandardName( Button b )
   {
     load();
     return ( Button )buttonsByStandardName.get( b.getStandardName().toLowerCase());
   }
 
 
   private String parseButtonMaps( RDFReader rdr )
     throws Exception
   {
     java.util.List< ButtonMap > work = new ArrayList< ButtonMap >();
     String line;
     ButtonMap map = null;
     int name = -1;
     java.util.List< java.util.List< Integer >> outer = new ArrayList< java.util.List< Integer >>();
     java.util.List< Integer > inner = null;
     boolean nested = false;
 
     while ( true )
     {
       line = rdr.readLine();
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, "=, \t" );
       if ( line.indexOf( '=' ) != -1 )
       {
         if ( name != -1 )
         {
           short[][] outerb = new short[ outer.size()][];
           int o = 0;
           for ( java.util.List< Integer > maps : outer )
           {
             short[] innerb = new short[ maps.size()];
             outerb[ o++ ] = innerb;
             int i = 0;
             for ( Integer v : maps )
             {
               innerb[ i++ ] = v.shortValue();
             }
             maps.clear();
           }
           outer.clear();
           work.add( new ButtonMap( name, outerb ));
         }
         name = rdr.parseNumber( st.nextToken());
       }
 
       while ( st.hasMoreTokens())
       {
         String token = st.nextToken();
         if ( token.charAt( 0 ) == '(' ) // it's a list
         {
           nested = true;
           token = token.substring( 1 );
           inner = new ArrayList< Integer >();
           outer.add( inner );
         }
 
         if ( !nested )
         {
           inner = new ArrayList< Integer >();
           outer.add( inner );
         }
 
         int closeParen = token.indexOf( ')' );
         if ( closeParen != -1 )
         {
           nested = false;
           token = token.substring( 0, closeParen );
         }
 
         inner.add( new Integer( rdr.parseNumber( token )));
       }
     }
     {
       short[][] outerb = new short[ outer.size()][];
       int o = 0;
       for ( java.util.List< Integer > maps : outer )
       {
         short[] innerb = new short[ maps.size()];
         outerb[ o++ ] = innerb;
         int i = 0;
         for ( Integer v : maps )
         {
           innerb[ i++ ] = v.shortValue();
         }
         maps.clear();
       }
       outer.clear();
       work.add( new ButtonMap( name, outerb ));
     }
     buttonMaps = work.toArray( buttonMaps );
     return line;
   }
 
   private String parseProtocols( RDFReader rdr )
     throws Exception
   {
     String line;
     while ( true )
     {
       line = rdr.readLine();
       if ( line == null )
         break;
       if ( line.length() != 0 )
       {
         if (line.charAt( 0 ) == '[')
           break;
         StringTokenizer st = new StringTokenizer( line, "," );
         while ( st.hasMoreTokens())
         {
           String token = st.nextToken().trim();
           String variantName = "";
           int colon = token.indexOf( ':' );
           String name = token;
           if ( colon != -1 )
           {
             variantName = token.substring( colon + 1 );
             token = token.substring( 0, colon );
           }
           Hex pid = new Hex( token );
           java.util.List< String > v = protocolVariantNames.get( pid );
           if ( v == null )
           {
             v = new ArrayList< String >();
             protocolVariantNames.put( pid, v );
           }
           v.add( variantName );
         }
       }
     }
     return line;
   }
 
   public int getHeight(){ load(); return height; }
   private int height;
 
   public int getWidth(){ load(); return width; }
   private int width;
 
   public boolean supportsVariant( Hex pid, String name )
   {
     load();
     java.util.List< String > v = protocolVariantNames.get( pid );
     if (( v == null ) || v.isEmpty())
       return false;
 
     if ( v.contains( name ))
         return true;
 
     return false;
   }
 
   public java.util.List< String > getSupportedVariantNames( Hex pid )
   {
     load();
     return protocolVariantNames.get( pid );
   }
 
   /*
   public void clearButtonAssignments()
   {
     load();
     for ( Enumeration e = buttons.elements(); e.hasMoreElements(); )
     {
       (( Button )e.nextElement()).setFunction( null ).setShiftedFunction( null ).setXShiftedFunction( null );
     }
   }
   */
 
   public void setProtocols( java.util.List< Protocol > protocols )
   {
     load();
     this.protocols = protocols;
   }
 
   public java.util.List< Protocol > getProtocols()
   {
     load();
     return protocols;
   }
 
   public EncrypterDecrypter getEncrypterDecrypter()
   {
     load();
     return encdec;
   }
 
   public KeyMove createKeyMoveKey( int keyCode, int deviceIndex, int deviceType, int setupCode, int movedKeyCode, String notes )
   {
     KeyMove keyMove = null;
     keyMove = new KeyMoveKey( keyCode, deviceIndex, deviceType, setupCode, movedKeyCode, notes );
     return keyMove;
   }
 
   public KeyMove createKeyMove( int keyCode, int deviceIndex, int deviceType, int setupCode, Hex cmd, String notes )
   {
     KeyMove keyMove = null;
     if ( advCodeFormat == HEX_FORMAT )
       keyMove = new KeyMove( keyCode, deviceIndex, deviceType, setupCode, cmd, notes );
     else if ( efcDigits == 3 )
       keyMove = new KeyMoveEFC( keyCode, deviceIndex, deviceType, setupCode, EFC.parseHex( cmd ), notes );
     else // EFCDigits == 5
       keyMove = new KeyMoveEFC5( keyCode, deviceIndex, deviceType, setupCode, EFC5.parseHex( cmd ), notes );
     return keyMove;
   }
 
   public KeyMove createKeyMove( int keyCode, int deviceIndex, int deviceType, int setupCode, int efc, String notes )
   {
     KeyMove keyMove = null;
     if ( advCodeFormat == HEX_FORMAT )
     {
       if ( efcDigits == 3 )
         keyMove = new KeyMove( keyCode, deviceIndex, deviceType, setupCode, EFC.toHex( efc ), notes );
       else // EFCDigits == 5
         keyMove = new KeyMove( keyCode, deviceIndex, deviceType, setupCode, EFC5.toHex( efc ), notes );
     }
     else if ( efcDigits == 3 )
       keyMove = new KeyMoveEFC( keyCode, deviceIndex, deviceType, setupCode, efc, notes );
     else // EFCDigits == 5
       keyMove = new KeyMoveEFC5( keyCode, deviceIndex, deviceType, setupCode, efc, notes );
     return keyMove;
   }
 
   public Integer getMaxUpgradeLength(){ return maxUpgradeLength; }
   public Integer getMaxProtocolLength(){ return maxProtocolLength; }
   public Integer getMaxCombinedUpgradeLength(){ return maxCombinedUpgradeLength; }
 
   // Interface Comparable
   public int compareTo( Object o )
   {
     return names[ nameIndex ].compareTo( o.toString());
   }
 
   public int getShiftMask(){ return shiftMask; }
   public int getXShiftMask(){ return xShiftMask; }
   public boolean getXShiftEnabled(){ return xShiftEnabled; }
   public void setXShiftEnabled( boolean flag ){ xShiftEnabled = flag; }
   public String getShiftLabel(){ return shiftLabel; }
   public String getXShiftLabel(){ return xShiftLabel; }
   public int getProtocolVectorOffset(){ return protocolVectorOffset; }
   public int getProtocolDataOffset(){ return protocolDataOffset; }
   public boolean getSupportsBinaryUpgrades(){ return supportsBinaryUpgrades; }
 
   private File file = null;
   private String signature = null;
   private String[] names = new String[ 1 ];
   private int nameIndex = 0;
   private boolean loaded = false;
   private int baseAddress = 0;
   private int eepromSize;
   private int deviceCodeOffset;
   private FavKey favKey = null;
   public FavKey getFavKey(){ return favKey; }
   private OEMDevice oemDevice = null;
   private int oemControl = 0;
   private boolean upgradeBug = false;
   private AddressRange advancedCodeAddress = null;
   public AddressRange getAdvancedCodeAddress(){ return advancedCodeAddress; }
   private boolean macroSupport = true;
   private AddressRange upgradeAddress = null;
   public AddressRange getUpgradeAddress(){ return upgradeAddress; }
   private AddressRange deviceUpgradeAddress = null;
   private AddressRange timedMacroAddress = null;
   private boolean timedMacroWarning = false;
   private AddressRange learnedAddress = null;
   public AddressRange getLearnedAddress(){ return learnedAddress; }
   private Processor processor = null;
   // private String processorVersion = null;
   private int RAMAddress;
   private int timeAddress = 0;
   private int RDFSync;
   private int punchThruBase;
   private int scanBase = 0;
   private StatusBit sleepStatusBit = null;
   private StatusBit vptStatusBit = null;
   private CheckSum[] checkSums = new CheckSum[ 0 ];
   private Setting[] settings = new Setting[ 0 ];
   private FixedData[] fixedData = new FixedData[ 0 ];
   private DeviceButton[] deviceButtons = new DeviceButton[ 0 ];
   private Hashtable< String, DeviceType> deviceTypes = new Hashtable< String, DeviceType >();
   private Hashtable< String, DeviceType> deviceTypeAliases = new Hashtable< String, DeviceType >();
   private String[] deviceTypeAliasNames = null;
   private java.util.List<Button> buttons = new ArrayList<Button>();
   private Hashtable< Integer, Button > buttonsByKeyCode = new Hashtable< Integer, Button >();
   private Hashtable< String, Button > buttonsByName = new Hashtable< String, Button >();
   private Hashtable< String, Button > buttonsByStandardName = new Hashtable< String, Button >();
   private Button[] upgradeButtons = new Button[ 0 ];
   private java.util.List< ButtonShape> phantomShapes = new ArrayList< ButtonShape >();
   private short[] digitMaps = new short[ 0 ];
   private ButtonMap[] buttonMaps = new ButtonMap[ 0 ];
   private boolean omitDigitMapByte = false;
   private Hashtable< Hex, java.util.List< String >> protocolVariantNames = new Hashtable< Hex, java.util.List< String >>();
   private java.util.List< Protocol > protocols = null;
   private ImageMap[] imageMaps = new ImageMap[ 0 ];
   private int mapIndex = 0;
   private int shiftMask = 0x80;
   private int xShiftMask = 0xC0;
   private boolean xShiftEnabled = false;
   private String shiftLabel = "Shift";
   private String xShiftLabel = "XShift";
   private int defaultRestrictions = 0;
   public static final int HEX_FORMAT = 0;
   public static final int EFC_FORMAT = 1;
   public static final int NORMAL = 0;
   public static final int LONG = 1;
   private int advCodeFormat = HEX_FORMAT;
   private int advCodeBindFormat = NORMAL;
   private int efcDigits = 3;
   private int[] devCombAddress = null;
   private int protocolVectorOffset = 0;
   private int protocolDataOffset = 0;
   private EncrypterDecrypter encdec = null;
   private boolean supportsBinaryUpgrades = false;
   private Integer maxProtocolLength = null;
   private Integer maxUpgradeLength = null;
   private Integer maxCombinedUpgradeLength = null;
   private short sectionTerminator = 0;
   public short getSectionTerminator(){ return sectionTerminator; }
   public java.util.List< SpecialProtocol > specialProtocols = new ArrayList< SpecialProtocol >();
   public java.util.List< SpecialProtocol > getSpecialProtocols(){ return specialProtocols; }
   private boolean twoBytePID = false;
   public boolean usesTwoBytePID(){ return twoBytePID; }
   private boolean learnedDevBtnSwapped = false;
   public boolean getLearnedDevBtnSwapped(){ return learnedDevBtnSwapped; }
 
   private static Hashtable< String, Integer > restrictionTable = null;
  }
