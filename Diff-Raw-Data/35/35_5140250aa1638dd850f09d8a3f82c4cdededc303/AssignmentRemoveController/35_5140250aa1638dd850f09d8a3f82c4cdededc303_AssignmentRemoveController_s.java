 import javax.swing.*;
 import java.awt.*;
 
 /** Removes the assignment selected within AssignmentTab from the specified
  * Course. Reads the name of the assignment to delete from the passed table.
  */
 public class AssignmentRemoveController implements ActionListener {
   private Course course;
   private JTable table;
   private AssignmentTableModel tableModel;
 
  /** Create this handler using the given Course and JTable
    * @param course    the Course model to delete an item from 
    * @param table     the JTable to read the selected value from 
    */
   public AssignmentRemoveController(Course course, JTable table, 
           AssignmentTableModel tableModel) {
     this.course = course;
     this.table = table;
     this.tableModel = tableModel;
   }
 
   /** Get the name of the currently selected assignment, and delete it from the 
    * Course.
    * @param e    the triggered event
    */
   public void actionPerformed(ActionEvent e) {
     /* getSelectedRow() only returns the selected row in terms of the view, 
      * while convertRowIndexToModel() provides the index of the selected item 
      * in terms of the table's underlying model. 
      */
     int selectedIndex = this.table.convertRowIndexToModel(
             this.table.getSelectedRow());
     String name = this.tableModel.getAssignmentName(selectedIndex);
     this.course.removeAssignment(name);
   }
 } 
