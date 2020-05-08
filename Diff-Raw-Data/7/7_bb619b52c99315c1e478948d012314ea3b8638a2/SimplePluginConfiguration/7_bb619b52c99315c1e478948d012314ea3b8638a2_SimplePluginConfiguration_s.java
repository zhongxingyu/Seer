 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  SimplePluginConfiguration.java is part of BukkitUtilities.
 
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
 
 import name.richardson.james.bukkit.utilities.logging.PluginLoggerFactory;
 import name.richardson.james.bukkit.utilities.updater.MavenPluginUpdater;
 import name.richardson.james.bukkit.utilities.updater.PluginUpdater.Branch;
 import name.richardson.james.bukkit.utilities.updater.PluginUpdater.State;
 
 /**
  * Represents a basic plugin configuration. This is designed to be extended to create custom plugin configuration.
  */
 public class SimplePluginConfiguration extends AbstractConfiguration implements PluginConfiguration {
 
 	private static final String BRANCH_KEY = "automatic-updates.branch";
 	private static final String UPDATER_STATE_KEY = "automatic-updates.method";
 	private static final String LOGGING_KEY = "logging";
 	private static final String STATISTICS_KEY = "send-anonymous-statistics";
 
 	private static final String INVALID_CONFIGURATION_VALUE_KEY = "invalid-configuration-value";
 
 	private final Logger logger = PluginLoggerFactory.getLogger(this.getClass());
 
 	public SimplePluginConfiguration(final File file, final InputStream defaults) throws IOException {
 		super(file, defaults, true);
 	}
 
 	public final Branch getAutomaticUpdaterBranch() {
			final MavenPluginUpdater.Branch defaultBranch = MavenPluginUpdater.Branch.STABLE;
 			try {
 				return MavenPluginUpdater.Branch.valueOf(this.getConfiguration().getString(BRANCH_KEY));
 			} catch (final IllegalArgumentException e) {
 				getLogger().log(Level.WARNING, INVALID_CONFIGURATION_VALUE_KEY, BRANCH_KEY);
 				this.getConfiguration().set(BRANCH_KEY, defaultBranch.name());
 				return defaultBranch;
 			}
 		}
 
 	public final State getAutomaticUpdaterState() {
		final MavenPluginUpdater.State defaultState = MavenPluginUpdater.State.NOTIFY;
 		try {
 			return MavenPluginUpdater.State.valueOf(this.getConfiguration().getString(UPDATER_STATE_KEY));
 		} catch (final IllegalArgumentException e) {
 			getLogger().log(Level.WARNING, INVALID_CONFIGURATION_VALUE_KEY, UPDATER_STATE_KEY);
 			this.getConfiguration().set(UPDATER_STATE_KEY, defaultState.name());
 			return defaultState;
 		}
 	}
 
 	public final Level getLogLevel() {
 		final Level defaultLevel = Level.INFO;
 		try {
 			return Level.parse(this.getConfiguration().getString(LOGGING_KEY));
 		} catch (final IllegalArgumentException e) {
 			getLogger().log(Level.WARNING, INVALID_CONFIGURATION_VALUE_KEY, LOGGING_KEY);
 			this.getConfiguration().set(LOGGING_KEY, defaultLevel.getName());
 			return defaultLevel;
 		}
 	}
 
 	public final boolean isCollectingStats() {
 		final boolean defaultValue = true;
 		return this.getConfiguration().getBoolean(STATISTICS_KEY, defaultValue);
 	}
 
 	protected final Logger getLogger() {
 		return logger;
 	}
 
 }
