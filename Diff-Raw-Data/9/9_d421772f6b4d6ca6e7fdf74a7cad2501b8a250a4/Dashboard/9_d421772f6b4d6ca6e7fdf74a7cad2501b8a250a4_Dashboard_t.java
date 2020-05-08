 package controllers;
 
 import controllers.abstracts.AppController;
 import controllers.security.Auth;
 import controllers.security.LoggedAccess;
 import helpers.YearCourseHelper;
 import models.school.Prestation;
 import models.school.SoeExam;
 import models.school.YearCourse;
 import models.user.Profile;
 import service.PrestationService;
 import service.SoeExamService;
 import service.YearCourseService;
 
 import javax.inject.Inject;
 import java.util.List;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 @LoggedAccess
 public class Dashboard extends AppController {
 
     @Inject
     private static YearCourseService yearCourseService;
 
     @Inject
     private static PrestationService prestationService;
 
     @Inject
     private static SoeExamService soeExamService;
 
     public static void index() {
 
         pageHelper().addControllerTitle();
 
 //        List<YearCourse> yearCourses;
 //
 //        if (Auth.getCurrentUser().profile == Profile.ADMIN) {
 //            yearCourses = yearCourseService.getAllCoursesForYear(YearCourseHelper.getCurrentYear());
 //        } else {
 //            yearCourses = yearCourseService.getAvailableCoursesForUser(Auth.getCurrentUser());
 //        }
 
         List<Prestation> prestations = prestationService.getPrestationsByProfessorAndYear(Auth.getCurrentUser(), YearCourseHelper.getCurrentYear());
        List<SoeExam> soeExams = soeExamService.getSoeExamsByExaminatorAndYear(Auth.getCurrentUser(), YearCourseHelper.getCurrentYear());
 
         render(prestations);
     }
 }
