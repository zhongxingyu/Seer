 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // UTGBEntryPointBase.java
 // Since: Jun 2, 2008
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackGroupProperty;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyChange;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyChangeListener;
 import org.utgenome.gwt.utgb.client.track.TrackQueue;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.track.UTGBProperty;
 import org.utgenome.gwt.utgb.client.ui.RoundCornerFrame;
 import org.utgenome.gwt.utgb.client.util.BrowserInfo;
 import org.utgenome.gwt.utgb.client.util.Properties;
 import org.utgenome.gwt.utgb.client.util.StringUtil;
 import org.utgenome.gwt.utgb.client.view.TrackView;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.EventTarget;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class UTGBEntryPointBase implements EntryPoint {
 	// widgets
 	private final DockPanel basePanel = new DockPanel();
 	private final TrackGroup trackGroup = new TrackGroup("root");
 	private TrackGroup mainGroup;
 	private final TrackQueue trackQueue = new TrackQueue(trackGroup);
 
 	private HashMap<String, String> queryParam = new HashMap<String, String>();
 
 	public TrackGroup getTrackGroup() {
 		return trackGroup;
 	}
 
 	public TrackQueue getTrackQueue() {
 		return trackQueue;
 	}
 
 	public DockPanel getBasePanel() {
 		return basePanel;
 	}
 
 	/**
 	 * Defines keyboard shortcuts
 	 * 
 	 * @author leo
 	 * 
 	 */
 	public class KeyboardShortcut implements Event.NativePreviewHandler {
 		public void onPreviewNativeEvent(NativePreviewEvent event) {
 
 			// handle shortcut keys
 			int type = event.getTypeInt();
 			int keyCode = event.getNativeEvent().getKeyCode();
 
 			switch (type) {
 			case Event.ONKEYDOWN:
 				EventTarget eventTarget = event.getNativeEvent().getEventTarget();
 				if (Element.is(eventTarget)) {
 					Element e = eventTarget.cast();
 					String tagName = e.getTagName();
 					// disable keyboard short cuts on the input form (text area, etc.)
 					if (tagName.equalsIgnoreCase("input"))
 						break;
 
 					// Also ignore keyboard input, ALT+(key)
 					if (event.getNativeEvent().getAltKey())
 						break;
 
 					double scrollPercentage = 20.0;
 					if (event.getNativeEvent().getShiftKey())
 						scrollPercentage = 25.0;
 
 					switch (keyCode) {
 					case KeyCodes.KEY_RIGHT:
 						trackGroup.getPropertyWriter().scrollTrackWindow(scrollPercentage);
 						event.getNativeEvent().preventDefault();
 						break;
 					case KeyCodes.KEY_LEFT:
 						trackGroup.getPropertyWriter().scrollTrackWindow(-scrollPercentage);
 						event.getNativeEvent().preventDefault();
 						break;
 					case KeyCodes.KEY_UP:
 						trackGroup.getPropertyWriter().scaleUpTrackWindow();
 						event.getNativeEvent().preventDefault();
 						break;
 					case KeyCodes.KEY_DOWN:
 						trackGroup.getPropertyWriter().scaleDownTrackWindow();
 						event.getNativeEvent().preventDefault();
 						break;
 					}
 
 				}
 				break;
 			}
 
 		}
 
 	}
 
 	public void onModuleLoad() {
 		RPCServiceManager.initServices();
 		queryParam = BrowserInfo.getURLQueryRequestParameters();
 		RootPanel.get().setStyleName("utgb");
 
 		basePanel.add(trackQueue, DockPanel.CENTER);
 
 		History.addValueChangeHandler(new HistoryChangeHandler());
 		Event.addNativePreviewHandler(new KeyboardShortcut());
 
 		// add window size change listener
 		Window.addResizeHandler(new ResizeHandler() {
 
 			public void onResize(ResizeEvent e) {
 				adjustTrackWidth();
 
 			}
 		});
 
 		// invoke main method
 		main();
 
 		if (BrowserInfo.isIE()) {
 			showErrorMessage("IE does not support canvas feature in HTML5 for drawing grpahics in the browser, so we strongly recommend you to use another browser supporting HTML5, e.g., Google Chrome, Firefox, Safari, Opera, etc.");
 		}
 
 	}
 
 	public static int computeTrackWidth() {
		RootPanel rootPanel = RootPanel.get("utgb-main");

		int newBrowserWidth = rootPanel.getOffsetWidth(); // Window.getClientWidth();
 		return Math.max((int) (newBrowserWidth * 0.95) - TrackFrame.INFOPANEL_WIDTH, 150);
 	}
 
 	private void adjustTrackWidth() {
 		int newTrackWidth = computeTrackWidth();
 		for (TrackGroup g : trackGroup.getTrackGroupList()) {
 			g.setTrackWindowWidth(newTrackWidth);
 		}
 
 	}
 
 	public void displayTrackView() {
 		// load a view
 		if (queryParam.containsKey("view")) {
 			loadView(queryParam.get("view"));
 		}
 		else {
 			loadView("default-view");
 		}
 
 		RootPanel rootPanel = RootPanel.get("utgb-main");
 		if (rootPanel != null) {
 			rootPanel.add(basePanel);
 		}
 		else {
 			RootPanel.get().add(new Label("Error: <div id=\"utgb-main\"></div> tag is not found in this HTML file."));
 		}
 	}
 
 	/**
 	 * load the view XML file from the public/view folder.
 	 * 
 	 * @param viewXMLPath
 	 */
 	public void loadView(String viewName) {
 
 		RPCServiceManager.getRPCService().getTrackView(viewName, new AsyncCallback<TrackView>() {
 			public void onFailure(Throwable e) {
 				showErrorMessage("failed to load view: " + e.getMessage());
 			}
 
 			public void onSuccess(TrackView v) {
 				try {
 					mainGroup = TrackGroup.createTrackGroup(v);
 
 					// apply the URL query parameters
 					String hash = BrowserInfo.getHash();
 					if (hash != null && hash.length() > 0)
 						hash = hash.substring(1);
 					setQueryParam(mainGroup, hash);
 					trackGroup.addTrackGroup(mainGroup);
 					mainGroup.addTrackGroupPropertyChangeListener(new URLRewriter(mainGroup));
 				}
 				catch (UTGBClientException e) {
 					showErrorMessage("failed to load view: " + e.getMessage());
 					GWT.log(e.getMessage(), e);
 				}
 			}
 		});
 
 	}
 
 	private static class URLRewriter implements TrackGroupPropertyChangeListener {
 		public final TrackGroup group;
 
 		public URLRewriter(TrackGroup group) {
 			this.group = group;
 		}
 
 		public void onChange(TrackGroupPropertyChange change, TrackWindow newWindow) {
 			if (newWindow != null || (change != null && change.containsOneOf(UTGBProperty.coordinateParameters)))
 				setBrowserURL();
 		}
 
 		public void setBrowserURL() {
 			ArrayList<String> prop = new ArrayList<String>();
 
 			TrackGroupProperty propertyReader = group.getPropertyReader();
 			TrackWindow w = group.getTrackWindow();
 			prop.add("start=" + w.getStartOnGenome());
 			prop.add("end=" + w.getEndOnGenome());
 			//prop.add("width=" + w.getWindowWidth());
 			for (String key : propertyReader.keySet()) {
 				prop.add(key + "=" + propertyReader.getProperty(key));
 			}
 
 			String n = StringUtil.join(prop, ";");
 			String prev = History.getToken();
 
 			if (prev != null && prev.equals(n))
 				return;
 			else {
 				History.newItem(n, false);
 				String s = propertyReader.getProperty(UTGBProperty.TARGET) + ":" + w.getStartOnGenome() + "-" + w.getEndOnGenome();
 				Window.setTitle(s + " - UTGB");
 			}
 
 		}
 
 	}
 
 	private class HistoryChangeHandler implements ValueChangeHandler<String> {
 
 		public HistoryChangeHandler() {
 		}
 
 		public void onValueChange(ValueChangeEvent<String> e) {
 			if (mainGroup != null)
 				setQueryParam(mainGroup, e.getValue());
 		}
 	}
 
 	private static void setQueryParam(TrackGroup group, String queryParam) {
 		TrackWindow w = group.getTrackWindow();
 
 		Properties p = getProperties(queryParam);
 		if (p.containsKey("start")) {
 			int start = Integer.parseInt(p.get("start"));
 			int end = p.containsKey("end") ? Integer.parseInt(p.get("end")) : start + 1000;
 			w = w.newWindow(start, end);
 		}
 
 		p.remove("start");
 		p.remove("end");
 
 		group.getPropertyWriter().setProperty(p, w);
 	}
 
 	private static Properties getProperties(String query) {
 		Properties properties = new Properties();
 		if (query == null || query.length() < 1)
 			return properties;
 
 		String[] keyAndValue = query.split(";");
 		for (int i = 0; i < keyAndValue.length; i++) {
 			String[] kv = keyAndValue[i].split("=");
 			if (kv.length > 1)
 				properties.put(kv[0], BrowserInfo.unescape(kv[1]));
 			else
 				properties.put(kv[0], "");
 		}
 
 		return properties;
 
 	}
 
 	public void main() {
 		displayTrackView();
 	}
 
 	private static RoundCornerFrame errorFrame;
 	private static Label errorLabel = new Label();
 	private static PopupPanel errorPopup = new PopupPanel(true);
 	{
 		errorFrame = new RoundCornerFrame("FF6699", 0.7f, 2);
 		errorFrame.setWidth("400px");
 		errorFrame.setWidget(errorLabel);
 		Style.fontColor(errorLabel, "white");
 		errorPopup.setWidget(errorFrame);
 	}
 
 	public static void showErrorMessage(final String message) {
 
 		DeferredCommand.addCommand(new Command() {
 
 			public void execute() {
 				errorLabel.setText(message);
 				int x = Window.getClientWidth() / 2 - 200;
 				int y = 10;
 				errorPopup.setPopupPosition(x, y);
 				errorPopup.show();
 			}
 		});
 	}
 
 	public static void hideLoadingMessage() {
 		Element _loadingMessage = DOM.getElementById("loading");
 		if (_loadingMessage != null) {
 			RootPanel.setVisible(_loadingMessage, false);
 		}
 	}
 
 	public static void showLoadingMessage() {
 		Element _loadingMessage = DOM.getElementById("loading");
 		if (_loadingMessage != null) {
 			RootPanel.setVisible(_loadingMessage, true);
 		}
 	}
 
 }
