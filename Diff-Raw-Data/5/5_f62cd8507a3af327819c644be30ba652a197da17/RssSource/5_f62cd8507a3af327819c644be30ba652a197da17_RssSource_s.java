 /**
  * 
  */
 package net.mysocio.connection.rss;
 
 import net.mysocio.connection.readers.Source;
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.data.messages.rss.RssMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.jmkgreen.morphia.annotations.Entity;
 
 /**
  * @author Aladdin
  *
  */
 @Entity("sources")
 public class RssSource extends Source {
 	static final Logger logger = LoggerFactory.getLogger(RssSource.class);
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3623303809928356829L;
 	
 	public Class<?> getMessageClass() {
 		return RssMessage.class;
 	}
 
 	public void createRoute(String to) throws Exception {
 		String url = getUrl();
 		if (logger.isDebugEnabled()){
 			logger.debug("Creating route for RSS feed on url " + url);
 		}
 		RssMessageProcessor processor = new RssMessageProcessor();
 		processor.setTo(to);
 		processor.setTag(url);
		DataManagerFactory.getDataManager().createRoute("rss:" + url + "?consumer.delay=2000", processor, 0l);
 	}
 
 	@Override
 	public void removeRoute(String userId) throws Exception {
		DataManagerFactory.getDataManager().removeRoute("rss:" + getUrl() + "?consumer.delay=2000", userId);		
 	}
 }
