 package client;
 /*
  * dJC: The dAmn Java Client
  * damnChatPage.java
  * 2005 The dAmn Java Project
  *
  * This software and it's source code are distributed under the terms and conditions of the GNU
  * General Public License, Version 2. A copy of this license has been provided.
  * If you do not agree with the terms of this license then please erase all copies
  * of this program and it's source. Thank you.
  */
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.lang.*;
 import java.io.IOException;
 import javax.swing.text.Document;
 import javax.swing.text.html.*;
 
 /**
  * This is the class which manages the chat pages.
  * @author MSF
  */
 public class damnChatPage implements ActionListener, HyperlinkListener, KeyListener {
     private JTabbedPane tabbedPane;
     private ArrayList<JPanel> chatPages;
     private ArrayList<JEditorPane> chatTerminals;
     private ArrayList<JScrollPane> chatScrollPanes;
     ArrayList<JTextField> chatFields;
     private ArrayList<String> channelList;
     private ArrayList<damnChatMemberList> chatMemberLists;
     private damnProtocol dP;
     private damnApp dJ;
     private String awaymsg;
     private String defaultHtml;
     private int searchSet = 0;
 
     /**
      * Initilizes an instance of damnChatPage.
      * @param app A reference to the damnApp object.
      * @param protocol A reference to the damnProtocol object.
      */
     public damnChatPage(damnApp app, damnProtocol protocol) {
         dP = protocol;
         dJ = app;
         chatPages = new ArrayList<JPanel>();
         chatTerminals = new ArrayList<JEditorPane>();
         chatScrollPanes = new ArrayList<JScrollPane>();
         chatFields = new ArrayList<JTextField>();
         channelList = new ArrayList<String>();
         chatMemberLists = new ArrayList<damnChatMemberList>();
         awaymsg = null;
         defaultHtml = new String("<html><head><style type=\"text/css\">\n a { color:#222222 } \n td.tn { margin-right:2px; margin-left: 2px; margin-top:2px; margin-bottom:2px; } </style>"+
                 "</head><body></body></html><html><head><style type=\"text/css\">\n a { color:#222222 } \n td.tn { margin-right:2px; margin-left: 2px; margin-top:2px; margin-bottom:2px; } </style>"+
                 "</head><body></body></html>");
     }
     
     /**
      * Assigns the tabbed pane to the damnChatPage object.
      * @param paneObj The pane to assign.
      */
     public void setPane(JTabbedPane paneObj) {
         tabbedPane = paneObj;
     }
     
     /**
      * Adds a Chat Page
      * @param chatname The name of the channel to add a page for.
      * @param tabbedPane A reference to the Application's Tabbed Pane.
      */
     public void addChatPage(String chatname) {
         JPanel chatPage = new JPanel(new BorderLayout(5,5));
         chatPage.setName("<html><body><font color=\"black\">#" + chatname + "</font></body></html>");
         
         JEditorPane chatTerminal = new JEditorPane();
 
         
         //chatTerminal.setLineWrap(true);
         chatTerminal.setEditable(false);
         chatTerminal.setContentType("text/html");
         chatTerminal.setText(defaultHtml);
         chatTerminal.addHyperlinkListener(this);
         JScrollPane chatScrollPane = new JScrollPane(chatTerminal, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         chatScrollPane.setAutoscrolls(true);
         chatPage.add(chatScrollPane, BorderLayout.CENTER);
         
         JTextField chatField = new JTextField(20);
         chatField.setFocusTraversalKeysEnabled(false);
         chatField.addActionListener(this);
         chatField.addKeyListener(this);
         chatPage.add(chatField, BorderLayout.PAGE_END);
         
         damnChatMemberList memberList = new damnChatMemberList();
         memberList.generateHtml();
         JScrollPane memberListScrollPane = new JScrollPane(memberList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         chatPage.add(memberListScrollPane, BorderLayout.LINE_END);
         
         tabbedPane.add(chatPage);
         
         chatPages.add(chatPage);
         chatTerminals.add(chatTerminal);
         chatScrollPanes.add(chatScrollPane);
         chatFields.add(chatField);
         chatMemberLists.add(memberList);
         channelList.add(chatname);
     }
     
     /** 
      * Deletes a Chat Page
      * @param chatname The name of the channel to delete the page for.
      * @param tabbedPane a reference to the application's Tabbed Pane.
      */
     public void delChatPage(String chatname, JTabbedPane tabbedPane) {
         int index = findPages(chatname);
         
         tabbedPane.remove(chatPages.get(index));
         
         channelList.remove(index);
         chatMemberLists.remove(index);
         chatFields.remove(index);
         chatTerminals.remove(index);
         chatPages.remove(index);
     }
     
     /** 
      * Another standard issue message handler.
      */
     public void actionPerformed(ActionEvent e) {
         JTextField chatField = chatFields.get(chatFields.indexOf(e.getSource()));
         if(chatField.getText().startsWith("/") && chatField.getText().startsWith("/me ") == false
                 && chatField.getText().startsWith("/topic ") == false && chatField.getText().startsWith("/title ") == false
                 && chatField.getText().startsWith("/kick ") == false && chatField.getText().startsWith("/admin ") == false
                 && chatField.getText().equalsIgnoreCase("/clear") == false && chatField.getText().startsWith("/promote ") == false
                 && chatField.getText().startsWith("/demote ") == false) {
             dJ.actionPerformed(e);
         } else {
             if(chatField.getText().startsWith("/topic ") || chatField.getText().startsWith("/title ")) {
                 String channel = channelList.get(chatFields.indexOf(e.getSource()));
                 String comparts[] = chatField.getText().split(" ", 2);
                 dP.doSet(channel.substring(1), comparts[0].substring(1), comparts[1]);
             } else if(chatField.getText().startsWith("/kick ")) {
                 String channel = channelList.get(chatFields.indexOf(e.getSource()));
                 String comparts[] = chatField.getText().split(" ", 3);
                 if(comparts[2] != null) {
                     dP.doKick(channel.substring(1), comparts[1], comparts[2]);
                 } else {
                     dP.doKick(channel.substring(1), comparts[1], " ");
                 }
             } else if(chatField.getText().startsWith("/admin ")) {
                 String channel = channelList.get(chatFields.indexOf(e.getSource()));
                 String parts[] = chatField.getText().split(" ", 2);
                 dP.doAdmin(channel.substring(1), parts[1]);
             } else if(chatField.getText().equalsIgnoreCase("/clear")) {
                 chatTerminals.get(chatFields.indexOf(e.getSource())).setText(defaultHtml);
             } else if(chatField.getText().startsWith("/promote ")) {
                 String channel = channelList.get(chatFields.indexOf(e.getSource()));
                 String parts[] = chatField.getText().split(" ");
                 if(parts.length > 2) {
                     dP.doPromote(channel, parts[1], parts[2]);
                 } else if(parts.length == 2) {
                     dP.doPromote(channel, parts[1], "");
                 } else {
                     dJ.terminalEcho(0, "promote: Not enough arguments.");
                 }
             } else if(chatField.getText().startsWith("/demote ")) {
                 String channel = channelList.get(chatFields.indexOf(e.getSource()));
                 String parts[] = chatField.getText().split(" ");
                 if(parts.length > 2) {
                     dP.doDemote(channel, parts[1], parts[2]);
                 } else if(parts.length == 2) {
                     dP.doDemote(channel, parts[1], "");
                 } else {
                     dJ.terminalEcho(0, "demote: Not enough arguments.");
                 }
             } else {
                 String channel = channelList.get(chatFields.indexOf(e.getSource()));
                 dP.doSendMessage(channel, chatField.getText());
             }
         }
         if(!chatField.getText().equalsIgnoreCase("You are away - you must unset away before you can talk.")) {
             chatField.setText("");
         }
     }
 
     /**
      * Inserts a line into the HTML doucment
      * @param editor the JEditorPane component
      * @param html the line to be inserted
      * @param location the valid location in the document, where to insert
      */
     private void insertHTML(JEditorPane editor, String html, int location)
             throws Exception {
         HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
         Document doc = editor.getDocument();
         StringReader reader = new StringReader(html);
         kit.read(reader, doc, location);
     }
     
     /**
      * Writes a user message to a chat page.
      * @param channel The name of the channel the message is going to.
      * @param user The user the message is coming from.
      * @param message The message.
      */
     public synchronized void echoChat(String channel, String user, String message) {
         JEditorPane chatTerminal = chatTerminals.get(findPages(channel));
         try {
             String highLight = "";
             if(message.toLowerCase().indexOf(dP.getUser().toLowerCase()) != -1) highLight = "bgcolor=\"#BBC2BB\"";
             
             if(message.toLowerCase().startsWith(dP.getUser().toLowerCase())) sendAwayAlert(channel);
             
             String style = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr "+highLight+"><td valign=\"middle\" nowrap>";
             String styleEnd = "</table>";
 
             insertHTML(chatTerminal, style+"&lt;<B>"+ user + "</B>&gt;&nbsp;<td valign=\"middle\">" +message+ styleEnd, chatTerminal.getDocument().getLength());
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         chatTerminal.setCaretPosition(chatTerminal.getDocument().getLength());
 //        chatTerminal.invalidate();
 
     }
     
     
     /**
      * Writes a misc message to a chat page.
      * @param channel The name of the channel the message is going to.
      * @param message The message.
      */
     public synchronized void echoChat(String channel, String message) {
         JEditorPane chatTerminal = chatTerminals.get(findPages(channel));
         try {
             String highLight = "";
             if(message.toLowerCase().indexOf(dP.getUser().toLowerCase()) != -1 && !message.toLowerCase().startsWith("*** " + dP.getUser().toLowerCase())) {
                 highLight =  "bgcolor=\"#BBC2BB\"";
                 sendAwayAlert(channel);
             }
 
             String style = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr "+highLight+"><td valign=\"middle\">";
             String styleEnd = "</table>";
 
             insertHTML(chatTerminal, style+message+styleEnd, chatTerminal.getDocument().getLength());
         } catch (Exception e) {
             e.printStackTrace();
         }
         chatTerminal.setCaretPosition(chatTerminal.getDocument().getLength());
 //        chatTerminal.invalidate();
         
     }
     
     /**
      * Locates the chat page object for a specified channel.
      * @param channel The name of the channel.
      * @return The index of the chat page in the list. Returns -1 if no such page exists.
      */
     private int findPages(String channel) {
         String select;
         for(int i=0; i<channelList.size(); i++) {
             select = channelList.get(i);
             if(select.equalsIgnoreCase(channel)) {
                 return i;
             }
         }
         
         return -1;
     }
     
     /**
      * Locates a member list.
      * @param channel The channel to locate a list for.
      * @return A DefaultListModel object for the member list.
      */
     public damnChatMemberList getMemberList(String channel) {
         return chatMemberLists.get(findPages(channel));
     }
     
     /**
      * Sets the object to away mode.
      * @param message The away message.
      */
     public void setAway(String message) {
         awaymsg = message;
         for(int i=0;i <chatFields.size();i++) {
             chatFields.get(i).setText("You are away - you must unset away before you can talk.");
             chatFields.get(i).setEditable(false);
             dP.doSendMessage(chatPages.get(i).getName().substring(1),  "/me is away: " + message);
         }
     }
     
     /**
      * Unsets the away mode.
      */
     public void unsetAway() {
         awaymsg = null;
         for(int i=0;i<chatFields.size();i++) {
             chatFields.get(i).setText("");
             chatFields.get(i).setEditable(true);
             dP.doSendMessage(chatPages.get(i).getName().substring(1), "/me is back.");
         }
     }
     
     /**
      * Will send the away alert if the away state is enabled.
      * @param channel The Channel to send the alert to.
      */
     public void sendAwayAlert(String channel) {
         if(awaymsg != null) {
             dP.doSendMessage(channel, "/me is away: " + awaymsg);
         }
     }
 
     public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
         if(hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
             /*try {
                 Runtime.getRuntime().exec("config.xml");
             } catch(IOException e) {
                 e.printStackTrace();
             }*/
         }
     }
 
     public void keyPressed(KeyEvent keyEvent) {
     }
 
     public void keyReleased(KeyEvent keyEvent) {
     }
 
     public void keyTyped(KeyEvent keyEvent) {
         damnChatMemberList memList = chatMemberLists.get(chatFields.indexOf(keyEvent.getSource()));
         if(keyEvent.getKeyChar() == '\t') {
             JTextField field = chatFields.get(chatFields.indexOf(keyEvent.getSource()));
             
             String[] parts = field.getText().split(" ");
             
             String result = memList.searchAgent(parts[parts.length-1]);
             field.setText("");
             
             for(int i=0;i<parts.length;i++) {
                 if(i < parts.length-1) {
                     field.setText(field.getText() + parts[i] + " ");
                 } else {
                    field.setText(field.getText() + result);
                 }
             }
         } else {
             memList.searchAgentReset();
         }
     }
 }
