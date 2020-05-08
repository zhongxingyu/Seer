 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.otrunk;
 
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Field;
 import java.lang.reflect.Proxy;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.concord.framework.otrunk.OTController;
 import org.concord.framework.otrunk.OTControllerRegistry;
 import org.concord.framework.otrunk.OTControllerService;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectService;
 
 public class OTControllerServiceImpl implements OTControllerService {
 	/**
 	 * key is a OTObject
 	 * value is a realObject
 	 */
 	Map realObjectFromOTMap = new HashMap();
 
 	/**
 	 * key is the real object
 	 * value is the OTController
 	 */ 
 	Map controllerFromRealMap = new WeakHashMap();
 	
 	/**
 	 * key is the OTObject
 	 * value is the OTController
 	 */
 	Map controllerFromOTMap = new WeakHashMap();
 	
 	OTObjectService objectService;
 
 	OTControllerRegistry registry;
 	
 	public OTControllerServiceImpl(OTObjectService objectService, 
 		OTControllerRegistry registry){
 		this.objectService = objectService;
 		this.registry = registry;
 	}
 	
 	public Object getRealObject(OTObject otObject) {
 		Object realObject = internalGetRealObject(otObject);
 
 		if(realObject != null){
 			return realObject;
 		}
 
 		OTController view = getControllerInternal(otObject, null);
 		
 		// The view might be null if there was an error so this 
 		// will throw a NPE
 		realObject = view.createRealObject();
 			
 		setupRealObject(view, otObject, realObject);
 
 		return realObject;		
 	}
 
 
 	private final OTController getControllerInternal(OTObject otObject, Object realObject)
 	{
 		// check to see if we already have a view for this otObject
 		OTController controller = (OTController)controllerFromOTMap.get(otObject);
 		if (controller != null) {
 			// we already have a view for this otObject
 			// TODO there might be multiple views for a single object
 			// but there should be only one realObject per view
 			return controller;
 		}
 		
 		// need to find the OTController that can handle this OTObject
 		Class otObjectClass = null;
 		if(otObject != null) {
 			otObjectClass = otObject.getClass();
 		}
 		Class realObjectClass = null;
 		if(realObject != null) {
 			realObjectClass = realObject.getClass();
 		}
 		
 		controller =  createControllerInternal(otObjectClass, realObjectClass);
 		controller.initialize(otObject, this);
 
 		return controller;
 	}
 
 	
 	private final OTController createControllerInternal(Class otObjectClass, Class realObjectClass)
 	{
 		// need to find the OTController that can handle this OTObject
 		// need to go through all the parents of the otObjectClass
 		Class controllerClass = 
 			registry.getControllerClassByOTObjectClass(otObjectClass);
 
 		// try the first level of interfaces if the class can't be found
 		// this should really go up the inheritance tree, both interfaces and
 		// superclasses		
 		// just doing the first level will handle the proxy classes
 		Class [] interfaces = null;
 		
 		if(controllerClass == null) {
 			// I believe this will return only the first level of interfaces
 			interfaces = otObjectClass.getInterfaces();
 			for(int i=0; i<interfaces.length; i++){				
 				controllerClass = 
 					registry.getControllerClassByOTObjectClass(interfaces[i]);
 				if(controllerClass != null){
 					break;
 				}
 			}
 		}
 		
 		if(controllerClass == null){
 			// Can't find a controller for this otObject
 			String otObjectClassStr = "" + otObjectClass;
 			if(Proxy.isProxyClass(otObjectClass) && 
 					interfaces != null && interfaces.length > 0){
 				otObjectClassStr = "" + interfaces[0];
 			}
 			throw new RuntimeException("Can't find a controller for this otObject: " +
 					otObjectClassStr);
 		}
 		
 		try {
 			OTController controller = (OTController)controllerClass.newInstance();
 
 			return controller;
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 		return null;
 	}
 		
 	private final Object internalGetRealObject(OTObject otObject) {
 		Reference ref = (Reference)realObjectFromOTMap.get(otObject);
 		if(ref == null){
 			return null;			
 		}
 		
 		return ref.get();	
 	}
 	
 	private final void setupRealObject(OTController view, 
 			OTObject otObject, Object realObject){
 		// Save this object now to prevent infinite loops if
 		// there is a circular reference
 		realObjectFromOTMap.put(otObject, new WeakReference(realObject));
 		controllerFromRealMap.put(realObject, view);
 		controllerFromOTMap.put(otObject, view);
 		
 		view.loadRealObject(realObject);
 
 		view.registerRealObject(realObject);
 	}
 	
 	
 	public Object getRealObject(OTObject otObject, Object realObject) {
 		Object oldRealObject = internalGetRealObject(otObject);
 
 		if(oldRealObject != null){
 			// TODO what should we do here?
 			if(oldRealObject == realObject){
 				System.err.println("otObject already had the object");
 			} else {
 				System.err.println("otObject already had a different object");				
 			}
 		}
 
 		if(otObject == null){
 			String realObjectDesc = "null";
 			if(realObject != null){
 				realObjectDesc = "" + realObject.getClass() + ": " + realObject;
 			}
 			throw new IllegalArgumentException("null otObject for: " + 
 					realObjectDesc);
 		}
 		
 		OTController view = getControllerInternal(otObject, realObject);
 		
 		setupRealObject(view, otObject, realObject);
 		
 		return realObject;
 	}
 
 	public Class [] getRealObjectClasses(Class controllerClass) {
 		try {
 			Field field = controllerClass.getField("realObjectClasses");
 			return (Class [])field.get(null);
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchFieldException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	public Class getOTObjectClass(Class controllerClass) {
 		try {
 			if(!OTController.class.isAssignableFrom(controllerClass)){
 				throw new IllegalArgumentException("controllerClass doesn't implement "+ OTController.class);
 			}
 			
 			Field field = controllerClass.getField("otObjectClass");
 			return (Class)field.get(null);
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchFieldException e) {
 			// TODO Auto-generated catch block
 			System.err.println("invalid controller class: " + controllerClass);
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * This should actually receive the class of the OTObject, that will help look up the correct
 	 * OTController.  There could be mutiple controllers which handle a particular real object class. 
 	 * 
 	 * @see org.concord.framework.otrunk.OTControllerService#getOTObject(java.lang.Object)
 	 */
 	public OTObject getOTObject(Object realObject) {		
 		
 		// check if we already have a controller, if so then return its otObject
 		OTController controller = (OTController)controllerFromRealMap.get(realObject);
 		
 		if(controller != null) {
 			return controller.getOTObject();
 		}
 
 		// Figure out which otObjectClass we should be using
 		// this comes from the controller class we found
 			// TODO this should look for matches up the inheritance tree.
 		Class controllerClass = 
 			registry.getControllerClassByRealObjectClass(realObject.getClass());
 		
 		if(controllerClass == null) {
 			System.err.println("can't find controller class for realObject: " 
 					+ realObject.getClass());
 			return null;
 		}
 
 		Class otObjectClass = getOTObjectClass(controllerClass);
 
 		// if we don't have a view then we don't have the otObject
 		// so we need to make a new one.
 		OTObject otObject = null;
 		
 		try {
 			otObject = (OTObject)objectService.createObject(otObjectClass);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 
 		if(otObject == null) {
 			System.err.println("can't create registered OTObject class: " +
 					otObjectClass);
 		}
 		
 		// instanciate the controller
 		try {
 			controller = (OTController)controllerClass.newInstance();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 
 		// initialize it
 		controller.initialize(otObject, this);
 						
 		// store the relationship in the proper places
 		// this should be done before methods on the controller are called
 		// this should prevent infinite loops if there is a circular reference
 		controllerFromRealMap.put(realObject, controller);
 		controllerFromOTMap.put(otObject, controller);
 		realObjectFromOTMap.put(otObject, new WeakReference(realObject));
 		
 		// save the object so the otObject will pickup all the properties
 		// from the real object
 		controller.saveRealObject(realObject);
 
 		// in this case the realObject is already initialized
 		controller.registerRealObject(realObject);
 		
 		return otObject;
 	}
 	
 	public void saveRealObject(Object realObject, OTObject otObject) {
 		OTController controller = getControllerInternal(otObject, realObject);
 		
 		controller.saveRealObject(realObject);
 	}
 	
 	public void registerRealObject(Object realObject, OTObject otObject) {
 		OTController controller = getControllerInternal(otObject, realObject);
 		
 		controller.registerRealObject(realObject);		
 	}
 	
 	public void registerControllerClass(Class viewClass) {
 		registry.registerControllerClass(viewClass);
 	}
 	
 	/**
 	 * @see org.concord.framework.otrunk.OTControllerService#loadRealObject(org.concord.framework.otrunk.OTObject, java.lang.Object)
 	 */
 	public void loadRealObject(OTObject otObject, Object realObject)
 	{
 		OTController controller = getControllerInternal(otObject, realObject);
 		
 		controller.loadRealObject(realObject);
 	}
 
 }
