 package de.dhbw.mannheim.cloudraid.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Enables you to handle the program's configuration via a XML file.<br>
  * Singleton
  * 
  * @author Florian Bausch
  */
 public class Config extends HashMap<String, String> {
 	/**
 	 * Maximum file size in MiB
 	 */
 	public static final int MAX_FILE_SIZE = 1024 * 1024 * 512;
 
 	private static final String CONFIG_FILE = System.getProperty("os.name")
 			.contains("windows") ? System.getenv("APPDATA")
 			+ "\\cloudraid\\config.xml" : System.getProperty("user.home")
 			+ "/.config/cloudraid.xml";
 
 	private static class Holder {
 		public static final Config INSTANCE = new Config();
 	}
 
 	private HashMap<String, String> salt = new HashMap<String, String>();
 
 	/**
 	 * Auto generated serialVersionUID
 	 */
 	private static final long serialVersionUID = -3740632761998756639L;
 
 	/**
 	 * Get the Singleton instance of Config.
 	 * 
 	 * @return The instance of Config
 	 */
 	public static synchronized Config getInstance() {
 		return Holder.INSTANCE;
 	}
 
 	/**
 	 * Creates a Config object that stores the configuration that is stored in
 	 * the config file.
 	 */
 	private Config() {
 		if (!new File(CONFIG_FILE).exists())
 			return;
 		DocumentBuilder docBuilder = null;
 		try {
 			docBuilder = DocumentBuilderFactory.newInstance()
 					.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		docBuilder.setErrorHandler(null);
 
 		Document doc;
 		try {
 			System.out.println("Path to config file: " + CONFIG_FILE);
 			doc = docBuilder.parse(new File(CONFIG_FILE));
		} catch (SAXException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		NodeList list = doc.getDocumentElement().getElementsByTagName("entry");
 		for (int i = 0; i < list.getLength(); i++) {
 			Element node = (Element) list.item(i);
 			this.put(node.getAttribute("name").trim(), node.getTextContent()
 					.trim());
 			this.salt.put(node.getAttribute("name").trim(),
 					node.getAttribute("salt").trim());
 		}
 	}
 
 	/**
 	 * Reads a setting from the settings list.
 	 * 
 	 * @param key
 	 *            The specific key of this setting.
 	 * @return A string representation of the setting.
 	 */
 	public synchronized String get(String key) {
 		try {
 			return super.get(key).replace("&quot", "\"").replace("&lt;", "<")
 					.replace("&gt;", ">").replace("&amp;", "&");
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the float representation of the value of a stored key, if it is
 	 * possible. If not, the <code>defaultVal</code> is returned. <br>
 	 * If the key does not exist, it is created with the value
 	 * <code>defaultVal</code>.
 	 * 
 	 * @param key
 	 *            The key of the value in the Config.
 	 * @param defaultVal
 	 *            The fall back value.
 	 * @return The float representation of the value, or <code>defaultVal</code>
 	 */
 	public synchronized float getFloat(String key, float defaultVal) {
 		String str = this.get(key);
 		if (str == null) {
 			this.put(key, "" + defaultVal);
 			return defaultVal;
 		}
 		try {
 			return Float.parseFloat(str);
 		} catch (Exception e) {
 			return defaultVal;
 		}
 	}
 	
 	/**
 	 * Returns the double representation of the value of a stored key, if it is
 	 * possible. If not, the <code>defaultVal</code> is returned. <br>
 	 * If the key does not exist, it is created with the value
 	 * <code>defaultVal</code>.
 	 * 
 	 * @param key
 	 *            The key of the value in the Config.
 	 * @param defaultVal
 	 *            The fall back value.
 	 * @return The float representation of the value, or <code>defaultVal</code>
 	 */
 	public synchronized double getDouble(String key, double defaultVal) {
 		String str = this.get(key);
 		if (str == null) {
 			this.put(key, "" + defaultVal);
 			return defaultVal;
 		}
 		try {
 			return Double.parseDouble(str);
 		} catch (Exception e) {
 			return defaultVal;
 		}
 	}
 
 	/**
 	 * Returns the int representation of the value of a stored key, if it is
 	 * possible. If not, the <code>defaultVal</code> is returned. <br>
 	 * If the key does not exist, it is created with the value
 	 * <code>defaultVal</code>.
 	 * 
 	 * @param key
 	 *            The key of the value in the Config.
 	 * @param defaultVal
 	 *            The fall back value.
 	 * @return The int representation of the value, or <code>defaultVal</code>
 	 */
 	public synchronized int getInt(String key, int defaultVal) {
 		String str = this.get(key);
 		if (str == null) {
 			this.put(key, "" + defaultVal);
 			return defaultVal;
 		}
 		try {
 			return Integer.parseInt(str);
 		} catch (Exception e) {
 			return defaultVal;
 		}
 	}
 
 	/**
 	 * Returns the String representation of the value of a stored key.<br>
 	 * If the key does not exist, it is created with the value
 	 * <code>defaultVal</code>.
 	 * 
 	 * @param key
 	 *            The key of the value in the Config.
 	 * @param defaultVal
 	 *            The fall back value.
 	 * @return The String representation of the value, or
 	 *         <code>defaultVal</code>
 	 */
 	public synchronized String getString(String key, String defaultVal) {
 		String str = this.get(key);
 		if (str == null) {
 			this.put(key, "" + defaultVal);
 			return defaultVal;
 		} else
 			return str;
 	}
 	
 	/**
 	 * Returns the boolean representation of the value of a stored key, if it is
 	 * possible. If not, the <code>defaultVal</code> is returned. <br>
 	 * If the key does not exist, it is created with the value
 	 * <code>defaultVal</code>.
 	 * 
 	 * @param key
 	 *            The key of the value in the Config.
 	 * @param defaultVal
 	 *            The fall back value.
 	 * @return The float representation of the value, or <code>defaultVal</code>
 	 */
 	public synchronized boolean getBoolean(String key, boolean defaultVal) {
 		String str = this.get(key);
 		if (str == null) {
 			this.put(key, "" + defaultVal);
 			return defaultVal;
 		}
 		try {
 			return Boolean.parseBoolean(str);
 		} catch (Exception e) {
 			return defaultVal;
 		}
 	}
 
 	/**
 	 * Checks whether there is a certain <code>key</code> stored in the config
 	 * or not.
 	 * 
 	 * @param key
 	 *            The key you want to check.
 	 * @return <code>true</code> if it exists.
 	 */
 	public synchronized boolean keyExists(String key) {
 		return !(this.get(key) == null);
 	}
 
 	/**
 	 * Sets the value of a certain parameter in the config. When the config is
 	 * written to a file the last value that was set for the <code>key</code> is
 	 * stored.
 	 * 
 	 * @param key
 	 *            A unique identification key for this setting.
 	 * @param value
 	 *            The value that shall be stored.
 	 * @return
 	 */
 	public synchronized String put(String key, String value) {
 		return super.put(key, value.replace("&", "&amp;").replace("<", "&lt;")
 				.replace(">", "&gt;").replace("\"", "&quot;"));
 	}
 
 	/**
 	 * Sets the value of a certain parameter in the config. When the config is
 	 * written to a file the last value that was set for the <code>key</code> is
 	 * stored.
 	 * 
 	 * @param key
 	 *            A unique identification key for this setting.
 	 * @param value
 	 *            The value that shall be stored.
 	 * @param salt
 	 *            The salt for an encrypted value.
 	 * @return The concatenation of value and salt.
 	 */
 	public synchronized String put(String key, String value, String salt) {
 		String put1 = super.put(
 				key,
 				value.replace("&", "&amp;").replace("<", "&lt;")
 						.replace(">", "&gt;").replace("\"", "&quot;"));
 		String put2 = this.salt.put(key, salt);
 		return put1 + put2;
 	}
 
 	/**
 	 * Writes the config to a file.
 	 */
 	public void save() {
 		try {
 			if (!new File(CONFIG_FILE).getParentFile().exists())
 				new File(CONFIG_FILE).getParentFile().mkdirs();
 			BufferedWriter writer = new BufferedWriter(new FileWriter(
 					CONFIG_FILE));
 			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 			writer.newLine();
 			writer.write("<root>");
 			Set<String> keys = this.keySet();
 			for (String k : keys) {
 				writer.newLine();
 				String saltString = this.salt.get(k);
 				String salt = saltString == null || saltString.equals("") ? ""
 						: " salt=\"" + saltString + "\"";
 				writer.write("\t<entry name=\"" + k + "\"" + salt + ">"
 						+ this.get(k) + "</entry>");
 			}
 			writer.newLine();
 			writer.write("</root>");
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 		Config.getInstance().put("test1", "test2", "test3");
 		Config.getInstance().put("test4", "test5");
 		Config.getInstance().save();
 	}
 }
