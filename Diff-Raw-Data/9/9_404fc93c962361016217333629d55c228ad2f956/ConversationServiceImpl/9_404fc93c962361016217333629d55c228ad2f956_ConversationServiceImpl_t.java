 package edu.rit.asksg.service;
 
 
 import edu.rit.asksg.domain.Conversation;
 import edu.rit.asksg.domain.Message;
 import edu.rit.asksg.repository.MessageRepository;
 import org.joda.time.LocalDateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Collection;
 
 public class ConversationServiceImpl implements ConversationService {
 
 	@Autowired
 	MessageService messageService;
 
 	@Autowired
 	MessageRepository messageRepository;
 
 	@Autowired
 	ProviderService providerService;
 
 	public void saveConversations(Collection<Conversation> conversations) {
 		for (Conversation conversation : conversations) {
 			saveConversation(conversation);
 		}
 	}
 
 	public void saveConversation(Conversation conversation) {
 		for (Message m : conversation.getMessages()) {
 
            if(m.getContent() == null) m.setContent("");
 			//TODO make 2000 more visible?
 			if (m.getContent().length() > 2000) m.setContent(m.getContent().substring(0, 2000));
 
 			messageService.saveMessage(m);
 		}
 
 		conversationRepository.save(conversation);
 	}
 
 	public Conversation updateConversation(Conversation conversation) {
 		conversation.setModified(LocalDateTime.now());
 
 //        for(Message m : conversation.getMessages()) {
 //            if(m.getId() == null) messageRepository.save(m);
 //        }
 
 		return conversationRepository.save(conversation);
 	}
 }
