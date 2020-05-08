 package model.http.crawler.persistentbuffer;
 
 import java.net.URL;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import model.persistence.dao.CrawlerModel;
 
 public class CrawlBuffer implements PersistentBuffer {
 	private final Queue<URL> crawlBuffer = new LinkedBlockingQueue<URL>();
 	private final CrawlerModel model;
 	public CrawlBuffer(CrawlerModel model) {
 		super();
 		this.model = model;
 	}
 	/* (non-Javadoc)
 	 * @see model.http.crawler.PersistentBuffer#add(java.net.URL)
 	 */
 	public void add(URL url) {
 		if(!model.contains(url)) {
 			crawlBuffer.add(url);
 			if(model != null) model.add(url, false);
 		}
 	}
 	/* (non-Javadoc)
 	 * @see model.http.crawler.PersistentBuffer#poll()
 	 */
 	public URL poll() {
 		if(crawlBuffer.isEmpty()) {
 			if(model != null) {
 				URL url = model.getNotCrawled();
 				model.update(url, true);
 				return url;
 			}
 			return null;
 		} else {
 			URL url = crawlBuffer.poll();
 			if(model != null) {
 				model.update(url, true);
 			}
 			return url;
 		}
 	}
 	public boolean isEmpty() {
		return crawlBuffer.isEmpty() || (model != null ? model.isEmpty() : true);
 	}
 }
