 /**
  * 
  */
 package net.mysocio.data.management.camel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mysocio.data.AbstractUserMessagesProcessor;
 import net.mysocio.data.IDataManager;
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.data.messages.GeneralMessage;
 import net.mysocio.data.messages.UnreaddenMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.annotations.Transient;
 
 /**
  * @author Aladdin
  *
  */
 public abstract class UserMessageProcessor extends AbstractUserMessagesProcessor{
	protected static final long MONTH = 30*24*3600l;
 	@Transient
 	private static final Logger logger = LoggerFactory.getLogger(UserMessageProcessor.class);
 	private Long lastUpdate = 0l;
 	private Long lastFailure = 0l;
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2112778445393534126L;
 
 	protected<T extends GeneralMessage> void addMessageForTag(T message, Class<T> T, String tag) throws Exception {
 		String userId = getUserId();
 		IDataManager dataManager = DataManagerFactory.getDataManager();
 		if (dataManager.isNewMessage(userId, message, T)){
 			dataManager.saveObject(createUnreaddenMessage(message, tag, userId));
 		}
 	}
 	
 	protected<T extends GeneralMessage> void addMessagesForTag(List<T> messages, Class<T> T, String tag) throws Exception {
 		List<UnreaddenMessage> unreaddenMessages = new ArrayList<UnreaddenMessage>();
 		String userId = getUserId();
 		IDataManager dataManager = DataManagerFactory.getDataManager();
 		for (T generalMessage : messages) {
 			if (dataManager.isNewMessage(userId, generalMessage, T)){
 				unreaddenMessages.add(createUnreaddenMessage(generalMessage, tag, userId));
 			}
 		}
 		if (!unreaddenMessages.isEmpty()){
 			dataManager.saveObjects(UnreaddenMessage.class, unreaddenMessages);
 		}
 	}
 
 	private UnreaddenMessage createUnreaddenMessage(GeneralMessage message,
 			String tag, String userId) {
 		UnreaddenMessage unreaddenMessage = new UnreaddenMessage();
 		unreaddenMessage.setMessageDate(message.getDate());
 		unreaddenMessage.setMessage(message);
 		unreaddenMessage.setTagId(tag);
 		unreaddenMessage.setUserId(userId);
 		if (logger.isDebugEnabled()){
 			logger.debug("Got message with uid " + message.getUniqueFieldValue().toString());
 		}
 		return unreaddenMessage;
 	}
 	
 	public Long getLastUpdate() {
 		return lastUpdate;
 	}
 
 	public void setLastUpdate(Long lastUpdate) {
 		this.lastUpdate = lastUpdate;
 	}
 
 	public Long getLastFailure() {
 		return lastFailure;
 	}
 
 	public void setLastFailure(Long lastFailure) {
 		this.lastFailure = lastFailure;
 	}
 }
