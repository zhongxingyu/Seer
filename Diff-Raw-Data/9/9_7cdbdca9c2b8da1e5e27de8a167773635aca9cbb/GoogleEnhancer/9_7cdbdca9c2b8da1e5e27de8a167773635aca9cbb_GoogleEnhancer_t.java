 package es.weso.acota.core.business.enhancer;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.traversal.NodeIterator;
 
 import com.sun.org.apache.xpath.internal.XPathAPI;
 
 import es.weso.acota.core.CoreConfiguration;
 import es.weso.acota.core.business.enhancer.EnhancerAdapter;
 import es.weso.acota.core.entity.ProviderTO;
 import es.weso.acota.core.entity.TagTO;
 import es.weso.acota.core.exceptions.AcotaConfigurationException;
 import es.weso.acota.core.exceptions.DocumentBuilderException;
 import es.weso.acota.core.utils.DocumentBuilderHelper;
 
 /**
  * GoogleEnhancer is an {@link Enhancer} specialized in making calls to Google 
  * Autocomplete's API with the different {@link TagTO}'s labels, enriching
  * the set of {@link TagTO}s
  * 
  * @author Jose María Álvarez
  * @author César Luis Alvargonzález
  */
 public class GoogleEnhancer extends EnhancerAdapter implements Configurable {
 
 	protected static Logger logger = Logger.getLogger(GoogleEnhancer.class);
 	
 	protected static final String TEXT_XML = "text/xml";
 	protected static final String APPLICATION_XML = "application/xml";
 	
 	protected String googleUrl;
 	protected String googleEncoding;
 	
 	protected double googleRelevance;
 	
 	protected CoreConfiguration configuration;
 	
 	/**
 	 * Zero-argument default constructor
 	 * @throws AcotaConfigurationException Any exception that occurs 
 	 * while initializing Configuration object
 	 */
 	public GoogleEnhancer() throws AcotaConfigurationException{
 		super();
 		GoogleEnhancer.provider = new ProviderTO("Google Enhancer");
 		loadConfiguration(configuration);
 	}
 
 	/**
 	 * Zero-argument default constructor
 	 * @param configuration acota-core's configuration class
 	 * @throws AcotaConfigurationException Any exception that occurs 
 	 * while initializing Configuration object
 	 */
 	public GoogleEnhancer(CoreConfiguration core) throws AcotaConfigurationException{
 		super();
 		GoogleEnhancer.provider = new ProviderTO("Google Enhancer");
 		loadConfiguration(configuration);
 	}
 	
 	@Override
 	public void loadConfiguration(CoreConfiguration configuration) throws AcotaConfigurationException{
 		if(configuration==null)
 			configuration = new CoreConfiguration();
 		this.configuration = configuration;
 		this.googleUrl = configuration.getGoogleUrl();
 		this.googleEncoding = configuration.getGoogleEncoding();
 		this.googleRelevance = configuration.getGoogleRelevance();
 	}
 	
 	@Override
 	protected void execute() throws Exception {	
 		Set<Entry<String, TagTO>> backupSet = new HashSet<Entry<String, TagTO>>();
 		for (Entry<String, TagTO> label : tags.entrySet()) {
 			backupSet.add(label);
 		}
 		for (Entry<String, TagTO> label : backupSet) {
 			URL url = new URL(googleUrl + label.getKey().toString());
 			makeRESTCall(url);
 		}
 	}
 
 	@Override
 	protected void preExecute() throws Exception {
 		this.suggest = request.getSuggestions();
 		this.tags = suggest.getTags();
 		suggest.setResource(request.getResource());
 	}
 
 	@Override
 	protected void postExecute() throws Exception {
 		this.request.getTargetProviders().add(provider);
 		this.request.setSuggestions(suggest);
 	}
 
 	/**
 	 * Makes a REST Call to Google Autocomplete's API
 	 * @param url URL of the Rest Call
 	 * @throws IOException Signals that an I/O exception of some sort has occurred
 	 * @throws DocumentBuilderException Any exception that occurs during the DOM creation.
 	 * @throws TransformerException Any exception that occurs 
 	 */
 	protected void makeRESTCall(URL url) throws IOException,
 			DocumentBuilderException, TransformerException {
 		HttpURLConnection connection = null;
 		try {
 			connection = (HttpURLConnection) url.openConnection();
 			connection.setInstanceFollowRedirects(true);
 			connection.setRequestProperty("Accept", APPLICATION_XML);
 			try{
 				connection.connect();
 			}catch(Exception e){
 				connection.connect();
 			}
 			if (isValidResponse(connection)) {
 				Document document = processResponse(connection);
 				processDocument(document);
 			}
 		} finally {
 			if (null != connection) {
 				connection.disconnect();
 			}
 		}
 	}
 
 	/**
 	 * Transforms the Google Autocomplete REST call return to an XML Document
 	 * @param connection Rest call return
 	 * @return XML Document returned by the rest call
 	 * @throws UnsupportedEncodingException The Character Encoding is not supported.
 	 * @throws IOException Signals that an I/O exception of some sort has occurred
 	 * @throws DocumentBuilderException Any exception that occurs during the DOM creation.
 	 */
 	protected Document processResponse(HttpURLConnection connection)
 			throws UnsupportedEncodingException, IOException,
 			DocumentBuilderException {
 		BufferedReader rd = new BufferedReader(new InputStreamReader(
 				connection.getInputStream(), googleEncoding));
 		StringBuilder response = new StringBuilder();
 		try {
 			String line = null;
 			while ((line = rd.readLine()) != null) {
 				response.append(line);
 				response.append('\n');
 			}
 		} finally {
 			rd.close();
 		}
 		return DocumentBuilderHelper.getDocumentFromString(response.toString());
 	}
 
 	/**
 	 * Checks if a HTTP connection has a valid response (200 OK) 
 	 * @param connection HTTP Connection
 	 * @return true If the response is "HTTP/1.0 200 OK"
 	 * @return false In other case
 	 * @throws IOException if an error occurred connecting to the server.
 	 */
 	protected boolean isValidResponse(HttpURLConnection connection)
 			throws IOException {
 		return connection.getResponseCode() == HttpURLConnection.HTTP_OK
 				&& connection.getContentType().contains(TEXT_XML);
 	}
 
 	/**
 	 * Loads Google Autocomplete's XML result document into the tags map
 	 * @param result Google Autocomplete's XML result document
 	 * @throws TransformerException If happens an exceptional condition that
 	 * occurred during the transformation process.
 	 */
 	protected void processDocument(Document result) throws TransformerException {
 		NodeIterator it = XPathAPI.selectNodeIterator(result, "//suggestion/@data");
 		Node node = null;
 
 		while ((node = it.nextNode()) != null) {
 			TagTO tag = new TagTO(node.getNodeValue(), provider,
 					request.getResource());
 			fillSuggestions(tag, googleRelevance);
 		}
 	}
 
 }
