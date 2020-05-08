 /*  YAMMWrite.java - Writing mail
  *  Copyright (C) 1999 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.gjt.fredde.yamm;
 
 import javax.swing.*;
 import java.awt.event.*;
 import javax.swing.border.*;
 import java.awt.*;
 import java.util.*;
 import java.text.SimpleDateFormat;
 import java.io.*;
 import org.gjt.fredde.yamm.encode.UUEncode;
 import org.gjt.fredde.yamm.gui.AttachListRenderer;
 import org.gjt.fredde.util.gui.MsgDialog;
 
 /**
  * The class for writing mails
  */
 public class YAMMWrite extends JFrame {
 
   /** A list of files to attach to the mail */
   static protected Vector         attach;
 
   /** The textarea for writing in the message */
   public JTextArea      myTextArea;
 
   JLabel      myLabel;
   JButton     myButton;
   JTextField  myTextField1;
   JTextField  myTextField2;
   JList       myList;
 
   Box         vert1, vert2, vert3, hori1, hori2;
   int         writex, writey, writew, writeh;
 
   /**
    * Creates a new empty mail with no subject and recipent
    */
   public YAMMWrite() {
     this("","","");
   }
 
   /**
    * Creates a new empty mail with no subject and with the specified recipent
    * @param to The recipents address
    */
   public YAMMWrite(String to) {
     this(to, "","");
   }
 
   /**
    * Creates a new empty mail with the specified subject and recipent
    * @param to The recipents address
    * @param subject The subject
    */
   public YAMMWrite(String to, String subject) {
     this(to,subject,"");
   }
 
   public YAMMWrite(String to, String subject, String body) {
     setTitle(subject);
 
     writex = Integer.parseInt(YAMM.getProperty("writex", "0"));
     writey = Integer.parseInt(YAMM.getProperty("writey", "0"));
     writew = Integer.parseInt(YAMM.getProperty("writew", "500"));
     writeh = Integer.parseInt(YAMM.getProperty("writeh", "300"));
 
     setBounds(writex, writey, writew, writeh);
     
     JMenuBar  Meny = new JMenuBar();
     JMenu     arkiv = new JMenu(YAMM.getString("file"));
     JMenuItem rad;
     Meny.add(arkiv);
 
     rad = new JMenuItem(YAMM.getString("button.cancel"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/cancel.gif"));
     rad.addActionListener(MListener);
     arkiv.add(rad);
 
     setJMenuBar(Meny);
 
     vert1 = Box.createHorizontalBox();
     vert2 = Box.createHorizontalBox();
     vert3 = Box.createVerticalBox();
     hori1 = Box.createHorizontalBox();
     hori2 = Box.createHorizontalBox();
 
     myButton = new JButton(YAMM.getString("button.send"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/send.gif"));
     myButton.addActionListener(BListener);
     hori1.add(myButton);
 
     myButton = new JButton(YAMM.getString("button.cancel"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/cancel.gif"));
     myButton.addActionListener(BListener);
     hori1.add(myButton);
 
     myLabel = new JLabel(YAMM.getString("mail.to") + "  ");
     vert1.add(myLabel);
 
     myTextField1 = new JTextField();
     myTextField1.setMaximumSize(new Dimension(1200, 20));
     myTextField1.setMinimumSize(new Dimension(75, 20));
     myTextField1.setText(to);
     myTextField1.setToolTipText(YAMM.getString("tofield.tooltip"));
     vert1.add(myTextField1);
 
     myLabel = new JLabel(YAMM.getString("mail.subject") + "  ");
     vert2.add(myLabel);
 
     myTextField2 = new JTextField();
     myTextField2.addKeyListener(new KeyAdapter(){
       public void keyReleased(KeyEvent ke) { setTitle(myTextField2.getText()); }
       public void keyPressed(KeyEvent ke) { setTitle(myTextField2.getText()); }
     });
     myTextField2.setMaximumSize(new Dimension(1200, 20));
     myTextField2.setMinimumSize(new Dimension(75, 20));
     myTextField2.setText(subject);
     vert2.add(myTextField2);
 
     vert3.add(hori1);
     vert3.add(vert1);
     vert3.add(vert2);
 
     myTextArea = new JTextArea();
     myTextArea.setText(body);
 
     if(YAMM.getProperty("signatur") != null && !YAMM.getProperty("signatur").equals("")) {
       try {
         FileInputStream in = new FileInputStream(YAMM.getProperty("signatur"));
         String tmp = null;
         byte[] singb = new byte[in.available()];
 
         in.read(singb);
         myTextArea.append(new String(singb));  
         in.close();
       } catch (IOException ioe) { System.err.println(ioe); }
     }
 
     JTabbedPane JTPane = new JTabbedPane(JTabbedPane.BOTTOM);
     JTPane.addTab(YAMM.getString("mail"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/mail.gif"), new JScrollPane(myTextArea));
 
 
     JPanel myPanel = new JPanel(new BorderLayout());
 
     hori1 = Box.createHorizontalBox();
 
     myButton = new JButton(YAMM.getString("button.add"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/new.gif"));
     myButton.addActionListener(BListener);
     hori1.add(myButton);
 
     myButton = new JButton(YAMM.getString("button.delete"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/delete.gif"));
     myButton.addActionListener(BListener);
     hori1.add(myButton);
 
     attach = new Vector();
     ListModel dataModel = new AbstractListModel() {
       public Object getElementAt(int index) { return attach.elementAt(index); }
       public int    getSize() { return attach.size(); }
     };
 
     myList = new JList(/* attach */ dataModel);
     myList.setCellRenderer(new AttachListRenderer());
     myPanel.add("Center", myList);
     myPanel.add("South", hori1);
 
     Border ram = BorderFactory.createEtchedBorder();
     myList.setBorder(ram);
 
 
     JTPane.addTab(YAMM.getString("mail.attachment"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/attach.gif"), myPanel);
    
     vert3.add(JTPane);
     getContentPane().add(vert3);
 
     addWindowListener(new FLyssnare());
     show();
   }
 
   boolean isSendReady() {
     
     String TF1 = myTextField1.getText();
 
     if(TF1.indexOf('@') != -1) {
       return true;
     }
     else return false;
   }
 
   ActionListener BListener = new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       String arg = ((JButton)e.getSource()).getText();
 
       if(arg.equals(YAMM.getString("button.send"))) {
         if(isSendReady()) {
           try {
             PrintWriter outFile = new PrintWriter(new FileOutputStream(System.getProperty("user.home") + "/.yamm/boxes/" + YAMM.getString("box.outbox"), true));
             SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
 
             String to = myTextField1.getText(), to2 = "", temp = null;
             StringTokenizer tok = new StringTokenizer(to, ",");
 
             while(tok.hasMoreTokens()) {
               temp = tok.nextToken().trim();
 
               if(to2.equals("")) to2 = temp;
               else to2 += "      " + temp;
 
               if(tok.hasMoreTokens()) to2 += ",\n";
             }
 
             if(attach.size() == 0) outFile.println("Date: " + dateFormat.format(new Date()) + "\n" 
                                                  + "From: " + YAMM.getProperty("username", "Anonymous") + " <" + YAMM.getProperty("email", " ") + ">\n"
                                                  + "To: " + to2 + "\n"
                                                  + "Subject: " + myTextField2.getText() + "\n"
                                                 + "X-Mailer: Yet Another Mail Manager " + YAMM.version + "\n"
                                                  + "\n"
                                                  + myTextArea.getText() + "\n"
                                                  + "\n"
                                                  + ".\n");
             else {
               outFile.println("Date: " + dateFormat.format(new Date()) + "\n"
                             + "From: " + YAMM.getProperty("username", "Anonymous") + " <" + YAMM.getProperty("email", " ") + ">" + "\n" 
                             + "To: " + to2 +"\n"
                             + "Subject: " + myTextField2.getText() + "\n"
                            + "X-Mailer: Yet Another Mail Manager " + YAMM.version + "\n" 
                             + "Content-Type: multipart/mixed; boundary=\"AttachThis\"\n"
                             + "\n"
                             + "This is a multi-part message in MIME format.\n"
                             + "\n"
                             + "--AttachThis\nContent-Type: text/plain; charset=us-ascii\n"
                             + "Content-Transfer-Encoding: 7bit\n"
                             + "\n"
                             + myTextArea.getText()
                             + "\n"
                             + "\n");
             }
             outFile.close();
             if(attach.size() > 0) new UUEncode(attach);
           }
           catch (IOException ioe) { System.out.println("Error: " + ioe); }
 
           Rectangle rv = new Rectangle();
           getBounds(rv);
 
           YAMM.setProperty("writex", new Integer(rv.x).toString());
           YAMM.setProperty("writey", new Integer(rv.y).toString());
           YAMM.setProperty("writew", new Integer(rv.width).toString());
           YAMM.setProperty("writeh", new Integer(rv.height).toString());
 
           dispose();
         } else {
             new MsgDialog(YAMMWrite.this, YAMM.getString("msg.error"),
 				"Missing \"@\" in address field!");
 	}
       }
 
       else if(arg.equals(YAMM.getString("button.add"))) {
         addAttach();
       }
 
       else if(arg.equals(YAMM.getString("button.delete"))) {
         int rem = myList.getSelectedIndex();
         if(rem != -1) {
           attach.remove(rem);
           myList.updateUI();
         }
       }
 
       else if(arg.equals(YAMM.getString("button.cancel"))) {
         Rectangle rv = new Rectangle();
         getBounds(rv);
 
         YAMM.setProperty("writex", new Integer(rv.x).toString());
         YAMM.setProperty("writey", new Integer(rv.y).toString());
         YAMM.setProperty("writew", new Integer(rv.width).toString());
         YAMM.setProperty("writeh", new Integer(rv.height).toString());
 
         dispose();
       }
     }
   };
 
   void addAttach() {
     JFileChooser jfs = new JFileChooser();
     jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
     jfs.setMultiSelectionEnabled(true);
     int ret = jfs.showOpenDialog(this);
 
     if(ret == JFileChooser.APPROVE_OPTION) {
       // Why can't I get getSelectedFiles() to work?!
       // File[] file = jfs.getSelectedFiles();
       File[] file = {jfs.getSelectedFile()};
 
       if(file != null) {
 
         for(int i = 0; i < file.length;i++) {
           attach.add(file[i]);
         }
         myList.updateUI();
       }
     }
   }
 
   ActionListener MListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       String kommando = ((JMenuItem)ae.getSource()).getText();
 
       if(kommando.equals(YAMM.getString("button.cancel"))) {
         Rectangle rv = new Rectangle();
         getBounds(rv);
 
         YAMM.setProperty("writex", new Integer(rv.x).toString());
         YAMM.setProperty("writey", new Integer(rv.y).toString());
         YAMM.setProperty("writew", new Integer(rv.width).toString());
         YAMM.setProperty("writeh", new Integer(rv.height).toString());
 
         dispose();
       }
     }
   };
 
   class FLyssnare extends WindowAdapter {
     public void windowClosing(WindowEvent event) {
       Rectangle rv = new Rectangle();
       getBounds(rv);
 
       YAMM.setProperty("writex", new Integer(rv.x).toString());
       YAMM.setProperty("writey", new Integer(rv.y).toString());
       YAMM.setProperty("writew", new Integer(rv.width).toString());
       YAMM.setProperty("writeh", new Integer(rv.height).toString());
 
       dispose();
     }
   }
 }
 
