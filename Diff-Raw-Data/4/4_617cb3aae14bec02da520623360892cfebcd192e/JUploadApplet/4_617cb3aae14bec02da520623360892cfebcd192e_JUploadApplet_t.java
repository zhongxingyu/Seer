 //
 // $Id$
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: ?
 // Creator: William JinHua Kwong
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
 import wjhk.jupload2.gui.FilePanel;
 import wjhk.jupload2.gui.JUploadPanel;
 import wjhk.jupload2.gui.JUploadTextArea;
 import wjhk.jupload2.policies.UploadPolicy;
 import wjhk.jupload2.policies.UploadPolicyFactory;
 
 /**
  * The applet. It contains quite only the call to creation of the
  * {@link wjhk.jupload2.gui.JUploadPanel}, which contains the real code. <BR>
  * <BR>
  * The behaviour of the applet can easily be adapted, by : <DIR>
  * <LI> Using an existing {@link wjhk.jupload2.policies.UploadPolicy}, and
  * specifying parameters.
  * <LI> Creating a new upload policy, based on the
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}, or created from scratch.
  * </DIR>
  * 
  * @author William JinHua Kwong (updated by Etienne Gauthier)
  * @version $Revision$
  */
 public class JUploadApplet extends Applet {
 
     /**
      * 
      */
     private static final long serialVersionUID = -3207851532114846776L;
 
     /**
      * The version of this applet.
      */
     public final static String VERSION = "3.0.0a3 ($Revision$)";
 
     /**
      * The last modification of this applet.
      */
     public final static String LAST_MODIFIED = "$Date$";
 
     private UploadPolicy uploadPolicy = null;
 
     private JUploadPanel jUploadPanel = null;
 
     private JUploadTextArea logWindow = null;
 
     private class Callback {
         private String m;
 
         private Object o;
 
         Callback(Object o, String m) {
             this.o = o;
             this.m = m;
         }
 
         void invoke() throws IllegalArgumentException, IllegalAccessException,
                 InvocationTargetException, SecurityException {
             Object args[] = {};
             Method methods[] = this.o.getClass().getMethods();
             for (int i = 0; i < methods.length; i++) {
                 if (methods[i].getName().equals(this.m)) {
                     methods[i].invoke(this.o, args);
                 }
             }
         }
     }
 
     private Vector<Callback> unloadCallbacks = new Vector<Callback>();
 
     /**
      * @see java.applet.Applet#init()
      */
     @Override
     public void init() {
 
         try {
             this.setLayout(new BorderLayout());
 
             // Creation of the Panel, containing all GUI objects for upload.
             this.logWindow = new JUploadTextArea(20, 20);
             this.uploadPolicy = UploadPolicyFactory.getUploadPolicy(this);
 
             this.jUploadPanel = new JUploadPanel(this, this.logWindow,
                     this.uploadPolicy);
 
             this.add(this.jUploadPanel, BorderLayout.CENTER);
         } catch (final Exception e) {
             System.out.println(e.getMessage());
             System.out.println(e.getStackTrace());
             JOptionPane
                     .showMessageDialog(
                             null,
                             "Error during applet initialization!\nHave a look in your Java console.",
                             "Error", JOptionPane.ERROR_MESSAGE);
         }
 
     }
 
     /**
      * Retrieves the FilePanel of this applet.
      * 
      * @return the current FilePanel of this instance.
      */
     public FilePanel getFilePanel() {
         return this.jUploadPanel.getFilePanel();
     }
 
     /**
      * Retrieves the current log window of this applet. This log window may
      * visible or not depending on various applet parameter.
      * 
      * @return the current log window of this instance.
      * @see JUploadPanel#showOrHideLogWindow()
      */
     public JUploadTextArea getLogWindow() {
         return this.logWindow;
     }
 
     /**
      * Retrieves the current upload panel.
      * 
      * @return the current upload panel of this instance.
      */
     public JUploadPanel getUploadPanel() {
         return this.jUploadPanel;
     }
 
     /**
      * Retrieves the current upload policy.
      * 
      * @return the current upload policy of this instance.
      */
     public UploadPolicy getUploadPolicy() {
         return this.uploadPolicy;
     }
 
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////:
     // //////////////// FUNCTIONS INTENDED TO BE CALLED BY JAVASCRIPT FUNCTIONS
     // ////////////////////////////:
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////:
 
     /**
      * This allow runtime modifications of properties. Currently, this is only
      * user after full initialization. This methods only calls the
      * UploadPolicy.setProperty method.
      * 
      * @param prop
      * @param value
      */
     public void setProperty(String prop, String value) {
         try {
             this.jUploadPanel.getUploadPolicy().setProperty(prop, value);
         } catch (Exception e) {
             this.jUploadPanel.getUploadPolicy().displayErr(e);
         }
     }
 
     /** @see UploadPolicy#displayErr(Exception) */
     public void displayErr(String err) {
         this.uploadPolicy.displayErr(err);
     }
 
     /** @see UploadPolicy#displayInfo(String) */
     public void displayInfo(String info) {
         this.uploadPolicy.displayInfo(info);
     }
 
     /** @see UploadPolicy#displayWarn(String) */
     public void displayWarn(String warn) {
         this.uploadPolicy.displayWarn(warn);
     }
 
     /** @see UploadPolicy#displayDebug(String, int) */
     public void displayDebug(String debug, int minDebugLevel) {
         this.uploadPolicy.displayDebug(debug, minDebugLevel);
     }
 
     /**
      * Register a callback to be executed during applet termination.
      * 
      * @param o The Object instance to be registered
     * @param method The Method of that object to be registered. The method must
     *            be of type void and must not take any parameters and must be
      *            public.
      */
     public void registerUnload(Object o, String method) {
         this.unloadCallbacks.add(new Callback(o, method));
     }
 
     private void runUnload() {
         Iterator<Callback> i = this.unloadCallbacks.iterator();
         while (i.hasNext()) {
             try {
                 i.next().invoke();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         this.unloadCallbacks.clear();
     }
 
     /**
      * @see java.applet.Applet#stop()
      */
     @Override
     public void stop() {
         runUnload();
     }
 
     /**
      * @see java.applet.Applet#destroy()
      */
     @Override
     public void destroy() {
         runUnload();
     }
 
     /**
      * Helper function for ant build to retrieve the current version.
      */
     public static void main(String[] args) {
         System.out.println(VERSION.split(" ")[0]);
     }
 }
