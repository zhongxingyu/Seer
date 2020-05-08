 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ts.examples;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.TextArea;
 import javafx.scene.control.TextField;
 
 /**
  *
  * @author jpgough
  */
 public class FutureController implements Initializable {
     
     private FutureManager futureManager;
     
     public FutureController() {
         futureManager = new FutureManager();
     }
     
     @FXML
     private TextArea logTextArea;
     
     @FXML
     private TextField secondsTextField;
     
     @FXML
     private TextField futureMessageTextField;
     
     @FXML
     private void beginFuture(ActionEvent event) {
         appendToLogBox( "Begin Future Task in " + secondsTextField.getText() );
         futureManager.createFuture(Integer.valueOf(secondsTextField.getText()), futureMessageTextField.getText() );
     }
     
     @FXML
     private void cancelFuture() {
         futureManager.cancel( true );
         appendToLogBox( "Cancelled The Future" );
     }
             
     @FXML
     private void getFuture() throws Exception {
         appendToLogBox( futureManager.get() );
     }
                     
     @FXML
     private void isFutureCancellable() {
         
     }
                             
     @FXML
     private void isFutureDone() {
         appendToLogBox( Boolean.toString( futureManager.isDone() ) );
     }
     
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         // TODO
     }   
     
     private void appendToLogBox( String newMessage ) {
         logTextArea.appendText("\n" + newMessage );
     }
 }
