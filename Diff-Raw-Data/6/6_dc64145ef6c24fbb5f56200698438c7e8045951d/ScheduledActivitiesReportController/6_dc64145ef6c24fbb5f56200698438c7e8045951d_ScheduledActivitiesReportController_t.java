 package edu.northwestern.bioinformatics.studycalendar.web.reporting;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
 import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
 import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
 import org.springframework.beans.propertyeditors.StringTrimmerEditor;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 /**
  * @author John Dzak
  */
 @AccessControl(roles = {Role.STUDY_ADMIN, Role.STUDY_COORDINATOR})
 public class ScheduledActivitiesReportController extends PscAbstractCommandController {
     private ScheduledActivitiesReportRowDao dao;
     private ControllerTools controllerTools;
     private UserDao userDao;
 
     public ScheduledActivitiesReportController() {
         setCommandClass(ScheduledActivitiesReportCommand.class);
         setCrumb(new DefaultCrumb("Report"));
     }
 
     protected Object getCommand(HttpServletRequest request) throws Exception {
         return new ScheduledActivitiesReportCommand(new ScheduledActivitiesReportFilters());
     }
 
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         binder.registerCustomEditor(ScheduledActivityMode.class, "filters.currentStateMode",
             new ControlledVocabularyEditor(ScheduledActivityMode.class, true));
         binder.registerCustomEditor(ActivityType.class, "filters.activityType",
             new ControlledVocabularyEditor(ActivityType.class, true));
         binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
         binder.registerCustomEditor(Date.class, "filters.actualActivityDate.start", controllerTools.getDateEditor(false));
         binder.registerCustomEditor(Date.class, "filters.actualActivityDate.stop", controllerTools.getDateEditor(false));
         binder.registerCustomEditor(User.class, "filters.subjectCoordinator", new DaoBasedEditor(userDao));
     }
 
     @Override
     protected ModelAndView handle(Object oCommand, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
         ScheduledActivitiesReportCommand command = (ScheduledActivitiesReportCommand) oCommand;
         return new ModelAndView("reporting/scheduledActivitiesReport", createModel(errors, search(errors, command)));
     }
 
     private List<ScheduledActivitiesReportRow> search(Errors errors, ScheduledActivitiesReportCommand command) {
         if (errors.hasErrors()) {
             return Collections.emptyList();
         } else {
             return search(command);
         }
     }
 
     protected List<ScheduledActivitiesReportRow> search(ScheduledActivitiesReportCommand command) {
         if (command.getLabel()!= null ) {
             command.setLabelFilter();                                 
         }
         List<ScheduledActivitiesReportRow> scheduledActivitiesReportRow = dao.search(command.getFilters());
         //todo - mounting for label is not working... need to set it manually
        if (command.getLabel() != null) {
            for (ScheduledActivitiesReportRow row : scheduledActivitiesReportRow) {
                row.setLabel(command.getLabel());
            }
         }
         return scheduledActivitiesReportRow;
     }
 
     @SuppressWarnings({"unchecked"})
     protected Map createModel(BindException errors, List<ScheduledActivitiesReportRow> results) {
         Map<String, Object> model = errors.getModel();
         model.put("modes", ScheduledActivityMode.values());
         model.put("types", ActivityType.values());
         model.put("coordinators", userDao.getAllSubjectCoordinators());
         model.put("results", results);
         model.put("resultSize", results.size());
         return model;
     }
 
     ////// Bean Setters
     public void setScheduledActivitiesReportRowDao(ScheduledActivitiesReportRowDao dao) {
         this.dao = dao;
     }
 
     public void setControllerTools(ControllerTools controllerTools) {
         this.controllerTools = controllerTools;
     }
 
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 }
