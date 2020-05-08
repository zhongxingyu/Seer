 package edu.umn.acm.minecraft.ldap;
 
 import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
import java.util.HashMap;
import java.util.Map;

 /**
  * Author: bunny
  * Date: 2/28/12
  * Time: 3:29 PM
  */
 public class MinecraftLDAP extends JavaPlugin {
     private AuthenticationSource authenticationSource;
     private PlayerListener playerListener;
 
     public MinecraftLDAP() {
         this.playerListener = new PlayerListener();
     }
 
     @Override
     public void onEnable() {
        this.configureAuthSource();
         this.playerListener.setServer(this.getServer());
         this.playerListener.setAuthenticationSource(this.authenticationSource);
 
         PluginManager pluginManager = this.getServer().getPluginManager();
         pluginManager.registerEvents(this.playerListener, this);
     }

    private void configureAuthSource() {
        FileConfiguration config = this.getConfig();
        LdapAuthenticationSource ldapAuthenticationSource = new LdapAuthenticationSource();
        ldapAuthenticationSource.setUrl(config.getString("ldap.url"));
        ldapAuthenticationSource.setBaseLdapPath(config.getString("ldap.base"));
        ldapAuthenticationSource.setUserAccountObjectClass(config.getString("ldap.user_class"));
        ldapAuthenticationSource.setUserIdFieldName(config.getString("ldap.uid_field"));
        if (config.contains("ldap.fullname_field")) {
            ldapAuthenticationSource.setUserFullnameFieldName(config.getString("ldap.fullname_field"));
        }
        this.authenticationSource = ldapAuthenticationSource;
    }
 }
