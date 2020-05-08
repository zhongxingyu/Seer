 /* HTMLPane.java
  *
  * Created April 7, 2009
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient.utils;
 import java.awt.Color;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.swing.*;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.StyleSheet;
 import shoddybattleclient.LobbyWindow;
 import shoddybattleclient.Preference;
 
 /**
  *
  * @author ben
  */
 public class HTMLPane extends JTextPane {
 
     private static final int MAXIMUM_LINES = 500;
 
 
     private int m_lines = 0;
     //This being true means to use the user's settings
     private boolean m_timestamps = true;
 
     public HTMLPane() {
         super();
         setContentType("text/html");
         setEditable(false);
         setBackground(Color.WHITE);
         setOpaque(true);
         HTMLEditorKit kit = new HTMLEditorKit();
         StyleSheet css = new StyleSheet();
         css.importStyleSheet(HTMLPane.class.getResource("/shoddybattleclient/resources/main.css"));
         kit.setStyleSheet(css);
         setEditorKit(kit);
 
         this.addHyperlinkListener(new HyperlinkListener() {
             public void hyperlinkUpdate(HyperlinkEvent e) {
                 if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                     LobbyWindow.viewWebPage(e.getURL());
                 }
             }
         });
     }
 
     public void setTimeStampsEnabled(boolean enabled) {
         m_timestamps = enabled;
     }
 
     /**
      * Encode HTML entities.
      * Copied from www.owasp.org.
      */
     public static String htmlEntityEncode(String s) {
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             if ((c >= 'a' && c <= 'z') ||
                     (c >= 'A' && c <= 'Z') ||
                     (c >= '0' && c <= '9')) {
                 buf.append(c);
             } else {
                 buf.append("&#" + (int)c + ";");
             }
         }
         return buf.toString();
     }
 
     public void addMessage(String user, String message) {
         addMessage(user, message, true);
     }
 
     public void addMessage(String user, String message, boolean encode) {
         if (message == null) return;
         if (encode) {
             message = htmlEntityEncode(message);
         }
         StringBuffer buffer = new StringBuffer();
         if (m_timestamps && Preference.timeStampsEnabled()) {
             try {
                 Date d = new Date();
                 String format = Preference.getTimeStampFormat();
                 format = htmlEntityEncode(format);
                 SimpleDateFormat f = new SimpleDateFormat(format);
                 buffer.append("<font class='timestamp'>");
                 buffer.append(f.format(d));
                 buffer.append("</font>");
             } catch (Exception e) {
                 
             }
         }
         if (user != null) {
             buffer.append(user);
             buffer.append(": ");
         }
         buffer.append(message);
         String msg = new String(buffer);
         msg = msg.replaceAll("&#32;", " ")
                         .replaceAll("\\b([^ ]*&#58;&#47;&#47;[^ ]+)",
                             "<a href=\"$1\">$1</a>");
         HTMLDocument doc = (HTMLDocument)getDocument();
         HTMLEditorKit kit = (HTMLEditorKit)getEditorKit();
         try {
             kit.insertHTML(doc, doc.getLength(), msg, 1, 0, null);
             if (++m_lines > MAXIMUM_LINES) {
                 int position = 0;
                 int index;
                 while (true) {
                     index = doc.getText(position, doc.getLength()).indexOf("\n");
                     if (index != 0) {
                         position += index;
                         break;
                     } else {
                         position += 1;
                     }
                 }
                 doc.remove(0, position);
             }
             //doc.dump(new java.io.PrintStream(new java.io.FileOutputStream("out.txt")));
         } catch (Exception e) {
             
         }
         
         //scroll only if we are already at the bottom
         JScrollBar vbar = ((JScrollPane)this.getParent().getParent()).getVerticalScrollBar();
         if ((vbar.getValue() + vbar.getVisibleAmount()) == vbar.getMaximum()) {
             this.setCaretPosition(doc.getLength());
         }
     }
 
     private void clear() {
         setText("");
         m_lines = 0;
     }
 
     public static void main(String[] args) {
         JFrame frame = new JFrame("test htmlpane");
         frame.setSize(500, 300);
         frame.setLayout(new java.awt.GridLayout(1, 1));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         final HTMLPane pane = new HTMLPane();
         JScrollPane scrollPane = new JScrollPane();
         scrollPane.setViewportView(pane);
         frame.add(scrollPane);
         pane.addMessage("Ben", "I am a polymath");
         pane.addMessage("Catherine", "I agree wholeheartedly");
         pane.addMessage("Catherine", "check out http://cathyisnotapolymath.com");
         pane.addMessage(null, "<b>=========================</b>", false);
         pane.addMessage(null, "<b>Begin turn #1</b>", false);
         pane.addMessage(null, "<b>=========================</b>", false);
         pane.addMessage("Ben", "<hr>'s are ugly");
         frame.setVisible(true);
         /*new Thread(new Runnable() {
             public void run() {
                 for (int i = 0; ; i++) {
                     pane.addMessage("spammer", String.valueOf(i));
                     synchronized (this) {
                         try {
                             wait(100);
                         } catch (InterruptedException ex) {
 
                         }
                     }
                 }
             }
         }).run();*/
         
     }
 }
