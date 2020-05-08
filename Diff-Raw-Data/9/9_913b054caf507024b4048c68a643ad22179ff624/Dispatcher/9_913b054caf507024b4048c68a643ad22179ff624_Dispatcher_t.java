 /**
  * Copyright (c) 2009 Aurora Software Technology Studio. All rights reserved.
  */
 package com.corona.async;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 
 import com.corona.context.ContextManagerFactory;
 
 /**
  * <p> </p>
  *
  * @author $Author$
  * @version $Id$
  */
 class Dispatcher implements InvocationHandler {
 
 	/**
	 * the current context manager factory
	 */
	private ContextManagerFactory contextManagerFactory;
	
	/**
 	 * the component that is in proxy state
 	 */
 	private Object component;
 	
 	/**
	 * @param contextManagerFactory the current context manager factory
 	 * @param component the component that is in a proxy state
 	 */
 	Dispatcher(final ContextManagerFactory contextManagerFactory, final Object component) {
		this.contextManagerFactory = contextManagerFactory;
 		this.component = component;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
 	 */
 	@Override
 	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
 		
 		return method.invoke(this.component, args);
 	}
 }
