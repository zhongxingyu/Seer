 package edgruberman.bukkit.simplelocks;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.HandlerList;
 
 import edgruberman.bukkit.simplelocks.commands.LockBreak;
 import edgruberman.bukkit.simplelocks.commands.LockDescribe;
 import edgruberman.bukkit.simplelocks.commands.LockGrant;
 import edgruberman.bukkit.simplelocks.commands.LockRevoke;
 import edgruberman.bukkit.simplelocks.commands.Reload;
 import edgruberman.bukkit.simplelocks.messaging.ConfigurationCourier;
 import edgruberman.bukkit.simplelocks.messaging.Courier;
 import edgruberman.bukkit.simplelocks.util.CustomPlugin;
 
 public class Main extends CustomPlugin {
 
     public static Courier courier;
 
     @Override
    public void onLoad() { this.putConfigMinimum("config.yml", "3.0.0a4"); }
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         Main.courier = ConfigurationCourier.Factory.create(this).setBase("messages").build();
 
         final String title = this.getConfig().getString("title");
         this.getLogger().config("Lock title: " + title);
         if (title.length() < 1 || title.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH)
             throw new IllegalArgumentException("Lock title must be between 1 and " + Locksmith.MAXIMUM_SIGN_LINE_LENGTH + " characters");
 
         final Locksmith locksmith = new Locksmith(this, title);
         final ConfigurationSection substitutions = this.getConfig().getConfigurationSection("substitutions");
         if (substitutions != null)
             for (final String name : substitutions.getKeys(false))
                 locksmith.substitutions.put(name, substitutions.getString(name));
 
         if (this.getConfig().getBoolean("explosionProtection")) new ExplosiveOrdnanceDisposal(this, locksmith);
 
         this.getCommand("simplelocks:lock.describe").setExecutor(new LockDescribe(locksmith));
         this.getCommand("simplelocks:lock.grant").setExecutor(new LockGrant(locksmith));
         this.getCommand("simplelocks:lock.revoke").setExecutor(new LockRevoke(locksmith));
         this.getCommand("simplelocks:lock.break").setExecutor(new LockBreak(locksmith));
         this.getCommand("simplelocks:reload").setExecutor(new Reload(this));
     }
 
     @Override
     public void onDisable() {
         HandlerList.unregisterAll(this);
         Main.courier = null;
     }
 
 }
