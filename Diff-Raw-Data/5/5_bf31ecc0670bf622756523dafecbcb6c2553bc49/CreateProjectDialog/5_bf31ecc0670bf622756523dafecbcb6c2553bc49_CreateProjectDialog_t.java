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
 package org.eclipse.emf.ecp.emfstorebridge.actions;
 
import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.ecp.common.util.DialogHandler;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.layout.LayoutConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Create project dialog.
  * 
  * @author shterev
  */
 public class CreateProjectDialog extends TitleAreaDialog {
 
 	private Text txtProjectName;
 	private Text txtProjectDesc;
 	private Usersession session;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param parent the parent shell
 	 * @param session the target usersession
 	 */
 	public CreateProjectDialog(Shell parent, Usersession session) {
 		super(parent);
 		this.session = session;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite contents = new Composite(parent, SWT.NONE);
 		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		setTitle("Create new project");
 		setMessage("Enter the name and the description of the project");
 
 		Label name = new Label(contents, SWT.NULL);
 		name.setText("Name:");
 		txtProjectName = new Text(contents, SWT.SINGLE | SWT.BORDER);
 		txtProjectName.setSize(150, 20);
 
 		Label desc = new Label(contents, SWT.NULL);
 		desc.setText("Description:");
 		txtProjectDesc = new Text(contents, SWT.MULTI | SWT.BORDER);
 		txtProjectDesc.setSize(150, 60);
 
 		Point defaultMargins = LayoutConstants.getMargins();
 		GridLayoutFactory.fillDefaults().numColumns(2).margins(defaultMargins.x, defaultMargins.y)
 			.generateLayout(contents);
 
 		return contents;
 	}
 
 	private ProjectSpace projectSpace;
 
 	/**
 	 * @return the projectSpace
 	 */
 	public ProjectSpace getProjectSpace() {
 		return projectSpace;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void okPressed() {
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				try {
 
 					if (session != null) {
						WorkspaceManager.getInstance().getCurrentWorkspace().createRemoteProject(session, txtProjectName.getText(), txtProjectDesc.getText(),
							new NullProgressMonitor());
 					} else {
 						projectSpace = WorkspaceManager.getInstance().getCurrentWorkspace()
 							.createLocalProject(txtProjectName.getText(), txtProjectDesc.getText());
 					}
 
 				} catch (AccessControlException e) {
 					DialogHandler.showExceptionDialog(e);
 				} catch (EmfStoreException e) {
 					DialogHandler.showExceptionDialog(e);
 				}
 			}
 
 		}.run();
 		close();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void cancelPressed() {
 		close();
 	}
 
 }
