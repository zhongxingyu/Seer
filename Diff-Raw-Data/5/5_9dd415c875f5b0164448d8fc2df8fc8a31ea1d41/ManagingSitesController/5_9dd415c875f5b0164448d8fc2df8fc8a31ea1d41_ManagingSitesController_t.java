 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
 import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_QA_MANAGER;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
 
 /**
  * @author Nataliya Shurupova
  */
 public class ManagingSitesController extends PscSimpleFormController implements PscAuthorizedHandler {
     private StudyDao studyDao;
     private ApplicationSecurityManager applicationSecurityManager;
     private SiteDao siteDao;
     private StudyService studyService;
 
     public ManagingSitesController() {
         setCommandClass(ManagingSitesCommand.class);
         setValidator(new ValidatableValidator());
         setFormView("template/managingSites");
     }
 
     public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
         Study study;
         try {
             study = studyDao.getById(Integer.parseInt(queryParameters.get("id")[0]));
         } catch (RuntimeException e) {
             study = null;
         }
         return ResourceAuthorization.createTemplateManagementAuthorizations(study, STUDY_QA_MANAGER, STUDY_CALENDAR_TEMPLATE_BUILDER);
     }
 
     @SuppressWarnings({ "unchecked" })
     @Override
      protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
         PscUser user = applicationSecurityManager.getUser();
         Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
         SuiteRoleMembership membershipForStudyQAManager = user.getMembership(PscRole.STUDY_QA_MANAGER);
         SuiteRoleMembership membershipForCalendarTemplateBuilders = user.getMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
 
         if (membershipForCalendarTemplateBuilders == null && membershipForStudyQAManager == null) {
             throw new StudyCalendarSystemException("UserRoles don't exist for the specified user");
         }
 
         List<SuiteRoleMembership> listOfRoles = new ArrayList<SuiteRoleMembership>();
         listOfRoles.add(membershipForStudyQAManager);
         listOfRoles.add(membershipForCalendarTemplateBuilders);
 
         return new ManagingSitesCommand(study, studyService, siteDao, listOfRoles);
     }
 
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         getControllerTools().registerDomainObjectEditor(binder, "sites", siteDao);
         binder.registerCustomEditor(Site.class, new DaoBasedEditor(siteDao));
     }
 
     @Override
     protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest, Object oCommand, Errors errors) throws Exception {
         ManagingSitesCommand command = ((ManagingSitesCommand) oCommand);
         Map<String, Object> refdata = new HashMap<String, Object>();
         refdata.put("isAllSites", command.getAllSitesAccess());
         Study study = command.getStudy();
         refdata.put("study", study);
        refdata.put("amendmentId", ServletRequestUtils.getIntParameter(httpServletRequest, "amendment"));
         refdata.put("isManaged", study.isManaged());
         refdata.put("userSitesToManage", command.getSelectableSites());
         refdata.put("managingSites", command.getManagingSites());
         return refdata;
     }
 
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         ManagingSitesCommand assignCommand = (ManagingSitesCommand) oCommand;
         assignCommand.apply();
        return getControllerTools().redirectToCalendarTemplate(ServletRequestUtils.getIntParameter(request, "id"),
                                                                null, ServletRequestUtils.getIntParameter(request, "amendment"));
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     @Required
     public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
         this.applicationSecurityManager = applicationSecurityManager;
     }
 
     @Required
     public void setStudyService(StudyService studyService) {
         this.studyService = studyService;
     }
 }
