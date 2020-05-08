 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.editor.sections;
 
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelPeerNodeQueryService;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
 import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
 import org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Peer services section implementation.
  */
 public class ServicesSection extends AbstractSection {
 	// The section sub controls
 	private Text local;
 	private Text remote;
 
 	// Reference to the original data object
 	/* default */ IPeerModel od;
 	// Reference to a copy of the original data
 	/* default */ final IPropertiesContainer odc = new PropertiesContainer();
 
 	/**
 	 * Constructor.
 	 *
 	 * @param form The parent managed form. Must not be <code>null</code>.
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 */
 	public ServicesSection(IManagedForm form, Composite parent) {
 		super(form, parent, Section.DESCRIPTION | ExpandableComposite.TWISTIE);
 		createClient(getSection(), form.getToolkit());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
 	 */
 	@Override
 	protected void createClient(Section section, FormToolkit toolkit) {
 		Assert.isNotNull(section);
 		Assert.isNotNull(toolkit);
 
 		// Configure the section
 		section.setText(Messages.ServicesSection_title);
 		section.setDescription(Messages.ServicesSection_description);
 
 		if (section.getParent().getLayout() instanceof GridLayout) {
 			section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		}
 
 		// Create the section client
 		Composite client = createClientContainer(section, 1, toolkit);
 		Assert.isNotNull(client);
 		section.setClient(client);
 
 		Group group = new Group(client, SWT.NONE);
 		group.setText(Messages.ServicesSection_group_local_title);
 		group.setLayout(new GridLayout());
 		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		local = new Text(group, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
 		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		layoutData.widthHint = SWTControlUtil.convertWidthInCharsToPixels(local, 20);
 		layoutData.heightHint = SWTControlUtil.convertHeightInCharsToPixels(local, 5);
 		local.setLayoutData(layoutData);
 
 		group = new Group(client, SWT.NONE);
 		group.setText(Messages.ServicesSection_group_remote_title);
 		group.setLayout(new GridLayout());
 		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		remote = new Text(group, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
 		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		layoutData.widthHint = SWTControlUtil.convertWidthInCharsToPixels(local, 20);
 		layoutData.heightHint = SWTControlUtil.convertHeightInCharsToPixels(remote, 5);
 		remote.setLayoutData(layoutData);
 
 		// Mark the control update as completed now
 		setIsUpdating(false);
 	}
 
 	/**
 	 * Indicates whether the sections parent page has become the active in the editor.
 	 *
 	 * @param active <code>True</code> if the parent page should be visible, <code>false</code> otherwise.
 	 */
 	public void setActive(boolean active) {
 		// If the parent page has become the active and it does not contain
 		// unsaved data, than fill in the data from the selected node
 		if (active) {
 			// Leave everything unchanged if the page is in dirty state
 			if (getManagedForm().getContainer() instanceof AbstractEditorPage
 					&& !((AbstractEditorPage)getManagedForm().getContainer()).isDirty()) {
 				Object node = ((AbstractEditorPage)getManagedForm().getContainer()).getEditorInputNode();
 				if (node instanceof IPeerModel) {
 					setupData((IPeerModel)node);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Initialize the page widgets based of the data from the given peer node.
 	 * <p>
 	 * This method may called multiple times during the lifetime of the page and
 	 * the given configuration node might be even <code>null</code>.
 	 *
 	 * @param node The peer node or <code>null</code>.
 	 */
 	public void setupData(final IPeerModel node) {
 		// Store a reference to the original data
 		od = node;
 		// Clean the original data copy
 		odc.clearProperties();
 
 		// If no data is available, we are done
 		if (node == null) return;
 
 		// Trigger the query of the services provided by the node
 		final AtomicBoolean needQueryServices = new AtomicBoolean();
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
				// If one services key is set, the other is set to. The more important information
				// is the remote services, so test for the remote services key.
				needQueryServices.set(!node.containsKey(IPeerModelProperties.PROP_REMOTE_SERVICES));
 			}
 		});
 
 		if (needQueryServices.get()) {
 			node.getModel().getService(ILocatorModelPeerNodeQueryService.class).queryLocalServices(node);
 		}
 
 		// Thread access to the model is limited to the executors thread.
 		// Copy the data over to the working copy to ease the access.
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				Map<String, Object> properties = od.getProperties();
 				odc.setProperties(properties);
 			}
 		});
 
 		boolean fireRefreshTabs = needQueryServices.get();
 
 		String value = odc.getStringProperty(IPeerModelProperties.PROP_LOCAL_SERVICES);
 		fireRefreshTabs |= value != null && !value.equals(SWTControlUtil.getText(local));
 		SWTControlUtil.setText(local, value != null ? value : ""); //$NON-NLS-1$
 		value = odc.getStringProperty(IPeerModelProperties.PROP_REMOTE_SERVICES);
 		fireRefreshTabs |= value != null && !value.equals(SWTControlUtil.getText(remote));
 		SWTControlUtil.setText(remote, value != null ? value : ""); //$NON-NLS-1$
 
 		if (fireRefreshTabs) {
 			// Fire a change event to trigger the editor refresh
 			od.fireChangeEvent("editor.refreshTab", Boolean.FALSE, Boolean.TRUE); //$NON-NLS-1$
 		}
 	}
 }
