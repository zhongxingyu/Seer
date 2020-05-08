 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.beans.propertyeditors.StringTrimmerEditor;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * @author Rhett Sutphin
  */
 public class EditPopulationController extends PscSimpleFormController {
     private PopulationDao populationDao;
     private PopulationService populationService;
     private AmendmentService amendmentService;
     private StudyDao studyDao;
     private DeltaService deltaService;
 
     protected EditPopulationController() {
         setValidator(new ValidatableValidator());
         setFormView("template/population");
         setCrumb(new Crumb());
     }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         Population pop;
         Integer popId = ServletRequestUtils.getIntParameter(request, "population");
         Integer studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
         if (popId == null) {
             pop = new Population();
         } else {
             pop = populationDao.getById(popId);
         }
         Study study = studyDao.getById(studyId);
         return new EditPopulationCommand(pop, populationService, amendmentService, populationDao, study);
     }
 
     @Override
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
         EditPopulationCommand command = (EditPopulationCommand) oCommand;
         Map<String, Object> refdata = new HashMap<String, Object>();
         getControllerTools().addToModel(command.getPopulation(), refdata);
         refdata.put("amendment", command.getStudy().getDevelopmentAmendment());
         refdata.put("study", command.getStudy());
         return refdata;
     }
 
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         binder.setAllowedFields(new String[] { "population.*" });
         binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
     }
 
     @Override
     protected ModelAndView onSubmit(
         HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors
     ) throws Exception {
         EditPopulationCommand command = (EditPopulationCommand) oCommand;
         try {
             command.apply();
         } catch (StudyCalendarValidationException scve) {
             scve.rejectInto(errors);
         }
 
         if (errors != null && errors.hasErrors()) {
             return showForm(request, response, errors);
         }
         else {
             return getControllerTools().redirectToCalendarTemplate(command.getStudy().getId(), null, command.getStudy().getDevelopmentAmendment().getId());
         }
     }
 
     /////// CONFIGURATION
 
     @Required
     public void setPopulationService(PopulationService populationService) {
         this.populationService = populationService;
     }
 
     @Required
     public void setPopulationDao(PopulationDao populationDao) {
         this.populationDao = populationDao;
     }
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     @Required
     public void setAmendmentService(AmendmentService amendmentService) {
         this.amendmentService = amendmentService;
     }
 
     @Required
     public void setDeltaService(final DeltaService deltaService) {
         this.deltaService = deltaService;
     }
 
     private static class Crumb extends DefaultCrumb {
         Logger log = LoggerFactory.getLogger(getClass());
         @Override
         public String getName(BreadcrumbContext context) {
             Population pop = context.getPopulation();
             StringBuilder name = new StringBuilder();
             if (pop.getId() == null) {
                 name.append("Create ");
             } else {
                 name.append("Edit ");
             }
             name.append("population");
             return name.toString();
         }
 
         @Override
         public Map<String, String> getParameters(BreadcrumbContext context) {
             Map<String, String> params = createParameters("study", context.getStudy().getId().toString());
             if (context.getPopulation().getId() != null) {
                 params.put("population", context.getPopulation().getId().toString());
             }
             return params;
         }
     }
 }
