 /*
  * McZLauncher (ZeTRiX's Minecraft Launcher)
  * Copyright (C) 2012 Evgen Yanov <http://www.zetlog.ru>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program (In the "_License" folder). If not, see <http://www.gnu.org/licenses/>.
 */
 
 package ru.zetrix;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 
 /**
  *
  * @author ZeTRiX
  */
 public class MServ extends JFrame {
     
     public static JTabbedPane Content;
     public static Box MainBox;
     private static Box AboutBox;
     
     public JButton Go = new JButton("Run MasterServer", (new ImageIcon(Util.getRes("go.png"))));
     public JButton Send = new JButton("Send command", (new ImageIcon(Util.getRes("send.png"))));
     public static JLabel ClC = new JLabel("Command:");
     public static JLabel ClT = new JLabel("Recieved text:");
     public static JTextField ClNum = new JTextField(20);
     public static JTextPane Text;
     public DataOutputStream out;
     
     public static JLabel About = new JLabel("McZMasterServer (ZeTRiX's Master Server for  McZLauncher)");
     public static JLabel About1 = new JLabel("This program is for a launcher's operator");
     public static JLabel About2 = new JLabel("to use it - push the Run button and then start the launcher!");
     public static JLabel About3 = new JLabel("Do not forget to write the IP of MasterServer in connect.php");
     public static JLabel About4 = new JLabel("Licensed under GNU GPL ver.3");
     public static JLabel About5 = new JLabel("Copyright (C) 2012 ZeTRiX (Evgen Yanov)");
     
     public MServ() {
         setTitle("McZMasterServer (Coded by ZeTRiX)");
         setIconImage(Util.getRes("favicon.png"));
         setBackground(Color.BLACK);
         this.setBounds(200, 200, 480, 220);
         setResizable(false);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         final ImageIcon icon = new ImageIcon(Util.getRes("bg.png"));
         Content = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT) {
             protected void paintComponent(Graphics g) {
                 g.drawImage(icon.getImage(), 0,0, null);
                 super.paintComponent(g);
             }
         };
         
         Box box1 = Box.createHorizontalBox();
         box1.add(ClC);
         box1.add(Box.createHorizontalStrut(6));
         box1.add(ClNum);
         Box box2 = Box.createHorizontalBox();
         box2.add(ClT);
         box2.add(Box.createHorizontalStrut(6));
         Text = new JTextPane() {
             private static final long serialVersionUID = 1L;
         };
         Text.setText("Client Message will appear here. Instruction is in the \"About\" tab!");
         Text.setEditable(false);
         box2.add(Text);
         Box box4 = Box.createHorizontalBox();
         box4.add(Box.createHorizontalGlue());
         box4.add(Box.createHorizontalStrut(12));
         Send.setEnabled(false);
         box4.add(Send);        
         box4.add(Go);
         ClC.setPreferredSize(ClT.getPreferredSize());
         MainBox = Box.createVerticalBox();
         MainBox.setBorder(new EmptyBorder(12,12,12,12));
         MainBox.add(box1);
         MainBox.add(Box.createVerticalStrut(12));
         MainBox.add(box2);
         MainBox.add(Box.createVerticalStrut(17));
         MainBox.add(box4);
         Content.add("Starter", MainBox);
         
         Box ab0 = Box.createHorizontalBox();
         Box ab1 = Box.createVerticalBox();
         ab1.add(About);
         ab1.add(About1);
         ab1.add(About2);
         ab1.add(About3);
         ab0.add(ab1);
         Box ab2 = Box.createHorizontalBox();
         ab2.add(About4);
         ab2.add(Box.createHorizontalStrut(6));
         Box ab3 = Box.createHorizontalBox();
         ab3.add(About5);
         AboutBox = Box.createVerticalBox();
         AboutBox.setBorder(new EmptyBorder(12,12,12,12));
         AboutBox.add(ab0);
         AboutBox.add(Box.createVerticalStrut(8));
         AboutBox.add(ab2);
         AboutBox.add(Box.createVerticalStrut(6));
         AboutBox.add(ab3);
         Content.add("About", AboutBox);
         
         this.getContentPane().add(Content);
         
         Go.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 MSRun();
                 Go.setEnabled(false);
                 Send.setEnabled(true);
             }
         });
         Send.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 SendMessage(ClNum.getText().toString());
             }
         });
     }
     
     public void MSRun() {
         new Thread() {
             @Override
             public void run() {
                 int port = 59092; // Cлучайный порт (любое число от 1025 до 65535)
                 try {
                     ServerSocket ss = new ServerSocket(port);
                     System.out.println("Waiting for a client...");
                     
                     Socket socket = ss.accept();
                    System.out.println("Connected from: " + socket.getInetAddress().getHostAddress() + "/" + socket.getInetAddress().getHostName());
                     
                     InputStream sin = socket.getInputStream();
                     OutputStream sout = socket.getOutputStream();
                     
                     DataInputStream in = new DataInputStream(sin);
                     out = new DataOutputStream(sout);
                     
                     while(true) {
                         Text.setText(in.readUTF());
                     }
                 } catch(Exception x) {
                     Text.setText(x.toString());
                 }
             }
         }
                 .start();
     }
     
     public void SendMessage(String text) {
         if (text.trim() == null) {
             JOptionPane.showMessageDialog((Component)
                     null,
                     "No command was typed. \n Please, type a command and try again! \n",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
         } else {
             try {
                 out.writeUTF(text);
                 out.flush();
             } catch (IOException ex) {
                 System.out.println(ex.toString());
             }
         }
     }
     
     public static void main(String[] args) {
         try {
             for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (Exception e) {
             System.out.println("Error: " + e);
         }
         MServ McZMServ = new MServ();
         McZMServ.show();
     }
 }
