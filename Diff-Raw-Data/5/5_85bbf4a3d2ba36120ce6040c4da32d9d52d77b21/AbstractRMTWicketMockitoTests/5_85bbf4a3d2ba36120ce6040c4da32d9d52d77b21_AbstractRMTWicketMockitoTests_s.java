 package de.flower.rmt.test;
 
 import de.flower.common.test.wicket.AbstractWicketMockitoTests;
 import de.flower.common.test.wicket.WicketTester;
 import de.flower.rmt.model.User;
 import de.flower.rmt.service.security.ISecurityService;
 import de.flower.rmt.ui.app.TestApplication;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.springframework.context.ApplicationContext;
 import org.testng.annotations.BeforeMethod;
 
 import static org.mockito.Mockito.when;
 
 /**
  * @author flowerrrr
  */
 
 public abstract class AbstractRMTWicketMockitoTests extends AbstractWicketMockitoTests {
 
 
     @Override
     protected WicketTester createWicketTester(final ApplicationContext mockCtx) {
         WebApplication webApp = new TestApplication(mockCtx);
         WicketTester wicketTester = new WicketTester(webApp);
 /*
         wicketTester.getLoggingSerializerFilter().addInclusion("\"de\\.flower\\.rmt\\.model\\.[^-]*?\"");
         wicketTester.getLoggingSerializerFilter().addExclusion(RSVPStatus.class.getName());
         wicketTester.getLoggingSerializerFilter().addExclusion(Password.class.getName());
 */
         return wicketTester;
     }
 
     @BeforeMethod
     public void initSecurityService() {
         ISecurityService securityService = mockCtx.getMock(ISecurityService.class);
         User user = TestData.newUser();
         when(securityService.getUser()).thenReturn(user);
     }
 
 
 }
