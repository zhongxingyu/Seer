 package dk.dma.aiscoverage.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JTabbedPane;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.JRadioButton;
 import javax.swing.UIManager;
 
 import dk.dma.aiscoverage.calculator.AbstractCalculator;
 import dk.dma.aiscoverage.calculator.CoverageCalculator;
 import dk.dma.aiscoverage.calculator.DensityPlotCalculator;
 import dk.dma.aiscoverage.data.Ship.ShipClass;
 import dk.dma.aiscoverage.project.ProjectHandler;
 import dk.frv.ais.message.ShipTypeCargo;
 import dk.frv.ais.message.ShipTypeCargo.ShipType;
 
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JCheckBox;
 import java.awt.FlowLayout;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.swing.SwingConstants;
 
 public class NewAnalysis2 extends JFrame implements KeyListener, MouseListener {
 
 	private String filePath;
 	private long id;
 
 	// helper tools
 	private GUIHelper guiHelper = new GUIHelper();
 
 	// panels
 	private JPanel contentPane;
 	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 	JPanel inputPanel = new JPanel();
 	JScrollPane scrollPane = new JScrollPane();
 
 	// panel filling
 	JTextArea ta = new JTextArea("Click here to select file");
 
 	// buttons
 	// input panel
 	ButtonGroup bg = new ButtonGroup();
 	JRadioButton rdbtnInputFromStream = new JRadioButton("Input from Stream");
 	JRadioButton rdbtnInputFromFile = new JRadioButton("Input from File");
 	JButton btnSelectFile = new JButton("Select File");
 	final JCheckBox chckbxSetAnalysisTimer = new JCheckBox("Enable timer");
 	ButtonGroup bgShipAB = new ButtonGroup();
 
 	// coverage panel
 	final JCheckBox chckbxEnableCoverage = new JCheckBox("Enabled");
 	final JCheckBox chckbxCoverageAdvancedSettings = new JCheckBox(
 			"Turn on advanced settings");
 	JCheckBox chckbxIncludeTurningShips = new JCheckBox(
 			"Include turning ships, turning");
 
 	// density panel
 	final JCheckBox chckbxEnableDensity = new JCheckBox("Enabled");
 
 	// advanced panel
 	final JCheckBox chckbxSetMapCenterpoint = new JCheckBox(
 			"Set Map Centerpoint");
 
 	// frame buttons
 	JButton btnCancel = new JButton("Cancel");
 	JButton btnNew = new JButton("New");
 
 	// project thingies
 	ProjectHandler projectHandler = ProjectHandler.getInstance();
 
 	private JTextField coverageCellsizeTxt;
 	private JTextField messageBufferTxt;
 	private JTextField rotationTxt;
 	private JTextField densityCellSizeTxt;
 	private JTextField analysisTime;
 	private JTextField txtLat;
 	private JTextField txtLong;
 	private final JPanel advancedSettingsPanel = new JPanel();
 	private final JLabel lblHigh = new JLabel("High");
 	private final JTextField highTxt = new JTextField();
 	private final JPanel higMedLowPanel = new JPanel();
 	private final JLabel lblMedium = new JLabel("Medium");
 	private final JTextField mediumTxt = new JTextField();
 	private final JLabel lblLow = new JLabel("Low");
 	private final JTextField lowTxt = new JTextField();
 	private final JPanel classABPanel = new JPanel();
 	private final JRadioButton rdbtnClassA = new JRadioButton("Class A");
 	private final JRadioButton rdbtnClassA_B = new JRadioButton("Class A & B");
 	private final JRadioButton rdbtnClassB = new JRadioButton("Class B");
 	private final JPanel shipTypePanel = new JPanel();
 	private final JCheckBox chckbxIncludeAll = new JCheckBox("Include all");
 	private final JScrollPane scrollPane_1 = new JScrollPane();
 	private final JPanel typePanel = new JPanel();
 	private ArrayList<JCheckBox> shipTypeFiltering = new ArrayList<JCheckBox>();
 	private final ShipType[] shippy= ShipTypeCargo.ShipType.values();
 
 
 	/**
 	 * Create the frame.
 	 */
 	public NewAnalysis2(final AnalysisPanel ap, final ChartPanel cp) {
 		setAlwaysOnTop(true);
 		lowTxt.setToolTipText("When there are this many ships in a cell per day, it's considered to have a very light load");
 		lowTxt.setHorizontalAlignment(SwingConstants.RIGHT);
 		lowTxt.setText("1");
 		lowTxt.setBounds(66, 80, 80, 20);
 		lowTxt.setColumns(10);
 		mediumTxt.setToolTipText("When there are this many ships in a cell per day, it's considered to have a moderate load");
 		mediumTxt.setHorizontalAlignment(SwingConstants.RIGHT);
 		mediumTxt.setText("5");
 		mediumTxt.setBounds(66, 50, 80, 20);
 		mediumTxt.setColumns(10);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 430, 460);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		id = 1;
 
 		// ta.addKeyListener(this);
 		// ta.addke
 
 		ta.addKeyListener(this);
 
 		/*
 		 * tab panel
 		 */
 		tabbedPane.setLocation(10, 5);
 		tabbedPane.setSize(new Dimension(394, 370));
 		contentPane.add(tabbedPane);
 
 		/*
 		 * input tab
 		 */
 		tabbedPane.addTab("Input", null, inputPanel, null);
 		inputPanel.setLayout(null);
 
 		// text area
 		scrollPane.setBounds(15, 40, 360, 80);
 		inputPanel.add(scrollPane);
 		scrollPane
 				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollPane.setViewportView(ta);
 		ta.setEditable(false);
 		ta.setEnabled(false);
 		ta.addMouseListener(this);
 		
 //		addActionListener(new ActionListener() {
 //			public void actionPerformed(ActionEvent e) {
 //				filePath = guiHelper.openAISFileDialog();
 //				if(filePath != null)
 //				{
 //				String[] chunks = filePath.split("\\\\");
 //				final String filename = chunks[chunks.length - 1];
 //				ta.setText(filename);
 //				btnNew.setEnabled(true);
 //				}
 //			}
 //		});
 		
 		
 		
 
 		// select file button
 		btnSelectFile.setBounds(285, 131, 90, 23);
 		//inputPanel.add(btnSelectFile);
 		btnSelectFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				filePath = guiHelper.openAISFileDialog();
 				if(filePath != null)
 				{
 				String[] chunks = filePath.split("\\\\");
 				final String filename = chunks[chunks.length - 1];
 				ta.setText(filename);
 				btnNew.setEnabled(true);
 				}
 			}
 		});
 
 		// input from stream selection
 		rdbtnInputFromStream.setBounds(265, 10, 135, 23);
 		inputPanel.add(rdbtnInputFromStream);
 		bg.add(rdbtnInputFromStream);
 		rdbtnInputFromStream.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				btnSelectFile.setEnabled(false);
 				ta.setText("");
 				ta.setEditable(true);
 				ta.setEnabled(true);
 				btnNew.setEnabled(false);
 			}
 		});
 
 		// input from file selection
 		rdbtnInputFromFile.setBounds(160, 10, 100, 23);
 		inputPanel.add(rdbtnInputFromFile);
 		bg.add(rdbtnInputFromFile);
 		rdbtnInputFromFile.setSelected(true);
 		rdbtnInputFromFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				btnSelectFile.setEnabled(true);
 				ta.setText("Select File");
 				ta.setEditable(false);
 				ta.setEnabled(false);
 				btnNew.setEnabled(false);
 			}
 		});
 
 		classABPanel.setBounds(15, 222, 135, 106);
 		inputPanel.add(classABPanel);
 		classABPanel.setBorder(new TitledBorder(null,
 				"Include class A & B ships", TitledBorder.LEADING,
 				TitledBorder.TOP, null, null));
 		classABPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
 		rdbtnClassA.setSelected(true);
 
 		classABPanel.add(rdbtnClassA);
 
 		classABPanel.add(rdbtnClassA_B);
 
 		classABPanel.add(rdbtnClassB);
 
 		bgShipAB.add(rdbtnClassA);
 		bgShipAB.add(rdbtnClassA_B);
 		bgShipAB.add(rdbtnClassB);
 
 		shipTypePanel.setBounds(160, 131, 215, 197);
 		inputPanel.add(shipTypePanel);
 		shipTypePanel.setBorder(new TitledBorder(null,
 				"Select included ship types", TitledBorder.LEADING,
 				TitledBorder.TOP, null, null));
 		shipTypePanel.setLayout(null);
 		chckbxIncludeAll.setSelected(true);
 		chckbxIncludeAll.setBounds(10, 15, 150, 23);
 		shipTypePanel.add(chckbxIncludeAll);
 		chckbxIncludeAll.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if(chckbxIncludeAll.isSelected() == true)
 				{
 					for (JCheckBox chckbx : shipTypeFiltering) {
 						chckbx.setEnabled(false);
 					}
 				}
 				else
 				{
 					for (JCheckBox chckbx : shipTypeFiltering) {
 						chckbx.setEnabled(true);
 					}
 				}
 			}
 		});
 
 		scrollPane_1.setBounds(15, 45, 185, 135);
 		shipTypePanel.add(scrollPane_1);
 		scrollPane_1.setViewportView(typePanel);
 		scrollPane_1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		scrollPane_1.getVerticalScrollBar().setUnitIncrement(16);
 		typePanel.setLayout(new GridLayout(0, 1, 0, 0));
 		
 		JPanel panel_1 = new JPanel();
 		panel_1.setToolTipText("Set a timer for how long the analysis should run. \\nMost usefull when tracking live streams");
 		panel_1.setBorder(new TitledBorder(null, "Analysis Timer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_1.setBounds(15, 131, 135, 80);
 		inputPanel.add(panel_1);
 		panel_1.setLayout(null);
		chckbxSetAnalysisTimer.setBounds(10, 16, 123, 28);
 		chckbxSetAnalysisTimer.setHorizontalAlignment(SwingConstants.LEFT);
 		panel_1.add(chckbxSetAnalysisTimer);
 		
 				analysisTime = new JTextField();
 				analysisTime.setLocation(12, 45);
 				panel_1.add(analysisTime);
 				analysisTime.setHorizontalAlignment(SwingConstants.LEFT);
 				analysisTime.setEditable(false);
 				analysisTime.setText("00:00:00");
 				analysisTime.setSize(new Dimension(52, 20));
 		chckbxSetAnalysisTimer.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (chckbxSetAnalysisTimer.isSelected() == true) {
 					analysisTime.setEditable(true);
 					contentPane.repaint();
 				} else {
 					analysisTime.setEditable(false);
 					contentPane.repaint();
 				}
 			}
 		});
 
 		for (ShipType type : shippy) {
 			String chckname = type.toString();
 			JCheckBox chck = new JCheckBox(chckname);
 			chck.setEnabled(false);
 			typePanel.add(chck);
 			shipTypeFiltering.add(chck);
 		}
 		
 
 		/*
 		 * coverage tab
 		 */
 		JPanel coveragePanel = new JPanel();
 		tabbedPane.addTab("Coverage Analysis", null, coveragePanel, null);
 		coveragePanel.setLayout(null);
 
 		JLabel lblCellsize = new JLabel("Cellsize: ");
 		lblCellsize.setBounds(20, 43, 46, 14);
 		coveragePanel.add(lblCellsize);
 
 		coverageCellsizeTxt = new JTextField();
 		coverageCellsizeTxt.setToolTipText("The width and height of each tile. \\nUnless  map centerpoint is selected, the meter representation will be translated into a lat/long degree difference based on the first message read");
 		coverageCellsizeTxt.setText("2500");
 		coverageCellsizeTxt.setBounds(75, 40, 80, 20);
 		coveragePanel.add(coverageCellsizeTxt);
 		coverageCellsizeTxt.setEditable(true);
 		coverageCellsizeTxt.setHorizontalAlignment(coverageCellsizeTxt.RIGHT);
 
 		JLabel lblMeter = new JLabel("Meter");
 		lblMeter.setBounds(170, 43, 46, 14);
 		coveragePanel.add(lblMeter);
 
 		chckbxEnableCoverage.setSelected(true);
 		chckbxEnableCoverage.setBounds(15, 10, 97, 23);
 		coveragePanel.add(chckbxEnableCoverage);
 
 		advancedSettingsPanel.setBounds(15, 100, 360, 80);
 		coveragePanel.add(advancedSettingsPanel);
 		advancedSettingsPanel.setLayout(null);
 		advancedSettingsPanel.setBorder(new TitledBorder(null,
 				"Advanced settings", TitledBorder.LEADING, TitledBorder.TOP,
 				null, null));
 
 		JLabel lblMessageBuffer = new JLabel("Message buffer");
 		lblMessageBuffer.setBounds(15, 23, 96, 14);
 		advancedSettingsPanel.add(lblMessageBuffer);
 
 		messageBufferTxt = new JTextField();
 		messageBufferTxt.setToolTipText("The amount of time between the messages saved in the message buffer. When the time limit is meet, the calculation will start on the messages in the buffer.");
 		messageBufferTxt.setHorizontalAlignment(SwingConstants.RIGHT);
 		messageBufferTxt.setBounds(180, 20, 45, 20);
 		advancedSettingsPanel.add(messageBufferTxt);
 		messageBufferTxt.setEditable(false);
 		messageBufferTxt.setText("20");
 		messageBufferTxt.setColumns(10);
 
 		JLabel lblSekunder = new JLabel("Sekunder");
 		lblSekunder.setBounds(235, 23, 46, 14);
 		advancedSettingsPanel.add(lblSekunder);
 		chckbxIncludeTurningShips.setBounds(10, 45, 167, 23);
 		chckbxIncludeTurningShips.setEnabled(false);
 		advancedSettingsPanel.add(chckbxIncludeTurningShips);
 
 		rotationTxt = new JTextField();
 		rotationTxt.setToolTipText("The limit which the rotation has to be over, to be counted as turning in the system.");
 		rotationTxt.setHorizontalAlignment(SwingConstants.RIGHT);
 		rotationTxt.setBounds(180, 45, 45, 20);
 		advancedSettingsPanel.add(rotationTxt);
 		rotationTxt.setEditable(false);
 		rotationTxt.setText("20");
 		rotationTxt.setColumns(10);
 
 		JLabel lblDegrees = new JLabel("Degrees per min");
 		lblDegrees.setBounds(235, 48, 96, 14);
 		advancedSettingsPanel.add(lblDegrees);
 		chckbxIncludeTurningShips.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (chckbxIncludeTurningShips.isSelected() == true) {
 					rotationTxt.setEditable(true);
 					contentPane.repaint();
 				} else {
 					rotationTxt.setEditable(false);
 					contentPane.repaint();
 				}
 			}
 		});
 		chckbxCoverageAdvancedSettings.setBounds(15, 75, 186, 23);
 		coveragePanel.add(chckbxCoverageAdvancedSettings);
 		chckbxCoverageAdvancedSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (chckbxCoverageAdvancedSettings.isSelected() == true) {
 					messageBufferTxt.setEditable(true);
 					chckbxIncludeTurningShips.setEnabled(true);
 
 					if (chckbxIncludeTurningShips.isSelected() == true) {
 						rotationTxt.setEditable(true);
 					}
 					contentPane.repaint();
 				} else {
 					messageBufferTxt.setEditable(false);
 					rotationTxt.setEditable(false);
 					chckbxIncludeTurningShips.setEnabled(false);
 					contentPane.repaint();
 				}
 			}
 		});
 		
 		chckbxEnableCoverage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (chckbxEnableCoverage.isSelected() == true) {
 					coverageCellsizeTxt.setEditable(true);
 					if (chckbxCoverageAdvancedSettings.isSelected() == true) {
 						messageBufferTxt.setEditable(true);
 						if (chckbxIncludeTurningShips.isSelected() == true) {
 							rotationTxt.setEditable(true);
 						}
 					}
 					contentPane.repaint();
 				} else {
 					coverageCellsizeTxt.setEditable(false);
 					messageBufferTxt.setEditable(false);
 					rotationTxt.setEditable(false);
 					contentPane.repaint();
 				}
 			}
 		});
 
 		/*
 		 * density tab
 		 */
 		JPanel densityPanel = new JPanel();
 		tabbedPane.addTab("Density Plot", null, densityPanel, null);
 		densityPanel.setLayout(null);
 
 		JLabel lblCellsize_1 = new JLabel("Cellsize");
 		lblCellsize_1.setBounds(30, 43, 46, 14);
 		densityPanel.add(lblCellsize_1);
 
 		densityCellSizeTxt = new JTextField();
 		densityCellSizeTxt.setToolTipText("The size of cells in the density plot");
 		densityCellSizeTxt.setHorizontalAlignment(SwingConstants.RIGHT);
 		densityCellSizeTxt.setText("200");
 		densityCellSizeTxt.setBounds(80, 40, 80, 20);
 		densityPanel.add(densityCellSizeTxt);
 		densityCellSizeTxt.setEditable(true);
 
 		JLabel lblMeter_1 = new JLabel("Meter");
 		lblMeter_1.setBounds(170, 43, 46, 14);
 		densityPanel.add(lblMeter_1);
 
 		chckbxEnableDensity.setSelected(true);
 		chckbxEnableDensity.setBounds(15, 10, 97, 23);
 		densityPanel.add(chckbxEnableDensity);
 		higMedLowPanel.setToolTipText("The amount of ships in each cell per day.");
 		higMedLowPanel.setBounds(15, 68, 335, 110);
 		higMedLowPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Threshold values", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 
 		densityPanel.add(higMedLowPanel);
 		higMedLowPanel.setLayout(null);
 		lblHigh.setBounds(15, 23, 46, 14);
 		higMedLowPanel.add(lblHigh);
 		highTxt.setToolTipText("When there are more ships then this value in a cell per day, it's considered heavy loaded");
 		highTxt.setHorizontalAlignment(SwingConstants.RIGHT);
 		highTxt.setBounds(66, 20, 80, 20);
 		higMedLowPanel.add(highTxt);
 		highTxt.setText("20");
 		highTxt.setColumns(10);
 		lblMedium.setBounds(15, 53, 46, 14);
 
 		higMedLowPanel.add(lblMedium);
 
 		higMedLowPanel.add(mediumTxt);
 		lblLow.setBounds(15, 83, 46, 14);
 
 		higMedLowPanel.add(lblLow);
 
 		higMedLowPanel.add(lowTxt);
 		chckbxEnableDensity.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (chckbxEnableDensity.isSelected() == true) {
 					densityCellSizeTxt.setEditable(true);
 					contentPane.repaint();
 				} else {
 					densityCellSizeTxt.setEditable(false);
 					contentPane.repaint();
 				}
 			}
 		});
 
 		/*
 		 * advanced settings tab
 		 */
 		JPanel advancedPanel = new JPanel();
 		tabbedPane.addTab("Advanced Settings", null, advancedPanel, null);
 		advancedPanel.setLayout(null);
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder(null, "Map settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.setBounds(10, 11, 369, 125);
 		advancedPanel.add(panel);
 		panel.setLayout(null);
 		chckbxSetMapCenterpoint.setBounds(10, 20, 125, 23);
 		panel.add(chckbxSetMapCenterpoint);
 		
 				JLabel lblLat = new JLabel("Lat");
 				lblLat.setBounds(15, 60, 46, 14);
 				panel.add(lblLat);
 				
 						txtLat = new JTextField();
 						txtLat.setBounds(46, 57, 46, 20);
 						panel.add(txtLat);
 						txtLat.setEditable(false);
 						txtLat.setColumns(10);
 						
 								JLabel lblLong = new JLabel("Long");
 								lblLong.setBounds(15, 92, 46, 14);
 								panel.add(lblLong);
 								
 										txtLong = new JTextField();
 										txtLong.setBounds(46, 89, 46, 20);
 										panel.add(txtLong);
 										txtLong.setEditable(false);
 										txtLong.setColumns(10);
 		chckbxSetMapCenterpoint.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (chckbxSetMapCenterpoint.isSelected() == true) {
 					txtLat.setEditable(true);
 					txtLong.setEditable(true);
 					contentPane.repaint();
 				} else {
 					txtLat.setEditable(false);
 					txtLong.setEditable(false);
 					contentPane.repaint();
 				}
 			}
 		});
 
 		/*
 		 * frame buttons
 		 */
 
 		btnCancel.setBounds(315, 386, 89, 23);
 		contentPane.add(btnCancel);
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 
 		/*
 		 * new button
 		 */
 		// JButton btnNew = new JButton("New");
 		btnNew.setEnabled(false);
 		btnNew.setBounds(212, 386, 89, 23);
 		contentPane.add(btnNew);
 		btnNew.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String densityCellSize = "-";
 				String coverageCellSize = "-";
 				//the used project
 				dk.dma.aiscoverage.project.AisCoverageProject project = projectHandler.createProject();
 
 				 //adds selected file, or loop thru selected streams and add them to the project
 				String input = setInput(project);
 				String timer = setAnalysisTimer(project);
 
 
 				/*
 				 * is coverage enabled. if so, add calculator
 				 */
 				if (chckbxEnableCoverage.isSelected() == true) {
 					// add coverage calculator
 					CoverageCalculator coverageCalc = new CoverageCalculator(project, true);
 					coverageCalc.setCellSize(Integer.parseInt(coverageCellsizeTxt.getText()));
 					coverageCellSize = coverageCellsizeTxt.getText();
 					filterShipClass(coverageCalc);
 					filterCargoType(coverageCalc);
 					coverageCalc.setBufferInSeconds(Integer.parseInt(messageBufferTxt.getText()));
 
 						if (chckbxIncludeTurningShips.isSelected() == true) {
 							coverageCalc.setIgnoreRotation(false);
 							coverageCalc.setDegreesPerMinute(Integer.parseInt(rotationTxt.getText()));
 						}
 					
 					project.addCalculator(coverageCalc);
 				}
 
 				/*
 				 * is density enabled, if so, if so add calculator
 				 */
 				if (chckbxEnableDensity.isSelected() == true) {
 					DensityPlotCalculator densityCalc = new DensityPlotCalculator(project, true);
 					densityCalc.setCellSize(Integer.parseInt(densityCellSizeTxt.getText()));
 					densityCellSize = densityCellSizeTxt.getText();
 					filterShipClass(densityCalc);
 					filterCargoType(densityCalc);
 					
 					cp.getDensityPlotLayer().setHighMedLow(Integer.parseInt(highTxt.getText()), Integer.parseInt(mediumTxt.getText()), Integer.parseInt(lowTxt.getText()));
 					
 					project.addCalculator(densityCalc);
 					
 					
 					
 				}
 
 				//sets the recorded data in the analysisPanel
 				ap.setAnalysisData(input, coverageCellSize, densityCellSize, timer);
 				//Close the window
 				dispose();
 			}
 		});
 	}
 	
 	
 	
 	/*
 	 * are the analysis timer set? if so, add the timeout
 	 */
 	private String setAnalysisTimer(dk.dma.aiscoverage.project.AisCoverageProject project)
 	{
 		String timer = "--:--:--";
 		if (chckbxSetAnalysisTimer.isSelected() == true) {
 		int hour = Integer.parseInt(analysisTime.getText().substring(0, 2));
 		int min = Integer.parseInt(analysisTime.getText().substring(3, 5));
 		int sec = Integer.parseInt(analysisTime.getText().substring(6, 8));
 		int time = ((((hour * 60) + min) * 60) + sec);
 		project.setTimeout(time);
 		timer = analysisTime.getText();
 	}
 		return timer;
 	}
 	
 	
 	/*
 	 * pick ship classes to use in the calculators
 	 */
 	private void filterShipClass(AbstractCalculator calc)
 	{
 		if (rdbtnClassA.isSelected() == true) {
 			calc.getAllowedShipClasses().put(ShipClass.CLASS_A, ShipClass.CLASS_A);
 			
 		} else if (rdbtnClassA_B.isSelected() == true) {
 			calc.getAllowedShipClasses().put(ShipClass.CLASS_A, ShipClass.CLASS_A);
 			calc.getAllowedShipClasses().put(ShipClass.CLASS_B, ShipClass.CLASS_B);
 			
 		} else if (rdbtnClassB.isSelected() == true) {
 			calc.getAllowedShipClasses().put(ShipClass.CLASS_B, ShipClass.CLASS_B);
 		}
 	}
 	
 	
 	/*
 	 * if user only wants to track certain ship types, set the message filter here
 	 */
 	private void filterCargoType(AbstractCalculator calc)
 	{
 		if (chckbxIncludeAll.isSelected() == false) {
 			
 			for (JCheckBox chckbx : shipTypeFiltering) {
 
 				if(chckbx.isSelected() == true)
 				{
 				
 				for (ShipType type : shippy) {
 					System.out.println(type.toString());
 					if(type.toString() == chckbx.getText())
 					{
 						calc.getAllowedShipTypes().put(type, type);
 					}
 				}
 				
 				}
 	
 			}	
 		}
 	}
 
 	private String setInput(dk.dma.aiscoverage.project.AisCoverageProject project)
 	{
 		String input = "";
 		/*
 		 * is input is from file or streams
 		 */
 		if (rdbtnInputFromFile.isSelected() == true) {
 				project.setFile(filePath);
 				input = ta.getText();
 		} else if (rdbtnInputFromStream.isSelected() == true) {
 				String[] ips = ta.getText().split("\n");
 				for (String ip : ips) {
 					project.addHostPort(ip, String.valueOf(id));
 					id++;	}	
 				input = "Stream";
 		}
 		return input;
 	}
 	
 	
 	
 	@Override
 	public void keyTyped(KeyEvent e) {
 		//do nothing
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if (e.getSource() == ta) {
 
 			if(ta.getText().contains(":") == true && ta.getText().length() != 0) {
 
 					btnNew.setEnabled(true);
 
 			} else {
 				btnNew.setEnabled(false);
 			}
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// do nothing
 		
 	}
 
 
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (e.getSource() == ta) {
 			if(rdbtnInputFromFile.isSelected() == true)
 			{
 			filePath = guiHelper.openAISFileDialog();
 			if(filePath != null)
 			{
 			String[] chunks = filePath.split("\\\\");
 			final String filename = chunks[chunks.length - 1];
 			ta.setText(filename);
 			btnNew.setEnabled(true);
 			}
 			}
 		}
 		
 	}
 
 
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
