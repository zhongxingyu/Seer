 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.target;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.SecureRandom;
 import java.util.Date;
 import java.util.Properties;
 
 import org.zend.sdklib.internal.utils.EnvironmentUtils;
 import org.zend.sdklib.target.IZendTarget;
 
 import com.ice.jni.registry.NoSuchKeyException;
 import com.ice.jni.registry.NoSuchValueException;
 import com.ice.jni.registry.Registry;
 import com.ice.jni.registry.RegistryException;
 import com.ice.jni.registry.RegistryKey;
 import com.ice.jni.registry.RegistryValue;
 
 /**
  * Auto detect local server
  * 
  * @author Roy, 2011
  */
 public class ZendTargetAutoDetect {
 
 	private static final String INSTALL_LOCATION = "InstallLocation";
 	private static final String USER_INI = "zend-server-user.ini";
 	private static final String NEED_TO_ELEVATE = "You need root privileges to run this script!";
 	private static final String MISSING_ZEND_SERVER = "Local Zend Server couldn't be found"
 			+ "please refer to http://www.zend.com/server";
	private static final String MAC_NOT_SUPPORTED = "Deployment on local Mac is not supported. This feature is for Linux and Windows Zend Server editions only.";
 
 	// linux key
 	private static final String CONFIG_FILE_LINUX = "/etc/zce.rc";
 	private static final String CONFIG_FILE_LINUX_DEB = "/etc/zce.rc-deb";
 	private static final String CONFIG_FILE_LINUX_RPM = "/etc/zce.rc-rpm";
 	private static final String ZCE_PREFIX = "ZCE_PREFIX";
 	private static final String APACHE_PORT = "APACHE_PORT";
 
 	// Registry
 	private static final String NODE_64 = "WOW6432node";
 	private static final String ZEND_SERVER = "ZendServer";
 	private static final String APACHE_APP_PORT = "ApacheAppPort";
 	private static final String ZEND_TECHNOLOGIES = "Zend Technologies";
 
 	public static URL localhost = null;
 	private String zendServerInstallLocation = null;
 	static {
 		try {
 			localhost = new URL("http://localhost");
 		} catch (MalformedURLException e) {
 			// ignore localhost is a valid URL
 		}
 	}
 
 	public ZendTargetAutoDetect() throws IOException {
 		findLocalhostInstallDirectory();
 	}
 
 	/**
 	 * 
 	 * @param targetId
 	 * @param key
 	 * @return the new target
 	 * @throws IOException
 	 */
 	public IZendTarget createLocalhostTarget(String targetId, String key)
 			throws IOException {
 
 		// find localhost install directory
 		String secretKey = findExistingSecretKey(key);
 		if (secretKey == null) {
 			secretKey = applySecretKey(key, generateSecretKey());
 		}
 
 		// create the target
 		return new ZendTarget(targetId, localhost, getDefaultServerURL(), key, secretKey);
 	}
 
 	/**
 	 * Generates a secret key and assigns locally. this key is not yet applied
 	 * to the local zend server. Read
 	 * {@link ZendTargetAutoDetect#applySecretKey(String, String)} to apply the
 	 * generated key
 	 * 
 	 * @param targetId
 	 * @param key
 	 * @return the new target
 	 */
 	public IZendTarget createTemporaryLocalhost(String targetId, String key) {
 		final String sk = generateSecretKey();
 
 		return new ZendTarget(targetId, localhost, getDefaultServerURL(), key, sk, true);
 	}
 
 	/**
 	 * Apply key and secret key to the installed local server
 	 * @param key
 	 * @param secretKey
 	 * @return
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
 	public String applySecretKey(String key, String secretKey)
 			throws IOException, FileNotFoundException {
 
 		File keysFile = getApiKeysFile();
 
 		// assert permissions are elevated
 		if (!keysFile.canWrite()) {
 			throw new IOException(NEED_TO_ELEVATE);
 		}
 
 		// backup file
 		final File edited = new File(keysFile.getParentFile(), USER_INI
 				+ ".tmp");
 		if (!edited.exists()) {
 			edited.createNewFile();
 		}
 
 		// backup file
 		BufferedReader ir = new BufferedReader(new FileReader(keysFile));
 		PrintStream os = new PrintStream(edited);
 		copyWithoutEdits(ir, os);
 		ir.close();
 		os.close();
 
 		// write zend-server-users.ini and find key
 		ir = new BufferedReader(new FileReader(edited));
 		os = new PrintStream(keysFile);
 		secretKey = copyWithEdits(ir, os, key, secretKey);
 		ir.close();
 		os.close();
 
 		return trimQuotes(secretKey);
 	}
 
 	public static String copyWithEdits(BufferedReader ir, PrintStream os,
 			String key, String secretKey) throws IOException {
 		String line = ir.readLine();
 		boolean block = false;
 		while (line != null) {
 			if ("[apiKeys]".equals(line)) {
 				writeApiKeyBlock(key, os, secretKey);
 				block = true;
 			} else {
 				os.println(line);
 			}
 			line = ir.readLine();
 		}
 
 		if (!block) {
 			writeApiKeyBlock(key, os, secretKey);
 		}
 
 		return secretKey;
 	}
 
 	public static void copyWithoutEdits(BufferedReader ir, PrintStream os)
 			throws IOException {
 		String line = ir.readLine();
 		while (line != null) {
 			os.println(line);
 			line = ir.readLine();
 		}
 	}
 
 	public static Properties readApiKeysSection(BufferedReader reader)
 			throws IOException {
 
 		Properties properties = new Properties();
 		String line = reader.readLine();
 		while (line != null) {
 			if ("[apiKeys]".equals(line)) {
 				line = reader.readLine();
 				while (line != null && !line.startsWith("[")) {
 					final String[] split = line.split("=");
 					if (split != null && split.length == 2) {
 						properties.put(split[0].trim(), split[1].trim());
 					}
 					line = reader.readLine();
 				}
 			}
 			line = reader.readLine();
 		}
 
 		return properties;
 	}
 
 	/**
 	 * Returns a new target for the localhost target
 	 * 
 	 * @param key
 	 * @return
 	 * @throws IOException
 	 */
 	public String findExistingSecretKey(String key) throws IOException {
 		// assert permissions are elevated
 		File keysFile = getApiKeysFile();
 		if (!keysFile.canRead()) {
 			// "Permission denied"
 			throw new IOException(NEED_TO_ELEVATE);
 		}
 
 		// read zend-server-users.ini and find key
 		final BufferedReader reader = new BufferedReader(new FileReader(
 				keysFile));
 		Properties p = readApiKeysSection(reader);
 		reader.close();
 		final String hash = p.getProperty(key + ":hash");
 		if (hash != null) {
 			// return secretKey if possible
 			return trimQuotes(hash);
 		}
 
 		// key not found
 		return null;
 	}
 
 	protected String trimQuotes(final String hash) {
 		return hash.startsWith("\"") ? hash.substring(1, hash.length() - 1) : hash;
 	}
 
 	/**
 	 * @return returns the Local installed Zend Server, null if no local Zend
 	 *         Server installed.
 	 * @throws IOException
 	 */
 	private void findLocalhostInstallDirectory() throws IOException {
 		if (zendServerInstallLocation != null) {
 			return;
 		}
 
		if (EnvironmentUtils.isUnderMaxOSX()) {
			throw new IllegalStateException(MAC_NOT_SUPPORTED);
		}
		
		if (EnvironmentUtils.isUnderLinux()) {
 			zendServerInstallLocation = getLocalZendServerFromFile();
 		} else {
 			// (EnvironmentUtils.isUnderWindows())
 			zendServerInstallLocation = getLocalZendServerFromRegistry();
 		}
 
 		if (zendServerInstallLocation == null) {
 			throw new IllegalStateException(MISSING_ZEND_SERVER);
 		}
 	}
 
 	private String getLocalZendServerFromFile() {
 		Properties props = getLocalZendServerProperties();
 		return props != null ? props.getProperty(ZCE_PREFIX) : null;
 	}
 	
 	private Properties getLocalZendServerProperties() {
 		Properties props = null;
 		// Try to find the zend.rc-deb file.
 		try {
 			FileInputStream fileStream = new FileInputStream(
 					CONFIG_FILE_LINUX_DEB);
 			props = new Properties();
 			props.load(fileStream);
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {
 		}
 		// If not found, find the zend.rc-rpm file.
 		if (props == null) {
 			try {
 				FileInputStream fileStream = new FileInputStream(
 						CONFIG_FILE_LINUX_RPM);
 				props = new Properties();
 				props.load(fileStream);
 			} catch (FileNotFoundException e) {
 			} catch (IOException e) {
 			}
 		}
 		// if not found, find the zend.rc file.
 		if (props == null) {
 			try {
 				FileInputStream fileStream = new FileInputStream(
 						CONFIG_FILE_LINUX);
 				props = new Properties();
 				props.load(fileStream);
 			} catch (FileNotFoundException e) {
 			} catch (IOException e) {
 			}
 		}
 		return props;
 	}
 
 	private String getLocalZendServerFromRegistry() throws IOException {
 		try {
 			RegistryKey zendServerKey = getZendServerRegistryKey();
 			if (zendServerKey != null) {
 				return zendServerKey.getStringValue(INSTALL_LOCATION);
 			}
 		} catch (NoSuchValueException e) {
 			return null;
 		} catch (RegistryException e) {
 			return null;
 		}
 		return null;
 	}
 
 	private RegistryKey getZendServerRegistryKey() throws RegistryException {
 		RegistryKey zendServerKey = null;
 		try {
 			zendServerKey = Registry.HKEY_LOCAL_MACHINE.openSubKey("SOFTWARE")
 					.openSubKey(ZEND_TECHNOLOGIES).openSubKey(ZEND_SERVER);
 			return zendServerKey;
 		} catch (NoSuchKeyException e1) {
 			// try the 64 bit
 
 			try {
 				zendServerKey = Registry.HKEY_LOCAL_MACHINE
 						.openSubKey("SOFTWARE").openSubKey(NODE_64)
 						.openSubKey(ZEND_TECHNOLOGIES).openSubKey(ZEND_SERVER);
 				return zendServerKey;
 			} catch (NoSuchKeyException e) {
 				return null;
 			}
 		}
 	}
 
 	private static void writeApiKeyBlock(String key, PrintStream os,
 			final String sk) {
 		os.println("[apiKeys]");
 
 		// roy:name = "roy"
 		os.print(key);
 		os.print(":name = \"");
 		os.print(key);
 		os.println("\"");
 
 		// roy:creationTime = 1304968104
 		os.print(key);
 		os.print(":creationTime = ");
 		os.println(new Date().getTime() / 1000);
 
 		// roy:hash = "c86ba2bc5fb62ee916031cf78..."
 		os.print(key);
 		os.print(":hash = \"");
 		os.print(sk);
 		os.println("\"");
 
 		// roy:role = "fullAccess"
 		os.print(key);
 		os.println(":role = \"fullAccess\"");
 
 		os.println();
 	}
 
 	private File getApiKeysFile() {
 		File keysFile = null;
 		if (EnvironmentUtils.isUnderLinux() || EnvironmentUtils.isUnderMaxOSX()) {
 			keysFile = new File(zendServerInstallLocation
 					+ "/gui/application/data/zend-server-user.ini");
 		} else if (EnvironmentUtils.isUnderWindows()) {
 			keysFile = new File(
 					zendServerInstallLocation
 							+ "ZendServer\\GUI\\application\\data\\zend-server-user.ini");
 		}
 
 		if (keysFile == null) {
 			throw new IllegalStateException(MISSING_ZEND_SERVER);
 		}
 		if (!keysFile.exists()) {
 			throw new IllegalArgumentException("The file "
 					+ keysFile.getAbsolutePath() + " does not exist.");
 		}
 
 		return keysFile;
 	}
 
 	private static String generateSecretKey() {
 		SecureRandom random = new SecureRandom();
 		final BigInteger bigInteger = new BigInteger(256, random);
 		final String string = bigInteger.toString(16);
 		return string.length() == 64 ? string : pad(string, 64);
 	}
 
 	/**
 	 * Random number was prefixed with some zeros... pad it
 	 * 
 	 * @param string
 	 * @param i
 	 * @return
 	 */
 	final private static String pad(String string, int i) {
 		i = i - string.length();
 		StringBuilder builder = new StringBuilder(string);
 		for (int j = 0; j < i; j++) {
 			builder.append("0");
 		}
 		return builder.toString();
 	}
 
 	private URL getDefaultServerURL() {
 		int port = -1;
 		try {
 			if (EnvironmentUtils.isUnderLinux() || EnvironmentUtils.isUnderMaxOSX()) {
 				Properties props = getLocalZendServerProperties();
 				if (props != null) {
 					port = Integer.valueOf(props.getProperty(APACHE_PORT, "-1"));
 				}
 			} else {
 				// (EnvironmentUtils.isUnderWindows())
 				RegistryKey zendServerKey = getZendServerRegistryKey();
 				if (zendServerKey != null) {
 					RegistryValue portValue = zendServerKey.getValue(APACHE_APP_PORT);
 					if (portValue != null) {
 						port = converByteArrayToInt(portValue.getByteData());
 					}
 				}
 			}
 			if (port != -1) {
 				return new URL(localhost.toString() + ":" + port);
 			}
 		} catch (Exception e) {
 			// if any exception occurs ignore it and return localhost
 		}
 		return localhost;
 	}
 
 	private int converByteArrayToInt(byte[] byteData) {
 		int result = 0;
 		for (int i = 0; i < byteData.length; i++) {
 			result += (int) (byteData[i] & 0xFF) << (8 * (byteData.length - i - 1));
 		}
 		return result != 0 ? result : -1;
 	}
 
 }
