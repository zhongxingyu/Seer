 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  SimpleDatabaseConfiguration.java is part of BukkitUtilities.
 
  BukkitUtilities is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option) any
  later version.
 
  BukkitUtilities is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along with
  BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.persistence.database;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.avaje.ebean.config.DataSourceConfig;
 import com.avaje.ebean.config.ServerConfig;
 import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;
 
 import name.richardson.james.bukkit.utilities.localisation.Localisation;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundleByClassLocalisation;
 import name.richardson.james.bukkit.utilities.logging.PluginLoggerFactory;
 import name.richardson.james.bukkit.utilities.persistence.configuration.AbstractConfiguration;
 
 public final class SimpleDatabaseConfiguration extends AbstractConfiguration implements DatabaseConfiguration {
 
 	private static final String USERNAME_KEY = "username";
 	private static final String PASSWORD_KEY = "password";
 	private static final String DRIVER_KEY = "driver";
 	private static final String ISOLATION_KEY = "isolation";
 	private static final String URL_KEY = "url";
 
 	private final DataSourceConfig dataSourceConfig;
 	private final File folder;
 	private final Logger logger = PluginLoggerFactory.getLogger(this.getClass());
 	private final String pluginName;
 	private final ServerConfig serverConfig;
 	private final Localisation localisation = new ResourceBundleByClassLocalisation(SimpleDatabaseConfiguration.class);
 
 	public SimpleDatabaseConfiguration(final File file, final InputStream defaults, final String pluginName, final ServerConfig serverConfig)
 	throws IOException {
 		super(file, defaults, false);
 		this.folder = file.getParentFile();
 		this.serverConfig = serverConfig;
 		this.dataSourceConfig = serverConfig.getDataSourceConfig();
 		this.pluginName = pluginName;
 		setDefaults();
 		setUserName();
 		setPassword();
 		setDriver();
 		setIsolation();
 		setUrl();
 	}
 
 	public DataSourceConfig getDataSourceConfig() {
 		return this.dataSourceConfig;
 	}
 
 	public ServerConfig getServerConfig() {
 		return this.serverConfig;
 	}
 
 	@Override
 	public String toString() {
 		return "SimpleDatabaseConfiguration {" +
 		"dataSourceConfig=" + dataSourceConfig.toString() +
 		", serverConfig=" + serverConfig.toString() +
 		", folder=" + folder +
 		", pluginName='" + pluginName + '\'' +
 		", username='" + this.dataSourceConfig.getUsername() + '\'' +
 		", password='" + maskString(this.dataSourceConfig.getPassword()) + '\'' +
 		", driver='" + this.dataSourceConfig.getDriver() + '\'' +
 		", isolation='" + this.dataSourceConfig.getIsolationLevel() + '\'' +
 		", url='" + this.dataSourceConfig.getUrl() + '\'' +
 		'}';
 	}
 
 	private String replaceDatabaseString(String input) {
		input = input.replaceAll("\\{DIR\\}", this.folder.getAbsolutePath() + File.separator);
 		input = input.replaceAll("\\{NAME\\}", this.pluginName.replaceAll("[^\\w_-]", ""));
 		return input;
 	}
 
 	private void setDefaults() {
 		this.serverConfig.setDefaultServer(false);
 		this.serverConfig.setRegister(false);
 		this.serverConfig.setName(pluginName);
 	}
 
 	private void setDriver() {
 		final String driver = this.getConfiguration().getString(DRIVER_KEY);
 		if (driver != null) {
 			logger.log(Level.CONFIG, localisation.getMessage("override-value", DRIVER_KEY, driver));
 			this.dataSourceConfig.setDriver(driver);
 		}
 	}
 
 	private void setIsolation() {
 		try {
 			String isolation = this.getConfiguration().getString("isolation");
 			if (isolation != null) {
 				logger.log(Level.CONFIG, localisation.getMessage("override-value", ISOLATION_KEY, isolation));
 				this.dataSourceConfig.setIsolationLevel(TransactionIsolation.getLevel(isolation));
 			}
 		} catch (RuntimeException e) {
 			logger.log(Level.WARNING, localisation.getMessage("transaction-level-invalid"));
 		}
 	}
 
 	private void setPassword() {
 		final String password = this.getConfiguration().getString(PASSWORD_KEY);
 		if (password != null) {
 			logger.log(Level.CONFIG, localisation.getMessage("override-value", PASSWORD_KEY, maskString(password)));
 			this.dataSourceConfig.setPassword(password);
 		}
 	}
 
 	private void setUrl() {
 		final String url = this.getConfiguration().getString("url");
 		if (url != null) {
 			logger.log(Level.CONFIG, localisation.getMessage("override-value", URL_KEY, url));
 			this.dataSourceConfig.setUrl(replaceDatabaseString(url));
 		} else {
 			this.dataSourceConfig.setUrl(replaceDatabaseString(dataSourceConfig.getUrl()));
 		}
 	}
 
 	private void setUserName() {
 		final String username = this.getConfiguration().getString(USERNAME_KEY);
 		if (username != null) {
 			logger.log(Level.CONFIG, localisation.getMessage("override-value", USERNAME_KEY, username));
 			this.dataSourceConfig.setUsername(username);
 		}
 	}
 
 	private String maskString(String string) {
 		return string.replaceAll(".", "*");
 	}
 
 }
