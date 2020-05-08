 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.InterfaceAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableColumn;
 
 public class ServerUI extends JFrame {
 
 	private JTabbedPane innerTabbedPane;
 	private static JLabel status;
 	private int connectionType = 1;
 	private static final int PORT = 8888;
 	private static final int FPS_MIN = 10;
 	private static final int FPS_MAX = 50;
 	private JComboBox comboBox;
 	private JComboBox comboBoxMouse;
 	private static int userChoice = 0;
 	private static final int FPS_INIT = 15; // initial frames per second
 	private static final String MESSAGETOUSER = "Please go to the settings tab to map PC controls to android touch controls.";
 	private String directionKeys[] = { "^ v < >", "W A S D" };
 	private String defaultKeyboardControls[] = { "-None-", "A", "B", "C", "D",
 			"E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
 			"R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4",
 			"5", "6", "7", "8", "9", "0", "SPACEBAR", "CTRL", "ALT", "SHIFT",
 			"TAB", "ENTER", "BACKSPACE", "CAPSLOCK", "LEFT-MOUSE-BUTTON",
 			"RIGHT-MOUSE-BUTTON" }; // TODO-add more?
 	private Object[][] defaultData = { { "Tap", "Z" },
 			{ "Swipe Up", "SPACEBAR" }, { "Swipe Down", "D" },
 			{ "Swipe Left", "A" }, { "Swipe Right", "X" } };
 
 	private Object[][] emptyData = { { "Tap", "-None-" },
 			{ "Swipe Up", "-None-" }, { "Swipe Down", "-None-" },
 			{ "Swipe Left", "-None-" }, { "Swipe Right", "-None-" } };
 
 	private String[] columnNames = { "Touch Controls", "PC Controls" };
 	private static ArrayList<String> keyboardControlMapping = new ArrayList<String>();
 
 	private static float mouseRatio;
 
 	private JTabbedPane tabbedPane;
 	private JPanel generalPane;
 	private JPanel keySettingsPane;
 	private JPanel noTiltPane;
 	private JPanel tiltUpPane;
 	private JPanel tiltDownPane;
 	private JPanel tiltRightPane;
 	private JPanel tiltLeftPane;
 	private JTable table1 = new JTable();
 	private JTable table2 = new JTable();
 	private JTable table3 = new JTable();
 	private JTable table4 = new JTable();
 	private JTable table5 = new JTable();
 	private JTable[] tables = { table1, table2, table3, table4, table5 };
 
 	public void createServerUI() throws UnknownHostException {
 		ServerUI sui = new ServerUI();
 		status = new JLabel(" Connection Status: Disconnected");
 		sui.createBasicUI();
 		sui.setVisible(true);
 	}
 
 	private void createBasicUI() throws UnknownHostException {
 		// TODO Auto-generated method stub
 		setTitle("Game Controller Server");
 		setSize(750, 500);
 
 		JPanel topPanel = new JPanel();
 		topPanel.setLayout(new BorderLayout());
 		getContentPane().add(topPanel);
 		createGeneralTab();
 		createSettingsTab();
 
 		// We create a panel which will hold the UI components
 		tabbedPane = new JTabbedPane();
 		tabbedPane.addTab("General", generalPane);
 		tabbedPane.addTab("Key Settings", keySettingsPane);
 		topPanel.add(tabbedPane, BorderLayout.CENTER);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Initialize variables
 		mouseRatio = (float) (FPS_INIT * 0.1);
 	}
 
 	public int getUserChoice() {
 		return userChoice;
 	}
 
 	private void createSettingsTab() {
 		keySettingsPane = new JPanel();
 		keySettingsPane = new JPanel(new BorderLayout());
 		keySettingsPane.setLayout(new FlowLayout(FlowLayout.LEADING));
 		keySettingsPane.setBorder(new EmptyBorder(2, 2, 2, 2));
 
 		buildComponentsInKeySettings(keySettingsPane, "Hold and Drag");
 
 		JLabel msgToUser = new JLabel(
 				"Please map your button configuration here.");
 		msgToUser.setFont(new Font("Serif", Font.ITALIC, 15));
 		keySettingsPane.add(msgToUser);
 
 		buildInnerTabs();
 
 		JButton restoreToDefault = new JButton("Restore to default settings");
 		restoreToDefault.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				innerTabbedPane.removeAll();
 				resetOriginalValues();
 				JPanel[] panels = new JPanel[5];
 				for (int i = 0; i < 5; i++) {
 					if (i == 0)
 						panels[i] = createTiltTab(defaultData, tables, i);
 					else
 						panels[i] = createTiltTab(emptyData, tables, i);
 				}
 				assignPanels(panels);
 				populateTabs(innerTabbedPane);
 
 				// direction keys
 				comboBox.setSelectedIndex(0);
 			}
 		});
 
 		keySettingsPane.add(restoreToDefault);
 
 		JButton save = new JButton("Save changes");
 		save.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				// ... called when button clicked
 				String directionKeys = comboBox.getSelectedItem().toString();
 				String mouseControl = comboBoxMouse.getSelectedItem()
 						.toString();
 
 				File dataFile = createKeyMapFile();
 				PrintWriter writer;
 				try {
 					writer = new PrintWriter(dataFile, "UTF-8");
 
 					storeMappings(writer, tables, 0, "No Tilt");
 					storeMappings(writer, tables, 1, "Tilt Up");
 					storeMappings(writer, tables, 2, "Tilt Down");
 					storeMappings(writer, tables, 3, "Tilt Left");
 					storeMappings(writer, tables, 4, "Tilt Right");
 					writer.close();
 					JOptionPane.showMessageDialog(null, "Direction keys: "
 							+ directionKeys + "  Mouse control: "
 							+ mouseControl
 							+ "\nAll changes saved successfully.");
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				setDefaultSettings();
 				// setKeyMapping();
 			}
 		});
 		keySettingsPane.add(save);
 	}
 
 	private void resetOriginalValues() {
 
 		Object[][] originalDefaultData = { { "Tap", "Z" },
 				{ "Swipe Up", "SPACEBAR" }, { "Swipe Down", "D" },
 				{ "Swipe Left", "A" }, { "Swipe Right", "X" } };
 
 		Object[][] originalEmptyData = { { "Tap", "-None-" },
 				{ "Swipe Up", "-None-" }, { "Swipe Down", "-None-" },
 				{ "Swipe Left", "-None-" }, { "Swipe Right", "-None-" } };
 		defaultData = originalDefaultData;
 		emptyData = originalEmptyData;
 	}
 
 	public void storeMappings(PrintWriter writer, JTable[] tables, int index,
 			String tabName) {
 		for (int i = 0; i < tables[index].getRowCount(); i++) {
 			String pcControl = tables[index].getValueAt(i, 1).toString();
 			writer.print(pcControl + " ");
 		}
 		writer.println();
 	}
 
 	public ArrayList<String> getKeyMappings() {
 		return keyboardControlMapping;
 	}
 
 	private void buildInnerTabs() {
 		// TODO Auto-generated method stub
 		innerTabbedPane = new JTabbedPane();
 		innerTabbedPane.setPreferredSize(new Dimension(450, 150));
 		setDefaultSettings();
 
 		populateTabs(innerTabbedPane);
 		keySettingsPane.add(innerTabbedPane, BorderLayout.CENTER);
 	}
 
 	public int getNumberOfLines(File file) {
 		int count = 0;
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new FileReader(file));
 			while ((br.readLine()) != null) {
 				count++;
 			}
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return count;
 	}
 
 	public void setDefaultSettings() {
 		// read from file here
 		BufferedReader br = null;
 		JPanel panels[] = new JPanel[5];
 		ArrayList<String> temp = new ArrayList<String>();
 		try {
 			String sCurrentLine;
 			File file = new File("SmartController/Data", "keyMappings.txt");
 			int count = 0;
 			if (!file.isFile() || getNumberOfLines(file) != 5) {
 				// create with default settings
 				System.out
 						.println("File corrupted/ missing.. Restoring default values..");
 				for (int i = 0; i < 5; i++) {
 					if (i == 0) {
 						panels[i] = createTiltTab(defaultData, tables, i);
 						for (int j = 0; j < 5; j++)
 							temp.add((String) defaultData[j][1]);
 					} else {
 						panels[i] = createTiltTab(emptyData, tables, i);
 						for (int j = 0; j < 5; j++)
 							temp.add((String) emptyData[j][1]);
 					}
 				}
 			} else {
 				br = new BufferedReader(new FileReader(file));
 				while ((sCurrentLine = br.readLine()) != null) {
 					String[] tokens = sCurrentLine.split("\\s+");
 					for (int i = 0; i < tokens.length; i++) {
 						if (count == 0)
 							defaultData[i][1] = tokens[i];
 						else
 							emptyData[i][1] = tokens[i];
 					}
 					if (count == 0) {
 						panels[0] = createTiltTab(defaultData, tables, 0);
 						for (int j = 0; j < 5; j++)
 							temp.add((String) defaultData[j][1]);
 					} else {
 						panels[count] = createTiltTab(emptyData, tables, count);
 						for (int j = 0; j < 5; j++)
 							temp.add((String) emptyData[j][1]);
 					}
 					count++;
 				}
 			}
 			keyboardControlMapping.clear();
 			for (int i = 0; i < temp.size(); i++) {
 				keyboardControlMapping.add(temp.get(i));
 			}
 			assignPanels(panels);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (br != null)
 					br.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	private JPanel createTiltTab(Object[][] data, final JTable[] tables,
 			final int index) {
 		// TODO Auto-generated method stub
 		JPanel pane = new JPanel(new BorderLayout());
 
 		tables[index] = new JTable(data, columnNames) {
 			@Override
 			public boolean getScrollableTracksViewportWidth() {
 				return getPreferredSize().width < getParent().getWidth();
 			}
 
 			@Override
 			// left column is not editable
 			public boolean isCellEditable(int row, int column) {
 				if (column == 0) {
 					setColumnSelectionAllowed(false);
 					return false;
 				} else {
 					return true;
 				}
 			}
 		};
 
 		JScrollPane jsp = new JScrollPane(tables[index],
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		tables[index].setRowHeight(20);
 		tables[index].setPreferredScrollableViewportSize(tables[index]
 				.getPreferredSize());
 		TableColumn col = tables[index].getColumnModel().getColumn(1);
 
 		tables[index].setCellSelectionEnabled(true);
 		tables[index].getSelectionModel().addListSelectionListener(
 				new ListSelectionListener() {
 					@Override
 					public void valueChanged(ListSelectionEvent lse) {
 						if (!lse.getValueIsAdjusting()) {
 							// do stuff
 							int col = tables[index].getSelectedColumn();
 							int row = tables[index].getSelectedRow();
 							if (!tables[index].isCellEditable(row, col))
 								tables[index].changeSelection(row, col + 1,
 										false, false);
 						}
 					}
 				});
 		JComboBox<String> comboBox = new JComboBox<String>();
 		for (String key : defaultKeyboardControls) {
 			comboBox.addItem(key);
 		}
 		// setKeyMapping();
 
 		col.setCellEditor(new DefaultCellEditor(comboBox));
 		pane.add(jsp);
 
 		// resetOriginalValues();
 		return pane;
 	}
 
 	public void assignPanels(JPanel[] panels) {
 		noTiltPane = panels[0];
 		tiltUpPane = panels[1];
 		tiltDownPane = panels[2];
 		tiltLeftPane = panels[3];
 		tiltRightPane = panels[4];
 	}
 
 	public void populateTabs(JTabbedPane innerTabbedPane) {
 		innerTabbedPane.addTab("No Tilt", noTiltPane);
 		innerTabbedPane.addTab("Tilt up", tiltUpPane);
 		innerTabbedPane.addTab("Tilt down", tiltDownPane);
 		innerTabbedPane.addTab("Tilt left", tiltLeftPane);
 		innerTabbedPane.addTab("Tilt right", tiltRightPane);
 	}
 
 	/*
 	 * public void setKeyMapping() { File f = new
 	 * File("SmartController/Data/keyMappings.txt");
 	 * 
 	 * if(f.exists()){ //There is an existing keyMap file, read it for keyMap
 	 * settings keyboardControlMapping = new
 	 * ArrayList<String>(readKeyMapFile()); }else{ //No existing keyMap file,
 	 * read from default setting keyboardControlMapping = new
 	 * ArrayList<String>(); for(String key: defaultKeyboardControls) {
 	 * keyboardControlMapping.add(key); } } }
 	 */
 
 	public File createKeyMapFile() {
 		File dir = new File("SmartController/Data");
 		dir.mkdirs();
 		File file = new File(dir, "keyMappings.txt");
 		System.out.println("File created: " + file.getAbsolutePath());
 		try {
 			if (!file.isFile() && !file.createNewFile()) {
 				throw new IOException("Error creating new file: "
 						+ file.getAbsolutePath());
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return file;
 	}
 
 	public void buildComponentsInKeySettings(JPanel layout, String mouseElement) {
 		JLabel dirControl = new JLabel("Direction Keys");
 		layout.add(dirControl);
 		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
 		for (String value : directionKeys) {
 			model.addElement(value);
 		}
 		comboBox = new JComboBox<String>(model);
 		comboBox.setSize(1, 1);
 		layout.add(comboBox);
 
 		// add the image label
 		// TODO get better GIF
 		JLabel imageLabel = new JLabel();
 		ImageIcon ii = new ImageIcon("src/Images/phoneswipe.gif");
 		imageLabel.setIcon(ii);
 		layout.add(imageLabel, java.awt.BorderLayout.CENTER);
 		// show it
 		this.setLocationRelativeTo(null);
 		this.setVisible(true);
 
 		JLabel mouseMove = new JLabel("Mouse Control");
 		layout.add(mouseMove);
 
 		DefaultComboBoxModel<String> mouseModel = new DefaultComboBoxModel<String>();
 		mouseModel.addElement(mouseElement);
 
 		comboBoxMouse = new JComboBox<String>(mouseModel);
 		comboBoxMouse.setSize(1, 1);
 		layout.add(comboBoxMouse);
 	}
 
 	private void createGeneralTab() {
 		// TODO Auto-generated method stub
 		generalPane = new JPanel(new BorderLayout());
 		generalPane.setLayout(new GridLayout(10, 1));
 		generalPane.setBorder(new EmptyBorder(10, 10, 10, 10));
 
 		JLabel heading = new JLabel(" Server Configuration ");
 		heading.setFont(new Font(heading.getName(), Font.BOLD, 20));
 		generalPane.add(heading);
 
 		JLabel connectionMode = new JLabel(
 				" Choose your preferred connection type: ");
 		generalPane.add(connectionMode);
 
 		JRadioButton wifiButton = new JRadioButton("Wifi");
 		wifiButton.setSelected(true);
 
 		JRadioButton bluetoothButton = new JRadioButton("Bluetooth");
 		JRadioButton wifiDirectButton = new JRadioButton("Wifi Direct");
 		final JButton connect = new JButton("Connect");
 		connect.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				int reply = JOptionPane
 						.showConfirmDialog(
 								null,
 								"Once confirmed, you will be not be able to change your choice!",
 								"Connect now?", JOptionPane.YES_NO_OPTION);
 
 				if (reply == JOptionPane.YES_OPTION) {
 					connect.setText("Choice confirmed");
 					userChoice = getConnectionType();
 					connect.setEnabled(false);
 				}
 			}
 		});
 
 		ButtonGroup group = new ButtonGroup();
 		group.add(wifiButton);
 		group.add(bluetoothButton);
 		group.add(wifiDirectButton);
 		group.add(connect);
 
 		wifiButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				connectionType = 1;
 			}
 		});
 
 		bluetoothButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				connectionType = 2;
 			}
 		});
 
 		JPanel radioPanel = new JPanel();
 		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.LINE_AXIS));
 		radioPanel.add(wifiButton);
 		radioPanel.add(bluetoothButton);
 		radioPanel.add(wifiDirectButton);
 		radioPanel.add(connect);
 		radioPanel.setVisible(true);
 		generalPane.add(radioPanel);
 
 		Enumeration<NetworkInterface> e;
 		String ip = "";
 		try {
 			e = NetworkInterface.getNetworkInterfaces();
 			while (e.hasMoreElements()) {
 				NetworkInterface n = (NetworkInterface) e.nextElement();
 				Enumeration<InetAddress> ee = n.getInetAddresses();
 				while (ee.hasMoreElements()) {
 					InetAddress i = (InetAddress) ee.nextElement();
 					if (i.isSiteLocalAddress()) {
						ip += i.getHostAddress() + "\n";
 						// System.out.println(i.getHostAddress());
 					}
 				}
 			}
 		} catch (SocketException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		JLabel ipConfig = new JLabel(" IP Address: " + ip);
 		generalPane.add(ipConfig);
 
 		JLabel port = new JLabel(" Port: " + PORT);
 		generalPane.add(port);
 
 		// change this dynamically
 		generalPane.add(status);
 
 		generalPane.add(new JSeparator(SwingConstants.HORIZONTAL));
 
 		JLabel mouseCursor = new JLabel(" Mouse Cursor Sensitivity ");
 		mouseCursor.setFont(new Font(mouseCursor.getName(), Font.BOLD, 20));
 		generalPane.add(mouseCursor);
 
 		JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL, FPS_MIN,
 				FPS_MAX, FPS_INIT);
 		framesPerSecond.addChangeListener(new SliderListener());
 
 		framesPerSecond.setMajorTickSpacing(10);
 		framesPerSecond.setMinorTickSpacing(1);
 		framesPerSecond.setPaintTicks(true);
 		framesPerSecond.setPaintLabels(true);
 
 		generalPane.add(framesPerSecond);
 
 		JLabel msgToUser = new JLabel(MESSAGETOUSER);
 		msgToUser.setFont(new Font("Serif", Font.ITALIC, 15));
 		generalPane.add(msgToUser);
 
 	}
 
 	public int getConnectionType() {
 		return connectionType;
 	}
 
 	public float getMouseRatio() {
 		return mouseRatio;
 	}
 
 	public void updateStatus(boolean value) {
 		// TODO
 		// call this function whenever a connection is made or broken
 		if (value) {
 			status.setText(" Connection Status: Connected");
 		} else {
 			status.setText(" Connection Status: Disconnected");
 		}
 	}
 
 	class SliderListener implements ChangeListener {
 
 		public void stateChanged(ChangeEvent e) {
 			JSlider source = (JSlider) e.getSource();
 			if (!source.getValueIsAdjusting()) {
 				int fps = (int) source.getValue();
 				System.out.println("Slider value" + fps);
 				// TODO
 				// change according to mouse cursor movement
 				mouseRatio = (float) (fps * 0.1);
 			}
 		}
 	}
 }
