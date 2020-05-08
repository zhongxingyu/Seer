 package com.hifiremote.jp1;
 
 import java.awt.*;
 import java.awt.geom.*;
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
     this.names = aRemote.names;
     nameIndex = index;
   }
 
   public Remote( File rdf )
   {
     file = rdf;
     StringTokenizer st = new StringTokenizer( rdf.getName());
     signature = st.nextToken(); // upto the 1st space
     st.nextToken( "()" ); // skip the space
     String name = st.nextToken(); // the stuff between the parens
     int underscore = name.indexOf( '_' );
     if ( underscore != -1 )
     {
       int count = 2;
       int start = name.indexOf( '_', underscore + 1 );
       while ( start != -1 )
       {
         count++;
         start = name.indexOf( '_', start + 1 );
       }
       
       int dash = name.lastIndexOf( '-', underscore );
       int space = name.lastIndexOf( ' ', underscore );
       int pos = Math.max( dash, space );
       int length = underscore - pos;
       if ( length > 3 )
       {
         String front = name.substring( 0, pos + 1 );
         int end = pos + count * length;
         String back = name.substring( end );
         --length;
         start = pos + 1;
         names = new String[ count ];
         for ( int i = 0; i < count; i++ )
         {
           names[ i ] = front + name.substring( start, start + length ) + back;
           start += length + 1;
         }
       }
       else
         names[ 0 ] = name.replace( '_', '/' );
     }
     else
       names[ 0 ] = name;
   }
 
   public File getFile(){ return file; }
 
   public void load()
     throws Exception
   {
     if ( loaded )
       return;
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
         else if ( line.equals( "DeviceTypeAliases" ))
           line = parseDeviceTypeAliases( rdr );
         else if ( line.equals( "Buttons" ))
           line = parseButtons( rdr );
         else if ( line.equals( "MultiMacros" ))
           line = parseMultiMacros( rdr );
         else if ( line.equals( "ButtonMaps" ))
           line = parseButtonMaps( rdr );
         else if ( line.equals( "Protocols" ))
           line = parseProtocols( rdr );
 //          else if ( line.equals( "NoBind" ))
 //            line = parseNoBind( rdr );
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
       buttonMaps[ 0 ] = new ButtonMap( 0, new int[ 0 ][ 0 ]);
     }
     for ( int i = 0; i < buttonMaps.length; i++ )
       buttonMaps[ i ].setButtons( this );
 
     for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
     {
       DeviceType type = ( DeviceType )e.nextElement();
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
       Vector v = new Vector();
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
 
     // Create the upgradeButtons[]
     // and starts off with the buttons in longest button map
     ButtonMap longestMap = null;
     for ( Enumeration e = deviceTypes.elements(); e.hasMoreElements(); )
     {
       DeviceType type = ( DeviceType )e.nextElement();
       ButtonMap thisMap = type.getButtonMap();
       if (( longestMap == null ) || ( longestMap.size() < thisMap.size() ))
         longestMap = thisMap;
     }
 
     int bindableButtons = 0;
     for ( Enumeration e = buttons.elements(); e.hasMoreElements(); )
     {
       Button b = ( Button )e.nextElement();
       if ( b.allowsKeyMove() || b.allowsShiftedKeyMove() ||
            b.allowsXShiftedKeyMove() || ( b.getButtonMaps() != 0 ))
         bindableButtons++;
     }
 
     upgradeButtons = new Button[ bindableButtons ];
 
     // first copy the buttons from the longest map
     int index = 0;
     while ( index < longestMap.size())
     {
       upgradeButtons[ index ] = longestMap.get( index );
       index++;
     }
 
     // now copy the rest of the buttons, skipping those in the map
     for ( Enumeration e = buttons.elements(); e.hasMoreElements(); )
     {
       Button b = ( Button )e.nextElement();
       if (( b.allowsKeyMove() ||
             b.allowsShiftedKeyMove() ||
             b.allowsXShiftedKeyMove() ||
             ( b.getButtonMaps() != 0 ))
           && !longestMap.isPresent( b ))
         upgradeButtons[ index++ ] = b;
     }
 
     if ( mapFiles[ mapIndex ] != null )
       readMapFile();
 
     loaded = true;
   }
 
   public String toString(){ return names[ nameIndex ]; }
   public String getSignature(){ return signature; }
   public String getName(){ return names[ nameIndex ]; }
   public int getNameCount(){ return names.length; }
   public int getEepromSize()
   {
     return eepromSize;
   }
 
   public int getDeviceCodeOffset()
   {
     return deviceCodeOffset;
   }
 
   public Hashtable getDeviceTypes()
   {
     return deviceTypes;
   }
 
   public DeviceType getDeviceType( String typeName )
   {
     DeviceType devType =( DeviceType )deviceTypes.get( typeName );
     return devType;
   }
 
   public DeviceType getDeviceTypeByAliasName( String aliasName )
   {
     return ( DeviceType )deviceTypeAliases.get( aliasName );
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
 
   public DeviceButton[] getDeviceButtons()
   {
     return deviceButtons;
   }
 
   public Vector getButtons()
   {
     return buttons;
   }
 
   public Button[] getUpgradeButtons()
   {
     return upgradeButtons;
   }
 
   public ButtonShape[] getButtonShapes()
   {
     return buttonShapes;
   }
 
   public String getProcessor()
   {
     return processor;
   }
 
   public String getProcessorVersion()
   {
     return processorVersion;
   }
 
   public int getRAMAddress()
   {
     return RAMAddress;
   }
 
   public int[] getDigitMaps()
   {
     return digitMaps;
   }
 
   public boolean getOmitDigitMapByte()
   {
     return omitDigitMapByte;
   }
 
   public ImageIcon getImageIcon()
   {
     return imageIcon;
   }
 
   public int getAdvCodeFormat()
   {
     return advCodeFormat;
   }
 
   private String parseGeneralSection( RDFReader rdr )
     throws Exception
   {
     String line = null;
     while ( true )
     {
       line = rdr.readLine();
 
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, "=" );
 
       String parm = st.nextToken();
       if ( parm.equals( "Name" ))
 //        name = st.nextToken();
         ;
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
            segregated = ( rdr.parseNumber( st.nextToken()) != 0 );
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
         upgradeBug = ( rdr.parseNumber( st.nextToken()) != 0 );
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
         timedMacroWarning = ( rdr.parseNumber( st.nextToken()) != 0 );
       else if ( parm.equals( "LearnedAddr" ))
       {
         int start = rdr.parseNumber( st.nextToken( ".=" ));
         int end = rdr.parseNumber( st.nextToken());
         learnedAddress = new AddressRange( start, end );
       }
       else if ( parm.equals( "Processor" ))
       {
         processor = st.nextToken();
         if ( processor.equals( "6805" ) && ( processorVersion == null ))
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
         omitDigitMapByte = ( rdr.parseNumber( st.nextToken()) != 0 );
       else if ( parm.equals( "ImageMap" ))
       {
         Vector v = new Vector();
         File imageDir = new File( KeyMapMaster.getHomeDirectory(), "images" );
 
         while ( st.hasMoreTokens())
           v.add( new File( imageDir, st.nextToken( "=," )));
 
         int mapCount = v.size();
         mapFiles = new File[ mapCount ];
         for ( int i = 0; i < mapCount; i++ )
           mapFiles[ i ] = ( File )v.elementAt( i );
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
           advCodeFormat = HEX;
         else if ( value.equals( "EFC" ))
           advCodeFormat = EFC;
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
     }
     return line;
   }
 
   public int[] getDevCombAddresses()
   {
     return devCombAddress;
   }
 
   private int parseRestrictions( String str )
   {
     int rc = 0;
     if ( restrictionTable == null )
     {
       restrictionTable = new Hashtable( 46 );
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
         Integer value = ( Integer )restrictionTable.get( token );
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
 
   private String parseCheckSums( RDFReader rdr )
     throws Exception
   {
     Vector work = new Vector();
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
 
   private String parseSettings( RDFReader rdr )
     throws Exception
   {
     String line;
     Vector work = new Vector();
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
 
       Vector options = null;
       String sectionName = null;
 
       if ( st.hasMoreTokens())
       {
         String token = st.nextToken( ",;)" );
 //        while ( token.charAt( 0 ) == ' ' )
 //          token = token.substring( 1 );
         if ( token.charAt( 0 ) == '(' )
         {
           options = new Vector();
           options.add( token.substring( 1 ));
           while ( st.hasMoreTokens())
           {
             options.add( st.nextToken());
           }
         }
         else
           sectionName = token;
       }
       String[] optionsList = null;
       if ( options != null )
       {
         optionsList = new String[ options.size()];
         int i = 0;
         for ( Enumeration e = options.elements(); e.hasMoreElements(); i++ )
         {
           optionsList[ i ] = ( String )e.nextElement();
         }
       }
       work.add( new Setting( title, byteAddress, bitNumber,
                                  numberOfBits, initialValue, inverted,
                                  optionsList,
                                  sectionName ));
     }
     settings = ( Setting[] )work.toArray( settings );
     return line;
   }
 
   private String parseFixedData( RDFReader rdr )
     throws Exception
   {
     Vector work = new Vector();
     Vector temp = new Vector();
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
             for ( Enumeration e = temp.elements();
                   e.hasMoreElements(); ++i )
             {
               b[ i ] = (( Byte )e.nextElement()).byteValue();
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
     for ( Enumeration en = temp.elements(); en.hasMoreElements(); ++j )
     {
       b[ j ] = (( Byte )en.nextElement()).byteValue();
     }
     work.add( new FixedData( address, b ));
     fixedData = ( FixedData[] )work.toArray( fixedData );
     return line;
   }
 
   private String parseDeviceButtons( RDFReader rdr )
     throws Exception
   {
     Vector work = new Vector();
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
     deviceButtons = ( DeviceButton[] )work.toArray( deviceButtons );
     return line;
   }
 
   private String parseDigitMaps( RDFReader rdr )
     throws Exception
   {
     Vector work = new Vector();
     String line;
     while ( true )
     {
       line = rdr.readLine();
 
       if (( line == null ) || ( line.length() == 0 ))
         break;
 
       StringTokenizer st = new StringTokenizer( line, ",; \t" );
       while ( st.hasMoreTokens())
       {
         work.add( new Integer(rdr.parseNumber( st.nextToken())));
       }
     }
 
     digitMaps = new int[ work.size()];
     int i = 0;
     for ( Enumeration e = work.elements(); e.hasMoreElements(); ++i )
     {
       digitMaps[ i ] = (( Integer )e.nextElement()).intValue();
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
     Vector v = new Vector();
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
     deviceTypeAliasNames = ( String[] )v.toArray( deviceTypeAliasNames );
     Arrays.sort( deviceTypeAliasNames );
     return line;
   }
 
   public String[] getDeviceTypeAliasNames()
   {
     return deviceTypeAliasNames;
   }
 
   private String parseButtons( RDFReader rdr )
     throws Exception
   {
     String line;
     int keycode = 1;
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
           keycode = rdr.parseNumber( keycodeStr );
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
         Button b = new Button( token, name, keycode );
         b.setRestrictions( restrictions );
         keycode++;
         addButton( b );
       }
     }
 
     return line;
   }
 
   public Button getButton( int keyCode )
   {
     return ( Button )buttonsByKeyCode.get( new Integer( keyCode ));
   }
 
   public Button getButton( String name )
   {
     return ( Button )buttonsByName.get( name.toLowerCase());
   }
 
   public void addButton( Button b )
   {
     int keycode = b.getKeyCode();
     int maskedCode = keycode & 0xC0;
     int unshiftedCode = keycode & 0x3f;
     if ( maskedCode == shiftMask )
     {
       b.setIsShifted( true );
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
     else if ( maskedCode == xShiftMask )
     {
       b.setIsXShifted( true );
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
     else if ( b.getName() == null )
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
     return ( Button )buttonsByStandardName.get( b.getStandardName().toLowerCase());
   }
 
 
   private String parseButtonMaps( RDFReader rdr )
     throws Exception
   {
     Vector work = new Vector();
     String line;
     ButtonMap map = null;
     int name = -1;
     Vector outer = new Vector();
     Vector inner = null;
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
           int[][] outerb = new int[ outer.size()][];
           int o = 0;
           for ( Enumeration oe = outer.elements(); oe.hasMoreElements(); o++ )
           {
             inner = ( Vector )oe.nextElement();
             int[] innerb = new int[ inner.size()];
             outerb[ o ] = innerb;
             int i = 0;
             for ( Enumeration ie = inner.elements(); ie.hasMoreElements(); i++ )
             {
               innerb[ i ] = (( Integer )ie.nextElement()).intValue();
             }
             inner.clear();
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
           inner = new Vector();
           outer.add( inner );
         }
 
         if ( !nested )
         {
           inner = new Vector();
           outer.add( inner );
         }
 
         int closeParen = token.indexOf( ')' );
         if ( closeParen != -1 )
         {
           nested = false;
           token = token.substring( 0, closeParen );
         }
 
         inner.add( new Integer(rdr.parseNumber( token )));
       }
     }
     {
       int[][] outerb = new int[ outer.size()][];
       int o = 0;
       for ( Enumeration oe = outer.elements(); oe.hasMoreElements(); o++ )
       {
         inner = ( Vector )oe.nextElement();
         int[] innerb = new int[ inner.size()];
         outerb[ o ] = innerb;
         int i = 0;
         for ( Enumeration ie = inner.elements(); ie.hasMoreElements(); i++ )
         {
           innerb[ i ] = (( Integer )ie.nextElement()).intValue();
         }
         inner.clear();
       }
       outer.clear();
       work.add( new ButtonMap( name, outerb ));
     }
     buttonMaps = ( ButtonMap[] )work.toArray( buttonMaps );
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
           protocolVariantNames.put( pid, variantName );
         }
       }
     }
     return line;
   }
 
   private void readMapFile()
     throws Exception
   {
     File mapFile = mapFiles[ mapIndex ];
     BufferedReader in = new BufferedReader( new FileReader( mapFile ));
     String line = in.readLine();
     Vector work = new Vector();
 
     if ( line.startsWith( "#$" ))
     {
       // This MAP file is a NCSA map file, probably created by Map This!
       while (( line = in.readLine()) != null )
       {
         if ( line.startsWith( "#$GIF:" ))
         {
           File imageFile = new File( mapFile.getParentFile(), line.substring( 6 ));
           imageIcon = new ImageIcon( imageFile.getAbsolutePath());
         }
         else if ( !line.startsWith( "#$" ))
         {
           StringTokenizer st = new StringTokenizer( line, " ," );
           String type = st.nextToken();
           if ( type.equals( "default" ))
             continue;
           String displayName = null;
           String keyCodeText = null;
           String buttonName = st.nextToken();
           // check if keycode is used
           int pos = buttonName.indexOf( ':' );
           if ( pos != -1 )
           {
             keyCodeText = buttonName.substring( 0, pos );
             buttonName = buttonName.substring( pos + 1 );
           }
           pos = buttonName.indexOf( '=' );
           if ( pos != -1 )
           {
             displayName = buttonName.substring( 0, pos );
             buttonName = buttonName.substring( pos + 1 );
           }
           Button button = null;
           // check if keycode is used
           if ( keyCodeText != null )
           {
             int keyCode;
             if ( keyCodeText.charAt( 0 ) == '$' )
               keyCode = Integer.parseInt( keyCodeText.substring( 1 ), 16 );
             else
               keyCode = Integer.parseInt( keyCodeText );
             button = getButton( keyCode );
           }
           else
             button = getButton( buttonName );
           Shape shape = null;
           if ( button == null )
           {
             System.err.println( "Warning: Shape defined for unknown button " + buttonName );
             continue;
           }
           if ( type.equals( "rect" ))
           {
             double x = Double.parseDouble( st.nextToken());
             double y = Double.parseDouble( st.nextToken());
             double x2 = Double.parseDouble( st.nextToken());
             double y2 = Double.parseDouble( st.nextToken());
             double w = x2 - x;
             double h = y2 - y;
             shape = new Rectangle2D.Double( x, y, w, h );
           }
           else if ( type.equals( "circle" ))
           {
             double x = Double.parseDouble( st.nextToken());
             double y = Double.parseDouble( st.nextToken());
             double x2 = Double.parseDouble( st.nextToken());
             double y2 = Double.parseDouble( st.nextToken());
             double w = x2 - x;
             x -= w;
             w += w;
             double h = y2 - y;
             y -= h;
             h += h;
             shape = new Ellipse2D.Double( x, y, w, h );
           }
           else if ( type.equals( "poly" ))
           {
             GeneralPath path = new GeneralPath( GeneralPath.WIND_EVEN_ODD,
                                                 st.countTokens()/2 );
             float x1 = Float.parseFloat( st.nextToken());
             float y1 = Float.parseFloat( st.nextToken());
             path.moveTo( x1, y1 );
 
             while ( st.hasMoreTokens())
             {
               float x = Float.parseFloat( st.nextToken());
               float y = Float.parseFloat( st.nextToken());
               if (( x == x1 ) && ( y == y1 ))
                 break;
               path.lineTo( x, y );
             }
             path.closePath();
             shape = path;
           }
           ButtonShape buttonShape = new ButtonShape( shape, button );
           if ( displayName != null )
             buttonShape.setName( displayName );
           work.add( buttonShape );
         }
       }
     }
     else
     {
       // This map file probably uses the proprietary RM format
       StringTokenizer st = new StringTokenizer( line, "=" );
       if ( !st.hasMoreTokens())
       {
         System.err.println( "File " + mapFile + " is not a valid map file!" );
         return;
       }
 
       String name = st.nextToken();
       if ( !name.equals( "Image" ) || !st.hasMoreTokens())
       {
         System.err.println( "File " + mapFile + " is not a valid map file!" );
         return;
       }
       String value = st.nextToken();
       File imageFile = new File( mapFile.getParentFile(), value );
       imageIcon = new ImageIcon( imageFile.getAbsolutePath());
 
       while (( line = in.readLine()) != null )
       {
         if ( line.length() == 0 )
           continue;
         else if ( line.equals( "[ButtonShapes]" ))
           break;
         else
           System.err.println( "File " + mapFile + " is not a valid map file!" );
       }
 
       while (( line = in.readLine()) != null )
       {
         if ( line.length() == 0 )
           continue;
 
         st = new StringTokenizer( line, "=:," );
         while ( st.hasMoreTokens())
         {
           name = st.nextToken();
 
           Button button = getButton( name );
           if ( button == null )
             continue;
           Shape shape = null;
           String type = st.nextToken();
           if ( type.equals( "ellipse" ))
           {
             double x = Double.parseDouble( st.nextToken());
             double y = Double.parseDouble( st.nextToken());
             double width = Double.parseDouble( st.nextToken());
             double height = Double.parseDouble( st.nextToken());
             shape = new Ellipse2D.Double( x, y, width, height );
           }
           else if ( type.equals( "rect" ))
           {
             double x = Double.parseDouble( st.nextToken());
             double y = Double.parseDouble( st.nextToken());
             double width = Double.parseDouble( st.nextToken());
             double height = Double.parseDouble( st.nextToken());
             shape = new Rectangle2D.Double( x, y, width, height );
           }
           else if ( type.equals( "poly" ))
           {
             GeneralPath path = new GeneralPath( GeneralPath.WIND_EVEN_ODD,
                                                 st.countTokens()/2 );
             float x = Float.parseFloat( st.nextToken());
             float y = Float.parseFloat( st.nextToken());
             path.moveTo( x, y );
 
             while ( st.hasMoreTokens())
             {
               x = Float.parseFloat( st.nextToken());
               y = Float.parseFloat( st.nextToken());
               path.lineTo( x, y );
             }
             path.closePath();
             shape = path;
           }
           work.add( new ButtonShape( shape, button ));
         }
       }
     }
     in.close();
     buttonShapes = ( ButtonShape[] )work.toArray( buttonShapes );
   }
 
   public String getSupportedVariantName( Hex pid )
   {
     String rc = ( String )protocolVariantNames.get( pid );
     return rc;
   }
 
 
   public void clearButtonAssignments()
   {
     for ( Enumeration e = buttons.elements(); e.hasMoreElements(); )
     {
       (( Button )e.nextElement()).setFunction( null ).setShiftedFunction( null ).setXShiftedFunction( null );
     }
   }
 
   public void setProtocols( Vector protocols )
   {
     this.protocols = protocols;
   }
 
   public Vector getProtocols()
   {
     return protocols;
   }
 
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
 
   private File file = null;
   private String signature = null;
   private String[] names = new String[ 1 ];
   private int nameIndex = 0;
   private boolean loaded = false;
   private int eepromSize;
   private int deviceCodeOffset;
   private FavKey favKey = null;
   private OEMDevice oemDevice = null;
   private int oemControl = 0;
   private boolean upgradeBug = false;
   private AddressRange advancedCodeAddress = null;
   private boolean macroSupport = true;
   private AddressRange upgradeAddress = null;
   private AddressRange deviceUpgradeAddress = null;
   private AddressRange timedMacroAddress = null;
   private boolean timedMacroWarning = false;
   private AddressRange learnedAddress = null;
   private String processor = null;
   private String processorVersion = null;
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
   private Hashtable deviceTypes = new Hashtable();
   private Hashtable deviceTypeAliases = new Hashtable();
   private String[] deviceTypeAliasNames = null;
   private Vector buttons = new Vector();
   private Hashtable buttonsByKeyCode = new Hashtable();
   private Hashtable buttonsByName = new Hashtable();
   private Hashtable buttonsByStandardName = new Hashtable();
   private Button[] upgradeButtons = null;
   private ButtonShape[] buttonShapes = new ButtonShape[ 0 ];
   private int[] digitMaps = new int[ 0 ];
   private ButtonMap[] buttonMaps = new ButtonMap[ 0 ];
   private boolean omitDigitMapByte = false;
   private Hashtable protocolVariantNames = new Hashtable();
   private Vector protocols = null;
   private ImageIcon imageIcon = null;
   private File[] mapFiles = new File[ 0 ];
   private int mapIndex = 0;
   private int shiftMask = 0x80;
   private int xShiftMask = 0xC0;
   private boolean xShiftEnabled = false;
   private String shiftLabel = "Shift";
   private String xShiftLabel = "XShift";
   private int defaultRestrictions = 0;
   public static final int HEX = 0;
   public static final int EFC = 1;
   private int advCodeFormat = HEX;
   private int[] devCombAddress = null;
   private static Hashtable restrictionTable = null;
  }
