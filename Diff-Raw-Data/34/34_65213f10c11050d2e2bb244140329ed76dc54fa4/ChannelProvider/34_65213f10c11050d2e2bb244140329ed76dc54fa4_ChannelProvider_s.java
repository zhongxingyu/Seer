 package com.comphenix.xp.messages;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import com.comphenix.xp.extra.ServiceProvider;
 
 /**
  * Contains every channel messaging channel plugins known and enabled on the server. The default plugin will
  * be used to send messages.
  * 
  * @author Kristian
  */
 public class ChannelProvider extends ServiceProvider<ChannelService> {
 
 	private List<String> defaultChannels = new ArrayList<String>();
 	private MessageFormatter messageFormatter;
 
 	public ChannelProvider() {
 		super(HeroService.NAME);
 	}
 	
 	public ChannelProvider(ChannelProvider other) {
 		super(other.getDefaultName());
 		setDefaultChannels(other.getDefaultChannels());
 		setMessageFormatter(other.getMessageFormatter());
 	}
 	
 	public ChannelProvider(String defaultService) {
 		super(defaultService);
 	}
 	
 	public MessageFormatter getFormatter(Player player, Integer experience) {
 		return messageFormatter.createView(player, experience);
 	}
 	
 	public MessageFormatter getFormatter(Player player, Integer experience, Integer count) {
 		return messageFormatter.createView(player, experience, count);
 	}
 	
 	public MessageFormatter getMessageFormatter() {
 		return messageFormatter;
 	}
 
 	public void setMessageFormatter(MessageFormatter messageFormatter) {
 		this.messageFormatter = messageFormatter;
 	}
 	
 	public List<String> getDefaultChannels() {
 		return defaultChannels;
 	}
 
 	public void setDefaultChannels(List<String> defaultChannels) {
 		this.defaultChannels = defaultChannels;
 	}
 	
 	/**
 	 * Creates a copy of this channel provider with shallow references to the same list of channel services, except with a different
 	 * internal default reward type. 
	 * @param config Configuration settings for the different services.
 	 * @return A shallow copy of this reward service provider.
 	 */
 	public ChannelProvider createView() {
 		return new ChannelProvider(this);
 	}
 }
