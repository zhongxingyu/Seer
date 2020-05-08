 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.internal.debug.ui.launchConf;
 
 import java.util.Iterator;
 
 import org.eclipse.debug.internal.ui.DebugUIMessages;
 import org.eclipse.debug.internal.ui.MultipleInputDialog;
 import org.eclipse.debug.internal.ui.launchConfigurations.EnvironmentVariable;
 import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
 import org.eclipse.debug.ui.EnvironmentTab;
 import org.eclipse.debug.ui.ILaunchConfigurationDialog;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.debug.ui.StringVariableSelectionDialog;
 import org.eclipse.dltk.mod.debug.ui.launchConfigurations.ScriptBuildTab;
 import org.eclipse.dltk.mod.debug.ui.launchConfigurations.ScriptCommonTab;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.vjet.dsf.jsnative.anno.BrowserType;
 
 public class VjetWebAppTabGroup extends AbstractLaunchConfigurationTabGroup {
 
 	private class MultipleInputDialogWithBrowser extends MultipleInputDialog {
 
 		MultipleInputDialogWithBrowser(Shell shell, String title) {
 			super(shell, title);
 		}
 
 		protected void okPressed() {
 
 			for (Iterator i = controlList.iterator(); i.hasNext();) {
 
 				Control control = (Control) i.next();
 				if (control instanceof Combo) {
 
 					Combo combo = ((Combo) control);
 
 					int index = combo.getSelectionIndex();
 					if (index < 0) {
 						index = 0;
 						combo.setItem(index, combo.getText());
 						combo.select(index);
 					}
 					valueMap.put(control.getData(FIELD_NAME), combo
 							.getItem(index));
 
 				}
 			}
 
 			super.okPressed();
 		}
 
 		public void createVariablesField(String labelText, String initialValue,
 				boolean allowEmpty) {
 
 			Label label = new Label(panel, SWT.NONE);
 			label.setText(labelText);
 			label.setLayoutData(new GridData(
 					GridData.HORIZONTAL_ALIGN_BEGINNING));
 
 			Composite comp = new Composite(panel, SWT.NONE);
 			GridLayout layout = new GridLayout();
 			layout.marginHeight = 0;
 			layout.marginWidth = 0;
 			comp.setLayout(layout);
 			comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 			final Combo combo = new Combo(comp, SWT.SINGLE | SWT.BORDER);
 			GridData data = new GridData(GridData.FILL_HORIZONTAL);
 			data.widthHint = 200;
 			combo.setLayoutData(data);
 			combo.setData(FIELD_NAME, labelText);
 
 			BrowserType[] types = BrowserType.values();
 			String[] strings = new String[types.length];
 			for (int iter = 0; iter < types.length; iter++) {
 				strings[iter] = types[iter].name();
 			}
 
 			combo.setItems(strings);
 
 			// make sure rows are the same height on both
 			// panels.
 			label.setSize(label.getSize().x, combo.getSize().y);
 
 			if (initialValue != null) {
 				combo.setText(initialValue);
 			}
 
 			Button button = createButton(comp, IDialogConstants.IGNORE_ID,
 					DebugUIMessages.MultipleInputDialog_8, false);
 			button.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
 							getShell());
 					int code = dialog.open();
 					if (code == IDialogConstants.OK_ID) {
 						String variable = dialog.getVariableExpression();
 						if (variable != null) {
 							combo.setItem(0, variable);
 							combo.setText(variable);
 							combo.select(0);
 						}
 					}
 				}
 			});
 
			controlList.add(combo);
 
 		}
 	};
 
 	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
 		setTabs(new ILaunchConfigurationTab[] {
 				new VjetMainLaunchConfTab(mode), 
 				new VjetArgumentsTab(),
 				new ScriptBuildTab(),
 				new EnvironmentTab() {
 					protected void handleEnvAddButtonSelected() {
 
 						MultipleInputDialog dialog = new MultipleInputDialogWithBrowser(
 								getShell(),
 								LaunchConfigurationsMessages.EnvironmentTab_22);
 
 						dialog.addTextField(
 								LaunchConfigurationsMessages.EnvironmentTab_8,
 								null, false);
 						dialog.addVariablesField(
 								LaunchConfigurationsMessages.EnvironmentTab_9,
 								null, true);
 
 						if (dialog.open() != Window.OK) {
 							return;
 						}
 
 						String name = dialog
 								.getStringValue(LaunchConfigurationsMessages.EnvironmentTab_8);
 						String value = dialog
 								.getStringValue(LaunchConfigurationsMessages.EnvironmentTab_9);
 
 						if (name != null && value != null) {
 							addVariable(new EnvironmentVariable(name.trim(),
 									value.trim()));
 							updateAppendReplace();
 						}
 
 					}
 				}, new ScriptCommonTab() });
 	}
 }
