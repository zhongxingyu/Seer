 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package modem;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import log.LoggingSystem;
 import static util.Utilities.getBaseDirectory;
 
 /**
  *
  * @author Shawn
  */
 public class PageWithModem implements Runnable, ReadListener {
     
     private static final String PP_PORT = "pagingPlugPort", MC_IP = "voiceModemIP", MC_PORT = "voiceModemPort";
     private static final File configFile = new File(getBaseDirectory() + "pagingsystem/" + "modemProps.cfg");
     
     private ModemConnector mc;
     private PagingPlug plug;
     
     private Properties props = new Properties();
     
     
     private JobAckCodeGenerator jacg = new JobAckCodeGenerator();
     
     private ServerSocket pagingModuleServer;
     private Socket pagingModuleSocket;
     
     public PageWithModem() {
         super();
         
         try {
             loadProps();
         } catch (IOException ex) {}
         checkProps();
     }
 
     
     
     private void loadProps() throws IOException {
         if(props == null)
             props = new Properties();
         
         if(!configFile.exists()) {
             String makePath = configFile.getPath().replace("modemProps.cfg", "");
             new File(makePath).mkdirs();
             configFile.createNewFile();
             loadProps(); // if the config file doesnt exist, then create the config file and try to load the properties again
             props.setProperty(MC_IP, "");
             props.setProperty(MC_PORT, "");
             props.setProperty(PP_PORT, "");
             saveProps();
         } else {
             props.load(new FileInputStream(configFile)); 
         }
     }
     
     private void checkProps() {
         checkPPPort();
         checkMCIP();
         checkMCPort();
     }
     
     private void checkPPPort() {
         while(!isValidPort(props.getProperty(PP_PORT))) {
             String port = JOptionPane.showInputDialog("Enter Paging Plugin Port").trim();
             props.setProperty(PP_PORT, port);
         }
     }
     
     private void checkMCIP() {
         while(!isValidPort(props.getProperty(MC_IP))) {
             String ip = JOptionPane.showInputDialog("Enter Phone Modem IP").trim();
             props.setProperty(MC_IP, ip);
         }
     }
     
     private void checkMCPort() {
         while(!isValidPort(props.getProperty(MC_PORT))) {
             String port = JOptionPane.showInputDialog("Enter Phone Modem Port".trim());
             props.setProperty(MC_PORT, port);
         }
     }
     
     private void saveProps() {
         try {
             props.store(new FileOutputStream(configFile), "Modem Properties, used by SCADA server");
             JOptionPane.showMessageDialog(null, "Please configure modem properties in " + configFile.getPath());
         } catch (IOException ex) {
             Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private void initModem() {
         try {
             String ip = props.getProperty(MC_IP).trim();
             String port = props.getProperty(MC_PORT).trim();
             
             mc = new ModemConnector(ip, port);
             mc.addReadListener(this);
             mc.start();
         } catch (IOException ex) {
             Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
             JOptionPane.showMessageDialog(null, "Voice Modem ip and port incorrect");
         }
     }
     
     public void startPage(int jobID, String message) {
         try {
             plug.startPage(jobID, message);
         } catch (IOException ex) {
             fix();
             startPage(jobID, message);
         }
     }
             
     public void stopPage(int jobID) {
         try {
             plug.stopPage(jobID);
         } catch (IOException ex) {
             fix();
             stopPage(jobID);
         }
     }
     
     public void acknowledgePage(int jobID) {
         try {
             plug.acknowledgePage(jobID);
         } catch (IOException ex) {
             fix();
             acknowledgePage(jobID);
         }
     }
     
     public int getStatus(int jobID) {
         try {
             return plug.getStatus(jobID);
         } catch (IOException ex) {
             fix();
             return getStatus(jobID);
         }
     }
     
     public String getAllActivePages() {
         try {
             return plug.getAllActivePages();
         } catch (IOException ex) {
             fix();
             return getAllActivePages();
         }
     }
     
     public void stopAllRunningPages() {
         try {
             plug.stopAllRunningPages();
         } catch (IOException ex) {
             fix();
             stopAllRunningPages();
         }
 ;
     }
     
     private void resetPagingModule() {
         stopPagingModule();
         startPagingModule();
     }
     
     private void startPagingModule() {
         new Thread(this).start();
     }
     
     public void stopPagingModule() {
         if(pagingModuleServer != null && !pagingModuleServer.isClosed()) {
             try {
                 pagingModuleServer.close();
             } catch (IOException ex) {
                 Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         if(pagingModuleSocket != null && !pagingModuleSocket.isClosed()) {
             try {
                 pagingModuleSocket.close();
             } catch (IOException ex) {
                 Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
             
     
     private void resetModemConnector() {
         stopModemConnector();
         startModemConnector();
     }
     
     private void stopModemConnector() {
         mc = null;
     }
     
     private void startModemConnector() {
         initModem();
     }
     
     public void start() {
         startModemConnector();
         startPagingModule();
     }
     
     public void stop() {
         stopModemConnector();
         stopPagingModule();
     }
     
     @Override
     public void run() {
         try{
             int port = Integer.parseInt(props.getProperty(PP_PORT));
             pagingModuleServer = new ServerSocket(port);
         } catch(IOException ex) {
             JOptionPane.showMessageDialog(null, "Server already on desginated port. Please reconfigure " + PP_PORT + " in " + configFile.getPath());
             return;
         }
         
         while(!pagingModuleServer.isClosed()) {
             try{
                 pagingModuleSocket = pagingModuleServer.accept();
                 plug = new PagingPlug(pagingModuleSocket);
             } catch(IOException ex) {
                 JOptionPane.showMessageDialog(null, "Error opening streams, please try again");
             }
         }
         
     }
     
     private void fix() {
         try {
             JOptionPane.showMessageDialog(null, "The paging module needs to connect to: " + InetAddress.getLocalHost().getHostAddress() + ":" + props.getProperty(PP_PORT));
         } catch(Exception ex) {}
     }
     
     
     public PagingPlug getPagingPlug() {
         return plug;
     }
 
     @Override
     public void onRead(final String pinText) {
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 try {
                     if(plug == null) {
                         JOptionPane.showMessageDialog(null, "Paging system not connected. Please connect and click OK");
                         onRead(pinText);
                         return;
                     }
                     int pin = Integer.parseInt(pinText);
                     int jobID = jacg.getJobID(pin);
                     plug.acknowledgePage(jobID);
                     jacg.ackJobID(jobID);
                 } catch (IOException ex) {
                     fix();
                     onRead(pinText);
                 }
             }
         };
         new Thread(r).start();
     }
     
     private class PagingPlug  {
         
         private InputStream is;
         private OutputStream os;
         
         public PagingPlug(Socket socket) throws IOException {
             super();
             is = socket.getInputStream();
             os = socket.getOutputStream();
         }
         
         protected void startPage(int jobID, String message) throws IOException {
             int ackCode = jacg.generateAckCode(jobID);
             String compose = "ST " + jobID + " " + message + " ACKCODE:" + ackCode;
             os.write(compose.getBytes());
             os.flush();
             LoggingSystem.getLoggingSystem().alertAllLogListeners("Page sent: jobID-" + jobID + " message-" + message);
         }
         
         protected void acknowledgePage(int jobID) throws IOException {
             String compose = "ACK " + jobID;
             os.write(compose.getBytes());
             os.flush();
             LoggingSystem.getLoggingSystem().alertAllLogListeners("Acknowledgement received: jobID-" + jobID);
         }
         
         protected void stopPage(int jobID) throws IOException {
             String compose = "SP " + jobID;
             os.write(compose.getBytes());
             os.flush();
         }
         
         protected int getStatus(int jobID) throws IOException {
             String compose = "S " + jobID;
             os.write(compose.getBytes());
             os.flush();
             return Integer.parseInt(readBuffer());
         }
         
         protected String getAllActivePages() throws IOException {
             String compose = "AAP";
             os.write(compose.getBytes());
             os.flush();
             return readBuffer();
         }
         
         protected void stopAllRunningPages() throws IOException {
             String compose = "SPA";
             os.write(compose.getBytes());
             os.flush();
         }
         
         private String readBuffer() throws IOException {
             String buffer = "";
             
             while(is.available() > 0) {
                 int read = is.read();
                 if(read == -1) {
                     throw new IOException("The connection was broken.");
                 }
                 buffer += (char) read;
             }
             
             return buffer;
         }
     }
     
     private class JobAckCodeGenerator {
         
         private Random random;
         private ArrayList<AckCode> activeCodes;
         
         public JobAckCodeGenerator() {
             super();
             random = new Random();
             activeCodes = new ArrayList();
         }
         
         public int generateAckCode(int jobID) {
             String codeText = "";
             int ran = -1;
             do {
                 int randomInt = random.nextInt(9); //0-8
                 randomInt++; //1-9
                 codeText += randomInt;
                 ran = Integer.parseInt(codeText);
             } while(!randomUsed(ran));
             //now we have a random int that isn't used yet
             AckCode code = new AckCode(jobID, ran);
             activeCodes.add(code);
             return code.getAckCode();
         }
         
         
         private boolean randomUsed(int randomInt) {
             for(AckCode code: activeCodes) {
                 if(code.getAckCode() == randomInt)
                     return true;
             }
             return false;
         }
         
         public int getAckCode(int jobID) {
             for(AckCode code: activeCodes) {
                 if(code.jobID == jobID)
                     return code.getAckCode();
             }
             
             return -1;
         }
         
         public int getJobID(int ackCode) {
             for(AckCode code: activeCodes) {
                 if(code.ackCode == ackCode)
                     return code.jobID;
             }
             
             return -1;
         }
         
         /*
          * Acknoledge the jobID
          */
         public void ackJobID(int jobID) {
             for(AckCode code: activeCodes) {
                 if(code.getJobID() == jobID) {
                     activeCodes.remove(code);
                     return;
                 }
             }
         }
         
         private class AckCode {
             private final int jobID;
             private final int ackCode;
             
             public AckCode(int jobID, int ackCode) {
                 super();
                 this.jobID = jobID;
                 this.ackCode = ackCode;
             }
 
             public int getAckCode() {
                 return ackCode;
             }
 
             public int getJobID() {
                 return jobID;
             }
         }
     }
     
     private void configure(JFrame parent) {
         JDialog dialog = new JDialog(parent, "Configure", true);
         dialog.setLayout(new BorderLayout());
         dialog.add(new PageAndModemPanel(this));
         dialog.pack();
         dialog.setVisible(true);
     }
     
     private class PageAndModemPanel extends JPanel {
         
         private JLabel pagingModulePortLabel = new JLabel(), modemIPLabel = new JLabel(), modemPortLabel = new JLabel();
         private JButton changePMPButton = new JButton("Change"), changeMIPButton = new JButton("Change"), changeMPButton = new JButton("Change");
         
         private PageWithModem pwm;
         
         public PageAndModemPanel(PageWithModem pwm) {
             super(new GridLayout(3,2,10,10));
             this.pwm = pwm;
             init();
         }
         
         private void init() {
             this.setBorder(new EmptyBorder(10,10,10,10));
             
             changePMPButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                     changePMP();
                     resetPagingModule();
                     updateLabels();
                 }
             });
             
             changeMIPButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                     changeMIP();
                     resetModemConnector();
                     updateLabels();
                 }
             });
             
             changeMPButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                     changeMP();
                     resetModemConnector();
                     updateLabels();
                 }
             });
             
             this.add(pagingModulePortLabel);
             this.add(changePMPButton);
             
             this.add(modemIPLabel);
             this.add(changeMIPButton);
             
             this.add(modemPortLabel);
             this.add(changeMPButton);
             
             updateLabels();
         }
         
         private void updateLabels() {
             if(pwm != null) {
                 pagingModulePortLabel.setText("Paging Module Port: " + props.getProperty(PP_PORT));
                 modemIPLabel.setText("Phone Modem IP: " + props.getProperty(MC_IP));
                 modemPortLabel.setText("Phone Modem Port: " + props.getProperty(MC_PORT));
             }
         }
         
         private void changePMP() {
             String port = null;
             do {
                 port = JOptionPane.showInputDialog(this, "Enter Paging Module Port").trim();
                 
             } while(!isValidPort(port));
             
             props.setProperty(PP_PORT, port);
         }
         
         private void changeMIP() {
             String ip = null;
             do {
                 ip = JOptionPane.showInputDialog(this, "Enter Phone Modem IP").trim();
                 
             } while(!isValidIPv4(ip));
             
             props.setProperty(MC_IP, ip);
         }
         
         private void changeMP() {
             String port = null;
             do {
                 port = JOptionPane.showInputDialog(this, "Enter Phone Modem Port").trim();
                 
             } while(!isValidPort(port));
             
             props.setProperty(MC_PORT, port);
         }
     }
     
     private boolean isValidIPv4(String ip) {
         if(ip == null || ip.equals(""))
             return false;
         
         try {
             InetAddress inet = InetAddress.getByName(ip);
             return inet.getHostAddress().equals(ip) && inet instanceof Inet4Address;
         } catch (final UnknownHostException ex) {
             return false;
         }
     }
     
     private static boolean isValidPort(String port) {
         if(port == null || port.equals(""))
             return false;
         
         try {
             int p = Integer.parseInt(port);
             if(p >= 0 && p <= 65535)
                 return true;
             else
                 return false;
         } catch(Exception ex) {
             return false;
         }
     }
 }
 
