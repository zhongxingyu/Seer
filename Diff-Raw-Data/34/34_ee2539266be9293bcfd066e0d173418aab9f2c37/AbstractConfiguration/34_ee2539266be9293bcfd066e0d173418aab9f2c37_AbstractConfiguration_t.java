 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  AbstractConfiguration.java is part of BukkitUtilities.
 
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
 
 package name.richardson.james.bukkit.utilities.persistence.configuration;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import org.apache.commons.lang.Validate;
 
import name.richardson.james.bukkit.utilities.localisation.Localisation;
import name.richardson.james.bukkit.utilities.localisation.ResourceBundleByClassLocalisation;
 import name.richardson.james.bukkit.utilities.logging.PluginLoggerFactory;
 
 /**
  * AbstractConfiguration is responsible for creating YAML configuration files, setting defaults from a provided {@link InputStream} and handling any exceptions
  * when the file is saved.
  */
 public abstract class AbstractConfiguration {
 
 	private final YamlConfiguration defaults;
 	private final File file;
 	private final Logger logger = PluginLoggerFactory.getLogger(AbstractConfiguration.class);
	private final Localisation localisation = new ResourceBundleByClassLocalisation(AbstractConfiguration.class);
 	private final boolean runtimeDefaults;
 	private YamlConfiguration configuration;
 
 	/**
 	 * Construct an AbstractConfiguration
 	 *
 	 * @param file               the file where this configuration is stored
 	 * @param defaults           the defaults that should be used for this configuration
 	 * @param useRuntimeDefaults {@code true} if the defaults should be applied if the value is missing in the configuration; {@code false} otherwise.
 	 * @throws IOException
 	 */
 	public AbstractConfiguration(final File file, final InputStream defaults, boolean useRuntimeDefaults)
 	throws IOException {
 		Validate.notNull(file, "File can not be null!");
 		Validate.notNull(file, "Defaults can not be null!");
 		this.file = file;
 		this.defaults = YamlConfiguration.loadConfiguration(defaults);
 		this.runtimeDefaults = useRuntimeDefaults;
 		this.load();
 	}
 
 	protected final YamlConfiguration getConfiguration() {
 		return this.configuration;
 	}
 
 	protected final void load()
 	throws IOException {
 		logger.log(Level.CONFIG, "Loading configuration: " + this.getClass().getSimpleName());
 		logger.log(Level.CONFIG, "Using path: " + this.file.getAbsolutePath());
 		if (!this.file.exists() || this.file.length() == 0) {
 			this.defaults.options().copyHeader(true);
 			this.defaults.options().copyDefaults(true);
			logger.log(Level.WARNING, localisation.getMessage("saving-default-configuration", this.file.getName()));
 			defaults.save(this.file);
 		}
 		this.configuration = YamlConfiguration.loadConfiguration(this.file);
 		if (runtimeDefaults) this.configuration.setDefaults(this.defaults);
 		this.configuration.options().copyDefaults(false);
 	}
 
 	protected final void save()
 	throws IOException {
 		logger.log(Level.CONFIG, "Saving configuration: " + this.file.getName());
 		this.configuration.save(this.file);
 	}
 }
