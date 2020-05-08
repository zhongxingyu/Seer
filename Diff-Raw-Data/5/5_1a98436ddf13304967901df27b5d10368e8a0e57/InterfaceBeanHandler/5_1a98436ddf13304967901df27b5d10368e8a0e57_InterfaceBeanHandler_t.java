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
 package org.eclipse.riena.core.extension;
 
 import static org.eclipse.riena.core.extension.InterfaceBeanHandler.MethodKind.OTHER;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.ContributorFactoryOSGi;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.variables.IStringVariableManager;
 import org.eclipse.core.variables.VariablesPlugin;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.internal.core.Activator;
 import org.osgi.framework.Bundle;
 import org.osgi.service.log.LogService;
 
 /**
  * InvocationHandler for proxies that map to configuration elements.
  */
 final class InterfaceBeanHandler implements InvocationHandler {
 
 	private final Class<?> interfaceType;
 	private final IConfigurationElement configurationElement;
 	private final boolean symbolReplace;
 	private final Map<Method, Result> resolved;
 
 	private final static Logger LOGGER = Activator.getDefault().getLogger(InterfaceBeanHandler.class);
 
 	InterfaceBeanHandler(final Class<?> interfaceType, final boolean symbolReplace,
 			final IConfigurationElement configurationElement) {
 		this.interfaceType = interfaceType;
 		this.configurationElement = configurationElement;
 		this.symbolReplace = symbolReplace;
 		this.resolved = new HashMap<Method, Result>();
 		if (!interfaceType.isAnnotationPresent(ExtensionInterface.class)) {
 			LOGGER.log(LogService.LOG_WARNING, "The interface '" + interfaceType.getName() //$NON-NLS-1$
 					+ "' is NOT annotated with @" + ExtensionInterface.class.getSimpleName() + " but it should!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	/*
 	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
 	 * java.lang.reflect.Method, java.lang.Object[])
 	 */
 	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
 		final MethodKind methodKind = MethodKind.of(method);
 		synchronized (resolved) {
 			Result result = resolved.get(method);
 			if (result == null) {
 				result = invoke(method, args, methodKind);
 				if (result.cash) {
 					resolved.put(method, result);
 				}
 			}
 			return result.object;
 		}
 	}
 
 	private Result invoke(final Method method, final Object[] args, final MethodKind methodKind) throws Throwable {
 		if (method.getParameterTypes().length == 0) {
 			if (method.getName().equals("toString")) { //$NON-NLS-1$
 				return Result.cache(proxiedToString());
 			} else if (method.getName().equals("hashCode")) { //$NON-NLS-1$
 				return Result.cache(proxiedHashCode());
 			}
 		}
 		if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == Object.class
 				&& method.getName().equals("equals")) { //$NON-NLS-1$
 			return Result.noCache(proxiedEquals(args[0]));
 		}
 		final Class<?> returnType = method.getReturnType();
 		final String name = getAttributeName(method, methodKind);
 		if (returnType == String.class) {
			return Result.noCache(modify(method.isAnnotationPresent(MapContent.class) ? configurationElement.getValue()
 					: configurationElement.getAttribute(name)));
 		}
 		if (returnType.isPrimitive()) {
			return Result.noCache(coerce(returnType, modify(configurationElement.getAttribute(name))));
 		}
 		if (returnType == Bundle.class) {
 			return Result.cache(ContributorFactoryOSGi.resolve(configurationElement.getContributor()));
 		}
 		if (returnType == Class.class) {
 			String value = configurationElement.getAttribute(name);
 			if (value == null) {
 				return Result.CACHED_NULL;
 			}
 			Bundle bundle = ContributorFactoryOSGi.resolve(configurationElement.getContributor());
 			if (bundle == null) {
 				return Result.CACHED_NULL;
 			}
 			// does it contain initialization data?
 			int colon = value.indexOf(':');
 			if (colon != -1) {
 				value = value.substring(0, colon);
 			}
 			return Result.cache(bundle.loadClass(value));
 		}
 		if (returnType.isInterface() && returnType.isAnnotationPresent(ExtensionInterface.class)) {
 			final IConfigurationElement[] cfgElements = configurationElement.getChildren(name);
 			if (cfgElements.length == 0) {
 				return Result.CACHED_NULL;
 			}
 			if (cfgElements.length == 1) {
 				return Result.cache(Proxy.newProxyInstance(returnType.getClassLoader(), new Class[] { returnType },
 						new InterfaceBeanHandler(returnType, symbolReplace, cfgElements[0])));
 			}
 			throw new IllegalStateException(
 					"Got more than one configuration element but the interface expected exactly one, .i.e no array type has been specified for: " //$NON-NLS-1$
 							+ method);
 		}
 		if (returnType.isArray() && returnType.getComponentType().isInterface()) {
 			final IConfigurationElement[] cfgElements = configurationElement.getChildren(name);
 			final Object[] result = (Object[]) Array.newInstance(returnType.getComponentType(), cfgElements.length);
 			for (int i = 0; i < cfgElements.length; i++) {
 				result[i] = Proxy.newProxyInstance(returnType.getComponentType().getClassLoader(),
 						new Class[] { returnType.getComponentType() }, new InterfaceBeanHandler(returnType
 								.getComponentType(), symbolReplace, cfgElements[i]));
 			}
 			return Result.cache(result);
 		}
 
 		if (method.getReturnType() == Void.class || (args != null && args.length > 0)) {
 			throw new UnsupportedOperationException("Can not handle method '" + method + "' in '" //$NON-NLS-1$ //$NON-NLS-2$
 					+ interfaceType.getName() + "'."); //$NON-NLS-1$
 		}
 		// Now try to create a fresh instance,i.e.
 		// createExecutableExtension() ()
 		if (configurationElement.getAttribute(name) == null) {
 			return Result.CACHED_NULL;
 		}
 		if (method.isAnnotationPresent(CreateLazy.class)) {
 			return Result.noCache(LazyExecutableExtension.newInstance(configurationElement, name));
 		}
 		return Result.noCache(configurationElement.createExecutableExtension(name));
 	}
 
 	/*
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean proxiedEquals(final Object obj) {
 		try {
 			InvocationHandler handler = Proxy.getInvocationHandler(obj);
 			if (handler instanceof InterfaceBeanHandler) {
 				return configurationElement.equals(((InterfaceBeanHandler) handler).configurationElement);
 			}
 		} catch (IllegalArgumentException e) {
 			// fall thru
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int proxiedHashCode() {
 		return configurationElement.hashCode();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	public String proxiedToString() {
 		final StringBuilder bob = new StringBuilder("Dynamic proxy for "); //$NON-NLS-1$
 		bob.append(interfaceType.getName()).append(':');
 		final String[] names = configurationElement.getAttributeNames();
 		for (String name : names) {
 			bob.append(name).append('=').append(configurationElement.getAttribute(name)).append(',');
 		}
 		bob.setLength(bob.length() - 1);
 		return bob.toString();
 	}
 
 	private String getAttributeName(final Method method, final MethodKind methodKind) {
 		final Annotation annotation = method.getAnnotation(MapName.class);
 		if (annotation != null) {
 			return ((MapName) annotation).value();
 		}
 
 		// No annotations
 		if (methodKind == OTHER) {
 			return null;
 		}
 		final String name = method.getName().substring(methodKind.prefix.length());
 		return name.substring(0, 1).toLowerCase() + name.substring(1);
 	}
 
 	private Object coerce(final Class<?> toType, final String value) {
 		if (toType == Long.TYPE) {
 			return Long.valueOf(value);
 		}
 		if (toType == Integer.TYPE) {
 			return Integer.valueOf(value);
 		}
 		if (toType == Boolean.TYPE) {
 			return Boolean.valueOf(value);
 		}
 		if (toType == Float.TYPE) {
 			return Float.valueOf(value);
 		}
 		if (toType == Double.TYPE) {
 			return Double.valueOf(value);
 		}
 		if (toType == Short.TYPE) {
 			return Short.valueOf(value);
 		}
 		if (toType == Character.TYPE) {
 			return Character.valueOf(value.charAt(0));
 		}
 		if (toType == Byte.TYPE) {
 			return Byte.valueOf(value);
 		}
 		return value;
 	}
 
 	private String modify(final String value) {
 		if (!symbolReplace || value == null) {
 			return value;
 		}
 
 		IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
 		if (variableManager == null) {
 			return value;
 		}
 
 		try {
 			return variableManager.performStringSubstitution(value);
 		} catch (CoreException e) {
 			LOGGER.log(LogService.LOG_ERROR, "Could not perfrom string substitution for '" + value + "' .", e); //$NON-NLS-1$ //$NON-NLS-2$
 			return value;
 		}
 	}
 
 	private final static class Result {
 
 		private final Object object;
 		private final boolean cash;
 
 		private static final Result CACHED_NULL = Result.cache(null);
 
 		private static Result noCache(final Object object) {
 			return new Result(object, false);
 		}
 
 		private static Result cache(final Object object) {
 			return new Result(object, true);
 		}
 
 		private Result(final Object object, final boolean cash) {
 			this.object = object;
 			this.cash = cash;
 		}
 	}
 
 	enum MethodKind {
 		GET("get"), IS("is"), CREATE("create"), OTHER; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 		private final String prefix;
 
 		private MethodKind(final String kind) {
 			this.prefix = kind;
 		}
 
 		private MethodKind() {
 			this.prefix = null;
 		}
 
 		/**
 		 * @param method
 		 * @return
 		 */
 		private static MethodKind of(final Method method) {
 			final String name = method.getName();
 			if (name.startsWith(GET.prefix)) {
 				return GET;
 			} else if (name.startsWith(IS.prefix)) {
 				return IS;
 			} else if (name.startsWith(CREATE.prefix)) {
 				return CREATE;
 			}
 			return OTHER;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Enum#toString()
 		 */
 		@Override
 		public String toString() {
 			return prefix;
 		}
 
 	}
 
 }
