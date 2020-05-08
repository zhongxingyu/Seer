 package nl.sense_os.commonsense.client.demo;
 
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.constants.Constants;
 import nl.sense_os.commonsense.client.utility.SenseIconProvider;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.extjs.gxt.ui.client.util.IconHelper;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.user.client.ui.Frame;
 
 public class DemoView extends View {
 
     private static final Logger LOGGER = Logger.getLogger(DemoView.class.getName());
     private TabPanel tabPanel;
 
     public DemoView(Controller controller) {
         super(controller);
     }
 
     @Override
     protected void handleEvent(AppEvent event) {
         final EventType type = event.getType();
 
         if (type.equals(DemoEvents.Show)) {
             LOGGER.finest("Show");
             LayoutContainer parent = event.getData("parent");
             showPanel(parent);
 
         } else {
             LOGGER.warning("Unexpected event: " + event);
         }
 
     }
 
     @Override
     protected void initialize() {
         initTabPanel();
         super.initialize();
     }
 
     private void initTabPanel() {
         // Tabs panel
         this.tabPanel = new TabPanel();
         this.tabPanel.setId("tab-panel");
         this.tabPanel.setSize("100%", "100%");
         this.tabPanel.setPlain(true);
         this.tabPanel.setMinTabWidth(120);
         this.tabPanel.addStyleName("transparent");
     }
 
     private void initTabItems() {
 
         // clear any existing tabs
         this.tabPanel.removeAll();
 
         // Availability dashboard
         String params = "";
         String sessionId = Registry.get(Constants.REG_SESSION_ID);
         if (null != sessionId) {
             params = "&session_id=" + sessionId;
         }
        final Frame dashboard = new Frame("http://data.sense-os.nl/dashboard?plain=1&hd_mode=1"
                + params);
         dashboard.setStylePrimaryName("senseFrame");
         final TabItem dashboardItem = new TabItem("Availability");
         dashboardItem.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH
                 + "sense_orange.gif"));
         dashboardItem.setLayout(new FitLayout());
         dashboardItem.add(dashboard);
         this.tabPanel.add(dashboardItem);
 
         // Track trace
         final Frame trackTrace = new Frame("http://almendetracker.appspot.com/?profileURL="
                 + "http://demo.almende.com/tracker/ictdelta");
         trackTrace.setStylePrimaryName("senseFrame");
         final TabItem trackTraceItem = new TabItem("Track & Trace");
         trackTraceItem.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH
                 + "sense_orange.gif"));
         trackTraceItem.setLayout(new FitLayout());
         trackTraceItem.add(trackTrace);
         this.tabPanel.add(trackTraceItem);
 
         // Humidity
         final Frame humid3d = new Frame(
                 "http://demo.almende.com/links/storm/day_40_humid_animation.html");
         humid3d.setStylePrimaryName("senseFrame");
         final TabItem humid3dItem = new TabItem("3D Chart Preview");
         humid3dItem.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH
                 + "sense_orange.gif"));
         humid3dItem.setLayout(new FitLayout());
         humid3dItem.add(humid3d);
         this.tabPanel.add(humid3dItem);
     }

     private void showPanel(LayoutContainer parent) {
         if (null != parent) {
             initTabItems();
 
             parent.add(this.tabPanel);
             parent.layout();
         } else {
             LOGGER.warning("Failed to show demo panel: parent=null");
         }
     }
 
 }
