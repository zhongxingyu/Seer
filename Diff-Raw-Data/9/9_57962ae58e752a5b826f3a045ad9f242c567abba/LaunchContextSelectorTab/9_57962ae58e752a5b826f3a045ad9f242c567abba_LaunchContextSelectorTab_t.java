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
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.tcf.te.launch.ui.nls.Messages;
 import org.eclipse.tcf.te.launch.ui.tabs.AbstractFormsLaunchConfigurationTab;
 import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 import org.eclipse.ui.forms.widgets.TableWrapLayout;
 
 /**
  * Launch context selector tab implementation.
  */
 public class LaunchContextSelectorTab extends AbstractFormsLaunchConfigurationTab {
 	// References to the tab sub sections
 	private ContextSelectorSection selectorSection;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.tabs.AbstractFormsLaunchConfigurationTab#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (selectorSection != null) { selectorSection.dispose(); selectorSection = null; }
 	    super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.tabs.AbstractFormsLaunchConfigurationTab#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
 	 */
 	@Override
 	protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
 		Assert.isNotNull(parent);
 		Assert.isNotNull(toolkit);
 
 		// Setup the main panel (using the table wrap layout)
 		Composite panel = toolkit.getFormToolkit().createComposite(parent);
 		TableWrapLayout layout = new TableWrapLayout();
 		layout.makeColumnsEqualWidth = true;
 		layout.numColumns = 1;
 		panel.setLayout(layout);
 		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		panel.setBackground(parent.getBackground());
 
 		selectorSection = new ContextSelectorSection(getManagedForm(), panel);
 		selectorSection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
 		getManagedForm().addPart(selectorSection);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	@Override
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
 	 */
 	@Override
 	public String getName() {
 		return Messages.LaunchContextSelectorTab_name;
 	}
 }
