 package edu.umn.acm.minecraft.ldap;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Author: bunny
  * Date: 2/28/12
  * Time: 3:29 PM
  */
 public class MinecraftLDAP extends JavaPlugin {
     private AuthenticationSource authenticationSource;
     private PlayerListener playerListener;
 
     public MinecraftLDAP() {
        LdapAuthenticationSource ldapAuthenticationSource = new LdapAuthenticationSource();
        FileConfiguration config = this.getConfig();
        ldapAuthenticationSource.setUrl(config.getString("ldap.url"));
        ldapAuthenticationSource.setBaseLdapPath(config.getString("ldap.base"));
        ldapAuthenticationSource.setUserAccountObjectClass(config.getString("ldap.user_class"));
        ldapAuthenticationSource.setUserIdFieldName(config.getString("ldap.uid_field"));
        if (config.contains("ldap.fullname_field")) {
            ldapAuthenticationSource.setUserFullnameFieldName(config.getString("ldap.fullname_field"));
        }
        this.authenticationSource = ldapAuthenticationSource;
         this.playerListener = new PlayerListener();
     }
 
     @Override
     public void onEnable() {
        //TODO: Update auth source
         this.playerListener.setServer(this.getServer());
         this.playerListener.setAuthenticationSource(this.authenticationSource);
 
         PluginManager pluginManager = this.getServer().getPluginManager();
         pluginManager.registerEvents(this.playerListener, this);
     }
 }
