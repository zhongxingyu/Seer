 package galapagos;
 
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * A graphical user-interface for creating and viewing Biotope's.  At
  * the top of the screen there are a set of buttons for creating and
  * simulating the Biotope.  At the middle of the screen the Biotope is
  * shown graphically.  At the bottom of the screen there are a panel
  * with statistics about the different types of behaviors Its possible
  * to spawn new Finches by choosing a behavior on the right and "draw"
  * them on the Biotope.  On the left part of the screen you can turn
  * on and of the graphical display and the logger (for performance),
  * its also here you change the updaterate.
  * 
  */
 public class GalapagosFrame extends JFrame {
 
     private AreaPanel area;
     public Map<Behavior, Color> colorMap;
     public int pixelSize;
     private NicerStatisticsPanel statistics;
     private BiotopeLogger logger;
     private BiotopeController controller;
     public final BiotopeCreator biotopeCreator;
     private Biotope biotope;
     
     private JButton newBiotope;
     private JButton nextRound;
     private JSpinner numberOfRounds;
     private JButton severalRounds;
     private JButton unlimitedRounds;
     private JButton stopRounds;
     private JCheckBox toggleLogging;
     private JCheckBox toggleDisplayRefresh;
     private JSpinner timerInterval;
 
     private ButtonGroup behaviorButtons;
     private JComponent behaviorButtonsBox;
     private JLabel behaviorButtonsLabel;
     private JLabel manipulationRadiusLabel;
     private JLabel manipulationRadiusDisplay;
     private JSlider manipulationRadius;
     private Behavior selectedBehavior;
     private final static int insertFinchButtonMask = InputEvent.BUTTON1_DOWN_MASK;
     private final static int removeFinchButtonMask = InputEvent.BUTTON3_DOWN_MASK;
 
     private boolean isLogging;
     private boolean isRefreshing;
     
     private static final Dimension minimumButtonDimension = new Dimension(0,30);
     private static final Dimension standardSpinnerSize = new Dimension(100,22);
     
     private List<Behavior> behaviors;
 
     /**
      * Create a GalapagosFrame simulating finches with the provided
      * behaviors and using the provided colors to visually represent
      * the simulation state.
      *
      * @param behaviors A mapping from behavior objects to colors. The
      * behavior objects specify which behaviors should be available
      * for use in the simulation (the user may choose to disable some
      * of them, so they are not guaranteed to participate in the run),
      * and the associated color will be used to draw a visual
      * representation of finches with that behavior.
      *
      * @require For every two distrinct behavior objects b1, b2 in
      * behaviors, b1.toString() != b2.toString() must hold.
      */
     
     public GalapagosFrame(Map<Behavior, Color> behaviors) {
         makeBehaviorListAndColorMap(behaviors);
         
         isRefreshing = true;
         isLogging = false;
         
         area = new AreaPanel(this.colorMap);
         // This class is anonymous because it is not generally usable,
         // but highly specialized.
         MouseInputAdapter listener = new MouseInputAdapter () {
                 /**
                  * Return true if the button for inserting finches is
                  * down in the provided InputEvent.
                  */
                 public boolean insertFinchDown(MouseEvent e) {
                     return (e.getModifiersEx() & insertFinchButtonMask) == 
                         insertFinchButtonMask;
                 }
                 
                 /**
                  * Return true if the button for removing finches is
                  * down in the provided InputEvent.
                  */
                 public boolean removeFinchDown(MouseEvent e) {
                     return (e.getModifiersEx() & removeFinchButtonMask) == 
                         removeFinchButtonMask;
                 }
                 
                 public void maybeDoFinchAction(MouseEvent e) {
                     int x = (e.getX() - area.offsetX()) / area.pixelSize();
                     int y = (e.getY() - area.offsetY()) / area.pixelSize();
 
                     // Only act if x,y is within the bounds of the
                     // world.
                     if (0 <= x && x < biotope.world.width() &&
                         0 <= y && y < biotope.world.height()) {
                         if (insertFinchDown(e)) {
                             if (selectedBehavior != null)
                                 controller.putFinches(x, y, selectedBehavior);
                         }
                         else if (removeFinchDown(e))
                             controller.takeFinches(x, y);
                     }
                 }
                 
                 public void mousePressed(MouseEvent e) {
                     maybeDoFinchAction(e);
                 }
                 public void mouseDragged(MouseEvent e) {
                     maybeDoFinchAction(e);
                 }
             };
         area.addMouseListener(listener);
         area.addMouseMotionListener(listener);
         statistics = new NicerStatisticsPanel(colorMap);
         logger = new BiotopeLogger();
         controller = new BiotopeController(biotope);
 
         setLayout(new BorderLayout());
         doLayout();
         addWindowListener(new Terminator());
         setTitle("Galapagos Finch Simulator");
         
         initializeControls();
         
         // Create a new Biotope with the default-settings of the BiotopeCreator. 
         biotopeCreator = new BiotopeCreator(this.behaviors);
         setBiotope(biotopeCreator.createBiotope());
         
         Dimension size = combinedSize();
         pack();
         setSize(size);
         validate();
     }
     
     public void setBiotope(Biotope biotope) {
     	this.biotope = biotope;
     	
         // Create RadioButtons and other widgets on the GalapagosFrame
         // for the spawning-tool
         behaviorButtons = new ButtonGroup();
         behaviorButtonsBox.removeAll();
         behaviorButtonsBox.add(Box.createGlue());
         behaviorButtonsBox.add(behaviorButtonsLabel);
 
         for (final Behavior b : biotope.behaviors()) {
             JRadioButton button = new JRadioButton(b.toString());
             button.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         selectedBehavior = b;
                     }
                 });
             button.setToolTipText(b.description());
             behaviorButtons.add(button);
             behaviorButtonsBox.add(button);
         }
 
         behaviorButtonsBox.add(manipulationRadiusLabel);
         behaviorButtonsBox.add(manipulationRadius);
         behaviorButtonsBox.add(manipulationRadiusDisplay);
         behaviorButtonsBox.add(Box.createGlue());
 
         selectedBehavior = null;
         controller.setBiotope(biotope);
         area.reset(biotope.world.width(), biotope.world.height());
         
         biotope.addObserver(statistics);
         if (isLogging)
             biotope.addObserver(logger);
         
         if (isRefreshing)
             biotope.addObserver(area);
         else
         	//redraw the initial stage of the Biotope even
             // when refreshing is disabled.
         	area.drawBiotope(biotope);
         
         
         biotope.doNotifyObservers();
     }
     
     /**
      * Create and position the controls of the main interface. 
      */
     private void initializeControls() {
         //create top controls
         newBiotope = new JButton("New Biotope");
         newBiotope.setToolTipText("Create a new biotope for simulation.");
         newBiotope.setMinimumSize(minimumButtonDimension);
         newBiotope.setActionCommand("newBiotope");
         newBiotope.addActionListener(new ActionListener () {
         	public void actionPerformed(ActionEvent e) {
         		//disable buttons
         		switchButtonsState(false);
         		controller.stopSimulation();
         		
         		// Showing the biotopeCreator will stop the current execution until it is closed
         		// this is because the biotopeCreator is a modal dialog. 
                 biotopeCreator.setVisible(true);
                 
                 //the biotopeCreator is closed we can now read the new biotope.
                 Biotope biotope = biotopeCreator.biotope();
                 
                 //change the Biotope if it was changed
                 if(biotope != GalapagosFrame.this.biotope && biotope != null)           	
                 	setBiotope(biotope);
 
                 //enable buttons
                 switchButtonsState(true);
             }
         });
         
         nextRound = newButton("Next Round", "nextRound", "Run a single round of simulation.");
         severalRounds = newButton("Compute Several Rounds", "severalRounds", "Run the specified number of rounds.");
         unlimitedRounds = newButton("Go!", "unlimitedRounds", "Run an unlimited number of rounds.");
         stopRounds = newButton("Stop Simulation", "stopRounds", "Stop currently running series of simulation rounds.");
         
         numberOfRounds = new JSpinner(new RevisedSpinnerNumberModel(0,0,Integer.MAX_VALUE,10));
         numberOfRounds.setToolTipText("Set number of simulation rounds to run");
         numberOfRounds.setName("numberOfRoundsSpinner");
         numberOfRounds.setPreferredSize(standardSpinnerSize);
         numberOfRounds.setMaximumSize(new Dimension(100,30));
         numberOfRounds.setMinimumSize(minimumButtonDimension);
         numberOfRounds.addChangeListener(controller);
         
         // The initial value needs to be set seperatly to notify the controller of the value.
         numberOfRounds.setValue(100);
         
         toggleLogging = new JCheckBox("Perform logging", isLogging);
         toggleLogging.setToolTipText("Enable/disable console logging");
         toggleLogging.addActionListener(new ActionListener () {
                 public void actionPerformed(ActionEvent e) {
                     if (isLogging)
                         biotope.deleteObserver(logger);
                     else
                         biotope.addObserver(logger);
                     isLogging = !isLogging;
                 }
             });
 
         toggleDisplayRefresh = new JCheckBox("Update display", isRefreshing);
         toggleDisplayRefresh.setToolTipText("Enable/disable updating of the biotope display area.");
         toggleDisplayRefresh.addActionListener(new ActionListener () {
                 public void actionPerformed(ActionEvent e) {
                     if (isRefreshing)
                         biotope.deleteObserver(GalapagosFrame.this.area);
                     else
                         biotope.addObserver(GalapagosFrame.this.area);
                     isRefreshing = !isRefreshing;
                 }
             });
         
         timerInterval = new JSpinner(new RevisedSpinnerNumberModel(0,0,Integer.MAX_VALUE,100));
         timerInterval.setToolTipText("Set the minimum number of milliseconds between simulation rounds.");
         timerInterval.setName("timerIntervalSpinner");
         timerInterval.setPreferredSize(standardSpinnerSize);
         timerInterval.setMaximumSize(new Dimension(100,30));
         timerInterval.setMinimumSize(minimumButtonDimension);
         timerInterval.addChangeListener(controller);
         
         // The initial value needs to be set seperatly to notify the controller of the value.
         timerInterval.setValue(100); 
         
         Container topContainer = Box.createHorizontalBox();
         topContainer.add(Box.createGlue());
         topContainer.add(newBiotope);
         topContainer.add(nextRound);
         topContainer.add(numberOfRounds);
         topContainer.add(severalRounds);
         topContainer.add(unlimitedRounds);
         topContainer.add(stopRounds);
         topContainer.add(Box.createGlue());
         
 
         JPanel leftContainer = new JPanel(new GridBagLayout());
         leftContainer.add(toggleLogging, getComponentConstraints(0,0, GridBagConstraints.CENTER));
         leftContainer.add(toggleDisplayRefresh, getComponentConstraints(0,1, GridBagConstraints.CENTER));
         leftContainer.add(new JLabel("Milliseconds between rounds"), getComponentConstraints(0,2, GridBagConstraints.CENTER));
         leftContainer.add(timerInterval,getComponentConstraints(0,3, GridBagConstraints.CENTER));
         leftContainer.setMaximumSize(leftContainer.getPreferredSize());
         Container outerLeftContainer = Box.createVerticalBox();
         outerLeftContainer.add(Box.createGlue());
         outerLeftContainer.add(leftContainer);
         outerLeftContainer.add(Box.createGlue());
 
         behaviorButtonsBox = Box.createVerticalBox();
         behaviorButtonsLabel = new JLabel(
                 "<HTML><B>Pencil for freehand finch drawing:</B><br>" +
                 "<I>Use the left mouse button to draw finches.<br>" +
                 "Use the right mouse button to delete finches.</I></HTML>");
         
         behaviorButtonsLabel.setFont(new Font("Dialog",Font.PLAIN,13));

        behaviorButtonsLabel = new JLabel("Pencil for freehand finch drawing");
         manipulationRadiusLabel = new JLabel("Radius of pencil");
         manipulationRadius = new JSlider(1, 25, 1);
         manipulationRadius.setToolTipText("Set the radius of the biotope manipulation pencil");
         manipulationRadius.setName("manipulationRadiusSlider");
         manipulationRadiusDisplay = new JLabel(((Integer)manipulationRadius.getValue()).toString());
         manipulationRadius.addChangeListener(new ChangeListener() {
                 public void stateChanged (ChangeEvent e) {
                     int newValue = manipulationRadius.getValue();
                     controller.setManipulationRadius(newValue);
                     manipulationRadiusDisplay.setText(((Integer)newValue).toString());
                 }
             });
 

         add(topContainer, BorderLayout.NORTH);
         add(area,BorderLayout.CENTER);
         add(statistics, BorderLayout.SOUTH);
         add(outerLeftContainer, BorderLayout.WEST);
         add(behaviorButtonsBox, BorderLayout.EAST);
     }
     
     /**
      * Create a new JButton with the specified text and actionCommand.
      * @param text The button's text
      * @param command The button's actionCommand
      * @param toolTip A tool tip for the button.
      * @return The new JButton
      */
     public JButton newButton(String text, String command, String toolTip) {
         JButton button = new JButton(text);
         button.setToolTipText(toolTip);
         button.setActionCommand(command);
         button.addActionListener(controller);
         button.setMinimumSize(minimumButtonDimension);
         
         return button;
     }
 
     /**
      * A set of GridBagConstraints for use with the GridBagLayout. Recommended for single components.
      * @param x the horisontal position of the component.
      * @param y the vertical position of the component.
      * @param alignment the alignment of the component in its display area.
      */            
     private GridBagConstraints getComponentConstraints(int x, int y, int alignment) {
         return new GridBagConstraints(x, y, 1, 1, 1.0, 1.0, 
                                         alignment,
                                         GridBagConstraints.NONE,
                                         new Insets(5,5,5,5),
                                         0, 0);
     }
 
     /**
      * Maps each color in the behaviors-map to the associated behaviors name (Behavior.toString()).
      * 
      * @require For every two distrinct behavior objects b1, b2 in
      * behaviors, b1.toString() != b2.toString() must hold.
      * 
      * @ensure this.colorByBehavior(b).equals(behaviors.get(b)) for all Behavior objects b in behaviors.
      */
     private void makeBehaviorListAndColorMap(Map<Behavior, Color> behaviors) {
         this.behaviors = new ArrayList<Behavior>();
         colorMap = new HashMap<Behavior, Color>();
         
         // Go through all the behaviors in the behaviors-map and add
         // its string representation to new map.
         for (Map.Entry<Behavior, Color> entry : behaviors.entrySet()) {
             Behavior currentBehavior = entry.getKey();
             assert !colorMap.containsKey(currentBehavior) : "Duplicate behaviors in Behavior-Color Map";
             this.behaviors.add(currentBehavior);
             colorMap.put(currentBehavior, entry.getValue());
         }
     }
     
     /**
      * Enables or disables buttons on the frame.
      * @param enable Indicates wheter the buttons be enabled or disabled (true for enabling).
      */
     public void switchButtonsState(boolean enable) {
         newBiotope.setEnabled(enable);
         nextRound.setEnabled(enable);
         numberOfRounds.setEnabled(enable);
         severalRounds.setEnabled(enable);
         unlimitedRounds.setEnabled(enable);
         stopRounds.setEnabled(enable);
     }
     
     /**
      * Get the combined size of all components preferred sizes plus 100
      * extra pixels in width and height.
      */
     public Dimension combinedSize() {
         BorderLayout layout = (BorderLayout) this.getContentPane().getLayout();
         Dimension centerDim = layout.getLayoutComponent(BorderLayout.CENTER).getPreferredSize();
         Dimension topDim = layout.getLayoutComponent(BorderLayout.NORTH).getPreferredSize();
         Dimension leftDim = layout.getLayoutComponent(BorderLayout.WEST).getPreferredSize();
         Dimension rightDim = layout.getLayoutComponent(BorderLayout.EAST).getPreferredSize();
         Dimension bottomDim = layout.getLayoutComponent(BorderLayout.SOUTH).getPreferredSize();
         
         /* Remark: With a BorderLayout the top and bottom areas cover the entire width of
          * of the frame, while the right and left areas only cover the part of the 
          * right and left frame borders not covered by the top and bottom areas.
          * This is reflected in a structural difference in the computation of the
          * combined Dimension of the five areas of the BorderLayout.
          */  
         int width = 100 + Math.max(leftDim.width + centerDim.width + rightDim.width, Math.max(topDim.width, bottomDim.width));
         int height = 100 + topDim.height + bottomDim.height + Math.max(centerDim.height, Math.max(leftDim.height, rightDim.height));
         return new Dimension(width, height);
     }
     
     /**
      * Get the BiotopeController used in this GalapagosFrame.
      */
     public BiotopeController controller() {
         return controller;
     }
 
     /**
      * Get the Biotope simulated in this GalapagosFrame.
      */
     public Biotope biotope() {
         return biotope;
     }
 }
