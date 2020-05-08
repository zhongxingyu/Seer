 package de.atlassoft.ui;
 
 
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 
 import de.atlassoft.application.ApplicationService;
 import de.atlassoft.model.Node;
 import de.atlassoft.model.ScheduleScheme;
 import de.atlassoft.model.SimulationStatistic;
 import de.atlassoft.model.TrainType;
 import de.atlassoft.util.I18NService;
 import de.atlassoft.util.I18NSingleton;
 import de.atlassoft.util.ImageHelper;
 
 /**
  * This class is for the SimulationStatistic Dialog where
  * you can see the Statistic after a Simulation.
  * 
  * @author Tobias Ilg
  */
 public class SimulationStatisticDialog {
 	private Shell shell;
 	private ApplicationService applicationService;
 	private SimulationStatistic simulationStatistic;
 	private TabItem statisticsItem;
 	private TabFolder tabFolder;
 	private Composite mainStatisticComposite;
 	
 	/**
 	 * Public constructor for the SimulationStatistic Dialog. Creates a new
 	 * SimulationStatistic shell and open it.
 	 * 
 	 * @param applicationService
 	 * 			to access application layer
 	 */
 	
 	public SimulationStatisticDialog (ApplicationService applicationService, SimulationStatistic simulationStatistic) {
 		this.applicationService = applicationService;
 		this.simulationStatistic = simulationStatistic;
 		I18NService I18N = I18NSingleton.getInstance();
 		
 		Display display = Display.getCurrent();
 		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		shell.setImage(ImageHelper.getImage("trainIcon"));
 		shell.setText(I18N.getMessage("SimulationStatisticDialog.Title"));
 		shell.setSize(880, 600);
 		shell.setLayout(new GridLayout(1, true));
 		
 		tabFolder = new TabFolder(shell, SWT.NONE);
 		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		tabFolder.setLayout(new RowLayout());
 		
 		//Statistics item
 		statisticsItem = new TabItem(tabFolder, SWT.NONE);
 		statisticsItem.setText("Statistik");
 		initUI();
 		statisticsItem.setControl(mainStatisticComposite);
 		
 		//Heatmap item
 		TabItem heatMapItem = new TabItem(tabFolder, SWT.NONE);
 		heatMapItem.setText("Heatmap");
 		
 		HeatMapComposite heatMapComposite = new HeatMapComposite(tabFolder, simulationStatistic);
 		
 		heatMapItem.setControl(heatMapComposite.getComposite());
 		MainWindow.center(shell);
 		shell.layout();
 		shell.open();
 	}
 	
 	/**
 	 * A method which creates the content, 
 	 * label, text, buttons and tool tips of
 	 * the shell of the SimulationStatistic Dialog.
 	 * 
 	 */
 	private void initUI() {
 		final I18NService I18N = I18NSingleton.getInstance();
 		mainStatisticComposite = new Composite (tabFolder, SWT.NULL);
 		mainStatisticComposite.setLayout (new GridLayout(1, true));
 		double allSec;
 		int min;
 		int sec;
 
 		//Title of Statistic
 		Label title = new Label(mainStatisticComposite, SWT.NONE);
 		title.setText(I18N.getMessage("SimulationStatisticDialog.Title2") + " '"
 						+ applicationService.getModel().getActiveRailwaySys().getID() +"'");
 		title.setLayoutData(new GridData(SWT.CENTER, SWT.NULL, true, false));
 		title.setFont(new Font(Display.getCurrent(), new FontData("Helvetica", 13, SWT.BOLD)));
 		
 		//Empty Row
 		new Label(mainStatisticComposite, SWT.NONE);
 		
 		//Overall Composite
 		final Composite overallStatisticComposite = new Composite (mainStatisticComposite, SWT.NULL);
 		overallStatisticComposite.setLayout (new GridLayout(2, true));
 		overallStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		// Top of the Composite
 		final Composite firstStatisticComposite = new Composite (overallStatisticComposite, SWT.BORDER);
 		firstStatisticComposite.setLayout (new GridLayout(3, false));
 		firstStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		//Title "Total"
 		Label title1 = new Label(firstStatisticComposite, SWT.NULL);
 		title1.setText(I18N.getMessage("SimulationStatisticDialog.littleTitle1"));
 		title1.setFont(new Font(Display.getCurrent(), new FontData("Helvetica", 10, SWT.BOLD)));
 		new Label(firstStatisticComposite, SWT.NULL);
 		new Label(firstStatisticComposite, SWT.NULL);
 		
 		// Eleventh row with "NumberOfRides"
 		new Label(firstStatisticComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.NumberOfRides") + "\t");
 		new Label(firstStatisticComposite, SWT.NONE);
 		Label meanDelayRides = new Label(firstStatisticComposite, SWT.NONE);
 		meanDelayRides.setText(String.valueOf(simulationStatistic.getNumberOfRides()
 								+ " " + I18N.getMessage("SimulationStatisticDialog.Rides")));
 		
 		//Twelfth row with "NomberOfNodes"
 		new Label(firstStatisticComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.NumberOfNodes") + "\t");
 		new Label(firstStatisticComposite, SWT.NONE);
 		Label meanDelayNodes = new Label(firstStatisticComposite, SWT.NONE);
 		meanDelayNodes.setText(String.valueOf(simulationStatistic.getInvolvedNodes().size()
 								+ " " + I18N.getMessage("SimulationStatisticDialog.Nodes")));
 		
 		// First row with three labels for "TotalDelay"
 		new Label(firstStatisticComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.TotalDelay") + "\t");
 		new Label(firstStatisticComposite, SWT.NONE);
 		Label totalDelay = new Label(firstStatisticComposite, SWT.NONE);
 		allSec = simulationStatistic.getTotalDelay();
 		min = (int) allSec / 60;
 		sec = (int) allSec % 60;
 		totalDelay.setText(min + " min " + sec + " s");
 		
 		// Second row with three labels for "MeanDelay"
 		new Label(firstStatisticComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.MeanDelay") + "\t");
 		new Label(firstStatisticComposite, SWT.NONE);
 		Label meanDelay = new Label(firstStatisticComposite, SWT.NONE);
 		allSec = simulationStatistic.getMeanDelay();
 		min = (int) allSec / 60;
 		sec = (int) allSec % 60;
 		meanDelay.setText(min + " min " + sec + " s");
 		
 		// Top of the Composite "TrainType"
 		final Composite TrainTypeStatisticComposite = new Composite (overallStatisticComposite, SWT.BORDER);
 		TrainTypeStatisticComposite.setLayout (new GridLayout(1, false));
 		TrainTypeStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		//Title "TrainType"
 		Label title2 = new Label(TrainTypeStatisticComposite, SWT.NULL);
 		title2.setText(I18N.getMessage("SimulationStatisticDialog.MeanDelayTrainType"));
 		title2.setFont(new Font(Display.getCurrent(), new FontData("Helvetica", 10, SWT.BOLD)));
 		
 		//Info of the Composite "TrainType"
 		final Composite secondStatisticComposite = new Composite (TrainTypeStatisticComposite, SWT.NULL);
 		secondStatisticComposite.setLayout (new GridLayout(3, false));
 		secondStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		// Third row with three labels for "MeanDelay (TrainType)"
 		new Label(secondStatisticComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.littleTitle2") + "\t");
 		new Label(secondStatisticComposite, SWT.NONE).setText("\t\t");
 		final List<TrainType> trainTypes = simulationStatistic.getInvolvedTrainTypes();
 	 	final String[] trainTypeSelection = new String [trainTypes.size()];
 		int index = 0;
 		for (TrainType type : trainTypes) {
 			trainTypeSelection[index] = type.getName();
 			index++;
 		}
 		final Combo comboTrainType = new Combo(secondStatisticComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 	    comboTrainType.setItems(trainTypeSelection);
 	    comboTrainType.select(0);
 		
 		// Fourth row with the result of "MeanDelay (TrainType)"
 		new Label(secondStatisticComposite, SWT.NONE);
 		TrainType selectedTT = null;
 		for (TrainType type : trainTypes) {
 			if (type.getName().equals(comboTrainType.getText())) {
 				selectedTT = type;
 			}
 		}
 		new Label(secondStatisticComposite, SWT.NONE);
 		final Label meanDelayTrainType = new Label(secondStatisticComposite, SWT.NONE);
 		allSec = simulationStatistic.getMeanDelay(selectedTT);
 		min = (int) allSec / 60;
 		sec = (int) allSec % 60;
 		meanDelayTrainType.setText(min + " min " + sec + " s");
 		
 		//row with Number of TrainRides (TrainType)
 		new Label(secondStatisticComposite, SWT.NONE);
 		new Label(secondStatisticComposite, SWT.NONE);
 		final Label numberOfRidesTT =new Label(secondStatisticComposite, SWT.NONE);
 		numberOfRidesTT.setText(String.valueOf(simulationStatistic.getNumberOfRides(selectedTT))
 								+ " " + I18N.getMessage("SimulationStatisticDialog.Rides"));
 		
 		//row with Number of Stations (TrainType)
 		new Label(secondStatisticComposite, SWT.NONE);
 		new Label(secondStatisticComposite, SWT.NONE);
 		final Label numberOfNodesTT =new Label(secondStatisticComposite, SWT.NONE);
 		numberOfNodesTT.setText(String.valueOf(simulationStatistic.getInvolvedNodes(selectedTT).size())
 								+ " " + I18N.getMessage("SimulationStatisticDialog.Nodes"));
 		
 		// Listener for the combo TrainType
 		comboTrainType.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				double allSec;
 				int min;
 				int sec;
 				TrainType selectedTrainType = null;
 				for (TrainType type : trainTypes) {
 					if (type.getName().equals(comboTrainType.getText())) {
 						selectedTrainType = type;
 					}
 				}
 				allSec = simulationStatistic.getMeanDelay(selectedTrainType);
 				min = (int) allSec / 60;
 				sec = (int) allSec % 60;
				meanDelayTrainType.setText(min + " min " + sec);
				numberOfRidesTT.setText(String.valueOf(simulationStatistic.getNumberOfRides(selectedTrainType)));
				numberOfNodesTT.setText(String.valueOf(simulationStatistic.getInvolvedNodes(selectedTrainType).size()));
 				secondStatisticComposite.layout();
 			}
 		});
 		
 		// Top of the Composite "Node"
 		final Composite nodeStatisticComposite = new Composite (overallStatisticComposite, SWT.BORDER);
 		nodeStatisticComposite.setLayout (new GridLayout(1, false));
 		nodeStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		//Title "Node"
 		Label title3 = new Label(nodeStatisticComposite, SWT.NULL);
 		title3.setText(I18N.getMessage("SimulationStatisticDialog.MeanDelayNode"));
 		title3.setFont(new Font(Display.getCurrent(), new FontData("Helvetica", 10, SWT.BOLD)));
 		
 		// Info of the Composite "Node"
 		final Composite thirdStatisticComposite = new Composite (nodeStatisticComposite, SWT.NULL);
 		thirdStatisticComposite.setLayout (new GridLayout(3, false));
 		thirdStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		// Fifth row with "MeanDelay (Node)"
 		new Label(thirdStatisticComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.littleTitle3") + "\t");
 		new Label(thirdStatisticComposite, SWT.NONE).setText("\t\t");
 		final java.util.List<Node> nodes = simulationStatistic.getInvolvedNodes();
 		final String[] nodeSelection = new String [nodes.size()];
 		index = 0;
 		for (Node node : nodes) {
 			nodeSelection[index] = node.getName();
 			index++;
 		}
 		final Combo comboNode = new Combo(thirdStatisticComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 	    comboNode.setItems(nodeSelection);
 	    comboNode.select(0);
 		
 		// Sixth row with the result of "MeanDelay (Node)"
 		new Label(thirdStatisticComposite, SWT.NONE);
 		Node selectedN = null;
 		for (Node node : nodes) {
 			if (node.getName().equals(comboNode.getText())) {
 				selectedN = node;
 			}
 		}
 		new Label(thirdStatisticComposite, SWT.NONE);
 		final Label meanDelayNode = new Label(thirdStatisticComposite, SWT.NONE);
 		allSec = simulationStatistic.getMeanDelay(selectedN);
 		min = (int) allSec / 60;
 		sec = (int) allSec % 60;
 		meanDelayNode.setText(min + " min " + sec + " s");
 		
 		//row with Number of TrainRides (Node)
 		new Label(thirdStatisticComposite, SWT.NONE);
 		new Label(thirdStatisticComposite, SWT.NONE);
 		final Label numberOfRidesN =new Label(thirdStatisticComposite, SWT.NONE);
 		numberOfRidesN.setText(String.valueOf(simulationStatistic.getNumberOfRides(selectedN))
 								+ " " + I18N.getMessage("SimulationStatisticDialog.Rides"));
 		
 		// Listener for the combo Node
 		comboNode.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				double allSec;
 				int min;
 				int sec;
 				Node selectedNode = null;
 				for (Node node : nodes) {
 					if (node.getName().equals(comboNode.getText())) {
 						selectedNode = node;
 					}
 				}
 				allSec = simulationStatistic.getMeanDelay(selectedNode);
 				min = (int) allSec / 60;
 				sec = (int) allSec % 60;
				meanDelayNode.setText(min + " min " + sec);
				numberOfRidesN.setText(String.valueOf(simulationStatistic.getNumberOfRides(selectedNode)));
 				thirdStatisticComposite.layout();
 			}
 		});
 		
 		// Top of the Composite
 		final Composite scheduleSchemeStatisticComposite = new Composite (overallStatisticComposite, SWT.BORDER);
 		scheduleSchemeStatisticComposite.setLayout (new GridLayout(1, false));
 		scheduleSchemeStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		//Title "ScheduleScheme"
 		Label title4 = new Label(scheduleSchemeStatisticComposite, SWT.NULL);
 		title4.setText(I18N.getMessage("SimulationStatisticDialog.MeanDelayScheduleScheme"));
 		title4.setFont(new Font(Display.getCurrent(), new FontData("Helvetica", 10, SWT.BOLD)));
 		
 		// Top of the Composite
 		final Composite fourthStatisticComposite = new Composite (scheduleSchemeStatisticComposite, SWT.NULL);
 		fourthStatisticComposite.setLayout (new GridLayout(3, false));
 		fourthStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		// Seventh row with "MeanDelay (ScheduleScheme)"
 		new Label(fourthStatisticComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.littleTitle4") + "\t");
 		new Label(fourthStatisticComposite, SWT.NONE).setText("\t\t");
 		final java.util.List<ScheduleScheme> scheduleSchemes = simulationStatistic.getInvolvedScheduleSchemes();
 		final String[] scheduleSchemeSelection = new String [scheduleSchemes.size()];
 		index = 0;
 		for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 			scheduleSchemeSelection[index] = scheduleScheme.getID();
 			index++;
 		}
 		final Combo comboScheduleScheme = new Combo(fourthStatisticComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 		comboScheduleScheme.setItems(scheduleSchemeSelection);
 		comboScheduleScheme.select(0);
 		
 		// Eighth row with the result of "MeanDelay (ScheduleScheme)"
 		new Label(fourthStatisticComposite, SWT.NONE);
 		ScheduleScheme selectedSS = null;
 		for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 			if (scheduleScheme.getID().equals(comboScheduleScheme.getText())) {
 				selectedSS = scheduleScheme;
 			}
 		}
 		new Label(fourthStatisticComposite, SWT.NONE);
 		final Label meanDelayScheduleScheme = new Label(fourthStatisticComposite, SWT.NONE);
 		allSec = simulationStatistic.getMeanDelay(selectedSS);
 		min = (int) allSec / 60;
 		sec = (int) allSec % 60;
 		meanDelayScheduleScheme.setText(min + " min " + sec + " s");
 		
 		//row with Number of TrainRides (ScheduleScheme)
 		new Label(fourthStatisticComposite, SWT.NONE);
 		new Label(fourthStatisticComposite, SWT.NONE);
 		final Label numberOfRidesSS =new Label(fourthStatisticComposite, SWT.NONE);
 		numberOfRidesSS.setText(String.valueOf(simulationStatistic.getNumberOfRides(selectedSS))
 								+ " " + I18N.getMessage("SimulationStatisticDialog.Rides"));
 		
 		//row with Number of Stations (ScheduleScheme)
 		new Label(fourthStatisticComposite, SWT.NONE);
 		new Label(fourthStatisticComposite, SWT.NONE);
 		final Label numberOfNodesSS =new Label(fourthStatisticComposite, SWT.NONE);
 		numberOfNodesSS.setText(String.valueOf(simulationStatistic.getInvolvedNodes(selectedSS).size())
 								+ " " + I18N.getMessage("SimulationStatisticDialog.Nodes"));
 //		toTheEnd(numberOfNodesSS);
 		
 		// Listener for the combo ScheduleScheme
 		comboScheduleScheme.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				double allSec;
 				int min;
 				int sec;
 				ScheduleScheme selectedScheduleScheme = null;
 				for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 					if (scheduleScheme.getID().equals(comboScheduleScheme.getText())) {
 						selectedScheduleScheme = scheduleScheme;
 					}
 				}
 				allSec = simulationStatistic.getMeanDelay(selectedScheduleScheme);
 				min = (int) allSec / 60;
 				sec = (int) allSec % 60;
				meanDelayScheduleScheme.setText(min + " min " + sec);
				numberOfRidesSS.setText(String.valueOf(simulationStatistic.getNumberOfRides(selectedScheduleScheme)));
				numberOfNodesSS.setText(String.valueOf(simulationStatistic.getInvolvedNodes(selectedScheduleScheme).size()));
 				fourthStatisticComposite.layout();
 			}
 		});
 
 		//Design
 		final Composite scheduleSchemeAndNodeStatisticComposite = new Composite (mainStatisticComposite, SWT.BORDER);
 		scheduleSchemeAndNodeStatisticComposite.setLayout (new GridLayout(1, false));
 		scheduleSchemeAndNodeStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false));
 		
 		//Title "ScheduleScheme and Node"
 		Label title5 = new Label(scheduleSchemeAndNodeStatisticComposite, SWT.NULL);
 		title5.setText(I18N.getMessage("SimulationStatisticDialog.MeanDelayScheduleSchemeAndNode"));
 		title5.setFont(new Font(Display.getCurrent(), new FontData("Helvetica", 10, SWT.BOLD)));
 		
 		// Top of the Composite
 		final Composite fifthStatisticComposite = new Composite (scheduleSchemeAndNodeStatisticComposite, SWT.NULL);
 		fifthStatisticComposite.setLayout (new GridLayout(3, false));
 		fifthStatisticComposite.setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false));
 		
 		// Ninth row with "MeanDelay (ScheduleScheme, Node)"
 		new Label(fifthStatisticComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.littleTitle5"));
 		final Combo comboScheduleScheme2 = new Combo(fifthStatisticComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 		comboScheduleScheme2.setItems(scheduleSchemeSelection);
 		comboScheduleScheme2.select(0);
 		final Combo comboNode2 = new Combo(fifthStatisticComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 		ScheduleScheme selectedScheduleScheme2 = null;
 		for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 			if (scheduleScheme.getID().equals(comboScheduleScheme2.getText())) {
 				selectedScheduleScheme2 = scheduleScheme;
 			}
 		}
 		if (selectedScheduleScheme2 != null) {
 			java.util.List<Node> nodes2 = simulationStatistic.getInvolvedNodes(selectedScheduleScheme2);
 			String[] node2Selection = new String [nodes2.size()];
 			int index2 = 0;
 			for (Node node : nodes2) {
 				node2Selection[index2] = node.getName();
 				index2++;
 			}
 			comboNode2.setItems(node2Selection);
 			comboNode2.select(0);
 		}
 	    
 	    // Tenth row with the result of "MeanDelay (ScheduleScheme, Node)"
 		new Label(fifthStatisticComposite, SWT.NONE);
 		ScheduleScheme selectedSS2 = null;
 		for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 			if (scheduleScheme.getID().equals(comboScheduleScheme2.getText())) {
 				selectedSS2 = scheduleScheme;
 			}
 		}
 		Node selectedN2 = null;
 		for (Node node : nodes) {
 			if (node.getName().equals(comboNode2.getText())) {
 				selectedN2 = node;
 			}
 		}
 		final Label meanDelayScheduleSchemeAndNode = new Label(fifthStatisticComposite, SWT.NONE);
 		allSec = simulationStatistic.getMeanDelay(selectedSS2, selectedN2);
 		min = (int) allSec / 60;
 		sec = (int) allSec % 60;
		meanDelayScheduleSchemeAndNode.setText(min + " min " + sec);
		new Label(fifthStatisticComposite, SWT.NONE).setText("s");
 		
 		// Listener for the combo scheduleScheme and Node
 		comboScheduleScheme2.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				double allSec;
 				int min;
 				int sec;
 				ScheduleScheme selectedScheduleScheme2 = null;
 				for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 					if (scheduleScheme.getID().equals(comboScheduleScheme2.getText())) {
 						selectedScheduleScheme2 = scheduleScheme;
 					}
 				}
 				
 				java.util.List<Node> nodes2 = selectedScheduleScheme2.getStations();
 				String[] node2Selection = new String [nodes2.size()];
 				int index = 0;
 				for (Node node : nodes2) {
 					node2Selection[index] = node.getName();
 					index++;
 				}
 				comboNode2.setItems(node2Selection);
 				comboNode2.select(0);
 				
 				Node selectedNode2 = null;
 				for (Node node : nodes) {
 					if (node.getName().equals(comboNode2.getText())) {
 						selectedNode2 = node;
 					}
 				}
 				allSec = simulationStatistic.getMeanDelay(selectedScheduleScheme2, selectedNode2);
 				min = (int) allSec / 60;
 				sec = (int) allSec % 60;
				meanDelayScheduleSchemeAndNode.setText(min + " min " + sec);
 				fifthStatisticComposite.layout();
 			}
 		});
 		
 		comboNode2.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				double allSec;
 				int min;
 				int sec;
 				ScheduleScheme selectedScheduleScheme2 = null;
 				for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 					if (scheduleScheme.getID().equals(comboScheduleScheme2.getText())) {
 						selectedScheduleScheme2 = scheduleScheme;
 					}
 				}
 				Node selectedNode2 = null;
 				for (Node node : nodes) {
 					if (node.getName().equals(comboNode2.getText())) {
 						selectedNode2 = node;
 					}
 				}allSec = simulationStatistic.getMeanDelay(selectedScheduleScheme2, selectedNode2);
 				min = (int) allSec / 60;
 				sec = (int) allSec % 60;
				meanDelayScheduleSchemeAndNode.setText(min + " min " + sec);				
 				fifthStatisticComposite.layout();
 			}
 		});
 		
 		new Label(mainStatisticComposite, SWT.NULL);
 		
 		//Information
 		Label infoTitle = new Label(mainStatisticComposite, SWT.NULL);
 		infoTitle.setText(I18N.getMessage("SimulationStatisticDialog.InformationTitle"));
 		infoTitle.setFont(new Font(Display.getCurrent(), new FontData("Helvetica", 10, SWT.BOLD)));
 		
 		Label info = new Label(mainStatisticComposite, SWT.NULL);
 		info.setText(I18N.getMessage("SimulationStatisticDialog.Information"));
 		
 		// Thirteenth row with Button "Save as PDF"
 		Button save = new Button(shell, SWT.PUSH);
 		save.setImage(ImageHelper.getImage("pdfIcon"));
 		save.setText(I18N.getMessage("SimulationStatisticDialog.Save"));
 		GridData dataSave = new GridData(GridData.FILL);
 		dataSave.horizontalAlignment = GridData.CENTER;
 	    save.setLayoutData(dataSave);
 		
 		// Function of Save-Button
 		save.addSelectionListener(new SelectionAdapter() {
 	        public void widgetSelected(SelectionEvent e) {
 	        	applicationService.showStatisticDoc(simulationStatistic);
 	       		MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
 	       		messageBox.setText(I18N.getMessage("SimulationStatisticDialog.informationSaved"));
         	    messageBox.setMessage(I18N.getMessage("SimulationStatisticDialog.informationStatisticSaved"));
         	    shell.setVisible(false);
         	    messageBox.open();
 	            shell.close();
 	        }
 	    });
 	}
 }
