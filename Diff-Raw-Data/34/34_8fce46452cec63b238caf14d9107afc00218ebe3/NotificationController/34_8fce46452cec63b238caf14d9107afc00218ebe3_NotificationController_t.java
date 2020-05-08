 package com.fornacif.osgi.manager.internal.controllers;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 
 import javafx.animation.PauseTransition;
 import javafx.animation.SequentialTransition;
 import javafx.animation.TranslateTransition;
 import javafx.application.Platform;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Label;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.layout.Pane;
 import javafx.scene.layout.VBox;
 import javafx.util.Duration;
 
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import aQute.bnd.annotation.component.Component;
 import aQute.bnd.annotation.component.ConfigurationPolicy;
 
 import com.fornacif.osgi.manager.internal.events.NotificationEvent;
 
 @Component(name = "NotificationController", provide = { Pane.class, EventHandler.class }, configurationPolicy = ConfigurationPolicy.require)
 public class NotificationController extends VBox implements EventHandler, Initializable {
 
 	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
 
 	private final BlockingQueue<NotificationEvent> queue = new LinkedBlockingQueue<>();
 
 	private final Semaphore semaphore = new Semaphore(1);
 
 	@FXML
 	private Label notificationLabel;
 
 	@FXML
 	private ImageView image;
 
 	private SequentialTransition sequence;
 
 	@Override
 	public void initialize(URL url, ResourceBundle resourceBundle) {
 		TranslateTransition showNotification = new TranslateTransition(Duration.millis(500), this);
 		showNotification.setFromY(30.0);
 		showNotification.setToY(0.0);
 
 		TranslateTransition hideNotification = new TranslateTransition(Duration.millis(500), this);
 		hideNotification.setFromY(0.0);
 		hideNotification.setToY(30.0);
 
 		sequence = new SequentialTransition(showNotification, new PauseTransition(Duration.seconds(3)), hideNotification);
 
 		sequence.setOnFinished(new javafx.event.EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent arg0) {
 				setManaged(false);
 				setVisible(false);
 				semaphore.release();
 			}
 		});
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while (true) {
 					try {
 						final NotificationEvent event = queue.take();
 						semaphore.acquire();
 
 						Platform.runLater(new Runnable() {
 							@Override
 							public void run() {
								ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
								try {
									Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
									switch (event.getLevel()) {
									case ERROR:
										image.setImage(new Image("/icons/error-16x16.png"));
										break;
									case INFO:
										image.setImage(new Image("/icons/info-16x16.png"));
										break;
									}
								} finally {
									Thread.currentThread().setContextClassLoader(threadContextClassLoader);
 								}
 
 								notificationLabel.setText(event.getMessage());
 
 								setManaged(true);
 								setVisible(true);
 
 								sequence.play();
 							}
 						});
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 				}
 			}
 		}, "Notification Thread").start();
 	}
 
 	@Override
 	public void handleEvent(final Event event) {
 		try {
 			queue.put((NotificationEvent) event);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		LOGGER.debug("Event {} received", event.getTopic());
 	}
 
 }
