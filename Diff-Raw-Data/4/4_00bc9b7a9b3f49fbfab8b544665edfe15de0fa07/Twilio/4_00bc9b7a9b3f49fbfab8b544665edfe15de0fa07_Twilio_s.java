 package edu.rit.asksg.domain;
 
 import com.twilio.sdk.TwilioRestClient;
 import com.twilio.sdk.TwilioRestException;
 import com.twilio.sdk.resource.factory.SmsFactory;
 import com.twilio.sdk.resource.instance.Sms;
 import edu.rit.asksg.dataio.ContentProvider;
 import edu.rit.asksg.domain.config.ProviderConfig;
 import edu.rit.asksg.domain.config.TwilioConfig;
 import edu.rit.asksg.service.ConversationService;
 import flexjson.JSON;
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson
 public class Twilio extends Service implements ContentProvider {
 
 	@Autowired
 	private transient ConversationService conversationService;
 
 	private transient static final Logger logger = LoggerFactory.getLogger(Twilio.class);
 
 	@JSON(include = false)
 	@Override
 	public List<Conversation> getNewContent() {
 		logger.debug("Twilio does not support fetching new content.");
 		return new ArrayList<Conversation>();
 	}
 
 	@JSON(include = false)
 	@Override
 	public List<Conversation> getContentSince(LocalDateTime datetime) {
 		return new ArrayList<Conversation>();
 	}
 
 	@Override
 	public boolean postContent(Message message) {
 		final TwilioConfig config = (TwilioConfig) this.getConfig();
 		TwilioRestClient twc = new TwilioRestClient(config.getUsername(), config.getAuthenticationToken());
 		Map<String, String> vars = new HashMap<String, String>();
 		vars.put("Body", message.getContent());
 		vars.put("From", config.getPhoneNumber());
 		// This is the fix recommended by Ryan because the UI can't be relied on to send a 'to' >_>
 		String number = ((Message[])message.getConversation().getMessages().toArray())[0].getAuthor();
 		vars.put("To", number);
 
 		SmsFactory smsFactory = twc.getAccount().getSmsFactory();
 		try {
 			Sms sms = smsFactory.create(vars);
 			//TODO: Twilio can use a callback to POST information to if sending fails
 		} catch (TwilioRestException e) {
 			//logger.error("Failed to send outgoing message to " + message.getAuthor(), e);
			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean authenticate() {
 		return false;
 	}
 
 	@Override
 	public boolean isAuthenticated() {
 		return false;
 	}
 
 	public void handleMessage(String smsSid, String accountSid, String from, String to, String body) {
 
 		Message msg = new Message();
 		msg.setContent(body);
 		msg.setAuthor(from);
 		msg.setCreated(LocalDateTime.now());
 		msg.setModified(LocalDateTime.now());
 
 		Conversation conv = new Conversation(msg);
 		msg.setConversation(conv);
 
 		conv.setService(this);
 		conv.setExternalId(smsSid);
 
 		conversationService.saveConversation(conv);
 	}
 
 	@JSON(include = false)
 	public ConversationService getConversationService() {
 		return conversationService;
 	}
 
 	public void setConversationService(ConversationService conversationService) {
 		this.conversationService = conversationService;
 	}
 }
