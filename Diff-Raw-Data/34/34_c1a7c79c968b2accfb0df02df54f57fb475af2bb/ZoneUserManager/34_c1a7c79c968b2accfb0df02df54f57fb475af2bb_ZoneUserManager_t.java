 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package info.jeppes.ZoneCore.Users;
 
 import info.jeppes.ZoneCore.Events.NewZoneUserEvent;
 import info.jeppes.ZoneCore.ZoneConfig;
 import info.jeppes.ZoneCore.ZoneCore;
 import info.jeppes.ZoneCore.ZonePlugin;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 
 /**
  *
  * @author Jeppe
  * @param <E>
  */
 public class ZoneUserManager<E extends ZoneUser> implements Listener{
     private final HashMap<String,WeakReference<E>> users = new HashMap();
    //Really messy fix for a problem...
    private final HashMap<String,String> userNames = new HashMap();
     private final Plugin plugin;
     private final boolean isZonePlugin;
     //This SoftReference most likely wont work due to ZoneUserData holding a ConfigurationSection object
     //of the config, need to look into that later.
     private ZoneConfig usersConfigReference;
     private final String usersConfigFilePath;
     private final int saveInterval = 24000; //ticks
     
     public ZoneUserManager(Plugin plugin, ZoneConfig usersConfig){
         this.usersConfigReference = usersConfig;
         this.usersConfigFilePath = usersConfig.getFile().getPath();
         this.plugin = plugin;
         if(plugin instanceof  ZonePlugin){
             this.isZonePlugin = true;
         } else {
             this.isZonePlugin = false;
         }
         
         loadUsers(usersConfig);
         
         Bukkit.getPluginManager().registerEvents(this, plugin);
         
         Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){
             @Override
             public void run() {
                 getUsersConfig().schedualSave();
             }
         }, saveInterval, saveInterval);
     }
     
     public HashMap<String,WeakReference<E>> getUsers(){
         return users;
     }
     
     public E getUser(Player player){
         return getUser(player.getName());
     }
     public E getUser(String userName){
         HashMap<String, WeakReference<E>> usersTemp = getUsers();
         String key = userName.toLowerCase();
         if(usersTemp.containsKey(key)){
             WeakReference<E> reference = usersTemp.get(key);
             if(reference != null){
                 E user = reference.get();
                 if(user == null){
                    user = this.loadUser(userNames.get(key));
                     reference = new WeakReference(user);
                     getUsers().put(key, reference);
                 }
                 return user;
             }
         }
         return null;
     }
     
     public boolean containsPlayer(Player player){
         return containsPlayer(player);
     }
     public boolean containsPlayer(String playerName){
         return getUsers().containsKey(playerName.toLowerCase());
     }
     
     public E createNewZoneUser(Player player){
         return createNewZoneUser(player.getName(),true);
     }
     public E createNewZoneUser(String playerName){
         return createNewZoneUser(playerName,true);
     }
     public E createNewZoneUser(Player player, boolean addToUserList){
         return createNewZoneUser(player.getName(),addToUserList);
     }
     public E createNewZoneUser(String playerName, boolean addToUserList){
         Player player = Bukkit.getPlayer(playerName);
         E newUser;
         if(player != null){
             newUser = loadUser(Bukkit.getPlayer(playerName), getUsersConfig().createSection(playerName));
         } else {
             newUser = loadUser(playerName, getUsersConfig().createSection(playerName));
         }
         if(addToUserList){
            addUserToUserList(newUser,true);
         }
         return newUser;
     }
    public boolean addUserToUserList(E user, boolean isNew){
         if(!containsPlayer(user.getName())){
            String key = user.getName().toLowerCase();
            getUsers().put(key, new WeakReference(user));
            userNames.put(key, user.getName());
            if(isNew){
                getUsersConfig().schedualSave();
 
                NewZoneUserEvent newZoneUserEvent = new NewZoneUserEvent(this,user);
                Bukkit.getPluginManager().callEvent(newZoneUserEvent);
            }
             return true;
         }
         return false;
     }
     
     public ZoneConfig getUsersConfig(){
 //        if(usersConfigReference != null){
 //            ZoneConfig config = usersConfigReference.get();
 //            if(config == null){
 //                config = new ZoneConfig(plugin, new File(usersConfigFilePath));
 //                usersConfigReference = new SoftReference(config);
 //            }
 //            return config;
 //        }
 //        return null;
         return usersConfigReference;
     }
     
     public void loadUsers(ZoneConfig usersConfig){
         if(isZonePlugin){
             if(((ZonePlugin)plugin).inDebugMode()) {
                 ((ZonePlugin)plugin).getLogger().log(Level.INFO,"Loading users...");
             }
         }
         Set<String> keys = usersConfig.getKeys(false);
         for(String userName : keys){
             E user = loadUser(userName);
            this.addUserToUserList(user, false);
         }
         
         if(isZonePlugin){
             if(((ZonePlugin)plugin).inDebugMode()) {
                 ((ZonePlugin)plugin).getLogger().log(Level.INFO,"Loaded users");
             }
         }
     }
     public E loadUser(Player player){
         return loadUser(player.getName());
     }
     public E loadUser(String userName){
         ConfigurationSection config = getUsersConfig().getConfigurationSection(userName);
         if(config == null){
             config = getUsersConfig().createSection(userName);
         }
         return loadUser(userName,config);
     }
     public E loadUser(Player player, ConfigurationSection config){
         return loadUser(player.getName(),config);
     }
     public E loadUser(String userName, ConfigurationSection config){
         return (E)new ZoneUserData(userName,config);
     }
     
     public boolean checkNewUser(Player player) {
        if(!containsPlayer(player.getName().toLowerCase())){
             E newUser = createNewZoneUser(player, true);
             return true;
         }
         return false;
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerJoin(PlayerJoinEvent event){
         checkNewUser(event.getPlayer());
         final ZoneUser user = getUser(event.getPlayer());
         user.setPlayer(event.getPlayer());
         
         Bukkit.getScheduler().runTaskLater(ZoneCore.getCorePlugin(), new Runnable(){
             @Override
             public void run() {
                 user.sendMesssagesWhenOnline();
                 user.giveItemsWhenOnline();
                 user.giveLevelsWhenOnline();
             }
         }, 20);
     }
     
     @EventHandler(priority=EventPriority.NORMAL)
     public void onPlayerQuit(PlayerQuitEvent event){
         ZoneUser user = getUser(event.getPlayer());
         if(user != null){
             user.saveConfig();
         }
     }
 }
