 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.css.test;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.jboss.tools.jst.css.test.jbide.CSSStyleDialogTest;
 import org.jboss.tools.jst.css.test.jbide.CaseSensitiveTest_JBIDE4940;
 import org.jboss.tools.jst.css.test.jbide.CssClassNewWizardTest;
 import org.jboss.tools.jst.css.test.jbide.ExtendingCSSViewTest_JBIDE4850;
 import org.jboss.tools.jst.css.test.jbide.IncorrectPageAfterSelectionTest_JBIDE4849;
 import org.jboss.tools.jst.css.test.jbide.InputFractionalValueTest_JBIDE4790;
 import org.jboss.tools.jst.css.test.jbide.NotCompletedCSS_JBIDE4677;
 import org.jboss.tools.jst.css.test.jbide.SelectionLosingByPropertySheet_JBIDE4791;
 import org.jboss.tools.test.util.ProjectImportTestSetup;
 
 /**
  * @author Sergey Dzmitrovich
  * 
  *         To import other projects add names of imported projects to array the
  *         same way as AbstractCSSViewTest.IMPORT_PROJECT_NAME
  */
 public class CSSAllTests {
 
 	public static final String RESOURCE_PATH = "resources/"; //$NON-NLS-1$
 
 	public static Test suite() {
 
 		TestSuite suite = new TestSuite("Tests for CSS views"); //$NON-NLS-1$
 		// $JUnit-BEGIN$
 		suite.addTestSuite(CSSViewTest.class);
 		suite.addTestSuite(InputFractionalValueTest_JBIDE4790.class);
		suite.addTestSuite(SelectionLosingByPropertySheet_JBIDE4791.class);
 		suite.addTestSuite(ExtendingCSSViewTest_JBIDE4850.class);
 		suite.addTestSuite(NotCompletedCSS_JBIDE4677.class);
 		suite.addTestSuite(IncorrectPageAfterSelectionTest_JBIDE4849.class);
 		suite.addTestSuite(CaseSensitiveTest_JBIDE4940.class);
 		suite.addTestSuite(CSSStyleDialogTest.class);
 		suite.addTestSuite(CssClassNewWizardTest.class);
 		// $JUnit-END$
 
 		return new ProjectImportTestSetup(
 				suite,
 				CSSTestPlugin.PLUGIN_ID,
 				new String[] { RESOURCE_PATH + CSSViewTest.IMPORT_PROJECT_NAME },
 				new String[] { AbstractCSSViewTest.IMPORT_PROJECT_NAME });
 	}
 }
