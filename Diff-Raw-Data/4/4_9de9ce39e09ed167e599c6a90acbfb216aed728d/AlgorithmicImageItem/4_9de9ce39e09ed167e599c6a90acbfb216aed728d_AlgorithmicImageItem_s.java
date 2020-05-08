 package org.linter;
 
 import java.awt.image.BufferedImage;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.Proxy;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.imageio.ImageIO;
 
 /**
  * Algorithmic Image Item 
  * 
  * Stores image data for algorithmically determining best preview image
  */
 public class AlgorithmicImageItem {
 	protected static final String RELATIVE_URL_TEST = "://";
 	public static final int UNSPECIFIED_IMAGE_DIM = -1;
 	public static final float UNSPECIFIED_ASPECT_RATIO = -1;
 	
 	// Image URL
 	String _url;
 	
 	// Image Id, blank string if DNE
 	String _id;
 	
 	// Image Class Name, blank string if DNE
 	String _class;
 	
 	// Image height, -1 if unspecified
 	int _height;
 	
 	// Image width, -1 if unspecified
 	int _width;
 	
 	// File Size
 	int _fileSize = 0;
 	
 	// Potential image score
 	float _score;
 	
 	/*
 	 * Constructor
 	 * @param providerUrl Provider URL for fixing relative links
 	 */
 	public AlgorithmicImageItem() {
 		_url = new String();
 		_id = new String();
 		_class = new String();
 		_height = UNSPECIFIED_IMAGE_DIM;
 		_width = UNSPECIFIED_IMAGE_DIM;
 		_score = 0;
 	}
 	
 	/*
 	 * Set URL
 	 * Set the URL attribute and fix relative links
 	 * @param url
 	 * @param providerUrl
 	 */
 	public void setUrl( String url, String providerUrl ) {
 		// Check if image src is relative url
 		if( url != null ) {			
 			if( !url.contains(RELATIVE_URL_TEST) ) {
 				
 				if( !providerUrl.endsWith( "/" ) ) {
 					providerUrl += "/";
 				}				
 				_url = providerUrl + url;
 				
 			} else {
 				_url = url;
 			}
 		}
 	}
 	
 	public String getUrl() {
 		return _url;
 	}
 
 	public void setId( String id ) {
 		_id = ( id == null ) ? new String() : id;
 	}
 	
 	public void setClass( String classname ) {
 		_class = ( classname == null ) ? new String() : classname;
 	}
 	
 	public void setHeight( String height ) {
 		_height = dimensionStringToInt( height );
 	}
 	
 	public void setWidth( String width ) {
 		_width = dimensionStringToInt( width );
 	}	
 	
 	public int getWidth() {
 		return _width;
 	}
 	
 	public int getHeight() {
 		return _height;
 	}
 	
 	/*
 	 * Add to Potential Image Score
 	 * @param changeAmount Increment or decrement amount of score
 	 */
 	public void addToScore( float changeAmount ) {
 		_score += changeAmount;
 	}
 	
 	public float getScore() {
 		return _score;
 	}
 	
 	/*
 	 * Do Image Attributes Contain a String
 	 * Search for image string in Id and Class attributes
 	 * @param str String to match
 	 * @return true if matches
 	 * 
 	 */
 	public boolean doAttributesContainString( String str ) {
 		boolean ret = false;
 		
 		if( _id.toLowerCase().indexOf( str ) != UNSPECIFIED_IMAGE_DIM ||
 			_class.toLowerCase().indexOf( str ) != UNSPECIFIED_IMAGE_DIM ) {
 			ret = true;
 		}
 		
 		return ret;
 	}
 	
 	/*
 	 * Get Image Extension
 	 * @return Image extension, blank if not found
 	 */
 	public String getExtension() {
 		String extension = new String();
 		
 		if( !_url.isEmpty() ) {
 			int lastPeriod = _url.lastIndexOf( "." );
			extension = _url.substring( lastPeriod );
 		}
 
 		return extension;
 	}
 
 	/*
 	 * Download an Image
 	 * Download a remote image and set height and width attributes
 	 */
 	public void downloadImage() {
 		final int CONNECT_TIMEOUT = 2000;
 		final int READ_TIMEOUT = 2000;
 		
 		try {
 			URL url = new URL( _url );	
 			URLConnection connection = url.openConnection();
 			connection.setConnectTimeout( CONNECT_TIMEOUT );
 			connection.setReadTimeout( READ_TIMEOUT );
 			connection.connect();
 			
 			_fileSize = connection.getContentLength();
 			
 			InputStream inputStream = connection.getInputStream();
 			BufferedImage img = ImageIO.read( inputStream );
 			
 			_width = img.getWidth();
 			_height = img.getHeight();
 		} catch( Exception e ) {
 		}
 	}
 	
 	/*
 	 * Download an Image HTTP Headers
 	 * Download just the HTTP Headers for an image. Primarily for 
 	 * determining file size. 
 	 * 
 	 * Currently unused.
 	 */
 	public void downloadImageHead() {
 		try {
 		URL url = new URL( _url );
 		
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
 		connection.setInstanceFollowRedirects(false);
 		connection.setRequestMethod("HEAD"); // only want the headers
 		connection.setConnectTimeout(LintedPage.HTTP_CONNECT_TIMEOUT);
 		connection.setReadTimeout(LintedPage.HTTP_READ_TIMEOUT);
 		connection.setRequestProperty("User-Agent", LintedPage.HTTP_USER_AGENT);
 		connection.connect();
 		
 		
 		//int contentLength = connection.getContentLength();
 		
 		} catch( Exception e ) {
 			
 		}
 	}
 
 	/*
 	 * Get Aspect Ratio
 	 * @return float aspect ratio, UNSPECIFIED_ASPECT_RATIO if undefined image sizes
 	 */
 	public float getAspectRatio() {
 		float aspectRatio = UNSPECIFIED_ASPECT_RATIO;
 		
 		if( _width > 0 && _height > 0 ) {
 			aspectRatio = (float) _width / (float) _height;
 		}
 		return aspectRatio;
 	}
 	
 	/*
 	 * Get Image Area
 	 * @return height*width of image, 0 if either dimension is undefined
 	 */
 	public int getImageArea() {
 		int area = 0;
 		if( _width > 0 && _height > 0 ) {
 			area = _width * _height;
 		}
 		return area;
 	}
 	
 	/*
 	 * Get File Size
 	 * @return int file size in bytes
 	 */
 	public int getFileSize() {
 		return _fileSize;
 	}
 	
 	/*
 	 * Dimension String to Integer
 	 * Convert a dimension string from HTML attribute to an integer. Removes any non-digit
 	 * text (e.g. "100px" -> 100 )
 	 */
 	private int dimensionStringToInt( String dimension ) {
 		if( dimension == null || dimension.isEmpty() ) {
 			return UNSPECIFIED_IMAGE_DIM;
 		}
 		
 		String dimensionNum = dimension.replaceAll( "[^0-9]", "" );		
 		int dimensionInt = UNSPECIFIED_IMAGE_DIM;
 		if( !dimensionNum.isEmpty() ) {
 			try {
 				dimensionInt = Integer.parseInt( dimensionNum );
 			} catch( NumberFormatException e ) {
 									
 			}
 		}
 			
 		return dimensionInt;
 	}
 }
