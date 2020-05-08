 package evaluation.simulator.gui.layout;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileFilter;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 
 import org.jfree.chart.ChartPanel;
 
 import evaluation.simulator.core.binding.gMixBinding;
 import evaluation.simulator.gui.results.LineJFreeChartCreator;
 import evaluation.simulator.gui.results.ResultPanelFactory;
 import evaluation.simulator.gui.service.ConfigParser;
 
 @SuppressWarnings("serial")
 public class SimulationTab extends JPanel implements ActionListener {
 
 	private final JButton addExperiment = new JButton(">>");
 	JList<File> availableExperiments;
 	final DefaultListModel<File> availableExperimentsModel;
 	private final JButton deleteExperiment = new JButton("<<");
 	private final JPanel leftNorth;
 	JScrollPane leftScrollPane;
 	private final JPanel north;
 
 	private int resultCounter;
 	JScrollPane rightScrollPane;
 	JList<File> runExperiments;
 	final DefaultListModel<File> runExperimentsModel;
 	private final JTabbedPane south;
 	private final JButton startButton = new JButton("Start Simulation");
 	private final JButton stopButton = new JButton("Stop Simulation");
 	
 	gMixBinding callSimulation;
 
 	public SimulationTab() {
 
 		this.availableExperimentsModel = new DefaultListModel<File>();
 		this.runExperimentsModel = new DefaultListModel<File>();
 
 		this.resultCounter = 1;
 
 		JSplitPane verticalSplitPlane = new JSplitPane();
 		verticalSplitPlane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		verticalSplitPlane.setOneTouchExpandable(true);
 		verticalSplitPlane.setDividerLocation(150);
 
 		this.setLayout(new BorderLayout());
 		this.add(verticalSplitPlane, BorderLayout.CENTER);
 
 		GridLayout simulationTabLayoutNorth = new GridLayout(1, 2);
 		this.leftNorth = new JPanel();
 		this.north = new JPanel();
 		this.north.setLayout(simulationTabLayoutNorth);
 		this.south = new JTabbedPane();
 
 		GridLayout gridLayout = new GridLayout(3, 3);
 		this.leftNorth.setLayout(gridLayout);
 
 		JLabel experimentsToSelectLabel = new JLabel(
 				"<html><b>Select</b> experiments:</html>");
 		JLabel selectedExperimentsLabel = new JLabel("Experiments to perform:");
 		this.leftNorth.add(experimentsToSelectLabel);
 		this.leftNorth.add(new JLabel(""));
 		this.leftNorth.add(selectedExperimentsLabel);
 
 		this.availableExperiments = new JList<File>(
 				this.availableExperimentsModel);
 		this.leftScrollPane = new JScrollPane(this.availableExperiments);
 		this.runExperiments = new JList<File>(this.runExperimentsModel);
 		this.rightScrollPane = new JScrollPane(this.runExperiments);
 		this.leftNorth.add(this.leftScrollPane);
 
 		JPanel experimentSelectionPane = new JPanel();
 		GridLayout buttonLayout = new GridLayout(2, 1);
 		experimentSelectionPane.setLayout(buttonLayout);
 		experimentSelectionPane.add(this.addExperiment);
 		experimentSelectionPane.add(this.deleteExperiment);
 		this.leftNorth.add(experimentSelectionPane);
 
 		this.leftNorth.add(this.rightScrollPane);
 
 		this.leftNorth.add(new JLabel(""));
 		this.leftNorth.add(this.startButton);
 		
 		this.leftNorth.add(this.stopButton);
 
 		this.north.add(this.leftNorth);
 		this.north.add(new JLabel(""));
 
 		this.startButton.addActionListener(this);
 		this.stopButton.addActionListener(this);
 		
 		this.south.addTab("Results_" + this.resultCounter, new ChartPanel(
 				LineJFreeChartCreator.createAChart()));
 		this.resultCounter++;
 
 		this.south.addTab("Results_" + this.resultCounter, new ChartPanel(
 				LineJFreeChartCreator.createAChart()));
 		this.resultCounter++;
 
 		verticalSplitPlane.setTopComponent(this.north);
 		verticalSplitPlane.setBottomComponent(this.south);
 
 		this.addExperiment.addActionListener(this);
 		this.deleteExperiment.addActionListener(this);
 
 		this.update();
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 
 		if (event.getSource() == this.startButton) {
 			ConfigParser configParser = new ConfigParser();
 
 			String[][] params = new String[this.runExperiments.getModel().getSize()][1];
 
 			for (int i = 0; i < this.runExperiments.getModel().getSize(); i++) {
 				params[i][0] = configParser.cleanupConfigurationForSimulator(runExperiments.getModel().getElementAt(i));
 				
 				callSimulation = new gMixBinding(params[i]);
 				callSimulation.start();
 				
 				// TODO: sync with main thread (pass Statistics)
				
				this.south.addTab("Results_" + this.resultCounter, ResultPanelFactory.getResultPanel());

				this.resultCounter++;
 			}
 			
			
 		}
 		
 		if (event.getSource() == this.stopButton) {
 			// callSimulation.interrupt();
 		}
 
 		if (event.getSource() == this.addExperiment) {
 			try {
 				int index = this.availableExperiments.getSelectedIndex();
 				if (index != -1) {
 					this.runExperimentsModel
 							.addElement(this.availableExperiments
 									.getSelectedValue());
 					this.availableExperimentsModel.remove(index);
 					this.updateUI();
 				}
 			} catch (Exception e) {
 
 			}
 		}
 
 		if (event.getSource() == this.deleteExperiment) {
 			try {
 				int index = this.runExperiments.getSelectedIndex();
 				if (index != -1) {
 					this.availableExperimentsModel
 							.addElement(this.runExperiments.getSelectedValue());
 					this.runExperimentsModel.remove(index);
 					this.updateUI();
 				}
 			} catch (Exception e) {
 
 			}
 		}
 	}
 
 	void update() {
 
 		// Read names of existing experiment configurations
 		final File folder = new File("etc/experiments/");
 		final File[] listOfFiles = folder.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(File f) {
 				return f.getName().toLowerCase().endsWith(".cfg");
 			}
 		});
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (File f : listOfFiles) {
 					boolean insertFlag = true;
 					for (int i = 0; i < SimulationTab.this.availableExperiments
 							.getModel().getSize(); i++) {
 						if (SimulationTab.this.availableExperiments.getModel()
 								.getElementAt(i).equals(f)) {
 							insertFlag = false;
 							break;
 						}
 					}
 
 					for (int i = 0; i < SimulationTab.this.runExperiments
 							.getModel().getSize(); i++) {
 						if (SimulationTab.this.runExperiments.getModel()
 								.getElementAt(i).equals(f)) {
 							insertFlag = false;
 							break;
 						}
 					}
 
 					if (insertFlag) {
 						SimulationTab.this.availableExperimentsModel
 								.addElement(f);
 					}
 				}
 			}
 		});
 	}
 }
