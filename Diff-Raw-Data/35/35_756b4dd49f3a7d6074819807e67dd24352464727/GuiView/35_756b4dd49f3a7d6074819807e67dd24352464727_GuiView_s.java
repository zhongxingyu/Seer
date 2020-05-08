 package de.fettlaus.thekraken.view;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Locale;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.PlainDocument;
 
 import de.fettlaus.thekraken.events.EventBus;
 import de.fettlaus.thekraken.events.NewConnectionEvent;
 import de.fettlaus.thekraken.events.SendMessageEvent;
 import de.fettlaus.thekraken.events.SynchronizeClientsEvent;
 import de.fettlaus.thekraken.events.TargetEvent;
 import de.fettlaus.thekraken.events.TargetEvent.TargetEventType;
 
 import java.awt.GridLayout;
 import java.awt.Color;
 import javax.swing.border.TitledBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.JCheckBoxMenuItem;
 
 public class GuiView implements View {
 
 	/**
 	 * 
 	 * @author bachelor
 	 * @see http://docs.oracle.com/javase/6/docs/api/javax/swing/JTextField.html
 	 */
 	public class TextFieldLimit extends PlainDocument {
 		private static final long serialVersionUID = 3046506161061318577L;
 		private final int maximum;
 
 		TextFieldLimit(int maximum) {
 			super();
 			this.maximum = maximum;
 		}
 
 		@Override
 		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
 			if (str == null) {
 				return;
 			}
 
 			if ((getLength() + str.length()) <= maximum) {
 				super.insertString(offset, str, attr);
 			}
 		}
 	}
 
 	private JFrame form_main;
 	private JTextField textField_status;
 	private JTextField textField_connect;
 	private JButton button_connect;
 	private DefaultListModel list_targets_model;
 	private JButton button_synchronize;
 	private JTextArea textArea_messages;
 	private JTextArea textArea_uart;
 	private JButton button_ping;
 	private JButton button_disconnect;
 	private JTextField textField_port;
 	private JTabbedPane tabbedPane_messages;
 	private final ButtonGroup buttonGroup = new ButtonGroup();
 	private JMenu menu_file;
 	private JMenu menu_help;
 	private JMenuItem menuItem_help_about;
 	private JMenu menu_language;
 	private JMenuItem menuItem_file_close;
 	private JRadioButtonMenuItem radio_language_en_us;
 	private JRadioButtonMenuItem radio_language_de_de;
 	private JLabel label_addtarget;
 	private JLabel label_ip;
 	private JLabel label_port;
 	private JLabel label_targets;
 	private JList list_targets;
 	private JPanel panel_common;
 	private JTextField textField_message;
 	private JButton button_message;
 	private JRadioButton radio_message_all;
 	private JRadioButton radio_message_target;
 
 	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
 	private final EventBus evt;
 	private JPanel panel_host;
 	private JScrollPane scrollPane_host;
 	private JTextArea textArea_host;
 	private JButton button_shutdown;
 	private JPanel panel;
 	private JPanel panel_sendmessage;
 	private JPanel panel_sendmessage_type;
 	private JPanel panel_sendmessage_target;
 	private JRadioButton radio_message_uart;
 	private JRadioButton radio_message_message;
 	private final ButtonGroup buttonGroup_2 = new ButtonGroup();
 	private JMenuItem menuItem_clear;
 	private JCheckBoxMenuItem menuItem_capture;
 	protected boolean capture = true;
 	private JMenu menu_view;
 	private JCheckBoxMenuItem menuItem_diff;
 	/**
 	 * Create the application.
 	 */
 	public GuiView() {
 		evt = EventBus.instance();
 		initialize();
 	}
 
 	@Override
 	public void addHostMessage(String timestamp, String msg) {
 		if(capture){
 			final StringBuilder b = new StringBuilder();
 			b.append(timestamp).append(" >> ").append(msg).append("\n");
 			textArea_host.append(b.toString());
 		}
 	}
 
 	@Override
 	public void addLogmessage(String timestamp, String target, String msg, String diff) {
 		if(capture){
 			final StringBuilder b = new StringBuilder();
 			b.append(timestamp).append(" <").append(target).append("> ").append(msg);
 			if(menuItem_diff.isSelected()){
 				b.append(" (diff ").append(diff).append(" ns)");
 			}
 			b.append("\n");
 			textArea_messages.append(b.toString());
 		}
 	}
 
 	@Override
 	public void addUARTMessage(String timestamp, String target, String msg, String diff) {
 		if(capture){
 			final StringBuilder b = new StringBuilder();
 			b.append(timestamp).append(" <").append(target).append("> ").append(msg);
 			if(menuItem_diff.isSelected()){
 				b.append(" (diff ").append(diff).append(" ns)");
 			}
 			b.append("\n");
 			textArea_uart.append(b.toString());
 		}
 	}
 
 	@Override
 	public void setClients(String[] clients) {
 		list_targets_model.clear();
 		for (final String client : clients) {
 			list_targets_model.addElement(client);
 		}
 
 	}
 
 	@Override
 	public void setNotification(String msg) {
 		textField_status.setText(msg);
 
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		form_main = new JFrame();
 		form_main.setMinimumSize(new Dimension(500, 300));
 		form_main.setBounds(100, 100, 943, 586);
 		form_main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		final JPanel panel_main = new JPanel();
 		form_main.setContentPane(panel_main);
 		final GridBagLayout gbl_panel_main = new GridBagLayout();
 		gbl_panel_main.columnWeights = new double[] { 1.0, 0.0, 0.0 };
 		gbl_panel_main.rowWeights = new double[] { 0.0, 1.0, 0.0 };
 		panel_main.setLayout(gbl_panel_main);
 
 		final JMenuBar menuBar_main = new JMenuBar();
 		menuBar_main.setMinimumSize(new Dimension(0, 21));
 		final GridBagConstraints gbc_menuBar_main = new GridBagConstraints();
 		gbc_menuBar_main.anchor = GridBagConstraints.NORTH;
 		gbc_menuBar_main.fill = GridBagConstraints.BOTH;
 		gbc_menuBar_main.insets = new Insets(0, 0, 5, 0);
 		gbc_menuBar_main.gridwidth = 3;
 		gbc_menuBar_main.gridx = 0;
 		gbc_menuBar_main.gridy = 0;
 		panel_main.add(menuBar_main, gbc_menuBar_main);
 		
 				menu_file = new JMenu();
 				menuBar_main.add(menu_file);
 				
 				menuItem_clear = new JMenuItem(Messages.getString("View.menuItem.clear")); //$NON-NLS-1$
 				menuItem_clear.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						textArea_host.setText("");
 						textArea_messages.setText("");
 						textArea_uart.setText("");
 					}
 				});
 				menu_file.add(menuItem_clear);
 				
 				menuItem_capture = new JCheckBoxMenuItem(Messages.getString("View.menuItem.capture")); //$NON-NLS-1$
 				menuItem_capture.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						capture  = menuItem_capture.isSelected();
 					}
 				});
 				menuItem_capture.setSelected(true);
 				menu_file.add(menuItem_capture);
 				
 						menu_language = new JMenu();
 						menu_file.add(menu_language);
 						
 								radio_language_en_us = new JRadioButtonMenuItem();
 								radio_language_en_us.addActionListener(new ActionListener() {
 									@Override
 									public void actionPerformed(ActionEvent arg0) {
 										Locale.setDefault(Locale.ENGLISH);
 										Locale.setDefault(Locale.US);
 										Messages.reloadBundle();
 										GuiView.this.load_strings();
 									}
 								});
 								buttonGroup.add(radio_language_en_us);
 								radio_language_en_us.setSelected(true);
 								menu_language.add(radio_language_en_us);
 								
 										radio_language_de_de = new JRadioButtonMenuItem();
 										buttonGroup.add(radio_language_de_de);
 										radio_language_de_de.addActionListener(new ActionListener() {
 											@Override
 											public void actionPerformed(ActionEvent arg0) {
 												Locale.setDefault(Locale.GERMAN);
 												Locale.setDefault(Locale.GERMANY);
 												Messages.reloadBundle();
 												GuiView.this.load_strings();
 											}
 										});
 										menu_language.add(radio_language_de_de);
 										menuItem_file_close = new JMenuItem();
 										menuItem_file_close.addActionListener(new ActionListener() {
 											public void actionPerformed(ActionEvent arg0) {
 												System.exit(0);
 											}
 										});
 										menu_file.add(menuItem_file_close);
 		
 		menu_view = new JMenu(Messages.getString("GuiView.mnNewMenu.text")); //$NON-NLS-1$
 		menuBar_main.add(menu_view);
 		
 		menuItem_diff = new JCheckBoxMenuItem(Messages.getString("View.menuItem.diff")); //$NON-NLS-1$
 		menu_view.add(menuItem_diff);
 		final String lang = Locale.getDefault().getLanguage();
 		if (lang.equals("de")) {
 			radio_language_de_de.setSelected(true);
 		} else {
 			radio_language_en_us.setSelected(true);
 		}
 
 		menu_help = new JMenu();
 		menuBar_main.add(menu_help);
 
 		menuItem_help_about = new JMenuItem();
 		menuItem_help_about.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(form_main, Messages.getString("About.text"), Messages.getString("About.title"), JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
 		menu_help.add(menuItem_help_about);
 
 		final JSeparator main_separator = new JSeparator();
 		main_separator.setPreferredSize(new Dimension(2, 0));
 		main_separator.setOrientation(SwingConstants.VERTICAL);
 		final GridBagConstraints gbc_main_separator = new GridBagConstraints();
 		gbc_main_separator.fill = GridBagConstraints.VERTICAL;
 		gbc_main_separator.insets = new Insets(4, 4, 5, 5);
 		gbc_main_separator.gridx = 1;
 		gbc_main_separator.gridy = 1;
 		panel_main.add(main_separator, gbc_main_separator);
 
 		final JPanel panel_targets = new JPanel();
 		final GridBagConstraints gbc_panel_targets = new GridBagConstraints();
 		gbc_panel_targets.fill = GridBagConstraints.VERTICAL;
 		gbc_panel_targets.insets = new Insets(2, 2, 5, 2);
 		gbc_panel_targets.gridx = 2;
 		gbc_panel_targets.gridy = 1;
 		panel_main.add(panel_targets, gbc_panel_targets);
 		final GridBagLayout gbl_panel_targets = new GridBagLayout();
 		gbl_panel_targets.columnWidths = new int[] { 0, 0, 0 };
 		gbl_panel_targets.columnWeights = new double[] { 0.0, 1.0, 1.0 };
 		gbl_panel_targets.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
 		panel_targets.setLayout(gbl_panel_targets);
 
 		label_addtarget = new JLabel();
 		final GridBagConstraints gbc_label_addtarget = new GridBagConstraints();
 		gbc_label_addtarget.gridwidth = 3;
 		gbc_label_addtarget.insets = new Insets(2, 2, 5, 2);
 		gbc_label_addtarget.gridx = 0;
 		gbc_label_addtarget.gridy = 0;
 		panel_targets.add(label_addtarget, gbc_label_addtarget);
 
 		label_ip = new JLabel();
 		final GridBagConstraints gbc_label_ip = new GridBagConstraints();
 		gbc_label_ip.insets = new Insets(2, 2, 5, 5);
 		gbc_label_ip.anchor = GridBagConstraints.WEST;
 		gbc_label_ip.gridx = 0;
 		gbc_label_ip.gridy = 1;
 		panel_targets.add(label_ip, gbc_label_ip);
 
 		label_port = new JLabel();
 		final GridBagConstraints gbc_label_port = new GridBagConstraints();
 		gbc_label_port.anchor = GridBagConstraints.EAST;
 		gbc_label_port.insets = new Insets(2, 2, 5, 5);
 		gbc_label_port.gridx = 0;
 		gbc_label_port.gridy = 2;
 		panel_targets.add(label_port, gbc_label_port);
 
 		textField_port = new JTextField();
 		textField_port.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				textField_port.setText("");
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				if (textField_port.getText().isEmpty()) {
 					textField_port.setText(Messages.getString("View.textField_port.text"));
 				}
 			}
 		});
 		final GridBagConstraints gbc_textField_port = new GridBagConstraints();
 		gbc_textField_port.insets = new Insets(2, 2, 5, 5);
 		gbc_textField_port.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_port.gridx = 1;
 		gbc_textField_port.gridy = 2;
 		panel_targets.add(textField_port, gbc_textField_port);
 		textField_port.setColumns(10);
 
 		label_targets = new JLabel();
 		final GridBagConstraints gbc_label_targets = new GridBagConstraints();
 		gbc_label_targets.gridwidth = 3;
 		gbc_label_targets.insets = new Insets(2, 2, 5, 2);
 		gbc_label_targets.gridx = 0;
 		gbc_label_targets.gridy = 4;
 		panel_targets.add(label_targets, gbc_label_targets);
 
 		final JScrollPane scrollPane_targets = new JScrollPane();
 		scrollPane_targets.setSize(new Dimension(20, 0));
 		scrollPane_targets.setMaximumSize(new Dimension(30, 32767));
 		scrollPane_targets.setPreferredSize(new Dimension(30, 3));
 		final GridBagConstraints gbc_scrollPane_targets = new GridBagConstraints();
 		gbc_scrollPane_targets.insets = new Insets(2, 2, 5, 2);
 		gbc_scrollPane_targets.weighty = 1.0;
 		gbc_scrollPane_targets.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_targets.gridwidth = 3;
 		gbc_scrollPane_targets.gridx = 0;
 		gbc_scrollPane_targets.gridy = 5;
 		panel_targets.add(scrollPane_targets, gbc_scrollPane_targets);
 
 		list_targets_model = new DefaultListModel();
 		list_targets = new JList(list_targets_model);
 		list_targets.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				final int tmp = list_targets.getSelectedIndex();
 				list_targets.clearSelection();
 				list_targets.setSelectedIndex(tmp);
 				super.mousePressed(e);
 			}
 		});
 		list_targets_model.addListDataListener(new ListDataListener() {
 
 			@Override
 			public void contentsChanged(ListDataEvent e) {
 			}
 
 			@Override
 			public void intervalAdded(ListDataEvent e) {
 				list_targets.clearSelection();
 				list_targets.setSelectedIndex(0);
 			}
 
 			@Override
 			public void intervalRemoved(ListDataEvent e) {
 			}
 
 		});
 
 		list_targets.setAlignmentX(Component.LEFT_ALIGNMENT);
 		scrollPane_targets.setViewportView(list_targets);
 		list_targets.setAlignmentY(Component.TOP_ALIGNMENT);
 
 		final JSeparator separator_target = new JSeparator();
 		separator_target.setPreferredSize(new Dimension(0, 3));
 		final GridBagConstraints gbc_separator_target = new GridBagConstraints();
 		gbc_separator_target.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separator_target.gridwidth = 3;
 		gbc_separator_target.insets = new Insets(2, 2, 5, 2);
 		gbc_separator_target.gridx = 0;
 		gbc_separator_target.gridy = 3;
 		panel_targets.add(separator_target, gbc_separator_target);
 
 		textField_connect = new JTextField();
 		textField_connect.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				if (textField_connect.getText().equals(Messages.getString("View.textField_connect.text"))) {
 					textField_connect.setText("");
 				}
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				if (textField_connect.getText().isEmpty()) {
 					textField_connect.setText(Messages.getString("View.textField_connect.text"));
 				}
 			}
 		});
 		textField_connect.setMinimumSize(new Dimension(120, 19));
 		textField_connect.setPreferredSize(new Dimension(100, 19));
 		textField_connect.setColumns(11);
 		final GridBagConstraints gbc_textField_connect = new GridBagConstraints();
 		gbc_textField_connect.weightx = 0.5;
 		gbc_textField_connect.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_connect.insets = new Insets(2, 2, 5, 5);
 		gbc_textField_connect.gridx = 1;
 		gbc_textField_connect.gridy = 1;
 		panel_targets.add(textField_connect, gbc_textField_connect);
 
 		button_connect = new JButton();
 		button_connect.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				evt.post(new NewConnectionEvent(textField_connect.getText(), textField_port.getText()));
 			}
 		});
 
 		final GridBagConstraints gbc_button_connect = new GridBagConstraints();
 		gbc_button_connect.gridheight = 2;
 		gbc_button_connect.weightx = 0.5;
 		gbc_button_connect.fill = GridBagConstraints.BOTH;
 		gbc_button_connect.insets = new Insets(2, 2, 5, 2);
 		gbc_button_connect.gridx = 2;
 		gbc_button_connect.gridy = 1;
 		panel_targets.add(button_connect, gbc_button_connect);
 		
 		panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.insets = new Insets(2, 2, 2, 2);
 		gbc_panel.gridwidth = 3;
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 6;
 		panel_targets.add(panel, gbc_panel);
 				panel.setLayout(new GridLayout(2, 2, 5, 7));
 		
 				button_ping = new JButton();
 				panel.add(button_ping);
 				
 						button_disconnect = new JButton();
 						panel.add(button_disconnect);
 						
 						button_shutdown = new JButton(Messages.getString("View.button_shutdown.text"));
 						button_shutdown.addActionListener(new ActionListener() {
 							public void actionPerformed(ActionEvent arg0) {
 								evt.post(new TargetEvent(TargetEventType.SHUTDOWN,list_targets.getSelectedIndex()));
 							}
 						});
 						panel.add(button_shutdown);
 						button_disconnect.addActionListener(new ActionListener() {
 							@Override
 							public void actionPerformed(ActionEvent arg0) {
 								evt.post(new TargetEvent(TargetEventType.DISCONNECT, list_targets.getSelectedIndex()));
 							}
 						});
 				button_ping.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						evt.post(new TargetEvent(TargetEventType.PING,list_targets.getSelectedIndex()));
 					}
 				});
 
 		final JPanel panel_status = new JPanel();
 		final GridBagConstraints gbc_panel_status = new GridBagConstraints();
 		gbc_panel_status.insets = new Insets(2, 2, 2, 2);
 		gbc_panel_status.anchor = GridBagConstraints.NORTH;
 		gbc_panel_status.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel_status.gridwidth = 3;
 		gbc_panel_status.gridx = 0;
 		gbc_panel_status.gridy = 2;
 		panel_main.add(panel_status, gbc_panel_status);
 		final GridBagLayout gbl_panel_status = new GridBagLayout();
 		gbl_panel_status.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
 		gbl_panel_status.rowWeights = new double[] { 0.0 };
 		panel_status.setLayout(gbl_panel_status);
 
 		textField_status = new JTextField();
 		textField_status.setEditable(false);
 		final GridBagConstraints gbc_textField_status = new GridBagConstraints();
 		gbc_textField_status.gridwidth = 3;
 		gbc_textField_status.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_status.gridx = 0;
 		gbc_textField_status.gridy = 0;
 		panel_status.add(textField_status, gbc_textField_status);
 		textField_status.setColumns(10);
 
 		panel_common = new JPanel();
 		final GridBagConstraints gbc_panel_common = new GridBagConstraints();
 		gbc_panel_common.insets = new Insets(2, 2, 5, 5);
 		gbc_panel_common.fill = GridBagConstraints.BOTH;
 		gbc_panel_common.gridx = 0;
 		gbc_panel_common.gridy = 1;
 		panel_main.add(panel_common, gbc_panel_common);
 		final GridBagLayout gbl_panel_common = new GridBagLayout();
 		gbl_panel_common.columnWeights = new double[] { 1.0 };
 		gbl_panel_common.rowWeights = new double[] { 0.0, 0.0, 0.0 };
 		panel_common.setLayout(gbl_panel_common);
 
 		tabbedPane_messages = new JTabbedPane(SwingConstants.TOP);
 		final GridBagConstraints gbc_tabbedPane_messages = new GridBagConstraints();
 		gbc_tabbedPane_messages.weighty = 1.0;
 		gbc_tabbedPane_messages.weightx = 1.0;
 		gbc_tabbedPane_messages.fill = GridBagConstraints.BOTH;
 		gbc_tabbedPane_messages.anchor = GridBagConstraints.NORTHWEST;
 		gbc_tabbedPane_messages.insets = new Insets(2, 2, 5, 2);
 		gbc_tabbedPane_messages.gridx = 0;
 		gbc_tabbedPane_messages.gridy = 0;
 		panel_common.add(tabbedPane_messages, gbc_tabbedPane_messages);
 
 		final JPanel panel_messages = new JPanel();
 		tabbedPane_messages.addTab(Messages.getString("View.panel_messages.title"), null, panel_messages, null); //$NON-NLS-1$ 
 		panel_messages.setLayout(new BorderLayout(0, 0));
 
		final JScrollPane scrollPane_messages = new JScrollPane();
 		scrollPane_messages.setAutoscrolls(true);
 		scrollPane_messages.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		scrollPane_messages.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		panel_messages.add(scrollPane_messages, BorderLayout.CENTER);
 
 		textArea_messages = new JTextArea();
 		textArea_messages.setColumns(2);
 		final DefaultCaret car = (DefaultCaret) textArea_messages.getCaret();
 		car.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 		scrollPane_messages.setViewportView(textArea_messages);
 
 		final JLabel lblNewLabel = new JLabel();
 		scrollPane_messages.setColumnHeaderView(lblNewLabel);
 
 		final JPanel panel_uart = new JPanel();
 		tabbedPane_messages.addTab(Messages.getString("View.panel_uart.title"), null, panel_uart, null); //$NON-NLS-1$ 
 		panel_uart.setLayout(new BorderLayout(0, 0));
 
		final JScrollPane scrollPane_uart = new JScrollPane();
 		scrollPane_uart.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		scrollPane_uart.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		panel_uart.add(scrollPane_uart, BorderLayout.CENTER);
 
 		textArea_uart = new JTextArea();
 		textArea_uart.setColumns(2);
 		scrollPane_uart.setViewportView(textArea_uart);
 
 		panel_host = new JPanel();
 		tabbedPane_messages.addTab(Messages.getString("View.panel_host.title"), null, panel_host, null); //$NON-NLS-1$
 		panel_host.setLayout(new BorderLayout(0, 0));
 
 		scrollPane_host = new JScrollPane();
 		scrollPane_host.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollPane_host.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		panel_host.add(scrollPane_host);
 
 		textArea_host = new JTextArea();
 		scrollPane_host.setViewportView(textArea_host);
 
 		button_synchronize = new JButton();
 		button_synchronize.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				evt.post(new SynchronizeClientsEvent());
 			}
 		});
 		final GridBagConstraints gbc_button_synchronize = new GridBagConstraints();
 		gbc_button_synchronize.insets = new Insets(2, 2, 2, 2);
 		gbc_button_synchronize.fill = GridBagConstraints.BOTH;
 		gbc_button_synchronize.anchor = GridBagConstraints.WEST;
 		gbc_button_synchronize.gridx = 0;
 		gbc_button_synchronize.gridy = 2;
 		panel_common.add(button_synchronize, gbc_button_synchronize);
 		
 		panel_sendmessage = new JPanel();
 		panel_sendmessage.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), Messages.getString("View.panel_sendmessage.borderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
 		GridBagConstraints gbc_panel_sendmessage = new GridBagConstraints();
 		gbc_panel_sendmessage.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel_sendmessage.insets = new Insets(2, 2, 5, 5);
 		gbc_panel_sendmessage.gridx = 0;
 		gbc_panel_sendmessage.gridy = 1;
 		panel_common.add(panel_sendmessage, gbc_panel_sendmessage);
 				GridBagLayout gbl_panel_sendmessage = new GridBagLayout();
 				gbl_panel_sendmessage.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
 				gbl_panel_sendmessage.rowWeights = new double[]{0.0};
 				panel_sendmessage.setLayout(gbl_panel_sendmessage);
 						
 						panel_sendmessage_type = new JPanel();
 						panel_sendmessage_type.setBorder(new TitledBorder(null, Messages.getString("View.panel_sendmessage_type.borderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
 						GridBagConstraints gbc_panel_sendmessage_type = new GridBagConstraints();
 						gbc_panel_sendmessage_type.insets = new Insets(0, 0, 5, 5);
 						gbc_panel_sendmessage_type.fill = GridBagConstraints.BOTH;
 						gbc_panel_sendmessage_type.gridx = 0;
 						gbc_panel_sendmessage_type.gridy = 0;
 						panel_sendmessage.add(panel_sendmessage_type, gbc_panel_sendmessage_type);
 						panel_sendmessage_type.setLayout(new GridLayout(0, 1, 0, 0));
 						
 						radio_message_uart = new JRadioButton(Messages.getString("View.radio_message_uart")); //$NON-NLS-1$
 						radio_message_uart.setSelected(true);
 						buttonGroup_2.add(radio_message_uart);
 						panel_sendmessage_type.add(radio_message_uart);
 						
 						radio_message_message = new JRadioButton(Messages.getString("View.radio_message_message")); //$NON-NLS-1$
 						buttonGroup_2.add(radio_message_message);
 						panel_sendmessage_type.add(radio_message_message);
 				
 						textField_message = new JTextField();
 						GridBagConstraints gbc_textField_message = new GridBagConstraints();
 						gbc_textField_message.weightx = 1.0;
 						gbc_textField_message.fill = GridBagConstraints.HORIZONTAL;
 						gbc_textField_message.insets = new Insets(0, 0, 5, 5);
 						gbc_textField_message.gridx = 1;
 						gbc_textField_message.gridy = 0;
 						panel_sendmessage.add(textField_message, gbc_textField_message);
 						textField_message.setDocument(new TextFieldLimit(512));
 						textField_message.addKeyListener(new KeyAdapter() {
 							@Override
 							public void keyPressed(KeyEvent e) {
 								if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 									sendMessage();
 								}
 							}
 						});
 						textField_message.setText("");
 						textField_message.setColumns(10);
 				
 				panel_sendmessage_target = new JPanel();
 				panel_sendmessage_target.setBorder(new TitledBorder(null, Messages.getString("View.panel_sendmessage_target.borderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
 				GridBagConstraints gbc_panel_sendmessage_target = new GridBagConstraints();
 				gbc_panel_sendmessage_target.fill = GridBagConstraints.BOTH;
 				gbc_panel_sendmessage_target.insets = new Insets(2, 2, 5, 5);
 				gbc_panel_sendmessage_target.gridx = 2;
 				gbc_panel_sendmessage_target.gridy = 0;
 				panel_sendmessage.add(panel_sendmessage_target, gbc_panel_sendmessage_target);
 				panel_sendmessage_target.setLayout(new GridLayout(0, 1, 0, 0));
 				
 						radio_message_target = new JRadioButton(Messages.getString("radio_message_target.text")); //$NON-NLS-1$
 						panel_sendmessage_target.add(radio_message_target);
 						buttonGroup_1.add(radio_message_target);
 						
 								radio_message_all = new JRadioButton(Messages.getString("radio_message_all.text"));
 								panel_sendmessage_target.add(radio_message_all);
 								radio_message_all.setSelected(true);
 								buttonGroup_1.add(radio_message_all);
 								radio_message_all.setVerticalAlignment(SwingConstants.TOP);
 				
 						button_message = new JButton(Messages.getString("View.btn_message.text")); //$NON-NLS-1$
 						GridBagConstraints gbc_button_message = new GridBagConstraints();
 						gbc_button_message.insets = new Insets(2, 2, 5, 2);
 						gbc_button_message.fill = GridBagConstraints.BOTH;
 						gbc_button_message.gridx = 3;
 						gbc_button_message.gridy = 0;
 						panel_sendmessage.add(button_message, gbc_button_message);
 						button_message.addActionListener(new ActionListener() {
 							@Override
 							public void actionPerformed(ActionEvent arg0) {
 								sendMessage();
 							}
 						});
 		load_strings();
 		form_main.setVisible(true);
 
 	}
 
 	private void sendMessage() {
 		int target = -1;
 		if (radio_message_target.isSelected()) {
 			target = list_targets.getSelectedIndex();
 		}
 		if(radio_message_uart.isSelected()){
 			evt.post(new SendMessageEvent(textField_message.getText(), target, true));
 		}else{
 			evt.post(new SendMessageEvent(textField_message.getText(), target, false));
 		}
 		textField_message.setText("");
 		textField_message.requestFocus();
 	}
 
 	protected void load_strings() {
 		button_connect.setText(Messages.getString("View.button_connect.text"));
 		button_disconnect.setText(Messages.getString("View.button_disconnect.text"));
 		button_ping.setText(Messages.getString("View.button_ping.text"));
 		button_synchronize.setText(Messages.getString("View.button_synchronize.text"));
 		button_message.setText(Messages.getString("View.btn_message.text"));
 		button_shutdown.setText(Messages.getString("View.button_shutdown.text"));
 		form_main.setTitle(Messages.getString("View.form_main.title"));
 		label_addtarget.setText(Messages.getString("View.label_addtarget.text"));
 		label_ip.setText(Messages.getString("View.label_ip.text"));
 		label_port.setText(Messages.getString("View.label_port.text"));
 		label_targets.setText(Messages.getString("View.label_targets.text"));
 		menu_file.setText(Messages.getString("View.menu_file.s"));
 		menu_help.setText(Messages.getString("View.menu_help.text"));
 		menu_language.setText(Messages.getString("View.menu_language.text"));
 		menu_view.setText(Messages.getString("View.menu_view.text")); //$NON-NLS-1$
 		menuItem_file_close.setText(Messages.getString("View.menuItem_file_close.text"));
 		menuItem_help_about.setText(Messages.getString("View.menuItem_help_close_1.text"));
 		menuItem_diff.setText(Messages.getString("View.menuItem.diff")); //$NON-NLS-1$
 		radio_language_de_de.setText(Messages.getString("View.radio_language_de_de.text"));
 		radio_language_en_us.setText(Messages.getString("View.radio_language_en_us.text"));
 		radio_message_target.setText(Messages.getString("View.radio_message_target.text")); //$NON-NLS-1$
 		radio_message_all.setText(Messages.getString("View.radio_message_all.text")); //$NON-NLS-1$
 		tabbedPane_messages.setTitleAt(0, Messages.getString("View.panel_messages.title"));
 		tabbedPane_messages.setTitleAt(1, Messages.getString("View.panel_uart.title"));
 		tabbedPane_messages.setTitleAt(2, Messages.getString("View.panel_host.title"));
 		textField_connect.setText(Messages.getString("View.textField_connect.text"));
 		textField_port.setText(Messages.getString("View.textField_port.text"));
 
 	}
 }
