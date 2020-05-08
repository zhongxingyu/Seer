 package at.ac.tuwien.ldsc.group1.ui;
 
 import at.ac.tuwien.ldsc.group1.application.CsvParser;
 import at.ac.tuwien.ldsc.group1.application.CsvWriter;
 import at.ac.tuwien.ldsc.group1.application.E2CElasticityManager;
 import at.ac.tuwien.ldsc.group1.application.Scheduler;
 import at.ac.tuwien.ldsc.group1.domain.CloudOverallInfo;
 import at.ac.tuwien.ldsc.group1.domain.CloudStateInfo;
 import at.ac.tuwien.ldsc.group1.ui.interfaces.GuiLogger;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.ui.RefineryUtilities;
 
 import javax.annotation.Resource;
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.util.List;
 
 public class MainWindow extends JFrame implements GuiLogger {
    private static final String SHOW_PLOT_ACTION = "showPlot";
     private static final String OPEN_FILE_ACTION = "openFile";
     private static final String defaultTarget = "data/TestScenario1.csv";
 
     private E2CElasticityManager manager;
     @Resource(name = "overviewWriter") private CsvWriter overviewWriter;
     @Resource(name = "schedulers") private List<Scheduler> schedulers;
     @Resource(name = "csvParser") private CsvParser parser;
     @Resource(name = "scenarioWriter1") private CsvWriter scenarioWriter1;
     @Resource(name = "scenarioWriter2") private CsvWriter scenarioWriter2;
     @Resource(name = "scenarioWriter3") private CsvWriter scenarioWriter3;
 
     private File selectedFile = null;
     private JTextPane textPane;
     private JLabel selectedFileLabel;
     private JLabel lblNumberOfFederationPartners;
     private JSpinner federationPartnerSpinner;
     private JSpinner spinner;
     private ActionListener generalActionListener = new GeneralActionListener();
     private JComboBox<String> comboBox;
     private JCheckBox extrapolateTimestampsCbx;
 
     private XYSeries seriesConsumption;
     private XYSeries seriesVm;
     private XYSeries seriesPm;
     private boolean extrapolationActivated;
 
 
     private void initializeObjects() {
         seriesVm = new XYSeries("VMs");
         seriesPm = new XYSeries("PMs");
         seriesConsumption = new XYSeries("Consumption");
         manager = new E2CElasticityManager(parser, schedulers);
     }
 
     public MainWindow() {
         initializeGuiComponents();
         this.pack();
         RefineryUtilities.centerFrameOnScreen(this);
         this.setVisible(true);
         this.setSize(600, 700);
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initializeGuiComponents() {
         try {
             //This works only on windows.
             UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
         } catch (Exception e) {
         }
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         //Prepare the control area in the top region of the frame
         JPanel northContainer = new JPanel();
         BoxLayout layout = new BoxLayout(northContainer, BoxLayout.Y_AXIS);
         northContainer.setLayout(layout);
         this.getContentPane().add(northContainer, BorderLayout.NORTH);
 
         JPanel controlPanel = buildControlPanel();
         northContainer.add(controlPanel);
 
         //Prepare the text output area in the center region of the frame
         JPanel textContainer = buildTextArea();
         this.getContentPane().add(textContainer, BorderLayout.CENTER);
     }
 
     private JPanel buildControlPanel() {
         JPanel container = new JPanel();
         container.setBorder(new LineBorder(Color.GRAY));
         BoxLayout layout = new BoxLayout(container, BoxLayout.PAGE_AXIS);
         container.setLayout(layout);
         JPanel controlPanel = new JPanel();
         controlPanel.add(container);
 
         JLabel label = new JLabel("Configure simulation");
         container.add(label);
 
         JPanel configurationPanel = new JPanel();
         GridBagLayout gbl_configurationPanel = new GridBagLayout();
         gbl_configurationPanel.rowHeights = new int[]{0, 40, 40, 40, 40, 0};
         gbl_configurationPanel.columnWidths = new int[]{120, 60};
         configurationPanel.setLayout(gbl_configurationPanel);
         container.add(configurationPanel);
 
         JLabel lblPleaseSelectThe = new JLabel("Select the scenario file:");
         GridBagConstraints gbc_lblPleaseSelectThe = new GridBagConstraints();
         gbc_lblPleaseSelectThe.anchor = GridBagConstraints.EAST;
         gbc_lblPleaseSelectThe.insets = new Insets(0, 0, 5, 5);
         gbc_lblPleaseSelectThe.gridx = 0;
         gbc_lblPleaseSelectThe.gridy = 0;
         configurationPanel.add(lblPleaseSelectThe, gbc_lblPleaseSelectThe);
 
         JButton openFileButton = new JButton("Open File");
         GridBagConstraints gbc_openFileButton = new GridBagConstraints();
         gbc_openFileButton.anchor = GridBagConstraints.EAST;
         gbc_openFileButton.insets = new Insets(0, 0, 5, 0);
         gbc_openFileButton.gridx = 1;
         gbc_openFileButton.gridy = 0;
         configurationPanel.add(openFileButton, gbc_openFileButton);
         openFileButton.addActionListener(generalActionListener);
         openFileButton.setActionCommand(OPEN_FILE_ACTION);
 
         openFileButton.setBounds(56, 13, 95, 25);
         selectedFileLabel = new JLabel("TestScenario1.csv");
         GridBagConstraints gbc_selectedFileLabel = new GridBagConstraints();
         gbc_selectedFileLabel.anchor = GridBagConstraints.NORTH;
         gbc_selectedFileLabel.insets = new Insets(0, 0, 5, 0);
         gbc_selectedFileLabel.gridx = 1;
         gbc_selectedFileLabel.gridy = 1;
         configurationPanel.add(selectedFileLabel, gbc_selectedFileLabel);
 
         JLabel lblNumberOPms = new JLabel("Number of PMs:");
         lblNumberOPms.setBounds(66, 51, 85, 16);
         GridBagConstraints gbc_lblNumberOPms = new GridBagConstraints();
         gbc_lblNumberOPms.anchor = GridBagConstraints.EAST;
         gbc_lblNumberOPms.insets = new Insets(0, 0, 5, 5);
         gbc_lblNumberOPms.gridx = 0;
         gbc_lblNumberOPms.gridy = 2;
         configurationPanel.add(lblNumberOPms, gbc_lblNumberOPms);
 
         spinner = new JSpinner();
         spinner.setModel(new SpinnerNumberModel(new Integer(5), new Integer(1), null, new Integer(1)));
         spinner.setBounds(213, 51, 29, 20);
         GridBagConstraints gbc_spinner = new GridBagConstraints();
         gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
         gbc_spinner.insets = new Insets(0, 0, 5, 0);
         gbc_spinner.gridx = 1;
         gbc_spinner.gridy = 2;
         configurationPanel.add(spinner, gbc_spinner);
 
         JLabel lblScheduler = new JLabel("Scheduler:");
         lblScheduler.setBounds(65, 87, 86, 16);
         GridBagConstraints gbc_lblScheduler = new GridBagConstraints();
         gbc_lblScheduler.anchor = GridBagConstraints.EAST;
         gbc_lblScheduler.insets = new Insets(0, 0, 5, 5);
         gbc_lblScheduler.gridx = 0;
         gbc_lblScheduler.gridy = 3;
         configurationPanel.add(lblScheduler, gbc_lblScheduler);
 
         comboBox = new JComboBox();
         comboBox.setModel(new DefaultComboBoxModel(new String[]{
                 "Scheduler1",
                 "Scheduler2",
                 "Scheduler3"}));
         comboBox.setBounds(213, 84, 125, 22);
 
         GridBagConstraints gbc_comboBox = new GridBagConstraints();
         gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_comboBox.insets = new Insets(0, 0, 5, 0);
         gbc_comboBox.gridx = 1;
         gbc_comboBox.gridy = 3;
         configurationPanel.add(comboBox, gbc_comboBox);
 
         JButton btnRun = new JButton("Run");
         btnRun.addActionListener(generalActionListener);
         btnRun.setActionCommand(RUN_ACTION);
 
         lblNumberOfFederationPartners = new JLabel("Number of FederationPartners: ");
         lblNumberOfFederationPartners.setBounds(66, 131, 162, 14);
         GridBagConstraints gbc_lblNumberOfFederationPartners = new GridBagConstraints();
         gbc_lblNumberOfFederationPartners.insets = new Insets(0, 0, 5, 5);
         gbc_lblNumberOfFederationPartners.gridx = 0;
         gbc_lblNumberOfFederationPartners.gridy = 4;
         configurationPanel.add(lblNumberOfFederationPartners, gbc_lblNumberOfFederationPartners);
 
         federationPartnerSpinner = new JSpinner();
         federationPartnerSpinner.setBounds(252, 128, 29, 20);
         GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
         gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
         gbc_spinner_1.insets = new Insets(0, 0, 5, 0);
         gbc_spinner_1.gridx = 1;
         gbc_spinner_1.gridy = 4;
         configurationPanel.add(federationPartnerSpinner, gbc_spinner_1);
 
         extrapolateTimestampsCbx = new JCheckBox("Extrapolate");
         GridBagConstraints gbc_extrapolateTimestampsCbx = new GridBagConstraints();
         gbc_extrapolateTimestampsCbx.insets = new Insets(0, 0, 5, 0);
         gbc_extrapolateTimestampsCbx.gridx = 1;
         gbc_extrapolateTimestampsCbx.gridy = 5;
         extrapolateTimestampsCbx.addItemListener(new CheckboxItemListener());
         configurationPanel.add(extrapolateTimestampsCbx, gbc_extrapolateTimestampsCbx);
         btnRun.setBackground(Color.RED);
         btnRun.setBounds(453, 84, 89, 23);
         GridBagConstraints gbc_btnRun = new GridBagConstraints();
         gbc_btnRun.anchor = GridBagConstraints.EAST;
         gbc_btnRun.gridx = 1;
         gbc_btnRun.gridy = 6;
         configurationPanel.add(btnRun, gbc_btnRun);
 
         return controlPanel;
     }
 
     private JPanel buildTextArea() {
         JPanel textAreaPanel = new JPanel();
         BoxLayout layout = new BoxLayout(textAreaPanel, BoxLayout.Y_AXIS);
         textAreaPanel.setLayout(layout);
         JPanel container = new JPanel();
         BoxLayout layout2 = new BoxLayout(container, BoxLayout.Y_AXIS);
         container.setLayout(layout2);
         textAreaPanel.add(container);
         container.setSize(300, 300);
         JScrollPane scrollPane = new JScrollPane();
         scrollPane.setBounds(0, 0, 705, 338);
 
         textPane = new JTextPane();
         textPane.setMargin(new Insets(5, 5, 5, 5));
         textPane.setSize(400, 400);
         scrollPane.setViewportView(textPane);
         container.add(scrollPane);
         return textAreaPanel;
     }
 
     @Override
     public void writeGuiLog(CloudStateInfo guiLog) {
         seriesVm.add(guiLog.getTimestamp(), guiLog.getRunningVMs());
         seriesPm.add(guiLog.getTimestamp(), guiLog.getRunningPMs());
         seriesConsumption.add(guiLog.getTimestamp(), guiLog.getTotalPowerConsumption());
         this.textPane.setText(this.textPane.getText() + "\n" + guiLog.toString());
     }
 
     class GeneralActionListener implements ActionListener {
         ChartFrame ch = null;
 
         @Override
         public void actionPerformed(ActionEvent e) {
             switch (e.getActionCommand()) {
                case SHOW_PLOT_ACTION: {
                    showPlotAction();
                     break;
                 }
                 case OPEN_FILE_ACTION: {
                     openFileAction();
                     break;
                 }
             }
         }
 
         private void openFileAction() {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setCurrentDirectory(new File(defaultTarget));
             int returnVal = fileChooser.showOpenDialog(null);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = fileChooser.getSelectedFile();
                 selectedFileLabel.setText(file.getName());
                 selectedFile = file;
             }
         }
 
         private void showPlotAction() {
             ch = new ChartFrame(seriesVm, seriesPm, seriesConsumption);
             ch.setVisible(true);
         }
 
         private void runAction() {
             if(ch != null) {
                 ch.dispose();
             }
             initializeObjects();
             if (selectedFile != null) parser.setFileName(selectedFile.getAbsolutePath());
             scenarioWriter1.setGuiLogger(MainWindow.this);
             scenarioWriter1.activateExtrapolation(extrapolationActivated);
             scenarioWriter2.setGuiLogger(MainWindow.this);
             scenarioWriter2.activateExtrapolation(extrapolationActivated);
             scenarioWriter3.setGuiLogger(MainWindow.this);
             scenarioWriter3.activateExtrapolation(extrapolationActivated);
 
             for (Scheduler scheduler : schedulers) {
                 scheduler.setMaxNumberOfPhysicalMachines((Integer) spinner.getValue());
                 scheduler.setNumberOfFederationPartners((Integer) federationPartnerSpinner.getValue());
             }
             int index = comboBox.getSelectedIndex();
             manager.startSpecificSimulation(index);
 
             for (CloudOverallInfo c : manager.getCloudOverAllInfo()) {
                 overviewWriter.writeLine(c);
             }
             overviewWriter.close();
             showPlotAction();
         }
     }
 
     private class CheckboxItemListener implements ItemListener {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 extrapolationActivated = e.getStateChange() == ItemEvent.SELECTED;
             }
     }
 }
