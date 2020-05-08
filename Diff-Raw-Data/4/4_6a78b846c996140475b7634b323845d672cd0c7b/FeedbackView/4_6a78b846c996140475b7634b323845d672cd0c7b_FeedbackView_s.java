 package nl.sense_os.commonsense.client.states.feedback;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.models.SensorModel;
 
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 
 public class FeedbackView extends View {
 
     private static final Logger LOGGER = Logger.getLogger("FeedbackView");
 
     public FeedbackView(Controller c) {
         super(c);
         LOGGER.setLevel(Level.ALL);
     }
     @Override
     protected void handleEvent(AppEvent event) {
         final EventType type = event.getType();
 
         if (type.equals(FeedbackEvents.FeedbackInit)) {
             LOGGER.finest("FeedbackInit");
             final SensorModel state = event.<SensorModel> getData("state");
             final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
             onInitEvent(state, sensors);
 
         } else
 
         /**
          * Request for labels
          */
         if (type.equals(FeedbackEvents.LabelsSuccess)) {
             LOGGER.finest("LabelsSuccess");
             final List<String> labels = event.<List<String>> getData("labels");
             final SensorModel state = event.<SensorModel> getData("state");
             final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
             onLabelsSuccess(state, sensors, labels);
 
         } else if (type.equals(FeedbackEvents.LabelsFailure)) {
             LOGGER.warning("LabelsFailure");
             onLabelsFailure();
 
         } else
 
         /*
          * Feedback chooser panel has finished
          */
         if (type.equals(FeedbackEvents.FeedbackChosen)) {
             LOGGER.finest("FeedbackChosen");
             final List<String> labels = event.<List<String>> getData("labels");
             final SensorModel state = event.<SensorModel> getData("state");
             final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
             final long start = event.getData("start");
             final long end = event.getData("end");
             showPanel(state, sensors, labels, start, end);
 
         } else
 
         /**
          * Feedback results
          */
         if (type.equals(FeedbackEvents.FeedbackComplete)) {
             LOGGER.fine("FeedbackComplete");
             final FeedbackPanel panel = event.<FeedbackPanel> getData("panel");
             panel.onFeedbackComplete();
 
         } else if (type.equals(FeedbackEvents.FeedbackFailed)) {
             LOGGER.warning("FeedbackFailed");
             final FeedbackPanel panel = event.<FeedbackPanel> getData("panel");
             panel.onFeedbackFailed();
 
         } else
 
         {
             LOGGER.warning("Unexpected event type!");
         }
     }
 
     @Override
     protected void initialize() {
         super.initialize();
     }
 
     private void onInitEvent(SensorModel state, List<SensorModel> sensors) {
         AppEvent labelRequest = new AppEvent(FeedbackEvents.LabelsRequest);
         labelRequest.setData("state", state);
         labelRequest.setData("sensors", sensors);
         fireEvent(labelRequest);
     }
 
     private void onLabelsFailure() {
         MessageBox.alert(null, "Failed to initialize feedback panel!", null);
     }
 
     private void onLabelsSuccess(SensorModel state, List<SensorModel> sensors, List<String> labels) {
         AppEvent showChooser = new AppEvent(FeedbackEvents.ShowChooser);
         showChooser.setData("state", state);
         showChooser.setData("sensors", sensors);
         showChooser.setData("labels", labels);
         fireEvent(showChooser);
     }
 
     private void showPanel(SensorModel state, List<SensorModel> sensors, List<String> labels,
             long start, long end) {
        String title = state.<String> get("text");
         FeedbackPanel panel = new FeedbackPanel(state, sensors, start, end, title, labels);
 
         AppEvent showEvent = new AppEvent(FeedbackEvents.ShowFeedback);
         showEvent.setData("panel", panel);
         showEvent.setData("title", title);
         Dispatcher.forwardEvent(showEvent);
     }
 
 }
