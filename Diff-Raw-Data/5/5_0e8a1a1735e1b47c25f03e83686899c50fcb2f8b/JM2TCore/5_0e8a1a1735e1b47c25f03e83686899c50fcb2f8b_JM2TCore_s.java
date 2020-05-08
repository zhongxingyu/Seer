 /*******************************************************************************
  * Copyright (c) 2010 Angelo ZERR.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:      
  *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
  *******************************************************************************/
 package org.eclipse.gmt.modisco.jm2t.core;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionDelta;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IRegistryChangeEvent;
 import org.eclipse.core.runtime.IRegistryChangeListener;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.gmt.modisco.jm2t.core.generator.IGeneratorType;
 import org.eclipse.gmt.modisco.jm2t.internal.core.JM2TModel;
 import org.eclipse.gmt.modisco.jm2t.internal.core.JM2TModelManager;
 import org.eclipse.gmt.modisco.jm2t.internal.core.Trace;
 import org.eclipse.gmt.modisco.jm2t.internal.core.generator.GeneratorType;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class JM2TCore extends Plugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.gmt.modisco.jm2t.core"; //$NON-NLS-1$
 
 	private static final String EXTENSION_GENERATOR_TYPE = "generatorTypes";
 
 	// The shared instance
 	private static JM2TCore plugin;
 
 	// cached copy of all generator and configuration types
 	private static List<IGeneratorType> generatorTypes;
 
 	private static IRegistryChangeListener registryListener;
 
 	protected static class RegistryChangeListener implements
 			IRegistryChangeListener {
 		public void registryChanged(IRegistryChangeEvent event) {
 			IExtensionDelta[] deltas = event.getExtensionDeltas(
 					JM2TCore.PLUGIN_ID, EXTENSION_GENERATOR_TYPE);
 			if (deltas != null) {
 				for (IExtensionDelta delta : deltas)
 					handleGeneratorTypeDelta(delta);
 			}
 		}
 	}
 
 	/**
 	 * The constructor
 	 */
 	public JM2TCore() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext context) throws Exception {
 		Trace.trace(Trace.CONFIG,
 				"----->----- JM2T Core plugin startup ----->-----");
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
 	public void stop(BundleContext context) throws Exception {
 		Trace.trace(Trace.CONFIG,
 				"-----<----- JM2T Core plugin shutdown -----<-----");
 		plugin = null;
 		super.stop(context);
 
 		if (registryListener != null)
 			Platform.getExtensionRegistry().removeRegistryChangeListener(
 					registryListener);
 
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static JM2TCore getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the Java M2T project corresponding to the given project.
 	 * 
 	 * @param project
 	 *            the given project
 	 * @return the Java M2T projec project corresponding to the given project,
 	 *         null if the given project is null
 	 */
 	public static IJM2TProject create(IProject project) {
 		if (project == null) {
 			return null;
 		}
 		JM2TModel jm2tModel = JM2TModelManager.getJM2TModelManager()
 				.getJM2TModel();
 		return jm2tModel.getJM2TProject(project);
 	}
 
 	/**
 	 * Returns an array of all known generator types.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the
 	 * result.
 	 * </p>
 	 * 
 	 * @return the array of generator types {@link IGeneratorType}
 	 */
 	public static IGeneratorType[] getGeneratorTypes() {
 		if (generatorTypes == null)
 			loadGeneratorTypes();
 
 		IGeneratorType[] st = new IGeneratorType[generatorTypes.size()];
 		generatorTypes.toArray(st);
 		return st;
 	}
 
 	/**
 	 * Returns the generator type with the given id, or <code>null</code> if
 	 * none. This convenience method searches the list of known generator types
 	 * ({@link #getGeneratorTypes()}) for the one with a matching generator type
 	 * id ({@link IGeneratorType#getId()}). The id may not be null.
 	 * 
 	 * @param id
 	 *            the generator type id
 	 * @return the generator type, or <code>null</code> if there is no generator
 	 *         type with the given id
 	 */
 	public static IGeneratorType findGeneratorType(String id) {
 		if (id == null)
 			throw new IllegalArgumentException();
 
 		if (generatorTypes == null)
 			loadGeneratorTypes();
 
 		Iterator<IGeneratorType> iterator = generatorTypes.iterator();
 		while (iterator.hasNext()) {
 			IGeneratorType generatorType = (IGeneratorType) iterator.next();
 			if (id.equals(generatorType.getId()))
 				return generatorType;
 		}
 		return null;
 	}
 
 	/**
 	 * Load the generator types.
 	 */
 	private static synchronized void loadGeneratorTypes() {
 		if (generatorTypes != null)
 			return;
 
 		Trace.trace(Trace.EXTENSION_POINT,
 				"->- Loading .generatorTypes extension point ->-");
 
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(
 				JM2TCore.PLUGIN_ID, EXTENSION_GENERATOR_TYPE);
 		List<IGeneratorType> list = new ArrayList<IGeneratorType>(cf.length);
 		addGeneratorTypes(cf, list);
 		addRegistryListener();
 		generatorTypes = list;
 
 		Trace.trace(Trace.EXTENSION_POINT,
 				"-<- Done loading .generatorTypes extension point -<-");
 	}
 
 	/**
 	 * Load the generator types.
 	 */
 	private static synchronized void addGeneratorTypes(
 			IConfigurationElement[] cf, List<IGeneratorType> list) {
 		for (IConfigurationElement ce : cf) {
 			try {
 				list.add(new GeneratorType(ce));
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded generatorType: "
 						+ ce.getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load generatorType: "
 						+ ce.getAttribute("id"), t);
 			}
 		}
 	}
 
 	protected static void handleGeneratorTypeDelta(IExtensionDelta delta) {
 		if (generatorTypes == null) // not loaded yet
 			return;
 
 		IConfigurationElement[] cf = delta.getExtension()
 				.getConfigurationElements();
 
 		List<IGeneratorType> list = new ArrayList<IGeneratorType>(
 				generatorTypes);
 		if (delta.getKind() == IExtensionDelta.ADDED) {
 			addGeneratorTypes(cf, list);
 		} else {
 			int size = list.size();
 			GeneratorType[] st = new GeneratorType[size];
 			list.toArray(st);
 			int size2 = cf.length;
 
 			for (int i = 0; i < size; i++) {
 				for (int j = 0; j < size2; j++) {
 					if (st[i].getId().equals(cf[j].getAttribute("id"))) {
 						st[i].dispose();
 						list.remove(st[i]);
 					}
 				}
 			}
 		}
 		generatorTypes = list;
 	}
 
 	private static void addRegistryListener() {
 		if (registryListener != null)
 			return;
 
 		registryListener = new RegistryChangeListener();
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		registry.addRegistryChangeListener(registryListener, JM2TCore.PLUGIN_ID);
 		JM2TCore.setRegistryListener(registryListener);
 	}
 
 	public static void setRegistryListener(IRegistryChangeListener listener) {
 		registryListener = listener;
 	}
 }
