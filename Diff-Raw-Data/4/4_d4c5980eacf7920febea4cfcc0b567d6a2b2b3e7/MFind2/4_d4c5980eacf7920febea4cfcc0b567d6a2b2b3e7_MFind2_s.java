 package net.jumperz.app.MFind2;
 
 //import net.jumperz.util.*;
 import net.jumperz.sql.*;
 import net.jumperz.util.MObjectArray;
 import net.jumperz.util.MRegEx;
 import net.jumperz.util.MSystemUtil;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.sql.*;
 import java.util.*;
 
 import org.h2.server.pg.PgServer;
 import org.h2.tools.Server;
 import java.io.*;
 
 public class MFind2
 extends MAbstractLogAgent
 {
 public static final String ADDR = "127.0.0.1";
 public static final int PORT = 35078;
 private String pwd;
 private static String tableName = "pwd";
 private ServerSocket sSocket;
 private Socket socket;
 private int pgPort;
 private static int maxDepth = Integer.MAX_VALUE;
 private static int maxCount = 1000000;
 private static int count;
 private static Server server;
 private static PgServer pgServer;
 private static volatile boolean terminated;
 //--------------------------------------------------------------------------------
 public static void main( String[] args )
 throws Exception
 {
 ( new MFind2() ).start( args );
 }
 //--------------------------------------------------------------------------------
 private void parseArgs( String[] args )
 {
 for( int i = 0; i < args.length; ++i )
 	{
 	if( args[ i ].equals( "--max-depth" ) )
 		{
 		maxDepth = Integer.parseInt( args[ i + 1 ] );
 		++i;
 		}
 	if( args[ i ].equals( "--max-count" ) )
 		{
 		maxCount = Integer.parseInt( args[ i + 1 ] );
 		}
 	}
 }
 //--------------------------------------------------------------------------------
 public void start( String[] args )
 throws Exception
 {
 parseArgs( args );
 
 server1();
 
 pgPort = 30000 + ( new Random() ).nextInt( 35500 );
 debug( "pgPort:" + pgPort );
 debug( "Please wait..." );
 
 if( args.length > 0 )
 	{
 	pwd = args[ 0 ];
 	}
 
 String dbDir = System.getProperty( "java.io.tmpdir" );
 dbDir = "~";
 Connection conn = MSqlUtil.getConnection( "jdbc:h2:" + dbDir + "/find2." + pgPort + ";LOG=0;CACHE_SIZE=100000;LOCK_MODE=0;UNDO_LOG=0", "sa", "sa" );
 //conn.setAutoCommit( false );
 try
 	{
 	MSqlUtil.executeUpdate2( conn, "CREATE ALIAS dir FOR \"net.jumperz.app.MFind2.MFind2.dir\"" );
 	MSqlUtil.executeUpdate2( conn, "CREATE ALIAS k FOR \"net.jumperz.app.MFind2.MFind2.k\"" );
 	MSqlUtil.executeUpdate2( conn, "CREATE ALIAS m FOR \"net.jumperz.app.MFind2.MFind2.m\"" );
 	MSqlUtil.executeUpdate2( conn, "CREATE ALIAS g FOR \"net.jumperz.app.MFind2.MFind2.g\"" );
 	MSqlUtil.executeUpdate2( conn, "CREATE ALIAS shutdownFind2 FOR \"net.jumperz.app.MFind2.MFind2.shutdownFind2\"" );
 	//MSqlUtil.executeUpdate2( conn, "CREATE VIEW pwd1 AS SELECT name, size, t, type, depth FROM " + tableName );
 	}
 catch( Exception e )
 	{
 	}
 MSqlUtil.getInt2( conn, "select dir( '" + pwd + "')" );
 //conn.commit();
 conn.close();
 
 String[] pgArgs = new String[]{ "-pg", "-pgPort", pgPort + "", "-baseDir", dbDir };
 pgServer = new PgServer();
 server = new Server( pgServer, pgArgs );
 server.start();
 
 socket.getOutputStream().write( ( pgPort + "\n" ).getBytes() );
 BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
 String received = reader.readLine();
 if( received != null && received.equals( "OK" ) )
 	{
 	debug( "Database is ready." );
 	}
 else
 	{
 	debug( "Launcher is not responding." );
 	System.exit( 0 );
 	}
 socket.close();
 sSocket.close();
 
 int idleCount = 0;
 while( !terminated && idleCount <= 5 ) // exit if idle for 30 seconds
 	{
 	MSystemUtil.sleep( 5 * 1000 );
 	Set running = getRunning( pgServer );
 	if( running.size() == 0 )
 		{
 		debug( "idle..." );
 		++ idleCount;
 		}
 	else
 		{
 		debug( "conn found." );
 		idleCount = 0;
 		}
 	}
 
 debug( "shutdown2" );
 pgServer.stop();
 server.shutdown();
 }
 //--------------------------------------------------------------------------------
 public void server1()
 throws Exception
 {
 sSocket = new ServerSocket( PORT, 2, InetAddress.getByName( ADDR ) );
 socket = sSocket.accept();
 debug( "Connection Accepted." );
 }
 //--------------------------------------------------------------------------------
 public static void sleep2( int ms )
 {
 try
 	{
 	Thread.sleep( ms );
 	}
 catch( Exception e )
 	{
 	e.printStackTrace();
 	}
 }
 //--------------------------------------------------------------------------------
 public static int shutdownFind2( Connection conn )
 throws SQLException
 {
 terminated = true;
 
 ( new Thread() {
 public void run()
 {
sleep2( 3000 );
//server.shutdown();
System.out.println( "shutdown1" );
 pgServer.stop();
 server.shutdown();
 }
 } ).start();
 
 return 0;
 }
 //--------------------------------------------------------------------------------
 public static long k( Connection conn, int size )
 throws SQLException
 {
 return 1024L * size;
 }
 //--------------------------------------------------------------------------------
 public static long m( Connection conn, int size )
 throws SQLException
 {
 return 1024L * 1024 * size;
 }
 //--------------------------------------------------------------------------------
 public static long g( Connection conn, int size )
 throws SQLException
 {
 return 1024L * 1024 * 1024 * size;
 }
 //--------------------------------------------------------------------------------
 public static int dir( Connection conn, String dirName )
 throws SQLException
 {
 File dir = new File( dirName );
 if( !dir.exists() || !dir.isDirectory() )
 	{
 	return -1;
 	}
 
 if( !dirName.endsWith( "/" ) )
 	{
 	dirName = dirName + "/" ;
 	}
 
 {
 StringBuffer buf = new StringBuffer();
 buf.append( "create table if not exists " );
 buf.append( tableName );
 buf.append( "(" );
 buf.append( "name varchar," );
 buf.append( "size bigint," );
 buf.append( "t timestamp," );
 buf.append( "type varchar," );
 buf.append( "dirname varchar," );
 buf.append( "filename varchar," );
 buf.append( "ext varchar," );
 buf.append( "depth int" );
 buf.append( ")" );
 MSqlUtil.executeUpdate( conn, buf.toString() );
 }
 		
 createIndexOnAllColumns( conn );
 
 MSqlUtil.executeUpdate2( conn, "delete from " + tableName );
 
 {
 int columnCount = getColumntCount( conn );
 StringBuffer buf = new StringBuffer();
 buf.append( "insert into " );
 buf.append( tableName );
 buf.append( " values (" );
 boolean first = true;
 for( int i = 0; i < columnCount; ++i )
 	{
 	if( !first )
 		{
 		buf.append( "," );
 		}
 	buf.append( "?" );
 	first = false;
 	}
 buf.append( ")" );
 
 PreparedStatement ps = conn.prepareStatement( buf.toString() );
 
 insertFileInfo( ps, tableName, dir, 0, 0, dirName.length() );
 
 ps.executeBatch();
 }
 
 return 0;
 }
 //--------------------------------------------------------------------------------
 private static int getColumntCount( Connection conn )
 throws SQLException
 {
 String queryString = "select * from " + tableName + " limit 0";
 ResultSet rs = conn.createStatement().executeQuery( queryString );
 ResultSetMetaData md = rs.getMetaData();
 return md.getColumnCount();
 }
 //--------------------------------------------------------------------------------
 private static void createIndexOnAllColumns( Connection conn )
 throws SQLException
 {
 String queryString = "select * from " + tableName + " limit 0";
 ResultSet rs = conn.createStatement().executeQuery( queryString );
 ResultSetMetaData md = rs.getMetaData();
 int columnCount = md.getColumnCount();
 for( int i = 0; i < columnCount; ++i )
 	{
 	String columnName = md.getColumnName( i + 1 );
 	MSqlUtil.executeUpdate2( conn, "create index  if not exists " + columnName + "_asc_idx on "  + tableName + " ( " + columnName + " ) " );
 	MSqlUtil.executeUpdate2( conn, "create index  if not exists " + columnName + "_desc_idx on " + tableName + " ( " + columnName + " desc ) " );
 	}
 }
 //--------------------------------------------------------------------------------
 private static void insertFileInfo( PreparedStatement ps, String tableName, File dir, int depth, int batchCount, int pwdLength )
 {
 if( depth > maxDepth )
 	{
 	return;
 	}
 
 try
 	{
 	File[] files = dir.listFiles();
 	for( int i = 0; i < files.length; ++i )
 		{
 		if( count >= maxCount )
 			{
 			return;
 			}
 		File file = files[ i ];
 		String name = ( dir.getAbsolutePath() + "/" + file.getName() ).substring( pwdLength );
 		String dirName = null;
 		if( depth == 0 )
 			{
 			dirName = "";
 			}
 		else
 			{
 			dirName = dir.getAbsolutePath().substring( pwdLength );
 			}
 		
 		String type = null;
 		if( isSymlink( file ) )
 			{
 			type = "link";
 			}
 		else if( file.isFile() )
 			{
 			type = "file";
 			}
 		else if( file.isDirectory() )
 			{
 			type = "dir";
 			}
 		else
 			{
 			type = "unknown";
 			}
 		
 		MObjectArray args = new MObjectArray();
 		args.add( name );
 		args.add( file.length() );
 		args.add( new Timestamp( file.lastModified() ) );
 		args.add( type );
 		args.add( dirName );
 		args.add( file.getName() );
 		args.add( MRegEx.getMatch( "\\.[a-zA-Z0-9]{1,8}$", file.getName() ) );
 		args.add( depth );
 		
 		MSqlUtil.setArgs( ps, args );
 		//ps.executeUpdate();
 		
 		
 		ps.addBatch();
 		++batchCount;
 		++count;
 		
 		if( ( batchCount % 10000 ) == 0 )
 			{
 			batchCount = 0;
 			ps.executeBatch();
 			}
 		
 		if( !type.equals( "link" ) && file.isDirectory() )
 			{
 			insertFileInfo( ps, tableName, file, depth + 1, batchCount, pwdLength );
 			}
 		}
 	}
 catch( Exception e )
 	{
 	e.printStackTrace();
 	}
 
 }
 //--------------------------------------------------------------------------------
 public static Set getRunning( PgServer pgServer )
 {
 try
 	{
 	Object object = pgServer;
 	String fieldName = "running";
 	Field field = null;
 	Method method = null;
 	Class clazz = null;
 
 	clazz  = Class.forName( "org.h2.server.pg.PgServer" );
 	field = clazz.getDeclaredField( fieldName );
 	field.setAccessible( true );
 	object = field.get( object );
 
 	return ( Set )object;	
 	}
 catch( Exception e )
 	{
 	e.printStackTrace();
 	return null;
 	}
 }
 //--------------------------------------------------------------------------------
 //From Apache Commons
 public static boolean isSymlink( File file )
 throws IOException
 {
 File parent = null;
 
 if( file.getParent() == null )
 	{
         parent = file;
         }
 else
 	{
 	File canonicalDir = file.getParentFile().getCanonicalFile();
 	parent = new File( canonicalDir, file.getName() );
         }
 
 if( parent.getCanonicalFile().equals( parent.getAbsoluteFile() ) )
 	{
 	return false;
 	}
 else
 	{
 	return true;
 	}
 }
 //--------------------------------------------------------------------------------
 }
