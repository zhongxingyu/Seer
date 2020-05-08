 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package alert;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import util.AlertListener;
 import util.UpdateListener;
 import java.io.*;
 import java.net.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Properties;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import util.LogListener;
 
 /**
  * This class monitors the alert system
  * @author Shawn
  */
 public final class AlertMonitoringSystem {
     
     private static final String IP_PROPERTY = "alertServerIP", PORT_PROPERTY = "alertServerPort";
 
     
    private JPanel parent; //incase the AMS becomes a GUI later, we can have a parent for JOptionPanes
     
     private AlertMonitorThread amt;
     Logger log = Logger.getGlobal();
     
     private Stack<AlertListener> alertListeners = new Stack();
     private Stack<LogListener> logListeners = new Stack();
     
     private ArrayList<Alert> activeAlerts = new ArrayList();
     private Alert[] pastAlerts = new Alert[50];
     
     private AlertDispatchThread dispatch;
     
     /**
      * 
      * @throws IOException when the config file cannot be found or there is a read error
      */
     public AlertMonitoringSystem() throws IOException {
         super();
         
         amt = new AlertMonitorThread(this);
         amt.start();
     }
     
     public void addAlertListener(AlertListener listener) {
         alertListeners.add(listener);
     }
     
     public void removeAlertListner(AlertListener listener) {
         alertListeners.remove(listener);
     }
     
     public void addLogListener(LogListener listener) {
         logListeners.add(listener);
     }
     
     public void removeLogListner(LogListener listener) {
         logListeners.remove(listener);
     }
     
     private void alertAllLogListeners(String log) {
         for(LogListener listener: logListeners) {
             listener.onLog(log);
         }
     }
     
     private void alertAllAlertListeners(Alert alert) {
         for(AlertListener listener: alertListeners) {
             listener.alertReceived(alert); // go through all of the listners and tell them the alert
         }
     }
     
     public JPanel getAlertMonitoringPanel() {
         return parent;
     }
     
     private static final int BADSYNTAX = -2;
     private static final int SUCCESS = 1;
     private static final int EXCEPTION = 2;
     private static final int NOTINSYSTEM = 3;
 
     private static final String AAP = "AAP"; // all active pages
     private static final String STATUS = "S";
     private static final String START = "ST"; // start paging
     private static final String STOP = "SP"; // stop paging
     private static final String ACKNOWLEDGE = "ACK";
     private static final String STOPALL = "SPA";
     
     public synchronized int doTask(String task) {
         try {
             String[] split = task.split(" ", 2); //split by spaces
             String command = split[0];
             String rest = split[1].trim();
 
             if(command.equals(STATUS)) {
                 int jobID = Integer.parseInt(rest);
                 int status = getStatus(jobID);
                 return status;
             } else if (command.equals(START)) {
                 Alert alert = parseAlert(rest);
                 try {
                     activeAlerts.add(alert);
                     //alertAllAlertListeners(alert);
                     
                     makeSurePageThreadIsRunning();
                     
                     alert.setNextAlertTime(Calendar.getInstance());
                     alertAllLogListeners("Created alert: " + alert.toString());
 
                     return SUCCESS;
                 } catch(Exception ex) {
                     log.log(Level.INFO, ex.getMessage());
                     return EXCEPTION;
                 }
 
 
             } else if (command.equals(STOP)) {
                 try {
                     int jobID;
                     try {
                         jobID = Integer.parseInt(rest);
                     } catch(Exception ex) {
                         Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                         return BADSYNTAX;
                     }
 
                     int index = searchFor(jobID);
                     if(index == -1) {
                         return NOTINSYSTEM;
                     }
 
                     Alert remove = activeAlerts.remove(index); //and since arraylist works on .equals(), it will remove the active alert that has the jobid
                     remove.acknowledge();
                     addToPastAlerts(remove);
                     alertAllLogListeners("Stopped alert: " + remove.toString());
                     return SUCCESS;
                 } catch(Exception ex) {
                     Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                     return EXCEPTION;
                 }
 
             } else if(command.equals(ACKNOWLEDGE)) {
 
                 int jobID;
                 try {
                     jobID = Integer.parseInt(rest);
                 } catch(Exception ex) {
                     Logger.getGlobal().log(Level.SEVERE, "Bad syntax n acknowledgement", ex);
                     return BADSYNTAX;
                 }
 
                 int index = searchFor(jobID);
 
                 if(index == -1) {
                     return NOTINSYSTEM;
                 }
 
                 Alert remove = activeAlerts.remove(index); //and since arraylist works on .equals(), it will remove the active alert that has the jobid
                 remove.acknowledge();
                 addToPastAlerts(remove);
                 alertAllLogListeners("Acknowledged alert: " + remove.toString());
                 return SUCCESS;
 
             } else if(command.equals(STOPALL)) {
                 try {
                     for(Alert alert: activeAlerts) {
                         alert.acknowledge();
                     }
 
                     alertAllLogListeners("Stopped all alerts");
                     return SUCCESS;
                 } catch(Exception ex) {
                     Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                     return EXCEPTION;
                 }
 
             } else {
                 // syntax is incorrect
                 return BADSYNTAX;
             }
         } catch(Exception ex) {
             return BADSYNTAX;
         }
     }
     
 
 
     
 
     private Alert parseAlert(String alertText) {
         String[] split = alertText.split(" ", 2);
         int jobID = Integer.parseInt(split[0]);
         String message = split[1];
         Alert alert = new Alert(jobID, message);
         return alert;
     }
 
     private int searchFor(int jobID) {
         for(int i = 0; i < activeAlerts.size(); i++) {
             Alert alert = activeAlerts.get(i);
             if(alert.getJobID() == jobID)
                 return i;
         }
 
         return -1;
     }
 
     private String getAllAlertText() {
         String alertText = "";
         for(int i = 0; i < activeAlerts.size(); i++) {
             alertText += activeAlerts.get(i);
             if(i != activeAlerts.size() - 1)
                 alertText += "\n";
         }
         return alertText;
     }
 
     private static final int ALREADY = 1;
     private static final int PENDING = 2;
     
     private int getStatus(int jobID) {
         // check if already acknowledged
         boolean hitNull = false;
         for(int i = 0; i < pastAlerts.length && !hitNull; i++) {
             Alert alert = pastAlerts[i];
             if(alert == null)
                 hitNull = true;
             else {
                 if(jobID == alert.getJobID())
                     return ALREADY;
             }
         }
         
         for(Alert alert: activeAlerts) {
             if(alert.getJobID() == jobID)
                 return PENDING;
         }
         
         return NOTINSYSTEM;
     }
     
     private void addToPastAlerts(Alert alert) {
         // move all to right
         for(int i = 0; i < pastAlerts.length - 1; i++) {
             pastAlerts[i + 1] = pastAlerts[i];
         }
         pastAlerts[0] = alert;
     }
     
     private void makeSurePageThreadIsRunning() {
         if(dispatch == null) {
             dispatch = new AlertDispatchThread();
         }
         
         if(!dispatch.isAlive()) {
             dispatch.start();
         }
     }
     
     private class AlertMonitorThread extends ErrorLoggingThread {
         
         private final AlertMonitoringSystem ams;
 
         private Socket socket = null;
         private InputStream is = null;
         private OutputStream os = null;
         
         public AlertMonitorThread(AlertMonitoringSystem ams) {
             super();
             this.ams = ams;
         }
         
         public void run() {
             
             try {
                 String ip = "127.0.0.1";
                 int port = 7655;
                 
                 socket = new Socket(ip, port);
                 
                 is = socket.getInputStream();
                 os = socket.getOutputStream();
                 
                 while(true) {
                     
                     String buffer = "";
                     do {
                         int read = is.read();
                         if(read == -1)
                             throw new IOException("The connection was broken");
                         buffer += (char) read;
                         
                     } while(is.available() > 0);
                     
                     if(buffer.equals(AAP)) {
                         os.write(getAllAlertText().getBytes());
                         os.flush();
                     } else {
                         log.log(Level.FINE , "Received: " + buffer);
                         write(doTask(buffer));
                     }
                 }
             } catch (Exception ex) {
                 /*
                 if(amt != this) {
                     System.out.println("User wanted to change something");
                     return;
                 }*/
                 
                 System.out.println(ex.getClass());
                 
                 if(is != null)
                     try {
                     is.close();
                 } catch (IOException ex1) {
                     Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                 }
                 
                 if(os != null)
                     try {
                     os.close();
                 } catch (IOException ex1) {
                     Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                 }
                 
                 if(socket != null)
                     try {
                     socket.close();
                 } catch (IOException ex1) {
                     log.log(Level.INFO, ex1.getMessage());
                 }
                     
                 Logger.getGlobal().log(Level.SEVERE, "AMS :", ex);
                 
                try {
                     Thread.sleep(1000);
                 } catch (InterruptedException ex1) {
                     Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                 }
                 ams.amt = new AlertMonitorThread(ams);
                 amt.start();
             }
             
             
             
         }
 
         /**
         * Simplified way of writing status codes
         */
         private void write(int i) throws IOException {
             os.write(("" + i).getBytes());
             os.flush();
         }
         
         
     }
     
     private class AlertDispatchThread extends ErrorLoggingThread {
 
         public AlertDispatchThread() {
             super();
         }
         
         @Override
         public void run(){
             while(!activeAlerts.isEmpty()) {
                 
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
                 for(Alert alert: activeAlerts) {
                     if(alert.isReadyToAlert()) {
                         
                         alert.incrementTimesPaged();
                         
                         alertAllAlertListeners(alert); // page employees
                         
                         Calendar cal = Calendar.getInstance();
                         cal.add(Calendar.MINUTE, 15); //add fifteen minutes.
                         alert.setNextAlertTime(cal);
                     }
                 }
             }
         }
         
     }
     
     public class AlertMonitoringPanel extends JPanel implements LogListener {
 
         private AlertMonitoringSystem ams;
         
         private JTextArea logArea;
         
         protected AlertMonitoringPanel(AlertMonitoringSystem aThis) {
             super();
             ams = aThis;
             init();
         }
         
         private void init() {
             this.setBorder(new EmptyBorder(10,10,10,10));
             this.setLayout(new BorderLayout());
             
             logArea = new JTextArea();
             logArea.setEditable(false);
             this.add(logArea, BorderLayout.CENTER);
             
             ams.addLogListener(this);
         }
         
         private void log(String toLog) {
             SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
             String formattedDate = sdf.format(Calendar.getInstance().getTime());
             logArea.append(toLog + " at " + formattedDate + "\n");
         }
 
         @Override
         public void onLog(String logText) {
             log(logText);
         }
     }
     
     
     private class ErrorLoggingThread extends Thread {
         
         
         @Override
         public UncaughtExceptionHandler getUncaughtExceptionHandler() {
             return new UncaughtExceptionHandler() {
                 @Override
                 public void uncaughtException(Thread t, Throwable e) {
                     Logger.getGlobal() .log(Level.SEVERE, null, e);
                     makeGUI(t, e);
                 }
                 
             };
         }
         
         private void makeGUI(Thread t, Throwable e) {
         
             JFrame frame = new JFrame("ALERT: Uncaught Exception");
 
             frame.getContentPane().setLayout(new BorderLayout());
 
             JTextArea area = new JTextArea();
             area.setWrapStyleWord(true);
             area.setLineWrap(true);
             area.setEditable(false);
 
             area.append("There was an uncaught exception in a thread.\nThis is a serious problem. Please report this to Specialized Programming LLC\n");
             area.append("Exception in " + t.getClass().getName() + "\n");
             area.append("Exception type: "+e.getClass().getName() + "\n");
 
             StackTraceElement[] stackTrace = e.getStackTrace();
             for(StackTraceElement ele: stackTrace) {
                 area.append(ele.toString() + "\n");
             }
 
             frame.add(area, BorderLayout.CENTER);
             frame.setSize(400,400);
             frame.setVisible(true);
         }
         
     }
 }
     
 
