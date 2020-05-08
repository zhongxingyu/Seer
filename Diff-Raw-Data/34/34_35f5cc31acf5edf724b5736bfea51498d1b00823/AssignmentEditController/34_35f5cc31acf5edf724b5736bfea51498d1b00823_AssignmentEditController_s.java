 import javax.swing.*;
 import java.awt.*;
 
 /** Makes an AssignmentEditor appear for the currently selected row on the 
  * assignment table.
  */
 public class AssignmentEditController implements ActionListener {
   private Course course;
   private JTable table;
   private AssignmentTableModel tableModel;
 
  /** Create this handler with the given Course, table, and table model 
    * @param course    the Course model to use 
    * @param table     the table to get the selected row index from 
    * @param tableModel   the model to get the selected assignment's data from 
    */
   public AssignmentEditController(Course course, JTable table,
          AssignmentTableModel tableModel) {
     this.course = course;
     this.table = table;
     this.tableModel = tableModel;
   }
 
  /** Display a 
 } 
