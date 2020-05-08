 package de.fuberlin.wiwiss.ng4j.semwebclient;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.Quad;
 
 import de.fuberlin.wiwiss.ng4j.semwebclient.urisearch.URISearchListener;
 import de.fuberlin.wiwiss.ng4j.semwebclient.urisearch.URISearchResult;
 
 /**
  * The FindQuery runs a find query against the Semantic Web. If there are
  * URIs that match the triple the FindQuery requests dereferencing and
  * optionally URI search.
  * 
  * @author Tobias Gauß
  * @author Olaf Hartig
  */
 public class FindQuery implements DereferencingListener, URISearchListener {
 	private SemWebIterator iterator;
 	private SemanticWebClient client;
 	private boolean enableURISearch;
 	private List<String> urisInDerefProcessing = new LinkedList<String>();
 	private List<String> urisInSearchProcessing = new LinkedList<String>();
 	private TimeoutThread timeoutThread;
 	private boolean stopped;
 	private Log log = LogFactory.getLog(FindQuery.class);
 	
 	public FindQuery(SemanticWebClient client, Triple pattern) {
 		this( client, pattern, false );
 	}
 
 	public FindQuery(SemanticWebClient client, Triple pattern, boolean enableURISearch) {
 		this.client = client;
 		this.enableURISearch = enableURISearch;
 		this.iterator = new SemWebIterator(this, pattern);
 		this.timeoutThread = new TimeoutThread(this.iterator);
 		this.timeoutThread.setName("Timeout");
 		synchronized (this.client) {
 			this.iterator.queueNamedGraphs(this.client.listGraphs());
 		}
 		// TODO The inspect operations can be expensive and shouldn't be executed by the application thread
 		this.inspectTriple(this.client, pattern, 0);
 		this.inspectNgs(this.client, pattern, 0);
 		checkIfProcessingFinished();
 	}
 
 	public void dereferenced(DereferencingResult result) {
 		if (result.isSuccess()) {
 			inspectNgs(result.getResultData(), this.iterator.getTriple(), result.getTask().getStep());
 			this.iterator.queueNamedGraphs(result.getResultData().listGraphs());
 		}else if(result.getResultCode()== DereferencingResult.STATUS_NEW_URIS_FOUND){
 			Iterator<String> it = result.getUriList().iterator();
 			while (it.hasNext()) {
 				requestDereferencing( it.next(),
 				                      result.getTask().getStep() + 1,
 				                      false ); // no URI search
 			}
 			
 		}else if(result.getResultCode()== DereferencingResult.STATUS_REDIRECTED){
 			requestDereferencing( result.getRedirectURI(),
 			                      result.getTask().getStep() + 1,
 			                      false ); // no URI search
 			
 		}
 		uriDerefProcessingFinished(result.getURI());
 	}
 
 	public void uriSearchFinished ( URISearchResult result ) {
 		if ( result.isSuccess() ) {
 			Iterator<String> it = result.getMentioningDocs().iterator();
 			while ( it.hasNext() ) {
 				requestDereferencing( it.next(),
 				                      result.getTask().getStep() + 1,
 				                      false ); // no URI search
 			}
 		}
 		uriSearchProcessingFinished( result.getTask().getURI() );
 	}
 
 	private synchronized void uriDerefProcessingFinished(String uri) {
 		urisInDerefProcessing.remove(uri);
 		checkIfProcessingFinished();
 	}
 
 	private synchronized void uriSearchProcessingFinished(String uri) {
 		urisInSearchProcessing.remove(uri);
 		checkIfProcessingFinished();
 	}
 
 	private void checkIfProcessingFinished() {
 		if (!urisInDerefProcessing.isEmpty()) {
 			return;
 		}
 		if (!urisInSearchProcessing.isEmpty()) {
 			return;
 		}
 		this.iterator.noMoreGraphs();
 		close();
 	}
 	
 	/**
 	 * Inspects a Triple if it contains URIs. If a URI is found it is added to
 	 * the UriList.
 	 * 
 	 * @param t
 	 *            The triple to inspect.
 	 * @param step
 	 *            The retrieval step.
 	 */
 	private void inspectTriple(NamedGraphSet ngs, Triple t, int step) {
 		Node sub = t.getSubject();
 		Node pred = t.getPredicate();
 		Node obj = t.getObject();
 		
 		this.inspectNode(ngs, sub, step);
 		this.inspectNode(ngs, pred, step);
 		this.inspectNode(ngs, obj, step);
 	}
 	
 	private void inspectNode(NamedGraphSet ngs, Node n, int step){
 		if (n.isURI()) {
 			requestDereferencing(n.getURI(), step + 1, enableURISearch);
 		}
 		if (n.isURI() || n.isBlank()) {
 			checkSeeAlso(ngs, n, step);
 		}
 	}
 
 	/**
 	 * Checks the given NamedGraphSet ngs for uris.
 	 * 
 	 * @param ngs
 	 *            The NamedgraphSet to inspect
 	 * @param step
 	 *            The retrieval step
 	 */
 	private void inspectNgs(NamedGraphSet ngs, Triple pattern, int step) {
 		synchronized (ngs) {
 			Iterator iter = ngs.findQuads(Node.ANY, pattern.getSubject(),
 					pattern.getPredicate(), pattern.getObject());
 
 			while (iter.hasNext()) {
 				Quad q = (Quad) iter.next();
 				Triple t = q.getTriple();
 				inspectTriple(ngs, t, step);
 			}
 		}
 	}
 
 	/**
 	 * Checks a given NamedGraphSet ngs for rdfs:seeAlso tags and adds the found
 	 * uris to the "to retrieve" list.
 	 * 
 	 * @param ngs
 	 *            The NamedGraphSet to inspect.
 	 * @param uri
 	 *            The URI.
 	 * @param step
 	 *            The retrieval step.
 	 */
 	private void checkSeeAlso(NamedGraphSet ngs, Node n, int step) {
 		synchronized (ngs) {
 			Iterator iter = ngs.findQuads(Node.ANY, n, RDFS.seeAlso.asNode(),
 					Node.ANY);
 			while (iter.hasNext()) {
 				Quad quad = (Quad) iter.next();
 				Node obj = quad.getObject();
 				if (obj.isURI()) {
 					requestDereferencing(obj.getURI(), step + 1, enableURISearch);
 				}
 			}
 		}
 	}
 
 	private void requestDereferencing(String uri, int step, boolean enableURISearch) {
 		if (this.stopped) {
 			return;
 		}
 		if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
 			// Don't try to reference mailto:, file: and other URI schemes
 			return;
 		}
 		if ( enableURISearch ) {
 			if ( client.requestDereferencingWithSearch(uri, step, this, this) ) {
 				String derefURI = ( uri.contains("#") ) ? uri.substring( 0, uri.indexOf("#") ) : uri;
 				urisInDerefProcessing.add(derefURI);
 				urisInSearchProcessing.add(uri);
 			}
 		}
 		else {
 			if ( client.requestDereferencing(uri, step, this) ) {
 				String derefURI = ( uri.contains("#") ) ? uri.substring( 0, uri.indexOf("#") ) : uri;
 				urisInDerefProcessing.add(derefURI);
 			}
 		}
 	}
 	
 	public synchronized void close() {
 		this.stopped = true;
 		this.timeoutThread.cancel();
 	}
 
 	public SemWebIterator iterator(){
 		return this.iterator;
 	}
 	
 	private long getTimeout() {
 		try {
 			return Long.parseLong(
 					this.client.getConfig(SemanticWebClient.CONFIG_TIMEOUT));
 		} catch (NumberFormatException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 	
 	private class TimeoutThread extends Thread {
 		private SemWebIterator iterator;
 		private boolean closeIterator = true;
 		TimeoutThread(SemWebIterator iterator) {
 			this.iterator = iterator;
 			start();
 		}
 		public synchronized void run() {
 			try {
				if (this.closeIterator) {
					wait(getTimeout());
				}
 			} catch (InterruptedException ex) {
 				// We don't know when this happens
 				throw new RuntimeException(ex);
 			}
 			if (this.closeIterator) {
 				log.debug("Timeout");
 				stopped = true;
 				this.iterator.close();
 			}
 		}
 		synchronized void cancel() {
 			this.closeIterator = false;
 			notify();
 		}
 	}
 }
