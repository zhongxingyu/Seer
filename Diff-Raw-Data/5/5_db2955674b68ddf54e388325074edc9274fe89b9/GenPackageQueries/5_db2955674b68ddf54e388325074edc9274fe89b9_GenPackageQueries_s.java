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
 package org.eclipse.ocl.examples.codegen.common;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClassifier;
 import org.eclipse.emf.codegen.ecore.genmodel.GenEnum;
 import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenOperation;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.codegen.util.CodeGenUtil;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.elements.DomainPackage;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.library.LibraryConstants;
 import org.eclipse.ocl.examples.library.oclstdlib.OCLstdlibPackage;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.Feature;
 import org.eclipse.ocl.examples.pivot.Library;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.Package;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PrimitiveType;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.ecore.Ecore2Pivot;
 import org.eclipse.ocl.examples.pivot.manager.FinalAnalysis;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceSetAdapter;
 import org.eclipse.ocl.examples.pivot.manager.TypeServer;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.uml2.codegen.ecore.genmodel.util.UML2GenModelUtil;
 
 public class GenPackageQueries
 {
 	@Deprecated // Obsolete
 	public @NonNull Type getAnotherType(@NonNull GenPackage genPackage, @NonNull Type type) {
 		MetaModelManager metaModelManager = getMetaModelManager(genPackage);
 		Type primaryType = metaModelManager.getPrimaryType(type);
 		return primaryType;
 	}
 	
 	public @Nullable String getCopyright(@NonNull GenPackage genPackage, @NonNull String indentation) {
 		return genPackage.getCopyright(indentation);
 	}
 
 	public @Nullable String getEcorePackageName(@NonNull GenPackage genPackage) {
 		return genPackage.getEcorePackage().getName();	// Workaround for Acceleo URI resolution bug
 	}
 	
 	public String getEscapedInterfaceName(@NonNull GenPackage genPackage, @NonNull Type type) {
 		Package package1 = type.getPackage();
 		assert package1 != null;
 		GenPackage genPackage2 = getGenPackage(genPackage, package1);
 		if (genPackage2 != null) {
 			GenClass genClass = getGenClass(genPackage2, type);
 			if (genClass != null) {
 				return "<%" + genClass.getQualifiedInterfaceName() + "%>";
 			}
 		}
		return "";
 	}
 	
 	public String getEscapedInterfacePackageName(@NonNull GenPackage genPackage, @NonNull org.eclipse.ocl.examples.pivot.Package package1) {
 		GenPackage genPackage2 = getGenPackage(genPackage, package1);
 		if (genPackage2 != null) {
 			return "<%" + genPackage2.getQualifiedPackageInterfaceName() + "%>";
 		}
		return "";
 	}
 	
 	public String getEscapedLiteralsName(@NonNull GenPackage genPackage, @NonNull Type type) {
 		GenClassifier genClassifier = getGenClassifier(genPackage, type);
 		if (genClassifier != null) {
 			return "<%" + genClassifier.getGenPackage().getQualifiedPackageInterfaceName() + "%>.Literals." + CodeGenUtil.upperName(genClassifier.getName());
 		}
 		return "";
 	}
 	
 	public String getEscapedPropertyType(@NonNull GenPackage genPackage, @NonNull Property property) {
 		Type owningType = property.getOwningType();
 		if (owningType != null) {
 			GenClass genClass = getGenClass(genPackage, owningType);
 			if (genClass != null) {
 				GenFeature genFeature = getGenFeature(genPackage, genClass, property);
 				if (genFeature != null) {
 					return genFeature.getQualifiedObjectType(genClass);
 				}
 			}
 			else {
 				return "Object";
 			}
 		}
 		return "";
 	}
 	
 	public @NonNull String getFeatureTypeCast(@NonNull GenPackage genPackage, @NonNull Feature typedElement) {
 		return "(" + typedElement.getClass().getSimpleName() + ")";
 	}
 	
 	public @Nullable GenClass getGenClass(@NonNull GenPackage genPackage, @NonNull Type type) {
 		String name = type.getName();
 		for (GenClass genClass : genPackage.getGenClasses()) {
 			String clsName = genClass.getEcoreClass().getName();
 			if (name.equals(clsName)) {
 				return genClass;
 			}
 		}
 		return null;
 	}
 	
 	public @Nullable GenClassifier getGenClassifier(@NonNull GenPackage genPackage, @NonNull Type type) {
 		String name = type.getName();
 		for (GenClassifier genClassifier : genPackage.getGenClassifiers()) {
 			String clsName = genClassifier.getEcoreClassifier().getName();
 			if (name.equals(clsName)) {
 				return genClassifier;
 			}
 		}
 		return null;
 	}
 	
 	public @Nullable GenFeature getGenFeature(@NonNull GenPackage genPackage, @NonNull GenClass genClass, @NonNull Property property) {
 		String name = property.getName();
 		for (GenFeature genFeature : genClass.getGenFeatures()) {
 			String featureName = genFeature.getEcoreFeature().getName();
 			if (name.equals(featureName)) {
 				return genFeature;
 			}
 		}
 		return null;
 	}
 	
 	public @Nullable GenOperation getGenOperation(@NonNull GenPackage genPackage, @NonNull GenClass genClass, @NonNull Operation operation) {
 		String name = operation.getName();
 		for (GenOperation genOperation : genClass.getGenOperations()) {
 			if (name.equals(genOperation.getName())) {
 				return genOperation;		// FIXME signatures
 			}
 		}
 		return null;
 	}
 	
 	@Deprecated
 	public @Nullable GenPackage getGenPackage(@NonNull GenPackage genPackage, @NonNull org.eclipse.ocl.examples.pivot.Package pivotPackage) {
 		return getGenPackage(genPackage, (DomainPackage)pivotPackage); 
 	}
 	public @Nullable GenPackage getGenPackage(@NonNull GenPackage genPackage, @NonNull DomainPackage pivotPackage) {
 //		org.eclipse.ocl.examples.pivot.Package pivotPackage = pivotType.getPackage();
 //		if (pivotPackage == null) {
 //			return genPackage;	// FIXME
 //		}
 		EPackage firstEPackage = genPackage.getEcorePackage();
 		if (firstEPackage.getName().equals(pivotPackage.getName())) {
 			return genPackage;
 		}
 		GenModel genModel = genPackage.getGenModel();
 		List<GenPackage> usedGenPackages = genModel.getUsedGenPackages();
 		assert usedGenPackages != null;
 //		String nsURI = pivotPackage.getNsURI();
 //		String name = pivotType.getName();
 //		GenPackage usedGenPackage = getNsURIGenPackage(usedGenPackages, nsURI, name);
 //		if (usedGenPackage != null) {
 //			return usedGenPackage;
 //		}		
 		Resource genModelResource = genPackage.eResource();
 		ResourceSet genModelResourceSet = genModelResource.getResourceSet();
 		assert genModelResourceSet != null;
 		MetaModelManager metaModelManager = getMetaModelManager(genPackage);
 		DomainPackage metaModelPackage = metaModelManager.getPivotMetaModel();
 		org.eclipse.ocl.examples.pivot.Package libraryPackage = metaModelManager.getLibraries().get(0);
 		if (pivotPackage == libraryPackage) {
 			GenPackage libraryGenPackage = getLibraryGenPackage(usedGenPackages);
 			if (libraryGenPackage == null) {
 				libraryGenPackage = loadGenPackage(genModelResourceSet, LibraryConstants.GEN_MODEL_URI);
 			}
 			return libraryGenPackage;
 		}
 		if (pivotPackage == metaModelPackage) {
 			GenPackage metaModelGenPackage = getMetaModelGenPackage(usedGenPackages);
 			if (metaModelGenPackage == null) {
 				metaModelGenPackage = loadGenPackage(genModelResourceSet, PivotConstants.GEN_MODEL_URI);
 			}
 			return metaModelGenPackage;
 		}
 		String nsURI = pivotPackage.getNsURI();
 		if (nsURI != null) {
 			GenPackage genPackage2 = metaModelManager.getGenPackage(nsURI);
 			if (genPackage2 != null) {
 				return genPackage2;
 			}
 		}
 		return genPackage;	// FIXME
 	}
 
 	protected @NonNull MetaModelManager getMetaModelManager(@NonNull GenPackage genPackage) {
 		Resource genModelResource = genPackage.eResource();
 		ResourceSet genModelResourceSet = genModelResource.getResourceSet();
 		assert genModelResourceSet != null;
 		MetaModelManagerResourceSetAdapter resourceSetAdapter = MetaModelManagerResourceSetAdapter.getAdapter(genModelResourceSet, null);
 		MetaModelManager metaModelManager = resourceSetAdapter.getMetaModelManager();
 		return metaModelManager;
 	}
 	
 	public String getInterfacePackageName(@NonNull GenPackage genPackage) {
 		return genPackage.getInterfacePackageName();
 	}
 
 	private <T extends GenPackage> T getLibraryGenPackage(List<T> genPackages) {
 		for (T genPackage : genPackages) {
 			EPackage ecorePackage = genPackage.getEcorePackage();
 			EClassifier eClassifier = ecorePackage.getEClassifier("_Dummy");		// FIXME
 			if (eClassifier != null) {
 				return genPackage;
 			}
 		}		
 		return null;
 	}
 
 	private @Nullable <T extends GenPackage> T getMetaModelGenPackage(@NonNull List<T> genPackages) {
 		for (T genPackage : genPackages) {
 			EPackage ecorePackage = genPackage.getEcorePackage();
 			EClassifier eClassifier = ecorePackage.getEClassifier("Element");
 			if (eClassifier != null) {
 				return genPackage;
 			}
 		}		
 		return null;
 	}
 
 	private @Nullable <T extends GenClassifier> T getNamedElement1(@Nullable List<T> genClasses, @NonNull String name) {
 		if (genClasses != null) {
 			for (T genClass : genClasses) {
 				if (genClass.getName().equals(name)) {
 					return genClass;
 				}
 			}
 		}
 		return null;
 	}
 
 	private @Nullable <T extends GenFeature> T getNamedElement2(@Nullable List<T> genClasses, @NonNull String name) {
 		if (genClasses != null) {
 			for (T genClass : genClasses) {
 				if (genClass.getName().equals(name)) {
 					return genClass;
 				}
 			}
 		}
 		return null;
 	}
 
 /*	private <T extends GenPackage> T getNsURIGenPackage(List<T> genPackages, String nsURI, String name) {
 		for (T genPackage : genPackages) {
 			EPackage ecorePackage = genPackage.getEcorePackage();
 			if (ecorePackage.getNsURI().equals(nsURI)) {
 				EClassifier eClassifier = ecorePackage.getEClassifier(name);
 				if (eClassifier != null) {
 					return genPackage;
 				}
 			}
 		}		
 		return null;
 	} */
 	protected static boolean isBlank(@Nullable String string)
 	{
 	    return string == null || string.length() == 0;
 	}
 	
 	public String getOperationID(@NonNull GenPackage genPackage, @NonNull Type type, @NonNull Constraint rule, @NonNull Boolean diagnosticCode) {
 		GenClass genClass = getGenClass(genPackage, type);
 		if (genClass != null) {
 			String name;
 			String prefix = null;
 			prefix = UML2GenModelUtil.getInvariantPrefix(genPackage.getGenModel());
 			if (rule.isCallable()) {
 				name = rule.getName();
 			}
 			else {
 				name = prefix + rule.getName();
 			}
 			for (GenOperation genOperation : genClass.getGenOperations()) {
 				String opName = genOperation.getEcoreOperation().getName();
 				if (name.equals(opName)) {
 					String operationID;
 					if (!isBlank(prefix)) {
 						String upperCaseOpName = CodeGenUtil.format(genOperation.getName(), '_', prefix, false, false).toUpperCase(); //$NON-NLS-1$
 						operationID = genClass.getClassifierID() + "__" + upperCaseOpName; //$NON-NLS-1$
 					}
 					else {
 						operationID = genClass.getOperationID(genOperation, diagnosticCode);
 					}
 					return operationID;
 				}
 			}
 		}
 		return "<<unknown-OperationId>>";
 	}
 	
 	public String getOperationResultType(@NonNull GenPackage genPackage, @NonNull Operation operation) {
 		Type owningType = operation.getOwningType();
 		if (owningType != null) {
 			GenClass genClass = getGenClass(genPackage, owningType);
 			if (genClass != null) {
 				GenOperation genOperation = getGenOperation(genPackage, genClass, operation);
 				if (genOperation != null) {
 					return genOperation.getQualifiedObjectType(genClass);
 				}
 			}
 		}
 		return "";
 	}
 	
 	public String getOperationReturnType(@NonNull GenPackage genPackage, @NonNull Operation operation) {
 		Type owningType = operation.getOwningType();
 		if (owningType != null) {
 			GenClass genClass = getGenClass(genPackage, owningType);
 			if (genClass != null) {
 				GenOperation genOperation = getGenOperation(genPackage, genClass, operation);
 				if (genOperation != null) {
 					return genOperation.getType(genClass);
 				}
 			}
 		}
 		return "";
 	}
 	
 	public org.eclipse.ocl.examples.pivot.Package getPivotPackage(@NonNull GenPackage genPackage) {
 		EPackage ePackage = genPackage.getEcorePackage();
 		Resource ecoreResource = ePackage.eResource();
 		if (ecoreResource == null) {
 			return null;
 		}
 		MetaModelManager metaModelManager = getMetaModelManager(genPackage);
 		Ecore2Pivot ecore2Pivot = Ecore2Pivot.getAdapter(ecoreResource, metaModelManager);
 		org.eclipse.ocl.examples.pivot.Package pivotPackage = ecore2Pivot.getCreated(org.eclipse.ocl.examples.pivot.Package.class, ePackage);
 		if (pivotPackage == null) {
 			return null;
 		}
 		if (pivotPackage.getNsURI().equals(OCLstdlibPackage.eNS_URI)) {				// If generating OCLstdlibTables ...
 			mergeLibrary(metaModelManager, pivotPackage);			// FIXME: redundant once M2T scans all partial types
 		}
 //		else if (pivotPackage.getNsURI().equals(PivotPackage.eNS_URI)) {			// If generating PivotTables ...
 //			mergeLibrary(metaModelManager, pivotPackage);
 //		}
 //		else if (pivotPackage.getNsURI().equals(OCLPackage.eNS_URI)) {
 //			mergeLibrary(metaModelManager, pivotPackage);
 //		}
 //		else if (pivotPackage.getNsURI().equals(OCLPackage.eNS_URI + ".oclstdlib")) {
 //			mergeLibrary(metaModelManager, pivotPackage);
 //		}
 		return pivotPackage;
 	}
 	
 	public String getPropertyGetter(@NonNull GenPackage genPackage, @NonNull Property property) {
 		Type owningType = property.getOwningType();
 		if (property.isStatic()) {
 //			owningType.getMetaTypeName()
 		}
 		if (owningType != null) {
 			GenClass genClass = getGenClass(genPackage, owningType);
 			if (genClass != null) {
 				GenFeature genFeature = getGenFeature(genPackage, genClass, property);
 				if (genFeature != null) {
 					return genFeature.getGetAccessor();
 				}
 			}
 			else {
 				String name = property.getName();
 				return "get" + name.substring(0,1).toUpperCase() + name.substring(1,name.length()-1);
 			}
 		}
 		return "";
 	}
 	
 	public String getPropertyResultType(@NonNull GenPackage genPackage, @NonNull Property property) {
 		Type owningType = property.getOwningType();
 		if (owningType != null) {
 			GenClass genClass = getGenClass(genPackage, owningType);
 			if (genClass != null) {
 				GenFeature genFeature = getGenFeature(genPackage, genClass, property);
 				if (genFeature != null) {
 					return genFeature.getQualifiedObjectType(genClass);
 				}
 			}
 		}
 		return "";
 	}
 	
 	public String getPropertyReturnType(@NonNull GenPackage genPackage, @NonNull Property property) {
 		Type owningType = property.getOwningType();
 		if (owningType != null) {
 			GenClass genClass = getGenClass(genPackage, owningType);
 			if (genClass != null) {
 				GenFeature genFeature = getGenFeature(genPackage, genClass, property);
 				if (genFeature != null) {
 					return genFeature.getType(genClass);
 				}
 			}
 		}
 		return "";
 	}
 	
 	public String getQualifiedPackageName(@NonNull GenPackage genPackage) {
 		return genPackage.getQualifiedPackageName();
 	}
 	
 	public String getQualifiedValidatorClassName(@NonNull GenPackage genPackage) {
 		return genPackage.getQualifiedValidatorClassName();
 	}
 	
 	public String getQualifyingPackage(@NonNull GenPackage genPackage, @NonNull Type type) {
 		org.eclipse.ocl.examples.pivot.Package owningPackage = type.getPackage();
 		if (owningPackage != null) {
 			GenPackage genPackage2 = getGenPackage(genPackage, owningPackage);
 			if (genPackage2 != null) {
 				return genPackage2.getQualifiedPackageName() + "." + genPackage2.getPrefix() + "Tables";
 			}
 		}
 		return "";
 	}
 
 	public String getSharedLibrary(@NonNull GenPackage genPackage) {
 		org.eclipse.ocl.examples.pivot.Package thisPackage = getPivotPackage(genPackage);
 		MetaModelManager metaModelManager = getMetaModelManager(genPackage);
 		PrimitiveType booleanType = metaModelManager.getBooleanType();
 		TypeServer typeServer = metaModelManager.getTypeServer(booleanType);
 		for (DomainType type : typeServer.getPartialTypes()) {
 			DomainPackage pivotPackage = type.getPackage();
 			if ((pivotPackage != null) && (pivotPackage != thisPackage)) {
 				GenPackage gPackage = getGenPackage(genPackage, pivotPackage);
 				if (gPackage != null) {
 					return getInterfacePackageName(gPackage) + "." + gPackage.getPrefix() + "Tables";
 				}
 			}
 		}
 		return "";
 	}
 		
 	/**
 	 * Return  true if type has another definition counterpart. The Standard Library
 	 * providers a base definition for the pivot model.
 	 */
 	@Deprecated // Obsolete
 	public @NonNull Boolean hasAnotherType(@NonNull GenPackage genPackage, @NonNull Type type) {
 /*		MetaModelManager metaModelManager = getMetaModelManager(genPackage);
 / *		TypeServer typeServer = metaModelManager.getTypeServer(type);
 		for (Type trackedType : typeServer.getTrackedTypes()) {
 			if (trackedType != type) {
 				GenPackage otherGenPackage = getGenPackage(genPackage, type.getPackage());
 				if (otherGenPackage != genPackage) {
 					return true;
 				}
 			}
 		}
 		return false; * /
 		Type primaryType = metaModelManager.getPrimaryType(type);
 //		GenClass genClass = getNamedElement1(genPackage.getGenClasses(), type.getName());
 //		if (genClass == null) {
 			return primaryType != type;
 //		}
 //		return true; */
 		return false;
 	}
 	
 	/**
 	 * Return  true if property has an Ecore counterpart. Non-navigable opposites may have a Property
 	 * but no Ecore EReference.
 	 */
 	public @NonNull Boolean hasEcore(@NonNull GenPackage genPackage, @NonNull Property property) {
 		Type owningType = property.getOwningType();
 		if (owningType == null) {
 			return false;
 		}
 		String typeName = owningType.getName();
 		if (typeName == null) {
 			return false;
 		}
 		GenClass genClass = getNamedElement1(genPackage.getGenClasses(), typeName);
 		if (genClass == null) {
 			return false;
 		}
 		String propertyName = property.getName();
 		if (propertyName == null) {
 			return false;
 		}
 		GenFeature genFeature = getNamedElement2(genClass.getAllGenFeatures(), propertyName);
 		if (genFeature == null) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Return true if type has an Ecore counterpart. The Standard Library genmodel has
 	 * no Ecore types, unless the Pivot model is also in use.
 	 */
 	public @NonNull Boolean hasEcore(@NonNull GenPackage genPackage, @NonNull Type type) {
 		String typeName = type.getName();
 		if (typeName != null) {
 			GenClass genClass = getNamedElement1(genPackage.getGenClasses(), typeName);
 			if (genClass != null) {
 				return true;
 			}
 			GenEnum genEnum = getNamedElement1(genPackage.getGenEnums(), typeName);
 			if (genEnum != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public @NonNull Boolean hasSharedLibrary(@NonNull GenPackage genPackage) {
 		org.eclipse.ocl.examples.pivot.Package thisPackage = getPivotPackage(genPackage);
 		MetaModelManager metaModelManager = getMetaModelManager(genPackage);
 		PrimitiveType booleanType = metaModelManager.getBooleanType();
 		TypeServer typeServer = metaModelManager.getTypeServer(booleanType);
 		boolean gotThatPackage = false;
 		boolean gotThisPackage = false;
 		for (DomainType type : typeServer.getPartialTypes()) {
 			if (type.getPackage() == thisPackage) {
 				gotThisPackage = true;
 			}
 			else {
 				gotThatPackage = true;
 			}
 		}
 		return gotThisPackage && gotThatPackage;
 	}
 	
 	/**
 	 * Return true if type has a compiled Tables class.
 	 */
 	public @NonNull Boolean hasTablesClass(@NonNull GenPackage genPackage, @NonNull Type type) {
 		GenPackage genPackage2 = getGenPackage(genPackage, DomainUtil.nonNullState(type.getPackage()));
 		if (genPackage2 == null) {
 			return false;
 		}
 		try {
 			String factoryInterfaceName = genPackage2.getInterfacePackageName() + "." + genPackage2.getFactoryInterfaceName();
 			Class<?> factoryInterfaceClass = Class.forName(factoryInterfaceName);
 			if (factoryInterfaceClass == null) {
 				return true;		// FIXME maybe we're going to generate it
 			}
 			String tablesClassName = factoryInterfaceName.replace("Factory", "Tables");
 			Class<?> tablesClassClass = Class.forName(tablesClassName);
 			return tablesClassClass != null;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	public Boolean isFinal(@NonNull GenPackage genPackage, @NonNull Operation anOperation) {
 		MetaModelManager metaModelManager = PivotUtil.findMetaModelManager(genPackage);
 		if (metaModelManager == null) {
 			return false;
 		}
 		FinalAnalysis finalAnalysis = metaModelManager.getPackageManager().getFinalAnalysis();
 		return finalAnalysis.isFinal(anOperation);
 	}
 
 	private @NonNull GenPackage loadGenPackage(@NonNull ResourceSet resourceSet, @NonNull URI genModelURI) {
 		Resource resource = resourceSet.getResource(genModelURI, true);
 		GenModel genModel = (GenModel) resource.getContents().get(0);
 		GenPackage genPackage = genModel.getGenPackages().get(0);
 		assert genPackage != null;
 		return genPackage;
 	}
 	
 	protected void mergeLibrary(@NonNull MetaModelManager metaModelManager, @NonNull org.eclipse.ocl.examples.pivot.Package primaryPackage) {
 //		primaryPackage.setName("ocl");
 		List<Type> primaryTypes = primaryPackage.getOwnedType();
 		for (Library library : metaModelManager.getLibraries()) {
 			Map<Type,Type> typeMap = new HashMap<Type,Type>();
 			ArrayList<Type> libraryTypes = new ArrayList<Type>(library.getOwnedType());
 			for (Type secondaryType : libraryTypes) {
 				Type primaryType = DomainUtil.getNamedElement(primaryTypes, secondaryType.getName());
 				if (primaryType != null) {
 					typeMap.put(secondaryType, primaryType);
 				}
 				else {
 					primaryTypes.add(secondaryType);
 				}
 			}
 			for (Type secondaryType : libraryTypes) {
 				Type primaryType = typeMap.get(secondaryType);
 				if (primaryType != null) {
 					List<Type> primarySuperClasses = primaryType.getSuperClass();
 					for (Type secondarySuperClass : secondaryType.getSuperClass()) {
 						Type primarySuperClass = typeMap.get(secondarySuperClass);
 						if (primarySuperClass == null) {
 							primarySuperClasses.add(secondarySuperClass);
 						}
 						else if (!primarySuperClasses.contains(primarySuperClass)) {
 							primarySuperClasses.add(primarySuperClass);
 						}
 					}
 					primaryType.getOwnedOperation().addAll(secondaryType.getOwnedOperation());
 					primaryType.getOwnedAttribute().addAll(secondaryType.getOwnedAttribute());
 				}
 			}
 		}
 		for (Type primaryType : primaryTypes) {
 			List<Type> primarySuperClasses = primaryType.getSuperClass();
 			Type classType = DomainUtil.getNamedElement(primarySuperClasses, TypeId.CLASS_NAME);
 			Type metaclass = DomainUtil.getNamedElement(primarySuperClasses, "Classifier");
 			if ((classType != null) && (metaclass != null)) {
 				primarySuperClasses.remove(classType);		// WIP FIXME fix at source
 			}
 		}
 	}  
 }
