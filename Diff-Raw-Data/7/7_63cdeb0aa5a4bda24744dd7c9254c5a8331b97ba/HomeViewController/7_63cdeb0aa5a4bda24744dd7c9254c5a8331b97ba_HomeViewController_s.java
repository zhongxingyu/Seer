 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.controllers.student;
 
 import adg.red.utils.Context;
 import adg.red.utils.LocaleManager;
 import adg.red.utils.ViewLoader;
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Hyperlink;
 import javafx.scene.control.Label;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.Pane;
 
 /**
  * FXML Controller class
  * <p/>
  * @author Witt
  */
 public class HomeViewController implements Initializable
 {
 
     @FXML
     private Button btnBrowseCourse;
     @FXML
     private Button btnViewDegreeInfo;
     @FXML
     private Button btnViewEnrolment;
     @FXML
     private Button btnViewTimetable;
     @FXML
     private AnchorPane disBrwCourseArea;
     @FXML
     private Hyperlink hplHome;
     @FXML
     private Hyperlink hplBrowseCourse;
     @FXML
     private static Hyperlink hplDept;
     @FXML
     private static Hyperlink hplCourse;
     @FXML
     private AnchorPane commonButtonArea;
     @FXML
     private AnchorPane homeView;
 
     public static Hyperlink getCourseLk()
     {
 
         return hplCourse;
     }
 
     public static void setCourseLk(Hyperlink courseLk)
     {
         HomeViewController.hplCourse = courseLk;
     }
 
     public static Hyperlink getDeptLk()
     {
         return hplDept;
     }
 
     public static void setDeptLk(Hyperlink deptLk)
     {
         HomeViewController.hplDept = deptLk;
     }
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
         ViewLoader view = new ViewLoader(commonButtonArea);
         view.loadView("CommonButtons");
         view = new ViewLoader(Context.getInstance().getSearchView());
         view.loadView("SearchBox");
         initializeComponentsByLocale();
         Context.getInstance().setDisplayView(disBrwCourseArea);
         // setOnAction when browse course button is pressed
         btnBrowseCourse.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 try
                 {
                     ViewLoader view = new ViewLoader(disBrwCourseArea);
                     view.loadView("student/BrowseCourse");
                     hplBrowseCourse.setVisible(true);
                     hplBrowseCourse.setText(LocaleManager.get(7) + ":");
                     hplDept.setVisible(false);
                     hplCourse.setVisible(false);
                 }
                 catch (Exception ex)
                 {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
         });
 
         // setOnAction when browse course button is pressed
         btnViewTimetable.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 try
                 {
                     ViewLoader view = new ViewLoader(disBrwCourseArea);
                     view.loadView("student/TimeTableView");
                     hplBrowseCourse.setVisible(true);
                     hplBrowseCourse.setText(LocaleManager.get(7) + ":");
                     hplDept.setVisible(false);
                     hplCourse.setVisible(false);
                 }
                 catch (Exception ex)
                 {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
         });
 
 
 //        // setOnAction when home link is clicked
         hplHome.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 try
                 {
                     ViewLoader view = new ViewLoader(Context.getInstance().getHomeView());
                     view.loadView("student/HomeView");
                 }
                 catch (Exception ex)
                 {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
 
         // setOnAction when browse course link is clicked
         hplBrowseCourse.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 try
                 {
                     if (((Hyperlink) event.getSource()).getText().contains("Browse"))
                     {
                         ViewLoader view = new ViewLoader(disBrwCourseArea);
                         view.loadView("student/BrowseCourse");
                         hplDept.setVisible(false);
                         hplCourse.setVisible(false);
                         hplDept.setVisited(false);
                         hplCourse.setVisited(false);
                     }
                     if (((Hyperlink) event.getSource()).getText().contains("faq"))
                     {
                         ViewLoader view = new ViewLoader(disBrwCourseArea);
                         view.loadView("FaqView");
                     }
                     hplBrowseCourse.setVisited(false);
                 }
                 catch (Exception ex)
                 {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
 
         // setOnAction when dept link is clicked
         hplDept.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 try
                 {
                     ViewLoader view = new ViewLoader(disBrwCourseArea);
                     view.loadView("student/CourseListView");
                     hplCourse.setVisible(false);
                     hplDept.setVisited(false);
                 }
                 catch (Exception ex)
                 {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
 
         // setOnAction when course link is clicked
         hplCourse.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 try
                 {
                     ViewLoader view = new ViewLoader(disBrwCourseArea);
                     view.loadView("student/CourseView");
                     hplCourse.setVisited(false);
                 }
                 catch (Exception ex)
                 {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
 
 
     }
 
     private void initializeComponentsByLocale()
     {
         // TODO
         btnBrowseCourse.setText(LocaleManager.get(7));
         btnViewTimetable.setText(LocaleManager.get(18));
         btnViewEnrolment.setText(LocaleManager.get(19));
         btnViewDegreeInfo.setText(LocaleManager.get(20));
         hplHome.setText(LocaleManager.get(24) + ":");
 
     }
 }
