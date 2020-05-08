 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-shell Project
 //
 // UTGBPortableWidget.java
 // Since: Sep 2, 2008
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.border.TitledBorder;
 
 import org.xerial.core.XerialException;
 import org.xerial.util.FileResource;
 import org.xerial.util.log.Logger;
 
 /**
  * GUI Panel of the UTGB Portable Server
  * 
  * @author leo
  * 
  */
 public class UTGBPortableWidget extends JFrame implements ServerListener {
 
 	private static Logger _logger = Logger.getLogger(UTGBPortableWidget.class);
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	static class CustomTitledBorder extends TitledBorder {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public CustomTitledBorder(String title) {
 			super(title);
 		}
 
 		@Override
 		public Insets getBorderInsets(Component c) {
 			return getBorderInsets(c, new Insets(0, 0, 0, 0));
 		}
 
 		@Override
 		public Insets getBorderInsets(Component c, Insets insets) {
 			insets.set(12, 7, 5, 7);
 			return insets;
 		}
 	}
 
 	static class CustomButton extends JButton {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public CustomButton(String label) {
 			super(label);
 			setMargin(new Insets(2, 5, 2, 5));
 		}
 	}
 
 	class ServerLaunchPanel extends JPanel {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		JButton startButton = new CustomButton("start");
 		JButton stopButton = new CustomButton("stop");
 		JButton restartButton = new CustomButton("restart");
 		JButton syncButton = new CustomButton("sync");
 		JLabel serverURL = new JLabel();
 		JTextField portNumberField = new JTextField(Integer.toString(config.getPortNumber()));
 		JTextField contextPathField = new JTextField(config.getContextPath());
 
 		public ServerLaunchPanel() {
 
 			JPanel buttonPanel = new JPanel();
 			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
 			buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 			buttonPanel.add(startButton);
 			buttonPanel.add(stopButton);
 			buttonPanel.add(restartButton);
 			// buttonPanel.add(syncButton);
 
 			// URL
 			serverURL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			buttonPanel.add(serverURL);
 
 			serverURL.addMouseListener(new MouseListener() {
 
 				public void mouseClicked(MouseEvent e) {
 					// open the OS's default browser
 					WebBrowser.openURL(config.getServerURL());
 				}
 
 				public void mouseEntered(MouseEvent e) {
 
 				}
 
 				public void mouseExited(MouseEvent e) {
 
 				}
 
 				public void mousePressed(MouseEvent e) {
 
 				}
 
 				public void mouseReleased(MouseEvent e) {
 
 				}
 			});
 
 			// button listener
 			startButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					onPushStart();
 				}
 			});
 
 			stopButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					onPushStop();
 				}
 			});
 
 			restartButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					onPushRestart();
 				}
 			});
 
 			syncButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					onPushSync();
 				}
 			});
 
 			// server configuration panel (port & context path)
 			JPanel serverConfigurationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 			// port number
 			JLabel portNumberLabel = new JLabel("port number:");
 
 			portNumberField.setColumns(5);
 			serverConfigurationPanel.add(portNumberLabel);
 			serverConfigurationPanel.add(portNumberField);
 			portNumberField.addKeyListener(new KeyListener() {
 
 				public void keyPressed(KeyEvent e) {
 				}
 
 				public void keyReleased(KeyEvent e) {
 					String inputPort = portNumberField.getText();
 					try {
 						config.portNumber = Integer.parseInt(inputPort);
 						serverLaunchPanel.update();
 						clearStatus();
 					}
 					catch (NumberFormatException ne) {
 						setStatus(MessageType.ERROR, "invalid port number");
 					}
 				}
 
 				public void keyTyped(KeyEvent e) {
 				}
 			});
 
 			// context path
 			JLabel contextPathLabel = new JLabel("context path:");
 
 			contextPathField.setColumns(12);
 			serverConfigurationPanel.add(contextPathLabel);
 			serverConfigurationPanel.add(contextPathField);
 			serverConfigurationPanel.setBorder(createTitledBorder("server configurations"));
 			contextPathField.addKeyListener(new KeyListener() {
 
 				public void keyPressed(KeyEvent e) {
 				}
 
 				public void keyReleased(KeyEvent e) {
 					String path = contextPathField.getText();
 					if (!path.startsWith("/")) {
 						path = "/" + path;
 						contextPathField.setText(path);
 					}
 					config.contextPath = path;
 					serverLaunchPanel.update();
 				}
 
 				public void keyTyped(KeyEvent e) {
 				}
 			});
 
 			// layout panels
 			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 			add(buttonPanel);
 			add(serverConfigurationPanel);
 		}
 
 		public void onPushStart() {
 			startButton.setEnabled(false);
 			stopButton.setEnabled(true);
 			restartButton.setEnabled(true);
 			syncButton.setEnabled(true);
 
 			portNumberField.setEditable(false);
 			contextPathField.setEditable(false);
 
 			try {
 				if (launcher != null)
 					launcher.startTomcatServer(config);
 			}
 			catch (XerialException e) {
 				setStatus(MessageType.ERROR, "failed to start server: " + e.getMessage());
 				onPushStop();
 			}
 		}
 
 		public void onPushStop() {
 			startButton.setEnabled(true);
 			stopButton.setEnabled(false);
 			restartButton.setEnabled(false);
 			syncButton.setEnabled(false);
 
 			portNumberField.setEditable(true);
 			contextPathField.setEditable(true);
 
 			setStatus(MessageType.INFO, "stopping the web server...");
 			if (launcher != null)
 				launcher.stopTomcatServer(config);
 		}
 
 		public void onPushRestart() {
 			if (launcher != null) {
 				onPushStop();
 				onPushStart();
 			}
 		}
 
 		public void onPushSync() {
 
 		}
 
 		public void update() {
 			serverURL.setText(config.generateServerURLLinkHTML());
 			UTGBPortableWidget.this.pack();
 		}
 
 	}
 
 	private final UTGBPortableConfig config;
 	private ServerLaunchPanel serverLaunchPanel;
 	private JLabel status = new JLabel(" ");
 	private JPanel mainPanel = new JPanel();
 	private TomcatServerLauncher launcher = null;
 
 	static {
 		// take the menu bar off the jframe
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 
 		// set the name of the application menu item
 		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "UTGB Portable");
 
 		// set the look and feel
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 	}
 
 	public UTGBPortableWidget() {
 		this(new UTGBPortableConfig());
 	}
 
 	public UTGBPortableWidget(UTGBPortableConfig config) {
 		this.config = config;
 		buildGUI();
 	}
 
 	public void pushStart() {
 		serverLaunchPanel.onPushStart();
 	}
 
 	public void updateLink() {
 		serverLaunchPanel.update();
 	}
 
 	protected void buildGUI() {
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 
 		setTitle("UTGB Portable");
 
 		// main panel
 
 		// set icon
 		ImageIcon imageIcon = new ImageIcon(FileResource.find(UTGBPortableWidget.class, "utgb-icon.png"));
 		setIconImage(imageIcon.getImage());
 
 		// add server start/stop/restart buttons
 		serverLaunchPanel = new ServerLaunchPanel();
 
 		// track project folder
 		JPanel trackProjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		trackProjectPanel.setBorder(createTitledBorder("project root"));
 		JLabel trackProjectFolder = new JLabel(config.getProjectRoot());
 		trackProjectPanel.add(trackProjectFolder);
 
 		// status panel
 		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		statusPanel.setBorder(createTitledBorder("status"));
 		statusPanel.add(status);
 
 		// layout panels
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 		mainPanel.add(serverLaunchPanel);
 		mainPanel.add(trackProjectPanel);
 		mainPanel.add(statusPanel);
 		add(mainPanel);
 		// init the widgets
 		serverLaunchPanel.onPushStop();
 		serverLaunchPanel.update();
 
 		this.pack();
 	}
 
 	public static enum MessageType {
 		INFO(new Color(0x50, 0x50, 0x50)), WARN(Color.ORANGE), ERROR(new Color(0xFF, 0x00, 0x00));
 
 		private Color color;
 
 		MessageType(Color color) {
 			this.color = color;
 		}
 
 		Color getColor() {
 			return color;
 		}
 	}
 
 	public void setStatus(MessageType type, String message) {
 		status.setForeground(type.getColor());
 		status.setText(message);
 		pack();
 	}
 
 	public void clearStatus() {
 		status.setText(" ");
 		pack();
 	}
 
 	public static TitledBorder createTitledBorder(String borderTitle) {
 		return new CustomTitledBorder(borderTitle);
 	}
 
 	public static void setButtonMargin(JButton button) {
 		button.setMargin(new Insets(2, 5, 2, 5));
 	}
 
 	public void setTomcatServerLauncher(TomcatServerLauncher launcher) {
 		this.launcher = launcher;
 		launcher.addServerListener(this);
 	}
 
 	public static void main(String[] args) {
 		UTGBPortableWidget portableWidget = new UTGBPortableWidget();
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		portableWidget.setLocation((int) d.getWidth() / 4, (int) d.getHeight() / 4);
 		portableWidget.setVisible(true);
 		portableWidget.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	public void beforeStart() {
 		setStatus(MessageType.INFO, "Starting the web server...");
 	}
 
 	public void afterStart() {
		setStatus(MessageType.INFO, "The web server has started.");
 	}
 
 	public void afterStop() {
		setStatus(MessageType.INFO, "The web server has terminated.");
 	}
 
 	public void beforeStop() {
 		setStatus(MessageType.INFO, "Terminating the web server...");
 	}
 
 }
 
 /**
  * Required to launch a Tomcat server instance
  * 
  * @author leo
  * 
  */
 interface TomcatServerLauncher {
 
 	void startTomcatServer(UTGBPortableConfig config) throws XerialException;
 
 	void stopTomcatServer(UTGBPortableConfig config);
 
 	void addServerListener(ServerListener listener);
 
 }
