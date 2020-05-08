 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
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
  * $Id: RoundTripTests.java,v 1.10 2011/05/12 06:04:42 ewillink Exp $
  */
 package org.eclipse.ocl.examples.test.xtext;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.match.MatchOptions;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.ocl.examples.common.utils.ClassUtils;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.ecore.Ecore2Pivot;
 import org.eclipse.ocl.examples.pivot.ecore.Pivot2Ecore;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceSetAdapter;
 import org.eclipse.ocl.examples.pivot.uml.Pivot2UML;
 import org.eclipse.ocl.examples.pivot.uml.UML2Pivot;
 import org.eclipse.ocl.examples.pivot.utilities.PivotResource;
 import org.eclipse.ocl.examples.xtext.base.pivot2cs.Pivot2CS;
 import org.eclipse.ocl.examples.xtext.base.utilities.BaseCSResource;
 import org.eclipse.ocl.examples.xtext.base.utilities.CS2PivotResourceAdapter;
 import org.eclipse.ocl.examples.xtext.oclinecore.oclinEcoreCST.OCLinEcoreCSTPackage;
 import org.eclipse.ocl.examples.xtext.oclinecore.pivot2cs.OCLinEcorePivot2CS;
 import org.eclipse.ocl.examples.xtext.tests.XtextTestCase;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.xtext.resource.XtextResource;
 
 /**
  * Test that an Ecore file can be loaded as OCLinEcore then saved back as Ecore.
  */
 public class RoundTripTests extends XtextTestCase
 {
 	public Resource createEcoreFromPivot(MetaModelManager metaModelManager, PivotResource pivotResource, URI ecoreURI) throws IOException {
 		Resource ecoreResource = Pivot2Ecore.createResource(metaModelManager, pivotResource, ecoreURI, null);
 		assertNoResourceErrors("To Ecore errors", ecoreResource);
 		if (ecoreURI != null) {
 			ecoreResource.save(null);
 		}
 		return ecoreResource;
 	}
 	public PivotResource createPivotFromEcore(MetaModelManager metaModelManager, Resource ecoreResource) throws IOException {
 		Ecore2Pivot ecore2Pivot = Ecore2Pivot.getAdapter(ecoreResource, metaModelManager);
 		org.eclipse.ocl.examples.pivot.Package pivotRoot = ecore2Pivot.getPivotRoot();
 		PivotResource pivotResource = (PivotResource) pivotRoot.eResource();
 		assertNoResourceErrors("Ecore2Pivot failed", pivotResource);
 		assertNoValidationErrors("Ecore2Pivot invalid", pivotResource);
 		return pivotResource;
 	}
 	public PivotResource createPivotFromXtext(MetaModelManager metaModelManager, BaseCSResource xtextResource) throws IOException {
 		CS2PivotResourceAdapter adapter = null;
 		try {
 			adapter = CS2PivotResourceAdapter.getAdapter(xtextResource, null);
 			PivotResource pivotResource = (PivotResource)adapter.getPivotResource(xtextResource);
 			assertNoResourceErrors("To Pivot errors", xtextResource);
 			assertNoUnresolvedProxies("Unresolved proxies", xtextResource);
 			List<EObject> pivotContents = pivotResource.getContents();
 			assertEquals(1, pivotContents.size());
 			assertNoValidationErrors("Pivot validation errors", pivotContents.get(0));
 			return pivotResource;
 		}
 		finally {
 			if (adapter != null) {
 				adapter.dispose();
 			}
 		}
 	}
 	public BaseCSResource createXtextFromPivot(MetaModelManager metaModelManager, PivotResource pivotResource, URI xtextURI) throws IOException {
 		XtextResource xtextResource = (XtextResource) resourceSet.createResource(xtextURI, OCLinEcoreCSTPackage.eCONTENT_TYPE);
 		Map<Resource, Resource> cs2PivotResourceMap = new HashMap<Resource, Resource>();
 		cs2PivotResourceMap.put(xtextResource, pivotResource);
 		Pivot2CS pivot2cs = new OCLinEcorePivot2CS(cs2PivotResourceMap, metaModelManager);
 		pivot2cs.update();
 		xtextResource.save(null);
 		assertNoResourceErrors("Conversion failed", xtextResource);
 		assertNoDiagnosticErrors("Concrete Syntax validation failed", xtextResource);
 		return (BaseCSResource) xtextResource;
 	}
 	public BaseCSResource createXtextFromURI(MetaModelManager metaModelManager, URI xtextURI) throws IOException {
 		ResourceSet resourceSet2 = metaModelManager.getExternalResourceSet();
 		BaseCSResource xtextResource = (BaseCSResource) resourceSet2.getResource(xtextURI, true);
 		assertNoResourceErrors("Load failed", xtextResource);
 		return xtextResource;
 	}
 	public void doRoundTripFromEcore(String stem) throws IOException, InterruptedException {
 		doRoundTripFromEcore(stem, stem);
 	}
 	public void doRoundTripFromEcore(String stem, String reference) throws IOException, InterruptedException {
 		String inputName = stem + ".ecore";
 		String pivotName = stem + ".ecore.pivot";
 		String outputName = stem + ".regenerated.ecore";
 		String referenceName = reference + ".ecore";
 		URI inputURI = getProjectFileURI(inputName);
 		URI pivotURI = getProjectFileURI(pivotName);
 		URI outputURI = getProjectFileURI(outputName);
 		URI referenceURI = getProjectFileURI(referenceName);
 		Resource inputResource = resourceSet.getResource(inputURI, true);
 		assertNoResourceErrors("Ecore load", inputResource);
 		assertNoValidationErrors("Ecore load", inputResource);
 		
 		MetaModelManager metaModelManager = new MetaModelManager();
 		try {
 			Ecore2Pivot ecore2Pivot = Ecore2Pivot.getAdapter(inputResource, metaModelManager);
 			org.eclipse.ocl.examples.pivot.Package pivotRoot = ecore2Pivot.getPivotRoot();
 			Resource pivotResource = pivotRoot.eResource();
 			pivotResource.setURI(pivotURI);
 			assertNoResourceErrors("Ecore2Pivot failed", pivotResource);
 			pivotResource.save(null);
 			assertNoValidationErrors("Ecore2Pivot invalid", pivotResource);
 			
 			Resource outputResource = Pivot2Ecore.createResource(metaModelManager, pivotResource, outputURI, null);
 			assertNoResourceErrors("Ecore2Pivot failed", outputResource);
 			outputResource.save(null);
 			assertNoValidationErrors("Ecore2Pivot invalid", outputResource);
 			
 	//		RootPackageCS csDocument = null; // FIXME Ecore2OCLinEcore.importFromEcore(resourceSet, null, leftResource);
 	//		assertNoResourceErrors("From Ecore errors", csDocument.eResource());
 	//		List<PackageCS> csObjects = new ArrayList<PackageCS>();
 	//		csObjects.addAll(csDocument.getPackages());
 	//		Resource middleResource = resourceSet.createResource(middleURI);
 	//		middleResource.getContents().addAll(csObjects);
 	//		middleResource.getContents().add(csDocument);
 	//		middleResource.save(null);
 	//		OCLinEcore2Ecore cs2e = new OCLinEcore2Ecore(resourceSet, middleResource, outputURI);
 	//		Resource rightResource = cs2e.exportToEcore();
 	//		assertNoResourceErrors("To Ecore errors", rightResource);
 	//		rightResource.save(null);
 	//		resourceSet.getResources().add(rightResource);
 			Resource referenceResource = new ResourceSetImpl().getResource(referenceURI, true);
 			assertSameModel(referenceResource, outputResource);
 		} finally {
 			metaModelManager.dispose();
 		}
 	}
 	
 	public void doRoundTripFromOCLinEcore(MetaModelManager metaModelManager1, String stem) throws IOException, InterruptedException {
 		String inputName = stem + ".oclinecore";
 		String ecoreName = stem + ".ecore";
 		String outputName = stem + ".regenerated.oclinecore";
 		URI inputURI = getProjectFileURI(inputName);
 		URI ecoreURI = getProjectFileURI(ecoreName);
 		URI outputURI = getProjectFileURI(outputName);
 
 		MetaModelManagerResourceSetAdapter.getAdapter(resourceSet, metaModelManager1);
 		BaseCSResource xtextResource1 = createXtextFromURI(metaModelManager1, inputURI);
 		PivotResource pivotResource1 = createPivotFromXtext(metaModelManager1, xtextResource1);
 		Resource ecoreResource = createEcoreFromPivot(metaModelManager1, pivotResource1, ecoreURI);
 		MetaModelManager metaModelManager2 = new MetaModelManager();
 		PivotResource pivotResource2 = createPivotFromEcore(metaModelManager2, ecoreResource);
 		@SuppressWarnings("unused")
 		BaseCSResource xtextResource2 = createXtextFromPivot(metaModelManager2, pivotResource2, outputURI);
 		MetaModelManager metaModelManager3 = new MetaModelManager();
 		BaseCSResource xtextResource3 = createXtextFromURI(metaModelManager3, outputURI);
 		PivotResource pivotResource3 = createPivotFromXtext(metaModelManager3, xtextResource3);
 		Map<String,Object> options = new HashMap<String,Object>();
 		options.put(MatchOptions.OPTION_IGNORE_ID, Boolean.TRUE);
 		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, Boolean.TRUE);
 		((NamedElement)pivotResource3.getContents().get(0)).setName(((NamedElement)pivotResource1.getContents().get(0)).getName());
     	assertSameModel(pivotResource1, pivotResource3, options);
 		metaModelManager1.dispose();
 	}
 	
 	public void doRoundTripFromUml(String stem) throws IOException, InterruptedException {
 //		Environment.Registry.INSTANCE.registerEnvironment(
 //			new UMLEnvironmentFactory().createEnvironment());
 		ResourceSet resourceSet = new ResourceSetImpl();
 		assertNull(UML2Pivot.initialize(resourceSet));
 		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
 //		assertNull(org.eclipse.ocl.uml.OCL.initialize(null));		
 //		org.eclipse.uml2.uml.Package umlMetamodel = (org.eclipse.uml2.uml.Package) resourceSet.getResource(
 //			URI.createURI(UMLResource.UML_METAMODEL_URI),
 //			true).getContents().get(0);
 //		org.eclipse.uml2.uml.Package umlPrimitiveTypes = (org.eclipse.uml2.uml.Package) resourceSet.getResource(
 //			URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI),
 //			true).getContents().get(0);
 //		org.eclipse.uml2.uml.Package ecorePrimitiveTypes = (org.eclipse.uml2.uml.Package) resourceSet.getResource(
 //			URI.createURI(UMLResource.ECORE_PRIMITIVE_TYPES_LIBRARY_URI),
 //			true).getContents().get(0);
 		String inputName = stem + ".uml";
 		String pivotName = stem + ".pivot";
 		String outputName = stem + ".regenerated.uml";
 		URI inputURI = getProjectFileURI(inputName);
 		URI pivotURI = getProjectFileURI(pivotName);
 		URI outputURI = getProjectFileURI(outputName);
 		Resource inputResource = resourceSet.getResource(inputURI, true);
 		assertNoResourceErrors("UML load", inputResource);
 		assertNoValidationErrors("UML load", inputResource);
 		
 		MetaModelManager pivotManager = new MetaModelManager();
 		UML2Pivot uml2Pivot = UML2Pivot.getAdapter(inputResource, pivotManager);
 		org.eclipse.ocl.examples.pivot.Package pivotRoot = uml2Pivot.getPivotRoot();
 		Resource pivotResource = pivotRoot.eResource();
 		pivotResource.setURI(pivotURI);
 		assertNoResourceErrors("UML2Pivot failed", pivotResource);
 		pivotResource.save(null);
 		assertNoValidationErrors("UML2Pivot invalid", pivotResource);
 		
 		List<? extends EObject> outputObjects = new ArrayList<EObject>(Pivot2UML.createResource(pivotManager, pivotResource));
 		@SuppressWarnings("unchecked")
 		List<? extends org.eclipse.uml2.uml.NamedElement> castOutputObjects = (List<? extends org.eclipse.uml2.uml.NamedElement>)outputObjects;
 		outputObjects.remove(getNamedElement(castOutputObjects, "orphanage"));
 		if (outputObjects.size() == 1) {
 			outputObjects = ((org.eclipse.uml2.uml.Package)outputObjects.get(0)).getNestedPackages();
 		}
 		Resource outputResource = resourceSet.createResource(outputURI);
 		outputResource.getContents().addAll(outputObjects);
 		assertNoResourceErrors("UML2Pivot failed", outputResource);
 		outputResource.save(null);
 		assertNoValidationErrors("UML2Pivot invalid", outputResource);
 		assertSameModel(inputResource, outputResource);
 	}
 
 	public static <T extends org.eclipse.uml2.uml.NamedElement> T getNamedElement(Collection<T> elements, String name) {
 		if (elements == null)
 			return null;
 		for (T element : elements)
 			if (ClassUtils.equals(name, element.getName()))
 				return element;
 		return null;				
 	}
 
 	public void testBug350894RoundTrip() throws IOException, InterruptedException {
 		String testFileA = 
 				"package a : aa = 'aaa'\n" +
 				"{\n" +
 				"class A;\n" +
 				"}\n";
 		createEcoreFile(new MetaModelManager(), "Bug350894A.ecore", testFileA);
 		String testFileB = 
 				"import aa : 'Bug350894A.ecore#/';\n" +
 				"package b : bb = 'bbb'\n" +
 				"{\n" +
 				"class B\n" +
 				"{\n" +
 				"invariant : not oclIsKindOf(aa::A);\n" +
 				"}\n" +
 				"}\n";
 		createOCLinEcoreFile("Bug350894B.oclinecore", testFileB);
 		doRoundTripFromOCLinEcore(new MetaModelManager(), "Bug350894B");
 	}
 
 	public void testCompanyRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("Company", "Company.reference");
 	}
 
 	public void testEcoreRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("Ecore");
 	}
 
 	public void testEmptyRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("Empty");
 	}
 
 	public void testImportsRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("Imports");
 	}
 
 	public void testKeysRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("Keys");
 	}
 
 	public void testOCLinEcoreCSTRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("OCLinEcoreCST");
 	}
 
 	public void testOCLstdlibRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("OCLstdlib");
 	}
 
 	public void testOCLRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("OCL", "OCL.reference"); 
 	}
 
 	public void testOCLCSTRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("OCLCST");
 	}
 
 	public void testOCLEcoreRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("OCLEcore");
	}
 
 	public void testQVTRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("QVT");
 	}	
 
 	public void testTypes_ecore() throws IOException, InterruptedException {
 		doRoundTripFromEcore("Types");
 	}
 
 //	public void testTypes_oclinecore() throws IOException, InterruptedException {
 //		BaseScopeProvider.LOOKUP.setState(true);		// FIXME CCE if this line commented out
 //		doRoundTripFromOclInEcore("Types");
 //	}
 
 	public void testXMLNamespaceRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("XMLNamespace");
 	}	
 
 	public void testXMLTypeRoundTrip() throws IOException, InterruptedException {
 		doRoundTripFromEcore("XMLType");
 	}
 
 //	public void testMy_uml() throws IOException, InterruptedException {
 //		doRoundTripFromUml("My");
 //	}
 
 //	public void testTriangle_uml() throws IOException, InterruptedException {
 //		doRoundTripFromUml("Triangle");
 //	}
 
 //	public void testProfile_less_Ecore_metamodel_uml() throws IOException, InterruptedException {
 //		doRoundTripFromUml("Profile-less-Ecore.metamodel");
 //	}
 }
