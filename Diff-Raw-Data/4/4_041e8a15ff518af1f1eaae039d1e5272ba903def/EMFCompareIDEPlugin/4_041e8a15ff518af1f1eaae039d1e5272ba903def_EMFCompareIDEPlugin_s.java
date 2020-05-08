 /*******************************************************************************
  * Copyright (c) 2011, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.compare.extension.PostProcessorRegistry;
 import org.eclipse.emf.compare.ide.internal.extension.PostProcessorRegistryListener;
 import org.eclipse.emf.compare.ide.internal.policy.LoadOnDemandPolicyRegistryImpl;
 import org.eclipse.emf.compare.ide.internal.policy.LoadOnDemandPolicyRegistryListener;
 import org.eclipse.emf.compare.ide.policy.ILoadOnDemandPolicy;
 import org.eclipse.emf.compare.ide.policy.ILoadOnDemandPolicy.Registry;
 import org.eclipse.emf.compare.ide.utils.AbstractRegistryEventListener;
 import org.eclipse.emf.compare.merge.IMerger;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class EMFCompareIDEPlugin extends Plugin {
 	/** The plug-in ID. */
 	public static final String PLUGIN_ID = "org.eclipse.emf.compare.ide"; //$NON-NLS-1$
 
 	/** The id of the load on demand policy extension point. */
 	public static final String LOAD_ON_DEMAND_POLICY_PPID = "load_on_demand_policy"; //$NON-NLS-1$
 
 	/**
 	 * The id of the merger extension point.
 	 * 
 	 * @since 3.0
 	 */
 	public static final String MERGER_EXTENSION_PPID = "mergerExtension"; //$NON-NLS-1$
 
 	/** The plug-in ID of org.eclipse.emf.compare. */
 	private static final String COMPARE_PLUGIN_ID = "org.eclipse.emf.compare"; //$NON-NLS-1$
 
 	/** This plugin's shared instance. */
 	private static EMFCompareIDEPlugin plugin;
 
 	/**
 	 * The registry that will hold references to all post processors.
 	 */
 	private PostProcessorRegistry postProcessorRegistry;
 
 	/** The registry listener that will be used to react to post processor changes. */
 	private PostProcessorRegistryListener postProcessorListener;
 
 	/** The registry that will hold references to all {@link ILoadOnDemandPolicy}. **/
 	private Registry loadOnDemandRegistry;
 
 	/** The registry listener that will be used to react to load on demand policy changes. */
 	private LoadOnDemandPolicyRegistryListener loadOnDemandRegistryListener;
 
 	/** The registry listener that will be used to react to merger extension changes. */
 	private AbstractRegistryEventListener mergerRegistryListener;
 
 	/** The registry that will hold references to all mergers. */
 	private IMerger.Registry mergerRegistry;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		plugin = this;
 		super.start(context);
 
 		final IExtensionRegistry registry = Platform.getExtensionRegistry();
 
 		this.postProcessorRegistry = new PostProcessorRegistry();
 		this.postProcessorListener = new PostProcessorRegistryListener(postProcessorRegistry);
 
 		registry.addListener(postProcessorListener,
 				PostProcessorRegistryListener.POST_PROCESSOR_EXTENSION_POINT);
 		postProcessorListener.parseInitialContributions();
 
 		this.loadOnDemandRegistry = new LoadOnDemandPolicyRegistryImpl();
 		this.loadOnDemandRegistryListener = new LoadOnDemandPolicyRegistryListener(loadOnDemandRegistry,
 				PLUGIN_ID, LOAD_ON_DEMAND_POLICY_PPID);
 
 		registry.addListener(loadOnDemandRegistryListener, PLUGIN_ID + "." + LOAD_ON_DEMAND_POLICY_PPID);
 		loadOnDemandRegistryListener.readRegistry(registry);
 
 		mergerRegistry = new IMerger.RegistryImpl();
		mergerRegistryListener = new MergerExtensionRegistryListener(COMPARE_PLUGIN_ID, MERGER_EXTENSION_PPID);
		registry.addListener(mergerRegistryListener, COMPARE_PLUGIN_ID + "." + MERGER_EXTENSION_PPID);
 		mergerRegistryListener.readRegistry(registry);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 
 		final IExtensionRegistry registry = Platform.getExtensionRegistry();
 		registry.removeListener(loadOnDemandRegistryListener);
 		registry.removeListener(postProcessorListener);
 	}
 
 	/**
 	 * Returns the post processor registry to which extension will be registered.
 	 * 
 	 * @return the post processor registry to which extension will be registered
 	 */
 	public PostProcessorRegistry getPostProcessorRegistry() {
 		return postProcessorRegistry;
 	}
 
 	/**
 	 * Returns the merger registry to which extension will be registered.
 	 * 
 	 * @return the merger registry to which extension will be registered
 	 * @since 3.0
 	 */
 	public IMerger.Registry getMergerRegistry() {
 		return mergerRegistry;
 	}
 
 	/**
 	 * Log the given message with the given severity to the logger of this plugin.
 	 * 
 	 * @param severity
 	 *            the severity of the message.
 	 * @param message
 	 *            the message to log.
 	 */
 	public void log(int severity, String message) {
 		getLog().log(new Status(severity, PLUGIN_ID, message));
 	}
 
 	/**
 	 * Returns the registry of load on demand policies.
 	 * 
 	 * @return the registry of load on demand policies.
 	 */
 	public ILoadOnDemandPolicy.Registry getLoadOnDemandPolicyRegistry() {
 		return loadOnDemandRegistry;
 	}
 
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the shared instance
 	 */
 	public static EMFCompareIDEPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Listener for contributions to the merger extension.
 	 */
 	private class MergerExtensionRegistryListener extends AbstractRegistryEventListener {
 
 		/** TAG_MERGER. */
 		static final String TAG_MERGER = "merger"; //$NON-NLS-1$
 
 		/** ATT_CLASS. */
 		static final String ATT_CLASS = "class"; //$NON-NLS-1$
 
 		/** ATT_RANKING. */
 		static final String ATT_RANKING = "ranking"; //$NON-NLS-1$
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param pluginID
 		 *            The plugin id.
 		 * @param extensionPointID
 		 *            The extension point id.
 		 */
 		public MergerExtensionRegistryListener(String pluginID, String extensionPointID) {
 			super(pluginID, extensionPointID);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.compare.ide.utils.AbstractRegistryEventListener#readElement(org.eclipse.core.runtime.IConfigurationElement,
 		 *      org.eclipse.emf.compare.ide.utils.AbstractRegistryEventListener.Action)
 		 */
 		@Override
 		protected boolean readElement(IConfigurationElement element, Action b) {
 			if (element.getName().equals(TAG_MERGER)) {
 				if (element.getAttribute(ATT_CLASS) == null) {
 					logMissingAttribute(element, ATT_CLASS);
 				} else if (element.getAttribute(ATT_RANKING) == null) {
 					String rankingStr = element.getAttribute(ATT_RANKING);
 					try {
 						Integer.parseInt(rankingStr);
 					} catch (NumberFormatException nfe) {
 						logError(element, "Attribute '" + ATT_RANKING
 								+ "' is malformed, should be an integer.");
 					}
 					logMissingAttribute(element, ATT_RANKING);
 				} else {
 					switch (b) {
 						case ADD:
 							try {
 								IMerger merger = (IMerger)element.createExecutableExtension(ATT_CLASS);
 								merger.setRanking(Integer.parseInt(element.getAttribute(ATT_RANKING)));
 								IMerger previous = mergerRegistry.add(merger);
 								if (previous != null) {
 									log(IStatus.WARNING, "The factory '" + merger.getClass().getName()
 											+ "' is registered twice.");
 								}
 							} catch (CoreException e) {
 								logError(element, e.getMessage());
 							}
 							break;
 						case REMOVE:
 							mergerRegistry.remove(element.getAttribute(ATT_CLASS));
 							break;
 						default:
 							break;
 					}
 					return true;
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.compare.ide.utils.AbstractRegistryEventListener#logError(org.eclipse.core.runtime.IConfigurationElement,
 		 *      java.lang.String)
 		 */
 		@Override
 		protected void logError(IConfigurationElement element, String string) {
 			log(IStatus.ERROR, string);
 		}
 	}
 }
