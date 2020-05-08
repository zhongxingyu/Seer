 package swag49.web;
 
 import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import swag49.transfer.model.MessageDTO;
 
 import java.util.Collection;
 
 public class InternalMessageStore {
 
     private Multimap<String, MessageDTO> receivedMessages = HashMultimap.create();
     private Multimap<String, MessageDTO> messageListCache = HashMultimap.create();
 
     public Collection<MessageDTO> getMessageList(String userId) {
         return messageListCache.get(userId);
     }
 
     public Collection<MessageDTO> getNewMessagesAndRemoveFromCache(String userId) {
        Collection<MessageDTO> result = Lists.newArrayList(receivedMessages.get(userId));
         receivedMessages.removeAll(userId);
         return result;
     }
 
     public void addMessageList(String userId, Iterable<MessageDTO> messages) {
         messageListCache.putAll(userId, messages);
     }
 
     public void addReceivedMessage(String userId, MessageDTO message) {
         receivedMessages.put(userId, message);
     }
 }
