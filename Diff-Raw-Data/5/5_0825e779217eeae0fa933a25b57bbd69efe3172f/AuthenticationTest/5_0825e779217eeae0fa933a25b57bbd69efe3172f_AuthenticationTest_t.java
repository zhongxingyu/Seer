 package cz.muni.fi.pv243.et.ftest;
 
 import cz.muni.fi.pv243.et.ftest.objects.ErrorMessages;
 import cz.muni.fi.pv243.et.ftest.objects.LoginForm;
 import cz.muni.fi.pv243.et.ftest.objects.Navigation;
 import cz.muni.fi.pv243.et.ftest.util.Deployments;
 import java.net.URL;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.drone.api.annotation.Drone;
 import org.jboss.arquillian.graphene.enricher.findby.FindBy;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.WebDriver;
 import static cz.muni.fi.pv243.et.ftest.util.Constants.*;
 import static org.junit.Assert.*;
 import org.openqa.selenium.WebElement;
 
 @RunWith(Arquillian.class)
 public class AuthenticationTest {
     
     @Deployment(testable = false)
     public static EnterpriseArchive deployment() {
         return Deployments.mainDeployment();
     }
     
     @Drone
     private WebDriver browser;
     
     @ArquillianResource
     private URL contextPath;
     
     @Before
     public void openContext() {
         browser.navigate().to(contextPath);
     }
     
     @FindBy(id = "login")
     LoginForm loginForm;
     
     @FindBy(id = "error-messages")
     ErrorMessages errors;
     
     @FindBy(id = "navbar")
     Navigation nav;
     
     @Test
     public void testCorrectLogin() {
         loginForm.login(ADMIN, ADMIN);
         assertTrue(nav.getLoggedInName().contains(ADMIN));
     }
     
     
     
 }
