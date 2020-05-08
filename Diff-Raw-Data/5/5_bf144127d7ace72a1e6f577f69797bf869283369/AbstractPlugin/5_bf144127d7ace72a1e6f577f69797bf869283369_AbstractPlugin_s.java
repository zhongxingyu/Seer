 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * AbstractPlugin.java is part of BukkitUtilities.
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
 import name.richardson.james.bukkit.utilities.metrics.MetricsListener;
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
 
   public Logger getCustomLogger() {
     return this.logger;
   }
 
   public String getGroupID() {
     return "name.richardson.james.bukkit";
   }
 
   public Locale getLocale() {
     return this.locale;
   }
 
   public Localisation getLocalisation() {
     return this.localisation;
   }
 
   public PermissionManager getPermissionManager() {
     return this.permissions;
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
   }
 
   @Override
   public final void onEnable() {
     try {
       this.loadLocalisation();
       this.setLogging();
       this.setPermissions();
       this.loadConfiguration();
       this.establishPersistence();
       this.registerCommands();
       this.registerListeners();
       this.setupMetrics();
       this.updatePlugin();
     } catch (final IOException e) {
       this.logger.severe("panic");
       e.printStackTrace();
       this.setEnabled(false);
     } catch (final SQLException e) {
       this.logger.severe("panic");
       e.printStackTrace();
       this.setEnabled(false);
     } catch (final Exception e) {
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
     this.logger = new ConsoleLogger(this.getLogger());
     this.logger.setPrefix("[" + this.getName() + "] ");
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
     new MetricsListener(this);
   }
 
   private void loadLocalisation() throws IOException {
     final ResourceBundle[] bundles = { 
         ResourceBundleLoader.getBundle(this.getClassLoader(), "bukkitutilities"), 
         ResourceBundleLoader.getBundle(this.getClassLoader(), this.getName().toLowerCase(), this.getDataFolder()) 
     };
     this.localisation = new ResourceBundleLocalisation(bundles);
   }
 
   private void updatePlugin() {
     if (this.configuration.getAutomaticUpdaterState() != State.OFF) {
       new PluginUpdater(this, this.configuration.getAutomaticUpdaterState(), this.configuration.getAutomaticUpdaterBranch());
     }
   }
 
 }
