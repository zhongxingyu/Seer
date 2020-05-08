 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.DevelopmentTemplate;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
 import org.apache.commons.collections.CollectionUtils;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public class StudyListController extends PscAbstractController {
     private StudyDao studyDao;
     private TemplateService templateService;
     private UserDao userDao;
     private ApplicationSecurityManager applicationSecurityManager;
     private StudySiteService studySiteService;
 
     public StudyListController() {
         setCrumb(new DefaultCrumb("Studies"));
     }
 
    @Override   
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Study> studies = studyDao.getAll();
 
         List<List<StudySite>> studySiteLists = studySiteService.refreshStudySites(studyDao.getAll());
         List<Study> refreshedStudies = collectStudies(studySiteLists);
 
        studies = (List<Study>) CollectionUtils.union(studies, refreshedStudies);
 
         User user = applicationSecurityManager.getUser();
 
         List<DevelopmentTemplate> inDevelopmentTemplates = templateService.getInDevelopmentTemplates(studies, user);
         List<ReleasedTemplate> releasedTemplates = templateService.getReleasedTemplates(studies, user);
         List<ReleasedTemplate> pendingTemplates = templateService.getPendingTemplates(studies, user);
         List<ReleasedTemplate> releasedAndAssignedTemplates = templateService.getReleasedAndAssignedTemplates(studies, user);
 
         log.debug("{} released templates visible to {}", releasedTemplates.size(), user.getName());
         log.debug("{} studies open for editing by {}", inDevelopmentTemplates.size(), user.getName());
 
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("pendingTemplates", pendingTemplates);
         model.put("releasedAndAssignedTemplate", releasedAndAssignedTemplates);
         model.put("releasedAndAssignedTemplatesSize", releasedAndAssignedTemplates.size());
 
         model.put("releasedTemplates", releasedTemplates);
         model.put("inDevelopmentTemplates", inDevelopmentTemplates);
 
         return new ModelAndView("studyList", model);
     }
 
     private List<Study> collectStudies(List<List<StudySite>> studySiteLists) {
         List<Study> result = new ArrayList<Study>();
         for (List<StudySite> studySites : studySiteLists) {
             for (StudySite studySite : studySites) {
                 Study study = studySite.getStudy();
                 if (!result.contains(study)) {
                     result.add(study);
                 }
             }
         }
         return result;
     }
 
 
     ////// CONFIGURATION
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
     
     @Required
     public void setTemplateService(TemplateService templateService) {
         this.templateService = templateService;
     }
 
     @Required
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     @Required
     public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
         this.applicationSecurityManager = applicationSecurityManager;
     }
 
     @Required
     public void setStudySiteService(StudySiteService studySiteService) {
         this.studySiteService = studySiteService;
     }
 }
