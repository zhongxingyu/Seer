 package net.praqma.util.debug;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 
 
 /**
  * A simple logger class.
  * @author wolfgang
  *
  */
 public class Logger
 {
 	private static Logger logger              = null;
 	private static FileWriter fw              = null;
 	private static BufferedWriter out         = null;
 	private static String path                = "./";
 	private static SimpleDateFormat format    = null;
 	private static SimpleDateFormat logformat = null;
 	private static Calendar nowDate           = null;
 	
 	private static File file                  = null;
 	
 	private static boolean enabled            = true;
 	private static boolean traceEnabled       = true;
 	
 	private static final String filesep       = System.getProperty( "file.separator" );
 	private static final String linesep       = System.getProperty( "line.separator" );
 	
 	private static ArrayList<String> trace    = null;
 	
 	private static boolean append             = false;
 	
 	/* Styling */
 	private static final int typemaxlength    = 8;
 	private static final int methodmaxlength  = 55;
 	private static final boolean indent       = false;
 	
 	
 	private static Map<String, String> exclude = new HashMap<String, String>();
 	
 	
 	private Logger( boolean append, boolean homePath )
 	{
 		Logger.append = append;
 		
 		nowDate   = Calendar.getInstance();
 		
 		format    = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
 		logformat = new SimpleDateFormat( "yyyyMMdd" );
 		
 		trace = new ArrayList<String>();
 		
 		if( homePath )
 		{
 			setPathHomeLogs();
 		}
 		else
 		{
 			newDate( nowDate );
 		}
 	}
 	
 	public static Logger getLogger( boolean append, boolean homePath )
 	{
 		if( logger == null )
 		{
 			logger = new Logger( append, homePath );
 		}
 		
 		return logger;
 	}
 	
 	public static Logger getLogger( boolean append )
 	{
 		if( logger == null )
 		{
 			logger = new Logger( append, true );
 		}		
 		
 		return logger;
 	}
 	
 	public static Logger getLogger( )
 	{
 		return getLogger( true, true );
 	}
 	
 	
 	public void excludeClass( String eclass )
 	{
 		exclude.put( eclass, "" );
 	}
 	
 	public void disable()
 	{
 		enabled = false;
 	}
 	
 	public void enable()
 	{
 		enabled = true;
 	}
 	
 	public void enableTrace()
 	{
 		traceEnabled = true;
 	}
 	
 	public void setPath( String path )
 	{
 		Logger.path = path;
 		newDate( nowDate );
 	}
 	
 	public boolean setPathHomeLogs()
 	{
 		String path = System.getProperty( "user.home" ) + filesep + "logs" + filesep;
 		File file = new File( path );
 		
 		/* Existence + creation */
 		if( !file.exists() )
 		{
 			boolean created = false;
 			try
 			{
				created = file.mkdir();
 			}
 			catch ( Exception e )
 			{
 				created = false;
 			}
 			
 			if( !created )
 			{
 				return false;
 			}
 		}
 		
 		Logger.path = path;
 		
 		newDate( nowDate );
 		
 		return true;
 	}
 	
 	private static Calendar getDate( Calendar c )
 	{
 		Calendar c2 = Calendar.getInstance(  );
 		c2.clear();
 		c2.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 		
 		//c2.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
 		//c2.set( Calendar.MILLISECOND, c.get( Calendar.MILLISECOND ) );
 
 		return c2;
 	}
 	
 	private static void newDate( Calendar n )
 	{
 		nowDate = n;
 		
 		if( fw != null )
 		{
 			try
 			{
 				fw.close();
 				out.close();
 			}
 			catch ( IOException e )
 			{
 				System.err.println( "Could not close file writer and/or buffered writer." );
 			}
 		}
 		
 		try
 		{
 			file = new File( path + "debug_" + logformat.format( nowDate.getTime() ) + ".log" );
 			fw = new FileWriter( file , append );
 			
 		}
 		catch ( IOException e )
 		{
 			//System.err.println( "Cannot use the specified path, \"" + path + "\". Defaulting to current working directory." );
 			//path = "./";
 			try
 			{
 				file = new File( "debug_" + logformat.format( nowDate.getTime() ) + ".log" );
 				fw = new FileWriter( file, append );
 			}
 			catch ( IOException e1 )
 			{
 				System.err.println( "Failed to use current working directory. Quitting!" );
 				System.exit( 1 );
 			}
 		}
 		
 		//System.out.println( "LOGGER USING " + file.getAbsolutePath() );
 		
 		out = new BufferedWriter( fw );
 	}
 	
 	public String getPath()
 	{
 		return file.getAbsolutePath();
 	}
 	
 	public void stacktrace()
 	{
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		StringBuffer sb = new StringBuffer();
 		sb.append( "Tracing" + linesep );
 		for( int i = 2 ; i < stack.length ; i++ )
 		{
 			sb.append( stack[i].getClassName() + "::" + stack[i].getMethodName() + "," + stack[i].getLineNumber() + linesep );
 		}
 		
 		_log( sb.toString(), "trace", 3 );
 	}
 	
 	public void trace_function( )
 	{
 		if( traceEnabled )
 		{
 			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 			trace.add( stack[2].getClassName() + "::" + stack[2].getMethodName() );
 		}
 	}
 	
 	public void print_trace( )
 	{
 		print_trace( true );
 	}
 	
 	public void print_trace( boolean tolog )
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append( "Function tracing" + linesep );
 		 
 		for( int i = 0 ; i < trace.size() ; i++ )
 		{
 			if( tolog )
 			{
 				sb.append( "[" + i + "] " + trace.get( i ) + linesep );
 			}
 			else
 			{
 				System.out.println( trace.get( i ) );
 			}
 		}
 		if( tolog )
 		{
 			_log( sb.toString(), "trace", 3 );
 		}
 	}
 	
 	public void print( String msg )
 	{
 		System.out.println( msg );
 	}
 	
 	public void log( String msg )
 	{
 		_log( msg, "info", 3 );
 	}
 	
 	public void debug( String msg )
 	{
 		_log( msg, "debug", 3 );
 	}
 	
 	public void warning( String msg )
 	{
 		/* Testing! */
 		//System.err.println( msg );
 		_log( msg, "warning", 3 );
 	}
 	
 	public void exceptionWarning( String msg )
 	{
 		//System.err.println( msg );
 		_log( msg, "warning", 4 );
 	}
 	
 	public void error( String msg )
 	{
 		/* Testing */
 		//System.err.println( msg );
 		_log( msg, "error", 3 );
 	}
 	
 	public void log( String msg, String type )
 	{
 		_log( msg, type, 3 );
 	}
 	
 	public void empty( String msg )
 	{
 		_log( msg, null, 3 );
 	}
 	
 	private void _log( String msg, String type, int size )
 	{
 		if( !enabled )
 		{
 			return;
 		}
 		
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		if( exclude.containsKey( stack[size].getClassName() ) )
 		{
 			return;
 		}
 		
 		if( type != null )
 		{
 			
 			if( type.length() > Logger.typemaxlength )
 			{
 				type = type.substring( 0, 8 );
 			}
 			
 			/* Check if the date is changed */
 			Calendar now = Calendar.getInstance();
 	
 			if( getDate( now ).after( getDate( nowDate ) ) )
 			{
 				newDate( now );
 			}
 	
 			String stackMsg = stack[3].getClassName() + "::" + stack[3].getMethodName() + "," + stack[3].getLineNumber();
 			String msg_ = format.format( now.getTime() ) + " [" + type + "] " + new String(new char[Logger.typemaxlength - type.length()]).replace("\0", " ") + stackMsg;
 			
 			try
 			{
 				if( indent )
 				{
 					out.write( msg_ + ": " + new String(new char[Logger.methodmaxlength - stackMsg.length()]).replace("\0", " ") + msg + linesep );
 				}
 				else
 				{
 					out.write( msg_ + ": " + msg + linesep );
 				}
 				out.flush();
 			}
 			catch ( IOException e )
 			{
 				e.printStackTrace();
 			}
 			catch( NegativeArraySizeException e )
 			{
 				try
 				{
 					out.write( msg_ + ": " + msg + linesep );
 					out.flush();
 				}
 				catch ( IOException e1 )
 				{
 					e1.printStackTrace();
 				}
 				
 			}
 		}
 		else
 		{
 			try
 			{
 				out.write( msg + linesep );
 				out.flush();
 			}
 			catch ( IOException e )
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 }
 
 
 
 
