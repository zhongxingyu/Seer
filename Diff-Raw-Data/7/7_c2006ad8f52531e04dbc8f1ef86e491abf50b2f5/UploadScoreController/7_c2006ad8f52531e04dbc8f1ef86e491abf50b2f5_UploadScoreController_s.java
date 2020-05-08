 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.controllers.faculty;
 
 import adg.red.controllers.BreadCrumbController;
 import adg.red.models.Enrolment;
 import adg.red.models.EnrolmentPK;
 import adg.red.models.Section;
 import adg.red.models.Session;
 import adg.red.utils.Context;
 import adg.red.utils.LocaleManager;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListView;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableView;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.input.MouseEvent;
 import javafx.stage.FileChooser;
 import javafx.stage.Stage;
 
 /**
  * FXML Controller class
  * <p/>
  * @author harsimran.maan
  */
 public class UploadScoreController implements Initializable
 {
 
     @FXML
     private Button btnBrowseFile;
     @FXML
     private Button btnUpload;
     @FXML
     private Label lblDeptCoureNumber;
     @FXML
     private Label lblHeading2;
     @FXML
     private Label lblSecNumber;
     @FXML
    private Label lblResponse;
    @FXML
     private Label lblSection;
     @FXML
     private Label lblSession;
     @FXML
     private Label lblYear;
     @FXML
     private Label lblFile;
     @FXML
     private Label lblFilePath;
     @FXML
     private ListView<String> lsvResult;
     private File file;
     private ArrayList<String> data;
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
         data = new ArrayList();
         BreadCrumbController.renderBreadCrumb("faculty/HomeView|faculty/UploadScore");
         Context.getInstance().setTitle(LocaleManager.get(56));
         initializeComponentsByLocale();
         toggleLabels();
     }
 
     private void initializeComponentsByLocale()
     {
         btnBrowseFile.setText(LocaleManager.get(70));
         btnUpload.setText(LocaleManager.get(71));
 
         lblHeading2.setText(LocaleManager.get(73) + ":");
         lblSection.setText(LocaleManager.get(74));
         lblFile.setText(LocaleManager.get(28) + ":");
 
     }
 
     private void toggleLabels()
     {
         if (file != null)
         {
             lblFilePath.setText(file.getPath());
             lblFilePath.setVisible(true);
             btnUpload.setDisable(false);
 
         }
         else
         {
             lblFilePath.setVisible(false);
             btnUpload.setDisable(true);
         }
     }
 
     @FXML
     public void browseFile(ActionEvent event)
     {
         btnBrowseFile.setDisable(true);
         FileChooser fileChooser = new FileChooser();
 
         // set filter
         FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
         fileChooser.getExtensionFilters().add(extFilter);
 
         file = fileChooser.showOpenDialog(new Stage());
         toggleLabels();
         btnBrowseFile.setDisable(false);
 
     }
 
     @FXML
     public void uploadScore(ActionEvent event)
     {
         upload(file);
     }
 
     private void upload(File file)
     {
         ArrayList<String> resultList = new ArrayList();
 
         try (BufferedReader reader = new BufferedReader(new FileReader(file)))
         {
 
             String text = "";
             while ((text = reader.readLine()) != null)
             {
                 text = text.trim();
                 String tempData[] = text.split(",");
                 for (int i = 0; i < tempData.length; i++)
                 {
                     data.add(tempData[i].trim());
                 }
             }
             for (int i = 0; i < data.size(); i++)
             {
                 String studentId = data.get(i).trim();
                 String score = data.get(++i).trim();
                 String result = "Student Id: " + studentId + " Score: " + score;
 
                 EnrolmentPK enPK = new EnrolmentPK(Integer.parseInt(studentId),
                         Context.getInstance().getSelectedSection().getSectionId(),
                         Context.getInstance().getSelectedSection().getSectionPK().getCourseNumber(),
                         Context.getInstance().getSelectedSection().getSectionPK().getDepartmentId(),
                         Context.getInstance().getSelectedSection().getSectionPK().getTermYear(),
                         Context.getInstance().getSelectedSection().getSectionPK().getSessionId(),
                         Context.getInstance().getSelectedSection().getSectionPK().getSectionTypeId());
                 try
                 {
                     Enrolment enrol = Enrolment.getEnrolmentByEnrolmentPK(enPK);
                     if (enrol.getScore() != null)
                     {
                         throw new Exception(LocaleManager.get(77));
                     }
                     enrol.setScore(Integer.parseInt(score));
                     enrol.setIsActive(false);
                     enrol.save();
                     result += " <uploaded>";
                     resultList.add(result);
 
                 }
                 catch (Exception ex)
                 {
                     result += " <failed: " + ex.getLocalizedMessage() + ">";
                     resultList.add(result);
                 }
             }
             lsvResult.getItems().setAll(resultList);
         }
         catch (IOException ex)
         {
             Logger.getLogger(UploadScoreController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
    public void selectSection(MouseEvent event)
     {
         Section sec = Context.getInstance().getSelectedSection();
         lblDeptCoureNumber.setText(sec.getSectionPK().getDepartmentId() + " " + sec.getSectionPK().getCourseNumber());
         lblSecNumber.setText(Integer.toString(sec.getSectionId()));
         lblSession.setText(Session.getBySessionId(sec.getSectionPK().getSessionId()).getName());
         toggleLabels();
 
     }
 }
