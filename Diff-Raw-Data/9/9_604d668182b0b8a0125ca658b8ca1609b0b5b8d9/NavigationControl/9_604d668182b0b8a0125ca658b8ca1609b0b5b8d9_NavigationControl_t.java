 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.zuehlke.zfb.control;
 
 import com.zuehlke.zfb.model.ModelLoader;
 import com.zuehlke.zfb.model.RootModel;
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.TextField;
 
 /**
  *
  * @author rlo
  */
 public class NavigationControl implements Initializable {
 
     private RootModel rootModel = RootModel.getInstance();
     
     @FXML
     private TextField currentUrl;
 
     public void changeDirectory() {
         this.rootModel.setCurrentDirectory("bla");
     }
 
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         currentUrl.textProperty().bindBidirectional(rootModel.currentDirectoryProperty());
    }  
     
 
 }
