 /**
  * 
  */
 package net.mysocio.connection.rss;
 
 import java.util.List;
 
 import net.mysocio.data.IDataManager;
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.data.management.camel.UserMessageProcessor;
 import net.mysocio.data.messages.rss.RssMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.annotations.Transient;
 
 /**
  * @author Aladdin
  *
  */
 public class RssMessageProcessor extends UserMessageProcessor {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6216755406044921125L;
 	@Transient
 	static final Logger logger = LoggerFactory.getLogger(RssMessageProcessor.class);
 	private String url;
 	
 	public void process() throws Exception {
 		long to = System.currentTimeMillis();
 		long from = getLastUpdate();
 		if (from == 0 || (to - from) > MONTH){
 			from = to - MONTH;
 		}
     	IDataManager dataManager = DataManagerFactory.getDataManager();
     	List<RssMessage> messages = dataManager.getMessagesAfterDate(RssMessage.class, from, "sourceId", url);
     	for (RssMessage message : messages) {
     		addMessageForTag(message, RssMessage.class, url);
 		}
     	setLastUpdate(to);
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((url == null) ? 0 : url.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		RssMessageProcessor other = (RssMessageProcessor) obj;
 		if (url == null) {
 			if (other.url != null)
 				return false;
 		} else if (!url.equals(other.url))
 			return false;
 		return true;
 	}
 }
