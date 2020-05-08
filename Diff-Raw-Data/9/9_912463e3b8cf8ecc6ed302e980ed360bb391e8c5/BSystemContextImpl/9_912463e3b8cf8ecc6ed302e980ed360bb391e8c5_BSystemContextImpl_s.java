 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 
 import org.eclipse.b3.backend.core.B3NoSuchFunctionSignatureException;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BSystemContext;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 
 import org.eclipse.emf.ecore.EClass;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BSystem Context</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * </p>
  *
  * @generated
  */
 public class BSystemContextImpl extends BExecutionContextImpl implements BSystemContext {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected BSystemContextImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BSYSTEM_CONTEXT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * TODO: Translate exceptions from not finding method etc. into B3 Exceptions
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Object callFunction(String functionName, Object[] parameters, Type[] types, BExecutionContext ctx) throws Throwable {
 		Method m = findMethod(functionName, types);
 		if(m == null)
 			throw new B3NoSuchFunctionSignatureException(functionName, types);
 		
 		Object[] callParameters = new Object[parameters.length-1];
 		for(int i = 1; i < parameters.length; i++)
 			callParameters[i-1] = parameters[i];
 
 		try {
 			// invoke handles unwrap of non primitive types
			return m.invoke(parameters[0], callParameters);
 		} catch (InvocationTargetException e) {
 			throw e.getCause();
 		}
 	}
 	private Method findMethod(String functionName, Type[] types) throws Throwable {
 		// In Java, all calls refer to an instance/class (which must be in the first parameter) 
 		if(types.length == 0)
 			throw new B3NoSuchFunctionSignatureException(functionName, types);
 		
 		Class<?>[] parameterTypes = new Class<?>[types.length-1];
 		for(int i = 1; i < types.length; i++)
 			parameterTypes[i-1] = TypeUtils.getRaw(types[i]);
 		
 
 		Method m = null;
 		try {
 			m = TypeUtils.getRaw(types[0]).getMethod(functionName, parameterTypes);
 		} catch(NoSuchMethodException e) {
 			// TODO: The following "autoboxing to primitive" is not good enough as a method may
 			// have a mix of object and primitive types
 			
 			// may need to lookup using primitive types for int, long, boolean
 			for(int i = 0; i < parameterTypes.length;i++)
 				{
 				Class<?> t = parameterTypes[i];
 				if(t == Integer.class) 
 					parameterTypes[i] = int.class;
 				else if (t == Long.class) 
 					parameterTypes[i] = long.class;
 				else if(t == Double.class) 
 					parameterTypes[i] = double.class;
 				else if(t == Boolean.class) 
 					parameterTypes[i] = boolean.class;
 				}
 			// try again, but give up if it did not work.
 			m = TypeUtils.getRaw(types[0]).getMethod(functionName, parameterTypes);
 		}
 		return m;
 	}
 	@Override
 	public Type getDeclaredFunctionType(String functionName, Type[] types) throws Throwable {
 		Method m = findMethod(functionName, types);
 		if(m == null)
 			throw new B3NoSuchFunctionSignatureException(functionName, types);
 		Type t = m.getReturnType();
 		if(t == boolean.class)
 			return Boolean.class;
		if(t == Integer.class)
 			return Integer.class;
 		if(t == long.class)
 			return Long.class;
 		if(t == double.class)
 			return Double.class;
 		return t;
 	}
 } //BSystemContextImpl
