 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.beans.factory.annotation.Required;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class SelectStudySegmentController implements Controller {
     private TemplateService templateService;
     private DeltaService deltaService;
     private ControllerTools controllerTools;
     private StudySegmentDao studySegmentDao;
     private static final Logger log = LoggerFactory.getLogger(SelectStudySegmentController.class.getName());
 
     @SuppressWarnings({ "unchecked" })
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
         int id = ServletRequestUtils.getRequiredIntParameter(request, "studySegment");
         StudySegment studySegment = studySegmentDao.getById(id);
         Map<String, Object> model = new HashMap<String, Object>();
         Study study = templateService.findStudy(studySegment);
         if (study.getDevelopmentAmendment() != null) {
             studySegment = deltaService.revise(studySegment);
             model.put("developmentRevision", study.getDevelopmentAmendment());
         }
         controllerTools.addHierarchyToModel(studySegment, model);
        Study theRevisedStudy = deltaService.revise(study, study.getDevelopmentAmendment());
         List<Epoch> epochs = theRevisedStudy.getPlannedCalendar().getEpochs();
         model.put("epochs", epochs);
         model.put("studySegment", new StudySegmentTemplate(studySegment));
         return new ModelAndView("template/ajax/selectStudySegment", model);
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
 }
