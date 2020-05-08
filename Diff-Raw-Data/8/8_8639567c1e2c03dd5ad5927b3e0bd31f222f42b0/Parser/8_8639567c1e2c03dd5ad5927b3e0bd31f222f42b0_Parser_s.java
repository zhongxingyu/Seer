 package com.benbenedek.parser;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.htmlcleaner.CleanerProperties;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.PrettyXmlSerializer;
 import org.htmlcleaner.TagNode;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /**
  * @author Ben Benedek
  *
  *	Main Parsing unit.
  */
 public class Parser implements IParser {
 	
 	private static final int TIMEOUT_WEBCLIENT = 15000;
 	
 	private HtmlCleaner cleaner;
 	private WebClient webClient;
 	private URL urlSrc;
 	private CleanerProperties properties;
 	private HtmlPage page;
 	//private Document xmlOutput;
 	private TagNode tagNode;
 	volatile private ParserState state;
 	private org.jsoup.nodes.Document jsoupDoc;
 	
 	enum ParserState {
 		INIT,
 		READY,
 		IN_PROGRESS,
 		ERROR,
 		COMPLETE
 	}
 	
 	private String charSet = "utf-8";
 	
 	private static final Logger logger = Logger.getLogger(Parser.class);
 	
 	
 	public Parser() {
 		initParser();
 	}
 	
 	/**
 	 * Create instance of parser from URL.
 	 * @see java.net.URL
 	 * @param URL src
 	 */
 	public Parser(URL src) {
 		setInput(src);
 		initParser();
 	}
 	
 	
 	/**
 	 * Create instance of parser from String URL source.
 	 * @param String src
 	 */
 	public Parser(String src) {
 		setInput(src);
 		initParser();
 	}
 		
 	public void setInput(String src) {
 		try {
 			this.urlSrc = new URL(src);
 			setState(ParserState.READY);
 		} catch (MalformedURLException e) {
 			logger.error("Invalid URL inserted as source for Parser.");
 			setState(ParserState.ERROR);
 			throw new RuntimeException("Invalid URL inserted as source for Parser.");
 		}		
 	}
 	public void setInput(URL src) {
 		this.urlSrc = src;	
 		setState(ParserState.READY);
 	}
 	
 	/**
 	 * Public Parse API, parse the source returns no output.
 	 */
 	public void parse() {
 		setState(ParserState.IN_PROGRESS);
 		logger.info("Parse API started.");
 		validateSource();		
 		validateProperties();
 		logger.info("Parsing " + urlSrc.toString() + ".");
 		Long preTime = new Date().getTime(); 
 		this.tagNode = run();
 		try {
 			page = webClient.getPage(urlSrc);
 			
 			// HACK FOR ALJEZIRA
 			// final HtmlSubmitInput button = form.getInputByName("Load more comments");
 			// END HACK
		} catch (FailingHttpStatusCodeException | IOException e) {
 			logger.error("Could not parse (using webClient): " + urlSrc.toString() + ".");
 			setState(ParserState.ERROR);
 			return;
 		}
 		finally {
 			webClient.closeAllWindows();
 		}
 		//System.out.println(page.asXml());
 		Long postTime = new Date().getTime();
 		logger.info("Parsing " + urlSrc.toString() + " finished successfully in " + (postTime - preTime) + "ms.");
 		setState(ParserState.COMPLETE);
 	}
 	
 	private TagNode run() {
 		TagNode tagNode = null;
 		try {
 			jsoupDoc = Jsoup.parse(urlSrc, TIMEOUT_WEBCLIENT);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			logger.error("Unexpected error while parsing." + "\n" + e1.getMessage());
 			setState(ParserState.ERROR);
 			throw new RuntimeException("Unexpected error while parsing.");
 		}
 		try {
 			// Actual parsing.
 			tagNode = cleaner.clean( urlSrc );
 		} catch (IOException e) {
 			e.printStackTrace();
 			logger.error("Unexpected error while parsing." + "\n" + e.getMessage());
 			setState(ParserState.ERROR);
 			throw new RuntimeException("Unexpected error while parsing.");
 		}
 		
 		return tagNode;
 	}
 	
 	private void validateSource() {
 		// Check if we have input source to parse.
 		if ( urlSrc == null ) { 
 			logger.error("No input source.");
 			setState(ParserState.ERROR);
 			throw new RuntimeException("No input source.");
 		}
 	}
 
 	private void validateProperties() {
 		// Check if we have properties file, if not, create it.
 		if (( properties == null ) && (cleaner != null)) {
 			properties = cleaner.getProperties();
 		}
 		else {
 			logger.error("Properties file is null, default parser was not initiaited.");
 			throw new RuntimeException("Properties file is null, default parser was not initiaited.");
 		}
 	}
 	
 	private void initParser() {
 		this.cleaner = new HtmlCleaner();
 		webClient = new WebClient();
 		webClient.getOptions().setTimeout(TIMEOUT_WEBCLIENT);
 		setState(ParserState.INIT);
 		logger.info("Parser initiated");
 	}
 	
 	/**
 	 * Creates a Document object from parsed output.
 	 * @see org.w3c.dom.Document
 	 */
 	public void createDocumentObject() { 
 
 		Long preTime = new Date().getTime();
 		// xmlString = StringEscapeUtils.unescapeXml(page.asXml());
 		// xmlString = StringEscapeUtils.escapeXml(page.asXml());
 		/*
 		 * DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		 * dbf.setNamespaceAware(false); dbf.setValidating(false);
 		 * dbf.setCoalescing(true); dbf.setIgnoringComments(true);
 		 * System.out.println(page.asText()); System.out.println(xmlString);
 		 * dbf.setExpandEntityReferences(true); DocumentBuilder db =
 		 * dbf.newDocumentBuilder();
 		 */
 		jsoupDoc = Jsoup.parse(page.asXml());
 		// InputSource is = new InputSource(new StringReader(xmlString));
 		// this.xmlOutput = db.parse(is);
 		
 		Long postTime = new Date().getTime();
 		logger.info("Created DOM Object successfully. Finished in "
 				+ (postTime - preTime) + " ms.");
 		
 	}
 	
 	/**
 	 * Outputs parsed XML into xml file.
 	 * @param fileName
 	 */
 	public void outputToXML(String fileName) {
 		try {
 			new PrettyXmlSerializer(properties).writeToFile(tagNode, fileName, charSet);
 			logger.info("Created XML File ( " + fileName + " ) successfully.");
 		} catch (IOException e) {
 			logger.error("Error while writing parse results to file.\n" + e.getMessage());
 			e.printStackTrace();
 			setState(ParserState.ERROR);
 			throw new RuntimeException("Error while writing parse results to file.");
 		}
 	}
 	
 	public String getXmlOutputAsString() {
 		try {
 			return new PrettyXmlSerializer(properties).getAsString(tagNode, charSet);
 		} catch (IOException e) {
 			logger.error("Error while writing parse results to string.\n" + e.getMessage());
 			e.printStackTrace();
 			setState(ParserState.ERROR);
 			throw new RuntimeException("Error while writing parse results to file.");		}
 	}
 	
 	/**
 	 * Returns the parsed XML as Document object.
 	 * @see org.jsoup.nodes.Document
 	 * @return
 	 */
 	public Document getXmlOutput() {
 		if (state != ParserState.COMPLETE) {
 			logger.error("Error getting XML Output.\n");
 			throw new RuntimeException("Parser is not complete.");
 		}
 		return jsoupDoc;
 	}
 
 
 	public void setUrlSrc(URL urlSrc) {
 		this.urlSrc = urlSrc;
 	}
 
 
 	public void setProperties(CleanerProperties properties) {
 		this.properties = properties;
 	}
 
 
 	public void setCharSet(String charSet) {
 		this.charSet = charSet;
 	}
 	
 	private void setState(ParserState state) {
 		if (this.state != null)
 			logger.info("Parser changed state, old state: " + this.state.name() + ", new state: " + state.name() + ".");
 		this.state = state;
 	}
 }
