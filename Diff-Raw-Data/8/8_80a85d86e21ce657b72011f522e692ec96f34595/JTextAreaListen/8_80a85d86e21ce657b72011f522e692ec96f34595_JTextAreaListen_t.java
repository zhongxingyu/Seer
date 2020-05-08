 package client;
 
 import javax.swing.*;
 
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.*;
 
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 
 
 public class JTextAreaListen extends JFrame
         implements DocumentListener, KeyListener, ActionListener, CaretListener {
      
 
     private static final long serialVersionUID = 6950001634065526391L;
 
     private JTextArea textArea;
      
     protected final PrintWriter out;
     protected final int id;
     protected final BufferedReader breader;
     private static int caretPos;
     private static int cMark;
     protected static boolean text_selected;
     
      
 /*
  * Connecting to server.
 (1337 is the port we are going to use).
 
 socket = new Socket(InetAddress.getByName("127.0.0.1"), 1337);
 
 (Open a new outStream, you can save this instead of opening on every time you want to send a message. If the connection is lost your should to out.close();
 
 out = new PrintWriter(socket.getOutputStream(), true);
 
 out.print("message"); to send something to the server.
  */
      
      
     public JTextAreaListen(Socket socket, PrintWriter out, BufferedReader breader, int id) {
         super("JTextAreaListen");
         
          
         textArea.getDocument().addDocumentListener(this);
        textArea.addCaretListener(this);
        textArea.addKeyListener(this);
         InputMap im = textArea.getInputMap();
         ActionMap am = textArea.getActionMap();
 
     }
      
     // Listener methods
      
     public void changedUpdate(DocumentEvent ev) {
     }
      
     public void removeUpdate(DocumentEvent ev) { 
         
     }
      
     public void insertUpdate(DocumentEvent ev) {
     }
      
 
     @Override
     public void actionPerformed(ActionEvent arg0) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void keyPressed(KeyEvent arg0) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void keyReleased(KeyEvent arg0) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void keyTyped(KeyEvent ev) {
         int evID = ev.getID();
         String keyString;
         int keyCode;
         if (evID == KeyEvent.KEY_TYPED) {
             if (ev.getKeyChar()==KeyEvent.CHAR_UNDEFINED){
                 keyCode = ev.getKeyCode();
                 if (keyCode==8){
                     if (text_selected){
                         if (caretPos > cMark){
                             for (int i = caretPos; i>=cMark; i--){
                                 sendMessage("DELETE" + " " + String.valueOf(id) + " " + String.valueOf(cMark+1));
                             }
                         }
                         else if(caretPos < cMark){
                             for (int i = caretPos; i>=cMark; i++){
                                 sendMessage("DELETE" + " " + String.valueOf(id) + " " + String.valueOf(caretPos+1));
                             }
                         }
                     }
                     else{
                         sendMessage("DELETE" + " " + String.valueOf(id) + " " + String.valueOf(caretPos+1));
                     }
                 }
             }
             else{
                 char c = ev.getKeyChar();
                 if (text_selected){
                     if (caretPos > cMark){
                         for (int i = caretPos; i>=cMark; i--){
                             sendMessage("DELETE" + " " + String.valueOf(id) + " " + String.valueOf(cMark+1));
                         }
                         sendMessage("INSERT" + " " + String.valueOf(id) + " " + String.valueOf(cMark) + " " + String.valueOf(c));
                     }
                     else if(caretPos < cMark){
                         for (int i = caretPos; i >=cMark; i++){
                             sendMessage("DELETE" + " "+ String.valueOf(id) + " " + String.valueOf(caretPos+1));
                         }
                         sendMessage("INSERT" + " " + String.valueOf(id) + " " + String.valueOf(caretPos) + " " + String.valueOf(c));
                     }
                 }
                 else{
                     sendMessage("INSERT" + " " + String.valueOf(id) + " " + String.valueOf(caretPos) + " " + String.valueOf(c));
                 }
             }
             
         } 
 
     }
     
 
     
     public void sendMessage(String s){
         out.print(s);
     }
 
     @Override
     public void caretUpdate(CaretEvent cev) {
         int dot  = cev.getDot();
         int mark = cev.getMark();
         caretPos = dot;
         cMark = mark;
         if (dot == mark){
             text_selected = false;
         }
         else if((dot < mark) | (dot > mark)){
             text_selected = true;
         }
     }
    
    
      
      
 }
