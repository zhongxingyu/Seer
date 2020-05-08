 package net.praqma.util.net;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.praqma.util.StopWatch;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.Logger.LogLevel;
 import net.praqma.util.debug.appenders.StreamAppender;
 import net.praqma.util.execute.CommandLine;
 
 public class Net {
 
 	private static Logger logger = Logger.getLogger();
 
 	private static final Pattern rx_nix_gateway = Pattern.compile( "^(0\\.0\\.0\\.0|default)\\s+(\\S+)\\s+.*$" );
 	private static final Pattern rx_ms_gateway = Pattern.compile( "^(Default Gateway:)\\s+(\\S+)\\s*$" );
 	// MS: Default Gateway:         10.10.1.1
 	
 	private static final Pattern rx_ms_ping = Pattern.compile( "^.*Average = (\\d+).*$", Pattern.DOTALL );
 	private static final Pattern rx_nix_ping = Pattern.compile( "^.*[\\d\\.]+/([\\d\\.]+)/[\\d\\.]+/[\\d\\.]+ ms.*$", Pattern.DOTALL );
 
 	public static String getDefaultGateway() throws IOException {
 		try {
 			List<String> list = CommandLine.getInstance().run( "netstat -rn" ).stdoutList;
 
 			logger.debug( "Out: " + list );
 			
 			switch( CommandLine.getInstance().getOS() ) {
 			case UNIX:
 				for( String l : list ) {
 					Matcher m = rx_nix_gateway.matcher( l );
 					if( m.find() ) {
 						return m.group( 2 );
 					}
 				}
 				break;
 				
 			case WINDOWS:
 				for( String l : list ) {
 					Matcher m = rx_ms_gateway.matcher( l );
 					if( m.find() ) {
 						return m.group( 2 );
 					}
 				}
 				break;
 			}
 			throw new IOException( "Not in the list" );
 		} catch( Exception e ) {
 			throw new IOException( "Unable to get default gateway: " + e.getMessage() );
 		}
 	}
 
 	public static void main( String[] args ) throws IOException {
 		StreamAppender app = new StreamAppender( System.out );
 		app.setMinimumLevel( LogLevel.DEBUG );
 		app.setTemplate( "[%level] %space%message%newline" );
 		Logger.addAppender( app );
 		String host = getDefaultGateway();
 		logger.info( "Host: " + host );
 		logger.info( "PING: " + ping( host, 10000 ) );
 	}
 	
 
 	public static double ping( String host, int timeout ) throws IOException {
 		try {
			StopWatch sw = StopWatch.get( "ping" );
 
 			sw.start();
 			String out = "";
 			Matcher m = null;
 			switch( CommandLine.getInstance().getOS() ) {
 			case WINDOWS:
 				out = CommandLine.getInstance().run( "ping -n 1 -w " + timeout + " " + host ).stdoutBuffer.toString();
 
 				m = rx_ms_ping.matcher( out );
 				if( m.find() ) {
 					return Double.parseDouble( m.group( 1 ) );
 				}
 				break;
 				
 			case UNIX:
 				out = CommandLine.getInstance().run( "ping -c 1 -w " + timeout + " " + host ).stdoutBuffer.toString();
 
 				m = rx_nix_ping.matcher( out );
 
 				break;
 				
 			default:
 				break;
 			}
 			
 			if( m.find() ) {
 				return Double.parseDouble( m.group( 1 ) );
 			}
 			
 			throw new IOException( "End of loop" );
 		} catch( Exception e ) {
 			throw new IOException( "Unable to ping " + host + ": " + e.getMessage() );
 		}
 	}
 	
 	public static double getPing( String host ) {
		StopWatch sw = StopWatch.get( "ping" );
 
 		sw.start();
 		try {
 			InetAddress address = InetAddress.getByName( host );
 			System.out.println( "Name: " + address.getHostName() );
 			System.out.println( "Addr: " + address.getHostAddress() );
 			System.out.println( "Reach: " + address.isReachable( 3000 ) );
 		} catch( UnknownHostException e ) {
 			System.err.println( "Unable to lookup web.mit.edu" );
 		} catch( IOException e ) {
 			System.err.println( "Unable to reach web.mit.edu" );
 		}
 		sw.stop();
 		
 		return sw.getSeconds();
 	}
 
 
 }
