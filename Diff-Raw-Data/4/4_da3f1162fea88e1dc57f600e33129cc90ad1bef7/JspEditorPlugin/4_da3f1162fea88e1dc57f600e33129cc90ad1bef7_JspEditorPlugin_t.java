 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;

 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.ui.text.JavaTextTools;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jst.jsp.ui.internal.JSPUIPlugin;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.jboss.tools.common.log.BaseUIPlugin;
 import org.jboss.tools.common.log.IPluginLog;
 import org.jboss.tools.common.text.xml.XmlEditorPlugin;
 import org.jboss.tools.jst.jsp.preferences.JSPOccurrencePreferenceConstants;
 import org.osgi.framework.Bundle;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class JspEditorPlugin extends BaseUIPlugin {
 	
 	public static final String EXTESION_POINT_LOCALE_PROVIDER = "org.jboss.tools.jst.jsp.localeProvider"; //$NON-NLS-1$
 	
 	//The shared instance.
 	private static JspEditorPlugin plugin;
 	public static final String PLUGIN_ID = "org.jboss.tools.jst.jsp";  //$NON-NLS-1$
 	public static final String I18N_VALIDATION_PROBLEM_ID = PLUGIN_ID + ".i18nproblemmarker"; //$NON-NLS-1$
 	public static final String RESOURCES_PATH = "/resources"; //$NON-NLS-1$
 
 	// A Map to save a descriptor for each image
 	private HashMap fImageDescRegistry = null;
 
 	public static final String CA_JSF_ACTION_IMAGE_PATH = "images/ca/icons_JSF_Actions.gif"; //$NON-NLS-1$
 	public static final String CA_JSF_EL_IMAGE_PATH = "images/ca/icons_JSF_EL.gif"; //$NON-NLS-1$
 	public static final String CA_RESOURCES_IMAGE_PATH = "images/ca/icons_Resource_path.gif"; //$NON-NLS-1$
 	public static final String CA_JSF_MESSAGES_IMAGE_PATH = "images/ca/icons_Message_Bundles.gif"; //$NON-NLS-1$
 
 	
 	/**
 	 * The constructor.
 	 */
 	public JspEditorPlugin() {
 		plugin = this;
 	}
 
 
 	/**
 	 * Returns the workspace instance.
 	 */
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 
 	
 	public void startup() throws CoreException {
 		super.startup();
         // Bug-fix for showing breakpoint icons at startup
         Platform.getPlugin("org.jboss.tools.jst.web.debug.ui"); //$NON-NLS-1$
 	}
 	
 	protected void initializeDefaultPluginPreferences() {
 		IPreferenceStore store = getPreferenceStore();
 		JSPOccurrencePreferenceConstants.initializeDefaultValues(store);
 
 	}
 
 	public void initDefaultPluginPreferences() {
 		IPreferenceStore store = JSPUIPlugin.getDefault().getPreferenceStore();
 
 		JSPOccurrencePreferenceConstants.initializeDefaultValues(store);
 
 	}
 
 	public synchronized JavaTextTools getJavaTextTools() {
 		return XmlEditorPlugin.getDefault().getJavaTextTools();
 	}
 
 	/* New Text Editors */	
 
 	public static Path getInstallLocation()
 	{
 //		Object obj = null;
 		try
 		{
 			URL url = getDefault().getDescriptor().getInstallURL();
 			String s1 = FileLocator.resolve(url).getFile();
 			if(s1.startsWith("/")) //$NON-NLS-1$
 				s1 = s1.substring(1);
 			s1 = (new Path(s1)).toOSString();
 			String s;
 			if(s1.endsWith(File.separator))
 				s = s1;
 			else
 				s = s1 + File.separator;
 			return new Path(s);
		}	catch(IOException exception)	{
 			getDefault().logError(exception);
 		}
 		return null;
 	}
 
 	public static Shell getActiveShell()
 	{
 		if(plugin == null)
 			return null;
 		IWorkbench workBench = plugin.getWorkbench();
 		if(workBench == null)
 			return null;
 		IWorkbenchWindow workBenchWindow = workBench.getActiveWorkbenchWindow();
 		if(workBenchWindow == null)
 			return null;
 		else
 			return workBenchWindow.getShell();
 	}
 
 	public static JspEditorPlugin getDefault() {
 		return plugin;
 	}
 
 	public static boolean isDebugEnabled() {
 		return JspEditorPlugin.getDefault().isDebugging();
 	}
 
 	/**
 	 * @return IPluginLog object
 	 */
 	public static IPluginLog getPluginLog() {
 		return getDefault();
 	}
 	
 	/**
 	     * Returns an image descriptor for the image file at the given plug-in
 	     * relative path
 	     * 
 	     * @param path
 	     *                the path
 	     * @return the image descriptor
 	     */
 	    public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	    }
 
 	    /**
 	     * Get plug-in resource path
 	     * 
 	     * @return path
 	     */
 	    public static String getPluginResourcePath() {
 		Bundle bundle = Platform.getBundle(PLUGIN_ID);
 		URL url = null;
 		try {
 		    url = bundle == null ? null : FileLocator.resolve(bundle
 			    .getEntry(RESOURCES_PATH));
 
 		} catch (IOException e) {
 		    url = bundle.getEntry(RESOURCES_PATH);
 		}
 		return (url == null) ? null : url.getPath();
 	    }
 	    
 		/**
 		 * Creates an image from the given resource and adds the image to the
 		 * image registry.
 		 * 
 		 * @param resource
 		 * @return Image
 		 */
 		private Image createImage(String resource) {
 			ImageDescriptor desc = getImageDescriptorFromRegistry(resource);
 			Image image = null;
 
 			if (desc != null) {
 				image = desc.createImage();
 				// dont add the missing image descriptor image to the image
 				// registry
 				if (!desc.equals(ImageDescriptor.getMissingImageDescriptor())) {
 					getImageRegistry().put(resource, image);
 				}
 			}
 			return image;
 		}
 
 		/**
 		 * Creates an image descriptor from the given imageFilePath and adds the
 		 * image descriptor to the image descriptor registry. If an image
 		 * descriptor could not be created, the default "missing" image descriptor
 		 * is returned but not added to the image descriptor registry.
 		 * 
 		 * @param imageFilePath
 		 * @return ImageDescriptor image descriptor for imageFilePath or default
 		 *         "missing" image descriptor if resource could not be found
 		 */
 		private ImageDescriptor createImageDescriptor(String imageFilePath) {
 			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, imageFilePath);
 			if (imageDescriptor != null) {
 				getImageDescriptorRegistry().put(imageFilePath, imageDescriptor);
 			}
 			else {
 				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
 			}
 
 			return imageDescriptor;
 		}
 
 		/**
 		 * Retrieves the image associated with resource from the image registry.
 		 * If the image cannot be retrieved, attempt to find and load the image at
 		 * the location specified in resource.
 		 * 
 		 * @param resource
 		 *            the image to retrieve
 		 * @return Image the image associated with resource or null if one could
 		 *         not be found
 		 */
 		public Image getImage(String resource) {
 			Image image = getImageRegistry().get(resource);
 			if (image == null) {
 				// create an image
 				image = createImage(resource);
 			}
 			return image;
 		}
 
 		/**
 		 * Retrieves the image descriptor associated with resource from the image
 		 * descriptor registry. If the image descriptor cannot be retrieved,
 		 * attempt to find and load the image descriptor at the location specified
 		 * in resource.
 		 * 
 		 * @param resource
 		 *            the image descriptor to retrieve
 		 * @return ImageDescriptor the image descriptor assocated with resource or
 		 *         the default "missing" image descriptor if one could not be
 		 *         found
 		 */
 		public ImageDescriptor getImageDescriptorFromRegistry(String resource) {
 			ImageDescriptor imageDescriptor = null;
 			Object o = getImageDescriptorRegistry().get(resource);
 			if (o == null) {
 				// create a descriptor
 				imageDescriptor = createImageDescriptor(resource);
 			}
 			else {
 				imageDescriptor = (ImageDescriptor) o;
 			}
 			return imageDescriptor;
 		}
 
 		/**
 		 * Returns the image descriptor registry for this plugin.
 		 * 
 		 * @return HashMap - image descriptor registry for this plugin
 		 */
 		private HashMap getImageDescriptorRegistry() {
 			if (fImageDescRegistry == null) {
 				fImageDescRegistry = new HashMap();
 			}
 			return fImageDescRegistry;
 		}
 	
 }
