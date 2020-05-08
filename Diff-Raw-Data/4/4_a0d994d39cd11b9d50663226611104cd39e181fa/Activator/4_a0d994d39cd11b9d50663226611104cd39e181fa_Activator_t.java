 /*******************************************************************************
  * Copyright (c) 2013 Atos
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Arthur Daussy - initial implementation
  *******************************************************************************/
 package org.eclipse.escriptmonkey.scripting.engine.python.jython;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
import org.python.core.Py;
 import org.python.core.PySystemState;
 
 
 public class Activator extends AbstractUIPlugin {
 
 	public static final String PLUGIN_ID = "org.eclipse.escriptmonkey.scripting.engine.python.jython";
 
 	private static Activator mInstance;
 
 	public static Activator getDefault() {
 		return mInstance;
 	}
 
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 
 		mInstance = this;
 		//		Properties preProperties = System.getProperties();
 		Properties preProperties = PySystemState.getBaseProperties();
 
 		Properties postProperties = new Properties();
 		postProperties.put("python.home", getPluginRootDir());
 		postProperties.put("python.modules.builtin", "errno");
		Py.getSystemState().setClassLoader(this.getClass().getClassLoader());
 		PySystemState.initialize(preProperties, postProperties, new String[0]);
 
 		//				PyObject load = org.python.core.imp.
 		//		System.out.println(load);
 
 		// set packageManager AFTER initialization as init will set it, too
 		// FIXME for now caching is disabled. We need to track how the cache destination is calculated
 		PySystemState.packageManager = new JythonPackageManager(null, PySystemState.registry);
 	}
 
 	private static String getPluginRootDir() {
 		try {
 			Bundle bundle = Platform.getBundle("org.jython");
 			URL fileURL = FileLocator.find(bundle, new Path("."), null);
 			return FileLocator.toFileURL(fileURL).getFile();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		mInstance = null;
 
 		super.stop(context);
 	}
 
 	public static List<File> getLibraryFolders() {
 		ArrayList<File> folders = new ArrayList<File>();
 
 		File rootFolder = new File(getPluginRootDir());
 		java.nio.file.Path path = rootFolder.toPath();
 		File libFolder = path.resolve("Lib").toFile();
 		if(libFolder.exists())
 			folders.add(libFolder);
 
 		return folders;
 	}
 }
