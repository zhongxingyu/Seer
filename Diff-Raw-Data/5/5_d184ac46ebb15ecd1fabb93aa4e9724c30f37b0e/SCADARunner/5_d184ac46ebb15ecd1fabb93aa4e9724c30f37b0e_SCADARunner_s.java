 /*
  * Author: Peter O'Connor
  * Purpose: To implement SCADA Monitoring throughout Washington County
  * Version: 1.0a
  * 
  * Contact: avogadrosg1@gmail.com
  * 
  */
 package washcoscadaserver;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 
 public class SCADARunner
 {
 
     static JTextArea mainArea;
     static JFrame frame;
     static SCADAServer server;
     
     public static void main(String[] args) 
     {
         server = new SCADAServer();
         frame = new JFrame("Beta SCADA Monitor GUI");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setLayout(new BorderLayout());
         frame.setSize(550,700);
         
         JLabel title = new JLabel("SCADA Server");
         JPanel main = new JPanel();
         JPanel titlePanel = new JPanel();
         titlePanel.setPreferredSize(new Dimension(500,30));
         //titlePanel.setLayout(new BorderLayout());
         main.setPreferredSize(new Dimension(500,500));
         
         mainArea = new JTextArea(30,30);
         mainArea.setText("Initializing.");
         mainArea.setEditable(false);
         
         JScrollPane scrollStatus = new JScrollPane(mainArea);
         scrollStatus.setPreferredSize(new Dimension(500,500));
         scrollStatus.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         scrollStatus.setAutoscrolls(true);
         
         
         main.add(scrollStatus);
         frame.add(main, BorderLayout.CENTER);
         
         title.setFont(Font.getFont("Calibri"));
         title.setForeground(Color.RED);
         //title.setFont(new Font("Calibri", 40, Font.BOLD));
         //title.setPreferredSize(new Dimension(300, 50));
         titlePanel.add(title);
         frame.add(titlePanel,BorderLayout.NORTH);
         Timer bob = new Timer(5001, new TimerListener());
         bob.start();
         frame.setVisible(true);
     }
     
     
     static class TimerListener implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             
             mainArea.setText("Status:\n");
             mainArea.append(server.getInformation());
             frame.repaint();
         }
 
     }
     
 }
