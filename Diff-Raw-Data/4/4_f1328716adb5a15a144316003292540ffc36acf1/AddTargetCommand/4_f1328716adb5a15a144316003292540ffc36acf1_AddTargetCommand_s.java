 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Properties;
 
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.internal.target.ZendDevCloud;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 
 /**
  * Creating a new target
  * 
  * @author Roy, 2011
  */
 public class AddTargetCommand extends TargetAwareCommand {
 
 	// properties file keys
 	private static final String PROP_SECRETKEY = "secretkey";
 	private static final String PROP_KEY = "key";
 
 	// options
 	private static final String ID = "t";
 	private static final String KEY = "k";
 	private static final String SECRETKEY = "s";
 	private static final String HOST = "h";
 	private static final String PROPERTIES = "p";
 	private static final String DEVPASS = "d";
 
 	private Properties props;
 
 	@Option(opt = DEVPASS, required = false, description = "The DevPaas username and password concatenated by a colon", argName = "user:pass")
 	public String getDevPaas() {
 		return getValue(DEVPASS);
 	}
 
 	@Option(opt = ID, required = false, description = "id of the new target", argName = "id")
 	public String getId() {
 		return getValue(ID);
 	}
 
 	@Option(opt = KEY, required = false, description = "API key used by the new target", argName = PROP_KEY)
 	public String getKey() {
 		Properties p = getProperties();
 		if (p != null) {
 			return p.getProperty(PROP_KEY);
 		}
 
 		return getValue(KEY);
 	}
 
 	@Option(opt = SECRETKEY, required = false, description = "API secret key used by the new target", argName = "secret-key")
 	public String getSecretKey() {
 		Properties p = getProperties();
 		if (p != null) {
 			return p.getProperty(PROP_SECRETKEY);
 		}
 
 		return getValue(SECRETKEY);
 	}
 
 	@Option(opt = HOST, required = false, description = "Host URL of the new target", argName = "host")
 	public String getHost() {
 		return getValue(HOST);
 	}
 
 	@Option(opt = PROPERTIES, required = false, description = "Properties file specifies 'key' and 'secretkey' values", argName = "path-to-file")
 	public File getPropertiesFile() {
 		final String filename = getValue(PROPERTIES);
 
 		if (filename == null || filename.length() == 0) {
 			return null;
 		}
 		final File file = new File(filename);
 		return file;
 	}
 
 	@Override
 	public boolean doExecute() {
 		final String targetId = getId();
 		final String devPaas = getDevPaas();
 		String key = getKey();
 		String secretKey = getSecretKey();
 		String host = getHost();
 
 		// resolve devpaas account properties
 		if (devPaas != null) {
 			IZendTarget[] targets = resolveTarget(devPaas);
 			if (targets != null) {
 				final TargetsManager targetManager = getTargetManager();
 				String id = targetId != null ? targetId : targetManager
 						.createUniqueId(null);
 				int i = 0;
 				for (IZendTarget zendTarget : targets) {
 					key = zendTarget.getKey();
 					secretKey = zendTarget.getSecretKey();
 					host = zendTarget.getHost().toString();
 					IZendTarget target = targetManager.createTarget(id + "_"
 							+ i++, host, key, secretKey);
 					if (target == null) {
 						return false;
 					}
 				}
 				return true;
 			}
 		}
 
 		if (key == null && secretKey == null || host == null) {
 			getLogger().error(
 					"To create target it is required to provide hostname, key and secret key. "
 							+ "It can be provided through a properties file.");
 			return false;
 		}
 
 		final TargetsManager targetManager = getTargetManager();
 		IZendTarget target = targetId == null ? targetManager.createTarget(
 				host, key, secretKey) : targetManager.createTarget(targetId,
 				host, key, secretKey);
 
 		if (target == null) {
 			return false;
 		}
 
 		getLogger().info(
 				MessageFormat.format(
 						"Target {0} was added successfully, with id {1}", host,
 						target.getId()));
 		return true;
 	}
 
 	private IZendTarget[] resolveTarget(final String devPaas) {
 		final ZendDevCloud detect = new ZendDevCloud();
 		final String[] split = devPaas.split(":");
 		if (split.length != 2) {
 
 			final String message = "Error resolving devpaas account properties: "
 					+ devPaas
 					+ ". Argument should include both user and password concatenated by "
 					+ "colon, for example john.dohe:8hi8Rfe";
 
 			getLogger().error(message);
 			throw new IllegalStateException(message);
 
 		}
 		try {
 			final IZendTarget[] targets = detect.detectTarget(split[0],
 					split[1]);
 
 			if (targets != null && targets.length > 0) {
 				return targets;
 			}
 		} catch (SdkException e) {
 			getLogger().error(e);
 		} catch (IOException e) {
 			getLogger().error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Reads properties files and return values
 	 * 
 	 * @return Properties loaded object
 	 */
 	private Properties getProperties() {
 		if (props == null) {
 			final File file = getPropertiesFile();
 			if (file == null) {
 				return null;
 			}
 			try {
 				Properties p = new Properties();
 				p.load(new FileInputStream(file));
 				getLogger().info("Loading file " + file.getAbsolutePath());
 				props = p;
 			} catch (FileNotFoundException e) {
 				getLogger().error("File not found " + file.getAbsolutePath());
 			} catch (IOException e) {
 				getLogger().error("Error reading " + file.getAbsolutePath());
 			}
 		}
 		return props;
 	}
 
 }
