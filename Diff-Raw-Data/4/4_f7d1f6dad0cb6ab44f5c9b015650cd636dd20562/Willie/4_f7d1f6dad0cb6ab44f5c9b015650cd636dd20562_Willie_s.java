 package com.drtshock.willie;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.pircbotx.Base64;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.exception.IrcException;
 import org.pircbotx.exception.NickAlreadyInUseException;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.WaitForQueue;
 import org.pircbotx.hooks.events.NoticeEvent;
 
 import com.drtshock.willie.command.AdminCommandHandler;
 import com.drtshock.willie.command.CICommandHandler;
 import com.drtshock.willie.command.Command;
 import com.drtshock.willie.command.DonateCommandHandler;
 import com.drtshock.willie.command.DrinkCommandHandler;
 import com.drtshock.willie.command.FixCommandHandler;
 import com.drtshock.willie.command.HelpCommandHandler;
 import com.drtshock.willie.command.IssuesCommandHandler;
 import com.drtshock.willie.command.JoinCommandHandler;
 import com.drtshock.willie.command.LatestCommandHandler;
 import com.drtshock.willie.command.LeaveCommandHandler;
 import com.drtshock.willie.command.PluginCommandHandler;
 import com.drtshock.willie.command.PopcornCommandHandler;
 import com.drtshock.willie.command.ReloadCommandHandler;
 import com.drtshock.willie.command.RepoCommandHandler;
 import com.drtshock.willie.command.RulesCommandHandler;
 import com.drtshock.willie.command.SaveCommandHandler;
 import com.drtshock.willie.command.TWSSCommandHandler;
 import com.google.gson.Gson;
 import com.google.gson.JsonParser;
 
 public class Willie extends PircBotX {
     public static final Logger logger = Logger.getLogger(Willie.class.getName());
     public static final Gson gson = new Gson();
     public static final JsonParser parser = new JsonParser();
     public static String GIT_AUTH;
     private static String CONFIG_FILE = "config.yml";
 
     public JenkinsServer jenkins;
     public CommandManager commandManager;
     private WillieConfig willieConfig;
 
     // Could be moved into it's own class if it gets crowded in here.
     private ListenerAdapter<Willie> nickAuthListener = new ListenerAdapter<Willie>() {
         @Override
         public void onNotice(NoticeEvent<Willie> event) throws Exception {
             if(event.getUser().getNick().equalsIgnoreCase("nickserv")) {
                 getListenerManager().removeListener(this);
                 logger.info("Removed nickserv listener.");
                 try (WaitForQueue queue = new WaitForQueue(event.getBot())) {
                     event.getUser().sendMessage("IDENTIFY " + event.getBot().getConfig().getAccountPass());
                     NoticeEvent rEvent = queue.waitFor(NoticeEvent.class);
                     if(rEvent.getNotice().contains("identified")) {
                         logger.info("Identified with nickserv.");
                     }
                     else {
                         logger.log(Level.INFO, "Got unexpected response from nickserv: {0}", rEvent.getNotice());
                     }
                 }
             }
         }
     };
 
     public Willie() {
         this(new WillieConfig());
     }
 
     public Willie(WillieConfig config) {
         super();
         this.willieConfig = config;
 
         GIT_AUTH = "Basic " + Base64.encodeToString((willieConfig.getGitHubUsername() + ":" + willieConfig.getGitHubPassword()).getBytes(), false);
 
         this.jenkins = new JenkinsServer(willieConfig.getJenkinsServer());
         this.commandManager = new CommandManager(this);
 
         this.commandManager.registerCommand(new Command("repo", "show Willie's repo", new RepoCommandHandler()));
         this.commandManager.registerCommand(new Command("latest", "<plugin_name> - Get latest file for plugin on BukkitDev", new LatestCommandHandler()));
         this.commandManager.registerCommand(new Command("plugin", "<name> - looks up a plugin on BukkitDev", new PluginCommandHandler()));
         this.commandManager.registerCommand(new Command("issues", "<job_name> [page] - check github issues for jobs on " + willieConfig.getJenkinsServer(), new IssuesCommandHandler()));
         this.commandManager.registerCommand(new Command("ci", "shows Jenkins info", new CICommandHandler()));
         this.commandManager.registerCommand(new Command("rules", "show channel rules", new RulesCommandHandler()));
         this.commandManager.registerCommand(new Command("help", "show this help info", new HelpCommandHandler()));
         this.commandManager.registerCommand(new Command("p", "pop some popcorn!", new PopcornCommandHandler()));
         this.commandManager.registerCommand(new Command("twss", "that's what she said!", new TWSSCommandHandler()));
         this.commandManager.registerCommand(new Command("donate", "shows donation info", new DonateCommandHandler()));
         this.commandManager.registerCommand(new Command("drink", "<name> - gives someone a drink!", new DrinkCommandHandler()));
         this.commandManager.registerCommand(new Command("fix", "[name] - Yell at someone to fix something", new FixCommandHandler()));
        this.commandManager.registerCommand(new Command("kick", "<name> - Kick a user", new FixCommandHandler()));
 
 
         this.commandManager.registerCommand(new Command("join", "<channel> - Joins a channel", new JoinCommandHandler(), true));
         this.commandManager.registerCommand(new Command("leave", "<channel> - Leaves a channel", new LeaveCommandHandler(), true));
         this.commandManager.registerCommand(new Command("reload", "Reloads willie", new ReloadCommandHandler(), true));
         this.commandManager.registerCommand(new Command("save", "Saves configuration", new SaveCommandHandler(), true));
         this.commandManager.registerCommand(new Command("admin", "add <user> | del <user> | list - Modifies the bot admin list.", new AdminCommandHandler(), true));
 
         this.setName(willieConfig.getNick());
         this.setVerbose(false);
         this.getListenerManager().addListener(this.commandManager);
     }
 
     public void connect() {
         try {
             this.connect(willieConfig.getServer());
             this.setAutoReconnectChannels(true);
             logger.log(Level.INFO, "Connected to ''{0}''", willieConfig.getServer());
 
             if(!willieConfig.getAccountPass().isEmpty()) {
                 getListenerManager().addListener(nickAuthListener);
             }
 
             for (String channel : willieConfig.getChannels()) {
                 this.joinChannel(channel);
                 logger.log(Level.INFO, "Joined channel ''{0}''", channel);
             }
 
             (new Timer()).schedule(new IssueNotifierTask(this), 300000, 300000); // 5 minutes
         } catch (NickAlreadyInUseException e) {
             logger.severe("That nickname is already in use!");
         } catch (IrcException | IOException e) {
         }
     }
 
     @Override
     public Channel getChannel(String string) {
         for (Channel channel : getUserBot().getChannels()) {
             if (channel.getName().equalsIgnoreCase(string)) {
                 return channel;
             }
         }
         return super.getChannel(string);
     }
 
     public boolean isOnChannel(String channel) {
         for (Channel chan : getUserBot().getChannels()) {
             if (chan.getName().equalsIgnoreCase(channel)) {
                 return true;
             }
         }
         return false;
     }
 
     public class AuthResponse {
         public boolean isValid = false;
         public boolean isLoggedIn = false;
         public boolean isAdmin = false;
         public String accountName = "";
 
         public AuthResponse(NoticeEvent event, User user) {
             if (!event.getUser().getNick().equals("NickServ")) return;
             String[] args = event.getNotice().split(" ");
             if (args.length < 5) return;
             if (!args[0].equals(user.getNick())) return;
 
             this.isValid = true;
             this.accountName = args[2];
             if (Integer.parseInt(args[4]) == 3) this.isLoggedIn = true;
             if (this.isLoggedIn && willieConfig.getAdmins().contains(accountName)) {
                 this.isAdmin = true;
             }
         }
 
         public AuthResponse() {
         }
     }
 
 
     public AuthResponse getAuth(User user) {
         getUser("NickServ").sendMessage(String.format("ACC %s *", user.getNick()));
         WaitForQueue queue = new WaitForQueue(this);
         try {
             NoticeEvent event = queue.waitFor(NoticeEvent.class);
             return new AuthResponse(event, user);
         } catch (Exception ignored) {
         }
         return new AuthResponse();
     }
 
     public void reload() {
         logger.info("Reloading...");
         willieConfig = WillieConfig.loadFromFile(CONFIG_FILE);
 
         // Update command prefix
         commandManager.setCommandPrefix(willieConfig.getCommandPrefix());
 
         // Nick
         if (!willieConfig.getNick().equals(getNick())) {
             changeNick(willieConfig.getNick());
             logger.info("Nick updated.");
         }
 
         // Server
         if (!willieConfig.getServer().equals(getServer())) {
             for (Channel channel : getChannels()) {
                 channel.sendMessage("Uh oh...looks like I'm on the wrong server.");
             }
             logger.info("Bot seems to be on the wrong server! Reconnecting...");
             disconnect();
             connect();
         }
 
         // Channels
         ArrayList<String> newChannels = willieConfig.getChannels();
         Set<String> oldChannels = getChannelsNames();
         for (Channel channel : getChannels()) channel.sendMessage(Colors.RED + "Reloading...");
         for (String channel : willieConfig.getChannels()) {
             if (!oldChannels.contains(channel)) {
                 joinChannel(channel);
                 logger.log(Level.INFO, "Joined new channel {0}", channel);
                 getChannel(channel).sendMessage(Colors.GREEN + "Someone told me I belong here.");
             }
         }
         for (String channel : oldChannels) {
             if (!newChannels.contains(channel)) {
                 getChannel(channel).sendMessage(Colors.RED + "Looks like I don't belong here...");
                 logger.log(Level.INFO, "Leaving channel {0}", channel);
                 partChannel(getChannel(channel));
             }
         }
     }
 
     public void save() {
         // Save channels
         willieConfig.getChannels().clear();
         for(Channel channel:getUserBot().getChannels()) {
             willieConfig.getChannels().add(channel.getName());
         }
 
         willieConfig.update();
         willieConfig.save(CONFIG_FILE);
     }
 
     public WillieConfig getConfig() {
         return willieConfig;
     }
 
     public static void main(String[] args) {
         Willie willie = new Willie(WillieConfig.loadFromFile("config.yml"));
         willie.connect();
     }
 
 }
