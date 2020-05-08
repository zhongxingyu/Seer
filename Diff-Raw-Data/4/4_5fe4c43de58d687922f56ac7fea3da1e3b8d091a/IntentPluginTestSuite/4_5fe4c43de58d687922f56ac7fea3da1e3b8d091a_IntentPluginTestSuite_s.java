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
 package org.eclipse.mylyn.docs.intent.client.ui.test.suite;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.compare.ChangeEditorUpdateTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.compare.SimpleOrderTests;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.editor.CompletionTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.hyperlink.IntentHyperLinkDetetectorTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.java.JavaResourceFactoryTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.refresher.RefresherTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.repository.IntentURITest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.CompilerNotificationsTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.ExternalContentReferencesTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.IntentAbstractResourceTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.IntentDocumentationUpdateDoesNotCauseResolvingIssuesTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.IntentProjectReopeningTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.synchronizer.SynchronizerTest;
 
 /**
  * This suite will launch all tests requiring to launch an Intent runtime.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentPluginTestSuite extends TestCase {
 
 	/**
 	 * Launches the collaborative test suite.
 	 * 
 	 * @param args
 	 *            the arguments
 	 */
 	public static void main(String[] args) {
 		TestRunner.run(suite());
 	}
 
 	/**
 	 * Creates the {@link junit.framework.TestSuite TestSuite} for all Intent UI tests.
 	 * 
 	 * @return The test suite containing all intent ui tests
 	 */
 	public static Test suite() {
 		final TestSuite suite = new TestSuite("Intent Global TestSuite");
 		suite.addTest(comparisonSuite());
 
 		final TestSuite uiTestSuite = new TestSuite("Intent Plugin Tests");
 		uiTestSuite.addTest(uiBasicSuite());
 		uiTestSuite.addTest(uiScenarioSuite());
 		uiTestSuite.addTest(uiDemoSuite());
 		uiTestSuite.addTest(uiUpdateSuite());
 		suite.addTest(uiTestSuite);
 
 		suite.addTest(cdoSuite());
 		suite.addTest(bridgesSuite());
 		return suite;
 	}
 
 	/**
 	 * CDO related test Suite.
 	 * 
 	 * @return the suite
 	 */
 	private static TestSuite cdoSuite() {
 		final TestSuite cdoSuite = new TestSuite("CDO integration tests");
 		// cdoSuite.addTestSuite(CDOIntegrationTest.class); // TODO reactivate when build permgen fixed
 		return cdoSuite;
 	}
 
 	/**
 	 * Match & merge Tests.
 	 * 
 	 * @return the suite
 	 */
 	private static TestSuite comparisonSuite() {
 		final TestSuite compareSuite = new TestSuite("Intent match and merge tests");
 		// compareSuite.addTestSuite(IntentMatchEngineTests.class); // TODO reactivate when match stable
 		return compareSuite;
 	}
 
 	/**
 	 * Core tests: all tests that test a technical concern (emf compare behavior, project lifecycle...).
 	 * 
 	 * @return the suite
 	 */
 	private static TestSuite uiBasicSuite() {
 		final TestSuite basicTestSuite = new TestSuite("Technical tests");
 		basicTestSuite.addTestSuite(IntentURITest.class);
 		// TODO: reactivate this test once the IntentWorkspaceRepositoryStructurer will be modified to
 		// correctly split the Intent Document
 		// basicTestSuite.addTestSuite(IntentRepositoryStructurerTest.class);
		// basicTestSuite.addTestSuite(ProjectTest.class);
 		basicTestSuite.addTestSuite(RefresherTest.class);
 		basicTestSuite.addTestSuite(ChangeEditorUpdateTest.class);
 		basicTestSuite.addTestSuite(SimpleOrderTests.class);
 		basicTestSuite.addTestSuite(CompletionTest.class);
 		basicTestSuite.addTestSuite(SynchronizerTest.class);
 		return basicTestSuite;
 	}
 
 	/**
 	 * Scenario tests: all tests that test an identified scenario for the end-user (very simple use case).
 	 * 
 	 * @return the suite
 	 */
 	private static TestSuite uiScenarioSuite() {
 		final TestSuite scenarioSuite = new TestSuite("Simple End-User Scenarios");
 		scenarioSuite.addTestSuite(CompilerNotificationsTest.class);
 		scenarioSuite.addTestSuite(IntentAbstractResourceTest.class);
 		scenarioSuite.addTestSuite(IntentDocumentationUpdateDoesNotCauseResolvingIssuesTest.class);
 		scenarioSuite.addTestSuite(IntentProjectReopeningTest.class);
 		scenarioSuite.addTestSuite(ExternalContentReferencesTest.class);
 		scenarioSuite.addTestSuite(IntentHyperLinkDetetectorTest.class);
 		return scenarioSuite;
 	}
 
 	/**
 	 * Complete use case testSuite: all tests that ensures the behavior of complete use cases.
 	 * 
 	 * @return the suite
 	 */
 	private static TestSuite uiDemoSuite() {
 		final TestSuite demoSuite = new TestSuite("Intent Demo TestSuite");
 		// demoSuite.addTestSuite(OpenEditorTest.class); // FIXME synchronizer issue
 		// demoSuite.addTestSuite(CompileTest.class); // FIXME synchronizer issue
 		// demoSuite.addTestSuite(EcoreTest.class); // FIXME synchronizer issue
 		// demoSuite.addTestSuite(JavaTest.class); // FIXME synchronizer issue
 		return demoSuite;
 	}
 
 	/**
 	 * Modeling Units updates tests.
 	 * 
 	 * @return the suite
 	 */
 	private static TestSuite uiUpdateSuite() {
 		final TestSuite updatesSuite = new TestSuite("Modeling Unit update tests");
 		// updatesSuite.addTestSuite(QuickFixTest.class); // TODO reactivate when comparison match stable
 		// updatesSuite.addTestSuite(DragAndDropTest.class); // TODO reactivate when comparison match stable
 		return updatesSuite;
 	}
 
 	/**
 	 * Bridges-related tests.
 	 * 
 	 * @return the suite
 	 */
 	private static Test bridgesSuite() {
 		final TestSuite bridgesSuite = new TestSuite("Intent Bridges Tests");
 
 		// Java bridge tests
 		final TestSuite javaBridgeSuite = new TestSuite("Java Bridge Tests");
 		javaBridgeSuite.addTestSuite(JavaResourceFactoryTest.class);
 		bridgesSuite.addTest(javaBridgeSuite);
 
 		return bridgesSuite;
 	}
 }
