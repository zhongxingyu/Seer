 /**
  * 
  */
 package optitask.ui;
 
 import java.awt.Image;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.table.AbstractTableModel;
 
 import optitask.AppController;
 import optitask.store.AppPersistence;
 import optitask.util.Task;
 
 /**
  * Displays a dialog to manage the task inventory.
  * @author Jerome Rodrigo
  * @since 0.9.0
  */
 public class TaskInventoryDialog extends AbstractTaskManager {
 
     /**
      * 
      */
     private static final long serialVersionUID = 9179795924304930629L;
 
     /**
      * The data model for the to do list table.
      * @author Jerome
      * @since 0.8
      * @see TasksDialog#tasksTable
      */
 
     private class TasksDataModel extends AbstractTableModel {
 
         /**
          * The serial version UID.
          */
         private static final long serialVersionUID = -2642740868251152662L;
 
         /**
          * The names for each column.
          */
         private final transient String[] columnNames = { "Task", "Description",
                 "Assigned", "Done" };
 
         /**
          * The limit where assigned pomodoros is considered as 'too many'.
          */
         private static final int TOO_MANY_POMS = 6;
         
         /**
          * The column number for Task Description.
          */
         private static final int DESC_COL = 1;
         
         /**
          * The column number for Assigned Pomodoros.
          */
         private static final int ASSPOM_COL = 2;
         
         /**
          * The column number for Task Done.
          */
         private static final int ISDONE_COL = 3;
 
         /**
          * The list of tasks.
          */
         private final transient List<Task> tasks;
         
         /**
          * Constructor for the TasksDataModel.
          * @param tsks the list of tasks
          */
         public TasksDataModel(final List<Task> tsks) {
             super();
             tasks = tsks;
         }
 
         @Override
         public int getColumnCount() {
             return columnNames.length;
         }
 
         @Override
         public String getColumnName(final int col) {
             return columnNames[col];
         }
 
         @Override
         public int getRowCount() {
             return tasks.size();
         }
 
         @Override
         public Class<?> getColumnClass(final int col) {
             return getValueAt(0, col).getClass();
         }
 
         @Override
         public boolean isCellEditable(final int row, final int col) {
             boolean editable;
             
             if (tasks.get(row).isDone() && (col == 2 
                     || col == 1)) {
                 editable = false;
             } else {
                 editable = col > 0 && col < columnNames.length;
             }            
             
             return editable;
         }
 
         @Override
         public Object getValueAt(final int row, final int col) {
             Object obj;
             
             switch (col) {
             case 0:
                 obj = row + 1;
                 break;
            case DESC_COL:
                 obj = tasks.get(row).getTaskDesc();
                 break;
             case ISDONE_COL:
                 obj = tasks.get(row).isDone();
                 break;
             case ASSPOM_COL:
                 obj = tasks.get(row).getAssignedPomodoros();
                 break;
             default:
                 obj = new Object();
             }
             
             return obj;
         }
 
         @Override
         public void setValueAt(final Object value,
                 final int row, final int col) {
             assert (col > 0 && col < columnNames.length);
             final Task task = tasks.get(row);
 
             switch (col) {
            case DESC_COL:
                 task.setTaskDesc((String) value);
                 break;
             case ISDONE_COL:
                 task.setTaskDone((Boolean) value);
 
                 // If a task is 'undone' then reset the current pomodoros
                 if (!(Boolean) value) {
                     task.setCurrentPomodoro(0);
                 }
 
                 break;
             case ASSPOM_COL:
                 task.setAssignedPomodoros((Integer) value);
 
                 if ((Integer) value > TOO_MANY_POMS) {
                     JOptionPane.showMessageDialog(getParent(),
                             "Too many pomodoros assigned.\n"
                                     + "Consider breaking down your tasks!",
                                     "Warning", JOptionPane.WARNING_MESSAGE);
                 }
 
                 break;
             default:
                 return;
             }
 
             tasks.set(row, task);
             fireTableCellUpdated(row, col);
         }
 
     };
 
     /**
      * The title of the window.
      */
     private static final String WINDOW_TITLE = "Task Inventory";
 
     /**
      * The column for the pomodoro editor.
      */
     private static final int POM_EDITOR_COLUMN = 2;
 
     /**
      * Constructs a TaskInventoryDialog.
      * @param mdl the persistence module
      * @param cntrller the application controller
      */
     
     public TaskInventoryDialog(final AppPersistence mdl,
             final AppController cntrller) {
         super(mdl, cntrller);
     }
 
     /* (non-Javadoc)
      * @see optitask.ui.TaskManager#getTableModel()
      */
     @Override
     protected final AbstractTableModel getTableModel() {
         return new TasksDataModel(getTasks());
     }
 
     /* (non-Javadoc)
      * @see optitask.ui.TaskManager#getWindowTitle()
      */
     @Override
     protected final String getWindowTitle() {
         return WINDOW_TITLE;
     }
 
     @Override
     protected final Image getIconImage() {
         return null;
     }
 
     @Override
     protected final String getMoveUpMessage() {
         return "Move Up Task Inventory";
     }
 
     @Override
     protected final String getMoveDownMessage() {
         return "Move Down Task Inventory";
     }
 
     @Override
     protected final String getAddMessage() {
         return "Add Task Task Inventory";
     }
 
     @Override
     protected final String getDeleteMessage() {
         return "Delete Task Task Inventory";
     }
 
     @Override
     protected final List<Task> getTasksModel() {
         return (List<Task>) getModel().getTaskInventory();
     }
 
     @Override
     protected final int getPomNumberEditorColumn() {
         return POM_EDITOR_COLUMN;
     }
 
     @Override
     protected final String getMoveToMessage() {
         return "Move To To Do List";
     }
 
 }
