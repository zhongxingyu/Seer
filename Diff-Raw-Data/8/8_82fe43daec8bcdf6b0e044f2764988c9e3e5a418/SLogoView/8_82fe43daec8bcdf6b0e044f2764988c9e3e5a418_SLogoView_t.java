 package view;
 
 import control.Controller;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.border.Border;
 
 
 /**
  * The View is comprised of everything visible to the user.
  * The View is the interactive space containing a file menu, buttons, and the Canvas.
  * 
  * @author srwareham, yoshi
  * 
  */
 public class SLogoView extends View {
     /**
      * Default magnitude for moving the turtle using the forward and backward buttons.
      */
     private static final int DEFAULT_FD_MAG = 10;
     /**
      * Default magnitude for moving the turtle using the forward button.
      */
     private static final int DEFAULT_TURN_MAG = 15;
     /**
      * Value for the borders of the JPanels.
      */
     private static final int BORDER_OFFSET = 5;
     /**
      * Forward button label text.
      */
     private static final String FORWARD_LABEL = "ForwardButton";
     /**
      * Backward button label text.
      */
     private static final String BACKWARD_LABEL = "BackwardButton";
     /**
      * Submit button label text.
      */
     private static final String SUBMIT_LABEL = "SubmitButton";
     /**
      * Right button label text.
      */
     private static final String RIGHT_LABEL = "RightButton";
     /**
      * Left button label text.
      */
     private static final String LEFT_LABEL = "LeftButton";
     /**
      * Forward command sent to the controller.
      */
     private static final String FD_COMMAND = "fd ";
     /**
      * Left command sent to the controller.
      */
     private static final String LEFT_COMMAND = "left ";
     /**
      * Right command sent to the controller.
      */
     private static final String RIGHT_COMMAND = "right ";
     /**
      * Initial size of the console area.
      */
     private static final Dimension PREFERRED_CONSOLE_SIZE = new Dimension(350, 100);
     /**
      * Initial size of the history area.
      */
     private static final Dimension PREFERRED_HISTORY_SIZE = new Dimension(350, 180);
     /**
      * Serial value.
      */
     private static final long serialVersionUID = 1L;
     /**
      * Name of the Canvas panel area.
      */
     private static final String CANVAS_NAME = "Canvas";
     /**
      * Name of the Workspace panel area.
      */
     private static final String WORKSPACE_NAME = "Workspace";
     /**
      * Name of the History panel area.
      */
     private static final String HISTORY_NAME = "History";
     /**
      * Name of the Input panel area.
      */
     private static final String INPUT_NAME = "Input";
     /**
      * ToolTip displayed when mouse is over the Workspace tab.
      */
     private static final String SLOGO_NAME = "SLogo";
     /**
      * Default location of the image resources.
      */
     private static final String RESOURCE_LOCATION = "/resources/images/";
     /**
      * Location of the turtle image.
      */
     private static final String TURTLE_IMG_FILENAME = "turtle_art.png";
     /**
      * Text area where the user can type, edit, copy and paste the commands.
      */
     private JTextArea myConsole;
     /**
      * Text area that displays the commands submitted by the user and
      * the controller messages
      */
     private JTextArea myHistory;
     
     /*
      * TODO: Implement CLEAR?
      * TODO: Implement the NEW workspace command
      */
     
     /**
      * Creates an instance of the View with personalized layout.
      * 
      * @param title The title of this View
      * @param language The desired language for the View
      */
     public SLogoView (String title, String language) {
         super(title, language);
         getContentPane().add(makeMainPanel(), BorderLayout.CENTER);
         pack();
         setVisible(true);
     }
     
     /**
      * Displays the text in the History panel and appends any text to it
      * 
      * @param text The string to be appended to the History.
      */
     @Override
     public void displayText (String text) {
         String s;
         if (text.length() > 0) {
             s = (myHistory.getText().length() == 0) ? text : "\n" + text;
             myHistory.append(s);
         }
     }
     /**
      * Creates the main panel with a Canvas, a History and an Input panels.
      * 
      * @return The main panel with a workspace.
      */
     private JTabbedPane makeMainPanel () {
         JTabbedPane workspace = new JTabbedPane();
         JPanel contentPanel = new JPanel();
         workspace.addTab(getName(WORKSPACE_NAME), null, contentPanel, SLOGO_NAME);
         contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
         contentPanel.setBorder(BorderFactory.createEmptyBorder(BORDER_OFFSET,
                                                                BORDER_OFFSET, BORDER_OFFSET,
                                                                BORDER_OFFSET));
         contentPanel.add(makeCanvasPanel());
         contentPanel.add(makeHistAndInputPanel());
         return workspace;
         
     }
     /**
      * Creates a panel with the Canvas.
      * 
      * @return Panel with Canvas.
      */
     @Override
     protected JComponent makeCanvasPanel () {
         JPanel canvasPanel = new JPanel();
         canvasPanel.add(super.getCanvas());
         canvasPanel.setBorder(makeBorder(CANVAS_NAME));
         return canvasPanel;
     }
     /**
      * Creates a panel with the History and Input Panels in a vertical box.
      * 
      * @return Panel with the History and Input.
      */
     private JComponent makeHistAndInputPanel () {
         JPanel hstInpPanel = new JPanel();
         hstInpPanel.setLayout(new BoxLayout(hstInpPanel, BoxLayout.PAGE_AXIS));
         hstInpPanel.add(makeInput());
         hstInpPanel.add(makeHistoryPane());
         return hstInpPanel;
     }
     /**
      * Creates a panel with the History, where the commands and results from the user are recorded.
      * 
      * @return Panel with History.
      */
     private JPanel makeHistoryPane () {
         JPanel histPane = new JPanel();
         JTextArea textArea = new JTextArea();
         myHistory = textArea;
         textArea.setEditable(false);
         JScrollPane scrollPane = new JScrollPane(textArea);
         
         scrollPane.setPreferredSize(PREFERRED_HISTORY_SIZE);
         histPane.setLayout(new BoxLayout(histPane, BoxLayout.PAGE_AXIS));
         histPane.add(scrollPane);
         histPane.setBorder(makeBorder(HISTORY_NAME));
         return histPane;
     }
     
     /**
      * Creates the input panel with buttons for the user to interact with the turtle
      * and the console in which the user writes the SLogo code.
      * 
      * @return Input panel.
      */
     @Override
     protected JComponent makeInput () {
         JPanel result = new JPanel();
         result.setLayout(new BorderLayout());
         result.setBorder(makeBorder(INPUT_NAME));
         result.add(makeTurtleMoveButtons(), BorderLayout.NORTH);
         result.add(makeCommandConsole(), BorderLayout.CENTER);
         result.add(makeSubmitButton(), BorderLayout.SOUTH);
         return result;
     }
     /**
      * Creates the buttons for moving the turtle.
      * The layout is not very nice, right now and are going to be improved in the following version.
      * 
      * @return Pane with the buttons.
      */
     private JComponent makeTurtleMoveButtons () {
         JPanel turtleMovePane = new JPanel();
         ImageIcon icon = 
                 new ImageIcon(getClass().getResource(RESOURCE_LOCATION + TURTLE_IMG_FILENAME));
         JLabel label = new JLabel(icon);
         turtleMovePane.setLayout(new BorderLayout());
         turtleMovePane.add(makeForwardButton(), BorderLayout.NORTH);
         turtleMovePane.add(makeLeftButton(), BorderLayout.LINE_START);
         turtleMovePane.add(makeRightButton(), BorderLayout.LINE_END);
         turtleMovePane.add(makeBackwardButton(), BorderLayout.SOUTH);
         turtleMovePane.add(label, BorderLayout.CENTER);
         
         return turtleMovePane;
     }
     /**
      * Creates a button that turns the turtle left.
      * 
      * @return turn right button
      */
     private JButton makeLeftButton () {
         final String COMMAND = LEFT_COMMAND + DEFAULT_TURN_MAG;
         return makeTurtleMoveButton(getName(LEFT_LABEL), COMMAND);
     }
     /**
      * Creates a button that turns the turtle right.
      * 
      * @return turn right button
      */
     private JButton makeRightButton () {
         final String COMMAND = RIGHT_COMMAND + DEFAULT_TURN_MAG;
         return makeTurtleMoveButton(getName(RIGHT_LABEL), COMMAND);
     }
     /**
      * Creates a button that moves the turtle forward.
      * 
      * @return move forward button
      */
     private JButton makeForwardButton () {
         final String COMMAND = FD_COMMAND + DEFAULT_FD_MAG;
         return makeTurtleMoveButton(getName(FORWARD_LABEL), COMMAND);
     }
     /**
      * Creates a button that moves the turtle backward.
      * 
      * @return move backward button
      */
     private JButton makeBackwardButton () {
         final String COMMAND = FD_COMMAND + -DEFAULT_FD_MAG;
         return makeTurtleMoveButton(getName(BACKWARD_LABEL), COMMAND);
     }
     /**
      * Creates a button that moves the turtle forward.
      * 
      * @return pane with the console
      */
     private JScrollPane makeCommandConsole () {
         JTextArea textArea = new JTextArea();
         myConsole = textArea;
         JScrollPane pane = new JScrollPane(myConsole);
         pane.setPreferredSize(PREFERRED_CONSOLE_SIZE);
         return pane;
     }    
     /**
      * Creates a button that submits the text in the console to the controller.
      * The code for this button is very similar to the code of the makeTurtleMoveButton method
      * because of the distinct action listener implementation.
      * 
      * @return submit button
      */
     private JButton makeSubmitButton () {
         JButton button = new JButton(getName(SUBMIT_LABEL));
         final Controller CONTROLLER = super.getController();
         button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed (ActionEvent e) {
                 String command = myConsole.getText();
                 CONTROLLER.createRunInstruction(command);
                 myConsole.setText("");
             }
         });
         return button;
     }    
     /**
      * Creates a default border for most of the panels.
      * 
      * @return border for panels
      */
     private Border makeBorder (String panelName) {
         Border border;
         border = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
                        getName(panelName)),
                        BorderFactory.createEmptyBorder(BORDER_OFFSET, BORDER_OFFSET, 
                                                        BORDER_OFFSET, BORDER_OFFSET));
         
         return border;
     }
     
     /**
      * Creates a button that takes in a final command.
      * Used to create the turtle movement buttons.
      * 
      * @return button
      */
     private JButton makeTurtleMoveButton (String name, final String command) {
         JButton button = new JButton(name);
         final Controller CONTROLLER = super.getController();
         button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed (ActionEvent e) {
                 CONTROLLER.createRunInstruction(command);
             }
         });
         return button;
     }
     /**
     * Returns a string with the text from the language resource file.
     * 
     * @param name of the element
     * @return name in the desired language specified in Main.
      */
    private String getName(String name) {
         return super.getResources().getString(name);
     }
 }
