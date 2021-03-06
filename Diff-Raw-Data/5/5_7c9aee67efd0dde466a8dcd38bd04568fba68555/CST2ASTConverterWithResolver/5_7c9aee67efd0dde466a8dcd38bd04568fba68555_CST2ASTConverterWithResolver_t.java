 /*******************************************************************************
  * Copyright (c) 2008, 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.acceleo.internal.parser.ast;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.acceleo.common.IAcceleoConstants;
 import org.eclipse.acceleo.internal.parser.AcceleoParserMessages;
 import org.eclipse.acceleo.internal.parser.IAcceleoParserProblemsConstants;
 import org.eclipse.acceleo.internal.parser.cst.utils.ParserUtils;
 import org.eclipse.acceleo.model.mtl.MacroInvocation;
 import org.eclipse.acceleo.model.mtl.QueryInvocation;
 import org.eclipse.acceleo.model.mtl.TemplateInvocation;
 import org.eclipse.acceleo.model.mtl.VisibilityKind;
 import org.eclipse.acceleo.parser.cst.FileBlock;
 import org.eclipse.acceleo.parser.cst.ModuleExtendsValue;
 import org.eclipse.acceleo.parser.cst.ModuleImportsValue;
 import org.eclipse.acceleo.parser.cst.ProtectedAreaBlock;
 import org.eclipse.acceleo.parser.cst.Template;
 import org.eclipse.acceleo.parser.cst.TemplateOverridesValue;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.ocl.ecore.CollectionType;
 import org.eclipse.ocl.expressions.OCLExpression;
 
 /**
  * The main class used to transform a CST model to an AST model. This class is able to run the 'Resolve' step.
  * 
  * @author <a href="mailto:jonathan.musset@obeo.fr">Jonathan Musset</a>
  */
 public class CST2ASTConverterWithResolver extends CST2ASTConverter {
 
 	/** The message for depreciated elements. */
 	protected static final String DEPRECATED_MESSAGE = AcceleoParserMessages
 			.getString("CST2ASTConverterWithResolver.Deprecated"); //$NON-NLS-1$
 
 	/** Key of the error message for an unavailable clause. */
 	private static final String UNAVAILABLE_CLAUSE_KEY = "CST2ASTConverterWithResolver.UnavailableClause"; //$NON-NLS-1$
 
 	/**
 	 * The resolution step can be limited to the specified region, for increasing performances. This index is
 	 * the lower bound. Remark : The -1 value means everywhere.
 	 */
 	private int resolveBeginPosition = -1;
 
 	/**
 	 * The resolution step can be limited to the specified region, for increasing performances. This index is
 	 * the upper bound. Remark : The -1 value means everywhere.
 	 */
 	private int resolveEndPosition = -1;
 
 	/**
 	 * Constructor.
 	 */
 	public CST2ASTConverterWithResolver() {
 		super();
 	}
 
 	/**
 	 * Resolution step of the AST creation : OCL and invocations. This function must be called after
 	 * 'createAST'. The single argument is the root element of the CST model, because this method is a
 	 * component of the 'CST to AST' transformation.
 	 * 
 	 * @param rootCST
 	 *            is the root element of the input module (CST)
 	 */
 	public void resolveAST(org.eclipse.acceleo.parser.cst.Module rootCST) {
 		resolveAST(rootCST, -1, -1);
 	}
 
 	/**
 	 * Resolution step of the AST creation : OCL and invocations. This function must be called after
 	 * 'createAST'. The single argument is the root element of the CST model, because this method is a
 	 * component of the 'CST to AST' transformation. The resolution step can be limited to the specified
 	 * region, for increasing performances. Remark : The -1 value means everywhere.
 	 * 
 	 * @param rootCST
 	 *            is the root element of the input module (CST)
 	 * @param beginPosition
 	 *            is the lower bound
 	 * @param endPosition
 	 *            is the upper bound
 	 */
 	public synchronized void resolveAST(org.eclipse.acceleo.parser.cst.Module rootCST, int beginPosition,
 			int endPosition) {
 		if (rootCST != null) {
 			resolveBeginPosition = beginPosition;
 			resolveEndPosition = endPosition;
 			transformStepResolve(rootCST);
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.Module' of the
 	 * input model.
 	 * 
 	 * @param iModule
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.Module'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.Module iModule) {
 		org.eclipse.acceleo.model.mtl.Module oModule = factory.getOrCreateModule(iModule);
 		if (iModule != null && oModule != null && factory.getOCL() != null) {
 			Iterator<org.eclipse.acceleo.parser.cst.TypedModel> iInputIt = iModule.getInput().iterator();
 			while (iInputIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.TypedModel iNext = iInputIt.next();
 				transformStepResolveAddEPackage(iNext);
 
 			}
 			try {
 				Iterator<ModuleImportsValue> iImportsIt = iModule.getImports().iterator();
 				while (iImportsIt.hasNext()) {
 					ModuleImportsValue ioNext = iImportsIt.next();
 					org.eclipse.acceleo.model.mtl.Module oImportedModule = getModule(oModule.eResource(),
 							ioNext.getName());
 					if (oImportedModule == null) {
 						logProblem(AcceleoParserMessages.getString(
 								"CST2ASTConverterWithResolver.MissingModule", ioNext //$NON-NLS-1$
 										.getName()), ioNext.getStartPosition(), ioNext.getEndPosition());
 					} else {
 						oModule.getImports().add(oImportedModule);
 						if (oImportedModule.isDeprecated()) {
 							logWarning(DEPRECATED_MESSAGE, ioNext.getStartPosition(), ioNext.getEndPosition());
 						}
 					}
 				}
 				Iterator<ModuleExtendsValue> iExtendsIt = iModule.getExtends().iterator();
 				while (iExtendsIt.hasNext()) {
 					ModuleExtendsValue ioNext = iExtendsIt.next();
 					org.eclipse.acceleo.model.mtl.Module oExtendedModule = getModule(oModule.eResource(),
 							ioNext.getName());
 					if (oExtendedModule == null) {
 						logProblem(AcceleoParserMessages.getString(
 								"CST2ASTConverterWithResolver.MissingModule", ioNext //$NON-NLS-1$
 										.getName()), ioNext.getStartPosition(), ioNext.getEndPosition());
 					} else {
 						oModule.getExtends().add(oExtendedModule);
 						if (isRecursiveExtends(oModule, oExtendedModule)) {
 							logProblem(AcceleoParserMessages.getString(
 									"CST2ASTConverterWithResolver.RecursiveModuleExtends", new Object[] { //$NON-NLS-1$
 									oModule.getName(), oExtendedModule.getName(), }), ioNext
 									.getStartPosition(), ioNext.getEndPosition());
 						}
 						if (oExtendedModule.isDeprecated()) {
 							logWarning(DEPRECATED_MESSAGE, ioNext.getStartPosition(), ioNext.getEndPosition());
 						}
 					}
 				}
 				factory.getOCL().addRecursivelyBehavioralFeaturesToScope(oModule);
 				try {
 					transformStepResolveOwnedModuleElement(iModule);
 				} finally {
 					factory.getOCL().removeRecursivelyBehavioralFeaturesToScope(oModule);
 				}
 			} finally {
 				iInputIt = iModule.getInput().iterator();
 				while (iInputIt.hasNext()) {
 					org.eclipse.acceleo.parser.cst.TypedModel iNext = iInputIt.next();
 					transformStepResolveRemoveEPackage(iNext);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Check for recursive extends.
 	 * 
 	 * @param module
 	 *            the module
 	 * @param extendedModule
 	 *            the extended module
 	 * @return true if there is a recursive extend
 	 */
 	private boolean isRecursiveExtends(org.eclipse.acceleo.model.mtl.Module module,
 			org.eclipse.acceleo.model.mtl.Module extendedModule) {
 		boolean res = false;
 		if (module != null && extendedModule != null) {
 			if (module == extendedModule) {
 				res = true;
 			} else {
 				for (org.eclipse.acceleo.model.mtl.Module extended : extendedModule.getExtends()) {
 					if (isRecursiveExtends(module, extended)) {
 						res = true;
 						break;
 					}
 				}
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Gets the module that matches with the given qualified name, in the resource set.
 	 * 
 	 * @param oResource
 	 *            is the current resource
 	 * @param fullModuleName
 	 *            is the qualified name of the module to search ('org::eclipse::myGen')
 	 * @return an AST module (i.e the root element of another AST)
 	 */
 	private org.eclipse.acceleo.model.mtl.Module getModule(Resource oResource, String fullModuleName) {
 		if (oResource != null && fullModuleName != null) {
 			Iterator<Resource> itOtherResources = oResource.getResourceSet().getResources().iterator();
 			while (itOtherResources.hasNext()) {
 				Resource otherResource = itOtherResources.next();
 				if (otherResource.getContents().size() > 0
 						&& otherResource.getContents().get(0) instanceof org.eclipse.acceleo.model.mtl.Module) {
 					org.eclipse.acceleo.model.mtl.Module otherModule = (org.eclipse.acceleo.model.mtl.Module)otherResource
 							.getContents().get(0);
 					if (fullModuleName.equals(otherModule.getNsURI())
 							|| fullModuleName.equals(otherModule.getName())) {
 						return otherModule;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the current AST module for the given object of the AST model.
 	 * 
 	 * @param eObject
 	 *            is an object of the AST model
 	 * @return the current module
 	 */
 	private org.eclipse.acceleo.model.mtl.Module getModule(EObject eObject) {
 		EObject current = eObject;
 		while (current != null) {
 			if (current instanceof org.eclipse.acceleo.model.mtl.Module) {
 				return (org.eclipse.acceleo.model.mtl.Module)current;
 			}
 			current = current.eContainer();
 		}
 		return null;
 	}
 
 	/**
 	 * To register the packages in the list of the metamodels considered during the OCL parsing. The step
 	 * 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.TypedModel' of the input
 	 * model.
 	 * 
 	 * @param iTypedModel
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.TypedModel'
 	 */
 	private void transformStepResolveAddEPackage(org.eclipse.acceleo.parser.cst.TypedModel iTypedModel) {
 		org.eclipse.acceleo.model.mtl.TypedModel oTypedModel = factory.getOrCreateTypedModel(iTypedModel);
 		if (oTypedModel != null) {
 			Iterator<EPackage> oTakesTypesFromIt = oTypedModel.getTakesTypesFrom().iterator();
 			while (oTakesTypesFromIt.hasNext()) {
 				EPackage oNext = oTakesTypesFromIt.next();
 				factory.getOCL().addMetamodel(oNext);
 			}
 		}
 	}
 
 	/**
 	 * To remove the packages in the list of the metamodels considered during the OCL parsing. The step
 	 * 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.TypedModel' of the input
 	 * model.
 	 * 
 	 * @param iTypedModel
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.TypedModel'
 	 */
 	private void transformStepResolveRemoveEPackage(org.eclipse.acceleo.parser.cst.TypedModel iTypedModel) {
 		org.eclipse.acceleo.model.mtl.TypedModel oTypedModel = factory.getOrCreateTypedModel(iTypedModel);
 		if (oTypedModel != null) {
 			Iterator<EPackage> oTakesTypesFromIt = oTypedModel.getTakesTypesFrom().iterator();
 			while (oTakesTypesFromIt.hasNext()) {
 				EPackage oNext = oTakesTypesFromIt.next();
 				factory.getOCL().removeMetamodel(oNext);
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.Template' of the
 	 * input model.
 	 * 
 	 * @param iTemplate
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.Template'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.Template iTemplate) {
 		org.eclipse.acceleo.model.mtl.Template oTemplate = factory.getOrCreateTemplate(iTemplate);
 		if (iTemplate != null && oTemplate != null) {
 			List<EClassifier> paramTypes = new ArrayList<EClassifier>();
 			Iterator<org.eclipse.acceleo.parser.cst.Variable> iParameterIt = iTemplate.getParameter()
 					.iterator();
 			org.eclipse.ocl.ecore.Variable oFirst = null;
 			while (iParameterIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.Variable iNext = iParameterIt.next();
 				if (iNext != null) {
 					org.eclipse.ocl.ecore.Variable oNext = factory.getOrCreateVariable(iNext);
 					if (oFirst == null) {
 						oFirst = oNext;
 					}
 					transformStepResolveAddVariable(iNext);
 					paramTypes.add(oNext.getType());
 				}
 			}
 			Iterator<TemplateOverridesValue> iOverridesIt = iTemplate.getOverrides().iterator();
 			while (iOverridesIt.hasNext()) {
 				TemplateOverridesValue ioNext = iOverridesIt.next();
 				List<org.eclipse.acceleo.model.mtl.Template> oOverrides = getExtendedTemplatesNamed(
 						getModule(oTemplate), ioNext.getName(), paramTypes);
 				if (oOverrides.size() == 0) {
 					logProblem(AcceleoParserMessages.getString(
 							"CST2ASTConverterWithResolver.MissingTemplate", //$NON-NLS-1$
 							ioNext.getName()), ioNext.getStartPosition(), ioNext.getEndPosition());
 				} else {
 					for (org.eclipse.acceleo.model.mtl.Template template : oOverrides) {
 						if (template.isDeprecated()) {
 							logWarning(DEPRECATED_MESSAGE, ioNext.getStartPosition(), ioNext.getEndPosition());
 							break;
 						}
 					}
 					oTemplate.getOverrides().addAll(oOverrides);
 				}
 			}
 
 			if (oFirst != null && oFirst.getType() != null) {
 				factory.getOCL().pushContext(oFirst.getType());
 			}
 			try {
 				org.eclipse.acceleo.parser.cst.ModelExpression iGuard = iTemplate.getGuard();
 				org.eclipse.ocl.ecore.OCLExpression oGuard = factory.getOrCreateOCLExpression(iGuard);
 				if (oGuard != null) {
 					oTemplate.setGuard(oGuard);
 				}
 				transformStepResolve(iGuard);
 
 				factory.getOCL().pushContext(getOCL().getStringType());
 				try {
 					org.eclipse.acceleo.parser.cst.ModelExpression iPost = iTemplate.getPost();
 					org.eclipse.ocl.ecore.OCLExpression oPost = factory.getOrCreateOCLExpression(iPost);
 					if (oPost != null) {
 						oTemplate.setPost(oPost);
 					}
 					transformStepResolve(iPost);
 				} finally {
 					factory.getOCL().popContext();
 				}
 
 				org.eclipse.acceleo.parser.cst.InitSection iInit = iTemplate.getInit();
 				transformStepResolveAddVariables(iInit);
 
 				transformStepResolveBody(iTemplate);
 
 				transformStepResolveRemoveVariables(iInit);
 
 				iParameterIt = iTemplate.getParameter().iterator();
 				while (iParameterIt.hasNext()) {
 					org.eclipse.acceleo.parser.cst.Variable iNext = iParameterIt.next();
 					transformStepResolveRemoveVariable(iNext);
 
 				}
 
 			} finally {
 				if (oFirst != null && oFirst.getType() != null) {
 					factory.getOCL().popContext();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the templates of the extended module that matches with the given name. It browses the extended
 	 * modules of the given module...
 	 * 
 	 * @param oModule
 	 *            is the current module
 	 * @param name
 	 *            is the name of the template to search
 	 * @param paramTypes
 	 *            are the types of the parameters, the candidate must have the same parameters
 	 * @return the templates, it is more often one template
 	 */
 	private List<org.eclipse.acceleo.model.mtl.Template> getExtendedTemplatesNamed(
 			org.eclipse.acceleo.model.mtl.Module oModule, String name, List<EClassifier> paramTypes) {
 		List<org.eclipse.acceleo.model.mtl.Template> result = new ArrayList<org.eclipse.acceleo.model.mtl.Template>();
 		if (oModule != null) {
 			result.addAll(getProtectedTemplatesNamed(oModule, name, paramTypes));
 			List<org.eclipse.acceleo.model.mtl.Module> allExtends = new ArrayList<org.eclipse.acceleo.model.mtl.Module>();
 			computeAllExtends(allExtends, oModule);
 			Iterator<org.eclipse.acceleo.model.mtl.Module> itOtherModules = allExtends.iterator();
 			while (itOtherModules.hasNext()) {
 				org.eclipse.acceleo.model.mtl.Module oOtherModule = itOtherModules.next();
 				result.addAll(getProtectedTemplatesNamed(oOtherModule, name, paramTypes));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Gets the templates that matches with the given name. It browses the given module and its children.
 	 * 
 	 * @param oModule
 	 *            is the current module
 	 * @param name
 	 *            is the name of the template to search
 	 * @param paramTypes
 	 *            are the types of the parameters, the candidate must have the same parameters
 	 * @return the templates, it is more often one template
 	 */
 	private List<org.eclipse.acceleo.model.mtl.Template> getProtectedTemplatesNamed(
 			org.eclipse.acceleo.model.mtl.Module oModule, String name, List<EClassifier> paramTypes) {
 		List<org.eclipse.acceleo.model.mtl.Template> result = new ArrayList<org.eclipse.acceleo.model.mtl.Template>();
 		Iterator<EObject> itObjects = oModule.eAllContents();
 		while (itObjects.hasNext()) {
 			EObject eObject = itObjects.next();
 			if (eObject instanceof org.eclipse.acceleo.model.mtl.Template) {
 				org.eclipse.acceleo.model.mtl.Template otherTemplate = (org.eclipse.acceleo.model.mtl.Template)eObject;
 				if (checksName(name, otherTemplate.getName(), oModule.getNsURI())
 						&& paramTypes.size() == otherTemplate.getParameter().size()
 						&& otherTemplate.getVisibility().getValue() >= VisibilityKind.PROTECTED_VALUE) {
 					boolean parameterMatches = true;
 					Iterator<EClassifier> itTypes = paramTypes.iterator();
 					Iterator<org.eclipse.ocl.ecore.Variable> itParameters = otherTemplate.getParameter()
 							.iterator();
 					while (parameterMatches && itParameters.hasNext()) {
 						EClassifier eParameterType = itParameters.next().getType();
 						EClassifier type = itTypes.next();
 						// Checks if 'type' is a sub-class of 'eParameterType' EMF-wise
 						if (eParameterType instanceof EClass && type instanceof EClass) {
 							parameterMatches = eParameterType == type
 									|| isSubTypeOf((EClass)eParameterType, (EClass)type);
 						} else {
 							parameterMatches = eParameterType == type
 									|| (eParameterType != null && eParameterType.equals(type));
 						}
 					}
 					if (parameterMatches) {
 						result.add(otherTemplate);
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Checks the name of the current overridden template with the name of the other template and with the
 	 * qualified version of the other template name.
 	 * 
 	 * @param name
 	 *            The name of the current overridden template
 	 * @param otherTemplateName
 	 *            The other template name
 	 * @param moduleNameSpace
 	 *            The NSURI of the module of the other template
 	 * @return true if the two names are equals or if the name esquals the qualified name of the other
 	 *         template
 	 */
 	private boolean checksName(String name, String otherTemplateName, String moduleNameSpace) {
 		boolean result = name.equals(otherTemplateName);
 		// If the result is false, we will check the qualified name of the other template
 		if (!result && moduleNameSpace != null
 				&& moduleNameSpace.contains(IAcceleoConstants.NAMESPACE_SEPARATOR)) {
 			String namespace = moduleNameSpace.substring(0, moduleNameSpace
 					.lastIndexOf(IAcceleoConstants.NAMESPACE_SEPARATOR));
 			String otherTemplateQualifiedName = namespace + IAcceleoConstants.NAMESPACE_SEPARATOR
 					+ otherTemplateName;
 			result = name.equals(otherTemplateQualifiedName);
 		}
 		return result;
 	}
 
 	/**
 	 * It creates the list of all inherited modules.
 	 * 
 	 * @param allExtends
 	 *            the list to compute
 	 * @param oModule
 	 *            is the current module
 	 */
 	private void computeAllExtends(List<org.eclipse.acceleo.model.mtl.Module> allExtends,
 			org.eclipse.acceleo.model.mtl.Module oModule) {
 		List<org.eclipse.acceleo.model.mtl.Module> toBrowse = new ArrayList<org.eclipse.acceleo.model.mtl.Module>();
 		Iterator<org.eclipse.acceleo.model.mtl.Module> itOtherModules = oModule.getExtends().iterator();
 		while (itOtherModules.hasNext()) {
 			org.eclipse.acceleo.model.mtl.Module oOtherModule = itOtherModules.next();
 			if (!allExtends.contains(oOtherModule)) {
 				allExtends.add(oOtherModule);
 				toBrowse.add(oOtherModule);
 			}
 		}
 		itOtherModules = toBrowse.iterator();
 		while (itOtherModules.hasNext()) {
 			org.eclipse.acceleo.model.mtl.Module oOtherModule = itOtherModules.next();
 			computeAllExtends(allExtends, oOtherModule);
 		}
 	}
 
 	/**
 	 * To register the variable in the list of the variables considered during the OCL parsing. The step
 	 * 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.Variable' of the input
 	 * model.
 	 * 
 	 * @param iVariable
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.Variable'
 	 */
 	private void transformStepResolveAddVariable(org.eclipse.acceleo.parser.cst.Variable iVariable) {
 		org.eclipse.ocl.ecore.Variable oVariable = factory.getOrCreateVariable(iVariable);
 		if (iVariable != null && oVariable != null) {
 			org.eclipse.acceleo.parser.cst.ModelExpression iInitExpression = iVariable.getInitExpression();
 			org.eclipse.ocl.ecore.OCLExpression oInitExpression = factory
 					.getOrCreateOCLExpression(iInitExpression);
 			if (oInitExpression != null) {
 				oVariable.setInitExpression(oInitExpression);
 			}
 			transformStepResolve(iInitExpression);
 			factory.getOCL().addVariableToScope(oVariable);
 			if (!ParserUtils.isIdentifier(iVariable.getName())) {
 				logProblem(IAcceleoParserProblemsConstants.SYNTAX_NAME_NOT_VALID + iVariable.getName(),
 						iVariable.getStartPosition(), iVariable.getEndPosition());
 			}
 			if (oVariable.getType() == null || oVariable.getType() == factory.getOCL().getInvalidType()) {
 				EClassifier eClassifier = factory.getOCL().lookupClassifier(iVariable.getType());
 				if (eClassifier != null) {
 					oVariable.setType(eClassifier);
 				} else {
 					logProblem(IAcceleoParserProblemsConstants.SYNTAX_TYPE_NOT_VALID + iVariable.getType(),
 							iVariable.getStartPosition(), iVariable.getEndPosition());
 				}
 			}
 		}
 	}
 
 	/**
 	 * To remove the variable in the list of the variables considered during the OCL parsing. The step
 	 * 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.Variable' of the input
 	 * model.
 	 * 
 	 * @param iVariable
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.Variable'
 	 */
 	private void transformStepResolveRemoveVariable(org.eclipse.acceleo.parser.cst.Variable iVariable) {
 		org.eclipse.ocl.ecore.Variable oVariable = factory.getOrCreateVariable(iVariable);
 		if (iVariable != null && oVariable != null) {
 			factory.getOCL().removeVariableFromScope(oVariable);
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.ModelExpression'
 	 * of the input model.
 	 * 
 	 * @param iModelExpression
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.ModelExpression'
 	 */
 	private synchronized void transformStepResolve(
 			org.eclipse.acceleo.parser.cst.ModelExpression iModelExpression) {
 		if (!isValidRegion(iModelExpression)) {
 			return;
 		}
 		org.eclipse.ocl.ecore.OCLExpression oOCLExpression = factory
 				.getOrCreateOCLExpression(iModelExpression);
 		if (iModelExpression != null && oOCLExpression != null) {
 			if (oOCLExpression.eContainer() == null) {
 				org.eclipse.acceleo.model.mtl.TemplateExpression oTemplateExpression = factory
 						.getTemporaryTemplateExpression(iModelExpression);
 				if (oTemplateExpression != null) {
 					EcoreUtil.replace(oTemplateExpression, oOCLExpression);
 				}
 			}
 			if (oOCLExpression instanceof TemplateInvocation) {
 				TemplateInvocation oTemplateInvocation = (TemplateInvocation)oOCLExpression;
 				transformStepResolveSuperTemplateInvocation(iModelExpression, oTemplateInvocation);
 			}
 			org.eclipse.acceleo.parser.cst.ModelExpression iBefore = iModelExpression.getBefore();
 			if (iBefore != null) {
 				if (oOCLExpression instanceof TemplateInvocation) {
 					org.eclipse.ocl.ecore.OCLExpression oBefore = factory.getOrCreateOCLExpression(iBefore);
 					((TemplateInvocation)oOCLExpression).setBefore(oBefore);
 					transformStepResolve(iBefore);
 				} else {
 					logProblem(AcceleoParserMessages.getString(UNAVAILABLE_CLAUSE_KEY,
 							IAcceleoConstants.BEFORE), iBefore.getStartPosition(), iBefore.getEndPosition());
 				}
 			}
 			org.eclipse.acceleo.parser.cst.ModelExpression iEach = iModelExpression.getEach();
 			if (iEach != null) {
 				if (oOCLExpression instanceof TemplateInvocation) {
 					org.eclipse.ocl.ecore.OCLExpression oEach = factory.getOrCreateOCLExpression(iEach);
 					((TemplateInvocation)oOCLExpression).setEach(oEach);
 					transformStepResolve(iEach);
 				} else {
 					logProblem(AcceleoParserMessages.getString(UNAVAILABLE_CLAUSE_KEY,
 							IAcceleoConstants.SEPARATOR), iEach.getStartPosition(), iEach.getEndPosition());
 				}
 			}
 			org.eclipse.acceleo.parser.cst.ModelExpression iAfter = iModelExpression.getAfter();
 			if (iAfter != null) {
 				if (oOCLExpression instanceof TemplateInvocation) {
 					org.eclipse.ocl.ecore.OCLExpression oAfter = factory.getOrCreateOCLExpression(iAfter);
 					((TemplateInvocation)oOCLExpression).setAfter(oAfter);
 					transformStepResolve(iAfter);
 				} else {
 					logProblem(AcceleoParserMessages.getString(UNAVAILABLE_CLAUSE_KEY,
 							IAcceleoConstants.AFTER), iAfter.getStartPosition(), iAfter.getEndPosition());
 				}
 			}
 
 			if (oOCLExpression instanceof TemplateInvocation) {
 				TemplateInvocation templateInvocation = (TemplateInvocation)oOCLExpression;
 				if (templateInvocation.getDefinition() != null
 						&& templateInvocation.getDefinition().isDeprecated()) {
 					logWarning(DEPRECATED_MESSAGE, templateInvocation.getStartPosition(), templateInvocation
 							.getEndPosition());
 				}
 			} else if (oOCLExpression instanceof QueryInvocation) {
 				QueryInvocation queryInvocation = (QueryInvocation)oOCLExpression;
 				if (queryInvocation.getDefinition() != null && queryInvocation.getDefinition().isDeprecated()) {
 					logWarning(DEPRECATED_MESSAGE, queryInvocation.getStartPosition(), queryInvocation
 							.getEndPosition());
 				}
 			} else if (oOCLExpression instanceof MacroInvocation) {
 				MacroInvocation macroInvocation = (MacroInvocation)oOCLExpression;
 				if (macroInvocation.getDefinition() != null && macroInvocation.getDefinition().isDeprecated()) {
 					logWarning(DEPRECATED_MESSAGE, macroInvocation.getStartPosition(), macroInvocation
 							.getEndPosition());
 				}
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'super' clause (template invocation) of the input
 	 * model.
 	 * 
 	 * @param iModelExpression
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.ModelExpression'
 	 * @param oTemplateInvocation
 	 *            is the output template invocation
 	 */
 	private void transformStepResolveSuperTemplateInvocation(
 			org.eclipse.acceleo.parser.cst.ModelExpression iModelExpression,
 			TemplateInvocation oTemplateInvocation) {
 		if (oTemplateInvocation.isSuper() && oTemplateInvocation.getDefinition() == null) {
 			org.eclipse.acceleo.model.mtl.Template oTemplate = getTemplate(oTemplateInvocation);
 			if (oTemplate != null && oTemplate.getOverrides().size() == 0) {
 				logProblem(AcceleoParserMessages.getString("CST2ASTConverterWithResolver.InvalidClause", //$NON-NLS-1$
 						IAcceleoConstants.SUPER, IAcceleoConstants.OVERRIDES), iModelExpression
 						.getStartPosition(), iModelExpression.getEndPosition());
 			} else {
 				oTemplateInvocation.setDefinition(oTemplate);
 			}
 		}
 	}
 
 	/**
 	 * Gets the current AST template for the given object of the AST model.
 	 * 
 	 * @param eObject
 	 *            is an object of the AST model
 	 * @return the current template, or null if it doesn't exist
 	 */
 	private org.eclipse.acceleo.model.mtl.Template getTemplate(EObject eObject) {
 		EObject current = eObject;
 		while (current != null) {
 			if (current instanceof org.eclipse.acceleo.model.mtl.Template) {
 				return (org.eclipse.acceleo.model.mtl.Template)current;
 			}
 			current = current.eContainer();
 		}
 		return null;
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.TextExpression'
 	 * of the input model.
 	 * 
 	 * @param iTextExpression
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.TextExpression'
 	 */
 	@SuppressWarnings("unused")
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.TextExpression iTextExpression) {
 		// Nothing to do here
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.Block' of the
 	 * input model.
 	 * 
 	 * @param iBlock
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.Block'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.Block iBlock) {
 		org.eclipse.acceleo.model.mtl.Block oBlock = factory.getOrCreateBlock(iBlock);
 		if (iBlock != null && oBlock != null) {
 			org.eclipse.acceleo.parser.cst.InitSection iInit = iBlock.getInit();
 			transformStepResolveAddVariables(iInit);
 
 			transformStepResolveBody(iBlock);
 
 			transformStepResolveRemoveVariables(iInit);
 		}
 	}
 
 	/**
 	 * To register the variables of the init section in the list of the variables considered during the OCL
 	 * parsing. The step 'StepResolve' of the transformation for each
 	 * 'org.eclipse.acceleo.parser.cst.InitSection' of the input model.
 	 * 
 	 * @param iInitSection
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.InitSection'
 	 */
 	private void transformStepResolveAddVariables(org.eclipse.acceleo.parser.cst.InitSection iInitSection) {
 		org.eclipse.acceleo.model.mtl.InitSection oInitSection = factory.getOrCreateInitSection(iInitSection);
 		if (iInitSection != null && oInitSection != null) {
 			Iterator<org.eclipse.acceleo.parser.cst.Variable> iVariableIt = iInitSection.getVariable()
 					.iterator();
 			while (iVariableIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.Variable iNext = iVariableIt.next();
 				transformStepResolveAddVariable(iNext);
 
 			}
 		}
 	}
 
 	/**
 	 * To remove the variables in the list of the variables considered during the OCL parsing. The step
 	 * 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.InitSection' of the input
 	 * model.
 	 * 
 	 * @param iInitSection
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.InitSection'
 	 */
 	private void transformStepResolveRemoveVariables(org.eclipse.acceleo.parser.cst.InitSection iInitSection) {
 		org.eclipse.acceleo.model.mtl.InitSection oInitSection = factory.getOrCreateInitSection(iInitSection);
 		if (iInitSection != null && oInitSection != null) {
 			Iterator<org.eclipse.acceleo.parser.cst.Variable> iVariableIt = iInitSection.getVariable()
 					.iterator();
 			while (iVariableIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.Variable iNext = iVariableIt.next();
 				transformStepResolveRemoveVariable(iNext);
 
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each
 	 * 'org.eclipse.acceleo.parser.cst.ProtectedAreaBlock' of the input model.
 	 * 
 	 * @param iProtectedAreaBlock
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.ProtectedAreaBlock'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.ProtectedAreaBlock iProtectedAreaBlock) {
 		org.eclipse.acceleo.model.mtl.ProtectedAreaBlock oProtectedAreaBlock = factory
 				.getOrCreateProtectedAreaBlock(iProtectedAreaBlock);
 		if (iProtectedAreaBlock != null && oProtectedAreaBlock != null) {
 
 			// checks if the identifier of the protected area is valid
 			if (iProtectedAreaBlock.getMarker() != null && iProtectedAreaBlock.getMarker().getBody() != null) {
 				String body = iProtectedAreaBlock.getMarker().getBody();
 				if ("".equals(body) || "''".equals(body)) { //$NON-NLS-1$ //$NON-NLS-2$
 					this.logProblem(AcceleoParserMessages
 							.getString("CST2ASTConverterWithResolver.ProtectedAreaMissingIdentifier"), //$NON-NLS-1$
 							iProtectedAreaBlock.getMarker().getStartPosition(), iProtectedAreaBlock
 									.getMarker().getEndPosition());
 				}
 			}
 
 			// checks conflicts with the name of another protected area block
 			this.checkNameConflict(iProtectedAreaBlock);
 
 			org.eclipse.acceleo.parser.cst.ModelExpression iMarker = iProtectedAreaBlock.getMarker();
 			org.eclipse.ocl.ecore.OCLExpression oMarker = factory.getOrCreateOCLExpression(iMarker);
 			if (oMarker != null) {
 				oProtectedAreaBlock.setMarker(oMarker);
 			}
 			transformStepResolve(iMarker);
 
 			org.eclipse.acceleo.parser.cst.InitSection iInit = iProtectedAreaBlock.getInit();
 			transformStepResolveAddVariables(iInit);
 
 			transformStepResolveBody(iProtectedAreaBlock);
 
 			transformStepResolveRemoveVariables(iInit);
 		}
 	}
 
 	/**
 	 * This method will determine if there is a conflict between the name of the protected block and the name
 	 * of another protected block within the module.
 	 * 
 	 * @param iProtectedArea
 	 *            the protected area.
 	 */
 	private void checkNameConflict(final org.eclipse.acceleo.parser.cst.ProtectedAreaBlock iProtectedArea) {
 		if (iProtectedArea.getMarker() == null) {
 			return;
 		}
 		final String protectedAreaName = iProtectedArea.getMarker().getBody();
 
 		// we find the scope of the current block.
 		EObject scopeContainer = this.getScopeContainer(iProtectedArea);
 
 		// we find all the protected areas in the module.
 		List<org.eclipse.acceleo.parser.cst.ProtectedAreaBlock> protectedAreas = new ArrayList<org.eclipse.acceleo.parser.cst.ProtectedAreaBlock>();
 		TreeIterator<EObject> children = scopeContainer.eAllContents();
 		while (children.hasNext()) {
 			EObject child = children.next();
 			if (child instanceof org.eclipse.acceleo.parser.cst.ProtectedAreaBlock) {
 				org.eclipse.acceleo.parser.cst.ProtectedAreaBlock protectedAreaBlock = (org.eclipse.acceleo.parser.cst.ProtectedAreaBlock)child;
 				if (this.getScopeContainer(protectedAreaBlock).equals(scopeContainer)) {
 					protectedAreas.add(protectedAreaBlock);
 				}
 			}
 		}
 
 		// we find all the conflicts.
 		List<org.eclipse.acceleo.parser.cst.ProtectedAreaBlock> conflictList = new ArrayList<org.eclipse.acceleo.parser.cst.ProtectedAreaBlock>();
 		for (org.eclipse.acceleo.parser.cst.ProtectedAreaBlock protectedAreaBlock : protectedAreas) {
 			if (!protectedAreaBlock.equals(iProtectedArea) && protectedAreaBlock.getMarker() != null
 					&& protectedAreaBlock.getMarker().getBody().equals(protectedAreaName)) {
 				conflictList.add(protectedAreaBlock);
 			}
 		}
 
		// for all conflict we log a warning.
 		if (!conflictList.isEmpty()) {
 			for (ProtectedAreaBlock protectedAreaBlock : conflictList) {
				this.logWarning(AcceleoParserMessages
 						.getString("CST2ASTConverterWithResolver.ProtectedAreaConflict"), protectedAreaBlock //$NON-NLS-1$
 						.getMarker().getStartPosition(), protectedAreaBlock.getMarker().getEndPosition());
 			}
 		}
 	}
 
 	/**
 	 * Returns the container of the scope of the protected area.
 	 * 
 	 * @param iProtectedArea
 	 *            the protected area
 	 * @return The container of the scope of the protected area
 	 */
 	private EObject getScopeContainer(final org.eclipse.acceleo.parser.cst.ProtectedAreaBlock iProtectedArea) {
 		EObject container = iProtectedArea.eContainer();
 		while (!(container instanceof Template) && !(container instanceof FileBlock)) {
 			container = container.eContainer();
 		}
 		return container;
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.ForBlock' of the
 	 * input model.
 	 * 
 	 * @param iForBlock
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.ForBlock'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.ForBlock iForBlock) {
 		org.eclipse.acceleo.model.mtl.ForBlock oForBlock = factory.getOrCreateForBlock(iForBlock);
 		if (iForBlock != null && oForBlock != null) {
 			org.eclipse.acceleo.parser.cst.ModelExpression iIterSet = iForBlock.getIterSet();
 			org.eclipse.ocl.ecore.OCLExpression oIterSet = factory.getOrCreateOCLExpression(iIterSet);
 			if (oIterSet != null) {
 				oForBlock.setIterSet(oIterSet);
 			}
 			transformStepResolve(iIterSet);
 
 			org.eclipse.acceleo.parser.cst.ModelExpression iBefore = iForBlock.getBefore();
 			org.eclipse.ocl.ecore.OCLExpression oBefore = factory.getOrCreateOCLExpression(iBefore);
 			if (oBefore != null) {
 				oForBlock.setBefore(oBefore);
 			}
 			transformStepResolve(iBefore);
 
 			org.eclipse.acceleo.parser.cst.ModelExpression iEach = iForBlock.getEach();
 			org.eclipse.ocl.ecore.OCLExpression oEach = factory.getOrCreateOCLExpression(iEach);
 			if (oEach != null) {
 				oForBlock.setEach(oEach);
 			}
 			transformStepResolve(iEach);
 
 			org.eclipse.acceleo.parser.cst.ModelExpression iAfter = iForBlock.getAfter();
 			org.eclipse.ocl.ecore.OCLExpression oAfter = factory.getOrCreateOCLExpression(iAfter);
 			if (oAfter != null) {
 				oForBlock.setAfter(oAfter);
 			}
 			transformStepResolve(iAfter);
 
 			org.eclipse.acceleo.parser.cst.Variable iLoopVariable = iForBlock.getLoopVariable();
 			org.eclipse.ocl.ecore.Variable oLoopVariable;
 			if (iLoopVariable != null) {
 				oLoopVariable = factory.getOrCreateVariable(iLoopVariable);
 			} else {
 				oLoopVariable = null;
 			}
 			EClassifier context = null;
 			if (iLoopVariable != null && oLoopVariable != null) {
 				transformStepResolveAddVariable(iLoopVariable);
 				context = oLoopVariable.getType();
 			} else if (oIterSet != null) {
 				context = oIterSet.getEType();
 				if (context instanceof CollectionType) {
 					context = ((CollectionType)context).getElementType();
 				}
 			}
 			if (context != null) {
 				factory.getOCL().pushContext(context);
 			}
 			// Implicit "i" variable for the iteration count
 			org.eclipse.ocl.ecore.Variable iterationCount = org.eclipse.ocl.ecore.EcoreFactory.eINSTANCE
 					.createVariable();
 			iterationCount.setName("i"); //$NON-NLS-1$
 			iterationCount.setType(factory.getOCL().getIntegerType());
 			factory.getOCL().addVariableToScope(iterationCount);
 			try {
 				org.eclipse.acceleo.parser.cst.ModelExpression iGuard = iForBlock.getGuard();
 				org.eclipse.ocl.ecore.OCLExpression oGuard = factory.getOrCreateOCLExpression(iGuard);
 				if (oGuard != null) {
 					oForBlock.setGuard(oGuard);
 				}
 				transformStepResolve(iGuard);
 
 				org.eclipse.acceleo.parser.cst.InitSection iInit = iForBlock.getInit();
 				transformStepResolveAddVariables(iInit);
 
 				transformStepResolveBody(iForBlock);
 
 				transformStepResolveRemoveVariables(iInit);
 			} finally {
 				if (iLoopVariable != null && oLoopVariable != null) {
 					transformStepResolveRemoveVariable(iLoopVariable);
 				}
 				if (context != null) {
 					factory.getOCL().popContext();
 				}
 				factory.getOCL().removeVariableFromScope(iterationCount);
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.IfBlock' of the
 	 * input model.
 	 * 
 	 * @param iIfBlock
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.IfBlock'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.IfBlock iIfBlock) {
 		org.eclipse.acceleo.model.mtl.IfBlock oIfBlock = factory.getOrCreateIfBlock(iIfBlock);
 		if (iIfBlock != null && oIfBlock != null) {
 			org.eclipse.acceleo.parser.cst.ModelExpression iIfExpr = iIfBlock.getIfExpr();
 			org.eclipse.ocl.ecore.OCLExpression oIfExpr = factory.getOrCreateOCLExpression(iIfExpr);
 			if (oIfExpr != null) {
 				oIfBlock.setIfExpr(oIfExpr);
 				if (oIfExpr.getType() != null
 						&& !(oIfExpr.getType() == getOCL().getOCLEnvironment().getOCLStandardLibrary()
 								.getBoolean())) {
 					logProblem(AcceleoParserMessages.getString(
 							"IAcceleoParserProblemsConstants.InvalidExprType", //$NON-NLS-1$
 							oIfExpr.getType().getName()), oIfExpr.getStartPosition(), oIfExpr
 							.getEndPosition());
 				}
 			}
 			transformStepResolve(iIfExpr);
 
 			org.eclipse.acceleo.parser.cst.InitSection iInit = iIfBlock.getInit();
 			transformStepResolveAddVariables(iInit);
 
 			transformStepResolveBody(iIfBlock);
 
 			transformStepResolveRemoveVariables(iInit);
 
 			Iterator<org.eclipse.acceleo.parser.cst.IfBlock> iElseIfIt = iIfBlock.getElseIf().iterator();
 			while (iElseIfIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.IfBlock iNext = iElseIfIt.next();
 				transformStepResolve(iNext);
 
 			}
 
 			org.eclipse.acceleo.parser.cst.Block iElse = iIfBlock.getElse();
 			if (iElse instanceof org.eclipse.acceleo.parser.cst.Template) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.Template)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.ProtectedAreaBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.ProtectedAreaBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.ForBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.ForBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.IfBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.IfBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.LetBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.LetBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.FileBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.FileBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.TraceBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.TraceBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.Macro) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.Macro)iElse);
 			} else {
 				transformStepResolve(iElse);
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.LetBlock' of the
 	 * input model.
 	 * 
 	 * @param iLetBlock
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.LetBlock'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.LetBlock iLetBlock) {
 		org.eclipse.acceleo.model.mtl.LetBlock oLetBlock = factory.getOrCreateLetBlock(iLetBlock);
 		if (iLetBlock != null && oLetBlock != null) {
 			org.eclipse.acceleo.parser.cst.Variable iLetVariable = iLetBlock.getLetVariable();
 			org.eclipse.ocl.ecore.Variable oLetVariable;
 			if (iLetVariable != null) {
 				oLetVariable = factory.getOrCreateVariable(iLetVariable);
 			} else {
 				oLetVariable = null;
 			}
 			OCLExpression<EClassifier> saveInitExpression = null;
 			if (iLetVariable != null && oLetVariable != null) {
 				transformStepResolveAddVariable(iLetVariable);
 				saveInitExpression = oLetVariable.getInitExpression();
 				if (saveInitExpression != null) {
 					oLetVariable.setInitExpression(null);
 				}
 				factory.getOCL().pushContext(oLetVariable.getType());
 			}
 			try {
 				org.eclipse.acceleo.parser.cst.InitSection iInit = iLetBlock.getInit();
 				transformStepResolveAddVariables(iInit);
 
 				transformStepResolveBody(iLetBlock);
 
 				transformStepResolveRemoveVariables(iInit);
 			} finally {
 				if (iLetVariable != null && oLetVariable != null) {
 					transformStepResolveRemoveVariable(iLetVariable);
 					if (saveInitExpression != null) {
 						oLetVariable.setInitExpression(saveInitExpression);
 					}
 					factory.getOCL().popContext();
 				}
 			}
 
 			org.eclipse.acceleo.parser.cst.Block iElse = iLetBlock.getElse();
 			if (iElse instanceof org.eclipse.acceleo.parser.cst.Template) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.Template)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.ProtectedAreaBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.ProtectedAreaBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.ForBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.ForBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.IfBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.IfBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.LetBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.LetBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.FileBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.FileBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.TraceBlock) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.TraceBlock)iElse);
 			} else if (iElse instanceof org.eclipse.acceleo.parser.cst.Macro) {
 				transformStepResolve((org.eclipse.acceleo.parser.cst.Macro)iElse);
 			} else {
 				transformStepResolve(iElse);
 			}
 
 			Iterator<org.eclipse.acceleo.parser.cst.LetBlock> iElseLetIt = iLetBlock.getElseLet().iterator();
 			while (iElseLetIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.LetBlock iNext = iElseLetIt.next();
 				transformStepResolve(iNext);
 
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.FileBlock' of the
 	 * input model.
 	 * 
 	 * @param iFileBlock
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.FileBlock'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.FileBlock iFileBlock) {
 		org.eclipse.acceleo.model.mtl.FileBlock oFileBlock = factory.getOrCreateFileBlock(iFileBlock);
 		if (iFileBlock != null && oFileBlock != null) {
 			org.eclipse.acceleo.parser.cst.ModelExpression iFileUrl = iFileBlock.getFileUrl();
 			org.eclipse.ocl.ecore.OCLExpression oFileUrl = factory.getOrCreateOCLExpression(iFileUrl);
 			if (oFileUrl != null) {
 				oFileBlock.setFileUrl(oFileUrl);
 				if (oFileUrl.getEType() != null && !oFileUrl.getEType().equals(getOCL().getStringType())) {
 					logProblem(AcceleoParserMessages.getString(
 							"IAcceleoParserProblemsConstants.InvalidUrlType", //$NON-NLS-1$
 							oFileUrl.getEType().getName()), oFileUrl.getStartPosition(), oFileUrl
 							.getEndPosition());
 				}
 			}
 
 			transformStepResolve(iFileUrl);
 
 			org.eclipse.acceleo.parser.cst.ModelExpression iFileCharset = iFileBlock.getCharset();
 			org.eclipse.ocl.ecore.OCLExpression oFileCharset = factory.getOrCreateOCLExpression(iFileCharset);
 			if (oFileCharset != null) {
 				oFileBlock.setCharset(oFileCharset);
 			}
 			transformStepResolve(iFileCharset);
 
 			org.eclipse.acceleo.parser.cst.InitSection iInit = iFileBlock.getInit();
 			transformStepResolveAddVariables(iInit);
 
 			transformStepResolveBody(iFileBlock);
 
 			transformStepResolveRemoveVariables(iInit);
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.TraceBlock' of
 	 * the input model.
 	 * 
 	 * @param iTraceBlock
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.TraceBlock'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.TraceBlock iTraceBlock) {
 		org.eclipse.acceleo.model.mtl.TraceBlock oTraceBlock = factory.getOrCreateTraceBlock(iTraceBlock);
 		if (iTraceBlock != null && oTraceBlock != null) {
 			org.eclipse.acceleo.parser.cst.ModelExpression iModelElement = iTraceBlock.getModelElement();
 			org.eclipse.ocl.ecore.OCLExpression oModelElement = factory
 					.getOrCreateOCLExpression(iModelElement);
 			if (oModelElement != null) {
 				oTraceBlock.setModelElement(oModelElement);
 			}
 			transformStepResolve(iModelElement);
 
 			org.eclipse.acceleo.parser.cst.InitSection iInit = iTraceBlock.getInit();
 			transformStepResolveAddVariables(iInit);
 
 			transformStepResolveBody(iTraceBlock);
 
 			transformStepResolveRemoveVariables(iInit);
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.Macro' of the
 	 * input model.
 	 * 
 	 * @param iMacro
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.Macro'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.Macro iMacro) {
 		org.eclipse.acceleo.model.mtl.Macro oMacro = factory.getOrCreateMacro(iMacro);
 		if (iMacro != null && oMacro != null) {
 			Iterator<org.eclipse.acceleo.parser.cst.Variable> iParameterIt = iMacro.getParameter().iterator();
 			org.eclipse.ocl.ecore.Variable oFirst = null;
 			while (iParameterIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.Variable iNext = iParameterIt.next();
 				if (oFirst == null && iNext != null) {
 					oFirst = factory.getOrCreateVariable(iNext);
 				}
 				transformStepResolveAddVariable(iNext);
 
 			}
 			if (oFirst != null && oFirst.getType() != null) {
 				factory.getOCL().pushContext(oFirst.getType());
 			}
 			try {
 				org.eclipse.acceleo.parser.cst.InitSection iInit = iMacro.getInit();
 				transformStepResolveAddVariables(iInit);
 
 				transformStepResolveBody(iMacro);
 
 				iParameterIt = iMacro.getParameter().iterator();
 				while (iParameterIt.hasNext()) {
 					org.eclipse.acceleo.parser.cst.Variable iNext = iParameterIt.next();
 					transformStepResolveRemoveVariable(iNext);
 
 				}
 				transformStepResolveRemoveVariables(iInit);
 			} finally {
 				if (oFirst != null && oFirst.getType() != null) {
 					factory.getOCL().popContext();
 				}
 			}
 			if (oMacro.getType() == null || oMacro.getType() == factory.getOCL().getInvalidType()) {
 				EClassifier eClassifier = factory.getOCL().lookupClassifier(iMacro.getType());
 				if (eClassifier != null) {
 					oMacro.setType(eClassifier);
 				} else {
 					logProblem(IAcceleoParserProblemsConstants.SYNTAX_TYPE_NOT_VALID + iMacro.getType(),
 							iMacro.getStartPosition(), iMacro.getEndPosition());
 				}
 			}
 			if (oMacro.isDeprecated()) {
 				logWarning(DEPRECATED_MESSAGE, oMacro.getStartPosition(), oMacro.getEndPosition());
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each 'org.eclipse.acceleo.parser.cst.Query' of the
 	 * input model.
 	 * 
 	 * @param iQuery
 	 *            is the input object of type 'org.eclipse.acceleo.parser.cst.Query'
 	 */
 	private void transformStepResolve(org.eclipse.acceleo.parser.cst.Query iQuery) {
 		org.eclipse.acceleo.model.mtl.Query oQuery = factory.getOrCreateQuery(iQuery);
 		if (iQuery != null && oQuery != null) {
 			Iterator<org.eclipse.acceleo.parser.cst.Variable> iParameterIt = iQuery.getParameter().iterator();
 			org.eclipse.ocl.ecore.Variable oFirst = null;
 			while (iParameterIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.Variable iNext = iParameterIt.next();
 				if (oFirst == null && iNext != null) {
 					oFirst = factory.getOrCreateVariable(iNext);
 				}
 				transformStepResolveAddVariable(iNext);
 			}
 			if (oFirst != null && oFirst.getType() != null) {
 				factory.getOCL().pushContext(oFirst.getType());
 			}
 			try {
 				org.eclipse.acceleo.parser.cst.ModelExpression iExpression = iQuery.getExpression();
 				org.eclipse.ocl.ecore.OCLExpression oExpression = factory
 						.getOrCreateOCLExpression(iExpression);
 				if (oExpression != null) {
 					oQuery.setExpression(oExpression);
 				}
 				transformStepResolve(iExpression);
 
 				iParameterIt = iQuery.getParameter().iterator();
 				while (iParameterIt.hasNext()) {
 					org.eclipse.acceleo.parser.cst.Variable iNext = iParameterIt.next();
 					transformStepResolveRemoveVariable(iNext);
 				}
 			} finally {
 				if (oFirst != null && oFirst.getType() != null) {
 					factory.getOCL().popContext();
 				}
 			}
 			if (oQuery.getType() == null || oQuery.getType() == factory.getOCL().getInvalidType()) {
 				EClassifier eClassifier = factory.getOCL().lookupClassifier(iQuery.getType());
 				if (eClassifier != null) {
 					oQuery.setType(eClassifier);
 				} else {
 					logProblem(IAcceleoParserProblemsConstants.SYNTAX_TYPE_NOT_VALID + iQuery.getType(),
 							iQuery.getStartPosition(), iQuery.getEndPosition());
 				}
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each reference 'ownedModuleElement' of an input
 	 * module.
 	 * 
 	 * @param iModule
 	 *            is the input module
 	 */
 	private void transformStepResolveOwnedModuleElement(org.eclipse.acceleo.parser.cst.Module iModule) {
 		if (!isCanceled) {
 			Iterator<org.eclipse.acceleo.parser.cst.ModuleElement> iOwnedModuleElementIt = iModule
 					.getOwnedModuleElement().iterator();
 			while (iOwnedModuleElementIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.ModuleElement iNext = iOwnedModuleElementIt.next();
 				if (isValidRegion(iNext)) {
 					if (iNext instanceof org.eclipse.acceleo.parser.cst.Template) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.Template)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.Macro) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.Macro)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.Query) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.Query)iNext);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * The step 'StepResolve' of the transformation for each reference 'body' of an input block.
 	 * 
 	 * @param iBlock
 	 *            is the input block
 	 */
 	private void transformStepResolveBody(org.eclipse.acceleo.parser.cst.Block iBlock) {
 		if (!isCanceled) {
 			Iterator<org.eclipse.acceleo.parser.cst.TemplateExpression> iBodyIt = iBlock.getBody().iterator();
 			while (iBodyIt.hasNext()) {
 				org.eclipse.acceleo.parser.cst.TemplateExpression iNext = iBodyIt.next();
 				if (isValidRegion(iNext)) {
 					if (iNext instanceof org.eclipse.acceleo.parser.cst.Template) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.Template)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.ModelExpression) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.ModelExpression)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.TextExpression) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.TextExpression)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.ProtectedAreaBlock) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.ProtectedAreaBlock)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.ForBlock) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.ForBlock)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.IfBlock) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.IfBlock)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.LetBlock) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.LetBlock)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.FileBlock) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.FileBlock)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.TraceBlock) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.TraceBlock)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.Macro) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.Macro)iNext);
 					} else if (iNext instanceof org.eclipse.acceleo.parser.cst.Block) {
 						transformStepResolve((org.eclipse.acceleo.parser.cst.Block)iNext);
 					} else if (!(iNext instanceof org.eclipse.acceleo.parser.cst.Comment)) {
 						logProblem(IAcceleoParserProblemsConstants.SYNTAX_TEXT_NOT_VALID + ' '
 								+ iNext.getClass().getName(), iNext.getStartPosition(), iNext
 								.getEndPosition());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * The resolution step can be limited to the specified region, for increasing performances. This method
 	 * validates if the given node is inside the region.
 	 * 
 	 * @param iNext
 	 *            is the CST node to test
 	 * @return true if the given node is inside the region
 	 */
 	private boolean isValidRegion(org.eclipse.acceleo.parser.cst.CSTNode iNext) {
 		if (iNext != null) {
 			return (resolveBeginPosition == -1 || resolveBeginPosition >= iNext.getStartPosition())
 					&& (resolveEndPosition == -1 || resolveEndPosition <= iNext.getEndPosition());
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Returns <code>true</code> if <code>eClass</code> is a sub-type of <code>superType</code>,
 	 * <code>false</code> otherwise.
 	 * 
 	 * @param superType
 	 *            Expected super type of <code>eClass</code>.
 	 * @param eClass
 	 *            EClass to consider.
 	 * @return <code>true</code> if <code>eClass</code> is a sub-type of <code>superType</code>,
 	 *         <code>false</code> otherwise.
 	 */
 	private boolean isSubTypeOf(EClass superType, EClass eClass) {
 		for (final EClass candidate : eClass.getEAllSuperTypes()) {
 			if (candidate == superType) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
