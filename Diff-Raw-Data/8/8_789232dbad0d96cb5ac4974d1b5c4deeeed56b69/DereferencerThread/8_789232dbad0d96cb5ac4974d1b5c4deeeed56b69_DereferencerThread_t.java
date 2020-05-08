 package de.fuberlin.wiwiss.ng4j.semwebclient;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphImpl;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 import de.fuberlin.wiwiss.ng4j.semwebclient.threadutils.Task;
 import de.fuberlin.wiwiss.ng4j.semwebclient.threadutils.TaskExecutorBase;
 
 /**
  * The DereferencerThread executes a given DereferencingTask. It opens a
  * HttpURLConnection, creates an InputStream and tries to parse it. If the
  * Thread is finished it delivers the retrieval result.
  * 
  * @author Tobias Gauß
  * @author Olaf Hartig
  */
 public class DereferencerThread extends TaskExecutorBase {
 	private HttpURLConnection connection;
 
 	private NamedGraphSet tempNgs = null;
 	
 	private int maxfilesize = -1;
 
         private boolean enablegrddl = false;
 
 	private int connectTimeout = 0;
 	private int readTimeout = 0;
 
 	private URL url;
 
 	private Log log = LogFactory.getLog(DereferencerThread.class);
 
 	public DereferencerThread() {
 		// Lower priority a little bit
 		setPriority(getPriority() - 1);
 	}
 
 
 	// implementation of the TaskExecutorBase interface
 
 	public Class getTaskType () {
 		return DereferencingTask.class;
 	}
 
 
 	protected void executeTask ( Task task ) {
 		DereferencingResult result = executeTask( (DereferencingTask) task );
 
 		// deliver the result of the task to the listeners
 		synchronized ( this ) {
 			if ( isStopped() )
 				return;
 
 			( (DereferencingTask) task ).notifyListeners( result );
 		}
 	}
 
 
 	// methods kept for compatibility
 
 	/**
 	 * @return Returns true if the DereferencerThread is available for new
 	 *         tasks.
 	 */
 	public synchronized boolean isAvailable() {
 		return !hasTask() && !isStopped();
 	}
 
 	/**
 	 * Starts to execute the DereferencingTask task. Returns true if the
 	 * retrieval process is started false if the thread is unable to execute the
 	 * task.
 	 * @deprecated Please use {@link TaskExecutorBase#startTask} instead.
 	 * 
 	 * @param task
 	 *            The task to execute.
 	 * @return
 	 */
 	public synchronized boolean startDereferencingIfAvailable(
 			DereferencingTask task) {
 		if (!isAvailable()) {
 			return false;
 		}
 		startTask( task );
 		return true;
 	}
 
 
 	// helper methods
 
 	/**
 	 * Creates a new DereferencingResult which contains information about the
 	 * retrieval failure.
 	 * 
 	 * @param errorCode
 	 *            the error code
 	 * @param exception
 	 *            the thrown exception
 	 * @return
 	 */
 	private DereferencingResult createErrorResult(DereferencingTask task, int errorCode,
 			Exception exception) {
 		return new DereferencingResult(task, errorCode, null, exception);
 	}
 
 	/**
 	 * Creates a new DereferencingResult which contains information about the
 	 * retrieval failure.
 	 * 
 	 * @param errorCode
 	 *            the error code
 	 * @param exception
 	 *            the thrown exception
 	 * @return
 	 */
 	private DereferencingResult createNewUrisResult(DereferencingTask task, int errorCode, ArrayList<String> urilist) {
 		return new DereferencingResult(task, errorCode, urilist);
 	}	
 
 	private DereferencingResult executeTask(DereferencingTask task) {
 		DereferencingResult result = null;
 		this.tempNgs = new NamedGraphSetImpl();
 		try {
 			url = new URL(task.getURI());
 		} catch (MalformedURLException ex) {
 			return createErrorResult( task, DereferencingResult.STATUS_MALFORMED_URL, ex );
 		}
 
 		try {
 			URLConnection con = url.openConnection();
 
 // TODO This works only with Java 5,
 // and Tobias said he's not even sure if it has any positive effect. [RC]
 //			con.setReadTimeout(60000);
 
 // It does (at least for me?) [Olaf]
 			con.setConnectTimeout( connectTimeout );
 			con.setReadTimeout( readTimeout );
 
 			connection = (HttpURLConnection) con;
 		} catch ( IOException e ) {
 			log.debug( "Creating a connection to <" + url.toString() + "> caused a " + e.getClass().getName() + ": " + e.getMessage(), e );
 			return createErrorResult( task, DereferencingResult.STATUS_UNABLE_TO_CONNECT, e );
 		}
 
 		connection.setInstanceFollowRedirects(false);
 		connection.addRequestProperty(
 							"Accept",
 							"application/rdf+xml;q=1,"
 							+ "text/xml;q=0.6,text/rdf+n3;q=0.9,"
 							+ "application/octet-stream;q=0.5,"
 							+ "application/xml q=0.5,application/rss+xml;q=0.5,"
 							+ "text/plain; q=0.5,application/x-turtle;q=0.5,"
 							+ "application/x-trig;q=0.5,"
 							+ "application/xhtml+xml;q=0.5, "
 							+ "text/html;q=0.5"
 							);
 
 		try {
 			connection.connect();
 		} catch ( SocketTimeoutException e ) {
 			log.debug( "Connecting to <" + url.toString() + "> caused a " + e.getClass().getName() + ": " + e.getMessage() );
 			return createErrorResult( task, DereferencingResult.STATUS_TIMEOUT, e );
 		} catch ( IOException e ) {
 			log.debug( "Connecting to <" + url.toString() + "> caused a " + e.getClass().getName() + ": " + e.getMessage(), e );
 			return createErrorResult( task, DereferencingResult.STATUS_UNABLE_TO_CONNECT, e );
 		} catch ( RuntimeException e ) {
 			log.debug( "Connecting to <" + url.toString() + "> caused a " + e.getClass().getName() + ": " + e.getMessage() );
 			return createErrorResult( task, DereferencingResult.STATUS_UNABLE_TO_CONNECT, e );
 		}
 
 		try {
 			this.log.debug(this.connection.getResponseCode() + " " + this.url
 				       + " (" + this.connection.getContentType() + ")");
 
 			if (    (this.connection.getResponseCode() == 301)
 			     || (this.connection.getResponseCode() == 302)
 			     || (this.connection.getResponseCode() == 303) ) {
 				String redirectURI = this.connection.getHeaderField("Location");
 				return new DereferencingResult(task, DereferencingResult.STATUS_REDIRECTED, redirectURI);
 			}
 
 			if ( this.connection.getResponseCode() != 200 ) {
 				return createErrorResult( task,
 				                          DereferencingResult.STATUS_UNABLE_TO_CONNECT,
 				                          new Exception("Unexpected response code ("+connection.getResponseCode()+")") );
 			}
 
 			if ( connection.getContentType() == null ) {
 				return createErrorResult( task,
 				                          DereferencingResult.STATUS_UNABLE_TO_CONNECT,
 				                          new Exception("Unknown content type") );
 			}
 
 			String lang = setLang();
 			try {
 				result = this.parseRdf(task, lang);
 			} catch (Exception ex) { // parse error
 				this.log.debug(ex.getMessage());
 				return createErrorResult(
 						task, DereferencingResult.STATUS_PARSING_FAILED, ex);
 			}
 			// }
 		} catch ( SocketTimeoutException e ) {
 			log.debug( "Accessing the connection to <" + url.toString() + "> caused a " + e.getClass().getName() + ": " + e.getMessage() );
 			return createErrorResult( task, DereferencingResult.STATUS_TIMEOUT, e );
 		} catch (IOException e) {
 			log.debug( "Accessing the connection to <" + url.toString() + "> caused a " + e.getClass().getName() + ": " + e.getMessage(), e );
 			return createErrorResult(task, DereferencingResult.STATUS_UNABLE_TO_CONNECT,
 					e);
 		}
 		//return new DereferencingResult(this.task,
 		//		DereferencingResult.STATUS_OK, this.tempNgs, null);
 		return result;
 	}
 
 	/**
 	 * Parses an RDF String.
 	 */
 	private DereferencingResult parseRdf(DereferencingTask task, String lang) throws Exception {
		if (    (lang != null)
		     && (lang.equals("html") || lang.equals("HTML")) ) {
 		        if (this.enablegrddl) {
 			    com.hp.hpl.jena.grddl.GRDDLReader r = new com.hp.hpl.jena.grddl.GRDDLReader();
 			    /*
 			    Gleaner g = new Gleaner(this.connection.getURL().toString(),
 						    this.connection.getInputStream());
 			    g.glean(this.tempNgs);
 			    */
 			    Model m = ModelFactory.createDefaultModel();
 			    r.read(m, this.connection.getInputStream(), this.url.toString());
 			    this.tempNgs.addGraph( new NamedGraphImpl(this.url.toString(), 
 								      m.getGraph()) );
 
 			    if (this.tempNgs.countGraphs() > 0)
 				return new DereferencingResult(task,
 							       DereferencingResult.STATUS_OK, this.tempNgs, null);
 			}
 			ArrayList<String> l = HtmlLinkFetcher.fetchLinks(this.connection.getInputStream());
 			Iterator<String> iter = l.iterator();
 			ArrayList<String> urilist = new ArrayList<String>();
 			while (iter.hasNext()) {
 				String link = iter.next();
 				link = link.replace( "&amp;", "&" );
 				link = link.replace( "&gt;", ">" );
 				link = link.replace( "&lt;", "<" );
 				try {
 					URL newURL = new URL( url, link );
 					urilist.add( newURL.toString() );
 				} catch ( MalformedURLException e ) {
 					log.debug( "Creating a URL from the link <" + link + "> fetched for <" + url.toString() + "> caused an exception (" + e.getMessage() + ").", e );
 				}
 			}
 			return createNewUrisResult(task, DereferencingResult.STATUS_NEW_URIS_FOUND, urilist);
 			
 		}
 			
 		RDFDefaultErrorHandler.silent = true;
 		LimitedInputStream lis = new LimitedInputStream(this.connection.getInputStream(),this.maxfilesize);
 		this.tempNgs.read(lis, lang, this.url
 				.toString());
 		return new DereferencingResult(task,
 						DereferencingResult.STATUS_OK, this.tempNgs, null);
 	}
 
 	/**
 	 * Tries to guess a lang String from a connection.
 	 * 
 	 * @return
 	 */
 	private String setLang() {
 		String type = this.connection.getContentType();
 		if (type == null)
			return null;
 
 		if (type.startsWith("application/rdf+xml")
 				|| type.startsWith("text/xml")
 				|| type.startsWith("application/xml")
 				|| type.startsWith("application/rss+xml")
 				|| type.startsWith("text/plain"))
 			return "RDF/XML";
 		if (type.startsWith("application/n3")
 				|| type.startsWith("application/x-turtle")
 				|| type.startsWith("text/rdf+n3"))
 			return "N3";
 		if (type.startsWith("text/html"))
 			return "html";
 
 		return type;
 	}
 
 
 	// accessor methods
 
 	public synchronized void setMaxfilesize(int size){
 		this.maxfilesize = size;
 	}
 	public synchronized void setEnableGrddl(boolean g){
 		this.enablegrddl = g;
 	}
 	public synchronized void setConnectTimeout(int t){
 		connectTimeout = t;
 	}
 	public synchronized void setReadTimeout(int t){
 		readTimeout = t;
 	}
 
 	/*
 	 * public void parseHTML(){ String site = null; try{ InputStream stream =
 	 * this.connection.getInputStream(); BufferedReader br = new
 	 * BufferedReader(new InputStreamReader(stream)); StringBuffer sb = new
 	 * StringBuffer(); String line = null;
 	 * 
 	 * while ((line = br.readLine()) != null) { sb.append(line + "\n"); } site =
 	 * sb.toString(); Pattern p = Pattern.compile("<link\\s*rel"); Matcher
 	 * match = p.matcher(site); int start = match.start(1); int end =
 	 * match.end(1);
 	 * 
 	 * }catch(Exception e){ System.out.println(e.getLocalizedMessage()); }
 	 *  }
 	 */
 }
