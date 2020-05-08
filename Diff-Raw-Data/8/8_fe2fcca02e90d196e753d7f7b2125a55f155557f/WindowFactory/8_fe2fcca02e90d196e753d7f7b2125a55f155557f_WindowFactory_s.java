 package org.iplantc.de.client.factories;
 
 import org.iplantc.core.uicommons.client.models.WindowConfig;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.analysis.views.MyAnalysesWindow3;
 import org.iplantc.de.client.apps.views.DEAppsWindow;
 import org.iplantc.de.client.models.IDropLiteWindowConfig;
 import org.iplantc.de.client.models.NotificationWindowConfig;
 import org.iplantc.de.client.models.SimpleDownloadWindowConfig;
 import org.iplantc.de.client.models.TitoWindowConfig;
 import org.iplantc.de.client.notifications.views.NotificationWindow3;
 import org.iplantc.de.client.util.WindowUtil;
 import org.iplantc.de.client.views.windows.AboutApplicationWindow;
 import org.iplantc.de.client.views.windows.IDropLiteAppletWindow;
 import org.iplantc.de.client.views.windows.IPlantWindowInterface;
 import org.iplantc.de.client.views.windows.MyDataWindow;
 import org.iplantc.de.client.views.windows.SimpleDownloadWindow;
 import org.iplantc.de.client.views.windows.TitoWindow;
 import org.iplantc.de.client.views.windows.WizardWindow;
 
 /**
  * Defines a factory for the creation of windows.
  * 
  * @see org.iplantc.de.client.views.windows.IPlantWindow
  */
 public class WindowFactory {
     /**
      * Builds a window from a tag.
      * 
      * @param type unique type for window used to determine type of window to build.
      * @param config a WindowConfiguration suitable for the type of window to create
      * @return new window.
      */
     public static IPlantWindowInterface build(String type, WindowConfig config) {
         IPlantWindowInterface ret = null;
 
         if (type != null) {
             if (type.equals(Constants.CLIENT.myDataTag())) {
                 ret = new MyDataWindow(type, config);
             } else if (type.equals(Constants.CLIENT.myNotifyTag())) {
                 ret = new NotificationWindow3(type, (NotificationWindowConfig)config);
             } else if (type.equals(Constants.CLIENT.myHelpTag())) {
                 // since the help page is now a wiki page, open it in a new window so that the user may
                 // login to the Wiki.
                 WindowUtil.open(Constants.CLIENT.deHelpFile());
             } else if (type.equals(Constants.CLIENT.myAboutTag())) {
                 ret = new AboutApplicationWindow(type);
             } else if (type.equals(Constants.CLIENT.myAnalysisTag())) {
                 ret = new MyAnalysesWindow3(type, config);
             } else if (type.equals(Constants.CLIENT.deCatalog())) {
                 ret = new DEAppsWindow(type, config);
                 // } else if (type.equals(Constants.CLIENT.pipelineEditorTag())) {
                 // ret = new PipelineEditorWindow(type);
                // } else if (type.startsWith(Constants.CLIENT.iDropLiteTag())) {
                 ret = new IDropLiteAppletWindow(type, (IDropLiteWindowConfig)config);
             } else if (type.equals(Constants.CLIENT.titoTag())) {
                 ret = new TitoWindow(type, (TitoWindowConfig)config);
             } else if (type.equals(Constants.CLIENT.simpleDownloadTag())) {
                 ret = new SimpleDownloadWindow(type, (SimpleDownloadWindowConfig)config);
             } else {
                 ret = new WizardWindow(type, config);
             }
         }
 
         return ret;
     }
 }
