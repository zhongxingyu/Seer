 package org.linter;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.htmlparser.jericho.CharacterReference;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Source;
 
 
 /**
  * Basic ServiceParser, algorithmically pulls basic meta data from all providers
  */
 public class ServiceParserAlgorithmic extends ServiceParser {
 
 	/**
 	 * Logging prefix
 	 */
 	protected String _logPrefix;
 	
 	/**
 	 * URL pattern, wildcard
 	 */
 	private static final Pattern PATTERN = Pattern.compile( 
 			".*" 
 			);
 	
 	
 	
 	/**
 	 * Constructor
 	 */
 	public ServiceParserAlgorithmic() {
 	}
 	
 	/**
 	 * Initialize ServiceParser
 	 * 
 	 * @param url	Full URL
 	 */
 	public void initialize(String url) {
 		super.initialize( url );
 		_logPrefix = "[" + _url + "] ";
 	}
 	
 	/**
 	 * Get generic service pattern matching all URLs
 	 * 
 	 * @return Wildcard pattern .*
 	 */
 	@Override public Pattern getServicePattern() {
 		return PATTERN;
 	}	
 	
 	/*
 	 * Parse meta data using common meta data fields or algorithms
 	 * 
 	 * @return True if successful 
 	 */
 	@Override public boolean parse() {
 		Source source = getJerichoSource();
 
 		if( source == null ) {
 			logger.warn( "Source is null! Skipping." );
 			return false;
 		}
 		
 		parseTitle( source );
 		parseDescription( source );
 		parseFavIconUrl( source );	
 		parsePreviewImage( source );
 		parseWithSuccessor();
 		return true;
 	}
 	
 	/**
 	 * Parse page title, store to meta data
 	 * 
 	 * @param source	Jericho HTML parser source
 	 * @return 			True if successful
 	 */	
 	protected boolean parseTitle( Source source ) {
 		logger.trace(_logPrefix + "Scraping page title...");
 		
 		// Page title
 		String title = "";
 		boolean success = false;
 		
 		try {
 			Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
 			if (titleElement != null) {
 				title = CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
 				logger.trace(_logPrefix + "TITLE: " + title);
 			}
 			else {
 				logger.trace(_logPrefix + "Could not extract the page title");
 			}
 		} catch (Exception ex) {
 			logger.error(_logPrefix + "Error extracting page title: ", ex);
 		}
 		
 		getMetaData().put( "title", title );
 		
 		return success;
 	}
 
 	/**
 	 * Parse page description, store to meta data
 	 * 
 	 * @param source	Jericho HTML parser source
 	 * @return 			True if successful
 	 */	
 	protected boolean parseDescription( Source source ) {
 		logger.trace(_logPrefix + "Scraping description...");
 		
 		// Description
 		// NOTE: we assume that the first element with attribute name="description" is the meta description tag; else this will fail
 		String description = "";
 		boolean success = false;
 		
 		try {
 			Element descElement = source.getFirstElement("name", "description", false);
 			if (descElement != null && descElement.getName().equalsIgnoreCase(HTMLElementName.META)) {
 				String contentAttr = descElement.getAttributeValue("content");
 				if (contentAttr != null)
 					description = CharacterReference.decodeCollapseWhiteSpace(contentAttr);
 			}
 
 			// Check for og:description tag
 			// <meta property="og:description" content="Description text">
 			if( description.isEmpty() ) {
 				descElement = source.getFirstElement( "property", "og:description", false );
 				if( descElement != null && descElement.getName().equalsIgnoreCase( HTMLElementName.META ) ) {
 					String contentAttr = descElement.getAttributeValue( "content" );
 					if( contentAttr != null ) {
 						description = CharacterReference.decodeCollapseWhiteSpace( contentAttr );
 					}
 				}
 			}
 
 			if (description != null) {
 				logger.trace(_logPrefix + "DESCRIPTION: " + description);
 			} else {
 				logger.trace(_logPrefix + "Could not extract the page description");
 			}
 		} catch (Exception ex) {
 			logger.error(_logPrefix + "Error extracting page description: ", ex);
 		}
 				
 		logger.trace(_logPrefix + "Scraping complete.");
 		
 		getMetaData().put( "description", description );
 		
 		return success;
 	}
 	
 	/**
 	 * Parse page Favicon, store to meta data
 	 * 
 	 * @param source	Jericho HTML parser source
 	 * @return 			True if successful
 	 */		
 	protected boolean parseFavIconUrl( Source source ) {
 		logger.trace(_logPrefix + "Scraping favicon URL...");
 		
 		// Favicon
 		String favIconUrl = "";
 		boolean success = false;
 		
 		try {
 			// Get a list of all 'icon' and 'shortcut icon' elements
 			List<Element> relIconElements = new ArrayList<Element>();
 			relIconElements.addAll(source.getAllElements("rel", "icon", false));
 			relIconElements.addAll(source.getAllElements("rel", "shortcut icon", false));
 			
 			for (Element element : relIconElements) {
 				if (element.getName().equalsIgnoreCase(HTMLElementName.LINK)) {
 					String hrefAttr = element.getAttributeValue("href");
 					if (hrefAttr != null) {
 						favIconUrl = CharacterReference.decodeCollapseWhiteSpace(hrefAttr);
 						break;
 					}
 				}
 			}
 			
 			if (favIconUrl != null && !favIconUrl.isEmpty() ) {
 				if (!favIconUrl.contains(RELATIVE_URL_TEST)) {
 					logger.trace("Relative URL for favicon. Prefixing provider URL: " + getProviderUrl());
 					favIconUrl = getProviderUrl() + favIconUrl;
 				}
 				
 				logger.trace(_logPrefix + "FAVICON URL: " + favIconUrl);
 			} else {
 				logger.trace("[" + _url + "] Could not extract the fav icon URL");
 			}
 		} catch (Exception ex) {
 			logger.error(_logPrefix + "Error extracting fav icon URL: ", ex);
 		}
 		
 		getMetaData().put( "favIconUrl", favIconUrl );
 		
 		return success;
 	}
 
 	
 	/**
 	 * Parse page preview image url, store to meta data
 	 * 
 	 * @param source	Jericho HTML parser source
 	 * @return 			True if successful
 	 */		
 	protected boolean parsePreviewImage( Source source ) {
 		logger.trace( _logPrefix + "Seleceting preview image" );
 		
 		String imagePreviewUrl = null;
 		
 		// Check if an image preview is provided in known meta tags
 		// <meta property="og:image" content="http://www.provider.com/image.jpg" />
 		// <link rel="image_src" href="http://www.provider.com/image.jpg" />
 		Element imagePreviewOG = source.getFirstElement( "property", "og:image", false );
 		if( imagePreviewOG != null ) {			
 			String imagePreviewUrlOG = imagePreviewOG.getAttributeValue( "content" ); 
 			if( isPreviewUrlValid( imagePreviewUrlOG ) ) {
 				imagePreviewUrl = imagePreviewUrlOG;
 				logger.trace( _logPrefix + "Preview image found in og:image" );
 			}			
 		}
 		
 		if( imagePreviewUrl == null ) {
 			Element imagePreviewRel = source.getFirstElement( "rel", "image_src", false );
 			if( imagePreviewRel != null ) {
 				String imagePreviewUrlRel = imagePreviewRel.getAttributeValue( "href" );
 				if( isPreviewUrlValid( imagePreviewUrlRel ) ) {
 					imagePreviewUrl = imagePreviewUrlRel;
 					logger.trace( _logPrefix + "Preview image found in link rel=image_src" );
 				}				
 			}
 		}
 		
 		// If the preview image is not specified, determine it algorithmically
 		if( imagePreviewUrl == null ) {
 			AlgorithmicImageSelector selector = new AlgorithmicImageSelector( source, getProviderUrl(), _logPrefix );
 			imagePreviewUrl = selector.getPreviewUrl();
 			logger.trace( _logPrefix + "Preview image found algorithmically" );			
 		}		
		
 		// Store meta data
 		if( imagePreviewUrl != null && imagePreviewUrl.length() > 0 ) {
 			
 			// Append provider URL if relative URL
 			if( !imagePreviewUrl.contains(RELATIVE_URL_TEST) ) {
 				String provider = getProviderUrl();
				if( !provider.endsWith( "/" ) &&
					!imagePreviewUrl.startsWith( "/" ) ) {
 					provider += "/";
 				}
 				imagePreviewUrl = provider + imagePreviewUrl;
 			}
 			
 			getMetaData().put( "preview-image-url", imagePreviewUrl );
 			logger.trace( _logPrefix + "Preview image url: " + imagePreviewUrl );
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Basic test for valid image preview urls, 
 	 * Test for null, empty, and '/', Relative URLs are acceptable
 	 * 
 	 * @param url 		Image url
 	 * @return 			True if valid
 	 */
 	private boolean isPreviewUrlValid( String url ) {
 		boolean valid = false;
 		if( ( url != null ) 
 			&& ( !url.isEmpty() )
 			&& ( url.compareTo( "/") != 0 ) ) {
 			valid = true;
 		}
 		return valid;
 	}
 }
