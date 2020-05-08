 /*******************************************************************************
  *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  ******************************************************************************/
 package net.sourceforge.jcctray.ui.settings;
 
 import net.sourceforge.jcctray.model.CruiseRegistry;
 import net.sourceforge.jcctray.model.Host;
 import net.sourceforge.jcctray.model.ICruise;
 import net.sourceforge.jcctray.model.IJCCTraySettings;
 import net.sourceforge.jcctray.ui.Utils;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Shows a dialog box that adds {@link Host}s to JCCTray.
  * 
  * @author Ketan Padegaonkar
  */
 public class AddServerDialog {
 
 	public class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
 		public String getText(Object element) {
 			try {
 				return ((ICruise) ((Class) element).newInstance()).getName();
 			} catch (Exception e) {
 				log.error("Could not instantiate cruise implementation", e);
 			}
 			return "Error getting name for " + ((Class) element).getName();
 		}
 	}
 
 	private final class CancelButtonListener implements SelectionListener {
 		public void widgetDefaultSelected(SelectionEvent e) {
 			shell.close();
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 	}
 
 	private class OkButtonListener implements SelectionListener {
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			if (comboViewer.getSelection().isEmpty() || hostStringText.getText().trim().length() == 0
 					|| serverURLString.getText().trim().length() == 0) {
 				MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING);
 				messageBox
 						.setMessage("You did not enter all the parameters for to configure JCCTray with cruise server.");
 				messageBox.setText("Please enter all the values");
 				messageBox.open();
 				hostStringText.forceFocus();
 				return;
 			}
 
 			Host host = new Host(hostStringText.getText(), serverURLString.getText());
 			IStructuredSelection selection = (IStructuredSelection) comboViewer.getSelection();
 			host.setCruiseClass(((Class) selection.getFirstElement()).getName());
 			traySettings.addHost(host);
 			Utils.saveSettings(shell);
 			shell.close();
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 	}
 
 	private static final Logger		log	= Logger.getLogger(AddServerDialog.class);
 	private final Shell				parentShell;
 	private Shell					shell;
 	private Button					okButton;
 	private Button					cancelButton;
 	private Text					hostStringText;
 	private Text					serverURLString;
	private Combo					serverType;
 	private ComboViewer				comboViewer;
 	private final IJCCTraySettings	traySettings;
 
 	public AddServerDialog(Shell shell, IJCCTraySettings traySettings) {
 		this.parentShell = shell;
 		this.traySettings = traySettings;
 		initialize();
 		hookEvents();
 	}
 
 	private void initialize() {
 		shell = new Shell(parentShell, SWT.DIALOG_TRIM);
 		shell.setText("Add Server");
 		shell.setSize(400, 200);
 		shell.setLayout(new GridLayout(2, false));
 
 		new Label(shell, SWT.NONE).setText("Server &Name:");
 		hostStringText = new Text(shell, SWT.BORDER);
 		hostStringText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		new Label(shell, SWT.NONE).setText("Server &Type:");
		serverType = new Combo(shell, SWT.BORDER);
 		serverType.setText("Select");
 		serverType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		comboViewer = new ComboViewer(serverType);
 		comboViewer.setContentProvider(new ArrayContentProvider());
 		comboViewer.setLabelProvider(new LabelProvider());
 
 		new Label(shell, SWT.NONE).setText("Server &URL:");
 		serverURLString = new Text(shell, SWT.BORDER);
 		serverURLString.setText("http://<somehost>:1234/cruise");
 		serverURLString.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		okButton = new Button(shell, SWT.NONE);
 		okButton.setText("&Ok");
 		okButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
 		shell.setDefaultButton(okButton);
 
 		cancelButton = new Button(shell, SWT.NONE);
 		cancelButton.setText("&Cancel");
 		cancelButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		// shell.pack();
 	}
 
 	private void hookEvents() {
 		okButton.addSelectionListener(new OkButtonListener());
 		cancelButton.addSelectionListener(new CancelButtonListener());
 	}
 
 	public void open() {
 		shell.open();
 		comboViewer.setInput(CruiseRegistry.getInstance().getCruiseImpls());
 		shell.pack();
 	}
 }
