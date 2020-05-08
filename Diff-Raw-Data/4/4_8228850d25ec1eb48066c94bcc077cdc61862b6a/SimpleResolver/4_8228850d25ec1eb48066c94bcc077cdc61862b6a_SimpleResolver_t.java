 /**
  * Copyright (c) 2010, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.build.core;
 
 import org.eclipse.b3.backend.core.B3InternalError;
 import org.eclipse.b3.backend.core.B3NoSuchVariableException;
 import org.eclipse.b3.backend.evaluator.IB3Evaluator;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.build.B3BuildFactory;
 import org.eclipse.b3.build.BeeModel;
 import org.eclipse.b3.build.BuildUnit;
 import org.eclipse.b3.build.DelegatingUnitProvider;
 import org.eclipse.b3.build.EffectiveRequirementFacade;
 import org.eclipse.b3.build.EffectiveUnitFacade;
 import org.eclipse.b3.build.FirstFoundUnitProvider;
 import org.eclipse.b3.build.RepositoryUnitProvider;
 import org.eclipse.b3.build.RequiredCapability;
 import org.eclipse.b3.build.ResolutionInfo;
 import org.eclipse.b3.build.UnitProvider;
 import org.eclipse.b3.build.UnitResolutionInfo;
 import org.eclipse.b3.build.internal.B3BuildActivator;
 import org.eclipse.b3.build.repository.IBuildUnitResolver;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 
 import com.google.inject.Inject;
 
 /**
  * A simplistic (brute force) resolver that resolves all units defined in the specified context.
  * An attempt is made to satisfy all effective requirements by resolving each against the
  * effective requirement's context's view of repository configuration.
  * 
  */
 public class SimpleResolver implements IBuildUnitResolver {
 	@Inject
 	IB3Evaluator evaluator;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.b3.build.core.IBuildUnitResolver#resolveAll(org.eclipse.b3.build.BuildContext)
 	 */
 	public IStatus resolveAll(BExecutionContext ctx) {
 		// System.err.print("RESOLVE ALL\n");
 		EffectiveUnitIterator uItor = new EffectiveUnitIterator(ctx);
 		// ctx.printStackTrace();
 		MultiStatus ms = new MultiStatus(B3BuildActivator.instance.getBundle().getSymbolicName(), 0, "", null);
 		while(uItor.hasNext())
 			ms.add(resolveUnit(uItor.next(), ctx));
 		return ms;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.b3.build.core.IBuildUnitResolver#resolveUnit(org.eclipse.b3.build.BuildUnit, org.eclipse.b3.build.BuildContext)
 	 */
 	public IStatus resolveUnit(BuildUnit unit, BExecutionContext ctx) {
 		// System.err.printf("RESOLVING UNIT: %s", unit.getName());
 
 		// ALREADY RESOLVED
 		// check if the unit is already resolved
 		final ResolutionInfoAdapter unitAdapter = ResolutionInfoAdapterFactory.eINSTANCE.adapt(unit);
 		final UnitResolutionInfo knownUnitResolutionInfo = (UnitResolutionInfo) unitAdapter.getAssociatedInfo(this);
 
 		if(knownUnitResolutionInfo != null && knownUnitResolutionInfo.getStatus().isOK()) {
 			// System.err.printf(" ALREADY RESOLVED - %s\n", ri.getStatus().getCode() == IStatus.OK
 			// ? "OK"
 			// : "FAIL");
 			return knownUnitResolutionInfo.getStatus();
 		}
 		// GIVE THE UNIT A NEW RESOLUTION INFO
 		final UnitResolutionInfo resultingUnitResolutionInfo = B3BuildFactory.eINSTANCE.createUnitResolutionInfo();
 		unitAdapter.setAssociatedInfo(this, resultingUnitResolutionInfo);
 		resultingUnitResolutionInfo.setUnit(unit);
 
 		// System.err.printf(" processing\n");
 
 		// DEFINE UNIT IF NOT DEFINED
 		// check if the unit is defined, and define it (and its parent BeeModel) if not
 		BuildUnit u = ctx.getSomeThing(BuildUnit.class, BuildUnitProxyAdapterFactory.eINSTANCE.adapt(unit).getIface());
 
 		// BuildUnit u = ctx.getEffectiveBuildUnit(unit);
 		if(u == null) {
 			// System.err.printf("Unit: %s, not defined - defining\n", unit.getName());
 
 			ctx = ctx.createOuterContext();
 			try {
 				if(unit.eContainer() instanceof BeeModel)
 					evaluator.doEvaluate(unit.eContainer(), ctx);
 				else
 					evaluator.doDefine(unit, ctx);
 				// ctx.defineBuildUnit(unit, false);
 				u = ctx.getSomeThing(BuildUnit.class, BuildUnitProxyAdapterFactory.eINSTANCE.adapt(unit).getIface());
 				// if(u == null) {
 				// System.err.printf("Definition of unit: %s failed\n", unit.getName());
 				// }
 			}
 			catch(Throwable e) {
 				resultingUnitResolutionInfo.setContext(ctx); // may be bad...
 				resultingUnitResolutionInfo.setStatus(new Status(
 					IStatus.ERROR, B3BuildActivator.instance.getBundle().getSymbolicName(),
 					"Resolution failed with exception when defining a candidate unit", e));
 				return resultingUnitResolutionInfo.getStatus(); // give up on unit
 			}
 		}
 
 		// EFFECTIVE UNIT
 		EffectiveUnitFacade uFacade;
 		try {
 			uFacade = unit.getEffectiveFacade(ctx);
 			ctx = uFacade.getContext();
 		}
 		catch(Throwable e1) {
 			resultingUnitResolutionInfo.setContext(ctx); // may be bad...
 			resultingUnitResolutionInfo.setStatus(new Status(
 				IStatus.ERROR, B3BuildActivator.instance.getBundle().getSymbolicName(),
 				"Resolution failed with exception when getting effective unit", e1));
 			return resultingUnitResolutionInfo.getStatus(); // give up on unit
 		}
 		// the unit being resolved should always have this context
 		// it's status may be set
 		resultingUnitResolutionInfo.setContext(ctx);
 
 		// PROCESS ALL REQUIREMENTS
 		// If there are no requirements, return OK status after having updated the Unit's resolution Info.
 		EList<EffectiveRequirementFacade> requiredCapabilities = uFacade.getRequiredCapabilities();
 		// trivial case - no requirements
 		if(requiredCapabilities.size() < 1) {
 			resultingUnitResolutionInfo.setStatus(Status.OK_STATUS);
 			// System.err.print("    OK - NO REQUIREMENTS\n");
 			return resultingUnitResolutionInfo.getStatus();
 		}
 
 		// Satisfy all requirements
 		//
 		final MultiStatus ms = new MultiStatus(B3BuildActivator.instance.getBundle().getSymbolicName(), 0, "", null);
 		resultingUnitResolutionInfo.setStatus(ms);
 
 		// create a stack provider (to be used in front of any defined providers)
 		final RepositoryUnitProvider stackProvider = B3BuildFactory.eINSTANCE.createRepositoryUnitProvider();
 		stackProvider.setBuildUnitRepository(B3BuildFactory.eINSTANCE.createExecutionStackRepository());
 
 		for(EffectiveRequirementFacade ereq : requiredCapabilities) {
 			RequiredCapability r = ereq.getRequirement();
 
 			// POSSIBLE BAD MODEL STATE
 			if(r == null) {
 				// bad model state (should have been caught by validation).
 				// override the ms already set, it is not needed, better to set the error status directly
 				// as there are no other statuses to collect (requirements are null).
 				resultingUnitResolutionInfo.setStatus(new Status(
 					IStatus.ERROR, B3BuildActivator.instance.getBundle().getSymbolicName(),
 					"Unit contains null requirement"));
 				// System.err.print("    FAIL - NULL EFFECTIVE REQUIREMENTS\n");
 				return resultingUnitResolutionInfo.getStatus();
 			}
 			// GET regAdapter, to associate result with requirement (i.e. what it resolved to).
 			final ResolutionInfoAdapter reqAdapter = ResolutionInfoAdapterFactory.eINSTANCE.adapt(r);
 
 			// System.err.printf("    REQUIREMENT %s, %s - has status: %s\n", r.getName(), r.getVersionRange(), ri == null
 			// ? "UNRESOLVED"
 			// : ri.getStatus().getCode() == IStatus.OK
 			// ? "OK"
 			// : "FAIL");
 
 			// A single requirement can be used multiple times (declared in unit, used in several builders),
 			// so it may already have been processed
 			if(reqAdapter.getAssociatedInfo(this) != null)
 				continue; // already processed and it has a status (ok, error, cancel)
 
 			// get the effective unit providers to use for resolution
 			try {
 				UnitProvider repos = null;
 				UnitProvider providerToUse = stackProvider;
 				try {
 					repos = UnitProvider.class.cast(ereq.getContext().getValue(
 						B3BuildConstants.B3ENGINE_VAR_UNITPROVIDERS));
 				}
 				catch(B3NoSuchVariableException e) {
 					// ignore - repos remain null
 				}
 				// if providers were configured, combine them with a first found using the stack
 				// if combination of multiple provider definitions should be performed, this is up
 				// to whoever assigns B3ENGINE_CAR_UNITPROVIDERS (i.e. delegate first, or last).
 				//
 				if(repos != null) {
 					FirstFoundUnitProvider ff = B3BuildFactory.eINSTANCE.createFirstFoundUnitProvider();
 					EList<UnitProvider> plist = ff.getProviders();
 					plist.add(stackProvider);
 					DelegatingUnitProvider delegate = B3BuildFactory.eINSTANCE.createDelegatingUnitProvider();
 					delegate.setDelegate(repos);
 					plist.add(delegate);
 					providerToUse = ff;
 				}
 
 				// GET THE UNIT FROM THE REPOSITORY CONFIGURATION
 				// note effective requirement has reference to the context to use
 				BuildUnit result = providerToUse.resolve(ereq);
 				if(result == null) {
 					// System.err.printf("        UNRESOLVED - provider did not find it.\n");
 
 					ms.add(new Status(
 						IStatus.WARNING, B3BuildActivator.instance.getBundle().getSymbolicName(), "Unresolved."));
 					reqAdapter.setAssociatedInfo(this, resultingUnitResolutionInfo);
 				}
 				else {
 					// System.err.printf("        RESOLVED - provider found match.\n");
 					// SET THE REQUIREMENT's RESOLUTION INFO -> Resolved Unit
 					final UnitResolutionInfo unitRi = B3BuildFactory.eINSTANCE.createUnitResolutionInfo();
 					unitRi.setUnit(result);
 					unitRi.setStatus(Status.OK_STATUS); // prevent recursion
 
 					if(ereq.getContext() == null) {
 						throw new B3InternalError("Effective Requirement found with null context");
 					}
 					unitRi.setContext(ereq.getContext()); // the context in which the requirement was resolved.
 					// update status with resulting status graph (i.e. both on requirement, and result for unit being
 					// resolved.
 					unitRi.setStatus(resolveUnit(result, ctx));
 					ms.add(unitRi.getStatus());
 					reqAdapter.setAssociatedInfo(this, unitRi);
 
					// // destroy to see error output
					// resultingUnitResolutionInfo.setContext(null); // CRASH !!
 				}
 			}
 			catch(Throwable e) {
 				// System.err.printf("        ERROR - %s, %s.\n", e.getClass().getName(), e.getMessage());
 				e.printStackTrace();
 
 				// associate the error information with the requirement
 				final ResolutionInfo ri = B3BuildFactory.eINSTANCE.createResolutionInfo();
 				ri.setStatus(new Status(
 					IStatus.ERROR, B3BuildActivator.instance.getBundle().getSymbolicName(),
 					"Resolution failed with exception", e));
 				reqAdapter.setAssociatedInfo(this, ri);
 				ms.add(ri.getStatus());
 			}
 
 		}
 		resultingUnitResolutionInfo.setStatus(ms);
 
 		// // update the unit with the status information from resolving all of its requirements
 		// ri = B3BuildFactory.eINSTANCE.createResolutionInfo();
 		// ri.setStatus(ms);
 		// unitAdapter.setAssociatedInfo(this, ri);
 		return ms;
 	}
 }
