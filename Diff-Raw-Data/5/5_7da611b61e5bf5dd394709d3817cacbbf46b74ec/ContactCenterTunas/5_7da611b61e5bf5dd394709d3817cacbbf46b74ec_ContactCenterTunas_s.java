 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * ContactCenterTunas.java
  *
  * Created on Feb 15, 2010, 9:41:37 AM
  */
 
 package cc_tunas;
 
 import java.beans.PropertyVetoException;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.Timer;
 import java.awt.Event.*;
 import java.sql.*;
 import javax.sun.database.JavaConnector;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.io.File;
 import java.io.*;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.event.WindowEvent;
 import java.lang.Boolean;
 import java.text.SimpleDateFormat;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import java.net.*;
 import javax.swing.JFileChooser;
 
 import jxl.*;
 import jxl.write.*;
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 import jxl.write.Label;
 import jxl.write.Number;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 import jxl.write.biff.RowsExceededException;
 
 import javax.swing.UIManager.*;
 import javax.swing.table.DefaultTableCellRenderer;
 
 /**
  *
  * @author jsm
  */
 public class ContactCenterTunas extends javax.swing.JInternalFrame {
 
     public static int counter=0,c=0,m=0,sm=0,fx=0,tt=0,x=0;
     public static boolean inshow=false,outshow=false,ticshow=false,outbound=false,asshow=false;
     public static String s,loid,callid;
     public static boolean teleOn=false, uploOn=false, brcaOn=false;
     long elapsed;
     
     // TCP Components
    public static ServerSocket hostServer = null;
    public static ServerSocket hostServer1 = null;
    public static Socket sockettele = null;
    public static Socket socketbroad = null;
    public static Socket socketupload = null;
    public static Socket socketupload1 = null;
    public static BufferedReader intele = null;
    public static BufferedReader inbroad = null;
    public static BufferedReader inupload = null;
    public static BufferedReader inupload1 = null;
    public static PrintWriter outtele = null;
    public static PrintWriter outbroad = null;
    public static PrintWriter outupload = null;
    public static PrintWriter outupload1 = null;
    
    // Connection atate info
    public static String IPtele = "localhost";
    public static int porttele = 6020;
    public static String IPbroad = "192.168.0.83";
    public static int portbroad = 23;
   // public static int connectionStatus = DISCONNECTED;
    public static boolean isHost = true;
    //public static String statusString = statusMessages[connectionStatus];
    public static StringBuffer toAppend = new StringBuffer("");
    public static StringBuffer toSend = new StringBuffer("");
 
     /** Creates new form ContactCenterTunas */
     public static String in[]=new String[20];
     public static String ou[]=new String[17];
     public static String in2[]=new String[15];
     public static String in3[]=new String[15];
     public static String ou1[]=new String[15];
     public static String ou3[]=new String[15];
     public static String tic[]=new String[30];
     public static String reptic[]=new String[100];
     public static String repcal[]=new String[40];
     public static String repsms[]=new String[20];
     public static String repmail[]=new String[20];
     public static String repfax[]=new String[20];
     public static String act[]=new String[7];
     public static String hoin[]=new String[20];
     public static String hoou[]=new String[20];
     public static String perfin[]=new String[20];
     public static String perfou[]=new String[20];
     public static String dayin[]=new String[20];
     public static String dayou[]=new String[20];
     String date;
     String oldtext;
     String scroltext;
     String newtext;
     String pass=null;
     String user=null;
     public static String pabx;
     public static String in_ext;
     public static String out_ext;
     public static String lt;
     public static String ld;
     public static int v;
     Timer broad;
     Timer inbo;
     Timer receiv;
     Timer Scrol;
     public static Timer msg;
     private int usrlvl;
     public static String msn[]=new String[6];
     public static String msu[]=new String[6];
     private String msgidin;
     private String msgidou;
     private String tu;
 
     public ContactCenterTunas() {
         
         initComponents();
         conn=Log.conn;jconn=Log.jconn;
         setSize(1020,750);
         usrlvl();followUp();
         currentdate();
         tblin.setModel(tabin);
         tblout.setModel(tabou);
         tbltic.setModel(tabtic);
         tblticconf.setModel(tabticconf);
         tblact.setModel(tabact);
         tblmin.setModel(tabmin);
         tblmou.setModel(tabmou);
         tblsin.setModel(tabsin);
         tblsou.setModel(tabsou);
         tblfin.setModel(tabfin);
         tblfou.setModel(tabfou);
         tblreptic.setModel(tabreptic);
         tblrepcal.setModel(tabrepcal);
         tblrepsms.setModel(tabrepsms);
         tblrepmail.setModel(tabrepmail);
         tblrepfax.setModel(tabrepfax);
         tblmsin.setModel(tabmsin);
         tblmsou.setModel(tabmsou);
         tblhourin.setModel(tabhoin);
         tblhourout.setModel(tabhoou);
         tbldailyin.setModel(tabdayin);
         tbldailyout.setModel(tabdayou);
         tblperformin.setModel(tabperfin);
         tblperformout.setModel(tabperfou);
         tbin(tblin,new int []{130,100,70,85,85,85,60,70,75,120,120,70,90,90,90,550,35,175,150});
         tbin(tblout,new int []{130,110,100,100,110,130,110,110});
         tbin(tblmin,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100});
         tbin(tblmou,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100});
         tbin(tblsin,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100});
         tbin(tblsou,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100});
         tbin(tblfin,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100});
         tbin(tblfou,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100});
         tbin(tblreptic,new int []{85,85,120,150,150,150,175,225,150,120,85,120,120,100,100,275,500,120,120,375,150,150,150,225,80,375,85,100,120,150,100,100,150,100,125,120,110,100,100,150,100,500,50,60,50,60,50,60,50,60,150,150,150,150,150,150,300,120,120,120,120,100,100,150,100,100,300,500,85,85,90,100,150,85,85,90,100,85,85,90,85,100,90,85,150,125,150,85});
         tbin(tblrepcal,new int []{0,85,85,90,75,100,90,90,90,75,75,75,110,90,85,85,85,90,110,120,110,350,300,85,85,85,90,110,175,85,110,160,150});
         tbin(tblticconf,new int []{100,115,100,350,200,100,300,120,250,100,170,120,120,120});
         tbin(tbltic,new int []{100,100,100,120,100      ,150,200,200,150,300,100
                                 ,150,250,120,500,100        ,100,100,120,100,500
                                 ,50,50,55,55,-1,-1});
         tbin(tblrepsms,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});
         tbin(tblrepmail,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});
         tbin(tblrepfax,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});
         tbin(tblhourin,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});
         tbin(tblhourout,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});
         tbin(tbldailyin,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});
         tbin(tbldailyout,new int []{100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});
         tbin(tblperformin,new int []{100,100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});
         tbin(tblperformout,new int []{100,100,100,100,100,100
                 ,100,100,100,100,100
                 ,100,100,100,100,100});  
         opdt();
         optm();
         SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
         Date dtdt;
         try {
             dtdt = sdf.parse(opdt.substring(8, 10) + "/" + opdt.substring(5, 7) + "/" + opdt.substring(0, 4));
             dctic1.setDate(dtdt);            dctic2.setDate(dtdt);            dctic3.setDate(dtdt);            dctic4.setDate(dtdt);
             dctic5.setDate(dtdt);            dctic6.setDate(dtdt);            dctic7.setDate(dtdt);            dctic8.setDate(dtdt);
             dccal1.setDate(dtdt);            dccal2.setDate(dtdt);            dcfax1.setDate(dtdt);            dcfax2.setDate(dtdt);
             dcmail1.setDate(dtdt);            dcmail2.setDate(dtdt);            dcsms1.setDate(dtdt);            dcsms2.setDate(dtdt);
             dtmi.setDate(dtdt);            dtmi1.setDate(dtdt);            dtmo.setDate(dtdt);            dtmo1.setDate(dtdt);
             dtsi.setDate(dtdt);            dtsi1.setDate(dtdt);            dtso.setDate(dtdt);            dtso1.setDate(dtdt);
             dtfi.setDate(dtdt);            dtfi1.setDate(dtdt);            dtfo.setDate(dtdt);            dtfo1.setDate(dtdt);
             dtmsi.setDate(dtdt);            dtmsi1.setDate(dtdt);            dtmso.setDate(dtdt);            dtmso1.setDate(dtdt);
             dthi.setDate(dtdt);            dtho.setDate(dtdt);            dtdi.setDate(dtdt);            dtdi1.setDate(dtdt);
             dtdo.setDate(dtdt);            dtdo1.setDate(dtdt);            dtpi.setDate(dtdt);            dtpi1.setDate(dtdt);
             dtpo.setDate(dtdt);            dtpo1.setDate(dtdt);
         } catch (ParseException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
         tabelin();
         tabelou();
         tabelmsin();
         tabelmsou();
         tabeltic();
         tabelticconf();
         tabelact();
         tabelmin();
         tabelmou();
         call();
         sms();
         mail();
         fax();
         new Timer(1000, dating).start();        
         new Timer(100, blinking).start();        
         inbo=new Timer(1000, activ);
         receiv=new Timer(10, testing);
         Scrol=new Timer(40, tiscrol);
         msg=new Timer(1000, inbound);
 
         if(Log.version!=Log.Loc){
             if(!Log.data[0].equals("herfan")){
                 connect();
                 connecttele();
                 connectuploder();
             }
         }
 
         v=btncall.getDebugGraphicsOptions();
 //        System.out.print("\nusrlvl = "+usrlvl);
 //        if(usrlvl!=0){
         usr();
         btnrelease.setEnabled(true);
 //        }
         showCust();showDept();showStatus();
         jtab.setEnabledAt(7, false);
     }
 
         private ticket Tic;
     public ContactCenterTunas(ticket tic){
         this();
         this.Tic=tic;
     }
 
     public static login Log;
     public ContactCenterTunas(login log){
         this();
         this.Log=log;
     }
 
     private InBoundCall Inc;
     public ContactCenterTunas(InBoundCall inc){
         this();
         this.Inc=inc;
     }
 
     private OutBound Obc;
     public ContactCenterTunas(OutBound obc){
         this();
         this.Obc=obc;
     }
     
     private History Hic;
     public ContactCenterTunas(History hic){
         this();
         this.Hic=hic;
     }
 
     public Sms_income Sin;
     public ContactCenterTunas(Sms_income sin){
         this();
         this.Sin=sin;
     }
     public Email_incoming Ein;
     public ContactCenterTunas(Email_incoming ein){
         this();
         this.Ein=ein;
     }
     public Fax_incoming Fin;
     public ContactCenterTunas(Fax_incoming fin){
         this();
         this.Fin=fin;
     }
     public Asdept Asd;
     public ContactCenterTunas(Asdept asd){
         this();
         this.Asd=asd;
     }
     public Mssg Misg;
     public ContactCenterTunas(Mssg misg){
         this();
         this.Misg=misg;
     }
     
     public static void tbin(javax.swing.JTable tb, int lebar[]){
         tb.setAutoResizeMode(tb.AUTO_RESIZE_OFF);
         DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
         renderer.setHorizontalAlignment(JLabel.CENTER);
         int kolom=tb.getColumnCount();
         for (int i=0;i<kolom;i++){
             javax.swing.table.TableColumn tbc=tb.getColumnModel().getColumn(i);
             tbc.setPreferredWidth(lebar[i]);
 //            tbc.setHeaderRenderer(tb.setAlignmentX(JTable.CENTER_ALIGNMENT));
             tb.setAlignmentY(tb.CENTER_ALIGNMENT);
             tb.setRowHeight(18);
         }
     }
 
     public static javax.swing.table.DefaultTableModel getDefaultTabelin(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Time","User","Shift","Line number","Call status","Duration","Inquiry","Complaint","Blank Call","Caller number","Caller Name","Ticket No.","Status","Log ID","Call Type","Comment","Wrong Number","Cust Company","Inbound Type"/*,"Callback date","Callback time"*/}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false//,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     public static void tabelin(){
         tabin.setRowCount(0);
         x=0;
              try{
                 Date dt5 =dctic5.getDate();
                 Date dt6 =dctic6.getDate();
                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                 cal3 = sdf.format(dt5);
                 cal4 = sdf.format(dt6);
 //              sql="select a.log_time, a.username, a.shift, a.line_number, a._callstatus, a.duration, a._complaint, a._blankcall, a.caller_number, a.caller_name, b.ticket_no, b._status from log_phone a, tickets b where a.log_date = '"+ld+"'";
               sql="select log_phone.*, " +
                       "_callstatus.data as cllstt, " +
                       "substring(callback_time from 1 for 10) as cb_date, " +
                       "substring(callback_time from 12 for 8) as cb_time, " +
                       "shift.data as dshift, " +
                       "tickets.ticket_no as notic " +
                       "from log_phone " +
                       "join _callstatus on log_phone._callstatus=_callstatus.code " +
                       "join shift on log_phone.shift=shift.code " +
                       "left join tickets on log_phone.ticket_id=tickets.ticket_id " +
                       "where log_date between '"+cal3+"' and '"+cal4+"' and _direction=0 ";
                 condition="";
             if(!cbagenin.getSelectedItem().equals("--")){
                 condition=condition+" and username like '%"+cbagenin.getSelectedItem()+"%'";
             }
             if(!cbcalstatin.getSelectedItem().equals("--")){
                 condition=condition+" and _callstatus like '%"+cbcalstatin.getSelectedIndex()+"%'";
             }
 
             sql=sql+condition+" order by log_id";
               rs=jconn.SQLExecuteRS(sql, conn);
               System.out.println(sql);
 
             while(rs.next()){
 //                System.out.print("\nisi id ="+rs.getString(1));
                 in[x]=rs.getString(2)+" "+rs.getString(3);x++;
                 in[x]=rs.getString(4);x++;
                 in[x]=rs.getString("dshift");x++;
                 in[x]=rs.getString(7);x++;
                 in[x]=rs.getString("cllstt");x++;
                 in[x]=rs.getString(10);x++;
                 in[x]=rs.getString(15);x++;
                 in[x]=rs.getString(16);x++;
                 in[x]=rs.getString(17);x++;
                 in[x]=rs.getString(19);x++;
                 in[x]=rs.getString(21);x++;
                 in[x]=rs.getString("notic");x++;
                 in[x]=null;x++;
                 in[x]=rs.getString(1);x++;//logid
                 in[x]=rs.getString(20);x++;//call_type
                 in[x]=rs.getString(22);x++;//comment
                 in[x]=rs.getString(18);x++;//wromg_num
                 in[x]=rs.getString("cust_name");x++;//call_back
                 in[x]=rs.getString("inbound_type");x++;//call_back
                 in[x]=rs.getString(34);x++;//call_back
 //                System.out.print("\nisi calback time ="+rs.getString(25));
                 tabin.addRow(in);
                 x=0;
             }
               lblcalincount.setText(String.valueOf(tabin.getRowCount()));
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
 
 
     public static javax.swing.table.DefaultTableModel getDefaultTabelout(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Time","User","Shift","Line number","Duration","Phone number","Ticket No","Status"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     private void tabelou(){
         x=0;
         tabou.setRowCount(0);
         try{
             Date dt7 =dctic7.getDate();
             Date dt8 =dctic8.getDate();
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             cal5 = sdf.format(dt7);
             cal6 = sdf.format(dt8);
              int row=0;
               sql="select log_phone.*" +
                       ", tickets.ticket_no as notic" +
                       ", _ticketstatus.data as status" +
                       ", shift.data as dshift" +
                       " from log_phone" +
                       " left join tickets on log_phone.ticket_id=tickets.ticket_id"+
                       " left join _ticketstatus on tickets._status=_ticketstatus.code"+
                       " join shift on log_phone.shift=shift.code" +
                       " where log_date between '"+cal5+"' and '"+cal6+"' and _direction=1";
                   condition="";
             if(!cbagenou.getSelectedItem().equals("--")){
                 condition=condition+" and username like '%"+cbagenou.getSelectedItem()+"%'";
             }
             sql=sql+condition+" order by log_id";
               rs=jconn.SQLExecuteRS(sql, conn);
 //              System.out.println(sql);
             while(rs.next()){
                 ou[x]=rs.getString("log_time");x++;
                 ou[x]=rs.getString("username");x++;
                 ou[x]=rs.getString("dshift");x++;
                 ou[x]=rs.getString("line_number");x++;
                 ou[x]=rs.getString("duration");x++;
                 ou[x]=rs.getString("phone_number");x++;
                 ou[x]=rs.getString("notic");x++;
                 ou[x]=rs.getString("status");x++;
                 tabou.addRow(ou);
                 x=0;
             }
               lblcaloutcount.setText(String.valueOf(tabou.getRowCount()));
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabeltic(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
 //                new String [] {"Ticket No.","Status","Category","Assign Dept.","Assign User","Customer","Phone number","Username","No. Plat","Type","Driver","Phone","Ticket Id","GS","GT","STORING","OTHER"}){
                 new String [] {"Ticket No.","Priority","Type","Status","No. Plat","Open By"
                         ,"Department","Assign Dept.","Assign User","Category","Follow Up By","User name"
                         ,"Customer","Phone number","PIC","Jenis","Type"
                         ,"Tahun","Driver","Phone","Details","Solution","Ticket Id"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     private void tabeltic(){
         x=0;
         tabtic.setRowCount(0);
         try{
             Date dt3 =dctic3.getDate();
             Date dt4 =dctic4.getDate();
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             tic3 = sdf.format(dt3);
             tic4 = sdf.format(dt4);
 //              sql="select a.ticket_no, a._status, a.category, a.assign_dept, a.assign_username, a.cust_name, a.cust_phone, a.user, a.vehicle_platno, a.vehicle_type, a.driver_name, a.driver_phone, a.ticket_id from tickets a where a.open_date ='"+ld+"' ";
               sql="select a.ticket_no, e.data, a._type, b.data, g.no_plat, a.open_username, d.dept_name, c.dept_name, a.assign_username, a.category, a.follow_up, a.cso_name, a.cust_name, a.cust_phone, a.cust_pic, a.vehicle_jenis, a.vehicle_type, a.vehicle_tahun, a.driver_name, a.driver_phone, a.details, a._gt, a._gs, a._storing, a._other, a.solution, a.ticket_id " +
                       "from tickets a"
                       + " left join _ticketstatus b on a._status=b.code" +
                         " left join _department c on a.assign_dept=c.dept_id" +
                         " left join _department d on a.dept_id=d.dept_id" +
                         " left join _ticketpriority e on a._priority=e.code" +
                         " left join agreements f on a.agreement_id=f.agreement_id" +
                         " left join units g on f.unit_code=g.unit_code" +
                       " where ticket_id is not null";
               condition="";
             if(cktgl.isSelected()==true){
                 if(!dctic4.getDate().equals("")){
                     condition=condition+" and a.open_date between '"+tic3+"' and '"+tic4+"'";
                 }else{
                     condition=condition+" and a.open_date= '"+tic3+"'";
                     System.out.print(condition);
                 }
             }
             if(!txtticno1.getText().equals("")){
                 condition=condition+" and a.ticket_no like '%"+txtticno1.getText()+"%'";
             }
             if(!txtplatno.getText().equals("")){
                 condition=condition+" and g.no_plat like '%"+txtplatno.getText()+"%'";
             }
             if(!cbdept.getSelectedItem().equals("--")){
                 condition=condition+" and a.dept_id = '"+cbdept.getSelectedIndex()+"'";
             }
             if(!cbticstatus.getSelectedItem().equals("--")){
                 if(!cbticstatus.getSelectedItem().equals("CANCEL")){
                     condition=condition+" and a._status = '"+cbticstatus.getSelectedIndex()+"'";
                 }else{
                     condition=condition+" and a._status = '-1'";
                 }
             }
             if(!cbFollowUp.getSelectedItem().equals("--")){
                 condition=condition+" and a.follow_up = '"+cbFollowUp.getSelectedItem()+"'";
             }
             if(ckassign.isSelected()==true){
                 condition=condition+" and a.assign_dept=0";
             }
             if(ckstoring.isSelected()==true){
                 condition=condition+" and a._storing=1";
             }
             if(cksubmit.isSelected()==true){
                 condition=condition+" and a._submitted=0";
             }
             if(ckFCR.isSelected()==true){
                 condition=condition+" and a.confirm=1";
             }
             if(!txtcus.getText().equals("")){
                 condition=condition+" and a.cust_name like '%"+txtcus.getText()+"%'";
             }
             if(!txtdriv.getText().equals("")){
                 condition=condition+" and a.driver_name like '%"+txtdriv.getText()+"%'";
             }
             if(!cbcate.getSelectedItem().equals("--")){
                 condition=condition+" and a.category = '"+cbcate.getSelectedItem()+"'";
             }
             if(!txtdrivcode.getText().equals("")){
                 condition=condition+" and a.driver_code like '%"+txtdrivcode.getText()+"%'";
             }
             sql=sql+condition+" order by ticket_no";
             rs=jconn.SQLExecuteRS(sql, conn);
             System.out.print("\nserach tic "+sql);
 
             while(rs.next()){
                 tic[x]=rs.getString(1);x++;
                 tic[x]=rs.getString(2);x++;
                 tic[x]=rs.getString(3);x++;
                 tic[x]=rs.getString(4);x++;
                 tic[x]=rs.getString(5);x++;
                 tic[x]=rs.getString(6);x++;
                 tic[x]=rs.getString(7);x++;
                 tic[x]=rs.getString(8);x++;
                 tic[x]=rs.getString(9);x++;
                 tic[x]=rs.getString(10);x++;
                 tic[x]=rs.getString(11);x++;
                 tic[x]=rs.getString(12);x++;
                 tic[x]=rs.getString(13);x++;
                 tic[x]=rs.getString(14);x++;
                 tic[x]=rs.getString(15);x++;
                 tic[x]=rs.getString(16);x++;
                 tic[x]=rs.getString(17);x++;
                 tic[x]=rs.getString(18);x++;
                 tic[x]=rs.getString(19);x++;
                 tic[x]=rs.getString(20);x++;
                 tic[x]=rs.getString(21);x++;
 //                tic[x]=rs.getString(22);x++;
 //                tic[x]=rs.getString(23);x++;
 //                tic[x]=rs.getString(24);x++;
 //                tic[x]=rs.getString(25);x++;
                 tic[x]=rs.getString(26);x++;
                 tic[x]=rs.getString(27);x++;
                 tabtic.addRow(tic);
                 x=0;
             }
             lblticcount.setText(String.valueOf(tabtic.getRowCount()));
 //              System.out.println(tic[0]);
 //              System.out.println(tic[12]);
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabelsin(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Time","From","Status","Messages","Process By","Id","Cust Company"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
 
     private void tabelsin() {
           try{
               tabsin.setRowCount(0);
               int x=0;
               Date dt1 =dtsi.getDate();
               Date dt2 =dtsi1.getDate();
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
               sin = sdf.format(dt1);
               sin1= sdf.format(dt2);
               sql="select log_sms.*, rcvd_status.data as stt, " +
                       "tickets.ticket_no as notic " +
                       "from log_sms " +
                       "join rcvd_status on log_sms._status=rcvd_status.code " +
                       "left join tickets on log_sms.ticket_id=tickets.ticket_id " +
                       "where sms_date between '"+sin+"' and '"+sin1+"' and _direction=0 order by sms_id";
               rs=jconn.SQLExecuteRS(sql, conn);
               System.out.println(sql);
 
             while(rs.next()){
                 in2[x]=rs.getString("sms_date")+" "+rs.getString("sms_time");x++;
                 in2[x]=rs.getString("sms_from");x++;
                 in2[x]=rs.getString("stt");x++;
                 in2[x]=rs.getString("sms_text");x++;
                 in2[x]=rs.getString("username");x++;
                 in2[x]=rs.getString("sms_id");x++;
                 in2[x]=rs.getString("cust_name");x++;
                 tabsin.addRow(in2);
                 x=0;
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
 
     public static javax.swing.table.DefaultTableModel getDefaultTabelsou(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Time","To","Status","Messages","Sent By","No. Ticket"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     private void tabelsou(){
         try{
            tabsou.setRowCount(0);
            int x=0;
            Date dt1 =dtso.getDate();
            Date dt2 =dtso1.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sou = sdf.format(dt1);
            sou1= sdf.format(dt2);
            sql="select log_sms.*, send_status.data as stt, " +
                    "tickets.ticket_no as notic " +
                    "from log_sms " +
                    "join send_status on log_sms._status=send_status.code " +
                    "left join tickets on log_sms.ticket_id=tickets.ticket_id " +
                    "where sms_date between '"+sou+"' and '"+sou1+"' and _direction=1 order by sms_id";
            rs=jconn.SQLExecuteRS(sql, conn);
            System.out.println(sql);
 
            while(rs.next()){
                ou1[x]=rs.getString("sms_date")+" "+rs.getString("sms_time");x++;
                ou1[x]=rs.getString("sms_to");x++;
                ou1[x]=rs.getString("stt");x++;
                ou1[x]=rs.getString("sms_text");x++;
                ou1[x]=rs.getString("username");x++;
                ou1[x]=rs.getString("notic");x++;
                tabsou.addRow(ou1);x=0;
            }
        }catch(Exception exc){
            System.err.println(exc.getMessage());
        }
     }
 
     public static javax.swing.table.DefaultTableModel getDefaultTabelmin(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Date","Time","From","Subject","Status","username","Text","id","Cust Company"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     String min,min1,mou,mou1,sin,sin1,sou,sou1,fin,fin1,fou,fou1;
     public static String msin,msin1,msou,msou1;
     private void tabelmin() {
         try{
             tabmin.setRowCount(0);
             int x=0;
             Date dt1 =dtmi.getDate();
             Date dt2 =dtmi1.getDate();
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             min = sdf.format(dt1);
             min1= sdf.format(dt2);
             sql="select log_mail.*, " +
                     "rcvd_status.data as stt, " +
                     "tickets.ticket_no as notic " +
                     "from log_mail " +
                     "join rcvd_status on log_mail.status=rcvd_status.code " +
                     "left join tickets on log_mail.ticket_id=tickets.ticket_id " +
                     "where mail_date between '"+min+"' and '"+min1+"' and direction=0 order by mail_id";
             rs=jconn.SQLExecuteRS(sql, conn);
             System.out.println(sql);
 
             while(rs.next()){
                 in2[x]=rs.getString("mail_date");x++;
                 in2[x]=rs.getString("mail_time");x++;
                 in2[x]=rs.getString("mail_from");x++;
                 in2[x]=rs.getString("mail_subject");x++;
                 in2[x]=rs.getString("stt");x++;
                 in2[x]=rs.getString("username");x++;
                 in2[x]=rs.getString("mail_text");x++;
                 in2[x]=rs.getString("mail_id");x++;
                 in2[x]=rs.getString("cust_name");x++;
                 tabmin.addRow(in2);
                 x=0;
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabelmout(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Date","Time","To","Username","Subject","Ticket no","Status","Text","CC","id","Cust Company"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     private void tabelmou() {
         try{
             tabmou.setRowCount(0);
             int x=0;
             Date dt1 =dtmo.getDate();
             Date dt2 =dtmo1.getDate();
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             mou = sdf.format(dt1);
             mou1= sdf.format(dt2);
             sql="select log_mail.*, send_status.data as stt, " +
                     "tickets.ticket_no as notic " +
                     "from log_mail " +
                     "join send_status on log_mail.status=send_status.code " +
                     "left join tickets on log_mail.ticket_id=tickets.ticket_id " +
                     "where mail_date between '"+mou+"' and '"+mou1+"' and direction=1 order by mail_id";
             rs=jconn.SQLExecuteRS(sql, conn);
             System.out.println(sql);
 
             while(rs.next()){
                 ou1[x]=rs.getString("mail_date");x++;
                 ou1[x]=rs.getString("mail_time");x++;
                 ou1[x]=rs.getString("mail_to");x++;
                 ou1[x]=rs.getString("username");x++;
                 ou1[x]=rs.getString("mail_subject");x++;
                 ou1[x]=rs.getString("notic");x++;
                 ou1[x]=rs.getString("stt");x++;
                 ou1[x]=rs.getString("mail_text");x++;
                 ou1[x]=rs.getString("mail_cc");x++;
                 ou1[x]=rs.getString("mail_id");x++;
                 ou1[x]=rs.getString("cust_name");x++;
                 tabmou.addRow(ou1);
                 x=0;
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
 
     public static javax.swing.table.DefaultTableModel getDefaultTabelfin(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Time","Document name","Status","Process By","Id","Cust Company"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     private void tabelfin() {
         try{
             tabfin.setRowCount(0);
             int x=0;
             Date dt1 =dtfi.getDate();
             Date dt2 =dtfi1.getDate();
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
             SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
             fin = sdf.format(dt1);
             fin1= sdf1.format(dt2);
             sql="select log_fax.*, rcvd_status.data as stt, " +
                     "tickets.ticket_no as notic " +
                     "from log_fax " +
                     "join rcvd_status on log_fax._status=rcvd_status.code " +
                     "left join tickets on log_fax.ticket_id=tickets.ticket_id " +
                     "where rcvd_time between '"+fin+"' and '"+fin1+"' and _direction=0 order by fax_id";
             rs=jconn.SQLExecuteRS(sql, conn);
             System.out.println(sql);
 
             while(rs.next()){
                 in3[x]=rs.getString("rcvd_time");x++;
                 in3[x]=rs.getString("doc_name");x++;
                 in3[x]=rs.getString("stt");x++;
                 in3[x]=rs.getString("username");x++;
                 in3[x]=rs.getString("fax_id");x++;
                 in3[x]=rs.getString("cust_name");x++;
                 tabfin.addRow(in3);
                 x=0;
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabelfou(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Time","To","Document name","Status","Sent By","No. Ticket"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     private void tabelfou(){
         try{
             tabfou.setRowCount(0);
             int row=0;
             Date dt1 =dtfo.getDate();
             Date dt2 =dtfo1.getDate();
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
             SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
             fou = sdf.format(dt1);
             fou1= sdf1.format(dt2);
             sql="select log_fax.*, _faxstatus.data as stt, " +
                     "tickets.ticket_no as notic " +
                     "from log_fax " +
                     "join _faxstatus on log_fax._status=_faxstatus.code " +
                     "left join tickets on log_fax.ticket_id=tickets.ticket_id " +
                     "where sent_time between '"+fou+"' and '"+fou1+"' and _direction=1 order by fax_id";
             rs=jconn.SQLExecuteRS(sql, conn);
             System.out.println(sql);
 
             while(rs.next()){
                 ou3[0]=rs.getString("sent_time");
                 ou3[1]=rs.getString("recipient");
                 ou3[2]=rs.getString("doc_name");
                 ou3[3]=rs.getString("stt");
                 ou3[4]=rs.getString("username");
                 ou3[5]=rs.getString("notic");
                 tabfou.addRow(ou3);
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jdp = new javax.swing.JDesktopPane();
         lbldate = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         btncall = new javax.swing.JButton();
         btnsms = new javax.swing.JButton();
         btnmail = new javax.swing.JButton();
         btnlogout = new javax.swing.JButton();
         lbluser = new javax.swing.JLabel();
         lbllogo = new javax.swing.JLabel();
         btnoutbound = new javax.swing.JButton();
         lblpas = new javax.swing.JLabel();
         lblactivity = new javax.swing.JLabel();
         btnready = new javax.swing.JButton();
         cbdirection = new javax.swing.JComboBox();
         lblshift = new javax.swing.JLabel();
         lblshift1 = new javax.swing.JLabel();
         txtcalnoti = new javax.swing.JTextField();
         txtfaxnoti = new javax.swing.JTextField();
         txtsmsnoti = new javax.swing.JTextField();
         txtmailnoti = new javax.swing.JTextField();
         btnfax = new javax.swing.JButton();
         pnlscroll = new javax.swing.JPanel();
         lblscroll = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jLabel101 = new javax.swing.JLabel();
         lblA1 = new javax.swing.JLabel();
         jLabel102 = new javax.swing.JLabel();
         lblA2 = new javax.swing.JLabel();
         jLabel99 = new javax.swing.JLabel();
         jLabel104 = new javax.swing.JLabel();
         lblIn1 = new javax.swing.JLabel();
         jLabel103 = new javax.swing.JLabel();
         lblIn2 = new javax.swing.JLabel();
         jLabel106 = new javax.swing.JLabel();
         lblOu1 = new javax.swing.JLabel();
         jLabel100 = new javax.swing.JLabel();
         jLabel105 = new javax.swing.JLabel();
         lblOu2 = new javax.swing.JLabel();
         jPanel2 = new javax.swing.JPanel();
         jtab = new javax.swing.JTabbedPane();
         pnlinbon = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblin = new javax.swing.JTable();
         jLabel55 = new javax.swing.JLabel();
         jLabel56 = new javax.swing.JLabel();
         jLabel57 = new javax.swing.JLabel();
         btninsrch = new javax.swing.JButton();
         dctic5 = new com.toedter.calendar.JDateChooser();
         dctic6 = new com.toedter.calendar.JDateChooser();
         jLabel58 = new javax.swing.JLabel();
         cbcalstatin = new javax.swing.JComboBox();
         cbagenin = new javax.swing.JComboBox();
         lblcalincount = new javax.swing.JLabel();
         lblrepticcount12 = new javax.swing.JLabel();
         pnltic = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         tbltic = new javax.swing.JTable();
         jButton6 = new javax.swing.JButton();
         dctic3 = new com.toedter.calendar.JDateChooser();
         jLabel52 = new javax.swing.JLabel();
         btnticsrch = new javax.swing.JButton();
         dctic4 = new com.toedter.calendar.JDateChooser();
         jLabel54 = new javax.swing.JLabel();
         btnsenddept = new javax.swing.JButton();
         cktgl = new javax.swing.JCheckBox();
         jLabel73 = new javax.swing.JLabel();
         jLabel77 = new javax.swing.JLabel();
         jScrollPane37 = new javax.swing.JScrollPane();
         txtsolution = new javax.swing.JTextArea();
         jScrollPane38 = new javax.swing.JScrollPane();
         txtdetail = new javax.swing.JTextArea();
         lblticcount = new javax.swing.JLabel();
         lblrepticcount10 = new javax.swing.JLabel();
         jScrollPane47 = new javax.swing.JScrollPane();
         jPanel25 = new javax.swing.JPanel();
         txtticno1 = new javax.swing.JTextField();
         ckstoring = new javax.swing.JCheckBox();
         jLabel62 = new javax.swing.JLabel();
         ckassign = new javax.swing.JCheckBox();
         jLabel68 = new javax.swing.JLabel();
         jLabel63 = new javax.swing.JLabel();
         cbticstatus = new javax.swing.JComboBox();
         cbdept = new javax.swing.JComboBox();
         cksubmit = new javax.swing.JCheckBox();
         jLabel53 = new javax.swing.JLabel();
         txtplatno = new javax.swing.JTextField();
         jLabel94 = new javax.swing.JLabel();
         txtcus = new javax.swing.JTextField();
         txtdriv = new javax.swing.JTextField();
         jLabel95 = new javax.swing.JLabel();
         jLabel96 = new javax.swing.JLabel();
         cbcate = new javax.swing.JComboBox();
         txtdrivcode = new javax.swing.JTextField();
         jLabel97 = new javax.swing.JLabel();
         jLabel98 = new javax.swing.JLabel();
         cbFollowUp = new javax.swing.JComboBox();
         ckFCR = new javax.swing.JCheckBox();
         pnlact = new javax.swing.JPanel();
         jScrollPane5 = new javax.swing.JScrollPane();
         tblact = new javax.swing.JTable();
         btnrelease = new javax.swing.JButton();
         cbagenrelease = new javax.swing.JComboBox();
         panelsms = new javax.swing.JTabbedPane();
         pninbox = new javax.swing.JPanel();
         jScrollPane6 = new javax.swing.JScrollPane();
         tblsin = new javax.swing.JTable();
         dtsi = new com.toedter.calendar.JDateChooser();
         dtsi1 = new com.toedter.calendar.JDateChooser();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel47 = new javax.swing.JLabel();
         jLabel49 = new javax.swing.JLabel();
         jScrollPane19 = new javax.swing.JScrollPane();
         txtimsg2 = new javax.swing.JTextArea();
         txtfrom2 = new javax.swing.JTextField();
         btnsmsinsrch = new javax.swing.JButton();
         jLabel80 = new javax.swing.JLabel();
         cbcust = new javax.swing.JComboBox();
         btncussavesms = new javax.swing.JButton();
         jLabel91 = new javax.swing.JLabel();
         txtnoticsms = new javax.swing.JTextField();
         pnoutbox = new javax.swing.JPanel();
         jScrollPane7 = new javax.swing.JScrollPane();
         tblsou = new javax.swing.JTable();
         jLabel3 = new javax.swing.JLabel();
         dtso = new com.toedter.calendar.JDateChooser();
         jLabel4 = new javax.swing.JLabel();
         dtso1 = new com.toedter.calendar.JDateChooser();
         jLabel8 = new javax.swing.JLabel();
         jLabel35 = new javax.swing.JLabel();
         jScrollPane18 = new javax.swing.JScrollPane();
         txtimsg1 = new javax.swing.JTextArea();
         txtfrom1 = new javax.swing.JTextField();
         btnsmsoutsrch = new javax.swing.JButton();
         panelmail = new javax.swing.JTabbedPane();
         jPanel9 = new javax.swing.JPanel();
         jScrollPane8 = new javax.swing.JScrollPane();
         tblmin = new javax.swing.JTable();
         jLabel5 = new javax.swing.JLabel();
         txtfrom = new javax.swing.JTextField();
         jLabel11 = new javax.swing.JLabel();
         txtisu = new javax.swing.JTextField();
         jLabel12 = new javax.swing.JLabel();
         jScrollPane9 = new javax.swing.JScrollPane();
         txtimsg = new javax.swing.JTextArea();
         jLabel18 = new javax.swing.JLabel();
         dtmi = new com.toedter.calendar.JDateChooser();
         jLabel48 = new javax.swing.JLabel();
         dtmi1 = new com.toedter.calendar.JDateChooser();
         btnmailinsrch = new javax.swing.JButton();
         jLabel81 = new javax.swing.JLabel();
         cbcust1 = new javax.swing.JComboBox();
         btncussaveEmail = new javax.swing.JButton();
         jLabel92 = new javax.swing.JLabel();
         txtnoticmail = new javax.swing.JTextField();
         btnAttachment = new javax.swing.JButton();
         scpCcList1 = new javax.swing.JScrollPane();
         jList2 = new javax.swing.JList();
         jPanel10 = new javax.swing.JPanel();
         jScrollPane10 = new javax.swing.JScrollPane();
         tblmou = new javax.swing.JTable();
         txtoto = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         txtocc = new javax.swing.JTextField();
         jLabel14 = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         txtosu = new javax.swing.JTextField();
         jLabel16 = new javax.swing.JLabel();
         jScrollPane11 = new javax.swing.JScrollPane();
         txtomsg = new javax.swing.JTextArea();
         jLabel17 = new javax.swing.JLabel();
         txtidti = new javax.swing.JTextField();
         jLabel50 = new javax.swing.JLabel();
         dtmo = new com.toedter.calendar.JDateChooser();
         jLabel51 = new javax.swing.JLabel();
         dtmo1 = new com.toedter.calendar.JDateChooser();
         btnmailoutsrch = new javax.swing.JButton();
         scpCcList2 = new javax.swing.JScrollPane();
         jList3 = new javax.swing.JList();
         btnAttachment1 = new javax.swing.JButton();
         tabbpanereport = new javax.swing.JTabbedPane();
         pnlrep = new javax.swing.JPanel();
         jScrollPane4 = new javax.swing.JScrollPane();
         tblreptic = new javax.swing.JTable();
         btnreptic = new javax.swing.JButton();
         jLabel37 = new javax.swing.JLabel();
         jLabel38 = new javax.swing.JLabel();
         dctic1 = new com.toedter.calendar.JDateChooser();
         dctic2 = new com.toedter.calendar.JDateChooser();
         btnexporttic = new javax.swing.JButton();
         lblrepticcount = new javax.swing.JLabel();
         lblrepticcount1 = new javax.swing.JLabel();
         jScrollPane48 = new javax.swing.JScrollPane();
         jPanel26 = new javax.swing.JPanel();
         txtticno2 = new javax.swing.JTextField();
         ckstoring1 = new javax.swing.JCheckBox();
         jLabel107 = new javax.swing.JLabel();
         ckassign1 = new javax.swing.JCheckBox();
         jLabel108 = new javax.swing.JLabel();
         jLabel109 = new javax.swing.JLabel();
         cbticstatus1 = new javax.swing.JComboBox();
         cbdept1 = new javax.swing.JComboBox();
         cksubmit1 = new javax.swing.JCheckBox();
         jLabel110 = new javax.swing.JLabel();
         txtplatno1 = new javax.swing.JTextField();
         jLabel111 = new javax.swing.JLabel();
         txtcus1 = new javax.swing.JTextField();
         txtdriv1 = new javax.swing.JTextField();
         jLabel112 = new javax.swing.JLabel();
         jLabel113 = new javax.swing.JLabel();
         cbcate1 = new javax.swing.JComboBox();
         txtdrivcode1 = new javax.swing.JTextField();
         jLabel114 = new javax.swing.JLabel();
         jLabel115 = new javax.swing.JLabel();
         cbFollowUp1 = new javax.swing.JComboBox();
         ckFCR1 = new javax.swing.JCheckBox();
         pnlrep1 = new javax.swing.JPanel();
         jScrollPane13 = new javax.swing.JScrollPane();
         tblrepcal = new javax.swing.JTable();
         jLabel21 = new javax.swing.JLabel();
         jLabel22 = new javax.swing.JLabel();
         jLabel23 = new javax.swing.JLabel();
         btnrepcal = new javax.swing.JButton();
         jLabel24 = new javax.swing.JLabel();
         jLabel39 = new javax.swing.JLabel();
         dccal1 = new com.toedter.calendar.JDateChooser();
         jLabel40 = new javax.swing.JLabel();
         dccal2 = new com.toedter.calendar.JDateChooser();
         cbcaldir = new javax.swing.JComboBox();
         cbcalstat = new javax.swing.JComboBox();
         cbagenirepcal = new javax.swing.JComboBox();
         cbcaltyperepcal = new javax.swing.JComboBox();
         btnexportcall = new javax.swing.JButton();
         lblrepcalcount = new javax.swing.JLabel();
         lblrepticcount3 = new javax.swing.JLabel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel17 = new javax.swing.JPanel();
         jScrollPane39 = new javax.swing.JScrollPane();
         tblhourin = new javax.swing.JTable();
         jLabel78 = new javax.swing.JLabel();
         dthi = new com.toedter.calendar.JDateChooser();
         btnhi = new javax.swing.JButton();
         btnexportcall1 = new javax.swing.JButton();
         jTabbedPane4 = new javax.swing.JTabbedPane();
         jPanel21 = new javax.swing.JPanel();
         jScrollPane43 = new javax.swing.JScrollPane();
         tbldailyin = new javax.swing.JTable();
         jLabel82 = new javax.swing.JLabel();
         dtdi = new com.toedter.calendar.JDateChooser();
         btndi = new javax.swing.JButton();
         jLabel84 = new javax.swing.JLabel();
         dtdi1 = new com.toedter.calendar.JDateChooser();
         btnexportcall3 = new javax.swing.JButton();
         jTabbedPane3 = new javax.swing.JTabbedPane();
         jPanel19 = new javax.swing.JPanel();
         jScrollPane41 = new javax.swing.JScrollPane();
         tblperformin = new javax.swing.JTable();
         dtpi = new com.toedter.calendar.JDateChooser();
         btnpi1 = new javax.swing.JButton();
         jLabel86 = new javax.swing.JLabel();
         dtpi1 = new com.toedter.calendar.JDateChooser();
         jLabel88 = new javax.swing.JLabel();
         btnexportcall5 = new javax.swing.JButton();
         pnlrep2 = new javax.swing.JPanel();
         jScrollPane14 = new javax.swing.JScrollPane();
         tblrepsms = new javax.swing.JTable();
         txtsmsstat = new javax.swing.JTextField();
         jLabel25 = new javax.swing.JLabel();
         jLabel26 = new javax.swing.JLabel();
         btnrepsms = new javax.swing.JButton();
         jLabel28 = new javax.swing.JLabel();
         jLabel43 = new javax.swing.JLabel();
         dcsms1 = new com.toedter.calendar.JDateChooser();
         jLabel44 = new javax.swing.JLabel();
         dcsms2 = new com.toedter.calendar.JDateChooser();
         txtsmsticid = new javax.swing.JTextField();
         jLabel27 = new javax.swing.JLabel();
         cbdirrepsms = new javax.swing.JComboBox();
         cbagenirepcal1 = new javax.swing.JComboBox();
         btnexportsms = new javax.swing.JButton();
         lblrepsmscount = new javax.swing.JLabel();
         lblrepticcount5 = new javax.swing.JLabel();
         pnlrep3 = new javax.swing.JPanel();
         jScrollPane15 = new javax.swing.JScrollPane();
         tblrepmail = new javax.swing.JTable();
         jLabel29 = new javax.swing.JLabel();
         jLabel30 = new javax.swing.JLabel();
         txtmailticid = new javax.swing.JTextField();
         jLabel31 = new javax.swing.JLabel();
         btnrepmail = new javax.swing.JButton();
         jLabel32 = new javax.swing.JLabel();
         txtmailsub = new javax.swing.JTextField();
         jLabel45 = new javax.swing.JLabel();
         dcmail1 = new com.toedter.calendar.JDateChooser();
         jLabel46 = new javax.swing.JLabel();
         dcmail2 = new com.toedter.calendar.JDateChooser();
         cbdirmail = new javax.swing.JComboBox();
         cbagenrepmail = new javax.swing.JComboBox();
         btnexportmail = new javax.swing.JButton();
         lblrepmailcount = new javax.swing.JLabel();
         lblrepticcount7 = new javax.swing.JLabel();
         jTabbedPane2 = new javax.swing.JTabbedPane();
         pninbox1 = new javax.swing.JPanel();
         jScrollPane33 = new javax.swing.JScrollPane();
         tblmsin = new javax.swing.JTable();
         dtmsi = new com.toedter.calendar.JDateChooser();
         dtmsi1 = new com.toedter.calendar.JDateChooser();
         jLabel66 = new javax.swing.JLabel();
         jLabel67 = new javax.swing.JLabel();
         jLabel70 = new javax.swing.JLabel();
         jScrollPane34 = new javax.swing.JScrollPane();
         txtimsg3 = new javax.swing.JTextArea();
         btnreplymsg = new javax.swing.JButton();
         btndelmsg = new javax.swing.JButton();
         pnoutbox1 = new javax.swing.JPanel();
         jScrollPane35 = new javax.swing.JScrollPane();
         tblmsou = new javax.swing.JTable();
         jLabel71 = new javax.swing.JLabel();
         dtmso = new com.toedter.calendar.JDateChooser();
         jLabel72 = new javax.swing.JLabel();
         dtmso1 = new com.toedter.calendar.JDateChooser();
         jLabel74 = new javax.swing.JLabel();
         jScrollPane36 = new javax.swing.JScrollPane();
         txtimsg4 = new javax.swing.JTextArea();
         btncomposemsg = new javax.swing.JButton();
         btndelmsg1 = new javax.swing.JButton();
         pnlinf = new javax.swing.JTabbedPane();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jPanel4 = new javax.swing.JPanel();
         jScrollPane20 = new javax.swing.JScrollPane();
         jTextArea2 = new javax.swing.JTextArea();
         jPanel5 = new javax.swing.JPanel();
         jScrollPane21 = new javax.swing.JScrollPane();
         jTextArea3 = new javax.swing.JTextArea();
         jPanel6 = new javax.swing.JPanel();
         jScrollPane22 = new javax.swing.JScrollPane();
         jTextArea4 = new javax.swing.JTextArea();
         jPanel7 = new javax.swing.JPanel();
         jScrollPane23 = new javax.swing.JScrollPane();
         jTextArea5 = new javax.swing.JTextArea();
         jPanel23 = new javax.swing.JPanel();
         jScrollPane45 = new javax.swing.JScrollPane();
         jTextArea11 = new javax.swing.JTextArea();
         jPanel8 = new javax.swing.JPanel();
         jScrollPane24 = new javax.swing.JScrollPane();
         jTextArea6 = new javax.swing.JTextArea();
         jPanel11 = new javax.swing.JPanel();
         jScrollPane25 = new javax.swing.JScrollPane();
         jTextArea7 = new javax.swing.JTextArea();
         jPanel12 = new javax.swing.JPanel();
         jScrollPane26 = new javax.swing.JScrollPane();
         jTextArea8 = new javax.swing.JTextArea();
         jPanel13 = new javax.swing.JPanel();
         jScrollPane27 = new javax.swing.JScrollPane();
         jTextArea9 = new javax.swing.JTextArea();
         jPanel16 = new javax.swing.JPanel();
         jScrollPane32 = new javax.swing.JScrollPane();
         jTextArea10 = new javax.swing.JTextArea();
         jPanel24 = new javax.swing.JPanel();
         jScrollPane46 = new javax.swing.JScrollPane();
         jTextArea12 = new javax.swing.JTextArea();
         pnlou = new javax.swing.JPanel();
         jScrollPane12 = new javax.swing.JScrollPane();
         tblout = new javax.swing.JTable();
         jScrollPane17 = new javax.swing.JScrollPane();
         tblticconf = new javax.swing.JTable();
         jLabel59 = new javax.swing.JLabel();
         dctic7 = new com.toedter.calendar.JDateChooser();
         dctic8 = new com.toedter.calendar.JDateChooser();
         jLabel60 = new javax.swing.JLabel();
         jLabel61 = new javax.swing.JLabel();
         btnoutsrch = new javax.swing.JButton();
         cbagenou = new javax.swing.JComboBox();
         lblcaloutcount = new javax.swing.JLabel();
         lblrepticcount11 = new javax.swing.JLabel();
         panelfax = new javax.swing.JTabbedPane();
         jPanel14 = new javax.swing.JPanel();
         jScrollPane28 = new javax.swing.JScrollPane();
         tblfin = new javax.swing.JTable();
         jLabel65 = new javax.swing.JLabel();
         dtfi = new com.toedter.calendar.JDateChooser();
         jLabel69 = new javax.swing.JLabel();
         dtfi1 = new com.toedter.calendar.JDateChooser();
         btnfinsrch = new javax.swing.JButton();
         jScrollPane29 = new javax.swing.JScrollPane();
         lblview = new javax.swing.JLabel();
         jLabel90 = new javax.swing.JLabel();
         cbcust2 = new javax.swing.JComboBox();
         btncussaveFax = new javax.swing.JButton();
         jLabel93 = new javax.swing.JLabel();
         txtnoticfax = new javax.swing.JTextField();
         jPanel15 = new javax.swing.JPanel();
         jScrollPane30 = new javax.swing.JScrollPane();
         tblfou = new javax.swing.JTable();
         jLabel75 = new javax.swing.JLabel();
         dtfo = new com.toedter.calendar.JDateChooser();
         jLabel76 = new javax.swing.JLabel();
         dtfo1 = new com.toedter.calendar.JDateChooser();
         btnfoutsrch = new javax.swing.JButton();
         jScrollPane31 = new javax.swing.JScrollPane();
         lblview1 = new javax.swing.JLabel();
         pnlRepHidden = new javax.swing.JTabbedPane();
         pnlHoOu = new javax.swing.JPanel();
         jScrollPane40 = new javax.swing.JScrollPane();
         tblhourout = new javax.swing.JTable();
         dtho = new com.toedter.calendar.JDateChooser();
         jLabel79 = new javax.swing.JLabel();
         btnho = new javax.swing.JButton();
         btnexportcall2 = new javax.swing.JButton();
         pnlDayOu = new javax.swing.JPanel();
         jScrollPane44 = new javax.swing.JScrollPane();
         tbldailyout = new javax.swing.JTable();
         dtdo = new com.toedter.calendar.JDateChooser();
         jLabel83 = new javax.swing.JLabel();
         btndo = new javax.swing.JButton();
         jLabel85 = new javax.swing.JLabel();
         dtdo1 = new com.toedter.calendar.JDateChooser();
         btnexportcall4 = new javax.swing.JButton();
         pnlPerfOu = new javax.swing.JPanel();
         jScrollPane42 = new javax.swing.JScrollPane();
         tblperformout = new javax.swing.JTable();
         dtpo = new com.toedter.calendar.JDateChooser();
         btnpo1 = new javax.swing.JButton();
         jLabel87 = new javax.swing.JLabel();
         dtpo1 = new com.toedter.calendar.JDateChooser();
         jLabel89 = new javax.swing.JLabel();
         btnexportcall6 = new javax.swing.JButton();
         pnlrepFax = new javax.swing.JPanel();
         txtfaxfinm = new javax.swing.JTextField();
         jLabel33 = new javax.swing.JLabel();
         jLabel34 = new javax.swing.JLabel();
         btnrepfax = new javax.swing.JButton();
         jLabel36 = new javax.swing.JLabel();
         jLabel41 = new javax.swing.JLabel();
         dcfax1 = new com.toedter.calendar.JDateChooser();
         jLabel42 = new javax.swing.JLabel();
         dcfax2 = new com.toedter.calendar.JDateChooser();
         cbstatusrepfax = new javax.swing.JComboBox();
         btnexportmail1 = new javax.swing.JButton();
         cbagenirepfax = new javax.swing.JComboBox();
         jLabel64 = new javax.swing.JLabel();
         cbdirfax = new javax.swing.JComboBox();
         lblrepfaxcount = new javax.swing.JLabel();
         lblrepticcount9 = new javax.swing.JLabel();
         jScrollPane16 = new javax.swing.JScrollPane();
         tblrepfax = new javax.swing.JTable();
 
         setBackground(new java.awt.Color(204, 255, 204));
         setTitle("CONTACT CENTER TUNAS RENTAL");
         setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
 
         jdp.setBackground(new java.awt.Color(255, 255, 255));
 
         lbldate.setFont(new java.awt.Font("Calibri", 1, 24)); // NOI18N
         lbldate.setForeground(new java.awt.Color(255, 255, 255));
         lbldate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lbldate.setText("Date Time");
         lbldate.setBounds(40, 660, 230, 30);
         jdp.add(lbldate, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jPanel1.setBackground(new java.awt.Color(255, 255, 255));
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel1.setForeground(new java.awt.Color(255, 102, 0));
         jPanel1.setFont(new java.awt.Font("Calibri", 0, 11)); // NOI18N
         jPanel1.setLayout(null);
 
         btncall.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         btncall.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/cal1.jpg"))); // NOI18N
         btncall.setBorder(null);
         btncall.setEnabled(false);
         btncall.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btncallActionPerformed(evt);
             }
         });
         jPanel1.add(btncall);
         btncall.setBounds(320, 20, 80, 80);
 
         btnsms.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         btnsms.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/sm.jpg"))); // NOI18N
         btnsms.setBorder(null);
         btnsms.setEnabled(false);
         btnsms.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnsmsActionPerformed(evt);
             }
         });
         jPanel1.add(btnsms);
         btnsms.setBounds(410, 20, 80, 80);
 
         btnmail.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         btnmail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/mail.jpg"))); // NOI18N
         btnmail.setBorder(null);
         btnmail.setEnabled(false);
         btnmail.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnmailActionPerformed(evt);
             }
         });
         jPanel1.add(btnmail);
         btnmail.setBounds(500, 20, 80, 80);
 
         btnlogout.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         btnlogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117830_public.png"))); // NOI18N
         btnlogout.setToolTipText("LOG OUT");
         btnlogout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnlogoutActionPerformed(evt);
             }
         });
         jPanel1.add(btnlogout);
         btnlogout.setBounds(920, 20, 50, 40);
 
         lbluser.setFont(new java.awt.Font("Calibri", 1, 20)); // NOI18N
         lbluser.setForeground(new java.awt.Color(255, 102, 51));
         lbluser.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         lbluser.setText("Username");
         jPanel1.add(lbluser);
         lbluser.setBounds(810, 20, 100, 40);
 
         lbllogo.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         lbllogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lbllogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/TUNAS Rental_High Res Logo.png"))); // NOI18N
         jPanel1.add(lbllogo);
         lbllogo.setBounds(10, 20, 210, 90);
 
         btnoutbound.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         btnoutbound.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/out.jpg"))); // NOI18N
         btnoutbound.setToolTipText("OutBound");
         btnoutbound.setEnabled(false);
         btnoutbound.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnoutboundActionPerformed(evt);
             }
         });
         jPanel1.add(btnoutbound);
         btnoutbound.setBounds(690, 20, 70, 0);
 
         lblpas.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         lblpas.setText("jLabel1");
         lblpas.setEnabled(false);
         lblpas.setRequestFocusEnabled(false);
         jPanel1.add(lblpas);
         lblpas.setBounds(110, 70, 40, 0);
 
         lblactivity.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N
         lblactivity.setForeground(new java.awt.Color(255, 102, 51));
         lblactivity.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblactivity.setText("Disconnected");
         jPanel1.add(lblactivity);
         lblactivity.setBounds(220, 20, 90, 20);
 
         btnready.setFont(btnready.getFont().deriveFont(btnready.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnready.setText("Ready");
         btnready.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnreadyActionPerformed(evt);
             }
         });
         jPanel1.add(btnready);
         btnready.setBounds(220, 60, 90, 23);
 
         cbdirection.setFont(cbdirection.getFont());
         cbdirection.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "INBOUND", "OUTBOUND" }));
         jPanel1.add(cbdirection);
         cbdirection.setBounds(220, 40, 90, 24);
 
         lblshift.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         lblshift.setEnabled(false);
         lblshift.setRequestFocusEnabled(false);
         jPanel1.add(lblshift);
         lblshift.setBounds(110, 70, 0, 0);
 
         lblshift1.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         lblshift1.setEnabled(false);
         lblshift1.setRequestFocusEnabled(false);
         jPanel1.add(lblshift1);
         lblshift1.setBounds(110, 70, 0, 0);
 
         txtcalnoti.setEditable(false);
         txtcalnoti.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         txtcalnoti.setForeground(new java.awt.Color(255, 0, 0));
         txtcalnoti.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         txtcalnoti.setBorder(null);
         txtcalnoti.setOpaque(false);
         txtcalnoti.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 txtcalnotiMouseClicked(evt);
             }
         });
         jPanel1.add(txtcalnoti);
         txtcalnoti.setBounds(710, 70, 30, 20);
 
         txtfaxnoti.setEditable(false);
         txtfaxnoti.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         txtfaxnoti.setForeground(new java.awt.Color(255, 0, 0));
         txtfaxnoti.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         txtfaxnoti.setBorder(null);
         txtfaxnoti.setOpaque(false);
         jPanel1.add(txtfaxnoti);
         txtfaxnoti.setBounds(600, 100, 60, 20);
 
         txtsmsnoti.setEditable(false);
         txtsmsnoti.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         txtsmsnoti.setForeground(new java.awt.Color(255, 0, 0));
         txtsmsnoti.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         txtsmsnoti.setBorder(null);
         txtsmsnoti.setOpaque(false);
         jPanel1.add(txtsmsnoti);
         txtsmsnoti.setBounds(420, 100, 60, 20);
 
         txtmailnoti.setEditable(false);
         txtmailnoti.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         txtmailnoti.setForeground(new java.awt.Color(255, 0, 0));
         txtmailnoti.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         txtmailnoti.setBorder(null);
         txtmailnoti.setOpaque(false);
         txtmailnoti.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 txtmailnotiActionPerformed(evt);
             }
         });
         jPanel1.add(txtmailnoti);
         txtmailnoti.setBounds(510, 100, 60, 20);
 
         btnfax.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         btnfax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/fax.jpg"))); // NOI18N
         btnfax.setBorder(null);
         btnfax.setEnabled(false);
         btnfax.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnfaxActionPerformed(evt);
             }
         });
         jPanel1.add(btnfax);
         btnfax.setBounds(590, 20, 0, 80);
 
         pnlscroll.setBackground(new java.awt.Color(255, 255, 255));
         pnlscroll.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 pnlscrollMouseClicked(evt);
             }
         });
         pnlscroll.setLayout(null);
 
         lblscroll.setFont(lblscroll.getFont().deriveFont(lblscroll.getFont().getStyle() | java.awt.Font.BOLD, 11));
         lblscroll.setForeground(new java.awt.Color(255, 0, 0));
         pnlscroll.add(lblscroll);
         lblscroll.setBounds(710, 0, 0, 20);
 
         jPanel1.add(pnlscroll);
         pnlscroll.setBounds(220, 120, 750, 20);
 
         jLabel10.setFont(jLabel10.getFont().deriveFont(jLabel10.getFont().getStyle() | java.awt.Font.BOLD, 11));
         jLabel10.setText("Activity =");
         jPanel1.add(jLabel10);
         jLabel10.setBounds(40, 0, 60, 20);
 
         jLabel101.setFont(jLabel101.getFont().deriveFont((float)11));
         jLabel101.setText("Total Login : ");
         jPanel1.add(jLabel101);
         jLabel101.setBounds(100, 0, 70, 20);
 
         lblA1.setFont(lblA1.getFont().deriveFont(lblA1.getFont().getStyle() | java.awt.Font.BOLD, 13));
         lblA1.setForeground(new java.awt.Color(255, 0, 0));
         lblA1.setText(".");
         jPanel1.add(lblA1);
         lblA1.setBounds(170, 0, 30, 20);
 
         jLabel102.setFont(jLabel102.getFont().deriveFont((float)11));
         jLabel102.setText("Queing : ");
         jPanel1.add(jLabel102);
         jLabel102.setBounds(220, 0, 50, 20);
 
         lblA2.setFont(lblA2.getFont().deriveFont(lblA2.getFont().getStyle() | java.awt.Font.BOLD, 13));
         lblA2.setForeground(new java.awt.Color(255, 0, 0));
         lblA2.setText(".");
         jPanel1.add(lblA2);
         lblA2.setBounds(270, 0, 30, 20);
 
         jLabel99.setFont(jLabel99.getFont().deriveFont(jLabel99.getFont().getStyle() | java.awt.Font.BOLD, 11));
         jLabel99.setText("INBOUND =");
         jPanel1.add(jLabel99);
         jLabel99.setBounds(340, 0, 70, 20);
 
         jLabel104.setFont(jLabel104.getFont().deriveFont((float)11));
         jLabel104.setText("Ready : ");
         jPanel1.add(jLabel104);
         jLabel104.setBounds(410, 0, 50, 20);
 
         lblIn1.setFont(lblIn1.getFont().deriveFont(lblIn1.getFont().getStyle() | java.awt.Font.BOLD, 13));
         lblIn1.setForeground(new java.awt.Color(255, 0, 0));
         lblIn1.setText(".");
         jPanel1.add(lblIn1);
         lblIn1.setBounds(460, 0, 30, 20);
 
         jLabel103.setFont(jLabel103.getFont().deriveFont((float)11));
         jLabel103.setText("Online : ");
         jPanel1.add(jLabel103);
         jLabel103.setBounds(520, 0, 50, 20);
 
         lblIn2.setFont(lblIn2.getFont().deriveFont(lblIn2.getFont().getStyle() | java.awt.Font.BOLD, 13));
         lblIn2.setForeground(new java.awt.Color(255, 0, 0));
         lblIn2.setText(".");
         jPanel1.add(lblIn2);
         lblIn2.setBounds(570, 0, 30, 20);
 
         jLabel106.setFont(jLabel106.getFont().deriveFont((float)11));
         jLabel106.setText("Online : ");
         jPanel1.add(jLabel106);
         jLabel106.setBounds(720, 0, 50, 20);
 
         lblOu1.setFont(lblOu1.getFont().deriveFont(lblOu1.getFont().getStyle() | java.awt.Font.BOLD, 13));
         lblOu1.setForeground(new java.awt.Color(255, 0, 0));
         lblOu1.setText(".");
         jPanel1.add(lblOu1);
         lblOu1.setBounds(770, 0, 30, 20);
 
         jLabel100.setFont(jLabel100.getFont().deriveFont(jLabel100.getFont().getStyle() | java.awt.Font.BOLD, 11));
         jLabel100.setText("OUTBOUND =");
         jPanel1.add(jLabel100);
         jLabel100.setBounds(640, 0, 80, 20);
 
         jLabel105.setFont(jLabel105.getFont().deriveFont((float)11));
         jLabel105.setText("IDLE :");
         jPanel1.add(jLabel105);
         jLabel105.setBounds(830, 0, 50, 20);
 
         lblOu2.setFont(lblOu2.getFont().deriveFont(lblOu2.getFont().getStyle() | java.awt.Font.BOLD, 13));
         lblOu2.setForeground(new java.awt.Color(255, 0, 0));
         lblOu2.setText(".");
         jPanel1.add(lblOu2);
         lblOu2.setBounds(880, 0, 30, 20);
 
         jPanel1.setBounds(10, 20, 990, 140);
         jdp.add(jPanel1, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jPanel2.setBackground(new java.awt.Color(255, 255, 255));
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel2.setLayout(null);
 
         jtab.setBackground(new java.awt.Color(255, 255, 255));
         jtab.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
         jtab.setFont(jtab.getFont().deriveFont(jtab.getFont().getStyle() | java.awt.Font.BOLD, 11));
 
         pnlinbon.setBackground(new java.awt.Color(255, 255, 255));
         pnlinbon.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnlinbon.setLayout(null);
 
         tblin.setAutoCreateRowSorter(true);
         tblin.setFont(tblin.getFont().deriveFont((float)11));
         tblin.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         tblin.setDoubleBuffered(true);
         tblin.setFillsViewportHeight(true);
         tblin.setMaximumSize(new java.awt.Dimension(2147483647, 72));
         tblin.setRowHeight(20);
         tblin.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblinMouseClicked(evt);
             }
         });
         jScrollPane1.setViewportView(tblin);
 
         pnlinbon.add(jScrollPane1);
         jScrollPane1.setBounds(10, 40, 950, 390);
 
         jLabel55.setFont(jLabel55.getFont().deriveFont((float)11));
         jLabel55.setText("Open From");
         pnlinbon.add(jLabel55);
         jLabel55.setBounds(10, 10, 80, 10);
 
         jLabel56.setFont(jLabel56.getFont().deriveFont((float)11));
         jLabel56.setText("Until");
         pnlinbon.add(jLabel56);
         jLabel56.setBounds(130, 10, 50, 10);
 
         jLabel57.setFont(jLabel57.getFont().deriveFont((float)11));
         jLabel57.setText("Agen");
         pnlinbon.add(jLabel57);
         jLabel57.setBounds(260, 10, 50, 10);
 
         btninsrch.setFont(btninsrch.getFont().deriveFont(btninsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btninsrch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btninsrch.setText("Search By");
         btninsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btninsrchActionPerformed(evt);
             }
         });
         pnlinbon.add(btninsrch);
         btninsrch.setBounds(500, 20, 130, 24);
 
         dctic5.setDateFormatString("dd/MM/yyyy");
         dctic5.setFont(dctic5.getFont().deriveFont((float)11));
         pnlinbon.add(dctic5);
         dctic5.setBounds(10, 20, 120, 24);
 
         dctic6.setDateFormatString("dd/MM/yyyy");
         dctic6.setFont(dctic6.getFont().deriveFont((float)11));
         pnlinbon.add(dctic6);
         dctic6.setBounds(130, 20, 120, 24);
 
         jLabel58.setFont(jLabel58.getFont().deriveFont((float)11));
         jLabel58.setText("Call Status");
         pnlinbon.add(jLabel58);
         jLabel58.setBounds(370, 10, 100, 10);
 
         cbcalstatin.setFont(cbcalstatin.getFont().deriveFont((float)11));
         cbcalstatin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ABANDON", "ANSWERED", "--" }));
         cbcalstatin.setSelectedIndex(2);
         pnlinbon.add(cbcalstatin);
         cbcalstatin.setBounds(370, 20, 100, 24);
 
         cbagenin.setFont(cbagenin.getFont().deriveFont((float)11));
         cbagenin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--", "aan", "ramos", "john", "yusnita", "tri", "fitri", "mariana", "mitha", "dessy", "andrianto", "nurdin", "david", "yudho", "favel", "feronika", "oktaviani", "rudi" }));
         pnlinbon.add(cbagenin);
         cbagenin.setBounds(260, 20, 100, 24);
 
         lblcalincount.setFont(lblcalincount.getFont().deriveFont((float)11));
         lblcalincount.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         pnlinbon.add(lblcalincount);
         lblcalincount.setBounds(880, 0, 40, 10);
 
         lblrepticcount12.setFont(lblrepticcount12.getFont().deriveFont((float)11));
         lblrepticcount12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblrepticcount12.setText("list");
         pnlinbon.add(lblrepticcount12);
         lblrepticcount12.setBounds(920, 0, 40, 10);
 
         jtab.addTab("InBound", pnlinbon);
 
         pnltic.setBackground(new java.awt.Color(255, 255, 255));
         pnltic.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnltic.setLayout(null);
 
         tbltic.setAutoCreateRowSorter(true);
         tbltic.setFont(tbltic.getFont().deriveFont((float)11));
         tbltic.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null}
             },
             new String [] {
                 "Ticket No.", "Status", "Category", "Assign Dept.", "Assign user", "Customer", "Phone Number", "User", "No.Plat", "Type", "Driver", "Phone", "id"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false, false, false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tbltic.setAlignmentX(1.0F);
         tbltic.setAlignmentY(1.0F);
         tbltic.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         tbltic.setRowHeight(20);
         tbltic.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblticMouseClicked(evt);
             }
         });
         jScrollPane3.setViewportView(tbltic);
 
         pnltic.add(jScrollPane3);
         jScrollPane3.setBounds(10, 60, 950, 260);
 
         jButton6.setFont(jButton6.getFont().deriveFont(jButton6.getFont().getStyle() | java.awt.Font.BOLD, 11));
         jButton6.setText("Open Ticket");
         jButton6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton6ActionPerformed(evt);
             }
         });
         pnltic.add(jButton6);
         jButton6.setBounds(850, 410, 110, 23);
 
         dctic3.setDateFormatString("dd/MM/yyyy");
         pnltic.add(dctic3);
         dctic3.setBounds(10, 20, 120, 24);
 
         jLabel52.setFont(jLabel52.getFont().deriveFont((float)11));
         jLabel52.setText("Open From");
         pnltic.add(jLabel52);
         jLabel52.setBounds(30, 10, 80, 10);
 
         btnticsrch.setFont(btnticsrch.getFont().deriveFont(btnticsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnticsrch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnticsrch.setText("Search By");
         btnticsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnticsrchActionPerformed(evt);
             }
         });
         pnltic.add(btnticsrch);
         btnticsrch.setBounds(840, 20, 120, 24);
 
         dctic4.setDateFormatString("dd/MM/yyyy");
         pnltic.add(dctic4);
         dctic4.setBounds(130, 20, 120, 24);
 
         jLabel54.setFont(jLabel54.getFont().deriveFont((float)11));
         jLabel54.setText("Until");
         pnltic.add(jLabel54);
         jLabel54.setBounds(130, 10, 21, 10);
 
         btnsenddept.setFont(btnsenddept.getFont().deriveFont(btnsenddept.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnsenddept.setText("Send to Dept.");
         btnsenddept.setEnabled(false);
         btnsenddept.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnsenddeptActionPerformed(evt);
             }
         });
         pnltic.add(btnsenddept);
         btnsenddept.setBounds(10, 410, 130, 24);
 
         cktgl.setBackground(new java.awt.Color(255, 255, 204));
         cktgl.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         cktgl.setSelected(true);
         cktgl.setOpaque(false);
         cktgl.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cktglActionPerformed(evt);
             }
         });
         pnltic.add(cktgl);
         cktgl.setBounds(10, 0, 20, 20);
 
         jLabel73.setText("Solution :");
         pnltic.add(jLabel73);
         jLabel73.setBounds(490, 320, 60, 10);
 
         jLabel77.setText("Details :");
         pnltic.add(jLabel77);
         jLabel77.setBounds(10, 320, 60, 10);
 
         txtsolution.setColumns(20);
         txtsolution.setEditable(false);
         txtsolution.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
         txtsolution.setLineWrap(true);
         txtsolution.setRows(5);
         jScrollPane37.setViewportView(txtsolution);
 
         pnltic.add(jScrollPane37);
         jScrollPane37.setBounds(490, 330, 470, 80);
 
         txtdetail.setColumns(20);
         txtdetail.setEditable(false);
         txtdetail.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
         txtdetail.setLineWrap(true);
         txtdetail.setRows(5);
         jScrollPane38.setViewportView(txtdetail);
 
         pnltic.add(jScrollPane38);
         jScrollPane38.setBounds(10, 330, 470, 80);
 
         lblticcount.setFont(lblticcount.getFont().deriveFont((float)11));
         lblticcount.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         pnltic.add(lblticcount);
         lblticcount.setBounds(880, 0, 40, 10);
 
         lblrepticcount10.setFont(lblrepticcount10.getFont().deriveFont((float)11));
         lblrepticcount10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblrepticcount10.setText("list");
         pnltic.add(lblrepticcount10);
         lblrepticcount10.setBounds(920, 0, 40, 10);
 
         jPanel25.setBackground(new java.awt.Color(255, 255, 255));
         jPanel25.setPreferredSize(new java.awt.Dimension(1200, 60));
         jPanel25.setLayout(null);
 
         txtticno1.setFont(txtticno1.getFont().deriveFont((float)11));
         jPanel25.add(txtticno1);
         txtticno1.setBounds(10, 20, 80, 24);
 
         ckstoring.setBackground(new java.awt.Color(255, 255, 204));
         ckstoring.setFont(ckstoring.getFont().deriveFont((float)11));
         ckstoring.setText("RAM");
         ckstoring.setOpaque(false);
         ckstoring.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ckstoringActionPerformed(evt);
             }
         });
         jPanel25.add(ckstoring);
         ckstoring.setBounds(420, 0, 80, 20);
 
         jLabel62.setFont(jLabel62.getFont().deriveFont((float)11));
         jLabel62.setText("Plat No");
         jPanel25.add(jLabel62);
         jLabel62.setBounds(90, 10, 80, 10);
 
         ckassign.setBackground(new java.awt.Color(255, 255, 204));
         ckassign.setFont(ckassign.getFont().deriveFont((float)11));
         ckassign.setText("Assigned");
         ckassign.setOpaque(false);
         jPanel25.add(ckassign);
         ckassign.setBounds(420, 20, 80, 20);
 
         jLabel68.setFont(jLabel68.getFont().deriveFont((float)11));
         jLabel68.setText("Status");
         jPanel25.add(jLabel68);
         jLabel68.setBounds(320, 10, 100, 10);
 
         jLabel63.setFont(jLabel63.getFont().deriveFont((float)11));
         jLabel63.setText("Dept.");
         jPanel25.add(jLabel63);
         jLabel63.setBounds(170, 10, 100, 10);
 
         cbticstatus.setFont(cbticstatus.getFont().deriveFont((float)11));
         cbticstatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OPEN", "PROCESS", "CLOSED", "CANCEL", "--" }));
         cbticstatus.setSelectedIndex(4);
         cbticstatus.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbticstatusActionPerformed(evt);
             }
         });
         jPanel25.add(cbticstatus);
         cbticstatus.setBounds(320, 20, 100, 24);
 
         cbdept.setFont(cbdept.getFont().deriveFont((float)11));
         cbdept.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DEPT. CALL CENTER", "DEPT. KENDARAAN KEBON JERUK", "DEPT. DRIVER", "DEPT. MARKETING", " ", "DEPT. KENDARAAN LUAR KOTA", "--" }));
         cbdept.setSelectedIndex(6);
         cbdept.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbdeptActionPerformed(evt);
             }
         });
         jPanel25.add(cbdept);
         cbdept.setBounds(170, 20, 150, 24);
 
         cksubmit.setBackground(new java.awt.Color(255, 255, 204));
         cksubmit.setFont(cksubmit.getFont().deriveFont((float)11));
         cksubmit.setText("Not Submitted");
         cksubmit.setOpaque(false);
         cksubmit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cksubmitActionPerformed(evt);
             }
         });
         jPanel25.add(cksubmit);
         cksubmit.setBounds(500, 0, 120, 20);
 
         jLabel53.setFont(jLabel53.getFont().deriveFont((float)11));
         jLabel53.setText("No Ticket");
         jPanel25.add(jLabel53);
         jLabel53.setBounds(10, 10, 80, 10);
 
         txtplatno.setFont(txtplatno.getFont().deriveFont((float)11));
         jPanel25.add(txtplatno);
         txtplatno.setBounds(90, 20, 80, 24);
 
         jLabel94.setFont(jLabel94.getFont().deriveFont((float)11));
         jLabel94.setText("Customer");
         jPanel25.add(jLabel94);
         jLabel94.setBounds(610, 10, 80, 10);
 
         txtcus.setFont(txtcus.getFont().deriveFont((float)11));
         jPanel25.add(txtcus);
         txtcus.setBounds(610, 20, 110, 24);
 
         txtdriv.setFont(txtdriv.getFont().deriveFont((float)11));
         jPanel25.add(txtdriv);
         txtdriv.setBounds(870, 20, 110, 24);
 
         jLabel95.setFont(jLabel95.getFont().deriveFont((float)11));
         jLabel95.setText("Driver Name");
         jPanel25.add(jLabel95);
         jLabel95.setBounds(870, 10, 80, 10);
 
         jLabel96.setFont(jLabel96.getFont().deriveFont((float)11));
         jLabel96.setText("Category");
         jPanel25.add(jLabel96);
         jLabel96.setBounds(720, 10, 100, 10);
 
         cbcate.setFont(cbcate.getFont().deriveFont((float)11));
         cbcate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DEPT. CALL CENTER", "DEPT. KENDARAAN KEBON JERUK", "DEPT. DRIVER", "DEPT. MARKETING", " ", "DEPT. KENDARAAN LUAR KOTA", "--" }));
         cbcate.setSelectedIndex(6);
         cbcate.setEnabled(false);
         cbcate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbcateActionPerformed(evt);
             }
         });
         jPanel25.add(cbcate);
         cbcate.setBounds(720, 20, 150, 24);
 
         txtdrivcode.setFont(txtdrivcode.getFont().deriveFont((float)11));
         txtdrivcode.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 txtdrivcodeActionPerformed(evt);
             }
         });
         jPanel25.add(txtdrivcode);
         txtdrivcode.setBounds(980, 20, 80, 24);
 
         jLabel97.setFont(jLabel97.getFont().deriveFont((float)11));
         jLabel97.setText("Driver Code");
         jPanel25.add(jLabel97);
         jLabel97.setBounds(980, 10, 80, 10);
 
         jLabel98.setFont(jLabel98.getFont().deriveFont((float)11));
         jLabel98.setText("Follow Up By");
         jPanel25.add(jLabel98);
         jLabel98.setBounds(420, 10, 100, 0);
 
         cbFollowUp.setFont(cbFollowUp.getFont().deriveFont((float)11));
         cbFollowUp.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OPEN", "PROCESS", "CLOSED", "CANCEL", "--" }));
         cbFollowUp.setSelectedIndex(4);
         cbFollowUp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbFollowUpActionPerformed(evt);
             }
         });
         jPanel25.add(cbFollowUp);
         cbFollowUp.setBounds(420, 20, 100, 0);
 
         ckFCR.setBackground(new java.awt.Color(255, 255, 204));
         ckFCR.setFont(ckFCR.getFont().deriveFont((float)11));
         ckFCR.setText("F.C.R");
         ckFCR.setOpaque(false);
         ckFCR.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ckFCRActionPerformed(evt);
             }
         });
         jPanel25.add(ckFCR);
         ckFCR.setBounds(500, 20, 120, 20);
 
         jScrollPane47.setViewportView(jPanel25);
 
         pnltic.add(jScrollPane47);
         jScrollPane47.setBounds(250, 1, 590, 63);
 
         jtab.addTab("Ticket", pnltic);
 
         pnlact.setBackground(new java.awt.Color(255, 255, 255));
         pnlact.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnlact.setLayout(null);
 
         tblact.setFont(tblact.getFont().deriveFont((float)11));
         tblact.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null}
             },
             new String [] {
                 "Username", "Level", "Activity", "Info", "Loggin", "Host address", "Line number"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblact.setRowHeight(20);
         tblact.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 tblactMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 tblactMouseExited(evt);
             }
         });
         jScrollPane5.setViewportView(tblact);
 
         pnlact.add(jScrollPane5);
         jScrollPane5.setBounds(10, 30, 950, 400);
 
         btnrelease.setFont(btnrelease.getFont().deriveFont(btnrelease.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnrelease.setText("Release");
         btnrelease.setEnabled(false);
         btnrelease.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnreleaseActionPerformed(evt);
             }
         });
         pnlact.add(btnrelease);
         btnrelease.setBounds(860, 10, 100, 24);
 
         cbagenrelease.setFont(cbagenrelease.getFont().deriveFont((float)11));
         cbagenrelease.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbagenreleaseActionPerformed(evt);
             }
         });
         pnlact.add(cbagenrelease);
         cbagenrelease.setBounds(690, 10, 170, 24);
 
         jtab.addTab("Activity Monitoring", pnlact);
 
         panelsms.setBackground(new java.awt.Color(255, 255, 255));
         panelsms.setFont(panelsms.getFont().deriveFont((float)10));
         panelsms.setOpaque(true);
 
         pninbox.setBackground(new java.awt.Color(255, 255, 255));
         pninbox.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pninbox.setLayout(null);
 
         tblsin.setAutoCreateRowSorter(true);
         tblsin.setFont(tblsin.getFont().deriveFont((float)11));
         tblsin.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Sender", "Read", "Replied", "Messages"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, true, true, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblsin.setRowHeight(20);
         tblsin.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblsinMouseClicked(evt);
             }
         });
         jScrollPane6.setViewportView(tblsin);
 
         pninbox.add(jScrollPane6);
         jScrollPane6.setBounds(10, 40, 930, 180);
 
         dtsi.setDateFormatString("dd/MM/yyyy");
         dtsi.setFont(dtsi.getFont().deriveFont((float)11));
         pninbox.add(dtsi);
         dtsi.setBounds(10, 20, 120, 24);
 
         dtsi1.setDateFormatString("dd/MM/yyyy");
         dtsi1.setFont(dtsi1.getFont().deriveFont((float)11));
         pninbox.add(dtsi1);
         dtsi1.setBounds(130, 20, 120, 24);
 
         jLabel1.setFont(jLabel1.getFont().deriveFont((float)11));
         jLabel1.setText("From :");
         pninbox.add(jLabel1);
         jLabel1.setBounds(10, 10, 100, 10);
 
         jLabel2.setFont(jLabel2.getFont().deriveFont((float)11));
         jLabel2.setText("Until :");
         pninbox.add(jLabel2);
         jLabel2.setBounds(130, 10, 100, 10);
 
         jLabel47.setFont(jLabel47.getFont().deriveFont((float)11));
         jLabel47.setText("Sender");
         pninbox.add(jLabel47);
         jLabel47.setBounds(10, 240, 100, 20);
 
         jLabel49.setFont(jLabel49.getFont().deriveFont((float)11));
         jLabel49.setText("messages :");
         pninbox.add(jLabel49);
         jLabel49.setBounds(10, 260, 100, 20);
 
         txtimsg2.setColumns(20);
         txtimsg2.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
         txtimsg2.setLineWrap(true);
         txtimsg2.setRows(5);
         jScrollPane19.setViewportView(txtimsg2);
 
         pninbox.add(jScrollPane19);
         jScrollPane19.setBounds(110, 260, 550, 76);
 
         txtfrom2.setFont(txtfrom2.getFont().deriveFont((float)11));
         pninbox.add(txtfrom2);
         txtfrom2.setBounds(110, 240, 250, 24);
 
         btnsmsinsrch.setFont(btnsmsinsrch.getFont().deriveFont(btnsmsinsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnsmsinsrch.setText("Search");
         btnsmsinsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnsmsinsrchActionPerformed(evt);
             }
         });
         pninbox.add(btnsmsinsrch);
         btnsmsinsrch.setBounds(260, 20, 90, 24);
 
         jLabel80.setFont(jLabel80.getFont().deriveFont((float)11));
         jLabel80.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel80.setText("Cust. Company");
         pninbox.add(jLabel80);
         jLabel80.setBounds(360, 240, 100, 20);
 
         cbcust.setFont(cbcust.getFont().deriveFont((float)11));
         cbcust.setMaximumRowCount(9);
         cbcust.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Non-customer", "Customer-Driver", "Customer-User", "Customer-PIC", "Customer-Other", "Internal-ANJ", "Internal-CC", "Internal-CSO", "Internal-Driver", "Internal-Other" }));
         cbcust.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbcustActionPerformed(evt);
             }
         });
         pninbox.add(cbcust);
         cbcust.setBounds(460, 240, 200, 24);
 
         btncussavesms.setFont(btncussavesms.getFont().deriveFont(btncussavesms.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btncussavesms.setText("Save");
         btncussavesms.setEnabled(false);
         btncussavesms.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btncussavesmsActionPerformed(evt);
             }
         });
         pninbox.add(btncussavesms);
         btncussavesms.setBounds(720, 240, 80, 24);
 
         jLabel91.setFont(jLabel91.getFont().deriveFont((float)11));
         jLabel91.setText("Ticket No");
         pninbox.add(jLabel91);
         jLabel91.setBounds(660, 260, 100, 20);
 
         txtnoticsms.setFont(txtnoticsms.getFont().deriveFont((float)11));
         pninbox.add(txtnoticsms);
         txtnoticsms.setBounds(660, 280, 140, 24);
 
         panelsms.addTab("InBox", pninbox);
 
         pnoutbox.setBackground(new java.awt.Color(255, 255, 255));
         pnoutbox.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnoutbox.setLayout(null);
 
         tblsou.setAutoCreateRowSorter(true);
         tblsou.setFont(tblsou.getFont().deriveFont((float)11));
         tblsou.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Sent Time", "Send by", "Recipient", "Messages"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblsou.setRowHeight(20);
         tblsou.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblsouMouseClicked(evt);
             }
         });
         jScrollPane7.setViewportView(tblsou);
 
         pnoutbox.add(jScrollPane7);
         jScrollPane7.setBounds(10, 40, 930, 180);
 
         jLabel3.setFont(jLabel3.getFont().deriveFont((float)11));
         jLabel3.setText("From :");
         pnoutbox.add(jLabel3);
         jLabel3.setBounds(10, 10, 100, 10);
 
         dtso.setDateFormatString("dd/MM/yyyy");
         pnoutbox.add(dtso);
         dtso.setBounds(10, 20, 120, 24);
 
         jLabel4.setFont(jLabel4.getFont().deriveFont((float)11));
         jLabel4.setText("Until :");
         pnoutbox.add(jLabel4);
         jLabel4.setBounds(130, 10, 100, 10);
 
         dtso1.setDateFormatString("dd/MM/yyyy");
         pnoutbox.add(dtso1);
         dtso1.setBounds(130, 20, 120, 24);
 
         jLabel8.setFont(jLabel8.getFont().deriveFont((float)11));
         jLabel8.setText("Recipient");
         pnoutbox.add(jLabel8);
         jLabel8.setBounds(10, 240, 100, 20);
 
         jLabel35.setFont(jLabel35.getFont().deriveFont((float)11));
         jLabel35.setText("messages :");
         pnoutbox.add(jLabel35);
         jLabel35.setBounds(10, 260, 100, 20);
 
         txtimsg1.setColumns(20);
         txtimsg1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
         txtimsg1.setLineWrap(true);
         txtimsg1.setRows(5);
         jScrollPane18.setViewportView(txtimsg1);
 
         pnoutbox.add(jScrollPane18);
         jScrollPane18.setBounds(110, 260, 510, 76);
 
         txtfrom1.setFont(txtfrom1.getFont().deriveFont((float)11));
         pnoutbox.add(txtfrom1);
         txtfrom1.setBounds(110, 240, 250, 24);
 
         btnsmsoutsrch.setFont(btnsmsoutsrch.getFont().deriveFont(btnsmsoutsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnsmsoutsrch.setText("Search");
         btnsmsoutsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnsmsoutsrchActionPerformed(evt);
             }
         });
         pnoutbox.add(btnsmsoutsrch);
         btnsmsoutsrch.setBounds(260, 20, 90, 24);
 
         panelsms.addTab("OutBox", pnoutbox);
 
         jtab.addTab("Sms", panelsms);
 
         panelmail.setBackground(new java.awt.Color(255, 255, 255));
         panelmail.setFont(panelmail.getFont().deriveFont((float)10));
         panelmail.setOpaque(true);
         panelmail.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 panelmailMouseClicked(evt);
             }
         });
 
         jPanel9.setBackground(new java.awt.Color(255, 255, 255));
         jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel9.setLayout(null);
 
         tblmin.setAutoCreateRowSorter(true);
         tblmin.setFont(tblmin.getFont().deriveFont((float)11));
         tblmin.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null}
             },
             new String [] {
                 "From", "Subject", "Date", "Read", "Status"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblmin.setRowHeight(20);
         tblmin.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblminMouseClicked(evt);
             }
         });
         jScrollPane8.setViewportView(tblmin);
 
         jPanel9.add(jScrollPane8);
         jScrollPane8.setBounds(10, 40, 950, 180);
 
         jLabel5.setFont(jLabel5.getFont().deriveFont((float)11));
         jLabel5.setText("From :");
         jPanel9.add(jLabel5);
         jLabel5.setBounds(10, 10, 100, 10);
 
         txtfrom.setFont(txtfrom.getFont().deriveFont((float)11));
         jPanel9.add(txtfrom);
         txtfrom.setBounds(110, 240, 300, 24);
 
         jLabel11.setFont(jLabel11.getFont().deriveFont((float)11));
         jLabel11.setText("Subject :");
         jPanel9.add(jLabel11);
         jLabel11.setBounds(10, 260, 100, 20);
 
         txtisu.setFont(txtisu.getFont().deriveFont((float)11));
         txtisu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 txtisuActionPerformed(evt);
             }
         });
         jPanel9.add(txtisu);
         txtisu.setBounds(110, 260, 700, 24);
 
         jLabel12.setFont(jLabel12.getFont().deriveFont((float)11));
         jLabel12.setText("messages :");
         jPanel9.add(jLabel12);
         jLabel12.setBounds(10, 280, 100, 20);
 
         txtimsg.setColumns(20);
         txtimsg.setFont(txtimsg.getFont().deriveFont((float)11));
         txtimsg.setLineWrap(true);
         txtimsg.setRows(5);
         txtimsg.setAutoscrolls(false);
         jScrollPane9.setViewportView(txtimsg);
 
         jPanel9.add(jScrollPane9);
         jScrollPane9.setBounds(110, 280, 700, 110);
 
         jLabel18.setFont(jLabel18.getFont().deriveFont((float)11));
         jLabel18.setText("From :");
         jPanel9.add(jLabel18);
         jLabel18.setBounds(10, 240, 100, 20);
 
         dtmi.setDateFormatString("dd/MM/yyyy");
         dtmi.setFont(dtmi.getFont().deriveFont((float)11));
         jPanel9.add(dtmi);
         dtmi.setBounds(10, 20, 120, 24);
 
         jLabel48.setFont(jLabel48.getFont().deriveFont((float)11));
         jLabel48.setText("Until :");
         jPanel9.add(jLabel48);
         jLabel48.setBounds(130, 10, 100, 10);
 
         dtmi1.setDateFormatString("dd/MM/yyyy");
         dtmi1.setFont(dtmi1.getFont().deriveFont((float)11));
         jPanel9.add(dtmi1);
         dtmi1.setBounds(130, 20, 120, 24);
 
         btnmailinsrch.setFont(btnmailinsrch.getFont().deriveFont(btnmailinsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnmailinsrch.setText("Search");
         btnmailinsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnmailinsrchActionPerformed(evt);
             }
         });
         jPanel9.add(btnmailinsrch);
         btnmailinsrch.setBounds(260, 20, 90, 24);
 
         jLabel81.setFont(jLabel81.getFont().deriveFont((float)11));
         jLabel81.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel81.setText("Cust. Company");
         jPanel9.add(jLabel81);
         jLabel81.setBounds(410, 240, 100, 20);
 
         cbcust1.setFont(cbcust1.getFont().deriveFont((float)11));
         cbcust1.setMaximumRowCount(9);
         cbcust1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Non-customer", "Customer-Driver", "Customer-User", "Customer-PIC", "Customer-Other", "Internal-ANJ", "Internal-CC", "Internal-CSO", "Internal-Driver", "Internal-Other" }));
         cbcust1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbcust1ActionPerformed(evt);
             }
         });
         jPanel9.add(cbcust1);
         cbcust1.setBounds(510, 240, 200, 24);
 
         btncussaveEmail.setFont(btncussaveEmail.getFont().deriveFont(btncussaveEmail.getFont().getStyle() | java.awt.Font.BOLD));
         btncussaveEmail.setText("Save");
         btncussaveEmail.setEnabled(false);
         btncussaveEmail.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btncussaveEmailActionPerformed(evt);
             }
         });
         jPanel9.add(btncussaveEmail);
         btncussaveEmail.setBounds(730, 240, 80, 24);
 
         jLabel92.setFont(jLabel92.getFont().deriveFont((float)11));
         jLabel92.setText("Ticket No");
         jPanel9.add(jLabel92);
         jLabel92.setBounds(810, 220, 100, 20);
 
         txtnoticmail.setFont(txtnoticmail.getFont().deriveFont((float)11));
         jPanel9.add(txtnoticmail);
         txtnoticmail.setBounds(810, 240, 150, 24);
 
         btnAttachment.setFont(btnAttachment.getFont().deriveFont(btnAttachment.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnAttachment.setText("Download");
         btnAttachment.setEnabled(false);
         btnAttachment.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAttachmentActionPerformed(evt);
             }
         });
         jPanel9.add(btnAttachment);
         btnAttachment.setBounds(860, 260, 100, 24);
 
         jList2.setFont(jList2.getFont().deriveFont((float)11));
         jList2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jList2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 jList2MouseReleased(evt);
             }
         });
         scpCcList1.setViewportView(jList2);
 
         jPanel9.add(scpCcList1);
         scpCcList1.setBounds(810, 280, 150, 110);
 
         panelmail.addTab("InBox", jPanel9);
 
         jPanel10.setBackground(new java.awt.Color(255, 255, 255));
         jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel10.setLayout(null);
 
         tblmou.setAutoCreateRowSorter(true);
         tblmou.setFont(tblmou.getFont().deriveFont((float)11));
         tblmou.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null}
             },
             new String [] {
                 "To", "Subject", "Date", "Cc", "Status"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblmou.setRowHeight(20);
         tblmou.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblmouMouseClicked(evt);
             }
         });
         jScrollPane10.setViewportView(tblmou);
 
         jPanel10.add(jScrollPane10);
         jScrollPane10.setBounds(10, 40, 950, 180);
 
         txtoto.setFont(txtoto.getFont().deriveFont((float)11));
         jPanel10.add(txtoto);
         txtoto.setBounds(110, 240, 300, 24);
 
         jLabel13.setBackground(new java.awt.Color(255, 255, 204));
         jLabel13.setFont(jLabel13.getFont().deriveFont((float)11));
         jLabel13.setText("To :");
         jPanel10.add(jLabel13);
         jLabel13.setBounds(10, 240, 100, 20);
 
         txtocc.setFont(txtocc.getFont().deriveFont((float)11));
         jPanel10.add(txtocc);
         txtocc.setBounds(510, 240, 300, 24);
 
         jLabel14.setFont(jLabel14.getFont().deriveFont((float)11));
         jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel14.setText("Cc :");
         jPanel10.add(jLabel14);
         jLabel14.setBounds(410, 240, 100, 20);
 
         jLabel15.setFont(jLabel15.getFont().deriveFont((float)11));
         jLabel15.setText("Subject :");
         jPanel10.add(jLabel15);
         jLabel15.setBounds(10, 260, 100, 20);
 
         txtosu.setFont(txtosu.getFont().deriveFont((float)11));
         jPanel10.add(txtosu);
         txtosu.setBounds(110, 260, 700, 24);
 
         jLabel16.setFont(jLabel16.getFont().deriveFont((float)11));
         jLabel16.setText("messages :");
         jPanel10.add(jLabel16);
         jLabel16.setBounds(10, 280, 100, 20);
 
         txtomsg.setColumns(20);
         txtomsg.setEditable(false);
         txtomsg.setFont(txtomsg.getFont().deriveFont((float)11));
         txtomsg.setLineWrap(true);
         txtomsg.setRows(5);
         txtomsg.setAutoscrolls(false);
         jScrollPane11.setViewportView(txtomsg);
 
         jPanel10.add(jScrollPane11);
         jScrollPane11.setBounds(110, 280, 700, 110);
 
         jLabel17.setFont(jLabel17.getFont().deriveFont((float)11));
         jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel17.setText("Ticket Id. :");
         jPanel10.add(jLabel17);
         jLabel17.setBounds(210, 220, 100, 20);
 
         txtidti.setFont(txtidti.getFont().deriveFont((float)11));
         jPanel10.add(txtidti);
         txtidti.setBounds(310, 220, 100, 24);
 
         jLabel50.setFont(jLabel50.getFont().deriveFont((float)11));
         jLabel50.setText("From :");
         jPanel10.add(jLabel50);
         jLabel50.setBounds(10, 10, 100, 10);
 
         dtmo.setDateFormatString("dd/MM/yyyy");
         dtmo.setFont(dtmo.getFont().deriveFont((float)11));
         jPanel10.add(dtmo);
         dtmo.setBounds(10, 20, 120, 24);
 
         jLabel51.setFont(jLabel51.getFont().deriveFont((float)11));
         jLabel51.setText("Until :");
         jPanel10.add(jLabel51);
         jLabel51.setBounds(130, 10, 100, 10);
 
         dtmo1.setDateFormatString("dd/MM/yyyy");
         dtmo1.setFont(dtmo1.getFont().deriveFont((float)11));
         jPanel10.add(dtmo1);
         dtmo1.setBounds(130, 20, 120, 24);
 
         btnmailoutsrch.setFont(btnmailoutsrch.getFont().deriveFont(btnmailoutsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnmailoutsrch.setText("Search");
         btnmailoutsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnmailoutsrchActionPerformed(evt);
             }
         });
         jPanel10.add(btnmailoutsrch);
         btnmailoutsrch.setBounds(260, 20, 90, 24);
 
         jList3.setFont(jList3.getFont().deriveFont((float)11));
         jList3.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jList3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 jList3MouseReleased(evt);
             }
         });
         scpCcList2.setViewportView(jList3);
 
         jPanel10.add(scpCcList2);
         scpCcList2.setBounds(810, 280, 150, 110);
 
         btnAttachment1.setFont(btnAttachment1.getFont().deriveFont(btnAttachment1.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnAttachment1.setText("Download");
         btnAttachment1.setEnabled(false);
         btnAttachment1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAttachment1ActionPerformed(evt);
             }
         });
         jPanel10.add(btnAttachment1);
         btnAttachment1.setBounds(860, 260, 100, 24);
 
         panelmail.addTab("OutBox", jPanel10);
 
         jtab.addTab("Email", panelmail);
 
         tabbpanereport.setBackground(new java.awt.Color(255, 255, 255));
         tabbpanereport.setFont(tabbpanereport.getFont().deriveFont(tabbpanereport.getFont().getStyle() | java.awt.Font.BOLD, 10));
         tabbpanereport.setOpaque(true);
 
         pnlrep.setBackground(new java.awt.Color(255, 255, 255));
         pnlrep.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnlrep.setLayout(null);
 
         tblreptic.setFont(tblreptic.getFont().deriveFont((float)11));
         tblreptic.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tblreptic.setRowHeight(20);
         tblreptic.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblrepticMouseClicked(evt);
             }
         });
         jScrollPane4.setViewportView(tblreptic);
 
         pnlrep.add(jScrollPane4);
         jScrollPane4.setBounds(10, 60, 950, 320);
 
         btnreptic.setFont(btnreptic.getFont().deriveFont(btnreptic.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnreptic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnreptic.setText("Search");
         btnreptic.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnrepticActionPerformed(evt);
             }
         });
         pnlrep.add(btnreptic);
         btnreptic.setBounds(840, 20, 110, 24);
 
         jLabel37.setFont(jLabel37.getFont().deriveFont((float)11));
         jLabel37.setText("Open From");
         pnlrep.add(jLabel37);
         jLabel37.setBounds(10, 10, 100, 10);
 
         jLabel38.setFont(jLabel38.getFont().deriveFont((float)11));
         jLabel38.setText("Until");
         pnlrep.add(jLabel38);
         jLabel38.setBounds(130, 10, 100, 10);
 
         dctic1.setDateFormatString("dd/MM/yyyy");
         dctic1.setFont(dctic1.getFont().deriveFont((float)11));
         pnlrep.add(dctic1);
         dctic1.setBounds(10, 20, 120, 24);
 
         dctic2.setDateFormatString("dd/MM/yyyy");
         dctic2.setFont(dctic2.getFont().deriveFont((float)11));
         pnlrep.add(dctic2);
         dctic2.setBounds(130, 20, 120, 24);
 
         btnexporttic.setFont(btnexporttic.getFont().deriveFont(btnexporttic.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexporttic.setText("Export");
         btnexporttic.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportticActionPerformed(evt);
             }
         });
         pnlrep.add(btnexporttic);
         btnexporttic.setBounds(10, 380, 90, 20);
 
         lblrepticcount.setFont(lblrepticcount.getFont().deriveFont((float)11));
         lblrepticcount.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         pnlrep.add(lblrepticcount);
         lblrepticcount.setBounds(880, 0, 40, 10);
 
         lblrepticcount1.setFont(lblrepticcount1.getFont().deriveFont((float)11));
         lblrepticcount1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblrepticcount1.setText("list");
         pnlrep.add(lblrepticcount1);
         lblrepticcount1.setBounds(920, 0, 40, 10);
 
         jPanel26.setBackground(new java.awt.Color(255, 255, 255));
         jPanel26.setPreferredSize(new java.awt.Dimension(1200, 60));
         jPanel26.setLayout(null);
 
         txtticno2.setFont(txtticno2.getFont().deriveFont((float)11));
         jPanel26.add(txtticno2);
         txtticno2.setBounds(10, 20, 80, 24);
 
         ckstoring1.setBackground(new java.awt.Color(255, 255, 204));
         ckstoring1.setFont(ckstoring1.getFont().deriveFont((float)11));
         ckstoring1.setText("RAM");
         ckstoring1.setOpaque(false);
         ckstoring1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ckstoring1ActionPerformed(evt);
             }
         });
         jPanel26.add(ckstoring1);
         ckstoring1.setBounds(420, 0, 80, 20);
 
         jLabel107.setFont(jLabel107.getFont().deriveFont((float)11));
         jLabel107.setText("Plat No");
         jPanel26.add(jLabel107);
         jLabel107.setBounds(90, 10, 80, 10);
 
         ckassign1.setBackground(new java.awt.Color(255, 255, 204));
         ckassign1.setFont(ckassign1.getFont().deriveFont((float)11));
         ckassign1.setText("Assigned");
         ckassign1.setOpaque(false);
         jPanel26.add(ckassign1);
         ckassign1.setBounds(420, 20, 80, 20);
 
         jLabel108.setFont(jLabel108.getFont().deriveFont((float)11));
         jLabel108.setText("Status");
         jPanel26.add(jLabel108);
         jLabel108.setBounds(320, 10, 100, 10);
 
         jLabel109.setFont(jLabel109.getFont().deriveFont((float)11));
         jLabel109.setText("Dept.");
         jPanel26.add(jLabel109);
         jLabel109.setBounds(170, 10, 100, 10);
 
         cbticstatus1.setFont(cbticstatus1.getFont().deriveFont((float)11));
         cbticstatus1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OPEN", "PROCESS", "CLOSED", "CANCEL", "--" }));
         cbticstatus1.setSelectedIndex(4);
         cbticstatus1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbticstatus1ActionPerformed(evt);
             }
         });
         jPanel26.add(cbticstatus1);
         cbticstatus1.setBounds(320, 20, 100, 24);
 
         cbdept1.setFont(cbdept1.getFont().deriveFont((float)11));
         cbdept1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DEPT. CALL CENTER", "DEPT. KENDARAAN KEBON JERUK", "DEPT. DRIVER", "DEPT. MARKETING", " ", "DEPT. KENDARAAN LUAR KOTA", "--" }));
         cbdept1.setSelectedIndex(6);
         cbdept1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbdept1ActionPerformed(evt);
             }
         });
         jPanel26.add(cbdept1);
         cbdept1.setBounds(170, 20, 150, 24);
 
         cksubmit1.setBackground(new java.awt.Color(255, 255, 204));
         cksubmit1.setFont(cksubmit1.getFont().deriveFont((float)11));
         cksubmit1.setText("Not Submitted");
         cksubmit1.setOpaque(false);
         cksubmit1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cksubmit1ActionPerformed(evt);
             }
         });
         jPanel26.add(cksubmit1);
         cksubmit1.setBounds(500, 0, 120, 20);
 
         jLabel110.setFont(jLabel110.getFont().deriveFont((float)11));
         jLabel110.setText("No Ticket");
         jPanel26.add(jLabel110);
         jLabel110.setBounds(10, 10, 80, 10);
 
         txtplatno1.setFont(txtplatno1.getFont().deriveFont((float)11));
         jPanel26.add(txtplatno1);
         txtplatno1.setBounds(90, 20, 80, 24);
 
         jLabel111.setFont(jLabel111.getFont().deriveFont((float)11));
         jLabel111.setText("Customer");
         jPanel26.add(jLabel111);
         jLabel111.setBounds(610, 10, 80, 10);
 
         txtcus1.setFont(txtcus1.getFont().deriveFont((float)11));
         jPanel26.add(txtcus1);
         txtcus1.setBounds(610, 20, 110, 24);
 
         txtdriv1.setFont(txtdriv1.getFont().deriveFont((float)11));
         jPanel26.add(txtdriv1);
         txtdriv1.setBounds(870, 20, 110, 24);
 
         jLabel112.setFont(jLabel112.getFont().deriveFont((float)11));
         jLabel112.setText("Driver Name");
         jPanel26.add(jLabel112);
         jLabel112.setBounds(870, 10, 80, 10);
 
         jLabel113.setFont(jLabel113.getFont().deriveFont((float)11));
         jLabel113.setText("Category");
         jPanel26.add(jLabel113);
         jLabel113.setBounds(720, 10, 100, 10);
 
         cbcate1.setFont(cbcate1.getFont().deriveFont((float)11));
         cbcate1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DEPT. CALL CENTER", "DEPT. KENDARAAN KEBON JERUK", "DEPT. DRIVER", "DEPT. MARKETING", " ", "DEPT. KENDARAAN LUAR KOTA", "--" }));
         cbcate1.setSelectedIndex(6);
         cbcate1.setEnabled(false);
         cbcate1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbcate1ActionPerformed(evt);
             }
         });
         jPanel26.add(cbcate1);
         cbcate1.setBounds(720, 20, 150, 24);
 
         txtdrivcode1.setFont(txtdrivcode1.getFont().deriveFont((float)11));
         txtdrivcode1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 txtdrivcode1ActionPerformed(evt);
             }
         });
         jPanel26.add(txtdrivcode1);
         txtdrivcode1.setBounds(980, 20, 80, 24);
 
         jLabel114.setFont(jLabel114.getFont().deriveFont((float)11));
         jLabel114.setText("Driver Code");
         jPanel26.add(jLabel114);
         jLabel114.setBounds(980, 10, 80, 10);
 
         jLabel115.setFont(jLabel115.getFont().deriveFont((float)11));
         jLabel115.setText("Follow Up By");
         jPanel26.add(jLabel115);
         jLabel115.setBounds(420, 10, 100, 0);
 
         cbFollowUp1.setFont(cbFollowUp1.getFont().deriveFont((float)11));
         cbFollowUp1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OPEN", "PROCESS", "CLOSED", "CANCEL", "--" }));
         cbFollowUp1.setSelectedIndex(4);
         cbFollowUp1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbFollowUp1ActionPerformed(evt);
             }
         });
         jPanel26.add(cbFollowUp1);
         cbFollowUp1.setBounds(420, 20, 100, 0);
 
         ckFCR1.setBackground(new java.awt.Color(255, 255, 204));
         ckFCR1.setFont(ckFCR1.getFont().deriveFont((float)11));
         ckFCR1.setText("F.C.R");
         ckFCR1.setOpaque(false);
         ckFCR1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ckFCR1ActionPerformed(evt);
             }
         });
         jPanel26.add(ckFCR1);
         ckFCR1.setBounds(500, 20, 120, 20);
 
         jScrollPane48.setViewportView(jPanel26);
 
         pnlrep.add(jScrollPane48);
         jScrollPane48.setBounds(250, 1, 590, 63);
 
         tabbpanereport.addTab("Tickets", pnlrep);
 
         pnlrep1.setBackground(new java.awt.Color(255, 255, 255));
         pnlrep1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnlrep1.setLayout(null);
 
         tblrepcal.setFont(tblrepcal.getFont().deriveFont((float)11));
         tblrepcal.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tblrepcal.setRowHeight(20);
         jScrollPane13.setViewportView(tblrepcal);
 
         pnlrep1.add(jScrollPane13);
         jScrollPane13.setBounds(10, 40, 950, 340);
 
         jLabel21.setFont(jLabel21.getFont().deriveFont((float)11));
         jLabel21.setText("Call Status");
         pnlrep1.add(jLabel21);
         jLabel21.setBounds(260, 10, 100, 10);
 
         jLabel22.setFont(jLabel22.getFont().deriveFont((float)11));
         jLabel22.setText("Direction");
         pnlrep1.add(jLabel22);
         jLabel22.setBounds(360, 10, 100, 10);
 
         jLabel23.setFont(jLabel23.getFont().deriveFont((float)11));
         jLabel23.setText("Caller Type");
         pnlrep1.add(jLabel23);
         jLabel23.setBounds(460, 10, 100, 10);
 
         btnrepcal.setFont(btnrepcal.getFont().deriveFont(btnrepcal.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnrepcal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnrepcal.setText("Search Call");
         btnrepcal.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnrepcalActionPerformed(evt);
             }
         });
         pnlrep1.add(btnrepcal);
         btnrepcal.setBounds(670, 20, 130, 24);
 
         jLabel24.setFont(jLabel24.getFont().deriveFont((float)11));
         jLabel24.setText("Agen");
         pnlrep1.add(jLabel24);
         jLabel24.setBounds(560, 10, 100, 10);
 
         jLabel39.setFont(jLabel39.getFont().deriveFont((float)11));
         jLabel39.setText("Open From");
         pnlrep1.add(jLabel39);
         jLabel39.setBounds(10, 10, 100, 10);
 
         dccal1.setDateFormatString("dd/MM/yyyy");
         dccal1.setFont(dccal1.getFont().deriveFont((float)11));
         pnlrep1.add(dccal1);
         dccal1.setBounds(10, 20, 120, 24);
 
         jLabel40.setFont(jLabel40.getFont().deriveFont((float)11));
         jLabel40.setText("Until");
         pnlrep1.add(jLabel40);
         jLabel40.setBounds(130, 10, 100, 10);
 
         dccal2.setDateFormatString("dd/MM/yyyy");
         dccal2.setFont(dccal2.getFont().deriveFont((float)11));
         pnlrep1.add(dccal2);
         dccal2.setBounds(130, 20, 120, 24);
 
         cbcaldir.setFont(cbcaldir.getFont().deriveFont((float)11));
         cbcaldir.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "INBOUND", "OUTBOUND", "--" }));
         cbcaldir.setSelectedIndex(2);
         pnlrep1.add(cbcaldir);
         cbcaldir.setBounds(360, 20, 100, 24);
 
         cbcalstat.setFont(cbcalstat.getFont().deriveFont((float)11));
         cbcalstat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ABANDON", "ANSWERED", "PHANTOM", "--" }));
         cbcalstat.setSelectedIndex(3);
         cbcalstat.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbcalstatActionPerformed(evt);
             }
         });
         pnlrep1.add(cbcalstat);
         cbcalstat.setBounds(260, 20, 100, 24);
 
         cbagenirepcal.setFont(cbagenirepcal.getFont().deriveFont((float)11));
         cbagenirepcal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--", "aan", "ramos", "john", "yusnita", "tri", "fitri", "mariana", "mitha", "dessy", "andrianto", "nurdin", "david" }));
         pnlrep1.add(cbagenirepcal);
         cbagenirepcal.setBounds(560, 20, 100, 24);
 
         cbcaltyperepcal.setFont(cbcaltyperepcal.getFont().deriveFont((float)11));
         cbcaltyperepcal.setMaximumRowCount(10);
         cbcaltyperepcal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Non-customer", "Customer-Driver", "Customer-User", "Customer-PIC", "Customer-Other", "Internal-ANJ", "Internal-CC", "Internal-CSO", "Internal-Driver", "Internal-Other", "--" }));
         cbcaltyperepcal.setSelectedIndex(10);
         pnlrep1.add(cbcaltyperepcal);
         cbcaltyperepcal.setBounds(460, 20, 100, 24);
 
         btnexportcall.setFont(btnexportcall.getFont().deriveFont(btnexportcall.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportcall.setText("Export");
         btnexportcall.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportcallActionPerformed(evt);
             }
         });
         pnlrep1.add(btnexportcall);
         btnexportcall.setBounds(10, 380, 90, 20);
 
         lblrepcalcount.setFont(lblrepcalcount.getFont().deriveFont((float)11));
         lblrepcalcount.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         pnlrep1.add(lblrepcalcount);
         lblrepcalcount.setBounds(880, 0, 40, 10);
 
         lblrepticcount3.setFont(lblrepticcount3.getFont().deriveFont((float)11));
         lblrepticcount3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblrepticcount3.setText("list");
         pnlrep1.add(lblrepticcount3);
         lblrepticcount3.setBounds(920, 0, 40, 10);
 
         tabbpanereport.addTab("Calls", pnlrep1);
 
         jTabbedPane1.setFont(jTabbedPane1.getFont().deriveFont((float)10));
 
         jPanel17.setBackground(new java.awt.Color(255, 255, 255));
         jPanel17.setLayout(null);
 
         tblhourin.setFont(tblhourin.getFont().deriveFont((float)11));
         tblhourin.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane39.setViewportView(tblhourin);
 
         jPanel17.add(jScrollPane39);
         jScrollPane39.setBounds(10, 40, 940, 310);
 
         jLabel78.setFont(jLabel78.getFont().deriveFont((float)11));
         jLabel78.setText("Date");
         jPanel17.add(jLabel78);
         jLabel78.setBounds(10, 10, 100, 10);
 
         dthi.setDateFormatString("dd/MM/yyyy");
         dthi.setFont(dthi.getFont().deriveFont((float)11));
         jPanel17.add(dthi);
         dthi.setBounds(10, 20, 120, 24);
 
         btnhi.setFont(btnhi.getFont().deriveFont(btnhi.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnhi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnhi.setText("Refresh");
         btnhi.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnhiActionPerformed(evt);
             }
         });
         jPanel17.add(btnhi);
         btnhi.setBounds(150, 20, 115, 24);
 
         btnexportcall1.setFont(btnexportcall1.getFont().deriveFont(btnexportcall1.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportcall1.setText("Export");
         btnexportcall1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportcall1ActionPerformed(evt);
             }
         });
         jPanel17.add(btnexportcall1);
         btnexportcall1.setBounds(10, 350, 90, 20);
 
         jTabbedPane1.addTab("Inbound", jPanel17);
 
         tabbpanereport.addTab("Hourly Calls", jTabbedPane1);
 
         jTabbedPane4.setFont(jTabbedPane4.getFont().deriveFont((float)10));
 
         jPanel21.setBackground(new java.awt.Color(255, 255, 255));
         jPanel21.setLayout(null);
 
         tbldailyin.setFont(tbldailyin.getFont().deriveFont((float)11));
         tbldailyin.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane43.setViewportView(tbldailyin);
 
         jPanel21.add(jScrollPane43);
         jScrollPane43.setBounds(10, 40, 940, 310);
 
         jLabel82.setFont(jLabel82.getFont().deriveFont((float)11));
         jLabel82.setText("From");
         jPanel21.add(jLabel82);
         jLabel82.setBounds(10, 10, 100, 10);
 
         dtdi.setDateFormatString("dd/MM/yyyy");
         dtdi.setFont(dtdi.getFont().deriveFont((float)11));
         jPanel21.add(dtdi);
         dtdi.setBounds(10, 20, 120, 24);
 
         btndi.setFont(btndi.getFont().deriveFont(btndi.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btndi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btndi.setText("Refresh");
         btndi.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btndiActionPerformed(evt);
             }
         });
         jPanel21.add(btndi);
         btndi.setBounds(270, 20, 115, 24);
 
         jLabel84.setFont(jLabel84.getFont().deriveFont((float)11));
         jLabel84.setText("Until");
         jPanel21.add(jLabel84);
         jLabel84.setBounds(140, 10, 100, 10);
 
         dtdi1.setDateFormatString("dd/MM/yyyy");
         dtdi1.setFont(dtdi1.getFont().deriveFont((float)11));
         jPanel21.add(dtdi1);
         dtdi1.setBounds(140, 20, 120, 24);
 
         btnexportcall3.setFont(btnexportcall3.getFont().deriveFont(btnexportcall3.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportcall3.setText("Export");
         btnexportcall3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportcall3ActionPerformed(evt);
             }
         });
         jPanel21.add(btnexportcall3);
         btnexportcall3.setBounds(10, 350, 90, 20);
 
         jTabbedPane4.addTab("Inbound", jPanel21);
 
         tabbpanereport.addTab("Daily Calls", jTabbedPane4);
 
         jTabbedPane3.setFont(jTabbedPane3.getFont().deriveFont((float)10));
 
         jPanel19.setBackground(new java.awt.Color(255, 255, 255));
         jPanel19.setLayout(null);
 
         tblperformin.setFont(tblperformin.getFont().deriveFont((float)11));
         tblperformin.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane41.setViewportView(tblperformin);
 
         jPanel19.add(jScrollPane41);
         jScrollPane41.setBounds(10, 40, 940, 310);
 
         dtpi.setDateFormatString("dd/MM/yyyy");
         dtpi.setFont(dtpi.getFont().deriveFont((float)11));
         jPanel19.add(dtpi);
         dtpi.setBounds(10, 20, 120, 24);
 
         btnpi1.setFont(btnpi1.getFont().deriveFont(btnpi1.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnpi1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnpi1.setText("Refresh");
         btnpi1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnpi1ActionPerformed(evt);
             }
         });
         jPanel19.add(btnpi1);
         btnpi1.setBounds(270, 20, 115, 24);
 
         jLabel86.setFont(jLabel86.getFont().deriveFont((float)11));
         jLabel86.setText("Until");
         jPanel19.add(jLabel86);
         jLabel86.setBounds(140, 10, 100, 10);
 
         dtpi1.setDateFormatString("dd/MM/yyyy");
         dtpi1.setFont(dtpi1.getFont().deriveFont((float)11));
         jPanel19.add(dtpi1);
         dtpi1.setBounds(140, 20, 120, 24);
 
         jLabel88.setFont(jLabel88.getFont().deriveFont((float)11));
         jLabel88.setText("From");
         jPanel19.add(jLabel88);
         jLabel88.setBounds(10, 10, 100, 10);
 
         btnexportcall5.setFont(btnexportcall5.getFont().deriveFont(btnexportcall5.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportcall5.setText("Export");
         btnexportcall5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportcall5ActionPerformed(evt);
             }
         });
         jPanel19.add(btnexportcall5);
         btnexportcall5.setBounds(10, 350, 90, 20);
 
         jTabbedPane3.addTab("Inbound", jPanel19);
 
         tabbpanereport.addTab("Performance", jTabbedPane3);
 
         pnlrep2.setBackground(new java.awt.Color(255, 255, 255));
         pnlrep2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnlrep2.setLayout(null);
 
         tblrepsms.setFont(tblrepsms.getFont().deriveFont((float)11));
         tblrepsms.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tblrepsms.setRowHeight(20);
         jScrollPane14.setViewportView(tblrepsms);
 
         pnlrep2.add(jScrollPane14);
         jScrollPane14.setBounds(10, 40, 950, 340);
 
         txtsmsstat.setFont(txtsmsstat.getFont().deriveFont((float)11));
         pnlrep2.add(txtsmsstat);
         txtsmsstat.setBounds(260, 20, 100, 24);
 
         jLabel25.setFont(jLabel25.getFont().deriveFont((float)11));
         jLabel25.setText("Status");
         pnlrep2.add(jLabel25);
         jLabel25.setBounds(260, 10, 100, 10);
 
         jLabel26.setFont(jLabel26.getFont().deriveFont((float)11));
         jLabel26.setText("Direction");
         pnlrep2.add(jLabel26);
         jLabel26.setBounds(360, 10, 100, 10);
 
         btnrepsms.setFont(btnrepsms.getFont().deriveFont(btnrepsms.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnrepsms.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnrepsms.setText("Search Messages");
         btnrepsms.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnrepsmsActionPerformed(evt);
             }
         });
         pnlrep2.add(btnrepsms);
         btnrepsms.setBounds(670, 20, 170, 24);
 
         jLabel28.setFont(jLabel28.getFont().deriveFont((float)11));
         jLabel28.setText("Agen");
         pnlrep2.add(jLabel28);
         jLabel28.setBounds(560, 10, 100, 10);
 
         jLabel43.setFont(jLabel43.getFont().deriveFont((float)11));
         jLabel43.setText("Open From");
         pnlrep2.add(jLabel43);
         jLabel43.setBounds(10, 10, 100, 10);
 
         dcsms1.setDateFormatString("dd/MM/yyyy");
         dcsms1.setFont(dcsms1.getFont().deriveFont((float)11));
         pnlrep2.add(dcsms1);
         dcsms1.setBounds(10, 20, 120, 24);
 
         jLabel44.setFont(jLabel44.getFont().deriveFont((float)11));
         jLabel44.setText("Until");
         pnlrep2.add(jLabel44);
         jLabel44.setBounds(130, 10, 100, 10);
 
         dcsms2.setDateFormatString("dd/MM/yyyy");
         dcsms2.setFont(dcsms2.getFont().deriveFont((float)11));
         pnlrep2.add(dcsms2);
         dcsms2.setBounds(130, 20, 120, 24);
 
         txtsmsticid.setFont(txtsmsticid.getFont().deriveFont((float)11));
         pnlrep2.add(txtsmsticid);
         txtsmsticid.setBounds(460, 20, 100, 24);
 
         jLabel27.setFont(jLabel27.getFont().deriveFont((float)11));
         jLabel27.setText("Ticket No");
         pnlrep2.add(jLabel27);
         jLabel27.setBounds(460, 10, 100, 10);
 
         cbdirrepsms.setFont(cbdirrepsms.getFont().deriveFont((float)11));
         cbdirrepsms.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "INBOUND", "OUTBOUND", "--" }));
         cbdirrepsms.setSelectedIndex(2);
         pnlrep2.add(cbdirrepsms);
         cbdirrepsms.setBounds(360, 20, 100, 24);
 
         cbagenirepcal1.setFont(cbagenirepcal1.getFont().deriveFont((float)11));
         cbagenirepcal1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--", "aan", "ramos", "john", "yusnita", "tri", "fitri", "mariana", "mitha", "dessy", "andrianto", "nurdin", "david", "yudho", "favel", "feronika", "oktaviani", "rudi" }));
         pnlrep2.add(cbagenirepcal1);
         cbagenirepcal1.setBounds(560, 20, 100, 24);
 
         btnexportsms.setFont(btnexportsms.getFont().deriveFont(btnexportsms.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportsms.setText("Export");
         btnexportsms.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportsmsActionPerformed(evt);
             }
         });
         pnlrep2.add(btnexportsms);
         btnexportsms.setBounds(10, 380, 90, 20);
 
         lblrepsmscount.setFont(lblrepsmscount.getFont().deriveFont((float)11));
         lblrepsmscount.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         pnlrep2.add(lblrepsmscount);
         lblrepsmscount.setBounds(880, 0, 40, 10);
 
         lblrepticcount5.setFont(lblrepticcount5.getFont().deriveFont((float)11));
         lblrepticcount5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblrepticcount5.setText("list");
         pnlrep2.add(lblrepticcount5);
         lblrepticcount5.setBounds(920, 0, 40, 10);
 
         tabbpanereport.addTab("SMS", pnlrep2);
 
         pnlrep3.setBackground(new java.awt.Color(255, 255, 255));
         pnlrep3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnlrep3.setLayout(null);
 
         tblrepmail.setFont(tblrepmail.getFont().deriveFont((float)11));
         tblrepmail.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tblrepmail.setRowHeight(20);
         jScrollPane15.setViewportView(tblrepmail);
 
         pnlrep3.add(jScrollPane15);
         jScrollPane15.setBounds(10, 40, 950, 340);
 
         jLabel29.setFont(jLabel29.getFont().deriveFont((float)11));
         jLabel29.setText("Direction");
         pnlrep3.add(jLabel29);
         jLabel29.setBounds(360, 10, 100, 10);
 
         jLabel30.setFont(jLabel30.getFont().deriveFont((float)11));
         jLabel30.setText("Agen");
         pnlrep3.add(jLabel30);
         jLabel30.setBounds(560, 10, 100, 10);
 
         txtmailticid.setFont(txtmailticid.getFont().deriveFont((float)11));
         pnlrep3.add(txtmailticid);
         txtmailticid.setBounds(460, 20, 100, 24);
 
         jLabel31.setFont(jLabel31.getFont().deriveFont((float)11));
         jLabel31.setText("Ticket no");
         pnlrep3.add(jLabel31);
         jLabel31.setBounds(460, 10, 100, 10);
 
         btnrepmail.setFont(btnrepmail.getFont().deriveFont(btnrepmail.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnrepmail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnrepmail.setText("Search Mail");
         btnrepmail.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnrepmailActionPerformed(evt);
             }
         });
         pnlrep3.add(btnrepmail);
         btnrepmail.setBounds(670, 20, 140, 24);
 
         jLabel32.setFont(jLabel32.getFont().deriveFont((float)11));
         jLabel32.setText("Mail subject");
         pnlrep3.add(jLabel32);
         jLabel32.setBounds(260, 10, 100, 10);
 
         txtmailsub.setFont(txtmailsub.getFont().deriveFont((float)11));
         pnlrep3.add(txtmailsub);
         txtmailsub.setBounds(260, 20, 100, 24);
 
         jLabel45.setFont(jLabel45.getFont().deriveFont((float)11));
         jLabel45.setText("Open From");
         pnlrep3.add(jLabel45);
         jLabel45.setBounds(10, 10, 100, 10);
 
         dcmail1.setDateFormatString("dd/MM/yyyy");
         dcmail1.setFont(dcmail1.getFont().deriveFont((float)11));
         pnlrep3.add(dcmail1);
         dcmail1.setBounds(10, 20, 120, 24);
 
         jLabel46.setFont(jLabel46.getFont().deriveFont((float)11));
         jLabel46.setText("Until");
         pnlrep3.add(jLabel46);
         jLabel46.setBounds(130, 10, 100, 10);
 
         dcmail2.setDateFormatString("dd/MM/yyyy");
         dcmail2.setFont(dcmail2.getFont().deriveFont((float)11));
         pnlrep3.add(dcmail2);
         dcmail2.setBounds(130, 20, 120, 24);
 
         cbdirmail.setFont(cbdirmail.getFont().deriveFont((float)11));
         cbdirmail.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "INBOUND", "OUTBOUND", "--" }));
         cbdirmail.setSelectedIndex(2);
         pnlrep3.add(cbdirmail);
         cbdirmail.setBounds(360, 20, 100, 24);
 
         cbagenrepmail.setFont(cbagenrepmail.getFont().deriveFont((float)11));
         cbagenrepmail.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--", "aan", "ramos", "john", "yusnita", "tri", "fitri", "mariana", "mitha", "dessy", "andrianto", "nurdin", "david", "yudho", "favel", "feronika", "oktaviani", "rudi" }));
         pnlrep3.add(cbagenrepmail);
         cbagenrepmail.setBounds(560, 20, 100, 24);
 
         btnexportmail.setFont(btnexportmail.getFont().deriveFont(btnexportmail.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportmail.setText("Export");
         btnexportmail.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportmailActionPerformed(evt);
             }
         });
         pnlrep3.add(btnexportmail);
         btnexportmail.setBounds(10, 380, 90, 20);
 
         lblrepmailcount.setFont(lblrepmailcount.getFont().deriveFont((float)11));
         lblrepmailcount.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         pnlrep3.add(lblrepmailcount);
         lblrepmailcount.setBounds(880, 0, 40, 10);
 
         lblrepticcount7.setFont(lblrepticcount7.getFont().deriveFont((float)11));
         lblrepticcount7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblrepticcount7.setText("list");
         pnlrep3.add(lblrepticcount7);
         lblrepticcount7.setBounds(920, 0, 40, 10);
 
         tabbpanereport.addTab("Email", pnlrep3);
 
         jtab.addTab("Report", tabbpanereport);
 
         jTabbedPane2.setBackground(new java.awt.Color(255, 255, 255));
         jTabbedPane2.setFont(jTabbedPane2.getFont().deriveFont((float)10));
         jTabbedPane2.setOpaque(true);
 
         pninbox1.setBackground(new java.awt.Color(255, 255, 255));
         pninbox1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pninbox1.setLayout(null);
 
         tblmsin.setAutoCreateRowSorter(true);
         tblmsin.setFont(tblmsin.getFont().deriveFont((float)11));
         tblmsin.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Sender", "Read", "Replied", "Messages"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, true, true, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblmsin.setRowHeight(20);
         tblmsin.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblmsinMouseClicked(evt);
             }
         });
         jScrollPane33.setViewportView(tblmsin);
 
         pninbox1.add(jScrollPane33);
         jScrollPane33.setBounds(10, 40, 930, 180);
 
         dtmsi.setDateFormatString("dd/MM/yyyy");
         dtmsi.setFont(dtmsi.getFont().deriveFont((float)11));
         dtmsi.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 dtmsiPropertyChange(evt);
             }
         });
         pninbox1.add(dtmsi);
         dtmsi.setBounds(10, 20, 120, 24);
 
         dtmsi1.setDateFormatString("dd/MM/yyyy");
         dtmsi1.setFont(dtmsi1.getFont().deriveFont((float)11));
         dtmsi1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 dtmsi1PropertyChange(evt);
             }
         });
         pninbox1.add(dtmsi1);
         dtmsi1.setBounds(130, 20, 120, 24);
 
         jLabel66.setFont(jLabel66.getFont().deriveFont((float)11));
         jLabel66.setText("From :");
         pninbox1.add(jLabel66);
         jLabel66.setBounds(10, 10, 100, 10);
 
         jLabel67.setFont(jLabel67.getFont().deriveFont((float)11));
         jLabel67.setText("Until :");
         pninbox1.add(jLabel67);
         jLabel67.setBounds(130, 10, 100, 10);
 
         jLabel70.setFont(jLabel70.getFont().deriveFont((float)11));
         jLabel70.setText("messages :");
         pninbox1.add(jLabel70);
         jLabel70.setBounds(10, 230, 100, 20);
 
         txtimsg3.setColumns(20);
         txtimsg3.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
         txtimsg3.setLineWrap(true);
         txtimsg3.setRows(5);
         jScrollPane34.setViewportView(txtimsg3);
 
         pninbox1.add(jScrollPane34);
         jScrollPane34.setBounds(110, 230, 830, 76);
 
         btnreplymsg.setFont(btnreplymsg.getFont().deriveFont(btnreplymsg.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnreplymsg.setText("Reply");
         btnreplymsg.setEnabled(false);
         btnreplymsg.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnreplymsgActionPerformed(evt);
             }
         });
         pninbox1.add(btnreplymsg);
         btnreplymsg.setBounds(260, 20, 90, 24);
 
         btndelmsg.setFont(btndelmsg.getFont().deriveFont(btndelmsg.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btndelmsg.setText("Delete");
         btndelmsg.setEnabled(false);
         btndelmsg.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btndelmsgActionPerformed(evt);
             }
         });
         pninbox1.add(btndelmsg);
         btndelmsg.setBounds(360, 20, 90, 24);
 
         jTabbedPane2.addTab("InBox", pninbox1);
 
         pnoutbox1.setBackground(new java.awt.Color(255, 255, 255));
         pnoutbox1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnoutbox1.setLayout(null);
 
         tblmsou.setAutoCreateRowSorter(true);
         tblmsou.setFont(tblmsou.getFont().deriveFont((float)11));
         tblmsou.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Sent Time", "Send by", "Recipient", "Messages"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblmsou.setRowHeight(20);
         tblmsou.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblmsouMouseClicked(evt);
             }
         });
         jScrollPane35.setViewportView(tblmsou);
 
         pnoutbox1.add(jScrollPane35);
         jScrollPane35.setBounds(10, 40, 930, 180);
 
         jLabel71.setFont(jLabel71.getFont().deriveFont((float)11));
         jLabel71.setText("From :");
         pnoutbox1.add(jLabel71);
         jLabel71.setBounds(10, 10, 100, 10);
 
         dtmso.setDateFormatString("dd/MM/yyyy");
         dtmso.setFont(dtmso.getFont().deriveFont((float)11));
         dtmso.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 dtmsoPropertyChange(evt);
             }
         });
         pnoutbox1.add(dtmso);
         dtmso.setBounds(10, 20, 120, 24);
 
         jLabel72.setFont(jLabel72.getFont().deriveFont((float)11));
         jLabel72.setText("Until :");
         pnoutbox1.add(jLabel72);
         jLabel72.setBounds(130, 10, 100, 10);
 
         dtmso1.setDateFormatString("dd/MM/yyyy");
         dtmso1.setFont(dtmso1.getFont().deriveFont((float)11));
         dtmso1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 dtmso1PropertyChange(evt);
             }
         });
         pnoutbox1.add(dtmso1);
         dtmso1.setBounds(130, 20, 120, 24);
 
         jLabel74.setFont(jLabel74.getFont().deriveFont((float)11));
         jLabel74.setText("messages :");
         pnoutbox1.add(jLabel74);
         jLabel74.setBounds(10, 230, 100, 20);
 
         txtimsg4.setColumns(20);
         txtimsg4.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
         txtimsg4.setLineWrap(true);
         txtimsg4.setRows(5);
         jScrollPane36.setViewportView(txtimsg4);
 
         pnoutbox1.add(jScrollPane36);
         jScrollPane36.setBounds(110, 230, 830, 90);
 
         btncomposemsg.setFont(btncomposemsg.getFont().deriveFont(btncomposemsg.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btncomposemsg.setText("Compose");
         btncomposemsg.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btncomposemsgActionPerformed(evt);
             }
         });
         pnoutbox1.add(btncomposemsg);
         btncomposemsg.setBounds(360, 20, 90, 24);
 
         btndelmsg1.setFont(btndelmsg1.getFont().deriveFont(btndelmsg1.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btndelmsg1.setText("Delete");
         btndelmsg1.setEnabled(false);
         btndelmsg1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btndelmsg1ActionPerformed(evt);
             }
         });
         pnoutbox1.add(btndelmsg1);
         btndelmsg1.setBounds(260, 20, 90, 24);
 
         jTabbedPane2.addTab("OutBox", pnoutbox1);
 
         jtab.addTab("Messaging", jTabbedPane2);
 
         pnlinf.setBackground(new java.awt.Color(255, 255, 255));
         pnlinf.setFont(pnlinf.getFont().deriveFont((float)10));
 
         jPanel3.setBackground(new java.awt.Color(255, 255, 255));
         jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel3.setLayout(null);
 
         jTextArea1.setEditable(false);
         jTextArea1.setColumns(20);
         jTextArea1.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea1.setRows(5);
         jTextArea1.setTabSize(5);
         jTextArea1.setText("BENGKEL ANJ RENT\n\t\t\t\t\t\nNo  \tNama bengkel\t\t\tAlamat\t\t\t\t\t\tPIC\t\tNo Telp Bengkel \t     No Fax Bengkel\n1    \t Family Serviceindo Kedoya\t\tJl. Arteri kedoya no 16\t\t\t\tJohan\t\t021-5680077\t     021 – 56007126\n2     \t Family Serviceindo Cibitung\t\tJl.Sumatra D4 kawasan Industri MM 2100 CIBITUNG\tEddy/Rofik\t021-89982288\t     021 – 89982233\n3    \t Family Serviceindo Cengkareng\tJl.Bojong raya tepat di depan SMU Vianny\t\t\t\n");
         jTextArea1.setBorder(null);
         jTextArea1.setOpaque(false);
         jScrollPane2.setViewportView(jTextArea1);
 
         jPanel3.add(jScrollPane2);
         jScrollPane2.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("Bengkel ANJ Rent", jPanel3);
 
         jPanel4.setBackground(new java.awt.Color(255, 255, 255));
         jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel4.setLayout(null);
 
         jTextArea2.setColumns(20);
         jTextArea2.setEditable(false);
         jTextArea2.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea2.setRows(5);
         jTextArea2.setTabSize(5);
         jTextArea2.setText("POOL ANJ RENT\t\n\t\t\t\t\nNo\tNama bengkel\t\t\tAlamat\t\t\t\t\t\tPIC\t\tNo Telp Pool \tNo Fax Pool\n1\tANJ Rent Head Office Panjang\tJl. Arteri Kelapa Dua No 16 , Kebon Jeruk Jakarta Barat\tPurwanto\t021 – 5367 0800\t021 – 549 1021\n2\tANJ Rent Cabang Cibitung\t\tJl.Sumatra D4 kawasan Industri MM 2100 CIBITUNG\tMuktar\t\t021 – 89982288\t021 – 89982233\n3\tANJ Rent Cabang Cengkareng\tJl.Bojong raya tepat di depan SMU Vianny\t\tMario\t\t021 – 706 13 421\t021 – 583 04829\n");
         jTextArea2.setBorder(null);
         jTextArea2.setOpaque(false);
         jScrollPane20.setViewportView(jTextArea2);
 
         jPanel4.add(jScrollPane20);
         jScrollPane20.setBounds(0, 0, 970, 430);
 
         pnlinf.addTab("Pool ANJ Rent", jPanel4);
 
         jPanel5.setBackground(new java.awt.Color(255, 255, 255));
         jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel5.setLayout(null);
 
         jTextArea3.setColumns(20);
         jTextArea3.setEditable(false);
         jTextArea3.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea3.setRows(5);
         jTextArea3.setTabSize(5);
         jTextArea3.setText("EMERGENCY\n\t\t\t\nEMERGENCY\tNama\t\tNo Telp Handphone\tNo Telp Emergency\nPIC  :\t\tDhani\t\t08159312202\t\t021-536 730 35\nStaff  :\t\tTogi\t\t081311064071\t\n\t\tAmung\t\t081385998098\t\n\t\tDedy\t\t085691510607\t\n\t\tGunawan\t\t08179929339\t\n\t\n\t\t\nAlamat Emegency\t\t\t\n\t\t\t\nJl.Bojong raya tepat di depan SMU Vianny\t\t\t\n");
         jTextArea3.setBorder(null);
         jTextArea3.setOpaque(false);
         jScrollPane21.setViewportView(jTextArea3);
 
         jPanel5.add(jScrollPane21);
         jScrollPane21.setBounds(0, 0, 990, 430);
 
         pnlinf.addTab("Emergency", jPanel5);
 
         jPanel6.setBackground(new java.awt.Color(255, 255, 255));
         jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel6.setLayout(null);
 
         jTextArea4.setColumns(20);
         jTextArea4.setEditable(false);
         jTextArea4.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea4.setRows(5);
         jTextArea4.setTabSize(5);
         jTextArea4.setText("DATA CSO & JAPAN DESK PT. ANJ RENT\t\t\t\t\n\t\t\t\t\nNo\tNama\tHand Phone\tEmail\t\t\tkode CSO\n1\tSiska\t087882817788\tsiska@anjrent.com\t\tCSO078\n2\tFirda\t08111558117\tfirda@anjrent.com\t\tCSO063\n3\tIca\t08111558115\tica@anjrent.com\t\tCSO070\n4\tInez\t0811839744\tinez@anjrent.com\t\tCSO001\n5\tLina\t081806981333\tlina@anjrent.com\t\tCSO059\n6\tRina\t08111558114\trina@anjrent.com\t\tCSO062\n7\tTasya\t081315453800\ttasya@anjrent.com\tCSO067\n8\tRoy\t087881600805\troy@anjrent.com\t\tCSO071\n9\tRosi\t0811839681\trosi@anjrent.com\t\tCSO006 \n10\tDessy\t08119001882\tdessy@anjrent.com\tCSO079\n11\tRizky\t08111558119\trizky@anjrent.com\t\tCSO073\n12\tSarah\t08119201499\tsarah@anjrent.com\tCSO080\n13\tStefy\t08111558116\tstefy@anjrent.com\t\tCSO066\n14\tFani\t087885158737\tstephanie@anjrent.com\tCSO081\n15\tArai\t0811975655\tarai@anjrent.com\t\n16\tTaguchi\t0811152936\ttaguchi@anjrent.com\t\n17\tSaga\t081319333216\tsaga@anjrent.com \t\n");
         jTextArea4.setBorder(null);
         jTextArea4.setOpaque(false);
         jScrollPane22.setViewportView(jTextArea4);
 
         jPanel6.add(jScrollPane22);
         jScrollPane22.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("CSO and Japan Desk", jPanel6);
 
         jPanel7.setBackground(new java.awt.Color(255, 255, 255));
         jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel7.setLayout(null);
 
         jTextArea5.setColumns(20);
         jTextArea5.setEditable(false);
         jTextArea5.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea5.setRows(5);
         jTextArea5.setTabSize(5);
         jTextArea5.setText("\tDATA BENGKEL REKANAN\t\t\t\t\t\t\n\t\t\t\t\t\t\t\n\tKota\t\t\tJenis\t\tNama\t\t\t\tAlamat\t\t\t\t\t\tTelp\t\t\tFax\t\t\tPIC\n1\tAceh\t\t\tUmum\t\tPutra jaya \t\t\tJL.Imuem leung bata km 2,5 Panterik\t\t\t(0651) 22694\t\t(0651) 29873\t\tPak Hendrik\n\t\t\t\t\t\t\t\n2\tBali\t\t\tMitsubishi\tBumen Redja Abadi\t\tJL. Imam Bonjol 375 R\t\t\t\t(0361) 483002\t\t(0361) 484133\t\tPrasetyo Theriady\n\tBali Gianyar\t\tDaihatsu\t\tPT. Astra International Tbk\t\tBr. Blah Tanah Ds Batuan Sukawati\t\t\t(0361) 974464, 974693\t(0361) 974463\t\tWahyudi\n\tBali, Denpasar\t\tToyota\t\tPT. Astra International Tbk\t\tJL. Hos. Cokroaminoto  No. 81, Ubung, Denpasar\t\t(0361) 422000\t\t(0361) 410311\t\tKetut Diyana (08883610555) PAK WAYAN DARMAWAN ADM SERVICE\n\t\t\t\t\t\t\t\n3\tBalikpapan\t\tMitsubishi\tMandau Berlian Sejati \t\tJL. MT Haryono No. 36 KM 5 Ring Road\t\t\t(0542) 875479\t\t(0542) 875464\t\tYB Yaniko, P'HERY ACC\n\tBalikpapan\t\tToyota\t\tAuto 2000\t\t\tJL. Jend Sudirman No. 29 Balikpapan\t\t\t(0542) 732000\t\t(0542) 734011\t\tFUKUH KA BENG,Taufik\n\tBalikpapan\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Jend. Sudirman No. 50\t\t\t\t(0542) 419000-760000,763819(0542) 442044\t\tJAMAL / Tatang Setiawan\n\tBalikpapan\t\tALL CAR\t\tFamily Serviceindo\t\t\t\t\t\t\t\t\tNur 087878831504\tJoko 0819-5580844\n\tBalikpapan\t\tBAN\t\tPt. Linda Hauta Wijaya\t\tJl. Jendral sudirman no 263 \t\t\t\t(0542) 427966/7\t\tIbu NANA\n\t\t\t\t\t\t\t\n4\tBandung\t\t\tMitsubishi\tPT Surya Putra Sarana\t\tJL. Jend Sudirman no 776-778\t\t\t\t(022) 6033720/2\t\t(022) 6030563\t\tPak Iwan suryadi\n\tBandung\t\t\tSuzuki\t\tPT. Nusantara Jaya Sentosa\t\tJL. Soekarno Hatta No. 289 Bandung 40234\t\t(022) 5204645\t\t(022) 5203942\t\tAri Rakhmadi\n\tBandung\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Asia Afrika No. 127\t\t\t\t(022) 4238977\t\t(022) 4214440\t\tTRISANTOSO\n\tBandung\t\t\tBAN\t\tPD Lautan Ban\t\t\tJL. Astana Anyar 81 Bandung 40241\t\t\t022) 4203443\t\t(022) 4209657\t\tIBU SUSI\n\tBandung\t\t\tMitsubishi\tPT Surya Putra Sarana\t\tJL. Abdulrahman saleh no 4\t\t\t\t022-6031040\t\t(022) 6030636\t\tPak Iwan suryadi\n\tBandung\t\t\tToyota\t\tPT. Astra International Tbk\t\tJL. Soekarno Hatta No. 145 Bandung\t\t\t(022) 6030450\t\t(022) 6031559\t\tAgus Ryanto\n\tBandung\t\t\tMitsubishi\tSrikandi Daimond Motor\t\tJL. Soekarno Hatta No. 342\t\t\t\t(022) 5407000\t\t(022) 5406776\t\tUnang Sunarya MNGR, SA YUYUNG,DENI\n\tBandung\t\t\tHonda\t\tIstana bandung raya Motor\t\tJl. Cicende No 18\t\t\t\t\t(022) 4240888\t\t(022) 4233629\t\tP'ANDRI SUDJANI Svce Mngr HP 0812-2157215\n\tBandung\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Raya Cibeureum 42\t\t\t\t(022) 6031568, 6031058\t(022) 6035615\t\tJunjung Siregar IBU MIKE 022-6031058\n\tBandung\t\t\tNissan\t\tINDOMOBIL TRADA NISSAN\t\tJL. Soekarno Hatta NO 382\t\t\t\t(022) 5207777\t\t(022) 5207181\t\tPETRUS KURNIA\n\tBandung\t\t\tAUTO 2000 Pasteur\tAUTO 2000 \t\tJl. Dr. Djundjunan No. 192 Bandung 40163 \t\t(022) 2000100 \t\t\t\t\tMunif Latief KABENG\n\tBandung\t\t\tAUTO 2000 Soekarno Hatta\tAUTO 2000 \t\tJl. Soekarno - Hatta No.145 Bandung 40223\t\t(022) 6022000 \t\t\t\t\tAgus Riyanto Kabeng\n\t\t\t\t\t\t\t\n5\tBanjarmasin\t\tDaihatsu\t\tNusantara Indah \t\t\tJL. A. yani km 4,5\t\t\t\t\t(0511) 3262791\t\t(0511) 3272900\t\tPak Arbani\n\tBanjarmasin\t\tMitsubishi\tPT sumber berlian motors\t\tJl. km 10.3 no 1 rt 5 kertak hanya banjar kalsel\t\t(0511) 4281699\t\t(0511) 4281664/ 65\t\tIr. H. Ajie MANAGER ,JAMAL KABENG\n\tBanjarmasin\t\tToyota\t\tWira Megah Profitamas\t\tJL. Jend. A. Yani KM 10 Kertak Hanyar\t\t\t(0511) 3272000\t\t(0511) 3263000\t\tHasbullah PAK HARIS SA\n\tBanjarmasin\t\tMitsubishi\tSumber Berlian Motor\t\tJL. Jend. A. Yani KM 5.5 No. 51\t\t\t(0511) 3252216\t\t(0511) 3255657\t\tIr. H. Ajie\n\tBanjarmasin\t\tIsuzu\t\tAstra International  ISUZU\t\tKALIMANTAN\t\t\t\t\t(0511)3265460, 3267000, 3273621 \t(0511)3267465 \t\n\t\t\t\t\t\t\t\n6\tBanyuwangi\t\tToyota\t\tAUTO 2000 Cab Pembantu Jember\tJL A.Yani no 7\t\t\t\t\t(0333) 422000\t\t(0333) 424953\t\tSA Pak Samuji\n\tBanyuwangi\t\tMitsubishi\tPT. Mayangsari Berlian Motor\tJL. Yos Sudarso, No. 69 A Banyuwang\ti\t\t(0333) 424417,424418\t(0333) 424535\t\tNoerhidayat (081559707300)\n\t\t\t\t\t\t\t\n7\tBatam \t\t\tToyota\t\tPt. Agung Automall\t\t\tJl. Yos Sudarso\t\t\t\t\t(0778)  427585\t\t(0778) 427589\t\tKabeng Bpk Eddy Rahmansyah\n\t\t\t\t\t\t\t\n8\tBaturaja\t\t\tSuzuki\t\tPT. Thamrin Bersaudara\t\tJL. Jend. A. Yani No. 99 Baturaja 31112\t\t\t( 0735 ) 321026\t\t\t\t\tBpk. Laily Ramadhan/ Iwan\n\tBaturaja\t\t\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Jend. A. Yani No. 319 Baturaja\t\t\t( 0735 ) 322747, 321333\t ( 0735 ) 320510\t\tBpk. Tarmizi/Bpk. Suparman\n\t\t\t\t\t\t\t\n9\tBengkulu\t\t\tMitsubishi\tLautan berlian utama motor\t\tJL. S. Parman no 61 padang jati Bengkulu\t\t(0736) 20062, 21934\t(0736)  21134\t\n\tBengkulu\t\t\tSuzuki\t\tSuzuki Service\t\t\tJl M.Jend Sutoyo No 88 \t\t\t\t(0736) 21010\t\t(0736) 26446\t\tIBU MEI\n\t\t\t\t\t\t\t\n10\tBogor\t\t\tSuzuki\t\tPT. RMK - Bogor \t\t\tJL. Raya Tajur No. 91, Bogor\t\t\t\t(0251) 8391273,8352282\t(0251) 8352281\t\tPAK AWANG / / HARI SA/  Handy Tjahjadinata\n\tBogor\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Pajajaran No. 22\t\t\t\t(0251) 325737\t\t(0251) 326633\t\tSapto Pamungkas\n\tBogor\t\t\tMitsubishi\tPT. Prabu pandawa motor\t\tJL.Pajajaran no 20 \t\t\t\t\t(0251) 331201-2\t\t(0251) 8312874\t\tPak Imat suyatman\n\t\t\t\t\t\t\t\n11\tCENTURY \t\tMBAK TATI\t\t\t\t\t\t\t\t\t\t\t(021) 5302423\t\t\n\t\t\t\t\t\t\t\n12\tCibinong\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Raya Bogor - Jakarta KM 43 Pabuaran\t\t(021) 8757870\t\t\t\t\tKhaerullah\n\t\t\t\t\t\t\t\n13\tCikarang\t\t\t\t\tOTO ZONE\t\t\tJL. MH. Thamrin Kav. 133 C Lippo Cikarang\t\t(021) 8990 6611\t\t(021) 8990 6622\t\tHalim\n\tCikarang\t\t\tToyota\t\tAuto 2000\t\t\tJl. M. H. Thamrin Kav. 168, Lippo Cikarang\t\t(021) 8990 2000\t\t(021) 8990 3758/57\t\tPak Daniel , Pak PARYOTO, PAK ODRADI\n\tCikarang\t\t\tHonda\t\tPT. Prospect Motor HONDA\t\tJL. MH. Thamrin No. 152 Lippo Cikarang\t\t(021) 8974142/43\t\t(021) 8974144\t\tArif Harsono\n\t\t\t\t\t\t\t\t\n14\tCilegon\t\t\tToyota\t\tTunas Ridean Tbk\t\t\tJL. Raya Cilegon KM 14\t\t\t\t(0254) 394777/394789\t(0254) 391580\t\tYasri Abdullah\n\tCilegon\t\t\tSuzuki\t\tPT. RMK - Cilegon\t\t\tJL. Raya Serang KM 1, Cibeber, Cilegon\t\t\t(0254) 381500\t\t(0254) 380563\t\tLukito (02549195455)\n\tCilegon\t\t\tMitsubishi\tSetia Kawan menara motor\t\tJl. Raya Cilegon No. 101, Cilegon \t\t\t(0254) 391-267 ; 391-773 \t(0254) 391-171 \t\trimbun\n\t\t\t\t\t\t\t\n15\tCimone\t\t\tSuzuki\t\tPT. RMK - Cimone\t\t\tJL. Merdeka  No. 1, Cimone Tangerang\t\t\t(021) 5517876\t\t(021) 5523968\t\tApendi\n\t\t\t\t\t\t\t\n16\tCirebon\t\t\tSuzuki\t\tCinta Damai Putra Bahagia\t\tJl. Kalijaga No 117 \t\t\t\t\t(0231) 230506 / 230477 \t(0231) 210060\t\n\tCirebon\t\t\tToyota\t\tAuto 2000\t\t\tJL. Brigjen Darsono 14 by pass\t\t\t(0231) 232000\t\t(0231) 202009\t\tPak Winawan SA PAK ALIF , PAK TRI ADM, MULYADI SA\n\tCirebon\t\t\t\t\tPENYET VARIASI ALARM\t\t\t\t\t\t\t\t(0231) 230308\t\t\t\t\tKO ATIK\n\tCirebon\t\t\t\t\tRaharja Putra Ban\t\t\tJL. kesambi No 50\t\t\t\t\t(0231) 231129\t\t(0231) 231129\t\t\t\t\tIsye Slamet, SANTI\n\tCirebon\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Tuparev No. 76\t\t\t\t\t(0231) 206691\t\t(0231) 205557\t\t\t\t\tAris Dwinanto\n\tCirebon\t\t\t\t\tPt Bintang Timur\t\t\tJL. Buyut no 4 \t\t\t\t\t(0231) 201040\t\t(0231) 231040\t\tPak Tomy Hadi\n\t\t\t\t\t\t\t\n17\tDenpasar Cokro\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Cokroaminoto 52\t\t\t\t(0361) 429000\t\t(0361) 410928\t\tRatno Yunanto\n\tDenpasar Sanur\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Ngurah Rai 17 X By Pass Sanur\t\t\t(0361) 288323, 288345\t(0361) 288002\t\tWahyudi\n\t\t\t\t\t\t\t\n18\tJakarta\t\t\tHonda\t\tPt Honda autoland\t\t\tjalan raya boulevard barat blok xb 1-2 kelapa gading \t(021) 4501858 / 4501868\t(021) 4529202\t\tibu ERI service\n\tJakarta\t\t\tHonda\t\tPT. IKM\tGedung IKM,\t\t JL. Daan Mogot No. 6\t\t\t\t(021) 5644888\t\t(021) 56962357\t\tRudy Widjaja SA PAK WENI\n\tJakarta\t\t\t\t\tToyota astra motor\t\t\tSUDIRMAN\t\t\t\t\t(021) 2511701-02-07 BOOKING\t(021) 2511700\tDARMA BAYU , SA PAK KUSNAN, JAKA\n\tJakarta\t\t\tHino\t\tPT Hino Motor Sales Indonesia\tJl MT haryono kav 8\t\t\t\t(021) 8564570 / 8564480\t(021) 8515731 / 8517550\tP'DEDEN FAX 5917887\n\tJakarta\t\t\tSuzuki\t\tPT. RMK - Kebon Jeruk\t\tJL. Raya Panjang No. 12  Kebon Jeruk Jakarta Barat\t(021) 5492727\t\t(021) 5493031\t\tAntonius Sumarsono\n\tJakarta\t\t\tHonda\t\tPT. Istana Kebon Jeruk\t\tJL. Panjang No. 200 Jakarta Barat\t\t\t(021) 5492580-81\t\t(021) 5493464\t\tOdik Suhendra (08129928257)\n\tJakarta Barat\t\tToyota\t\tAuto 2000 Daan Mogot\t\tJL. Daan Mogot No.146-147 Jakarta Barat 11510\t\t(021) 5642000\t\t(021) 5688719\t\tDedi Suhendi\n\tJakarta Barat\t\tSemua\t\tPT. Family Servisindo\t\tJL. Arteri Kedoya No.16 Kedoya \t\t\t(021) 5680077\t\t(021) 56007126\t\tWidionarko\n\tJakarta Cibitung\t\tSemua\t\tPT. Family Servisindo\t\tJL. Sumatra D 4 Kawasan Industri MM2100\t\t(021) 89982288\t\t(021) 89982233\t\tRofiq\n\tJakarta klender\t\tAstrido\t\tAstrido Klender\t\t\tKlender\t\t\t\t\t\t021-8610322\t\t021-8610320\t\tP'Tri\n\tJakarta Pusat\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. P. Jayakarta No. 28\t\t\t\t(021) 6590606, 6590600\t(021) 6590610\t\tAgus S.M\n\tJakarta Pusat\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Balikpapan No. 8\t\t\t\t(021) 63864386-87\t\t(021) 63862223\t\tShandy\n\tJakarta Selatan\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Raya Ciputat No. 22\t\t\t\t(021) 7651638, 7651642-44\t(021) 7651639\t\tDjoko Untung\n\tJakarta Selatan\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Sultan Iskandar Muda No. 15\t\t\t(021) 7291011\t\t(021) 7291008\t\tDolf Valentino\n\tJakarta Serpong\t\tDaihatsu\t\tPT. Astra International Tbk - Daihatsu\tBSD City Blok 405/2A Sektor 8 Tangerang\t\t(021) 5380011\t\t(021) 5383888\t\tDARJAT / RAMLI\n\tJakarta Utara\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Raya Pluit Selatan No. 4\t\t\t\t(021) 6690755, 6612341\t(021) 6695092\t\tTeguh Santoso\n\tJakarta Utara\t\tNissan\t\tNissan Diesel, Sunter\t\tJL. Danau Sunter Selatan Blok O 2 No. 5\t\t(021) 6507150\t\t6507161\t\t\tNyoman Puja\n\tJakarta Utara\t\tSuzuki\t\tPT. Sumberbaru sentral mobile \tJl. Inspeksi Kalimalang Kav. Billy & Moon RT 03/01\t(021) 869 03019 \t\t(021) 869 03021 \t\tIbu Anna\n\tJakarta Utara\t\tToyota\t\tPT. Toyota Astra Motor\t\tJL. Laks. Yos Sudarso Sunter II Jakarta 14330\t\t(021) 6515551\t\t(021) 6512238\t\tBudi riyanto FAX 65307639\n\tJakarta Utara\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Yos Sudarso Kav. 24, Sunter II\t\t\t(021) 6511002, 6511720\t(021) 6511043\t\tSudjarwo Priyono\n\tJakarta Utara\t\tIsuzu\t\tPT. Astra International Isuzu\t\tJL. Yos Sudarso No. 30 Sunter\t\t\t\t(021) 65834684\t\t(021)65304434  \t\tArif  \n\t\t\t\t\t\t\t\n19\tJambi\t\t\tSuzuki\t\tCV Jaya Indah Motor\t\tJl. KH Dewantara 66/68 simpang kawat jambi\t\t(0741) 63362 \t\t(0741) 60675 \t\tZul fakar\n\tJambi\t\t\tMitsubishi\tPT Kerinci Permata Motors\t\tJl Abunjani no 10 Simpang 3 sipin Jambi\t\t\t(0741) 60300 /  616 66\t(0741) 60263\t\tP'ERWIN KA BENG, NURAINI/IKE ADM LIA SERVICE\n\tJambi\t\t\tDaihatsu\t\tSurya sentosa primatama\t\tJl kol Abunjani no 9\t\t\t\t\t(0741) 670055-56\t\t(0741) 61573\t\tIMELDA,  \n\t\t\t\t\t\t\t\t\n20\tJember\t\t\tToyota\t\tAUTO 2000 \t\t\tJl Hayam Wuruk No 54 Jember\t\t\t(0331) 422000\t\t\t\t\tKa Beng Pak Lutfi HP 0815-59807529\n\tJember\t\t\tMitsubishi\tPT Mayang Sari Berlian Motors\tJL Gajahmada no 224 A\t\t\t\t(0331) 484366/484367\t(0331) 484633\t\tIbu Dyah\n\tJember\t\t\tDaihatsu\t\tBesuki Raya Motor\t\t\tJL. Hayam Wuruk No. 40\t\t\t\t(0331) 421891\t\t(0331) 482670\t\tArief Subagyo\n\t\t\t\t\t\t\t\n21\tJepara\t\t\tSuzuki\t\t\t\t\t\t\t\t\t\t\t\t0291-3367878 \t\t\t\t\tPAK SUGENG BRAND MANAGER\n\t\t\t\t\t\t\t\n22\tKarawang\t\tSuzuki\t\tPT. RMK - Karawang\t\tJL. Jend. A. Yani No. 36, Karawang\t\t\t(0267) 403085\t\t(0267) 403083\t\tTantri (08128741667)\n\tKarawang\t\tToyota\t\tAUTO 2000 \t\t\t\t\t\t\t\t\t(0267) 412000 \t\t(0267) 402570 \t\tPAK BOBY 0816-787918 P'DASEP\n\tKarawang\t\tASTRA\t\tdaihatsu dan isuzu jg ac\t\tjl. kerta bumi no 38 \t\t\t\t(0267) 402539\t\t(0267) 402006\t\tbpk syahrul, kabeng ato tojiri\n\t\t\t\t\t\t\t\n23\tKbn Jeruk\t\t\tNissan\t\tINDOMOBIL TRADA NISSAN\t\tARTERI KELAPA 2 NO 30\t\t\t\t(021) 5362477\t\t(021)  5324756\t\tP'ALEX, IBU FANIE\n\t\t\t\t\t\t\t\n24\tKediri\t\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. Soekarno Hatta 152\t\t\t\t(0354) 688501-2, 684412\t(0354) 684576\t\tA. Chuzaeni\n\tKediri\t\t\tSuzuki\t\tgaruda motor\t\t\tJl. Brawijaya No. 34\t\t\t\t(0354) 681562\t\t(0354) 661371\t\t 081556539655\n25\tKendari SULTENG\t\tSemua\t\tSetia Kawan Motor Suzuki\t\tJL R.Suprapto No 58 kendari\t\t\t\t(0401) 326320\t\t(0401) 3126402\t\tBELUM REKANAN\n\t\t\t\t\t\t\t\n26\tKetapang\t\tMitsubishi\tPd Sumber baru motor\t\tJL.brigjen Katamso KM 2.5 sukaharja \t\t\t(0534) 31200\t\t\t\t\tIBU Kapui\n\t\t\t\t\t\t\t\n27\tKudus - Jateng\t\tToyota\t\tCv Surya Indah Motor\t\tJL.Raya Kudus pati KM 4\t\t\t\t(0291) 433215\t\t(0291) 432091\t\tPak Hartono\n\t\t\t\t\t\t\t\n28\tLampung\t\t\tSuzuki\t\tPT. Persada Lampung Raya\t\tJL. Zainal Abidin Pagar Alam No. 2 Lampung\t\t(0721) 703325, 703329\t(0721) 704953\t\tBpk. Suyono\n\tLampung\t\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. Jend. A. Yani No. 1 Tanjung Karang\t\t\t(0721) 269000\t\t(0721) 241283\t\tKahono\n\tLampung\t\t\tBAN\t\tAnugrah Ban \t\t\tJL. Laksamana Hayati No. 8 Telukbetung\t\t(0721) 486458\t\t(0721) 482386\t\tSansan\n\tLampung bandar jaya\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Proklamator Bandar Jaya\t\t\t\t(0725) 528088-528152\t(0725) 528294\t\tPAK Eko Supriyanto\n\tLampung bandar lampung\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. KH. Moch. Salim No. 29 Way Lunik - Panjang\t\t( 0721 ) 31656, 341111\t( 0721 ) 343123\t\tBpk. Misri\n\tLampung Kotabumi\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Jend Sudirman No. 202 Kotabumi - Lampung\t( 0724 ) 22563\t\t( 0724 ) 21983\t\tPak Slamet\n\tlampung Metro\t\tSuzuki\t\tPT. Sriwijaya Metropersada\t\tJL. Jend. Soedirman Ganjaragung 14/11 Metro\t\t ( 0725 ) 42386\t\t ( 0725 ) 42336\t\tBpk. Zuki\n\tLampung Metro\t\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Jend. Sudirman No. 198 Metro - Lampung\t\t ( 0725 ) 42588\t\t ( 0725 ) 42522\t\tBpk. Nyoman\n\tLampung Selatan\t\tToyota\t\tPT. Astra International Tbk \t\tJL. Raya Haji Mena No. 2000, Natar 35362\t\t ( 0721 ) 782000\t \t( 0721 ) 789176\t\tBpk. Elfasri\n\tlampung Tanjung Karang\tToyota\t\tPT. Astra International Tbk\t\tJL. Raden Intan No. 61-A Tanjung Karang 35118\t\t ( 0721 ) 252000\t\t ( 0721 ) 264923\t\tBpk. Murad\n\tLampung Tenggiri\t\tMitsubishi\tLautan berlian utama motor\t\t \t\t\t\t\t( 0721 ) 483000\t\t ( 0721 ) 481765\t\tPak Juanda SA X\n\t\t\t\t\t\t\t\n29\tMadiun\t\t\tDaihatsu\tPt Jolo Indah Motor\t\tJL Cokrominoto no 96\t\t\t\t(0351) 451275 / 464264\t\t\t\tIBU ANA\n\tMadiun\t\t\tToyota\t\tAUTO 2000 \t\t\tJL Cokrominoto no 47\t\t\t\t(0351) 492000\t\t(0351) 493573\t\tPKS DENGAN BASUKI RAHMAD SA SYAIFUL BAHRI\n\tMadiun\t\t\tIsuzu\t\tJolo Motor ASTRA INTENATIONAL\tJl. Urip sumoharjo no 13 Madiun\t\t\t(0351) 464264\t\t(0351) 455021\t\tKepala bengkel bapak andry 081330010280\n\t\t\t\t\t\t\t\n30\tMagelang\t\tToyota\t\tNasmoco toyota\t\t\tJL. Raya magelang km 5 Magelang\t\t\t(0293) 326871\t\t(0293) 326611\t\tIbu Jamela\n\tMagelang\t\tBAN\t\tSetiawan spooring\t\tJL Urip Sumoharjo no 169 \t\t\t\t(0293) 360890\t\t(0293) 360890\t\tIBU SISKA 0888-694525 Adm,PAK Wahyu\n\t\t\t\t\t\t\t\n31\tMakasar\t\t\tToyota\t\tPT. Hadji Kalla II\t\t\tJL. Urip Sumoharjo No. 110 Ujung Pandang\t\t(0411) 448844\t\t(0411) 449303\t\n\tMakasar\t\t\tMitsubishi\tBosowa Berlian Motor\t\tJL. Urip Sumoharjo No. 266 KM 4\t\t\t(0411) 444444\t\t(0411) 447742\t\tZakaria Lande\n\tMakasar\t\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. Urip Sumoharjo 64\t\t\t\t(0411) 449111, 449911\t(0411) 449359\t\tEndro Sasongko SDH BUKAN ) SA PAK ISMAIL HP 0852-99300657\n\t\t\t\t\t\t\t\n32\tMalang\t\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. A. Yani 175\t\t\t\t\t(0341) 491743, 496786\t(0341) 491983\t\tSuluh Djatmiko\n\tMalang\t\t\tToyota\t\tPT. Astra International Tbk\t\tJL Let Sutoyo NO 25\t\t\t\t(0341) 472000\t\t(0341) 491003\t\tBERNADHI\n\tMalang\t\t\tSuzuki\t\tPT Sun star motor\t\t\tJL A.Yani no 102\t\t\t\t\t(0341) 456592-3\t\t(0341) 456596\t\n\t\t\t\t\t\t\t\n33\tManado\t\t\tDaihatsu\tPT. Astra International Tbk \t\tJL. RE. Martadinata No. 69\t\t\t\t(0431) 866602\t\t(0431) 851278\t\tPAK ARTHER HP 0431-3398708\n\tManado\t\t\tToyota\t\tCV Kombos\t\t\tJL. Raya Kombos Manado 95233\t\t\t(0431) 813507\t\t(0431) 813537\t\tDiky\n\tManado\t\t\tMitsubishi\tBosowa Berlian Motor\t\tJL. Kairagi No. 36\t\t\t\t\t(0431) 863856\t\t(0431) 862468\t\tRidwan\n\t\t\t\t\t\t\t\n34\tMedan\t\t\tSuzuki\t\tPT. Trans Sumatra Agung\t\tJL. H. Adam Malik D/H Glugur By Pass Medan\t\t(061) 6618006\t\t(061) 6618777\t\tZoson,  \n\tMedan\t\t\tMitsubishi\tPT Sumatra Berlian Motors\t\tJL Tanjung morawa km 7 no 34\t\t\t(061) 7866868\t\t(061) 7867989\t\tPak Didin HP 0812-6507074 / Rina admin\n\tMedan\t\t\tBAN\t\tUD. Banindo Perkasa\t\tJL. Glugur By Pass 100 d/h 90 Medan\t\t\t(061) 6618017\t\t(061) 6632008\t\tKasda / Ali\n\tMedan\t\t\tToyota\t\tAuto 2000 Medan\t\t\tJL. Sisingamangaraja No. 8 Medan\t\t\t(061) 7363388 \t\t(061) 7362300\t\tRobertus Homadi\n\tMedan\t\t\tUmum\t\tCV Asta Padu\t\t\tJL selar No 3 Koala tanjung (aluminium smelting)\t(0621) 21413\t\t(0621) 326474\t\tAyung HP 0811-621948\n\tMedan\t\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. Sisingamangaraja No. 170\t\t\t(061) 7349000\t\t(061) 7361133\t\tSutarsono\n\tMedan\t\t\tDaihatsu\tCapella Medan Kisaran Asahan\tJL Imam Bonjol No 303  Kisaran dekat Asahan\t\t(0623) 44236\t\t(0623) 44236\t\tSudarsono\n\tMedan \t\t\tDaihatsu\tCapella Medan \t\t\tJL. Sisingamangaraja KM 6,5 Tanjung Morawa\t\t(061) 7863214\t\t(061) 7863207\t\tT. Sitanggang\n\t\t\t\t\t\t\t\n35\tPadang\t\t\tDaihatsu\tPT. Capella Medan\t\tJL. Prof. Dr. Hamka No. 123 Padang Utara\t\t(0751) 7051777, 7059894 \t(0751) 7057644\t\tYusman ( 0751-7861114\n\tPadang\t\t\tIsuzu\t\tPt.isuindomas\t\t\tJl. S. Parman 182, Padang\t\t\t\t(0751) 51637, 50783, 59124\t(0751) 52130\t\n\t\t\t\t\t\t\t\n36\tPalembang\t\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Raya Jend. A. Yani No. 17 Rt 20/13 Ulu\t\t ( 0711 ) 510583, 510585\t ( 0711 ) 510586\t\tBpk. H. Danu Darmoko\n\tPalembang\t\tSuzuki\t\tPT. Thamrin Bersaudara\t\tJL.Mayor Santoso No. 31 - 38 Palembang\t\t ( 0711 ) 311311, 364072\t ( 0711 ) 311112\t\tBpk. Hendrik Suganda,SA HERY, SOLEH\n\tPalembang\t\tDaihatsu\tPT. Astra International Tbk\t \tJL. Jend. A. Yani 100 - 104 Ulu\t\t\t(0711) 511889, 511812\t(0711) 352621\t\tA. Supardi\n\tPalembang\t\tBAN\t\tSumber Ban\t\t\tJL. Jend. Sudirman KM 3.5 No. 22 AB\t\t\t(0711) 356272\t\t(0711) 314662\t\tAnastasia Makmur\n\tPalembang \t\tToyota\t\tPT. Astra International Tbk - Toyota\tJL. Jend. A. Yani No. 5502 Palembang 30126\t\t ( 0711 ) 512000, 517979\t ( 0711 ) 512943\t\tPak Amin ADM SERVICE\n\tPalembang Prabumulih\tSuzuki\t\tPT. Thamrin Bersaudara Sekarang\tJL. Jend. Soedirman KM 6 Prabumulih\t\t\t ( 0713 ) 321071, 321075\t0713-321779\t\tBpk. Budiono\n\t\t\t\t\t\t\t\n37\tPalu\t\t\tMitsubishi\tBosowa Berlian Motor\t\tJL. RA. Kartini No. 51\t\t\t\t(0451) 422026\t\t(0451) 421196\t\tAbdurahim (0811459337)\n\t\t\t\t\t\t\t\n38\tPangkal pinang\t\tMitsubishi\tPT lautan berlian utama motor\tJl M.S Rachman no 59 A Pangkal Pinang 33144\t\t(0717) 431666 / 422333\t(0717) 431666\t\tEDO\n\t\t\t\t\t\t\t\n39\tPekalongan\t\tDaihatsu\tPT. Astra International Tbk \t\tJL. Merdeka 19\t\t\t\t\t(0285) 412345\t\t(0285) 412828\t\tAli Arifin\n\t\t\t\t\t\t\t\n40\tPekanbaru\t\tDaihatsu\tPT. Capella Medan\t\tJL. Arengka No. 53\t\t\t\t\t(0761) 571900\t\t(0761) 572411\t\tAdi SUCIPTO ( HP : 08127529826 ) SA P'GINTING\n\tPekanbaru\t\tMitsubishi\tPT. Suka Fajar\t\t\tJL. Arengka No. 352-354 No. 140\t\t\t ( 0761 ) 572544\t\t ( 0761 ) 572816\t\tBpk. Taufik\n\tPekanbaru\t\tMitsubishi\tPT Pekan Perkasa Berlian Motors\tJL. Jend. Sudirman No. 230\t\t\t\t(0761) 848808,76129027\t(0761) 848438\t\tBpk. H. GINO HP 0761-3060316\n\tPekanbaru\t\tAC \t\tSekawan Servis\t\t\t. Riau Ujung No. 328\t\t\t\t(0761) 861668/853684\t(0761) 861663 / 858818\tPingping\n\tPekanbaru\t\tToyota\t\tPT. Agung Automall\t\tJL. Dr. Sutomo No. 13, Pakanbaru 28143\t\t(0761) 22252\t\t( 0761 ) 32352\t\tBpk. Made W / Bpk. Gunadi EXT 105 P'HENDRA SA\n\tPekanbaru\t\tAC \t\tCitra Sejuk Klimatindo\t\tJl. Soekarno Hatta no 2 Arengka\t\t\t(0761) 7051340\t\tBpk Kainul 08127534340\n\tPekanbaru\t\tIsuzu\t\tPt. Insuindomas Putra\t\tJl. Tambusai no 347-349\t\t\t\t0761 – 29145/46\t\t(0761) 29147\t\tBpk Yulius\n\t\t\t\t\t\t\t\n41\tPematang Siantar\t\tDaihatsu\tPT Capella Medan\t\t\tJL. Medan Km 6 Simpang Karang Sari \t\t\t(0622) 23711\t\t(0622) 7436522\t\tPak Salam Bangun HP 0812-64301354\n\t\t\t\t\t\t\t\n42\tPontianak\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. Imam Bonjol 529\t\t\t\t(0561) 748260, 748262\t(0561) 732768\t\tM. Ismail\n\tPontianak\t\tMitsubishi\tGemilang berlian Indah\t\tJl arteri Supadio km 1.5\t\t\t\t(0561) 589888\t\t(0561) 582999\t\tIBU ERA\n\tPontianak\t\tIsuzu\t\tBorneo auto cemerlang\t\tJl . Pahlawan no 2 & 3 \t\t\t\t(0561) 735757\t\t(0561) 734308\t\tPak Arif\n\tPontianak\t\tBAN\t\tBan Pontianak\t\t\tJl. Imam Bonjol No 8 A\t\t\t\t(0561) 761607/608\t\t\t\t\tKo Atman\n\t\t\t\t\t\t\t\n43\tProbolinggo JATIM\t\tSuzuki\t\tSurya Raya Motor\t\t\tJl Soekarno Hatta  170 \t\t\t\t20420-23108\t\t\t\t\tLIENA  SURYAWATI\n\t\t\t\t\t\t\t\n44\tPurwokerto\t\tMitsubishi\tSinar Berlian Auto Graha\t\tJL. Gerilya Timur 103\t\t\t\t(0281) 635333\t\t(0281) 636777\t\tJumeno\n\tPurwokerto\t\tDaihatsu\tTorana Motor\t\t\tJL. Jend. Sudirman No. 61\t\t\t\t(0281) 633934\t\t\t\t\tYudi ( HP : 08156971260 )\n\t\t\t\t\t\t\t\n45\tSamarinda\t\tDaihatsu\tPT. Astra International Tbk \t\tJL. Ir. H. Juanda No. 55\t\t\t\t(0541) 748595\t\t(0541) 748594\t\tArthur Kindangen\n\t\t\t\t\t\t\t\n46\tSampit\t\t\tMitsubishi\tMurni Berlian Motor\t\t\t\t\t\t\t\t(0531) 30876\t\t(0531) 30867\t\tIBU MILA\n\tSampit\t\t\tSemua\t\tDwi Jaya Motor\t\t\tJL. cilik riwut KM 2 kalteng\t\t\t\t(0531) 32000\t\t(0531) 31700\t\tHelen / yayu/  Agus subagyo bos HP 0811-524001\n\t\t\t\t\t\t\t\t\n47\tSemarang\t\tMitsubishi\tSidodadi Berlian Motor\t\tJL. Siliwangi No. 287 A Kalibanteng\t\t\t(024) 7603957\t\t(024) 7604206\t\tHeru Rubianto\n\tSemarang\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. Majapahit No. 111-117\t\t\t\t(024) 6717111, 6718111\t(024) 6723650\t\tA. Bayu Syafe'I\n\tSemarang\t\tSuzuki\t\tPT. Sunmotor Indosentra Strada\tJL. Pemuda No. 65 Semarang\t\t\t(024) 3565000\t\t(024) 3584677\t\tDita Mahar\n\tSemarang\t\tBAN\t\tFajar Baru\t\t\tJL. Barito Raya 58 ( Sebelah Ciliwung II )\t\t(024) 3543156\t\t(024) 3543156 PENCET 101\tWahyu / RITA\n\t\t\t\t\t\t\t\n48\tSidoarjo\t\t\tMitsubishi\tBumen Redja Abadi\t\tJL. Raya Larangan  No. 2 Candi\t\t\t(031) 8950467\t\t(031) 8921401\t\tHeri Winarko IBU UMI SERVICE\n\tSidoarjo\t\t\tUmum\t\tMEGAH ASRI MOTOR\t\tJL Wadug Asri 82-84 Waru Sidoarjo\t\t\t(031) 8672866\t\t(031) 8672844\t\tIBU LENI\n\t\t\t\t\t\t\t\n49\tSolo\t\t\tDaihatsu\tPT. \tAstra International Tbk \t\tJL. Raya Solo Permai - Solo baru\t\t\t(0271) 620973, 620977\t(0271) 620963\t\tM. Surofi\n\tSolo \t\t\tMitsubishi\tPT Satrio Widodo\t\t\tJL. Adisucipto km 7.3\t\t\t\t0274-486706/488601\t0274-489214\t\tPak Sugi\n\t\t\t\t\t\t\t\n50\tSukabumi\t\t\tToyota\t\tSelamet  lestari mandiri\t\tJL Arif rahman hakim no 43\t\t\t\t(0266) 221800 /224976\t(0266) 224976/ 222354\tBudi harso\n\tSukabumi\t\t\tMitsubishi\tPT. Merdeka Motors\t\tJL. KH. A. Sanusi No. 33\t\t\t\t(0266) 222702,225023\t(0266) 225044\t\tRudy ( 08569994398 )\n\t\t\t\t\t\t\t\n51\tSumedang\t\t\t\tCV Sumber Rejeki\t\t\tJL. Mayjen Abdul Rachman no 128-130\t\t(0266) 201527/201953\t(0266) 201720\t\tPak Apandi\n\t\t\t\t\t\t\t\n52\tSurabaya\t\t\tBAN\t\tParamitha Ban\t\t\tJL. Jemur Sari Raya No. 150 Surabaya\t\t\t(031) 8432112\t\t(031) 8497087 /8438701\tDede (DEWI/EKA PIC)\n\tSurabaya\t\t\tToyota\t\tPT. Astra International Tbk\t\tJL. Basuki Rahmat 115-117\t\t\t\t(031) 5452000\t\t(031) 5342060\t\tHartono Kurniawan SA Pak Rofikun / WAHYUWIDIATMOKO\n\tSurabaya\t\t\tMitsubishi\tMurni Berlian Motor\t\tJL. Demak 172 Surabaya\t\t\t\t(031) 5323736 / 5353531\t(031) 5314386\t\tSudarsono\n\tSurabaya\t\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. HR. Muhammad No. 73 Bengkel Sungkono\t\t(031) 7312000\t\t(031) 7314000\t\tAzis ( SA )\n\tSurabaya\t\t\tMitsubishi\tPT Sun star motor\t\t\tJL. Ngagel  no 81-85\t\t\t\t(031) 5015690/1\t\t(031) 5018272\t\tPak Yohan\n\tSurabaya\t\t\tAC \t\tPanca Jaya Ac\t\t\tJl Kapas krampung No 1991\t\t\t\t(031) 5037265 \t\t(031) 5039131\t\tP' Ameng HP 0812-1785198 / 7341156\n\tSurabaya\t\t\tSuzuki\t\tUnited Motor Centre\t\tJL A.Yani no 40 \t\t\t\t\t(031) 8280612\t\t\t\t\tP'BAMBANG SUGIONO\n\tSurabaya\t\t\tIsuzu\t\tAstra Isuzu\t\t\tJL Kombes M Duryat  17\t\t\t\t(031) 5470808\t\t(031) 5342269\t \n\tSurabaya\t\tHonda\t\tHonda Saver\t\t\tJl Genteng Besar 106-110 Surabaya\t\t\t(031) 5460975 – 5325525\t(031) 5346894\t\n\tSurabaya Hr Muhammad\tDaihatsu\tPT. Astra International Tbk\t\tJL. HR Muhammad No. 4 & 6\t\t\t\t(031) 7345700\t\t\t\t\tAndreas Benjamin\n\tSurabaya Waru\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. Sawo Tratap Km 15 Waru - Sidoarjo\t\t(031) 8533777, 8535526\t(031) 8533778\t\tAidil FB Swastomo, SA PAK SUWOTO\n\t\t\t\t\t\t\t\n53\tTangerang\t\tHino\t\tPT Hino Motor Sales Indonesia\tJL Raya gatot subroto KM 8.5 Tgr\t\t\t(021) 5918844\t\t(021) 5917887\t\tPak Deden \n\t\t\t\t\t\t\t\n54\tTasikmalaya\t\tSuzuki\t\tCakra Putra Parahyangan\t\tJL. Dr. Moh. Hatta No. 158\t\t\t\t(0265) 337470\t\t(0265) 337471\t\tJono (081802281163) ADM SA IBU AIRIKA\n\t\t\t\t\t\t\t\n55\tTegal\t\t\tDaihatsu\tPT. Astra International Tbk\t\tJL. Kol. Sugiono No. 104\t\t\t\t(0283) 359676, 359677\t(0283) 324954-359678\tAgustinus Karyadi\n\tTegal \t\t\tMitsubishi\tPT Matahari Berlian Motor\t\tJL. Kapten sudibyo no 125\t\t\t\t(0283) 352525/359595\t(0283) 350505\t\n\t\t\t\t\t\t\t\n56\tYogyakarta\t\tBAN\t\tSetiawan spooring\t\tJL Laksda Adisucipto KM 7\t\t\t\t(0274) 867449\t\t(0274) 867449\t\tIBU TUTI/ IBU Galuh adm, PAK Wahid \n\tYogyakarta\t\tDaihatsu\tPT. Astra International Tbk \t\tJL. Magelang KM 7,2\t\t\t\t(0274) 868074, 868075\t(0274) 868650\t\tSuhardiman\n\tYogyakarta\t\tMitsubishi\tUD. Borobudur Motors\t\tJL. Laksda Adi Sucipto Km 73\t\t\t(0274) 486706/488601\t(0274) 512214/ 487169/ 488573 OK\tSugih Utomo IBU SUMIRAH\n\tYogyakarta\t\tToyota\t\tNasmoco toyota\t\t\tJL Raya Magelang km 7\t\t\t\t(0274) 868808\t\t(0274) 868992\t\tP'BANDUNG 0856-643163369\n");
         jTextArea5.setBorder(null);
         jTextArea5.setOpaque(false);
         jScrollPane23.setViewportView(jTextArea5);
 
         jPanel7.add(jScrollPane23);
         jScrollPane23.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("Bengkel Rekanan", jPanel7);
 
         jPanel23.setLayout(null);
 
         jTextArea11.setColumns(20);
         jTextArea11.setEditable(false);
         jTextArea11.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea11.setRows(5);
         jTextArea11.setTabSize(5);
         jTextArea11.setText("\tDATA BENGKEL REKANAN\t\t\t\t\t\t\n\t\t\t\t\t\t\t\n\tKota\t\t\tJenis\t\tNama\t\t\t\tAlamat\t\t\t\t\t\tTelp\t\t\tFax\t\t\tPIC\n1\tAceh\t\t\tUmum\t\tPutra jaya \t\t\tJL.Imuem leung bata km 2,5 Panterik\t\t\t(0651) 22694\t\t(0651) 29873\t\tPak Hendrik\n\t\t\t\t\t\t\t\n2\tBali\t\t\tMitsubishi\tBumen Redja Abadi\t\tJL. Imam Bonjol 375 R\t\t\t\t(0361) 483002\t\t(0361) 484133\t\tPrasetyo Theriady\n\tBali Gianyar\t\tDaihatsu\t\tPT. Astra International Tbk\t\tBr. Blah Tanah Ds Batuan Sukawati\t\t\t(0361) 974464, 974693\t(0361) 974463\t\tWahyudi\n\tBali, Denpasar\t\tToyota\t\tPT. Astra International Tbk\t\tJL. Hos. Cokroaminoto  No. 81, Ubung, Denpasar\t\t(0361) 422000\t\t(0361) 410311\t\tKetut Diyana (08883610555) PAK WAYAN DARMAWAN ADM SERVICE\n\t\t\t\t\t\t\t\n3\tBalikpapan\t\tMitsubishi\tMandau Berlian Sejati \t\tJL. MT Haryono No. 36 KM 5 Ring Road\t\t\t(0542) 875479\t\t(0542) 875464\t\tYB Yaniko, P'HERY ACC\n\tBalikpapan\t\tToyota\t\tAuto 2000\t\t\tJL. Jend Sudirman No. 29 Balikpapan\t\t\t(0542) 732000\t\t(0542) 734011\t\tFUKUH KA BENG,Taufik\n\tBalikpapan\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Jend. Sudirman No. 50\t\t\t\t(0542) 419000-760000,763819(0542) 442044\t\tJAMAL / Tatang Setiawan\n\tBalikpapan\t\tALL CAR\t\tFamily Serviceindo\t\t\t\t\t\t\t\t\tNur 087878831504\tJoko 0819-5580844\n\tBalikpapan\t\tBAN\t\tPt. Linda Hauta Wijaya\t\tJl. Jendral sudirman no 263 \t\t\t\t(0542) 427966/7\t\tIbu NANA\n\t\t\t\t\t\t\t\n4\tBandung\t\t\tMitsubishi\tPT Surya Putra Sarana\t\tJL. Jend Sudirman no 776-778\t\t\t\t(022) 6033720/2\t\t(022) 6030563\t\tPak Iwan suryadi\n\tBandung\t\t\tSuzuki\t\tPT. Nusantara Jaya Sentosa\t\tJL. Soekarno Hatta No. 289 Bandung 40234\t\t(022) 5204645\t\t(022) 5203942\t\tAri Rakhmadi\n\tBandung\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Asia Afrika No. 127\t\t\t\t(022) 4238977\t\t(022) 4214440\t\tTRISANTOSO\n\tBandung\t\t\tBAN\t\tPD Lautan Ban\t\t\tJL. Astana Anyar 81 Bandung 40241\t\t\t022) 4203443\t\t(022) 4209657\t\tIBU SUSI\n\tBandung\t\t\tMitsubishi\tPT Surya Putra Sarana\t\tJL. Abdulrahman saleh no 4\t\t\t\t022-6031040\t\t(022) 6030636\t\tPak Iwan suryadi\n\tBandung\t\t\tToyota\t\tPT. Astra International Tbk\t\tJL. Soekarno Hatta No. 145 Bandung\t\t\t(022) 6030450\t\t(022) 6031559\t\tAgus Ryanto\n\tBandung\t\t\tMitsubishi\tSrikandi Daimond Motor\t\tJL. Soekarno Hatta No. 342\t\t\t\t(022) 5407000\t\t(022) 5406776\t\tUnang Sunarya MNGR, SA YUYUNG,DENI\n\tBandung\t\t\tHonda\t\tIstana bandung raya Motor\t\tJl. Cicende No 18\t\t\t\t\t(022) 4240888\t\t(022) 4233629\t\tP'ANDRI SUDJANI Svce Mngr HP 0812-2157215\n\tBandung\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Raya Cibeureum 42\t\t\t\t(022) 6031568, 6031058\t(022) 6035615\t\tJunjung Siregar IBU MIKE 022-6031058\n\tBandung\t\t\tNissan\t\tINDOMOBIL TRADA NISSAN\t\tJL. Soekarno Hatta NO 382\t\t\t\t(022) 5207777\t\t(022) 5207181\t\tPETRUS KURNIA\n\tBandung\t\t\tAUTO 2000 Pasteur\tAUTO 2000 \t\tJl. Dr. Djundjunan No. 192 Bandung 40163 \t\t(022) 2000100 \t\t\t\t\tMunif Latief KABENG\n\tBandung\t\t\tAUTO 2000 Soekarno Hatta\tAUTO 2000 \t\tJl. Soekarno - Hatta No.145 Bandung 40223\t\t(022) 6022000 \t\t\t\t\tAgus Riyanto Kabeng\n\t\t\t\t\t\t\t\n5\tBanjarmasin\t\tDaihatsu\t\tNusantara Indah \t\t\tJL. A. yani km 4,5\t\t\t\t\t(0511) 3262791\t\t(0511) 3272900\t\tPak Arbani\n\tBanjarmasin\t\tMitsubishi\tPT sumber berlian motors\t\tJl. km 10.3 no 1 rt 5 kertak hanya banjar kalsel\t\t(0511) 4281699\t\t(0511) 4281664/ 65\t\tIr. H. Ajie MANAGER ,JAMAL KABENG\n\tBanjarmasin\t\tToyota\t\tWira Megah Profitamas\t\tJL. Jend. A. Yani KM 10 Kertak Hanyar\t\t\t(0511) 3272000\t\t(0511) 3263000\t\tHasbullah PAK HARIS SA\n\tBanjarmasin\t\tMitsubishi\tSumber Berlian Motor\t\tJL. Jend. A. Yani KM 5.5 No. 51\t\t\t(0511) 3252216\t\t(0511) 3255657\t\tIr. H. Ajie\n\tBanjarmasin\t\tIsuzu\t\tAstra International  ISUZU\t\tKALIMANTAN\t\t\t\t\t(0511)3265460, 3267000, 3273621 \t(0511)3267465 \t\n\t\t\t\t\t\t\t\n6\tBanyuwangi\t\tToyota\t\tAUTO 2000 Cab Pembantu Jember\tJL A.Yani no 7\t\t\t\t\t(0333) 422000\t\t(0333) 424953\t\tSA Pak Samuji\n\tBanyuwangi\t\tMitsubishi\tPT. Mayangsari Berlian Motor\tJL. Yos Sudarso, No. 69 A Banyuwang\ti\t\t(0333) 424417,424418\t(0333) 424535\t\tNoerhidayat (081559707300)\n\t\t\t\t\t\t\t\n7\tBatam \t\t\tToyota\t\tPt. Agung Automall\t\t\tJl. Yos Sudarso\t\t\t\t\t(0778)  427585\t\t(0778) 427589\t\tKabeng Bpk Eddy Rahmansyah\n\t\t\t\t\t\t\t\n8\tBaturaja\t\t\tSuzuki\t\tPT. Thamrin Bersaudara\t\tJL. Jend. A. Yani No. 99 Baturaja 31112\t\t\t( 0735 ) 321026\t\t\t\t\tBpk. Laily Ramadhan/ Iwan\n\tBaturaja\t\t\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Jend. A. Yani No. 319 Baturaja\t\t\t( 0735 ) 322747, 321333\t ( 0735 ) 320510\t\tBpk. Tarmizi/Bpk. Suparman\n\t\t\t\t\t\t\t\n9\tBengkulu\t\t\tMitsubishi\tLautan berlian utama motor\t\tJL. S. Parman no 61 padang jati Bengkulu\t\t(0736) 20062, 21934\t(0736)  21134\t\n\tBengkulu\t\t\tSuzuki\t\tSuzuki Service\t\t\tJl M.Jend Sutoyo No 88 \t\t\t\t(0736) 21010\t\t(0736) 26446\t\tIBU MEI\n\t\t\t\t\t\t\t\n10\tBogor\t\t\tSuzuki\t\tPT. RMK - Bogor \t\t\tJL. Raya Tajur No. 91, Bogor\t\t\t\t(0251) 8391273,8352282\t(0251) 8352281\t\tPAK AWANG / / HARI SA/  Handy Tjahjadinata\n\tBogor\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Pajajaran No. 22\t\t\t\t(0251) 325737\t\t(0251) 326633\t\tSapto Pamungkas\n\tBogor\t\t\tMitsubishi\tPT. Prabu pandawa motor\t\tJL.Pajajaran no 20 \t\t\t\t\t(0251) 331201-2\t\t(0251) 8312874\t\tPak Imat suyatman\n\t\t\t\t\t\t\t\n11\tCENTURY \t\tMBAK TATI\t\t\t\t\t\t\t\t\t\t\t(021) 5302423\t\t\n\t\t\t\t\t\t\t\n12\tCibinong\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Raya Bogor - Jakarta KM 43 Pabuaran\t\t(021) 8757870\t\t\t\t\tKhaerullah\n\t\t\t\t\t\t\t\n13\tCikarang\t\t\t\t\tOTO ZONE\t\t\tJL. MH. Thamrin Kav. 133 C Lippo Cikarang\t\t(021) 8990 6611\t\t(021) 8990 6622\t\tHalim\n\tCikarang\t\t\tToyota\t\tAuto 2000\t\t\tJl. M. H. Thamrin Kav. 168, Lippo Cikarang\t\t(021) 8990 2000\t\t(021) 8990 3758/57\t\tPak Daniel , Pak PARYOTO, PAK ODRADI\n\tCikarang\t\t\tHonda\t\tPT. Prospect Motor HONDA\t\tJL. MH. Thamrin No. 152 Lippo Cikarang\t\t(021) 8974142/43\t\t(021) 8974144\t\tArif Harsono\n\t\t\t\t\t\t\t\t\n14\tCilegon\t\t\tToyota\t\tTunas Ridean Tbk\t\t\tJL. Raya Cilegon KM 14\t\t\t\t(0254) 394777/394789\t(0254) 391580\t\tYasri Abdullah\n\tCilegon\t\t\tSuzuki\t\tPT. RMK - Cilegon\t\t\tJL. Raya Serang KM 1, Cibeber, Cilegon\t\t\t(0254) 381500\t\t(0254) 380563\t\tLukito (02549195455)\n\tCilegon\t\t\tMitsubishi\tSetia Kawan menara motor\t\tJl. Raya Cilegon No. 101, Cilegon \t\t\t(0254) 391-267 ; 391-773 \t(0254) 391-171 \t\trimbun\n\t\t\t\t\t\t\t\n15\tCimone\t\t\tSuzuki\t\tPT. RMK - Cimone\t\t\tJL. Merdeka  No. 1, Cimone Tangerang\t\t\t(021) 5517876\t\t(021) 5523968\t\tApendi\n\t\t\t\t\t\t\t\n16\tCirebon\t\t\tSuzuki\t\tCinta Damai Putra Bahagia\t\tJl. Kalijaga No 117 \t\t\t\t\t(0231) 230506 / 230477 \t(0231) 210060\t\n\tCirebon\t\t\tToyota\t\tAuto 2000\t\t\tJL. Brigjen Darsono 14 by pass\t\t\t(0231) 232000\t\t(0231) 202009\t\tPak Winawan SA PAK ALIF , PAK TRI ADM, MULYADI SA\n\tCirebon\t\t\t\t\tPENYET VARIASI ALARM\t\t\t\t\t\t\t\t(0231) 230308\t\t\t\t\tKO ATIK\n\tCirebon\t\t\t\t\tRaharja Putra Ban\t\t\tJL. kesambi No 50\t\t\t\t\t(0231) 231129\t\t(0231) 231129\t\t\t\t\tIsye Slamet, SANTI\n\tCirebon\t\t\tDaihatsu\t\tPT.Astra International Tbk \t\tJL. Tuparev No. 76\t\t\t\t\t(0231) 206691\t\t(0231) 205557\t\t\t\t\tAris Dwinanto\n\tCirebon\t\t\t\t\tPt Bintang Timur\t\t\tJL. Buyut no 4 \t\t\t\t\t(0231) 201040\t\t(0231) 231040\t\tPak Tomy Hadi\n\t\t\t\t\t\t\t\n17\tDenpasar Cokro\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Cokroaminoto 52\t\t\t\t(0361) 429000\t\t(0361) 410928\t\tRatno Yunanto\n\tDenpasar Sanur\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Ngurah Rai 17 X By Pass Sanur\t\t\t(0361) 288323, 288345\t(0361) 288002\t\tWahyudi\n\t\t\t\t\t\t\t\n18\tJakarta\t\t\tHonda\t\tPt Honda autoland\t\t\tjalan raya boulevard barat blok xb 1-2 kelapa gading \t(021) 4501858 / 4501868\t(021) 4529202\t\tibu ERI service\n\tJakarta\t\t\tHonda\t\tPT. IKM\tGedung IKM,\t\t JL. Daan Mogot No. 6\t\t\t\t(021) 5644888\t\t(021) 56962357\t\tRudy Widjaja SA PAK WENI\n\tJakarta\t\t\t\t\tToyota astra motor\t\t\tSUDIRMAN\t\t\t\t\t(021) 2511701-02-07 BOOKING\t(021) 2511700\tDARMA BAYU , SA PAK KUSNAN, JAKA\n\tJakarta\t\t\tHino\t\tPT Hino Motor Sales Indonesia\tJl MT haryono kav 8\t\t\t\t(021) 8564570 / 8564480\t(021) 8515731 / 8517550\tP'DEDEN FAX 5917887\n\tJakarta\t\t\tSuzuki\t\tPT. RMK - Kebon Jeruk\t\tJL. Raya Panjang No. 12  Kebon Jeruk Jakarta Barat\t(021) 5492727\t\t(021) 5493031\t\tAntonius Sumarsono\n\tJakarta\t\t\tHonda\t\tPT. Istana Kebon Jeruk\t\tJL. Panjang No. 200 Jakarta Barat\t\t\t(021) 5492580-81\t\t(021) 5493464\t\tOdik Suhendra (08129928257)\n\tJakarta Barat\t\tToyota\t\tAuto 2000 Daan Mogot\t\tJL. Daan Mogot No.146-147 Jakarta Barat 11510\t\t(021) 5642000\t\t(021) 5688719\t\tDedi Suhendi\n\tJakarta Barat\t\tSemua\t\tPT. Family Servisindo\t\tJL. Arteri Kedoya No.16 Kedoya \t\t\t(021) 5680077\t\t(021) 56007126\t\tWidionarko\n\tJakarta Cibitung\t\tSemua\t\tPT. Family Servisindo\t\tJL. Sumatra D 4 Kawasan Industri MM2100\t\t(021) 89982288\t\t(021) 89982233\t\tRofiq\n\tJakarta klender\t\tAstrido\t\tAstrido Klender\t\t\tKlender\t\t\t\t\t\t021-8610322\t\t021-8610320\t\tP'Tri\n\tJakarta Pusat\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. P. Jayakarta No. 28\t\t\t\t(021) 6590606, 6590600\t(021) 6590610\t\tAgus S.M\n\tJakarta Pusat\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Balikpapan No. 8\t\t\t\t(021) 63864386-87\t\t(021) 63862223\t\tShandy\n\tJakarta Selatan\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Raya Ciputat No. 22\t\t\t\t(021) 7651638, 7651642-44\t(021) 7651639\t\tDjoko Untung\n\tJakarta Selatan\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Sultan Iskandar Muda No. 15\t\t\t(021) 7291011\t\t(021) 7291008\t\tDolf Valentino\n\tJakarta Serpong\t\tDaihatsu\t\tPT. Astra International Tbk - Daihatsu\tBSD City Blok 405/2A Sektor 8 Tangerang\t\t(021) 5380011\t\t(021) 5383888\t\tDARJAT / RAMLI\n\tJakarta Utara\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Raya Pluit Selatan No. 4\t\t\t\t(021) 6690755, 6612341\t(021) 6695092\t\tTeguh Santoso\n\tJakarta Utara\t\tNissan\t\tNissan Diesel, Sunter\t\tJL. Danau Sunter Selatan Blok O 2 No. 5\t\t(021) 6507150\t\t6507161\t\t\tNyoman Puja\n\tJakarta Utara\t\tSuzuki\t\tPT. Sumberbaru sentral mobile \tJl. Inspeksi Kalimalang Kav. Billy & Moon RT 03/01\t(021) 869 03019 \t\t(021) 869 03021 \t\tIbu Anna\n\tJakarta Utara\t\tToyota\t\tPT. Toyota Astra Motor\t\tJL. Laks. Yos Sudarso Sunter II Jakarta 14330\t\t(021) 6515551\t\t(021) 6512238\t\tBudi riyanto FAX 65307639\n\tJakarta Utara\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Yos Sudarso Kav. 24, Sunter II\t\t\t(021) 6511002, 6511720\t(021) 6511043\t\tSudjarwo Priyono\n\tJakarta Utara\t\tIsuzu\t\tPT. Astra International Isuzu\t\tJL. Yos Sudarso No. 30 Sunter\t\t\t\t(021) 65834684\t\t(021)65304434  \t\tArif  \n\t\t\t\t\t\t\t\n19\tJambi\t\t\tSuzuki\t\tCV Jaya Indah Motor\t\tJl. KH Dewantara 66/68 simpang kawat jambi\t\t(0741) 63362 \t\t(0741) 60675 \t\tZul fakar\n\tJambi\t\t\tMitsubishi\tPT Kerinci Permata Motors\t\tJl Abunjani no 10 Simpang 3 sipin Jambi\t\t\t(0741) 60300 /  616 66\t(0741) 60263\t\tP'ERWIN KA BENG, NURAINI/IKE ADM LIA SERVICE\n\tJambi\t\t\tDaihatsu\t\tSurya sentosa primatama\t\tJl kol Abunjani no 9\t\t\t\t\t(0741) 670055-56\t\t(0741) 61573\t\tIMELDA,  \n\t\t\t\t\t\t\t\t\n20\tJember\t\t\tToyota\t\tAUTO 2000 \t\t\tJl Hayam Wuruk No 54 Jember\t\t\t(0331) 422000\t\t\t\t\tKa Beng Pak Lutfi HP 0815-59807529\n\tJember\t\t\tMitsubishi\tPT Mayang Sari Berlian Motors\tJL Gajahmada no 224 A\t\t\t\t(0331) 484366/484367\t(0331) 484633\t\tIbu Dyah\n\tJember\t\t\tDaihatsu\t\tBesuki Raya Motor\t\t\tJL. Hayam Wuruk No. 40\t\t\t\t(0331) 421891\t\t(0331) 482670\t\tArief Subagyo\n\t\t\t\t\t\t\t\n21\tJepara\t\t\tSuzuki\t\t\t\t\t\t\t\t\t\t\t\t0291-3367878 \t\t\t\t\tPAK SUGENG BRAND MANAGER\n\t\t\t\t\t\t\t\n22\tKarawang\t\tSuzuki\t\tPT. RMK - Karawang\t\tJL. Jend. A. Yani No. 36, Karawang\t\t\t(0267) 403085\t\t(0267) 403083\t\tTantri (08128741667)\n\tKarawang\t\tToyota\t\tAUTO 2000 \t\t\t\t\t\t\t\t\t(0267) 412000 \t\t(0267) 402570 \t\tPAK BOBY 0816-787918 P'DASEP\n\tKarawang\t\tASTRA\t\tdaihatsu dan isuzu jg ac\t\tjl. kerta bumi no 38 \t\t\t\t(0267) 402539\t\t(0267) 402006\t\tbpk syahrul, kabeng ato tojiri\n\t\t\t\t\t\t\t\n23\tKbn Jeruk\t\t\tNissan\t\tINDOMOBIL TRADA NISSAN\t\tARTERI KELAPA 2 NO 30\t\t\t\t(021) 5362477\t\t(021)  5324756\t\tP'ALEX, IBU FANIE\n\t\t\t\t\t\t\t\n24\tKediri\t\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Soekarno Hatta 152\t\t\t\t(0354) 688501-2, 684412\t(0354) 684576\t\tA. Chuzaeni\n\tKediri\t\t\tSuzuki\t\tgaruda motor\t\t\tJl. Brawijaya No. 34\t\t\t\t(0354) 681562\t\t(0354) 661371\t\t 081556539655\n25\tKendari SULTENG\t\tSemua\t\tSetia Kawan Motor Suzuki\t\tJL R.Suprapto No 58 kendari\t\t\t\t(0401) 326320\t\t(0401) 3126402\t\tBELUM REKANAN\n\t\t\t\t\t\t\t\n26\tKetapang\t\t\tMitsubishi\tPd Sumber baru motor\t\tJL.brigjen Katamso KM 2.5 sukaharja \t\t\t(0534) 31200\t\t\t\t\tIBU Kapui\n\t\t\t\t\t\t\t\n27\tKudus - Jateng\t\tToyota\t\tCv Surya Indah Motor\t\tJL.Raya Kudus pati KM 4\t\t\t\t(0291) 433215\t\t(0291) 432091\t\tPak Hartono\n\t\t\t\t\t\t\t\n28\tLampung\t\t\tSuzuki\t\tPT. Persada Lampung Raya\t\tJL. Zainal Abidin Pagar Alam No. 2 Lampung\t\t(0721) 703325, 703329\t(0721) 704953\t\tBpk. Suyono\n\tLampung\t\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Jend. A. Yani No. 1 Tanjung Karang\t\t\t(0721) 269000\t\t(0721) 241283\t\tKahono\n\tLampung\t\t\tBAN\t\tAnugrah Ban \t\t\tJL. Laksamana Hayati No. 8 Telukbetung\t\t(0721) 486458\t\t(0721) 482386\t\tSansan\n\tLampung bandar jaya\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Proklamator Bandar Jaya\t\t\t\t(0725) 528088-528152\t(0725) 528294\t\tPAK Eko Supriyanto\n\tLampung bandar lampung\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. KH. Moch. Salim No. 29 Way Lunik - Panjang\t\t( 0721 ) 31656, 341111\t( 0721 ) 343123\t\tBpk. Misri\n\tLampung Kotabumi\t\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Jend Sudirman No. 202 Kotabumi - Lampung\t( 0724 ) 22563\t\t( 0724 ) 21983\t\tPak Slamet\n\tlampung Metro\t\tSuzuki\t\tPT. Sriwijaya Metropersada\t\tJL. Jend. Soedirman Ganjaragung 14/11 Metro\t\t ( 0725 ) 42386\t\t ( 0725 ) 42336\t\tBpk. Zuki\n\tLampung Metro\t\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Jend. Sudirman No. 198 Metro - Lampung\t\t ( 0725 ) 42588\t\t ( 0725 ) 42522\t\tBpk. Nyoman\n\tLampung Selatan\t\tToyota\t\tPT. Astra International Tbk \t\tJL. Raya Haji Mena No. 2000, Natar 35362\t\t ( 0721 ) 782000\t \t( 0721 ) 789176\t\tBpk. Elfasri\n\tlampung Tanjung Karang\tToyota\t\tPT. Astra International Tbk\t\tJL. Raden Intan No. 61-A Tanjung Karang 35118\t\t ( 0721 ) 252000\t\t ( 0721 ) 264923\t\tBpk. Murad\n\tLampung Tenggiri\t\tMitsubishi\tLautan berlian utama motor\t\t \t\t\t\t\t( 0721 ) 483000\t\t ( 0721 ) 481765\t\tPak Juanda SA X\n\t\t\t\t\t\t\t\n29\tMadiun\t\t\tDaihatsu\t\tPt Jolo Indah Motor\t\tJL Cokrominoto no 96\t\t\t\t(0351) 451275 / 464264\t\t\t\tIBU ANA\n\tMadiun\t\t\tToyota\t\tAUTO 2000 \t\t\tJL Cokrominoto no 47\t\t\t\t(0351) 492000\t\t(0351) 493573\t\tPKS DENGAN BASUKI RAHMAD SA SYAIFUL BAHRI\n\tMadiun\t\t\tIsuzu\t\tJolo Motor ASTRA INTENATIONAL\tJl. Urip sumoharjo no 13 Madiun\t\t\t(0351) 464264\t\t(0351) 455021\t\tKepala bengkel bapak andry 081330010280\n\t\t\t\t\t\t\t\n30\tMagelang\t\tToyota\t\tNasmoco toyota\t\t\tJL. Raya magelang km 5 Magelang\t\t\t(0293) 326871\t\t(0293) 326611\t\tIbu Jamela\n\tMagelang\t\tBAN\t\tSetiawan spooring\t\t\tJL Urip Sumoharjo no 169 \t\t\t\t(0293) 360890\t\t(0293) 360890\t\tIBU SISKA 0888-694525 Adm,PAK Wahyu\n\t\t\t\t\t\t\t\n31\tMakasar\t\t\tToyota\t\tPT. Hadji Kalla II\t\t\tJL. Urip Sumoharjo No. 110 Ujung Pandang\t\t(0411) 448844\t\t(0411) 449303\t\n\tMakasar\t\t\tMitsubishi\tBosowa Berlian Motor\t\tJL. Urip Sumoharjo No. 266 KM 4\t\t\t(0411) 444444\t\t(0411) 447742\t\tZakaria Lande\n\tMakasar\t\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Urip Sumoharjo 64\t\t\t\t(0411) 449111, 449911\t(0411) 449359\t\tEndro Sasongko SDH BUKAN ) SA PAK ISMAIL HP 0852-99300657\n\t\t\t\t\t\t\t\n32\tMalang\t\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. A. Yani 175\t\t\t\t\t(0341) 491743, 496786\t(0341) 491983\t\tSuluh Djatmiko\n\tMalang\t\t\tToyota\t\tPT. Astra International Tbk\t\tJL Let Sutoyo NO 25\t\t\t\t(0341) 472000\t\t(0341) 491003\t\tBERNADHI\n\tMalang\t\t\tSuzuki\t\tPT Sun star motor\t\t\tJL A.Yani no 102\t\t\t\t\t(0341) 456592-3\t\t(0341) 456596\t\n\t\t\t\t\t\t\t\n33\tManado\t\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. RE. Martadinata No. 69\t\t\t\t(0431) 866602\t\t(0431) 851278\t\tPAK ARTHER HP 0431-3398708\n\tManado\t\t\tToyota\t\tCV Kombos\t\t\tJL. Raya Kombos Manado 95233\t\t\t(0431) 813507\t\t(0431) 813537\t\tDiky\n\tManado\t\t\tMitsubishi\tBosowa Berlian Motor\t\tJL. Kairagi No. 36\t\t\t\t\t(0431) 863856\t\t(0431) 862468\t\tRidwan\n\t\t\t\t\t\t\t\n34\tMedan\t\t\tSuzuki\t\tPT. Trans Sumatra Agung\t\tJL. H. Adam Malik D/H Glugur By Pass Medan\t\t(061) 6618006\t\t(061) 6618777\t\tZoson,  \n\tMedan\t\t\tMitsubishi\tPT Sumatra Berlian Motors\t\tJL Tanjung morawa km 7 no 34\t\t\t(061) 7866868\t\t(061) 7867989\t\tPak Didin HP 0812-6507074 / Rina admin\n\tMedan\t\t\tBAN\t\tUD. Banindo Perkasa\t\tJL. Glugur By Pass 100 d/h 90 Medan\t\t\t(061) 6618017\t\t(061) 6632008\t\tKasda / Ali\n\tMedan\t\t\tToyota\t\tAuto 2000 Medan\t\t\tJL. Sisingamangaraja No. 8 Medan\t\t\t(061) 7363388 \t\t(061) 7362300\t\tRobertus Homadi\n\tMedan\t\t\tUmum\t\tCV Asta Padu\t\t\tJL selar No 3 Koala tanjung (aluminium smelting)\t(0621) 21413\t\t(0621) 326474\t\tAyung HP 0811-621948\n\tMedan\t\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Sisingamangaraja No. 170\t\t\t(061) 7349000\t\t(061) 7361133\t\tSutarsono\n\tMedan\t\t\tDaihatsu\t\tCapella Medan Kisaran Asahan\tJL Imam Bonjol No 303  Kisaran dekat Asahan\t\t(0623) 44236\t\t(0623) 44236\t\tSudarsono\n\tMedan \t\t\tDaihatsu\t\tCapella Medan \t\t\tJL. Sisingamangaraja KM 6,5 Tanjung Morawa\t\t(061) 7863214\t\t(061) 7863207\t\tT. Sitanggang\n\t\t\t\t\t\t\t\n35\tPadang\t\t\tDaihatsu\t\tPT. Capella Medan\t\tJL. Prof. Dr. Hamka No. 123 Padang Utara\t\t(0751) 7051777, 7059894 \t(0751) 7057644\t\tYusman ( 0751-7861114\n\tPadang\t\t\tIsuzu\t\tPt.isuindomas\t\t\tJl. S. Parman 182, Padang\t\t\t\t(0751) 51637, 50783, 59124\t(0751) 52130\t\n\t\t\t\t\t\t\t\n36\tPalembang\t\tMitsubishi\tPT. Lautan Berlian Utama Motors\tJL. Raya Jend. A. Yani No. 17 Rt 20/13 Ulu\t\t ( 0711 ) 510583, 510585\t ( 0711 ) 510586\t\tBpk. H. Danu Darmoko\n\tPalembang\t\tSuzuki\t\tPT. Thamrin Bersaudara\t\tJL.Mayor Santoso No. 31 - 38 Palembang\t\t ( 0711 ) 311311, 364072\t ( 0711 ) 311112\t\tBpk. Hendrik Suganda,SA HERY, SOLEH\n\tPalembang\t\tDaihatsu\t\tPT. Astra International Tbk\t \tJL. Jend. A. Yani 100 - 104 Ulu\t\t\t(0711) 511889, 511812\t(0711) 352621\t\tA. Supardi\n\tPalembang\t\tBAN\t\tSumber Ban\t\t\tJL. Jend. Sudirman KM 3.5 No. 22 AB\t\t\t(0711) 356272\t\t(0711) 314662\t\tAnastasia Makmur\n\tPalembang \t\tToyota\t\tPT. Astra International Tbk - Toyota\tJL. Jend. A. Yani No. 5502 Palembang 30126\t\t ( 0711 ) 512000, 517979\t ( 0711 ) 512943\t\tPak Amin ADM SERVICE\n\tPalembang Prabumulih\tSuzuki\t\tPT. Thamrin Bersaudara Sekarang\tJL. Jend. Soedirman KM 6 Prabumulih\t\t\t ( 0713 ) 321071, 321075\t0713-321779\t\tBpk. Budiono\n\t\t\t\t\t\t\t\n37\tPalu\t\t\tMitsubishi\tBosowa Berlian Motor\t\tJL. RA. Kartini No. 51\t\t\t\t(0451) 422026\t\t(0451) 421196\t\tAbdurahim (0811459337)\n\t\t\t\t\t\t\t\n38\tPangkal pinang\t\tMitsubishi\tPT lautan berlian utama motor\tJl M.S Rachman no 59 A Pangkal Pinang 33144\t\t(0717) 431666 / 422333\t(0717) 431666\t\tEDO\n\t\t\t\t\t\t\t\n39\tPekalongan\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Merdeka 19\t\t\t\t\t(0285) 412345\t\t(0285) 412828\t\tAli Arifin\n\t\t\t\t\t\t\t\n40\tPekanbaru\t\tDaihatsu\t\tPT. Capella Medan\t\tJL. Arengka No. 53\t\t\t\t\t(0761) 571900\t\t(0761) 572411\t\tAdi SUCIPTO ( HP : 08127529826 ) SA P'GINTING\n\tPekanbaru\t\tMitsubishi\tPT. Suka Fajar\t\t\tJL. Arengka No. 352-354 No. 140\t\t\t ( 0761 ) 572544\t\t ( 0761 ) 572816\t\tBpk. Taufik\n\tPekanbaru\t\tMitsubishi\tPT Pekan Perkasa Berlian Motors\tJL. Jend. Sudirman No. 230\t\t\t\t(0761) 848808,76129027\t(0761) 848438\t\tBpk. H. GINO HP 0761-3060316\n\tPekanbaru\t\tAC \t\tSekawan Servis\t\t\t. Riau Ujung No. 328\t\t\t\t(0761) 861668/853684\t(0761) 861663 / 858818\tPingping\n\tPekanbaru\t\tToyota\t\tPT. Agung Automall\t\tJL. Dr. Sutomo No. 13, Pakanbaru 28143\t\t(0761) 22252\t\t( 0761 ) 32352\t\tBpk. Made W / Bpk. Gunadi EXT 105 P'HENDRA SA\n\tPekanbaru\t\tAC \t\tCitra Sejuk Klimatindo\t\tJl. Soekarno Hatta no 2 Arengka\t\t\t(0761) 7051340\t\tBpk Kainul 08127534340\n\tPekanbaru\t\tIsuzu\t\tPt. Insuindomas Putra\t\tJl. Tambusai no 347-349\t\t\t\t0761 – 29145/46\t\t(0761) 29147\t\tBpk Yulius\n\t\t\t\t\t\t\t\n41\tPematang Siantar\t\tDaihatsu\t\tPT Capella Medan\t\t\tJL. Medan Km 6 Simpang Karang Sari \t\t\t(0622) 23711\t\t(0622) 7436522\t\tPak Salam Bangun HP 0812-64301354\n\t\t\t\t\t\t\t\n42\tPontianak\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Imam Bonjol 529\t\t\t\t(0561) 748260, 748262\t(0561) 732768\t\tM. Ismail\n\tPontianak\t\tMitsubishi\tGemilang berlian Indah\t\tJl arteri Supadio km 1.5\t\t\t\t(0561) 589888\t\t(0561) 582999\t\tIBU ERA\n\tPontianak\t\tIsuzu\t\tBorneo auto cemerlang\t\tJl . Pahlawan no 2 & 3 \t\t\t\t(0561) 735757\t\t(0561) 734308\t\tPak Arif\n\tPontianak\t\tBAN\t\tBan Pontianak\t\t\tJl. Imam Bonjol No 8 A\t\t\t\t(0561) 761607/608\t\t\t\t\tKo Atman\n\t\t\t\t\t\t\t\n43\tProbolinggo JATIM\t\tSuzuki\t\tSurya Raya Motor\t\t\tJl Soekarno Hatta  170 \t\t\t\t20420-23108\t\t\t\t\tLIENA  SURYAWATI\n\t\t\t\t\t\t\t\n44\tPurwokerto\t\tMitsubishi\tSinar Berlian Auto Graha\t\tJL. Gerilya Timur 103\t\t\t\t(0281) 635333\t\t(0281) 636777\t\tJumeno\n\tPurwokerto\t\tDaihatsu\t\tTorana Motor\t\t\tJL. Jend. Sudirman No. 61\t\t\t\t(0281) 633934\t\t\t\t\tYudi ( HP : 08156971260 )\n\t\t\t\t\t\t\t\n45\tSamarinda\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Ir. H. Juanda No. 55\t\t\t\t(0541) 748595\t\t(0541) 748594\t\tArthur Kindangen\n\t\t\t\t\t\t\t\n46\tSampit\t\t\tMitsubishi\tMurni Berlian Motor\t\t\t\t\t\t\t\t(0531) 30876\t\t(0531) 30867\t\tIBU MILA\n\tSampit\t\t\tSemua\t\tDwi Jaya Motor\t\t\tJL. cilik riwut KM 2 kalteng\t\t\t\t(0531) 32000\t\t(0531) 31700\t\tHelen / yayu/  Agus subagyo bos HP 0811-524001\n\t\t\t\t\t\t\t\t\n47\tSemarang\t\tMitsubishi\tSidodadi Berlian Motor\t\tJL. Siliwangi No. 287 A Kalibanteng\t\t\t(024) 7603957\t\t(024) 7604206\t\tHeru Rubianto\n\tSemarang\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Majapahit No. 111-117\t\t\t\t(024) 6717111, 6718111\t(024) 6723650\t\tA. Bayu Syafe'I\n\tSemarang\t\tSuzuki\t\tPT. Sunmotor Indosentra Strada\tJL. Pemuda No. 65 Semarang\t\t\t(024) 3565000\t\t(024) 3584677\t\tDita Mahar\n\tSemarang\t\tBAN\t\tFajar Baru\t\t\tJL. Barito Raya 58 ( Sebelah Ciliwung II )\t\t(024) 3543156\t\t(024) 3543156 PENCET 101\tWahyu / RITA\n\t\t\t\t\t\t\t\n48\tSidoarjo\t\t\tMitsubishi\tBumen Redja Abadi\t\tJL. Raya Larangan  No. 2 Candi\t\t\t(031) 8950467\t\t(031) 8921401\t\tHeri Winarko IBU UMI SERVICE\n\tSidoarjo\t\t\tUmum\t\tMEGAH ASRI MOTOR\t\tJL Wadug Asri 82-84 Waru Sidoarjo\t\t\t(031) 8672866\t\t(031) 8672844\t\tIBU LENI\n\t\t\t\t\t\t\t\n49\tSolo\t\t\tDaihatsu\tPT. \tAstra International Tbk \t\tJL. Raya Solo Permai - Solo baru\t\t\t(0271) 620973, 620977\t(0271) 620963\t\tM. Surofi\n\tSolo \t\t\tMitsubishi\tPT Satrio Widodo\t\t\tJL. Adisucipto km 7.3\t\t\t\t0274-486706/488601\t0274-489214\t\tPak Sugi\n\t\t\t\t\t\t\t\n50\tSukabumi\t\t\tToyota\t\tSelamet  lestari mandiri\t\tJL Arif rahman hakim no 43\t\t\t\t(0266) 221800 /224976\t(0266) 224976/ 222354\tBudi harso\n\tSukabumi\t\t\tMitsubishi\tPT. Merdeka Motors\t\tJL. KH. A. Sanusi No. 33\t\t\t\t(0266) 222702,225023\t(0266) 225044\t\tRudy ( 08569994398 )\n\t\t\t\t\t\t\t\n51\tSumedang\t\t\t\tCV Sumber Rejeki\t\t\tJL. Mayjen Abdul Rachman no 128-130\t\t(0266) 201527/201953\t(0266) 201720\t\tPak Apandi\n\t\t\t\t\t\t\t\n52\tSurabaya\t\t\tBAN\t\tParamitha Ban\t\t\tJL. Jemur Sari Raya No. 150 Surabaya\t\t\t(031) 8432112\t\t(031) 8497087 /8438701\tDede (DEWI/EKA PIC)\n\tSurabaya\t\t\tToyota\t\tPT. Astra International Tbk\t\tJL. Basuki Rahmat 115-117\t\t\t\t(031) 5452000\t\t(031) 5342060\t\tHartono Kurniawan SA Pak Rofikun / WAHYUWIDIATMOKO\n\tSurabaya\t\t\tMitsubishi\tMurni Berlian Motor\t\tJL. Demak 172 Surabaya\t\t\t\t(031) 5323736 / 5353531\t(031) 5314386\t\tSudarsono\n\tSurabaya\t\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. HR. Muhammad No. 73 Bengkel Sungkono\t\t(031) 7312000\t\t(031) 7314000\t\tAzis ( SA )\n\tSurabaya\t\t\tMitsubishi\tPT Sun star motor\t\t\tJL. Ngagel  no 81-85\t\t\t\t(031) 5015690/1\t\t(031) 5018272\t\tPak Yohan\n\tSurabaya\t\t\tAC \t\tPanca Jaya Ac\t\t\tJl Kapas krampung No 1991\t\t\t\t(031) 5037265 \t\t(031) 5039131\t\tP' Ameng HP 0812-1785198 / 7341156\n\tSurabaya\t\t\tSuzuki\t\tUnited Motor Centre\t\tJL A.Yani no 40 \t\t\t\t\t(031) 8280612\t\t\t\t\tP'BAMBANG SUGIONO\n\tSurabaya\t\t\tIsuzu\t\tAstra Isuzu\t\t\tJL Kombes M Duryat  17\t\t\t\t(031) 5470808\t\t(031) 5342269\t \n\tSurabaya\t\t\tHonda\t\tHonda Saver\t\t\tJl Genteng Besar 106-110 Surabaya\t\t\t(031) 5460975 – 5325525\t(031) 5346894\t\n\tSurabaya Hr Muhammad\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. HR Muhammad No. 4 & 6\t\t\t\t(031) 7345700\t\t\t\t\tAndreas Benjamin\n\tSurabaya Waru\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Sawo Tratap Km 15 Waru - Sidoarjo\t\t(031) 8533777, 8535526\t(031) 8533778\t\tAidil FB Swastomo, SA PAK SUWOTO\n\t\t\t\t\t\t\t\n53\tTangerang\t\tHino\t\tPT Hino Motor Sales Indonesia\tJL Raya gatot subroto KM 8.5 Tgr\t\t\t(021) 5918844\t\t(021) 5917887\t\tPak Deden \n\t\t\t\t\t\t\t\n54\tTasikmalaya\t\tSuzuki\t\tCakra Putra Parahyangan\t\tJL. Dr. Moh. Hatta No. 158\t\t\t\t(0265) 337470\t\t(0265) 337471\t\tJono (081802281163) ADM SA IBU AIRIKA\n\t\t\t\t\t\t\t\n55\tTegal\t\t\tDaihatsu\t\tPT. Astra International Tbk\t\tJL. Kol. Sugiono No. 104\t\t\t\t(0283) 359676, 359677\t(0283) 324954-359678\tAgustinus Karyadi\n\tTegal \t\t\tMitsubishi\tPT Matahari Berlian Motor\t\tJL. Kapten sudibyo no 125\t\t\t\t(0283) 352525/359595\t(0283) 350505\t\n\t\t\t\t\t\t\t\n56\tYogyakarta\t\tBAN\t\tSetiawan spooring\t\t\tJL Laksda Adisucipto KM 7\t\t\t\t(0274) 867449\t\t(0274) 867449\t\tIBU TUTI/ IBU Galuh adm, PAK Wahid \n\tYogyakarta\t\tDaihatsu\t\tPT. Astra International Tbk \t\tJL. Magelang KM 7,2\t\t\t\t(0274) 868074, 868075\t(0274) 868650\t\tSuhardiman\n\tYogyakarta\t\tMitsubishi\tUD. Borobudur Motors\t\tJL. Laksda Adi Sucipto Km 73\t\t\t(0274) 486706/488601\t(0274) 512214/ 487169/ 488573 OK\tSugih Utomo IBU SUMIRAH\n\tYogyakarta\t\tToyota\t\tNasmoco toyota\t\t\tJL Raya Magelang km 7\t\t\t\t(0274) 868808\t\t(0274) 868992\t\tP'BANDUNG 0856-643163369\n");
         jTextArea11.setBorder(null);
         jTextArea11.setOpaque(false);
         jScrollPane45.setViewportView(jTextArea11);
 
         jPanel23.add(jScrollPane45);
         jScrollPane45.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("Bengkel Rekanan 2", jPanel23);
 
         jPanel8.setBackground(new java.awt.Color(255, 255, 255));
         jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel8.setLayout(null);
 
         jTextArea6.setColumns(20);
         jTextArea6.setEditable(false);
         jTextArea6.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea6.setRows(5);
         jTextArea6.setTabSize(5);
         jTextArea6.setText("\nEmergency Unit\t\tPIC\t\ttelepon\t\tketerangan\npic\t\t\tPak Yos\t\t081288536789\turgent\nemergency driver\t\t\t\t021-53673035\toffice hour\nemergency driver\t\t\t\t08118885381\t\n");
         jTextArea6.setBorder(null);
         jTextArea6.setOpaque(false);
         jScrollPane24.setViewportView(jTextArea6);
 
         jPanel8.add(jScrollPane24);
         jScrollPane24.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("Emergency Driver", jPanel8);
 
         jPanel11.setBackground(new java.awt.Color(255, 255, 255));
         jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel11.setLayout(null);
 
         jTextArea7.setColumns(20);
         jTextArea7.setEditable(false);
         jTextArea7.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea7.setRows(5);
         jTextArea7.setTabSize(5);
         jTextArea7.setText("Zona & No. HP Koordinator\t\t\t\nKoord. Lapangan\t\t\t\n\t\t\t\nNo.\tZona\t\t\t\tNama\t\tNo. HP\n1\tKarawang, Cikampek\t\tRifai\t\t081311379819\n2\tCikarang\t\t\t\tBayu\t\t08111800327\n3\tBekasi, Cibitung, MM2100\t\tFitra\t\t081310675702\n4\tDalam Kota I\t\t\tZulkarnaen\t081280453599\n5\tDalam Kota II\t\t\tDeni\t\t081280420067\n6\tSelatan Jakarta\t\t\tAbel\t\t081369630978\n7\tLingkar Luar Selatan Jakarta, PMI\tNares (ex Priyo)\t081398584778\n8\tLingkar Luar Barat Jakarta\t\tPriyo\t\t081288308872\n9\tLingkar Luar Utara Jakarta\t\tDavid\t\t081288304222\n10\tLingkar Luar Timur Jakarta\t\tDiki (ex Izul)\t081386555553\n11\tLuar Kota\t\t\t\tDidin\t\t081381756909\n\t\t\t\nKoord. Back-Up\t\t\t\n\t\t\t\nNo.\tZona\t\tNama\t\tNo. HP\n1\tKlarifikasi\t\tHendry\t\t08176621874\n2\tRental / GT\tIwan\t\t085288666869\n3\tGS / Standby \tDidin\t\t081381756909\n4\tTraining\t\tSupardiyanto\t08121315665\n5\tKlarifikasi\t\tSido\t\t085222206161\n6\tTraining Staff\tHartono\t\t085248329822\n");
         jTextArea7.setBorder(null);
         jTextArea7.setOpaque(false);
         jScrollPane25.setViewportView(jTextArea7);
 
         jPanel11.add(jScrollPane25);
         jScrollPane25.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("Koordinator Driver", jPanel11);
 
         jPanel12.setBackground(new java.awt.Color(255, 255, 255));
         jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel12.setLayout(null);
 
         jTextArea8.setColumns(20);
         jTextArea8.setEditable(false);
         jTextArea8.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea8.setRows(5);
         jTextArea8.setTabSize(5);
         jTextArea8.setText("\nunit LAKA\t\n\t\nPIC\t\tTelepon1\tTelepon2\nRONNY\t\t08161440682\t081380001562");
         jTextArea8.setBorder(null);
         jTextArea8.setOpaque(false);
         jScrollPane26.setViewportView(jTextArea8);
 
         jPanel12.add(jScrollPane26);
         jScrollPane26.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("LAKA", jPanel12);
 
         jPanel13.setBackground(new java.awt.Color(255, 255, 255));
         jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel13.setLayout(null);
 
         jTextArea9.setColumns(20);
         jTextArea9.setEditable(false);
         jTextArea9.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea9.setRows(5);
         jTextArea9.setTabSize(5);
         jTextArea9.setText("\t\n\tunit Luar Kota\t\t\n\nno \tPIC\ttelepon\t\tcakupan\n1\tDhani\t08159312202\tsumatera, sulawesi, kalimantan\n2\tAdam\t0817794945\tjawa, bali , papua\n3\tDewi\t02127481033\tsurat luar kota\n");
         jTextArea9.setBorder(null);
         jTextArea9.setOpaque(false);
         jScrollPane27.setViewportView(jTextArea9);
 
         jPanel13.add(jScrollPane27);
         jScrollPane27.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("Unit Luar Kota", jPanel13);
 
         jPanel16.setBackground(new java.awt.Color(255, 255, 255));
         jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel16.setLayout(null);
 
         jTextArea10.setColumns(20);
         jTextArea10.setEditable(false);
         jTextArea10.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea10.setRows(5);
         jTextArea10.setTabSize(5);
         jTextArea10.setText("\n\t\tNo Telepon Storing Unit\t\t\t\n\t\t\t\nStoring\t\tTelepon\t\tFax\t\tPIC\nKedoya\t\t021 - 5680077\t021 - 56007123\tIbu Ria\nCibitung\t\t021 - 89982288\t021 - 89982233\tBpk Edi\n");
         jTextArea10.setBorder(null);
         jTextArea10.setOpaque(false);
         jScrollPane32.setViewportView(jTextArea10);
 
         jPanel16.add(jScrollPane32);
         jScrollPane32.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("Storing Unit", jPanel16);
 
         jPanel24.setBackground(new java.awt.Color(255, 255, 255));
         jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel24.setLayout(null);
 
         jTextArea12.setColumns(20);
         jTextArea12.setEditable(false);
         jTextArea12.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
         jTextArea12.setLineWrap(true);
         jTextArea12.setRows(5);
         jTextArea12.setTabSize(5);
         jTextArea12.setText("\t\t\t\t~PHONEBOOK ANJ RENT~ UP DATE\n\nTLP     \t0213867878\nFAX     \t0213865030\nSMS  \t081386987878\t\nEMAIL \tcontact@anjrent.com\n=============================================================================================\n\t\t                                         ====^EMERGENCY^====\n\nBERLAN (KOOR BCA POOL)\t\t\t08111753836\nPERMINTAAN DRIVER (YOS)\t\t\t081288536789\n(EMERGENY KENDARAAN)\t\t\t021- 53673035 /  NEW 58304396  /  5491021 (FAX)                \t\t\t\n(DRIVER)\t\t\t\t\t085289939394 / UPDATE 28/2 =>  081310300020\nRIKI DEPT DRV CNKRG\t\t\t081380700887\n(URGENT)\t\t\t\t\t08118885381\nTEAM LAKA RONNY      \t\t\t08161440682   /  081380001562\nMUKHTAR CIBITUNG\t\t\t\t085710022868 /  085716169192\nPURWANTO DEPT KEND\t\t\t087883008555\nNORMAN DEPT DRV\t\t\t\t085213000810\nDAUD (KEDOYA)\t\t\t\t081281196503\nEMERGENCY STORING \t\t\t081511231510\nEDY CIBITUNG\t\t\t\t081386778800\nNOVER CIBITUNG \t\t\t\t08129102505\nYUDI JOE\t\t\t\t\t085643566755  /  081210436661 / 08999242282\nDEPT KEND CENGKANRENG \t\t\t70613421 / 70613420\nDIRECT LINE MARKETING\t\t\t53677281\nBPK EDI CIBITUNG\t\t\t\t081386778800\nHENDRI\t\t\t\t\t081316155533\nDEDY LK\t\t\t\t\t085691510607\nNANA ANJ PANJANG (STNK)\t\t\t085883869676\nAMUNG\t\t\t\t\t081385998098\t\nMARIO\t\t\t\t\t081510087117\nNYOMAN BALI\t\t\t\t08123910911 \nKORLAP DAVID                       \t\t\t085213151061\nKORLAP ABEL\t\t\t\t081369630978\nKORLAP DENY\t\t\t\t081280420067\nKORLAP TRI ADI\t\t\t\t081382940000\nKORLAP RIFAI\t\t\t\t081311379819\nKORLAP AGUNG\t\t\t\t081282403686\nKORLAP AGUS\t\t\t\t082111316488\nKORLAP HENDRI\t\t\t\t081316155533\nKORLAP PRIYO (BOT)\t\t\t081288308872\nDIDIN STORING CILEGON\t\t\t085694276595\nDEWI DOKUMEN\t\t\t\t081908325877 / 021-27481033, paling baru => 083898555337 \t\nOBENK (Bp Kris/Ibu Niar)  \t\t\t53162052\n  \n\t\t\t\t              ===^LUAR KOTA^===\nFAHMI\t\t\t\t\t085693031668\nADAM\t\t\t\t\t0817794945 \t\nDEDY LK\t\t\t\t\t085691510607\nDEWI DOKUMEN\t\t\t\t083898555337 / 02127481033\nLIA STNK\tDALAM KOTA\t\t\t081210001225\n\njika tidak ada perwakilan maka klaim asuransi \"body repair\" dikirimkan kepusat\n\nEDWIN KIR LUAR KOTA\n\" BERKAS STNK\" \t\t\t\t081388207404\t\tedwin.suharta@anjrent.com, \n\t\t\t\t\t\t\t\tcc : contactcenter@anjrent.com\n      \t\t\t\t\t\t\t\tcso terkait\n\nTONI  BERKAS STNK - SEMARANG\t\t081326660010 / 081802449353\nNOVI  BERKAS STNK - SURABAYA\t\t08819349372 /  087854323000\nWIDI  BERKAS STNK - SOLO\t\t\t08179487146\nRIKI  BERKAS STNK - PALEMBANG\t\t0811712302\n\nTELP PANJANG (DGN EXT)\t021-5367 0880\t\nEXT DEPT.LUAR KOTA \t118\nEXT DRIVER PANJANG\t119\nEXT IBU IKA (ACC)\t\t223\nEXT IBU NANA\t\t110\nEXT BP FAHRUR\t\t106\nEXT IBU DEWI\t\t115\nEXT LUAR KOTA`\t\t108\nEXT INVOICE\t\t224\nEXT IBU JUWITA (ADM DRV)\t114\n\n============================================================================================\n\t\t\t      \t\t===^CSO^===\n\t\t     \t                                           (RESIGN)\n    NAMA\t\t\t\t\tHP                                              \t\t            EMAIL\n1.  SISKA   \t\t\t087882817788 //\n\t\t\t\t081398799878 (PAKAI NO INI)\t\tsiska@anjrent.com\t\n2.  ICA \t\t\t\t08111558115\t             \t\tica@anjrent.com\n3.  FANI\t\t\t\t087885158737 (PAKAI NO INI)  \t\tstephanie@anjrent.com\n4.  INDAH\t\t\t\t08119001882 \t\t             \tindah.srimulyani@anjrent.com \n5.  RINA\t\t\t\t08111558114\n6.  DHANI\t\t\t\t08111558113/ 08159312202    \t \tdhani.anj@blackberry.com\n     \t\t\t\t\t\t\t\tdhani@anjrent.com;\t\t\t\t\t\t\n=============================================================================================\n\t\t\t\t                  ===^CSO^===\n\t\t     \t                                            (AKTIF)\n\n1.   LISMA \t\t\t08111558117\t             \t\tlisma.silaen@anjrent.com\n2.   TASYA\t\t\t\t081315453800\t\t\ttasya@anjrent.com\n3.   INEZ \t\t\t\t0811839744 // 02192501001\t\tinez@anjrent.com\n4.   LINA\t\t\t\t081806981333\t             \t\tlina@anjrent.com\t\n5.   FENNY \t\t\t\t08111558114 \t\t\tfenny@anjrent.com\t\n6.   TASYA \t\t\t081315453800\t           \t\ttasya@anjrent.com\n7.   ROSI\t          \t\t\t0811839681 \t             \t\trosi@anjrent.com \n8.   NIA\t\t\t\t08111558118 (pakai ini) //\n\t\t\t\t081218734881\t\t\tnia@anjrent.com\n9.   DIAH\t\t\t\t08119001882\t\t\tdiah@anjrent.com\n10. SARAH\t\t\t\t08119201499 \t             \t\tsarah.dewi@anjrent.com\n11. STEFY\t\t\t\t08111558116\t             \t\tstefy@anjrent.com\n12. ARAI\t\t\t\t0811975655\t             \t\tarai@anjrent.com\n13. TAGUCHI\t\t\t0811152936                           \t\ttaguchi@anjrent.com\n14. SAGA\t\t\t\t0813199333216\t             \t\tsaga@anjrent.com\n15. EVIE\t\t\t\t0811179035\t\t\tevie@anjrent.com\n16. NOVIE\t\t\t\t081510205724 / 0811142772\t\tnovie@anjrent.com\n17. FENNY \t\t\t\t08111558114 \t\t\tfenny@anjrent.com\n18. DYAH\t\t\t\t0811164143\t\t\tdyah@anjrent.com\t\n19. RUTH S\t\t\t08111558115\t\t\truth@anjrent.com\n20. IWAN\t\t\t\t08118406471\t\t\tiwan.darmawan@anjrent.com\t\n21. VIAS\t\t\t\t08119202799\t\t\ttrivias@anjrent.com\n22. KIKI\t\t\t\t085883287417\t\t\tkiki.maulana@anjrent.com\n23. RITA\t\t\t\t0811152797\t\t\trita@anjrent.com\n\nNEW PROSPEK CUSTOMER\n\n24. RULLY\t\t\t\t0811542392\t\t\trully.herlambang@anjrent.com\n25. ROY ISKANDAR\t\t\t087881600805              \t\troy@anjrent.com\n26. STEVEN \t\t\t087887975155          \t\t\tsteven.chang@anjrent.com\n27. HANDY LIEONG\t\t\t08128609677 \t\t\thandylioeng@anjrent.com\n28. INEZ \t\t\t\t0811839744 // 02192501001\t\tinez@anjrent.com\n29. HORI\t\t\t\t\t\t\t\thori@anjrent.com\n30. AGUNG                                                                 \t\t\t\tagung@anjrent.com\n\n\nDIRECT LINE MARKETING              \n\t\t 53677281\n\nINVOICE \t\t\t^Berkenaan Invoice^\n31.  CHRISTIAN KURNIAWAN  53670880  ext 224\t\tFax 02153677286\n\n\n=========================================================================================\n\t                                             ^OPERASIONAL KEND. POOL CENGKARENG^\n\t\t                          ====================================\n\n>>> BARANG TERTINGGAL\t\t\t \t      => FAJAR             :  70613421\n\t\t\t\t\t\t      => AMUNG           :  70613421     /   081385998098\n\t\t\t\t\t   \t    \n>>> KEPASTIAN MOBIL GS \t\t\t\t      => PURWANTO  :   58351787     /  087883008555\n\n>>> PERMINTAAN URGENT, TIKETING, STORING, SERVICE\t      => JOE                  :  53673035     /  70613420\t\t\t\t\t\t\t\t      => ALKIN\t   \t            085643566755\t\t\n\n>>> BAN & SPAREPART LOGISTIK, HISTORY PERBAIKAN\t      => IRWAN             :  70613421\n\n\n===========================================================================================\n \t\t                          =====^JADWAL STORING & LOKASI^=====\n\t\t\t  \t  \n1> STOORING FAMILY SUMMITMAS PARKIRAN LT 6, JUMAT\t\t\tPKL 10:00-15:00\n2> STOORING SUMMITMAS, SELASA, PASTINYA SETELAH MAKAN SIANG \t\tPKL 15:00\n3> STOORING MIDPLAZA, SENIN\t\t\t\t\t\tPKL 10:00-12:00\n\n============================================================================================\n  \t\t\t\t               ===^BENGKEL^===\n\t   NAMA BENGKEL \t\t            \t\t\t                                ALAMAT\n\n1>              CENGKARENG (UP: DAUD)       \t\t583053999   \t\t58358181 (F)\n\t 081281196503\n\t\t\t\tFamily Serviceindo Cengkareng   Jl.Bojong raya tepat di depan SMU Vianny\n\t- DAUD CENGKARENG : \t\t58305555 (Direct Bpk Daud)\n\n2>              CIBITUNG (UP: EDI)        \t\t89982288 \t\t89982233 (F)    \t\t\n\t\t\t\tFamily Serviceindo Cibitung \t     Jl.Sumatra D4 kawasan Industri MM 2100                           \n\tCIBITUNG\n\t- EDI CIBITUNG              :\t\t081386778800\n\t- MUKHTAR                    :\t\t085710022868\n                   - NOVER CIBITUNG       : \t\t08129102505\n\n3> \tCIKARANG OTOZONE \t\t89906611\n                  Jl. MH. Thamrin Kav. 133 C Lippo Cikarang \n=============================================================================================\n\t\t\t\t     ===^HEAD OFFICE ANJ RENT^===\n\t\t\t\t\t\t\n1.  ANJ Rent Head Office Panjang\tJl. Arteri Kelapa Dua No 16 , Kebon Jeruk Jakarta Barat\t\n    \tPurwanto\t\t\t021 – 5367 0880\t\t\tfax: 021 – 549 1021\n\n2.  ANJ Rent Cabang Cibitung\t\tJl. Sumatra D4 kawasan Industri MM 2100 CIBITUNG\t\n    \tMuktar \t\t\t021 – 89982288\t\t\tfax: 021 – 89982233\n\n3.  ANJ Rent Cabang Cengkareng\tJl. Bojong raya tepat di depan SMU Vianny\t\n\tMario\t\t\t021 – 706 13 421\t\t\tfax: 021 – 583 04829\n\t\n=============================================================================================\n\t\t\t\t       ===^INFORMASI GS DRIVER^===\n\nNORMAL\n1>. TAIYO SINAR RAYA TEKHNIK \nCATEGORY: OTHER-OTHER \n\t      \nPIC JAPAN \t   : MR EUZUMI\t\t0811883952\nCSO JAPAN\t   : MR TAGUCHI\t\t0811152936\t\t  \n\t\nPIC INDONESIA \t   : MR SARKUN \t\t081389118492 \nCSO INDONESIA\t   : DIAH\t\t\t08119001882\n\n\n2>. YAMAHA MORIQ\nCATEGORY: OTHER-OTHER \n\nPIC JAPAN\t: SUGIYAMA DAISUKE\t0818880928\n\n==> CONTOH FORMAT SMS:\n\nSMS KE USER\nDear (nama user/PT.Taiyo Sinar raya Tekhnik),pls kindly be informed, due to (alasan penggantian drv), (nama & no tlp drv ori), replaced with (nama & no tlp drv GS), standby at (alamat & waktu standby drv GS). CC ANJ Rent\n\nEX:\nDear Mr Enrico,pls kindly be informed,due to drivers absence,Sujaka 087879422727replaced with Buchori 085888584984,Nov 7rd,stand by at .....Taiyo Sinar Cibitung at 6.30 am. CC ANJ RENT.\n\nSMS KE PIC IND/JAPAN:\n(Copy)Dear Mr Uezumi,pls kindly be informed,due to drivers absence,Edi Suyatno 081379543604 replaced with Heri S 081356555592, Nov 3rd,stand by at Park P1 Lot41.Nama user. CC ANJ RENT.\n\nSelamat malam, (Nama User/PT Taiyo Sinar Raya Teknik), kami sampaikan, berkenaan (Alasan), (Nama & no telp Drv Ori) digantikan dengan ((Nama & no telp DRV GS), stand by di (Alamat & waktu stand by DRV GS). CC ANJ Rent\n\n============================================================\n\nDRAFT EMAIL KE BAG DOKUMEN:\n\nSUBJECT: PT XXX - PLAT, Masa berlaku STNK akan berakhir tgl 4/4\n\nSelamat siang, Ibu Nana\n\nMohon bantuannya mengenai masa berlaku STNK yg akan berakhir. Berikut datanya:\n\nCustomer\t\t: ASAHI DENSO INDONESIA\nPlat No / Tipe kend\t\t: B 7086 QG / KIJANG INNOVA G 2.0 MT HITAM METALIK 2008\nTgl berakhir STNK\t\t: 4 April 2011\nContact Person\t\t: Driver Zikri 081317824605\nLokasi pengambilan\t: Asahi Denso Jl Irian 5\n\nTerima kasih, \n\n\nCC ANJR\n==============================================================\n\nTUJUAN EMAIL MENGENAI KLAIM ASURANSI\nLuar Kota\t\t- to: \tadm.operation@anjrent.com; edwin.suharta@anjrent.com\n\t\t-cc:\tcso terkait; contact@anrjent.com\n\nDalam Kota\t-to:\tadm.operation@anjrent.com; mario@anjrent.com\n\t\t-cc:\tcso terkait; contact@anjrent.com\n===============================================================\nINFO DARI IBU DEWI:\n-STNK dalam kota plat B, dapat ke IBU LIA 081210001225, emailnya dapat ke IBU DEWI/NANA\n-KIR DALAM KOTA plat B, dapat ke BP EDWIN 081388207404, email: edwin.suharta@anjrent.com\n\n================================================================\nREIMBURST LUAR KOTA:\nSelamat siang, Bp Jimi. Kami memerlukan invoice/kuitansi asli juga atas nama PT ANJ Rent utk pengurusan reimbursement. Mohon invoice/kuitansi asli tersebut dikirim ke: Bpk Yulianto - Operasional Kendaraan, PT ANJ Rent, Jl. Arteri Kelapa Dua No 16, Kebon Jeruk, Jakarta Barat. Terima kasih. CC ANJR \n\n================================================================\nKLAIM ASURANSI LUAR KOTA (tanpa no telp):\nSelamat siang, Ibu Delin\n\nMengenai klaim asuransi kendaraan, kami mohon bantuannya untuk melengkapi data berikut: kronologis kejadian, kopi SIM-STNK dan foto kerusakan. Data tersebut mohon dikirim lewat email to: adm.operation@anjrent.com; edwin.suharta@anjrent.com, cc: contact@anjrent.com; (cso terkait)\nKami juga mohon informasi nomor telepon (kantor dan HP) yang dapat dihubungi agar memudahkan komunikasi nantinya. \n\nTerima kasih, \n\n\n\t\t\t             ===^PROSPEK DATA CUSTOMER / EXISTING^===\n\n\nRULLY\t\t0811542392\t\t\trully.herlambang@anjrent.com\nROY ISKANDAR\t087881600805              \t\troy@anjrent.com\nSTEVEN \t\t087887975155          \t\tsteven.chang@anjrent.com\nHANDY LIEONG\t08128609677 \t\t\thandylioeng@anjrent.com\nINEZ \t\t0811839744 // 02192501001\t\tinez@anjrent.com\n\nMAIN PIC\t: RULLY HERLAMBANG (SMS, TLP AND EMAIL)\n2nd PIC\t: ROY ISKANDAR (EMAIL)\n\nCC EMAIL TO: \nRULLY\t\t\t\t\t\trully.herlambang@anjrent.com\nSTEVEN \t\t\t\t          \t\tsteven.chang@anjrent.com\nHANDY LIEONG\t\t\t\t\thandylioeng@anjrent.com\nINEZ \t\t\t\t\t\tinez@anjrent.com\nHORI\t\t\t\t\t\thori@anjrent.com\nAGUNG                                                                 \t\tagung@anjrent.com\n\n\nData-data yg dibutuhkan (existing cust & new prospek cust)\n- nama penelepon & perusahaan & alamat perusahaan ( jika customer keberatan memberikan alamat perusahaan tdk   ush memaksa utk menanyakan)\n- no tlp caller (min 2 Hp / kantor)\n- Sumber info mengenai ANJR & apakah sudah pernah Rental sblmnya\n- Pengunaan rental perorangan / perusahaan (klw perorangan tdk bisa)\n- Kendaran yg akan dirental (Jenis,type,tahun,jumlah unit)\n- Periode & Perkiraan Periode Rental\n- Area Penggunaan Rental\n- Request Driver / No (short time menggunakan drv)\n\n\nFORMAT SMS (existing cust): DEPT.MARKETING >> REQ.QUOTATION \nDear (PIC),Mhn bantu f/u prospek cust sbb: (nama caller),(tlp 1, tlp 2), existing cust(nama customer / perusahaan) / req (jenis, type,tahun,jumlah unit rental) / (periode &perkiraan tgl kebutuhan rental) / (area penggunaan rental) / (driver/no driver). CC ANJR.\n\nFORMAT SMS (prospek cust) : DEPT.MARKETING >> NEW CUSTOMER\nDear (PIC),Mhn bantu f/u prospek cust sbb: (nama caller),(tlp 1, tlp 2), (sumber info ttg ANJR) / (sumber info mengenai ANJR)(pernah/belum rental ANJR) / req (jenis, type,tahun,jum;ah unit rental) / (periode &perkiraan tgl kebutuhan rental) / (area penggunaan rental) / (driver/no driver). CC ANJR.\n \nEMAIL PIC ANJR\nDear (Nama PIC),\nTelah dilakukan follow up oleh (Nama PIC yg berhasil dihubungi CC) terhadap prospek data existing customer / new prospek data berikut ini:\n(nama caller),(no tlp.1&2),existing cust (nama cust / perusahaan)/req(jenis,type,tahun,jumlah unit rental)/(periode & tgl perkiraan rental)/(area penggunaan rental)/(driver / no drv).\nCC ANJR.\n\nKELUHAN\nUntuk setiap informasi /keluhan yang diterima oleh CC dari Customer :  Loreal\nin parallel,, mohon SMS ke CSO Dyah & Bpk  Handy = 08128609677\n\nUntuk yang berhubungan dengan operational, in parallel, kirimkan SMS ke Pak Mario (081510087117)\n\nUntuk informasi Luar Kota juga diinformasikan by SMS kepada Bpk Dedy = 085691510607\n\t\t\n=================================================================================================\n\n\t\t\t                   ===^KASUS PERMASALAHAN^==\n\t\t\t                   ===========================\n\n\n>> Mobil mogok             : Nama YBS; Plat Kendaraan; PT. mana; Tanyakan Penyebab Mogok, Lokasi kend.; bertemu dengan siapa, \n\t\t      No. telp yg bisa dihubungi.\n\n     Penyebab  mogok   : indikator \t      => starter kenceng => tmbl on/of => klakson msh kuat.\n                                              \t\t      => Starter lemot     => aki\n              \t\t   : mogok ke 2 kalinya      => kemungkinan alternator => GS\n---------------------------------------------------------------------------------------------------------------------------------------------------------\n>> Masalah ban : Nama YBS; Plat kendaraan; PT. mana; KM saat ini; tanyakan permasalahan ban; JENIS DAN UKURAN BAN, berapa ban, lokasi storing; bertemu dengan siapa; nomor yang bisa dihub.\n---------------------------------------------------------------------------------------------------------------------------------------------------------                      \n>> Penyewaan Mobil Baru :   Nama YBS dan perusahaan'nya, berapa unit, jenis kendaraan apa, periode berapa lama, warna kend. untuk daerah mana, menggunakan driver / tidak, No. FAX dan nomor telp, up'nya atas nama siapa. sudah pernah rental di ANJ? jika belum, tau  info ANJ Rent Car dari mana.\n-----------------------------------------------------------------------------------------------------------------------------------------------------------\n>> Ganti Oli :  kilometernya suda berapa?, lokasi storing?; bContact person dan nama yg bs dihub.\n------------------------------------------------------------------------------------------------------------------------------------------------------------\n\t            \t\t\t\t\t- lokasi kecelakaan\n\t         \t\t\t\t\t- kerusakan / kondisi kendaraan\n                                                 masih dilokasi kejadian :\t- ditabrak / menabrak\n\t         \t                     /   \t\t\t- ada pihak ke 3 / tidak\n  \t              \t                    /  \t\t\t- jika ada pihak ke3 ; nama,nomor tlp yg bs dihub.\n\t           - nama             /   \t\t\t- kronologi kejadian. \n>>Kecelakaan : - plat kend     /                    \t\t===========================================\n\t           - PT mana     \\                                                 \t- lokasi kecelakaan\n                              - no. telp.        \\    \t\t\t- lokasi kendaraan saat ini\n                     \t                    \\        \t\t\t- kerusakan/kondisi kendaraan\n\t                                        \\ \t\t\t- ditabrak / menabrak\n\t\t            tidak dilokasi kejadian :\t\t- ada pihak ke 3 / tidak\n\t\t \t\t\t\t- jika ada pihak ke 3 : nama,no tlp yg bs dihub/KTP\n\t\t\t\t\t\t- kronologi kejadian\n\n--------------------------------------------------------------------------------------------------------------------------------------------------------\n>> Kasus barang tertinggal \t=> Tanya tertinggal di kendaraan ORI atau GS..?? kemudian tanya plat kend. ORI / \t\t\t\t\t\t      GSnya. Kemudian jenis barang yang tertinggal, nama & nomor yg bs dihub. \n\t\t\t\t\t\n===============================================================================================\nCONTOH CONTOH KASUS DAN TINDAKAN YG DILAKUKAN...... :\n\n>> Keluhan tentang  mesin \t=> arahkan ke bengkel.\n                                        \t=> kita lakukan storing.\n                                  \t\t=> pilihan terakhir kita GS. (usahakan agar GS tidak terjadi).\n\n>> Temp. Naik \t\t: Disertai AC panas \t=> kirim storing.\n                       \t\t: tidak disertai AC panas   => harus GS.\n                        \t\t: AC nyala,tp kipas mati    => kirim storing.\n\n>> AC \t\t\t:  Kadang – kadang\t=> tanyakan pada kecepatan berapa?\n          \t\t\t:  AC mati total \t=> disarankan boleh jalan dengan kecepatan dibawah 60 km/jam.\n\n>> Kf hasil storing \t\t:  apakah sudah dilakukan storing? dan untuk hasilnya? Jika ada masalah lagi ? baik \t\t\t   \t   \t\t\t                   kami akan segera menindak lanjutinya.\n\n>> Penjadwalan storing\t:  kami akan koordinasikan dgn tim storing kami. Apabila sdh ada jadwal, kami akan \t\t        \t\t   \t\t\t   menghubungi bapak kembali.\n\n>> Pengembalian mobil ori    \t:  - lokasi pengembalian?\t    - bertemu siapa?\n\t\t                   - nomor yg bisa dihub?\t    - waktu / jam?           \n\n>> Proses return ori\t:  - kirim tiket ke dept.Drive\t     - email dan sms CSO\n\t\t\t   - email ke dhani.\t\t     - tulis di excel.\n\n*Bengkel cibitung     \t  : lokasi storing hanya melayani bagian timur. Cth : pd. Gede,cibitung, karawang.\n*Bengkel cengkareng  : semua wilayah kecuali arah timur.\n\n*Emg. Storing\t : 081511231510 / 081310457706\n\n*Emg. Kendaraan\t : 53673035 / 58304396    NEW >> 58304396\n\n*Bgkl cibitung\t : 02189982288 / fax 89982233 \n  pak edi. \t\t   jl sumatra D4. kawasan industri MM 2100. => bisa uji emisi.\n\n*Bgkl cengkareng\t : 58353999/ fax 58358181.\n\t\t   jl. Bojong Raya. Dekat smu Viani. / Minggu off. \n\n*Unit laka \t: 08161440682 / 081380001562. pic Ronny.\n\n*Kend. Luar Kota\t: 085693031668\n\n>> Kategori informasi keluhan yg bersifat urgent :  ( hub. Emergency kend. )\n\n- Mogok di perjalanan dan tidak bisa dinyalakan.               - Kecelakaan\n- Ancaman ( costumer marah dan mengancam )               - Keamanan / safety ( terkait dgn mslh  rem / kaca pecah )\n- tlpn  dr costumer yg sama lebih dr 3 x. dlm sehari.          - AC mati.\n- over heat\t\t\t\t      - mobil hilang.\n====================\nPIC WAHYU// PT.MURINDA (085880622220/ 98612530)\n======================================================================\n");
         jTextArea12.setWrapStyleWord(true);
         jTextArea12.setBorder(null);
         jTextArea12.setOpaque(false);
         jScrollPane46.setViewportView(jTextArea12);
 
         jPanel24.add(jScrollPane46);
         jScrollPane46.setBounds(0, 0, 970, 410);
 
         pnlinf.addTab("Phonebook & Procedures", jPanel24);
 
         jtab.addTab("Information", pnlinf);
 
         jPanel2.add(jtab);
         jtab.setBounds(6, 8, 980, 470);
 
         jPanel2.setBounds(10, 170, 990, 480);
         jdp.add(jPanel2, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         pnlou.setBackground(new java.awt.Color(255, 255, 255));
         pnlou.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnlou.setLayout(null);
 
         tblout.setAutoCreateRowSorter(true);
         tblout.setFont(tblout.getFont().deriveFont((float)11));
         tblout.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null}
             },
             new String [] {
                 "Ticket No.", "Status", "Category", "Assign Dept.", "Assign user", "Customer", "Phone Number", "User", "No.Plat", "Type", "Driver", "Phone", "id"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false, false, false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblout.setRowHeight(20);
         tblout.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tbloutMouseClicked(evt);
             }
         });
         jScrollPane12.setViewportView(tblout);
 
         pnlou.add(jScrollPane12);
         jScrollPane12.setBounds(10, 40, 950, 180);
 
         tblticconf.setAutoCreateRowSorter(true);
         tblticconf.setFont(tblticconf.getFont().deriveFont((float)11));
         tblticconf.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null, null, null, null, null, null}
             },
             new String [] {
                 "Ticket No.", "Confirm Username", "Status", "Category", "Assign Dept.", "Assign user", "Customer", "Phone Number", "User", "No.Plat", "Type", "Driver", "Phone", "id"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false, false, false, false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblticconf.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         tblticconf.setRowHeight(20);
         tblticconf.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblticconfMouseClicked(evt);
             }
         });
         jScrollPane17.setViewportView(tblticconf);
 
         pnlou.add(jScrollPane17);
         jScrollPane17.setBounds(10, 230, 950, 200);
 
         jLabel59.setFont(jLabel59.getFont().deriveFont((float)11));
         jLabel59.setText("Open From");
         pnlou.add(jLabel59);
         jLabel59.setBounds(10, 10, 100, 10);
 
         dctic7.setDateFormatString("dd/MM/yyyy");
         dctic7.setFont(dctic7.getFont().deriveFont((float)11));
         pnlou.add(dctic7);
         dctic7.setBounds(10, 20, 120, 24);
 
         dctic8.setDateFormatString("dd/MM/yyyy");
         dctic8.setFont(dctic8.getFont().deriveFont((float)11));
         pnlou.add(dctic8);
         dctic8.setBounds(130, 20, 120, 24);
 
         jLabel60.setFont(jLabel60.getFont().deriveFont((float)11));
         jLabel60.setText("Until");
         pnlou.add(jLabel60);
         jLabel60.setBounds(130, 10, 100, 10);
 
         jLabel61.setFont(jLabel61.getFont().deriveFont((float)11));
         jLabel61.setText("Agen");
         pnlou.add(jLabel61);
         jLabel61.setBounds(260, 10, 100, 10);
 
         btnoutsrch.setFont(btnoutsrch.getFont().deriveFont(btnoutsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnoutsrch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnoutsrch.setText("Search By");
         btnoutsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnoutsrchActionPerformed(evt);
             }
         });
         pnlou.add(btnoutsrch);
         btnoutsrch.setBounds(370, 20, 120, 24);
 
         cbagenou.setFont(cbagenou.getFont().deriveFont((float)11));
         cbagenou.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--", "aan", "ramos", "john", "yusnita", "tri", "fitri", "mariana", "mitha", "dessy", "andrianto", "nurdin", "david", "yudho", "favel", "feronika", "oktaviani", "rudi" }));
         pnlou.add(cbagenou);
         cbagenou.setBounds(260, 20, 100, 24);
 
         lblcaloutcount.setFont(lblcaloutcount.getFont().deriveFont((float)11));
         lblcaloutcount.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         pnlou.add(lblcaloutcount);
         lblcaloutcount.setBounds(880, 0, 40, 10);
 
         lblrepticcount11.setFont(lblrepticcount11.getFont().deriveFont((float)11));
         lblrepticcount11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblrepticcount11.setText("list");
         pnlou.add(lblrepticcount11);
         lblrepticcount11.setBounds(920, 0, 40, 10);
 
         pnlou.setBounds(0, 0, 0, 0);
         jdp.add(pnlou, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         panelfax.setBackground(new java.awt.Color(255, 255, 255));
         panelfax.setFont(panelfax.getFont().deriveFont((float)10));
         panelfax.setOpaque(true);
         panelfax.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 panelfaxMouseClicked(evt);
             }
         });
 
         jPanel14.setBackground(new java.awt.Color(255, 255, 255));
         jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel14.setLayout(null);
 
         tblfin.setAutoCreateRowSorter(true);
         tblfin.setFont(tblfin.getFont().deriveFont((float)11));
         tblfin.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null}
             },
             new String [] {
                 "From", "Subject", "Date", "Read", "Status"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblfin.setRowHeight(20);
         tblfin.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblfinMouseClicked(evt);
             }
         });
         jScrollPane28.setViewportView(tblfin);
 
         jPanel14.add(jScrollPane28);
         jScrollPane28.setBounds(10, 40, 950, 140);
 
         jLabel65.setFont(jLabel65.getFont().deriveFont((float)11));
         jLabel65.setText("From :");
         jPanel14.add(jLabel65);
         jLabel65.setBounds(10, 10, 100, 10);
 
         dtfi.setDateFormatString("dd/MM/yyyy");
         dtfi.setFont(dtfi.getFont().deriveFont((float)11));
         jPanel14.add(dtfi);
         dtfi.setBounds(10, 20, 120, 24);
 
         jLabel69.setFont(jLabel69.getFont().deriveFont((float)11));
         jLabel69.setText("Until :");
         jPanel14.add(jLabel69);
         jLabel69.setBounds(130, 10, 100, 10);
 
         dtfi1.setDateFormatString("dd/MM/yyyy");
         dtfi1.setFont(dtfi1.getFont().deriveFont((float)11));
         jPanel14.add(dtfi1);
         dtfi1.setBounds(130, 20, 120, 24);
 
         btnfinsrch.setFont(btnfinsrch.getFont().deriveFont(btnfinsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnfinsrch.setText("Search");
         btnfinsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnfinsrchActionPerformed(evt);
             }
         });
         jPanel14.add(btnfinsrch);
         btnfinsrch.setBounds(260, 20, 100, 24);
 
         lblview.setBackground(new java.awt.Color(204, 204, 255));
         lblview.setOpaque(true);
         jScrollPane29.setViewportView(lblview);
 
         jPanel14.add(jScrollPane29);
         jScrollPane29.setBounds(10, 180, 950, 220);
 
         jLabel90.setFont(jLabel90.getFont().deriveFont((float)11));
         jLabel90.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel90.setText("Cust. Company");
         jPanel14.add(jLabel90);
         jLabel90.setBounds(670, 10, 90, 10);
 
         cbcust2.setFont(cbcust2.getFont().deriveFont((float)11));
         cbcust2.setMaximumRowCount(9);
         cbcust2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Non-customer", "Customer-Driver", "Customer-User", "Customer-PIC", "Customer-Other", "Internal-ANJ", "Internal-CC", "Internal-CSO", "Internal-Driver", "Internal-Other" }));
         cbcust2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbcust2ActionPerformed(evt);
             }
         });
         jPanel14.add(cbcust2);
         cbcust2.setBounds(670, 20, 200, 24);
 
         btncussaveFax.setFont(btncussaveFax.getFont().deriveFont(btncussaveFax.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btncussaveFax.setText("Save");
         btncussaveFax.setEnabled(false);
         btncussaveFax.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btncussaveFaxActionPerformed(evt);
             }
         });
         jPanel14.add(btncussaveFax);
         btncussaveFax.setBounds(880, 20, 80, 24);
 
         jLabel93.setFont(jLabel93.getFont().deriveFont((float)11));
         jLabel93.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel93.setText("Ticket No");
         jPanel14.add(jLabel93);
         jLabel93.setBounds(530, 10, 60, 10);
 
         txtnoticfax.setFont(txtnoticfax.getFont().deriveFont((float)11));
         jPanel14.add(txtnoticfax);
         txtnoticfax.setBounds(530, 20, 140, 24);
 
         panelfax.addTab("InBox", jPanel14);
 
         jPanel15.setBackground(new java.awt.Color(255, 255, 255));
         jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel15.setLayout(null);
 
         tblfou.setAutoCreateRowSorter(true);
         tblfou.setFont(tblfou.getFont().deriveFont((float)11));
         tblfou.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null}
             },
             new String [] {
                 "To", "Subject", "Date", "Cc", "Status"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblfou.setRowHeight(20);
         tblfou.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblfouMouseClicked(evt);
             }
         });
         jScrollPane30.setViewportView(tblfou);
 
         jPanel15.add(jScrollPane30);
         jScrollPane30.setBounds(10, 40, 950, 140);
 
         jLabel75.setFont(jLabel75.getFont().deriveFont((float)11));
         jLabel75.setText("From :");
         jPanel15.add(jLabel75);
         jLabel75.setBounds(10, 10, 100, 10);
 
         dtfo.setDateFormatString("dd/MM/yyyy");
         dtfo.setFont(dtfo.getFont().deriveFont((float)11));
         jPanel15.add(dtfo);
         dtfo.setBounds(10, 20, 120, 24);
 
         jLabel76.setFont(jLabel76.getFont().deriveFont((float)11));
         jLabel76.setText("Until :");
         jPanel15.add(jLabel76);
         jLabel76.setBounds(130, 10, 100, 10);
 
         dtfo1.setDateFormatString("dd/MM/yyyy");
         dtfo1.setFont(dtfo1.getFont().deriveFont((float)11));
         jPanel15.add(dtfo1);
         dtfo1.setBounds(130, 20, 120, 24);
 
         btnfoutsrch.setFont(btnfoutsrch.getFont().deriveFont(btnfoutsrch.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnfoutsrch.setText("Search");
         btnfoutsrch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnfoutsrchActionPerformed(evt);
             }
         });
         jPanel15.add(btnfoutsrch);
         btnfoutsrch.setBounds(260, 20, 90, 24);
 
         lblview1.setBackground(new java.awt.Color(204, 204, 255));
         lblview1.setOpaque(true);
         jScrollPane31.setViewportView(lblview1);
 
         jPanel15.add(jScrollPane31);
         jScrollPane31.setBounds(10, 180, 950, 220);
 
         panelfax.addTab("OutBox", jPanel15);
 
         panelfax.setBounds(0, 0, 50, 0);
         jdp.add(panelfax, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         pnlHoOu.setBackground(new java.awt.Color(255, 255, 255));
         pnlHoOu.setLayout(null);
 
         tblhourout.setFont(tblhourout.getFont().deriveFont((float)11));
         tblhourout.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane40.setViewportView(tblhourout);
 
         pnlHoOu.add(jScrollPane40);
         jScrollPane40.setBounds(10, 40, 940, 310);
 
         dtho.setDateFormatString("dd/MM/yyyy");
         dtho.setFont(dtho.getFont().deriveFont((float)11));
         pnlHoOu.add(dtho);
         dtho.setBounds(10, 20, 120, 24);
 
         jLabel79.setFont(jLabel79.getFont().deriveFont((float)11));
         jLabel79.setText("Date");
         pnlHoOu.add(jLabel79);
         jLabel79.setBounds(10, 10, 100, 10);
 
         btnho.setFont(btnho.getFont().deriveFont(btnho.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnho.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnho.setText("Refresh");
         btnho.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnhoActionPerformed(evt);
             }
         });
         pnlHoOu.add(btnho);
         btnho.setBounds(150, 20, 115, 24);
 
         btnexportcall2.setFont(btnexportcall2.getFont().deriveFont(btnexportcall2.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportcall2.setText("Export");
         btnexportcall2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportcall2ActionPerformed(evt);
             }
         });
         pnlHoOu.add(btnexportcall2);
         btnexportcall2.setBounds(10, 350, 90, 20);
 
         pnlRepHidden.addTab("Outbound", pnlHoOu);
 
         pnlDayOu.setBackground(new java.awt.Color(255, 255, 255));
         pnlDayOu.setLayout(null);
 
         tbldailyout.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane44.setViewportView(tbldailyout);
 
         pnlDayOu.add(jScrollPane44);
         jScrollPane44.setBounds(10, 40, 940, 310);
 
         dtdo.setDateFormatString("dd/MM/yyyy");
         pnlDayOu.add(dtdo);
         dtdo.setBounds(10, 20, 120, 24);
 
         jLabel83.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         jLabel83.setText("From");
         pnlDayOu.add(jLabel83);
         jLabel83.setBounds(10, 10, 100, 10);
 
         btndo.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         btndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btndo.setText("Refresh");
         btndo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btndoActionPerformed(evt);
             }
         });
         pnlDayOu.add(btndo);
         btndo.setBounds(270, 20, 115, 24);
 
         jLabel85.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         jLabel85.setText("Until");
         pnlDayOu.add(jLabel85);
         jLabel85.setBounds(140, 10, 100, 10);
 
         dtdo1.setDateFormatString("dd/MM/yyyy");
         pnlDayOu.add(dtdo1);
         dtdo1.setBounds(140, 20, 120, 24);
 
         btnexportcall4.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
         btnexportcall4.setText("Export");
         btnexportcall4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportcall4ActionPerformed(evt);
             }
         });
         pnlDayOu.add(btnexportcall4);
         btnexportcall4.setBounds(10, 350, 90, 20);
 
         pnlRepHidden.addTab("Outbound", pnlDayOu);
 
         pnlPerfOu.setBackground(new java.awt.Color(255, 255, 255));
         pnlPerfOu.setLayout(null);
 
         tblperformout.setFont(tblperformout.getFont().deriveFont((float)11));
         tblperformout.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane42.setViewportView(tblperformout);
 
         pnlPerfOu.add(jScrollPane42);
         jScrollPane42.setBounds(10, 40, 940, 310);
 
         dtpo.setDateFormatString("dd/MM/yyyy");
         dtpo.setFont(dtpo.getFont().deriveFont((float)11));
         pnlPerfOu.add(dtpo);
         dtpo.setBounds(10, 20, 120, 24);
 
         btnpo1.setFont(btnpo1.getFont().deriveFont(btnpo1.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnpo1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnpo1.setText("Refresh");
         btnpo1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnpo1ActionPerformed(evt);
             }
         });
         pnlPerfOu.add(btnpo1);
         btnpo1.setBounds(270, 20, 115, 24);
 
         jLabel87.setFont(jLabel87.getFont().deriveFont((float)11));
         jLabel87.setText("Until");
         pnlPerfOu.add(jLabel87);
         jLabel87.setBounds(140, 10, 100, 10);
 
         dtpo1.setDateFormatString("dd/MM/yyyy");
         dtpo1.setFont(dtpo1.getFont().deriveFont((float)11));
         pnlPerfOu.add(dtpo1);
         dtpo1.setBounds(140, 20, 120, 24);
 
         jLabel89.setFont(jLabel89.getFont().deriveFont((float)11));
         jLabel89.setText("From");
         pnlPerfOu.add(jLabel89);
         jLabel89.setBounds(10, 10, 100, 10);
 
         btnexportcall6.setFont(btnexportcall6.getFont().deriveFont(btnexportcall6.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportcall6.setText("Export");
         btnexportcall6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportcall6ActionPerformed(evt);
             }
         });
         pnlPerfOu.add(btnexportcall6);
         btnexportcall6.setBounds(10, 350, 90, 20);
 
         pnlRepHidden.addTab("Outbound", pnlPerfOu);
 
         pnlrepFax.setBackground(new java.awt.Color(255, 255, 255));
         pnlrepFax.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnlrepFax.setLayout(null);
 
         txtfaxfinm.setFont(txtfaxfinm.getFont().deriveFont((float)11));
         pnlrepFax.add(txtfaxfinm);
         txtfaxfinm.setBounds(460, 20, 100, 24);
 
         jLabel33.setFont(jLabel33.getFont().deriveFont((float)11));
         jLabel33.setText("Status");
         pnlrepFax.add(jLabel33);
         jLabel33.setBounds(360, 10, 100, 10);
 
         jLabel34.setFont(jLabel34.getFont().deriveFont((float)11));
         jLabel34.setText("File name");
         pnlrepFax.add(jLabel34);
         jLabel34.setBounds(460, 10, 100, 10);
 
         btnrepfax.setFont(btnrepfax.getFont().deriveFont(btnrepfax.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnrepfax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/1245117595_001_37.png"))); // NOI18N
         btnrepfax.setText("Search Fax");
         btnrepfax.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnrepfaxActionPerformed(evt);
             }
         });
         pnlrepFax.add(btnrepfax);
         btnrepfax.setBounds(670, 20, 140, 24);
 
         jLabel36.setFont(jLabel36.getFont().deriveFont((float)11));
         jLabel36.setText("Username");
         pnlrepFax.add(jLabel36);
         jLabel36.setBounds(560, 10, 100, 10);
 
         jLabel41.setFont(jLabel41.getFont().deriveFont((float)11));
         jLabel41.setText("Open From");
         pnlrepFax.add(jLabel41);
         jLabel41.setBounds(10, 10, 100, 10);
 
         dcfax1.setDateFormatString("dd/MM/yyyy");
         dcfax1.setFont(dcfax1.getFont().deriveFont((float)11));
         pnlrepFax.add(dcfax1);
         dcfax1.setBounds(10, 20, 120, 24);
 
         jLabel42.setFont(jLabel42.getFont().deriveFont((float)11));
         jLabel42.setText("Until");
         pnlrepFax.add(jLabel42);
         jLabel42.setBounds(130, 10, 100, 10);
 
         dcfax2.setDateFormatString("dd/MM/yyyy");
         dcfax2.setFont(dcfax2.getFont().deriveFont((float)11));
         pnlrepFax.add(dcfax2);
         dcfax2.setBounds(130, 20, 120, 24);
 
         cbstatusrepfax.setFont(cbstatusrepfax.getFont().deriveFont((float)11));
         pnlrepFax.add(cbstatusrepfax);
         cbstatusrepfax.setBounds(360, 20, 100, 24);
 
         btnexportmail1.setFont(btnexportmail1.getFont().deriveFont(btnexportmail1.getFont().getStyle() | java.awt.Font.BOLD, 11));
         btnexportmail1.setText("Export");
         btnexportmail1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnexportmail1ActionPerformed(evt);
             }
         });
         pnlrepFax.add(btnexportmail1);
         btnexportmail1.setBounds(10, 380, 90, 20);
 
         cbagenirepfax.setFont(cbagenirepfax.getFont().deriveFont((float)11));
         cbagenirepfax.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--", "aan", "ramos", "john", "yusnita", "tri", "fitri", "mariana", "mitha", "dessy", "andrianto", "nurdin", "david", "yudho", "favel", "feronika", "oktaviani", "rudi" }));
         pnlrepFax.add(cbagenirepfax);
         cbagenirepfax.setBounds(560, 20, 100, 24);
 
         jLabel64.setFont(jLabel64.getFont().deriveFont((float)11));
         jLabel64.setText("Direction");
         pnlrepFax.add(jLabel64);
         jLabel64.setBounds(260, 10, 100, 10);
 
         cbdirfax.setFont(cbdirfax.getFont().deriveFont((float)11));
         cbdirfax.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "INBOUND", "OUTBOUND", "--" }));
         cbdirfax.setSelectedIndex(2);
         pnlrepFax.add(cbdirfax);
         cbdirfax.setBounds(260, 20, 100, 24);
 
         lblrepfaxcount.setFont(lblrepfaxcount.getFont().deriveFont((float)11));
         lblrepfaxcount.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         pnlrepFax.add(lblrepfaxcount);
         lblrepfaxcount.setBounds(880, 0, 40, 10);
 
         lblrepticcount9.setFont(lblrepticcount9.getFont().deriveFont((float)11));
         lblrepticcount9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblrepticcount9.setText("list");
         pnlrepFax.add(lblrepticcount9);
         lblrepticcount9.setBounds(920, 0, 40, 10);
 
         tblrepfax.setFont(tblrepfax.getFont().deriveFont((float)11));
         tblrepfax.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tblrepfax.setRowHeight(20);
         jScrollPane16.setViewportView(tblrepfax);
 
         pnlrepFax.add(jScrollPane16);
         jScrollPane16.setBounds(10, 40, 950, 340);
 
         pnlRepHidden.addTab("Fax", pnlrepFax);
 
         pnlRepHidden.setBounds(0, 0, 64, 0);
         jdp.add(pnlRepHidden, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jdp, javax.swing.GroupLayout.DEFAULT_SIZE, 1010, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jdp, javax.swing.GroupLayout.PREFERRED_SIZE, 723, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(17, 17, 17))
         );
 
         getAccessibleContext().setAccessibleName("MPM RENT");
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnfaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnfaxActionPerformed
         // TODO add your handling code here:
         int z=0;
          try {
             sql = "select * from log_fax " +
                     "where _direction=0 " +
                     "and _status=1 " +
                     "and username='"+ lbluser.getText()+"'";
             rs = jconn.SQLExecuteRS(sql, conn);
             while (rs.next()) {
                 z++;
             }
             if (z!=0){
                 Fax_incoming fax = new Fax_incoming();
                 fax.setVisible(true);
                 Fin.open();
 
             }else{
                 sql="update log_fax set " +
                         "_status=1, " +
                         "username='"+lbluser.getText()+"' " +
                         "where _direction=0 " +
                         "and _status=0 " +
                         "order by rcvd_time limit 1";
                 jconn.SQLExecute(sql, conn);
                 // cek rebutan
                 sql1="select * from log_fax " +
                     "where _direction=0 " +
                     "and _status=1 " +
                     "and username='"+ lbluser.getText()+"'";
                 rs = jconn.SQLExecuteRS(sql1, conn);
                 z=0;
                 while (rs.next()) {
                     z++;
                 }
                 if(z!=0){
                     s = "FAX|UPDATE\r\n";
                     kirimBroad();
                     Fax_incoming fax = new Fax_incoming();
                     fax.setVisible(true);
                     Fin.open();
                 }
             }
 
 
         } catch (SQLException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
 }//GEN-LAST:event_btnfaxActionPerformed
 
     private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
         // TODO add your handling code here:
         if (ticshow==false){
             ticket tic = new ticket();
             tic.setVisible(true);
 
             Tic.newtic=true;
             ticshow=true;
         }
         
     }//GEN-LAST:event_jButton6ActionPerformed
 
     @SuppressWarnings("static-access")
     private void btncallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncallActionPerformed
         // TODO add your handling code here:
         btncall.setEnabled(false);
         btnready.setEnabled(true);
         lblactivity.setText("On line");
         btncall.setDebugGraphicsOptions(v);
         s = "PICKUP\r\n";
         kirimTele();
 //          System.out.print("\nyang ini d PICKUP isi string s = "+ s);
           stop();
           delay();
 
           sql="update log_phone set delay='"+elapsed+"', _callstatus=1 where log_id='"+loid+"'";
           jconn.SQLExecute(sql, conn);
 //                      System.out.print(sql);
           sql3="update user_account set _activity=3, time_activity=CURRENT_TIMESTAMP where username= '" +lbluser.getText()+ "' limit 1";
           jconn.SQLExecute(sql3, conn);
 
         InBoundCall inc = new InBoundCall();
         inc.setVisible(true);
         Inc.cek();
     }//GEN-LAST:event_btncallActionPerformed
 
     private void btnsmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsmsActionPerformed
          // TODO add your handling code here:
         int z=0;
          try {
             sql = "select * from log_sms " +
                     "where _direction=0 " +
                     "and _status=1 " +
                     "and username='"+ lbluser.getText()+"'";
             rs = jconn.SQLExecuteRS(sql, conn);
             while (rs.next()) {
                 z++;
             }
             if (z!=0){
                 Sms_income sms = new Sms_income();
                 sms.setVisible(true);
                 Sin.open();
 
             }else{
                 sql="update log_sms set " +
                         "_status=1, " +
                         "username='"+lbluser.getText()+"' " +
                         "where _direction=0 " +
                         "and _status=0 " +
                         "order by sms_date, sms_time limit 1";
                 jconn.SQLExecute(sql, conn);
                 // cek rebutan
                 sql1="select * from log_sms " +
                     "where _direction=0 " +
                     "and _status=1 " +
                     "and username='"+ lbluser.getText()+"'";
                 rs = jconn.SQLExecuteRS(sql1, conn);
                 z=0;
                 while (rs.next()) {
                     z++;
                 }
                 if(z!=0){
                     s = "SMS|UPDATE\r\n";
                     kirimBroad();
                     Sms_income sms = new Sms_income();
                     sms.setVisible(true);
                     Sin.open();
                 }
             }
 
 
         } catch (SQLException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_btnsmsActionPerformed
 
     private void btnmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnmailActionPerformed
         // TODO add your handling code here:
         int z=0;
          try {
             sql = "select * from log_mail " +
                     "where direction=0 " +
                     "and status=1 " +
                     "and username='"+ lbluser.getText()+"'";
             rs = jconn.SQLExecuteRS(sql, conn);
             while (rs.next()) {
                 z++;
             }
             if (z!=0){
                 Email_incoming mail = new Email_incoming();
                 mail.setVisible(true);
                 Ein.open();
 
             }else{
                 sql="update log_mail set " +
                         "status=1, " +
                         "username='"+lbluser.getText()+"' " +
                         "where direction=0 " +
                         "and status=0 " +
                         "order by mail_date, mail_time limit 1";
                 jconn.SQLExecute(sql, conn);
                 // cek rebutan
                 sql1="select * from log_mail " +
                     "where direction=0 " +
                     "and status=1 " +
                     "and username='"+ lbluser.getText()+"'";
                 rs = jconn.SQLExecuteRS(sql1, conn);
                 z=0;
                 while (rs.next()) {
                     z++;
                 }
                 if(z!=0){
                     s = "MAIL|UPDATE\r\n";
                     kirimBroad();
                     Email_incoming mail = new Email_incoming();
                     mail.setVisible(true);
                     Ein.open();
                 }
             }
 
 
         } catch (SQLException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_btnmailActionPerformed
 
     private void tblticMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblticMouseClicked
         // TODO add your handling code here:
         if(evt.getClickCount()==1){
             btnsenddept.setEnabled(true);
             if (tbltic.getValueAt(tbltic.getSelectedRow(), tbltic.getTableHeader().getColumnModel().getColumnIndex("Details"))==null){
                 txtdetail.setText("");
             }else{
                 txtdetail.setText((String)tbltic.getValueAt(tbltic.getSelectedRow(), tbltic.getTableHeader().getColumnModel().getColumnIndex("Details")));
             }
             if (tbltic.getValueAt(tbltic.getSelectedRow(), tbltic.getTableHeader().getColumnModel().getColumnIndex("Solution"))==null){
                 txtsolution.setText("");
             }else{
                 txtsolution.setText((String)tbltic.getValueAt(tbltic.getSelectedRow(), tbltic.getTableHeader().getColumnModel().getColumnIndex("Solution")));
             }
             
         }
         if(evt.getClickCount()==2){
             if(ticshow==false){                
     //            System.out.print("debugging");
                 ticket tic = new ticket();
                 tic.setVisible(true);
                 Tic.newtic=false;
 
                 Tic.txtnotic.setText((String)tbltic.getValueAt(tbltic.getSelectedRow(), 0));
 //                Tic.ass=((String)tbltic.getValueAt(tbltic.getSelectedRow(),3));
                 Tic.id=Integer.parseInt((String)tbltic.getValueAt(tbltic.getSelectedRow(), tbltic.getTableHeader().getColumnModel().getColumnIndex("Ticket Id")));
     //            System.out.print("isi dari txtnotic"+Tic.txtnotic);
                 System.out.print("isi dari txtnotic"+Tic.id);
                 System.out.print("isi dari ticno"+Tic.ticno);
 
                 Tic.klik();
                 Tic.showcus();
                 ticshow=true;
             }
 
         }
     }//GEN-LAST:event_tblticMouseClicked
 
     private void tblminMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblminMouseClicked
         // TODO add your handling code here:
         int row=0;
         row=tblmin.getSelectedRow();
         if(row>=0){
             txtfrom.setText(tabmin.getValueAt(row,2).toString());
             txtisu.setText(tabmin.getValueAt(row,3).toString());
             txtimsg.setText(tabmin.getValueAt(row,6).toString());
             mailid=tabmin.getValueAt(row,7).toString();
             DefaultListModel listModel = new DefaultListModel();
             jList2.setModel(listModel);
             try{
                 jList2.setModel(listModel);
                 sql1="select filename from mail_attachment where mail_id='"+mailid+"'";
                 rs1=jconn.SQLExecuteRS(sql1,conn);
                 while(rs1.next()){
                     listModel.addElement(rs1.getString("filename").toString());
                 }
             }catch(Exception e){
                 System.out.println(e);
             }
             cuscom1=tabmin.getValueAt(row,8).toString();
             cbcust1.setSelectedItem(cuscom1);
         }else{
             txtfrom.setText("");
             txtisu.setText("");
             txtimsg.setText("");
             cbcust1.setSelectedIndex(-1);
         }
 }//GEN-LAST:event_tblminMouseClicked
 
     private void tblmouMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblmouMouseClicked
         // TODO add your handling code here:
             int row=0;
             row=tblmou.getSelectedRow();
             txtoto.setText(tabmou.getValueAt(row,2).toString());
             txtosu.setText(tabmou.getValueAt(row,4).toString());
             txtomsg.setText(tabmou.getValueAt(row,7).toString());
             if (tabmou.getValueAt(row,8)!=null){
                 txtocc.setText(tabmou.getValueAt(row,8).toString());
             }
             if (tabmou.getValueAt(row,5)!=null){
                 txtidti.setText(tabmou.getValueAt(row,5).toString());
             }
             mailid=tabmou.getValueAt(row,9).toString();
             DefaultListModel listModel1 = new DefaultListModel();
             jList3.setModel(listModel1);
             try{
                 jList3.setModel(listModel1);
                 sql1="select filename from mail_attachment where mail_id='"+mailid+"'";
                 rs1=jconn.SQLExecuteRS(sql1,conn);
                 while(rs1.next()){
                     listModel1.addElement(rs1.getString("filename").toString());
                 }
             }catch(Exception e){
                 System.out.println(e);
             }
 }//GEN-LAST:event_tblmouMouseClicked
 
     private void panelmailMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelmailMouseClicked
         // TODO add your handling code here:
 }//GEN-LAST:event_panelmailMouseClicked
 
     private void btnlogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnlogoutActionPerformed
         // TODO add your handling code here:
         int i=JOptionPane.showConfirmDialog(null, "Really want to Exit..?","Exit",JOptionPane.YES_NO_OPTION);
         if (i==JOptionPane.YES_OPTION){
         String input=JOptionPane.showInputDialog("Fill the reason");
             sql="update user_account set _status=0, _activity=0, host_addr='"+pabx+"',logout_time='"+ld+"',info='"+input+"'where password = md5('"+lblpas.getText()+"') and username= '" +lbluser.getText()+ "' limit 1";
             jconn.SQLExecute(sql, conn);            
 //            System.out.println(sql);
             s = "CLOSE\r\n";
               kirimTele();
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {
                 Logger.getLogger(InBoundCall.class.getName()).log(Level.SEVERE, null, ex);
             }
 //            connectuploder();
             s = "CLOSE\r\n";
              kirimUplo();
 
               cleanUptele();
               cleanUpupload();
               cleanbroad();
             try {
                 conn.close();
             } catch (SQLException ex) {
                 System.err.println(ex.getMessage());
             }
 
             System.exit(0);            
         }
 }//GEN-LAST:event_btnlogoutActionPerformed
 
     private void btnoutboundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnoutboundActionPerformed
         // TODO add your handling code here:
         if(outbound==true){
             OutBound out = new OutBound();
             out.setVisible(true);
         }else{
             s = "REGISTER|"+pabx+"|"+out_ext+"|"+out_ext+"\r\n";
               kirimTele();
               int z=0;
              try {
                  //cek gantung
                 sql = "select * from tickets " +
                         "where confirmed=1 " +
                         "and confirm_username='"+ lbluser.getText()+"'";
                 rs = jconn.SQLExecuteRS(sql, conn);
                 System.out.print("\ncek gantung : "+sql);
                 while (rs.next()) {
                     z++;
                 }
                 System.out.print("\ncek counter : "+z);
                 if (z!=0){
                     OutBound out = new OutBound();
                     out.setVisible(true);
                     Obc.open();
 
                 }else{
                     sql="update tickets set " +
                             "confirmed=1, " +
                             "confirm_username='"+lbluser.getText()+"' " +
                             "where _status=2 and confirm=1 and confirm_by=0 and confirm_username is null and confirmed=0 " +
                             "order by close_date, close_time limit 1";
                     jconn.SQLExecute(sql, conn);
                      // cek rebutan
                     sql1="select * from tickets " +
                         "where confirmed=1 " +
                         "and confirm_username='"+ lbluser.getText()+"'";
                     rs = jconn.SQLExecuteRS(sql1, conn);
                     z=0;
                     while (rs.next()) {
                         z++;
                     }
                     if(z!=0){
                         s = "TICKET|CONFIRMED\r\n";
                         kirimBroad();
                         OutBound out = new OutBound();
                         out.setVisible(true);
                         Obc.open();
                     }
                 }
 
 
             } catch (SQLException ex) {
                 Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
 //        if (c==0){
 //
 //        }else{
 //            c--;
 //            txtcalnoti.setText(String.valueOf(c));
 //        }
         
 }//GEN-LAST:event_btnoutboundActionPerformed
 
 public static String clbk=null;
 public static String cldt=null;
 String tic0;
     private void tblinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblinMouseClicked
         // TODO add your handling code here:
         if(tabin.getRowCount()!=0){
             if(evt.getClickCount()==2&&lblactivity.getText().equals("Disconnected")){
                 if(inshow==false){
                     
                     InBoundCall inc = new InBoundCall();
                     inc.setVisible(true);
                     Inc.loid=null;
     //                clbk=
     //                Date dt3 =(Date) tblin.getValueAt(tblin.getSelectedRow(), 18);
     //                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
     //                tic0 = sdf.format(dt3);
 
                     Inc.ckblank.setEnabled(false);
                     Inc.txtlogid.setText((String)tblin.getValueAt(tblin.getSelectedRow(),13));
                     Inc.loid=String.valueOf((String)tblin.getValueAt(tblin.getSelectedRow(),13));
                     Inc.txtcalnm.setText((String)tblin.getValueAt(tblin.getSelectedRow(), 10));
                     Inc.txtcalnum.setText((String)tblin.getValueAt(tblin.getSelectedRow(),9));
                     Inc.cbcaltype.setSelectedItem((String)tblin.getValueAt(tblin.getSelectedRow(),14));
 
                     Inc.txtnotic.setText((String)tblin.getValueAt(tblin.getSelectedRow(),11));
                     Inc.txtareacom.setText((String)tblin.getValueAt(tblin.getSelectedRow(),15));
                     Inc.lbldurasi.setText((String)tblin.getValueAt(tblin.getSelectedRow(),5));
         //            System.out.print("abis lempar durasi");
                     Inc.blankcall=Integer.parseInt((String)tblin.getValueAt(tblin.getSelectedRow(),8));
     //                Inc.callback=Integer.parseInt((String)tblin.getValueAt(tblin.getSelectedRow(),17));
                     Inc.complaint=Integer.parseInt((String)tblin.getValueAt(tblin.getSelectedRow(),7));
                     Inc.inquiry=Integer.parseInt((String)tblin.getValueAt(tblin.getSelectedRow(),6));
                     Inc.wrongnum=Integer.parseInt((String)tblin.getValueAt(tblin.getSelectedRow(),16));
                     Inc.cbServiceArea.setSelectedItem((String)tblin.getValueAt(tblin.getSelectedRow(),17));
                     Inc.cbInbType.setSelectedItem((String)tblin.getValueAt(tblin.getSelectedRow(),18));
     ////                clbk=((String)tblin.getValueAt(tblin.getSelectedRow(),18));
     //                System.out.print("\nisi dari clbk = "+clbk+"\n");
     //                if(clbk!=null){
     //                    cldt=clbk.substring(8,10)+"/"+clbk.substring(5, 7)+"/"+clbk.substring(0,4);
     //                    System.out.print("\nisi dari cldt = "+cldt+"\n");
     //                    Inc.date.setText(cldt);
     //                    Inc.txtcalbac.setText((String)tblin.getValueAt(tblin.getSelectedRow(),19));
     //                    System.out.print("pas clik 2 kali"+Inc.date);
     //                }
                     Inc.hangup=true;
                     Inc.cek();
                     inshow=true;
                 }else{
                     JOptionPane.showMessageDialog(null,"Close the last form before");
                 }
             }else if(evt.getClickCount()==2){
                 JOptionPane.showMessageDialog(null,"You cannot open history if you registered");
             }
         }
     }//GEN-LAST:event_tblinMouseClicked
 
     private void tbloutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbloutMouseClicked
         // TODO add your handling code here:
 //        if(evt.getClickCount()==1){
 //            tabou.setRowCount(0);
 //            tabelou();
 //        }
 //        if(evt.getClickCount()==2){
 //           OutBound obc = new OutBound();
 //           obc.setVisible(true);
 
 //           Obc.t
 //        }
 }//GEN-LAST:event_tbloutMouseClicked
 
     public static javax.swing.table.DefaultTableModel getDefaultTabelreptic(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Ticket No","Ticket Priority","Case Area","Department","Assigned Deptartment"
                         ,"Status","Open by","Open Date","Open time","Closed Date","Closed time","Sla"
                         ,"Solution","Representatvie Area","Ticket Type","Ticket Category","Ticket Detail Category"
                         ,"Detail Ticket","Cust Name","Cust Address","Cust Code","Awal Kontrak"
                         ,"Akhir Kontrak","CRO Name","Lisence Plate","Tipe Kendaraan","Tahun"
                         ,"Driver Name","F.C.R."}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     private static String tic1,tic2,tic3,tic4,cal1,cal2,cal3,cal4,cal5,cal6,sms1,sms2,mail1,mail2,fax1,fax2;
     private void btnrepticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnrepticActionPerformed
         // TODO add your handling code here:
         int x=0;
         tabreptic.setRowCount(0);
         Date dt1 =dctic1.getDate();
         Date dt2 =dctic2.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         tic1 = sdf.format(dt1);
         tic2= sdf.format(dt2);
         try{
             int row=0;         
             sql="select *, " +
                     "a.dept_name as dept," +
                     "b.dept_name as asdept," +
                     "c.dept_name as opdept," +
                     "d.dept_name as rearea," +
                     " _ticketstatus.data as ticstat, " +
                     "_ticketpriority.data as prior, " +        
                     "_yesno.data as fcr" +        
                     " from tickets" +
                     " left join _department a on tickets.dept_id=a.dept_id" +
                     " left join _department b on tickets.assign_dept=b.dept_id" +
                     " left join _department c on tickets.open_dept=c.dept_id" +
                     " left join _department d on tickets.representative_area_id=d.dept_id" +
                     " left join _ticketstatus on tickets._status=_ticketstatus.code" +
                     " left join _ticketpriority on tickets._priority=_ticketpriority.code" +
                     " left join agreements on tickets.agreement_id=agreements.agreement_id" +
                     " left join units on agreements.unit_code=units.unit_code" +
                     " left join drivers on agreements.driver_code=drivers.driver_code" +
                     " left join branches on '%'+agreements.contract_no+'%' like branches.branch_code" +
                     " left join cro on branches.branch_name=cro.branch_name" +
                     " left join _yesno on tickets.confirm=_yesno.code" +
                     " where open_date between '"+tic1+"' and '"+tic2+"' ";
             condition="";            
             if(!txtticno2.getText().equals("")){
                 condition=condition+" and ticket_no like '%"+txtticno2.getText()+"%'";
             }
             if(!txtplatno1.getText().equals("")){
                 condition=condition+" and units.no_plat like '%"+txtplatno1.getText()+"%'";
             }
             if(!cbdept1.getSelectedItem().equals("--")){
                 condition=condition+" and dept_id = '"+cbdept1.getSelectedIndex()+"'";
             }
             if(!cbticstatus1.getSelectedItem().equals("--")){
                 if(!cbticstatus1.getSelectedItem().equals("CANCEL")){
                     condition=condition+" and _status = '"+cbticstatus1.getSelectedIndex()+"'";
                 }else{
                     condition=condition+" and _status = '-1'";
                 }
             }
             if(!cbFollowUp1.getSelectedItem().equals("--")){
                 condition=condition+" and follow_up = '"+cbFollowUp1.getSelectedItem()+"'";
             }
             if(ckassign1.isSelected()==true){
                 condition=condition+" and assign_dept=0";
             }
             if(ckstoring1.isSelected()==true){
                 condition=condition+" and _storing=1";
             }
             if(cksubmit1.isSelected()==true){
                 condition=condition+" and _submitted=0";
             }
             if(ckFCR1.isSelected()==true){
                 condition=condition+" and confirm=1";
             }
             if(!txtcus1.getText().equals("")){
                 condition=condition+" and cust_name like '%"+txtcus1.getText()+"%'";
             }
             if(!txtdriv1.getText().equals("")){
                 condition=condition+" and drivers.driver_name like '%"+txtdriv1.getText()+"%'";
             }
             if(!cbcate1.getSelectedItem().equals("--")){
                 condition=condition+" and category = '"+cbcate1.getSelectedItem()+"'";
             }
             if(!txtdrivcode1.getText().equals("")){
                 condition=condition+" and drivers.driver_code like '%"+txtdrivcode1.getText()+"%'";
             }
 
             sql=sql+condition+" order by ticket_id";
             rs=jconn.SQLExecuteRS(sql, conn);
             System.out.println(sql);
 
             while(rs.next()){
                 reptic[x]=rs.getString("ticket_no");x++;
                 reptic[x]=rs.getString("prior");x++;
                 reptic[x]=rs.getString("_servicearea");x++;
                 reptic[x]=rs.getString("dept");x++;
                 reptic[x]=rs.getString("asdept");x++;
                 
                 reptic[x]=rs.getString("ticstat");x++;
                 reptic[x]=rs.getString("open_username");x++;
                 reptic[x]=rs.getString("open_date");x++;
                 reptic[x]=rs.getString("open_time");x++;
                 reptic[x]=rs.getString("close_date");x++;
                 reptic[x]=rs.getString("close_time");x++;
                 reptic[x]=rs.getString("sla");x++;
                 
                 reptic[x]=rs.getString("solution");x++;
                 reptic[x]=rs.getString("rearea");x++;
                 reptic[x]=rs.getString("_type");x++;
                 reptic[x]=rs.getString("category");x++;
                 reptic[x]=rs.getString("category_detail");x++;
                 
                 reptic[x]=rs.getString("details");x++;
                 reptic[x]=rs.getString("cust_name");x++;
                 reptic[x]=rs.getString("cust_address");x++;
                 reptic[x]=rs.getString("cust_code");x++;
                 reptic[x]=rs.getString("agreements.awal_kontrak");x++;
                 
                 reptic[x]=rs.getString("agreements.akhir_kontrak");x++;
                 reptic[x]=rs.getString("cro.employee_name");x++;
                 reptic[x]=rs.getString("units.no_plat");x++;
                 reptic[x]=rs.getString("units.tipe");x++;
                 reptic[x]=rs.getString("units.tahun");x++;
                 
                 reptic[x]=rs.getString("drivers.driver_name");x++;
                 reptic[x]=rs.getString("fcr");x++;
 //                reptic[x]=rs.getString(32);x++;
 //                reptic[x]=rs.getString(33);x++;
 //                reptic[x]=rs.getString(34);x++;
 //                reptic[x]=rs.getString(35);x++;
 //                reptic[x]=rs.getString(36);x++;
 //                reptic[x]=rs.getString(37);x++;
 //                reptic[x]=rs.getString(38);x++;
 //                reptic[x]=rs.getString(39);x++;
 //                reptic[x]=rs.getString(40);x++;
 //                reptic[x]=rs.getString(41);x++;
 //                reptic[x]=rs.getString(42);x++;
 //                reptic[x]=rs.getString(43);x++;
 //                reptic[x]=rs.getString(44);x++;
 //                reptic[x]=rs.getString(45);x++;
 //                reptic[x]=rs.getString(46);x++;
 //                reptic[x]=rs.getString("_gs");x++;
 //                reptic[x]=rs.getString("gs_status");x++;
 //                reptic[x]=rs.getString("_gt");x++;
 //                reptic[x]=rs.getString("gt_status");x++;
 //                reptic[x]=rs.getString("_storing");x++;
 //                reptic[x]=rs.getString("storing_status");x++;
 //                reptic[x]=rs.getString("_other");x++;
 //                reptic[x]=rs.getString("other_status");x++;
 //                reptic[x]=rs.getString("r_driverdept");x++;
 //                reptic[x]=rs.getString("r_oprdept");x++;
 //                reptic[x]=rs.getString("r_cso");x++;
 //                reptic[x]=rs.getString("r_custname");x++;
 //                reptic[x]=rs.getString("r_contact");x++;
 //                reptic[x]=rs.getString("r_phoneno");x++;
 //                reptic[x]=rs.getString("r_address");x++;
 //                reptic[x]=rs.getString("r_orgcar_status");x++;
 //                reptic[x]=rs.getString("r_orgcar_plate");x++;
 //                reptic[x]=rs.getString("r_replacecar_status");x++;
 //                reptic[x]=rs.getString("r_replacecar_plate");x++;
 //                reptic[x]=rs.getString("r_deliverydate");x++;
 //                reptic[x]=rs.getString("r_deliverytime");x++;
 //                reptic[x]=rs.getString("r_driverphone");x++;
 //                reptic[x]=rs.getString("r_startdate");x++;
 //                reptic[x]=rs.getString("r_untildate");x++;
 //                reptic[x]=rs.getString("r_memo");x++;
 //                reptic[x]=rs.getString("solution");x++;
 ////                reptic[65]=rs.getString(50);
 //                reptic[x]=rs.getString("open_date");x++;
 //                reptic[x]=rs.getString("open_time");x++;
 //                reptic[x]=rs.getString("open_duration");x++;
 //                reptic[x]=rs.getString("open_username");x++;
 //                reptic[x]=rs.getString("opdept");x++;
 //                reptic[x]=rs.getString("process_date");x++;
 //                reptic[x]=rs.getString("process_time");x++;
 //                reptic[x]=rs.getString("process_duration");x++;
 //                reptic[x]=rs.getString("process_username");x++;
 //                reptic[x]=rs.getString("close_date");x++;
 //                reptic[x]=rs.getString("close_time");x++;
 //                reptic[x]=rs.getString("close_duration");x++;
 //                reptic[x]=rs.getString("close_days");x++;
 //                reptic[x]=rs.getString("close_username");x++;
 //                reptic[x]=rs.getString("_submitted");x++;
 //                reptic[x]=rs.getString("confirm");x++;
 ////                if(Integer.parseInt(rs.getString("confirm_by"))==0){
 ////                    reptic[56]="CONTACT CENTER";
 ////                }else{
 ////                    reptic[56]="CSO";
 ////                }
 ////                reptic[x]=rs.getString("cnfrm");x++;
 ////                reptic[x]=rs.getString("confirm_username");x++;
 ////                reptic[x]=rs.getString("confirmd");x++;
 //                reptic[x]=rs.getString("logphone_id");x++;
 //                reptic[60]=rs.getString(62);
                 tabreptic.addRow(reptic);
                 x=0;
                 row+=1;
             }if(row==0){
 //                JOptionPane.showMessageDialog(null,"Ticket with number ticket "+txtuser.getText()+", categoty "+txtcategory.getText()+", with customer "+txtcustomer.getText()+", with driver "+txtdriver.getText()+" doesn't exsist");
             }
             lblrepticcount.setText(String.valueOf(tabreptic.getRowCount()));            
         }catch(SQLException exc){
             System.err.println(exc.getMessage());
         }
 }//GEN-LAST:event_btnrepticActionPerformed
 
     private void btnrepcalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnrepcalActionPerformed
         // TODO add your handling code here:
         tabrepcal.setRowCount(0);
         Date dt1 =dccal1.getDate();
         Date dt2 =dccal2.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         cal1 = sdf.format(dt1);
         cal2= sdf.format(dt2);
         try{
             int row=0;            
             sql="select log_phone.*, " +
                     "_callstatus.data as cllstatus, " +
                     "shift.data as dshift, " +
                     "_direction.data as ddir, " +
                     "tickets.ticket_no as notic " +
                     "from log_phone " +
                     "left join _callstatus on log_phone._callstatus=_callstatus.code " +
                     "left join tickets on log_phone.ticket_id=tickets.ticket_id " +
                     "join shift on log_phone.shift=shift.code " +
                     "join _direction on log_phone._direction=_direction.code " +
                     "where log_date between '"+cal1+"' and '"+cal2+"' ";
             condition="";
             if(!cbagenirepcal.getSelectedItem().equals("--")){
                 condition=condition+" and username like '%"+cbagenirepcal.getSelectedItem()+"%'";
             }
             if(!cbcaldir.getSelectedItem().equals("--")){
                 condition=condition+" and _direction = '"+cbcaldir.getSelectedIndex()+"'";
             }
             if(!cbcaltyperepcal.getSelectedItem().equals("--")){
                 condition=condition+" and caller_type like '%"+cbcaltyperepcal.getSelectedItem()+"%'";
             }
             if(!cbcalstat.getSelectedItem().equals("--")){
                 if(cbcalstat.getSelectedItem().equals("PHANTOM")){
                     condition=condition+" and _callstatus = '-1'";
                 }else{
                     condition=condition+" and _callstatus = '"+cbcalstat.getSelectedIndex()+"'";
                 }
             }
 
             sql=sql+condition+" order by log_id";
             rs=jconn.SQLExecuteRS(sql, conn);
 //            System.out.println(sql);
 
             while(rs.next()){
 //                repcal[0]=rs.getString(1);
                 repcal[1]=rs.getString(2);
                 repcal[2]=rs.getString(3);
                 repcal[3]=rs.getString(4);
                 repcal[4]=rs.getString("dshift");
                 repcal[5]=rs.getString(6);
                 repcal[6]=rs.getString(7);
                 repcal[7]=rs.getString("ddir");
                 repcal[8]=rs.getString("cllstatus");
                 repcal[9]=rs.getString(10);
                 repcal[10]=rs.getString(11);
                 repcal[11]=rs.getString(12);
                 repcal[12]=rs.getString(13);
                 repcal[13]=rs.getString(14);
                 repcal[14]=rs.getString(15);
                 repcal[15]=rs.getString(16);
                 repcal[16]=rs.getString(17);
                 repcal[17]=rs.getString(18);
                 repcal[18]=rs.getString(19);
                 repcal[19]=rs.getString(20);
                 repcal[20]=rs.getString(21);
                 repcal[21]=rs.getString(22);
                 repcal[22]=rs.getString(23);
 //                repcal[23]=rs.getString(24);
 //                System.out.print("\nisi kolom 24"+repcal[23]);
 //                repcal[24]=rs.getString(25);
 //                System.out.print("\nisi kolom 25"+repcal[24]);
                 repcal[23]=rs.getString(26);
                 repcal[24]=rs.getString(27);
                 repcal[25]=rs.getString(28);
                 repcal[26]=rs.getString(29);
                 repcal[27]=rs.getString(30);
                 repcal[28]=rs.getString("notic");
 //                repcal[25]=rs.getString(26);
 //                repcal[26]=rs.getString(27);
 //                repcal[27]=rs.getString(28);
 //                repcal[28]=rs.getString(29);
                 repcal[29]=rs.getString("cust_name");
                 repcal[30]=rs.getString("inbound_type");
                 tabrepcal.addRow(repcal);
                 row+=1;
             }if(row==0){
 //                JOptionPane.showMessageDialog(null,"Ticket with number ticket "+txtuser.getText()+", categoty "+txtcategory.getText()+", with customer "+txtcustomer.getText()+", with driver "+txtdriver.getText()+" doesn't exsist");
             }
             lblrepcalcount.setText(String.valueOf(tabrepcal.getRowCount()));
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
 }//GEN-LAST:event_btnrepcalActionPerformed
 
     private void btnrepsmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnrepsmsActionPerformed
         // TODO add your handling code here:
         tabrepsms.setRowCount(0);
         Date dt1 =dcsms1.getDate();
         Date dt2 =dcsms2.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         sms1 = sdf.format(dt1);
         sms2= sdf.format(dt2);
         try{
             int row=0;            
              sql="select log_sms.* , " +
                      "_direction.data as dir," +
                      " tickets.ticket_no as notic" +
                      ",case log_sms._direction" +
                      " when 0 then (select rcvd_status.data from rcvd_status where code=log_sms._status) " +
                      " when 1 then (select send_status.data from send_status where code=log_sms._status)" +
                      " end stt" +
                      " from log_sms" +
                      " join _direction on log_sms._direction=_direction.code" +
                      " left join tickets on log_sms.ticket_id=tickets.ticket_id" +
                      " where sms_date between '"+sms1+"' and '"+sms2+"'";
             condition="";
             if(!cbagenirepcal1.getSelectedItem().equals("--")){
                 condition=condition+" and username like '%"+cbagenirepcal1.getSelectedItem()+"%'";
             }
             if(!cbdirrepsms.getSelectedItem().equals("--")){
                 condition=condition+" and _direction = '"+cbdirrepsms.getSelectedIndex()+"'";
             }
             if(!txtsmsstat.getText().equals("")){
                 condition=condition+" and _status like '%"+txtsmsstat.getText()+"%'";
             }
             if(!txtsmsticid.getText().equals("")){
                 condition=condition+" and ticket_no = '"+txtsmsticid.getText()+"'";
             }
 
             sql=sql+condition+" order by sms_id";
             rs=jconn.SQLExecuteRS(sql, conn);
 //            System.out.println(sql);
 
             while(rs.next()){
                 repsms[0]=rs.getString(1);
                 repsms[1]=rs.getString(2);
                 repsms[2]=rs.getString(3);
                 repsms[3]=rs.getString(4);
                 repsms[4]=rs.getString("stt");
                 repsms[5]=rs.getString("dir");
                 repsms[6]=rs.getString(8);
                 repsms[7]=rs.getString(9);
                 repsms[8]=rs.getString(10);
                 repsms[9]=rs.getString("notic");
                 repsms[10]=rs.getString(13);
                 repsms[11]=rs.getString("direction_type");
                 repsms[12]=rs.getString("cust_name");
                 tabrepsms.addRow(repsms);
                 row+=1;
             }if(row==0){
 //                JOptionPane.showMessageDialog(null,"Sms with number ticket "+txtuser.getText()+", categoty "+txtcategory.getText()+", with customer "+txtcustomer.getText()+", with driver "+txtdriver.getText()+" dosen't exsist");
             }
             lblrepsmscount.setText(String.valueOf(tabrepsms.getRowCount()));
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
 }//GEN-LAST:event_btnrepsmsActionPerformed
 
     private void btnrepmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnrepmailActionPerformed
         // TODO add your handling code here:
         tabrepmail.setRowCount(0);
         Date dt1 =dcmail1.getDate();
         Date dt2 =dcmail2.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         mail1 = sdf.format(dt1);
         mail2= sdf.format(dt2);
         try{
             int row=0;
             
                 sql="select log_mail.*, _direction.data as dir" +
                         ", tickets.ticket_no as notic" +
                         ",case log_mail.direction" +
                         " when 0 then (select rcvd_status.data from rcvd_status where code=log_mail.status) " +
                         " when 1 then (select send_status.data from send_status where code=log_mail.status)" +
                         " end stt" +
                         " from log_mail" +
                         " join _direction on log_mail.direction=_direction.code" +
                         " left join tickets on log_mail.ticket_id=tickets.ticket_id" +
                         " where mail_id is not null and mail_date between '"+mail1+"' and '"+mail2+"'";
             condition="";
             if(!cbagenrepmail.getSelectedItem().equals("--")){
                 condition=condition+" and username like '%"+cbagenrepmail.getSelectedItem()+"%'";
             }
             if(!cbdirmail.getSelectedItem().equals("--")){
                 condition=condition+" and direction = '"+cbdirmail.getSelectedIndex()+"'";
             }
             if(!txtmailsub.getText().equals("")){
                 condition=condition+" and mail_subject like '%"+txtmailsub.getText()+"%'";
             }
             if(!txtmailticid.getText().equals("")){
                 condition=condition+" and ticket_no = '"+txtmailticid.getText()+"'";
             }
 
             sql=sql+condition+" GROUP by mail_id order by mail_id";
             rs=jconn.SQLExecuteRS(sql, conn);
             System.out.println(sql);
 
             while(rs.next()){
                 repmail[0]=rs.getString(1);
                 repmail[1]=rs.getString(2);
                 repmail[2]=rs.getString(3);
                 repmail[3]=rs.getString(4);
                 repmail[4]=rs.getString(5);
                 repmail[5]=rs.getString(6);
                 repmail[6]=rs.getString(7);
                 repmail[7]=rs.getString(8);
                 repmail[8]=rs.getString(9);
                 repmail[9]=rs.getString(10);
                 repmail[10]=rs.getString("stt");
                 repmail[11]=rs.getString("notic");
                 repmail[12]=rs.getString("dir");
                 repmail[13]=rs.getString("direction_type");
                 repmail[14]=rs.getString("cust_name");
                 tabrepmail.addRow(repmail);
                 row+=1;
             }if(row==0){
 //                JOptionPane.showMessageDialog(null,"Mail with number subject "+txtmailsub.getText()+", username "+txtmailusr.getText()+", with direction "+txtmaildir.getText()+", with ticekt id "+txtmailticid.getText()+" dosen't exsist");
             }
             lblrepmailcount.setText(String.valueOf(tabrepmail.getRowCount()));
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
 }//GEN-LAST:event_btnrepmailActionPerformed
 
     private void btnrepfaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnrepfaxActionPerformed
         // TODO add your handling code here:
         tabrepfax.setRowCount(0);
         Date dt1 =dcfax1.getDate();
         Date dt2 =dcfax2.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
         SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
         fax1 = sdf.format(dt1);
         fax2= sdf1.format(dt2);
         sql3="update log_fax set sent_time=null where sent_time like '0000%'";
         jconn.SQLExecute(sql3,conn);
         try{
             int row=0;
             sql="select log_fax.* , " +
                      "_direction.data as dir," +
                      " tickets.ticket_no as notic" +
                      ",case log_fax._direction" +
                      " when 0 then (select rcvd_status.data from rcvd_status where code=log_fax._status) " +
                      " when 1 then (select send_status.data from send_status where code=log_fax._status)" +
                      " end stt" +
                      " from log_fax" +
                      " join _direction on log_fax._direction=_direction.code" +
                      " left join tickets on log_fax.ticket_id=tickets.ticket_id" +
                      " where (sent_time between '"+fax1+"' and '"+fax2+"' or rcvd_time between '"+fax1+"' and '"+fax2+"')";
             condition="";
             if(!cbagenirepfax.getSelectedItem().equals("--")){
                 condition=condition+" and username like '%"+cbagenirepfax.getSelectedItem()+"%'";
             }
             if(!cbdirfax.getSelectedItem().equals("--")){
                 condition=condition+" and _direction = '"+cbdirfax.getSelectedIndex()+"'";
             }
             if(cbstatusrepfax.getSelectedIndex()!=-1){
                 condition=condition+" and _status like '%"+cbstatusrepfax.getSelectedIndex()+"%'";
             }
             if(!txtfaxfinm.getText().equals("")){
                 condition=condition+" and doc_name like '%"+txtfaxfinm.getText()+"%'";
             }
 
             sql=sql+condition+" order by fax_id";
             rs=jconn.SQLExecuteRS(sql, conn);
             System.out.println(sql);
 
             while(rs.next()){
                 repfax[0]=rs.getString(1);
                 repfax[1]=rs.getString(2);
                 repfax[2]=rs.getString(3);
                 repfax[3]=rs.getString(4);
                 repfax[4]=rs.getString(5);
                 repfax[5]=rs.getString(6);
                 repfax[6]=rs.getString(7);
                 repfax[7]=rs.getString("stt");
                 repfax[8]=rs.getString("dir");
                 repfax[9]=rs.getString("notic");
                 repfax[10]=rs.getString("cust_name");
                 tabrepfax.addRow(repfax);
                 row+=1;
             }if(row==0){
                 JOptionPane.showMessageDialog(null,"Data Fax doesn't exsist");
             }
             lblrepfaxcount.setText(String.valueOf(tabrepfax.getRowCount()));
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
 }//GEN-LAST:event_btnrepfaxActionPerformed
 
     private void tblrepticMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblrepticMouseClicked
         // TODO add your handling code here:
         if(evt.getClickCount()==2){
             History hic = new History();
             hic.setVisible(true);
 
             Hic.no=Integer.parseInt((String)tblreptic.getValueAt(tblreptic.getSelectedRow(),0));
             Hic.klik2();
         }
     }//GEN-LAST:event_tblrepticMouseClicked
 
     private void txtcalnotiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtcalnotiMouseClicked
         // TODO add your handling code here:
 
     }//GEN-LAST:event_txtcalnotiMouseClicked
 
     private void tblticconfMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblticconfMouseClicked
         // TODO add your handling code here:
         if (evt.getClickCount()==2){
             OutBound out = new OutBound();
             out.setVisible(true);
 
             Obc.txtnotic.setText((String)tblticconf.getValueAt(tblticconf.getSelectedRow(), 0));
             Obc.txtnotic1.setText((String)tblticconf.getValueAt(tblticconf.getSelectedRow(), 12));
             
             Obc.klik2();
         }
 }//GEN-LAST:event_tblticconfMouseClicked
 
     private void tblactMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblactMouseEntered
         // TODO add your handling code here:
         inbo.start();
     }//GEN-LAST:event_tblactMouseEntered
 
     private void tblactMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblactMouseExited
         // TODO add your handling code here:
         inbo.stop();
     }//GEN-LAST:event_tblactMouseExited
 
     private void txtisuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtisuActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_txtisuActionPerformed
 
     private void btnsmsinsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsmsinsrchActionPerformed
         // TODO add your handling code here:
         tabelsin();
 }//GEN-LAST:event_btnsmsinsrchActionPerformed
 
     private void btnsmsoutsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsmsoutsrchActionPerformed
         // TODO add your handling code here:
         tabelsou();
 }//GEN-LAST:event_btnsmsoutsrchActionPerformed
 
     private void btnmailinsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnmailinsrchActionPerformed
         // TODO add your handling code here:
         tabelmin();
 }//GEN-LAST:event_btnmailinsrchActionPerformed
 
     private void btnmailoutsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnmailoutsrchActionPerformed
         // TODO add your handling code here:
         tabelmou();
 }//GEN-LAST:event_btnmailoutsrchActionPerformed
 
     private void btnexportticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportticActionPerformed
         // TODO add your handling code here:
         tabex=tabreptic;
         createexcel();
 }//GEN-LAST:event_btnexportticActionPerformed
 public static String cuscom,cuscom1,cuscom2,smsid,mailid,faxid;
     private void tblsinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblsinMouseClicked
         // TODO add your handling code here:
         int row=0;
         row=tblsin.getSelectedRow();
         if(row>=0){
             txtfrom2.setText(tblsin.getValueAt(row,1).toString());
             txtimsg2.setText(tblsin.getValueAt(row,3).toString());
             smsid=tblsin.getValueAt(row,5).toString();
             cuscom=tblsin.getValueAt(row,6).toString();
             cbcust.setSelectedItem(cuscom);
         }else{
             txtfrom2.setText("");
             txtimsg2.setText("");
             cbcust.setSelectedIndex(-1);
         }
     }//GEN-LAST:event_tblsinMouseClicked
 
     private void tblsouMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblsouMouseClicked
         // TODO add your handling code here:
         int row=0;
         row=tblsou.getSelectedRow();
         if(row>=0){
             txtfrom1.setText(tblsou.getValueAt(row,1).toString());
             txtimsg1.setText(tblsou.getValueAt(row,3).toString());
         }else{
             txtfrom1.setText("");
             txtimsg1.setText("");
         }
     }//GEN-LAST:event_tblsouMouseClicked
     
     private void btnticsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnticsrchActionPerformed
         // TODO add your handling code here:
         tabeltic();
 }//GEN-LAST:event_btnticsrchActionPerformed
 
 
     private void btninsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btninsrchActionPerformed
         // TODO add your handling code here:
         tabelin();
 }//GEN-LAST:event_btninsrchActionPerformed
      
     private void btnoutsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnoutsrchActionPerformed
         // TODO add your handling code here:
         tabelou();
 }//GEN-LAST:event_btnoutsrchActionPerformed
 
     private void btnexportcallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportcallActionPerformed
         // TODO add your handling code here:
         tabex=tabrepcal;
         createexcel();
 }//GEN-LAST:event_btnexportcallActionPerformed
 
     private void btnexportsmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportsmsActionPerformed
         // TODO add your handling code here:
         tabex=tabrepsms;
         createexcel();
 }//GEN-LAST:event_btnexportsmsActionPerformed
 
     private void btnexportmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportmailActionPerformed
         // TODO add your handling code here:
         tabex=tabrepmail;
         createexcel();
 }//GEN-LAST:event_btnexportmailActionPerformed
 
     private void btnsenddeptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsenddeptActionPerformed
         // TODO add your handling code here:
         if (asshow==false){
             Asdept asd = new Asdept();
             asd.setVisible(true);
 
             Asd.id=Integer.parseInt((String)tbltic.getValueAt(tbltic.getSelectedRow(), tbltic.getTableHeader().getColumnModel().getColumnIndex("Ticket Id")));
             Asd.notic=Integer.parseInt((String)tbltic.getValueAt(tbltic.getSelectedRow(), 0));
 
             btnsenddept.setEnabled(false);
             asshow=true;
         }        
 }//GEN-LAST:event_btnsenddeptActionPerformed
 
     private void cktglActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cktglActionPerformed
         // TODO add your handling code here:
 }//GEN-LAST:event_cktglActionPerformed
 
     private void txtmailnotiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtmailnotiActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_txtmailnotiActionPerformed
 
     private void cbcalstatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbcalstatActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbcalstatActionPerformed
 
     private void cbdeptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbdeptActionPerformed
         // TODO add your handling code here:
 //        if(!cbdept.getSelectedItem().equals("--")){            
 //            cbcate.removeAllItems();
 //            cbcate.addItem("--");
 //            try {
 //                sql="select category_name from _ticketcategory where dept_id='"+cbdept.getSelectedIndex()+"'";
 //                rs1=jconn.SQLExecuteRS(sql,conn);
 //                while(rs1.next()){
 //                    cbcate.addItem(rs1.getString(1));
 //                }
 //                cbcate.setEnabled(true);
 //            } catch (SQLException ex) {
 //                Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
 //            }
 //        }else{
 //            cbcate.removeAllItems();
 //            cbcate.addItem("--");
 //            cbcate.setEnabled(false);
 //        }
 }//GEN-LAST:event_cbdeptActionPerformed
 
     private void btnexportmail1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportmail1ActionPerformed
         // TODO add your handling code here:
         tabex=tabrepfax;
         createexcel();
     }//GEN-LAST:event_btnexportmail1ActionPerformed
 
     private void tblfinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblfinMouseClicked
         // TODO add your handling code here:
         if(evt.getClickCount()==1){
             int row=0;
             row=tblfin.getSelectedRow();
             lblview.setIcon(new ImageIcon("Z:/localhost/inbox/"+tabfin.getValueAt(row,1).toString()+""));
             faxid=tabfin.getValueAt(row,4).toString();
             cuscom2=tabfin.getValueAt(row,5).toString();
         }
 
         if(evt.getClickCount()==2){
 //            Tampil1();
         }
 }//GEN-LAST:event_tblfinMouseClicked
 
     private void btnfinsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnfinsrchActionPerformed
         // TODO add your handling code here:
         tabelfin();
 }//GEN-LAST:event_btnfinsrchActionPerformed
 
     private void tblfouMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblfouMouseClicked
         // TODO add your handling code here:
         if(evt.getClickCount()==1){
             int row=0;
             row=tblfou.getSelectedRow();
             lblview.setIcon(new ImageIcon("Z:/localhost/outbox/"+tabfou.getValueAt(row,2).toString()+""));
         }
 
         if(evt.getClickCount()==2){
 //            Tampil1();
         }
 }//GEN-LAST:event_tblfouMouseClicked
 
     private void btnfoutsrchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnfoutsrchActionPerformed
         // TODO add your handling code here:
         tabelfou();
 }//GEN-LAST:event_btnfoutsrchActionPerformed
 
     private void panelfaxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelfaxMouseClicked
         // TODO add your handling code here:
 }//GEN-LAST:event_panelfaxMouseClicked
 
     private void btnreleaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnreleaseActionPerformed
         // TODO add your handling code here:
         sql="update user_account set _activity=0 where username= '" +cbagenrelease.getSelectedItem()+ "' limit 1";
         jconn.SQLExecute(sql, conn);
         JOptionPane.showMessageDialog(null, "AGEN "+cbagenrelease.getSelectedItem()+" HAS BEEN RELEASED", "SETTING",JOptionPane.WARNING_MESSAGE);
 }//GEN-LAST:event_btnreleaseActionPerformed
 
     private void cbagenreleaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbagenreleaseActionPerformed
         // TODO add your handling code here:
 }//GEN-LAST:event_cbagenreleaseActionPerformed
 
     private void tblmsinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblmsinMouseClicked
         // TODO add your handling code here:
        if(tblmsin.getSelectedRow()>=0){
             btnreplymsg.setEnabled(true);
             btndelmsg.setEnabled(true);
             msgidin=(String)tblmsin.getValueAt(tblmsin.getSelectedRow(), 5);
             if (tblmsin.getValueAt(tblmsin.getSelectedRow(), 4)==null){
                 txtimsg3.setText("");
             }else{
                 txtimsg3.setText((String)tblmsin.getValueAt(tblmsin.getSelectedRow(), 4));
             }
             tu = (String)tblmsin.getValueAt(tblmsin.getSelectedRow(), 2);
             if(tblmsin.getValueAt(tblmsin.getSelectedRow(), 3).equals("0")){
                 sql="update msg_inbox set _read = 1 where msg_id='"+msgidin+"'";
                 jconn.SQLExecute(sql, conn);
                 tabelmsin();
                 tt--;
             }
         }
 }//GEN-LAST:event_tblmsinMouseClicked
 
     private void btnreplymsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnreplymsgActionPerformed
         // TODO add your handling code here:
         Mssg mssg = new Mssg();
         mssg.setVisible(true);
 
         Misg.txttu.setText(tu);
 }//GEN-LAST:event_btnreplymsgActionPerformed
 
     private void tblmsouMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblmsouMouseClicked
         // TODO add your handling code here:
         if(tblmsou.getSelectedRow()>=0){
             btndelmsg1.setEnabled(true);
             msgidou=(String)tblmsou.getValueAt(tblmsou.getSelectedRow(), 4);
             if (tblmsou.getValueAt(tblmsou.getSelectedRow(), 3)==null){
                 txtimsg4.setText("");
             }else{
                 txtimsg4.setText((String)tblmsou.getValueAt(tblmsou.getSelectedRow(), 3));
             }
         }
 }//GEN-LAST:event_tblmsouMouseClicked
 
     private void btncomposemsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncomposemsgActionPerformed
         // TODO add your handling code here:
         Mssg mssg = new Mssg();
         mssg.setVisible(true);
 }//GEN-LAST:event_btncomposemsgActionPerformed
 
     private void btndelmsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndelmsgActionPerformed
         // TODO add your handling code here:
         sql="update msg_inbox set _erased = 1 where msg_id='"+msgidin+"'";
         jconn.SQLExecute(sql, conn);
 }//GEN-LAST:event_btndelmsgActionPerformed
 
     private void btndelmsg1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndelmsg1ActionPerformed
         // TODO add your handling code here:
         sql="update msg_outbox set _erased = 1 where msg_id='"+msgidou+"'";
         jconn.SQLExecute(sql, conn);
     }//GEN-LAST:event_btndelmsg1ActionPerformed
 
     private void ckstoringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckstoringActionPerformed
         // TODO add your handling code here:
 }//GEN-LAST:event_ckstoringActionPerformed
 
     private void cbticstatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbticstatusActionPerformed
         // TODO add your handling code here:
 }//GEN-LAST:event_cbticstatusActionPerformed
 int i;
 int InboundCall;
 int AnsweredCall ;
 int AbandonCall;
 int PhantomCall;
 int BlankCall;
 int Customer;
 int Inquiry;
 int Non_customer;
 int Complain;
 int CustDriver;
 int CustUser;
 int Towing;
 int CustPIC;
 int CustOther;
 int IntANJ;
 int IntCC;
 int IntCSO;
 int IntDriver;
 int IntOther;
 
 String TotalInbWait;
 String TotalInbDelay;
 String TotalInbBusy;
 String TotalInbCall;
 
 String AvgInbCall;
 String AvgInbBusy;
 String ASA ;
 
 int OutAnsweredCall    ;
 int OutNotAnsweredCall ;
 int OutCall         ;
 String TotalOutCall    ;
 String AvgOutCall      ;
     //OtherCall       := 0;
 int BCustCall       ;
 int BDealerCall     ;
 int BTechCall       ;
 int BHPMCall        ;
 int BOtherCall      ;
 int OutgoingSMS     ;
 String lpad            ;
 String hi,ho,dayin1,dayin2,dayou1,dayou2,perfin1,perfin2,perfou1,perfou2;
     private void btnhiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnhiActionPerformed
         // TODO add your handling code here:
         tabhoin.setRowCount(0);
         Date dt1 =dthi.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         hi = sdf.format(dt1);
         sql="delete from report_inbound";
         jconn.SQLExecute(sql, conn);
         for(i=0;i<=23;i++){
             InboundCall = 0;
             AnsweredCall    = 0;
             AbandonCall     = 0;
             BlankCall       = 0;
             Inquiry         = 0;
             Complain        = 0;
 
 
             TotalInbWait    = "0";
             TotalInbDelay   = "0";
             TotalInbBusy    = "0";
             TotalInbCall    = "0";
 
             AvgInbCall      = "0";
             AvgInbBusy      = "0";
             ASA             = "0";
             try{
                 sql="select lpad("+i+",2,00)";
                 rs=jconn.SQLExecuteRS(sql, conn);
                 while(rs.next()){
                     lpad=String.valueOf(rs.getString(1));
                 }
                 sql1="select count(username) as total_inbound " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql1, conn);
                 while(rs.next()){
                     InboundCall=Integer.parseInt(rs.getString("total_inbound"));
                 }
                 sql2="select count(username) as total_outbound " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1";
                 rs=jconn.SQLExecuteRS(sql2, conn);
                 while(rs.next()){
                     OutCall=Integer.parseInt(rs.getString("total_outbound"));
                 }
                 sql3="select count(username) as total_answered " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0 " +
                         "and _callstatus=1";
                 rs=jconn.SQLExecuteRS(sql3, conn);
                 while(rs.next()){
                     AnsweredCall=Integer.parseInt(rs.getString("total_answered"));
                 }
                 sql4="select count(username) as total_abandon " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0 " +
                         "and _callstatus=0";
                 rs=jconn.SQLExecuteRS(sql4, conn);
                 while(rs.next()){
                     AbandonCall=Integer.parseInt(rs.getString("total_abandon"));
                 }
                 sql5="select ifnull(sec_to_time(sum(time_to_sec(delay))),0) as total_delay " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql5, conn);
                 while(rs.next()){
                     TotalInbDelay=String.valueOf(rs.getString("total_delay"));
                 }
                 sql6="select ifnull(sec_to_time(sum(time_to_sec(duration))),0) as total_duration " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql6, conn);
                 while(rs.next()){
                     TotalInbCall=String.valueOf(rs.getString("total_duration"));
                 }
                 sql7="select ifnull(sec_to_time(sum(time_to_sec(busy))),0) as total_busy " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql7, conn);
                 while(rs.next()){
                     TotalInbBusy=String.valueOf(rs.getString("total_busy"));
                 }
                 sql8="select ifnull(sec_to_time(avg(time_to_sec(duration))),0) as avg_inbound " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql8, conn);
                 while(rs.next()){
                     AvgInbCall=String.valueOf(rs.getString("avg_inbound"));
                 }
                 sql9="select ifnull(sec_to_time(avg(time_to_sec(busy))),0) as avg_busy " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql9, conn);
                 while(rs.next()){
                     AvgInbBusy=String.valueOf(rs.getString("avg_busy"));
                 }
                 sql10="select ifnull(sum(_inquiry),0) as inquiry " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql10, conn);
                 while(rs.next()){
                     Inquiry=Integer.parseInt(rs.getString("inquiry"));
                 }
                 sql11="select ifnull(sum(_complaint),0) as complain " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql11, conn);
                 while(rs.next()){
                     Complain=Integer.parseInt(rs.getString("complain"));
                 }
                 sql12="select ifnull(sum(_blankcall),0) as _blankcall " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql12, conn);
                 while(rs.next()){
                     BlankCall=Integer.parseInt(rs.getString("_blankcall"));
                 }
                 sql14="select ifnull(sec_to_time(avg(time_to_sec(delay))),0) as avg_delay " +
                         "from log_phone " +
                         "where log_date='"+hi+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=0";
                 rs=jconn.SQLExecuteRS(sql14, conn);
                 while(rs.next()){
                     ASA=String.valueOf(rs.getString("avg_delay"));
                 }
             }catch(Exception e){
                 System.out.print(e);
             }
             sql13="insert into report_inbound(" +
                     "hour " +
                     ",inb_call_count " +
                     ",abandon_call " +
                     ",answered_call " +
 //                    ",total_inb_wait_time " +
                     ",total_inb_delay_time " +
                     ",total_inb_call_time " +
                     ",total_inb_busy_time " +
                     ",asa " +
                     ",avg_inb_call " +
                     ",avg_inb_busy " +
                     ",blank_call " +
 //                    ",prank_call " +
                     ",inquiry " +
 //                    ",inquiry_transfer " +
                     ",complain " +
 //                    ",complain_transfer " +
 //                    ",road_assistance " +
 //                    ",towing) " +
                     ")values (" +
                     "'"+lpad+":00"+"' " +
                     ",'"+InboundCall+"'" +
                     ",'"+AbandonCall+"'" +
                     ",'"+AnsweredCall+"'" +      
                     ",'"+TotalInbDelay+"'" +
                     ",'"+TotalInbCall+"'" +
                     ",'"+TotalInbBusy+"'" +                    
                     ",'"+ASA+"'" +              
 //           +  ','+QuotedStr(FormatDateTime('hh:nn:ss',ASA))
                     ",'"+AvgInbCall+"'" +
                     ",'"+AvgInbBusy+"'" +
                     ",'"+BlankCall+"'" +
                     ",'"+Inquiry+"'" +
                     ",'"+Complain+"'" +
                     ")";
             jconn.SQLExecute(sql13, conn);
         }
         try{
               sql="select * from report_inbound " ;
               rs = jconn.SQLExecuteRS(sql, conn);
               while(rs.next()){
                   hoin[0]=rs.getString("hour");
                   hoin[1]=rs.getString("inb_call_count");
                   hoin[2]=rs.getString("answered_call");
                   hoin[3]=rs.getString("abandon_call");
                   hoin[4]=rs.getString("total_inb_call_time");
                   hoin[5]=rs.getString("avg_inb_call");
                   hoin[6]=rs.getString("total_inb_busy_time");
                   hoin[7]=rs.getString("avg_inb_busy");
                   hoin[8]=rs.getString("total_inb_delay_time");
                   hoin[9]=rs.getString("asa");
                   hoin[10]=rs.getString("blank_call");
                   hoin[11]=rs.getString("inquiry");
                   hoin[12]=rs.getString("complain");
                   tabhoin.addRow(hoin);
               }
           }catch(Exception exc){
               System.err.println(exc.getMessage());
           }
 }//GEN-LAST:event_btnhiActionPerformed
 
     private void btnhoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnhoActionPerformed
         // TODO add your handling code here:
         tabhoou.setRowCount(0);
         Date dt1 =dtho.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         ho = sdf.format(dt1);
         sql="delete from report_outbound";
         jconn.SQLExecute(sql, conn);
         for(i=0;i<=23;i++){
 
             OutCall         = 0;
             TotalOutCall    = "0";
             AvgOutCall      = "0";
             Customer        = 0;
             Non_customer    = 0;
             CustDriver      = 0;
             CustUser        = 0;
             CustPIC         = 0;
             CustOther       = 0;
             IntANJ          = 0;
             IntCC           = 0;
             IntCSO          = 0;
             IntDriver       = 0;
             IntOther        = 0;
 
 //            ASA             = "0";
 
             try{
                 sql="select lpad("+i+",2,00)";
                 rs=jconn.SQLExecuteRS(sql, conn);
                 while(rs.next()){
                     lpad=String.valueOf(rs.getString(1));
                 }
                 sql1="select count(username) as total_outbound " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1";
                 rs=jconn.SQLExecuteRS(sql1, conn);
                 while(rs.next()){
                     OutCall=Integer.parseInt(rs.getString("total_outbound"));
                 }
                 sql2="select ifnull(sec_to_time(sum(time_to_sec(duration))),0) as total_duration " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1";
                 rs=jconn.SQLExecuteRS(sql2, conn);
                 while(rs.next()){
                     TotalOutCall=String.valueOf(rs.getString("total_duration"));
                 }
                 sql3="select ifnull(sec_to_time(avg(time_to_sec(duration))),0) as avg_outbound " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1";
                 rs=jconn.SQLExecuteRS(sql3, conn);
                 while(rs.next()){
                     AvgOutCall=String.valueOf(rs.getString("avg_outbound"));
                 }
                 sql4="select count(caller_type) as customer " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Customer' ";
                 rs=jconn.SQLExecuteRS(sql4, conn);
                 while(rs.next()){
                     Customer=Integer.parseInt(rs.getString("customer"));
                 }
                 sql5="select count(caller_type) as noncust " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Non-customer' ";
                 rs=jconn.SQLExecuteRS(sql5, conn);
                 while(rs.next()){
                     Non_customer=Integer.parseInt(rs.getString("noncust"));
                 }
                 sql6="select count(caller_type) as cdriver " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-Driver' ";
                 rs=jconn.SQLExecuteRS(sql6, conn);
                 while(rs.next()){
                     CustDriver=Integer.parseInt(rs.getString("cdriver"));
                 }
                 sql7="select count(caller_type) as cuser " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-User' ";
                 rs=jconn.SQLExecuteRS(sql7, conn);
                 while(rs.next()){
                     CustUser=Integer.parseInt(rs.getString("cuser"));
                 }
                 sql8="select count(caller_type) as cpic " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-PIC' ";
                 rs=jconn.SQLExecuteRS(sql8, conn);
                 while(rs.next()){
                     CustPIC=Integer.parseInt(rs.getString("cpic"));
                 }
                 sql9="select count(caller_type) as cother " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-Other' ";
                 rs=jconn.SQLExecuteRS(sql9, conn);
                 while(rs.next()){
                     CustOther=Integer.parseInt(rs.getString("cother"));
                 }
                 sql10="select count(caller_type) as ianj " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type = 'Internal-ANJ' ";
                 rs=jconn.SQLExecuteRS(sql10, conn);
                 while(rs.next()){
                     IntANJ=Integer.parseInt(rs.getString("ianj"));
                 }
                 sql11="select count(caller_type) as icc " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-CC' ";
                 rs=jconn.SQLExecuteRS(sql11, conn);
                 while(rs.next()){
                     IntCC=Integer.parseInt(rs.getString("icc"));
                 }
                 sql12="select count(caller_type) as icso " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-CSO' ";
                 rs=jconn.SQLExecuteRS(sql12, conn);
                 while(rs.next()){
                     IntCSO=Integer.parseInt(rs.getString("icso"));
                 }
                 sql13="select count(caller_type) as idriver " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-Driver' ";
                 rs=jconn.SQLExecuteRS(sql13, conn);
                 while(rs.next()){
                     IntDriver=Integer.parseInt(rs.getString("idriver"));
                 }
                 sql14="select count(caller_type) as iother " +
                         "from log_phone " +
                         "where log_date='"+ho+"' " +
                         "and log_time like '"+lpad+"%' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-Other' ";
                 rs=jconn.SQLExecuteRS(sql14, conn);
                 while(rs.next()){
                     IntOther=Integer.parseInt(rs.getString("iother"));
                 }
 
             }catch(Exception e){
                 System.out.print(e);
             }
             sql15="insert into report_outbound(" +
                     "hour " +
                     ",out_call_count " +
                     ",total_out_call_time " +
                     ",avg_out_call " +
                     ",customer_count " +
                     ",noncust_count " +
                     ",custdriver_count " +
                     ",custuser_count " +
                     ",custpic_count " +
                     ",custother_count " +
                     ",internalanj_count " +
                     ",internalcc_count " +
                     ",internalcso_count " +
                     ",internaldriver_count " +
                     ",internalother_count " +
                     ")values (" +
                     "'"+lpad+":00"+"' " +
                     ",'"+OutCall+"'" +
                     ",'"+TotalOutCall+"'" +
                     ",'"+AvgOutCall+"'" +
                     ",'"+Customer+"'" +
                     ",'"+Non_customer+"'" +
                     ",'"+CustDriver+"'" +
                     ",'"+CustUser+"'" +
                     ",'"+CustPIC+"'" +
                     ",'"+CustOther+"'" +
                     ",'"+IntANJ+"'" +
                     ",'"+IntCC+"'" +
                     ",'"+IntCSO+"'" +
                     ",'"+IntDriver+"'" +
                     ",'"+IntOther+"'" +
                     ")";
             jconn.SQLExecute(sql15, conn);
         }
         try{
               sql="select * from report_outbound " ;
               rs = jconn.SQLExecuteRS(sql, conn);
               while(rs.next()){
                   hoou[0]=rs.getString("hour");
                   hoou[1]=rs.getString("out_call_count");
                   hoou[2]=rs.getString("total_out_call_time");
                   hoou[3]=rs.getString("avg_out_call");
                   hoou[4]=rs.getString("customer_count");
                   hoou[5]=rs.getString("noncust_count");
                   hoou[6]=rs.getString("custdriver_count");
                   hoou[7]=rs.getString("custuser_count");
                   hoou[8]=rs.getString("custpic_count");
                   hoou[9]=rs.getString("custother_count");
                   hoou[10]=rs.getString("internalanj_count");
                   hoou[11]=rs.getString("internalcc_count");
                   hoou[12]=rs.getString("internalcso_count");
                   hoou[13]=rs.getString("internaldriver_count");
                   hoou[14]=rs.getString("internalother_count");
                   tabhoou.addRow(hoou);
               }
           }catch(Exception exc){
               System.err.println(exc.getMessage());
           }
 }//GEN-LAST:event_btnhoActionPerformed
 
     private void btnpi1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnpi1ActionPerformed
         // TODO add your handling code here:
         tabperfin.setRowCount(0);
         String tgl;
         String nama;
         Date dt1 =dtpi.getDate();
         Date dt2 =dtpi1.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         perfin1 = sdf.format(dt1);
         perfin2 = sdf.format(dt2);
         sql="delete from report_inbound";
         jconn.SQLExecute(sql, conn);
 
         sql1="insert into report_inbound(date, username) " +
                 "select distinct log_date, username from log_phone " +
                 "where _direction=0 " +
                 "and log_date between '"+perfin1+"' and '"+perfin2+"'";
         jconn.SQLExecute(sql1, conn);
 
         try{
             sql="select date, username from report_inbound";
             rs=jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 tgl=rs.getString(1);
                 nama=rs.getString(2);
 
                 sql1="select count(username) as total_inbound " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql1, conn);
                 while(rs1.next()){
                     InboundCall=Integer.parseInt(rs1.getString("total_inbound"));
                 }
                 sql2="select count(username) as total_phantom " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0 " +
                         "and _callstatus=-1";
                 rs1=jconn.SQLExecuteRS(sql2, conn);
                 while(rs1.next()){
                     PhantomCall=Integer.parseInt(rs1.getString("total_phantom"));
                 }
                 sql3="select count(username) as total_answered " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0 " +
                         "and _callstatus=1";
                 rs1=jconn.SQLExecuteRS(sql3, conn);
                 while(rs1.next()){
                     AnsweredCall=Integer.parseInt(rs1.getString("total_answered"));
                 }
                 sql4="select count(username) as total_abandon " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0 " +
                         "and _callstatus=0";
                 rs1=jconn.SQLExecuteRS(sql4, conn);
                 while(rs1.next()){
                     AbandonCall=Integer.parseInt(rs1.getString("total_abandon"));
                 }
                 sql5="select ifnull(sec_to_time(sum(time_to_sec(delay))),0) as total_delay " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql5, conn);
                 while(rs1.next()){
                     TotalInbDelay=String.valueOf(rs1.getString("total_delay"));
                 }
                 sql6="select ifnull(sec_to_time(sum(time_to_sec(duration))),0) as total_duration " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql6, conn);
                 while(rs1.next()){
                     TotalInbCall=String.valueOf(rs1.getString("total_duration"));
                 }
                 sql7="select ifnull(sec_to_time(sum(time_to_sec(busy))),0) as total_busy " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql7, conn);
                 while(rs1.next()){
                     TotalInbBusy=String.valueOf(rs1.getString("total_busy"));
                 }
                 sql8="select ifnull(sec_to_time(avg(time_to_sec(duration))),0) as avg_inbound " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql8, conn);
                 while(rs1.next()){
                     AvgInbCall=String.valueOf(rs1.getString("avg_inbound"));
                 }
                 sql9="select ifnull(sec_to_time(avg(time_to_sec(busy))),0) as avg_busy " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql9, conn);
                 while(rs1.next()){
                     AvgInbBusy=String.valueOf(rs1.getString("avg_busy"));
                 }
                 sql10="select ifnull(sum(_inquiry),0) as inquiry " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql10, conn);
                 while(rs1.next()){
                     Inquiry=Integer.parseInt(rs1.getString("inquiry"));
                 }
                 sql11="select ifnull(sum(_complaint),0) as complain " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql11, conn);
                 while(rs1.next()){
                     Complain=Integer.parseInt(rs1.getString("complain"));
                 }
                 sql12="select ifnull(sum(_blankcall),0) as _blankcall " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql12, conn);
                 while(rs1.next()){
                     BlankCall=Integer.parseInt(rs1.getString("_blankcall"));
                 }
                 sql15="select ifnull(sec_to_time(avg(time_to_sec(delay))),0) as avg_delay " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql15, conn);
                 while(rs1.next()){
                     ASA=String.valueOf(rs1.getString("avg_delay"));
                 }
                 sql13="update report_inbound set " +
                         "inb_call_count='"+InboundCall+"' " +
                         ",answered_call='"+AnsweredCall+"' " +
                         ",abandon_call='"+AbandonCall+"' " +
                         ",total_inb_delay_time='"+TotalInbDelay+"' " +
                         ",total_inb_call_time='"+TotalInbCall+"' " +
                         ",avg_inb_call='"+AvgInbCall+"' " +
                         ",total_inb_busy_time='"+TotalInbBusy+"' " +
                         ",asa='"+ASA+"' " +
                         ",avg_inb_busy='"+AvgInbBusy+"' " +
                         ",blank_call='"+BlankCall+"' " +
                         ",complain='"+Complain+"' " +
                         ",inquiry='"+Inquiry+"' " +
                         "where date='"+tgl+"' and username='"+nama+"' ";
                 jconn.SQLExecute(sql13, conn);
             }
         }catch(Exception e){
             System.out.print(e);
         }
 
         try{
               sql="select * from report_inbound " ;
               rs = jconn.SQLExecuteRS(sql, conn);
               while(rs.next()){
                   perfin[0]=rs.getString("Date");
                   perfin[1]=rs.getString("username");
                   perfin[2]=rs.getString("inb_call_count");
                   perfin[3]=rs.getString("answered_call");
                   perfin[4]=rs.getString("abandon_call");
                   perfin[5]=rs.getString("total_inb_call_time");
                   perfin[6]=rs.getString("avg_inb_call");
                   perfin[7]=rs.getString("total_inb_busy_time");
                   perfin[8]=rs.getString("avg_inb_busy");
                   perfin[9]=rs.getString("total_inb_delay_time");
                   perfin[10]=rs.getString("asa");
                   perfin[11]=rs.getString("blank_call");
                   perfin[12]=rs.getString("complain");
                   perfin[13]=rs.getString("inquiry");
                   tabperfin.addRow(perfin);
               }
           }catch(Exception exc){
               System.err.println(exc.getMessage());
           }
 }//GEN-LAST:event_btnpi1ActionPerformed
 
     private void btnpo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnpo1ActionPerformed
         // TODO add your handling code here:
         tabperfou.setRowCount(0);
         String tgl;
         String nama;
         Date dt1 =dtpo.getDate();
         Date dt2 =dtpo1.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         perfou1 = sdf.format(dt1);
         perfou2 = sdf.format(dt2);
         sql="delete from report_outbound";
         jconn.SQLExecute(sql, conn);
 
         sql1="insert into report_outbound(date, username) " +
                 "select distinct log_date, username from log_phone " +
                 "where _direction=1 " +
                 "and log_date between '"+perfou1+"' and '"+perfou2+"'";
         jconn.SQLExecute(sql1, conn);
 
         try{
             sql="select date, username from report_outbound";
             rs=jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 tgl=rs.getString(1);
                 nama=rs.getString(2);
 
                 sql1="select count(username) as total_outbound " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1";
                 rs1=jconn.SQLExecuteRS(sql1, conn);
                 while(rs1.next()){
                     OutCall=Integer.parseInt(rs1.getString("total_outbound"));
                 }
                 sql2="select ifnull(sec_to_time(sum(time_to_sec(duration))),0) as total_duration " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1";
                 rs1=jconn.SQLExecuteRS(sql2, conn);
                 while(rs1.next()){
                     TotalOutCall=String.valueOf(rs1.getString("total_duration"));
                 }
                 sql3="select ifnull(sec_to_time(avg(time_to_sec(duration))),0) as avg_outbound " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1";
                 rs1=jconn.SQLExecuteRS(sql3, conn);
                 while(rs1.next()){
                     AvgOutCall=String.valueOf(rs1.getString("avg_outbound"));
                 }
                 sql4="select count(caller_type) as customer " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer' ";
                 rs1=jconn.SQLExecuteRS(sql4, conn);
                 while(rs1.next()){
                     Customer=Integer.parseInt(rs1.getString("customer"));
                 }
                 sql5="select count(caller_type) as noncust " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Non-customer' ";
                 rs1=jconn.SQLExecuteRS(sql5, conn);
                 while(rs1.next()){
                     Non_customer=Integer.parseInt(rs1.getString("noncust"));
                 }
                 sql6="select count(caller_type) as cdriver " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-Driver' ";
                 rs1=jconn.SQLExecuteRS(sql6, conn);
                 while(rs1.next()){
                     CustDriver=Integer.parseInt(rs1.getString("cdriver"));
                 }
                 sql7="select count(caller_type) as cuser " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-User' ";
                 rs1=jconn.SQLExecuteRS(sql7, conn);
                 while(rs1.next()){
                     CustUser=Integer.parseInt(rs1.getString("cuser"));
                 }
                 sql8="select count(caller_type) as cpic " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-PIC' ";
                 rs1=jconn.SQLExecuteRS(sql8, conn);
                 while(rs1.next()){
                     CustPIC=Integer.parseInt(rs1.getString("cpic"));
                 }
                 sql9="select count(caller_type) as cother " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-Other' ";
                 rs1=jconn.SQLExecuteRS(sql9, conn);
                 while(rs1.next()){
                     CustOther=Integer.parseInt(rs1.getString("cother"));
                 }
                 sql10="select count(caller_type) as ianj " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type = 'Internal-ANJ' ";
                 rs1=jconn.SQLExecuteRS(sql10, conn);
                 while(rs1.next()){
                     IntANJ=Integer.parseInt(rs1.getString("ianj"));
                 }
                 sql11="select count(caller_type) as icc " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-CC' ";
                 rs1=jconn.SQLExecuteRS(sql11, conn);
                 while(rs1.next()){
                     IntCC=Integer.parseInt(rs1.getString("icc"));
                 }
                 sql12="select count(caller_type) as icso " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-CSO' ";
                 rs1=jconn.SQLExecuteRS(sql12, conn);
                 while(rs1.next()){
                     IntCSO=Integer.parseInt(rs1.getString("icso"));
                 }
                 sql13="select count(caller_type) as idriver " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-Driver' ";
                 rs1=jconn.SQLExecuteRS(sql13, conn);
                 while(rs1.next()){
                     IntDriver=Integer.parseInt(rs1.getString("idriver"));
                 }
                 sql14="select count(caller_type) as iother " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and username='"+nama+"' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-Other' ";
                 rs1=jconn.SQLExecuteRS(sql14, conn);
                 while(rs1.next()){
                     IntOther=Integer.parseInt(rs1.getString("iother"));
                 }
 
                 sql15="update report_outbound set " +
                         "out_call_count='"+OutCall+"' " +
                         ",total_out_call_time='"+TotalOutCall+"' " +
                         ",avg_out_call='"+AvgOutCall+"' " +
                         ",customer_count='"+Customer+"' " +
                         ",noncust_count='"+Non_customer+"' " +
                         ",custdriver_count='"+CustDriver+"' " +
                         ",custuser_count='"+CustUser+"' " +
                         ",custpic_count='"+CustPIC+"' " +
                         ",custother_count='"+CustOther+"' " +
                         ",internalanj_count='"+IntANJ+"' " +
                         ",internalcc_count='"+IntCC+"' " +
                         ",internalcso_count='"+IntCSO+"' " +
                         ",internaldriver_count='"+IntDriver+"' " +
                         ",internalother_count='"+IntOther+"' " +
                         "where date='"+tgl+"' and username='"+nama+"' ";
                 jconn.SQLExecute(sql15, conn);
             }
         }catch(Exception e){
             System.out.print(e);
         }
 
         try{
               sql="select * from report_outbound " ;
               rs = jconn.SQLExecuteRS(sql, conn);
               while(rs.next()){
                   perfou[0]=rs.getString("Date");
                   perfou[1]=rs.getString("username");
                   perfou[2]=rs.getString("out_call_count");
                   perfou[3]=rs.getString("total_out_call_time");
                   perfou[4]=rs.getString("avg_out_call");
                   perfou[5]=rs.getString("customer_count");
                   perfou[6]=rs.getString("noncust_count");
                   perfou[7]=rs.getString("custdriver_count");
                   perfou[8]=rs.getString("custuser_count");
                   perfou[9]=rs.getString("custpic_count");
                   perfou[10]=rs.getString("custother_count");
                   perfou[11]=rs.getString("internalanj_count");
                   perfou[12]=rs.getString("internalcc_count");
                   perfou[13]=rs.getString("internalcso_count");
                   perfou[14]=rs.getString("internaldriver_count");
                   perfou[15]=rs.getString("internalother_count");
                   tabperfou.addRow(perfou);
               }
           }catch(Exception exc){
               System.err.println(exc.getMessage());
           }
 }//GEN-LAST:event_btnpo1ActionPerformed
 
     private void btndiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndiActionPerformed
         // TODO add your handling code here:
         tabdayin.setRowCount(0);
         String tgl;
         Date dt1 =dtdi.getDate();
         Date dt2 =dtdi1.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         dayin1 = sdf.format(dt1);
         dayin2 = sdf.format(dt2);
         sql="delete from report_inbound";
         jconn.SQLExecute(sql, conn);
 
         sql1="insert into report_inbound(date) " +
                 "select distinct log_date from log_phone " +
                 "where _direction=0 " +
                 "and log_date between '"+dayin1+"' and '"+dayin2+"'";
         jconn.SQLExecute(sql1, conn);
 
         try{
             sql="select date from report_inbound";
             rs=jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 tgl=rs.getString(1);
                 System.out.print("\n isi tgl = "+tgl);
 
                 sql1="select count(username) as total_inbound " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql1, conn);
                 while(rs1.next()){
                     InboundCall=Integer.parseInt(rs1.getString("total_inbound"));
                 }
                 sql2="select count(username) as total_phantom " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0 " +
                         "and _callstatus=-1";
                 rs1=jconn.SQLExecuteRS(sql2, conn);
                 while(rs1.next()){
                     PhantomCall=Integer.parseInt(rs1.getString("total_phantom"));
                 }
                 sql3="select count(username) as total_answered " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0 " +
                         "and _callstatus=1";
                 rs1=jconn.SQLExecuteRS(sql3, conn);
                 while(rs1.next()){
                     AnsweredCall=Integer.parseInt(rs1.getString("total_answered"));
                 }
                 sql4="select count(username) as total_abandon " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0 " +
                         "and _callstatus=0";
                 rs1=jconn.SQLExecuteRS(sql4, conn);
                 while(rs1.next()){
                     AbandonCall=Integer.parseInt(rs1.getString("total_abandon"));
                 }
                 sql5="select ifnull(sec_to_time(sum(time_to_sec(delay))),0) as total_delay " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql5, conn);
                 while(rs1.next()){
                     TotalInbDelay=String.valueOf(rs1.getString("total_delay"));
                 }
                 sql6="select ifnull(sec_to_time(sum(time_to_sec(duration))),0) as total_duration " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql6, conn);
                 while(rs1.next()){
                     TotalInbCall=String.valueOf(rs1.getString("total_duration"));
                 }
                 sql7="select ifnull(sec_to_time(sum(time_to_sec(busy))),0) as total_busy " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql7, conn);
                 while(rs1.next()){
                     TotalInbBusy=String.valueOf(rs1.getString("total_busy"));
                 }
                 sql8="select ifnull(sec_to_time(avg(time_to_sec(duration))),0) as avg_inbound " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql8, conn);
                 while(rs1.next()){
                     AvgInbCall=String.valueOf(rs1.getString("avg_inbound"));
                 }
                 sql9="select ifnull(sec_to_time(avg(time_to_sec(busy))),0) as avg_busy " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql9, conn);
                 while(rs1.next()){
                     AvgInbBusy=String.valueOf(rs1.getString("avg_busy"));
                 }
                 sql10="select ifnull(sum(_inquiry),0) as inquiry " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql10, conn);
                 while(rs1.next()){
                     Inquiry=Integer.parseInt(rs1.getString("inquiry"));
                 }
                 sql11="select ifnull(sum(_complaint),0) as complain " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql11, conn);
                 while(rs1.next()){
                     Complain=Integer.parseInt(rs1.getString("complain"));
                 }
                 sql12="select ifnull(sum(_blankcall),0) as _blankcall " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql12, conn);
                 while(rs1.next()){
                     BlankCall=Integer.parseInt(rs1.getString("_blankcall"));
                 }
                 sql14="select ifnull(sec_to_time(avg(time_to_sec(delay))),0) as avg_delay " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=0";
                 rs1=jconn.SQLExecuteRS(sql14, conn);
                 while(rs1.next()){
                     ASA=String.valueOf(rs1.getString("avg_delay"));
                 }
                 sql13="update report_inbound set " +
                         "inb_call_count='"+InboundCall+"' " +
                         ",answered_call='"+AnsweredCall+"' " +
                         ",abandon_call='"+AbandonCall+"' " +
                         ",total_inb_delay_time='"+TotalInbDelay+"' " +
                         ",total_inb_call_time='"+TotalInbCall+"' " +
                         ",avg_inb_call='"+AvgInbCall+"' " +
                         ",total_inb_busy_time='"+TotalInbBusy+"' " +
                         ",asa='"+ASA+"' " +
                         ",avg_inb_busy='"+AvgInbBusy+"' " +
                         ",blank_call='"+BlankCall+"' " +
                         ",complain='"+Complain+"' " +
                         ",inquiry='"+Inquiry+"' " +
                         "where date='"+tgl+"' ";
                 jconn.SQLExecute(sql13, conn);
                 System.out.print("\nisi sql13 = "+sql13);
             }
         }catch(Exception e){
             System.out.print(e);
         }
 
         try{
               sql="select * from report_inbound " ;
               rs = jconn.SQLExecuteRS(sql, conn);
               while(rs.next()){
                   dayin[0]=rs.getString("Date");
                   dayin[1]=rs.getString("inb_call_count");
                   dayin[2]=rs.getString("answered_call");
                   dayin[3]=rs.getString("abandon_call");
                   dayin[4]=rs.getString("total_inb_call_time");
                   dayin[5]=rs.getString("avg_inb_call");
                   dayin[6]=rs.getString("total_inb_busy_time");
                   dayin[7]=rs.getString("avg_inb_busy");
                   dayin[8]=rs.getString("total_inb_delay_time");
                   dayin[9]=rs.getString("asa");
                   dayin[10]=rs.getString("blank_call");
                   dayin[11]=rs.getString("complain");
                   dayin[12]=rs.getString("inquiry");
 //                  dayin[11]=rs.getString("internalcc_count");
 //                  dayin[12]=rs.getString("internalcso_count");
 //                  dayin[13]=rs.getString("internaldriver_count");
 //                  dayin[14]=rs.getString("internalother_count");
                   tabdayin.addRow(dayin);
               }
           }catch(Exception exc){
               System.err.println(exc.getMessage());
           }
 
 }//GEN-LAST:event_btndiActionPerformed
 
     private void btndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndoActionPerformed
         // TODO add your handling code here:
         tabdayou.setRowCount(0);
         String tgl;
         Date dt1 =dtdo.getDate();
         Date dt2 =dtdo1.getDate();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         dayou1 = sdf.format(dt1);
         dayou2 = sdf.format(dt2);
         sql="delete from report_outbound";
         jconn.SQLExecute(sql, conn);
 
         sql1="insert into report_outbound(date) " +
                 "select distinct log_date from log_phone " +
                 "where _direction=1 " +
                 "and log_date between '"+dayou1+"' and '"+dayou2+"'";
         jconn.SQLExecute(sql1, conn);
 
         try{
             sql="select date from report_outbound";
             rs=jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 tgl=rs.getString(1);
                 System.out.print("\n isi tgl = "+tgl);
 
                 sql1="select count(username) as total_outbound " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1";
                 rs1=jconn.SQLExecuteRS(sql1, conn);
                 while(rs1.next()){
                     OutCall=Integer.parseInt(rs1.getString("total_outbound"));
                 }
                 sql2="select ifnull(sec_to_time(sum(time_to_sec(duration))),0) as total_duration " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1";
                 rs1=jconn.SQLExecuteRS(sql2, conn);
                 while(rs1.next()){
                     TotalOutCall=String.valueOf(rs1.getString("total_duration"));
                 }
                 sql3="select ifnull(sec_to_time(avg(time_to_sec(duration))),0) as avg_outbound " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1";
                 rs1=jconn.SQLExecuteRS(sql3, conn);
                 while(rs1.next()){
                     AvgOutCall=String.valueOf(rs1.getString("avg_outbound"));
                 }
                 sql4="select count(caller_type) as customer " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer' ";
                 rs1=jconn.SQLExecuteRS(sql4, conn);
                 while(rs1.next()){
                     Customer=Integer.parseInt(rs1.getString("customer"));
                 }
                 sql5="select count(caller_type) as noncust " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Non-customer' ";
                 rs1=jconn.SQLExecuteRS(sql5, conn);
                 while(rs1.next()){
                     Non_customer=Integer.parseInt(rs1.getString("noncust"));
                 }
                 sql6="select count(caller_type) as cdriver " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-Driver' ";
                 rs1=jconn.SQLExecuteRS(sql6, conn);
                 while(rs1.next()){
                     CustDriver=Integer.parseInt(rs1.getString("cdriver"));
                 }
                 sql7="select count(caller_type) as cuser " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-User' ";
                 rs1=jconn.SQLExecuteRS(sql7, conn);
                 while(rs1.next()){
                     CustUser=Integer.parseInt(rs1.getString("cuser"));
                 }
                 sql8="select count(caller_type) as cpic " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-PIC' ";
                 rs1=jconn.SQLExecuteRS(sql8, conn);
                 while(rs1.next()){
                     CustPIC=Integer.parseInt(rs1.getString("cpic"));
                 }
                 sql9="select count(caller_type) as cother " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Customer-Other' ";
                 rs1=jconn.SQLExecuteRS(sql9, conn);
                 while(rs1.next()){
                     CustOther=Integer.parseInt(rs1.getString("cother"));
                 }
                 sql10="select count(caller_type) as ianj " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type = 'Internal-ANJ' ";
                 rs1=jconn.SQLExecuteRS(sql10, conn);
                 while(rs1.next()){
                     IntANJ=Integer.parseInt(rs1.getString("ianj"));
                 }
                 sql11="select count(caller_type) as icc " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-CC' ";
                 rs1=jconn.SQLExecuteRS(sql11, conn);
                 while(rs1.next()){
                     IntCC=Integer.parseInt(rs1.getString("icc"));
                 }
                 sql12="select count(caller_type) as icso " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-CSO' ";
                 rs1=jconn.SQLExecuteRS(sql12, conn);
                 while(rs1.next()){
                     IntCSO=Integer.parseInt(rs1.getString("icso"));
                 }
                 sql13="select count(caller_type) as idriver " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-Driver' ";
                 rs1=jconn.SQLExecuteRS(sql13, conn);
                 while(rs1.next()){
                     IntDriver=Integer.parseInt(rs1.getString("idriver"));
                 }
                 sql14="select count(caller_type) as iother " +
                         "from log_phone " +
                         "where log_date='"+tgl+"' " +
                         "and _direction=1 " +
                         "and caller_type='Internal-Other' ";
                 rs1=jconn.SQLExecuteRS(sql14, conn);
                 while(rs1.next()){
                     IntOther=Integer.parseInt(rs1.getString("iother"));
                 }
 
                 sql15="update report_outbound set " +
                         "out_call_count='"+OutCall+"' " +
                         ",total_out_call_time='"+TotalOutCall+"' " +
                         ",avg_out_call='"+AvgOutCall+"' " +
                         ",customer_count='"+Customer+"' " +
                         ",noncust_count='"+Non_customer+"' " +
                         ",custdriver_count='"+CustDriver+"' " +
                         ",custuser_count='"+CustUser+"' " +
                         ",custpic_count='"+CustPIC+"' " +
                         ",custother_count='"+CustOther+"' " +
                         ",internalanj_count='"+IntANJ+"' " +
                         ",internalcc_count='"+IntCC+"' " +
                         ",internalcso_count='"+IntCSO+"' " +
                         ",internaldriver_count='"+IntDriver+"' " +
                         ",internalother_count='"+IntOther+"' " +
                         "where date='"+tgl+"' ";
                 jconn.SQLExecute(sql15, conn);
             }
         }catch(Exception e){
             System.out.print(e);
         }
 
         try{
               sql="select * from report_outbound " ;
               rs = jconn.SQLExecuteRS(sql, conn);
               while(rs.next()){
                   dayou[0]=rs.getString("Date");
                   dayou[1]=rs.getString("out_call_count");
                   dayou[2]=rs.getString("total_out_call_time");
                   dayou[3]=rs.getString("avg_out_call");
                   dayou[4]=rs.getString("customer_count");
                   dayou[5]=rs.getString("noncust_count");
                   dayou[6]=rs.getString("custdriver_count");
                   dayou[7]=rs.getString("custuser_count");
                   dayou[8]=rs.getString("custpic_count");
                   dayou[9]=rs.getString("custother_count");
                   dayou[10]=rs.getString("internalanj_count");
                   dayou[11]=rs.getString("internalcc_count");
                   dayou[12]=rs.getString("internalcso_count");
                   dayou[13]=rs.getString("internaldriver_count");
                   dayou[14]=rs.getString("internalother_count");
                   tabdayou.addRow(dayou);
               }
           }catch(Exception exc){
               System.err.println(exc.getMessage());
           }
 }//GEN-LAST:event_btndoActionPerformed
 
     private void cksubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cksubmitActionPerformed
         // TODO add your handling code here:
 }//GEN-LAST:event_cksubmitActionPerformed
 
     private void pnlscrollMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlscrollMouseClicked
         // TODO add your handling code here:
         if(evt.getClickCount()==2){
             lblscroll.setText("");
             Scrol.stop();
         }
     }//GEN-LAST:event_pnlscrollMouseClicked
 
     private void btnexportcall1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportcall1ActionPerformed
         // TODO add your handling code here:
         tabex=tabhoin;
         createexcel();
     }//GEN-LAST:event_btnexportcall1ActionPerformed
 
     private void btnexportcall2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportcall2ActionPerformed
         // TODO add your handling code here:
         tabex=tabhoou;
         createexcel();
     }//GEN-LAST:event_btnexportcall2ActionPerformed
 
     private void btnexportcall3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportcall3ActionPerformed
         // TODO add your handling code here:
         tabex=tabdayin;
         createexcel();
     }//GEN-LAST:event_btnexportcall3ActionPerformed
 
     private void btnexportcall4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportcall4ActionPerformed
         // TODO add your handling code here:
         tabex=tabdayou;
         createexcel();
     }//GEN-LAST:event_btnexportcall4ActionPerformed
 
     private void btnexportcall5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportcall5ActionPerformed
         // TODO add your handling code here:
         tabex=tabperfin;
         createexcel();
     }//GEN-LAST:event_btnexportcall5ActionPerformed
 
     private void btnexportcall6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportcall6ActionPerformed
         // TODO add your handling code here:
         tabex=tabperfou;
         createexcel();
     }//GEN-LAST:event_btnexportcall6ActionPerformed
 
     private void cbcustActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbcustActionPerformed
         // TODO add your handling code here:
         if(cbcust.getSelectedIndex()==-1){
             btncussavesms.setEnabled(false);
         }else if(!cbcust.getSelectedItem().equals(cuscom)){
             btncussavesms.setEnabled(true);
         }else{
             btncussavesms.setEnabled(false);
         }
     }//GEN-LAST:event_cbcustActionPerformed
 
     private void cbcust1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbcust1ActionPerformed
         // TODO add your handling code here:
         if(cbcust1.getSelectedIndex()==-1){
             btncussaveEmail.setEnabled(false);
         }else if(!cbcust1.getSelectedItem().equals(cuscom1)){
             btncussaveEmail.setEnabled(true);
         }else{
             btncussaveEmail.setEnabled(false);
         }
     }//GEN-LAST:event_cbcust1ActionPerformed
 
     private void btncussavesmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncussavesmsActionPerformed
         // TODO add your handling code here:
         btncussavesms.setEnabled(false);
         sql="update log_sms set cust_name='"+cbcust.getSelectedItem()+"'"
                 + ", ticket_no='"+txtnoticsms.getText()+"', ticket_id=(select ticket_id from tickets where ticket_no="+txtnoticsms.getText()+")"
                 + ", contract_no=(select contract_no from tickets where ticket_no="+txtnoticsms.getText()+")"
                 + " where sms_id='"+smsid+"' limit 1";
         jconn.SQLExecute(sql, conn);
         tabelsin();
     }//GEN-LAST:event_btncussavesmsActionPerformed
 
     private void btncussaveEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncussaveEmailActionPerformed
         // TODO add your handling code here:
         btncussaveEmail.setEnabled(false);
         sql="update log_mail set cust_name='"+cbcust1.getSelectedItem()+"'"
                 + ", ticket_no='"+txtnoticmail.getText()+"', ticket_id=(select ticket_id from tickets where ticket_no="+txtnoticmail.getText()+")"
                 + ", contract_no=(select contract_no from tickets where ticket_no="+txtnoticmail.getText()+")"
                 + " where mail_id='"+mailid+"' limit 1";
         jconn.SQLExecute(sql, conn);
         tabelmin();
     }//GEN-LAST:event_btncussaveEmailActionPerformed
 
     private void btncussaveFaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncussaveFaxActionPerformed
         // TODO add your handling code here:
         btncussaveFax.setEnabled(false);
         sql="update log_fax set cust_name='"+cbcust2.getSelectedItem()+"'"
                 + ", ticket_no='"+txtnoticfax.getText()+"', ticket_id=(select ticket_id from tickets where ticket_no="+txtnoticfax.getText()+")"
                 + ", contract_no=(select contract_no from tickets where ticket_no="+txtnoticfax.getText()+")"
                 + " where fax_id='"+faxid+"' limit 1";
         jconn.SQLExecute(sql, conn);
         tabelfin();
     }//GEN-LAST:event_btncussaveFaxActionPerformed
 
     private void cbcust2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbcust2ActionPerformed
         // TODO add your handling code here:
         if(cbcust2.getSelectedIndex()==-1){
             btncussaveFax.setEnabled(false);
         }else if(!cbcust2.getSelectedItem().equals(cuscom2)){
             btncussaveFax.setEnabled(true);
         }else{
             btncussaveFax.setEnabled(false);
         }
     }//GEN-LAST:event_cbcust2ActionPerformed
 
     private void dtmsiPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dtmsiPropertyChange
         // TODO add your handling code here:
         tabelmsin();
     }//GEN-LAST:event_dtmsiPropertyChange
 
     private void dtmsi1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dtmsi1PropertyChange
         // TODO add your handling code here:
         tabelmsin();
     }//GEN-LAST:event_dtmsi1PropertyChange
 
     private void dtmsoPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dtmsoPropertyChange
         // TODO add your handling code here:
         tabelmsou();
     }//GEN-LAST:event_dtmsoPropertyChange
 
     private void dtmso1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dtmso1PropertyChange
         // TODO add your handling code here:
         tabelmsou();
     }//GEN-LAST:event_dtmso1PropertyChange
 String nmfile, fullnmfile, nmfile1, fullnmfile1;
     private void btnAttachmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttachmentActionPerformed
         // TODO add your handling code here:
         JFileChooser chooser = new JFileChooser(att);
         chooser.setSelectedFile(new File(att));
         if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
             nmfile=(chooser.getSelectedFile().getName().toString());
             System.out.print("\nnamafile: " + nmfile + "\n");
             fullnmfile=(chooser.getSelectedFile().getAbsolutePath());
             s = "DOWNLOAD|EMAIL|"+mailid+"|0|"+att+"|"+fullnmfile+"|"+Log.ftpserver+"|"+Log.ftpuser+"\r\n";
             kirimUplo();
         }
 }//GEN-LAST:event_btnAttachmentActionPerformed
 String att,att1;
 Object sel1,sel2;
     private void jList2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList2MouseReleased
         // TODO add your handling code here:
         att="";
         int selectedIx = jList2.getSelectedIndex();
         sel1 = jList2.getModel().getElementAt(selectedIx);
         att=String.valueOf(sel1);
         btnAttachment.setEnabled(true);
 }//GEN-LAST:event_jList2MouseReleased
 
     private void jList3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList3MouseReleased
         // TODO add your handling code here:
         att1="";
         int selectedIx = jList3.getSelectedIndex();
         sel2 = jList3.getModel().getElementAt(selectedIx);
         att1=String.valueOf(sel2);
         btnAttachment1.setEnabled(true);
 }//GEN-LAST:event_jList3MouseReleased
 
     private void btnAttachment1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttachment1ActionPerformed
         // TODO add your handling code here:
         JFileChooser chooser = new JFileChooser(att1);
         chooser.setSelectedFile(new File(att1));
         if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
             nmfile1=(chooser.getSelectedFile().getName().toString());
             fullnmfile1=(chooser.getSelectedFile().getAbsolutePath());
             s = "DOWNLOAD|EMAIL|"+mailid+"|1|"+att1+"|"+fullnmfile1+"|"+Log.ftpserver+"|"+Log.ftpuser+"\r\n";
             kirimUplo();
         }
 }//GEN-LAST:event_btnAttachment1ActionPerformed
 
     private void cbcateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbcateActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbcateActionPerformed
 
     private void btnreadyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnreadyActionPerformed
         // TODO add your handling code here:
         dataregis();
         if (counter==0){
             if(cbdirection.getSelectedItem().equals("INBOUND")){
                 btnready.setText("Unregis");
                 lblactivity.setText("Registered");
                 cbdirection.setEnabled(false);
                 btnlogout.setEnabled(false);
                 s = "REGISTER|"+pabx+"|"+in_ext+"|"+in_ext+"\r\n";                  
                 kirimTele();
                 counter++;
                 receiv.start();
                 sql3="update user_account set _mode=0, _activity=4, time_activity=CURRENT_TIMESTAMP where username= '" +lbluser.getText()+ "' limit 1";
                 jconn.SQLExecute(sql3, conn);
             }else{
                 btnready.setText("Unregis");
                 lblactivity.setText("Registered");
                 btnoutbound.setEnabled(true);
                 cbdirection.setEnabled(false);
                 btnlogout.setEnabled(false);
                 s = "REGISTER|"+pabx+"|"+out_ext+"|"+out_ext+"\r\n";                  
                 kirimTele();
                 counter++;
                 outbound=true;
                 sql3="update user_account set _mode=1, time_activity=CURRENT_TIMESTAMP where username= '" +lbluser.getText()+ "' limit 1";
                 jconn.SQLExecute(sql3, conn);
             }
         }else{
            if(cbdirection.getSelectedItem().equals("INBOUND")){
                 btnready.setText("Ready");
                 lblactivity.setText("Disconnected");
                 btncall.setEnabled(false);
                 cbdirection.setEnabled(true);
                 btnlogout.setEnabled(true);
                 s = "UNREGISTER|"+pabx+"|"+in_ext+"|"+in_ext+"\r\n";                  
                 kirimTele();
                 counter=0;
            }else{
                 btnready.setText("Ready");
                 lblactivity.setText("Disconnected");
                 cbdirection.setEnabled(true);
                 btnlogout.setEnabled(true);
                 s = "UNREGISTER|"+pabx+"|"+out_ext+"|"+out_ext+"\r\n";                  
                 kirimTele();
                 counter=0;
                 outbound=false;
                 if(txtcalnoti.getText().equals("")){
                     btnoutbound.setEnabled(false);  
                 }
            }
            sql3="update user_account set _mode=NULL, _activity=1, time_activity=CURRENT_TIMESTAMP where username= '" +lbluser.getText()+ "' limit 1";
            jconn.SQLExecute(sql3, conn);
         }
     }//GEN-LAST:event_btnreadyActionPerformed
 
     private void cbFollowUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFollowUpActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbFollowUpActionPerformed
 
     private void txtdrivcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtdrivcodeActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_txtdrivcodeActionPerformed
 
     private void ckFCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckFCRActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_ckFCRActionPerformed
 
     private void ckstoring1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckstoring1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_ckstoring1ActionPerformed
 
     private void cbticstatus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbticstatus1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbticstatus1ActionPerformed
 
     private void cbdept1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbdept1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbdept1ActionPerformed
 
     private void cksubmit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cksubmit1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cksubmit1ActionPerformed
 
     private void cbcate1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbcate1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbcate1ActionPerformed
 
     private void txtdrivcode1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtdrivcode1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_txtdrivcode1ActionPerformed
 
     private void cbFollowUp1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFollowUp1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbFollowUp1ActionPerformed
 
     private void ckFCR1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckFCR1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_ckFCR1ActionPerformed
 
     private void createexcel(){
         int koltab=0;
         int counter=1;
         int kolom=0;
         int baris=0;
         int k;
         int b;
 
         JFileChooser chooser = new JFileChooser("");
         if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
             try{
 
                 //Membuat workbook baru dengan nama laporan.xls
                 WritableWorkbook workBook = Workbook.createWorkbook(new File(chooser.getSelectedFile().getAbsolutePath()+".xls"));
 
 
                  //Membuat sheet dengan nama Sheet pertama
                  WritableSheet sheet = workBook.createSheet("First sheet ",0);
                  System.out.print("\n debug : pembuatan header");
 
             Label label;
             for(int q=0;q<tabex.getColumnCount();q++){
                 label = new Label(q,0,(tabex.getColumnName(kolom).toString()));
                 kolom++;
                 sheet.addCell(label);
             }
 
             k=tabex.getColumnCount();
                 k-=1;
             b=tabex.getRowCount();
     //            b+=1;
 
            while(counter<=b){
                if (koltab>=k){
                    koltab=0;
                }
                while(koltab<=k){
                    if(kolom>k){
                        kolom=0;
                    }
     //               System.out.print(tblreptic.getValueAt(baris, kolom));
                    if (tabex.getValueAt(baris, kolom)==null){
                        label = new Label(koltab,counter,"");
                    }else{
                        label = new Label(koltab,counter,(tabex.getValueAt(baris, kolom).toString()));
                    }
                    koltab++;
                    kolom++;
 
                    sheet.addCell(label);
                }
                counter++;
                baris++;
            }
             workBook.write();
             workBook.close();
             JOptionPane.showMessageDialog(null, "SUCCESSED EXPORTING TO EXCEL", "REPORTING",JOptionPane.INFORMATION_MESSAGE);
 
 
             } catch (WriteException ex) {
                 System.out.print(ex);
             } catch (IOException ex) {
                 System.out.print(ex);
             }
            catch (Exception ex){
                ex.printStackTrace();
            }
         }
     }
             
        public static javax.swing.table.DefaultTableModel getDefaultTabelticconf(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Ticket No.","Confirm username","Status","Category","Assign Dept.","Assign User","Customer","Phone number","Username","No. Plat","Type","Driver","Phone","Ticket Id"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
         
         public static javax.swing.table.DefaultTableModel getDefaultTabelrepcal(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"log_id","log_date","log_time","username","shift","host_addr","line_number","_direction","_callstatus","duration","abandon","wait","Speed of answer","ACW","_inquiry","_complaint","_blankcall","_wrongnumber","caller_number","caller_type","caller_name","comment","filename"/*,"_callback","callback_time"*/,"_connected","_contacted","not_connect_reason","not_contact_reason","phone_number","ticket_id","cust company","Inbound Type"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
         public static javax.swing.table.DefaultTableModel getDefaultTabelrepsms(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Sms Id","Sms Date","Sms Time","Username","Status","Direction","Sms From","Sms To","Sms Text","Ticket Id",/*"ref_no","rpt_code",*/"Retry Count","Destination Type","Cust Company"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
         public static javax.swing.table.DefaultTableModel getDefaultTabelrepmail(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Mail Id","Mail Date","Mail Time","Mail From","Mail To","Mail Cc","Mail Subject","Mail Text","Mail Read","Username","Status","Ticket Id","Direction","Destination Type","Cust Company"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
         public static javax.swing.table.DefaultTableModel getDefaultTabelrepfax(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"fax_id","recipient","sender","username","doc_name","sent_time","rcvd_time","_status","_direction","ticket no","Cust Company"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
         public static javax.swing.table.DefaultTableModel getDefaultTabelact(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Username","Level","Activity","Info","Login","Host address","Line number"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }   
     
     public static javax.swing.table.DefaultTableModel getDefaultTabelmsin(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Date","Time","From","Read","Message",""}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabelmsou(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Date","Time","Recipient","Message",""}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabelhoin(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Hour","Total Receive","Answered","Abandoned","Call Duration","Avg Call Duration"
                         ,"ACW Time","Avg ACW Time","Total S.A.","A.S.A.","Blank Call"
                         ,"Complain","Inquiry"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabelhoou(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Hour","Outgoing Call","Outbound Call Time","Avg Outbound Call Time","Customer"
                         ,"Non-customer","Customer-Driver","Customer-User","Customer-PIC","Customer-Other"
                         ,"Internal-ANJ","Internal-CC","Internal-CSO","Internal-Driver","Internal-Other"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabeldayin(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Date","Total Receive","Answered","Abandoned"/*,"Phantom"*/,"Call Duration","Avg Call Duration"
                         ,"ACW Time","Avg ACW Time","Total S.A.","A.S.A.","Blank Call"
                         ,"Complain","Inquiry"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabeldayou(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Date","Outgoing Call","Outbound Call Time","Avg Outbound Call Time","Customer"
                         ,"Non-customer","Customer-Driver","Customer-User","Customer-PIC","Customer-Other"
                         ,"Internal-ANJ","Internal-CC","Internal-CSO","Internal-Driver","Internal-Other"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabelperfin(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Date","Agent","Total Receive","Answered","Abandoned","Call Duration","Avg Call Duration"
                         ,"ACW Time","Avg ACW Time","Total S.A.","A.S.A.","Blank Call"
                         ,"Complain","Inquiry"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
     public static javax.swing.table.DefaultTableModel getDefaultTabelperfou(){
         return new javax.swing.table.DefaultTableModel(
                 new Object [][]{},
                 new String [] {"Date","Agent","Outgoing Call","Outbound Call Time","Avg Outbound Call Time","Customer"
                         ,"Non-customer","Customer-Driver","Customer-User","Customer-PIC","Customer-Other"
                         ,"Internal-ANJ","Internal-CC","Internal-CSO","Internal-Driver","Internal-Other"}){
                 boolean[] canEdit=new boolean[]{
                     false,false,false,false,false,false
                             ,false,false,false,false,false
                             ,false,false,false,false,false
                 };
                 public boolean isCellEditable(int rowIndex, int columnIndex){
                         return canEdit[columnIndex];
                 }
         };
     }
        private static void appendToChatBox(String s) {
          synchronized (toAppend) {
              toAppend.append(s);
       }
    }
        private static void sendString(String s) {
          synchronized (toSend) {
              toSend.append(s + "\r\n");
       }
    }
        private static void sending(String pik) {
          synchronized (toSend) {
              toSend.append(pik + "\r\n");
       }
    }
        private static void angkat(String pick) {
          synchronized (toSend) {
              toSend.append(pick + "\r\n");
       }
    }
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new ContactCenterTunas().setVisible(true);
             }
         });
     }
 
     public static javax.swing.table.DefaultTableModel tabex;
     public static javax.swing.table.DefaultTableModel tabin=getDefaultTabelin();
     public static javax.swing.table.DefaultTableModel tabou=getDefaultTabelout();
     public static javax.swing.table.DefaultTableModel tabtic=getDefaultTabeltic();
     public static javax.swing.table.DefaultTableModel tabticconf=getDefaultTabelticconf();
     public static javax.swing.table.DefaultTableModel tabreptic=getDefaultTabelreptic();
     public static javax.swing.table.DefaultTableModel tabrepcal=getDefaultTabelrepcal();
     public static javax.swing.table.DefaultTableModel tabrepsms=getDefaultTabelrepsms();
     public static javax.swing.table.DefaultTableModel tabrepmail=getDefaultTabelrepmail();
     public static javax.swing.table.DefaultTableModel tabrepfax=getDefaultTabelrepfax();
     public static javax.swing.table.DefaultTableModel tabact=getDefaultTabelact();
     public static javax.swing.table.DefaultTableModel tabmin=getDefaultTabelmin();
     public static javax.swing.table.DefaultTableModel tabmou=getDefaultTabelmout();
     public static javax.swing.table.DefaultTableModel tabsin=getDefaultTabelsin();
     public static javax.swing.table.DefaultTableModel tabsou=getDefaultTabelsou();
     public static javax.swing.table.DefaultTableModel tabfin=getDefaultTabelfin();
     public static javax.swing.table.DefaultTableModel tabfou=getDefaultTabelfou();
     public static javax.swing.table.DefaultTableModel tabmsin=getDefaultTabelmsin();
     public static javax.swing.table.DefaultTableModel tabmsou=getDefaultTabelmsou();
     public static javax.swing.table.DefaultTableModel tabhoin=getDefaultTabelhoin();
     public static javax.swing.table.DefaultTableModel tabhoou=getDefaultTabelhoou();
     public static javax.swing.table.DefaultTableModel tabdayin=getDefaultTabeldayin();
     public static javax.swing.table.DefaultTableModel tabdayou=getDefaultTabeldayou();
     public static javax.swing.table.DefaultTableModel tabperfin=getDefaultTabelperfin();
     public static javax.swing.table.DefaultTableModel tabperfou=getDefaultTabelperfou();
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     public static javax.swing.JButton btnAttachment;
     public static javax.swing.JButton btnAttachment1;
     public static javax.swing.JButton btncall;
     private javax.swing.JButton btncomposemsg;
     private javax.swing.JButton btncussaveEmail;
     private javax.swing.JButton btncussaveFax;
     private javax.swing.JButton btncussavesms;
     private javax.swing.JButton btndelmsg;
     private javax.swing.JButton btndelmsg1;
     private javax.swing.JButton btndi;
     private javax.swing.JButton btndo;
     private javax.swing.JButton btnexportcall;
     private javax.swing.JButton btnexportcall1;
     private javax.swing.JButton btnexportcall2;
     private javax.swing.JButton btnexportcall3;
     private javax.swing.JButton btnexportcall4;
     private javax.swing.JButton btnexportcall5;
     private javax.swing.JButton btnexportcall6;
     private javax.swing.JButton btnexportmail;
     private javax.swing.JButton btnexportmail1;
     private javax.swing.JButton btnexportsms;
     private javax.swing.JButton btnexporttic;
     public static javax.swing.JButton btnfax;
     private javax.swing.JButton btnfinsrch;
     private javax.swing.JButton btnfoutsrch;
     private javax.swing.JButton btnhi;
     private javax.swing.JButton btnho;
     private javax.swing.JButton btninsrch;
     public static javax.swing.JButton btnlogout;
     public static javax.swing.JButton btnmail;
     private javax.swing.JButton btnmailinsrch;
     private javax.swing.JButton btnmailoutsrch;
     public static javax.swing.JButton btnoutbound;
     private javax.swing.JButton btnoutsrch;
     private javax.swing.JButton btnpi1;
     private javax.swing.JButton btnpo1;
     public static javax.swing.JButton btnready;
     private javax.swing.JButton btnrelease;
     private javax.swing.JButton btnrepcal;
     private javax.swing.JButton btnrepfax;
     private javax.swing.JButton btnreplymsg;
     private javax.swing.JButton btnrepmail;
     private javax.swing.JButton btnrepsms;
     private javax.swing.JButton btnreptic;
     public static javax.swing.JButton btnsenddept;
     public static javax.swing.JButton btnsms;
     private javax.swing.JButton btnsmsinsrch;
     private javax.swing.JButton btnsmsoutsrch;
     private javax.swing.JButton btnticsrch;
     private javax.swing.JComboBox cbFollowUp;
     private javax.swing.JComboBox cbFollowUp1;
     public static javax.swing.JComboBox cbagenin;
     private javax.swing.JComboBox cbagenirepcal;
     private javax.swing.JComboBox cbagenirepcal1;
     private javax.swing.JComboBox cbagenirepfax;
     private javax.swing.JComboBox cbagenou;
     private javax.swing.JComboBox cbagenrelease;
     private javax.swing.JComboBox cbagenrepmail;
     private javax.swing.JComboBox cbcaldir;
     private javax.swing.JComboBox cbcalstat;
     public static javax.swing.JComboBox cbcalstatin;
     private javax.swing.JComboBox cbcaltyperepcal;
     private javax.swing.JComboBox cbcate;
     private javax.swing.JComboBox cbcate1;
     public static javax.swing.JComboBox cbcust;
     public static javax.swing.JComboBox cbcust1;
     public static javax.swing.JComboBox cbcust2;
     private javax.swing.JComboBox cbdept;
     private javax.swing.JComboBox cbdept1;
     public static javax.swing.JComboBox cbdirection;
     private javax.swing.JComboBox cbdirfax;
     private javax.swing.JComboBox cbdirmail;
     private javax.swing.JComboBox cbdirrepsms;
     private javax.swing.JComboBox cbstatusrepfax;
     private javax.swing.JComboBox cbticstatus;
     private javax.swing.JComboBox cbticstatus1;
     private javax.swing.JCheckBox ckFCR;
     private javax.swing.JCheckBox ckFCR1;
     private javax.swing.JCheckBox ckassign;
     private javax.swing.JCheckBox ckassign1;
     private javax.swing.JCheckBox ckstoring;
     private javax.swing.JCheckBox ckstoring1;
     private javax.swing.JCheckBox cksubmit;
     private javax.swing.JCheckBox cksubmit1;
     private javax.swing.JCheckBox cktgl;
     private com.toedter.calendar.JDateChooser dccal1;
     private com.toedter.calendar.JDateChooser dccal2;
     private com.toedter.calendar.JDateChooser dcfax1;
     private com.toedter.calendar.JDateChooser dcfax2;
     private com.toedter.calendar.JDateChooser dcmail1;
     private com.toedter.calendar.JDateChooser dcmail2;
     private com.toedter.calendar.JDateChooser dcsms1;
     private com.toedter.calendar.JDateChooser dcsms2;
     private com.toedter.calendar.JDateChooser dctic1;
     private com.toedter.calendar.JDateChooser dctic2;
     public static com.toedter.calendar.JDateChooser dctic3;
     public static com.toedter.calendar.JDateChooser dctic4;
     public static com.toedter.calendar.JDateChooser dctic5;
     public static com.toedter.calendar.JDateChooser dctic6;
     public static com.toedter.calendar.JDateChooser dctic7;
     public static com.toedter.calendar.JDateChooser dctic8;
     private com.toedter.calendar.JDateChooser dtdi;
     private com.toedter.calendar.JDateChooser dtdi1;
     private com.toedter.calendar.JDateChooser dtdo;
     private com.toedter.calendar.JDateChooser dtdo1;
     public static com.toedter.calendar.JDateChooser dtfi;
     public static com.toedter.calendar.JDateChooser dtfi1;
     public static com.toedter.calendar.JDateChooser dtfo;
     public static com.toedter.calendar.JDateChooser dtfo1;
     private com.toedter.calendar.JDateChooser dthi;
     private com.toedter.calendar.JDateChooser dtho;
     public static com.toedter.calendar.JDateChooser dtmi;
     public static com.toedter.calendar.JDateChooser dtmi1;
     public static com.toedter.calendar.JDateChooser dtmo;
     public static com.toedter.calendar.JDateChooser dtmo1;
     public static com.toedter.calendar.JDateChooser dtmsi;
     public static com.toedter.calendar.JDateChooser dtmsi1;
     public static com.toedter.calendar.JDateChooser dtmso;
     public static com.toedter.calendar.JDateChooser dtmso1;
     private com.toedter.calendar.JDateChooser dtpi;
     private com.toedter.calendar.JDateChooser dtpi1;
     private com.toedter.calendar.JDateChooser dtpo;
     private com.toedter.calendar.JDateChooser dtpo1;
     public static com.toedter.calendar.JDateChooser dtsi;
     public static com.toedter.calendar.JDateChooser dtsi1;
     public static com.toedter.calendar.JDateChooser dtso;
     public static com.toedter.calendar.JDateChooser dtso1;
     private javax.swing.JButton jButton6;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel100;
     private javax.swing.JLabel jLabel101;
     private javax.swing.JLabel jLabel102;
     private javax.swing.JLabel jLabel103;
     private javax.swing.JLabel jLabel104;
     private javax.swing.JLabel jLabel105;
     private javax.swing.JLabel jLabel106;
     private javax.swing.JLabel jLabel107;
     private javax.swing.JLabel jLabel108;
     private javax.swing.JLabel jLabel109;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel110;
     private javax.swing.JLabel jLabel111;
     private javax.swing.JLabel jLabel112;
     private javax.swing.JLabel jLabel113;
     private javax.swing.JLabel jLabel114;
     private javax.swing.JLabel jLabel115;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel22;
     private javax.swing.JLabel jLabel23;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel29;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel30;
     private javax.swing.JLabel jLabel31;
     private javax.swing.JLabel jLabel32;
     private javax.swing.JLabel jLabel33;
     private javax.swing.JLabel jLabel34;
     private javax.swing.JLabel jLabel35;
     private javax.swing.JLabel jLabel36;
     private javax.swing.JLabel jLabel37;
     private javax.swing.JLabel jLabel38;
     private javax.swing.JLabel jLabel39;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel40;
     private javax.swing.JLabel jLabel41;
     private javax.swing.JLabel jLabel42;
     private javax.swing.JLabel jLabel43;
     private javax.swing.JLabel jLabel44;
     private javax.swing.JLabel jLabel45;
     private javax.swing.JLabel jLabel46;
     private javax.swing.JLabel jLabel47;
     private javax.swing.JLabel jLabel48;
     private javax.swing.JLabel jLabel49;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel50;
     private javax.swing.JLabel jLabel51;
     private javax.swing.JLabel jLabel52;
     private javax.swing.JLabel jLabel53;
     private javax.swing.JLabel jLabel54;
     private javax.swing.JLabel jLabel55;
     private javax.swing.JLabel jLabel56;
     private javax.swing.JLabel jLabel57;
     private javax.swing.JLabel jLabel58;
     private javax.swing.JLabel jLabel59;
     private javax.swing.JLabel jLabel60;
     private javax.swing.JLabel jLabel61;
     private javax.swing.JLabel jLabel62;
     private javax.swing.JLabel jLabel63;
     private javax.swing.JLabel jLabel64;
     private javax.swing.JLabel jLabel65;
     private javax.swing.JLabel jLabel66;
     private javax.swing.JLabel jLabel67;
     private javax.swing.JLabel jLabel68;
     private javax.swing.JLabel jLabel69;
     private javax.swing.JLabel jLabel70;
     private javax.swing.JLabel jLabel71;
     private javax.swing.JLabel jLabel72;
     private javax.swing.JLabel jLabel73;
     private javax.swing.JLabel jLabel74;
     private javax.swing.JLabel jLabel75;
     private javax.swing.JLabel jLabel76;
     private javax.swing.JLabel jLabel77;
     private javax.swing.JLabel jLabel78;
     private javax.swing.JLabel jLabel79;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel80;
     private javax.swing.JLabel jLabel81;
     private javax.swing.JLabel jLabel82;
     private javax.swing.JLabel jLabel83;
     private javax.swing.JLabel jLabel84;
     private javax.swing.JLabel jLabel85;
     private javax.swing.JLabel jLabel86;
     private javax.swing.JLabel jLabel87;
     private javax.swing.JLabel jLabel88;
     private javax.swing.JLabel jLabel89;
     private javax.swing.JLabel jLabel90;
     private javax.swing.JLabel jLabel91;
     private javax.swing.JLabel jLabel92;
     private javax.swing.JLabel jLabel93;
     private javax.swing.JLabel jLabel94;
     private javax.swing.JLabel jLabel95;
     private javax.swing.JLabel jLabel96;
     private javax.swing.JLabel jLabel97;
     private javax.swing.JLabel jLabel98;
     private javax.swing.JLabel jLabel99;
     public static javax.swing.JList jList2;
     public static javax.swing.JList jList3;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel14;
     private javax.swing.JPanel jPanel15;
     private javax.swing.JPanel jPanel16;
     private javax.swing.JPanel jPanel17;
     private javax.swing.JPanel jPanel19;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel21;
     private javax.swing.JPanel jPanel23;
     private javax.swing.JPanel jPanel24;
     private javax.swing.JPanel jPanel25;
     private javax.swing.JPanel jPanel26;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane10;
     private javax.swing.JScrollPane jScrollPane11;
     private javax.swing.JScrollPane jScrollPane12;
     private javax.swing.JScrollPane jScrollPane13;
     private javax.swing.JScrollPane jScrollPane14;
     private javax.swing.JScrollPane jScrollPane15;
     private javax.swing.JScrollPane jScrollPane16;
     private javax.swing.JScrollPane jScrollPane17;
     private javax.swing.JScrollPane jScrollPane18;
     private javax.swing.JScrollPane jScrollPane19;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane20;
     private javax.swing.JScrollPane jScrollPane21;
     private javax.swing.JScrollPane jScrollPane22;
     private javax.swing.JScrollPane jScrollPane23;
     private javax.swing.JScrollPane jScrollPane24;
     private javax.swing.JScrollPane jScrollPane25;
     private javax.swing.JScrollPane jScrollPane26;
     private javax.swing.JScrollPane jScrollPane27;
     private javax.swing.JScrollPane jScrollPane28;
     private javax.swing.JScrollPane jScrollPane29;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane30;
     private javax.swing.JScrollPane jScrollPane31;
     private javax.swing.JScrollPane jScrollPane32;
     private javax.swing.JScrollPane jScrollPane33;
     private javax.swing.JScrollPane jScrollPane34;
     private javax.swing.JScrollPane jScrollPane35;
     private javax.swing.JScrollPane jScrollPane36;
     private javax.swing.JScrollPane jScrollPane37;
     private javax.swing.JScrollPane jScrollPane38;
     private javax.swing.JScrollPane jScrollPane39;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane40;
     private javax.swing.JScrollPane jScrollPane41;
     private javax.swing.JScrollPane jScrollPane42;
     private javax.swing.JScrollPane jScrollPane43;
     private javax.swing.JScrollPane jScrollPane44;
     private javax.swing.JScrollPane jScrollPane45;
     private javax.swing.JScrollPane jScrollPane46;
     private javax.swing.JScrollPane jScrollPane47;
     private javax.swing.JScrollPane jScrollPane48;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTabbedPane jTabbedPane2;
     private javax.swing.JTabbedPane jTabbedPane3;
     private javax.swing.JTabbedPane jTabbedPane4;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextArea jTextArea10;
     private javax.swing.JTextArea jTextArea11;
     private javax.swing.JTextArea jTextArea12;
     private javax.swing.JTextArea jTextArea2;
     private javax.swing.JTextArea jTextArea3;
     private javax.swing.JTextArea jTextArea4;
     private javax.swing.JTextArea jTextArea5;
     private javax.swing.JTextArea jTextArea6;
     private javax.swing.JTextArea jTextArea7;
     private javax.swing.JTextArea jTextArea8;
     private javax.swing.JTextArea jTextArea9;
     private javax.swing.JDesktopPane jdp;
     private javax.swing.JTabbedPane jtab;
     private javax.swing.JLabel lblA1;
     private javax.swing.JLabel lblA2;
     private javax.swing.JLabel lblIn1;
     private javax.swing.JLabel lblIn2;
     private javax.swing.JLabel lblOu1;
     private javax.swing.JLabel lblOu2;
     public static javax.swing.JLabel lblactivity;
     private static javax.swing.JLabel lblcalincount;
     private static javax.swing.JLabel lblcaloutcount;
     private javax.swing.JLabel lbldate;
     private javax.swing.JLabel lbllogo;
     public static javax.swing.JLabel lblpas;
     private javax.swing.JLabel lblrepcalcount;
     private javax.swing.JLabel lblrepfaxcount;
     private javax.swing.JLabel lblrepmailcount;
     private javax.swing.JLabel lblrepsmscount;
     private javax.swing.JLabel lblrepticcount;
     private javax.swing.JLabel lblrepticcount1;
     private javax.swing.JLabel lblrepticcount10;
     private javax.swing.JLabel lblrepticcount11;
     private javax.swing.JLabel lblrepticcount12;
     private javax.swing.JLabel lblrepticcount3;
     private javax.swing.JLabel lblrepticcount5;
     private javax.swing.JLabel lblrepticcount7;
     private javax.swing.JLabel lblrepticcount9;
     private javax.swing.JLabel lblscroll;
     public static javax.swing.JLabel lblshift;
     public static javax.swing.JLabel lblshift1;
     private static javax.swing.JLabel lblticcount;
     public static javax.swing.JLabel lbluser;
     public static javax.swing.JLabel lblview;
     public static javax.swing.JLabel lblview1;
     private javax.swing.JTabbedPane panelfax;
     private javax.swing.JTabbedPane panelmail;
     private javax.swing.JTabbedPane panelsms;
     private javax.swing.JPanel pninbox;
     private javax.swing.JPanel pninbox1;
     private javax.swing.JPanel pnlDayOu;
     private javax.swing.JPanel pnlHoOu;
     private javax.swing.JPanel pnlPerfOu;
     private javax.swing.JTabbedPane pnlRepHidden;
     private javax.swing.JPanel pnlact;
     private javax.swing.JPanel pnlinbon;
     private javax.swing.JTabbedPane pnlinf;
     private javax.swing.JPanel pnlou;
     private javax.swing.JPanel pnlrep;
     private javax.swing.JPanel pnlrep1;
     private javax.swing.JPanel pnlrep2;
     private javax.swing.JPanel pnlrep3;
     private javax.swing.JPanel pnlrepFax;
     private javax.swing.JPanel pnlscroll;
     private javax.swing.JPanel pnltic;
     private javax.swing.JPanel pnoutbox;
     private javax.swing.JPanel pnoutbox1;
     private javax.swing.JScrollPane scpCcList1;
     private javax.swing.JScrollPane scpCcList2;
     private javax.swing.JTabbedPane tabbpanereport;
     private javax.swing.JTable tblact;
     private javax.swing.JTable tbldailyin;
     private javax.swing.JTable tbldailyout;
     private javax.swing.JTable tblfin;
     private javax.swing.JTable tblfou;
     private javax.swing.JTable tblhourin;
     private javax.swing.JTable tblhourout;
     public static javax.swing.JTable tblin;
     private javax.swing.JTable tblmin;
     private javax.swing.JTable tblmou;
     private javax.swing.JTable tblmsin;
     private javax.swing.JTable tblmsou;
     private javax.swing.JTable tblout;
     private javax.swing.JTable tblperformin;
     private javax.swing.JTable tblperformout;
     private javax.swing.JTable tblrepcal;
     private javax.swing.JTable tblrepfax;
     private javax.swing.JTable tblrepmail;
     private javax.swing.JTable tblrepsms;
     private javax.swing.JTable tblreptic;
     private javax.swing.JTable tblsin;
     private javax.swing.JTable tblsou;
     public static javax.swing.JTable tbltic;
     private javax.swing.JTable tblticconf;
     public static javax.swing.JTextField txtcalnoti;
     private javax.swing.JTextField txtcus;
     private javax.swing.JTextField txtcus1;
     private javax.swing.JTextArea txtdetail;
     private javax.swing.JTextField txtdriv;
     private javax.swing.JTextField txtdriv1;
     private javax.swing.JTextField txtdrivcode;
     private javax.swing.JTextField txtdrivcode1;
     private javax.swing.JTextField txtfaxfinm;
     public static javax.swing.JTextField txtfaxnoti;
     private javax.swing.JTextField txtfrom;
     private javax.swing.JTextField txtfrom1;
     private javax.swing.JTextField txtfrom2;
     private javax.swing.JTextField txtidti;
     private javax.swing.JTextArea txtimsg;
     private javax.swing.JTextArea txtimsg1;
     private javax.swing.JTextArea txtimsg2;
     private javax.swing.JTextArea txtimsg3;
     private javax.swing.JTextArea txtimsg4;
     private javax.swing.JTextField txtisu;
     public static javax.swing.JTextField txtmailnoti;
     private javax.swing.JTextField txtmailsub;
     private javax.swing.JTextField txtmailticid;
     public static javax.swing.JTextField txtnoticfax;
     public static javax.swing.JTextField txtnoticmail;
     public static javax.swing.JTextField txtnoticsms;
     private javax.swing.JTextField txtocc;
     private javax.swing.JTextArea txtomsg;
     private javax.swing.JTextField txtosu;
     private javax.swing.JTextField txtoto;
     private javax.swing.JTextField txtplatno;
     private javax.swing.JTextField txtplatno1;
     public static javax.swing.JTextField txtsmsnoti;
     private javax.swing.JTextField txtsmsstat;
     private javax.swing.JTextField txtsmsticid;
     private javax.swing.JTextArea txtsolution;
     private javax.swing.JTextField txtticno1;
     private javax.swing.JTextField txtticno2;
     // End of variables declaration//GEN-END:variables
 
     public static String sql;
     public static String sql1;
     public static String sql2;
     public static String sql3;
     public static String sql4;
     public static String sql5;
     public static String sql6;
     public static String sql7;
     public static String sql8;
     public static String sql9;
     public static String sql10;
     public static String sql11;
     public static String sql12;
     public static String sql13;
     public static String sql14;
     public static String sql15;
     public static String sqlid;
     public static String sqlld;
     public static String sqllt;
     public static String condition;
     public static ResultSet rs;
     public static ResultSet rs1;
     public static JavaConnector jconn=new JavaConnector();
     public static Connection conn;
     private Timer dateTimer;
     //private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy, HH:mm:ss");
     private long startTime = 0;
     private long stopTime = 0;
     private boolean running = false;
 
     private void tabelticconf(){
         tabticconf.setRowCount(0);
              try{
                  sql="select tickets.*" +
                       ", _department.dept_name as asdept" +
                       ", _ticketstatus.data as ticstat" +
                       " from tickets" +
                       " left join _department on tickets.assign_dept=_department.dept_id" +
                       " left join _ticketstatus on tickets._status=_ticketstatus.code" +
                       " where confirm=1 and confirm_by=0 and confirmed=1 order by ticket_no";
               rs=jconn.SQLExecuteRS(sql, conn);
 
             while(rs.next()){
                 tic[0]=rs.getString("ticket_no");
                 tic[1]=rs.getString("confirm_username");
                 tic[2]=rs.getString("ticstat");
                 tic[3]=rs.getString("category");
                 tic[4]=rs.getString("asdept");
                 tic[5]=rs.getString("assign_username");
                 tic[6]=rs.getString("cust_name");
                 tic[7]=rs.getString("cust_phone");
                 tic[8]=rs.getString("user");
                 tic[9]=rs.getString("vehicle_platno");
                 tic[10]=rs.getString("vehicle_type");
                 tic[11]=rs.getString("driver_name");
                 tic[12]=rs.getString("driver_phone");
                 tic[13]=rs.getString("ticket_id");
                 tabticconf.addRow(tic);
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }              
 
     private void tabelact(){
         tabact.setRowCount(0);
              try{
              int row=0;
               sql="select username, _userlevel.data, _activity.data, info, login_time, host_addr, line_number from user_account join _activity on user_account._activity=_activity.code join _userlevel on user_account._level=_userlevel.code order by username";
               rs=jconn.SQLExecuteRS(sql, conn);
 //              System.out.println(sql);
 
             while(rs.next()){
                 act[0]=rs.getString(1);
                 act[1]=rs.getString(2);
                 act[2]=rs.getString(3);
                 act[3]=rs.getString(4);
                 act[4]=rs.getString(5);
                 act[5]=rs.getString(6);
                 act[6]=rs.getString(7);
                 tabact.addRow(act);
             }
 
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }    
     
     public static void tabelmsin() {
         try{
             tabmsin.setRowCount(0);
             int row=0;
             Date dt1 =dtmsi.getDate();
             Date dt2 =dtmsi1.getDate();
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             msin = sdf.format(dt1);
             msin1= sdf.format(dt2);
             sql="select * " +
                     "from msg_inbox " +
                     "where msg_date between '"+msin+"' and '"+msin1+"' and _erased=0 and msg_to = '"+lbluser.getText()+"' order by msg_id";
             rs=jconn.SQLExecuteRS(sql, conn);
 //            System.out.println(sql);
 
             while(rs.next()){
                 msn[0]=rs.getString("msg_date");
                 msn[1]=rs.getString("msg_time");
                 msn[2]=rs.getString("msg_from");
                 msn[3]=rs.getString("_read");
                 msn[4]=rs.getString("msg_text");
                 msn[5]=rs.getString("msg_id");
                 tabmsin.addRow(msn);
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     public static void tabelmsou()        {
         try{
             tabmsou.setRowCount(0);
             int row=0;
             Date dt1 =dtmso.getDate();
             Date dt2 =dtmso1.getDate();
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             msou = sdf.format(dt1);
             msou1= sdf.format(dt2);
             sql="select * " +
                     "from msg_outbox " +
                     "where msg_date between '"+msou+"' and '"+msou1+"' and _erased=0 and msg_from = '"+lbluser.getText()+"' order by msg_id";
             rs=jconn.SQLExecuteRS(sql, conn);
 //            System.out.println(sql);
             while(rs.next()){
                 msu[0]=rs.getString("msg_date");
                 msu[1]=rs.getString("msg_time");
                 msu[2]=rs.getString("msg_to");
                 msu[3]=rs.getString("msg_text");
                 msu[4]=rs.getString("msg_id");
                 tabmsou.addRow(msu);
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     int a=0;
     private void blink(){
 //        try {             
             if(!lblA2.getText().equals("0")){                
                 if(a==0){
                     lblA2.setForeground(Color.blue);a++;
                 }else{
                     lblA2.setForeground(Color.red);a=0;
                 }
             }else{
                 lblA2.setForeground(Color.red);
             }       
 //        } catch (InterruptedException ex) {
 //            
 //        }
     }
     Action activ = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             tabelact();
         }
     };
            
     Action dating = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             ambilTgl();
             ambilMonitor();
         }
     };
     Action blinking = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             blink();
         }
     };
     int pjg=750;
     Action tiscrol = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {            
             pjg--;
             if(pjg<-tp){
                 pjg=750;
             }
             lblscroll.setLocation(pjg,0);
 //                if(lblscroll.LEFT > -lblscroll.WIDTH){
 //                    lblscroll.setLocation(pjg,0);
 //                }
         }
     };
     int tp;
     Action inbound = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {                
             try {
                 String come;
                 String[] tcp = new String[20];
                 String conf;
                 int p;
                 int index;
                 if (inbroad.ready()) {
                     come = inbroad.readLine();
 //                    System.out.print("\nframe: " + come + "\n");
                     index = come.indexOf(':');
                     p = come.length();
                     if (index >= 0) {
 //                        System.out.print("\n ada titik dua\n");
                         tcp[0] = come.substring(index + 1, p);
 //                        System.out.print("\n tcp[0] (stlh buang :): " + tcp[0] + "\n");
                         if (tcp[0].length() != 0) {
                             index = tcp[0].indexOf('|');
                             p = tcp[0].length();
                             if (index != -1) {
                                 tcp[1] = tcp[0].substring(0, index);
                                 System.out.print("\n keyword " + tcp[1] + "\n");
                                 tcp[2] = tcp[0].substring(index + 1, p);
                                 System.out.print("\n String1 " + tcp[2] + "\n");
                                 if (tcp[2].length() != 0) {
                                     index = tcp[2].indexOf('|');
                                     p = tcp[2].length();
                                     if (index != -1) {
                                         tcp[3] = tcp[2].substring(0, index);
                                         System.out.print("\nisi data1 " + tcp[3] + "\n");
                                         tcp[4] = tcp[2].substring(index + 1, p);
                                         System.out.print("\nisi String2 " + tcp[4] + "\n");
                                         if (tcp[4].length() != 0) {
                                             index = tcp[4].indexOf('|');
                                             p = tcp[4].length();
                                             if (index != -1) {
                                                 tcp[5] = tcp[4].substring(0, index);
                                                 System.out.print("\nisi data2 " + tcp[5] + "\n");
                                                 tcp[6] = tcp[4].substring(index + 1, p);
                                                 System.out.print("\nisi String3 " + tcp[6] + "\n");
                                                 if (tcp[6].length() != 0) {
                                                     index = tcp[6].indexOf('|');
                                                     p = tcp[6].length();
                                                     if (index != -1) {
                                                         tcp[7] = tcp[6].substring(0, index);
                                                         System.out.print("\nisi data3 " + tcp[7] + "\n");
                                                         tcp[8] = tcp[6].substring(index + 1, p);
                                                         System.out.print("\nisi String4 " + tcp[8] + "\n");
                                                         if (tcp[8].length() != 0) {
                                                             index = tcp[8].indexOf('|');
                                                             p = tcp[8].length();
                                                             if (index != -1) {
                                                                 tcp[9] = tcp[8].substring(0, index);
                                                                 System.out.print("\nisi data4 " + tcp[9] + "\n");
                                                                 tcp[10] = tcp[8].substring(index + 1, p);
                                                                 System.out.print("\nisi String5 " + tcp[10] + "\n");
                                                             }else{
                                                                 tcp[9] = tcp[8].substring(0, p);
                                                                 System.out.print("\n data4: " + tcp[9] + "\n");
                                                             }
                                                         }
                                                     }else{
                                                         tcp[7] = tcp[6].substring(0, p);
                                                         System.out.print("\n data3: " + tcp[7] + "\n");
                                                     }
                                                 }
                                             }else{
                                                 tcp[5] = tcp[4].substring(0, p);
                                                 System.out.print("\n data2: " + tcp[5] + "\n");
                                             }
                                         }
                                     }else{
                                         tcp[3] = tcp[2].substring(0, p);
                                         System.out.print("\n data1: " + tcp[3] + "\n");
                                     }
                                 }
                             } else {
                                 tcp[1] = tcp[0].substring(0, p);
                                 System.out.print("\n keyword: " + tcp[1] + "\n");
                             }
                         } else {
                             System.out.print("\n ga da keyword\n");
                         }
                     } else {
                         System.out.print("\n ga da : ny\n");
                     }
                     if (tcp[0].length() != 0 && tcp[3] != null) {
                         if (tcp[1].equals("MSG")&&tcp[3].equals(lbluser.getText())) {
                             tt++;
                             oldtext = lblscroll.getText();
                             if(oldtext.equals("")){
                                 lblscroll.setText("1 message received");
                                 newtext = lblscroll.getText();
                                 tp=newtext.length()*8;
                                 lblscroll.setSize(tp, 20);
                                 Scrol.start();
                             }else{
                                 oldtext=oldtext.replaceAll("1 message received", "");
                                 lblscroll.setText(tt+" messages received ,"+oldtext);
                                 newtext = lblscroll.getText();
                                 tp=newtext.length()*8;
                                 lblscroll.setSize(tp, 20);
                             }
                         }
                     }
                     if (tcp[0].length() != 0 && tcp[3] != null) {
                         System.out.print("\n panjang tcp 3 = "+tcp[3].length());
                         if (tcp[1].equals("TICKET")||tcp[3].equals("CONFIRM")) {
 //                            System.out.print("\n" + tcp[0] + "\n");
                             sql = "select count(*) from tickets where _status=2 and confirm=1 and confirm_by=0 and confirm_username is null and confirmed=0";
                             rs = jconn.SQLExecuteRS(sql, conn);
                             try {
                                 while (rs.next()) {
                                     c = Integer.parseInt(rs.getString(1));
                                 }
                             } catch (SQLException ex) {
                                 Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
                             }
                             btnoutbound.setEnabled(true);
                             txtcalnoti.setText(String.valueOf(c));
                             if(c==0){
                                 btnoutbound.setEnabled(false);
                                 txtcalnoti.setText("");
                             }else{
                                 btnoutbound.setEnabled(true);
                             }
 //                            JOptionPane.showMessageDialog(null, "YOU'VE GOT " + c + " OUTBOUND CALL TO COMFIRM", "OUTBOUND CONFIRMATION", JOptionPane.WARNING_MESSAGE);
                         }
                         if (tcp[1].equals("MAIL")||tcp[3].equals("INBOUND")) {
 //                            System.out.print("\n" + tcp[0] + "\n");
                             sql = "select count(*) from log_mail where direction=0 and status=0";
                             rs = jconn.SQLExecuteRS(sql, conn);
                             try {
                                 while (rs.next()) {
                                     m = Integer.parseInt(rs.getString(1));
 //                                    m++;
                                 }
                             } catch (SQLException ex) {
                                 Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
                             }
 //                            tabelticconf();
                             btnmail.setEnabled(true);
                             txtmailnoti.setText(String.valueOf(m));
                             if(m==0){
                                 btnmail.setEnabled(false);
                                 txtmailnoti.setText("");
                             }else{
                                 btnmail.setEnabled(true);
                             }
 //                            JOptionPane.showMessageDialog(null, "YOU'VE GOT " + m + " INBOUND MAIL TO COMFIRM", "INBOUND CONFIRMATION", JOptionPane.WARNING_MESSAGE);
                         }
                         if (tcp[1].equals("SMS")||tcp[3].equals("INBOUND")) {
 //                            System.out.print("\n" + tcp[0] + "\n");
                             sql = "select count(*) from log_sms where _direction=0 and _status=0";
                             rs = jconn.SQLExecuteRS(sql, conn);
                             try {
                                 while (rs.next()) {
                                     sm = Integer.parseInt(rs.getString(1));
 //                                    sm++;
                                 }
                             } catch (SQLException ex) {
                                 Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
                             }
 //                            tabelticconf();
                             txtsmsnoti.setText(String.valueOf(sm));
                             btnsms.setEnabled(true);
                             if(sm==0){
                                 btnsms.setEnabled(false);
                                 txtsmsnoti.setText("");
                             }else{
                                 btnsms.setEnabled(true);
                             }
 //                            JOptionPane.showMessageDialog(null, "YOU'VE GOT " + sm + " INBOUND SMS ", "INBOUND CONFIRMATION", JOptionPane.WARNING_MESSAGE);
                         }
                         if (tcp[1].equals("FAX")||tcp[3].equals("INBOUND")) {
 //                            System.out.print("\n" + tcp[0] + "\n");
                             sql = "select count(*) from log_fax where _direction=0 and _status=0";
                             rs = jconn.SQLExecuteRS(sql, conn);
                             try {
                                 while (rs.next()) {
                                     fx = Integer.parseInt(rs.getString(1));
 //                                    sm++;
                                 }
                             } catch (SQLException ex) {
                                 Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
                             }
 //                            tabelticconf();
                             txtfaxnoti.setText(String.valueOf(fx));
                             btnsms.setEnabled(true);
                             if(fx==0){
                                 btnfax.setEnabled(false);
                                 txtfaxnoti.setText("");
                             }else{
                                 btnfax.setEnabled(true);
                             }
 //                            JOptionPane.showMessageDialog(null, "YOU'VE GOT " + sm + " INBOUND SMS ", "INBOUND CONFIRMATION", JOptionPane.WARNING_MESSAGE);
                         }
                         if (tcp[1].equals("SMS")||tcp[3].equals("UPDATE")) {
                             sms();
                         }
                         if (tcp[1].equals("EMAIL")||tcp[3].equals("UPDATE")) {
                             mail();
                         }
                         if (tcp[1].equals("FAX")||tcp[3].equals("UPDATE")) {
                             fax();
                         }
 //                        TICKET|ASSIGN|"+deptid+"|"+id+"\r\n            
                         if(tcp[5] != null ){
 //                            if (tcp[1].equals("TICKET")&&tcp[3].equals("ASSIGN")&&tcp[5].equals("0")) {
                             if (tcp[1].equals("TICKET")){
                                 if(tcp[3].equals("ASSIGN")&&tcp[5].equals("0")){
                                     oldtext = lblscroll.getText();
                                     if(oldtext.equals("")){
                                         lblscroll.setText("[Ticket No. "+tcp[7]+"] Assigned to your department");
                                         newtext = lblscroll.getText();
                                         tp=newtext.length()*6;
                                         System.out.print("isi tp : "+tp);
                                         lblscroll.setSize(tp, 20);
                                         Scrol.start();
                                     }else{
                                         lblscroll.setText("[Ticket No. "+tcp[7]+"] Assigned to your department, "+oldtext);
                                         newtext = lblscroll.getText();
                                         tp=newtext.length()*6;
                                         System.out.print("isi tp : "+tp);
                                         lblscroll.setSize(tp, 20);
                                     }
                                 }else if(tcp[3].equals("UPDATE")&&tcp[5].equals("0")){
                                     oldtext = lblscroll.getText();
                                     if(oldtext.equals("")){
                                         lblscroll.setText("[Ticket No. "+tcp[7]+"] Updated");
                                         newtext = lblscroll.getText();
                                         tp=newtext.length()*6;
                                         System.out.print("isi tp : "+tp);
                                         lblscroll.setSize(tp, 20);
                                         Scrol.start();
                                     }else{
                                         lblscroll.setText("[Ticket No. "+tcp[7]+"] Updated, "+oldtext);
                                         newtext = lblscroll.getText();
                                         tp=newtext.length()*6;
                                         System.out.print("isi tp : "+tp);
                                         lblscroll.setSize(tp, 20);
                                     }
                                 }
 //                                tt++;
 //                                JOptionPane.showMessageDialog(null, "YOU'VE GOT " + tt + " TICKET ", "TICKET CONFIRMATION", JOptionPane.WARNING_MESSAGE);
                             }
                         }
                         
                     }
                 }
             } catch (IOException ex) {
                 Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     };
     private void ambilTgl(){
         GregorianCalendar now=new GregorianCalendar();
         int tgl=now.get(now.DATE);
         int bln=now.get(now.MONTH)+1;
         int thn=now.get(now.YEAR);
         int h=now.get(now.HOUR);
         int m=now.get(now.MINUTE);
         int s=now.get(now.SECOND);
     
         lbldate.setText(String.valueOf(thn)+"-"+String.valueOf(bln)+"-"+String.valueOf(tgl)+"  "+String.valueOf(h)+":"+String.valueOf(m)+":"+String.valueOf(s));
     }
     private void ambilMonitor(){
         try{
             sql="select queue from monitorings";
             rs=jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 lblA2.setText(rs.getString(1));
             }
             
             sql1="SELECT login,ready,InOnline,OuOnline, SUM(login-ready-InOnline-OuOnline) AS others"
                     + " FROM ("
                     + " select "
                     + " (select count(username) from user_account where _activity>=1 and dept_id=0) as 'login'"
                     + ",(select count(username) from user_account where _activity=4 and dept_id=0) as 'ready'"
                     + ",(select count(username) from user_account where _activity=3 and _mode=0 and dept_id=0) as 'InOnline'"
                     + ",(select count(username) from user_account where _activity=3 and _mode=1 and dept_id=0) as 'OuOnline'"
                     + ") AS details group by login";
             rs1=jconn.SQLExecuteRS(sql1, conn);
             while(rs1.next()){
                 lblA1.setText(rs1.getString(1));
                 lblIn1.setText(rs1.getString(2));
                 lblIn2.setText(rs1.getString(3));
                 lblOu1.setText(rs1.getString(4));
                 lblOu2.setText(rs1.getString(5));
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
      
     public void dataregis(){
           try{
              int row=0;
               sql="select pabx_host, inbound_ext, outbound_ext from user_account where username='"+lbluser.getText()+"'";
               rs=jconn.SQLExecuteRS(sql, conn);
 //              System.out.println(sql);
 
             while(rs.next()){
                 pabx=rs.getString(1);
                 in_ext=rs.getString(2);
                 out_ext=rs.getString(3);
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     String ring;
     String come;
     String tcp[]=new String[20];
     String conf;
     
          Action testing = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {                                
             try {
                 if (intele.ready()) {
                     int p;
                     int index;
                     ring = intele.readLine();
                     if((ring.substring(0,12).equals("DISCONNECTED") )) {
                         stop();
                         delay();
                         if(elapsed<=5){
                            sql="update log_phone set abandon='"+elapsed+"', _callstatus=-1 where log_id='"+loid+"'";
                             jconn.SQLExecute(sql, conn);
                         }else{
                            sql="update log_phone set abandon='"+elapsed+"', _callstatus=0 where log_id='"+loid+"'";
                             jconn.SQLExecute(sql, conn);
                         }
                         btncall.setEnabled(false);
                         btnready.setEnabled(true);
                         btncall.setDebugGraphicsOptions(v);
                         JOptionPane.showMessageDialog(null, "CALL DISCONNECTED", "INCOMING CALL",JOptionPane.WARNING_MESSAGE);
                     }else if ((ring.substring(0,7).equals("RINGING") )) {
                             start();
                             p=ring.length();
                             callid=ring.substring(8,p);
                             show();
                             toFront();                            
 //                            JOptionPane.showMessageDialog(null, callid +"\n"+"CALLING", "INCOMING CALL",JOptionPane.WARNING_MESSAGE);
 //                            int i=JOptionPane.showConfirmDialog(null, callid +"\n"+"CALLING", "INCOMING CALL",JOptionPane.YES_NO_OPTION);
                             btncall.setEnabled(true);
 //                            btncall.setBackground(Color.RED);
 //                            btncall.setIcon(frameIcon);
                             System.out.print("udah nyampe testing"+ ring);
                             try{
                                 sqllt="select CURRENT_TIME";
                                 rs = jconn.SQLExecuteRS(sqllt, conn);
                                 while(rs.next()){
                                     lt=rs.getString(1);
                                 }
 //                                System.out.println(lt);
 
                                   sql="insert into log_phone (log_date,log_time,username,shift,host_addr,line_number,_direction,_callstatus,caller_number) values ('"+ld+"','"+lt+"','"+lbluser.getText()+"','"+Log.cbshift.getSelectedIndex()+"','"+Log.data[2]+"','"+in_ext+"',0,0,'"+callid+"')";
                                   jconn.SQLExecute(sql, conn);
 //                                  System.out.println(sql);
                                   btncall.setEnabled(true);
 
                                   sqlid="select distinct last_insert_id() from log_phone";
                                   rs=jconn.SQLExecuteRS(sqlid, conn);
                                   while (rs.next()) {
                                     loid = rs.getString(1);
                                   }
                                   btncall.setEnabled(true);
                                   btnready.setEnabled(false);
                             }catch(Exception exc){
                                 System.err.println(exc.getMessage());
                             }
                         }else{
                             btncall.setEnabled(false);
                             btnready.setEnabled(true);
                             btncall.setDebugGraphicsOptions(v);
                         }                    
                 }
                 
             } catch (IOException ex) {
                 Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
             }
             }        
        };
 
 
        public void start() {
         this.startTime = System.currentTimeMillis();
     }
 
     
     public void stop() {
         this.stopTime = System.currentTimeMillis();
     }
 
     
     //elaspsed time in milliseconds
     private void currentdate() {
         try {
             sqlld = "select CURRENT_DATE";
             rs = jconn.SQLExecuteRS(sqlld, conn);
             while (rs.next()) {
                 ld = rs.getString(1);
             }
             System.out.println(ld);
         } catch (SQLException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     
     //elaspsed time in seconds
     public long delay() {
         if (running) {
             elapsed = ((System.currentTimeMillis() - startTime) / 1000);
         }
         else {
             elapsed = ((stopTime - startTime) / 1000);
 
         //System.out.print(elapsed);
         }
         return elapsed;
     }
 
     public static void call(){
         try {
             sql="select count(*) from tickets where _status=2 and confirm=1 and confirm_by=0 and confirm_username is null and confirmed=0";
             rs=jconn.SQLExecuteRS(sql, conn);
             while (rs.next()) {
                 c = Integer.parseInt(rs.getString(1));
             }
             txtcalnoti.setText(String.valueOf(c));
             if(c==0){
                 btnoutbound.setEnabled(false);
                 txtcalnoti.setText("");
             }else{
                 btnoutbound.setEnabled(true);
             }
         } catch (SQLException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public static void sms(){
         try {
             sql="select count(*) from log_sms where _direction=0 and _status=0";
             rs=jconn.SQLExecuteRS(sql, conn);
             while (rs.next()) {
                 sm = Integer.parseInt(rs.getString(1));
             }
             if(sm==0){
                 btnsms.setEnabled(false);
                 txtsmsnoti.setText("");
             }else{
                 btnsms.setEnabled(true);
                 txtsmsnoti.setText(String.valueOf(sm));
             }
         } catch (SQLException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public static void mail(){
         try {
             sql="select count(*) from log_mail where direction=0 and status=0";
             rs=jconn.SQLExecuteRS(sql, conn);
             while (rs.next()) {
                 m = Integer.parseInt(rs.getString(1));
             }            
             if(m==0){
                 btnmail.setEnabled(false);
                 txtmailnoti.setText("");
             }else{
                 btnmail.setEnabled(true);
                 txtmailnoti.setText(String.valueOf(m));
             }
         } catch (SQLException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public static void fax(){
         try {
             sql="select count(*) from log_fax where _direction=0 and _status=0";
             rs=jconn.SQLExecuteRS(sql, conn);
             while (rs.next()) {
                 fx = Integer.parseInt(rs.getString(1));
             }            
             if(fx==0){
                 btnfax.setEnabled(false);
                 txtfaxnoti.setText("");
             }else{
                 btnfax.setEnabled(true);
                 txtfaxnoti.setText(String.valueOf(fx));
             }
         } catch (SQLException ex) {
             Logger.getLogger(ContactCenterTunas.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     String optm, opdt;
     private void optm(){
         try{
             sql="select CURRENT_TIME";
             rs = jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 optm=rs.getString(1);
             }
             //            txtcalbac.setText(optm);
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     private void opdt(){
         try{
             sql="select CURRENT_DATE";
             rs = jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 opdt=rs.getString(1);
             }
         }catch(Exception exc){
             System.err.println(exc.getMessage());
         }
     }
     private void usr(){
         cbagenrelease.removeAllItems();        cbagenin.removeAllItems();        cbagenirepcal.removeAllItems();
         cbagenirepcal1.removeAllItems();        cbagenirepfax.removeAllItems();        cbagenou.removeAllItems();
         cbagenrepmail.removeAllItems();
 
         cbagenrelease.addItem("--");        cbagenin.addItem("--");        cbagenirepcal.addItem("--");
         cbagenirepcal1.addItem("--");        cbagenirepfax.addItem("--");        cbagenou.addItem("--");
         cbagenrepmail.addItem("--");
         try{
             sql="select username from user_account where dept_id=0" ;
             rs=jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 cbagenrelease.addItem(rs.getString(1));
                 cbagenin.addItem(rs.getString(1));
                 cbagenirepcal.addItem(rs.getString(1));
                 cbagenirepcal1.addItem(rs.getString(1));
                 cbagenirepfax.addItem(rs.getString(1));
                 cbagenou.addItem(rs.getString(1));
                 cbagenrepmail.addItem(rs.getString(1));
             }
         }catch(Exception e){
             System.out.print(e);
         }
         cbagenrelease.setSelectedItem("--");        cbagenin.setSelectedItem("--");        cbagenirepcal.setSelectedItem("--");
         cbagenirepcal1.setSelectedItem("--");        cbagenirepfax.setSelectedItem("--");        cbagenou.setSelectedItem("--");
         cbagenrepmail.setSelectedItem("--");
     }
     private void showStatus(){
         try{
             cbticstatus.removeAllItems();
             cbticstatus1.removeAllItems();
 
             sql="select data from _ticketstatus where code !=-1 order by code";
             rs=Log.jconn.SQLExecuteRS(sql,Log.conn);
             while(rs.next()){
                 cbticstatus.addItem(rs.getString(1));
                 cbticstatus1.addItem(rs.getString(1));
             }
             cbticstatus.addItem("--");
             cbticstatus1.addItem("--");
             cbticstatus.setSelectedItem("--");
             cbticstatus1.setSelectedItem("--");
         }catch(Exception e){
             System.out.println(e);
         }
     }
     private void showDept(){
         try{
             cbdept.removeAllItems();cbdept1.removeAllItems();
 
             sql="select dept_name from _department where _deleted=0 order by dept_id";
             rs=Log.jconn.SQLExecuteRS(sql,Log.conn);
             while(rs.next()){
                 cbdept.addItem(rs.getString(1));
                 cbdept1.addItem(rs.getString(1));
             }
             cbdept.addItem("--");
             cbdept1.addItem("--");
             cbdept.setSelectedItem("--");cbdept1.setSelectedItem("--");
         }catch(Exception e){
             System.out.println(e);
         }
     }
     private void usrlvl(){
         try{
             sql="select _level from user_account where username='"+Log.txtnm.getText()+"'";
             rs=jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 usrlvl=Integer.parseInt(rs.getString(1));
             }
         }catch(Exception e){
             System.out.print(e);
         }
     }
     private void followUp(){
         cbFollowUp.removeAllItems();
         cbFollowUp.addItem("--");
         try{
             sql="select data from _followup ";
             rs=jconn.SQLExecuteRS(sql, conn);
             while(rs.next()){
                 cbFollowUp.addItem(rs.getString(1));
             }
         }catch(Exception e){
             System.out.print(e);
         }
         cbFollowUp.setSelectedItem("--");
     }
     private void showCust(){
         try{
             cbcust.removeAllItems();cbcust1.removeAllItems();cbcust2.removeAllItems();
             cbcust.addItem("Others");cbcust1.addItem("Others");cbcust2.addItem("Others");
 
             sql="select distinct(fullname) from customers";
             rs=jconn.SQLExecuteRS(sql,conn);
             while(rs.next()){
                 cbcust.addItem(rs.getString(1));
                 cbcust1.addItem(rs.getString(1));
                 cbcust2.addItem(rs.getString(1));
             }            
             cbcust.setSelectedIndex(-1);cbcust1.setSelectedIndex(-1);cbcust2.setSelectedIndex(-1);
         }catch(Exception e){
             System.out.println(e);
 
         }
     }
     public static void connecttele()  {
          try {
              // If guest, try to connect to the server
              sockettele = new Socket(IPtele, porttele);
              intele = new BufferedReader(new
                      InputStreamReader(sockettele.getInputStream()));
              outtele = new PrintWriter(sockettele.getOutputStream(), true);
              //               System.out.print(socket);
              teleOn=true;
          }
          // If error, clean up and output an error message
          catch (IOException e) {
              int i=JOptionPane.showConfirmDialog(null,"Telephony did not work\n\nPlease activate your telephony application","Activate Telepohony",JOptionPane.YES_NO_OPTION);
              if (i==JOptionPane.YES_OPTION){
                  connecttele();
              }else{
                  teleOn=false;
                  sql="update user_account set _activity=0 where username= '" +Log.data[0]+ "' limit 1";
                  jconn.SQLExecute(sql, conn);
                  try {
                      Thread.sleep(5000);
                  } catch (InterruptedException ex) {
                      //                     Logger.getLogger(home.class.getName()).log(Level.SEVERE, null, ex);
                  }
                  System.exit(0);
              }
          }
      }
 
     public static int bc=0;
     public static void connect()  {
         try {
             bc++;
             socketbroad = new Socket(IPbroad, portbroad);
             inbroad = new BufferedReader(new
                     InputStreamReader(socketbroad.getInputStream()));
             outbroad = new PrintWriter(socketbroad.getOutputStream(), true);
             msg.start();
             brcaOn=true;
 //               System.out.print(socket1);
             }
             // If error, clean up and output an error message
             catch (IOException e) {
                 cleanbroad();
                 brcaOn=false;
                 if(bc==1000||bc==1){
                     JOptionPane.showMessageDialog(null,"Broadcaster didnt work...");
                     connect();
                     if(bc==1000){
                         bc=0;
                     };
                 }
                 bc++;
             }
     }
     public static void connectuploder()  {
         try {
             socketupload = new Socket("localhost", 6021);
             inupload = new BufferedReader(new
                     InputStreamReader(socketupload.getInputStream()));
             outupload = new PrintWriter(socketupload.getOutputStream(), true);
             //               System.out.print(socket1);
             uploOn=true;
         }
         // If error, clean up and output an error message
         catch (IOException e) {
             int i=JOptionPane.showConfirmDialog(null,"Uploader did not work\n\nPlease activate your uploader application","Activate Uploader",JOptionPane.YES_NO_OPTION);
             if (i==JOptionPane.YES_OPTION){
                 connectuploder();
             }else{
                 uploOn=false;
                 sql="update user_account set _activity=0 where username= '" +Log.data[0]+ "' limit 1";
                 jconn.SQLExecute(sql, conn);
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException ex) {
                         //                     Logger.getLogger(home.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 System.exit(0);
             }
         }
     }
     public static void connectuploder1()  {
         try {
             socketupload1 = new Socket("localhost", 6022);
             inupload1 = new BufferedReader(new
                     InputStreamReader(socketupload1.getInputStream()));
             outupload1 = new PrintWriter(socketupload1.getOutputStream(), true);
             //               System.out.print(socket1);
             uploOn=true;
         }
         // If error, clean up and output an error message
         catch (IOException e) {
             int i=JOptionPane.showConfirmDialog(null,"Uploader did not work\n\nPlease activate your uploader application","Activate Uploader",JOptionPane.YES_NO_OPTION);
             if (i==JOptionPane.YES_OPTION){
                 connectuploder();
             }else{
                 uploOn=false;
                 sql="update user_account set _activity=0 where username= '" +Log.data[0]+ "' limit 1";
                 jconn.SQLExecute(sql, conn);
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException ex) {
                         //                     Logger.getLogger(home.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 System.exit(0);
             }
         }
     }
     public static void kirimTele(){
         if(teleOn==true){
             sendString(s);
             outtele.print(toSend); outtele.flush();
             toSend.setLength(0);
             s=null;
         }
     }
     public static void kirimBroad(){
         if(brcaOn==true){
             sendString(s);
             outbroad.print(toSend); outbroad.flush();
             toSend.setLength(0);
             s=null;
         }
     }
     public static void kirimUplo(){
         if(uploOn==true){
             sendString(s);
             outupload.print(toSend); outupload.flush();
             toSend.setLength(0);
             s=null;
         }
     }
     private static void cleanUptele() {
         try {
             if (hostServer != null) {
                 hostServer.close();
                 hostServer = null;
             }
         }
         catch (IOException e) { hostServer = null; }
 
         try {
             if (sockettele != null) {
                 sockettele.close();
                 sockettele = null;
             }
         }
         catch (IOException e) { sockettele = null; }
 
         try {
             if (intele != null) {
                 intele.close();
                 intele = null;
             }
         }
         catch (IOException e) { intele = null; }
 
         if (outtele != null) {
             outtele.close();
             outtele = null;
         }
     }
     private static void cleanbroad() {
         try {
             if (hostServer1 != null) {
                 hostServer1.close();
                 hostServer1 = null;
             }
         }
         catch (IOException e) { hostServer1 = null; }
 
         try {
             if (socketbroad != null) {
                 socketbroad.close();
                 socketbroad = null;
             }
         }
         catch (IOException e) { socketbroad = null; }
 
         try {
             if (inbroad != null) {
                 inbroad.close();
                 inbroad = null;
             }
         }
         catch (IOException e) { inbroad = null; }
 
         if (outbroad != null) {
             outbroad.close();
             outbroad = null;
         }
     }
     private static void cleanUpupload() {
 
       try {
          if (socketupload != null) {
             socketupload.close();
             socketupload = null;
          }
       }
       catch (IOException e) { socketupload = null; }
 
       try {
          if (inupload != null) {
             inupload.close();
             inupload = null;
          }
       }
       catch (IOException e) { inupload = null; }
 
       if (outupload != null) {
          outupload.close();
          outupload = null;
       }
    }
 }
 
