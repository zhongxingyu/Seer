 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  * Max Weninger (Wind River) - [366374] [TERMINALS][TELNET] Add Telnet terminal support
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.terminals.telnet.controls;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.TypedEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
 import org.eclipse.tcf.te.ui.controls.BaseDialogPageControl;
 import org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode;
 import org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel;
 import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
 import org.eclipse.tm.internal.terminal.telnet.NetworkPortMap;
 import org.eclipse.tm.internal.terminal.telnet.TelnetConnector;
 import org.eclipse.tm.internal.terminal.telnet.TelnetSettings;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * telnet wizard configuration panel implementation.
  */
 @SuppressWarnings("restriction")
 public class TelnetWizardConfigurationPanel extends AbstractConfigurationPanel implements IDataExchangeNode {
 
     public TelnetSettings telnetSettings;
 	private ISettingsPage telnetSettingsPage;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param parentControl The parent control. Must not be <code>null</code>!
 	 */
 	public TelnetWizardConfigurationPanel(BaseDialogPageControl parentControl) {
 	    super(parentControl);
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
 	 */
 	@Override
 	public void setupPanel(Composite parent, FormToolkit toolkit) {
 		Composite panel = new Composite(parent, SWT.NONE);
 		panel.setLayout(new GridLayout());
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		panel.setLayoutData(data);
 
 		// Create the host selection combo
 		if (isWithoutSelection()) createHostsUI(panel, true);
 
 		TelnetConnector conn = new TelnetConnector();
 		telnetSettings = (TelnetSettings) conn.getTelnetSettings();
 		telnetSettings.setHost(getSelectionHost());
 		// MWE otherwise we don't get a valid default selection of the combo
 		telnetSettings.setNetworkPort(NetworkPortMap.PROP_VALUETELNET);
 
		telnetSettingsPage = conn.makeSettingsPage();
		telnetSettingsPage.createControl(panel);

 		// Create the encoding selection combo
 		createEncodingUI(panel, true);
 
 		setControl(panel);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
 	 */
 	@Override
 	public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataExchangeNode#setupData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
     public void setupData(IPropertiesContainer data) {
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataExchangeNode#extractData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
     public void extractData(IPropertiesContainer data) {
     	// set the terminal connector id for ssh
     	data.setProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, "org.eclipse.tm.internal.terminal.telnet.TelnetConnector"); //$NON-NLS-1$
 
     	// set the connector type for ssh
     	data.setProperty(ITerminalsConnectorConstants.PROP_CONNECTOR_TYPE_ID, "org.eclipse.tcf.te.ui.terminals.type.telnet"); //$NON-NLS-1$
 
     	telnetSettingsPage.saveSettings();
 		data.setProperty(ITerminalsConnectorConstants.PROP_IP_HOST,telnetSettings.getHost());
 		data.setProperty(ITerminalsConnectorConstants.PROP_IP_PORT, telnetSettings.getNetworkPort());
 		data.setProperty(ITerminalsConnectorConstants.PROP_TIMEOUT, telnetSettings.getTimeout());
 		data.setProperty(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#fillSettingsForHost(java.lang.String)
 	 */
 	@Override
 	protected void fillSettingsForHost(String host){
 		if (host != null && host.length() != 0){
 			if (hostSettingsMap.containsKey(host)){
 				Map<String, String> hostSettings = hostSettingsMap.get(host);
 				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST) != null) {
 					telnetSettings.setHost(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST));
 				}
 				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT) != null) {
 					telnetSettings.setNetworkPort(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT));
 				}
 				if (hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT) != null) {
 					telnetSettings.setTimeout(hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT));
 				}
 				if (hostSettings.get(ITerminalsConnectorConstants.PROP_ENCODING) != null) {
 					setEncoding(hostSettings.get(ITerminalsConnectorConstants.PROP_ENCODING));
 				}
 			} else {
 				telnetSettings.setHost(getSelectionHost());
 				// MWE otherwise we don't get a valid default selection of the combo
 				telnetSettings.setNetworkPort(NetworkPortMap.PROP_VALUETELNET);
 			}
 			// set settings in page
 			telnetSettingsPage.loadSettings();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#saveSettingsForHost(boolean)
 	 */
 	@Override
 	protected void saveSettingsForHost(boolean add){
 		String host = getHostFromSettings();
 		if(host != null && host.length() != 0) {
 			if (hostSettingsMap.containsKey(host)) {
 				Map<String, String> hostSettings=hostSettingsMap.get(host);
 				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_HOST, telnetSettings.getHost());
 				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_PORT, Integer.toString(telnetSettings.getNetworkPort()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.toString(telnetSettings.getTimeout()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
 			} else if (add) {
 				Map<String, String> hostSettings=new HashMap<String, String>();
 				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_HOST, telnetSettings.getHost());
 				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_PORT, Integer.toString(telnetSettings.getNetworkPort()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.toString(telnetSettings.getTimeout()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
 				hostSettingsMap.put(host, hostSettings);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
 	 */
 	@Override
     public boolean isValid(){
 		return isEncodingValid() && telnetSettingsPage.validateSettings();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
 	 */
 	@Override
     public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
     	saveSettingsForHost(true);
     	super.doSaveWidgetValues(settings, idPrefix);
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#getHostFromSettings()
 	 */
 	@Override
     protected String getHostFromSettings() {
 		telnetSettingsPage.saveSettings();
 	    return telnetSettings.getHost();
     }
 }
