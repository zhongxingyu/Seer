 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.web.ui.wizards.css;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.dialogs.IPageChangedListener;
 import org.eclipse.jface.dialogs.PageChangedEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWizard;
 import org.jboss.tools.jst.jsp.outline.cssdialog.CSSClassDialog;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.MessageDialogEvent;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.MessageDialogListener;
 import org.jboss.tools.jst.web.ui.wizards.messages.WebUIMessages;
 
 /**
  * New CSS class wizard.
  */
 public class NewCSSClassWizard extends Wizard implements INewWizard {
 
 	private CSSClassDescription classDescription = new  CSSClassDescription();
 
 	// workbench selection when the wizard was started
 	protected IStructuredSelection selection;
 	// the workbench instance
 	protected IWorkbench workbench;
 
 	// wizard contains only one page
 	private NewCSSClassWizardPage editFilePage;
 
 	private WizardNewCssClassPage selectFilePage;
 
 	/**
 	 * Constructor for SampleNewWizard.
 	 */
 	public NewCSSClassWizard() {
 		super();
 		setWindowTitle(WebUIMessages.WIZARD_WINDOW_TITLE);
 	}
 
 	
 	/**
 	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 	}
 
 	/**
 	 * Adding the page to the wizard.
 	 */
 	@Override
 	public void addPages() {
 
 		selectFilePage = new WizardNewCssClassPage(classDescription);
 		editFilePage = new NewCSSClassWizardPage();
 		addPage(selectFilePage);
 		addPage(editFilePage);
 	}
 
 	/**
 	 * This method is called when 'Finish' button is pressed in the wizard. We
 	 * will create an operation and run it using wizard as execution context.
 	 */
 	@Override
 	public boolean performFinish() {
 		editFilePage.saveChanges();
 
 		return true;
 	}
 
 	/**
 	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
 	 */
 	@Override
 	public boolean canFinish() {
 		return selectFilePage.canFlipToNextPage()
 				&& getContainer().getCurrentPage() == editFilePage
 				&& editFilePage.canFinish;
 	}
 
 	/**
 	 * @see org.eclipse.jface.wizard.IWizard#performCancel()
 	 */
 	@Override
 	public boolean performCancel() {
 		editFilePage.cancel();
 		return true;
 	}
 	
 	public class CSSClassDescription{
 		private IFile cssFile;
 		private String cssClassName;
 		public IFile getCssFile() {
 			return cssFile;
 		}
 		public void setCssFile(IFile cssFile) {
 			this.cssFile = cssFile;
 		}
 		public String getCssClassName() {
 			return cssClassName;
 		}
 		public void setCssClassName(String cssClassName) {
 			this.cssClassName = cssClassName;
 		}
 	}
 
 	/**
 	 * Class representing the first page of the wizard.
 	 */
 	private class NewCSSClassWizardPage extends WizardPage {
 
 		private CSSClassDialog dialog;
 
 		boolean canFinish = false;
 
 		/**
 		 * Constructor for SampleNewWizardPage.
 		 * 
 		 * @param pageName
 		 */
 		public NewCSSClassWizardPage() {
 			super("newCSSClassWizard");
 			setTitle(WebUIMessages.WIZARD_TITLE);
 			setDescription(WebUIMessages.WIZARD_DESCRIPTION);
 			
 		}
 
 		/**
 		 * @see IDialogPage#createControl(Composite)
 		 */
 		public void createControl(Composite parent) {
 			Composite container = new Composite(parent, SWT.NONE);
 			GridLayout layout = new GridLayout();
 			container.setLayout(layout);
 
 			// Initialize CSS dialog that is integrated to CSS wizard.
 			// Also it can be used separately without integration to wizard
 			// component.
 			dialog = new CSSClassDialog(getShell(), selection, true);
 			dialog.addMessageDialogListener(new MessageDialogListener() {
 				public void throwMessage(MessageDialogEvent event) {
 					if (event != null) {
 						IStatus status = event.getOperationStatus();
 						if (status != null) {
 							applyToStatusLine(status);
 						}
						if(getWizard().getContainer().getCurrentPage()!=null) {
							getWizard().getContainer().updateButtons();
						}
 					}
 				}
 			});
 
 			dialog.createDialog(container);
 			setControl(container);
 			
 			if (getWizard().getContainer() instanceof WizardDialog){
 				WizardDialog wd = (WizardDialog) getWizard().getContainer();
 				wd.addPageChangedListener(new IPageChangedListener(){
 
 					//set console configuration as treeViewer input
 					public void pageChanged(PageChangedEvent event) {
 						if (event.getSelectedPage() == editFilePage){
 							dialog.setCurrentFile(classDescription.getCssFile());
 							dialog.reinit();
 							dialog.addNewStyleClass(classDescription.cssClassName);
 						}
 					}});
 			}
 
 		}
 
 		/**
 		 * Save page model.
 		 */
 		public void saveChanges() {
 //			dialog.saveChanges(true);
 			dialog.close();
 		}
 
 		/**
 		 * Handle cancel operation correctly.
 		 */
 		public void cancel() {
 			dialog.closeDialog();
 		}
 
 		/**
 		 * Applies the status to the status line of a dialog page.
 		 */
 		private void applyToStatusLine(IStatus status) {
 			String message = status.getMessage();
 			if (message.length() == 0) {
 				message = null;
 			}
 			switch (status.getSeverity()) {
 			case IStatus.OK:
 				setErrorMessage(null);
 				setMessage(message);
 				canFinish = true;
 				break;
 			case IStatus.WARNING:
 				setErrorMessage(null);
 				canFinish = true;
 				setMessage(message, WizardPage.WARNING);
 				break;
 			case IStatus.INFO:
 				setErrorMessage(null);
 				canFinish = true;
 				setMessage(message, WizardPage.INFORMATION);
 				break;
 			default:
 				setErrorMessage(message);
 				setMessage(null);
 				canFinish = false;
 				break;
 			}
 		}
 	}
 
 }
