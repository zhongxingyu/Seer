 /*
  *
  *
  */
 package adg.red.controllers;
 
 import adg.red.controls.MessageStyleManager;
 import adg.red.models.User;
 import adg.red.session.Context;
 import adg.red.locale.LocaleManager;
 import adg.red.models.skeleton.ILocalizable;
 import adg.red.utils.ViewLoader;
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.PasswordField;
 
 /**
  * FXML Controller class for ChangePassword.fxml
  * <p/>
  * @author harsimran.maan
  */
 public class ChangePasswordController implements Initializable, ILocalizable
 {
 
     @FXML
     private PasswordField txtOldPassword;
     @FXML
     private PasswordField txtNewPassword;
     @FXML
     private PasswordField txtReNewPwd;
     @FXML
     private Button btnSavePassword;
     @FXML
     private Button btnClear;
     @FXML
     private Label lblError;
     private User currentUser;
     @FXML
     private Label lblOldPwd;
     @FXML
     private Label lblNewPwd;
     @FXML
     private Label lblReNew;
 
     @FXML
     private void changePassword(ActionEvent event)
     {
         String oldPwd = txtOldPassword.getText();
         String newPwd = txtNewPassword.getText();
         String pwdRe = txtReNewPwd.getText();
         String pwdErrorMsg;
         try
         {
 
 
             if (newPwd.equals(pwdRe))
             {
                 final boolean isReset = Context.getInstance().isReset();
                 //check if old password is valid
                 if (!isReset)
                 {
 
                     User.login(currentUser.getUsername(), oldPwd);
                 }
                 currentUser.setPassword(newPwd);
                 currentUser.save();
                 MessageStyleManager.setSuccess(lblError);
                 if (isReset)
                 {
                     btnSavePassword.setDisable(true);
                     btnClear.setText(LocaleManager.get(1));
                     btnClear.setOnAction(new EventHandler<ActionEvent>()
                     {
                         @Override
                         public void handle(ActionEvent t)
                         {
                             ViewLoader view = new ViewLoader(Context.getInstance().getMainView());
                             view.loadView("Login");
                         }
                     });
                 }
 
                 pwdErrorMsg = LocaleManager.get(11);
             }
             else
             {
                 MessageStyleManager.setError(lblError);
                 pwdErrorMsg = LocaleManager.get(12);
 
             }
             showPasswordMessage(pwdErrorMsg);
         }
         catch (Exception ex)
         {
             pwdErrorMsg = LocaleManager.get(13);
             MessageStyleManager.setError(lblError);
             showPasswordMessage(pwdErrorMsg);
         }
     }
 
     /**
      * display password error messages
      * <p/>
      * @param pwdErrorMsg error message
      */
     private void showPasswordMessage(String pwdErrorMsg)
     {
         lblError.setVisible(true);
         lblError.setText(pwdErrorMsg);
     }
 
     /**
      * Clear old password, new password, re-enter password fields
      * <p/>
      * @param event user action
      */
     @FXML
     private void clear(ActionEvent event)
     {
         txtOldPassword.setText("");
         txtNewPassword.setText("");
         txtReNewPwd.setText("");
         lblError.setVisible(false);
     }
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
         final Context context = Context.getInstance();
         currentUser = context.getCurrentUser();
         if (context.isReset())
         {
             txtOldPassword.setDisable(true);
         }
         localize();
     }
 
     @Override
     public void localize()
     {
        lblOldPwd.setText(LocaleManager.get(119) + ":");
        lblNewPwd.setText(LocaleManager.get(120) + ":");
        lblReNew.setText(LocaleManager.get(121) + ":");
         btnSavePassword.setText(LocaleManager.get(54));
         btnClear.setText(LocaleManager.get(55));
     }
 }
