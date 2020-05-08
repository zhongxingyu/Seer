 package org.bodytrack.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
 
 /**
  * A widget that allows the user to switch between, add,
  * and modify views.  This is usually shown above the grapher itself,
  * and handles buttons and popup windows so that the user interface
  * for views is all out of this class.
  */
 // TODO: Keep track of current view, probably in this class
 // TODO: Comment new code
 public class ViewSwitchWidget extends HorizontalPanel {
 	/**
 	 * The ID attribute of this element.  Note that this only
 	 * works properly if there is only one <tt>ViewSwitchWidget</tt>
 	 * per page.
 	 */
 	private static final String WIDGET_ID = "viewSwitchWidget";
 
 	/**
 	 * The name of the CSS class on the popups that come from
 	 * this widget.
 	 */
 	public static final String POPUP_CLASS_NAME = "savedViewPopup";
 
 	/**
 	 * The maximum number of view names that will be visible to
 	 * the user at any time, in any popup from this widget.
 	 */
 	private static final int MAX_VISIBLE_VIEW_NAMES = 10;
 
 	private final int userId;
 	private final ChannelManager channels;
 	private final ViewSwitchClickHandler clickHandler;
 	private final PushButton saveView;
 	private final PushButton restoreView;
 
 	/**
 	 * Creates a new <tt>ViewSwitchWidget</tt>.
 	 *
 	 * @param userId
 	 * 		the ID of the current user
 	 * @param mgr
 	 * 		the {@link org.bodytrack.client.ChannelManager ChannelManager}
 	 * 		that keeps the current set of channels
 	 * @throws NullPointerException
 	 * 		if mgr is <tt>null</tt>
 	 */
 	public ViewSwitchWidget(int userId, ChannelManager mgr) {
 		if (mgr == null)
 			throw new NullPointerException(
 				"Cannot use null ChannelManager");
 
 		this.userId = userId;
 		channels = mgr;
 		clickHandler = new ViewSwitchClickHandler();
 		saveView = new PushButton("Save", clickHandler);
 		restoreView = new PushButton("Restore", clickHandler);
 
 		getElement().setId(WIDGET_ID);
 
 		add(saveView);
 		add(restoreView);
 	}
 
 	/**
 	 * The class that handles clicks for the saveView and restoreView
 	 * buttons.
 	 */
 	private class ViewSwitchClickHandler implements ClickHandler {
 		/**
 		 * Handles clicks for the saveView and restoreView buttons.
 		 *
 		 * <p>If event is <tt>null</tt> or came from a button that
 		 * is not part of this widget, does nothing.  Otherwise,
 		 * opens the correct popup to save or restore the current
 		 * view.</p>
 		 *
 		 * @param event
 		 * 		the click event containing information about the
 		 * 		user's click on the saveView or restoreView button
 		 */
 		@Override
 		public void onClick(ClickEvent event) {
 			if (event == null)
 				return;
 
 			Object sourceObject = event.getSource();
 			if (sourceObject == saveView) {
 				showPopup(new ViewSavePopup(null), saveView);
 			} else if (sourceObject == restoreView) {
 				showPopup(new ViewRestorePopup(), restoreView);
 			}
 		}
 
 		private void showPopup(final PopupPanel popup, final PushButton pos) {
 			popup.setPopupPositionAndShow(new PositionCallback() {
 				@Override
 				public void setPosition(int offsetWidth,
 						int offsetHeight) {
 					int left = pos.getAbsoluteLeft();
 					int top = pos.getAbsoluteTop()
 						+ pos.getOffsetHeight();
 					popup.setPopupPosition(left, top);
 				}
 			});
 		}
 	}
 
 	/**
 	 * A popup window that lets the user save the current view to
 	 * the server.
 	 *
 	 * <p>This window is designed to pop up once, save the current
 	 * view, and then be garbage collected.  If the user clicks the
 	 * save button multiple times, a new <tt>ViewSavePopup</tt>
 	 * should be created every time.</p>
 	 */
 	private class ViewSavePopup extends PopupPanel {
 		private final String currentView;
 		private final List<String> viewNames;
 
 		private final VerticalPanel content;
 		// We know that the top panel of content will be a HorizontalPanel
 		// containing the saveName box on the left and the save button
 		// on the right.  The bottom panel of content will be devoted
 		// to viewNamesList
 
 		private final TextBox saveName;
 		private final PushButton save;
 		private final ListBox viewNamesControl;
 
 		/**
 		 * Creates a new <tt>ViewSavePopup</tt> object but does not show it.
 		 *
 		 * @param currentView
 		 * 		the name of the current view
 		 */
 		public ViewSavePopup(String currentView) {
 			super(true, true);
 
 			this.currentView = currentView;
 			viewNames = new ArrayList<String>();
 
 			saveName = new TextBox();
 
 			save = new PushButton("Save", new ClickHandler() {
 				/**
 				 * Called whenever save is clicked.
 				 */
 				@Override
 				public void onClick(ClickEvent event) {
 					if (event.getSource() != save)
 						// We should never enter this
 						return;
 
 					String viewName = saveName.getText();
 					if (viewName.isEmpty())
 						return;
 					// TODO: Enforce rules on valid view names
 
 					SavableView view = SavableView.buildView(channels,
 						viewName);
 
 					// Send data to the server
 					RequestBuilder builder = new RequestBuilder(
 						RequestBuilder.POST, getViewWriteUrl());
 					builder.setHeader("Content-type",
 						"application/x-www-form-urlencoded");
 					try {
 						builder.sendRequest(URL.encode("name=" +
 							viewName + "&data=" +
 							new JSONObject(view).toString()),
 							new EmptyRequestCallback());
 						// TODO: Check the return value
 					} catch (RequestException e) {
 						// Nothing to do here
 					}
 
 					// Hide the popup
 					ViewSavePopup.this.hide();
 				}
 			});
 
 			viewNamesControl = new ListBox();
 			viewNamesControl.addChangeHandler(new ChangeHandler() {
 				@Override
 				public void onChange(ChangeEvent event) {
 					int selectedIndex = viewNamesControl.getSelectedIndex();
 					String selectedValue =
 						viewNamesControl.getValue(selectedIndex);
 
 					saveName.setText(selectedValue);
 					saveName.selectAll();
 				}
 			});
 
 			// Add all the controls to content - see the
 			// comments next to the declaration of content
 			// for the layout
 			HorizontalPanel topMatter = new HorizontalPanel();
 			topMatter.add(saveName);
 			topMatter.add(save);
 
 			content = new VerticalPanel();
 			content.add(topMatter);
 			// We will add viewNamesList whenever we create it
 
 			setWidget(content);
 			addStyleName(POPUP_CLASS_NAME);
 
 			retrieveViewNames(getViewNamesUrl(), viewNames,
 				new Alertable<Object>() {
 					@Override
 					public void onFailure(Object message) {	}
 
 					@Override
 					public void onSuccess(Object message) { showViewNames(); }
 				});
 		}
 
 		/**
 		 * Puts together the URL at which we can write data back to the
 		 * server, adding or changing a view.
 		 *
 		 * @return
 		 * 		the URL used to set views on the server
 		 */
 		private String getViewWriteUrl() {
 			return "/users/" + userId + "/views/set.json";
 		}
 
 		/**
 		 * Fills and shows the viewNamesControl widget.
 		 *
 		 * <p>Uses the viewNames private variable to get the list of
 		 * current views, and then adds all those names to
 		 * viewNamesControl.</p>
 		 */
 		private void showViewNames() {
 			int numViews = viewNames.size();
 			if (numViews == 0)
 				return;
 
 			for (String name: viewNames)
 				viewNamesControl.addItem(name);
 
 			if (numViews == 1)
 				viewNamesControl.setVisibleItemCount(2);
 			else
 				viewNamesControl.setVisibleItemCount(
 					Math.min(numViews, MAX_VISIBLE_VIEW_NAMES));
 
 			content.add(viewNamesControl);
 		}
 
 		/**
 		 * Does exactly the same thing that setVisible does for any other
 		 * popup, along with handling highlighting in the text box
 		 * for this popup window.
 		 */
 		@Override
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
 
 			// Only do something different from the default
 			// when we become visible
 			if (! visible)
 				return;
 
 			// Funny ordering because saveName.selectAll() only
 			// works when saveName is attached to the document
 			// and not hidden
 			if (this.currentView != null) {
 				saveName.setText(currentView);
 				saveName.selectAll();
 			}
 
 			// Make sure saveName gets the focus
 			saveName.setFocus(true);
 			// TODO: Maybe add some kind of keyboard listener that fires
 			// a click event on the save button whenever Enter is pressed
 		}
 	}
 
 	/**
 	 * Returns the URL that should be used to retrieve the set of view
 	 * names.
 	 *
 	 * @return
 	 * 		the URL from which we can get the list of view names
 	 */
 	private String getViewNamesUrl() {
 		return "/users/" + userId + "/views.json";
 	}
 
 	/**
 	 * Fills the viewNames parameter with a list of view names retrieved
 	 * from url.
 	 *
 	 * @param url
 	 * 		the URL at which we can get the view names
 	 * @param viewNames
 	 * 		the list that will be filled with the view names
 	 * @param callback
 	 * 		an object that will be alerted whenever results come back.
 	 * 		If the asynchronous request succeeds, calls
 	 * 		<code>callback.onSuccess(null)</code>.  However, if the
 	 * 		request fails, calls <code>callback.onFailure(null)</code>.
 	 */
 	// TODO: Add more useful parameters to the callback calls?
 	private static void retrieveViewNames(String url,
 			final List<String> viewNames,
 			final Alertable<Object> callback) {
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 
 		try {
 			builder.sendRequest(null, new RequestCallback() {
 				@Override
 				public void onError(Request request,
 						Throwable exception) {
 					callback.onFailure(null);
 				}
 
 				@Override
 				public void onResponseReceived(Request request,
 						Response response) {
 					if (GrapherTile.isSuccessful(response)) {
 						fillViewNames(viewNames, response.getText());
 						callback.onSuccess(null);
 					} else
 						callback.onFailure(null);
 				}
 			});
 		} catch (RequestException e) {
 			callback.onFailure(null);
 		}
 	}
 
 	/**
 	 * Fills the viewNames list with the set of view names.
 	 *
 	 * <p>This is intended only as a helper method for
 	 * {@link #retrieveViewNames(String, List, Alertable)}.</p>
 	 *
 	 * @param viewNames
 	 * 		the list to be filled
 	 * @param responseBody
 	 * 		the body of the response to our request for view
 	 * 		names
 	 */
 	private static void fillViewNames(List<String> viewNames,
 			String responseBody) {
 		JSONValue response = JSONParser.parse(responseBody);
 		JSONArray views = response.isArray();
 		if (views == null)
 			return;
 
 		for (int i = 0; i < views.size(); i++) {
 			JSONValue viewValue = views.get(i);
 			JSONObject viewObject = viewValue.isObject();
 			if (viewObject != null) {
 				// Success - this should always happen
 				JSONValue nameValue = viewObject.get("name");
 				if (nameValue == null)
 					continue;
 				JSONString nameString = nameValue.isString();
 				if (nameString == null)
 					continue;
				viewNames.add(nameString.stringValue());
 			} else {
 				// Also accept an array of strings coming from
 				// the server, even though this isn't part of
 				// the API
 				JSONString viewString = viewValue.isString();
 				if (viewString != null)
 					viewNames.add(viewString.stringValue());
 			}
 		}
 	}
 
 	/**
 	 * A class that represents an empty callback on a request.  Does
 	 * nothing, regardless of circumstances, so is useful for
 	 * situations in which we don't care about the server's return value.
 	 */
 	private static class EmptyRequestCallback implements RequestCallback {
 		@Override
 		public void onError(Request request, Throwable exception) { }
 
 		@Override
 		public void onResponseReceived(Request request,
 				Response response) { }
 	}
 
 	/**
 	 * Gives the user a way to restore to a particular view, selected
 	 * from a list.
 	 */
 	private class ViewRestorePopup extends PopupPanel {
 		// TODO: Possibly use the current view name to stop
 		// that name from appearing in the list of views
 		private final List<String> viewNames;
 
 		private final ScrollPanel content;
 		// content will contain nothing until the set of view names
 		// loads
 
 		private final Grid viewNamesControl;
 
 		/**
 		 * Creates a new <tt>ViewRestorePopup</tt> object but does not show it.
 		 */
 		public ViewRestorePopup() {
 			super(true, true);
 
 			viewNames = new ArrayList<String>();
 			viewNamesControl = new Grid(10, 3);
 			content = new ScrollPanel();
			// TODO: Set a max height on content
 
 			setWidget(content);
 			addStyleName(POPUP_CLASS_NAME);
 
 			retrieveViewNames(getViewNamesUrl(), viewNames,
 				new Alertable<Object>() {
 					@Override
 					public void onFailure(Object message) {	}
 
 					@Override
 					public void onSuccess(Object message) { showViewNames(); }
 				});
 		}
 
 		/**
 		 * Fills and shows the viewNamesControl widget.
 		 *
 		 * <p>Uses the viewNames private variable to get the list of
 		 * current views, and then adds all those names to
 		 * viewNamesControl.</p>
 		 */
 		// TODO: Combine this with ViewSavePopup.showViewNames, which
 		// does something pretty similar
 		private void showViewNames() {
 			int numViews = viewNames.size();
 			if (numViews == 0)
 				return;
 
 			viewNamesControl.resizeRows(numViews);
 
 			for (int i = 0; i < numViews; i++) {
 				String name = viewNames.get(i);
 
 				viewNamesControl.setWidget(i, 0, new Anchor(name));
 				viewNamesControl.setWidget(i, 1, new Anchor("Channels Only"));
 				viewNamesControl.setWidget(i, 2, new Anchor("Time Only"));
 				// TODO: Event handlers on all three links
 			}
 
 			content.add(viewNamesControl);
 		}
 
 		// TODO: Maybe override setVisible to set invisible when we
 		// don't have a list of view names?
 	}
 }
