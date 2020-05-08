 /*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.dialogs.preferences;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.logger.CyLogger;
 import cytoscape.util.ProxyHandler;
 
 
 /**
  *
  */
 public class ProxyServerDialog extends JDialog implements ActionListener, ItemListener {
 
 	private static final long serialVersionUID = -2693844068486336199L;
         private static final String   WELL_KNOWN_URL   = "http://google.com";
         private static final String PROPERTIES_FILE_NAME = "cytoscape.props";
 
 	/** Creates new form URLimportAdvancedDialog */
 	public ProxyServerDialog(JFrame pParent) {
 		super(pParent, true);
		this.setTitle("Proxy Server Settings");
 		this.setLocationRelativeTo(pParent);
 
 		initComponents();
 		initValues();
 	}
 
 	private JButton btnCancel;
         // TODO: Change the update button only be enabled when there has
         //       been a change that needs updating.
 	private JButton btnUpdate;
 	private JCheckBox chbUseProxy;
 	private JComboBox cmbType;
 	private JPanel jPanel1;
 	private JPanel jPanel2;
 	private JPanel jPanel3;
 	private JLabel lbHost;
 	private JLabel lbPort;
 	private JLabel lbType;
 	private JLabel lbUseProxy;
 	private JTextField tfHost;
 	private JTextField tfPort;
         private boolean _lastUseProxy;
         private String  _lastProxyHost;
         private String  _lastProxyPort;
         private String  _lastProxyType;
 
 	private void initComponents() {
 		
 		GridBagConstraints gridBagConstraints;
 
 		jPanel3 = new JPanel();
 		lbUseProxy = new JLabel();
 		chbUseProxy = new JCheckBox();
 		jPanel1 = new JPanel();
 		btnUpdate = new JButton();
 		btnCancel = new JButton();
 		jPanel2 = new JPanel();
 		lbType = new JLabel();
 		lbHost = new JLabel();
 		lbPort = new JLabel();
 		cmbType = new JComboBox();
 		tfHost = new JTextField();
 		tfPort = new JTextField();
 
 		getContentPane().setLayout(new GridBagLayout());
 
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		lbUseProxy.setText("Use Proxy");
 		jPanel3.add(lbUseProxy);
 
 		chbUseProxy.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		chbUseProxy.setMargin(new Insets(0, 0, 0, 0));
 		jPanel3.add(chbUseProxy);
 
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.anchor = GridBagConstraints.WEST;
 		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
 		getContentPane().add(jPanel3, gridBagConstraints);
 
 		btnUpdate.setText("Update");
 		jPanel1.add(btnUpdate);
 
 		btnCancel.setText("Cancel");
 		jPanel1.add(btnCancel);
 
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 3;
 		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
 		gridBagConstraints.insets = new Insets(10, 0, 0, 0);
 		getContentPane().add(jPanel1, gridBagConstraints);
 
 		jPanel2.setLayout(new GridBagLayout());
 
 		jPanel2.setBorder(BorderFactory.createTitledBorder("Proxy Settings"));
 		lbType.setText("Type");
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 1;
 		jPanel2.add(lbType, gridBagConstraints);
 
 		lbHost.setText("Host name");
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 1;
 		jPanel2.add(lbHost, gridBagConstraints);
 
 		lbPort.setText("Port");
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 1;
 		jPanel2.add(lbPort, gridBagConstraints);
 
 		cmbType.setModel(new DefaultComboBoxModel(new String[] { "HTTP", "SOCKS" }));
 		cmbType.setMinimumSize(new Dimension(61, 18));
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 2;
 		gridBagConstraints.insets = new Insets(5, 10, 10, 5);
 		jPanel2.add(cmbType, gridBagConstraints);
 
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 2;
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.insets = new Insets(5, 5, 10, 5);
 		jPanel2.add(tfHost, gridBagConstraints);
 
 		final int tfPortHeight = (int)tfHost.getMinimumSize().getHeight();
 		tfPort.setMinimumSize(new Dimension(43, tfPortHeight));
 		tfPort.setPreferredSize(new Dimension(43, tfPortHeight));
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 2;
 		gridBagConstraints.insets = new Insets(5, 5, 10, 10);
 		jPanel2.add(tfPort, gridBagConstraints);
 
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.weightx = 1.0;
 		getContentPane().add(jPanel2, gridBagConstraints);
 
 		// add event listeners
 		btnUpdate.addActionListener(this);
 		btnCancel.addActionListener(this);
 		//cmbType.addItemListener(this);
 		chbUseProxy.addItemListener(this);
 
 		pack();
		setSize(new Dimension(400, 210));
 	} // </editor-fold>
 
 	private void initValues() {
 	    // initially set the last values as they are read from the
 	    // ProxyHandler. We will compare these to the final values
 	    // to see if anything changed.
 	    _lastUseProxy = false;
 	    _lastProxyHost = "";
 	    _lastProxyPort = "";
 	    _lastProxyType = "";
 
 	    Proxy p = ProxyHandler.getProxyServer();
 	    chbUseProxy.setSelected(true);
 	    cmbType.setEnabled(true);
 	    tfHost.setEnabled(true);
 	    tfPort.setEnabled(true);
 	    
 	    if ((p != null) && (p.type() != Proxy.Type.DIRECT)) {
 		_lastUseProxy = true;
 		if (p.type() == Proxy.Type.HTTP) {
 		    _lastProxyType = "HTTP";
 		    cmbType.setSelectedItem(_lastProxyType);
 		} else if (p.type() == Proxy.Type.SOCKS) {
 		    _lastProxyType = "SOCKS";
 		    cmbType.setSelectedItem(_lastProxyType);
 		}
 
 		InetSocketAddress address = (InetSocketAddress) p.address();
 		_lastProxyHost = address.getHostName();
 		_lastProxyPort = new Integer(address.getPort()).toString();
 	    } else {
 		chbUseProxy.setSelected(false);
 		cmbType.setEnabled(false);
 		tfHost.setEnabled(false);
 		tfPort.setEnabled(false);
 	    }
 	    tfHost.setText(_lastProxyHost);
 	    tfPort.setText(_lastProxyPort);
 
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void itemStateChanged(ItemEvent e) {
 		
         Object _actionObject = e.getSource();
 
         if (_actionObject instanceof JCheckBox)
         {
         	if (chbUseProxy.isSelected()) { // UseProxy is checked
             	// enable the setting panel
         		cmbType.setEnabled(true);
         		tfHost.setEnabled(true);
         		tfPort.setEnabled(true);
         	}
         	else
         	{// UseProxy is unchecked
         		// disable the setting panel
         		cmbType.setEnabled(false);
         		tfHost.setEnabled(false);
         		tfPort.setEnabled(false);
         	}
         }
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
         // TODO: Split each action into a separate private class so _btn and _actionObject are not needed:
 	public void actionPerformed(ActionEvent e) {
 		Object _actionObject = e.getSource();
 
 		// handle Button events
 		if (_actionObject instanceof JButton) {
 			JButton _btn = (JButton) _actionObject;
 
 			if (_btn == btnCancel) {
 				this.dispose();
 			} else if (_btn == btnUpdate) {
 				if (!updateProxyServer())
 					return;
 
 				this.dispose();
 			}
 		}
 	}
 
 	private boolean updateProxyServer() {
 		Proxy.Type proxyType = Proxy.Type.valueOf(cmbType.getSelectedItem().toString());
 
 		// If UseProxy is unchecked, that means NULL proxy sever
 		if (!chbUseProxy.isSelected()) {
 			tfHost.setText("");
 			tfPort.setText("");
 			// If Host or Port is empty, ProxyServer will be set to NULL
 		}
 		else { //UseProxy is checked
 			// Try if we can create a proxyServer, if not, report error
 			if (tfHost.getText().trim().equals("")) {
 				JOptionPane.showMessageDialog(this, "Host name is empty!", "Warning",
 				                              JOptionPane.INFORMATION_MESSAGE);
 				return false;
 			}
 	
 			int thePort;
 	
 			try {
 				Integer tmpInteger = new Integer(tfPort.getText().trim());
 				thePort = tmpInteger.intValue();
 			} catch (Exception exp) {
 				JOptionPane.showMessageDialog(this, "Port error!", "Warning",
 				                              JOptionPane.INFORMATION_MESSAGE);
 				return false;
 			}
 	
 			InetSocketAddress theAddress = new InetSocketAddress(tfHost.getText().trim(), thePort);
 	
 			try {
 				new Proxy(proxyType, theAddress);
 			} catch (Exception expProxy) {
 				JOptionPane.showMessageDialog(this, "Proxy server error!", "Warning",
 				                              JOptionPane.INFORMATION_MESSAGE);
 				return false;
 			}
 			//Yes, got valid input for a proxy server
 		}
 		
 		// Update the proxy server info 
 		CytoscapeInit.getProperties().setProperty(ProxyHandler.PROXY_HOST_PROPERTY_NAME, tfHost.getText().trim());
 		CytoscapeInit.getProperties().setProperty(ProxyHandler.PROXY_PORT_PROPERTY_NAME, tfPort.getText());
 		CytoscapeInit.getProperties()
 		             .setProperty(ProxyHandler.PROXY_TYPE_PROPERTY_NAME, cmbType.getSelectedItem().toString());
 		return saveProxyChangesAndTestWhenNeeded ();
 	}
 
     // Return true iff something with the proxy settings has changes
     // since this dialog was constructed:
     private boolean hasProxyValuesChanged () {
 	return ((_lastUseProxy != chbUseProxy.isSelected()) ||
 		(!_lastProxyType.equals (cmbType.getSelectedItem().toString())) ||
 		(!_lastProxyPort.equals (tfPort.getText())) ||
 		(!_lastProxyHost.equals (tfHost.getText().trim())));
     }
 
 
     // TODO: This method is using knowledge of how properties are loaded
     // and saved. A better approach is to add some property management
     // classes or methods that encapsulate this behavior. This also
     // applies to the PreferencesDialog.
 
     private boolean saveProxyChangesAndTestWhenNeeded () {
 
 	if (!hasProxyValuesChanged ()) {
 	    // nothing requires saving, but test the Proxy:
 	    return performProxyTest();
 	}
 	// To avoid saving potentially unwanted, temporary properties,
 	// we can't just save the existing in-memory
 	// properties. Instead, we must read the last saved properties
 	// into a new Properties object, add the proxy values, and
 	// save the Properties back out. If the user saved the in-memory
 	// properties, that will be fine also, since the proxy info
 	// has been updated there.
 
 	Properties inMemoryProps = CytoscapeInit.getProperties();
 	Properties propsToSave = new Properties();
 	// fill in propsToSave with the last read properties (NOT the
 	// existing in-memory properties):
 	CytoscapeInit.loadStaticProperties (PROPERTIES_FILE_NAME, propsToSave);
 	// Now add in-memory values to our Properties to save:
 	propsToSave.setProperty (ProxyHandler.PROXY_HOST_PROPERTY_NAME,
 				 inMemoryProps.getProperty(ProxyHandler.PROXY_HOST_PROPERTY_NAME,null));
 	propsToSave.setProperty (ProxyHandler.PROXY_PORT_PROPERTY_NAME,
 				 inMemoryProps.getProperty(ProxyHandler.PROXY_PORT_PROPERTY_NAME,null));
 	propsToSave.setProperty (ProxyHandler.PROXY_TYPE_PROPERTY_NAME,
 				 inMemoryProps.getProperty(ProxyHandler.PROXY_TYPE_PROPERTY_NAME,null));
 	// Now save the updated properties back out:
 	try {
 	    File file = CytoscapeInit.getConfigFile(PROPERTIES_FILE_NAME);
 	    FileOutputStream output = null;
 	    try {
 		output = new FileOutputStream(file);
 		propsToSave.store(output, "Cytoscape Property File");
 		CyLogger.getLogger().info("wrote Cytoscape properties file to: " + file.getAbsolutePath());
 	    }
 	    finally {
 		if (output != null) {
 		    output.close();
 		}
 	    }
 	} catch (Exception ex) {
 	    CyLogger.getLogger().error("Could not write the " + PROPERTIES_FILE_NAME + " file!", ex);
 	}
 	// Even if the properties were not sucessfully written, signal
 	// they changed in memory. The ProxyHandler will use this
 	// event to update the real Proxy:
 	Cytoscape.firePropertyChange(Cytoscape.PREFERENCES_UPDATED, null, null);
 	return performProxyTest ();
     }
 
     private boolean performProxyTest () {
 	if (testNetConnection (ProxyHandler.getProxyServer(), 2000)) {
 	    JOptionPane.showMessageDialog (this,
 					   "We successfully connected to " + WELL_KNOWN_URL + " using your new settings!");
 	    return true;
 	} else {
 	    JOptionPane.showMessageDialog (this,
 					   "<HTML>We could <STRONG>not connect</STRONG> to " + WELL_KNOWN_URL + " using your new settings.");
 	    return false;
 	}
     }
 
     // Attempt to connect to a well known site (e.g., google.com):
     // return true if we successfully connected to the Internet to
     // a well know site. Return false otherwise.
     private boolean testNetConnection(final Proxy proxy, final int ms_timeout) {
         try {
             final URL           u  = new URL(WELL_KNOWN_URL);
             URLConnection uc = null;
 	    if (proxy != null) {
 		uc = u.openConnection(proxy);
 	    } else {
 		uc = u.openConnection();
 	    }
             uc.setAllowUserInteraction(false);
             uc.setUseCaches(false); // don't use a cached page
             uc.setConnectTimeout(ms_timeout);
             uc.connect();
             return true;
         } catch (IOException e) {
             // we couldn't connect:
             return false;
         }
     }
 
 
 }
