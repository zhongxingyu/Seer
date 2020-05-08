 import javax.swing.*;
 import javax.swing.table.*;
 
 import java.awt.*;
 
 /**
  * Week view for the calendar
  * 
  * @author gordon
 *
  */
 public class WeekView extends BaseView {
 
     private static final long serialVersionUID = 1L;
 
     public WeekView() {
         super();
     }
 
     protected void setupCalendar() {
         TableModel weekView = new WeekDataModel();
         JTable week = new JTable(weekView);
         week.getTableHeader().setReorderingAllowed(false);
         JScrollPane scrollPane = new JScrollPane(week);
         week.setRowSelectionAllowed(false);
         week.setRowHeight(50);
         this.add(scrollPane, BorderLayout.CENTER);
     }
 }
