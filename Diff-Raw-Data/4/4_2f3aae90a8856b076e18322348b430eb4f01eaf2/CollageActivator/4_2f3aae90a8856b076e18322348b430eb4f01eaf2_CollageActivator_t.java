 /*******************************************************************************
  * Copyright (c) 2011, 2012 Alex Bradley.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Alex Bradley    - initial implementation
  *******************************************************************************/
 package org.eclipselabs.collage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.resource.ColorRegistry;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchListener;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipselabs.collage.actions.FilesystemSchedulingRule;
 import org.eclipselabs.collage.model.CollageRoot;
 import org.eclipselabs.collage.util.CollageFontRegistry;
 import org.eclipselabs.collage.util.CollageUtilities;
 import org.osgi.framework.BundleContext;
 
 /**
  * Singleton activator for Collage plugin. Provides SWT image, colour and font registries
  * that are used throughout the Collage plugin and are also available for use in extensions.
  * Handles deserialization and serialization of Collage data on plugin load and unload.  
  * @author Alex Bradley
  */
 public final class CollageActivator extends AbstractUIPlugin implements IWorkbenchListener {
 	private enum CollageStorageState { UNAVAILABLE, BLANK, HAS_DATA }; 
 	
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipselabs.collage"; //$NON-NLS-1$
 	public static final String PLUGIN_NAME = "Collage";
 
 	// Persistent storage file
 	public static final String COLLAGE_STORAGE_FILE = "collage-storage.xml.gz";
 
 	// Save error messages
 	private static final String SAVE_FAILURE_STOP_SHUTDOWN_QUESTION = "Saving Collage data to %s was unsuccessful because %s. Would you like to stop the Eclipse shutdown and try to fix the problem?";
 	private static final String SAVE_FAILURE_FORCED_SHUTDOWN_ERROR = "Saving Collage data to %s was unsuccessful because %s. As this is a forced shutdown, Collage data may be lost.";
 
 	// Icons
 	public static final String UNKNOWN_SHAPE_ICON = "icons/unknown_shape.gif";
 	public static final String IBEAM_ICON = "icons/ibeam.png";
 	public static final String IMPORT_ICON = "icons/import.gif";
 	public static final String EXPORT_ICON = "icons/export.gif";
 	public static final String LAYERS_VIEW_ICON = "icons/layersview.png";
 	public static final String LAYER_VISIBLE_ICON = "icons/layer-visible.png";
 	public static final String LAYER_NEW_ICON = "icons/new_layer.gif";
 	
 	// The shared instance
 	private static CollageActivator plugin;
 	
 	private CollageRoot defaultCollageRoot = new CollageRoot();
 	
 	private ColorRegistry colorRegistry;
 	private CollageFontRegistry fontRegistry;
 	
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		
 		PlatformUI.getWorkbench().addWorkbenchListener(this);
 		
 		colorRegistry = new ColorRegistry();
 		fontRegistry = new CollageFontRegistry();
 		
 		loadDefaultCollageRoot();
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared plugin instance
 	 * @return the shared instance
 	 */
 	public static CollageActivator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Get the Collage root model element (which is loaded from storage when the plugin starts and
 	 * saved when the plugin stops.)
 	 * @return Collage model root
 	 */
 	public CollageRoot getDefaultCollageRoot() {
 		return defaultCollageRoot;
 	}
 
 	/**
 	 * Get font registry.
 	 * @return Common font registry for this plugin.
 	 */
 	public CollageFontRegistry getFontRegistry () {
 		return fontRegistry;
 	}
 
 	/**
 	 * Get colour registry.
 	 * @return Common colour registry for this plugin.
 	 */
 	public ColorRegistry getColorRegistry () {
 		return colorRegistry;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given
 	 * plug-in relative path
 	 *
 	 * @param path the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 
     /**
      * Return an {@link Image} for the image file at the given path in this plugin.
      * @param path image path
      * @return {@link Image} at the given path.
      */
     public static Image getImage (String path) {
     	return getImage(PLUGIN_ID, path);
     }
 
     /**
      * Return an {@link Image} for the image file at the given path in the given plugin.
      * @param plugin a plugin ID
      * @param path image path
      * @return {@link Image} at the given path in the given plugin
      */
     public static Image getImage (String plugin, String path) {
     	ImageRegistry imageReg = getDefault().getImageRegistry();
     	String key = plugin + ":" + path;
     	Image image = imageReg.get(key);
     	if (image == null) {
     		imageReg.put(key, imageDescriptorFromPlugin(plugin, path));
     		image = imageReg.get(key);
     	}
     	return image;
     }
     
     /**
      * Return an {@link Image} for the given {@link ImageDescriptor}.
      * @param descriptor an image descriptor
      * @return image for the given descriptor
      */
     public static Image getImage (ImageDescriptor descriptor) {
     	ImageRegistry imageReg = getDefault().getImageRegistry();
     	String key = "DESC:" + descriptor.hashCode();
     	Image image = imageReg.get(key);
     	if (image == null) {
     		imageReg.put(key, descriptor);
     		image = imageReg.get(key);
     	}
     	return image;
     }
 	
 	@Override
 	public boolean preShutdown(IWorkbench workbench, boolean forced) {
 		final File saveFile = getStateLocation().append(COLLAGE_STORAGE_FILE).toFile();
 		String path = saveFile.getAbsolutePath();
 		if (collageStorageAvailable() == CollageStorageState.UNAVAILABLE) {
 			return shouldShutdownProceed(path, "the file was not accessible", forced);
 		} else {
 			try {
 				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
 					@Override
 					public void run(IProgressMonitor monitor) throws InvocationTargetException,
 							InterruptedException {
 						IJobManager manager = Job.getJobManager();
 						ISchedulingRule rule = new FilesystemSchedulingRule(saveFile);
 						try {
 							manager.beginRule(rule, monitor);
 							GZIPOutputStream gzOutputStream = new GZIPOutputStream(new FileOutputStream(saveFile));
 							try {
 								defaultCollageRoot.saveTo(gzOutputStream);
 							} finally {
 								gzOutputStream.close();
 							}
 						} catch (Exception e) {
 							throw new InvocationTargetException(e);
 						} finally {
 							manager.endRule(rule);
 						}
 					}
 				});
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 				return shouldShutdownProceed(path, "XML serialization failed", forced);				
 			} catch (InterruptedException e) {
 				return shouldShutdownProceed(path, "the save operation was interrupted", forced);				
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void postShutdown(IWorkbench workbench) {
 	}
 
 	private void loadDefaultCollageRoot() {
 		switch (collageStorageAvailable()) {
 		case UNAVAILABLE:
 			showStorageUnavailableError();
 			break;
		case BLANK:
			// No action needed
			break;
 		case HAS_DATA:
 			File loadFile = getStateLocation().append(COLLAGE_STORAGE_FILE).toFile();
 			try {
 				IJobManager manager = Job.getJobManager();
 				ISchedulingRule rule = new FilesystemSchedulingRule(loadFile);
 				try {
 					manager.beginRule(rule, new NullProgressMonitor());
 					GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(loadFile));
 					try {
 						defaultCollageRoot = CollageRoot.loadFrom(inputStream);
 					} finally {
 						inputStream.close();
 					}
 				} finally {
 					manager.endRule(rule);
 				}
 				List<String> warnings = defaultCollageRoot.getDependencyWarnings();
 				if (!warnings.isEmpty()) {
 					CollageUtilities.showWarning(PLUGIN_NAME, 
 							String.format("Collage stored data loaded with the following warning%s:%n%s",
 									(warnings.size() == 1) ? "" : "s",
 									CollageUtilities.join(warnings, "\n")));
 				}
 			} catch (Exception e) {
 				File backupTarget = getStateLocation().append(getBackupFileName()).toFile();
 				try {
 					CollageUtilities.fileCopy(loadFile, backupTarget);
 					CollageUtilities.showError(PLUGIN_NAME, 
 							String.format("Unable to load Collage stored data:%n%s%nCollage storage has been backed up to \"%s\".", 
 									e.getMessage(), backupTarget.getAbsolutePath()));
 				} catch (Exception e1) {
 					CollageUtilities.showError(PLUGIN_NAME, 
 							String.format("Unable to load Collage stored data:%n%s%nWARNING: An attempt to back up Collage storage to \"%s\" failed!%nIf you want to recover your data, copy it from \"%s\" before closing Eclipse.", 
 									e.getMessage(), backupTarget.getAbsolutePath(), loadFile.getAbsolutePath()));
 				}
 			}
 			break;
 		}
 	}
 
 	/**
 	 * In the event of an error during save, ask the user if shutdown should be stopped.
 	 * @param path Path to Collage data location where save failed.
 	 * @param failureCause Cause of failure.
 	 * @param forced Is this a forced shutdown?
 	 * @return True if shutdown should proceed, false otherwise.
 	 */
 	private static boolean shouldShutdownProceed (String path, String failureCause, boolean forced) {
 		if (forced) {
 			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), PLUGIN_NAME, 
 					String.format(SAVE_FAILURE_FORCED_SHUTDOWN_ERROR, path, failureCause));
 			return true;
 		} else {
 			return !MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(), PLUGIN_NAME, 
 					String.format(SAVE_FAILURE_STOP_SHUTDOWN_QUESTION, path, failureCause));
 		}
 	}
 
 	private CollageStorageState collageStorageAvailable () {
 		try {
 			File collageFile = getStateLocation().append(COLLAGE_STORAGE_FILE).toFile();
 			CollageStorageState existsValue = CollageStorageState.HAS_DATA;
 			if (!collageFile.exists()) {
 				collageFile.createNewFile();
 				existsValue = CollageStorageState.BLANK;
 			}
 			
 			if (collageFile.canRead() && collageFile.canWrite()) {
 				return existsValue;
 			}
 		} catch (IOException e) {
 			// fall through and return unavailable
 		}
 		return CollageStorageState.UNAVAILABLE;
 	}
 	
 	private static void showStorageUnavailableError () {
 		CollageUtilities.showError(PLUGIN_NAME, 
 				String.format("Unable to access Collage storage in workspace path %s. Collage data will not be stored.", 
 						COLLAGE_STORAGE_FILE));
 	}
 
 	private static String getBackupFileName () {
 		return "collage-backup-" + System.currentTimeMillis() + ".xml.gz";
 	}
 }
