 package cg3204l;
 import java.util.concurrent.Callable;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 public class FetcherCallable implements Callable<Document> {
 	
 	protected String mURL;
 	
 	public FetcherCallable(String url) {
 		this.mURL = url;
 	}
 
 	@Override
 	public Document call() throws Exception {
 		// fetch html content from URL
 		CGHTTPClient client = new CGHTTPClient(mURL);
 		client.get();
 		String html = client.getResponse();
 		Document doc = null;
 		if (html != null) {
 			doc = Jsoup.parse(html);
 		}
		String baseUri = client.getScheme() + "://" + client.getAuthority();
		doc.setBaseUri(baseUri);
 		return doc;
 	}
 }
