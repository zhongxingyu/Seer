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
 import java.util.Properties;
 
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 
 /**
  * Creating a new target
  * 
  * @author Roy, 2011
  * 
  */
 public class CreateTargetCommand extends TargetAwareCommand {
 
 	// properties file keys
 	private static final String PROP_SECRETKEY = "secretkey";
 	private static final String PROP_KEY = "key";
 
 	// options
 	private static final String ID = "t";
 	private static final String KEY = "k";
 	private static final String SECRETKEY = "s";
 	private static final String HOST = "h";
 	private static final String PROPERTIES = "p";
 
 	@Option(opt = ID, required = false, description = "Target id", argName = "id")
 	public String getId() {
 		return getValue(ID);
 	}
 
	@Option(opt = KEY, required = false, description = "Target environment API Key name", argName = PROP_KEY)
 	public String getKey() {
 		Properties p = getProperties();
 		if (p != null) {
 			return p.getProperty(PROP_KEY);
 		}
 
 		return getValue(KEY);
 	}
 
	@Option(opt = SECRETKEY, required = false, description = "Target environment API Key secret value", argName = "secret-key")
 	public String getSecretKey() {
 		Properties p = getProperties();
 		if (p != null) {
 			return p.getProperty(PROP_SECRETKEY);
 		}
 
 		return getValue(SECRETKEY);
 	}
 
 	@Option(opt = HOST, required = true, description = "Target host URL", argName = "host")
 	public String getHost() {
 		return getValue(HOST);
 	}
 
 	@Option(opt = PROPERTIES, required = false, description = "The properties file", argName = "file")
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
 		final String key = getKey();
 		final String secretKey = getSecretKey();
 		final String host = getHost();
 
		if (key == null && secretKey == null) {
			getLogger()
					.error("To create target it is required to provide key and secret key or properties file.");
			return false;
		}

 		final TargetsManager targetManager = getTargetManager();
 		IZendTarget target = targetId == null ? targetManager.createTarget(
 				host, key, secretKey) : targetManager.createTarget(targetId,
 				host, key, secretKey);
 
 		if (target == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Reads properties files and return values
 	 * 
 	 * @return Properties loaded object
 	 */
 	private Properties getProperties() {
 		final File file = getPropertiesFile();
 		if (file == null) {
 			return null;
 		}
 		try {
 			Properties p = new Properties();
 			p.load(new FileInputStream(file));
 			getLogger().info("Loading file " + file.getAbsolutePath());
 			return p;
 		} catch (FileNotFoundException e) {
 			getLogger().error("File not found " + file.getAbsolutePath());
 		} catch (IOException e) {
 			getLogger().error("Error reading " + file.getAbsolutePath());
 		}
 		return null;
 	}
 
 }
