 /**
  * 
  */
 package net.mysocio.connection.rss;
 
 import java.util.List;
 
 import net.mysocio.connection.readers.Source;
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.data.management.MessagesManager;
 import net.mysocio.data.messages.rss.RssMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.annotations.Entity;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 
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
 
 	public void createProcessor(String userId) throws Exception {
 		String url = getUrl();
 		if (logger.isDebugEnabled()){
 			logger.debug("Creating route for RSS feed on url " + url);
 		}
 		RssMessageProcessor processor = new RssMessageProcessor();
 		processor.setUrl(url);
 		processor.setUserId(userId);
 		DataManagerFactory.getDataManager().saveProcessor(processor, "url", getUrl());
 	}
 
 	@Override
 	public void removeProcessor(String userId) throws Exception {
 		DataManagerFactory.getDataManager().deleteUserProcessorByField(RssMessageProcessor.class, "url", getUrl(), userId);		
 	}
 	
 	public void processMessages(SyndFeed feed, List<SyndEntryImpl> entries) throws Exception {
 		for (SyndEntryImpl entry : entries) {
     		RssMessage message = new RssMessage();
     		message.setFeedTitle(feed.getTitle());
     		String language = feed.getLanguage();
     		if (language != null){
     			message.setLanguage(language);
     		}
     		message.setSourceId(getUrl());
     		processMessage(entry, message);
 		}
 	}
 	
 	protected void processMessage(SyndEntryImpl entry, RssMessage message) throws Exception {
 		//We are adding key to the end of the link to differ it from FB links  
 		message.setLink(entry.getLink() + "#mysocioRSS");
 		if (entry.getPublishedDate() != null){
 			message.setDate(entry.getPublishedDate().getTime());
 		}else{
 			if (entry.getUpdatedDate() != null){
 				message.setDate(entry.getUpdatedDate().getTime());
 			}else{
 				message.setDate(System.currentTimeMillis());
 			}
 		}
 		String title = entry.getTitle();
 		if (title == null){
 			title = "";
 		}
 		message.setTitle(title);
 		SyndContent description = entry.getDescription();
 		if (description == null){
 			//if message has no text - we ignore it
 			return;
 		}
 		String text = description.getValue();
		@SuppressWarnings("unchecked")
		List<SyndContent> contents = entry.getContents();
		for (SyndContent syndContent : contents) {
			text = text + "<br>" + syndContent.getValue();
		}
 		message.setText(text);
 		
 		if (logger.isDebugEnabled()){
 			logger.debug("Message title: " + title);
 			logger.debug("Message text: " + text);
 		}
 		try {
 			MessagesManager.getInstance().storeMessage(message);
 		} catch (Exception e) {
 			//if it's duplicate message - we ignore it
 		}
 	}
 }
