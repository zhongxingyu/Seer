 package ecologylab.net;
 
 import java.io.InputStream;
 import java.net.URLConnection;
 
 import ecologylab.generic.Debug;
 
 public class PURLConnection extends Debug
 {
 	protected InputStream			inputStream;
 	protected URLConnection			urlConnection;
 	protected String				mimeType;
 
 	/**
 	 * Fill out the instance of this resulting from a succcessful connect().
 	 * @param purl TODO
 	 * @param urlConnection
 	 * @param inStream
 	 * @param container
 	 * @param infoCollector
 	 */
 	//TODO change to package level access when ParsedURL moves
 	public PURLConnection(URLConnection urlConnection, InputStream inputStream)
 	{
 		this.inputStream	= inputStream;
 		this.urlConnection	= urlConnection;
 	}
 
 	public void recycle()
 	{
 		// parsing done. now free resources asap to avert leaking and memory fragmentation
 		// (this is a known problem w java.net.HttpURLConnection)
 		URLConnection urlConnection	= this.urlConnection;
 		if (urlConnection != null)
 		{
 			NetTools.disconnect(urlConnection);
 			this.urlConnection		= null;
 		}
 		InputStream inputStream		= this.inputStream;
 		if (inputStream != null)
 		{
 			try
 			{
 				inputStream.close();
 			} catch (Exception e)
 			{
 			}
 			this.inputStream		= null;
 		}
 		mimeType					= null;
 	}
 
 	/**
 	 * @return Returns the inputStream.
 	 */
 	public InputStream inputStream()
 	{
 		return inputStream;
 	}
 
 	/**
 	 * @return Returns the urlConnection.
 	 */
 	public URLConnection urlConnection()
 	{
 		return urlConnection;
 	}
 
	/**
	 * Find the mime type returned by the web server to the URLConnection, in its header.
	 * Thus, if there is no URLConnection (as for local file system), this always returns null.
	 * 
	 * @return	the mime type or null
	 */
 	public String mimeType()
 	{
 		String result				= this.mimeType;
		if ((result == null) && (urlConnection != null))
 		{
 			result					= urlConnection.getContentType();
 			if (result != null)
 			{
 				// create the appropriate DocumentType object
 				// lookout for mime types with charset appened
 				int semicolonIndex	= result.indexOf(';');
 				if (semicolonIndex > 0)
 					result			= result.substring(0, semicolonIndex);
 				this.mimeType		= result;
 			}
 		}
 		return result;
 	}
 }
