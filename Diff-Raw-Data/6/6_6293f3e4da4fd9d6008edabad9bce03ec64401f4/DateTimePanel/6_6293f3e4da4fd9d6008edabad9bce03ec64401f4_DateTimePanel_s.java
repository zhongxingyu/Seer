 package views.shared;
 
 import com.toedter.calendar.JDateChooser;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.Date;
 
 /**
  * Created: 13-12-2012
  * @version: 0.1
  * Filename: DateTimePanel.java
  * Description:
  * @changes
  */
 
 public class DateTimePanel
 {
     public DateTimePanel()
     {
     }
 
     private static JSpinner _timeSpinner;
     private static JDateChooser _dateChooser;
 
     public JSpinner getTimeSpinner()
     { return _timeSpinner; }
 
     public JDateChooser getDateChooser()
     { return _dateChooser; }
 
     public static JPanel buildDateTimePanel(Date value)
     {
         JPanel datePanel = new JPanel();

         _dateChooser = new JDateChooser();
         if (value != null)
             _dateChooser.setDate(value);
 
         for (Component comp : _dateChooser.getComponents())
         {
             if (comp instanceof JTextField)
             {
                ((JTextField) comp).setColumns(70);
                 ((JTextField) comp).setEditable(false);
             }
         }
 
         datePanel.add(_dateChooser);
 
         SpinnerModel model = new SpinnerDateModel();
         _timeSpinner = new JSpinner(model);
         JComponent editor = new JSpinner.DateEditor(_timeSpinner, "HH:mm");
         _timeSpinner.setEditor(editor);
 
         if(value != null)
             _timeSpinner.setValue(value);
 
         datePanel.add(_timeSpinner);
 
         return datePanel;
     }
 }
