 package se.starbox.models;
 
 import java.io.BufferedReader;
 import se.starbox.util.SearchResult;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.LinkedList;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 //import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
 import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
 import org.apache.solr.client.solrj.request.DirectXmlRequest;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 //import org.apache.solr.core.CoreContainer;
 //import org.apache.solr.schema.UUIDField;
 
 
 
 /**
  * A Model class for searching files in your local index.
 *
 * @author Linus, Robin, Kim
 *
 */
 public class SearchModel {
 
 	// Handle for Solr
 	private static SolrServer solr = null;
 	
 	/**
 	 * URI to the Solr server.
 	 */
 	public final String SOLR_URI = "http://localhost:8080/starbox-solr-server/";
 
 	/**
 	* Initiate the model instance. Creates an instance of Solr on startup if
 	* one has not been created already.
 	* 
 	* @throws MalformedURLException
 	*/
 	public SearchModel() {
 		// Initilize Solr
 		if (solr == null)
 			getSolr();
 		
 		//checkConnection();
 		//testFill();
 	}
 
 	/**
 	 * This is a test function for development purposes. It fills Solr with data in a local index.
 	 */
 	public void testFill() {
 		File testIndexData = new File("C:/Users/Kim/workspace/Starbox/exempelIndexData.xml");
 		
 		String doc = getStringFromDocument(testIndexData);
 		System.out.println("Read file, content was: " + doc);
 		
 		DirectXmlRequest xmlreq = new DirectXmlRequest("/update", doc);
 		try {
 			solr.request(xmlreq);
 		} catch (SolrServerException e) {
 			System.err.println("Error updating solr");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("Error updating solr");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Reads a file into a string.
 	 * @param file
 	 * @return a string with the content of file
 	 */
 	private String getStringFromDocument(File file) {
 		BufferedReader br = null;
 	
 		try {
 			br = new BufferedReader(new FileReader(file));
 		} catch (FileNotFoundException fe) {
 			System.err.println("Error! Couldnt find file " + file.getName());
 			fe.printStackTrace();
 		}
 		
 		String line;
 		StringBuilder sb = new StringBuilder();
 		
 		try {
 			while((line=br.readLine()) != null)
 				sb.append(line);
 		} catch(IOException e) {
 			System.err.println("Error reading line.");
 			e.printStackTrace();
 		}
 		
 		return sb.toString();
 	}
 
 	@SuppressWarnings("unused")
 	private void checkConnection() {
 		try {
 			solr.ping();
 		} catch (SolrServerException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * This method creates an embedded instance of Solr.
 	 * If theres an exception solr will be set to <b>null</b>.
 	 */
 	private void getSolr() {
 		try {
 			solr = new CommonsHttpSolrServer(SOLR_URI);
 		} catch(MalformedURLException mex) {
 			System.err.println("Error (MalformedURLException) in SearchModel. Here is the stack trace:");
 			mex.printStackTrace();
 			solr = null;
 		}
 	}
 
 
 	/**
 	* Searches the index of Solr.
 	* @param SearchString - The string which you are searching for
 	* @param params - Parameters to filter the search, on this format:
 	* 		 "filetype:exe;minfilesize:20;maxfilesize:10"
 	* 
 	* @return Returns a LinkedList<SearchResult> with the matches, null if empty.
 	*/
 	public LinkedList<SearchResult> query(String searchString, String params){
 		// If search string is empty, simply return an empty serachresult array.
 		if (searchString == "") 
 			searchString = "*:*";
 		
 		SolrQuery solrQuery = null;
 	    
 	    // Update the search query with the chosen parameters
 	    if(params != null && searchString != null) {
 	    	solrQuery = buildQuery(searchString, params);
 	    	if (solrQuery == null)
 	    		return null;
 	    } else {
 	    	return null;
 	    }
 	    
 	    QueryResponse rsp;
 	    SolrDocumentList results;
 	    LinkedList<SearchResult> searchResults = new LinkedList<SearchResult>();
 	    try {
 	    	rsp = solr.query(solrQuery);
 	    	results = rsp.getResults();
 	    
 	    	// Loop over all hits and create a SearchResult instance for each hit.
 	    	for (SolrDocument res : results) {
 	    		SearchResult sr = new SearchResult();
 	    		sr.setName((String)res.getFieldValue("name"));
 	    		sr.setUrl((String)res.getFieldValue("url"));
 	    		sr.setFiletype((String)res.getFieldValue("filetype"));
 	    		sr.setFilesize((Integer)res.getFieldValue("filesize")); // i bytes
 	    		sr.setTimestamp((String)res.getFieldValue("timestamp"));
 	    		sr.setUsername((String)res.getFieldValue("username"));
 	    		searchResults.add(sr);
 	    	}
 	    } catch (SolrServerException sse) {
 			System.out.println("SearchModel() - Caught an exception while exeucting the solr query!" +
 								"\ninputQuery was " + searchString + 
 								"\nparams was " + params);
 			//sse.printStackTrace();	    	
 	    }
 	    
 	    return searchResults;
 	}
 	
 	/**  
 	 * TODO - NEW METHOD - Not defined in ADD  
 	 *
 	 * Parses the params string and sets parameters for the Solr-query based
 	 * on that string.
 	 * @param searchString - the query for the solr search
 	 * @param params parameters to filter the search, on this format:
 	 * 		  "filetype:exe;minfilesize:20;maxfilesize:10"
 	 * 
 	 * @return returns a SolrQuery with the set parameters
 	 */
 	private SolrQuery buildQuery(String searchString, String params){
 		
 		// Debug output.
 		System.out.println("Entering buildQuery");
 		System.out.println("searchString:"+searchString);
 		System.out.println("params:"+params);
 		
 		// Fix the paramters such as doctype:avi,exe
 		String[] ps = params.split(";");
 		
 		System.out.println("Cleaning searchString");
 		if (!searchString.equals("*:*")){
 			System.out.println("Removing illegal characters from searchString.");
 			searchString = searchString.replaceAll("[^A-Za-z0-9 ]","");
 		}
 		System.out.println("Result:" + searchString + " length:" + searchString.length());
 		
 		// Create query with main search string
 		SolrQuery solrQuery = null;
 		if (searchString.length() > 0 && searchString.replace(" ", "").length() != 0) {
 			solrQuery = new SolrQuery(searchString);
 			solrQuery.setSortField("id", SolrQuery.ORDER.asc); 
 		} else {
 			return null;
 		}
 	
 		System.out.println("Traversing params.");
 		for (String param: ps) {
 			System.out.println("Current param:" + param);
 			
 			if(!param.contains(":"))
 				continue;
 			String[] values = param.split(":");
 			
 			if(values.length < 2)
 				continue;
 			
 			// Eg. paramName = filetype
 			String paramName = values[0];
 			// Eg. paramValues = { "avi", "exe" }
 			if (values[1].contains(",")) {
 				String[] paramValues = values[1].split(",");
 				
 				for (String v : paramValues) {
 					System.out.println("Adding filter query: " + paramName + ":" + v);
 					solrQuery.addFilterQuery(paramName + ":" + v);
 				}
 			} else {
 				String paramValue = values[1];
 				
				if(paramValue.compareTo("minfilesize") == 0) {
 					System.out.println("Adding filter query: " + "filesize: ["+paramValue+" TO *]");
 					solrQuery.addFilterQuery("filesize: ["+paramValue+" TO *]");
				} else if (paramValue.compareTo("maxfilesize") == 0) {
					System.out.println("Adding filter query: " + "filesize: ["+paramValue+" TO *]");
 					solrQuery.addFilterQuery("filesize: [0 TO "+paramValue+"]");
 				} else { 
 					System.out.println("Adding filter query: " + paramName + ":" + paramValue);
 					solrQuery.addFilterQuery(paramName + ":" + paramValue);
 				}
 			}
 		}
 	
 		return solrQuery;
 	}
 
 }
