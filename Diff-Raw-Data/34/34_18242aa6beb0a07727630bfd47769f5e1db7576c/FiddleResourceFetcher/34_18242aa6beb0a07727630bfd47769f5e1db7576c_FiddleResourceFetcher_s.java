 package org.zkoss.fiddler.executor.resources.fetch;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.mortbay.resource.FileResource;
 import org.mortbay.resource.Resource;
 import org.mortbay.util.ajax.JSON;
 import org.zkoss.fiddler.executor.server.Configs;
 import org.zkoss.fiddler.executor.utils.URLUtil;
 
 public class FiddleResourceFetcher {
 
	Map<FetchedToken, List<FetchResource>> cacheResult = new HashMap<FetchedToken, List<FetchResource>>();
 
 	private String host;
 
 	private File base;
 
 	/**
 	 * we need a classloader to provide classpath
 	 */
 	public FiddleResourceFetcher(String phost, File base) {
 		host = phost;
 		this.base = base;
 	}
 
 	public boolean isFetched(FetchedToken ft) {
 		return cacheResult.containsKey(ft);
 	}
 
 	private URL fileToURL(File f) {
 		try {
 			return f.toURI().toURL();
 		} catch (MalformedURLException e) {
 			if (Configs.isLogMode())
 				e.printStackTrace();
 			return null;
 		}
 	}
 
 	public Resource getTokenHolder(FetchedToken ft) throws IOException, URISyntaxException {
 		return new FileResource(fileToURL(new File(base.getAbsolutePath() + "/" + ft.getToken() + "/" + ft.getVersion()
 				+ "/")));
 	}
 
 	private List<FetchResource> parseResourceList(String content, String storeParent) {
		Map map = (Map) JSON.parse(content);
 		Object[] resources = (Object[]) map.get("resources");
 		List<FetchResource> list = new ArrayList<FetchResource>();
 		for (Object obj : resources) {
 			Map resource = (Map) obj;
 			if (Configs.isLogMode())
 				System.out.println("loading:" + resource.get("name"));
 			FetchResource fr = new FetchResource();
 			fr.setType(((Long) resource.get("type")).intValue());
 			fr.setFileName((String) resource.get("name"));
 			fr.setContent((String) resource.get("content"));
 			fr.setStoreBasePath(base.getAbsolutePath() + storeParent + File.separator);
 			list.add(fr);
 		}
 		return list;
 	}
 
 	public List<FetchResource> fetch(FetchedToken ft) throws MalformedURLException, ConnectException {
 
 		URL u = new URL(host + "/data/" + ft.getToken() + "/" + ft.getVersion());
 		String content = URLUtil.fetchContent(u);
 
 		if ("".equals(content)) {
 			cacheResult.put(ft, null);
 			return null;
 		}
 
 		String parent = File.separator + ft.getToken() + File.separator + ft.getVersion();
 		List<FetchResource> resources = parseResourceList(content, parent);
 		cacheResult.put(ft, resources);
 		return resources;
 	}
 
 }
