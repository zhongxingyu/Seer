 /*******************************************************************************
  * $URL: svn://176.28.48.244/projects/softmodeler/trunk/ui/plugins/com.softmodeler.ui/src/com/softmodeler/ui/BindingData.java $
  *
  * Copyright (c) 2007 henzler informatik gmbh, CH-4106 Therwil
  *******************************************************************************/
 package com.softmodeler.ui;
 
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 /**
  * Simple data store for binding Data
  * 
  * @author created by Author: fdo, last update by $Author: fdo $
  * @version $Revision: 9874 $, $Date: 2011-02-07 18:10:56 +0100 (Mo, 07 Feb 2011) $
  */
 public class BindingData {
 	/** binding object */
 	private EObject bindingObject;
 	/** binding feature */
 	private EStructuralFeature bindingFeature;
 
 	/**
 	 * constructor
 	 * 
 	 * @param bindingObject
 	 * @param bindingFeature
 	 */
 	public BindingData(EObject bindingObject, EStructuralFeature bindingFeature) {
 		this.bindingObject = bindingObject;
 		this.bindingFeature = bindingFeature;
 	}
 
 	/**
 	 * returns the binding object
 	 * 
 	 * @return
 	 */
 	public EObject getObject() {
 		return bindingObject;
 	}
 
 	/**
 	 * returns the binding feature
 	 * 
 	 * @return
 	 */
 	public EStructuralFeature getFeature() {
 		return bindingFeature;
 	}
 
 	/**
 	 * returns the value of the objects feature
 	 * 
 	 * @return
 	 */
 	public Object getValue() {
 		return getObject().eGet(getFeature());
 	}
 
 	/**
 	 * sets the value of the object feature
 	 * 
 	 * @param value new value
 	 */
 	public void setValue(Object value) {
 		if (value != null) {
 			getObject().eSet(getFeature(), value);
 		} else {
 			getObject().eUnset(getFeature());
 		}
 	}
 
 	/**
 	 * Finds and returns the business object attribute/feature that should be used for binding
 	 * 
 	 * @param binding
 	 * @param object
 	 * @return
 	 */
 	public static BindingData getBindingData(String binding, Map<String, EObject> bindingObjects) {
 		if (binding != null) {
 			String objectKey = null;
 			String bindingName = null;
 
 			if (binding.indexOf(".") == -1) { //$NON-NLS-1$
 				objectKey = "obj"; //$NON-NLS-1$
 				bindingName = binding;
 			} else {
 				String[] keys = binding.split("\\."); //$NON-NLS-1$
 				objectKey = keys[0];
 				bindingName = keys[1];
 			}
 
 			EObject bindingObject = bindingObjects.get(objectKey);
 			if (bindingObject == null) {
 				throw new UIException("Object not found for binding: {0}", binding); //$NON-NLS-1$
 			}
 			EStructuralFeature bindingFeature = bindingObject.eClass().getEStructuralFeature(bindingName);
 			if (bindingFeature == null) {
 				throw new UIException("Attribute {0} not found for binding in: {1}, {2}", bindingName, bindingObject, //$NON-NLS-1$
 						bindingObject.eClass());
 			}
 			return new BindingData(bindingObject, bindingFeature);
 		}
 		return null;
 	}
 }
