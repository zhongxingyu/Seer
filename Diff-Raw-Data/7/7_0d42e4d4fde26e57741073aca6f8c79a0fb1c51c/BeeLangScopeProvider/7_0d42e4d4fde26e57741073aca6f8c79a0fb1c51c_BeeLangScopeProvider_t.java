 package org.eclipse.b3.scoping;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 
 import org.eclipse.b3.backend.evaluator.b3backend.B3FunctionType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3JavaImport;
 import org.eclipse.b3.backend.evaluator.b3backend.B3ParameterizedType;
 import org.eclipse.b3.beeLang.BeeModel;
 import org.eclipse.b3.build.build.AliasedRequiredCapability;
 import org.eclipse.b3.build.build.BuilderReference;
 import org.eclipse.b3.build.build.IRequiredCapabilityContainer;
 import org.eclipse.b3.build.build.RequiredCapability;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.xtext.resource.EObjectDescription;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.scoping.IScope;
 import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider;
 import org.eclipse.xtext.scoping.impl.SimpleScope;
 
 /**
  * This class contains custom scoping description.
  * 
  * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#scoping
  * on how and when to use it 
  *
  */
 public class BeeLangScopeProvider extends AbstractDeclarativeScopeProvider {
 //	IScope scope_B3ParameterizedType_rawType(B3ParameterizedType ctx, EReference ref) {
 	IScope scope_IType(B3ParameterizedType ctx, EReference ref) {
 		EList<EObject> x = ctx.eResource().getContents();
 		// create an Iterable of IEObjectDescription - instances of EObjectDescription
 		//
 		ArrayList<IEObjectDescription> result = new ArrayList<IEObjectDescription>();
 		for(EObject y: x) {
 			if(y instanceof BeeModel) {
 				for(Type t : ((BeeModel)y).getImports())
 				if(t instanceof B3JavaImport)
 					result.add(new EObjectDescription(((B3JavaImport)t).getName(),(B3JavaImport)t, null));
 			}
 		}
 		return new SimpleScope(result);
 	}
 	IScope scope_IType(B3FunctionType ctx, EReference ref) {
 		EList<EObject> x = ctx.eResource().getContents();
 		// create an Iterable of IEObjectDescription - instances of EObjectDescription
 		//
 		ArrayList<IEObjectDescription> result = new ArrayList<IEObjectDescription>();
 		for(EObject y: x) {
 			if(y instanceof BeeModel) {
 				for(Type t : ((BeeModel)y).getImports())
 				if(t instanceof B3JavaImport)
 					result.add(new EObjectDescription(((B3JavaImport)t).getName(),(B3JavaImport)t, null));
 			}
 		}
 		return new SimpleScope(result);
 	}
 	/**
 	 * Finds aliased required capabilities in a containing element.
 	 * @param ctx
 	 * @param ref
 	 * @return
 	 */
 	IScope scope_AliasedRequiredCapability(BuilderReference ctx, EReference ref) {
 		ArrayList<IEObjectDescription> result = new ArrayList<IEObjectDescription>();
 		EObject container = ctx.eContainer();
 		while(container != null) {
 			if(container instanceof IRequiredCapabilityContainer) {
 				for(RequiredCapability rc : ((IRequiredCapabilityContainer)container).getRequiredCapabilities())
					if(rc instanceof AliasedRequiredCapability) {
						String name = ((AliasedRequiredCapability)rc).getAlias();
						if(name != null)
							result.add(new EObjectDescription(name, rc, null));
					}
 			}
 			container = container.eContainer();
 		}
 		if(result.isEmpty())
 			return IScope.NULLSCOPE;
 		return new SimpleScope(result);
 	}
 }
