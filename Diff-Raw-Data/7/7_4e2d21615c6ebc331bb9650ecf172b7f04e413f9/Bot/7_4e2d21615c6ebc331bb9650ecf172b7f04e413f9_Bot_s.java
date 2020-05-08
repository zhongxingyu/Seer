 /**
  * Copyright © 2012-2014 Bruce Cowan <bruce@bcowan.me.uk>
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
 
 import org.pircbotx.Configuration;
 import org.pircbotx.Configuration.Builder;
 import org.pircbotx.PircBotX;
 
 import uk.co.unitycoders.pircbotx.commandprocessor.CommandListener;
 import uk.co.unitycoders.pircbotx.commandprocessor.CommandProcessor;
 import uk.co.unitycoders.pircbotx.commands.CalcCommand;
 import uk.co.unitycoders.pircbotx.commands.DateTimeCommand;
 import uk.co.unitycoders.pircbotx.commands.FactoidCommand;
 import uk.co.unitycoders.pircbotx.commands.HelpCommand;
 import uk.co.unitycoders.pircbotx.commands.JoinsCommand;
 import uk.co.unitycoders.pircbotx.commands.KarmaCommand;
 import uk.co.unitycoders.pircbotx.commands.KillerTroutCommand;
 import uk.co.unitycoders.pircbotx.commands.LartCommand;
 import uk.co.unitycoders.pircbotx.commands.NickCommand;
 import uk.co.unitycoders.pircbotx.commands.RandCommand;
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
         LocalConfiguration localConfig = ConfigurationManager.loadConfig();
 
         CommandProcessor processor = new CommandProcessor(localConfig.trigger);
 
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
         processor.register("nick", new NickCommand());
         processor.register("factoid", new FactoidCommand(DBConnection.getFactoidModel()));
 
         // Configure bot
         Builder<PircBotX> cb = new Configuration.Builder<PircBotX>()
             .setName(localConfig.nick)
             .setAutoNickChange(true)
             .setAutoReconnect(true)
             .setServer(localConfig.host, localConfig.port)
             .addAutoJoinChannel("unity-coders")
             .addListener(new CommandListener(processor))
             .addListener(new LinesListener())
             .addListener(JoinsListener.getInstance());
 
         // Add channels to join
         for (String channel : localConfig.channels)
         {
             cb.addAutoJoinChannel(channel);
         }
         Configuration<PircBotX> configuration = cb.buildConfiguration();
         PircBotX bot = new PircBotX(configuration);
 
         try {
             bot.startBot();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
