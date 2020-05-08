 package edu.northwestern.bioinformatics.studycalendar.web.subject;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
 import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
 import gov.nih.nci.cabig.ctms.editors.GridIdentifiableDaoBasedEditor;
 import gov.nih.nci.cabig.ctms.lang.NowFactory;
 import org.apache.commons.collections.Predicate;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.ServletRequestBindingException;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
 import static org.apache.commons.collections.CollectionUtils.select;
 
 /**
  * @author Rhett Sutphin
  */
 public class SubjectCentricScheduleController extends PscAbstractController implements PscAuthorizedHandler {
     private SubjectDao subjectDao;
     private StudySubjectAssignmentDao studySubjectAssignmentDao;
     private ScheduledCalendarDao scheduledCalendarDao;
     private NowFactory nowFactory;
     private ApplicationSecurityManager applicationSecurityManager;
 
     public SubjectCentricScheduleController() {
         setCrumb(new Crumb());
     }
 
     public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
         return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER, DATA_READER, STUDY_TEAM_ADMINISTRATOR);
     }
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         Subject subject = interpretUsingIdOrGridId(request, "subject", subjectDao);
 
         // Try assignment
         if (subject == null) {
             StudySubjectAssignment assignment = interpretUsingIdOrGridId(request, "assignment", studySubjectAssignmentDao);
             if (assignment != null) subject = assignment.getSubject();
         }
         // Try calendar
         if (subject == null) {
             ScheduledCalendar calendar = interpretUsingIdOrGridId(request, "calendar", scheduledCalendarDao);
             if (calendar != null) subject = calendar.getAssignment().getSubject();
         }
         
         if (subject == null) {
             response.sendError(HttpServletResponse.SC_NOT_FOUND, "No matching subject found");
             return null;
         }
 
         List<UserStudySubjectAssignmentRelationship> assignments =
             new ArrayList<UserStudySubjectAssignmentRelationship>(subject.getAssignments().size());
         for (StudySubjectAssignment ssa : subject.getAssignments()) {
             assignments.add(new UserStudySubjectAssignmentRelationship(
                 applicationSecurityManager.getUser(), ssa));
         }
         MultipleAssignmentScheduleView schedule = new MultipleAssignmentScheduleView(
             assignments, nowFactory);
 
         ModelMap model = new ModelMap("schedule", schedule);
         model.addAttribute(subject);
         model.addAttribute("schedulePreview", false);
         model.addAttribute("canUpdateSchedule", canUpdateSchedule(assignments));
         return new ModelAndView("subject/schedule", model);
     }
 
     @SuppressWarnings({"unchecked"})
     private <T extends GridIdentifiable & DomainObject> T interpretUsingIdOrGridId(
         HttpServletRequest request, String paramName, GridIdentifiableDao<T> dao
     ) throws ServletRequestBindingException {
         DaoBasedEditor editor = new GridIdentifiableDaoBasedEditor(dao);
        editor.setAsText(ServletRequestUtils.getStringParameter(request, paramName));
         return (T) editor.getValue();
     }
 
     @SuppressWarnings({"unchecked"})
     private boolean canUpdateSchedule(List<UserStudySubjectAssignmentRelationship> assignments) {
         Collection<UserStudySubjectAssignmentRelationship> update = select(assignments, new Predicate() {
             public boolean evaluate(Object o) {
                 UserStudySubjectAssignmentRelationship r = (UserStudySubjectAssignmentRelationship) o;
                 return r.getCanUpdateSchedule();
             }
         });
 
         return isNotEmpty(update);
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setSubjectDao(SubjectDao subjectDao) {
         this.subjectDao = subjectDao;
     }
 
     @Required
     public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
         this.studySubjectAssignmentDao = studySubjectAssignmentDao;
     }
 
     @Required
     public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
         this.scheduledCalendarDao = scheduledCalendarDao;
     }
 
     @Required
     public void setNowFactory(NowFactory nowFactory) {
         this.nowFactory = nowFactory;
     }
 
     @Required
     public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
         this.applicationSecurityManager = applicationSecurityManager;
     }
 
     private static class Crumb extends DefaultCrumb {
         @Override
         public String getName(DomainContext context) {
             return new StringBuilder()
                 .append("Comprehensive schedule for ").append(context.getSubject().getFullName())
                 .toString();
         }
 
         @Override
         public Map<String, String> getParameters(DomainContext context) {
             return createParameters(
                 "subject", context.getSubject().getId().toString()
             );
         }
     }
 }
