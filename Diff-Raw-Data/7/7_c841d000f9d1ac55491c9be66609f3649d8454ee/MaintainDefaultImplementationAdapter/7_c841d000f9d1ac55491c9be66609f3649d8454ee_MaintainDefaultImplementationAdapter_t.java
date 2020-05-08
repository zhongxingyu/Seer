 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.internal.jsflibraryregistry.adapter;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jst.jsf.core.internal.jsflibraryconfig.JSFLibraryRegistryUtil;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistry;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistryPackage;
 
 /**
  * EMF adapter that attempts to always maintain a default implementation
  * JSFLibrary upon addition and removal of JSFLibrary instances and upon
  * changing of a JSFLibrary instance's implementation property.
  * 
  * @author Ian Trimble - Oracle
  */
 public class MaintainDefaultImplementationAdapter extends AdapterImpl {
 
 	private static MaintainDefaultImplementationAdapter INSTANCE =
 		new MaintainDefaultImplementationAdapter();
 
 	/**
 	 * Gets the single instance of this adapter.
 	 * 
 	 * @return The single instance of this adapter.
 	 */
 	public static MaintainDefaultImplementationAdapter getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Called to notify this adapter that a change has occured.
 	 * 
 	 * @param notification EMF Notification instance
 	 */
 	public void notifyChanged(Notification notification) {
 		Object objNotifier = notification.getNotifier();
 		if (objNotifier instanceof JSFLibraryRegistry) {
 			int eventType = notification.getEventType();
 			switch (eventType) {
 				case Notification.ADD:
 					Object objNewValue = notification.getNewValue();
 					if (objNewValue instanceof JSFLibrary) {
 						libraryAdded((JSFLibrary)objNewValue);
 					}
 					break;
 				case Notification.REMOVE:
 					Object objOldValue = notification.getOldValue();
 					if (objOldValue instanceof JSFLibrary) {
 						libraryRemoved((JSFLibrary)objOldValue);
 					}
 					break;
 			}
 		} else if (objNotifier instanceof JSFLibrary) {
 			if (notification.getFeatureID(JSFLibrary.class) == JSFLibraryRegistryPackage.JSF_LIBRARY__IMPLEMENTATION) {
 				implementationFlagSet((JSFLibrary)objNotifier);
 			}
 		}
 	}
 
 	/**
 	 * Checks if the library added is an implementation and, if so, makes it
 	 * the default implementation if it is the only implementation.
 	 * 
 	 * @param library JSFLibrary instance
 	 */
 	protected void libraryAdded(JSFLibrary library) {
 		if (library != null && library.isImplementation()) {
 			JSFLibraryRegistry jsfLibReg = JSFLibraryRegistryUtil.getInstance().getJSFLibraryRegistry();
 			EList impls = jsfLibReg.getImplJSFLibraries();
 			if (impls.size() == 1) {
 				jsfLibReg.setDefaultImplementation(library);
 			}
 		}
 	}
 
 	/**
 	 * Checks if the library removed is the default implementation and, if so,
 	 * makes the first remaining implementation the new default or nulls out
 	 * the default implementation if no other implementation remains. 
 	 * 
 	 * @param library JSFLibrary instance
 	 */
 	protected void libraryRemoved(JSFLibrary library) {
 		if (library != null && library.isImplementation()) {
 			JSFLibraryRegistry jsfLibReg = JSFLibraryRegistryUtil.getInstance().getJSFLibraryRegistry();
 			JSFLibrary defaultImpl = jsfLibReg.getDefaultImplementation(); 
 			if (defaultImpl == null || library.getID().equals(defaultImpl.getID())) { 
 				setNewDefaultImplementation();
 			}
 		}
 	}
 
 	/**
 	 * Checks if the implementation flag of the JSFLibrary has been changed
 	 * such that it is now eligible to become the default implementation or
 	 * such that it is no longer eligible as the default implementation and
 	 * sets the default implementation appropriately. Note that the passed
 	 * JSFLibrary instance must have been added to the model before calling
 	 * this method for it to have any effect.
 	 * 
 	 * @param library JSFLibrary instance
 	 */
	private void implementationFlagSet(JSFLibrary library) {
 		JSFLibraryRegistry jsfLibReg = JSFLibraryRegistryUtil.getInstance().getJSFLibraryRegistry();
 		if (jsfLibReg != null) {
 			JSFLibrary defaultImpl = jsfLibReg.getDefaultImplementation();
 			if (
 					library.isImplementation() &&
 					defaultImpl == null
 			) {
 				jsfLibReg.setDefaultImplementation(library);
 			} else if (
 					!library.isImplementation() &&
					(defaultImpl != null && library.getID().equals(defaultImpl.getID())))
			{
 				setNewDefaultImplementation();
 			}
 		}
 	}
 
 	/**
 	 * Sets the first available JSFLibrary marked as an implementation as the
 	 * default implementation or sets the default implementation to null if no
 	 * JSFLibrary is marked as an implementation.
 	 */
 	protected void setNewDefaultImplementation() {
 		JSFLibraryRegistry jsfLibReg = JSFLibraryRegistryUtil.getInstance().getJSFLibraryRegistry();
 		EList impls = jsfLibReg.getImplJSFLibraries();
 		if (impls.size() > 0) {
 			jsfLibReg.setDefaultImplementation((JSFLibrary)impls.get(0));
 		} else {
 			jsfLibReg.setDefaultImplementation(null);
 		}
 	}
 }
