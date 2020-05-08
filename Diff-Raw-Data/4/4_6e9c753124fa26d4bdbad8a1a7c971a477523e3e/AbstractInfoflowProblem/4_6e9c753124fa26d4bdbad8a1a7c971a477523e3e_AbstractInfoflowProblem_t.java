 /*******************************************************************************
  * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
  * Bodden, and others.
  ******************************************************************************/
 package soot.jimple.infoflow.problems;
 
 import heros.InterproceduralCFG;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import soot.ArrayType;
 import soot.IntType;
 import soot.LongType;
 import soot.PrimType;
 import soot.RefType;
 import soot.Scene;
 import soot.SootClass;
 import soot.SootMethod;
 import soot.Type;
 import soot.Unit;
 import soot.Value;
 import soot.jimple.ArrayRef;
 import soot.jimple.Constant;
 import soot.jimple.InstanceFieldRef;
 import soot.jimple.infoflow.data.Abstraction;
 import soot.jimple.infoflow.data.AccessPath;
 import soot.jimple.infoflow.handlers.TaintPropagationHandler;
 import soot.jimple.infoflow.nativ.DefaultNativeCallHandler;
 import soot.jimple.infoflow.nativ.NativeCallHandler;
 import soot.jimple.infoflow.solver.IInfoflowCFG;
 import soot.jimple.infoflow.solver.IInfoflowSolver;
 import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
 import soot.jimple.infoflow.util.DataTypeHandler;
 import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;
 /**
  * abstract super class which 
  * 	- concentrates functionality used by InfoflowProblem and BackwardsInfoflowProblem
  *  - contains helper functions which should not pollute the naturally large InfofflowProblems
  *
  */
 public abstract class AbstractInfoflowProblem extends DefaultJimpleIFDSTabulationProblem<Abstraction, InterproceduralCFG<Unit, SootMethod>> {
 
 	protected final Map<Unit, Set<Abstraction>> initialSeeds = new HashMap<Unit, Set<Abstraction>>();
 	protected ITaintPropagationWrapper taintWrapper;
 	protected NativeCallHandler ncHandler = new DefaultNativeCallHandler();
 	
 	protected boolean enableImplicitFlows = false;
 	protected boolean enableStaticFields = true;
 	protected boolean enableExceptions = true;
 	protected boolean flowSensitiveAliasing = true;
 
 	protected boolean inspectSources = true;
 	protected boolean inspectSinks = true;
 
 	Abstraction zeroValue = null;
 	
 	protected IInfoflowSolver solver = null;
 	
 	protected boolean stopAfterFirstFlow = false;
 	
 	protected Set<TaintPropagationHandler> taintPropagationHandlers = new HashSet<TaintPropagationHandler>();
 	
 	public AbstractInfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
 		super(icfg);
 	}
 	
 	protected boolean canCastType(Type destType, Type sourceType) {
		// If we don't have a source type, we generally allow the cast
		if (sourceType == null)
			return true;
		
 		if (Scene.v().getFastHierarchy().canStoreType(destType, sourceType) // cast-up, i.e. Object to String
 				|| Scene.v().getFastHierarchy().canStoreType(sourceType, destType)) // cast-down, i.e. String to Object
 			return true;
 		
 		if (destType instanceof PrimType && sourceType instanceof PrimType)
 			if (sourceType instanceof LongType && destType instanceof IntType
 					|| destType instanceof LongType && sourceType instanceof IntType)
 				return true;
 			
 		return false;
 	}
 		
 	protected boolean hasCompatibleTypesForCall(AccessPath apBase, SootClass dest) {
 		// Cannot invoke a method on a primitive type
 		if (apBase.getType() instanceof PrimType)
 			return false;
 		// Cannot invoke a method on an array
 		if (apBase.getType() instanceof ArrayType)
 			return dest.getName().equals("java.lang.Object");
 		
 		return Scene.v().getFastHierarchy().canStoreType(apBase.getType(), dest.getType())
 				|| Scene.v().getFastHierarchy().canStoreType(dest.getType(), apBase.getType());
 	}
 
 	public void setSolver(IInfoflowSolver solver) {
 		this.solver = solver;
 	}
 	
 	public void setZeroValue(Abstraction zeroValue) {
 		this.zeroValue = zeroValue;
 	}
 
 	/**
 	 * we need this option as we start directly at the sources, but need to go 
 	 * backward in the call stack
 	 */
 	@Override
 	public boolean followReturnsPastSeeds(){
 		return true;
 	}
 	
 	public void setTaintWrapper(ITaintPropagationWrapper wrapper){
 		taintWrapper = wrapper;
 	}
 		
 	/**
 	 * Sets whether the information flow analysis shall stop after the first
 	 * flow has been found
 	 * @param stopAfterFirstFlow True if the analysis shall stop after the
 	 * first flow has been found, otherwise false.
 	 */
 	public void setStopAfterFirstFlow(boolean stopAfterFirstFlow) {
 		this.stopAfterFirstFlow = stopAfterFirstFlow;
 	}
 		
 	/**
 	 * Sets whether the solver shall consider implicit flows.
 	 * @param enableImplicitFlows True if implicit flows shall be considered,
 	 * otherwise false.
 	 */
 	public void setEnableImplicitFlows(boolean enableImplicitFlows) {
 		this.enableImplicitFlows = enableImplicitFlows;
 	}
 
 	/**
 	 * Sets whether the solver shall consider assignments to static fields.
 	 * @param enableStaticFields True if assignments to static fields shall be
 	 * tracked, otherwise false
 	 */
 	public void setEnableStaticFieldTracking(boolean enableStaticFields) {
 		this.enableStaticFields = enableStaticFields;
 	}
 
 	/**
 	 * Sets whether the solver shall track taints over exceptions, i.e. throw
 	 * new RuntimeException(secretData).
 	 * @param enableExceptions True if taints in thrown exception objects shall
 	 * be tracked.
 	 */
 	public void setEnableExceptionTracking(boolean enableExceptions) {
 		this.enableExceptions = enableExceptions;
 	}
 
 	/**
 	 * Sets whether the solver shall use flow sensitive aliasing. This makes
 	 * the analysis more precise, but also requires more time.
 	 * @param flowSensitiveAliasing True if flow sensitive aliasing shall be
 	 * used, otherwise false
 	 */
 	public void setFlowSensitiveAliasing(boolean flowSensitiveAliasing) {
 		this.flowSensitiveAliasing = flowSensitiveAliasing;
 	}
 
 	@Override
 	public Abstraction createZeroValue() {
 		if (zeroValue == null)
 			zeroValue = Abstraction.getZeroAbstraction(flowSensitiveAliasing);
 		return zeroValue;
 	}
 
 	/**
 	 * Gets whether the given method is an entry point, i.e. one of the initial
 	 * seeds belongs to the given method
 	 * @param sm The method to check
 	 * @return True if the given method is an entry point, otherwise false
 	 */
 	protected boolean isInitialMethod(SootMethod sm) {
 		for (Unit u : this.initialSeeds.keySet())
 			if (interproceduralCFG().getMethodOf(u) == sm)
 				return true;
 		return false;
 	}
 	
 	@Override
 	public Map<Unit, Set<Abstraction>> initialSeeds() {
 		return initialSeeds;
 	}
 	
 	/**
 	 * performance improvement: since we start directly at the sources, we do not 
 	 * need to generate additional taints unconditionally
 	 */
 	@Override
 	public boolean autoAddZero() {
 		return false;
 	}
 	
 	/**
 	 * default: inspectSources is set to true, this means sources are analyzed as well.
 	 * If inspectSources is set to false, then the analysis does not propagate values into 
 	 * the source method. 
 	 * @param inspect boolean that determines the inspectSource option
 	 */
 	public void setInspectSources(boolean inspect){
 		inspectSources = inspect;
 	}
 
 	/**
 	 * default: inspectSinks is set to true, this means sinks are analyzed as well.
 	 * If inspectSinks is set to false, then the analysis does not propagate values into 
 	 * the sink method. 
 	 * @param inspect boolean that determines the inspectSink option
 	 */
 	public void setInspectSinks(boolean inspect){
 		inspectSinks = inspect;
 	}
 	
 	/**
 	 * we cannot rely just on "real" heap objects, but must also inspect locals because of Jimple's representation ($r0 =... )
 	 * @param val the value which gets tainted
 	 * @param source the source from which the taints comes from. Important if not the value, but a field is tainted
 	 * @return true if a reverseFlow should be triggered or an inactive taint should be propagated (= resulting object is stored in heap = alias)
 	 */
 	public boolean triggerInaktiveTaintOrReverseFlow(Value val, Abstraction source){
 		if(val == null){
 			return false;
 		}
 		//no string
 		if(!(val instanceof InstanceFieldRef) && !(val instanceof ArrayRef) 
 				&& val.getType() instanceof RefType && ((RefType)val.getType()).getClassName().equals("java.lang.String")){
 			return false;
 		}
 		if(val instanceof InstanceFieldRef && ((InstanceFieldRef)val).getBase().getType() instanceof RefType &&
 				 ((RefType)((InstanceFieldRef)val).getBase().getType()).getClassName().equals("java.lang.String")){
 			return false;
 		}
 		if(val.getType() instanceof PrimType){
 			return false;
 		}
 		if(val instanceof Constant)
 			return false;
 		
 		if(DataTypeHandler.isFieldRefOrArrayRef(val)
 				|| source.getAccessPath().isInstanceFieldRef()
 				|| source.getAccessPath().isStaticFieldRef()
 				|| (val.getType() instanceof RefType && source.getAccessPath().getType() instanceof RefType))
 			return true;
 		
 		return false;
 	}
 	
 	@Override
 	public IInfoflowCFG interproceduralCFG() {
 		return (IInfoflowCFG) super.interproceduralCFG();
 	}
 	
 	/**
 	 * Adds the given initial seeds to the information flow problem
 	 * @param unit The unit to be considered as a seed
 	 * @param seeds The abstractions with which to start at the given seed
 	 */
 	public void addInitialSeeds(Unit unit, Set<Abstraction> seeds) {
 		if (this.initialSeeds.containsKey(unit))
 			this.initialSeeds.get(unit).addAll(seeds);
 		else
 			this.initialSeeds.put(unit, new HashSet<Abstraction>(seeds));
 	}
 	
 	/**
 	 * Gets whether this information flow problem has initial seeds
 	 * @return True if this information flow problem has initial seeds,
 	 * otherwise false
 	 */
 	public boolean hasInitialSeeds() {
 		return !this.initialSeeds.isEmpty();
 	}
 
 	/**
 	 * Gets the initial seeds with which this information flow problem has been
 	 * configured
 	 * @return The initial seeds with which this information flow problem has
 	 * been configured.
 	 */
 	public Map<Unit, Set<Abstraction>> getInitialSeeds() {
 		return this.initialSeeds;
 	}
 	
 	/**
 	 * Adds a handler which is invoked whenever a taint is propagated
 	 * @param handler The handler to be invoked when propagating taints
 	 */
 	public void addTaintPropagationHandler(TaintPropagationHandler handler) {
 		this.taintPropagationHandlers.add(handler);
 	}
 	
 }
