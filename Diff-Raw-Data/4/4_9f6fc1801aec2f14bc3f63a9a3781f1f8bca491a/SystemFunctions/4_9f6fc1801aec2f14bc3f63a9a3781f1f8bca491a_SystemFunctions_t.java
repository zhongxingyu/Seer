 package org.eclipse.b3.backend.functions;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.b3.backend.core.B3AssertionFailedException;
 import org.eclipse.b3.backend.core.B3Backend;
 import org.eclipse.b3.backend.evaluator.Any;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 
 public class SystemFunctions {
 
 	public static Boolean assertType(
 			@B3Backend(name="message")String message, 
 			@B3Backend(name="expectedType")Object expected, 
 			@B3Backend(name="actualType")Object actual) throws Throwable {
 			if(!(expected instanceof Type))
 				throw new B3AssertionFailedException(message, expected, actual.getClass());
 //			if(!TypeUtils.isAssignableFrom((Type)expected, actual.getClass()))
 			if(expected != actual.getClass())
 				throw new B3AssertionFailedException(message, expected, actual.getClass());
 			return Boolean.TRUE;
 	}
 	@B3Backend(systemFunction="_assertEquals")
 	public static Boolean assertEquals(
 			@B3Backend(name="message")String message, 
 			@B3Backend(name="expected")Object expected, 
 			@B3Backend(name="actual")Object actual) throws Throwable { return null; }
 		
 	@B3Backend(system=true)
 	public static Object _assertEquals(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		String message = (String)params[0];
		Object expected = params[1];
		Object actual = params[2];
 		
 		if(expected == actual)
 			return Boolean.TRUE;
 		if(expected == null || actual == null)
 			throw new B3AssertionFailedException(message, expected, actual);
 //		if(expected instanceof Number && actual instanceof Number) {
 //			if(!RelationalFunctions.equals((Number)expected, (Number)actual).booleanValue())
 //				throw new B3AssertionFailedException(message, expected, actual);
 //		}	
 		else if(ctx.callFunction("equals", new Object[] { params[1], params[2]}, 
 				new Type[] { types[1], types[2]}) != Boolean.TRUE)
 			throw new B3AssertionFailedException(message, expected, actual);
 		return Boolean.TRUE;
 	}
 	public static Boolean assertTrue(
 			@B3Backend(name="message")String message, 
 			@B3Backend(name="booleanExpr")Boolean booleanExpr 
 			) throws Throwable {
 		
 		if(booleanExpr == null)
 			throw new B3AssertionFailedException(message, Boolean.TRUE, null);
 		if(!booleanExpr.equals(Boolean.TRUE))
 			throw new B3AssertionFailedException(message, Boolean.TRUE, booleanExpr);
 		return Boolean.TRUE;
 	}
 	public static Boolean assertFalse(
 			@B3Backend(name="message")String message, 
 			@B3Backend(name="booleanExpr")Boolean booleanExpr 
 			) throws Throwable {
 		
 		if(booleanExpr == null)
 			throw new B3AssertionFailedException(message, Boolean.FALSE, null);
 		if(!booleanExpr.equals(Boolean.FALSE))
 			throw new B3AssertionFailedException(message, Boolean.TRUE, booleanExpr);
 		return Boolean.TRUE;
 	}
 	
 	@B3Backend(hideOriginal=true, funcNames={"instanceof"})
 	public static Boolean _instanceOf(@B3Backend(name="instance") Object o, @B3Backend(name="type")Type t) {
 		// TODO: this is cheating - it only compares raw
 		return TypeUtils.isAssignableFrom(t, o.getClass()) ? Boolean.TRUE : Boolean.FALSE;
 	}
 	@B3Backend(systemFunction="_whileTrue")
 	public static Object whileTrue(@B3Backend(name="doWhileBlock")BFunction func) { return null; }
 
 	@B3Backend(systemFunction="_whileTrue")
 	public static Object whileTrue(@B3Backend(name="conditionBlock")BFunction cond, 
 			@B3Backend(name="functionBlock")BFunction body) { return null; }
 
 	@B3Backend(systemFunction="_whileFalse")
 	public static Object whileFalse(@B3Backend(name="doWhileBlock")BFunction func) { return null; }
 
 	@B3Backend(systemFunction="_whileFalse")
 	public static Object whileFalse(@B3Backend(name="conditionBlock")BFunction cond, 
 			@B3Backend(name="functionBlock")BFunction body) { return null; }
 	
 	@B3Backend(system=true)
 	public static Object _whileTrue(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Object[] callParams = new Object[0];
 		Type[] typeParams = new Type[0];
 		
 		if(params.length == 2) {
 			BFunction cond = (BFunction)params[0];
 			BFunction body = (BFunction)params[1];
 			Object c = Boolean.TRUE;
 			Object e = Boolean.FALSE;
 			while(c == Boolean.TRUE) {
 				BExecutionContext useCtx = cond.getClosure();
 				useCtx = useCtx == null ? ctx.createOuterContext() : useCtx.createInnerContext();
 				c = cond.internalCall(useCtx, callParams, typeParams);
 				if(c != Boolean.TRUE)
 					return e;
 				useCtx = body.getClosure();
 				useCtx = useCtx == null ? ctx.createOuterContext() : useCtx.createInnerContext();
 				e = body.internalCall(useCtx, callParams, typeParams);
 			}
 		}
 		if(params.length == 1) {
 			BFunction body = (BFunction)params[0];
 			Object e = Boolean.TRUE;
 			while(e == Boolean.TRUE) {
 				BExecutionContext useCtx = body.getClosure();
 				useCtx = useCtx == null ? ctx.createOuterContext() : useCtx.createInnerContext();
 				e = body.internalCall(useCtx, callParams, typeParams);
 			}
 			return e;
 		}
 		throw new IllegalArgumentException("_whileTrue got wrong number of parameters: " + params.length + ", accepts 1 or 2 lambdas");
 	}
 	@B3Backend(system=true)
 	public static Object _whileFalse(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Object[] callParams = new Object[0];
 		Type[] typeParams = new Type[0];
 		
 		if(params.length == 2) {
 			BFunction cond = (BFunction)params[0];
 			BFunction body = (BFunction)params[1];
 			Object c = Boolean.FALSE;
 			Object e = Boolean.TRUE;
 			while(c == Boolean.FALSE) {
 				BExecutionContext useCtx = cond.getClosure();
 				useCtx = useCtx == null ? ctx.createOuterContext() : useCtx.createInnerContext();
 				c = cond.internalCall(useCtx, callParams, typeParams);
 				if(c != Boolean.FALSE)
 					return e;
 				useCtx = body.getClosure();
 				useCtx = useCtx == null ? ctx.createOuterContext() : useCtx.createInnerContext();
 				e = body.internalCall(useCtx, callParams, typeParams);
 				e = body.internalCall(useCtx, callParams, typeParams);
 			}
 		}
 		if(params.length == 1) {
 			BFunction body = (BFunction)params[0];
 			Object e = Boolean.FALSE;
 			while(e == Boolean.FALSE) {
 				BExecutionContext useCtx = body.getClosure();
 				useCtx = useCtx == null ? ctx.createOuterContext() : useCtx.createInnerContext();
 				e = body.internalCall(useCtx, callParams, typeParams);
 			}
 			return e;
 		}
 		throw new IllegalArgumentException("_whileFalse got wrong number of parameters: " + params.length + ", accepts 1 or 2 lambdas");
 	}
 	
 	@B3Backend(hideOriginal=true, funcNames={"do"}, systemFunction="__do", varargs=true)
 	public static Object _do(
 			@B3Backend(name="iterable")Iterable<?> iterable,
 			@B3Backend(name="paramsAnyAndFunction") Object... variable
 			) { return null; }
 
 	@B3Backend(system=true)
 	public static Object __do(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable{
 		Curry cur = hurryCurry(params, types, "do");
 
 		Object result = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null ? ctx.createOuterContext() : cur.closure.createInnerContext();
 			result = cur.lambda.internalCall(useCtx, cur.p, cur.t);
 			}
 		return result;
 	}
 
 	/**
 	 * Evaluate a function - the same as calling it. 
 	 *  
 	 * @param func - the function to evaluate
 	 * @param params - the parameters passed to the function
 	 * @return the result of calling the function
 	 */
 	@B3Backend(systemFunction="_evaluate", varargs=true)
 	public static final Object evaluate(@B3Backend(name="function")BFunction func, Object...params) 
 	{ return null; }
 
 	@B3Backend(system=true)
 	public static Object _evaluate(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		if(params == null || params.length <1 || !(params[0] instanceof BFunction))
 			throw new IllegalArgumentException("_evaluate called with too few/wrong arguments");
 		BFunction func = (BFunction)params[0];
 		int limit = params.length;
 		Object[] callparams = new Object[limit-1];
 		Type[] calltypes = new Type[limit-1];
 		for(int i = 1; i < limit; i++) {
 			callparams[i-1] = params[i];
 			calltypes[i-1] = types[i];
 		}
 		// If function comes with a closure, use it, else use a new outer context. (If the function does
 		// not have a closure it should not see the calling inner context).
 		//
 		BExecutionContext useCtx = func.getClosure();
 		useCtx = useCtx == null ? ctx.createOuterContext() : useCtx.createInnerContext();
 		return func.internalCall(useCtx, callparams, calltypes);
 	}
 	
 	@B3Backend(systemFunction="_exists", varargs=true)
 	public static Object exists(
 			@B3Backend(name="iterable")Iterable<?> iterable,
 			@B3Backend(name="paramsAnyAndFunction") Object... variable
 			) { return null; }
 
 	@B3Backend(system=true)
 	public static Object _exists(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable{
 		Curry cur = hurryCurry(params, types, "exists");
 
 		Object result = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null ? ctx.createOuterContext() : cur.closure.createInnerContext();
 			result = cur.lambda.internalCall(useCtx, cur.p, cur.t);
 			if(result instanceof Boolean && ((Boolean)result) == Boolean.TRUE)
 				return Boolean.TRUE;
 			}
 		return Boolean.FALSE;
 	}
 	@B3Backend(systemFunction="_all", varargs=true)
 	public static Object all(
 			@B3Backend(name="iterable")Iterable<?> iterable,
 			@B3Backend(name="paramsAnyAndFunction") Object... variable
 			) { return null; }
 
 	@B3Backend(system=true)
 	public static Object _all(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable{		
 		Curry cur = hurryCurry(params, types, "all");
 
 		Object result = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null ? ctx.createOuterContext() : cur.closure.createInnerContext();
 			result = cur.lambda.internalCall(useCtx, cur.p, cur.t);
 			if(!(result instanceof Boolean) || ((Boolean)result) == Boolean.FALSE)
 				return Boolean.FALSE;
 			}
 		return Boolean.TRUE;
 	}
 	
 	@B3Backend(systemFunction="_select", varargs=true)
 	public static List<Object> select(
 			@B3Backend(name="iterable")Iterable<?> iterable,
 			@B3Backend(name="paramsAnyAndFunction") Object... variable
 			) { return null; }
 
 	@B3Backend(system=true)
 	public static List<Object> _select(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable{		
 		Curry cur = hurryCurry(params, types, "select");
 
 		List<Object> result = new ArrayList<Object>();
 		Object cond = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null ? ctx.createOuterContext() : cur.closure.createInnerContext();
 			cond = cur.lambda.internalCall(useCtx, cur.p, cur.t);
 			if(cond instanceof Boolean && ((Boolean)cond) == Boolean.TRUE)
 				result.add(curryVal);
 			}
 		return result;
 	}
 	@B3Backend(systemFunction="_reject", varargs=true)
 	public static List<Object> reject(
 			@B3Backend(name="iterable")Iterable<?> iterable,
 			@B3Backend(name="paramsAnyAndFunction") Object... variable
 			) { return null; }
 
 	@B3Backend(system=true)
 	public static List<Object> _reject(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable{		
 		Curry cur = hurryCurry(params, types, "reject");
 
 		List<Object> result = new ArrayList<Object>();
 		Object cond = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null ? ctx.createOuterContext() : cur.closure.createInnerContext();
 			cond = cur.lambda.internalCall(useCtx, cur.p, cur.t);
 			// if true is returned, the element is not added
 			if(!(cond instanceof Boolean && ((Boolean)cond) == Boolean.TRUE))
 				result.add(curryVal);
 			}
 		return result;
 	}
 	
 	@B3Backend(systemFunction="_inject", varargs=true)
 	public static Object inject(
 			@B3Backend(name="iterable")Iterable<?> iterable,
 			@B3Backend(name="paramsAnyAndFunction") Object...variable
 			) { return null; }
 
 	@B3Backend(system=true)
 	public static Object _inject(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable{		
 		Curry cur = hurryCurry(params, types, "inject");
 
 		if( !((cur.p.length == 1 && cur.curry == -1) || (cur.p.length == 2 && cur.curry != -1)))
 			throw new IllegalArgumentException("inject only accepts (val, func), (val, _, func), or (_, val, func) as parameters");
 
 		// if curry not set, the call is on the form inject(val, func), and curry should be 1
 		if(cur.curry == -1)
 			cur.curry = 1;
 		if(cur.p.length != 2) {
 			cur.p = new Object[] { cur.p[0], null };
 			cur.t = new Type[] { cur.t[0], cur.lambda.getParameterTypes()[1]};
 		}
 		Object inject = null;
 		int injectPos = cur.curry == 0 ? 1 : 0;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null ? ctx.createOuterContext() : cur.closure.createInnerContext();
 			inject = cur.lambda.internalCall(useCtx, cur.p, cur.t);
 			cur.p[injectPos] = inject;
 			}
 		return inject;
 	}
 	
 	private static class Curry {
 		Iterator<?> itor;
 		int curry;
 		Object[] p;
 		Type[] t;
 		BFunction lambda;
 		BExecutionContext closure;
 	}
 	
 	/**
 	 * Organizes the parameters in a curried call.
 	 * @param params
 	 * @param types
 	 * @param name
 	 * @return A Curry with data for the function to use
 	 */
 	private static Curry hurryCurry(Object[] params, Type[] types, String name) 
 	{
 		Curry cur = new Curry();
 		
 		Iterable<?> iterable = (Iterable<?>)params[0];
 		int nParameters = params.length;
 		if(nParameters < 2)
 			throw new IllegalArgumentException("system function '"+name+"' expected 2 or more arguments");
 //		Object[] varargs = (Object[])params[1];
 //		int varargsLength = varargs.length;
 		if(!(params[nParameters-1] instanceof BFunction))
 			throw new IllegalArgumentException("system function '"+name+"' did not get a function as last argument");
 		cur.lambda = (BFunction)params[nParameters-1];
 		cur.closure = cur.lambda.getClosure();
 		cur.curry = -1; // unknown
 		int nLambdaParameters = 1; // default
 		if(nParameters == 2)
 			cur.curry = 0;
 		if(nParameters > 2) {
 			for(int i = 1; i < nParameters-1; i++)
 				if(params[i] == Any.ANY) {
 					cur.curry = i-1;
 					break;
 				}
 			nLambdaParameters = nParameters -2; // -1 for iterator, -1 for lambda
 		}
 		cur.itor = iterable.iterator();
 		cur.p = new Object[nLambdaParameters];
 		cur.t = new Type[nLambdaParameters];
 		for(int i = 0; i < nLambdaParameters; i++) {
 			cur.p[i] = params[i+1];
 			cur.t[i] = params[i+1].getClass(); 
 		}
 		if(cur.curry != -1) {
 			// TODO: get the type of the itor - cheating now by getting type of parameter 
 			// from called function
 			cur.t[cur.curry] = cur.lambda.getParameterTypes()[cur.curry];
 		}
 		return cur;
 	}
 }
