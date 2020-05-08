 package org.smartsnip.client;
 
 import java.util.List;
 
 import org.smartsnip.shared.ISession;
 import org.smartsnip.shared.ISnippet;
 import org.smartsnip.shared.XNotification;
 import org.smartsnip.shared.XSnippet;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * The area for managing notifications
  * 
  * @author Felix Niederwanger
  * 
  */
 public class NotificationsArea extends Composite {
 
 	/** Root panel for the notifications */
 	private final Panel rootPanel = new VerticalPanel();
 
 	/** Show only unread items */
 	private boolean unreadOnly = true;
 
 	/* Components */
 
 	private final Label lblTitle = new Label("Notifications");
 	private final Label lblStatus = new Label("");
 	private final ScrollPanel scrollPanel = new ScrollPanel();
 	private final HorizontalPanel pnlToolbar = new HorizontalPanel();
 	private final Button btnRefresh = new Button("Refresh");
 	private final Button btnMarkAllRead = new Button("Mark all read");
 	private final Button btnUnreadOnly = new Button("Unread only");
 
 	/* End of components */
 
 	/** Initialises a new {@link NotificationsArea} */
 	public NotificationsArea() {
 		btnRefresh.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				update();
 			}
 		});
 		btnMarkAllRead.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				markAllRead();
 			}
 		});
 		btnUnreadOnly.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				unreadOnly = !unreadOnly;
 
 				// Set style
 				if (unreadOnly)
					btnUnreadOnly.setStyleName("enabledToggleButton");
 				else
					btnUnreadOnly.setStyleName("disabledToggleButton");
 
 				update();
 			}
 		});
 
 		rootPanel.add(lblTitle);
 		rootPanel.add(lblStatus);
 		rootPanel.add(pnlToolbar);
 
 		pnlToolbar.add(btnRefresh);
 		pnlToolbar.add(btnMarkAllRead);
 		pnlToolbar.add(btnUnreadOnly);
 
 		rootPanel.add(scrollPanel);
 		scrollPanel.setHeight("100%");
 
 		initWidget(rootPanel);
 		applyStyles();
 
 		update();
 	}
 
 	private void applyStyles() {
 		lblTitle.setStyleName("h3");

		btnRefresh.setStyleName("enabledToggleButton");
		btnMarkAllRead.setStyleName("enabledToggleButton");
 	}
 
 	/** Updates the component */
 	public void update() {
 		lblStatus.setText("Getting notifications ... ");
 		disableControls();
 		scrollPanel.clear();
 
 		ISession.Util.getInstance().getNotifications(unreadOnly, new AsyncCallback<List<XNotification>>() {
 
 			@Override
 			public void onSuccess(List<XNotification> result) {
 				if (result == null) {
 					onFailure(new IllegalArgumentException("Null returned"));
 					return;
 				}
 
 				for (XNotification notification : result)
 					scrollPanel.add(createNotificationPanel(notification));
 
 				lblStatus.setText("Showing " + result.size() + (unreadOnly ? " unread" : "") + " notifications");
 				enableControls();
 
 			}
 
 			/** Create the panel for a notification */
 			private Panel createNotificationPanel(final XNotification notification) {
 				if (notification == null)
 					return null;
 
 				final Panel result = new HorizontalPanel();
 
 				final Label lblMessage = new Label(notification.message);
 				final Label lblSource = new Label(notification.source);
 				final Label lblTime = new Label(notification.time);
 				final Anchor anchSnippet;
 				final Button btnMarkRead = new Button(notification.read ? "Mark unread" : "Mark read");
 
 				if (notification.refersToSnippet == null) {
 					anchSnippet = new Anchor();
 					anchSnippet.setVisible(false);
 				} else {
 					anchSnippet = new Anchor("Go to snippet");
 					anchSnippet.addClickHandler(new ClickHandler() {
 
 						@Override
 						public void onClick(ClickEvent event) {
 							anchSnippet.setEnabled(false);
 							anchSnippet.setText("Fetching snippet ... ");
 							ISnippet.Util.getInstance().getSnippet(notification.refersToSnippet, new AsyncCallback<XSnippet>() {
 
 								@Override
 								public void onFailure(Throwable caught) {
 									// Error occured. Offer retry
 									anchSnippet.setText("Go to snippet (retry)");
 									anchSnippet.setEnabled(true);
 								}
 
 								@Override
 								public void onSuccess(XSnippet result) {
 									if (result == null) {
 										onFailure(new IllegalArgumentException("Null returned"));
 										return;
 									}
 
 									Control.getInstance().changeToSnipPage(result);
 								}
 							});
 						}
 					});
 				} // End creation of anchSnippet
 				btnMarkRead.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						btnMarkRead.setEnabled(false);
 
 						if (notification.read) {
 							btnMarkRead.setText("Marking unread ... ");
 						} else {
 							btnMarkRead.setText("Marking read ... ");
 						}
 						ISession.Util.getInstance().markNotificationRead(notification.id, !notification.read, new AsyncCallback<Void>() {
 
 							@Override
 							public void onFailure(Throwable caught) {
 								refreshButton();
 							}
 
 							@Override
 							public void onSuccess(Void result) {
 								notification.read = !notification.read;
 								refreshButton();
 							}
 
 							private void refreshButton() {
 								if (notification.read)
 									btnMarkRead.setText("Mark unread");
 								else
 									btnMarkRead.setText("Mark read");
 								btnMarkRead.setEnabled(true);
 							}
 						});
 					}
 				});
 
 				result.add(lblMessage);
 				result.add(lblSource);
 				result.add(lblTime);
 				result.add(anchSnippet);
 				result.add(btnMarkRead);
 
 				return result;
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				// TODO: Make this pretty
 				lblStatus.setText("Getting notifications failed: " + caught.getMessage());
 				enableControls();
 			}
 		});
 	}
 
 	/** Mark all notifications read */
 	private void markAllRead() {
 		disableControls();
 		lblStatus.setText("Marking all read ... ");
 		ISession.Util.getInstance().markAllNotificationsRead(new AsyncCallback<Void>() {
 
 			@Override
 			public void onSuccess(Void result) {
 				update();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				lblStatus.setText("Error marking all read: " + caught.getMessage());
 				enableControls();
 			}
 		});
 	}
 
 	/** Re-enabled the controls */
 	private void enableControls() {
 		btnRefresh.setEnabled(true);
 		btnMarkAllRead.setEnabled(true);
 		btnUnreadOnly.setEnabled(true);
 	}
 
 	/** Disabled the controls */
 	private void disableControls() {
 		btnRefresh.setEnabled(false);
 		btnMarkAllRead.setEnabled(false);
 		btnUnreadOnly.setEnabled(false);
 	}
 }
