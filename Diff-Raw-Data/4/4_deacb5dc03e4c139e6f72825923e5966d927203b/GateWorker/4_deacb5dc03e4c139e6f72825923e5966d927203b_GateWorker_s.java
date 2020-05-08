 /**
  * This file is part of RibbonWeb application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package controllers;
 
 import play.db.ebean.Model;
 
 /**
  * Gate connector thread class. This class is almost a copy 
  * of AppComponents.NetWorker class in libRibbonApp
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class GateWorker extends Thread{
     
     /**
      * Client socket object.
      */
     public java.net.Socket clientSocket;
     
     /**
      * Input stream (from server).
      */
     public java.io.BufferedReader inStream;
     
     /**
      * Output stream (to server).
      */
     public java.io.PrintWriter outStream;
     
     /**
      * Display is NetWorker connection is alive.
      */
     public Boolean isAlive = false;
     
     /**
      * Array of protocol listeners.
      */
     public AppComponents.Listener[] NetListeners;
     
     /**
      * Define NetWorker collect/execute behavior.
      * <p>Modes:
      * <ul>
      * <li>0 - execute commands;</li>
      * <li>1 - collect single input line</li>
      * <li>2 - collect all line before END: command emmited;</li>
      * </ul>
      * </p>
      */
     protected Integer collectState = 0;
     
     /**
      * String buffer for collected data.
      */
     protected StringBuffer collectBuf = new StringBuffer();
     
     /**
      * Personal collect lock object.
      */
     private final Object collectLock = new Object();
     
     private Boolean atomFlag = false;
     
     private final Object atomLock = new Object();
     
     /**
      * Try connect to server.
      * @param givenAddress address to connect;
      * @param givenPort port to connect;
      */
     public void tryConnect(String givenAddress, Integer givenPort) {
         this.NetListeners = getProtocol();
         try {
             clientSocket = new java.net.Socket(givenAddress, givenPort);
             inStream = new java.io.BufferedReader(new java.io.InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
             outStream = new java.io.PrintWriter(clientSocket.getOutputStream(), true);
             isAlive = true;
         } catch (java.io.IOException ex) {MiniGate.gateErrorStr = "Неможливо встановити з’єднання!";}
     }
     
     /**
      * Get protocol listeners (should be overriden).
      * @return array of listeners;
      */
     public AppComponents.Listener[] getProtocol() {
         return new AppComponents.Listener[] {
             
             //Disable gate if server closing connection.
             new AppComponents.Listener("COMMIT_CLOSE") {
                 @Override
                 public void exec(String args) {
                     System.out.println("Завершення роботи гейта.");
                     isAlive = false;
                 }
             },
             
             //Mark database message entry with server command.
             new AppComponents.Listener("RIBBON_UCTL_LOAD_INDEX") {
                 @Override
                 public void exec(String args) {
                     MessageClasses.MessageEntry currMessage = new MessageClasses.MessageEntry(args);
                     synchronized (MiniGate.sender.getLock()) {
                         if (currMessage.getProperty("REMOTE_ID") != null) {
                             models.MessageProbe gettedProbe = (models.MessageProbe) new Model.Finder(String.class, models.MessageProbe.class).byId(currMessage.getProperty("REMOTE_ID").TEXT_MESSAGE);
                             if (gettedProbe != null) {
                                 gettedProbe.ribbon_index = currMessage.INDEX;
                                 gettedProbe.update();
                             }
                         }
                         models.MessageProbe gettedProbe2 = (models.MessageProbe) new Model.Finder(String.class, models.MessageProbe.class).where().eq("ribbon_index", currMessage.ORIG_INDEX).findUnique();
                         if (gettedProbe2 != null) {
                             gettedProbe2.curr_status = models.MessageProbe.STATUS.ACCEPTED;
                             gettedProbe2.update();
                         }
                     }
                 }
             },
             
             new AppComponents.Listener("RIBBON_UCTL_UPDATE_INDEX") {
                 @Override
                 public void exec(String args) {
                     MessageClasses.MessageEntry currMessage = new MessageClasses.MessageEntry(args);
                     synchronized (MiniGate.sender.getLock()) {
                         models.MessageProbe gettedProbe = (models.MessageProbe) new Model.Finder(String.class, models.MessageProbe.class).where().eq("ribbon_index", currMessage.INDEX).findUnique();
                         if (gettedProbe != null) {
                             if (gettedProbe.curr_status == models.MessageProbe.STATUS.WAIT_CONFIRM) {
                                 gettedProbe.curr_status = models.MessageProbe.STATUS.POSTED;
                                 gettedProbe.update(); 
                             } else {
                                 gettedProbe.curr_status = models.MessageProbe.STATUS.ACCEPTED;
                                 gettedProbe.update(); 
                             }
                         }
                     }
                 }
             }
         };
     }
     
     @Override
     public void run() {
          while (isAlive) {
             try {
                 synchronized (atomLock) {
                     atomFlag = true;
                     atomLock.notifyAll();
                 }
                 String inputLine = null;
                 inputLine = inStream.readLine();
                 System.out.println("SERV: " + inputLine);
                 if (inputLine == null) {
                     throw new NullPointerException();
                 }
                 synchronized (collectLock) {
                     synchronized (atomLock) {
                         atomFlag = false;
                     }
                     System.out.println("COLLECT = " + collectState);
                     switch (collectState) {
                         case 0:
                             try {
                                 this.exec(inputLine);
                             } catch (Exception ex) {
                                 ex.printStackTrace();
                             }
                             break;
                         case 1:
                             collectBuf.append(inputLine);
                             collectState = 0;
                             System.out.println("<RET");
                             collectLock.notify();
                             break;
                         case 2:
                             if (!inputLine.startsWith("END:")) {
                                 collectBuf.append(inputLine + "\n");
                                 if (inputLine.startsWith("RIBBON_ERROR:")) {
                                     collectState = 0;
                                     System.out.println("<ERROR");
                                     collectLock.notify();
                                 }
                             } else {
                                 collectState = 0;
                                 System.out.println("<COLLECT");
                                 collectLock.notify();
                             }
                     }
                 }
             } catch (java.io.IOException ex) {
                 isAlive = false;
             } catch (NullPointerException ex) {
                 MiniGate.isGateReady = false;
                 MiniGate.gateErrorStr = "Сервер не відповідає!";
                 this.closeGate();
                 MiniGate.gate = new GateWorker();
                 MiniGate.init();
                 break;
             }
         }
     }
     
     /**
      * Execute command with listeners array.
      * @param inputCommand command to execute;
      */
     private void exec(String inputCommand) {
         String[] parsedCommandStruct = Generic.CsvFormat.parseDoubleStruct(inputCommand);
         Boolean executed = false;
         for (AppComponents.Listener currListener : NetListeners) {
             if (parsedCommandStruct[0].equals(currListener.COMM_NAME)) {
                 currListener.exec(parsedCommandStruct[1]);
                 executed = true;
             }
         }
         if (!executed) {}
     }
     
     /**
      * Send command to the server.
      * @param givenCommand command to send;
      */
     public void sendCommand(String givenCommand) {
         synchronized (atomLock) {
             if (!atomFlag) {
                 try {
                     System.out.println("===WAIT===");
                     atomLock.wait();
                 } catch (InterruptedException ex) {}
             }
         }
         System.out.println("===PRINT===");
         outStream.println(givenCommand);
     }
     
     /**
      * Send command and return server status.
      * @param givenCommand command to send;
      * @return return status line from server;
      */
     public String sendCommandWithReturn(String givenCommand) {
         String respond = null;
         this.collectState = 1;
         System.out.println(">RET: " + givenCommand);
         sendCommand(givenCommand);
         synchronized (collectLock) {
             while (collectState == 1) {
                 try {
                     collectLock.wait();
                 } catch (InterruptedException ex) {}
             }
             respond = collectBuf.toString();
             collectBuf = new StringBuffer();
         }
         return respond;
     }
     
     /**
      * Send command and return command status.
      * @param givenCommand command to send;
      * @return return null if respond is OK: or String if error ocurred;
      */
     public String sendCommandWithCheck(String givenCommand) {
         String respond = sendCommandWithReturn(givenCommand);
         if (respond.startsWith("OK:") || respond.startsWith("PROCEED:")) {
             return null;
         } else if (respond.startsWith("RIBBON_ERROR")){
             return respond.substring(respond.indexOf(':') + 1);
         } else {
             this.exec(respond);
             return null;
         }
     }
     
     /**
      * Send command and get input stream to END: command break.
      * @param givenCommand command to send;
      * @return all lines to END:;
      */
     public String sendCommandWithCollect(String givenCommand) {
         String respond;
         synchronized (collectLock) {
             System.out.println(">COLLECT: " + givenCommand);
             this.collectState = 2;
             sendCommand(givenCommand);
             while (collectState == 2) {
                 try {
                     collectLock.wait();
                 } catch (InterruptedException ex) {
                     
                 }
             }
             respond = collectBuf.toString();
             collectBuf = new StringBuffer();
         }
         return respond;
     }
     
     /**
      * Close this gate connection.
      */
     public void closeGate() {
        outStream.println("RIBBON_NCTL_CLOSE:");
     }
     
     /**
      * Collect stream to END: command break.<br>
      * <p><b>WARNING! This method don't use synchronization with <code>socketLock</code>!</b></p>
      * @return all lines to END:;
      */
     public String collectToEnd() {
         StringBuffer buf = new StringBuffer();
         Boolean keepCollect = true;
         while (keepCollect) {
             try {
                 String inputLine = inStream.readLine();
                 if (!inputLine.equals("END:")) {
                     buf.append(inputLine);
                     buf.append("\n");
                 } else {
                     keepCollect = false;
                 }
             } catch (java.io.IOException ex) {
                 isAlive = false;
             }
         }
         return buf.toString();
     }
     
     /**
      * Send command with context switching and return answer from server;
      * @param givenUser name form using context;
      * @return answer from server.
      */
     public String sendCommandWithContextCollect(String givenUser, String givenCommand) {
         String contextErr = this.sendCommandWithCheck("RIBBON_NCTL_ACCESS_CONTEXT:{" + givenUser + "}");
         if (contextErr != null) {
             return contextErr;
         }
         return this.sendCommandWithCollect(givenCommand);
     }
     
     /**
      * Send command with context switching and return answer from server;
      * @param givenUser name form using context;
      * @return answer from server.
      */
     public String sendCommandWithContextReturn(String givenUser, String givenCommand) {
         String contextErr = this.sendCommandWithCheck("RIBBON_NCTL_ACCESS_CONTEXT:{" + givenUser + "}");
         if (contextErr != null) {
             return contextErr;
         }
         return this.sendCommandWithReturn(givenCommand);
     }
 }
 
