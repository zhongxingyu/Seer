 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractListModel;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListDataListener;
 
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.IModel;
 import chalmers.dax021308.ecosystem.model.environment.SimulationSettings;
 import chalmers.dax021308.ecosystem.model.environment.mapeditor.DefaultMaps;
 import chalmers.dax021308.ecosystem.model.environment.mapeditor.MapFileHandler;
 import chalmers.dax021308.ecosystem.model.environment.mapeditor.SimulationMap;
 import chalmers.dax021308.ecosystem.model.util.ButtonGroupWrapper;
 //import java.awt.Container;
 
 public class NEWSettingsMenuView extends JDialog {
 	
 	private static final long serialVersionUID = -7514048933302292458L;
 	static final int DEFAULT_ITERATION_DELAY = 16;
     static final int DEFAULT_PRED_POP_SIZE = 10;
     static final int DEFAULT_PREY_POP_SIZE = 100;
     static final int DEFAULT_VEG_POP_SIZE = 400;
     static final int DEFAULT_NO_OF_ITERATIONS = 10000;
     static final int DEFAULT_WIDTH = 750;
     static final int DEFAULT_HEIGHT = 750;
     
     private JPanel contentPane;
     private JPanel left;
     private JPanel right;
     private JPanel panelButton1;
     private JPanel panelButton2;
     private JPanel panelButtonStart;
     private JPanel panelButtonCancel;
     private JPanel panelPred;
     private JPanel panelPrey;
     private JPanel panelVeg;
     
     private JLabel labelPredators;
     private JLabel labelPreys;
     private JLabel labelVegetation;
     private JLabel labelIterationDelay;
     private JLabel labelIterations;
     private JLabel labelDimension;
     private JLabel labelThreads;
     private JLabel labelShape;
     private JLabel labelWidth;
     private JLabel labelHeight;
     private JLabel labelObstacle;
     private JPanel shapeRadioPanel;
     
     //public final JTextField textFieldIterationDelay;
     //public final JTextField textFieldPredPopSize;
     //public final JTextField textFieldPreyPopSize;
     //public final JTextField textFieldVegPopSize;
     //public final JTextField textFieldNoOfIterations;
     
     public final JTextField textFieldWidth;
     public final JTextField textFieldHeight;
     
     public final JCheckBox checkBoxRecordSim;
     public final JCheckBox checkBoxLimitIterations;
     public final JCheckBox checkBoxCustomSize;
     
     //de h�r kanske kan vara privata nu..?
     public final JRadioButton radioButton2Threads;
     public final JRadioButton radioButton4Threads;
     public final JRadioButton radioButton8Threads;
     public final JRadioButton radioButtonSquare;
     public final JRadioButton radioButtonCircle;
     public final JRadioButton radioButtonTriangle;
     public final ButtonGroupWrapper buttonGroupThread;
     public final ButtonGroupWrapper buttonGroupShape;
     
     public final JButton buttonStart; //kanske den h�r bara ska vara apply, och sen startar man fr�n mainwindow?
     private JButton buttonCancel;
     private JButton buttonAdvanced;
     
     //TODO: sliders till spinners?
     //public final JSlider sliderDelayLength;
     //public final JSlider sliderNoOfIterations;
     //public final JSlider sliderPreyPopSize;
     //public final JSlider sliderPredPopSize;
     //public final JSlider sliderVegPopSize;
     
     public final JSpinner spinnerDelayLength;
     public final JSpinner spinnerNoOfIterations;
     public final JSpinner spinnerPreyPopSize;
     public final JSpinner spinnerPredPopSize;
     public final JSpinner spinnerVegPopSize;
     
     public final JList listSimDimension;
     public final JList<SimulationMap> listMap;
     public final JList listPred;
     public final JList listPrey;
     public final JList listVegetation;
     
     private Font heading;
     
     private ActionListener listenerCancel;
     private ActionListener listenerAdvanced;
     private ChangeListener listenerCustomSize;
     private ChangeListener listenerLimitIterations;
 
     private AdvancedSettings advancedSettingsView;
     
     //Auto generated	
     /**
      * Launch the application.
      */
     public static void main(String[] args) {
         EventQueue.invokeLater(new Runnable() {
             EcoWorld model = new EcoWorld();
 
             public void run() {
                 try {
                     NEWSettingsMenuView frame = new NEWSettingsMenuView(model, null);
                     frame.setVisible(true);
                     frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     /**
      * Create the frame.
      */
     public NEWSettingsMenuView(IModel model, Frame superFrame) {
     	super(superFrame);
 
         //initializing the graphical objects - done here since most of them are final		
         contentPane = new JPanel();
         left = new JPanel();
         right = new JPanel();
         panelButton1 = new JPanel();
         panelButton2 = new JPanel();
         panelButtonStart = new JPanel();
         panelButtonCancel = new JPanel();
         panelPred = new JPanel();
         panelPrey = new JPanel();
         panelVeg = new JPanel();
 
         //setting colors to assist when doing the layout
         /*
         left.setBackground(Color.RED);
         center.setBackground(Color.BLUE);
         panelButton.setBackground(Color.CYAN);
         panelPred.setBackground(Color.GREEN);
         panelPrey.setBackground(Color.MAGENTA);
         panelVeg.setBackground(Color.ORANGE);
         */
 
         labelPredators = new JLabel();
         labelPreys = new JLabel();
         labelVegetation = new JLabel();
         labelIterationDelay = new JLabel();
         labelIterations = new JLabel();
         labelDimension = new JLabel();
         labelThreads = new JLabel();
         labelShape = new JLabel();
         labelWidth = new JLabel();
         labelHeight = new JLabel();
         labelObstacle = new JLabel();
 
         //textFieldIterationDelay = new JTextField();
         //textFieldPredPopSize = new JTextField();
         //textFieldPreyPopSize = new JTextField();
         //textFieldVegPopSize = new JTextField();
         //textFieldNoOfIterations = new JTextField();
         textFieldWidth = new JTextField();
         textFieldHeight = new JTextField();
 
         checkBoxRecordSim = new JCheckBox();
         checkBoxLimitIterations = new JCheckBox();
         checkBoxCustomSize = new JCheckBox();
 
         radioButton2Threads = new JRadioButton();
         radioButton4Threads = new JRadioButton();
         radioButton8Threads = new JRadioButton();
         radioButtonSquare = new JRadioButton();
         radioButtonCircle = new JRadioButton();
         radioButtonTriangle = new JRadioButton();
         shapeRadioPanel = new JPanel();
 
         buttonGroupThread = new ButtonGroupWrapper();
         buttonGroupShape = new ButtonGroupWrapper();
 
         buttonStart = new JButton();
         buttonCancel = new JButton();
         buttonAdvanced = new JButton();
         
         spinnerDelayLength = new JSpinner(new SpinnerNumberModel(DEFAULT_ITERATION_DELAY, 0, Integer.MAX_VALUE, 1));
         spinnerNoOfIterations = new JSpinner(new SpinnerNumberModel(DEFAULT_NO_OF_ITERATIONS, 0, Integer.MAX_VALUE, 100));
         spinnerPreyPopSize = new JSpinner(new SpinnerNumberModel(DEFAULT_PREY_POP_SIZE, 0, Integer.MAX_VALUE, 1));
         spinnerPredPopSize = new JSpinner(new SpinnerNumberModel(DEFAULT_PRED_POP_SIZE, 0, Integer.MAX_VALUE, 1));
         spinnerVegPopSize = new JSpinner(new SpinnerNumberModel(DEFAULT_VEG_POP_SIZE, 0, Integer.MAX_VALUE, 10));
 
         //sliderDelayLength = new JSlider();
         //sliderPreyPopSize = new JSlider();
         //sliderPredPopSize = new JSlider();
         //sliderVegPopSize = new JSlider();
         //sliderNoOfIterations = new JSlider();
 
         listSimDimension = new JList();
         listMap = new JList<SimulationMap>();
         listPred = new JList();
         listPrey = new JList();
         listVegetation = new JList();
 
         heading = new Font("Tahoma", Font.BOLD, 14);
 
         setMyLayout();
         createButtonGroups();
         setMyText();
         setMyFonts();
         //setMySliders();
         //setMySpinners();
        setMyListeners();
         setMyCheckBoxes();
         setMyLists();
         setMyButtons();
         setMyBorders();
         addToLeftPanel();
         addToRightPanel();
 
         setResizable(false);
         setTitle("Simulation Settings");
         setIconImage(new ImageIcon("res/Simulated ecosystem icon.png").getImage());
         setLocation(300, 150);
         pack();
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         setVisible(true);
     }
 
     private void setMyListeners() {
         listenerCancel = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 NEWSettingsMenuView.this.dispose();
             }
         };
 
         listenerAdvanced = new ActionListener() {
 
 			@Override
             public void actionPerformed(ActionEvent e) {
 				advancedSettingsView = new AdvancedSettings(); 
             }
         };
 
         listenerCustomSize = new ChangeListener() {
             public void stateChanged(ChangeEvent ce) {
                 if (checkBoxCustomSize.isSelected()) {
                     listSimDimension.setEnabled(false);
                     labelWidth.setEnabled(true);
                     labelHeight.setEnabled(true);
                     textFieldWidth.setEnabled(true);
                     textFieldHeight.setEnabled(true);
                 } else {
                     listSimDimension.setEnabled(true);
                     labelWidth.setEnabled(false);
                     labelHeight.setEnabled(false);
                     textFieldWidth.setEnabled(false);
                     textFieldHeight.setEnabled(false);
                 }
             }
         };
 
         listenerLimitIterations = new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent ce) {
                 if (checkBoxLimitIterations.isSelected()) {
                     //textFieldNoOfIterations.setEnabled(true);
                     labelIterations.setEnabled(true);
                     //sliderNoOfIterations.setEnabled(true);
                     spinnerNoOfIterations.setEnabled(true);
                 } else {
                     //textFieldNoOfIterations.setEnabled(false);
                     labelIterations.setEnabled(false);
                     //sliderNoOfIterations.setEnabled(false);
                     spinnerNoOfIterations.setEnabled(false);
                 }
                
             }
         };
     }
 
     private void setMyLayout() {
         setContentPane(contentPane);
         contentPane.setLayout(new GridBagLayout());
 
         left.setLayout(new GridBagLayout());
         right.setLayout(new GridBagLayout());
         panelButton1.setLayout(new GridBagLayout());
         panelButton2.setLayout(new GridBagLayout());
         panelButtonStart.setLayout(new GridBagLayout());
         panelButtonCancel.setLayout(new GridBagLayout());
         
         shapeRadioPanel.setLayout(new GridLayout(3,1));
 
         /*
          left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
          right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
          center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
          panelButton.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
          */
 
         GridBagConstraints c = new GridBagConstraints();
         c.anchor = GridBagConstraints.NORTH;
         c.gridx = c.gridy = 0;
         contentPane.add(left, c);
         c.gridx++;
         contentPane.add(right, c);
     }
 
     private void createButtonGroups() {
         buttonGroupThread.add(radioButton2Threads);
         buttonGroupThread.add(radioButton4Threads);
         buttonGroupThread.add(radioButton8Threads);
         buttonGroupShape.add(radioButtonSquare);
         buttonGroupShape.add(radioButtonCircle);
         buttonGroupShape.add(radioButtonTriangle);
         
         shapeRadioPanel.add(radioButtonSquare);
         shapeRadioPanel.add(radioButtonCircle);
         shapeRadioPanel.add(radioButtonTriangle);
     }
 
     private void setMyText() {
         labelPredators.setText("Predators");
         labelPreys.setText("Preys");
         labelVegetation.setText("Vegetation");
         labelIterationDelay.setText("Iteration Delay");
         labelIterations.setText("Number of Iterations");
         labelDimension.setText("Simulation Window Dimensions");
         labelThreads.setText("Number of Threads");
         labelShape.setText("Shape of Universe");
         labelWidth.setText("Width");
         labelWidth.setEnabled(false);
         labelHeight.setText("Height");
         labelHeight.setEnabled(false);
         labelObstacle.setText("Obstacles");
 
         radioButtonSquare.setText("Square");
         radioButtonCircle.setText("Circle");
         radioButtonTriangle.setText("Triangle");
         radioButton2Threads.setText("2");
         radioButton4Threads.setText("4");
         radioButton8Threads.setText("8");
 
         checkBoxRecordSim.setText("Record Simulation");
         checkBoxLimitIterations.setText("Limit Number of Iterations");
         checkBoxCustomSize.setText("Custom Size");
 
         buttonStart.setText("Start");
         buttonCancel.setText("Cancel");
         buttonAdvanced.setText("Advanced");
 
         //textFieldPredPopSize.setText("10");
         //textFieldPredPopSize.setColumns(10);
         //textFieldPreyPopSize.setText("100");
         //textFieldPreyPopSize.setColumns(10);
         //textFieldVegPopSize.setText("400");
         //textFieldVegPopSize.setColumns(10);
         textFieldWidth.setEnabled(false);
         textFieldWidth.setText("" + DEFAULT_WIDTH); //lite fult
         textFieldWidth.setColumns(10);
         textFieldHeight.setEnabled(false);
         textFieldHeight.setText("" + DEFAULT_HEIGHT); //lite fult
         textFieldHeight.setColumns(10);
         //textFieldNoOfIterations.setEnabled(false);
         //textFieldNoOfIterations.setText("10000");
         //textFieldNoOfIterations.setColumns(10);
         //textFieldIterationDelay.setText("16");
         //textFieldIterationDelay.setColumns(10);
     }
 
     private void setMyFonts() {
         labelPredators.setFont(heading);
         labelPreys.setFont(heading);
         labelVegetation.setFont(heading);
         labelIterationDelay.setFont(heading);
         labelIterations.setFont(heading);
         labelDimension.setFont(heading);
         labelThreads.setFont(heading);
         labelShape.setFont(heading);
         labelObstacle.setFont(heading);
     }
 
     private void setMySpinners() {
     
 
     }
 
     /*
      private void setMySliders() {
      sliderDelayLength.setValue(16);
      sliderDelayLength.setSnapToTicks(true);  //kolla upp vad de h�r g�r...
      sliderDelayLength.setPaintTicks(true); 
      sliderDelayLength.setPaintLabels(true);
      sliderDelayLength.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
      textFieldIterationDelay.setText(sliderDelayLength.getValue()
      + "");
      }
      });
 		
      sliderPreyPopSize.setMaximum(400);
      sliderPreyPopSize.setValue(100);
      sliderPreyPopSize.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
      textFieldPreyPopSize.setText(sliderPreyPopSize.getValue()
      + "");
      }
      });
 		
      sliderPredPopSize.setValue(10);
      sliderPredPopSize.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
      textFieldPredPopSize.setText(sliderPredPopSize.getValue()
      + "");
      }
      });
 		
      sliderVegPopSize.setMaximum(1200);
      sliderVegPopSize.setValue(400);
      sliderVegPopSize.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
      textFieldVegPopSize.setText(sliderVegPopSize.getValue()
      + "");
      }
      });
 		
      sliderNoOfIterations.setEnabled(false); //TODO: h�ll koll p� den h�r
      sliderNoOfIterations.setMinimum(1);
      sliderNoOfIterations.setMaximum(20000);
      sliderNoOfIterations.setValue(10000);
      sliderNoOfIterations.setSnapToTicks(true);
      sliderNoOfIterations.setPaintTicks(true);
      sliderNoOfIterations.setPaintLabels(true);
      sliderNoOfIterations.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
      textFieldNoOfIterations.setText(sliderNoOfIterations.getValue()
      + "");
      }
      });
      }	
      */
     private void setMyButtons() {
         buttonCancel.addActionListener(listenerCancel);
         buttonAdvanced.addActionListener(listenerAdvanced);
     }
 
     private void setMyCheckBoxes() {
         checkBoxLimitIterations.addChangeListener(listenerLimitIterations);
         checkBoxCustomSize.addChangeListener(listenerCustomSize);
     }
 
     private void setMyLists() {
         listPred.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         listPred.setValueIsAdjusting(true);
         listPred.setSelectedIndices(new int[]{3});
         listPred.setModel(new AbstractListModel() {
             String[] values = SimulationSettings.PRED_VALUES;
 
             public int getSize() {
                 return values.length;
             }
 
             public Object getElementAt(int index) {
                 return values[index];
             }
         });
         listPred.setSelectedIndex(0);
 
         listPrey.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         listPrey.setValueIsAdjusting(true);
         listPrey.setSelectedIndices(new int[]{3});
         listPrey.setModel(new AbstractListModel() {
             String[] values = SimulationSettings.PREY_VALUES;
 
             public int getSize() {
                 return values.length;
             }
 
             public Object getElementAt(int index) {
                 return values[index];
             }
         });
         listPrey.setSelectedIndex(0);
 
         listVegetation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         listVegetation.setValueIsAdjusting(true);
         listVegetation.setSelectedIndices(new int[]{3});
         listVegetation.setModel(new AbstractListModel() {
             public int getSize() {
                 return SimulationSettings.GRASS_VALUES.length;
             }
 
             public Object getElementAt(int index) {
                 return SimulationSettings.GRASS_VALUES[index];
             }
         });
         listVegetation.setSelectedIndex(0);
 
         listSimDimension.setValueIsAdjusting(true);
         listSimDimension.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         listSimDimension.setSelectedIndices(new int[]{3});
         listSimDimension.setModel(new AbstractListModel() {
             public int getSize() {
                 return SimulationSettings.DIM_VALUES.length;
             }
 
             public Object getElementAt(int index) {
                 return SimulationSettings.DIM_VALUES[index];
             }
         });
         listSimDimension.setSelectedIndex(1);
 
         listMap.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         loadSimulationMaps();
         addWindowListener(new WindowAdapter() {
         	@Override
         	public void windowActivated(WindowEvent e) {
                 loadSimulationMaps();
         	}
         	
         });
     }
     
     private void loadSimulationMaps() {
     	SimulationMap lastSelected = listMap.getSelectedValue();
     	listMap.setModel(new SimulationMapListModel());
     	if(lastSelected != null) {
     		listMap.setSelectedValue(lastSelected, true);
     	}
     }
     
     private class SimulationMapListModel implements ListModel<SimulationMap> {
         	List<SimulationMap> mapList;
     		public SimulationMapListModel() {
     	        final List<SimulationMap> foundMaps = DefaultMaps.getDefaultMaps();
     	        List<SimulationMap> readMaps = MapFileHandler.readMapsFromMapsFolder();
     	        if(readMaps != null) {
     	        	foundMaps.addAll(readMaps);
     	        }
     	        mapList = foundMaps;
     		}
 			@Override
 			public int getSize() {
 				return mapList.size();
 			}
 			@Override
 			public SimulationMap getElementAt(int index) {
 				return mapList.get(index);
 			}
 			@Override
 			public void addListDataListener(ListDataListener l) {
 			}
 
 			@Override
 			public void removeListDataListener(ListDataListener l) {
 			}
     }
 
     private void setMyBorders() {
         left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
         right.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
         //panelButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
         shapeRadioPanel.setBorder(createTitledBorder("Shape of Universe"));
         listMap.setBorder(createTitledBorder("Obstacles"));
         
         listPred.setBorder(createTitledBorder("Predators"));
         listPrey.setBorder(createTitledBorder("Preys"));
         listVegetation.setBorder(createTitledBorder("Vegetation"));
         
         panelButton1.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
         panelButton2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
         
         //panelButtonStart.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
         //panelButtonCancel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
     }
 
     private Border createTitledBorder(String title) {
         Border actualBorder = BorderFactory.createTitledBorder(title);
         Border padding = BorderFactory.createEmptyBorder(10, 10, 20, 0);
 
         return BorderFactory.createCompoundBorder(padding, actualBorder);
     }
 
     private void addToLeftPanel() {
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0.5;
         c.gridx = 0;
         c.gridy = 1;
         left.add(shapeRadioPanel, c);
 
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 2;
         left.add(listMap, c);
         
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 3;
         left.add(checkBoxRecordSim, c);
     }
 
     private void addToRightPanel() {
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 1;
         right.add(listPred, c);
         //panelPred.add(sliderPredPopSize);
         //panelPred.add(textFieldPredPopSize);
 
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 1;
         c.gridy = 1;
         right.add(spinnerPredPopSize, c);
 
         //center.add(panelPred, c);
 
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 3;
         right.add(listPrey, c);
         //panelPrey.add(sliderPreyPopSize);
         //panelPrey.add(textFieldPreyPopSize);
 
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 1;
         c.gridy = 3;
         right.add(spinnerPreyPopSize, c);
 
         //center.add(panelPrey, c);
 
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 5;
         right.add(listVegetation, c);
         //panelVeg.add(sliderVegPopSize);
         //panelVeg.add(textFieldVegPopSize);
 
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 1;
         c.gridy = 5;
         right.add(spinnerVegPopSize, c);
         
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 0;
         panelButton1.add(buttonAdvanced, c);
         
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 0;
         panelButtonStart.add(buttonStart, c);
         
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 0;
         panelButtonCancel.add(buttonCancel, c);
         
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 0;
         panelButton2.add(panelButtonStart, c);
         
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 1;
         c.gridy = 0;
         panelButton2.add(panelButtonCancel, c);
 
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 7;
         right.add(panelButton1, c);
         
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.5;
         //c.weighty = 0;
         c.gridx = 0;
         c.gridy = 8;
         right.add(panelButton2, c);
     }
 
     public void showErrorMessage() {
         //TODO: create different exceptions if needed, and if-statements for different exceptions somewhere... 
         JOptionPane.showMessageDialog(this,
                 "Something went wrong.");
     }
 
     class AdvancedSettings extends JFrame {
 
         private JPanel advContentPane;
         private JPanel advLeft;
         private JPanel advRight;
         private JPanel advPanelButton;
         private JPanel advPanelIterations;
         private JPanel advPanelDelay;
         private JPanel advPanelWidth;
         private JPanel advPanelHeight;
         private JPanel advPanelNumThreads;
         private JPanel advPanelSimWindowDim;
 
         private JButton advButtonCancel;
         private JButton advButtonApply;
         private ActionListener advListenerCancel;
         private ActionListener advListenerApply;
 
         public AdvancedSettings() {
         	advContentPane = new JPanel();
             setContentPane(advContentPane);
             advLeft = new JPanel();
             advRight = new JPanel();
             advPanelButton = new JPanel();
             advPanelIterations = new JPanel();
             advPanelDelay = new JPanel();
             advPanelWidth = new JPanel();
             advPanelHeight = new JPanel();
             advPanelNumThreads = new JPanel();
             advPanelSimWindowDim = new JPanel();
 
             //setting colors to assist when doing the layout
             /*
             advLeft.setBackground(Color.BLACK);
             advRight.setBackground(Color.PINK);
             advPanelButton.setBackground(Color.GREEN);
             advPanelIterations.setBackground(Color.CYAN);
             advPanelDelay.setBackground(Color.WHITE);
             advPanelWidth.setBackground(Color.ORANGE);
             advPanelHeight.setBackground(Color.BLUE);
             */
 
             advButtonCancel = new JButton("Cancel");
             advButtonApply = new JButton("Apply");
 
             setMyAdvLayout();
             setMyAdvListeners();
             setMyAdvButtons();
             layoutNumThreads();
             layoutSimulationDimensions();
             layoutDelay();
             layoutIterations();
             setMyAdvBorders();
             addToAdv();
             
             setResizable(false);
             setTitle("Advanced Simulation Settings");
             setIconImage(new ImageIcon("res/Simulated ecosystem icon.png").getImage());
             //setBounds(100, 100, 700, 700);
             pack();
             setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
             setVisible(true);
         }
 
         private void setMyAdvListeners() {
             advListenerCancel = new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     AdvancedSettings.this.dispose();
                 }
             };
 
             advListenerApply = new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     //TODO: save the settings somehow...
                     AdvancedSettings.this.dispose();
                 }
             };
         }
 
         private void setMyAdvLayout() {
             advContentPane.setLayout(new BorderLayout());
             advLeft.setLayout(new GridBagLayout());
             advRight.setLayout(new GridBagLayout());
             advPanelButton.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
         }
                 
         private void layoutDelay() {
             advPanelDelay.setLayout(new GridBagLayout());
             
             GridBagConstraints c = new GridBagConstraints();
             c.gridx = c.gridy = 0;
             c.fill = GridBagConstraints.HORIZONTAL;
             c.weightx = 0.8;
             advPanelDelay.add(spinnerDelayLength, c);
             c.gridx++;
             c.weightx = 1 - c.weightx;
             advPanelDelay.add(new JLabel(" ms"));
         }
         
         private void layoutIterations() {
             advPanelIterations.setLayout(new GridLayout(2, 1));
             
             advPanelIterations.add(checkBoxLimitIterations);
             advPanelIterations.add(spinnerNoOfIterations);
         }
         
         private void layoutNumThreads() {
             advPanelNumThreads.setLayout(new GridLayout(3,1));
             
             advPanelNumThreads.add(radioButton2Threads);
             advPanelNumThreads.add(radioButton4Threads);
             advPanelNumThreads.add(radioButton8Threads);            
         }
         
         private void layoutSimulationDimensions() {
             advPanelSimWindowDim.setLayout(new GridBagLayout());
             
             advPanelWidth.add(labelWidth);
             advPanelWidth.add(textFieldWidth);
             advPanelHeight.add(labelHeight);
             advPanelHeight.add(textFieldHeight);
             GridBagConstraints c = new GridBagConstraints();
             c.fill = GridBagConstraints.BOTH;
             c.gridy = 0;
             advPanelSimWindowDim.add(listSimDimension, c);
             c.gridy++;
             advPanelSimWindowDim.add(checkBoxCustomSize, c);
             c.gridy++;
             advPanelSimWindowDim.add(advPanelHeight,c);
             c.gridy++;
             advPanelSimWindowDim.add(advPanelWidth, c);
         }
         
         private void setMyAdvBorders() {
             advPanelDelay.setBorder(createTitledBorder("Iteration delay"));
             advPanelIterations.setBorder(createTitledBorder("Limit number of iterations"));
             
             advPanelNumThreads.setBorder(createTitledBorder("Number of threads"));
             advPanelSimWindowDim.setBorder(createTitledBorder("Simulation dimensions"));
             
             advLeft.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
             advRight.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
             advPanelButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         }
 
         private void setMyAdvButtons() {
             advButtonCancel.addActionListener(advListenerCancel);
             advButtonApply.addActionListener(advListenerApply);
         }
 
         private void addToAdv() {
             JPanel center = new JPanel(new GridBagLayout());
             GridBagConstraints c = new GridBagConstraints();
             c.anchor = GridBagConstraints.NORTH;
             c.gridx = c.gridy = 0;
             center.add(advLeft, c);
             c.gridx++;
             center.add(advRight, c);
             
             advContentPane.add(center, BorderLayout.CENTER);
 
             advPanelButton.add(advButtonCancel);
             advPanelButton.add(advButtonApply);
             advContentPane.add(advPanelButton, BorderLayout.SOUTH);
 
             //advPanelDelay.add(sliderDelayLength);
             //advPanelDelay.add(textFieldIterationDelay);
 
             c = new GridBagConstraints();
             c.fill = GridBagConstraints.HORIZONTAL;
             c.weightx = 0.5;
             //c.weighty = 0;
             c.gridx = 0;
             c.gridy = 1;
             advLeft.add(advPanelDelay, c);
 
             c = new GridBagConstraints();
             c.fill = GridBagConstraints.HORIZONTAL;
             c.weightx = 0.5;
             //c.weighty = 0;
             c.gridx = 0;
             c.gridy = 2;
             advLeft.add(advPanelIterations, c);
 
             c = new GridBagConstraints();
             c.fill = GridBagConstraints.HORIZONTAL;
             c.weightx = 0.5;
             //c.weighty = 0;
             c.gridx = 0;
             c.gridy = 3;
             advLeft.add(advPanelNumThreads, c);
             
 
             c = new GridBagConstraints();
             c.fill = GridBagConstraints.HORIZONTAL;
             c.weightx = 0.5;
             //c.weighty = 0;
             c.gridx = 0;
             c.gridy = 0;
             advRight.add(advPanelSimWindowDim, c);
         }
     }
 
 	public void toggleVisibility() {
 		if(isVisible()) {
 			setVisible(false);
 			if(advancedSettingsView != null) {
 				advancedSettingsView.dispose();
 				advancedSettingsView = null;
 			}
 		} else {
 			setVisible(true);
 			if(advancedSettingsView == null) {
 				advancedSettingsView = new AdvancedSettings();
 			}
 		}
 	}
 }
