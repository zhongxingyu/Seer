 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swing;
 
 import java.awt.AWTError;
 import java.awt.AWTEvent;
 import java.awt.Toolkit;
 import java.awt.event.AWTEventListener;
 import java.lang.reflect.InvocationTargetException;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRobotFactory;
 import org.eclipse.jubula.rc.common.listener.BaseAUTListener;
 import org.eclipse.jubula.rc.common.tester.adapter.factory.GUIAdapterFactoryRegistry;
 import org.eclipse.jubula.rc.swing.driver.RobotFactoryConfig;
 import org.eclipse.jubula.rc.swing.listener.CheckListener;
 import org.eclipse.jubula.rc.swing.listener.ComponentHandler;
 import org.eclipse.jubula.rc.swing.listener.FocusTracker;
 import org.eclipse.jubula.rc.swing.listener.MappingListener;
 import org.eclipse.jubula.rc.swing.listener.RecordListener;
 import org.eclipse.jubula.rc.swing.swing.tester.adapter.factory.SwingAdapterFactory;
 import org.eclipse.jubula.tools.constants.AUTServerExitConstants;
 import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 
 /**
  * The AutServer controling the AUT. <br>
  * A quasi singleton: the instance is created from main(). <br>
  * Expected arguments to main are, see also
  * StartAUTServerCommand.createCmdArray():
  * <ul>
  * <li>The name of host the client is running on, must be InetAddress
  * conform.</li>
  * <li>The port the JubulaClient is listening to.</li>
  * <li>The main class of the AUT.</li>
  * <li>Any further arguments are interpreted as arguments to the AUT.</li>
  * <ul>
  * When a connetion to the JubulaClient could made, any errors will send as a
  * message to the JubulaClient.
  * 
  * Changing the mode to OBJECT_MAPPING results in installing an AWTEventListener
  * (an instance of <code>MappingListener</code>). For simplification the
  * virtual machine is closed  without sending a message to the client when an
  * error occurs during the installation of the AWTEventListener. The exitcode is
  * the appopriate EXIT_* constant
  * 
  * Changing the mode to TESTING removes the installed MappingListener.
  * 
  * @author BREDEX GmbH
  * @created 26.07.2004
  */
 public class SwingAUTServer extends AUTServer {
     
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(SwingAUTServer.class);
     
     /**
      * name of Environment Variable or Java Property that defines the
      * regular expression to use when waiting for the Event Dispatch Thread
      * to start 
      */
     private static final String EDT_NAME_REGEX_KEY = "TEST_EDT_NAME_REGEX"; //$NON-NLS-1$
     
     /** 
      * private constructor
      * instantiates the listeners
      */
     public SwingAUTServer() {
         super(new MappingListener(), new RecordListener(), new CheckListener());
     }
 
     /**
      * Starts the AWT-EventQueue-Thread. <br>
      * <b>Important:</b> Must be called in complete AUT environment!
      * (Thread, ClassLoader, etc.)
      */
     protected void startToolkitThread() {
         // add a dummy listener to start the AWT-Thread
         Toolkit.getDefaultToolkit().addAWTEventListener(
                 new AWTEventListener() {
 
                     public void eventDispatched(AWTEvent event) {
                         // do nothing
                     }
                 }, 0L);
     }
 
     /**
      * {@inheritDoc}
      */
     protected void addToolkitEventListeners() {
         // install the component handler
         addToolkitEventListener(new ComponentHandler());
         // install the focus tracker
         addToolkitEventListener(new FocusTracker());
     }
 
     /**
      * {@inheritDoc}
      */
     protected void addToolkitEventListener(BaseAUTListener listener) {
         if (LOG.isInfoEnabled()) {
             LOG.info("installing AWTEventListener " //$NON-NLS-1$ 
                 + listener.toString());
         }
         try {
             long mask = 0;
             for (int i = 0; i < listener.getEventMask().length; i++) {
                 mask = mask | listener.getEventMask()[i];
             }
             Toolkit.getDefaultToolkit().addAWTEventListener(
                     (AWTEventListener)listener, mask);
         } catch (AWTError awte) {
             // no default toolkit
             LOG.error(awte.getLocalizedMessage(), awte);
         } catch (SecurityException se) {
             // no permission to add an AWTEventListener
             LOG.error(se.getLocalizedMessage(), se);
             System.exit(AUTServerExitConstants
                     .EXIT_SECURITY_VIOLATION_AWT_EVENT_LISTENER);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void removeToolkitEventListener(BaseAUTListener listener) {
         if (LOG.isInfoEnabled()) {
             LOG.info("removing AWTEventListener " //$NON-NLS-1$ 
                 + listener.toString());
         }
         try {
             Toolkit.getDefaultToolkit().removeAWTEventListener(
                     (AWTEventListener)listener);
         } catch (AWTError awte) {
             // no default toolkit
             LOG.error(awte.getLocalizedMessage(), awte);
         } catch (SecurityException se) {
             // no permission to remove an AWTEventListener,
             // should not occur, because addAWTEventListener() should be called 
             // first. But just in case, close the vm
             LOG.error(se.getLocalizedMessage(), se);
             System.exit(AUTServerExitConstants
                     .EXIT_SECURITY_VIOLATION_AWT_EVENT_LISTENER);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void startTasks() throws ExceptionInInitializerError, 
         InvocationTargetException, NoSuchMethodException {
 
         String edtNameRegEx = 
             EnvironmentUtils.getProcessEnvironment()
                 .getProperty(EDT_NAME_REGEX_KEY);
         if (edtNameRegEx == null) {
             edtNameRegEx = 
                 System.getProperty(EDT_NAME_REGEX_KEY);
         }
         if (edtNameRegEx != null) {
             // fail fast if the regex is malformed
             try {
                 Pattern.compile(edtNameRegEx);
             } catch (PatternSyntaxException pse) {
                 throw new InvocationTargetException(pse, 
                         "Invalid " + EDT_NAME_REGEX_KEY + " value."); //$NON-NLS-1$  //$NON-NLS-2$
             }
             final String accessibleEdtNameRegEx = edtNameRegEx;
             Thread addListenersThread = new Thread("Register initial Jubula Swing / AWT listeners") { //$NON-NLS-1$
                 public void run() {
 
                     boolean isThreadFound = false;
                     ThreadGroup rootThreadGroup = 
                         Thread.currentThread().getThreadGroup();
                     while (rootThreadGroup.getParent() != null) {
                         rootThreadGroup = rootThreadGroup.getParent();
                     }
                     while (!isThreadFound) {
                         Thread[] activeThreads = getActiveThreads();
                         for (int i = 0; i < activeThreads.length; i++) {
                             if (activeThreads[i].getName().matches(
                                     accessibleEdtNameRegEx)) {
                                 isThreadFound = true;
                                 break;
                             }
                         }
                         
                         try {
                             Thread.sleep(1000);
                         } catch (InterruptedException e) {
                             // do nothing. the next loop iteration will simply
                             // occur earlier than expected, which is not a 
                             // problem.
                         }
                     }
 
                     addToolKitEventListenerToAUT();
                 }
             };
             addListenersThread.setDaemon(true);
             addListenersThread.start();
         } else {
             startToolkitThread();
             addToolKitEventListenerToAUT();
         }
         
        // FIXME need better place to put the registration of the factory
         GUIAdapterFactoryRegistry.getInstance().
             registerFactory(new SwingAdapterFactory());
        
        AUTServer.getInstance().invokeAUT();
        

     }
 
     /**
      * {@inheritDoc}
      */
     public IRobot getRobot() {
         IRobotFactory robotFactory = new RobotFactoryConfig().getRobotFactory();
         return robotFactory.getRobot();
     }
     
     /**
      * Adapted from LGPLed code from an online Java article.
      * Modified to work with Java 1.4 and pass Jubula's checkstyle.
      * 
      * @return all currently active threads.
      * @see http://nadeausoftware.com/articles/2008/04/java_tip_how_list_and_find_threads_and_thread_groups
      */
     private static Thread[] getActiveThreads() {
         ThreadGroup rootThreadGroup = 
             Thread.currentThread().getThreadGroup();
         while (rootThreadGroup.getParent() != null) {
             rootThreadGroup = rootThreadGroup.getParent();
         }
         int nAlloc = rootThreadGroup.activeCount();
         int n = 0;
         Thread[] threads;
         do {
             nAlloc *= 2;
             threads = new Thread[ nAlloc ];
             n = rootThreadGroup.enumerate(threads);
         } while (n == nAlloc);
 
         Thread[] returnArray = new Thread[n];
         System.arraycopy(threads, 0, returnArray, 0, n);
         
         return returnArray;
     }
 }
