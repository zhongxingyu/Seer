 package de.derflash.plugins.cnwarn;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.cubenation.plugins.utils.commandapi.CommandsManager;
 import de.cubenation.plugins.utils.commandapi.ErrorHandler;
 import de.cubenation.plugins.utils.commandapi.exception.CommandException;
 import de.cubenation.plugins.utils.commandapi.exception.CommandManagerException;
 import de.cubenation.plugins.utils.commandapi.exception.CommandWarmUpException;
 import de.derflash.plugins.cnwarn.commands.WarnAcceptCommand;
 import de.derflash.plugins.cnwarn.commands.WarnAddCommand;
 import de.derflash.plugins.cnwarn.commands.WarnSearchCommand;
 import de.derflash.plugins.cnwarn.commands.WarnConfirmCommand;
 import de.derflash.plugins.cnwarn.commands.WarnDeleteCommand;
 import de.derflash.plugins.cnwarn.commands.WarnListCommand;
 import de.derflash.plugins.cnwarn.commands.WatchAddCommand;
 import de.derflash.plugins.cnwarn.commands.WatchDeleteCommand;
 import de.derflash.plugins.cnwarn.commands.WatchInfoCommand;
 import de.derflash.plugins.cnwarn.commands.WatchListCommand;
 import de.derflash.plugins.cnwarn.eventlistener.PlayerListener;
 import de.derflash.plugins.cnwarn.model.Warn;
 import de.derflash.plugins.cnwarn.model.Watch;
 import de.derflash.plugins.cnwarn.services.ChatService;
 import de.derflash.plugins.cnwarn.services.PermissionService;
 import de.derflash.plugins.cnwarn.services.WarnService;
 import de.derflash.plugins.cnwarn.services.WatchService;
 
 public class CNWarn extends JavaPlugin {
     private ChatService chatService;
     private PermissionService permissionService;
     private WarnService warnService;
     private WatchService watchService;
 
     private CommandsManager commandsManager;
    
    public static CNWarn p;
 
     @Override
     public void onEnable() {
    	CNWarn.p = this;
    	
         setupDatabase();
 
         permissionService = new PermissionService();
         chatService = new ChatService();
         warnService = new WarnService(getDatabase(), chatService);
         watchService = new WatchService(getDatabase());
 
         getServer().getPluginManager().registerEvents(new PlayerListener(warnService, watchService, permissionService, chatService), this);
 
         try {
             commandsManager = new CommandsManager(this);
             commandsManager.setPermissionInterface(permissionService);
             commandsManager.setErrorHandler(new ErrorHandler() {
                 @Override
                 public void onError(Exception e) {
                     getLogger().log(Level.SEVERE, "error on command", e);
                 }
             });
             registerCommands();
         } catch (CommandWarmUpException e) {
             getLogger().log(Level.SEVERE, "error on register command", e);
         } catch (CommandManagerException e) {
             getLogger().log(Level.SEVERE, "error on inital command manager", e);
         }
     }
 
     private void registerCommands() throws CommandWarmUpException {
         commandsManager.add(WarnAcceptCommand.class, warnService, chatService);
         commandsManager.add(WarnAddCommand.class, warnService, chatService);
         commandsManager.add(WarnConfirmCommand.class, warnService);
         commandsManager.add(WarnDeleteCommand.class, warnService, chatService);
         commandsManager.add(WarnListCommand.class, warnService, chatService);
         commandsManager.add(WarnSearchCommand.class, warnService, chatService);
         commandsManager.add(WatchAddCommand.class, watchService, chatService);
         commandsManager.add(WatchDeleteCommand.class, watchService, chatService);
         commandsManager.add(WatchInfoCommand.class, watchService, chatService);
         commandsManager.add(WatchListCommand.class, watchService, chatService);
     }
 
     @Override
     public void onDisable() {
         commandsManager.clear();
     }
 
     public void setupDatabase() {
         try {
             getDatabase().find(Warn.class).findRowCount();
             getDatabase().find(Watch.class).findRowCount();
         } catch (PersistenceException ex) {
             getLogger().info("Installing database due to first time usage");
             installDDL();
         }
     }
 
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(Watch.class);
         list.add(Warn.class);
         return list;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         try {
             commandsManager.execute(sender, command, label, args);
         } catch (CommandException e) {
             getLogger().log(Level.SEVERE, "error on command", e);
             return false;
         }
         return true;
     }
 }
