 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D.Willink - Initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.pivot.context;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.ocl.examples.pivot.ExpressionInOcl;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 
 /**
  * EInvocationContext supports parsing OCL expressions in the context of a query,
  * which is an Ecore Class and Ecore Parameters.
  */
 public class EInvocationContext extends EClassContext
 {
 	private final Map<String, EClassifier> eParameters;
 	private Map<String, Type> parameters = null;
 	
 	public EInvocationContext(MetaModelManager metaModelManager, URI uri, EClassifier eClassContext, Map<String, EClassifier> eParameters) {
 		super(metaModelManager, uri, eClassContext);
 		this.eParameters = eParameters;
 	}
 
 	public Map<String, Type> getParameters() {
 		if (parameters == null) {
 			parameters = new HashMap<String, Type>();
			for (Map.Entry<String, EClassifier> entry : eParameters.entrySet()) {
				Type type = metaModelManager.getPivotOfEcore(Type.class, entry.getValue());
				parameters.put(entry.getKey(), type);
 			}
 		}
 		return parameters;
 	}
 
 	@Override
 	public void initialize(Base2PivotConversion conversion, ExpressionInOcl expression) {
 		super.initialize(conversion, expression);
 		conversion.setParameterVariables(expression, getParameters());
 	}
 }
