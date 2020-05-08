 import javax.swing.*;
 import javax.swing.table.*;
 
 import java.awt.*;
 
 /**
  * Month view for the calendar
  * 
  * @author gordon
 * 
  */
 public class MonthView extends BaseView {
 
     private static final long serialVersionUID = 1L;
 
     public MonthView() {
         super();
     }
 
     protected void setupCalendar() {
         TableModel monthView = new MonthDataModel(6, 30);
         JTable month = new JTable(monthView);
         month.getTableHeader().setReorderingAllowed(false);
         JScrollPane scrollPane = new JScrollPane(month);
         month.setRowHeight(100);
         month.setRowSelectionAllowed(false);
         this.add(scrollPane, BorderLayout.CENTER);
     }
 }
