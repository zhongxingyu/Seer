 /*
  * Copyright (c) 2012 Spout LLC <http://www.spout.org>
  * All Rights Reserved, unless otherwise granted permission.
  *
  * You may use and modify for private use, fork the official repository
  * for contribution purposes, contribute code, and reuse your own code.
  */
 package org.spout.platform;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import com.cathive.fx.guice.GuiceApplication;
 import com.cathive.fx.guice.GuiceFXMLLoader;
 import com.cathive.fx.guice.GuiceFXMLLoader.Result;
 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 import com.google.inject.name.Names;
 import com.narrowtux.fxdecorate.FxDecorateScene;
 import javafx.application.Platform;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.image.Image;
 import javafx.scene.text.Font;
 import javafx.stage.Stage;
 import javafx.stage.StageStyle;
 import org.jivesoftware.smack.Connection;
 
 import org.spout.platform.chat.manager.ChatManager;
 import org.spout.platform.chat.manager.XmppChatManager;
 import org.spout.platform.controller.ApplicationController;
 import org.spout.platform.controller.NotificationController;
 import org.spout.platform.gui.Views;
 import org.spout.platform.services.PropertyManager;
 import org.spout.platform.services.impl.SimplePropertyManager;
 import org.spout.platform.util.OperatingSystem;
 
 public class Launcher extends GuiceApplication {
 	public static final int MIN_WIDTH = 800;
 	public static final int MIN_HEIGHT = 600;
 
 	static {
 		if (OperatingSystem.MAC_OSX.equals(OperatingSystem.getOS())) {
 			// Should be disabled in production! Will produce funny warnings in console.
 			System.setProperty("javafx.macosx.embedded", "true");
 			java.awt.Toolkit.getDefaultToolkit();
 		}
 	}
 
 	@Inject
 	private GuiceFXMLLoader fxmlLoader;
 	private ApplicationController applicationController;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		launch(args);
 	}
 
 	@Override
 	public void start(Stage mainStage) throws Exception {
 		loadFonts();
 
 		Connection.DEBUG_ENABLED = true; // Enable XMPP / Smack API Debugging, for development only!
 		System.setProperty("java.awt.headless", "false"); // Mac OS X workaround for Smack debugging and JavaFX (Swing + JavaFX issue).
 
 		mainStage.initStyle(StageStyle.UNDECORATED);
 		mainStage.setTitle("Spout Platform");
 		mainStage.setMinHeight(MIN_HEIGHT);
 		mainStage.setMinWidth(MIN_WIDTH);
 		mainStage.setWidth(MIN_WIDTH);
 		mainStage.setHeight(MIN_HEIGHT);
 		mainStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/org/spout/platform/resources/spout.png")));
 		Result appViewResult = fxmlLoader.load(getClass().getResource(Views.APP_VIEW));
 		FxDecorateScene fxDecorateScene = new FxDecorateScene((Parent) appViewResult.getRoot(), mainStage);
 		fxDecorateScene.setEdgeSize(5);
 		mainStage.setScene(fxDecorateScene);
 		applicationController = appViewResult.getController();
 		applicationController.setDecorateScene(fxDecorateScene);
 		mainStage.show();
 		fxDecorateScene.getController().centerOnScreen();
 
 		Result notificationViewResult = fxmlLoader.load(getClass().getResource(Views.NOTIFICATION_VIEW));
 		Stage notificationStage = new Stage(StageStyle.TRANSPARENT);
 		notificationStage.setScene(new Scene((Parent) notificationViewResult.getRoot()));
 		NotificationController notificationController = notificationViewResult.getController();
 		notificationController.setStage(notificationStage);
 		notificationController.init();
 		applicationController.setNotificationController(notificationController);
 	}
 
 	@Override
 	public void stop() throws Exception {
 		Platform.runLater(new Runnable() {
 			@Override
 			public void run() {
 				applicationController.onQuit();
 			}
 		});
 	}
 
 	@Override
 	public void init(List<Module> modules) throws Exception {
 		modules.add(new AbstractModule() {
 			@Override
 			protected void configure() {
 				bind(ChatManager.class).to(XmppChatManager.class).in(Singleton.class);
 				bind(PropertyManager.class).to(SimplePropertyManager.class).in(Singleton.class);
 				Names.bindProperties(binder(), getProperties());
 			}
 
 			private Map<String, String> getProperties() {
 				Properties properties = new Properties();
 				try {
 					properties.load(Launcher.class.getResourceAsStream("/org/spout/platform/resources/config/config.properties"));
 				} catch (IOException e) {
 					throw new RuntimeException(e); // Configuration didn't load, no reason to continue execution.
 				}
 				return (Map) properties;
 			}
 		});
 	}
 
 	private static void loadFonts() {
		loadFont("/org/spout/platform/resources/font/awesome/fontawesome-webfont.ttf");
		loadFont("/org/spout/platform/resources/font/ubuntu/Ubuntu-L.ttf");
 	}
 
 	private static void loadFont(String location) {
 		Font.loadFont(Launcher.class.getResourceAsStream(location), 12);
 	}
 }
