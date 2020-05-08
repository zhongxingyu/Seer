 package com.novel.stdobj.guiconsole;
 import com.novel.odisp.common.*;
 import java.util.regex.*;
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 /**  ODISP      
  * @author  . 
  * @author (C) 2003,  "-"
 * @version $Id: GUIConsole.java,v 1.3 2003/10/16 22:10:20 dron Exp $
  */
 public class GUIConsole extends PollingODObject {
    JFrame mFrame;
    JTextArea mTextArea1;
    JButton mButton1;
    JButton mButton2;
    JTextField mEdit1;
    public void handleMessage(Message msg) {
       log("handleMessage", "processing "+msg);
    	if(msg.getAction().equals("od_cleanup")) {
          cleanUp(((Integer)msg.getField(0)).intValue());
       }
       else {
          mTextArea1.append("Received:\n");
          mTextArea1.append(msg.toString()+"\n");
          if(msg.getFieldsCount() > 0) {
             mTextArea1.append("Fields dump:\n");
          }
          for(int i = 0;i<msg.getFieldsCount();i++) {
             mTextArea1.append(i+":\n");
             mTextArea1.append(msg.getField(i).toString()+"\n");
          }
       }
       return;
    }
 
    /* CleanUp
    */
    public int cleanUp(int type) {
       mFrame.setVisible(false);
      mFrame.hide();
      // mFrame.postEvent(Event.WINDOW_DESTROY);
       try {
          synchronized(this) {
             wait(1000);
          }
       } catch(InterruptedException e) {
          log("cleanUp", "Exception while wait: "+e);
       }
       synchronized(mFrame) {
          mFrame.dispose();
       }
       doExit = true;
       return 0;
    }
 
    private void parseCommand(String stCmd) {
       String stContent[] = stCmd.split(" ");
       for(int i=0; i < stContent.length; i++) {
          mTextArea1.append("["+i+"]"+stContent[i]+"\n");
       }
       if(stContent.length > 1) {
          Message newMsg = dispatcher.getNewMessage(stContent[0], stContent[1], getObjectName(), 0);
          if(stContent.length % 2 == 0) {
             for(int i=0; i < Math.round((stContent.length - 2)/2); i++) {
                if(stContent[i+2+0].startsWith("i")) {
                   try {
                      newMsg.addField(new Integer(stContent[i + 2 + 1]));
                   } catch(NumberFormatException e) {
                      mTextArea1.append("Exception while converting string to integer: "+e+"\n");
                   }
                }
                else {
                   newMsg.addField(new String(stContent[i + 2 + 1]));
                }
             }
          }
          getDispatcher().sendMessage(newMsg);
       }
       mEdit1.selectAll();
    }
    
    public GUIConsole(Integer id) {
       super("guiconsole"+id);
       mFrame = new JFrame("GUIConsole");
      mFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       mFrame.setSize(600, 400);
       mTextArea1 = new JTextArea();
       mEdit1 = new JTextField(20);
       mButton1 = new JButton("Send");
       mButton2 = new JButton("Clear");
      
       mButton1.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
             parseCommand(mEdit1.getText());
          }
       });
      
       mButton2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                mTextArea1.setText(new String(""));
             }
          });
 
       mEdit1.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             //mTextArea1.append(e.getActionCommand()+"\n");
             parseCommand(mEdit1.getText());
          }
       });
       
       JPanel mPanel1 = new JPanel();
       
       // mPanel1.setLayout(new GridLayout(1, 1));
       mPanel1.add(BorderLayout.CENTER, new JScrollPane(mTextArea1/*,
                                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER*/));
 
       JPanel mPanel2 = new JPanel();
       mPanel1.setLayout(new BoxLayout(mPanel1, BoxLayout.X_AXIS));
       mPanel2.add(mEdit1);
       mPanel2.add(mButton1);
       mPanel2.add(mButton2);
 
       Container cp = mFrame.getContentPane();
       cp.add(BorderLayout.CENTER, mPanel1);
       cp.add(BorderLayout.SOUTH, mPanel2);
       // mFrame.pack();
       mFrame.setVisible(true);
    }
    
    /**    
    */
    public String[] getProviding() {
       String res[] = {"testGUI"};
       return res;
    }
    
    /**   
    */
    public String[] getDepends() {
       String res[] = {"stddispatcher"};
       return res;
    }
 
 }
