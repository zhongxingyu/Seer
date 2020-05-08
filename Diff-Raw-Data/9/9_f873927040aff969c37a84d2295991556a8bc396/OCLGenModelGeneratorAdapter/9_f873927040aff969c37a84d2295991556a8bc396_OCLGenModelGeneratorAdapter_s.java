 /**
  * <copyright>
  * 
  * Copyright (c) 2011 E.D.Willink and others.
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
  * $Id$
  */
 package org.eclipse.ocl.examples.codegen.ecore;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.acceleo.engine.generation.strategy.IAcceleoGenerationStrategy;
 import org.eclipse.acceleo.engine.generation.strategy.PreviewStrategy;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.codegen.ecore.CodeGenEcorePlugin;
 import org.eclipse.emf.codegen.ecore.genmodel.GenAnnotation;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.common.util.Monitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.ocl.examples.codegen.tables.Model2bodies;
 import org.eclipse.ocl.examples.codegen.tables.Model2tables;
 import org.eclipse.ocl.examples.library.LibraryConstants;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.UMLReflection;
 import org.eclipse.ocl.examples.pivot.delegate.OCLDelegateDomain;
 import org.eclipse.ocl.examples.pivot.ecore.Ecore2Pivot;
 import org.eclipse.ocl.examples.pivot.ecore.Pivot2Ecore;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceSetAdapter;
 import org.eclipse.ocl.examples.pivot.utilities.Pivot2Moniker;
 import org.eclipse.uml2.codegen.ecore.genmodel.util.UML2GenModelUtil;
 
 public class OCLGenModelGeneratorAdapter extends GenBaseGeneratorAdapter
 {
 	public static final String OCL_GENMODEL_URI = "http://www.eclipse.org/OCL/GenModel";
 	public static final String USE_DELEGATES_KEY = "Use Delegates";
 
 	/**
 	 * Return  true if the genModel has a {@link OCL_GENMODEL_URI} GenAnnotation with a
 	 * {@link USE_DELEGATES_KEY} detail set to true.
 	 */
 	public static boolean useDelegates(GenModel genModel) {
 		boolean useDelegates = false;
 		GenAnnotation genAnnotation = genModel.getGenAnnotation(OCL_GENMODEL_URI);
 		if (genAnnotation != null) {
 			EMap<String, String> details = genAnnotation.getDetails();
 			useDelegates = Boolean.valueOf(details.get(USE_DELEGATES_KEY));
 		}
 		return useDelegates;
 	}
 
 	private Set<GenModel> hadDelegates = new HashSet<GenModel>();
 	
 	public OCLGenModelGeneratorAdapter(OCLGeneratorAdapterFactory generatorAdapterFactory) {
 		super(generatorAdapterFactory);
 	}
 
 	protected void convertConstraintToOperation(Ecore2Pivot ecore2pivot, GenModel genModel, EClassifier eClassifier, String key, String body, String message) {
 		Type pType = ecore2pivot.getCreated(Type.class, eClassifier);
 		List<Constraint> ownedRules = pType.getOwnedRules();
 		for (Constraint rule : ownedRules) {
			if (rule.getStereotype().equals(UMLReflection.INVARIANT) && rule.getName().equals(key)) {
 				String prefix = UML2GenModelUtil.getInvariantPrefix(genModel);
				EOperation eOperation = Pivot2Ecore.createConstraintEOperation(rule, prefix + rule.getName());
 				((EClass)eClassifier).getEOperations().add(eOperation);
 				ecore2pivot.addMapping(eOperation, rule);
 				EcoreUtil.setAnnotation(eOperation, OCLDelegateDomain.OCL_DELEGATE_URI_PIVOT, "body", body);
 				if (message != null) {
 					EcoreUtil.setAnnotation(eOperation, OCLDelegateDomain.OCL_DELEGATE_URI_PIVOT, "body" + PivotConstants.MESSAGE_ANNOTATION_DETAIL_SUFFIX, message);
 				}
 			}
 		}
 	}
 
 	protected void convertConstraintsToOperations(MetaModelManager metaModelManager, GenModel genModel) {
 		List<GenPackage> genPackages = genModel.getAllGenPackagesWithClassifiers();
 		for (GenPackage genPackage : genPackages) {
 			EPackage ecorePackage = genPackage.getEcorePackage();
 			Ecore2Pivot ecore2pivot = Ecore2Pivot.getAdapter(ecorePackage.eResource(), metaModelManager);
 			for (GenClass genClass : genPackage.getGenClasses()) {
 				EClass eClass = genClass.getEcoreClass();
 				EClassifier eClassifier = eClass;
 				List<EAnnotation> obsoleteAnnotations = null;
 				for (EAnnotation eAnnotation : eClassifier.getEAnnotations()) {
 					String source = eAnnotation.getSource();
 					if (OCLDelegateDomain.isDelegateURI(source)) {
 						EMap<String, String> details = eAnnotation.getDetails();
 						for (String key : details.keySet()) {
 							if (!key.endsWith(PivotConstants.MESSAGE_ANNOTATION_DETAIL_SUFFIX)) {
 								String expression = details.get(key);
 								String messageExpression = details.get(key + PivotConstants.MESSAGE_ANNOTATION_DETAIL_SUFFIX);
 								convertConstraintToOperation(ecore2pivot, genModel, eClassifier, key, expression, messageExpression);
 							}
 						}
 						if (obsoleteAnnotations == null) {
 							obsoleteAnnotations = new ArrayList<EAnnotation>();
 						}
 						obsoleteAnnotations.add(eAnnotation);
 					}
 				    if (EcorePackage.eNS_URI.equals(source))
 				    {
 				      eAnnotation.getDetails().remove("constraints");
 				    }
 				}
 				if (obsoleteAnnotations != null) {
 					eClassifier.getEAnnotations().removeAll(obsoleteAnnotations);
 				}
 				genClass.initialize(eClass);
 			}
 		}
 	}
 
 	protected void createClassBodies(GenModel genModel, Monitor monitor) {
 	  	try {
 	  		File projectFolder = getProjectFolder(genModel);
             List<String> arguments = new ArrayList<String>();
   			Model2bodies generator = new Model2bodies(genModel, projectFolder, arguments);
 	        generator.generate(monitor);
 //	        folder.refreshLocal(IResource.DEPTH_INFINITE, BasicMonitor.toIProgressMonitor(CodeGenUtil.createMonitor(monitor, 1)));
 		} catch (Exception e) {
 	        CodeGenEcorePlugin.INSTANCE.log(e);
 		}
 	}
 
 	protected void createDispatchTables(GenModel genModel, Monitor monitor) {
 	  	try {
   			File projectFolder = getProjectFolder(genModel);
             List<String> arguments = new ArrayList<String>();
             Model2tables generator = new Model2tables(genModel, projectFolder, arguments);
 	        generator.generate(monitor);
 //	        folder.refreshLocal(IResource.DEPTH_INFINITE, BasicMonitor.toIProgressMonitor(CodeGenUtil.createMonitor(monitor, 1)));
 		} catch (Exception e) {
 	        CodeGenEcorePlugin.INSTANCE.log(e);
 		}
 	}
 
 	/**
 	 * Create a Map of feature identification to body to be embedded in the EMF model.
 	 */
 	protected Map<String, String> createFeatureBodies(GenModel genModel) {
 		Map<String, String> results = new HashMap<String, String>();
 	  	try {
             File folder = new File("/");       
             List<String> arguments = new ArrayList<String>();
 			Ocl2java4genmodel generator = new Ocl2java4genmodel(genModel, folder, arguments)
 			{
 			    public IAcceleoGenerationStrategy getGenerationStrategy() {
 			        return new PreviewStrategy();
 			    }
 			};
 	        Map<String, String> result = generator.generate(new BasicMonitor());
 	        for (String key : result.keySet()) {
 	        	String key2 = "/" + key.replace('\\', '/');		// BUG 359139
 				String value = result.get(key);
 				results.put(key2, value);
 	        }
 		} catch (IOException e) {
 	        CodeGenEcorePlugin.INSTANCE.log(e);
 		}
 		return results;
 	}
 
 	@Override
 	protected Diagnostic doPreGenerate(Object object, Object projectType) {
 		GenModel genModel = (GenModel) object;
 		if ((projectType == MODEL_PROJECT_TYPE) && !useDelegates(genModel) && hasDelegates(genModel)) {
 			List<String> modelPluginVariables = genModel.getModelPluginVariables();
 			if (!modelPluginVariables.contains(LibraryConstants.PLUGIN_ID)) {
 				modelPluginVariables.add(LibraryConstants.PLUGIN_ID);
 			}
 	    	GenPackage genPackage = genModel.getGenPackages().get(0);
 	        createImportManager(genPackage.getReflectionPackageName(), genPackage.getFactoryInterfaceName() + "Tables");	// Only used to suppress NPE
 			Resource genResource = genModel.eResource();
 			ResourceSet resourceSet = genResource.getResourceSet();
 			MetaModelManager metaModelManager = MetaModelManager.findAdapter(resourceSet);
 			MetaModelManagerResourceSetAdapter adapter = MetaModelManagerResourceSetAdapter.getAdapter(resourceSet, metaModelManager);
 			metaModelManager = adapter.getMetaModelManager();
 			convertConstraintsToOperations(metaModelManager, genModel);
 		    Map<String, String> results = createFeatureBodies(genModel);			
 			installJavaBodies(metaModelManager, genModel, results);
 			pruneDelegates(genModel);
 		}
 		return super.doPreGenerate(object, projectType);
 	}
 
 	@Override
 	protected Diagnostic generateModel(Object object, Monitor monitor) {
 		GenModel genModel = (GenModel) object;
 		if (!useDelegates(genModel) && hadDelegates.contains(genModel)) {
 		    monitor.beginTask("", 3);
 		    monitor.subTask("Generating Dispatch Tables");
 		    GenPackage genPackage = genModel.getGenPackages().get(0);
 		    createImportManager(genPackage.getReflectionPackageName(), genPackage.getFactoryInterfaceName() + "Tables");	// Only used to suppress NPE
 			createDispatchTables(genModel, monitor);
 		    monitor.worked(1);
 		    createClassBodies(genModel, monitor);
 		    monitor.worked(1);
 		}
 		return super.generateModel(object, monitor);
 	}
 
 	protected File getProjectFolder(GenModel genModel) {
 		String modelProjectDirectory = genModel.getModelProjectDirectory();
 		String modelDirectory = genModel.getModelDirectory();
 		if (EMFPlugin.IS_ECLIPSE_RUNNING) {
 		    IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		    IProject modelProject = workspace.getRoot().getProject(modelProjectDirectory);
 		    IPath javaSource = new Path(modelDirectory);
 		    IFolder folder = modelProject.getParent().getFolder(javaSource);
 		    java.net.URI locationURI = folder.getLocationURI();
 		    return new File(locationURI.getRawPath());
 		}
 		else {
 		    URI locationURI = URI.createPlatformResourceURI(modelDirectory, true);
 			ResourceSet resourceSet = genModel.eResource().getResourceSet();
 			URIConverter uriConverter = resourceSet != null ? resourceSet.getURIConverter() : URIConverter.INSTANCE;
 			URI normalizedURI = uriConverter.normalize(locationURI);
 			return new File(normalizedURI.toFileString());
 		}
 	}
 
 	/**
 	 * Return true if any local GenPackage is for an EPackage that has OCL validation/setting/invocation delegates.
 	 */
 	protected boolean hasDelegates(GenModel genModel) {
 		for (GenPackage genPackage : genModel.getGenPackages()) {
 			EPackage ePackage = genPackage.getEcorePackage();
 			if (hasDelegates(ePackage)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	protected boolean hasDelegates(EPackage ePackage) {
 		List<String> validationDelegates = EcoreUtil.getValidationDelegates(ePackage);
 		for (String validationDelegate : validationDelegates) {
 			if (OCLDelegateDomain.isDelegateURI(validationDelegate)) {
 				return true;
 			}
 		}
 		List<String> settingDelegates = EcoreUtil.getSettingDelegates(ePackage);
 		for (String settingDelegate : settingDelegates) {
 			if (OCLDelegateDomain.isDelegateURI(settingDelegate)) {
 				return true;
 			}
 		}
 		List<String> invocationDelegates = EcoreUtil.getInvocationDelegates(ePackage);
 		for (String invocationDelegate : invocationDelegates) {
 			if (OCLDelegateDomain.isDelegateURI(invocationDelegate)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	protected void installJavaBodies(MetaModelManager metaModelManager, GenModel genModel, Map<String, String> results) {
 		List<GenPackage> genPackages = genModel.getAllGenPackagesWithClassifiers();
 		for (GenPackage genPackage : genPackages) {
 			EPackage ecorePackage = genPackage.getEcorePackage();
 			Ecore2Pivot ecore2pivot = Ecore2Pivot.getAdapter(ecorePackage.eResource(), metaModelManager);
 			for (GenClass genClass : genPackage.getGenClasses()) {
 				EClass eClass = genClass.getEcoreClass();
 				for (EOperation eOperation : eClass.getEOperations()) {
 					installOperation(ecore2pivot, eOperation, results);
 				}
 				for (EStructuralFeature eFeature : eClass.getEStructuralFeatures()) {
 					installProperty(ecore2pivot, eFeature, results);
 				}
 			}
 		}
 	}
 
 	protected void installOperation(Ecore2Pivot ecore2pivot, EOperation eOperation, Map<String, String> results) {
 		Element pOperation = ecore2pivot.getCreated(Element.class, eOperation);
 		String fragmentURI;
 		if (pOperation instanceof Operation) {
 			fragmentURI = EcoreUtil.getURI(pOperation).fragment().toString();
 		}
 		else {
 			Constraint constraint = (Constraint) pOperation;
 			fragmentURI = EcoreUtil.getURI(constraint.eContainer()).fragment().toString() + "==" + constraint.getName();
 		}
 		String body = results.get(fragmentURI);
 		if ((body == null) || (body.trim().length() == 0)) {
 			body = "throw new UnsupportedOperationException();  // FIXME Unimplemented " + Pivot2Moniker.toString(pOperation);
 		}
 		EcoreUtil.setAnnotation(eOperation, GenModelPackage.eNS_URI, "body", body);
 		List<EAnnotation> eAnnotations = eOperation.getEAnnotations();
 		EAnnotation oclAnnotation = eOperation.getEAnnotation(OCLDelegateDomain.OCL_DELEGATE_URI);
 		if (oclAnnotation != null) {
 			eAnnotations.remove(oclAnnotation);
 		}
 		oclAnnotation = eOperation.getEAnnotation(OCLDelegateDomain.OCL_DELEGATE_URI_LPG);
 		if (oclAnnotation != null) {
 			eAnnotations.remove(oclAnnotation);
 		}
 		oclAnnotation = eOperation.getEAnnotation(OCLDelegateDomain.OCL_DELEGATE_URI_PIVOT);
 		if (oclAnnotation != null) {
 			eAnnotations.remove(oclAnnotation);
 		}
 		oclAnnotation = eOperation.getEAnnotation(UML2GenModelUtil.UML2_GEN_MODEL_PACKAGE_1_1_NS_URI);
 		if (oclAnnotation != null) {
 			eAnnotations.remove(oclAnnotation);
 		}
 	}
 
 	protected void installProperty(Ecore2Pivot ecore2pivot, EStructuralFeature eFeature, Map<String, String> results) {
 		Property pProperty = ecore2pivot.getCreated(Property.class, eFeature);
 		String fragmentURI = EcoreUtil.getURI(pProperty).fragment().toString();
 		String body = results.get(fragmentURI);
 		if (body == null) {
 			body = "throw new UnsupportedOperationException();  // FIXME Unimplemented " + Pivot2Moniker.toString(pProperty);
 		}
 		if (body != null) {
 			EcoreUtil.setAnnotation(eFeature, GenModelPackage.eNS_URI, "get", body);
 //			EcoreUtil.setAnnotation(eFeature, GenModelPackage.eNS_URI, "body", body);
 			List<EAnnotation> eAnnotations = eFeature.getEAnnotations();
 			EAnnotation oclAnnotation = eFeature.getEAnnotation(OCLDelegateDomain.OCL_DELEGATE_URI);
 			if (oclAnnotation != null) {
 				eAnnotations.remove(oclAnnotation);
 			}
 			oclAnnotation = eFeature.getEAnnotation(OCLDelegateDomain.OCL_DELEGATE_URI_LPG);
 			if (oclAnnotation != null) {
 				eAnnotations.remove(oclAnnotation);
 			}
 			oclAnnotation = eFeature.getEAnnotation(OCLDelegateDomain.OCL_DELEGATE_URI_PIVOT);
 			if (oclAnnotation != null) {
 				eAnnotations.remove(oclAnnotation);
 			}
 			oclAnnotation = eFeature.getEAnnotation(UML2GenModelUtil.UML2_GEN_MODEL_PACKAGE_1_1_NS_URI);
 			if (oclAnnotation != null) {
 				eAnnotations.remove(oclAnnotation);
 			}
 		}
 	}
 
 	/**
 	 * Eliminate all OCL validation/setting/invocation delegates.
 	 */
 	protected void pruneDelegates(GenModel genModel) {
 		for (GenPackage genPackage : genModel.getGenPackages()) {
 			EPackage ePackage = genPackage.getEcorePackage();
 			if (hasDelegates(ePackage)) {
 				hadDelegates.add(genModel);
 				EcoreUtil.setValidationDelegates(ePackage, pruneDelegates(EcoreUtil.getValidationDelegates(ePackage)));
 				EcoreUtil.setSettingDelegates(ePackage, pruneDelegates(EcoreUtil.getSettingDelegates(ePackage)));
 				EcoreUtil.setInvocationDelegates(ePackage, pruneDelegates(EcoreUtil.getInvocationDelegates(ePackage)));
 			}
 		}
 	}
 	protected List<String> pruneDelegates(List<String> oldDelegates) {
 		List<String> newDelegates = new ArrayList<String>();
 		for (String aDelegate : oldDelegates) {
 			if (!OCLDelegateDomain.isDelegateURI(aDelegate)) {
 				newDelegates.add(aDelegate);
 			}
 		}
 		return newDelegates;
 	}
 }
