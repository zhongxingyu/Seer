 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.WorkflowService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Collection;
import java.util.Collections;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper.setUserAndReturnMembership;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_QA_MANAGER;
import static org.easymock.EasyMock.notNull;
import static org.easymock.classextension.EasyMock.expect;
 
 /**
  * @author Rhett Sutphin
  */
 public class ReleaseAmendmentControllerTest extends ControllerTestCase {
     private ReleaseAmendmentController mockCommandController;
     private ReleaseAmendmentController controller;
     private ReleaseAmendmentCommand mockCommand;
     private ReleaseAmendmentCommand command;
     private StudyDao studyDao;
     private AmendmentService amendmentService;
     private DeltaService deltaService;
    private WorkflowService workflowService;
 
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         studyDao = registerDaoMockFor(StudyDao.class);
         amendmentService = registerMockFor(AmendmentService.class);
         deltaService = registerMockFor(DeltaService.class);
        workflowService = registerMockFor(WorkflowService.class);
 
         mockCommand = registerMockFor(ReleaseAmendmentCommand.class);
         mockCommandController = new ReleaseAmendmentController() {
             @Override
             protected Object formBackingObject(HttpServletRequest request) throws Exception {
                 return mockCommand;
             }
         };
         mockCommandController.setStudyDao(studyDao);
         mockCommandController.setControllerTools(controllerTools);
 
         command = new ReleaseAmendmentCommand(amendmentService);
         controller = new ReleaseAmendmentController() {
             @Override
             protected Object formBackingObject(HttpServletRequest request) throws Exception {
                 return command;
             }
         };
         controller.setStudyDao(studyDao);
         controller.setControllerTools(controllerTools);
         controller.setDeltaService(deltaService);
        controller.setWorkflowService(workflowService);
        controller.setApplicationSecurityManager(applicationSecurityManager);
     }
 
     public void testAuthorizedRoles() {
         Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
         assertRolesAllowed(actualAuthorizations, STUDY_QA_MANAGER);
     }
 
     public void testBindStudy() throws Exception {
         request.setMethod("GET");
 
         int id = 4;
         request.addParameter("study", Integer.toString(id));
         Study study = setId(id, new Study());
         Study revisedStudy = study;
         PlannedCalendar pc = new PlannedCalendar();
         Epoch e = new Epoch();
         pc.addEpoch(e);
         revisedStudy.setPlannedCalendar(pc);
         expect(deltaService.revise(study, study.getDevelopmentAmendment())).andReturn(revisedStudy);
         expect(studyDao.getById(id)).andReturn(study);
        StudyWorkflowStatus studyWorkflowStatus = registerMockFor(StudyWorkflowStatus.class);
                setUserAndReturnMembership("jo", STUDY_CALENDAR_TEMPLATE_BUILDER).
            forAllStudies().forAllSites();
        expect(studyWorkflowStatus.getStructureRelatedMessages()).andReturn(Collections.EMPTY_LIST);
        expect(workflowService.build((Study) notNull(), (PscUser) notNull())).andReturn(studyWorkflowStatus);
         replayMocks();
         controller.handleRequest(request, response);
         verifyMocks();
 
         assertSame(study, command.getStudy());
     }
 
     public void testApplyOnPost() throws Exception {
         mockCommand.apply();
 
         replayMocks();
         mockCommandController.handleRequest(request, response);
         verifyMocks();
     }
 
     public void testModelAndViewOnPost() throws Exception {
         mockCommand.apply();
 
         replayMocks();
         ModelAndView mv = mockCommandController.handleRequest(request, response);
         verifyMocks();
 
         assertEquals("redirectToStudyList", mv.getViewName());
         assertEquals(0, mv.getModel().size());
     }
 }
