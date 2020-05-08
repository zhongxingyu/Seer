 package be.artesis.timelog.externAuth;
 
 import be.artesis.timelog.controller.Inserter;
 import be.artesis.timelog.externAuth.*;
 import be.artesis.timelog.gui.*;
 import be.artesis.timelog.model.*;
 import be.artesis.timelog.view.*;
 import java.awt.Color;
 import java.awt.HeadlessException;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import org.json.JSONException;
 ////
 import java.awt.Dimension;
 import java.awt.Point;
 
 import javafx.application.Platform;
 import javafx.embed.swing.JFXPanel;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.web.WebEngine;
 import javafx.scene.web.WebView;
 import javax.swing.*;
 
 import javax.swing.JPanel;
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 
 public class AuthBrowser {
 	
 	private WebEngine webEngine;
 	private Group group;
 	private Scene scene;
 	private WebView webView;
 	private final int BROWSERWIDTH = 720;
 	private final int BROWSERHEIGHT = 490;
 	
 	private final String urlGoogle = "https://accounts.google.com/o/oauth2/auth?scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&state=%2Fprofile&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&client_id=536253651406.apps.googleusercontent.com";
     private final String urlFacebook = "https://www.facebook.com/dialog/oauth?client_id=346106655506499&redirect_uri=https://www.facebook.com/connect/login_success.html&scope=email&response_type=token&type=user_agent";
 	
 	public AuthBrowser() {
 		
 	}
 	
 	public void initBrowser(final LoginDialog loginDialog, final JFXPanel browserPanel, final String provider) {
         //this.pack();
         Platform.runLater(new Runnable() { // this will run initFX as JavaFX-Thread
             @Override
             public void run() {
                 startBrowser(loginDialog, browserPanel, provider);
             }
         });
     }
 	
 	private void startBrowser(final LoginDialog loginDialog, final JFXPanel BrowserPanel, String provider) {
         group = new Group();
         scene = new Scene(group);
         BrowserPanel.setScene(scene);
         webView = new WebView();
         group.getChildren().add(webView);
         //webView.setMinSize(720, 500);
         webView.setMaxSize(BROWSERWIDTH, BROWSERHEIGHT);
         webEngine = webView.getEngine();
         
         if(provider.equals("Facebook")) {
             webEngine.load(urlFacebook);
         }
         else if(provider.equals("Google")) {
             webEngine.load(urlGoogle);
         }
         
        /*webEngine.titleProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override 
                     public void run() {
                         String title = webEngine.getTitle();
                         if(title != null && title.startsWith("Success state=/profile&code=")) {
                             loginDialog.maakExterneGebruiker(title.substring(28), "Google");
                             exit();
                         }
                     }
                 });
             }
         });
         webEngine.locationProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override public void run() {
                         String url = webEngine.getLocation();
                         if(url != null && url.startsWith("https://www.facebook.com/connect/login_success.html")) {
                             loginDialog.maakExterneGebruiker(url.substring(65, 173), "Facebook");
                             exit(); 
                         }
                     }
                 });
             }
        });*/
     }
 	
 	private void exit() {
         Platform.runLater( new Runnable(){
             @Override 
             public void run(){
                 //System.err.println( "exit/runLater/run" );
                 webEngine.getLoadWorker().cancel();
                 Platform.exit();
                 SwingUtilities.invokeLater( new Runnable(){ 
                     @Override 
                     public void run() {
                         //System.err.println( "exit/invokeLater/run" );
                         //thisDialog.dispose();
                     	
                     	
                     }
                 });
             }
         });
     }
 
 }
