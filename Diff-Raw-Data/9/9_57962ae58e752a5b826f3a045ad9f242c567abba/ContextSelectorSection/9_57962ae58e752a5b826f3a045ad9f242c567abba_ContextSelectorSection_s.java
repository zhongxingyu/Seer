 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.launch.ui.tabs.selector;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
 import org.eclipse.tcf.te.launch.ui.internal.ImageConsts;
 import org.eclipse.tcf.te.launch.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Context selector section implementation.
  */
 public class ContextSelectorSection extends AbstractSection {
 	// Reference to the section sub controls
 	/* default */ RemoteContextSelectorControl selector;
 
 	/**
 	 * Context selector control refresh action implementation.
 	 */
 	protected class RefreshAction extends Action {
 
 		/**
          * Constructor.
          */
         public RefreshAction() {
         	super(null, IAction.AS_PUSH_BUTTON);
         	setImageDescriptor(UIPlugin.getImageDescriptor(ImageConsts.ACTION_Refresh_Enabled));
         	setToolTipText(Messages.ContextSelectorControl_toolbar_refresh_tooltip);
         }
 
         /* (non-Javadoc)
          * @see org.eclipse.jface.action.Action#run()
          */
         @Override
         public void run() {
         	if (selector != null && selector.getViewer() != null) {
         		selector.getViewer().refresh();
         	}
         }
 	}
 
 	/**
 	 * Constructor.
 	 *
 	 * @param form The parent managed form. Must not be <code>null</code>.
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 */
 	public ContextSelectorSection(IManagedForm form, Composite parent) {
 		super(form, parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
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
 		section.setText(Messages.ContextSelectorSection_title);
 		section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		// Create the section client
 		Composite client = createClientContainer(section, 1, toolkit);
 		Assert.isNotNull(client);
 		section.setClient(client);
 
 		// Create a toolbar for the section
 		createSectionToolbar(section, toolkit);
 
 		// Create the section sub controls
 		selector = new RemoteContextSelectorControl(null);
 		selector.setFormToolkit(toolkit);
 		selector.setupPanel(client);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (selector != null) { selector.dispose(); selector = null; }
 	    super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createSectionToolbarItems(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.jface.action.ToolBarManager)
 	 */
 	@Override
 	protected void createSectionToolbarItems(Section section, FormToolkit toolkit, ToolBarManager tlbMgr) {
 	    super.createSectionToolbarItems(section, toolkit, tlbMgr);
 	    tlbMgr.add(new RefreshAction());
 	}
 }
