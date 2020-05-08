 package org.linter;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.Proxy;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 
 public class LintedPage {
 	static private Logger logger = Logger.getLogger(LintedPage.class);
 	
 	private boolean _parseOk = false;
 	private String _originalUrl;
 	private String[] _aliases = {};
 	private String _destinationUrl;
 	private String _title;
 	private String _description;
 	private String _favIconUrl;
 	
 	private long _processingTime;
 	
 	private TagNode _node;
 	
 	/**
 	 * Create a blank linted page, expects you to call {@link process} at some point 
 	 * @param originalUrl - the URL to be processed
 	 */
 	public LintedPage(String originalUrl) {
 		_originalUrl = originalUrl;
 	}
 	
 	/***
 	 * Process the original URL, including alias resolution, scraping and metadata extraction
 	 */
 	void process() {
 		final long startTime = System.nanoTime();
 		final long endTime;
 		try {
 			processRunner();
 		} finally {
 		  endTime = System.nanoTime();
 		}
 		_processingTime = endTime - startTime;
 	}
 	
 	/***
 	 * Process the original URL associated with this LintedPage
 	 */
 	protected void processRunner() {
 		logger.info("Processing URL: " + _originalUrl);
 		
 		logger.debug("Expanding any shortened URLs...");
 		followUrlRedirects();
 		
 		logger.debug("Scraping & cleaning HTML...");
 		scrapeMetadata();
 	}
 	
 	/***
 	 * Follows the originalUrl to its destination, saving any aliases along the way. This is useful to expand
 	 * URL shortening services.
 	 * @return True if successful, false otherwise
 	 */
 	public boolean followUrlRedirects() {
 		ArrayList<String> aliases = new ArrayList<String>();
 		
 		String nextLocation = _originalUrl;
 		String lastLocation = nextLocation;
 		
 		while (nextLocation != null) {
 			try {
 				URL url = new URL(nextLocation);
 				
 				logger.trace("Following " + nextLocation + "...");
 				
 				HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
 				connection.setInstanceFollowRedirects(false);
 				connection.setRequestMethod("HEAD");
 				connection.setConnectTimeout(Linter.HTTP_CONNECT_TIMEOUT);
 				connection.connect();
 				
 				nextLocation = connection.getHeaderField("Location");
 				if (nextLocation != null) {
 					logger.trace("Discovered redirect to " + nextLocation);
 					aliases.add(lastLocation);
 					lastLocation = nextLocation;
 				} else {
 					logger.trace("URL resolved to its destination");
 					_destinationUrl = lastLocation;
 				}
 				connection.disconnect();
 			} catch (MalformedURLException ex) {
 				logger.error("Invalid URL [" + nextLocation + "]: " + ex.getMessage());
 				return false;
 			} catch (IOException ioe) {
 				logger.error("IO Exception [" + nextLocation + "]: " + ioe.getMessage());
 				return false;
 			}
 		}
 		
 		_aliases = aliases.toArray(new String[0]);
 
 		return true;
 	}
 	
 	/**
 	 * Scrapes the metadata on this page (can be called separately from {@link process}
 	 */
 	public void scrapeMetadata() {
 		HtmlCleaner cleaner = new HtmlCleaner();
 		try {
 			_node = cleaner.clean(new URL(this.getDestinationUrl()));
 		} catch (MalformedURLException mue) {
 			logger.error("Invalid URL [" + this.getDestinationUrl() + "]: " + mue.toString());
 			return;
 		} catch (Exception ex) {
 			logger.error("Unable to scrape and clean HTML: " + ex.toString());
 			return;
 		}
 
 		// Page title
 		try {
 			Object[] titleNodes = _node.evaluateXPath("//head/title");
 			if (titleNodes != null) {
 				_title = ((TagNode)titleNodes[0]).getText().toString().trim();
 			}
 		} catch (Exception e) {
			logger.warn("Error extracting the page title: " + e.toString());
 		}
 		
 		// Page description
 		try {
 			Object[] descNodes = _node.evaluateXPath("//head/meta[@name='description']");
 			if (descNodes != null) {
 				_description = ((TagNode)descNodes[0]).getAttributeByName("content").trim();
 			}
 		} catch (Exception e) {
			logger.warn("Error extracting the page description: " + e.toString());
 		}
 		
 		// Fav icon
 		try {
 			Object[] favIconNodes = _node.evaluateXPath("//head/link[@rel='icon']");
 			if (favIconNodes != null) {
 				_favIconUrl = ((TagNode)favIconNodes[0]).getAttributeByName("href").trim();
 			}
 		} catch (Exception e) {
			logger.warn("Error extracting the favicon URL: " + e.toString());
 		}
 		
 		_parseOk = true;
 	}
 	
 	/***
 	 * Whether or not the parse completed successfully
 	 * @return
 	 */
 	public Boolean getParseOk() {
 		return _parseOk;
 	}
 	
 	/***
 	 * Returns the original URL (before any shortened URLs were expanded)
 	 * @return
 	 */
 	public String getOriginalUrl() {
 		return _originalUrl;
 	}
 	
 	/***
 	 * Gets any aliases for this URL -- e.g. any redirects between the original URL and its actual destination
 	 * @return
 	 */
 	public String[] getAliases() {
 		return _aliases;
 	}
 	
 	/***
 	 * Gets the page title
 	 * @return
 	 */
 	public String getTitle() {
 		return _title;
 	}
 	
 	/***
 	 * Gets the final destination, after expanding any shortened URLs starting from the original URL
 	 * @return
 	 */
 	public String getDestinationUrl() {
 		return _destinationUrl;
 	}
 	
 	/***
 	 * Gets the page descripton (from <meta name='description'>)
 	 * @return
 	 */
 	public String getDescription() {
 		return _description;
 	}
 	
 	/***
 	 * Gets the page's favicon (from <link rel='icon'>)
 	 * @return
 	 */
 	public String getFavIconUrl() {
 		return _favIconUrl;
 	}
 	
 	/***
 	 * Gets the processing time in milliseconds
 	 * @return
 	 */
 	public long getProcessingTimeMillis() {
 		return TimeUnit.NANOSECONDS.toMillis(_processingTime);
 	}
 	
 	/***
 	 * Gets the processing time in human-readable format
 	 * @return
 	 */
 	public String getProcessingTimeForHumans() {
 
 		// mm:ss is:
 		//	    TimeUnit.NANOSECONDS.toMinutes(_processingTime),
 		//	    TimeUnit.NANOSECONDS.toSeconds(_processingTime) - 
 		//	    TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(_processingTime))
 		
 		Float millis = new Float(getProcessingTimeMillis() / 1000);
 		return String.format("%.4f", millis);
 	}
 	
 	public String toDebugString() {
 		StringBuilder sb = new StringBuilder(_originalUrl);
 		sb.append(" {\n");
 		sb.append("\tPARSE OK:\t\t"); sb.append(this.getParseOk()); sb.append('\n');
 		sb.append("\tALIASES:");
 			if (_aliases == null || _aliases.length == 0)
 				sb.append("\t\tNONE\n");
 			else {
 				sb.append('\n');
 				for (String alias : _aliases) {
 					sb.append("\t\t"); sb.append(alias); sb.append('\n');
 				}
 			}
 		sb.append("\tDEST URL:\t\t"); sb.append(this.getDestinationUrl()); sb.append('\n');
 		sb.append("\tPAGE TITLE:\t\t"); sb.append(this.getTitle()); sb.append('\n');
 		sb.append("\tDESCRIPTION:\t\t"); sb.append(this.getDescription()); sb.append('\n');
 		sb.append("\tFAV ICON:\t\t"); sb.append(this.getFavIconUrl()); sb.append('\n');
 		sb.append("} in "); sb.append(this.getProcessingTimeForHumans()); sb.append(" s\n");
 		return sb.toString();
 	}
 }
