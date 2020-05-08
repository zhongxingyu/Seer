 /*
  * Author: C.Williams
  * 
  * Copyright (c) 2004 RubyPeople.
  * 
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
  * can get copy of the GPL along with further information about RubyPeople and
  * third party software bundled with RDT in the file
  * org.rubypeople.rdt.core_x.x.x/RDT.license or otherwise at
  * http://www.rubypeople.org/RDT.license.
  * 
  * RDT is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 package org.rubypeople.rdt.testunit.launcher;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
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
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
 import org.rubypeople.rdt.internal.launching.InterpreterRunnerConfiguration;
 import org.rubypeople.rdt.internal.launching.RubyLaunchConfigurationAttribute;
 import org.rubypeople.rdt.internal.ui.utils.RubyFileSelector;
 import org.rubypeople.rdt.internal.ui.utils.RubyProjectSelector;
 import org.rubypeople.rdt.testunit.TestunitPlugin;
 import org.rubypeople.rdt.testunit.views.TestUnitMessages;
 
 /**
  * @author Chris
  *  
  */
 public class TestUnitMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {
 
 	protected RubyProjectSelector projectSelector;
 	protected RubyFileSelector fileSelector;
 	private RubyClassSelector classSelector;
 	protected ElementListSelectionDialog dialog;
 	protected Button keepRunning;
 	private IProject rubyProject;
 
 	public TestUnitMainTab() {
 		super();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite composite = createPageRoot(parent);
 
 		new Label(composite, SWT.NONE).setText(TestUnitMessages.getString("JUnitMainTab.label.test"));
 		classSelector = new RubyClassSelector(composite);
 		classSelector.setBrowseDialogMessage(RdtDebugUiMessages.getString("LaunchConfigurationTab.RubyEntryPoint.projectSelectorMessage"));
 		classSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		classSelector.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent evt) {
 				updateLaunchConfigurationDialog();
 			}
 		});
 
 		new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.getString("LaunchConfigurationTab.RubyEntryPoint.projectLabel"));
 		projectSelector = new RubyProjectSelector(composite);
 		projectSelector.setBrowseDialogMessage(RdtDebugUiMessages.getString("LaunchConfigurationTab.RubyEntryPoint.projectSelectorMessage"));
 		projectSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		projectSelector.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent evt) {
 				updateLaunchConfigurationDialog();
 			}
 		});
 
 		new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.getString("LaunchConfigurationTab.RubyEntryPoint.fileLabel"));
 		fileSelector = new RubyFileSelector(composite, projectSelector);
 		fileSelector.setBrowseDialogMessage(RdtDebugUiMessages.getString("LaunchConfigurationTab.RubyEntryPoint.fileSelectorMessage"));
 		fileSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		fileSelector.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent evt) {
 				updateLaunchConfigurationDialog();
 			}
 		});
 
 		Composite keepRunningComposite = new Composite(composite, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		keepRunningComposite.setLayout(layout);
 		keepRunning = new Button(keepRunningComposite, SWT.CHECK);
 		keepRunning.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				setKeepRunning(((Button) e.getSource()).getSelection());
 			}
 		});
 		new Label(keepRunningComposite, SWT.NONE).setText(TestUnitMessages.getString("JUnitMainTab.label.keeprunning"));
 		keepRunningComposite.pack();
 	}
 
 	protected Composite createPageRoot(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout compositeLayout = new GridLayout();
 		compositeLayout.marginWidth = 0;
 		compositeLayout.numColumns = 1;
 		composite.setLayout(compositeLayout);
 
 		setControl(composite);
 		return composite;
 	}
 
 	protected void setKeepRunning(boolean keepItRunning) {
 		if (keepRunning.getSelection() != keepItRunning) keepRunning.setSelection(keepItRunning);
 		// FIXME Why does JUnit disable project selection if keepRunning is
 		// selected?
 		//		if (keepItRunning) {
 		//			projectSelector.setSelectionText("");
 		//		}
 		//		projectSelector.setEnabled(!keepItRunning);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(TestUnitLaunchConfiguration.ATTR_KEEPRUNNING, false);
 		configuration.setAttribute(TestUnitLaunchConfiguration.LAUNCH_CONTAINER_ATTR, "");
 		configuration.setAttribute(TestUnitLaunchConfiguration.TESTTYPE_ATTR, "");
 		configuration.setAttribute(TestUnitLaunchConfiguration.TESTNAME_ATTR, "");
 		// set hidden attribute
 		configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.rubypeople.rdt.debug.ui.rubySourceLocator");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	public void initializeFrom(ILaunchConfiguration configuration) {
		boolean keepRunning = true;
 		try {
			// Have to set the project selection first! Otherwise when launch
			// container ise set it will use null for project when trying to
			// validate the file exists and will eventually set the launch
			// container to ""
 			InterpreterRunnerConfiguration config = new InterpreterRunnerConfiguration(configuration);
 			rubyProject = config.getProject().getProject();
			projectSelector.setSelectionText(rubyProject.getName());
			classSelector.setSelectionText(configuration.getAttribute(TestUnitLaunchConfiguration.TESTTYPE_ATTR, ""));
			fileSelector.setSelectionText(configuration.getAttribute(TestUnitLaunchConfiguration.LAUNCH_CONTAINER_ATTR, ""));
			keepRunning = configuration.getAttribute(TestUnitLaunchConfiguration.ATTR_KEEPRUNNING, false);
 		} catch (CoreException e) {
			TestunitPlugin.log(e);
 		}
 
		setKeepRunning(keepRunning);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		//configuration.setAttribute(TestUnitLaunchConfiguration.ATTR_KEEPRUNNING, keepRunning.getSelection());
		// TODO We should really just remove the "keep running" attribute. It makes the Test::Unit run act weird
		configuration.setAttribute(TestUnitLaunchConfiguration.ATTR_KEEPRUNNING, false);
 		configuration.setAttribute(TestUnitLaunchConfiguration.TESTTYPE_ATTR, classSelector.getValidatedSelectionText());
 		configuration.setAttribute(TestUnitLaunchConfiguration.LAUNCH_CONTAINER_ATTR, fileSelector.getValidatedSelectionText());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
 	 */
 	public String getName() {
 		return TestUnitMessages.getString("JUnitMainTab.tab.label"); //$NON-NLS-1$
 	}
 
 }
