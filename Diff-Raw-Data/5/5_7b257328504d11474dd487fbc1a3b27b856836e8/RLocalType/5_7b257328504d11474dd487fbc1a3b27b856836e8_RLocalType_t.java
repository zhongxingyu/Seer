 /*******************************************************************************
  * Copyright (c) 2012 Servoy
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Servoy - initial API and Implementation (Johan Compagner)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.typeinfo;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.dltk.compiler.problem.IValidationStatus;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.IValue;
 import org.eclipse.dltk.internal.javascript.validation.JavaScriptValidations;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.PhantomValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 
 /**
  * @author jcompagner
  */
 class RLocalType extends RType implements IRLocalType {
 
 	private final IValueReference functionValue;
 	private final String name;
 
 	RLocalType(String name, IValueReference functionValue) {
 		this.name = name;
 		this.functionValue = functionValue;
 	}
 
 	public IValueReference getValue() {
 		IValueCollection value = (IValueCollection) functionValue.getAttribute(
 				IReferenceAttributes.FUNCTION_SCOPE, false);
 		if (value != null) {
 			return value.getThis();
 		}
 		// backup value, target is not known to be a function.
 		return PhantomValueReference.REFERENCE;
 	}
 
 	public IValueReference getDirectChild(String name) {
 		final IValueReference value = getValue();
 		if (value.getDirectChildren(IValue.NO_LOCAL_TYPES).contains(name)) {
 			return value.getChild(name);
 		} else {
 			JSTypeSet declaredTypes = getValue().getDeclaredTypes();
 			HashSet<IRType> set = new HashSet<IRType>();
 			set.add(this);
 			return getChildFromDeclaredTypes(name, declaredTypes, set);
 		}
 	}
 
 	/**
 	 * @param name
 	 * @param declaredTypes
 	 * @param set
 	 */
 	private IValueReference getChildFromDeclaredTypes(String name,
 			JSTypeSet declaredTypes, HashSet<IRType> set) {
 		for (IRType irType : declaredTypes) {
 			if (irType instanceof RLocalType && set.add(irType)) {
 				IValueReference declaredValue = ((RLocalType) irType)
 						.getValue();
 				if (declaredValue.getDirectChildren(IValue.NO_LOCAL_TYPES)
 						.contains(name)) {
 					return declaredValue.getChild(name);
 				}
				IValueReference fromChild = getChildFromDeclaredTypes(name,
 						declaredValue.getDeclaredTypes(), set);
				if (fromChild != null)
					return fromChild;
 			}
 		}
 		return null;
 	}
 
 	public Set<String> getDirectChildren() {
 		Set<String> children = getValue().getDirectChildren(
 				IValue.NO_LOCAL_TYPES);
 		JSTypeSet declaredTypes = getValue().getDeclaredTypes();
 		HashSet<IRType> set = new HashSet<IRType>();
 		set.add(this);
 		fillDeclaredLocalTypesChildren(children, declaredTypes, set);
 		return children;
 	}
 
 	/**
 	 * @param children
 	 * @param declaredTypes
 	 * @param set
 	 */
 	private void fillDeclaredLocalTypesChildren(Set<String> children,
 			JSTypeSet declaredTypes, HashSet<IRType> set) {
 		for (IRType irType : declaredTypes) {
 			if (irType instanceof RLocalType && set.add(irType)) {
 				children.addAll(((RLocalType) irType).getValue()
 						.getDirectChildren(IValue.NO_LOCAL_TYPES));
 				fillDeclaredLocalTypesChildren(children, ((RLocalType) irType)
 						.getValue().getDeclaredTypes(), set);
 			}
 		}
 	}
 
 	public ReferenceLocation getReferenceLocation() {
 		return functionValue.getLocation();
 	}
 
 	@Override
 	public TypeCompatibility isAssignableFrom(IRType type) {
 		if (type instanceof IRLocalType) {
 			if (getReferenceLocation().equals(
 					((IRLocalType) type).getReferenceLocation())) {
 				return TypeCompatibility.TRUE;
 			}
 		}
 		return super.isAssignableFrom(type);
 	}
 
 	public IValidationStatus isAssignableFrom(IValueReference argument) {
 		if (argument == null)
 			return TypeCompatibility.TRUE;
 		Set<IRType> types = JavaScriptValidations.getTypes(argument);
 		for (IRType irType : types) {
 			if (irType instanceof IRLocalType) {
 				if (getReferenceLocation().equals(
 						((IRLocalType) irType).getReferenceLocation())) {
 					return TypeCompatibility.TRUE;
 				}
 			}
 		}
 		return TypeCompatibility.FALSE;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public int hashCode() {
 		return name.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof RLocalType) {
 			return getReferenceLocation().equals(
 					((RLocalType) obj).getReferenceLocation());
 		}
 		return false;
 	}
 }
