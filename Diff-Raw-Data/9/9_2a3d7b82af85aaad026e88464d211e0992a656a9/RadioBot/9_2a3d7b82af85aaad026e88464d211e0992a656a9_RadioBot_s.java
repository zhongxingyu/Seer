 //Copyright 2013 Xirme Community
 
 package me.xir.radio.radiobot;
 
 //Java imports
 
 //PircBotX imports
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.UtilSSLSocketFactory;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.ListenerAdapter;
 
 
 
 
 
 
 //RadioBot imports
 import me.xir.radio.radiobot.Config;
 import me.xir.radio.radiobot.commands.*;
 import me.xir.radio.radiobot.utils.*;
 
 @SuppressWarnings("rawtypes")
 public class RadioBot extends ListenerAdapter implements Listener {
 
 	public static PircBotX bot = new PircBotX();
 	public final static Logger logger = Logger.getLogger("RadioBot");
 
 	public static void main(String[] args) throws Exception, FileNotFoundException, IOException {
 		//Load Config
 		try {
 			Config.loadConfig();
 		} catch (FileNotFoundException ex) {
 			Config.config.setProperty("nick", "RadioBot");
 			Config.config.setProperty("user", "RadioBot");
 			Config.config.setProperty("server", "irc.xir.me");
 			Config.config.setProperty("port", "6667");
 			Config.config.setProperty("password", "");
 			Config.config.setProperty("serverpassword", "");
 			Config.config.setProperty("channels", "#radio");
 			Config.config.setProperty("ssl", "false");
 			Config.config.setProperty("admins", "");
 			
 			Config.config.setProperty("scserver", "127.0.0.1");
			Config.config.setProperty("scserver", "8000");
 			Config.config.setProperty("scauser", "admin");
 			Config.config.setProperty("scapass", "password");
 			
 			Config.config.store(new FileOutputStream("RadioBot.properties"), null);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		
 
 		//Setup the bot with all the things
 		bot.setAutoNickChange(true);
 		bot.setVerbose(true);
 
 		bot.setVersion("RadioBot v0.1 - http://radio.xir.me");
 
 		bot.setLogin(Config.user);
 		bot.setName(Config.nick);
 		bot.identify(Config.pass);
 
 		//Connection start
 		if(Config.SSL && !Config.serverpass.isEmpty()) {
 			bot.connect(Config.server, Config.port, Config.serverpass, new UtilSSLSocketFactory().trustAllCertificates());			
 		} else if (Config.SSL && Config.serverpass.isEmpty()) {
 			bot.connect(Config.server, Config.port, new UtilSSLSocketFactory().trustAllCertificates());
 		} else {
 			bot.connect(Config.server, Config.port);
 		}
 
 		bot.setAutoReconnect(true);
 		bot.setAutoReconnectChannels(true);
 
 		joinChannels();
 		loadListeners();
 		Radio_Query.repeatQuery();
 		
 	}
 	public static void joinChannels() {
 		for (int i = 0; i < Config.channels.length; i++) {
 			bot.joinChannel(Config.channels[i]);
 		}
 	}
 	public static void loadListeners() throws Exception {
 		bot.getListenerManager().addListener(new Radio_Query());
 		bot.getListenerManager().addListener(new Basic_Information());
 	}
 }
