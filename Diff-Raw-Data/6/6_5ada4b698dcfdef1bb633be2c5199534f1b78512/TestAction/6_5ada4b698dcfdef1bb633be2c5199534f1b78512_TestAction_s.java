 package org.tanukisoftware.wrapper.test;
 
 /*
  * Copyright (c) 1999, 2004 Tanuki Software
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of the Java Service Wrapper and associated
  * documentation files (the "Software"), to deal in the Software
  * without  restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sub-license,
  * and/or sell copies of the Software, and to permit persons to
  * whom the Software is furnished to do so, subject to the
  * following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  * 
  * 
  * Portions of the Software have been derived from source code
  * developed by Silver Egg Technology under the following license:
  * 
  * Copyright (c) 2001 Silver Egg Technology
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sub-license, and/or 
  * sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following 
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  */
 
 // $Log$
 // Revision 1.8  2004/03/27 14:39:20  mortenson
 // Add actions for the stopImmediate method.
 //
 // Revision 1.7  2004/01/16 04:41:55  mortenson
 // The license was revised for this version to include a copyright omission.
 // This change is to be retroactively applied to all versions of the Java
 // Service Wrapper starting with version 3.0.0.
 //
 // Revision 1.6  2004/01/15 09:50:30  mortenson
 // Fix some problems where the Wrapper was not handling exit codes correctly.
 //
 // Revision 1.5  2004/01/10 15:44:15  mortenson
 // Rework the test wrapper app so there is less code duplication.
 //
 // Revision 1.4  2003/10/18 07:51:10  mortenson
 // The DeadlockPrintStream should not be set until after the WrapperManager class
 // has been initialized.
 //
 // Revision 1.3  2003/10/18 07:35:30  mortenson
 // Add test cases to test how the wrapper handles it when the System.out stream
 // becomes deadlocked.  This can happen if buggy usercode overrides those streams.
 //
 // Revision 1.2  2003/04/03 04:05:22  mortenson
 // Fix several typos in the docs.  Thanks to Mike Castle.
 //
 // Revision 1.1  2003/02/03 06:55:29  mortenson
 // License transfer to TanukiSoftware.org
 //
 
 import org.tanukisoftware.wrapper.WrapperManager;
 import org.tanukisoftware.wrapper.WrapperListener;
 
 /**
  *
  *
  * @author Leif Mortenson <leif@tanukisoftware.com>
  * @version $Revision$
  */
 public class TestAction
     extends AbstractActionApp
     implements WrapperListener
 {
     private ActionRunner m_actionRunner;
     
     /**************************************************************************
      * Constructors
      *************************************************************************/
     private TestAction() {
     }
 
     /**************************************************************************
      * WrapperListener Methods
      *************************************************************************/
     public Integer start(String[] args) {
         Thread actionThread;
 
         System.out.println("start()");
         
         if (args.length <= 0)
             printHelp("Missing action parameter.");
 
         prepareSystemOutErr();
         
         // * * Start the action thread
         m_actionRunner = new ActionRunner(args[0]);
         actionThread = new Thread(m_actionRunner);
         actionThread.start();
 
         return null;
     }
     
     public int stop(int exitCode) {
         System.out.println("stop(" + exitCode + ")");
         
         return exitCode;
     }
     
     public void controlEvent(int event) {
         System.out.println("controlEvent(" + event + ")");
         if (event == WrapperManager.WRAPPER_CTRL_C_EVENT) {
             //WrapperManager.stop(0);
             m_actionRunner.endThread();
         }
     }
 
     /**************************************************************************
      * Inner Classes
      *************************************************************************/
     private class ActionRunner implements Runnable {
         private String m_action;
         private boolean m_alive;
         
         public ActionRunner(String action) {
             m_action = action;
             m_alive = true;
         }
     
         public void run() {
             // Wait for 5 seconds so that the startup will complete.
             try {
                 Thread.sleep(5000);
             } catch (InterruptedException e) {}
             
             if (!TestAction.this.doAction(m_action)) {
                 printHelp("\"" + m_action + "\" is an unknown action.");
                 WrapperManager.stop(0);
                 return;
             }
     
             while (m_alive) {
                 // Idle some
                 try {
                    Thread.currentThread().sleep(500);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
     
         public void endThread( ) {
             m_alive = false;
         }
     }
     
     /**
      * Prints the usage text.
      *
      * @param error_msg Error message to write with usage text
      */
     private static void printHelp(String error_msg) {
         System.err.println( "USAGE" );
         System.err.println( "" );
         System.err.println( "TestAction <action>" );
         System.err.println( "" );
         System.err.println( "[ACTIONS]" );
         System.err.println( "  Actions which should cause the Wrapper to exit cleanly:" );
         System.err.println( "   stop0                    : Calls WrapperManager.stop(0)" );
         System.err.println( "   exit0                    : Calls System.exit(0)" );
         System.err.println( "   stopimmediate0           : Calls WrapperManager.stopImmediate(0)" );
         System.err.println( "  Actions which should cause the Wrapper to exit in an error state:" );
         System.err.println( "   stop1                    : Calls WrapperManager.stop(1)" );
         System.err.println( "   exit1                    : Calls System.exit(1)" );
         System.err.println( "   stopimmediate1           : Calls WrapperManager.stopImmediate(1)" );
         System.err.println( "  Actions which should cause the Wrapper to restart the JVM:" );
         System.err.println( "   access_violation         : Calls WrapperManager.accessViolation" );
         System.err.println( "   access_violation_native  : Calls WrapperManager.accessViolationNative()" );
         System.err.println( "   appear_hung              : Calls WrapperManager.appearHung()" );
         System.err.println( "   halt                     : Calls Runtime.getRuntime().halt(0)" );
         System.err.println( "   restart                  : Calls WrapperManager.restart()" );
         System.err.println( "  Additional Tests:" );
         System.err.println( "   dump                     : Calls WrapperManager.requestThreadDump()" );
         System.err.println( "   deadlock_out             : Deadlocks the JVM's System.out and err streams." );
         System.err.println( "   users                    : Start polling the current and interactive users." );
         System.err.println( "   groups                   : Start polling the current and interactive users with groups." );
         System.err.println( "" );
         System.err.println( "[EXAMPLE]" );
         System.err.println( "   TestAction access_violation_native " );
         System.err.println( "" );
         System.err.println( "ERROR: " + error_msg );
         System.err.println( "" );
 
         System.exit( -1 );
     }
 
     /**************************************************************************
      * Main Method
      *************************************************************************/
     public static void main(String[] args) {
         System.out.println("Initializing...");
         
         // Start the application.  If the JVM was launched from the native
         //  Wrapper then the application will wait for the native Wrapper to
         //  call the application's start method.  Otherwise the start method
         //  will be called immediately.
         WrapperManager.start(new TestAction(), args);
     }
 }
 
