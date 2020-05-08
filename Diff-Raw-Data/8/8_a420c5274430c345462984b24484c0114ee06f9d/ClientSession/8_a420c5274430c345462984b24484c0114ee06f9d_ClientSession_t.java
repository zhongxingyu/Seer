 /*
  * Copyright (c) 2009  Mel Nicholson.
  * All Rights Reserved.
  */
 
 package mel.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
import javax.swing.JOptionPane;
 import mel.common.User;
 
 /**
  *
  * @author nicholson
  */
 public class ClientSession
 {
     private final String name;
     private final Socket socket;
     private final PrintWriter out;
     private final BufferedReader in;
     
     private static ClientSession singleton;
 
     ClientSession(String username, String pw, Socket s) throws IOException
     {
         name = username;
         socket = s;
         out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
         new ReadThread().start();
         out.println(name);
         out.println(pw);
         out.flush();
 
         singleton = this;
     }
 
     public static void send(String convName, char opcode, String content)
     {
         singleton.out.println(singleton.name + ":" + convName + ":" + opcode + content);
         singleton.out.flush();
     }
 
     // TODO remove this test code
     public static void flushln(String ln)
     {
         singleton.out.println(ln);
         singleton.out.flush();
     }
 
     public class ReadThread extends Thread
     {
         @Override
         public void run()
         {
             try
             {
                 while(true)
                 {
                     String s = in.readLine();
                     if(s == null) break;
                     else dispatchMessage(s);
                 }
             }
             catch(Exception ex)
             {
                 ex.printStackTrace();
                 System.exit(1);
             }
             try
             {
                 in.close();
             } catch (IOException ex)
             {
                 dispatchMessage("Error: Server connection has been lost.");
             }
         }
     }
 
     public void dispatchMessage(String s)
     {
         //parse user:conversation:Ocontent
         // where O is Opcode character
         String args[] = s.split(":", 3);
         if(args.length != 3 || args[2].length() < 1)
         {
             showError("Server Syntax Error: \n"+s);
             return;
         }
         // names to avoid confusion
         String  userName =           args[0];
         String  conversationName =   args[1];
         char    opcode =             args[2].charAt(0);
         String  content =            args[2].substring(1);
 
         // treat the empty conversation specially
         if("".equals(conversationName))
         {
             handleSystemMessage(userName, opcode, content);
         }
 
        if(opcode == 'E') 
        {
        	JOptionPane.showMessageDialog(null, content, conversationName+" Error", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        
         // spawn conversation window if not already open
         ClientWindow cw = WindowManager.get(conversationName);
         if(cw == null)
         {
             // cannot spawn window without type ==> ask for new join & abort
             if(opcode != 'J')
             {
                 send("lobby", 'J', conversationName);
                 return;
             }
             cw = WindowManager.makeWindow(conversationName, content);
             if(cw == null) return;
         }
         // forward message to conversation window
         cw.handleMessage(userName, opcode, content);
     }
 
     private void handleSystemMessage(String userName, char opcode, String content)
     {
         if(!User.MODERATOR.getName().equals(userName))
         {
             showError(
                     "Illegal Message Received\n\tUser ="+userName+
                     "opcode = "+opcode+
                     "content ="+content
             );
             return;
         }
         switch(opcode)
         {
             case 'E':
                 showError(content);
             break;
             default:
                 showError("Unknown Server Opcode: "+opcode);
             break;
         }
     }
 
     public void showError(String message)
     {
         // TODO RICHARD create an error dialog
     }
 }
