 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mmt;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.TableView;
 
 import java.lang.String;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ResourceBundle;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ListChangeListener;
 import javafx.collections.ObservableList;
 import javafx.event.EventHandler;
 import javafx.scene.SnapshotResult;
 import javafx.scene.control.Button;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.Hyperlink;
 import javafx.scene.control.Label;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.SelectionMode;
 import javafx.scene.control.TabPane;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TextArea;
 import javafx.scene.control.TextField;
 import javafx.scene.control.Tooltip;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.Pane;
 import javafx.stage.DirectoryChooser;
 import javafx.stage.Window;
 import javafx.util.Callback;
 
 import se.mbaeumer.fxmessagebox.*;
 import mmt.ReportPageManager.*;
 import mmt.utilities.DataValidator;
 
 /**
  * FXML Controller class
  *
  * @author Aditya Mishra
  */
 public class MmtController implements Initializable {
 
     /*
      * TextFiled to get search string
      */
     @FXML
     private TextField searchField;
     @FXML
     private Button backupButton, restoreButton;
     /*
      * Hyperlinks for menu-items
      */
     @FXML
     private Hyperlink employeeHyperlink;
     @FXML
     private Hyperlink employerHyperlink;
     @FXML
     private Hyperlink reportHyperlink;
     @FXML
     private Hyperlink helpHyperlink;
     @FXML
     private Hyperlink helpAboutHyperlink;
     /*
      * Shows data in TableView for the selected category
      */
     @FXML
     private TableView datatableview;
     /*
      * Shows data in filter choice-box : FILTERNM
      */
     //@FXML
     //private ChoiceBox filterChoiceBox;
     /*
      * Definitions of all control buttons in the controller
      */
     @FXML
     private Button addButton;
     @FXML
     private Button removeButton;
     @FXML
     private Button saveButton;
     @FXML
     private Button printButton;
     @FXML
     private Button evaluateButton;
     @FXML
     private Button reportButton;
     @FXML
     private Button commitButton;
     @FXML
     private AnchorPane reportPagesAnchor;
     @FXML
     private Label paginationLabel;
     @FXML
     private Button firstPageButton;
     @FXML
     private Button prevPageButton;
     @FXML
     private Button nextPageButton;
     @FXML
     private Button lastPageButton;
     /*
      * Employee placement related controls
      */
     @FXML
     private AnchorPane placeEmployeePane;
     @FXML
     private ChoiceBox employerChoiceBox;
     @FXML
     private Button placeButton;
 
     /*
      * Controls for employee evaluation report panel
      *
      */
     @FXML
     private AnchorPane evalResultPane;
     @FXML
     private TabPane empScoreTabs;
     @FXML
     private TextField evalNumber;
     @FXML
     private TextField employeeNumber;
     @FXML
     private TextField employerNumber;
     @FXML
     private TextField evalDate;
     @FXML
     private TextField nextEvalDate;
     @FXML
     private TextField averageScore;
     @FXML
     private CheckBox evalRecommendation;
     @FXML
     private RadioButton wqs1, wqs2, wqs3, wqs4, wqs5;
     @FXML
     private RadioButton hqs1, hqs2, hqs3, hqs4, hqs5;
     @FXML
     private RadioButton kqs1, kqs2, kqs3, kqs4, kqs5;
     @FXML
     private RadioButton bqs1, bqs2, bqs3, bqs4, bqs5;
     @FXML
     private RadioButton pqs1, pqs2, pqs3, pqs4, pqs5;
     @FXML
     private TextArea qualityCommentTextArea;
     @FXML
     private TextArea behaviorCommentTextArea;
     @FXML
     private TextArea progressCommentTextArea;
     @FXML
     private TextArea knowledgeCommentTextArea;
     @FXML
     private TextArea habitsCommentTextArea;
     // Holds the pages of evaluation reports
     private ReportPageManager reportPageManager = null;
     /*
      * Controls for employee details panel
      *
      */
     @FXML
     private AnchorPane employeeDetailsPane;
     @FXML
     private TextField employeeDetails_empNumber;
     @FXML
     private TextField employeeDetails_firstName;
     @FXML
     private TextField employeeDetails_lastName;
     @FXML
     private TextField employeeDetails_emailAddressss;
     @FXML
     private TextField employeeDetails_phoneNumber;
     @FXML
     private TextField employeeDetails_cellNumber;
     @FXML
     private TextField employeeDetails_streetAddress;
     @FXML
     private TextField employeeDetails_city;
     @FXML
     private TextField employeeDetails_state;
     @FXML
     private TextField employeeDetails_zipCode;
     /*
      * Controls for employer details panel
      *
      */
     @FXML
     private AnchorPane employerDetailsPane;
     @FXML
     private TextField employerDetails_empNumber;
     @FXML
     private TextField employerDetails_companyName;
     @FXML
     private TextField employerDetails_contactPersonName;
     @FXML
     private TextField employerDetails_emailAddressss;
     @FXML
     private TextField employerDetails_phoneNumber;
     @FXML
     private TextField employerDetails_streetAddress;
     @FXML
     private TextField employerDetails_city;
     @FXML
     private TextField employerDetails_state;
     @FXML
     private TextField employerDetails_zipCode;
 
     /*
      * Controls for concise report's details panel
      *
      */
     @FXML
     private AnchorPane reportDetailsPane;
     @FXML
     private TextField report_evalNumber;
     @FXML
     private TextField report_employeeNumber;
     @FXML
     private TextField report_employerNumber;
     @FXML
     private TextField report_evalDate;
     @FXML
     private TextField report_nextEvalDate;
     @FXML
     private TextField report_averageScore;
     @FXML
     private CheckBox report_recommended;
     @FXML
     private TableView reportDetails_scoreTable;
     /* 
      * Separate page controls : report
      */
     @FXML
     private Pane empFullReportPane;
     @FXML
     private AnchorPane sepPageReportControlPane;
     @FXML
     private TextArea empFullReportTextAreaa;
     @FXML
     private AnchorPane reportPagesAnchor2;
     @FXML
     private Button prevPageButton2, nextPageButton2;
     @FXML
     private Label paginationLabel2;
     @FXML
     private Label printReportForLabel;
     /* 
      * Separate page controls : First page main page
      */
     @FXML
     private AnchorPane mainAnchor;
     @FXML
     private Pane firstPage;
     /* 
      * Help page related anchor-pane and controls
      */
     @FXML
     private AnchorPane helpPageAnchor;
     /*
      * Print related stuff
      */
     Callback<SnapshotResult, Void> printCallback = new PrintCallback();
     /*
      * mode of the view
      */
     @FXML
     private ViewMode appMode = ViewMode.NONE;
 
     /*
      * Data list for empolyee/employer/report/field placements
      */
     private DataManager employeeDataManager = new DataManager();
     private DataManager employerDataManager = new DataManager();
     private DataManager reportDataManager = new DataManager();
     private DataStateManager dataStateManager = new DataStateManager();
     private IdGenerator idGenerator = null;
     private boolean addIsInProgress = false; //set to true when clicked add, resets to false when saved
     private final boolean empEvalInSeparateRecord = true; // holds true if evaluaiton result is stored in separate record/file
     private FieldPlacementManager fieldPlacementManager = new FieldPlacementManager(); // keeps the employeeId-employerId map for placed employees
     private ObservableList<EmployerIdName> employerIdNameList = FXCollections.observableArrayList();
 
     /*
      * Class to represent score table for concise report
      */
     public static class EvalScore {
 
         private final SimpleStringProperty category;
         private final SimpleStringProperty score;
 
         private EvalScore(String category, String score) {
             this.category = new SimpleStringProperty(category);
             this.score = new SimpleStringProperty(score);
         }
 
         public String getCategory() {
             return category.get();
         }
 
         public void setFirstName(String cat) {
             category.set(cat);
         }
 
         public String getScore() {
             return score.get();
         }
 
         public void setLastName(String sc) {
             score.set(sc);
         }
     }
 
     /*
      * Class to represent employer name and id for selection purpose
      * during placement of the employee.
      */
     public static class EmployerIdName {
 
         private String name;
         private String id;
 
         @Override
         public String toString() {
             //return name + " (id:" + id + ")";
             String str = name;
             if (!id.isEmpty()) {
                 str = str + " (id:" + id + ")";
             }
             return str;
         }
 
         public EmployerIdName(String name, String id) {
             this.name = name;
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public String getId() {
             return id;
         }
 
         public void setSalary(String id) {
             this.id = id;
         }
     }
 
     /*
      * Handler for Employee Button-Click
      * This event will load the employee data from data-file
      * @params event: the action event
      */
     @FXML
     private void onClickEmployeeButton(ActionEvent event) {
         System.out.println("You clicked Employee Button!");
 
         // If it is first time then initialize the filter data from master data
         if (employeeDataManager.filteredData == null) {
             //initEmployeeFilteredData();
             initFilteredData();
         }
 
         // Do the clean up of view/resource/hide etc
         // before showing selcted menu-items's views
         beforeSwitchingToNewSelectedPage(ViewMode.EMPLOYEE);
 
         // Set the view mode to employee
         appMode = ViewMode.EMPLOYEE;
         enableDetailPanel(true);
         try {
             System.out.println("The size of data is " + employeeDataManager.filteredData.size());
             datatableview.setItems(employeeDataManager.filteredData);
             datatableview.getColumns().setAll(employeeDataManager.columnsData);
             System.out.println("Employee data loaded");
 
             // Select the first element by default, if any
             datatableview.getSelectionModel().clearSelection();
             //datatableview.getSelectionModel().select(0);
             datatableview.getSelectionModel().selectFirst();//selectIndices(1, new int[]{0});
 
         } catch (Exception ex) {
             Logger.getLogger(MmtController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /*
      * Handler for Employer Button-Click
      * @params event: the action event
      */
     @FXML
     private void onClickEmployerButton(ActionEvent event) {
         System.out.println("You clicked Employer Button!");
 
         // If it is first time then initialize the filter data from master data
         if (employerDataManager.filteredData == null) {
             //initEmployerFilteredData();
             initFilteredData();
         }
 
         // Do the clean up of view/resource/hide etc
         // before showing selcted menu-items's views
         beforeSwitchingToNewSelectedPage(ViewMode.EMPLOYER);
 
         // Set the view mode to employee
         appMode = ViewMode.EMPLOYER;
         enableDetailPanel(true);
         try {
             System.out.println("The size of data is " + employerDataManager.filteredData.size());
             datatableview.setItems(employerDataManager.filteredData);
             datatableview.getColumns().setAll(employerDataManager.columnsData);
             System.out.println("Employer data loaded");
 
             // Select the first element by default, if any
             datatableview.getSelectionModel().clearSelection();
             //datatableview.getSelectionModel().select(0);
             datatableview.getSelectionModel().selectFirst();
 
         } catch (Exception ex) {
             Logger.getLogger(MmtController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /*
      * Handler for Report Button-Click
      * @params event: the action event
      */
     @FXML
     private void onClickMainReportButton(ActionEvent event) {
         System.out.println("You clicked Report Button!");
 
         // If it is first time then initialize the filter data from master data
         if (reportDataManager.filteredData == null) {
             //initEvalResultsFilteredData();
             initFilteredData();
         }
 
         // Do the clean up of view/resource/hide etc
         // before showing selcted menu-items's views
         beforeSwitchingToNewSelectedPage(ViewMode.REPORT_CONCISE);
 
         // Set the view mode to employee
         appMode = ViewMode.REPORT_CONCISE;
         enableDetailPanel(true);
         try {
             System.out.println("The size of data is " + reportDataManager.filteredData.size());
             datatableview.setItems(reportDataManager.filteredData);
             datatableview.getColumns().setAll(reportDataManager.columnsData);
             System.out.println("Evaluation Results Report data loaded");
 
             // Select the first element by default, if any
             datatableview.getSelectionModel().clearSelection();
             //datatableview.getSelectionModel().select(0);
             datatableview.getSelectionModel().selectFirst();
 
         } catch (Exception ex) {
             Logger.getLogger(MmtController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /*
      * Handler for Help Button-Click
      * @params event: the action event
      */
     @FXML
     private void onClickHelpButton(ActionEvent event) {
         System.out.println("You clicked Help Button!");
 
         // Do the clean up of view/resource/hide etc
         // before showing selcted menu-items's views
         beforeSwitchingToNewSelectedPage(ViewMode.HELP);
 
         // Set the view mode to employee
         appMode = ViewMode.HELP;
 
         // Show help page
         helpPageAnchor.setVisible(true);
     }
 
     /*
      * Handler for Help Button-Click
      * @params event: the action event
      */
     @FXML
     private void onClickHelpAboutButton(ActionEvent event) {
         System.out.println("You clicked Help About Button!");
 
         // Do the clean up of view/resource/hide etc
         // before showing selcted menu-items's views
         //beforeSwitchingToNewSelectedPage(ViewMode.HELP_ABOUT); 
 
         // Display message box with credits
         String str = HelpAboutString.getString();
         MessageBox mb = new MessageBox(str, MessageBoxType.OK_ONLY);
         mb.setHeight(500);
         mb.showAndWait();
     }
 
     /*
      * Handler for Add Button-Click
      * @params event: the action event
      */
     @FXML
     private void onClickAddButton(ActionEvent event) {
         System.out.println("You clicked Add Button!");
     }
 
     /*
      * Handler for Remove Button-Click
      * @params event: the action event
      */
     @FXML
     private void onClickRemoveButton(ActionEvent event) {
         System.out.println("You clicked Remove Button!");
     }
 
     /*
      * Handler for Save Button-Click
      * @params event: the action event
      */
     @FXML
     private void onClickSaveButton(ActionEvent event) {
         System.out.println("You clicked Save Button!");
     }
 
     /*
      * Method to show details filter choice-box
      * @params : flag to show or hide it 
      */
     @FXML
     private void showFilterChoiceBox(boolean enable) {
         //filterChoiceBox.setVisible(enable); : FILTERNM
     }
 
     /*
      * Method to init details filter choice-box
      * @params : choices as string for selection
      * @params: index of choice to set as selected choice
      */
     /* FILTERNM
      * @FXML
      private void initFilterChoiceBox(String[] choices, int curSelIndex) {
      if (choices == null) {
             
      filterChoiceBox.setItems(FXCollections.observableArrayList(
      "All", "Employer", "Employee") );
             
      } else {
      filterChoiceBox.setItems(FXCollections.observableArrayList(choices));
      }
         
      // Add tooltip
      filterChoiceBox.setTooltip(new Tooltip("Filter result by"));
         
      // Select default choice
      filterChoiceBox.getSelectionModel().select(curSelIndex);
 
      }
      */
     /*
      * Helper Method to init button
      * @params : handle to image
      * @params: text for tooltip
      * @params: enable/disable flag
      */
     @FXML
     private void initButton(Button button, String toolTipText, boolean enable, EventHandler eventHandler) {
         if (button != null) {
             // Set the tooltip, if provided
             if (toolTipText != null) {
                 button.setTooltip(new Tooltip(toolTipText));
             }
 
             // set event handler, if provided
             if (eventHandler != null) {
                 button.setOnAction(eventHandler);
             }
 
             // set visibility
             button.setVisible(enable);
         }
     }
 
     /*
      * Method to initialize all control buttons on the center of the page
      * @params : 
      */
     @FXML
     private void initContolButtons() {
 
         initButton(backupButton, "Click to backup data", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         backupButtonHanlder(e);
                     }
                 });
         initButton(restoreButton, "Click to restore backed up data", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         restoreButtonHanlder(e);
                     }
                 });
         initButton(addButton, "Click to add new record", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         addButtonHanlder(e);
                     }
                 });
         initButton(removeButton, "Click to delete selected record", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         removeButtonHanlder(e);
                     }
                 });
         /* Not using it any more
          initButton(saveButton, "Click to update changes", true, 
          new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent e) {
          saveButtonHanlder(e);
          }
          });
          initButton(printButton, "Click to print report", true, 
          new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent e) {
          printButtonHanlder(e);
          }
          });
          */
         initButton(evaluateButton, "Click to evaluate an employee", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         evaluateButtonHanlder(e);
                     }
                 });
         initButton(reportButton, "Click to see record", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         reportButtonHanlder(e);
                     }
                 });
         initButton(commitButton, "Click to save changes to disk", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         commitButtonHanlder(e);
                     }
                 });
         initButton(placeButton, "Click to place selected employee(s) to this employer", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         placeButtonHanlder(e);
                     }
                 });
     }
     /*
      * Method to find employer id-name list from the
      * employer data
      */
 
     private List getEmployerIdNameList() {
         List idNameList = new ArrayList<EmployerIdName>();
         // Get the employer id and name from the employer-data
         // Build the employer id-name observableList which will be used to update 
         HashMap<String, EmployerIdName> hashMap = new HashMap<String, EmployerIdName>();
         if (employerDataManager.masterData != null && !employerDataManager.masterData.isEmpty()) {
             // Iterare thru list and get the
             /* Dummy test data
             hashMap.put("Google", new EmployerIdName("Google", "17"));
             hashMap.put("Yahoo", new EmployerIdName("Yahoo", "73"));
             hashMap.put("Microsoft", new EmployerIdName("Microsoft", "87"));
 
             hashMap.put("Google", new EmployerIdName("Google", "17"));
             hashMap.put("Yahoo", new EmployerIdName("Yahoo", "73"));
             hashMap.put("Microsoft", new EmployerIdName("Microsoft", "87"));
             */
             
             Iterator<String[]> iter = employerDataManager.masterData.iterator();
             while (iter.hasNext()) {
                 String[] rowStings = iter.next();
                 String id = rowStings[0];       // id
                 String name = rowStings[1];     // name
                 hashMap.put(name,new EmployerIdName(name, id));
             } // while there is more employer data
         }
 
         // Get the unique id and name from the hashmap to build list
         Iterator<EmployerIdName> iter = hashMap.values().iterator();
         while (iter.hasNext()) {
             EmployerIdName object = (EmployerIdName) iter.next();
             idNameList.add(object);
         }
 
         return idNameList;
     }
 
     @FXML
     private void initEmployeePlacementControl(boolean enable) {
         // choice box
         //
         employerIdNameList.clear();
         employerIdNameList.add(new EmployerIdName("Select an employer", ""));
         List idNameList = getEmployerIdNameList();
         employerIdNameList.addAll(idNameList);
 
         // Update the choice-box
         employerChoiceBox.setItems(employerIdNameList);
         employerChoiceBox.getSelectionModel().selectFirst(); // Select first as default
         employerChoiceBox.setTooltip(new Tooltip("Select employer & click place button"));
 
         // show the choice-box, if enable
         placeEmployeePane.setVisible(enable);
     }
 
     @FXML
     private void placeButtonHanlder(ActionEvent e) {
         //Get the user's choice
         EmployerIdName selIdName = (EmployerIdName) employerChoiceBox.getSelectionModel().getSelectedItem();
         final String newEmployerId = selIdName.getId();
         if (!selIdName.getId().isEmpty()) {
             // DEBUG
             //String msg = newEmployerId + " : is selected: " + selIdName.getName();
             //MessageBox mb = new MessageBox(msg, MessageBoxType.OK_ONLY);
             //mb.showAndWait();
             // DEBUG
 
             // Get all selected employee from employee-data and its employer id
             ArrayList<String> employeeIdsForUpdate = new ArrayList<String>();
             ArrayList<String> newEmployerIds = new ArrayList<String>();
             ObservableList<String[]> selEmployeeList = datatableview.getSelectionModel().getSelectedItems();
             int count = selEmployeeList.size();
             for (int i = 0; i < count; i++) {
                 String[] employeeDataStrings = selEmployeeList.get(i);
                 // Get the employee id
                 String employeeId = employeeDataStrings[0];
                 employeeIdsForUpdate.add(employeeId);
                 newEmployerIds.add(newEmployerId); // the list will have same values for all entry
             } // check each selected item
 
             // Update the field-placement-data
             fieldPlacementManager.placeEmployee(newEmployerId, employeeIdsForUpdate);
             // Update the employee's report data with new employer id
             final int colIndexForEmployeeIdInReport = 1;
             final int colIndexForEmployerIdInReport = 2;
             boolean status = updateReportData(colIndexForEmployeeIdInReport, colIndexForEmployerIdInReport, employeeIdsForUpdate, newEmployerIds);
             String statusMsg;
             if (status == false) {
                 statusMsg = "Fatal Error in updating employer to : " + selIdName.toString();
             } else {
                 statusMsg = "Success in updating employer to : " + selIdName.toString();
             }
             // Show error message box
             MessageBox mb = new MessageBox(statusMsg, MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
     }
 
     @FXML
     private void backupButtonHanlder(ActionEvent e) {
         // Prompt user to get backup-location/directory
         String title = Prompts.getBackupDirPrompt();
         String initialDir = AppSettings.DataRootPath;
         File dirPath = getDirectoryFromUser(title, initialDir);
 
         //MessageBox mb = new MessageBox(filePath, MessageBoxType.OK_ONLY);
         //mb.showAndWait();
         // User has provided folder
         if (dirPath != null) {
             // Backup the data to the input directory
             String filePath = dirPath.getAbsolutePath();
             if (!filePath.endsWith(System.getProperty("file.separator"))) {
                 filePath = filePath + System.getProperty("file.separator");
             }
             //System.out.println(filePath);
             boolean status = backupData(filePath);
             String statusMsg = "";
             if (status == false) {
                 statusMsg = "Fatal Error in storing application data.";
             } else {
                 statusMsg = "Success in storing application data.";
             }
             // Show error message box
             MessageBox mb = new MessageBox(statusMsg, MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
     }
 
     @FXML
     private void restoreButtonHanlder(ActionEvent e) {
 
         // Prompt user to get backup-location/directory
         String title = Prompts.getRestoreDirPrompt();
         String initialDir = AppSettings.DataRootPath;
         File dirPath = getDirectoryFromUser(title, initialDir);
         //MessageBox mb = new MessageBox(filePath, MessageBoxType.OK_ONLY);
         //mb.showAndWait();
         // User has provided folder
         if (dirPath != null) {
             // Restore the data from the input directory
             String filePath = dirPath.getAbsolutePath();
             if (!filePath.endsWith(System.getProperty("file.separator"))) {
                 filePath = filePath + System.getProperty("file.separator");
             }
 
             boolean status = restoreData(filePath);
 // This code is moved inside restoreData to give enough time to get data-tables populated
 // So that data can be saved to app-data-location for future use.
 //            String statusMsg = "";
 //            if (status == false) {
 //                statusMsg = "Fatal Error in restoring application data.";
 //            } else {
 //                statusMsg = "Succuss in restoring application data.";
 //            }
 //            // Show error message box
 //            MessageBox mb = new MessageBox(statusMsg, MessageBoxType.OK_ONLY);
 //            mb.showAndWait();
 
         }
     }
 
     @FXML
     private void addButtonHanlder(ActionEvent e) {
 
         /*
          MessageBox mb = new MessageBox("Add new?", MessageBoxType.OK_CANCEL);
          mb.showAndWait();
          if (mb.getMessageBoxResult() == MessageBoxResult.OK){
          System.out.println("OK");
          }else{
          System.out.println("Cancel");
          }
          */
         clearDetailPanel(appMode);
     }
 
     @FXML
     private void removeButtonHanlder(ActionEvent e) {
         MessageBox mb = new MessageBox("Do you want to delete?", MessageBoxType.YES_NO);
         mb.showAndWait();
         if (mb.getMessageBoxResult() == MessageBoxResult.YES) {
             System.out.println("YES");
             ObservableList obj = datatableview.getSelectionModel().getSelectedItems();
             boolean changed = datatableview.getItems().removeAll(obj.toArray());
             // If collection has changed then mark the state dirty
             if (changed) {
                 dataStateManager.setDirty(appMode);
             }
         } else {
             System.out.println("NO");
         }
     }
 
     @FXML
     private void saveButtonHanlder(ActionEvent e) {
     }
     /*
      * Method to saves master data to disk based on selected mode
      * @params : flag to enable or disable the details panel for current mode 
      */
 
     @FXML
     private void commitMasterData(ViewMode mode, CommitMode commitMode) {
         // Check which table is active
         boolean success = false;
         switch (mode) {
             case EMPLOYEE:
                 // if full report is visible then it means user wants to save
                 // employee's evaluation report data. Otherwise employee
                 // data will be saved.
                 if (evalResultPane.isVisible()) {
                     success = commitMasterDataForFullReport(commitMode);
                 } else {
                     success = commitMasterDataForEmployee(commitMode);
                 }
                 break;
             case EMPLOYER:
                 success = commitMasterDataForEmployer(commitMode);
                 break;
             case REPORT_FULL: // mode when user presses "Eval" button on employee page
                 success = commitMasterDataForFullReport(commitMode);
                 break;
         } //switch
 
         // If commit has been done succefully then save the next-gen ids
         if (success) {
             idGenerator.save();
         }
     }
     /*
      * Method to saves employee master data to disk based on selected mode
      */
 
     @FXML
     private boolean commitMasterDataForEmployee(CommitMode commitMode) {
         // Get the data from details panel to validate
         String[] employeeDataStrings = new String[AppUtils.columnsInEmployeeData];
         //if (addIsInProgress) {
         //    // add mode
         //    reportDataStrings[0] = idGenerator.getNextId(ViewMode.EMPLOYEE);
         //} else 
         {
             // edit mode
             employeeDataStrings[0] = employeeDetails_empNumber.getText();
         }
         employeeDataStrings[1] = employeeDetails_firstName.getText();
         employeeDataStrings[2] = employeeDetails_lastName.getText();
         employeeDataStrings[3] = employeeDetails_emailAddressss.getText();
         employeeDataStrings[4] = employeeDetails_phoneNumber.getText();
         employeeDataStrings[5] = employeeDetails_cellNumber.getText();
         employeeDataStrings[6] = employeeDetails_streetAddress.getText();
         employeeDataStrings[7] = employeeDetails_city.getText();
         employeeDataStrings[8] = employeeDetails_state.getText();
         employeeDataStrings[9] = employeeDetails_zipCode.getText();
 
         boolean bValidData1 = DataValidator.isEmailValid(employeeDataStrings[3]);
         boolean bValidData2 = DataValidator.isPhoneNumberValid(employeeDataStrings[4]);
         boolean bValidData3 = DataValidator.isPhoneNumberValid(employeeDataStrings[5]);
         boolean bValidData4 = DataValidator.isZipCodeValid(employeeDataStrings[9]);
         if (!bValidData1 || !bValidData2 || !bValidData3 || !bValidData4) {
             MessageBox mb = new MessageBox("Error in data. Do you want to discard changes : changes will be lost?", MessageBoxType.YES_NO);
             mb.showAndWait();
             if (mb.getMessageBoxResult() == MessageBoxResult.YES) {
                 // Discard the changes and chane the state clean
                 addIsInProgress = false;
                 dataStateManager.setClean(ViewMode.EMPLOYEE);
                 return false;
             } else {
                 // Let user fix the table and click save again.
                 return false;
             }
         }
 
         //You are here that means data is valid
         // Update master data (it should triger to update filtered data automatically).
         if (addIsInProgress) {
             // add mode
             employeeDataManager.masterData.add(employeeDataStrings);
         } else {
             // edit mode
             // Get selected index
             int i = datatableview.getSelectionModel().getSelectedIndex();
             employeeDataManager.masterData.set(i, employeeDataStrings);
         }
 
         // Write it to disk
         boolean status = false;
         if (commitMode == CommitMode.UPDATE_DISK) {
             status = AppUtils.BackupData(ViewMode.EMPLOYEE, AppUtils.dataHeaderEmployee, employeeDataManager, null);
             // Mark the data dirty
             dataStateManager.setClean(ViewMode.EMPLOYEE);
         }
 
         // Update the state
         addIsInProgress = false;
 
         return status;
     }
     /*
      * Method to saves employer master data to disk based on selected mode
      */
 
     @FXML
     private boolean commitMasterDataForEmployer(CommitMode commitMode) {
         // Get the data from details panel to validate
         String[] employerDataStrings = new String[AppUtils.columnsInEmployerData];
         //if (addIsInProgress) {
         //    // add mode
         //    reportDataStrings[0] = idGenerator.getNextId(ViewMode.EMPLOYEE);
         //} else 
         //{
         // edit mode
         //employerDataStrings[0] = employerDetails_empNumber.getText();
         //}
         employerDataStrings[0] = employerDetails_empNumber.getText();
         employerDataStrings[1] = employerDetails_companyName.getText();
         employerDataStrings[2] = employerDetails_streetAddress.getText();
         employerDataStrings[3] = employerDetails_city.getText();
         employerDataStrings[4] = employerDetails_state.getText();
         employerDataStrings[5] = employerDetails_zipCode.getText();
         employerDataStrings[6] = employerDetails_phoneNumber.getText();
         employerDataStrings[7] = employerDetails_emailAddressss.getText();
         employerDataStrings[8] = employerDetails_contactPersonName.getText();
 
         boolean bValidData1 = DataValidator.isEmailValid(employerDataStrings[7]);
         boolean bValidData2 = DataValidator.isPhoneNumberValid(employerDataStrings[6]);
         boolean bValidData3 = DataValidator.isZipCodeValid(employerDataStrings[5]);
         if (!bValidData1 || !bValidData2 || !bValidData3) {
             MessageBox mb = new MessageBox("Error in data. Do you want to discard changes : changes will be lost?", MessageBoxType.YES_NO);
             mb.showAndWait();
             if (mb.getMessageBoxResult() == MessageBoxResult.YES) {
                 // Discard the changes and chane the state clean
                 addIsInProgress = false;
                 dataStateManager.setClean(ViewMode.EMPLOYER);
                 return false;
             } else {
                 // Let user fix the table and click save again.
                 return false;
             }
         }
 
         //You are here that means data is valid
         // Update master data (it should triger to update filtered data automatically).
         if (addIsInProgress) {
             // add mode
             employerDataManager.masterData.add(employerDataStrings);
         } else {
             // edit mode
             // Get selected index
             int i = datatableview.getSelectionModel().getSelectedIndex();
             employerDataManager.masterData.set(i, employerDataStrings);
         }
 
         // Write it to disk
         boolean status = false;
         if (commitMode == CommitMode.UPDATE_DISK) {
             status = AppUtils.BackupData(ViewMode.EMPLOYER, AppUtils.dataHeaderEmployer, employerDataManager, null);
             // Mark the data dirty
             dataStateManager.setClean(ViewMode.EMPLOYER);
         }
 
         // Update the state
         addIsInProgress = false;
 
         return status;
     }
     /*
      * Method to saves report master data to disk based on selected mode
      */
 
     @FXML
     private boolean commitMasterDataForFullReport(CommitMode commitMode) {
         // Get the data from details panel to validate
         String[] reportDataStrings = new String[AppUtils.columnsInFullReportData];
         //if (addIsInProgress) {
         //    // add mode
         //    reportDataStrings[0] = idGenerator.getNextId(ViewMode.EMPLOYEE);
         //} else 
         //{
         // edit mode
         //employerDataStrings[0] = employerDetails_empNumber.getText();
         //}
         reportDataStrings[0] = evalNumber.getText();
         reportDataStrings[1] = employeeNumber.getText();
         reportDataStrings[2] = employerNumber.getText();
         reportDataStrings[3] = evalDate.getText();
         reportDataStrings[4] = nextEvalDate.getText();
 
         // Update quality-tab
         int sumScore = 0;
         int score = 1;
         if (wqs1.isSelected()) {
             score = 1;
         } else if (wqs2.isSelected()) {
             score = 2;
         }
         if (wqs3.isSelected()) {
             score = 3;
         } else if (wqs4.isSelected()) {
             score = 4;
         } else if (wqs5.isSelected()) {
             score = 5;
         }
         sumScore += score;
 
         reportDataStrings[5] = score + "";
         reportDataStrings[6] = qualityCommentTextArea.getText();
 
         // Update habits-tab
         score = 1;
         if (hqs1.isSelected()) {
             score = 1;
         } else if (hqs2.isSelected()) {
             score = 2;
         }
         if (hqs3.isSelected()) {
             score = 3;
         } else if (hqs4.isSelected()) {
             score = 4;
         } else if (hqs5.isSelected()) {
             score = 5;
         }
         sumScore += score;
         reportDataStrings[7] = score + "";
         reportDataStrings[8] = habitsCommentTextArea.getText();
 
 
         // Update knowledge-tab
         score = 1;
         if (kqs1.isSelected()) {
             score = 1;
         } else if (kqs2.isSelected()) {
             score = 2;
         }
         if (kqs3.isSelected()) {
             score = 3;
         } else if (kqs4.isSelected()) {
             score = 4;
         } else if (kqs5.isSelected()) {
             score = 5;
         }
         sumScore += score;
         reportDataStrings[9] = score + "";
         reportDataStrings[10] = knowledgeCommentTextArea.getText();
 
         // Update behavior-tab
         score = 1;
         if (bqs1.isSelected()) {
             score = 1;
         } else if (bqs2.isSelected()) {
             score = 2;
         }
         if (bqs3.isSelected()) {
             score = 3;
         } else if (bqs4.isSelected()) {
             score = 4;
         } else if (bqs5.isSelected()) {
             score = 5;
         }
         sumScore += score;
         reportDataStrings[11] = score + "";
         reportDataStrings[12] = behaviorCommentTextArea.getText();
 
         // Get the average score
         double avgScore = sumScore / 5.0;
         String tempStr = String.format("%.2f", avgScore);
         reportDataStrings[13] = tempStr;
 
         // Update overall-progress-tab
         score = 1;
         if (pqs1.isSelected()) {
             score = 1;
         } else if (pqs2.isSelected()) {
             score = 2;
         }
         if (pqs3.isSelected()) {
             score = 3;
         } else if (pqs4.isSelected()) {
             score = 4;
         } else if (pqs5.isSelected()) {
             score = 5;
         }
         sumScore += score;
         reportDataStrings[14] = score + "";
         reportDataStrings[15] = progressCommentTextArea.getText();
 
         boolean bRecommended = evalRecommendation.isSelected();
         if (bRecommended) {
             reportDataStrings[16] = "1";
         } else {
             reportDataStrings[16] = "0";
         }
 
         /*
          boolean bValidData1 = DataValidator.isEmailValid(reportDataStrings[7]);
          boolean bValidData2 = DataValidator.isPhoneNumberValid(reportDataStrings[6]);
          boolean bValidData3 = DataValidator.isZipCodeValid(reportDataStrings[5]);
          if (!bValidData1 || !bValidData2 || !bValidData3) {
          MessageBox mb = new MessageBox("Error in data. Do you want to discard changes : changes will be lost?", MessageBoxType.YES_NO);
          mb.showAndWait();
          if (mb.getMessageBoxResult() == MessageBoxResult.YES){
          // Discard the changes and chane the state clean
          addIsInProgress = false;
          dataStateManager.setClean(ViewMode.REPORT_FULL);                
          return false;
          }else{
          // Let user fix the table and click save again.
          return false;
          }
          }
          */
 
         //You are here that means data is valid
         // Update master data (it should triger to update filtered data automatically).
         boolean bRecordFound = false;
         int dataLength = reportDataManager.masterData.size();
         for (int i = 0; i < dataLength; i++) {
             String[] rowStings = reportDataManager.masterData.get(i);
             if (matchesFilter(rowStings, reportDataStrings[0], 0)) {
                 reportDataManager.masterData.set(i, reportDataStrings);
                 bRecordFound = true;
             }
         }
         // If record is not found in data that means it is new record
         // add it to master table
         if (bRecordFound == false) {
             // add mode
             reportDataManager.masterData.add(reportDataStrings);
         }
 
         // Write it to disk
         boolean status = false;
         if (empEvalInSeparateRecord) {
             Evaluation empEval = new Evaluation(reportDataStrings);
             //empEval.exportDataWithHeader(os, AppUtils.dataHeaderFullEvalReport);
             //os.close();
             if (commitMode == CommitMode.UPDATE_DISK) {
                 status = AppUtils.backupSingleEvalData(empEval, null);
                 // Mark the data dirty
                 dataStateManager.setClean(ViewMode.REPORT_FULL);
             }
         }
         //else : we are saving reports into consolidated file also.
         {
             // Commits al employees' data into a single file
             // This also works fine
             if (commitMode == CommitMode.UPDATE_DISK) {
                 status = AppUtils.BackupData(ViewMode.REPORT_FULL, AppUtils.dataHeaderFullEvalReport, reportDataManager, null);
                 // Mark the data dirty
                 dataStateManager.setClean(ViewMode.REPORT_FULL);
             }
         }
 
         // Update the state
         addIsInProgress = false;
 
         return status;
     }
 
     @FXML
     private void setMaxCharsInCommentTextArea() {
         //qualityCommentTextArea.;
         /*
          final int maxChars = 10;
          final String restictTo = “[a-zA-z0-9\\s]*”;
          final TextField tf = new TextField() {
          @Override
          public void replaceText(int start, int end, String text) {
          if (matchTest(text)) {
          super.replaceText(start, end, text);
          }
          }
          @Override
          public void replaceSelection(String text) {
          if (matchTest(text)) {
          super.replaceSelection(text);
          }
          }
          private boolean matchTest(String text) {
          return text.isEmpty() || (text.matches(restictTo) && getText().length() < maxChars);
          }
          };
          */
     }
 
     @FXML
     private void printButtonHanlder(ActionEvent e) {
         // Check the report type
         boolean bFullReport = isFullReportMode();
         if (bFullReport == false) {
             // TODO pagination of jobs : multi-doc
         } else {
             printPane(empFullReportPane);
         }
     }
 
     @FXML
     private void evaluateButtonHanlder(ActionEvent e) {
         boolean bOnSeparatePage = false;
         reportButtonHanlder(bOnSeparatePage);
     }
 
     @FXML
     private void reportButtonHanlder(ActionEvent e) {
         boolean bOnSeparatePage = true;
         reportButtonHanlder(bOnSeparatePage);
     }
 
     @FXML
     private void reportButtonHanlder(boolean bOnSeparatePage) {
         // Hide the current details panel
         //enableDetailPanel(false);
         disableAllDetailPanel();
 
         // Get the selcted employee(s) evaluation from the current table-data
         // if mode is EMPLOYEE then we can get data using currently selected emplyoee id
         // if mdoe is EMPLOYER then we need to get evaluation reort for each employee
         // of currently selected employer
         List empEvalList = null;
         switch (appMode) {
             case EMPLOYEE:
                 // Get currently selected employee
                 empEvalList = getSelectedEmployeeReportData();
                 reportPageManager = new ReportPageManager(empEvalList);
                 break;
             case EMPLOYER:
                 // Get currently selected employer's employee
                 // For each employee get the evaluation report
                 // Get the employee's evaluation report
                 empEvalList = getSelectedEmployerEmployeeReportData();
                 reportPageManager = new ReportPageManager(empEvalList);
 
                 break;
         }
 
         AnchorPane tempAnchorPane = reportPagesAnchor;
         if (bOnSeparatePage == false) {
             // Bring up the report details panel
             evalResultPane.setVisible(true);
             tempAnchorPane = reportPagesAnchor;
         } else {
             enableEmployeeReportOnSeparatePage(true);
             tempAnchorPane = reportPagesAnchor2;
         }
 
         // Disable the pagination control buttons, if there is not more than 1 employee
         // in the selected list.
         if (empEvalList.size() > 1) {
             tempAnchorPane.setVisible(true);
         } else {
             tempAnchorPane.setVisible(false);
         }
 
         // Show the details report fot the first record
         if (!reportPageManager.isEmpty()) {
             //Evaluation firstEmpEval = (Evaluation) reportPageManager.getCurPage();
             firstPageButtonHanlder(null);
         } else {
             MessageBox mb = new MessageBox("No report is found?", MessageBoxType.OK_ONLY);
             mb.showAndWait();
             //printPane(evalResultPane); Test printing
         }
     }
 
     @FXML
     private void commitButtonHanlder(ActionEvent e) {
         /*
          MessageBox mb = new MessageBox("Do you want to save changes?", MessageBoxType.YES_NO);
          mb.showAndWait();
          if (mb.getMessageBoxResult() == MessageBoxResult.YES){
          System.out.println("YES");
          // Save the recordd from the table to the persistent storage.
          // and mark the data not-dirty
          }else{
          System.out.println("NO");
          }
          */
         commitMasterData(appMode, CommitMode.UPDATE_DISK);
     }
 
     /*
      * Method to initialize all control buttons on the center of the page
      * @params : 
      */
     @FXML
     private void initPageContolButtons() {
 
         // Keep the anchor-pane for report-page-buttons invisible all time
         // It will be made visible whenever report button is pressed and
         // there is more than 1 page to show/display.
         reportPagesAnchor.setVisible(false);
 
         initButton(firstPageButton, "Click to see first report", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         firstPageButtonHanlder(e);
                     }
                 });
         initButton(prevPageButton, "Click to see previous report", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         prevPageButtonHanlder(e);
                     }
                 });
         initButton(nextPageButton, "Click to see next report", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         nextPageButtonHanlder(e);
                     }
                 });
         initButton(lastPageButton, "Click to see last record", true,
                 new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent e) {
                         lastPageButtonHanlder(e);
                     }
                 });
     }
 
     @FXML
     private void firstPageButtonHanlder(ActionEvent e) {
         Evaluation empEval = (Evaluation) reportPageManager.getFirstPage();
 
         // Check if report is being shown on separate page
         boolean bSeparatePageReport = false;
         if (empFullReportPane.isVisible()) {
             bSeparatePageReport = true;
         }
 
         // Check if we need to show full report or concise report
         boolean bFullReport = isFullReportMode();
         if (bSeparatePageReport == false) {
             updateEmployeeFullReportDetails(empEval);
             enablePageControlButtons(prevPageButton, nextPageButton, paginationLabel);
         } else {
             updateEmployeeFullReportDetailsOnSeparatePage(empEval, bFullReport);
             enablePageControlButtons(prevPageButton2, nextPageButton2, paginationLabel2);
         }
     }
 
     @FXML
     private void prevPageButtonHanlder(ActionEvent e) {
         Evaluation empEval = (Evaluation) reportPageManager.movePrevPage();
         // Check if report is being shown on separate page
         boolean bSeparatePageReport = false;
         if (empFullReportPane.isVisible()) {
             bSeparatePageReport = true;
         }
 
         // Check if we need to show full report or concise report
         boolean bFullReport = isFullReportMode();
         if (bSeparatePageReport == false) {
             updateEmployeeFullReportDetails(empEval);
             enablePageControlButtons(prevPageButton, nextPageButton, paginationLabel);
         } else {
             updateEmployeeFullReportDetailsOnSeparatePage(empEval, bFullReport);
             enablePageControlButtons(prevPageButton2, nextPageButton2, paginationLabel2);
         }
     }
 
     @FXML
     private void nextPageButtonHanlder(ActionEvent e) {
         Evaluation empEval = (Evaluation) reportPageManager.moveNextPage();
         // Check if report is being shown on separate page
         boolean bSeparatePageReport = false;
         if (empFullReportPane.isVisible()) {
             bSeparatePageReport = true;
         }
 
         // Check if we need to show full report or concise report
         boolean bFullReport = isFullReportMode();
         if (bSeparatePageReport == false) {
             updateEmployeeFullReportDetails(empEval);
             enablePageControlButtons(prevPageButton, nextPageButton, paginationLabel);
         } else {
             updateEmployeeFullReportDetailsOnSeparatePage(empEval, bFullReport);
             enablePageControlButtons(prevPageButton2, nextPageButton2, paginationLabel2);
         }
     }
 
     @FXML
     private void lastPageButtonHanlder(ActionEvent e) {
         Evaluation empEval = (Evaluation) reportPageManager.getLastPage();
         // Check if report is being shown on separate page
         boolean bSeparatePageReport = false;
         if (empFullReportPane.isVisible()) {
             bSeparatePageReport = true;
         }
 
         // Check if we need to show full report or concise report
         boolean bFullReport = isFullReportMode();
         if (bSeparatePageReport == false) {
             updateEmployeeFullReportDetails(empEval);
             enablePageControlButtons(prevPageButton, nextPageButton, paginationLabel);
         } else {
             updateEmployeeFullReportDetailsOnSeparatePage(empEval, bFullReport);
             enablePageControlButtons(prevPageButton2, nextPageButton2, paginationLabel2);
         }
     }
 
     private void enablePageControlButtons(Button prevButton, Button nextButton, Label pageLabel) {
 
         // Check prevPage button for enableing/disabling
         boolean enable = reportPageManager.isPrevPageAvailable();
         prevButton.setDisable(!enable);
 
         // Check prevPage button for enableing/disabling
         enable = reportPageManager.isNextPageAvailable();
         nextButton.setDisable(!enable);
 
         String tempStr = reportPageManager.getCurPageNumber() + " of " + reportPageManager.getPageCount() + " Pages";
         pageLabel.setText(tempStr);
     }
 
     /*
      * Method to show details panel based on selected mode
      * @params : flag to enable or disable the details panel for current mode 
      */
     @FXML
     private void enableDetailPanel(boolean enable) {
         System.out.println("enableDetailPanel Called -- enable: " + enable);
 
         // Keep the anchor-pane for report-page-buttons invisible all time
         // It will be made visible whenever report button is pressed and
         // there is more than 1 page to show/display.
         reportPagesAnchor.setVisible(false);
 
         //if (enable == false) 
         {
             // Make details panel for all app-modes invisible
             employeeDetailsPane.setVisible(false);
             employerDetailsPane.setVisible(false);
             evalResultPane.setVisible(false);
             reportDetailsPane.setVisible(false);
         }
 
         // Check which table is active
         boolean enableEvaluateBtn = false; // if we are not in EMPLOYEE page, disable the evaluate button
         boolean enablePlaceBtn = false;
         switch (appMode) {
             /* Moved up to first hide all detail panels before showing
              * mode-specific panel.
              * case NONE:
              if (enable == false) {
              // Make details panel for all app-modes invisible
              employeeDetailsPane.setVisible(false);
              employerDetailsPane.setVisible(false);
              evalResultPane.setVisible(false);
              reportDetailsPane.setVisible(false);
              }
              break;
              */
             case EMPLOYEE:
                 enableEvaluateBtn = true;
                 enablePlaceBtn = true;
                 employeeDetailsPane.setVisible(enable);
                 break;
             case EMPLOYER:
                 employerDetailsPane.setVisible(enable);
                 break;
             case REPORT_CONCISE:
                 reportDetailsPane.setVisible(enable);
                 break;
             case REPORT_FULL:
                 evalResultPane.setVisible(enable);
                 break;
         }
 
         // if we are not in EMPLOYEE page, disable the evaluate button
         if (enableEvaluateBtn == false) {
             evaluateButton.setVisible(enableEvaluateBtn);
         }
 
         // if we are not in EMPLOYEE page, disable the evaluate button
         if (enableEvaluateBtn == false) {
             placeEmployeePane.setVisible(enableEvaluateBtn);
         } else {
             initEmployeePlacementControl(enableEvaluateBtn);
         }
     }
 
     /*
      * Method to clear details panel based on selected mode
      * @params : flag to enable or disable the details panel for current mode 
      */
     @FXML
     private void clearDetailPanel(ViewMode mode) {
 
         // Check which table is active
         switch (mode) {
             case EMPLOYEE:
                 clearDetailPanelForEmployee();
                 break;
             case EMPLOYER:
                 clearDetailPanelForEmployer();
                 break;
             case REPORT_FULL: // mode when user presses "Eval" button on employee page
                 clearDetailPanelForFullReport();
                 break;
         } //switch
     }
     /*
      * Method to clear details panel based for employee 
      */
 
     @FXML
     private void clearDetailPanelForEmployee() {
 
         addIsInProgress = true;
         // Mark the data dirty
         dataStateManager.setDirty(ViewMode.EMPLOYEE);
 
         // Clear out the input fields
         employeeDetails_empNumber.clear();
         employeeDetails_firstName.clear();
         employeeDetails_lastName.clear();
         employeeDetails_emailAddressss.clear();
         employeeDetails_phoneNumber.clear();
         employeeDetails_cellNumber.clear();
         employeeDetails_streetAddress.clear();
         employeeDetails_city.clear();
         employeeDetails_state.clear();
         employeeDetails_zipCode.clear();
 
         // id is set automatically
         String newId = idGenerator.getNextId(ViewMode.EMPLOYEE);
         employeeDetails_empNumber.setText(newId);
     }
     /*
      * Method to clear details panel based for employee 
      */
 
     @FXML
     private void clearDetailPanelForEmployer() {
         // Mark the data dirty
         dataStateManager.setDirty(ViewMode.EMPLOYER);
 
         employerDetails_empNumber.clear();
         employerDetails_companyName.clear();
         employerDetails_contactPersonName.clear();
         employerDetails_emailAddressss.clear();
         employerDetails_phoneNumber.clear();
         employerDetails_streetAddress.clear();
         employerDetails_city.clear();
         employerDetails_state.clear();
         employerDetails_zipCode.clear();
 
         // id is set automatically
         String newId = idGenerator.getNextId(ViewMode.EMPLOYER);
         employerDetails_empNumber.setText(newId);
     }
     /*
      * Method to clear details panel based for employee 
      */
 
     @FXML
     private void clearDetailPanelForFullReport() {
         // Mark the data dirty
         dataStateManager.setDirty(ViewMode.REPORT_FULL);
 
         // Clear everything in "evalResultPane"
         evalNumber.clear();
         employeeNumber.clear();
         employerNumber.clear();
         evalDate.clear();
         nextEvalDate.clear();
         averageScore.clear();
         evalRecommendation.setSelected(false);
 
         wqs1.setSelected(true); // radtion button
         qualityCommentTextArea.clear();
 
         hqs1.setSelected(true); // radtion button
         habitsCommentTextArea.clear();
 
         kqs1.setSelected(true); // radtion button
         knowledgeCommentTextArea.clear();
 
         bqs1.setSelected(true); // radtion button
         behaviorCommentTextArea.clear();
 
         pqs1.setSelected(true); // radtion button
         progressCommentTextArea.clear();
 
         // id is set automatically
         String newId = idGenerator.getNextId(ViewMode.REPORT_FULL);
         evalNumber.setText(newId);
     }
 
     @FXML
     private void enableEmployeeReportOnSeparatePage(boolean enable) {
 
         // Disable/enanle the main page's controls
         mainAnchor.setVisible(!enable);
         firstPage.setVisible(!enable);
         enableDetailPanel(!enable);
 
         // Enable/disable report page controls
         sepPageReportControlPane.setVisible(enable);
         empFullReportPane.setVisible(enable);
     }
 
     @FXML
     private void enableMainPage(boolean enable) {
 
         // Disable/enable the main page's controls
 
         // Enable/disable report page controls
         empFullReportPane.setVisible(enable);
     }
 
     /*
      * Method to show details panel based on selected mode
      * @params : flag to enable or disable the details panel for current mode 
      */
     @FXML
     private void disableAllDetailPanel() {
 
         // Save current mode
         ViewMode vm = appMode;
         appMode = ViewMode.NONE;
 
         // disable
         enableDetailPanel(false);
 
         // Restore mode
         appMode = vm;
     }
 
     /*
      * Method to update details panel based on selected index
      * @params oldValue: last selected index
      * @params newValue: new selected index
      */
     @FXML
     private void updateDetails(Number oldValue, Number newValue) {
         System.out.println("Selection Changed -- old: " + oldValue + ", new: " + newValue);
 
         // Check if we are showing report-details
         // Hide it, if so
         if (evalResultPane.isVisible()) {
             evalResultPane.setVisible(false);
             enableDetailPanel(true);
         }
 
         // Check which table is active
         switch (appMode) {
             case EMPLOYEE:
                 updateEmployeeDetails(oldValue, newValue);
                 break;
             case EMPLOYER:
                 updateEmployerDetails(oldValue, newValue);
                 break;
             case REPORT_CONCISE:
                 updateConciseReportDetails(oldValue, newValue);
                 break;
             case REPORT_FULL:
                 //updateEvalReportDetails(oldValue, newValue);
                 break;
         }
     }
 
     /*
      * Method is called when user selects different filter 
      * from filter choice-box
      * @params oldValue: last selected index
      * @params newValue: new selected index
      */
     @FXML
     private void onFilterSelectionChange(Number oldValue, Number newValue) {
         System.out.println("Selection Changed -- old: " + oldValue + ", new: " + newValue);
 
         // Check which table is active
         switch (appMode) {
             case EMPLOYEE:
                 // TODO
                 break;
             case EMPLOYER:
                 // TODO
                 break;
             case REPORT_CONCISE:
                 // TODO
                 break;
         }
     }
 
     /*
      * Query methods
      */
     private boolean isFullReportMode() {
         boolean bFullReport = true;
         if (appMode == ViewMode.EMPLOYER) {
             bFullReport = false;
         }
 
         return bFullReport;
     }
     /*
      * Method to return true if data is dirty and not yet committed to disk.
      * Otherwise, it returns false.
      */
 
     public boolean isDirty() {
         if (dataStateManager != null) {
             return dataStateManager.isDirty();
         }
 
         return false;
     }
 
     /*
      * Method to update employee details panel based on selected index
      * @params oldValue: last selected index
      * @params newValue: new selected index
      */
     @FXML
     private void updateEmployeeDetails(Number oldValue, Number newValue) {
         System.out.println("Employee Selection Changed -- old: " + oldValue + ", new: " + newValue);
 
         // Get the selected employee
         String[] obj = (String[]) datatableview.getSelectionModel().getSelectedItem();
         String employeeId = obj[0];
 
         // if we are in EMPLOYEE page, row(s) is selected and
         // selected row employee is employed then enable the evaluate button
         if (fieldPlacementManager.isEmployed(employeeId) && !evaluateButton.isVisible()) {
             evaluateButton.setVisible(true);
         }
 
         // Updates the details panel for employee
         System.out.println(obj);
         // String[] dataHeader = new String[]{"id", "firstName", "lastName", "email", "phone", "cellNumber", "address", "city", "state", "zipCode"};
         employeeDetails_empNumber.setText(obj[0]);
         employeeDetails_firstName.setText(obj[1]);
         employeeDetails_lastName.setText(obj[2]);
         employeeDetails_emailAddressss.setText(obj[3]);
         employeeDetails_phoneNumber.setText(obj[4]);
         employeeDetails_cellNumber.setText(obj[5]);
         employeeDetails_streetAddress.setText(obj[6]);
         employeeDetails_city.setText(obj[7]);
         employeeDetails_state.setText(obj[8]);
         employeeDetails_zipCode.setText(obj[9]);
     }
 
     /*
      * Method to update employer details panel based on selected index
      * @params oldValue: last selected index
      * @params newValue: new selected index
      */
     @FXML
     private void updateEmployerDetails(Number oldValue, Number newValue) {
         System.out.println("Employer Selection Changed -- old: " + oldValue + ", new: " + newValue);
 
         // Get the selected employer
         String[] obj = (String[]) datatableview.getSelectionModel().getSelectedItem();
 
         // Updates the details panel for employer
         System.out.println(obj);
         // String[] dataHeader = new String[]{"id", "name", "address", "city", "state", "zipCode", "phone", "email", "contactPerson"};
         employerDetails_empNumber.setText(obj[0]);
         employerDetails_companyName.setText(obj[1]);
         employerDetails_contactPersonName.setText(obj[8]);
         employerDetails_streetAddress.setText(obj[2]);
         employerDetails_city.setText(obj[3]);
         employerDetails_state.setText(obj[4]);
         employerDetails_zipCode.setText(obj[5]);
         employerDetails_emailAddressss.setText(obj[7]);
         employerDetails_phoneNumber.setText(obj[6]);
     }
 
     /*
      * Method to update details panel with evaluation result for currently
      * selected data/employee
      * @params oldValue: last selected index
      * @params newValue: new selected index
      */
     @FXML
     private void updateEmployeeFullReportDetails(String[] obj) {
         System.out.println("Report Selection Changed -- old: ");
 
         // Get the selected employer
         evalNumber.setText(obj[0]);
         employeeNumber.setText(obj[1]);
         employerNumber.setText(obj[2]);
         evalDate.setText(obj[3]);
         nextEvalDate.setText(obj[4]);
         //averageScore.setText(obj[0]);
 
         // Get the average score
         float avgScore = 0;
         int tempInt = 0;
         final int scoreStartIndex = 5;
         int tempI = 0;
         for (tempI = 0; tempI < 5; tempI++) {
             tempInt = Integer.valueOf(obj[scoreStartIndex + tempI * 2]);
             avgScore += tempInt;
         }
         avgScore = (avgScore / tempI);
         String tempStr = String.format("%.2f", avgScore);
         averageScore.setText(tempStr);
 
         boolean bRecommended = false;
         bRecommended = Integer.valueOf(obj[15]) == 1;
         evalRecommendation.setSelected(bRecommended);
 
         /*
          // Update the tabs for qscore-data
          ObservableList tabsList = empScoreTabs.getTabs();
          //Object[] tabs = (Tab[]) tabsList.toArray();
          for(int i = 0; i < tabsList.size(); i++) {
          Tab curTab = (Tab) tabsList.get(i);
          if (curTab.getId().equals("qualityTab")) {
          // Set the score value
                 
          int xxx = 5;
          xxx++;
          // Set the comment
          }
          }
          */
 
         // Update quality-tab        
     }
 
     /*
      * Method to update details panel with evaluation result for currently
      * selected data/employee
      * @params oldValue: last selected index
      * @params newValue: new selected index
      */
     @FXML
     private void updateEmployeeFullReportDetails(Evaluation empEval) {
         System.out.println("Report Selection Changed -- old: ");
 
         // Get the selected employer
         evalNumber.setText(empEval.getEvaluationNumber());
         employeeNumber.setText(empEval.getEmployeeNumber());
         employerNumber.setText(empEval.getEmployerNumber());
         evalDate.setText(empEval.getEvaluationDate());
         nextEvalDate.setText(empEval.getNxtEvalDate());
         //averageScore.setText(obj[0]);
 
         boolean bRecommended = empEval.isEmploymentRecommendation();
         evalRecommendation.setSelected(bRecommended);
 
         // Update quality-tab
         int sumScore = 0;
         int score = 1;
         score = empEval.getWorkQualityScore();
         sumScore += score;
         switch (score) {
             case 1:
                 wqs1.setSelected(true);
                 break;
             case 2:
                 wqs2.setSelected(true);
                 break;
             case 3:
                 wqs3.setSelected(true);
                 break;
             case 4:
                 wqs4.setSelected(true);
                 break;
             case 5:
                 wqs5.setSelected(true);
                 break;
         }
         qualityCommentTextArea.setText(empEval.getWorkHabitsComments());
 
         // Update behavior-tab
         score = empEval.getBehaviorScore();
         sumScore += score;
         switch (score) {
             case 1:
                 bqs1.setSelected(true);
                 break;
             case 2:
                 bqs2.setSelected(true);
                 break;
             case 3:
                 bqs3.setSelected(true);
                 break;
             case 4:
                 bqs4.setSelected(true);
                 break;
             case 5:
                 bqs5.setSelected(true);
                 break;
         }
         behaviorCommentTextArea.setText(empEval.getBehaviorComments());
 
         // Update overall-progress-tab
         score = empEval.getOverallProgressScore();
         sumScore += score;
         switch (score) {
             case 1:
                 pqs1.setSelected(true);
                 break;
             case 2:
                 pqs2.setSelected(true);
                 break;
             case 3:
                 pqs3.setSelected(true);
                 break;
             case 4:
                 pqs4.setSelected(true);
                 break;
             case 5:
                 pqs5.setSelected(true);
                 break;
         }
         progressCommentTextArea.setText(empEval.getOverallProgressComments());
 
 
         // Update knowledge-tab
         score = empEval.getJobKnowledgeScore();
         sumScore += score;
         switch (score) {
             case 1:
                 kqs1.setSelected(true);
                 break;
             case 2:
                 kqs2.setSelected(true);
                 break;
             case 3:
                 kqs3.setSelected(true);
                 break;
             case 4:
                 kqs4.setSelected(true);
                 break;
             case 5:
                 kqs5.setSelected(true);
                 break;
         }
         knowledgeCommentTextArea.setText(empEval.getJobKnowledgeComments());
 
         // Update habits-tab
         score = empEval.getWorkHabitsScore();
         sumScore += score;
         switch (score) {
             case 1:
                 hqs1.setSelected(true);
                 break;
             case 2:
                 hqs2.setSelected(true);
                 break;
             case 3:
                 hqs3.setSelected(true);
                 break;
             case 4:
                 hqs4.setSelected(true);
                 break;
             case 5:
                 hqs5.setSelected(true);
                 break;
         }
         habitsCommentTextArea.setText(empEval.getWorkHabitsComments());
 
         // Get the average score
         double avgScore = empEval.getAverageScore();
         avgScore = sumScore / 5.0;
         String tempStr = avgScore + "";
         averageScore.setText(tempStr);
     }
 
     /*
      * Method to update details panel with evaluation result for currently
      * selected data/employee
      * @params oldValue: last selected index
      * @params newValue: new selected index
      */
     @FXML
     private void updateEmployeeFullReportDetailsOnSeparatePage(Evaluation empEval, boolean bFullReport) {
         System.out.println("Report Selection Changed -- old: ");
 
         // Get the employee name from the employee-data
         String firstName = ""; // Aditya
         String lastName = ""; // Mishra
         String searchString = empEval.getEmployeeNumber();
         Iterator<String[]> iter = employeeDataManager.masterData.iterator();
         while (iter.hasNext()) {
             String[] rowStings = iter.next();
             if (matchesFilter(rowStings, searchString, 0)) {
                 firstName = rowStings[1];
                 lastName = rowStings[2];
                 break;
             }
         }
         String reportTitle = "Performance report for " + firstName + " " + lastName;
         printReportForLabel.setText(reportTitle);
 
         // Get the selected employer
         //Get the employee name
         StringBuilder sb = new StringBuilder();
 
         sb.append("Evaluation Number:      ");
         sb.append(empEval.getEvaluationNumber());
         sb.append("\nEmployee Number:      ");
         sb.append(empEval.getEmployeeNumber());
         sb.append("\nEmployee Number:      ");
         sb.append(empEval.getEmployerNumber());
         sb.append("\nEvaluation Date:      ");
         sb.append(empEval.getEvaluationDate());
         sb.append("\nNext Evaluation Date: ");
         sb.append(empEval.getNxtEvalDate());
         //averageScore.setText(obj[0]);
 
         // Update quality-tab
         int score = 1;
         score = empEval.getWorkQualityScore();
         sb.append("\n\nWork Quality Score: ");
         sb.append(Integer.toString(score));
         if (bFullReport) {
             sb.append("\nWork Quality Comment: ");
             sb.append(empEval.getWorkHabitsComments());
         }
 
         // Update behavior-tab
         score = empEval.getBehaviorScore();
         sb.append("\n\nWork Behavior Score: ");
         sb.append(Integer.toString(score));
         if (bFullReport) {
             sb.append("\nWork Behavior Comment: ");
             sb.append(empEval.getBehaviorComments());
         }
 
         // Update overall-progress-tab
         score = empEval.getOverallProgressScore();
         sb.append("\n\nOverall Score: ");
         sb.append(Integer.toString(score));
         if (bFullReport) {
             sb.append("\nOverall Progress Comment: ");
             sb.append(empEval.getOverallProgressComments());
         }
 
         // Update knowledge-tab
         score = empEval.getJobKnowledgeScore();
         sb.append("\n\nJob Knowledge Score: ");
         sb.append(Integer.toString(score));
         if (bFullReport) {
             sb.append("\nJob Knowledge Comment: ");
             sb.append(empEval.getJobKnowledgeComments());
         }
 
         // Update habits-tab
         score = empEval.getWorkHabitsScore();
         sb.append("\n\nWork Habits Score: ");
         sb.append(Integer.toString(score));
         if (bFullReport) {
             sb.append("\nWork Habits Comment: ");
             sb.append(empEval.getWorkHabitsComments());
         }
 
         // Get the average score
         float avgScore = (float) empEval.getAverageScore();
         String tempStr = String.format("%.2f", avgScore);
         sb.append("\n----------------------------------------------------------------------------------------------------------------------------------------------------");
         sb.append("\nAverage Score: ");
         sb.append(tempStr);
 
         boolean bRecommended = empEval.isEmploymentRecommendation();
         if (bRecommended) {
             tempStr = "Yes";
         } else {
             tempStr = "No";
         }
         sb.append("\t\t\t\t\tRecommended:   ");
         sb.append(tempStr);
 
         // Write it out to report area
         empFullReportTextAreaa.setText(sb.toString());
     }
 
     /*
      * Updates the master-report data with passed values of the data
      * if search string in the search column matches
      * @param int: searchColumnIndex, which column to search
      * @param int : updateColumnIndex, which column to update if match found
      * @param ArrayList<String>: search-string for each record (e.g; Ids of all records that need update)
      * @param ArrayList<String>: newValueRecords, new value to update selected column with
      */
     private boolean updateReportData(int searchColumnIndex, int updateColumnIndex, ArrayList<String> searchStringInRecords, ArrayList<String> newValueRecords) {
         //You are here that means data is valid
         // Update master data (it should triger to update filtered data automatically).
         if (searchStringInRecords.size() != newValueRecords.size()) {
             return false;
         }
         boolean status = true;
         int dataLength = reportDataManager.masterData.size();
         for (int i = 0; i < dataLength; i++) {
             String[] rowStings = reportDataManager.masterData.get(i);
 
             // Check if find any matching for requested records
             for (int record = 0; record < searchStringInRecords.size(); record++) {
                 if (matchesFilter(rowStings, searchStringInRecords.get(record), searchColumnIndex)) {
                     rowStings[updateColumnIndex] = newValueRecords.get(record);
                     //reportDataManager.masterData.set(i, rowStings); // I dont need to call this as reference is updating the record/value
                     status = true;
                     // match is found: should I break the loop???
                 }
             }// for records in the input parameters
         } // for each record in the master-data
         
         // We are here either because we have updated report-data or did  not
         // find any matching record. In all case, it means we are successful.
         return status;
     }
 
     /*
      * Method to update employer details panel based on selected index
      * @params oldValue: last selected index
      * @params newValue: new selected index
      */
     @FXML
     private void updateConciseReportDetails(Number oldValue, Number newValue) {
         System.out.println("Report Selection Changed -- old: " + oldValue + ", new: " + newValue);
 
         // Get the selected employer
         String[] obj = (String[]) datatableview.getSelectionModel().getSelectedItem();
 
         // Updates the details panel for employer
         report_evalNumber.setText(obj[0]);
         report_employeeNumber.setText(obj[1]);
         report_employerNumber.setText(obj[2]);
         report_evalDate.setText(obj[3]);
         report_nextEvalDate.setText(obj[4]);
         //report_evalNumber.setPromptText("Aditya Mishra");
 
         // Get the employee name from the employee-data
         String firstName = ""; // Aditya
         String lastName = ""; // Mishra
         String searchString = obj[1];
         Iterator<String[]> iter = employeeDataManager.masterData.iterator();
         while (iter.hasNext()) {
             String[] rowStings = iter.next();
             if (matchesFilter(rowStings, searchString, 0)) {
                 firstName = rowStings[1];
                 lastName = rowStings[2];
                 break;
             }
         }
         String reportTitle = "Performance report for " + firstName + " " + lastName;
         printReportForLabel.setText(reportTitle);
 
         // Get the average score
         final int scoreStartIndex = 5;
         /*
          float avgScore = 0;
          int tempInt = 0;
          int tempI = 0;
          for(tempI=0; tempI < 5; tempI ++) {
          tempInt = Integer.valueOf(obj[scoreStartIndex + tempI*2]);
          avgScore += tempInt;
          }
          avgScore = (avgScore/tempI);
          String tempStr = avgScore + "";
          */
         String tempStr = obj[14];
         report_averageScore.setText(tempStr);
 
         boolean bRecommended = false;
         bRecommended = Integer.valueOf(obj[16]) == 1;
         report_recommended.setSelected(bRecommended);
 
         // update the table
         updateConciseReportDetailsScoreTable(obj, scoreStartIndex);
     }
 
     /*
      * Method to get directory name from user
      */
     private File getDirectoryFromUser(String title, String initialDir) {
         DirectoryChooser dirChooser = new DirectoryChooser();
         dirChooser.setTitle(title);
         File dirFile1 = new File(AppUtils.class.getClassLoader().getResource(initialDir).getPath());
         dirChooser.setInitialDirectory(dirFile1);
 
         Window mainWindow = mainAnchor.getScene().getWindow();
         File dirFile2 = dirChooser.showDialog(mainWindow);
 
         return dirFile2;
     }
     /*
      * Method to find a row which has text 'searchString' in the column specified by
      * 'columnIndex'. Returns null if none found.
      * @param DataManager: which data to search into
      * @params String: search string
      * @params int: which column to look for matching
      */
 
     private String[] getRowDataMatchColumn(DataManager dataManager, String searchString, int columnIndex) {
         // Check if we have evaluation report available for this employee
         String[] reportRowStrings = null;
         Iterator<String[]> iter = dataManager.masterData.iterator();
         while (iter.hasNext()) {
             reportRowStrings = iter.next();
             if (matchesFilter(reportRowStrings, searchString, columnIndex)) {
                 return reportRowStrings;
             }
         }
         return null;
     }
 
     private Evaluation getEvalForEmployee(String selEmployeeId, String todayDateString) {
         String[] reportRowStrings = null;
         reportRowStrings = getRowDataMatchColumn(reportDataManager, selEmployeeId, 1); //reportDataManager.masterData.iterator();
         if (reportRowStrings == null) {
             reportRowStrings = new String[]{"", "", "", "", "", "1", "", "1", "", "1", "", "1", "", "1", "1", "", "0"};
             reportRowStrings[0] = idGenerator.getNextId(ViewMode.REPORT_FULL);
             reportRowStrings[1] = selEmployeeId;
             // TODO : get the employer id from the field placement data
             reportRowStrings[2] = fieldPlacementManager.findEmployerOf(selEmployeeId);
             reportRowStrings[3] = todayDateString;
             reportRowStrings[4] = todayDateString;
         }
 
         Evaluation empEval1 = new Evaluation(reportRowStrings);
         return empEval1;
     }
     /*
      * Method to return selected employee's report data from report data-manager
      * using id of selected employee
      */
 
     private List getSelectedEmployeeReportData() {
         // Get selected employee for evaluation
         String[] selRowStr = (String[]) datatableview.getSelectionModel().getSelectedItem();
 
         // Get id of the employee
         String selEmployeeId = selRowStr[0];
 
         Calendar calendar = Calendar.getInstance();
         calendar.clear(Calendar.HOUR);
         calendar.clear(Calendar.MINUTE);
         calendar.clear(Calendar.SECOND);
         Date todayDate = calendar.getTime();
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
         String todayDateString = "03/02/2013";
         try {
             todayDateString = dateFormat.format(todayDate);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         List empEvalList = new ArrayList();
         /*
          // Check if we have evaluation report available for this employee
          String[] reportRowStrings = null;
          reportRowStrings = getRowDataMatchColumn(reportDataManager, selEmployeeId, 1); reportDataManager.masterData.iterator();
          if (reportRowStrings == null) {
          reportRowStrings = new String[]{"", "", "", "", "", "1", "", "1","","1","","1","","1", "1","","0"};
          reportRowStrings[0] = idGenerator.getNextId(ViewMode.REPORT_FULL);
          reportRowStrings[1] = selEmployeeId;
          // TODO : get the employer id from the field placement data
          reportRowStrings[2] = fieldPlacementManager.findEmployerOf(selEmployeeId);
          reportRowStrings[3] = todayDateString;
          reportRowStrings[4] = todayDateString;
          }
         
          Evaluation empEval1 = new Evaluation(reportRowStrings);
          */
         Evaluation empEval1 = getEvalForEmployee(selEmployeeId, todayDateString);
         empEvalList.add(empEval1);
 
         return empEvalList;
     }
     /*
      * Method to return selected employer's all employee's report data 
      * from report data-manager using id of selected employee
      */
 
     private List getSelectedEmployerEmployeeReportData() {
         // HACK : TODO get the object
         /*
          List empEvalList =  new ArrayList();
          String[] reportData2 = new String[] {"100", "90000", "1", "10/3/2012", "3/8/2013", "1", "qs1", "2","hs1","3","semper","1","dolor","1","2","posuere","0"};
          Evaluation empEval2 = new Evaluation(reportData2);
          empEvalList.add(empEval2);
 
          reportData2[0] = "123";
          reportData2[1] = "801";
          reportData2[15] = "1";
          empEval2 = new Evaluation(reportData2);
          empEvalList.add(empEval2);
 
          reportData2[0] = "124";
          reportData2[1] = "901";
          empEval2 = new Evaluation(reportData2);
          empEvalList.add(empEval2);
          */
         // Get selected employer 
         String[] selRowStr = (String[]) datatableview.getSelectionModel().getSelectedItem();
 
         // Get id of the employer
         String selEmployerId = selRowStr[0];
 
         // Get employee ids of the this employer from field placement
         List employeeIdList = fieldPlacementManager.findEmployeesOf(selEmployerId);
 
         Calendar calendar = Calendar.getInstance();
         calendar.clear(Calendar.HOUR);
         calendar.clear(Calendar.MINUTE);
         calendar.clear(Calendar.SECOND);
         Date todayDate = calendar.getTime();
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
         String todayDateString = "03/02/2013";
         try {
             todayDateString = dateFormat.format(todayDate);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         List empEvalList = new ArrayList();
 
         for (int i = 0; i < employeeIdList.size(); i++) {
             Evaluation empEval1 = getEvalForEmployee((String) employeeIdList.get(i), todayDateString);
             empEvalList.add(empEval1);
         }
 
         return empEvalList;
     }
     // Gets the score list from row of report-table
 
     private ObservableList<EvalScore> getScoreTable(String[] rowData, int scoreStartIndex) {
         ObservableList<EvalScore> data =
                 FXCollections.observableArrayList(
                 new EvalScore("Quality", rowData[scoreStartIndex]),
                 new EvalScore("Habit", rowData[scoreStartIndex + 2]),
                 new EvalScore("Knowledge", rowData[scoreStartIndex + 4]),
                 new EvalScore("Behavior", rowData[scoreStartIndex + 6]),
                 new EvalScore("Overall", rowData[scoreStartIndex + 9])
                 );
 
         return data;
     }
 
     /*
      * updates the concise report's score table
      */
     private void updateConciseReportDetailsScoreTable(String[] rowData, int scoreStartIndex) {
 
         TableColumn firstCol = new TableColumn("Performance Category");
         firstCol.setMinWidth(100);
         firstCol.setCellValueFactory(new PropertyValueFactory<EvalScore, String>("category"));
 
         TableColumn secondCol = new TableColumn("Score");
         secondCol.setMinWidth(100);
         secondCol.setCellValueFactory(new PropertyValueFactory<EvalScore, String>("score"));
         
         // get data
         ObservableList<EvalScore> data = getScoreTable(rowData, scoreStartIndex);
         reportDetails_scoreTable.setItems(data);
         reportDetails_scoreTable.getColumns().setAll(firstCol, secondCol);
     }
 
     /**
      * Updates the id generator to match with restored data. Should be called
      * only when data is restored very first time on launch.
      */
     private void updateIdGenerator(ViewMode mode) {
 
         int nextEmployeeId = Integer.parseInt(idGenerator.getNextId(ViewMode.EMPLOYEE));
         int nextEmployerId = Integer.parseInt(idGenerator.getNextId(ViewMode.EMPLOYER));
         int nextReportId = Integer.parseInt(idGenerator.getNextId(ViewMode.REPORT_CONCISE));
         boolean updateEmployeeId = false;
         boolean updateEmployerId = false;
         boolean updateReportId = false;
         switch (mode) {
             case NONE: // wrong type but did not want to add another one.. to update all in one call
                 updateEmployeeId = true;
                 updateEmployerId = true;
                 updateReportId = true;
                 break;
             case EMPLOYEE:
                 updateEmployeeId = true;
                 break;
             case EMPLOYER:
                 updateEmployerId = true;
                 break;
             case REPORT_CONCISE:
             case REPORT_FULL:
                 updateReportId = true;
                 break;
         } // switch
 
         if (updateEmployeeId) {
             // datatableview is sorted on id, so get the last row and corresponding column
             // from this row to fetch id
             ObservableList<String[]> obsEmployeeList = datatableview.getItems();
             if (obsEmployeeList != null && !obsEmployeeList.isEmpty()) {
                 nextEmployeeId = Integer.parseInt(obsEmployeeList.get(obsEmployeeList.size() - 1)[0]);
             }
         }
         if (updateEmployerId) {
             // datatableview is sorted on id, so get the last row and corresponding column
             // from this row to fetch id
             ObservableList<String[]> obsEmployerList = datatableview.getItems();
             if (obsEmployerList != null && !obsEmployerList.isEmpty()) {
                 nextEmployerId = Integer.parseInt(obsEmployerList.get(obsEmployerList.size() - 1)[0]);
             }
 
         }
         if (updateReportId) {
             ObservableList<String[]> obsReportList = datatableview.getItems();
             if (obsReportList != null && !obsReportList.isEmpty()) {
                 nextReportId = Integer.parseInt(obsReportList.get(obsReportList.size() - 1)[0]);
             }
         }
 
         // Got the updated info from tableview
         // update the generator
         idGenerator.reset(nextEmployeeId, nextEmployerId, nextReportId);
     }
 
     /**
      * Updates the filteredData to contain all data from the masterData that
      * matches the current filter.
      */
     private boolean updateFilteredData(ViewMode mode) {
         DataManager dataManager = null;
 
         // Check if caller has overriden the app-mode to be used
         // for updating the filtered data. Use if if so.
         ViewMode curAppMode = appMode;
         if (mode != null) {
             curAppMode = mode;
         }
 
         // Check which table is active
         switch (curAppMode) {
             case EMPLOYEE:
                 dataManager = employeeDataManager;
                 break;
             case EMPLOYER:
                 dataManager = employerDataManager;
                 break;
             case REPORT_CONCISE:
                 dataManager = reportDataManager;
                 break;
             case REPORT_FULL:
                 //dataManager = evalResultDataManager;
                 break;
         }
 
         if (dataManager == null || dataManager.masterData == null) {
             return false;
         }
 
         // If it is first time then filtered-data must be null.
         // Check it if it is null and insitlaize the filter data from master data
         //if (dataManager.filteredData == null) {
         //    dataManager.filteredData = FXCollections.observableArrayList();
         //    dataManager.filteredData.addAll(dataManager.masterData);
         //    System.out.println("1st time init of filter, size is : " + dataManager.filteredData.size());
         //} else 
         {
             // filtered-data exists so just update them.
             dataManager.filteredData.clear();
             String searchString = searchField.getText();
             /*		
              for (String[] colStr : dataManager.masterData) {
              if (matchesFilter(colStr, searchString, -1)) {
              dataManager.filteredData.add(colStr);
              }
              }
              * 
              * // this works too
              for (int i = 0; i < dataLength; i++) {
              String[] colStr = dataManager.masterData.get(i);
              if (matchesFilter(colStr, searchString, -1)) {
              dataManager.filteredData.add(colStr);
              }
              }
              */
             Iterator<String[]> iter = dataManager.masterData.iterator();
             while (iter.hasNext()) {
                 String[] rowStings = iter.next();
                 if (matchesFilter(rowStings, searchString, -1)) {
                     dataManager.filteredData.add(rowStings);
                 }
             }
         }
 
         // Must re-sort table after items changed
         reapplyTableSortOrder();
 
         // Check if match has been found
         boolean matchFound = !dataManager.filteredData.isEmpty();
 
         return matchFound;
     }
 
     /**
      * Returns true if the parameter matches the current filter. Lower/Upper
      * case is ignored.
      *
      * @param String[] : row data
      * @param String: search string
      * @param int : column index to look for. Pass -1, for all columns
      * @return
      */
     private boolean matchesFilter(String[] rowData, String searchString, int columnIndex) {
 
         if (searchString == null) {
             // No filter --> Add all.
             return true;
         }
         if (searchString.isEmpty()) {
             // No filter --> Add all.
             //searchField.setPromptText(Prompts.getSearchPrompt());
             return true;
         }
 
         // Check each column, return true if any column matches with search/filter string
         String lowerCaseFilterString = searchString.toLowerCase();
         if (columnIndex < 0) {
             // Search all columns
             for (String colStr : rowData) {
                 if (colStr.toLowerCase().indexOf(lowerCaseFilterString) != -1) {
                     return true;
                 }
             }
         } else {
             // look only into requested column
             if (0 <= columnIndex && columnIndex < rowData.length) {
                 if (rowData[columnIndex].compareToIgnoreCase(searchString) == 0) {
                     return true;
                 }
             }
         }
 
         return false; // Does not match
     }
 
     private void reapplyTableSortOrder() {
         ArrayList<TableColumn<String[], ?>> sortOrder = new ArrayList<>(datatableview.getSortOrder());
         datatableview.getSortOrder().clear();
         datatableview.getSortOrder().addAll(sortOrder);
     }
 
     @FXML
     private void printPane(Pane pane) {
         pane.snapshot(printCallback, null, null);
     }
 
     @FXML
     private void onClickBackButton(ActionEvent event) {
         enableEmployeeReportOnSeparatePage(false);
     }
 
     /*
      * Method to auto select menu-item if user enters search-string without
      * pre-selecing menu-items.
      */
     private void autoSelectMenuItem() {
     }
 
     /*
      * Utitlity method to get hyperlink object associated with menu-item
      */
     @FXML
     private Hyperlink getHyperLinkAssociateToMenuItem(ViewMode viewMode) {
         Hyperlink hyperlink = null;
         // Check for which mode associated menu item is needed
         switch (viewMode) {
             case EMPLOYEE:
                 hyperlink = employeeHyperlink;
                 break;
             case EMPLOYER:
                 hyperlink = employerHyperlink;
                 break;
             case REPORT_CONCISE:
                 hyperlink = reportHyperlink;
                 break;
             case HELP:
                 hyperlink = helpHyperlink;
                 break;
             case HELP_ABOUT:
                 hyperlink = helpAboutHyperlink;
                 break;
         }
 
         return hyperlink;
     }
 
     /*
      * This Method is called whenever user clicks on any Main Menu Options
      * choices (like Employee, Employer, Report, Help), etc to 
      * before setting up data of selected view and clearing up previous view
      * @params: app mode for newly selected page. The app will switch into it on success.
      */
     @FXML
     private void beforeSwitchingToNewSelectedPage(ViewMode newAppMode) {
 
         // Remove the selected style from the current menu-item
         Hyperlink hyperLink = null;
         hyperLink = getHyperLinkAssociateToMenuItem(appMode);
         if (hyperLink != null) {
             hyperLink.getStyleClass().removeAll("options-text-selected");
         }
         // Apply selected style to new selection menu-item
         hyperLink = getHyperLinkAssociateToMenuItem(newAppMode);
         if (hyperLink != null) {
             hyperLink.getStyleClass().addAll("options-text", "options-text-selected");
         }
 
         // Check if there is change in app-mode
         // Disable the current details panel before enabling 
         // the details panel of new app-mode
         //boolean appModeChanged = false;
         //if (appMode != ViewMode.EMPLOYEE) 
         {
             //appModeChanged = true;
             enableDetailPanel(false);
         }
 
         // Clear up the search field too
         searchField.clear();
 
         // If we are switching to HELP page hide main page
         if (newAppMode == ViewMode.HELP) {
             firstPage.setVisible(false);
         } else {
             // if we are switching from help-page to first page
             // then hide help-page and show 
             helpPageAnchor.setVisible(false);
             firstPage.setVisible(true);
         }
     }
     /*
      * Method initializes all filtered data (Employee, employer, reports)
      * if they ar not already initialized.
      */
 
     @FXML
     private void initFilteredData() {
         // If it is first time then initialize the filter data from master data
         if (employeeDataManager.filteredData == null) {
             initEmployeeFilteredData();
         }
         // If it is first time then initialize the filter data from master data
         if (employerDataManager.filteredData == null) {
             initEmployerFilteredData();
         }
         // If it is first time then initialize the filter data from master data
         if (reportDataManager.filteredData == null) {
             initEvalResultsFilteredData();
         }
     }
 
     @FXML
     private void initEmployeeFilteredData() {
         // If it is first time then insitlaize the filter data from master data
         // And bind list-change event of master-data to update filtered-data
         if (employeeDataManager.filteredData == null) {
             employeeDataManager.filteredData = FXCollections.observableArrayList();
             employeeDataManager.filteredData.addAll(employeeDataManager.masterData);
             System.out.println("1st time init of filter, size is : " + employeeDataManager.filteredData.size());
 
             // Listen for changes in master data.
             // Whenever the master data changes we must also update the filtered data.
             employeeDataManager.masterData.addListener(new ListChangeListener<String[]>() {
                 @Override
                 public void onChanged(ListChangeListener.Change<? extends String[]> change) {
                     updateFilteredData(ViewMode.EMPLOYEE); // TODO
                 }
             });
         }
     }
 
     @FXML
     private void initEmployerFilteredData() {
         // If it is first time then insitlaize the filter data from master data
         // And bind list-change event of master-data to update filtered-data
         if (employerDataManager.filteredData == null) {
             employerDataManager.filteredData = FXCollections.observableArrayList();
             employerDataManager.filteredData.addAll(employerDataManager.masterData);
             System.out.println("1st time init of employer filter, size is : " + employerDataManager.filteredData.size());
 
             // Listen for changes in master data.
             // Whenever the master data changes we must also update the filtered data.
             employerDataManager.masterData.addListener(new ListChangeListener<String[]>() {
                 @Override
                 public void onChanged(ListChangeListener.Change<? extends String[]> change) {
                     updateFilteredData(ViewMode.EMPLOYER); // TODO
                 }
             });
         }
     }
 
     @FXML
     private void initEvalResultsFilteredData() {
         // If it is first time then insitlaize the filter data from master data
         if (reportDataManager.filteredData == null) {
             reportDataManager.filteredData = FXCollections.observableArrayList();
             reportDataManager.filteredData.addAll(reportDataManager.masterData);
             System.out.println("1st time init of report-filter, size is : " + reportDataManager.filteredData.size());
 
             // Listen for changes in master data.
             // Whenever the master data changes we must also update the filtered data.
             reportDataManager.masterData.addListener(new ListChangeListener<String[]>() {
                 @Override
                 public void onChanged(ListChangeListener.Change<? extends String[]> change) {
                     updateFilteredData(ViewMode.REPORT_CONCISE); // TODO
                 }
             });
         }
     }
 
     /*
      * Method to restore data from given path. If no path is supplied then
      * it reads the app-data
      */
     private boolean restoreData(String dataPath) {
         // Read employee's data: read it from disk
         boolean status1 = AppUtils.RestoreData(ViewMode.EMPLOYEE, AppUtils.dataHeaderEmployee, employeeDataManager, dataPath);
         if (status1 == false) {
             // Show error message box
             MessageBox mb = new MessageBox("Error in reading employee data.", MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
         dataStateManager.setClean(ViewMode.EMPLOYEE);
 
         // Read employer's data: read it from disk
         boolean status2 = AppUtils.RestoreData(ViewMode.EMPLOYER, AppUtils.dataHeaderEmployer, employerDataManager, dataPath);
         if (status2 == false) {
             // Show error message box
             MessageBox mb = new MessageBox("Error in reading employer data.", MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
         dataStateManager.setClean(ViewMode.EMPLOYER);
 
         // Read report/evaluation results data: read it from disk
         // Check mode
         boolean status3 = false;
 ///////////////////////////////////////////////////////////////////////////////
 // Somehow single record reading is not going to work as DataFX is not 
 // populating data-table immediately when request to read a file is made. 
 // This is causing getting employee id from employee-data to fail as employee-data
 // which is read firs in this function is not yet populated and hence empty.
 ///////////////////////////////////////////////////////////////////////////////
 //        if (empEvalInSeparateRecord) {
 //            // Read evaluation report of each employee from employees-data
 //            // Add each to report-data
 //            String[] reportRowStrings = null;
 //            Iterator<String[]> iter = employeeDataManager.masterData.iterator();
 //            ArrayList employeeEvalList = new ArrayList<Evaluation>();
 //            while (iter.hasNext()) {
 //                reportRowStrings = iter.next();
 //                Evaluation empEval = new Evaluation(reportRowStrings[0]);
 //                boolean status4 = AppUtils.RestoreSingleEvalData(empEval, null);
 //                if (status4) {
 //                    employeeEvalList.add(empEval);
 //                }
 //             } // while
 //            
 //            // All evaluations are available. write it to combined-file "evaluations.txt"
 //            status3 =  AppUtils.writeMultipleReportToSingleFile(employeeEvalList, dataPath);
 //            
 //            // Populate report table using consolidated-evaluation file
 //            status3 = AppUtils.RestoreData(ViewMode.REPORT_FULL, AppUtils.dataHeaderFullEvalReport, reportDataManager, dataPath);
 //        } else 
         {
             // old-format, not to be used but works fine: all evaluation files are in single file.
             status3 = AppUtils.RestoreData(ViewMode.REPORT_FULL, AppUtils.dataHeaderFullEvalReport, reportDataManager, dataPath);
         }
         if (status3 == false) {
             // Show error message box
             MessageBox mb = new MessageBox("Error in reading report/evaluation data.", MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
         dataStateManager.setClean(ViewMode.REPORT_FULL);
 
         // restore field placement data
         int count = fieldPlacementManager.importData(dataPath, AppSettings.fieldPlacementDataFileName);
 
         // Data has been read into application.
         boolean status = status1 && status2 && status3;
         String statusMsg = "";
         // Show message only when we are restoring on user's request
         if (dataPath != null) {
             if (status == false) {
                 statusMsg = "Fatal Error in reading application data.";
             } else {
                 statusMsg = "Succuss in reading application data.";
             }
             // Show error message box
             MessageBox mb = new MessageBox(statusMsg, MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
 
         // Update app-data from restore data only if we have restored from 
         // different path
         if (status && dataPath != null) {
             // Now that filtered data are poopulated for each category/pages
             //initFilteredData();
 
             // Now save them to the app-data-location
             // threading issue is blocking it to save the restored data because
             // 
             backupData(null); // null will save/overwrite existing app-data with new restored-data.
         }
 
         return status;
     }
 
     /*
      * Method to backups data to the given path. If no path is supplied then
      * it writes to app-data
      */
     private boolean backupData(String dataPath) {
         // Make sure that filtered data are poopulated for each category/pages
         initFilteredData();
 
         // Write employee's data: Write it to disk
         boolean status1 = AppUtils.BackupData(ViewMode.EMPLOYEE, AppUtils.dataHeaderEmployee, employeeDataManager, dataPath);
         if (status1 == false) {
             // Show error message box
             MessageBox mb = new MessageBox("Error in storing employee data.", MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
         dataStateManager.setClean(ViewMode.EMPLOYEE);
 
         // Write employer's data: Write it to disk
         boolean status2 = AppUtils.BackupData(ViewMode.EMPLOYER, AppUtils.dataHeaderEmployer, employerDataManager, dataPath);
         if (status2 == false) {
             // Show error message box
             MessageBox mb = new MessageBox("Error in storing employer data.", MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
         dataStateManager.setClean(ViewMode.EMPLOYER);
 
         // Write report/evaluation results data: Write it to disk
         boolean status3 = AppUtils.BackupData(ViewMode.REPORT_FULL, AppUtils.dataHeaderFullEvalReport, reportDataManager, dataPath);
         if (status3 == false) {
             // Show error message box
             MessageBox mb = new MessageBox("Error in storing report/evaluation data.", MessageBoxType.OK_ONLY);
             mb.showAndWait();
         } else {
             // Save individual record also
             if (empEvalInSeparateRecord) {
                 // Read evaluation report of each employee from employees-data
                 // Add each to report-data
                 String[] reportRowStrings = null;
                 Iterator<String[]> iter = reportDataManager.masterData.iterator();
                 //ArrayList employeeEvalList = new ArrayList<Evaluation>();
                 while (iter.hasNext()) {
                     reportRowStrings = iter.next();
                     Evaluation empEval = new Evaluation(reportRowStrings);
                     boolean status4 = AppUtils.backupSingleEvalData(empEval, dataPath);
                     status3 &= status4;
                 } // while
             }
         }
         dataStateManager.setClean(ViewMode.REPORT_FULL);
 
         // restore field placement data
         boolean status4 = fieldPlacementManager.exportData(dataPath, AppSettings.fieldPlacementDataFileName);
         if (status4 == false) {
             // Show error message box
             MessageBox mb = new MessageBox("Error in storing field placement data.", MessageBoxType.OK_ONLY);
             mb.showAndWait();
         }
 
         boolean status = status1 && status2 && status3 && status4;
 
         return status;
     }
 
     /**
      * The constructor. The constructor is called before the initialize()
      * method.
      */
     public MmtController() {
         // Initialize id generator
         idGenerator = IdGenerator.GetIdGenerator(AppUtils.getNextIdListFileName());
 
         /*
          // Rstore the data before loading
          // Restore employee data before loading
          AppUtils.GetEmployeeData(BackupRestoreMode.RESTORE, employeeDataManager, null);
          //updateIdGenerator(ViewMode.EMPLOYEE);
          // Restore employer data before loading
          AppUtils.GetEmployerData(BackupRestoreMode.RESTORE, employerDataManager, null);            
          //updateIdGenerator(ViewMode.EMPLOYER);
          // Restore report before loading
          AppUtils.GetEvalResultData(BackupRestoreMode.RESTORE, reportDataManager, null);
          //updateIdGenerator(ViewMode.REPORT_CONCISE);
          */
         restoreData(null);
         //updateIdGenerator(ViewMode.NONE);
     }
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         // TODO
        
        // Get rid of extra columm from all tables
        //datatableview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reportDetails_scoreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
 
         // Rstore the data before loading
         //initEmployeeData();
 
         // Initialize table for multiple selectiom
         datatableview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
 
         // Initialize filter choice box
         //initFilterChoiceBox(null, 0); : FILTERNM
         //showFilterChoiceBox(true); : FILTERNM
 
 
 // Creates a button with image : testing only            
 //            InputStream is = this.getClass().getClassLoader().getResourceAsStream("mmt/Resources/commitToDisk.png");
 //            Image imageDecline = new Image(is);
 //            testButton2.setGraphic(new ImageView(imageDecline));
 
         // Initialize the buttons
         initContolButtons();
         initPageContolButtons();
 
         // Listen for text changes in the filter text field
         searchField.textProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> observable,
                     String oldValue, String newValue) {
                 updateFilteredData(null);
             }
         });
 
         // Add the change-listener to table view for user's single-selection
         datatableview.getSelectionModel().selectedIndexProperty().addListener(
                 new ChangeListener<Number>() {
                     public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                         System.out.println("Category Selection Changed -- old: " + oldValue + ", new: " + newValue);
                         if (newValue.intValue() >= 0 && oldValue.intValue() != newValue.intValue()) {
                             updateDetails(oldValue, newValue);
                         }
                     }
                 });
 
         // Add the change-listener to filter choice-box
         /* No more filter: FILTERNM
          * filterChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
          new ChangeListener<Number>() {
          public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
          System.out.println("Filter Selection Changed -- old: " + oldValue + ", new: " + newValue);
          if (newValue.intValue() >=0 && oldValue.intValue() != newValue.intValue() ) {
          onFilterSelectionChange(oldValue, newValue);
          }
          }
          });
          */
         // Keep all details panel invisible on start
         enableDetailPanel(false);
 
         // Set the maximum chars in comment
         setMaxCharsInCommentTextArea();
         try {
             imageLoader = new ImageLoader(helpImagePane);
             Thread t = new Thread(imageLoader);
             t.setDaemon(true);
             t.start();
 
             imageLoader.setNextImage(helpImagePane, imageLoader.getNextImage());
             beforeSwitchingToNewSelectedPage(ViewMode.HELP);
 
             // Set the view mode to employee
             appMode = ViewMode.HELP;
 
             // Show help page
             helpPageAnchor.setVisible(true);
         } catch (Exception ex) {
             Logger.getLogger(MmtController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
     }
     @FXML
     AnchorPane helpImagePane;
     ImageLoader imageLoader;
 
     @FXML
     private void onSlideButtonClickedAction(ActionEvent event) {
         Object source = event.getSource();
         if (source instanceof Button) {
             switch (((Button) source).getId()) {
                 case "leftSlideBtn":
                     //System.out.println("Handling leftSlide..."); 
                     imageLoader.paused = true;
                     imageLoader.setNextImage(helpImagePane, imageLoader.getPrevImage());
                     break;
                 case "rightSlideBtn":
                     imageLoader.paused = true;
                     //System.out.println("Handling rightSlide..."); 
 
                     imageLoader.setNextImage(helpImagePane, imageLoader.getNextImage());
                     break;
 
                 default:
                     System.out.println("Unsupported " + source);
             }
         } else {
             System.out.println("Unsupported source: " + source);
         }
     }
 
     @FXML
     private void onImageClicked(MouseEvent event) {
         imageLoader.togglePause();
         System.out.println("paused is " + imageLoader.paused);
         if (!imageLoader.paused) {
             imageLoader.setNextImage(helpImagePane, imageLoader.getNextImage());
         }
     }
 }
