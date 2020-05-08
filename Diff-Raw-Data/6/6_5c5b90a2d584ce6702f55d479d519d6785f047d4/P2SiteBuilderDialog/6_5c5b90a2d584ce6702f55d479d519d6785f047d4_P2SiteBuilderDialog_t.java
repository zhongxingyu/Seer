 /*******************************************************************************
  * Copyright (c) 2011 Matthew Piggott. All rights reserved. This
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Matthew Piggott - initial API and implementation
  *******************************************************************************/
 package ca.piggott.p2.site.webview.ui;
 
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.eclipse.equinox.p2.operations.RepositoryTracker;
 import org.eclipse.equinox.p2.ui.ProvisioningUI;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 import ca.piggott.p2.site.webview.P2SiteBuilder;
 
 public class P2SiteBuilderDialog extends TitleAreaDialog implements ModifyListener, IStructuredContentProvider, ILabelProvider {
 	private Text text;
 	private ComboViewer comboViewer;
 	/**
 	 * Create the dialog.
 	 * @param parentShell
 	 */
 	public P2SiteBuilderDialog(Shell parentShell) {
 		super(parentShell);
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 * @param parent
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		setTitle("p2 Site Builder");
 		this.getShell().setText("p2 Site Builder");
 		Composite area = (Composite) super.createDialogArea(parent);
 		Composite container = new Composite(area, SWT.NONE);
 		container.setLayout(new GridLayout(3, false));
 		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		Label lblRepository = new Label(container, SWT.NONE);
 		lblRepository.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		lblRepository.setText("Repository:");
 		
 		comboViewer = new ComboViewer(container, SWT.NONE);
 		Combo combo = comboViewer.getCombo();
 		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 		comboViewer.setContentProvider(this);
 		comboViewer.setLabelProvider(this);
 		comboViewer.setInput(ProvisioningUI.getDefaultUI().getRepositoryTracker());
 		Label lblDestination = new Label(container, SWT.NONE);
 		lblDestination.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		lblDestination.setText("Destination");
 		
 		text = new Text(container, SWT.BORDER);
 		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		text.addModifyListener(this);
 
 		Button btnBrowse = new Button(container, SWT.NONE);
 		btnBrowse.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog fDialog = new FileDialog(getShell(), SWT.SAVE);
 				if (text.getText().length() > 0) {
 					fDialog.setFileName(text.getText());
 				}
 				fDialog.setOverwrite(true);
 				fDialog.setFilterExtensions(new String[]{"*.html", "*.htm"});
 				
 				String selection = fDialog.open();
 				if (selection != null) {
 					text.setText(selection);
 				}
 			}
 		});
 		btnBrowse.setText("Browse");
 		new Label(container, SWT.NONE);
 		new Label(container, SWT.NONE);
 
 		return area;
 	}
 
 	/**
 	 * Create contents of the button bar.
 	 * @param parent
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
 				true);
 		createButton(parent, IDialogConstants.CANCEL_ID,
 				IDialogConstants.CANCEL_LABEL, false);
 	}
 
 	protected void okPressed() {
 		try {
			P2SiteBuilder.writeMainPage(ProvisioningUI.getDefaultUI().loadMetadataRepository((URI) ((IStructuredSelection)comboViewer.getSelection()).getFirstElement(), false, new NullProgressMonitor()), new File(text.getText()));
 			super.okPressed();
 		} catch (ProvisionException e) {
 			this.setMessage(e.getStatus().getMessage(), IMessageProvider.ERROR);
 			StatusManager.getManager().handle(e.getStatus(), StatusManager.LOG);
 		} catch (FileNotFoundException e) {
 			this.setMessage("Unable to write to file", IMessageProvider.ERROR);
 			StatusManager.getManager().handle(new Status(IStatus.ERROR, "ca.piggott.p2.site.webview.ui", "Failed to write to file: " + text.getText(), e), StatusManager.LOG);
 		} catch (IOException e) {
 			this.setMessage("Error writing to file", IMessageProvider.ERROR);
 			StatusManager.getManager().handle(new Status(IStatus.ERROR, "ca.piggott.p2.site.webview.ui", "Failed to write to file: " + text.getText(), e), StatusManager.LOG);
 		}
 	}
 
 	/**
 	 * Return the initial size of the dialog.
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(450, 300);
 	}
 
 	public void modifyText(ModifyEvent e) {
 		this.getButton(IDialogConstants.OK_ID).setEnabled(text.getText().length() > 0 /* TODO Repo selection */);
 	}
 
 	public Object[] getElements(Object inputElement) {
 		if (inputElement instanceof RepositoryTracker) {
 			RepositoryTracker tracker = (RepositoryTracker) inputElement;
 			return  tracker.getKnownRepositories(ProvisioningUI.getDefaultUI().getSession());
 		}
 		return new Object[0];
 	}
 
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		
 	}
 
 	public void addListener(ILabelProviderListener listener) {
 		
 	}
 
 	public boolean isLabelProperty(Object element, String property) {
 		return false;
 	}
 
 	public void removeListener(ILabelProviderListener listener) {
 	}
 
 	public Image getImage(Object element) {
 		return null;
 	}
 
 	public String getText(Object element) {
 		return element.toString();
 	}
 }
