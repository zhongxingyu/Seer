 /**
  * Copyright (c) 2010, Cloudsmith Inc and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  * - Cloudsmith Inc - initial API and implementation.
  */
 package org.eclipse.b3.build.core;
 
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.b3.backend.core.IB3Evaluator;
 import org.eclipse.b3.backend.core.exceptions.B3EngineException;
 import org.eclipse.b3.backend.core.exceptions.B3InternalError;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BInnerContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BParameter;
 import org.eclipse.b3.backend.evaluator.b3backend.BParameterList;
 import org.eclipse.b3.backend.evaluator.b3backend.BPropertySet;
 import org.eclipse.b3.backend.evaluator.b3backend.ExecutionMode;
 import org.eclipse.b3.backend.evaluator.b3backend.impl.AbstractB3Executor;
 import org.eclipse.b3.backend.evaluator.b3backend.impl.AbstractB3Job;
 import org.eclipse.b3.backend.inference.ITypeProvider;
 import org.eclipse.b3.build.B3BuildFactory;
 import org.eclipse.b3.build.BuildResultContext;
 import org.eclipse.b3.build.BuildSet;
 import org.eclipse.b3.build.BuildUnit;
 import org.eclipse.b3.build.BuilderCall;
 import org.eclipse.b3.build.EffectiveBuilderCallFacade;
 import org.eclipse.b3.build.IBuilder;
 import org.eclipse.b3.build.PathVector;
 import org.eclipse.b3.build.RequiredCapability;
 import org.eclipse.b3.build.ResolutionInfo;
 import org.eclipse.b3.build.UnitResolutionInfo;
 import org.eclipse.b3.build.core.adapters.BuildUnitProxyAdapterFactory;
 import org.eclipse.b3.build.core.adapters.ResolutionInfoAdapterFactory;
 import org.eclipse.b3.build.core.exceptions.B3UnresolvedRequirementException;
 import org.eclipse.b3.build.core.exceptions.B3WrongBuilderReturnTypeException;
 import org.eclipse.b3.build.core.iterators.BuilderCallIteratorProvider;
 import org.eclipse.b3.build.core.iterators.EffectivePathVectorIterator;
 import org.eclipse.b3.build.internal.B3BuildActivator;
 import org.eclipse.b3.build.repository.IBuildUnitResolver;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 
 import com.google.inject.Injector;
 
 /**
  * A Job that executes a Builder.
  * A call to an IBuilder should return an (unscheduled) instance of B3BuilderJob that is configured to
  * run the builder (the prepared context normally passed on the the "internalCall" should be passed to
  * the constructor of a B3BuilderJob, together with a reference to the builder).
  * 
  */
 public class B3BuilderJob extends AbstractB3Job {
 
 	private IBuilder builder;
 
 	private BuildUnit unit;
 
 	private B3BuilderJob parent;
 
 	private List<String> aliases;
 
 	private IB3Evaluator evaluator;
 
 	private ITypeProvider typer;
 
 	/**
 	 * Creates a B3BuilderJob that will run a builder for a unit identified by the value of "unit"
 	 * found in the context passed as an argument.
 	 * 
 	 * @param ctx
 	 * @param builder
 	 * @throws B3EngineException
 	 *             if the value "unit" is not defined in the context.
 	 */
 	public B3BuilderJob(BExecutionContext ctx, IBuilder builder) throws B3EngineException {
 		super(ctx, "builder job"); // dummy name, replaced below
 		if(ctx == null)
 			throw new IllegalArgumentException("Context can not be null when creating a B3BuilderJob");
 		if(builder == null)
 			throw new IllegalArgumentException("Builder can not be null when creating a B3BuilderJob");
 		this.ctx = ctx;
 		this.builder = builder;
 		Injector injector = ctx.getInjector();
 		this.evaluator = injector.getInstance(IB3Evaluator.class);
 		this.typer = injector.getInstance(ITypeProvider.class);
 		this.unit = (BuildUnit) ctx.getValue("unit");
 		if(unit == null)
 			throw new IllegalArgumentException(
 				"Context must have an instance of BuildUnit bound to context value 'unit'");
 		unit = BuildUnitProxyAdapterFactory.eINSTANCE.adapt(unit).getProxy();
 		this.aliases = null;
 		setName("building: " + unit.getName() + "#" + builder.getName());
 	}
 
 	@Override
 	public boolean belongsTo(Object family) {
 		if(family instanceof B3BuilderJob && ((B3BuilderJob) family) == parent)
 			return true;
 		return super.belongsTo(family);
 	}
 
 	/**
 	 * Obtains the result of the job, and the BuildResult from the returned status.
 	 * If the job state is not OK, an IllegalStateException is thrown.
 	 * 
 	 * @return the BuildResult
 	 * @throws IllegalStateException
 	 *             (if this method is called when state is not OK).
 	 */
 	public BuildSet getBuildResult() {
 		IStatus r = getResult();
 		if(r != null && r.isOK())
 			return ((B3BuilderStatus) r).getBuildResult();
 		throw new IllegalStateException("Can not obtain result when job state is not OK", new CoreException(r));
 	}
 
 	@Override
 	protected IStatus runb3(IProgressMonitor monitor) {
 		try {
 			// set the UNIT DEFAULT PROPERTIES and BUILDER DEFAULT PROPERTIES in a
 			// context visible downstream
 			// (but only if there are default properties to evaluate).
 			BPropertySet unitProperties = unit.getDefaultProperties();
 			BPropertySet builderProperties = builder.getDefaultProperties();
 			if(unitProperties != null || builderProperties != null) {
 				BInnerContext ictx = ctx.createWrappedInnerContext();
 				if(unitProperties != null)
 					evaluator.doEvaluateDefaults(unitProperties, ictx, true);
 				if(builderProperties != null)
 					evaluator.doEvaluateDefaults(builderProperties, ictx, true);
 				ctx = ictx;
 				return new AbstractB3Executor<IStatus>(ictx) {
 
 					@Override
 					protected IStatus runb3(IProgressMonitor monitor) throws Throwable {
 						return runInInnerContext(monitor);
 					}
 				}.run();
 			}
 			return runInInnerContext(monitor);
 		}
 		catch(OperationCanceledException e) {
 			return B3BuilderStatus.CANCEL_STATUS;
 
 		}
 		catch(Throwable t) {
			return B3BuilderStatus.error("Builder Job \"" + this.getName() + "\" Failed - see details", t);
 		}
 	}
 
 	protected IStatus runInInnerContext(IProgressMonitor monitor) {
 		try {
 			// PRECONDITION
 			// just evaluate, supposed to throw exception if not acceptable
 			// The precondition sees the input context, and unit+builder default properties
 			// but not 'output', 'input', and 'source'.
 			//
 			BExpression tmp = builder.getPrecondExpr();
 			if(tmp != null)
 				evaluator.doEvaluate(tmp, ctx);
 
 			// Iterate over all builder references, and call each builder to produce a build job.
 			// Collect all build jobs to be executed.
 			//
 			List<B3BuilderJob> jobsToRun = new ArrayList<B3BuilderJob>();
 			Iterator<EffectiveBuilderCallFacade> rItor = ctx.getInjector().getInstance(
 				BuilderCallIteratorProvider.class).doGetEffectiveIterator(builder, ctx);
 			// Iterator<EffectiveBuilderCallFacade> rItor = builder.getEffectiveBuilderReferences(ctx);
 			while(rItor.hasNext()) {
 				EffectiveBuilderCallFacade ebref = rItor.next();
 				BuilderCall bref = ebref.getBuilderReference();
 				final String builderName = bref.getBuilderName();
 
 				final BExecutionContext ctxToUse = ebref.getContext();
 				final BParameterList parameters = bref.getParameters();
 				int size = 1 + (parameters == null
 						? 0
 						: parameters.getParameters().size());
 				final Object[] values = new Object[size];
 				final Type[] types = new Type[size];
 
 				// run in correct context
 				new AbstractB3Executor<Object>(ctxToUse) {
 
 					@Override
 					protected Object runb3(IProgressMonitor monitor) throws Throwable {
 						int idx = 1;
 						if(parameters != null)
 							for(BParameter p : parameters.getParameters()) {
 								values[idx] = evaluator.doEvaluate(p.getExpr(), ctxToUse);
 								types[idx++] = typer.doGetInferredType(p.getExpr());
 							}
 						return null;
 					}
 				}.run();
 
 				// Get the resolved unit in the current resolution scope
 				//
 				BuildUnit unitToUse = unit; // default is a call on "self"
 				RequiredCapability rq = null;
 				rq = ebref.getRequiredCapability(); // a contained requirement
 
 				// if call is on unit other than 'self', find the resolution
 				if(rq != null) {
 					// use the resolver as the resolution scope key
 					IBuildUnitResolver scopeKey = ctxToUse.getInjector().getInstance(IBuildUnitResolver.class);
 
 					// get the resolution info
 					ResolutionInfo rinfo = ResolutionInfoAdapterFactory.eINSTANCE.adapt(rq).getAssociatedInfo(scopeKey);
 
 					// TODO: Should probably pass status to exception if info and status exist
 					if(rinfo == null || !rinfo.getStatus().isOK() || !(rinfo instanceof UnitResolutionInfo))
 						throw new B3UnresolvedRequirementException(unit, builder, rq, rinfo != null
 								? rinfo.getStatus()
 								: null);
 					unitToUse = ((UnitResolutionInfo) rinfo).getUnit();
 
 				}
 				// in case a non proxy managed to get this far...
 				if(!Proxy.isProxyClass(unitToUse.getClass()))
 					unitToUse = BuildUnitProxyAdapterFactory.eINSTANCE.adapt(unitToUse).getProxy();
 
 				values[0] = unitToUse;
 				types[0] = unitToUse.getClass();
 				if(builderName == null) {
 					// TODO: call the builder that provided the capability
 					// - to make these calls typesafe (and with parameter validation), there must be some declaration that
 					// the resolution of a requirement is made by a builder with a particular signature.
 					throw new UnsupportedOperationException(
 						"Calling builder that provided a capability is not yet implemented!");
 				}
 				Object buildJobObject = new AbstractB3Executor<Object>(ctxToUse) {
 
 					@Override
 					protected Object runb3(IProgressMonitor monitor) throws Throwable {
 						return evaluator.callFunction(builderName, values, types, ctxToUse);
 					}
 				}.run();
 				// Object buildJobObject = ctxToUse.callFunction(builderName, values, types);
 				if(!(buildJobObject instanceof B3BuilderJob))
 					throw new B3InternalError("Builder did not return a B3BuilderJob: " + builderName);
 				B3BuilderJob buildJob = (B3BuilderJob) buildJobObject;
 				buildJob.setAliases(ebref.getAliases());
 				jobsToRun.add(buildJob);
 			}
 			// EXECUTE JOB ARRAY observing the execution mode of the builder and the unit
 			if(builder.getExecutionMode() == ExecutionMode.PARALLEL &&
 					unit.getExecutionMode() == ExecutionMode.PARALLEL)
 				runParallel(jobsToRun, monitor);
 			else
 				runSequential(jobsToRun, monitor);
 
 			// COLLECT INPUT RESULT
 			// Merge the input into "input" BuildResult, and all other aliased groups
 			//
 			IStatus status = collectResult(jobsToRun);
 			if(!status.isOK())
 				return status;
 
 			// TODO - INPUT ANNOTATIONS
 			// Currently missing in input, but should act as default annotations
 			// Should be processed here
 
 			// SOURCE (if stated) should be evaluated at this point to make it available in
 			// the post input condition. Even if there is no source, assign an empty build result to "source"
 			// (for consistency, and user code may modify this instance).
 			//
 			BuildSet source = B3BuildFactory.eINSTANCE.createBuildSet();
 			EffectivePathVectorIterator pvItor = new EffectivePathVectorIterator(ctx, builder.getSource());
 			EList<PathVector> sourcePaths = source.getPathVectors();
 			while(pvItor.hasNext())
 				sourcePaths.add(pvItor.next().resolve(unit.getSourceLocation()));
 
 			// ANNOTATION PROCESSING IN SOURCE
 			// Annotations in input are processed before source is made available in the context
 			if(builder.getSource() != null) {
 				BPropertySet propertySet = builder.getSource().getAnnotations();
 				if(propertySet != null) {
 					BuildResultContext specialContext = B3BuildFactory.eINSTANCE.createBuildResultContext();
 					specialContext.setParentContext(ctx);
 					specialContext.setOuterContext(ctx instanceof BInnerContext
 							? ((BInnerContext) ctx).getOuterContext()
 							: ctx);
 					specialContext.getValueMap().merge(source.getValueMap());
 					evaluator.doEvaluateDefaults(propertySet, specialContext.createInnerContext(), false);
 
 					// propertySet.evaluateDefaults(specialContext.createInnerContext(), false);
 					source.setValueMap(specialContext.getValueMap());
 				}
 			}
 
 			ctx.defineFinalValue("source", source, BuildSet.class);
 
 			// OUTPUT (if stated) should be evaluated at this point to make it available in
 			// the post input condition. Even if there is no output, assign an empty build result to "output"
 			// (for consistency, and user code may modify this instance).
 			// Annotations in output are not processed until later.
 			BuildSet output = B3BuildFactory.eINSTANCE.createBuildSet();
 			pvItor = new EffectivePathVectorIterator(ctx, builder.getOutput());
 			EList<PathVector> outputPaths = output.getPathVectors();
 			while(pvItor.hasNext())
 				outputPaths.add(pvItor.next().resolve(unit.getOutputLocation()));
 
 			ctx.defineFinalValue("output", output, BuildSet.class);
 
 			// POST INPUT CONDITION
 			// just evaluate, supposed to throw exception if not acceptable
 			// context has all input defined at this point and output refers to effective Build Result
 			// (but without default annotations).
 			//
 			tmp = builder.getPostinputcondExpr();
 			if(tmp != null)
 				evaluator.doEvaluate(tmp, ctx);
 
 			// EVALUATE THE FUNCTION BODY, OR PERFORM THE DEFAULT
 			// TODO: using "internalCall" ONLY WORKS FOR B3 FUNCTIONS
 			// (A B3 Function simply calls its funcExpr
 			// and relies on the context to hold the parameters. A Java function makes use of the
 			// parameters. The context prepared for the call (by BContext) knows about the parameters,
 			// so, when a Java Based Builder is supported, more work is required here (alternatively
 			// refactor the internalCall to always make used of the context for parameters).
 			//
 			Object br = evaluator.doCall(builder, new Object[] {}, new Type[] {}, ctx, false);
 			// Object br = builder.internalCall(ctx, new Object[] {}, new Type[] {});
 
 			// A return of null means there was no funcExpression (or that the funcExpression returned
 			// null explicitly). In both cases - this means that (in decreasing order of significance) the
 			// first specified of "output", "source", or "input" should be returned
 			// Lastly, if none of these were specified, empty output is returned.
 			//
 			String returnedBuildSetName = "result";
 			;
 			if(br == null)
 				br = builder.getOutput() != null
 						? ctx.getValue(returnedBuildSetName = "output")
 						: null;
 			if(br == null)
 				br = builder.getSource() != null
 						? ctx.getValue(returnedBuildSetName = "source")
 						: null;
 			if(br == null)
 				br = builder.getInput() != null
 						? ctx.getValue(returnedBuildSetName = "input")
 						: null;
 
 			// if still null, use an empty output result
 			if(br == null)
 				br = B3BuildFactory.eINSTANCE.createBuildSet();
 
 			if(!(br instanceof BuildSet))
 				throw new B3WrongBuilderReturnTypeException(
 					unit.getName(), builder.getName(), returnedBuildSetName, br.getClass());
 
 			BuildSet buildResult = (BuildSet) br;
 
 			// PROCESS DEFAULT ANNOTATIONS
 			// If the returned value is the result of processing output (and there was output declared)
 			// then, evaluate the default annotation properties. In all other scenarios, it is the user's
 			// responsibility to set/process these.
 			//
 			if(buildResult == output && builder.getOutput() != null) {
 				BPropertySet propertySet = builder.getOutput().getAnnotations();
 				if(propertySet != null) {
 					// BuildResultContext is specially constructed for the purpose of collecting
 					// property values as used here.
 					BuildResultContext specialContext = B3BuildFactory.eINSTANCE.createBuildResultContext();
 					specialContext.setParentContext(ctx); // current scope is visible.
 					// if calls are made when evaluating properties the correct outer context is needed
 					specialContext.setOuterContext(ctx instanceof BInnerContext
 							? ((BInnerContext) ctx).getOuterContext()
 							: ctx);
 
 					// Make sure special context is initialized with the values from the produced
 					// output (if any were set). (Can not simply use setValueMap, as the values would then
 					// be missing from the BuildResult (both include a ValueMap by containment), and
 					// user code may refer to values in the output...)
 					specialContext.getValueMap().merge(output.getValueMap());
 
 					// Evaluate in an inner context wrapper to ensure the special context's value map
 					// does not get polluted with local variables (since the special context is a
 					// property scope, it will only have properties set in its value map).
 					// Evaluate default properties with allVisible == false to ensure that
 					// only the special context is consulted for already set values (i.e. to determine
 					// if the default should be used or not, as opposed to evaluating properties and values
 					// visible in the context).
 					evaluator.doEvaluateDefaults(propertySet, specialContext.createInnerContext(), false);
 					// propertySet.evaluateDefaults(specialContext.createInnerContext(), false);
 
 					// Steal the value map from the special context and use it in the result
 					// (the context is forgotten at this point and does not need its values).
 					output.setValueMap(specialContext.getValueMap());
 				}
 			}
 
 			// POST CONDITION
 			// just evaluate, supposed to throw exception if not acceptable
 			// make the actual returned value available
 			ctx.defineFinalValue("builder", buildResult, BuildSet.class);
 			tmp = builder.getPostcondExpr();
 			if(tmp != null)
 				evaluator.doEvaluate(tmp, ctx);
 
 			// All done, return an OK status with the result set. (Partial grouped/aliased results are not visible
 			// to caller).
 			return new B3BuilderStatus(buildResult);
 
 		}
 		catch(OperationCanceledException e) {
 			return B3BuilderStatus.CANCEL_STATUS;
 
 		}
 		catch(Throwable t) {
			return B3BuilderStatus.error("Builder Job \"" + this.getName() + "\" Failed - see details", t);
 		}
 	}
 
 	/**
 	 * Collect result, merges results according to aliases/groups, and assigns these as
 	 * unmodifiable values in the context.
 	 * 
 	 * @param jobsToRun
 	 * @return MultiStatus if result was not ok, otherwise B3BuilderStatus
 	 */
 	private IStatus collectResult(List<B3BuilderJob> jobsToRun) throws B3EngineException {
 		MultiStatus ms = new MultiStatus(
 			B3BuildActivator.instance.getBundle().getSymbolicName(), 0,
 			"One or several build jobs ended with error or were canceled", null);
 		for(B3BuilderJob job : jobsToRun) {
 			IStatus s = job.getResult();
 			if(s == null) {
 				// if never started jobs are included, the overall status becomes CANCELED
 				// s = Status.CANCEL_STATUS; // unfinished, never scheduled etc...
 				continue;
 			}
 			ms.add(s);
 		}
 		if(!ms.isOK())
 			return ms;
 
 		// collection of all as "input", and collect per "alias"
 		//
 		// create the resulting map, and make sure there is at least an empty BuildResult
 		// (i.e. when no effective input was declared).
 		Map<String, BuildSet> resultMap = new HashMap<String, BuildSet>();
 		resultMap.put("input", B3BuildFactory.eINSTANCE.createBuildSet());
 
 		for(B3BuilderJob job : jobsToRun) {
 			BuildSet r = job.getBuildResult();
 			for(String alias : job.getAliases())
 				mergeResult(alias, r, resultMap);
 			mergeResult("input", r, resultMap); // all are added to "input"
 		}
 		// define all the BuildResults in the context per respective name
 		for(String key : resultMap.keySet())
 			ctx.defineFinalValue(key, resultMap.get(key), BuildSet.class);
 
 		return Status.OK_STATUS;
 	}
 
 	private List<String> getAliases() {
 		if(aliases == null) {
 			return Collections.emptyList();
 		}
 		return aliases;
 	}
 
 	/**
 	 * Merges result per key
 	 * 
 	 * @param key
 	 * @param add
 	 * @param resultMap
 	 * @throws B3EngineException
 	 *             - if merging values causes type or immutable violation
 	 */
 	private void mergeResult(String key, BuildSet add, Map<String, BuildSet> resultMap) throws B3EngineException {
 		BuildSet buildResult = resultMap.get(key);
 		if(buildResult == null)
 			resultMap.put(key, buildResult = B3BuildFactory.eINSTANCE.createBuildSet());
 
 		// merge the job result to add into the buildResult
 		buildResult.merge(add);
 	}
 
 	private void runParallel(List<B3BuilderJob> jobsToRun, IProgressMonitor monitor) {
 		for(B3BuilderJob job : jobsToRun) {
 			if(monitor.isCanceled())
 				throw new OperationCanceledException();
 			job.setFamily(this);
 			job.schedule();
 		}
 
 		try {
 			// wait for all of the scheduled jobs.
 			getJobManager().join(this, monitor);
 
 		}
 		catch(InterruptedException e) {
 			// TODO What to do on interrupted? There should be no interruptions...
 			// Maybe have some watch dog that times out if a job takes too long (? hours?)
 			e.printStackTrace();
 		}
 		// in case the wait was canceled
 		if(monitor.isCanceled())
 			throw new OperationCanceledException();
 
 	}
 
 	private void runSequential(List<B3BuilderJob> jobsToRun, IProgressMonitor monitor) {
 		for(B3BuilderJob job : jobsToRun) {
 			if(monitor.isCanceled())
 				throw new OperationCanceledException();
 			job.schedule();
 			try {
 				job.join();
 			}
 			catch(InterruptedException e) {
 				// TODO What to do on interrupted? There should be no interruptions...
 				// Maybe have some watch dog that times out if a job takes too long (? hours?)
 				e.printStackTrace();
 			}
 			if(job.getResult() == null || !job.getResult().isOK())
 				return; // stop scheduling, let collect result deal with what is wrong
 		}
 
 	}
 
 	private void setAliases(List<String> aliases) {
 		this.aliases = aliases;
 	}
 
 	private void setFamily(B3BuilderJob parent) {
 		this.parent = parent;
 	}
 }
