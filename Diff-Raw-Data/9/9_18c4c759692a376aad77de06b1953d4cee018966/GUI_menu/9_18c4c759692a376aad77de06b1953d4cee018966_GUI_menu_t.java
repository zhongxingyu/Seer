 /**
 *Text genereted by Simple GUI Extension for BlueJ
 */
 import javax.swing.UIManager.LookAndFeelInfo;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import javax.swing.border.Border;
 import javax.swing.*;
 
 
 public class GUI_menu extends JFrame {
 
     private JButton button1;
     private JButton button2;
     private JButton button3;
     private JButton button4;
     private JButton button5;
     private JLabel label1;
     private JLabel label2;
 
     //Constructor 
     public GUI_menu() {
 
         this.setTitle("PT_SP");
         this.setSize(300,400);
 
         //pane with null layout
         JPanel contentPane = new JPanel(null);
         contentPane.setPreferredSize(new Dimension(300,400));
         contentPane.setBackground(new Color(192,192,192));
 
 
         button1 = new JButton();
         button1.setBounds(50,100,190,40);
         button1.setBackground(new Color(214,217,223));
         button1.setForeground(new Color(0,0,0));
         button1.setEnabled(true);
         button1.setFont(new Font("sansserif",0,12));
         button1.setText("Generate new data");
         button1.setVisible(true);
         //Set action for button click
         //Call defined method
         button1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 generate(evt);
             }
         });
 
 
         button2 = new JButton();
         button2.setBounds(50,195,190,40);
         button2.setBackground(new Color(214,217,223));
         button2.setForeground(new Color(0,0,0));
         button2.setEnabled(true);
         button2.setFont(new Font("SansSerif",1,12));
         button2.setText("Start simulation");
         button2.setVisible(true);
         //Set action for button click
         //Call defined method
         button2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 start(evt);
             }
         });
 
 
         button3 = new JButton();
         button3.setBounds(50,240,190,40);
         button3.setBackground(new Color(214,217,223));
         button3.setForeground(new Color(0,0,0));
         button3.setEnabled(true);
         button3.setFont(new Font("sansserif",0,12));
         button3.setText("Stop simulation");
         button3.setVisible(true);
         //Set action for button click
         //Call defined method
         button3.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 stop(evt);
             }
         });
 
 
         button4 = new JButton();
         button4.setBounds(50,145,190,40);
         button4.setBackground(new Color(214,217,223));
         button4.setForeground(new Color(0,0,0));
         button4.setEnabled(false);
         button4.setFont(new Font("sansserif",0,12));
         button4.setText("Save new data");
         button4.setVisible(true);
         //Set action for button click
         //Call defined method
         button4.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 try
                 {
                     save(evt);
                 }
                 catch (Exception e)
                 {
                 };
             }
         });
         
         button5 = new JButton();
         button5.setBounds(50,290,190,40);
         button5.setBackground(new Color(214,217,223));
         button5.setForeground(new Color(0,0,0));
         button5.setEnabled(true);
         button5.setFont(new Font("sansserif",0,12));
         button5.setText("Add new settlement");
         button5.setVisible(true);
         //Set action for button click
         //Call defined method
         button5.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 addNode(evt);
             }
         });
 
         label1 = new JLabel();
         label1.setBounds(115,60,90,40);
         label1.setBackground(new Color(214,217,223));
         label1.setForeground(new Color(0,0,0));
         label1.setEnabled(true);
         label1.setFont(new Font("SansSerif",3,20));
         label1.setText("MENU");
         label1.setVisible(true);
 
         label2 = new JLabel();
         label2.setBounds(125,10,45,40);
         label2.setBackground(new Color(214,217,223));
         label2.setForeground(new Color(0,0,0));
         label2.setEnabled(true);
         label2.setFont(new Font("SansSerif",1,14));
         label2.setText("PT_SP");
         label2.setVisible(true);
 
         //adding components to contentPane panel
         contentPane.add(button1);
         contentPane.add(button2);
         contentPane.add(button3);
         contentPane.add(button4);
         contentPane.add(button5);
         contentPane.add(label1);
         contentPane.add(label2);
 
         //adding panel to JFrame and seting of window position and close operation
         this.add(contentPane);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setLocationRelativeTo(null);
         this.pack();
         this.setVisible(true);
     }
 
     //Method actionPerformed for button1
     private void generate (ActionEvent evt) {
         Core.generateNew();
         button4.setEnabled(true);
     }
 
     //Method actionPerformed for button2
     private void start (ActionEvent evt) {
         Core.start();
     }
 
     //Method actionPerformed for button3
     private void stop (ActionEvent evt) {
         Core.stop();
     }
 
     //Method actionPerformed for button4
     private void save (ActionEvent evt) throws Exception{
         Core.save();
     }
     
     private void addNode (ActionEvent evt) {
         int id = 1; //pracovne
         int x = 1; //pracovne
         int y = 1; //pracovne
         SettleNode n = new SettleNode(id, x, y);
        //JOptionPane msg = new JOptionPane();
        //msg.setMessageType(JOptionPane.QUESTION_MESSAGE);
        //msg.setMessage("hello");
        //msg.setName("Adding new settlement");
        String pop = JOptionPane.showInputDialog(this, new JOptionPane(), "Adding new settlement", JOptionPane.QUESTION_MESSAGE);
        System.out.println(pop);
        n.people = Integer.parseInt(pop);
         Core.g.addNode(n);
     }
 
     
     /* public static void main(String[] args){
         System.setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 new GUI_menu();
             }
         });
     }*/
 
 }
