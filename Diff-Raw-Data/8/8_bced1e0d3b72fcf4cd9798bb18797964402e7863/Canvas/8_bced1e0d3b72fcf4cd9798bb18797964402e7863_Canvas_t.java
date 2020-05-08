 package frontEnd;
 
 import java.awt.Color;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ResourceBundle;
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import backEnd.Workspace;
 import controller.Controller;
 
 
 /**
  * Canvas Class extends JPanel. Contains three areas: CommandPrompt Panel, Command History TextArea,
  * and TurtleView Component. CommandPrompt Panel contains command TextArea.
  * 
  * 
  * @author Danny Goodman, David Le
  * 
  */
 
 @SuppressWarnings("serial")
 public class Canvas extends JPanel {
 
     // default serialization ID
     private static final long serialVersionUID = 1L;
     private static final String FRONTEND_RESOURCE = "resources.FrontEnd";
     private static final String HELP_PATH = "resources/help.html";
 
     private JFileChooser myChooser;
     private Controller myController;
     private JTabbedPane myWorkspaces;
     private ResourceBundle myResources;
 
     /**
      * Canvas object created in Main class. Takes in size of JPanel.
      * Initializes JComponents. Instantiates Workspace.
      * 
      * @param size
      */
     public Canvas (Dimension size) {
         // set size (a bit of a pain)
         setPreferredSize(size);
         setSize(size);
         // prepare to receive input
         setFocusable(true);
         requestFocus();
         myResources = ResourceBundle.getBundle(FRONTEND_RESOURCE);
         myWorkspaces = new JTabbedPane();
         add(myWorkspaces);
         int workspaceCount = myWorkspaces.getTabCount() + 1;
         WorkspaceView first = makeWorkspaceView();
         myWorkspaces.add(myResources.getString("WorkspaceTitle") + " " + workspaceCount, first);
         myController = new Controller(this);
         first.addToModel();
         // make file chooser
         myChooser = new JFileChooser(myResources.getString("UserDirectory"));
         // size and display the GUI
         setVisible(true);
         
     }
 
     /**
      * Returns the workspace which the user is currently working in
      * 
      * @return activeWorkspace
      */
     public WorkspaceView getWorkspaceView () {
         WorkspaceView activeWorkspace = (WorkspaceView) myWorkspaces.getSelectedComponent();
         return activeWorkspace;
     }
 
     /**
      * Returns the index of the workspace which the user is currently working
      * in
      * 
      * @return index of active workspace
      */
     public int getWorkspaceIndex () {
         return myWorkspaces.getSelectedIndex();
     }
 
     /**
      * Returns the controller
      * 
      * @return controller
      */
     public Controller getController () {
         return myController;
     }
 
     public void updateWorkspace (Workspace changedWorkspace) {
         int index = changedWorkspace.getIndex();
         WorkspaceView current = (WorkspaceView) myWorkspaces.getComponent(index);
         current.updateWorkspace(changedWorkspace);
     }
 
     /**
      * Makes the JMenuBar for the user to interact with
      * 
      * @return menuBar
      */
 
     public void writeHistory (String text) {
         getWorkspaceView().writeHistory(text);
     }
 
     /**
      * Makes the MenuBar for the Canvas. Called by Main.
      * 
      * @return JMenuBar
      */
     public JMenuBar makeMenus () {
         JMenuBar menuBar = new JMenuBar();
         menuBar.add(makeFileMenu());
         menuBar.add(makeViewMenu());
         menuBar.add(makeHelpMenu());
         return menuBar;
     }
 
     /**
      * Displays an error message that the user must click to continue the program
      * 
      * @param text to be displayed
      */
     public void showErrorMsg (String text) {
         JOptionPane.showMessageDialog(this, text, myResources.getString("Error"),
                                       JOptionPane.ERROR_MESSAGE);
     }
 
     /**
      * Returns the myResources ResourceBundle
      * 
      * @return myResources
      */
     public ResourceBundle getResources () {
         return myResources;
     }
 
     private WorkspaceView makeWorkspaceView () {
         WorkspaceView workspaceView = new WorkspaceView(this);
         return workspaceView;
     }
 
     /**
      * Makes the "File" menu from which the user can make new workspaces, load commands,
      * or exit the program
      * 
      * @return fileMenu
      */
     private JMenu makeFileMenu () {
         JMenu fileMenu = new JMenu(myResources.getString("FileMenu"));
         fileMenu.add(new AbstractAction(myResources.getString("NewCommand")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 int workspaceNumber = myWorkspaces.getTabCount() + 1;
                 WorkspaceView newWorkspace = makeWorkspaceView();
                 myWorkspaces.add(myResources.getString("WorkspaceTitle") + " " + workspaceNumber,
                                  newWorkspace);
                 newWorkspace.addToModel();
             }
         });
         fileMenu.add(new AbstractAction(myResources.getString("OpenCommand")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 try {
                     int response = myChooser.showOpenDialog(null);
                     if (response == JFileChooser.APPROVE_OPTION) {
                         new FileReader(myChooser.getSelectedFile());
                     }
                 }
                 catch (IOException io) {
                     showErrorMsg(io.toString());
                 }
             }
         });
         fileMenu.add(new JSeparator());
         fileMenu.add(new AbstractAction(myResources.getString("ExitCommand")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 // clean up; exit.
                 System.exit(0);
             }
         });
         return fileMenu;
     }
 
     /**
      * Makes the "View" menu from which the user can toggle warping through
      * borders
      * 
      * @return viewMenu
      */
     private JMenu makeViewMenu () {
         JMenu viewMenu = new JMenu(myResources.getString("ViewMenu"));
         viewMenu.add(new AbstractAction(myResources.getString("WarpCommand")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 getWorkspaceView().getTurtleView().toggleWarp();
             }
         });
         viewMenu.add(new AbstractAction(myResources.getString("FillTurtle")) {
             @Override
             public void actionPerformed (ActionEvent arg0) {
                 getWorkspaceView().getTurtleView().toggleFill();
             }
         });
         viewMenu.add(new AbstractAction(myResources.getString("ToggleGrid")){
             @Override
             public void actionPerformed (ActionEvent arg0) {
                getWorkspaceView().getTurtleView().toggleGrid();
             } 
         });
         viewMenu.add(new AbstractAction(myResources.getString("SetBackground")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 String color =
                         JOptionPane.showInputDialog(null, myResources.getString("ColorPrompt"));
                 Color c;
                 try {
                     Field field = Color.class.getField(color);
                     c = (Color) field.get(null);
                 }
                 catch (Exception e1) {
                     showErrorMsg(myResources.getString("ColorError"));
                     c = null;
                 }
                 if (c != null) getWorkspaceView().getTurtleView().setBackgroundColor(c);
             }
         });
         return viewMenu;
     }
 
     /**
      * Makes the "Help" menu from which the user can access documentation
      * if they need help with anything.
      * 
      * @return helpMenu
      */
     private JMenu makeHelpMenu () {
         JMenu helpMenu = new JMenu(myResources.getString("HelpMenu"));
         helpMenu.add(new AbstractAction(myResources.getString("CommandsInfo")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                 URL url = classLoader.getResource(HELP_PATH);
                 File htmlFile;
                 try {
                     htmlFile = new File(url.toURI());
                     Desktop.getDesktop().open(htmlFile);
                 }
                 catch (URISyntaxException e1) {
                     showErrorMsg("FileNotFound");
                 }
                 catch (IOException e2) {
                     showErrorMsg("DesktopError");
                 }
             }
         });
         return helpMenu;
     }
 }
