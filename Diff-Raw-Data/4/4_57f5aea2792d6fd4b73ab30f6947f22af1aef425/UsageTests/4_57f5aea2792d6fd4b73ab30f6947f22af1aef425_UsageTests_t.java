 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.test.xtext;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.emf.codegen.ecore.generator.Generator;
 import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory;
 import org.eclipse.emf.codegen.ecore.genmodel.GenJDKLevel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
 import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
 import org.eclipse.emf.codegen.ecore.genmodel.generator.GenModelGeneratorAdapterFactory;
 import org.eclipse.emf.codegen.ecore.genmodel.util.GenModelUtil;
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.Monitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.mwe.core.ConfigurationException;
 import org.eclipse.ocl.examples.codegen.ecore.OCLGeneratorAdapterFactory;
 import org.eclipse.ocl.examples.domain.values.util.ValuesUtil;
 import org.eclipse.ocl.examples.pivot.library.StandardLibraryContribution;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.xtext.tests.XtextTestCase;
 import org.eclipse.xtext.diagnostics.ExceptionDiagnostic;
 
 /**
  * Tests that load a model and verify that there are no unresolved proxies as a result.
  */
 public class UsageTests extends XtextTestCase
 {	
 	private static Logger log = Logger.getLogger(UsageTests.class);	
 
 	/**
 	 * Checks all resources in a resource set for any errors or warnings.
 	 * @param resourceSet
 	 * @throws ConfigurationException if any error present
 	 */
 	public static void checkResourceSet(ResourceSet resourceSet) throws ConfigurationException {
 		int errorCount = 0;
 		for (Resource aResource : resourceSet.getResources()) {
 			List<Resource.Diagnostic> errors = aResource.getErrors();
 			if (errors.size() > 0) {
 				for (Resource.Diagnostic error : errors) {
 					if (error instanceof ExceptionDiagnostic) {
 						log.error("Error for '" + aResource.getURI() + "'", ((ExceptionDiagnostic)error).getException());
 					}
 					else {
 						log.error(error + " for '" + aResource.getURI() + "'");
 					}
 					errorCount++;
 				}
 			}
 			List<Resource.Diagnostic> warnings = aResource.getWarnings();
 			if (warnings.size() > 0) {
 				for (Resource.Diagnostic warning : warnings) {
 					if (warning instanceof ExceptionDiagnostic) {
 						log.warn("Warning for '" + aResource.getURI() + "'", ((ExceptionDiagnostic)warning).getException());
 					}
 					else {
 						log.warn(warning + " for '" + aResource.getURI() + "'");
 					}
 				}
 			}
 		}
 		if (errorCount > 0) {
 			throw new RuntimeException("Errors in ResourceSet");
 		}
 	}
 
 	protected MetaModelManager metaModelManager = null;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 //		AcceleoNature.class.getName();				// Pull in the plugin for Hudson
 		doOCLinEcoreSetup();
 		configurePlatformResources();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("pivot", new XMIResourceFactoryImpl()); //$NON-NLS-1$
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		if (metaModelManager != null) {
 			metaModelManager.dispose();
 			metaModelManager = null;
 		}
 		StandardLibraryContribution.REGISTRY.remove(MetaModelManager.DEFAULT_OCL_STDLIB_URI);
 		uninstall();
 		super.tearDown();
 	}
 	
 	public void createGenModelFile(String fileName, String fileContent) throws IOException {
 		File file = new File(getProjectFile(), fileName);
 		Writer writer = new FileWriter(file);
 		writer.append(fileContent);
 		writer.close();
 	}
 
 /*	protected void doLoadFromString(String fileName, String testFile) throws Exception {
 		URI libraryURI = getProjectFileURI(fileName);
 		MetaModelManager metaModelManager = new MetaModelManager();
 		ResourceSet resourceSet = new ResourceSetImpl();
 		MetaModelManagerResourceSetAdapter.getAdapter(resourceSet, metaModelManager);
 		BaseCSResource xtextResource = (BaseCSResource) resourceSet.createResource(libraryURI);
 		InputStream inputStream = new ByteArrayInputStream(testFile.getBytes());
 		xtextResource.load(inputStream, null);
 		assertNoResourceErrors("Load failed", xtextResource);
 		CS2PivotResourceAdapter adapter = CS2PivotResourceAdapter.getAdapter(xtextResource, metaModelManager);
 		Resource fileResource = adapter.getPivotResource(xtextResource);
 		assertNoResourceErrors("File Model", fileResource);
 		assertNoUnresolvedProxies("File Model", fileResource);
 		assertNoValidationErrors("File Model", fileResource);
 		adapter.dispose();
 		metaModelManager.dispose();
 		metaModelManager = null;
 		resourceSet = null;
 		adapter = null;
 		StandardLibraryContribution.REGISTRY.remove(MetaModelManager.DEFAULT_OCL_STDLIB_URI);
 	} */
 
 	@SuppressWarnings("null")
	public void testBug370824() throws Exception {
 		String testProjectName;
 		if (EMFPlugin.IS_ECLIPSE_RUNNING) {
 			suppressGitPrefixPopUp();
 			testProjectName = "Bug370824";
 	        IWorkspace workspace = ResourcesPlugin.getWorkspace();
 	        IProject project = workspace.getRoot().getProject(testProjectName);
 	        if (!project.exists()) {
 	        	project.create(null);
 	        }
 		}
 		else {
 			testProjectName = "org.eclipse.ocl.examples.xtext.tests";
 		}
 		metaModelManager = new MetaModelManager();
 		String oclinecoreFile =
 				"package bug370824 : bug370824 = 'http://bug370824'\n" +
 				"{\n" +
 				"    class Clase1\n" +
 				"    {\n" +
 				"        invariant : self.name.size() > 0;\n" +
 				"        attribute name : String[?] { ordered };\n" +
 				"    }\n" +
 				"}\n";
 		createEcoreFile(metaModelManager, "Bug370824", oclinecoreFile);
 		String genmodelFile =
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 				"<genmodel:GenModel xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n" +
 					"    xmlns:genmodel=\"http://www.eclipse.org/emf/2002/GenModel\" modelDirectory=\"/" + testProjectName + "/src-gen\" modelPluginID=\"Bug370824.bug370824\"\n" +
 						"    modelName=\"Bug370824\" importerID=\"org.eclipse.emf.importer.ecore\" complianceLevel=\"5.0\"\n" +
 						"    copyrightFields=\"false\">\n" +
 						"  <foreignModel>Bug370824.ecore</foreignModel>\n" +
 						"  <genPackages prefix=\"Bug370824\" disposableProviderFactory=\"true\" ecorePackage=\"Bug370824.ecore#/\">\n" +
 						"  </genPackages>\n" +
 						"</genmodel:GenModel>\n" +
 						"\n";
 		createGenModelFile("Bug370824.genmodel", genmodelFile);
 		GeneratorAdapterFactory.Descriptor.Registry.INSTANCE.addDescriptor(GenModelPackage.eNS_URI, OCLGeneratorAdapterFactory.DESCRIPTOR);
 		URI fileURI = getProjectFileURI("Bug370824.genmodel");
 //		System.out.println("Generating Ecore Model using '" + fileURI + "'");
 		metaModelManager.dispose();
 		metaModelManager = new MetaModelManager();
 		ResourceSet resourceSet = metaModelManager.getExternalResourceSet();
 		resourceSet.getPackageRegistry().put(GenModelPackage.eNS_URI, GenModelPackage.eINSTANCE);
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("genmodel", new EcoreResourceFactoryImpl());
 		GeneratorAdapterFactory.Descriptor.Registry.INSTANCE.addDescriptor
 	     (GenModelPackage.eNS_URI, GenModelGeneratorAdapterFactory.DESCRIPTOR);
 		GeneratorAdapterFactory.Descriptor.Registry.INSTANCE.addDescriptor(GenModelPackage.eNS_URI, OCLGeneratorAdapterFactory.DESCRIPTOR);
 		if (resourceSet instanceof ResourceSetImpl) {
 			ResourceSetImpl resourceSetImpl = (ResourceSetImpl) resourceSet;
 			Map<URI, Resource> uriResourceMap = resourceSetImpl.getURIResourceMap();
 			if (uriResourceMap != null) {
 				uriResourceMap.clear();
 			}
 		}
 		resourceSet.getResources().clear();
 		Resource resource = resourceSet.getResource(fileURI, true);
 		// EcoreUtil.resolveAll(resourceSet); -- genModel can fail if
 		// proxies resolved here
 		// problem arises if genmodel has an obsolete feature for a feature
 		// moved up the inheritance hierarchy
 		// since the proxy seems to be successfully resolved giving a double
 		// feature
 		checkResourceSet(resourceSet);
 		EObject eObject = resource.getContents().get(0);
 		if (!(eObject instanceof GenModel)) {
 			throw new ConfigurationException("No GenModel found in '"
 					+ resource.getURI() + "'");
 		}
 		GenModel genModel = (GenModel) eObject;
 		genModel.reconcile();
 		checkResourceSet(resourceSet);
 		// genModel.setCanGenerate(true);
 		// validate();
 
 		
 		
 		genModel.setValidateModel(true); // The more checks the better
 //		genModel.setCodeFormatting(true); // Normalize layout
 		genModel.setForceOverwrite(false); // Don't overwrite read-only
 											// files
 		genModel.setCanGenerate(true);
 		// genModel.setFacadeHelperClass(null); // Non-null gives JDT
 		// default NPEs
 //		genModel.setFacadeHelperClass(StandaloneASTFacadeHelper.class.getName()); // Bug 308069
 		// genModel.setValidateModel(true);
 		genModel.setBundleManifest(false); // New manifests should be
 											// generated manually
 		genModel.setUpdateClasspath(false); // New class-paths should be
 											// generated manually
 		genModel.setComplianceLevel(GenJDKLevel.JDK50_LITERAL);
 		// genModel.setRootExtendsClass("org.eclipse.emf.ecore.impl.MinimalEObjectImpl$Container");
 		Diagnostic diagnostic = genModel.diagnose();
 		if (diagnostic.getSeverity() != Diagnostic.OK) {
 			fail(diagnostic.toString());
 		}
 
 		/*
 		 * JavaModelManager.getJavaModelManager().initializePreferences();
 		 * new
 		 * JavaCorePreferenceInitializer().initializeDefaultPreferences();
 		 * 
 		 * GenJDKLevel genSDKcomplianceLevel =
 		 * genModel.getComplianceLevel(); String complianceLevel =
 		 * JavaCore.VERSION_1_5; switch (genSDKcomplianceLevel) { case
 		 * JDK60_LITERAL: complianceLevel = JavaCore.VERSION_1_6; case
 		 * JDK14_LITERAL: complianceLevel = JavaCore.VERSION_1_4; default:
 		 * complianceLevel = JavaCore.VERSION_1_5; } // Hashtable<?,?>
 		 * defaultOptions = JavaCore.getDefaultOptions(); //
 		 * JavaCore.setComplianceOptions(complianceLevel, defaultOptions);
 		 * // JavaCore.setOptions(defaultOptions);
 		 */
 
 		Generator generator = GenModelUtil.createGenerator(genModel);
 		Monitor monitor = new BasicMonitor();
 		diagnostic = generator.generate(genModel,
 				GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE, monitor);
 		if (diagnostic.getSeverity() != Diagnostic.OK) {
 			fail(diagnostic.toString());
 		}
 	}
 	
 	public void testInitStatics() {
 		assertTrue(ValuesUtil.initAllStatics());
 		assertFalse(ValuesUtil.initAllStatics());
 	}
 
 /*	public void testType_Parameters() throws Exception {
 		String testFile =
 				"import ecore : 'http://www.eclipse.org/emf/2002/Ecore#/';\n" +
 				"package C1 : C2 = 'C3'\n" +
 				"{\n" +
 				"    class A {\n" +
 				"    	operation opEBigInteger(arg : ecore::EBigInteger) : Boolean {\n" +
 				"	 	}\n" +
 				"	 }\n" +
 				"    class Test {\n" +
 				"       property a : A;\n" +
 				"       invariant EBigInteger: a.opEBigInteger(1);\n" +
 				"    }\n" +
 				"}\n";
 		doLoadFromString("Type_Parameters.oclinecore", testFile);
 	} */
 }
