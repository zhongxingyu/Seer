 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  * William Chen (Wind River) - [345387] Open the remote files with a proper editor
  * William Chen (Wind River) - [345552] Edit the remote files with a proper editor
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.activator;
 
 import java.net.URL;
 import java.util.Hashtable;
 
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.commands.IExecutionListener;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWTException;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.tcf.te.tcf.filesystem.internal.ImageConsts;
 import org.eclipse.tcf.te.tcf.filesystem.internal.autosave.SaveAllListener;
 import org.eclipse.tcf.te.tcf.filesystem.internal.autosave.SaveListener;
 import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSClipboard;
 import org.eclipse.tcf.te.tcf.filesystem.internal.url.TcfURLConnection;
 import org.eclipse.tcf.te.tcf.filesystem.internal.url.TcfURLStreamHandlerService;
 import org.eclipse.tcf.te.tcf.filesystem.internal.utils.PersistenceManager;
 import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;
 import org.eclipse.ui.IWorkbenchCommandConstants;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.url.URLConstants;
 import org.osgi.service.url.URLStreamHandlerService;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class UIPlugin extends AbstractUIPlugin {
 	// The shared instance of this plug-in.
 	private static UIPlugin plugin;
 	// The service registration for the "tcf" URL stream handler.
 	private ServiceRegistration<?> regURLStreamHandlerService;
 	// The listener which listens to command "SAVE" and synchronize the local file with the target.
 	private IExecutionListener saveListener;
 	// The listener which listens to command "SAVE ALL" and synchronize the local file with the target.
 	private IExecutionListener saveAllListener;
 	// The shared instance of Clipboard
 	private FSClipboard clipboard;
 
 	/**
 	 * The constructor
 	 */
 	public UIPlugin() {
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static UIPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Convenience method which returns the unique identifier of this plugin.
 	 */
 	public static String getUniqueIdentifier() {
 		if (getDefault() != null && getDefault().getBundle() != null) {
 			return getDefault().getBundle().getSymbolicName();
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		clipboard = new FSClipboard();
 
 		// Register the "tcf" URL stream handler service.
 		Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
 		properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { TcfURLConnection.PROTOCOL_SCHEMA });
 		regURLStreamHandlerService = context.registerService(URLStreamHandlerService.class.getName(), new TcfURLStreamHandlerService(), properties);
 		// Add the two execution listeners to command "SAVE" and "SAVE ALL".
 		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
 		if (commandService != null) {
 			saveListener = new SaveListener();
 			Command saveCmd = commandService.getCommand(IWorkbenchCommandConstants.FILE_SAVE);
 			saveCmd.addExecutionListener(saveListener);
 			saveAllListener = new SaveAllListener();
 			Command saveAllCmd = commandService.getCommand(IWorkbenchCommandConstants.FILE_SAVE_ALL);
 			saveAllCmd.addExecutionListener(saveAllListener);
 		}
 	}
 
 	/**
 	 * Get the shared instance of clipboard
 	 */
 	public FSClipboard getClipboard() {
 		return clipboard;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		PersistenceManager.getInstance().dispose();
 		if (regURLStreamHandlerService != null) {
 			// When URL stream handler service is unregistered, any URL related operation will be invalid.
 			regURLStreamHandlerService.unregister();
 			regURLStreamHandlerService = null;
 		}
 		// Remove the two execution listeners.
 		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
 		if (commandService != null) {
 			Command saveCmd = commandService.getCommand(IWorkbenchCommandConstants.FILE_SAVE);
 			saveCmd.removeExecutionListener(saveListener);
 			Command saveAllCmd = commandService.getCommand(IWorkbenchCommandConstants.FILE_SAVE_ALL);
 			saveAllCmd.removeExecutionListener(saveAllListener);
 		}
		// Ignore SWTException here, the display might be disposed already.
		try { clipboard.dispose(); } catch (SWTException e) { /* ignored on purpose */ }
 		clipboard = null;
 		plugin = null;
 		super.stop(context);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
 	 */
 	@Override
 	protected void initializeImageRegistry(ImageRegistry registry) {
 		URL url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "folder.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.FOLDER, ImageDescriptor.createFromURL(url));
 
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "root.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.ROOT, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "rootdrive.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.ROOT_DRIVE, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "rootdriveopen.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.ROOT_DRIVE_OPEN, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "synch_synch.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.COMPARE_EDITOR, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ32 + "replace_confirm.png"); //$NON-NLS-1$
 		registry.put(ImageConsts.REPLACE_FOLDER_CONFIRM, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ32 + "delete_readonly.png"); //$NON-NLS-1$
 		registry.put(ImageConsts.DELETE_READONLY_CONFIRM, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ32 + "banner.png"); //$NON-NLS-1$
 		registry.put(ImageConsts.BANNER_IMAGE, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "error.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.ERROR_IMAGE, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "pending.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.PENDING, ImageDescriptor.createFromURL(url));
 	}
 
 	/**
 	 * Loads the image registered under the specified key from the image
 	 * registry and returns the <code>Image</code> object instance.
 	 *
 	 * @param key The key the image is registered with.
 	 * @return The <code>Image</code> object instance or <code>null</code>.
 	 */
 	public static Image getImage(String key) {
 		return getDefault().getImageRegistry().get(key);
 	}
 
 	/**
 	 * Loads the image registered under the specified key from the image
 	 * registry and returns the <code>ImageDescriptor</code> object instance.
 	 *
 	 * @param key The key the image is registered with.
 	 * @return The <code>ImageDescriptor</code> object instance or <code>null</code>.
 	 */
 	public static ImageDescriptor getImageDescriptor(String key) {
 		return getDefault().getImageRegistry().getDescriptor(key);
 	}
 
 	/**
 	 * Loads the image given by the specified image descriptor from the image
 	 * registry. If the image has been loaded ones before already, the cached
 	 * <code>Image</code> object instance is returned. Otherwise, the <code>
 	 * Image</code> object instance will be created and cached before returned.
 	 *
 	 * @param descriptor The image descriptor.
 	 * @return The corresponding <code>Image</code> object instance or <code>null</code>.
 	 */
 	public static Image getSharedImage(AbstractImageDescriptor descriptor) {
 		ImageRegistry registry = getDefault().getImageRegistry();
 
 		String imageKey = descriptor.getDecriptorKey();
 		Image image = registry.get(imageKey);
 		if (image == null) {
 			registry.put(imageKey, descriptor);
 			image = registry.get(imageKey);
 		}
 
 		return image;
 	}
 }
