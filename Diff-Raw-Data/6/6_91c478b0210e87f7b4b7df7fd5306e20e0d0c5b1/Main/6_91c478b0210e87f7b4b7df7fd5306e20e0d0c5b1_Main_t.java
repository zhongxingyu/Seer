 package net.robbytu.banjoserver.bungee;
 
 import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ListenerInfo;
 import net.md_5.bungee.api.plugin.Plugin;
 import net.robbytu.banjoserver.bungee.auth.AuthStateAnnouncer;
 import net.robbytu.banjoserver.bungee.auth.ChangePasswordCommand;
 import net.robbytu.banjoserver.bungee.auth.LoginCommand;
 import net.robbytu.banjoserver.bungee.auth.RegisterCommand;
 import net.robbytu.banjoserver.bungee.automessages.BroadcastTask;
 import net.robbytu.banjoserver.bungee.bans.*;
 import net.robbytu.banjoserver.bungee.consoles.ConsoleCommand;
 import net.robbytu.banjoserver.bungee.directsupport.HelpCommand;
 import net.robbytu.banjoserver.bungee.directsupport.ReminderTask;
 import net.robbytu.banjoserver.bungee.directsupport.TicketCommand;
 import net.robbytu.banjoserver.bungee.directsupport.TicketLogoutHandler;
 import net.robbytu.banjoserver.bungee.donate.DonateCommand;
 import net.robbytu.banjoserver.bungee.donate.DonateUtil;
 import net.robbytu.banjoserver.bungee.donate.SmsCommand;
 import net.robbytu.banjoserver.bungee.gamemode.GamemodeCommand;
 import net.robbytu.banjoserver.bungee.kicks.KickCommand;
 import net.robbytu.banjoserver.bungee.list.ListCommand;
 import net.robbytu.banjoserver.bungee.listeners.ChatListener;
 import net.robbytu.banjoserver.bungee.listeners.LoginListener;
 import net.robbytu.banjoserver.bungee.listeners.PluginMessageListener;
 import net.robbytu.banjoserver.bungee.logger.IPLogCommand;
 import net.robbytu.banjoserver.bungee.logger.UserLogCommand;
 import net.robbytu.banjoserver.bungee.mail.MailCheckTask;
 import net.robbytu.banjoserver.bungee.mail.MailCommand;
 import net.robbytu.banjoserver.bungee.mail.MailLoginHandler;
 import net.robbytu.banjoserver.bungee.masks.AboutCommand;
 import net.robbytu.banjoserver.bungee.masks.PluginsCommand;
 import net.robbytu.banjoserver.bungee.mute.MuteCommand;
import net.robbytu.banjoserver.bungee.perms.PermsCommand;
 import net.robbytu.banjoserver.bungee.pm.MsgCommand;
 import net.robbytu.banjoserver.bungee.pm.ReplyCommand;
 import net.robbytu.banjoserver.bungee.tp.TpAcceptCommand;
 import net.robbytu.banjoserver.bungee.tp.TpCommand;
 import net.robbytu.banjoserver.bungee.tp.TpDenyCommand;
 import net.robbytu.banjoserver.bungee.tp.TpHereCommand;
 import net.robbytu.banjoserver.bungee.votes.VoteCheckTask;
 import net.robbytu.banjoserver.bungee.votes.VoteReceiver;
 import net.robbytu.banjoserver.bungee.warns.WarnsCommand;
 import net.robbytu.banjoserver.bungee.whois.WhoisCommand;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.util.concurrent.TimeUnit;
 
 public class Main extends Plugin {
     public static final String PLUGIN_NAME = "bs-bungee";
     public static Main instance;
     public static PluginConfig config;
     public static Connection conn;
 
     private static VoteReceiver voteReceiver;
 
     public void onLoad() {
         instance = this;
         config = new PluginConfig(this);
     }
 
     public void onEnable() {
         // Set up a connection
         try{
             conn = DriverManager.getConnection("jdbc:mysql://" + config.db_host + ":" + config.db_port + "/" + config.db_database + "?autoReconnect=true", config.db_username, config.db_password);
         }
         catch (Exception e) {
             getLogger().warning("bs-bungee has not been enabled. Please check your configuration.");
             e.printStackTrace();
         }
 
         getLogger().info("Registering commands...");
         this.registerCommands();
 
         getLogger().info("Registering listeners...");
         this.registerListeners();
 
         getLogger().info("Registering scheduled tasks...");
         this.registerSchedulers();
 
         getLogger().info("Registering for plugin channels...");
         this.registerChannels();
 
         getLogger().info("Populating statics...");
         this.populateStatics();
 
         getLogger().info("Starting VoteReceiver...");
         this.startVoteReceiver();
     }
 
     private void registerCommands() {
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new WarnsCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new BanCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new TempBanCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new KickCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new IPBanCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new UnbanIPCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new UnbanCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new TpCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new TpHereCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new TpAcceptCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new TpDenyCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new HelpCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new ListCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new DonateCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new SmsCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new TicketCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new MuteCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new MailCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new LoginCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new RegisterCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new ChangePasswordCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new ConsoleCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new MsgCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new ReplyCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new UserLogCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new IPLogCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new GamemodeCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new AboutCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new PluginsCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhoisCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new PermsCommand());
     }
 
     private void registerListeners() {
         ProxyServer.getInstance().getPluginManager().registerListener(this, new LoginListener());
         ProxyServer.getInstance().getPluginManager().registerListener(this, new TicketLogoutHandler());
         ProxyServer.getInstance().getPluginManager().registerListener(this, new PluginMessageListener());
         ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListener());
         ProxyServer.getInstance().getPluginManager().registerListener(this, new MailLoginHandler());
         ProxyServer.getInstance().getPluginManager().registerListener(this, new AuthStateAnnouncer());
     }
 
     private void registerSchedulers() {
         ProxyServer.getInstance().getScheduler().schedule(this, new BroadcastTask(), 300, TimeUnit.SECONDS);
         ProxyServer.getInstance().getScheduler().schedule(this, new ReminderTask(), 60, TimeUnit.SECONDS);
         ProxyServer.getInstance().getScheduler().schedule(this, new MailCheckTask(), 60, TimeUnit.SECONDS);
         ProxyServer.getInstance().getScheduler().schedule(this, new VoteCheckTask(), 300, TimeUnit.SECONDS);
     }
 
     private void registerChannels() {
         ProxyServer.getInstance().registerChannel("BSBungee");
     }
 
     private void populateStatics() {
         DonateUtil.populateStatics();
     }
 
     private void startVoteReceiver() {
        Main.voteReceiver = new VoteReceiver("0.0.0.0", 8192);
         getProxy().getScheduler().runAsync(this, new Runnable() {
             @Override
             public void run() {
                 voteReceiver.run();
             }
         });
     }
 
     public void onDisable() {
         try {
             config.save();
             getLogger().info("Saved configuration.");
         } catch (InvalidConfigurationException e) {
             getLogger().warning("Could not save configuration file to disk.");
             e.printStackTrace();
         }
     }
 }
