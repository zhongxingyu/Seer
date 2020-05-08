 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * 
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.preview;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.InvocationEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 import java.net.URLStreamHandlerFactory;
 import java.rmi.*;
 import java.rmi.registry.*;
 import java.rmi.server.UnicastRemoteObject;
 import java.security.Permission;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.UIManager;
 import org.openide.util.Exceptions;
 
 
 
 public class Preview {
     private PreviewSideDispatchingServer previewSideDispatcherServer = null;
     private boolean permissionToExitIsGranted = false;
        
     class PreviewSideDispatchingServer extends UnicastRemoteObject implements PreviewSideDispatchingServerFace {
         private HashMap<Integer, PreviewSideServer> previewSideServers = new HashMap<Integer, PreviewSideServer>();
         private Registry registry = null;
         private NBSideDispatchingServerFace nbServer = null;
         private String lf = null;
         
         PreviewSideDispatchingServer(Registry registry, NBSideDispatchingServerFace nbServer) throws RemoteException {
             super();
             this.registry = registry;
             this.nbServer = nbServer;
             new PingThread().start();
         }
         
         public void stopPreview(int hashCode) throws RemoteException {
             try {
                 PreviewSideServer engine = previewSideServers.get(hashCode);
                 engine.remove();
                 registry.unbind(PREVIEW_SIDE + " " + hashCode);                         //NOI18
                 UnicastRemoteObject.unexportObject(engine, true);
                 previewSideServers.remove(hashCode);    
             } catch (NotBoundException ex) {
                 Exceptions.printStackTrace(ex);
             } catch (AccessException ex) {
                 Exceptions.printStackTrace(ex);
             }
         }
         public void createPreview(int hashCode, String fileName, Point previewLocation, Dimension previewSize) throws RemoteException {
             try {
                 NBSideServerFace nbSideServerFace = (NBSideServerFace) registry.lookup(NB_SIDE + " " + hashCode);                           //NOI18
                 PreviewSideServer engine = new PreviewSideServer(nbSideServerFace, hashCode, fileName, lf, previewLocation, previewSize);
                 previewSideServers.put(hashCode, engine);
                 UnicastRemoteObject.unexportObject(engine, true);
                 PreviewSideServerFace stub = (PreviewSideServerFace) UnicastRemoteObject.exportObject(engine, 0);
                 registry.rebind(PREVIEW_SIDE + " " + hashCode, stub);                                                                       //NOI18
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
         
         public void processExitVM(ThreadGroup tg) {
             for (PreviewSideServer previewSideServer : previewSideServers.values()) {
                 if (previewSideServer.getThreadGroup() == tg) {
                     previewSideServer.cleanDesktop();
                     break;
                 }
             }
         }
         
         public void processTopWindow(Object _window) {
             Window window = (Window)_window;
             for (PreviewSideServer previewSideServer : previewSideServers.values()) {
                 if (Thread.currentThread().getThreadGroup() == previewSideServer.getThreadGroup()) {
                     previewSideServer.processTopWindow(window);
                     break;
                 }
             }
         }
         
         public void terminate() throws RemoteException {
             for (PreviewSideServer previewSideServer : previewSideServers.values()) {
                 previewSideServer.setPreviewPlacement();
             }
             nbServer.notifyPreviewSideTerminated();
             new Thread() {
                 @Override
                 public void run() {
                     try {
                         Thread.sleep(500);
                     } catch (InterruptedException ex) {
                         ex.printStackTrace();
                     }
                     permissionToExitIsGranted = true;
                     System.exit(0);
                 }
             }.start();
         }
         
         public void setLookAndFeel(String lf) throws RemoteException {
             try {
                 this.lf = lf;
                 UIManager.setLookAndFeel(lf);
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
 
         public void ping() throws RemoteException {
         }
         
         class PingThread extends Thread {
             @Override
             public void run() {
                 while (!isInterrupted()) {
                     try {
                         if (nbServer != null)
                             nbServer.ping();
                         try {
                             Thread.sleep(500);
                         } catch (InterruptedException ex) {
                             interrupt();
                         }
                     } catch (Throwable ex) {
                         if (!isInterrupted()) {
                             permissionToExitIsGranted = true;
                             System.exit(0);
                         }
                     }
                 }
             }
         }
     }
 
     class PreviewSideServer extends UnicastRemoteObject implements PreviewSideServerFace, WindowListener {
         private NBSideServerFace nbServer = null;
         private JComponent scrollComponent = null;
         private String fileName = null;
         private String lf = null;
         private ExceptionAwareThreadGroup threadGroup = null;
         private ACThread acTread = null;
         private CheckThread checkTread = null;
         private PreviewSideServer thiss = this;
         private ClassLoader bootClassLoader = null;
         private JFrame previewFrame = null;
         private Point previewLocation = null;
         private Dimension previewSize = null;
         private JComponent comp = null;
         private AutoResizableDesktopPane desktopPane = null;
 
         PreviewSideServer(NBSideServerFace nbServer, int hashCode, String fileName, String lf, Point previewLocation, Dimension previewSize) throws RemoteException {
             super();
             this.nbServer = nbServer;
             this.fileName = fileName;
             this.lf = lf;
             this.previewLocation = previewLocation;
             this.previewSize = previewSize;
         }
         
         public Object getAC() {
             return acTread.getAC();
         }
 
         public ThreadGroup getThreadGroup() {
             return acTread.getTG();
         }
         
         public EventQueue getEventQueue() {
             return acTread.getEQ();
         }
         
         public void cleanDesktop() {
             acTread.executeOnEDT(new Runnable() {
                 public void run() {
                     desktopPane.removeAll();
                     desktopPane.repaint();
                 }
             });
         }
 
         public void setNBServer(NBSideServerFace nbServer) {
             this.nbServer = nbServer;
         }
 
         public void remove() {
             checkTread.interrupt();
             threadGroup.skipExceptions(true);
             if (previewFrame != null) previewFrame.dispose();
             new Thread(new Runnable() {
                 public void run() {
                     acTread.disposeAC();
                 }
              }).start();
         }
 
         private boolean noWindows = true;
         private volatile boolean  blockProcessing = false;
         
         public void processTopWindow(final Window window) {
             if (blockProcessing) return;
             window.setVisible(false);
             noWindows = false;
             Container contentPane = null;
             boolean properWindow = false;
             if (window instanceof JFrame) {
                 properWindow = true;
                 if (((JFrame)window).getRootPane() != null)
                     contentPane = ((JFrame)window).getContentPane();
             } else {
                 if (window instanceof JDialog) {
                     properWindow = true;
                     if (((JDialog)window).getRootPane() != null)
                         contentPane = ((JDialog)window).getContentPane();
                 }
             }
             if (contentPane == null) {
                 if (properWindow) window.addWindowListener(this);
             } else {
                 acTread.executeOnEDT(new Runnable() {
                     public void run() {
                         checkSize(CodeUtils.moveToInner(desktopPane, window));
                     }
                 });
             }
         }
 
         public void processObject(Object object) {
             JComponent component = CodeUtils.parseComponent(object);
             if (component != null) {
                 if (!noWindows) {
                     CodeUtils.moveToInner(desktopPane, component);
                 } else {
                     comp.remove(desktopPane);
                     comp.add(component);
                 }
             }
         }
         
         public void preparePreviewFrame() {
             desktopPane = new AutoResizableDesktopPane();
             desktopPane.setBackground(Color.WHITE);
                 
             comp = new JPanel();
             comp.setLayout(new BorderLayout());
             comp.add(desktopPane);
 
             scrollComponent = new JScrollPane();
             blockProcessing = true;
             previewFrame = new JFrame(DESIGN_PREVIEW + " [" + fileName + "]"); // NOI18
             blockProcessing = false;
             previewFrame.setSize(previewSize);
             previewFrame.setLocation(previewLocation);
             previewFrame.setLayout(new BorderLayout());
             previewFrame.add(scrollComponent);
             previewFrame.addWindowListener(thiss);
             ((JScrollPane)scrollComponent).setViewportView(comp);
             previewFrame.setVisible(true);
         }
         
         class ACThread extends Thread {
             private String lf = null;
             private Object ac = null;
             EventQueue eq = null;
             ThreadGroup tg = null;
             public ACThread(ThreadGroup tg, String lf) {
                 super(tg, "SACT");                          //NOI18
                 this.tg = tg;
                 this.lf = lf;
             }
             
             public Object getAC() {
                 return ac;
             }
             
             public EventQueue getEQ() {
                 return eq;
             }
             
             public ThreadGroup getTG() {
                 return tg;
             }
             
             public void disposeAC() {
                 if (ac != null) {
                     try {
                         Class<?> acc = this.getClass().getClassLoader().loadClass(APC);
                         acc.getDeclaredMethod(DSP).invoke(ac);
                     } catch (Throwable er) {
                         er.printStackTrace();
                     }
                 }
             }
 
             void executeOnEDT(Runnable r ){
                 eq.postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(), r));
             }
             
             @Override
             public void run() {
                 try {
                     Class acc = this.getClass().getClassLoader().loadClass(SAST);   // NOI18N
                     ac = acc.getDeclaredMethod(CNAC).invoke(null);                  // NOI18N
                     eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
                 } catch (Exception ex) {
                     Exceptions.printStackTrace(ex);
                 }
                 try {
                     UIManager.setLookAndFeel(lf);
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
         }
         
         void body(final Object _context, final ClassLoader cl) {
             try {
                 UIManager.setLookAndFeel(lf);
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             noWindows = true;
             desktopPane.removeAll();
             comp.removeAll();
             comp.add(desktopPane);
             desktopPane.repaint();
             Object obj = CodeUtils.run(_context, cl);
             if (obj != null) thiss.processObject(obj);
         }
         
         void checkSize(Dimension d) {
             Container content = previewFrame.getContentPane();
             int borderWidth = previewFrame.getWidth() - content.getWidth();
             int borderHeight = previewFrame.getHeight() - content.getHeight();
             int width = content.getWidth();
             int height = content.getHeight();
             boolean set = false;
             if (d.width > width) {
                 width = d.width + 2;
                 set = true;
             }
             if (d.height > height) {
                 height = d.height + 2;
                 set = true;
             }
            if (set) previewFrame.setSize(width + borderWidth, height + borderHeight);
         }
 
         private class ExceptionAwareThreadGroup extends ThreadGroup {
             private boolean skip = false;
             public ExceptionAwareThreadGroup(String s) {
                 super(s);
             }
             public void skipExceptions(boolean skip) {
                 this.skip = skip;
             }
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 if (!skip) super.uncaughtException(t, e);
             }
         }
 
         class CheckThread extends Thread {
             @Override
             public void run() {
                 while (!isInterrupted()) {
                     check = false;
                     acTread.executeOnEDT(new Runnable() {
                         public void run() {
                             check = true;
                         }
                     });
                     try {
                         Thread.sleep(20000);
                     } catch (InterruptedException ex) {
                         interrupt();
                     }
                     if (!check && !isInterrupted()) {
                         System.out.println(EDT_HANGS);
                         closeRequest();
                         interrupt();
                     }
                 }
             }
         }
         
         private volatile boolean check = false;
         public void  run(final Object context)  throws RemoteException {
             if (threadGroup == null) {
                 threadGroup = new ExceptionAwareThreadGroup("SACG" + instanceCounter++);      //NOI18
                 acTread = new ACThread(threadGroup, lf);
                 acTread.start();
                 try {
                     acTread.join();
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
                 acTread.executeOnEDT(new Runnable() {
                     public void run() {
                         preparePreviewFrame();
                     }
                 });
                 acTread.executeOnEDT(new Runnable() {
                     public void run() {
                         bootClassLoader = CodeUtils.getBootClassloader(context, this.getClass().getClassLoader().getParent());
                         body(context, bootClassLoader);
                     }
                 });
                 checkTread = new CheckThread();
                 checkTread.start();
             } else {
                 acTread.executeOnEDT(new Runnable() {
                     public void run() {
                         body(context, bootClassLoader);
                     }
                 });
             }
         };
         
         public void setPreviewPlacement() {
             try {
                 if (previewFrame != null) nbServer.setPreviewPlacement(previewFrame.getLocation(), previewFrame.getSize());
             } catch (RemoteException ex) {
                 Exceptions.printStackTrace(ex);
             }
         }
 
         public void windowOpened(WindowEvent e) {
             final Window window = (Window) e.getSource();
             if (window == previewFrame) return;
             window.removeWindowListener(this);
             acTread.executeOnEDT(new Runnable() {
                 public void run() {
                     checkSize(CodeUtils.moveToInner(desktopPane, window));
                 }
             });
         }
         
         public void windowClosing(WindowEvent e) {
             checkTread.interrupt();
             closeRequest();
         }
         
         boolean closed = false;
         synchronized private void closeRequest() {
             if (!closed) {
                 closed = true;
                 try {
                     nbServer.setPreviewPlacement(previewFrame.getLocation(), previewFrame.getSize());
                     nbServer.previewWindowClosed();
                 } catch (RemoteException ex) {
                     Exceptions.printStackTrace(ex);
                 }
             }
         }
         
         public void moveToFront() throws RemoteException {
             previewFrame.setAlwaysOnTop(true);
             previewFrame.setAlwaysOnTop(false);
         }
 
         public void notifyClassPathChanged() throws RemoteException {
             threadGroup = null;
         }
 
         public void windowClosed(WindowEvent e) {
         }
         public void windowIconified(WindowEvent e) {
         }
         public void windowDeiconified(WindowEvent e) {
         }
         public void windowActivated(WindowEvent e) {
         }
         public void windowDeactivated(WindowEvent e) {
         }
 
         public SerializableImage getPicture() throws RemoteException {
             final SerializableImage offscreenBuffer = new SerializableImage((BufferedImage) desktopPane.createImage(desktopPane.getWidth(), desktopPane.getHeight()));
             Thread thread = new Thread(acTread.getTG(), new Runnable(){
                 public void run() {
                     desktopPane.print(offscreenBuffer.getGraphics());
                 }
             });
             thread.start();
             try {
                 thread.join();
             } catch (InterruptedException ex) {
                 ex.printStackTrace();
             }
             return offscreenBuffer;
         }
     }
 
     public void start(int instance) {
         System.setProperty("apple.awt.UIElement", "true");
         NB_SIDE = instance + " " +  NB_SIDE;
         PREVIEW_SIDE = instance +  " " + PREVIEW_SIDE;
         try {
             URL.setURLStreamHandlerFactory( (URLStreamHandlerFactory) new MFOURLStreamHanfler.Factory());
             System.setSecurityManager(new RMISecurityManager() {
 
                 @Override
                 public void checkPermission(Permission perm) {
                     if (perm.getName().contains("exitVM")) {
                         if (!permissionToExitIsGranted) {
                             previewSideDispatcherServer.processExitVM(Thread.currentThread().getThreadGroup());
                             throw new SecurityException("Attempt to exit from Preview JVM");
                         }
                     }
                 }
                 
                 @Override
                 public void checkPermission(Permission perm, Object context) {
                 }
                 @Override
                 public boolean checkTopLevelWindow(Object window) {
                     if (previewSideDispatcherServer != null)
                         previewSideDispatcherServer.processTopWindow(window);
                     return super.checkTopLevelWindow(window);
                 }
             });
             Registry registry = LocateRegistry.getRegistry(1099);
             NBSideDispatchingServerFace nbServer = (NBSideDispatchingServerFace) registry.lookup(NB_SIDE);
             previewSideDispatcherServer = new PreviewSideDispatchingServer(registry, nbServer);
             UnicastRemoteObject.unexportObject(previewSideDispatcherServer, true);
             PreviewSideDispatchingServerFace stub = (PreviewSideDispatchingServerFace) UnicastRemoteObject.exportObject(previewSideDispatcherServer, 0);
             registry.rebind(PREVIEW_SIDE, stub);
             
             nbServer.notifyPreviewSideStarted();
             
             while (true) {
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Preview.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             
         } catch (Throwable ex) {
             ex.printStackTrace();
         }
     }
 
     private static int instanceCounter = 0;
     
     private static String NB_SIDE = "NBSide";                               // NOI18N
     private static String PREVIEW_SIDE = "PreviewSide";                     // NOI18N
     private static String APC = "sun.awt.AppContext";                       // NOI18N
     private static String DSP = "dispose";                                  // NOI18N
     private static String CNAC = "createNewAppContext";                     // NOI18N
     private static String SAST = "sun.awt.SunToolkit";                      // NOI18N
     private static String DESIGN_PREVIEW = "Design Preview";                // NOI18N
     private static String EDT_HANGS = "Preview cancelled due to problems in previewed code.";                      // NOI18N
 }
