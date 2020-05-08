 /*
  * The controller class for SectionView.fxml.
  */
 package adg.red.controllers.student;
 
 import adg.red.controllers.BreadCrumbController;
 import adg.red.models.CoRequisite;
 import adg.red.models.Course;
 import adg.red.models.Enrolment;
 import adg.red.models.EnrolmentPK;
 import adg.red.models.Prerequisite;
 import adg.red.models.Section;
 import adg.red.models.Session;
 import adg.red.models.Student;
 import adg.red.session.Context;
 import adg.red.locale.LocaleManager;
 import adg.red.models.SectionTimeTable;
 import adg.red.utils.ViewLoader;
 import java.net.URL;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.TextArea;
 import javafx.scene.input.MouseEvent;
 import javafx.util.Callback;
 import adg.red.utils.DateFormatter;
 import java.util.Date;
 
 /**
  * FXML Controller class for SectionView.fxml
  * <p/>
  * @author Witt
  */
 public class SectionViewController implements Initializable
 {
 
     @FXML
     private TextArea courseDesTxt;
     @FXML
     private Label courseNameLbl;
     @FXML
     private Label secLbl;
     @FXML
     private Label creditLbl;
     @FXML
     private Label deptIdAndCourseNoLbl;
     @FXML
     private Label gradingSchmLbl;
     @FXML
     private Label passRqLbl;
     @FXML
     private Label lblCreditName;
     @FXML
     private Label lblGrading;
     @FXML
     private Label lblPassReq;
     @FXML
     private Label lblTerm;
     @FXML
     private Label lblEnd;
     @FXML
     private Label lblStart;
     @FXML
     private Label lblOutPrereq;
     @FXML
     private Label lblOutCoReq;
     @FXML
     private Button btnRegister;
     @FXML
     private Button btnDrop;
     @FXML
     private Label lblResponse;
     @FXML
     private Label lblSecType;
     @FXML
     private Label lblSession;
     @FXML
     private Label lblTermYear;
     @FXML
     private Label lblStartDate;
     @FXML
     private Label lblEndDate;
     @FXML
     private Label lblRegisDL;
     @FXML
     private Label lblRegisDLDate;
     @FXML
     private Label lblDropDL;
     @FXML
     private Label lblDropDLDate;
     @FXML
     private ListView<Prerequisite> lsvOutstandPrereq;
     @FXML
     private ListView<CoRequisite> lsvOutstandCoReq;
     private Enrolment enrolment = null;
     private Section section = null;
     private EnrolmentPK enrolmentPk;
     @FXML
     private Label lblRegDLResponse;
     @FXML
     private Label lblDropDLResponse;
 
     /**
      * The function toggles the register and drop buttons according to the
      * status of the enrolment of the student.
      */
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
             btnRegister.setDisable(true);
             btnDrop.setDisable(true);
         }
     }
 
     /**
      * The function to handle showPreReq mouseEvent event.
      * <p/>
      * @param event the mouse event
      */
     @FXML
     private void showPreReq(MouseEvent event)
     {
         if (lsvOutstandPrereq.getSelectionModel().getSelectedItem() != null)
         {
             Context.getInstance().setSelectedCourse(lsvOutstandPrereq.getSelectionModel().getSelectedItem().getCourse());
             ViewLoader view = new ViewLoader(Context.getInstance().getDisplayView());
             view.loadView("student/CourseView");
         }
     }
 
     /**
      * The function to handle showCoReq mouseEvent event.
      * <p/>
      * @param event the mouse event
      */
     @FXML
     private void showCoReq(MouseEvent event)
     {
         if (lsvOutstandCoReq.getSelectionModel().getSelectedItem() != null)
         {
             Context.getInstance().setSelectedCourse(lsvOutstandCoReq.getSelectionModel().getSelectedItem().getCourse1());
             ViewLoader view = new ViewLoader(Context.getInstance().getDisplayView());
             view.loadView("student/CourseView");
         }
     }
 
     /**
      * The function to handle register action event.
      * <p/>
      * @param event the action event
      */
     @FXML
     private void register(ActionEvent event)
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
 
         section.setTotalSeats(section.getTotalSeats() - 1);
         section.save();
 
     }
 
     /**
      * The function to handle drop action event.
      * <p/>
      * @param event the action event
      */
     @FXML
     private void drop(ActionEvent event)
     {
         enrolment.setIsActive(false);
         enrolment.save();
         lblResponse.setText(LocaleManager.get(32));
         lblResponse.setVisible(true);
         toggleRegDropButtons();
     }
 
     /**
      * The function initializes all the components text by locality.
      */
     private void initializeComponentsByLocale()
     {
         lblCreditName.setText(LocaleManager.get(42) + ":");
         lblGrading.setText(LocaleManager.get(63) + ":");
         lblPassReq.setText(LocaleManager.get(64) + ":");
         lblTerm.setText(LocaleManager.get(65) + ":");
         lblStart.setText(LocaleManager.get(46) + ":");
         lblEnd.setText(LocaleManager.get(45) + ":");
         lblOutPrereq.setText(LocaleManager.get(66));
         lblOutCoReq.setText(LocaleManager.get(67));
         lblRegisDL.setText(LocaleManager.get(101) + ":");
         lblDropDL.setText(LocaleManager.get(102) + ":");
     }
 
     /**
      * Initializes the controller class.
      * <p/>
      * @param url the url
      * @param rb  the resource bundle
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
         final Context context = Context.getInstance();
         context.setTitle(LocaleManager.get(62));
         BreadCrumbController.renderBreadCrumb("student/HomeView|student/BrowseCourse|student/CourseListView|student/CourseView|student/SectionView");
         try
         {
             section = context.getSelectedSection();
             enrolmentPk = new EnrolmentPK(context.getCurrentUser().getStudent().getStudentId(),
                     section.getSectionId(),
                     section.getCourse().getCoursePK().getCourseNumber(),
                     section.getCourse().getCoursePK().getDepartmentId(),
                     section.getTerm().getTermPK().getTermYear(),
                     section.getTerm().getTermPK().getSessionId(),
                     section.getSectionType().getSectionTypeId());
 
             // first check if student has already has the enrolment
             if (checkStudentAlreadyEnrolled(enrolmentPk))
             {
                 toggleRegDropButtons();
                 lblResponse.setText(LocaleManager.get(105));
                 lblResponse.setVisible(true);
                 checkAllDeadlines();
             }
             // check if the student still has not passed all the prerequisite courses
             else if (!checkStudentPrereq(section.getCourse()).isEmpty())
             {
                 btnRegister.setDisable(true);
                 lblResponse.setText(LocaleManager.get(35));
                 lblResponse.setVisible(true);
             }
             // check if there is still a seat available
             else if (!cheackSeats())
             {
                 btnRegister.setDisable(true);
                 lblResponse.setText(LocaleManager.get(109));
                 lblResponse.setVisible(true);
             }
             // check for deadlines
             else if (checkAllDeadlines())
             {
                 // check for time confict
                 if (!checkTimeConflict())
                 {
                     btnRegister.setDisable(true);
                     lblResponse.setText(LocaleManager.get(149));
                     lblResponse.setVisible(true);
                 }
             }
         }
         catch (Exception ex)
         {
             Logger.getLogger(SectionViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
         initializeComponentsByLocale();
         secLbl.setText(LocaleManager.get(74) + " " + context.getSelectedSection().getSectionPK().getSectionId());
         creditLbl.setText(Integer.toString(context.getSelectedCourse().getCredits()));
         passRqLbl.setText(context.getSelectedCourse().getPassingRequirement());
         courseDesTxt.setText(context.getSelectedCourse().getDescription());
         courseNameLbl.setText(context.getSelectedCourse().getName());
         deptIdAndCourseNoLbl.setText(context.getSelectedCourse().getDepartmentIdAndCourseNumber());
         gradingSchmLbl.setText(context.getSelectedCourse().getGradingSchemeId().getName());
         lblSecType.setText("(" + context.getSelectedSection().getSectionType().getName() + ")");
         lblSession.setText(Session.getBySessionId(context.getSelectedSection().getSectionPK().getSessionId()).getName());
         lblTermYear.setText(Integer.toString(context.getSelectedSection().getTerm().getTermPK().getTermYear()));
         populatePrereqListView(context.getSelectedCourse());
         populateCoReqListView(context.getSelectedCourse());
         lblStartDate.setText(DateFormatter.formatDate(context.getSelectedSection().getStartDate()));
         lblEndDate.setText(DateFormatter.formatDate(context.getSelectedSection().getEndDate()));
         lblRegisDLDate.setText(DateFormatter.formatDate(context.getSelectedSection().getRegisterDeadline()));
         lblDropDLDate.setText(DateFormatter.formatDate(context.getSelectedSection().getDropDeadline()));
     }
 
     /**
      * The function compares the deadline date with the current date.
      * <p/>
      * @param deadline the deadline date to be checked
      * <p/>
      * @return true, if the deadline has already passed, false otherwise
      */
     private boolean checkDeadLinePassed(Date deadline)
     {
         Date current = new Date();
         // after a deadline
         if (current.compareTo(deadline) > 0)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     /**
      * The function to check time conflict between the section about to enrol
      * and current time table.
      * <p/>
      * @return true if there is no conflict, false otherwise
      */
     private boolean checkTimeConflict()
     {
        List<SectionTimeTable> currentTab = Context.getInstance().getTimeTable();
         List<SectionTimeTable> secTabs = SectionTimeTable.getBySection(section);
         for (SectionTimeTable secTab : secTabs)
         {
             for (SectionTimeTable curTab : currentTab)
             {
                 // check day id
                 if (secTab.getSectionTimeTablePK().getDayId() == curTab.getSectionTimeTablePK().getDayId())
                 {
                     int startHour = Integer.parseInt(DateFormatter.formatHour(curTab.getSectionTimeTablePK().getStartTime()));
                     int startMin = Integer.parseInt(DateFormatter.formatMins(curTab.getSectionTimeTablePK().getStartTime()));
                     int endHour = startHour + curTab.getLengthInMinutes() / 60;
                     int endMin = startMin + curTab.getLengthInMinutes() % 60;
 
                     int startHourSec = Integer.parseInt(DateFormatter.formatHour(secTab.getSectionTimeTablePK().getStartTime()));
                     int startMinSec = Integer.parseInt(DateFormatter.formatMins(secTab.getSectionTimeTablePK().getStartTime()));
                     int endHourSec = startHour + secTab.getLengthInMinutes() / 60;
                     int endMinSec = startMin + secTab.getLengthInMinutes() % 60;
 
                     if (startHourSec >= startHour && startHourSec <= endHour)
                     {
                         if (startHourSec == endHour)
                         {
                             if (startMinSec < endMinSec)
                             {
                                 return false;
                             }
                         }
                         else
                         {
                             return false;
                         }
                     }
                     if (endHourSec >= startHour && endHourSec <= endHour)
                     {
                         if (endHourSec == startHour)
                         {
                             if (endMinSec > endMin)
                             {
                                 return false;
                             }
                         }
                         else
                         {
                             return false;
                         }
                     }
                 }
             }
         }
         return true;
     }
 
     /**
      * The function checks for the list of prerequisite courses that student has
      * already enrolled and passed, then it will remove those courses out from
      * the list.
      * <p/>
      * @param course the course to check for prerequisite course
      * <p/>
      * @return the list of remaining prerequisite courses that student has to
      *         take
      */
     private List<Prerequisite> checkStudentPrereq(Course course)
     {
         List<Prerequisite> prereqList = null;
 
         try
         {
             List<Enrolment> enrolList = Enrolment.getEnrolmentsByStudentId(Context.getInstance().getCurrentUser().getStudent().getStudentId());
             prereqList = Prerequisite.getByCourse(course);
             // check if there is no prerequisite course
             if (prereqList.isEmpty())
             {
                 return prereqList;
             }
 
             for (int i = 0; i < prereqList.size(); i++)
             {
                 for (Enrolment enrol : enrolList)
                 {
                     if (prereqList.get(i).getCourse().equals(enrol.getSection().getCourse()))
                     {
                         if (enrol.getResultId() != null)
                         {
                             if (enrol.getResultId().getName().equalsIgnoreCase("pass"))
                             {
                                 prereqList.remove(i);
                                 i--;
                                 if (prereqList.isEmpty())
                                 {
                                     return prereqList;
                                 }
                                 break;
                             }
                         }
                     }
                 }
             }
         }
         catch (Exception ex)
         {
             Logger.getLogger(SectionViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return prereqList;
     }
 
     /**
      * The function checks for the list of corequisite courses that student has
      * already enrolled and passed, then it will remove those courses out from
      * the list.
      * <p/>
      * @param course the course to check for corequisite course
      * <p/>
      * @return the list of remaining corequisite courses that student has to
      *         take
      */
     private List<CoRequisite> checkStudentCoReq(Course course)
     {
         List<CoRequisite> coReqList = null;
 
         try
         {
             List<Enrolment> enrolList = Enrolment.getEnrolmentsByStudentId(Context.getInstance().getCurrentUser().getStudent().getStudentId());
             coReqList = CoRequisite.getByCourse(course);
             // check if there is no corequisite course
             if (coReqList.isEmpty())
             {
                 return coReqList;
             }
             for (int i = 0; i < coReqList.size(); i++)
             {
                 for (Enrolment enrol : enrolList)
                 {
                     if (coReqList.get(i).getCourse().equals(enrol.getSection().getCourse()))
                     {
                         if (enrol.getResultId() != null)
                         {
                             if (enrol.getResultId().getName().equalsIgnoreCase("pass"))
                             {
                                 coReqList.remove(i);
                                 i--;
                                 if (coReqList.isEmpty())
                                 {
                                     return coReqList;
                                 }
                                 break;
                             }
                         }
                     }
                 }
             }
         }
         catch (Exception ex)
         {
             Logger.getLogger(SectionViewController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return coReqList;
     }
 
     /**
      * The function checks for seat available in the section.
      * <p/>
      * @return true if there is a seat available, false otherwise
      */
     private boolean cheackSeats()
     {
         if (Context.getInstance().getSelectedSection().getTotalSeats() > 0)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     /**
      * The function checks for register and drop deadlines, and sets the
      * appropriate buttons and labels.
      * <p/>
      * @return true if both register and drop deadlines haven't passed, false if
      *         either or both of the deadlines have passed
      */
     private boolean checkAllDeadlines()
     {
         boolean result = true;
         if (checkDeadLinePassed(Context.getInstance().getSelectedSection().getRegisterDeadline()))
         {
             btnRegister.setDisable(true);
             lblRegDLResponse.setText(LocaleManager.get(104));
             lblRegDLResponse.setVisible(true);
             result = false;
         }
         if (checkDeadLinePassed(Context.getInstance().getSelectedSection().getDropDeadline()))
         {
             btnDrop.setDisable(true);
             lblDropDLResponse.setText(LocaleManager.get(104));
             lblDropDLResponse.setVisible(true);
             result = false;
         }
         return result;
     }
 
     /**
      * The function check whether the student is already enrolled in the
      * selected EnrolmentPK or not. Note that termYear, sessionId, SectionTypeId
      * are also considered.
      * <p/>
      * @param enrolPk the EnrolmentPK that will be checking
      * <p/>
      * @return true if the student is already enrolled, false otherwise
      */
     private boolean checkStudentAlreadyEnrolled(EnrolmentPK enrolPk)
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
 
     /**
      * The function to populate the list view of PreRequisite course.
      * <p/>
      * @param selectedCourse the selected course to display the prerequisite
      *                       course for
      */
     public void populatePrereqListView(Course selectedCourse)
     {
         final List<Prerequisite> prereq = checkStudentPrereq(selectedCourse);
 
         lsvOutstandPrereq.setCellFactory(new Callback<ListView<Prerequisite>, ListCell<Prerequisite>>()
         {
             @Override
             public ListCell<Prerequisite> call(ListView<Prerequisite> param)
             {
                 ListCell<Prerequisite> cell = new ListCell<Prerequisite>()
                 {
                     @Override
                     public void updateItem(Prerequisite pre, boolean bln)
                     {
                         super.updateItem(pre, bln);
                         if (pre != null)
                         {
                             this.setText(pre.getPrerequisitePK().getPreRequisiteDeptId() + " " + pre.getPrerequisitePK().getPreRequisiteNumber());
                         }
                     }
                 };
                 return cell;
             }
         });
         lsvOutstandPrereq.getItems().setAll(prereq);
     }
 
     /**
      * The function to populate the list view of CoRequisite course.
      * <p/>
      * @param selectedCourse the selected course to display the corequisite
      *                       course for
      */
     public void populateCoReqListView(Course selectedCourse)
     {
         final List<CoRequisite> correq = checkStudentCoReq(selectedCourse);
 
         lsvOutstandCoReq.setCellFactory(new Callback<ListView<CoRequisite>, ListCell<CoRequisite>>()
         {
             @Override
             public ListCell<CoRequisite> call(ListView<CoRequisite> param)
             {
                 ListCell<CoRequisite> cell = new ListCell<CoRequisite>()
                 {
                     @Override
                     public void updateItem(CoRequisite co, boolean bln)
                     {
                         super.updateItem(co, bln);
                         if (co != null)
                         {
                             this.setText(co.getCoRequisitePK().getDepartmentId() + " " + co.getCoRequisitePK().getCoRequisiteNumber());
                         }
                     }
                 };
                 return cell;
             }
         });
         lsvOutstandCoReq.getItems().setAll(correq);
     }
 }
