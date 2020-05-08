 package org.iplantc.de.client.factories;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.util.WindowUtil;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.views.windows.AboutApplicationWindow;
 import org.iplantc.de.client.views.windows.AppIntegrationWindow;
 import org.iplantc.de.client.views.windows.AppWizardWindow;
 import org.iplantc.de.client.views.windows.DEAppsWindow;
 import org.iplantc.de.client.views.windows.DeDiskResourceWindow;
 import org.iplantc.de.client.views.windows.FileViewerWindow;
 import org.iplantc.de.client.views.windows.IDropLiteAppletWindow;
 import org.iplantc.de.client.views.windows.IPlantWindowInterface;
 import org.iplantc.de.client.views.windows.MyAnalysesWindow;
 import org.iplantc.de.client.views.windows.NotificationWindow;
 import org.iplantc.de.client.views.windows.PipelineEditorWindow;
 import org.iplantc.de.client.views.windows.SimpleDownloadWindow;
 import org.iplantc.de.client.views.windows.SystemMessagesWindow;
 import org.iplantc.de.client.views.windows.configs.AboutWindowConfig;
 import org.iplantc.de.client.views.windows.configs.AnalysisWindowConfig;
 import org.iplantc.de.client.views.windows.configs.AppWizardConfig;
 import org.iplantc.de.client.views.windows.configs.AppsIntegrationWindowConfig;
 import org.iplantc.de.client.views.windows.configs.AppsWindowConfig;
 import org.iplantc.de.client.views.windows.configs.DiskResourceWindowConfig;
 import org.iplantc.de.client.views.windows.configs.FileViewerWindowConfig;
 import org.iplantc.de.client.views.windows.configs.IDropLiteWindowConfig;
 import org.iplantc.de.client.views.windows.configs.NotifyWindowConfig;
 import org.iplantc.de.client.views.windows.configs.SimpleDownloadWindowConfig;
 import org.iplantc.de.client.views.windows.configs.SystemMessagesWindowConfig;
 import org.iplantc.de.client.views.windows.configs.WindowConfig;
 
 import com.google.common.base.Strings;
 
 /**
  * Defines a factory for the creation of windows.
  * 
  */
 public class WindowFactory {
     
     /**
      * Constructs a DE window based on the given {@link WindowConfig}
      * The "tag" for the window must be constructed here.
      * 
      * @param config
      * @return
      */
     public static <C extends WindowConfig> IPlantWindowInterface  build(C config){
         final EventBus eventBus = EventBus.getInstance();
         IPlantWindowInterface ret = null;
         switch (config.getWindowType()) {
             case ABOUT:
                 ret = new AboutApplicationWindow((AboutWindowConfig)config);
                 break;
             case ANALYSES:
                 ret = new MyAnalysesWindow((AnalysisWindowConfig)config, eventBus);
                 break;
             case APP_INTEGRATION:
                 ret = new AppIntegrationWindow((AppsIntegrationWindowConfig)config, eventBus);
                 break;
             case APP_WIZARD:
                 ret = new AppWizardWindow((AppWizardConfig)config);
                 break;
             case APPS:
                 ret = new DEAppsWindow((AppsWindowConfig)config);
                 break;
             case DATA:
                 ret = new DeDiskResourceWindow((DiskResourceWindowConfig)config);
                 break;
             case DATA_VIEWER:
                 ret = new FileViewerWindow((FileViewerWindowConfig)config);
                 break;
             case HELP:
                 WindowUtil.open(Constants.CLIENT.deHelpFile());
                 break;
            case IDROP_LITE:
                 ret = new IDropLiteAppletWindow((IDropLiteWindowConfig)config);
                 break;
             case NOTIFICATIONS:
                 ret = new NotificationWindow((NotifyWindowConfig)config);
                 break;
             case SIMPLE_DOWNLOAD:
                 ret = new SimpleDownloadWindow((SimpleDownloadWindowConfig)config);
                 break;
             case WORKFLOW_INTEGRATION:
                 ret = new PipelineEditorWindow(config);
                 break;
             case SYSTEM_MESSAGES:
             	ret = new SystemMessagesWindow((SystemMessagesWindowConfig)config);
             default:
                 break;
         }
         return ret;
     }
 
     public static <C extends org.iplantc.de.client.views.windows.configs.WindowConfig> String constructWindowId(C config) {
         String windowType = config.getWindowType().toString();
         String tag = config.getTag();
         return (!Strings.isNullOrEmpty(tag)) ? windowType + "_" + tag : windowType;
     }
 }
