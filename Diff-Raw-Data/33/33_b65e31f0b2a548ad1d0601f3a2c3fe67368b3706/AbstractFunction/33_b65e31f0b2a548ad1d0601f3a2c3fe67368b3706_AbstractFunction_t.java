 package org.funcish.core.impl;
 
 import java.util.Arrays;
 
 import org.funcish.core.fn.Function;
 
 public abstract class AbstractFunction<T> implements Function<T> {
 	protected static Object[] asArgs(Function<?> f, Object... values) {
 		if(values.length == f.args().length)
 			return values;
 		return Arrays.copyOf(values, f.args().length);
 	}
 	
 	private Class<T> ret;
 	private Class<?>[] args;
 	
 	public AbstractFunction(Class<T> ret, Class<?>[] fnargs) {
 		this.ret = ret;
 		this.args = fnargs;
 	}
 	
 	protected Object[] asArgs(Object... values) {
 		if(values.length == args().length)
 			return values;
 		return Arrays.copyOf(values, args().length);
 	}
 
 	public T call() throws Exception {
 		return call(asArgs(new Object[0]));
 	}
 
 	@Override
 	public Class<T> ret() {
 		return ret;
 	}
 	
 	public Class<?>[] args() {
 		return args;
 	}
 	
	protected String getClassName() {
		return getClass().getName();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(ret.getSimpleName());
		sb.append("(");
		String sep = "";
		for(Class<?> a : args()) {
			sb.append(sep);
			sb.append(a.getSimpleName());
			sep = ",";
		}
		sb.append("):");
		sb.append(getClassName());
		return sb.toString();
	}
 }
