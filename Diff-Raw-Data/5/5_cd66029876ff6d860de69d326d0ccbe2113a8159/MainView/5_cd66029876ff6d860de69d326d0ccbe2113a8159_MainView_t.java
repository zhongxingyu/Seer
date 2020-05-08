 package org.accesointeligente.client.views;
 
 import org.accesointeligente.client.presenters.MainPresenter;
 import org.accesointeligente.client.presenters.MainPresenterIface;
 import org.accesointeligente.shared.AppPlace;
 import org.accesointeligente.shared.NotificationEventParams;
 import org.accesointeligente.shared.RequestListType;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.*;
 
 public class MainView extends Composite implements MainPresenter.Display {
 	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);
 	interface MainViewUiBinder extends UiBinder<Widget, MainView> {}
 
 	public enum DisplayMode {
 		LoggedIn,
 		LoggedOut,
 		LoginPending
 	}
 
 	@UiField HTMLPanel headerPanel;
 	@UiField Image logo;
 	@UiField Label welcomeMessage;
 	@UiField HTMLPanel notificationPanel;
 	@UiField Label notificationLabel;
 	@UiField Label notificationClose;
 	@UiField FlowPanel mainPanel;
 	@UiField MenuItem myMenu;
 	@UiField MenuItem myrequests;
 	@UiField MenuItem favorites;
 	@UiField MenuItem logout;
 	@UiField MenuItem home;
 	@UiField MenuItem about;
 	@UiField MenuItem contact;
 	@UiField HTMLPanel footerPanel;
 	@UiField Label loginPending;
 
 	Timer notificationTimer;
 	private MainPresenterIface presenter;
 
 	public MainView() {
 		initWidget(uiBinder.createAndBindUi(this));
 
 		myrequests.setCommand(new Command() {
 			@Override
 			public void execute() {
 				History.newItem(AppPlace.LIST.getToken() + "?type=" + RequestListType.MYREQUESTS.getType());
 			}
 		});
 
 		favorites.setCommand(new Command() {
 			@Override
 			public void execute() {
 				History.newItem(AppPlace.LIST.getToken() + "?type=" + RequestListType.FAVORITES.getType());
 			}
 		});
 
 		logout.setCommand(new Command() {
 			@Override
 			public void execute() {
 				History.newItem(AppPlace.LOGOUT.getToken());
 			}
 		});
 
 		home.setCommand(new Command() {
 			@Override
 			public void execute() {
 				History.newItem(AppPlace.HOME.getToken());
 			}
 		});
 
 		about.setCommand(new Command() {
 			@Override
 			public void execute() {
 				History.newItem(AppPlace.ABOUT.getToken());
 			}
 		});
 
 		contact.setCommand(new Command() {
 			@Override
 			public void execute() {
 				History.newItem(AppPlace.CONTACT.getToken());
 			}
 		});
 	}
 
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 
 	@Override
 	public void setPresenter(MainPresenterIface presenter) {
 		this.presenter = presenter;
 	}
 
 	@Override
 	public void setDisplayMode(DisplayMode mode) {
 		headerPanel.setVisible(DisplayMode.LoggedIn.equals(mode) || DisplayMode.LoggedOut.equals(mode));
 		welcomeMessage.setVisible(DisplayMode.LoggedIn.equals(mode));
 		mainPanel.setVisible(DisplayMode.LoggedIn.equals(mode) || DisplayMode.LoggedOut.equals(mode));
 		myrequests.setVisible(DisplayMode.LoggedIn.equals(mode));
 		favorites.setVisible(DisplayMode.LoggedIn.equals(mode));
 		logout.setVisible(DisplayMode.LoggedIn.equals(mode));
 		footerPanel.setVisible(DisplayMode.LoggedIn.equals(mode) || DisplayMode.LoggedOut.equals(mode));
 		loginPending.setVisible(DisplayMode.LoginPending.equals(mode));
 
 		if (DisplayMode.LoggedIn.equals(mode)) {
 			myMenu.setText("Mi cuenta");
 			myMenu.setCommand(null);
 		} else {
 			myMenu.setText("Ingresar");
 			myMenu.setCommand(new Command() {
 				@Override
 				public void execute() {
 						History.newItem(AppPlace.LOGIN.getToken());
 				}
 			});
 		}
 	}
 
 	@Override
 	public FlowPanel getLayout() {
 		return mainPanel;
 	}
 
 	@Override
 	public void setWelcomeMessage(String message) {
 		welcomeMessage.setText(message);
 	}
 
 	@Override
 	public void setNotificationMessage(NotificationEventParams params) {
 		notificationLabel.setText(params.getMessage());
		notificationPanel.setStyleName(params.getType().getType());
		notificationPanel.setVisible(true);
 		notificationTimer = new Timer() {
 
 			@Override
 			public void run() {
 				notificationPanel.setVisible(false);
 			}
 		};
 		notificationTimer.schedule(15000);
 	}
 
 	@UiHandler("notificationClose")
 	public void onNotificationCloseClick(ClickEvent event) {
 		notificationPanel.setVisible(false);
 		notificationTimer.cancel();
 	}
 
 	@UiHandler("logo")
 	public void onLogoClick(ClickEvent event) {
 		History.newItem(AppPlace.HOME.getToken());
 	}
 }
