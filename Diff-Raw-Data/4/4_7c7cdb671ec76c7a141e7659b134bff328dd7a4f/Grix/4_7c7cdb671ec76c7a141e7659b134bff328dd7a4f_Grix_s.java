 /* Copyright 2006 VPAC
  * 
  * This file is part of grix.
  * Grix is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * any later version.
 
  * Grix is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with Grix; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package org.vpac.grix.view.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.apache.log4j.Logger;
 import org.globus.common.CoGProperties;
 import org.globus.gsi.GlobusCredential;
 import org.vpac.common.model.GlobusLocations;
 import org.vpac.common.model.gridproxy.GlobusProxy;
 import org.vpac.common.model.gridproxy.GridProxy;
 import org.vpac.common.model.gridproxy.LocalProxy;
 import org.vpac.common.view.swing.gridproxy.GridProxyStatusPanel;
 import org.vpac.common.view.swing.messagePanel.MessagePanel;
 import org.vpac.common.view.swing.messagePanel.SimpleMessagePanel;
 import org.vpac.grix.control.utils.DateHelper;
 import org.vpac.grix.control.utils.GrixProperty;
 import org.vpac.grix.control.utils.UserProperty;
 import org.vpac.grix.model.certificate.Certificate;
 import org.vpac.grix.view.swing.certificate.CertificateEvent;
 import org.vpac.grix.view.swing.certificate.CertificatePanel;
 import org.vpac.grix.view.swing.certificate.CertificateStatusListener;
 import org.vpac.grix.view.swing.common.GridProxyDialog;
 import org.vpac.grix.view.swing.tools.OptionsDialog;
 import org.vpac.grix.view.swing.vomrs.VOPanelShlix;
 import org.vpac.security.light.CredentialHelpers;
 import org.vpac.security.light.Init;
 import org.vpac.security.light.control.CertificateFiles;
 import org.vpac.security.light.control.VomsesFiles;
 import org.vpac.security.light.view.swing.ProxyInitListener;
 import org.vpac.security.light.view.swing.proxyInit.GenericProxyCreationPanel;
 import org.vpac.voms.control.LocalVomses;
 import org.vpac.voms.model.proxy.NoVomsProxyException;
 import org.vpac.voms.model.proxy.VomsProxy;
 
 import au.org.arcs.jcommons.constants.ArcsEnvironment;
 import au.org.arcs.jcommons.dependencies.Dependency;
 import au.org.arcs.jcommons.dependencies.DependencyManager;
 import au.org.arcs.jcommons.utils.ArcsSecurityProvider;
 import au.org.arcs.jcommons.utils.JythonHelpers;
 
 public class Grix implements CertificateStatusListener, ProxyInitListener {
 
 	public static final String GRIX_VERSION = "v1.2.2";
 
 	static final Logger myLogger = Logger.getLogger(Grix.class.getName());
 
 	private static ResourceBundle messages = ResourceBundle.getBundle(
 			"SwingViewMessagesBundle", java.util.Locale.getDefault()); // @jve:decl-index=0:
 
 	private JFrame jFrame = null; // @jve:decl-index=0:visual-constraint="0,51"
 
 	private JPanel jContentPane = null;
 
 	private JMenuBar jJMenuBar = null;
 
 	private JMenu fileMenu = null;
 
 	private JMenu toolsMenu = null;
 
 	private JMenu helpMenu = null;
 
 	private JMenuItem exitMenuItem = null;
 
 	private JMenuItem aboutMenuItem = null;
 
 	private JMenuItem cutMenuItem = null;
 
 	private JMenuItem copyMenuItem = null;
 
 	private JMenuItem pasteMenuItem = null;
 
 	private JMenuItem saveMenuItem = null;
 
 	private JMenuItem optionsMenuItem = null;
 
 	private JDialog aboutDialog = null; // @jve:decl-index=0:visual-constraint="954,106"
 
 	private JPanel aboutContentPane = null;
 
 	private JLabel aboutVersionLabel = null;
 
 	private JTabbedPane jTabbedPane = null;
 
 	private CertificatePanel certificatePanel = null;
 
 	private JPanel vomrsPanel = null;
 
 	private GenericProxyCreationPanel authenticationPanel = null;
 
 	private GridProxyDialog gridProxyDialog = null;
 
 	private JDialog optionsDialog = null;
 
 	private JPanel gridProxyStatusPanel = null;
 
 	private Color base_color = null;
 
 	private Color lighter_color = null;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		DependencyManager.showDownloadDialog = true;
 		
 		Map<Dependency, String> dependencies = new HashMap<Dependency, String>();
 
 		dependencies.put(Dependency.BOUNCYCASTLE, "jdk15-143");
 //		dependencies.put(Dependency.ARCSGSI, "1.1");
 
 		DependencyManager.addDependencies(dependencies, ArcsEnvironment
 				.getArcsCommonJavaLibDirectory());
 
 		JythonHelpers.setJythonCachedir();
 		CoGProperties.getDefault().setProperty(CoGProperties.ENFORCE_SIGNING_POLICY, "false");
 
 		java.security.Security.addProvider(new ArcsSecurityProvider());
 
 		java.security.Security.setProperty("ssl.TrustManagerFactory.algorithm",
 				"TrustAllCertificates");
 
 		Init.initBouncyCastle();
		
 		final SplashScreen screen = new SplashScreen();
 		screen.setVisible(true);
 
 		try {
 			CertificateFiles.copyCACerts();
 		} catch (Exception e) {
 			myLogger.error(e);
 		}
 
 		try {
 			VomsesFiles.copyVomses();
 		} catch (Exception e) {
 			myLogger.error(e);
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 
 				try {
 					UIManager.setLookAndFeel(UIManager
 							.getSystemLookAndFeelClassName());
 					// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 				} catch (Exception e) {
 					// try {
 					// UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
 					// } catch (Exception e1) {
 					// // TODO Auto-generated catch block
 					// //e1.printStackTrace();
 					// }
 				}
 				try {
 					// try whether the current proxy is a VomsProxy
 					if (LocalProxy.getProxyFile().exists()) {
 
 						try {
 							VomsProxy vomsProxy = new VomsProxy(LocalProxy
 									.getProxyFile());
 							LocalProxy.setDefaultProxy(vomsProxy);
 						} catch (NoVomsProxyException e) {
 							// TODO Auto-generated catch block
 							// e.printStackTrace();
 							myLogger.debug(e);
 							// thats ok, but make sure that there is a
 							// LocalProxy.getDefaultProxy() object
 							LocalProxy.setDefaultProxy(new GlobusProxy(
 									LocalProxy.getProxyFile()));
 						}
 					} else {
 						LocalProxy.setDefaultProxy(new GlobusProxy(LocalProxy
 								.getProxyFile()));
 					}
 				} catch (IOException ioe) {
 					myLogger.error(ioe);
 					// ioe.printStackTrace();
 				}
 
 				try {
 
 					Grix application = new Grix();
 					application.initIcons();
 
 					application.getJFrame().setVisible(true);
 
 					screen.dispose();
 
 					if (LocalProxy.getDefaultProxy().getStatus() == GridProxy.INITIALIZED) {
 						LocalVomses.getLocalVomses().getVomses();
 					}
 					LocalProxy.addStatusListener(LocalVomses.getLocalVomses());
 
 					if (GlobusLocations.defaultLocations().getUserCert()
 							.exists()
 							&& GlobusLocations.defaultLocations().getUserKey()
 									.exists()
 							&& !LocalProxy.getDefaultProxy().isValid()
 							&& "yes".equals(UserProperty
 									.getProperty("CREATE_PROXY_AT_STARTUP"))) {
 						// display proxy window
 						application.getGridProxyDialog().setVisible(true);
 					}
 
 				} catch (Exception e) {
 
 					JOptionPane.showMessageDialog(null,
 							"Could not start Grix: " + e.getLocalizedMessage(),
 							"Startup error", JOptionPane.ERROR_MESSAGE);
 
 					System.exit(1);
 				}
 			}
 		});
 	}
 
 	/**
 	 * This method initializes jTabbedPane
 	 * 
 	 * @return javax.swing.JTabbedPane
 	 */
 	private JTabbedPane getJTabbedPane() {
 		if (jTabbedPane == null) {
 			jTabbedPane = new JTabbedPane();
 			jTabbedPane.setTabPlacement(JTabbedPane.TOP);
 			jTabbedPane.addTab("Certificate", getCertificatePanel());
 
 			jTabbedPane.addTab("Authentication", getAuthenticationPanel());
 			getAuthenticationPanel().addProxyListener(this);
 
 			jTabbedPane.addTab("VOs", null, getVomrsPanel(), null);
 
 			if ((GlobusLocations.defaultLocations().getUserCert().exists() && GlobusLocations
 					.defaultLocations().getUserKey().exists())
 			// better not ||LocalProxy.getDefaultProxy().isValid()
 			) {
 
 				// if cert expired or is expiring
 				try {
 					Certificate cert = new Certificate(GlobusLocations
 							.defaultLocations().getUserCert());
 					DateFormat df = DateHelper.getDateFormat();
 					Date enddate = df.parse(cert.getEnddate());
 
 					if (enddate.before(new Date())) {
 						jTabbedPane.setIconAt(0, redLight);
 					} else {
 						Calendar rightNow = Calendar.getInstance();
 						rightNow.add(Calendar.MONTH, 1);
 						Date inOneMonth = rightNow.getTime();
 						if (enddate.before(inOneMonth)) {
 							jTabbedPane.setIconAt(0, yellowLight);
 						}
 					}
 
 				} catch (Exception e) {
 					// do nothing
 					e.printStackTrace();
 				}
 
 				// jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("VOs"),
 				// true);
 				// jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("LocalProxy"),
 				// true);
 				// jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("MyProxy"),
 				// true);
 				if ("Authentication".equals(UserProperty
 						.getProperty("TAB_AT_STARTUP")))
 					jTabbedPane.setSelectedIndex(jTabbedPane
 							.indexOfTab("MyProxy"));
 				// else if ("LocalProxy".equals(UserProperty
 				// .getProperty("TAB_AT_STARTUP")))
 				// jTabbedPane.setSelectedIndex(jTabbedPane
 				// .indexOfTab("LocalProxy"));
 				else if ("VO"
 						.equals(UserProperty.getProperty("TAB_AT_STARTUP")))
 					jTabbedPane.setSelectedIndex(jTabbedPane.indexOfTab("VOs"));
 				else
 					jTabbedPane.setSelectedIndex(jTabbedPane
 							.indexOfTab("Certificate"));
 
 				// } else {
 				// jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("VOs"),
 				// false);
 				// jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("LocalProxy"),
 				// false);
 				// jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("MyProxy"),
 				// false);
 			}
 
 		}
 		return jTabbedPane;
 	}
 
 	/**
 	 * This method initializes certificatePanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private CertificatePanel getCertificatePanel() {
 		if (certificatePanel == null) {
 			certificatePanel = new CertificatePanel(this);
 			// certificatePanel.setLayout(new GridBagLayout());
 		}
 		return certificatePanel;
 	}
 
 	private GenericProxyCreationPanel getAuthenticationPanel() {
 
 		if (authenticationPanel == null) {
 
 			boolean useShib = true;
 			String disableShib = GrixProperty.getString("disable.shibboleth");
 			if ("yes".equals(disableShib)) {
 				useShib = false;
 			}
 			
 			String shibUrl = System.getProperty("shibUrl");
 
 			authenticationPanel = new GenericProxyCreationPanel(useShib, true,
 					true, true, shibUrl);
 		}
 		return authenticationPanel;
 
 	}
 
 	// private ProxyPanel getProxyPanel() {
 	// if (proxyPanel == null) {
 	// proxyPanel = new ProxyPanel();
 	// }
 	// return proxyPanel;
 	// }
 	//
 	// private VomsProxyPanelHolder getLocalGridProxyPanel() {
 	// if (vomsProxyPanel == null) {
 	// vomsProxyPanel = new VomsProxyPanelHolder();
 	// }
 	// return vomsProxyPanel;
 	// }
 
 	/**
 	 * This method initializes vomrsPanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getVomrsPanel() {
 		if (vomrsPanel == null) {
 			try {
 				vomrsPanel = new VOPanelShlix();
 				vomrsPanel.setEnabled(true);
 			} catch (Exception e) {
 				myLogger.error(e);
 				// e.printStackTrace();
 				vomrsPanel = new SimpleMessagePanel(messages
 						.getString("error.details")
 						+ "<p>" + e.getStackTrace() + "</p>", Color.white);
 			}
 
 		}
 		return vomrsPanel;
 	}
 
 	// private JPanel getSlcsPanel() {
 	// if (slcsPanel == null ) {
 	//			
 	// // try {
 	// slcsPanel = new SlcsPanel();
 	// // } catch (ShibbolethException e) {
 	// // slcsPanel = new SimpleMessagePanel(messages
 	// // .getString("error.details")
 	// // + "<p>" + e.getStackTrace() + "</p>", Color.white);
 	// // }
 	//			
 	// }
 	// return slcsPanel;
 	// }
 
 	// private JPanel getAuthToolPanel() {
 	// if ( authToolPanel == null ) {
 	// authToolPanel = new AuthToolPanel();
 	// }
 	// return authToolPanel;
 	// }
 
 	/**
 	 * This method initializes gridProxyStatusPanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getGridProxyStatusPanel() {
 		if (gridProxyStatusPanel == null) {
 			gridProxyStatusPanel = new GridProxyStatusPanel();
 			gridProxyStatusPanel.setBackground(getBaseColor());
 		}
 		return gridProxyStatusPanel;
 	}
 
 	/**
 	 * This method initializes jFrame
 	 * 
 	 * @return javax.swing.JFrame
 	 */
 	private JFrame getJFrame() {
 		if (jFrame == null) {
 			jFrame = new JFrame();
 			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			jFrame.setJMenuBar(getJJMenuBar());
 			jFrame.setSize(764, 730);
 			jFrame.setContentPane(getJContentPane());
 			jFrame.setTitle("Grix " + GRIX_VERSION);
 		}
 		return jFrame;
 	}
 
 	/**
 	 * This method initializes jContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
 			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
 			gridBagConstraints1.weightx = 1.0;
 			gridBagConstraints1.gridx = 0;
 			GridBagConstraints gridBagConstraints = new GridBagConstraints();
 			gridBagConstraints.fill = GridBagConstraints.BOTH;
 			gridBagConstraints.gridy = 1;
 			gridBagConstraints.weightx = 1.0;
 			gridBagConstraints.weighty = 1.0;
 			gridBagConstraints.gridx = 0;
 			jContentPane = new JPanel();
 			jContentPane.setLayout(new GridBagLayout());
 			jContentPane.add(getJTabbedPane(), gridBagConstraints);
 			if ("top".equals(UserProperty
 					.getProperty("SHOW_GRID_PROXY_STATUS_PANEL"))) {
 				gridBagConstraints1.gridy = 0;
 				jContentPane
 						.add(getGridProxyStatusPanel(), gridBagConstraints1);
 			} else if ("bottom".equals(UserProperty
 					.getProperty("SHOW_GRID_PROXY_STATUS_PANEL"))) {
 				gridBagConstraints1.gridy = 2;
 				jContentPane
 						.add(getGridProxyStatusPanel(), gridBagConstraints1);
 			}
 		}
 		return jContentPane;
 	}
 
 	private GridProxyDialog getGridProxyDialog() {
 		if (gridProxyDialog == null) {
 			gridProxyDialog = new GridProxyDialog(this.getJFrame());
 			// gridProxyDialog.setLayout(new BorderLayout());
 		}
 		return gridProxyDialog;
 	}
 
 	private JDialog getOptionsDialog() {
 		if (optionsDialog == null) {
 			optionsDialog = new OptionsDialog(this.getJFrame());
 		}
 		return optionsDialog;
 	}
 
 	/**
 	 * This method initializes jJMenuBar
 	 * 
 	 * @return javax.swing.JMenuBar
 	 */
 	private JMenuBar getJJMenuBar() {
 		if (jJMenuBar == null) {
 			jJMenuBar = new JMenuBar();
 			jJMenuBar.add(getFileMenu());
 			// jJMenuBar.add(getToolsMenu());
 			jJMenuBar.add(getHelpMenu());
 		}
 		return jJMenuBar;
 	}
 
 	/**
 	 * This method initializes jMenu
 	 * 
 	 * @return javax.swing.JMenu
 	 */
 	private JMenu getFileMenu() {
 		if (fileMenu == null) {
 			fileMenu = new JMenu();
 			fileMenu.setText("File");
 			// fileMenu.add(getSaveMenuItem());
 			fileMenu.add(getExitMenuItem());
 		}
 		return fileMenu;
 	}
 
 	/**
 	 * This method initializes jMenu
 	 * 
 	 * @return javax.swing.JMenu
 	 */
 	private JMenu getToolsMenu() {
 		if (toolsMenu == null) {
 			toolsMenu = new JMenu();
 			toolsMenu.setText("Tools");
 			toolsMenu.setActionCommand("Tools");
 			// toolsMenu.add(getGridProxyMenuItem());
 			toolsMenu.add(getOptionsMenuItem());
 			// toolsMenu.add(getCutMenuItem());
 			// toolsMenu.add(getCopyMenuItem());
 			// toolsMenu.add(getPasteMenuItem());
 		}
 		return toolsMenu;
 	}
 
 	/**
 	 * This method initializes jMenu
 	 * 
 	 * @return javax.swing.JMenu
 	 */
 	private JMenu getHelpMenu() {
 		if (helpMenu == null) {
 			helpMenu = new JMenu();
 			helpMenu.setText("Help");
 			helpMenu.add(getAboutMenuItem());
 		}
 		return helpMenu;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getExitMenuItem() {
 		if (exitMenuItem == null) {
 			exitMenuItem = new JMenuItem();
 			exitMenuItem.setText("Exit");
 			exitMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					System.exit(0);
 				}
 			});
 		}
 		return exitMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getAboutMenuItem() {
 		if (aboutMenuItem == null) {
 			aboutMenuItem = new JMenuItem();
 			aboutMenuItem.setText("About");
 			aboutMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					JDialog aboutDialog = getAboutDialog();
 					aboutDialog.pack();
 					Point loc = getJFrame().getLocation();
 					loc.translate(20, 20);
 					aboutDialog.setLocation(loc);
 					aboutDialog.setVisible(true);
 				}
 			});
 		}
 		return aboutMenuItem;
 	}
 
 	/**
 	 * This method initializes aboutDialog
 	 * 
 	 * @return javax.swing.JDialog
 	 */
 	private JDialog getAboutDialog() {
 		if (aboutDialog == null) {
 			aboutDialog = new JDialog(getJFrame(), true);
 			aboutDialog.setTitle("About");
 			aboutDialog.setSize(new Dimension(402, 365));
 			aboutDialog.setContentPane(getAboutContentPane());
 		}
 		return aboutDialog;
 	}
 
 	/**
 	 * This method initializes aboutContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getAboutContentPane() {
 		if (aboutContentPane == null) {
 			aboutContentPane = new JPanel();
 			aboutContentPane.setLayout(new BorderLayout());
 			aboutContentPane.setPreferredSize(new Dimension(300, 300));
 			// aboutContentPane.add(getAboutVersionLabel(),
 			// BorderLayout.CENTER);
 			try {
 				aboutContentPane.add(new SimpleMessagePanel(MessagePanel
 						.getHTMLDocument("about"), Color.white),
 						BorderLayout.CENTER);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				// e.printStackTrace();
 				myLogger.error(e);
 				aboutContentPane.add(getAboutVersionLabel(),
 						BorderLayout.CENTER);
 			}
 		}
 		return aboutContentPane;
 	}
 
 	/**
 	 * This method initializes aboutVersionLabel
 	 * 
 	 * @return javax.swing.JLabel
 	 */
 	private JLabel getAboutVersionLabel() {
 		if (aboutVersionLabel == null) {
 			aboutVersionLabel = new JLabel();
 			aboutVersionLabel.setText("Grix " + GRIX_VERSION);
 			aboutVersionLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		}
 		return aboutVersionLabel;
 	}
 
 	// /**
 	// * This method initializes jMenuItem
 	// *
 	// * @return javax.swing.JMenuItem
 	// */
 	// private JMenuItem getGridProxyMenuItem() {
 	// if (gridProxyMenuItem == null) {
 	// gridProxyMenuItem = new JMenuItem();
 	// gridProxyMenuItem.setText("Grid proxy");
 	// gridProxyMenuItem
 	// .addActionListener(new java.awt.event.ActionListener() {
 	// public void actionPerformed(java.awt.event.ActionEvent e) {
 	// getGridProxyDialog().setVisible(true);
 	// }
 	// });
 	// }
 	// return gridProxyMenuItem;
 	// }
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getOptionsMenuItem() {
 		if (optionsMenuItem == null) {
 			optionsMenuItem = new JMenuItem();
 			optionsMenuItem.setText("Preferences");
 			optionsMenuItem
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							getOptionsDialog().setVisible(true);
 						}
 					});
 		}
 		return optionsMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getCutMenuItem() {
 		if (cutMenuItem == null) {
 			cutMenuItem = new JMenuItem();
 			cutMenuItem.setText("Cut");
 			cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
 					Event.CTRL_MASK, true));
 		}
 		return cutMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getCopyMenuItem() {
 		if (copyMenuItem == null) {
 			copyMenuItem = new JMenuItem();
 			copyMenuItem.setText("Copy");
 			copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
 					Event.CTRL_MASK, true));
 		}
 		return copyMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getPasteMenuItem() {
 		if (pasteMenuItem == null) {
 			pasteMenuItem = new JMenuItem();
 			pasteMenuItem.setText("Paste");
 			pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
 					Event.CTRL_MASK, true));
 		}
 		return pasteMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getSaveMenuItem() {
 		if (saveMenuItem == null) {
 			saveMenuItem = new JMenuItem();
 			saveMenuItem.setText("Save");
 			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 					Event.CTRL_MASK, true));
 		}
 		return saveMenuItem;
 	}
 
 	public static ResourceBundle getMessages() {
 		return messages;
 	}
 
 	public void statusChanged(CertificateEvent event) {
 
 		// try {
 		// if (CertificatePanel.CERT_PRESENT_EXPORTED
 		// .equals(event.getStatus())
 		// || CertificatePanel.CERT_PRESENT_NOT_EXPORTED.equals(event
 		// .getStatus())
 		// || CertificatePanel.RENEW_READY_ON_CA_SERVER.equals(event
 		// .getStatus())
 		// || CertificatePanel.RENEW_REQUEST_CREATED.equals(event
 		// .getStatus())
 		// || CertificatePanel.RENEW_REQUESTED.equals(event
 		// .getStatus())
 		// || CertificatePanel.RENEW_REQUEST_CREATED.equals(event.getStatus())
 		// || CertificatePanel.CERT_EXPIRING.equals(event.getStatus())
 		// ) {
 		// if ( CertificatePanel.CERT_EXPIRING.equals(event.getStatus()) ) {
 		// getJTabbedPane().setIconAt(0, yellowLight);
 		// } else {
 		// getJTabbedPane().setIconAt(0, greenLight);
 		// }
 		// getJTabbedPane().setEnabledAt(jTabbedPane.indexOfTab("VOs"), true);
 		// getJTabbedPane().setEnabledAt(jTabbedPane.indexOfTab("LocalProxy"),
 		// true);
 		// getJTabbedPane().setEnabledAt(jTabbedPane.indexOfTab("MyProxy"),
 		// true);
 		// }else {
 		//				 
 		// if ( CertificatePanel.CERT_EXPIRED.equals(event.getStatus()) ) {
 		// getJTabbedPane().setIconAt(0, redLight);
 		// } else {
 		// getJTabbedPane().setIconAt(0, yellowLight);
 		// }
 		//				
 		// getJTabbedPane().setEnabledAt(jTabbedPane.indexOfTab("VOs"), false);
 		// getJTabbedPane().setEnabledAt(jTabbedPane.indexOfTab("LocalProxy"),
 		// false);
 		// getJTabbedPane().setEnabledAt(jTabbedPane.indexOfTab("MyProxy"),
 		// false);
 		// }
 		// } catch (IndexOutOfBoundsException ioobe) {
 		// // never mind
 		// // ioobe.printStackTrace();
 		// }
 
 	}
 
 	public Color getBaseColor() {
 		if (base_color == null) {
 			base_color = getJContentPane().getBackground();
 		}
 		return base_color;
 	}
 
 	public Color getLighterColor() {
 		if (lighter_color == null) {
 			int red = getBaseColor().getRed() + 10;
 			int green = getBaseColor().getGreen() + 10;
 			int blue = getBaseColor().getBlue() + 10;
 			lighter_color = new Color(red, green, blue);
 		}
 		return lighter_color;
 	}
 
 	// =====================================================================
 	// blinking light
 
 	private ImageIcon redLight = null;
 	private ImageIcon yellowLight = null;
 	private ImageIcon greenLight = null; // @jve:decl-index=0:
 	private ImageIcon greyLight = null; // @jve:decl-index=0:
 
 	public void initIcons() {
 
 		redLight = new ImageIcon(getClass().getResource(
 				"/org/vpac/grix/images/redLight_small.png"));
 		yellowLight = new ImageIcon(getClass().getResource(
 				"/org/vpac/grix/images/yellowLight_small.png"));
 		greenLight = new ImageIcon(getClass().getResource(
 				"/org/vpac/grix/images/greenLight_small.png"));
 		greyLight = new ImageIcon(getClass().getResource(
 				"/org/vpac/grix/images/greyLight_small.png"));
 
 	}
 
 	public void proxyCreated(GlobusCredential newProxy) {
 
 		// LocalProxy.destroy();
 
 		myLogger.debug("Proxy created in authentication panel.");
 
 		if (newProxy == null) {
 			proxyDestroyed();
 		} else {
 
 			try {
 				CredentialHelpers.writeToDisk(newProxy, LocalProxy
 						.getProxyFile());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return;
 			}
 
 			LocalProxy.getDefaultProxy().checkStatus();
 		}
 
 	}
 
 	public void proxyDestroyed() {
 
 		myLogger.debug("Proxy destroyed");
 		LocalProxy.destroy();
 
 	}
 
 }
