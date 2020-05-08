 package smartpool.web;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 import smartpool.domain.Buddy;
 import smartpool.domain.LDAPResultSet;
 import smartpool.service.BuddyService;
 import smartpool.service.LDAPService;
 import smartpool.web.form.CreateProfileForm;
 
 import javax.servlet.http.HttpServletRequest;
 
 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.Matchers.endsWith;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static smartpool.util.matchers.ReflectionMatcher.reflectionEquals;
 
 public class BuddyProfileControllerTest {
 
     @Mock
     BuddyService buddyProfileService;
     private BuddyProfileController buddyProfileController;
 
     @Mock
     private HttpServletRequest request;
 
     @Mock
     private LDAPService ldapService;
     private ModelMap model;
     private Buddy buddy;
 
     @Before
     public void setUp() {
         initMocks(this);
 
         when(ldapService.searchByUserName("mzhao")).thenReturn(new LDAPResultSet("Ming Zhao", "mzhao@thoughtworks.com"));
         when(buddyProfileService.getUserNameFromCAS(request)).thenReturn("mzhao");
 
         buddy = new Buddy("Ming");
         when(buddyProfileService.getBuddy("mzhao")).thenReturn(buddy);
 
         model = new ModelMap();
 
         buddyProfileController = new BuddyProfileController(buddyProfileService, ldapService);
     }
 
     @Test
     public void shouldReturnBuddyProfile() {
         String response = buddyProfileController.viewProfile("mzhao", model);
 
         assertThat(response, equalTo("buddy/viewUserProfile"));
         assertThat((Buddy) model.get("buddyProfile"), equalTo(buddy));
     }
 
     @Test
     public void shouldReturnCurrentlyLoggedInUserProfile() {
         Buddy currentlyLoggedInUser = new Buddy("mzhao");
         when(buddyProfileService.getCurrentBuddy(request)).thenReturn(currentlyLoggedInUser);
 
         String response = buddyProfileController.viewMyProfile(model, request);
 
         assertThat(response, equalTo("buddy/viewUserProfile"));
         assertThat((Buddy) model.get("buddyProfile"), is(currentlyLoggedInUser));
     }
 
     @Test
     public void shouldRenderPrePopulatedCreateProfileForm() throws Exception {
         String jsp = buddyProfileController.renderForm(model, request);
 
         assertThat(jsp, is("buddy/form"));
         assertThat((CreateProfileForm) model.get("createProfileForm"),
                 reflectionEquals(new CreateProfileForm("mzhao", "Ming Zhao", "mzhao@thoughtworks.com", null, null, null, null)));
     }
 
     @Test
     public void shouldShowErrorPageIfMissingMandatoryInformation() throws Exception {
        CreateProfileForm invalidForm = new CreateProfileForm(null, null, null, "", "", "", "10:00");
         BindException errors = new BindException(invalidForm, "createProfileForm");
 
         ModelAndView modelAndView = buddyProfileController.submit(invalidForm, errors, request);
 
         assertThat(modelAndView.getViewName(), is("buddy/form"));
         assertThat((CreateProfileForm) modelAndView.getModel().get("createProfileForm"),
                reflectionEquals(new CreateProfileForm("mzhao", "Ming Zhao", "mzhao@thoughtworks.com", "", "", "", "10:00")));
         assertThat(errors.hasErrors(), is(true));
     }
 
     @Test
     public void shouldCreateProfileIfInformationIsProvided() throws Exception {
        CreateProfileForm profileForm = new CreateProfileForm("mzhao", "Ming Zhao", "mzhao@thoughtworks.com", "", "234567809876", "", "10:00");
         BindException errors = new BindException(profileForm, "createProfileForm");
 
         ModelAndView modelAndView = buddyProfileController.submit(profileForm, errors, request);
 
         assertThat(modelAndView.getView(), instanceOf(RedirectView.class));
         assertThat(((RedirectView) modelAndView.getView()).getUrl(), endsWith("/buddyProfile"));
     }
 
 }
