 package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
 import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
 import static org.easymock.EasyMock.expect;
 
 import javax.servlet.http.HttpServletRequest;
 import static java.util.Arrays.asList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author John Dzak
  */
 public class AbstractAssignSubjectCoordinatorControllerTest extends ControllerTestCase {
     AbstractAssignSubjectCoordinatorController controller;
     private StudyDao studyDao;
     private UserDao userDao;
     private SiteDao siteDao;
     private TemplateService templateService;
     private UserService userService;
     private AuthorizationService authorizationService;
 
     private AbstractAssignSubjectCoordinatorCommand command;
 
     private List<Site> sites;
     private List<Study> studies;
     private User siteCoordinator;
     private Site site0, site1;
     private User user0, user1;
     private Study study0, study1;
 
     private List<User> users;
     private UserRole siteCoordinatorRole;
     private StudySiteService studySiteService;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         siteDao         = registerDaoMockFor(SiteDao.class);
         userDao         = registerDaoMockFor(UserDao.class);
         studyDao        = registerDaoMockFor(StudyDao.class);
         templateService = registerMockFor(TemplateService.class);
         userService     = registerMockFor(UserService.class);
         studySiteService = registerMockFor(StudySiteService.class);
         authorizationService = registerMockFor(AuthorizationService.class);
 
         controller = new SimpleAssignSubjectCoordinatorController();
         controller.setSiteDao(siteDao);
         controller.setStudyDao(studyDao);
         controller.setTemplateService(templateService);
         controller.setStudySiteService(studySiteService);
         // TODO: indirect mocking like this is a bad idea
         applicationSecurityManager.setUserService(registerMockFor(UserService.class));
         controller.setApplicationSecurityManager(applicationSecurityManager);
         controller.setAuthorizationService(authorizationService);
 
         command = registerMockFor(SimpleAssignSubjectCoordinatorCommand.class);
 
         user0 = createNamedInstance("John", User.class);
         user1 = createNamedInstance("Jake", User.class);
         users = asList(user0, user1);
 
         site0 = createNamedInstance("Mayo Clinic", Site.class);
         site1 = createNamedInstance("Northwestern", Site.class);
         sites = asList(site0, site1);
 
         study0 = createNamedInstance("Study A", Study.class);
         study1 = createNamedInstance("Study B", Study.class);
         study1.setAmendment(new Amendment());
         study1.setStudySites(asList(createStudySite(study1, site0)));
 
         studies = asList(study0, study1);
 
         siteCoordinator = createUser(1, "Site Coord", 1L, true);
         siteCoordinatorRole = createUserRole(siteCoordinator, Role.SITE_COORDINATOR, site0, site1);
         siteCoordinator.addUserRole(siteCoordinatorRole);
 
         SecurityContextHolderTestHelper.setSecurityContext(siteCoordinator, "pass");
     }
 
     public void testGetRefData() throws Exception {
         request.setMethod("GET");
         expectRefData();
         replayMocks();
         
         controller.handleRequest(request, response);
         verifyMocks();
     }
 
     public void testGetSiteCoordinator() throws Exception {
         expect(applicationSecurityManager.getFreshUser()).andReturn(siteCoordinator).anyTimes();
         replayMocks();
 
         User actualSiteCoord = controller.getSiteCoordinator();
         verifyMocks();
         assertEquals("Wrong site coordinator", actualSiteCoord.getName(), siteCoordinator.getName());
     }
 
     public void testGetAssignableStudies() throws Exception {
         expect(studyDao.getAll()).andReturn(studies);
         expect(studySiteService.refreshStudySitesForStudies(studies)).andReturn(asList(
                 study0.getStudySites(),
                 study1.getStudySites()
         ));
         expect(authorizationService.filterStudiesForVisibility(studies, siteCoordinatorRole)).andReturn(studies);
         replayMocks();
 
         List<Study> actualAssignableStudies = controller.getAssignableStudies(siteCoordinator);
         verifyMocks();
 
         assertEquals("Wrong site", study1.getName(), actualAssignableStudies.get(0).getName());
     }
 
     public void testGetAssignableSites() throws Exception {
         replayMocks();
         List<Site> actualAssignableSites = controller.getAssignableSites(siteCoordinator);
         verifyMocks();
 
         assertEquals("Wrong number of sites", sites.size(), actualAssignableSites.size());
         assertEquals("Wrong site", site0.getName(), actualAssignableSites.get(0).getName());
     }
 
     public void testGetAssignableSitesDependingOnStudy() throws Exception {
         replayMocks();
         List<Site> actualAssignableSites = controller.getAssignableSites(siteCoordinator, study1);
         verifyMocks();
 
         assertEquals("Wrong Site", site0.getName(), actualAssignableSites.get(0).getName());
     }
 
     public void expectRefData() throws Exception{
         expect(command.getAssignableStudies()).andReturn(studies);
         expect(command.getAssignableSites()).andReturn(sites);
         expect(command.getAssignableUsers()).andReturn(users);
     }
 
     private class SimpleAssignSubjectCoordinatorController extends AbstractAssignSubjectCoordinatorController {
         public SimpleAssignSubjectCoordinatorController() {
             setSiteDao(siteDao);
             setStudyDao(studyDao);
             setUserDao(userDao);
             setUserService(userService);
             setSuccessView("foo");
         }
 
         @Override
         protected Object formBackingObject(HttpServletRequest request) throws Exception {
             return command;
         }
     }
 
     private class SimpleAssignSubjectCoordinatorCommand extends AbstractAssignSubjectCoordinatorCommand<Study, Site> {
         private Map<Study, Map<Site, GridCell>> studyAssignmentGrid = new HashMap<Study, Map<Site, GridCell>>();
         public SimpleAssignSubjectCoordinatorCommand(List assignableStudies, List assignableSites, List assignableUsers) {
             super(assignableStudies, assignableSites, assignableUsers);
         }
 
         @Override
         public Map<Study, Map<Site, GridCell>> getGrid() {
             return studyAssignmentGrid;
         }
 
         @Override
         protected void performCheckAction(Study row, Site column) throws Exception {
             throw new UnsupportedOperationException();
         }
 
         @Override
         protected void performUncheckAction(Study row, Site column) throws Exception {
             throw new UnsupportedOperationException();
         }
 
         @Override
         protected boolean isSiteSelected(Study rowElement, Site columnElement) {
             throw new UnsupportedOperationException();
         }
 
         @Override
         protected boolean isSiteAccessAllowed(Study rowElement, Site columnElement) {
             throw new UnsupportedOperationException();
         }
     }
 }
