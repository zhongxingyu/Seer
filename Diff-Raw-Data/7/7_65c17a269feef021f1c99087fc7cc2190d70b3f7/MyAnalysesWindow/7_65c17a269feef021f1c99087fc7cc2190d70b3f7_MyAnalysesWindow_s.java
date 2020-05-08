 package org.iplantc.de.client.views.windows;
 
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.dispatchers.WindowDispatcher;
 import org.iplantc.de.client.factories.EventJSONFactory.ActionType;
 import org.iplantc.de.client.factories.WindowConfigFactory;
 import org.iplantc.de.client.models.AnalysesWindowConfig;
 import org.iplantc.de.client.models.WindowConfig;
 import org.iplantc.de.client.views.panels.MyAnalysesPanel;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Element;
 
 /**
  * A window thats acts as a container for MyAnalysesPanel
  * 
  * @author sriram
  * 
  */
 public class MyAnalysesWindow extends IPlantWindow {
     private BorderLayoutData centerData;
     private MyAnalysesPanel pnlAnlys;
    private WindowConfig config;
 
     /**
      * Instantiate from a tag.
      * 
      * @param tag unique tag identifying this window.
      * @param config a window configuration
      */
     public MyAnalysesWindow(String tag, WindowConfig config) {
         super(tag, false, true, true, true);
 
         this.config = config;
         init(tag);
     }
 
     private void init(String tag) {
         setId(tag);
         setHeading(I18N.DISPLAY.analyses());
         setSize(700, 410);
 
         centerData = buildCenterData();
 
         BorderLayout layout = new BorderLayout();
         setLayout(layout);
     }
 
     /**
      * Build center data for BorderLayout.
      * 
      * @return an object describing the layout for the panel.
      */
     protected BorderLayoutData buildCenterData() {
         BorderLayoutData ret = new BorderLayoutData(LayoutRegion.CENTER, 400);
         ret.setMargins(new Margins(5, 5, 5, 5));
 
         return ret;
     }
 
     /**
      * Release resources allocated by this window.
      */
     @Override
     public void cleanup() {
         super.cleanup();
 
         pnlAnlys.cleanup();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onRender(Element parent, int index) {
         super.onRender(parent, index);
 
         String idCurrentSelection = (config == null) ? null : ((AnalysesWindowConfig)config)
                 .getAnalysisId();
 
         pnlAnlys = new MyAnalysesPanel(I18N.DISPLAY.analysisOverview(), idCurrentSelection);
 
         if (config != null) {
             pnlAnlys.updateSelection(idCurrentSelection);
         }
 
         add(pnlAnlys, centerData);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setWindowConfig(WindowConfig config) {
         if (config != null) {
             this.config = (AnalysesWindowConfig)config;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void show() {
         super.show();
 
         if (pnlAnlys != null && config != null) {
             pnlAnlys.updateSelection(((AnalysesWindowConfig)config).getAnalysisId());
             setWindowViewState();
             config = null;
         }
 
     }
 
     @Override
     public JSONObject getWindowState() {
         // Build config data
         AnalysesWindowConfig configData = new AnalysesWindowConfig(config);
         storeWindowViewState(configData);
 
         if (pnlAnlys.getIdCurrentSelection() != null) {
             configData.setAnalysisId(pnlAnlys.getIdCurrentSelection());
         }
 
         // Build window config
         WindowConfigFactory configFactory = new WindowConfigFactory();
         JSONObject windowConfig = configFactory.buildWindowConfig(Constants.CLIENT.myAnalysisTag(),
                 configData);
         WindowDispatcher dispatcher = new WindowDispatcher(windowConfig);
         return dispatcher.getDispatchJson(Constants.CLIENT.myAnalysisTag(), ActionType.DISPLAY_WINDOW);
     }
 }
