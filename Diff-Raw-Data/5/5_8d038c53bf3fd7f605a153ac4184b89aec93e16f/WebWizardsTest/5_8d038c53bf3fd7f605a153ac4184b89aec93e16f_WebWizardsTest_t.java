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
 
 
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.IWorkbenchWizard;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.jboss.tools.common.model.ui.test.AbstractModelNewWizardTest;
 //import org.jboss.tools.common.model.ui.wizard.newfile.NewHTMLFileWizard;
 //import org.jboss.tools.common.model.ui.wizard.newfile.NewJSPFileWizard;
 import org.jboss.tools.common.model.ui.wizard.newfile.NewPropertiesFileWizard;
 import org.jboss.tools.common.model.ui.wizards.standard.DefaultStandardStep;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewCSSFileWizard;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewJSFileWizard;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewTLDFileWizard;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewWebFileWizard;
 import org.jboss.tools.jst.web.ui.wizards.newfile.NewXHTMLWizard;
 import org.jboss.tools.test.util.WorkbenchUtils;
 
 /**
  * @author eskimo
  *
  */
 public class WebWizardsTest extends AbstractModelNewWizardTest {
 	
 	public void _testNewCssWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewCSSFileWizard.class.getName());
 	}
 	
 	public void _testNewJsWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewJSFileWizard.class.getName());
 	}
 	
 	public void testNewWebWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewWebFileWizard.class.getName());
 	}
 	
 //	public void _testNewJspWizardInstanceIsCreated() {
 //		testNewWizardInstanceIsCreated(NewJSPFileWizard.class.getName());
 //	}
 	/*
 	 *	rewritten by Maksim Areshkau, as fix for
 	 * https://jira.jboss.org/jira/browse/JBIDE-6216,
 	 * https://jira.jboss.org/jira/browse/JBIDE-6190 
 	 */
 	public void testNewXhtmlWizardInstanceIsCreated() {
 		/*
 		 * commented by Maksim Areshkau, 
 		 * because in this method not called init for wizard
 		 */
 		//testNewWizardInstanceIsCreated(NewXHTMLWizard.class.getName());
 		IWorkbenchWizard 
 		aWizard = (IWorkbenchWizard) WorkbenchUtils.findWizardByDefId(
 				NewXHTMLWizard.class.getName());
 		WizardDialog dialog = new WizardDialog(
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 				aWizard);
 		try {
 			/*
 			 * here we show view to get initialized selection
 			 */
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.jdt.ui.PackageExplorer"); //$NON-NLS-1$
 		} catch (PartInitException e) {
 			fail(e.toString());
 		}
 	    ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
 		ISelection eclipseSelection = selectionService.getSelection();
 		aWizard.init(PlatformUI.getWorkbench(), (IStructuredSelection) eclipseSelection);
 	    dialog.setBlockOnOpen(false);
 		try {
 			dialog.open();
 			IWizardPage htmlWizardNewFileCreationPage = dialog.getCurrentPage();
 			assertEquals("The Page should be","HTMLWizardNewFileCreationPage", htmlWizardNewFileCreationPage.getName());  //$NON-NLS-1$//$NON-NLS-2$
 			IWizardPage newXHTMLTemplatesWizardPage=htmlWizardNewFileCreationPage.getNextPage();
 			assertEquals("The Page should be","NewXHTMLTemplatesWizardPage", newXHTMLTemplatesWizardPage.getName());  //$NON-NLS-1$//$NON-NLS-2$
			if (newXHTMLTemplatesWizardPage.canFlipToNextPage()) {
				IWizardPage newXHTMLTagLibrariesWizardPage = newXHTMLTemplatesWizardPage.getNextPage();
				assertTrue("Start page is not loaded", newXHTMLTagLibrariesWizardPage instanceof DefaultStandardStep); //$NON-NLS-1$
			}
 		} finally {
 			dialog.close();
 		}
 	}
 	
 //	public void _testNewHtmlWizardInstanceIsCreated() {
 //		testNewWizardInstanceIsCreated(NewHTMLFileWizard.class.getName());
 //	}
 	
 	public void testNewPropertiesWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewPropertiesFileWizard.class.getName());
 	}
 	
 	public void testNewTldWizardInstanceIsCreated() {
 		testNewWizardInstanceIsCreated(NewTLDFileWizard.class.getName());
 	}
 }
