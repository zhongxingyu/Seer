 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.communication.core.factory;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashSet;
 
 import javax.security.auth.Subject;
 
 import org.eclipse.riena.communication.core.RemoteFailure;
 import org.eclipse.riena.communication.core.RemoteServiceDescription;
 import org.eclipse.riena.communication.core.hooks.AbstractHooksProxy;
 import org.eclipse.riena.communication.core.hooks.CallContext;
 import org.eclipse.riena.communication.core.hooks.ICallHook;
 import org.eclipse.riena.communication.core.hooks.ICallMessageContext;
 import org.eclipse.riena.communication.core.hooks.ICallMessageContextAccessor;
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.internal.communication.core.Activator;
 
 import com.caucho.hessian.client.HessianRuntimeException;
 import com.caucho.hessian.io.HessianProtocolException;
 
 public class CallHooksProxy extends AbstractHooksProxy {
 
 	private HashSet<ICallHook> callHooks = new HashSet<ICallHook>();
 	private RemoteServiceDescription rsd;
 	private ICallMessageContextAccessor mca;
 
 	public CallHooksProxy(Object proxiedInstance) {
 		super(proxiedInstance);
 		Inject.service(ICallHook.class.getName()).into(this).andStart(Activator.getDefault().getContext());
 	}
 
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		ICallMessageContext mc = null;
 		if (mca != null) {
 			mc = mca.createMessageContext(getProxiedInstance(), method.getName(), null);
 		}
 
 		CallContext context = new CallContext(rsd, method.getName(), mc);
 		if (callHooks.size() > 0) {
 			// call before service hook
 			for (ICallHook sHook : callHooks) {
 				sHook.beforeCall(context);
 			}
 		}
 
 		try {
 			return super.invoke(proxy, method, args);
 		} catch (InvocationTargetException e) {
 			// first check for specific Hessian exceptions
 			if (e.getTargetException() instanceof HessianRuntimeException
 					|| e.getTargetException() instanceof HessianProtocolException) {
 				Throwable cause = e.getTargetException();
 				while (cause.getCause() != null) {
 					if (cause.getCause() instanceof RemoteFailure) {
						throw new RemoteFailure(null, cause.getCause());
 					}
 					cause = cause.getCause();
 				}
				throw new RemoteFailure("", e.getTargetException()); //$NON-NLS-1$
 			}
 			// if runtime exception throw it anyway
 			if (e.getTargetException() instanceof RuntimeException) {
 				throw e.getTargetException();
 			}
 			// throw exception if it is in the method signature
 			for (Class<?> exceptionType : method.getExceptionTypes()) {
 				if (exceptionType.isAssignableFrom(e.getTargetException().getClass())) {
 					throw e.getTargetException();
 				}
 			}
 			// otherwise throw a remote failure
			throw new RemoteFailure("error while invoking remote service", e.getTargetException()); //$NON-NLS-1$
 			// throw e.getTargetException();
 		} finally {
 			context.getMessageContext().fireEndCall();
 			// call hooks after the call
 			if (callHooks.size() > 0) {
 				for (ICallHook sHook : callHooks) {
 					sHook.afterCall(context);
 				}
 			}
 		}
 	}
 
 	public void bind(ICallHook serviceHook) {
 		callHooks.add(serviceHook);
 	}
 
 	public void unbind(ICallHook serviceHook) {
 		callHooks.remove(serviceHook);
 	}
 
 	public Object getCallProxy() {
 		return getProxiedInstance();
 	}
 
 	public void setRemoteServiceDescription(RemoteServiceDescription rsd) {
 		this.rsd = rsd;
 	}
 
 	public void setMessageContextAccessor(ICallMessageContextAccessor mca) {
 		this.mca = mca;
 	}
 
 	@Override
 	public Subject getSubject() {
 		return null;
 	}
 }
