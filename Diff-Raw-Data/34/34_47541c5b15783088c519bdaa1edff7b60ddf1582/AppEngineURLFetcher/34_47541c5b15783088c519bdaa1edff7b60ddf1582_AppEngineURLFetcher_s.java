 package org.qrone.r7.appengine;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.qrone.r7.fetcher.URLFetcher;
 
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 public class AppEngineURLFetcher implements URLFetcher{
 	private URLFetchService service = URLFetchServiceFactory.getURLFetchService();
 	
 	@Override
 	public InputStream fetch(String url) throws IOException{
 		byte[] bytes = service.fetch(new URL(url)).getContent();
		System.err.println(new String(bytes));
 		return new ByteArrayInputStream(bytes);
 	}
 
 	@Override
 	public InputStream fetch(String url, byte[] body) throws IOException{
 		HTTPRequest r = new HTTPRequest(new URL(url));
 		r.setPayload(body);
 		byte[] bytes = service.fetch(r).getContent();
		System.err.println(new String(bytes));
 		return new ByteArrayInputStream(bytes);
 	}
 }
