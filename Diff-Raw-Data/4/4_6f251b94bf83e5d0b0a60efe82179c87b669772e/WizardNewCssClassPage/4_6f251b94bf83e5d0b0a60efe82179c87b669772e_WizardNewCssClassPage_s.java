 /*******************************************************************************
  * Copyright (c) 2007-2008 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.web.ui.wizards.css;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.eclipse.ui.model.BaseWorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.FileExtensionFilter;
 import org.jboss.tools.jst.web.ui.wizards.css.NewCSSClassWizard.CSSClassDescription;
 import org.jboss.tools.jst.web.ui.wizards.messages.WebUIMessages;
 
 /**
  * @author Sergey Dzmitrovich
  * 
  */
 public class WizardNewCssClassPage extends WizardPage implements ModifyListener {
 
 	private final static String REQUIRED_FIELD_SIGN = "*"; //$NON-NLS-1$
 	public final static String CSS_FILE_EXTENSION = "css"; //$NON-NLS-1$
 	private CSSClassDescription classDescription;
 	private final static String[] fileExtensions = { CSS_FILE_EXTENSION };
 	private int numColumns = 3;
 	private Text selectFileText;
 	private Text classNameText;
 
 	/**
 	 * @param pageName
 	 */
 	public WizardNewCssClassPage(CSSClassDescription classDescription) {
 		super("WizardNewCssClassPage"); //$NON-NLS-1$
 		this.classDescription = classDescription;
 		setTitle(WebUIMessages.WIZARD_TITLE);
 		setDescription(WebUIMessages.WIZARD_DESCRIPTION);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
 	 * .Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = numColumns;
 		layout.makeColumnsEqualWidth = false;
 		container.setLayout(layout);
 
 		Label selectFileLabel = new Label(container, SWT.NONE);
 		selectFileLabel.setText(WebUIMessages.FILE_SELECT_LABEL
 				+ REQUIRED_FIELD_SIGN);
 
 		selectFileText = new Text(container, SWT.SINGLE | SWT.BORDER);
 		selectFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		selectFileText.setFont(parent.getFont());
		selectFileText.addModifyListener(this);
 
 		Button selectFileButton = new Button(container, SWT.NONE);
 		selectFileButton.setText(WebUIMessages.FILE_SELECT_BUTTON);
 		selectFileButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
 						getShell(), new WorkbenchLabelProvider(),
 						new BaseWorkbenchContentProvider());
 				dialog.addFilter(new FileExtensionFilter(fileExtensions));
 				dialog.setTitle(WebUIMessages.FILE_SELECT_DIALOG_TITLE);
 				dialog.setMessage(WebUIMessages.FILE_SELECT_DIALOG_MESSAGE);
 				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
 				dialog.setAllowMultiple(false);
 				dialog.setDoubleClickSelects(true);
 				dialog.setValidator(new ISelectionStatusValidator() {
 
 					public IStatus validate(Object[] selection) {
 						if (selection != null && selection.length == 1) {
 							if (selection[0] instanceof IFile) {
 								return new Status(IStatus.OK,
 										PlatformUI.PLUGIN_ID, IStatus.OK, "", //$NON-NLS-1$
 										null);
 							}
 						}
 						return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
 								IStatus.ERROR,
 								WebUIMessages.WIZARD_ERROR_FILE_SELECTION, 
 								null);
 					}
 				});
 				if (classDescription.getCssFile() != null) {
 					dialog.setInitialSelection(classDescription.getCssFile());
 				}
 				dialog
 						.setEmptyListMessage(WebUIMessages.FILE_SELECT_DIALOG_EMPTY_MESSAGE);
 
 				if (dialog.open() == Window.OK) {
 					classDescription.setCssFile((IResource) dialog
 							.getFirstResult());
 					selectFileText.setText(classDescription.getCssFile()
 							.getFullPath().toString());
 				}
 
 			}
 		});
 
 		Label classNameLabel = new Label(container, SWT.NONE);
 		classNameLabel.setText(WebUIMessages.CSS_CLASS_NAME_LABEL
 				+ REQUIRED_FIELD_SIGN);
 
 		classNameText = new Text(container, SWT.SINGLE | SWT.BORDER);
 		classNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		classNameText.setFont(parent.getFont());
 		classNameText.addModifyListener(this);
 
 		if (classDescription.getCssFile() != null)
 			selectFileText.setText(classDescription.getCssFile().getFullPath()
 					.toString());
 
 		setControl(container);
 	}
 
 	@Override
 	public boolean canFlipToNextPage() {
 		if ((classNameText.getText().length() != 0)
 				&& (getCssFile(selectFileText.getText()) != null)) {
 			return true;
 		}
 		return false;
 	}
 
 	public void modifyText(ModifyEvent e) {
 
 		classDescription.setCssClassName(classNameText.getText());
 		classDescription.setCssFile(getResource(selectFileText.getText()));
 		getContainer().updateButtons();
 
 	}
 
 	private IFile getCssFile(String path) {
 		IResource cssFile = getResource(path);
 		if ((cssFile != null)
 				&& (CSS_FILE_EXTENSION.equals(cssFile.getFileExtension()))) {
 			return (IFile) cssFile;
 		}
 		return null;
 	}
 
 	private IResource getResource(String path) {
 		IResource resource = null;
 		if (path != null) {
 			resource = ResourcesPlugin.getWorkspace().getRoot()
 					.findMember(path);
 		}
 		return resource;
 	}
 
 }
