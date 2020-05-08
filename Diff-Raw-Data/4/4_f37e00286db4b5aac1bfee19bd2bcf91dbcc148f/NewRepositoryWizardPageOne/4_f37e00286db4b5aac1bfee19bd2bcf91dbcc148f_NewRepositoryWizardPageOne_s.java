 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.views;
 
 import java.util.ArrayList;
 
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.client.model.exceptions.CertificateStoreException;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * The main page of the wizard.
  * 
  * @author shterev
  */
 public class NewRepositoryWizardPageOne extends WizardPage {
 
 	private static final int DEFAULT_PORT = 8080;
 	private Text name;
 	private Text url;
 	private Spinner port;
 	private Combo cert;
 
 	/**
 	 * Default constructor.
 	 */
 	public NewRepositoryWizardPageOne() {
 		super("Main");
 		setTitle("Server Details");
 		setDescription("Select the details for the new repository");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void createControl(Composite parent) {
 		NewRepositoryWizard wizard = (NewRepositoryWizard) getWizard();
 		ServerInfo serverInfo = wizard.getServerInfo();
 
 		GridData gd;
 		Composite composite = new Composite(parent, SWT.NONE);
 
 		GridLayout gl = new GridLayout();
 		int ncol = 3;
 		gl.numColumns = ncol;
 		composite.setLayout(gl);
 
 		// Server Name
 		new Label(composite, SWT.NONE).setText("Name:");
 		name = new Text(composite, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = ncol - 1;
 		name.setLayoutData(gd);
 
 		// Server URL
 		new Label(composite, SWT.NONE).setText("URL:");
 		url = new Text(composite, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = ncol - 1;
 		url.setLayoutData(gd);
 
 		// Server Port
 		new Label(composite, SWT.NONE).setText("Port:");
 		port = new Spinner(composite, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = ncol - 1;
 		port.setLayoutData(gd);
 		port.setValues(DEFAULT_PORT, 1, 999999, 0, 1, 10);
 		setControl(composite);
 
 		// Certificate
 		new Label(composite, SWT.NONE).setText("Certificate:");
 		cert = new Combo(composite, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = ncol - 2;
 		cert.setLayoutData(gd);
 		cert.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 		initCombo();
 
 		// Choose Certificate, Opens Dialogue
 		Button button = new Button(composite, SWT.NONE);
 		button.setText("Edit");
 		button.addSelectionListener(new SelectionDialogListener());
 
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		gd.horizontalSpan = 1;
 		button.setLayoutData(gd);
 
 		if (serverInfo.getUrl() != null) {
 			name.setText(serverInfo.getName());
 			url.setText(serverInfo.getUrl());
 			port.setSelection(serverInfo.getPort());
 			if (serverInfo.getCertificateAlias() == null) {
 				return;
 			}
 
 			try {
 				if (KeyStoreManager.getInstance().contains(serverInfo.getCertificateAlias())) {
 					for (int i = 0; i < cert.getItemCount(); i++) {
						if (!cert.getItem(i).equals(serverInfo.getCertificateAlias())) {
 							cert.select(i);
 							break;
 						}
 					}
 				} else {
 					cert.setText("");
 				}
 			} catch (CertificateStoreException e1) {
 				cert.setText("");
 			}
 		}
 	}
 
 	private void initCombo() {
 		try {
 			ArrayList<String> certificates = KeyStoreManager.getInstance().getCertificates();
 			String[] aliases = new String[certificates.size()];
 			for (int i = 0; i < certificates.size(); i++) {
 				aliases[i] = certificates.get(i);
 			}
 			cert.setItems(aliases);

 		} catch (CertificateStoreException e) {
 			WorkspaceUtil.logException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * @return if the input on the current page is valid.
 	 */
 	@Override
 	public boolean canFlipToNextPage() {
 		if (getErrorMessage() != null) {
 			return false;
 		}
 		if (isTextNonEmpty(name.getText()) && isTextNonEmpty(url.getText()) && isComboNotEmpty()) {
 			saveDataToModel();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Saves the uses choices from this page to the model. Called on exit of the
 	 * page
 	 */
 	private void saveDataToModel() {
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				NewRepositoryWizard wizard = (NewRepositoryWizard) getWizard();
 				ServerInfo serverInfo = wizard.getServerInfo();
 				serverInfo.setName(name.getText());
 				serverInfo.setUrl(url.getText());
 				serverInfo.setPort(port.getSelection());
 				serverInfo.setCertificateAlias(cert.getText());
 			}
 		}.run();
 	}
 
 	/**
 	 * Returns true if Text is not empty, i.e. not null/only space characters.
 	 * 
 	 * @param t
 	 * @return boolean
 	 */
 	private static boolean isTextNonEmpty(String s) {
 		if ((s != null) && (s.trim().length() > 0)) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isComboNotEmpty() {
 		String s = cert.getItem(cert.getSelectionIndex());
 		return isTextNonEmpty(s);
 	}
 
 	/**
 	 * Listener for the selection dialog.
 	 * 
 	 * @author pfeifferc
 	 */
 	class SelectionDialogListener implements SelectionListener {
 
 		/**
 		 * @param e
 		 *            selection event
 		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent e) {
 			// nothing to do
 		}
 
 		/**
 		 * @param e
 		 *            selection event
 		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			CertificateSelectionDialog csd = new CertificateSelectionDialog(Display.getCurrent().getActiveShell(),
 				new LabelProvider() {
 					@Override
 					public String getText(Object element) {
 						if (element instanceof String) {
 							return element.toString();
 						} else {
 							return "";
 						}
 					}
 				});
 			ArrayList<String> certificates;
 			try {
 				certificates = KeyStoreManager.getInstance().getCertificates();
 				csd.setElements(certificates.toArray());
 			} catch (CertificateStoreException e1) {
 				csd.setErrorMessage(e1.getMessage());
 			}
 			csd.setBlockOnOpen(true);
 			csd.setTitle("Certificate Selection Dialog");
 			csd.open();
 			if (csd.getReturnCode() == Window.OK) {
 				initCombo();
 				for (int i = 0; i < cert.getItemCount(); i++) {
 					String item = cert.getItem(i);
 					if (item.equals(csd.getCertificateAlias())) {
 						cert.select(i);
 						break;
 					}
 				}
 			}
 		}
 	}
 }
