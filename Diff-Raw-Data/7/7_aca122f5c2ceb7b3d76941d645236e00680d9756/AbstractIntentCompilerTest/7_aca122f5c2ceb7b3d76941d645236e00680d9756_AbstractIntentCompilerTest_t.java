 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.compiler.test.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.Assert;
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.core.runtime.ILogListener;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.mylyn.docs.intent.client.compiler.ModelingUnitCompiler;
 import org.eclipse.mylyn.docs.intent.client.compiler.generator.modellinking.ModelingUnitLinkResolver;
 import org.eclipse.mylyn.docs.intent.client.compiler.utils.IntentCompilerInformationHolder;
 import org.eclipse.mylyn.docs.intent.compare.utils.EMFCompareUtils;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocumentPackage;
 import org.eclipse.mylyn.docs.intent.core.genericunit.GenericUnitPackage;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitPackage;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceDeclaration;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ModelingUnitParser;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ModelingUnitParserImpl;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ParseException;
 import org.junit.After;
 import org.junit.Before;
 
 /**
  * An abstract test class providing API for manage an Intent IDE projects and editors.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public abstract class AbstractIntentCompilerTest implements ILogListener {
 
 	/**
 	 * The parser.
 	 */
 	private static ModelingUnitParser modelingUnitParser = new ModelingUnitParserImpl();
 
 	/**
 	 * The resource set.
 	 */
 	private static ResourceSet resourceSet = new ResourceSetImpl();
 
 	/**
 	 * The last computed compilation status.
 	 */
 	private List<CompilationStatus> statusToCheck = new ArrayList<CompilationStatus>();
 
 	/**
 	 * Constructor.
 	 */
 	public AbstractIntentCompilerTest() {
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put("*", new XMIResourceFactoryImpl());
 
 		resourceSet.getPackageRegistry().put(ModelingUnitPackage.eNS_PREFIX, ModelingUnitPackage.eINSTANCE);
 		resourceSet.getPackageRegistry().put(GenericUnitPackage.eNS_PREFIX, GenericUnitPackage.eINSTANCE);
 		resourceSet.getPackageRegistry().put(IntentDocumentPackage.eNS_PREFIX, GenericUnitPackage.eINSTANCE);
 		resourceSet.getPackageRegistry().put(GenModelPackage.eNS_PREFIX, GenModelPackage.eINSTANCE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Before
 	public void setUp() throws Exception {
 		statusToCheck.clear();
 		resourceSet.getResources().clear();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@After
 	public void tearDown() throws Exception {
 		if (!statusToCheck.isEmpty()) {
 			StringBuilder builder = new StringBuilder();
 			for (CompilationStatus status : statusToCheck) {
 				builder.append(status.getSeverity().toString() + ' ' + status.getMessage() + "\n");
 			}
 			throw new AssertionFailedError("There are unchecked status :\n" + builder.toString());
 		}
 	}
 
 	/**
 	 * Parses a modeling unit.
 	 * 
 	 * @param testName
 	 *            the modeling unit file
 	 * @return the modeling unit
 	 */
 	private ModelingUnit parse(String testName) {
 		try {
 			EObject res = modelingUnitParser.parseFile(testName);
 			if (res instanceof ModelingUnit) {
 				return (ModelingUnit)res;
 			}
 		} catch (ParseException e) {
 			throw new AssertionFailedError(e.getMessage());
 		} catch (IOException e) {
 			throw new AssertionFailedError(e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * Compiles the given test.
 	 * 
 	 * @param testName
 	 *            the test
 	 */
 	protected void compile(String testName) {
 		EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
 		IntentCompilerInformationHolder informationHolder = IntentCompilerInformationHolder.getInstance();
 		ModelingUnit modelingUnit = parse(testName);
 		Resource modelingUnitResource = resourceSet.createResource(URI.createURI(testName + ".xmi"));
 		modelingUnitResource.getContents().add(modelingUnit);
 
 		// compilation
 		ModelingUnitLinkResolver linkResolver = new ModelingUnitLinkResolver(packageRegistry,
 				informationHolder);
 		ModelingUnitCompiler compiler = new ModelingUnitCompiler(packageRegistry, linkResolver,
 				informationHolder, BasicMonitor.toMonitor(new NullProgressMonitor()));
 		compiler.compile(Collections.singletonList(modelingUnit));
 
 		// output models checking
 		for (ResourceDeclaration resource : informationHolder.getDeclaredResources()) {
 			Resource generatedResource = resourceSet.createResource(URI.createURI(testName + '.'
 					+ resource.getName() + ".xmi"));
 			generatedResource.getContents().addAll(informationHolder.getResourceContent(resource));
 
 			URI expectedURI = URI.createURI(generatedResource.getURI().toString()
 					.replace("dataTests", "expectedResults"));
 
 			if (!new File(expectedURI.toFileString()).exists()) {
 				System.out.println(expectedURI + " not found, initialized.");
 				Resource expectedResource = resourceSet.createResource(expectedURI);
 				expectedResource.getContents().addAll(generatedResource.getContents());
 				try {
 					expectedResource.save(null);
 				} catch (IOException e) {
 					throw new AssertionFailedError(e.getMessage());
 				}
 			} else {
 				Resource expected = resourceSet.getResource(expectedURI, true);
				Comparison comparison = EMFCompareUtils.compare(expected, generatedResource);
				Assert.assertTrue("There are differences between expected and actual", comparison
						.getDifferences().isEmpty());
 			}
 		}
 
 		statusToCheck.addAll(IntentCompilerInformationHolder.getInstance().getCompilationStatusList());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus, java.lang.String)
 	 */
 	public void logging(IStatus status, String plugin) {
 		if (status.getSeverity() == IStatus.ERROR) {
 			throw new AssertionFailedError(status.getMessage());
 		}
 	}
 
 	/**
 	 * Checks whether the given compilation status exists.
 	 * 
 	 * @param severity
 	 *            the status severity
 	 * @param message
 	 *            the status message
 	 */
 	protected void checkCompilationStatus(CompilationStatusSeverity severity, String message) {
 		List<String> candidates = new ArrayList<String>();
 		CompilationStatus found = null;
 		for (CompilationStatus compilationStatus : statusToCheck) {
 			if (compilationStatus.getSeverity().equals(severity)) {
 				candidates.add(compilationStatus.getMessage());
 				if (compilationStatus.getMessage().equals(message)) {
 					found = compilationStatus;
 				}
 			}
 		}
 		if (found != null) {
 			statusToCheck.remove(found);
 			return;
 		}
 		if (candidates.size() == 1) {
 			Assert.assertEquals(message, candidates.get(0));
 		} else {
 			if (!candidates.isEmpty()) {
 				StringBuilder builder = new StringBuilder();
 				for (String candidate : candidates) {
 					builder.append(candidate + "\n");
 				}
 				System.out.println("candidates :\n" + builder.toString());
 			}
 			throw new AssertionFailedError("Status not found : " + severity + ' ' + message);
 		}
 	}
 }
