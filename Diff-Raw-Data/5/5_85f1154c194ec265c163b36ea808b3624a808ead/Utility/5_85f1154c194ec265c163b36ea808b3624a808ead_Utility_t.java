 // Utility.java
 package org.eclipse.stem.core;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This class contains common utility methods used in STEM.
  */
 public class Utility {
 
 	/**
 	 * EMF Save Options, for properly serializing to UTF-8
 	 */
 	private static final Map<String,String> EMF_SAVE_OPTIONS 
 		= new HashMap<String,String>();
 	static {
 		EMF_SAVE_OPTIONS.put(XMLResource.OPTION_ENCODING, "UTF-8");
 	}
 	
 	public final static String NESTING_WARNING = "Warning, possible scenario initialization problem detected. Check the nesting of your models";
 	public final static String URI_WARNING = "Warning, URI does not exist in Graph. Check the infector/innoculator URIs your models. ";
 	
 	
 	/**
 	 * Cached resource set
 	 */
 
 	public static ResourceSet resourceSet;
 	
 	static {
 		resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", STEMXMIResourceFactoryImpl.INSTANCE);
 		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("platform", STEMXMIResourceFactoryImpl.INSTANCE);
 	}
 	
 	/**
 	 * @param identifableURI
 	 *            the {@link URI} of file with a serialized {@link Identifiable}
 	 * @return the {@link Identifiable} de-serialized from the file, or
 	 *         <code>null</code> if an error occurred.
 	 */
 	public static Identifiable getIdentifiable(final URI identifableURI) {
 		Identifiable retValue = null;
 
 		try {
 			URI normalized = STEMURI.normalize(identifableURI);
 			Resource resource = resourceSet.getResource(normalized,
 					true);
 			if(resource.isModified()) {				
 				resource.unload();
 				resource.load(null);
 				if(resource.getErrors().size() > 0) {
 					for(Resource.Diagnostic d: resource.getErrors()) 
 						CorePlugin.logError(d.getMessage(), new Exception());
 				}
 			}
 			EList<EObject>cont = resource.getContents();
 			if(cont.size() == 0) {
 				int maxretry = 10;
 				while(cont.size() == 0)
 					Thread.yield(); // allow other thread to load resource
 				if(cont.size() == 0)
 					CorePlugin.logError("Unable to load content for resource "+normalized, new Exception());
 			}
 			retValue = (Identifiable) cont.get(0);
 		} catch (final Exception e) {
 			CorePlugin.logError(
 					"The serialized instance of an Identifiable at \""
 							+ identifableURI.toString()
 							+ "\" was not found or was of the wrong format", e);
 			retValue = null;
 		}
 		return retValue;
 	} // getIdentifiable
 
 	/**
 	 * Serialize an {@link Identifiable}
 	 * <p>
 	 * Note this code is copied from
 	 * org.eclipse.stem.internal.data.records.Record. That method should be
 	 * removed and this one used instead.
 	 * 
 	 * @param identifiable
 	 *            the {@link Identifiable} to be serialized.
 	 * @param serializationURI
 	 *            the {@link URI} that specifies where the {@link Identifiable}
 	 *            is to be serialized.
 	 * @throws IOException
 	 *             if there is a problem serializing the {@link Identifiable}
 	 */
 	public static void serializeIdentifiable(final Identifiable identifiable,
 			final URI serializationURI) throws IOException {
 		final List<Identifiable> set = new ArrayList<Identifiable>();
 		set.add(identifiable);
 		serializeIdentifiables(set, serializationURI);
 	} // serializeIdentifiable
 
 	/**
 	 * Serialize an {@link Identifiable}
 	 * 
 	 * @param identifiable
 	 *            the {@link Identifiable} to be serialized.
 	 * @param serializationURI
 	 *            the {@link URI} that specifies where the {@link Identifiable}
 	 *            is to be serialized.
 	 * @throws IOException
 	 *             if there is a problem serializing the {@link Identifiable}
 	 */
 	public static void serializeIdentifiables(final List<Identifiable> identifiables,
 			final URI serializationURI) throws IOException {
 
 		// This code could be running "stand alone" (i.e., not within eclipse),
 		// thus the default factories for the extensions are not registered as
 		// they would from their extension of
 		// "org.eclipse.emf.ecore.extension_parser". So we need to register them
 		// here.
 
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
 				STEMXMIResourceFactoryImpl.INSTANCE);
 		Resource.Factory.Registry.INSTANCE.getProtocolToFactoryMap().put(
 				"platform", STEMXMIResourceFactoryImpl.INSTANCE);
 
 		final ResourceSet resourceSet = new ResourceSetImpl();
 
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put("*", STEMXMIResourceFactoryImpl.INSTANCE);
 
 		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(
 				"platform", STEMXMIResourceFactoryImpl.INSTANCE);
 
 		final Resource resource = resourceSet.createResource(serializationURI);
 		
 		for (Identifiable id : identifiables) {
 			resource.getContents().add(id);
 		}
 
 		resource.save(EMF_SAVE_OPTIONS);
 	} // serializeIdentifiable
 	
 	/**
 	 * Common method for determining the geographic level of a geographic key (last segment of a URI)
 	 * @param key
 	 * @return the level of the key
 	 */
 	public  static int keyLevel(final String key) {
 		// null means unspecified and truly global for population models.
 		if(key == null || key.trim().equals("")) return -2;
		// ZZZ is special, it means the world
		if(key.equalsIgnoreCase("ZZZ")) return -1;
 		
 		int level = 0;
 		int start = 0;
 		for (int temp = key.indexOf("-"); temp > 0;) { //$NON-NLS-1$
 			level++;
 			start += temp + 1;
 			temp = key.substring(start).indexOf("-"); //$NON-NLS-1$
 		} // for
 		return level;
 	} // keyLevel
 	
 	/**
 	 * Displays a warning that the scenario composition may be invalid.
 	 * For example the graph nesting within a simulation may be invalid
 	 * An infector or inoculator may point to a node that does not exist.
 	 * @param message
 	 */
 	public static void displayScenarioCompositionWarning(final String message) {
 		try {
 			if(Display.getDefault() != null)
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						try {
 							final IWorkbenchWindow window = PlatformUI
 									.getWorkbench().getActiveWorkbenchWindow();
 //							final IStatus warning = new Status(IStatus.WARNING,
 //									CorePlugin.PLUGIN_ID, 1, message, null);
 //							ErrorDialog.openError(window.getShell(), null, null,
 //									warning);
 							String [] labels = new String[2];
 							labels[1]  = "Help";
 							labels[0] = "Okay";
 							MessageDialog dialog = new MessageDialog(window.getShell(), "Warning", null, message,
 									MessageDialog.WARNING, labels, 0) {
 								@Override
 								protected void buttonPressed(int buttonId) {
 									if(buttonId == 1)
 										PlatformUI.getWorkbench().getHelpSystem().displayHelp("org.eclipse.stem.doc.invalidnesting_contextid");
 									else
 										super.buttonPressed(buttonId);
 								}
 							};
 							dialog.open();
 						} catch(Exception e) {
 							// If we get this exception, it is because we're not running in
 							// eclipse.
 						}
 					} // run
 				});
 			
 			else CorePlugin.logError(message, null);
 			
 		} catch (final Error ncdfe) {
 			// Empty
 		} // catch
 	}
 	
 } // Utility
