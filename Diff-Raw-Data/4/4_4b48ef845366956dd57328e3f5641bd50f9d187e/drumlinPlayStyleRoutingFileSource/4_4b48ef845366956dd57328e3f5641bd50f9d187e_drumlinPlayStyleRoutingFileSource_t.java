 package com.rathravane.drumlin.service.framework.routing.playish;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import com.rathravane.drumlin.util.logSetup;
 import com.rathravane.drumlin.util.throwableLoggingHelper;
 
 public class drumlinPlayStyleRoutingFileSource extends drumlinStaticEntryPointRoutingSource
 {
 	public drumlinPlayStyleRoutingFileSource ( File f ) throws IOException
 	{
 		this ( f, true );
 	}
 
 	public drumlinPlayStyleRoutingFileSource ( File f, boolean withAutoRefresh ) throws IOException
 	{
 		super ();
 
 		loadRoutes ( f );
 		createRefreshThread ( f, withAutoRefresh );
 	}
 
 	public drumlinPlayStyleRoutingFileSource ( URL u ) throws IOException
 	{
 		super ();
 
		if ( u == null )
		{
			throw new IOException ( "URL for routing file is null in drumlinPlayStyleRoutingFileSource ( URL u )" );
		}
 		loadRoutes ( u );
 	}
 
 	private static final Logger log = logSetup.getLog ( drumlinPlayStyleRoutingFileSource.class );
 
 	private synchronized void loadRoutes ( URL u ) throws IOException
 	{
 		loadRoutes ( new InputStreamReader ( u.openStream () ) );
 	}
 
 	private synchronized void loadRoutes ( File f ) throws IOException
 	{
 		loadRoutes ( new FileReader ( f ) );
 	}
 
 	private synchronized void loadRoutes ( Reader r ) throws IOException
 	{
 		clearRoutes ();
 
 		final BufferedReader fr = new BufferedReader ( r );
 		
 		String line;
 		while ( ( line = fr.readLine () ) != null )
 		{
 			line = line.trim ();
 			if ( line.length () > 0 && !line.startsWith ( "#" ) )
 			{
 				processLine ( line );
 			}
 		}
 	}
 
 	private void processLine ( String line )
 	{
 		try
 		{
 			final StringTokenizer st = new StringTokenizer ( line );
 			final String verb = st.nextToken ();
 			if ( verb.toLowerCase ().equals ( "package" ) )
 			{
 				final String pkg = st.nextToken ();
 				addPackage ( pkg );
 			}
 			else
 			{
 				final String path = st.nextToken ();
 				final String action = st.nextToken ();
 				addRoute ( verb, path, action );
 			}
 		}
 		catch ( NoSuchElementException e )
 		{
 			log.warning ( "There was an error processing route config line: \"" + line + "\"" );
 		}
 		catch ( IllegalArgumentException e )
 		{
 			log.warning ( "There was an error processing route config line: \"" + line + "\": " + e.getMessage () );
 		}
 	}
 
 	private Thread createRefreshThread ( final File f, boolean routeRefresh )
 	{
 		Thread result = null;
 		if ( routeRefresh )
 		{
 			result = new Thread ()
 			{
 				private long fLastMod = f.lastModified ();
 	
 				@Override
 				public void run ()
 				{
 					try
 					{
 						sleep ( 2000 );
 					}
 					catch ( InterruptedException e1 )
 					{
 						// ignore
 					}
 	
 					final long lastMod = f.lastModified (); 
 					if ( lastMod > fLastMod )
 					{
 						log.info ( "Reloading routes from " + f.getAbsolutePath () );
 						try
 						{
 							fLastMod = lastMod;
 							loadRoutes ( f );
 						}
 						catch ( IOException e )
 						{
 							throwableLoggingHelper.log ( log, e );
 						}
 					}
 				}					
 			};
 			result.setName ( "Route file update watcher for " + f.getName () + "." );
 			result.setDaemon ( true );
 			result.start (); 	// FIXME: clunky, and not cool to start thread in constructor's scope
 		}
 		return result;
 	}
 }
