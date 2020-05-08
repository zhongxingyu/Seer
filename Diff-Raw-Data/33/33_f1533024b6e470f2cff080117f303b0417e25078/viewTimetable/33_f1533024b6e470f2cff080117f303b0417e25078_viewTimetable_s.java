 import java.awt.*;
 import java.awt.Component;
 import javax.swing.table.*;
 import java.awt.event.*;
 import java.util.Date;
 import javax.swing.*;
 
 /**
  *
  * @author Anthony
  */
 public class viewTimetable extends javax.swing.JFrame {
     /**
      * Creates new form DisplayTimetable
      * 
      */
     Object[][] data = null;
     String[] columnName = null;
     int dayID;
     
     public viewTimetable() {
         initComponents();
     }
 
     @SuppressWarnings("unchecked")
     private void initComponents() {
 
         final JScrollPane jscroll = new javax.swing.JScrollPane();
         final JPanel DailyPanel = new JPanel();
         final JScrollPane jScrollPane3 = new JScrollPane();
         TableColumn tcol;
         
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         
         /*jlabels for Titles and headers*/
         final JLabel title = new JLabel();
         title.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
         title.setText("Rosters");
         
         final JLabel Daylabel = new JLabel();
         Daylabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         Daylabel.setText(" ");
         
         /*creating the Dailytable*/
         final JTable timeTable = new JTable();
         timeTable.setDefaultRenderer(Object.class, new cellRenderer());
         timeTable.setAutoCreateRowSorter(true);
         timeTable.setShowGrid(true);
         timeTable.getTableHeader().setReorderingAllowed(false);
         timeTable.setModel(new DefaultTableModel(
             new Object [][] {
                 {"358", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {"383", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {"384", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}},
             new String [] {
                 "Service", "5:00 - 6:00", "6:00 - 7:00", "7:00 - 8:00", "8:00 - 9:00", "9:00 - 10:00", "10:00 - 11:00", 
                 "11:00 - 12:00", "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00", 
                 "17:00 - 18:00", "18:00 - 19:00", "19:00 - 20:00", "20:00 - 21:00", "21:00 - 22:00", "22:00 - 23:00", 
                 "23:00 - 00:00", "00:00 - 1:00"}
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
             };
                     
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         
         for(int i = 0; i<21; i++)
         {
             tcol = timeTable.getColumnModel().getColumn(i);
             tcol.setCellRenderer(new cellRenderer());
         }
         
         jScrollPane3.setViewportView(timeTable);
      
         for(int column=0; column<21; column++)
         {
             timeTable.getColumnModel().getColumn(column).setPreferredWidth(120);
             timeTable.getColumnModel().getColumn(column).setResizable(false);
         }
         for(int row=0; row<=3; row++)
             timeTable.setRowHeight(150);
        
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(DailyPanel);
         DailyPanel.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(Daylabel)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 4000, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(20, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                 .addGap(22, 22, 22)
                 .addComponent(Daylabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(21, Short.MAX_VALUE))
         );
         jscroll.setViewportView(DailyPanel);
         DailyPanel.setVisible(false);
         jScrollPane3.setVisible(false);
         
         
         /*creating the combobox*/
         final JComboBox combobox = new JComboBox();
         final JComboBox combobox1 = new JComboBox();
         
         combobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-- Select --","Weekly", "Daily" }));
         
         combobox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", 
                                                                                 "Friday", "Saturday", "Sunday" }));
         combobox1.setVisible(true);
         combobox.setVisible(false);
         /*creating the Button*/
         final JButton button = new JButton();                                                        
         button.setText("OK!");
         button.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 
                 String selType = /*combobox.getSelectedItem().toString();*/ "Daily";
                 String selDay = combobox1.getSelectedItem().toString();
                 
                           
                 if(selType == "Daily")
                 {
                     data = new Object[][] {
                             {"358 ", null, null, null, null, null, null, null, null, null,null, null, null, null, null, null, null, null, null, null, null},
                             {"383", null, null, null, null, null, null, null, null,null, null, null, null, null, null, null, null, null, null, null, null},
                             {"384", null, null, null, null, null, null, null,null, null, null, null, null, null, null, null, null, null, null, null, null}};
                     
                     columnName = new String [] {"Service", "5:00 - 6:00" , "6:00 - 7:00", "7:00 - 8:00", "8:00 - 9:00", "9:00 - 10:00", 
                         "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", 
                         "16:00 - 17:00", "17:00 - 18:00", "18:00 - 19:00", "19:00 - 20:00", "20:00 - 21:00", "21:00 - 22:00", 
                         "22:00 - 23:00", "23:00 - 00:00", "00:00 - 1:00"};
                     
                     if (selDay.compareTo("Monday") == 0)
                        dayID = 2;
                     else if(selDay.compareTo("Tuesday") == 0)
                        dayID = 3;
                     else if(selDay.compareTo("Wednesday") == 0)
                        dayID = 4;
                     else if(selDay.compareTo("Thursday") == 0)
                        dayID = 5;
                     else if(selDay.compareTo("Friday") == 0)
                        dayID = 6;
                     else if(selDay.compareTo("Saturday") == 0)
                        dayID = 7;
                     else if (selDay.compareTo("Sunday") == 0)
                        dayID = 1;
 
                     System.out.println("Date you have selected is "+selDay);
                     String[][] toBePrinted = RoasterGenerator.dayGUI(dayID, RoasterGenerator.roster);
                     for(int column = 1; column < 21; column++)
                     {
                         for(int row = 0; row < 3; row++)
                         {
                             data[row][column] = toBePrinted[row][column];
                         }
                     }
                     
                     timeTable.setModel(new DefaultTableModel(data,columnName));
                     
                     combobox1.setVisible(true);
                     
                     if (selDay == "Monday")
                     {
                         dayID = 2;
                         jScrollPane3.setVisible(true);
                         DailyPanel.setVisible(true);
                         Daylabel.setText("Monday Roster");
                     }
                     else if(selDay == "Tuesday")
                     {
                         dayID = 3;
                         jScrollPane3.setVisible(true);
                         DailyPanel.setVisible(true);
                         Daylabel.setText("Tuesday Roster");
                     }
                     else if(selDay == "Wednesday")
                     {
                         dayID = 4;
                         jScrollPane3.setVisible(true);
                         DailyPanel.setVisible(true);
                         Daylabel.setText("Wednesday Roster");
                     }
                     else if(selDay == "Thursday")
                     {
                         dayID = 5;
                         jScrollPane3.setVisible(true);
                         DailyPanel.setVisible(true);
                         Daylabel.setText("Thursday Roster");
                     }
                     else if(selDay == "Friday")
                     {
                         dayID = 6;
                         jScrollPane3.setVisible(true);
                         DailyPanel.setVisible(true);
                         Daylabel.setText("Friday Roster");
                     }
                     else if(selDay == "Saturday")
                     {
                         dayID = 7;
                         jScrollPane3.setVisible(true);
                         DailyPanel.setVisible(true);
                         Daylabel.setText("Saturday Roster");
                     }
                     else if(selDay == "Sunday")
                     {
                         dayID = 1;
                         jScrollPane3.setVisible(true);
                         DailyPanel.setVisible(true);
                         Daylabel.setText("Sunday Roster");
                     }
                 }
                 else if(selType == "Weekly")
                 {
                     jScrollPane3.setVisible(true);
                     DailyPanel.setVisible(true);
                     Daylabel.setText("Weekly Roster");
                     
                     data = new Object[][] {
                             {"6:00 - 7:00", null, null, null, null, null, null, null},
                             {"7:00 - 8:00", null, null, null, null, null, null, null},
                             {"8:00 - 9:00", null, null, null, null, null, null, null, null},
                             {"9:00 - 10:00", null, null, null, null, null, null, null, null},
                             {"10:00 - 11:00", null, null, null, null, null, null, null, null},
                             {"11:00 - 12:00", null, null, null, null, null, null, null, null},
                             {"12:00 - 13:00", null, null, null, null, null, null, null, null},
                             {"13:00 - 14:00", null, null, null, null, null, null, null, null},
                             {"15:00 - 16:00", null, null, null, null, null, null, null, null},
                             {"16:00 - 17:00", null, null, null, null, null, null, null, null},
                             {"17:00 - 18:00", null, null, null, null, null, null, null, null},
                             {"18:00 - 19:00", null, null, null, null, null, null, null, null},
                             {"19:00 - 20:00", null, null, null, null, null, null, null, null},
                             {"20:00 - 21:00", null, null, null, null, null, null, null, null},
                             {"21:00 - 22:00", null, null, null, null, null, null, null, null},
                             {"22:00 - 23:00", null, null, null, null, null, null, null, null},
                             {"23:00 - 00:00", null, null, null, null, null, null, null, null},
                             {"00:00 - 1:00", null, null, null, null, null, null, null, null},
                             };
                     
                     for(int column = 1; column <= 7; column++)
                     {
                         for(int row = 0; row <= 17; row++)                          
                         {   
                             /*
                              * get driver  info
                              * 
                              */
                             
                             /* Column
                              * 1 = Mon, 2 = Tue, 3 = Wed, .... You get the point..
                              * Row
                              * 0 = 6:00, 1 = 7:00, 2 = 8:00 .. again you get the point .. 17 = 00:00, 18 = 1:00 again you get the point*/
                             
                             /*if( busdriver time <= row && busdriver > row && busdriver day == column)
                              *       insert in data[row][column]
                              */
                             
                             data[row][column] = "dddddd\n dddddddddddd\n dddddddddddddddddddddddddd\n dddddd\n ddddddddddddd \n dddddd \ndddd \nsds" ;
                         }
                     }
                     
                     columnName = new String [] {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                     
                     timeTable.setModel(new DefaultTableModel(data,columnName));
                     
                 }
                 
                
         }
         });
         
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(31, 31, 31)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(10, 10, 10)
                         .addComponent(combobox, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(28, 28, 28)
                         .addComponent(combobox1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(26, 26, 26)
                         .addComponent(button, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jscroll, javax.swing.GroupLayout.PREFERRED_SIZE, 1000, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGap(29, 29, 29)
                 .addComponent(title)
                 .addGap(9, 9, 9)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(button, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(combobox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(combobox1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jscroll, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(77, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(viewTimetable.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(viewTimetable.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(viewTimetable.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(viewTimetable.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new viewTimetable().setVisible(true);
             }
         });
     }
 }
 
