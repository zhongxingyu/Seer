 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * SQLStorage.java is part of BukkitUtilities.
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
 package name.richardson.james.bukkit.utilities.persistence;
 
 import java.io.BufferedReader;
 import java.io.StringReader;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.EbeanServerFactory;
 import com.avaje.ebean.LogLevel;
 import com.avaje.ebean.config.DataSourceConfig;
 import com.avaje.ebean.config.ServerConfig;
 import com.avaje.ebeaninternal.api.SpiEbeanServer;
 import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
 
 import name.richardson.james.bukkit.utilities.configuration.SimpleDatabaseConfiguration;
 import name.richardson.james.bukkit.utilities.localisation.Localised;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundles;
 import name.richardson.james.bukkit.utilities.logging.PluginLogger;
 
 public class SQLStorage implements Localised {
 
 	private final List<Class<?>> classes;
 	private final ClassLoader classLoader;
 	private final DataSourceConfig datasourceConfig;
 	private EbeanServer ebeanserver;
 	private DdlGenerator generator;
 	private boolean rebuild;
 	private final ServerConfig serverConfig;
 
 	private static final Logger logger = PluginLogger.getLogger(SQLStorage.class);
 	private static final ResourceBundle localisation = ResourceBundle.getBundle(ResourceBundles.MESSAGES.getBundleName());
 
 	public SQLStorage(final SimpleDatabaseConfiguration configuration, final List<Class<?>> classes, final String pluginName, final ClassLoader classLoader) {
 		this.classes = classes;
 		this.serverConfig = configuration.getServerConfig();
 		this.serverConfig.setName(pluginName);
 		this.datasourceConfig = configuration.getDataSourceConfig();
 		this.classLoader = classLoader;
 	}
 
 	public List<Class<?>> getClasses() {
 		return this.classes;
 	}
 
 	public EbeanServer getEbeanServer() {
 		return this.ebeanserver;
 	}
 
 	public String getMessage(final String key) {
 		final String message = SQLStorage.localisation.getString(key);
 		return message;
 	}
 
 	public String getMessage(final String key, final Object... elements) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getMessage(final String key, final String... elements) {
 		final MessageFormat formatter = new MessageFormat(SQLStorage.localisation.getString(key));
 		formatter.setLocale(Locale.getDefault());
 		final String message = formatter.format(elements);
 		return message;
 	}
 
 	public void initalise() {
 		if (this.ebeanserver != null) {
			SQLStorage.logger.log(Level.WARNING, this.getMessage("warning.sqlstorage.already-initalised"));
 		}
 		this.load();
 		if (!this.validate() || this.rebuild) {
 			final SpiEbeanServer server = (SpiEbeanServer) this.ebeanserver;
 			this.generator = server.getDdlGenerator();
 			this.drop();
 			this.create();
			SQLStorage.logger.log(Level.INFO, this.getMessage("notice.sqlstorage.rebuilt-schema"));
 		}
 	}
 
 	protected void afterDatabaseCreate() {
 		// TODO Auto-generated method stub
 	}
 
 	protected void beforeDatabaseCreate() {
 		// TODO Auto-generated method stub
 	}
 
 	protected void beforeDatabaseDrop() {
 		// TODO Auto-generated method stub
 	}
 
 	protected void create() {
		SQLStorage.logger.log(Level.INFO, this.getMessage("notice.sqlstorage.creating-database"));
 		this.beforeDatabaseCreate();
 		// reload the database this allows for removing classes
 		String script = this.generator.generateCreateDdl();
 		final Level level = java.util.logging.Logger.getLogger("").getLevel();
 		if (this.datasourceConfig.getDriver().contains("sqlite")) {
 			script = this.fixScript(script);
 		}
 		try {
 			java.util.logging.Logger.getLogger("").setLevel(Level.OFF);
 			this.load();
 			this.generator.runScript(false, script);
 		} finally {
 			java.util.logging.Logger.getLogger("").setLevel(level);
 		}
 		this.afterDatabaseCreate();
 	}
 
 	private void drop() {
 		SQLStorage.logger.log(Level.FINE, "Dropping and destroying database.");
 		this.beforeDatabaseDrop();
 		final Level level = java.util.logging.Logger.getLogger("").getLevel();
 		try {
 			java.util.logging.Logger.getLogger("").setLevel(Level.OFF);
 			this.generator.runScript(true, this.generator.generateDropDdl());
 		} finally {
 			java.util.logging.Logger.getLogger("").setLevel(level);
 		}
 	}
 
 	private String fixScript(final String script) {
 		SQLStorage.logger.log(Level.FINE, "Fixing DDL script for SQLite databases.");
 		// Create a BufferedReader out of the potentially invalid script
 		final BufferedReader scriptReader = new BufferedReader(new StringReader(script));
 		// Create an array to store all the lines
 		final List<String> scriptLines = new ArrayList<String>();
 		// Create some additional variables for keeping track of tables
 		final HashMap<String, Integer> foundTables = new HashMap<String, Integer>();
 		// The name of the table we are currently reviewing
 		String currentTable = null;
 		int tableOffset = 0;
 		try {
 			// Loop through all lines
 			String currentLine;
 			while ((currentLine = scriptReader.readLine()) != null) {
 				// Trim the current line to remove trailing spaces
 				currentLine = currentLine.trim();
 				// Add the current line to the rest of the lines
 				scriptLines.add(currentLine.trim());
 				// Check if the current line is of any use
 				if (currentLine.startsWith("create table")) {
 					// Found a table, so get its name and remember the line it has been
 					// encountered on
 					currentTable = currentLine.split(" ", 4)[2];
 					foundTables.put(currentLine.split(" ", 3)[2], scriptLines.size() - 1);
 				} else
 					if (currentLine.startsWith(";") && (currentTable != null) && !currentTable.equals("")) {
 						// Found the end of a table definition, so update the entry
 						final int index = scriptLines.size() - 1;
 						foundTables.put(currentTable, index);
 						// Remove the last ")" from the previous line
 						String previousLine = scriptLines.get(index - 1);
 						previousLine = previousLine.substring(0, previousLine.length() - 1);
 						scriptLines.set(index - 1, previousLine);
 						// Change ";" to ");" on the current line
 						scriptLines.set(index, ");");
 						// Reset the table-tracker
 						currentTable = null;
 						// Found a potentially unsupported action
 					} else
 						if (currentLine.startsWith("alter table")) {
 							final String[] alterTableLine = currentLine.split(" ", 4);
 							if (alterTableLine[3].startsWith("add constraint")) {
 								// Found an unsupported action: ALTER TABLE using ADD CONSTRAINT
 								final String[] addConstraintLine = alterTableLine[3].split(" ", 4);
 								// Check if this line can be fixed somehow
 								if (addConstraintLine[3].startsWith("foreign key")) {
 									// Calculate the index of last line of the current table
 									final int tableLastLine = foundTables.get(alterTableLine[2]) + tableOffset;
 									// Add a "," to the previous line
 									scriptLines.set(tableLastLine - 1, scriptLines.get(tableLastLine - 1) + ",");
 									// Add the constraint as a new line - Remove the ";" on the
 									// end
 									final String constraintLine = String.format("%s %s %s", addConstraintLine[1], addConstraintLine[2], addConstraintLine[3]);
 									scriptLines.add(tableLastLine, constraintLine.substring(0, constraintLine.length() - 1));
 									// Remove this line and raise the table offset because a line
 									// has
 									// been inserted.
 									scriptLines.remove(scriptLines.size() - 1);
 									tableOffset++;
 								} else {
 									// Exception: This line cannot be fixed but is known the be
 									// unsupported by SQLite.
 									throw new RuntimeException("Unsupported action encountered: ALTER TABLE using ADD CONSTRAINT with " + addConstraintLine[3]);
 								}
 							}
 						}
 			}
 		} catch (final Exception exception) {
 			throw new RuntimeException("Failed to valid the CreateDDL script!");
 		}
 		// Turn all the lines back into a single string
 		String newScript = "";
 		for (final String newLine : scriptLines) {
 			newScript += newLine + "\n";
 		}
 		// Print the new script
 		// System.out.println(newScript);
 		// Return the fixed script
 		return newScript;
 	}
 
 	private void load() {
 		SQLStorage.logger.log(Level.FINE, "Loading database.");
 		final Level level = java.util.logging.Logger.getLogger("").getLevel();
 		ClassLoader currentClassLoader = null;
 		try {
 			this.serverConfig.setClasses(this.classes);
 			if (SQLStorage.logger.isLoggable(Level.ALL)) {
 				this.serverConfig.setLoggingToJavaLogger(true);
 				this.serverConfig.setLoggingLevel(LogLevel.SQL);
 			}
 			// suppress normal ebean warnings and notifications
 			java.util.logging.Logger.getLogger("").setLevel(Level.OFF);
 			currentClassLoader = Thread.currentThread().getContextClassLoader();
 			Thread.currentThread().setContextClassLoader(this.classLoader);
 			this.ebeanserver = EbeanServerFactory.create(this.serverConfig);
 		} finally {
 			java.util.logging.Logger.getLogger("").setLevel(level);
 			if (currentClassLoader != null) {
 				Thread.currentThread().setContextClassLoader(currentClassLoader);
 			}
 		}
 	}
 
 	private boolean validate() {
 		for (final Class<?> ebean : this.classes) {
 			try {
 				this.ebeanserver.find(ebean).findRowCount();
 			} catch (final Exception exception) {
				SQLStorage.logger.log(Level.WARNING, this.getMessage("warning.sqlstorage.schema-invalid"));
 				return false;
 			}
 		}
 		SQLStorage.logger.log(Level.FINE, "Database schema is valid.");
 		return true;
 	}
 
 }
