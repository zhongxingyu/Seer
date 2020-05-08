 package landrop;
 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.DecimalFormat;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 import landrop.ui.DropForm;
 
 /**
  * Useful functions...
  */
 public class Util
 {
 	private final static long SECOND = 1000;
 	private final static long SECONDS_IN_MINUTE = 60;
 
 	private final static long MINUTE = SECOND * SECONDS_IN_MINUTE;
 	private final static long MINUTES_IN_HOUR = 60;
 
 	private final static long HOUR = MINUTE * MINUTES_IN_HOUR;
 	private final static long HOURS_IN_DAY = 24;
 
 	private final static long DAY = HOUR * HOURS_IN_DAY;
 
 	public static Image getApplicationIcon()
 	{
		return Toolkit.getDefaultToolkit().getImage( DropForm.class.getResource( "airplaneIcon.png" ) );
 	}
 	/**
 	 * Converts time (in milliseconds) to human-readable format
 	 * "<w> days, <x> hours, <y> minutes and (z) seconds"
 	 * 
 	 * from http://www.rgagnon.com/javadetails/java-0585.html
 	 */
 	public static String millisToLongDHMS( long duration )
 	{
 		StringBuffer res = new StringBuffer();
 		long temp = 0;
 		if ( duration >= SECOND )
 		{
 			temp = duration / DAY;
 			if ( temp > 0 )
 			{
 				duration -= temp * DAY;
 				res.append( temp ).append( " day" ).append( temp > 1 ? "s" : "" ).append( duration >= MINUTE ? ", " : "" );
 			}
 
 			temp = duration / HOUR;
 			if ( temp > 0 )
 			{
 				duration -= temp * HOUR;
 				res.append( temp ).append( " hour" ).append( temp > 1 ? "s" : "" ).append( duration >= MINUTE ? ", " : "" );
 			}
 
 			temp = duration / MINUTE;
 			if ( temp > 0 )
 			{
 				duration -= temp * MINUTE;
 				res.append( temp ).append( " minute" ).append( temp > 1 ? "s" : "" );
 			}
 
 			if ( !res.toString().equals( "" ) && ( duration >= SECOND ) )
 				res.append( " and " );
 
 			temp = duration / SECOND;
 			if ( temp > 0 )
 				res.append( temp ).append( " second" ).append( temp > 1 ? "s" : "" );
 			return res.toString();
 		}
 		else
 			return "0 seconds";
 	}
 
 	/**
 	 * Returns the MD5 digest of the given data, in the familiar hex-string format.
 	 * Example: 4a20374bf419ed3c7b1fa3bc18e7922a
 	 */
 	public static String md5( byte[] inputData )
 	{
 		try
 		{
 			MessageDigest algorithm = MessageDigest.getInstance( "MD5" );
 			algorithm.update( inputData );
 			return digestToHexString( algorithm );
 		}
 		catch ( NoSuchAlgorithmException ex )
 		{
 			JOptionPane.showMessageDialog( null, "Couldn't initialize MD5", "Error", JOptionPane.ERROR_MESSAGE );
 			return "";
 		}
 	}
 
 	/**
 	 * Converts an MD5 digest to a human-readable hex string (e.g. 4a20374bf419ed3c7b1fa3bc18e7922a).
 	 */
 	public static String digestToHexString( MessageDigest digest )
 	{
 		BigInteger number = new BigInteger( 1, digest.digest() );
 		return number.toString( 16 );
 	}
 
 	/**
 	 * Formats the given number of bytes into human-readable format (e.g. "72.7 KB").
 	 */
 	public static String formatFileSize( double d )
 	{
 		String[] types = { "bytes", "KB", "MB", "GB", "TB", "PB", "XB", "ZB", "YB" };
 
 		int index = 0;
 		if ( d > 0 )
 			index = Math.min( types.length - 1, (int) ( Math.log( d ) / Math.log( 1024 ) ) );
 		return new DecimalFormat( "0.0" ).format( d / Math.pow( 1024, index ) ) + " " + types[index];
 	}
 
 	/**
 	 * Uses the native OS's UI styling.
 	 */
 	public static void useNativeLookAndFeel()
 	{
 		try
 		{
 			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
 		}
 		catch ( Exception e )
 		{
 		}
 	}
 
 	public static void safeSleep( int milliseconds )
 	{
 		try
 		{
 			Thread.sleep( milliseconds );
 		}
 		catch ( InterruptedException e )
 		{
 		}
 	}
 
 }
