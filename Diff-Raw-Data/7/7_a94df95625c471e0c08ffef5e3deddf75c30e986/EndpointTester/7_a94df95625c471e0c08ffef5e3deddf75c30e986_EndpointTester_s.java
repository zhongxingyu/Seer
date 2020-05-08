 package de.uni_leipzig.simba.saim.core;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSet;
 
 /** Class to test if a certain endpoint is available */
 public class EndpointTester {
 	
 	private String url;
 	
 	public enum EndpointStatus {
 			OK, EMPTY, TIMED_OUT, SERVER_NOT_FOUND, HTTP_ERROR, EXECUTION_ERROR, OTHER_ERROR
 	} 
 	
 	public EndpointTester(String url){
 		this.url = url;
 	}
 	
 	/**
 	 * Tests whether the URL is available.s
 	 * @param url
 	 * @return
 	 */
 	private static boolean pingEndpoint(String url) {
 		boolean available = false;
 		try{
 		    final URLConnection connection = new URL(url).openConnection();
 		    connection.connect();
 		    available = true;
 		} catch(final MalformedURLException e){
 		    throw new IllegalStateException("Bad URL: " + url, e);
 		} catch(final IOException e){
 		}
 		return available;
 	}
 	
 	/**
 	 * Send SPARQL query to end point.
 	 * @return true if given URL is a SPARQL end point with at least a triple.
 	 */
 	private boolean querySPARQLEndpoint() {
 		boolean answer = false;
 		String query = "SELECT ?s WHERE { ?s ?p ?o . } LIMIT 1";		
 		Query sparqlQuery = QueryFactory.create(query);
         QueryExecution qexec = QueryExecutionFactory.sparqlService(url, sparqlQuery);
         ResultSet res = qexec.execSelect();
         if(res.hasNext())
         	answer = true;       
         return answer;				
 	}
 	
 	/**
 	 * Tests whether the given url is a valid SPARQL end point. 
 	 * @param url URL of the server to test.
 	 * @return EndpointStatus indicating the status of the server.
 	 */
 	public static EndpointStatus testSPARQLEndpointTimeOut(String url)
 	{
 		if(!pingEndpoint(url)) {
 			return EndpointStatus.SERVER_NOT_FOUND;
 		}	
 		final EndpointTester tester = new EndpointTester(url);
 		boolean result = false;
         ExecutorService executor = Executors.newCachedThreadPool();
         Callable<Boolean> task = new Callable<Boolean>() {
            public Boolean call() {
               return tester.querySPARQLEndpoint();
            }
         };
         Future<Boolean> future = executor.submit(task);
         try {
            result = future.get(10000, TimeUnit.MILLISECONDS);
            if(!result) {
         	   return EndpointStatus.EMPTY;
            }
            return EndpointStatus.OK;
         } catch (TimeoutException ex) {
         	return EndpointStatus.TIMED_OUT;
         } catch (InterruptedException e) {
         	return EndpointStatus.OTHER_ERROR;
         } catch (ExecutionException e) {
        	return EndpointStatus.EXECUTION_ERROR;
         } catch (Exception e) {
         	return EndpointStatus.OTHER_ERROR;
         }
 	}
 }
