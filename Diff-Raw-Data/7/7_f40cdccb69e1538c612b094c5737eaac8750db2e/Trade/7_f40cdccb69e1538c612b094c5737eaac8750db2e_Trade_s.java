 package me.Josvth.Trade;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import me.Josvth.Trade.Handlers.CommandHandler;
 import me.Josvth.Trade.Handlers.CurrencyTradeHandler;
 import me.Josvth.Trade.Handlers.LanguageHandler;
 import me.Josvth.Trade.Handlers.LanguageHandler.MessageArgument;
 import me.Josvth.Trade.Handlers.TradeHandler;
 import me.Josvth.Trade.Handlers.YamlHandler;
 import me.Josvth.Trade.Listeners.InTradeListener;
 import me.Josvth.Trade.Listeners.RequestListener;
 import net.citizensnpcs.Citizens;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.GameMode;
 import org.bukkit.Server;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.ServicesManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import com.garbagemule.MobArena.MobArena;
 import com.garbagemule.MobArena.MobArenaHandler;
 import com.herocraftonline.heroes.Heroes;
 
 public class Trade extends JavaPlugin
 {
   Logger logger;
   public HashMap<Player, Player> pendingRequests = new HashMap();
 
   public HashMap<Player, TradeHandler> activeTrades = new HashMap();
 
   public ArrayList<Player> ignoring = new ArrayList();
   public Economy economyHandler;
   public MobArenaHandler mobArenaHandler;
   public Heroes heroesPlugin;
   public Plugin cititzensPlugin;
   public YamlHandler yamlHandler;
   LanguageHandler languageHandler;
   CommandHandler commandHandler;
   InTradeListener inTradeListener;
   RequestListener requestListener;
 
   public void onEnable()
   {
     logger = getLogger();
 
     mobArenaHandler = getMobArenaHandler(this.getServer().getPluginManager().getPlugin("MobArena"));
     heroesPlugin = getHeroesPlugin(this.getServer().getPluginManager().getPlugin("Heroes"));
     cititzensPlugin = getCitizensPlugin(this.getServer().getPluginManager().getPlugin("Citizens"));
     yamlHandler = new YamlHandler(this);
     languageHandler = new LanguageHandler(this);
     commandHandler = new CommandHandler(this);
 
     if (yamlHandler.configYaml.getBoolean("economy.enabled", false)) setupEconomy();
 
     inTradeListener = new InTradeListener(this);
     requestListener = new RequestListener(this);
   }
 
   private boolean setupEconomy()
   {
     RegisteredServiceProvider economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
     if (economyProvider != null) {
       economyHandler = ((Economy)economyProvider.getProvider());
     }
 
     return economyHandler != null;
   }
 
   public LanguageHandler getLanguageHandler() {
     return languageHandler;
   }
 
   public void requestPlayer(final Player requester, final Player requested) {
     if ((!requester.canSee(requested)) && (yamlHandler.configYaml.getBoolean("must_see", true))) {
       languageHandler.sendMessage(requester, "request.must-see");
       return;
     }
     if ((!requester.getWorld().equals(requested.getWorld())) && (!yamlHandler.configYaml.getBoolean("cross_world", false))) {
       languageHandler.sendMessage(requester, "request.no-cross-world");
       return;
     }
     if ((!requester.getGameMode().equals(requested.getGameMode())) && (!yamlHandler.configYaml.getBoolean("cross_gamemode", false))) {
       languageHandler.sendMessage(requester, "request.no-cross-gamemode");
       return;
     }
 
     if (activeTrades.containsKey(requested)) {
       languageHandler.sendMessage(requester, "request.in-trade", new LanguageHandler.MessageArgument("%playername%", requested.getName()));
       return;
     }
 
     if (ignoring.contains(requested)) {
       languageHandler.sendMessage(requester, "request.ignoring.is-ignoring", new LanguageHandler.MessageArgument("%playername%", requested.getName()));
       return;
     }
     if (pendingRequests.containsKey(requester)) {
       languageHandler.sendMessage(requester, "request.please-wait", new LanguageHandler.MessageArgument("%playername%", requested.getName()));
       return;
     }
     pendingRequests.put(requester, requested);
     languageHandler.sendMessage(requester, "request.new.self", new LanguageHandler.MessageArgument("%playername%", requested.getName()));
     languageHandler.sendMessage(requested, "request.new.other", new LanguageHandler.MessageArgument("%playername%", requester.getName()));
     getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable()
     {
       public void run() {
         if (pendingRequests.containsKey(requester)) {
           languageHandler.sendMessage(requester, "request.no-response", new LanguageHandler.MessageArgument("%playername%", requested.getName()));
           pendingRequests.remove(requester);
         }
       }
     }
     , yamlHandler.configYaml.getInt("request_timeout", 10) * 20);
   }
 
   public void acceptRequest(Player requested, Player requester) {
     startTrading(requested, requester);
     pendingRequests.remove(requester);
   }
 
   public void refuseRequest(Player requested, Player requester) {
     languageHandler.sendMessage(requester, "request.refused", new LanguageHandler.MessageArgument("%playername%", requested.getName()));
     pendingRequests.remove(requester);
   }
 
   public void startTrading(Player player1, Player player2)
   {
     TradeHandler tradeHandler;
     if (economyHandler != null)
       tradeHandler = new CurrencyTradeHandler(this, player1, player2);
     else {
       tradeHandler = new TradeHandler(this, player1, player2);
     }
 
     activeTrades.put(player1, tradeHandler);
     activeTrades.put(player2, tradeHandler);
     tradeHandler.startTrading();
   }
 
     private MobArenaHandler getMobArenaHandler(Plugin plugin) {
         MobArenaHandler mobArenaHandler = null;
         if (plugin != null && plugin instanceof MobArena) {
             mobArenaHandler = new MobArenaHandler();
             logger.info("Successfully hooked " + plugin.getDescription().getName());
         }
         return mobArenaHandler;
     }
 
     private Heroes getHeroesPlugin(Plugin plugin) {
         Heroes heroesPlugin = null;
         if (plugin != null && plugin instanceof Heroes) {
             heroesPlugin = (Heroes) plugin;
             logger.info("Successfully hooked " + plugin.getDescription().getName());
         }
         return heroesPlugin;
     }
     
    private Citizens getCitizensPlugin(Plugin plugin) {
        Citizens citizensPlugin = null;
         if (plugin != null && plugin instanceof Citizens) {
            citizensPlugin = (Citizens) plugin;
             logger.info("Successfully hooked " + plugin.getDescription().getName());
         }
         return citizensPlugin;
     }
 }
