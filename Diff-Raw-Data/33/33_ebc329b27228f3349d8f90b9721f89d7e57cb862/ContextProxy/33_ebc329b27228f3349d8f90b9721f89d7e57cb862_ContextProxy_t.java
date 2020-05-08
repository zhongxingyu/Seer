 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.objecttransaction.context;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 /**
  * A Proxy for the management of a Context on the contained Object. All public
  * methods will be encapsulated by activating and passivating the corresponding
  * context. If the encapsulated object is a Context carrier, the context of this
  * object is used, otherwise is the context stored in the proxy is used. -> the
  * context of the object, if any there, has the higher priority
  * 
  * (It does not make sence to extend the manufactured proxy with an extra
  * interface. because it is not possible to access the corresponding methods)
  * 
  */
 
 public final class ContextProxy implements InvocationHandler {
 
 	// private final static ILogger LOGGER = LoggerAccessor.fetchLogger(
 	// ContextProxy.class );
 
 	private IContextProvider contextProvider;
 	private Object service;
 
 	/**
 	 * Create a Context Proxy
 	 */
 	private ContextProxy(Object pService, IContextProvider pContextProvider) {
 		service = pService;
 		contextProvider = pContextProvider;
 	}
 
 	/**
 	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
 	 *      java.lang.reflect.Method, java.lang.Object[])
 	 */
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		try {
 			ContextHelper.activateContext(contextProvider.getContext());
			// TODO not sure why accessible has to be true, fix some day
			method.setAccessible(true);
 
 			return method.invoke(service, args);
 		} catch (InvocationTargetException e) {
 			throw e.getTargetException();
 		} finally {
 			ContextHelper.passivateContext(contextProvider.getContext());
 		}
 	}
 
 	/**
 	 * Creates a new Proxy on the passed object. The return typ is created
 	 * automatically depending on the passed interface type. The genrics are
 	 * checking the consystency of the passe object and interface automatically.
 	 * The created proxy covers automatically the whole public interface of the
 	 * passed object
 	 * 
	 * @param <T>
	 *            - the expected interface typ equal also the type expected
	 * @param pObject
	 *            - the Object to create proxy on
	 * @param pContext
	 *            - the context to work on with this proxy
 	 * @return the Proxy
 	 */
 	@SuppressWarnings( { "unchecked" })
 	public static <T> T cover(T pObject, IContextProvider pContextProvider) {
 		assert pObject != null : "The object to proxy must not be null"; //$NON-NLS-1$
 		assert pContextProvider != null : "The context carrier must not be null"; //$NON-NLS-1$
		return (T) Proxy.newProxyInstance(pObject.getClass().getClassLoader(), getInterfaces(pObject.getClass()),
				new ContextProxy(pObject, pContextProvider));
 	}
 
 	/**
 	 * Creates a new Proxy on the passed object. The return typ is created
 	 * automatically depending on the passed interface type. The genrics are
 	 * checking the consystency of the passe object and interface automatically.
 	 * The created proxy covers automatically the whole public interface of the
 	 * passed object
 	 * 
	 * @param <T>
	 *            - the expected interface typ equals the type passe to
	 * @param pContext
	 *            - the context to work on with this proxy
 	 * @return the Proxy
 	 */
 	@SuppressWarnings( { "unchecked" })
 	public static <T extends IContextProvider> T cover(T pContextProvider) {
 		assert pContextProvider != null : "The context carrier must not be null"; //$NON-NLS-1$
		return (T) Proxy.newProxyInstance(pContextProvider.getClass().getClassLoader(), getInterfaces(pContextProvider
				.getClass()), new ContextProxy(pContextProvider, pContextProvider));
 	}
 
 	/**
 	 * Acertains all interfaces of the passed class
 	 * 
 	 * @param pClass
 	 *            the class to find interfaces from
 	 * @return an Array of interfaces
 	 */
 	private static Class<?>[] getInterfaces(Class<?> pClass) {
 		Class<?>[] result = pClass.getInterfaces();
 		Class<?> superclazz = pClass.getSuperclass();
 		if (superclazz != null) {
 			Class<?>[] superinterfaces = getInterfaces(superclazz);
 			if (superinterfaces.length > 0) {
 				Class<?>[] superresult = new Class[result.length + superinterfaces.length];
 				System.arraycopy(result, 0, superresult, 0, result.length);
 				System.arraycopy(superinterfaces, 0, superresult, result.length, superinterfaces.length);
 				result = superresult;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @return Returns the service.
 	 */
 	public Object getService() {
 		return service;
 	}
 
 }
