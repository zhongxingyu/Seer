/**
  * Copyright © 2012-2013 Bruce Cowan <bruce@bcowan.me.uk>
  * Copyright © 2012-2013 Joseph Walton-Rivers <webpigeon@unitycoders.co.uk>
  *
  * This file is part of uc_PircBotX.
  *
  * uc_PircBotX is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * uc_PircBotX is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * uc_PircBotX. If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.unitycoders.pircbotx;
 
 import javax.net.ssl.SSLSocketFactory;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.managers.ListenerManager;
 
 import uk.co.unitycoders.pircbotx.commandprocessor.CommandListener;
 import uk.co.unitycoders.pircbotx.commandprocessor.CommandProcessor;
 import uk.co.unitycoders.pircbotx.commands.*;
 import uk.co.unitycoders.pircbotx.data.db.DBConnection;
 import uk.co.unitycoders.pircbotx.listeners.JoinsListener;
 import uk.co.unitycoders.pircbotx.listeners.LinesListener;
 import uk.co.unitycoders.pircbotx.profile.ProfileCommand;
 import uk.co.unitycoders.pircbotx.profile.ProfileManager;
 
 /**
  * The actual bot itself.
  *
  * @author Bruce Cowan
  */
 public class Bot {
 
     public static void main(String[] args) throws Exception {
         // Bot Configuration
         Configuration config = ConfigurationManager.loadConfig();
 
         CommandProcessor processor = new CommandProcessor(config.trigger);
 
         ProfileManager profiles = new ProfileManager(DBConnection.getProfileModel());
         DateTimeCommand dtCmd = new DateTimeCommand();
 
         // Commands
         processor.register("rand", new RandCommand());
         processor.register("time", dtCmd);
         processor.register("date", dtCmd);
         processor.register("datetime", dtCmd);
         processor.register("lart", new LartCommand());
         processor.register("killertrout", new KillerTroutCommand());
         processor.register("joins", new JoinsCommand());
         processor.register("calc", new CalcCommand());
         processor.register("karma", new KarmaCommand());
         processor.register("profile", new ProfileCommand(profiles));
         processor.register("help", new HelpCommand(processor));
 
         PircBotX bot = new PircBotX();
         ListenerManager<? extends PircBotX> manager = bot.getListenerManager();
 
         // Listeners
         manager.addListener(new CommandListener(processor));
         manager.addListener(new LinesListener());
         manager.addListener(JoinsListener.getInstance());
 
         // Snapshot (1.8-SNAPSHOT) only
         bot.setAutoReconnect(true);
         bot.setAutoReconnectChannels(true);
 
         try {
             bot.setName(config.nick);
             if (config.ssl) {
                 bot.connect(config.host, config.port, SSLSocketFactory.getDefault());
             } else {
                 bot.connect(config.host, config.port);
             }
 
             for (String channel : config.channels) {
                 bot.joinChannel(channel);
             }
             bot.setVerbose(true);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
