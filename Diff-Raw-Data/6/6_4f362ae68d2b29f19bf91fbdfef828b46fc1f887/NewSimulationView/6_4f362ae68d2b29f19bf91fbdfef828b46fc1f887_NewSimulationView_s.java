 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.util.Log;
 
 import java.awt.Dimension;
 import javax.swing.JPanel;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JLabel;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JToggleButton;
 import javax.swing.JCheckBox;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.AbstractListModel;
 import javax.swing.JRadioButton;
 import javax.swing.ListSelectionModel;
 
 /**
  * Window for starting a new simulation.
  * 
  * @author Erik
  *
  */
 public class NewSimulationView {
 
 	private JFrame frmSimulatedEcosystem;
 	private JTextField textfield_Iterationdelay;
 	private JTextField tvPredPopSize;
 	private JTextField tvPreyPopSize;
 	private JTextField tvGrassPopSize;
 	private EcoWorld model;
 	private JList predList  = new JList();;
 	private JList preyList  = new JList();;
 	private JList grassList = new JList();
 	private JCheckBox chckbxRecordSimulation;
 	private JTextField tvNumMinutes;
 	private JList listSimulationDim;
 
 	/**
 	 * Create the application.
 	 * @param model 
 	 */
 	public NewSimulationView(EcoWorld model) {
 		this.model = model;
 		initialize();
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		frmSimulatedEcosystem.setLocation(dim.width/2-frmSimulatedEcosystem.getSize().width/2, dim.height/2-frmSimulatedEcosystem.getSize().height/2);
 		frmSimulatedEcosystem.setVisible(true);
 	}
 	
 	private void startSimulation() {
 		model.setNumIterations(Integer.MAX_VALUE);
 		int tickDelay = Integer.parseInt(textfield_Iterationdelay.getText());
 		if(tickDelay < 1) {
 			model.setRunWithoutTimer(true);			
 		} else {
 			model.setRunWithoutTimer(false);	
 			model.adjustTickRate(tickDelay);			
 		}
 		model.setRecordSimulation(chckbxRecordSimulation.isSelected());
 		model.createInitialPopulations((String) predList.getSelectedValue(),Integer.parseInt(tvPredPopSize.getText()),(String)  preyList.getSelectedValue(),Integer.parseInt(tvPreyPopSize.getText()),(String)  grassList.getSelectedValue(),Integer.parseInt(tvGrassPopSize.getText()));
 		try {
 			model.start();
 		} catch (IllegalStateException e) {
 			Log.v(e.toString());
 		}
 		int minutes = Integer.parseInt(tvNumMinutes.getText());
 		int mSeconds = minutes * 60 * 1000;
 		if(tickDelay > 0) {
 			int numIterations = mSeconds / tickDelay;
 			Log.v("NUMITERATIONS: " + numIterations);
 			model.setNumIterations(numIterations);
 		}
		model.setSimulationDimension((String) listSimulationDim.getSelectedValue());
 	}
 	
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmSimulatedEcosystem = new JFrame();
 		frmSimulatedEcosystem.setResizable(false);
 		frmSimulatedEcosystem.setAlwaysOnTop(true);
 		frmSimulatedEcosystem.setTitle("Start new Simulation");
 		frmSimulatedEcosystem.setBounds(100, 100, 621, 544);
 		frmSimulatedEcosystem.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		JButton btnRunSim = new JButton("Start new");
 		btnRunSim.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				startSimulation();
 				frmSimulatedEcosystem.dispose();
 			}
 		});
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				frmSimulatedEcosystem.dispose();
 			}
 		});
 		
 		JLabel lblPrededators = new JLabel("Prededators");
 		lblPrededators.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		
 		final JSlider slider_delaylength = new JSlider();
 		slider_delaylength.setValue(16);
 		slider_delaylength.setSnapToTicks(true);
 		slider_delaylength.setPaintTicks(true);
 		slider_delaylength.setPaintLabels(true);
 		
 		textfield_Iterationdelay = new JTextField();
 		textfield_Iterationdelay.setText("16");
 		textfield_Iterationdelay.setColumns(10);
 		
 		final JLabel lblIterationDelay = new JLabel("Iteration delay");
 		slider_delaylength.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				textfield_Iterationdelay.setText(slider_delaylength.getValue() + "");
 			}
 		});
 		predList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 		predList.setValueIsAdjusting(true);
 		predList.setSelectedIndices(new int[] {3});
 		predList.setModel(new AbstractListModel() {
 			String[] values = EcoWorld.PRED_VALUES;
 			public int getSize() {
 				return values.length;
 			}
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		predList.setSelectedIndex(0);
 		preyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 		preyList.setValueIsAdjusting(true);
 		preyList.setSelectedIndices(new int[] {3});
 		preyList.setModel(new AbstractListModel() {
 			String[] values = EcoWorld.PREY_VALUES;
 			public int getSize() {
 				return values.length;
 			}
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		preyList.setSelectedIndex(0);
 		
 		JLabel lblPreys = new JLabel("Preys");
 		lblPreys.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		grassList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 		grassList.setValueIsAdjusting(true);
 		grassList.setSelectedIndices(new int[] {3});
 		grassList.setModel(new AbstractListModel() {
 			public int getSize() {
 				return EcoWorld.GRASS_VALUES.length;
 			}
 			public Object getElementAt(int index) {
 				return EcoWorld.GRASS_VALUES[index];
 			}
 		});
 		grassList.setSelectedIndex(0);
 		
 		JLabel lblVegatablePopulation = new JLabel("Vegatable population");
 		lblVegatablePopulation.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		
 		tvPredPopSize = new JTextField();
 		tvPredPopSize.setText("10");
 		tvPredPopSize.setColumns(10);
 		
 		tvPreyPopSize = new JTextField();
 		tvPreyPopSize.setText("100");
 		tvPreyPopSize.setColumns(10);
 		
 		tvGrassPopSize = new JTextField();
 		tvGrassPopSize.setText("600");
 		tvGrassPopSize.setColumns(10);
 		
 		JLabel lblInitialSize = new JLabel("Initial size");
 		
 		JLabel label = new JLabel("Initial size");
 		
 		JLabel label_1 = new JLabel("Initial size");
 		
 		JRadioButton rdbtnNewRadioButton = new JRadioButton("Use OpenGLSimulationView");
 		rdbtnNewRadioButton.setSelected(true);
 		
 		chckbxRecordSimulation = new JCheckBox("Record simulation");
 		
 		final JSlider sliderPreySize = new JSlider();
 		sliderPreySize.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				tvPreyPopSize.setText(sliderPreySize.getValue() + "");
 			}
 		});
 		sliderPreySize.setMaximum(400);
 		sliderPreySize.setValue(100);
 		
 		final JSlider sliderGrassSize = new JSlider();
 		sliderGrassSize.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				tvGrassPopSize.setText(sliderGrassSize.getValue() + "");
 			}
 		});
 		sliderGrassSize.setMaximum(1200);
 		sliderGrassSize.setValue(600);
 		
 		final JSlider sliderPredSize = new JSlider();
 		sliderPredSize.setValue(10);
 		sliderPredSize.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				tvPredPopSize.setText(sliderPredSize.getValue() + "");
 			}
 		});
 		
 
 		
 		JLabel lblSimulationDuration = new JLabel("Simulation duration (minutes)");
 		
 		tvNumMinutes = new JTextField();
 		tvNumMinutes.setText("10");
 		tvNumMinutes.setColumns(10);
 		final JSlider sliderNumMinutes = new JSlider();
 		sliderNumMinutes.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				tvNumMinutes.setText(sliderNumMinutes.getValue() + "");
 			}
 		});
 		sliderNumMinutes.setMaximum(20);
 		sliderNumMinutes.setValue(10);
 		sliderNumMinutes.setSnapToTicks(true);
 		sliderNumMinutes.setPaintTicks(true);
 		sliderNumMinutes.setPaintLabels(true);
 		
 		listSimulationDim = new JList();
 		listSimulationDim.setValueIsAdjusting(true);
 		listSimulationDim.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		listSimulationDim.setSelectedIndices(new int[] {3});
 		listSimulationDim.setModel(new AbstractListModel() {
 			public int getSize() {
 				return EcoWorld.DIM_VALUES.length;
 			}
 			public Object getElementAt(int index) {
 				return EcoWorld.DIM_VALUES[index];
 			}
 		});
		listSimulationDim.setSelectedIndex(0);
 		
 		JLabel lblSimulationDimension = new JLabel("Simulation dimension");
 		lblSimulationDimension.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		GroupLayout groupLayout = new GroupLayout(frmSimulatedEcosystem.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblPrededators, GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblPreys, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
 								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 									.addGroup(groupLayout.createSequentialGroup()
 										.addComponent(grassList, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
 										.addGap(18)
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 											.addComponent(sliderGrassSize, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 											.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 												.addGroup(groupLayout.createSequentialGroup()
 													.addPreferredGap(ComponentPlacement.RELATED)
 													.addComponent(label_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 												.addComponent(tvGrassPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
 									.addComponent(lblVegatablePopulation, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
 									.addGroup(groupLayout.createSequentialGroup()
 										.addComponent(predList, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 											.addComponent(lblInitialSize, GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
 											.addGroup(groupLayout.createSequentialGroup()
 												.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 													.addComponent(sliderPredSize, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
 													.addComponent(tvPredPopSize, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
 												.addPreferredGap(ComponentPlacement.RELATED))))
 									.addGroup(groupLayout.createSequentialGroup()
 										.addComponent(preyList, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 											.addComponent(label, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 											.addComponent(tvPreyPopSize)
 											.addComponent(sliderPreySize, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))))
 							.addGap(18)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(listSimulationDim, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)
 									.addContainerGap())
 								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 									.addGroup(groupLayout.createSequentialGroup()
 										.addComponent(lblSimulationDimension, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 										.addContainerGap())
 									.addGroup(groupLayout.createSequentialGroup()
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 											.addGroup(groupLayout.createSequentialGroup()
 												.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
 												.addPreferredGap(ComponentPlacement.RELATED)
 												.addComponent(btnRunSim, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
 											.addGroup(groupLayout.createSequentialGroup()
 												.addComponent(sliderNumMinutes, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 												.addPreferredGap(ComponentPlacement.UNRELATED)
 												.addComponent(tvNumMinutes, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 											.addComponent(lblIterationDelay)
 											.addGroup(groupLayout.createSequentialGroup()
 												.addComponent(slider_delaylength, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 												.addPreferredGap(ComponentPlacement.UNRELATED)
 												.addComponent(textfield_Iterationdelay, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 											.addComponent(lblSimulationDuration, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE))
 										.addGap(5))
 									.addGroup(groupLayout.createSequentialGroup()
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 											.addComponent(rdbtnNewRadioButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 											.addComponent(chckbxRecordSimulation))
 										.addContainerGap()))))))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(lblPrededators)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(predList, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(lblInitialSize)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(tvPredPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(sliderPredSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 							.addGap(37)
 							.addComponent(lblPreys, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 									.addComponent(preyList, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
 									.addComponent(label))
 								.addGroup(groupLayout.createSequentialGroup()
 									.addGap(20)
 									.addComponent(tvPreyPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(sliderPreySize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 							.addGap(29)
 							.addComponent(lblVegatablePopulation, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addGap(20)
 									.addComponent(tvGrassPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.UNRELATED)
 									.addComponent(sliderGrassSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 									.addComponent(grassList, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
 									.addComponent(label_1))))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(lblIterationDelay)
 							.addGap(6)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(slider_delaylength, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(textfield_Iterationdelay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(lblSimulationDuration)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(sliderNumMinutes, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
 									.addGap(18)
 									.addComponent(rdbtnNewRadioButton)
 									.addComponent(chckbxRecordSimulation))
 								.addComponent(tvNumMinutes, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addGap(25)
 							.addComponent(lblSimulationDimension, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(listSimulationDim, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)))
 					.addGap(11)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
 						.addComponent(btnRunSim, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap())
 		);
 		frmSimulatedEcosystem.getContentPane().setLayout(groupLayout);
 	}
 }
