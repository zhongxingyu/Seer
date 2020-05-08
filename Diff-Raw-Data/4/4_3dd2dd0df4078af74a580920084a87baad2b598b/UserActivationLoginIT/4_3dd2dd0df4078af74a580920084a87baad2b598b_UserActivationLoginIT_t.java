 package org.osiam.client;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseOperation;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.DatabaseTearDown;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.connector.OsiamConnector;
 import org.osiam.client.exception.ConnectionInitializationException;
 import org.osiam.client.oauth.AccessToken;
 import org.osiam.client.oauth.GrantType;
 import org.osiam.client.oauth.Scope;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
import static org.junit.Assert.fail;

 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @DatabaseSetup(value = "/database_seed_activation.xml")
 @DatabaseTearDown(value = "/database_seed_activation.xml", type = DatabaseOperation.DELETE_ALL)
 public class UserActivationLoginIT extends AbstractIntegrationTestBase {
 
     @Test(expected = ConnectionInitializationException.class)
     public void log_in_as_an_deactivated_user_is_impossible() {
         getAccessToken("hsimpson", "koala");
        fail("Exception expected");
     }
 
     private AccessToken getAccessToken(String userName, String password) {
         return new OsiamConnector.Builder()
                 .setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS)
                 .setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                 .setClientId("example-client")
                 .setClientSecret("secret")
                 .setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS)
                 .setUserName(userName)
                 .setPassword(password)
                 .setScope(Scope.ALL)
                 .build()
                 .retrieveAccessToken();
     }
 }
