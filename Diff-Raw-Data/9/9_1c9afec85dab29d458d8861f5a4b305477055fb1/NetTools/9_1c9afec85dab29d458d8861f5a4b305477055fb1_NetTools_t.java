 package ecologylab.net;
 
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.Generic;
 
 /**
  * Reusable static methods that do nifty network stuff.
  * 
  * @author andruid
  * @author blake
  * @author eunyee
  *
  */
 public class NetTools extends Debug
 {
 	final static String SUPPORTED_CHARSETS[]	=
 	{
 		"us-ascii", "windows-1250", "windows-1251", "windows-1252", "windows-1253",
 		"windows-1254", "windows-1257", "iso-8859-1", "iso-8859-2", "iso-8859-4",
 		"iso-8859-5", "iso-8859-7", "iso-8859-9", "iso-8859-13", "iso-8859-15",
 		"ISO-8859-1", "ISO-8859-2", "ISO-8859-4",
 		"ISO-8859-5", "ISO-8859-7", "ISO-8859-9", "ISO-8859-13", "ISO-8859-15",
 		"koi8-r", "utf-8", "utf-16", "utf-16be", "utf-16le",
 		"UTF-8", "UTF-16", "UTF-16be", "UTF-16le"
 	};
 	final static HashMap supportedCharsetMap	= Generic.buildHashMapFromStrings(SUPPORTED_CHARSETS);
 	
 	/**
 	 * Seek a charset specification in the MimeType header of the HTTP request.
 	 * The return values are strange, in order to enable reporting an error to happen conveniently
 	 * around the call site.
 	 *
 	 * @param mimeType	The Mime Type header.
 	 * 
 	 * @return			Null if the charset is supported (including if there is no specificaton of it in the header).
 	 * 					The charset that is unsupported, if that is the case.
 	 */
 	public static String isCharsetSupported(String mimeType)
 	{
 		if (mimeType == null)
 			return null;
 		
 		int charsetIndex	= mimeType.indexOf("charset");
 		if (charsetIndex > -1)
 		{
 			int equalsIndex	= mimeType.indexOf('=', charsetIndex);
 			if (equalsIndex++ > -1)		// seek and skip over the equals
 			{
 				int closingSemIndex	= mimeType.indexOf(';', equalsIndex);
				String charset = null;
				if( equalsIndex >= closingSemIndex )
				{
					charset		= (closingSemIndex == -1) ? mimeType.substring(equalsIndex) :
 							mimeType.substring(closingSemIndex, equalsIndex);
				}
				
 				if ((charset != null) && (charset.length() > 0))
 				{
 					charset		= charset.trim();
 					if (charset.startsWith("\""))
 						charset	= charset.substring(1);
 					if (charset.endsWith("\""))
 						charset	= charset.substring(0, charset.length() - 1);
 					//println("CHARSET: '" + charset + "'");
 					if (!supportedCharsetMap.containsKey(charset))
 					{
 						return charset;
 					}
 				}
 			}
 		}
 		return null;
 		/*
 		 StringTokenizer st	= new StringTokenizer( mimeType, ";= ");
 		 String encoding		= null;
 		 while(st.hasMoreTokens())
 		 {
 		 if( st.nextToken().equals("charset"))
 		 {
 		 encoding = st.nextToken();
 		 println("ENCODING : " + encoding);
 		 }
 		 }
 		 if( (encoding != null) && !supportedCharsetMap.containsKey(encoding) )
 		 {
 		 infoCollector.displayStatus("Cant process charset " + encoding + " in " + purl.toString() );
 		 return null;
 		 }
 		 */
 	}
 	
 /**
  * Free resources as possible on the URLConnection passed in.
  * 
  * This is accomplished by calling disconnect() if it turns out to be an instance of
  * HttpURLConnection.
  * 
  * @param urlConnection	a reference to a URLConnection.
  * 
  * @return	true if the URLConnection reference passed in is not null.
  */
 	public static void disconnect(URLConnection urlConnection)
 	{
 		if ((urlConnection != null) && (urlConnection instanceof HttpURLConnection))
 		{
 			HttpURLConnection	httpConnection	= (HttpURLConnection) urlConnection;
 			httpConnection.disconnect(); // free resources!
 		}
 	}
 
 	static String localHost = null;
 	/**
 	 * local host address (parse out only IP address)
 	 * @return
 	 */
 	public static String localHost()
 	{
 		String localHost			= NetTools.localHost;
 		if (localHost == null)
 		{
 			try
 			{
 				localHost			= InetAddress.getLocalHost().toString();
 				//		localHost = localHost.replace('/','_');
 				localHost			= localHost.substring(localHost.indexOf('/')+1);
 				NetTools.localHost	= localHost;
 			} catch (UnknownHostException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return localHost;
 	}
 	
 }
