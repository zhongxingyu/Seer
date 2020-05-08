 package org.bodytrack.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bodytrack.client.ChannelManager.StringPair;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A panel that offers a way to show the current channel names available to
  * the user, not necessarily just the current channels that are showing.
  */
 public class ChannelNamesWidget extends VerticalPanel
 		implements ChannelChangedListener {
 	private final ChannelManager visible; // Channels currently on the grapher
 	private final String devicesUrl;
 	private final Grapher2 channelGenerator;
 
 	private final Map<String, DisclosurePanel> devices;
 	private final Map<CheckBox, StringPair> checkBoxes;
 	private final Map<CheckBox, HandlerRegistration> handlerRegs;
 		// We will use handlerRegs to allow adding and removing channels
 		// on this widget itself, cleaning up any handlers as we go
 
 	private final OnCheckHandler valueChangeHandler;
 
 	/**
 	 * Creates a new ChannelNamesWidget.
 	 *
 	 * <p>This downloads the map of device and channel names, then
 	 * adds all the channels to this widget.  This also adds itself
 	 * as a listener to mgr, so that check boxes work properly whenever
 	 * the user adds or removes a channel through some other means.</p>
 	 *
 	 * @param mgr
 	 * 		the {@link org.bodytrack.client.ChannelManager ChannelManager}
 	 * 		that holds the list of channels on the grapher at any time
 	 * @param userId
 	 * 		the ID of the current user
 	 * @param channelGenerator
 	 * 		the <tt>Grapher2</tt> that can be used to generate a new
 	 * 		<tt>DataPlot</tt> to add to the grapher
 	 * @throws NullPointerException
 	 * 		if mgr or channelGenerator is <tt>null</tt>
 	 */
 	public ChannelNamesWidget(ChannelManager mgr, int userId,
 			Grapher2 channelGenerator) {
 		if (mgr == null || channelGenerator == null)
 			throw new NullPointerException(
 				"Cannot use null value to initialize ChannelNamesWidget");
 
 		visible = mgr;
 		devicesUrl = buildDevicesUrl(userId);
 		this.channelGenerator = channelGenerator;
 
 		devices = new HashMap<String, DisclosurePanel>();
 		valueChangeHandler = new OnCheckHandler();
 
 		checkBoxes = new HashMap<CheckBox, StringPair>();
 		handlerRegs = new HashMap<CheckBox, HandlerRegistration>();
 
 		// Initialization code
 		mgr.addChannelListener(this);
 
 		loadDevicesAndChannels();
 	}
 
 	/**
 	 * Builds the URL at which we can get the JSON map of devices
 	 * and channels.
 	 *
 	 * @param userId
 	 * 		the ID of the current user
 	 * @return
 	 * 		the non-<tt>null</tt> URL at which we can download the
 	 * 		JSON map of devices and channels
 	 */
 	private static String buildDevicesUrl(int userId) {
 		return "/users/" + userId + "/devices.json";
 	}
 
 	/**
 	 * A method to be called in the constructor.
 	 *
 	 * <p>This loads the map of devices and channels from devicesUrl,
 	 * and then calls {@link #loadSuccess(String)} with the response
 	 * body if the request loads successfully, or {@link #loadFailure()}
 	 * otherwise.</p>
 	 */
 	private void loadDevicesAndChannels() {
 		// Send request to server and catch any errors.
 		RequestBuilder builder =
 			new RequestBuilder(RequestBuilder.GET, devicesUrl);
 
 		try {
 			builder.sendRequest(null, new RequestCallback() {
 				@Override
 				public void onError(Request request,
 						Throwable exception) {
 					loadFailure(devicesUrl);
 				}
 
 				@Override
 				public void onResponseReceived(Request request,
 						Response response) {
 					if (GrapherTile.isSuccessful(response))
 						loadSuccess(response.getText());
 					else
 						loadFailure(devicesUrl);
 				}
 			});
 		} catch (RequestException e) {
 			loadFailure(devicesUrl);
 		}
 	}
 
 	/**
 	 * Called whenever the map of devices and channels fails to load.
 	 *
 	 * <p>This simply alerts the user to an error in loading the
 	 * channels.<p>
 	 *
 	 * @param requestUrl
 	 * 		the URL at which the failed tile was requested (this should
 	 * 		be the devicesUrl instance variable)
 	 */
 	private native void loadFailure(String requestUrl) /*-{
 		alert("Failed to load list of channels, using URL " + requestUrl);
 	}-*/;
 
 	/**
 	 * Called whenever the map of devices and channels is successfully loaded.
 	 *
 	 * <p>This populates the deviceMap variable, and calls
 	 * {@link #addChannel(String, String)} on each channel in deviceMap.
 	 * All information comes from json.</p>
 	 *
 	 * <h4 style="color: red">Security Warning:</h4>
 	 * <p>This method uses eval(), meaning that it is vulnerable to
 	 * security issues if json is from an untrusted source.  It is
 	 * <em>critical</em> that json be from a trusted source, as
 	 * discussed in the documentation for
 	 * {@link org.bodytrack.client.GrapherTile#retrieveTile
 	 * GrapherTile.retrieveTile}.</p>
 	 *
 	 * @param json
 	 * 		the body of the reply from the server
 	 */
 	private void loadSuccess(String json) {
 		// TODO: Possibly add DeviceMap overlay type that will
 		// persistently hold deviceMap info in a more usable form
 
 		// We know that json represents a map
 		JSONValue deviceValue = JSONParser.parse(json);
 
 		JSONObject deviceMap = deviceValue.isObject();
 		if (deviceMap == null)
 			return;
 
 		for (String deviceName: deviceMap.keySet()) {
 			JSONValue currDeviceValue = deviceMap.get(deviceName);
 
 			JSONObject currDevice = currDeviceValue.isObject();
 			if (currDevice == null)
 				continue;
 
 			// Ignore any maps that don't have the right key
 			if (currDevice.containsKey("ch_names")) {
 				JSONValue channelValue = currDevice.get("ch_names");
 
 				JSONArray channels = channelValue.isArray();
 				if (channels == null)
 					continue;
 
 				for (int i = 0; i < channels.size(); i++) {
 					// Channel name Should always get a string
 					JSONString channelName = channels.get(i).isString();
 
 					if (channelName != null)
 						addChannelToWidget(deviceName,
 							channelName.stringValue());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds the specified channel to the widget, not to the channel manager.
 	 *
 	 * <p>Note that we require that the channel is not already part of this
 	 * widget, unless the caller wants to add that channel twice (a sticky
 	 * situation).  We do not attempt to check whether the channel is already
 	 * part of this widget or not.</p>
 	 *
 	 * @param deviceName
 	 * 		the name of the device for the channel to add
 	 * @param channelName
 	 * 		the name of the channel on the device given by deviceName
 	 */
 	private void addChannelToWidget(String deviceName,
 			String channelName) {
 		if (devices.containsKey(deviceName)) {
 			// Need to add to the existing panel
 			DisclosurePanel panel = devices.get(deviceName);
 
 			addChannelToDisclosurePanel(panel, deviceName, channelName,
 				valueChangeHandler);
 		} else {
 			// Need to make a new panel
 			List<String> channelNames = new ArrayList<String>();
 			add(buildSingleDevicePanel(deviceName, channelNames,
 					valueChangeHandler));
 		}
 	}
 
 	/**
 	 * Builds and returns a new
 	 * {@link com.google.gwt.user.client.ui.DisclosurePanel DisclosurePanel}
 	 * with a series of check boxes that get shown when the user opens
 	 * the panel.
 	 *
 	 * <p>This adds the appropriate check boxes and handlers to the
 	 * devices, channels, checkBoxes, and handlerRegs private variables, as
 	 * well.</p>
 	 *
 	 * <p>All the check boxes are initially unchecked, and in the order
 	 * in which they appear in the channelNames parameter.  The returned
 	 * disclosure panel is initially closed.</p>
 	 *
 	 * @param deviceName
 	 * 		the device name that will show even when the panel is closed
 	 * @param channelNames
 	 * 		the device names that will show as check boxes when the panel
 	 * 		is opened
 	 * @param handler
 	 * 		a {@link com.google.gwt.event.logical.shared.ValueChangeHandler
 	 * 		ValueChangeHandler} that will be called whenever
 	 * 		a check box is checked or unchecked
 	 * @return
 	 * 		a <tt>DisclosurePanel</tt> showing deviceName at all times, and
 	 * 		the elements of channelNames when opened
 	 */
 	private DisclosurePanel buildSingleDevicePanel(String deviceName,
 			List<String> channelNames, ValueChangeHandler<Boolean> handler) {
 		DisclosurePanel result = new DisclosurePanel(deviceName);
 		result.add(new VerticalPanel());
 		// We can only add 1 widget directly to a DisclosurePanel,
 		// so we use a second panel to allow multiple check boxes
 		// on one DisclosurePanel
 
 		for (String name: channelNames)
 			addChannelToDisclosurePanel(result, deviceName, name, handler);
 
 		devices.put(deviceName, result);
 
 		return result;
 	}
 
 	/**
 	 * Adds a single channel to a panel.
 	 *
 	 * <p>This adds the appropriate check boxes and handlers to the
 	 * channels, checkBoxes, and handlerRegs private variables, as well.</p>
 	 *
 	 * @param panel
 	 * 		the panel to which to add a <tt>CheckBox</tt> with the specified
 	 * 		channel name
 	 * @param deviceName
 	 * 		the name of the device represented by panel
 	 * @param channelName
 	 * 		the name of the channel to add
 	 * @param handler
 	 * 		the <tt>ValueChangeHandler</tt> that should handle
 	 * 		events on the new check box for the new channel
 	 */
 	private void addChannelToDisclosurePanel(DisclosurePanel panel,
 			String deviceName, String channelName,
 			ValueChangeHandler<Boolean> handler) {
 		CheckBox box = new CheckBox(channelName);
 		HandlerRegistration reg = box.addValueChangeHandler(handler);
 
 		StringPair channelNamePair =
 			new StringPair(deviceName, channelName);
 		checkBoxes.put(box, channelNamePair);
 		handlerRegs.put(box, reg);
 
 		Widget innerWidget = panel.getContent();
 		if (innerWidget instanceof Panel)
 			((Panel) innerWidget).add(box);
 		// We can only add 1 widget directly to a DisclosurePanel,
 		// so we use a second panel to allow multiple check boxes
 		// on one DisclosurePanel
 	}
 
	// TODO: COMMENT
 	@Override
 	public void channelAdded(String deviceName, String channelName) {
 		setCheckBoxValue(deviceName, channelName, true);
 	}
 
	// TODO: COMMENT
 	@Override
 	public void channelRemoved(String deviceName, String channelName) {
 		setCheckBoxValue(deviceName, channelName, false);
 	}
 
 	/**
 	 * Helper method for {@link #channelAdded(String, String)} and
 	 * {@link #channelRemoved(String, String)}.
 	 *
 	 * <p>This method finds all check boxes with the specified
 	 * channel and device name, and changes their value to value.
 	 *
 	 * @param deviceName
 	 * 		the name of the device for the check boxes to change
 	 * @param channelName
 	 * 		the channel name for the check boxes to change
 	 * @param value
 	 * 		the value (<tt>true</tt> to check the boxes,
 	 * 		<tt>false</tt> to uncheck the boxes) to which to change
 	 * 		the check boxes matching (deviceName, channelName)
 	 */
 	private void setCheckBoxValue(String deviceName, String channelName,
 			Boolean value) {
 		StringPair name = new StringPair(deviceName, channelName);
 
 		for (Map.Entry<CheckBox, StringPair> ent: checkBoxes.entrySet()) {
 			if (name.equals(ent.getValue()))
 				ent.getKey().setValue(value);
 		}
 	}
 
 	/**
 	 * A handler that will call the appropriate method on its
 	 * {@link org.bodytrack.client.ChannelChangedListener
 	 * ChannelChangedListener} whenever a check box is checked or unchecked.
 	 */
 	private class OnCheckHandler implements ValueChangeHandler<Boolean> {
 		/**
 		 * Called whenever a check box is checked or unchecked.
 		 */
 		@Override
 		public void onValueChange(ValueChangeEvent<Boolean> event) {
 			Object source = event.getSource();
 			if (! (source instanceof CheckBox))
 				return;
 
 			CheckBox sourceBox = (CheckBox) source;
 			String deviceName = checkBoxes.get(sourceBox).getFirst();
 			String channelName = checkBoxes.get(sourceBox).getSecond();
 
 			if (event.getValue())
 				// User just checked a button
 				visible.addChannel(channelGenerator.buildDataPlot(deviceName,
 					channelName));
 			else
 				// User just unchecked a button
 				visible.removeChannel(deviceName, channelName);
 		}
 	}
 }
