 package confdb.converter;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import confdb.converter.ConverterBase;
 import confdb.converter.ConverterException;
 import confdb.converter.DbProperties;
 import confdb.converter.OfflineConverter;
 import confdb.data.IConfiguration;
 import confdb.db.ConfDB;
 import confdb.db.ConfDBSetups;
 
 
 public class BrowserConverter extends OfflineConverter
 {
     static private HashMap<Integer,BrowserConverter> map = new HashMap<Integer,BrowserConverter>();
     static private String[] dbNames = null;
     
     private PreparedStatement psSelectHltKeyFromRunSummary = null;
     
     private BrowserConverter(String dbType,String dbUrl,
 			     String dbUser,String dbPwrd) throws ConverterException
     {
     	super( "HTML", dbType, dbUrl, dbUser, dbPwrd );	
     }
     
 	public int getKeyFromRunSummary( int runnumber ) throws SQLException
 	{
 		if ( psSelectHltKeyFromRunSummary == null )
 			psSelectHltKeyFromRunSummary = getDatabase().getDbConnector().getConnection().prepareStatement( "SELECT HLTKEY FROM CMS_WBM.RUNSUMMARY WHERE RUNNUMBER=?" );
 		psSelectHltKeyFromRunSummary.setInt( 1, runnumber );
 		ResultSet rs = psSelectHltKeyFromRunSummary.executeQuery();
 		int key = -1;
 		if ( rs.next() )
 			key = rs.getInt(1);
 		return key;
 	}
     
 	protected void finalize() throws Throwable
 	{
 		super.finalize();
 		ConfDB db = getDatabase();
 		if ( db != null )
 			db.disconnect();
 	}
 
 	static public BrowserConverter getConverter( String dbName ) throws ConverterException 
 	{
 		return getConverter( getDbIndex(dbName) );
 	}
 	
 	
 	static public BrowserConverter getConverter( int dbIndex ) throws ConverterException 
 	{
 	    ConfDBSetups dbs = new ConfDBSetups();
 	    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
 	    String dbUser = dbProperties.getDbUser();
 	    if (dbUser.endsWith("_w"))
 	    	dbUser = dbUser.substring(0,dbUser.length()-1)+"r";
 	    else if (dbUser.endsWith("_writer"))
 	    	dbUser = dbUser.substring(0,dbUser.length()-6)+"reader";
 	    else
 	    	dbUser = "cms_hlt_reader";
 
 	    dbProperties.setDbUser(dbUser);
 	    
 	    BrowserConverter converter = map.get( new Integer( dbIndex ) );
 		if ( converter == null )
 		{
 			converter = new BrowserConverter( dbs.type( dbIndex ), dbProperties.getDbURL(), dbProperties.getDbUser(), "convertme!" );		
 			map.put( new Integer( dbIndex ), converter );
 		}
 		return converter;
 	}
 
 	static public void deleteConverter( ConverterBase converter )
 	{
 		Set<Map.Entry<Integer,BrowserConverter>> entries = map.entrySet();
 		for ( Map.Entry<Integer,BrowserConverter> entry : entries )
 		{
 			if ( entry.getValue() == converter )
 			{
 				map.remove( entry.getKey() );
 				return;
 			}
 		}
 	}
 	
     static public String getDbName( int dbIndex )
     {
      	if ( dbNames == null )
      	{
      		ConfDBSetups dbs = new ConfDBSetups();
      		dbNames = dbs.labelsAsArray();
      	}
      	return dbNames[ dbIndex ];
     }
 
     
     static public int getDbIndex( String dbName )
     {
     	if ( dbName.equalsIgnoreCase( "hltdev" ) )
     		dbName = "HLT Development";
 
 	  	int setupCount = new ConfDBSetups().setupCount();
   		for ( int i = 0; i < setupCount; i++ )
   		{
   			if ( dbName.equalsIgnoreCase( getDbName( i ) ) )
   				return i;
 		}
   		return -1;
     }
     
     static public String[] listDBs()
     {
     	ConfDBSetups dbs = new ConfDBSetups();
     	if ( dbNames == null )
     		dbNames = dbs.labelsAsArray();
     	ArrayList<String> list = new ArrayList<String>();
      	for ( int i = 0; i < dbs.setupCount(); i++ )
      	{
     		String name = dbs.name(i);
     		if ( name != null && name.length() > 0  )
     		{
     			String host = dbs.host(i);
     			if (     host != null 
        				 && !host.equalsIgnoreCase("localhost") 
    				     && !host.endsWith( ".cms") )
    				     {
    				    	 list.add( getDbName(i) );
    				     }
     		}
     	}
     	return list.toArray( new String[ list.size() ] );
     }
     
     static public String[] getAnchors( int dbIndex, int configKey ) throws ConverterException
     {
 		ArrayList<String> list = new ArrayList<String>();
 		ConverterBase converter = null;
     	try {
 			converter = BrowserConverter.getConverter( dbIndex );
 			IConfiguration conf = converter.getConfiguration( configKey );
 			if ( conf == null )
 				list.add( "??" );
 			else
 			{
 				if ( conf.pathCount() > 0 )
 					list.add( "paths" );
 				if ( conf.sequenceCount() > 0 )
 					list.add( "sequences" );
 				if ( conf.moduleCount() > 0 )
 					list.add( "modules" );
 				if ( conf.edsourceCount() > 0 )
 					list.add( "ed_sources" );
 				if ( conf.essourceCount() > 0 )
 					list.add( "es_sources" );
 				if ( conf.esmoduleCount() > 0 )
 					list.add( "es_modules" );
 				if ( conf.serviceCount() > 0 )
 					list.add( "services" );
 			}
 		} catch (ConverterException e) {
 			if ( converter != null )
 				BrowserConverter.deleteConverter( converter );
 			throw e;
 		}
 		return list.toArray( new String[ list.size() ] );
     }
     
     static public int getCacheEntries()
     {
     	return ConfCache.getNumberCacheEntries();
     }
 
     static public class UrlParameter {
     	public boolean asFragment = false;
     	public String format = "python";
     	public int configId = -1;
     	public String configName = null;
     	public int runNumber = -1;
     	public String dbName = "orcoff";
         public HashMap<String,String> toModifier = new HashMap<String,String>();
         
         UrlParameter() {}
     }
 
     static public UrlParameter getUrlParameter( Map<String,String[]> map ) throws ConverterException
     {
     	if ( map.isEmpty())
     		throw new ConverterException( "ERROR: configId or configName or runNumber must be specified!" );
     		
     	UrlParameter p = new UrlParameter();
     	Set<Map.Entry<String,String[]>> parameters = map.entrySet(); 
     	for ( Map.Entry<String,String[]> entry : parameters )
     	{
     		if ( entry.getValue().length > 1 )
     			throw new ConverterException( "ERROR: Only one parameter '" + entry.getKey() + "' allowed!" );
 		
     		String value = entry.getValue()[ 0 ];
     		String key = entry.getKey();
     		if ( key.equals("configId"))
     			p.configId = Integer.parseInt( value );
     		else if ( key.equals("configKey"))
     			p.configId = Integer.parseInt( value );
     		else if ( key.equals("runNumber"))
     			p.runNumber = Integer.parseInt( value );
     		else if (key.equals( "configName")) {  
     			p.configName = value;
     		}
     		else if (key.equals( "cff")) {
     			p.asFragment =true;
     			p.toModifier.put( key, value );
     		}
     		else if (key.equals( "format")) {
     			p.format = value;
     		}
     		else if ( key.equals( "dbIndex" ) )
     			p.dbName = BrowserConverter.getDbName( Integer.parseInt( value ) );
     		else if ( key.equals( "dbName" ) ) 
     			p.dbName = value;
     		else {
     			p.toModifier.put(entry.getKey(),value);
     		}
     	}
 
     	if ( p.configId == -1  &&  p.configName == null && p.runNumber == -1 )
     		throw new ConverterException( "ERROR: configId or configName or runNumber must be specified!" );
 
     	int moreThanOne = ( p.configId != -1 ? 1 : 0 ) 
 			+  ( p.configName != null ? 1 : 0 )
 			+  ( p.runNumber != -1 ? 1 : 0 );
     	if ( moreThanOne > 1 ) 
     		throw new ConverterException( "ERROR: configId *OR* configName *OR* runNumber must be specified!" );
 		return p;
 	}
 
 	
 }
