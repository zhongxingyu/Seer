 package org.funcish.core.impl;
 
 import java.lang.reflect.Method;
 
 public class MethodProxyFunction<T> extends AbstractFunction<T> {
 	private Object target;
 	private Method method;
 	
 	public MethodProxyFunction(Class<T> t, Method method, Object target) {
 		super(t, method.getParameterTypes());
 		this.target = target;
 		this.method = method;
 		if(!t.isAssignableFrom(method.getReturnType()))
 			throw new IllegalArgumentException();
 	}
 
 	@Override
 	public T call(Object... args) throws Exception {
 		return ret().cast(method.invoke(target, args));
 	}
 
 }
