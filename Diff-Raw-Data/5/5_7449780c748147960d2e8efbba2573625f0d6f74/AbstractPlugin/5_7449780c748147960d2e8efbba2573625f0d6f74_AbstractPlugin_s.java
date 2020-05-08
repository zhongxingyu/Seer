 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * SkeletonPlugin.java is part of BukkitUtilities.
  * 
  * BukkitUtilities is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * BukkitUtilities is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.plugin;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import name.richardson.james.bukkit.utilities.configuration.PluginConfiguration;
 import name.richardson.james.bukkit.utilities.localisation.Localisation;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundleLoader;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundleLocalisation;
 import name.richardson.james.bukkit.utilities.logging.ConsoleLogger;
 import name.richardson.james.bukkit.utilities.logging.Logger;
 import name.richardson.james.bukkit.utilities.permissions.BukkitPermissionManager;
 import name.richardson.james.bukkit.utilities.permissions.PermissionManager;
 import name.richardson.james.bukkit.utilities.updater.PluginUpdater;
 import name.richardson.james.bukkit.utilities.updater.PluginUpdater.State;
 
 public abstract class AbstractPlugin extends JavaPlugin implements Plugin {
 
   /* The configuration file for this plugin */
   private PluginConfiguration configuration;
 
   /* The locale of the system the plugin is running on */
   private final Locale locale = Locale.getDefault();
 
   private ResourceBundleLocalisation localisation;
 
   /* The logger that belongs to this plugin */
   private Logger logger;
 
   private PermissionManager permissions;
 
   public String getGroupID() {
     return "name.richardson.james.bukkit";
   }
 
   public Locale getLocale() {
     return this.locale;
   }
 
   public URL getRepositoryURL() {
     try {
       switch (this.configuration.getAutomaticUpdaterBranch()) {
       case DEVELOPMENT:
         return new URL("http://repository.james.richardson.name/snapshots");
       default:
         return new URL("http://repository.james.richardson.name/releases");
       }
     } catch (final MalformedURLException e) {
       return null;
     }
   }
 
   @Override
   public void onDisable() {
     this.getServer().getScheduler().cancelTasks(this);
     this.logger.info(AbstractPlugin.class, "disabled", this.getName());
   }
 
   @Override
   public final void onEnable() {
    // set the prefix of the logger for this plugin
    // all other classes attached to this plugin should use the same prefix
    this.logger.setPrefix("[" + this.getName() + "] ");
     try {
       this.setLogging();
       this.loadLocalisation();
       this.loadConfiguration();
       this.setPermissions();
       this.establishPersistence();
       this.registerCommands();
       this.registerListeners();
       this.setupMetrics();
       this.updatePlugin();
     } catch (final IOException e) {
       this.logger.severe(AbstractPlugin.class, "panic");
       e.printStackTrace();
       this.setEnabled(false);
     } catch (final SQLException e) {
       this.logger.severe(AbstractPlugin.class, "panic");
       e.printStackTrace();
       this.setEnabled(false);
     } catch (final Exception e) {
       this.logger.severe(AbstractPlugin.class, "panic");
       e.printStackTrace();
       this.setEnabled(false);
     } finally {
       if (!this.isEnabled()) {
         return;
       }
     }
   }
 
   protected void establishPersistence() throws SQLException {
     return;
   }
 
   protected void loadConfiguration() throws IOException {
     this.configuration = new PluginConfiguration(this);
     this.logger.setDebugging(this.configuration.isDebugging());
   }
 
   protected void registerCommands() {
     return;
   }
 
   protected void registerListeners() {
     return;
   }
 
   protected void setLogging() {
     this.logger = new ConsoleLogger(this);
   }
 
   protected void setPermissions() {
     this.permissions = new BukkitPermissionManager(this);
     final String node = this.getDescription().getName().toLowerCase() + ".*";
     final String description = this.localisation.getMessage(AbstractPlugin.class, "permission-description", this.getDescription().getName());
     final Permission permission = new Permission(node, description, PermissionDefault.OP);
     this.permissions.addPermission(permission, false);
     this.permissions.setRootPermission(permission);
   }
 
   protected void setupMetrics() throws IOException {
     return;
   }
 
   private void loadLocalisation() throws IOException {
     final ResourceBundle[] bundles = { ResourceBundleLoader.getBundle("bukkitutilities-localisation"), ResourceBundleLoader.getBundle(this.getName().toLowerCase(), this.getDataFolder()) };
     this.localisation = new ResourceBundleLocalisation(bundles);
   }
 
   private void updatePlugin() {
     if (this.configuration.getAutomaticUpdaterState() != State.OFF) {
       new PluginUpdater(this, this.configuration.getAutomaticUpdaterState(), this.configuration.getAutomaticUpdaterBranch());
     }
   }
   
   public PermissionManager getPermissionManager() {
     return this.permissions;
   }
   
   public Localisation getLocalisation() {
     return this.localisation;
   }
   
   public Logger getCustomLogger() {
     return this.logger;
   }
 
 }
