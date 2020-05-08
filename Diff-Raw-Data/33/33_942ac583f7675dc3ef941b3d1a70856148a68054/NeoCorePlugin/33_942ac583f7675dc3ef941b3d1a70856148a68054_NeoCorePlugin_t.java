 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.core;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.amanzi.neo.core.preferences.PreferencesInitializer;
 import org.amanzi.neo.services.AweProjectService;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.ui.NeoServicesUiPlugin;
 import org.amanzi.neo.services.ui.UpdateViewManager;
 import org.apache.log4j.PropertyConfigurator;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * Plugin class for org.amanzi.neo.core
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 
 public class NeoCorePlugin extends AbstractUIPlugin{
 
     /*
      * Plugin's ID
      */
 
     private static final String ID = "org.amanzi.neo.core";
 
     /*
      * Plugin variable
      */
 
     static private NeoCorePlugin plugin;
 
     /*
      * Initializer for AWE-specific Neo Preferences
      */
 
     private PreferencesInitializer initializer = new PreferencesInitializer();
 
     private AweProjectService aweProjectService;
 
 
 
     /**
      * Constructor for SplashPlugin.
      */
     public NeoCorePlugin() {
         super();
         plugin = this;
     }
 
     @Override
     public void start(BundleContext context) throws Exception {
         super.start(context);
         plugin = this;
 
         //TODO need solution to use log4j libraries from separate plugin but not from udig libraries
         URL url = getBundle().getEntry("/logCinfig.properties");
         if (url != null) {
             URL rUrl = FileLocator.toFileURL(url);
         
             PropertyConfigurator.configure(rUrl);
         }
     }
 
     /*
      * (non-Javadoc)
      * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
      */
     @Override
     public void stop(BundleContext context) throws Exception {
         // savePluginPreferences();
         plugin = null;
         super.stop(context);
     }
 
     /**
      * Returns the shared instance.
      */
     public static NeoCorePlugin getDefault() {
         return plugin;
     }
 
     /**
      * Returns initializer of NeoPreferences
      * 
      * @return initializer of Neo Preferences
      */
 
     public PreferencesInitializer getInitializer() {
         return initializer;
     }
 
     /**
      * @return awe project service
      */
     public AweProjectService getProjectService() {
        return NeoServiceFactory.getInstance().getProjectService();
     }
 
 
 
 
     /**
      * @return UpdateBDManager
      */
     public UpdateViewManager getUpdateViewManager() {
         return NeoServicesUiPlugin.getDefault().getUpdateViewManager();
     }
 
     /**
      * Sets initializer of NeoPreferences
      * 
      * @param initializer new initializer for NeoPreferences
      */
 
     public void setInitializer(PreferencesInitializer initializer) {
         this.initializer = initializer;
     }
 
     /**
      * Print a message and information about exception to Log
      * 
      * @param message message
      * @param e exception
      */
 
     public static void error(String message, Throwable e) {
         getDefault().getLog().log(new Status(IStatus.ERROR, ID, 0, message == null ? "" : message, e)); //$NON-NLS-1$
     }
 
     public String getNeoPluginLocation() {
         try {
             return FileLocator.resolve(Platform.getBundle("org.neo4j").getEntry(".")).getFile();
         } catch (IOException e) {
             error(null, e);
             return null;
         }
     }
 
 


 }
