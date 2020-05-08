 package nl.sense_os.commonsense.main.client.sensors.publish;
 
 import java.util.List;
 
 import nl.sense_os.commonsense.common.client.util.Constants;
 import nl.sense_os.commonsense.main.client.ext.component.CenteredWindow;
 import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
 import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 
 public class PublishConfirmDialog extends View {
 
 	private CenteredWindow window;
	private Text text;
 	private Button publishButton;
 	private Button cancelButton;
 	private List<ExtSensor> sensors;
 	private CheckBox anonymous;
 
 	public PublishConfirmDialog(Controller c) {
 		super(c);
 	}
 
 	private void closeWindow() {
 		setBusy(false);
 		window.hide();
 	}
 
 	@SuppressWarnings("unused")
 	private void getDatasetUrl() {
 		setBusy(true);
 		AppEvent publish = new AppEvent(PublishEvents.DatasetUrlRequest);
 		publish.setData("user", Registry.<ExtUser> get(Constants.REG_USER));
 		publish.setData("anonymous", anonymous.getValue().booleanValue());
 		fireEvent(publish);
 	}
 
 	@Override
 	protected void handleEvent(AppEvent event) {
 		final EventType type = event.getType();
 
 		if (type.equals(PublishEvents.ShowPublisher)) {
 			final List<ExtSensor> sensors = event.<List<ExtSensor>> getData("sensors");
 			onShow(sensors);
 
 		} else if (type.equals(PublishEvents.PublicationSuccess)) {
 			String url = event.getData("url");
 			String title = event.getData("title");
 			String name = event.getData("name");
 			int[] sensorIds = event.getData("sensorIds");
 			onPublicationSuccess(url, title, name, sensorIds);
 
 		} else if (type.equals(PublishEvents.DatasetUrlSuccess)) {
 			String url = event.getData("url");
 			onDatasetUrlSuccess(url);
 
 		} else if (type.equals(PublishEvents.PublicationError)
 				|| type.equals(PublishEvents.DatasetUrlError)) {
 			int code = event.getData("code");
 			Throwable error = event.getData("error");
 			onPublicationFailure(code, error);
 		}
 	}
 
 	private void initButtons() {
 		SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				final Button button = ce.getButton();
 				if (button.equals(publishButton)) {
 					publish();
 				} else if (button.equals(cancelButton)) {
 					closeWindow();
 				}
 
 			}
 		};
 
 		publishButton = new Button("Yes", l);
 		publishButton.setIconStyle("sense-btn-icon-go");
 		cancelButton = new Button("No", l);
 		window.addButton(publishButton);
 		window.addButton(cancelButton);
 	}
 
 	private void initForm() {
 
 		FormPanel form = new FormPanel();
 		form.setHeaderVisible(false);
 		form.setBodyBorder(false);
 		anonymous = new CheckBox();
 		anonymous.setBoxLabel("Publish anonymously");
 		anonymous.setHideLabel(true);
 		form.add(anonymous);
 
		text = new Text();
 		text.setStyleAttribute("font-size", "13px");
		text.setStyleAttribute("margin", "10px");
 
 		LayoutContainer container = new LayoutContainer();
 		container.add(text);
 		container.add(form);
 		window.add(container);
 	}
 
 	@Override
 	protected void initialize() {
 		super.initialize();
 
 		window = new CenteredWindow();
         window.setHeadingText("Publish sensors");
 		// window.setLayout(new FitLayout());
 		window.setSize(400, 250);
 		window.setScrollMode(Scroll.AUTOY);
 
 		initButtons();
 		initForm();
 
 		setBusy(false);
 	}
 
 	private void onDatasetUrlSuccess(String url) {
 		setBusy(false);
 		closeWindow();
 		MessageBox.info(null, "Publication complete! Your data will be published at " + url, null);
 	}
 
 	private void onPublicationFailure(int code, Throwable error) {
		text.setText("Publication failed! Error: " + code + ", message: '" + error.getMessage()
				+ "'.<br/><br/>Do you want to try again?");
 		setBusy(false);
 		window.show();
 	}
 
 	private void onPublicationSuccess(String url, String title, String name, int[] sensorIds) {
 		setBusy(false);
 		closeWindow();
 		String msg = "Publication complete! Your data is published ";
 		msg += "<a href='http://data.rotterdamopendata.nl:9090/nl/dataset/" + url
 				+ "' target='_blank'>here</a>.";
 		MessageBox.info(null, msg, null);
 	}
 
 	private void onShow(final List<ExtSensor> sensors) {
 
 		this.sensors = sensors;
 
		String message = "This will add a link to your sensor data on the Rotterdam Open Data Store (RODS). For more information, please go to <a href=\"http://data.rotterdamopendata.nl\" target=\"_blank\">data.rotterdamopendata.nl</a>.";
		message += "<br/><br/>";
 		if (sensors.size() > 1) {
			message += "Are you sure you want to continue with the publication of the "
					+ sensors.size() + " selected sensors?";
 		} else {
			message += "Are you sure you want to continue with the publication of the selected sensor?";
 		}
 
		text.setText(message);
 		window.show();
 		window.center();
 	}
 
 	private void publish() {
 		setBusy(true);
 		AppEvent publish = new AppEvent(PublishEvents.PublishRequest);
 		publish.setData("user", Registry.<ExtUser> get(Constants.REG_USER));
 		publish.setData("sensors", sensors);
 		publish.setData("anonymous", anonymous.getValue().booleanValue());
 		fireEvent(publish);
 	}
 
 	private void setBusy(boolean busy) {
 		if (busy) {
 			publishButton.setIconStyle("sense-btn-icon-loading");
 			cancelButton.setEnabled(false);
 		} else {
 			publishButton.setIconStyle("sense-btn-icon-go");
 			cancelButton.setEnabled(true);
 		}
 	}
 }
