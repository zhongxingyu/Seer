 
 
 package org.vpac.grisu.client.view.swing.login;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.border.EtchedBorder;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.client.control.utils.ClientPropertiesManager;
 
 import au.org.arcs.jcommons.utils.HttpProxyManager;
 
 public class HttpProxyPanel extends JPanel implements ItemListener {
 
 	private static final long serialVersionUID = 1L;
 	private JLabel jLabel = null;
 	private JLabel jLabel1 = null;
 	private JTextField serverTextField = null;
 	private JLabel jLabel2 = null;
 	private JTextField portTextField = null;
 	private JTextField usernameTextField = null;
 	private JCheckBox advancedCheckBox = null;
 	private JLabel jLabel3 = null;
 	private JPasswordField proxyPasswordField = null;
 	
 	/**
 	 * This is the default constructor
 	 */
 	public HttpProxyPanel() {
 		super();
 		initialize();
 		HttpProxyManager.setDefaultHttpProxy();
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
 		gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints8.gridy = 3;
 		gridBagConstraints8.weightx = 1.0;
 		gridBagConstraints8.gridwidth = 3;
 		gridBagConstraints8.insets = new Insets(0, 10, 15, 15);
 		gridBagConstraints8.gridx = 1;
 		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
 		gridBagConstraints7.gridx = 0;
 		gridBagConstraints7.insets = new Insets(0, 15, 15, 5);
 		gridBagConstraints7.anchor = GridBagConstraints.EAST;
 		gridBagConstraints7.gridy = 3;
 		jLabel3 = new JLabel();
 		jLabel3.setText("Http proxy password:");
 		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
 		gridBagConstraints6.gridx = 0;
 		gridBagConstraints6.insets = new Insets(10, 10, 10, 0);
 		gridBagConstraints6.gridwidth = 4;
 		gridBagConstraints6.anchor = GridBagConstraints.WEST;
 		gridBagConstraints6.weightx = 1.0;
 		gridBagConstraints6.gridy = 0;
 		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
 		gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints5.gridy = 2;
 		gridBagConstraints5.weightx = 1.0;
 		gridBagConstraints5.gridwidth = 3;
 		gridBagConstraints5.insets = new Insets(0, 10, 10, 15);
 		gridBagConstraints5.gridx = 1;
 		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
 		gridBagConstraints4.fill = GridBagConstraints.BOTH;
 		gridBagConstraints4.gridy = 1;
 		gridBagConstraints4.weightx = 0.5;
 		gridBagConstraints4.insets = new Insets(15, 10, 10, 15);
 		gridBagConstraints4.gridx = 3;
 		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
 		gridBagConstraints3.gridx = 2;
 		gridBagConstraints3.insets = new Insets(15, 0, 10, 5);
 		gridBagConstraints3.weightx = 0.0;
 		gridBagConstraints3.gridy = 1;
 		jLabel2 = new JLabel();
 		jLabel2.setText("Port:");
 		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
 		gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints2.gridy = 1;
 		gridBagConstraints2.weightx = 1.0;
 		gridBagConstraints2.insets = new Insets(15, 10, 10, 10);
 		gridBagConstraints2.gridx = 1;
 		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
 		gridBagConstraints1.gridx = 0;
 		gridBagConstraints1.anchor = GridBagConstraints.EAST;
 		gridBagConstraints1.insets = new Insets(0, 15, 10, 5);
 		gridBagConstraints1.gridy = 2;
 		jLabel1 = new JLabel();
 		jLabel1.setText("Http proxy username:");
 		GridBagConstraints gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.anchor = GridBagConstraints.EAST;
 		gridBagConstraints.insets = new Insets(15, 15, 10, 5);
 		gridBagConstraints.gridy = 1;
 		jLabel = new JLabel();
 		jLabel.setText("Http proxy server:");
 		this.setSize(501, 207);
 		this.setLayout(new GridBagLayout());
 		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
 		this.add(jLabel, gridBagConstraints);
 		this.add(jLabel1, gridBagConstraints1);
 		this.add(getServerTextField(), gridBagConstraints2);
 		this.add(jLabel2, gridBagConstraints3);
 		this.add(getPortTextField(), gridBagConstraints4);
 		this.add(getUsernameTextField(), gridBagConstraints5);
 		this.add(getAdvancedCheckBox(), gridBagConstraints6);
 		this.add(jLabel3, gridBagConstraints7);
 		this.add(getProxyPasswordField(), gridBagConstraints8);
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.anchor = GridBagConstraints.EAST;
 		gbc.insets = new Insets(0, 0, 10, 10);
 		gbc.gridx = 3;
 		gbc.gridy = 4;
 		add(getButton(), gbc);
 		
 		boolean show;
 		try {
 			show = "true".equals(ClientPropertiesManager.getClientConfiguration().getProperty("httpProxy"));
 			if ( show ) {
 				getAdvancedCheckBox().setSelected(true);
 			}
 			showHttpProxy(show);
 		} catch (ConfigurationException e) {
 			getProxyPasswordField().setVisible(false);
 			getUsernameTextField().setVisible(false);
 			getServerTextField().setVisible(false);
 			getPortTextField().setVisible(false);
 			jLabel.setVisible(false);
 			jLabel1.setVisible(false);
 			jLabel2.setVisible(false);
 			jLabel3.setVisible(false);
 		}
 	}
 
 	/**
 	 * This method initializes serverTextField	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getServerTextField() {
 		if (serverTextField == null) {
 			serverTextField = new JTextField();
 			serverTextField.addKeyListener(new KeyAdapter() {
 				public void keyTyped(final KeyEvent e) {
 					fireHttpProxyValuesChanged();
 				}
 			});
 			try {
 				// set proxy server from last time
				String httpProxyServer = (String)ClientPropertiesManager.getClientConfiguration().getProperty("httpProxyServer");
 				if ( httpProxyServer != null && !"".equals(httpProxyServer) )
 					serverTextField.setText(httpProxyServer);
 			} catch (Exception e) {
 				// not really important
 			} 
 
 		}
 		return serverTextField;
 	}
 
 	/**
 	 * This method initializes portTextField	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getPortTextField() {
 		if (portTextField == null) {
 			portTextField = new JTextField();
 //			portTextField.addKeyListener(new KeyAdapter() {
 //				public void keyTyped(final KeyEvent e) {
 //					fireHttpProxyValuesChanged();
 //				}
 //			});
 			try {
 				// set proxy server from last time
				String httpProxyPort = (String)ClientPropertiesManager.getClientConfiguration().getProperty("httpProxyPort");
 				if ( httpProxyPort != null && !"".equals(httpProxyPort) )
 					portTextField.setText(httpProxyPort);
 			} catch (Exception e) {
 				// not really important
 			} 
 		}
 		return portTextField;
 	}
 
 	/**
 	 * This method initializes usernameTextField	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getUsernameTextField() {
 		if (usernameTextField == null) {
 			usernameTextField = new JTextField();
 			usernameTextField.addKeyListener(new KeyAdapter() {
 				public void keyTyped(final KeyEvent e) {
 					fireHttpProxyValuesChanged();
 				}
 			});
 			try {
 				// set proxy server from last time
				String httpProxyUsername = (String)ClientPropertiesManager.getClientConfiguration().getProperty("httpProxyUsername");
 				if ( httpProxyUsername != null && !"".equals(httpProxyUsername) )
 					usernameTextField.setText(httpProxyUsername);
 			} catch (Exception e) {
 				// not really important
 			} 
 		}
 		return usernameTextField;
 	}
 
 	/**
 	 * This method initializes advancedCheckBox	
 	 * 	
 	 * @return javax.swing.JCheckBox	
 	 */
 	private JCheckBox getAdvancedCheckBox() {
 		if (advancedCheckBox == null) {
 			advancedCheckBox = new JCheckBox();
 			advancedCheckBox.setText("Advanced connection properties");
 			advancedCheckBox.addItemListener(this);
 
 		}
 		return advancedCheckBox;
 	}
 
 	/**
 	 * This method initializes proxyPasswordField	
 	 * 	
 	 * @return javax.swing.JPasswordField	
 	 */
 	private JPasswordField getProxyPasswordField() {
 		if (proxyPasswordField == null) {
 			proxyPasswordField = new JPasswordField();
 			proxyPasswordField.addKeyListener(new KeyAdapter() {
 				public void keyTyped(final KeyEvent e) {
 					fireHttpProxyValuesChanged();
 				}
 			});
 		}
 		return proxyPasswordField;
 	}
 	
 	public String getProxyServer() {
 		if ( getAdvancedCheckBox().isSelected() ) {
 			return getServerTextField().getText();
 		} else { 
 			return null;
 		}
 	}
 	
 	public int getProxyPort() {
 		if ( getAdvancedCheckBox().isSelected() ) {
 			try {
 				int port = new Integer(getPortTextField().getText());
 				return port;
 			} catch (NumberFormatException e) {
 				return -1;
 			}
 		} else {
 			return -1;
 		}
 	}
 	
 	public String getProxyUsername() {
 		if ( getAdvancedCheckBox().isSelected() ) {
 			return getUsernameTextField().getText();
 		} else {
 			return null;
 		}
 	}
 
 	public char[] getProxyPassword() {
 		if ( getAdvancedCheckBox().isSelected() ) {
 			return getProxyPasswordField().getPassword();
 		} else {
 			return null;
 		}
 	}
 	
 	private void showHttpProxy(boolean show) {
 		
 		if ( show ) {
 			getProxyPasswordField().setVisible(true);
 			getUsernameTextField().setVisible(true);
 			getServerTextField().setVisible(true);
 			getPortTextField().setVisible(true);
 			jLabel.setVisible(true);
 			jLabel1.setVisible(true);
 			jLabel2.setVisible(true);
 			jLabel3.setVisible(true);
 			getButton().setVisible(true);
 		} else {
 			getProxyPasswordField().setVisible(false);
 			getUsernameTextField().setVisible(false);
 			getServerTextField().setVisible(false);
 			getPortTextField().setVisible(false);
 			jLabel.setVisible(false);
 			jLabel1.setVisible(false);
 			jLabel2.setVisible(false);
 			jLabel3.setVisible(false);
 			getButton().setVisible(false);
 		}
 		
 	}
 
 	public void itemStateChanged(ItemEvent e) {
 		
 		showHttpProxy( e.getStateChange() == ItemEvent.SELECTED );
 		fireHttpProxyValuesChanged();
 	}
 	
 	// ---------------------------------------------------------------------------------------
 	// Event stuff (MountPoints)
 	private Vector<HttpProxyPanelListener> httpProxyPanelListener;
 	private JButton button;
 
 	private void fireHttpProxyValuesChanged() {
 		// if we have no mountPointsListeners, do nothing...
 		if (httpProxyPanelListener != null && !httpProxyPanelListener.isEmpty()) {
 
 			// make a copy of the listener list in case
 			// anyone adds/removes mountPointsListeners
 			Vector targets;
 			synchronized (this) {
 				targets = (Vector) httpProxyPanelListener.clone();
 			}
 
 			// walk through the listener list and
 			// call the gridproxychanged method in each
 			Enumeration e = targets.elements();
 			while (e.hasMoreElements()) {
 				HttpProxyPanelListener l = (HttpProxyPanelListener) e.nextElement();
 				try {
 					l.httpProxyValueChanged();
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 
 	// register a listener
 	synchronized public void addHttpProxyPanelListener(HttpProxyPanelListener l) {
 		if (httpProxyPanelListener == null)
 			httpProxyPanelListener = new Vector();
 		httpProxyPanelListener.addElement(l);
 	}
 
 	// remove a listener
 	synchronized public void removeHttpProxyPanelListener(HttpProxyPanelListener l) {
 		if (httpProxyPanelListener == null) {
 			httpProxyPanelListener = new Vector<HttpProxyPanelListener>();
 		}
 		httpProxyPanelListener.removeElement(l);
 	}
 	
 	
 	private JButton getButton() {
 		if (button == null) {
 			button = new JButton("Apply");
 			button.setVisible(false);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					
 					String host = getProxyServer();
 					int port = getProxyPort();
 					
 					if ( host != null ) {
 						if ( StringUtils.isNotBlank(getProxyUsername()) ) {
 							String username = getProxyUsername();
 							char[] password = getProxyPassword();
 							HttpProxyManager.setHttpProxy(host, port, username, password);
 						} else {
 							HttpProxyManager.setHttpProxy(host, port, null, null);
 						}
 					}
 					fireHttpProxyValuesChanged();
 				}
 			});
 		}
 		return button;
 	}
 }  //  @jve:decl-index=0:visual-constraint="10,10"
