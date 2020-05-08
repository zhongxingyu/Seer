 /**
  * <copyright>
  *
  * Copyright (c) 2010 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: BaseScopeProvider.java,v 1.4 2011/04/20 19:02:27 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.base.scoping.cs;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.ocl.examples.common.utils.TracingOption;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceAdapter;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementCS;
 import org.eclipse.ocl.examples.xtext.base.scope.BaseScopeView;
 import org.eclipse.ocl.examples.xtext.base.utilities.ElementUtil;
 import org.eclipse.xtext.common.types.TypesPackage;
 import org.eclipse.xtext.scoping.IGlobalScopeProvider;
 import org.eclipse.xtext.scoping.IScope;
 import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider;
 
 import com.google.inject.Inject;
 
 /**
  * This class contains custom scoping description.
  * 
  * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#scoping on
  * how and when to use it
  * 
  */
 public class BaseScopeProvider extends AbstractDeclarativeScopeProvider
 {
 	@Inject
 	private IGlobalScopeProvider globalScopeProvider;
 
 	public static final TracingOption LOOKUP = new TracingOption(
 		"org.eclipse.ocl.examples.xtext.base", "lookup"); //$NON-NLS-1$//$NON-NLS-2$
 
 	@Override
 	public IScope getScope(EObject context, EReference reference) {
 		Resource csResource = context.eResource();
 		if (reference.getEReferenceType().getEPackage().getNsURI().equals(TypesPackage.eNS_URI)) {
 			return globalScopeProvider.getScope(csResource, reference, null);
 		}
 		MetaModelManagerResourceAdapter adapter = MetaModelManagerResourceAdapter.findAdapter(csResource);
 		if (adapter == null) {
			return null;
 		}
 		MetaModelManager metaModelManager = adapter.getMetaModelManager();
 		ElementCS csElement = (ElementCS) context;
 		CSScopeAdapter scopeAdapter = ElementUtil.getScopeAdapter(csElement);
 		if (scopeAdapter == null) {
			return null;
 		}
 		return new BaseScopeView(metaModelManager, csElement, scopeAdapter, null, reference, reference);
 	}
 }
