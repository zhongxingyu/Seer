 /*
  * Copyright (c) 2005, 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.gmf.runtime.emf.type.core.internal.EMFTypePlugin;
 import org.eclipse.gmf.tests.gef.CompartmentPropertiesTest;
 import org.eclipse.gmf.tests.gef.DiagramEditorTest;
 import org.eclipse.gmf.tests.gef.DiagramNodeTest;
 import org.eclipse.gmf.tests.gef.ParsersTest;
 import org.eclipse.gmf.tests.gef.ParsersTest.ParsersSetup;
 import org.eclipse.gmf.tests.gen.*;
 import org.eclipse.gmf.tests.migration.AllMigrationTests;
 import org.eclipse.gmf.tests.rt.AuditRulesTest;
 import org.eclipse.gmf.tests.rt.BundleActivationTest;
 import org.eclipse.gmf.tests.rt.EditHelpersTest;
 import org.eclipse.gmf.tests.rt.ElementInitializerTest;
 import org.eclipse.gmf.tests.rt.LinkChildMetaFeatureNotFromContainerTest;
 import org.eclipse.gmf.tests.rt.LinkCreationConstraintsTest;
 import org.eclipse.gmf.tests.rt.LinkCreationTest;
 import org.eclipse.gmf.tests.rt.LinkEcoreConstraintsTest;
 import org.eclipse.gmf.tests.rt.MetricRulesTest;
 import org.eclipse.gmf.tests.setup.LinksSessionSetup;
 import org.eclipse.gmf.tests.setup.SessionSetup;
 import org.eclipse.gmf.tests.setup.TestSetupTest;
 import org.eclipse.gmf.tests.setup.figures.FigureCodegenSetup;
 import org.eclipse.gmf.tests.setup.figures.FigureLayoutSetup;
 import org.eclipse.gmf.tests.setup.figures.LabelSupportSetup;
 import org.eclipse.gmf.tests.setup.figures.ShapePropertiesSetup;
 import org.eclipse.gmf.tests.tr.AuditRootTest;
 import org.eclipse.gmf.tests.tr.EcoreGenModelMatcherTest;
 import org.eclipse.gmf.tests.tr.GenModelGraphAnalyzerTest;
 import org.eclipse.gmf.tests.tr.GenModelTransformerBasicRTTest;
 import org.eclipse.gmf.tests.tr.GenModelTransformerSimpleTest;
 import org.eclipse.gmf.tests.tr.HistoryTest;
 import org.eclipse.gmf.tests.tr.LabelMappingTransformTest;
 import org.eclipse.gmf.tests.tr.ManifestMergeTest;
 import org.eclipse.gmf.tests.tr.NamingStrategyTest;
 import org.eclipse.gmf.tests.tr.PaletteTransformationTest;
 import org.eclipse.gmf.tests.tr.PluginXMLTextMergerTest;
 import org.eclipse.gmf.tests.tr.TestDefaultMergeService;
 import org.eclipse.gmf.tests.tr.TransformToGenModelOperationTest;
 import org.eclipse.gmf.tests.tr.XmlTextMergerTest;
 import org.eclipse.gmf.tests.validate.AllValidateTests;
 
 public class AllTests {
 
 	public static Test suite() throws Exception {
 		EMFTypePlugin.startDynamicAwareMode();
 		TestSuite suite = new TestSuite("Tests for org.eclipse.gmf, tooling side");
 		final SessionSetup sessionSetup = SessionSetup.newInstance();
 		final LinksSessionSetup sessionSetup2 = (LinksSessionSetup) LinksSessionSetup.newInstance();
 		
 		SessionSetup.disallowSingleTestCaseUse();
 
 		/*
 		 * [AS++] Temporary workaround: loading all the projects in the
 		 * beginning to get rid of the problems with runtime registries
 		 * reloading. In particular - ViewService.
 		 */
 		try {
 			sessionSetup.getGeneratedPlugin();
 			sessionSetup2.getGeneratedPlugin();
 			LinkChildMetaFeatureNotFromContainerTest.setup.getGeneratedPlugin();
 			LinkEcoreConstraintsTest.setup.getGeneratedPlugin();
 			EditHelpersTest.setup.getGeneratedPlugin();
 			BundleActivationTest.setup.getGeneratedPlugin();
 		} catch (final Exception e) {
 			suite.addTest(new TestCase("Session setup initialization problem") {
 				protected void runTest() throws Throwable {
 					e.printStackTrace();
 					fail(e.getMessage());
 				}
 			});
 			return suite;
 		}
 		/* [AS--] */
 
 		//$JUnit-BEGIN$
 		suite.addTestSuite(TestSetupTest.class); // first, check sources/setups we use for rest of the tests
 		suite.addTest(feed(HandcodedImplTest.class, sessionSetup)); // then, check handcoded implementations are in place
 		suite.addTestSuite(HandcodedGraphDefTest.class);
 		suite.addTestSuite(HandcodedPaletteTest.class);
 		suite.addTestSuite(HandcodedContributionItemTest.class);
 		suite.addTestSuite(HandcodedGMFMapItemProvidersTest.class);
 
 		suite.addTest(feed(GenModelTransformerSimpleTest.class, sessionSetup));
 		suite.addTest(feed(TransformToGenModelOperationTest.class, sessionSetup));
 		suite.addTest(feed(LabelMappingTransformTest.class, sessionSetup));
 		suite.addTest(feed(PaletteTransformationTest.class, sessionSetup));
 		suite.addTestSuite(AuditRootTest.class);
 		suite.addTestSuite(HistoryTest.class);
 		suite.addTestSuite(XmlTextMergerTest.class);
 		suite.addTestSuite(TestDefaultMergeService.class);
 		suite.addTestSuite(PluginXMLTextMergerTest.class);
 		suite.addTestSuite(ManifestMergeTest.class);
         suite.addTestSuite(OrganizeImportsPostprocessorTest.class);
 
 		suite.addTestSuite(EcoreGenModelMatcherTest.class);
 		suite.addTestSuite(ModelLoadHelperTest.class);		
 		suite.addTest(AllMigrationTests.suite());
 		suite.addTest(AllValidateTests.suite());
 
 		suite.addTest(feed(FigureCodegenTest.class, new FigureCodegenSetup()));
 		suite.addTest(feed(LabelSupportTest.class, new LabelSupportSetup()));
 		suite.addTest(feed(ShapePropertiesTest.class, new ShapePropertiesSetup()));
 		suite.addTest(feed(FigureLayoutTest.class, new FigureLayoutSetup()));
 		suite.addTestSuite(StandaloneMapModeTest.class);
 		suite.addTestSuite(StandalonePluginConverterTest.class);
 		suite.addTestSuite(RTFigureTest.class);
 		suite.addTestSuite(MapModeStrategyTest.class);
 		suite.addTestSuite(ViewmapProducersTest.class);
 		suite.addTestSuite(ToolDefHandocodedImplTest.class);
 		suite.addTest(feed(AuditHandcodedTest.class, sessionSetup));		
 		suite.addTest(feed(AuditRulesTest.class, sessionSetup2));		
 		suite.addTest(feed(ElementInitializerTest.class, sessionSetup2));
 		suite.addTest(feed(CodegenReconcileTest.class, sessionSetup));
 		// fires new runtime workbench initialization
 		suite.addTestSuite(RuntimeCompilationTest.class);
 		
 		suite.addTest(feed(DiagramNodeTest.class, sessionSetup));
 		suite.addTest(feed(CompartmentPropertiesTest.class, sessionSetup));
 		suite.addTest(feed(NamingStrategyTest.class, sessionSetup));
 		suite.addTest(feed(GenModelTransformerBasicRTTest.class, sessionSetup));
 		suite.addTest(feed(DiagramEditorTest.class, sessionSetup));
 		suite.addTestSuite(LinkChildMetaFeatureNotFromContainerTest.class);
 		suite.addTestSuite(LinkEcoreConstraintsTest.class);
 
 		suite.addTestSuite(BundleActivationTest.class);
 
 //		suite.addTestSuite(RunTimeModelTransformerTest.class); #113966
 //		suite.addTestSuite(PropertiesTest.class); #113965 
 //		suite.addTestSuite(CanvasTest.class); Nothing there yet
 //		suite.addTestSuite(SpecificRTPropertiesTest.class); #113965
 		
 		suite.addTest(feed(LinkCreationTest.class, sessionSetup));
 		suite.addTest(feed(LinkCreationConstraintsTest.class, sessionSetup2));
 		suite.addTest(feed(MetricRulesTest.class, sessionSetup2));		
 		suite.addTestSuite(GenFeatureSeqInitializerTest.class);
 		suite.addTestSuite(GenModelGraphAnalyzerTest.class);
 		suite.addTestSuite(EditHelpersTest.class);
 		suite.addTest(feed(ParsersTest.class, new ParsersSetup(false), "-direct"));
 		suite.addTest(feed(ParsersTest.class, new ParsersSetup(true), "-provider"));
 
 		//$JUnit-END$
 		suite.addTest(new CleanupTest("testCleanup") {
 			protected void performCleanup() throws Exception {
 				sessionSetup.cleanup();
 				sessionSetup2.cleanup();
 				LinkChildMetaFeatureNotFromContainerTest.setup.cleanup();
 				LinkEcoreConstraintsTest.setup.cleanup();
 				EditHelpersTest.setup.cleanup();
 			}
 		});
 		
 		return suite;
 	}
 
 	// should be in a better namespace than AllTests suite, though
 	public static Test feed(Class<?> theClass, TestConfiguration config) {
 		return feed(theClass, config, null);
 	}
 	public static Test feed(Class<?> theClass, TestConfiguration config, String suffix) {
 		TestSuite suite = new TestSuite(theClass, theClass.getSimpleName());
 		if (suffix != null) {
 			suite.setName(suite.getName() + suffix);
 		}
 		if (!NeedsSetup.class.isAssignableFrom(theClass)) {
 			return suite;
 		}
 		try {
 			Method m = null;
 			Class<?> configClass = config.getClass();
 			while (m == null && configClass != null) {
 				try {
 					m = theClass.getMethod(NeedsSetup.METHOD_NAME, new Class[] { configClass });
 				} catch (NoSuchMethodException ex) {
 					configClass = configClass.getSuperclass();
 				}
 			}
 			if (m == null) {
 				String methodInvocation = NeedsSetup.METHOD_NAME + "(" + config.getClass().getName() + " arg);";
 				return new ConfigurationFailedCase(theClass.getName() + " has no method compatible with " + methodInvocation);
 			}
 			final Object[] args = new Object[] { config };
 			for (Enumeration<?> en = suite.tests(); en.hasMoreElements(); ) {
 				Object nextTest = en.nextElement();
 				m.invoke(nextTest, args);
 			}
 		} catch (SecurityException ex) {
 			return new ConfigurationFailedCase(theClass.getName() + ": " + ex.getMessage());
 		} catch (IllegalAccessException ex) {
 			return new ConfigurationFailedCase(theClass.getName() + ": " + ex.getMessage());
 		} catch (InvocationTargetException ex) {
 			return new ConfigurationFailedCase(theClass.getName() + ": " + ex.getMessage());
 		}
 		return suite;
 	}
 
 	private static class ConfigurationFailedCase extends TestCase {
 		private final String cause; 
 		ConfigurationFailedCase(String aCause) {
 			super(ConfigurationFailedCase.class.getName());
 			cause = aCause;
 		}
 		protected void runTest() throws Throwable {
 			fail(cause);
 		}
 	}
 }
