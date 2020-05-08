 package org.qrone.r7.appengine;
 
 import javax.servlet.ServletContext;
 
 import org.qrone.png.PNGMemoryImageService;
 import org.qrone.r7.ExtensionIndex;
 import org.qrone.r7.PortingServiceBase;
 import org.qrone.r7.fetcher.URLFetcher;
 import org.qrone.r7.github.GitHubResolver;
 import org.qrone.r7.handler.ExtendableURIHandler;
 import org.qrone.r7.handler.HTML5Handler;
 import org.qrone.r7.handler.PathFinderHandler;
 import org.qrone.r7.handler.ResolverHandler;
 import org.qrone.r7.resolver.URIResolver;
  
 public class AppEngineURIHandler extends ExtendableURIHandler{
 	
 	private URLFetcher fetcher = new AppEngineURLFetcher();
 	private URIResolver cache  = new AppEngineResolver();
 	private GitHubResolver github = new GitHubResolver(fetcher, cache, 
			"qronon","qronesite","master");
 	private AppEngineRepositoryService repository = new AppEngineRepositoryService(fetcher, cache);
 	
 	public AppEngineURIHandler(ServletContext cx) {
 		//resolver.add(new FilteredResolver("/qrone-server/", new InternalResourceResolver()));
 		//resolver.add(new ServletResolver(cx));
 		
 		resolver.add(github);
 		resolver.add(repository.getResolver());
 
 		HTML5Handler html5handler = new HTML5Handler(
 				new PortingServiceBase(
 						fetcher, 
 						resolver, 
 						new AppEngineKVSService(), 
 						new AppEngineMemcachedService(), 
 						new AppEngineLoginService(), 
 						new PNGMemoryImageService(),
 						repository
 				));
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
 		
 	}
 	
 }
