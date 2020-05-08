 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class PackagerPlugin extends AbstractUIPlugin {
 
 	
 	/** The symbolic plugin ID.	 */
 	public static final String PLUGIN_ID = "org.fedoraproject.eclipse.packager"; //$NON-NLS-1$
 	// The shared instance
 	private static PackagerPlugin plugin;
 	
 	// Persistent property things
 	
 	// Type values for persistent property types
 	/** Git type for persistent property types */
 	public static final String PROJECT_KEY = "project"; //$NON-NLS-1$
 	/** Qualified name for the type property */
 	public static final QualifiedName PROJECT_PROP = new QualifiedName(PLUGIN_ID, PROJECT_KEY);
 
 	/**
 	 * The constructor
 	 */
 	public PackagerPlugin() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
 	 * )
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static PackagerPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given plug-in
 	 * relative path
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 	
 	/**
 	 * Get a String preference related to this plug-in.
 	 * 
 	 * @param prefrenceIdentifier
 	 *            The identifier of the preference to retrieve.
 	 * @return The value of the prefrence in question, or {@code null} if not
 	 *         set.
 	 */
 	public static String getStringPreference(final String prefrenceIdentifier) {
 		IPreferenceStore store = getDefault().getPreferenceStore();
 		String candidate = store.getString(prefrenceIdentifier);
 		if (candidate.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
 			return null;
 		}
 		return candidate;
 	}
 	
 	/**
 	 * 
 	 * @return {@code true} when platform was started in debug mode (
 	 *         {@code -debug} switch) and
 	 *         {@code org.fedoraproject.eclipse.packager/debug=true} is set in
 	 *         some .options file either in $HOME/.options or $(pwd)/.options.
 	 */
 	public static boolean inDebugMode() {
		return Platform.inDebugMode()
				&& Platform.getDebugOption(PLUGIN_ID + "/debug").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 }
