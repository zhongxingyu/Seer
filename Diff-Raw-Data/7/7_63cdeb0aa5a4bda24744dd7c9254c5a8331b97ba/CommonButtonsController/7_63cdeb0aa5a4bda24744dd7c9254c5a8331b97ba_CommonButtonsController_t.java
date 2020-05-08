 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.controllers;
 
 import adg.red.controllers.student.HomeViewController;
 import adg.red.utils.Context;
 import adg.red.utils.LocaleManager;
 import adg.red.utils.ViewLoader;
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 
 /**
  * FXML Controller class
  * <p/>
  * @author harsimran.maan
  */
 public class CommonButtonsController implements Initializable
 {
 
     @FXML
     private Button btnLogout;
     @FXML
     private Button btnUserProfile;
     @FXML
     private Button btnFaq;
     @FXML
     private Button btnGlossary;
     @FXML
     private Button btnMessage;
 
     private void initializeComponentsByLocale()
     {
         btnUserProfile.setText(LocaleManager.get(21));
         btnMessage.setText(LocaleManager.get(22));
         btnGlossary.setText(LocaleManager.get(14));
         btnFaq.setText(LocaleManager.get(16));
         btnLogout.setText(LocaleManager.get(8));
     }
 
     public void logout(ActionEvent event)
     {
         try
         {
             ViewLoader view = new ViewLoader(Context.getInstance().getHomeView());
             Context.getInstance().setWasLoggedIn(true);
            Context.getInstance().getSearchView().setVisible(false);
             view.loadView("Login");
         }
         catch (Exception ex)
         {
             Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void message(ActionEvent event)
     {
         try
         {
             ViewLoader view = new ViewLoader(Context.getInstance().getDisplayView());
             view.loadView("Message");
 
         }
         catch (Exception ex)
         {
             Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public void userProfile(ActionEvent event)
     {
         try
         {
             ViewLoader view = new ViewLoader(Context.getInstance().getDisplayView());
             view.loadView("UserProfile");
         }
         catch (Exception ex)
         {
             Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public void glossary(ActionEvent event)
     {
         try
         {
             ViewLoader view = new ViewLoader(Context.getInstance().getDisplayView());
             view.loadView("Glossary");
         }
         catch (Exception ex)
         {
             Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public void faq(ActionEvent event)
     {
         try
         {
             ViewLoader view = new ViewLoader(Context.getInstance().getDisplayView());
             view.loadView("FaqView");
         }
         catch (Exception ex)
         {
             Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
         initializeComponentsByLocale();
     }
 }
