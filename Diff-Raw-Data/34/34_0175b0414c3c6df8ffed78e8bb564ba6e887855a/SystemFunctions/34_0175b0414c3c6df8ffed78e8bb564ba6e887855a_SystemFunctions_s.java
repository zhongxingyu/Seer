 package org.eclipse.b3.backend.functions;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.b3.backend.core.B3Backend;
 import org.eclipse.b3.backend.core.B3ContextAccess;
 import org.eclipse.b3.backend.core.IB3Evaluator;
 import org.eclipse.b3.backend.core.datatypes.Any;
 import org.eclipse.b3.backend.core.exceptions.B3AssertionFailedException;
 import org.eclipse.b3.backend.core.internal.B3BackendActivator;
 import org.eclipse.b3.backend.evaluator.b3backend.B3Function;
 import org.eclipse.b3.backend.evaluator.b3backend.B3FunctionType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendFactory;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.b3.backend.inference.FunctionUtils;
 import org.eclipse.b3.backend.inference.ITypeProvider;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 
 public class SystemFunctions {
 
 	private static class Curry {
 		Iterator<?> itor;
 
 		int curry;
 
 		Object[] p;
 
 		Type[] t;
 
 		BFunction lambda;
 
 		BExecutionContext closure;
 	}
 
 	public static final IStatus EMPTY_ERROR = new Status(
 		IStatus.ERROR, B3BackendActivator.PLUGIN_IN, IStatus.OK, "", null);
 
 	public static final IStatus EMPTY_INFO = new Status(
 		IStatus.INFO, B3BackendActivator.PLUGIN_IN, IStatus.OK, "", null);
 
 	public static final IStatus EMPTY_WARNING = new Status(
 		IStatus.WARNING, B3BackendActivator.PLUGIN_IN, IStatus.OK, "", null);
 
 	@B3Backend(system = true, typeFunction = "tcReturnTypeOfLastLambda")
 	public static Object __do(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Curry cur = hurryCurry(params, types, "do");
 
 		Object result = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null
 					? ctx.createOuterContext()
 					: cur.closure.createInnerContext();
 			result = ctx.getInjector().getInstance(IB3Evaluator.class).doCall(cur.lambda, cur.p, cur.t, useCtx);
 			// result = cur.lambda.call(useCtx, cur.p, cur.t);
 		}
 		return result;
 	}
 
 	@B3Backend(system = true)
 	public static Boolean _all(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Curry cur = hurryCurry(params, types, "all");
 		if(!cur.itor.hasNext())
 			return false;
 		Object result = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null
 					? ctx.createOuterContext()
 					: cur.closure.createInnerContext();
 			result = ctx.getInjector().getInstance(IB3Evaluator.class).doCall(cur.lambda, cur.p, cur.t, useCtx);
 			// result = cur.lambda.call(useCtx, cur.p, cur.t);
 			if(!(result instanceof Boolean) || ((Boolean) result) == Boolean.FALSE)
 				return Boolean.FALSE;
 		}
 		return Boolean.TRUE;
 	}
 
 	@B3Backend(system = true)
 	public static Object _assertEquals(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		String message = (String) params[0];
 		Object expected = params[1];
 		Object actual = params[2];
 
 		if(expected == actual)
 			return Boolean.TRUE;
 		if(expected == null || actual == null)
 			throw new B3AssertionFailedException(message, expected, actual);
 		IB3Evaluator evaluator = ctx.getInjector().getInstance(IB3Evaluator.class);
 		if(evaluator.callFunction(
 			"equals", new Object[] { params[1], params[2] }, new Type[] { types[1], types[2] }, ctx) != Boolean.TRUE)
 			throw new B3AssertionFailedException(message, expected, actual);
 		return Boolean.TRUE;
 	}
 
 	@B3Backend(system = true)
 	public static List<Object> _collect(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Curry cur = hurryCurry(params, types, "collect");
 
 		List<Object> result = new ArrayList<Object>();
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null
 					? ctx.createOuterContext()
 					: cur.closure.createInnerContext();
 
 			result.add(ctx.getInjector().getInstance(IB3Evaluator.class).doCall(cur.lambda, cur.p, cur.t, useCtx));
 			// result.add(cur.lambda.call(useCtx, cur.p, cur.t));
 		}
 		return result;
 	}
 
 	@B3Backend(hideOriginal = true, funcNames = { "do" }, systemFunction = "__do", varargs = true, typeFunction = "tcReturnTypeOfLastLambda")
 	public static Object _do(@B3Backend(name = "iterable") Iterable<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(hideOriginal = true, funcNames = { "do" }, systemFunction = "__do", varargs = true, typeFunction = "tcReturnTypeOfLastLambda")
 	public static Object _do(@B3Backend(name = "iterable") Iterator<?> iterator,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(system = true, typeFunction = "tcReturnTypeOfFirstLambda")
 	public static Object _evaluate(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		if(params == null || params.length < 1 || !(params[0] instanceof BFunction))
 			throw new IllegalArgumentException("_evaluate called with too few/wrong arguments");
 		BFunction func = (BFunction) params[0];
 		int limit = params.length;
 		Object[] callparams = new Object[limit - 1];
 		Type[] calltypes = new Type[limit - 1];
 		for(int i = 1; i < limit; i++) {
 			callparams[i - 1] = params[i];
 			calltypes[i - 1] = types[i];
 		}
 		// If function comes with a closure, use it, else use a new outer context. (If the function does
 		// not have a closure it should not see the calling inner context).
 		//
 		BExecutionContext useCtx = func.getClosure();
 		useCtx = useCtx == null
 				? ctx.createOuterContext()
 				: useCtx.createInnerContext();
 		return ctx.getInjector().getInstance(IB3Evaluator.class).doCall(func, callparams, calltypes, useCtx);
 
 		// return func.call(useCtx, callparams, calltypes);
 	}
 
 	@B3Backend(system = true)
 	public static Boolean _exists(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Curry cur = hurryCurry(params, types, "exists");
 		if(!cur.itor.hasNext())
 			return false;
 
 		Object result = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null
 					? ctx.createOuterContext()
 					: cur.closure.createInnerContext();
 
 			result = ctx.getInjector().getInstance(IB3Evaluator.class).doCall(cur.lambda, cur.p, cur.t, useCtx);
 			// result = cur.lambda.call(useCtx, cur.p, cur.t);
 			if(result instanceof Boolean && ((Boolean) result) == Boolean.TRUE)
 				return Boolean.TRUE;
 		}
 		return Boolean.FALSE;
 	}
 
 	@B3Backend(system = true, typeFunction = "tcReturnTypeOfLastLambda")
 	public static Object _inject(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Curry cur = hurryCurry(params, types, "inject");
 
 		if(!((cur.p.length == 1 && cur.curry == -1) || (cur.p.length == 2 && cur.curry != -1)))
 			throw new IllegalArgumentException(
 				"inject only accepts (val, func), (val, _, func), or (_, val, func) as parameters");
 
 		// if curry not set, the call is on the form inject(val, func), and curry should be 1
 		if(cur.curry == -1)
 			cur.curry = 1;
 		if(cur.p.length != 2) {
 			cur.p = new Object[] { cur.p[0], null };
 			cur.t = new Type[] { cur.t[0], FunctionUtils.getParameterTypes(cur.lambda)[1] };
 		}
 		Object inject = null;
 		int injectPos = cur.curry == 0
 				? 1
 				: 0;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null
 					? ctx.createOuterContext()
 					: cur.closure.createInnerContext();
 			inject = ctx.getInjector().getInstance(IB3Evaluator.class).doCall(cur.lambda, cur.p, cur.t, useCtx);
 			// inject = cur.lambda.call(useCtx, cur.p, cur.t);
 			cur.p[injectPos] = inject;
 		}
 		// if the iterator had no elements, return the injected start value.
 		return inject == null
 				? cur.p[injectPos]
 				: inject;
 	}
 
 	@B3Backend(hideOriginal = true, funcNames = { "instanceof" })
 	public static Boolean _instanceOf(@B3Backend(name = "instance") Object o, @B3Backend(name = "type") Type t) {
 		// TODO: this is cheating - it only compares raw
 		return TypeUtils.isAssignableFrom(t, o)
 				? Boolean.TRUE
 				: Boolean.FALSE;
 	}
 
 	@B3Backend(system = true)
 	public static List<Object> _reject(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Curry cur = hurryCurry(params, types, "reject");
 
 		List<Object> result = new ArrayList<Object>();
 		Object cond = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null
 					? ctx.createOuterContext()
 					: cur.closure.createInnerContext();
 			cond = ctx.getInjector().getInstance(IB3Evaluator.class).doCall(cur.lambda, cur.p, cur.t, useCtx);
 			// cond = cur.lambda.call(useCtx, cur.p, cur.t);
 			// if true is returned, the element is not added
 			if(!(cond instanceof Boolean && ((Boolean) cond) == Boolean.TRUE))
 				result.add(curryVal);
 		}
 		return result;
 	}
 
 	@B3Backend(system = true)
 	public static List<Object> _select(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Curry cur = hurryCurry(params, types, "select");
 
 		List<Object> result = new ArrayList<Object>();
 		Object cond = null;
 		while(cur.itor.hasNext()) {
 			Object curryVal = cur.itor.next();
 			if(cur.curry != -1)
 				cur.p[cur.curry] = curryVal;
 			BExecutionContext useCtx = cur.closure == null
 					? ctx.createOuterContext()
 					: cur.closure.createInnerContext();
 			cond = ctx.getInjector().getInstance(IB3Evaluator.class).doCall(cur.lambda, cur.p, cur.t, useCtx);
 
 			// cond = cur.lambda.call(useCtx, cur.p, cur.t);
 			if(cond instanceof Boolean && ((Boolean) cond) == Boolean.TRUE)
 				result.add(curryVal);
 		}
 		return result;
 	}
 
 	@B3Backend(system = true)
 	public static Object _whileFalse(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Object[] callParams = new Object[0];
 		Type[] typeParams = new Type[0];
 		IB3Evaluator evaluator = ctx.getInjector().getInstance(IB3Evaluator.class);
 		if(params.length == 2) {
 			BFunction cond = (BFunction) params[0];
 			BFunction body = (BFunction) params[1];
 			Object c = Boolean.FALSE;
 			Object e = Boolean.TRUE;
 			while(c == Boolean.FALSE) {
 				BExecutionContext useCtx = cond.getClosure();
 				useCtx = useCtx == null
 						? ctx.createOuterContext()
 						: useCtx.createInnerContext();
 				c = evaluator.doCall(cond, callParams, typeParams, useCtx);
 
 				// c = cond.call(useCtx, callParams, typeParams);
 				if(c != Boolean.FALSE)
 					return e;
 				useCtx = body.getClosure();
 				useCtx = useCtx == null
 						? ctx.createOuterContext()
 						: useCtx.createInnerContext();
 				e = evaluator.doCall(body, callParams, typeParams, useCtx);
 				// e = body.call(useCtx, callParams, typeParams);
 			}
 		}
 		if(params.length == 1) {
 			BFunction body = (BFunction) params[0];
 			Object e = Boolean.FALSE;
 			while(e == Boolean.FALSE) {
 				BExecutionContext useCtx = body.getClosure();
 				useCtx = useCtx == null
 						? ctx.createOuterContext()
 						: useCtx.createInnerContext();
 				e = evaluator.doCall(body, callParams, typeParams, useCtx);
 
 				// e = body.call(useCtx, callParams, typeParams);
 			}
 			return e;
 		}
 		throw new IllegalArgumentException("_whileFalse got wrong number of parameters: " + params.length +
 				", accepts 1 or 2 lambdas");
 	}
 
 	@B3Backend(system = true)
 	public static Object _whileTrue(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		Object[] callParams = new Object[0];
 		Type[] typeParams = new Type[0];
 		IB3Evaluator evaluator = ctx.getInjector().getInstance(IB3Evaluator.class);
 		if(params.length == 2) {
 			BFunction cond = (BFunction) params[0];
 			BFunction body = (BFunction) params[1];
 			Object c = Boolean.TRUE;
 			Object e = Boolean.FALSE;
 			while(c == Boolean.TRUE) {
 				BExecutionContext useCtx = cond.getClosure();
 				useCtx = useCtx == null
 						? ctx.createOuterContext()
 						: useCtx.createInnerContext();
 				c = evaluator.doCall(cond, callParams, typeParams, useCtx);
 				// c = cond.call(useCtx, callParams, typeParams);
 				if(c != Boolean.TRUE)
 					return e;
 				useCtx = body.getClosure();
 				useCtx = useCtx == null
 						? ctx.createOuterContext()
 						: useCtx.createInnerContext();
 				e = evaluator.doCall(body, callParams, typeParams, useCtx);
 
 				// e = body.call(useCtx, callParams, typeParams);
 			}
 		}
 		if(params.length == 1) {
 			BFunction body = (BFunction) params[0];
 			Object e = Boolean.TRUE;
 			while(e == Boolean.TRUE) {
 				BExecutionContext useCtx = body.getClosure();
 				useCtx = useCtx == null
 						? ctx.createOuterContext()
 						: useCtx.createInnerContext();
 				e = evaluator.doCall(body, callParams, typeParams, useCtx);
 				// e = body.call(useCtx, callParams, typeParams);
 			}
 			return e;
 		}
 		throw new IllegalArgumentException("_whileTrue got wrong number of parameters: " + params.length +
 				", accepts 1 or 2 lambdas");
 	}
 
 	@B3Backend(systemFunction = "_all", varargs = true)
 	public static Boolean all(@B3Backend(name = "iterable") Iterable<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_all", varargs = true)
 	public static Boolean all(@B3Backend(name = "iterator") Iterator<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Asserts that a variable of type <code>expected</code> can be assigned the value <code>actual</code>.
 	 * 
 	 * @param message
 	 * @param expected
 	 * @param actual
 	 * @return
 	 * @throws Throwable
 	 */
 	public static Boolean assertAssignable(@B3Backend(name = "message") String message,
 			@B3Backend(name = "expectedType") Object expected, @B3Backend(name = "actualType") Object actual)
 			throws Throwable {
 		if(!(expected instanceof Type))
 			throw new B3AssertionFailedException(message, expected, actual.getClass());
 		if(!TypeUtils.isAssignableFrom((Type) expected, actual))
 			throw new B3AssertionFailedException(message, expected, actual.getClass());
 		return Boolean.TRUE;
 	}
 
 	public static Boolean assertContainsAll(@B3Backend(name = "message") String message,
 			@B3Backend(name = "container") Collection<?> container, @B3Backend(name = "data") Collection<?> data)
 			throws Throwable {
 		if(!container.containsAll(data))
 			throw new B3AssertionFailedException(message, data, container);
 		return Boolean.TRUE;
 
 	}
 
 	@B3Backend(systemFunction = "_assertEquals")
 	public static Boolean assertEquals(@B3Backend(name = "message") String message,
 			@B3Backend(name = "expected") Object expected, @B3Backend(name = "actual") Object actual) throws Throwable {
 		return null;
 	}
 
 	public static Boolean assertFalse(@B3Backend(name = "message") String message,
 			@B3Backend(name = "booleanExpr") Boolean booleanExpr) throws Throwable {
 
 		if(booleanExpr == null)
 			throw new B3AssertionFailedException(message, Boolean.FALSE, null);
 		if(!booleanExpr.equals(Boolean.FALSE))
 			throw new B3AssertionFailedException(message, Boolean.FALSE, booleanExpr);
 		return Boolean.TRUE;
 	}
 
 	public static Boolean assertNotNull(@B3Backend(name = "message") String message,
 			@B3Backend(name = "expr") Object expr) throws Throwable {
 
 		if(expr == null)
 			throw new B3AssertionFailedException(message, "<a non null value>", null);
 		return Boolean.TRUE;
 	}
 
 	public static Boolean assertNull(@B3Backend(name = "message") String message, @B3Backend(name = "expr") Object expr)
 			throws Throwable {
 
 		if(expr != null)
 			throw new B3AssertionFailedException(message, "<null value>", expr);
 		return Boolean.TRUE;
 	}
 
 	public static Boolean assertTrue(@B3Backend(name = "message") String message,
 			@B3Backend(name = "booleanExpr") Boolean booleanExpr) throws Throwable {
 
 		if(booleanExpr == null)
 			throw new B3AssertionFailedException(message, Boolean.TRUE, null);
 		if(!booleanExpr.equals(Boolean.TRUE))
 			throw new B3AssertionFailedException(message, Boolean.TRUE, booleanExpr);
 		return Boolean.TRUE;
 	}
 
 	public static Boolean assertType(@B3Backend(name = "message") String message,
 			@B3Backend(name = "expectedType") Object expected, @B3Backend(name = "actualType") Object actual)
 			throws Throwable {
 		if(!(expected instanceof Type))
 			throw new B3AssertionFailedException(message, expected, actual.getClass());
 		if(expected != actual.getClass())
 			throw new B3AssertionFailedException(message, expected, actual.getClass());
 		return Boolean.TRUE;
 	}
 
 	@B3Backend(systemFunction = "_collect", varargs = true)
 	public static List<Object> collect(@B3Backend(name = "iterable") Iterable<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_collect", varargs = true)
 	public static List<Object> collect(@B3Backend(name = "iterator") Iterator<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	public static IStatus ERROR() {
 		return EMPTY_ERROR;
 	}
 
 	public static IStatus ERROR(String message) {
 		return new Status(IStatus.ERROR, B3BackendActivator.PLUGIN_IN, IStatus.OK, message, null);
 	}
 
 	public static IStatus ERROR(String message, Throwable t) {
 		return new Status(IStatus.ERROR, B3BackendActivator.PLUGIN_IN, IStatus.OK, message, t);
 	}
 
 	/**
 	 * Evaluate a function - the same as calling it.
 	 * 
 	 * @param func
 	 *            - the function to evaluate
 	 * @param params
 	 *            - the parameters passed to the function
 	 * @return the result of calling the function
 	 */
 	@B3Backend(systemFunction = "_evaluate", varargs = true, typeFunction = "tcReturnTypeOfFirstLambda")
 	public static final Object evaluate(@B3Backend(name = "function") BFunction func, Object... params) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_exists", varargs = true)
 	public static Boolean exists(@B3Backend(name = "iterable") Iterable<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_exists", varargs = true)
 	public static Boolean exists(@B3Backend(name = "iterator") Iterator<?> iterator,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Organizes the parameters in a curried call.
 	 * 
 	 * @param params
 	 * @param types
 	 * @param name
 	 * @return A Curry with data for the function to use
 	 */
 	private static Curry hurryCurry(Object[] params, Type[] types, String name) {
 		Curry cur = new Curry();
 		BExecutionContext ctx = B3ContextAccess.get();
 		ITypeProvider typer = ctx.getInjector().getInstance(ITypeProvider.class);
 		int nParameters = params.length;
 		if(nParameters < 2)
 			throw new IllegalArgumentException("system function '" + name + "' expected 2 or more arguments");
 		// Object[] varargs = (Object[])params[1];
 		// int varargsLength = varargs.length;
 		if(!(params[nParameters - 1] instanceof BFunction))
 			throw new IllegalArgumentException("system function '" + name + "' did not get a function as last argument");
 		cur.lambda = (BFunction) params[nParameters - 1];
 		cur.closure = cur.lambda.getClosure();
 		cur.curry = -1; // unknown
 		int nLambdaParameters = 1; // default
 		if(nParameters == 2)
 			cur.curry = 0;
 		if(nParameters > 2) {
 			for(int i = 1; i < nParameters - 1; i++)
 				if(params[i] == Any.ANY) {
 					cur.curry = i - 1;
 					break;
 				}
 			nLambdaParameters = nParameters - 2; // -1 for iterator, -1 for lambda
 		}
 
 		cur.itor = params[0] instanceof Iterator
 				? ((Iterator<?>) params[0])
 				: ((Iterable<?>) params[0]).iterator();
 		cur.p = new Object[nLambdaParameters];
 		cur.t = new Type[nLambdaParameters];
 		for(int i = 0; i < nLambdaParameters; i++) {
 			cur.p[i] = params[i + 1];
 			cur.t[i] = typer.doGetInferredType(params[i + 1]);
 			// cur.t[i] = params[i + 1].getClass();
 		}
 		if(cur.curry != -1) {
 			// TODO: get the type of the itor - cheating now by getting type of parameter
 			// from called function
 			cur.t[cur.curry] = typer.doGetInferredType(cur.lambda.getParameters().get(cur.curry));
 		}
 		return cur;
 	}
 
 	public static IStatus INFO() {
 		return EMPTY_INFO;
 	}
 
 	public static IStatus INFO(String message) {
 		return new Status(IStatus.INFO, B3BackendActivator.PLUGIN_IN, IStatus.OK, message, null);
 	}
 
 	@B3Backend(systemFunction = "_inject", varargs = true, typeFunction = "tcReturnTypeOfLastLambda")
 	public static Object inject(@B3Backend(name = "iterable") Iterable<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_inject", varargs = true, typeFunction = "tcReturnTypeOfLastLambda")
 	public static Object inject(@B3Backend(name = "iterator") Iterator<?> iterator,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(varargs = true)
 	public static IStatus MULTISTATUS(String message, IStatus... iStatus) {
 		if(iStatus == null)
 			throw new IllegalArgumentException("Vararg IStatus... is null");
 		return MULTISTATUS(message, null, iStatus);
 	}
 
 	@B3Backend(varargs = true)
 	public static IStatus MULTISTATUS(String message, Throwable t, IStatus... iStatus) {
 		if(iStatus == null)
 			throw new IllegalArgumentException("Vararg IStatus... is null");
 		return new MultiStatus(B3BackendActivator.PLUGIN_IN, IStatus.OK, iStatus, message, t);
 	}
 
 	/**
 	 * Returns a simple OK status without a result.
 	 * 
 	 * @return
 	 */
 	public static IStatus OK() {
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Returns a simple OK status without a result, but with a custom message.
 	 * 
 	 * @return
 	 */
 	public static IStatus OK(String message) {
 		return new Status(IStatus.OK, B3BackendActivator.PLUGIN_IN, IStatus.OK, message, null);
 	}
 
 	@B3Backend
 	public static Object print(@B3Backend(name = "anObject") Object object) {
 		System.out.print(object.toString());
 		System.out.print("\n");
 		return object;
 	}
 
 	@B3Backend(systemFunction = "_reject", varargs = true)
 	public static List<Object> reject(@B3Backend(name = "iterable") Iterable<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_reject", varargs = true)
 	public static List<Object> reject(@B3Backend(name = "iterator") Iterator<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_select", varargs = true, typeFunction = "tcBooleanLambda")
 	public static List<Object> select(@B3Backend(name = "iterable") Iterable<?> iterable,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_select", varargs = true, typeFunction = "tcBooleanLambda")
 	public static List<Object> select(@B3Backend(name = "iterator") Iterator<?> iterator,
 			@B3Backend(name = "paramsAnyAndFunction") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Type Calculator for f(?<T>,<U1>...<Un>, f(<U1>..<Un>)=>Boolean)=>List<T>
 	 * Where an Any in U1..Un is replaced by T.
 	 * Example:
 	 * select(Iterator<Integer>, 20, _, {u1, u2 | u2 > u1; })
 	 * results in signature for the select call of:
 	 * (Iterator<Integer>, Integer, Integer, (Integer, Integer)=>Boolean)=>List<Integer>
 	 * 
 	 * @param types
 	 * @return
 	 */
 	@B3Backend(typeCalculator = true)
 	public static B3FunctionType tcBooleanLambda(Type[] types) {
 		B3FunctionType result = B3backendFactory.eINSTANCE.createB3FunctionType();
 		int ix = types.length - 1;
 		// must have at least iterator, and lambda as parameters
 		if(types.length < 2 || !(types[ix] instanceof B3FunctionType)) {
 			result.setReturnType(TypeUtils.coerceToEObjectType(Object.class));
 			return result; // non conforming, return ()=>Object
 		}
 		// determine curry type
 		Type[] curryGenericArgs = TypeUtils.getTypeArguments(types[0]);
 		// if generic is not 1 arg, it is either wrong i.e. Iterator<K, V>, or erased in runtime - use Object
		Type curryType = curryGenericArgs.length == 1
 				? curryGenericArgs[0]
				: TypeUtils.coerceToEObjectType(Object.class);
 
 		// The commonTypes are parameters both in the system function, and in the lambda
 		Type[] commonTypes = new Type[types.length - 2];
 		for(int i = 1; i < types.length - 1; i++)
 			commonTypes[i - 1] = TypeUtils.coerceToEObjectAndResolveAny(types[i], curryType);
 
 		// Compute the (resulting) lambda signature
 		// (type[0-n])=>Boolean
 		B3FunctionType lambda = B3backendFactory.eINSTANCE.createB3FunctionType();
		for(int i = 0; i < commonTypes.length; i++)
			lambda.getParameterTypes().add(commonTypes[i]);
		lambda.setReturnType(TypeUtils.coerceToEObjectType(Boolean.class));
 		lambda.setFunctionType(B3Function.class);
 		// Set all parameters in resulting type
 		result.getParameterTypes().add(types[0]); // the iterator/iterable
 		for(int i = 0; i < commonTypes.length; i++)
 			result.getParameterTypes().add(commonTypes[i]);
 		result.getParameterTypes().add(lambda);
 		return result;
 	}
 
 	@B3Backend(typeCalculator = true)
 	public static B3FunctionType tcReturnTypeOfFirstLambda(Type[] types) {
 		B3FunctionType result = B3backendFactory.eINSTANCE.createB3FunctionType();
 
 		if(types.length <= 1 || !(types[0] instanceof B3FunctionType))
 			result.setReturnType(TypeUtils.coerceToEObjectType(Object.class));
 		else {
 			B3FunctionType ft = (B3FunctionType) types[0];
 			result.setReturnType(ft.getReturnTypeForParameterTypes(types));
 		}
 		return result;
 	}
 
 	/**
 	 * TODO: Incomplete - only sets return type
 	 * 
 	 * @param types
 	 * @return
 	 */
 	@B3Backend(typeCalculator = true)
 	public static B3FunctionType tcReturnTypeOfLastLambda(Type[] types) {
 		B3FunctionType result = B3backendFactory.eINSTANCE.createB3FunctionType();
 
 		int ix = types.length - 1;
 		if(types.length < 1 || !(types[ix] instanceof B3FunctionType)) {
 			result.setReturnType(TypeUtils.coerceToEObjectType(Object.class));
 		}
 		else {
 			B3FunctionType ft = (B3FunctionType) types[ix];
 			result.setReturnType(ft.getReturnTypeForParameterTypes(types));
 		}
 		return result;
 	}
 
 	@B3Backend(typeCalculator = true)
 	public static Type typeOfFirstParameter(Type[] types) {
 		if(types.length >= 1)
 			return types[0];
 
 		return Object.class;
 	}
 
 	public static IStatus WARNING() {
 		return EMPTY_WARNING;
 	}
 
 	public static IStatus WARNING(String message) {
 		return new Status(IStatus.WARNING, B3BackendActivator.PLUGIN_IN, IStatus.OK, message, null);
 	}
 
 	public static IStatus WARNING(String message, Throwable t) {
 		return new Status(IStatus.WARNING, B3BackendActivator.PLUGIN_IN, IStatus.OK, message, t);
 	}
 
 	@B3Backend(systemFunction = "_whileFalse")
 	public static Object whileFalse(@B3Backend(name = "doWhileBlock") BFunction func) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_whileFalse")
 	public static Object whileFalse(@B3Backend(name = "conditionBlock") BFunction cond,
 			@B3Backend(name = "functionBlock") BFunction body) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_whileTrue")
 	public static Object whileTrue(@B3Backend(name = "doWhileBlock") BFunction func) {
 		return null;
 	}
 
 	@B3Backend(systemFunction = "_whileTrue")
 	public static Object whileTrue(@B3Backend(name = "conditionBlock") BFunction cond,
 			@B3Backend(name = "functionBlock") BFunction body) {
 		return null;
 	}
 }
