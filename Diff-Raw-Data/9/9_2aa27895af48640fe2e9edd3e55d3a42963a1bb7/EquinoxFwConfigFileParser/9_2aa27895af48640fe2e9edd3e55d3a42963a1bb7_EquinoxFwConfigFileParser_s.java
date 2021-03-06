 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  ******************************************************************************/
 package org.eclipse.equinox.frameworkadmin.equinox.internal;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import org.eclipse.equinox.frameworkadmin.*;
 import org.eclipse.equinox.frameworkadmin.equinox.internal.utils.FileUtils;
 import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 
 public class EquinoxFwConfigFileParser {
 	private static final String KEY_ECLIPSE_PROV_CACHE = "eclipse.prov.cache"; //$NON-NLS-1$
 	private static final String KEY_ECLIPSE_PROV_DATA_AREA = "eclipse.prov.data.area"; //$NON-NLS-1$
 	private static final String KEY_ORG_ECLIPSE_EQUINOX_SIMPLECONFIGURATOR_CONFIGURL = "org.eclipse.equinox.simpleconfigurator.configUrl"; //$NON-NLS-1$
 	private static final String KEY_OSGI_BUNDLES = "osgi.bundles"; //$NON-NLS-1$
 	private static final String KEY_OSGI_BUNDLES_EXTRA_DATA = "osgi.bundles.extraData"; //$NON-NLS-1$
 	private static final String KEY_OSGI_FRAMEWORK = "osgi.framework"; //$NON-NLS-1$
 	private static final String KEY_OSGI_LAUNCHER_PATH = "osgi.launcherPath"; //$NON-NLS-1$
 	private static final String[] PATHS = new String[] {KEY_OSGI_LAUNCHER_PATH, KEY_ECLIPSE_PROV_CACHE};
 	private static final String[] URLS = new String[] { //
 	KEY_OSGI_FRAMEWORK, //
 			KEY_ORG_ECLIPSE_EQUINOX_SIMPLECONFIGURATOR_CONFIGURL, //
 			KEY_ECLIPSE_PROV_DATA_AREA};
 	private static final String[] URL_ARRAYS = new String[] {KEY_OSGI_BUNDLES, KEY_OSGI_BUNDLES_EXTRA_DATA};
 
 	private static boolean DEBUG = false;
 	private final BundleContext context;
 	private static String USE_REFERENCE_STRING = null;
 
 	public EquinoxFwConfigFileParser(BundleContext context) {
 		this.context = context;
 		if (context != null)
 			USE_REFERENCE_STRING = context.getProperty(EquinoxConstants.PROP_KEY_USE_REFERENCE);
 
 	}
 
 	private static String getCommandLine(BundleInfo bundleInfo, final URL baseUrl) {
 		String location = bundleInfo.getLocation();
 		if (location == null)
 			return null;
 		boolean useReference = true;
 		if (location.startsWith("file:")) {
 			if (USE_REFERENCE_STRING != null && USE_REFERENCE_STRING.equals("false"))
 				useReference = false;
 		}
 
 		try {
 			new URL(location);
 		} catch (MalformedURLException e) {
 			Log.log(LogService.LOG_ERROR, "EquinoxFwConfigFileParser.getCommandLine():bundleInfo=" + bundleInfo, e);
 			//			Never happen. ignore.
 		}
 		if (useReference)
 			if (!location.startsWith("reference:"))
 				location = "reference:" + location;
 
 		int startLevel = bundleInfo.getStartLevel();
 		boolean toBeStarted = bundleInfo.isMarkedAsStarted();
 
 		StringBuffer sb = new StringBuffer();
 		//		if (baseUrl != null && bundleUrl.getProtocol().equals("file")) {
 		//			String bundlePath = bundleUrl.toString();
 		//			bundlePath = Utils.getRelativePath(bundleUrl, baseUrl);
 		//			sb.append(bundlePath);
 		//		} else
 		sb.append(location);
 		if (startLevel == BundleInfo.NO_LEVEL && !toBeStarted)
 			return sb.toString();
 		sb.append("@");
 		if (startLevel != BundleInfo.NO_LEVEL)
 			sb.append(startLevel);
 		if (toBeStarted)
 			sb.append(":start");
 		return sb.toString();
 	}
 
 	private static Properties getConfigProps(BundleInfo[] bInfos, ConfigData configData, LauncherData launcherData, boolean relative, File fwJar) {
 
 		Properties props = new Properties();
 
 		if (configData.getInitialBundleStartLevel() != BundleInfo.NO_LEVEL)
 			props.setProperty(EquinoxConstants.PROP_BUNDLES_STARTLEVEL, Integer.toString(configData.getInitialBundleStartLevel()));
 		if (configData.getBeginingFwStartLevel() != BundleInfo.NO_LEVEL)
 			props.setProperty(EquinoxConstants.PROP_INITIAL_STARTLEVEL, Integer.toString(configData.getBeginingFwStartLevel()));
 
 		if (fwJar != null) {
 			URL fwJarUrl = null;
 			try {
 				fwJarUrl = Utils.getUrl("file", null, fwJar.getAbsolutePath());
 			} catch (MalformedURLException e) {
 				// Never happens
 				e.printStackTrace();
 			}
 			String fwJarSt = fwJarUrl.toExternalForm();
			if (fwJarSt.length() > 5 && fwJarSt.charAt(4) != '/') {
				fwJarSt = "file:/" + fwJarUrl.getFile();
 			}
 			props.setProperty(EquinoxConstants.PROP_OSGI_FW, fwJarSt /* fwJar.getAbsolutePath() */);
 		}
 
 		final File launcher = launcherData.getLauncher();
 		if (launcher != null) {
 			String launcherName = launcher.getName();
 			if (launcherName.endsWith(EquinoxConstants.EXE_EXTENSION)) {
 				props.setProperty(EquinoxConstants.PROP_LAUNCHER_NAME, launcherName.substring(0, launcherName.lastIndexOf(EquinoxConstants.EXE_EXTENSION)));
 				props.setProperty(EquinoxConstants.PROP_LAUNCHER_PATH, launcher.getParentFile().getAbsolutePath());
 			}
 		}
 
 		if (bInfos != null) {
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < bInfos.length; i++) {
 				normalizeLocation(bInfos[i]);
 				sb.append(getCommandLine(bInfos[i], null));
 				if (i + 1 < bInfos.length)
 					sb.append(",");
 			}
 			props.setProperty(EquinoxConstants.PROP_BUNDLES, sb.toString());
 			setOSGiBundlesExtraData(props, bInfos);
 
 		}
 		props = Utils.appendProperties(props, configData.getFwIndependentProps());
 
 		props = Utils.appendProperties(props, configData.getFwDependentProps());
 		//props.setProperty(EquinoxConstants.AOL, EquinoxConstants.AOL);
 		return props;
 	}
 
 	private static String getExtraDataCommandLine(BundleInfo bundleInfo, final URL baseUrl) {
 		StringBuffer sb = new StringBuffer();
 		sb.append(bundleInfo.getLocation()).append(',').append(bundleInfo.getSymbolicName()).append(',').append(bundleInfo.getVersion());
 		return sb.toString();
 	}
 
 	private static boolean getMarkedAsStartedFormat(String msg, String original) {
 		//boolean ret = false;
 		if (msg == null)
 			return false;
 		msg = msg.trim();
 		if (msg.equals("start")) {
 			//ret = true;
 			return true;
 		}
 		if (!msg.equals(""))
 			new IllegalArgumentException("Invalid Format =" + original);
 		return false;
 	}
 
 	static boolean isFwDependent(String key) {
 		// TODO This algorithm is temporal. 
 		if (key.startsWith(EquinoxConstants.PROP_EQUINOX_DEPENDENT_PREFIX))
 			return true;
 		return false;
 	}
 
 	private static void normalizeLocation(BundleInfo bInfo) {
 		String location = bInfo.getLocation();
 		if (location.startsWith("file:")) {
 			location = location.substring("file:".length());
 			if (!location.startsWith("/"))
 				location = "/" + location;
 			//		if (fwJarSt != null)
 			//			if (fwJarSt.equals(location))
 			//				continue;
 			location = Utils.replaceAll(location, File.separator, "/");
 			//String jarName = location.substring(location.lastIndexOf("/") + 1);
 			//		if (jarName.startsWith(EquinoxConstants.FW_JAR_PLUGIN_NAME))
 			//			continue;
 			bInfo.setLocation("file:" + location);
 		}
 	}
 
 	/**
 	 * @param value
 	 * @throws ManipulatorException
 	 */
 	private static void setInstallingBundles(Manipulator manipulator, String value) throws NumberFormatException {
 		ConfigData configData = manipulator.getConfigData();
 		if (value != null) {
 			String[] bInfoStrings = Utils.getTokens(value, ",");
 			for (int i = 0; i < bInfoStrings.length; i++) {
 				String token = bInfoStrings[i].trim();
 				token = FileUtils.getRealLocation(manipulator, token, false);
 				int index = 0;
 				while (true) {
 					if (token.charAt(index) == ' ')
 						index++;
 					else
 						break;
 				}
 				if (index != 0)
 					token = token.substring(index);
 
 				int indexI = token.indexOf("@");
 				if (indexI == -1) {
 					String location = FileUtils.getEclipseRealLocation(manipulator, token);
 					configData.addBundle(new BundleInfo(location));
 					//	configData.installingBundlesList.add(new BundleInfo(this.convertUrl(bInfoStrings[i])));
 					continue;
 				}
 				String location = token.substring(0, indexI);
 				location = FileUtils.getEclipseRealLocation(manipulator, location);
 				//					URL url = this.convertUrl(bInfoStrings[i].substring(0, indexI));
 				String slAndFlag = token.substring(indexI + "@".length());
 				boolean markedAsStarted = false;
 				int startLevel = -1;
 				int indexJ = slAndFlag.indexOf(":");
 				if (indexJ == -1) {
 					markedAsStarted = getMarkedAsStartedFormat(slAndFlag, token);
 					// 3 or start
 					//					try {
 					//startLevel = Integer.parseInt(slAndFlag);
 					configData.addBundle(new BundleInfo(location, markedAsStarted));
 					continue;
 					//					} catch (NumberFormatException nfe) {
 					//						throw new ManipulatorException("Invalid Format of bInfoStrings[" + i + "]=" + bInfoStrings[i], nfe, ManipulatorException.OTHERS);
 					//					}
 				} else if (indexJ == 0) {
 					markedAsStarted = getMarkedAsStartedFormat(slAndFlag.substring(indexJ + ":".length()), token);
 					configData.addBundle(new BundleInfo(location, startLevel, markedAsStarted));
 					continue;
 				}
 				//				try {
 				startLevel = Integer.parseInt(slAndFlag.substring(0, indexJ));
 				markedAsStarted = getMarkedAsStartedFormat(slAndFlag.substring(indexJ + ":".length()), bInfoStrings[i]);
 				configData.addBundle(new BundleInfo(location, startLevel, markedAsStarted));
 				//				} catch (NumberFormatException nfe) {
 				//					throw new ManipulatorException("Invalid Format of bInfoStrings[" + i + "]=" + bInfoStrings[i], nfe, ManipulatorException.OTHERS);
 				//				}
 			}
 		}
 	}
 
 	private static void setOSGiBundlesExtraData(Properties props, BundleInfo[] bInfos) {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < bInfos.length; i++) {
 			normalizeLocation(bInfos[i]);
 			sb.append(getExtraDataCommandLine(bInfos[i], null));
 			if (i + 1 < bInfos.length)
 				sb.append(",");
 		}
 		props.setProperty(EquinoxConstants.PROP_BUNDLES_EXTRADATA, sb.toString());
 
 	}
 
 	/**
 	 * inputFile must be not a directory but a file.
 	 * 
 	 * @param configData
 	 * @param inputFile
 	 * @throws IOException
 	 */
 	public void readFwConfig(Manipulator manipulator, File inputFile) throws IOException {
 		if (inputFile.isDirectory())
 			throw new IllegalArgumentException("inputFile:" + inputFile + " must not be a directory.");
 
 		ConfigData configData = manipulator.getConfigData();
 		LauncherData launcherData = manipulator.getLauncherData();
 		configData.initialize();
 
 		Properties props = new Properties();
 		InputStream is = null;
 		try {
 			is = new FileInputStream(inputFile);
 			props.load(is);
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			is = null;
 		}
 
 		String launcherName = null;
 		String launcherPath = null;
 		configData.setBundles(null);
 		props = makeAbsolute(props, launcherData.getLauncher().getParentFile().toURL());
 		for (Enumeration enumeration = props.keys(); enumeration.hasMoreElements();) {
 			String key = (String) enumeration.nextElement();
 			String value = props.getProperty(key);
 			if (key.equals(EquinoxConstants.PROP_BUNDLES_STARTLEVEL))
 				configData.setInitialBundleStartLevel(Integer.parseInt(value));
 			else if (key.equals(EquinoxConstants.PROP_INITIAL_STARTLEVEL)) {
 				configData.setBeginningFwStartLevel(Integer.parseInt(value));
 			} else if (key.equals(EquinoxConstants.PROP_BUNDLES)) {
 				setInstallingBundles(manipulator, value);
 			} else {
 				if (isFwDependent(key)) {
 					configData.setFwDependentProp(key, value);
 				} else
 					configData.setFwIndependentProp(key, value);
 				if (key.equals(EquinoxConstants.PROP_OSGI_FW)) {
 					File f = new File(new URL(value).getFile());
 					launcherData.setFwJar(f);
 					configData.addBundle(new BundleInfo(value));
 				}
 				if (key.equals(EquinoxConstants.PROP_LAUNCHER_NAME))
 					if (launcherData.getLauncher() == null)
 						launcherName = value;
 				if (key.equals(EquinoxConstants.PROP_LAUNCHER_PATH))
 					if (launcherData.getLauncher() == null)
 						launcherPath = value;
 			}
 		}
 		if (launcherName != null && launcherPath != null) {
 			launcherData.setLauncher(new File(launcherPath, launcherName + EquinoxConstants.EXE_EXTENSION));
 		}
 
 		Log.log(LogService.LOG_INFO, "Config file(" + inputFile.getAbsolutePath() + ") is read successfully.");
 	}
 
 	private static Properties makeRelative(Properties props, URL rootURL) throws IOException {
 		for (int i = 0; i < PATHS.length; i++) {
 			String path = props.getProperty(PATHS[i]);
 			if (path != null)
 				props.put(PATHS[i], EquinoxManipulatorImpl.makeRelative(path, rootURL.getFile()));
 		}
 
 		for (int i = 0; i < URLS.length; i++) {
 			String url = props.getProperty(URLS[i]);
 			if (url != null)
 				props.put(URLS[i], EquinoxManipulatorImpl.makeRelative(url, rootURL));
 		}
 
 		String value = props.getProperty(KEY_OSGI_BUNDLES);
 		if (value != null)
 			props.setProperty(KEY_OSGI_BUNDLES, EquinoxManipulatorImpl.makeRelative(value, new URL(rootURL, "plugins/")));
 
 		String extra = props.getProperty(KEY_OSGI_BUNDLES_EXTRA_DATA);
 		if (extra != null) {
 			StringBuffer buffer = new StringBuffer();
 			for (StringTokenizer tokenizer = new StringTokenizer(extra, ","); tokenizer.hasMoreTokens();) {
 				String token = tokenizer.nextToken();
 				String absolute = EquinoxManipulatorImpl.makeRelative(token, rootURL);
 				buffer.append(absolute);
 				buffer.append(',');
 				buffer.append(tokenizer.nextToken());
 				buffer.append(',');
 				buffer.append(tokenizer.nextToken());
 				if (tokenizer.hasMoreTokens())
 					buffer.append(',');
 			}
 			props.setProperty(KEY_OSGI_BUNDLES_EXTRA_DATA, buffer.toString());
 		}
 		return props;
 	}
 
 	private static Properties makeAbsolute(Properties props, URL rootURL) throws IOException {
 		for (int i = 0; i < PATHS.length; i++) {
 			String path = props.getProperty(PATHS[i]);
 			if (path != null)
 				props.setProperty(PATHS[i], EquinoxManipulatorImpl.makeAbsolute(path, rootURL.getFile()));
 		}
 
 		for (int i = 0; i < URLS.length; i++) {
 			String url = props.getProperty(URLS[i]);
 			if (url != null)
 				props.put(URLS[i], EquinoxManipulatorImpl.makeAbsolute(url, rootURL));
 		}
 
 		String value = props.getProperty(KEY_OSGI_BUNDLES);
 		if (value != null)
 			props.setProperty(KEY_OSGI_BUNDLES, EquinoxManipulatorImpl.makeArrayAbsolute(value, new URL(rootURL, "plugins/")));
 
 		String extra = props.getProperty(KEY_OSGI_BUNDLES_EXTRA_DATA);
 		if (extra != null) {
 			StringBuffer buffer = new StringBuffer();
 			for (StringTokenizer tokenizer = new StringTokenizer(extra, ","); tokenizer.hasMoreTokens();) {
 				String token = tokenizer.nextToken();
 				String absolute = EquinoxManipulatorImpl.makeAbsolute(token, rootURL);
 				buffer.append(absolute);
 				buffer.append(',');
 				buffer.append(tokenizer.nextToken());
 				buffer.append(',');
 				buffer.append(tokenizer.nextToken());
 				if (tokenizer.hasMoreTokens())
 					buffer.append(',');
 			}
 			props.setProperty(KEY_OSGI_BUNDLES_EXTRA_DATA, buffer.toString());
 		}
 
 		return props;
 	}
 
 	public void saveFwConfig(BundleInfo[] bInfos, Manipulator manipulator, boolean backup, boolean relative) throws IOException {//{
 		ConfigData configData = manipulator.getConfigData();
 		LauncherData launcherData = manipulator.getLauncherData();
 		File fwJar = EquinoxBundlesState.getFwJar(launcherData, configData);
 		if (fwJar != null)
 			launcherData.setFwJar(fwJar);
 		File outputFile = launcherData.getFwConfigLocation();
 		if (outputFile.exists()) {
 			if (outputFile.isFile()) {
 				if (!outputFile.getName().equals(EquinoxConstants.CONFIG_INI))
 					throw new IllegalStateException("launcherData.getFwConfigLocation() is a File but its name doesn't equal " + EquinoxConstants.CONFIG_INI);
 			} else { // Directory
 				outputFile = new File(outputFile, EquinoxConstants.CONFIG_INI);
 			}
 		} else {
 			if (!outputFile.getName().equals(EquinoxConstants.CONFIG_INI)) {
 				if (!outputFile.mkdir())
 					throw new IOException("Fail to mkdir (" + outputFile + ")");
 				outputFile = new File(outputFile, EquinoxConstants.CONFIG_INI);
 			}
 		}
 		String header = "This properties were written by " + this.getClass().getName();
 
 		Properties configProps = getConfigProps(bInfos, configData, launcherData, relative, fwJar);
 		if (configProps == null || configProps.size() == 0) {
 			Log.log(LogService.LOG_WARNING, this, "saveFwConfig() ", "configProps is empty");
 			return;
 		}
 		Utils.createParentDir(outputFile);
 
 		if (DEBUG)
 			Utils.printoutProperties(System.out, "configProps", configProps);
 
 		if (backup)
 			if (outputFile.exists()) {
 				File dest = Utils.getSimpleDataFormattedFile(outputFile);
 				if (!outputFile.renameTo(dest))
 					throw new IOException("Fail to rename from (" + outputFile + ") to (" + dest + ")");
 				Log.log(LogService.LOG_INFO, this, "saveFwConfig()", "Succeed to rename from (" + outputFile + ") to (" + dest + ")");
 			}
 
 		FileOutputStream out = null;
 		try {
 			out = new FileOutputStream(outputFile);
 			configProps = makeRelative(configProps, launcherData.getLauncher().getParentFile().toURL());
 			configProps.store(out, header);
 			Log.log(LogService.LOG_INFO, "FwConfig is saved successfully into:" + outputFile);
 		} finally {
 			try {
 				out.flush();
 				out.close();
 				//				Log.log(LogService.LOG_INFO, "out is closed successfully.");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			out = null;
 		}
 	}
 }
