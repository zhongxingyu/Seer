 package com.bigvisible.kanbansimulator;
 
 import static com.bigvisible.kanbansimulator.IterationParameter.startingAt;
 import static com.bigvisible.kanbansimulator.IterationParameter.WorkflowStepParameter.named;
 
 import java.awt.Container;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.table.TableModel;
 
 import org.apache.commons.lang3.StringUtils;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.category.DefaultCategoryDataset;
 
 
 public class GUI extends JFrame {
     private static final long serialVersionUID = -1045195339415169014L;
     private static GUI instance;
 
     private JFrame outputWindow;
 
     private JTextField scenarioName = new JTextField();
     private JTextField storiesInBacklog = new JTextField();
     private JTextArea outputTextArea = new JTextArea();
     private JTextField iterationsToRun = new JTextField();
 
     private JTable table;
     private JScrollPane scrollPane;
     private JButton runButton = new JButton("Run");
     private JLabel statusLabel;
     private DefaultCategoryDataset cfdData;
     private JFreeChart cummulativeFlowDiagram;
 
     public GUI() {
         outputWindow = new JFrame();
         
         setTitle("Kanban Simulator (\"Tom-U-later\")");
         
         scenarioName.setName("scenarioName");
         scenarioName.setText("(scenario description here)");
         
         storiesInBacklog.setName("storiesInBacklog");
         storiesInBacklog.setText("88");
 
         iterationsToRun.setName("iterationsToRun");
         iterationsToRun.setText("10");
 
         String[] columnNames = { "Iteration", "Batch Size", "BA", "Dev", "WebDev", "QA" };
         Object[][] data = { 
         		{ 1, 11, 13, 12, 12, 10 }, 
         		{ 2, "", "", "",  7, "" }, 
         		{ 3, "", "", "", "", "" }, 
         		{ 4, "10", "", "", "", "" },
                 { 5, "", "", "", 16, "" }, 
                 { 6, "", "", "", "", "" }, 
                 { 7, "", "",  7, 12,  7 }, 
                 { 8, "", "", "", "", "" },
                 { 9, "11", "", "", "", "" }, 
                 { 10, "", "","", "", "" },
 
         };
         table = new JTable(data, columnNames);
 
         scrollPane = new JScrollPane(table);
         table.setFillsViewportHeight(true);
 
         runButton.setName("runButton");
         runButton.addActionListener(this.new StartSimulationActionListener());
 
         statusLabel = new JLabel("Waiting for user to configure simulation.");
         statusLabel.setName("statusLabel");
 
         outputTextArea.setName("outputTextArea");
         outputTextArea.setText("Hello, Major Tom.");
 
         Container pane = getContentPane();
         pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
 
         JPanel scenarioNamePanel = new JPanel();
         scenarioNamePanel.add(new JLabel("Scenario Name:"));
         scenarioNamePanel.add(scenarioName);
         scenarioName.setColumns(30);
         
         JPanel storiesInBacklogPanel = new JPanel();
         storiesInBacklogPanel.add(new JLabel("Backlog:"));
         storiesInBacklogPanel.add(storiesInBacklog);
         storiesInBacklog.setColumns(2);
         
         JPanel iterationsToRunPanel = new JPanel();
         iterationsToRunPanel.add(new JLabel("Iterations To Run:"));
         iterationsToRunPanel.add(iterationsToRun);
         iterationsToRun.setColumns(2);
         
         JPanel iterationParameterPanel = new JPanel();
         iterationParameterPanel.add(table.getTableHeader());
         iterationParameterPanel.add(scrollPane);
         
         JPanel runButtonPanel = new JPanel();
         runButtonPanel.add(runButton);
         

        
         Container outputPane = outputWindow.getContentPane();
         outputPane.setLayout(new BoxLayout(outputPane, BoxLayout.Y_AXIS));
         
         cfdData = new DefaultCategoryDataset();
         cummulativeFlowDiagram = ChartFactory.createStackedAreaChart("Cummulative Flow Diagram", "Iteration", "Stories", cfdData, PlotOrientation.VERTICAL, true, true, false);
         JPanel cfdPanel = new ChartPanel(cummulativeFlowDiagram);
 
         JTabbedPane outputTabs = new JTabbedPane();
 
 
         WindowListener windowListener = this.new GUIWindowListener();
         addWindowListener(windowListener);
         outputWindow.addWindowListener(windowListener);
 
         add(scenarioNamePanel);
         add(storiesInBacklogPanel);
         add(iterationsToRunPanel);
         add(iterationParameterPanel);
         add(runButtonPanel);
         add(statusLabel);
 
         outputWindow.add(outputTabs);
         outputTabs.addTab("Cummulative Flow Diagram", cfdPanel);
         outputTabs.addTab("Raw Output", outputTextArea);
 
         setSize(500, 390);
         outputWindow.setSize(700,500);
         
         Point outputWindowLocation = getLocation();
         outputWindowLocation.translate(getWidth(),0);
         
         outputWindow.setLocation(outputWindowLocation);
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         instance = new GUI();
         instance.setVisible(true);
     }
     
 
     @Override
     public void setVisible(boolean shouldBeVisible) {
         super.setVisible(shouldBeVisible);
         outputWindow.setVisible(shouldBeVisible);
     }
 
 
     private class GUIWindowListener extends WindowAdapter {
         @Override
         public void windowClosing(WindowEvent event) {
             System.exit(0);
         }
     }
 
     private class StartSimulationActionListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             // TODO-SOMEDAY: do this in a worker thread, not on the Event Thread!!
             SimulatorEngine simulator = new SimulatorEngine();
             configureSimulator(simulator);
 
             simulator.run(null);
 
             cummulativeFlowDiagram.setTitle(scenarioName.getText());
             setTitle(scenarioName.getText());
             outputWindow.setTitle("Results: " + scenarioName.getText());
             pushSimulationResultsIntoCFD(simulator.results(), cfdData);
             outputSimulationResultsAsCSV(simulator);
 
             statusLabel.setText("Done");
         }
 
         private void configureSimulator(SimulatorEngine simulator) {
             simulator.addStories(Integer.parseInt(storiesInBacklog.getText()));
             simulator.setNumberOfIterationsToRun(Integer.parseInt(iterationsToRun.getText()));
 
             Object[][] tableData = getTableData(table);
 
             for (int rowIdx = 0; rowIdx < tableData.length; rowIdx++) {
                 Object[] cellsInRow = tableData[rowIdx];
 
                 Integer iteration = getIntegerFromCell(cellsInRow[0]);
                 Integer batchSize = getIntegerFromCell(cellsInRow[1]);
                 Integer baCapacity = getIntegerFromCell(cellsInRow[2]);
                 Integer devCapacity = getIntegerFromCell(cellsInRow[3]);
                 Integer webDevCapacity = getIntegerFromCell(cellsInRow[4]);
                 Integer qaCapacity = getIntegerFromCell(cellsInRow[5]);
 
                 simulator.addParameter(startingAt(iteration).setBatchSize(batchSize));
                 simulator.addParameter(startingAt(iteration).forStep(named("BA").setCapacity(baCapacity)));
                 simulator.addParameter(startingAt(iteration).forStep(named("Dev").setCapacity(devCapacity)));
                 simulator.addParameter(startingAt(iteration).forStep(named("WebDev").setCapacity(webDevCapacity)));
                 simulator.addParameter(startingAt(iteration).forStep(named("QA").setCapacity(qaCapacity)));
             }
         }
 
         private void outputSimulationResultsAsCSV(SimulatorEngine simulator) {
             StringBuffer output = new StringBuffer();
             for (IterationResult iterationResult : simulator.results()) {
                 output.append(iterationResult.toCSVString());
                 output.append("\n");
             }
             outputTextArea.setText(output.toString());
         }
 
         private Integer getIntegerFromCell(Object cell) {
             Integer value;
             if (cell instanceof Integer) {
                 value = (Integer) cell;
             } else if (cell instanceof String) {
                 if (StringUtils.isBlank((String)cell)) {
                     value = null;
                 } else {
                     value = Integer.parseInt((String) cell);
                 }
             } else {
                 throw new RuntimeException("Cell in iteration parameter table must be either Integer or String."
                         + String.format("Type is %s; expected", cell.getClass().getName()));
             }
             return value;
         }
     }
 
     private static Object[][] getTableData(JTable table) {
         TableModel dtm = table.getModel();
         int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
         Object[][] tableData = new Object[nRow][nCol];
         for (int i = 0; i < nRow; i++)
             for (int j = 0; j < nCol; j++)
                 tableData[i][j] = dtm.getValueAt(i, j);
         return tableData;
     }
     
     
     private void pushSimulationResultsIntoCFD(List<IterationResult> results, DefaultCategoryDataset dataset) {
         dataset.clear();
 
         dataset.addValue(0, "Done", "0");
         dataset.addValue(0, "QA", "0");
         dataset.addValue(0, "WebDev", "0");
         dataset.addValue(0, "Dev", "0");
         dataset.addValue(0, "BA", "0");
         
         for (IterationResult iterationResult : results) {
             dataset.addValue(iterationResult.getTotalCompleted(), "Done", ""+iterationResult.getIterationNumber());
             dataset.addValue(iterationResult.getQueued("QA"), "QA", ""+iterationResult.getIterationNumber());
             dataset.addValue(iterationResult.getQueued("WebDev"), "WebDev", ""+iterationResult.getIterationNumber());
             dataset.addValue(iterationResult.getQueued("Dev"), "Dev", ""+iterationResult.getIterationNumber());
             dataset.addValue(iterationResult.getQueued("BA"), "BA", ""+iterationResult.getIterationNumber());
         }
     }
 
     public JFrame outputWindow() {
         return outputWindow;
     }
 
 }
