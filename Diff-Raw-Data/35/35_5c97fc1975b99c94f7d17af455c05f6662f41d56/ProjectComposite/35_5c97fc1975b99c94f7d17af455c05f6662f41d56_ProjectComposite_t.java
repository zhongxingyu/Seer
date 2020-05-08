 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Hodaie
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.views.emfstorebrowser.dialogs.admin;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.internal.client.model.AdminBroker;
 import org.eclipse.emf.emfstore.internal.client.ui.dialogs.EMFStoreMessageDialog;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACOrgUnit;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.roles.Role;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.roles.RolesPackage;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * This is shows the properties of a project (name, ...) and a list of its participants. The user can change the role of
  * a participant in TableViewer using a ComboboxCellEditor.
  * 
  * @author Hodaie
  */
 public class ProjectComposite extends PropertiesComposite {
 
 	private static final int READER_ROLE = 0;
 	private static final int WRITER_ROLE = 1;
 	private static final int PROJECT_ADMIN_ROLE = 2;
 	private static final int SERVER_ADMIN_ROLE = 3;
 
 	// Set column names
	private final String[] roleNames = new String[] { Messages.ProjectComposite_Reader, Messages.ProjectComposite_Writer, Messages.ProjectComposite_ProjectAdmin, Messages.ProjectComposite_ServerAdmin };
 
 	private Label lblVersion;
 	private Text txtVersion;
 
 	private ProjectInfo projectInfo;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parent parent
 	 * @param style style
 	 * @param adminBroker used to communicate with server.
 	 */
 	public ProjectComposite(Composite parent, int style, AdminBroker adminBroker) {
 		super(parent, style, adminBroker);
 		createControls();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void removeOrgUnit(ACOrgUnit orgUnit) {
 		try {
 			getAdminBroker().removeParticipant(projectInfo.getProjectId(), orgUnit.getId());
 		} catch (final ESException e) {
 			EMFStoreMessageDialog.showExceptionDialog(e);
 		}
 		getTableViewer().refresh();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void addExistingOrgUnit(ACOrgUnit participant) {
 		try {
 			if (participant != null) {
 				getAdminBroker().addParticipant(projectInfo.getProjectId(), participant.getId(),
 					RolesPackage.eINSTANCE.getReaderRole());
 			}
 		} catch (final ESException e) {
 			EMFStoreMessageDialog.showExceptionDialog(e);
 		}
 		getTableViewer().refresh();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void addNewOrgUnit() {
 		try {
 			final EList<ACOrgUnit> participants = getParticipants();
 			for (final ACOrgUnit orgUnit : participants) {
 
 				getAdminBroker().addParticipant(projectInfo.getProjectId(), orgUnit.getId(),
 					RolesPackage.eINSTANCE.getReaderRole());
 
 			}
 
 		} catch (final ESException e) {
 			EMFStoreMessageDialog.showExceptionDialog(e);
 		}
 		getTableViewer().refresh();
 	}
 
 	/**
 	 * This will be used when adding a new participant using add button. 1. show a list of all AcOrgUnits that do not
 	 * participate in this project (get list of all AcOrgUnits, remove those who take part in this Project) 2. return
 	 * the selected participant
 	 * 
 	 * @return
 	 */
 	private EList<ACOrgUnit> getParticipants() {
 
 		final Collection<ACOrgUnit> allOrgUnits = new BasicEList<ACOrgUnit>();
 		final EList<ACOrgUnit> participants = new BasicEList<ACOrgUnit>();
 
 		try {
 
 			allOrgUnits.addAll(getAdminBroker().getOrgUnits());
 
 			if (projectInfo != null) {
 				allOrgUnits.removeAll(getAdminBroker().getParticipants(projectInfo.getProjectId()));
 			}
 
			final Object[] result = showDialog(allOrgUnits, Messages.ProjectComposite_Select_Participant);
 
 			for (int i = 0; i < result.length; i++) {
 				if (result[i] instanceof ACOrgUnit) {
 					participants.add((ACOrgUnit) result[i]);
 				}
 			}
 		} catch (final ESException e) {
 			EMFStoreMessageDialog.showExceptionDialog(e);
 		}
 
 		return participants;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.views.emfstorebrowser.dialogs.admin.PropertiesComposite#getTabTitle()
 	 */
 	@Override
 	protected String getTabTitle() {
		return Messages.ProjectComposite_Participants;
 	}
 
 	/**
 	 * This adds role column to table viewer. Using role column user can see and change role of a participant.
 	 * 
 	 * @param parent parent
 	 */
 	@Override
 	protected void createTableViewer(Composite parent) {
 
 		super.createTableViewer(parent);
 
 		final TableViewerColumn roleColumnViewer = new TableViewerColumn(getTableViewer(), SWT.NONE);
		roleColumnViewer.getColumn().setText(Messages.ProjectComposite_Role);
 		roleColumnViewer.getColumn().setWidth(120);
 		roleColumnViewer.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				final ACOrgUnit orgUnit = (ACOrgUnit) element;
 				final int roleIndex = getCurrentRoleIndex(orgUnit);
 				return roleNames[roleIndex];
 			}
 
 		});
 		roleColumnViewer.setEditingSupport(new RoleEditingSupport(getTableViewer()));
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void addDragNDropSupport() {
 		// add drag support
 		super.addDragNDropSupport();
 
 		// add drop support
 		final int ops = DND.DROP_COPY;
 		final Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };
 		final DropTargetListener dropListener = new DropTargetAdapter() {
 			@Override
 			public void dragEnter(DropTargetEvent event) {
				if (PropertiesForm.getDragSource().equals(Messages.ProjectComposite_Projects)) {
 					event.detail = DND.DROP_NONE;
 
 				} else {
 					event.detail = DND.DROP_COPY;
 				}
 			}
 
 			@Override
 			public void drop(DropTargetEvent event) {
 				if (PropertiesForm.getDragNDropObject() != null) {
 					if (PropertiesForm.getDragNDropObject() instanceof ACOrgUnit) {
 						final ACOrgUnit orgUnit = (ACOrgUnit) PropertiesForm.getDragNDropObject();
 						addExistingOrgUnit(orgUnit);
 						PropertiesForm.setDragNDropObject(null);
 						getTableViewer().refresh();
 					}
 				}
 			}
 
 		};
 		getTableViewer().addDropSupport(ops, transfers, dropListener);
 	}
 
 	/**
 	 * This changes the role of an OrgUnit for current project. This will be called form RoleEditinSupport.setValue().
 	 * 
 	 * @param orgUnit orgUnit
 	 * @param role new role
 	 */
 	public void changeRole(ACOrgUnit orgUnit, int role) {
 		try {
 			switch (role) {
 			case READER_ROLE:
 				getAdminBroker().changeRole(projectInfo.getProjectId(), orgUnit.getId(),
 					RolesPackage.eINSTANCE.getReaderRole());
 
 				break;
 
 			case WRITER_ROLE:
 				getAdminBroker().changeRole(projectInfo.getProjectId(), orgUnit.getId(),
 					RolesPackage.eINSTANCE.getWriterRole());
 				break;
 
 			case PROJECT_ADMIN_ROLE:
 				getAdminBroker().changeRole(projectInfo.getProjectId(), orgUnit.getId(),
 					RolesPackage.eINSTANCE.getProjectAdminRole());
 				break;
 
 			case SERVER_ADMIN_ROLE:
 				getAdminBroker().changeRole(projectInfo.getProjectId(), orgUnit.getId(),
 					RolesPackage.eINSTANCE.getServerAdmin());
 				break;
 			default:
 				break;
 
 			}
 		} catch (final ESException e) {
 			EMFStoreMessageDialog.showExceptionDialog(e);
 		}
 		getTableViewer().refresh();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void createSimpleAttributes() {
 
 		super.createSimpleAttributes();
 
 		getTxtName().setEnabled(false);
 		getTxtDescription().setEnabled(false);
 
 		lblVersion = new Label(getAttributesGroup(), SWT.NONE);
		lblVersion.setText(Messages.ProjectComposite_Version);
 		txtVersion = new Text(getAttributesGroup(), SWT.BORDER);
 		txtVersion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		txtVersion.setEnabled(false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
	 * @param input
	 *            must be a {@link ProjectInfo} instance
 	 */
 	@Override
 	public void updateControls(EObject input) {
 
 		if (input instanceof ProjectInfo) {
 			projectInfo = (ProjectInfo) input;
 
 			getTxtName().setText(projectInfo.getName());
 			getTxtDescription().setText(projectInfo.getDescription());
 			txtVersion.setText(String.valueOf(projectInfo.getVersion().getIdentifier()));
 			getTableViewer().setInput(projectInfo);
 		}
 
 	}
 
 	/**
 	 * This returns an integer representing the actual role of an OrgUnit.
 	 * 
 	 * @param orgUnit orgUnit
 	 * @return integer
 	 */
 	public int getCurrentRoleIndex(ACOrgUnit orgUnit) {
 		int result = 0;
 		Role role;
 		try {
 			role = getAdminBroker().getRole(projectInfo.getProjectId(), orgUnit.getId());
 			if (role.eClass().equals(RolesPackage.eINSTANCE.getReaderRole())) {
 				result = READER_ROLE;
 			} else if (role.eClass().equals(RolesPackage.eINSTANCE.getWriterRole())) {
 				result = WRITER_ROLE;
 
 			} else if (role.eClass().equals(RolesPackage.eINSTANCE.getProjectAdminRole())) {
 				result = PROJECT_ADMIN_ROLE;
 			} else if (role.eClass().equals(RolesPackage.eINSTANCE.getServerAdmin())) {
 				result = SERVER_ADMIN_ROLE;
 			}
 		} catch (final ESException e) {
 			EMFStoreMessageDialog.showExceptionDialog(e);
 		}
 		return result;
 	}
 
 	/**
 	 * This class provides editing support for Role column in TableViewer. It uses a ComboBoxCellEditor.
 	 * 
 	 * @author Hodaie
 	 */
 	private class RoleEditingSupport extends EditingSupport {
 
 		private final ComboBoxCellEditor cellEditor;
 
 		public RoleEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 			cellEditor = new ComboBoxCellEditor(getTableViewer().getTable(), roleNames, SWT.READ_ONLY);
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			return cellEditor;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			return getCurrentRoleIndex((ACOrgUnit) element);
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			changeRole((ACOrgUnit) element, ((Integer) value).intValue());
 		}
 
 	}
 
 }// ProjectComposite
