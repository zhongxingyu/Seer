 package spaceshooters.util.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.StandardCopyOption;
 import java.util.Properties;
 
 import spaceshooters.gui.font.Fonts;
 import spaceshooters.main.Spaceshooters;
 import spaceshooters.util.Logger;
 
 /**
  * Standard Spaceshooters 2 configuration implementation, whoih uses {@link Options} and {@link IntOptions}.
  * 
  * @author Mat
  * 
  */
 public final class Configuration {
 	
 	private static Configuration config = new Configuration();
 	private final Properties props;
 	private final String path;
 	
 	private Configuration() {
 		props = new Properties();
 		path = Spaceshooters.getPath() + "config.cfg";
 		try {
 			this.load();
 		} catch (IOException e) {
 			this.copy();
 			try {
 				this.load();
 			} catch (IOException e1) {
 				Logger.error(e1, "I cannot load configuration!");
 			}
 		}
 	}
 	
 	/**
 	 * Grabs a boolean value from the config.
 	 * 
 	 * @param key
 	 *            The key from {@link Options}.
 	 * @return The value of given key in the config.
 	 */
 	public boolean getBoolean(Options key) {
 		try {
 			int i = Integer.parseInt(props.getProperty(key.toString()));
 			if (i == 0) {
 				return false;
 			} else if (i == 1) {
 				return true;
 			} else {
 				throw new NumberFormatException();
 			}
 		} catch (NumberFormatException e) {
 			Logger.error("The key " + key + " cannot be found or has an invalid value!");
 			Logger.error("Defaulting to " + key.getDefaultValue() + " !");
 			this.setBoolean(key, key.getDefaultValue());
 			return true;
 		}
 	}
 	
 	/**
 	 * Grabs an Integer value from the config.
 	 * 
 	 * @param key
 	 *            The key from {@link IntOptions}
 	 * @return The value of given key in the config.
 	 */
 	public int getInt(IntOptions key) {
 		try {
 			return Integer.parseInt(props.getProperty(key.toString()));
 		} catch (NumberFormatException e) {
 			Logger.error("The key " + key + " cannot be found!");
 			Logger.error("Defaulting to " + key.getDefaultValue() + "!");
 			this.setInt(key, key.getDefaultValue());
 			return key.getDefaultValue();
 		}
 	}
 	
 	/**
 	 * Grabs a String value from the config.
 	 * 
 	 * @param key
 	 *            The key from {@link StringOptions}
 	 * @return
 	 */
 	public String getString(StringOptions key) {
 		return props.getProperty(key.toString());
 	}
 	
 	/**
 	 * Sets a boolean value.
 	 * 
 	 * @param key
 	 *            The key from {@link Options}.
 	 * @param value
 	 *            True or false.
 	 */
 	public void setBoolean(Options key, boolean value) {
 		String configValue;
 		if (Boolean.valueOf(value)) {
 			configValue = "1";
 		} else {
 			configValue = "0";
 		}
 		props.setProperty(key.toString(), configValue);
 		this.save();
 	}
 	
 	/**
 	 * Sets an Integer value.
 	 * 
 	 * @param key
 	 *            The key from {@link IntOptions}.
 	 * @param value
 	 *            Integer.
 	 */
 	public void setInt(IntOptions key, int value) {
 		props.setProperty(key.toString(), Integer.toString(value));
 		this.save();
 	}
 	
 	/**
 	 * Sets a String value.
 	 * 
 	 * @param key
 	 *            The key from {@link IntOptions}.
 	 * @param value
 	 *            Integer.
 	 */
 	public void setString(StringOptions key, String value) {
 		props.setProperty(key.toString(), value);
 		this.save();
 	}
 	
 	/**
 	 * Checks for potential errors in the configuration.
 	 */
 	private void checkForErrors() {
 		if ((this.getBoolean(Options.USE_VSYNC)) && (this.getBoolean(Options.LIMIT_FPS))) {
 			this.setBoolean(Options.LIMIT_FPS, false);
 			Logger.error("Configuration error fixed! VSync and FPS limitation cannot be on simultaneously!");
 		}
 		
 		if (this.getInt(IntOptions.FONT_INDEX) > Fonts.values().length - 1) {
 			this.setInt(IntOptions.FONT_INDEX, IntOptions.FONT_INDEX.getDefaultValue());
 			Logger.error("Configuration error fixed! Font index cannot be greater than the number of fonts available!");
 		}
 	}
 	
 	/**
 	 * Copies the configuration file to the path of this configuration.
 	 */
 	protected void copy() {
 		new File(Spaceshooters.getPath()).mkdir();
 		try {
 			InputStream src = this.getClass().getResourceAsStream("/config.cfg");
 			File dest = new File(path);
 			Files.copy(src, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
 		} catch (IOException e) {
 			Logger.error(e, "I cannot copy my configuration.");
 		}
 	}
 	
 	/**
 	 * Loads the configuration.
 	 * 
 	 * @param stream
 	 *            The input stream.
 	 * @throws IOException
 	 *             When configuration is not found.
 	 */
 	private void load() throws IOException {
 		props.load(new FileInputStream(path));
 		this.checkForErrors();
 		props.load(new FileInputStream(path));
 	}
 	
 	/**
 	 * Saves the configuration.
 	 */
 	public void save() {
 		try {
 			props.store(new FileOutputStream(path), null);
 		} catch (IOException e) {
 			Logger.error(e, "I cannot save my configuration :(");
 		}
 	}
 	
 	public static Configuration getConfiguration() {
		if (config == null) {
			return new Configuration();
		}
 		return config;
 	}
 }
