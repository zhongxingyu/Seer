 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 class ControllerTimetableViewScreen extends JFrame
         implements ActionListener {
 
     //Declare the components
     JLabel jLabelTimetable, jLabelSelectedView, jLabelContent;
     JPanel contentPanel, mainContentPanel, requestPanel;
     JScrollPane jScrollPanel;
     JMenuBar mainMenuBar;
     JMenu jMenuFile, jMenuView;
     JMenuItem jMItemSave, jMItemPrint, jMItemExit;
     JMenuItem jMItemTimetables, jMItemDrivers, jMItemReport, jMItemProblems;
     //Declate the colors
     Color layoutBgClr = new Color(255, 255, 255);
     Color lblFgClr = new Color(150, 150, 150);
     Color reqBgClr = new Color(140, 140, 140);
 
     public ControllerTimetableViewScreen(String paramString) {
 
         this.setTitle(paramString);
 
         //Create the main menu bar
         mainMenuBar = new JMenuBar();
         mainMenuBar.setBackground(this.layoutBgClr);
 
         //Create the two menus
         jMenuFile = new JMenu("File");
         jMenuFile.setMnemonic(KeyEvent.VK_F);
         jMenuView = new JMenu("View");
         jMenuView.setMnemonic(KeyEvent.VK_V);
 
         jLabelSelectedView = new JLabel("Selected View: Timetable");
         jLabelSelectedView.setForeground(lblFgClr);
 
         //Add the two menus to the menu bar
         mainMenuBar.add(jMenuFile);
         mainMenuBar.add(jMenuView);
         mainMenuBar.add(Box.createHorizontalGlue());
         mainMenuBar.add(jLabelSelectedView);
 
         //Create the menu items
         jMItemSave = new JMenuItem("Save");
         jMItemPrint = new JMenuItem("Print");
         jMItemExit = new JMenuItem("Exit", KeyEvent.VK_X);
         jMItemExit.setActionCommand("exit");
         jMItemExit.addActionListener(this);
 
 
         jMItemTimetables = new JMenuItem("Timetables", KeyEvent.VK_T);
         jMItemTimetables.setActionCommand("timetables");
         jMItemTimetables.addActionListener(this);
 
         jMItemReport = new JMenuItem("Report", KeyEvent.VK_R);
         jMItemReport.setActionCommand("report");
         jMItemReport.addActionListener(this);
 
         jMItemDrivers = new JMenuItem("Drivers", KeyEvent.VK_D);
         jMItemDrivers.setActionCommand("drivers");
         jMItemDrivers.addActionListener(this);
 
         jMItemProblems = new JMenuItem("Problems", KeyEvent.VK_P);
         jMItemProblems.setActionCommand("problems");
         jMItemProblems.addActionListener(this);
 
         //Add the items to the menus
         jMenuFile.add(this.jMItemSave);
         jMenuFile.add(this.jMItemPrint);
         jMenuFile.add(this.jMItemExit);
 
         jMenuView.add(this.jMItemTimetables);
         jMenuView.add(this.jMItemReport);
         jMenuView.add(this.jMItemDrivers);
         jMenuView.add(this.jMItemProblems);
 
         //Create the mainContent panel
         mainContentPanel = new JPanel();
         mainContentPanel.setLayout(new GridLayout(0, 3, 20, 20));
         mainContentPanel.setBackground(layoutBgClr);
 
         //Create the scrollpanel
         jScrollPanel = new JScrollPane(mainContentPanel);
         jScrollPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jScrollPanel.setBackground(layoutBgClr);
 
         //Create the content panel
         contentPanel = new JPanel();
         contentPanel.setPreferredSize(new Dimension(600, 300));
         contentPanel.setLayout(new BorderLayout());
         contentPanel.setBackground(layoutBgClr);
 
         //Create the label for the content panel
         jLabelContent = new JLabel("Timetable");
         jLabelContent.setForeground(lblFgClr);
         jLabelContent.setBackground(layoutBgClr);
         jLabelContent.setHorizontalAlignment(SwingConstants.LEFT);
 
         //Add the label to the Scroll panel
         mainContentPanel.add(jLabelContent);
 
         //Add the label to the content panel
         contentPanel.add(jScrollPanel, BorderLayout.CENTER);
 
         //Resize and position the window
         Dimension localDimension = Toolkit.getDefaultToolkit().getScreenSize();
 
         int i = getSize().width;
         int j = getSize().height;
         int k = (localDimension.width - i) / 2 - 300;
         int m = (localDimension.height - j) / 2 - 150;
 
         this.setResizable(false);
 
         //Locate the window
         this.setLocation(k, m);
 
         this.setJMenuBar(this.mainMenuBar);
         this.setContentPane(this.contentPanel);
         this.pack();
         this.setVisible(true);
 
         //Define the action on closing the window
         addWindowListener(new WindowAdapter() {
 
             public void windowClosing(WindowEvent paramAnonymousWindowEvent) {
 
                 System.exit(0);
             }//windowClosing
         });//addWindowListener
 
     }//constructor
 
     //Catch the click events
     public void actionPerformed(ActionEvent paramActionEvent) {
 
         String actionCmd = paramActionEvent.getActionCommand();
         String title = "G7 - IBMS System | Controller";
 
         if ("exit".equals(actionCmd)) {
             System.exit(0);
         } else if ("timetables".equals(actionCmd)) {
             this.dispose();
             new ControllerTimetableViewScreen(title);
        } else if ("report".equals(actionCmd)) {
             this.dispose();
            new ControllerReportViewScreen(title);
        }  else if ("drivers".equals(actionCmd)) {
             this.dispose();
             new ControllerDriversViewScreen(title);
         } else if ("problems".equals(actionCmd)) {
             this.dispose();
             new ControllerProblemsViewScreen(title);
         }
 
     }//actionPerformed
 }//class
 
