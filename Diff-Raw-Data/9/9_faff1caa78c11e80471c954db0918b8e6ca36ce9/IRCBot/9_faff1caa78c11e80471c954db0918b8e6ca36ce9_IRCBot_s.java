 package com.araeosia.ArcherGames.utils;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.ConnectEvent;
 import org.pircbotx.hooks.events.MessageEvent;
 
 public class IRCBot extends ListenerAdapter implements Listener {
 
 	public ArcherGames plugin;
 	public String botname;
 	public String host;
 	public int port;
 	public String channel;
 	public PircBotX bot;
 	public String password;
 	public int howMuchToGive;
 
 	public IRCBot(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	public void setupBot() throws Exception {
 		bot = new PircBotX();
 		bot.setLogin(botname);
 		bot.setName(botname);
 		bot.connect(host);
 		bot.joinChannel(channel);
 	}
 
 	@Override
 	public void onConnect(final ConnectEvent event) {
 		bot.identify(password);
 	}
 
 	@Override
 	public void onMessage(final MessageEvent event) {
 		//User§ServiceName§Address§Timestamp
 		String message = event.getMessage();
 		String[] data = message.split("§");
 		plugin.serverwide.sendMessageToAllPlayers(String.format(plugin.strings.get("playervoted"), data[0]));
 		if (plugin.getServer().getPlayer(data[0]) != null) {
			ArcherGames.econ.depositPlayer(plugin.getServer().getPlayer(data[0]).getName(), 3000);
 		}
 	}
 }
