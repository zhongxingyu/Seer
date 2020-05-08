 package controllers;
 
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
     
     /**
      * Try connect to server;
      */
     public void tryConnect() {
         this.NetListeners = getProtocol();
         try {
             clientSocket = new java.net.Socket("127.0.0.1", 3003);
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
             new AppComponents.Listener("COMMIT_CLOSE") {
                 @Override
                 public void exec(String args) {
                     System.out.println("Завершення роботи гейта.");
                     isAlive = false;
                 }
             }
         };
     }
     
     @Override
     public void run() {
          while (isAlive) {
             try {
                 String inputLine = inStream.readLine();
                 System.out.println(inputLine);
                 switch (collectState) {
                     case 0:
                         this.exec(inputLine);
                         break;
                     case 1:
                         synchronized (collectLock) {
                             collectBuf.append(inputLine);
                             collectState = 0;
                             collectLock.notify();
                         }
                         break;
                     case 2:
                         synchronized (collectLock) {
                             if (!inputLine.startsWith("END:")) {
                                 collectBuf.append(inputLine + "\n");
                                 if (inputLine.startsWith("RIBBON_ERROR:")) {
                                     collectState = 0;
                                     collectLock.notify();
                                 }
                             } else {
                                 collectState = 0;
                                 collectLock.notify();
                             }
                         }
                 }
             } catch (java.io.IOException ex) {
                 isAlive = false;
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
         outStream.println(givenCommand);
     }
     
     /**
      * Send command and return server status.
      * @param givenCommand command to send;
      * @return return status line from server;
      */
     public String sendCommandWithReturn(String givenCommand) {
         String respond;
         this.collectState = 1;
         outStream.println(givenCommand);
         synchronized (collectLock) {
             while (collectState == 1) {
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
      * Send command and return command status.
      * @param givenCommand command to send;
      * @return return null if respond is OK: or String if error ocurred;
      */
     public String sendCommandWithCheck(String givenCommand) {
         System.out.println(givenCommand);
         String respond;
         this.collectState = 1;
         outStream.println(givenCommand);
         synchronized (collectLock) {
             while (collectState == 1) {
                 try {
                     collectLock.wait();
                 } catch (InterruptedException ex) {
                     
                 }
             }
             respond = collectBuf.toString();
             collectBuf = new StringBuffer();
         }
         if (respond.startsWith("OK:")) {
             return null;
         } else {
             return respond.substring(respond.indexOf(':') + 1);
         }
     }
     
     /**
      * Send command and get input stream to END: command break.
      * @param givenCommand command to send;
      * @return all lines to END:;
      */
     public String sendCommandWithCollect(String givenCommand) {
         String respond;
         this.collectState = 2;
         outStream.println(givenCommand);
         synchronized (collectLock) {
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
 }
 
