 import javax.swing.*;
 import java.awt.*;
 
 /** Makes an AssignmentEditor appear for the currently selected row on the 
  * assignment table.
  */
 public class AssignmentEditController implements ActionListener {
   private Course course;
   private JTable table;
   private AssignmentTableModel tableModel;
  private JFrame mainWindow;
 
  /** Create this handler with the given Course, table, table model, and main 
   * window
    * @param course    the Course model to use 
    * @param table     the table to get the selected row index from 
    * @param tableModel   the model to get the selected assignment's data from 
   * @param mainWindow   the application's main window
    */
   public AssignmentEditController(Course course, JTable table,
          AssignmentTableModel tableModel, JFrame mainWindow) {
     this.course = course;
     this.table = table;
     this.tableModel = tableModel;
    this.mainWindow = mainWindow;
   }
 
  /** Display an AssignmentEditor when the button to edit an assignment is 
   * clicked 
   * @param e    the triggered event
   */
  public void actionPerformed(ActionEvent e) {
    // The index of the selected row, relative to the table's model
    int selectedIndex = this.table.convertRowIndexToModel(
            this.table.getSelectedRow());
    String assignmentName = this.tableModel.getAssignmentName(selectedIndex);
    AssignmentEditor editor = new AssignmentEditor(this.course, assignmentName,
            this.mainWindow);
  }
 } 
