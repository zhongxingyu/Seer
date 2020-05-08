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
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
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
      * The final that contains the SVN properties. These properties are
      * generated during compilation, by the build.xml ant file.
      */
     private final static String svnPropertiesFilename = "/conf/svn.properties";
 
     /**
      * The properties, created at build time, by the build.xml ant file. Or a
      * dummy property set, with 'unknown' values.
      */
     private static Properties svnProperties = getSvnProperties();
 
     /**
      * The version of this applet. The version itseld is to be updated in the
      * JUploadApplet.java file. The revision is added at build time, by the
      * build.xml ant file, packaged with the applet.
      */
    public final static String VERSION = "3.3.0 [SVN-Rev: "
             + svnProperties.getProperty("revision") + "]";
 
     /**
      * The last modification of this applet. Not accurate: would work only if
      * the whole src folder is commited. Remplaced by the build date.
      * 
      * @deprecated since v3.1
      */
     public final static String LAST_MODIFIED = svnProperties
             .getProperty("lastSrcDirModificationDate");
 
     /**
      * Date of the build for the applet. It's generated at build time by the
      * build.xml packaged by the script. If compiled with eclipse (for
      * instance), the build_date is noted as 'unknown'.
      */
     public final static String BUILD_DATE = svnProperties
             .getProperty("buildDate");
 
     /**
      * The current upload policy. This class is responsible for the call to the
      * UploadPolicyFactory.
      */
     private UploadPolicy uploadPolicy = null;
 
     /**
      * The JUploadPanel, which actually contains all the applet components.
      */
     private JUploadPanel jUploadPanel = null;
 
     /**
      * The log messages should go there ...
      */
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
             // TODO Translate this sentence
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
      *
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
             uploadPolicy.setProperty(prop, value);
         } catch (Exception e) {
             uploadPolicy.displayErr(e);
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
 
     // /////////////////////////////////////////////////////////////////////////
     // ////////////////////// Helper functions
     // /////////////////////////////////////////////////////////////////////////
 
     /**
      * Helper function for ant build to retrieve the current version.
      */
     public static void main(String[] args) {
         System.out.println(VERSION.split(" ")[0]);
     }
 
     /**
      * Helper function, to get the Revision number, if available. The applet
      * must be built fro the build.xml ant file.
      */
     public static Properties getSvnProperties() {
         Properties properties = new Properties();
         Boolean bPropertiesLoaded = false;
 
         // Let's try to load the properties file.
         // The upload policy is not created yet: we can not use its disply
         // methods to trace what is happening here.
         try {
             properties.load(Class.forName("wjhk.jupload2.JUploadApplet")
                     .getResourceAsStream(svnPropertiesFilename));
             bPropertiesLoaded = true;
         } catch (Exception e) {
             // An error occured when reading the file. The applet was
             // probably not built with the build.xml ant file.
             // We'll create a fake property list. See below.
 
             // We can not output to the uploadPolicy display method, as the
             // upload policy is not created yet. We output to the system output.
             // Consequence: if this doesn't work during build, you'll see an
             // error during the build: the generated file name will contain the
             // following error message.
             System.out.println(e.getClass().getName()
                     + " in JUploadApplet.getSvnProperties() (" + e.getMessage()
                     + ")");
         }
 
         /*
          * } catch (NullPointerException e) { } catch (IOException e) { // An
          * error occured when reading the file. The applet was // probably not
          * built with the build.xml ant file. // We'll create a fake propery
          * list. See below. }
          */
         if (!bPropertiesLoaded) {
             properties.setProperty("buildDate",
                     "Unknown build date (please use the build.xml ant script)");
             properties
                     .setProperty("lastSrcDirModificationDate",
                             "Unknown last modification date (please use the build.xml ant script)");
             properties.setProperty("revision",
                     "Unknown revision (please use the build.xml ant script)");
         }
         return properties;
     }
 }
