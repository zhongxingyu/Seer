 package org.iplantc.de.client.views.windows;
 
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.dispatchers.WindowDispatcher;
 import org.iplantc.de.client.factories.EventJSONFactory.ActionType;
 import org.iplantc.de.client.factories.WindowConfigFactory;
 import org.iplantc.de.client.models.NotificationWindowConfig;
 import org.iplantc.de.client.models.WindowConfig;
 import org.iplantc.de.client.utils.NotificationHelper;
 import org.iplantc.de.client.utils.NotificationHelper.Category;
 import org.iplantc.de.client.views.panels.NotificationPanel;
 
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 
 /**
  * Window for displaying event notifications.
  * 
  * @author lenards, sriram, hariolf
  * 
  */
 public class NotificationWindow extends IPlantWindow {
     private NotificationPanel panel;
 
     /**
      * Constructs an instance given a unique identifier.
      * 
      * @param tag a unique identifier that serves as a "window handle."
      * @param config configuration parameters
      */
     public NotificationWindow(String tag, NotificationWindowConfig config) {
         super(tag, false, true, true, true);
 
         setWindowConfig(config);
         init();
 
     }
 
     private void init() {
         setId(tag);
         setHeading(I18N.DISPLAY.myNotifications());
         setResizable(true);
         setWidth(740);
         setHeight(400);
         setLayout(new FitLayout());
 
         if (config != null) {
             Category category = ((NotificationWindowConfig)config).getCategory();
             List<String> selectedIds = JsonUtil.buildStringList(((NotificationWindowConfig)config)
                     .getSelectedIds());
             int page = ((NotificationWindowConfig)config).getCurrentPage();
             SortDir dir = ((NotificationWindowConfig)config).getSortDir();
 
             panel = new NotificationPanel(page, selectedIds, category, dir);
         } else {
             panel = new NotificationPanel();
         }
 
         add(panel);
     }
 
     @Override
     public void setWindowConfig(WindowConfig config) {
         if (config instanceof NotificationWindowConfig) {
             this.config = config;
         }
 
     }
 
     @Override
     public void show() {
         super.show();
         applyWindowConfig();
     }
 
     private void applyWindowConfig() {
         if (config != null) {
             setWindowViewState();
             config = null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void cleanup() {
         super.cleanup();
         NotificationHelper.getInstance().cleanup();
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void afterRender() {
         super.afterRender();
     }
 
     @Override
     public JSONObject getWindowState() {
         NotificationWindowConfig configData = new NotificationWindowConfig(config);
         storeWindowViewState(configData);
 
         configData.setSelectedIds(panel.getSelectedItems());
         configData.setCategory(panel.getCurrentFilter());
         configData.setCurrentPage(panel.getCurrentOffset());
         configData.put(NotificationWindowConfig.SORT_DIR, new JSONString(panel.getCurrentSortDir()
                 .toString()));
 
         // Build window config
         WindowConfigFactory configFactory = new WindowConfigFactory();
         JSONObject windowConfig = configFactory.buildWindowConfig(Constants.CLIENT.myNotifyTag(),
                 configData);
         WindowDispatcher dispatcher = new WindowDispatcher(windowConfig);
         return dispatcher.getDispatchJson(Constants.CLIENT.myNotifyTag(), ActionType.DISPLAY_WINDOW);
 
     }
 }
