 /*******************************************************************************
  * Copyright (c) 2007 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.test;
 
 import org.jboss.tools.common.model.ui.test.ModelNewWizardTest;
 import org.jboss.tools.common.model.ui.wizard.newfile.NewHTMLFileWizard;
 import org.jboss.tools.common.model.ui.wizard.newfile.NewJSPFileWizard;
 import org.jboss.tools.common.model.ui.wizard.newfile.NewPropertiesFileWizard;
 import org.jboss.tools.common.model.ui.wizard.newfile.NewXHTMLFileWizard;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewCSSFileWizard;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewJSFileWizard;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewTLDFileWizard;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewWebFileWizard;
 
 /**
  * @author eskimo
  *
  */
 public class WebWizardsTest extends ModelNewWizardTest {
 	
 	public void testNewCssWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewCSSFileWizard.class.getName());
 	}
 	
 	public void testNewJsWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewJSFileWizard.class.getName());
 	}
 	
 	public void testNewWebWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewWebFileWizard.class.getName());
 	}
 	
 	public void testNewJspWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewJSPFileWizard.class.getName());
 	}
 	
 	public void testNewXhtmlWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewXHTMLFileWizard.class.getName());
 	}
 	
 	public void testNewHtmlWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewHTMLFileWizard.class.getName());
 	}
 	
 	public void testNewPropertiesWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewPropertiesFileWizard.class.getName());
 	}
 	
 	public void testNewTldWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewTLDFileWizard.class.getName());
 	}
 }
