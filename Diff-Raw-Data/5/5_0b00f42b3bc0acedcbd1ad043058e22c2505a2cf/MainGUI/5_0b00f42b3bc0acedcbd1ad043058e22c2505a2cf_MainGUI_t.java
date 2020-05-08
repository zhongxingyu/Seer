 package ReactorEE.swing;
 
 import java.awt.Insets;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import java.awt.BorderLayout;
 import javax.swing.JLayeredPane;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingConstants;
 import java.awt.Color;
 import javax.swing.JSlider;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.JTextField;
 
 import ReactorEE.model.Repair;
 import ReactorEE.pcomponents.*;
 import ReactorEE.simulator.PlantController;
 
 
 
 import java.awt.ComponentOrientation;
 import java.util.ArrayList;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.Desktop;
 import java.awt.Font;
 import java.awt.Dimension;
 import java.io.File;
 import java.io.IOException;
 
 
 /**
  * This is the main GUI class. Only a reference to the PlantController class is needed when this class is instantiated.
  * It creates all the required components, instantiates them and connects them to the plant.
  * This class is kept as separated from the plant as possible, the only classes it interacts with are the OperatingSoftware, the PlantController and the UIData.
  * At each change in any of the components the gui is updated via dedicated method.
  * When the scores need to be shown this class creates ScoresGUI object, which has its own gui.
  * When the game is over the EndGame class is instantiated which has its own gui.
  * @author 
  */
 public class MainGUI
 {
 	protected JLayeredPane layeredPane = new JLayeredPane();
 	
 	// the only reference that is needed to the plant
     protected PlantController plantController;
     
     //the main frame
     private JFrame frmReactoree;
     
     //the field where the player should write theirs
     private JTextField nameTextField;
     
     //the buttons which effect is not dependent on the operating software
     protected JButton btnNewGame;
     protected JButton btnLoad;
     protected JButton btnSave;
     protected JButton btnShowManual;
     protected JButton btnShowScores;
     //make a number of steps
     protected JButton btnStep;
     protected JButton btnRepairOperatingSoftware;
     
     //the affect of those buttons is dependent on the state of the operating software
     private JButton btnValve1;
     private JButton btnValve2;
     protected JButton btnRepairPump1;
     protected JButton btnRepairPump2;
     protected JButton btnRepairPump3;
     protected JButton btnRepairTurbine;
     private JButton btnQuench;
     
     //labels showing the state of the different components that can fail
     private JLabel lblPump1State;
     private JLabel lblPump2State;
     private JLabel lblPump3State;
     private JLabel lblTurbineState;
     private JLabel lblOperatingSoftwareState;
     private JLabel warningLabel;
     
     private JLabel lblScore;
     
     //progress bars showing the temperature, pressure, water level and
     //health of the reactor and the condenser
     private JProgressBar progressBarReactorTemperature;
     private JProgressBar progressBarReactorPressure;
     private JProgressBar progressBarReactorHealth;
     private JProgressBar progressBarCondenserTemperature;
     private JProgressBar progressBarCondenserPressure;    
     private JProgressBar progressBarCondenserHealth;
     private JProgressBar progressBarReactorWaterLevel;
     private JProgressBar progressBarCondenserWaterLevel;
     
     
     //sliders controlling the rpm of the pumps, the level of the
     //control rods and the number of timesteps
     private JSlider sliderPump1RPM;
     private JSlider sliderPump2RPM;
     private JSlider sliderPump3RPM;
     private JSlider sliderRodsLevel;
     
     //image icons containing the images that are used in the gui
     private ImageIcon repairButtonEnabledImageIcon;
     private ImageIcon repairButtonDisabledImageIcon;
     private ImageIcon stateSafeImageIcon;
     private ImageIcon stateBeingRepairedImageIcon;
     private ImageIcon stateBrokenImageIcon;
     private ImageIcon valveOpenedImageIcon;
     private ImageIcon valveClosedImageIcon;
     private ImageIcon newGameImageIcon;
     private ImageIcon loadGameImageIcon;
     private ImageIcon saveGameImageIcon;
     private ImageIcon viewManualImageIcon;
     private ImageIcon viewScoresImageIcon;
     private ImageIcon nextStepImageIcon;
     private ImageIcon nextStepPausedImageIcon;
     
     //the repair buttons are not disabled directly but a different image is associated
     //with them when they cannot be used - this variable prevents them from being used
     //when they are 'disabled'
     protected boolean controlButtonsEnabled = true;
     
     //a shorthand for the list of the components that are being repaired
     private ArrayList<String> componentsBeingRepaired = new ArrayList<String>();
     
     //the string that is shown initially in the player name field
     protected String initialNameValue = "Change me";
 
     //a temporary value which has different usages 
     private int tempValue;
     
 
     
     /**
      * The constructor sets the plantController object, initialises the gui
      * and makes it visible.
      * @param plantController
      */
     public MainGUI(PlantController plantController)
     {
         this.plantController = plantController;
         initialize();
         frmReactoree.setVisible(true);
     }
 
 
     /**
      * Initialises the contents of the frame.
      */
     private void initialize()
     {
     	//instantiates the main frame
         frmReactoree = new JFrame();
         frmReactoree.setTitle("ReactorEE");
         frmReactoree.setBounds(100, 100, 1049, 740);
         frmReactoree.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         //which is layered - two layers: background picture - layer 0
         //all interactive components - layer 1
         
         
         frmReactoree.getContentPane().add(layeredPane, BorderLayout.CENTER);
         
         //loads and sets the background image
         java.net.URL imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/plantBackground.png");
         ImageIcon backgroundImageIcon = new ImageIcon(imageURL);
         
         warningLabel = new JLabel("Warning");
         warningLabel.setToolTipText("Reactor is overheating!");
         warningLabel.setIcon(new ImageIcon(MainGUI.class.getResource("/ReactorEE/graphics/animates.gif")));
         warningLabel.setBounds(27, 58, 500, 500);
         layeredPane.add(warningLabel);
         warningLabel.setVisible(false);
         JLabel backgroundImageLabel = new JLabel(backgroundImageIcon);
         backgroundImageLabel.setBackground(new Color(0, 153, 0));
         backgroundImageLabel.setBounds(0, 0, 1040, 709);
         layeredPane.add(backgroundImageLabel);
 
         //loads all the images that are required for the image labels
         //the path is relative to the project
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/btnRepairEnabled.png");
         repairButtonEnabledImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/btnRepairDisabled.png");
         repairButtonDisabledImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/stateSafe.png");
         stateSafeImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/stateBeingRepaired.png");
         stateBeingRepairedImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/stateBroken.png");
         stateBrokenImageIcon = new ImageIcon(imageURL);  
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/valveOpened.png");
         valveOpenedImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/valveClosed.png");
         valveClosedImageIcon = new ImageIcon(imageURL);
         
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/newButtonLabel.png");
         newGameImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/loadButtonLabel.png");
         loadGameImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/saveButtonLabel.png");
         saveGameImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/manualButtonLabel.png");
         viewManualImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/scoresButtonLabel.png");
         viewScoresImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/nextButtonLabel.png");
         nextStepImageIcon = new ImageIcon(imageURL);
         imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/nextButtonPausedLabel.png");
         nextStepPausedImageIcon = new ImageIcon(imageURL);
         
         //initialises the label that shows the score
         lblScore = new JLabel("0");
         lblScore.setFont(new Font("Tahoma", Font.PLAIN, 30));
         lblScore.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         lblScore.setBounds(860, 81, 160, 23);
         layeredPane.setLayer(lblScore, 1);
         layeredPane.add(lblScore);
         
         //the player should type in their name in that text field
         //when the field loses focus the text is checked and if it is not
         //empty or bigger than 15 characters, it is set as the operator's name
         //if the text is bigger than 15 characters only the first 15 are used
         //if the text field is empty, the initial text is set put it
         nameTextField = new JTextField(initialNameValue);
        nameTextField.setFont(new Font("OCR A Std", Font.PLAIN, 15));
         nameTextField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         nameTextField.setColumns(20);
         nameTextField.setOpaque(false);
         nameTextField.setBorder(null);
        nameTextField.setBounds(0, 14, 117, 20);
         nameTextField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusGained(FocusEvent arg0) {
                 nameTextField.selectAll();
             }
             @Override
             public void focusLost(FocusEvent e) {
                 if(nameTextField.getText() == "")
                 {
                     nameTextField.setText(initialNameValue);
                     nameTextField.selectAll();
                 }
                 else
                 	if(nameTextField.getText().length() > 15)
                 		plantController.getPlant().setOperatorName(nameTextField.getText().substring(0,15));
                 	else plantController.getPlant().setOperatorName(nameTextField.getText());
             }
         });
         nameTextField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg0) {
                 if(nameTextField.getText() != "")
                     plantController.getPlant().setOperatorName(nameTextField.getText());
             }
         });
         layeredPane.setLayer(nameTextField, 1);
         layeredPane.add(nameTextField);
         
         //all the labels that show component states are
         //initialised with green light showing
         lblPump1State = new JLabel(stateSafeImageIcon);
         lblPump1State.setBounds(273, 592, 78, 23);
         layeredPane.setLayer(lblPump1State, 1);
         layeredPane.add(lblPump1State);
         
         lblPump2State = new JLabel(stateSafeImageIcon);
         lblPump2State.setBounds(496, 592, 78, 23);
         layeredPane.setLayer(lblPump2State, 1);
         layeredPane.add(lblPump2State);
         
         lblPump3State = new JLabel(stateSafeImageIcon);
         lblPump3State.setBounds(716, 592, 78, 23);
         layeredPane.setLayer(lblPump3State, 1);
         layeredPane.add(lblPump3State);
         
         lblTurbineState = new JLabel(stateSafeImageIcon);
         lblTurbineState.setBounds(826, 592, 78, 23);
         layeredPane.setLayer(lblTurbineState, 1);
         layeredPane.add(lblTurbineState);
         
         lblOperatingSoftwareState = new JLabel(stateSafeImageIcon);
         lblOperatingSoftwareState.setBounds(927, 592, 78, 23);
         layeredPane.setLayer(lblOperatingSoftwareState, 1);
         layeredPane.add(lblOperatingSoftwareState);
         
         //creation and instantiation of the progress bars
         //change state listeners added at the end of this method
         progressBarReactorTemperature = new JProgressBar();
         progressBarReactorTemperature.setBounds(781, 168, 234, 14);
         layeredPane.setLayer(progressBarReactorTemperature, 1);
         layeredPane.add(progressBarReactorTemperature);
         
         progressBarReactorPressure = new JProgressBar();
         progressBarReactorPressure.setBounds(781, 203, 234, 14);
         layeredPane.setLayer(progressBarReactorPressure, 1);
         layeredPane.add(progressBarReactorPressure);
         
         progressBarReactorHealth = new JProgressBar();
         progressBarReactorHealth.setBounds(781, 273, 234, 14);
         layeredPane.setLayer(progressBarReactorHealth, 1);
         layeredPane.add(progressBarReactorHealth);
         
         progressBarReactorWaterLevel = new JProgressBar();
         progressBarReactorWaterLevel.setBounds(781, 237, 234, 14);
         layeredPane.setLayer(progressBarReactorWaterLevel, 1);
         layeredPane.add(progressBarReactorWaterLevel);
         
         progressBarCondenserTemperature = new JProgressBar();
         progressBarCondenserTemperature.setBounds(781, 359, 234, 14);
         layeredPane.setLayer(progressBarCondenserTemperature, 1);
         layeredPane.add(progressBarCondenserTemperature);
         
         progressBarCondenserPressure = new JProgressBar();
         progressBarCondenserPressure.setBounds(781, 394, 234, 14);
         layeredPane.setLayer(progressBarCondenserPressure, 1);
         layeredPane.add(progressBarCondenserPressure);
         
         progressBarCondenserHealth = new JProgressBar();
         progressBarCondenserHealth.setBounds(781, 468, 234, 14);
         layeredPane.setLayer(progressBarCondenserHealth, 1);
         layeredPane.add(progressBarCondenserHealth);
         
         progressBarCondenserWaterLevel = new JProgressBar();
         progressBarCondenserWaterLevel.setBounds(781, 430, 234, 14);
         progressBarCondenserWaterLevel.setForeground(new Color(0, 255, 0));
         layeredPane.setLayer(progressBarCondenserWaterLevel, 1);
         layeredPane.add(progressBarCondenserWaterLevel);
         
         //creation and instantiation of the sliders
         //every slider calls the appropriate method in the OperatingSoftware
         //requests its execution from the plantController
         //and updates the gui
         sliderPump1RPM = new JSlider();
         sliderPump1RPM.setOpaque(false);
         sliderPump1RPM.setBounds(173, 581, 25, 108);
         sliderPump1RPM.setOrientation(SwingConstants.VERTICAL);
         sliderPump1RPM.setMaximum(1000);
         sliderPump1RPM.setValue(0);
         sliderPump1RPM.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent arg0) {
                 if(plantController.getUIData().getPumps().get(0).isOperational() && controlButtonsEnabled)
                 {   plantController.getPlant().getOperatingSoftware().setPumpRpm(1, sliderPump1RPM.getValue());
                     plantController.executeStoredCommand();
                     updateGUI();
                 }
             }
         });
         layeredPane.setLayer(sliderPump1RPM, 1);
         layeredPane.add(sliderPump1RPM);
           
         sliderPump2RPM = new JSlider();
         sliderPump2RPM.setBounds(384, 581, 25, 108);
         sliderPump2RPM.setOrientation(SwingConstants.VERTICAL);
         sliderPump2RPM.setOpaque(false);
         sliderPump2RPM.setValue(0);
         sliderPump2RPM.setMaximum(1000);
         sliderPump2RPM.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent arg0) {
                 if(plantController.getUIData().getPumps().get(1).isOperational() && controlButtonsEnabled)
                 {
                     plantController.getPlant().getOperatingSoftware().setPumpRpm(2, sliderPump2RPM.getValue());
                     plantController.executeStoredCommand();
                     updateGUI();
                 }
             }
         });
         layeredPane.setLayer(sliderPump2RPM, 1);
         layeredPane.add(sliderPump2RPM);
         
         sliderPump3RPM = new JSlider();
         sliderPump3RPM.setBounds(597, 581, 42, 108);
         sliderPump3RPM.setOpaque(false);
         sliderPump3RPM.setOrientation(SwingConstants.VERTICAL);
         sliderPump3RPM.setMaximum(1000);
         sliderPump3RPM.setValue(0);
         sliderPump3RPM.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent arg0) {
                 if(plantController.getUIData().getPumps().get(2).isOperational() && controlButtonsEnabled)
                 {
                     plantController.getPlant().getOperatingSoftware().setPumpRpm(3, sliderPump3RPM.getValue());
                     plantController.executeStoredCommand();
                     updateGUI();
                 }
             }
         });
         layeredPane.setLayer(sliderPump3RPM, 1);
         layeredPane.add(sliderPump3RPM);
         
         sliderRodsLevel = new JSlider();
         sliderRodsLevel.setOpaque(false);
         sliderRodsLevel.setBounds(27, 592, 25, 106);
         sliderRodsLevel.setOrientation(SwingConstants.VERTICAL);
         sliderRodsLevel.setValue(0);
         sliderRodsLevel.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 if(controlButtonsEnabled)
                 {
                     plantController.getPlant().getOperatingSoftware().setControlRods(100-sliderRodsLevel.getValue());
                     plantController.executeStoredCommand();
                     updateGUI();
                 }
                 
             }
         });
         layeredPane.setLayer(sliderRodsLevel, 1);
         layeredPane.add(sliderRodsLevel);
         
         //starts a new game when pressed
         //and updates the gui
         btnNewGame = new JButton(newGameImageIcon);
         btnNewGame.setToolTipText("New Game");
         btnNewGame.setMargin(new Insets(0,0,0,0));
         btnNewGame.setBorder(null);
         btnNewGame.setBounds(749, 17, 40, 40);
         btnNewGame.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg0) {
                 btnNewGame.setEnabled(false);
                 //plantController.newGame(initialNameValue);
                 //updateGUI();
                 btnNewGame.setEnabled(true);
                 frmReactoree.dispose();
                 new GameTypeSelectionGUI();
             }
         });
         layeredPane.setLayer(btnNewGame, 1);
         layeredPane.add(btnNewGame);
         
         //loads the saved game and updates the gui
         btnLoad = new JButton(loadGameImageIcon);
         btnLoad.setToolTipText("Load Game");
         btnLoad.setMargin(new Insets(0,0,0,0));
         btnLoad.setBorder(null);
         btnLoad.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		plantController.loadGame();
         		updateGUI();
         	}
         });
         btnLoad.setBounds(799, 17, 40, 40);
         layeredPane.setLayer(btnLoad, 1);
         layeredPane.add(btnLoad);
         
         //if the current game is not over it saves it
         btnSave = new JButton(saveGameImageIcon);
         btnSave.setToolTipText("Save Game");
         btnSave.setMargin(new Insets(0,0,0,0));
         btnSave.setBorder(null);
         btnSave.setBounds(849, 17, 40, 40);
         btnSave.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 btnSave.setEnabled(false);
                 if(!plantController.getUIData().isGameOver())
                  plantController.saveGame();
                 btnSave.setEnabled(true);
             }
         });
         layeredPane.setLayer(btnSave, 1);
         layeredPane.add(btnSave);
         
         //shows the scores so far
         //by calling a function that instantiates the scoresGUI
         btnShowScores = new JButton(viewScoresImageIcon);
         btnShowScores.setToolTipText("Leaderboard");
         btnShowScores.setMargin(new Insets(0,0,0,0));
         btnShowScores.setBorder(null);
         btnShowScores.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		showScores();
         	}
         });
         btnShowScores.setBounds(949, 17, 40, 40);
         layeredPane.setLayer(btnShowScores, 1);
         layeredPane.add(btnShowScores);
         
         //displays the user manual by opening it with its default program
         btnShowManual = new JButton(viewManualImageIcon);
         btnShowManual.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		java.net.URL manualURL = this.getClass().getClassLoader().getResource("Manual3.pdf");
                 try{
                 	 Desktop.getDesktop().open(new File(manualURL.getPath()));
                 }catch (IOException e)
                 {
                     e.printStackTrace();
                 }
 
         	}
         });
         btnShowManual.setToolTipText("Manual");
         btnShowManual.setMargin(new Insets(0,0,0,0));
         btnShowManual.setBorder(null);
         btnShowManual.setBounds(899, 17, 40, 40);
         layeredPane.setLayer(btnShowManual, 1);
         layeredPane.add(btnShowManual);
         
         //when this button is pressed it takes the value of the sliderNumber of time steps,
         //and issues a single time step at a time to the plant
         //if the plant has not failed and updates the gui
         //if the plant has failed - invokes the end game handler
         btnStep = new JButton(nextStepImageIcon);
         btnStep.setToolTipText("Step");
         btnStep.setOpaque(false);
         btnStep.setBounds(426, 500, 49, 39);
         btnStep.setMargin(new Insets(0,0,0,0));
         btnStep.setBorder(null);
         btnStep.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg0) {
                
                 	if (!plantController.getUIData().isGameOver()) {
 						plantController.togglePaused();
 						if(plantController.getPlant().isPaused())
 							btnStep.setIcon(nextStepImageIcon);
 						else
 							btnStep.setIcon(nextStepPausedImageIcon);
 						updateGUI();
 					}
             	
                 if(plantController.getUIData().isGameOver())
                 {
                 	//Show score and create a new game selection screen
                 	   
                 	//frame.dispose();
                 	endGameHandler();
                 }
                     
             }
         });
         layeredPane.setLayer(btnStep, 1);
         layeredPane.add(btnStep);
 
 
         //used to open and close the first valve
         btnValve1 = new JButton(valveOpenedImageIcon);
         btnValve1.setBounds(860, 508, 59, 23);
         btnValve1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (controlButtonsEnabled)
                 {
                 	//checks if the valve 1 state and alternates it
                     if (plantController.getUIData().getValves().get(0).isOpen())
                     {
                         plantController.getPlant().getOperatingSoftware().setValve(1, false);
                         plantController.executeStoredCommand();
                         updateGUI();
                         
                     } else
                     {
                         plantController.getPlant().getOperatingSoftware().setValve(1, true);
                         plantController.executeStoredCommand();
                         updateGUI();
                     }
                 }
             }
         });
         layeredPane.setLayer(btnValve1, 1);
         layeredPane.add(btnValve1);
         
         //used to open and close the second valve
         btnValve2 = new JButton(valveOpenedImageIcon);
         btnValve2.setBounds(968, 508, 59, 23);
         btnValve2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (controlButtonsEnabled)
                 {
                 	//checks if the valve 1 state and alternates it
                     if (plantController.getUIData().getValves().get(1).isOpen())
                     {
                         plantController.getPlant().getOperatingSoftware().setValve(2, false);
                         plantController.executeStoredCommand();
                         updateGUI();
                     } else
                     {
                         plantController.getPlant().getOperatingSoftware().setValve(2, true);
                         plantController.executeStoredCommand();
                         updateGUI();
                     }
                 }
             }
         });
         layeredPane.setLayer(btnValve2, 1);
         layeredPane.add(btnValve2);
         
 
         //issues a repair command to pump 1 if it is not operational
         btnRepairPump1 = new JButton(repairButtonDisabledImageIcon);
         btnRepairPump1.setBounds(283, 626, 59, 57);
         btnRepairPump1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(!plantController.getUIData().getPumps().get(0).isOperational() && controlButtonsEnabled)
                 {
                     plantController.getPlant().getOperatingSoftware().repairPump(1);
                     plantController.executeStoredCommand();
                     updateGUI();
                     
                 } 
             }
         });
         btnRepairPump1.setMargin(new Insets(0,0,0,0));
         btnRepairPump1.setBorder(null);
         layeredPane.setLayer(btnRepairPump1, 1);
         layeredPane.add(btnRepairPump1);
         
         //issues a repair command to pump 2 if it is not operational
         btnRepairPump2 = new JButton(repairButtonDisabledImageIcon);
         btnRepairPump2.setBounds(506, 626, 59, 57);
         btnRepairPump2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(!plantController.getUIData().getPumps().get(1).isOperational() && controlButtonsEnabled)
                 {
                     plantController.getPlant().getOperatingSoftware().repairPump(2);
                     plantController.executeStoredCommand();
                     updateGUI();
                     
                 } 
             }
         });
         btnRepairPump2.setMargin(new Insets(0,0,0,0));
         btnRepairPump2.setBorder(null);
         layeredPane.setLayer(btnRepairPump2, 1);
         layeredPane.add(btnRepairPump2);
         
         //issues a repair command to pump 3 if it is not operational
         btnRepairPump3 = new JButton(repairButtonDisabledImageIcon);
         btnRepairPump3.setBounds(726, 626, 59, 57);
         btnRepairPump3.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(!plantController.getUIData().getPumps().get(2).isOperational() && controlButtonsEnabled)
                 {
                     plantController.getPlant().getOperatingSoftware().repairPump(3);
                     plantController.executeStoredCommand();
                     updateGUI();
                     
                 } 
             }
         });
         btnRepairPump3.setMargin(new Insets(0,0,0,0));
         btnRepairPump3.setBorder(null);
         layeredPane.setLayer(btnRepairPump3, 1);
         layeredPane.add(btnRepairPump3);
         
         //issues a repair command to the turbine if it is not operational
         btnRepairTurbine = new JButton(repairButtonDisabledImageIcon);
         btnRepairTurbine.setBounds(836, 626, 59, 57);
         btnRepairTurbine.setMargin(new Insets(0,0,0,0));
         btnRepairTurbine.setBorder(null);
         btnRepairTurbine.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(!plantController.getUIData().isTurbineFunctional()  && controlButtonsEnabled)
                 {
                     plantController.getPlant().getOperatingSoftware().repairTurbine();
                     plantController.executeStoredCommand();
                     updateGUI();
                     
                 }  
             }
         });
         layeredPane.setLayer(btnRepairTurbine, 1);
         layeredPane.add(btnRepairTurbine);
         
         //directly repairs the operating software - commands from this button are directly
         //executed by the plant and cannot fail
         btnRepairOperatingSoftware = new JButton(repairButtonDisabledImageIcon);
         btnRepairOperatingSoftware.setPreferredSize(new Dimension(93, 71));
         btnRepairOperatingSoftware.setBounds(937, 626, 59, 57);
         btnRepairOperatingSoftware.setMargin(new Insets(0,0,0,0));
         btnRepairOperatingSoftware.setBorder(null);
         btnRepairOperatingSoftware.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(!plantController.getUIData().isOperatingSoftwareFunctional())
                 {
                     plantController.repairOperatingSoftware();
                     updateGUI();
                     
                 } 
             }
         });
         btnQuench = new JButton("QUENCH!");
         btnQuench.setIcon(new ImageIcon(MainGUI.class.getResource("/ReactorEE/graphics/btnRepairDisabled.png")));
         btnQuench.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		plantController.getPlant().getOperatingSoftware().quench();
         		plantController.executeStoredCommand();
                 updateGUI();
         	}
         });
         layeredPane.setLayer(btnQuench, 1);
         btnQuench.setBounds(70, 626, 59, 62);
         layeredPane.add(btnQuench);
         
         
         layeredPane.setLayer(btnRepairOperatingSoftware, 1);
         layeredPane.add(btnRepairOperatingSoftware);
       
         //adds change listeners to the progress bars. Every time a bar's value is changed,
         //the colour of the bar changes depending on what is its value
         //temperature and pressure bars change colour smoothly from blue to red
         //health pressure bars change colour smoothly from red to green
         progressBarCondenserHealth.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 colourProgressBarRedToGreen(progressBarCondenserHealth);
             }
         });
         progressBarCondenserPressure.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 colourProgressBarBlueToRed(progressBarCondenserPressure);
             }
         });
         progressBarCondenserTemperature.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 colourProgressBarBlueToRed(progressBarCondenserTemperature);
             }
         });
         progressBarReactorWaterLevel.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 colourProgressBarRedToGreen(progressBarReactorWaterLevel);
             }
         });
         progressBarReactorHealth.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 colourProgressBarRedToGreen(progressBarReactorHealth);
             }
         });
         progressBarReactorPressure.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 colourProgressBarBlueToRed(progressBarReactorPressure);
             }
         });
         progressBarReactorTemperature.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 colourProgressBarBlueToRed(progressBarReactorTemperature);
             }
         });
         
         //after everything but the name is set the gui is updates so it
         //synchronises with the plant
         updateGUI();
         nameTextField.setText(initialNameValue);
     }
     
     
     /**
      * This method takes a progress bar, gets its value and based on it
      * sets the bars colour, from blue to red, the calculations involved
      * give the desired colour
      * @param pb
      */
     private void colourProgressBarBlueToRed(JProgressBar pb)
     {
         int pbv = pb.getValue();
         
         //red green and blue colour components
         //used to create the new colour
         int r=0,g=0,b=255;
         
         if(pbv>0 && pbv<=20)
         {
             g=(int) (pbv*12.75);
             //casting is needed because of the new Colour()
             //constructor type - takes int values
         }
         else if(pbv>20 && pbv<=45)
         {
             r=0;g=255;b=(int) (255-((pbv-20)*10.2));
         }
         else if(pbv>45 && pbv<=65)
         {
             r= (int) ((pbv-45)*12.75);g=255;b=0;
         }
         else if(pbv>65 && pbv<=90)
         {
             r=255;g=(int) (255-((pbv-65)*10.2));b=0;
         }
         else if(pbv>90 && pbv<=100)
         {
             r=255;g=0;b=0;
         }
         pb.setForeground(new Color(r, g, b));
         
     }
 
     /**
      * This method takes a progress bar, gets its value and based on it
      * sets the bars colour, from red to green, the calculations involved
      * give the desired colour
      * @param pb
      */
     private void colourProgressBarRedToGreen(JProgressBar pb)
     {
         int pbv = pb.getValue();
         int r=255,g=0,b=0;
         
         if(pbv>0 && pbv<=20)
         {
             r=255; g=0; b=0;
         }
         else if(pbv>20 && pbv<=60)
         {
             r=255;g = (int) ((pbv-20)*6.375);
         }
         else if(pbv>60 && pbv<=90)
         {
             r= (int) (255-((pbv-60)*8.5));g=255;b=0;
         }
         else if(pbv>90 && pbv<=100)
         {
             r=0;g=255;b=0;
         }
         pb.setForeground(new Color(r, g, b));
     }
 
     /**
      * This method updates the appearance of the gui
      * synchronising it with the plant
      */
     public void updateGUI()
     {
     	//updates the information that is stored in the UIData object
         plantController.getUIData().updateUIData();
         
         //resets the list with components that are being repaired
         componentsBeingRepaired.clear();
         
         //restores the state of the control buttons and sliderRodsLevel variables to true
         controlButtonsEnabled = true;
         
         //updates the operators name that is shown to that that is stored,
         //useful when a game is being loaded
         //Only change when text box is not in focus, this allows operator name to be changed while the game is running.
         if(!nameTextField.isFocusOwner())
         	nameTextField.setText(plantController.getUIData().getOperatorName());
         
         //updates the score and enables the buttons the control the valves
         //they can be disabled if the operatingSoftware is being repaired
         lblScore.setText(""+plantController.getUIData().getScore());
         btnValve1.setEnabled(true);
         btnValve2.setEnabled(true);
         
         //sets the button valve icons appropriately
         if(plantController.getUIData().getValves().get(0).isOpen())
             btnValve1.setIcon(valveOpenedImageIcon);
         else
             btnValve1.setIcon(valveClosedImageIcon);
         
         if(plantController.getUIData().getValves().get(1).isOpen())
             btnValve2.setIcon(valveOpenedImageIcon);
         else
             btnValve2.setIcon(valveClosedImageIcon);
         
         //sets the level of the control rods appropriately 
         tempValue = plantController.getUIData().getControlRodsPercentage();
         if(tempValue >=0 && tempValue <= 100)
         	
         	//it is 100 - tempValue because in the plant it is shown how much
         	//the control rods are inside the rods while in the gui it shows how
         	//much the control rods are out of the rods
             sliderRodsLevel.setValue(100 - tempValue);
                 
         //sets the values of the progress bars by scaling the value to 100
         tempValue = plantController.getUIData().getReactorHealth();
         if(tempValue >=0 && tempValue <= 100)
             progressBarReactorHealth.setValue(tempValue);
         
         tempValue = plantController.getUIData().getCondenserHealth();
         if(tempValue >=0 && tempValue <= 100)
             progressBarCondenserHealth.setValue(tempValue);
         
         tempValue = plantController.getUIData().getReactorTemperature();
         if(tempValue >=0 && tempValue <= 3000)
             progressBarReactorTemperature.setValue((int) tempValue/30);
         else if(tempValue > 3000)
         		progressBarReactorTemperature.setValue(100);
         
         tempValue = plantController.getUIData().getCondenserTemperature();
         if(tempValue >=0 && tempValue <= 2000)
             progressBarCondenserTemperature.setValue((int) tempValue/20);
         else if(tempValue > 2000)
     		progressBarCondenserTemperature.setValue(100);
         
         tempValue = plantController.getUIData().getReactorPressure();
         if(tempValue >=0 && tempValue <= 2000)
             progressBarReactorPressure.setValue((int) tempValue/20);
         else if(tempValue > 2000)
     		progressBarReactorPressure.setValue(100);
         
         tempValue = plantController.getUIData().getCondenserPressure();
         if(tempValue >=0 && tempValue <= 2000)
             progressBarCondenserPressure.setValue((int) tempValue/20);
         else if(tempValue > 2000)
     		progressBarCondenserPressure.setValue(100);
         
         tempValue = plantController.getUIData().getReactorWaterVolume();
         if(tempValue >=0 && tempValue <= 10000)
             progressBarReactorWaterLevel.setValue((int) tempValue/100);  
         
         tempValue = plantController.getUIData().getCondenserWaterVolume();
         if(tempValue >=0 && tempValue <= 10000)
             progressBarCondenserWaterLevel.setValue((int) tempValue/100);
         
         tempValue = plantController.getUIData().getControlRodsPercentage();
         if(tempValue >=0 && tempValue <= 100)
             sliderRodsLevel.setValue((int) 100 - tempValue);
         
         tempValue = plantController.getUIData().getPumps().get(0).getRpm();
         if(tempValue >=0 && tempValue <= 1000)
             sliderPump1RPM.setValue((int) tempValue);
         
         tempValue = plantController.getUIData().getPumps().get(1).getRpm();
         if(tempValue >=0 && tempValue <= 1000)
             sliderPump2RPM.setValue((int) tempValue);
         
         tempValue = plantController.getUIData().getPumps().get(2).getRpm();
         if(tempValue >=0 && tempValue <= 1000)
             sliderPump3RPM.setValue((int) tempValue);
         
         
         
         //reads all the components that are being repaired and adds them to a short hand
         //string quick reference list
         for(Repair repair:plantController.getPlant().getBeingRepaired())
         {
             if(repair.getPlantComponent() instanceof ReactorEE.pcomponents.Pump)
             {
                 int id = ((Pump) repair.getPlantComponent()).getID();
                 
                 componentsBeingRepaired.add("pump"+id);
             }
             
             if(repair.getPlantComponent() instanceof ReactorEE.pcomponents.Turbine)
             {
                 componentsBeingRepaired.add("turbine");
             }
             
             if(repair.getPlantComponent() instanceof ReactorEE.pcomponents.OperatingSoftware)
             {
                 componentsBeingRepaired.add("operatingSoftware");
             }
         }
         
         //checks which components are being repaired and updates the gui in an appropriate way
         //if a component is being repaired its controls are disabled and a yellow light is showing
         if(componentsBeingRepaired.contains("pump1"))
         {
             lblPump1State.setIcon(stateBeingRepairedImageIcon);
             sliderPump1RPM.setEnabled(false);
             btnRepairPump1.setIcon(repairButtonDisabledImageIcon);
         }//if a component has failed and is not repaired its controls are disabled and red light is showing
         else if(!plantController.getUIData().getPumps().get(0).isOperational())
         {
             lblPump1State.setIcon(stateBrokenImageIcon);
             sliderPump1RPM.setEnabled(false);
             btnRepairPump1.setIcon(repairButtonEnabledImageIcon);
         }else//the component is in its normal safe operating state
         	 //its controls are enabled and green light is showing
         {
             lblPump1State.setIcon(stateSafeImageIcon);
             sliderPump1RPM.setEnabled(true);
             sliderPump1RPM.setValue(plantController.getUIData().getPumps().get(0).getRpm());
             btnRepairPump1.setIcon(repairButtonDisabledImageIcon);
         }
         
         if(componentsBeingRepaired.contains("pump2"))
         {
             lblPump2State.setIcon(stateBeingRepairedImageIcon);
             sliderPump2RPM.setEnabled(false);
             btnRepairPump2.setIcon(repairButtonDisabledImageIcon);
         }else if(!plantController.getUIData().getPumps().get(1).isOperational())
         {
             lblPump2State.setIcon(stateBrokenImageIcon);
             sliderPump2RPM.setEnabled(false);
             btnRepairPump2.setIcon(repairButtonEnabledImageIcon);
         }else
         {
             lblPump2State.setIcon(stateSafeImageIcon);
             sliderPump2RPM.setEnabled(true);
             sliderPump2RPM.setValue(plantController.getUIData().getPumps().get(1).getRpm());
             btnRepairPump2.setIcon(repairButtonDisabledImageIcon);
         }
         
         if(componentsBeingRepaired.contains("pump3"))
         {   lblPump3State.setIcon(stateBeingRepairedImageIcon);
             sliderPump3RPM.setEnabled(false);
             btnRepairPump3.setIcon(repairButtonDisabledImageIcon);
         }else if(!plantController.getUIData().getPumps().get(2).isOperational())
         {   
             lblPump3State.setIcon(stateBrokenImageIcon);
             sliderPump3RPM.setEnabled(false);
             btnRepairPump3.setIcon(repairButtonEnabledImageIcon);
         }else
         {
            
             lblPump3State.setIcon(stateSafeImageIcon);
             sliderPump3RPM.setEnabled(true);
             sliderPump3RPM.setValue(plantController.getUIData().getPumps().get(2).getRpm());
             btnRepairPump3.setIcon(repairButtonDisabledImageIcon);
         }
         
         if(componentsBeingRepaired.contains("turbine"))
         {
             lblTurbineState.setIcon(stateBeingRepairedImageIcon);
             btnRepairTurbine.setIcon(repairButtonDisabledImageIcon);
         }else if(!plantController.getUIData().isTurbineFunctional())
         {
             lblTurbineState.setIcon(stateBrokenImageIcon);
             btnRepairTurbine.setIcon(repairButtonEnabledImageIcon);
         }else
         {
             lblTurbineState.setIcon(stateSafeImageIcon);
             btnRepairTurbine.setIcon(repairButtonDisabledImageIcon);
         }
         
         //if the operating software is being repaired all components that rely on it for their commands to
         //be executed are disabled
         if(componentsBeingRepaired.contains("operatingSoftware"))
         {
             lblOperatingSoftwareState.setIcon(stateBeingRepairedImageIcon);
             btnRepairOperatingSoftware.setIcon(repairButtonDisabledImageIcon);
             controlButtonsEnabled = false;
             sliderPump1RPM.setEnabled(false);
             sliderPump2RPM.setEnabled(false);
             sliderPump3RPM.setEnabled(false);
             sliderRodsLevel.setEnabled(false);
             btnRepairPump1.setIcon(repairButtonDisabledImageIcon);
             btnRepairPump2.setIcon(repairButtonDisabledImageIcon);
             btnRepairPump3.setIcon(repairButtonDisabledImageIcon);
             btnRepairTurbine.setIcon(repairButtonDisabledImageIcon);
             btnValve1.setEnabled(false);
             btnValve2.setEnabled(false);
         }else if(!plantController.getUIData().isOperatingSoftwareFunctional())
         {
         	//otherwise just set its light to show red and enable its repair button
             lblOperatingSoftwareState.setIcon(stateBrokenImageIcon);
             btnRepairOperatingSoftware.setIcon(repairButtonEnabledImageIcon);
         }else
         {   //otherwise just set its light to show green and disable its repair button
             lblOperatingSoftwareState.setIcon(stateSafeImageIcon);
             btnRepairOperatingSoftware.setIcon(repairButtonDisabledImageIcon);
             sliderRodsLevel.setEnabled(true);
         }   
         
         //Change the play/pause button depending on whether the game is paused or running.
         if(plantController.getPlant().isPaused())
         	btnStep.setIcon(nextStepImageIcon);
         else
         	btnStep.setIcon(nextStepPausedImageIcon);
         
         if(plantController.getPlant().getReactor().isQuenchable()){
         	btnQuench.setEnabled(true);
         	warningLabel.setVisible(true);
         }else{
         	btnQuench.setEnabled(false);
         	warningLabel.setVisible(false);
         }
     }
     
     /**
      * called when the the game is over - creates a new EndGame object passing a reference to this object,
      * the operator's name and the end score
      * then it updates the gui and set the slider for the timesteps to 1
      */
     public void endGameHandler()
     {
     	@SuppressWarnings("unused")
 		EndGameGUI endGameGui = new EndGameGUI(this, plantController.getUIData().getScore());
     	plantController.togglePaused();
     	//plantController.newGame(initialNameValue);
     	//updateGUI();
     	//sliderNumberOfSteps.setValue(1);
     	frmReactoree.dispose();
 
     	new GameTypeSelectionGUI();
     }
     
     
     /**
      * 
      * @return the main frame - used for relative positioning
      */
     public JFrame getFrame()
     {
     	return frmReactoree;
     }
     
     
     /**
      * called when the the scores should be shown - creates a new ScoresGUI object passing a reference to this object,
      * and the plantControllers object
      */
     private void showScores()
     {
     	@SuppressWarnings("unused")
 		ScoresGUI scoresGui = new ScoresGUI(this, plantController);
     }
 }
