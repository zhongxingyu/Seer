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
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.internal.target.ZendDevCloud;
 import org.zend.sdklib.internal.target.ZendTarget;
 import org.zend.sdklib.manager.TargetException;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.sdklib.target.LicenseExpiredException;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.values.ServerType;
 import org.zend.webapi.core.connection.data.values.WebApiVersion;
 import org.zend.webapi.core.connection.response.ResponseCode;
 import org.zend.webapi.internal.core.connection.exception.UnexpectedResponseCode;
 import org.zend.webapi.internal.core.connection.exception.WebApiCommunicationError;
 
 /**
  * Creating a new target
  * 
  * @author Roy, 2011
  */
 public class AddTargetCommand extends TargetAwareCommand {
 
 	private static final int[] possiblePorts = new int[] { 10081, 10082, 10088 };
 	private static final int[] possiblePhpcloudPorts = new int[] { 10082 };
 
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
 		String targetId = getId();
 		String devPaas = getDevPaas();
 		TargetsManager targetManager = getTargetManager();
 		List<IZendTarget> targets = new ArrayList<IZendTarget>();
 		// resolve phpcloud account properties
 		if (devPaas != null) {
 			IZendTarget[] phpcloudTargets = resolveTarget(devPaas);
 			if (phpcloudTargets != null) {
 				String id = targetId != null ? targetId : targetManager
 						.createUniqueId(null);
 				int i = 0;
 				for (IZendTarget zendTarget : phpcloudTargets) {
 					String key = zendTarget.getKey();
 					String secretKey = zendTarget.getSecretKey();
 					String host = zendTarget.getHost().toString();
 					IZendTarget target = null;
 					try {
 						target = new ZendTarget(id + "_" + i++, new URL(host),
 								key, secretKey);
 						targets.add(target);
 					} catch (MalformedURLException e) {
 						getLogger()
 								.error(MessageFormat
 										.format("Cannot add target {0}. Invalid host value.",
 												getId()));
 					}
 				}
 
 			}
 		} else {
 			String key = getKey();
 			String secretKey = getSecretKey();
 			String host = getHost();
			if (key == null && secretKey == null || host == null) {
 				getLogger()
 						.error("To create target it is required to provide hostname, key and secret key. "
								+ "It can be provided through a properties file.");
 				return false;
 			}
 			if (targetId == null) {
 				targetId = targetManager.createUniqueId(null);
 			}
 			try {
 				targets.add(new ZendTarget(targetId, new URL(host), key,
 						secretKey));
 			} catch (MalformedURLException e) {
 				getLogger().error(
 						MessageFormat.format(
 								"Cannot add target {0}. Invalid host value.",
 								getId()));
 			}
 		}
 		List<IZendTarget> toRemove = new ArrayList<IZendTarget>();
 		for (IZendTarget t : targets) {
 			IZendTarget target = null;
 			try {
 				target = testConnectAndDetectPort(t);
 				if (target != null) {
 					targetManager.add(target, true);
 					getLogger()
 							.info(MessageFormat
 									.format("Target {0} was added successfully, with id {1}",
 											t.getHost().toString(), t.getId()));
 				} else {
 					toRemove.add(t);
 				}
 			} catch (LicenseExpiredException e) {
 				getLogger()
 						.error(MessageFormat
 								.format("Cannot add target {0}. Check if license has not exipred.",
 										getId()));
 			} catch (WebApiException e) {
 				getLogger().error(
 						MessageFormat.format(
 								"Cannot add target {0}. " + e.getMessage(),
 								getId()));
 			} catch (TargetException e) {
 				getLogger().error(
 						MessageFormat.format(
 								"Cannot add target {0}. " + e.getMessage(),
 								getId()));
 			}
 		}
 		for (IZendTarget t : toRemove) {
 			targets.remove(t);
 		}
 		if (targets.size() == 0) {
 			return false;
 		}
 		return true;
 	}
 
 	public IZendTarget testTargetConnection(IZendTarget target)
 			throws WebApiException, LicenseExpiredException {
 		try {
 			if (target.connect(WebApiVersion.V1_3, ServerType.ZEND_SERVER)) {
 				return target;
 			}
 		} catch (WebApiCommunicationError e) {
 			throw e;
 		} catch (UnexpectedResponseCode e) {
 			ResponseCode code = e.getResponseCode();
 			switch (code) {
 			case INTERNAL_SERVER_ERROR:
 			case AUTH_ERROR:
 			case INSUFFICIENT_ACCESS_LEVEL:
 				throw e;
 			default:
 				break;
 			}
 		}
 		try {
 			if (target.connect(WebApiVersion.UNKNOWN, ServerType.ZEND_SERVER)) {
 				return target;
 			}
 		} catch (WebApiException ex) {
 			if (target.connect()) {
 				return target;
 			}
 		}
 		return null;
 	}
 
 	private IZendTarget testConnectAndDetectPort(IZendTarget target)
 			throws WebApiException, LicenseExpiredException {
 		WebApiException catchedException = null;
 		int[] portToTest = possiblePorts;
 		if (TargetsManager.isPhpcloud(target)) {
 			portToTest = possiblePhpcloudPorts;
 		}
 		if (target.getHost().getPort() == -1) {
 			for (int port : portToTest) {
 				URL old = target.getHost();
 				URL host;
 				try {
 					host = new URL(old.getProtocol(), old.getHost(), port,
 							old.getFile());
 					((ZendTarget) target).setHost(host);
 				} catch (MalformedURLException e) {
 					// should never happen
 				}
 				try {
 					return testTargetConnection(target);
 				} catch (WebApiException e) {
 					catchedException = e;
 				}
 			}
 		} else {
 			try {
 				return testTargetConnection(target);
 			} catch (WebApiException e) {
 				catchedException = e;
 			}
 		}
 		if (catchedException != null) {
 			throw catchedException;
 		}
 		return null;
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
