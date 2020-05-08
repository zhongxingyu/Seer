 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public class SelectStudySegmentController implements Controller, PscAuthorizedHandler {
     private ApplicationSecurityManager applicationSecurityManager;
     private TemplateService templateService;
     private DeltaService deltaService;
     private ControllerTools controllerTools;
     private StudySegmentDao studySegmentDao;
     private AmendmentDao amendmentDao;
     private AmendmentService amendmentService;
 
     public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
         // further authorization done in handleRequest
         return ResourceAuthorization.createCollection(PscRole.valuesWithStudyAccess());
     }
 
     @SuppressWarnings({"unchecked"})
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
 
         int id = ServletRequestUtils.getRequiredIntParameter(request, "studySegment");
         StudySegment studySegment = studySegmentDao.getById(id);
 
         Integer amendId = ServletRequestUtils.getIntParameter(request, "amendment");
         Amendment amendment = null;
         if (amendId != null) {
             amendment = amendmentDao.getById(amendId);
         }
 
        studySegment = (amendment != null) ? amendmentService.getAmendedNode(studySegment, amendment) : studySegment;
 
         Study study = templateService.findStudy(studySegment);
 
         UserTemplateRelationship utr =
             new UserTemplateRelationship(applicationSecurityManager.getUser(), study);
 
        boolean isDevelopmentRequest = !StringUtils.isBlank(request.getParameter("development"));
         if ((isDevelopmentRequest && utr.getCanSeeDevelopmentVersion()) ||
             (!isDevelopmentRequest && utr.getCanSeeReleasedVersions())) {
             if (study.getDevelopmentAmendment() != null && isDevelopmentRequest) {
                 studySegment = deltaService.revise(studySegment);
                 model.put("developmentRevision", study.getDevelopmentAmendment());
                 model.put("canEdit", utr.getCanDevelop());
             } else {
                 model.put("canEdit", false);
             }
             controllerTools.addHierarchyToModel(studySegment, model);
 
             model.put("studySegment", new StudySegmentTemplate(studySegment));
             return new ModelAndView("template/ajax/selectStudySegment", model);
         } else {
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
             return null;
         }
     }
 
     @Required
     public void setControllerTools(ControllerTools controllerTools) {
         this.controllerTools = controllerTools;
     }
 
     @Required
     public void setDeltaService(DeltaService deltaService) {
         this.deltaService = deltaService;
     }
 
     @Required
     public void setTemplateService(TemplateService templateService) {
         this.templateService = templateService;
     }
 
     @Required
     public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
         this.studySegmentDao = studySegmentDao;
     }
 
     @Required
     public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
         this.applicationSecurityManager = applicationSecurityManager;
     }
 
     @Required
     public void setAmendmentDao(AmendmentDao amendmentDao) {
         this.amendmentDao = amendmentDao;
     }
 
     public void setAmendmentService(AmendmentService amendmentService) {
         this.amendmentService = amendmentService;
     }
 }
