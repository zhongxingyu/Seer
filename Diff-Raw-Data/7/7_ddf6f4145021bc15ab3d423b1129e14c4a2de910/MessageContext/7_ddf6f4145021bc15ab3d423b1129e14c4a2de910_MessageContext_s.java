 package roujo.emily.core;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.PrivateMessageEvent;
 
 import roujo.emily.core.util.InternalUser;
 import roujo.emily.core.util.UserHelper;
 
 public class MessageContext {
 	private final State state;
 	private final PircBotX bot;
 	private final Channel channel;
 	private final User user;
 	private final InternalUser internalUser;
 	private final String message;
 	private String processedMessage;
 
 	public MessageContext(State state, MessageEvent<PircBotX> event) {
 		this(state, event.getBot(), event.getChannel(), event.getUser(), event.getMessage());
 	}
 
 	public MessageContext(State state, PrivateMessageEvent<PircBotX> event) {
 		this(state, event.getBot(), null, event.getUser(), event.getMessage());
 	}
 	
 	public MessageContext(State state, PircBotX bot, Channel channel, User user, String message) {
 		this.state = state;
 		this.bot = bot;
 		this.channel = channel;
 		this.user = user;
 		this.internalUser = UserHelper.getUserByNick(user.getNick());
 		this.message = message;
 		this.processedMessage = null;
 	}
 	
 	public State getState() {
 		return state;
 	}
 	
 	public PircBotX getBot() {
 		return bot;
 	}
 
 	public Channel getChannel() {
 		return channel;
 	}
 
 	public User getUser() {
 		return user;
 	}
 	
 	public InternalUser getInternalUser() {
 		return internalUser;
 	}
 
 	public String getMessage() {
		return message;
 	}
 	
 	public boolean hasBeenProcessed() {
 		return processedMessage != null;
 	}
 	
 	public String getProcessedMessage() {
 		return processedMessage;
 	}
 	
 	public void setProcessedMessage(String processedMessage) {
 		this.processedMessage = processedMessage;
 	}
 	
 	public boolean isPrivateMessage() {
 		return channel == null;
 	}
 }
