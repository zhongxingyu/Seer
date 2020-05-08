 /**
  * 
  */
 package net.mysocio.connection.lj;
 
 import java.util.List;
 
 import net.mysocio.connection.rss.RssSource;
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.data.messages.lj.LjMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.annotations.Entity;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndImage;
 
 /**
  * @author Aladdin
  *
  */
 @Entity("sources")
 public class LjSource extends RssSource {
 	static final Logger logger = LoggerFactory.getLogger(LjSource.class);
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -559758680101816312L;
 	
 	public Class<?> getMessageClass() {
 		return LjMessage.class;
 	}
 	
 	public void createProcessor(String userId) throws Exception {
 		String url = getUrl();
 		if (logger.isDebugEnabled()){
 			logger.debug("Creating route for LJ feed on url" + url);
 		}
 		LjMessageProcessor processor = new LjMessageProcessor();
 		processor.setUrl(url);
 		processor.setUserId(userId);
 		DataManagerFactory.getDataManager().saveProcessor(processor, "url", getUrl());
 	}
 	
 	@Override
 	public void removeProcessor(String userId) throws Exception {
 		DataManagerFactory.getDataManager().deleteUserProcessorByField(LjMessageProcessor.class, "url", getUrl(), userId);		
 	}
 	
 	@Override
 	public void processMessages(SyndFeed feed, List<SyndEntryImpl> entries) throws Exception {
 		SyndImage image = feed.getImage();
 		for (SyndEntryImpl entry : entries) {
 			LjMessage message = new LjMessage();
     		message.setUserpic(image.getUrl());
    		message.setSourceId(getUrl());
     		processMessage(entry, message);
 		}
 	}
 }
