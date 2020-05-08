 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Matthew Conway - initial API and implementation
  *     IBM Corporation - concepts and ideas taken from Eclipse code
  *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
  *******************************************************************************/
 package net.sourceforge.eclipseccase.ui;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * UI plugin for the Clearcase plugin.
  * 
  * @author Gunnar Wagenknecht
  */
 public class ClearcaseUI extends AbstractUIPlugin {
 
 	//The shared instance.
 	private static ClearcaseUI plugin;
 
 	//Resource bundle.
 	private ResourceBundle resourceBundle;
 
 	public static final String PLUGIN_ID = "net.sourceforge.eclipseccase.ui"; //$NON-NLS-1$
 
 	/**
 	 * The constructor.
 	 * 
 	 * @param descriptor
 	 */
 	public ClearcaseUI() {
 		super();
 		plugin = this;
 		try {
 			resourceBundle = ResourceBundle.getBundle(PLUGIN_ID + ".messages"); //$NON-NLS-1$
 		} catch (MissingResourceException x) {
 			resourceBundle = null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 
 		PlatformUI.getWorkbench().addWindowListener(partListener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 
 		PlatformUI.getWorkbench().removeWindowListener(partListener);
 	}
 
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the default instance
 	 */
 	public static ClearcaseUI getInstance() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the string from the plugin's resource bundle, or 'key' if not
 	 * found.
 	 * 
 	 * @param key
 	 * @return the string
 	 */
 	public static String getResourceString(String key) {
 		ResourceBundle bundle = ClearcaseUI.getInstance().getResourceBundle();
 		try {
 			return (bundle != null) ? bundle.getString(key) : key;
 		} catch (MissingResourceException e) {
 			return key;
 		}
 	}
 
 	/**
 	 * Returns the plugin's resource bundle,
 	 * 
 	 * @return the resource bundle
 	 */
 	public ResourceBundle getResourceBundle() {
 		return resourceBundle;
 	}
 
 	/**
 	 * Returns an array of all editors that have an unsaved content. If the
 	 * identical content is presented in more than one editor, only one of those
 	 * editor parts is part of the result.
 	 * 
 	 * @return an array of all dirty editor parts.
 	 */
 	public static IEditorPart[] getDirtyEditors() {
 		Set inputs = new HashSet();
 		List result = new ArrayList(0);
 		IWorkbench workbench = getInstance().getWorkbench();
 		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
 		for (int i = 0; i < windows.length; i++) {
 			IWorkbenchPage[] pages = windows[i].getPages();
 			for (int x = 0; x < pages.length; x++) {
 				IEditorPart[] editors = pages[x].getDirtyEditors();
 				for (int z = 0; z < editors.length; z++) {
 					IEditorPart ep = editors[z];
 					IEditorInput input = ep.getEditorInput();
 					if (!inputs.contains(input)) {
 						inputs.add(input);
 						result.add(ep);
 					}
 				}
 			}
 		}
 		return (IEditorPart[]) result.toArray(new IEditorPart[result.size()]);
 	}
 
 	/**
 	 * Returns the preference value for <code>GENERAL_DEEP_DECORATIONS</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isDeepDecoration() 
 	{
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.GENERAL_DEEP_DECORATIONS);
 	}
 
 	/**
 	 * Returns the preference value for <code>GENERAL_DEEP_NEW</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isDeepNew() {
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.GENERAL_DEEP_NEW);
 	}	
 	/**
 	 * Returns the preference value for <code>TEXT_VERSION_DECORATION</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isTextVersionDecoration() {
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION);
 	}
 
 	/**
 	 * Returns the preference value for <code>TEXT_VIEW_DECORATION</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isTextViewDecoration() {
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION);
 	}
 
 	/**
 	 * Returns the preference value for <code>TEXT_VIEW_DECORATION</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isTextPrefixDecoration() {
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.TEXT_PREFIX_DECORATION);
 	}
 	/**
 	 * Returns the preference value for <code>ICON_DECORATE_NEW</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isIconNewDecoration() {
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.ICON_DECORATE_NEW);
 	}
 
 	/**
 	 * Returns the preference value for <code>ICON_DECORATE_EDITED</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isIconEditedDecoration() {
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.ICON_DECORATE_EDITED);
 	}
 
 	/**
 	 * Returns the preference value for <code>ICON_DECORATE_UNKNOWN</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isIconUnknownDecoration() {
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.ICON_DECORATE_UNKNOWN);
 	}
 
 	/**
 	 * Returns the preference value for <code>ICON_DECORATE_HIJACKED</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isIconHijackedDecoration() {
 		return getInstance().getPluginPreferences().getBoolean(
 				IClearcaseUIPreferenceConstants.ICON_DECORATE_HIJACKED);
 	}
 
 	/**
 	 * Returns the preference value for <code>TEXT_PREFIX_NEW</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static String getTextPrefixNew() {
 		return getInstance().getPluginPreferences().getString(
 				IClearcaseUIPreferenceConstants.TEXT_PREFIX_NEW);
 	}
 
 	/**
 	 * Returns the preference value for <code>TEXT_PREFIX_DIRTY</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static String getTextPrefixDirty() {
 		return getInstance().getPluginPreferences().getString(
 				IClearcaseUIPreferenceConstants.TEXT_PREFIX_DIRTY);
 	}
 
 	/**
 	 * Returns the preference value for <code>TEXT_PREFIX_UNKNOWN</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static String getTextPrefixUnknown() {
 		return getInstance().getPluginPreferences().getString(
 				IClearcaseUIPreferenceConstants.TEXT_PREFIX_UNKNOWN);
 	}
 
 	/**
 	 * Returns the preference value for <code>TEXT_PREFIX_HIJACKED</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static String getTextPrefixHijacked() {
 		return getInstance().getPluginPreferences().getString(
 				IClearcaseUIPreferenceConstants.TEXT_PREFIX_HIJACKED);
 	}
 
 	/**
 	 * Returns the preference value for <code>TEXT_PREFIX_EDITED</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static String getTextPrefixEdited() {
 		return getInstance().getPluginPreferences().getString(
 				IClearcaseUIPreferenceConstants.TEXT_PREFIX_EDITED);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
 	 */
 	protected void initializeImageRegistry(ImageRegistry reg) {
 		super.initializeImageRegistry(reg);
 
 		// objects
 		createImageDescriptor(reg, ClearcaseImages.IMG_QUESTIONABLE);
 		createImageDescriptor(reg, ClearcaseImages.IMG_EDITED);
 		createImageDescriptor(reg, ClearcaseImages.IMG_NO_REMOTEDIR);
 		createImageDescriptor(reg, ClearcaseImages.IMG_LINK);
 		createImageDescriptor(reg, ClearcaseImages.IMG_LINK_WARNING);
 		createImageDescriptor(reg, ClearcaseImages.IMG_HIJACKED);
 	}
 
 	private static void createImageDescriptor(ImageRegistry reg, String id) {
 		ImageDescriptor desc = imageDescriptorFromPlugin(ClearcaseUI.PLUGIN_ID,
 				ClearcaseImages.ICON_PATH + id);
 		reg.put(id, null != desc ? desc : ImageDescriptor
 				.getMissingImageDescriptor());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
 	 */
 	protected ImageRegistry createImageRegistry() {
 		// to overcome SWT issues we create this inside the UI thread
 		final ImageRegistry[] imageRegistries = new ImageRegistry[1];
 		getWorkbench().getDisplay().syncExec(new Runnable() {
 			public void run() {
 				imageRegistries[0] = new ImageRegistry(getWorkbench()
 						.getDisplay());
 			}
 		});
 		return imageRegistries[0];
 	}
 
 	/** the listener for opened editors */
 	private PartListener partListener = new PartListener();
 
     /**
      * Returns the workbench display
      * @return the workbench display
      */
     public static Display getDisplay() {
         return PlatformUI.getWorkbench().getDisplay();
     }
         
 
 }
