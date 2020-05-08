 package model.http.crawler;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import model.http.HttpGet;
 import model.http.crawler.extractorjoint.Extractor;
 import model.http.crawler.extractorjoint.ExtractorJoint;
 import model.http.crawler.persistentbuffer.PersistentBuffer;
 
 public class Crawler implements ICrawler {
 	private static final Logger log = (Logger) LoggerFactory
 			.getLogger(Crawler.class);
 	private String crawlNamespace;
 	private String dataNamespace;
 	private Queue<URL> dataBuffer = new LinkedBlockingQueue<URL>();
 	private HttpGet httpCon;
 	private PersistentBuffer crawlBuffer;
 	private ExtractorJoint factory;
 
 	public Crawler(PersistentBuffer model, ExtractorJoint factory2,
 			String crawlNamespace, String dataNamespace) {
 		init(factory2, model, httpCon, crawlNamespace, dataNamespace);
 	}
 
 	private void init(ExtractorJoint factory2, PersistentBuffer model,
 			HttpGet httpCon, String crawlNamespace, String dataNamespace) {
 		this.factory = factory2;
 		this.httpCon = httpCon;
 		this.crawlNamespace = crawlNamespace;
 		this.dataNamespace = dataNamespace;
 		this.crawlBuffer = model;
 	}
 
 	public String getDataNamespace() {
 		return dataNamespace;
 	}
 
 	public String getCrawlNamespace() {
 		return crawlNamespace;
 	}
 
 	/* (non-Javadoc)
 	 * @see model.http.crawler.ICrawler#getNext(int)
 	 */
 	public Set<URL> getNext(int count) {
 		while (this.dataBuffer.size() < count && !this.crawlBuffer.isEmpty())
 			try {
 				crawlNextURL();
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		Set<URL> urls = new HashSet<URL>();
		while (urls.size() < count && !this.crawlBuffer.isEmpty())
 			urls.add(this.dataBuffer.poll());
 		return urls;
 	}
 
 	private void crawlNextURL() throws MalformedURLException {
 		URL next = this.crawlBuffer.poll();
 		if(next != null) {
 			log.debug("Crawling: " + next.toExternalForm());
 			Extractor<URL> extract = factory.create(next);
 			Set<URL> links = extract.get();
 			for (URL link : links) {
 				if (link.toExternalForm().matches(this.dataNamespace)) {
 					log.debug("Found new data: " + link.toExternalForm());
 					this.dataBuffer.add(link);
 				} else if (link.toExternalForm().matches(this.crawlNamespace)) {
 					log.debug("Found new url to crawl: " + link.toExternalForm());
 					this.crawlBuffer.add(link);
 				}
 			}
 		}
 	}
 }
