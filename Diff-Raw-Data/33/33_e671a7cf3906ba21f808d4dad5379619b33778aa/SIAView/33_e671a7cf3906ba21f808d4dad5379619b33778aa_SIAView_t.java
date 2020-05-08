 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tutoring.ui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.GradientPaint;
 import java.awt.Window;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JComboBox;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.border.MatteBorder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.text.JTextComponent;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.CategoryLabelPositions;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PiePlot3D;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.category.BarRenderer;
 import org.jfree.chart.title.LegendTitle;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.data.general.DefaultPieDataset;
 import org.jfree.data.general.PieDataset;
 import org.jfree.util.Rotation;
 import tutoring.entity.*;
 import tutoring.helper.*;
 
 
 /**
  *
  * @author shohe_i
  */
 public final class SIAView extends javax.swing.JFrame
 {
 
     /**
      * Creates new form SIA
      */
     public enum ComboBoxesIndexes
     {   
         CFNAME(0, "First Name", "fname", 'd'),
         CLNAME(1, "Last Name", "lname", 'd'),
         CPHONE(2,"Phone", "phone", 'd'),
         CEMAIL(3, "Email", "email", 'd'),
         //CATEGORY(4, "Category", "name", 'a'),
         COURSE(0, "Course", "abbrevName", 's'),
         CREATOR(0, "Creator", "", 'e'),
         LEVEL(1, "Level", "level", 'c'),
         LOCATION(1, "Location", "location",'l'),
         PARAPROFESSIONAL(2, "Tutor","", 'p'),
         TEACHER(2, "Teacher", "concat_ws(' ', t.fname, t.lname)", 't');
         
         private int indexOfCombo;
         private String displayName;
         private String databaseName;
         private char letter;
         
 	private ComboBoxesIndexes(int i, String displayName, String databaseName, char letter) {
 		indexOfCombo = i;
                 this.displayName = displayName;
                 this.databaseName = databaseName;
                 this.letter = letter;
 	}
         
         public char getLetter()
         {
             return letter;
         }
  
 	public int getBoxIndex() {
 		return indexOfCombo;
 	}
         
         public String getDisplayName() {
 		return displayName;
 	}
         
         public String getDatabaseName()
         {
             return databaseName;
         }
         
         public String getDatabaseName(String DisplayName)
         {
             SIAView.ComboBoxesIndexes[] components = SIAView.ComboBoxesIndexes.class.getEnumConstants();
             for(int i=0; i< components.length; i++)
                 if(components[i].getDisplayName().equalsIgnoreCase(DisplayName))
                     return components[i].getDatabaseName();
             
             return "";
         }
     }
     private UltimateAutoComplete uac;
     
     UltimateAutoAutoComplete uaacClient; 
 
     UltimateAutoAutoComplete uaacCourse;
     private int sessionID = -1;
     
     Thread threadLogoff = new Thread(){
                 public void run(){
                 while(timeTillLogoff-System.currentTimeMillis() > 0)
                 {
                     //timeTillLogoff = System.currentTimeMillis();
                 }
                 
                 }     
             };
 
         
     
     public SIAView() 
     {

         initComponents();
        this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);
         threadLogoff.start();
         
         //Logoff thread
         
         
         
         notesField.setLineWrap(true);
         sessionstartField.setText("mm/dd/yyyy hh:mm aa");
         sessionendField.setText("mm/dd/yyyy hh:mm aa");
         editSaveButton.setVisible(false);
         
         SessionTableHelper tableHelper = new SessionTableHelper(sessionsTable, false, null);
         SessionTableHelper tableHelperFuture = new SessionTableHelper(appointmentsTable, true, (SessionTableModel)sessionsTable.getModel());
         AgendaTableHelper tableHelperAgenda = new AgendaTableHelper(agendaTable);
          tableHelperAgenda.allowScrollingOnTable();
        
         tableHelperAgenda.increaseRowHeight(12);
         
         tableHelperFuture.allowScrollingOnTable();
         tableHelperFuture.increaseRowHeight(12);
         
         tableHelper.allowScrollingOnTable();
        
         tableHelper.increaseRowHeight(12);
        
        // sessionsTable.setCellSelectionEnabled(true);
 
         DatabaseHelper.open();
         
        Data d = new Data(false);
        System.out.println("DATA");
        //Clients autocomplete
       JComboBox[] cboxes = new  JComboBox[4];
        cboxes[0]=fnameCombo;
        cboxes[1]=lnameCombo;
        cboxes[2]=phoneCombo;
        cboxes[3]=emailCombo;
        
        ArrayList<ArrayList<String>> cultimateList = new ArrayList<ArrayList<String>>();
 System.out.println("LIST1");
        cultimateList.add(Data.getClientsfirst());
        cultimateList.add(Data.getClientslast());
        cultimateList.add(Data.getClientsphone());
        cultimateList.add(Data.getClientsemail());
        System.out.println("DONE LIST1");
        ArrayList<ArrayList<String>> cultimateList1 = new ArrayList<ArrayList<String>>();
 System.out.println("LIST 2");
        cultimateList1.add(Data.getFnameOrderedList());
        cultimateList1.add(Data.getLnameOrderedList());
        cultimateList1.add(Data.getPhoneOrderedList());
        cultimateList1.add(Data.getEmailOrderedList());
 System.out.println("DONE LIST2");
        uaacClient = new UltimateAutoAutoComplete(cultimateList, cboxes, cultimateList1);//Data.getClientFirst(), Data.getClientLast(), Data.getClientPhone(), Data.getClientEmail());
       
        
        JComboBox[] cboxes2 = new  JComboBox[3];
        cboxes2[0]=courseCombo;
        cboxes2[1]=levelCombo;
        cboxes2[2]=teacherCombo;
        //cboxes[3]=emailCombo;
        System.out.println("LIST 3");
        ArrayList<ArrayList<String>> cultimateList2 = new ArrayList<ArrayList<String>>();
 
        cultimateList2.add(Data.getSubjectslist());
        cultimateList2.add(Data.getLevelslist());
        cultimateList2.add(Data.getTeacherslist());
 System.out.println("DONE list 3");
        ArrayList<ArrayList<String>> cultimateList22 = new ArrayList<ArrayList<String>>();
 System.out.println("LIst 4");
        cultimateList22.add(Data.getSubjectOrderedList());
        cultimateList22.add(Data.getLevelOrderedList());
        cultimateList22.add(Data.getTeacherOrderedList());
 System.out.println("Done list 4");
        uaacCourse = new UltimateAutoAutoComplete(cultimateList2, cboxes2, cultimateList22);//Data.getClientFirst(), Data.getClientLast(), Data.getClientPhone(), Data.getClientEmail());
       
        
        JComboBox[] boxes3 = new  JComboBox[3];
         
         boxes3[0]=creatorCombo;
         boxes3[1]=locationCombo;
         boxes3[2]=paraprofessionalCombo;
 
         ArrayList<ArrayList<String>> cultimateList3 = new ArrayList<ArrayList<String>>();
         
         cultimateList3.add(Data.getTutorslist());
         cultimateList3.add(Data.getLocationslist());
         cultimateList3.add(Data.getTutorslist());
        
         uac = new UltimateAutoComplete(cultimateList3, boxes3);
             
             
         clearComboBoxes();
       
         Timestamp now = new Timestamp((new Date()).getTime());
         /*
         Transaction trns = null;
         ArrayList<ParaprofessionalSession> result = null;
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         //if(session == null)
         //    session = HibernateUtil.getSessionFactory().openSession();
         try {
             trns = session.beginTransaction();
             Query query = session.createQuery("from ParaprofessionalSession as ps where (ps.sessionStart IS NULL or (ps.sessionStart <= '"+now.toString()+"' and ps.sessionEnd IS NULL)) AND walkout='false'");
             result = (ArrayList<ParaprofessionalSession>) query.list();
             if(result.size() > 0)
             {
                 for(int i=0; i<result.size(); i++)
                 {
                     ((SessionTableModel) sessionsTable.getModel()).addRow(result.get(i)); 
                 }
 
                 sessionsTable.repaint();
             }
         } catch (RuntimeException e) {
             e.printStackTrace();
         } finally {
             session.flush();
             session.close();
         }
         trns = null;
         result = null;
         session = HibernateUtil.getSessionFactory().getCurrentSession();
         try {
             trns = session.beginTransaction();
             Query query = session.createQuery("from ParaprofessionalSession as ps where (ps.sessionStart IS NOT NULL and ps.sessionEnd IS NULL) AND ps.sessionStart >= '"+now.toString()+"' AND walkout='false'");
             result = (ArrayList<ParaprofessionalSession>) query.list();
             
              if(result.size() > 0)
             {
                 for(int i=0; i<result.size(); i++)
                 {
                     ((SessionTableModel) appointmentsTable.getModel()).addRow(result.get(i)); 
                 }
 
                 appointmentsTable.repaint();
             }
         } catch (RuntimeException e) {
             e.printStackTrace();
         } finally {
             session.flush();
             session.close();
         }*/
         
        System.out.println("SESSIONS");
        
        String sessStartCol = ParaprofessionalSession.ParaSessTable.SESSIONSTART.getWithAlias();
        String sessEndCol = ParaprofessionalSession.ParaSessTable.SESSIONEND.getWithAlias();
        String walkoutCol = ParaprofessionalSession.ParaSessTable.WALKOUT.getWithAlias();
        
       String currentSessionsWhere = " where ("+sessStartCol+" IS NULL or ("+sessStartCol+" <= '"+now.toString()+"' and "+sessEndCol+" IS NULL)) AND "+walkoutCol+"='false'";
 
        ArrayList<ParaprofessionalSession> sessions = ParaprofessionalSession.selectAllParaprofessionalSession(currentSessionsWhere,DatabaseHelper.getConnection());
         if(sessions.size() > 0)
         {
             for(int i=0; i<sessions.size(); i++)
             {
                 ((SessionTableModel) sessionsTable.getModel()).addRow(sessions.get(i)); 
             }
             
             sessionsTable.repaint();
         }
         
         System.out.println("SESSIONS AGIAN");
         
         String futureSessionsWhere = " where ("+sessStartCol+" IS NOT NULL and "+sessEndCol+" IS NULL) AND "+sessStartCol+" >= '"+now.toString()+"' AND "+walkoutCol+"='false'";
 
         ArrayList<ParaprofessionalSession> futureSessions = ParaprofessionalSession.selectAllParaprofessionalSession(futureSessionsWhere, DatabaseHelper.getConnection());
         if(futureSessions.size() > 0)
         {
             for(int i=0; i<futureSessions.size(); i++)
             {
                 ((SessionTableModel) appointmentsTable.getModel()).addRow(futureSessions.get(i)); 
             }
             
             appointmentsTable.repaint();
         }
         
         
         /*
          ArrayList<ParaprofessionalSession> sessions = (ArrayList<ParaprofessionalSession>)HibernateTest.select("from ParaprofessionalSession as ps where (ps.sessionStart IS NULL or ps.sessionEnd IS NULL) AND walkout='false'");
 
         if(sessions.size() > 0)
         {
             for(int i=0; i<sessions.size(); i++)
             {
                 ((SessionTableModel) sessionsTable.getModel()).addRow(sessions.get(i)); 
             }
             
             sessionsTable.repaint();
         }*/
         
         
         
         Timer timer = new Timer("Minute Update");
  
         //2- Taking an instance of class contains your repeated method.
         MinuteUpdate min = new MinuteUpdate((SessionTableModel)sessionsTable.getModel());
  
         MinuteUpdate min2 = new MinuteUpdate((SessionTableModel)appointmentsTable.getModel());
 
         timer.schedule(min, 0, 60000);
         timer.schedule(min2, 0, 60000);
         
         
         
         DefaultCellEditor dce = makeEditSessionCellEditor();
         DefaultCellEditor dceAgenda = makeEditAgendaCellEditor();
         
         setUpAgenda();
         
         tableHelper.setTableRendersAndEditors(true, dce);
         tableHelperFuture.setTableRendersAndEditors(true, dce);
         tableHelperAgenda.setTableRendersAndEditors(true, dceAgenda);
         //tableHelperAgenda.autoResizeColWidth();
         tableHelper.autoResizeColWidth();
         tableHelperFuture.autoResizeColWidth();
         //tableHelper.fasterScrolling(20);
             
     ///SIAScrollPanel.getVerticalScrollBar().setUnitIncrement(20);
         setUpGeneralReportTab();
         
     DatabaseHelper.close();
     }
     
     
     public void updateTables()
     {
         DatabaseHelper.open();
         Timestamp now = new Timestamp((new Date()).getTime());
         String sessStartCol = ParaprofessionalSession.ParaSessTable.SESSIONSTART.getWithAlias();
        String sessEndCol = ParaprofessionalSession.ParaSessTable.SESSIONEND.getWithAlias();
        String walkoutCol = ParaprofessionalSession.ParaSessTable.WALKOUT.getWithAlias();
        
       String currentSessionsWhere = " where ("+sessStartCol+" IS NULL or ("+sessStartCol+" <= '"+now.toString()+"' and "+sessEndCol+" IS NULL)) AND "+walkoutCol+"='false'";
 ((SessionTableModel) sessionsTable.getModel()).deleteAllRows();
        ArrayList<ParaprofessionalSession> sessions = ParaprofessionalSession.selectAllParaprofessionalSession(currentSessionsWhere,DatabaseHelper.getConnection());
         if(sessions.size() > 0)
         {
             for(int i=0; i<sessions.size(); i++)
             {
                 ((SessionTableModel) sessionsTable.getModel()).addRow(sessions.get(i)); 
                 System.out.println("ADDED SESSION");
             }
             
             sessionsTable.repaint();
         }
         
         System.out.println("SESSIONS AGIAN");
         
         String futureSessionsWhere = " where ("+sessStartCol+" IS NOT NULL and "+sessEndCol+" IS NULL) AND "+sessStartCol+" >= '"+now.toString()+"' AND "+walkoutCol+"='false'";
 
         SessionTableModel stm = ((SessionTableModel) appointmentsTable.getModel());
         stm.deleteAllRows();
         ArrayList<ParaprofessionalSession> futureSessions = ParaprofessionalSession.selectAllParaprofessionalSession(futureSessionsWhere, DatabaseHelper.getConnection());
         if(futureSessions.size() > 0)
         {
             for(int i=0; i<futureSessions.size(); i++)
             {
                 ((SessionTableModel) appointmentsTable.getModel()).addRow(futureSessions.get(i)); 
                 System.out.println("ADDED FUTURE SESSION");
             }
             
             appointmentsTable.repaint();
         }
         
         String fromCurrentDateWhere = " where "+Agenda.AgendaTable.DATE.getWithAlias()+" >= '2012-04-18'";
 
         ArrayList<Agenda> agenda = Agenda.selectAllAgenda(fromCurrentDateWhere, DatabaseHelper.getConnection());
         ((AgendaTableModel) agendaTable.getModel()).deleteAllRows();
         if(agenda.size() > 0)
         {          
             
             for(int i=0; i<agenda.size(); i++)
             {
                 
                 Agenda a = agenda.get(i);
                 ((AgendaTableModel) agendaTable.getModel()).addRow(a);
                 //System.out.println("AGENDA : "+a.getAgendaID()+" "+ a.getDate()+" "+ a.getNotes()+" " +a.getAgendaCategoryID().getType());
                 
             }
            
         agendaTable.repaint();
         
         }
         DatabaseHelper.close();
     }
     
     
     public void setUpAgenda()
     {
         
         Timestamp now = new Timestamp((new Date()).getTime());
         
         String fromCurrentDateWhere = " where "+Agenda.AgendaTable.DATE.getWithAlias()+" >= '2012-04-18'";
 
         ArrayList<Agenda> agenda = Agenda.selectAllAgenda(fromCurrentDateWhere, DatabaseHelper.getConnection());
 
         if(agenda.size() > 0)
         {          
             
             for(int i=0; i<agenda.size(); i++)
             {
                 
                 Agenda a = agenda.get(i);
                 ((AgendaTableModel) agendaTable.getModel()).addRow(a);
                 //System.out.println("AGENDA : "+a.getAgendaID()+" "+ a.getDate()+" "+ a.getNotes()+" " +a.getAgendaCategoryID().getType());
                 
             }
             
             agendaTable.repaint();
             
             /*agendaTable = new JTable(new DefaultTableModel(null,new Object[]{"Agenda ID", "Date", "Notes", "Type"}));
             DefaultTableModel dtm = (DefaultTableModel) agendaTable.getModel();
             
             for(int i=0; i<agenda.size(); i++)
             {
                 
                 Agenda a = agenda.get(i);
                 dtm.addRow(new Object[]{a.getAgendaID(), a.getDate(), a.getNotes(), a.getAgendaCategoryID().getType()});
                 System.out.println("AGENDA : "+a.getAgendaID()+" "+ a.getDate()+" "+ a.getNotes()+" " +a.getAgendaCategoryID().getType() +" "+ dtm.getColumnCount() + " "+dtm.getRowCount());
                 
             }
                 //model.addRow(new Object[] {""); 
             
             agendaTable.setModel(dtm);
             dtm.fireTableDataChanged();
             agendaTable.invalidate();
             agendaTable.repaint();*/
         }
         else
             System.out.println("EEEMMMMMMPPPPPPTTYYYYY");
         
         System.out.println("AGENDA AGENDA AGENDA AGENDA AGENDA DONE DONE DONE DONE DONE");
         
     }
     
     public DefaultCellEditor makeEditSessionCellEditor()
     {
         DefaultCellEditor dce = new DefaultCellEditor(new JTextField())
         {
             @Override
             public Component getTableCellEditorComponent(JTable table, Object value,
                         boolean isSelected, int row, int column) 
             {
                 sessionID = ((Integer)table.getValueAt(row, SessionTableModel.Columns.ID.getColumnIndex())).intValue();
                 
                 uaacCourse.setComboValue(table.getValueAt(row, SessionTableModel.Columns.TEACHER.getColumnIndex()).toString(), ComboBoxesIndexes.TEACHER.getBoxIndex());
                 uaacCourse.setComboValue(table.getValueAt(row, SessionTableModel.Columns.LEVEL.getColumnIndex()).toString(), ComboBoxesIndexes.LEVEL.getBoxIndex());
                 uaacCourse.setComboValue(table.getValueAt(row, SessionTableModel.Columns.COURSE.getColumnIndex()).toString(), ComboBoxesIndexes.COURSE.getBoxIndex());
                 uaacClient.setComboValue(table.getValueAt(row, SessionTableModel.Columns.CLIENTEMAIL.getColumnIndex()).toString(), ComboBoxesIndexes.CEMAIL.getBoxIndex());
                 uaacClient.setComboValue(table.getValueAt(row, SessionTableModel.Columns.CLIENTPHONE.getColumnIndex()).toString(), ComboBoxesIndexes.CPHONE.getBoxIndex());
                 uaacClient.setComboValue(table.getValueAt(row, SessionTableModel.Columns.CLIENTLASTNAME.getColumnIndex()).toString(), ComboBoxesIndexes.CLNAME.getBoxIndex());
                 uaacClient.setComboValue(table.getValueAt(row, SessionTableModel.Columns.CLIENTFIRSTNAME.getColumnIndex()).toString(), ComboBoxesIndexes.CFNAME.getBoxIndex());
                 //uac.setComboValue(table.getValueAt(row, SessionTableModel.Columns.CATEGORY.getColumnIndex()).toString(), ComboBoxesIndexes.CATEGORY.getBoxIndex());
                 uac.setComboValue(table.getValueAt(row, SessionTableModel.Columns.PARAPROFESSIONAL.getColumnIndex()).toString(), ComboBoxesIndexes.PARAPROFESSIONAL.getBoxIndex());
                 uac.setComboValue(table.getValueAt(row, SessionTableModel.Columns.LOCATION.getColumnIndex()).toString(), ComboBoxesIndexes.LOCATION.getBoxIndex());
                 uac.setComboValue(table.getValueAt(row, SessionTableModel.Columns.CREATOR.getColumnIndex()).toString(), ComboBoxesIndexes.CREATOR.getBoxIndex());
                 
                 gcCheck.setSelected((Boolean)table.getValueAt(row, SessionTableModel.Columns.GC.getColumnIndex()));
                 
                 notesField.setText(table.getValueAt(row, SessionTableModel.Columns.NOTES.getColumnIndex()).toString());
                 
                // System.out.println("LKJDSFLDSJLKDSJFLKSDJF DSLJDSFLKDSJ "+table.getValueAt(row, SessionTableModel.Columns.START.getColumnIndex()));
                // System.out.println("LKJDSFLDSJLKDSJFLKSDJF DSLJDSFLKDSJ "+table.getValueAt(row, SessionTableModel.Columns.STOP.getColumnIndex()));
                                
                 SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.ENGLISH);
                
                 boolean hasSessionStart = (table.getValueAt(row, SessionTableModel.Columns.START.getColumnIndex()) != null && Validate.validateTimestamp(sdf.format(new Date(((Timestamp)table.getValueAt(row, SessionTableModel.Columns.START.getColumnIndex())).getTime()))) && !sdf.format(new Date(((Timestamp)table.getValueAt(row, SessionTableModel.Columns.START.getColumnIndex())).getTime())).equals("12/31/9999 12:00 PM"));
                 boolean hasSessionEnd = (table.getValueAt(row, SessionTableModel.Columns.STOP.getColumnIndex()) != null && Validate.validateTimestamp(sdf.format(new Date(((Timestamp)table.getValueAt(row, SessionTableModel.Columns.STOP.getColumnIndex())).getTime())))  && !sdf.format(new Date(((Timestamp)table.getValueAt(row, SessionTableModel.Columns.STOP.getColumnIndex())).getTime())).equals("12/31/9999 12:00 PM"));
                 
                 
                 if(hasSessionStart)
                     sessionstartField.setText(sdf.format(new Date(((Timestamp)table.getValueAt(row, SessionTableModel.Columns.START.getColumnIndex())).getTime())));
                 else
                     sessionstartField.setText("mm/dd/yyyy hh:mm aa");
 
                 if(hasSessionEnd)
                     sessionendField.setText(sdf.format(new Date(((Timestamp)table.getValueAt(row, SessionTableModel.Columns.STOP.getColumnIndex())).getTime())));
                 else
                     sessionendField.setText("mm/dd/yyyy hh:mm aa");
                 
                 walkoutCheck.setSelected((Boolean)table.getValueAt(row, SessionTableModel.Columns.WALKOUT.getColumnIndex()));
                 
                 editSaveButton.setVisible(true);
                
                 tabsPane.setSelectedIndex(0);
                 return null;
             }
         };
         
         return dce;
     }
     
     
     public DefaultCellEditor makeEditAgendaCellEditor()
     {
         DefaultCellEditor dce = new DefaultCellEditor(new JTextField())
         {
             @Override
             public Component getTableCellEditorComponent(JTable table, Object value,
                         boolean isSelected, int row, int column) 
             {
                 SimpleDateFormat sdfFrom = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
                 SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                 String type = table.getValueAt(row, AgendaTableModel.Columns.TYPE.getColumnIndex()).toString();
                 String date = "";
                 try{
                     date= sdfTo.format(sdfFrom.parse(table.getValueAt(row, AgendaTableModel.Columns.DATE.getColumnIndex()).toString()));
                 }
                 catch(Exception e)
                 {
                     
                 }
                 String notes = table.getValueAt(row, AgendaTableModel.Columns.NOTES.getColumnIndex()).toString();
                 int agendaID =((Integer) table.getValueAt(row, AgendaTableModel.Columns.ID.getColumnIndex())).intValue();
                 
                 NewAgendaObject ndo = new NewAgendaObject(new Frame(), true, Data.getAgendaCategoryList(), type, date, notes, agendaID);
                 ndo.setLocationRelativeTo(null);
                 ndo.setVisible(true);
                 System.out.println("HOPEFULLY NOT HERE YET");
                 updateTables();
                 return null;
             }
         };
         
         return dce;
     }
     
     public void clearComboBoxes()
     {
          for(int i=0; i<uac.getBoxesLength(); i++)
             uac.setComboValue("", i);
          for(int i=0; i<uaacClient.getBoxesLength(); i++)
             uaacClient.setComboValue("", i);
          for(int i=0; i<uaacCourse.getBoxesLength(); i++)
             uaacCourse.setComboValue("", i);
     }
     
     public void setUpGeneralReportTab()
     {
 
 
         DatabaseHelper.open();
         String[][] data = DatabaseHelper.getDataFromRegularQuery(
                 "SELECT "
                 + "abbrevName,"
                 + "COUNT(paraprofessionalSessionID) as 'Total Sessions',"
                 + "Sum(IF( TIMESTAMPDIFF("
                 + "MINUTE , sessionStart, sessionEnd ) >30, TIMESTAMPDIFF( "
                 + "MINUTE , sessionStart, sessionEnd ) /30, 1)) AS '30-min. Sessions', "
                 + "Sum(IF( TIMESTAMPDIFF( "
                 + "MINUTE , sessionStart, sessionEnd ) >30, TIMESTAMPDIFF( "
                 + "MINUTE , sessionStart, sessionEnd ) /30, 1))/count(paraprofessionalSessionID) as 'Avg. Session/Visit', "
                 + "SUM(walkout) as 'Walkouts', "
                 + "SUM(TIMESTAMPDIFF(MINUTE , timeAndDateEntered, sessionStart)) as 'Total Wait Time', "
                 + "SUM(TIMESTAMPDIFF(MINUTE , timeAndDateEntered, sessionStart))/COUNT(paraprofessionalSessionID) as 'Avg. Wait Time' "
                 + "FROM ParaprofessionalSession ps "
                 + "join Course c on ps.courseID=c.courseID "
                 + "join Subject s on c.subjectID=s.subjectID "
                 + "group by abbrevName");
 
         String[][] categoryData = DatabaseHelper.getDataFromRegularQuery(
                 "select c.name, count(paraprofessionalSessionID) as '# of Sessions'"
                 + " from ParaprofessionalSession ps"
                 + " join Course course on course.courseID=ps.courseID"
                 + " join Subject s on course.subjectID=s.subjectID"
                 + " join Category c on s.categoryID=c.categoryID"
                 + " group by c.name");
 
         String[][] otherValues = DatabaseHelper.getDataFromRegularQuery(
                 "SELECT "
                 + "SUM(walkout) as 'Walkouts', "
                 + "COUNT(paraprofessionalID) as 'Total Students',"
                 + "Sum(IF( TIMESTAMPDIFF("
                 + "MINUTE , sessionStart, sessionEnd ) >30, TIMESTAMPDIFF( "
                 + "MINUTE , sessionStart, sessionEnd ) /30, 1)) AS 'Total Sessions' "
                 + "FROM ParaprofessionalSession ps");
 
         String[][] studentMinutes = DatabaseHelper.getDataFromRegularQuery(
                 "SELECT "
                 + "Sum(IF( TIMESTAMPDIFF(MINUTE, sessionStart, sessionEnd ) < 10"
                 + " and TIMESTAMPDIFF(MINUTE, sessionStart, sessionEnd ) > 0, 1, 0))"
                 + " AS '<10 Min. Sessions', "
                 + "Sum(IF( TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) >= 10"
                 + " and TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) < 25, 1, 0))"
                 + " AS '10-24 Min. Sessions', "
                 + "Sum(IF( TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) >= 25"
                 + " and TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) <= 35, 1, 0))"
                 + " AS '25-35 Min. Sessions', "
                 + "Sum(IF( TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) > 35"
                 + " and TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) <= 60, 1, 0))"
                 + " AS '36-60 Min. Sessions', "
                 + "Sum(IF( TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) > 60"
                 + ", 1, 0))"
                 + " AS '>60 Min. Sessions' "
                 + "FROM ParaprofessionalSession ps");
 
         DatabaseHelper.close();
         displayCharts(data, categoryData, otherValues, studentMinutes);
         
 
 
         // String[] columns = new String[c.size()];
         // for(int i=0; i<c.size(); i++)
         //     columns[i]=(String)c.get(i);
 
 
     }
     
     private void displayCharts(String[][] generalData, String[][] categoryData, String[][] otherValues, String[][] studentMinutes)
     {
         String[] columns =
         {
             "Subject", "Total Sessions", "30-min. Sessions", "Avg. Session/Visit", "Total Wait Time", "Avg. Wait Time"
         };
 
         DefaultTableModel dtm = new DefaultTableModel();
         dtm.setDataVector(generalData, columns);
         generalReportTable.setModel(dtm);
 
         String[] columns1 =
         {
             "Category", "# of Sessions"
         };
 
         DefaultTableModel dtm1 = new DefaultTableModel();
         dtm1.setDataVector(categoryData, columns1);
         generalReportTable3.setModel(dtm1);
 
         String[] columns2 =
         {
             "Walkouts", "Total Students", "Total Sessions"
         };
 
         DefaultTableModel dtm2 = new DefaultTableModel();
         dtm2.setDataVector(otherValues, columns2);
         generalReportTable2.setModel(dtm2);
 
         String[] columns3 =
         {
             "<10 Min.", "10-25 Min.", "25-35 Min.", "36-60 Min.", ">60 Min."
         };
 
         DefaultTableModel dtm3 = new DefaultTableModel();
         dtm3.setDataVector(studentMinutes, columns3);
         generalReportTable4.setModel(dtm3);
 
 
 
         DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
         DefaultCategoryDataset barDataset1 = new DefaultCategoryDataset();
         DefaultCategoryDataset barDataset2 = new DefaultCategoryDataset();
         DefaultCategoryDataset barDataset3 = new DefaultCategoryDataset();
 
         DefaultPieDataset pieDataset = new DefaultPieDataset();
         DefaultPieDataset pieDataset1 = new DefaultPieDataset();
         DefaultPieDataset pieDataset2 = new DefaultPieDataset();
 
         String series = "Category";
 
 
         // String[] categories = new String[data.length];
         // for(int i=0; i<data.length; i++)
         //     categories[i] = data[i][1];
 
         for (int i = 0; i < categoryData.length; i++)
         {
             barDataset1.addValue(Double.parseDouble(categoryData[i][1]), series, categoryData[i][0]);
             pieDataset1.setValue(categoryData[i][0], Double.parseDouble(categoryData[i][1]));
         }
 
         for (int i = 0; i < columns2.length; i++)
         {
             barDataset2.addValue(Double.parseDouble(otherValues[0][i]), series, columns2[i]);
         }
 
         for (int i = 0; i < columns3.length; i++)
         {
             barDataset3.addValue(Double.parseDouble(studentMinutes[0][i]), series, columns3[i]);
             pieDataset2.setValue(columns3[i], Double.parseDouble(studentMinutes[0][i]));
         }
 
         for (int i = 0; i < generalData.length; i++)
         {
             System.out.println(Double.parseDouble(generalData[i][1]) + " + " + generalData[i][0]);
             barDataset.addValue(Double.parseDouble(generalData[i][1]), series, generalData[i][0]);
             pieDataset.setValue(generalData[i][0], Double.parseDouble(generalData[i][1]));
 
 
         }
 
         final JFreeChart barChart = createChart(barDataset, "Total Student Sessions by Subject", "# of Student Sessions", "Subject", false, Color.green, Color.gray);
         final ChartPanel barChartPanel = new ChartPanel(barChart);
         barChartPanel.setPreferredSize(generalChartPanelLong.getSize());
         System.out.println(barChartPanel.getPreferredSize().height + " " + barChartPanel.getPreferredSize().width);
         generalChartPanelLong.removeAll();
         generalChartPanelLong.add(barChartPanel);
         generalChartPanelLong.validate();
 
 
         final JFreeChart barChart1 = createChart(barDataset1, "Total Student Sessions by Category", "# of Student Sessions", "Category", false, Color.blue, Color.gray);
         final ChartPanel barChartPanel1 = new ChartPanel(barChart1);
         barChartPanel1.setPreferredSize(generalChartPanelLeft2.getSize());
         System.out.println(barChartPanel1.getPreferredSize().height + " " + barChartPanel1.getPreferredSize().width);
         generalChartPanelLeft2.removeAll();
         generalChartPanelLeft2.add(barChartPanel1);
         generalChartPanelLeft2.validate();
 
         final JFreeChart barChart2 = createChart(barDataset2, "Other Information", "Total #", "Statistic", false, Color.red, Color.gray);
         final ChartPanel barChartPanel2 = new ChartPanel(barChart2);
         barChartPanel2.setPreferredSize(generalChartPanelMid2.getSize());
         System.out.println(barChartPanel2.getPreferredSize().height + " " + barChartPanel2.getPreferredSize().width);
         generalChartPanelMid2.removeAll();
         generalChartPanelMid2.add(barChartPanel2);
         generalChartPanelMid2.validate();
 
         final JFreeChart barChart3 = createChart(barDataset3, "Session Length Overview", "# of Sessions of Length", "Length Period", false, Color.gray, Color.white);
         final ChartPanel barChartPanel3 = new ChartPanel(barChart3);
         barChartPanel3.setPreferredSize(generalChartPanelRight2.getSize());
         System.out.println(barChartPanel3.getPreferredSize().height + " " + barChartPanel3.getPreferredSize().width);
         generalChartPanelRight2.removeAll();
         generalChartPanelRight2.add(barChartPanel3);
         generalChartPanelRight2.validate();
 
 
         final JFreeChart pieChart = createChart(pieDataset, "Total Student Sessions by Subject");
         final ChartPanel pieChartPanel = new ChartPanel(pieChart);
         pieChartPanel.setPreferredSize(generalChartPanelLeft.getSize());
         System.out.println(pieChartPanel.getPreferredSize().height + " " + pieChartPanel.getPreferredSize().width);
         generalChartPanelLeft.removeAll();
         generalChartPanelLeft.add(pieChartPanel);
         generalChartPanelLeft.validate();
 
         final JFreeChart pieChart1 = createChart(pieDataset1, "Total Student Sessions by Category");
         final ChartPanel pieChartPanel1 = new ChartPanel(pieChart1);
         pieChartPanel1.setPreferredSize(generalChartPanelRight.getSize());
         System.out.println(pieChartPanel1.getPreferredSize().height + " " + pieChartPanel1.getPreferredSize().width);
         generalChartPanelRight.removeAll();
         generalChartPanelRight.add(pieChartPanel1);
         generalChartPanelRight.validate();
 
         final JFreeChart pieChart2 = createChart(pieDataset2, "Total Student Sessions by Length");
         final ChartPanel pieChartPanel2 = new ChartPanel(pieChart2);
         pieChartPanel2.setPreferredSize(generalChartPanelMid.getSize());
         System.out.println(pieChartPanel2.getPreferredSize().height + " " + pieChartPanel2.getPreferredSize().width);
         generalChartPanelMid.removeAll();
         generalChartPanelMid.add(pieChartPanel2);
         generalChartPanelMid.validate();
 
         // thechartPanel1.validate();
         //  reportPanel1.validate();
         // jScrollPane10.validate();
     }
     
     private JFreeChart createChart(PieDataset dataset, String title)
     {
 
         JFreeChart chart = ChartFactory.createPieChart3D(title, // chart title
                 dataset, // data
                 true, // include legend
                 true,
                 false);
 
         PiePlot3D plot = (PiePlot3D) chart.getPlot();
         chart.setBorderVisible(false);
         plot.setBackgroundPaint(this.getBackground());
         plot.setStartAngle(290);
         plot.setOutlineVisible(false);
         plot.setDirection(Rotation.CLOCKWISE);
         chart.setBackgroundPaint(this.getBackground());
         plot.setForegroundAlpha(0.75f);
 
         LegendTitle lt = chart.getLegend();
         lt.setBackgroundPaint(this.getBackground());
         lt.setBorder(0, 0, 0, 0);
         //lt.setBackgroundPaint(null);
         return chart;
     }
 
     private JFreeChart createChart(final CategoryDataset dataset, final String title, final String xText, final String yText, final boolean showLegend, Color start, Color end)
     {
 
         // create the chart...
         final JFreeChart chart = ChartFactory.createBarChart(
                 title, // chart title
                 xText, // domain axis label
                 yText, // range axis label
                 dataset, // data
                 PlotOrientation.VERTICAL, // orientation
                 showLegend, // include legend
                 true, // tooltips?
                 false // URLs?
                 );
 
         // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
 
         // set the background color for the chart...
         chart.setBackgroundPaint(this.getBackground());
 
         // get a reference to the plot for further customisation...
         final CategoryPlot plot = chart.getCategoryPlot();
         plot.setBackgroundPaint(this.getBackground());
         plot.setDomainGridlinePaint(Color.black);
         plot.setRangeGridlinePaint(Color.black);
         plot.setOutlineVisible(false);
         // set the range axis to display integers only...
         final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
         rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 
         // disable bar outlines...
         final BarRenderer renderer = (BarRenderer) plot.getRenderer();
         renderer.setDrawBarOutline(false);
 
         // set up gradient paints for series...
         final GradientPaint gp0 = new GradientPaint(
                 0.0f, 0.0f, start,
                 0.0f, 0.0f, end);
         /*
          final GradientPaint gp1 = new GradientPaint(
          0.0f, 0.0f, Color.green, 
          0.0f, 0.0f, Color.lightGray
          );
          final GradientPaint gp2 = new GradientPaint(
          0.0f, 0.0f, Color.red, 
          0.0f, 0.0f, Color.lightGray
          );*/
         renderer.setSeriesPaint(0, gp0);
         /*
          renderer.setSeriesPaint(1, gp1);
          renderer.setSeriesPaint(2, gp2);*/
 
         final CategoryAxis domainAxis = plot.getDomainAxis();
         domainAxis.setCategoryLabelPositions(
                 CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
         // OPTIONAL CUSTOMISATION COMPLETED.
 
         return chart;
     }
 
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents()
     {
         java.awt.GridBagConstraints gridBagConstraints;
 
         jPanel3 = new javax.swing.JPanel();
         tabsPane = new javax.swing.JTabbedPane();
         jPanel2 = new javax.swing.JPanel();
         studentInfoPanel = new javax.swing.JPanel();
         fnameLabel = new javax.swing.JLabel();
         fnameCombo = new javax.swing.JComboBox();
         lnameLabel = new javax.swing.JLabel();
         lnameCombo = new javax.swing.JComboBox();
         emailLabel = new javax.swing.JLabel();
         emailCombo = new javax.swing.JComboBox();
         phoneLabel = new javax.swing.JLabel();
         phoneCombo = new javax.swing.JComboBox();
         courseInfoPanel = new javax.swing.JPanel();
         courseLabel = new javax.swing.JLabel();
         courseCombo = new javax.swing.JComboBox();
         levelLabel = new javax.swing.JLabel();
         levelCombo = new javax.swing.JComboBox();
         teacherLabel = new javax.swing.JLabel();
         teacherCombo = new javax.swing.JComboBox();
         paraprofessionalInfoPanel = new javax.swing.JPanel();
         ParaprofessionalLabel = new javax.swing.JLabel();
         paraprofessionalCombo = new javax.swing.JComboBox();
         sessionstartLabel = new javax.swing.JLabel();
         sessionstartField = new javax.swing.JTextField();
         sessionendLabel = new javax.swing.JLabel();
         sessionendField = new javax.swing.JTextField();
         notesLabel = new javax.swing.JLabel();
         gcCheck = new javax.swing.JCheckBox();
         walkoutCheck = new javax.swing.JCheckBox();
         locationLabel = new javax.swing.JLabel();
         locationCombo = new javax.swing.JComboBox();
         creatorCombo = new javax.swing.JComboBox();
         creatorLabel = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         notesField = new javax.swing.JTextArea();
         jPanel5 = new javax.swing.JPanel();
         clearButton = new javax.swing.JButton();
         addSessionbutton = new javax.swing.JButton();
         editSaveButton = new javax.swing.JButton();
         newStudentButton = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         agendaPanel = new javax.swing.JPanel();
         agendaTableScrollPanel = new javax.swing.JScrollPane();
         agendaTable = new javax.swing.JTable();
         addAgendaItemButton = new javax.swing.JButton();
         deleteAgendaButton = new javax.swing.JButton();
         sessionsAndAgendaPanel = new javax.swing.JPanel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         sessionsTablePanel = new javax.swing.JPanel();
         sessionsScrollPane = new javax.swing.JScrollPane();
         sessionsTable = new javax.swing.JTable();
         deleteSessionButton = new javax.swing.JButton();
         futureSessionsPanel = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         appointmentsTable = new javax.swing.JTable();
         deleteSessionButton1 = new javax.swing.JButton();
         reportsPane = new javax.swing.JPanel();
         reportsScrollPane = new javax.swing.JScrollPane();
         reportsTopPane = new javax.swing.JPanel();
         tablePane = new javax.swing.JPanel();
         jScrollPane8 = new javax.swing.JScrollPane();
         generalReportTable2 = new javax.swing.JTable();
         generalReportBeginField = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         generalReportEndField = new javax.swing.JTextField();
         downloadButton1 = new javax.swing.JButton();
         generalReportLoadButton = new javax.swing.JButton();
         jScrollPane11 = new javax.swing.JScrollPane();
         generalReportTable = new javax.swing.JTable();
         jScrollPane12 = new javax.swing.JScrollPane();
         generalReportTable3 = new javax.swing.JTable();
         jScrollPane13 = new javax.swing.JScrollPane();
         generalReportTable4 = new javax.swing.JTable();
         graphPane = new javax.swing.JPanel();
         generalChartPanelLeft = new javax.swing.JPanel();
         generalChartPanelMid = new javax.swing.JPanel();
         generalChartPanelRight = new javax.swing.JPanel();
         generalChartPanelLong = new javax.swing.JPanel();
         generalChartPanelMid2 = new javax.swing.JPanel();
         generalChartPanelLeft2 = new javax.swing.JPanel();
         generalChartPanelRight2 = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setMinimumSize(new java.awt.Dimension(1150, 750));
         addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
         {
             public void mouseMoved(java.awt.event.MouseEvent evt)
             {
                 formMouseMoved(evt);
             }
         });
 
         jPanel3.setBackground(new java.awt.Color(255, 255, 255));
         jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Create"));
         jPanel2.setMinimumSize(new java.awt.Dimension(234, 0));
         jPanel2.setPreferredSize(new java.awt.Dimension(1111, 449));
 
         studentInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Student Information"));
         studentInfoPanel.setMaximumSize(new java.awt.Dimension(977, 63));
         studentInfoPanel.setMinimumSize(new java.awt.Dimension(977, 63));
         studentInfoPanel.setPreferredSize(new java.awt.Dimension(977, 63));
 
         fnameLabel.setText("First Name*");
 
         fnameCombo.setEditable(true);
         fnameCombo.setMaximumSize(new java.awt.Dimension(141, 28));
         fnameCombo.setMinimumSize(new java.awt.Dimension(141, 28));
         fnameCombo.setPreferredSize(new java.awt.Dimension(141, 28));
 
         lnameLabel.setText("Last Name*");
 
         lnameCombo.setEditable(true);
         lnameCombo.setMaximumSize(new java.awt.Dimension(156, 28));
         lnameCombo.setMinimumSize(new java.awt.Dimension(156, 28));
         lnameCombo.setPreferredSize(new java.awt.Dimension(156, 28));
 
         emailLabel.setText("Email");
 
         emailCombo.setEditable(true);
         emailCombo.setMaximumSize(new java.awt.Dimension(211, 28));
         emailCombo.setMinimumSize(new java.awt.Dimension(211, 28));
         emailCombo.setPreferredSize(new java.awt.Dimension(211, 28));
 
         phoneLabel.setText("Telephone");
 
         phoneCombo.setEditable(true);
         phoneCombo.setMaximumSize(new java.awt.Dimension(128, 28));
 
         org.jdesktop.layout.GroupLayout studentInfoPanelLayout = new org.jdesktop.layout.GroupLayout(studentInfoPanel);
         studentInfoPanel.setLayout(studentInfoPanelLayout);
         studentInfoPanelLayout.setHorizontalGroup(
             studentInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(studentInfoPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(fnameLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fnameCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(18, 18, 18)
                 .add(lnameLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(lnameCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(emailLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(emailCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(18, 18, 18)
                 .add(phoneLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(phoneCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         studentInfoPanelLayout.setVerticalGroup(
             studentInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(studentInfoPanelLayout.createSequentialGroup()
                 .add(3, 3, 3)
                 .add(studentInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(fnameCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(fnameLabel)
                     .add(lnameLabel)
                     .add(lnameCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(emailLabel)
                     .add(emailCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(phoneLabel)
                     .add(phoneCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(0, 0, 0))
         );
 
         phoneCombo.getAccessibleContext().setAccessibleParent(studentInfoPanel);
 
         courseInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Course Information"));
         courseInfoPanel.setMaximumSize(new java.awt.Dimension(832, 63));
         courseInfoPanel.setMinimumSize(new java.awt.Dimension(832, 63));
         courseInfoPanel.setPreferredSize(new java.awt.Dimension(832, 63));
 
         courseLabel.setText("Course*");
 
         courseCombo.setEditable(true);
         courseCombo.setMaximumSize(new java.awt.Dimension(128, 28));
 
         levelLabel.setText("Course#*");
 
         levelCombo.setEditable(true);
         levelCombo.setMaximumSize(new java.awt.Dimension(128, 28));
 
         teacherLabel.setText("Teacher*");
 
         teacherCombo.setEditable(true);
         teacherCombo.setMaximumSize(new java.awt.Dimension(347, 28));
         teacherCombo.setMinimumSize(new java.awt.Dimension(347, 28));
         teacherCombo.setPreferredSize(new java.awt.Dimension(347, 28));
         teacherCombo.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 teacherComboActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout courseInfoPanelLayout = new org.jdesktop.layout.GroupLayout(courseInfoPanel);
         courseInfoPanel.setLayout(courseInfoPanelLayout);
         courseInfoPanelLayout.setHorizontalGroup(
             courseInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(courseInfoPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(courseLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(courseCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(levelLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(levelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(teacherLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(teacherCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         courseInfoPanelLayout.setVerticalGroup(
             courseInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(courseInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(courseCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(courseLabel)
                 .add(levelLabel)
                 .add(teacherLabel)
                 .add(teacherCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
             .add(org.jdesktop.layout.GroupLayout.TRAILING, levelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
         );
 
         paraprofessionalInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Session Information"));
         paraprofessionalInfoPanel.setMaximumSize(new java.awt.Dimension(832, 155));
         paraprofessionalInfoPanel.setMinimumSize(new java.awt.Dimension(832, 155));
 
         ParaprofessionalLabel.setText("Paraprofessional*");
 
         paraprofessionalCombo.setEditable(true);
         paraprofessionalCombo.setMaximumSize(new java.awt.Dimension(160, 28));
         paraprofessionalCombo.setMinimumSize(new java.awt.Dimension(160, 28));
         paraprofessionalCombo.setPreferredSize(new java.awt.Dimension(160, 28));
 
         sessionstartLabel.setText("Session Start");
 
         sessionstartField.setText("dd/mm/yyyy hh:mm aa");
         sessionstartField.setMaximumSize(new java.awt.Dimension(156, 28));
         sessionstartField.setMinimumSize(new java.awt.Dimension(156, 28));
         sessionstartField.setPreferredSize(new java.awt.Dimension(156, 28));
 
         sessionendLabel.setText("Session End");
 
         sessionendField.setText("dd/mm/yyyy hh:mm:ss aa");
         sessionendField.setMaximumSize(new java.awt.Dimension(156, 28));
         sessionendField.setMinimumSize(new java.awt.Dimension(156, 28));
         sessionendField.setPreferredSize(new java.awt.Dimension(156, 28));
 
         notesLabel.setText("Notes");
 
         gcCheck.setText("GC");
 
         walkoutCheck.setText("Walkout");
 
         locationLabel.setText("Location*");
 
         locationCombo.setMaximumSize(new java.awt.Dimension(160, 28));
         locationCombo.setMinimumSize(new java.awt.Dimension(160, 28));
         locationCombo.setPreferredSize(new java.awt.Dimension(160, 28));
 
         creatorCombo.setEditable(true);
         creatorCombo.setMaximumSize(new java.awt.Dimension(160, 28));
         creatorCombo.setMinimumSize(new java.awt.Dimension(160, 28));
         creatorCombo.setPreferredSize(new java.awt.Dimension(160, 28));
 
         creatorLabel.setText("Creator*");
 
         notesField.setColumns(20);
         notesField.setLineWrap(true);
         notesField.setRows(5);
         notesField.setMaximumSize(new java.awt.Dimension(185, 116));
         notesField.setMinimumSize(new java.awt.Dimension(185, 116));
         notesField.setPreferredSize(new java.awt.Dimension(185, 116));
         jScrollPane2.setViewportView(notesField);
 
         org.jdesktop.layout.GroupLayout paraprofessionalInfoPanelLayout = new org.jdesktop.layout.GroupLayout(paraprofessionalInfoPanel);
         paraprofessionalInfoPanel.setLayout(paraprofessionalInfoPanelLayout);
         paraprofessionalInfoPanelLayout.setHorizontalGroup(
             paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(paraprofessionalInfoPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(ParaprofessionalLabel)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                         .add(creatorLabel)
                         .add(locationLabel)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(locationCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 160, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(paraprofessionalInfoPanelLayout.createSequentialGroup()
                         .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(paraprofessionalCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(creatorCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .add(18, 18, 18)
                         .add(notesLabel)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 185, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(18, 18, 18)
                 .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(sessionstartLabel)
                     .add(sessionendLabel))
                 .add(4, 4, 4)
                 .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(sessionendField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, paraprofessionalInfoPanelLayout.createSequentialGroup()
                         .add(walkoutCheck)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(gcCheck))
                     .add(org.jdesktop.layout.GroupLayout.LEADING, sessionstartField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .add(27, 27, 27))
         );
         paraprofessionalInfoPanelLayout.setVerticalGroup(
             paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(paraprofessionalInfoPanelLayout.createSequentialGroup()
                 .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(paraprofessionalInfoPanelLayout.createSequentialGroup()
                         .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(paraprofessionalCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(ParaprofessionalLabel)
                             .add(notesLabel))
                         .add(18, 18, 18)
                         .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(creatorLabel)
                             .add(creatorCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .add(18, 18, 18)
                         .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(locationLabel)
                             .add(locationCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .add(0, 2, Short.MAX_VALUE))
                     .add(paraprofessionalInfoPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(sessionstartLabel)
                             .add(sessionstartField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .add(23, 23, 23)
                         .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(sessionendField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(sessionendLabel))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(paraprofessionalInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(walkoutCheck)
                             .add(gcCheck)))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2))
                 .addContainerGap())
         );
 
         jPanel5.setMaximumSize(new java.awt.Dimension(139, 218));
         jPanel5.setPreferredSize(new java.awt.Dimension(139, 218));
         jPanel5.setLayout(new java.awt.GridBagLayout());
 
         clearButton.setForeground(new java.awt.Color(153, 0, 0));
         clearButton.setText("Clear");
         clearButton.setMaximumSize(new java.awt.Dimension(121, 50));
         clearButton.setMinimumSize(new java.awt.Dimension(121, 50));
         clearButton.setPreferredSize(new java.awt.Dimension(121, 50));
         clearButton.addMouseListener(new java.awt.event.MouseAdapter()
         {
             public void mouseClicked(java.awt.event.MouseEvent evt)
             {
                 clearButtonMouseClicked(evt);
             }
         });
         clearButton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 clearButtonActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 12);
         jPanel5.add(clearButton, gridBagConstraints);
 
         addSessionbutton.setForeground(new java.awt.Color(51, 102, 255));
         addSessionbutton.setText("Add Session");
         addSessionbutton.setMaximumSize(new java.awt.Dimension(121, 50));
         addSessionbutton.setMinimumSize(new java.awt.Dimension(121, 50));
         addSessionbutton.setPreferredSize(new java.awt.Dimension(121, 50));
         addSessionbutton.addMouseListener(new java.awt.event.MouseAdapter()
         {
             public void mouseClicked(java.awt.event.MouseEvent evt)
             {
                 addSessionbuttonMouseClicked(evt);
             }
         });
         addSessionbutton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 addSessionbuttonActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
         jPanel5.add(addSessionbutton, gridBagConstraints);
 
         editSaveButton.setText("Save/Edit");
         editSaveButton.setMaximumSize(new java.awt.Dimension(121, 44));
         editSaveButton.setMinimumSize(new java.awt.Dimension(121, 44));
         editSaveButton.setPreferredSize(new java.awt.Dimension(121, 44));
         editSaveButton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 editSaveButtonActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 12);
         jPanel5.add(editSaveButton, gridBagConstraints);
 
         newStudentButton.setText("New Student");
         newStudentButton.setMaximumSize(new java.awt.Dimension(121, 44));
         newStudentButton.setMinimumSize(new java.awt.Dimension(121, 44));
         newStudentButton.setPreferredSize(new java.awt.Dimension(121, 44));
         newStudentButton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 newStudentButtonActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 12);
         jPanel5.add(newStudentButton, gridBagConstraints);
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .addContainerGap(90, Short.MAX_VALUE)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(studentInfoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                             .add(paraprofessionalInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(courseInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(113, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(20, 20, 20)
                 .add(studentInfoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel2Layout.createSequentialGroup()
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(courseInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(12, 12, 12)
                         .add(paraprofessionalInfoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                         .add(18, 18, 18)
                         .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .add(0, 95, Short.MAX_VALUE))
         );
 
         tabsPane.addTab("Create", jPanel2);
 
         agendaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Today's Agenda"));
 
         agendaTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][]
             {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String []
             {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         agendaTableScrollPanel.setViewportView(agendaTable);
 
         addAgendaItemButton.setText("Add Item");
         addAgendaItemButton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 addAgendaItemButtonActionPerformed(evt);
             }
         });
 
         deleteAgendaButton.setText("Delete Item");
         deleteAgendaButton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 deleteAgendaButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout agendaPanelLayout = new org.jdesktop.layout.GroupLayout(agendaPanel);
         agendaPanel.setLayout(agendaPanelLayout);
         agendaPanelLayout.setHorizontalGroup(
             agendaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(agendaPanelLayout.createSequentialGroup()
                 .addContainerGap(138, Short.MAX_VALUE)
                 .add(agendaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(agendaTableScrollPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 892, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(agendaPanelLayout.createSequentialGroup()
                         .add(addAgendaItemButton)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(deleteAgendaButton)))
                 .addContainerGap(138, Short.MAX_VALUE))
         );
         agendaPanelLayout.setVerticalGroup(
             agendaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(agendaPanelLayout.createSequentialGroup()
                 .add(16, 16, 16)
                 .add(agendaTableScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(agendaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(deleteAgendaButton)
                     .add(addAgendaItemButton))
                 .addContainerGap())
         );
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(agendaPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(agendaPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabsPane.addTab("Agenda", jPanel1);
 
         sessionsTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Current Sessions"));
 
         sessionsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
         sessionsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
         sessionsTable.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
         sessionsTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][]
             {
 
             },
             new String []
             {
 
             }
         ));
         sessionsTable.setRowMargin(5);
         sessionsTable.setSurrendersFocusOnKeystroke(true);
         sessionsScrollPane.setViewportView(sessionsTable);
 
         deleteSessionButton.setText("Delete Session");
         deleteSessionButton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 deleteSessionButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout sessionsTablePanelLayout = new org.jdesktop.layout.GroupLayout(sessionsTablePanel);
         sessionsTablePanel.setLayout(sessionsTablePanelLayout);
         sessionsTablePanelLayout.setHorizontalGroup(
             sessionsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, sessionsTablePanelLayout.createSequentialGroup()
                 .add(0, 0, Short.MAX_VALUE)
                 .add(deleteSessionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
             .add(sessionsTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(sessionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1135, Short.MAX_VALUE)
                 .addContainerGap())
         );
         sessionsTablePanelLayout.setVerticalGroup(
             sessionsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(sessionsTablePanelLayout.createSequentialGroup()
                 .add(sessionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(deleteSessionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jTabbedPane1.addTab("Current", sessionsTablePanel);
 
         futureSessionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Future Sessions"));
 
         jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
         jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jScrollPane1.setPreferredSize(sessionsScrollPane.getMinimumSize());
 
         appointmentsTable.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
         appointmentsTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][]
             {
 
             },
             new String []
             {
 
             }
         ));
         jScrollPane1.setViewportView(appointmentsTable);
 
         deleteSessionButton1.setText("Delete Session");
         deleteSessionButton1.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 deleteSessionButton1ActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout futureSessionsPanelLayout = new org.jdesktop.layout.GroupLayout(futureSessionsPanel);
         futureSessionsPanel.setLayout(futureSessionsPanelLayout);
         futureSessionsPanelLayout.setHorizontalGroup(
             futureSessionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, futureSessionsPanelLayout.createSequentialGroup()
                 .add(0, 1011, Short.MAX_VALUE)
                 .add(deleteSessionButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
             .add(futureSessionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         futureSessionsPanelLayout.setVerticalGroup(
             futureSessionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, futureSessionsPanelLayout.createSequentialGroup()
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(deleteSessionButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jTabbedPane1.addTab("Future", futureSessionsPanel);
 
         org.jdesktop.layout.GroupLayout sessionsAndAgendaPanelLayout = new org.jdesktop.layout.GroupLayout(sessionsAndAgendaPanel);
         sessionsAndAgendaPanel.setLayout(sessionsAndAgendaPanelLayout);
         sessionsAndAgendaPanelLayout.setHorizontalGroup(
             sessionsAndAgendaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(sessionsAndAgendaPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(jTabbedPane1)
                 .addContainerGap())
         );
         sessionsAndAgendaPanelLayout.setVerticalGroup(
             sessionsAndAgendaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, sessionsAndAgendaPanelLayout.createSequentialGroup()
                 .addContainerGap(13, Short.MAX_VALUE)
                 .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 430, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         tabsPane.addTab("Sessions", sessionsAndAgendaPanel);
 
         reportsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
         reportsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
         reportsTopPane.setLayout(new java.awt.BorderLayout());
 
         tablePane.setPreferredSize(new java.awt.Dimension(1300, 258));
 
         generalReportTable2.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][]
             {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String []
             {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane8.setViewportView(generalReportTable2);
 
         generalReportBeginField.setText("mm/dd/yyyy hh:mm aa");
 
         jLabel5.setText("Begin");
 
         jLabel6.setText("End");
 
         generalReportEndField.setText("mm/dd/yyyy hh:mm aa");
 
         downloadButton1.setText("Download");
 
         generalReportLoadButton.setText("Load");
         generalReportLoadButton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 generalReportLoadButtonActionPerformed(evt);
             }
         });
 
         generalReportTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][]
             {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String []
             {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane11.setViewportView(generalReportTable);
 
         generalReportTable3.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][]
             {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String []
             {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane12.setViewportView(generalReportTable3);
 
         generalReportTable4.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][]
             {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String []
             {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane13.setViewportView(generalReportTable4);
 
         org.jdesktop.layout.GroupLayout tablePaneLayout = new org.jdesktop.layout.GroupLayout(tablePane);
         tablePane.setLayout(tablePaneLayout);
         tablePaneLayout.setHorizontalGroup(
             tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, tablePaneLayout.createSequentialGroup()
                 .add(tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(tablePaneLayout.createSequentialGroup()
                         .add(598, 598, 598)
                         .add(downloadButton1))
                     .add(tablePaneLayout.createSequentialGroup()
                         .add(183, 183, 183)
                         .add(jScrollPane8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 698, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jScrollPane13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 376, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jScrollPane12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 376, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(56, 56, 56))
             .add(tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(tablePaneLayout.createSequentialGroup()
                     .addContainerGap()
                     .add(tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                         .add(org.jdesktop.layout.GroupLayout.TRAILING, tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(generalReportEndField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(generalReportBeginField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .add(org.jdesktop.layout.GroupLayout.TRAILING, tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(tablePaneLayout.createSequentialGroup()
                                 .add(12, 12, 12)
                                 .add(jLabel5))
                             .add(tablePaneLayout.createSequentialGroup()
                                 .add(21, 21, 21)
                                 .add(jLabel6)))
                         .add(generalReportLoadButton))
                     .addContainerGap(1151, Short.MAX_VALUE)))
             .add(tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(tablePaneLayout.createSequentialGroup()
                     .add(185, 185, 185)
                     .add(jScrollPane11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 698, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(436, Short.MAX_VALUE)))
         );
         tablePaneLayout.setVerticalGroup(
             tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, tablePaneLayout.createSequentialGroup()
                 .addContainerGap(14, Short.MAX_VALUE)
                 .add(tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, tablePaneLayout.createSequentialGroup()
                         .add(jScrollPane12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(jScrollPane8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(18, 18, 18)
                 .add(downloadButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(17, 17, 17))
             .add(tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(tablePaneLayout.createSequentialGroup()
                     .addContainerGap()
                     .add(jLabel5)
                     .add(1, 1, 1)
                     .add(generalReportBeginField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(jLabel6)
                     .add(1, 1, 1)
                     .add(generalReportEndField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(18, 18, 18)
                     .add(generalReportLoadButton)
                     .addContainerGap(109, Short.MAX_VALUE)))
             .add(tablePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(tablePaneLayout.createSequentialGroup()
                     .add(16, 16, 16)
                     .add(jScrollPane11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(163, Short.MAX_VALUE)))
         );
 
         reportsTopPane.add(tablePane, java.awt.BorderLayout.PAGE_END);
 
         graphPane.setPreferredSize(new java.awt.Dimension(1300, 800));
 
         generalChartPanelLeft.setLayout(new java.awt.GridBagLayout());
 
         generalChartPanelMid.setLayout(new java.awt.GridBagLayout());
 
         generalChartPanelRight.setLayout(new java.awt.GridBagLayout());
 
         generalChartPanelLong.setLayout(new java.awt.GridBagLayout());
 
         generalChartPanelMid2.setLayout(new java.awt.GridBagLayout());
 
         generalChartPanelLeft2.setLayout(new java.awt.GridBagLayout());
 
         generalChartPanelRight2.setLayout(new java.awt.GridBagLayout());
 
         org.jdesktop.layout.GroupLayout graphPaneLayout = new org.jdesktop.layout.GroupLayout(graphPane);
         graphPane.setLayout(graphPaneLayout);
         graphPaneLayout.setHorizontalGroup(
             graphPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(graphPaneLayout.createSequentialGroup()
                 .add(16, 16, 16)
                 .add(generalChartPanelLeft, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 430, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(6, 6, 6)
                 .add(generalChartPanelMid, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 431, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(6, 6, 6)
                 .add(generalChartPanelRight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 430, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
             .add(graphPaneLayout.createSequentialGroup()
                 .add(10, 10, 10)
                 .add(generalChartPanelLeft2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 436, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(6, 6, 6)
                 .add(generalChartPanelMid2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 431, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(6, 6, 6)
                 .add(generalChartPanelRight2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 431, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
             .add(org.jdesktop.layout.GroupLayout.TRAILING, graphPaneLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(generalChartPanelLong, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
         graphPaneLayout.setVerticalGroup(
             graphPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(graphPaneLayout.createSequentialGroup()
                 .add(11, 11, 11)
                 .add(graphPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(generalChartPanelLeft, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 249, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(generalChartPanelMid, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 249, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(generalChartPanelRight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 249, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(6, 6, 6)
                 .add(graphPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(generalChartPanelLeft2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(generalChartPanelMid2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(generalChartPanelRight2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(6, 6, 6)
                 .add(generalChartPanelLong, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         reportsTopPane.add(graphPane, java.awt.BorderLayout.CENTER);
 
         reportsScrollPane.setViewportView(reportsTopPane);
 
         org.jdesktop.layout.GroupLayout reportsPaneLayout = new org.jdesktop.layout.GroupLayout(reportsPane);
         reportsPane.setLayout(reportsPaneLayout);
         reportsPaneLayout.setHorizontalGroup(
             reportsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 1192, Short.MAX_VALUE)
             .add(reportsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(reportsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1192, Short.MAX_VALUE))
         );
         reportsPaneLayout.setVerticalGroup(
             reportsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 449, Short.MAX_VALUE)
             .add(reportsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(org.jdesktop.layout.GroupLayout.TRAILING, reportsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE))
         );
 
         tabsPane.addTab("Reports", reportsPane);
 
         jPanel4.setBackground(new java.awt.Color(255, 255, 255));
         jPanel4.setLayout(new java.awt.BorderLayout());
 
         jLabel1.setBackground(new java.awt.Color(255, 255, 255));
         jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tutoring/images/pmslogoSmall.PNG"))); // NOI18N
         jPanel4.add(jLabel1, java.awt.BorderLayout.CENTER);
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(tabsPane)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(5, 5, 5)
                 .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 0, 0)
                 .add(tabsPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 495, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(50, 50, 50))
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void deleteSessionButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSessionButton1ActionPerformed
 
         int[] selectedRows =appointmentsTable.getSelectedRows();
 
         ((SessionTableModel) appointmentsTable.getModel()).deleteRows(selectedRows);
     }//GEN-LAST:event_deleteSessionButton1ActionPerformed
 
     private void deleteSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSessionButtonActionPerformed
         int[] selectedRows = sessionsTable.getSelectedRows();
 
         ((SessionTableModel) sessionsTable.getModel()).deleteRows(selectedRows);
     }//GEN-LAST:event_deleteSessionButtonActionPerformed
 
     private void newStudentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newStudentButtonActionPerformed
 
         NewClientObject ndo = new NewClientObject(new Frame(), true);
         ndo.setLocationRelativeTo(null);
         ndo.setVisible(true);
         
 
     }//GEN-LAST:event_newStudentButtonActionPerformed
 
     private void addSessionbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSessionbuttonActionPerformed
         //Paraprofessional t = new Paraprofessional(count, "TUTORFIRSTTEST", "TUTORLASTTEST", true);
         //Teacher teach = new Teacher(count, jComboBoxTeacher.getSelectedItem().toString(), "TestFirstName");
         //Subject sub = new Subject(count, jComboBoxCourse.getSelectedItem().toString(), "FullNameTest", new Category(count, "MABS"));
         getParaprofessionalSession(false);
     }//GEN-LAST:event_addSessionbuttonActionPerformed
 
     private void addSessionbuttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addSessionbuttonMouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_addSessionbuttonMouseClicked
 
     private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearForm();
     }//GEN-LAST:event_clearButtonActionPerformed
 
     private void clearButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearButtonMouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_clearButtonMouseClicked
 
     private void editSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSaveButtonActionPerformed
 
         //Check for errors and then save to database
         boolean updated = getParaprofessionalSession(true);
 
         if (updated)
         {
             editSaveButton.setVisible(false);
         }
     }//GEN-LAST:event_editSaveButtonActionPerformed
 
     private void teacherComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teacherComboActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_teacherComboActionPerformed
 
     private void addAgendaItemButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addAgendaItemButtonActionPerformed
     {//GEN-HEADEREND:event_addAgendaItemButtonActionPerformed
         NewAgendaObject ndo = new NewAgendaObject(new Frame(), true, Data.getAgendaCategoryList());
         ndo.setLocationRelativeTo(null);
         ndo.setVisible(true);
         System.out.println("HOPEFULLY NOT HERE YET");
         updateTables();
     }//GEN-LAST:event_addAgendaItemButtonActionPerformed
 
     private void deleteAgendaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAgendaButtonActionPerformed
 
         int[] selectedRows = agendaTable.getSelectedRows();
 
         ((AgendaTableModel) agendaTable.getModel()).deleteRows(selectedRows);
     }//GEN-LAST:event_deleteAgendaButtonActionPerformed
 
     private void generalReportLoadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generalReportLoadButtonActionPerformed
     {//GEN-HEADEREND:event_generalReportLoadButtonActionPerformed
         try
         {
             String begin = generalReportBeginField.getText().trim();
             String end = generalReportEndField.getText().trim();
 
             boolean isDateBegin = Validate.validateTimestamp(begin);
             boolean isDateEnd = Validate.validateTimestamp(end);
             SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.ENGLISH);
             Timestamp beginDate = null;
             Timestamp endDate = null;
             if (isDateBegin)
             {
                 beginDate = new Timestamp(sdf.parse(begin).getTime());
             }
             if (isDateEnd)
             {
                 endDate = new Timestamp(sdf.parse(end).getTime());
             }
 
             if (beginDate != null && endDate != null && beginDate.before(endDate))
             {
                 DatabaseHelper.open();
                 String[][] data = DatabaseHelper.getDataFromRegularQuery(
                     "SELECT "
                     + "abbrevName,"
                     + "COUNT(paraprofessionalSessionID) as 'Total Sessions',"
                     + "Sum(IF( TIMESTAMPDIFF("
                     + "MINUTE , sessionStart, sessionEnd ) >30, TIMESTAMPDIFF( "
                     + "MINUTE , sessionStart, sessionEnd ) /30, 1)) AS '30-min. Sessions', "
                     + "Sum(IF( TIMESTAMPDIFF( "
                     + "MINUTE , sessionStart, sessionEnd ) >30, TIMESTAMPDIFF( "
                     + "MINUTE , sessionStart, sessionEnd ) /30, 1))/count(paraprofessionalSessionID) as 'Avg. Session/Visit', "
                     + "SUM(walkout) as 'Walkouts', "
                     + "SUM(TIMESTAMPDIFF(MINUTE , timeAndDateEntered, sessionStart)) as 'Total Wait Time', "
                     + "SUM(TIMESTAMPDIFF(MINUTE , timeAndDateEntered, sessionStart))/COUNT(paraprofessionalSessionID) as 'Avg. Wait Time' "
                     + "FROM ParaprofessionalSession ps "
                     + "join Course c on ps.courseID=c.courseID "
                     + "join Subject s on c.subjectID=s.subjectID "
                     + "where "
                     + "timeAndDateEntered "
                     + "between "
                     + "'" + beginDate.toString() + "' and '" + endDate.toString() + "'"
                     + "group by abbrevName");
 
                 String[][] categoryData = DatabaseHelper.getDataFromRegularQuery(
                     "select c.name, count(paraprofessionalSessionID) as '# of Sessions'"
                     + " from ParaprofessionalSession ps"
                     + " join Course course on course.courseID=ps.courseID"
                     + " join Subject s on course.subjectID=s.subjectID"
                     + " join Category c on s.categoryID=c.categoryID "
                     + "where "
                     + "timeAndDateEntered "
                     + "between "
                     + "'" + beginDate.toString() + "' and '" + endDate.toString() + "'"
                     + " group by c.name");
 
                 String[][] otherValues = DatabaseHelper.getDataFromRegularQuery(
                     "SELECT "
                     + "SUM(walkout) as 'Walkouts', "
                     + "COUNT(paraprofessionalID) as 'Total Students', "
                     + "Sum(IF( TIMESTAMPDIFF("
                     + "MINUTE , sessionStart, sessionEnd ) >30, TIMESTAMPDIFF( "
                     + "MINUTE , sessionStart, sessionEnd ) /30, 1)) AS 'Total Sessions' "+ "FROM ParaprofessionalSession "
                     + "where "
                     + "timeAndDateEntered "
                     + "between "
                     + "'" + beginDate.toString() + "' and '" + endDate.toString() + "'"
                 );
 
                 String[][] studentMinutes = DatabaseHelper.getDataFromRegularQuery(
                     "SELECT "
                     + "Sum(IF( TIMESTAMPDIFF(MINUTE, sessionStart, sessionEnd ) < 10"
                     + " and TIMESTAMPDIFF(MINUTE, sessionStart, sessionEnd ) > 0, 1, 0))"
                     + " AS '<10 Min. Sessions', "
                     + "Sum(IF( TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) >= 10"
                     + " and TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) < 25, 1, 0))"
                     + " AS '10-24 Min. Sessions', "
                     + "Sum(IF( TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) >= 25"
                     + " and TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) <= 35, 1, 0))"
                     + " AS '25-35 Min. Sessions', "
                     + "Sum(IF( TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) > 35"
                     + " and TIMESTAMPDIFF(MINUTE , sessionStart, sessionEnd ) <= 60, 1, 0))"
                     + " AS '36-60 Min. Sessions' "+ "FROM ParaprofessionalSession ps "
                     + "where "
                     + "timeAndDateEntered "
                     + "between "
                     + "'" + beginDate.toString() + "' and '" + endDate.toString() + "'"
                 );
 
                 DatabaseHelper.close();
                 displayCharts(data, categoryData, otherValues, studentMinutes);
 
             }
 
         } catch (Exception e)
         {
             System.out.println("EXCEPTION on load");
         }
     }//GEN-LAST:event_generalReportLoadButtonActionPerformed
 
     private void close()
     {
         Window win = SwingUtilities.getWindowAncestor(this);
         if (win != null) {
            win.dispose();
         }
     }
     
     
     private int timeTillLogoff = 30*1000;
     private void formMouseMoved(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseMoved
     {//GEN-HEADEREND:event_formMouseMoved
         timeTillLogoff = 30*1000;
         threadLogoff.interrupt();
         System.out.println(threadLogoff.getState().toString());
         threadLogoff.stop();
         threadLogoff.start();
         
     }//GEN-LAST:event_formMouseMoved
 
     
     private void clearForm()
     {
         clearComboBoxes();
         sessionstartField.setText("mm/dd/yyyy hh:mm aa");
         sessionendField.setText("mm/dd/yyyy hh:mm aa");
         editSaveButton.setVisible(false);
         notesField.setText("");
         gcCheck.setSelected(false);
         walkoutCheck.setSelected(false);
     }
     private boolean getParaprofessionalSession(boolean isUpdating)
     {
         try
         {
             boolean clientPanelCheck = true;
             boolean coursePanelCheck = true;
             boolean paraprofessionalPanelCheck = true;
             boolean creatorPanelCheck = true;
             boolean locationPanelCheck = true;
           //  boolean otherPanelCheck = true;
             
             courseCombo.setBorder(null);
             teacherCombo.setBorder(null);
             levelCombo.setBorder(null);
             paraprofessionalCombo.setBorder(null);
             creatorCombo.setBorder(null);
             fnameCombo.setBorder(null);
             lnameCombo.setBorder(null);
             locationCombo.setBorder(null);
             sessionendField.setBorder(null);
             sessionstartField.setBorder(null);
             studentInfoPanel.repaint();
            /// otherInfoPanel.repaint();
             courseInfoPanel.repaint();
            /// creatorInfoPanel.repaint();
             paraprofessionalInfoPanel.repaint();
            /// locationInfoPanel.repaint();
             
             //courseInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
             //studentInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
            // otherInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
            
             String course = ((JTextComponent)courseCombo.getEditor().getEditorComponent()).getText();
             if(course.length() <= 0)
             {
                 courseCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 coursePanelCheck = false;
             }
              String tname = ((JTextComponent)teacherCombo.getEditor().getEditorComponent()).getText();
             if(tname.length() <= 0)
             {
                 teacherCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 coursePanelCheck = false;
             }
             
                 
             
             String level = ((JTextComponent)levelCombo.getEditor().getEditorComponent()).getText();
             Integer intLevel = null;
             try
             {
                 intLevel = Integer.parseInt(level);
                 
             }
             catch(Exception z)
             {
                 levelCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 coursePanelCheck = false;
             }
             
             String pName = ((JTextComponent)paraprofessionalCombo.getEditor().getEditorComponent()).getText();
             if(pName.length() <= 0)
             {
                 paraprofessionalCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 paraprofessionalPanelCheck = false;
             }
             
             String cName = ((JTextComponent)creatorCombo.getEditor().getEditorComponent()).getText();
             if(cName.length() <= 0)
             {
                 creatorCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 creatorPanelCheck = false;
             }
             
             String clientFName = ((JTextComponent)fnameCombo.getEditor().getEditorComponent()).getText();
             if(clientFName.length() <= 0)
             {
                 fnameCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 clientPanelCheck = false;
             }
             String clientLName = ((JTextComponent)lnameCombo.getEditor().getEditorComponent()).getText();
             if(clientLName.length() <= 0)
             {
                 lnameCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 clientPanelCheck = false;
             }
 
             
             boolean GC = gcCheck.isSelected();
             String notes = notesField.getText().toString();
             
             String location = ((JTextComponent)locationCombo.getEditor().getEditorComponent()).getText();
             if(location.length() <= 0)
             {
                 locationCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 locationPanelCheck = false;
             }
             
             
             SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.ENGLISH);
             Timestamp sessionStart = null;
             Timestamp sessionEnd = null;
             boolean hasSessionStart = true;
             
             if(sessionstartField.getText().trim().length() > 0 && !sessionstartField.getText().trim().equals("mm/dd/yyyy hh:mm aa"))
             {
                 hasSessionStart= Validate.validateTimestamp(sessionstartField.getText().trim());
  
                 if(hasSessionStart)
                     sessionStart = new Timestamp(sdf.parse(sessionstartField.getText().trim()).getTime());
                 else
                 {
                     sessionstartField.setBorder(new MatteBorder(3,3,3,3,Color.red));
                 }
             }
          
             boolean hasSessionEnd = true;
             if(sessionendField.getText().trim().length() > 0 && !sessionendField.getText().trim().equals("mm/dd/yyyy hh:mm aa"))
             {
                 hasSessionEnd = Validate.validateTimestamp(sessionendField.getText().trim());
                 if(!hasSessionEnd)
                 {
                     sessionendField.setBorder(new MatteBorder(3,3,3,3,Color.red));
                     
                 }    
                 else
                     sessionEnd = new Timestamp(sdf.parse(sessionendField.getText().trim()).getTime());
             }
             
             if((!hasSessionStart || !hasSessionEnd) || (!hasSessionStart && hasSessionEnd))
                 throw new ParseException("parse exception with timestamp", 0);
                 
  
             boolean walkout = walkoutCheck.isSelected();
 
            // String term = jComboBoxTerm.getSelectedItem().toString();
 
             String abbrevNameString = Subject.SubjectTable.ABBREVNAME.getWithAlias();
             String teachFNameString = Teacher.TeacherTable.FNAME.getWithAlias();
             String teachLNameString = Teacher.TeacherTable.LNAME.getWithAlias();
             String subjectIDString = Course.CourseTable.SUBJECTID.getWithAlias();
             String paraFNameString = Paraprofessional.ParaTable.FNAME.getWithAlias();
             String paraLNameString = Paraprofessional.ParaTable.LNAME.getWithAlias();
             String teachIDString = Course.CourseTable.TEACHERID.getWithAlias();
             String levelString = Course.CourseTable.LEVEL.getWithAlias();
             String clientFNameString = Client.ClientTable.FNAME.getWithAlias();
             String clientLNameString = Client.ClientTable.LNAME.getWithAlias();
             
             DatabaseHelper.open();
 
             ArrayList<Subject> subjects =(ArrayList<Subject>) Subject.selectAllSubjects("where "+abbrevNameString+"='"+course+"'", DatabaseHelper.getConnection());
             
             ArrayList<Teacher> teachers = (ArrayList<Teacher>) Teacher.selectAllTeacher("where concat(concat("+teachFNameString+",' '),"+teachLNameString+")='"+tname.trim()+"'", DatabaseHelper.getConnection());
             ArrayList<Course> courses = null;
             try
             {
                 String query = "where "+subjectIDString+"="+subjects.get(0).getSubjectID()+" and "+teachIDString+"="+teachers.get(0).getTeacherID() + " and "+levelString+"="+intLevel.intValue();
                 System.out.println(query);
                 courses = (ArrayList<Course>)Course.selectAllCourse(query, DatabaseHelper.getConnection());
                 
             }
             catch(Exception z)
             {
                 z.printStackTrace();
                 courses = new ArrayList<Course>();
                 //coursePanelCheck = true;
             }
             ArrayList<Paraprofessional> paraprofessionals = (ArrayList<Paraprofessional>)Paraprofessional.selectAllParaprofessional("where concat(concat("+paraFNameString+",' '),"+paraLNameString+")='"+pName.trim()+"'", DatabaseHelper.getConnection());
 
             ArrayList<Paraprofessional> creators = (ArrayList<Paraprofessional>)Paraprofessional.selectAllParaprofessional("where concat(concat("+paraFNameString+",' '),"+paraLNameString+")='"+cName.trim()+"'", DatabaseHelper.getConnection());
 
             ArrayList<Client> clients = (ArrayList<Client>)Client.selectAllClients("where "+clientFNameString+"='"+clientFName.trim()+"' and "+clientLNameString+"='"+clientLName.trim()+"'", DatabaseHelper.getConnection());
 
             ArrayList<Location> locations = (ArrayList<Location>)Location.selectAllLocation("where "+Location.LocationTable.NAME.getWithAlias()+"='"+location.trim()+"'", DatabaseHelper.getConnection());
 
             courseInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
             studentInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
          ///   otherInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
          ///   creatorInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
             paraprofessionalInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
          ///   locationInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
             
                
             System.out.println("CLIENTS SIZE: "+clients.size());
             if(subjects.size() <= 0 && coursePanelCheck)
             {
                 System.out.println("Subjects less than 1");
                 courseInfoPanel.setBackground(Color.red);
             }
             if(teachers.size() <= 0 && coursePanelCheck)
             {
                 System.out.println("Teachers less than 1");
                 courseInfoPanel.setBackground(Color.red);
             }
             if(courses.size() <= 0 && coursePanelCheck)
             {
                 System.out.println("Courses less than 1");
                 courseInfoPanel.setBackground(Color.red);
             }
             if(paraprofessionals.size() <= 0 && paraprofessionalPanelCheck)
             {
                 System.out.println("Paraprofessionals less than 1");
                 paraprofessionalInfoPanel.setBackground(Color.red);
             }
             if(creators.size() <= 0 && creatorPanelCheck)
             {
                 System.out.println("Creators less than 1");
                 creatorCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
             ///    creatorInfoPanel.setBackground(Color.red);
             }
             if(clients.size() <= 0 && clientPanelCheck)
             {
                 System.out.println("Clients less than 1");
                 studentInfoPanel.setBackground(Color.red);
             }
             if(locations.size() <= 0 && locationPanelCheck)
             {
                 System.out.println("Locations less than 1");
                 locationCombo.setBorder(new MatteBorder(3,3,3,3,Color.red));
               ///  locationInfoPanel.setBackground(Color.red);
             }
           //  if(terms.size() <= 0)
           //      System.out.println("Terms less than 1");
             Timestamp now = new Timestamp((new Date()).getTime());
 
             String sessionStartString;
             if(sessionStart != null)
                 sessionStartString = "='"+sessionStart+"'";
             else
                 sessionStartString = " is null";
             
             String sessionEndString;
             if(sessionEnd != null)
                 sessionEndString = "='"+sessionEnd+"'";
             else
                 sessionEndString = " is null";
             
             ParaprofessionalSession ps = new ParaprofessionalSession(-1, paraprofessionals.get(0), clients.get(0), courses.get(0), locations.get(0), creators.get(0), now, sessionStart, sessionEnd, GC, notes, walkout);
             String paraIDString = ParaprofessionalSession.ParaSessTable.PARAPROFESSIONALID.getWithAlias();
             String courseIDString = ParaprofessionalSession.ParaSessTable.COURSEID.getWithAlias();
             String locationIDString = ParaprofessionalSession.ParaSessTable.LOCATIONID.getWithAlias();
             String creatorIDString = ParaprofessionalSession.ParaSessTable.PARAPROFESSIONALCREATORID.getWithAlias();
             String enteredString = ParaprofessionalSession.ParaSessTable.TIMEANDDATEENTERED.getWithAlias();
             String sessStartString = ParaprofessionalSession.ParaSessTable.SESSIONSTART.getWithAlias();
             String sessEndString = ParaprofessionalSession.ParaSessTable.SESSIONEND.getWithAlias();
             String gcString = ParaprofessionalSession.ParaSessTable.GRAMMARCHECK.getWithAlias();
             String walkoutString = ParaprofessionalSession.ParaSessTable.WALKOUT.getWithAlias();
             String notesString = ParaprofessionalSession.ParaSessTable.NOTES.getWithAlias();
             String clientIDString = ParaprofessionalSession.ParaSessTable.CLIENTID.getWithAlias();
             if(!isUpdating)
             {
                 DatabaseHelper.insert(ParaprofessionalSession.getValues(ps), ParaprofessionalSession.ParaSessTable.getTable());
                // HibernateTest.create(ps);
 
                 System.out.println("NOW: "+now.toString());
                 
                 String query =  "where "+paraIDString+"="+paraprofessionals.get(0).getParaprofessionalID()+" and "+clientIDString+"="+clients.get(0).getClientID()+" and "+courseIDString+"="+courses.get(0).getCourseID()+" and "+locationIDString+"="+locations.get(0).getLocationID()+" and "+creatorIDString+"="+creators.get(0).getParaprofessionalID()+" and "+enteredString+"='"+now.toString()+"' and "+sessStartString+""+sessionStartString+" and "+sessEndString+""+sessionEndString+" and "+gcString+"="+GC+" and "+notesString+"='"+notes+"' and "+walkoutString+"="+walkout;
                 System.out.println(query);
                 ArrayList<ParaprofessionalSession> sessions = ParaprofessionalSession.selectAllParaprofessionalSession(query, DatabaseHelper.getConnection()); // (ArrayList<ParaprofessionalSession>)HibernateTest.select(query);
 
                 if(sessions.size() <=0)
                 {
                     System.out.println("SESSION WAS NOT CREATED ERROR");
                 }
                 else
                 {
 
                     System.out.println("ID: "+sessions.get(0).getParaprofessionalSessionID());
                 }
                 ((SessionTableModel) sessionsTable.getModel()).addRow(sessions.get(0));
             }
             else
             {
                 ps.setParaprofessionalSessionID(sessionID);
                 sessionID = -1;
                     
                 DatabaseHelper.update(ParaprofessionalSession.getValues(ps), ParaprofessionalSession.ParaSessTable.getTable());
 
                 System.out.println("NOW: "+now.toString());
                 String query =  "where "+clientIDString+"="+clients.get(0).getClientID()+" and "+courseIDString+"="+courses.get(0).getCourseID()+" and "+locationIDString+"="+locations.get(0).getLocationID()+" and "+creatorIDString+"="+creators.get(0).getParaprofessionalID()+" and "+enteredString+"='"+now.toString()+"' and "+sessStartString+""+sessionStartString+" and "+sessEndString+""+sessionEndString+" and "+gcString+"="+GC+" and "+notesString+"='"+notes+"' and "+walkoutString+"="+walkout;
 
                // String query = "where ps.clientID="+clients.get(0).getClientID()+" and ps.courseID="+courses.get(0).getCourseID()+" and ps.locationID="+locations.get(0).getLocationID()+" and ps.paraprofessionalCreatorID="+creators.get(0).getParaprofessionalID()+" and ps.timeAndDateEntered='"+now.toString()+"' and ps.sessionStart"+sessionStartString+" and ps.sessionEnd"+sessionEndString+" and ps.grammarCheck="+GC+" and ps.notes='"+notes+"' and ps.walkout="+walkout;
 
                 System.out.println(query);
                 ArrayList<ParaprofessionalSession> sessions = (ArrayList<ParaprofessionalSession>)ParaprofessionalSession.selectAllParaprofessionalSession(query,DatabaseHelper.getConnection());
 
                 if(sessions.size() <=0)
                 {
                     System.out.println("SESSION WAS NOT CREATED ERROR");
                 }
                 else if(sessions.size() > 1)
                 {
                     System.out.println("ID: "+sessions.get(0).getParaprofessionalSessionID());
                 }
                 else
                 {
                     //HibernateTest.update(ps);
                     //!!!
                     //Call reload data
                     //!!!
                     //Remove when finished
                 }
             }
 
             sessionsTable.repaint();
             
             
             Thread thread = new Thread(){
                 public void run(){
                      int colorOfGreen = 160;
 
                     while(colorOfGreen < 255)
                     {
                         //sessionsAndAgendaPanel.setBackground(new Color( 0,colorOfGreen,0));
                         courseInfoPanel.setBackground(new Color( 0,colorOfGreen,0));
                         studentInfoPanel.setBackground(new Color(0,colorOfGreen,0));
                        /// otherInfoPanel.setBackground(new Color(0,colorOfGreen,0));
                       ///  creatorInfoPanel.setBackground(new Color(0,colorOfGreen,0));
                         paraprofessionalInfoPanel.setBackground(new Color(0,colorOfGreen,0));
                       ///  locationInfoPanel.setBackground(new Color(0,colorOfGreen,0));
                         colorOfGreen += 2;
                         try {
                             this.sleep(13);
                         } catch (InterruptedException ex) {
                         }
                     }  
                     //sessionsAndAgendaPanel.setBackground(sessionsTablePanel.getBackground());
                     courseInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
                     studentInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
                    /// otherInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground()); 
                    /// creatorInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
                     paraprofessionalInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
                    /// locationInfoPanel.setBackground(sessionsAndAgendaPanel.getBackground());
                     updateTables();
                     clearForm();
                 }
             };
 
             thread.start();
             
             return true;
         }
         catch(Exception e)
         {
             System.out.println("ERROR ADDING SESSION"+e.getMessage() +"\n\n");
             e.printStackTrace();
             return false;
         } 
         finally
         {
             DatabaseHelper.close();
         }
         
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[])
     {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try
         {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
             {
                 if ("Nimbus".equals(info.getName()))
                 {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex)
         {
             java.util.logging.Logger.getLogger(SIAView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex)
         {
             java.util.logging.Logger.getLogger(SIAView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex)
         {
             java.util.logging.Logger.getLogger(SIAView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex)
         {
             java.util.logging.Logger.getLogger(SIAView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable()
         {
             public void run()
             {
                 new SIAView().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel ParaprofessionalLabel;
     private javax.swing.JButton addAgendaItemButton;
     private javax.swing.JButton addSessionbutton;
     private javax.swing.JPanel agendaPanel;
     private javax.swing.JTable agendaTable;
     private javax.swing.JScrollPane agendaTableScrollPanel;
     private javax.swing.JTable appointmentsTable;
     private javax.swing.JButton clearButton;
     private javax.swing.JComboBox courseCombo;
     private javax.swing.JPanel courseInfoPanel;
     private javax.swing.JLabel courseLabel;
     private javax.swing.JComboBox creatorCombo;
     private javax.swing.JLabel creatorLabel;
     private javax.swing.JButton deleteAgendaButton;
     private javax.swing.JButton deleteSessionButton;
     private javax.swing.JButton deleteSessionButton1;
     private javax.swing.JButton downloadButton1;
     private javax.swing.JButton editSaveButton;
     private javax.swing.JComboBox emailCombo;
     private javax.swing.JLabel emailLabel;
     private javax.swing.JComboBox fnameCombo;
     private javax.swing.JLabel fnameLabel;
     private javax.swing.JPanel futureSessionsPanel;
     private javax.swing.JCheckBox gcCheck;
     private javax.swing.JPanel generalChartPanelLeft;
     private javax.swing.JPanel generalChartPanelLeft2;
     private javax.swing.JPanel generalChartPanelLong;
     private javax.swing.JPanel generalChartPanelMid;
     private javax.swing.JPanel generalChartPanelMid2;
     private javax.swing.JPanel generalChartPanelRight;
     private javax.swing.JPanel generalChartPanelRight2;
     private javax.swing.JTextField generalReportBeginField;
     private javax.swing.JTextField generalReportEndField;
     private javax.swing.JButton generalReportLoadButton;
     private javax.swing.JTable generalReportTable;
     private javax.swing.JTable generalReportTable2;
     private javax.swing.JTable generalReportTable3;
     private javax.swing.JTable generalReportTable4;
     private javax.swing.JPanel graphPane;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane11;
     private javax.swing.JScrollPane jScrollPane12;
     private javax.swing.JScrollPane jScrollPane13;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JComboBox levelCombo;
     private javax.swing.JLabel levelLabel;
     private javax.swing.JComboBox lnameCombo;
     private javax.swing.JLabel lnameLabel;
     private javax.swing.JComboBox locationCombo;
     private javax.swing.JLabel locationLabel;
     private javax.swing.JButton newStudentButton;
     private javax.swing.JTextArea notesField;
     private javax.swing.JLabel notesLabel;
     private javax.swing.JComboBox paraprofessionalCombo;
     private javax.swing.JPanel paraprofessionalInfoPanel;
     private javax.swing.JComboBox phoneCombo;
     private javax.swing.JLabel phoneLabel;
     private javax.swing.JPanel reportsPane;
     private javax.swing.JScrollPane reportsScrollPane;
     private javax.swing.JPanel reportsTopPane;
     private javax.swing.JTextField sessionendField;
     private javax.swing.JLabel sessionendLabel;
     private javax.swing.JPanel sessionsAndAgendaPanel;
     private javax.swing.JScrollPane sessionsScrollPane;
     private javax.swing.JTable sessionsTable;
     private javax.swing.JPanel sessionsTablePanel;
     private javax.swing.JTextField sessionstartField;
     private javax.swing.JLabel sessionstartLabel;
     private javax.swing.JPanel studentInfoPanel;
     private javax.swing.JPanel tablePane;
     private javax.swing.JTabbedPane tabsPane;
     private javax.swing.JComboBox teacherCombo;
     private javax.swing.JLabel teacherLabel;
     private javax.swing.JCheckBox walkoutCheck;
     // End of variables declaration//GEN-END:variables
 }
