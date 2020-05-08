 package de.aidger.view;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.text.MessageFormat;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.InputMap;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.WindowConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.AboutAction;
 import de.aidger.controller.actions.ExitAction;
 import de.aidger.controller.actions.HelpAction;
 import de.aidger.controller.actions.PrintAction;
 import de.aidger.controller.actions.SettingsAction;
 import de.aidger.controller.actions.TaskPaneAction;
 import de.aidger.controller.actions.TaskPaneAction.Task;
 import de.aidger.utils.Logger;
 import de.aidger.view.tabs.EmptyTab;
 import de.aidger.view.tabs.Tab;
 import de.aidger.view.tabs.WelcomeTab;
 
 /**
  * The UI manages the main window and all its tabs. The main window consists of
  * the menu bar, the navigation and a tabbed pane which holds all tabs in the
  * center of the main window.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public final class UI extends JFrame {
     /**
      * Holds an instance of this class.
      */
     private static UI instance = null;
 
     /**
      * The tabbed plane that will contain the tabs.
      */
     private JTabbedPane tabbedPane;
 
     /**
      * A change listener for the tabbed pane
      */
     private ChangeListener tabbedPaneListener;
 
     /**
      * Creates the main window of the application.
      */
     private UI() {
         super();
 
         // Create the menu bar
         setJMenuBar(createMainMenuBar());
 
         // Build the main panel
         Container contentPane = getContentPane();
         contentPane.setLayout(new BorderLayout());
 
         contentPane.add(createToolbar(), BorderLayout.PAGE_START);
         contentPane.add(createTaskPane(), BorderLayout.LINE_START);
         contentPane.add(createTabbedPane(), BorderLayout.CENTER);
         contentPane.add(createStatusPane(), BorderLayout.PAGE_END);
 
         pack();
 
         setTitle("aidGer");
 
         // Initialize window event handling
         setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent event) {
                 Action action;
                 try {
                     action = ActionRegistry.getInstance().get(
                             ExitAction.class.getName());
 
                     if (action != null) {
                         ActionEvent evt = new ActionEvent(event.getSource(),
                                 event.getID(), null);
 
                         action.actionPerformed(evt);
                     }
                 } catch (ActionNotFoundException e) {
                     displayError(e.getMessage());
                 }
             }
         });
 
         // shortcuts for better tab handling
         JComponent comp = (JComponent) getContentPane();
         ActionMap actionMap = comp.getActionMap();
         InputMap inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                 ActionEvent.CTRL_MASK), "addNewTab");
 
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                 ActionEvent.CTRL_MASK), "removeCurrentTab");
 
         actionMap.put("addNewTab", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 addNewTab(new EmptyTab());
             }
         });
 
         actionMap.put("removeCurrentTab", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 removeCurrentTab();
             }
         });
 
         // Add the welcome tab to the UI.
         addNewTab(new WelcomeTab());
     }
 
     /**
      * Provides access to an instance of this class.
      * 
      * @return instance of this UI
      */
     public synchronized static UI getInstance() {
         if (instance == null) {
             instance = new UI();
         }
 
         return instance;
     }
 
     /**
      * Display an error message in a dialog window.
      * 
      * @param error
      *            The error message to display
      */
     public static void displayError(String error) {
         JOptionPane.showMessageDialog(instance, error, _("Error"),
                 JOptionPane.ERROR_MESSAGE);
         Logger.error(error);
     }
 
     /**
      * Runs and displays the graphical user interface.
      */
     public void run() {
         setVisible(true);
 
         // start maximized
         setExtendedState(Frame.MAXIMIZED_BOTH);
     }
 
     /**
      * Adds a new tab at the given index to the tabbed plane.
      * 
      * @param tab
      *            The tab to be added.
      * @param index
      *            The position where the tab will be added
      */
     public void addNewTab(Tab tab, int index) {
         Logger.debug(MessageFormat.format(
                 _("Adding new tab \"{0}\" at position {1}"), new Object[] {
                         tab.getName(), index }));
 
         tabbedPane.removeChangeListener(tabbedPaneListener);
 
         tabbedPane.add(tab, index);
         tabbedPane.setTabComponentAt(index,
                 new CloseTabComponent(tab.getName()));
 
         tabbedPane.setSelectedIndex(index);
 
         tabbedPane.addChangeListener(tabbedPaneListener);
     }
 
     /**
      * Adds a new tab at the end of the tabbed pane.
      * 
      * @param tab
      *            The tab to be added
      */
     public void addNewTab(Tab tab) {
         addNewTab(tab, tabbedPane.getTabCount() - 1);
     }
 
     /**
      * Adds a new empty tab and focus it if "+" button was clicked
      */
     public void addNewTab() {
         int index = tabbedPane.getTabCount() - 1;
 
         if (tabbedPane.getSelectedIndex() == index) {
             addNewTab(new EmptyTab());
         }
     }
 
     /**
      * Removes the tab at given index.
      * 
      * @param index
      *            The index identifies the tab that will be removed
      */
     public void removeTabAt(int index) {
         Logger.debug(MessageFormat.format(_("Removing {0}. tab"),
                 new Object[] { index }));
 
         tabbedPane.removeChangeListener(tabbedPaneListener);
 
         tabbedPane.remove(index);
 
         if (tabbedPane.getTabCount() == 1) {
             Tab emptyTab = new EmptyTab();
             tabbedPane.add(emptyTab, 0);
             tabbedPane.setTabComponentAt(0, new CloseTabComponent(emptyTab
                     .getName()));
 
             tabbedPane.setSelectedIndex(0);
         } else if (index == tabbedPane.getTabCount() - 1
                 && tabbedPane.getSelectedIndex() == tabbedPane.getTabCount() - 1) {
             tabbedPane.setSelectedIndex(index - 1);
         }
 
         tabbedPane.addChangeListener(tabbedPaneListener);
     }
 
     /**
      * Removes the current tab.
      */
     public void removeCurrentTab() {
         removeTabAt(tabbedPane.getSelectedIndex());
     }
 
     /**
      * Replaces the current tab with the given one. The old tab will be removed.
      * 
      * @param tab
      *            the new current tab
      */
     public void replaceCurrentTab(Tab tab) {
         int index = tabbedPane.getSelectedIndex();
 
         addNewTab(tab, index);
         removeTabAt(index + 1);
     }
 
     /**
      * Get the currently selected tab.
      * 
      * @return The selected tab
      */
     public Tab getCurrentTab() {
         return (Tab) tabbedPane.getSelectedComponent();
     }
 
     /**
      * Sets the current tab on the tabbed plane to the one specified.
      * 
      * @param tab
      *            The tab to be set as current. The tab must exist already.
      */
     public void setCurrentTab(Tab tab) {
         int index = tabbedPane.indexOfComponent(tab);
 
         if (index != -1) {
             setCurrentTabAt(index);
         }
     }
 
     /**
      * Sets the current tab to the tab at given position.
      * 
      * @param index
      */
     public void setCurrentTabAt(int index) {
         Tab newTab = (Tab) tabbedPane.getComponentAt(index);
 
         Logger.debug(MessageFormat.format(_("Setting current tab to \"{0}\""),
                 new Object[] { newTab.getName() }));
 
         tabbedPane.setSelectedIndex(index);
     }
 
     /**
      * Sets the current to the previous tab.
      */
     public void setPreviousTab() {
         int prevIndex = tabbedPane.getSelectedIndex() - 1;
 
         if (prevIndex < 0) {
             prevIndex = tabbedPane.getTabCount() - 2;
         }
 
         setCurrentTabAt(prevIndex);
     }
 
     /**
      * Sets the current to the next tab.
      */
     public void setNextTab() {
         int nextIndex = tabbedPane.getSelectedIndex() + 1;
 
         if (nextIndex == tabbedPane.getTabCount() - 1) {
             nextIndex = 0;
         }
 
         setCurrentTabAt(nextIndex);
     }
 
     /**
      * Retrieves the tabbed pane.
      * 
      * @return the tabbed pane
      */
     public JTabbedPane getTabbedPane() {
         return tabbedPane;
     }
 
     /**
      * Sets up the main menu bar.
      */
     private JMenuBar createMainMenuBar() {
         try {
             JMenuBar menuBar = new JMenuBar();
             menuBar.add(createFileMenu());
             menuBar.add(createHelpMenu());
 
             return menuBar;
         } catch (ActionNotFoundException e) {
             displayError(e.getMessage());
             return null;
         }
     }
 
     /**
      * Sets up the file menu.
      * 
      * @throws ActionNotFoundException
      */
     private JMenu createFileMenu() throws ActionNotFoundException {
         JMenu fileMenu = new JMenu(_("File"));
 
         fileMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
                 PrintAction.class.getName())));
         fileMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
                 SettingsAction.class.getName())));
         fileMenu.addSeparator();
         fileMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
                 ExitAction.class.getName())));
 
         return fileMenu;
     }
 
     /**
      * Sets up the help menu.
      * 
      * @throws ActionNotFoundException
      */
     private JMenu createHelpMenu() throws ActionNotFoundException {
         JMenu helpMenu = new JMenu(_("Help"));
 
         helpMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
                 HelpAction.class.getName())));
         helpMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
                 AboutAction.class.getName())));
 
         return helpMenu;
     }
 
     /**
      * Sets up the toolbar.
      */
     private JToolBar createToolbar() {
         JToolBar toolBar = new JToolBar();
 
         // toolBar.add(ActionRegistry.getInstance().get(OpenAction.class.getName()));
 
         return toolBar;
     }
 
     /**
      * Creates a button for the task pane.
      * 
      * @param name
      *            the button name
      * @param tab
      *            the tab that will be opened on click
      * @return the task pane button
      */
     private TaskPaneButton createTaskPaneButton(String name, Task task) {
         return new TaskPaneButton(new TaskPaneAction(name, task));
     }
 
     /**
      * Sets up the task pane.
      */
     private JScrollPane createTaskPane() {
         TaskPaneContainer tpc = new TaskPaneContainer();
 
         TaskPane tpMasterData = new TaskPane(_("Master Data Management"));
 
         tpMasterData.add(createTaskPaneButton(_("Courses"), Task.ViewCourses));
         tpMasterData.add(createTaskPaneButton(_("Assistants"),
                 Task.ViewAssistants));
         tpMasterData.add(createTaskPaneButton(_("Financial Categories"),
                 Task.ViewFinancialCategories));
         tpMasterData.add(createTaskPaneButton(_("Hourly Wages"),
                 Task.ViewHourlyWages));
 
         TaskPane tpEmployments = new TaskPane(_("Employments"));
         tpEmployments.add(createTaskPaneButton(_("Create new employment"),
                 Task.Void));
         tpEmployments.add(createTaskPaneButton(_("Show all contracts"),
                 Task.Void));
         tpEmployments.add(new JTextField());
 
         TaskPane tpActivities = new TaskPane(_("Activities"));
         tpActivities.add(createTaskPaneButton(_("Create new activity"),
                 Task.Void));
         tpActivities.add(createTaskPaneButton(_("Export"), Task.Void));
         tpActivities.add(new JTextField());
 
         TaskPane tpReports = new TaskPane(_("Reports"));
 
         String[] reports = { _("Annual Balance"), _("Semester Balance"),
                 _("Partial Balance"), _("Activity Report"), _("Protocol") };
         tpReports.add(new JComboBox(reports));
 
         TaskPane tpControlling = new TaskPane(_("Controlling"));
         JPanel monthSelection = new JPanel();
         monthSelection.add(new JLabel(_("Choose a month") + ":"));
         monthSelection.add(new JComboBox(new String[] { _("") }));
         monthSelection.setOpaque(false);
         tpControlling.add(monthSelection);
 
         TaskPane tpBudgetCheck = new TaskPane(_("Budget Check"));
         JPanel budgetFilter = new JPanel();
         budgetFilter.add(new JLabel(_("Add Filter") + ":"));
         budgetFilter.add(new JComboBox(new String[] { _("") }));
         budgetFilter.setOpaque(false);
         tpBudgetCheck.add(budgetFilter);
 
         TaskPane tpQuickSettings = new TaskPane(_("Quick Settings"));
         tpQuickSettings.add(new JCheckBox(_("Remember my tabs")));
         tpQuickSettings.add(new JCheckBox(_("Open reports instantly")));
 
         tpc.addTask(tpMasterData);
         tpc.addTask(tpEmployments);
         tpc.addTask(tpActivities);
         tpc.addTask(tpReports);
         tpc.addTask(tpControlling);
         tpc.addTask(tpBudgetCheck);
         tpc.addTask(tpQuickSettings);
         tpc.addFiller();
 
         tpReports.setExpanded(false);
         tpControlling.setExpanded(false);
         tpBudgetCheck.setExpanded(false);
         tpQuickSettings.setExpanded(false);
 
         return new JScrollPane(tpc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     }
 
     /**
      * Sets up the tabbed pane in the middle.
      */
     private JTabbedPane createTabbedPane() {
         tabbedPane = new JTabbedPane();
 
         tabbedPaneListener = new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 addNewTab();
             }
         };
 
         tabbedPane.add(new JPanel(), "+");
         tabbedPane.setToolTipTextAt(0, _("Open a new tab"));
 
         tabbedPane.addChangeListener(tabbedPaneListener);
 
         tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
 
         return tabbedPane;
     }
 
     /**
      * Sets up the panel that contains the status label.
      */
     private JPanel createStatusPane() {
         JPanel statusPane = new JPanel();
         statusPane.setLayout(new FlowLayout(FlowLayout.LEFT));
 
         // statusPane.add(getStatusLabel());
 
         return statusPane;
     }
 
 }
