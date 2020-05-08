 package view;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import main.ServerManager;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class CoreStatus extends JPanel implements MouseListener {
     private final int verticalPosition = 60;
     private final int offset = 40;
     private JLabel notExist;
 
     private Process mangosd = null;
     private Process realmd = null;
 
     public CoreStatus() {
         super();
         setLayout(null);
         initComponents();
     }
 
     /** create the gui for this page */
     private void initComponents() {
         addTitle();
         addOptions();
     }
 
     /** add the page's title */
     private void addTitle() {
         JLabel lblTitle = new JLabel();
         lblTitle.setText("Server Manager");
         lblTitle.setBounds(20, 20, 150, 20);
         lblTitle.setFont(ServerManager.FONT_16_BOLD);
         add(lblTitle);
     }
 
     private void addOptions() {
         JLabel startCore = new JLabel();
         startCore.setName("StartCore");
         startCore.setText("Start Core");
         startCore.setBounds(35, verticalPosition + 1 * offset, 200, 20);
         startCore.setFont(ServerManager.FONT_12_BOLD);
         startCore.addMouseListener(this);
         startCore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         add(startCore);
 
         JLabel stopCore = new JLabel();
         stopCore.setName("StopCore");
         stopCore.setText("Stop Core");
         stopCore.setBounds(35, verticalPosition + 2 * offset, 200, 20);
         stopCore.setFont(ServerManager.FONT_12_BOLD);
         stopCore.addMouseListener(this);
         stopCore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         add(stopCore);
 
         notExist = new JLabel();
         notExist.setName("notExist");
         notExist.setText("");
         notExist.setBounds(35, verticalPosition + 4 * offset, 600, 20);
         notExist.setFont(ServerManager.FONT_12_BOLD);
         notExist.setVisible(false);
         notExist.setEnabled(false);
         add(notExist);
     }
 
     private boolean isRunning(String processname) throws IOException
     {
         processname = splitAtDir(processname, false);
         boolean found = false;
         Runtime runtime = Runtime.getRuntime();
         String cmds[] = {"cmd", "/c", "tasklist"};
         Process proc = runtime.exec(cmds);
         InputStream inputstream = proc.getInputStream();
         InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
         BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
         String line;
         Pattern pattern = Pattern.compile(processname);
         while ((line = bufferedreader.readLine()) != null)
         {
             Matcher matcher =  pattern.matcher(line);
             if (matcher.find())
                 found = true;
         }
         return found;
     }
 
     private String splitAtDir(String fileLocation, boolean dir)
     {
         String result = "";
         String[] pieces = fileLocation.split("\\\\");
         if (dir == false)
            result = pieces[pieces.length-1];
         else
         {
             for (int i = 0; i < pieces.length-1; i++)
             {
                 if (i != pieces.length -2)
                     result += pieces[i]+"\\";
                 else
                     result += pieces[i];
             }
         }
         return result;
     }
 
     public boolean canExecute(String executeFile)
     {
         File aFile = new File(executeFile);
         if (aFile == null) {
             return false;
         }
         if (!aFile.exists()) {
             return false;
         }
         if (!aFile.isFile()) {
             return false;
         }
         return true;
     }
 
     @Override
     public void mouseClicked(MouseEvent event) {
         JLabel label = (JLabel) event.getSource();
         String Labelname = label.getName();
         if (Labelname.equals("StopCore"))
         {
             if (this.realmd != null)
                 this.realmd.destroy();
             if (this.mangosd != null)
                 this.mangosd.destroy();
         }
         else
         {
            File WorkingDir = new File("D:\\Users\\Dark\\Desktop\\Batch\\Completed_Release_x32");
             String mangosdname = "D:\\Users\\Dark\\Desktop\\Batch\\Completed_Release_x32\\mangosd.exe";
             String realmdname = "D:\\Users\\Dark\\Desktop\\Batch\\Completed_Release_x32\\realmd.exe";
             try {
                 if (isRunning(mangosdname) == false) {
                     if (canExecute(mangosdname) == true)
                     {
                        this.mangosd = Runtime.getRuntime().exec(mangosdname,null,WorkingDir);
                     }
                     else
                         notExist.setText("Cannot run " + mangosdname + ": file not found. ");
                 }
             } catch (IOException ex) {
                 Logger.getLogger(CoreStatus.class.getName()).log(Level.SEVERE, null, ex);
             }
             try {
                 if (isRunning(realmdname) == false) {
                     if (canExecute(realmdname) == true)
                     {
                        this.realmd = Runtime.getRuntime().exec(realmdname,null,WorkingDir);
                     }
                     else
                         notExist.setText(notExist.getText() + "Cannot run " + realmdname + ": file not found.");
                 }
             } catch (IOException ex) {
                 Logger.getLogger(CoreStatus.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         if (!notExist.getText().equals(""))
             notExist.setVisible(true);
         else
             notExist.setVisible(false);
     }
 
     @Override
     public void mouseEntered(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void mousePressed(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void mouseReleased(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void mouseExited(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void paint(Graphics graphics) {
         super.paint(graphics);
         graphics.drawLine(20, 45, ServerManager.ScreenWidth - 20, 45);
     }
 }
