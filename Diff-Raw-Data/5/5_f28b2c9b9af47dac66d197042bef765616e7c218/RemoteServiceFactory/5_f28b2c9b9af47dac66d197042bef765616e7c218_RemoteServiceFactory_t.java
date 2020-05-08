 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.communication.core.factory;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.HashMap;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 
import org.eclipse.core.runtime.Assert;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.communication.core.IRemoteServiceReference;
 import org.eclipse.riena.communication.core.IRemoteServiceRegistration;
 import org.eclipse.riena.communication.core.IRemoteServiceRegistry;
 import org.eclipse.riena.communication.core.RemoteServiceDescription;
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.core.wire.InjectExtension;
 import org.eclipse.riena.core.wire.InjectService;
 import org.eclipse.riena.core.wire.Wire;
 import org.eclipse.riena.internal.communication.core.Activator;
 import org.eclipse.riena.internal.communication.core.factory.CallHooksProxy;
 
 /**
  * The IRemoteServiceFactory creates a {@link IRemoteServiceReference} for given
  * service end point description. The IRemoteServiceReference holds a
  * serviceInstance reference instance to the service end point. The
  * RemoteService Factory can register an IRemoteServiceReference into the
  * {@link IRemoteServiceRegistry}. To create a {@link IRemoteServiceReference}
  * call {@link #createProxy(..)}. To create and register a "remote" OSGi Service
  * in one step call {@link #createAndRegisterProxy(..)}
  * <p>
  * The RemoteServiceFactory does not create a IRemoteServiceReference itself.
  * This is delegated to protocol specific implementations of
  * {@code IRemoteServiceFactory}. These implementations are defined with the
  * extension point "remoteservicefactory".
  * <p>
  * This RemoteServiceFactory does nothing if no protocol specific
  * IRemoteServiceFactory is available.
  * <p>
  * <b>NOTE</b><br>
  * The Riena communication bundle content includes generic class loading and
  * object instantiation or delegates this behavior to other Riena communication
  * bundles. Riena supports Eclipse-BuddyPolicy concept.
  * 
  * @see <a
  *      href="http://wiki.eclipse.org/Riena_Getting_started_remoteservices">Riena
  *      Wiki</a>
  */
 public class RemoteServiceFactory {
 
 	private static boolean wired;
 	private static IRemoteServiceRegistry registry;
 	private static HashMap<String, IRemoteServiceFactory> remoteServiceFactoryImplementations = null;
 	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), RemoteServiceFactory.class);
 
 	/**
 	 * Creates a RemoteServiceFactory instance with the default bundle context.
 	 * Prerequisite: application bundle should registered as
 	 * Eclipse-RegisterBuddy. Sample Manifest.mf of foo.myapplication.api:
 	 * 
 	 * Eclipse-RegisterBuddy: org.eclipse.riena.communication.core
 	 * 
 	 */
 	public RemoteServiceFactory() {
 		synchronized (RemoteServiceFactory.class) {
 			if (!wired) {
 				wired = true;
				Assert.isNotNull(Wire.instance(this).andStart(), "Wiring of " + RemoteServiceFactory.class.getName() //$NON-NLS-1$
						+ " was not possible because this bundle has not been started."); //$NON-NLS-1$
 			}
 		}
 	}
 
 	@InjectService(useRanking = true)
 	public void bind(final IRemoteServiceRegistry registryParm) {
 		registry = registryParm;
 	}
 
 	public void unbind(final IRemoteServiceRegistry registryParm) {
 		if (registry == registryParm) {
 			registry = null;
 		}
 	}
 
 	/**
 	 * @since 1.2
 	 */
 	@InjectExtension
 	public void update(final IRemoteServiceFactoryExtension[] factories) {
 		remoteServiceFactoryImplementations = new HashMap<String, IRemoteServiceFactory>();
 		for (final IRemoteServiceFactoryExtension factory : factories) {
 			remoteServiceFactoryImplementations.put(factory.getProtocol(), factory.createRemoteServiceFactory());
 		}
 	}
 
 	/**
 	 * Creates and registers a protocol specific remote service reference and
 	 * registers the reference into the {@link IRemoteServiceRegistry}. A
 	 * registered reference becomes automatically registered as "remote" OSGi
 	 * Service within the local OSGi container. Answers the registration object
 	 * for the reference. If no protocol specific {@link IRemoteServiceFactory}
 	 * OSGI Service available answers <code>null</code>.<br>
 	 * <p>
 	 * 
 	 * @param interfaceClass
 	 *            the interface of the OSGi Service
 	 * @param url
 	 *            the URL of the remote service location
 	 * @param protocol
 	 *            the used protocol
 	 * @param context
 	 *            the context in which the proxy is registered
 	 * @return the registration object or <code>null</code>
 	 */
 	public IRemoteServiceRegistration createAndRegisterProxy(final Class<?> interfaceClass, final String url,
 			final String protocol, final BundleContext context) {
 		final RemoteServiceDescription rsd = createDescription(interfaceClass, url, protocol, context.getBundle());
 		return createAndRegisterProxy(rsd, context);
 	}
 
 	/**
 	 * Creates and registers a protocol specific remote service reference and
 	 * registers the reference into the {@link IRemoteServiceRegistry}. A
 	 * registered reference becomes automatically registered as "remote" OSGi
 	 * Service within the local OSGi container. Answers the registration object
 	 * for the reference. If no protocol specific {@link IRemoteServiceFactory}
 	 * OSGI Service available answers <code>null</code>.<br>
 	 * <p>
 	 * The hostId identifies who is responsible for this remote service
 	 * registration
 	 * 
 	 * @param rsDesc
 	 *            the remote service description with all the metadata about the
 	 *            remote service
 	 * @param context
 	 *            the context in which the proxy is registered
 	 * @return the registration object or <code>null</code>
 	 */
 	public IRemoteServiceRegistration createAndRegisterProxy(final RemoteServiceDescription rsDesc,
 			final BundleContext context) {
 		// create serviceInstance first
 		IRemoteServiceReference rsRef = createProxy(rsDesc);
 		if (rsRef == null) {
 			rsRef = createLazyProxy(rsDesc);
 		}
 		if (rsRef == null) {
 			LOGGER.log(LogService.LOG_ERROR,
 					"could not create serviceInstance (neither serviceInstance nor lazy serviceInstance) for " //$NON-NLS-1$
 							+ rsDesc);
 			return null;
 		}
 		// register directly
 		if (registry != null) {
 			final IRemoteServiceRegistration reg = registry.registerService(rsRef, context);
 			return reg;
 		}
 		return null;
 	}
 
 	/**
 	 * Creates a protocol specific serviceInstance reference
 	 * (IRemoteServiceReference) with given end point parameters. Answers the
 	 * IRemoteServiceReference. If no protocol specific
 	 * {@link IRemoteServiceFactory} OSGI Service available answers
 	 * <code>null</code>.
 	 * 
 	 * @param interfaceClass
 	 * @param url
 	 * @param protocol
 	 * @return the serviceInstance references or <code>null</code>
 	 * @since 3.0
 	 */
 	public IRemoteServiceReference createProxy(final Class<?> interfaceClass, final String url, final String protocol,
 			final BundleContext context) {
 		return createProxy(createDescription(interfaceClass, url, protocol, context.getBundle()));
 	}
 
 	private RemoteServiceDescription createDescription(final Class<?> interfaceClass, final String url,
 			final String protocol, final Bundle bundle) {
 		return new RemoteServiceDescription(interfaceClass, url, protocol, bundle);
 	}
 
 	/**
 	 * Creates a protocol specific IRemoteServcieReference for the given end
 	 * point description. Answers the IRemoteServiceReference. If no end point
 	 * specific {@link IRemoteServiceFactory} OSGI Service available answers
 	 * <code>null</code>.
 	 * 
 	 * @param rsd
 	 * @return the serviceInstance references or <code>null</code>
 	 */
 	public IRemoteServiceReference createProxy(final RemoteServiceDescription rsd) {
 		if (!RienaStatus.isActive()) {
 			LOGGER.log(LogService.LOG_WARNING, "riena.core is not started. This may probably not work."); //$NON-NLS-1$
 		}
 
 		if (rsd.getProtocol() == null) {
 			return null;
 		}
 
 		// find a factory for this specific protocol
 		final IRemoteServiceFactory factory = remoteServiceFactoryImplementations.get(rsd.getProtocol());
 
 		// could not get instance for existing reference
 		if (factory == null) {
 			LOGGER.log(LogService.LOG_WARNING, "no IRemoteServiceFactory extension available protocol [" //$NON-NLS-1$
 					+ rsd.getProtocol() + "] id [" + rsd.getServiceInterfaceClassName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
 			return null;
 		}
 		LOGGER.log(LogService.LOG_INFO, "found protocol [" + rsd.getProtocol() + "] " + factory); //$NON-NLS-1$ //$NON-NLS-2$
 
 		// ask factory to create a serviceInstance for me, and intercept the
 		// calls with a CallHooksProxy instance
 		final IRemoteServiceReference rsr = factory.createProxy(rsd);
 		final CallHooksProxy callHooksProxy = new CallHooksProxy(rsr.getServiceInstance());
 		callHooksProxy.setRemoteServiceDescription(rsd);
 		callHooksProxy.setMessageContextAccessor(factory.getMessageContextAccessor());
 		rsr.setServiceInstance(Proxy.newProxyInstance(rsd.getServiceInterfaceClass().getClassLoader(),
 				new Class[] { rsd.getServiceInterfaceClass() }, callHooksProxy));
 		return rsr;
 	}
 
 	private IRemoteServiceReference createLazyProxy(final RemoteServiceDescription rsd) {
 		//		try {
 		final LazyProxyHandler lazyProxyHandler = new LazyProxyHandler(rsd);
 		final Class<?> serviceClass = rsd.getServiceInterfaceClass();
 		if (serviceClass == null) {
 			LOGGER.log(LogService.LOG_ERROR, "Could not load service interface class '" //$NON-NLS-1$
 					+ rsd.getServiceInterfaceClassName() + "'."); //$NON-NLS-1$
 			return null;
 		}
 		final Object serviceInstance = Proxy.newProxyInstance(rsd.getServiceClassLoader(),
 				new Class[] { serviceClass }, lazyProxyHandler);
 		final LazyRemoteServiceReference ref = new LazyRemoteServiceReference(serviceInstance,
 				rsd.getServiceInterfaceClassName(), rsd);
 		lazyProxyHandler.setLazyRemoteServiceReference(ref);
 		return ref;
 	}
 
 	/**
 	 * Load class types for the given intefaceClassName.
 	 * 
 	 * @param interfaceClassName
 	 * @return the class type for the given class Name
 	 * 
 	 * @throws ClassNotFoundException
 	 */
 	public Class<?> loadClass(final String interfaceClassName) throws ClassNotFoundException {
 
 		return getClass().getClassLoader().loadClass(interfaceClassName);
 	}
 
 	private class LazyProxyHandler implements InvocationHandler {
 
 		private InvocationHandler delegateHandler;
 		private final RemoteServiceDescription rsd;
 		private LazyRemoteServiceReference lazyRemoteServiceReference;
 
 		protected LazyProxyHandler(final RemoteServiceDescription rsd) {
 			super();
 			this.rsd = rsd;
 		}
 
 		public void setLazyRemoteServiceReference(final LazyRemoteServiceReference ref) {
 			this.lazyRemoteServiceReference = ref;
 		}
 
 		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
 			if (delegateHandler == null) {
 				final IRemoteServiceReference ref = createProxy(rsd);
 				if (ref == null) {
 					throw new RuntimeException("LazyProxy: missing IRemoteServiceFactory to create proxy for " //$NON-NLS-1$
 							+ "protocol=" + rsd.getProtocol() + " url=" + rsd.getURL() + " interface=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 							+ rsd.getServiceInterfaceClassName());
 				}
 				final Object proxyInstance = ref.getServiceInstance();
 				delegateHandler = Proxy.getInvocationHandler(proxyInstance);
 				lazyRemoteServiceReference.setDelegateRef(ref);
 			}
 
 			return delegateHandler.invoke(proxy, method, args);
 		}
 
 	}
 
 	private static class LazyRemoteServiceReference implements IRemoteServiceReference {
 
 		private final Object serviceInstance;
 		private final String serviceClass;
 		private IRemoteServiceReference delegateReference;
 		private BundleContext tempBundleContext;
 		private final RemoteServiceDescription rsd;
 		private ServiceRegistration serviceRegistration;
 
 		protected LazyRemoteServiceReference(final Object serviceInstance, final String serviceClass,
 				final RemoteServiceDescription rsd) {
 			super();
 			this.serviceInstance = serviceInstance;
 			this.serviceClass = serviceClass;
 			this.rsd = rsd;
 		}
 
 		private void setDelegateRef(final IRemoteServiceReference delegateRef) {
 			this.delegateReference = delegateRef;
 			if (tempBundleContext != null) {
 				delegateRef.setContext(tempBundleContext);
 			}
 		}
 
 		public void dispose() {
 			if (delegateReference != null) {
 				delegateReference.dispose();
 			}
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (delegateReference != null) {
 				return delegateReference.equals(obj);
 			}
 			return false;
 		}
 
 		public RemoteServiceDescription getDescription() {
 			if (delegateReference == null) {
 				return rsd;
 			}
 			return delegateReference.getDescription();
 		}
 
 		public BundleContext getContext() {
 			if (delegateReference == null) {
 				return tempBundleContext;
 			}
 			return delegateReference.getContext();
 		}
 
 		public Object getServiceInstance() {
 			if (delegateReference == null) {
 				return serviceInstance;
 			}
 			return delegateReference.getServiceInstance();
 		}
 
 		public String getServiceInterfaceClassName() {
 			if (delegateReference == null) {
 				return serviceClass;
 			}
 			return delegateReference.getServiceInterfaceClassName();
 		}
 
 		public ServiceRegistration getServiceRegistration() {
 			return serviceRegistration;
 		}
 
 		public String getURL() {
 			if (delegateReference != null) {
 				return delegateReference.getURL();
 			}
 			return null;
 		}
 
 		@Override
 		public int hashCode() {
 			if (delegateReference != null) {
 				return delegateReference.hashCode();
 			}
 			return this.getClass().hashCode();
 		}
 
 		public void setContext(final BundleContext context) {
 			if (delegateReference == null) {
 				tempBundleContext = context;
 			} else {
 				delegateReference.setContext(context);
 			}
 		}
 
 		public void setServiceInstance(final Object serviceInstance) {
 			if (delegateReference != null) {
 				delegateReference.setServiceInstance(serviceInstance);
 			} else {
 				throw new RuntimeException(
 						"trying to set serviceInstance for lazyRemoteServiceReference with no delegate"); //$NON-NLS-1$
 			}
 		}
 
 		public void setServiceRegistration(final ServiceRegistration serviceRegistration) {
 			this.serviceRegistration = serviceRegistration;
 		}
 
 		@Override
 		public String toString() {
 			if (delegateReference != null) {
 				return delegateReference.toString();
 			}
 			String symbolicName = "no context"; //$NON-NLS-1$
 			if (tempBundleContext != null) {
 				symbolicName = tempBundleContext.getBundle().getSymbolicName();
 			}
 			return "(lazyreference) context for bundle=" + symbolicName + ", end point=(" + getDescription() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 
 	}
 
 }
