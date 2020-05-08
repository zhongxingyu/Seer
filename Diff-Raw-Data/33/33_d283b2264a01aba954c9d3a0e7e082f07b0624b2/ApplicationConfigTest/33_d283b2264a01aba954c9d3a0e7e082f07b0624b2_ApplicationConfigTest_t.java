 package in.shekhar.forumapp.config;
 
 import static org.junit.Assert.assertNotNull;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = ApplicationConfig.class)
 @ActiveProfiles("dev")
@WebAppConfiguration
 public class ApplicationConfigTest {
 
    @PersistenceContext
    EntityManager entityManager;
 
    @Test
    public void shouldLoadApplicationContext() {
        assertNotNull(entityManager);
    }
 
 }
