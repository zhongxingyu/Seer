 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.validation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.dltk.ast.parser.IModuleDeclaration;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.SourceParserUtil;
 import org.eclipse.dltk.core.builder.IBuildContext;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.IValueReference;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.parser.JavaScriptParser;
 import org.eclipse.dltk.javascript.parser.Reporter;
 import org.eclipse.dltk.javascript.typeinfo.model.Element;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 
 public class JavaScriptValidations {
 
 	public static Script parse(IBuildContext context) {
 		final ISourceModule module = context.getSourceModule();
 		if (module != null) {
 			final String parserId = module.getScriptProject().getOption(
 					DLTKCore.PROJECT_SOURCE_PARSER_ID, false);
			if (parserId != null
					&& !JavaScriptParser.PARSER_ID.equals(parserId)) {
 				return null;
 			}
 		}
 		final IModuleDeclaration savedAST = (IModuleDeclaration) context
 				.get(IBuildContext.ATTR_MODULE_DECLARATION);
 		if (savedAST instanceof Script) {
 			return (Script) savedAST;
 		}
 		if (module != null) {
 			// TODO pass additional predicate here...
 			final IModuleDeclaration declaration = SourceParserUtil.parse(
 					module, context.getProblemReporter());
 			if (declaration instanceof Script) {
 				context.set(IBuildContext.ATTR_MODULE_DECLARATION, declaration);
 				return (Script) declaration;
 			}
 		}
 		final JavaScriptParser parser = new JavaScriptParser();
 		if (module == null) {
 			parser.setTypeInformationEnabled(true);
 		}
 		final Script script = parser.parse(context,
 				context.getProblemReporter());
 		context.set(IBuildContext.ATTR_MODULE_DECLARATION, script);
 		return script;
 	}
 
 	public static Type typeOf(IValueReference reference) {
 		if (reference != null) {
 			if (reference.getDeclaredType() != null) {
 				return reference.getDeclaredType();
 			}
			Set<Type> declaredTypes = reference.getDeclaredTypes();
			if (declaredTypes.size() == 1) {
				return declaredTypes.toArray(new Type[1])[0];
			}
 			final Set<Type> types = reference.getTypes();
 			if (types.size() == 1) {
 				return types.toArray(new Type[1])[0];
 			}
 		}
 		return null;
 	}
 
 	protected static Reporter createReporter(IBuildContext context) {
 		return new Reporter(context.getLineTracker(),
 				context.getProblemReporter());
 	}
 
 	/**
 	 * @param reference
 	 * @param elementType
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static <E extends Element> List<E> extractElements(
 			IValueReference reference, Class<E> elementType) {
 		final Object value = reference
 				.getAttribute(IReferenceAttributes.ELEMENT);
 		if (elementType.isInstance(value)) {
 			return Collections.singletonList((E) value);
 		} else if (value instanceof Element[]) {
 			final Element[] elements = (Element[]) value;
 			List<E> result = null;
 			for (Element element : elements) {
 				if (elementType.isInstance(element)) {
 					if (result == null) {
 						result = new ArrayList<E>(elements.length);
 					}
 					result.add((E) element);
 				}
 			}
 			return result;
 		}
 		return null;
 	}
 
 	/**
 	 * @param methods
 	 * @param arguments
 	 * @return
 	 */
 	public static Method selectMethod(List<Method> methods,
 			IValueReference[] arguments) {
 		if (methods.size() == 1) {
 			return methods.get(0);
 		}
 		Method argCountMatches = null;
 		for (Method method : methods) {
 			if (method.getParameters().size() == arguments.length) {
 				if (argCountMatches == null) {
 					argCountMatches = method;
 				} else {
 					argCountMatches = null;
 					break;
 				}
 			}
 		}
 		if (argCountMatches != null) {
 			return argCountMatches;
 		}
 		// TODO implement additional checks
 		return methods.get(0);
 	}
 
 }
