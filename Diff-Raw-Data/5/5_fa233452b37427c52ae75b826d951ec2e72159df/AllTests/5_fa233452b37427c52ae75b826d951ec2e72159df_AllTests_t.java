 /*******************************************************************************
  * Copyright (c) 2006, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.tests.suite;
 
 import java.util.Arrays;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.eclipse.emf.compare.tests.merge.AllMergeTests;
 import org.eclipse.emf.compare.tests.unit.core.CoreTestSuite;
 import org.eclipse.emf.compare.tests.unit.diff.DiffTestSuite;
import org.eclipse.emf.compare.tests.unit.diff.UMLHistoryDiff;
import org.eclipse.emf.compare.tests.unit.diff.UMLHistoryDiffWithResource;
 import org.eclipse.emf.compare.tests.unit.match.MatchTestSuite;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 
 /**
  * Launches all the JUnit tests for EMF compare.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 @SuppressWarnings("nls")
 public class AllTests extends TestCase implements IApplication {
 	/**
 	 * Launches the test with the given arguments.
 	 * 
 	 * @param args
 	 *            Arguments of the testCase.
 	 */
 	public static void main(String[] args) {
 		TestRunner.run(suite());
 	}
 
 	/**
 	 * Creates the {@link junit.framework.TestSuite TestSuite} for all the test.
 	 * 
 	 * @return The testsuite containing all the tests
 	 */
 	public static Test suite() {
 		final TestSuite suite = new TestSuite("EMF Compare test suite");
 		suite.addTest(CoreTestSuite.suite());
 		suite.addTest(MatchTestSuite.suite());
 		suite.addTest(AllMergeTests.suite());
		suite.addTestSuite(UMLHistoryDiff.class);
		suite.addTestSuite(UMLHistoryDiffWithResource.class);
 		// This will be null if memory setting is too low
 		final Test diffSuite = DiffTestSuite.suite();
 		if (diffSuite != null)
 			suite.addTest(diffSuite);
 		// Non regression
 		// suite.addTestSuite(TestNonRegressionModels.class);
 		return suite;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
 	 */
 	public Object start(IApplicationContext context) throws Exception {
 		TestRunner.run(suite());
 		return Arrays.asList(new String[] {"Please see raw test suite output for details." }); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.equinox.app.IApplication#stop()
 	 */
 	public void stop() {
 		// implements org.eclipse.equinox.app.IApplication#stop(). No action.
 	}
 }
