 package org.valabs.stdobj.console;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Logger;
 
 import org.doomdark.uuid.UUID;
 import org.valabs.odisp.common.Dispatcher;
 import org.valabs.odisp.common.Message;
 
 
 /**     .
  * 
  * @author (C) 2003-2004 <a href="mailto:valeks@novel-il.ru"> . </a>
  * @author (C) 2003-2004 <a href="mailto:dron@novel-il.ru"> . </a>
 * @version $Id: ConsoleReader.java,v 1.19 2005/07/22 13:06:53 dron Exp $
  */
 
 public class ConsoleReader extends Thread {
   /**   . */
   private boolean doExit;
   /** . */
   private static final Logger logger = Logger.getLogger(ConsoleReader.class.getName());
   /**   . */
   private Dispatcher dispatcher;
   /**  . */
   private BufferedReader inp
     = new BufferedReader(new InputStreamReader(System.in));
 
   /**      .
    * 
    * @param disp  ODISP
    */
   public ConsoleReader(final Dispatcher disp) {
     super("ConsoleReader");
     setDaemon(true);
     dispatcher = disp;
   }
 
   /**     -.
    */
   public final void run() {
     try {
       System.out.print("action> ");
       String action, tmp, fieldName;
       while ((action = inp.readLine()) != null) {
         System.out.print("destination> ");
         final Message m
          = dispatcher.getNewMessage(action, inp.readLine(), null, UUID.getNullUUID());
         System.out.print("params? ");
         int paramCount = 0;
         while (!inp.readLine().equals("")) {
           System.out.print("int|str? ");
           tmp = inp.readLine();
           System.out.print("name> ");
           fieldName = inp.readLine();
           System.out.print("value> ");
           if (tmp.startsWith("i")) {
             try {
               m.addField((fieldName.length() > 0) ? fieldName : ("" + paramCount++),
                 new Integer(inp.readLine()));
             } catch (NumberFormatException e) {
               System.out.println("(ConsoleReader) NumberFormatException: please, retry.");
             }
           } else {
             m.addField((fieldName.length() > 0) ? fieldName : ("" + paramCount++),
               "" + inp.readLine());
           }
           System.out.print("more? ");
         }
         m.setCorrect(true);
         dispatcher.send(m);
         if (doExit) {
           break;
         }
         System.out.print("action> ");
       }
     } catch (IOException e) {
       logger.finest("ConsoleReader: Terminal connection lost. Quitting.");
     }
   }
 
   /**  .
    */
   public final synchronized void exit() {
     try {
       inp.close();
       this.interrupt();
     } catch (IOException e) { /*NOP*/ }
     doExit = true;
   }
 }
