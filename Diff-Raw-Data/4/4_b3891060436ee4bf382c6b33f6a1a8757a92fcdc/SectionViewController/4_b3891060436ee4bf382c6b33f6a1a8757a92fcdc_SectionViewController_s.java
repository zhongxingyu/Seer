 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.controllers;
 
 import adg.red.models.Enrolment;
 import adg.red.models.EnrolmentPK;
 import adg.red.models.Section;
 import adg.red.models.Student;
 import adg.red.utils.Context;
 import adg.red.utils.LocaleManager;
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListView;
 import javafx.scene.control.TextArea;
 
 /**
  * FXML Controller class
  * <p/>
  * @author Witt
  */
 public class SectionViewController implements Initializable
 {
 
     @FXML //  fx:id="courseDesTxt"
     private TextArea courseDesTxt; // Value injected by FXMLLoader
     @FXML //  fx:id="courseNameLbl"
     private Label courseNameLbl; // Value injected by FXMLLoader
     @FXML //  fx:id="secLbl"
     private Label secLbl; // Value injected by FXMLLoader
     @FXML //  fx:id="creditLbl"
     private Label creditLbl; // Value injected by FXMLLoader
     @FXML //  fx:id="deptIdAndCourseNoLbl"
     private Label deptIdAndCourseNoLbl; // Value injected by FXMLLoader
     @FXML //  fx:id="gradingSchmLbl"
     private Label gradingSchmLbl; // Value injected by FXMLLoader
     @FXML //  fx:id="passRqLbl"
     private Label passRqLbl; // Value injected by FXMLLoader
     @FXML //  fx:id="dateLv"
     private ListView<String> dateLv; // Value injected by FXMLLoader
     @FXML //  fx:id="btnRegister"
     private Button btnRegister; // Value injected by FXMLLoader
     @FXML //  fx:id="btnDrop"
     private Button btnDrop; // Value injected by FXMLLoader
     @FXML //  fx:id="lblResponse"
     private Label lblResponse; // Value injected by FXMLLoader
     private Enrolment enrolment = null;
     private EnrolmentPK enrolmentPk;
 
     private void toggleRegDropButtons()
     {
         //check to see if the student has already dropped the section
         if (enrolment.getIsActive())
         {
             btnRegister.setDisable(true);
             btnDrop.setDisable(false);
         }
         else
         {
             btnRegister.setDisable(false);
             btnDrop.setDisable(true);
         }
     }
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
         // TODO
         try
         {
             Section section = Context.getInstance().getSelectedSection();
             enrolmentPk = new EnrolmentPK(Student.getStudentByUsername(Context.getInstance().getCurrentUser()).getStudentId(),
                     section.getSectionId(),
                     section.getCourse().getCoursePK().getCourseNumber(),
                     section.getCourse().getCoursePK().getDepartmentId(),
                     section.getTerm().getTermPK().getTermYear(),
                     section.getTerm().getTermPK().getSessionId(),
                     Context.getInstance().getSelectedSection().getSectionType().getSectionTypeId());
 
             if (checkUserAlreadyEnrolled(enrolmentPk))
             {
                 toggleRegDropButtons();
             }
         }
         catch (Exception ex)
         {
             Logger.getLogger(SectionViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
 
        secLbl.setText("Secion " + Context.getInstance().getSelectedSection().getSectionPK().getSectionId());
        creditLbl.setText("" + Context.getInstance().getSelectedCourse().getCredits());
         passRqLbl.setText(Context.getInstance().getSelectedCourse().getPassingRequirement());
         courseDesTxt.setText(Context.getInstance().getSelectedCourse().getDescription());
         courseNameLbl.setText(Context.getInstance().getSelectedCourse().getName());
         deptIdAndCourseNoLbl.setText(Context.getInstance().getSelectedCourse().getDepartmentIdAndCourseNumber());
         gradingSchmLbl.setText(Context.getInstance().getSelectedCourse().getGradingSchemeId().getName());
 
         // setOnAction when register button is pressed
         btnRegister.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 try
                 {
 
                     if (enrolment == null)
                     {
                         //first time
                         enrolment = new Enrolment(enrolmentPk);
                     }
                     enrolment.setIsActive(true);
                     enrolment.save();
                     lblResponse.setText(LocaleManager.get(10));
                     lblResponse.setVisible(true);
                     toggleRegDropButtons();
                 }
                 catch (Exception ex)
                 {
                     Logger.getLogger(SectionViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
         });
 
         // setOnAction when drop button is pressed
         //should fire only when we have an enrolment selected
         btnDrop.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 try
                 {
                     enrolment.setIsActive(false);
                     enrolment.save();
                     lblResponse.setText(LocaleManager.get(32));
                     lblResponse.setVisible(true);
                     toggleRegDropButtons();
                 }
                 catch (Exception ex)
                 {
                     Logger.getLogger(SectionViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
     }
 
     private boolean checkUserAlreadyEnrolled(EnrolmentPK enrolPk)
     {
         try
         {
             enrolment = Enrolment.getEnrolmentByEnrolmentPK(enrolPk);
             return true;
         }
         catch (Exception ex)
         {
             enrolment = null;
             return false;
         }
     }
 
     private void populateList()
     {
 
         dateLv.setItems(null);
     }
 }
