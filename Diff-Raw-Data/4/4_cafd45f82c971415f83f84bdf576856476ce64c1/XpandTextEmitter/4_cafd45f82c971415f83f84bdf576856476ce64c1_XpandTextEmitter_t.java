 /*
  * Copyright (c) 2007, 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.common.codegen;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.common.codegen.ImportAssistant;
 import org.eclipse.gmf.internal.xpand.BufferOutput;
 import org.eclipse.gmf.internal.xpand.ResourceManager;
 import org.eclipse.gmf.internal.xpand.XpandFacade;
import org.eclipse.gmf.internal.xpand.model.AmbiguousDefinitionException;
 import org.eclipse.gmf.internal.xpand.model.EvaluationException;
 import org.eclipse.gmf.internal.xpand.model.ExecutionContext;
 import org.eclipse.gmf.internal.xpand.model.Variable;
 import org.eclipse.gmf.internal.xpand.util.ContextFactory;
 
 /**
  * @author artem
  */
 public class XpandTextEmitter implements TextEmitter {
 	private final ResourceManager myResourceManager;
 	private final String myTemplateFQN;
 	private final ClassLoader myContext;
 	private final List<Variable> myGlobals;
 
 	public XpandTextEmitter(ResourceManager manager, String templateFQN, ClassLoader context) {
 		this(manager, templateFQN, context, null);
 	}
 
 	public XpandTextEmitter(ResourceManager manager, String templateFQN, ClassLoader context, Map<String, Object> globals) {
 		assert manager != null && templateFQN != null;
 		myResourceManager = manager;
 		myTemplateFQN = templateFQN;
 		myContext = context;
 		if (globals != null && globals.size() > 0) {
 			myGlobals = new ArrayList<Variable>(globals.size());
 			for (Map.Entry<String, Object> e : globals.entrySet()) {
 				assert e.getValue() instanceof EObject;
 				myGlobals.add(new Variable(e.getKey(), ((EObject) e.getValue()).eClass(), e.getValue()));
 			}
 		} else {
 			myGlobals = Collections.<Variable>emptyList();
 		}
 	}
 
 	public String generate(IProgressMonitor monitor, Object[] arguments) throws InterruptedException, InvocationTargetException {
 		if (monitor != null && monitor.isCanceled()) {
 			throw new InterruptedException();
 		}
 		try {
 			StringBuilder result = new StringBuilder();
 			new XpandFacade(createContext(result)).evaluate(myTemplateFQN, extractTarget(arguments), extractArguments(arguments));
 			return result.toString();
 		} catch (EvaluationException ex) {
 			throw new InvocationTargetException(ex);
		} catch (AmbiguousDefinitionException e) {
			throw new InvocationTargetException(e);
 		}
 	}
 
 	protected Object extractTarget(Object[] arguments) {
 		assert arguments != null && arguments.length > 0;
 		return arguments[0];
 	}
 
 	protected Object[] extractArguments(Object[] arguments) {
 		assert arguments != null && arguments.length > 0;
 		ArrayList<Object> res = new ArrayList<Object>(arguments.length);
 		// strip first one off, assume it's target
 		for (int i = 1; i < arguments.length; i++) {
 			if (false == arguments[i] instanceof ImportAssistant) {
 				// strip assistant off
 				res.add(arguments[i]);
 			}
 		}
 		return res.toArray();
 	}
 
 	private ExecutionContext createContext(StringBuilder result) {
 		final BufferOutput output = new BufferOutput(result);
 		return ContextFactory.createXpandContext(myResourceManager, output, myGlobals, myContext);
 	}
 }
