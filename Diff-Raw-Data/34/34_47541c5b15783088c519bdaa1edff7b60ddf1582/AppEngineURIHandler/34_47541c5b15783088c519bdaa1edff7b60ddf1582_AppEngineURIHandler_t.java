 package org.qrone.r7.appengine;
 
 import javax.servlet.ServletContext;
 
import org.qrone.png.PNGMemoryImageService;
 import org.qrone.r7.ExtensionIndex;
 import org.qrone.r7.fetcher.URLFetcher;
 import org.qrone.r7.github.GitHubResolver;
 import org.qrone.r7.handler.ExtendableURIHandler;
 import org.qrone.r7.handler.HTML5Handler;
 import org.qrone.r7.handler.PathFinderHandler;
 import org.qrone.r7.handler.ResolverHandler;
 import org.qrone.r7.resolver.MemoryResolver;
 import org.qrone.r7.resolver.URIResolver;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
  
 public class AppEngineURIHandler extends ExtendableURIHandler{
 	private static final String KIND = "qrone.repository";
 	private static final String NAME = "name";
 	private static final String REPO = "repo";
 	private static final String TAG = "sha";
 	
 	private DatastoreService service = DatastoreServiceFactory.getDatastoreService();
 	private URLFetcher fetcher = new AppEngineURLFetcher();
 	private URIResolver cache  = new AppEngineResolver();
 	private GitHubResolver github = new GitHubResolver(fetcher, cache, 
 			"qronon","qronesite","master");
 	
 	public AppEngineURIHandler(ServletContext cx) {
 		//resolver.add(new FilteredResolver("/qrone-server/", new InternalResourceResolver()));
 		//resolver.add(new ServletResolver(cx));
 		
 		setupResolver();
 
 		HTML5Handler html5handler = new HTML5Handler(
				resolver, new PNGMemoryImageService(),new AppEngineLoginService());
 		ExtensionIndex ei = new ExtensionIndex();
		//if(ei.unpack(resolver) == null){
 			ei.find(cx);
		//	ei.pack(resolver);
		//}
 		ei.extend(html5handler);
 		ei.extend(this);
 
 		handler.add(github);
 		handler.add(new PathFinderHandler(html5handler));
 		handler.add(new ResolverHandler(resolver));
 	}
 	
 	private void setupResolver(){
 		Query query = new Query(KIND);
 		PreparedQuery pq = service.prepare(query);
 		resolver.asList().clear();
 		
 		resolver.add(github);
 		for( Entity e : pq.asIterable()){
 			github = new GitHubResolver(fetcher, cache, 
 					(String)e.getProperty(NAME), (String)e.getProperty(REPO), (String)e.getProperty(TAG));
 			resolver.add(github);
 		}
 		
 	}
 	
 }
