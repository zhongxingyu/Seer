 /**
  * Copyright (c) 2009-2010, Cloudsmith Inc and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  * - Cloudsmith Inc - initial API and implementation.
  */
 
 package org.eclipse.b3.build.functions;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Type;
 import java.util.Iterator;
 
 import org.eclipse.b3.backend.core.B3Backend;
 import org.eclipse.b3.backend.core.B3ContextAccess;
 import org.eclipse.b3.backend.core.IB3Engine;
 import org.eclipse.b3.backend.core.IB3Evaluator;
 import org.eclipse.b3.backend.core.exceptions.B3EngineException;
 import org.eclipse.b3.backend.core.exceptions.B3NoSuchVariableException;
 import org.eclipse.b3.backend.evaluator.b3backend.BContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.build.BuildSet;
 import org.eclipse.b3.build.BuildUnit;
 import org.eclipse.b3.build.core.B3BuildConstants;
 import org.eclipse.b3.build.core.B3BuilderJob;
 import org.eclipse.b3.build.core.adapters.BuildUnitProxyAdapterFactory;
 import org.eclipse.b3.build.core.iterators.EffectiveUnitIterator;
 import org.eclipse.b3.build.core.runtime.SharedScope;
 import org.eclipse.b3.build.repository.IBuildUnitResolver;
 import org.eclipse.core.runtime.IStatus;
 
 import com.google.inject.Injector;
 
 public class BuildFunctions {
 
 	@B3Backend(system = true)
 	public static Object _callFunction(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		String functionName = (String) params[1];
 
 		Object[] args = new Object[params.length - 2];
 		System.arraycopy(params, 2, args, 0, args.length);
 		Type[] argTypes = new Type[params.length - 2];
 		System.arraycopy(types, 2, argTypes, 0, argTypes.length);
 		return ctx.getInjector().getInstance(IB3Evaluator.class).callFunction(functionName, args, argTypes, ctx);
 	}
 
 	@B3Backend(system = true)
 	public static BuildSet _resolveAndRunBuilder(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		BuildUnit unitToUse = null;
 		String unitName = null;
 
 		if(params[1] instanceof BuildUnit) {
 			unitToUse = ((BuildUnit) params[1]);
 			unitName = unitToUse.getName();
 		}
 		else {
 			if(!(params[1] instanceof String))
 				throw new IllegalArgumentException(
 					"The argument 'unit' is neither a BuildUnit nor a String with a unit's name. Got: " +
 							params[1].getClass().getName());
 			unitName = (String) params[1];
 		}
 		SharedScope resolutionScope = null;
 		Injector injector = ctx.getInjector();
 		resolutionScope = injector.getInstance(B3BuildConstants.KEY_RESOLUTION_SCOPE);
 		resolutionScope.enter(); // !remember to call exit()
 		try {
 			if(unitToUse == null) {
 				// find unit via name
 				EffectiveUnitIterator unitItor = new EffectiveUnitIterator(ctx.getContext(BContext.class));
 
 				while(unitItor.hasNext()) {
 					if((unitToUse = unitItor.next()).getName().equals(unitName))
 						break;
 					// System.err.print("Unit name:" + unitToUse.getName() + "\n");
 					unitToUse = null;
 				}
 				if(unitToUse == null)
 					throw new B3NoSuchVariableException(unitName);
 			}
 			IBuildUnitResolver resolver = injector.getInstance(IBuildUnitResolver.class);
 			IStatus status = resolver.resolveUnit(unitToUse, ctx);
 
 			if(!status.isOK())
 				throw new B3EngineException(
 					"Resolution Failed with non OK status: " + status.toString(), status.getException());
 			return _runBuilder(ctx, params, types);
 		}
 		finally {
 			resolutionScope.exit();
 		}
 	}
 
 	@B3Backend(system = true)
 	public static BuildSet _runBuilder(BExecutionContext ctx, Object[] params, Type[] types) throws Throwable {
 		BuildUnit unitToUse = null;
 		String unitName = null;
 
 		if(params[1] instanceof BuildUnit) {
 			unitToUse = ((BuildUnit) params[1]);
 			unitName = unitToUse.getName();
 		}
 		else {
 			if(!(params[1] instanceof String))
 				throw new IllegalArgumentException(
 					"The argument 'unit' is neither a BuildUnit nor a String with a unit's name. Got: " +
 							params[1].getClass().getName());
 			unitName = (String) params[1];
 		}
 
 		String functionName = (String) params[2];
 
 		Object[] args = new Object[params.length - 2];
 		System.arraycopy(params, 2, args, 0, args.length);
 		Type[] argTypes = new Type[params.length - 2];
 		System.arraycopy(types, 2, argTypes, 0, argTypes.length);
 
 		// find and set the unit as parameter and with correct type (if not passed as an argument)
 		if(unitToUse == null) {
 			EffectiveUnitIterator unitItor = new EffectiveUnitIterator(ctx.getContext(BContext.class));
 			while(unitItor.hasNext()) {
 				if((unitToUse = unitItor.next()).getName().equals(unitName)) {
 					unitToUse = BuildUnitProxyAdapterFactory.eINSTANCE.adapt(unitToUse).getProxy();
 					break;
 				}
 				// System.err.print("Unit name:" + unitToUse.getName() + "\n");
 				unitToUse = null;
 			}
 		}
 		if(unitToUse != null) {
 			args[0] = unitToUse;
 			argTypes[0] = unitToUse.getClass();
 		}
 		else {
 			args[0] = null;
 			argTypes[0] = BuildUnit.class;
 		}
 		B3BuilderJob job = null;
 		try {
 			job = (B3BuilderJob) ctx.getInjector().getInstance(IB3Evaluator.class).callFunction(
 				functionName, args, argTypes, ctx);
 		}
 		catch(Throwable e) {
 			// a failing call must be wrapped, or the engine will think it is the call to runBuilder that
 			// failed.
 			throw new InvocationTargetException(e, "Exception when invoking builder");
 		}
 		job.schedule();
 		job.join();
 		return job.getBuildResult();
 	}
 
 	/**
 	 * Dynamically run a Builder.
 	 * 
 	 * @param engine
 	 * @param functionName
 	 * @param variable
 	 * @return
 	 */
 	@B3Backend
 	public static Iterator<BuildUnit> allDefinedUnits( //
 			@B3Backend(name = "engine") IB3Engine engine) {
 		return new EffectiveUnitIterator.UnitProxies(B3ContextAccess.get());
 	}
 
 	/**
 	 * Dynamically call a function.
 	 * 
 	 * @param engine
 	 * @param functionName
 	 * @param variable
 	 * @return
 	 */
 	@B3Backend(systemFunction = "_callFunction", varargs = true)
 	public static Object callFunction(@B3Backend(name = "engine") IB3Engine engine,
 			@B3Backend(name = "functionName") String functionName, @B3Backend(name = "arguments") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Dynamically Resolve and run a Builder.
 	 * 
 	 * @param engine
 	 * @param functionName
 	 * @param variable
 	 * @return
 	 */
 	@B3Backend(systemFunction = "_resolveAndRunBuilder", varargs = true)
 	public static BuildSet resolveAndRunBuilder( //
 			@B3Backend(name = "engine") IB3Engine engine, //
 			@B3Backend(name = "unit") BuildUnit unit, //
 			@B3Backend(name = "builderName") String builderName, //
 			@B3Backend(name = "arguments") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Dynamically Resolve and run a Builder.
 	 * TODO: THis is cheating because type inference does not work when doing select etc.
 	 * 
 	 * @param engine
 	 * @param functionName
 	 * @param variable
 	 * @return
 	 */
 	@B3Backend(systemFunction = "_resolveAndRunBuilder", varargs = true)
 	public static BuildSet resolveAndRunBuilder( //
 			@B3Backend(name = "engine") IB3Engine engine, //
 			@B3Backend(name = "unitName") Object unit, //
 			@B3Backend(name = "builderName") String builderName, //
 			@B3Backend(name = "arguments") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Dynamically Resolve and run a Builder.
 	 * 
 	 * @param engine
 	 * @param functionName
 	 * @param variable
 	 * @return
 	 */
 	@B3Backend(systemFunction = "_resolveAndRunBuilder", varargs = true)
 	public static BuildSet resolveAndRunBuilder( //
 			@B3Backend(name = "engine") IB3Engine engine, //
 			@B3Backend(name = "unitName") String unitName, //
 			@B3Backend(name = "builderName") String builderName, //
 			@B3Backend(name = "arguments") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Dynamically run a Builder.
 	 * 
 	 * @param engine
 	 * @param functionName
 	 * @param variable
 	 * @return
 	 */
 	@B3Backend(systemFunction = "_runBuilder", varargs = true)
 	public static BuildSet runBuilder( //
 			@B3Backend(name = "engine") IB3Engine engine, //
 			@B3Backend(name = "unit") BuildUnit unit, //
 			@B3Backend(name = "builderName") String builderName, //
 			@B3Backend(name = "arguments") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Dynamically run a Builder.
 	 * TODO: This is cheating - the type system should have detected that a Unit is found when using
 	 * select, etc.
 	 * 
 	 * @param engine
 	 * @param functionName
 	 * @param variable
 	 * @return
 	 */
 	@B3Backend(systemFunction = "_runBuilder", varargs = true)
 	public static BuildSet runBuilder( //
 			@B3Backend(name = "engine") IB3Engine engine, //
 			@B3Backend(name = "unit") Object unit, //
 			@B3Backend(name = "builderName") String builderName, //
 			@B3Backend(name = "arguments") Object... variable) {
 		return null;
 	}
 
 	/**
 	 * Dynamically run a Builder.
 	 * 
 	 * @param engine
 	 * @param functionName
 	 * @param variable
 	 * @return
 	 */
 	@B3Backend(systemFunction = "_runBuilder", varargs = true)
 	public static BuildSet runBuilder( //
 			@B3Backend(name = "engine") IB3Engine engine, //
 			@B3Backend(name = "unitName") String unitName, //
 			@B3Backend(name = "builderName") String builderName, //
 			@B3Backend(name = "arguments") Object... variable) {
 		return null;
 	}
 }
