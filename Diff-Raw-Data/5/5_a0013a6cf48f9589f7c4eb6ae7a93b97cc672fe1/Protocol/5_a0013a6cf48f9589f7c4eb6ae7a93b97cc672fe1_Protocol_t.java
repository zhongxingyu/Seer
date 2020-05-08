 package com.hifiremote.jp1;
 
 import java.io.*;
 import java.util.*;
 import javax.swing.table.*;
 
 public class Protocol
 {
   public Protocol( String name, Hex id, Properties props )
   {
     this.name = name;
     this.id = id;
     this.variantName = props.getProperty( "VariantName", "" );
     this.defaultCmd = new Hex( props.getProperty( "DefaultCmd", "00" ));
     this.cmdIndex = Integer.parseInt( props.getProperty( "CmdIndex", "0" ));
 
     String temp = props.getProperty( "AlternatePID" );
     if ( temp != null )
     {
       StringTokenizer st = new StringTokenizer( temp.trim(), "," );
       alternatePID = new Hex( st.nextToken());
       while ( st.hasMoreTokens())
         altPIDOverrideList.add( st.nextToken());
     }
 
     temp = props.getProperty( "DevParms", "" );
     if ( temp != null )
       devParms = DeviceParmFactory.createParameters( temp );
 
     temp = props.getProperty( "DeviceTranslator" );
     if ( temp != null )
     {
       deviceTranslators = TranslatorFactory.createTranslators( temp );
     }
 
     temp = props.getProperty( "DeviceImporter" );
     if ( temp != null )
     {
       devImporters = ImporterFactory.createImporters( temp );
     }
     else
     {
       System.err.println( "Geenrating deviceImporter for protocol " + name );
       int mappedIndex = 0;
       boolean needRemap = false;
       String[] map = new String[ 4 ];
       int maxParm = Math.min( 4, devParms.length );
       for ( int i = 0; i < maxParm; i++ )
       {
         System.err.println( "DevParm is " + devParms[ i ].getName());
         if ( devParms[ i ].getClass() != FlagDeviceParm.class )
           map[ i ] = Integer.toString( mappedIndex );
         else
           needRemap = true;
         mappedIndex++;
       }
       if ( needRemap )
       {
         devImporters = new Importer[ 1 ];
         devImporters[ 0 ] = new ReorderImporter( map );
       }
     }
 
     this.fixedData = new Hex( props.getProperty( "FixedData", "" ));
 
     temp = props.getProperty( "CmdTranslator" );
     if ( temp != null )
     {
       cmdTranslators = TranslatorFactory.createTranslators( temp );
     }
     else
       cmdTranslators = new Translate[ 0 ];
 
     temp = props.getProperty( "ImportCmdTranslator" );
     if ( temp != null )
     {
       importCmdTranslators = TranslatorFactory.createTranslators( temp );
     }
 
     notes = props.getProperty( "Notes" );
 
     temp = props.getProperty( "Code.S3C80" );
     if ( temp != null )
       code.put( "S3C80", new Hex( temp ));
     temp = props.getProperty( "Code.740" );
     if ( temp != null )
       code.put( "740", new Hex( temp ));
     temp = props.getProperty( "Code.6805-C9" );
     if ( temp != null )
       code.put( "6805-C9", new Hex( temp ));
     temp = props.getProperty( "Code.6805-RC16/18" );
     if ( temp != null )
       code.put( "6805-RC16/18", new Hex( temp ));
 
     temp = props.getProperty( "CmdParms", "" );
     StringTokenizer st = new StringTokenizer( temp, "," );
     int count = st.countTokens();
     cmdParms = new CmdParameter[ count ];
     for ( int i = 0; i < count; i++ )
     {
       String str = st.nextToken();
       cmdParms[ i ] = CmdParmFactory.createParameter( str, devParms );
       if (cmdParms[i] == null)
         System.err.println( "Protocol.Protocol("+ name +") failed createParameter("+ str +")");
     }
     temp = props.getProperty( "CmdParmInit" );
     if ( temp != null )
     {
       cmdParmInit = InitializerFactory.create( temp );
     }
 
     temp = props.getProperty( "OldNames" );
     if ( temp != null )
     {
       StringTokenizer st2 = new StringTokenizer( temp, "," );
       while ( st2.hasMoreTokens())
         oldNames.add( st2.nextToken().trim());
     }
   }
 
   public void reset()
   {
     int len = devParms.length;
     Value[] vals = new Value[ len ];
     for ( int i = 0; i < len; i++ )
       vals[ i ] = new Value( null,  null );
     setDeviceParms( vals );
   }
 
   public void setProperties( Properties props ){}
 
   public void importUpgradeCode( Remote remote, String notes )
   {
     StringTokenizer st = new StringTokenizer( notes, "\n" );
     String text = null;
     while ( st.hasMoreTokens())
     {
       text = st.nextToken().toUpperCase();
       if ( text.startsWith( "UPGRADE PROTOCOL 0 =" ))
         break;
     }
     if ( st.hasMoreTokens())
     {
       text = st.nextToken(); // 1st line of code
       while ( st.hasMoreTokens())
       {
         String temp = st.nextToken();
         if ( temp.equals( "End" ))
           break;
         text = text + ' ' + temp;
       }
       Processor p = remote.getProcessor();
       code.put( p.getFullName(), new Hex( text ));
     }
   }
 
   public KMPanel getPanel( DeviceUpgrade deviceUpgrade )
   {
     return null;
   }
 
   public void initializeParms()
   {
     if ( cmdParmInit != null )
     {
       for ( int i = 0; i < cmdParmInit.length; i++ )
       {
         cmdParmInit[ i ].initialize( devParms, cmdParms );
       }
     }
   }
 
   public boolean needsCode( Remote remote )
   {
     if ( remote.supportsVariant( id, variantName ))
       return false;
     else
       return true;
   }
 
   public boolean hasCode( Remote remote )
   {
     return ( getCode( remote ) != null );
   }
 
   public Hex getCode( Remote remote )
   {
     Processor p = remote.getProcessor();
     return ( Hex )code.get( p.getFullName());
   }
 
   public void importDeviceParms( Value[] parms )
   {
     if ( devImporters != null )
     {
       for ( int i = 0; i < devImporters.length; i++ )
         parms = devImporters[ i ].convertParms( parms ); 
     }
     setDeviceParms( parms );
   }
 
   public void setDeviceParms( Value[] parms )
   {
     if ( parms.length != devParms.length )
     {
       System.err.println( "Protocol.setDeviceParms(), protocol=" + getDiagnosticName() +
                           ", parms.length=" +
                           parms.length + " and devParms.length=" + devParms.length );
     }
 
     for ( int i = 0; i < parms.length; i++ )
     {
       if (( i < devParms.length ) && ( parms[ i ] != null ) && ( parms[ i ].getUserValue() != null ))
         devParms[ i ].setValue( parms[ i ].getUserValue());
     }
   }
 
   public Value[] getDeviceParmValues()
   {
     Value[] rc = new Value[ devParms.length ];
     for ( int i = 0; i < rc.length; i++ )
     {
       DeviceParameter parm = devParms[ i ];
       rc[ i ] = new Value( parm.getValue(), parm.getDefaultValue());
     }
     return rc;
   }
 
   public static Hex efc2hex( EFC efc, Hex hex, int index )
   {
     int temp = efc.getValue() + 156;
     temp = ( temp & 0xFF ) ^ 0xAE;
     temp = ( temp >> 3 ) | ( temp << 5 );
     hex.getData()[ index ] = temp;
     return hex;
   }
 
   public Hex efc2hex( EFC efc, Hex hex )
   {
     if ( hex == null )
       hex = getDefaultCmd();
     return efc2hex( efc, hex, cmdIndex );
   }
 
   public static EFC hex2efc( Hex hex, int index )
   {
     int temp = hex.getData()[ index ] & 0xFF;
     temp = ( temp << 3 ) | ( temp >> 5 );
     temp = ( temp ^ 0xAE ) - 156;
     EFC efc = new EFC( temp );
     return efc;
   }
 
   public EFC hex2efc( Hex hex )
   {
     return hex2efc( hex, cmdIndex );
   }
 
   public Hex getDefaultCmd()
   {
     Hex rc = null;
     try
     {
       rc = ( Hex )defaultCmd.clone();
     }
     catch ( CloneNotSupportedException e )
     {
     }
     Value[] vals = new Value[ cmdParms.length ];
 
     for ( int i = 0; i < cmdParms.length; i++ )
       vals[ i ] = new Value( cmdParms[ i ].getDefaultValue());
 
     for ( int i = 0; i < cmdTranslators.length; i++ )
       cmdTranslators[ i ].in( vals, rc, devParms, -1 );
 
     return rc;
   }
 
   public DeviceParameter[] getDeviceParameters()
   {
     return devParms;
   }
 
   public CmdParameter[] getCommandParameters()
   {
     return cmdParms;
   }
 
   public String getNotes()
   {
     return notes;
   }
 
   public Vector getOldNames()
   {
     return oldNames;
   }
 
   // These methods allow adding columns to the Functions Panel
   public int getColumnCount()
   {
     return cmdParms.length;
   }
 
   public Class getColumnClass( int col )
   {
     return cmdParms[ col ].getValueClass();
   }
 
   public TableCellEditor getColumnEditor( int col )
   {
     return cmdParms[ col ].getEditor();
   }
 
   public TableCellRenderer getColumnRenderer( int col )
   {
     return cmdParms[ col ].getRenderer();
   }
 
   public String getColumnName( int col )
   {
     return cmdParms[ col ].getName();
   }
 
   public Object getValueAt( int col, Hex hex )
   {
     Value[] vals = new Value[ cmdParms.length ];
     for ( int i = 0; i < cmdTranslators.length; i++ )
       cmdTranslators[ i ].out( hex, vals, devParms );
     Value v = vals[ col ];
     if ( v == null )
     {
       System.err.println( "Protocol.getValueAt("+ col +") failed" );
       return new Integer(0);
     }
     return cmdParms[ col ].getValue( v.getValue());
   }
 
   public void setValueAt( int col, Hex hex, Object value )
   {
     Value[] vals = new Value[ cmdParms.length ];
     vals[ col ] = new Value( cmdParms[ col ].convertValue( value ), null );
     for ( int i = 0; i < cmdTranslators.length; i++ )
       cmdTranslators[ i ].in( vals, hex, devParms, col );
   }
 
   public void importCommandParms( Hex hex, String text )
   {
    if ( cmdParms.length == 1 )
      return;
     Translate[] translators = importCmdTranslators;
     if ( translators == null )
      translators = cmdTranslators;
     StringTokenizer st = new StringTokenizer( text );
     Value[] values = new Value[ st.countTokens() ];
     int index = 0;
     while ( st.hasMoreTokens())
       values[ index++ ] = new Value( new Integer( st.nextToken()));
 
     for ( index = 0; index < values.length; index++ )
     {
       for ( int i = 0; i < translators.length; i++ )
         translators[ i ].in( values, hex, devParms, index );
     }
   }
 
   public boolean isEditable( int col ){ return true; }
 
   public String toString(){ return name; }
 
   public String getName(){ return name; }
 
   public Hex getID(){ return id; }
 
   public Hex getAlternatePID(){ return alternatePID; }
 
   public Hex getID( Remote remote )
   {
     if ( alternatePID == null )
       return id;
 
     if ( !needsCode( remote ))
       return id;
 
     if ( altPIDOverrideList.isEmpty())
       return alternatePID;
 
     Protocol p = ProtocolManager.getProtocolManager().findProtocolForRemote( remote, id, false );
     String builtin = "none";
     if ( p != null )
       builtin = p.getVariantName();
   
     for ( Enumeration e = altPIDOverrideList.elements(); e.hasMoreElements(); )
     {
       String temp = ( String )e.nextElement();
       if ( temp.equalsIgnoreCase( builtin ))
         return id;
     }
     return alternatePID;  
   }
 
   public String getVariantName(){ return variantName; }
 
   public String getDiagnosticName( )
   {
     String result = "\"" + name + "\" (" + id;
     if ( variantName.length() > 0 )
       result += " : " + variantName;
     return result + ")";
   }
 
   public Hex getFixedData()
   {
     Hex temp = null;
     try {
       temp = ( Hex )fixedData.clone();
     } catch ( CloneNotSupportedException e ){}
     Value[] parms = getDeviceParmValues();
     if ( deviceTranslators != null )
     {
       for ( int i = 0; i < deviceTranslators.length; i++ )
         deviceTranslators[ i ].in( parms, temp, devParms, -1 );
     }
     return temp;
   }
 
   // convert the functions defined in this protocol to the new Protocol
   public void convertFunctions( Vector funcs, Protocol newProtocol )
   {
     CmdParameter[] newParms = newProtocol.cmdParms;
 
     // count the number of matching parameters
     int matchingParms = 0;
     for ( int i = 0; i < cmdParms.length; i++ )
     {
       String name = cmdParms[ i ].getName();
       for ( int j = 0; j < newParms.length; j++ )
       {
         if ( name.equals( newParms[ j ].getName()))
         {
           matchingParms++;
           break;
         }
       }
     }
     // create a map of command parameter indexs from this protocol to the new one
     int[] oldIndex = new int[ matchingParms ];
     int[] newIndex = new int[ matchingParms ];
     int match = 0;
     for ( int i = 0; i < cmdParms.length; i++ )
     {
       String name = cmdParms[ i ].getName();
       for ( int j = 0; j < newParms.length; j++ )
       {
         if ( name.equals( newParms[ j ].getName()))
         {
           oldIndex[ match ] = i;
           newIndex[ match ] = j;
           match++;
           break;
         }
       }
     }
 
     // create Value arrays for holding the command parameters
     Value[] currValues = new Value[ cmdParms.length ];
     Value[] newValues = new Value[ newParms.length ];
     // initialize the contents of newValues with the defaultValues for the new protocol
     for ( int i = 0; i < newValues.length; i++ )
     {
       newValues[ i ] = new Value( newParms[ i ].getDefaultValue(), null );
     }
 
     // now convert each defined function
     for ( Enumeration en = funcs.elements(); en.hasMoreElements(); )
     {
       Function f = ( Function )en.nextElement();
       Hex hex = f.getHex();
       Hex newHex = newProtocol.getDefaultCmd();
       if ( hex != null )
       {
         // extract the command parms from the hex
         for ( int i = 0; i < cmdTranslators.length; i++ )
           cmdTranslators[ i ].out( hex, currValues, devParms );
 
         // copy the matching parameters to the new Values;
         for ( int i = 0; i < oldIndex.length; i++ )
         {
           newValues[ newIndex[ i]] = currValues[ oldIndex[ i ]];
         }
 
         // generate the appropriate hex for the new protocol
         for ( int i = 0; i < newProtocol.cmdTranslators.length; i++ )
           newProtocol.cmdTranslators[ i ].in( newValues, newHex, newProtocol.devParms, -1 );
 
         // store the hex back into the function
         f.setHex( newHex );
       }
     }
   }
 
   public void updateFunctions( Vector funcs )
   {
     Value[] values = new Value[ cmdParms.length ];
     for ( Enumeration en = funcs.elements(); en.hasMoreElements(); )
     {
       Function f = ( Function )en.nextElement();
       Hex hex = f.getHex();
       if ( hex != null )
       {
         // extract the command parms from the hex
         for ( int i = 0; i < cmdTranslators.length; i++ )
           cmdTranslators[ i ].out( hex, values, devParms );
 
         // recompute the hex
         for ( int i = 0; i < cmdTranslators.length; i++ )
           cmdTranslators[ i ].in( values, hex, devParms, -1 );
 
         // store the hex back into the function
         f.setHex( hex );
       }
     }
   }
 
   public int different(Properties props)
   //
   // This is intended to become a fuzzy comparison to help select the best protocol
   // when protocols.ini has been changed and there is no perfect fit.
   //
   // It returns the value tooDifferent in cases where this protocol wouldn't be
   // good enough even if it were the best found.  (It never returns any value greater
   // than tooDifferent).
   // It returns 0 for a perfect match or a larger value for a worse match.
   //
   // For now it absolutely requires a match of id.  I expect that won't always be
   // an absolute.
   {
     Hex pid = new Hex( props.getProperty( "Protocol" ));
     if ( !pid.equals( id ))
       return tooDifferent;
     int result = 0;
     String str = props.getProperty( "Protocol.name" );
     if ( str != null && ! str.equals( name ))
     {
       // I think we should use a fuzzy string compare here, but for now...
       result += 1000;
     }
     str = props.getProperty( "FixedData" );
     if (str != null)
     {
       Hex hex = new Hex( str );
       if ( hex.length() != fixedData.length() )
       {
         result += 2000;
       }
       else
       {
         // Ought to compare lengths and valid ranges of parms
         // Ought to test translate Parms to see how closely they match
       }
     }
     return result;
   }
 
   public void store( PropertyWriter out )
     throws IOException
   {
     out.print( "Protocol", id.toString());
     out.print( "Protocol.name", name );
     if ( variantName.length() > 0 )
       out.print( "Protocol.variantName", variantName );
     Value[] parms = getDeviceParmValues();
     if (( parms != null ) && ( parms.length != 0 ))
       out.print( "ProtocolParms", DeviceUpgrade.valueArrayToString( parms ));
     if ( fixedData != null )
       out.print( "FixedData", getFixedData().toString());
   }
 
   public final static int tooDifferent = 0x7FFFFFFF;
 
   private String name = null;;
   protected Hex id = null;
   private Hex alternatePID = null;
   private String variantName = null;
   protected Hex fixedData = null;
   protected Hex defaultCmd = null;
   protected int cmdIndex;
   protected DeviceParameter[] devParms = null;
   protected Translate[] deviceTranslators = null;
   protected Translate[] devImportTranslators = null;
   protected CmdParameter[] cmdParms = null;
   protected Translate[] cmdTranslators = null;
   protected Translate[] importCmdTranslators = null;
   protected Importer[] devImporters = null;
   protected HashMap code = new HashMap( 4 );
   protected Initializer[] cmdParmInit = null;
   protected String notes = null;
   private Vector oldNames = new Vector();
   private Vector altPIDOverrideList = new Vector();
 }
