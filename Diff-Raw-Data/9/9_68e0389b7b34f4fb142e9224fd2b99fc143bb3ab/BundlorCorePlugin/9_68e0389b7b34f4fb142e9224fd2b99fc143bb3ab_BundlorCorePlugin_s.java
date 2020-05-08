 /*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.bundlor.internal.core;
 
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * Bundle Activator of the bundlor.core plugin.
  * 
  * @author Christian Dupuis
  * @author Leo Dos Santos
  * @since 1.1.2
  */
 public class BundlorCorePlugin extends Plugin {
 
 	/** The bundle symbolic name */
 	public static final String PLUGIN_ID = "org.eclipse.virgo.ide.bundlor.core";
 
 	/** The id of the bundlor builder */
 	public static final String BUILDER_ID = PLUGIN_ID + ".builder";
 
 	/** The properties key for properties files */
 	public static final String TEMPLATE_PROPERTIES_FILE_KEY = "template.properties.files";
 
 	/** Default property value for properties files */
 	public static final String TEMPLATE_PROPERTIES_FILE_DEFAULT = "template.properties;build.properties";
 
 	/** The properties key for byte code vs source code scanning */
 	public static final String TEMPLATE_BYTE_CODE_SCANNING_KEY = "byte.code.scanning";
 
 	/** Default property value for byte code vs source code scanning */
	public static final boolean TEMPLATE_BYTE_CODE_SCANNING_DEFAULT = true;
 
 	/** The properties key for auto-formatting generated manifests */
 	public static final String FORMAT_GENERATED_MANIFESTS_KEY = "bundlor.generated.manifest.autoformatting";
 
 	/** Default property value for auto-formatting generated manifests */
 	public static final boolean FORMAT_GENERATED_MANIFESTS_DEFAULT = false;
 
 	/** The singleton instance */
 	private static BundlorCorePlugin plugin;
 
 	/** Holds references to {@link IncrementalReadablePartialManifest} */
 	private IncrementalPartialManifestManager manifestManager;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		manifestManager = new IncrementalPartialManifestManager();
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the singleton instance.
 	 */
 	public static BundlorCorePlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the {@link IncrementalPartialManifestManager}.
 	 */
 	public IncrementalPartialManifestManager getManifestManager() {
 		return manifestManager;
 	}
 
 }
