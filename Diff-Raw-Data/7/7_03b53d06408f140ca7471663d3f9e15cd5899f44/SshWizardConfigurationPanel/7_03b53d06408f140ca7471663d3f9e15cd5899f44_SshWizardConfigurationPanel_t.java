 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  * Max Weninger (Wind River) - [361352] [TERMINALS][SSH] Add SSH terminal support
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.terminals.ssh.controls;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.equinox.security.storage.ISecurePreferences;
 import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
 import org.eclipse.equinox.security.storage.StorageException;
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
 import org.eclipse.tm.internal.terminal.ssh.SshConnector;
 import org.eclipse.tm.internal.terminal.ssh.SshSettings;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * SSH wizard configuration panel implementation.
  */
 @SuppressWarnings("restriction")
 public class SshWizardConfigurationPanel extends AbstractConfigurationPanel implements IDataExchangeNode {
 
     private SshSettings sshSettings;
 	private ISettingsPage sshSettingsPage;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param parentControl The parent control. Must not be <code>null</code>!
 	 */
 	public SshWizardConfigurationPanel(BaseDialogPageControl parentControl) {
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
 
 		SshConnector conn = new SshConnector();
 		sshSettings = (SshSettings) conn.getSshSettings();
 		sshSettings.setHost(getSelectionHost());
 		sshSettings.setUser(getDefaultUser());
 		sshSettingsPage = conn.makeSettingsPage();
 		sshSettingsPage.createControl(panel);
 
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
 
 	/**
 	 * Returns the default user name.
 	 *
 	 * @return The default user name.
 	 */
 	private String getDefaultUser(){
 		return System.getProperty("user.name"); //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataExchangeNode#extractData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
     public void extractData(IPropertiesContainer data) {
     	// set the terminal connector id for ssh
     	data.setProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, "org.eclipse.tm.internal.terminal.ssh.SshConnector"); //$NON-NLS-1$
 
     	// set the connector type for ssh
     	data.setProperty(ITerminalsConnectorConstants.PROP_CONNECTOR_TYPE_ID, "org.eclipse.tcf.te.ui.terminals.type.ssh"); //$NON-NLS-1$
 
     	sshSettingsPage.saveSettings();
 		data.setProperty(ITerminalsConnectorConstants.PROP_IP_HOST,sshSettings.getHost());
 		data.setProperty(ITerminalsConnectorConstants.PROP_IP_PORT, sshSettings.getPort());
 		data.setProperty(ITerminalsConnectorConstants.PROP_TIMEOUT, sshSettings.getTimeout());
 		data.setProperty(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE, sshSettings.getKeepalive());
 		data.setProperty(ITerminalsConnectorConstants.PROP_SSH_PASSWORD, sshSettings.getPassword());
 		data.setProperty(ITerminalsConnectorConstants.PROP_SSH_USER, sshSettings.getUser());
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
 					sshSettings.setHost(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST));
 				}
 				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT) != null) {
 					sshSettings.setPort(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT));
 				}
 				if (hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT) != null) {
 					sshSettings.setTimeout(hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT));
 				}
 				if (hostSettings.get(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE) != null) {
 					sshSettings.setKeepalive(hostSettings.get(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE));
 				}
 				if (hostSettings.get(ITerminalsConnectorConstants.PROP_SSH_USER) != null) {
 					sshSettings.setUser(hostSettings.get(ITerminalsConnectorConstants.PROP_SSH_USER));
 				}
 				String password = accessSecurePassword(sshSettings.getHost());
 				if (password != null) {
 					sshSettings.setPassword(password);
 				}

				String encoding = hostSettings.get(ITerminalsConnectorConstants.PROP_ENCODING);
				if (encoding == null || "null".equals(encoding)) encoding = "ISO-8859-1"; //$NON-NLS-1$ //$NON-NLS-2$
				setEncoding(encoding);
 			} else {
 				sshSettings.setHost(getSelectionHost());
 				sshSettings.setUser(getDefaultUser());
 			}
 			// set settings in page
 			sshSettingsPage.loadSettings();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
 	 */
 	@Override
     public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
     	saveSettingsForHost(true);
     	super.doSaveWidgetValues(settings, idPrefix);
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#saveSettingsForHost(boolean)
 	 */
 	@Override
 	protected void saveSettingsForHost(boolean add){
 		String host = getHostFromSettings();
 		if (host != null && host.length() != 0) {
 			if (hostSettingsMap.containsKey(host)){
 				Map<String, String> hostSettings = hostSettingsMap.get(host);
 				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_HOST, sshSettings.getHost());
 				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_PORT, Integer.toString(sshSettings.getPort()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.toString(sshSettings.getTimeout()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE, Integer.toString(sshSettings.getKeepalive()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_SSH_USER, sshSettings.getUser());
 				hostSettings.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
 
 				if (sshSettings.getPassword() != null && sshSettings.getPassword().length() != 0){
 					saveSecurePassword(host, sshSettings.getPassword());
 				}
 			} else if (add) {
 				Map<String, String> hostSettings = new HashMap<String, String>();
 				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_HOST, sshSettings.getHost());
 				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_PORT, Integer.toString(sshSettings.getPort()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.toString(sshSettings.getTimeout()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE, Integer.toString(sshSettings.getKeepalive()));
 				hostSettings.put(ITerminalsConnectorConstants.PROP_SSH_USER, sshSettings.getUser());
 				hostSettings.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
 				hostSettingsMap.put(host, hostSettings);
 
 				if (sshSettings.getPassword() != null && sshSettings.getPassword().length() != 0){
 					saveSecurePassword(host, sshSettings.getPassword());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Save the password to the secure storage.
 	 *
 	 * @param host The host. Must not be <code>null</code>.
 	 * @param password The password. Must not be <code>null</code>.
 	 */
 	private void saveSecurePassword(String host, String password) {
 		Assert.isNotNull(host);
 		Assert.isNotNull(password);
 
 		// To access the secure storage, we need the preference instance
 		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
 		if (preferences != null) {
 			// Construct the secure preferences node key
 			String nodeKey = "/Target Explorer SSH Password/" + host; //$NON-NLS-1$
 			ISecurePreferences node = preferences.node(nodeKey);
 			if (node != null) {
 				try {
 					node.put("password", password, true); //$NON-NLS-1$
 				}
 				catch (StorageException ex) { /* ignored on purpose */ }
 			}
 		}
 	}
 
 	/**
 	 * Reads the password from the secure storage.
 	 *
 	 * @param host The host. Must not be <code>null</code>.
 	 * @return The password or <code>null</code>.
 	 */
 	private String accessSecurePassword(String host) {
 		Assert.isNotNull(host);
 
 		// To access the secure storage, we need the preference instance
 		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
 		if (preferences != null) {
 			// Construct the secure preferences node key
 			String nodeKey = "/Target Explorer SSH Password/" + host; //$NON-NLS-1$
 			ISecurePreferences node = preferences.node(nodeKey);
 			if (node != null) {
 				String password = null;
 				try {
 					password = node.get("password", null); //$NON-NLS-1$
 				}
 				catch (StorageException ex) { /* ignored on purpose */ }
 
 				return password;
 			}
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#removeSecurePassword(java.lang.String)
 	 */
 	@Override
 	protected void removeSecurePassword(String host) {
 		Assert.isNotNull(host);
 
 		// To access the secure storage, we need the preference instance
 		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
 		if (preferences != null) {
 			// Construct the secure preferences node key
 			String nodeKey = "/Target Explorer SSH Password/" + host; //$NON-NLS-1$
 			ISecurePreferences node = preferences.node(nodeKey);
 			if (node != null) {
 				node.remove("password"); //$NON-NLS-1$
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
 	 */
 	@Override
     public boolean isValid(){
 		return isEncodingValid() && sshSettingsPage.validateSettings();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#getHostFromSettings()
 	 */
 	@Override
     protected String getHostFromSettings() {
 		sshSettingsPage.saveSettings();
 	    return sshSettings.getHost();
     }
 }
