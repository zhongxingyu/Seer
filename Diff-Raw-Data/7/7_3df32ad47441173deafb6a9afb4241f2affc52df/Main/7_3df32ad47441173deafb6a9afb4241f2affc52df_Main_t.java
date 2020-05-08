 package ru.silvestrov.timetracker;
 
 import org.springframework.transaction.support.TransactionTemplate;
 import ru.silvestrov.timetracker.data.ActivityDao;
 import ru.silvestrov.timetracker.data.DataConfiguration;
 import ru.silvestrov.timetracker.data.TimeEntryDao;
 import ru.silvestrov.timetracker.model.ActivityControlList;
 import ru.silvestrov.timetracker.view.*;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * Created by Silvestrov Ilya
  * Date: Jul 9, 2008
  * Time: 10:58:13 PM
  */
 public class Main {
     public static void main(String[] args) throws InvocationTargetException, InterruptedException {
 //        ActivityDao activityDao = new MockActivityDao();
 //        TimeEntryDao timeEntryDao = new MockTimeEntryDao();
 //        TransactionTemplate tt = new TransactionTemplate() {
 //            public Object execute(TransactionCallback action) throws TransactionException {
 //                return action.doInTransaction(null);
 //            }
 //        };
 
         DataConfiguration dataConfiguration = new DataConfiguration("./timeDB");
         ActivityDao activityDao = dataConfiguration.getActivityDao();
         TimeEntryDao timeEntryDao = dataConfiguration.getTimeEntryDao();
         TransactionTemplate tt = dataConfiguration.getTransactionTemplate();
 
 
 
         final JFrame mainFrame = new JFrame();
         final ActivityControlList activityControlList = new ActivityControlList();
         activityControlList.setActivityDao(activityDao);
         activityControlList.setTimeEntryDao(timeEntryDao);
         activityControlList.setTransactionTemplate(tt);
 
         RenameActivityController renameActivityController = new RenameActivityController();
         renameActivityController.setActivityControlList(activityControlList);
         renameActivityController.setActivityDao(activityDao);
         renameActivityController.setTransactionTemplate(tt);
 
         ActivityListTableModel tableModel = new ActivityListTableModel(
             activityControlList, renameActivityController);
         activityControlList.setUpdateListener(tableModel.new UpdateListenerControl());
         activityControlList.afterPropertiesSet();
 
         final JTable table = new JTable(tableModel);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.getSelectionModel().addListSelectionListener(tableModel.new SelectionListener());
         tableModel.addTableModelListener(table);
         JTextField cellField = new JTextField();
         table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(cellField));
         cellField.addFocusListener(new FocusListener() {
             public void focusGained(FocusEvent e) {
                 table.changeSelection(0, 0, true, true);
             }
 
             public void focusLost(FocusEvent e) {
             }
         });
         ButtonCellHelper buttonCellHelper = new ButtonCellHelper("finish", new ButtonCellHelper.CellButtonListener() {
             public void cellButtonPressed(int row) {
                 activityControlList.finishActivity(row - 1);
             }
         });
         table.getColumnModel().getColumn(2).setCellRenderer(buttonCellHelper);
         table.getColumnModel().getColumn(2).setCellEditor(buttonCellHelper);
 
 
        Component timeRendererComponent = table.getDefaultRenderer(String.class).getTableCellRendererComponent(table, "00:00:00", true, true, 2, 1);
        table.getColumnModel().getColumn(1).setMaxWidth((int) timeRendererComponent.getPreferredSize().getWidth() + 10);
        Component finishRendererComponent = buttonCellHelper.getTableCellRendererComponent(table, "finish", true, true, 1, 2);
        table.getColumnModel().getColumn(2).setMaxWidth((int) finishRendererComponent.getPreferredSize().getWidth() + 10);

 
         final JButton addActivityButton = new JButton("Add activity");
         addActivityButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         JFrame frame = new JFrame("Add activity");
                         JPanel panel = new JPanel(new GridLayout());
                         JTextField text = new JTextField();
                         panel.add(new JLabel("Name"));
                         panel.add(text);
 
                         JButton addActivityButton = new JButton("Add activity");
                         AddActivityController addActivityController = new AddActivityController();
                         addActivityController.setActivityControlList(activityControlList);
                         addActivityController.setName(text);
                         addActivityController.setForm(frame);
                         addActivityButton.addActionListener(addActivityController);
                         panel.add(addActivityButton);
                         frame.add(panel);
                         frame.setVisible(true);
                     }
                 });
             }
         });
 
         JPanel panel = new JPanel(new BorderLayout());
         JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
         panel.add(topPanel, BorderLayout.NORTH);
         topPanel.add(addActivityButton);
         JPanel centerPanel = new JPanel(new GridLayout(0, 1, 0, 0));
         centerPanel.add(table);
         panel.add(new JScrollPane(centerPanel),BorderLayout.CENTER);
 
         mainFrame.add(panel);
         mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         final WindowSettingsSaver settingsSaver = new WindowSettingsSaver();
 
         Toolkit.getDefaultToolkit().addAWTEventListener(
             new AWTEventListener() {
                 public void eventDispatched(AWTEvent event) {
                     WindowEvent we = (WindowEvent) event;
                     switch (event.getID()) {
                         case WindowEvent.WINDOW_OPENED:
                             settingsSaver.windowOpened(we);
                             break;
                         case WindowEvent.WINDOW_CLOSING:
                             settingsSaver.windowClosing(we);
                     }
                 }
             }, AWTEvent.WINDOW_EVENT_MASK
         );
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 mainFrame.setVisible(true);
                 addActivityButton.setVisible(true);
             }
         });
 
     }
 }
